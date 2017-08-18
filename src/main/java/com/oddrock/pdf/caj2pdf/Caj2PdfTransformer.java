package com.oddrock.pdf.caj2pdf;

import java.awt.AWTException;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;

public class Caj2PdfTransformer {
	private static Logger logger = Logger.getLogger(Caj2PdfTransformer.class);
	private RobotManager robotMngr;
	
	public Caj2PdfTransformer() throws AWTException{
		robotMngr = new RobotManager();
	}
	
	private CmdResult openCaj(String cajFilePath){
		String cajViwerPath = Prop.get("cajviewer.path");
		return CmdExecutor.getSingleInstance().exeCmd(
				cajViwerPath + " \"" + cajFilePath + "\"");
	}
	
	private CmdResult closeCaj(){
		String cajviewerName = Prop.get("cajviewer.appname");
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + cajviewerName + "\"");
	}
	
	private CmdResult closeFoxit() {
		String foxitAppName = Prop.get("foxit.appname");
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + foxitAppName + "\"");
	}
	
	private void delayMin(){
		robotMngr.delay(Prop.getInt("cajviewer.operdelay.min"));
	}
	
	private void delayMiddle(){
		robotMngr.delay(Prop.getInt("cajviewer.operdelay.middle"));
	}
	
	private void delayMax(){
		robotMngr.delay(Prop.getInt("cajviewer.operdelay.max"));
	}
	
	/*
	 * 假设caj文件已被打开
	 */
	private void transCurrentCaj2Pdf(String dstFilePath){	
		delayMiddle();
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_P);
		delayMin();
		for(int i=0; i<10; i++){
			robotMngr.pressUp();
		}
		delayMin();
		for(int i=1; i< Prop.getInt("cajviewer.print.microsoftindex");i++){
			robotMngr.pressDown();
		}
		delayMin();
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_O);
		delayMin();
		ClipboardUtils.setSysClipboardText(dstFilePath);
		delayMin();	
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		delayMiddle();	
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		delayMiddle();	
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		delayMiddle();
		robotMngr.pressKey(KeyEvent.VK_Y);
		robotMngr.delay(Prop.getInt("cajviewer.operdelay.aftersavepdf"));
		closeFoxit();
		delayMin();
	}
	
	public static void main(String[] args) throws AWTException {
		String cajFilePath = "C:\\_XPS13_Doc\\Work\\项目\\上海联通能力集成平台\\2017-06-28 联通征文\\论文参考文献\\4.基于Dubbox的分布式服务架构设计与实现_谢璐俊.caj";
		Caj2PdfTransformer trans = new Caj2PdfTransformer();
		trans.openCaj(cajFilePath);
		trans.robotMngr.delay(10000);
		trans.transCurrentCaj2Pdf("C:\\Users\\oddro\\Desktop\\123.pdf");
		trans.robotMngr.delay(10000);
		trans.closeCaj();
	}

}
