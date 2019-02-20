package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.service.IHotelOrderUnconfirmCertifService;
import com.lvmama.vst.back.order.po.OrdOrder;

@Component("hotelOrderUnconfirmCertifRemote")
public class HotelOrderUnconfirmCertifServiceImpl implements IHotelOrderUnconfirmCertifService {

	@Autowired
	private OrdOrderDao ordOrderDao;
	@Override
	public List<OrdOrder> selectHotelOrderList(Map<String, Object> params) {
		return ordOrderDao.selectHotelOrderList();
	}

}
