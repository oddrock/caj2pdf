package com.oddrock.caj2pdf.biz;

import java.io.File;
import java.io.IOException;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.common.awt.RobotManager;

public class Img2WordUtils {
	public static void img2word_batch(TransformInfoStater tfis) throws IOException, TransformNofileException, InterruptedException {
		if(!tfis.hasFileToTransform()) throw new TransformNofileException();
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet;
		// 先全部caj转pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
		}
		// 再全部pdf转word
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
	}
	
	public static void img2word_test(TransformInfoStater tfis) throws IOException, InterruptedException, TransformNofileException {
		if(!tfis.hasFileToTransform()) throw new TransformNofileException();
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet;
		// 先全部caj转pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			fileSet = Img2PdfUtils.img2pdf(file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
			break;
		}
		// 再全部pdf转word
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Pdf2WordUtils.pdf2word(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
	}
}
