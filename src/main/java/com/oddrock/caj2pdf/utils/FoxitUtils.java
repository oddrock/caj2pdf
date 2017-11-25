package com.oddrock.caj2pdf.utils;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;

public class FoxitUtils {
	private static Logger logger = Logger.getLogger(FoxitUtils.class);
	
	// pdf是否打开
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.pdfopen.x")
				,Prop.getInt("cajviewer.mark.pdfopen.y")
				,Prop.getInt("cajviewer.mark.pdfopen.width")
				,Prop.getInt("cajviewer.mark.pdfopen.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.pdfopen.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 是否处在提取页面的导出页面状态下
	public static boolean isExportPageOpenAtExtractPage(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.extactpage.exportpage.x")
				,Prop.getInt("cajviewer.mark.extactpage.exportpage.y")
				,Prop.getInt("cajviewer.mark.extactpage.exportpage.width")
				,Prop.getInt("cajviewer.mark.extactpage.exportpage.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.extactpage.exportpage.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 是否处在提取页面的输入文件名状态下
	public static boolean isInputfilenameAtExtractPage(RobotManager robotMngr) throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.extactpage.inputfilename.x")
				,Prop.getInt("cajviewer.mark.extactpage.inputfilename.y")
				,Prop.getInt("cajviewer.mark.extactpage.inputfilename.width")
				,Prop.getInt("cajviewer.mark.extactpage.inputfilename.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.extactpage.inputfilename.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 用foxit打开pdf
	@Deprecated
	public static CmdResult openPdfOld(String pdfFilePath) {
		return CmdExecutor.getSingleInstance().exeCmd(Prop.get("foxit.path") + " \"" + pdfFilePath + "\"");
	}
	
	// 关闭福昕PDF阅读器
	@Deprecated
	public static CmdResult closeFoxit() {
		CmdResult result  = null;
		for(String appname : Prop.get("foxit.appname").split(",")) {
			result = CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + appname + "\"");
		}
		return result;
	}
	
	// foxit进程是否启动
	public static boolean isStart() throws IOException {
		for(String appname : Prop.get("foxit.appname").split(",")) {
			if(CmdExecutor.getSingleInstance().isAppAlive(appname)) {
				return true;
			}
		}
		return false;
	}
	
	// 关闭并等待完成关闭
	public static void close() throws IOException, InterruptedException {
		if(isStart()) {
			for(String appname : Prop.get("foxit.appname").split(",")) {
				CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + appname + "\"");
			}
		}
		while(isStart()) {
			logger.warn("等待foxit关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认foxit已关闭");
	}
	
	// 用foxit打开一个pdf并等待打开完成
	public static void openPdf(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException {
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("foxit.path") + " \"" + pdfFilePath + "\"");
		while(!isStart()) {
			logger.warn("等待foxit启动......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认foxit已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待foxit打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认foxit已打开");
	}
	
	// 等待直到打开提取页面时的导出页面
	public static void waitExportPageWhenExracting(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isExportPageOpenAtExtractPage(robotMngr)) {
			logger.warn("等待打开提取页面时的导出页面");
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		logger.warn("已完成打开提取页面时的导出页面");
	}
	
	// 等待直到打开提取页面时的输入文件名页面
	public static void waitInputfilenameOpenWhenExracting(RobotManager robotMngr) throws IOException, InterruptedException{
		while(!isInputfilenameAtExtractPage(robotMngr)) {
			logger.warn("等待打开提取页面时的输入文件名页面");
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		logger.warn("已完成打开提取页面时的输入文件名页面");
	}
	
	// 等待直到关闭提取页面时的输入文件名页面
	public static void waitInputfilenameCloseWhenExracting(RobotManager robotMngr) throws IOException, InterruptedException{
		while(isInputfilenameAtExtractPage(robotMngr)) {
			logger.warn("等待关闭提取页面时的输入文件名页面");
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		logger.warn("已完成关闭提取页面时的输入文件名页面");
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		openPdf(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf");
	}
}
