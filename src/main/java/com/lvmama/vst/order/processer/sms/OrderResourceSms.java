package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lvmama.vst.back.prod.po.ProdProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.order.utils.OrderUtils;


/**
 * 订单审核通过
 * @author zhaomingzhu
 *
 */
public class OrderResourceSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderResourceSms.class);
	private boolean prepayFlag;
	
	

	public OrderResourceSms(boolean prepayFlag) {
		super();
		this.prepayFlag = prepayFlag;
	}
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
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)){
				if(item.hasTicketAperiodic()){
					return true;
				}
			}
		}
		return false;		
	}
	//品类(门票) 并且 当前门票是非期票
	public boolean isNoTicket(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)){
				if(!item.hasTicketAperiodic()){
					return true;
				}
			}
		}
		return false;		
	}
	//门票 并且 期票并且都是二维码
	public boolean isAllTicketQrCode(OrdOrder order){
		int count = 0;
		int num = 0;
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)){
				if(item.hasTicketAperiodic()){
					num++;//期票的item个数
					if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
							SuppGoods.NOTICETYPE.QRCODE.name())){
						count++;//期票+二维码的item个数
					}
				}
			}
		}
		if(count>0 &&count == num && count==order.getOrderItemList().size()){//(期票+二维码)的item个数等于期票个数
			return true;
		}
		return false;		
	}
	//门票 并且 非期票并且都是二维码
	public boolean isAllNoTicketQrCode(OrdOrder order){
		int count = 0;
		int num = 0;
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)){
				if(!item.hasTicketAperiodic()){
					num++;//非期票的item个数
					if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
							SuppGoods.NOTICETYPE.QRCODE.name())){
						count++;//期票+二维码的item个数
					}
				}
			}
		}
		if(count == num && count == order.getOrderItemList().size()){//(非期票+二维码)的item个数等于期票个数
			return true;
		}
		return false;		
	}
	
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderResourceSms ===>>> isPrepaid(order)=" + isPrepaid(order)
				+ "isPayed(order)=" + isPayed(order)
				+ "prepayFlag=" + prepayFlag
				+ "isGuarantee(order)=" + isGuarantee(order)
				+ "order.isPayMentType()=" + order.isPayMentType()
				+ "isHotel(order)=" + isHotel(order)
				+ "isPay(order)=" + isPay(order)
				+ "isTicket(order)=" + isTicket(order)
				+ "isNoTicket(order)=" + isTicket(order)
				+ "isAllTicketQrCode(order)=" + isAllTicketQrCode(order)
				+ "isAllNoTicketQrCode(order)=" + isAllNoTicketQrCode(order)
				+"orderidexeSmsRule="+order.getOrderId()
			);	
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&OrdOrderUtils.isLocalBuFrontOrder(order)
				&&((BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
				&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue())){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()
					==order.getCategoryId().longValue()){
				boolean hasFlightOrder = false;
				boolean hasHotelOrder = false;
				List<OrdOrderItem> orderItemList = order.getOrderItemList();
				for(OrdOrderItem orderitem : orderItemList){
					if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue() 
							== orderitem.getCategoryId()){
						hasFlightOrder =true;
						break;
					} else if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue() 
							== orderitem.getCategoryId()){
						hasHotelOrder =true;
					}
				}
				if(hasFlightOrder){  //如果存在机票子单
					sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE.name());
					return sendList;
				} else { //如果没有机票子单
					if(hasHotelOrder){  //如果有酒店子单
						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID.name());
						return sendList;
					} else {  //没有机票子单，也没有酒店子单，是单门票。此处不发短信
						return sendList;
					}
				}
			}
			sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE.name());
			return sendList;
		}
		
		//判断订单是否为国内巴士+酒订单
		if((BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode()))
				&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
				&&BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()==order.getSubCategoryId()){
			logger.info("OrderPaymentSms:orderId:"+order.getOrderId()+"=enter=国内巴士+酒支付前置,资审完成=");
			//获取模板
			sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_HOTEL_RESOURCE_AMPLE.name());
			for(OrdOrderItem orderItem : order.getOrderItemList()){
				if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
					sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_BUS_RESOURCE_AMPLE.name());
					return sendList;
				}
			}
			return sendList;
		}
		
		//1.[主订单]已审核+已支付+自由行机酒
		if (BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
				&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()) 
				&& isPayed(order)){
			
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_ROUTE_FLIGHT_HOTEL.name());
		}
		//1.[主订单]已审核+未支付		([主订单]已审核+未支付+预付)
		else if(isPrepaid(order)){
			if(!isPayed(order)){
				//对交通+X品类做特殊处理，有其自己的模板
				if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId() == order.getCategoryId()) {
					sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_ROUTE_AERO_HOTEL.name());
				} else if(OrderUtils.isIncludeFlightExcludeXOrderItem(order)){
					//@todo 加含机票的订单短信模板 对应于需求中的第一点 "预订成功后短信模板"
					sendList.add(OrdSmsTemplate.SEND_NODE.FLIGHT_VERIFIED_PREPAID.name());
				} else {
					sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID.name());
					if (isCancelStrategy(order)) {
						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
					}
					if (isHotelCancelStrategy(order)) {
						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
					}
				}
			}else{
				//对交通+X品类做特殊处理，有其自己的模板
				if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId() == order.getCategoryId()) {
					sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_ROUTE_AERO_HOTEL.name());
				} 
				//1.1 目的地BU前台订单-[支付前置]审核通过+预授权已授权/常规支付
				else if(OrdOrderUtils.isDestBuFrontOrder(order)){
					
					//add by xiachengliang 迪斯尼新增短信模板
					boolean isDisiney = false;
					List<OrdOrderItem> orderItemList = order.getOrderItemList();
					logger.info("OrderResourceSms orderItemList size:"+orderItemList.size());
					for(OrdOrderItem orderitem : orderItemList){
						if(orderitem.getSupplierId() != null && orderitem.getSupplierId() == 21435 && orderitem.getCategoryId() == 1){
							isDisiney =true;
							break;
						}
					}
					if(isDisiney){//迪斯尼订单
						logger.info("OrderResourceSms disiney message:"+order.getOrderId());
						sendList.add(OrdSmsTemplate.SEND_NODE.DISNEY_VERIFIED_PAYED_PREPAID.name());
					}
					// 保留房不发生效短信
					if(!OrdOrderUtils.hasStockFlag(order)){
						
						if(!isDisiney){//迪斯尼订单就不发了
							if(isForeignLineHotelOrder(order)){//判断是否是出境港澳台酒店
								sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTEL_FOREIGNLINE.name());
							}else {
								sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID.name());
							}
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
					}
				}
				//1.2其他订单
				else{
					if(prepayFlag){
						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY.name());
						if (isCancelStrategy(order)) {
							sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
						}
						if (isHotelCancelStrategy(order)) {
							sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
						}
					}else if(order.isPayMentType()){
						sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED.name());
					}		
				}
			}
		}
		//2.[主订单]已审核+担保且已担保 ([主订单]已审核+担保且已担保+酒店+现付)
		if(isGuarantee(order) && isHotel(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_GUARANTEE.name());
			if (isCancelStrategy(order)) {
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY.name());
			}
			if (isHotelCancelStrategy(order)) {
				sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY.name());
			}
		}
		//3.[主订单]已审核+非担保 	([主订单]已审核+非担保+酒店+现付)
		if(!isGuarantee(order) && isHotel(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNGUARANTEE.name());
		}
		//4.[主订单]已审核+期票		([主订单]已审核+期票+门票+现付)
		if(isTicket(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC.name());
		}
		//5.[主订单]已审核+非期票		([主订单]已审核+非期票+门票+现付)
		if(isNoTicket(order) && isPay(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC.name());
		}
		//6.当订单有且仅有二维码，则不发送，[主订单]已审核+期票
		if(isAllTicketQrCode(order) && isPay(order)){
			noneSendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC.name());
		}
		//7.当订单有且仅有二维码，则不发送，[主订单]已审核+非期票
		if(isAllNoTicketQrCode(order)){
			noneSendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC.name());
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
		return null;
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
	 * 条件：酒店套餐，景酒 产品 设置退改规则是阶梯退改。
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
}
