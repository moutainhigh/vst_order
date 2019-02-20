package com.lvmama.vst.order.builder.impl;

/**
 * SQL适配器,供mybatis的sqlmap文件读取完整SQL
 * 
 * @author wenzhengtao
 * 
 */
public class SQLStatementAdapter {
	private String sqlStatement;

	public SQLStatementAdapter(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	public String getSqlStatement() {
		return sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

}
