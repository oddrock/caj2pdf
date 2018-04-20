package com.oddrock.caj2pdf.bean;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TransformFileSetEx {
	private Set<File> srcFile;		// 源文件
	private Set<File> midFile;		// 中间过程文件
	private Set<File> dstFile;		// 目标文件
	private boolean success;
	public TransformFileSetEx() {
		srcFile = new HashSet<File>();
		midFile = new HashSet<File>();
		dstFile = new HashSet<File>();
		success = false;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void addSrcFile(File file) {
		srcFile.add(file);
	}
	public void addMidFile(File file) {
		midFile.add(file);
	}
	public void addDstFile(File file) {
		dstFile.add(file);
	}
	public Set<File> getSrcFile() {
		return srcFile;
	}
	public Set<File> getMidFile() {
		return midFile;
	}
	public Set<File> getDstFile() {
		return dstFile;
	}
}
