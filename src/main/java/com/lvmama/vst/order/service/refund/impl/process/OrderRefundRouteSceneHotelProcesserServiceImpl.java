package com.lvmama.vst.order.service.refund.impl.process;

import java.util.Date;
import java.util.Map;

import com.lvmama.comm.utils.DateUtil;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.order.service.refund.OrdRefundSaleRecordService;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.refund.IOrderRefundProcesserService;
/**
 * 自由行酒景
 * @version 1.0
 */
@Service("orderRefundRouteSceneHotelProcesserService")
public class OrderRefundRouteSceneHotelProcesserServiceImpl implements IOrderRefundProcesserService{
	@Autowired
	private OrderRefundComProcesserService orderRefundComProcesserService;
	@Autowired
	private OrdRefundSaleRecordService ordRefundSaleRecordService;

	@Override
	public void startProcesserByRefund(OrdOrder ordOrder ,Map<String, Object> params) {
		//初始化record快照信息
		Date applyTime =null;
		if(params.containsKey(OrderRefundConstant.APPLY_DATE)){
			applyTime =(Date)params.get(OrderRefundConstant.APPLY_DATE);
		}
		ordRefundSaleRecordService.init(ordOrder, applyTime);

		orderRefundComProcesserService.startProcesserByRefund(IOrderRefundProcesserService.REFUND_ORDER_PREPAID_MAIN_PROCESS_KEY
				, ordOrder.getOrderId(), params);
	}

	@Override
	public void updateOrderStatusToOrderRefund(OrdOrder order,
			Map<String, Object> params, Date applyDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completeTaskByOnlineRefundAudit(ComAudit comAudit) {
		orderRefundComProcesserService.completeTaskByOnlineRefundAudit(comAudit);
	}

	@Override
	public void completeTaskBySupplierConfirm(OrdOrder order, String supplierKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStartProcessByRefund(OrdOrder order, String operateName) {
		return false;
	}

	@Override
	public void updateOrderStatusToOrderRefund(OrdOrder order,
			Map<String, Object> params, Date applyDate, String operateName) {
		// TODO Auto-generated method stub
		
	}

}
