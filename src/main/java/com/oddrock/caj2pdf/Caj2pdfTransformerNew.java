package com.oddrock.caj2pdf;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.ClipboardUtils;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;
import com.oddrock.common.windows.GlobalKeyListener;
import com.oddrock.pdf.caj2pdf.Prop;

public class Caj2pdfTransformerNew {
	private static Logger logger = Logger.getLogger(Caj2pdfTransformerNew.class);
	private RobotManager robotMngr;
	public Caj2pdfTransformerNew() throws AWTException, NativeHookException {
		super();
		robotMngr = new RobotManager();
		if(Boolean.parseBoolean(Prop.get("needesckey"))){
			GlobalScreen.registerNativeHook();//初始化ESC钩子 
	        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
		}
	}

	/*
	 * 关闭Notepad++
	 */
	/*private CmdResult closeNotepadpp() {
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("notice.txt.notepadpp.appname") + "\"");
	}*/
	
	/*
	 * 关闭福昕PDF阅读器
	 */
	/*private CmdResult closeFoxit() {
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("foxit.appname") + "\"");
	}*/
	
	/*
	 * 关闭caj阅读器
	 */
	private CmdResult closeCaj(){
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("cajviewer.appname") + "\"");
	}

	/*
	 * 打开caj文件
	 */
	private CmdResult openCaj(String cajFilePath){
		return CmdExecutor.getSingleInstance().exeCmd(
				Prop.get("cajviewer.path") + " \"" + cajFilePath + "\"");
	}
	
	// 测试Caj是否打开
	private boolean isCajOpen() throws IOException{
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
	private boolean isPrintReady() throws IOException{
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
	private boolean isInputfilename() throws IOException{
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
	
	// 是否要覆盖文件
	/*private boolean isOverwrite() throws IOException{
		boolean flag = false;
		BufferedImage image = robotMngr.createScreenCapture(Prop.getInt("cajviewer.mark.overwrite.x")
				,Prop.getInt("cajviewer.mark.overwrite.y")
				,Prop.getInt("cajviewer.mark.overwrite.width")
				,Prop.getInt("cajviewer.mark.overwrite.height"));
		if(PictureComparator.compare(image, BufferedImageUtils.read(Prop.get("cajviewer.mark.overwrite.picfilepath")))>=0.9){
			flag = true;
		}
		return flag;
	}*/
	
	// 是否正在打印
	private boolean isPrintnow() throws IOException{
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
	
	private void wait(int millis) throws InterruptedException{
		Thread.sleep(millis);
	}
	
	/**
	 * 打印单个文件
	 * @param srcCajFilePath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void caj2pdf(String srcCajFilePath) throws IOException, InterruptedException{
		closeCaj();
		while(isCajOpen()){
			logger.warn("等待关闭caj");
			wait(Prop.getInt("interval.waitmillis"));
		}
		wait(Prop.getInt("interval.waitmillis"));
		File srcFile = new File(srcCajFilePath);
		if(!srcFile.exists() || !srcFile.isFile() || !srcFile.getName().endsWith(".caj")){
			return;
		}
		openCaj(srcFile.getCanonicalPath());
		// 检查caj是否完全打开，没有就等待
		while(!isCajOpen()){
			logger.warn("等待打开caj");
			wait(Prop.getInt("interval.waitmillis"));
		}
		// 打开打印机
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_P);
		// 检查打印机是否打开，没有就等待
		while(!isPrintReady()){
			logger.warn("等待打开打印机");
			wait(Prop.getInt("interval.waitmillis"));
		}
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_O);
		// 检查是否要输入文件名了，没有就等待
		while(!isInputfilename()){
			logger.warn("等待输入文件名");
			wait(Prop.getInt("interval.waitmillis"));
		}
		File dstFile = new File(srcFile.getParent(), srcFile.getName().replaceAll(".caj$", ""));
		// 将生成的pdf文件名复制到文本框
		ClipboardUtils.setSysClipboardText(dstFile.getCanonicalPath());
		wait(Prop.getInt("interval.waitmillis"));
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		wait(Prop.getInt("interval.waitmillis"));
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		wait(Prop.getInt("interval.waitmillis"));
		// 点击确定俺妞妞
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		boolean printnow = false;
		robotMngr.pressKey(KeyEvent.VK_Y);
		while(true){
			if(isPrintnow()){
				printnow = true;
			}else if(printnow){
				break;
			}
			if(printnow){
				logger.warn("等待打印完毕");
			}else{
				logger.warn("等待开始打印");
			}
			
			wait(Prop.getInt("interval.waitminmillis"));
		}
		wait(Prop.getInt("interval.waitmillis"));
		closeCaj();
		while(isCajOpen()){
			logger.warn("等待关闭caj");
			wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("完成打印，文件位置："+dstFile.getCanonicalPath());
	}
	
	public void caj2pdfBatch(String srcDirPath) throws IOException, InterruptedException{
		File srcDir = new File(srcDirPath);
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		for(File file : srcDir.listFiles()){
			caj2pdf(file.getCanonicalPath());
		}
		logger.warn("完成"+ srcDir.getCanonicalPath() +"目录下所有caj打印成pdf！");
	}
	
	public static void main(String[] args) throws AWTException, NativeHookException, IOException, InterruptedException {
		Caj2pdfTransformerNew cts = new Caj2pdfTransformerNew();
		String srcDirPath = Prop.get("srcdirpath");
		cts.caj2pdfBatch(srcDirPath);	
	}
}
