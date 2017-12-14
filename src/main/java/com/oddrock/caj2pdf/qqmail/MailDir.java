package com.oddrock.caj2pdf.qqmail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 以mail信息命名的文件夹的信息
public class MailDir {
	private File dir;
	private String timeStr;
	private String fromNick;
	private String fromEmail;
	private String subject;
	public File getDir() {
		return dir;
	}
	public void setDir(File dir) {
		this.dir = dir;
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	public String getFromNick() {
		return fromNick;
	}
	public void setFromNick(String fromNick) {
		this.fromNick = fromNick;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	@Override
	public String toString() {
		return "MailDir [dir=" + dir + ", timeStr=" + timeStr + ", fromNick=" + fromNick + ", fromEmail=" + fromEmail
				+ ", subject=" + subject + "]";
	}
	public static Set<MailDir> scanAndGetMailDir(File dir) {
		Set<MailDir> set = new HashSet<MailDir>();
		for(File sonDir : dir.listFiles()) {
			if(sonDir==null || !sonDir.exists() || !sonDir.isDirectory()) continue;
			Pattern pattern1 = Pattern.compile("(\\d{2}月\\d{2}日\\d{2}时\\d{2}分)\\]-{1,}\\[(.*)\\]-{1,}\\[(.*@.*)\\]-{1,}\\[(.*)");
	        Matcher matcher = pattern1.matcher(sonDir.getName());
	        if(matcher.matches()) {
	        	MailDir md = new MailDir();
	        	md.setTimeStr(matcher.group(1));
	        	md.setFromNick(matcher.group(2));
	        	md.setFromEmail(matcher.group(3));
	        	md.setSubject(matcher.group(4));
	        	md.setDir(sonDir);
	        	set.add(md);
	        }
 		}
		return set;
	}
	
	public static void main(String[] args) {
		String path = "C:\\Users\\qzfeng\\Desktop\\cajwait";
		scanAndGetMailDir(new File(path));
	}
}
