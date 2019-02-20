package com.lvmama.vst.order.builder.impl;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.order.builder.IComplexQuerySQLBuilder;
import com.lvmama.vst.order.builder.IComplexQuerySQLDirector;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;
import com.lvmama.vst.order.builder.material.OrderActivitySQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderContentSQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderExcludedSQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderIndentitySQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderPageIndexSQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderSortSQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderStatusSQLMaterialImpl;
import com.lvmama.vst.order.builder.material.OrderTimeRangeSQLMaterialImpl;

/**
 * SQL控制器实现类 需要手动实例化
 * 
 * 
 * @author wenzhengtao
 * 
 */
public final class ComplexQuerySQLDirectorImpl implements IComplexQuerySQLDirector {
	// 查询条件封装类
	private transient ComplexQuerySQLCondition queryCondition;
	// 订单内容类SQL原材料构建类
	private IComplexQuerySQLMaterial orderContentSQLMaterial = OrderContentSQLMaterialImpl.getInstance();
	// 订单排除类SQL原材料构建类
	private IComplexQuerySQLMaterial orderExcludedSQLMaterial = OrderExcludedSQLMaterialImpl.getInstance();
	// 订单ID类SQL原材料构建类
	private IComplexQuerySQLMaterial orderIndentitySQLMaterial = OrderIndentitySQLMaterialImpl.getInstance();
	// 订单分页类SQL原材料构建类
	private IComplexQuerySQLMaterial orderPageIndexSQLMaterial = OrderPageIndexSQLMaterialImpl.getInstance();
	// 订单排序类SQL原材料构建类
	private IComplexQuerySQLMaterial orderSortSQLMaterial = OrderSortSQLMaterialImpl.getInstance();
	// 订单状态类SQL原材料构建类
	private IComplexQuerySQLMaterial orderStatusSQLMaterial = OrderStatusSQLMaterialImpl.getInstance();
	// 订单时间范围类SQL原材料构建类
	private IComplexQuerySQLMaterial orderTimeRangeSQLMaterial = OrderTimeRangeSQLMaterialImpl.getInstance();
	//订单活动类SQL原材料构建类
	private IComplexQuerySQLMaterial orderActivitySQLMaterial = OrderActivitySQLMaterialImpl.getInstance();

	/**
	 * 设置综合查询条件
	 */
	@Override
	public void setComplexQueryCondition(final ComplexQuerySQLCondition condition) {
		this.queryCondition = condition;
	}

	/**
	 * 控制命令函数
	 */
	@Override
	public void order(final IComplexQuerySQLBuilder builder, final boolean pageable) {
		final ComplexQuerySQLMaterial material = this.buildMaterial();
		builder.buildSQLSelectStatement(material);// select **
		builder.buildSQLFromStatement(material);// from **
		builder.buildSQLWhereStatement(material);// where **
		builder.buildSQLGroupByStatement(material);// group by **
		builder.buildSQLOrderByStatement(material);// order by **
		if (pageable) {
			builder.buildSQLPageIndexStatement(material);// pagination
		}
	}

	/**
	 * 构建SQL原材料函数
	 * 
	 * @return
	 */
	private ComplexQuerySQLMaterial buildMaterial() {
		ComplexQuerySQLMaterial material = new ComplexQuerySQLMaterial();
		material = orderContentSQLMaterial.buildMaterial(queryCondition.getOrderContentParam(), material);
		material = orderExcludedSQLMaterial.buildMaterial(queryCondition.getOrderExcludedParam(), material);
		material = orderIndentitySQLMaterial.buildMaterial(queryCondition.getOrderIndentityParam(), material);
		material = orderPageIndexSQLMaterial.buildMaterial(queryCondition.getOrderPageIndexParam(), material);
		material = orderSortSQLMaterial.buildMaterial(queryCondition.getOrderSortParams(), material);
		material = orderStatusSQLMaterial.buildMaterial(queryCondition.getOrderStatusParam(), material);
		material = orderTimeRangeSQLMaterial.buildMaterial(queryCondition.getOrderTimeRangeParam(), material);
		material = orderActivitySQLMaterial.buildMaterial(queryCondition.getOrderActivityParam(), material);
		return material;
	}
}
