package com.oddrock.caj2pdf.utils;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;

public class Browser360Utils {
	private static Logger logger = Logger.getLogger(Browser360Utils.class);
	
	public static boolean isHuifu(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.huifu.x")
				,Prop.getInt("browser360.mark.huifu.y")
				,Prop.getInt("browser360.mark.huifu.width")
				,Prop.getInt("browser360.mark.huifu.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.huifu.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	public static void closeOpenClearAllDownloadClose(RobotManager robotMngr) throws IOException, TransformWaitTimeoutException, InterruptedException{
		Browser360Utils.close();
		Browser360Utils.open(robotMngr);
		Browser360Utils.clearAllDownload(robotMngr);
		Browser360Utils.close();
	}
	
	// 清空已下载
	public static void clearAllDownload(RobotManager robotMngr) throws IOException, TransformWaitTimeoutException, InterruptedException{
		if(isOpen(robotMngr)){
			/*robotMngr.moveMouseTo(Prop.getInt("browser360.coordinate.menu.x"), Prop.getInt("browser360.coordinate.menu.y"));
			Common.waitM();
			robotMngr.clickMouseLeft();
			Common.waitM();
			waitMenuOpen(robotMngr);*/
			Common.waitLong();
			// 打开“下载页面”
			robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_J);
			waitXiazaiPageOpen(robotMngr);
			// 鼠标移动到“清空已下载”这个button
			robotMngr.moveMouseTo(Prop.getInt("browser360.coordinate.qingkongyixiazaibutton.x"), Prop.getInt("browser360.coordinate.qingkongyixiazaibutton.y"));
			Common.waitShort();
			robotMngr.clickMouseLeft();
			Common.waitLong();
			// 如果“删除下载任务”窗口被打开
			if(Browser360Utils.isShanchuxiazairenwuPageOpen(robotMngr)){
				// 取消掉“同时删除文件”这个选项
				Browser360Utils.offTongshishanchuwenjianCheckbox(robotMngr);
				Common.waitShort();
				// 确认删除
				robotMngr.pressEnter();
				Common.waitShort();
			}
			Common.waitShort();
		}
	}
	
	// 测试“菜单”是否被打开
	public static boolean isMenuOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.menuopen.x")
				,Prop.getInt("browser360.mark.menuopen.y")
				,Prop.getInt("browser360.mark.menuopen.width")
				,Prop.getInt("browser360.mark.menuopen.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.menuopen.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 等待直到菜单打开
	public static void waitMenuOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isMenuOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.browser360.waitmenuopen")) {
				logger.warn("等待菜单打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待菜单打开");
			Common.waitShort();
		}
		logger.warn("确认菜单已打开");
	}
	
	// 测试“同时删除文件”这个checkbox是否被选中了
	public static boolean isTongshishanchuwenjianCheckboxOn(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.tongshishanchuwenjiancheckboxon.x")
				,Prop.getInt("browser360.mark.tongshishanchuwenjiancheckboxon.y")
				,Prop.getInt("browser360.mark.tongshishanchuwenjiancheckboxon.width")
				,Prop.getInt("browser360.mark.tongshishanchuwenjiancheckboxon.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.tongshishanchuwenjiancheckboxon.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 取消掉“同时删除文件”这个选项
	public static void offTongshishanchuwenjianCheckbox(RobotManager robotMngr) throws IOException, InterruptedException{
		if(Browser360Utils.isTongshishanchuwenjianCheckboxOn(robotMngr)){
			// 鼠标移动到“同时删除文件”这个checkbox
			robotMngr.moveMouseTo(Prop.getInt("browser360.coordinate.qingkongyixiazaibutton.x"), Prop.getInt("browser360.coordinate.qingkongyixiazaibutton.y"));
			Common.waitM();
			// 将这个选项勾掉
			robotMngr.clickMouseLeft();
			Common.waitM();
		}
	}
	
	// 测试删除下载任务窗口是否打开
	public static boolean isShanchuxiazairenwuPageOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.shanchuxiazairenwupageopen.x")
				,Prop.getInt("browser360.mark.shanchuxiazairenwupageopen.y")
				,Prop.getInt("browser360.mark.shanchuxiazairenwupageopen.width")
				,Prop.getInt("browser360.mark.shanchuxiazairenwupageopen.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.shanchuxiazairenwupageopen.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 等待直到下载任务完成
	public static void waitShanchuxiazairenwuPageOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isShanchuxiazairenwuPageOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.browser360.waitxiaizaiend")) {
				logger.warn("等待删除下载任务窗口过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待删除下载任务窗口打开");
			Common.waitShort();
		}
		logger.warn("确认删除下载任务窗口打开");
	}
	
	// 测试下载任务是否完成
	public static boolean isXiazaiEnd(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.xiazaiend.x")
				,Prop.getInt("browser360.mark.xiazaiend.y")
				,Prop.getInt("browser360.mark.xiazaiend.width")
				,Prop.getInt("browser360.mark.xiazaiend.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.xiazaiend.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 等待直到下载任务完成
	public static void waitXiazaiEnd(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isXiazaiEnd(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.browser360.waitxiaizaiend")) {
				logger.warn("等待下载任务完成过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待下载任务完成");
			Common.waitShort();
		}
		logger.warn("确认下载任务已完成");
	}
	
	// 测试新建下载页面是否打开
	public static boolean isXiazaiPageOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.xiazaipageopen.x")
				,Prop.getInt("browser360.mark.xiazaipageopen.y")
				,Prop.getInt("browser360.mark.xiazaipageopen.width")
				,Prop.getInt("browser360.mark.xiazaipageopen.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.xiazaipageopen.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 等待直到下载页面打开
	public static void waitXiazaiPageOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isXiazaiPageOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.browser360.waitxiaizaipageopen")) {
				logger.warn("等待下载页面打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待下载页面打开");
			Common.waitShort();
		}
		logger.warn("确认下载页面已打开");
	}
	
	// 测试新建下载任务框是否打开
	public static boolean isXinjianxiazairenwuOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.xinjianxiazairenwuopen.x")
				,Prop.getInt("browser360.mark.xinjianxiazairenwuopen.y")
				,Prop.getInt("browser360.mark.xinjianxiazairenwuopen.width")
				,Prop.getInt("browser360.mark.xinjianxiazairenwuopen.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.xinjianxiazairenwuopen.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 等待直到新建下载任务框打开
	public static void waitXinjianxiazairenwuOpen(RobotManager robotMngr) throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		while(!isXinjianxiazairenwuOpen(robotMngr)) {
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.browser360.waitxinjianxiazairenwuopen")) {
				logger.warn("等待新建下载任务框打开时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException();
			}
			logger.warn("等待新建下载任务框打开");
			Common.waitShort();
		}
		logger.warn("确认新建下载任务框已打开");
	}
	
	// 测试360浏览器是否打开
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("browser360.mark.open.x")
				,Prop.getInt("browser360.mark.open.y")
				,Prop.getInt("browser360.mark.open.width")
				,Prop.getInt("browser360.mark.open.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("browser360.mark.open.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 检测360浏览器是否启动
	public static boolean isStart() throws IOException {
		return CmdExecutor.getSingleInstance().isAppAlive(Prop.get("browser360.appname"));
	}
	
	// 关闭360浏览器
	public static void close() throws IOException, InterruptedException{
		if(isStart()) {
			logger.warn("开始关闭360浏览器...");
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("browser360.appname") + "\"");
		}
		while(isStart()) {
			logger.warn("等待360浏览器关闭...");
			Common.waitShort();
		}
		logger.warn("确认360浏览器已关闭");
	}
	
	// 打开360浏览器
	public static void open(RobotManager robotMngr) throws IOException, InterruptedException {
		logger.warn("启动360浏览器...");
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("browser360.path"));
		while(!isStart()) {
			logger.warn("等待360浏览器启动......");
			Common.waitShort();
		}
		logger.warn("确认360浏览器已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待360浏览器打开");
			Common.waitShort();
		}
		logger.warn("确认360浏览器已打开");
	}
	
	// 用360浏览器打开一个网址
	public static void open(RobotManager robotMngr, String url) throws IOException, InterruptedException {
		logger.warn("启动360浏览器...");
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("browser360.path")+ " \"" + url + "\"");
		while(!isStart()) {
			logger.warn("等待360浏览器启动......");
			Common.waitShort();
		}
		logger.warn("确认360浏览器已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待360浏览器打开");
			Common.waitShort();
		}
		logger.warn("确认360浏览器已打开");
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, AWTException, TransformWaitTimeoutException {
		close();
		RobotManager r = new RobotManager();
		//String url = "https://mail.qq.com/cgi-bin/ftnExs_download?k=2c626134314929cb71b9572c1166531855565951555758544f5202560f4b550e56074c560e52591a0153075551560503005a5456377f6107534fdef283954c8391a89c8aeadd9883c34f501a4716154f6214&t=exs_ftn_download&code=bba47fa7";
		open(r);
		clearAllDownload(r);
	}

}
