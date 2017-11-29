package com.oddrock.caj2pdf.persist;

import java.io.InputStream;
import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.oddrock.caj2pdf.bean.TransformInfo;

public class TransformInfoDAOTest {
	public static void main(String[] args) {
        String resource = "conf.xml";
        InputStream is = TransformInfoDAOTest.class.getClassLoader().getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is); 
        SqlSession session = sessionFactory.openSession();
        try {
        	TransformInfoDAO dao = session.getMapper(TransformInfoDAO.class);
        	TransformInfo info = new TransformInfo();
        	info.setStart_time(new Date());
        	info.setTransform_type("caj2pdf11");
        	info.setSrc_file_size(11000000000000L);
        	dao.addTransformInfo(info);
        } finally {
            session.close();
        }
    }
}
