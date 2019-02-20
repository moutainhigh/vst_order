package com.lvmama.vst.neworder.order.vo;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseBuyInfo<T> implements Serializable{

	private static final long serialVersionUID = -2393242447105072790L;

	public T getT() {
		return t;
	}

	public void setT(T t) {
		this.t = t;
	}

	private  T t;

	/**
	 * 分销商渠道
	 */
	private Long distributionId;
	/**
	 * 分销子渠道
	 */
	private Long subDistributorId;		
	
	/**
	 * 分销商ID
	 */
	private Long distributionChannel;


	/**
	 * 分销商代码
	 */
	private String distributorCode;
	/**
	 * 分销商名称
	 */
	private String distributorName;
	
	/**
	 * 是否需要发票
	 */
	private String needInvoice;
	
	/**
	 * 用户备注
	 */
	private String remark;

	/**
	 * 下单IP
	 */
	private String ip;
	
	/**
	 * 传真备注
	 */
	private String faxMemo;

	/**
	 * 游玩人员
	 */
	private List<Person> travellers = new ArrayList<Person>();

	/**
	 * 下单人信息
	 */
	private Person booker;

	/**
	 * 联系人
	 */
	private Person contact;

	/**
	 * 紧急联系人
	 */
	private Person emergencyPerson;

	
	/**
	 * 下单人ID number;
	 */
	private Long userNo;
	
	/**
	 * 32位id
	 */
	private String userId;
	
	
	/**
	 * 是否是测试单
	 */
	private char isTestOrder;
	
	/**
	 * 促销列表
	 */
	private Map<String, List<Long>> promotionMap = new HashMap<String, List<Long>>();

	/**
	 * 优惠和活动
	 */
	private List<Coupon> couponList;


	public String getMobileEquipmentNo() {
		return mobileEquipmentNo;
	}

	public void setMobileEquipmentNo(String mobileEquipmentNo) {
		this.mobileEquipmentNo = mobileEquipmentNo;
	}

	// 手机设备号
	private String mobileEquipmentNo;

	private InvoiceInfo invoiceInfo;

	/**
	 * 支付渠道
	 */
	private String paymentChannel;
	
	
	//是否使用优惠
	private String youhui;
	//奖金抵扣
	private Long bonus;
	//奖金抵扣元
	private Float bonusYuan;
	//触发计算目标
	private String target;
	

	public Long getDistributionId() {
		return distributionId;
	}


	public void setDistributionId(Long distributionId) {
		this.distributionId = distributionId;
	}


	public Long getSubDistributorId() {
		return subDistributorId;
	}


	public void setSubDistributorId(Long subDistributorId) {
		this.subDistributorId = subDistributorId;
	}


	public Long getDistributionChannel() {
		return distributionChannel;
	}


	public void setDistributionChannel(Long distributionChannel) {
		this.distributionChannel = distributionChannel;
	}


	public String getDistributorCode() {
		return distributorCode;
	}


	public void setDistributorCode(String distributorCode) {
		this.distributorCode = distributorCode;
	}


	public String getDistributorName() {
		return distributorName;
	}


	public void setDistributorName(String distributorName) {
		this.distributorName = distributorName;
	}


	public String getNeedInvoice() {
		return needInvoice;
	}


	public void setNeedInvoice(String needInvoice) {
		this.needInvoice = needInvoice;
	}


	public String getRemark() {
		return remark;
	}


	public void setRemark(String remark) {
		this.remark = remark;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public List<Person> getTravellers() {
		return travellers;
	}


	public void setTravellers(List<Person> travellers) {
		this.travellers = travellers;
	}


	public Person getBooker() {
		return booker;
	}


	public void setBooker(Person booker) {
		this.booker = booker;
	}


	public Person getContact() {
		return contact;
	}


	public void setContact(Person contact) {
		this.contact = contact;
	}


	public Person getEmergencyPerson() {
		return emergencyPerson;
	}


	public void setEmergencyPerson(Person emergencyPerson) {
		this.emergencyPerson = emergencyPerson;
	}


	public Long getUserNo() {
		return userNo;
	}


	public void setUserNo(Long userNo) {
		this.userNo = userNo;
	}


	public Map<String, List<Long>> getPromotionMap() {
		return promotionMap;
	}

	public void setPromotionMap(Map<String, List<Long>> promotionMap) {
		this.promotionMap = promotionMap;
	}

	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public char getIsTestOrder() {
		return isTestOrder;
	}


	public void setIsTestOrder(char isTestOrder) {
		this.isTestOrder = isTestOrder;
	}


	public List<Coupon> getCouponList() {
		return couponList;
	}


	public void setCouponList(List<Coupon> couponList) {
		this.couponList = couponList;
	}


	public String getPaymentChannel() {
		return paymentChannel;
	}


	public void setPaymentChannel(String paymentChannel) {
		this.paymentChannel = paymentChannel;
	}


	public String getFaxMemo() {
		return faxMemo;
	}


	public void setFaxMemo(String faxMemo) {
		this.faxMemo = faxMemo;
	}


	public InvoiceInfo getInvoiceInfo() {
		return invoiceInfo;
	}


	public void setInvoiceInfo(InvoiceInfo invoiceInfo) {
		this.invoiceInfo = invoiceInfo;
	}


	public String getYouhui() {
		return youhui;
	}


	public void setYouhui(String youhui) {
		this.youhui = youhui;
	}


	public Long getBonus() {
		return bonus;
	}


	public void setBonus(Long bonus) {
		this.bonus = bonus;
	}


	public Float getBonusYuan() {
		return bonusYuan;
	}


	public void setBonusYuan(Float bonusYuan) {
		this.bonusYuan = bonusYuan;
	}


	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}
	
	
	
	
	/**
	 * 发票信息
	 *
	 * @author pengyayun
	 *
	 */
	public static class InvoiceInfo implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -2661728890261009494L;

		private boolean needFlag;

		private String title;

		private String content;
		
		private String contactPerson;
		
		private String contactTel;
		
		private String address;
		
		private String postCode;
		
		private String amount;
		
		private String deliveryType;//送货方式
		
		private String invoiceType;
		
		private String companyName;

		public boolean isNeedFlag() {
			return needFlag;
		}

		public void setNeedFlag(boolean needFlag) {
			this.needFlag = needFlag;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getContactPerson() {
			return contactPerson;
		}

		public void setContactPerson(String contactPerson) {
			this.contactPerson = contactPerson;
		}

		public String getContactTel() {
			return contactTel;
		}

		public void setContactTel(String contactTel) {
			this.contactTel = contactTel;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getPostCode() {
			return postCode;
		}

		public void setPostCode(String postCode) {
			this.postCode = postCode;
		}

		public String getAmount() {
			return amount;
		}

		public void setAmount(String amount) {
			this.amount = amount;
		}

		public String getDeliveryType() {
			return deliveryType;
		}

		public void setDeliveryType(String deliveryType) {
			this.deliveryType = deliveryType;
		}

		public String getInvoiceType() {
			return invoiceType;
		}

		public void setInvoiceType(String invoiceType) {
			this.invoiceType = invoiceType;
		}

		public String getCompanyName() {
			return companyName;
		}

		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}

	}
	
	/**
	 * 优惠和活动.
	 */
	public static final class Coupon implements Serializable {
		private Long couponId;
		private String code;
		//private String checked = "false";

		public Long getCouponId() {
			return couponId;
		}

		public void setCouponId(Long couponId) {
			this.couponId = couponId;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

	}
	
}
