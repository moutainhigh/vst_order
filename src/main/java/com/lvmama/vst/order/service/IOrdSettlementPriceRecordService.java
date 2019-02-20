package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItemExtend;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;

import java.util.HashMap;
import java.util.List;


/**
 * 
 * 订单结算价修改业务
 * 
 * @author YungHua.Ma
 *
 */
public interface IOrdSettlementPriceRecordService {
	
	public int insert(OrdSettlementPriceRecord ordSettlementPriceRecord);
	
	public OrdSettlementPriceRecord selectByPrimaryKey(Long id);
	
	public int updateByPrimaryKeySelective(OrdSettlementPriceRecord ordSettlementPriceRecord);
	
	public int updateByPrimaryKey(OrdSettlementPriceRecord ordSettlementPriceRecord);
	
	public List<OrdSettlementPriceRecord> findOrdSettlementPriceRecordList(HashMap<String,Object> params);
	
	public Integer findOrdSettlementPriceRecordCounts(HashMap<String,Object> params);
	
	/**保存修改买断结算价的修改记录
	 * @param ordSettlementPriceRecord
	 * @param orderId
	 * @return
	 */
	public int saveBuyoutMultiPriceAfterApprove(Long newBuyoutTotalPrice,OrdSettlementPriceRecord ordSettlementPriceRecord,Long orderId,String priceModel);
	
	public int saveAfterApprove(OrdSettlementPriceRecord oldOrdSettlementPriceRecord,OrdSettlementPriceRecord ordSettlementPriceRecord,Long orderId);
	
	public Integer saveReservationForSettlementAmountChange(OrdSettlementPriceRecord newOrdSettlementPriceRecord, String assignor,String content) throws Exception ;
	
	public void updateNewSettlementPrice(OrdOrderItem ordOrderItem, Long newUpdateActualSettlementPrice,
			String updatePriceType) ;
	
	/**
	 * 对于多单价的子订单，根据结算总价反推结算单价.
	 * @param orderItemId Order Item ID
	 * @param totalPrice 结算总价
	 * @return 结算单价记录
	 */
	List<OrdMulPriceRate> calcSettlementUnitPrice (Long orderItemId, Long totalPrice);

	/**
	 * 对于多单价的子订单，根据结算总价反推结算单价.
	 * @param orderItemId Order Item ID
	 * @param totalPrice 结算总价
	 * @param oriTotalPrice 老结算总价
	 * @return 结算单价记录
	 */
	List<OrdMulPriceRate> calcSettlementUnitPrice (Long orderItemId, Long totalPrice, Long totalSettlementPrice);
	
	List<OrdMulPriceRate> calcBuyoutSettlementUnitPrice (Long orderItemId, Long totalPrice);
	
	/**
	 * 更新子订单，包括子订单的结算总价和结算单价.
	 * @param orderItem Order Item
	 * @param totalPrice 结算总价
	 * @return 返回子订单记录
	 */
	OrdOrderItem resetOrderItem4Settlement (OrdOrderItem orderItem, Long totalPrice);
	
	/**
	 * 修改结算总价修改历史记录入库，价格变更记录入库.
	 * 
	 * @param priceRecords
	 * @return
	 */
	boolean addSettlementTotalPrice(List<OrdSettlementPriceRecord> priceRecords, Long orderId);
	
	/**
	 * 修改多价格表，修改结算总价的时候调用.
	 * 
	 * @param ordMulPriceRates
	 */
	public void updateMulPriceRates(List<OrdMulPriceRate> ordMulPriceRates);

	/**
	 * 保存外币结算价调用
	 * @param ordSettlementPriceRecord
	 * @param orderItemExtend
	 * @return
	 */
	int saveForeignAfterApprove(OrdSettlementPriceRecord ordSettlementPriceRecord, OrdOrderItemExtend orderItemExtend);

	/**
	 * 仅用在子单更新结算价存在拓展表记录了人民币的情况同步更新掉拓展表结算价字段
	 * @param orderItem
	 * @return
	 */
	int saveExtendPrice(OrdOrderItem orderItem);
}
