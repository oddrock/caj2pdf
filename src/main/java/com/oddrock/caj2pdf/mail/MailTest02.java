package com.oddrock.caj2pdf.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;

import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.mail.MailSenderExt;

public class MailTest02 {
	
	public static void main(String[] args) throws UnsupportedEncodingException, MessagingException {
		String recverAccounts = "oddrock@qq.com";
		String senderAccount = Prop.get("notice.mail.sender.qq.account");
		String senderPasswd = Prop.get("notice.mail.sender.qq.passwd");
		String smtpPort = Prop.get("notice.mail.sender.qq.smtpport");
		Set<File> attach = new HashSet<File>();
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\test.log"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\杨贤考勤.docx"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\落地思路v0.4-qzfeng - 副本.xlsx"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\00-安徽电信OSS互联网化汇报交流v0.3.pptx"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\0021 开发Eclipse启动时间显示器插件 - 副本.pptx"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\01-科大-大数据基础-1.pptx"));
		attach.add(new File("C:\\Users\\oddro\\Desktop\\2017信息安全体系培训材料.pptx"));
		//attach.add(new File("C:\\Users\\oddro\\Desktop\\公司介绍-姜锦绣版.pptx"));
		MailSenderExt.sendEmail(senderAccount, senderPasswd, recverAccounts, "很好就这样", "明晚去吃饭", true, smtpPort, attach);
	}

}
