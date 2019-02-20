package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.comm.vst.vo.CallBackRequestDto;
import com.lvmama.comm.vst.vo.CallBackResponseDto;
import com.lvmama.comm.vst.vo.VstTravellerCallBackRequest;
import com.lvmama.comm.vst.vo.VstTravellerCallBackResponseDto;
import com.lvmama.vst.back.client.ord.service.OrdPersonClientService;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.order.service.IOrdAddressService;
import com.lvmama.vst.order.service.IOrdPersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("ordPersonServiceRemote")
public class OrdPersonClientServiceImpl implements OrdPersonClientService {
	private static final Log logger = LogFactory.getLog(OrdPersonClientServiceImpl.class);

	@Autowired
	private IOrdPersonService ordPersonService;

	@Autowired
	private IOrdAddressService ordAddressService;

	@Override
	public int addOrdPerson(OrdPerson ordPerson) {
		return ordPersonService.addOrdPerson(ordPerson);
	}

	@Override
	public OrdPerson findOrdPersonById(Long id) {
		return ordPersonService.findOrdPersonById(id);
	}

	@Override
	public List<OrdPerson> findOrdPersonList(Map<String, Object> params) {
		return ordPersonService.findOrdPersonList(params);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdPerson ordPerson) {
		return ordPersonService.updateByPrimaryKeySelective(ordPerson);
	}

	@Override
	public List<OrdPerson> getOrderPersonListWithAddress(Long orderId,String personType) {
		return ordPersonService.getOrderPersonListWithAddress(orderId, personType);
	}

	@Override
	public VstTravellerCallBackResponseDto updateTravellerPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {
		return ordPersonService.updateTravellerPersonInfo(travellerRequest);
	}
	
	@Override
	public CallBackResponseDto updateOrderTravellerInfo(CallBackRequestDto requestDto) {
		return ordPersonService.updateOrderTravellerInfo(requestDto);
	}
}
