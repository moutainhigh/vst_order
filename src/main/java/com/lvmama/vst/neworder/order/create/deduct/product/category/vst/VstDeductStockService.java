package com.lvmama.vst.neworder.order.create.deduct.product.category.vst;

import com.lvmama.vst.order.vo.OrdOrderDTO;

public interface VstDeductStockService {

	
	void deductStock(OrdOrderDTO order);
}
