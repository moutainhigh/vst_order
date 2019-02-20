package com.lvmama.vst.order.service.impl;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.passport.po.PassProvider;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.confirm.inquiry.enums.OrderInquiryEnum.API_ORDER_OPERATE_TYPE;
import com.lvmama.vst.order.confirm.inquiry.service.NewOrderConfirmService;
import com.lvmama.vst.order.redis.JedisTemplate2;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.supp.client.service.SupplierOrderService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 
 * @author sunjian
 *
 */
@Component
public class SupplierOrderOperatorImpl implements ISupplierOrderOperator {
	private static final Log LOG = LogFactory
			.getLog(SupplierOrderOperatorImpl.class);

	// H000997 - 未知异常
	private static final String ERROR_CODE_H000997 = "H000997";

	// H000998 - 请求参数异常
	private static final String ERROR_CODE_H000998 = "H000998";

	// H100999 - 系统异常
	private static final String ERROR_CODE_H000999 = "H000999";

	// H001085 - 底层提交订单异常
	private static final String ERROR_CODE_H001085 = "H001085";

	@Resource(name = "supplierOrderService")
	private SupplierOrderService supplierOrderService;

	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	private JedisTemplate2 jedis;
	@Autowired
	private IOrderRouteService orderRouteService;
    @Autowired
    private NewOrderConfirmService newOrderConfirmService;
	@Autowired
	private IOrder2RouteService order2RouteService;

	@Override
	public OrderSupplierOperateResult createSupplierOrder(Long orderId) {
		OrderSupplierOperateResult result = new OrderSupplierOperateResult();
		createSupplierOrder(orderId, null);
		return result;
	}
	
	@Override
	public OrderSupplierOperateResult createSupplierOrder(Long orderId, boolean isFromComJob) {
		OrderSupplierOperateResult result = new OrderSupplierOperateResult();
		createSupplierOrder(orderId, null, isFromComJob);
		return result;
	}
	
	/**
	 * 过滤不需要调用创建供应商订单的子订单
	 * @param order
	 * @param orderItemList
	 * @param isFromComJob
	 */
	private void filterNoNeedItems(OrdOrder order, Set<Long> orderItemList, boolean isFromComJob) {
		if(order == null || order.getOrderItemList() == null) {
			LOG.error("订单为空或该订单无子订单");
			return;
		}
		if (orderItemList != null) {
			for (Iterator<OrdOrderItem> it = order.getOrderItemList()
					.iterator(); it.hasNext();) {
				OrdOrderItem item = it.next();
				if (!orderItemList.contains(item.getOrderItemId())) {
					LOG.info("==112===orderId:" + order.getOrderId()+",it.orderitemId:"+item.getOrderItemId());
					it.remove();
				}
			}
		}
		LOG.info("==116===orderId:" + order.getOrderId() + ",order.itemlist size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());
		//如果不是从ComJob调用的，则过滤掉已经调用过创建供应商订单的子订单
		if(!isFromComJob) {
			for (Iterator<OrdOrderItem> it = order.getOrderItemList()
					.iterator(); it.hasNext();) {
				OrdOrderItem item = it.next();
				if (OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(item.getInvokeInterfacePfStatus())) {
					it.remove();
				}
			}
		}
		LOG.info("==127===orderId:" + order.getOrderId() + ",order.itemlist size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());
		//如果是目的地，酒+景、酒套餐，意外险后置订单，只有在游玩人补全（后置状态为完成）下，才会去创建意外险供应商订单
        filterUnCompletedAccInsOrderItem(order);
	}
	
	/**
	 * 校验重复调用
	 * @param order
	 * @return
	 */
	private String checkDuplicateInvoke(OrdOrder order) {
		if(order == null || order.getOrderItemList() == null) {
			LOG.error("订单为空或该订单无子订单");
			return null;
		}
		
		for (Iterator<OrdOrderItem> it = order.getOrderItemList()
				.iterator(); it.hasNext();) {
			OrdOrderItem item = it.next();
			String key = "CREATE_SUPPLIER_ORDER_" + item.getOrderItemId();
			
			if(MemcachedUtil.getInstance().keyExists(key)) {
				return "重复调用创建供应商订单!";
			}
		}
		
		for (Iterator<OrdOrderItem> it = order.getOrderItemList()
				.iterator(); it.hasNext();) {
			OrdOrderItem item = it.next();
			String key = "CREATE_SUPPLIER_ORDER_" + item.getOrderItemId();
			
			MemcachedUtil.getInstance().set(key, 30, 1);
		}
		
		return null;
	}
	
	/**
	 * 更新订单的调用状态
	 * @param order
	 */
	private void updateInvokeStatus(OrdOrder order) {
		if(order == null || order.getOrderItemList() == null) {
			LOG.error("订单为空或该订单无子订单");
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (Iterator<OrdOrderItem> it = order.getOrderItemList()
				.iterator(); it.hasNext();) {
			OrdOrderItem item = it.next();
			item.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name());
			if(sb.length() > 0) {
				sb.append(",");
			}
			
			sb.append(item.getOrderItemId());
		}
		
		//更新调用状态
		if(sb.length() > 0) {
			orderUpdateService.updateItemInvokeInterfacePfStatus(sb.toString(), OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name());
		}
		
		String orderInvokeInterfacePfStatus = OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name();
		
		List<OrdOrderItem> ordItemList = orderUpdateService.queryOrderItemByOrderId(order.getOrderId());
		
		for (Iterator<OrdOrderItem> it = ordItemList
				.iterator(); it.hasNext();) {
			OrdOrderItem item = it.next();
			if(item.isSupplierOrderItem() && !OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(item.getInvokeInterfacePfStatus())) {
				orderInvokeInterfacePfStatus = OrderEnum.INVOKE_INTERFACE_PF_STATUS.PART_CREATED.name();
				break;
			}
		}
		
		//更新调用状态
		orderUpdateService.updateInvokeInterfacePfStatus(order.getOrderId(), orderInvokeInterfacePfStatus);
	}
	
	
	public List<SuppOrderResult>  createSupplierOrderToInquiry(OrdOrder order) {
		List<OrdOrderItem> orderItemList=order.getOrderItemList();
		if(orderItemList.size()<1){
			return null;
		}
		OrdOrder inquiryOrder =new OrdOrder();
		List<OrdOrderItem> inquiryItemList=new ArrayList<OrdOrderItem>();
		EnhanceBeanUtils.copyProperties(order,inquiryOrder);
		for (Iterator<OrdOrderItem> it = order.getOrderItemList().iterator(); it.hasNext();) {
			OrdOrderItem item = it.next();
			//询单灰度开关
			boolean isRouteToInquirySys=orderRouteService.isRouteToInquirySys(item.getCategoryId(),API_ORDER_OPERATE_TYPE.CREATE.getOperateCode(),item.getSupplierId());
			boolean ord2Route=order2RouteService.isOrderItemRouteToNewSys(item.getOrderItemId());
			LOG.info("createSupplierOrderToInquiry orderId:"+order.getOrderId()+",orderitemId:"+item.getOrderItemId()+",isRouteToInquirySys:"+isRouteToInquirySys+",ord2Route:"+ord2Route);
			if(isRouteToInquirySys||ord2Route){
				inquiryItemList.add(item);
				it.remove();
			}
		}
		if(inquiryItemList.size()<1){
			return null;
		}
		inquiryOrder.setOrderItemList(inquiryItemList);
		LOG.info("inquiry supplierOrderCreate,orderId:"+order.getOrderId()+",orderItemList size:"+inquiryOrder.getOrderItemList().size());
		return newOrderConfirmService.createSupplierOrder(inquiryOrder)	;	
	}
	
	

	@Override
	public OrderSupplierOperateResult createSupplierOrder(Long orderId,
			Set<Long> orderItemList, boolean isFromComJob) {
		LOG.info("开始创建供应商订单[orderId:" + orderId + "]");
		OrderSupplierOperateResult result = new OrderSupplierOperateResult();
		// 获取供应商订单对象图
		OrdOrder order = getSupplierOrderByOrderId(orderId);
		
		if(order == null) {
			LOG.error("订单对象为空");
			return result;
		}
		filterInsurePerson(order);
		LOG.info("==before===orderId:" + orderId + ",order.itemlist size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());
		//过滤不需要调用创建供应商订单的子订单
		filterNoNeedItems(order, orderItemList, isFromComJob);
		LOG.info("=====orderId:" + orderId + ",orderitem list size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());		
		result.setVstOrder(order);
		// 如果订单已经取消则无需创建供应商订单
		if (order.isCancel()) {
			LOG.error("订单已经取消,无法创建供应商订单!");
			result.setErrMsg("订单已经取消,无法创建供应商订单!");
			return result;
		}
		
		//校验重复调用
		/*if(!isFromComJob) {
			String duplicateInvokeMsg = checkDuplicateInvoke(order);
			if(duplicateInvokeMsg != null) {
				LOG.error("重复调用创建供应商订单!");
				result.setErrMsg("重复调用创建供应商订单!");
				return result;
			}
		}*/
		
		List<OrdOrderItem> toCreateOrderItemList = order.getOrderItemList();
		LOG.info("To sent supplierOrderItem orderId = " + orderId);
		for (OrdOrderItem ordOrderItem : toCreateOrderItemList) {
		    LOG.info("orderItemId = " + ordOrderItem.getOrderItemId());
        }
		
		//------------------过滤掉部分走新询单系统的订单子项--------------
		List<SuppOrderResult> inquiryResultList=createSupplierOrderToInquiry(order);
		
		//若redis服务挂了，不影响程序执行 modify by wujian 2017-09-01 begin
		String key="LOCK_CREATE_SUPPLIER_ORDER_"+orderId.toString();
		try{
			jedis=JedisTemplate2.getReaderInstance();
			if(!jedis.setnxex(key,"lock", 30)){
				LOG.info("获取锁"+key+"失败,重复调用创建供应商订单!");
				result.setErrMsg("获取锁失败,重复调用创建供应商订单!");
				return result;
			}else{
				LOG.info("获取锁"+key+"成功");
			}
		}catch(Exception e){
			LOG.error("获取锁"+key+"异常",e);
		}
		
		
		//1.捕获异常是为了如果发生业务异常立马释放锁，让别的线程使用
		//2.锁超时时间30秒
		try {
			LOG.info("===248==orderId:" + orderId + ",orderitem list size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());
			LOG.info("batchNo:" + order.getBatchNo());
			// 对返回结果处理：成功：失败
			List<SuppOrderResult> suppOrderResultList = supplierOrderService
					.createOrder(order);
			LOG.info("===after==orderId:" + orderId + ",orderitem list size:"+order.getOrderItemList()==null?"null":order.getOrderItemList().size());
			//更新订单的调用状态
			updateInvokeStatus(order);	
			if(suppOrderResultList==null){
				suppOrderResultList =new ArrayList<SuppOrderResult>();
			}
			if(inquiryResultList!=null&&inquiryResultList.size()>0){
				suppOrderResultList.addAll(inquiryResultList);
			}
			if(suppOrderResultList == null) {
				LOG.error("远程调用返回null值");
				result.setErrMsg("远程调用返回null值");
				result.setRetry(false);
				releaseLock(key);//释放key
				return result;
			}
			
			if(suppOrderResultList.size() <= 0) {
				LOG.info("远程调用返回结果记录个数为" + suppOrderResultList.size());
				result.setErrMsg("远程调用返回结果记录个数为" + suppOrderResultList.size());
				result.setRetry(false);
				releaseLock(key);//释放key
				return result;
			}
			
			for (SuppOrderResult suppOrderResult : suppOrderResultList) {
				if(suppOrderResult == null) {
					LOG.info("远程调用返回结果存在空记录");
					result.setErrMsg("远程调用返回结果存在空记录");
					result.setRetry(false);
					break;
				}
				
				LOG.info("返回结果，是否成功：" + suppOrderResult.isSuccess()
						+ "[orderId=" + suppOrderResult.getOrdOrderId()
						+ ",orderItemId=" + suppOrderResult.getOrderItemId()
						+ "],errMsg:" + suppOrderResult.getErrMsg());
				
				if (!suppOrderResult.isSuccess()) {
					String errorMsg = (suppOrderResult.getErrMsg() == null ? ""
							: suppOrderResult.getErrMsg().trim());
					result.setErrMsg(errorMsg);
					if (isContainRetryCode(errorMsg)) {
						result.setRetry(true);
					} else {
						result.setRetry(false);
					}
					break;
				}
			}
			if(result.isSuccess()){
				result.setSuppOrderResultList(suppOrderResultList);
			}
			releaseLock(key);//释放key
		} catch (RemoteAccessException ex) {
			LOG.error("远程调用异常:[orderId="
					+ order.getOrderId() + "]," + ex.getMessage());
			result.setErrMsg("远程调用异常");
			result.setRetry(true);
			LOG.info("因为远程调用异常释放key:"+key);
			releaseLock(key);//释放key
		}catch(Exception e){
			//对这类异常没有处理，遵循原先的代码，原路返回
			LOG.error("创建供应商订单异常",e);
			releaseLock(key);//释放key
			LOG.info("因为创建供应商订单异常释放key:"+key);
			throw new RuntimeException(e.getMessage());
		}
		// modify by wujian 2017-09-01 end
		return result;
	
	}
	
	
	/** 
	 * @Title: filterInsurePerson 
	 * @Description: 筛选被保人
	 * @param order
	 * @return: void
	 * @author lijunshuai 2018-09-11
	 */
	private void filterInsurePerson(OrdOrder order) {
		if(order != null && order.getOrdPersonList() != null) {
			for (OrdPerson person : order.getOrdPersonList()) {
				if(OrderEnum.ORDER_PERSON_TYPE.INSURER.name().equalsIgnoreCase(person.getPersonType())) {
					order.getOrdInsurerList().add(person);
				}
			}
		}
	}
	
	@Override
	public OrderSupplierOperateResult createSupplierOrder(Long orderId,
			Set<Long> orderItemList) {
		/*if (orderItemList == null || orderItemList.isEmpty()) {
			OrderSupplierOperateResult result = new OrderSupplierOperateResult();
			result.setErrMsg("子订单ID集合为空");
			return result;
		}*/
		return createSupplierOrder(orderId, orderItemList, false);
	}

	@Override
	public OrderSupplierOperateResult cancelSupplierOrder(Long orderId) {
		OrderSupplierOperateResult result = new OrderSupplierOperateResult();
		// 获取艺龙订单，内部直填充订单子项
		OrdOrder order = getSupplierOrderWithItemByOrderId(orderId);
		if (order != null) {
			LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: [orderId="
					+ order.getOrderId() + "] get success.");
			result.setVstOrder(order);
			String key = "CANCEL_SUPPLIER_ORDER_" + order.getOrderId();
			
			if(MemcachedUtil.getInstance().keyExists(key)) {
				result.setErrMsg("重复调用取消供应商订单!");
				return result;
			} else {
				//超过30s，重新计数
				MemcachedUtil.getInstance().set(key, 30, 1);
			}
			// xiaorui add 调用 释放库存接口 不论成功与否 不影响 下面操作
//			try {
//				for (OrdOrderItem  orderItem : order.getOrderItemList()){
//					if(DisneyUtils.isDisney(orderItem)){
//						supplierOrderOtherService.freeSuppOderStock(order.getOrderId());
//						break;
//					}	
//				}
//			} catch (Exception e) {
//				LOG.info("supplierOrderOtherService.freeSuppOderStock: [orderId="
//						+ order.getOrderId() + "] get failed." + e.getMessage());
//			}
			
			try {
				List<SuppOrderResult> suppOrderResultList = supplierOrderService
						.cancelOrder(order);
				
				//更新调用状态
				StringBuffer sb = new StringBuffer();
				for (Iterator<OrdOrderItem> it = order.getOrderItemList()
						.iterator(); it.hasNext();) {
					OrdOrderItem item = it.next();
					item.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
					if(sb.length() > 0) {
						sb.append(",");
					}
					
					sb.append(item.getOrderItemId());
				}
				
				//更新调用状态
				if(sb.length() > 0) {
					orderUpdateService.updateItemInvokeInterfacePfStatus(sb.toString(), OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
				}
				orderUpdateService.updateInvokeInterfacePfStatus(orderId, OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
				
				
				if (suppOrderResultList != null) {
					if (suppOrderResultList.size() > 0) {
						SuppOrderResult orderResult = null;
						for (int i = 0; i < suppOrderResultList.size(); i++) {
							orderResult = suppOrderResultList.get(i);
							if (orderResult != null) {
								if (!orderResult.isSuccess()) {
									LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel fail:[orderId="
											+ orderResult.getOrdOrderId()
											+ ",orderItmeId="
											+ orderResult.getOrderItemId()
											+ "],errMsg:"
											+ orderResult.getErrMsg());
									String errorMsg = orderResult.getErrMsg()
											.trim();
									if (!isCancledCode(errorMsg)) {
										result.setErrMsg(errorMsg);
										if (isContainRetryCode(errorMsg)) {
											result.setRetry(true);
										} else {
											result.setRetry(false);
										}
										break;
									}
								} else {
									LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel success:[orderId="
											+ orderResult.getOrdOrderId()
											+ ",orderItmeId="
											+ orderResult.getOrderItemId());
								}
							} else {
								LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel fail:[orderId="
										+ order.getOrderId()
										+ "],SuppOrderResult is null.");
								result.setErrMsg("远程调用返回结果存在空记录");
								result.setRetry(false);
								break;
							}
						}
					} else {
						LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel fail:List<SuppOrderResult> size is "
								+ suppOrderResultList.size());
						result.setErrMsg("远程调用返回结果记录个数为"
								+ suppOrderResultList.size());
						result.setRetry(false);
					}
				} else {
					LOG.error("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel return list null");
					result.setErrMsg("远程调用返回null值");
					result.setRetry(false);
				}
			} catch (RemoteAccessException ex) {
				LOG.info("SupplierOrderOperatorImpl.cancelSupplierOrder: supplierOrderService.hotelOrderCancel fail:[orderId="
						+ order.getOrderId() + "]," + ex.getMessage());
				result.setErrMsg("远程调用异常");
				result.setRetry(true);
			}

		}

		return result;
	}

	/**
	 * 获取供应商订单的对象图
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getSupplierOrderByOrderId(Long orderId) {
		OrdOrder supplierOrder = null;
		OrdOrder order = null;
		order = getOrderWithOrderItemByOrderId(orderId);
		// 检验供应商
		if ((order != null && order.isSupplierOrder())||order2RouteService.isOrderRouteToNewSys(orderId)) {
			// 深度检索Order对象图
			order = getOrderWithOjbectDiagramByOrderId(orderId);
			if (order != null) {
				LOG.info("SupplierOrderOperatorImpl.getSupplierOrderByOrderId: getOrderWithOjbectDiagramByOrderId,orderList.size");
				return order;
			} else {
				LOG.info("SupplierOrderOperatorImpl.getSupplierOrderByOrderId: getOrderWithOjbectDiagramByOrderId return null. orderId="
						+ orderId);
			}
		}

		return supplierOrder;
	}

	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);

		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);

		List<OrdOrder> orderList = complexQueryService
				.queryOrderListByCondition(condition);
		if (orderList != null) {
			LOG.info("SupplierOrderOperatorImpl.getOrderWithOrderItemByOrderId: queryOrderListByCondition,orderList.size="
					+ orderList.size());
			if (orderList.size() == 1) {
				order = orderList.get(0);
			}
		} else {
			LOG.info("SupplierOrderOperatorImpl.getOrderWithOrderItemByOrderId: queryOrderListByCondition return null. orderId="
					+ orderId);
		}

		return order;
	}

	/**
	 * 根据OrderId获取整个Order对象图
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOjbectDiagramByOrderId(Long orderId) {

		return complexQueryService.queryOrderByOrderId(orderId);
	}

	/**
	 * 错误信息是否包含重发类型错误代码
	 * 
	 * @param errorMsg
	 * @return
	 */
	private boolean isContainRetryCode(String errorMsg) {
		boolean isContain = false;
		LOG.info("SupplierOrderOperatorImpl.isContainRetryCode,errorMsg="
				+ errorMsg);
		if (errorMsg != null) {
			if (errorMsg.indexOf(ERROR_CODE_H000997) == 0
					|| errorMsg.indexOf(ERROR_CODE_H000998) == 0
					|| errorMsg.indexOf(ERROR_CODE_H000999) == 0
					|| errorMsg.indexOf(ERROR_CODE_H001085) == 0) {
				isContain = true;
			}
		}

		return isContain;
	}

	/**
	 * 判断是否是已经取消状态的艺龙订单
	 * 
	 * @param errorMsg
	 * @return
	 */
	private boolean isCancledCode(String errorMsg) {
		boolean isCancled = false;
		/*
		 * LOG.info("SupplierOrderOperatorImpl.isContainRetryCode,errorMsg=" +
		 * errorMsg); if (errorMsg != null) { if (errorMsg.indexOf("") == 0 && 1
		 * == 0) { isCancled = true; } }
		 */

		return isCancled;
	}

	/**
	 * 获取供应商订单的Order，内部直填充OrderItem列表
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getSupplierOrderWithItemByOrderId(Long orderId) {
		OrdOrder supplierOrder = null;
		OrdOrder order = null;
		order = getOrderWithOrderItemByOrderId(orderId);
		// 检验供应商
		if (order != null && order.isSupplierOrder()) {
			supplierOrder = order;
		}

		return supplierOrder;
	}

	@Override
	public ResultHandle createCancelAuditForSupplierCreateFail(Long orderId) {
		ResultHandle resultHandle = new ResultHandle();
		OrdOrder order = getSupplierOrderWithItemByOrderId(orderId);
		if (order != null) {
			LOG.info("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail: orderId="
					+ orderId
					+ ",isSupplierCreateFailCode="
					+ order.isSupplierCreateFailCode());
			// 是否创建订单取消活动（因为创建第三方订单不成功的订单，需要创建订单取消活动）
			if (order.isNotSupplierCreateFailCode()) {
				LOG.info("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail:创建订单取消活动,orderId="
						+ order.getOrderId());
				ComAudit audit = new ComAudit();
				audit.setCreateTime(new Date());
				audit.setObjectId(order.getOrderId());
				audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				audit.setAuditType(OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.name());
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
				try {
					int count = orderAuditService.saveAudit(audit);
					LOG.info("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail: OrderID="
							+ order.getOrderId()
							+ ",orderAuditService.saveAudit, count=" + count);
				} catch (Exception ex) {
					resultHandle.setMsg(ex);
					LOG.info("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail: OrderID="
							+ order.getOrderId()
							+ ",取消活动, 发生异常："
							+ ex.getMessage());
				}
			} else {
				LOG.debug("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail:订单(ID="
						+ orderId
						+ ",CancelCode="
						+ order.getCancelCode()
						+ ")不是因为创建供应商订单失败而取消");
			}
		} else {
			LOG.debug("SupplierOrderOperatorImpl.createCancelActivityForSupplierCreateFail:订单(ID="
					+ orderId + ")不存在。");
			resultHandle.setMsg("订单(ID=" + orderId + ")不存在。");
		}

		return resultHandle;
	}

	@Override
	public OrderSupplierOperateResult updateSupplierOrder(Long orderId,
			String addition) {
		OrderSupplierOperateResult result = new OrderSupplierOperateResult();
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		if (order.isCancel()) {
			return null;
		}
		if (!order.hasInfoAndResourcePass()) {
			return null;
		}
		// 如果是预付的需要已经支付才可以操作，现付不受限制
		if (order.hasNeedPrepaid() && !order.hasPayed()) {
			return null;
		}
		result.setVstOrder(order);
		try {
			order.setAddition(addition);
			List<SuppOrderResult> suppOrderResultList = supplierOrderService
					.updateOrder(order);
			LOG.info("update supplier order:" + suppOrderResultList.size());
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
		}

		return result;
	}

	@Override
	public PassProvider getProductServiceInfo(Long goodsId) {
		PassProvider provider = null;
		try {
			provider= supplierOrderService.searchProductProvider(goodsId);
		} catch (Exception ex) {
			LOG.error("goodsId:" + goodsId
					+ ",error to get productService information:{}", ex);
		}
		return provider;
	}

    /**
     * 如果是目的地，酒+景、酒套餐，意外险后置订单，只有在游玩人补全（后置状态为完成）下，才会去创建意外险供应商订单
     * @param order
     * @param orderItemList
     * @param isFromComJob
     */
    private void filterUnCompletedAccInsOrderItem(OrdOrder order) {
        LOG.info("filterUnCompletedAccInsOrderItem orderId" + order.getOrderId());
        if(order == null || order.getOrderItemList() == null) {
            LOG.error("订单为空或该订单无子订单");
            return;
        }
        
        Long orderId = order.getOrderId();
        OrdAccInsDelayInfo accInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
        //非后置
        if (null == accInsDelayInfo) {
            LOG.info("No AccInsDelay order orderId = " + orderId);
            return;
        }
        
        if (null != accInsDelayInfo) {
            String travDelayFlag = accInsDelayInfo.getTravDelayFlag();
            String travDelayStatus = accInsDelayInfo.getTravDelayStatus();
            LOG.info("travDelayFlag " + travDelayFlag);
            LOG.info("travDelayStatus " + travDelayStatus);
            //非后置
            if (StringUtils.isNotBlank(travDelayFlag) && "N".equalsIgnoreCase(travDelayFlag)) {
                LOG.info("No AccInsDelay order orderId = " + orderId);
                return;
            }
            
            if (StringUtils.isNotBlank(travDelayFlag) && "Y".equalsIgnoreCase(travDelayFlag)) {
                
                if (StringUtils.isNotBlank(travDelayStatus) && travDelayStatus.equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.COMPLETED.name())) {
                    //意外险 游玩人 已补全
                    LOG.info("AccInsDelay has completed orderId = " + orderId);
                    return;
                }else {
                    //意外险弃保 或者 未完成
                    List<OrdOrderItem> accInsOrderItem = OrdOrderUtils.getAccInsOrderItem(order);
                    
                    Set<Long> destBuAccInsOrderItemSet = new HashSet<Long>();
                    for (OrdOrderItem ordOrderItem : accInsOrderItem) {
                        destBuAccInsOrderItemSet.add(ordOrderItem.getOrderItemId());
                    }
                    
                    if (accInsOrderItem != null) {
                        for (Iterator<OrdOrderItem> it = order.getOrderItemList()
                                .iterator(); it.hasNext();) {
                            OrdOrderItem item = it.next();
                            if (destBuAccInsOrderItemSet.contains(item.getOrderItemId())) {
                                it.remove();
                            }
                        }
                    }else {
                        LOG.info("accInsOrderItem is null");
                    }
                }
            }
        }
    }
	
    
    /**
     * 释放锁
     */
    private void releaseLock(String key){
    	if(jedis!=null){
    		try{
    			jedis.del(key);
    			LOG.info("释放锁"+key+"成功");
    		}catch(Exception e){
    			LOG.info("释放锁"+key+"异常",e);
    		}
    	}else{
    		LOG.info("释放锁"+key+",发现jedis对象为空");
    	}
    }
	
}
