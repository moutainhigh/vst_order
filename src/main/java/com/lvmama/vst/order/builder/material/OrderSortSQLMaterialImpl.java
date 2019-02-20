package com.lvmama.vst.order.builder.material;

import java.util.List;

import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单排序类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderSortSQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {
	
	private static OrderSortSQLMaterialImpl INSTANCE = null;
	
	private OrderSortSQLMaterialImpl(){
		
	}
	
	public static OrderSortSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderSortSQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		@SuppressWarnings("unchecked")
		final List<OrderSortParam> orderSortParams = (List<OrderSortParam>) object;
		for (OrderSortParam orderSortParam : orderSortParams) {
			if (orderSortParam.equals(orderSortParam.CREATE_TIME_ASC)) {
				// 订单表
				material.getTableSet().add(T_ORD_ORDER);
				// 下单时间升序
				material.getOrderBySet().add("ORD_ORDER.CREATE_TIME ASC");
			}
			if (orderSortParam.equals(orderSortParam.CREATE_TIME_DESC)) {
				// 订单表
				material.getTableSet().add(T_ORD_ORDER);
				// 下单时间降序
				material.getOrderBySet().add("ORD_ORDER.CREATE_TIME DESC");
			}
			if (orderSortParam.equals(orderSortParam.VISIT_TIME_ASC)) {
				// 订单表
				material.getTableSet().add(T_ORD_ORDER);
				// 游玩时间升序
				material.getOrderBySet().add("ORD_ORDER.VISIT_TIME ASC");
			}
			if (orderSortParam.equals(orderSortParam.VISIT_TIME_DESC)) {
				// 订单表
				material.getTableSet().add(T_ORD_ORDER);
				// 游玩时间时间降序
				material.getOrderBySet().add("ORD_ORDER.VISIT_TIME DESC");
			}
		}
		return material;
	}

}
