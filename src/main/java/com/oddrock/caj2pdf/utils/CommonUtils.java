package com.oddrock.caj2pdf.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.mail.MessagingException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.oddrock.common.DateUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.mail.MailSender;
import com.oddrock.common.media.WavPlayer;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;

public class CommonUtils {
	// 邮件通知
	public static void noticeMail(String content) throws UnsupportedEncodingException, MessagingException{
		if(!Prop.getBool("notice.mail.flag")){
			return;
		}
		//String content="所有caj文件转换为PDF已完成！！！";
		String senderAccount = null;
		String senderPasswd = null;
		String recverAccounts = Prop.get("notice.mail.recver.accounts");
		if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("qq")){
			senderAccount = Prop.get("notice.mail.sender.qq.account");
			senderPasswd = Prop.get("notice.mail.sender.qq.passwd");
			MailSender.sendEmailFastByAuth(senderAccount, senderPasswd, recverAccounts, content, Prop.get("notice.mail.sender.qq.smtpport"));
		}else if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("163")) {
			senderAccount = Prop.get("notice.mail.sender.163.account");
			senderPasswd = Prop.get("notice.mail.sender.163.passwd");
			MailSender.sendEmailFast(senderAccount, senderPasswd, recverAccounts, content);
		}
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
	
	// 声音通知
	public static void noticeSound(){
		if(Prop.getBool("notice.sound.flag")){
			try {
				WavPlayer.play(Prop.get("notice.sound.wavpath"), Prop.getInt("notice.sound.playcount"));
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 等待时间
	public static void wait(int millis) throws InterruptedException{
		Thread.sleep(millis);
	}
	
	// 比较已保存的图片和截取的图片
	public static boolean comparePic(RobotManager robotMngr, String prefix) throws IOException {
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
	
	// 鼠标挪开，避免挡事
	public static void moveMouseAvoidHandicap(RobotManager robotMngr) {
		robotMngr.moveMouseToRightDownCorner(Prop.getInt("xgap"),Prop.getInt("ygap"));
	}
	
	// 根据规则生成目标文件夹
	public static File generateDstDir() {
		return new File(Prop.get("dstdirpath")+File.separator+DateUtils.timeStrInChinese());
	}
	
	public static File generateDstDir(File dstDir) throws IOException {
		return new File(dstDir.getCanonicalPath()+File.separator+DateUtils.timeStrInChinese());
	}
	
	
	// 将需要移动的文件移动到目标文件夹
	public static File mvAllFilesFromSrcToDst(Set<File> needMoveFilesSet, File dstDir) throws IOException {
		// 如果需要移动的文件数为0，则返回null
		if(needMoveFilesSet.size()==0){
			return null;
		}	
		// 如果不需要移动，则返回源文件文件夹
		if(!Prop.getBool("needmovesrc2dst")){
			for(File file: needMoveFilesSet) {
				if(file!=null && dstDir!=null) {
					return file.getParentFile();
				}
			}
		}
		dstDir = generateDstDir(dstDir);
		if(!dstDir.exists() || !dstDir.isDirectory()) {
			dstDir.mkdirs();
		}
		for(File file: needMoveFilesSet) {
			if(file!=null && dstDir!=null) {
				FileUtils.moveFile(file.getCanonicalPath(), dstDir.getCanonicalPath());
			}
		}
		return dstDir;
	}
	
	// 打开已完成窗口
	public static void openFinishedWindows(File dstDir) throws IOException {
		if(!Prop.getBool("needopenfinishedwindows")){
			return;
		}
		CmdExecutor.getSingleInstance().openDirWindows(dstDir.getCanonicalPath());
	}
	// 在桌面生成一个已完成文件夹的bat文件，可以一运行立刻打开文件夹
	public static void createBatDirectToFinishedWindows(File dstDir) throws IOException{
		if(!Prop.getBool("bat.directtofinishedwindows.need")){
			return;
		}
		String parentPath = Prop.get("bat.directtofinishedwindows.parentpath");
		File file = new File(parentPath, "刚转完的.bat");
		FileUtils.writeToFile(file.getCanonicalPath(), "start /max explorer \""+dstDir.getCanonicalPath()+"\"", false, "GBK");
	}
	
}
