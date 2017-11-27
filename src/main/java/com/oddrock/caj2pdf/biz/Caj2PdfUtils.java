package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.utils.CajViewerUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.FoxitUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Caj2PdfUtils {
	public static TransformFileSet caj2pdf(RobotManager robotMngr, String cajFilePath) throws IOException, InterruptedException{
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File cajFile = new File(cajFilePath);
		if(!cajFile.exists() || !cajFile.isFile() || !cajFile.getCanonicalPath().endsWith(".caj")) {
			return result;
		}
		result.setSrcFile(cajFile);
		// 关闭cajviewer
		CajViewerUtils.close();
		// 关闭foxit
		FoxitUtils.close();
		// 用cajviewer打开指定caj文件
		CajViewerUtils.openCaj(robotMngr, cajFilePath);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 打开“打印机”菜单
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_P);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打印机打开
		CajViewerUtils.waitPrinterOpen(robotMngr);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击“确定”开始打印
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_O);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打印完毕
		CajViewerUtils.waitPrintFinish(robotMngr);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到输入文件名
		CajViewerUtils.waitInputfilename(robotMngr);
		// pdf文件生成在原地，只修改后缀
		File pdfFile = new File(cajFile.getParent(), cajFile.getName().replaceAll(".caj$", ".pdf"));
		result.setDstFile(pdfFile);
		// 将生成的pdf文件名复制到文本框
		ClipboardUtils.setSysClipboardText(pdfFile.getCanonicalPath());
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 全选输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板里的文件路径复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确定按钮
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确定覆盖按钮
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭cajviewer
		CajViewerUtils.close();
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 关闭foxit
		FoxitUtils.close();
		return result;
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException {
		caj2pdf(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.caj");
	}
}
