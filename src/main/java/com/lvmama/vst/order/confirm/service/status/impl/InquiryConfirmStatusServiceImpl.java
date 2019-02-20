package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 询位库服务(客服)
 */
@Service("inquiryConfirmStatusService")
public class InquiryConfirmStatusServiceImpl extends DefaultConfirmStatusService implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(InquiryConfirmStatusServiceImpl.class);
    @Autowired
    private IOrdStatusManageConfirmProcessService ordStatusManageConfirmProcessService;

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
        //更新资审状态&备注
        result =ordStatusManageConfirmProcessService.executeUpdateOrderResourceStatusAmple_O2O(orderItem.getOrderItemId()
                , OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM
                ,confirmStatusParamVo.getResourceRetentionTime() , confirmStatusParamVo.getOperator()
                , confirmStatusParamVo.getMemo(),confirmStatusParamVo.getAuditId());

        return (T)result;
    }
}
