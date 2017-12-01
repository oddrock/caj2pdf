package com.oddrock.caj2pdf.persist;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.oddrock.caj2pdf.main.DocFormatConverter;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.file.FileUtils;

public class DocBakUtils {

	public static void bakDoc(String transformType, Set<File> needBakFileSet) throws IOException {
		// 如果不需要备份文档，或者在自测时也不需要备份文档， 则立即返回
		if(!Prop.getBool("docbak.need") || DocFormatConverter.selftest) return;
		// 备份文档目录为基础目录+转换类型
		File dstDir = new File(Prop.get("docbak.basedirpath"), transformType);
		dstDir.mkdirs();
		dstDir.mkdir();
		for(File file : needBakFileSet) {
			if(file==null) continue;
			FileUtils.copyFile(file.getCanonicalPath(), new File(dstDir, file.getName()).getCanonicalPath());
		}
	}
}
