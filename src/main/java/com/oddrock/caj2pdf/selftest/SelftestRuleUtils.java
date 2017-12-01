package com.oddrock.caj2pdf.selftest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.selftest.bean.SelftestRule;
import com.oddrock.common.file.FileUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SelftestRuleUtils {
	private static Logger logger = Logger.getLogger(SelftestRuleUtils.class);
	
	//  获取自测规则集合
	public static List<SelftestRule> getSelftestRules() throws IOException {
		File ruleFile = new File("settings\\selftestrule.json");
		logger.warn("开始从本文件导入自测规则："+ruleFile.getCanonicalPath());
		List<SelftestRule> result = new ArrayList<SelftestRule>();
		String jsonStr = FileUtils.readFileContentToStr(ruleFile.getCanonicalPath());
		JSONArray jsonArray = new JSONArray();
		jsonArray = JSONArray.fromObject(jsonStr);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObj = (JSONObject) jsonArray.get(i);
			SelftestRule sr = (SelftestRule) JSONObject.toBean(jsonObj,SelftestRule.class);
			result.add(sr);
		}
		logger.warn("完成从本文件导入自测规则："+ruleFile.getCanonicalPath());
		return result;
	}

	public static void main(String[] args) throws IOException {
		/*
		 * List<SelftestRule> rules=new ArrayList<SelftestRule>(); rules.add(new
		 * SelftestRule("caj2word", 10 ,3)); rules.add(new
		 * SelftestRule("caj2word", 3 ,2)); rules.add(new
		 * SelftestRule("pdf2word", 2 ,3)); JSONArray result =
		 * JSONArray.fromObject(rules); System.out.println(result);
		 */
		List<SelftestRule> rules = getSelftestRules();
		for (SelftestRule sr : rules) {
			System.out.println(sr);
		}
	}
}
