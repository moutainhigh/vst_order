package com.lvmama.vst.order.service.refund.impl.front;

import java.util.*;

import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;
import com.lvmama.vst.order.service.refund.OrdRefundSaleRecordService;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundAmountAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.refund.IOrderRefundFrontService;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundConstant;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;


/**
 * 自由行酒景
 * @author chenhao
 *
 */
@Service("orderRefundRouteSceneHotelFrontService")
public class OrderRefundRouteSceneHotelFrontServiceImpl implements IOrderRefundFrontService {
	private static final Log LOG = LogFactory.getLog(OrderRefundRouteSceneHotelFrontServiceImpl.class);
	@Autowired
	private OrderRefundComFrontService orderRefundComFrontService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	private OrderRefundAmountAdapter orderRefundAmountAdapter;

	@Autowired
	private OrdRefundSaleRecordService ordRefundSaleRecordService;
	
	@Override
	public String getOrderRefundApplyStatus(OrdOrder order, String refundApplyStatus) {
		//自由行酒+景退改按钮露出
		Long orderId =order.getOrderId();
		String cancelStrategy = order.getRealCancelStrategy();//退改类型
        String paymentTarget = order.getPaymentTarget();//支付方式
        String paymentStatus = order.getPaymentStatus();//支付状态
        Date visitTime = order.getVisitTime();//出游日期
        String orderStatus = order.getOrderStatus();//订单状态
        Date nowTime = new Date();

        //判断是否为目的地在线退款
        LOG.info("destBU online refund getOrderRefundApplyStatus ,orderId:"+orderId+" isDestBU :"+
				order.isDestBuRefund()+",cancelStrategy:"+cancelStrategy+",paymentTarget:"+paymentTarget +
				",paymentStatus:"+paymentStatus+" ,visitTime:"+visitTime+" ,orderStatus:"+orderStatus+"");
        if (!order.isDestBuRefund()) {//非目的地订单
            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND.getCode();
        }

		if (OrderRefundConstant.ORDER_VIEW_STATUS.UNPAY.getCode().equals(paymentStatus) ||
				OrderRefundConstant.ORDER_VIEW_STATUS.PARTPAY.getCode().equals(paymentStatus)) {//待支付、部分支付
			return Constant.VST_ORDER_CANCEL_STATUS.OTHER.getCode();
		}

		if (OrderRefundConstant.ORDER_STATUS.CANCEL.getCode().equals(orderStatus)) {//已取消
			if (!StringUtil.isEmptyString(refundApplyStatus)) {//如有退款单记录
				if (OrderRefundConstant.REFUNDMENT_STATUS.REFUNDED.getCode().equals(refundApplyStatus)) {    //退款成功
					LOG.info("RefundStatus is REFUNDED");
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_SUCCESS.getCode();
				} else if (OrderRefundConstant.REFUNDMENT_STATUS.FAIL.getCode().equals(refundApplyStatus)
						|| OrderRefundConstant.REFUNDMENT_STATUS.CANCEL.getCode().equals(refundApplyStatus)
						|| OrderRefundConstant.REFUNDMENT_STATUS.REJECTED.getCode().equals(refundApplyStatus)) {  //退款失败 || 订单取消 || 审核拒绝
					LOG.info("RefundStatus is FAIL or CANCEL or REJECTED");
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_FAIL.getCode();
				} else if (OrderRefundConstant.REFUNDMENT_STATUS.UNVERIFIED.getCode().equals(refundApplyStatus)
						|| OrderRefundConstant.REFUNDMENT_STATUS.VERIFIEDING.getCode().equals(refundApplyStatus)
						|| OrderRefundConstant.REFUNDMENT_STATUS.REFUND_APPLY.getCode().equals(refundApplyStatus)) {  //售后单正在审核
					LOG.info("RefundStatus is UNVERIFIED or VERIFIEDING");
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_CHECKING.getCode();
				} else if (OrderRefundConstant.REFUNDMENT_STATUS.WORKORDER.getCode().equals(refundApplyStatus)) { //工单特殊处理
					LOG.info("RefundStatus is WORKORDER");
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.ORDER_REFUND_BTN_DISABLE.getCode();
				} else {
					LOG.info("RefundStatus is Other");
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_PROCESSING.getCode();//默认为退款处理中
				}
			}
		}

		if (OrderRefundConstant.ORDER_STATUS.NORMAL.getCode().equals(orderStatus)) {//未取消
			Map<String, Object> params = new HashMap<>();
			params.put("orderId", orderId);
			List<OrdRefundSaleRecord> recordList = ordRefundSaleRecordService.findOrdRefundSaleRecordList(params);
			if(recordList !=null && recordList.size() > 0 ){
				return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_CHECKING.getCode();//存在退款你记录，退款审核中
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(cancelStrategy)) {//不可退改
				return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();//该订单不退不改
			} else if (ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(cancelStrategy)) {//人工退改
				return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CONTACT_SERVICE.getCode();//联系客服
			}  else {//同步商品退改或者可退该，且子订单全部为不退不改或者至少含有一个人工退改
				boolean unretreatandchange = true;
				boolean manualchange = false;
				for(OrdOrderItem ordOrderItem : order.getOrderItemList()){
					if(!ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrderItem.getCancelStrategy())){
						unretreatandchange = false;
					}
					if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrderItem.getCancelStrategy())){
						manualchange = true;
					}
				}
				if(unretreatandchange){//子订单中全部不退不改
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();//该订单不退不改
				}
				if(manualchange){//子订单中存在
					return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CONTACT_SERVICE.getCode();//联系客服
				}
			}
		}

		Long refundAmount = orderRefundAmountAdapter.getRefundAmount(order, nowTime);
        LOG.info("actualAmount = "+ order.getActualAmount() +" refundAmount = " + refundAmount + " orderId = " + orderId);
        if(refundAmount.equals(order.getActualAmount())){//退全款
        	return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_APPLY.getCode();
		}else if(refundAmount > 0) {
			return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.PARTIAL_REFUND_APPLY.getCode();
		}else {
			return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.OVER_LAST_CANCEL_TIME.getCode();
		}

	}

	@Override
	public void checkRefundOnlineByCommit(OrdOrder ordOrder)
			throws IllegalArgumentException {
		if (!ordOrder.isDestBuRefund() ||
                !Constant.PAYMENT_STATUS.PAYED.getCode().equals(ordOrder.getPaymentStatus()) ||
                Constant.ORDER_STATUS.CANCEL.getCode().equals(ordOrder.getOrderStatus())) {
            String s = "订单ID:" + ordOrder.getOrderId() + "不是目的地在线退款品类或未支付或已取消!";
            LOG.error(s);
            throw new IllegalArgumentException(s);
        }
		
	}

	@Override
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(OrdOrder simpleOrdOrder) {
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(simpleOrdOrder.getOrderId());

		//计算子订单退款明细
		Date nowDate = new Date();
		List<OrdRefundSaleRecord> saleRecordList = ordRefundSaleRecordService.getOrdRefundSaleRecordByOrder(ordOrder, nowDate);
		Map<String, OrdRefundSaleRecord> recordMap = new HashMap<>();
		for(OrdRefundSaleRecord record : saleRecordList){
			recordMap.put(String.valueOf(record.getOrderItemId()), record);
		}
		//自由行景+酒退款明细创建
		LOG.info("订单orderId = " + ordOrder.getOrderId() + "，退改策略cancelStrategy = " + ordOrder.getRealCancelStrategy());
		List<OrderRefundDetailVO> refundDetailList = new ArrayList<OrderRefundDetailVO>();
		
		if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy()) ||
				ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy()) ||
				ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//不退不改或者可退该或者人工退改

			List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
			for(OrdOrderItem orderItem : ordOrderItems) {
				OrderRefundDetailVO orderRefundDetailVO = new OrderRefundDetailVO();
				LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
				Map<String,Object> contentMap = orderItem.getContentMap();
				LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));
				
				orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
				//产品名
				orderRefundDetailVO.setItemProductName(orderItem.getProductName());
				//规格名
				orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
				if(orderItem.getOrderPackId() != null){//主订单自由行中被打包的商品
					LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
					//不退不改
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
						orderRefundDetailVO.setItemDeductExplain("该产品不可退改，入住如需修改或取消，一律收取订单的全部费用的100%作为损失费，敬请谅解！\n");
						//orderRefundDetailVO.setItemDeductAmount(itemDeductAmount);
					}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//人工退改
						orderRefundDetailVO.setItemDeductExplain("该产品支持人工退改，可致电24小时服务热线1010-6060。\n");
					}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//可退该
						LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
						//List<ProdRefundRule> rulesList = JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
						List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
							if(refundList_ != null){
								rulesList = refundList_;
							}
						}
						StringBuffer refundRulesStr = new StringBuffer();
						for(ProdRefundRule rule: rulesList){
							refundRulesStr.append(rule.getRuleDesc(ordOrder.getVisitTime())+"\n");
						}
						orderRefundDetailVO.setItemDeductExplain(refundRulesStr.toString());
					}
				}else{//关联销售（只显示门票）
					String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
					BizCategory bizCategory=result.getReturnContent();
					if((bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L))) {//门票
						//将门票的的退改规则快照转化为商品退改规则list
						//List<SuppGoodsRefund> refundList = JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
						List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
							if(refundList_ != null){
								refundList = refundList_;
							}
						}
						orderRefundDetailVO.setItemDeductExplain(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(refundList,contentMap.get("aperiodic_flag").toString())+"\n");
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){//保险
						orderRefundDetailVO.setItemDeductExplain("在"+DateUtil.getFormatDate(orderItem.getLastCancelTime(),DateUtil.HHMM_DATE_FORMAT)+
								"前您可免费变更/取消订单，超时变更/取消订单，将扣除保险费用。"+"\n");
					}
				}
				if(recordMap.get(String.valueOf(orderItem.getOrderItemId())) != null){
					orderRefundDetailVO.setItemDeductAmount(recordMap.get(String.valueOf(orderItem.getOrderItemId())).getDeductAmount());
				}
				refundDetailList.add(orderRefundDetailVO);
			}
		}else if(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//同步商品退改
			List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
			
			for(OrdOrderItem orderItem : ordOrderItems){
				LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
				OrderRefundDetailVO orderRefundDetailVO = new OrderRefundDetailVO();
				Map<String,Object> contentMap = orderItem.getContentMap();
				String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
				BizCategory bizCategory=result.getReturnContent();

				LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));
				if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()) ){//酒店
					SuppGoodsTimePrice timePrice = new SuppGoodsTimePrice();
					timePrice.setDeductValue(orderItem.getDeductAmount());
					timePrice.setDeductType(orderItem.getDeductType());
					
					orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
					//产品名
					orderRefundDetailVO.setItemProductName(orderItem.getProductName());
					//规格名
					orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
					
					if (SuppGoods.PAYTARGET.PREPAID.name().equalsIgnoreCase(ordOrder.getPaymentTarget())) {
						if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())) { // 预付可退改
							LOG.info("----------查查看子订单所属BU----------------"+orderItem.getBuCode());
							if(CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(orderItem.getBuCode())){
								orderRefundDetailVO.setItemDeductExplain("在"+DateUtil.getFormatDate(orderItem.getLastCancelTime(),DateUtil.HHMM_DATE_FORMAT)+
										"前您可免费变更/取消订单，超时变更/取消订单，酒店将扣除房费。"+"\n");
							}else{
								orderRefundDetailVO.setItemDeductExplain("在"+DateUtil.getFormatDate(orderItem.getLastCancelTime(),DateUtil.HHMM_DATE_FORMAT)+
										"前您可免费变更/取消订单，超时变更/取消订单，酒店将扣除房费￥"+orderItem.getDeductAmountToYuan()+"。\n");
							}							
						}else{
							orderRefundDetailVO.setItemDeductExplain("订单一经预订成功，不可变更/取消，如未入住将扣除" + SuppGoodsTimePrice.getReturnMessage(timePrice)+"。\n");
						}
					}
					if(recordMap.get(String.valueOf(orderItem.getOrderItemId())) != null){
						orderRefundDetailVO.setItemDeductAmount(recordMap.get(String.valueOf(orderItem.getOrderItemId())).getDeductAmount());
					}
					refundDetailList.add(orderRefundDetailVO);
				}else if(bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L)){//门票
					orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
					//产品名
					orderRefundDetailVO.setItemProductName(orderItem.getProductName());
					//规格名
					orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
					//将门票的的退改规则快照转化为商品退改规则list
					//List<SuppGoodsRefund> refundList = JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
					List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
					if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
						List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
						if(refundList_ != null){
							refundList = refundList_;
						}
					}
					
					orderRefundDetailVO.setItemDeductExplain(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(refundList,contentMap.get("aperiodic_flag").toString())+"\n");
					if(recordMap.get(String.valueOf(orderItem.getOrderItemId())) != null){
						orderRefundDetailVO.setItemDeductAmount(recordMap.get(String.valueOf(orderItem.getOrderItemId())).getDeductAmount());
					}
					refundDetailList.add(orderRefundDetailVO);
				}else if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
					orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
					//产品名
					orderRefundDetailVO.setItemProductName(orderItem.getProductName());
					//规格名
					orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
					
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){//不退不改
						orderRefundDetailVO.setItemDeductExplain("该产品不可退改，入住如需修改或取消，一律收取订单的全部费用的100%作为损失费，敬请谅解！\n");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){//人工退改
						orderRefundDetailVO.setItemDeductExplain("该产品支持人工退改，可致电24小时服务热线1010-6060。\n");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){//可退改
						//将快照中的退改规则转化为产品阶梯退改规则list
						//List<ProdRefundRule> rulesList = JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
						List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
							if(refundList_ != null){
								rulesList = refundList_;
							}
						}
						StringBuffer refundRulesStr = new StringBuffer();
						for(ProdRefundRule rule: rulesList){
							refundRulesStr.append(rule.getRuleDesc(ordOrder.getVisitTime())+"\n");
						}
						orderRefundDetailVO.setItemDeductExplain(refundRulesStr.toString());
					}
					if(recordMap.get(String.valueOf(orderItem.getOrderItemId())) != null){
						orderRefundDetailVO.setItemDeductAmount(recordMap.get(String.valueOf(orderItem.getOrderItemId())).getDeductAmount());
					}
					refundDetailList.add(orderRefundDetailVO);
				}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){//门票
					orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
					//产品名
					orderRefundDetailVO.setItemProductName(orderItem.getProductName());
					//规格名
					orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
					
					orderRefundDetailVO.setItemDeductExplain("在"+DateUtil.getFormatDate(orderItem.getLastCancelTime(),DateUtil.HHMM_DATE_FORMAT)+
							"前您可免费变更/取消订单，超时变更/取消订单，将扣除保险费用。"+"\n");
					if(recordMap.get(String.valueOf(orderItem.getOrderItemId())) != null){
						orderRefundDetailVO.setItemDeductAmount(recordMap.get(String.valueOf(orderItem.getOrderItemId())).getDeductAmount());
					}
					refundDetailList.add(orderRefundDetailVO);
				}
				
			}
			
		}
		LOG.info("orderId = " + ordOrder.getOrderId() + " getOrderRefundDetailVO End，refundDetailList size = " + refundDetailList.size());
		
		return refundDetailList;
	}
	
    
    

}
