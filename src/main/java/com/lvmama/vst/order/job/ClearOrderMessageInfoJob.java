package com.lvmama.vst.order.job;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.JmsOrderMessageInfoDao;
import com.lvmama.vst.order.dao.SendFailedMessageInfoDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * 清理订单消息表数据JOB
 * Created by sangbowei on 2017/8/1.
 */
public class ClearOrderMessageInfoJob implements Runnable{

    private static final Log LOG = LogFactory.getLog(ClearOrderMessageInfoJob.class);

    @Autowired
    private JmsOrderMessageInfoDao jmsOrderMessageInfoDao;

    @Autowired
    private SendFailedMessageInfoDao sendFailedMessageInfoDao;


    @Override
    public void run() {
        if (Constant.getInstance().isJobRunnable()) {
            long startTime = System.nanoTime();
            LOG.info("ClearOrderMessageInfoJob begins running at " + startTime);
            clearOrderMessageInfo();
            clearFailedMessageInfo();
            long endTime = System.nanoTime();
            LOG.info("ClearOrderMessageInfoJob finished, spent " + TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.SECONDS) + " s");
        }
    }

    /**
     * 定期清除 订单消息表 中数据
     */
    private void clearOrderMessageInfo(){
        LOG.info("============ Auto clearOrderMessageInfo start ========");
        int result = jmsOrderMessageInfoDao.clearTwoMonthAgoMessageInfo();
        if(result>0){
            LOG.info(" clearOrderMessageInfo success! "+result+"  rows affected! ");
        }
        LOG.info("============ Auto clearOrderMessageInfo end ========");
    };

    /**
     * 定期清除 推送失败消息表 中数据
     */
    private void clearFailedMessageInfo(){
        LOG.info("============ Auto clearFailedMessageInfo start ========");
        int result = sendFailedMessageInfoDao.clearTwoMonthAgoFailedMessageInfo();
        if(result>0){
            LOG.info(" clearFailedMessageInfo success! "+result+"  rows affected! ");
        }
        LOG.info("============ Auto clearFailedMessageInfo end ========");
    }



}
