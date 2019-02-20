package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdOrderDisneyInfo;
import com.lvmama.vst.order.dao.OrdOrderDisneyInfoDao;
import com.lvmama.vst.order.service.IOrdOrderDisneyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by luoweiyi on 2016/3/4.
 */
@Service
public class OrdOrderDisneyInfoServiceImpl implements IOrdOrderDisneyInfoService {

    @Autowired
    private OrdOrderDisneyInfoDao ordOrderDisneyInfoDao;

    @Override
    public List<OrdOrderDisneyInfo> findByParams(Map<String, Object> params) {
        return ordOrderDisneyInfoDao.selectByParams(params);
    }

    @Override
    public Integer insert(OrdOrderDisneyInfo ordOrderDisneyInfo) {
        return ordOrderDisneyInfoDao.insert(ordOrderDisneyInfo);
    }
}
