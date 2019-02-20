package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdPremUserRel;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.dao.OrdPremUserRelDao;
import com.lvmama.vst.order.service.IOrdPremUserRelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 客服与用户接口实现类
 * @author Zhang.Wei
 */
@Service
public class OrdPremUserRelServiceImpl implements IOrdPremUserRelService{
    private static Logger logger = LoggerFactory.getLogger(OrdPremUserRelServiceImpl.class);

    @Autowired
    OrdPremUserRelDao ordPremUserRelDao;
    @Override
    public ResultHandle saveOrdPremUserRel(OrdPremUserRel ordPremUserRel) {
        Assert.notNull("ordPremUserRel  can't be null");
        ResultHandle resultHandle=new ResultHandle();
        try {
            ordPremUserRelDao.insert(ordPremUserRel);
        } catch (Exception e) {
        	logger.error(ExceptionFormatUtil.getTrace(e));
            resultHandle.setMsg(e);
        }
        return resultHandle;
    }
}
