package com.lvmama.vst.order.service.impl;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;
import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdSmsSendDao;
import com.lvmama.vst.order.dao.OrdSmsTemplateDao;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderSmsNotSendRuleService;
import com.lvmama.vst.order.service.IOrderSmsSendService;
import com.lvmama.vst.order.service.IOrderSmsTemplateService;
import com.lvmama.vst.pet.adapter.ISmsRemoteServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

@Service
public class OrderSmsSendServiceImpl implements IOrderSmsSendService {
	private final static Log LOG=LogFactory.getLog(OrderSmsSendServiceImpl.class);

	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	@Autowired
	ProdProductClientService prodProductClientService;
	/**
	 * 短信服务
	 */
	@Autowired
	ISmsRemoteServiceAdapter smsRemoteService;
	@Autowired
	IOrderLocalService orderLocalService;
	@Autowired
	OrdPersonDao ordPersonDao;
	@Autowired
	IOrderSmsTemplateService orderSmsTemplateService;
	@Autowired
	IOrderSmsNotSendRuleService orderSmsNotSendRuleService;
	@Autowired
	OrdSmsSendDao ordSmsSendDao;
	@Autowired
	OrdSmsTemplateDao OrdSmsTemplateDao;
	@Override
	public String getContent(Long orderId, OrdSmsTemplate.SEND_NODE sendNode){
		if(orderId==null || sendNode==null )
			return null;
		OrdSmsTemplate ordSmsTemplate = this.getOrdSmsTemplate(orderId, sendNode);
		if(ordSmsTemplate==null)
			return null;		
		return ordSmsTemplate.getContent();
	}
	@Override
	public OrdSmsTemplate getOrdSmsTemplate(Long orderId, OrdSmsTemplate.SEND_NODE sendNode){
		if(orderId==null || sendNode==null)
			return null;
		OrdOrder ordOrder =  orderLocalService.queryOrdorderByOrderId(orderId);
		OrdOrderItem ordOrderItem= ordOrder.getMainOrderItem();
		//不存在主订单
		if(ordOrderItem==null){
			LOG.warn("OrderId:"+ordOrder.getOrderId()+"不存在主订单");
			return null;
		}
		//不发送规则
		OrdSmsNotSendRule ordSmsNotSendRule = this.getNotSendRule(ordOrder, ordOrderItem, sendNode);
		//发送模板
		OrdSmsTemplate ordSmsTemplate =null;
		//如果没有存在不发送规则就获取短信模板
		if(ordSmsNotSendRule==null)
			ordSmsTemplate = this.getSendTemplate(ordOrder, ordOrderItem, sendNode);
		if(ordSmsTemplate!=null && ordSmsTemplate.getContent()!=null && ordSmsTemplate.getContent().trim().length()>0){

			//模板内容填充
			ordSmsTemplate.setContent(composeMessage(ordSmsTemplate.getContent(), ordOrder));
		}
		return ordSmsTemplate;
	}
	/**
	 * 模板内容填充
	 * @param content
	 * @param ordOrder
	 * @return
	 */
	private String composeMessage(String content,OrdOrder ordOrder){
		OrdOrderItem ordOrderItem = ordOrder.getMainOrderItem();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		Long productId = null;
		if (ordOrder.getOrdOrderPack() != null) {
			productId = ordOrder.getOrdOrderPack().getProductId();
		} else {
			productId = ordOrderItem.getProductId();
		}
		ProdProduct prodProduct =prodProductClientService.findProdProductById(productId, true, true);
		String regex = "\\$\\{(.+?)\\}";   
		Pattern pattern = Pattern.compile(regex);   
		Matcher matcher = pattern.matcher(content); 
		while (matcher.find()) {   
			String keyField = matcher.group(1);//键名   
			if(OrdSmsTemplate.FIELD.ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField, ordOrder.getOrderId());
			}
			//订单总金额
			else if(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, ordOrder.getOughtAmountYuan());
			}
			//最晚支付时间
			else if(OrdSmsTemplate.FIELD.LATEST_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(ordOrder.getWaitPaymentTime(), "MM.dd HH:mm"));
			}
			/*//到店时间
			else if(OrdSmsTemplate.FIELD.ARRIVAL_TIME.getField().equals(keyField)){
				if(ordOrderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name())!=null){
					dataMap.put(keyField, DateUtil.formatDate(ordOrder.getVisitTime(), "MM.dd")+" "+ordOrderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name()));
				}else{
					dataMap.put(keyField, DateUtil.formatDate(ordOrder.getVisitTime(), "yyyy-MM-dd"));
				}
			}*/
			//游玩/入住日期
			else if(OrdSmsTemplate.FIELD.VISIT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(ordOrder.getVisitTime(), "yyyy-MM-dd"));
			}
			//离店日期
			else if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				List<OrdOrderHotelTimeRate> hotelTimeRates= ordOrderItem.getOrderHotelTimeRateList();
				if(hotelTimeRates!=null){
					Date maxDate = null;
					for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : hotelTimeRates) {
						if(maxDate==null)
							maxDate = ordOrderHotelTimeRate.getVisitTime();
						if(maxDate.before(ordOrderHotelTimeRate.getVisitTime()))
							maxDate = ordOrderHotelTimeRate.getVisitTime();
					}
					if(maxDate==null)
						maxDate = ordOrder.getVisitTime();
					dataMap.put(keyField, DateUtil.formatDate(DateUtil.dsDay_Date(maxDate, 1), "yyyy-MM-dd"));
				}
			}
			//订购数量
			else if(OrdSmsTemplate.FIELD.QUANTITY.getField().equals(keyField)){
				dataMap.put(keyField, ordOrderItem.getQuantity());
			}
			//退款规则
			else if(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField().equals(keyField)){
				dataMap.put(keyField, SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(ordOrder.getCancelStrategy()));
			}
			//取消原因
			else if(OrdSmsTemplate.FIELD.CANCEL_REASON.getField().equals(keyField)){
				dataMap.put(keyField, ordOrder.getReason());
			}
			//最晚保留时间
			else if(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField().equals(keyField)){
				//最晚到底时间
				String[] orderLastArrival = ordOrderItem.getContentValueByKey(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name()).toString().split(":");
				if(orderLastArrival.length>1){
					Calendar visitTime = Calendar.getInstance();
					visitTime.setTime(ordOrderItem.getVisitTime());
					visitTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(orderLastArrival[0]));
					visitTime.set(Calendar.MINUTE, Integer.parseInt(orderLastArrival[1]));
					visitTime.set(Calendar.SECOND, 0);
					visitTime.set(Calendar.MILLISECOND, 0);
					dataMap.put(keyField, DateUtil.formatDate(visitTime.getTime(), "MM.dd HH:mm"));
				}else {
					dataMap.put(keyField, "");
				}
			}
			/*//游玩时间
			else if(OrdSmsTemplate.FIELD.VISIT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(ordOrder.getVisitTime(), "MM.dd"));
			}
			//游玩时间段
			else if(OrdSmsTemplate.FIELD.VISIT_TIME_FROM_TO.getField().equals(keyField)){
				List<OrdOrderHotelTimeRate> hotelTimeRates= ordOrderItem.getOrderHotelTimeRateList();
				if(hotelTimeRates!=null){
					Date maxDate = null;
					for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : hotelTimeRates) {
						if(maxDate==null)
							maxDate = ordOrderHotelTimeRate.getVisitTime();
						if(maxDate.before(ordOrderHotelTimeRate.getVisitTime()))
							maxDate = ordOrderHotelTimeRate.getVisitTime();
					}
					if(maxDate==null)
						maxDate = ordOrder.getVisitTime();
					dataMap.put(keyField, DateUtil.formatDate(ordOrder.getVisitTime(), "MM.dd")+"-"+DateUtil.formatDate(maxDate, "MM.dd"));
				}
			}
			//会员帐号
			else if(OrdSmsTemplate.FIELD.ORDER_USER_NAME.getField().equals(keyField)){
				if(ordOrder.getUserId()!=null){
					UserUser user =   userUserProxy.getUserUserByUserNo(ordOrder.getUserId());
					if(user!=null){
						dataMap.put(keyField, user.getUserName());
					}
				}
			}*/
			//产品信息
			/*else if(OrdSmsTemplate.FIELD.PRODUCT_ID.getField().equals(keyField)){
				dataMap.put(keyField, ordOrderItem.getProductId());
			}*/else if(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField().equals(keyField)){
				if (ordOrder.getOrdOrderPack() != null) {
					dataMap.put(keyField, ordOrder.getOrdOrderPack().getProductName());
				} else {
					dataMap.put(keyField, ordOrderItem.getProductName());
				}
			}else if(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField().equals(keyField)){
				if(prodProduct!=null)
					dataMap.put(keyField, prodProduct.getPropValue().get("address"));
			}else if(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField().equals(keyField)){
				if(prodProduct!=null)
					dataMap.put(keyField, prodProduct.getPropValue().get("telephone"));
			}
			//商品信息
			/*else if(OrdSmsTemplate.FIELD.SUPP_GOODS_ID.getField().equals(keyField)){
				dataMap.put(keyField, ordOrderItem.getSuppGoodsId());
			}*/
			//SUPP_GOODS_NAME
			else if(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField().equals(keyField)){
				dataMap.put(keyField, ordOrderItem.getSuppGoodsName());
			}
			//商品单价
			/*else if(OrdSmsTemplate.FIELD.SUPP_GOODS_PRICE.getField().equals(keyField)){
				List<OrdOrderHotelTimeRate> hotelTimeRates= ordOrderItem.getOrderHotelTimeRateList();
				if(hotelTimeRates!=null){

					List<Map<String, List<Date>>> identicalDates = new ArrayList<Map<String, List<Date>>>();  
					String lastYuan =null;
					List<Date> dateList=null;
					Map<String, List<Date>> dateMap=null;
					
					for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : hotelTimeRates) {
						if(lastYuan==null || lastYuan.equals(ordOrderHotelTimeRate.getPriceYuan())){
							if(lastYuan==null){
								dateList = new ArrayList<Date>();
								dateMap = new HashMap<String, List<Date>>();
								identicalDates.add(dateMap);
							}
							dateList.add(ordOrderHotelTimeRate.getVisitTime());
							lastYuan = ordOrderHotelTimeRate.getPriceYuan();
							dateMap.put(lastYuan, dateList);
						}else {
							dateList = new ArrayList<Date>();
							dateMap = new HashMap<String, List<Date>>();
							identicalDates.add(dateMap);
							dateList.add(ordOrderHotelTimeRate.getVisitTime());
							lastYuan = ordOrderHotelTimeRate.getPriceYuan();
							dateMap.put(lastYuan, dateList);
						}
					}
					StringBuffer priceContent = new StringBuffer();
					int idx = 0;
					for (Map<String, List<Date>> identicalDateMap : identicalDates) {
						Iterator<Entry<String, List<Date>>> entryIt = identicalDateMap.entrySet().iterator();
						while (entryIt.hasNext()) {
							Entry<String, List<Date>> entry= entryIt.next();
							if(idx>0){
								priceContent.append("，");
							}
							if(entry.getValue().size()==1){
								priceContent.append("订单"+Integer.parseInt(DateUtil.formatDate(entry.getValue().get(0), "dd"))+"号，价格为"+entry.getKey()+"元");
							}else if(entry.getValue().size()>1){
								priceContent.append("订单"+Integer.parseInt(DateUtil.formatDate(entry.getValue().get(0), "dd"))
										+"-"+Integer.parseInt(DateUtil.formatDate(entry.getValue().get(entry.getValue().size()-1), "dd"))
										+"号，价格均为"+entry.getKey()+"元");
							}
							idx++;
						}
					}
					dataMap.put(keyField,priceContent);
				}else{
					dataMap.put(keyField,ordOrderItem.getPriceYuan()+"元");
				}
			}*/
			//最晚支付时间
			else if(OrdSmsTemplate.FIELD.LATEST_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, ordOrder.getWaitPaymentTime());
			}
			//客服电话
			else if(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField().equals(keyField)){
				dataMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE);
			}
			//客户端签名
			else if(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField().equals(keyField)){
				dataMap.put(keyField, Constant.CLIENT_SIGN);
			}else{
				LOG.warn("短信中没有定义内容："+keyField);
			}
		}
		return StringUtil.composeMessage(content, dataMap);
	}
	@Override
	public void sendSmsByCustom(Long orderId, String content, String operate,
			String mobile) throws BusinessException {
		if(orderId==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单号为空不能发送");
		if(content==null || "".equals(content.trim()))
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0003,String.valueOf(orderId)+",内容为空不能发送");
		if(mobile==null ||"".equals(mobile)){

			Map<String, Object> params = new HashMap<String, Object>();
			params.clear();
			params.put("object", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			params.put("objectId", orderId);
			params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			List<OrdPerson> ordPersons= ordPersonDao.findOrdPersonList(params);
			OrdPerson ordPerson = null;
			if( ordPersons.size()>0)
				ordPerson = ordPersons.get(0);
			//没有联系人时直接返回
			if(ordPerson==null || ordPerson.getMobile()==null || "".equals(ordPerson.getMobile()))
				return ;
			mobile = ordPerson.getMobile();
		}
		if(mobile==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0003,String.valueOf(orderId)+",手机号为空不能发送");

		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate(operate);
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		try {
			smsRemoteService.sendSms(orderId, content,mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
		} catch (Exception e) {
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0002,String.valueOf(orderId),content);
		}
	}
	@Override
	public Long sendSms(Long orderId, OrdSmsTemplate.SEND_NODE sendNode) throws BusinessException{
		return this.sendSms(orderId, sendNode,"system");
	}
	@Override
	public Long sendSms(Long orderId, OrdSmsTemplate.SEND_NODE sendNode,String operate) throws BusinessException{
		if(orderId==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单号为空不能发送");
		if(sendNode==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"发送节点为空不能发送");
		LOG.info("OrderSmsSendServiceImpl.sendSms:orderId=" + orderId + ",SEND_NODE=" + sendNode.name() + ",operate=" + operate);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.clear();
		params.put("object", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("objectId", orderId);
		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		List<OrdPerson> ordPersons= ordPersonDao.findOrdPersonList(params);
		OrdPerson ordPerson = null;
		if( ordPersons.size()>0)
			ordPerson = ordPersons.get(0);
		//没有联系人时直接返回
		if(ordPerson==null || ordPerson.getMobile()==null || "".equals(ordPerson.getMobile()))
			return null;
		String content= this.getContent(orderId, sendNode);
		if(content==null )
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单ID=" + orderId + ",发送节点：" + sendNode.name() + "，获取不到发送内容不能发送");

		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(ordPerson.getMobile());
		record.setOperate(operate);
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		try {
			smsRemoteService.sendSms(orderId, content,ordPerson.getMobile());
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
		} catch (Exception e) {
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),sendNode.getCnName());
		}
		return record.getSmsId();
	}
	@Override
	public void reSendSms(Long smsId,String operate) throws BusinessException {
		OrdSmsSend ordSmsSend =ordSmsSendDao.selectByPrimaryKey(smsId);
		if(ordSmsSend==null)
			return;
		ordSmsSend.setOperate(operate);
		try {
			smsRemoteService.sendSms(ordSmsSend.getOrderId(), ordSmsSend.getContent(),ordSmsSend.getMobile());
			ordSmsSend.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.updateByPrimaryKey(ordSmsSend);
		} catch (Exception e) {
			ordSmsSend.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.updateByPrimaryKey(ordSmsSend);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0002,String.valueOf(ordSmsSend.getOrderId()),ordSmsSend.getContent());
		}
		
	}
	/**
	 * 发送规则
	 * @param ordOrder
	 * @param ordOrderItem
	 * @param sendNode
	 * @return
	 */
	private OrdSmsTemplate getSendTemplate(OrdOrder ordOrder ,OrdOrderItem ordOrderItem,OrdSmsTemplate.SEND_NODE sendNode){
		OrdSmsTemplate ordSmsTemplate = null;

		//**************
		//指定渠道和供应商的规则 start
		//**************
		//下单时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,true,true,true);
		//全部时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,false,true,true);
		//**************
		//指定渠道和供应商的规则 end
		//**************

		//**************
		//指定渠道的规则（无供应商） start
		//**************
		//下单时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,true,true,false);
		//全部时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,false,true,false);
		//**************
		//指定渠道的规则（无供应商） end
		//**************


		//**************
		//无指定渠道，指定供应商 start
		//**************
		//下单时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,true,false,true);
		//全部时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,false,false,true);
		//**************
		//无指定渠道，指定供应商 end
		//**************
		

		//**************
		//无指定渠道，无指定供应商 start
		//**************
		//下单时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,true,false,false);
		//全部时间
		if(ordSmsTemplate==null)
			ordSmsTemplate = this.getOrdSmsTemplate(ordOrder,ordOrderItem, sendNode,false,false,false);
		//**************
		//无指定渠道，无指定供应商 end
		//**************
		
		return ordSmsTemplate;
	}
	/**
	 * 
	 * @param ordOrder
	 * @param ordOrderItem
	 * @param sendNode
	 * @param orderTime
	 * @param haveDis
	 * @param haveSupplier
	 * @return
	 */
	private OrdSmsTemplate getOrdSmsTemplate(OrdOrder ordOrder ,OrdOrderItem ordOrderItem,OrdSmsTemplate.SEND_NODE sendNode,boolean orderTime,boolean haveDis,boolean haveSupplier){

		Map<String, Object> params = new HashMap<String, Object>();
		if (ordOrder.getOrdOrderPack() != null) {
			params.put("categoryId", ordOrder.getOrdOrderPack().getCategoryId());
		} else {
			params.put("categoryId", ordOrderItem.getCategoryId());
		}
		params.put("sendNode", sendNode.name());
		//是否需要渠道对应
		if(haveDis){
			params.put("distributorId", ordOrder.getDistributorId());
		}else{
			params.put("distributorId", "-1");//全部时为-1
			//params.put("distributorIdIsNull","1" );
		}
		//是否需要供应商对应
		if(haveSupplier){
			params.put("supplierId", ordOrderItem.getSupplierId());
		}else{
			params.put("supplierIdIsNull", "1");
		}
		//根据下单时间
		if(orderTime){
			Calendar calendar = Calendar.getInstance();			
			calendar.setTime(ordOrder.getCreateTime());
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			//白天时间
			if(hour>=9 && hour<=18){
				params.put("orderTime", OrdSmsTemplate.ORDER_TIME.DAY.name());
			}
			//晚上时间
			else if(hour<9 || hour>18){
				params.put("orderTime", OrdSmsTemplate.ORDER_TIME.NIGHT.name());
			}
		}
		//全部下单时间
		else{
			params.put("orderTime", OrdSmsTemplate.ORDER_TIME.ALL.name());

		}
		params.put("valid", "Y");
		LOG.info("getOrdSmsTemplate orderId = "+ordOrder.getOrderId()+" sendNode="+(sendNode==null ? "" :sendNode.name()));
		List<OrdSmsTemplate> ordSmsTemplates =  orderSmsTemplateService.findOrdSmsTemplateList(params);
		//发送策略
		if(ordSmsTemplates.size()>0)
			return ordSmsTemplates.get(0);
		return null;
	}

	/**
	 * 不发送规则
	 * @param ordOrder
	 * @param ordOrderItem
	 * @param sendNode
	 * @return
	 */
	private OrdSmsNotSendRule getNotSendRule(OrdOrder ordOrder ,OrdOrderItem ordOrderItem,OrdSmsTemplate.SEND_NODE sendNode){
		OrdSmsNotSendRule ordSmsNotSendRule = null;

		//**************
		//指定渠道和供应商的规则 start
		//**************
		if(ordSmsNotSendRule==null)
			ordSmsNotSendRule = this.getOrdSmsNotSendRule(ordOrder,ordOrderItem, sendNode,true,true);
		//**************
		//指定渠道和供应商的规则 end
		//**************

		//**************
		//指定渠道的规则（无供应商） start
		//**************
		if(ordSmsNotSendRule==null)
			ordSmsNotSendRule = this.getOrdSmsNotSendRule(ordOrder,ordOrderItem, sendNode,true,false);
		//**************
		//指定渠道的规则（无供应商） end
		//**************


		//**************
		//无指定渠道，指定供应商 start
		//**************
		if(ordSmsNotSendRule==null)
			ordSmsNotSendRule = this.getOrdSmsNotSendRule(ordOrder,ordOrderItem, sendNode,false,true);
		//**************
		//无指定渠道，指定供应商 end
		//**************
		

		//**************
		//无指定渠道，无指定供应商 start
		//**************
		if(ordSmsNotSendRule==null)
			ordSmsNotSendRule = this.getOrdSmsNotSendRule(ordOrder,ordOrderItem, sendNode,false,false);
		//**************
		//无指定渠道，无指定供应商 end
		//**************
		
		return ordSmsNotSendRule;
	}
	/**
	 * 
	 * @param ordOrder
	 * @param ordOrderItem
	 * @param sendNode
	 * @param orderTime
	 * @param haveDis
	 * @param haveSupplier
	 * @return
	 */
	private OrdSmsNotSendRule getOrdSmsNotSendRule(OrdOrder ordOrder ,OrdOrderItem ordOrderItem,OrdSmsTemplate.SEND_NODE sendNode,boolean haveDis,boolean haveSupplier){

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryId", ordOrderItem.getCategoryId());
		params.put("sendNode", sendNode.name());
		//是否需要渠道对应
		if(haveDis){
			params.put("distributorId", ordOrder.getDistributorId());
		}else{
			params.put("distributorIdIsNull","1" );
		}
		//是否需要供应商对应
		if(haveSupplier){
			params.put("supplierId", ordOrderItem.getSupplierId());
		}else{
			params.put("supplierIdIsNull", "1");
		}
		List<OrdSmsNotSendRule> ordSmsNotSendRules =  orderSmsNotSendRuleService.findOrdSmsNotSendRuleList(params);
		//不发送策略
		if(ordSmsNotSendRules.size()>0)
			return ordSmsNotSendRules.get(0);
		return null;
	}

	@Override
	public int findOrdSmsSendCount(Map<String, Object> params) throws BusinessException {
		try {
			return ordSmsSendDao.getTotalCount(params);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
	}

	@Override
	public List<OrdSmsSend> findOrdSmsSendList(Map<String, Object> params)
			throws BusinessException {
		
		try {
			return ordSmsSendDao.findOrdSmsSendList(params);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
	}
	@Override
	public String getSmsContent(String templateId,Map<String, Object> parameters) {
		OrdSmsTemplate ordSmsTemplate = OrdSmsTemplateDao.selectByPrimaryKey(Long.parseLong(templateId));
		String content = null;
		if(ordSmsTemplate != null){
		   try {
			   content = StringUtil.composeMessage(ordSmsTemplate.getContent(), parameters);
			} catch (Exception e) {
			}
		}else{
			content= null;
		}
		return content;
	}
}
