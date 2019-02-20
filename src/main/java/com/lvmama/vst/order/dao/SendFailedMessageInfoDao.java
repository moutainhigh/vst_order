package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.SendFailedMessaeInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SendFailedMessageInfoDao extends MyBatisDao {
	public SendFailedMessageInfoDao() {
		super("SEND_FAILED_MESSAGE_INFO");
	}

	public int deleteByPrimaryKey(Long failedMessageId) {
		return super.delete("deleteByPrimaryKey", failedMessageId);
	}

	public int deleteByOrderId(Long orderId) {
		return super.delete("deleteByOrderId", orderId);
	}

	public int deleteByOrderItemId(Long orderItemId) {
		return super.delete("deleteByOrderItemId", orderItemId);
	}

	public int insert(SendFailedMessaeInfo sendFailedMessaeInfo) {
		return super.insert("insert", sendFailedMessaeInfo);
	}

	public int insertSelective(SendFailedMessaeInfo SendFailedMessaeInfo) {
		return super.insert("insertSelective", SendFailedMessaeInfo);
	}


	public SendFailedMessaeInfo selectByPrimaryKey(Long failedMessageId) {
		return super.get("selectByPrimaryKey", failedMessageId);
	}

	public List<SendFailedMessaeInfo> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public int updateByPrimaryKeySelective(SendFailedMessaeInfo sendFailedMessaeInfo) {
		return super.update("updateByPrimaryKeySelective", sendFailedMessaeInfo);
	}

	public int updateByPrimaryKey(SendFailedMessaeInfo sendFailedMessaeInfo) {
		return super.update("updateByPrimaryKey", sendFailedMessaeInfo);
	}

	public int updateMessageStatusByOrderId(Long orderId) {
		return super.update("updateMessageStatusByOrderId", orderId);
	}

	public List<SendFailedMessaeInfo> selectByOrderId(Long orderId) {
		return super.queryForList("selectByOrderId", orderId);
	}

	public List<SendFailedMessaeInfo> selectByOrderItemId(Long orderItemId) {
		return super.queryForList("selectByOrderItemId", orderItemId);
	}

	public Long getFailedMessageCount(Map<String, Object> paramMap){
		return super.get("getFailedMessageCount", paramMap);
	}

	/**
	 * 最多返回1000条数据
	 * @return
	 */
	public List<SendFailedMessaeInfo> getFailedMessageInfoList() {
		return super.queryForList("getFailedMessageInfoList", new HashMap());
	}

	public int clearTwoMonthAgoFailedMessageInfo(){
		return super.delete("clearTwoMonthAgoFailedMessageInfo",new HashMap());
	}
}
