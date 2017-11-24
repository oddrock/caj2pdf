package com.oddrock.caj2pdf;

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
}
