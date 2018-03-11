package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformPdfEncryptException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.AbbyyUtils;
import com.oddrock.caj2pdf.utils.CalibreUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.MicrosoftWordUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Pdf2EpubUtils {

	public static TransformFileSet pdf2epub(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException {	
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File pdfFile = new File(pdfFilePath);
		if(!pdfFile.exists() || !pdfFile.isFile() || !pdfFile.getCanonicalPath().toLowerCase().endsWith(".pdf")) {
			return result;
		}
		result.setSrcFile(pdfFile);
		// 关闭word
		MicrosoftWordUtils.close();
		// 关闭ABBYY
		AbbyyUtils.close();
		// 用ABBYY打开指定PDF文件
		AbbyyUtils.openPdf(robotMngr, pdfFilePath);
		// 打开“文件”菜单
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_F);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 去掉输入法
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SPACE);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“另存为”
		robotMngr.pressKey(KeyEvent.VK_V);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“epub”
		robotMngr.pressKey(KeyEvent.VK_E);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待出现文件名输入框
		AbbyyUtils.waitToInputfilename(robotMngr);
		// 将文件名的pdf替换为docx，保存到剪贴板，word文件就保存在原地
		File wordFile = new File(pdfFile.getCanonicalPath().replaceAll(".pdf$", ".epub"));
		result.setDstFile(wordFile);
		ClipboardUtils.setSysClipboardText(wordFile.getCanonicalPath());
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板的文件名复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击“保存按钮”
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 确认覆盖已有文件
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_Y);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待准换开始直到完成
		AbbyyUtils.waitPdf2epubTransformingFinised(robotMngr);
		// 等待calibre彻底打开
		CalibreUtils.waitToOpen(robotMngr);
		Common.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭ABBYY
		AbbyyUtils.close();
		Common.wait(Prop.getInt("interval.waitmillis"));
		// 关闭calibre
		CalibreUtils.close();
		Common.wait(Prop.getInt("interval.waitmillis"));
		return result;
	}
	
	public static void pdf2epub_batch(TransformInfoStater tfis) throws TransformNofileException, IOException, InterruptedException, TransformWaitTimeoutException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException("目录里没有pdf文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet;
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
	}
	
	public static void pdf2epub_test(TransformInfoStater tfis) throws IOException, TransformNofileException, InterruptedException, TransformPdfEncryptException, TransformWaitTimeoutException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException("目录里没有pdf文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(pdfFile==null) {
				pdfFile = file;
			}else {
				if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
					pdfFile = file;
				}
			}
		}
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException, TransformWaitTimeoutException {
		pdf2epub(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\装配式建筑施工安全评价体系研究_杨爽.pdf");
	}

}
