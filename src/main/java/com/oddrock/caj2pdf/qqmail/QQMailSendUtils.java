package com.oddrock.caj2pdf.qqmail;


import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.mail.MailSenderExt;

public class QQMailSendUtils {
	public static void sendMailWithFile(TransformInfoStater tfis) throws UnsupportedEncodingException, MessagingException {
		if(tfis.getDstFileSet().size()==0) return;
		if(tfis.getMaildir()==null) return;
		if(StringUtils.isBlank(tfis.getMaildir().getFromEmail())) return;
		String recverAccounts = tfis.getMaildir().getFromEmail();
		String senderAccount = Prop.get("qqmail.account");
		String senderPasswd = Prop.get("qqmail.passwd");
		String smtpPort = Prop.get("qqmail.smtpport");
		String subjetct = "reply：" + tfis.getMaildir().getSubject();
		String smtpHost = Prop.get("qqmail.smtpserver");
		MailSenderExt.sendEmail(senderAccount, senderPasswd, recverAccounts, 
				subjetct, subjetct, true, smtpHost, smtpPort, tfis.getDstFileSet());
	}
}
