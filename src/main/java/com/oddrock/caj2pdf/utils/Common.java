package com.oddrock.caj2pdf.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Set;

import javax.mail.MessagingException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import com.oddrock.common.DateUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.mail.MailSender;
import com.oddrock.common.media.WavPlayer;
import com.oddrock.common.pic.BufferedImageUtils;
import com.oddrock.common.pic.PictureComparator;
import com.oddrock.common.windows.CmdExecutor;

public class Common {
	private static Logger logger = Logger.getLogger(Common.class);
	
	// 邮件通知
	public static void noticeMail(String content) throws UnsupportedEncodingException, MessagingException{
		if(!Prop.getBool("notice.mail.flag")) return;
		// 如果在排除时间段，而且没有替代通知人，就不做邮件通知
		if(isInNoticeMailExcludetime() && !Prop.getBool("notice.mail.excludetime.takeplace")){
			return;
		}
		//String content="所有caj文件转换为PDF已完成！！！";
		String senderAccount = null;
		String senderPasswd = null;
		String recverAccounts = Prop.get("notice.mail.recver.accounts");
		// 如果在排除时间段，而且有替代通知人，则发送给替代的收信人
		if(isInNoticeMailExcludetime() && Prop.getBool("notice.mail.excludetime.takeplace")){
			recverAccounts = Prop.get("notice.mail.excludetime.takeplace.recver.accounts");
		}
		if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("qq")){
			senderAccount = Prop.get("notice.mail.sender.qq.account");
			senderPasswd = Prop.get("notice.mail.sender.qq.passwd");
			MailSender.sendEmailFastByAuth(senderAccount, senderPasswd, recverAccounts, content, Prop.get("notice.mail.sender.qq.smtpport"));
		}else if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("163")) {
			senderAccount = Prop.get("notice.mail.sender.163.account");
			senderPasswd = Prop.get("notice.mail.sender.163.passwd");
			MailSender.sendEmailFast(senderAccount, senderPasswd, recverAccounts, content);
		}
		logger.warn(senderAccount + "已发送邮件通知给：" + recverAccounts);
	}
	
	// 发送告警邮件
	public static void noticeAlertMail(String content) throws UnsupportedEncodingException, MessagingException{
		String senderAccount = null;
		String senderPasswd = null;
		// 暂时发送给我，稳定下来再发送给卫
		String recverAccounts = Prop.get("notice.mail.excludetime.takeplace.recver.accounts");
		if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("qq")){
			senderAccount = Prop.get("notice.mail.sender.qq.account");
			senderPasswd = Prop.get("notice.mail.sender.qq.passwd");
			MailSender.sendEmailFastByAuth(senderAccount, senderPasswd, recverAccounts, content, Prop.get("notice.mail.sender.qq.smtpport"));
		}else if(Prop.get("notice.mail.sender.type").equalsIgnoreCase("163")) {
			senderAccount = Prop.get("notice.mail.sender.163.account");
			senderPasswd = Prop.get("notice.mail.sender.163.passwd");
			MailSender.sendEmailFast(senderAccount, senderPasswd, recverAccounts, content);
		}
		logger.warn(senderAccount + "已发送邮件通知给：" + recverAccounts);
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
		// 如果不需要声音通知，或者不在通知时间段，都不需要声音通知
		if(Prop.getBool("notice.sound.flag") && !isInNoticeSoundExcludetime()){
			try {
				WavPlayer.play(Prop.get("notice.sound.wavpath"), Prop.getInt("notice.sound.playcount"));
				logger.warn("已进行声音通知");
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 异常声音通知
	public static void noticeAlertSound(){
		try {
			WavPlayer.play(Prop.get("notice.exceptionsound.wavpath"), Prop.getInt("notice.exceptionsound.playcount"));
			logger.warn("已进行异常声音通知");
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	// 等待时间
	public static void wait(int millis) throws InterruptedException{
		Thread.sleep(millis);
	}
	
	// 短时间等待
	public static void waitShort() throws InterruptedException {
		Common.wait(Prop.getInt("interval.waitminmillis"));
	}
	
	// 中等时间等待
	public static void waitM() throws InterruptedException {
		Common.wait(Prop.getInt("interval.waitmillis"));
	}
	
	// 长等时间等待
	public static void waitLong() throws InterruptedException {
		Common.wait(Prop.getInt("interval.waitlongmillis"));
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
	
	public static File generateDstDir(File dstBaseDir) throws IOException {
		return new File(dstBaseDir.getCanonicalPath()+File.separator+DateUtils.timeStrInChinese());
	}
	
	public static File generateDstDir(File dstDir, boolean needMkdir) throws IOException {
		File result = new File(dstDir.getCanonicalPath()+File.separator+DateUtils.timeStrInChinese());
		if(needMkdir) {
			result.mkdirs();
		}
		return result;
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
	
	public static void mvFileSet(Set<File> needMoveFilesSet, File dstDir) throws IOException {	
		dstDir.mkdirs();
		for(File file: needMoveFilesSet) {
			if(file!=null && dstDir!=null) {
				FileUtils.moveFile(file.getCanonicalPath(), dstDir.getCanonicalPath());
			}
		}
	}
	
	// 打开已完成窗口
	public static void openFinishedWindows(File dstDir) throws IOException {
		CmdExecutor.getSingleInstance().openDirWindows(dstDir.getCanonicalPath());
	}
	// 在桌面生成一个已完成文件夹的bat文件，可以一运行立刻打开文件夹
	public static void createBatDirectToFinishedWindows(File dstDir) throws IOException{
		String parentPath = Prop.get("bat.directtofinishedwindows.parentpath");
		File file = new File(parentPath, "刚转完的.bat");
		FileUtils.writeToFile(file.getCanonicalPath(), "start /max explorer \""+dstDir.getCanonicalPath()+"\"", false, "GBK");
	}
	
	// 当前是否处于邮件通知的排除时段
	public static boolean isInNoticeMailExcludetime() {
		if(!Prop.getBool("notice.mail.excludetime.need")) return false;
		int start = Prop.getInt("notice.mail.excludetime.start");
		int end = Prop.getInt("notice.mail.excludetime.end");
		if(start<0 || end<0 || start>=24 || end>=24) {
			return false;
		}
		@SuppressWarnings("deprecation")
		int hour = new Date().getHours();
		if((hour>=start && hour<=23) || (hour>=0 && hour<=end) ) {
			return true;
		}
		return false;
	}
	
	// 是否在排除时间段
	public static boolean isInNoticeSoundExcludetime() {
		if(!Prop.getBool("notice.sound.excludetime.need")) return false;
		int start = Prop.getInt("notice.sound.excludetime.start");
		int end = Prop.getInt("notice.sound.excludetime.end");
		if(start<0 || end<0 || start>=24 || end>=24) {
			return false;
		}
		@SuppressWarnings("deprecation")
		int hour = new Date().getHours();
		if((hour>=start && hour<=23) || (hour>=0 && hour<=end) ) {
			return true;
		}
		return false;
	}
	
	// 判断文件是否存在，并且可以判断是否符合某种后缀（后缀名不区分大小写）
	public static boolean isFileExists(File file, String suffix) throws IOException {
		if(!file.exists() || !file.isFile()) {
			return false;
		}
		if(suffix!=null && suffix.trim().length()>0) {
			suffix = suffix.trim();
			if(file.getCanonicalPath().toLowerCase().endsWith(suffix)) {
				return true;
			}else {
				return false;
			}
		}else {
			return true;
		}
	}
	
	// 判断文件是否是图片文件
	public static boolean isImgFile(File file) throws IOException {
		if(!file.exists() || !file.isFile()) {
			return false;
		}
		for(String suffix : Prop.get("img.filenamesuffix").split(",")) {
			if(file.getCanonicalPath().toLowerCase().endsWith(suffix.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, MessagingException {
		noticeMail("test");
	}
}
