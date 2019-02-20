package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.Constant;


/**
 * 出游短信
 * @author zhaomingzhu
 *
 */
public class OrderTravelSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderTravelSms.class);
	
	//品类(酒店)
	public boolean isHotel(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BizEnum.BIZ_CATEGORY_TYPE.category_hotel)){
				return true;
			}
		}
		return false;
	}
	//品类(门票) 并且 当前门票是非期票
	public boolean isNoTicket(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_show_ticket)
					){
				if(!item.hasTicketAperiodic()){
					Log.info("is ticket ORDER ID IS ="+order.getOrderId());
					return true;
				}
			}
		}
		return false;		
	}
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderTravelSms ===>>> isNoTicket(order)=" + isNoTicket(order)
				+ "isHotel(order)=" + isHotel(order)
				+"orderidexeSmsRule="+order.getOrderId()
			);
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.非期票----出游前一天19点+门票
		if(isNoTicket(order)){
				if(order.getCategoryId() == 11L || order.getCategoryId() == 12L || order.getCategoryId() == 13L || order.getCategoryId() ==BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
					//门票
					sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name());
				}else{
					sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_LINE_TICKET_DAY_BEFORE_REMIND.name());
				}
			}	
		
		
		//2.出游前一天19点+酒店
		if(isHotel(order)){
			if(isInclueLocalBuOrDestinationBu(order)&&isCategory(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND_NEW.name());
			}else{
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name());
			}
			
		}
		//当地玩乐 美食 娱乐 
		if(Long.valueOf(43L).equals(order.getCategoryId()) || Long.valueOf(44L).equals(order.getCategoryId())){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_TRAVEL_PLAY_DAY_BEFORE_REMIND.name());
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
	

	/**
	 * 判断是否国内bu或者目的地bu
	 */
	public boolean isInclueLocalBuOrDestinationBu(OrdOrder order){
		//如果是目的地bu或者是国内bu
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
				Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断品类含景酒、机酒
	 */
	public boolean isCategory(OrdOrder order){
		long categoryId=order.getCategoryId().longValue();
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())){//景酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())){//机酒
			return true;
		}
		return false;
	}
	
	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}
	
	/**
	 *  已支付状态下，发送此短信(限制为供应商打包：跟团游-短信和当地游-短线)
	 */
	public List<String> getShortLineNodeList(OrdOrder order,
			ProdProduct product) {
		if (order==null || product==null) {
			return null;
		}
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//已支付状态下，发送此短信(限制为供应商打包：跟团游-短信和当地游-短线)
		if (CommEnumSet.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())) {
			if(order.getCategoryId() == 15L){
				if ("SUPPLIER".equals(product.getPackageType()) && "INNERSHORTLINE".equals(product.getProductType())) {
					//属于跟团游-短线
					sendList.add(OrdSmsTemplate.SEND_NODE.PAY_PERFORM_PREVIOUS_DAY.name());
				}
			}
			if(order.getCategoryId() == 16L){
				if ("SUPPLIER".equals(product.getPackageType()) &&
	    				("INNERSHORTLINE".equals(product.getProductType()) || "INNERLINE".equals(product.getProductType()))) {
				//属于当地游-短线
				sendList.add(OrdSmsTemplate.SEND_NODE.PAY_PERFORM_PREVIOUS_DAY.name());
				}
			}
		}
			
		return sendList;
	}
}
