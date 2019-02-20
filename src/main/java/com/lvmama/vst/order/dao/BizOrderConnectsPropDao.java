package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BizOrderConnectsPropDao extends MyBatisDao {

	public BizOrderConnectsPropDao() {
		super("BIZ_ORDER_CONNECTS_PROP");
	}

	public BizOrderConnectsProp selectByPrimaryKey(Long propId) {
		return super.get("selectByPrimaryKey", propId);
	}
	
	public List<BizOrderConnectsProp> selectAllByParams(Map<String, Object> params) {
		return super.queryForList("selectAllByParams", params);
	}
	
	public long insert(BizOrderConnectsProp bizOrderConnectsProp) {
		return super.insert("insert", bizOrderConnectsProp);
	}

	public long insertSelective(BizOrderConnectsProp bizOrderConnectsProp) {
		return super.insert("insertSelective", bizOrderConnectsProp);
	}

	public int updateByPrimaryKeySelective(BizOrderConnectsProp bizOrderConnectsProp){
		return super.update("updateByPrimaryKeySelective", bizOrderConnectsProp);
	}

	public int updateByPrimaryKey(BizOrderConnectsProp bizOrderConnectsProp){
		return super.update("updateByPrimaryKey", bizOrderConnectsProp);
	}

	public int deleteByPrimaryKey(long propId){
		return super.delete("deleteByPrimaryKey", propId);
	}
	
}