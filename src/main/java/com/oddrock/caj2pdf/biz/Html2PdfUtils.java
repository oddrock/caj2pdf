package com.oddrock.caj2pdf.biz;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.AbbyyUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.FoxitUtils;
import com.oddrock.caj2pdf.utils.MicrosoftWordUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Html2PdfUtils {
	private static Logger logger = Logger.getLogger(Html2PdfUtils.class);
	
	public static void html2pdf_batch(TransformInfoStater tfis) throws TransformNofileException, IOException, TransformWaitTimeoutException, InterruptedException {
		if(!tfis.hasFileToTransform())  {
			tfis.setErrorMsg("目录里没有html文件");
			throw new TransformNofileException("目录里没有html文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet;
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			// 将单个html文件转换为pdf
			fileSet = Html2PdfUtils.html2pdf(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
	}

	private static TransformFileSet html2pdf(RobotManager robotMngr, String htmlFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException {
		logger.warn("sda"+htmlFilePath);
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File htmlFile = new File(htmlFilePath);
		if(htmlFile==null || !htmlFile.exists() || !htmlFile.isFile()) {
			return result;
		}
		result.setSrcFile(htmlFile);
		// 关闭foxit
		FoxitUtils.close();
		// 关闭ABBYY
		AbbyyUtils.close();
		// 用ABBYY打开指定html文件
		AbbyyUtils.openPdf(robotMngr, htmlFilePath);
		// 打开“文件”菜单
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_F);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 去掉输入法
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SPACE);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“另存为”
		robotMngr.pressKey(KeyEvent.VK_V);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“pdf”
		robotMngr.pressKey(KeyEvent.VK_U);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待出现文件名输入框
		AbbyyUtils.waitToInputfilename(robotMngr);
		// 将文件名的pdf替换为docx，保存到剪贴板，word文件就保存在原地
		File pdfFile = null;
		if(htmlFilePath.toLowerCase().endsWith("html")) {
			pdfFile = new File(htmlFile.getCanonicalPath().replaceAll(".html$", ".pdf"));
		}else {
			pdfFile = new File(htmlFile.getCanonicalPath().replaceAll(".htm$", ".pdf"));
		}
		result.setDstFile(pdfFile);
		ClipboardUtils.setSysClipboardText(pdfFile.getCanonicalPath());
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
		Common.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭ABBYY
		AbbyyUtils.close();
		Common.wait(Prop.getInt("interval.waitmillis"));
		return result;
	}
	

}
