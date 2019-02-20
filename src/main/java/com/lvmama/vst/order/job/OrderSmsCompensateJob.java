package com.lvmama.vst.order.job;

import com.lvmama.vst.back.order.po.OrdSmsReSend;
import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdSmsSendDao;
import com.lvmama.vst.order.service.IOrdSmsReSendService;
import com.lvmama.vst.pet.adapter.ISmsRemoteServiceAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 短信补偿
 * @author zhouguoliang
 *
 */
public class OrderSmsCompensateJob implements Runnable{

    private final String SUCCESS ="SUCCESS";

    private final String FAIL = "FAIL";

    @Autowired
    OrdSmsSendDao ordSmsSendDao;
    @Autowired
    ISmsRemoteServiceAdapter smsRemoteService;
    @Autowired
    IOrdSmsReSendService iOrdSmsReSendService;

    @Override
    public void run() {
        if(Constant.getInstance().isJobRunnable()) {
            //先搜索所有失败的订单
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("status", FAIL);
            params.put("reTryCount","3");
            List<OrdSmsReSend> ordSmsSendList = iOrdSmsReSendService.findOrdSmsSendList(params);
            //循环订单。发送成功设置成SUCCESS然后 重试次数加以，失败了就加一,不做操作。
            //三次就不给重新发送
            for (OrdSmsReSend ordSmsReSend : ordSmsSendList) {
                try {
                    if(StringUtils.isNotBlank(ordSmsReSend.getMobile())) {
                        smsRemoteService.sendSms(ordSmsReSend.getOrderId(), ordSmsReSend.getContent(), ordSmsReSend.getMobile());
                        ordSmsReSend.setSendTime(new Date());
                        ordSmsReSend.setStatus(SUCCESS);
                        ordSmsReSend.setReTryCount(countPlusOne(ordSmsReSend));
                        iOrdSmsReSendService.updateByPrimaryKey(ordSmsReSend);
                        OrdSmsSend ordSmsSend = beanCopyForOrdSmsSend(ordSmsReSend);
                        ordSmsSendDao.insert(ordSmsSend);
                    }else{
                        ordSmsReSend.setReTryCount(4);
                        ordSmsReSend.setSendTime(new Date());
                        iOrdSmsReSendService.updateByPrimaryKey(ordSmsReSend);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ordSmsReSend.setReTryCount(countPlusOne(ordSmsReSend));
                    iOrdSmsReSendService.updateByPrimaryKey(ordSmsReSend);
                }
            }
        }
    }

    private int countPlusOne(OrdSmsReSend ordSmsReSend){
       return ordSmsReSend.getReTryCount()+1;
    }

    private OrdSmsSend beanCopyForOrdSmsSend(OrdSmsReSend reSend){
        OrdSmsSend record = new OrdSmsSend();
        record.setMobile(reSend.getMobile());
        record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
        record.setOrderId(reSend.getOrderId());
        record.setContent(reSend.getContent());
        record.setSendTime(new Date());
        record.setOperate("reTry");
        return record;
    }

}
