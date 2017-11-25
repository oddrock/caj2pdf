package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.utils.CommonUtils;
import com.oddrock.caj2pdf.utils.FoxitUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class PdfUtils {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PdfUtils.class);
	
	// 提取PDF多少页，从第一页开始提取
	public static TransformFileSet extractPage(RobotManager robotMngr, String pdfFilePath, int tiquPageCount) throws IOException, InterruptedException {
		// 移开鼠标避免挡事
		CommonUtils.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File pdfFile = new File(pdfFilePath);
		if(!pdfFile.exists() || !pdfFile.isFile() || !pdfFile.getCanonicalPath().endsWith(".pdf")) {
			return result;
		}
		result.setSrcFile(pdfFile);
		// 关闭foxit
		FoxitUtils.close();
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 打开PDF文件
		FoxitUtils.openPdf(robotMngr, pdfFilePath);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 打开页面管理菜单
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_O);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 打开提取页面菜单
		robotMngr.pressKey(KeyEvent.VK_E);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打开提取页面时的导出页面
		FoxitUtils.waitExportPageWhenExracting(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 两次tab移动到输入数字文本框
		robotMngr.pressKey(KeyEvent.VK_TAB);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		robotMngr.pressKey(KeyEvent.VK_TAB);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 全选输入数字文本框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		// 将数字写入粘贴板
		ClipboardUtils.setSysClipboardText(String.valueOf(tiquPageCount));
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 将数字复制到输入数字文本框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 选中导出页面另存为其他文档
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确认按钮
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_K);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打开提取页面时的输入文件名页面
		FoxitUtils.waitInputfilenameOpenWhenExracting(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 在源文件名前加上“提取页面 ”字样，其他原封不动，成为新的提取后的文件名
		File dstFile = new File(pdfFile.getParentFile(), "提取页面 "+pdfFile.getName());
		result.setDstFile(dstFile);
		// 将提取后文件名复制到剪贴板
		ClipboardUtils.setSysClipboardText(dstFile.getCanonicalPath());
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板内容复制到文件名输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确定按钮
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确认覆盖（如果要覆盖的话）
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_Y);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到关闭提取页面时的输入文件名页面
		FoxitUtils.waitInputfilenameCloseWhenExracting(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitlongmillis"));
		FoxitUtils.close();
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		return result;
				 
	}
	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		extractPage(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf", 10);
	}
}
