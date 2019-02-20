package com.lvmama.vst.order.service.refund.impl.amount;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.service.refund.IOrderRefundAmountService;

/**
 * 酒店套餐
 * @version 1.0
 */
@Service("orderRefundHotelCombAmountService")
public class OrderRefundHotelCombAmountServiceImpl implements IOrderRefundAmountService{
	 
	@Override
	public Long getOrderTotalChangeMount(Long orderId) {
		return 0L;
	}

	@Override
	public Long getRefundAmount(OrdOrder ordOrder, Date applyDate) {
        return 0L;
	}
}
