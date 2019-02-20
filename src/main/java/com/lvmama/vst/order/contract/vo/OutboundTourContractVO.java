package com.lvmama.vst.order.contract.vo;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.order.CurrencyUtil;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;

/**
 * 
 * @author sunjian
 *
 */
public class OutboundTourContractVO {
	//模板目录
	private String templateDirectory;
	
	//合同版本号
	private String contractVersion;
	
	//订单编号
	private String orderId;
	
	//甲方
	private String travellers;
	
	//产品名称
	private String productName;
	
	//是否委托组团
	private boolean delegateGroup;
	
	//委托社名称
	private String delegateGroupName;
	
	//出发日期
	private String vistDate;
	
	//出发地
	private String departurePlace;
	
	//目的地
	private String destination;
	
	//结束日期
	private String overDate;
	
	//返回地
	private String returnPlace;
	
	//地接社名称
	private String localTravelAgencyName;
	
	//地接社地址
	private String localTravelAgencyAddress;
	
	//是否有保险
	private boolean hasInsurance;
	
	//是否同意
	private String agreeInsurance;
	
	//保险公司名称+产品名称
	private String insuranceCompanyAndProductName;
	
	//保险总金额
	private String insuranceAmount;
	
	//旅游费用总金额
	private String traveAmount;
	
	//旅游费用总金额-中文大写
	private String chineseNumeralTraveAmount;
	
	//是否有最低人数
	private boolean hasMinPersonCount;
	
	//最小成团人数
	private String minPersonCountOfGroup;

	//补充条款
	private String supplementaryTerms;
	
	//甲方署名人名字
	private String signaturePersonName;
	
	//甲方代表人名字
	private String firstDelegatePersonName;
	
	//联系人电话
	private String contactTelePhoneNo;
	
	//甲方签约日期
	private String firstSignatrueDate;
	
	//乙方签约日期
	private String secondSignatrueDate;
	
	//合同专用章
	private String stampImage;
	
	//旅游费用交纳方式
	private String payWay;
	
	private List<OrdOrderItem> orderItemList;
	
	private List<OrderMonitorRst> orderMonitorRstList;;

	public String getTemplateDirectory() {
		return templateDirectory;
	}

	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}

	public String getContractVersion() {
		return contractVersion;
	}

	public void setContractVersion(String contractVersion) {
		this.contractVersion = contractVersion;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTravellers() {
		return travellers;
	}

	public void setTravellers(String travellers) {
		this.travellers = travellers;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public boolean getDelegateGroup() {
		return delegateGroup;
	}

	public void setDelegateGroup(boolean delegateGroup) {
		this.delegateGroup = delegateGroup;
	}

	public String getDelegateGroupName() {
		return delegateGroupName;
	}

	public void setDelegateGroupName(String delegateGroupName) {
		this.delegateGroupName = delegateGroupName;
	}

	public String getVistDate() {
		return vistDate;
	}

	public void setVistDate(String vistDate) {
		this.vistDate = vistDate;
	}

	public String getDeparturePlace() {
		return departurePlace;
	}

	public void setDeparturePlace(String departurePlace) {
		this.departurePlace = departurePlace;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOverDate() {
		return overDate;
	}

	public void setOverDate(String overDate) {
		this.overDate = overDate;
	}

	public String getReturnPlace() {
		return returnPlace;
	}

	public void setReturnPlace(String returnPlace) {
		this.returnPlace = returnPlace;
	}

	public String getLocalTravelAgencyName() {
		return localTravelAgencyName;
	}

	public void setLocalTravelAgencyName(String localTravelAgencyName) {
		this.localTravelAgencyName = localTravelAgencyName;
	}

	public String getLocalTravelAgencyAddress() {
		return localTravelAgencyAddress;
	}

	public void setLocalTravelAgencyAddress(String localTravelAgencyAddress) {
		this.localTravelAgencyAddress = localTravelAgencyAddress;
	}

	public boolean isHasInsurance() {
		return hasInsurance;
	}

	public void setHasInsurance(boolean hasInsurance) {
		this.hasInsurance = hasInsurance;
	}

	public String getAgreeInsurance() {
		return agreeInsurance;
	}

	public void setAgreeInsurance(String agreeInsurance) {
		this.agreeInsurance = agreeInsurance;
	}

	public String getInsuranceCompanyAndProductName() {
		return insuranceCompanyAndProductName;
	}

	public void setInsuranceCompanyAndProductName(
			String insuranceCompanyAndProductName) {
		this.insuranceCompanyAndProductName = insuranceCompanyAndProductName;
	}

	public String getInsuranceAmount() {
		return insuranceAmount;
	}

	public void setInsuranceAmount(String insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}

	public String getTraveAmount() {
		return traveAmount;
	}

	public void setTraveAmount(String traveAmount) {
		this.traveAmount = traveAmount;
		chineseNumeralTraveAmount =  CurrencyUtil.toChinese(traveAmount);
		if (chineseNumeralTraveAmount != null
				&& chineseNumeralTraveAmount.lastIndexOf("元") == chineseNumeralTraveAmount.length() - 1) {
			chineseNumeralTraveAmount = chineseNumeralTraveAmount.substring(0, chineseNumeralTraveAmount.length() - 1);
		}
	}

	public String getChineseNumeralTraveAmount() {
		return chineseNumeralTraveAmount;
	}

	public void setChineseNumeralTraveAmount(String chineseNumeralTraveAmount) {
		this.chineseNumeralTraveAmount = chineseNumeralTraveAmount;
	}

	public String getMinPersonCountOfGroup() {
		return minPersonCountOfGroup;
	}

	public void setMinPersonCountOfGroup(String minPersonCountOfGroup) {
		this.minPersonCountOfGroup = minPersonCountOfGroup;
	}

	public String getSupplementaryTerms() {
		return supplementaryTerms;
	}

	public void setSupplementaryTerms(String supplementaryTerms) {
		this.supplementaryTerms = supplementaryTerms;
	}

	public String getSignaturePersonName() {
		return signaturePersonName;
	}

	public void setSignaturePersonName(String signaturePersonName) {
		this.signaturePersonName = signaturePersonName;
	}

	public String getFirstDelegatePersonName() {
		return firstDelegatePersonName;
	}

	public void setFirstDelegatePersonName(String firstDelegatePersonName) {
		this.firstDelegatePersonName = firstDelegatePersonName;
	}

	public String getContactTelePhoneNo() {
		return contactTelePhoneNo;
	}

	public void setContactTelePhoneNo(String contactTelePhoneNo) {
		this.contactTelePhoneNo = contactTelePhoneNo;
	}

	public String getFirstSignatrueDate() {
		return firstSignatrueDate;
	}

	public void setFirstSignatrueDate(String firstSignatrueDate) {
		this.firstSignatrueDate = firstSignatrueDate;
	}

	public String getSecondSignatrueDate() {
		return secondSignatrueDate;
	}

	public void setSecondSignatrueDate(String secondSignatrueDate) {
		this.secondSignatrueDate = secondSignatrueDate;
	}

	public boolean getHasMinPersonCount() {
		return hasMinPersonCount;
	}

	public void setHasMinPersonCount(boolean hasMinPersonCount) {
		this.hasMinPersonCount = hasMinPersonCount;
	}

	public String getStampImage() {
		return stampImage;
	}

	public void setStampImage(String stampImage) {
		this.stampImage = stampImage;
	}

	public String getPayWay() {
		return payWay;
	}

	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}

	public List<OrdOrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<OrdOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

	public List<OrderMonitorRst> getOrderMonitorRstList() {
		return orderMonitorRstList;
	}

	public void setOrderMonitorRstList(List<OrderMonitorRst> orderMonitorRstList) {
		this.orderMonitorRstList = orderMonitorRstList;
	}

}
