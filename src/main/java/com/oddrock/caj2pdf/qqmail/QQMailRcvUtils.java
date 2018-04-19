package com.oddrock.caj2pdf.qqmail;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.mail.AttachDownloadDirGenerator;
import com.oddrock.common.mail.GeneralAttachDownloadDirGenerator;
import com.oddrock.common.mail.MailRecv;
import com.oddrock.common.mail.PopMailRcvr;
import com.oddrock.common.mail.PopMailReadRecordManager;
import com.oddrock.common.mail.qqmail.QQFileDownloadPage;
import com.oddrock.common.mail.qqmail.QQFileDownloader;

public class QQMailRcvUtils {
	private static Logger logger = Logger.getLogger(QQMailRcvUtils.class);
	
	public static File rcvAllUnreadMails(String server, String account, String passwd, 
			String folderName, boolean downloadAttachToLocal, 
			String localBaseDirPath) throws Exception{
		List<MailRecv> mails = null;
		try {
			PopMailRcvr pmr = new PopMailRcvr();
			AttachDownloadDirGenerator generator = new GeneralAttachDownloadDirGenerator();
			mails = pmr.rcvMail(server, account, passwd, folderName, downloadAttachToLocal, localBaseDirPath, generator);
			for(MailRecv mail : mails){
				downloadQQFileInMail(mail, localBaseDirPath, generator);
			}
		}catch(Exception e) {
			// 如果出现异常，则回滚已记录的邮件UID，便于重新下载。
			if(mails!=null) {
				for(MailRecv mail: mails) {
					PopMailReadRecordManager.instance.setUnReadInAllDays(account, mail.getUID());
					if(mail.getAttachments()!=null) {
						//System.out.println(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile().getCanonicalPath());
						FileUtils.deleteDirAndAllFiles(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile());
					}
				}
			}
			throw e;
		}
		return new File(localBaseDirPath);
	}

	
	public static File rcvOneUnreadMailToSrcDir() throws Exception {
		File mailDir = rcvOneUnreadMail();
		File maildirInSrcDir = null;
		if(mailDir!=null) {
			File srcDir = new File(Prop.get("srcdirpath"));
			if(!srcDir.exists()) {
				srcDir.mkdirs();
			}else {
				FileUtils.clearDir(srcDir);
			}
			maildirInSrcDir = FileUtils.copyDirToParentDir(mailDir, srcDir);
		}
		return maildirInSrcDir;
	}
	
	// 读取一封邮件
	public static File rcvOneUnreadMail() throws Exception{
		String server = Prop.get("qqmail.popserver");
		String account = Prop.get("qqmail.account"); 
		String passwd = Prop.get("qqmail.passwd"); 
		String foldername = Prop.get("qqmail.foldername"); 
		String savefolder = Prop.get("qqmail.savefolder");
		int recentDays = Prop.getInt("qqmail.recentdays");
		MailRecv mail = null;
		File dstDir = null;
		PopMailRcvr imr = new PopMailRcvr();
		AttachDownloadDirGenerator generator = new GeneralAttachDownloadDirGenerator();
		try {
			mail = imr.rcvOneMailCylclyInSpecDays(server, account, passwd, foldername, true, savefolder, generator, recentDays);
			if(mail!=null) {
				downloadQQFileInMail(mail, savefolder, generator);
				dstDir = generator.generateDir(new File(savefolder), mail);
			}
		}catch(Exception e) {
			// 如果出现异常，则回滚已记录的邮件UID，便于重新下载。
			if(mail!=null) {
				PopMailReadRecordManager.instance.setUnReadInAllDays(account, mail.getUID());
				if(mail.getAttachments()!=null) {
					//System.out.println(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile().getCanonicalPath());
					FileUtils.deleteDirAndAllFiles(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile());
				}
			}
			throw e;
		}
		// 如果收了一封没有附件的邮件，就继续收下一封
		while(mail!=null && dstDir!=null && (!dstDir.exists() || (dstDir.exists() && dstDir.listFiles()!=null && dstDir.listFiles().length==0))) {
			try {
				mail = imr.rcvOneMailCylclyInSpecDays(server, account, passwd, foldername, true, savefolder, generator, recentDays);
				if(mail!=null) {
					downloadQQFileInMail(mail, savefolder, generator);
					dstDir = generator.generateDir(new File(savefolder), mail);
				}
			}catch(Exception e) {
				// 如果出现异常，则回滚已记录的邮件UID，便于重新下载。
				if(mail!=null) {
					PopMailReadRecordManager.instance.setUnReadInAllDays(account, mail.getUID());
					if(mail.getAttachments()!=null) {
						//System.out.println(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile().getCanonicalPath());
						FileUtils.deleteDirAndAllFiles(new File(mail.getAttachments().get(0).getLocalFilePath()).getParentFile());
					}
				}
				throw e;
			}
		}
		return dstDir;
	}

	private static File downloadQQFileInMail(MailRecv mail, String localBaseDirPath, AttachDownloadDirGenerator generator) throws TransformWaitTimeoutException, IOException, InterruptedException, AWTException {
		List<QQFileDownloadPage> list = QQFileDownloader.parseQQFileDownloadPageFromQQMail(mail.getPlainContent());
		File localDir = generator.generateDir(new File(localBaseDirPath), mail);
		localDir.mkdirs();
		if(list.size()==0) return null;
		logger.warn("开始下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件...");
		String[] urlArr = new String[list.size()];
		int i = 0;
		for(QQFileDownloadPage page : list){	
			urlArr[i] = page.getPageUrl();
			i++;
		}
		QQFileDownloadUtils.download(new RobotManager(), localDir, urlArr);
		logger.warn("结束下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件");
		return localDir;
	}
	
	public static void main(String[] args) throws Exception {
		String imapserver = Prop.get("qqmail.popserver");
		String account = Prop.get("qqmail.account"); 
		String passwd = Prop.get("qqmail.passwd"); 
		String foldername = Prop.get("qqmail.foldername"); 
		String savefolder = Prop.get("qqmail.savefolder"); 
		rcvAllUnreadMails(imapserver, account, passwd, foldername, true, savefolder);
	}
}
