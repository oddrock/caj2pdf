package com.oddrock.caj2pdf.bean;

public class BatchDocCount {
	private String fileType;
	private int pageCount;
	private long fileSize;		
	private int docCount;
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
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
	public int getDocCount() {
		return docCount;
	}
	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}
	public BatchDocCount() {
		super();
		this.pageCount=0;
		this.docCount=0;
		this.fileSize=0l;
	}
	
	public BatchDocCount(String fileType) {
		this();
		this.fileType = fileType;
	}
	@Override
	public String toString() {
		return "BatchDocCount [fileType=" + fileType + ", pageCount=" + pageCount + ", fileSize=" + fileSize
				+ ", docCount=" + docCount + "]";
	}
	public void add(SingleDocCount singleDocCount) {
		docCount++;
		this.fileSize += singleDocCount.getFileSize();
		this.pageCount += singleDocCount.getPageCount();
	}
	public String stat() {
		if(fileType.equalsIgnoreCase("pdf")) {
			return fileType+":"+docCount+"个,"+pageCount+"页";
		}else if(fileType.equalsIgnoreCase("txt") || fileType.equalsIgnoreCase("doc")|| fileType.equalsIgnoreCase("docx")) {
			return fileType+":"+docCount+"个,"+fileSize/1024+"KB";
		}else {
			if(pageCount!=0 && fileSize!=0) {
				return fileType+":"+docCount+"个,"+pageCount+"页"+fileSize/1024+"KB";
			}else if(pageCount!=0) {
				return fileType+":"+docCount+"个,"+pageCount+"页";
			}
			else if(pageCount!=0) {
				return fileType+":"+docCount+"个,"+fileSize/1024+"KB";
			}else {
				return fileType+":"+docCount+"个,"+pageCount+"页"+fileSize/1024+"KB";
			}
		}
		
	}
}
