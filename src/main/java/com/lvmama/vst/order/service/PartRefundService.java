package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdPartRefundItem;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.FavorableInfo;
import com.lvmama.vst.comm.vo.order.OrdItemResidueRefundInfo;
import com.lvmama.vst.comm.vo.order.OrdRefundInfo;
import com.lvmama.vst.comm.vo.order.OrdRefundItemInfo;
import com.lvmama.vst.comm.vo.order.OrdRefundUpdateInfo;
import com.lvmama.vst.comm.vo.order.PartRefundAmountInfo;


/**
 * 门票部分退
 * @author taiqichao
 *
 */
public interface PartRefundService {	
	
	ResultHandleT<Boolean> canPartRefundFront(Long orderId);
	
	ResultHandleT<List<OrdRefundItemInfo>> queryOrdRefundInfo(Long orderId);
	
	ResultHandleT<List<OrdRefundItemInfo>> queryOrdRefundInfo(Long orderId, boolean isFront);
	
	ResultHandleT<Map<String,Object>> partRefundSubmit(OrdRefundInfo ordRefundInfo);

	ResultHandleT<Map<String,Object>> partRefundSubmit(OrdRefundInfo ordRefundInfo, boolean isFront);
	
	String partRefundAudit(OrdRefundInfo ordRefundInfo);
	
	/**
	 * 退款成功更新退款份数/人员
	 * @param orderId, OrdRefundItemInfo
	 * @return
	 */
	void updateRefundQuantityOrPerson(List<OrdRefundUpdateInfo> ordRefundUpdateInfos);
	
	/**
	 * 退款拒绝更新退款锁定份数/人员
	 * @param orderId, OrdRefundItemInfo
	 * @return
	 */
	void updateRefundLock(List<OrdRefundUpdateInfo> ordRefundUpdateInfos);
	
	/**
	 * 查询剩余可退份数/人员
	 * @param orderItemId 子订单ID
	 * @param partType 类型（ 剩余分数/剩余人数）
	 * @return
	 */
	ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPerson(Long orderItemId,String partType);
	
	/**
	 * 查询剩余可退份数/人员 （意外险）
	 * @param orderItemId 子订单ID
	 * @param partType 类型（ 剩余分数/剩余人数）
	 * @return
	 */
	ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPersonForAccidentInsurance(Long orderItemId);
	
	/**
	 * 查询剩余可退份数/人员（非意外险）
	 * @param orderItemId 子订单ID
	 * @param partType 类型（ 剩余分数/剩余人数）
	 * @return
	 */
	ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPersonForOtherInsurance(Long orderItemId);
	
	/**
	 * 计算部分退门票扣款退款金额
	 * @param ordRefundInfoVo
	 * @return
	 */
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmount(OrdRefundInfo ordRefundInfo,boolean isFront);
	
	/**
	 * @param ordRefundInfo
	 * @param isFront
	 * @return
	 */
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmountNew(OrdRefundInfo ordRefundInfo,boolean isFront);
	
	/**
	 * 部分退查询订单优惠信息
	 * @param orderId
	 * @return
	 */
	public ResultHandleT<FavorableInfo> queryPartRefundFavorableInfo(Long orderId);


	void updateOrdRefundBatch(Long refundApplyId, String status);

	void updateOrdRefundBatch(Long refundApplyId, Long orderItemId, String status);

	void updateAuditStatus(Long refundApplyId, String status);

	Boolean hasRefundApplyProcessing(Long orderId);

	/**
	 * 生成退款单时客服修改原退款份数时重新计算金额的方法
	 * @param ordRefundInfo
	 * @return
	 */
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmount(OrdRefundInfo ordRefundInfo);
	
	/**
	 * @param ordRefundInfo
	 * @return
	 */
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmountNew(OrdRefundInfo ordRefundInfo);

	void checkPartRefund(Long orderId, Long passSerialno, String status);

	Boolean hasSaleNotClosed(Long orderId);

	OrdPartRefundItem queryOrdPartRefundItem(Long orderItemId);
	
}
