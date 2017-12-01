package com.oddrock.caj2pdf.selftest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.file.FileUtils;

/**
 * 自测文件池，提供测试文件
 * @author qzfeng
 *
 */
public class SelftestFilesPool {
	// 生成指定文件类型的单个测试文件
	public static File getSingleRandomTestFileByFileType(String fileType){
		File testTypeFileDir = new File(Prop.get("selftest.pool.basedirpath"), fileType);
		if(!testTypeFileDir.exists() || !testTypeFileDir.isDirectory()) {
			return null;
		}
		File[] testFiles = testTypeFileDir.listFiles();	
		int count = testFiles.length;
		int max=count-1;
        int min=0;
        Random random = new Random();
        // 获得从0到池中文件个数-1的范围内的随机整数，作为取文件的序号
        int randomIndex = random.nextInt(max)%(max-min+1) + min;  
		return testFiles[randomIndex];
	}
	
	// 生成指定数量指定文件类型的测试文件
	public static Set<File> getRandomTestFilesByFileType(String fileType, int count){
		Set<File> fileSet = new HashSet<File>();
		File testTypeFileDir = new File(Prop.get("selftest.pool.basedirpath"), fileType);
		// 如果要获得的文件数量大于池中文件总数，则将池中文件全部返回
		if(testTypeFileDir.listFiles().length<=count) {		
			fileSet.addAll(Arrays.asList(testTypeFileDir.listFiles()));
		}else {
			while(count>0) {
				File file = getSingleRandomTestFileByFileType(fileType);
				if(!fileSet.contains(file)) {
					fileSet.add(file);
					count--;
				}
			}
		}
		return fileSet;
	}
	
	// 生成指定数量指定文件类型的测试文件，拷贝到指定目录下
	public static void generateTestFilesByFileType(String type, int count, File dstDir) throws IOException {
		Set<File> fileSet = getRandomTestFilesByFileType(type, count);
		for(File file : fileSet) {
			FileUtils.copyFile(file.getCanonicalPath(), new File(dstDir, file.getName()).getCanonicalPath());
		}
	}
	
	// 在srcdirpath目录下生成指定数量指定文件类型的测试文件
	public static void generateTestFilesByFileType(String type, int count) throws IOException {
		generateTestFilesByFileType(type, count, new File(Prop.get("srcdirpath")));
	}
	
	public static void main(String[] args) throws IOException {
		/*Set<File> fileSet = getRandomTestFiles("img",10);
		System.out.println(fileSet.size());
		for(File file : fileSet) {
			System.out.println(file.getCanonicalPath());
		}*/
		generateTestFilesByFileType("img", 10);
	}
}
