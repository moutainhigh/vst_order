package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdPaymentInfo;


/**
 * @author 张伟
 *
 */
public interface IOrdPaymentInfoService {
	/**
	 * 添加支付记录信息
	 * @param ordPaymentInfo
	 * @return
	 */
	public int addOrdPaymentInfo(OrdPaymentInfo ordPaymentInfo);

	/**
	 * 根据条件查询支付记录信息
	 * @param params
	 * @return
	 */
	public List<OrdPaymentInfo> findOrdPaymentInfoList(
			Map<String, Object> params);
}
