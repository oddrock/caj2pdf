package com.oddrock.caj2pdf;

import org.apache.log4j.Logger;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;

/**
 * 软件操控工具类
 * @author qzfeng
 *
 */
public class SoftwareControlUtils {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SoftwareControlUtils.class);
	
	public static void wait(int millis) throws InterruptedException{
		Thread.sleep(millis);
	}

	/*
	 * 打开空的caj
	 */
	public static CmdResult openCaj(){
		return CmdExecutor.getSingleInstance().exeCmd(Prop.get("cajviewer.path"));
	}
	

	/**
	 * 关闭Notepad++
	 * @return
	 */
	public static CmdResult closeNotepadpp() {
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("notice.txt.notepadpp.appname") + "\"");
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
	public static CmdResult openCaj(String cajFilePath){
		return CmdExecutor.getSingleInstance().exeCmd(
				Prop.get("cajviewer.path") + " \"" + cajFilePath + "\"");
	}

	/*
	 * 用foxit打开pdf
	 */
	public static CmdResult openPdf(String pdfFilePath) {
		return CmdExecutor.getSingleInstance().exeCmd(Prop.get("foxit.path") + " \"" + pdfFilePath + "\"");
	}
	
	/**
	 * 打开已完成文件所在目录的窗口
	 */
	public static void openFinishedWindows() {
		if(!Prop.getBool("needopenfinishedwindows")){
			return;
		}
		if(!Prop.getBool("needmovesrc2dst")){
			CmdExecutor.getSingleInstance().openDirWindows(Prop.get("srcdirpath"));
		}else {
			CmdExecutor.getSingleInstance().openDirWindows(Prop.get("dstdirpath"));
		}	
	}
}
