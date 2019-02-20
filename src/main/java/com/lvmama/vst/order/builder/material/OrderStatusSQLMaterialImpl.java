package com.lvmama.vst.order.builder.material;

import com.lvmama.vst.comm.utils.SecurityTool;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderStatusParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单状态类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderStatusSQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {
	
	private static OrderStatusSQLMaterialImpl INSTANCE = null;
	
	private OrderStatusSQLMaterialImpl(){
		
	}
	
	public static OrderStatusSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderStatusSQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	/**
	 * 状态类字段首先要看数据库里是数字型还是字符串型，然后再决定拼接方式
	 */
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderStatusParam orderStatusParam = (OrderStatusParam) object;
		if (UtilityTool.isValid(orderStatusParam.getOrderStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单状态
			material.getWhereSet().add("ORD_ORDER.ORDER_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getOrderStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderStatusParam.getInfoStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 信息状态
			material.getWhereSet().add("ORD_ORDER.INFO_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getInfoStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderStatusParam.getResourceStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 资源状态
			material.getWhereSet().add("ORD_ORDER.RESOURCE_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getResourceStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderStatusParam.getPaymentStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 支付状态
			material.getWhereSet().add("ORD_ORDER.PAYMENT_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getPaymentStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderStatusParam.getIsTestOrder())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 是否测试订单
			material.getWhereSet().add("ORD_ORDER.IS_TEST_ORDER = '" + SecurityTool.preventSqlInjection(orderStatusParam.getIsTestOrder()) + "' ");
		}
		
		if (UtilityTool.isValid(orderStatusParam.getCertConfirmStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 凭证确认状态
			material.getWhereSet().add("ORD_ORDER.CERT_CONFIRM_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getCertConfirmStatus()) + "' ");
		}
		
		
		
		
		
		if (UtilityTool.isValid(orderStatusParam.getNoticeRegimentStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			
			material.getTableSet().add(T_ORD_ADDITION_STATUS);
			
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ADDITION_STATUS.ORDER_ID");
			
			// 出团通知书状态
			material.getWhereSet().add("ORD_ADDITION_STATUS.STATUS_TYPE ='NOTICE_REGIMENT_STATUS' AND ORD_ADDITION_STATUS.STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getNoticeRegimentStatus()) + "' ");
		}

		if (UtilityTool.isValid(orderStatusParam.getContractStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			
			material.getTableSet().add(T_ORD_TRAVEL_CONTRACT);
			
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_TRAVEL_CONTRACT.ORDER_ID");
			
			// 合同状态
			material.getWhereSet().add("ORD_TRAVEL_CONTRACT.STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getContractStatus()) + "' ");
		}
		//门票订单使用状态
		if (UtilityTool.isValid(orderStatusParam.getPerformStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 资源状态
			material.getWhereSet().add("ORD_ORDER.PERFORM_STATUS = '" + SecurityTool.preventSqlInjection(orderStatusParam.getPerformStatus()) + "' ");
		}
		return material;
	}

}
