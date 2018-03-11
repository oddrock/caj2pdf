package com.oddrock.caj2pdf.utils;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
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
		return Common.comparePic(robotMngr, "abbyy.mark.open");
	}
	
	// 判断ABBYY是否处在需要输入文件名
	public static boolean isInputfilename(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, "abbyy.mark.inputfilename");
	}
	
	// 是否正在从pdf转为word
	public static boolean isPdf2wordTransforming(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, "abbyy.mark.pdf2wordtransforming");
	}
	
	// 等待pdf2word完成转换
	public static void waitPdf2wordTransformingFinised(RobotManager robotMngr) throws IOException, InterruptedException {
		// 等待开始转换
		while(!isPdf2wordTransforming(robotMngr)) {
			logger.warn("等待pdf2word开始转换......");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		// 等待完成转换
		while(isPdf2wordTransforming(robotMngr)) {
			logger.warn("pdf2word开始转换中......");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("已完成pdf2word转换");
	}
	
	// 是否pdf转epub
	public static boolean isPdf2epubTransforming(RobotManager robotMngr) throws IOException {
		return Common.comparePic(robotMngr, "abbyy.mark.pdf2epubtransforming");
	}
	
	// 等待pdf2epub完成转换
	public static void waitPdf2epubTransformingFinised(RobotManager robotMngr) throws IOException, InterruptedException {
		// 等待开始转换
		while(!isPdf2epubTransforming(robotMngr)) {
			logger.warn("等待pdf2epub开始转换......");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		// 等待完成转换
		while(isPdf2epubTransforming(robotMngr)) {
			logger.warn("pdf2epub开始转换中......");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("已完成pdf2epub转换");
	}
	
	// 阻塞等待出现文件名输入款
	public static void waitToInputfilename(RobotManager robotMngr) throws IOException, InterruptedException {
		while(!isInputfilename(robotMngr)) {
			logger.warn("等待出现文件名输入框......");
			Common.wait(Prop.getInt("interval.waitminmillis"));
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
	 * @throws TransformWaitTimeoutException 
	 */
	public static void close() throws IOException, InterruptedException, TransformWaitTimeoutException {
		Timer timer = new Timer().start();
		if(isStart()) {
			CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("abbyy.appname") + "\"");
		}
		while(isStart()) {
			logger.warn("等待ABBYY关闭......");
			Common.wait(Prop.getInt("interval.waitmillis"));
			if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.long")) {
				logger.warn("等待ABBYY关闭时间过长，已达到："+timer.getSpentTimeMillis()/1000L+"秒");
				throw new TransformWaitTimeoutException("ABBYY无法关闭");
			}else if(timer.getSpentTimeMillis()>TimeoutUtils.getTimeout("timeout.short")) {
				CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + Prop.get("abbyy.appname") + "\"");
				Common.wait(Prop.getInt("interval.waitlongmillis"));
			}
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
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待ABBYY打开");
			Common.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认ABBYY已打开");
	}
	
}
