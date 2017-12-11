package com.oddrock.caj2pdf.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TransformRuleUtils {
	
	/**
	 * 计算试转应该提取多少页
	 * @param realPageCount
	 * @param transformType
	 * @return
	 */
	public static int computeTestPageCount(int realPageCount, String transformType) {
		int testcount = Prop.getInt("test.pagecount");
		if(Prop.get("test."+transformType+".pagecount")!=null 
				&& Prop.get("test."+transformType+".pagecount").length()>0) {
			testcount = Prop.getInt("test."+transformType+".pagecount");
		}
		int testcountmin = Prop.getInt("test.minpagecount");
		if(Prop.get("test."+transformType+".minpagecount")!=null 
				&& Prop.get("test."+transformType+".minpagecount").length()>0) {
			testcount = Prop.getInt("test."+transformType+".minpagecount");
		}
		if(realPageCount>=testcount*2) {
			return testcount;
		}else if(realPageCount>=testcountmin*2) {
			return testcountmin;
		}else if(realPageCount>=3) {
			return 2;
		}else {
			return 1;
		}
	}
	
	/**
	 * 计算试转应该提取多少Byte文字
	 * @param txtFileSize 	单位Byte
	 * @return  			单位Byte
	 */
	public static long computeTestTxtSize(long txtFileSize) {
		long testfilesize = Prop.getLong("test.filesize");
		long testfilesizemin = Prop.getLong("test.minfilesize");
		if(txtFileSize>=testfilesize*1024*2) {
			return testfilesize*1024;
		}else if(txtFileSize>=testfilesizemin*1024*2) {
			return testfilesizemin*1024;
		}else if(txtFileSize>=3*1024) {
			return 2*1024;
		}else {
			return 1*1024;
		}
	}
	
	// 检查是否是合格的输入文件
	public static boolean isQualifiedSrcFile(File file, String transformType) throws IOException {
		if(file==null || !file.exists() || !file.isFile()) return false;
		if(transformType==null) return false;
		transformType = transformType.trim();
		if(transformType.equalsIgnoreCase("caj2pdf") 
				|| transformType.equalsIgnoreCase("caj2pdf_test")
				|| transformType.equalsIgnoreCase("caj2word")
				|| transformType.equalsIgnoreCase("caj2word_test")) {
			if(file.getCanonicalPath().endsWith(".caj") || file.getCanonicalPath().endsWith(".nh")) {
				return true;
			}
		}
		if(transformType.equalsIgnoreCase("pdf2word") 
				|| transformType.equalsIgnoreCase("pdf2word_test")
				|| transformType.equalsIgnoreCase("pdf2epub")
				|| transformType.equalsIgnoreCase("pdf2epub_test")
				|| transformType.equalsIgnoreCase("pdf2mobi_byabbyy")
				|| transformType.equalsIgnoreCase("pdf2mobi_byabbyy_test")
				|| transformType.equalsIgnoreCase("pdf2mobi_bycalibre")
				|| transformType.equalsIgnoreCase("pdf2mobi_bycalibre_test")) {
			if(file.getCanonicalPath().endsWith(".pdf")) {
				return true;
			}
		}
		if(transformType.equalsIgnoreCase("txt2mobi") 
				|| transformType.equalsIgnoreCase("txt2mobi_test")) {
			if(file.getCanonicalPath().endsWith(".txt")) {
				return true;
			}
		}
		if(transformType.equalsIgnoreCase("img2word") 
				|| transformType.equalsIgnoreCase("img2word_test")) {
			return Common.isImgFile(file);
		}
		return false;
	}
	
	// 从目录里找出符合需要转换要求的源文件
	public static Set<File> getQualifiedSrcFileSet(File srcDir, String transformType) throws IOException{
		Set<File> fileSet = new HashSet<File>();
		if(srcDir==null || !srcDir.exists() || !srcDir.isDirectory()) return fileSet;
		for(File file : srcDir.listFiles()) {
			if(isQualifiedSrcFile(file, transformType)) {
				fileSet.add(file);
			}
		}
		return fileSet;
	}
}
