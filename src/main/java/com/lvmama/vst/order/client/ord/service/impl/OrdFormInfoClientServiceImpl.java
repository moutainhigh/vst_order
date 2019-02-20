package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.client.ord.po.OrdFormInfoQueryPO;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.comm.vo.ResultHandleT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdFormInfoClientService;
import com.lvmama.vst.order.service.IOrdFormInfoService;
@Component("ordFormInfoServiceRemote")
public class OrdFormInfoClientServiceImpl implements OrdFormInfoClientService {

	private static final Log log = LogFactory.getLog(OrdFormInfoClientServiceImpl.class);

	@Autowired
	private IOrdFormInfoService ordFormInfoService;

	@Override
	public Map<String, String> findFrontBusStop(Long orderId) {
		return ordFormInfoService.findFrontBusStop(orderId);
	}

	@Override
	public String findVisaDocLastDate(Long orderId) {
		
		return ordFormInfoService.findVisaDocLastTime(orderId);
	}

	/**
	 * 根据订单id查询ord_form_info表中的记录
	 *
	 * @param ordFormInfoQueryPO
	 */
	@Override
	public ResultHandleT<List<OrdFormInfo>> findOrdFormInfoList(OrdFormInfoQueryPO ordFormInfoQueryPO) {
		ResultHandleT<List<OrdFormInfo>> resultHandleT = new ResultHandleT<List<OrdFormInfo>>();
		try {
			List<OrdFormInfo> ordFormInfoList = ordFormInfoService.findOrdFormInfoList(ordFormInfoQueryPO);
			resultHandleT.setReturnContent(ordFormInfoList);
		} catch (Exception e){
			resultHandleT.setMsg(e.getMessage());
			log.error("Error occurs while load data from table ord_form_info ", e);
		}
		return resultHandleT;
	}


}
