package com.oddrock.caj2pdf.biz;

import java.io.File;
import java.io.IOException;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;

public class Caj2WordUtils {
	public static void caj2word_batch(TransformInfoStater tfis) throws TransformWaitTimeoutException, IOException, InterruptedException, TransformNofileException {
		TransformFileSet fileSet;
		RobotManager robotMngr = tfis.getRobotMngr();
		if(!tfis.hasFileToTransform()) {
			throw new TransformNofileException();
		}
		// 先全部caj转pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			fileSet = Caj2PdfUtils.caj2pdf(robotMngr, file.getCanonicalPath());
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
	
	public static void caj2word_test(TransformInfoStater tfis) throws TransformNofileException, IOException, TransformWaitTimeoutException, InterruptedException {
		if(!tfis.hasFileToTransform()) {
			throw new TransformNofileException();
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
