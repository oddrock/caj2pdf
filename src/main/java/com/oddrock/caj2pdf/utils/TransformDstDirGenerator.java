package com.oddrock.caj2pdf.utils;

import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.persist.TransformInfoStater;

public interface TransformDstDirGenerator {
	public File generate(TransformInfoStater tfis) throws IOException;
}
