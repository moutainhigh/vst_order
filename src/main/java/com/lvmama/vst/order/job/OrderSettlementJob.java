/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.supp.po.SuppSettleRule;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrderSettlementService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.SettlementServiceAdapter;

/**
 * 自动废单
 * 针对已经资源审核，并且过了支付最晚时间的订单做废单处理
 * @author lancey
 *
 */
@Service
public class OrderSettlementJob implements Runnable{
	private static final Log LOG = LogFactory.getLog(OrderSettlementJob.class);
	
	//订单取消原因
	public static String ORDER_CANCEL_REASON = "超过支付等待时间自动废单";
	//订单取消操作人ID
	public static String ORDER_CANCEL_OPERATOR_ID = "SYSTEM";
	
	@Autowired
	private SettlementServiceAdapter settlementServiceAdapter;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	
	
	@Autowired
	private IOrderUpdateService ordOrderUpdateService;
	
	@Autowired
	@Resource(name="orderServiceRemote")
	private IOrderLocalService orderLocalService;

	@Autowired
	private OrderSettlementService orderSettlementService;
	
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;

	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			
			
			Date nowDate=new Date();
			//保证每次请求都是一个新的对象
			ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
			
			condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
			condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
			
			//到付
			condition.getOrderContentParam().setPayTarget(SuppGoods.PAYTARGET.PAY.getCode());

			//组装订单状态类条件
			condition.getOrderStatusParam().setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.getCode());
			condition.getOrderStatusParam().setInfoStatus(OrderEnum.INFO_STATUS.INFOPASS.getCode());
			condition.getOrderStatusParam().setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());

			//组装订单时间类条件
			condition.getOrderTimeRangeParam().setVisitTimeBegin(DateUtils.addDays(nowDate, -1));
			condition.getOrderTimeRangeParam().setVisitTimeEnd(nowDate);

			
			List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
			for (OrdOrder ordOrder : orderList) {
				
				List<SettlementItem> settlementItemList=this.fillSettlementItemList(ordOrder);
				orderSettlementService.batchInsertSettlementItem(settlementItemList);
				/*
				for (SettlementItem settlementItem : settlementItemList) {
					
					settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.NORMAL.name());//状态  
					settlementServiceAdapter.insertSettlementItem(settlementItem);
				}*/
			}

			
		}
		
	}
	
	//批量获取结算状态
	public List<SetSettlementItem> getSetSettlementItem(List<OrdOrderItem> orderItemList){
		List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
			List<Long> itemIds = new ArrayList<Long>();
			try {
			for (OrdOrderItem ordOrderItem : orderItemList) {
				itemIds.add(ordOrderItem.getOrderItemId());
			}
			setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
			} catch (Exception e) {
				throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
			}
		return setSettlementItems;
	}

	
	public String getSettlementStatus(List<SetSettlementItem> setSettlementItems,Long orderItemId){
		if(null!=setSettlementItems && setSettlementItems.size()>0){
			for(int i=0;i<setSettlementItems.size();i++){
				if(setSettlementItems.get(i).getOrderItemMetaId().equals(orderItemId)){
					return setSettlementItems.get(i).getSettlementStatus();
				}
			}
		}
		return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}
	/**
	 * 填充数据SettlementItem
	 * @param orderId
	 * @return
	 */
	private List<SettlementItem>  fillSettlementItemList(OrdOrder order) {
		
		List<SettlementItem> settlementItemList=new ArrayList<SettlementItem>();
		
		//OrdOrder order=getOrderWithOrderItemByOrderId(orderId);
		//OrdOrder order=new OrdOrder();
		Long orderId=order.getOrderId();
		List<OrdOrderItem> orderItemList=order.getOrderItemList();

		Long countSettleAmount=0L;//整个订单结算总额（分）
		Long orderTotalSettlementPrice=0L;
		for (OrdOrderItem ordOrderItem : orderItemList) {
			countSettleAmount+=ordOrderItem.getQuantity()*ordOrderItem.getTotalSettlementPrice();
			orderTotalSettlementPrice+=ordOrderItem.getQuantity()*ordOrderItem.getSettlementPrice();
		}
		
		//支付获取结算状态
		List<SetSettlementItem> setSettlementItems = getSetSettlementItem(orderItemList);
		
		
		for (OrdOrderItem orderItem : orderItemList) {
			
			
			//订单销售分拆后的支付金额   订单应付总金额*（当前订单子项单家*数量/所有订单子项数量*单价和）
			Long orderItemMetaPayedAmount=order.getOughtAmount()*(orderItem.getQuantity()*orderItem.getSettlementPrice()/orderTotalSettlementPrice);
			
			String branchName=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
			if (resultHandleSuppGoods.isFail() || resultHandleSuppGoods.getReturnContent() == null) {
				throw new RuntimeException("method:fillSettlementItemList:获取商品(ID=" + orderItem.getSuppGoodsId() + ")失败。");
			}
			SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
			PermUser permUser=permUserServiceAdapter.getPermUserByUserId(suppGoods.getManagerId());
			String productManager=permUser.getUserName();
			
			SettlementItem setSettlementItem=new SettlementItem();
			
			setSettlementItem.setOrderId(orderId);
			LOG.info("processer5>>orderId = "+order.getOrderId()+"orderStatus = "+order.getOrderStatus());
			setSettlementItem.setOrderStatus(order.getOrderStatus());
			setSettlementItem.setOrderPaymentTime(order.getPaymentTime());
			setSettlementItem.setOrderCreateTime(order.getCreateTime());
			setSettlementItem.setOrderPaymentStatus(order.getPaymentStatus());
			setSettlementItem.setOrderContactPerson(order.getContactPerson().getFullName());
			// 将游玩人推送给结算系统
			List<OrdPerson> travellerList = order.getOrdTravellerList();
			if (travellerList != null && !travellerList.isEmpty()) {
				StringBuilder travellerBulider = new StringBuilder();
				for (OrdPerson travellerPerson : travellerList) {
					travellerBulider.append(travellerPerson.getFullName())
							.append(",");
				}
				String travellerName = travellerBulider.toString().substring(0,
						travellerBulider.toString().length() - 1);
				setSettlementItem.setTravelingPerson(travellerName);
			}
			setSettlementItem.setOrderCouponAmount(0L);//订单的优惠券金额
//				setSettlementItem.setPassCode(passCode);//通关码  这边不需要填写
//				setSettlementItem.setPassSerialno(passSerialno);//通关流水号  这边不需要填写
//				setSettlementItem.setPassExtid(passExtid);//供应商回调信息  这边不需要填写
			setSettlementItem.setOrderRefund(Constants.ORDER_REFUND_FALSE);//是否订单有退款 0.没有 1.有退款
//				setSettlementItem.setRefundMemo(refundMemo);
			setSettlementItem.setOrderItemProdId(orderItem.getOrderItemId());//订单子项ID
			setSettlementItem.setProductId(orderItem.getProductId());
			setSettlementItem.setProductName(orderItem.getProductName());
			setSettlementItem.setProductType(Constant.PRODUCT_TYPE.HOTEL.getCode());//销售产品类型  orderItem.getCategoryId()+""
			setSettlementItem.setProductBranchId(suppGoods.getSuppGoodsId());
			setSettlementItem.setProductBranchName(suppGoods.getGoodsName());
			setSettlementItem.setProductPrice(orderItem.getPrice());
			setSettlementItem.setFilialeName(order.getFilialeName());
			setSettlementItem.setOrderItemMetaId(orderItem.getOrderItemId());//订单子子项ID
			setSettlementItem.setOrderItemMetaPayedAmount(orderItemMetaPayedAmount);//订单销售分拆后的支付金额  
			setSettlementItem.setMetaProductId(orderItem.getProductId());//采购产品ID
			setSettlementItem.setMetaProductName(orderItem.getProductName());//采购产品名称
			setSettlementItem.setMetaBranchId(suppGoods.getSuppGoodsId());//采购产品分类ID  BRANCH_ID
			
			setSettlementItem.setMetaBranchName(suppGoods.getGoodsName());//采购产品分类名称  
			setSettlementItem.setMetaProductManager(productManager);
			setSettlementItem.setSettlementPrice(orderItem.getSettlementPrice());
			setSettlementItem.setActualSettlementPrice(orderItem.getActualSettlementPrice());
			setSettlementItem.setSupplierId(orderItem.getSupplierId());
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("contractId", orderItem.getContractId());
			params.put("supplierId", orderItem.getSupplierId());
			ResultHandleT<List<SuppSettleRule>> resultHandleList = suppSupplierClientService.findSuppSettleRuleList(params);
			if (resultHandleList.isSuccess()) {
				List<SuppSettleRule>  suppSettleRuleList = resultHandleList.getReturnContent();
				if (suppSettleRuleList != null && !suppSettleRuleList.isEmpty()) {
					SuppSettleRule suppSettleRule=suppSettleRuleList.get(0);
					setSettlementItem.setTargetId(suppSettleRule.getSettleRuleId());//结算对象ID  SUPP_SETTLE_RULE表 SETTLE_RULE_ID
				}
			} else {
				LOG.info("method fillSettlementItemList:findSuppSettleRuleList is fail,msg=" + resultHandleList.getMsg());
			}
			
			setSettlementItem.setProductQuantity(1L);//打包数量
			setSettlementItem.setQuantity(orderItem.getQuantity());
			setSettlementItem.setVisitTime(order.getVisitTime());
//				setSettlementItem.setSubProductType(subProductType);//采购产品子类型
			
			/**
			 * 2017/03/06 结算状态改造
			 */

			//setSettlementItem.setSettlementStatus(orderItem.getSettlementStatus());

			setSettlementItem.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSettlementStatus(setSettlementItems, orderItem.getOrderItemId())));
		


//				setSettlementItem.setSettlementId(settlementId);//结算单ID 这边不需要填写
//				setSettlementItem.setJoinSettlementTime(joinSettlementTime);//加入结算单的时间	 这边不需要填写
			setSettlementItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice());//子项结束总额
//			setSettlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.NORMAL.name());//状态   订单支付成功和订单取消后 分别是正常和取消
			setSettlementItem.setSettlementType(OrderEnum.SETTLEMENT_TYPE.ORDER.getCode());//结算项类别（GROUP  OR  ORDER）
			
			
//				setSettlementItem.setProductSubProductType(productSubProductType);//销售产品子类型
//			setSettlementItem.setAdjustmentRemark(adjustmentRemark);//订单调整总额（分）
//			setSettlementItem.setRefundedAmount(refundedAmount);//订单退款金额（分）
			setSettlementItem.setOughtPay(order.getOughtAmount());//订单应付总额（分）
			setSettlementItem.setActualPay(order.getActualAmount());//订单实付金额（分）
			setSettlementItem.setCountSettleAmount(countSettleAmount);//整个订单结算总额（分）
			//setSettlementItem.setAdjustmentRemark(adjustmentRemark);//订单调整金额备注
			//setSettlementItem.setUpdateRemark(updateRemark);//订单修改结算价备注
			
			
			settlementItemList.add(setSettlementItem);
			
		}
		
		return settlementItemList;
	}
	

	/**
	 * 根据OrderId返回单个用订单那子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPersonTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}

}
