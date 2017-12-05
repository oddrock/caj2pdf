package com.oddrock.caj2pdf.utils;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.oddrock.common.mail.MailSender;

public class AsyncQQMailSender implements Runnable{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AsyncQQMailSender.class);
	private String subject;
	private String senderAccount;
	private String senderPasswd;
	private String recverAccounts;
	private String smtpPort;
	public AsyncQQMailSender(String senderAccount,
			String senderPasswd, String recverAccounts, String subject, String smtpPort) {
		super();
		this.senderAccount = senderAccount;
		this.senderPasswd = senderPasswd;
		this.recverAccounts = recverAccounts;
		this.subject = subject;
		this.smtpPort = smtpPort;
	}
	public void run() {
		try {
			MailSender.sendEmailFastByAuth(senderAccount, senderPasswd, recverAccounts, subject, smtpPort);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	public static void sendMail(String senderAccount, String senderPasswd, String recverAccounts, String content, String smtpPort){
		new Thread(new AsyncQQMailSender(senderAccount, senderPasswd, recverAccounts, content, smtpPort)).start();
	}
}
