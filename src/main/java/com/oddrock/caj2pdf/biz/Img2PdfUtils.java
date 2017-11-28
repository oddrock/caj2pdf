package com.oddrock.caj2pdf.biz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.common.file.FileUtils;

/**
 * 图片转pdf
 * 
 * @author qzfeng
 *
 */
public class Img2PdfUtils {
	private static Logger logger = Logger.getLogger(Img2PdfUtils.class);
	
	// 图片转pdf，直接保存在源文件所在目录
	public static TransformFileSet img2pdf(String imgFilePath) throws IOException {
		File imgFile = new File(imgFilePath);
		String fileNameSuffix = FileUtils.getFileNameSuffix(imgFile.getName());
		File dstFile = new File(imgFile.getCanonicalPath().replaceAll("."+fileNameSuffix+"$", ".pdf"));
		return img2pdf(imgFile.getCanonicalPath(), dstFile.getCanonicalPath());
	}
	
	// 图片转pdf
	public static TransformFileSet img2pdf_zanshibuyong(String imgFilePath, String pdfFilePath) throws IOException {
		File file = new File(imgFilePath);
		TransformFileSet  result = new TransformFileSet();
		if(!Common.isImgFile(file)) {
			logger.warn("不是图片："+imgFilePath);
			return result;
		}
		result.setSrcFile(file);
		result.setDstFile(new File(pdfFilePath));
		Document document = new Document();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pdfFilePath);
			PdfWriter.getInstance(document, fos);
			document.setPageSize(PageSize.A4);
			document.open();
			Image image = Image.getInstance(imgFilePath);
			float imageHeight = image.getScaledHeight();
			float imageWidth = image.getScaledWidth();
			int i = 0;
			while (imageHeight > 500 || imageWidth > 500) {
				image.scalePercent(100 - i);
				i++;
				imageHeight = image.getScaledHeight();
				imageWidth = image.getScaledWidth();
			}
			image.setAlignment(Image.ALIGN_CENTER);
			document.add(image);
		} catch (DocumentException de) {
			logger.warn(de.getMessage());
		} catch (IOException ioe) {
			logger.warn(ioe.getMessage());
		}finally {
			document.close();
			fos.flush();
			fos.close();
		}
		return result;
	}
	
	public static TransformFileSet img2pdf(String imgFilePath, String pdfFilePath) throws IOException {
		File file = new File(imgFilePath);
		TransformFileSet  result = new TransformFileSet();
		if(!Common.isImgFile(file)) {
			logger.warn("不是图片："+imgFilePath);
			return result;
		}
		Document doc = null;
		FileOutputStream fos = null;
		try {
			doc = new Document();
			fos = new FileOutputStream(pdfFilePath);
			PdfWriter.getInstance(doc, fos);
			doc.open();
			Image jpg1 = Image.getInstance(imgFilePath);
			float heigth = jpg1.getHeight();
			float width = jpg1.getWidth();
			int percent = getPercent2(heigth, width);
			jpg1.setAlignment(Image.ALIGN_CENTER);
			jpg1.scalePercent(percent);
			doc.add(jpg1);
		} catch (DocumentException e) {
			e.printStackTrace();
		}finally {
			if(doc!=null) doc.close();
			if(fos!=null) {
				fos.flush();
				fos.close();
			}
		}
		result.setSrcFile(file);
		result.setDstFile(new File(pdfFilePath));
		return result;
	}

	private static int getPercent2(float h, float w) {
		int p = 0;
		float p2 = 0.0f;
		p2 = 530 / w * 100;
		p = Math.round(p2);
		return p;
	}

	public static void main(String[] args) throws IOException {
		img2pdf("C:\\Users\\qzfeng\\Desktop\\cajwait\\1.jpg");
	}
}
