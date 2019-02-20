package com.lvmama.vst.order.builder.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.order.builder.impl.AbstractQuerySQLBuilder;

/**
 * 订单综合查询SQL
 * 
 * 需要手动实例化
 * 
 * @author wenzhengtao
 * 
 */
public final class OrderQuerySQLBuilderImpl extends AbstractQuerySQLBuilder {
	// 日志记录
	private static final Log LOG = LogFactory.getLog(OrderQuerySQLBuilderImpl.class);
	// 查询字段
	private static final String COLUMN = "DISTINCT ORD_ORDER.*";

	@Override
	public Log getLog() {
		return LOG;
	}

	@Override
	public String getColumn() {
		return COLUMN;
	}

}
