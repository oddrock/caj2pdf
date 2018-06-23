package com.oddrock.caj2pdf.biz;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.bean.TransformFileSetEx;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformPdfEncryptException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.CalibreUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.pdf.PdfManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Pdf2MobiUtils {
	private static Logger logger = Logger.getLogger(Pdf2MobiUtils.class);
	
	public static void pdf2mobiByCalibre_step1_1(RobotManager robotMngr, File srcFile) throws IOException, InterruptedException, TransformWaitTimeoutException {
		// 用calibre打开一本书，并且保证只打开这一本书(即calibre里只有这本书)，如果有别的书就删除
		CalibreUtils.openSingleBook(robotMngr, srcFile);	
		Common.waitShort();
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		Common.waitM();
		// 打开右键菜单
		robotMngr.clickMouseRight();
		Common.waitShort();
		// 连按5次tab键，移动到“转换书籍”菜单
		for(int i=0; i<5; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			Common.waitShort();
		}
		// 移动到逐个转换
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		Common.waitShort();
		// 确认“逐个转换”
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
	}
	
	public static void pdf2mobiByCalibre_step1(RobotManager robotMngr, File srcFile) throws IOException, InterruptedException, TransformWaitTimeoutException {
		pdf2mobiByCalibre_step1_1(robotMngr, srcFile);
		// 等待直到转换页面打开
		try {
			CalibreUtils.waitTransformPageOpen(robotMngr);
		} catch (TransformWaitTimeoutException e) {
			e.printStackTrace();
			pdf2mobiByCalibre_step1_1(robotMngr, srcFile);
			CalibreUtils.waitTransformPageOpen(robotMngr);
		}
		// 鼠标移动到“输出格式”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.outformat.x"), 
				Prop.getInt("calibre.coordinate.outformat.y"));
		Common.waitM();
		robotMngr.clickMouseLeft();
		Common.waitShort();
		// 选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		Common.waitShort();
		// 确定选择“mobi”格式
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 开始转换
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitM();
	}
	
	// 用calibre实现pdf转mobi
	public static TransformFileSet pdf2mobiByCalibre(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException {
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File srcFile = new File(pdfFilePath);
		if(!Common.isFileExists(srcFile, "pdf")) {
			logger.warn("文件不存在或后缀名不对："+pdfFilePath);
			return result;
		}
		result.setSrcFile(srcFile);
		pdf2mobiByCalibre_step1(robotMngr, srcFile);
		// 等待直到打开转换任务页面
		try {
			CalibreUtils.openAndWaitTransformTaskPage(robotMngr);
		} catch (TransformWaitTimeoutException e) {
			e.printStackTrace();
			pdf2mobiByCalibre_step1(robotMngr, srcFile);
			CalibreUtils.openAndWaitTransformTaskPage(robotMngr);
		}
		Common.waitM();
		// 等待直到转换任务完成
		CalibreUtils.waitTransformTaskEnd(robotMngr);
		Common.waitM();
		CalibreUtils.closeAndWaitTransformTaskPage(robotMngr);
		Common.waitM();
		// 鼠标移动到第一本书上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.firstbook.x"), 
				Prop.getInt("calibre.coordinate.firstbook.y"));
		Common.waitM();
		// 打开右键菜单
		robotMngr.clickMouseRight();
		Common.waitShort();
		// 连按3次tab键，移动到“保存到磁盘”菜单
		for(int i=0; i<3; i++) {
			robotMngr.pressKey(KeyEvent.VK_TAB);
			Common.waitShort();
		}
		// 移动打“保存到磁盘单个目录”
		robotMngr.pressKey(KeyEvent.VK_RIGHT);
		Common.waitShort();
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		Common.waitShort();
		// 确认
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 等待直到“选择目标目录”页面被打开
		CalibreUtils.waitSelectTargetDirOpen(robotMngr);
		File dstDir = new File(Prop.get("calibre.tmpoutputdir"));
		dstDir.mkdirs();
		// 将生成的目标文件夹地址复制到剪贴板
		ClipboardUtils.setSysClipboardText(dstDir.getCanonicalPath());
		Common.waitShort();
		// 将剪贴板里的文件路径复制到输入框
		robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		Common.waitM();
		// 确认目录
		robotMngr.pressKey(KeyEvent.VK_ENTER);
		Common.waitShort();
		// 确认保存
		//robotMngr.pressKey(KeyEvent.VK_ENTER);
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.selecttargetdir.selectdirbutton.x"), 
				Prop.getInt("calibre.coordinate.selecttargetdir.selectdirbutton.y"));
		Common.waitM();
		robotMngr.clickMouseLeft();
		Common.waitShort();
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		Common.waitM();
		// 等待直到“选择目标目录”页面被关闭
		CalibreUtils.waitSelectTargetDirClose(robotMngr);
		Common.waitLong();
		File dstFile = null;
		// 遍历目标文件夹，删除mobi以外文件，并将mobi文件改名(因为默认的是拼音名字)
		for(File file : dstDir.listFiles()) {
			System.out.println(file.getCanonicalPath());
			if(!file.getCanonicalPath().toLowerCase().endsWith(".mobi")) {
				file.delete();
			}else {
				dstFile = new File(srcFile.getCanonicalPath().replaceAll(".pdf$", "")+".mobi");
				// 删除同名文件
				dstFile.delete();
				// 将得到的mobi改名，并移动到源文件所在目录
				file.renameTo(dstFile);
			}	
		}
		result.setDstFile(dstFile);
		// 关闭calibre
		CalibreUtils.close();
		return result;
	}
	
	public static void pdf2mobi_bycalibre_batch(TransformInfoStater tfis) throws TransformNofileException, IOException, TransformWaitTimeoutException, InterruptedException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException("目录里没有pdf文件");
		}
		TransformFileSet fileSet;
		RobotManager robotMngr = tfis.getRobotMngr();
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}	
	}
	
	public static void pdf2mobi_bycalibre_test(TransformInfoStater tfis) throws IOException, TransformNofileException, InterruptedException, TransformWaitTimeoutException, TransformPdfEncryptException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException("目录里没有pdf文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(pdfFile==null) {
				pdfFile = file;
			}else {
				if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
					pdfFile = file;
				}
			}
		}
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为word
		fileSet = Pdf2MobiUtils.pdf2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
	}
	
	public static void pdf2mobi_byabbyy_batch(TransformInfoStater tfis) throws IOException, InterruptedException, TransformNofileException, TransformWaitTimeoutException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException();
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		TransformFileSet fileSet;
		// 先全部pdf转epub
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(file==null) continue;
			fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
			tfis.addSrcFile(fileSet.getSrcFile());
			tfis.addMidFile(fileSet.getDstFile());
		}
		// 再全部epub转mobi
		for(File file : tfis.getMidFileSet()){
			if(file==null) continue;
			fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
		}
	}
	
	public static TransformFileSetEx pdf2mobi_byabbyy_single(File file, RobotManager robotMngr) throws IOException, TransformWaitTimeoutException, InterruptedException  {
		logger.warn("开始转换:"+file.getCanonicalPath());
		TransformFileSetEx result = new TransformFileSetEx();
		if(file==null || !file.exists() || !file.isFile()) {
			logger.warn("文件不存在或文件为空");
			return result;
		}
		File srcFile = TransformRuleUtils.isQualifiedSrcFile(file, "pdf2mobi_byabbyy");
		if(srcFile==null) {
			logger.warn("文件不是要转换的类型："+file.getCanonicalPath());
			return result;
		}
		result.addSrcFile(srcFile);
		TransformFileSet tmpFileSet = Pdf2EpubUtils.pdf2epub(robotMngr, file.getCanonicalPath());
		if(tmpFileSet.getDstFile()!=null) {
			tmpFileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, tmpFileSet.getDstFile().getCanonicalPath());
			if(tmpFileSet!=null && tmpFileSet.getDstFile()!=null) {
				result.addDstFile(tmpFileSet.getDstFile());
				result.setSuccess(true);
			}
		}
		logger.warn("结束转换:"+file.getCanonicalPath());
		return result;
	}
	
	public static void pdf2mobi_byabbyy_test(TransformInfoStater tfis) throws IOException, TransformNofileException, InterruptedException, TransformWaitTimeoutException, TransformPdfEncryptException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有pdf文件");
			throw new TransformNofileException();
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 存放每次单次转换后的源文件和目标文件
		TransformFileSet fileSet = null;
		File pdfFile = null;
		PdfManager pm = new PdfManager();
		// 找到目录下页数最多那个pdf
		for(File file : tfis.getQualifiedSrcFileSet()){
			if(pdfFile==null) {
				pdfFile = file;
			}else {
				if(pm.pdfPageCount(file.getCanonicalPath())>pm.pdfPageCount(pdfFile.getCanonicalPath())) {
					pdfFile = file;	
				}
			}
		}
		if(pdfFile==null) return;
		tfis.addSrcFile(pdfFile);
		// 获得转换得到的pdf的实际页数
		int realPageCount = pm.pdfPageCount(pdfFile.getCanonicalPath());
		// 计算出应该提取的页数
		int tiquPageCount = TransformRuleUtils.computeTestPageCount(realPageCount, tfis.getInfo().getTransform_type());
		// 从已转换的pdf中提取相应页数，另存为新的pdf，新的pdf名为在已有PDF名称前加上“提取页面 ”
		fileSet = PdfUtils.extractPage(robotMngr, pdfFile.getCanonicalPath(), tiquPageCount);
		// 将提取后的页面转为epub
		fileSet = Pdf2EpubUtils.pdf2epub(robotMngr, fileSet.getDstFile().getCanonicalPath());
		fileSet = Epub2MobiUtils.epub2mobiByCalibre(robotMngr, fileSet.getDstFile().getCanonicalPath());
		tfis.addMidFile(fileSet.getSrcFile());
		tfis.addDstFile(fileSet.getDstFile());
	}

	public static void main(String[] args) throws AWTException, IOException, InterruptedException, TransformWaitTimeoutException {
		String filePath = "C:\\Users\\qzfeng\\Desktop\\cajwait\\ZX粮油食品有限公司人力资源管理研究_何微.pdf";
		RobotManager robotMngr = new RobotManager();
		pdf2mobiByCalibre(robotMngr, filePath);
	}

}
