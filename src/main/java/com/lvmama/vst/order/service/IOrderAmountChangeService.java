package com.lvmama.vst.order.service;

import com.lvmama.comm.tnt.po.OrderPrice;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * 订单审核业务
 * 
 * @author YungHua.Ma
 *
 */
public interface IOrderAmountChangeService {
	
	public int insert(OrdAmountChange ordAmountChange);
	
	public OrdAmountChange selectByPrimaryKey(Long ordAmountChangeId);
	
	public int updateByPrimaryKeySelective(OrdAmountChange ordAmountChange);
	
	public int updateByPrimaryKey(OrdAmountChange ordAmountChange);
	
	public List<OrdAmountChange> findOrdAmountChangeList(HashMap<String, Object> params);

	public Integer findOrdAmountChangeCounts(HashMap<String, Object> params);

	public Integer isOrderApproving(Long orderId);

	public Integer updateOrderAmount(OrdAmountChange ordAmountChange, OrdOrder ordOrder);

	public List<OrdOrderAmountItem> findOrderAmountItemList(Map<String, Object> params);


	public Integer saveReservationForOrderAmountChang(OrdAmountChange ordAmountChange, String assignor, String messge) throws Exception;

	public Integer queryApprovingRecords(Long orderId);

	void updateOrderAmount(OrdAmountChange oldOrdAmountChange, OrderPrice orderPrice);

	/**
	 * 根据金额类型获取订单的总金额，返回结果包含符号(正负号)
	 * */
	String getOrderAmountItemByType(OrdOrder order, String amountType);

	/**
	 * 获取所有主单上已经验证通过的改价信息
	 * */
	List<OrdAmountChange> queryOrderPassedAmountChangeList(Long orderId);

	/**
	 * 批量获取所有主单上已经验证通过的改价信息
	 * */
	List<OrdAmountChange> queryOrderPassedAmountChangeList(List<Long> orderIdList);
}
