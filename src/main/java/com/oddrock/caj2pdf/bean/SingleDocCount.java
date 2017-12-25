package com.oddrock.caj2pdf.bean;

public class SingleDocCount {
	private String fileType;
	private int pageCount;
	private long fileSize;		// 单位是Byte
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public SingleDocCount() {
		super();
	}
	public SingleDocCount(String fileType) {
		this();
		this.fileType = fileType;
	}
	@Override
	public String toString() {
		return "DocTransformDifficulty [pageCount=" + pageCount + ", fileSize=" + fileSize + "]";
	}
}
