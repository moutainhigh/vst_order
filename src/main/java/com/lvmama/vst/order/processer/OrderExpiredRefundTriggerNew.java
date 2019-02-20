package com.lvmama.vst.order.processer;

import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.lvcc.OrderLvccAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class OrderExpiredRefundTriggerNew extends BaseActionSupport {
    private static final Log LOG = LogFactory.getLog(OrderLvccAction.class);

    @Autowired
    private OrderExpiredRefundProcesser orderExpiredRefundProcesser;

    @RequestMapping(value = "/test/overRefundTest")
    @ResponseBody
    public Object overRefundTest(Model model, HttpServletRequest req){
        Long orderId = Long.valueOf(req.getParameter("orderId"));
        LOG.info("overRefundTest start ------ orderId:"+orderId);
        Message message = MessageFactory.newExpiredRefundMessage(orderId, "");
        orderExpiredRefundProcesser.process(message);
        LOG.info("overRefundTest end");
        return "1";
    }
}
