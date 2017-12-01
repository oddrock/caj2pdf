package com.oddrock.caj2pdf.main;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.oddrock.caj2pdf.persist.DocBakUtils;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.selftest.SelftestFilesPool;
import com.oddrock.caj2pdf.selftest.SelftestRuleUtils;
import com.oddrock.caj2pdf.selftest.bean.SelftestRule;
import com.oddrock.caj2pdf.utils.Common;
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
	
	private void doBeforeTransform(File srcDir) {
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
		}
	}
	
	// 转换后的动作
	private void doAfterTransform(File srcDir, File dstDir, Set<File> needMoveFilesSet, String noticeContent, String transformType, Set<File> needBakFileSet) throws IOException, MessagingException {
		boolean debug = Prop.getBool("debug");
		// 如果不是调试或者自测模式，则需要备份
		if(!debug && !selftest && Prop.getBool("docbak.need")) {
			// 备份不是必须步骤，任何异常不要影响正常流程
			try {
				// 备份文件，以便未来测试
				DocBakUtils.bakDoc(transformType, needBakFileSet);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		// 将需要移动的文件移动到目标文件夹
		dstDir = Common.mvAllFilesFromSrcToDst(needMoveFilesSet, dstDir);
		// 如果是调试或者自测模式，不需要通知
		if(!debug && !selftest) {
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
		if(!selftest && Prop.getBool("needopenfinishedwindows")) {
			// 打开完成后的文件夹窗口
			Common.openFinishedWindows(dstDir);
		}
		// 如果是调试或者自测模式，则不需要修改桌面快捷方式
		if(!debug && !selftest && Prop.getBool("bat.directtofinishedwindows.need")) {
			// 在桌面生成一个已完成文件夹的bat文件，可以一运行立刻打开文件夹
			Common.createBatDirectToFinishedWindows(dstDir);
		}
		if(Prop.getBool("deletehiddenfile")) {
			// 删除隐藏文件
			FileUtils.deleteHiddenFiles(srcDir);
		}
		logger.warn(noticeContent+ ":" + srcDir.getCanonicalPath());
	}
	
	// 批量caj转pdf
	public void caj2pdf(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="caj2pdf";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				needMoveFilesSet.add(fileSet.getSrcFile());
				needBakFileSet.add(fileSet.getSrcFile());
				tfis.addSrcFile(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				needMoveFilesSet.add(fileSet.getDstFile());
				tfis.addDstFile(fileSet.getDstFile());
			}
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "caj转pdf已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	// 批量caj转pdf，用默认的源文件夹和目标文件夹
	public void caj2pdf() throws IOException, InterruptedException, MessagingException {
		caj2pdf(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量caj转word
	public void caj2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="caj2word";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2word");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		// 存放待转换的pdf文件
		Set<File> pdfFileSet = new HashSet<File>();
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				tfis.addSrcFile(file);
				needMoveFilesSet.add(fileSet.getSrcFile());
				needBakFileSet.add(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				needMoveFilesSet.add(fileSet.getDstFile());
				pdfFileSet.add(fileSet.getDstFile());
			}
		}
		// 再全部pdf转word
		for(File file : pdfFileSet){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			if(fileSet.getSrcFile()!=null) {
				needMoveFilesSet.add(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addDstFile(fileSet.getDstFile());
				needMoveFilesSet.add(fileSet.getDstFile());
			}	
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "caj转word已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	// 批量caj转word
	public void caj2word() throws IOException, InterruptedException, MessagingException {
		caj2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转pdf
	public void caj2pdf_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="caj2pdf_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2pdf_test");
		if(!srcDir.exists() || !srcDir.isDirectory()) return;
		TransformFileSet fileSet = null;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 找到目录下第一个caj，并转换为pdf
			if(file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".caj")) {
				fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
				if(fileSet.getSrcFile()!=null) {
					tfis.addSrcFile(fileSet.getSrcFile());
					needBakFileSet.add(fileSet.getSrcFile());
				}
				break;
			}
		}
		if(fileSet==null) return;
		// 获得转换得到的pdf的实际页数
		int realPageCount = new PdfManager().pdfPageCount(fileSet.getDstFile().getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, fileSet.getDstFile().getCanonicalPath(), tiquPageCount);
		if(fileSet.getDstFile()!=null) {
			// 将需要移动的文档记录下来
			needMoveFilesSet.add(fileSet.getDstFile());
			tfis.addDstFile(fileSet.getDstFile());
		}
		
		// 进行完成后的各项通知和扫尾工作
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "caj试转pdf已完成",transformType,needBakFileSet);
		tfis.save2db();
	}
	
	// caj试转pdf
	public void caj2pdf_test() throws IOException, InterruptedException, MessagingException {
		caj2pdf_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// caj试转word
	public void caj2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="caj2word_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("caj2word_test");
		if(!srcDir.exists() || !srcDir.isDirectory()) return;
		TransformFileSet fileSet = null;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 找到目录下第一个caj，并转换为pdf
			if(file.exists() && file.isFile() && file.getCanonicalPath().endsWith(".caj")) {
				fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
				tfis.addSrcFile(fileSet.getSrcFile());
				needBakFileSet.add(fileSet.getSrcFile());
				break;
			}
		}
		if(fileSet==null) return;
		// 获得转换得到的pdf的实际页数
		int realPageCount = new PdfManager().pdfPageCount(fileSet.getDstFile().getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, fileSet.getDstFile().getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2WordUtils.pdf2word(robotMngr, fileSet.getDstFile().getCanonicalPath());
		// 将转换后的文档移动到目标目录，如果需要的话
		needMoveFilesSet.add(fileSet.getSrcFile());
		needMoveFilesSet.add(fileSet.getDstFile());
		tfis.addDstFile(fileSet.getDstFile());
		// 进行完成后的各项通知和扫尾工作
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "caj试转word已完成",transformType, needBakFileSet);
		tfis.save2db();
	}
	
	// caj试转word
	public void caj2word_test() throws IOException, InterruptedException, MessagingException{
		caj2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转word
	public void pdf2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2word";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2word");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet;
		// 存放需要移动的文件
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			// 将单个pdf文件转换为word
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf转word已完成",transformType, needBakFileSet);
		tfis.save2db();
	}
	
	// pdf批量转word
	public void pdf2word() throws IOException, InterruptedException, MessagingException {
		pdf2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf试转word
	public void pdf2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2word_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2word_test");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		// 存放需要移动的文件
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
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
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		needBakFileSet.add(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2WordUtils.pdf2word(robotMngr, fileSet.getDstFile().getCanonicalPath());
		needMoveFilesSet.add(fileSet.getDstFile());
		needMoveFilesSet.add(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf试转word已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void pdf2word_test() throws IOException, InterruptedException, MessagingException {
		pdf2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2mobi_bycalibre";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getSrcFile());
			needMoveFilesSet.add(fileSet.getDstFile());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf转mobi已完成",transformType, needBakFileSet);
		tfis.save2db();
	}
	
	// 批量pdf转mobi，用calibre
	public void pdf2mobi_bycalibre() throws IOException, InterruptedException, MessagingException {
		pdf2mobi_bycalibre(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2mobi_bycalibre_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_bycalibre_test");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		// 存放需要移动的文件
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
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
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		needBakFileSet.add(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		needMoveFilesSet.add(fileSet.getDstFile());
		needMoveFilesSet.add(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf试转mobi已完成",transformType,needBakFileSet);
		tfis.save2db();
	}
	
	// 试转pdf转mobi
	public void pdf2mobi_bycalibre_test() throws IOException, InterruptedException, MessagingException {
		pdf2mobi_bycalibre_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量txt转mobi，用calibre
	public void txt2mobi(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="txt2mobi";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 将srcDir目录下的txt文件全部切割为不超过500KB大小，并且删除超过500KB大小的源文件。
		TxtUtils.splitTxtFiles(srcDir);
		TransformFileSet fileSet;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null || !Common.isFileExists(file, "txt")) continue;
			fileSet = Txt2MobiUtils.txt2mobi(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getSrcFile());
			needMoveFilesSet.add(fileSet.getDstFile());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "txt转mobi已完成",transformType,needBakFileSet);
		tfis.save2db();
	}
	
	public void txt2mobi() throws IOException, InterruptedException, MessagingException {
		txt2mobi(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转txt转mobi
	public void txt2mobi_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="txt2mobi_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("txt2mobi_test");
		File firstTxtFile = TxtUtils.getFirstTxtFile();
		File srcFile = TxtUtils.extractFrontPart(firstTxtFile);
		if(srcFile==null) {
			logger.warn("没有txt文件可以试转");
			return;
		}
		TransformFileSet fileSet = Txt2MobiUtils.txt2mobi(robotMngr, srcFile.getCanonicalPath());
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		needMoveFilesSet.add(fileSet.getSrcFile());
		needMoveFilesSet.add(fileSet.getDstFile());
		tfis.addDstFile(fileSet.getDstFile());
		tfis.addSrcFile(fileSet.getSrcFile());
		needBakFileSet.add(firstTxtFile);
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "txt试转mobi已完成",transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void txt2mobi_test() throws IOException, InterruptedException, MessagingException {
		txt2mobi_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 批量img转word
	public void img2word(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="img2word";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("img2word");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		// 存放待转换的pdf文件
		Set<File> imgFileSet = new HashSet<File>();
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			imgFileSet.add(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		// 再全部pdf转word
		for(File file : imgFileSet){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "图片转word已完成",transformType,needBakFileSet);
		tfis.save2db();
	}
	
	public void img2word() throws IOException, InterruptedException, MessagingException {
		img2word(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转img转word
	public void img2word_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="img2word_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("img2word_test");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		// 存放待转换的pdf文件
		Set<File> imgFileSet = new HashSet<File>();
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		// 先全部caj转pdf
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			if(Common.isImgFile(file)) {
				fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
				needMoveFilesSet.add(fileSet.getDstFile());
				imgFileSet.add(fileSet.getDstFile());
				tfis.addSrcFile(fileSet.getSrcFile());
				needBakFileSet.add(fileSet.getSrcFile());
				break;
			}
		}
		// 再全部pdf转word
		for(File file : imgFileSet){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "图片试转word已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void img2word_test() throws IOException, InterruptedException, MessagingException {
		img2word_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// pdf批量转epub
	public void pdf2epub(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2epub";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getSrcFile());
			needMoveFilesSet.add(fileSet.getDstFile());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf转epub已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void pdf2epub() throws IOException, InterruptedException, MessagingException {
		pdf2epub(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 试转pdf转epub
	public void pdf2epub_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2epub_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2epub_test");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		// 存放需要移动的文件
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
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
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		needBakFileSet.add(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		needMoveFilesSet.add(fileSet.getDstFile());
		needMoveFilesSet.add(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf试转epub已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void pdf2epub_test() throws IOException, InterruptedException, MessagingException {
		pdf2epub_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 用abbyy进行pdf转mobi
	public void pdf2mobi_byabbyy(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2mobi_byabbyy";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		TransformFileSet fileSet;
		// 存放待转换的pdf文件
		Set<File> imgFileSet = new HashSet<File>();
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
		// 先全部pdf转epub
		for(File file : srcDir.listFiles()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			imgFileSet.add(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
			needBakFileSet.add(fileSet.getSrcFile());
		}
		// 再全部epub转mobi
		for(File file : imgFileSet){
			if(file==null) continue;
			fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, file.getCanonicalPath());
			needMoveFilesSet.add(fileSet.getDstFile());
			needMoveFilesSet.add(fileSet.getSrcFile());
			tfis.addDstFile(fileSet.getDstFile());
		}
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf转mobi已完成", transformType, needBakFileSet);
		tfis.save2db();
	}
	
	public void pdf2mobi_byabbyy() throws IOException, InterruptedException, MessagingException {
		pdf2mobi_byabbyy(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	public void pdf2mobi_byabbyy_test(File srcDir, File dstDir) throws IOException, InterruptedException, MessagingException {
		String transformType="pdf2mobi_byabbyy_test";
		doBeforeTransform(srcDir);
		TransformInfoStater tfis = new TransformInfoStater("pdf2mobi_byabbyy_test");
		if(!srcDir.exists() || !srcDir.isDirectory()){
			return;
		}
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		// 存放需要移动的文件
		Set<File> needMoveFilesSet = new HashSet<File>();
		Set<File> needBakFileSet = new HashSet<File>();
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
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		needBakFileSet.add(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount);
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		needMoveFilesSet.add(fileSet.getSrcFile());
		fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		needMoveFilesSet.add(fileSet.getDstFile());
		needMoveFilesSet.add(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
		doAfterTransform(srcDir, dstDir, needMoveFilesSet, "pdf试转mobi已完成", transformType, needBakFileSet);	
		tfis.save2db();
	}
	
	public void pdf2mobi_byabbyy_test() throws IOException, InterruptedException, MessagingException {
		pdf2mobi_byabbyy_test(new File(Prop.get("srcdirpath")), new File(Prop.get("dstdirpath")));
	}
	
	// 执行单个测试计划
	private void execSingleSelftestRule(SelftestRule rule) throws IOException, InterruptedException, MessagingException{
		// 如果规则无效，则直接退出
		if(rule==null || !rule.isValid()) return;
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
			} catch (Exception e) {		// 单次执行出现问题不影响其他测试
				e.printStackTrace();
			}
		}
		selftest = false;
	}
	
	public void execTransform(String[] args) throws IOException, InterruptedException, MessagingException {
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
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException, MessagingException {
		DocFormatConverter dfc = new DocFormatConverter();
		if(Prop.getBool("debug")) {		// 调试模式
			//dfc.img2word();
			//AbbyyUtils.openPdf(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\装配式建筑施工安全评价体系研究_杨爽.pdf");
			dfc.selftest();
		}else {
			dfc.execTransform(args);
		}
	}
}
