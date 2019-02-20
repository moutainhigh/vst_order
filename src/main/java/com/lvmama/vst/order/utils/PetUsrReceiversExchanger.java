package com.lvmama.vst.order.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl;
import com.lvmama.vst.pet.vo.PetUsrReceivers;

public class PetUsrReceiversExchanger {
	
	private static final Log LOG = LogFactory.getLog(PetUsrReceiversExchanger.class);

	/**
	 * PetUsrReceivers对象转换成OrdPerson对象
	 * 
	 * @param person
	 * @param userId
	 * @return
	 */
	public static PetUsrReceivers changeOrdPerson2PetUsrReceivers(OrdPerson ordPerson) {
		PetUsrReceivers petUsrReceivers = null;
		if (ordPerson != null) {
			petUsrReceivers = new PetUsrReceivers();
			
			petUsrReceivers.setEmail(ordPerson.getEmail());
			petUsrReceivers.setFax(ordPerson.getFax());
			petUsrReceivers.setReceiverName(ordPerson.getFullName());
			if (OrderEnum.ORDER_PERSON_GENDER_TYPE.MAN.name().equals(ordPerson.getGender())) {
				petUsrReceivers.setGender("M");
			} else if (OrderEnum.ORDER_PERSON_GENDER_TYPE.WOMAN.name().equals(ordPerson.getGender())) {
				petUsrReceivers.setGender("F");
			}
			petUsrReceivers.setCardNum(ordPerson.getIdNo());
			petUsrReceivers.setIssued(ordPerson.getIssued());
			petUsrReceivers.setExpDate(ordPerson.getExpDate());
			petUsrReceivers.setCardType(ordPerson.getIdType());
			petUsrReceivers.setMobileNumber(ordPerson.getMobile());
			petUsrReceivers.setPhone(ordPerson.getPhone());
			if (OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name().equals(ordPerson.getPersonType())) {
				petUsrReceivers.setReceiversType(ordPerson.getPersonType());
			} else {
				petUsrReceivers.setReceiversType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			}
			
			if (ordPerson.getAddressList() != null && ordPerson.getAddressList().size() > 0) {
				OrdAddress address = ordPerson.getAddressList().get(0);
				if (address != null) {
					petUsrReceivers.setProvince(address.getProvince());
					petUsrReceivers.setCity(address.getCity());
					petUsrReceivers.setPostCode(address.getPostalCode());
					petUsrReceivers.setAddress(address.getDistrict() + "||" + address.getStreet());
				}
			}
		}
		
		return petUsrReceivers;
	}
	
	/**
	 * OrdPerson对象转换成PetUsrReceivers对象
	 * 
	 * @param petUsrReceivers
	 * @return
	 */
	public static OrdPerson changePetUsrReceivers2OrdPerson(PetUsrReceivers petUsrReceivers) {
		OrdPerson ordPerson = null;
		if (petUsrReceivers != null) {
			ordPerson = new OrdPerson();
			ordPerson.setEmail(petUsrReceivers.getEmail());
			ordPerson.setFax(petUsrReceivers.getFax());
			ordPerson.setFullName(petUsrReceivers.getReceiverName());
			if ("M".equals(petUsrReceivers.getGender())) {
				ordPerson.setGender(OrderEnum.ORDER_PERSON_GENDER_TYPE.MAN.name());
			} else {
				ordPerson.setGender(OrderEnum.ORDER_PERSON_GENDER_TYPE.WOMAN.name());
			}
			ordPerson.setIdNo(petUsrReceivers.getCardNum());
			ordPerson.setIdType(petUsrReceivers.getCardType());
			ordPerson.setMobile(petUsrReceivers.getMobileNumber());
			ordPerson.setPersonType(petUsrReceivers.getReceiversType());
			ordPerson.setPhone(petUsrReceivers.getPhone());
			ordPerson.setIssued(petUsrReceivers.getIssued());
			ordPerson.setExpDate(petUsrReceivers.getExpDate());
			List<OrdAddress> addressList = new ArrayList<OrdAddress>();
			OrdAddress address = new OrdAddress();
			address.setCity(petUsrReceivers.getCity());
			address.setPostalCode(petUsrReceivers.getPostCode());
			address.setProvince(petUsrReceivers.getProvince());
			if (petUsrReceivers.getAddress() != null) {
				String[] str = petUsrReceivers.getAddress().split("||");
				if (str != null && str.length >= 2) {
					address.setDistrict(str[0]);
					address.setStreet(str[0]);
				}
			}
			addressList.add(address);
			ordPerson.setAddressList(addressList);
		}
		
		return ordPerson;
	}
	
	/**
	 * PetUsrReceivers对象转换成Person对象
	 * @param person
	 * @param personType
	 * @return
	 */
	public static PetUsrReceivers changePerson2PetUsrReceivers(Person person, String personType) {
		PetUsrReceivers petUsrReceivers = null;
		if (person != null) {
			petUsrReceivers = new PetUsrReceivers();
			petUsrReceivers.setReceiverId(person.getReceiverId());
			
			petUsrReceivers.setEmail(person.getEmail());
			petUsrReceivers.setFax(person.getFax());
			
			petUsrReceivers.setFirstName( person.getFirstName());
			petUsrReceivers.setLastName(person.getLastName());
			// 设置全名
			if (person.getFullName() == null || "".equals(person.getFullName().trim())) {
				boolean isChineseName = false;
				String lastName = person.getLastName();
				String firstName = person.getFirstName();

				if (lastName != null) {
					if (StringUtil.hasChinese(lastName)) {
						isChineseName = true;
					}
				}

				if (!isChineseName && (firstName != null)) {
					if (StringUtil.hasChinese(firstName)) {
						isChineseName = true;
					}
				}
				
				if (isChineseName) {
					StringBuffer sb=new StringBuffer();
					if(StringUtils.isNotEmpty(lastName)){
						sb.append(lastName);
					}
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					
					petUsrReceivers.setReceiverName(sb.toString());
				} else {
					StringBuffer sb=new StringBuffer();
					
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					if(StringUtils.isNotEmpty(lastName)){
						if(sb.length()>1){
							sb.append("/");
						}
						sb.append(lastName);
					}
					
					petUsrReceivers.setReceiverName(sb.toString());
				}
			}else {
				petUsrReceivers.setReceiverName(person.getFullName());
			}
			
			if (OrderEnum.ORDER_PERSON_GENDER_TYPE.MAN.name().equals(person.getGender())) {
				petUsrReceivers.setGender("M");
			} else if (OrderEnum.ORDER_PERSON_GENDER_TYPE.WOMAN.name().equals(person.getGender())) {
				petUsrReceivers.setGender("F");
			}
			petUsrReceivers.setCardNum(person.getIdNo());
			petUsrReceivers.setCardType(person.getIdType());
			petUsrReceivers.setMobileNumber(person.getMobile());
			petUsrReceivers.setPhone(person.getPhone());
			
			if(StringUtil.isNotEmptyString(person.getIdType())){
				if(person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name())
						|| person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name()) 
						|| person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.HUZHAO.name()) ){
					petUsrReceivers.setIssued(person.getIssued());
					petUsrReceivers.setExpDate(person.getExpDate());
				}else{
					petUsrReceivers.setIssued("");
					petUsrReceivers.setExpDate(null);
				}
			}
			

			if (StringUtils.isNotEmpty(person.getBirthday())) {
				try {
					petUsrReceivers.setBrithday(CalendarUtils.getDateFormatDate(person.getBirthday(), "yyyy-MM-dd"));
				} catch (Exception e) {
					//e.printStackTrace();
					LOG.info(e.getMessage());
				}
			}
			
			if (OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name().equals(personType)) {
				petUsrReceivers.setReceiversType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());
			} else {
				petUsrReceivers.setReceiversType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			}
		}
		
		return petUsrReceivers;
	}
	
	/**
	 * Person对象转换成PetUsrReceivers对象
	 * 
	 * @param petUsrReceivers
	 * @return
	 */
	public static Person changePetUsrReceivers2Person(PetUsrReceivers petUsrReceivers) {
		Person person = null;
		if (petUsrReceivers != null) {
			person = new Person();
			person.setReceiverId(petUsrReceivers.getReceiverId());
			person.setEmail(petUsrReceivers.getEmail());
			person.setFax(petUsrReceivers.getFax());
			person.setFullName(petUsrReceivers.getReceiverName());
			if ("M".equals(petUsrReceivers.getGender())) {
				person.setGender(OrderEnum.ORDER_PERSON_GENDER_TYPE.MAN.name());
			} else {
				person.setGender(OrderEnum.ORDER_PERSON_GENDER_TYPE.WOMAN.name());
			}
			person.setIdNo(petUsrReceivers.getCardNum());
			person.setIdType(petUsrReceivers.getCardType());
			person.setMobile(petUsrReceivers.getMobileNumber());
			person.setPhone(petUsrReceivers.getPhone());
			person.setIssued(petUsrReceivers.getIssued());
			person.setExpDate(petUsrReceivers.getExpDate());
			person.setFirstName(petUsrReceivers.getFirstName());
			person.setLastName(petUsrReceivers.getLastName());
			if (petUsrReceivers.getBrithday() != null) {
				try{
					if(petUsrReceivers.getBrithday()!=null)
						person.setBirthday(CalendarUtils.getDateFormatString(petUsrReceivers.getBrithday(), "yyyy-MM-dd"));
				}catch(Exception e){
					LOG.info(e.getMessage());
					//e.printStackTrace();
				}
			}
		}
		
		return person;
	}
}
