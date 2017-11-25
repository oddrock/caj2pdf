package com.oddrock.caj2pdf.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;

public class CajViewerUtils {
	private static Logger logger = Logger.getLogger(CajViewerUtils.class);
	
	// 测试Caj是否打开
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.leftupcorner.x")
				,Prop.getInt("cajviewer.mark.leftupcorner.y")
				,Prop.getInt("cajviewer.mark.leftupcorner.width")
				,Prop.getInt("cajviewer.mark.leftupcorner.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.leftupcorner.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 测试打印机是否打开
	public static boolean isPrintReady(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.printready.x")
				,Prop.getInt("cajviewer.mark.printready.y")
				,Prop.getInt("cajviewer.mark.printready.width")
				,Prop.getInt("cajviewer.mark.printready.height"));
		if(PictureComparator.compare(image, 
				BufferedImageUtils.read(Prop.get("cajviewer.mark.printready.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 检查是否到输入文件名的地方了
	public static boolean isInputfilename(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.inputfilename.x")
				,Prop.getInt("cajviewer.mark.inputfilename.y")
				,Prop.getInt("cajviewer.mark.inputfilename.width")
				,Prop.getInt("cajviewer.mark.inputfilename.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.inputfilename.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 是否正在打印
	public static boolean isPrintnow(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.printnow.x")
				,Prop.getInt("cajviewer.mark.printnow.y")
				,Prop.getInt("cajviewer.mark.printnow.width")
				,Prop.getInt("cajviewer.mark.printnow.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.printnow.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	/*
	 * 打开空的caj
	 */
	@Deprecated
	public static CmdResult openCajFile(){
		return CmdExecutor.getSingleInstance().exeCmd(Prop.get("cajviewer.path"));
	}
	

	
	/*
	 * 关闭caj阅读器
	 */
	public static CmdResult closeCaj(){
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("cajviewer.appname") + "\"");
	}

	/*
	 * 打开caj文件
	 */
	@Deprecated
	public static CmdResult openCajOld(String cajFilePath){
		return CmdExecutor.getSingleInstance().exeCmd(
				Prop.get("cajviewer.path") + " \"" + cajFilePath + "\"");
	}
	
	public static boolean isStart() throws IOException {
		return CmdExecutor.getSingleInstance().isAppAlive(Prop.get("cajviewer.appname"));
	}
	
	public static void close() throws IOException, InterruptedException {
		if(isStart()) {
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("cajviewer.appname") + "\"");
		}
		while(isStart()) {
			logger.warn("等待cajviewer关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认cajviewer已关闭");
	}
	
	public static void openCaj(RobotManager robotMngr, String cajFilePath) throws IOException, InterruptedException{
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("cajviewer.path") + " \"" + cajFilePath + "\"");
		while(!isStart()) {
			logger.warn("等待cajviewer启动......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认cajviewer已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待cajviewer打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认cajviewer已打开");
	}
	
	// 等待直到打印机打开
	public static void waitPrinterOpen(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isPrintReady(robotMngr)) {
			logger.warn("等待打印机打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认打印机已打开");
	}
	
	// 等待打印完毕
	public static void waitPrintFinish(RobotManager robotMngr) throws IOException, InterruptedException {
		boolean hasprinted = false;
		while(true){
			if(isPrintnow(robotMngr)){
				hasprinted = true;
			}else if(hasprinted){
				break;
			}
			if(hasprinted){
				logger.warn("等待打印完毕");
			}else{
				logger.warn("等待开始打印");
			}
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
	}
	// 检查是否要输入文件名了，没有就等待
	public static void waitInputfilename(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isInputfilename(robotMngr)){
			logger.warn("等待输入文件名");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
	}
}
