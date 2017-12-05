package com.oddrock.caj2pdf.main;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.biz.Caj2PdfUtils;
import com.oddrock.caj2pdf.biz.Epub2MobiUtils;
import com.oddrock.caj2pdf.biz.Img2PdfUtils;
import com.oddrock.caj2pdf.biz.Pdf2EpubUtils;
import com.oddrock.caj2pdf.biz.Pdf2MobiUtils;
import com.oddrock.caj2pdf.biz.Pdf2WordUtils;
import com.oddrock.caj2pdf.biz.PdfUtils;
import com.oddrock.caj2pdf.biz.Txt2MobiUtils;
import com.oddrock.caj2pdf.exception.TransformNodirException;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.DocBakUtils;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.selftest.SelftestFilesPool;
import com.oddrock.caj2pdf.selftest.SelftestRuleUtils;
import com.oddrock.caj2pdf.selftest.bean.SelftestRule;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.AsnycHiddenFileDeleter;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.caj2pdf.utils.TxtUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;
import com.oddrock.common.pdf.PdfManager;
import com.oddrock.common.windows.CmdExecutor;

public class DocFormatConverter {
	public static boolean selftest = false;
	private static Logger logger = Logger.getLogger(DocFormatConverter.class);
	private RobotManager robotMngr;
	public DocFormatConverter() throws AWTException {
		super();
		robotMngr = new RobotManager();
	}
	
	private void doBeforeTransform(File srcDir) throws TransformNodirException {
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
	private void doAfterTransform(String noticeContent,TransformInfoStater tfis) throws IOException, MessagingException {
		boolean debug = Prop.getBool("debug");
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
		if(Prop.getBool("needdel.midfile")){
			for(File file: tfis.getMidFileSet()) {
				file.delete();
			}
		}
		// 将需要移动的文件移动到目标文件夹
		if(Prop.getBool("needmove.srcfile") || Prop.getBool("needmove.midfile") || Prop.getBool("needmove.dstfile")) {
			tfis.setDstDir(Common.generateDstDir(tfis.getDstDir()));
			if(Prop.getBool("needmove.srcfile") && 
					(!tfis.getInfo().getTransform_type().contains("test") 
							|| (Prop.getBool("testtransform.needmove.srcfile")))) {
				Common.mvFileSet(tfis.getSrcFileSet(), tfis.getDstDir());	
			}
			if(Prop.getBool("needmove.midfile")) {
				Common.mvFileSet(tfis.getMidFileSet(), tfis.getDstDir());
			}
			if(Prop.getBool("needmove.dstfile")) {
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
		logger.warn(noticeContent+ ":" + tfis.getSrcDir().getCanonicalPath());
	}
	
	// 批量caj转pdf
	public void caj2pdf(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf", srcDir, dstDir);
		TransformFileSet fileSet;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				tfis.addSrcFile(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addDstFile(fileSet.getDstFile());
			}
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		doAfterTransform("caj转pdf已完成", tfis);
		tfis.save2db();
	}
	
	// 批量caj转pdf，用默认的源文件夹和目标文件夹
	public void caj2pdf() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2pdf(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量caj转word
	public void caj2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2word", srcDir, dstDir);
		TransformFileSet fileSet;
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				tfis.addSrcFile(file);
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addMidFile(fileSet.getDstFile());
			}
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		// 再全部pdf转word
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				tfis.addMidFile(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addDstFile(fileSet.getDstFile());
			}	
		}
		doAfterTransform("caj转word已完成", tfis);
		tfis.save2db();
	}
	
	// 批量caj转word
	public void caj2word() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转pdf
	public void caj2pdf_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf_test",srcDir, dstDir);
		TransformFileSet fileSet = null;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 找到目录下第一个caj，并转换为pdf
			if(file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".caj")) {
				fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
				if(fileSet.getSrcFile()!=null) {
					tfis.addSrcFile(fileSet.getSrcFile());
				}
				if(fileSet.getDstFile()!=null) {
					tfis.addMidFile(fileSet.getDstFile());
				}
				break;
			}
		}
		if(fileSet==null) {
			throw new TransformNofileException();
		}
		// 获得转换得到的pdf的实际页数
		int realPageCount = new PdfManager().pdfPageCount(fileSet.getDstFile().getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, fileSet.getDstFile().getCanonicalPath(), tiquPageCount);
		if(fileSet.getDstFile()!=null) {
			tfis.addDstFile(fileSet.getDstFile());
		}
		
		// 进行完成后的各项通知和扫尾工作
		doAfterTransform("caj试转pdf已完成",tfis);
		tfis.save2db();
	}
	
	// caj试转pdf
	public void caj2pdf_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		caj2pdf_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转word
	public void caj2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2word_test",srcDir, dstDir);
		TransformFileSet fileSet = null;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 找到目录下第一个caj，并转换为pdf
			if(file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".caj")) {
				fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
				tfis.addSrcFile(fileSet.getSrcFile());
				tfis.addMidFile(fileSet.getDstFile());
				break;
			}
		}
		if(fileSet==null) {
			throw new TransformNofileException();
		}
		// 获得转换得到的pdf的实际页数
		int realPageCount = new PdfManager().pdfPageCount(fileSet.getDstFile().getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, fileSet.getDstFile().getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2WordUtils.pdf2word(robotMngr, fileSet.getDstFile().getCanonicalPath());
		// 将转换后的文档移动到目标目录，如果需要的话
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		// 进行完成后的各项通知和扫尾工作
		doAfterTransform("caj试转word已完成",tfis);
		tfis.save2db();
	}
	
	// caj试转word
	public void caj2word_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException{
		caj2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转word
	public void pdf2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2word",srcDir, dstDir);
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 将单个pdf文件转换为word
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		doAfterTransform("pdf转word已完成",tfis);
		tfis.save2db();
	}
	
	// pdf批量转word
	public void pdf2word() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf试转word
	public void pdf2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2word_test",srcDir, dstDir);
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : srcDir.listFiles()){
			if(file!=null && file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".pdf")) {
				if(pdfFile==null) {
					pdfFile = file;
				}else {
					if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
						pdfFile = file;		
					}
				}
			}
		}
		if(pdfFile==null) {
			throw new TransformNofileException();
		}
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2WordUtils.pdf2word(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform("pdf试转word已完成", tfis);
		tfis.save2db();
	}
	
	public void pdf2word_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre",srcDir, dstDir);
		TransformFileSet fileSet;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		doAfterTransform("pdf转mobi已完成",tfis);
		tfis.save2db();
	}
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_bycalibre(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre_test",srcDir, dstDir);
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : srcDir.listFiles()){
			if(file!=null && file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".pdf")) {
				if(pdfFile==null) {
					pdfFile = file;
				}else {
					if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
						pdfFile = file;
					}
				}
			}
		}
		if(pdfFile==null) {
			throw new TransformNofileException();
		}
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform("pdf试转mobi已完成", tfis);
		tfis.save2db();
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_bycalibre_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量txt转mobi，用calibre
	public void txt2mobi(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi",srcDir, dstDir);
		// 将srcDir目录下的txt文件全部切割为不超过500KB大小，并且删除超过500KB大小的源文件。
		TxtUtils.splitTxtFiles(srcDir);
		TransformFileSet fileSet;
		for(File file : srcDir.listFiles()){
			if(file==null || !Common.isFileExists(file, "txt")) continue;
			fileSet = Txt2MobiUtils.txt2mobi(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		doAfterTransform("txt转mobi已完成", tfis);
		tfis.save2db();
	}
	
	public void txt2mobi() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		txt2mobi(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转txt转mobi
	public void txt2mobi_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi_test",srcDir, dstDir);
		File firstTxtFile = TxtUtils.getFirstTxtFile();
		File srcFile = TxtUtils.extractFrontPart(firstTxtFile);
		if(srcFile==null) {
			throw new TransformNofileException();
		}
		TransformFileSet fileSet = Txt2MobiUtils.txt2mobi(robotMngr, srcFile.getCanonicalPath());
		tfis.addDstFile(fileSet.getDstFile());
		tfis.addSrcFile(fileSet.getSrcFile());
		doAfterTransform("txt试转mobi已完成", tfis);
		tfis.save2db();
	}
	
	public void txt2mobi_test() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		txt2mobi_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量img转word
	public void img2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("img2word",srcDir, dstDir);
		TransformFileSet fileSet;
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		// 再全部pdf转word
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform("图片转word已完成",tfis);
		tfis.save2db();
	}
	
	public void img2word() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		img2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转img转word
	public void img2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("img2word_test",srcDir, dstDir);
		TransformFileSet fileSet;
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			if(Common.isImgFile(file)) {
				fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
				tfis.addSrcFile(fileSet.getSrcFile());
				tfis.addMidFile(fileSet.getDstFile());
				break;
			}
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		// 再全部pdf转word
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform("图片试转word已完成", tfis);
		tfis.save2db();
	}
	
	public void img2word_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		img2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转epub
	public void pdf2epub(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub",srcDir, dstDir);
		TransformFileSet fileSet;
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		doAfterTransform("pdf转epub已完成", tfis);
		tfis.save2db();
	}
	
	public void pdf2epub() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2epub(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转epub
	public void pdf2epub_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub_test",srcDir, dstDir);
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : srcDir.listFiles()){
			if(file!=null && file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".pdf")) {
				if(pdfFile==null) {
					pdfFile = file;
				}else {
					if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
						pdfFile = file;
					}
				}
			}
		}
		if(pdfFile==null) {
			throw new TransformNofileException();
		}
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform("pdf试转epub已完成", tfis);
		tfis.save2db();
	}
	
	public void pdf2epub_test() throws IOException, InterruptedException, MessagingException, TransformNofileException, TransformNodirException {
		pdf2epub_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 用abbyy进行pdf转mobi
	public void pdf2mobi_byabbyy(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy",srcDir, dstDir);
		TransformFileSet fileSet;
		// 先全部pdf转epub
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
		}
		if(tfis.getSrcFileSet().size()==0) {
			throw new TransformNofileException();
		}
		// 再全部epub转mobi
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform("pdf转mobi已完成", tfis);
		tfis.save2db();
	}
	
	public void pdf2mobi_byabbyy() throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		pdf2mobi_byabbyy(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	public void pdf2mobi_byabbyy_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy_test",srcDir, dstDir);
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : srcDir.listFiles()){
			if(file!=null && file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".pdf")) {
				if(pdfFile==null) {
					pdfFile = file;
				}else {
					if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
						pdfFile = file;	
					}
				}
			}
		}
		if(pdfFile==null) {
			throw new TransformNofileException();
		}
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform("pdf试转mobi已完成", tfis);	
		tfis.save2db();
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
	
	public void execTransform(String[] args) throws IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		String method = Prop.get("caj2pdf.start");
		if(method==null) {
			method = "caj2word";
		}
		if(args.length>=1) {
			method = args[0].trim(); 
		}
		if("caj2word".equalsIgnoreCase(method)) {
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
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException, MessagingException, TransformWaitTimeoutException, TransformNofileException, TransformNodirException {
		DocFormatConverter dfc = new DocFormatConverter();
		if(Prop.getBool("debug")) {		// 调试模式
			//dfc.img2word();
			//AbbyyUtils.openPdf(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\装配式建筑施工安全评价体系研究_杨爽.pdf");
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
