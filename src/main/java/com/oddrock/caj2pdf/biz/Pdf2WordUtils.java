package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.utils.AbbyyUtils;
import com.oddrock.caj2pdf.utils.CommonUtils;
import com.oddrock.caj2pdf.utils.MicrosoftWordUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

/**
 * Pdf2Word转换工具类
 * @author qzfeng
 *
 */
public class Pdf2WordUtils {
	
	/**
	 * 单个pdf转word
	 * @param pdfFilePath
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static TransformFileSet pdf2word(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException {	
		// 移开鼠标避免挡事
		CommonUtils.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File pdfFile = new File(pdfFilePath);
		if(!pdfFile.exists() || !pdfFile.isFile() || !pdfFile.getCanonicalPath().endsWith(".pdf")) {
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
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 去掉输入法
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SPACE);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“另存为”
		robotMngr.pressKey(KeyEvent.VK_V);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“word”
		robotMngr.pressKey(KeyEvent.VK_W);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待出现文件名输入框
		AbbyyUtils.waitToInputfilename(robotMngr);
		// 将文件名的pdf替换为docx，保存到剪贴板，word文件就保存在原地
		File wordFile = new File(pdfFile.getCanonicalPath().replaceAll(".pdf$", ".docx"));
		result.setDstFile(wordFile);
		ClipboardUtils.setSysClipboardText(wordFile.getCanonicalPath());
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板的文件名复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 点击“保存按钮”
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确认覆盖已有文件
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_Y);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待准换开始直到完成
		AbbyyUtils.waitPdf2wordTransformingFinised(robotMngr);
		// 等待Word彻底打开
		MicrosoftWordUtils.waitToOpen(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭ABBYY
		AbbyyUtils.close();
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 关闭word
		MicrosoftWordUtils.close();
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		return result;
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException {
		pdf2word(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf");
	}
}
