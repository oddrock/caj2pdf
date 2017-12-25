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

import com.oddrock.caj2pdf.bean.DiffernetBatchDocCount;
import com.oddrock.caj2pdf.bean.TransformFileInfo;
import com.oddrock.caj2pdf.bean.TransformInfo;
import com.oddrock.caj2pdf.main.DocFormatConverter;
import com.oddrock.caj2pdf.qqmail.MailDir;
import com.oddrock.caj2pdf.utils.DateStrTransformDstDirGenerator;
import com.oddrock.caj2pdf.utils.Prop;
import com.oddrock.caj2pdf.utils.TransformDstDirGenerator;
import com.oddrock.caj2pdf.utils.TransformRuleUtils;
import com.oddrock.common.awt.RobotManager;
import com.oddrock.common.file.FileUtils;

/**
 * 传输信息统计类
 * @author qzfeng
 *
 */
public class TransformInfoStater {
	private static Logger logger = Logger.getLogger(TransformInfoStater.class);
	private DiffernetBatchDocCount docCount;
	private String errorMsg;
	private MailDir maildir;
	private boolean needCopyContentOnClipboard;
	private String clipboardContent;
	private boolean needSendDstFileMail;			// 是否需要发含目标文件邮件
	private boolean needMoveSrcFile;
	private boolean needMoveMidFile;
	private boolean needMoveDstFile;
	private boolean needDelMidFile;
	private boolean testtransformNeedMoveSrcFile;
	private TransformInfo info;
	private RobotManager robotMngr;
	private Set<File> qualifiedSrcFileSet;	// 初选的可被转换的源文件
	private Set<File> srcFileSet;		// 源文件集合（被成功转换的源文件）
	private Set<File> dstFileSet;		// 目标文件集合（被成功转换的目标文件）
	private Set<File> midFileSet;		// 中间文件集合（被成功转换的中间文件）
	private File srcDir;
	private File dstDir;
	private File dstParentDir;			// 目标地址父路径
	private TransformDstDirGenerator dstDirgenerator;		// 目标路径生成器
	private boolean needDelSrcDir;
	public DiffernetBatchDocCount getDocCount() {
		return docCount;
	}
	public void setDocCount(DiffernetBatchDocCount docCount) {
		this.docCount = docCount;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public boolean isNeedCopyContentOnClipboard() {
		return needCopyContentOnClipboard;
	}
	public void setNeedCopyContentOnClipboard(boolean needCopyContentOnClipboard) {
		this.needCopyContentOnClipboard = needCopyContentOnClipboard;
	}
	public String getClipboardContent() {
		return clipboardContent;
	}
	public void setClipboardContent(String clipboardContent) {
		this.clipboardContent = clipboardContent;
	}
	public MailDir getMaildir() {
		return maildir;
	}
	public void setMaildir(MailDir maildir) {
		this.maildir = maildir;
	}
	public boolean isNeedSendDstFileMail() {
		return needSendDstFileMail;
	}
	public void setNeedSendDstFileMail(boolean needSendDstFileMail) {
		this.needSendDstFileMail = needSendDstFileMail;
	}
	public void setNeedDelSrcDir(boolean needDelSrcDir) {
		this.needDelSrcDir = needDelSrcDir;
	}
	public boolean isNeedDelSrcDir() {
		return needDelSrcDir;
	}
	public void setDstDirgenerator(TransformDstDirGenerator dstDirgenerator) {
		this.dstDirgenerator = dstDirgenerator;
	}
	public File getDstParentDir() {
		return dstParentDir;
	}
	public void setDstParentDir(File dstParentDir) {
		this.dstParentDir = dstParentDir;
	}
	public boolean isTesttransformNeedMoveSrcFile() {
		return testtransformNeedMoveSrcFile;
	}
	public void setTesttransformNeedMoveSrcFile(boolean testtransformNeedMoveSrcFile) {
		this.testtransformNeedMoveSrcFile = testtransformNeedMoveSrcFile;
	}
	public boolean isNeedMoveSrcFile() {
		return needMoveSrcFile;
	}
	public void setNeedMoveSrcFile(boolean needMoveSrcFile) {
		this.needMoveSrcFile = needMoveSrcFile;
	}
	public boolean isNeedMoveMidFile() {
		return needMoveMidFile;
	}
	public void setNeedMoveMidFile(boolean needMoveMidFile) {
		this.needMoveMidFile = needMoveMidFile;
	}
	public boolean isNeedMoveDstFile() {
		return needMoveDstFile;
	}
	public void setNeedMoveDstFile(boolean needMoveDstFile) {
		this.needMoveDstFile = needMoveDstFile;
	}
	public boolean isNeedDelMidFile() {
		return needDelMidFile;
	}
	public void setNeedDelMidFile(boolean needDelMidFile) {
		this.needDelMidFile = needDelMidFile;
	}
	// 检查是否有文件需要转换
	public boolean hasFileToTransform() throws IOException {
		Set<File> fileSet = getQualifiedSrcFileSet();
		if(fileSet.size()==0) {
			return false;
		}else {
			return true;
		}
	}
	public Set<File> getQualifiedSrcFileSet() throws IOException {
		// 如果没有待转换的源文件，就从源文件目录扫描得到待转换源文件
		if(qualifiedSrcFileSet.size()==0) {
			qualifiedSrcFileSet = TransformRuleUtils.getQualifiedSrcFileSet(srcDir, info.getTransform_type());
		}
		return qualifiedSrcFileSet;
	}
	public void setQualifiedSrcFileSet(Set<File> waitSrcFileSet) {
		this.qualifiedSrcFileSet = waitSrcFileSet;
	}
	public RobotManager getRobotMngr() {
		return robotMngr;
	}
	public void setRobotMngr(RobotManager robotMngr) {
		this.robotMngr = robotMngr;
	}
	public File getSrcDir() {
		return srcDir;
	}
	public File getDstDir() throws IOException {
		if(dstDir==null) {
			dstDir = dstDirgenerator.generate(this);
		}
		return dstDir;
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
		needDelSrcDir = false;
		needSendDstFileMail = false;
		needCopyContentOnClipboard = false;
		dstDirgenerator = new DateStrTransformDstDirGenerator();
		needMoveSrcFile = Prop.getBool("needmove.srcfile");
		needMoveMidFile = Prop.getBool("needmove.midfile");
		needMoveDstFile = Prop.getBool("needmove.dstfile");
		needDelMidFile = Prop.getBool("needdel.midfile");
		testtransformNeedMoveSrcFile = Prop.getBool("testtransform.needmove.srcfile");
		srcFileSet = new HashSet<File>();
		midFileSet = new HashSet<File>();
		dstFileSet = new HashSet<File>();
		qualifiedSrcFileSet = new HashSet<File>();
		info = new TransformInfo();
		info.setStart_time(new Date());
		docCount = new DiffernetBatchDocCount();
	}
	

	public TransformInfoStater(String transformType, File srcDir, File dstParentDir, RobotManager robotMngr) {
		this();
		/*if(transformType.contains("caj2word_test") || transformType.contains("caj2pdf_test")) {
			this.needDelMidFile = false;
		}*/
		info.setTransform_type(transformType);
		this.srcDir = srcDir;
		this.dstParentDir = dstParentDir;
		this.robotMngr = robotMngr;
	}
	
	public TransformInfoStater(String transformType, File srcDir, File dstParentDir, RobotManager robotMngr, TransformDstDirGenerator dstDirGenerator) {
		this(transformType,srcDir,dstParentDir,robotMngr);
		this.dstDirgenerator = dstDirGenerator;
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
	public void addQualidfiedSrcFile(File file) throws IOException {
		qualifiedSrcFileSet.add(file);
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
	
	// 目标地址和源地址是否不一致
	public boolean isDstDirDifferentWithSrcDir() throws IOException {
		if(this.getDstDir().equals(this.getSrcDir())){
			return true;
		}else {
			return false;
		}
	}
}
