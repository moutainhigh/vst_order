package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdRemarkLog;
import com.lvmama.vst.comm.web.BusinessException;

/**
 * 订单详情页备注
 * @author zhaomingzhu
 *
 */
public interface IOrdRemarkLogService {
	
	/**
	 * 订单详情页备注数量
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public int findOrdRemarkLogCount(Map<String, Object> params) throws BusinessException;
	/**
	 * 订单详情页备注列表
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List<OrdRemarkLog> findOrdRemarkLogList(Map<String, Object> params) throws BusinessException;
	/**
	 * 订单详情页备注
	 * @param logId
	 * @return
	 * @throws BusinessException
	 */
	public OrdRemarkLog findOrdRemarkLogById(Long logId) throws BusinessException;
	/**
	 * 新增订单详情页备注
	 * @param OrdRemarkLog
	 * @return
	 * @throws BusinessException
	 */
	public int addOrdRemarkLog(OrdRemarkLog ordRemarkLog) throws BusinessException;
	/**
	 * 修改订单详情页备注
	 * @param OrdRemarkLog
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdRemarkLog(OrdRemarkLog ordRemarkLog) throws BusinessException;
	/**
	 * 修改订单详情页备注状态
	 * @param OrdRemarkLog
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdRemarkLogStatus(OrdRemarkLog ordRemarkLog) throws BusinessException;
	/**
	 * 删除订单详情页备注
	 * @param logId
	 * @return
	 * @throws BusinessException
	 */
	public int deleteOrdRemarkLog(Long logId) throws BusinessException;

}
