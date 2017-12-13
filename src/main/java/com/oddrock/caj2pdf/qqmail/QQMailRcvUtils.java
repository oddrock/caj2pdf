package com.oddrock.caj2pdf.qqmail;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.mail.AttachDownloadDirGenerator;
import com.oddrock.common.mail.GeneralAttachDownloadDirGenerator;
import com.oddrock.common.mail.MailRecv;
import com.oddrock.common.mail.PopMailRcvr;
import com.oddrock.common.mail.PopMailReadRecordManager;
import com.oddrock.common.mail.qqmail.QQFileDownloadPage;
import com.oddrock.common.mail.qqmail.QQFileDownloader;

public class QQMailRcvUtils {
	private static Logger logger = Logger.getLogger(QQMailRcvUtils.class);
	
	public static File rcvMail(String server, String account, String passwd, 
			String folderName, boolean readwriteFlag, boolean downloadAttachToLocal, String localBaseDirPath) throws Exception{
		List<MailRecv> mails = null;
		File dstDir = null;
		try {
			PopMailRcvr imr = new PopMailRcvr();
			AttachDownloadDirGenerator generator = new GeneralAttachDownloadDirGenerator();
			mails = imr.rcvMail(server, account, passwd, folderName, readwriteFlag, downloadAttachToLocal, localBaseDirPath, generator);
			for(MailRecv mail : mails){
				dstDir = downloadQQFileInMail(mail, localBaseDirPath, generator);
			}
		}catch(Exception e) {
			// 如果出现异常，则回滚已记录的邮件UID，便于重新下载。
			if(mails!=null) {
				for(MailRecv mail: mails) {
					PopMailReadRecordManager.instance.setUnRead(account, mail.getUID());
				}
			}
			throw e;
		}
		return dstDir;
	}

	private static File downloadQQFileInMail(MailRecv mail, String localBaseDirPath, AttachDownloadDirGenerator generator) throws TransformWaitTimeoutException, IOException, InterruptedException, AWTException {
		List<QQFileDownloadPage> list = QQFileDownloader.parseQQFileDownloadPageFromQQMail(mail.getPlainContent());
		if(list.size()==0) return null;
		logger.warn("开始下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件...");
		String[] urlArr = new String[list.size()];
		int i = 0;
		for(QQFileDownloadPage page : list){	
			urlArr[i] = page.getPageUrl();
			i++;
		}
		File localDir = generator.generateDir(new File(localBaseDirPath), mail);
		localDir.mkdirs();
		QQFileDownloadUtils.download(new RobotManager(), localDir, urlArr);
		logger.warn("结束下载【"+mail.getFrom()+"】主题为【"+mail.getSubject()+"】的邮件中的QQ中转站文件");
		return localDir;
	}
	
	public static void main(String[] args) throws Exception {
		String imapserver = Prop.get("qqmail.popserver");
		String account = Prop.get("qqmail.account"); 
		String passwd = Prop.get("qqmail.passwd"); 
		String foldername = Prop.get("qqmail.foldername"); 
		boolean readwrite = Prop.getBool("qqmail.readwrite");
		String savefolder = Prop.get("qqmail.savefolder"); 
		rcvMail(imapserver, account, passwd, foldername, readwrite, true, savefolder);
	}
}
