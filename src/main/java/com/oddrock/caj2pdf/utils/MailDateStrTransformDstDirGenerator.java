package com.oddrock.caj2pdf.utils;

import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.common.DateUtils;

public class MailDateStrTransformDstDirGenerator implements TransformDstDirGenerator {
	public File generate(TransformInfoStater tfis) throws IOException {
		File file1 = new File(tfis.getDstParentDir(), DateUtils.timeStrInChinese());
		File file2 = new File(file1, tfis.getSrcDir().getName());
		return file2;
	}
}
