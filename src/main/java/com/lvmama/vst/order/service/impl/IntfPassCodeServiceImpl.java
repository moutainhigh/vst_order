package com.lvmama.vst.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.passport.service.PassCodeService;
import com.lvmama.vst.back.passport.po.PassCode;
import com.lvmama.vst.order.service.IntfPassCodeService;

@Service
public class IntfPassCodeServiceImpl implements IntfPassCodeService{
	
	
	
	private static Logger logger = LoggerFactory.getLogger(IntfPassCodeServiceImpl.class);
	
	@Autowired
	private PassCodeService passcodeService;
	
	/**
	 * 根据申码成功消息objectId的codeId 查主订单号
	 */
	public Long getOrderIdByPassCodeId(Long passCodeId){
		PassCode code = passcodeService.getPassCodeByCodeId(passCodeId);
		if(code == null)
			return null;
		return code.getOrderId();
	}
	
}
