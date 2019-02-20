package com.lvmama.vst.order.contract.service;

import java.io.File;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;

public interface IOrderNoticeRegimentService {
	/**
	 * 根据OrdOrder生成出团通知书，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle saveNoticeRegiment(TravelContractVO travelContractVo, String operatorName);
	
	public Map<String,Object> captureContract(TravelContractVO travelContractVo,OrdOrder order,File directioryFile);
}
