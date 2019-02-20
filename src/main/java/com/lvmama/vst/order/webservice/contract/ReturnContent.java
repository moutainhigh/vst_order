package com.lvmama.vst.order.webservice.contract;

public class ReturnContent {
	private String result;
	private Error[] errors;
	private String token;
	private Contract contract;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Error[] getErrors() {
		return errors;
	}

	public void setErrors(Error[] errors) {
		this.errors = errors;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}
	
	/**
	 * 指定长度的error详细信息
	 * @return
	 */
	public String getErrorDetails(int length) {
		StringBuffer errorSB = new StringBuffer();
		if(errors != null && errors.length > 0) {
			for(Error error : errors) {
				errorSB.append("[ERROR CODE]:" + error.getCode() + "\r\n");
				errorSB.append("[ERROR INFO]:" + error.getInfo() + "\r\n");
			}
		}
		if(errorSB.length() > length) {
			return errorSB.substring(0, length);
		}
		return errorSB.toString();
	}
	/**
	 * error详细信息
	 * @return
	 */
	public String getErrorDetails() {
		StringBuffer errorSB = new StringBuffer();
		if(errors != null && errors.length > 0) {
			for(Error error : errors) {
				errorSB.append("[ERROR CODE]:" + error.getCode() + "\r\n");
				errorSB.append("[ERROR INFO]:" + error.getInfo() + "\r\n");
			}
		}
		
		return errorSB.toString();
	}
}
