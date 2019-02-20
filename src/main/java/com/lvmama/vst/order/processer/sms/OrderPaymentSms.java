package com.lvmama.vst.order.processer.sms;

import com.google.gson.JsonArray;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.order.utils.OrderUtils;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 订单支付完成(包含预授权成功)
 * @author zhaomingzhu
 *
 */
public class OrderPaymentSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderPaymentSms.class);
	private boolean preauthSuccessFlag;
	
	public OrderPaymentSms(boolean preauthSuccessFlag){
		this.preauthSuccessFlag = preauthSuccessFlag;
	}
	
	//品类(酒店套餐-自主打包)
	public boolean isHotelPackage(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(Long.valueOf(18).equals(item.getCategoryId())){
				return true;
			}
		}
		return false;
	}
	//品类(自由行-自主打包)
	public boolean isFreeTrip(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(Long.valueOf(17).equals(item.getCategoryId())){
				return true;
			}
		}		
		return false;
	}	
	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	//支付状态(已支付)
	public boolean isPayed(OrdOrder order){
		if(order.hasPayed()){
			return true;
		}else{
			return false;
		}
	}	
	//工作时间(是day而非night)
	public boolean isDay(){
		Calendar cal = Calendar.getInstance();
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		if(9 <= hours && hours < 18){
			return true;
		}else{
			return false;		
		}
	}
	//预授权支付成功 (预授权判断)
	public boolean isPreauthSuccess(OrdOrder order){
		if(preauthSuccessFlag){
			return true;
		}
		return false;
	}
	//门票 当订单有且仅有二维码
	public boolean isAllTicketQrCode(OrdOrder order){
		int num = 0;//门票item个数
		int count = 0;//二维码门票item个数
		for(OrdOrderItem item : order.getOrderItemList()){
			num++;
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_show_ticket)){
				if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
						SuppGoods.NOTICETYPE.QRCODE.name())){
					count++;
				}
			}
		}
		if(num>0 && count == num){
			return true;
		}
		return false;
	}
	//有且只有供应商 (北京春秋永乐文化传播有限公司)的二维码门票  or 有且只有供应商 (广州银旅通国际旅行社有限公司)的二维码门票
	public boolean hasOnlySupplier(OrdOrder order,String supplierId){
		int total = 0;//总的商品个数
		int num = 0;//门票item个数
		int count = 0;//二维码门票item个数
		if(supplierId == null){
			return false;
		}
		for(OrdOrderItem item : order.getOrderItemList()){
			total++;
			if(supplierId.equalsIgnoreCase(String.valueOf(item.getSupplierId())) 
					&&(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket))){
				num++;
				if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
						SuppGoods.NOTICETYPE.QRCODE.name())){
					count++;
				}
			}
		}
		if(total == num && total == count){
			return true;
		}
		return false;
	}
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderPaymentSms ===>>> isPrepaid(order)=" + isPrepaid(order)
				+ "isPreauthSuccess(order)=" + isPreauthSuccess(order)
				+ "order.isPayMentType()=" + order.isPayMentType()
				+ "isDay(order)=" + isDay()
				+ "isHotelPackage(order)=" + isHotelPackage(order)
				+ "isFreeTrip(order)=" + isFreeTrip(order)
				+ "isAllTicketQrCode(order)=" + isAllTicketQrCode(order)
				+ "isPayed(order)=" + isPayed(order)
				+ "hasOnlySupplier(order, gz.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId"))
				+ "hasOnlySupplier(order, bj.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId"))
				+"orderidexeSmsRule="+order.getOrderId()
				);	
		logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===order.getCategoryId():"+order.getCategoryId()+"===order.getMainOrderItemProductType():"+order.getMainOrderItemProductType()+"==");
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		if (isNeedSpecialSms(order)) {//康旅卡 支付 短信
		    sendList.add(OrdSmsTemplate.SEND_NODE.KLK_PAID_REMIND.name());
        }else {
            
    		//判断订单是否为国内机酒订单
    		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
    				&&((BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
    				&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
    				||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue())
    				&&OrdOrderUtils.isLocalBuFrontOrder(order)){
    			logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"=enter=国内支付前置,支付完成=");
    			//获取模板
    			sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL.name());
    			return sendList;
    		}
    		//判断订单是否为国内巴士+酒订单
    		if((BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode()))
    				&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
    				&&BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()==order.getSubCategoryId()){
    			logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"=enter=国内巴士+酒支付前置,支付完成=");
    			//获取模板
    			sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_HOTEL.name());
    			return sendList;
    		}
    		//1.[主订单]预付  alter by xiaoyulin
    		if(isPrepaid(order) && isPayed(order)){
    			//1.1 目的地BU前台订单-(预付支付前置)支付完成
    			if(OrdOrderUtils.isDestBuFrontOrder(order)){
    				logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"=enter=1.1 目的地BU前台订单-(预付支付前置)支付完成=");
    				setPayAheadOrderSendList(sendList, order);
    			}
    			else{
    				logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"=enter=支付完成=order.isContainApiFlightTicket():"+order.isContainApiFlightTicket()+",order.item:"+order.getOrderItemList().size());
    				if(order.getTravellerDelayFlag()!=null&&
    						"Y".equals(order.getTravellerDelayFlag().toUpperCase())&&
    						order.getTravellerLockFlag()!=null&&
    						"N".equals(order.getTravellerLockFlag().toUpperCase())){
    					//先判断订单是不是后置，判断品类，然后查询产品信息判断是否是短线
    					if(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==order.getCategoryId().longValue()
    							||BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().longValue()==order.getCategoryId().longValue()
    							||BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().longValue()==order.getCategoryId().longValue()
    							||(BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId().longValue()&&!order.isContainApiFlightTicket())
    							||BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().longValue()==order.getSubCategoryId().longValue()){
    							//确定游玩人后置订单未锁定模板
    						if (OrderUtils.hasOutAndFreed(order)) {
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM_NO_OUT_FREED.name());
    						} else {
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM_NO.name());
    						}
    							logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===>>PAY_PAYED_DELAY_TRAVELLER==游玩人后置订单未锁定模板===");
    					}
    				}
    				//如果是交通＋X品类，则发送特殊的交通+X的短信模板
    				else if (BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId() == order.getCategoryId()) {
    					sendList.add(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_AERO_HOTEL.name());
    					logger.info("OrderPaymentSms===>>PAY_ROUTE_AERO_HOTEL==交通＋X模板===");
    				}
    				//1.2[主订单]预授权支付成功
    				else if(isPreauthSuccess(order)){
    					if(needResourceConfirm(order)){
    						//1.2.1[主订单]预授权支付成功+工作时间 +预付
    						if(isDay()){
    							sendList.add(OrdSmsTemplate.SEND_NODE.PREAUTH_WORKTIME_PREPAID.name());		
    						}
    						//1.2.2[主订单]预授权支付成功+非工作时间+预付
    						else if(!isDay()){
    							sendList.add(OrdSmsTemplate.SEND_NODE.PREAUTH_UNWORKTIME_PREPAID.name());
    						}
    					}
    					//1.2.3审核成功+预付+预授权完成
    					else{
    						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY.name());
    						if (isCancelStrategy(order)) {
    							sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
    						}
    						if (isHotelCancelStrategy(order)) {
    							sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
    						}
    					}
    				}
    				//1.3[主订单]支付成功+非预授权
    				else if(!isPreauthSuccess(order)){
    					//如果是自由行机+酒品类，则发送特殊的自由行机+酒的短信模板
    					if (BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
    							&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()) 
    							&& !order.hasInfoAndResourcePass()){
    						//[主订单]支付-支付完成+自由行机酒+工作时间+预付
    						if(isDay()){
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_FLIGHT_HOTEL_WORKTIME_PREPAID.name());		
    						}
    						//[主订单]支付-支付完成+自由行机酒+非工作时间+预付
    						else{
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_FLIGHT_HOTEL_UNWORKTIME_PREPAID.name());		
    						}
    					}
    					else if(order.isPayMentType() && needResourceConfirm(order)){
    						//1.3.1[主订单]支付-支付完成-预授权不成功+工作时间+预付
    						if(isDay()){
    							sendList.add(OrdSmsTemplate.SEND_NODE.PREAUTH_FAILED_WORKTIME_PREPAID.name());		
    						}
    						//1.3.2[主订单]支付-支付完成-预授权不成功+非工作时间+预付
    						else{
    							sendList.add(OrdSmsTemplate.SEND_NODE.PREAUTH_FAILED_UNWORKTIME_PREPAID.name());		
    						}
    					}
    					//1.3.2[主订单]支付-支付完成-预付
    					else{
    						//如果是交通＋X品类，则发送特殊的交通+X的短信模板
    						if (BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId() == order.getCategoryId()) {
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_AERO_HOTEL.name());
    						} else if(OrderUtils.isIncludeFlightExcludeXOrderItem(order)){
    							//@todo 加含机票的订单短信模板 对应于需求中的第二点 "支付成功后短信模板"
    							sendList.add(OrdSmsTemplate.SEND_NODE.FLIGHT_PAY_PREPAID.name());
    						} else if (OrderEnum.ORDER_STAMP.STAMP.name().equals(order.getOrderSubType())) { // 预售券订单
    							sendList.add(OrdSmsTemplate.SEND_NODE.STAMP_PAID.name());
							} else if (BU_NAME.OONEWSALE_BU.name().equalsIgnoreCase(order.getBuCode()) && BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(order.getCategoryId())) {
								logger.info("PAY_SUCCESS_WIFI category_wifi:"+order.getOrderId());
    							sendList.add(OrdSmsTemplate.SEND_NODE.PAY_SUCCESS_WIFI.name());
							} else {
								if (OrderUtils.hasOutAndFreed(order)) {
									sendList.add(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED.name());
								} else {
								    logger.info("OrderPaymentSms add pay orderId:"+order.getOrderId()+"---CategoryId="+order.getCategoryId());
									sendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
								}
								if (isCancelStrategy(order)) {
									sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
								}
								if (isHotelCancelStrategy(order)) {
									sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
								}
							}
						}
    				}
    			}
    		}
    		
    		//2.友情提示------[主订单]支付完成	(友情提示------[主订单]支付完成+预付+(自由行-自主打包、酒店套餐-自主打包) )
    		if((isHotelPackage(order) || isFreeTrip(order)) && isPrepaid(order)){
    			sendList.add(OrdSmsTemplate.SEND_NODE.PAY_CATEGORY.name());			
    		}
    		//3.当订单有且仅有二维码，则不发送，[主订单]支付完成  + 预付
    		if(isAllTicketQrCode(order)&& isPrepaid(order)){
    			if (OrderUtils.hasOutAndFreed(order)) {
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED.name());
    			} else {
    			    logger.info("isAllTicketQrCode remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}
    		//4.当订单有且仅有供应商（北京春秋永乐文化传播有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
    		if(hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId")) && isPrepaid(order)){
    			if (OrderUtils.hasOutAndFreed(order)) {
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED.name());
    			} else {
    			    logger.info("bj.supplierId remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}
    		//5.当订单有且仅有供应商（广州银旅通国际旅行社有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
    		if(hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId")) && isPrepaid(order)){
    			if (OrderUtils.hasOutAndFreed(order)) {
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED.name());
    			} else {
    			    logger.info("gz.supplierId remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}
    		
    		//WIFI支付-支付完成-预付
            if(BU_NAME.OONEWSALE_BU.name().equalsIgnoreCase(order.getBuCode())){
    		    //如果是新零售的去掉(支付-支付完成-预付-WIFI自取)这个节点
                noneSendList.add(OrdSmsTemplate.SEND_NODE.WIFI_PICKUP_PAY.name());
                //如果是新零售的去掉(支付-支付完成-预付-电话卡邮寄)这个节点
				noneSendList.add(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PHONE_PAY.name());
				//如果是新零售的去掉(支付-支付完成-预付-WIFI邮寄)这个节点
				noneSendList.add(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PAY.name());
            }
    		if((OrderUtil.isExpressWifi(order)&&isPrepaid(order))){
    			sendList.add(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PAY.name());
    			if(OrderUtil.isMainItemWifi(order.getMainOrderItem())){
    			    logger.info("isExpressWifi remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}else if(OrderUtil.isPickUpWifi(order)&&isPrepaid(order)){
    			sendList.add(OrdSmsTemplate.SEND_NODE.WIFI_PICKUP_PAY.name());
    			if(OrderUtil.isMainItemWifi(order.getMainOrderItem())){
    			    logger.info("isPickUpWifi remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}else if(OrderUtil.isExpressWifiPhone(order)&&isPrepaid(order)){
    			sendList.add(OrdSmsTemplate.SEND_NODE.WIFI_EXPRESS_PHONE_PAY.name());
    			if(OrderUtil.isMainItemWifiPhone(order.getMainOrderItem())){
    			    logger.info("isExpressWifiPhone remove PAY orderId=" + order.getOrderId());
    				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAY.name());
    			}
    		}
        }
		
		logger.info("OrderPaymentSms orderId=" + order.getOrderId() + "---sendList=" + JSONArray.fromObject(sendList).toString());
		logger.info("OrderPaymentSms orderId=" + order.getOrderId() + "---noneSendList=" + JSONArray.fromObject(noneSendList).toString());
		
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		
		logger.info("OrderPaymentSms result orderId=" + order.getOrderId() + "---sendList=" + JSONArray.fromObject(sendList).toString());
		return sendList;
	}
	
	/**
	 * 设置支付前置订单支付成功短信发送节点
	 * @param sendList
	 * @param order
	 */
	private void setPayAheadOrderSendList(List<String> sendList,
			OrdOrder order) {
		// 保留房不发支付短信，发的是生效短信
		if(OrdOrderUtils.hasStockFlag(order)){
			logger.info("setPayAheadOrderSendList has stock flag,orderId:"+order.getOrderId());
			//todo 处理出境港澳台酒店 add by lijuntao
			if(isForeignLineHotelOrder(order)){//判断是否是出境港澳台酒店
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTEL_FOREIGNLINE.name());
			}else {
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID.name());
			}
			if (isCancelStrategy(order)) {	//当订单为可退改时，发送退改短信
				if(isForeignLineHotelOrder(order)){//判断是否是出境港澳台酒店
					sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY_FOREIGNLINE.name());
				}else {
					sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
				}
			}
			if (isHotelCancelStrategy(order)) {	//当订单为可退改时，发送退改短信 
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
			}
			return;
		}
		
		// 1.[主订单]预授权支付成功
		if(isPreauthSuccess(order)){
			// 1.1.[支付前置]预授权支付成功+工作时间/(非工作时间+保留房)
			if(isDay() || OrdOrderUtils.hasStockFlag(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PREAUTH_WORKTIME_OR_UNWORKTIME_STOCK.name());	
			}
			// 1.2.[支付前置]预授权支付授权成功+非工作时间 
			else{
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PREAUTH_UNWORKTIME.name());
			}
		}
		// 2.普通支付成功
		else{
			// 2.1.[支付前置]普通支付成功+工作时间/(非工作时间+保留房）  
			if(isDay() || OrdOrderUtils.hasStockFlag(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_WORKTIME_OR_UNWORKTIME_STOCK.name());
			}else if (OrdOrderUtils.hasNotStockFlag(order) && !isDay() && !OrdOrderUtils.checkIsTodayVisit(order)){
			// 2.2.[支付前置]普通支付成功+非工作时间+非当日入住+非保留房
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME_UNSTOCK_NOTTODAY.name());
			}
			// 2.3.[支付前置]普通支付成功+非工作时间
			else{
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_UNWORKTIME.name());
			}
		}
	}
	

	/**
	 * 是否可退改
	 * 1）、单酒店、自由行酒景同步商品退改，下发此条短信
	 * 条件：设置退改规则是可退改（含可退改、阶梯退改、同步商品退改），并且可退改一定设置最晚无损取消时间。
	 * @param order
	 * @return
	 */
	private boolean isCancelStrategy(OrdOrder order) {
		if (4 == order.getDistributorId() && order.getDistributionChannel() != 10000) {
			return false;
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
			if (order.getLastCancelTime() == null) {
				return false;
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy()) 
					&& ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getHotelCancelStrategy())) {
				return true;
			}
			
		}
		
		return false;
	}

	/**
	 * 是否可退改
	 * 条件：酒店套餐，景酒 产品 设置退改规则是阶梯退改
	 * @param order
	 * @return
	 */
	private boolean isHotelCancelStrategy(OrdOrder order) {
		if (4 == order.getDistributorId() && order.getDistributionChannel() != 10000) {
			return false;
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				return true;
			}
			
		}
		
		return false;
	}

	/**
	 * 是否是出境港澳台酒店(分销商必须是特卖，无线app或者无线wap)
	 * @param order
	 * @return
	 */
	private boolean isForeignLineHotelOrder(OrdOrder order){
		if (4 == order.getDistributorId()) {
			if(!ArrayUtils.contains(new Long[]{10000L,107L,10001L}, order.getDistributionChannel())){
				return false;
			}
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
			if (order.getMainOrderItem() == null) {
				return false;
			}
			if (ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(order.getMainOrderItem().getProductType())) {
				return true;
			}
		}
		return false;
	}

	private boolean needResourceConfirm(OrdOrder order){
		return order.isNeedResourceConfirm();
	}
	@Override
	public String fillSms(String content, OrdOrder order) {
		//如果是预授权支付成功,则调用该方法SEND_NODE = PREAUTH_WORKTIME_PREPAID 或者PREAUTH_UNWORKTIME_PREPAID
		return null;
	}

	private  boolean isNeedSpecialSms(OrdOrder order) {
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
        // 判断是否支付成功，并且是三个指定的商品。如果都符合则返回true 否则都是false
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        for (OrdOrderItem ordOrderItem : orderItemList) {
            Long suppGoodsId = ordOrderItem.getSuppGoodsId();
            if (suppGoodsId.equals(diamond) || suppGoodsId.equals(platinum) || suppGoodsId.equals(gold)) {
                return true;
            }
        }
        return false;
    }

    public boolean  orderStatus(OrdOrder order){
        if(Constant.ORDER_STATUS_ENUM.NORMAL.name().equalsIgnoreCase(order.getOrderStatus())){
            return true;
        }
        return false;
    }

}
