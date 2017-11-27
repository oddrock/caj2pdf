package com.oddrock.caj2pdf.utils;

import com.oddrock.common.mail.ImapMailRcvr;

public class QQMailUtils {

	public static void main(String[] args) throws Exception {
		ImapMailRcvr iqmr = new ImapMailRcvr();
		String imapserver = Prop.get("qqmail.imapserver");
		String emailAccount = Prop.get("qqmail.account");
		String emailPasswd = Prop.get("qqmail.passwd");
		String folderName = Prop.get("qqmail.foldername");
		boolean readwriteFlag = Prop.getBool("qqmail.readwrite");
		String localAttachFolderPath = Prop.get("qqmail.savefolder");
		boolean downloadAttachToLocal = true;
		iqmr.rcvMail(imapserver, emailAccount, emailPasswd, folderName, readwriteFlag, downloadAttachToLocal, localAttachFolderPath);

	}

}
