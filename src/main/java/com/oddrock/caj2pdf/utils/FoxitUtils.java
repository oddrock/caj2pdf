package com.oddrock.caj2pdf.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;

public class FoxitUtils {
	private static Logger logger = Logger.getLogger(FoxitUtils.class);
	
	// pdf是否打开
	public static boolean isPdfOpen(RobotManager robotMngr) throws IOException{
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
	
	/*
	 * 用foxit打开pdf
	 */
	public static CmdResult openPdf(String pdfFilePath) {
		return CmdExecutor.getSingleInstance().exeCmd(Prop.get("foxit.path") + " \"" + pdfFilePath + "\"");
	}
	
	/*
	 * 关闭福昕PDF阅读器
	 */
	public static CmdResult closeFoxit() {
		CmdResult result  = null;
		for(String appname : Prop.get("foxit.appname").split(",")) {
			result = CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + appname + "\"");
		}
		return result;
	}
	
	public static boolean isStart() throws IOException {
		for(String appname : Prop.get("foxit.appname").split(",")) {
			if(CmdExecutor.getSingleInstance().isAppAlive(appname)) {
				return true;
			}
		}
		return false;
	}
	
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
}
