/**
 * 
 */
package com.lvmama.vst.comm.mybatis;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * oralce sqlSession基类
 * 
 * @author chenlizhao
 *
 */
public class SqlSessionTemplateOraDaoSupport {

	protected SqlSessionTemplate sqlSession;
	
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSession = sqlSessionTemplate;
	}

	public final SqlSession getBatchSqlSession() {
		return new SqlSessionTemplate(this.sqlSession.getSqlSessionFactory(), ExecutorType.BATCH);
	}

	public SqlSession getSqlSession() {
		return this.sqlSession;
	}

	@Autowired(required = false)
	public final void setOraSqlSessionFactory(SqlSessionFactory oraSqlSessionFactory) {
		this.sqlSession = new SqlSessionTemplate(oraSqlSessionFactory);
	}
}
