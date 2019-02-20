package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.order.po.OrdPartRefundItem;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.order.service.PartRefundService;
import com.lvmama.vst.ticket.service.PartRefundClientService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("partRefundServiceRemote")
public class PartRefundClientServiceImpl implements PartRefundClientService {

	private static final Log LOG = LogFactory.getLog(PartRefundClientServiceImpl.class);
	
	@Autowired
	private PartRefundService partRefundservice;

	@Override
	public ResultHandleT<List<OrdRefundItemInfo>> queryOrdRefundInfo(Long orderId, boolean isFront) {
		return partRefundservice.queryOrdRefundInfo(orderId, isFront);
	}

	@Override
	public ResultHandleT<Map<String,Object>> partRefundSubmit(OrdRefundInfo ordRefundInfo) {
		return partRefundservice.partRefundSubmit(ordRefundInfo, true);
		
	}


	@Override
	public ResultHandleT<Boolean> canPartRefundFront(Long orderId) {
		return partRefundservice.canPartRefundFront(orderId);
	}

	@Override
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmount(OrdRefundInfo ordRefundInfo,Boolean isFront) {
		return partRefundservice.queryPartRefundAmount(ordRefundInfo,isFront);
	}

	@Override
	public ResultHandleT<FavorableInfo> queryPartRefundFavorableInfo(
			Long orderId) {
		return partRefundservice.queryPartRefundFavorableInfo(orderId);
	}
	
	@Override
	public void updateOrdRefundBatch(Long refundApplyId,String status){

		partRefundservice.updateOrdRefundBatch(refundApplyId, status);
		
	}

	@Override
	public void updateRefundQuantityOrPerson(List<OrdRefundUpdateInfo> ordRefundUpdateInfos) {
		partRefundservice.updateRefundQuantityOrPerson(ordRefundUpdateInfos);
		
	}

	@Override
	public void updateRefundLock(List<OrdRefundUpdateInfo> ordRefundUpdateInfos) {
		partRefundservice.updateRefundLock(ordRefundUpdateInfos);
		
	}

	@Deprecated
	@Override
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmount(
			OrdRefundInfo ordRefundInfo) {
		return partRefundservice.calRefundmentAmount(ordRefundInfo);
	}
	
	@Override
	public OrdPartRefundItem queryOrdPartRefundItem(
			Long orderItemId) {
		return partRefundservice.queryOrdPartRefundItem(orderItemId);
	}

	@Override
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmountNew(OrdRefundInfo ordRefundInfo, Boolean isFront) {
		return partRefundservice.queryPartRefundAmountNew(ordRefundInfo, isFront);
	}

	@Override
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmountNew(OrdRefundInfo ordRefundInfo) {
		return partRefundservice.calRefundmentAmountNew(ordRefundInfo);
	}
	

	
}
