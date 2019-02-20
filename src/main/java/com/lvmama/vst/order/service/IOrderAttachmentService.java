package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.pub.po.ComLog;

/**
 * 订单附件业务接口
 * @author wenzhengtao
 *
 */
public interface IOrderAttachmentService {
	/**
	 * 新增附件记录
	 * 
	 * @param orderAttachment
	 */
	void saveOrderAttachment(OrderAttachment orderAttachment,String operatorName,String memo);
	/**
	 * 新增一个附件记录并记录日志
	 * @param orderAttachment
	 */
	void saveOrderAttachment(OrderAttachment orderAttachment,ComLog comLog);
	/**
	 * 根据订单ID查询附件记录
	 * 
	 * @param orderId
	 * @return
	 */
	List<OrderAttachment> queryOrderAttachment(Long orderId);
	/**
	 * 根据订单ID统计附件数量
	 * @param orderId
	 * @return
	 */
	int countOrderAttachment(Long orderId);
	
	List<OrderAttachment> findOrderAttachmentByCondition(Map<String, Object> params);
	
	int countOrderAttachmentByCondition(Map<String, Object> params);
	
	/**
	 * @desc 订单附件状态修改
	 * @param param
	 * @return
	 */
	int updateOrderAttachmentFlag(Map<String,Object> param);
	
}
