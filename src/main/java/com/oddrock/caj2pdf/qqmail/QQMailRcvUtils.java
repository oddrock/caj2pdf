package com.oddrock.caj2pdf.qqmail;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.mail.FromMailAttachDownloadDirGenerator;
import com.oddrock.common.mail.ImapMailRcvr;
import com.oddrock.common.mail.MailRecv;
import com.oddrock.common.mail.qqmail.QQFileDownloadPage;
import com.oddrock.common.mail.qqmail.QQFileDownloader;

public class QQMailRcvUtils {
	private static Logger logger = Logger.getLogger(QQMailRcvUtils.class);
	
	public static void rcvMail(String imapServer, String account, String passwd, 
			String folderName, boolean readwriteFlag, boolean downloadAttachToLocal, String localBaseDirPath) throws Exception{
		ImapMailRcvr imr = new ImapMailRcvr();
		List<MailRecv> mails = imr.rcvMail(imapServer, account, passwd, folderName, readwriteFlag, downloadAttachToLocal, localBaseDirPath, new FromMailAttachDownloadDirGenerator());
		for(MailRecv mail : mails){
			downloadQQFileInMail(mail, localBaseDirPath);
		}
	}

	private static void downloadQQFileInMail(MailRecv mail, String localBaseDirPath) throws TransformWaitTimeoutException, IOException, InterruptedException, AWTException {
		List<QQFileDownloadPage> list = QQFileDownloader.parseQQFileDownloadPageFromQQMail(mail.getPlainContent());
		if(list.size()==0) return;
		logger.warn("开始下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件...");
		String[] urlArr = new String[list.size()];
		int i = 0;
		for(QQFileDownloadPage page : list){	
			urlArr[i] = page.getPageUrl();
			i++;
		}
		File localDir = new FromMailAttachDownloadDirGenerator().generateDir(new File(localBaseDirPath), mail);
		localDir.mkdirs();
		QQFileDownloadUtils.download(new RobotManager(), localDir, urlArr);
		logger.warn("结束下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件");
	}
	
	public static void main(String[] args) throws Exception {
		String imapserver = Prop.get("qqmail.imapserver");
		String account = Prop.get("qqmail.account"); 
		String passwd = Prop.get("qqmail.passwd"); 
		String foldername = Prop.get("qqmail.foldername"); 
		boolean readwrite = Prop.getBool("qqmail.readwrite");
		String savefolder = Prop.get("qqmail.savefolder"); 
		rcvMail(imapserver, account, passwd, foldername, readwrite, true, savefolder);
	}
}
