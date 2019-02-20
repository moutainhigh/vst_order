package com.lvmama.vst.order.webservice.contract;

public class Error {
	private String code; // 错误代码
	private String info;// 错误信息

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
