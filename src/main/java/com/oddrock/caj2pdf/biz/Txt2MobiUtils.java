package com.oddrock.caj2pdf.biz;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.bean.TransformFileSet;
import com.oddrock.caj2pdf.exception.TransformNofileException;
import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.persist.TransformInfoStater;
import com.oddrock.caj2pdf.utils.CalibreUtils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TxtUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class Txt2MobiUtils {
	private static Logger logger = Logger.getLogger(Pdf2MobiUtils.class);
	
	public static void txt2mobi_step1_1(RobotManager robotMngr, File srcFile) throws TransformWaitTimeoutException, IOException, InterruptedException {
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

	public static void txt2mobi_step1(RobotManager robotMngr, File srcFile) throws IOException, InterruptedException, TransformWaitTimeoutException {
		txt2mobi_step1_1(robotMngr, srcFile);
		try {
			// 等待直到转换页面打开
			CalibreUtils.waitTransformPageOpen(robotMngr);
		}catch(TransformWaitTimeoutException e) {
			e.printStackTrace();
			txt2mobi_step1_1(robotMngr, srcFile);
			CalibreUtils.waitTransformPageOpen(robotMngr);
		}
		
		/*// 鼠标移动到“txt input”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.transformpage.txtinput.x"), 
				Prop.getInt("calibre.coordinate.transformpage.txtinput.y"));
		Common.waitM();
		robotMngr.clickMouseLeft();
		Common.waitShort();
		// 等待txt input页面打开
		CalibreUtils.waitTransformPageTxtinputOpen(robotMngr);
		Common.waitM();
		// 鼠标移动到“formatting style”上
		robotMngr.moveMouseTo(Prop.getInt("calibre.coordinate.transformpage.formattingstyle.x"), 
				Prop.getInt("calibre.coordinate.transformpage.formattingstyle.y"));
		Common.waitM();
		robotMngr.clickMouseLeft();
		Common.waitShort();
		// 选择“plain”
		robotMngr.pressKey(KeyEvent.VK_DOWN);
		Common.waitShort();
		// 确定选择“plain”
		robotMngr.pressKey(KeyEvent.VK_ENTER);*/
		
		Common.waitM();
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
	public static TransformFileSet txt2mobi(RobotManager robotMngr, String pdfFilePath) throws IOException, InterruptedException, TransformWaitTimeoutException {
		// 移开鼠标避免挡事
		Common.moveMouseAvoidHandicap(robotMngr);
		TransformFileSet result = new TransformFileSet();
		File srcFile = new File(pdfFilePath);
		if(!Common.isFileExists(srcFile, "txt")) {
			logger.warn("文件不存在或后缀名不对："+pdfFilePath);
			return result;
		}
		result.setSrcFile(srcFile);
		txt2mobi_step1(robotMngr, srcFile);
		// 等待直到打开转换任务页面
		try {
			CalibreUtils.openAndWaitTransformTaskPage(robotMngr);
		} catch (TransformWaitTimeoutException e) {
			e.printStackTrace();
			txt2mobi_step1(robotMngr, srcFile);
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
		// 选择临时输出文件夹
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
		// 点击确认保存
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
			if(!file.getCanonicalPath().toLowerCase().endsWith(".mobi")) {
				file.delete();
			}else {
				dstFile = new File(srcFile.getCanonicalPath().replaceAll(".txt$", "")+".mobi");
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
	
	public static void txt2mobi_batch(TransformInfoStater tfis) throws TransformNofileException, IOException, InterruptedException, TransformWaitTimeoutException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有txt文件");
			throw new TransformNofileException("目录里没有txt文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		// 将srcDir目录下的txt文件全部切割为不超过500KB大小，并且删除超过500KB大小的源文件。
		TxtUtils.splitTxtFiles(tfis.getSrcDir());
		TransformFileSet fileSet;
		for(File file : tfis.getSrcDir().listFiles()){
			if(file==null || !Common.isFileExists(file, "txt")) continue;
			fileSet = Txt2MobiUtils.txt2mobi(robotMngr, file.getCanonicalPath());
			tfis.addDstFile(fileSet.getDstFile());
			tfis.addSrcFile(fileSet.getSrcFile());
		}
	}
	
	public static void txt2mobi_test(TransformInfoStater tfis) throws TransformNofileException, IOException, TransformWaitTimeoutException, InterruptedException {
		if(!tfis.hasFileToTransform()) {
			tfis.setErrorMsg("目录里没有txt文件");
			throw new TransformNofileException("目录里没有txt文件");
		}
		RobotManager robotMngr = tfis.getRobotMngr();
		File firstTxtFile = TxtUtils.getFirstTxtFile();
		File srcFile = TxtUtils.extractFrontPart(firstTxtFile);
		TransformFileSet fileSet = Txt2MobiUtils.txt2mobi(robotMngr, srcFile.getCanonicalPath());
		tfis.addDstFile(fileSet.getDstFile());
		tfis.addSrcFile(fileSet.getSrcFile());
	}
}
