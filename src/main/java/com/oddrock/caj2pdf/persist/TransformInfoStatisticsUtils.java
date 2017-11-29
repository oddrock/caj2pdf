package com.oddrock.caj2pdf.persist;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import com.oddrock.caj2pdf.bean.TransformFileInfo;
import com.oddrock.caj2pdf.bean.TransformInfo;
import com.oddrock.common.file.FileUtils;

public class TransformInfoStatisticsUtils {
	// 统计源文件信息
	public static void statisticsSrcFileInfo(TransformInfo info, Set<File> srcFileSet) throws IOException{
		for(File file : srcFileSet){
			TransformFileInfo fileInfo = fileInfo(file);
			info.setSrc_file_type(fileInfo.getFile_type());
			info.setSrc_file_count(info.getSrc_file_count()+1);
			String file_names = info.getSrc_file_names();
			if(file_names==null){
				file_names = fileInfo.getFile_name();
			}else{
				file_names = file_names + " ||| " + fileInfo.getFile_name();
			}
			info.setSrc_file_names(file_names);
			info.setSrc_file_size(info.getSrc_file_size()+fileInfo.getFile_size());
		}
	}
	
	// 统计目标文件信息
	public static void statisticsDstFileInfo(TransformInfo info, Set<File> srcFileSet) throws IOException{
		for(File file : srcFileSet){
			TransformFileInfo fileInfo = fileInfo(file);
			info.setDst_file_type(fileInfo.getFile_type());
			info.setDst_file_count(info.getDst_file_count()+1);
			String file_names = info.getDst_file_names();
			if(file_names==null){
				file_names = fileInfo.getFile_name();
			}else{
				file_names = file_names + " ||| " + fileInfo.getFile_name();
			}
			info.setDst_file_names(file_names);
			info.setDst_file_size(info.getDst_file_size()+fileInfo.getFile_size());
		}
	}
	
	// 从文件中获取信息
	private static TransformFileInfo fileInfo(File file) throws IOException{
		TransformFileInfo fileInfo = new TransformFileInfo();
		fileInfo.setFile_name(file.getName());
		fileInfo.setFile_size(file.length());
		String suffix = FileUtils.getFileNameSuffix(file.getName());
		fileInfo.setFile_type(suffix);
		return fileInfo;
	}
}
