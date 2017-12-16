package com.oddrock.caj2pdf.qqmail;

import java.io.IOException;

import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.OddrockStringUtils;
import com.oddrock.common.file.FileUtils;

public class MailContentUtils {
	public static String getTestTransformMailContent() throws IOException {
		String content = FileUtils.readFileContentToStrExt(Prop.get("qqmail.replycontentfile.testtransform"));
		content = OddrockStringUtils.txtToHtml(content);
		return content;
	}
	
	public static String getTransformMailContent() throws IOException {
		String content = FileUtils.readFileContentToStrExt(Prop.get("qqmail.replycontentfile.transform"));
		content = OddrockStringUtils.txtToHtml(content);
		return content;
	}
	
	public static String getDianpuTailMailContent() throws IOException {
		String content = FileUtils.readFileContentToStrExt(Prop.get("qqmail.replycontentfile.dianputail"));
		content = OddrockStringUtils.txtToHtml(content);
		return content;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(getTestTransformMailContent());
	}
}

