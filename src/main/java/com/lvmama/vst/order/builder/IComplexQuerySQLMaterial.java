package com.lvmama.vst.order.builder;

import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;

/**
 * SQL原材料接口
 * 
 * @author wenzhengtao
 * 
 */
public interface IComplexQuerySQLMaterial {
	/**
	 * 构建SQL原材料
	 * 
	 * @param object
	 * @param material
	 * @return
	 */
	ComplexQuerySQLMaterial buildMaterial(Object object, ComplexQuerySQLMaterial material);
}
