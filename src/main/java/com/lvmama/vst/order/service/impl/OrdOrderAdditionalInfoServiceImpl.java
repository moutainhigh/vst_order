package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdOrderAdditionalInfo;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.dao.OrdOrderAdditionalInfoDao;
import com.lvmama.vst.order.service.OrdOrderAdditionalInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/2/6.
 */
@Service
public class OrdOrderAdditionalInfoServiceImpl implements OrdOrderAdditionalInfoService {
    private static final Log log = LogFactory.getLog(OrdOrderAdditionalInfoServiceImpl.class);
    @Resource
    private OrdOrderAdditionalInfoDao ordOrderAdditionalInfoDao;
    @Override
    public int insert(OrdOrderAdditionalInfo ordOrderAdditionalInfo) {
        if(ordOrderAdditionalInfo == null || ordOrderAdditionalInfo.getOrderId() == null) {
            return 0;
        }

        log.info("Saving order additional information, param is " + GsonUtils.toJson(ordOrderAdditionalInfo));
        int insertResult = ordOrderAdditionalInfoDao.insert(ordOrderAdditionalInfo);
        log.info("Save order additional information completed, result is " + insertResult);
        return insertResult;
    }
}
