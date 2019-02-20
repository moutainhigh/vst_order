/**
 * 
 */
package com.lvmama.vst.precontrol.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.lvmama.precontrol.service.CommResPrecontrolBindGoodsService;
import com.lvmama.vst.precontrol.dao.RemoteResPrecontrolBindGoodsDao;

/**
 * @author chenlizhao
 *
 */
public class CommResPrecontrolBindsServiceImpl implements CommResPrecontrolBindGoodsService {

	private static final Log LOG = LogFactory.getLog(CommResPrecontrolBindsServiceImpl.class);
	
	@Autowired
    protected RemoteResPrecontrolBindGoodsDao remoteResPrecontrolBindGoodsDao;

    @Override
    public Long getOrderItemNum(Map<String, Object> param) {
    	LOG.info("call getOrderItemNum with " + JSON.toJSONString(param));
        return  remoteResPrecontrolBindGoodsDao.getOrderItemNum(param);
    }

	@Override
	public List<Long> getPreControlPolicyHistoryOrder(Map<String, Object> params) {
		LOG.info("call getPreControlPolicyHistoryOrder with " + JSON.toJSONString(params));
		return remoteResPrecontrolBindGoodsDao.getPreControlPolicyHistoryOrder(params);
	}

	@Override
	public int setVstOrderItemBudgetFlag(Map<String, Object> params) {
		LOG.info("call setVstOrderItemBudgetFlag with " + JSON.toJSONString(params));
		return remoteResPrecontrolBindGoodsDao.setVstOrderItemBudgetFlag(params);
	}	
}
