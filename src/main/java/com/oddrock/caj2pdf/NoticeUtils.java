package com.oddrock.caj2pdf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.oddrock.common.mail.MailSender;
import com.oddrock.common.media.WavPlayer;

public class NoticeUtils {
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
}
