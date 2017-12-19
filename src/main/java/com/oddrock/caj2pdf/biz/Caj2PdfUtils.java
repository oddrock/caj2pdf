package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.CajViewerUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.FoxitUtils;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Caj2PdfUtils {
	public static void caj2pdf_step1(RobotManager robotMngr, String cajFilePath) throws IOException, InterruptedException {
		// 关闭cajviewer
		CajViewerUtils.close();
		// 关闭foxit
		FoxitUtils.close();
		// 用cajviewer打开指定caj文件
		CajViewerUtils.openCaj(robotMngr, cajFilePath);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 打开“打印机”菜单
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_P);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打印机打开
		CajViewerUtils.waitPrinterOpen(robotMngr);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击“确定”开始打印
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_O);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 等待直到打印完毕
		CajViewerUtils.waitPrintFinish(robotMngr);
		Common.wait(Prop.getInt("interval.waitminmillis"));
	}
	

	
	public static TransformFileSet caj2pdf(RobotManager robotMngr, String cajFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException{
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File cajFile = new File(cajFilePath);
		if(!cajFile.exists() || !cajFile.isFile() || !cajFile.getCanonicalPath().endsWith(".caj")) {
			return result;
		}
		result.setSrcFile(cajFile);
		caj2pdf_step1(robotMngr, cajFilePath);
		// 等待直到输入文件名
		try {
			CajViewerUtils.waitInputfilename(robotMngr);
		} catch (TransformWaitTimeoutException e) {
			e.printStackTrace();
			caj2pdf_step1(robotMngr, cajFilePath);
			CajViewerUtils.waitInputfilename(robotMngr);
		}
		// pdf文件生成在原地，只修改后缀
		File pdfFile = new File(cajFile.getParent(), cajFile.getName().replaceAll(".caj$", ".pdf"));
		System.out.println(pdfFile.getName());
		result.setDstFile(pdfFile);
		// 将生成的pdf文件名复制到文本框
		ClipboardUtils.setSysClipboardText(pdfFile.getCanonicalPath());
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 全选输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 将剪贴板里的文件路径复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确定按钮
		robotMngr.pressCombinationKey(KeyEvent.VK_ALT, KeyEvent.VK_S);
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 点击确定覆盖按钮
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.wait(Prop.getInt("interval.waitlongmillis"));
		// 关闭cajviewer
		CajViewerUtils.close();
		Common.wait(Prop.getInt("interval.waitminmillis"));
		// 关闭foxit
		FoxitUtils.close();
		return result;
	}
	
	public static void caj2pdf_batch(TransformInfoStater tfis) throws TransformWaitTimeoutException, IOException, InterruptedException, TransformNofileException {
		TransformFileSet fileSet;
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有caj文件");
			throw new TransformNofileException("目录里没有caj文件");
		}
		for(File file : tfis.getQualifiedSrcFileSet()){
			File pdfFile = new File(file.getParent(), file.getName().replaceAll(".caj$", ".pdf"));
			// 如果pdf文件已经存在，则不必再转换，跳过这一步
			if(pdfFile.exists()) {
				fileSet = new TransformFileSet();
				fileSet.setSrcFile(file);
				fileSet.setDstFile(pdfFile);
			}else {
				fileSet = caj2pdf(tfis.getRobotMngr(), file.getCanonicalPath());
			}
			if(fileSet.getSrcFile()!=null) {
				tfis.addSrcFile(fileSet.getSrcFile());
			}
			if(fileSet.getDstFile()!=null) {
				tfis.addDstFile(fileSet.getDstFile());
			}
		}
	}
	
	public static void caj2pdf_test(TransformInfoStater tfis) throws TransformWaitTimeoutException, IOException, InterruptedException, TransformNofileException {
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet = null;
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有caj文件");
			throw new TransformNofileException("目录里没有caj文件");
		}
		// 找到目录下第一个caj，并转换为pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			fileSet = caj2pdf(robotMngr, file.getCanonicalPath());
			break;
		}	
		if(fileSet.getSrcFile()!=null) {
			tfis.addSrcFile(fileSet.getSrcFile());
		}
		if(fileSet.getDstFile()!=null) {
			tfis.addMidFile(fileSet.getDstFile());
		}
		// 获得转换得到的pdf的实际页数
		int realPageCount = new PdfManager().pdfPageCount(fileSet.getDstFile().getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, fileSet.getDstFile().getCanonicalPath(), tiquPageCount);
		if(fileSet.getDstFile()!=null) {
			tfis.addDstFile(fileSet.getDstFile());
		}
	}
	
	public static void main(String[] args) throws AWTException, IOException, InterruptedException, TransformWaitTimeoutException {
		caj2pdf(new RobotManager(), "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.caj");
	}
}
