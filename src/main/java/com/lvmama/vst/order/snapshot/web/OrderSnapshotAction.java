package com.lvmama.vst.order.snapshot.web;

import com.lvmama.order.snapshot.comm.util.OrdSnapshotUtils;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.snapshot.async.AsyncGatewaySnapshotComService;
import com.lvmama.vst.order.snapshot.async.IVstUrlRecoupSnapshotService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 订单快照Action
 */
@Controller
@RequestMapping("/order/snapshot")
public class OrderSnapshotAction extends BaseActionSupport {
    @Resource
    private AsyncGatewaySnapshotComService asyncGatewaySnapshotComService;
    @Resource
    private IVstUrlRecoupSnapshotService vstUrlRecoupSnapshotService;
    @Resource
    protected IComplexQueryService complexQueryService;
    @Resource
    private IOrderUpdateService orderUpdateService;

    /**
     * 快照补偿
     * @param request
     * @param orderId
     * @param orderItemId
     * @param id
     * @param key
     * @return
     */
    @RequestMapping(value = "/recoup/id")
    @ResponseBody
    public String showOrderStatusManage(HttpServletRequest request, Long orderId, Long orderItemId,Long id, String key){
        if(!checkUrlValid(request.getParameter("code"))){
            return ("连接非法");
        }
        String[] authUsers = new String[]{"lv6800", "admin"};
        if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
            return "权限不足："+ key;
        }
        try{
            if(!OrdSnapshotUtils.checkKey(key)){
                return "key error";
            }
            //加载对象
            Object object =asyncGatewaySnapshotComService.getObject(key, id);
            if(object ==null) return "object is null";

            //快照
            ResultHandle resultHandle =new ResultHandle();
            if (orderId != null) {
                OrdOrder ordOrder =complexQueryService.queryOrderByOrderId(orderId);
                resultHandle = vstUrlRecoupSnapshotService.orderRecoupKeyByObject(ordOrder, key, object);

            }else if (orderItemId != null) {
                OrdOrderItem ordOrderItem =orderUpdateService.getOrderItem(orderItemId);
                resultHandle = vstUrlRecoupSnapshotService.orderItemRecoupKeyByObject(ordOrderItem, key, object);
            }
            if (resultHandle.isFail()) {
                return "error :"+ resultHandle.getMsg();
            }
            return "successful";
        }catch (Exception ex){
            ex.printStackTrace();;
            return "error:" +ex.getMessage();
        }
    }
    /**
     * 快照补偿-mongo
     * @return
     */
    @RequestMapping(value = "/mongo/recoup/id")
    @ResponseBody
    public String showOrderStatusManage(HttpServletRequest request, Long orderId, Long orderItemId,Long id, String key, String isKeyByObject){
        if(!checkUrlValid(request.getParameter("code"))){
            return ("连接非法");
        }
        String[] authUsers = new String[]{"lv6800", "admin"};
        if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
            return "权限不足："+ key;
        }
        try{
            if(!OrdSnapshotUtils.checkKey(key)){
                return "key error";
            }
            //加载对象
            Object object =asyncGatewaySnapshotComService.getObject(key, id);
            if(object ==null) return "object is null";

            //快照
            ResultHandle resultHandle =new ResultHandle();
            if (orderId != null) {
                OrdOrder ordOrder =complexQueryService.queryOrderByOrderId(orderId);
                resultHandle = vstUrlRecoupSnapshotService.orderMongoRecoupKey(ordOrder, key, object);

            }else if (orderItemId != null) {
                OrdOrderItem ordOrderItem =orderUpdateService.getOrderItem(orderItemId);
                resultHandle = vstUrlRecoupSnapshotService.orderItemmongoRecoupKey(ordOrderItem, key, object);
            }
            if (resultHandle.isFail()) {
                return "error :"+ resultHandle.getMsg();
            }
            return "successful";
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
