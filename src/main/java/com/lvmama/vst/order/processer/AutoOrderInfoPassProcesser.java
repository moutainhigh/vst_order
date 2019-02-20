/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;

/**
 * @author lancey
 *
 */
public class AutoOrderInfoPassProcesser implements MessageProcesser{
	private static final Log LOG = LogFactory.getLog(AutoOrderInfoPassProcesser.class);
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderLocalService orderLocalService;

	@Override
	public void process(Message message) {
		// ==判断消息来源是否新订单系统，若是，则不处理 start== 2017-09-20 by zhujingfeng
		if (NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType())) {
			LOG.info("AutoOrderInfoPassProcesser process message from new order system,no need to deal !message:" + message.toString());
			return;
		}
		// ==判断消息来源是否新订单系统，若是，则不处理 end== 2017-09-20 by zhujingfeng
		LOG.info("AutoOrderInfoPassProcesser.process: msg=eventType:" + message.getEventType());
		if(MessageUtils.isOrderCreateMsg(message)){
			//查询订单，如果是后台下单或是api订单，直接信息审核通过
			Long orderId = message.getObjectId();
			OrdOrder order = getOrderWithOrderItemByOrderId(orderId);
			if(ActivitiUtils.hasNotActivitiOrder(order)){
				//api订单
				if (order.isSupplierOrder()) {
					orderLocalService.executeUpdateInfoStatus(order, INFO_STATUS.INFOPASS.name(), "SYSTEM", "第三方供应商订单信息状态审核通过");
					LOG.info("AutoOrderInfoPassProcesser.process: msg=Order(ID=" + orderId + "), 第三方供应商订单信息状态审核通过。");
				//后台下单
				} else if (Constant.DIST_BACK_END == order.getDistributorId()) {
					orderLocalService.executeUpdateInfoStatus(order, INFO_STATUS.INFOPASS.name(), order.getBackUserId(), "后台下单信息状态审核通过");
					LOG.info("AutoOrderInfoPassProcesser.process: msg=Order(ID=" + orderId + "), 后台下单信息状态审核通过。");
				}
			}
		}
	}

	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}
}
