package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.comm.vo.ResultHandleT;

public interface IBusinessRuleService {
	/**
	 * 查询所有业务规则列表
	 * 
	 * @return
	 */
	public ResultHandleT<List<BusinessRule>> findBusinessRuleByAllValid();
	
}
