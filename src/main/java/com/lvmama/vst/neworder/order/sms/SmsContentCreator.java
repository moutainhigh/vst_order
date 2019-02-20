package com.lvmama.vst.neworder.order.sms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.order.processer.sms.SmsUtil;


public class SmsContentCreator {
	
	private final static Log LOG=LogFactory.getLog(SmsContentCreator.class);
	
	/**
	 * 预付支付前置订单短信（目前只适用于酒套餐）
	 * @param content  短信模板内容
	 * @param orderSmsPo 
	 * @return
	 */
	public static String fillSmsForPayAhead(String content, OrderSmsPo orderSmsPo) {
		OrdOrder order = orderSmsPo.getOrdOrder();
		ProductSmsPo productSmsPo = orderSmsPo.getProductSmsPo();
//		Map<String, String> customSmsMap = orderSmsPo.getCustomSmsMap();
		LOG.info("SmsUtil.fillSmsForPayAhead:orderId=" + order.getOrderId()+"==fillSmsForPayAhead:visitTime="+order.getVisitTime()+"==fillSmsForPayAhead:endTime="+order.getEndTime());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			int count = 0;
			for(OrdOrderItem item : order.getOrderItemList()){
				if(count > 0){
					sb.append(",");
				}
				sb.append(item.getProductName()).append(" ").append(item.getSuppGoodsName()).append(item.getQuantity()).append("份");
				count++;
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//出游时间
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
	    
	    //离店时间
	    param.put(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField(), DateUtil.formatDate(order.getEndTime(), "yyyy-MM-dd"));

	    //酒店地址productAddress
	    param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), "，酒店地址："+(productSmsPo.getProdAddress() != null ? productSmsPo.getProdAddress() : "暂未提供"));

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

		//担保方式cancelStrategy
		if (!SmsUtil.isCancelStrategy(order)) {
			String cancelStrategy = order.getRealCancelStrategy();
			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				cancelStrategy = order.getCancelStrategy();
			}
			
			if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			//当地玩乐 美食 娱乐 购物 退改策略
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
		}

		return SmsUtil.fillSms(content, order, param);
	}
}
