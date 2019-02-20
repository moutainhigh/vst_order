package com.lvmama.vst.order.builder.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.order.builder.impl.AbstractCountSQLBuilder;

/**
 * 订单综合查询总数SQL
 * 
 * @author wenzhengtao
 * 
 */
public final class OrderCountSQLBuilderImpl extends AbstractCountSQLBuilder {
	// 日志记录
	private static final Log LOG = LogFactory.getLog(OrderCountSQLBuilderImpl.class);
	// 查询字段
	private static final String COLUMN = " COUNT(DISTINCT ORD_ORDER.ORDER_ID) AS NUM ";

	@Override
	public Log getLog() {
		return LOG;
	}

	@Override
	public String getColumn() {
		return COLUMN;
	}

}
