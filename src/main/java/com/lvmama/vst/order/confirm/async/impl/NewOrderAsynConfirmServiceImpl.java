package com.lvmama.vst.order.confirm.async.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.dest.service.provider.ApiDockProviderService;
import com.lvmama.vst.back.client.ord.service.DestOrderService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.async.AsynConfirmService;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;

/**
 * 异步确认(新单库)
 */
@Service("newOrderAsynConfirmService")
public class NewOrderAsynConfirmServiceImpl implements AsynConfirmService {
    private static final Log LOG = LogFactory.getLog(NewOrderAsynConfirmServiceImpl.class);
    @Autowired
    private DestOrderService destOrderService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ApiDockProviderService remoteDockProviderService;

    @Override
    public void apiAsynConfirmOrder(ResultHandleT<OrdOrderItem> resultHandel) {
        if(resultHandel ==null ||resultHandel.getReturnContent() ==null) return;
        OrdOrderItem orderItem =resultHandel.getReturnContent();
        LOG.info("orderItemId=" +orderItem.getOrderItemId()
                +",success=" +resultHandel.isSuccess()
                +",msg=" +resultHandel.getMsg());
        if(resultHandel.isFail()) return;
        try{
            //是否为新单库
            orderItem =orderService.getOrderItem(orderItem.getOrderItemId());
            LOG.info("orderItemId=" +orderItem.getOrderItemId()
                    +",confirmStatus=" +orderItem.getConfirmStatus()
                    +",supplierId=" +orderItem.getSupplierId());
            if(!OrdOrderItemUtils.checkSupplierIdItem(orderItem) ||
                    !Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name().equals(orderItem.getConfirmStatus())) return;

            //确认状态(默认非即时确认)
            Confirm_Enum.CONFIRM_STATUS status = Confirm_Enum.CONFIRM_STATUS.INCONFIRM;
            
            //是否及时确认
            String immediacyFlag = remoteDockProviderService.getProviderValueByKey("immediacyFlag", String.valueOf(orderItem.getSupplierId()));
            
            if(!OrdOrderItemUtils.isNotImmediately(orderItem.getSupplierId()) || "Y".equals(immediacyFlag)){
                status = Confirm_Enum.CONFIRM_STATUS.SUCCESS;
            }
            //更新子订单确认状态
            ResultHandleT<ComAudit> result =destOrderService.updateChildConfirmStatus(orderItem, status
                    , "SYSTEM",null);
            LOG.info("orderItemId=" +orderItem.getOrderItemId()
                    +",success=" +result.isSuccess());

            if(result.isSuccess() && result.getReturnContent() !=null){
                destOrderService.completeTaskByAudit(orderItem, result.getReturnContent());

                LOG.info("orderItemId=" +orderItem.getOrderItemId()
                        +",comAuditId=" +result.getReturnContent().getAuditId());
            }
        }catch (Exception ex) {
            LOG.error("orderItemId=" +orderItem.getOrderItemId()
                    +",Exception：" +ex);
        }
    }
    
    /**
     * 校验供应商异步确认子订单
     * @param orderItem
     * @return
     */
    public static boolean checkSupplierIdItem(OrdOrderItem orderItem){
        if(orderItem.hasSupplierApi() && StringUtils.isNotBlank(orderItem.getConfirmStatus())){
            return true;
        }
        return false;
    }
}
