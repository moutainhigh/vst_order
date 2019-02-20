package com.lvmama.vst.order.client.ord.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrdOrderStatusClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStatus;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdOrderStatusDao;

@Component("ordOrderStatusServiceRemote")
public class OrdOrderStatusClientServiceImpl implements
        OrdOrderStatusClientService {
    
    @Autowired
    private OrdOrderStatusDao orderStatusDao;
    
    @Override
    public int addOrdOrderStatus(OrdOrderStatus orderStatus) {
        return orderStatusDao.insertSelective(orderStatus);
    }

    @Override
    public OrdOrderStatus findOrdOrderStatusByOrderId(Long orderId) {
        return orderStatusDao.selectByOrderId(orderId);
    }

    @Override
    public void addOrdOrderStatus(ResultHandleT<OrdOrder> handle) {
        
        OrdOrder ordOrder = handle.getReturnContent();
        if (!handle.isFail()) {
            return;
        }
        
        if(null == ordOrder || null == ordOrder.getOrderId() || !isCategoryHotel(ordOrder)) return;

        Long status = null;
        
        String errorCode = null;
        String errorMsg = null;
        Long orderItemId = null;
        
        if (handle.isFail()) {
            //创建订单失败
            if (StringUtil.isNotEmptyString(handle.getErrorCode())) {
                errorCode = handle.getErrorCode();
            }
            errorMsg = handle.getMsg();
            status = OrderStatusEnum.ORDER_PROCESS_STATUS.FAILED.getStatusCode();
        }
        OrdOrderStatus orderStatus = new OrdOrderStatus();
        
        Long orderId = ordOrder.getOrderId();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderItemId(orderItemId);
        orderStatus.setStatus(status);
        orderStatus.setErrorCode(errorCode);
        orderStatus.setErrorMsg(errorMsg);
        orderStatus.setCreateTime(new Date());
        
        //如果错误缓存表里已经存在该orderId的记录，则不插入。
        OrdOrderStatus check = orderStatusDao.selectByOrderId(orderId);
        if (null != check || null != check.getOrderId()) {
            return;
        }
        
        orderStatusDao.insertSelective(orderStatus);
    }

    /**
     * 判断酒店品类
     * @param oldOrder
     * @return
     */
    private boolean isCategoryHotel(OrdOrder ordOrder) {
        return ordOrder.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId();
    }

}
