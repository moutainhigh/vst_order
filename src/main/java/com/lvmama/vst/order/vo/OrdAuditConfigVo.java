/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.OrdFunctionInfo;

/**
 * @author pengyayun
 *
 */
public class OrdAuditConfigVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5373877117467581530L;
	
	private Long ordAllocationId;
	
	private Long firstDeptId;
	
	private String firstDeptName;
	
	private Long secondDeptId;
	
	private String secondDeptName;
	
	private Long threeDeptId;
	
	private String threeDeptName;
	
	private BizCategory bizCategory;
	
	private BusinessRule businessRule;
	
    private String distributionChannel;
	
	private List<OrdFunctionInfo>  ordFunctionInfoList;

	public BizCategory getBizCategory() {
		return bizCategory;
	}
	public void setBizCategory(BizCategory bizCategory) {
		this.bizCategory = bizCategory;
	}
	public Long getFirstDeptId() {
		return firstDeptId;
	}

	public void setFirstDeptId(Long firstDeptId) {
		this.firstDeptId = firstDeptId;
	}

	public String getFirstDeptName() {
		return firstDeptName;
	}

	public void setFirstDeptName(String firstDeptName) {
		this.firstDeptName = firstDeptName;
	}

	public Long getSecondDeptId() {
		return secondDeptId;
	}

	public void setSecondDeptId(Long secondDeptId) {
		this.secondDeptId = secondDeptId;
	}

	public String getSecondDeptName() {
		return secondDeptName;
	}

	public void setSecondDeptName(String secondDeptName) {
		this.secondDeptName = secondDeptName;
	}

	public Long getThreeDeptId() {
		return threeDeptId;
	}

	public void setThreeDeptId(Long threeDeptId) {
		this.threeDeptId = threeDeptId;
	}

	public String getThreeDeptName() {
		return threeDeptName;
	}

	public void setThreeDeptName(String threeDeptName) {
		this.threeDeptName = threeDeptName;
	}
	public List<OrdFunctionInfo> getOrdFunctionInfoList() {
		return ordFunctionInfoList;
	}
	public void setOrdFunctionInfoList(List<OrdFunctionInfo> ordFunctionInfoList) {
		this.ordFunctionInfoList = ordFunctionInfoList;
	}
	public Long getOrdAllocationId() {
		return ordAllocationId;
	}
	public void setOrdAllocationId(Long ordAllocationId) {
		this.ordAllocationId = ordAllocationId;
	}
	
	public String getDistributionChannel() {
		return distributionChannel;
	}
	public void setDistributionChannel(String distributionChannel) {
		this.distributionChannel = distributionChannel;
	}
	public String getDeptStr(){
		StringBuffer str=new StringBuffer();
		if(StringUtils.isNotEmpty(firstDeptName)){
			str.append(firstDeptName);
			if(StringUtils.isNotEmpty(secondDeptName)){
				str.append(">");
				str.append(secondDeptName);
				if(StringUtils.isNotEmpty(threeDeptName)){
					str.append(">");
					str.append(threeDeptName);
				}
			}
		}
		return str.toString();
	}
	public BusinessRule getBusinessRule() {
		return businessRule;
	}
	public void setBusinessRule(BusinessRule businessRule) {
		this.businessRule = businessRule;
	}
}
