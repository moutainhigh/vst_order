package com.lvmama.vst.order.contract.service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;

/**
 * 
 * @author sunjian
 *
 */
public interface IOrderTravelContractDataService {
	public ResultHandleT<OutboundTourContractVO> captureOutboundTourContract(OrdOrder order);
	
	public ResultHandleT<CuriseProductVO> getCombCuriseProducatData(Long combCategoryId, Long combProductId) ;
	
	public String getAppendVersion(OrdTravelContract ordTravelContract);
}
