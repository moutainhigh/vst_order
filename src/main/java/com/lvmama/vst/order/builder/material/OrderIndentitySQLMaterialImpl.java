package com.lvmama.vst.order.builder.material;

import org.apache.commons.collections.CollectionUtils;

import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单ID类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderIndentitySQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {
	private static OrderIndentitySQLMaterialImpl INSTANCE = null;
	
	private OrderIndentitySQLMaterialImpl(){
		
	}
	
	public static OrderIndentitySQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderIndentitySQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderIndentityParam orderIndentityParam = (OrderIndentityParam) object;
		if (UtilityTool.isValid(orderIndentityParam.getOrderId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = " + orderIndentityParam.getOrderId());
		}
		
		if (null != orderIndentityParam.getOrderIds()) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 多个订单ID
			StringBuilder builder = new StringBuilder();
			for (Long orderId : orderIndentityParam.getOrderIds()) {
				builder.append(orderId);
				builder.append(",");
			}
			//去掉最后一个逗号
			builder.setLength(builder.length() - 1);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID IN " + K_LEFT_PARENTHESIS + builder.toString() + K_RIGHT_PARENTHESIS);
		}
		boolean isAdd = false;
		if (CollectionUtils.isNotEmpty(orderIndentityParam.getDistributorIds())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			String thisQuery = "";
			// 下单渠道ID
			if (1 == orderIndentityParam.getDistributorIds().size()) {
				//一个
				thisQuery = "ORD_ORDER.DISTRIBUTOR_ID = " + orderIndentityParam.getDistributorIds().get(0);
			} else {
				//多个
				StringBuilder builder = new StringBuilder();
				for (Long distributorId : orderIndentityParam.getDistributorIds()) {
					builder.append(distributorId);
					builder.append(",");
				}
				//去掉最后一个逗号
				builder.setLength(builder.length() - 1);
				thisQuery = "ORD_ORDER.DISTRIBUTOR_ID IN " + K_LEFT_PARENTHESIS + builder.toString() + K_RIGHT_PARENTHESIS;
			}
			
			if ("Y".equals(orderIndentityParam.getDistributorIdForWepAndApp())) {
				// 订单表
				//在新订单监控（备用）中下单渠道检索增加无线订单（App、Wap） 搜索
				isAdd = true;
				thisQuery = K_LEFT_PARENTHESIS 
						+ thisQuery 
						+ K_OR 
						+ "ORD_ORDER.DISTRIBUTION_CHANNEL IN " + K_LEFT_PARENTHESIS + "10000,107,108,110,10001,10002" + K_RIGHT_PARENTHESIS
						+ K_RIGHT_PARENTHESIS;
			}
			material.getWhereSet().add(thisQuery);
		}
		
		if ("Y".equals(orderIndentityParam.getDistributorIdForWepAndApp()) && !isAdd) {
			// 订单表
			//在新订单监控（备用）中下单渠道检索增加无线订单（App、Wap） 搜索
			material.getTableSet().add(T_ORD_ORDER);
			material.getWhereSet().add("ORD_ORDER.DISTRIBUTION_CHANNEL IN " + K_LEFT_PARENTHESIS + "10000,107,108,110,10001,10002" + K_RIGHT_PARENTHESIS);
		}

		if (UtilityTool.isValid(orderIndentityParam.getProductId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			
			material.getTableSet().add(T_ORD_ORDER_PACK);
			
			// 产品ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			//material.getWhereSet().add("ORD_ORDER_ITEM.PRODUCT_ID = " + orderIndentityParam.getProductId());
			
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_PACK.ORDER_ID(+)");
			material.getWhereSet().add("(ORD_ORDER_ITEM.PRODUCT_ID = " + orderIndentityParam.getProductId() + " or ORD_ORDER_PACK.PRODUCT_ID=" + orderIndentityParam.getProductId()  + ")");
		
		}
		
		if (UtilityTool.isValid(orderIndentityParam.getCategoryIds())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品分类ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER.CATEGORY_ID in  (" + orderIndentityParam.getCategoryIds() +")");
			//多选
			if (UtilityTool.isValid(orderIndentityParam.getSubCategoryIds())) {
				if (orderIndentityParam.getCategoryIds().indexOf(",") > -1) {
					material.getWhereSet().add("(ORD_ORDER.SUB_CATEGORY_ID in (" + orderIndentityParam.getSubCategoryIds() +") or ORD_ORDER.SUB_CATEGORY_ID is null)");
				} else {
					material.getWhereSet().add("ORD_ORDER.SUB_CATEGORY_ID in (" + orderIndentityParam.getSubCategoryIds() +")");
				}
			}
			//下拉框
			if (UtilityTool.isValid(orderIndentityParam.getSubCategoryId())) {
				if (orderIndentityParam.getCategoryIds().indexOf(",") > -1) {
					material.getWhereSet().add("(ORD_ORDER.SUB_CATEGORY_ID = " + orderIndentityParam.getSubCategoryIds() +" or ORD_ORDER.SUB_CATEGORY_ID is null)");
				} else {
					material.getWhereSet().add("ORD_ORDER.SUB_CATEGORY_ID = " + orderIndentityParam.getSubCategoryId());
				}
			}
		}
		if (UtilityTool.isValid(orderIndentityParam.getCategoryId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品分类ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.CATEGORY_ID = " + orderIndentityParam.getCategoryId());
		}
		
		if (UtilityTool.isValid(orderIndentityParam.getOrderCategoryId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			material.getWhereSet().add("ORD_ORDER.CATEGORY_ID = " + orderIndentityParam.getOrderCategoryId());
		}
		
		if (UtilityTool.isValid(orderIndentityParam.getSupplierId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 供应商ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.SUPPLIER_ID = " + orderIndentityParam.getSupplierId());
		}

		if(UtilityTool.isValid(orderIndentityParam.getManagerId())){
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			material.getWhereSet().add("  ORD_ORDER.MANAGER_ID = "+orderIndentityParam.getManagerId());
		}
		
		if(UtilityTool.isValid(orderIndentityParam.getItemManagerId())){
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 供应商ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add(" (ORD_ORDER_ITEM.MANAGER_ID = " + orderIndentityParam.getItemManagerId()+") ");
		}
		
		
		if (UtilityTool.isValid(orderIndentityParam.getPackCategoryId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单打包信息
			material.getTableSet().add(T_ORD_ORDER_PACK);
			// 产品分类ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_PACK.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_PACK.CATEGORY_ID = " + orderIndentityParam.getPackCategoryId());
		}
		if (UtilityTool.isValid(orderIndentityParam.getOrderItemId())){
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单打包信息
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品分类ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.ORDER_ITEM_ID = " + orderIndentityParam.getOrderItemId());
		}
		if (UtilityTool.isValid(orderIndentityParam.getOrderItems())){
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单打包信息
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品分类ID
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			StringBuffer sb = new StringBuffer();
			for(Long id:orderIndentityParam.getOrderItems()){
				sb.append(id);
				sb.append(",");
			}
			sb.setLength(sb.length()-1);
			material.getWhereSet().add("ORD_ORDER_ITEM.ORDER_ITEM_ID in(" + sb.toString()+")");
		}
		return material;
	}

}
