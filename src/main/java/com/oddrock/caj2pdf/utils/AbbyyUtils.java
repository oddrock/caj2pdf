package com.oddrock.caj2pdf.utils;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.CmdExecutor;

public class AbbyyUtils {
	private static Logger logger = Logger.getLogger(AbbyyUtils.class);
	
	/**
	 * 判断ABBYY是否已打开，并位于首页
	 * @param robotMngr
	 * @return
	 * @throws IOException
	 */
	public static boolean isHomePage(RobotManager robotMngr) throws IOException{
		return CommonUtils.comparePic(robotMngr, "abbyy.mark.homepage");
	}
	
	/**
	 * 判断ABBYY是否已打开，并位于word转换任务页面
	 * @param robotMngr
	 * @return
	 * @throws IOException
	 */
	public static boolean isWordPage(RobotManager robotMngr) throws IOException{
		return CommonUtils.comparePic(robotMngr, "abbyy.mark.wordpage");
	}
	
	/**
	 * 判断ABBYY进程是否启动
	 * @return
	 * @throws IOException
	 */
	public static boolean isStarted() throws IOException {
		return CmdExecutor.getSingleInstance().isAppAlive(Prop.get("abbyy.appname"));
	}
	
	/**
	 * 打开并确保打开ABBYY
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void close() throws IOException, InterruptedException {
		if(isStarted()) {
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("abbyy.appname") + "\"");
		}
		while(isStarted()) {
			logger.warn("等待ABBYY关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已关闭");
	}
	
	/**
	 * 关闭并确保关闭ABBYY
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void open() throws IOException, InterruptedException {
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("abbyy.path"));
		while(!isStarted()) {
			logger.warn("等待ABBYY打开......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已打开");
	}
	
	/**
	 * 用ABBYY打开一个pdf文件
	 * @param pdfFilePath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void openPdf(String pdfFilePath)throws IOException, InterruptedException {
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("abbyy.path") + " \"" + pdfFilePath + "\"");
		while(!isStarted()) {
			logger.warn("等待ABBYY打开......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已打开");
	}
	
}
