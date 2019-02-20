/**
 * 
 */
package com.lvmama.vst.order.web.visa.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.OrdFunctionInfo;

/**
 * @author pengyayun
 *
 */
public class OrdVisaProductQueryVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5373877117467581530L;
	
	private String product;//签证产品ID
	
	private String visaCountry; //签证国家/地区
	
	private String visaRange;//所属领区
	
	private String visaType; //签证类型
	
	private String recommendLevel;

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getVisaCountry() {
		return visaCountry;
	}

	public void setVisaCountry(String visaCountry) {
		this.visaCountry = visaCountry;
	}

	public String getVisaRange() {
		return visaRange;
	}

	public void setVisaRange(String visaRange) {
		this.visaRange = visaRange;
	}

	public String getVisaType() {
		return visaType;
	}

	public void setVisaType(String visaType) {
		this.visaType = visaType;
	}

	public String getRecommendLevel() {
		return recommendLevel;
	}

	public void setRecommendLevel(String recommendLevel) {
		this.recommendLevel = recommendLevel;
	}

}
