package com.oddrock.caj2pdf.biz;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.bean.TransformFileSetEx;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformPdfEncryptException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;

public class Caj2WordUtils {
	private static Logger logger = Logger.getLogger(Caj2WordUtils.class);
	
	public static TransformFileSetEx caj2word_single(File file, RobotManager robotMngr) throws IOException, TransformWaitTimeoutException, InterruptedException {
		logger.warn("开始转换:"+file.getCanonicalPath());
		TransformFileSetEx result = new TransformFileSetEx();
		if(file==null || !file.exists() || !file.isFile()) {
			logger.warn("文件不存在或文件为空");
			return result;
		}
		File srcFile = TransformRuleUtils.isQualifiedSrcFile(file, "caj2word");
		if(srcFile==null) {
			logger.warn("文件不是要转换的类型："+file.getCanonicalPath());
			return result;
		}
		result.addSrcFile(srcFile);
		File pdfFile = null;
		TransformFileSet tmpFileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
		if(tmpFileSet!=null && tmpFileSet.getDstFile()!=null) {
			pdfFile = tmpFileSet.getDstFile();
		}
		if(pdfFile!=null && pdfFile.exists()) {
			result.addMidFile(pdfFile);
			tmpFileSet = Pdf2WordUtils.pdf2word(robotMngr, pdfFile.getCanonicalPath());
			if(tmpFileSet!=null && tmpFileSet.getDstFile()!=null) {
				result.addDstFile(tmpFileSet.getDstFile());
				result.setSuccess(true);
			}
		}
		logger.warn("结束转换:"+file.getCanonicalPath());
		return result;
	}
	
	public static void caj2word_batch(TransformInfoStater tfis) throws TransformWaitTimeoutException, IOException, InterruptedException, TransformNofileException {
		TransformFileSet fileSet;
		RobotManager robotMngr = tfis.getRobotMngr();
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有caj文件");
			throw new TransformNofileException("目录里没有caj文件");
		}
		// 先全部caj转pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			File pdfFile = null;
			if(file.getName().toLowerCase().endsWith("caj")) {
				pdfFile = new File(file.getParent(), file.getName().replaceAll("(?i).caj$", ".pdf"));
			}else if(file.getName().toLowerCase().endsWith("caa")) {
				pdfFile = new File(file.getParent(), file.getName().replaceAll("(?i).caa$", ".pdf"));
			}if(file.getName().toLowerCase().endsWith("nh")) {
				pdfFile = new File(file.getParent(), file.getName().replaceAll("(?i).nh$", ".pdf"));
			}
			// 如果pdf文件已经存在，则不必再转换，跳过这一步
			if(pdfFile!=null && pdfFile.exists()) {
				fileSet = new TransformFileSet();
				fileSet.setSrcFile(file);
				fileSet.setDstFile(pdfFile);
			}else {
				fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			}
			if(fileSet.getSrcFile()!=null) {
				tfis.addSrcFile(file);
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addMidFile(fileSet.getDstFile());
			}
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
	}
	
	public static void caj2word_test(TransformInfoStater tfis) throws TransformNofileException, IOException, TransformWaitTimeoutException, InterruptedException, TransformPdfEncryptException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有caj文件");
			throw new TransformNofileException("目录里没有caj文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet = null;
		for(File file : tfis.getQualifiedSrcFileSet()){
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
			break;
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
	}
}
