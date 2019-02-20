package com.lvmama.vst.order.service.refund.impl.amount;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.insurant.client.service.InsPolicyClientService;
import com.lvmama.vst.insurant.po.InsPolicy;
import com.lvmama.vst.order.service.refund.OrdRefundSaleRecordService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrderAmountChangeClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.service.refund.IOrderRefundAmountService;

/**
 * 自由行酒景
 * @version 1.0
 */
@Service("orderRefundRouteSceneHotelAmountService")
public class OrderRefundRouteSceneHotelAmountServiceImpl implements IOrderRefundAmountService{
	private static final Log LOG = LogFactory.getLog(OrderRefundRouteSceneHotelAmountServiceImpl.class);
	@Autowired
	private OrderAmountChangeClientService orderAmountChangeClientService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private InsPolicyClientService insPolicyClientService;
	@Autowired
	private OrdRefundSaleRecordService ordRefundSaleRecordService;
	 
	@Override
	public Long getOrderTotalChangeMount(Long orderId) {
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("orderId",orderId);
		params.put("approveStatus", "APPROVE_PASSED");
		List<OrdAmountChange> list = orderAmountChangeClientService.findOrdAmountChangeList(params);

		//订单价格修改总计
		long totalAmountChange = 0;
		for (int i = 0; i < list.size(); i++) {

			OrdAmountChange ordAmountChange = list.get(i);

			// 订单价格减少
			if (StringUtils.isNotEmpty(ordAmountChange.getFormulas())
					&& "SUBTRACT".equals(ordAmountChange.getFormulas())) {
				if (i < 1) {
					totalAmountChange = -ordAmountChange.getAmount();
				} else {
					totalAmountChange -= ordAmountChange.getAmount();
				}

			} else {
				if (i < 1) {
					totalAmountChange = ordAmountChange.getAmount();
				} else {
					totalAmountChange += ordAmountChange.getAmount();
				}

			}

		}
		LOG.info("orderId="+ orderId +",totalAmountChange=" +totalAmountChange);
		return totalAmountChange;
	}

	@Override
	public Long getRefundAmount(OrdOrder ordOrder, Date applyDate) {
		LOG.info("getRefundAmount : orderId = "+ordOrder.getOrderId()+", applyDate = "+applyDate);

		Long refundAmount = 0L;

		List<OrdRefundSaleRecord> saleRecordList = ordRefundSaleRecordService.getOrdRefundSaleRecordByOrder(ordOrder, applyDate);
		if(saleRecordList != null){
			for(OrdRefundSaleRecord record : saleRecordList){
				refundAmount += record.getRefundMoney();
			}
		}
        return refundAmount;
	}



}
