package com.lvmama.vst.order.builder.material;

import org.apache.commons.collections.CollectionUtils;

import com.lvmama.vst.comm.utils.SecurityTool;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderExcludedParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单排除类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderExcludedSQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {
	
	private static OrderExcludedSQLMaterialImpl INSTANCE = null;
	
	private OrderExcludedSQLMaterialImpl(){
		
	}
	
	public static OrderExcludedSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderExcludedSQLMaterialImpl();
		}
		return INSTANCE;
	}

	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderExcludedParam orderExcludedParam = (OrderExcludedParam) object;
		if (UtilityTool.isValid(orderExcludedParam.getOrderStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单状态
			material.getWhereSet().add("ORD_ORDER.ORDER_STATUS <> '" + SecurityTool.preventSqlInjection(orderExcludedParam.getOrderStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderExcludedParam.getInfoStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 信息状态
			material.getWhereSet().add("ORD_ORDER.INFO_STATUS <> '" + SecurityTool.preventSqlInjection(orderExcludedParam.getInfoStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderExcludedParam.getResourceStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 资源状态
			material.getWhereSet().add("ORD_ORDER.RESOURCE_STATUS <> '" + SecurityTool.preventSqlInjection(orderExcludedParam.getResourceStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderExcludedParam.getPaymentStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 支付状态
			material.getWhereSet().add("ORD_ORDER.PAYMENT_STATUS <> '" + SecurityTool.preventSqlInjection(orderExcludedParam.getPaymentStatus()) + "' ");
		}

		if (CollectionUtils.isNotEmpty(orderExcludedParam.getDistributorIds())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 下单渠道ID
			if (1 == orderExcludedParam.getDistributorIds().size()) {
				//一个
				material.getWhereSet().add("ORD_ORDER.DISTRIBUTOR_ID <> " + orderExcludedParam.getDistributorIds().get(0));
			} else {
				//多个
				StringBuilder builder = new StringBuilder();
				for (Long distributorId : orderExcludedParam.getDistributorIds()) {
					builder.append(distributorId);
					builder.append(",");
				}
				//去掉最后一个逗号
				builder.setLength(builder.length() - 1);
				material.getWhereSet().add("ORD_ORDER.DISTRIBUTOR_ID NOT IN " + K_LEFT_PARENTHESIS + builder.toString() + K_RIGHT_PARENTHESIS);
			}
		}
		return material;
	}

}
