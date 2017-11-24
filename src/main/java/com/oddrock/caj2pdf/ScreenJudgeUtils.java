package com.oddrock.caj2pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.common.DateUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;

/**
 * 根据抓取屏幕图像做一些判断的工具类
 * @author qzfeng
 *
 */
public class ScreenJudgeUtils {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ScreenJudgeUtils.class);
	
	/**
	 * 判断ABBYY是否打开
	 * @param robotMngr
	 * @return
	 * @throws IOException
	 */
	public static boolean isABBYYOpen(RobotManager robotMngr) throws IOException{
		return comparePic(robotMngr, "abbyy.mark.open");
	}
	
	/**
	 * 判断ABBYY是否正在执行转换任务
	 * @param robotMngr
	 * @return
	 * @throws IOException
	 */
	public static boolean isABBYYTasking(RobotManager robotMngr) throws IOException{
		return comparePic(robotMngr, "abbyy.mark.tasking");
	}
	
	private static boolean comparePic(RobotManager robotMngr, String prefix) throws IOException {
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt(prefix+".x")
				,Prop.getInt(prefix+".y")
				,Prop.getInt(prefix+".width")
				,Prop.getInt(prefix+".height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get(prefix+".picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}
	
	// 测试Caj是否打开
	public static boolean isCajOpen(RobotManager robotMngr) throws IOException{
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
	
	/*
	 * 截图并保存为文件，文件名为带毫秒数的时间字符串
	 */
	public static void captureImageAndSave(RobotManager robotMngr, int x, int y, int width, int height) throws IOException {
		// 图片的保存目录从属性文件中取，如果没有定义，就放在当前目录
		String dirpath = Prop.get("captureimage.savedirpath");
		if(dirpath==null) {
			dirpath = System.getProperty("user.dir");
		}
		BufferedImageUtils.captureImageAndSave(robotMngr, x, y, width, height, dirpath, DateUtils.timeStrWithMillisWithoutPunctuation());
	}
}
