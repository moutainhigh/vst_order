package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.comm.vo.order.OrderWechatAppVo;

public interface OrdWechatAppService {
	public void insert(OrderWechatAppVo orderWechatApp);
	public List<OrderWechatAppVo> search(Long orderId);
}
