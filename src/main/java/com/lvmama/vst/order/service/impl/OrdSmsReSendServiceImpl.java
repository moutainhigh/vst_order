package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdSmsReSend;
import com.lvmama.vst.order.dao.OrdSmsReSendDao;
import com.lvmama.vst.order.service.IOrdSmsReSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouguoliang on 2016/8/1.
 */
@Service
public class OrdSmsReSendServiceImpl implements IOrdSmsReSendService {
    @Autowired
    private OrdSmsReSendDao ordSmsReSendDao;
    @Override
    public int deleteByPrimaryKey(Long smsId) {
        return ordSmsReSendDao.deleteByPrimaryKey(smsId);
    }

    @Override
    public int insert(OrdSmsReSend record) {

        return ordSmsReSendDao.insert(record);
    }

    @Override
    public int insertSelective(OrdSmsReSend record) {
        return ordSmsReSendDao.insertSelective(record);
    }

    @Override
    public OrdSmsReSend selectByPrimaryKey(Long smsId) {
        return ordSmsReSendDao.selectByPrimaryKey(smsId);
    }

    @Override
    public int updateByPrimaryKeySelective(OrdSmsReSend record) {
        return ordSmsReSendDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(OrdSmsReSend record) {
        return ordSmsReSendDao.updateByPrimaryKey(record);
    }

    @Override
    public Integer getTotalCount(Map<String, Object> params) {
        return ordSmsReSendDao.getTotalCount(params);
    }

    @Override
    public List<OrdSmsReSend> findOrdSmsSendList(Map<String, Object> params) {
        return ordSmsReSendDao.findOrdSmsSendList(params);
    }
    public int updateByPrimaryKeyEmail(List<Long> ids){
        return ordSmsReSendDao.updateByPrimaryKeyEmail(ids);
    }



}
