package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdItemAddition;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrdItemAdditionDAO extends MyBatisDao {

	public OrdItemAdditionDAO() {
		super("ORD_ITEM_ADDITION");
	}
	
	public List<OrdItemAddition> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	

    public int deleteByPrimaryKey(Long ordItemAdditionId) {
    	return super.delete("deleteByPrimaryKey", ordItemAdditionId);
    }

    public int insert(OrdItemAddition record) {
    	return super.insert("insert", record);
    }

    public OrdItemAddition selectByPrimaryKey(Long ordItemAdditionId) {
    	return super.get("selectByPrimaryKey", ordItemAdditionId);
    }

    public int updateByPrimaryKeySelective(OrdItemAddition record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdItemAddition record) {
    	return super.update("updateByPrimaryKey", record);
    }
}
