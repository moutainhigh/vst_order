package com.lvmama.vst.order.builder.material;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderActivityParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单活动类SQL原材料
 * 
 * @author wenzhengtao
 *
 */
public class OrderActivitySQLMaterialImpl implements IComplexQuerySQLMaterial,IComplexQuerySQLConstant{
	
	private static OrderActivitySQLMaterialImpl INSTANCE = null;
	
	private OrderActivitySQLMaterialImpl(){
		
	}
	
	public static OrderActivitySQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderActivitySQLMaterialImpl();
		}
		return INSTANCE;
	}

	@Override
	public ComplexQuerySQLMaterial buildMaterial(Object object,ComplexQuerySQLMaterial material) {
		final OrderActivityParam orderActivityParam = (OrderActivityParam)object;
		String activityName = orderActivityParam.getActivityName();
		String activityDetail = orderActivityParam.getActivityDetail();
		String activityStatus = orderActivityParam.getActivityStatus();
		
		if(UtilityTool.isValid(activityName)){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//订单审核活动表
			material.getTableSet().add(T_COM_AUDIT);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
			material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
			if(activityName.split(",").length>1){
				//多个活动列表
				material.getWhereSet().add("COM_AUDIT.AUDIT_TYPE IN ("+activityName + ")");
			}else{
				//单个活动列表
				material.getWhereSet().add("COM_AUDIT.AUDIT_TYPE = '"+activityName + "'");
			}
			
			//活动细分要依托于活动列表
			if(UtilityTool.isValid(activityDetail)){
				//订单表
				material.getTableSet().add(T_ORD_ORDER);
				//订单审核活动表
				material.getTableSet().add(T_COM_AUDIT);
				material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
				material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
				if(activityDetail.split(",").length>1){
					//多个活动列表
					material.getWhereSet().add("COM_AUDIT.AUDIT_SUBTYPE IN ("+activityDetail + ")");
				}else{
					//单个活动列表
					material.getWhereSet().add("COM_AUDIT.AUDIT_SUBTYPE = '"+activityDetail + "'");
				}
			}
		}
		
		if(UtilityTool.isValid(activityStatus)){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//订单审核活动表
			material.getTableSet().add(T_COM_AUDIT);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
			material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
			if(activityStatus.split(",").length>1){
				//多个活动状态
				material.getWhereSet().add("COM_AUDIT.AUDIT_STATUS IN ("+activityStatus + ")");
			}else{
				//单个活动状态
				material.getWhereSet().add("COM_AUDIT.AUDIT_STATUS = '"+activityStatus + "'");
			}
		}
		return material;
	}

}
