package com.lvmama.vst.order.snapshot.service.impl;

import com.lvmama.order.snapshot.api.service.ISnapshotClientService;
import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.util.OrdSnapshotUtils;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import com.lvmama.vst.order.snapshot.service.IVstSnapshotService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class VstSnapshotServiceImpl implements IVstSnapshotService {
	private static final Log LOG = LogFactory.getLog(VstSnapshotServiceImpl.class);
	@Resource
	private ISnapshotClientService snapshotClientService;

	@Override
	public void orderSnapshot_prodProduct(OrdOrder ordOrder, ProdProduct prodProduct) {
		ResponseBody<String> responseBody =snapshotClientService.orderSnapshot_prodProduct(ordOrder.getContent()
				, SnapshotParamFactory.convertSnapshotProdProduct(prodProduct));

		LOG.info(OrdSnapshotUtils.formatLog("prodProductId-"+prodProduct.getProductId()
				, OrdSnapshotUtils.SNAPSHOT_LOG, responseBody.getT()));
		if(responseBody.isSuccess() && responseBody.getT() !=null){
			ordOrder.setContent(responseBody.getT());
		}
	}
	@Override
	public void orderSnapshot_prodProduct(OrdOrderItem ordOrderItem, ProdProduct prodProduct) {
		ResponseBody<String> responseBody =snapshotClientService.orderItemSnapshot_prodProduct(ordOrderItem.getContent()
				, SnapshotParamFactory.convertSnapshotProdProduct(prodProduct));

		LOG.info(OrdSnapshotUtils.formatLog("prodProductId-"+prodProduct.getProductId()
				, OrdSnapshotUtils.SNAPSHOT_ITEM_LOG, responseBody.getT()));
		if(responseBody.isSuccess() && responseBody.getT() !=null){
			ordOrderItem.setContent(responseBody.getT());
		}
	}
	@Override
	public void orderSnapshot_prodProductBranch(OrdOrderItem ordOrderItem, ProdProductBranch prodProductBranch) {
		ResponseBody<String> responseBody =snapshotClientService.orderItemSnapshot_prodProductBranch(ordOrderItem.getContent()
				, SnapshotParamFactory.convertSnapshotProdProductBranch(prodProductBranch));

		LOG.info(OrdSnapshotUtils.formatLog("productBranchId-"+prodProductBranch.getProductBranchId()
				, OrdSnapshotUtils.SNAPSHOT_ITEM_LOG, responseBody.getT()));
		if(responseBody.isSuccess() && responseBody.getT() !=null){
			ordOrderItem.setContent(responseBody.getT());
		}
	}
	@Override
	public void orderSnapshot_suppSuppGoods(OrdOrderItem ordOrderItem, SuppGoods suppGoods) {
		ResponseBody<String> responseBody =snapshotClientService.orderItemSnapshot_suppSuppGoods(ordOrderItem.getContent()
				, SnapshotParamFactory.convertSnapshotSuppGoods(suppGoods));

		LOG.info(OrdSnapshotUtils.formatLog("suppSuppGoodsId-"+suppGoods.getSuppGoodsId()
				, OrdSnapshotUtils.SNAPSHOT_ITEM_LOG, responseBody.getT()));
		if(responseBody.isSuccess() && responseBody.getT() !=null){
			ordOrderItem.setContent(responseBody.getT());
		}
	}
}
