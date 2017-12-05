package com.oddrock.caj2pdf.utils;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.CmdExecutor;

public class CalibreUtils{
	private static Logger logger = Logger.getLogger(CalibreUtils.class);
	
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
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"已关闭");
	}
	
	// 通过图片比对，检查软件界面是否被打开
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		return Common.comparePic(robotMngr, softwareName+".mark.open");
	}
	
	public static void waitToOpen(RobotManager robotMngr) throws IOException, InterruptedException{
		while(!isOpen(robotMngr)) {
			logger.warn("等待calibre打开");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认calibre已打开");
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
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"打开");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"已打开");
	}
	
	// 检查是否有同名书籍
	public static boolean hasSameBook(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.hassamebook");
	}
	
	// 检查是否正在添加书籍
	public static boolean isAddingBook(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.addingbook");
	}
	
	// 等待添加书籍直到完成
	public static void waitAddingBookEnd(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(isAddingBook(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waitaddingbookend")) {
				logger.warn("等待书籍添加时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"书籍添加完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"书籍添加已完成");
	}
	
	// 确认calibre里没有书籍
	public static boolean isNoBook(RobotManager robotMngr)throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.nobook");
	}
	
	// 确认calibre是否打开了转换页面
	public static boolean isTransformPageOpen(RobotManager robotMngr)throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.transformpageopen");
	}
	
	// 等待直到calibre打开转换页面
	public static void waitTransformPageOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isTransformPageOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waittransformpageopen")) {
				logger.warn("等待转换页面打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"转换页面打开完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"转换页面打开已完成");
	}
	
	public static boolean isTransformPageTxtinputOpen(RobotManager robotMngr)throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.transformpage.txtinput");
	}
	
	public static void waitTransformPageTxtinputOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isTransformPageTxtinputOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waittransformpagetxtinput")) {
				logger.warn("等待转换txtinput页面打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"转换txtinput页面打开完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"转换txtinput页面打开已完成");
	}
	
	// 检查转换任务页面是否被打开
	public static boolean isTransformTaskPageOpen(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.transformtaskpageopen");
	}
	
	// 打开或关闭转换任务界面
	public static void toggleTransformTaskPage(RobotManager robotMngr) throws InterruptedException {
		// 鼠标移动到右下角“任务图标”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.opentransformpage.x"), 
				Prop.getInt("calibre.coordinate.opentransformpage.y"));
		Common.waitM();
		// 鼠标点击打开转换任务界面
		robotMngr.clickMouseLeft();
		Common.waitM();
	}
	
	// 打开并等待转换任务界面
	public static void openAndWaitTransformTaskPage(RobotManager robotMngr) throws InterruptedException, IOException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		Common.waitM();
		if(!isTransformTaskPageOpen(robotMngr)) {
			toggleTransformTaskPage(robotMngr);
		}
		while(!isTransformTaskPageOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waittransformtask")) {
				logger.warn("等待转换任务页面打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"转换任务页面打开完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"转换任务页面打开已完成");
	}
	
	// 检查转换任务是否已完成
	public static boolean isTransformTaskEnd(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.transformtaskend");
	}
	
	// 等待直到转换任务完成
	public  static void waitTransformTaskEnd(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isTransformTaskEnd(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waittransformtaskend")) {
				logger.warn("等待转换任务时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"转换任务完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"转换任务已完成");
	}
	
	// 关闭并等待转换任务界面
	public static void closeAndWaitTransformTaskPage(RobotManager robotMngr) throws InterruptedException, IOException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		Common.waitM();
		if(isTransformTaskPageOpen(robotMngr)) {
			toggleTransformTaskPage(robotMngr);
		}
		while(isTransformTaskPageOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.calibre.waittransformtaskpageclose")) {
				logger.warn("等待转换任务页面打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待"+softwareName+"转换任务页面关闭完成......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"转换任务页面关闭已完成");
	}
	
	// 判断“选择目标目录”页面是否被打开
	public static boolean isSelectTargetDirOpen(RobotManager robotMngr)throws IOException {
		return Common.comparePic(robotMngr, softwareName+".mark.selecttargetdiropen");
	}
	
	// 等待直到“选择目标目录”页面被打开
	public static void waitSelectTargetDirOpen(RobotManager robotMngr)throws IOException, InterruptedException {
		while(!isSelectTargetDirOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"“选择目标目录”页面被打开......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"“选择目标目录”页面被打开");
	}
	
	// 等待直到“选择目标目录”页面被关闭
	public static void waitSelectTargetDirClose(RobotManager robotMngr)throws IOException, InterruptedException {
		while(isSelectTargetDirOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"“选择目标目录”页面被关闭......");
			Common.waitM();
		}
		logger.warn("确认"+softwareName+"“选择目标目录”页面被关闭");
	}
	
	// 删除calibre里所有书籍，并且等待直到完成删除
	public static void delAllBooks(RobotManager robotMngr) throws InterruptedException, IOException {
		// 按下ctrl+A，选中所有书籍
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		Common.waitShort();
		robotMngr.pressKey(KeyEvent.VK_DELETE);
		Common.waitShort();
		while(!isNoBook(robotMngr)) {
			logger.warn("等待"+softwareName+"书籍删除完成......");
			Common.waitShort();
		}
		logger.warn("确认"+softwareName+"书籍删除已完成");
	}
	
	public static void openSingleBook_step1_1(RobotManager robotMngr, File file) throws IOException, InterruptedException {
		// 鼠标挪开，避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		// 关闭calibre
		close();
		// 用calibre打开这本书
		open(robotMngr, file);
		Common.waitM();
		// 如果calibre里已经有书
		if(!isNoBook(robotMngr)) {
			Common.waitLong();
			// 不管有没有，点击确定导入
			robotMngr.pressKey(KeyEvent.VK_ENTER);
			Common.waitM();
		}			
	}
	
	public static void openSingleBook_step1(RobotManager robotMngr, File file) throws IOException, InterruptedException, TransformWaitTimeoutException {
		openSingleBook_step1_1(robotMngr, file);
		if(!isNoBook(robotMngr)) {
			// 等待书导入完毕
			try {
				waitAddingBookEnd(robotMngr);
			} catch (TransformWaitTimeoutException e) {
				e.printStackTrace();
				openSingleBook_step1_1(robotMngr, file);
				waitAddingBookEnd(robotMngr);
			}
			Common.waitM();
			// 然后删除calibre里所有书
			delAllBooks(robotMngr);
			Common.waitM();
			// 然后重新添加书籍
			open(robotMngr, file);
			Common.waitM();
		}
	}
	
	// 用calibre打开一本书，并且保证只打开这一本书(即calibre里只有这本书)，如果有别的书就删除
	public static void openSingleBook(RobotManager robotMngr, File file) throws IOException, InterruptedException, TransformWaitTimeoutException {
		if(!file.exists()&&!file.isFile()) return;
		openSingleBook_step1(robotMngr, file);
		// 等待书籍添加完毕
		try {
			waitAddingBookEnd(robotMngr);
		} catch (TransformWaitTimeoutException e) {
			e.printStackTrace();
			openSingleBook_step1(robotMngr, file);
			waitAddingBookEnd(robotMngr);
		}
		Common.waitLong();
	}
}
