package com.lvmama.vst.neworder.order.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 下单用户信息
 * 
 * @author lancey
 * 
 */
public class Person implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3050925777711411310L;
	private String lastName;
	private String firstName;
	private String fullName;
	private String mobile;
	private String outboundPhone;
	private String idType;
	private String idNo;
	private String birthday;
	private String email;
	private String fax;
	private String gender;
	private String nationality;
	private String phone;
	//字符串：PEOPLE_TYPE_ADULT=成人，PEOPLE_TYPE_CHILD=儿童，PEOPLE_TYPE_OLDER=老人
	private String peopleType;

	private String personType;
	
	/**
	 * 远程的PetUsrReceivers对象ID
	 */
	private String receiverId;
	
	/**
	 * 是否买保险
	 */
	private String buyInsuranceFlag="N";
	
	/**
	 * 是否保存到常用游玩人
	 */
	private String saveFlag="false";
	  
    private Date expDate; // 有效期
    
    private String issued ; // 签发地
    
	private Date issueDate; // 期发日期
	private String birthPlace; // 出生地
	
	private Long roomNo;
	
	private String passportUrl;
	
	public String getPassportUrl() {
		return passportUrl;
	}
	public void setPassportUrl(String passportUrl) {
		this.passportUrl = passportUrl;
	}
	public Date getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}
	public String getBirthPlace() {
		return birthPlace;
	}
	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}
    public Date getExpDate() {
		return expDate;
	}
	public void setExpDate(Date expDate) {
		this.expDate = expDate;
	}
	public String getIssued() {
		return issued;
	}
	public void setIssued(String issued) {
		this.issued = issued;
	}
   
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public String getPeopleType() {
		return peopleType;
	}

	public void setPeopleType(String peopleType) {
		this.peopleType = peopleType;
	}

	public String getBuyInsuranceFlag() {
		return buyInsuranceFlag;
	}

	public void setBuyInsuranceFlag(String buyInsuranceFlag) {
		this.buyInsuranceFlag = buyInsuranceFlag;
	}

	public String getSaveFlag() {
		return saveFlag;
	}

	public void setSaveFlag(String saveFlag) {
		this.saveFlag = saveFlag;
	}
	
	public void setBirthdayStr(String birthday){
		this.birthday = birthday;
	}
	
	public String getBirthdayStr(){
		return birthday;
	}
	public Long getRoomNo() {
		return roomNo;
	}
	public void setRoomNo(Long roomNo) {
		this.roomNo = roomNo;
	}
	public String getOutboundPhone() {
		return outboundPhone;
	}
	public void setOutboundPhone(String outboundPhone) {
		this.outboundPhone = outboundPhone;
	}

	public String getPersonType() {
		return personType;
	}

	public void setPersonType(String personType) {
		this.personType = personType;
	}


}
