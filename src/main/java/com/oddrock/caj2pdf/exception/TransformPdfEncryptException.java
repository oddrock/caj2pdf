package com.oddrock.caj2pdf.exception;

// PDF被加密
public class TransformPdfEncryptException extends TransformException {
	private static final long serialVersionUID = 1L;
	public TransformPdfEncryptException() {}  
	public TransformPdfEncryptException(String msg) {  
	    super(msg);  
	}
}
