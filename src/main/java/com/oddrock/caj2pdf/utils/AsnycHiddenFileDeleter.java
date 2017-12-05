package com.oddrock.caj2pdf.utils;

import java.io.File;

import com.oddrock.common.file.FileUtils;

public class AsnycHiddenFileDeleter implements Runnable{
	private File dir;
	public AsnycHiddenFileDeleter(File dir) {
		super();
		this.dir = dir;
	}
	public void run() {
		FileUtils.deleteHiddenFiles(dir);
	}
	public static void delete(File dir){
		new Thread(new AsnycHiddenFileDeleter(dir)).start();;
	}
}
