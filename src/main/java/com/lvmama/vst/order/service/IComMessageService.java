package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.web.BusinessException;


/**
 * @author 张伟
 *
 */
public interface IComMessageService {

	
	public int addComMessage(ComMessage comMessage);
	
	public ComMessage findComMessageById(Long id);
	
	public List<ComMessage> findComMessageList(Map<String, Object> params);

	public int findComMessageCount(Map<String, Object> params);

	public int updateComMessage(ComMessage comMessage);


	public int updateReservationListProcessed(String orderId,String[] messageIds,String[] auditIdArray,String assignor,String memo);

	/**
	 * 新增预定通知（主订单订单详情页面）
	 * @param comMessage
	 */
	public int saveReservation(ComMessage comMessage,String auditType,Long orderId,String assignor,String memo)throws BusinessException;

	/**
	 * 新增预定通知（酒店子单订单详情页面）
	 * @param comMessage
	 */
	public int saveChildReservation(ComMessage comMessage,String auditType,Long orderId, Long orderItemId, String assignor,String memo)throws BusinessException;

	/**
	 * 新增预定通知（子订单订单详情页面）
	 * @param comMessage
	 */
	public int saveReservationChildOrder(ComMessage comMessage,String auditType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException;
	
	
	/**
	 * 订单取消成功后产生预定通知
	 * @param orderId
	 * @param loginUserId
	 */
	public int saveReservationAfterCan(Long orderId, String loginUserId);
	/**
	 * 关房后取消订单成功生成通知
	 * 
	 * @param orderId
	 * @param loginUserId
	 */
	public int savaReservationAfterCalOfCloseHourse(Long orderId,String loginUserId);
	
	/**
	 * 新增预定通知（主订单订单详情页面）增加了auditSubType
	 * @param comMessage
	 */
	public int saveReservation(ComMessage comMessage,String auditType,String auditSubType,Long orderId,String assignor,String memo)throws BusinessException;
	
	/**
	 * 新增预定通知（子订单订单详情页面）增加了auditSubType
	 * @param comMessage
	 */
	public int saveReservationChildOrder(ComMessage comMessage,String auditType,String auditSubType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException;
	
	public ComAudit newReservationChildOrder(ComMessage comMessage,String auditType,String auditSubType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException;
	
	
	/**
	 * 新增主订单预定通知 分单对象，主订单负责人
	 * @param comMessage
	 * @throws Exception 
	 */
	public int saveReservationOrder(Long orderId,String auditSubType,String assignor,String memo,boolean bigTrafficValidate) throws Exception;
	public int saveReservationOrderNew(Long orderId,String auditSubType,String assignor,String memo,boolean bigTrafficValidate) throws Exception;
	/**
	 * 批量预定通知的处理
	 * @param comAuditList 审核信息列表
	 * @param assignor 操作人
	 * @param memo 备注
	 * @return
	 */
	public int updateBatchReservationListProcessed(List<ComAudit> comAuditList,String assignor, String memo);
	/**
	 * 预定通知的处理
	 * 一.如果处理的为主订单则
	 * 		1.查找相关通知信息
	 * 		2.更新后台任务消息信息和审核信息的壮态
	 * 		3.写操作业务日志
	 * 二.如果处理的为子订单则
	 * 		1.在子订单表中根据子订单id查找相关记录
	 * 		2.查找相关通知信息
	 * 		3.更新后台任务消息信息和审核信息的壮态
	 * 		4.写操作业务日志
	 * @param comAudit 审核信息
	 * @param assignor 操作人
	 * @param memo 备注
	 * @return
	 */
	public int updateReservationListProcessed(ComAudit comAudit,String assignor, String memo);
}
