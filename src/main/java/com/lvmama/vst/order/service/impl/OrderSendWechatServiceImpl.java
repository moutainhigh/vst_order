package com.lvmama.vst.order.service.impl;

import com.lvmama.channel.api.wechat.service.ChannelWechatService;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.comm.utils.HttpsUtil;
import com.lvmama.comm.utils.MemcachedUtil;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsTicketDetailVO;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrdWechatTemplate.SendNode;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdWechatTemplateService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSendWechatService;
import com.lvmama.vst.order.service.OrdWechatAppService;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import com.lvmama.vst.pet.adapter.WechatServiceAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.service.ISupplierOrderHandleService;
import com.lvmama.vst.order.utils.Base64;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.comm.vo.order.OrderWechatAppVo;

@Service
public class OrderSendWechatServiceImpl implements IOrderSendWechatService {

    private static final Log LOG = LogFactory.getLog(OrderSendWechatServiceImpl.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserUserProxyAdapter userUserProxyAdapter;
    @Autowired
    private IOrdWechatTemplateService ordWechatTemplateService;
    @Autowired
    private WechatServiceAdapter wechatServiceAdapter;
    @Autowired
    private IOrderSendSmsService iOrderSendSmsService;

    @Autowired
    SuppGoodsClientService suppGoodsClientService;
    
	@Autowired
	private ISupplierOrderHandleService supplierOrderHandleService;
	
	@Autowired
	private ChannelWechatService channelWechatService;
	
	@Autowired
	private OrdWechatAppService ordWechatAppService;

    @Autowired
    private OrderRefundmentServiceAdapter orderRefundmentService;
	
    /**
     * 发送微信前,处理取到微信节点的逻辑
     */
    public Long sendSms(Long orderId, String mobile, SendNode sendNode) throws Exception {
        LOG.info("start OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "wechat sending sendNode: " + (sendNode != null ? sendNode.getCode() : "is null") + "the phoneNo" + mobile);
        if (sendNode == null) {
            return 0L;
        }
        
        //1.查询订单
        OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
        
        //2.查询订单下单人
        if (order == null) {
            return 0L;
        }
        UserUser userUser = userUserProxyAdapter.getUserUserByUserNo(order.getUserId());
        if (userUser != null && mobile != null && sendNode.getCode() != null) {
            if (ifUserIsValid(userUser) && "1".equals(userUser.getSubScribe())) {
                LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "prepare wechat content");
                
                //3.根据发送节点取微信模板
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sendNode", sendNode.getCode());
                params.put("state", "Y");
                params.put("_orderby", "UPDATED_TIME");
                params.put("_order", "DESC");
                List<OrdWechatTemplate> ordWechatTemplates = ordWechatTemplateService.findOrdWechatTemplateList(params);
                if (ordWechatTemplates == null || (ordWechatTemplates != null && ordWechatTemplates.size() <= 0)) {
                    LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "wechat template " + sendNode.getCode() + " is null");
                    return 0L;
                }
                OrdWechatTemplate wechatTemplate = ordWechatTemplates.get(0);
                if (wechatTemplate == null) {
                    LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "wechat template " + sendNode.getCode() + " is null");
                    return 0L;
                }
                String templateCode = wechatTemplate.getMessageCode();
                String templateId = "";
                Map<String, String> paraMap = new HashMap<String, String>();
                
               
                //4.解析模板内容
                String content = wechatTemplate.getMessageContent();
                if (content == null) {
                    LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "wechat template " + sendNode.getCode() + " content is null");
                    return 0L;
                }
                Map<String, Object> dataMap = new HashMap<String, Object>();
                String regex = "\\$\\{(.+?)\\}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String keyField = matcher.group(1);
                    LOG.info("keyField:" + keyField);
                    if (OrdWechatTemplate.WechatInfo.InfoField.ORDER_ID.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, order.getOrderId());
                        paraMap.put(keyField, String.valueOf(order.getOrderId()));
                    }

                    if (OrdWechatTemplate.WechatInfo.InfoField.ORDER_PRICE.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, order.getOughtAmountYuan());
                        paraMap.put(keyField, order.getOughtAmountYuan());
                    }
                    if (OrdWechatTemplate.WechatInfo.InfoField.ORDER_STATUS.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
                        paraMap.put(keyField, OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
                    }
                    if (OrdWechatTemplate.WechatInfo.InfoField.PRODUCT_NAME.nameToLower().equals(keyField)) {
                        String productName = null;
                        if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
                            for (OrdOrderPack pack : order.getOrderPackList()) {
                                if (pack.getProductName() != null) {
                                    productName = pack.getProductName();
                                    break;
                                }
                            }
                        }
                        if (productName == null) {
                            productName = order.getMainOrderItem().getProductName();
                        }
                        dataMap.put(keyField, productName);
                        paraMap.put(keyField, productName);
                    }
                    if (OrdWechatTemplate.WechatInfo.InfoField.WECHAT_ID.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, userUser.getWechatId());
                        paraMap.put(keyField, userUser.getWechatId());
                    }
                    if (OrdWechatTemplate.WechatInfo.InfoField.CUSTOMER_SERVICE_PHONE.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE);
                        paraMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE);
                    }
                    /**
                     * 新加酒店出游前一天提醒和门票出游前一天提醒节点 for update Zhang.Wei
                     *
                     */

                    Map<String, ProdProduct> productMap = new HashMap<String, ProdProduct>();
                    if (OrdWechatTemplate.WechatInfo.HotelInfoField.KEYWORD1.nameToLower().equals(keyField)) {
                        dataMap.put(keyField, order.getOrderId());
                        paraMap.put(keyField, String.valueOf(order.getOrderId()));
                    }
                    if (sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name())) {
                        productMap = iOrderSendSmsService.getItemProdProducts(order);
                        if (OrdWechatTemplate.WechatInfo.HotelInfoField.KEYWORD4.nameToLower().equals(keyField)) {
                            for (OrdOrderItem item : order.getOrderItemList()) {
                                if (item.getCategoryId() == 1L) {//酒店
                                    ProdProduct product = productMap.get(item.getOrderItemId().toString());
                                    if (product != null) {
                                        dataMap.put(keyField, (product.getPropValue() == null ? null : product.getPropValue().get("address")));
                                        paraMap.put(keyField, (product.getPropValue() == null ? null : (String)product.getPropValue().get("address")));
                                    }
                                }
                            }
                        }
                        if (OrdWechatTemplate.WechatInfo.HotelInfoField.KEYWORD5.nameToLower().equals(keyField)) {
                            for (OrdOrderItem item : order.getOrderItemList()) {
                                if (item.getCategoryId() == 1L) {//酒店
                                    ProdProduct product = productMap.get(item.getOrderItemId().toString());
                                    if (product != null) {
                                        dataMap.put(keyField, (product.getPropValue() == null ? null : product.getPropValue().get("telephone")));
                                        paraMap.put(keyField, (product.getPropValue() == null ? null : (String)product.getPropValue().get("telephone")));
                                    }
                                }
                            }
                        }

                        if (OrdWechatTemplate.WechatInfo.HotelInfoField.KEYWORD3.nameToLower().equals(keyField)) {
                            for (OrdOrderItem item : order.getOrderItemList()) {
                                if (item.getCategoryId() == 1L) {//酒店
                                    dataMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));  
                                    paraMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
                                   
                                }
                            }
                        }
                        if (OrdWechatTemplate.WechatInfo.HotelInfoField.KEYWORD2.nameToLower().equals(keyField)) {
                            String productName = null;
                            if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
                                for (OrdOrderPack pack : order.getOrderPackList()) {
                                    if (pack.getProductName() != null) {
                                        productName = pack.getProductName();
                                        break;
                                    }
                                }
                            }
                            if (productName == null) {
                                productName = order.getMainOrderItem().getProductName();
                            }
                            dataMap.put(keyField, productName);
                            paraMap.put(keyField, productName);
                        }
                    }

                    if (sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name())) {
                        if (OrdWechatTemplate.WechatInfo.TicketsInfoField.KEYWORD2.nameToLower().equals(keyField)) {//出游门票产品名称
                            String productName = null;
                            if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
                                for (OrdOrderPack pack : order.getOrderPackList()) {
                                    if (pack.getProductName() != null) {
                                        productName = pack.getProductName();
                                        break;
                                    }
                                }
                            }
                            if (productName == null) {
                                productName = order.getMainOrderItem().getProductName();
                            }
                            dataMap.put(keyField, productName);
                            paraMap.put(keyField, productName);
                        }
                        if (OrdWechatTemplate.WechatInfo.TicketsInfoField.KEYWORD3.nameToLower().equals(keyField)) {//出游门票游玩时间
                            for (OrdOrderItem item : order.getOrderItemList()) {
                                if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
                                    dataMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
                                    paraMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
                                }
                            }
                        }
                        if (OrdWechatTemplate.WechatInfo.TicketsInfoField.KEYWORD4.nameToLower().equals(keyField)) {//取票地址
                            for (OrdOrderItem item : order.getOrderItemList()) {
                                if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
                                    SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
                                    if (sgtdv != null && sgtdv.getSuppGoodsDesc() != null) {
                                        dataMap.put(keyField, (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
                                        paraMap.put(keyField, (sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
                                    } else {
                                        dataMap.put(keyField, "");
                                        paraMap.put(keyField, "");
                                    }
                                }
                            }
                        }
                    }
                    
                    
                    LOG.info("orderId="+orderId + "-" + "sendNode.name=============" + sendNode.name());
                    
                    //电子票提醒
                    if (sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name())||
                    	sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name())) {
                    	templateCode = "ELECTRONIC_TICKETS_REMIND";
                    	templateId = "ZWn05nPbW3CakCfRCgCkG8RgIrTZ1mLnTmzlUBwlNds";
                    	if (OrdWechatTemplate.WechatInfo.ElectronicInfoField.ORDER_ID.nameToLower().equals(keyField)) {//订单号
                    		 dataMap.put(keyField, order.getOrderId());
                    		 paraMap.put(keyField, String.valueOf(order.getOrderId()));
                    		 LOG.info("order.getOrderId=============" + order.getOrderId());
                    	}
                    	  
                    	if (OrdWechatTemplate.WechatInfo.ElectronicInfoField.PRODUCT_NAME.nameToLower().equals(keyField)) {//产品名称
                             String productName = null;
                             if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
                                 for (OrdOrderPack pack : order.getOrderPackList()) {
                                     if (pack.getProductName() != null) {
                                         productName = pack.getProductName();
                                         break;
                                     }
                                 }
                             }
                             if (productName == null) {
                                 productName = order.getMainOrderItem().getProductName();
                             }
                             dataMap.put(keyField, productName);
                             paraMap.put(keyField, productName);
                             LOG.info("productName=============" + productName);
                         }
                    	 
                    	 if (OrdWechatTemplate.WechatInfo.ElectronicInfoField.FETCH_TIME.nameToLower().equals(keyField)) {//取票时间
                    		 dataMap.put(keyField, "");
                    		 paraMap.put(keyField, "");
                    		 if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	                    		 for (OrdOrderItem item : order.getOrderItemList()) {
	                                 if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
	                                     SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
	                                     if (sgtdv != null && sgtdv.getSuppGoodsDesc() != null) {
	                                         dataMap.put(keyField, (sgtdv.getSuppGoodsDesc().getChangeTime() != null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
	                                         paraMap.put(keyField, (sgtdv.getSuppGoodsDesc().getChangeTime() != null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
	                                         LOG.info("ChangeTime=============" + sgtdv.getSuppGoodsDesc().getChangeTime());
	                                     }
	                                 }
	                             }
                    		 }
                    	 }
                    	 
                         if (OrdWechatTemplate.WechatInfo.ElectronicInfoField.PASS_CODE.nameToLower().equals(keyField)) {//入园凭证
                        	 dataMap.put(keyField, "");
                        	 paraMap.put(keyField, "");
                        	 if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	                    		 for (OrdOrderItem item : order.getOrderItemList()) {
	                                 if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
	                                	 OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
	                                	 dataMap.put("orderItemId", item.getOrderItemId());
                                         dataMap.put("passCodeId", "");
	                                	 if(ordPassCode != null){
                                             dataMap.put("passCodeId", ordPassCode.getPassCodeId());
                                             if("BASE64".equalsIgnoreCase(ordPassCode.getCode())){
	                                			 dataMap.put(keyField, ordPassCode.getAddCode()); 
	                                			 paraMap.put(keyField, ordPassCode.getAddCode());
	                                		 }else{
	                                			 dataMap.put(keyField, ordPassCode.getCode()); 
	                                			 paraMap.put(keyField, ordPassCode.getCode()); 
	                                		 }
	                                		 LOG.info("ordPassCode.getCode=============" + ordPassCode.getCode());
	                                		 LOG.info("ordPassCode.getAddCodee=============" + ordPassCode.getAddCode());
	                                	 }
	                                	 LOG.info("weixin ordPassCode.orderItemId=============" + item.getOrderItemId());
	                                	 //LOG.info("weixin ordPassCode.passCodeId=============" + ordPassCode.getPassCodeId());
	                                 }
	                             }
                    		 }
                    	 }
                         
                         if (OrdWechatTemplate.WechatInfo.ElectronicInfoField.VISIT_TIME.nameToLower().equals(keyField)) {//游玩日期
                        	 dataMap.put(keyField, "");
                        	 paraMap.put(keyField, "");
                        	 if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	                        	 for (OrdOrderItem item : order.getOrderItemList()) {
	                        		 if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
	                                     dataMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
	                                     paraMap.put(keyField, DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
	                                     LOG.info("item.getVisitTime=============" + item.getVisitTime());
	                                 }
	                             }
                        	 }
                    	 }
                    }
                    
                    //退款提醒
                    if (sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND.name())||
                		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_REFUND_APPLY.name())||
                		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND.name())||
                		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.CANCEL_REFUND_FIRST_BACK.name())||
                		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.CANCEL_REFUND_UNFIRST_BACK.name())){
                    	    templateCode = "REFUND_REMIND";
                    	    templateId = "H9CKZkIG93tgftibG48054XeeTtHVC6GW1tligvMOyA";
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.REFUND_PRICE.nameToLower().equals(keyField)) {//退款金额


                                //退款(直接从支付接口取，订单表中数据不准)
                                Long refundedAmount = 0L;
                                List<OrdRefundment> ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
                                for (OrdRefundment ordRefundment : ordRefundments) {
                                    //退款总金额
                                    if(Constant.REFUND_TYPE.ORDER_REFUNDED.name().equals(ordRefundment.getRefundType())){
                                        refundedAmount += ordRefundment.getAmount();
                                    }
                                }
                                dataMap.put(keyField, refundedAmount);
                                paraMap.put(keyField, String.valueOf(new BigDecimal(refundedAmount).divide(new BigDecimal(100))));

                                StringBuilder sb = new StringBuilder("call orderRefundmentService.findOrderRefundmentByOrderIdStatus parmas=").append(com.alibaba.fastjson.JSONObject.toJSONString(order.getOrderId())).append(" ;result=").append(com.alibaba.fastjson.JSONObject.toJSONString(ordRefundments));
                                LOG.info(sb.toString());

                                LOG.info("wechat send data dataMap content is "+refundedAmount + " paraMap conent is "+String.valueOf(new BigDecimal(refundedAmount).divide(new BigDecimal(100))));

                                if( order.getRefundedAmount()==null){
                                    LOG.info("oldwechat send data dataMap content is 0");
                                }else{
                                    LOG.info("oldwechat send data dataMap content is "+order.getRefundedAmount() + " paraMap conent is "+String.valueOf(new BigDecimal(order.getRefundedAmount()).divide(new BigDecimal(100))));
                                }

//	                       	    dataMap.put(keyField, order.getRefundedAmount());
	                       	    
//	                       	    Long refundedAmount = 0L;
//	                       	    if( order.getRefundedAmount()==null){
//	                    			refundedAmount = 0L;
//	                    		}else{
//	                    			refundedAmount = order.getRefundedAmount();
//	                    		}
//	                       	    paraMap.put(keyField, String.valueOf(new BigDecimal(refundedAmount).divide(new BigDecimal(100))));
//	                       	    LOG.info("order.getRefundedAmount=============" + order.getRefundedAmount());
	                   	    }
	                    	
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.REFUND_TYPE.nameToLower().equals(keyField)) {//退款方式
	                       	    dataMap.put(keyField, "原路退回");
	                       	    paraMap.put(keyField, "原路退回");
	                   	    }
	                    	
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.PAYMENT_TIME.nameToLower().equals(keyField)) {//到账时间
	                       	    dataMap.put(keyField, "7-15个工作日");
	                       	    paraMap.put(keyField, "7-15个工作日");
	                   	    }
	                    	
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.GOOD_DESC.nameToLower().equals(keyField)) {//商品描述
	                       	   	 dataMap.put(keyField, "");
	                       	     paraMap.put(keyField, "");
	                        	 if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
		                        	 for (OrdOrderItem item : order.getOrderItemList()) {
		                        		 if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
		                                     dataMap.put(keyField, item.getSuppGoodsName());
		                                     paraMap.put(keyField, item.getSuppGoodsName());
		                                     LOG.info("item.getSuppGoodsName=============" + item.getSuppGoodsName());
		                                 }
		                             }
	                        	 }
	                   	    }
	                    	
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.TRANS_CODE.nameToLower().equals(keyField)) {//交易单号
	                       	    dataMap.put(keyField, order.getOrderId());
	                       	    paraMap.put(keyField, String.valueOf(order.getOrderId()));
	                       	    LOG.info("order.getOrderId()============" + order.getOrderId());
	                   	    }
	                    	
	                    	if (OrdWechatTemplate.WechatInfo.RefundInfoField.REFUND_REASON.nameToLower().equals(keyField)) {//退款原因
	                    		if("CANCEL".equals(order.getOrderStatus())){
	                    			dataMap.put(keyField, "订单取消");
	                    			paraMap.put(keyField, "订单取消");
	                    		}else{
	                    			dataMap.put(keyField, "退还差额");
	                    			paraMap.put(keyField, "退还差额");
	                    		}
	                       	    
	                   	    }
                    }
                }
                
                //客服电话
                if(getFormInfo(order, BuyInfoAddition.customerServiceTel) != null){
                	paraMap.put("customerServiceTel", getFormInfo(order, BuyInfoAddition.customerServiceTel));
				}else{
					paraMap.put("customerServiceTel", Constant.CUSTOMER_SERVICE_PHONE);
				}
                
                if("ORDER_STATUS_CHANGE".equals(templateCode)){
                	templateId = "GqYWCX7GYMs8RloeVesZJ3hH1d-vRk9QfLzSjxMfPfo";
                }
                if("HOTEL_TRAVEL_REMIND".equals(templateCode)){
                	templateId = "9l5V_O0TVgKfG-6f9obcDXKNEHytpvmFjuGHWR0oHd0";
                }
                if("TICKETS_TRAVEL_REMIND".equals(templateCode)){
                	templateId = "wUV50bTbp720XBfA9pSG1onaAWZkelXFKQ-ZQ5ZeWB8";
                }
                
                String wechatContent = StringUtil.composeMessage(content, dataMap);
                LOG.info("OrderSendWechatServiceImpl sendSms orderId=" + orderId + ",wechatContent#####===============" + wechatContent );
                LOG.info("OrderSendWechatServiceImpl sendSms orderId=" + orderId + ",templateCode=" + templateCode);
                //非单酒店订单，去掉参数  productType=hotel
                if("REFUND_REMIND".equals(templateCode)||"ELECTRONIC_TICKETS_REMIND".equals(templateCode)){
                    wechatContent = wechatContent.replace("hotelOrder", "ticketOrder");
					wechatContent = wechatContent.replace("productType=HOTEL", "productType=TICKET");
                }else{
	                if (order.getOrderPackList() == null) {
	                    OrdOrderItem ordOrderItem = order.getMainOrderItem();
	                    if (ordOrderItem != null && ordOrderItem.getCategoryId() != 1L) {
	                        wechatContent = wechatContent.replace("hotelOrder", "ticketOrder");
							wechatContent = wechatContent.replace("productType=HOTEL", "productType=TICKET");
	                    }
	                }
                }
                
                JSONObject josnObject = JSONObject.fromObject(wechatContent);
                String url = josnObject.getString("url");
                
                if("ELECTRONIC_TICKETS_REMIND".equals(templateCode)){
                	url = "http://m.lvmama.com/user/pages/elec_detail.html?orderItemId="+dataMap.get("orderItemId")+"&passCodeId="+dataMap.get("passCodeId");
                	LOG.info("OrderSendWechatServiceImpl sendSms orderId=" + orderId + "---elec_detail url=" + url);
                }
                LOG.info("OrderSendWechatServiceImpl sendSms orderId=" + orderId + ",url=" + url);
                String urlEnCode="";
        		try {
        			//urlEnCode = "http://weixin.lvmama.com/wechat/preVisitBase64?target="+Base64.encoder(url);
        			urlEnCode = "http://weixin.lvmama.com/wechat/preLogin?target="+Base64.encoder(url);
        		} catch (Exception e) {
        			LOG.info("Base64 error====================");
        			e.printStackTrace();
        		}
        		josnObject.put("url", urlEnCode);
        		wechatContent = josnObject.toString();
               

                paraMap.put("touser", userUser.getWechatId());
                paraMap.put("templateId", templateId);
                paraMap.put("url", urlEnCode);
                LOG.info("orderId=" + orderId + "paraMap#####===============" + paraMap );
                
                Map<String,String> wparamMap = new HashMap<>();
                List<OrderWechatAppVo> orderWechatAppVos = ordWechatAppService.search(orderId);
                LOG.info("orderId=" + orderId + "orderWechatAppVos#####1===============" + orderWechatAppVos.toString() );
                getWeixinContent(orderId,wparamMap,order,orderWechatAppVos,sendNode);
        		LOG.info("orderId=" + orderId + "wparamMap#####========1=======" + wparamMap );
        		if(null != orderWechatAppVos && orderWechatAppVos.size() > 0 && isQRCodeSuccess(sendNode)) {
        			try {
        				Object wxObj = MemcachedUtil.getInstance().get("WXSMS"+wparamMap.get("formId"));
        				LOG.info("MemcachedUtil formId:" + wparamMap.get("formId") +" obj : " + wxObj);
        				if(wxObj == null){
        					String value = HttpsUtil.requestPostForm("https://weixin.lvmama.com/api/applet/send", wparamMap);
        					LOG.info("OrderSendWechatServiceImpl.sendSms====1小程序:OrderId:" + orderId+ "end sendWeChatMsg" +"---result:" + value);
        					MemcachedUtil.getInstance().set("WXSMS"+wparamMap.get("formId"),2*60,"Y");
        				}else{
        					return 1L;
        				}
					} catch (Exception e) {
						LOG.error("###微信小程序远程调用异常: "+ExceptionUtil.getExceptionDetails(e));
					}
        		}else{
        			//LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "start sendWeChatMsg---wechatContent:" + wechatContent);
        			//wechatServiceAdapter.sendWeChatMsg(userUser, wechatContent);
        			Long result = channelWechatService.sendWechat(userUser.getWechatId(), templateCode, paraMap, "");
        			LOG.info("OrderSendWechatServiceImpl.sendSms:OrderId:" + orderId + "end sendWeChatMsg" +"---result:"+result);
        			
        		}
                return 1L;
            
            }else if(ifUserIsValid(userUser) && "0".equals(userUser.getSubScribe())) {
		            	Map<String,String> wparamMap = new HashMap<>();
		                List<OrderWechatAppVo> orderWechatAppVos = ordWechatAppService.search(orderId);
		                LOG.info("orderId=" + orderId + "orderWechatAppVos#####0===============" + orderWechatAppVos );
		            	getWeixinContent(orderId,wparamMap,order,orderWechatAppVos,sendNode);
		            	LOG.info("orderId=" + orderId + "wparamMap#####========0=======" + wparamMap );
		            	if(null != orderWechatAppVos && orderWechatAppVos.size() > 0 && isQRCodeSuccess(sendNode)) {
		            		try {
		            			Object wxObj = MemcachedUtil.getInstance().get("WXSMS"+wparamMap.get("formId"));
		            			LOG.info("MemcachedUtil formId:" + wparamMap.get("formId") +" obj : " + wxObj);
		        				if(wxObj == null){
		        					String value = HttpsUtil.requestPostForm("https://weixin.lvmama.com/api/applet/send", wparamMap);
		        					LOG.info("OrderSendWechatServiceImpl.sendSms====0小程序:OrderId:" + orderId+ "end sendWeChatMsg" +"---result:" + value);
		        					MemcachedUtil.getInstance().set("WXSMS"+wparamMap.get("formId"),2*60,"Y");
		        				}else{
		        					return 1L;
		        				}
							} catch (Exception e) {
								LOG.error("###微信小程序远程调用异常: "+ExceptionUtil.getExceptionDetails(e));
							}
		            	}
		            	return 1L;
             }
        }
        return 0L;
    }

    /**
     * 重新发送微信
     */
    public void reSendSms(Long id) throws BusinessException {

    }
    
	/**
	 * 订单客服电话
	 * @param order
	 * @param buyInfoAddition
	 * @return
	 */
	private static String getFormInfo(OrdOrder order, BuyInfoAddition buyInfoAddition){
		OrdFormInfo ordFormInfo = order.getFormInfo(buyInfoAddition.name());
		if(ordFormInfo != null && ordFormInfo.getContent() != null){
			return ordFormInfo.getContent();
		}
		return null;
	}
	
	//门票 是否由驴妈妈申码
	public boolean isAllTicketQrCode(OrdOrder order){
		boolean result = true;
		if(null == order){
			return false;
		}
		try {
			for(OrdOrderItem item : order.getOrderItemList()){
				if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)){
					OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
					
					if(null == ordPassCode){
						return false;
					}
					if(null != ordPassCode && "PARTNER".equals(ordPassCode.getSendSms()) ){
						LOG.info("isAllTicketQrCode orderID:"+order.getOrderId() +" smssend: "+ordPassCode.getSendSms());
						return false;
					}
					if(null != ordPassCode && StringUtil.isEmptyString(ordPassCode.getSendSms()) ){
						return false;
					}
				}else{
					return false;
				}
			}
		} catch (Exception e) {
			result =  false;
			LOG.error("isAllTicketQrCode error:" + e);
		}
		return result;
	}
	
	
	public boolean isQRCodeSuccess(SendNode sendNode){
		boolean result = false;
		if(sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE_PROVIDER.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name())||
        		sendNode.name().equals(OrdSmsTemplate.SEND_NODE.PAY.name())){
			result = true;
		}
		return result;
		
	}
	
	/**
	 * 判断用户是否有效
	 * @param userUser
	 * @return
	 */
	public boolean ifUserIsValid(UserUser userUser){
		if(StringUtil.isNotEmptyString(userUser.getSubScribe())
                && StringUtil.isNotEmptyString(userUser.getWechatId())
                && StringUtil.isNotEmptyString(userUser.getIsValid())
                && "Y".equals(userUser.getIsValid())){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 微信小程序 购买成功通知
	 */
	public void getWeixinContent(Long orderId,Map<String,String> wparamMap,OrdOrder order,List<OrderWechatAppVo> orderWechatAppVos,SendNode sendNode){
		try {
			if(null != orderWechatAppVos && orderWechatAppVos.size() > 0 && isQRCodeSuccess(sendNode)) {
				wparamMap.put("openid", orderWechatAppVos.get(0).getOpenId());
				wparamMap.put("formId", orderWechatAppVos.get(0).getFromId());
				wparamMap.put("page", "pages/orderDetail/orderDetail?orderId="+order.getOrderId());
				String productName = null;
                if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
                    for (OrdOrderPack pack : order.getOrderPackList()) {
                        if (pack.getProductName() != null) {
                            productName = pack.getProductName();
                            break;
                        }
                    }
                }
                if (productName == null) {
                    productName = order.getMainOrderItem().getProductName();
                }
                wparamMap.put("prodName",productName);
                LOG.info("orderId=" + orderId + "productName#####===============" + productName );
                boolean ifLvFlag = false;//是否驴妈妈申码
    			ifLvFlag = isAllTicketQrCode(order);
				if(!ifLvFlag){
					wparamMap.put("messageType", "1");
					wparamMap.put("orderId", order.getOrderId().toString());
					wparamMap.put("orderAmount", order.getOughtAmountYuan().toString());
					wparamMap.put("orderStatus",OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
					
				}else{
					wparamMap.put("messageType", "2");
					wparamMap.put("dealId", String.valueOf(order.getOrderId()));
					if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
                		 for (OrdOrderItem item : order.getOrderItemList()) {
                             if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
                            	//取票时间
                                 SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
                                 if (sgtdv != null && sgtdv.getSuppGoodsDesc() != null) {
                                     wparamMap.put("fetchTime",(sgtdv.getSuppGoodsDesc().getChangeTime() != null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
                                 }
                                 //入园凭证
                                 OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
                                 if(ordPassCode != null){
                                     if("BASE64".equalsIgnoreCase(ordPassCode.getCode())){
                            			 wparamMap.put("passCode", ordPassCode.getAddCode());
                            		 }else{
                            			 wparamMap.put("passCode", ordPassCode.getCode());
                            		 }
                            	 }
                                 
                             }
                             String value = "";
                    		 String invalidDate = "";
                    		 if (item.getCategoryId() == 5L || item.getCategoryId() == 11L || item.getCategoryId() == 12L || item.getCategoryId() == 13L) {//门票
                      			  value = DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");
                      			  if(item.hasTicketAperiodic()){
                      				  if(null != item.getInvalidDate() && !"".equals(item.getInvalidDate())){
                      					  invalidDate = ",不适用日期 :" + item.getInvalidDate();
                      				  } 
                      			  }
                      			LOG.info("orderId=" + orderId + "value + invalidDate#####===============" + value+invalidDate );
                                wparamMap.put("playTime", value+invalidDate);
                             }
                             
                         }
           		     }
				}
			}
		} catch (Exception e) {
			LOG.error("###微信小程序 购买成功通知数据组装异常"+ExceptionUtil.getExceptionDetails(e));
		}
	}
	
}
