package com.oddrock.caj2pdf.utils;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.CmdExecutor;

public class MicrosoftWordUtils {
	private static Logger logger = Logger.getLogger(MicrosoftWordUtils.class);
			
	public static boolean isStart() throws IOException {
		return CmdExecutor.getSingleInstance().isAppAlive(Prop.get("microsoftword.appname"));
	}
	
	public static boolean isOpen(RobotManager robotMngr) throws IOException {
		return CommonUtils.comparePic(robotMngr, "microsoftword.mark.open");
	}
	
	public static void waitToOpen(RobotManager robotMngr) throws IOException, InterruptedException{
		while(!isOpen(robotMngr)) {
			logger.warn("等待Word打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认Word已打开");
	}
	
	public static void close() throws IOException, InterruptedException {
		if(isStart()) {
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("microsoftword.appname") + "\"");
		}
		while(isStart()) {
			logger.warn("等待MicrosoftWord关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认MicrosoftWord已关闭");
	}
}
