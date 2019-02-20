package com.lvmama.vst.order.snapshot.async.impl;

import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.order.snapshot.recoup.UrlRecoupSnapshotService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.snapshot.enums.VstSnapshotEnum;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import com.lvmama.vst.order.snapshot.async.IVstUrlRecoupSnapshotService;
import com.lvmama.vst.order.snapshot.web.OrderSnapshotAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * URL
 */
@Service("vstUrlRecoupSnapshotService")
public class VstUrlRecoupSnapshotServiceImpl implements IVstUrlRecoupSnapshotService {
	private static final Log LOG = LogFactory.getLog(VstUrlRecoupSnapshotServiceImpl.class);
	@Resource
	private IOrderUpdateService orderUpdateService;
	@Resource
	private UrlRecoupSnapshotService urlRecoupSnapshotService;

	@Override
	public ResultHandle orderRecoupKeyByObject(OrdOrder ordOrder, String key, Object object) throws Exception {
		ResultHandle resultHandle =new ResultHandle();
		//主单快照
		ResponseBody<String> responseBody =urlRecoupSnapshotService.orderRecoupKeyByObject(
				SnapshotParamFactory.convertOrderParamVo(ordOrder), key, object);
		if(!checkResponseBody(responseBody, resultHandle)){
			return resultHandle;
		}
		//更新
		ordOrder.setContent(responseBody.getT());
		updateContentById(ordOrder);

		return resultHandle;
	}
	@Override
	public ResultHandle orderItemRecoupKeyByObject(OrdOrderItem ordOrderItem, String key, Object object) throws Exception {
		ResultHandle resultHandle =new ResultHandle();
		//子单快照
		ResponseBody<String> responseBody =urlRecoupSnapshotService.orderIemRecoupByObject(
				SnapshotParamFactory.convertOrdOrderItem(ordOrderItem), key, object);
		if(!checkResponseBody(responseBody, resultHandle)){
			return resultHandle;
		}
		//更新
		ordOrderItem.setContent(responseBody.getT());
		orderUpdateService.updateContentById(ordOrderItem);


		return resultHandle;
	}
	@Override
	public ResultHandle orderMongoRecoupKey(OrdOrder ordOrder, String key, Object object) throws Exception {
		ResultHandle resultHandle =new ResultHandle();
		//主单快照
		OrderParamVo orderParamVo =SnapshotParamFactory.convertOrderParamVo(ordOrder);
		orderParamVo.setGatewayClassService(VstSnapshotEnum.GATEWAY_CLASSS_ERVICE.VST_PRODUCT_ORDER.getServiceName());
		ResponseBody<String> responseBody =urlRecoupSnapshotService.orderMongoRecoupByObject(orderParamVo, key, object);
		if(!checkResponseBody(responseBody, resultHandle)){
			return resultHandle;
		}
		//暂存oracle,后续改成mongodb
		ordOrder.setContent(responseBody.getT());
		updateContentById(ordOrder);

		return resultHandle;
	}
	@Override
	public ResultHandle orderItemmongoRecoupKey(OrdOrderItem ordOrderItem, String key, Object object) throws Exception {
		ResultHandle resultHandle =new ResultHandle();
		OrderItemParamVo orderItemParamVo =SnapshotParamFactory.convertOrdOrderItem(ordOrderItem);
		orderItemParamVo.setGatewayClassService(VstSnapshotEnum.GATEWAY_CLASSS_ERVICE.VST_PRODUCT_ORDER_ITEM.getServiceName());
		ResponseBody<String> responseBody =urlRecoupSnapshotService.orderIemMongoRecoupByObject(orderItemParamVo, key, object);
		LOG.info("orderItemId=" +orderItemParamVo.getOrderItemId()
				+",key=" +key+",object=" +object
				+",responseBody=" +responseBody);
		if(!checkResponseBody(responseBody, resultHandle)){
			return resultHandle;
		}
		//暂存oracle,后续改成mongodb
		ordOrderItem.setContent(responseBody.getT());
		updateContentById(ordOrderItem);

		return resultHandle;
	}
	/**
	 * updateContentById
	 * @param ordOrder
	 */
	private void updateContentById(OrdOrder ordOrder){
		OrdOrder upOrdOrder =new OrdOrder();
		upOrdOrder.setOrderId(ordOrder.getOrderId());
		upOrdOrder.setContent(ordOrder.getContent());
		orderUpdateService.updateContentById(upOrdOrder);
	}
	private void updateContentById(OrdOrderItem ordOrderItem){
		OrdOrderItem upOrderItem = new OrdOrderItem();
		upOrderItem.setOrderItemId(ordOrderItem.getOrderItemId());
		upOrderItem.setContent(ordOrderItem.getContent());
		orderUpdateService.updateContentById(upOrderItem);
	}
	/**
	 * checkResponseBody
	 */
	private boolean checkResponseBody(ResponseBody<String> responseBody, ResultHandle resultHandle){
		if(responseBody.isFailure() || responseBody.getT() ==null){
			resultHandle.setMsg(responseBody.getMessage() +"@"+responseBody.getErrorMessage());
			return false;
		}
		return true;
	}
}
