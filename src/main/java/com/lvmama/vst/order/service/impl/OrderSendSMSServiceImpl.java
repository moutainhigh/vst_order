package com.lvmama.vst.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.sms.SmsMMS;
import com.lvmama.comm.stamp.vo.StampCode;
import com.lvmama.comm.stamp.vo.StampOrderDetails;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.dest.api.vst.orderproduct.service.IHotelOrderProductQueryService;
import com.lvmama.dest.api.vst.orderproduct.vo.HotelOrdOrderProductVO;
import com.lvmama.dest.api.vst.prod.service.IHotelProductQrVstApiService;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.order.enums.OrderEnum;
import com.lvmama.order.enums.ticket.TicketEnums;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vo.comm.OrderVo;
import com.lvmama.order.vo.comm.person.OrdPersonVo;
import com.lvmama.price.api.capacity.model.vo.SuppGoodsAdditionVo;
import com.lvmama.price.api.capacity.service.SuppGoodsAdditionApiService;
import com.lvmama.prod.router.service.RouterService;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.DestContentClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsAdditionClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsBusClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.passport.service.PassportSendSmsService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.client.wifi.service.WifiClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddition;
import com.lvmama.vst.back.goods.po.SuppGoodsBus;
import com.lvmama.vst.back.goods.vo.SuppGoodsTicketDetailVO;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppSpecialSmsPo;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.back.wifi.po.WifiPickingPoint;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.OrderUtils;
import com.lvmama.vst.neworder.order.sms.OrderSmsPo;
import com.lvmama.vst.neworder.order.sms.ProductSmsPo;
import com.lvmama.vst.neworder.order.sms.SmsContentCreator;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.processer.sms.*;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.passport.util.VirtualCodeUtil;
import com.lvmama.vst.pet.adapter.ISmsRemoteServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class OrderSendSMSServiceImpl implements IOrderSendSmsService {
	private final static Log LOG=LogFactory.getLog(OrderSendSMSServiceImpl.class);

	@Autowired
	IOrdSmsReSendService ordSmsReSendService;
	@Autowired
	OrdPersonDao ordPersonDao;
	@Autowired
	ISmsRemoteServiceAdapter smsRemoteService;
	@Autowired
	OrdSmsSendDao ordSmsSendDao;
	@Autowired
	IOrderLocalService orderLocalService;
	@Autowired
	IOrderSmsTemplateService orderSmsTemplateService;
	@Autowired
	OrdOrderItemDao ordOrderItemDao;
	@Autowired
	OrdOrderDao ordOrderDao;
	@Autowired
	SuppGoodsClientService suppGoodsClientService;
	@Autowired
	SuppSupplierClientService SuppSupplierClientService;
	@Autowired
	OrdItemPersonRelationDao ordItemPersonRelationDao;
	@Autowired
	ProdProductClientService prodProductClientService;
	@Autowired
    DestContentClientService destContentClientRemote;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	@Autowired
	private SuppGoodsAdditionClientService suppGoodsAdditionClientRemote;
	@Autowired 
	private PassportSendSmsService passportSendSmsServiceRemote;
	@Autowired
	private OrdFormInfoDao ordFormInfoDao;
	@Autowired
	SuppSupplierClientService suppSupplierClientService;

    @Autowired
    IOrderSendWechatService orderSendWechatService;
	
	@Autowired
	private ISupplierOrderHandleService supplierOrderHandleService;
	
	@Autowired
	OrdOrderWifiPickingPointDao ordOrderWifiPickingPointDao;
	@Autowired
	private WifiClientService wifiClientService;
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	
	@Autowired
	private OrdPassCodeDao ordPassCodeDao;
	
	@Autowired
	private ApiSuppOrderService  suppOrderClientService;
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentService;
	
	@Autowired
	private IHotelOrderProductQueryService  hotelOrderProductQueryRemote;
	
	@Autowired
	private IHotelProductQrVstApiService  hotelProductQrVstApiServiceRemote;
	
	@Autowired
	private IHotelGoodsQueryVstApiService  hotelGoodsQueryVstApiServiceRemote;
	
	@Resource
	private DestHotelAdapterUtils destHotelAdapterUtils;
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;

	@Autowired
	private RouterService routerService;
	
	@Autowired
	private SuppGoodsBusClientService suppGoodsBusClientService;
	
	@Autowired
	private ProdProductBranchClientService prodProductBranchClientService;

	@Autowired
	private SuppGoodsAdditionApiService suppGoodsAdditionApiServiceRemote;
	

	@Override
	public Long sendSms(Long orderId, SEND_NODE sendNode)
			throws BusinessException {
		return sendSms(orderId, sendNode,"system");
	}
	
	public Map<String,OrdPerson> getOrdPersons(Long orderId, SEND_NODE sendNode){
		LOG.info("OrderSendSmsServiceImpl.getOrdPersons:orderId=" + orderId + ",SEND_NODE=" + sendNode.name());
		
		Map<String,OrdPerson> opMap = new HashMap<String, OrdPerson>();
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.clear();
		params.put("object", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("objectId", orderId);
		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		List<OrdPerson> ordPersons= ordPersonDao.findOrdPersonList(params);
		
		OrdPerson ordPerson = null;
		if( ordPersons.size()>0){
			ordPerson = ordPersons.get(0);
		}
		
		params.clear();
		params.put("object", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("objectId", orderId);
		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
		List<OrdPerson> ordPersons1= ordPersonDao.findOrdPersonList(params);
		OrdPerson ordPerson1 = null;
		if( ordPersons1.size()>0){
			ordPerson1 = ordPersons1.get(0);
		}
		
		//没有联系人时直接返回
		if(ordPerson==null&&ordPerson1==null){
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单ID=" + orderId + ",发送节点：" + sendNode.name() + "，无发送手机号");
		}
		if(ordPerson != null){
			opMap.put(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name(), ordPerson);
		}
		if(ordPerson1 != null){
			opMap.put(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name(), ordPerson1);
		}
		return opMap;
	}
	
	@Override
	public Long sendSms(Long orderId, SEND_NODE sendNode, String operate)
			throws BusinessException {
		if(orderId==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单号为空不能发送");
		if(sendNode==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"发送节点为空不能发送");
		LOG.info("OrderSendSmsServiceImpl.sendSms:orderId=" + orderId + ",SEND_NODE=" + sendNode.name() + ",operate=" + operate);
		
		String mobile = null;
		Map<String,OrdPerson> opMap = getOrdPersons(orderId, sendNode);
		if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
			mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getMobile();
		}
		if(mobile == null){
			if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()) != null){
				mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()).getMobile();
			}
		}

		//门票发送短信手机号码选取的特殊逻辑
		OrdOrder ordOrder = orderLocalService.queryOrdorderByOrderId(orderId);
		mobile = selelctMobileForTicket(orderId, mobile, opMap,ordOrder);
		String content= getContent2(orderId, sendNode,ordOrder);
		//分销部分渠道屏蔽短信
		content = this.shieldMessages(orderId, ordOrder, content);
		if(content==null ){
			LOG.error("OrderSendSmsServiceImpl.sendSms:OrderId:" + orderId + "短信发送失败,发送节点" + sendNode.name() + ",获取不到发送内容不能发送");
			return null;
		}
		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate(operate);
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		try {
			/* update by xiexun 修改后将传入发送节点
			 * smsRemoteService.sendSms(orderId, content,mobile);*/
			smsRemoteService.sendSms(orderId,sendNode.getCode(),content,mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
			
			/* 发送短信的同时发送微信  */
			LOG.info("start OrderSendSmsServiceImpl.sendSms:OrderId:" + orderId + "sms send Over,and wechat begining" + sendNode.name() + "the sms sended phoneNo" + mobile);
			sendWeChat(orderId, sendNode, mobile);
			LOG.info("end OrderSendSmsServiceImpl.sendSms:OrderId:" + orderId);
			
		} catch (Exception e) {
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			ordSmsReSendService.insert(beanCopyForOrdSmsReSend(record));
			LOG.info("order sms exception ="+ExceptionUtil.getExceptionDetails(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),sendNode.getCnName());
		}
		return record.getSmsId();
	}
	
	//机酒自由行产品，在资审通过、凭证确认且支付成功后发送防诈骗短信
	@Override
	public Long sendPreventCheatSms(Long orderId)
			throws BusinessException {
		if(orderId==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0004,"订单号为空不能发送");
		LOG.info("OrderSendSmsServiceImpl.sendSms:orderId=" + orderId + "(防诈骗短信)");
		
		SEND_NODE sendNode=OrdSmsTemplate.SEND_NODE.valueOf(OrdSmsTemplate.SEND_NODE.PAYMENT_VERIFIED_CERTIFIED_ROUTE_FLIGHT_HOTEL.name());
		
		String mobile = null;
		Map<String,OrdPerson> opMap = getOrdPersons(orderId, sendNode);
		if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
			mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getMobile();
		}
		if(mobile == null){
			if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()) != null){
				mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()).getMobile();
			}
		}

		String content= "友情提示：近期有不法分子冒用驴妈妈名义，谎称航班变动、取消，要求旅客提供身份证，银行卡等信息，请旅客朋友收到此类信息，切勿提供相关信息，务必拨打驴妈妈客服电话10106060-1-6核实航班信息。";

		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate("system");
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		try {
			smsRemoteService.sendSms(orderId, content,mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
			
		} catch (Exception e) {
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			ordSmsReSendService.insert(beanCopyForOrdSmsReSend(record));
			LOG.info("order sms exception ="+ExceptionUtil.getExceptionDetails(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),sendNode.getCnName());
		}
		return record.getSmsId();
	}

	private void sendWeChat(Long orderId, SEND_NODE sendNode, String mobile) {
		try {
            orderSendWechatService.sendSms(orderId, mobile, OrdWechatTemplate.SendNode.getSendNodeObj(sendNode.getCode()));
        } catch (Exception e) {
            LOG.info("weChat Excpetion is="+ ExceptionUtil.getExceptionDetails(e));
        }
	}

	private String selelctMobileForTicket(Long orderId, String mobile, Map<String, OrdPerson> opMap,OrdOrder ordOrder) {
		if (orderId != null) {

			Long categoryId = ordOrder.getCategoryId();
			if (BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() == categoryId
					|| BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() == categoryId
					|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId() == categoryId) {
				mobile = null;
				if (opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()) != null) {
					mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()).getMobile();
				}
				if (mobile == null) {
					if (opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null) {
						mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getMobile();
					}
				}
			}
		}
		return mobile;
	}

	public String getContent2(Long orderId, SEND_NODE sendNode,OrdOrder ordOrder) {
		if(orderId==null || sendNode==null )
			return null;
//		OrdOrder ordOrder =  orderLocalService.queryOrdorderByOrderId(orderId);
		OrdOrderItem ordOrderItem= ordOrder.getMainOrderItem();
		//不存在主订单
		if(ordOrderItem==null){
			LOG.warn("OrderId:"+ordOrder.getOrderId()+"不存在主订单");
			return null;
		}
		//立式设备自助购票不发送短信
		if(ordOrder.getDistributorId()!=null && ordOrder.getDistributorId().equals(Constant.SELF_SERVICE_SELL)){
			return null;
		}
		OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, sendNode);
		if(ordSmsTemplate!=null && ordSmsTemplate.getContent()!=null && ordSmsTemplate.getContent().trim().length()>0){
			//模板内容填充
			LOG.warn("方法:OrderSendSMSServiceImpl——getContent" + "OrderId:" + orderId + "找到短信模板:" + sendNode.name());
			ordSmsTemplate.setContent(composeMessage(ordSmsTemplate.getContent(), ordOrder,sendNode));
		}
		if(ordSmsTemplate==null){
			LOG.warn("方法:OrderSendSMSServiceImpl——getContent" + "OrderId:" + orderId + "没有找到短信模板:" + sendNode.name());
			return null;
		}

		if("ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND".equals(sendNode.getCode())){
			if("".equals(ordSmsTemplate.getContent())||ordSmsTemplate.getContent()==null){
				return null;
			}
		}

		return ordSmsTemplate.getContent();
	}


	@Override
	public String getContent(Long orderId, SEND_NODE sendNode) {
		if(orderId==null || sendNode==null )
			return null;
		OrdOrder ordOrder =  orderLocalService.queryOrdorderByOrderId(orderId);
		OrdOrderItem ordOrderItem= ordOrder.getMainOrderItem();
		//不存在主订单
		if(ordOrderItem==null){
			LOG.warn("OrderId:"+ordOrder.getOrderId()+"不存在主订单");
			return null;
		}
		//立式设备自助购票不发送短信
		if(ordOrder.getDistributorId()!=null && ordOrder.getDistributorId().equals(Constant.SELF_SERVICE_SELL)){
			return null;
		}
		OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, sendNode);
		if(ordSmsTemplate!=null && ordSmsTemplate.getContent()!=null && ordSmsTemplate.getContent().trim().length()>0){
			//模板内容填充
			LOG.warn("方法:OrderSendSMSServiceImpl——getContent" + "OrderId:" + orderId + "找到短信模板:" + sendNode.name());
			ordSmsTemplate.setContent(composeMessage(ordSmsTemplate.getContent(), ordOrder,sendNode));
		}
		if(ordSmsTemplate==null){
			LOG.warn("方法:OrderSendSMSServiceImpl——getContent" + "OrderId:" + orderId + "没有找到短信模板:" + sendNode.name());
			return null;	
		}
		
		if("ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND".equals(sendNode.getCode())){
			if("".equals(ordSmsTemplate.getContent())||ordSmsTemplate.getContent()==null){
				return null;	
			}
		}
		
		return ordSmsTemplate.getContent();
	}
	
	@Override
	public OrdSmsTemplate getOrdSmsTemplate(Long orderId, SEND_NODE sendNode) {
		Map<String, Object> params = new HashMap<String, Object>();
		LOG.info("OrderSendSMSServiceImpl——getOrdSmsTemplate" + "OrderId:" + orderId + "NODE IS:" + sendNode.name());
		params.put("sendNode", sendNode.name());
		params.put("valid", "Y");
		List<OrdSmsTemplate> ordSmsTemplates =  orderSmsTemplateService.findOrdSmsTemplateList(params);
		//发送策略
		if(ordSmsTemplates.size()>0){
			return ordSmsTemplates.get(0);
		}else{
			LOG.warn("方法:OrderSendSMSServiceImpl——getOrdSmsTemplate" + "OrderId:" + orderId + "没有找到短信模板:" + sendNode.name());
			return null;
		}
	}
	
	private String getEmail(OrdOrder order){
		String email = order.getContactPerson().getEmail();
		if(StringUtils.isNotEmpty(email)){
			return email;
		}
		if(CollectionUtils.isNotEmpty(order.getOrdTravellerList())){
			for(OrdPerson person :order.getOrdTravellerList()){
				if(StringUtils.isNotEmpty(person.getEmail())){
					return email;
				}
			}
		}
		return null;
	}	
	
	private Map<String,String> getGoodsCustomSms(OrdOrder order){
		Map<String,String> goodsCustomSmsMap = new HashMap<String, String>();
		// 打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		fillGoodsCustomSms(pack.getOrderItemList(), goodsCustomSmsMap);
	    	}
	    }
	    // 非打包
	    fillGoodsCustomSms(order.getOrderItemList(), goodsCustomSmsMap);
		return goodsCustomSmsMap;
	}
	
	private void fillGoodsCustomSms(List<OrdOrderItem> ordOrderItemrderList, Map<String,String> goodsCustomSmsMap){
		if(ordOrderItemrderList != null && ordOrderItemrderList.size() > 0){
			for(OrdOrderItem item : ordOrderItemrderList){
				SuppGoodsAddition suppGoodsAddition= suppGoodsAdditionClientRemote.selectByPrimaryKey(item.getSuppGoodsId());
				if(suppGoodsAddition != null && suppGoodsAddition.getSmsContent() != null){
					goodsCustomSmsMap.put(item.getOrderItemId().toString(), suppGoodsAddition.getSmsContent());
				}else{
					goodsCustomSmsMap.put(item.getOrderItemId().toString(),"");
				}
			}
		}
	}
	
	
	/**
	 * 填充订单中门票取票地址和入园方式Map
	 * @param order
	 * @param addressMap
	 * @param enterStyleMap
	 */
	private void fillTicketAddressAndEnterStyle(OrdOrder order, Map<String,String> addressMap, Map<String,String> enterStyleMap){
		if(addressMap != null && enterStyleMap != null){
			// 打包
		    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
		    	for(OrdOrderPack pack : order.getOrderPackList()){
		    		fillTicketAddressAndEnterStyle(pack.getOrderItemList(), addressMap, enterStyleMap);
		    	}
		    }
		    // 非打包
		    fillTicketAddressAndEnterStyle(order.getOrderItemList(), addressMap, enterStyleMap);
		}
	}
	
	/**
	 * 填充订单中门票取票地址，入园方式Map，取票时间
	 * @param order
	 * @param addressMap
	 * @param enterStyleMap
	 * @param timeMap
	 */
	private void fillTicketAddressAndEnterStyleAndTime(OrdOrder order,
			Map<String, String> addressMap, Map<String, String> enterStyleMap,
			Map<String, String> timeMap) {
		// 打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		fillTicketAddressAndEnterStyleAndTime(pack.getOrderItemList(),addressMap,enterStyleMap,timeMap);
	    	}
	    }
	    // 非打包
	    fillTicketAddressAndEnterStyleAndTime(order.getOrderItemList(),addressMap,enterStyleMap,timeMap);
	}
	/**
	 * 填充订单中门票取票地址和入园方式Map，和通关码
	 * @param order
	 * @param addressMap
	 * @param enterStyleMap
	 * @param auxiliaryCodeMap
	 */
	private void fillTicketAddressAndEnterStyleAndCode(OrdOrder order, Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> auxiliaryCodeMap){
		if(addressMap != null && enterStyleMap != null){
			// 打包
		    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
		    	for(OrdOrderPack pack : order.getOrderPackList()){
		    		fillTicketAddressAndEnterStyle(pack.getOrderItemList(), addressMap, enterStyleMap);
		    		fillTicketCode(pack.getOrderItemList(), auxiliaryCodeMap);
		    	}
		    }
		    // 非打包
		    fillTicketAddressAndEnterStyle(order.getOrderItemList(), addressMap, enterStyleMap);
		    fillTicketCode(order.getOrderItemList(), auxiliaryCodeMap);
		}
	}
	
	
	private void fillTicketAddressAndEnterStyleAndCodeAndTime(OrdOrder order, Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> auxiliaryCodeMap){
		if(addressMap != null && enterStyleMap != null){
			// 打包
		    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
		    	for(OrdOrderPack pack : order.getOrderPackList()){
		    		fillTicketAddressAndEnterStyleAndTime(pack.getOrderItemList(), addressMap, enterStyleMap, timeMap);
		    		fillTicketCode(pack.getOrderItemList(), auxiliaryCodeMap);
		    	}
		    }
		    // 非打包
		    fillTicketAddressAndEnterStyleAndTime(order.getOrderItemList(), addressMap, enterStyleMap,timeMap);
		    fillTicketCode(order.getOrderItemList(), auxiliaryCodeMap);
		}
	}
	
	/**
	 * 填充门票取票地址和入园方式Map
	 * @param ordOrderItemList
	 * @param addressMap
	 * @param enterStyleMap
	 */
	private void fillTicketAddressAndEnterStyle(List<OrdOrderItem> ordOrderItemList, Map<String,String> addressMap, Map<String,String> enterStyleMap){
		if(ordOrderItemList != null && ordOrderItemList.size() > 0){
			for(OrdOrderItem item : ordOrderItemList){
				//取到入园地址和入园方式
				if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
					SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
					if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
						addressMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
						enterStyleMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getEnterStyle() != null) ? sgtdv.getSuppGoodsDesc().getEnterStyle() : "");
					}else{
						addressMap.put(item.getOrderItemId().toString(), "");
						enterStyleMap.put(item.getOrderItemId().toString(), "");
					}
				}
			}
		}
	}
	
	
	/**
	 * 填充门票取票地址和入园方式Map
	 * @param ordOrderItemList
	 * @param addressMap
	 * @param enterStyleMap
	 */
	private void fillTicketAddressAndEnterStyleAndTime(List<OrdOrderItem> ordOrderItemList, Map<String,String> addressMap, Map<String,String> enterStyleMap , Map<String,String> timeMap){
		if(ordOrderItemList != null && ordOrderItemList.size() > 0){
			for(OrdOrderItem item : ordOrderItemList){
				//取到入园地址和入园方式
				if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
					SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
					if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
						addressMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
						addressMap.put(item.getOrderItemId().toString()+"visitAddress", (sgtdv.getSuppGoodsDesc().getVisitAddress() != null) ? sgtdv.getSuppGoodsDesc().getVisitAddress() : "");
						enterStyleMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getEnterStyle() != null) ? sgtdv.getSuppGoodsDesc().getEnterStyle() : "");
						timeMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getChangeTime()!=null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
						timeMap.put(item.getOrderItemId().toString()+"passLimitTime", (sgtdv.getSuppGoodsDesc().getPassLimitTime()!=null) ? fillPassLimitTime(sgtdv.getSuppGoodsDesc().getPassLimitTime()) : "");

					}else{
						addressMap.put(item.getOrderItemId().toString(), "");
						addressMap.put(item.getOrderItemId().toString()+"visitAddress", "");
						enterStyleMap.put(item.getOrderItemId().toString(), "");
						timeMap.put(item.getOrderItemId().toString(), "");
					}
				}
			}
		}
	}
	
	/**
	 * 填充门票通关码
	 * @param ordOrderItemList
	 * @param auxiliaryCodeMap
	 */
	private void fillTicketCode(List<OrdOrderItem> ordOrderItemList, Map<String,String> auxiliaryCodeMap){
		try{
			if(ordOrderItemList != null && ordOrderItemList.size() > 0){
				for(OrdOrderItem item : ordOrderItemList){
					//取到入园地址和入园方式
					if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
						if(item.isSupplierOrderItem()){//对接
							OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
							if(ordPassCode != null && "LVMAMA".equals(ordPassCode.getSendSms()) 
									&& (StringUtils.isNotEmpty(ordPassCode.getCode()) || StringUtils.isNotEmpty(ordPassCode.getAddCode()))
									&& (ordPassCode.getServiceId()!= 11L && ordPassCode.getServiceId()!=66L) ){//长隆
								String addCodeString = buildAddCode(ordPassCode);
								auxiliaryCodeMap.put(item.getOrderItemId().toString(), addCodeString);
							}
						}
					}
				}
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}
	
	private String buildAddCode(OrdPassCode ordPassCode){
		String content = "";
		StringBuilder sb = new StringBuilder("入园凭证");
    	if("BASE64".equalsIgnoreCase(ordPassCode.getCode())){ // 发送彩信的，此时该字段不表示通关码
			if(StringUtils.isEmpty(ordPassCode.getAddCode())){
				sb.append("");
			}else{
				sb.append("(辅助码): "+ordPassCode.getAddCode()+"。");
			}
			content = sb.toString();
		}else{ // ordPassCode.getCode()表示通关码
			if(StringUtils.isEmpty(ordPassCode.getCode())&&StringUtils.isEmpty(ordPassCode.getAddCode())){
				sb.append("");
			}else{
				if(StringUtils.isNotEmpty(ordPassCode.getCode())){
					sb.append("(取票码): ").append(ordPassCode.getCode()).append(" ").append(",");
				}
				
				if(StringUtils.isNotEmpty(ordPassCode.getAddCode())&&
						!ordPassCode.getAddCode().equals(ordPassCode.getCode())){
					sb.append("(辅助码): ").append(ordPassCode.getAddCode()).append(" ").append(",");
				}
				content = sb.substring(0, sb.length()-1).trim()+"。";
			}
		}
    	return content;
    }
	/**
	 * wifi短信内容
	 * @param ordOrderItemList
	 * @param smsParm
	 */
	private  void fillWifiSmsInfo(List<OrdOrderItem> ordOrderItemList, Map<String,Object> smsParm){
		try {
			if(ordOrderItemList != null && ordOrderItemList.size() > 0){
				for(OrdOrderItem item : ordOrderItemList){
					if(OrderUtil.isWifiCategory(item)&&ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(OrderUtil.getProductType(item))){
						Map<String,Object> params = new HashMap<String, Object>();
						params.put("orderItemId",item.getOrderItemId());
						smsParm.put(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField(),item.getSuppGoodsName());
						String startDay = item.getContentStringByKey(OrderEnum.ORDER_WIFI_TYPE.lease_startDay.name());
						String endDay = item.getContentStringByKey(OrderEnum.ORDER_WIFI_TYPE.lease_endDay.name());
						smsParm.put(OrdSmsTemplate.FIELD.WIFI_QUANTITY.getField(),item.getQuantity());
						smsParm.put(OrdSmsTemplate.FIELD.LEASE_START_DAY.getField(),startDay);
						smsParm.put(OrdSmsTemplate.FIELD.LEASE_END_DAY.getField(),endDay);
						List<OrdOrderWifiPickingPoint>  pickingPointList = ordOrderWifiPickingPointDao.findOrdOrderWifiPickingPoint(params);
						if(pickingPointList!=null && pickingPointList.size()>0){
							OrdOrderWifiPickingPoint point = pickingPointList.get(0);
							List<Long> pointIdlist = new ArrayList<Long>();
							pointIdlist.add(point.getTakePickingPointId());
							ResultHandleT<List<WifiPickingPoint>> takePointResult = wifiClientService.findWifiPickingPoint(item.getSuppGoodsId(), null, pointIdlist,null);
							pointIdlist.clear();
							pointIdlist.add(point.getBackPickingPointId());
							ResultHandleT<List<WifiPickingPoint>> backPointResult = wifiClientService.findWifiPickingPoint(item.getSuppGoodsId(), null, pointIdlist,null);
							if(takePointResult!=null &&takePointResult.getReturnContent()!=null){
								WifiPickingPoint tackeWifiPickingPoint = takePointResult.getReturnContent().get(0);
								smsParm.put(OrdSmsTemplate.FIELD.TAKE_PICKING_POINT.getField(),tackeWifiPickingPoint.getPickingAddr());
								smsParm.put(OrdSmsTemplate.FIELD.TAKE_PICKING_POINT_CONTACT.getField(),tackeWifiPickingPoint.getSuppContactPerson());
								smsParm.put(OrdSmsTemplate.FIELD.TAKE_PICKING_POINT_PHONE.getField(),tackeWifiPickingPoint.getSuppContactPhone());
							}
							if(backPointResult!=null &&backPointResult.getReturnContent()!=null){
								WifiPickingPoint backWifiPickingPoint = backPointResult.getReturnContent().get(0);
								smsParm.put(OrdSmsTemplate.FIELD.BACK_PICKING_POINT.getField(),backWifiPickingPoint.getPickingAddr());
								smsParm.put(OrdSmsTemplate.FIELD.BACK_PICKING_POINT_CONTACT.getField(),backWifiPickingPoint.getSuppContactPerson());
								smsParm.put(OrdSmsTemplate.FIELD.BACK_PICKING_POINT_PHONE.getField(),backWifiPickingPoint.getSuppContactPhone());
							}
							
						}
					}else if(OrderUtil.isWifiCategory(item)&&ProdProduct.WIFIPRODUCTTYPE.PHONE.name().equals(OrderUtil.getProductType(item))){
						smsParm.put(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField(),item.getSuppGoodsName());
						String startDay = item.getContentStringByKey(OrderEnum.ORDER_WIFI_TYPE.lease_startDay.name());
						smsParm.put(OrdSmsTemplate.FIELD.LEASE_START_DAY.getField(),startDay);
						smsParm.put(OrdSmsTemplate.FIELD.WIFI_QUANTITY.getField(),item.getQuantity());
					}
					
				  }
				}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		
	}
	
	
	/**
	 * 获取订单入园方式Map
	 * @param order
	 * @return
	 */
	private Map<String,String> getEnterStyle(OrdOrder order){
		Map<String,String> enterStyleMap = new HashMap<String, String>();
		// 打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		fillEnterStyle(pack.getOrderItemList(), enterStyleMap);
	    	}
	    }
	    // 非打包
	    fillEnterStyle(order.getOrderItemList(), enterStyleMap);
		return enterStyleMap;
	}
	
	/**
	 * 填充门票入园方式Map
	 * @param ordOrderItemList
	 * @param addressMap
	 */
	private void fillEnterStyle(List<OrdOrderItem> ordOrderItemList, Map<String,String> enterStyleMap){
		if(ordOrderItemList != null && ordOrderItemList.size() > 0){
			for(OrdOrderItem item : ordOrderItemList){
				//存入园方式，key为子订单号
				enterStyleMap.put(item.getOrderItemId().toString(), getItemEnterStyle(item));
			}
		}
	}
	
	/**
	 * 获取子订单入园方式
	 * @param item
	 * @return
	 */
	private String getItemEnterStyle(OrdOrderItem item){
		//取到入园方式
		if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
			SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
			if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
				return  (sgtdv.getSuppGoodsDesc().getEnterStyle() != null) ? sgtdv.getSuppGoodsDesc().getEnterStyle() : "";
			}
		}
		return "";
	}
	
	/**
	 * 获取订单取票地址列表
	 * @param order
	 * @return
	 */
	public Map<String,String> getTicketAddress(OrdOrder order){
		Map<String,String> addressMap = new HashMap<String, String>();
		// 打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		fillTicketAddress(pack.getOrderItemList(), addressMap);
	    	}
	    }
	    // 非打包
	    fillTicketAddress(order.getOrderItemList(), addressMap);
		return addressMap;
	}
	
	/**
	 * 填充门票取票地址
	 * @param ordOrderItemList
	 * @param addressMap
	 */
	private void fillTicketAddress(List<OrdOrderItem> ordOrderItemList, Map<String,String> addressMap){
		if(ordOrderItemList != null && ordOrderItemList.size() > 0){
			for(OrdOrderItem item : ordOrderItemList){
				//取到入园地址
				if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
					SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
					if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
						addressMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
					}else{
						addressMap.put(item.getOrderItemId().toString(), "");
					}
				}
			}
		}
	}
	
	/**
	 * 获取订单子项产品列表  Map<String,ProdProduct>
	 * @param order
	 * @return
	 */
	public Map<String,ProdProduct> getItemProdProducts(OrdOrder order){
		Map<String,ProdProduct> prodProductMap = new HashMap<String, ProdProduct>();
		// 打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	
	    	LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
	    				+ order.getOrderId()
	    				+ "orderPackList-->size:" + order.getOrderPackList().size()
	    			);
	    	
	    	for(OrdOrderPack pack : order.getOrderPackList()){
		    	
	    		LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
	    				+ order.getOrderId()
	    				+ "pack info:" + pack.getOrderPackId() + "||" + pack.getProductId() + "||" + pack.getCategoryId()
	    			);
	    		
		    	List<OrdOrderItem>  orderItemList = pack.getOrderItemList();
		    	
		    	LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
	    				+ order.getOrderId()
	    				+ "pack orderItemList-->size:" + ((orderItemList == null) ? "null" : orderItemList.size())
	    			);		    	
		    	
	    		ProdProduct product = findProdProductById(pack.getProductId());
	    		BizCategory bizCategory = product.getBizCategory();
	    		if(bizCategory != null && bizCategory.getParentId() != null 
	    				&& Constant.VST_CATEGORY.CATEGORY_ROUTE.getCategoryId().equalsIgnoreCase(String.valueOf(bizCategory.getParentId()))){
			    	
	    			LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
		    				+ order.getOrderId()
		    				+ "line product packageType:" + product.getPackageType()
		    			);
	    			
	    			String packageType = product.getPackageType();
					if(ProdProduct.PACKAGETYPE.LVMAMA.name().equalsIgnoreCase(packageType)){
				    	
						LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
			    				+ order.getOrderId()
			    				+ "lvmamaPackage"
			    			);	
						
						Set<String> productIdSet = new HashSet<String>();
						
						if(orderItemList != null && orderItemList.size() > 0){
							for(OrdOrderItem item : orderItemList){
								ProdProduct pro = findProdProduct4FrontById(item.getProductId(), true, true);
								if(pro != null){
									
									LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
						    				+ order.getOrderId()
						    				+ "lvmama item info:" + item.getOrderItemId() + "||" + item.getProductId() 
						    				+ "||" + item.getCategoryId() + "||" + item.getBranchId() 
						    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
						    			);
									
									if(item.getCategoryId() == 1L){ 
										prodProductMap.put(item.getOrderItemId().toString(), pro);
									}else if(item.getCategoryId() == 15L //跟团游 
												|| item.getCategoryId() == 16L //当地游
												|| item.getCategoryId() == 17L //酒店套餐
												|| item.getCategoryId() == 18L//自由行
												|| item.getCategoryId() == 42L){//定制游
										if(!productIdSet.contains(item.getProductId().toString())){
											productIdSet.add(item.getProductId().toString());
											prodProductMap.put(item.getOrderItemId().toString(), pro);
										}
									}else{
										
									}
								}
							}
						}
					}else if(ProdProduct.PACKAGETYPE.SUPPLIER.name().equalsIgnoreCase(packageType)){
				    	
						LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
			    				+ order.getOrderId()
			    				+ "supplierPackage"
			    			);
						
						if(orderItemList != null && orderItemList.size() > 0){
							for(OrdOrderItem item : orderItemList){
								if(item.getCategoryId() == 15L //跟团游 
										|| item.getCategoryId() == 16L //当地游
										|| item.getCategoryId() == 17L //酒店套餐
										|| item.getCategoryId() == 18L//自由行
										|| item.getCategoryId() == 42L){//定制游
									ProdProduct pro1 = findProdProduct4FrontById(item.getProductId(), true, true);
									if(pro1 != null){
								    	
										LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
							    				+ order.getOrderId()
							    				+ "supplier item info:" + item.getOrderItemId() + "||" + item.getProductId() 
							    				+ "||" + item.getCategoryId() + "||" + item.getBranchId() 
							    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
							    			);	
										
										prodProductMap.put(item.getOrderItemId().toString(), pro1);
										break;
									}
								}
							}
						}						
					}
	    		}else{
	    			
			    	LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
		    				+ order.getOrderId()
		    				+ "non line product packageType:" + product.getPackageType()
		    			);		    			
	    			
			    	for(OrdOrderItem item : orderItemList){
			    		if(item.getOrderPackId() != null){
			    			ProdProduct prod = findProdProduct4FrontById(item.getProductId(), true, true);
			    			if(prod != null){
			    				if(item.getCategoryId() == 1L){ //酒店
			    					
			    					LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
						    				+ order.getOrderId()
						    				+ "non line productPackage item info:" + item.getOrderItemId() + "||" + item.getProductId() 
						    				+ "||" + item.getCategoryId() + "||" + item.getBranchId() 
						    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
						    			);	
			    					
			    					prodProductMap.put(item.getOrderItemId().toString(), prod);	
			    				}
			    		    }
			    		}
			    	}
	    		}
	    	}
	   }
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	
	    	LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
    				+ order.getOrderId()
    				+ "order orderItemList-->size:" + order.getOrderItemList().size()
    			);	
	    	
	    	Set<String> productIdSet1 = new HashSet<String>();
	    	for(OrdOrderItem item : order.getOrderItemList()){
	    		if(item.getOrderPackId() == null){//非打包子项item
	    			ProdProduct product1 = findProdProduct4FrontById(item.getProductId(), true, true);
	    			if(product1 != null){
	    				if(item.getCategoryId() == 1L){ //酒店
	    					prodProductMap.put(item.getOrderItemId().toString(), product1);
	    				}else if(item.getCategoryId() == 15L //跟团游 
								|| item.getCategoryId() == 16L //当地游
								|| item.getCategoryId() == 17L //酒店套餐
								|| item.getCategoryId() == 18L//自由行
								|| item.getCategoryId() == 42L //定制游
								|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId() //演出票
	    						){
							if(!productIdSet1.contains(item.getProductId().toString())){
		    					
								LOG.info("OrderSendSMSServiceImpl-->getItemProdProducts-->orderId:" 
					    				+ order.getOrderId()
					    				+ "non pack item info:" + item.getOrderItemId() + "||" + item.getProductId() 
					    				+ "||" + item.getCategoryId() + "||" + item.getBranchId() 
					    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
					    			);
		    					
								productIdSet1.add(item.getProductId().toString());
								prodProductMap.put(item.getOrderItemId().toString(), product1);
							}	    					
	    				}else{
	    					
	    				}
	    			}
	    		}
	    	}
	    }
		return prodProductMap;
	}
	
	/**
	 * 获取订单子项供应商列表  Map<String,SuppSupplier>
	 * @param order
	 * @return
	 */
	public Map<String, SuppSupplier> getItemSuppSupplier(OrdOrder order) {
		Map<String, SuppSupplier> suppSupplierMap = new HashMap<String, SuppSupplier>();

		if (order.getOrderItemList() != null && order.getOrderItemList().size() > 0) {

			LOG.info("OrderSendSMSServiceImpl-->getItemSuppSupplier-->orderId:"
					+ order.getOrderId() + "orderItemList-->size:"
					+ order.getOrderItemList().size());

			for (OrdOrderItem item : order.getOrderItemList()) {
				LOG.info("item.getSupplierId() ="+item.getSupplierId());
				LOG.info("tem.getOrderItemId() ="+item.getOrderItemId());
				SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(item.getSupplierId()).getReturnContent();
				if (suppSupplier != null) {
					suppSupplierMap.put(item.getOrderItemId().toString(),suppSupplier);
				}
			}
		}
		return suppSupplierMap;
	}
	
	/**
	 * 订单购买份数
	 * @param order
	 * @return
	 */
	public Integer buildBuyCount(OrdOrder order) {
		Integer buyCount = 0;
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
			buyCount = orderItem.getQuantity().intValue();
		}
		return buyCount;
	}
	
	/**
	 * 模板填充
	 * @param content
	 * @param ordOrder
	 * @param sendNode
	 * @return
	 * @throws Exception 
	 */
	private String composeMessage(String content,OrdOrder order,OrdSmsTemplate.SEND_NODE sendNode){
			LOG.info("OrderSendSmsServiceImpl.composeMessage:orderId=" + order.getOrderId() + ",sendNode:" + sendNode.name());
			OrdOrderItem mainOrderItem = order.getMainOrderItem();
			Long productId = null;
			if (order.getOrdOrderPack() != null) {
				productId = order.getOrdOrderPack().getProductId();
			} else {
				productId = mainOrderItem.getProductId();
			}
			ProdProduct product =findProdProduct4FrontById(productId, true, true);
			LOG.info("orderId=" + order.getOrderId()+"productType:"+(product!=null?product.getProductType():null));
			//预付支付前置
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID)
					|| sendNode.equals(SEND_NODE.VERIFIED_PAYED_PREPAID_HOTEL_FOREIGNLINE)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PREAUTH_WORKTIME_OR_UNWORKTIME_STOCK)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PREAUTH_UNWORKTIME)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_WORKTIME_OR_UNWORKTIME_STOCK)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME_UNSTOCK_NOTTODAY)){
				//客服工作时间段短信模版修改,超过当天18点的,要把短信模版内的客服工作时间9点-18点审核订单的引起用户疑惑的信息去掉
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME)||
			       sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PREAUTH_UNWORKTIME)){
					if(DateUtil.formatDate(new Date(),"yyyy-MM-dd").equals(DateUtil.formatDate(order.getVisitTime(),"yyyy-MM-dd"))
							&&24>DateUtil.getHour(new Date())&&DateUtil.getHour(new Date())>=18){
						if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME)){
							content = content.replace("客服将在工作时间段（9:00-18:00）尽快审核您的订单，", "");
						}else{
							content = content.replace("客服将在工作时间段（9:00-23:00）尽快审核您的订单，", "");
						}
					}
				}
				if(OrderUtils.isHotelComProduct(routerService.getProductCategoryId(productId))){
					OrderSmsPo orderSmsPo = composeOrderSmsPo(order);
					return SmsContentCreator.fillSmsForPayAhead(content, orderSmsPo);
				}else{
					return SmsUtil.fillSmsForPayAhead(content, order, sendNode, product, getGoodsCustomSms(order));
				}
			}
			//上海迪士尼改期 || 世园会改期
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.DISNEY_REVISE_DATE) || sendNode.equals(OrdSmsTemplate.SEND_NODE.SHIYUANHUI_REVISE_DATE)){
				HashMap<String, Object> map =new HashMap<String, Object>();	
				HashMap<String, String> addressMap =new HashMap<String, String>();	
				HashMap<String, String> enterStyleMap =new HashMap<String, String>();	
				HashMap<String, String> timeMap =new HashMap<String, String>();
				Map<String,String> goodsCustomSms = getGoodsCustomSms(order);
				fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					String specialTicketType = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name());
					if (SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_TICKET.name().equals(specialTicketType)
							||SuppGoods.SPECIAL_TICKET_TYPE.SHIYUANHUI_TICKET.name().equals(specialTicketType)) {
						String orderItemId = String.valueOf(orderItem.getOrderItemId());
						map.put("address", addressMap.get(orderItemId));
						map.put("enterStyle", enterStyleMap.get(orderItemId));
						if(sendNode.equals(OrdSmsTemplate.SEND_NODE.SHIYUANHUI_REVISE_DATE)){
							String orginContent = enterStyleMap.get(orderItemId);
							String customContent = goodsCustomSms.get(orderItemId);
							map.put("enterStyle", orginContent+ "，"+ customContent);
						}
						map.put("changeTime", timeMap.get(orderItemId));
						map.put("visitTime", DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd"));
					}
				}
				return SmsUtil.fillSms(content, order,map);
			}
			//万达改期
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.WANDA_REVISE_DATE)){
				HashMap<String, Object> map =new HashMap<String, Object>();
				HashMap<String, String> addressMap =new HashMap<String, String>();
				HashMap<String, String> enterStyleMap =new HashMap<String, String>();
				HashMap<String, String> timeMap =new HashMap<String, String>();
				fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					String orderItemId = String.valueOf(orderItem.getOrderItemId());
					map.put("address", addressMap.get(orderItemId));
					map.put("enterStyle", enterStyleMap.get(orderItemId));
					map.put("visitTime", DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd"));
				}
				return SmsUtil.fillSms(content, order,map);
			}
			if(sendNode.equals(SEND_NODE.FANGTE_REVISE_DATE)){
				HashMap<String, Object> map =new HashMap<String, Object>();
				HashMap<String, String> addressMap =new HashMap<String, String>();
				HashMap<String, String> enterStyleMap =new HashMap<String, String>();
				HashMap<String, String> timeMap =new HashMap<String, String>();
				fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				return SmsUtil.fillSmsForOrdReviseDate(content, order,addressMap,enterStyleMap,timeMap);
			}
			//国内机酒订单支付前置支付成功短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL)){
				return SmsUtil.orderAdvancePaySmsTemplate();
			}
			//国内机酒订单支付前置支付,资源审核通过短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE)){
				if(ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(product.getPackageType())){
					return SmsUtil.orderAdvancePaySmsResourceAmplTemplateForLVMAMA(order);
				}else if(ProdProduct.PACKAGETYPE.SUPPLIER.getCode().equals(product.getPackageType())){
					return SmsUtil.orderAdvancePaySmsResourceAmplTemplateForSUPPLIER(order,product);
				}
			}
			//国内机酒订单支付前置支付,未支付超时取消
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL_CANCEL_TIMEOUT)){
				return SmsUtil.orderAdvancePaySmsTemplateCancel(order);
			}
			//国内巴士+酒订单支付前置支付成功短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_HOTEL)){
				Map<Long, ProdTraffic> prodMap=new HashMap<>();
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
						ResultHandleT<Map<String, Object>> resultHandleT = prodProductClientService.findProdTrafficByProdId(orderItem.getProductId());
						ProdTraffic prodTraffic = (ProdTraffic) resultHandleT.getReturnContent().get("prodTraffic");
						prodMap.put(orderItem.getProductId(), prodTraffic);
					}
				}
				return SmsUtil.orderAdvancePaySmsTemplateForBusHotel(content,order,prodMap);
			}
			//国内巴士+酒订单支付前置审核成功短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_HOTEL_RESOURCE_AMPLE)){
				Map<Long, ProdTraffic> prodMap=new HashMap<>();
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
						ResultHandleT<Map<String, Object>> resultHandleT = prodProductClientService.findProdTrafficByProdId(orderItem.getProductId());
						ProdTraffic prodTraffic = (ProdTraffic) resultHandleT.getReturnContent().get("prodTraffic");
						prodMap.put(orderItem.getProductId(), prodTraffic);
					}
				}
				return SmsUtil.orderAdvancePaySmsTemplateForBusHotel(content,order,prodMap);
			}
			//国内巴士+酒（巴士部分）订单支付前置审核成功短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_RESOURCE_AMPLE)){
				Map<Long, ProdTraffic> prodTrafficMap=new HashMap<>();
				Map<Long, ProdProductBranch> prodProductBranchMap=new HashMap<>();
				Map<Long, SuppGoodsBus> suppGoodsBusMap=new HashMap<>();
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
						ResultHandleT<Map<String, Object>> resultHandleT = prodProductClientService.findProdTrafficByProdId(orderItem.getProductId());
						ProdTraffic prodTraffic = (ProdTraffic) resultHandleT.getReturnContent().get("prodTraffic");
						prodTrafficMap.put(orderItem.getProductId(), prodTraffic);
						Map<String, Object> parameprodProductBranch = new HashMap<String, Object>();
					    parameprodProductBranch.put("prodBranchId", orderItem.getBranchId());
						try {
							ResultHandleT<List<ProdProductBranch>> prodProductBranchList = prodProductBranchClientService.findProdProductBranchList(parameprodProductBranch);
							ProdProductBranch prodProductBranch=prodProductBranchList.getReturnContent().get(0);
							prodProductBranchMap.put(orderItem.getProductId(), prodProductBranch);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Map<String, Object> params = new HashMap<String, Object>();
				    	params.put("suppGoodsId", orderItem.getSuppGoodsId());
						ResultHandleT<List<SuppGoodsBus>> resultHandle = suppGoodsBusClientService.findSuppGoodsBusList(params);
						suppGoodsBusMap.put(orderItem.getProductId(), resultHandle.getReturnContent().get(0));
					}
				}
				return SmsUtil.orderAdvancePaySmsTemplateForBus(content,order,prodTrafficMap,prodProductBranchMap,suppGoodsBusMap,getItemProdProducts(order));
			}
			//游玩人后置订单未锁定模板
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM_NO) || sendNode.equals(SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM_NO_OUT_FREED)){
				if(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equals(product.getProductType())){
					Map<String, Object> param=new HashMap<String, Object>();
					if(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equals(product.getProductType())){
						param.put("PRODUCTTYPE", ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name());
						param.put("findFrontBusStop", findFrontBusStop(order.getOrderId()));
					}else{
						param.put("PRODUCTTYPE", product.getProductType());
					}
					return SmsUtil.fillSmsForPostposition(content,order,param);
				}
			}
			//游玩人后置订单未锁定并发合同
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM)){
				if(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERLINE.name().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equals(product.getProductType())){
					return SmsUtil.fillSmsForPostpositionLock(content,order);
				}
			}
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_UNPAY_UNPREAUTH) // 取消
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_UNPAY_PREAUTH_PREPAY)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_PAYED_PREPAY)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_TIMEOUT_PREPAID)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_TO_PAY)//到付-订单取消成功
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_ORDER_APPLY)//预付-订单取消申请
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND)// 退款
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND)// 催支付
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND_OUTBOUND_FREED) // 出境自由行催支付
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_WORKTIME_PREPAID)// 支付完成-预授权成功
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_UNWORKTIME_PREPAID)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_FAILED_WORKTIME_PREPAID)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_FAILED_UNWORKTIME_PREPAID)				
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_UNPREAUTH_WORKTIME)//订单提交
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_UNPREAUTH_UNWORKTIME)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_PREAUTH)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_FLIGHT_HOTEL_WORKTIME_PREPAID)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_FLIGHT_HOTEL_UNWORKTIME_PREPAID)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_ORDER_COLLECTION)
				|| sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND_ROUTE_FLIGHT_HOTEL)
				){
				
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND)||
				   sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_TIMEOUT_PREPAID)||
				   sendNode.equals(OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND)){
					String isToForeign = "N";
					for (OrdOrderItem orderItem : order.getOrderItemList()) {
						isToForeign = orderItem.getContentStringByKey(VstOrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name());
						if("Y".equals(isToForeign)){
							break;
						}
					}
					
					Map<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("isToForeign", isToForeign);
					
					if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND)){
						List<OrdRefundment> ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
						if(!CollectionUtils.isEmpty(ordRefundments)&&null!=ordRefundments.get(0)){
							paramMap.put("refundAmount", ordRefundments.get(0).getAmountYuan()==null?0.0f:ordRefundments.get(0).getAmountYuan());
						}
					}
					
					return SmsUtil.fillSms(content, order,paramMap);
				}
				//客服工作时间段短信模版修改,超过当天18点的,要把短信模版内的客服工作时间9点-18点审核订单的引起用户疑惑的信息去掉
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_UNWORKTIME_PREPAID)||
			       sendNode.equals(OrdSmsTemplate.SEND_NODE.PREAUTH_FAILED_UNWORKTIME_PREPAID)){
					if(DateUtil.formatDate(new Date(),"yyyy-MM-dd").equals(DateUtil.formatDate(order.getVisitTime(),"yyyy-MM-dd"))
							&&24>DateUtil.getHour(new Date())&&DateUtil.getHour(new Date())>=18){
						content = content.replace("客服将在工作时间段（9:00-18:00）尽快审核您的订单，", "");
					}
				}
				
				return SmsUtil.fillSms(content, order,new HashMap<String, Object>());
			}
			
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_ORDER_CLOSEHOUSE)){
				OrdOrderItem ordOrderItem= order.getMainOrderItem();
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("suppGoodsName", ordOrderItem.getSuppGoodsName());
				map.put("productName", ordOrderItem.getProductName());
			    String  branchName = ordOrderItem.getContentStringByKey("branchName");
				if(branchName!=null&&!branchName.equals(ordOrderItem.getSuppGoodsName())){
				 map.put("branchName", branchName);
				}
				return SmsUtil.fillSmsOfCloseHouser(content, order,map);
			}  	
			//预付-提交退款申请
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_REFUND_APPLY)){
				return SmsUtil.fillSmsForRefundBack(content,order,new HashMap<String, Object>());
			}
			
			//add by xiachengliang 迪斯尼短信模板
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.DISNEY_VERIFIED_PAYED_PREPAID)){
				LOG.info("OrderSendSMSServiceImpl disiney message start:"+order.getOrderId());
				Map<String,OrdPerson> opMap = getOrdPersons(order.getOrderId(), sendNode);
				String conTactName = "";
				/*if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
					conTactName = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getFullName();
				}*/
				
				OrdPerson firstCheckInPerson = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
	            if(firstCheckInPerson != null){
	                conTactName = firstCheckInPerson.getFullName();
	            }
				
				/*Map<String, Object> params =new HashMap<String, Object>();
				params.put("orderId", order.getOrderId());
				params.put("supplierId", 21435);//迪斯尼
				RequestSuppOrder supporder = null;
				List<RequestSuppOrder> suppOrders;
				String suppOrderId = null;
				try {
					suppOrders = suppOrderClientService.findSuppOrderList(params);
					LOG.info("OrderSendSMSServiceImpl disney suppOrders:"+suppOrders);
					if(suppOrders != null && suppOrders.size() > 0){
						supporder = suppOrders.get(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(supporder != null){
					String[] suppOrderIds = supporder.getSuppOrderId().split(",");
					suppOrderId = suppOrderIds[0];
					LOG.info("OrderSendSMSServiceImpl disiney suppOrderId:"+suppOrderId);
				}*/
				
	            content = content.replaceFirst("凭预订号(.+?)入住", "于前台处报预留预订人姓名\\${contactName}及预留联系方式\\${mobile}入住");
	            LOG.info("修改后的迪斯尼短信模板:" + content);
	            
	            String mobile = null;
	            if(firstCheckInPerson != null && firstCheckInPerson.getMobile() != null) {
	            	mobile = firstCheckInPerson.getMobile();
	            } else {           	
	            	mobile = order.getContactPerson().getMobile();
	            }
								
				return SmsUtil.fillDisineySmsForPayAhead(content, order, sendNode, product, getGoodsCustomSms(order),conTactName,mobile);
			}
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY) ||
					sendNode.equals(SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY_FOREIGNLINE)) {
				return SmsUtil.fillSmsForCancelStrategy(content, order, sendNode, product, getGoodsCustomSms(order));
			}
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY)) {
				//TODO 
				return SmsUtil.fillSmsForHotelCombCancelStrategy(content, order, sendNode, product, getGoodsCustomSms(order));
			}

			// 提交
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				//增加玩乐演出时间
				return SmsUtil.fillSmsForTicketPrepaidUnPayed(content, order, sendNode,addressMap, enterStyleMap, timeMap,getGoodsCustomSms(order), getItemProdProducts(order), findFrontBusStop(order.getOrderId()),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNGUARANTEE)){
				return SmsUtil.fillSmsForPayHotel(content, order, product,"");
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_GUARANTEE)){
				return SmsUtil.fillSmsForPayHotel(content, order, product,"");
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				return SmsUtil.fillSmsForPayTickets(content,order,addressMap,enterStyleMap,timeMap,getGoodsCustomSms(order),getItemProdProducts(order),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				//增加 演出时间 ，入场地点，入场方式 订单提交-审核成功+到付+非期票
				return SmsUtil.fillSmsForPayTickets(content,order,addressMap,enterStyleMap,timeMap,getGoodsCustomSms(order),getItemProdProducts(order),suppSpecialSmsFlag(order));
			}
			// 审核完成
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				return SmsUtil.fillSmsForTicketPrepaidUnPayed(content, order, sendNode,addressMap, enterStyleMap, timeMap,null, getItemProdProducts(order), findFrontBusStop(order.getOrderId()),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_GUARANTEE) || sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNGUARANTEE) ){
				Map<String,OrdPerson> opMap = getOrdPersons(order.getOrderId(), sendNode);
				String conTactName = "";
				if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
					conTactName = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getFullName();
				}
				return SmsUtil.fillSmsForPayHotel(content, order, product,conTactName);
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				return SmsUtil.fillSmsForPayTickets(content,order,addressMap,enterStyleMap,timeMap,getGoodsCustomSms(order),getItemProdProducts(order),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				//增加玩乐演出票，审核成功+到付+非期票 三个信息
				return SmsUtil.fillSmsForPayTickets(content,order,addressMap,enterStyleMap,timeMap,getGoodsCustomSms(order),getItemProdProducts(order),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)){
				return SmsUtil.fillSmsForPrepaidUnPayed(content, order, sendNode, getTicketAddress(order), getGoodsCustomSms(order), getItemProdProducts(order), findFrontBusStop(order.getOrderId()),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)){
				return SmsUtil.fillSmsForPrepaidUnPayed(content, order, sendNode, getTicketAddress(order), getGoodsCustomSms(order), getItemProdProducts(order), findFrontBusStop(order.getOrderId()),suppSpecialSmsFlag(order));
			}
			// 支付完成
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)||
					sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_PERFORM_PREVIOUS_DAY)
		          || sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)){
				
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				// 增加玩乐 的信息
				boolean ifYLShowTicket = supplierOrderHandleService.getProductIfYL(productId);
				LOG.info("OrderSendSMSServiceImpl ifYLShowTicket orderID:" + order.getOrderId() + " ifYLShowTicket:" + ifYLShowTicket);
				return SmsUtil.fillSmsForPrepaidPayed(content, order, sendNode, addressMap, enterStyleMap,timeMap, getGoodsCustomSms(order), getItemProdProducts(order), findFrontBusStop(order.getOrderId()),getItemSuppSupplier(order),buildBuyCount(order),suppSpecialSmsFlag(order),ifYLShowTicket);
					
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_CATEGORY)){
				//这种情况暂时去掉
			}
			
			//国内出游前一日关怀短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY)){
				return SmsUtil.fillSmsForPerformPreviousDay(order);
			}
			//出境,邮轮出游前一日关怀短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY_OUBBOUNDBU)||sendNode.equals(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY_SHIP)){
				return SmsUtil.fillSmsForPerformPreviousDay_outBu(content,order);
			}
			//交通+X的品类，使用交通+X的模板，需要特殊处理，仅对资源审核完成和支付完成的发送节点
			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_ROUTE_AERO_HOTEL)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_AERO_HOTEL)) {			
				List<OrdOrderItem> orderItems = order.getOrderItemList();
				if (CollectionUtils.isEmpty(orderItems)) {
					orderItems = ordOrderItemDao.selectByOrderId(order.getOrderId());
				}
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
				return SmsUtil.fillSmsForRouteAeroHotel(content, order, orderItems, sendNode,addressMap,enterStyleMap,getGoodsCustomSms(order),timeMap,getItemProdProducts(order));
			}
			
			//机+酒的品类，使用机+酒的模板，需要特殊处理，仅对资源审核完成和支付完成的发送节点
			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_ROUTE_FLIGHT_HOTEL)) {			
				List<OrdOrderItem> orderItems = order.getOrderItemList();
				if (CollectionUtils.isEmpty(orderItems)) {
					orderItems = ordOrderItemDao.selectByOrderId(order.getOrderId());
				}
				return SmsUtil.fillSmsForRouteAeroHotel(content, order, orderItems, sendNode,getGoodsCustomSms(order),getItemProdProducts(order));
			}
			
			// 电子合同更改
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ELECONTRACT_UPDATE)){
					String email =  getEmail(order);
				return SmsUtil.fillSmsForelEcontractUpdate(content, order, email);
			}
			// 出团通知
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_GROUP_NOTICE_SENT)){
				String email =  getEmail(order);
				return SmsUtil.fillSmsForGroupNotice(content, order, email);
			}
			// 出游短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND)){
			
				//Modified by yangzhenzhong start
				return SmsUtil.fillSmsForTravelHotel(content, order,getItemProdProducts(order));
				//end
				
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND_NEW)){
				return SmsUtil.fillSmsForTravelHotelNew(content, order,getItemProdProducts(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> auxiliaryCodeMap = new HashMap<String, String>(); //通关入园码
				Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
				//增加上海迪士尼的逻辑 游玩前一天
				Map<String,Object> map = new HashMap<String,Object>();// 演出时间，和座位号
				fillDisneyPassCode(map, order);
				LOG.info("orderid is"+order.getOrderId()+"map is"+map.toString());
				this.fillTicketAddressAndEnterStyleAndCodeAndTime(order, addressMap, enterStyleMap,timeMap, auxiliaryCodeMap);
				
				 String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");
                 if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
					for(OrdOrderPack pack : order.getOrderPackList()){
						if(pack == null) {
			    			continue;
			    		}

						if (CollectionUtils.isEmpty(pack.getOrderItemList())) {
							continue;
						}
						
						for (OrdOrderItem item : pack.getOrderItemList()) {
							boolean isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
							if(isLvji){
								OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
								map.put(String.valueOf(item.getOrderItemId()), ordPassCode);
							}
							
						}
					}
				}
				
				if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			    	for(OrdOrderItem item : order.getOrderItemList()){
			    		if(item.getOrderPackId() == null){
			    			boolean isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
			    			if(isLvji){
			    				OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
								map.put(String.valueOf(item.getOrderItemId()), ordPassCode);
			    			}
			    		}
			    	}
				}
				LOG.info("map============"+map);
				//增加玩乐演出票的信息
				return SmsUtil.fillSmsForTravelTicket(content, order, addressMap, enterStyleMap, timeMap, auxiliaryCodeMap, getGoodsCustomSms(order),map,getItemProdProducts(order),suppSpecialSmsFlag(order));
			}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_LINE_TICKET_DAY_BEFORE_REMIND)){
				Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
				Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
				Map<String,String> auxiliaryCodeMap = new HashMap<String, String>(); //通关入园码
				this.fillTicketAddressAndEnterStyleAndCode(order, addressMap, enterStyleMap, auxiliaryCodeMap);
				return SmsUtil.fillSmsForTravelLine(content, order, addressMap, enterStyleMap, auxiliaryCodeMap, getGoodsCustomSms(order));
			}


			//申码成功
			
			/**Wifi短信**/
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PAY)
					   ||sendNode.equals(OrdSmsTemplate.SEND_NODE.WIFI_PICKUP_PAY)
					   ||sendNode.equals(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PHONE_PAY)){
						Map <String,Object> param = new HashMap<String, Object>();
						this.fillWifiSmsInfo(order.getOrderItemList(), param);
						return SmsUtil.fillSms(content, order, param);
			}
			if (SEND_NODE.PAY_SUCCESS_WIFI.equals(sendNode)) {
                LOG.info("PAY_SUCCESS_WIFI smsutil");
				return SmsUtil.fillSmsForPaySuccessWifi(content,order);
			}

			//@todo 含机票订单短信三个模板,工作时间下单 资源紧张短信模板,预订成功后短信模板,支付成功后短信模板
			if(OrdSmsTemplate.SEND_NODE.FLIGHT_ORDER_CREATE_UNVERIFIED_WORKTIME.equals(sendNode)
					||OrdSmsTemplate.SEND_NODE.FLIGHT_VERIFIED_PREPAID.equals(sendNode)
					||OrdSmsTemplate.SEND_NODE.FLIGHT_PAY_PREPAID.equals(sendNode)){
				if(OrdSmsTemplate.SEND_NODE.FLIGHT_ORDER_CREATE_UNVERIFIED_WORKTIME.equals(sendNode)){//工作时间下单 资源紧张短信模板
					return SmsUtil.fillSmsForOrderBusyIncludeFlight(content, order,new HashMap<String, Object>());
				}else{
					Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
					Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
					Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
					this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
					if(OrdSmsTemplate.SEND_NODE.FLIGHT_VERIFIED_PREPAID.equals(sendNode)){//预订成功后短信模板
						return SmsUtil.fillSmsForOrderCreatedIncludeFlight(content, order, sendNode,addressMap, enterStyleMap, timeMap,null, getItemProdProducts(order), findFrontBusStop(order.getOrderId()),suppSpecialSmsFlag(order));
					}else{//支付成功后短信模板
						return SmsUtil.fillSmsForOrderPayedIncludeFlight(content, order, sendNode, addressMap, enterStyleMap,timeMap, getGoodsCustomSms(order), getItemProdProducts(order), findFrontBusStop(order.getOrderId()),getItemSuppSupplier(order),buildBuyCount(order),suppSpecialSmsFlag(order));
					}
				}
			}
			
			//在线退款驳回
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ONLINE_REFUNDED_REJECTED)) {
				Map<String,Object> param = new HashMap<String, Object>();
				param.put("orderId", order.getOrderId());
				LOG.info("stamp order orderid is"+order.getOrderId()+"map is"+JSONUtil.bean2Json(param));
				return StringUtil.composeMessage(content, param);
			}
			
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.STAMP_PAID) || sendNode.equals(OrdSmsTemplate.SEND_NODE.STAMP_DEPOSIT_PAID)) {
				
				Map<String, Object> param = Maps.newHashMap();
				this.fillStampSmsInfo(order, param);
				LOG.info("stamp order orderid is"+order.getOrderId()+"map is"+JSONUtil.bean2Json(param));
				return SmsUtil.fillSms(content, order, param);
			}
			//玩乐出游短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_PLAY_DAY_BEFORE_REMIND)){
				Map<String,String> customMap=getGoodsCustomSms(order);
				return SmsUtil.fillSmsForPlay(content, order, customMap);
			}
			//意外险后置，过等待补充游玩人时间一半，发送提醒游玩人短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ACCINS_DELAY_REMIND)){
			    OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
                Date travDelayWaitTime = ordAccInsDelayInfo.getTravDelayWaitTime();//目的地线路意外险订单游玩人后置补全剩余时间
                Date currentDate = new Date();//当前时间
                
                Map<String,Object> map = new HashMap<String, Object>();
                int toRemindTime = DateUtil.getHour(currentDate, travDelayWaitTime);
                
                if (toRemindTime == 0) {
                    toRemindTime = 1;
                }
                
                map.put("halfWaitSupplyTrav", toRemindTime);
                
                LOG.info("ACCINS_DELAY_REMIND orderId = " + order.getOrderId());
                LOG.info("travDelayWaitTime = " + travDelayWaitTime);
                LOG.info("currentDate = " + currentDate);
                LOG.info("waitTime = " + DateUtil.getHour(currentDate, travDelayWaitTime));
                LOG.info("toRemindTime = " + toRemindTime);
                
                return SmsUtil.fillSms(content, order, map);
            } 
			// 意外险后置, 超过等待补充游玩人时间取消意外险 或 主动取消意外险  发送意外险取消短信
			if(sendNode.equals(OrdSmsTemplate.SEND_NODE.CANCEL_ACCINS_DELAY)){
			    return SmsUtil.fillSms(content, order,new HashMap<String, Object>());
	            }
			
			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.KLK_PAID_REMIND)) {
                
			    String type="";
			    //根据商品判断是白金还是黄金还是钻石
			    Long suppGoodsId = order.getMainOrderItem().getSuppGoodsId();

			    String diamondStr = Constant.getInstance().getProperty("orderSms.diamond");
			    String platinumStr = Constant.getInstance().getProperty("orderSms.platinum");
			    String goldStr = Constant.getInstance().getProperty("orderSms.gold");
			    Long diamond = 0L;
			    Long platinum = 0L;
			    Long gold = 0L;
			    if (StringUtil.isNotEmptyString(diamondStr)) {
			        diamond = Long.valueOf(diamondStr);
			    }
			    if (StringUtil.isNotEmptyString(platinumStr)) {
			        platinum = Long.valueOf(platinumStr);
			    }
			    if (StringUtil.isNotEmptyString(goldStr)) {
			        gold = Long.valueOf(goldStr);
			    }

			    type = getTypeStr(suppGoodsId, diamond, platinum, gold);
			    
                Map<String,Object> map = new HashMap<String, Object>();
                
                map.put("orderId", order.getOrderId());
                map.put("klType", type);
                
                LOG.info("KLK_PAYED_REMIND orderId = " + order.getOrderId() + "---klType=" + type);
                
                return SmsUtil.fillSms(content, order, map);
            }
			
			return content;
	}

	/**
	 * 酒店套餐短信po组装
	 * @param order
	 * @return
	 */
	private OrderSmsPo composeOrderSmsPo(OrdOrder order) {
		OrderSmsPo orderSmsPo = new OrderSmsPo();
		orderSmsPo.setOrdOrder(order);
		
		ProductSmsPo productSmsPo = new ProductSmsPo();
		productSmsPo.setProductName(order.getProductName());
		for(OrdOrderItem orderItem : order.getOrderItemList()){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem.getCategoryId()){
				productSmsPo.setProdAddress(orderItem.getContentStringByKey("prodAddress"));
				break;
			}
		}
		orderSmsPo.setProductSmsPo(productSmsPo);
		return orderSmsPo;
	}

	private void fillStampSmsInfo(OrdOrder order, Map<String, Object> param) {
		try {
		    LOG.info("------------------6------------------");
			String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/{orderId}";
			StampOrderDetails stampOrder = RestClient.getClient().getForObject(url, StampOrderDetails.class, String.valueOf(order.getOrderId()));
			
			if(stampOrder == null)
				return;
			if(stampOrder.getStamp() == null)
				return;
			param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), stampOrder.getStamp().getName());
			StringBuffer codes = new StringBuffer();
			if(CollectionUtils.isEmpty(stampOrder.getStampCodes()))
				return;
			for(StampCode code : stampOrder.getStampCodes()) {
				codes.append(code.getSerialNumber()).append(",");
			}
			param.put(OrdSmsTemplate.FIELD.STAMP_CODES.getField(), codes.substring(0, codes.length()-1));
		} catch (Throwable e) {
			LOG.error("load stamp order info failed, orderId:" + order.getOrderId(), e);
		}
	}

	/**
	 * 上车点
	 * @param orderId
	 * @return
	 */
	private String findFrontBusStop(Long orderId) {
		Map<String, Object> parametersOrdFormInfo = new HashMap<String, Object>();
		parametersOrdFormInfo.put("orderId",orderId);
		parametersOrdFormInfo.put("contentType",BuyInfoAddition.frontBusStop.name());
		List<OrdFormInfo> ordFormInfoList=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
		String frontBusStop=null;
		if (CollectionUtils.isNotEmpty(ordFormInfoList)) {
			OrdFormInfo ordFormInfo=ordFormInfoList.get(0);
			frontBusStop=ordFormInfo.getContent();
			frontBusStop=frontBusStop.replace("备注：",""); 
		}
		return frontBusStop;
	}
	
	@Override
	public void sendSmsByCustom(Long orderId, String content, String operate,
			String mobile) throws BusinessException {
			LOG.info("OrderSendSmsServiceImpl.sendSmsByCustom:orderId=" + orderId + ",content:" + content + ",operate" + operate + ",mobile" + mobile);
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
			params.clear();
			params.put("object", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			params.put("objectId", orderId);
			params.put("personType", OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
			List<OrdPerson> ordPersons1= ordPersonDao.findOrdPersonList(params);
			OrdPerson ordPerson1 = null;
			if( ordPersons1.size()>0)
				ordPerson1 = ordPersons1.get(0);
			//没有联系人时直接返回
			if(ordPerson==null || ordPerson.getMobile()==null || "".equals(ordPerson.getMobile())||ordPerson1==null || ordPerson1.getMobile()==null || "".equals(ordPerson1.getMobile()))
				return ;
			if(ordPerson.getMobile()!=null){
				mobile = ordPerson.getMobile();
			}else{
				mobile = ordPerson1.getMobile();
			}
		}
		if(mobile==null)
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0003,String.valueOf(orderId)+",手机号为空不能发送");

		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate(operate);
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		
		ComLog comLog = new ComLog();
		comLog.setContent("");
		comLog.setContentType("");
		comLog.setLogName("");
		comLog.setLogType("");
		comLog.setMemo("");
		comLog.setObjectId(null);
		comLog.setObjectType("");
		comLog.setOperatorName("");
		comLog.setParentId(null);
		comLog.setParentType("");
		
		try {
			/* update by xiexun 此处重新传入订单ID 业务类型 用于标识调用方标志
			 * smsRemoteService.sendSms(orderId, content,mobile);*/
			smsRemoteService.sendSms(orderId,"CUSTOM",content,mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
			lvmmLogClientService.sendLog(comLog);
		} catch (Exception e) {
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0002,String.valueOf(orderId),content);
		}
	}

	@Override
	public void reSendSms(Long smsId, String operate) throws BusinessException {
		LOG.info("OrderSendSmsServiceImpl.reSendSms:smsId=" + smsId + ",operate" + operate);
		OrdSmsSend ordSmsSend =ordSmsSendDao.selectByPrimaryKey(smsId);
		if(ordSmsSend==null)
			return;
		ordSmsSend.setOperate(operate);
		try {
//			smsRemoteService.sendSms(ordSmsSend.getContent(),ordSmsSend.getMobile());
			/*
			 * update by xiexun 此处重新传入重发标识
			 * smsRemoteService.sendSms(ordSmsSend.getOrderId(), ordSmsSend.getContent(),ordSmsSend.getMobile());*/
			smsRemoteService.sendSms(ordSmsSend.getOrderId(), "RESEND", ordSmsSend.getContent(), ordSmsSend.getMobile());
			ordSmsSend.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.updateByPrimaryKey(ordSmsSend);
		} catch (Exception e) {
			ordSmsSend.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.updateByPrimaryKey(ordSmsSend);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0002,String.valueOf(ordSmsSend.getOrderId()),ordSmsSend.getContent());
		}
	}
	
	private static OrdOrder converOrderItem(OrdOrder order,List<Long> orderItemList){
		List<OrdOrderItem> list = order.getOrderItemList();
		Set<Long> set = new HashSet<Long>();
		for(Long id:orderItemList){
			set.add(id);
		}
		LOG.info("OrderSendSmsServiceImpl.converOrderItem:orderId=" + order.getOrderId() + ",size of call in orderItemList:" + set.size());
		for(Iterator<OrdOrderItem> it=list.iterator();it.hasNext();){
			OrdOrderItem orderItem = it.next();
			if(!set.contains(orderItem.getOrderItemId())){
				it.remove();
			}
		}
		LOG.info("OrderSendSmsServiceImpl.converOrderItem:orderId=" + order.getOrderId() + ",size of order self orderItemList:" + set.size());
		order.setOrderItemList(list);
		return order;
	}
	
	/**
	 * 商品自定义短信
	 * @param order
	 * @param orderItemList
	 * @return
	 */
	private Map<String,String> getGoodsCustomSms(OrdOrder order, List<Long> orderItemList){
		Map<String,String> goodsCustomSmsMap = new HashMap<String, String>();
		for(OrdOrderItem item : order.getOrderItemList()){
			for(int i=0;i<orderItemList.size();i++){
				if(item.getOrderItemId().equals(orderItemList.get(i))){
					SuppGoodsAddition suppGoodsAddition= null;
					SuppGoodsAdditionVo vo = suppGoodsAdditionApiServiceRemote.getSuppGoodsAddtionByGoodsId(item.getSuppGoodsId());
					if(null != vo){
						String jsonVo = com.alibaba.fastjson.JSON.toJSONString(vo, SerializerFeature.DisableCircularReferenceDetect);
						suppGoodsAddition = com.alibaba.fastjson.JSON.parseObject(jsonVo,new TypeReference<SuppGoodsAddition>(){});
					}
//					SuppGoodsAddition suppGoodsAddition= suppGoodsAdditionClientRemote.selectByPrimaryKey(item.getSuppGoodsId());
					if(suppGoodsAddition != null && suppGoodsAddition.getSmsContent() != null){
						goodsCustomSmsMap.put(item.getOrderItemId().toString(), suppGoodsAddition.getSmsContent());
					}else{
						goodsCustomSmsMap.put(item.getOrderItemId().toString(),"");
					}
				}
			}
		}
		return goodsCustomSmsMap;
	}
	

	@Override
	public String getTicketCertContent(Long orderId, List<Long> orderItemList,Map<String, Object> map) {
		//返回组装的短信内容
		StringBuffer returnMessage = new StringBuffer();
		
		//1.取到订单数据
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		converOrderItem(order, orderItemList);//找出符合条件的子项

		//2.找到对应模板(短信发送只会找出一个模板)
		OrderApplyCodeSuccessSms sms = new OrderApplyCodeSuccessSms();
		//获取是驴妈妈发码还是服务商发码
		String smsSender = (String) map.get("SMS_SENDER");
//		Boolean isChimeLong = (Boolean)map.get("isChimeLong");
		List<String> smsNodeList = new ArrayList<String>();
		if (map.containsKey(VirtualCodeUtil.PASSPORT_PROVIDER_IS_VIRTUAL_CODE)) {
			smsNodeList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE_VIRTUAL_CODE.name());  //虚拟凭证码库服务商申码成功短信模板
		} else {
			smsNodeList = sms.exeSmsRule(order, smsSender);
		}
//		if(isChimeLong!=null && isChimeLong==true){
//            smsNodeList.clear();
//			smsNodeList.add(SEND_NODE.CHANGLONG_SPECIAL_SMS.name());
//            LOG.info("OrderSendSmsServiceImpl.getTicketCertContent changlong:orderId=" + orderId);
//		}
		//List<String> smsNodeList = sms.exeSmsRule(order);
		LOG.info("OrderSendSmsServiceImpl.getTicketCertContent:orderId=" + orderId + ",size of smsNodeList:" + smsNodeList.size());
		Boolean applyFailedSms = (Boolean) map.get("applyFailedSms");
		LOG.info("OrderSendSmsServiceImpl.applyFailedSms:orderId=" + orderId + "boolean value is" + applyFailedSms);
		if(applyFailedSms!=null &&applyFailedSms){
			OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, OrdSmsTemplate.SEND_NODE.SPECIAL_PASSPORT_SMS_FAIL);
			returnMessage.append(SmsUtil.fillSpecialFailSms(ordSmsTemplate.getContent(), order, orderItemList));
			return returnMessage.toString();
		}
        //
        if(map.containsKey("RESCHEDULE_FLAG")){
            for(OrdOrderItem orderItem:order.getOrderItemList()){
                orderItem.setVisitTime((Date)map.get(String.valueOf(orderItem.getOrderItemId())));
            }
            OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, OrdSmsTemplate.SEND_NODE.ORDER_RESCHEDULE_TICKET_EBK);
            Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
            Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
            Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
            this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap,timeMap);
            returnMessage.append(SmsUtil.fillSmsForPrepaidNoAperiodicTicketReschedule(ordSmsTemplate.getContent(), order, orderItemList, map, addressMap, enterStyleMap, timeMap, getGoodsCustomSms(order, orderItemList), suppSpecialSmsFlag(order)));
            return returnMessage.toString();
        }
        //
		for(String a:smsNodeList){
			LOG.info("-->>>OrderSendSmsServiceImpl.getTicketCertContent:orderId=" + orderId + "node is -->" + a);
		}
		//3.填充短信内容
		if(smsNodeList != null && smsNodeList.size() > 0){
			OrdSmsTemplate.SEND_NODE sendNode = null;
			OrdSmsTemplate ordSmsTemplate = null;
			String smsContent = null;
			for(String smsNode : smsNodeList){
				sendNode = OrdSmsTemplate.SEND_NODE.valueOf(smsNode);
				LOG.info("-->>>OrderSendSmsServiceImpl.ordSmsTemplate.send_node.valueOf(smsNode)=" + orderId + "node is -->>>" + smsNode);

				ordSmsTemplate = getOrdSmsTemplate(orderId, sendNode);
				if(ordSmsTemplate == null){
					LOG.warn("方法:OrderSendSMSServiceImpl——getTicketCertContent" + "OrderId:" + orderId + "没有找到短信模板:" + sendNode.name());
					continue;
				}
				if(ordSmsTemplate != null && ordSmsTemplate.getContent() == null){
					LOG.warn("方法:OrderSendSMSServiceImpl——getTicketCertContent" + "OrderId:" + orderId + "没有找到短信模板：" + sendNode.name() + "的内容!!!");
					continue;
				}
				//4.取到短信内容
				smsContent = ordSmsTemplate.getContent();
				LOG.warn("-->>OrderSendSMSServiceImpl::smsContent---" + "OrderId:" + orderId + "smsContent" + smsContent );

				Object obj = map.get("IS_NEED_SEND_MMS");
				if(obj != null){
					Boolean isMMS= (Boolean)obj;
					LOG.info("OrderSendSmsServiceImpl.getTicketCertContent:orderId=" + orderId + "申码且普通短信还是申码发二维码彩信isMMS:" + isMMS.booleanValue());
					if(isMMS.booleanValue()){//彩信需要保留内容
						;
					}else{//普通申码短信不保留
						smsContent = smsContent.replace(Constant.SMS_KEEP_CONTENT1 + "，", "");
						smsContent = smsContent.replace(Constant.SMS_KEEP_CONTENT2 + "。", "");
					}
				}				
				//5.组装对应短信内容
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE)
						||sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE)){
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
						returnMessage.append(SmsUtil.fillSmsForPayAperiodicTicket(smsContent, order, orderItemList, map,addressMap, enterStyleMap,timeMap, getGoodsCustomSms(order, orderItemList),suppSpecialSmsFlag(order)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE)
							||sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE)){
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
						
						//增加玩乐演出票 的场次信息。入场方式，入场地点
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
						returnMessage.append(SmsUtil.fillSmsForPayNoAperiodicTicket(smsContent, order, orderItemList, map, addressMap, enterStyleMap,timeMap, getGoodsCustomSms(order, orderItemList),getItemProdProducts(order),suppSpecialSmsFlag(order)));
				} else if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV)
						|| sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO)) {
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
						returnMessage.append(SmsUtil.fillSmsForPayNoAperiodicTicket(smsContent, order, orderItemList, map, addressMap, enterStyleMap,timeMap, getGoodsCustomSms(order, orderItemList),getItemProdProducts(order),suppSpecialSmsFlag(order)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ)){
						returnMessage.append(SmsUtil.fillSmsForPrepaidTicketBJ(smsContent, order, orderItemList, map, getGoodsCustomSms(order, orderItemList)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ)){
						Map<String,String> enterStyleMap = getEnterStyle(order); // 入园方式
						returnMessage.append(SmsUtil.fillSmsForPrepaidTicketGZ(smsContent, order, orderItemList, map, enterStyleMap, getGoodsCustomSms(order, orderItemList)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE)){
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();//取票时间
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap, timeMap);
						returnMessage.append(SmsUtil.fillSmsForPrepaidAperiodicTicket(smsContent, order, orderItemList, map, addressMap, enterStyleMap, timeMap, getGoodsCustomSms(order, orderItemList),suppSpecialSmsFlag(order)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE_VIRTUAL_CODE)) {
					returnMessage.append(SmsUtil.fillSmsForPrepaidAperiodicTicketForVirtualCode(smsContent, order));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE)){
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址 
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap,timeMap);
						//新增加迪士尼的剧场票的判断
						fillDisneyPassCode(map, order);
						
						List<OrdOrderItem> ordItemList = order.getOrderItemList();
						if(ordItemList != null && ordItemList.size() > 0){
							for(OrdOrderItem item : ordItemList){
								String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");
								boolean isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
								if(isLvji){
									OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
									map.put(String.valueOf(item.getOrderItemId()), ordPassCode);
								}
							}
						}
						LOG.info("map==============="+map);
						Long productId = 0L;
						try {
							if (null != order && order.getOrdOrderPack() != null) {
								productId = order.getOrdOrderPack().getProductId();
							} else {
								productId = order.getMainOrderItem().getProductId();
							}
						} catch (Exception e) {
							LOG.error("->>>OrderSendSmsServiceImpl.getTicketCertContent>>>getProductId orderid:"+ orderId + e);
						}
						boolean ifYLShowTicket = supplierOrderHandleService.getProductIfYL(productId);
						LOG.info("-->>>OrderSendSmsServiceImpl.getTicketCertContent:orderId=" + orderId +" productId:"+productId +" ifYLShowTicket"+ifYLShowTicket);
						returnMessage.append(SmsUtil.fillSmsForPrepaidNoAperiodicTicket(smsContent, order, orderItemList, map, addressMap, enterStyleMap, timeMap, getGoodsCustomSms(order, orderItemList),getItemProdProducts(order),suppSpecialSmsFlag(order),ifYLShowTicket));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE_PROVIDER)){
						Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
						Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
						Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
						this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap,timeMap);
						returnMessage.append(SmsUtil.fillSmsForPrepaidNoAperiodicTicketProvider(smsContent, order, orderItemList, map, addressMap, enterStyleMap, timeMap, getGoodsCustomSms(order, orderItemList),suppSpecialSmsFlag(order)));
				}else if(sendNode.equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_CONNECTS)){
				    //交通接驳
				    returnMessage.append(SmsUtil.fillSmsForConnectsNotice(smsContent, order));
				    LOG.info("PAYMENT_PREPAY_CONNECTS:"+returnMessage.toString());
			    }else if(sendNode.equals(SEND_NODE.CHANGLONG_SPECIAL_SMS)){
			    	//长隆特殊短信
					Map<String,String> addressMap = new HashMap<String, String>(); // 取票地址
					Map<String,String> enterStyleMap = new HashMap<String, String>(); // 入园方式
					Map<String,String> timeMap = new HashMap<String,String>();// 取票时间
					this.fillTicketAddressAndEnterStyleAndTime(order, addressMap, enterStyleMap,timeMap);
					returnMessage.append(SmsUtil.fillSmsForChimeLongProvider(smsContent, order, orderItemList, map, addressMap, enterStyleMap, timeMap, getGoodsCustomSms(order, orderItemList),suppSpecialSmsFlag(order)));
			    }
				
				//发送微信
				String mobile = null;
				Map<String,OrdPerson> opMap = getOrdPersons(orderId, sendNode);
				if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
					mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getMobile();
				}
				if(mobile == null){
					if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()) != null){
						mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()).getMobile();
					}
				}
				LOG.info("orderId:"+orderId+"mobile========"+mobile);
				//电子票提醒
                if (sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE_PROVIDER.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name())||
                	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name())) {
					try {
						orderSendWechatService.sendSms(orderId, mobile, OrdWechatTemplate.SendNode.getSendNodeObj(sendNode.getCode()));
					} catch (Exception e) {
						LOG.error("WECHAT EXCEPTION: "+ExceptionUtil.getExceptionDetails(e));
					}
				}
			}
		}
		//发送短信
		return returnMessage.toString();
	}

	private void fillDisneyPassCode(Map<String, Object> map, OrdOrder order) {
		LOG.info("inif-disney,orderid:" + order.getOrderId());
		List<Long> disneyShowItems = isDisneyShowOrderItemId(order);
		LOG.info("inif-disney,orderid:" + order.getOrderId()
				+ "itemidisDisneyShow:" + disneyShowItems.toString());
		if (CollectionUtils.isEmpty(disneyShowItems)) {
			return;
		}

		Long orderItemId = disneyShowItems.get(0);
		LOG.info("itemidisDisneyShow orderid" + order.getOrderId());
		OrdPassCode ordPassCode = ordPassCodeDao
				.getOrdPassCodeByOrderItemId(orderItemId);
		if (ordPassCode != null) {

			String orderTickets = supplierOrderOtherService
					.getOrderTickets(ordPassCode.getPassSerialno());
			LOG.info("disney,orderid:" + order.getOrderId() + "json:-"
					+ orderTickets);
			if (StringUtils.isNotBlank(orderTickets)) {
				try {
					JSONObject orderTicketsJson = JSONObject
							.fromObject(orderTickets);
					String showtime = (String) orderTicketsJson.get("showtime");
					String sectionInf = (String) orderTicketsJson.get("sectionInf");
					map.put("SHOWTIME", showtime + ",");
					map.put("SECTIONINF", sectionInf);

				} catch (Exception e) {
					LOG.debug("Json showtiem exception orderid:"
							+ order.getOrderId() + "heihei i am sorry " + e);
				}
			}
		}

	}
	
	/**
	 * 取票地址
	 * @param order
	 * @param orderItemList
	 * @return
	 */
	@Deprecated
	private Map<String,String> getTicketAddress(OrdOrder order, List<Long> orderItemList){
		Map<String,String> addressMap = new HashMap<String, String>();
		for(OrdOrderItem item : order.getOrderItemList()){
			for(int i=0;i<orderItemList.size();i++){
				if(item.getOrderItemId().equals(orderItemList.get(i))){
					//取到入园地址
					SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
					if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
						addressMap.put(item.getOrderItemId().toString(), (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
					}else{
						addressMap.put(item.getOrderItemId().toString(), "");
					}
				}
			}
		}
		return addressMap;
	}

	@Override
	public int findOrdSmsSendCount(Map<String, Object> params)
			throws BusinessException {
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
	public void sendSMS(String content, String mobile, Long orderId) {
		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate("system");
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		LOG.info("OrderSendSmsServiceImpl.sendSMS(String content, String mobile, Long orderId):orderId=" + orderId + ",mobile=" + mobile + ",content=" + content);
		OrdOrder ordOrder = orderLocalService.queryOrdorderByOrderId(orderId);
		if(null != ordOrder){
			//分销渠道为"DISTRIBUTOR_DAOMA"的订单，不发任何短信
			if("DISTRIBUTOR_DAOMA".equalsIgnoreCase(ordOrder.getDistributorCode())){
				LOG.info("sendSMS#orderId:"+orderId+",distributorCode:"+ordOrder.getDistributorCode()+",分销渠道代code为DISTRIBUTOR_DAOMA不发送短信");
				return;
		    }
		}
		try {
			smsRemoteService.sendSms(orderId, content,mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
		} catch (Exception e) {
			LOG.error("SendSMS Error:", e);
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			ordSmsReSendService.insert(beanCopyForOrdSmsReSend(record));
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),"system");
		}
	}

	@Override
	public void sendSMS(SmsMMS sms, String mobile) {
		LOG.info("OrderSendSmsServiceImpl.sendSMS(SmsMMS sms, String mobile):mobile=" + mobile);
		if(sms != null) {
			LOG.info("SmsMMS:" + sms.toString());
		}
		try {
			smsRemoteService.sendMMSms(sms, mobile, "SUZHOULEYUAN");
		} catch (Exception e) {
			LOG.error("SendMMS Error:", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,null,"TICKET_CERT_APPLY_SUCCESS");
		}
	}
	
	/**
	 * 判断是否二维码
	 * @param order
	 * @return
	 */
	private boolean getTwoCodeFlag(OrdOrder order){
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				if(StringUtils.equals(SuppGoods.NOTICETYPE.QRCODE.name(), 
						item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()))){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 订单凭证短信接口
	 * @param orderId
	 */
	@Override
	public void sendSms(Long orderId){
		LOG.info("订单凭证短信接口sendSms(Long orderId)=" + orderId);
		AbstractSms sms = null;
		boolean twoCodeFlag = false;
		
		//1.取到订单数据
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		twoCodeFlag = getTwoCodeFlag(order);
		//2.二维码部分-调用接口
		if(twoCodeFlag){
			passportSendSmsServiceRemote.resendSms(order.getOrderId());
		}
		//3.非二维码部分
		else {
			if(order.hasNeedPay()){	//到付
				if(order.hasResourceAmple()){ //资源审核成功
					sms = new OrderResourceSms(false);
				}
			}else if(order.hasNeedPrepaid()){	//预付
				if(order.hasResourceAmple() && order.hasPayed()){	//资源审核成功  + 支付完成 
					sms = new OrderPaymentSms(false);
				}
			}
			if(sms == null){
				sms = new OrderBaseSms();
			}
			//取到短信发送规则
			List<String> smsNodeList = sms.exeSmsRule(order);
			LOG.info("OrderSendSMSServiceImpl smsNodeList size:"+smsNodeList.size()+" orderId: "+order.getOrderId());
			//有短信发送
			if(smsNodeList != null && smsNodeList.size() > 0){
				for(String smsNode : smsNodeList){
					this.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
				}
			}
		}
	}
	
	@Override
	public boolean isShouldSendCertOfTicket(Long orderId){
		boolean shouldSendCertFlag = false;
		boolean twoCodeFlag = false;
		
		//1.取到订单数据
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		if(isTicket(order)){
			Calendar c = Calendar.getInstance(); 
			c.setTime(order.getVisitTime());
			c.set(Calendar.DATE, c.get(Calendar.DATE) + 1); 
			Date today = new Date();
			boolean validFlag = false;// 订单是否超过有效期设置
			validFlag = today.before(c.getTime());
			
			if(validFlag && OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())){
				twoCodeFlag = getTwoCodeFlag(order);
				//2.二维码部分
				if(twoCodeFlag){
					if(passportSendSmsServiceRemote.canResendSms(orderId).size() > 0)
						shouldSendCertFlag = true;
				}
				//3.非二维码部分
				else {
					if(order.hasNeedPay()){	//到付
						if(order.hasResourceAmple()){ //资源审核成功
							shouldSendCertFlag = true;
						}
					}else if(order.hasNeedPrepaid()){	//预付
						if(order.hasResourceAmple() && order.hasPayed()){	//资源审核成功  + 支付完成 
							shouldSendCertFlag = true;
						}
					}
				}
			}
		}
		return shouldSendCertFlag;
	}
	
	/**
	 * 是否是门票订单
	 * @param order
	 * @return
	 */
	public boolean isTicket(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_show_ticket)){
				return true;
			}
		}
		return false;		
	}
	
	@Override
	public String getMessageContent(Long orderId) {
		//1.取到订单数据
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		if(order == null){
			LOG.info("OrderSendSmsServiceImpl.getMessageContent:orderId=" + orderId + "的订单，查找出来为null");
			return null;
		}
		//申码成功先告诉用户第二天会收到含有取票码的短信
		OrdSmsTemplate.SEND_NODE sendNode = OrdSmsTemplate.SEND_NODE.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME;
		OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, sendNode);
		if(ordSmsTemplate == null){
			LOG.info("OrderSendSmsServiceImpl.getMessageContent:orderId=" + orderId + "没有取到模板:" + sendNode.name());
			return null;
		}
		//取到短信内容
		if(ordSmsTemplate.getContent() == null){
			LOG.info("OrderSendSmsServiceImpl.getMessageContent:orderId=" + orderId + "取到模板:" + sendNode.name() + ",模板短信内容为null");
			return null;
		}
		Map<String,Object> param = new HashMap<String,Object>();
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				if(item.hasMainItem()){
					// 入园方式
					String enterStyle = getItemEnterStyle(item); 
					if(StringUtils.isNotEmpty(enterStyle)){
						enterStyle = "，入园方式：" + enterStyle;
						param.put(OrdSmsTemplate.FIELD.ENTER_STYLE.getField(), enterStyle);
					}
					break;
				}
			}
		}
		String smsContent = SmsUtil.fillSms(ordSmsTemplate.getContent(), order, param);
		LOG.info("OrderSendSmsServiceImpl.getMessageContent:orderId=" + orderId + "取到模板:" + sendNode.name() + ",模板短信内容:" + smsContent);
	
		return smsContent;
	}
	
	
	@Override
	public void sendSms(Long orderId,String mobile, String sendNode,Map<String, Object> params) {
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("sendNode", sendNode);
		LOG.info("sendSms(Long orderId,String mobile, String sendNode,Map<String, Object> params) orderId="+orderId+" sendNode="+sendNode);
		List<OrdSmsTemplate> templateList = orderSmsTemplateService.findOrdSmsTemplateList(param);
		OrdSmsTemplate ordSmsTemplate = null;
		String content = null;
		if(templateList!=null && templateList.size()>0){
			ordSmsTemplate = templateList.get(0);
		}
		if(ordSmsTemplate!=null){
			content = ordSmsTemplate.getContent();
			String smsContent = StringUtil.composeMessage(content, params);
			OrdSmsSend record = new OrdSmsSend();
			record.setContent(smsContent);
			record.setMobile(mobile);
			record.setOperate("system");
			record.setOrderId(orderId);
			record.setSendTime(new Date());
			LOG.info("OrderSendSmsServiceImpl.sendSMS(String orderId, String mobile, String sendNode,Map<String, Object> params):orderId=" + orderId + ",mobile=" + mobile +",sendNode=" +sendNode+ ",params=" + params);
			try {
				/* update by xiexun 传入节点用于标识调用方
				 * smsRemoteService.sendSms(orderId, smsContent,mobile);*/
				smsRemoteService.sendSms(orderId, sendNode, smsContent, mobile);
				record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
				ordSmsSendDao.insert(record);
			} catch (Exception e) {
				record.setStatus(OrdSmsSend.STATUS.FAIL.name());
				ordSmsSendDao.insert(record);
				throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),"system");
			}
		}
	
	}
	
	private List<Long> isDisneyShowOrderItemId(OrdOrder order) {
		List<Long> arrayList = new ArrayList<Long>();
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			String specialTicketType = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name());
			if (SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_SHOW.name()
					.equals(specialTicketType)) {
				arrayList.add(orderItem.getOrderItemId());
			}
		}
		return arrayList;
	}
	
	private Map<Long, Object> getShowTicketTime(OrdOrder order) {

		Map<Long, Object> itemIdAndPlace = new HashMap<Long, Object>();
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			String startTime = orderItem
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventStartTime
							.name());
			String endTime = orderItem
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventEndTime
							.name());
			String showTicketShowTime = "";
			if (StringUtils.isNotBlank(endTime)
					&& StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime + "至" + endTime;
			} else if (StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime;
			}

			itemIdAndPlace.put(orderItem.getOrderItemId(), showTicketShowTime);
		}
		LOG.info("SHOW TICKETTIME MAP ORDER IS " +order.getOrderId() +"TICKET MAP IS " + itemIdAndPlace);
		return itemIdAndPlace;
	}



	private Map<Long,String> suppSpecialSmsFlag(OrdOrder order){
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		List<Long> suppGoodsIds = new ArrayList<Long>();
		for (OrdOrderItem ordOrderItem : orderItemList) {
			suppGoodsIds.add(ordOrderItem.getSuppGoodsId());
		}
		List<SuppSpecialSmsPo> specialSmsFlagById = getSuppGoods(suppGoodsIds);
		Map<Long, String> suppSpecialSmsMap = new HashMap<Long, String>();
		if(specialSmsFlagById.size()>0){
			for (SuppSpecialSmsPo suppSpecialSmsPo : specialSmsFlagById) {
				suppSpecialSmsMap.put(suppSpecialSmsPo.getSuppGoodId(),suppSpecialSmsPo.getSpecialSmsFlag());
			}
		}
		return suppSpecialSmsMap;
	}
	private OrdSmsReSend beanCopyForOrdSmsReSend(OrdSmsSend record ){
		OrdSmsReSend reSend = new OrdSmsReSend();
		reSend.setMobile(record.getMobile());
		reSend.setStatus("FAIL");
		reSend.setOrderId(record.getOrderId());
		reSend.setContent(record.getContent());
		reSend.setSendTime(record.getSendTime());
		reSend.setOperate(record.getOperate());
		return reSend;
	}

	private String fillPassLimitTime(String input){
		if(!StringUtils.isNotBlank(input)) {
			return "";
		}
		try {
			String[] split = input.split(":");
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("下单后");
			List<String> strings = new ArrayList<String>();
			strings.add("小时");
			strings.add("分");
			strings.add("秒");
			for (int i =0;i<split.length;i++){
				if(!"00".equals(split[i])) {
					stringBuilder.append(Integer.parseInt(split[i]));
					stringBuilder.append(strings.get(i));
				}
			}
			stringBuilder.append("后方可入园");
			return stringBuilder.toString();
		}catch (Exception e){
			LOG.info(ExceptionUtil.getExceptionDetails(e));
		}
		return "";
	}
	
	private List<SuppSpecialSmsPo> getSuppGoods(List<Long> suppGoodsId){
		List<SuppSpecialSmsPo> goods = new ArrayList<SuppSpecialSmsPo>();
		if(suppGoodsId !=null && suppGoodsId.size() > 0 ){
			List<Long> hotelSuppGoodsId = new ArrayList<Long>();
			List<Long> otherSuppGoodsId = new ArrayList<Long>();
			for (int i = 0; i < suppGoodsId.size(); i++) {
				if(destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(suppGoodsId.get(i))){
					hotelSuppGoodsId.add(suppGoodsId.get(i));
				}else{
					otherSuppGoodsId.add(suppGoodsId.get(i));
				}
			}
			if(otherSuppGoodsId.size() > 0){
				goods = suppGoodsClientService.findSpecialSmsFlagById(suppGoodsId);
			}
			if(hotelSuppGoodsId.size() > 0){//如果当前酒店迁移系统已上线
				RequestBody<List<Long>> requestBody = new RequestBody<List<Long>>();
				requestBody.setT(suppGoodsId);
				requestBody.setToken(com.lvmama.vst.comm.vo.Constant.DEST_BU_HOTEL_TOKEN);
				com.lvmama.dest.api.common.ResponseBody<List<HotelGoodsVstVo>> responseBody= hotelGoodsQueryVstApiServiceRemote.findSuppGoodsByIdList(requestBody);
				List<HotelGoodsVstVo> hotelGoodsVstVos= responseBody.getT();
				if(hotelGoodsVstVos !=null){
					goods = new ArrayList<SuppSpecialSmsPo>();
					for (HotelGoodsVstVo hotelGoodsVstVo : hotelGoodsVstVos) {
						SuppSpecialSmsPo suppSpecialSmsPo = new SuppSpecialSmsPo();
						EnhanceBeanUtils.copyProperties(hotelGoodsVstVo,suppSpecialSmsPo);
						goods.add(suppSpecialSmsPo);
					}
				}
			}
		}
		return goods;
	}


	@Override
	public String getExpressContentAndSend(Long orderId,Map<String,Object> map){
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		OrdSmsTemplate ordSmsTemplate = getOrdSmsTemplate(orderId, SEND_NODE.ORDER_MAIL_SMS);
		String mobile = null;
		Map<String,OrdPerson> opMap = getOrdPersons(orderId, SEND_NODE.ORDER_MAIL_SMS);
		if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()) != null){
			mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name()).getMobile();
		}
		if(mobile == null){
			if(opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()) != null){
				mobile = opMap.get(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name()).getMobile();
			}
		}
		String content=null;
		if(ordSmsTemplate!=null){
			 content = SmsUtil.fillSms(ordSmsTemplate.getContent(), order, map);
		}

		try {
			if(StringUtils.isNotBlank(content) && StringUtils.isNotBlank(mobile)) {
				/*
				 * update by xiexun 此处传入业务类型用于标识调用方
				 * sendSMS(content, mobile, orderId);*/
				sendSMS(content, mobile, "EXPRESS", orderId);
			}
		} catch (Exception e) {
			LOG.error("send sms express exception=="+ ExceptionUtils.getFullStackTrace(e));
			return "FAIL";
		}
		return  "SUCCESS";
	}

	private ProdProduct findProdProductById(Long productId) {
		if(destHotelAdapterUtils.checkHotelRouteEnableByProductId(productId)){
			try {
				RequestBody<Long> requestBody = new RequestBody<Long>();
				requestBody.setT(productId);
				requestBody.setToken(com.lvmama.vst.comm.vo.Constant.DEST_BU_HOTEL_TOKEN);
				com.lvmama.dest.api.common.ResponseBody<HotelOrdOrderProductVO> responseBody = hotelOrderProductQueryRemote.findProdProductByIdFromCache(requestBody);
				if (responseBody==null || responseBody.isFailure()) {
					LOG.debug("use new service hotelProductQueryApiService#findProdProductListById fail! productId is "+productId);
					return null;
				}
				if (responseBody.isSuccess()) {
					ProdProduct prodProduct = new ProdProduct();
					HotelOrdOrderProductVO vo = responseBody.getT();
					EnhanceBeanUtils.copyProperties(vo, prodProduct);
					LOG.debug("use new service hotelProductQueryApiService#findProdProductListById success! productId is "+productId);
					return prodProduct;
				}else {
					return null;
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				LOG.error(e.getMessage());
				return null;
			}
		}else {
			return prodProductClientService.findProdProductByIdFromCache(productId).getReturnContent();
		}
	}
	
	private ProdProduct findProdProduct4FrontById(Long productId,boolean hasProp,boolean hasPropValue) {
		Long categoryId = routerService.getProductCategoryId(productId);
		LOG.info("OrderSendSMSServiceImpl.findProdProduct4FrontById productId:"+productId+",categoryId:"+categoryId);
		if(OrderUtils.isHotelComProduct(categoryId)){
			// 酒套餐的情况不反查产品
			return null;
		}else{
			return prodProductClientService.findProdProductById(productId, true, true);
		}
	}

	private String shieldMessages(Long orderId, OrdOrder ordOrder,String content) {
		if(null != ordOrder){
			List<String> codeList = Arrays.asList(SmsUtil.DISTRIBUTOR_CODE_ARRAY);
			if(ordOrder.getDistributorId() == 4L && codeList.contains(ordOrder.getDistributorCode())){
				if(ordOrder.getCategoryId() == 11L || ordOrder.getCategoryId()==12L || ordOrder.getCategoryId()==13L){
					content = content.replace(SmsUtil.REPLACEAPI,"");
					LOG.info("shieldMessages#orderId:"+orderId+",distributorCode:"+ordOrder.getDistributorCode()+",content:"+content);
				}
				
			}
			//分销渠道为"DISTRIBUTOR_DAOMA"的订单，不发任何短信
			if("DISTRIBUTOR_DAOMA".equalsIgnoreCase(ordOrder.getDistributorCode())){
				LOG.info("shieldMessages#orderId:"+orderId+",distributorCode:"+ordOrder.getDistributorCode()+",分销渠道代码为DISTRIBUTOR_DAOMA不发送短信");
				return null;
			}
		}
		return content;
	}

	private String getTypeStr(Long suppGoodsId, Long diamond, Long platinum, Long gold) {
	    String type = ""; 
	    if (suppGoodsId.equals(diamond)) {
	        //钻石
	        type="钻石";
	    }
	    else if (suppGoodsId.equals(platinum)) {
	        type="白金";
	    }
	    else if (suppGoodsId.equals(gold)) {
	        type="黄金";
	    }
	    return type;
	}

	@Override
	public void sendSMS(String content, String mobile, String bussType,
			Long orderId) {

		OrdSmsSend record = new OrdSmsSend();
		record.setContent(content);
		record.setMobile(mobile);
		record.setOperate("system");
		record.setOrderId(orderId);
		record.setSendTime(new Date());
		LOG.info("OrderSendSmsServiceImpl.sendSMS(String content, String mobile, Long orderId):orderId=" + orderId + ",mobile=" + mobile + ",content=" + content);
		OrdOrder ordOrder = orderLocalService.queryOrdorderByOrderId(orderId);
		if(null != ordOrder){
			//分销渠道为"DISTRIBUTOR_DAOMA"的订单，不发任何短信
			if("DISTRIBUTOR_DAOMA".equalsIgnoreCase(ordOrder.getDistributorCode())){
				LOG.info("sendSMS#orderId:"+orderId+",distributorCode:"+ordOrder.getDistributorCode()+",分销渠道代code为DISTRIBUTOR_DAOMA不发送短信");
				return;
		    }
		}
		try {
			smsRemoteService.sendSms(orderId, bussType, content, mobile);
			record.setStatus(OrdSmsSend.STATUS.SUCCESS.name());
			ordSmsSendDao.insert(record);
		} catch (Exception e) {
			LOG.error("SendSMS Error:", e);
			record.setStatus(OrdSmsSend.STATUS.FAIL.name());
			ordSmsSendDao.insert(record);
			ordSmsReSendService.insert(beanCopyForOrdSmsReSend(record));
			throw new BusinessException(ErrorCodeMsg.ERR_SMS_SEND_0001,String.valueOf(orderId),"system");
		}
		
	}
}
