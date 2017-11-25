package com.oddrock.caj2pdf.utils;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.CmdExecutor;

public class AbbyyUtils {
	private static Logger logger = Logger.getLogger(AbbyyUtils.class);
	
	/**
	 * 判断ABBYY是否已打开
	 * @param robotMngr
	 * @return
	 * @throws IOException
	 */
	public static boolean isOpen(RobotManager robotMngr) throws IOException{
		return CommonUtils.comparePic(robotMngr, "abbyy.mark.open");
	}
	
	// 判断ABBYY是否处在需要输入文件名
	public static boolean isInputfilename(RobotManager robotMngr) throws IOException {
		return CommonUtils.comparePic(robotMngr, "abbyy.mark.inputfilename");
	}
	
	// 是否正在从pdf转为word
	public static boolean isPdf2wordTransforming(RobotManager robotMngr) throws IOException {
		return CommonUtils.comparePic(robotMngr, "abbyy.mark.pdf2wordtransforming");
	}
	
	// 等待pdf2word完成转换
	public static void waitPdf2wordTransformingFinised(RobotManager robotMngr) throws IOException, InterruptedException {
		// 等待开始转换
		while(!isPdf2wordTransforming(robotMngr)) {
			logger.warn("等待pdf2word开始转换......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		// 等待完成转换
		while(isPdf2wordTransforming(robotMngr)) {
			logger.warn("pdf2word开始转换中......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("已完成pdf2word转换");
	}
	
	// 阻塞等待出现文件名输入款
	public static void waitToInputfilename(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isInputfilename(robotMngr)) {
			logger.warn("等待出现文件名输入框......");
			CommonUtils.wait(Prop.getInt("interval.waitminmillis"));
		}
		logger.warn("已出现文件名输入框......");
	}
	
	/**
	 * 判断ABBYY进程是否启动
	 * @return
	 * @throws IOException
	 */
	public static boolean isStart() throws IOException {
		return CmdExecutor.getSingleInstance().isAppAlive(Prop.get("abbyy.appname"));
	}
	

	/**
	 * 关闭并确保ABBYY已关闭
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void close() throws IOException, InterruptedException {
		if(isStart()) {
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("abbyy.appname") + "\"");
		}
		while(isStart()) {
			logger.warn("等待ABBYY关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已关闭");
	}
	
	/**
	 * 用ABBYY打开一个pdf文件，并确保已打开
	 * @param pdfFilePath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void openPdf(RobotManager robotMngr, String pdfFilePath)throws IOException, InterruptedException {
		CmdExecutor.getSingleInstance().exeCmd(Prop.get("abbyy.path") + " \"" + pdfFilePath + "\"");
		while(!isStart()) {
			logger.warn("等待ABBYY启动......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待ABBYY打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已打开");
	}
	
}
