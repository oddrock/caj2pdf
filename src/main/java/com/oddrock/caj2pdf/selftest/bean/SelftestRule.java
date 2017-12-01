package com.oddrock.caj2pdf.selftest.bean;

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
	// 检验规则是否有效
	public boolean isValid(){
		// 转换类型为空无效
		if(transformType==null || transformType.trim().length()==0){
			return false;
		}
		// 测试文件数小于等于0或者测试次数小于等于0无效
		if(fileCount<=0 || testCount<=0){
			return false;
		}
		return true;
	}
	public SelftestRule(String transformType, int fileCount, int testCount) {
		super();
		this.transformType = transformType;
		this.fileCount = fileCount;
		this.testCount = testCount;
	}
	@Override
	public String toString() {
		return "SelftestRule [transformType=" + transformType + ", fileCount="
				+ fileCount + ", testCount=" + testCount + "]，是否有效：" + isValid();
	}
}
