package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrdTicketRefundSerialService;
import com.lvmama.vst.back.order.po.OrdTicketRefundSerial;
import com.lvmama.vst.order.dao.OrdTicketRefundSerialDao;

/**
 * 门票子订单退款流水
 * @author jsyangyang
 *
 */
@Service("ordTicketRefundSerialService")
public class OrdTicketRefundSerialServiceImpl implements OrdTicketRefundSerialService {
    
	@Autowired
	private OrdTicketRefundSerialDao ordTicketRefundSerialDao;
	@Override
	public void saveOrdTicketRefundSerial(OrdTicketRefundSerial ordTicketRefundSerial) {
		ordTicketRefundSerialDao.insert(ordTicketRefundSerial);

	}

	@Override
	public List<OrdTicketRefundSerial> queryOrdTicketRefundSerialList(Map<String, Object> params) {
		return ordTicketRefundSerialDao.findOrdTicketRefundSerialList(params);
	}

}
