package com.oddrock.caj2pdf.utils;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import com.oddrock.common.awt.RobotManager;

/**
 * Pdf2Word转换工具类
 * @author qzfeng
 *
 */
public class Pdf2WordUtils {
	
	/**
	 * 单个pdf转word
	 * @param pdfFilePath
	 * @return
	 * @throws IOException 
	 */
	public static File pdf2word(RobotManager robotMngr, String pdfFilePath) throws IOException {
		File wordFile = null;
		while(true) {
			if(AbbyyUtils.isHomePage(robotMngr)) {
				break;
			}
		}
		
		return wordFile;
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException {
		RobotManager robotMngr = new RobotManager();
		AbbyyUtils.open();
		//AbbyyUtils.open();
	}
}
