/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;

/**
 * @author pengyayun
 *
 */
public interface IOrderExpressService {
	
	public ResultHandleT<List<ExpressSuppGoodsVO>> findOrderExpressGoods(BuyInfo buyInfo);
	
}
