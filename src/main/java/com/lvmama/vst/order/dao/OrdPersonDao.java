package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderpersonInvoiceInfoAddress;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdPersonDao extends MyBatisDao {

	public OrdPersonDao() {
		super("ORD_PERSON");
	}

	public int deleteByPrimaryKey(Long ordPersonId) {
		return super.delete("deleteByPrimaryKey", ordPersonId);
	}

	public int insert(OrdPerson ordPerson) {
		ordPerson.setUpdateTime(new Date());
		return super.insert("insert", ordPerson);
	}

	public int insertSelective(OrdPerson ordPerson) {
		ordPerson.setUpdateTime(new Date());
		return super.insert("insertSelective", ordPerson);
	}

	public OrdPerson selectByPrimaryKey(Long ordPersonId) {
		return super.get("selectByPrimaryKey", ordPersonId);
	}

	public int updateByPrimaryKeySelective(OrdPerson ordPerson) {
		ordPerson.setUpdateTime(new Date());
		// 同步更新订单查询信息
		syncOrderQueryInfo(ordPerson);
		return super.update("updateByPrimaryKeySelective", ordPerson);
	}

	public int updateByPrimaryKey(OrdPerson ordPerson) {
		ordPerson.setUpdateTime(new Date());
		// 同步更新订单查询信息
		syncOrderQueryInfo(ordPerson);

		return super.update("updateByPrimaryKey", ordPerson);
	}

	public List<OrdPerson> findOrdPersonList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	/**
	 * 参数要求传入orderId和personType
	 * 
	 * @param params
	 * @return
	 */
	public List<OrdPerson> findOrdPersonListWithAddress(
			Map<String, Object> params) {
		return super.queryForList("selectOrdPersonListWithAddress", params);
	}

	/**
	 * 同步更新订单查询信息
	 */
	private void syncOrderQueryInfo(OrdPerson ordPerson) {
		// 同步更新订单查询信息
		OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();
		if ("ORDER".equals(ordPerson.getObjectType())) {
			if (OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(
					ordPerson.getPersonType())) {
				orderQueryInfo.setOrderId(ordPerson.getObjectId());
				orderQueryInfo.setBookerName(ordPerson.getFullName());
				orderQueryInfo.setBookerMobile(ordPerson.getMobile());
			}
			if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(
					ordPerson.getPersonType())) {
				orderQueryInfo.setOrderId(ordPerson.getObjectId());
				orderQueryInfo.setContactName(ordPerson.getFullName());
				orderQueryInfo.setContactMobile(ordPerson.getMobile());
				if(ordPerson.getPhone()!=null){
					orderQueryInfo.setContactPhone(ordPerson.getPhone());
				}
				orderQueryInfo.setContactEmail(ordPerson.getEmail());
			}
		}
//		if (orderQueryInfo.getOrderId() != null) {
//			orderQueryInfoDao.updateByOrderId(orderQueryInfo);
//		}
	}
	
	public List<OrdPerson> selectLatestContactPerson(Map<String, Object> params) {
		return queryForList("selectLatestContactPerson", params);
		
	}

	/**
	 * 修改游玩人信息
	 * */
	public int updateTraveller(OrdPerson ordPerson){
		ordPerson.setUpdateTime(new Date());
		return super.update("updateTraveller", ordPerson);
	}
	
	public int deleteByOrderId(Long orderId){
		return super.delete("deleteByOrderId", orderId);
	}
	
	
	public List<OrderpersonInvoiceInfoAddress> findOrdPersonListInvoice(Map<String, Object> params) {
		return super.queryForList("selectByParamsApplyInvoice", params);
	}

	public List<OrdPerson> getBookPersonInfoByOrderId(Map<String, Object> params){
		Long orderId =(Long)params.get("orderId");
		return super.queryForList("selectInvoicePersonByOrderId", orderId);
	}

   public List<OrdPerson> getOrdApplyInvoicePersonByOrderId(Map<String, Object> params){
        Long orderId =(Long)params.get("orderId");
        return super.queryForList("getOrdApplyInvoicePersonByOrderId", orderId);
    }

}