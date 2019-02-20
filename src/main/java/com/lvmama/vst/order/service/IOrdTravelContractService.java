package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.po.ComFileMap;


/**
 * @author 张伟
 *
 */
public interface IOrdTravelContractService {

	
	public int addOrdTravelContract(OrdTravelContract ordTravelContract);
	
	public OrdTravelContract findOrdTravelContractById(Long id);
	
	public List<OrdTravelContract> findOrdTravelContractList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdTravelContract ordTravelContract);
	
	public int saveOrdTravelContract(OrdTravelContract ordTravelContract, String operatorName);

	public int updateByPrimaryKeySelective(OrdTravelContract ordTravelContract, String operatorName);

	public int deleteByPrimaryKey(Long contractId, String operatorName);
	
	public int deleteByPrimaryKey(OrdTravelContract ordTravelContract, String operatorName);
	
	/**
	 * 根据订单id修改合同状态
	 * @param params
	 * @return
	 */
	public int updateContractStatusByOrderId(Map<String, Object> params);
	
	/**
	 * 根据合同附件名查询附件ID
	 * @param fileName
	 * @return
	 */
	public ComFileMap getComFileMapByFileName(String fileName);
	/**
	 * 
	 * @param id
	 * @return
	 */
	public int updateSendEmailFlag(Set<Long> ids);
	
	/**
	 * 查询需要进行金棕榈同步的相关合同数据
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findPushDataByList(
			Map<String, Object> params);
	
	/**
	 * 根据合同ID更新金棕榈同步相关数据
	 * @param params
	 * @return
	 */
	public int updatePushDataByContractId(Map<String, Object> params);
}
