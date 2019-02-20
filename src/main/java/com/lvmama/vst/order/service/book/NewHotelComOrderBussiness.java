package com.lvmama.vst.order.service.book;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.prom.po.PromForbidKeyPo;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.vo.OrdOrderDTO;

public interface  NewHotelComOrderBussiness {
	
	/**
	 * 返现计算
	 * @author fangxiang  2016-11-3
	 * @param order
	 */
	public void DestBucalcRebate(OrdOrderDTO order) throws BusinessException;
	
	/**
	 * 促销是否限购
	 * @author fangxiang  2016-11-3
	 * @param buyInfo
	 */
	public PromForbidKeyPo  isPromForbidBuyOrder(DestBuBuyInfo buyInfo );
	
	
	/**
	 * 初始化促销信息
	 * @param order
	 * @param key
	 * @param promotionIds
	 * @return
	 */
	List<OrdPromotion> initPromotion(OrdOrderDTO order,String key,List<Long> promotionIds);
	
	/**
	 * 促销赋值
	 * @param obj
	 * @param promotion
	 * @return
	 */
	IPromFavorable fillFavorableData(Object obj, PromPromotion promotion);

}
