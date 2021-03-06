package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.bean.TransformFileSetEx;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformPdfEncryptException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.AbbyyUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.MicrosoftWordUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;
import com.oddrock.common.windows.ClipboardUtils;

/**
 * Pdf2Word转换工具类
 * @author qzfeng
 *
 */
public class Pdf2WordUtils {
	private static Logger logger = Logger.getLogger(Pdf2WordUtils.class);
	
	/**
	 * 单个pdf转word
	 * @param pdfFilePath
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws TransformWaitTimeoutException 
	 */
	public static TransformFileSet pdf2word(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException {	
		logger.warn("sda"+pdfFilePath);
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
		// 选择“word”
		robotMngr.pressKey(KeyEvent.VK_W);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待出现文件名输入框
		AbbyyUtils.waitToInputfilename(robotMngr);
		// 将文件名的pdf替换为docx，保存到剪贴板，word文件就保存在原地
		File wordFile = new File(pdfFile.getCanonicalPath().replaceAll(".pdf$", ".docx"));
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
		AbbyyUtils.waitPdf2wordTransformingFinised(robotMngr);
		// 等待Word彻底打开
		MicrosoftWordUtils.waitToOpen(robotMngr);
		Common.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭ABBYY
		AbbyyUtils.close();
		Common.wait(Prop.getInt("interval.waitmillis"));
		// 关闭word
		MicrosoftWordUtils.close();
		Common.wait(Prop.getInt("interval.waitmillis"));
		return result;
	}
	
	public static void pdf2word_batch(TransformInfoStater tfis) throws TransformNofileException, IOException, InterruptedException, TransformWaitTimeoutException {
		if(!tfis.hasFileToTransform())  {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException("目录里没有pdf文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet;
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			// 将单个pdf文件转换为word
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
	}
	
	public static void pdf2word_test(TransformInfoStater tfis) throws TransformNofileException, IOException, InterruptedException, TransformPdfEncryptException, TransformWaitTimeoutException {
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
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2WordUtils.pdf2word(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException, TransformWaitTimeoutException {
		pdf2word(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf");
	}

	public static TransformFileSetEx pdf2word_single(File file, RobotManager robotMngr) throws IOException, TransformWaitTimeoutException, InterruptedException {
		logger.warn("开始转换:"+file.getCanonicalPath());
		TransformFileSetEx result = new TransformFileSetEx();
		if(file==null || !file.exists() || !file.isFile()) {
			logger.warn("文件不存在或文件为空");
			return result;
		}
		File srcFile = TransformRuleUtils.isQualifiedSrcFile(file, "pdf2word");
		if(srcFile==null) {
			logger.warn("文件不是要转换的类型："+file.getCanonicalPath());
			return result;
		}
		result.addSrcFile(srcFile);
		TransformFileSet tmpFileSet = Pdf2WordUtils.pdf2word(robotMngr, srcFile.getCanonicalPath());
		if(tmpFileSet!=null && tmpFileSet.getDstFile()!=null) {
			result.addDstFile(tmpFileSet.getDstFile());
			result.setSuccess(true);
		}
		logger.warn("结束转换:"+file.getCanonicalPath());
		return result;
	}
}
