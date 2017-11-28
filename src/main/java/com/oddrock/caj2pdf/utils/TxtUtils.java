package com.oddrock.caj2pdf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.oddrock.common.file.FileUtils;

/**
 * 将txt文件切割为较小的文件，给予其规范命名，并且防止切割导致中文乱码
 * @author qzfeng
 *
 */
public class TxtUtils {
	private static Logger logger = Logger.getLogger(TxtUtils.class);
	
	/**
	 * 将txt文件切割为最大不超过maxsizeKB大小的txt文件
	 * @param srcFile	
	 * @param maxSize	单位为KB
	 * @return
	 * @throws IOException
	 */
	public static Set<File> split(File srcFile, long maxSize) throws IOException{
		Set<File> result = new HashSet<File>();
		if(!Common.isFileExists(srcFile, "txt")) {
			logger.warn("文件不存在或后缀名不对："+srcFile.getCanonicalPath());
			return result;
		}
		
		if(srcFile.length()<=maxSize*1024) {
			return result;
		}
		
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		String encoding = FileUtils.getEncoding(srcFile);
		String writeEncoding = encoding;
		// 检查是否需要强制使用系统指定的编码
		if(Prop.getBool("txtfile.write.usesystemencoding")) {
			writeEncoding = Prop.get("txtfile.write.encoding");
		}
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile),encoding));
			String tempString = null;
			int i = 1;
			File splittedFile = new File(srcFile.getParentFile(), srcFile.getName().replaceAll("\\.txt", "")+StringUtils.leftPad(String.valueOf(i), 2, "0")+".txt");
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString + "\n");
				if(sb.length() >= maxSize*1024) {	
					FileUtils.writeToFile(splittedFile.getCanonicalPath(), sb.toString(), false, writeEncoding);
					result.add(splittedFile);
					i++;
					splittedFile = new File(srcFile.getParentFile(), srcFile.getName().replaceAll("\\.txt", "")+StringUtils.leftPad(String.valueOf(i), 2, "0")+".txt");	
					sb = new StringBuffer();
				}
			}
			if(sb.length()>0) {
				FileUtils.writeToFile(splittedFile.getCanonicalPath(), sb.toString(), false, writeEncoding);
				result.add(splittedFile);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) reader.close();
		}
		return result;
	}
	
	public static Set<File> split(File srcFile) throws IOException{
		return split(srcFile, Prop.getLong("txtfile.maxsize"));
	}
	
	/**
	 * 将srcDir目录下的txt文件全部切割为不超过500KB大小，并且删除超过500KB大小的源文件。
	 * @param srcDir
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void splitTxtFiles(File srcDir) throws IOException, InterruptedException {
		splitTxtFiles(srcDir, true);
	}
	
	/**
	 * 将srcDir目录下的txt文件全部切割为不超过500KB大小，并且删除超过500KB大小的源文件。
	 * @param srcDir
	 * @param del			是否删除源文件
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void splitTxtFiles(File srcDir, boolean del) throws IOException, InterruptedException {
		if(!srcDir.exists() || !srcDir.isDirectory()) return;
		for(File file : srcDir.listFiles()) {
			if(!Common.isFileExists(file, "txt")) continue;
			Set<File> result = TxtUtils.split(file);
			if(del && result.size()>0) {
				FileUtils.deleteAndConfirm(file);
			}
		}
	}
	
	/**
	 * 提取txt文件的前多少KB部分
	 * @param srcTxtFile
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public static File extractFrontPart(File srcTxtFile, long size) throws IOException {
		File result = null;
		if(!Common.isFileExists(srcTxtFile, "txt")) {
			logger.warn("文件不存在或后缀名不对："+srcTxtFile.getCanonicalPath());
			return null;
		}
		if(srcTxtFile.length()<=size*1024) {
			return srcTxtFile;
		}
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		String encoding = FileUtils.getEncoding(srcTxtFile);
		String writeEncoding = encoding;
		// 检查是否需要强制使用系统指定的编码
		if(Prop.getBool("txtfile.write.usesystemencoding")) {
			writeEncoding = Prop.get("txtfile.write.encoding");
		}
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcTxtFile), encoding));
			String tempString = null;
			File extractedFile = new File(srcTxtFile.getParentFile(), "提取页面 "+srcTxtFile.getName());
			boolean hasWrite = false;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString + "\n");
				if(sb.length() >= size*1024) {	
					FileUtils.writeToFile(extractedFile.getCanonicalPath(), sb.toString(), false, writeEncoding);
					result = extractedFile;
					hasWrite = true;
					break;
				}
			}
			if(!hasWrite && sb.length()>0) {
				FileUtils.writeToFile(extractedFile.getCanonicalPath(), sb.toString(), false, writeEncoding);
				result = extractedFile;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) reader.close();
		}
		return result;
	}
	
	public static File extractFrontPart(File srcTxtFile) throws IOException {
		long size = TransformRuleUtils.computeTestTxtSize(srcTxtFile.length())/1024;
		return extractFrontPart(srcTxtFile, size);
	}
	
	/**
	 * 提取源文件夹第一个txt文件
	 * @return
	 * @throws IOException
	 */
	public static File extractFrontPart() throws IOException {
		File srcDir = new File(Prop.get("srcdirpath"));
		if(!srcDir.exists() || !srcDir.isDirectory()) return null;
		File srcTxtFile = null;
		for(File file : srcDir.listFiles()) {
			if(Common.isFileExists(file, "txt")) {
				srcTxtFile=file;
				break;
			}
		}
		if(srcTxtFile!=null) {
			return extractFrontPart(srcTxtFile);
		}else {
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		TxtUtils.extractFrontPart();
		//TxtUtils.splitTxtFiles(new File(Prop.get("srcdirpath")));
		/*File file = new File("C:\\Users\\qzfeng\\Desktop\\cajwait\\知否？知否？应是绿肥红瘦.txt");
		file.delete();*/
	}
}
