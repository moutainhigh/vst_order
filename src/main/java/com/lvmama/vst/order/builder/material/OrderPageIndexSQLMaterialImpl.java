package com.lvmama.vst.order.builder.material;

import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderPageIndexParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 分页SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderPageIndexSQLMaterialImpl implements IComplexQuerySQLMaterial {
	private static OrderPageIndexSQLMaterialImpl INSTANCE = null;
	
	private OrderPageIndexSQLMaterialImpl(){
		
	}
	
	public static OrderPageIndexSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderPageIndexSQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderPageIndexParam orderPageIndexParam = (OrderPageIndexParam) object;
		if (UtilityTool.isValid(orderPageIndexParam.getBeginIndex())) {
			// 起始索引
			material.setBeginIndex(orderPageIndexParam.getBeginIndex());
		}
		if (UtilityTool.isValid(orderPageIndexParam.getEndIndex())) {
			// 结束索引
			material.setEndIndex(orderPageIndexParam.getEndIndex());
		}
		return material;
	}

}
