package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 
 *
 */
@Repository
public class ComMessageDao extends MyBatisDao {

	public ComMessageDao() {
		super("COM_MESSAGE");
	}

	public int deleteByPrimaryKey(Long ComMessageId) {
		return super.delete("deleteByPrimaryKey", ComMessageId);
	}

	public int insert(ComMessage record) {
		return super.insert("insert", record);
	}

	public int insertSelective(ComMessage record) {
		return super.insert("insertSelective", record);
	}

	public ComMessage selectByPrimaryKey(Long ComMessageId) {
		return super.get("selectByPrimaryKey", ComMessageId);
	}

	public int updateByPrimaryKeySelective(ComMessage record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(ComMessage record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<ComMessage> findComMessageList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}