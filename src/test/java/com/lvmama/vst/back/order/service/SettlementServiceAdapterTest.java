/**
 * 
 */
package com.lvmama.vst.back.order.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.pet.adapter.SettlementServiceAdapter;

/**
 * @author 张伟
 *
 */
public class SettlementServiceAdapterTest extends OrderTestBase {

	
	private SettlementServiceAdapter settlementServiceAdapter = null;

	
	@Before
	public void prepare() {
		super.prepare();
		if(null != applicationContext){
			settlementServiceAdapter = (SettlementServiceAdapter) applicationContext.getBean(SettlementServiceAdapter.class);
		}
	}
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.lvmama.vst.pet.adapter.SettlementServiceAdapter#insertSettlementItem(com.lvmama.comm.pet.po.fin.SettlementItem)}.
	 */
	@Test
	public void testInsertSettlementItem() {
		
		SettlementItemTest setSettlementItemTest=new SettlementItemTest();
		
		SettlementItem setSettlementItem=new SettlementItem();
		
		
		BeanUtils.copyProperties(setSettlementItemTest, setSettlementItem);
		
		
		
		
		this.settlementServiceAdapter.saveSettlementItem(setSettlementItem);
	}

	/**
	 * Test method for {@link com.lvmama.vst.pet.adapter.SettlementServiceAdapter#batchInsertSettlementItem(java.util.List)}.
	 */
	@Test
	public void testBatchInsertSettlementItem() {
		
		List<SettlementItem> setSettlementItems=new ArrayList<SettlementItem>(5);
		for (SettlementItem settlementItem : setSettlementItems) {
			
			SettlementItemTest setSettlementItemTest=new SettlementItemTest();
			SettlementItem setSettlementItem=new SettlementItem();
			
			BeanUtils.copyProperties(setSettlementItemTest, settlementItem);
			
			
		}
		
		
		
		
		
		settlementServiceAdapter.batchInsertSettlementItem(setSettlementItems);
	}
	
	
	
	class SettlementItemTest{

		protected static final long serialVersionUID = 1L;

		protected Long settlementItemId;
		protected Long orderId=238L;
		protected String orderStatus="NORMAL";
		protected Date orderPaymentTime=new Date();
		protected Date orderCreateTime=new Date();
		protected String orderPaymentStatus="UNPAY";
		protected String orderContactPerson="zw";
		protected Long orderCouponAmount=2L;
		protected String passCode="code";
		protected String passSerialno="code";
		protected String passExtid="code";
		protected Boolean orderRefund=true;
		protected String refundMemo="code";
		protected Long orderItemProdId=1L;
		protected Long productId=1L;
		protected String productName="code";
		protected String productType="code";
		protected Long productBranchId=1L;
		protected String productBranchName="code";
		protected Long productPrice=1L;
		protected String filialeName="code";
		protected Long orderItemMetaId=1L;
		protected Long orderItemMetaPayedAmount=1L;
		protected Long metaProductId=1L;
		protected String metaProductName="code";
		protected Long metaBranchId=1L;
		protected String metaBranchName="code";
		protected String metaProductManager="code";
		protected Long settlementPrice=1L;
		protected Long actualSettlementPrice=1L;
		protected Long supplierId=1L;
		protected String supplierName="code";
		protected Long targetId=1L;
		protected String targetName="code";
		protected Long productQuantity=1L;
		protected Long quantity=1L;
		protected Date visitTime=new Date();
		protected String productSubProductType="code";
		protected String subProductType="code";
		
		
		protected String settlementStatus="code";
		protected Long settlementId=1L;
		protected Date joinSettlementTime=new Date();
		protected Date settlementTime=new Date();
		protected Long totalSettlementPrice=1L;
		protected String status="code";
		protected String settlementType="code";
		

		/* 我方结算主体 */
		protected String companyId;
		/* 结算周期 */
		protected String settlementPeriod;
		/*建议打款时间*/
		protected Date suggestionPayTime;
		
		/**
		 * 订单调整总额（分）
		 */
		protected Long adjustmentAmount;
		/**
		 * 订单退款金额（分）
		 */
		protected Long refundedAmount;
		/**
		 * 订单应付金额（分）
		 */
		protected Long oughtPay;//OUGHT_PAY

		/**
		 * 修改结算价备注
		 */
		protected String updateRemark;
		
		/**
		 * 订单的退款备注
		 */
		protected String adjustmentRemark;
		/**
		 * 订单结算总价 （元） 
		 */
		protected Long countSettleAmount;
		
		/**
		 * 所属系统名称
		 */
		protected String businessName;
		public String getBusinessNameCh(){
			if(StringUtil.isEmptyString(businessName)){
				return Constant.SET_SETTLEMENT_BUSINESS_NAME.SUPER_ORDER_BUSINESS.getCnName();
			}
			return Constant.SET_SETTLEMENT_BUSINESS_NAME.getCnName(businessName);
		}
		public Long getSettlementItemId() {
			return this.settlementItemId;
		}

		public void setSettlementItemId(Long settlementItemId) {
			this.settlementItemId = settlementItemId;
		}

		public Long getOrderId() {
			return this.orderId;
		}

		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}

		public Date getOrderPaymentTime() {
			return this.orderPaymentTime;
		}

		public String getOrderPaymentTimeStr() {
			return DateUtil.formatDate(this.orderPaymentTime, "yyyy-MM-dd HH:mm:ss");
		}

		public void setOrderPaymentTime(Date orderPaymentTime) {
			this.orderPaymentTime = orderPaymentTime;
		}

		public Date getOrderCreateTime() {
			return this.orderCreateTime;
		}

		public String getOrderCreateTimeStr() {
			return DateUtil.formatDate(this.orderCreateTime, "yyyy-MM-dd HH:mm:ss");
		}

		public void setOrderCreateTime(Date orderCreateTime) {
			this.orderCreateTime = orderCreateTime;
		}

		public String getOrderPaymentStatus() {
			return this.orderPaymentStatus;
		}

		public void setOrderPaymentStatus(String orderPaymentStatus) {
			this.orderPaymentStatus = orderPaymentStatus;
		}

		public String getOrderContactPerson() {
			return this.orderContactPerson;
		}

		public void setOrderContactPerson(String orderContactPerson) {
			this.orderContactPerson = orderContactPerson;
		}

		public String getPassCode() {
			return this.passCode;
		}

		public void setPassCode(String passCode) {
			this.passCode = passCode;
		}

		public String getPassSerialno() {
			return this.passSerialno;
		}

		public void setPassSerialno(String passSerialno) {
			this.passSerialno = passSerialno;
		}

		public String getPassExtid() {
			return this.passExtid;
		}

		public void setPassExtid(String passExtid) {
			this.passExtid = passExtid;
		}

		public Boolean getOrderRefund() {
			return orderRefund;
		}

		public void setOrderRefund(Boolean orderRefund) {
			this.orderRefund = orderRefund;
		}

		public String getRefundMemo() {
			return this.refundMemo;
		}

		public void setRefundMemo(String refundMemo) {
			this.refundMemo = refundMemo;
		}

		public Long getOrderItemProdId() {
			return this.orderItemProdId;
		}

		public void setOrderItemProdId(Long orderItemProdId) {
			this.orderItemProdId = orderItemProdId;
		}

		public Long getProductId() {
			return this.productId;
		}

		public void setProductId(Long productId) {
			this.productId = productId;
		}

		public String getProductName() {
			return this.productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public String getProductType() {
			return this.productType;
		}

		public void setProductType(String productType) {
			this.productType = productType;
		}

		public String getFilialeName() {
			return this.filialeName;
		}
		public String getFilialeNameName() {
			return Constant.FILIALE_NAME.getCnName(this.filialeName);
		}

		public void setFilialeName(String filialeName) {
			this.filialeName = filialeName;
		}
		

		public Long getOrderItemMetaId() {
			return this.orderItemMetaId;
		}

		public void setOrderItemMetaId(Long orderItemMetaId) {
			this.orderItemMetaId = orderItemMetaId;
		}

		public Long getMetaProductId() {
			return this.metaProductId;
		}

		public void setMetaProductId(Long metaProductId) {
			this.metaProductId = metaProductId;
		}

		public String getMetaProductName() {
			return this.metaProductName;
		}

		public void setMetaProductName(String metaProductName) {
			this.metaProductName = metaProductName;
		}

		public Long getMetaBranchId() {
			return this.metaBranchId;
		}

		public void setMetaBranchId(Long metaBranchId) {
			this.metaBranchId = metaBranchId;
		}

		public String getMetaBranchName() {
			return this.metaBranchName;
		}

		public void setMetaBranchName(String metaBranchName) {
			this.metaBranchName = metaBranchName;
		}

		public Long getActualSettlementPrice() {
			return this.actualSettlementPrice;
		}

		public Float getActualSettlementPriceYuan() {
			return PriceUtil.convertToYuan(this.actualSettlementPrice);
		}

		public void setActualSettlementPrice(Long actualSettlementPrice) {
			this.actualSettlementPrice = actualSettlementPrice;
		}

		public Long getSupplierId() {
			return this.supplierId;
		}

		public void setSupplierId(Long supplierId) {
			this.supplierId = supplierId;
		}

		public Long getTargetId() {
			return this.targetId;
		}

		public void setTargetId(Long targetId) {
			this.targetId = targetId;
		}

		public Long getProductQuantity() {
			return this.productQuantity;
		}

		public void setProductQuantity(Long productQuantity) {
			this.productQuantity = productQuantity;
		}

		public Long getQuantity() {
			return this.quantity;
		}

		public void setQuantity(Long quantity) {
			this.quantity = quantity;
		}

		public Long getTotalQuantity() {
			return this.quantity * this.productQuantity;
		}

		public Date getVisitTime() {
			return this.visitTime;
		}

		public String getVisitTimeStr() {
			return DateUtil.formatDate(this.visitTime, "yyyy-MM-dd");
		}

		public void setVisitTime(Date visitTime) {
			this.visitTime = visitTime;
		}

		public String getSubProductType() {
			return this.subProductType;
		}

		public void setSubProductType(String subProductType) {
			this.subProductType = subProductType;
		}

		public Long getSettlementId() {
			return this.settlementId;
		}

		public void setSettlementId(Long settlementId) {
			this.settlementId = settlementId;
		}

		public Long getTotalSettlementPrice() {
			return this.totalSettlementPrice;
		}

		public Float getTotalSettlementPriceYuan() {
			return PriceUtil.convertToYuan(this.totalSettlementPrice);
		}

		public void setTotalSettlementPrice(Long totalSettlementPrice) {
			this.totalSettlementPrice = totalSettlementPrice;
		}

		public String getStatus() {
			return this.status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getSettlementType() {
			return this.settlementType;
		}

		public void setSettlementType(String settlementType) {
			this.settlementType = settlementType;
		}

		public String getOrderStatus() {
			return orderStatus;
		}

		public String getOrderStatusName() {
			return Constant.ORD_ORDER_STATUS.getCnName(this.orderStatus);
		}
		public void setOrderStatus(String orderStatus) {
			this.orderStatus = orderStatus;
		}

		public Long getOrderItemMetaPayedAmount() {
			return orderItemMetaPayedAmount;
		}
		public float getOrderItemMetaPayedAmountYuan() {
			return PriceUtil.convertToYuan(orderItemMetaPayedAmount);
		}
		public void setOrderItemMetaPayedAmount(Long orderItemMetaPayedAmount) {
			this.orderItemMetaPayedAmount = orderItemMetaPayedAmount;
		}

		public String getTargetName() {
			return targetName;
		}

		public void setTargetName(String targetName) {
			this.targetName = targetName;
		}

		public String getSupplierName() {
			return supplierName;
		}

		public void setSupplierName(String supplierName) {
			this.supplierName = supplierName;
		}

		public String getCompanyName() {
			if (StringUtil.isEmptyString(this.companyId)) {
				return "";
			}
			return Constant.SETTLEMENT_COMPANY.getCnName(this.companyId);
		}

		public String getCompanyId() {
			return companyId;
		}

		public void setCompanyId(String companyId) {
			this.companyId = companyId;
		}

		public String getStatusName() {
			return Constant.SET_SETTLEMENT_ITEM_STATUS.getCnName(this.status);
		}

		public String getSettlementStatusName() {
			return Constant.SETTLEMENT_STATUS.getCnName(this.settlementStatus);
		}

		public String getSettlementPeriod() {
			return settlementPeriod;
		}

		public String getSettlementPeriodName() {
			return Constant.SETTLEMENT_PERIOD.getCnName(this.settlementPeriod);
		}

		public void setSettlementPeriod(String settlementPeriod) {
			this.settlementPeriod = settlementPeriod;
		}

		public Date getSettlementTime() {
			return settlementTime;
		}

		public String getSettlementTimeStr() {
			if (this.settlementTime != null) {
				return DateUtil.formatDate(this.settlementTime, "yyyy-MM-dd HH:mm:ss");
			} else
				return "";
		}

		public void setSettlementTime(Date settlementTime) {
			this.settlementTime = settlementTime;
		}

		public String getSettlementStatus() {
			return settlementStatus;
		}

		public Date getJoinSettlementTime() {
			return joinSettlementTime;
		}

		public void setJoinSettlementTime(Date joinSettlementTime) {
			this.joinSettlementTime = joinSettlementTime;
		}

		public void setSettlementStatus(String settlementStatus) {
			this.settlementStatus = settlementStatus;
		}


		public String getMetaProductManager() {
			return metaProductManager;
		}

		public void setMetaProductManager(String metaProductManager) {
			this.metaProductManager = metaProductManager;
		}

		public Long getProductPrice() {
			return productPrice;
		}
		public float getProductPriceYuan() {
			return PriceUtil.convertToYuan(productPrice);
		}
		public void setProductPrice(Long productPrice) {
			this.productPrice = productPrice;
		}

		public Long getSettlementPrice() {
			return settlementPrice;
		}

		public Float getSettlementPriceYuan() {
			return PriceUtil.convertToYuan(this.settlementPrice);
		}
		public void setSettlementPrice(Long settlementPrice) {
			this.settlementPrice = settlementPrice;
		}

		public Date getSuggestionPayTime() {
			return suggestionPayTime;
		}

		public String getSuggestionPayTimeStr(){
			if (this.suggestionPayTime != null) {
				return DateUtil.formatDate(this.suggestionPayTime, "yyyy-MM-dd");
			} else
				return "";
		}
		public void setSuggestionPayTime(Date suggestionPayTime) {
			this.suggestionPayTime = suggestionPayTime;
		}

		public Long getProductBranchId() {
			return productBranchId;
		}

		public void setProductBranchId(Long productBranchId) {
			this.productBranchId = productBranchId;
		}

		public String getProductBranchName() {
			return productBranchName;
		}

		public void setProductBranchName(String productBranchName) {
			this.productBranchName = productBranchName;
		}

		public Long getOrderCouponAmount() {
			return orderCouponAmount;
		}

		public float getOrderCouponAmountYuan() {
			return Math.abs(PriceUtil.convertToYuan(this.orderCouponAmount));
		}
		public void setOrderCouponAmount(Long orderCouponAmount) {
			this.orderCouponAmount = orderCouponAmount;
		}

		public String getProductSubProductType() {
			return productSubProductType;
		}
		
		public String getProductSubProductTypeName() {
			if(this.productSubProductType!=null){
				return Constant.SUB_PRODUCT_TYPE.getCnName(this.productSubProductType);
			}else{
				return "";
			}
		}
		public void setProductSubProductType(String productSubProductType) {
			this.productSubProductType = productSubProductType;
		}

		public Long getAdjustmentAmount() {
			return adjustmentAmount;
		}

		public void setAdjustmentAmount(Long adjustmentAmount) {
			this.adjustmentAmount = adjustmentAmount;
		}

		public Long getRefundedAmount() {
			return refundedAmount;
		}

		public void setRefundedAmount(Long refundedAmount) {
			this.refundedAmount = refundedAmount;
		}


		public Long getOughtPay() {
			return oughtPay;
		}

		public void setOughtPay(Long oughtPay) {
			this.oughtPay = oughtPay;
		}

		public String getUpdateRemark() {
			return updateRemark;
		}

		public void setUpdateRemark(String updateRemark) {
			this.updateRemark = updateRemark;
		}

		public String getAdjustmentRemark() {
			return adjustmentRemark;
		}

		public Long getCountSettleAmount() {
			return countSettleAmount;
		}

		public void setCountSettleAmount(Long countSettleAmount) {
			this.countSettleAmount = countSettleAmount;
		}
		public void setAdjustmentRemark(String adjustmentRemark) {
			this.adjustmentRemark = adjustmentRemark;
		}
		public Float getAdjustmentAmountYuan(){
			return PriceUtil.convertToYuan(adjustmentAmount);
		}
		public Float getRefundedAmountYuan(){
			return PriceUtil.convertToYuan(refundedAmount);
		}
		public Float getOughtPayYuan(){
			return PriceUtil.convertToYuan(oughtPay);
		}
		public Float getCountSettleAmountYuan(){
			return PriceUtil.convertToYuan(countSettleAmount);
		}

		public String getBusinessName() {
			return businessName;
		}

		public void setBusinessName(String businessName) {
			this.businessName = businessName;
		}
	}

}
