package com.oddrock.caj2pdf.bean;

import java.util.Date;

public class TransformInfo {
	private String src_file_type;
	private String dst_file_type;
	private String transform_type;
	private Date start_time;
	private Date end_time;
	private int src_file_count;
	private int src_page_count;
	private int dst_file_count;
	private int dst_page_count;
	private String transform_status;
	private Date record_time;
	private String src_dirpath;
	private String src_file_names;
	private String dst_dirpath;
	private String dst_file_names;
	private String remark;
	public String getSrc_file_type() {
		return src_file_type;
	}
	public void setSrc_file_type(String src_file_type) {
		this.src_file_type = src_file_type;
	}
	public String getDst_file_type() {
		return dst_file_type;
	}
	public void setDst_file_type(String dst_file_type) {
		this.dst_file_type = dst_file_type;
	}
	public String getTransform_type() {
		return transform_type;
	}
	public void setTransform_type(String transform_type) {
		this.transform_type = transform_type;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Date getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	public int getSrc_file_count() {
		return src_file_count;
	}
	public void setSrc_file_count(int src_file_count) {
		this.src_file_count = src_file_count;
	}
	public int getSrc_page_count() {
		return src_page_count;
	}
	public void setSrc_page_count(int src_page_count) {
		this.src_page_count = src_page_count;
	}
	public int getDst_file_count() {
		return dst_file_count;
	}
	public void setDst_file_count(int dst_file_count) {
		this.dst_file_count = dst_file_count;
	}
	public int getDst_page_count() {
		return dst_page_count;
	}
	public void setDst_page_count(int dst_page_count) {
		this.dst_page_count = dst_page_count;
	}
	public String getTransform_status() {
		return transform_status;
	}
	public void setTransform_status(String transform_status) {
		this.transform_status = transform_status;
	}
	public Date getRecord_time() {
		return record_time;
	}
	public void setRecord_time(Date record_time) {
		this.record_time = record_time;
	}
	public String getSrc_dirpath() {
		return src_dirpath;
	}
	public void setSrc_dirpath(String src_dirpath) {
		this.src_dirpath = src_dirpath;
	}
	public String getSrc_file_names() {
		return src_file_names;
	}
	public void setSrc_file_names(String src_file_names) {
		this.src_file_names = src_file_names;
	}
	public String getDst_dirpath() {
		return dst_dirpath;
	}
	public void setDst_dirpath(String dst_dirpath) {
		this.dst_dirpath = dst_dirpath;
	}
	public String getDst_file_names() {
		return dst_file_names;
	}
	public void setDst_file_names(String dst_file_names) {
		this.dst_file_names = dst_file_names;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
