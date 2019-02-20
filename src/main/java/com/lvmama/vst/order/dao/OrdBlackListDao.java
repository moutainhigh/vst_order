package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.supp.po.SuppGoodsBlackList;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdBlackListDao extends MyBatisDao {

	public OrdBlackListDao() {
		super("ORD_BLACK_LIST");
	}

	public int deleteByPrimaryKey(Long blacklistId) {
		return super.delete("deleteByPrimaryKey", blacklistId);
	}

	public int insert(SuppGoodsBlackList record) {
		return super.insert("insert", record);
	}

	public int insertSelective(SuppGoodsBlackList record) {
		return super.insert("insertSelective", record);
	}

	public SuppGoodsBlackList selectByPrimaryKey(Long blacklistId) {
		return super.get("selectByPrimaryKey", blacklistId);
	}
	
	public List<SuppGoodsBlackList> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Long queryCount(Map<String, Object> params) {
		return super.get("queryCount", params);
	}
	
	public int updateByPrimaryKeySelective(SuppGoodsBlackList record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	int updateByPrimaryKey(SuppGoodsBlackList record) {
		return super.update("updateByPrimaryKey", record);
	}

	public Long findGoodsIdByBlackId(Long blacklistId){
		return super.get("findGoodsIdByBlackId", blacklistId);
	}

}