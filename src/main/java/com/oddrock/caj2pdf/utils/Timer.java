package com.oddrock.caj2pdf.utils;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;

public class Timer {
	private Long startTime;
	private Long endTime;
	public Long getStartTime() {
		return this.startTime;
	}
	public Long getEndTime() {
		return this.endTime;
	}
	// 开始计时
	public Timer start() {
		this.startTime = System.currentTimeMillis();
		return this;
	}
	// 结束计时
	public Timer end() {
		this.endTime = System.currentTimeMillis();
		return this;
	}
	// 重置
	public void reset() {
		this.startTime = System.currentTimeMillis();
		this.endTime = -1L;
	}
	// 获得当前已花费时间，单位是毫秒
	public Long getSpentTimeMillis() {
		if(this.startTime<0) {
			return -1L;
		}else if(this.endTime>0) {
			return this.endTime-this.startTime;
		}else {
			return System.currentTimeMillis()-this.startTime;
		}
	}
	public Timer() {
		super();
		this.startTime = -1L;
		this.endTime = -1L;
	}
	// 检查是否超时
	public void checkTimeout(long timeoutMillis, String warnLogContent, Logger logger) throws TransformWaitTimeoutException{
		if(getSpentTimeMillis()>timeoutMillis) {
			logger.warn("等待"+warnLogContent+"时间过长，已达到："+getSpentTimeMillis()/1000L+"秒");
			throw new TransformWaitTimeoutException();
		}
	}
}