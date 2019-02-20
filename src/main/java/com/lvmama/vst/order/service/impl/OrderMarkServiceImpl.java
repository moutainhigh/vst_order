package com.lvmama.vst.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderMark;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderMarkDao;
import com.lvmama.vst.order.service.IOrdOrderMarkService;

@Service
public class OrderMarkServiceImpl implements IOrdOrderMarkService {
    private static Logger logger = LoggerFactory.getLogger(OrderMarkServiceImpl.class);
    @Autowired
    private OrdOrderMarkDao ordOrderMarkDao;

    @Override
    public Long saveOrdOrderMark(OrdOrderMark ordOrderMark) {
        Long id = null;
        try {
            ordOrderMarkDao.saveOrdOrderMark(ordOrderMark);
            id = ordOrderMark.getId();
        } catch (Exception e) {
            logger.error("method saveOrdOrderMark error", e);
            throw new BusinessException(ErrorCodeMsg.ERR_SYS);
        }

        return id;
    }

    @Override
    public int updateOrdOrderMark(OrdOrderMark ordOrderMark) {
        int num;
        try {
            num = ordOrderMarkDao.updateOrdOrderMark(ordOrderMark);
        } catch (Exception e) {
            logger.error("method updateOrdOrderMark error", e);
            throw new BusinessException(ErrorCodeMsg.ERR_SYS);
        }

        return num;
    }

    @Override
    public OrdOrderMark findOrdOrderMarkByOrderId(Long orderId) throws BusinessException {
        OrdOrderMark ordOrderMark = null;
        try {
            ordOrderMark = ordOrderMarkDao.findOrdOrderMarkByOrderId(orderId);
        } catch (Exception e) {
            logger.error("method findOrdOrderMarkByOrderId error", e);
            throw new BusinessException(ErrorCodeMsg.ERR_SYS);
        }

        return ordOrderMark;
    }

}
