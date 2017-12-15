package com.oddrock.caj2pdf.qqmail;


import java.io.File;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.mail.MailSenderExt;

public class QQMailSendUtils {
	private static Logger logger = Logger.getLogger(QQMailSendUtils.class);
	public static void sendMailWithFile(TransformInfoStater tfis) throws UnsupportedEncodingException, MessagingException {
		
		if(tfis.getDstFileSet().size()==0) return;
		if(tfis.getMaildir()==null) return;
		if(StringUtils.isBlank(tfis.getMaildir().getFromEmail())) return;
		String recverAccounts = tfis.getMaildir().getFromEmail();
		String senderAccount = Prop.get("qqmail.account");
		String senderPasswd = Prop.get("qqmail.passwd");
		String smtpPort = Prop.get("qqmail.smtpport");
		String subjetct = null;
		String content = null;
		String smtpHost = Prop.get("qqmail.smtpserver");
		if(tfis.getInfo().getTransform_type().contains("test")) {
			subjetct = "试转效果：" + tfis.getMaildir().getSubject();
			content = "您好，这是您的试转效果，这是我们能转换的最好效果，如果您对此满意，可以下单，我们给您全部转换。\r\n\r\n";
			content += "一、价格：\r\n" + 
					"1、转word或mobi每个文件（100页以内）起价2元，每增加100页加2元。有的比较复杂的转换要看具体情况开价。\r\n" + 
					"2、其他种类转换每个文件（100页以内）起价1元，每增加100页加1元。\r\n" + 
					"二、如果您转换的量大（5个文件以上），我们也可以给您提供试转服务，您看到效果满意再足额拍下宝贝付款再转剩下的部分。\r\n" + 
					"三、转换耗时耗人工，小店纯粹赚吆喝，谢绝还价。\r\n" + 
					"四、下单链接\r\n" + 
					"1、转word/mobi（2元1个）的下单链接：\r\n" + 
					"https://item.taobao.com/item.htm?spm=a1z38n.10677092.0.0.1ac3f133jzyjzD&amp;id=561901256897\r\n" + 
					"2、其他转换（1元1个）下单链接是：https://item.taobao.com/item.htm?spm=a1z38n.10677092.0.0.e6eb7bvZKjYT&amp;id=554002751556";
		}else {
			subjetct = "转换完成：" + tfis.getMaildir().getSubject();
			content = "您好，您要的文件全部转换好放在邮件附件中了。\r\n";
		}
		logger.warn("开始将以下文件发送邮件给："+recverAccounts);
		for(File file : tfis.getDstFileSet()) {
			logger.warn(file.getName());
		}
		MailSenderExt.sendEmail(senderAccount, senderPasswd, recverAccounts, 
				subjetct, content, true, smtpHost, smtpPort, tfis.getDstFileSet());
		logger.warn("结束将以上文件发送邮件给："+recverAccounts);
	}
}
