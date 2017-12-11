package com.oddrock.caj2pdf.utils;

import java.io.InputStream;
import java.util.Date;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import com.oddrock.caj2pdf.bean.TransformInfo;
import com.oddrock.caj2pdf.persist.TransformInfoDAO;
import com.oddrock.caj2pdf.persist.TransformInfoDAOTest;
import com.oddrock.caj2pdf.persist.TransformInfoStater;

public class AsyncDbSaver implements Runnable{
	private static Logger logger = Logger.getLogger(AsyncDbSaver.class);
	private TransformInfoStater tfis;
	public AsyncDbSaver(TransformInfoStater tfis) {
		super();
		this.tfis = tfis;
	}
	
	public void run() {
		logger.warn("开始保存转换信息到数据库...");
		TransformInfo info = tfis.getInfo();
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
        logger.warn("结束保存转换信息到数据库");
	}
	
	public static void saveDb(TransformInfoStater tfis){
		new Thread(new AsyncDbSaver(tfis)).start();
	}
}
