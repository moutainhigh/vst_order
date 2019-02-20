package com.lvmama.vst.order.builder;

import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;

/**
 * SQL构造器接口
 * 
 * @author wenzhengtao
 * 
 */
public interface IComplexQuerySQLBuilder {
	/**
	 * 构建SELECT语句
	 * 
	 * @param material
	 */
	void buildSQLSelectStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建FROM语句
	 * 
	 * @param material
	 */
	void buildSQLFromStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建WHERE语句
	 * 
	 * @param material
	 */
	void buildSQLWhereStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建ORDER BY语句
	 * 
	 * @param material
	 */
	void buildSQLOrderByStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建GROUP BY语句
	 * 
	 * @param material
	 */
	void buildSQLGroupByStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建分页语句
	 * 
	 * @param material
	 */
	void buildSQLPageIndexStatement(ComplexQuerySQLMaterial material);

	/**
	 * 构建完整的SQL语句
	 * 
	 * @return
	 */
	String buildCompleteSQLStatement();
}
