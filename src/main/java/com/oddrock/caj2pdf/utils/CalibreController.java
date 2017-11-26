package com.oddrock.caj2pdf.utils;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;

import com.oddrock.common.awt.RobotManager;

public class CalibreController extends SoftwareController{
	public CalibreController() {
		super("calibre");
	}

	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		CalibreController cc = new CalibreController();
		cc.open(new RobotManager(), new File("C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf"));
	}
}
