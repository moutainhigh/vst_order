package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;

import com.lvmama.vst.back.goods.po.SuppGoodsStock;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.web.BusinessException;

public interface IOrderStockService {

	/**
	 * 检查商品库存情况
	 * 
	 * @param distributionId
	 * @param goodsId
	 * @param visitTime
	 * @return
	 * @throws BusinessException
	 */
	public ResultHandleT<SupplierProductInfo> checkStock(BuyInfo buyInfo);

	/**
	 * 检查一个商品能否下单
	 * @param distributionId
	 * @param item
	 * @return
	 */
	ResultHandleT<Boolean> checkStock(Long distributionId, BuyInfo.Item item,boolean checkParamFlag);

	/**
	 * 
	 * @param orderItemId
	 * @return
	 */
	public List<OrdOrderStock> findOrderStockListByOrderItemId(Long orderItemId);
	
	   /**
     * 单酒店，根据商品ID，入住日期，离店日期，分销商ID查询库存
     * @param suppGoodsId 商品ID
     * @param visitTimeDate 入住日期
     * @param leaveTimeDate 离店日期
     * @param distributionId 分销商ID
     * @return
     */
    public ResultHandleT<SuppGoodsStock> getHotelSuppGoodsStock(Long suppGoodsId, Date visitTimeDate, Date leaveTimeDate, Long distributionId);
}
