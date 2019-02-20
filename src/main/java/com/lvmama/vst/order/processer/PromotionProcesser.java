package com.lvmama.vst.order.processer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.impl.OrdOrderServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.OrderPromotionService;
import com.lvmama.vst.order.service.PromPromotionService;

import javax.annotation.Resource;

/**
 * 促销
 * @author yanliping
 *
 */
public class PromotionProcesser implements MessageProcesser{
	protected transient final Log logger = LogFactory.getLog(getClass());
	@Autowired
	private OrderPromotionService orderPromotionService;
	@Autowired
	private PromPromotionService promPromotionService;
	@Autowired
	private PromotionService promotionService;
	@Autowired
	private IOrdOrderService orderService;
	@Resource
	protected IOrder2RouteService order2RouteService;
	@Override
	public void process(Message message) {
		logger.info("start PromotionProcesser.process");
		//订单消息迁移开关
		boolean msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
		//是否来自新订单系统
		boolean isFromNewOrderSys=NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType());
		logger.info("msgAndJobSwitch:"+msgAndJobSwitch+",isFromNewOrderSys:"+isFromNewOrderSys+",message:"+message);
		//如果消息开关打开且消息来自新系统，则消息在新系统处理，否则继续执行
		if(msgAndJobSwitch&&isFromNewOrderSys){
			return;
		}

		if(message.hasOrderMessage()){
			if(MessageUtils.isOrderCancelMsg(message)){
				Long orderId=message.getObjectId();
				logger.info("orderCancelMsg orderId="+orderId);
				List<OrdPromotion> promList = orderPromotionService.selectOrdPromotionsByOrderId(orderId);
				OrdOrder order = orderService.findByOrderId(orderId);
				if(promList!=null&&!promList.isEmpty()){
					List<Long> list = new ArrayList<Long>();
					for(OrdPromotion prom :promList){
						promPromotionService.subtractPromAmount(prom.getFavorableAmount(), prom.getPromPromotionId());
						list.add(prom.getPromPromotionId());
					}
					promotionService.subtractPromOrderAndPromUserNumber(order.getUserNo(),list);
				}
			}
		}
		
	}

}
