package com.lvmama.vst.neworder.order.sms;

import java.io.Serializable;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;

public class OrderSmsPo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//规格名称（branchName），地址（prodAddress），电话（prodTel），最晚预定（lastTime），担保时间（guaranteeTime）
	
	private OrdOrder ordOrder;
	
	private ProductSmsPo productSmsPo;
	
	/**
	 * 自定义短信 
	 * key--子单id
	 * value--内容（String）
	 */
	Map<String, String> customSmsMap;

	public Map<String, String> getCustomSmsMap() {
		return customSmsMap;
	}

	public void setCustomSmsMap(Map<String, String> customSmsMap) {
		this.customSmsMap = customSmsMap;
	}

	public OrdOrder getOrdOrder() {
		return ordOrder;
	}

	public void setOrdOrder(OrdOrder ordOrder) {
		this.ordOrder = ordOrder;
	}

	public ProductSmsPo getProductSmsPo() {
		return productSmsPo;
	}

	public void setProductSmsPo(ProductSmsPo productSmsPo) {
		this.productSmsPo = productSmsPo;
	}



	
}
