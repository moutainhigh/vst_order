package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.client.service.ConfirmAdapterClientService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 已审库服务(客服)
 */
@Service("inconfirmStatusService")
public class InconfirmStatusServiceImpl extends DefaultConfirmStatusService implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(InconfirmStatusServiceImpl.class);
	@Autowired
	private ConfirmAdapterClientService confirmAdapterServiceRemote;

    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception{
        OrdOrderItem orderItem =confirmStatusParamVo.getOrderItem();
        ResultHandleT<ComAudit> result =new ResultHandleT<ComAudit>();
        if (orderItem == null) {
            result.setMsg("orderItem is null");
            return (T)result;
        }
        //加载参数
        Confirm_Enum.CONFIRM_STATUS newStatus =confirmStatusParamVo.getNewStatus();
        String supplierNo =confirmStatusParamVo.getSupplierNo();
        String operator =confirmStatusParamVo.getOperator();
        Long linkId =confirmStatusParamVo.getLinkId();
        EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel = confirmStatusParamVo.getConfirmChannel();
        String supplierApiFlag=(String)orderItem.getContentMap().get(
                OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name());
        LOG.info("OrderItemId=" + orderItem.getOrderItemId()
                + ",supplierApiFlag=" + supplierApiFlag);
        //非对接
        if(!"Y".equals(supplierApiFlag)){
            //同步供应商凭证信息
            ResultHandle resultHandle =confirmAdapterServiceRemote.updateSupplierProcess(orderItem,newStatus
                    ,supplierNo,operator,linkId,confirmChannel);
            if(resultHandle.isFail()){
                throw new BusinessException(resultHandle.getMsg());
            }
        }
        //更新子订单确认状态
        result = updateChildConfirmStatus(confirmStatusParamVo);
        if (result.isFail()) {
            throw new BusinessException(result.getMsg());
        }
        return (T)result;
    }
}
