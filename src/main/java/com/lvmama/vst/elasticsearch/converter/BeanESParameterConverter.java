package com.lvmama.vst.elasticsearch.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.elasticsearch.params.BasicESParams;
import com.lvmama.vst.elasticsearch.params.ESParams;
import com.lvmama.vst.elasticsearch.params.ParamType;

public class BeanESParameterConverter implements ParameterConverter<OrderMonitorCnd> {

	@Override
	public ESParams convert(OrderMonitorCnd condition) {
		ESParams params = new BasicESParams();
		// Order Infomation Start============================================

		// TODO CANCEL_TIME
		// TODO REMARK
		// TODO BOOK_LIMIT_TYPE
		// TODO MOBILE_EQUIPMENT_NO
		// TODO TOURIST_MOBILE

		// TODO REMIND_SMS_SEND_STATUS
		// 主订单ID
		Long orderID = condition.getOrderId();
		if (null != orderID) {
			params.setLongParameter("ORDER_ID", orderID);
		}
		// 子订单编号
		Long orderItemID = condition.getOrderItemId();
		if (null != orderItemID) {
			params.setLongParameter("SUB_ORDER_IDS", orderItemID);
		}
		// 下单人工号
		String backUserID = condition.getBackUserId();
		if (StringUtils.isNotBlank(backUserID)) {
			params.setParameter(ParamType.LONG.getName(), "BACK_USER_ID", backUserID);
		}
		// 审核人工号
		String auditUserID = condition.getResponsiblePerson();
		if (StringUtils.isNotBlank(auditUserID)) {
			params.setParameter(ParamType.CHARACTER.getName(), "AUDIT_CODE", auditUserID);
		}
		// 下单时间
		String createTimeBegin = condition.getCreateTimeBegin();
		String createTimeEnd = condition.getCreateTimeEnd();
		if (StringUtils.isNotBlank(createTimeBegin)) {
			Date createDateBegin = parse(createTimeBegin, "");
			List<Long> createDates = new ArrayList<Long>();
			createDates.add(createDateBegin.getTime());
			if (StringUtils.isNotBlank(createTimeEnd)) {
				Date createDateEnd = parse(createTimeEnd, "");
				createDates.add(createDateEnd.getTime());
			}
			params.setParameter(ParamType.DATE.getName(), "CREATE_TIME", createDates);
		}
		// 订单状态
		String orderStatus = condition.getOrderStatus();
		if (StringUtils.isNotBlank(orderStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "ORDER_STATUS", orderStatus);
		}
		// 信息审核
		String infoStatus = condition.getInfoStatus();
		if (StringUtils.isNotBlank(infoStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "INFO_STATUS", infoStatus);
		}
		// 资源审核
		String resourceStatus = condition.getResourceStatus();
		if (StringUtils.isNotBlank(resourceStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "RESOURCE_STATUS", resourceStatus);
		}
		// 支付状态
		String paymentStatus = condition.getPaymentStatus();
		if (StringUtils.isNotBlank(paymentStatus)) {
			params.setParameter(ParamType.STRING.getName(), "PAYMENT_STATUS", paymentStatus);
		}
		
		String performStatus = condition.getPerformStatus();
		if (StringUtils.isNotBlank(performStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "PERFORM_STATUS", performStatus);
		}
		
		// 支付时间
		Date paymentTimeBegin = condition.getPaymentTimeBegin();
		Date paymentTimeEnd = condition.getPaymentTimeEnd();
		if (null != paymentTimeBegin) {
			List<Long> paymentDates = new ArrayList<Long>();
			paymentDates.add(paymentTimeBegin.getTime());
			if (null != paymentTimeEnd) {
				paymentDates.add(paymentTimeEnd.getTime());
			}
			params.setParameter(ParamType.DATE.getName(), "PAYMENT_TIME", paymentDates);

		}
		// 凭证确认状态
		String certConfirmStatus = condition.getCertConfirmStatus();
		if (StringUtils.isNotBlank(certConfirmStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "CERT_CONFIRM_STATUS", certConfirmStatus);
		}
		// 合同状态
		// TODO 合同状态 未找到字段
		// 出团通知书状态
		// TODO 出团通知书状态 未找到字段
		String noticeRegimentStatus = condition.getNoticeRegimentStatus();
		if (StringUtils.isNotBlank(noticeRegimentStatus)) {
			params.setParameter(ParamType.CHARACTER.getName(), "INVOICE_STATUS", noticeRegimentStatus);
		}
		// 下单渠道
		// TODO 多渠道查询对应索引如何解决
		List<Long> distributorIDs = condition.getDistributorIds();
		if (null != distributorIDs && !distributorIDs.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			int iMax = distributorIDs.size() - 1;
			for (int i = 0; i < distributorIDs.size(); i++) {
				sb.append(distributorIDs.get(i));
				if (i != iMax)
					sb.append(", ");
			}
			params.setParameter(ParamType.LIST.getName(), "DISTRIBUTOR_ID", distributorIDs);
		}

		// TODO VIEW_ORDER_STATUS
		// TODO MANAGER_ID_PERM

		// TODO CANCEL_CODE
		// TODO CURRENCY_CODE
		// TODO ACTUAL_AMOUNT

		// Order Infomation End============================================

		// Product Infomation Start============================================
		// 产品编号
		Long productID = condition.getProductId();
		if (null != productID) {
			List<Long> productIDs = new ArrayList<Long>();
			productIDs.add(productID);
			params.setParameter(ParamType.LIST.getName(), "PRODUCT_IDS", productIDs);
		}
		String productName = condition.getProductName();
		if (StringUtils.isNotBlank(productName)) {
			params.setParameter(ParamType.CHARACTER.getName(), "PRODUCT_NAMES", productName);
		}
		String goodsName = condition.getSuppGoodsName();
		if (StringUtils.isNotBlank(goodsName)) {
			params.setParameter(ParamType.CHARACTER.getName(), "GOOGS_NAMES", goodsName);
		}

		Long goodsID = condition.getSuppGoodsId();
		if (null != goodsID) {
			List<Long> goodsIDs = new ArrayList<Long>();
			goodsIDs.add(goodsID);
			params.setParameter(ParamType.LIST.getName(), "GOODS_IDS", goodsIDs);
		}

		String whichManagerID = condition.getWhichManagerId();// main:
																// managerId;
																// item:itemManagerId
		Long managerID = condition.getManagerId();
		String managerName = condition.getManagerName();
		if (StringUtils.isNotBlank(managerName)) {
			if ("managerId".equalsIgnoreCase(whichManagerID)) {
				if (null != managerID) {
					params.setParameter(ParamType.STRING.getName(), "MANAGER_ID", managerID.toString());
				}
			} else {
				params.setParameter(ParamType.LIST.getName(), "PRODUCT_MANAGER_IDS", managerID.toString());
			}
		}

		String buCode = condition.getBelongBU();
		if (StringUtils.isNotBlank(buCode)) {
			//如果是国内，目的地合成的，传list,否则传string
			String[] arr = buCode.toLowerCase().split("\\|");
			if(arr.length > 1) {
				params.setParameter(ParamType.LIST.getName(), "BU_CODE", Arrays.asList(arr));
			} else {
				params.setParameter(ParamType.STRING.getName(), "BU_CODE", buCode);
			}
		}

		// List<String> categoryIDs =
		// (List<String>)condition.getCategoryIdList();
		// if (CollectionUtils.isNotEmpty(categoryIDs)) {
		// params.setParameter("PRODUCT_CATEGORY_IDS", categoryIDs);
		// }

		List<String> categoryIDs = condition.getCategoryIdList();
		if (null != categoryIDs && !categoryIDs.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			int iMax = categoryIDs.size() - 1;
			for (int i = 0; i < categoryIDs.size(); i++) {
				sb.append(categoryIDs.get(i));
				if (i != iMax)
					sb.append(", ");
			}
			params.setParameter(ParamType.LIST.getName(), "PRODUCT_CATEGORY_IDS", categoryIDs);
		}

		// 供应商
		Long supplierId = condition.getSupplierId();
		if (null != supplierId && supplierId.intValue() > 0) {
			List<Long> supplierIds = new ArrayList<Long>();
			supplierIds.add(supplierId);
			params.setParameter(ParamType.LIST.getName(), "SUPPLIER_IDS", supplierIds);
		} else {
			String supplierName = condition.getSupplierName();
			if (StringUtils.isNotBlank(supplierName)) {
				params.setParameter(ParamType.CHARACTER.getName(), "PRODUCT_SUPPLIERNAMES", supplierName);
			}
		}

		// 商品支付方式
		String paymentMethod = condition.getPayTarget();
		if (StringUtils.isNotBlank(paymentMethod)) {
			params.setParameter(ParamType.STRING.getName(), "PAYMENT_METHODS", paymentMethod);
		}

		List<String> filialeNames = condition.getFilialeNames();
		if (CollectionUtils.isNotEmpty(filialeNames)) {
			params.setParameter(ParamType.STRING.getName(), "FILIALE_NAME", filialeNames.get(0));
		}
		// Product Infomation End============================================

		// Tourist Information Start============================================
		// 驴妈妈账号
		String bookerName = condition.getBookerName();
		if (StringUtils.isNotBlank(bookerName)) {
			params.setParameter(ParamType.STRING.getName(), "BOOKER_NAME", bookerName);
		}
		// 已绑定手机号
		String bookerMobile = condition.getBookerMobile();
		if (StringUtils.isNotBlank(bookerMobile)) {
			List<String> travellerMobiles = new ArrayList<String>();
			travellerMobiles.add(bookerMobile);
			params.setParameter(ParamType.LIST.getName(), "TOURIST_MOBILES", travellerMobiles);
		}

		// 出游人姓名
		String travellerName = condition.getTravellerName();
		if (StringUtils.isNotBlank(travellerName)) {
			List<String> travellerNames = new ArrayList<String>();
			travellerNames.add(travellerName);
			params.setParameter(ParamType.LIST.getName(), "TOURIST_NAMES", travellerNames);
		}

		// TODO 日期查询
		Date visitTimeEffect = condition.getVisitTimeBegin();
		Date visitTimeExpiry = condition.getVisitTimeEnd();
		if (null != visitTimeEffect) {
			List<Long> visitDates = new ArrayList<Long>();
			visitDates.add(visitTimeEffect.getTime());
			if (null != visitTimeExpiry) {
				visitDates.add(visitTimeExpiry.getTime());
			}
			params.setParameter(ParamType.DATE.name(), "VISIT_TIME", visitDates);
		}

		// 联系人姓名
		String contactName = condition.getContactName();
		if (StringUtils.isNotBlank(contactName)) {
			params.setParameter(ParamType.CHARACTER.getName(), "CONTACT_NAME", contactName);
		}
		// 联系人手机
		String contactMobile = condition.getContactMobile();
		if (StringUtils.isNotBlank(contactMobile)) {
			params.setParameter(ParamType.CHARACTER.getName(), "CONTACT_MOBILE", contactMobile);
		}
		// 联系人邮箱
		String contractEmail = condition.getContactEmail();
		if (StringUtils.isNotBlank(contractEmail)) {
			params.setParameter(ParamType.CHARACTER.getName(), "CONTANCT_EMAIL", contractEmail);
		}
		String contractFixPhone = condition.getContactPhone();
		if (StringUtils.isNotBlank(contractFixPhone)) {
			params.setParameter(ParamType.CHARACTER.getName(), "CONTACT_FIXED_PHONES", contractFixPhone);
		}
		
		//分销商ID
		String distributionChannel = condition.getDistributionChannel();
		if (StringUtils.isNotBlank(distributionChannel)) {
			params.setParameter(ParamType.STRING.getName(), "DISTRIBUTION_CHANNEL", distributionChannel);
		}
		
		//渠道代码
		String distributionCode = condition.getDistributionCode();
		if (StringUtils.isNotBlank(distributionCode)) {
			params.setParameter(ParamType.STRING.getName(), "DISTRIBUTOR_CODE", distributionCode);
		}
		
		//产品品类
		Long categoryId = condition.getCategoryId();
		if (categoryId != null) {
			params.setParameter(ParamType.LONG.getName(), "CATEGORY_ID", categoryId);
		}
		
		//是否后置
		String travellerDelayFlag = condition.getTravellerDelayFlag();
		if (StringUtils.isNotBlank(travellerDelayFlag)) {
			params.setParameter(ParamType.STRING.getName(), "TRAVELLER_DELAY_FLAG", travellerDelayFlag);
		}
		
		//是否锁定
		String travellerLockFlag = condition.getTravellerLockFlag();
		if (StringUtils.isNotBlank(travellerLockFlag)) {
			params.setParameter(ParamType.STRING.getName(), "TRAVELLER_LOCK_FLAG", travellerLockFlag);
		}

		// Tourist Infomation End
		// ===============================================================================

		return params;
	}

	private Date parse(String value, String pattern) {
		if (StringUtils.isBlank(value)) {
			throw new IllegalArgumentException("Date must not be null");
		}
		if (StringUtils.isBlank(pattern)) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}
		Date formatedDate = new Date();
		SimpleDateFormat parser = new SimpleDateFormat(pattern);
		try {
			formatedDate = parser.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return formatedDate;
	}

	public static void main(String[] args) {
		BeanESParameterConverter convert = new BeanESParameterConverter();
		OrderMonitorCnd cnd = new OrderMonitorCnd();
		List<String> ids = new ArrayList<String>();
		ids.add("1");
		ids.add("2");
		ids.add("3");
		cnd.setCategoryIdList(ids);
		convert.convert(cnd);
	}
}
