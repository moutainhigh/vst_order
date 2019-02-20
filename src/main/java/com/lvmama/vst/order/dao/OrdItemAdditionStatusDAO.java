package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdItemAdditionStatus;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdItemAdditionStatusDAO extends MyBatisDao {

	public OrdItemAdditionStatusDAO() {
		super("ORD_ITEM_ADDITION_STATUS");
	}
	
	public List<OrdItemAdditionStatus> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	

    public int deleteByPrimaryKey(Long ordItemAdditionStatusId) {
    	return super.delete("deleteByPrimaryKey", ordItemAdditionStatusId);
    }

    public int insert(OrdItemAdditionStatus record) {
    	return super.insert("insert", record);
    }

    public OrdItemAdditionStatus selectByPrimaryKey(Long ordItemAdditionStatusId) {
    	return super.get("selectByPrimaryKey", ordItemAdditionStatusId);
    }

    public int updateByPrimaryKeySelective(OrdItemAdditionStatus record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdItemAdditionStatus record) {
    	return super.update("updateByPrimaryKey", record);
    }
}
