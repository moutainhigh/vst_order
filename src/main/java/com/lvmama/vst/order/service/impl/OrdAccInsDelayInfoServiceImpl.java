package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.order.dao.OrdAccInsDelayInfoDao;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;

/** 
 * @Title: OrdAccInsDelayInfoServiceImpl.java 
 * @Package com.lvmama.vst.order.service.impl 
 * @Description: TODO 
 * @author Wangsizhi
 * @date 2017-2-14 下午4:50:07 
 * @version V1.0.0 
 */
@Service("ordAccInsDelayInfoService")
public class OrdAccInsDelayInfoServiceImpl implements IOrdAccInsDelayInfoService {

    //private static final Logger logger = LoggerFactory.getLogger(OrdAccInsDelayInfoServiceImpl.class);
    
    @Autowired
    private OrdAccInsDelayInfoDao ordAccInsDelayInfoDao;

    @Override
    public int saveOrdAccInsDelayInfo(OrdAccInsDelayInfo ordAccInsDelayInfo) {
        return ordAccInsDelayInfoDao.insert(ordAccInsDelayInfo);
    }

    @Override
    public OrdAccInsDelayInfo selectByOrderId(Long orderId) {
        return ordAccInsDelayInfoDao.selectByOrderId(orderId);
    }

    @Override
    public List<OrdAccInsDelayInfo> queryOrdAccInsDelayInfoByParam(
            Map<String, Object> param) {
        return ordAccInsDelayInfoDao.selectByParam(param);
    }

    @Override
    public int updateOrdAccInsDelayInfo(OrdAccInsDelayInfo ordAccInsDelayInfo) {
        return ordAccInsDelayInfoDao.updateByPrimaryKey(ordAccInsDelayInfo);
    }
    

}
