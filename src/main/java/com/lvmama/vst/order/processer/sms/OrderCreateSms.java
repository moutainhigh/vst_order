package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.order.utils.OrderUtils;

/**
 * 订单提交
 * @author zhaomingzhu
 *
 */
public class OrderCreateSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderCreateSms.class);
	
	//品类(酒店)
	public boolean isHotel(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BizEnum.BIZ_CATEGORY_TYPE.category_hotel)){
				return true;
			}
		}
		return false;
	}
	//支付对象(现付)
	public boolean isPay(OrdOrder order){
		if(order.hasNeedPay()){
			return true;
		}else{
			return false;
		}
	}
	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	
	//订单提交 - 审核通过
	public boolean hasInfoAndResourcePass(OrdOrder order){
		if(order.hasResourceAmple()){
			return true;
		}else{
			return false;
		}
	}
	
	//预订限制 (预授权)
	public boolean isPreauth(OrdOrder order){
		if(order.getPaymentType() != null && SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(order.getPaymentType())){
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
	//支付状态(已支付)
	public boolean isPayed(OrdOrder order){
		if(order.hasPayed()){
			return true;
		}else{
			return false;
		}
	}
	//已担保
	public boolean isGuarantee(OrdOrder order){
		if(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equalsIgnoreCase(order.getGuarantee())){
			return true;
		}else{
			return false;
		}
	}
	
	//品类(门票) 并且 当前门票是期票
	public boolean isTicket(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(checkTicketItem(item)){
				if(item.hasTicketAperiodic()){
					return true;
				}
			}
		}
		return false;		
	}
	//门票、EBK或者传真
	public boolean isEbkOrFax(OrdOrder order){
		Boolean IS_EBK_NOTICE = Boolean.FALSE;       	//供应商通知方式是否为EBK
		Boolean IS_FAX_NOTICE = Boolean.FALSE;       	//供应商通知方式是否为传真
		Boolean TWO_CODE_FLAG = getTwoCodeFlag(order); 	//是否是二维码对接
		String fax = OrderEnum.ORDER_COMMON_TYPE.fax_flag.name();
		String ebk = OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name();
		for(OrdOrderItem item : order.getOrderItemList()){
			if(!IS_FAX_NOTICE){
				IS_FAX_NOTICE = item.hasContentValue(fax, "Y");
			}
			if(!IS_EBK_NOTICE){
				IS_EBK_NOTICE = item.hasContentValue(ebk, "Y");
			}
			//如果商品详情页。是否二维码，是否是传真，是否是EBK都没选的情况。需要发送一条预定成功的短信
			if (!IS_EBK_NOTICE && !IS_FAX_NOTICE && !TWO_CODE_FLAG) {
				return true;
			}	
			if(IS_FAX_NOTICE || IS_EBK_NOTICE){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断是否二维码
	 * @param order
	 * @return
	 */
	public boolean getTwoCodeFlag(OrdOrder order){
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
	
	
	public boolean checkTicketItem(OrdOrderItem item) {
		return item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
				||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
				||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket);
	}
	//品类(门票) 并且 当前门票是非期票
	public boolean isNoTicket(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(checkTicketItem(item)){
				if(!item.hasTicketAperiodic()){
					return true;
				}
			}
		}
		return false;		
	}
	
	//品类(门票) 并且 当前门票是期票
	public boolean isStamp(OrdOrder order){
		return StringUtils.equals(order.getOrderSubType(), OrderEnum.ORDER_STAMP.STAMP.name());
	}
	
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderCreateSms ===>>> hasInfoAndResourcePass(order)=" + hasInfoAndResourcePass(order)
				+ "isPreauth(order)=" + isPreauth(order)
				+ "isDay(order)=" + isDay()
				+ "isPrepaid(order)=" + isPrepaid(order)
				+ "isPayed(order)=" + isPayed(order)
				+ "isGuarantee(order)=" + isGuarantee(order)
				+ "isHotel(order)=" + isHotel(order)
				+ "isNoTicket(order)=" + isNoTicket(order)
				+ "isTicket(order)=" + isTicket(order)
				+ "isStamp(order)=" + isStamp(order)
				+"orderidexeSmsRule="+order.getOrderId()
			);			
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.订单提交+[主订单]待审核+非强制预授权+工作时间
		if(!hasInfoAndResourcePass(order) && !isPreauth(order) && isDay()){
			if(OrderUtils.isIncludeFlightExcludeXOrderItem(order)){
				//@todo 加含机票的订单短信模板 对应于需求中的第三点 "工作时间下单 资源紧张短信"
				sendList.add(OrdSmsTemplate.SEND_NODE.FLIGHT_ORDER_CREATE_UNVERIFIED_WORKTIME.name());
			}else{
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_UNPREAUTH_WORKTIME.name());
			}
		}
		//2.订单提交+[主订单]待审核+非强制预授权+非工作时间
		if(!hasInfoAndResourcePass(order) && !isPreauth(order) && !isDay()){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_UNPREAUTH_UNWORKTIME.name());
		}
		//3.订单提交+强制预售权 	(订单提交+待审核 +强制预售权 +预付)
		if(isPreauth(order) && isPrepaid(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_PREAUTH.name());
		}
		//4.订单提交+[主订单]已审核+未支付	(订单提交+[主订单]已审核+未支付 +预付)
		if(hasInfoAndResourcePass(order) && !isPayed(order) && isPrepaid(order) && !isPreauth(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID.name());
		}
		//5.订单提交+[主订单]已审核+担保且已担保  (订单提交+[主订单]已审核+担保且已担保+酒店+现付)
		if(hasInfoAndResourcePass(order) && isGuarantee(order) && isHotel(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_GUARANTEE.name());
			if (isCancelStrategy(order)) {
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
			}
			//酒店套餐阶梯退改和景酒阶梯退改
			if (isHotelCancelStrategy(order)) {
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
			}
		}
		//6.订单提交+[主订单]已审核+非担保	(订单提交+[主订单]已审核+非担保+酒店+现付)
		if(hasInfoAndResourcePass(order) && !isGuarantee(order) && isHotel(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNGUARANTEE.name());
		}
		//7.订单提交+[主订单]已审核+期票 		(订单提交+[主订单]已审核+期票+门票+现付)
		if(hasInfoAndResourcePass(order) && isTicket(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC.name());
		}
		//8.订单提交+[主订单]已审核+非期票	(订单提交+[主订单]已审核+期票+门票+现付)
		if(hasInfoAndResourcePass(order) && isNoTicket(order) && isPay(order) && isEbkOrFax(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC.name());
		}
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		return sendList;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		//如果是订单提交资源审核中ORDER_CREATE_UNVERIFIED_UNPREAUTH_WORKTIME 或 ORDER_CREATE_UNVERIFIED_UNPREAUTH_UNWORKTIME
		//或ORDER_CREATE_UNVERIFIED_PREAUTH则调用该方法
		return null;
	}

	/**
	 * 是否可退改
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
	
}
