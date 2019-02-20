/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdOrderTravellerService;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;

/**
 * 电子合同处理器
 * 
 * @author sunjian
 *
 */
public class OrderEcontractProcesser implements MessageProcesser{
	private static final Log LOG = LogFactory.getLog(OrderEcontractProcesser.class);
	private static final String NO_INPUT="待填写";
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private OrderService orderservice;
	@Autowired
	private IOrdPersonService iOrdPersonService;
	@Autowired
	private OrdOrderTravellerService ordOrderTravellerService;
	@Autowired
	private IOrderSendSmsService iOrderSendSmsService;
	@Autowired
	private OrderEcontractGeneratorService orderEcontractGeneratorService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Override
	public void process(Message message) {
		LOG.info("OrderEcontractProcesser.process: jms eventType=" + message.getEventType() + ",objectId=" + message.getObjectId());
		//支付完成
		if(MessageUtils.isOrderPaymentMsg(message)){
			Long orderId = message.getObjectId();
			//查询订单，自动锁定游玩人
			queryAndUpdateOrderForLockTraveller(orderId);
			setTravleContractEffectStatusByOrderId(orderId);
		//资源审核通过
		} else if(MessageUtils.isOrderResourcePassMsg(message)){
			Long orderId = message.getObjectId();
			setTravleContractEffectStatusByOrderId(orderId);
		//信息审核通过
		} else if(MessageUtils.isOrderInfoPassMsg(message)){
			Long orderId = message.getObjectId();
			setTravleContractEffectStatusByOrderId(orderId);
		//订单取消
		} else if(MessageUtils.isOrderCancelMsg(message)){
			Long orderId = message.getObjectId();
			if (orderId != null) {
//				OrdTravelContract originTravelContract = getOrdTravelContractByOrderId(orderId);
				List<OrdTravelContract> originTravelContractList = getOrdTravelContractByOrderId(orderId);
				if(originTravelContractList != null) {
					for(OrdTravelContract originTravelContract : originTravelContractList) {
						OrdTravelContract ordTravelContract = new OrdTravelContract();
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
						ordTravelContract.setOrdContractId(originTravelContract.getOrdContractId());
						ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, "SYSTEM");
					}
				}
			}
		} 
	}

	/**
	 * 查询订单，当地游与跟团游，游玩人已填写则锁定游玩人
	 * @param orderId
	 */
	private void queryAndUpdateOrderForLockTraveller(Long orderId) {
		OrdOrder order=orderservice.queryOrdorderByOrderId(orderId);
		boolean flag=true,isInsurance=false;
		if(order!=null){
			OrdOrderItem mainOrderItem=null;
			for (OrdOrderItem element : order.getOrderItemList()) {
				if(element.hasMainItem()){
					mainOrderItem=element;
				}
				if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().longValue()==element.getCategoryId().longValue()){
					isInsurance=true;
				}
			}
			if(mainOrderItem!=null){
				LOG.info("=====orderId:"+order.getOrderId()+"===mainOrderItem:"+mainOrderItem.hasMainItem()+"===ProductType:"+mainOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()));
			}
			if(order.hasPayed()
					&& ((BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==order.getCategoryId().longValue()
							&&ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equals(mainOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name())))
						||(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().longValue()==order.getCategoryId().longValue()
						&&(ProdProduct.PRODUCTTYPE.INNERLINE.name().equals(mainOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))
						||ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equals(mainOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))
						||ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equals(mainOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name())))))
					&& "Y".equals(order.getTravellerDelayFlag())&&!"Y".equals(order.getTravellerLockFlag())){
				//查询游玩人信息
				Map<String, Object> params=new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				params.put("personType",OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
				List<OrdPerson> ordpersonlist=iOrdPersonService.findOrdPersonList(params);
				//如果也填写，则锁定游玩人
				if(CollectionUtils.isNotEmpty(ordpersonlist)){
					for (OrdPerson ordPerson : ordpersonlist) {
						LOG.info("====orderId:"+order.getOrderId()+"ordPerson.getFullName():"+ordPerson.getFullName()+"===");
						if(isInsurance&&(StringUtil.isEmptyString(ordPerson.getIdNo())||StringUtil.isEmptyString(ordPerson.getIdType()))){
							flag=false;
							break;
						}
						if(StringUtil.isEmptyString(ordPerson.getFullName())||NO_INPUT.equals(ordPerson.getFullName())){
							flag=false;
							break;
						}
					}
					if(flag){
						LOG.info("====orderId:"+order.getOrderId()+",lock traveller ok");
						ordOrderTravellerService.updateOrderLockTraveller(orderId);
						orderEcontractGeneratorService.generateEcontract(orderId, "SYSTEM");
						iOrderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM);
						// 添加操作日志
						try {
							lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,orderId,orderId,
									"SYSTEM","已支付系统自动锁定出游人",ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PAYMENT.name(),"系统自动锁定出游人","已支付游玩人已填写，系统自动锁定出游人");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							LOG.error("Record Log failure ！Log type:" + COM_LOG_LOG_TYPE.PROD_PRODUCT_PRODUCT_CHANGE.name());
							LOG.error(e.getMessage());
						}
					}
				}
			}
		}
	}
	
	/**
	 * 根据订单ID获取电子合同
	 * 
	 * @param orderId
	 * @return
	 */
	private List<OrdTravelContract> getOrdTravelContractByOrderId(Long orderId) {
//		OrdTravelContract ordTravelContract = null;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId",orderId);
		List<OrdTravelContract> ordTravelContractList = ordTravelContractService.findOrdTravelContractList(params);
//		if (ordTravelContractList != null && !ordTravelContractList.isEmpty()) {
//			ordTravelContract = ordTravelContractList.get(0);
//		}
		
		return ordTravelContractList;
	}
	
	/**
	 * 根据订单ID更新合同为生效状态
	 * 
	 * @param orderId
	 */
	private void setTravleContractEffectStatusByOrderId(Long orderId) {
		if (orderId != null) {
			OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
			
			if (ordOrder != null) {
				LOG.info("OrderEcontractProcesser.setTravleContractEffectStatusByOrderId: hasInfoAndResourcePass=" + ordOrder.hasInfoAndResourcePass()
						+ ",hasFullPayment=" + ordOrder.hasFullPayment() + ",hasPayed" + ordOrder.hasPayed());
				//后置订单
				if("Y".equals(ordOrder.getTravellerDelayFlag())){
					List<OrdTravelContract> originTravelContractList = getOrdTravelContractByOrderId(orderId);
					if(originTravelContractList != null) {
						for(OrdTravelContract originTravelContract : originTravelContractList) {
							LOG.info("OrderEcontractProcesser.setTravleContractEffectStatusByOrderId:(TravellerDelayFlag)Status=" + originTravelContract.getStatus());
							OrdTravelContract ordTravelContract = new OrdTravelContract();
							if(OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(ordOrder.getPaymentStatus()) &&
									"Y".equals(ordOrder.getTravellerLockFlag()) && "AMPLE".equals(ordOrder.getResourceStatus())) {
								ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
							}else{
								ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
							}
//							ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
							ordTravelContract.setOrdContractId(originTravelContract.getOrdContractId());
							ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, "SYSTEM");
						}
					}
				}else{
					//信息通过同时资源满足、支付金额完成、订单完成
					if (ordOrder.hasInfoAndResourcePass() && ordOrder.hasFullPayment() && ordOrder.hasPayed()) {
						List<OrdTravelContract> originTravelContractList = getOrdTravelContractByOrderId(orderId);
						if(originTravelContractList != null) {
							for(OrdTravelContract originTravelContract : originTravelContractList) {
								LOG.info("OrderEcontractProcesser.setTravleContractEffectStatusByOrderId: Status=" + originTravelContract.getStatus());
								OrdTravelContract ordTravelContract = new OrdTravelContract();
								ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
								ordTravelContract.setOrdContractId(originTravelContract.getOrdContractId());
								ordTravelContract.setOrderId(originTravelContract.getOrderId());
								ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, "SYSTEM");
							}
						}
					}
				}
			}
		}
	}
}
