package com.lvmama.vst.neworder.order.api.impl;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.order.utils.EnumUtilsEx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.order.api.base.vo.BusinessException;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.comm.utils.ResponseBodyCreator;
import com.lvmama.order.comm.utils.StringUtil;
import com.lvmama.order.comm.validation.ApiValidation;
import com.lvmama.order.enums.ApiEnum;
import com.lvmama.order.vst.api.common.service.IApiDestOrderWorkflowService;
import com.lvmama.order.vst.api.common.vo.request.MarkTaskVo;
import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_OBJECT_TYPE;

/** 
 * @ImplementProject vst_order
 * @Description 
 * @author chenlizhao
 * @date 2018年2月27日 下午3:16:45
 */
@Component("apiDestOrderWorkflowService")
public class ApiDestOrderWorkflowServiceImpl implements IApiDestOrderWorkflowService {

	private static final Log LOG = LogFactory.getLog(ApiDestOrderWorkflowServiceImpl.class);
	
	@Autowired
	private DestOrderWorkflowService destOrderWorkflowService;
	
	/* (non-Javadoc)
	 * @see com.lvmama.order.vst.api.common.service.IApiDestOrderWorkflowService#markTaskValid(com.lvmama.order.api.base.vo.RequestBody)
	 */
	@Override
	public ResponseBody markTaskValid(RequestBody<MarkTaskVo> request) {
		try {
			LOG.info("markTaskValid with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "标志未处理活动失败,所传参数为空!");
	        }
			//检查标记活动对象
			MarkTaskVo vo = request.getT();
			if(vo.getObjectId() == null || vo.getObjectType() == null) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "标志未处理活动失败,所传标记活动对象的ID或者Type为空!");
			}
			
			destOrderWorkflowService.markTaskValid(vo.getObjectId(), AUDIT_OBJECT_TYPE.valueOf(vo.getObjectType().name()));
			
			return ResponseBodyCreator.success(null);
		} catch (Exception ex) {
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "标志未处理活动内部错误", ex, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER_ITEM.name());
		}
	}

	@Override
	public ResponseBody<String> getAuditTypeByConfirmStatus(Long orderItemId) {
		String confirmStatus=destOrderWorkflowService.getAuditTypeByConfirmStatus(orderItemId);
		LOG.info("getAuditTypeByConfirmStatus orderItemId="+orderItemId+",confirmStatus="+confirmStatus);
		ResponseBody<String> res=new ResponseBody<>();
		res.setT(confirmStatus);
		return res;
	}

	@Override
	public void createTaskLog(Long orderId, Long orderItemId, String confirmAuditType, String operator) {
		Confirm_Enum.CONFIRM_AUDIT_TYPE object_type=EnumUtilsEx.getEnum(Confirm_Enum.CONFIRM_AUDIT_TYPE.class,confirmAuditType);
		destOrderWorkflowService.createTaskLog(orderId,orderItemId,object_type,operator);
	}

}
