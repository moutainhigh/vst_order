package com.lvmama.vst.order.service.impl;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.vo.order.OrderWechatAppVo;
import com.lvmama.vst.order.dao.OrdWechatAppDao;
import com.lvmama.vst.order.service.OrdWechatAppService;
@Service
public class OrdWechatAppServiceImpl implements OrdWechatAppService{
	@Autowired
	private OrdWechatAppDao ordWechatAppDao;
	@Override
	public void insert(OrderWechatAppVo orderWechatApp) {
		ordWechatAppDao.insert(orderWechatApp);
	}
	@Override
	public List<OrderWechatAppVo> search(Long orderId) {
		return ordWechatAppDao.search(orderId);
	}
}
