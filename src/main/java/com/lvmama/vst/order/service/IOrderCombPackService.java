package com.lvmama.vst.order.service;

import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;

/**
 * 
 * @author sunjian
 *
 */
public interface IOrderCombPackService {
	/**
	 * 验证订单打包商品
	 * 
	 * 如果categoryId是标准产品，则商品全部属于此productId，则返回true，否则返回false
	 * 如果categoryId是组合产品，则商品符合此产品组合选项，则返回true，否则返回false
	 * 
	 * @param orderPackVO
	 * @return
	 */
	public ResultHandle validateOrderCombPack(BuyInfo buyInfo);
}
