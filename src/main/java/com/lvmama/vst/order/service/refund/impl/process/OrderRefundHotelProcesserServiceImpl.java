package com.lvmama.vst.order.service.refund.impl.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.perm.PermRole;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkCertifClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_TYPE;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_TASK_STATUS;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.refund.IOrderRefundProcesserService;
import com.lvmama.vst.pet.adapter.PermUserRoleProxyAdapter;
/**
 * 单酒
 * @version 1.0
 */
@Service("orderRefundHotelProcesserService")
public class OrderRefundHotelProcesserServiceImpl implements IOrderRefundProcesserService {
	private static Logger LOG = LoggerFactory.getLogger(OrderRefundHotelProcesserServiceImpl.class);
	private static final String REFUND_ROLE_ID ="refund_roles";
	@Autowired
	private OrderService orderService;
	@Autowired
	private ProcesserClientService processerClientService;
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	@Autowired
	private EbkCertifClientService ebkCertifClientService;
	@Autowired
	private PermUserRoleProxyAdapter permUserRoleProxyService;
	@Autowired
	private OrderRefundComProcesserService orderRefundComProcesserService;
	
	@Override
	public void startProcesserByRefund(OrdOrder ordOrder ,Map<String, Object> params) {
		orderRefundComProcesserService.startProcesserByRefund(IOrderRefundProcesserService.REFUND_SINGLE_HOTEL_PROCESS_KEY
				, ordOrder.getOrderId(), params);
	}
	
	@Override
	public void updateOrderStatusToOrderRefund(OrdOrder order, Map<String, Object> params,Date applyDate) {
		if(order == null) return;
		LOG.info("orderId=" +order.getOrderId() +",hasPayed=" +order.hasPayed()
				+",hasNeedPrepaid=" +order.hasNeedPrepaid());
		//是否支付或是否预付
		if (!order.hasPayed() || !order.hasNeedPrepaid()) return;
		//暂不支持预售
		if(OrderEnum.ORDER_STAMP.STAMP.name().equals(order.getOrderSubType())
				 || OrderEnum.ORDER_STAMP.STAMP_PROD.name().equals(order.getOrderSubType())){
			return;
		}
		//验证凭证信息
		if (!validateCertif(order)) return;
		
		//在线退款工作流是否存在
		ComActivitiRelation comActiveRelation = comActivitiRelationService
				.queryRelation(IOrderRefundProcesserService.REFUND_SINGLE_HOTEL_PROCESS_KEY,
						order.getOrderId(),
						ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
		LOG.info("comActiveRelation：" + comActiveRelation);
		if (comActiveRelation != null)
			return;
		String processId = processerClientService
				.queryProcessIdByBusinessKey(ActivitiUtils
						.createOrderRefundBussinessKey(order));
		LOG.info("orderId:" + order.getOrderId() + ",processId:"
				+ processId);
		if (processId != null)
			return;
		startProcesserByRefund(order, params);
	}
	
	@Override
	public void completeTaskByOnlineRefundAudit(ComAudit comAudit) {
		orderRefundComProcesserService.completeTaskByOnlineRefundAudit(comAudit);
	}

	@Override
	public void completeTaskBySupplierConfirm(OrdOrder order, String supplierKey) {
		if(order== null) return;
		final String startKey="VST_" +supplierKey +"_SUPPLIER_CONFIRM_ORDER_"+order.getOrderId(); 
		LOG.info("ONLINE_REFUND_AUDIT startKey=" +startKey
				+",isCancel=" +order.isCancel());
		if(!order.isCancel()) return;
		
		if(ActivitiUtils.hasActivitiOrder(order)){
			try{
				if (SynchronizedLock.isOnDoingMemCached(startKey)) {
					LOG.info("并发发起操作");
					return ;
				}
				ActivitiKey activitiKey =new ActivitiKey((String)null, ActivitiUtils.createOrderRefundBussinessKey(order));
				processerClientService.completeTask(activitiKey, supplierKey, null);
			}catch(Exception ex){
				ex.printStackTrace();
				LOG.error("completeTaskBySupplierConfirm failure", ex);
			}finally{
				SynchronizedLock.release(startKey);
			}
		}
		
	}
	
	/**
	 * validateCertif
	 */
	private boolean validateCertif(OrdOrder order){
		//fax,不通过
		OrdOrderItem ordOrderItem = order.getMainOrderItem();
		if(ordOrderItem == null) {
			LOG.info("ordOrderItem is null,orderId=" +order.getOrderId());
			return false;
		}
		if("Y".equalsIgnoreCase(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name()))){
			return false;
		}
		//ebk,新订接受,不通过
		Map<String, Object> params=new HashMap<String, Object>();
        params.put("orderId",order.getOrderId());
        params.put("certifType", EBK_CERTIFICATE_TYPE.CONFIRM.name());
        params.put("certifStatus", EBK_TASK_STATUS.ACCEPT.name());
        List<EbkCertif> ebkCertifs = ebkCertifClientService.findEbkCertifListByMap(params).getReturnContent();
        
        return CollectionUtils.isEmpty(ebkCertifs);
	}

	@Override
	public boolean isStartProcessByRefund(OrdOrder order, String operateName) {
		try {
			//获取退款角色列表
			List<Long> list =getRefundRoleList();
			LOG.info("orderId=" +order.getOrderId() +",list：" +list);
			if(CollectionUtils.isEmpty(list)) return false;
			
			//目的地事业部->客服部->资源审核组(1062-默认分单,1427-分销组分单)
			ResultHandleT<List<PermRole>> result = permUserRoleProxyService.getPermRoleByUserno(operateName);
			LOG.info("orderId=" +order.getOrderId() +",operateName=" +operateName
					+",roleSize=" +(result.getReturnContent()==null?null:result.getReturnContent().size()));
			if(!result.isSuccess()) return false;
			boolean isPermission =false;
			for(PermRole role :result.getReturnContent()){
				if(list.contains(role.getRoleId())){
					isPermission =true;
					break;
				}
			}
			LOG.info("orderId=" +order.getOrderId() +",isPermission=" +isPermission);
			return isPermission;
			
		} catch (Exception e) {
			LOG.error("orderId="+ order.getOrderId() +",error:",e);
			return false;
		}
	}

	/**
	 * 加载退款角色列表
	 */
	private List<Long> getRefundRoleList(){
		List<Long> list =new ArrayList<Long>();
		String roles =Constant.getInstance().getProperty(REFUND_ROLE_ID);//1062L
		if(StringUtils.isNotBlank(roles)){
			for(String id: roles.split(",")){
				list.add(Long.valueOf(id));
			}
		}
		return list;
	}

	@Override
	public void updateOrderStatusToOrderRefund(OrdOrder order,
			Map<String, Object> params, Date applyDate, String operateName) {
		try {
			if(isStartProcessByRefund(order,operateName)){
				updateOrderStatusToOrderRefund(order, params, applyDate);
			}
		} catch (Exception e) {
			LOG.error("orderId="+ order.getOrderId() +",error:",e);
		}
		
	}
}
