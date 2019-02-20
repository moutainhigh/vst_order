package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdSmsReSend;
import com.lvmama.vst.order.dao.OrdSmsReSendDao;
import org.apache.flume.annotations.InterfaceAudience;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouguoliang on 2016/7/29.
 */
public interface IOrdSmsReSendService {
    public int deleteByPrimaryKey(Long smsId);

    public int insert(OrdSmsReSend record);

    public int insertSelective(OrdSmsReSend record);

    public OrdSmsReSend selectByPrimaryKey(Long smsId);

    public int updateByPrimaryKeySelective(OrdSmsReSend record);

    public int updateByPrimaryKey(OrdSmsReSend record);

    public Integer getTotalCount(Map<String, Object> params);

    public List<OrdSmsReSend> findOrdSmsSendList(Map<String, Object> params) ;

    int updateByPrimaryKeyEmail(List<Long> ids);

}
