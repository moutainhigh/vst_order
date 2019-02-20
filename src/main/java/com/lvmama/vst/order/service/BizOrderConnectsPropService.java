package com.lvmama.vst.order.service;

import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BizOrderConnectsPropService {

	/**
	 * 查询交通接驳属性对象
	 * @param propId 属性Id
	 * @return
     */
	BizOrderConnectsProp selectByPrimaryKey(Long propId);

	
	List<BizOrderConnectsProp> selectMemByBranchId(Long branchId);
	
	/**
	 * 查询交通接驳属性对象
	 * @param params
	 * @return
     */
	List<BizOrderConnectsProp> selectAllByParams(Map<String, Object> params);

	/**
	 * 保存
	 * @param bizOrderConnectsProp
	 * @return
     */
	Long insert(BizOrderConnectsProp bizOrderConnectsProp);

	/**
	 * 保存
	 * @param bizOrderConnectsProp
	 * @return
	 */
	Long insertSelective(BizOrderConnectsProp bizOrderConnectsProp);

	/**
	 * 更新
	 * @param bizOrderConnectsProp
	 * @return
	 */
	void updateByPrimaryKeySelective(BizOrderConnectsProp bizOrderConnectsProp);

	/**
	 * 更新
	 * @param bizOrderConnectsProp
	 * @return
	 */
	void updateByPrimaryKey(BizOrderConnectsProp bizOrderConnectsProp);

	/**
	 * 跟新
	 * @param propId
     */
	void deleteByPrimaryKey(long propId);
	
}