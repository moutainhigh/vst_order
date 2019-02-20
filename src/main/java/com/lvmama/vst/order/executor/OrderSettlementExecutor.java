package com.lvmama.vst.order.executor;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.order.processer.OrderSettlementProcesser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 漏单结算处理类
 * @author Zhang.Wei
 */
@Component("orderSettlementExecutor")
public class OrderSettlementExecutor implements ComJobConfigExecutor {
    private static final Log LOG = LogFactory.getLog(OrderSettlementExecutor.class);

    @Autowired
    private OrderSettlementProcesser orderSettlementProcesser;

    @Override
    public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
        ResultHandleTT<ComJobConfig> resultHandle = new ResultHandleTT<ComJobConfig>();
        ComJobConfig updateComJobConfig = null;
        int operateCode = ComJobConfigExecutor.DElETE_COMJOBCONFIG;
        boolean processFalg=true;//用于判断如果执行成功，则根据pkId删除
        Message message = MessageFactory.newOrderCreateMessage(comJobConfig.getObjectId());
        message.setEventType(comJobConfig.getObjectType());
        if (comJobConfig != null) {

            LOG.info("OrderSettlementExecutor.execute: OrderID=" + comJobConfig.getObjectId() + ",RetryCount=" + comJobConfig.getRetryCount());

            try {
                orderSettlementProcesser.process(message, false);
            } catch (Exception e) {
                processFalg=false;
            }

            if (processFalg==false && comJobConfig.getRetryCount() > 0) {

                updateComJobConfig = new ComJobConfig();
                updateComJobConfig.setComJobConfigId(comJobConfig.getComJobConfigId());
                updateComJobConfig.setRetryCount(comJobConfig.getRetryCount() - 1);
                updateComJobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));

                operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
            }
        }

        if (operateCode == ComJobConfigExecutor.DElETE_COMJOBCONFIG) {
            updateComJobConfig = comJobConfig;
        }
        resultHandle.setCode(operateCode);
        resultHandle.setReturnContent(updateComJobConfig);
        return resultHandle;
    }
}
