package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.utils.CalibreUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Pdf2MobiUtils {
	private static Logger logger = Logger.getLogger(Pdf2MobiUtils.class);
	
	// 用calibre实现pdf转mobi
	public static TransformFileSet pdf2mobiByCalibre(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException {
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File srcFile = new File(pdfFilePath);
		if(Common.isFileExists(srcFile, "\\.pdf")) {
			logger.warn("文件不存在："+pdfFilePath);
			return result;
		}
		result.setSrcFile(srcFile);
		// 用calibre打开一本书，并且保证只打开这一本书(即calibre里只有这本书)，如果有别的书就删除
		CalibreUtils.openSingleBook(robotMngr, srcFile);	
		Common.waitShort();
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		Common.waitM();
		// 打开右键菜单
		robotMngr.clickMouseRight();
		Common.waitShort();
		// 连按5次tab键，移动到“转换书籍”菜单
		for(int i=0; i<5; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			Common.waitShort();
		}
		// 移动到逐个转换
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		Common.waitShort();
		// 确认“逐个转换”
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 等待直到转换页面打开
		CalibreUtils.waitTransformPageOpen(robotMngr);
		// 鼠标移动到“输出格式”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.outformat.x"), 
				Prop.getInt("calibre.coordinate.outformat.y"));
		Common.waitM();
		robotMngr.clickMouseLeft();
		Common.waitShort();
		// 选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		Common.waitShort();
		// 确定选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 开始转换
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitM();
		// 等待直到打开转换任务页面
		CalibreUtils.openAndWaitTransformTaskPage(robotMngr);
		Common.waitM();
		// 等待直到转换任务完成
		CalibreUtils.waitTransformTaskEnd(robotMngr);
		Common.waitM();
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		Common.waitM();
		// 打开右键菜单
		robotMngr.clickMouseRight();
		Common.waitShort();
		// 连按3次tab键，移动到“保存到磁盘”菜单
		for(int i=0; i<3; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			Common.waitShort();
		}
		// 移动打“保存到磁盘单个目录”
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		Common.waitShort();
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		Common.waitShort();
		// 确认
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 等待直到“选择目标目录”页面被打开
		CalibreUtils.waitSelectTargetDirOpen(robotMngr);
		File dstDir = Common.generateDstDir(new File(Prop.get("dstdirpath")), true);
		// 将生成的目标文件夹地址复制到剪贴板
		ClipboardUtils.setSysClipboardText(dstDir.getCanonicalPath());
		Common.waitShort();
		// 将剪贴板里的文件路径复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		Common.waitM();
		// 确认目录
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 确认保存
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitM();
		// 等待直到“选择目标目录”页面被关闭
		CalibreUtils.waitSelectTargetDirClose(robotMngr);
		File dstFile = null;
		// 遍历目标文件夹，删除mobi以外文件，并将mobi文件改名(因为默认的是拼音名字)
		for(File file : dstDir.listFiles()) {
			if(!file.getCanonicalPath().toLowerCase().endsWith(".mobi")) {
				file.delete();
			}else {
				dstFile = new File(file.getParentFile().getCanonicalPath() + 
						srcFile.getCanonicalPath().replaceAll(".pdf$", "")+".mobi");
				file.renameTo(dstFile);
			}
			
		}
		result.setDstFile(dstFile);
		// 关闭calibre
		//close();
		return result;
	}

	public static void main(String[] args) throws AWTException, IOException, InterruptedException {
		String filePath = "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf";
		RobotManager robotMngr = new RobotManager();
		pdf2mobiByCalibre(robotMngr, filePath);
	}

}
