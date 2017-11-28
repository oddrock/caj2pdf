package com.oddrock.caj2pdf.utils;

public class TransformRuleUtils {
	/**
	 * 计算试转应该提取多少页
	 * @param realPageCount
	 * @return
	 */
	public static int computeTestPageCount(int realPageCount) {
		int testcount = Prop.getInt("test.pagecount");
		int testcountmin = Prop.getInt("test.minpagecount");
		if(realPageCount>=testcount*2) {
			return testcount;
		}else if(realPageCount>=testcountmin*2) {
			return testcountmin;
		}else if(realPageCount>=3) {
			return 2;
		}else {
			return 1;
		}
	}
	
	/**
	 * 计算试转应该提取多少Byte文字
	 * @param txtFileSize 	单位Byte
	 * @return  			单位Byte
	 */
	public static long computeTestTxtSize(long txtFileSize) {
		long testfilesize = Prop.getLong("test.filesize");
		long testfilesizemin = Prop.getLong("test.minfilesize");
		if(txtFileSize>=testfilesize*1024*2) {
			return testfilesize*1024;
		}else if(txtFileSize>=testfilesizemin*1024*2) {
			return testfilesizemin*1024;
		}else if(txtFileSize>=3*1024) {
			return 2*1024;
		}else {
			return 1*1024;
		}
	}
}
