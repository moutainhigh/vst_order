package com.lvmama.vst.order.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdGuaranteeCreditCardDao extends MyBatisDao {
	public OrdGuaranteeCreditCardDao() {
		super("ORD_GUARANTEE_CREDIT_CARD");
	}

	public int deleteByPrimaryKey(Long ordGuaranteeCreditCardId) {
		return super.delete("deleteByPrimaryKey", ordGuaranteeCreditCardId);
	}

	public int insert(OrdGuaranteeCreditCard ordGuaranteeCreditCard) {
		return super.insert("insert", ordGuaranteeCreditCard);
	}

	public int insertSelective(OrdGuaranteeCreditCard ordGuaranteeCreditCard) {
		return super.insert("insertSelective", ordGuaranteeCreditCard);
	}

	public OrdGuaranteeCreditCard selectByPrimaryKey(Long ordGuaranteeCreditCardId) {
		return super.get("selectByPrimaryKey", ordGuaranteeCreditCardId);
	}

	public int updateByPrimaryKeySelective(OrdGuaranteeCreditCard ordGuaranteeCreditCard) {
		return super.update("updateByPrimaryKeySelective", ordGuaranteeCreditCard);
	}

	public int updateByPrimaryKey(OrdGuaranteeCreditCard ordGuaranteeCreditCard) {
		return super.update("updateByPrimaryKey", ordGuaranteeCreditCard);
	}
	
	public OrdGuaranteeCreditCard getByOrderId(Long orderId){
		OrdGuaranteeCreditCard cc = new OrdGuaranteeCreditCard();
		cc.setOrderId(orderId);
		return super.get("getByOrderId", cc);
	}
	
	public List<Long> selectOrderIds(){
		return super.queryForList("selectOrderIds");
	}
}