package com.oddrock.caj2pdf.utils;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;
import com.oddrock.common.windows.CmdExecutor;

public class CalibreUtils{
	private static Logger logger = Logger.getLogger(CalibreUtils		.class);
	
	private static String softwareName = "calibre";
	
	// 该软件的进程是否已启动
	public static boolean isStart() throws IOException {
		for(String appname: Prop.get(softwareName+".appname").split(",")) {
			if(CmdExecutor.getSingleInstance().isAppAlive(appname)) {
				return true;
			}
		}
		return false;
	}
	
	// 关闭并确保该软件被关闭
	public static void close() throws IOException, InterruptedException {
		if(isStart()) {
			for(String appname: Prop.get(softwareName+".appname").split(",")) {
				CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + appname + "\"");
			}
		}
		while(isStart()) {
			logger.warn("等待"+softwareName+"关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已关闭");
	}
	
	// 通过图片比对，检查软件界面是否被打开
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.open");
	}
	
	// 用软件打开一个文件，并等待直到打开成功
	public static void open(RobotManager robotMngr,File file) throws IOException, InterruptedException {
		if(file!=null && file.exists()) {
			CmdExecutor.getSingleInstance().exeCmd(Prop.get(softwareName+".path") + " \"" + file.getCanonicalPath() + "\"");
		}else {
			CmdExecutor.getSingleInstance().exeCmd(Prop.get(softwareName+".path"));
		}
		while(!isStart()) {
			logger.warn("等待"+softwareName+"启动......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已打开");
	}
	
	// 检查是否有同名书籍
	public static boolean hasSameBook(RobotManager robotMngr) throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.hassamebook");
	}
	
	// 检查是否正在添加书籍
	public static boolean isAddingBook(RobotManager robotMngr) throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.addingbook");
	}
	
	// 等待添加书籍直到完成
	public static void waitAddingBookEnd(RobotManager robotMngr) throws IOException, InterruptedException {
		while(isAddingBook(robotMngr)) {
			logger.warn("等待"+softwareName+"书籍添加完成......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"书籍添加已完成");
	}
	
	// 确认calibre里没有书籍
	public static boolean isNoBook(RobotManager robotMngr)throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.nobook");
	}
	
	// 确认calibre是否打开了转换页面
	public static boolean isTransformPageOpen(RobotManager robotMngr)throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.transformpageopen");
	}
	
	// 等待直到calibre打开转换页面
	public static void waitTransformPageOpen(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isTransformPageOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"转换页面打开完成......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"转换页面打开已完成");
	}
	
	// 检查是否在转换
	public static boolean isTransforming(RobotManager robotMngr)throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.transforming");
	}
	
	// 等待转换完成
	public static void waitTransformingEnd(RobotManager robotMngr)throws IOException, InterruptedException {
		while(isTransforming(robotMngr)) {
			logger.warn("等待"+softwareName+"转换完成......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"转换已完成");
	}
	
	// 判断“选择目标目录”页面是否被打开
	public static boolean isSelectTargetDirOpen(RobotManager robotMngr)throws IOException {
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.selecttargetdiropen");
	}
	
	// 等待直到“选择目标目录”页面被打开
	public static void waitSelectTargetDirOpen(RobotManager robotMngr)throws IOException, InterruptedException {
		while(!isSelectTargetDirOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"“选择目标目录”页面被打开......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"“选择目标目录”页面被打开");
	}
	
	// 等待直到“选择目标目录”页面被关闭
	public static void waitSelectTargetDirClose(RobotManager robotMngr)throws IOException, InterruptedException {
		while(isSelectTargetDirOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"“选择目标目录”页面被关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"“选择目标目录”页面被关闭");
	}
	
	// 删除calibre里所有书籍，并且等待直到完成删除
	public static void delAllBooks(RobotManager robotMngr) throws InterruptedException, IOException {
		// 按下ctrl+A，选中所有书籍
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		robotMngr.pressKey(KeyEvent.VK_DELETE);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		while(!isNoBook(robotMngr)) {
			logger.warn("等待"+softwareName+"书籍删除完成......");
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		logger.warn("确认"+softwareName+"书籍删除已完成");
	}
	
	// 用calibre打开一本书，并且保证只打开这一本书(即calibre里只有这本书)，如果有别的书就删除
	public static void openSingleBook(RobotManager robotMngr, File file) throws IOException, InterruptedException {
		if(!file.exists()&&!file.isFile()) return;
		// 鼠标挪开，避免挡事
		CommonUtils.moveMouseAvoidHandicap(robotMngr);
		// 关闭calibre
		close();
		// 用calibre打开这本书
		open(robotMngr, file);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 如果calibre里已经有书
		if(!isNoBook(robotMngr)) {
			CommonUtils.wait(Prop.getInt("interval.waitlongmillis"));
			// 不管有没有，点击确定导入
			robotMngr.pressKey(KeyEvent.VK_ENTER);
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
			// 等待书导入完毕
			waitAddingBookEnd(robotMngr);
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
			// 然后删除calibre里所有书
			delAllBooks(robotMngr);
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
			// 然后重新添加书籍
			open(robotMngr, file);
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		// 等待书籍添加完毕
		waitAddingBookEnd(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
	}

	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		String filePath = "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf";
		File file = new File(filePath);
		RobotManager robotMngr = new RobotManager();
		openSingleBook(robotMngr, file);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 打开右键菜单
		robotMngr.clickMouseRight();
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 连按5次tab键，移动到“转换书籍”菜单
		for(int i=0; i<5; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		// 移动到逐个转换
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确认“逐个转换”
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到转换页面打开
		waitTransformPageOpen(robotMngr);
		// 鼠标移动到“输出格式”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.outformat.x"), 
				Prop.getInt("calibre.coordinate.outformat.y"));
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		robotMngr.clickMouseLeft();
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确定选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 开始转换
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 等待直到转换完毕
		waitTransformingEnd(robotMngr);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 打开右键菜单
		robotMngr.clickMouseRight();
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 连按3次tab键，移动到“保存到磁盘”菜单
		for(int i=0; i<3; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		// 移动打“保存到磁盘单个目录”
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确认
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到“选择目标目录”页面被打开
		waitSelectTargetDirOpen(robotMngr);
		File dstDir = CommonUtils.generateDstDir(new File(Prop.get("dstdirpath")), true);
		// 将生成的目标文件夹地址复制到剪贴板
		ClipboardUtils.setSysClipboardText(dstDir.getCanonicalPath());
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板里的文件路径复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 确认目录
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		// 确认保存
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		// 等待直到“选择目标目录”页面被关闭
		waitSelectTargetDirClose(robotMngr);
		// 遍历目标文件夹，删除pdf以外文件，并将pdf文件改名
		for(File f : dstDir.listFiles()) {
			if(!f.getCanonicalPath().toLowerCase().endsWith(".pdf")) {
				
			}
		}
		// 关闭calibre
		//close();
	}
}
