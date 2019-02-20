package com.lvmama.vst.order.web;

import com.lvmama.comm.bee.service.ord.UnityOrderService;
import com.lvmama.vst.comm.vo.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author : liuchenggui
 * @Description:订单详情页订单删除/恢复功能
 * @Date : Create in 上午11:32 2018/4/16
 */
@Controller
@RequestMapping("/order/orderDelete")
public class OrderDeleteAction {

    private static final Logger LOG = LoggerFactory.getLogger(OrderDeleteAction.class);

    @Autowired
    private UnityOrderService unityOrderService;

    @RequestMapping(value = "/updateOrderDelFlag")
    @ResponseBody
    public Object updateOrderDelFlag(HttpServletRequest request){
        ResultMessage message = new ResultMessage("success","");
        try {
            Long orderId = Long.parseLong(request.getParameter("orderId"));
            String deleteFlag = request.getParameter("deleteFlag");
            LOG.info("updateOrderDelFlag orderId :"+orderId+" ,deleteFlag :"+deleteFlag);

            Map params = new HashMap();
            params.put("orderId",orderId);
            params.put("deleteFlag",deleteFlag);
            params.put("platform","VST");

            int count = unityOrderService.updateDeleteFlag(params);
            if(count == 0) message.raise("操作失败,该订单不存在!");
        }catch(Exception e){
            LOG.error("updateOrderDelFlag exception"+e.getMessage(),e);
            message.raise("系统异常,请稍后再试");
        }
        return message;
    }
}
