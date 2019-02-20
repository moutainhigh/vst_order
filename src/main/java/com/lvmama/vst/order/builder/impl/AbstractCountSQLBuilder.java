package com.lvmama.vst.order.builder.impl;

import java.util.Set;

import org.apache.commons.logging.Log;

import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.order.builder.IComplexQuerySQLBuilder;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;

/**
 * 抽象SQL构造器
 * 
 * @author wenzhengtao
 * 
 */
public abstract class AbstractCountSQLBuilder implements
		IComplexQuerySQLBuilder, IComplexQuerySQLConstant {
	/**
	 * SQL缓存
	 */
	private final transient StringBuilder sqlBuilder = new StringBuilder();

	/**
	 * 根据不同的业务获取不同的日志信息
	 * 
	 * @return
	 */
	public abstract Log getLog();

	/**
	 * 根据不同的业务获取不同的查询信息
	 * 
	 * @return
	 */
	public abstract String getColumn();

	@Override
	public void buildSQLSelectStatement(final ComplexQuerySQLMaterial material) {
		String column = getColumn();
		Set<String> tableSet = material.getTableSet();
		if (tableSet.size() == 1) {
			column = column.replaceFirst("DISTINCT ", "");
		}
		if (!UtilityTool.isValid(column)) {
			throw new IllegalArgumentException("select statement can not null!");
		} else {
			sqlBuilder.append(K_SELECT);
			sqlBuilder.append(column);
		}
	}

	@Override
	public void buildSQLFromStatement(final ComplexQuerySQLMaterial material) {
		Set<String> tableSet = material.getTableSet();
		if (tableSet.isEmpty()) {
			throw new IllegalArgumentException("from statement can not null!");
		} else {
			int i = 0;
			for (String table : tableSet) {
				if (0 == i) {
					sqlBuilder.append(K_FROM);
				}
				sqlBuilder.append(table);
				if ((tableSet.size() - 1) > i) {
					sqlBuilder.append(K_COMMA);
				}
				i++;
			}
		}
	}

	@Override
	public void buildSQLWhereStatement(final ComplexQuerySQLMaterial material) {
		Set<String> whereSet = material.getWhereSet();
		int i = 0;
		for (String where : whereSet) {
			if (0 == i) {
				sqlBuilder.append(K_WHERE);
			}
			sqlBuilder.append(where);
			if ((whereSet.size() - 1) > i) {
				sqlBuilder.append(K_AND);
			}
			i++;
		}
	}

	@Override
	public void buildSQLOrderByStatement(final ComplexQuerySQLMaterial material) {
		// 统计总数时不排序
	}

	@Override
	public void buildSQLGroupByStatement(final ComplexQuerySQLMaterial material) {
		Set<String> groupBySet = material.getGroupBySet();
		int i = 0;
		for (String groupBy : groupBySet) {
			if (0 == i) {
				sqlBuilder.append(K_GROUP_BY);
			}
			sqlBuilder.append(groupBy);
			if ((groupBySet.size() - 1) > i) {
				sqlBuilder.append(K_COMMA);
			}
			i++;
		}
	}

	@Override
	public void buildSQLPageIndexStatement(
			final ComplexQuerySQLMaterial material) {
		// 统计总数时不分页
	}

	@Override
	public String buildCompleteSQLStatement() {
		final String sql = sqlBuilder.toString();
		Log logger = getLog();
		if (logger.isDebugEnabled()) {
			logger.debug("ORDER COUNT SQL:" + sql);
		}
		return sql;
	}

}
