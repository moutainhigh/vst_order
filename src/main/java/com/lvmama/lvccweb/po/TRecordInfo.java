package com.lvmama.lvccweb.po;

import java.io.Serializable;
import java.util.Date;


public class TRecordInfo implements Serializable {

	private static final long serialVersionUID = 4146917614542565584L;

	private String callId;

    private String callerNo;

    private String calleeNo;
	  
    private Integer agentId;
	  
	private Integer callCenterId;
	  
	private Integer virtualCallCenterId;
	 
	private Date beginTime;
	  
	private Date endTime;
	  
	private String fileName;
	  
	private Integer callType;
	  
	private Integer serviceNo;
	  
	private Date visitTime;
	
	private Integer visitFlag;
	  
	private Integer mediaType;
	  
	private Integer modNo;
	  
	private Integer trkNo;
	  
	private Integer serviceId;
	  
	private String serviceInfo;
	  
	private String callInfo;
	  
	private Integer stopReason;
	  
	private Integer locationId;
	  
	private Integer recordFormat;
	  
	private Integer userWantedSkillId;
	  
	private Integer currentSkillId;
	  
	private Integer custLevel;
	
	private String agentName;

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getCallerNo() {
		return callerNo;
	}

	public void setCallerNo(String callerNo) {
		this.callerNo = callerNo;
	}

	public String getCalleeNo() {
		return calleeNo;
	}

	public void setCalleeNo(String calleeNo) {
		this.calleeNo = calleeNo;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public Integer getCallCenterId() {
		return callCenterId;
	}

	public void setCallCenterId(Integer callCenterId) {
		this.callCenterId = callCenterId;
	}

	public Integer getVirtualCallCenterId() {
		return virtualCallCenterId;
	}

	public void setVirtualCallCenterId(Integer virtualCallCenterId) {
		this.virtualCallCenterId = virtualCallCenterId;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getCallType() {
		return callType;
	}

	public void setCallType(Integer callType) {
		this.callType = callType;
	}

	public Integer getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(Integer serviceNo) {
		this.serviceNo = serviceNo;
	}

	public Date getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

	public Integer getVisitFlag() {
		return visitFlag;
	}

	public void setVisitFlag(Integer visitFlag) {
		this.visitFlag = visitFlag;
	}

	public Integer getMediaType() {
		return mediaType;
	}

	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}

	public Integer getModNo() {
		return modNo;
	}

	public void setModNo(Integer modNo) {
		this.modNo = modNo;
	}

	public Integer getTrkNo() {
		return trkNo;
	}

	public void setTrkNo(Integer trkNo) {
		this.trkNo = trkNo;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(String serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	public String getCallInfo() {
		return callInfo;
	}

	public void setCallInfo(String callInfo) {
		this.callInfo = callInfo;
	}

	public Integer getStopReason() {
		return stopReason;
	}

	public void setStopReason(Integer stopReason) {
		this.stopReason = stopReason;
	}

	public Integer getLocationId() {
		return locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	public Integer getRecordFormat() {
		return recordFormat;
	}

	public void setRecordFormat(Integer recordFormat) {
		this.recordFormat = recordFormat;
	}

	public Integer getUserWantedSkillId() {
		return userWantedSkillId;
	}

	public void setUserWantedSkillId(Integer userWantedSkillId) {
		this.userWantedSkillId = userWantedSkillId;
	}

	public Integer getCurrentSkillId() {
		return currentSkillId;
	}

	public void setCurrentSkillId(Integer currentSkillId) {
		this.currentSkillId = currentSkillId;
	}

	public Integer getCustLevel() {
		return custLevel;
	}

	public void setCustLevel(Integer custLevel) {
		this.custLevel = custLevel;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
  
}
