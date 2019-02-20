package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.back.client.biz.service.BusinessRuleClientService;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IBusinessRuleService;

@Service
public class BusinessRuleServiceImpl implements IBusinessRuleService {

	private static final Log LOG = LogFactory.getLog(BusinessRuleServiceImpl.class);
	
	@Autowired
	BusinessRuleClientService businessRuleClientRemote;
	
	@Override
	public ResultHandleT<List<BusinessRule>> findBusinessRuleByAllValid() {
		return businessRuleClientRemote.findBusinessRuleByAllValid();
	}
}