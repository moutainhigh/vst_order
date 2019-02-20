package com.lvmama.vst.order.confirm.async.impl;

import com.lvmama.vst.back.order.po.OrdOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.DestOrderService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.async.AsynConfirmService;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;

/**
 * 异步确认(已审库)
 */
@Service("inconfirmAsynConfirmService")
public class InconfirmAsynConfirmServiceImpl implements AsynConfirmService {
    private static final Log LOG = LogFactory.getLog(InconfirmAsynConfirmServiceImpl.class);
    @Autowired
    private DestOrderService destOrderService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Override
    public void apiAsynConfirmOrder(ResultHandleT<OrdOrderItem> resultHandel) {
        if(resultHandel ==null ||resultHandel.getReturnContent() ==null) return;
        OrdOrderItem orderItem =resultHandel.getReturnContent();
        LOG.info("orderItemId=" +orderItem.getOrderItemId()
                +",success=" +resultHandel.isSuccess()
                +",msg=" +resultHandel.getMsg()
                +",errorCode=" +resultHandel.getErrorCode()
                +",confirmStatus=" +orderItem.getConfirmStatus());
        try{
            //如果订单状态已取消，则不再创建活动
            OrdOrder order = orderService.findByOrderId(orderItem.getOrderId());
            if (order.isCancel()) {
                //插入日志,便于查看
                insertChildLog(orderItem, "SYSTEM");
                return;
            }
            //已审->已确认
            Confirm_Enum.CONFIRM_STATUS status =convertConfirmStatus(orderItem.getConfirmStatus());//Confirm_Enum.CONFIRM_STATUS.SUCCESS;
//            if(resultHandel.isFail()){//对接失败
//                status =confirmAdapterService.convertConfirmStatus(resultHandel.getErrorCode());
//            }
            //是否人工已确认
            orderItem =orderService.getOrderItem(orderItem.getOrderItemId());
            LOG.info("orderItemId=" +orderItem.getOrderItemId()
                    +",confirmStatus=" +orderItem.getConfirmStatus()
                    +",status=" +status.name());
            if(!OrdOrderItemUtils.checkSupplierIdItem(orderItem)) return ;

            if(!Confirm_Enum.CONFIRM_STATUS.INCONFIRM.name().equals(orderItem.getConfirmStatus())){
                //插入日志,便于查看
                insertChildLog(orderItem,orderItem.getConfirmStatus(),status.name(),"SYSTEM");
                return;
            }
            //更新子订单确认状态
            ResultHandleT<ComAudit> result =destOrderService.updateChildConfirmStatus(orderItem, status, "SYSTEM",null);
            LOG.info("orderItemId=" +orderItem.getOrderItemId()
                    +",success=" +result.isSuccess());

            if(result.isSuccess() && result.getReturnContent()!=null){
                destOrderService.completeTaskByAudit(orderItem, result.getReturnContent());

                LOG.info("orderItemId=" +orderItem.getOrderItemId()
                        +",comAuditId=" +result.getReturnContent().getAuditId());
            }
        }catch (Exception ex) {
            LOG.error("orderItemId=" +orderItem.getOrderItemId()
                    +",Exception：" +ex);
        }
    }
    
    private CONFIRM_STATUS convertConfirmStatus(String resultStatus) {
		//对接失败，返回状态码匹配，默认满房
		CONFIRM_STATUS status =CONFIRM_STATUS.getCode(resultStatus);
		if(status ==null){
			status =CONFIRM_STATUS.FULL;
		}
		return status;
	}
    /**
     * 插入日志
     * @param orderItem
     * @param confirmStatus
     * @param newConfirmStatus
     * @param operator
     */
    private void insertChildLog(OrdOrderItem orderItem, String confirmStatus,String newConfirmStatus, String operator) {
        try{
            String logContent = "";
            logContent = ComLogUtil.getLogTxt("确认状态",Confirm_Enum.CONFIRM_STATUS.getCnName(newConfirmStatus),
                    Confirm_Enum.CONFIRM_STATUS.getCnName(confirmStatus));

            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), orderItem.getOrderItemId(),
                    operator,
                    "新增子订单["+orderItem.getOrderItemId()+"]对接异步确认日志: "+logContent,
                    ComLog.COM_LOG_LOG_TYPE.SUPP_SUPPLIER_SUPPLIER_CHANGE.name(),
                    "新增日志[" +ComLog.COM_LOG_LOG_TYPE.SUPP_SUPPLIER_SUPPLIER_CHANGE.getCnName() +"]",null);
        }catch(Exception ex){
            LOG.error("OrderItemId=" +  orderItem.getOrderItemId()
                    + ",Exception:" +ex);
        }
    }
    /**
     * 插入日志
     * @param orderItem
     * @param operator
     */
    private void insertChildLog(OrdOrderItem orderItem, String operator) {
        try{
            String logContent = "订单已取消";

            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), orderItem.getOrderItemId(),
                    operator,
                    "新增子订单["+orderItem.getOrderItemId()+"]对接异步确认日志: "+logContent,
                    ComLog.COM_LOG_LOG_TYPE.SUPP_SUPPLIER_SUPPLIER_CHANGE.name(),
                    "新增日志[" +ComLog.COM_LOG_LOG_TYPE.SUPP_SUPPLIER_SUPPLIER_CHANGE.getCnName() +"]",null);
        }catch(Exception ex){
            LOG.error("OrderItemId=" +  orderItem.getOrderItemId()
                    + ",Exception:" +ex);
        }
    }
}
