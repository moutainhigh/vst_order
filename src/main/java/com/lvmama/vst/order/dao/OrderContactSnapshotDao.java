package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrderContactSnapshotDao extends MyBatisDao {
	
	public OrderContactSnapshotDao() {
		super("ORD_CONTRACT_SNAPSHOT_DATA");
	}

	public int insertSelective(OrdContractSnapshotData ordContractSnapshotData) {
		return super.insert("insertSelective", ordContractSnapshotData);
	}
	
    public List<OrdContractSnapshotData> selectByParam(Map<String, Object> params) {
    	return super.queryForList("selectByParams", params);
    }

}
