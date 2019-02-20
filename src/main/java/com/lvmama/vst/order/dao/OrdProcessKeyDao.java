package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository("ordProcessKeyDao")
public class OrdProcessKeyDao extends MyBatisDao {
	
	public OrdProcessKeyDao() {
		super("ORD_PROCESS_KEY");
	}

	/**
	 * 
	 * @Description: 根据主键id查询工作流
	 * @param ordProcessKeyId
	 * @return
	 */
	public OrdProcessKey selectByPrimaryKey(Long ordProcessKeyId) {
		return super.get("selectByPrimaryKey", ordProcessKeyId);
	}

	/**
	 * 
	 * @Description: 查询工作流
	 * @param ordProcessKey
	 * @return
	 */
	public List<OrdProcessKey> query(OrdProcessKey ordProcessKey) {
		return super.queryForList("query", ordProcessKey);
	}
	
	public List<OrdProcessKey> selectOrdProcessKeyList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	/**
	 * 
	 * @Description: 保存工作流
	 * @param ordProcessKey
	 * @return
	 */
	public Integer insert(OrdProcessKey ordProcessKey) {
		return super.insert("insert", ordProcessKey);
	}

	/**
	 * 
	 * @Description: 更新工作流
	 * @param ordProcessKey
	 * @return
	 */
	public Integer update(OrdProcessKey ordProcessKey) {
		return super.update("update", ordProcessKey);
	}

	/**
	 * 
	 * @Description: 更新工作流状态(流程启动状态（0未启动，1已启动，2启动失败）)
	 * @param params
	 * @return
	 */
	public Integer updateStatus(Map<String, Object> params) {
		return super.update("updateStatus", params);
	}
	
	/**
	 * 
	 * @Description: 逻辑删除,设置VALID为N(无效)
	 * @param params
	 * @return
	 */
	public int deleteOrdProcessKey(Map<String, Object> params) {
		return super.update("deleteOrdProcessKey", params);
	}

	/**
	 * 
	 * @Description: 物理删除工作流
	 * @param ordProcessKeyId
	 * @return
	 */
	public int deleteByPrimaryKey(Long ordProcessKeyId) {
		return super.delete("deleteByPrimaryKey", ordProcessKeyId);
	}
}
