package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PACKAGETYPE;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.PreRefundCounterDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.PreRefundService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import com.lvmama.vst.ticket.service.PreRefundClientService;

@Component("preRefundServiceRemote")
public class PreRefundClientServiceImpl implements PreRefundClientService {

	private static final Log LOG = LogFactory.getLog(PreRefundClientServiceImpl.class);
	
	@Autowired
	private PreRefundService preRefundservice;
	

	@Override
	public void increase(Long orderId) {
		preRefundservice.increase(orderId);
		
	}
	
	@Override
	public boolean isPreRefundCountLessThan3(Long orderId) {
		return preRefundservice.isPreRefundCountLessThan3(orderId) ;
	}
	
	@Override
	public boolean canPreRefund(Long orderId){
		
		return preRefundservice.canPreRefund(orderId);
	}
	
}
