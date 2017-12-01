package com.oddrock.caj2pdf.bean;

import java.io.File;
import java.io.IOException;

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
	@Override
	public String toString() {
		String result = null;
		try {
			result = "TransformFileSet [srcFile=" + srcFile.getCanonicalPath() + ", dstFile=" + dstFile.getCanonicalPath()+ "]";
		}catch (IOException e) {
			e.printStackTrace();
			result = "TransformFileSet [srcFile=" + srcFile + ", dstFile=" + dstFile+ "]";
		}
		return result;
	}
}
