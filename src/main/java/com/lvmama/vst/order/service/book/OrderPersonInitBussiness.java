/**
 * 
 */
package com.lvmama.vst.order.service.book;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.BuyInfo.PersonRelation;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.utils.FieldValidUtils;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lancey
 *
 */
@Component
public class OrderPersonInitBussiness extends AbstractBookService{
	private static final Logger logger = LoggerFactory.getLogger(OrderPersonInitBussiness.class);
	public void initPerson(OrdOrderDTO order){
		
		List<OrdPerson> ordPersonList = order.getOrdPersonList();
		if(ordPersonList==null){
			ordPersonList = new ArrayList<OrdPerson>();
		}
		//下单人
		Person booker = order.getBuyInfo().getBooker();
		// 从vo的Person对象转换成po的OrdPerson对象。
		OrdPerson ordPerson = getOrdPersonFromPerson(booker);

		if (ordPerson != null) {
			// 设置下单人与订单关联
			ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			// 设置下单人类型。
			ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name());
			ordPersonList.add(ordPerson);
		}
		if(order.getBuyInfo().getExpressage()!=null){
			ordPerson = makeExpressInfo(order.getBuyInfo().getExpressage());
			if(ordPerson!=null){
				order.setExpressAddress(ordPerson);
				ordPersonList.add(ordPerson);
			}
		}
		if(order.getBuyInfo().hasAdditionalTravel()){
			initAdditionalTravel(order, ordPersonList);
		}
		//加入导游信息
		initGuideInfo(order, ordPersonList);
		order.setOrdPersonList(ordPersonList);
	}

	/**
	 * 导游信息
	 * @param order
	 * @param ordPersonList
     */
	private void initGuideInfo(OrdOrderDTO order, List<OrdPerson> ordPersonList) {
		Person guide = order.getBuyInfo().getGuide();
		if(null!=guide){
			OrdPerson ordPerson=getOrdPersonFromPerson(guide);
			if (ordPerson != null) {
				// 设置导游与订单关联
				ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				// 设置导游类型。
				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.GUIDE.name());
				ordPersonList.add(ordPerson);
			}
		}
	}


	public void initAdditionalTravel(OrdOrderDTO order, List<OrdPerson> ordPersonList) {
		OrdPerson ordPerson;
		//联系人
		Person contact = order.getBuyInfo().getContact();
		// 从vo的Person对象转换成po的OrdPerson对象。
		ordPerson = getOrdPersonFromPerson(contact);
		boolean contactFlag=false;
		if (ordPerson != null) {
			// 设置联系人与订单关联
			ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			// 设置联系人类型。
			ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			ordPersonList.add(ordPerson);
			contactFlag=true;
		}
		
		//紧急联系人
		Person emergencyPerson = order.getBuyInfo().getEmergencyPerson();
		// 从vo的Person对象转换成po的OrdPerson对象。
		ordPerson = getOrdPersonFromPerson(emergencyPerson);

		if (ordPerson != null) {
			// 设置紧急联系人与订单关联
			ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			// 设置紧急联系人类型。
			ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
			ordPersonList.add(ordPerson);
		}
		//游玩人
		List<Person> personList = order.getBuyInfo().getTravellers();
		if (personList != null && !personList.isEmpty()) {
			List<OrdPerson> ordTravellerList = new ArrayList<OrdPerson>();
			for (Person traveller : personList) {
				// 从vo的Person对象转换成po的OrdPerson对象。
				ordPerson = getOrdPersonFromPerson(traveller);

				if (ordPerson != null) {
					// 设置游玩人与订单关联
					ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
					// 设置游玩人类型。
					ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
					ordPersonList.add(ordPerson);
					OrderUtils.resetPersonInfo(ordPerson);
					ordTravellerList.add(ordPerson);
				}
			}
				
			if(!contactFlag){
				OrdPerson person =ordTravellerList.get(0);
				OrdPerson op = new OrdPerson();
				BeanUtils.copyProperties(person, op);
				op.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
				ordPersonList.add(0, op);
			}
			order.setOrdTravellerList(ordTravellerList);
		}
	}



	public OrdPerson makeExpressInfo(BuyInfo.Expressage express) {
		OrdPerson ordPerson;
		//快递联系人			
		ordPerson=this.getOrdAddressFromExpresss(express);
		if(ordPerson!=null){
			// 设置联系人与订单关联
			ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			// 设置联系人类型。
			ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());				
		}
		return ordPerson;
	}
	
	
	
	public List<OrdItemPersonRelation> initPersonRelation(OrdOrderDTO ordOrderDTO, List<BuyInfo.ItemPersonRelation> itemPersonRelationList){
		List<OrdItemPersonRelation> ordItemPersonRelationList = new ArrayList<OrdItemPersonRelation>();
		if (itemPersonRelationList != null && !itemPersonRelationList.isEmpty()) {
			List<Person> travellers = ordOrderDTO.getBuyInfo().getTravellers();
			for (ItemPersonRelation itemPersonRelation : itemPersonRelationList) {
				if (itemPersonRelation != null) {

					OrdPerson ordPerson = null;
					Person person = itemPersonRelation.getPerson();
					if (person != null) {
						int index = travellers.indexOf(person);
						if (index >= 0) {
							ordPerson = ordOrderDTO.getOrdTravellerList().get(
									index);
						}
						itemPersonRelation.setSeq(index);
					} else {
						if (itemPersonRelation.getSeq() != null ) {
							//非游玩人后置
							if(itemPersonRelation.getSeq() != -520) {
								ordPerson = ordOrderDTO.getOrdTravellerList().get(
										itemPersonRelation.getSeq());
							} else {
								ordPerson = new OrdPerson();
							}
						}else{
							continue;
						}
					}
					
					if (ordPerson == null) {
						throwIllegalException("商品人员绑定失败");
					}
					OrdItemPersonRelation ordItemPersonRelation = OrderUtils
							.makeItemOrdPersonRelationRecord(
									itemPersonRelation.getRoomNo(),
									itemPersonRelation.getOptionContent(),
									new Long(itemPersonRelation.getSeq()),
									ordPerson);
					if (ordItemPersonRelation != null) {
						ordItemPersonRelationList.add(ordItemPersonRelation);
					}
				}
			}
		}
		return ordItemPersonRelationList;
	}
	
	public List<BuyInfo.ItemPersonRelation> getPersonRelation(final BuyInfo buyInfo,OrdOrderItem orderItem,OrdOrderPack orderPack){
		String key=null;
		if(orderPack!=null){
			key = "PRODUCT_"+orderPack.getProductId();
		}
		if(key==null||!buyInfo.getPersonRelationMap().containsKey(key)){
			key = "GOODS_"+orderItem.getSuppGoodsId();
		}
		logger.debug("OrderPersonInitBussiness.getPersonRelation key:"+key);
		PersonRelation personRelation=buyInfo.getPersonRelationMap().get(key);
		if(personRelation==null){
			if(orderPack!=null){
				if(StringUtils.equalsIgnoreCase(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.name(), 
						(String)orderPack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
					return ((OrdOrderItemDTO)orderItem).getItem().getItemPersonRelationList();
				}
			}
			return null;
		}
		return personRelation.getItemPersonRelationList();
	}
	
	/**
	 * vo中的Person对象转换成po中的OrdPerson对象。
	 * 
	 * @param person
	 * @return
	 */
	private OrdPerson getOrdPersonFromPerson(Person person) {
		OrdPerson ordPerson = null;

		if (person != null) {
			ordPerson = new OrdPerson();
			ordPerson.setEmail(person.getEmail());
			ordPerson.setFax(person.getFax());
			ordPerson.setFirstName(person.getFirstName());

			ordPerson.setGender(person.getGender());

			ordPerson.setLastName(person.getLastName());
			ordPerson.setMobile(person.getMobile());
			ordPerson.setNationality(person.getNationality());
			ordPerson.setPhone(person.getPhone());
			ordPerson.setIdNo(person.getIdNo());
			ordPerson.setIdType(person.getIdType());
			
			ordPerson.setPeopleType(person.getPeopleType());
			//导游证号
			ordPerson.setGuideCertificate(person.getGuideCertificate());
			//交通接驳 出境手机号
			ordPerson.setOutboundPhone(person.getOutboundPhone());
			if(person.getBirthday() != null && !"".equals(person.getBirthday().trim())) {
				ordPerson.setBirthday(DateUtil.toDate(person.getBirthday().trim(), "yyyy-MM-dd"));
			}

			ordPerson.setPassportUrl(person.getPassportUrl());

			//台胞证和回乡证设置签发地和有效期
			if(!StringUtil.isEmptyString(person.getIdType())){
//
//				if(person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name())||
//						person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name())){
//					
					//有效期
					if(person.getExpDate() != null && !"".equals(person.getExpDate())) {
						ordPerson.setExpDate(person.getExpDate());
					}
					//签发地
					ordPerson.setIssued(person.getIssued());
//				}
					//出生地
					ordPerson.setBirthPlace(person.getBirthPlace());
					//签发日期
					if(person.getIssueDate() != null && !"".equals(person.getIssueDate())) {
						ordPerson.setIssueDate(person.getIssueDate());
					}
			}
			// 设置全名
			boolean isChineseName = false;
			String lastName = ordPerson.getLastName();
			String firstName = ordPerson.getFirstName();
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
			ordPerson.setFullName(person.getFullName());
			if(StringUtils.isEmpty(person.getFullName())){
				if (isChineseName) {
					StringBuffer sb=new StringBuffer();
					if(StringUtils.isNotEmpty(lastName)){
						sb.append(lastName);
					}
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					ordPerson.setFullName(sb.toString());
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
					ordPerson.setFullName(sb.toString());
				}
			}
			
			if(!FieldValidUtils.checkFieldLength(ordPerson.getFullName(), 150)){
				throwIllegalException("游玩人中文姓名长度过长。");
			}
		}

		return ordPerson;
	}
	private OrdPerson getOrdAddressFromExpresss(BuyInfo.Expressage express){
		OrdPerson ordPerson=null;
		OrdAddress ordAddress=null;
		if(express!=null&&StringUtils.isNotEmpty(express.getRecipients())){
			ordAddress=new OrdAddress();
			ordAddress.setCity(express.getCityName());
			ordAddress.setProvince(express.getProvinceName());
			ordAddress.setPostalCode(express.getPostcode()==null?"":express.getPostcode()+"");
			ordAddress.setStreet(express.getAddress());
			
			ordPerson=new OrdPerson();
			ordPerson.setFullName(express.getRecipients());
			ordPerson.setMobile(express.getContactNumber());
			List<OrdAddress> addressList=new ArrayList<OrdAddress>(1);
			addressList.add(ordAddress);
			ordPerson.setAddressList(addressList);			
		}
		return ordPerson;
	}
}
