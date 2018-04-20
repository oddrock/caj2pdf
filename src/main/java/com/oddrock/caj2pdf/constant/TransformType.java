package com.oddrock.caj2pdf.constant;

public enum TransformType {
	caj2word,
	caj2pdf,
	pdf2word,
	pdf2mobi_bycalibre,
	txt2mobi,
	img2word,
	pdf2epub,
	pdf2mobi_byabbyy;
	
	public static TransformType str2type(String name) {
		if("caj转word".equalsIgnoreCase(name)) {
			return caj2word;
		}else if("pdf转word".equalsIgnoreCase(name)) {
			return pdf2word;
		}
		return null;
	}
}
