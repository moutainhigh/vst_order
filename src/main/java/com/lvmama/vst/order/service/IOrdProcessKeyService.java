package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;
import com.lvmama.vst.back.order.po.OrdProcessKey;

/**
 * OrdProcessKey操作
 * @author zhangbin
 *
 */
public interface IOrdProcessKeyService {
	
	public OrdProcessKey selectByPrimaryKey(Long ordProcessKeyId);

	/**
	 * 
	 * @Description: 查询工作流
	 * @param ordProcessKey
	 * @return
	 */
	public List<OrdProcessKey> query(OrdProcessKey ordProcessKey);
	
	/**
	 * @Description: 根据参数查询结果
	 * @param params
	 * @return
	 */
	public List<OrdProcessKey> selectOrdProcessKeyList(Map<String, Object> params);
	

	/**
	 * 
	 * @Description: 保存工作流
	 * @param ordProcessKey
	 * @return
	 */
	public Integer insert(OrdProcessKey ordProcessKey);

	/**
	 * 
	 * @Description: 更新工作流
	 * @param ordProcessKey
	 * @return
	 */
	public Integer update(OrdProcessKey ordProcessKey);

	/**
	 * 
	 * @Description: 更新工作流状态(流程启动状态（0未启动，1已启动，2启动失败）)
	 * @param params
	 * @return
	 */
	public Integer updateStatus(Map<String, Object> params);
	
	/**
	 * 
	 * @Description: 逻辑删除,设置VALID为N(无效)
	 * @param params
	 * @return
	 */
	public int deleteOrdProcessKey(Map<String, Object> params);

	/**
	 * 
	 * @Description: 物理删除工作流
	 * @param ordProcessKeyId
	 * @return
	 */
	public int deleteByPrimaryKey(Long ordProcessKeyId);
}
