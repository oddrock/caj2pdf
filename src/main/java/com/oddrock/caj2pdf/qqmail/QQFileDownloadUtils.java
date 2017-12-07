package com.oddrock.caj2pdf.qqmail;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import com.oddrock.caj2pdf.exception.TransformWaitTimeoutException;
import com.oddrock.caj2pdf.utils.Browser360Utils;
import com.oddrock.caj2pdf.utils.Common;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.ClipboardUtils;

public class QQFileDownloadUtils {	
	public static void download(RobotManager robotMngr, File dstDir, String[] downPageUrls) throws IOException, InterruptedException, TransformWaitTimeoutException{		
		Browser360Utils.close();
		Browser360Utils.open(robotMngr);
		Common.waitLong();
		if(Browser360Utils.waitHuifu(robotMngr)){
			// 如果出现恢复栏，将恢复栏关掉
			Browser360Utils.waitAndCloseHuifu(robotMngr);
		}
		// 清空所有下载项，无论是否
		Browser360Utils.clearAllDownloadNoMatterEnd(robotMngr);
		for(String downPageUrl: downPageUrls){
			Browser360Utils.open(robotMngr, downPageUrl);
			Common.waitM();
			Browser360Utils.waitQQFileDownloadButtonExists(robotMngr);
			Common.waitLong();
			// 鼠标移动到下载按钮位置
			robotMngr.moveMouseTo(Prop.getInt("browser360.coordinate.xiazaibutton.x"), Prop.getInt("browser360.coordinate.xiazaibutton.y"));
			Common.waitShort();
			// 右击下载按钮
			robotMngr.clickMouseRight();
			Common.waitM();
			// 菜单向下移动四个位置，到“另存为”
			for(int i=0; i<4; i++){
				robotMngr.pressDown();
				Common.waitShort();
			}
			Common.waitShort();
			// 选中“另存为”
			robotMngr.pressEnter();
			Common.waitShort();
			// 等待新建下载任务框打开
			Browser360Utils.waitXinjianxiazairenwuOpen(robotMngr);
			Common.waitM();
			// 鼠标移动到“下载到”这个输入框
			robotMngr.moveMouseTo(Prop.getInt("browser360.coordinate.xiazaidaoinput.x"), Prop.getInt("browser360.coordinate.xiazaidaoinput.y"));
			Common.waitShort();
			robotMngr.clickMouseLeft();
			Common.waitShort();
			// 选中这个输入框全部字符
			robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
			Common.waitShort();
			dstDir.mkdirs();
			Common.waitShort();
			// 将生成的目标文件夹地址复制到剪贴板
			ClipboardUtils.setSysClipboardText(dstDir.getCanonicalPath());
			Common.waitShort();
			// 将剪贴板里的文件路径复制到输入框
			robotMngr.pressCombinationKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
			Common.waitShort();
			// 确认下载
			robotMngr.pressEnter();
			Common.waitShort();
			// 等待直到下载页面打开
			Browser360Utils.waitXiazaiPageOpen(robotMngr);
			Common.waitLong();
			
		}
		Browser360Utils.waitAndClearDownloadEnd(robotMngr);
		Browser360Utils.close();
	}

	public static void main(String[] args) throws IOException, InterruptedException, AWTException, TransformWaitTimeoutException {
		RobotManager robotMngr = new RobotManager();
		File dstDir = new File("C:\\Users\\oddro\\Desktop\\qqmail");
		String url1 = "http://mail.qq.com/cgi-bin/ftnExs_download?k=2b6161633e4e7ac476ba577b1e610017075553065900015b485903025d4c060e03054c025b575015555700050103000d5256585238783208544cdea58c921f8c96ab9cdde5dacb8cc44c504d481146406517&t=exs_ftn_download&code=eaac8a28";
		String url2 = "http://mail.qq.com/cgi-bin/ftnExs_download?k=2e36613161628aca76ed5729176157180452000957075d5248030703084c510604024c080950531a51530706540556025d5352003177658ecefddf8cd8a8b91ad89cdcc4e184d5d14b461145496113&t=exs_ftn_download&code=e6a11ae7";
		String url3 = "http://mail.qq.com/cgi-bin/ftnExs_download?k=6a3333384d80fdc52be80520133101160a03500054000758150a050f511c075d5a0a1e00020652140955560802530a5d0c0a5109352c330b080204e8f0fe91898afb98f4d0fe86fcd8e2868af1f0fc17484347403547&t=exs_ftn_download&code=83385139";
		String[] urlArr = new String[]{url1,url2,url3};
		download(robotMngr, dstDir, urlArr);
	}
}
