package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdOrderItemExtend;
import com.lvmama.vst.order.dao.OrdOrderItemExtendDao;
import com.lvmama.vst.order.service.IOrdOrderItemExtendService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author limingkai
 * @Description
 * @date 2018/11/2
 */

@Service
public class OrdOrderItemExtendServiceImpl implements IOrdOrderItemExtendService {

    private static final Log LOG = LogFactory.getLog(OrdOrderItemExtendServiceImpl.class);

    @Autowired
    private OrdOrderItemExtendDao ordOrderItemExtendDao;

    @Override
    public int deleteByPrimaryKey(Long orderItemId) {
        return ordOrderItemExtendDao.deleteByPrimaryKey(orderItemId);
    }

    @Override
    public int insert(OrdOrderItemExtend ordOrderItemExtend) {
        return ordOrderItemExtendDao.insert(ordOrderItemExtend);
    }

    @Override
    public OrdOrderItemExtend selectByPrimaryKey(Long orderItemId) {
        return ordOrderItemExtendDao.selectByPrimaryKey(orderItemId);
    }

    @Override
    public int updateByPrimaryKeySelective(OrdOrderItemExtend ordOrderItemExtend) {
        return ordOrderItemExtendDao.updateByPrimaryKeySelective(ordOrderItemExtend);
    }
}
