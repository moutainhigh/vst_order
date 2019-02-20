package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdApplyInvoiceInfoDao;
import com.lvmama.vst.order.service.OrdApplyInvoiceInfoService;

/** 
 * @Title: OrderApplyInvoiceJob.java 
 * @Package com.lvmama.vst.order.job 
 * @Description: 定时job 用于酒店订单 入住24小时后自动申请发票 
 * @author Wangsizhi
 * @date 2016-10-11 下午2:20:06 
 * @version V1.0.0 
 */
public class OrderApplyInvoiceJob implements Runnable{

    private static final Log LOG = LogFactory.getLog(OrderApplyInvoiceJob.class);
    
    @Autowired
    private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;
    
    @Override
    public void run() {
        LOG.info(Constant.getInstance().isJobRunnable());
        if (Constant.getInstance().isJobRunnable()) {
            LOG.info("目的地单酒店前台下单申请发票，入住24小时后，自动申请发票job启动");
            ordApplyInvoiceInfoService.autoApplyInvoice();
        }
    }
}
