package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdAddressDao extends MyBatisDao {
	public OrdAddressDao() {
		super("ORD_ADDRESS");
	}

	public int deleteByPrimaryKey(Long ordAddressId) {
		return super.delete("deleteByPrimaryKey", ordAddressId);
	}

	public int insert(OrdAddress ordAddress) {
		return super.insert("insert", ordAddress);
	}

	public int insertSelective(OrdAddress ordAddress) {
		return super.insert("insertSelective", ordAddress);
	}

	public OrdAddress selectByPrimaryKey(Long ordAddressId) {
		return super.get("selectByPrimaryKey", ordAddressId);
	}

	public int updateByPrimaryKeySelective(OrdAddress ordAddress) {
		return super.update("updateByPrimaryKeySelective", ordAddress);
	}

	public int updateByPrimaryKey(OrdAddress ordAddress) {
		return super.update("updateByPrimaryKey", ordAddress);
	}
	
	public List<OrdAddress> findOrdAddressList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}	
}