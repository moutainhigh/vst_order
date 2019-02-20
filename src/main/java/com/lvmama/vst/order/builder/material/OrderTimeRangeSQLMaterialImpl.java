package com.lvmama.vst.order.builder.material;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderTimeRangeParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单时间范围类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderTimeRangeSQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {
	
	private static OrderTimeRangeSQLMaterialImpl INSTANCE = null;
	
	private OrderTimeRangeSQLMaterialImpl(){
		
	}
	
	public static OrderTimeRangeSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderTimeRangeSQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderTimeRangeParam orderTimeRangeParam = (OrderTimeRangeParam) object;
		if (UtilityTool.isValid(orderTimeRangeParam.getCreateTimeBegin())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 下单起始时间,包含上限
			material.getWhereSet().add("ORD_ORDER.CREATE_TIME >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeBegin(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss')");
		
			// 子订单表
			if(material.getTableSet().contains(T_ORD_ORDER_ITEM)){
				material.getWhereSet().add("ORD_ORDER_ITEM.CREATE_TIME >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeBegin(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss')");
			}
			
			//打包
			if(material.getTableSet().contains(T_ORD_ORDER_PACK)){
				material.getWhereSet().add("(ORD_ORDER_PACK.CREATE_TIME >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeBegin(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss') OR ORD_ORDER_PACK.CREATE_TIME IS NULL)");
			}
		}

		if (UtilityTool.isValid(orderTimeRangeParam.getCreateTimeEnd())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 下单结束时间,包含下限
			material.getWhereSet().add("ORD_ORDER.CREATE_TIME <= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeEnd(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss')");
		
			// 子订单表
			if(material.getTableSet().contains(T_ORD_ORDER_ITEM)){
				material.getWhereSet().add("ORD_ORDER_ITEM.CREATE_TIME <= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeEnd(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss')");
			}			
			//打包
			if(material.getTableSet().contains(T_ORD_ORDER_PACK)){
				material.getWhereSet().add("(ORD_ORDER_PACK.CREATE_TIME <= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getCreateTimeEnd(),"yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:ss') OR ORD_ORDER_PACK.CREATE_TIME IS NULL)");
			}
		}
		
		if (UtilityTool.isValid(orderTimeRangeParam.getPaymentTimeBegin())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			Date paymentTimeBegin = orderTimeRangeParam.getPaymentTimeBegin();
			String timeBegin=DateUtil.formatDate(paymentTimeBegin, "yyyy-MM-dd HH:mm:ss");
			// 支付起始时间,包含上限
			material.getWhereSet().add("ORD_ORDER.PAYMENT_TIME >= TO_DATE('" + timeBegin + "','YYYY-MM-DD HH24:MI:ss')");
		}

		if (UtilityTool.isValid(orderTimeRangeParam.getPaymentTimeEnd())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			Date paymentTimeEnd=DateUtils.addSeconds(orderTimeRangeParam.getPaymentTimeEnd(), 1440*60-1);
			String timeEnd=DateUtil.formatDate(paymentTimeEnd, "yyyy-MM-dd HH:mm:ss");
			// 支付结束时间,包含下限
			material.getWhereSet().add("ORD_ORDER.PAYMENT_TIME <= TO_DATE('" + timeEnd + "','YYYY-MM-DD HH24:MI:ss')");
		}

		if (UtilityTool.isValid(orderTimeRangeParam.getVisitTimeBegin())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 入住开始时间，包含上限
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.VISIT_TIME >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getVisitTimeBegin(),"yyyy-MM-dd") + "','YYYY-MM-DD')");
		}

		if (UtilityTool.isValid(orderTimeRangeParam.getVisitTimeEnd())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 入住结束时间,包含下限
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.VISIT_TIME <= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getVisitTimeEnd(),"yyyy-MM-dd") + "','YYYY-MM-DD')");
		}
		
		if (UtilityTool.isValid(orderTimeRangeParam.getDistributionTimeBegin())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			//订单审核活动表
			material.getTableSet().add(T_COM_AUDIT);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
			material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
			//分单时间开始
			material.getWhereSet().add("TRUNC(COM_AUDIT.CREATE_TIME) >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getDistributionTimeBegin(),"yyyy-MM-dd") + "','YYYY-MM-DD')");
		}

		if (UtilityTool.isValid(orderTimeRangeParam.getDistributionTimeEnd())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			//订单审核活动表
			material.getTableSet().add(T_COM_AUDIT);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
			material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
			//分单时间结束
			material.getWhereSet().add("TRUNC(COM_AUDIT.CREATE_TIME) >= TO_DATE('" + DateUtil.formatDate(orderTimeRangeParam.getDistributionTimeEnd(),"yyyy-MM-dd") + "','YYYY-MM-DD')");
		}
		return material;
	}

}
