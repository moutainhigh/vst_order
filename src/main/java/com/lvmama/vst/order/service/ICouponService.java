/**
 * 
 */
package com.lvmama.vst.order.service;

import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;

/**
 * @author pengyayun
 *
 */
public interface ICouponService {
	
	public ResultHandle validateCoupon(BuyInfo buyInfo);
	public Pair<FavorStrategyInfo,Object> calCoupon(BuyInfo buyInfo);
	
	
}
