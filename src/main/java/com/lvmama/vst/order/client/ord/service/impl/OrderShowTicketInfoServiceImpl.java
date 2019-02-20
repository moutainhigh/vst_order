package com.lvmama.vst.order.client.ord.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import com.lvmama.vst.back.client.ord.service.OrderShowTicketInfoService;
import com.lvmama.vst.back.order.po.OrdItemShowTicketInfoVO;
import com.lvmama.vst.order.service.OrdItemShowTicketInfoService;
import org.springframework.stereotype.Service;

@Service("orderShowTicketInfoService")
public class OrderShowTicketInfoServiceImpl implements OrderShowTicketInfoService{
	private  final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
    private OrdItemShowTicketInfoService ordItemShowTicketInfoService;
	@Override
	public OrdItemShowTicketInfoVO queryByOrdItemId(Long ordItemId) {
		log.info("yongle ordItemId=========="+ordItemId);
		OrdItemShowTicketInfoVO commItemShowTicketInfo = new OrdItemShowTicketInfoVO();
		com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO ordItemShowTicketInfo = ordItemShowTicketInfoService.queryByOrdItemId(ordItemId);
		try {
			BeanUtils.copyProperties(ordItemShowTicketInfo,commItemShowTicketInfo);
		} catch (Exception e) {
			log.info("yongle Exception=========="+e.getMessage());
			e.printStackTrace();
		}
		return commItemShowTicketInfo;
	}

}
