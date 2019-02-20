/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.presale;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * @author lancey
 *
 */
@Component("preSaleOrderItemBussiness")
public class PreSaleOrderItemBussiness  extends AbstractBookService implements OrderInitBussiness{
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
//		BuyInfo buyInfo = order.getBuyInfo();
//		OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;
//		String error = fillOrderItemContentWithHotelAdditationInfo(orderItem,orderItemDTO.getItem(),order,orderItem.getSuppGoods());
//		if(StringUtils.isNotEmpty(error)){
//			throwIllegalException(error);
//		}
		
		return true;
	}
	
	/**
	 * 填充酒店附加信息到订单子项
	 * 
	 * @param orderItem 订单子项PO
	 * @param item 订单子项VO
	 * @param ordOrderDTO 订单
	 * @param branchName 规格名称
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String fillOrderItemContentWithHotelAdditationInfo(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO,SuppGoods suppGoods) {
		String errorMsg = null;
		// 因为HotelAdditation对象属性并不是全部加入到JSON，所以不易采用反射机制。
		if ((item != null) && (item.getHotelAdditation() != null)) {
			errorMsg = checkHotelAdditationInfo(item, suppGoods);
			
			if (errorMsg == null) {
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name(), item.getHotelAdditation().getArrivalTime());
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name(), item.getHotelAdditation().getEarlyArrivalTime());
				String str="无";
				Map<String,Object> propValueMap = prodProductClientService.getProdProductBranchProp(suppGoods.getProdProductBranch().getBranchId(), suppGoods.getProductBranchId());
				if(propValueMap.containsKey("add_bed_flag")){
					List<PropValue> list = (List<PropValue>)propValueMap.get("add_bed_flag");
					if(CollectionUtils.isNotEmpty(list)){
						str =list.get(0).getName();
						if(StringUtils.isNotEmpty(list.get(0).getAddValue())){
							str += list.get(0).getAddValue();
						}
					}
				}
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.addBedFlag.name(), str);
				
				str="无";
				if(propValueMap.containsKey("internet")){
					List<PropValue> list = (List<PropValue>)propValueMap.get("internet");
					if(CollectionUtils.isNotEmpty(list)){
						str =list.get(0).getName();
						if(StringUtils.isNotEmpty(list.get(0).getAddValue())){
							str += list.get(0).getAddValue();
						}
					}
				}
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.internet.name(), str);
			}
		}

		return errorMsg;
	}
	
	/**
	 * 验证酒店附件信息
	 * 
	 * @param item
	 * @return
	 */
	private String checkHotelAdditationInfo(Item item, SuppGoods suppGoods) {
		String errorMsg = null;
		HotelAdditation hotelAdditation = item.getHotelAdditation();
		String timeReg = "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])";
		
		if (hotelAdditation != null) {
			if (hotelAdditation.getArrivalTime() != null) {
				if (hotelAdditation.getEarlyArrivalTime() == null) {
					Map<String,Object> propMap = prodProductClientService.findProdProductProp(suppGoods.getProdProduct().getBizCategoryId(), suppGoods.getProductId());
					String earlyArrivalTime = propMap.get("earliest_arrive_time").toString();
					if (earlyArrivalTime != null) {
						if (earlyArrivalTime.indexOf(":") < 0) {
							earlyArrivalTime = earlyArrivalTime + ":00";
						}
						
						if (!earlyArrivalTime.matches(timeReg)) {
							errorMsg = "酒店入住的最早到达时间不正确，earlyArrivalTime=" + earlyArrivalTime;
							LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
						}
					}
					hotelAdditation.setEarlyArrivalTime(earlyArrivalTime);
				}
				
				if (!hotelAdditation.getArrivalTime().matches(timeReg)) {
					errorMsg = "酒店入住的到达时间格式不正确，arrivalTime=" + hotelAdditation.getArrivalTime();
					LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
				}
				
				if (hotelAdditation.getEarlyArrivalTime() != null) {
					if (!hotelAdditation.getEarlyArrivalTime().matches(timeReg)) {
						errorMsg = "酒店入住的最早到达时间格式格式不正确，earlyArrivalTime=" + hotelAdditation.getEarlyArrivalTime();
						LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
					} else {
						Date visitTime = item.getVisitTimeDate();
						String[] earlyTimeStrs = hotelAdditation.getEarlyArrivalTime().split(":");
						int hour = Integer.parseInt(earlyTimeStrs[0]);
						int min = Integer.parseInt(earlyTimeStrs[1]);
						visitTime = DateUtil.DsDay_HourOfDay(visitTime, hour);
						visitTime = DateUtil.DsDay_Minute(visitTime, min);
						Date now = new Date();
						
						if (visitTime.before(now)) {
							now = DateUtils.addMinutes(now, 40);
							Calendar c = Calendar.getInstance();
							c.setTime(now);
							hour = c.get(Calendar.HOUR_OF_DAY);
							min = c.get(Calendar.MINUTE);
							StringBuffer sb=new StringBuffer();
							sb.append(hour);
							sb.append(":");
							if(min<30){
								sb.append("00");
							}else{
								sb.append("30");
							}
							hotelAdditation.setEarlyArrivalTime(sb.toString());
						}
					}
				}
			} else {
				errorMsg = "请您填写酒店入住的到达时间。";
			}
		} else {
			errorMsg = "请您填写酒店入住的相关信息。 ";
		}
		
		return errorMsg;
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(PreSaleOrderItemBussiness.class);
}
