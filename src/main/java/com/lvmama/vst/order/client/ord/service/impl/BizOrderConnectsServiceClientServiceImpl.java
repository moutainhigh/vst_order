package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.play.connects.client.BizOrderConnectsServiceClientService;
import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.BizOrderConnectsPropService;

@Component("bizOrderConnectsServiceClientServiceRemote")
public class BizOrderConnectsServiceClientServiceImpl implements  BizOrderConnectsServiceClientService{

	@Autowired
	private BizOrderConnectsPropService bizOrderConnectsPropService;
	
	@Override
	public ResultHandleT<List<BizOrderConnectsProp>> findConnectsServiceByBranchId(
			Long branchId) {
		ResultHandleT<List<BizOrderConnectsProp>> result = new ResultHandleT<List<BizOrderConnectsProp>>();
		try {
			List<BizOrderConnectsProp> bizOrderConnectsProps= bizOrderConnectsPropService.selectMemByBranchId(branchId);
			result.setReturnContent(bizOrderConnectsProps);
		} catch (Exception e) {
			result.setMsg(e);
		}
		return result;
	}

	@Override
	public ResultHandleT<List<BizOrderConnectsProp>> findConnectsServiceByBranchIdNoCach(
			Long branchId) {
		ResultHandleT<List<BizOrderConnectsProp>> result = new ResultHandleT<List<BizOrderConnectsProp>>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("branchId", branchId);
			List<BizOrderConnectsProp> bizOrderConnectsProps= bizOrderConnectsPropService.selectAllByParams(params);
			result.setReturnContent(bizOrderConnectsProps);
		} catch (Exception e) {
			result.setMsg(e);
		}
		return result;
	}
	
}
