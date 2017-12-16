package com.oddrock.caj2pdf.main;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.biz.Caj2PdfUtils;
import com.oddrock.caj2pdf.biz.Caj2WordUtils;
import com.oddrock.caj2pdf.biz.Img2WordUtils;
import com.oddrock.caj2pdf.biz.Pdf2EpubUtils;
import com.oddrock.caj2pdf.biz.Pdf2MobiUtils;
import com.oddrock.caj2pdf.biz.Pdf2WordUtils;
import com.oddrock.caj2pdf.biz.Txt2MobiUtils;
import com.oddrock.caj2pdf.exception.TransformNodirException;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.DocBakUtils;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.qqmail.MailDir;
import com.oddrock.caj2pdf.qqmail.QQMailArchiveUtils;
import com.oddrock.caj2pdf.qqmail.QQMailRcvUtils;
import com.oddrock.caj2pdf.qqmail.QQMailSendUtils;
import com.oddrock.caj2pdf.selftest.SelftestFilesPool;
import com.oddrock.caj2pdf.selftest.SelftestRuleUtils;
import com.oddrock.caj2pdf.selftest.bean.SelftestRule;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.MailDateStrTransformDstDirGenerator;
import com.oddrock.caj2pdf.utils.AsnycHiddenFileDeleter;
import com.oddrock.caj2pdf.utils.AsyncDbSaver;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.windows.ClipboardUtils;
import com.oddrock.common.windows.CmdExecutor;

public class DocFormatConverter {
	public static boolean selftest = false;
	private static Logger logger = Logger.getLogger(DocFormatConverter.class);
	private RobotManager robotMngr;
	public DocFormatConverter() throws AWTException {
		super();
		robotMngr = new RobotManager();
	}
	
	private void doBeforeTransform(TransformInfoStater tfis) throws TransformNodirException {
		File srcDir = tfis.getSrcDir();
		if(!srcDir.exists() || !srcDir.isDirectory()){
			throw new TransformNodirException(srcDir+"：该目录不存在！");
		}
		if(Prop.getBool("deletehiddenfile")) {
			// 删除隐藏文件
			FileUtils.deleteHiddenFiles(srcDir);
		}
		for(File file : srcDir.listFiles()) {
			if(file.isHidden() || file.isDirectory()) continue;
			String fileName = file.getName();
			// 看文件名中是否有多个连续的空格，如果有，则替换为1个空格。
			// 因为名字里有两个空格的文件，无法用CmdExecutor打开
			if(fileName.matches(".*\\s{2,}.*")) {
				fileName = fileName.replaceAll("\\s{2,}", " ");
				file.renameTo(new File(srcDir, fileName));
			}
			// 有的caj文件用nh结尾，需要修改后缀名
			if(fileName.matches("^.*\\.nh$")) {
				fileName = fileName.replaceAll("\\.nh$", ".caj");
				file.renameTo(new File(srcDir, fileName));
			}
		}
	}
	
	// 转换后的动作
	private void doAfterTransform(TransformInfoStater tfis) throws IOException, MessagingException {
		String noticeContent = tfis.getInfo().getTransform_type().replace("2", "转") + "已完成";
		boolean debug = Prop.getBool("debug");
		/*boolean isError = false;
		TransformException exception = null;*/
		// 如果需要发邮件
		if(tfis.isNeedSendDstFileMail()) {
			try {
				if(selftest || debug) {	// 如果是自测，始终只发给自测的邮箱，避免骚扰用户
					tfis.getMaildir().setFromEmail(Prop.get("selftest.mail.recver.accounts"));
				}
				QQMailSendUtils.sendMailWithFile(tfis);
			}catch(Exception e) {
				e.printStackTrace();
				/*isError = true;
				exception = new TransformSendFileMailException("发送邮件发生异常");*/
				noticeContent += "但发送邮件失败，请手动发送邮件！";
			}
		}
		// 如果不是调试或者自测模式，则需要备份
		if(!debug && (!selftest || Prop.getBool("selftest.simureal")) && Prop.getBool("docbak.need")) {
			// 备份不是必须步骤，任何异常不要影响正常流程
			try {
				// 备份文件，以便未来测试
				DocBakUtils.bakDoc(tfis.getInfo().getTransform_type(), tfis.getSrcFileSet());
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(tfis.isNeedDelMidFile()){
			for(File file: tfis.getMidFileSet()) {
				file.delete();
			}
		}
		// 将需要移动的文件移动到目标文件夹
		if(tfis.isNeedMoveSrcFile() || tfis.isNeedMoveMidFile() || tfis.isNeedMoveDstFile()) {
			if(tfis.isNeedMoveSrcFile() && 
						(!tfis.getInfo().getTransform_type().contains("test") 
						|| tfis.isTesttransformNeedMoveSrcFile())) {
				Common.mvFileSet(tfis.getSrcFileSet(), tfis.getDstDir());	
			}
			if(tfis.isNeedMoveMidFile()) {
				Common.mvFileSet(tfis.getMidFileSet(), tfis.getDstDir());
			}
			if(tfis.isNeedMoveDstFile()) {
				Common.mvFileSet(tfis.getDstFileSet(), tfis.getDstDir());
			}
		}
		
		// 如果是调试或者自测模式，不需要通知
		if(!debug && (!selftest || Prop.getBool("selftest.simureal"))) {
			// 通知不是必须步骤，任何异常不要影响正常流程
			try {
				// 完成后声音通知
				Common.noticeSound();
				// 完成后短信通知
				Common.noticeMail(noticeContent);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		// 如果是自测，不需要打开文件窗口
		if((!selftest || Prop.getBool("selftest.simureal")) && Prop.getBool("needopenfinishedwindows")) {
			// 打开完成后的文件夹窗口
			if(Prop.getBool("needmove.dstfile")) {		
				Common.openFinishedWindows(tfis.getDstDir());
			}else {
				Common.openFinishedWindows(tfis.getSrcDir());
			}
		}
		// 如果是调试或者自测模式，则不需要修改桌面快捷方式
		if(!debug && (!selftest || Prop.getBool("selftest.simureal")) && Prop.getBool("bat.directtofinishedwindows.need")) {
			// 在桌面生成一个已完成文件夹的bat文件，可以一运行立刻打开文件夹
			Common.createBatDirectToFinishedWindows(tfis.getDstDir());
		}
		if(Prop.getBool("deletehiddenfile")) {
			// 删除隐藏文件
			AsnycHiddenFileDeleter.delete(tfis.getSrcDir());
		}
		// 如果需要删除源文件夹
		if(tfis.isNeedDelSrcDir()) {
			FileUtils.deleteDirAndAllFiles(tfis.getSrcDir());
		}
		// 保存信息到数据库
		AsyncDbSaver.saveDb(tfis);
		if(tfis.isNeedCopyContentOnClipboard()) {
			ClipboardUtils.setSysClipboardText(tfis.getClipboardContent());
		}
		logger.warn(noticeContent+ ":" + tfis.getSrcDir().getCanonicalPath());
		
	}
	
	private void doAfter(String noticeContent, File dstDir, boolean exception) throws IOException {
		boolean debug = Prop.getBool("debug");
		boolean selftest_simureal = Prop.getBool("selftest.simureal");
		boolean needopenfinishedwindows = Prop.getBool("needopenfinishedwindows");
		// 如果是调试或者自测模式，不需要通知
		if(!debug && (!selftest || selftest_simureal)) {
			// 通知不是必须步骤，任何异常不要影响正常流程
			try {
				if(!exception) {
					// 完成后声音通知
					Common.noticeSound();
					// 完成后短信通知
					Common.noticeMail(noticeContent);
				}else {
					// 声音告警
					Common.noticeAlertSound();
					// 邮件告警
					Common.noticeAlertMail(noticeContent);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		// 如果是自测，不需要打开文件窗口
		if(!debug && (!selftest || selftest_simureal) && needopenfinishedwindows) {
			if(dstDir!=null && dstDir.exists()) {
				Common.openFinishedWindows(dstDir);
			}
		}
	}
	
	// 批量caj转pdf
	public void caj2pdf(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf", srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);	
		Caj2PdfUtils.caj2pdf_batch(tfis);
		doAfterTransform(tfis);
		
	}
	
	// 批量caj转pdf，用默认的源文件夹和目标文件夹
	public void caj2pdf() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2pdf(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量caj转word
	public void caj2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("caj2word", srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Caj2WordUtils.caj2word_batch(tfis);
		doAfterTransform(tfis);
	}
	
	// 批量caj转word
	public void caj2word() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转pdf
	public void caj2pdf_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Caj2PdfUtils.caj2pdf_test(tfis);
		doAfterTransform(tfis);
	}
	
	// caj试转pdf
	public void caj2pdf_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2pdf_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转word
	public void caj2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("caj2word_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Caj2WordUtils.caj2word_test(tfis);	
		doAfterTransform(tfis);
	}
	
	// caj试转word
	public void caj2word_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException{
		caj2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转word
	public void pdf2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2word",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2WordUtils.pdf2word_batch(tfis);
		doAfterTransform(tfis);
	}
	
	// pdf批量转word
	public void pdf2word() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf试转word
	public void pdf2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2word_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2WordUtils.pdf2word_test(tfis);	
		doAfterTransform(tfis);
	}
	
	public void pdf2word_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {	
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_bycalibre_batch(tfis);
		doAfterTransform(tfis);
	}
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_bycalibre(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_bycalibre_test(tfis);
		doAfterTransform(tfis);
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_bycalibre_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量txt转mobi，用calibre
	public void txt2mobi(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Txt2MobiUtils.txt2mobi_batch(tfis);
		doAfterTransform(tfis);
	}
	
	public void txt2mobi() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		txt2mobi(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转txt转mobi
	public void txt2mobi_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Txt2MobiUtils.txt2mobi_test(tfis);
		doAfterTransform(tfis);
	}
	
	public void txt2mobi_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		txt2mobi_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量img转word
	public void img2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {	
		TransformInfoStater tfis = new TransformInfoStater("img2word",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Img2WordUtils.img2word_batch(tfis);	
		doAfterTransform(tfis);
	}
	
	public void img2word() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		img2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转img转word
	public void img2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("img2word_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Img2WordUtils.img2word_test(tfis);
		doAfterTransform(tfis);
	}
	
	public void img2word_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		img2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转epub
	public void pdf2epub(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2EpubUtils.pdf2epub_batch(tfis);
		doAfterTransform(tfis);
	}
	
	public void pdf2epub() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2epub(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转epub
	public void pdf2epub_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2EpubUtils.pdf2epub_test(tfis);
		doAfterTransform(tfis);
	}
	
	public void pdf2epub_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2epub_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 用abbyy进行pdf转mobi
	public void pdf2mobi_byabbyy(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_byabbyy_batch(tfis);
		doAfterTransform(tfis);
	}
	
	public void pdf2mobi_byabbyy() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_byabbyy(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	public void pdf2mobi_byabbyy_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy_test",srcDir, dstDir, robotMngr);
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_byabbyy_test(tfis);
		doAfterTransform(tfis);	
	}
	
	public void pdf2mobi_byabbyy_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_byabbyy_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 执行单个测试计划
	private void execSingleSelftestRule(SelftestRule rule) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException{
		// 如果规则无效，则直接退出
		if(rule==null || !rule.isValid()) return;
		File[] oldFiles = new File(Prop.get("srcdirpath")).listFiles();
		for(File oldFile : oldFiles) {
			if(oldFile.exists() && oldFile.isFile()) {
				oldFile.delete();
			}
		}
		for(int i=0; i<rule.getTestCount(); i++){
			SelftestFilesPool.generateTestFilesByTransformType(rule.getTransformType(),rule.getFileCount());
			if(rule.getTransformType().equals("caj2pdf")){
				caj2pdf();
			}else if(rule.getTransformType().equals("caj2pdf_test")){
				caj2pdf_test();
			}else if(rule.getTransformType().equals("caj2word")){
				caj2word();
			}else if(rule.getTransformType().equals("caj2word_test")){
				caj2word_test();
			}else if(rule.getTransformType().equals("img2word")){
				img2word();
			}else if(rule.getTransformType().equals("img2word_test")){
				img2word_test();
			}else if(rule.getTransformType().equals("pdf2epub")){
				pdf2epub();
			}else if(rule.getTransformType().equals("pdf2epub_test")){
				pdf2epub_test();
			}else if(rule.getTransformType().equals("pdf2mobi_byabbyy")){
				pdf2mobi_byabbyy();
			}else if(rule.getTransformType().equals("pdf2mobi_byabbyy_test")){
				pdf2mobi_byabbyy_test();
			}else if(rule.getTransformType().equals("pdf2mobi_bycalibre")){
				pdf2mobi_bycalibre();
			}else if(rule.getTransformType().equals("pdf2mobi_bycalibre_test")){
				pdf2mobi_bycalibre_test();
			}else if(rule.getTransformType().equals("txt2mobi")){
				txt2mobi();
			}else if(rule.getTransformType().equals("txt2mobi_test")){
				txt2mobi_test();
			}else if(rule.getTransformType().equals("pdf2word")){
				pdf2word();
			}else if(rule.getTransformType().equals("pdf2word_test")){
				pdf2word_test();
			}
		}
	}
	
	// 自测模式
	public void selftest() throws IOException {
		selftest = true;
		List<SelftestRule> rules = SelftestRuleUtils.getSelftestRules();
		for (SelftestRule sr : rules) {
			try {
				execSingleSelftestRule(sr);
			} catch (Throwable e) {		// 单次执行出现任何问题都不影响其他测试
				e.printStackTrace();
			}
		}
		selftest = false;
	}
	
	// 下载QQ邮件中的附件
	public void download_qqmailfiles() throws IOException, ParseException {
		logger.warn("开始下载QQ邮件...");
		QQMailArchiveUtils.archive();
		String noticeContent = "下载QQ邮件成功，请回到电脑继续操作！";
		File dstDir = null;
		boolean exception = false;
		String imapserver = Prop.get("qqmail.popserver");
		String account = Prop.get("qqmail.account"); 
		String passwd = Prop.get("qqmail.passwd"); 
		String foldername = Prop.get("qqmail.foldername"); 
		boolean readwrite = Prop.getBool("qqmail.readwrite");
		String savefolder = Prop.get("qqmail.savefolder");
		try {
			dstDir = QQMailRcvUtils.rcvAllUnreadMails(imapserver, account, passwd, foldername, readwrite, true, savefolder);
		} catch (Exception e) {
			e.printStackTrace();
			noticeContent = "下载QQ邮件失败，请自行手动下载QQ邮件！！！";
			exception = true;
		}finally {
			if(dstDir==null) noticeContent="没有需要下载的QQ邮件，请回到电脑继续操作！";
			doAfter(noticeContent,dstDir,exception);
		}
		logger.warn("完成下载QQ邮件...");
	}
	
	private void download_one_qqmailfiles() throws IOException, ParseException {
		logger.warn("开始下载一封含附件的QQ未读邮件...");
		QQMailArchiveUtils.archive();
		String noticeContent = "下载QQ邮件成功，请回到电脑继续操作！！！";
		File dstDir = null;
		boolean exception = false;
		try {
			dstDir = QQMailRcvUtils.rcvOneUnreadMailToSrcDir();
		}catch (Exception e) {
			e.printStackTrace();
			noticeContent = "下载QQ邮件失败，请自行手动下载QQ邮件！！！";
			exception = true;
		}finally {
			doAfter(noticeContent,dstDir,exception);
		}
		logger.warn("结束下载一封含附件的QQ未读邮件...");
	}
	
	private void caj2word_sendmail(MailDir md) throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("caj2word", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Caj2WordUtils.caj2word_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void caj2word_sendmail() throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			caj2word_sendmail(md);
		}
	}
	
	private void caj2word_test_sendmail() throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			caj2word_test_sendmail(md);
		}
	}

	private void caj2word_test_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("caj2word_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Caj2WordUtils.caj2word_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void caj2pdf_sendmail() throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			caj2pdf_sendmail(md);
		}
	}

	private void caj2pdf_sendmail(MailDir md) throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Caj2PdfUtils.caj2pdf_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void caj2pdf_test_sendmail() throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			caj2pdf_test_sendmail(md);
		}
	}

	private void caj2pdf_test_sendmail(MailDir md) throws TransformNodirException, TransformWaitTimeoutException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Caj2PdfUtils.caj2pdf_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2word_sendmail() throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2word_sendmail(md);
		}
	}

	private void pdf2word_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2word", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2WordUtils.pdf2word_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2word_test_sendmail() throws TransformNofileException, TransformNodirException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2word_test_sendmail(md);
		}
	}

	private void pdf2word_test_sendmail(MailDir md) throws TransformNofileException, IOException, InterruptedException, TransformNodirException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2word_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2WordUtils.pdf2word_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2mobi_bycalibre_sendmail() throws TransformNofileException, TransformWaitTimeoutException, TransformNodirException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2mobi_bycalibre_sendmail(md);
		}
	}

	private void pdf2mobi_bycalibre_sendmail(MailDir md) throws TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, TransformNodirException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_bycalibre_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2mobi_bycalibre_test_sendmail() throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2mobi_bycalibre_test_sendmail(md);
		}
	}

	private void pdf2mobi_bycalibre_test_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_bycalibre_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void txt2mobi_sendmail() throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			txt2mobi_sendmail(md);
		}
	}

	private void txt2mobi_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Txt2MobiUtils.txt2mobi_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void txt2mobi_test_sendmail() throws TransformNofileException, TransformWaitTimeoutException, TransformNodirException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			txt2mobi_test_sendmail(md);
		}
	}

	private void txt2mobi_test_sendmail(MailDir md) throws TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, TransformNodirException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Txt2MobiUtils.txt2mobi_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void img2word_sendmail() throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			img2word_sendmail(md);
		}
	}

	private void img2word_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("img2word", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Img2WordUtils.img2word_batch(tfis);	
		doAfterTransform(tfis);
	}
	
	private void img2word_test_sendmail() throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			img2word_test_sendmail(md);
		}
	}

	private void img2word_test_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("img2word_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Img2WordUtils.img2word_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2epub_sendmail() throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2epub_sendmail(md);
		}
	}

	private void pdf2epub_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2EpubUtils.pdf2epub_batch(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2epub_test_sendmail() throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2epub_test_sendmail(md);
		}
	}

	private void pdf2epub_test_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2EpubUtils.pdf2epub_test(tfis);
		doAfterTransform(tfis);
	}
	
	private void pdf2mobi_byabbyy_sendmail() throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2mobi_byabbyy_sendmail(md);
		}
	}
	
	private void pdf2mobi_byabbyy_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(true);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_byabbyy_batch(tfis);
		doAfterTransform(tfis);
	}

	private void pdf2mobi_byabbyy_test_sendmail() throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		Set<MailDir> set = MailDir.scanAndGetMailDir(new File(Prop.get("srcdirpath")));
		for(MailDir md : set) {
			pdf2mobi_byabbyy_test_sendmail(md);
		}
	}

	private void pdf2mobi_byabbyy_test_sendmail(MailDir md) throws TransformNodirException, TransformNofileException, TransformWaitTimeoutException, IOException, InterruptedException, MessagingException {
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy_test", md.getDir(), new File(Prop.get("dstdirpath")), robotMngr, new MailDateStrTransformDstDirGenerator());
		tfis.setNeedDelSrcDir(false);
		tfis.setNeedSendDstFileMail(true);
		tfis.setMaildir(md);
		tfis.setNeedCopyContentOnClipboard(true);
		tfis.setClipboardContent("您的文件试转效果已经转好发到您的邮箱了。");
		doBeforeTransform(tfis);
		Pdf2MobiUtils.pdf2mobi_byabbyy_test(tfis);
		doAfterTransform(tfis);
	}

	public void execTransform(String[] args) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException, ParseException {
		String method = Prop.get("caj2pdf.start");
		if(method==null) {
			method = "caj2word";
		}
		if(args.length>=1) {
			method = args[0].trim(); 
		}
		if("caj2word_sendmail".equalsIgnoreCase(method)) {
			caj2word_sendmail();
		}else if("caj2word_test_sendmail".equalsIgnoreCase(method)) {
			caj2word_test_sendmail();
		}else if("caj2pdf_sendmail".equalsIgnoreCase(method)) {
			caj2pdf_sendmail();
		}else if("caj2pdf_test_sendmail".equalsIgnoreCase(method)) {
			caj2pdf_test_sendmail();
		}else if("pdf2word_sendmail".equalsIgnoreCase(method)) {
			pdf2word_sendmail();
		}else if("pdf2word_test_sendmail".equalsIgnoreCase(method)) {
			pdf2word_test_sendmail();
		}else if("pdf2mobi_bycalibre_sendmail".equalsIgnoreCase(method)) {
			pdf2mobi_bycalibre_sendmail();
		}else if("pdf2mobi_bycalibre_test_sendmail".equalsIgnoreCase(method)) {
			pdf2mobi_bycalibre_test_sendmail();
		}else if("txt2mobi_sendmail".equalsIgnoreCase(method)) {
			txt2mobi_sendmail();
		}else if("txt2mobi_test_sendmail".equalsIgnoreCase(method)) {
			txt2mobi_test_sendmail();
		}else if("img2word_sendmail".equalsIgnoreCase(method)) {
			img2word_sendmail();
		}else if("img2word_test_sendmail".equalsIgnoreCase(method)) {
			img2word_test_sendmail();
		}else if("pdf2epub_sendmail".equalsIgnoreCase(method)) {
			pdf2epub_sendmail();
		}else if("pdf2epub_test_sendmail".equalsIgnoreCase(method)) {
			pdf2epub_test_sendmail();
		}else if("pdf2mobi_byabbyy_sendmail".equalsIgnoreCase(method)) {
			pdf2mobi_byabbyy_sendmail();
		}else if("pdf2mobi_byabbyy_test_sendmail".equalsIgnoreCase(method)) {
			pdf2mobi_byabbyy_test_sendmail();
		}else if("caj2word".equalsIgnoreCase(method)) {
			caj2word();
		}else if("caj2word_test".equalsIgnoreCase(method)) {
			caj2word_test();
		}else if("caj2pdf".equalsIgnoreCase(method)) {
			caj2pdf();
		}else if("caj2pdf_test".equalsIgnoreCase(method)) {
			caj2pdf_test();
		}else if("pdf2word".equalsIgnoreCase(method)) {
			pdf2word();
		}else if("pdf2word_test".equalsIgnoreCase(method)) {
			pdf2word_test();
		}else if("pdf2mobi_bycalibre".equalsIgnoreCase(method)) {
			pdf2mobi_bycalibre();
		}else if("pdf2mobi_bycalibre_test".equalsIgnoreCase(method)) {
			pdf2mobi_bycalibre_test();
		}else if("txt2mobi".equalsIgnoreCase(method)) {
			txt2mobi();
		}else if("txt2mobi_test".equalsIgnoreCase(method)) {
			txt2mobi_test();
		}else if("img2word".equalsIgnoreCase(method)) {
			img2word();
		}else if("img2word_test".equalsIgnoreCase(method)) {
			img2word_test();
		}else if("pdf2epub".equalsIgnoreCase(method)) {
			pdf2epub();
		}else if("pdf2epub_test".equalsIgnoreCase(method)) {
			pdf2epub_test();
		}else if("pdf2mobi_byabbyy".equalsIgnoreCase(method)) {
			pdf2mobi_byabbyy();
		}else if("pdf2mobi_byabbyy_test".equalsIgnoreCase(method)) {
			pdf2mobi_byabbyy_test();
		}else if("selftest".equalsIgnoreCase(method)) {
			selftest();
		}else if("download_qqmailfiles".equalsIgnoreCase(method)) {
			download_qqmailfiles();
		}else if("download_one_qqmailfiles".equalsIgnoreCase(method)) {
			download_one_qqmailfiles();
		}else if("captureimage".equalsIgnoreCase(method)) {
			if(args.length>=6) {
				Thread.sleep(Integer.parseInt(args[1])*1000);
				Common.captureImageAndSave(robotMngr, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			}
		}else if("tasklist".equalsIgnoreCase(method)) {
			String dstDir = Prop.get("tasklist.savedirpath");
			String filename = Prop.get("tasklist.filename");
			if(args.length>=2) {
				dstDir = args[1].trim();
			}
			if(args.length>=3) {
				filename = args[2].trim();
			}
			CmdExecutor.getSingleInstance().exportTasklistToFile(new File(dstDir, filename));
		}
	}



	public static void main(String[] args) throws AWTException, IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException, ParseException {
		DocFormatConverter dfc = new DocFormatConverter();
		if(Prop.getBool("debug")) {		// 调试模式
			//dfc.download_one_qqmailfiles();
			//dfc.pdf2mobi_byabbyy_test_sendmail();
			dfc.selftest();
		}else {
			try {
				dfc.execTransform(args);
			} catch (TransformWaitTimeoutException e) {
				e.printStackTrace();
				// 声音告警
				Common.noticeAlertSound();
				// 邮件告警
				Common.noticeAlertMail("转换错误：转换等待时间过长！！！");
			}catch (TransformNofileException e) {
				e.printStackTrace();
				// 声音告警
				Common.noticeAlertSound();
				// 邮件告警
				Common.noticeAlertMail("转换错误：文件夹里没有要转换的文件！！！");
			} catch (TransformNodirException e) {
				e.printStackTrace();
				// 声音告警
				Common.noticeAlertSound();
				// 邮件告警
				Common.noticeAlertMail("转换错误：文件夹不存在！！！");
			}
		}
	}
}
