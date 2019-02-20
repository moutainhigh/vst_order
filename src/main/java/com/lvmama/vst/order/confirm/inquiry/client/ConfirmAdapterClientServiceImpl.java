package com.lvmama.vst.order.confirm.inquiry.client;

import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.vst.back.client.ord.service.OrderConfirmClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.vo.ConfirmParamVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL;
import com.lvmama.vst.order.client.service.ConfirmAdapterClientService;
import com.lvmama.vst.order.confirm.inquiry.enums.OrderInquiryEnum.API_ORDER_OPERATE_TYPE;
import com.lvmama.vst.order.confirm.inquiry.service.NewOrderConfirmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 新询单系统路由接口
 * @author kemeisong
 */
@Component("confirmAdapterServiceRemote")
public class ConfirmAdapterClientServiceImpl implements ConfirmAdapterClientService {
	private static final Logger LOG = LoggerFactory.getLogger(ConfirmAdapterClientServiceImpl.class);

	@Autowired
	private IOrderRouteService orderRouteService;
    @Autowired
    private OrderConfirmClientService confirmClientService;

    @Autowired
    private NewOrderConfirmService newOrderConfirmService;

	@Override
	public ResultHandleT<ComAudit> createConfirmOrder(
			ConfirmParamVo confirmParamVo) {
		ResultHandleT<ComAudit> resultHandle =new ResultHandleT<ComAudit>();
		OrdOrderItem orderItem =confirmParamVo.getOrderItem();
		//询单灰度开关
		boolean isRouteToInquirySys=orderRouteService.isRouteToInquirySys(orderItem.getCategoryId(), API_ORDER_OPERATE_TYPE.CREATE.getOperateCode(),orderItem.getSupplierId());
		LOG.info("confirmAdapterServiceRemote createConfirmOrder orderItemId:"+orderItem.getOrderItemId()+",categoryId:"+orderItem.getCategoryId()+", isRouteToInquirySys:"+isRouteToInquirySys);
		//走询单系统新接口
		if(isRouteToInquirySys){
			try {
				resultHandle=newOrderConfirmService.createConfirmOrder(confirmParamVo);
			} catch (Exception e) {
				LOG.error(orderItem.getOrderItemId()+"", e);
				resultHandle.setMsg(e.getMessage());
			}
		}else{//走vst_back老逻辑
			resultHandle =confirmClientService.createConfirmOrder(confirmParamVo);
		}
		return resultHandle;
	}

	@Override
	public ResultHandle updateSupplierProcess(OrdOrderItem orderItem,
			CONFIRM_STATUS newStatus, String supplierNo, String operator,
			Long linkId, EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) {
		ResultHandle result =new ResultHandle();
		if (orderItem == null) {
			result.setMsg("orderItem is null");
			return result;
		}
		//询单灰度开关
		boolean isRouteToInquirySys=orderRouteService.isRouteToInquirySys(orderItem.getCategoryId(),API_ORDER_OPERATE_TYPE.CERTIF_AUDIT.getOperateCode(),orderItem.getSupplierId());
		LOG.info("confirmAdapterServiceRemote  updateSupplierProcess orderItemId:"+orderItem.getOrderItemId()+",categoryId:"+orderItem.getCategoryId()+", isRouteToInquirySys:"+isRouteToInquirySys);
		//走询单系统新接口
		if(isRouteToInquirySys){
			return newOrderConfirmService.updateSupplierProcess(orderItem, newStatus, supplierNo, operator, linkId, confirmChannel);		
		}else{//走vst_back老逻辑
			return confirmClientService.updateSupplierProcess(orderItem, newStatus, supplierNo, operator, linkId, confirmChannel);		
		}
	}

}
