package com.oddrock.caj2pdf.selftest;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

public class SelftestRuleUtils {
	public static void main(String[] args) {
		List<SelftestRule> rules=new  ArrayList<SelftestRule>();  
		rules.add(new SelftestRule("caj2word", 10 ,3));
		rules.add(new SelftestRule("caj2word", 3 ,2));
		rules.add(new SelftestRule("pdf2word", 2 ,3));
		JSONArray result = JSONArray.fromObject(rules);  
        System.out.println(result);
	}
}
