package com.lvmama.vst.order.contract.service;

import com.lvmama.vst.comm.vo.ResultHandleT;

/**
 * 
 * 电子合同接口扩展
 *
 */
public interface IOrderElectricService extends IOrderElectricContactService{

	
	/**
	 * 预览合同产品信息填充
	 * @param productId
	 * @return
	 */
	public ResultHandleT<String> getContractTemplateHtml(Long productId);
	
}
