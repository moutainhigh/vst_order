package com.lvmama.vst.order.service;

import com.lvmama.vst.back.prom.po.PromForbidKeyPo;

import com.lvmama.vst.comm.vo.order.BuyInfo;


public interface IOrderPromService {

	
	public PromForbidKeyPo isPromForbidBuyOrder(BuyInfo buyInfo);

}
