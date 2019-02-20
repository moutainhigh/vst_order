package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.ComAuditSortRule;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * 项目名称：vst_order
 * 类名称：IOrderAuditSortRuleService
 * 类描述：订单排序规则业务接口
 * 创建人：majunli
 * 创建时间：2016-10-15 上午10:25:48
 * 修改人：majunli
 * 修改时间：2016-10-15 上午10:25:48
 * 修改备注：
 */
public interface IOrderAuditSortRuleService {
	
	public int saveComAuditSortRule(ComAuditSortRule comAuditSortRule);
	
	public int saveComAuditSortRuleSelective(ComAuditSortRule comAuditSortRule);
	
	public int updateComAuditSortRuleByPrimaryKeySelective(ComAuditSortRule comAuditSortRule);
	
	public int updateComAuditSortRuleByPrimaryKey(ComAuditSortRule comAuditSortRule);
	
	public ComAuditSortRule selectComAuditSortRuleByPrimaryKey(Long sortRuleId);
	
	/**
	 * map动态查询列表
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAuditSortRule> queryComAuditSortRuleListByParam(Map<String, Object> param);
	
	/**
	 * map动态查询总数
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param);
	
	
	/**
	 * 根据订单查询排序规则
	 * @param ordOrderItem
	 * @return
	 * @author majunli
	 * @date 2016-10-20 上午11:42:36
	 */
	public ComAuditSortRule getComAuditSortRuleByOrderItem(OrdOrderItem ordOrderItem);
	public ComAuditSortRule getComAuditSortRuleByOrderItemByJob(OrdOrderItem ordOrderItem, Date nowDate);

}
