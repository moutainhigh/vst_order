package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.comm.vst.vo.VstInvoiceAmountVo;
import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.json.ResultHandle;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.order.utils.InvoiceResult;

public interface IOrdInvoiceService {

	int deleteByPrimaryKey(Long ordInvoiceId);
	
	int insert(OrdInvoice record);

	int insertSelective(OrdInvoice record);

	OrdInvoice selectByPrimaryKey(Long ordInvoiceId);

	int updateByPrimaryKeySelective(OrdInvoice record);

	int updateByPrimaryKey(OrdInvoice record);
	
	public Long getInvoiceAmountSum(Map<String,Object> map);
	
	public long getSumCompensationAndRefundment(Long ordInvoiceId);
	
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String,Object> param);
	
	InvoiceResult update(String status, Long invoiceId, String operatorId);
	
	void updateRedFlag(Long invoiceId,String redFlag,String operatorId);
	
	void updateInvoiceExpressNo(Long invoiceId,String expressNo,String operatorId);
	
	void updateInvoiceNo(Long invoiceId, String invoiceNo, String operatorId);
	
	/**
     * 调用第三方接口（百旺）开具发票
     * @param invoiceIdsList
     */
    ResultHandle issueInvoice(final List<String> invoiceIdsList, String operator);
    
   /**
     * 调用第三方接口（百旺）重新打印发票
     * @param invoiceIdsList
     */
    ResultHandle reprintInvoice(final List<String> invoiceIdsList);
    
	/**
	 * 新增OrdInvoice.
	 */
	OrdInvoice insert(Pair<OrdInvoice,OrdPerson> invoice, List<OrdOrder> orderIds,String operatorId);
	
	/**
	 * 添加多张发票
	 */
	void insert(List<Pair<OrdInvoice,OrdPerson>> invoices,Long orderId,String operatorId);
	
	/**
	 * 综合条件 查询发票红冲信息
	 * @param condition
	 * @return
	 */
	//public List<OrdInvoice> queryOrdInvoice(final ComplexQuerySQLCondition condition);
	
	public List<OrdInvoice> getOrdInvoiceListByOrderId(Map<String,Object> param);
	
	//申请红冲
	public InvoiceResult updateInvoiceRedFlag(Long invoiceId, String redFlag, String operatorId);
	
	//更新红冲
	public int updateRedFlag(OrdInvoice ordInvoice,String operatorId);
	
	//根据条件查询总条数
	public Long getInvoiceCount(Map<String,Object> param);
	
	//根据状态查看发票信息
	public List<OrdInvoice> getStatusOrdInvoiceListByParam(Map<String, Object> param);

	public List<OrdInvoice> getOrdInvoiceListByParam2(Map<String, Object> param);
	
	//根据订单id集合查询信息
	public List<OrdInvoice> getOrdInvoiceListByOrderIdList(List<Long> orderIds);
	
	/**
	 * 根据订单id获取vst订单可开发票金额,单位为分
	 * @param orderId
	 * @return
	 */
	public VstInvoiceAmountVo getInvoiceAmount(Long orderId);
}
