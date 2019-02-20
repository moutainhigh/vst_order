package com.lvmama.vst.order.snapshot.async.impl;

import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductVo;
import com.lvmama.order.snapshot.comm.util.OrdSnapshotUtils;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.order.snapshot.recoup.MessageRecoupSnapshotService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.snapshot.async.IVstMsgRecoupSnapshotService;
import com.lvmama.vst.order.snapshot.enums.VstSnapshotEnum;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 消息
 */
@Service("vstMsgRecoupSnapshotService")
public class VstMsgRecoupSnapshotServiceImpl implements IVstMsgRecoupSnapshotService {
	private static final Log LOG = LogFactory.getLog(VstMsgRecoupSnapshotServiceImpl.class);
	@Resource
	private MessageRecoupSnapshotService messageRecoupSnapshotService;

	@Override
	public ResultHandle recoupProdProduct(OrdOrder ordOrder) throws Exception {
		ResultHandle resultHandle =new ResultHandle();
		//初始化主单参数
		OrderParamVo ordOrderParam =SnapshotParamFactory.convertOrderParamVo(ordOrder);
		ordOrderParam.setGatewayClassService(VstSnapshotEnum.GATEWAY_CLASSS_ERVICE.VST_PRODUCT_ORDER.getServiceName());
		ProdProductVo prodProductVo =SnapshotParamFactory.convertSnapshotProdProduct(null);
		//主单快照
		ordOrderParam.setContent(null);
		ResponseBody<String> responseBody =messageRecoupSnapshotService.orderMongoRecoupSnapshot(ordOrderParam
				,Snapshot_Detail_Enum.SERVICE_KEY.productService.name(),prodProductVo);

		LOG.info(OrdSnapshotUtils.formatLog("ordOrderId-"+ordOrder.getOrderId(), OrdSnapshotUtils.SNAPSHOT_LOG, responseBody.getT()));
		//TODO 持久化mongodb

		//子单快照
		OrderItemParamVo orderItemParamVo =null;
		for(OrdOrderItem ordOrderItem :ordOrder.getOrderItemList()){
			//初始化子单参数
			ordOrderParam =SnapshotParamFactory.convertOrderParamVo(ordOrder);
			orderItemParamVo =SnapshotParamFactory.convertOrdOrderItem(ordOrderItem);
			orderItemParamVo.setGatewayClassService(VstSnapshotEnum.GATEWAY_CLASSS_ERVICE.VST_PRODUCT_ORDER_ITEM.getServiceName());
			prodProductVo =SnapshotParamFactory.convertSnapshotProdProduct(null);

			ordOrderItem.setContent(null);
			orderItemParamVo.setContent(null);
			responseBody =messageRecoupSnapshotService.orderItemMongoRecoupSnapshot(orderItemParamVo
					,Snapshot_Detail_Enum.SERVICE_KEY.productService.name(),prodProductVo);

			LOG.info(OrdSnapshotUtils.formatLog("ordOrderId-"+ordOrder.getOrderId(), OrdSnapshotUtils.SNAPSHOT_ITEM_LOG, responseBody.getT()));
			//TODO 持久化mongodb

			ordOrderParam =null;
			orderItemParamVo =null;
			prodProductVo =null;
			responseBody =null;
		}
		return resultHandle;
	}

	@Override
	public ResultHandle recoupSuppGoods(OrdOrder ordOrder) throws Exception {
		return null;
	}


}
