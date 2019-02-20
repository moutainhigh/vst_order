/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdCourierListingService;

import javax.annotation.Resource;

/**
 * 订单结算推送
 * @author lancey
 *
 */
public class OrderCourierListingProcesser implements MessageProcesser{

	protected transient final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdCourierListingService ordCourierListingService ;
	

	@Autowired
	private ProdProductClientService prodProductClientService;
	@Resource
	protected IOrder2RouteService order2RouteService;
	
	@Override
	public void process(Message message) {
		//订单消息迁移开关
		boolean msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
		//是否来自新订单系统
		boolean isFromNewOrderSys=NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType());
		logger.info("msgAndJobSwitch:"+msgAndJobSwitch+",isFromNewOrderSys:"+isFromNewOrderSys+",message:"+message);
		//如果消息开关打开且消息来自新系统，则消息在新系统处理，否则继续执行
		if(msgAndJobSwitch&&isFromNewOrderSys){
			return;
		}
		
		if (MessageUtils.isOrderPaymentMsg(message)
				|| MessageUtils.isOrderResourcePassMsg(message)
				|| MessageUtils.isOrderInfoPassMsg(message)) {

			Long orderId = message.getObjectId();
			logger.info("接受支付成功消息 orderId=" + orderId);

			OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderId);

			if (orderObj.hasInfoAndResourcePass() && orderObj.hasPayed() && orderObj.isNormal()) {
				
				 List<OrdOrderItem> orderItemList=orderObj.getOrderItemList();
				for (OrdOrderItem ordOrderItem : orderItemList) {

//					ProdProduct prodProduct = prodProductClientService
//							.findProdProductById(ordOrderItem.getProductId(),
//									Boolean.TRUE, Boolean.TRUE);
					ResultHandleT<ProdProduct> result = prodProductClientService.findProdProductByIdFromCache(ordOrderItem.getProductId());
					ProdProduct prodProduct = result.getReturnContent();
					logger.info("ordOrderItem=" + ordOrderItem.getOrderItemId());
					logger.info("hasExpresstypeDisplay=" + ordOrderItem.hasExpresstypeDisplay());
					logger.info("PRODUCTTYPE=" + prodProduct.getProductType());
					
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderItemId", ordOrderItem.getOrderItemId());
					List<OrdCourierListing> courierListings = ordCourierListingService.findOrdCourierListingList(params);
					logger.info("OrderCourierListingProcesser, orderItemId:" + ordOrderItem.getOrderItemId() + ", courierListings:" + courierListings + ", empty:" + CollectionUtils.isEmpty(courierListings));
					if (ordOrderItem.hasExpresstypeDisplay()
							&& !StringUtils.equalsIgnoreCase(
									PRODUCTTYPE.EXPRESS.getCode(),
									prodProduct.getProductType()) && CollectionUtils.isEmpty(courierListings)) {
						OrdCourierListing ordCourierListing = new OrdCourierListing();
						ordCourierListing.setOrderId(orderId);
						ordCourierListing.setOrderItemId(ordOrderItem
								.getOrderItemId());
						ordCourierListing
								.setExpressType(ordOrderItem
										.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.express_type
												.name()));
						ordCourierListing.setCreateTime(new Date());

						ordCourierListingService
								.addOrdCourierListing(ordCourierListing);
					}

				}
				
			}

		}

	}
	
	
	/**
	 * 根据OrderId返回单个用订单那子项Order对象
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
		orderFlagParam.setOrderPersonTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}

}
