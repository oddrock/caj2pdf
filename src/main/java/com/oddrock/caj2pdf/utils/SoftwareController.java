package com.oddrock.caj2pdf.utils;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.windows.CmdExecutor;

/**
 * 软件控制器抽象类
 * @author qzfeng
 *
 */
public abstract class SoftwareController {
	private static Logger logger = Logger.getLogger(SoftwareController.class);
	// 软件名称，便于在属性文件中检索
	private String softwareName;
	public SoftwareController(String softwareName) {
		super();
		this.softwareName = softwareName;
	}
	
	// 该软件的进程是否已启动
	public boolean isStart() throws IOException {
		for(String appname: Prop.get(softwareName+".appname").split(",")) {
			if(CmdExecutor.getSingleInstance().isAppAlive(appname)) {
				return true;
			}
		}
		return false;
	}
	
	// 关闭并确保该软件被关闭
	public void close() throws IOException, InterruptedException {
		if(isStart()) {
			for(String appname: Prop.get(softwareName+".appname").split(",")) {
				CmdExecutor.getSingleInstance().exeCmd("taskkill /f /im \"" + appname + "\"");
			}
		}
		while(isStart()) {
			logger.warn("等待"+softwareName+"关闭......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已关闭");
	}
	
	// 通过图片比对，检查软件界面是否被打开
	public boolean isOpen(RobotManager robotMngr) throws IOException{
		return CommonUtils.comparePic(robotMngr, softwareName+".mark.open");
	}
	
	// 用软件打开一个文件，并等待直到打开成功
	public void open(RobotManager robotMngr,File file) throws IOException, InterruptedException {
		if(file!=null && file.exists()) {
			CmdExecutor.getSingleInstance().exeCmd(Prop.get(softwareName+".path") + " \"" + file.getCanonicalPath() + "\"");
		}else {
			CmdExecutor.getSingleInstance().exeCmd(Prop.get(softwareName+".path"));
		}
		while(!isStart()) {
			logger.warn("等待"+softwareName+"启动......");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已启动");
		while(!isOpen(robotMngr)) {
			logger.warn("等待"+softwareName+"打开");
			CommonUtils.wait(Prop.getInt("interval.waitmillis"));
		}
		logger.warn("确认"+softwareName+"已打开");
	}
}
