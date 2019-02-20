package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.JmsOrderMessageInfo;
import com.lvmama.vst.order.dao.JmsOrderMessageInfoDao;
import com.lvmama.vst.order.service.IJmsOrderMessageInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/21.
 */
@Service("jmsOrderMessageInfoService")
public class JmsOrderItemServiceImpl implements IJmsOrderMessageInfoService {

    private static Logger logger = LoggerFactory.getLogger(JmsOrderItemServiceImpl.class);

    @Autowired
    private JmsOrderMessageInfoDao jmsOrderMessageInfoDao;

    @Override
    public int deleteByMessageInfoId(Long messageInfoId) {
        return jmsOrderMessageInfoDao.deleteByPrimaryKey(messageInfoId);
    }

    @Override
    public int deleteByOrderId(Long orderId) {
        return jmsOrderMessageInfoDao.deleteByOrderId(orderId);
    }

    @Override
    public int deleteByOrderItemId(Long orderItemId) {
        return jmsOrderMessageInfoDao.deleteByOrderItemId(orderItemId);
    }

    @Override
    public int saveJmsOrderMessaeInfo(JmsOrderMessageInfo jmsOrderMessageInfo) {
        return jmsOrderMessageInfoDao.insert(jmsOrderMessageInfo);
    }

    @Override
    public int saveJmsOrderMessaeInfoSelective(JmsOrderMessageInfo jmsOrderMessageInfo) {
        return jmsOrderMessageInfoDao.insertSelective(jmsOrderMessageInfo);
    }

    @Override
    public JmsOrderMessageInfo selectByMessageInfoId(Long messageInfoId) {
        return jmsOrderMessageInfoDao.selectByPrimaryKey(messageInfoId);
    }

    @Override
    public List<JmsOrderMessageInfo> selectByParams(Map<String, Object> params) {
        return jmsOrderMessageInfoDao.selectByParams(params);
    }

    @Override
    public int updateJmsOrderMessaeInfo(JmsOrderMessageInfo jmsOrderMessageInfo) {
        return jmsOrderMessageInfoDao.updateByPrimaryKey(jmsOrderMessageInfo);
    }

    @Override
    public int updateJmsOrderMessaeInfoSelective(JmsOrderMessageInfo jmsOrderMessageInfo) {
        return jmsOrderMessageInfoDao.updateByPrimaryKeySelective(jmsOrderMessageInfo);
    }

    @Override
    public List<JmsOrderMessageInfo> selectByOrderId(Long orderId) {
        return jmsOrderMessageInfoDao.selectByOrderId(orderId);
    }

    @Override
    public List<JmsOrderMessageInfo> selectByOrderItemId(Long orderItemId) {
        return jmsOrderMessageInfoDao.selectByOrderItemId(orderItemId);
    }
}
