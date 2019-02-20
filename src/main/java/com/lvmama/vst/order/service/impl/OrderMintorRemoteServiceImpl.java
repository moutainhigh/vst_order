package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.client.ord.service.OrderMintiorService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.order.processer.OrderSmsSendProcesser;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;

@Component("orderMintorServiceRemote")
public class OrderMintorRemoteServiceImpl implements OrderMintiorService{
	@Autowired
	private IComplexQueryService  complexQueryService ;
	
	@Autowired
	private IComMessageService comMessageService;
    
	@Autowired
	private OrderSmsSendProcesser orderSmsSendProcesser;
	@Override
	@ReadOnlyDataSource
	public List<OrdOrder> checkOrderListFromReadDB(ComplexQuerySQLCondition condition) {
	     
		return complexQueryService.checkOrderListFromReadDB(condition);
	}

	@Override
	public OrdOrder queryOrderByOrderId(Long id) {
		
		return complexQueryService.queryOrderByOrderId(id);
	}

	@Override
	public int reminderOrderOfCloseHouse(Long orderId, String operatorId) {
	
		return comMessageService.savaReservationAfterCalOfCloseHourse(orderId, operatorId);
	}

	@Override
	public void handle(Message message, OrdOrder order) {
	    orderSmsSendProcesser.handle(message, order);
		
	}

}
