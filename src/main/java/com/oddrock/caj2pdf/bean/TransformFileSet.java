package com.oddrock.caj2pdf.bean;

import java.io.File;

/**
 * 存放转换文件的集合
 * @author qzfeng
 *
 */
public class TransformFileSet {
	private File srcFile;		// 源文件（转换前的文件）
	private File dstFile;		// 目标文件（转换后的文件）
	public File getSrcFile() {
		return srcFile;
	}
	public void setSrcFile(File srcFile) {
		this.srcFile = srcFile;
	}
	public File getDstFile() {
		return dstFile;
	}
	public void setDstFile(File dstFile) {
		this.dstFile = dstFile;
	}
}
