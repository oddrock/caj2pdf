package com.oddrock.pdf.caj2pdf;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.email.EmailManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.media.WavPlayer;
import com.oddrock.common.windows.ClipboardUtils;
import com.oddrock.common.windows.CmdExecutor;
import com.oddrock.common.windows.CmdResult;
import com.oddrock.common.windows.GlobalKeyListener;

public class Caj2PdfTransformer {
	private static Logger logger = Logger.getLogger(Caj2PdfTransformer.class);
	private RobotManager robotMngr;
	
	public Caj2PdfTransformer() throws AWTException, NativeHookException{
		robotMngr = new RobotManager();
		if(Boolean.parseBoolean(Prop.get("needesckey"))){
			GlobalScreen.registerNativeHook();//初始化ESC钩子 
	        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
		}
	}
	
	private CmdResult closeNotepadpp() {
		return CmdExecutor.getSingleInstance().exeCmd(
				"taskkill /f /im \"" + Prop.get("notice.txt.notepadpp.appname") + "\"");
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
	}
	
	/*
	 * 将一个caj文件转为pdf
	 */
	public void transCaj2Pdf(String cajFilepath, String pdfDirPath){
		String suffix = FileUtils.getFileNameSuffix(cajFilepath);
		if(!"caj".equalsIgnoreCase(suffix)){
			return;
		}
		String fileName = FileUtils.getFileNameWithoutSuffixFromFilePath(cajFilepath);
		FileUtils.mkdirIfNotExists(pdfDirPath);
		String destFilePath = pdfDirPath + java.io.File.separator+fileName;
		closeCaj();
		delayMiddle();
		openCaj(cajFilepath);
		delayMax();
		transCurrentCaj2Pdf(destFilePath);
		FileUtils.writeLineToFile(Prop.get("notice.txt.filepath"), "已完成转换："+FileUtils.getFileNameFromFilePath(cajFilepath), true);
		closeCaj();
		delayMiddle();
	}
	
	public void transCaj2PdfBatch() throws MessagingException, UnsupportedAudioFileException, IOException, LineUnavailableException{
		FileUtils.writeLineToFile(Prop.get("notice.txt.filepath"), "开始caj转换pdf...", false);
		closeNotepadpp();
		delayMiddle();
		String cajDirPath = Prop.get("srcdirpath");
		File srcDir = new File(cajDirPath);
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			return;
		}
		String pdfDirPath = Prop.get("dstdirpath");
		File[] files = srcDir.listFiles();
		for (File file : files) {
			if(file.isFile()){
				transCaj2Pdf(file.getAbsolutePath(), pdfDirPath);
			}
		}
		FileUtils.writeLineToFile(Prop.get("notice.txt.filepath"), "结束caj转换pdf...", true);
		noticeAfterFinish();
		if(Boolean.parseBoolean(Prop.get("needesckey"))){
			System.exit(0);
		}
		
	}
	
	private CmdResult openResultByNotepadpp() {
		return CmdExecutor.getSingleInstance().exeCmd(
				Prop.get("notice.txt.notepadpp.path") + " \"" + Prop.get("notice.txt.filepath")+ "\"");
	}
	
	public void sendMail(String content) throws UnsupportedEncodingException, MessagingException{
		String senderAccount = null;
		String senderPasswd = null;
		String recverAccounts = Prop.get("notice.mail.recver.accounts");
		if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("qq")){
			senderAccount = Prop.get("notice.mail.sender.qq.account");
			senderPasswd = Prop.get("notice.mail.sender.qq.passwd");
			EmailManager.sendEmailFastByAuth(senderAccount, senderPasswd, recverAccounts, content, Prop.get("notice.mail.sender.qq.smtpport"));
		}else if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("163")) {
			senderAccount = Prop.get("notice.mail.sender.163.account");
			senderPasswd = Prop.get("notice.mail.sender.163.passwd");
			EmailManager.sendEmailFast(senderAccount, senderPasswd, recverAccounts, content);
		}
	}
	
	private void noticeAfterFinish() throws MessagingException, UnsupportedAudioFileException, IOException, LineUnavailableException{
		if(Prop.getBool("notice.txt.flag")){
			openResultByNotepadpp();
		}
		if(Prop.getBool("notice.mail.flag")){
			sendMail("所有caj文件转换为PDF已完成！！！");
		}
		if(Prop.getBool("notice.sound.flag")){
			WavPlayer.play(Prop.get("notice.sound.wavpath"), Prop.getInt("notice.sound.playcount"));
		}
	}
	
	public static void main(String[] args) throws AWTException, NativeHookException, MessagingException, UnsupportedAudioFileException, IOException, LineUnavailableException {
		//String cajFilePath = "C:\\_XPS13_Doc\\Work\\项目\\上海联通能力集成平台\\2017-06-28 联通征文\\论文参考文献\\4.基于Dubbox的分布式服务架构设计与实现_谢璐俊.caj";
		Caj2PdfTransformer trans = new Caj2PdfTransformer();
		//trans.transCaj2Pdf(cajFilePath, "C:\\Users\\oddro\\Desktop\\pdfceshi");
		trans.transCaj2PdfBatch();
	}

}
