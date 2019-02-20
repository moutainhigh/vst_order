/**
 * 
 */
package com.lvmama.vst.order.service;

import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * @author lancey
 *
 */
public interface IOrderInitService {

	OrdOrderDTO initOrder(OrdOrderDTO order,boolean validTimePrice);
	
	/**
	 * 计算并初始化信息
	 * @return
	 */
	OrdOrderDTO initOrderAndCalc(final BuyInfo buyInfo);
	
	/**
	 * 计算并初始化信息(应付金额不计算促销)
	 * @return
	 */
	OrdOrderDTO initOrderAndCalcWithOutPromotion(final BuyInfo buyInfo);

	/**
	 * 简单地初始化订单，仅仅初始化子订单信息，不初始化价格等信息
	 * */
	OrdOrderDTO initOrderLightly(OrdOrderDTO order);
	
    /**
     * 
     * @Description: 目的地 自由行 酒+景 酒店套餐 计算需要后置的意外险游玩人 
     * @author Wangsizhi
     * @date 2016-11-22 上午10:41:22
     */
    void saveDestBuAccTrav(BuyInfo buyInfo, OrdOrder ordOrder);
    
    /**
     * 
     * @Description: 目的地 酒+景 酒店套餐 意外险游玩人后置 设置主单游玩人后置
     * @author Wangsizhi
     * @date 2016-11-21 下午4:48:14
     */
     void initDestBuAccTravDelayed(BuyInfo buyInfo, OrdOrder ordOrder);
     
    
}
