package com.oddrock.caj2pdf.persist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import com.oddrock.caj2pdf.bean.TransformFileInfo;
import com.oddrock.caj2pdf.bean.TransformInfo;
import com.oddrock.caj2pdf.main.DocFormatConverter;
import com.oddrock.common.file.FileUtils;

/**
 * 传输信息统计类
 * @author qzfeng
 *
 */
public class TransformInfoStater {
	private static Logger logger = Logger.getLogger(TransformInfoStater.class);
	private TransformInfo info;
	private Set<File> srcFileSet;		// 源文件集合
	private Set<File> dstFileSet;		// 目标文件集合
	private Set<File> midFileSet;		// 中间文件集合
	private File srcDir;
	private File dstDir;
	public File getSrcDir() {
		return srcDir;
	}
	public File getDstDir() {
		return dstDir;
	}
	
	public void setDstDir(File dstDir) {
		this.dstDir = dstDir;
	}
	public TransformInfo getInfo() {
		return info;
	}
	public Set<File> getSrcFileSet() {
		return srcFileSet;
	}

	public Set<File> getDstFileSet() {
		return dstFileSet;
	}

	public Set<File> getMidFileSet() {
		return midFileSet;
	}

	public TransformInfoStater() {
		super();
		srcFileSet = new HashSet<File>();
		midFileSet = new HashSet<File>();
		dstFileSet = new HashSet<File>();
		info = new TransformInfo();
		info.setStart_time(new Date());
	}
	
	public TransformInfoStater(String transformType) {
		this();
		info.setTransform_type(transformType);
	}
	
	public TransformInfoStater(String transformType, File srcDir, File dstDir) {
		this(transformType);
		this.srcDir = srcDir;
		this.dstDir = dstDir;
	}

	public void addSrcFile(File file) throws IOException {
		if(file==null) return;
		TransformFileInfo fileInfo = fileInfo(file);
		if(fileInfo==null) return;
		info.setSrc_file_type(fileInfo.getFile_type());
		info.setSrc_file_count(info.getSrc_file_count()+1);
		String file_names = info.getSrc_file_names();
		if(file_names==null){
			file_names = fileInfo.getFile_name();
		}else{
			file_names = file_names + " ||| " + fileInfo.getFile_name();
		}
		info.setSrc_file_names(file_names);
		info.setSrc_file_size(info.getSrc_file_size()+fileInfo.getFile_size());
		if(DocFormatConverter.selftest) {
			info.setSelftest(1);
		}else {
			info.setSelftest(0);
		}
		srcFileSet.add(file);
	}
	
	public void addDstFile(File file) throws IOException {
		if(file==null) return;
		TransformFileInfo fileInfo = fileInfo(file);
		if(fileInfo==null) return;
		info.setDst_file_type(fileInfo.getFile_type());
		info.setDst_file_count(info.getDst_file_count()+1);
		String file_names = info.getDst_file_names();
		if(file_names==null){
			file_names = fileInfo.getFile_name();
		}else{
			file_names = file_names + " ||| " + fileInfo.getFile_name();
		}
		info.setDst_file_names(file_names);
		info.setDst_file_size(info.getDst_file_size()+fileInfo.getFile_size());
		dstFileSet.add(file);
	}
	
	public void addMidFile(File file) throws IOException {
		midFileSet.add(file);
	}
	
	// 从文件中获取信息
	private TransformFileInfo fileInfo(File file) throws IOException{
		if(file==null) return null;
		TransformFileInfo fileInfo = new TransformFileInfo();
		fileInfo.setFile_name(file.getName());
		fileInfo.setFile_size(file.length());
		String suffix = FileUtils.getFileNameSuffix(file.getName());
		fileInfo.setFile_type(suffix);
		return fileInfo;
	}
	
	public void save2db() throws IOException {
		info.setEnd_time(new Date());
		String resource = "conf.xml";
        InputStream is = TransformInfoDAOTest.class.getClassLoader().getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is); 
        SqlSession session = sessionFactory.openSession();
        try {
        	TransformInfoDAO dao = session.getMapper(TransformInfoDAO.class);
        	dao.addTransformInfo(info);
        } finally {
            session.close();
        }
        logger.warn("转换信息保存到数据库成功！！！");
	}
}
