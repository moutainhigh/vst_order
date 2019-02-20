package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.comm.vst.vo.CallBackRequestDto;
import com.lvmama.comm.vst.vo.CallBackResponseDto;
import com.lvmama.comm.vst.vo.VstTravellerCallBackRequest;
import com.lvmama.comm.vst.vo.VstTravellerCallBackResponseDto;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.comm.vo.order.Person;


/**
 * @author 张伟
 *
 */
public interface IOrdPersonService {

	
	public int addOrdPerson(OrdPerson ordPerson);
	
	public OrdPerson findOrdPersonById(Long id);
	
	public List<OrdPerson> findOrdPersonList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdPerson ordPerson);
	
	public List<OrdPerson> getOrderPersonListWithAddress(Long orderId,String personType);
	
	/**
	 * 写入一个发票的收件地址信息,如果该发票已经存在地址，即为修改发票地址
	 * @param person
	 * @param invoiceId
	 * @param operatorId
	 * @return
	 */
	boolean insertInvoicePerson(OrdPerson ordPerson,Long invoiceId,String operatorId);
	
	/**
	 * 	前台单个游玩人信息修改接口
	 * @param travellerRequest
	 * @return
	 */
	VstTravellerCallBackResponseDto updateTravellerPersonInfo(VstTravellerCallBackRequest travellerRequest); 
	
	/**
	 * 	目的地意外险后置前台单个游玩人信息校验接口
	 * @param travellerRequest
	 * @return
	 */
	VstTravellerCallBackResponseDto checkDestBuTravDelayPersonInfo(VstTravellerCallBackRequest travellerRequest);
	
	
	/**
	 * 前台批量修改游玩人，修改次数按照orderID做限制
	 * @param orderId
	 * @param personIds
	 * @return
	 */
	VstTravellerCallBackResponseDto updateAndSaveTravellerPersonInfo(VstTravellerCallBackRequest travellerRequest); 
	
	public List<OrdPerson> findOrdPerson(Long orderId, List<Long> personIds);

	
	public List<OrdPerson> selectLatestContactPerson(Map<String, Object> params);
	/**
	 * 查询批量修改游玩人防刷次数
	 * @param orderId
	 * @return
	 */
	public int queryListTraverllerCountByOrderId(Long orderId);
	
	public CallBackResponseDto updateOrderTravellerInfo(CallBackRequestDto requestDto);

	public List<OrdPerson> getBookPersonInfoByOrderId(Map<String, Object> var1);
}
