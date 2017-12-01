package com.oddrock.caj2pdf.selftest;

public class SelftestRule {
	private String transformType;	// 测试的转换类型
	private int fileCount;			// 单次测试的文件个数
	private int testCount;			// 重复测试次数
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public String getTransformType() {
		return transformType;
	}
	public void setTransformType(String transformType) {
		this.transformType = transformType;
	}
	public int getTestCount() {
		return testCount;
	}
	public void setTestCount(int testCount) {
		this.testCount = testCount;
	}
	public SelftestRule() {
		super();
	}
	public SelftestRule(String transformType, int fileCount, int testCount) {
		super();
		this.transformType = transformType;
		this.fileCount = fileCount;
		this.testCount = testCount;
	}
}
