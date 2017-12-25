package com.oddrock.caj2pdf.bean;

import java.util.HashMap;
import java.util.Map;

public class DiffernetBatchDocCount {
	private Map<String, BatchDocCount> mapper;
	public DiffernetBatchDocCount() {
		super();
		mapper = new HashMap<String, BatchDocCount>();
	}
	public void add(SingleDocCount singleDocCount) {
		BatchDocCount batchDocCount = null;
		String fileType = singleDocCount.getFileType();
		if(mapper.containsKey(fileType)) {
			batchDocCount = mapper.get(fileType);
		}else {
			batchDocCount =  new BatchDocCount(fileType);
			mapper.put(fileType, batchDocCount);
		}
		batchDocCount.add(singleDocCount);
	}
	// 统计所有文档
	public String stat() {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		for(BatchDocCount batchDocCount:mapper.values()) {
			if(first) {
				first = false;
			}else {
				result.append("。");
			}
			result.append(batchDocCount.stat());
		}
		return result.toString();
	}
}
