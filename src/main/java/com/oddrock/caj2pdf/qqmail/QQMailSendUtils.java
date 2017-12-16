package com.oddrock.caj2pdf.qqmail;


import java.io.File;
import java.io.IOException;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.mail.MailSenderExt;

public class QQMailSendUtils {
	private static Logger logger = Logger.getLogger(QQMailSendUtils.class);
	public static void sendMailWithFile(TransformInfoStater tfis) throws MessagingException, IOException {
		
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
		String senderName = "PDF快转直通车";
		if(tfis.getInfo().getTransform_type().contains("test")) {
			subjetct = "试转效果：" + tfis.getMaildir().getSubject();
			/*content = "您好，这是您的试转效果，这是我们能转换的最好效果，如果您对此满意，可以下单，我们给您全部转换。<br/><br/>" +
					"一、价格：<br/>" + 
					"1、转word或mobi每个文件（100页以内）起价2元，每增加100页加2元。有的比较复杂的转换要看具体情况开价。<br/>" + 
					"2、其他种类转换每个文件（100页以内）起价1元，每增加100页加1元。<br/>" + 
					"二、转换耗时耗人工，小店纯粹赚吆喝，谢绝还价。<br/>" + 
					"三、下单链接<br/>" + 
					"1、转word/mobi（2元1个）的下单链接：<a href=\""+Prop.get("taobao.url.baobei.caj2word")+"\">"+Prop.get("taobao.url.baobei.caj2word")+"</a><br/>" + 
					"2、其他转换（1元1个）下单链接：<a href=\""+Prop.get("taobao.url.baobei.qita")+"\">"+Prop.get("taobao.url.baobei.qita")+"</a><br/><br/><br/>" + 
					"=======================================================<br/>"+
					"<b>时间就该用在刀刃上，PDF快转直通车的理念是：用我们的服务，让您免去敲击键盘和鼠标的辛苦，帮您节省大量的宝贵时间。</b><br/>" +
					"淘宝店铺地址：<a href=\""+Prop.get("taobao.url.dianpu")+"\">"+Prop.get("taobao.url.dianpu")+"</a>";*/
			content = MailContentUtils.getTestTransformMailContent();
			content += MailContentUtils.getDianpuTailMailContent();
		}else {
			subjetct = "转换完成：" + tfis.getMaildir().getSubject();
			/*content = "您好：<br/>"+ 
					"您要的文件全部转换好放在邮件附件中了。<br/>" + 
					"文档转换主要是为了提取文字，因为毕竟是转换，格式很难和原来完全一致。如果需要调整格式，建议您重一个新文档，把原文档中的文字无格式拷贝到新文档中再编辑格式即可。<br/><br/><br/>"+
					"=======================================================<br/>"+
					//"1个20页（每页平均1500字）的文档需要普通人敲上1天键盘，而如果您有源文件交给我们转换，则只需要几分钟，然后您只需要稍加修改和调整格式即可。<br/>"+
					"<b>时间就该用在刀刃上，PDF快转直通车的理念是：用我们的服务，让您免去敲击键盘和鼠标的辛苦，帮您节省大量的宝贵时间。</b><br/>" +
					"PDF快转直通车淘宝店铺地址：<a href=\""+Prop.get("taobao.url.dianpu")+"\">"+Prop.get("taobao.url.dianpu")+"</a>";*/
			content = MailContentUtils.getTransformMailContent();
			content += MailContentUtils.getDianpuTailMailContent();
		}
		logger.warn("开始将以下文件发送邮件给："+recverAccounts);
		for(File file : tfis.getDstFileSet()) {
			logger.warn(file.getName());
		}
		MailSenderExt.sendEmail(senderAccount, senderName, senderPasswd, recverAccounts, 
				subjetct, content, true, smtpHost, smtpPort, tfis.getDstFileSet());
		logger.warn("结束将以上文件发送邮件给："+recverAccounts);
	}
}
