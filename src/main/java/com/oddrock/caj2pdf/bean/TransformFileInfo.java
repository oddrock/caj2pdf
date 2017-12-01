package com.oddrock.caj2pdf.bean;

public class TransformFileInfo {
	private String file_type;
	private String file_name;
	private long file_size;
	public String getFile_type() {
		return file_type;
	}
	public void setFile_type(String file_type) {
		this.file_type = file_type;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public long getFile_size() {
		return file_size;
	}
	public void setFile_size(long file_size) {
		this.file_size = file_size;
	}
	@Override
	public String toString() {
		return "TransformFileInfo [file_type=" + file_type + ", file_name="
				+ file_name + ", file_size=" + file_size + "]";
	}
}
