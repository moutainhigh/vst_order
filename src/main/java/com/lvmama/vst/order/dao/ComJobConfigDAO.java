package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 
 * @author sunjian
 *
 */
@Repository
public class ComJobConfigDAO extends MyBatisDao {

	public ComJobConfigDAO() {
		super("COM_JOB_CONFIG");
	}
	
	public int deleteByPrimaryKey(Long comJobConfigId){
		return super.delete("deleteByPrimaryKey", comJobConfigId);
	}
	
	public int insert(ComJobConfig record){
		return super.insert("insert", record);
	}
	
	public int insertSelective(ComJobConfig record){
		return super.insert("insertSelective", record);
	}
	
	public ComJobConfig selectByPrimaryKey(Long comJobConfigId){
		return super.get("selectByPrimaryKey", comJobConfigId);
	}
	
	public Integer updateByPrimaryKeySelective(ComJobConfig record){
		return super.update("updateByPrimaryKeySelective", record);
	}
	
	public int updateByPrimaryKey(ComJobConfig record){
		return super.update("updateByPrimaryKey", record);
	}
	
	public List<ComJobConfig> selectList(String jobType, Date planTime) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("jobType", jobType);
		params.put("planTime", planTime);
		return super.queryForList("selectList", params);
	}
	
	public List<ComJobConfig> selectByParams(String jobType, Date beginDate, Date endDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("jobType", jobType);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		return super.queryForList("selectByParams", params);
	}
	
	public List<ComJobConfig> selectByObjectId(String jobType, Long objectId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("jobType", jobType);
		params.put("objectId", objectId);
		return super.queryForList("selectByObjectId", params);
	}

	public void deleteComJobConfigByCondition(ComJobConfig comJobConfig) {
		 super.delete("deleteComJobConfigByCondition", comJobConfig);
	}

}
