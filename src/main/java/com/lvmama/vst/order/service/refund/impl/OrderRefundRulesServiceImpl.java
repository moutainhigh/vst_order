package com.lvmama.vst.order.service.refund.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;
import com.lvmama.vst.order.service.refund.IOrderRefundRulesService;

@Service
public class OrderRefundRulesServiceImpl implements IOrderRefundRulesService{
	private static Logger LOG = LoggerFactory.getLogger(OrderRefundRulesServiceImpl.class);
	@Autowired
	private CategoryClientService categoryClientService;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	
	@Override
	public String getRouteOrderRefundRules(OrdOrder ordOrder) {
		StringBuffer rules = new StringBuffer();
		if(ordOrder == null){
			LOG.info("订单不存在");
			return rules.toString();
		}
		LOG.info("getRouteOrderRefundRules,orderId="+ordOrder.getOrderId());
		
		LOG.info("订单品类Id，categoryId = " + ordOrder.getCategoryId());
		
		OrdOrderPack orderPack = ordOrder.getOrdOrderPack();
		boolean needRefundDetails = false;
		if (orderPack != null && "LVMAMA".equals(orderPack.getOwnPack()) && (
				"INNERSHORTLINE".equals(orderPack.getContentStringByKey("route_product_type"))
				|| "INNER_BORDER_LINE".equals(orderPack.getContentStringByKey("route_product_type"))
				|| "INNERLONGLINE".equals(orderPack.getContentStringByKey("route_product_type"))
				|| "INNERLINE".equals(orderPack.getContentStringByKey("route_product_type"))
				)) {
			needRefundDetails = true;
		}
		if(!needRefundDetails && !BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())
				&& !BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())
				&& !BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(ordOrder.getCategoryId())
				&& !BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(ordOrder.getSubCategoryId())
				){
			//判断是否是自由行或者酒店套餐
			LOG.info("订单orderId = " + ordOrder.getOrderId() + "，不属于酒店套餐和自由行景酒，无法获取退改规则。");
			return rules.toString();
		}
		
		LOG.info("订单orderId = " + ordOrder.getOrderId() + "，退改策略cancelStrategy = " + ordOrder.getRealCancelStrategy());
		StringBuilder hotelRules = null;
		StringBuilder hotelCombRules = null;//酒店套餐和酒套餐公用
		StringBuilder ticketRules = null;
		StringBuilder insuranceRules = null;
		//除上面这几个类别外的存放
		Map<Long, StringBuilder> rulesMap = new HashMap<Long, StringBuilder>();
		
		List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
		for(OrdOrderItem orderItem : ordOrderItems) {
			LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
			Map<String,Object> contentMap = orderItem.getContentMap();
			LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));

			if(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId())
	                || BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
	                || BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(orderItem.getCategoryId())
	                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getCategoryId())){
				if (ticketRules == null) {
					ticketRules = new StringBuilder("<br/><hr/><br/>门票<br/>");
				}
				ticketRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
				//特卖会秒杀景酒主订单退改规则为‘同步商品退改’，子订单商品退改规则固定为‘不退不改’
				//非酒套，即景酒,目的地bu
				if(!BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())
						&& ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
						&&CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(ordOrder.getBuCode())
						&&ordOrder.getDistributionChannel()!=null && ordOrder.getDistributionChannel() == 110L){
					ticketRules.append("不退不改<br/>");
				}else {
					//显示子订单的阶梯退改规则
		            Map<String, Object> mp = differentialChildRule(ordOrder, orderItem);
		            ticketRules.append(mp.get("refoundStr"));
				}
	        } else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){//排除保险
	        	if (insuranceRules == null) {
					insuranceRules = new StringBuilder("<br/><hr/><br/>保险<br/>"); 
				}
	        	String destBuAccFlag = (String) contentMap.get("destBuAccFlag");
	        	OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(ordOrder.getOrderId());
	            
	            String travDelayFlag = null;
	            String travDelayStatus = null;

	            if (null != ordAccInsDelayInfo) {
	                travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
	                travDelayStatus = ordAccInsDelayInfo.getTravDelayStatus();
                }
	            
	        	if (StringUtils.isNotBlank(destBuAccFlag) && "Y".equalsIgnoreCase(destBuAccFlag) 
	        	        && StringUtils.isNotBlank(travDelayStatus) && travDelayStatus.equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.ABANDON.name())
	        	        && StringUtils.isNotBlank(travDelayFlag) && travDelayFlag.equalsIgnoreCase("Y")) {
	        	    insuranceRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "   <strong>[已取消]</strong>" + "<br/>");
	        	}else {
	        	    insuranceRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
	        	}

				//不退不改
				if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
					insuranceRules.append("不可退改<br/>");
				}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){
					insuranceRules.append("人工退改<br/>");
				}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
					LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
					Date minLastCancelTime = orderItem.getLastCancelTime();
					String lineae = "", end = "";
					if (null != minLastCancelTime) {
						if (new Date().compareTo(minLastCancelTime) == 1) {
							lineae = "<span class='lineae_line'>";
							end = "</span>";
						}
						insuranceRules.append("可退改 扣款金额[("+orderItem.getDeductAmountToYuan()+"元)]" +lineae+ DateUtil.getFormatDate(orderItem.getLastCancelTime(), DateUtil.HHMM_DATE_FORMAT) + end +"前无损取消<br/>");
					}else{
						insuranceRules.append("可退改 扣款金额[("+orderItem.getDeductAmountToYuan()+"元)]"+"<br/>");	
					}
				}
	        } else if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
	        	if (hotelRules == null) {
	        		hotelRules = new StringBuilder("<br/><hr/><br/>酒店<br/>"); 
	        	}
	        	hotelRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
				if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
					hotelRules.append(getChildRule(ordOrder, orderItem, ordOrder.getVisitTime()).get("refoundStr"));
				}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
						|| ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
					//特卖会秒杀景酒主订单退改规则为‘同步商品退改’，子订单商品退改规则固定为‘不退不改’   非酒套，即景酒,目的地bu
					if(!BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())
							&& !BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())
							&& ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
							&&CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(ordOrder.getBuCode())
							&&ordOrder.getDistributionChannel()!=null && ordOrder.getDistributionChannel() == 110L){
						hotelRules.append("不退不改<br/>");
					}else{
						String cancelDeatils = "人工退改<br/>";
						if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
							cancelDeatils = "不可退改<br/>";
						}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
							LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
							Date minLastCancelTime = orderItem.getLastCancelTime();
							String lineae = "", end = "";
							if (new Date().compareTo(minLastCancelTime) == 1) {
								lineae = "<span class='lineae_line'>";
								end = "</span>";
							}
							cancelDeatils = "可退改 扣款金额[("+orderItem.getDeductAmountToYuan()+"元)]" +lineae+ DateUtil.getFormatDate(orderItem.getLastCancelTime(), DateUtil.HHMM_DATE_FORMAT)+ end +"前无损取消<br/>";
						}
						hotelRules.append(cancelDeatils);
					}
				}else{
					hotelRules.append("不可退改<br/>");
				}
			} else if (BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId()) || BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) {
				if (hotelCombRules == null && BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) {
					hotelCombRules = new StringBuilder("<br/><hr/><br/>酒店套餐<br/>");
				}else{
					hotelCombRules = new StringBuilder("<br/><hr/><br/>酒套餐<br/>");
				}
				boolean needHotelcombRefundDetail = needRefundDetails 
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(ordOrder.getCategoryId());
				hotelCombRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
				if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
						&& needHotelcombRefundDetail){
					//主产品自由行，阶梯退改
					hotelCombRules.append(getChildRule(ordOrder, orderItem, ordOrder.getVisitTime()).get("refoundStr"));
				}else if((ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
							&& (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId()) || BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId())))
						|| (needHotelcombRefundDetail
								&& (ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
										|| ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy()))
								&& ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy()))){
					Map<String, Object> mp = getChildRule(ordOrder, orderItem, orderItem.getVisitTime());
					hotelCombRules.append(mp.get("refoundStr"));
				}else if(needHotelcombRefundDetail
								&& ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
					hotelCombRules.append(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCnName()+"</br>");
				}else{
					String refoundStr = "";
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
						refoundStr = ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCnName()+"</br>";
					}
					if( ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){
						refoundStr = ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCnName()+"</br>";
					}
					hotelCombRules.append(refoundStr);
				}
			} else if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_traffic_ship_other.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_traffic_train_other.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(orderItem.getCategoryId()) ) {
				
				StringBuilder sb = null;
				if ( !rulesMap.containsKey(orderItem.getCategoryId()) ) {
					sb = new StringBuilder("<br/><hr/><br/>" + BIZ_CATEGORY_TYPE.getCnName(orderItem.getCategoryId()) + "<br/>");
					rulesMap.put(orderItem.getCategoryId(), sb);
				} else {
					sb = rulesMap.get(orderItem.getCategoryId());
				}
				StringBuilder currItemRules = getRefundDetail(ordOrder, orderItem);
				sb.append(currItemRules);
			} else {	//关联销售（只显示门票）
				if (ticketRules == null) {
					ticketRules = new StringBuilder("<br/><hr/><br/>门票<br/>");
				}
				String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
				BizCategory bizCategory=result.getReturnContent();
				if(bizCategory != null && bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L)) {//门票
					//商品名称
					ticketRules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
					Map<String, Object> mp = getChildRule(ordOrder, orderItem, orderItem.getVisitTime());
					ticketRules.append(mp.get("refoundStr"));
				}
			}
		}
		rules.append(hotelRules==null?"":hotelRules);	//酒店
		rules.append(ticketRules==null?"":ticketRules);	//门票
		rules.append(hotelCombRules==null?"":hotelCombRules);	//酒店套餐
		
		for(Map.Entry<Long, StringBuilder> entry : rulesMap.entrySet()) {
			StringBuilder sb = entry.getValue();
			if(sb != null) {
				rules.append(sb);
			}
		}
		rules.append(insuranceRules==null?"":insuranceRules);	//保险
		LOG.info("ordOrder.getOrderId() = " + ordOrder.getOrderId() + "，rules = " + rules.toString());
		return rules.append("<br/>").toString();
	}

	private Map<String, Object> differentialChildRule(OrdOrder order, OrdOrderItem ordItem){
		Map<String, Object> mp = new HashMap<String, Object>();
		try{
			StringBuffer refoundStr = new StringBuffer("");
			List<SuppGoodsRefundVO> goodsRefundList = null;
			Date cancleDate = new Date();
			Date lastTime = this.getLastTime(ordItem);
			goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(ordItem, lastTime, cancleDate);
			//判断是不是期票
			Boolean isAperiodic = ordItem.hasTicketAperiodic();

			if(CollectionUtils.isNotEmpty(goodsRefundList)){
				if(isAperiodic && ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equalsIgnoreCase(order.getRealCancelStrategy())){
					List<SuppGoodsRefund> suppGoodsRefunds = new ArrayList<SuppGoodsRefund>();
					for(SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList){
						SuppGoodsRefund suppGoodsRefund = new SuppGoodsRefund();
						BeanUtils.copyProperties(suppGoodsRefundVO, suppGoodsRefund);
						suppGoodsRefunds.add(suppGoodsRefund);
					}
					refoundStr.append(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(suppGoodsRefunds, "Y"));
				}else {
					for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
						refoundStr.append(isBackRule(goodsRefundList.size() > 1, suppGoodsRefundVO, order));
					}
				}
			} else {
                if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
                    refoundStr.append(isBackRule(false, null, order));
                }
            }
			mp.put("refoundStr", refoundStr);
			return mp;
		}catch(Exception e){
			LOG.error("{}", e);
		}
		return mp;
	}

	private Map<String,Object> getChildRule(OrdOrder order, OrdOrderItem orderItem, Date visitDate){
		Map<String, Object> mp = new HashMap<String, Object>();
		try{
			StringBuffer refoundStr = new StringBuffer("");
			if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
				mp.put("refoundStr", ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCnName()+"</br>");
				return mp;
			}
			if( ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){
				mp.put("refoundStr", ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCnName()+"</br>");
				return mp;
			}

			Date cancleDate = new Date();
			visitDate = this.getLastTime(orderItem);
			List<SuppGoodsRefundVO> goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(orderItem, visitDate, cancleDate);

			if(CollectionUtils.isNotEmpty(goodsRefundList)){
				for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {

					//修改游玩日标准 add by lijuntao
					if(!SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(suppGoodsRefundVO.getCancelTimeType())){
						Date refundDate = DateUtils.addMinutes(visitDate, -suppGoodsRefundVO.getLatestCancelTime().intValue());
						suppGoodsRefundVO.setRefundDate(refundDate);
					}


					refoundStr.append(isBackDestBuRule(goodsRefundList.size() > 1, suppGoodsRefundVO, order));
				}
			} else {
                if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
                    refoundStr.append(isBackDestBuRule(false, null, order));
                }
            }
			mp.put("refoundStr", refoundStr);
			return mp;
		}catch(Exception e){
			LOG.error("{}", e);
		}
		return mp;
	}

    private String isBackRule(boolean moreThanOneRule, SuppGoodsRefundVO suppGoodsRefundVO, OrdOrder order){
        if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
            return "景区支付订单无退改规则</br>";
        }

		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
			return "不可退</br>";
		}

		if (StringUtils.isBlank(suppGoodsRefundVO.getCancelStrategy())
				|| SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())) {
			return "人工退改</br>";
		}

//		String isBack ="人工退改";
//		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
		String expCssPrefix = "";
		String expCssSuffix = "";
		Date currDate = new Date();
		if (suppGoodsRefundVO.getRefundDate() != null && currDate
						.compareTo(suppGoodsRefundVO.getRefundDate()) > 0) {
			expCssPrefix = "<span class='lineae_line'>";
			expCssSuffix = "</span>";
		}

		String isBack = "人工退改</br>";
		if(suppGoodsRefundVO.getRefundDate() != null) {
			isBack = "可退，扣款金额("
					+ PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())
					+ "元)，"
					+ expCssPrefix
					+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
							DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
					+ "</br>";
		}

		if(!SuppGoodsRefund.CANCEL_TIME_TYPE.OTHER.name().equals(suppGoodsRefundVO.getCancelTimeType())) {
			//阶梯退改的情况，直接返回
			return isBack;
		}
		//固定金额/不满足上述条件
		if(!moreThanOneRule) {//只有一条规则，即选择的是“订单未使用，扣除每张（份）”
			if(suppGoodsRefundVO.getDeductValue() <= 0) {
                if (DateUtil.diffDay(order.getVisitTime(), new Date()) <= 365) {
                    isBack = "未使用，无损退</br>";
                } else {
                    isBack = "<SPAN style='TEXT-DECORATION: line-through'>未使用，无损退</SPAN></br>";
                }
			} else {
				isBack = "未使用，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)</br>";
			}

			return isBack;
		}
		//规则多余一条，阶梯退改的情况
		if(SuppGoodsRefund.DEDUCTTYPE.AMOUNT.name().equals(suppGoodsRefundVO.getDeductType())) { //固定金额
			if(suppGoodsRefundVO.getDeductValue() <= 0) {
				isBack = "逾期未使用，无损退</br>";
			} else {
				isBack = "逾期未使用，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)，可退</br>";
			}
		} else if(SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefundVO.getDeductType())) {
			if(suppGoodsRefundVO.getDeductValue() <= 0) {
				isBack = "逾期未使用，无损退</br>";
			} else if(suppGoodsRefundVO.getDeductValue() == 10000) {//100%
				isBack = "逾期未使用，不可退</br>";
			} else {
				isBack = "逾期未使用，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)，可退</br>";
			}
		}

		return isBack;
	}


	/**
	 * 目的地bu产品退改规则生成方法 add by lijuntao
	 * @param moreThanOneRule
	 * @param suppGoodsRefundVO
	 * @param order
	 * @return
	 */
	private String isBackDestBuRule(boolean moreThanOneRule, SuppGoodsRefundVO suppGoodsRefundVO, OrdOrder order){
		if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
			return "现付订单无退改规则</br>";
		}

		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
			return "不可退</br>";
		}

		if (StringUtils.isBlank(suppGoodsRefundVO.getCancelStrategy())
				|| SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())) {
			return "人工退改</br>";
		}

//		String isBack ="人工退改";
//		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
		String expCssPrefix = "";
		String expCssSuffix = "";
		Date currDate = new Date();
		if (suppGoodsRefundVO.getRefundDate() != null && currDate
				.compareTo(suppGoodsRefundVO.getRefundDate()) > 0) {
			expCssPrefix = "<span class='lineae_line'>";
			expCssSuffix = "</span>";
		}

		String isBack = "人工退改</br>";
		if(suppGoodsRefundVO.getRefundDate() != null) {
			isBack = "可退，扣款金额("
					+ PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())
					+ "元)，"
					+ expCssPrefix
					+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
					DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
					+ "</br>";
		}

		if(!SuppGoodsRefund.CANCEL_TIME_TYPE.OTHER.name().equals(suppGoodsRefundVO.getCancelTimeType())) {
			//阶梯退改的情况，直接返回
			return isBack;
		}
		//固定金额/不满足上述条件
		if(!moreThanOneRule) {//只有一条规则，即选择的是“订单未使用，扣除每份”
//			if(suppGoodsRefundVO.getDeductValue() <= 0) {
//				if (DateUtil.diffDay(order.getVisitTime(), new Date()) <= 30) {
//					isBack = "可退，无损退</br>";
//				} else {
//					isBack = "不可退改</br>";
//				}
//			} else {
				isBack = "可退，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)</br>";
//			}

			return isBack;
		}
		//规则多余一条，阶梯退改的情况
		if(SuppGoodsRefund.DEDUCTTYPE.AMOUNT.name().equals(suppGoodsRefundVO.getDeductType())) { //固定金额

			isBack = "可退，不满足以上条件，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)</br>";

		} else if(SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefundVO.getDeductType())) {

			isBack = "可退，不满足以上条件，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)</br>";

		}

		return isBack;
	}
	@Override
	public Date getLastTime(OrdOrderItem orderItem) throws Exception {
		Date lastTime = null;
		//判断是不是期票
		Boolean isAperiodic = orderItem.hasTicketAperiodic();
		if(isAperiodic)
		{
			boolean isTicket = BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId())
			                || BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
			                || BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(orderItem.getCategoryId())
			                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getCategoryId());
			boolean isLastExp = false;
			String jsonStr = orderItem.getRefundRules();
			//List<SuppGoodsRefundVO> refundList = JSONArray.parseArray(jsonStr, SuppGoodsRefundVO.class); 
			List<SuppGoodsRefundVO> refundList =  new ArrayList<SuppGoodsRefundVO>();
			if(StringUtil.isNotEmptyString(jsonStr)){
				List<SuppGoodsRefundVO> refundList_ = JSONArray.parseArray(jsonStr, SuppGoodsRefundVO.class);
				if(refundList_ != null){
					refundList = refundList_;
				}
			}
			for(SuppGoodsRefundVO sgrv : refundList){
				if(SuppGoodsRefundVO.REFUND_TYPE.LAST_EXP.getCode().equals(sgrv.getRefundType())){
					isLastExp = true;
					break;
				}else if(SuppGoodsRefundVO.REFUND_TYPE.ORDER_DAY.getCode().equals(sgrv.getRefundType())){
					lastTime = DateUtil.getDayStart(orderItem.getCreateTime());
					break;
				}else if(SuppGoodsRefundVO.REFUND_TYPE.ANY_DAY.getCode().equals(sgrv.getRefundType())
						&& sgrv.getAnyDay()!=null){
					lastTime = sgrv.getAnyDay();
					break;
				}
			}
			Date suppGoodExpEndTime = null;
			if(isLastExp || (!isTicket && lastTime==null) ){
				//取出期票的有效时间
				//首选取快照中的最晚有效期
				Object expEndTime =  orderItem.getContentValueByKey("goodsEndTime");
				if(null != expEndTime){
					String expEndTimeStr = expEndTime.toString();
					SimpleDateFormat sdf=new SimpleDateFormat(DateUtil.HHMM_DATE_FORMAT);
					suppGoodExpEndTime = sdf.parse(expEndTimeStr);
				}
				//历史数据取子订单最晚游玩日有效期
				if(null == suppGoodExpEndTime){
					suppGoodExpEndTime = orderItem.getValidEndTime();
				}
				//最后取商品有效期
				if(null == suppGoodExpEndTime){
					SuppGoodsExp suppGoodExp =  suppGoodsClientService.findTicketSuppGoodsExp(orderItem.getSuppGoodsId());
					suppGoodExpEndTime = suppGoodExp.getEndTime();
				}
				if(isLastExp && null == suppGoodExpEndTime){
					LOG.info("子订单"+orderItem.getOrderItemId()+":suppGoodExpEndTime为null");
					throw new BusinessException("子订单"+orderItem.getOrderItemId()+"期票有效期为null");
				}
			}
			if(null != suppGoodExpEndTime){
				lastTime = suppGoodExpEndTime;
			}
		}else{
			lastTime = orderItem.getVisitTime();
		}
		
		LOG.info("子订单"+orderItem.getOrderItemId()+":lastTime为"+lastTime);
		return lastTime;
	}
	
	private StringBuilder getRefundDetail(OrdOrder ordOrder, OrdOrderItem orderItem) {
		StringBuilder rules = new StringBuilder();
    	Map<String,Object> contentMap = orderItem.getContentMap();
    	rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
		if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
			rules.append(getChildRule(ordOrder, orderItem, ordOrder.getVisitTime()).get("refoundStr"));
		}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())
				|| ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
			String cancelDeatils = "人工退改<br/>";
			if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
				cancelDeatils = "不可退改<br/>";
			}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
				LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
				Date minLastCancelTime = orderItem.getLastCancelTime();
				String lineae = "", end = "";
				if (new Date().compareTo(minLastCancelTime) == 1) {
					lineae = "<span class='lineae_line'>";
					end = "</span>";
				}
				cancelDeatils = "可退改 扣款金额[("+orderItem.getDeductAmountToYuan()+"元)]" +lineae+ DateUtil.getFormatDate(orderItem.getLastCancelTime(), DateUtil.HHMM_DATE_FORMAT)+ end +"前无损取消<br/>";
			}
			rules.append(cancelDeatils);
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(ordOrder.getCategoryId()) && BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())){
			String cancelDeatils = ProdRefund.CANCELSTRATEGYTYPE.getCnName(orderItem.getCancelStrategy());
			rules.append(cancelDeatils).append("<br/>");
		}else{
			rules.append("不可退改<br/>");
		}
		return rules;
	}

}
