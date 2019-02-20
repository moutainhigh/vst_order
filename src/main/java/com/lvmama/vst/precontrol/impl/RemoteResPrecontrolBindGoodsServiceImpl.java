package com.lvmama.vst.precontrol.impl;

import com.alibaba.fastjson.JSON;
import com.lvmama.precontrol.service.RemoteResPreControlBindGoodsService;
import com.lvmama.precontrol.vo.VstOrderItemVo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouguoliang on 2017/4/26.
 */
@Service("remoteResPrecontrolBindGoodsService")
public class RemoteResPrecontrolBindGoodsServiceImpl extends CommResPrecontrolBindsServiceImpl implements RemoteResPreControlBindGoodsService {

	private static final Log LOG = LogFactory.getLog(RemoteResPrecontrolBindGoodsServiceImpl.class);
	
    @Override
    public void updateVstBudgetFlagBylist(Map<String, Object> params) {
    	LOG.info("call updateVstBudgetFlagBylist with " + JSON.toJSONString(params));
        remoteResPrecontrolBindGoodsDao.updateVstBudgetFlagBylist(params);
    }

    @Override
    public List<VstOrderItemVo> getVstNotBuyoutOrder(Map<String, Object> params) {
    	LOG.info("call getVstNotBuyoutOrder with " + JSON.toJSONString(params));
        return remoteResPrecontrolBindGoodsDao.getVstNotBuyoutOrder(params);
    }

}
