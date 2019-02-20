package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.SendFailedMessaeInfo;
import com.lvmama.vst.order.dao.SendFailedMessageInfoDao;
import com.lvmama.vst.order.service.ISendFailedMessageInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/21.
 */
@Service("sendFailedMessageInfoService")
public class SendFailedMessageInfoServiceImpl implements ISendFailedMessageInfoService {

    private static Logger logger = LoggerFactory.getLogger(SendFailedMessageInfoServiceImpl.class);

    @Autowired
    private SendFailedMessageInfoDao sendFailedMessageInfoDao;


    @Override
    public int deleteByFailedMessageId(Long failedMessageId) {
        return sendFailedMessageInfoDao.deleteByPrimaryKey(failedMessageId);
    }

    @Override
    public int deleteByOrderId(Long orderId) {
        return sendFailedMessageInfoDao.deleteByOrderId(orderId);
    }

    @Override
    public int deleteByOrderItemId(Long orderItemId) {
        return sendFailedMessageInfoDao.deleteByOrderItemId(orderItemId);
    }

    @Override
    public int saveSendFailedMessaeInfo(SendFailedMessaeInfo sendFailedMessaeInfo) {
        return sendFailedMessageInfoDao.insert(sendFailedMessaeInfo);
    }

    @Override
    public int saveSendFailedMessaeInfoSelective(SendFailedMessaeInfo sendFailedMessaeInfo) {
        return sendFailedMessageInfoDao.insertSelective(sendFailedMessaeInfo);
    }

    @Override
    public SendFailedMessaeInfo selectByFailedMessageId(Long failedMessageId) {
        return sendFailedMessageInfoDao.selectByPrimaryKey(failedMessageId);
    }

    @Override
    public List<SendFailedMessaeInfo> selectByParams(Map<String, Object> params) {
        return sendFailedMessageInfoDao.selectByParams(params);
    }

    @Override
    public int updateSendFailedMessaeInfo(SendFailedMessaeInfo sendFailedMessaeInfo) {
        return sendFailedMessageInfoDao.updateByPrimaryKey(sendFailedMessaeInfo);
    }

    @Override
    public int updateSendFailedMessaeInfoSelective(SendFailedMessaeInfo sendFailedMessaeInfo) {
        return sendFailedMessageInfoDao.updateByPrimaryKeySelective(sendFailedMessaeInfo);
    }

    @Override
    public List<SendFailedMessaeInfo> selectByOrderId(Long orderId) {
        return sendFailedMessageInfoDao.selectByOrderId(orderId);
    }

    @Override
    public List<SendFailedMessaeInfo> selectByOrderItemId(Long orderItemId) {
        return sendFailedMessageInfoDao.selectByOrderItemId(orderItemId);
    }
}
