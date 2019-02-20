package com.lvmama.vst.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdTicketPost;
import com.lvmama.vst.order.dao.OrdTicketPostDao;
import com.lvmama.vst.order.service.OrdTicketPostService;

@Service
public class OrdTicketPostServiceImpl implements OrdTicketPostService{
	
	private static Logger logger = LoggerFactory.getLogger(OrdTicketPostServiceImpl.class);
	@Autowired
	private OrdTicketPostDao ordTicketPostDao;
	
	@Override
	public void insertOrdTicketPost(OrdTicketPost ordTicketPost){
		ordTicketPostDao.insert(ordTicketPost);
	}
}
