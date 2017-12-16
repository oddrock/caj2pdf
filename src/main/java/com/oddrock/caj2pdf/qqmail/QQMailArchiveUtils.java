package com.oddrock.caj2pdf.qqmail;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.DateUtils;
import com.oddrock.common.file.FileUtils;

public class QQMailArchiveUtils {
	private static Logger logger = Logger.getLogger(QQMailArchiveUtils.class);
	
	public static void archive() throws ParseException, IOException {
		File archiveDir = new File(Prop.get("qqmail.archive.basedir"), "以前的邮件");
		archiveDir.mkdirs();
		File mailSaveDir = new File(Prop.get("qqmail.savefolder"));
		int daysArchive = Prop.getInt("qqmail.archive.days");
		logger.warn("开始归档"+daysArchive+"天前的邮件到："+archiveDir.getCanonicalPath());
		if(!mailSaveDir.exists() || !mailSaveDir.isDirectory()) return;
		Date today = new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		for(File sonDir : mailSaveDir.listFiles()) {
			if(sonDir==null || !sonDir.exists() || !sonDir.isDirectory()) continue;
			Pattern pattern1 = Pattern.compile("(\\d{4})年(\\d{2})月(\\d{2})日(\\d{2})时(\\d{2})分\\]-{1,}\\[(.*)\\]-{1,}\\[(.*@.*)\\]-{1,}\\[(.*)");
	        Matcher matcher = pattern1.matcher(sonDir.getName());    
	        if(matcher.matches()) {
	        	String dateStr = matcher.group(1)+"-"+matcher.group(2)+"-"+matcher.group(3);
	        	Date sentDate = sdf.parse(dateStr);
	        	if(DateUtils.daysBetween(sentDate, today)>daysArchive) {
	        		FileUtils.mvDirToParentDir(sonDir, archiveDir);
	        		logger.warn("完成归档："+sonDir.getCanonicalPath());
	        	}
	        }
		}
		logger.warn("结束归档"+daysArchive+"天前的邮件到："+archiveDir.getCanonicalPath());
	}
}
