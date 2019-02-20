package com.lvmama.vst.order.snapshot.web;

import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.snapshot.service.OrderSnapshotRecoupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 订单快照测试Action
 */
@Controller
@RequestMapping("/order/snapshot/test")
public class OrderSnapshotTestAction extends BaseActionSupport {
    private static final Log LOG = LogFactory.getLog(OrderSnapshotTestAction.class);
    @Resource
    private OrderSnapshotRecoupService orderSnapshotRecoupService;

    /**
     * 快照补偿
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/get")
    @ResponseBody
    public String showOrderStatusManage(HttpServletRequest request, Long orderId, Long orderItemId){
        if(!checkUrlValid(request.getParameter("code"))){
            return ("连接非法");
        }
        String[] authUsers = new String[]{"lv6800", "admin"};
        if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
            return "权限不足";
        }
        try{
            return orderSnapshotRecoupService.showOrderStatusManage(orderId, orderItemId);

        }catch (Exception ex){
            ex.printStackTrace();;
            return "error:" +ex.getMessage();
        }
    }
    /**
     * checkUrlValid
     * @param code
     * @return
     */
    private boolean checkUrlValid(String code){
        if(code == null){
            return false;
        }

        try{
            code = DESCoder.decrypt(code);
        }catch(Exception e){
            log.info(e);
        }

        String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());

        if(today.equalsIgnoreCase(code)){
            return true;
        }
        return false;
    }
}
