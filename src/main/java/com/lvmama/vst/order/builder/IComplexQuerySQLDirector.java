package com.lvmama.vst.order.builder;

import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;

/**
 * SQL控制器接口
 * 
 * @author wenzhengtao
 * 
 */
public interface IComplexQuerySQLDirector {
	/**
	 * 设置综合查询条件
	 * 
	 * @param complexQueryCondition
	 */
	void setComplexQueryCondition(ComplexQuerySQLCondition condition);

	/**
	 * 命令SQL构造器构建SQL
	 * 
	 * @param builder
	 * @param pageable
	 */
	void order(IComplexQuerySQLBuilder builder, boolean pageable);
}
