package com.lvmama.vst.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.dao.OrdItemShowTicketInfoDAO;
import com.lvmama.vst.order.service.OrdItemShowTicketInfoService;
import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;
@Service("ordItemShowTicketInfoService")
public class OrdItemShowTicketInfoServiceImpl implements OrdItemShowTicketInfoService{
	@Autowired
	private OrdItemShowTicketInfoDAO ordItemShowTicketInfoDAO;
	@Override
	public void insert(OrdItemShowTicketInfoVO ordYlTicketInfoVO) {
		ordItemShowTicketInfoDAO.insert(ordYlTicketInfoVO);
	}
	@Override
	public OrdItemShowTicketInfoVO queryByOrdItemId(Long ordItemId) {
		return ordItemShowTicketInfoDAO.queryByOrdItemId(ordItemId);
	}

}
