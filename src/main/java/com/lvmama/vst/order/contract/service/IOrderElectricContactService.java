package com.lvmama.vst.order.contract.service;

import java.io.File;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;

public interface IOrderElectricContactService {
	/**
	 * 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName);
	
	
	/**
	 *更新合同 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle updateTravelContact(OutboundTourContractVO contractVO , String operatorName);
	
	
	public ResultHandle sendEcontractWithEmail(OrdTravelContract ordTravelContract) ;
		
	
	
	public Map<String,Object> captureContract(OrdTravelContract ordTravelContract,OrdOrder order,File directioryFile);
	
	public ResultHandle sendOrderEcontractEmail(OrdOrder order,String opterator) ;

	
	public ResultHandle sendContractEmail(OrdOrder order,Long contractId,String opterator) ;
	
	public void insertOrderLog(final Long orderId, Long contractId,String operatorName,String content, String memo);
	
	public ResultHandleT<String> getContractTemplateHtml();
	
	/**
	 *更新合同 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO, OrdOrder order ,OrdTravelContract ordTravelContract,String operatorName);
	
}
