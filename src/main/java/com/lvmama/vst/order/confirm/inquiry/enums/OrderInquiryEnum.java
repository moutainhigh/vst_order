package com.lvmama.vst.order.confirm.inquiry.enums;



public class OrderInquiryEnum {
	
	/**
	 * 询单下单,操作类型
	 */
	public static enum API_ORDER_OPERATE_TYPE {

		CREATE(1,"新订"), UPDATA(2,"修改"), CANCEL(3,"取消"),CERTIF_AUDIT(4,"凭证信息同步");
		private int operateCode;
		private String cnName;

		private API_ORDER_OPERATE_TYPE(int code,String name) {
			this.operateCode=code;
			this.cnName = name;
		}

		public String getCnName() {
			return cnName;
		}
		public int getOperateCode() {
			return operateCode;
		}
	}

}
