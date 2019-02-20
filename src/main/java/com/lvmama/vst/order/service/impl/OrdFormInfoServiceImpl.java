package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.client.ord.po.OrdFormInfoQueryPO;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.order.dao.OrdFormInfoDao;
import com.lvmama.vst.order.service.IOrdFormInfoService;
@Service
public class OrdFormInfoServiceImpl implements IOrdFormInfoService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrdFormInfoServiceImpl.class);
	
	@Autowired
	private OrdFormInfoDao ordFormInfoDao;

	@Override
	public Map<String, String> findFrontBusStop(Long orderId) {
		Map<String,String> resultMap=new HashMap<String, String>();
		Map<String, Object> parametersOrdFormInfo = new HashMap<String, Object>();
		parametersOrdFormInfo.put("orderId",orderId);
		parametersOrdFormInfo.put("contentType",BuyInfoAddition.frontBusStop.name());
		List<OrdFormInfo> ordFormInfoList=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
		String frontBusStop=null;
		if (CollectionUtils.isNotEmpty(ordFormInfoList)) {
			OrdFormInfo ordFormInfo=ordFormInfoList.get(0);
			frontBusStop=ordFormInfo.getContent();
		}
		parametersOrdFormInfo.put("contentType",BuyInfoAddition.backBusStop.name());
		List<OrdFormInfo> ordFormInfoListBack=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
		String backBusStop=null;
		if (CollectionUtils.isNotEmpty(ordFormInfoListBack)) {
			OrdFormInfo ordFormInfoBack=ordFormInfoListBack.get(0);
			backBusStop=ordFormInfoBack.getContent();
		}
		resultMap.put("frontBusStop", frontBusStop);
		resultMap.put("backBusStop", backBusStop);
		return resultMap;			
	}

	@Override
	public String findVisaDocLastTime(Long orderId) {
		Map<String, Object> parametersOrdFormInfo = new HashMap<String, Object>();
		parametersOrdFormInfo.put("orderId",orderId);
		parametersOrdFormInfo.put("contentType",BuyInfoAddition.visaDocLastTime.name());
		List<OrdFormInfo> ordFormInfoList=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
		String visaDocLastTime=null;
		if (CollectionUtils.isNotEmpty(ordFormInfoList)) {
			OrdFormInfo ordFormInfo=ordFormInfoList.get(0);
			visaDocLastTime=ordFormInfo.getContent();
		}
		return visaDocLastTime;
		
	}

	@Override
	public void updateVisaDocLastDate(Long orderId, String LastDate) {
		if(orderId!=null && LastDate!=null){
			Map<String, Object> parametersOrdFormInfo = new HashMap<String, Object>();
			parametersOrdFormInfo.put("orderId",orderId);
			parametersOrdFormInfo.put("contentType",BuyInfoAddition.visaDocLastTime.name());
			parametersOrdFormInfo.put("content", LastDate);
			this.ordFormInfoDao.updateContentByPrimaryKey(parametersOrdFormInfo);
			
		}
	}

	/**
	 * 根据订单id查询ord_form_info表中的记录
	 *
	 * @param ordFormInfoQueryPO: 查询po，orderId必须有合法值，否则返回空
	 */
	@Override
	public List<OrdFormInfo> findOrdFormInfoList(OrdFormInfoQueryPO ordFormInfoQueryPO) {
		if(ordFormInfoQueryPO == null || ordFormInfoQueryPO.getOrderId() == null || ordFormInfoQueryPO.getOrderId() < 0L){
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		if (ordFormInfoQueryPO.getContentTypeEnum() != null) {
			params.put("contentType", ordFormInfoQueryPO.getContentTypeEnum().getContentType());
		}
		params.put("orderId", ordFormInfoQueryPO.getOrderId());
		return ordFormInfoDao.findOrdFormInfoList(params);
	}


}
