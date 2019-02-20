package com.lvmama.vst.order.client.ord.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdOrderDisneyInfoQueryService;
import com.lvmama.vst.back.order.po.OrdOrderDisneyInfo;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.service.IOrdOrderDisneyInfoService;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;

@Component("ordOrderDisneyInfoQueryService")
public class OrdOrderDisneyInfoQueryServiceImpl implements
		OrdOrderDisneyInfoQueryService {
	@Autowired
	private IOrdOrderDisneyInfoService ordOrderDisneyInfoService;
	@Autowired
	SupplierOrderOtherService  supplierOrderOtherService;
	@Autowired
	private OrdPassCodeDao ordPassCodeDao;

	@Override
	public String queryDisneyInfoByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		List<OrdOrderDisneyInfo> list=ordOrderDisneyInfoService.findByParams(params);
		if(list ==null ||list.size()<0){
			return null;
		}else{
			OrdOrderDisneyInfo temp=list.get(0);
			return StringUtils.isEmpty(temp.getContent()) ? null:temp.getContent();
		}
	}

	@Override
	public String queryDisneyShowTime(Long orderId,Long orderItemId) {
		String showTimeStr ="";
		OrdPassCode ordPassCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItemId);
		if(ordPassCode!=null){
			String passSerialNo = ordPassCode.getPassSerialno();
			String ticketStr = supplierOrderOtherService.getOrderTickets(passSerialNo);
			if(StringUtil.isNotEmptyString(ticketStr)){
				JSONObject	newRst = JSONObject.fromObject(ticketStr);
				showTimeStr=newRst.getString("showtime");
			}
		}
		
		if(StringUtil.isEmptyString(showTimeStr)){
			String disneyInfo=queryDisneyInfoByOrderId(orderId);
			List<JSONObject> seats=new ArrayList<JSONObject>();
			if(StringUtil.isNotEmptyString(disneyInfo)){
				JSONObject	newRst = JSONObject.fromObject(disneyInfo);
				if(disneyInfo.contains("VisitTime")){
					showTimeStr=newRst.getString("VisitTime");
				}
			}
		}
		return showTimeStr;
	}
	


}
