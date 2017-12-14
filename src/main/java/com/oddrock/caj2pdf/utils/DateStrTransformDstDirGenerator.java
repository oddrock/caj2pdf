package com.oddrock.caj2pdf.utils;

import java.io.File;
import java.io.IOException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.common.DateUtils;

public class DateStrTransformDstDirGenerator implements TransformDstDirGenerator {

	public File generate(TransformInfoStater tfis) throws IOException{
		return new File(tfis.getDstParentDir(), DateUtils.timeStrInChinese());
	}

}
