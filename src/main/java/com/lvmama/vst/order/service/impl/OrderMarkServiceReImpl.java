package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderMarkReDao;
import com.lvmama.vst.order.service.IOrdOrderMarkReService;
import com.lvmama.vst.order.vo.OrdOrderMarkVo;

@Service
public class OrderMarkServiceReImpl implements IOrdOrderMarkReService {
    private static Logger logger = LoggerFactory.getLogger(OrderMarkServiceReImpl.class);
    @Autowired
    private OrdOrderMarkReDao ordOrderMarkReDao;

    @Override
    public List<OrdOrderMarkVo> findOrdOrderMarkResByParams(Map<String, Object> params) throws BusinessException {
        List<OrdOrderMarkVo> ordOrderMarkReList = null;
        try {
            ordOrderMarkReList = ordOrderMarkReDao.findOrdOrderMarkResByParams(params);
        } catch (Exception e) {
            logger.error("method findOrdOrderMarkResByParams error", e);
            throw new BusinessException(ErrorCodeMsg.ERR_SYS);
        }

        return ordOrderMarkReList;
    }

    @Override
    public int getTotalCount(Map<String, Object> params) throws BusinessException {
        int count = 0;
        try {
            Integer result = ordOrderMarkReDao.getTotalCount(params);
            if (result != null) {
                count = result;
            }
        } catch (Exception e) {
            logger.error("method getTotalCount error", e);
            throw new BusinessException(ErrorCodeMsg.ERR_SYS);
        }

        return count;
    }

}
