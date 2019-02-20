/**
 * 
 */
package com.lvmama.vst.order.web;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.ticket.service.PassCodeForChangLongService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsCircusClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.ticket.service.InterimProcesserClientForTicketService;
import com.lvmama.vst.back.goods.po.SuppGoodsCircus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.vo.TicketOrderItemVo;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderHandleService;
import com.lvmama.vst.order.service.book.util.ConfirmEnum;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;

/**
 * 
 * @author liuxiaoyong
 *
 */
@Controller
public class ActivitiTaskFinishAction extends BaseActionSupport {

	private static final Logger logger = LoggerFactory.getLogger(ActivitiTaskFinishAction.class);
	
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;
	
	@Autowired
	private ProcesserClientService processerClientService;
	
	@Autowired
	private InterimProcesserClientForTicketService interimProcesserClientForTicketService;
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	
	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrderUpdateService  orderUpdateService;
	
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
	private ISupplierOrderHandleService supplierOrderHandleService;
	
	@Autowired
	protected ProdPackageGroupClientService prodPackageGroupClientService;
	
	@Autowired
	protected SuppGoodsCircusClientService suppGoodsCircusClientServiceRemote;

	@Autowired
	private PassCodeForChangLongService passCodeForChangLongService;
	@RequestMapping(value = "/ord/order/finishTask")
	@ResponseBody
	public Object addMessage( HttpServletRequest request,Long orderId,String auditType){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		return finishOrderTask(orderId, auditType);
	}
	private String finishOrderTask(Long orderId, String auditType) {
	    if(orderId==null||auditType==null){
			return("参数为空.");
		}
		
		ComAudit audit = null;
		
		if (("PRETRIAL_AUDIT".equalsIgnoreCase(auditType) || "INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", orderId);
			param.put("auditType", auditType);
			param.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			List<ComAudit> audits = this.orderAuditService.queryAuditListByParam(param);
			if (CollectionUtils.isNotEmpty(audits)) {
				audit = audits.get(0);
			}
		}
		if (audit == null) {
			return (orderId+"查不到audit信息");
		}
		
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
			if(null!=order){
				if(ActivitiUtils.hasActivitiOrder(order)){
					OrdOrderItem mainOrderItem = order.getMainOrderItem();
					ActivitiKey activitiKey = null;
					if(OrdOrderUtils.isDestBuFrontOrder(order) 
							&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId() 
							&& mainOrderItem != null && mainOrderItem.hasSupplierApi()) {
						activitiKey = createKeyOfHotelDockByOrder(order, ActivitiUtils.getOrderType(audit));
					} else {
						activitiKey = createKeyByOrder(order);
					}
					this.processerClientService.completeTaskByAudit(activitiKey,audit);
					msg.raise(orderId+"成功执行.");
				}else{
					msg.raise(orderId+"不是工作流订单.");
				}
			}else{
				msg.raise(orderId+"Order对象不存在.");
			}
		}catch (Exception e){
			// TODO: handle exception
			msg.raise("发生异常."+e);
		}
		return msg.toString();
    }
	
	
	/**
	 * 新增子订单工作流补偿处理
	 * 处理子工作流的信息
	 * */
	@RequestMapping(value = "/ord/order/finishChildTask")
	@ResponseBody
	public Object addChildMessage(HttpServletRequest request,Long orderItemId,String auditType){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		return finishOrderItemTask(orderItemId, auditType);
	}
	private String finishOrderItemTask(Long orderItemId, String auditType) {
	    if (!("INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
			return ("audit type非法");
		}
		if(orderItemId == null || auditType == null){
			return("orderItemId 和 auditId 为必要参数!");
		}
		
		ComAudit audit = null;
		
		if (("PRETRIAL_AUDIT".equalsIgnoreCase(auditType) || "INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", orderItemId);
			param.put("auditType", auditType);
			param.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			List<ComAudit> audits = this.orderAuditService.queryAuditListByParam(param);
			if (CollectionUtils.isNotEmpty(audits)) {
				audit = audits.get(0);
			}
		}
		if (audit == null) {
			return ("查不到audit信息");
		}
		
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			OrdOrderItem ordOrderItem = this.ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
			if(ordOrderItem == null){
				msg.raise(orderItemId+"没有找到对应的子订单!");
				return msg.toString();
			}
			OrdOrder order=this.complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
			if(order == null){
				msg.raise(orderItemId+"没有找到对应的主订单!");
				return msg.toString();
			}
			if(!ActivitiUtils.hasActivitiOrder(order)){
				msg.raise(orderItemId+"不是工作流订单!");
				return msg.toString();
			}
			
             ActivitiKey activitiKey = null;
             if(OrdOrderUtils.isDestBuFrontOrder(order) 
          			&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == ordOrderItem.getCategoryId() 
          			&& ordOrderItem != null && ordOrderItem.hasSupplierApi()) {
	          	 activitiKey = createKeyOfHotelDockByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(audit));
	           } else {
	          	 activitiKey = createKeyByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(audit));
	           }
			this.processerClientService.completeTaskByAudit(activitiKey,audit);
			msg.raise(orderItemId+"成功执行.");
		}catch(Exception e){
			msg.raise("处理异常:"+e);
		}
		return msg.toString();
    }
	
	@RequestMapping(value = "/ord/order/sendResourceMessage")
	@ResponseBody
	public Object sendResourceMessage(HttpServletRequest request,Long orderId){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			if(orderId == null){
				msg.raise("orderId 为必要参数!");
				return msg;
			}
			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
			if(order == null){
				msg.raise("没有找到对应的主订单!");
				return msg;
			}
			if (order.hasResourceAmple()) {
				msg.addObject("CallFlag", "成功执行.");
				this.sendResourceStatusAmpleMsg(orderId);
				prepayAmpleVst(orderId, order);
			}else{
				msg.addObject("CallFlag", "没有资源审核,无法处理!");
			}
		}catch(Exception e){
			msg.raise("处理异常:"+e);
		}
		return msg;
	}
		
	/**
	 * 资源审核成功后发送消息
	 * @param order
	 * @return
	 */
	public void sendResourceStatusAmpleMsg(Long orderId){
		//ResultHandle resultHandle=new ResultHandle();
		//发送jms消息通知资源审核通过
		orderMessageProducer.sendMsg(MessageFactory.newOrderResourceStatusMessage(orderId,  OrderEnum.RESOURCE_STATUS.AMPLE.getCode()));
	}
	
	public void prepayAmpleVst(Long orderId, OrdOrder order) {
		if(order.hasNeedPrepaid()){
			if(order.getOughtAmount()==0){
				ordPrePayServiceAdapter.vstOrder0YuanPayMsg(orderId);
			}else if(order.hasPayed()){
				ordPrePayServiceAdapter.resourceAmpleVst(orderId);
			}
		}
	}
	
	private ActivitiKey createKeyByOrder(OrdOrder order){
		ComActivitiRelation relation = getRelation(order);
		return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
	}
	
	private ActivitiKey createKeyByOrderItem(OrdOrderItem item,String type){
		logger.info("createKeyByOrderItem item.orderid="+item.getOrderId()+",type="+type);
		return new ActivitiKey((String)null, ActivitiUtils.createOrderBussinessKey(item, type));
	}

	/**
	 * 打包酒店对接流程key
	 * @param item
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrderItem(OrdOrderItem item, String type){
		return new ActivitiKey((String)null, ActivitiUtils.createItemHotelDockBussinessKey(item, type));
	}
	
	/**
	 * 单酒店对接流程key
	 * @param order
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrder(OrdOrder order, String type){
		return new ActivitiKey((String)null, ActivitiUtils.createOrderHotelDockBussinessKey(order, type));
	}

	private ComActivitiRelation getRelation(OrdOrder order){
		try{
			ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
			if(comActiveRelation == null){//补偿机制,通过工作流再次去触发查询
				String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
				if(processId != null){
					comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					comActiveRelation = new ComActivitiRelation();
					comActiveRelation.setObjectId(order.getOrderId());
					comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
					comActiveRelation.setProcessId(processId);
					comActiveRelation.setProcessKey(order.getProcessKey());
				}
			}
			return comActiveRelation;
		}catch(Exception e){
		}
		return null;
	}
	
	@RequestMapping(value = "/ord/order/showDeleteHistoricProcess")
	public String showDeleteHistoricProcess(Model model, HttpServletRequest request) {
		
		return "/order/deleteHistoricprocess";
	}
	
	@RequestMapping(value = "/ord/order/delHistoricProcess")
	@ResponseBody
	public Object delHistoricProcess( HttpServletRequest request,String processIds){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		ResultMessage msg = ResultMessage.createResultMessage();
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		try {
			if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
				throw new Exception("权限不足");
			}
			if(StringUtil.isNotEmptyString(processIds)) {
				String[] processIdArray = processIds.split(",");
				for(int i=0; i<processIdArray.length; i++) {
//					processerClientService.deleteHistoricProcess(processIdArray[i]);
					String currProcessStatus = "DELETED";
					try {
						processerClientService.deleteHistoricProcess(processIdArray[i], null);
					} catch (Exception ex) {
						logger.error("删除流程历史记录失败，流程ID：" + processIdArray[i] + "，异常信息：" + ex.getMessage());
						currProcessStatus = "DELETE_FAIL";
					}
					
					//更新状态
					comActivitiRelationService.updateProcessStatus(processIdArray[i], currProcessStatus);
				}
			}else{
				msg.raise("参数为空.");
			}
		}catch (Exception e) {
			msg.raise("发生异常."+e);
		}
		return msg;
	}

	// 此功能仅作后台使用
	@RequestMapping(value = "/ord/order/findchimelongordersbyvisittime")
	public String findChimelongOrdersByVisitTime(Model model, String visitTimeStart, String visitTimeEnd,
												 String createTimeStart, String createTimeEnd,
												 String ids, HttpServletRequest req,HttpServletResponse res) throws Exception
	{


//		if (StringUtil.isEmptyString(getLoginUserId()))
//		{
//			model.addAttribute("errorMsg","请先<a href='http://super.lvmama.com/pet_back/login.do'>登录</a>！");
//			return "/order/findChimelongOrdersByVisitTime";
//		}

		model.addAttribute("visitTimeStart", visitTimeStart);
		model.addAttribute("visitTimeEnd", visitTimeEnd);
		model.addAttribute("createTimeStart", createTimeStart);
		model.addAttribute("createTimeEnd", createTimeEnd);
		model.addAttribute("ids", ids);


		if(StringUtil.isEmptyString(visitTimeStart)) {
			model.addAttribute("errorMsg","请输入游玩开始时间！");
			return "/order/findChimelongOrdersByVisitTime";
		}
		if(StringUtil.isEmptyString(visitTimeEnd)) {
			model.addAttribute("errorMsg","请输入游玩结束时间！");
			return "/order/findChimelongOrdersByVisitTime";
		}
		if(StringUtil.isEmptyString(ids)) {
			model.addAttribute("errorMsg","请输入相关的产品ID！");
			return "/order/findChimelongOrdersByVisitTime";
		}
		if(StringUtil.isEmptyString(createTimeStart) && StringUtil.isNotEmptyString(createTimeEnd)) {
			model.addAttribute("errorMsg","订单创建开始结束时间必须同时输入或者不输入，不可只填一个！");
			return "/order/findChimelongOrdersByVisitTime";
		}
		if(StringUtil.isNotEmptyString(createTimeStart) && StringUtil.isEmptyString(createTimeEnd)) {
			model.addAttribute("errorMsg","订单创建开始结束时间必须同时输入或者不输入，不可只填一个！");
			return "/order/findChimelongOrdersByVisitTime";
		}

		visitTimeStart = visitTimeStart.trim();
		visitTimeEnd = visitTimeEnd.trim();
		ids = ids.trim();

		// 订单创建时间的查询条件
//		String createTimeConditionTemplate =
//				"and od.create_time <= to_date('###CREATE_TIME_END###', 'yyyy-mm-dd hh24:mi:ss') and od.create_time >= to_date('###CREATE_TIME_START###', 'yyyy-mm-dd hh24:mi:ss')";

		// 查询长隆指定日期的SQL
//		String mainSqlTemplate = "select distinct oo.order_id 订单号, nvl((select op.mobile from ord_person op where op.person_type = 'CONTACT' and op.object_id = oo.order_id), (select op.mobile from ord_person op where op.person_type = 'BOOKER' and op.object_id = oo.order_id)) 联系人手机号, decode(oo.payment_status, 'UNPAY', '未付款', 'PAYED', '已支付') 支付状态, to_char(od.create_time, 'yyyy-mm-dd hh24:mi:ss') 下单时间, to_char(oo.visit_time, 'yyyy-mm-dd') 游玩时间, (select s.distributor_name from dist_distributor s where s.distributor_id = od.distributor_id) 分销商, distributor_code 下单渠道, decode(ipc.status, 'DESTROYED_FAILED', '废码失败', 'DESTROYED_SUCCESS', '废码成功', 'DESTROYED_NOHANDLE', '废码未提交', 'DESTROYED_TEMP_FAILED', '废码临时失败', 'DESTROYED_AUDIT', '废码审核中', 'UPDATE_NOHANDLE', '修改未提交', 'UPDATE_SUCCESS', '修改成功', 'UPDATE_FAILED', '修改失败', 'ABANDON', '已丢弃', 'APPLIED_FAILED', '申码失败', 'APPLIED_NOHANDLE', '申码未处理', 'APPLIED_SUCCESS', '申码成功', 'APPLIED_TEMP_FAILED', '申码处理中', 'APPLIED_AUDIT', '申码中', '未知状态') 申码状态, ipc.code 通关码信息, ipc.add_code 辅助码信息 from ord_order_item oo, ord_order od, intf_pass_code ipc where oo.order_id = od.order_id and ipc.order_id(+) = od.order_id and oo.supp_goods_id in (###IDS###) and oo.visit_time <= to_date('###VISIT_TIME_END###', 'yyyy-mm-dd') and oo.visit_time >= to_date('###VISIT_TIME_START###', 'yyyy-mm-dd') ###CREATE_TIME_CONDITION###  and od.order_status != 'CANCEL' order by 支付状态 desc";

		// 替换相关的商品ID和游玩时间
		List<String> goodsIds = Arrays.asList(StringUtils.split(ids,","));
		Map<String,Object> params1 =new HashMap<String,Object>();
		params1.put("ids", goodsIds);
		params1.put("visitTimeStart", visitTimeStart);
		params1.put("visitTimeEnd", visitTimeEnd);
		// 替换下单时间
		if (StringUtil.isNotEmptyString(createTimeStart) && StringUtil.isNotEmptyString(createTimeEnd))
		{
			createTimeStart = createTimeStart.trim() + " 00:00:00";
			createTimeEnd = createTimeEnd.trim() + " 23:59:59";
			params1.put("createTimeStart", createTimeStart);
			params1.put("createTimeEnd", createTimeEnd);

		}
		logger.info("params1 sql=" + params1);
		List<Map<String, Object>> results = complexQueryService.selectOrdOrderByOrderIds(params1);
		if(results==null||results.size()==0){
			return "/order/findChimelongOrdersByVisitTime";
		}
		List<Long> longs = new ArrayList<>();
		for (Map<String, Object> map : results) {
			Object id = map.get("订单号");
			String s = id.toString();
			longs.add(Long.parseLong(s));
		}

		List<Map<String, Object>> byOrderIds = passCodeForChangLongService.queryForChangLong(longs);

		for (Map<String, Object> map : byOrderIds) {
			Long orderId = Long.parseLong(map.get("订单号").toString());
			for (Map<String, Object> result : results) {
				Long orderId2 = Long.parseLong(result.get("订单号").toString());
				if(orderId.equals(orderId2)){
					result.putAll(map);
				}
			}
		}

		Iterator<Map<String, Object>> iterator = results.iterator();
		while (iterator.hasNext()){
			Map<String, Object> next = iterator.next();
			if(next.get("申码状态")==null){
				iterator.remove();
			}
		}

		try {
			if(CollectionUtils.isNotEmpty(results)){
				int size = results.get(0) ==null ? 0 : results.get(0).size();
				Set<String> head = results.get(0)==null?null:results.get(0).keySet();
				for(Map<String, Object> r : results) {
					if(r==null) {
						continue;
					}
					if(r.size() > size) {
						size = r.size();
						head = r.keySet();
					}
				}
				model.addAttribute("resultHead", head);
				model.addAttribute("resultList", results);
			} else {
				model.addAttribute("errorMsg","没有查到数据。");
			}
		} catch (Exception e) {
			logger.error("findChimelongOrdersByVisitTime error:", e);
			model.addAttribute("errorMsg","查询数据出错，请检查输入的条件格式是否正确。");
		}


		return "/order/findChimelongOrdersByVisitTime";
	}
	
	@RequestMapping(value = "/ord/order/findObjectList")
	public String findObjectList(Model model, String sql, HttpServletRequest req,HttpServletResponse res,String readDB) throws Exception {
		res.setContentType("text/html;charset=utf-8");
		if(!checkUrlValid(req.getParameter("code"))){
			return ("连接非法");
		}
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			model.addAttribute("errorMsg","no permition!");
			return "/order/findObjectList";
		}
		if(StringUtil.isNotEmptyString(sql)) {
			List<Map<String, Object>> result=null;
			if(sql.trim().toLowerCase().startsWith("select ")) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("sql", sql);
                if(readDB != null){
                    result = complexQueryService.findAllObjectsBySqlFromReadDB(params);
                }else{
                    result = complexQueryService.findAllObjectsBySql(params);
                }
			} else {
				model.addAttribute("errorMsg","only select accept!");
				return "/order/findObjectList";
			}
			if(CollectionUtils.isNotEmpty(result)){
				int size = result.get(0) ==null ? 0 : result.get(0).size();
				Set<String> head = result.get(0)==null?null:result.get(0).keySet();
				for(Map<String, Object> r : result) {
					if(r==null) {
						continue;
					}
					if(r.size() > size) {
						size = r.size();
						head = r.keySet();
					}
				}
				model.addAttribute("resultHead", head);
				model.addAttribute("resultList", result);
			}
		}
		model.addAttribute("sql", sql);
		try {
			model.addAttribute("code", DESCoder.encrypt(DateUtil.formatSimpleDate(DateUtil.DsDay_HourOfDay(DateUtil.getTodayDate(), 0 * 24))));
		} catch (Exception e) {
			 
		}
		return "/order/findObjectList";
	}
	
	@RequestMapping(value = "/ord/order/startProcesser")
	@ResponseBody
	public Object startProcesser(HttpServletRequest request,Long orderId) {
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("startProcesser start, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		if(comActivitiRelationService.queryRelation(null, orderId, ComActivitiRelation.OBJECT_TYPE.ORD_ORDER) != null) {
			logger.info("工作流已经存在，不能重复生成, orderId:" + orderId);
			return "工作流已经存在，不能重复生成";
		}
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		if(order == null) {
			return null;
		}
		
		if(order.hasCanceled()) {
			return "order has been canceled";
		}
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("orderId", order.getOrderId());
		params.put("mainOrderItem", order.getMainOrderItem());
		params.put("order", order);
		String processId = processerClientService.startProcesser(order.getProcessKey(),ActivitiUtils.createOrderBussinessKey(order), params);
		comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
		
		if(order.getDistributorId().longValue() == 2) {
			orderLocalService.startBackOrder(orderId, getLoginUserId());
		}
		
		if(order.hasNeedPrepaid() && order.hasPayed() && ActivitiUtils.hasActivitiOrder(order)){
			processerClientService.paymentSuccess(createKeyByOrder(order));
		}
		
		logger.info("startProcesser end, orderId:" + orderId);
		return "successful";
	}
	
	@RequestMapping(value = "/ord/order/sendMessage")
	@ResponseBody
	public Object sendMessage(HttpServletRequest request, String objectType, Long objectId, String eventType, String addition) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, objectId:" + objectId);
			return "权限不足";
		}
		
		logger.info("start send message, objectId:" + objectId);
		if(objectId == null) {
			return null;
		}
		
		Message message = MessageFactory.newCommonMessage(objectId, objectType, eventType, addition);
		
		orderMessageProducer.sendMsg(message);
		
		return "successful";
	}
	
	@RequestMapping(value = "/ord/order/completeTaskByAudit")
	@ResponseBody
	public Object completeTaskByAudit(HttpServletRequest request, Long orderId, Long orderItemId, Long auditId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, auditId:" + auditId);
			return "权限不足";
		}
		
		logger.info("start complete task, auditId:" + auditId);
		if(auditId == null) {
			return null;
		}
		
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		
		if (orderItemId != null) {
			OrdOrderItem mainOrderItem = orderUpdateService.getOrderItem(orderItemId);
			OrdOrder order = complexQueryService.queryOrderByOrderId(mainOrderItem.getOrderId());
            ActivitiKey activitiKey = null;
            if(OrdOrderUtils.isDestBuFrontOrder(order)
                    && mainOrderItem != null && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == mainOrderItem.getCategoryId()
                    && mainOrderItem.hasSupplierApi()) {
                activitiKey = createKeyOfHotelDockByOrderItem(mainOrderItem, ActivitiUtils.getOrderType(audit));
            } else {
                activitiKey = createKeyByOrderItem(mainOrderItem, ActivitiUtils.getOrderType(audit));
            }
            this.processerClientService.completeTaskByAudit(activitiKey,audit);

		} else if (orderId != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			if(ActivitiUtils.hasActivitiOrder(order)){
				OrdOrderItem mainOrderItem = order.getMainOrderItem();
				ActivitiKey activitiKey = null;
                if(OrdOrderUtils.isDestBuFrontOrder(order)
                        && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()
                        && mainOrderItem != null && mainOrderItem.hasSupplierApi()) {
                    activitiKey = createKeyOfHotelDockByOrder(order, ActivitiUtils.getOrderType(audit));
                } else {
                    activitiKey = createKeyByOrder(order);
                }
                this.processerClientService.completeTaskByAudit(activitiKey,audit);
				
			} else {
				return "it does not have a workflow.";
			}
		}
		
		return "successful";
	}
	
	@RequestMapping(value = "/ord/order/completeTaskByTaskName")
	@ResponseBody
	public Object completeTaskByTaskName(HttpServletRequest request, Long orderId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243","lv10283"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("start completeTaskByTaskName, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		processerClientService.completeTask(createKeyByOrder(order),"orderAuditReceiveTask","system");
		
		return "successful";
	}
	
	@RequestMapping(value = "/ord/order/completeTaskByOrderTaskName")
	@ResponseBody
	public Object completeTaskByOrderTaskName(HttpServletRequest request, Long orderId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243","lv10283"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("start completeTaskByOrderTaskName, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		processerClientService.processerTask(createKeyByOrder(order), "receivetask2", "system");
		
		return "successful";
	}
	
	
	@RequestMapping(value = "/ord/order/paymentSuccessProcess")
	@ResponseBody
	public Object paymentSuccessProcess(HttpServletRequest request, Long orderId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("start paymentSuccessProcess, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		processerClientService.paymentSuccess(createKeyByOrder(order));
		
		return "successful";
	}
	
	@RequestMapping(value = "/ord/order/getTicketItemListByUserId")
	@ResponseBody
	public Object getTicketItemListByUserId(HttpServletRequest request, String userId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + userId);
			return "权限不足";
		}
		
		logger.info("start getTicketItemListByUserId, userId:" + userId);
		if(userId == null) {
			return null;
		}
		
		ResultHandleT<List<TicketOrderItemVo>> ticketOrderItemVoResult = this.orderLocalService.getTicketItemListByUserId(userId);
		StringBuffer sb = new StringBuffer();
		if(ticketOrderItemVoResult != null) {
			List<TicketOrderItemVo> ticketOrderItemVoList = ticketOrderItemVoResult.getReturnContent();
			if(CollectionUtils.isNotEmpty(ticketOrderItemVoList)) {
				for(TicketOrderItemVo itemVo : ticketOrderItemVoList) {
					if(sb.length() > 0) {
						sb.append(";");
					}
					sb.append(itemVo.getCode()  + "," + itemVo.getCodeImage());
				}
			}
		}
		
		return sb.toString();
	}
	
	@RequestMapping(value = "/ord/order/getCodeImageByPassSerialNo")
	@ResponseBody
	public Object getCodeImageByPassSerialNo(HttpServletRequest request, String passSerialNo) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, passSerialNo:" + passSerialNo);
			return "权限不足";
		}
		
		logger.info("start getCodeImageByPassSerialNo, passSerialNo:" + passSerialNo);
		if(passSerialNo == null) {
			return null;
		}
		
		List<String> passSerialNos = new ArrayList<String>();
		Map<String, byte[]> codeImageMap = new HashMap<String, byte[]>();
		passSerialNos.add(passSerialNo);
		codeImageMap.putAll(supplierOrderOtherService.getCodeImages(passSerialNos));
		byte[] codeImageArray = codeImageMap.get(passSerialNo);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("passSerialno", passSerialNo);
		List<OrdPassCode> ordPassCodeList = supplierOrderHandleService.selectOrdPassCodeByParams(params);
		byte[] codeImages = null;
		if(CollectionUtils.isNotEmpty(ordPassCodeList)) {
			codeImages = ordPassCodeList.get(0).getCodeImage();
		}
		
		StringBuffer sb = new StringBuffer();
		if(codeImageArray != null) {
			sb.append("codeImageArray:");
			for(byte b : codeImageArray) {
				sb.append(b);
			}
		}
		if(codeImageArray != null) {
			sb.append("<br/>codeImages:");
			for(byte b : codeImages) {
				sb.append(b);
			}
		}
		
		return sb.toString();
	}
	
	@RequestMapping(value = "/ord/order/getTicketItemListByOrderId")
	@ResponseBody
	public Object getTicketItemListByOrderId(HttpServletRequest request, Long orderId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("start getTicketItemListByOrderId, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		
		ResultHandleT<List<TicketOrderItemVo>> ticketOrderItemVoResult = this.orderLocalService.getTicketItemListByOrderId(orderId);
		StringBuffer sb = new StringBuffer();
		if(ticketOrderItemVoResult != null) {
			List<TicketOrderItemVo> ticketOrderItemVoList = ticketOrderItemVoResult.getReturnContent();
			if(CollectionUtils.isNotEmpty(ticketOrderItemVoList)) {
				for(TicketOrderItemVo itemVo : ticketOrderItemVoList) {
					if(sb.length() > 0) {
						sb.append(";");
					}
					sb.append(itemVo.getOrderId() + ", " + itemVo.getOrderItemId() + ", " + itemVo.getCode() + ",");
					if(itemVo.getCodeImage() != null) {
						for(byte b : itemVo.getCodeImage()) {
							sb.append(b);
						}
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	@RequestMapping(value = "/ord/order/getOrdPassCodeByOrderItemId")
	@ResponseBody
	public Object getOrdPassCodeByOrderItemId(HttpServletRequest request, Long orderId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, orderId:" + orderId);
			return "权限不足";
		}
		
		logger.info("start getTicketItemListByOrderId, orderId:" + orderId);
		if(orderId == null) {
			return null;
		}
		
		
		ResultHandleT<OrdPassCode> passCodeResult = this.orderLocalService.getOrdPassCodeByOrderItemId(orderId);
		StringBuffer sb = new StringBuffer();
		if(passCodeResult != null) {
			OrdPassCode ordPassCode = passCodeResult.getReturnContent();
			if(ordPassCode != null) {
				sb.append(ordPassCode.getOrderItemId() + "," + ordPassCode.getPassSerialno() + ",");
				if(ordPassCode.getCodeImage() != null) {
					for(byte b : ordPassCode.getCodeImage()) {
						sb.append(b);
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	
	@RequestMapping(value = "/ord/order/findTicketPackageProdByGoodsId")
	@ResponseBody
	public Object findTicketPackageProdByGoodsId(HttpServletRequest request, Long suppGoodsId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, suppGoodsId:" + suppGoodsId);
			return "权限不足";
		}
		
		logger.info("start findTicketPackageProdByGoodsId, suppGoodsId:" + suppGoodsId);
		if(suppGoodsId == null) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		List<ProdProduct> packageProdList = this.prodPackageGroupClientService
				.findTicketPackageProdByGoodsId(suppGoodsId);
		if (null != packageProdList) {
			for(ProdProduct product : packageProdList) {
				if(sb.length() > 0) {
					sb.append(";");
				}
				sb.append(product.getProductId() + "," + product.getSaleFlag());
			}
			
		}
		
		return sb.toString();
	}

    @RequestMapping(value = "/ord/order/setMemcachedValue")
    @ResponseBody
    public Object setMemcachedValue(HttpServletRequest request, String memKey, String memValue) {

        if(!checkUrlValid(request.getParameter("code"))){
            return ("连接非法");
        }

        String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
        if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
            logger.info("权限不足, memKey:" + memKey);
            return "权限不足";
        }

        logger.info("start setMemcachedValue, memKey:" + memKey);
        if(memKey == null) {
            return null;
        }

        MemcachedUtil.getInstance().set(memKey, memValue);

        return "successful";
    }
	
	@RequestMapping(value = "/ord/order/findSuppGoodsCircusByGoodsId")
	@ResponseBody
	public Object findSuppGoodsCircusByGoodsId(HttpServletRequest request, Long suppGoodsId) {
		
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		
		String[] authUsers = new String[]{"lv6800", "lv6764", "admin","lv9243"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			logger.info("权限不足, suppGoodsId:" + suppGoodsId);
			return "权限不足";
		}
		
		logger.info("start findSuppGoodsCircusByGoodsId, suppGoodsId:" + suppGoodsId);
		if(suppGoodsId == null) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		ResultHandleT<List<SuppGoodsCircus>> goodsCircusListResult = this.suppGoodsCircusClientServiceRemote.getSuppGoodsCircusByGoodsId(suppGoodsId);
		if (null != goodsCircusListResult && goodsCircusListResult.getReturnContent() !=  null) {
			for(SuppGoodsCircus circus : goodsCircusListResult.getReturnContent()) {
				if(sb.length() > 0) {
					sb.append(";");
				}
				sb.append(circus.getCircusId() + "," + circus.getSuppGoodsId() + "," + circus.getProductId());
			}
		}
		
		return sb.toString();
	}
	
	
	
	private boolean checkUrlValid(String code){
		if(code == null){
			return false;
		}
		
		try{
			code = DESCoder.decrypt(code);
		}catch(Exception e){
			log.info(e);
		}
		
		String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());
		
		if(today.equalsIgnoreCase(code)){
			return true;
		}
		return false;
	}
	
	@RequestMapping(value = "/ord/order/updateTicketFlow")
	@ResponseBody
	public Object updateTicketFlowByRedis(HttpServletRequest request,String type,String status){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			if(type == null || status ==null){
				msg.raise("type and status为必要参数!");
				return msg;
			}
			if("PC".equalsIgnoreCase(type) || "APP".equalsIgnoreCase(type) || "TNT".equalsIgnoreCase(type)){
				ConfirmEnum.setTicketByRedis(type, status);
			}else{
				msg.raise("非法要参数!");
				return msg;
			}
		}catch(Exception e){
			msg.raise("处理异常:"+e);
		}
		return msg;
	}
	
	@RequestMapping(value = "/ord/order/queryTicketFlowStatus")
	@ResponseBody
	public Object queryTicketFlowByRedis(HttpServletRequest request,String type){
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			if(type == null){
				msg.raise("type为必要参数!");
				return msg;
			}
			if("PC".equalsIgnoreCase(type) || "APP".equalsIgnoreCase(type) || "TNT".equalsIgnoreCase(type)){
				return ConfirmEnum.getTicketWorkEnabledStatus(type);
			}else{
				msg.raise("非法要参数!");
				return msg;
			}
		}catch(Exception e){
			msg.raise("处理异常:"+e);
		}
		return msg;
	}
	
	@RequestMapping(value = "/ord/order/completeTaskforServiceTask")
	@ResponseBody
	public Object updateTicketFlowForServiceTask(HttpServletRequest request,String orderId){
		if(!checkUrlValid(request.getParameter("code"))||StringUtil.isEmptyString(orderId)){
			return ("连接非法");
		}
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			
			OrdOrder order = this.complexQueryService.queryOrderByOrderId(Long.parseLong(orderId));
			if(null!=order){
				if(ActivitiUtils.hasActivitiOrder(order)){
					ActivitiKey activitiKey = createKeyByOrder(order);
					processerClientService.completeJobTask(activitiKey,"USER");
				}
			}
            
		}catch(Exception e){
			msg.raise("处理异常:"+e);
		}
		return msg;
	}
	
	
	/**
	 * 执行门票工作流job，临时链接，后期会删除
	 * @param request
	 * @param orderId
	 * @return
	 */
	@RequestMapping(value = "/ord/order/completeJobForInterimTicket")
	@ResponseBody
	public Object updateTicketFlowForInterimTicket(HttpServletRequest request,String orderId){
		ResultMessage msg = ResultMessage.createResultMessage();
		if(!checkUrlValid(request.getParameter("code"))||StringUtil.isEmptyString(orderId)){
			msg.raise("连接非法");
			return msg;
		}
		
		try{
			OrdOrder order = this.complexQueryService.queryOrderByOrderId(Long.parseLong(orderId));
			if(null!=order){
				if(ActivitiUtils.hasActivitiOrder(order)){
					ActivitiKey activitiKey = createKeyByOrder(order);
					ResultHandle rh =interimProcesserClientForTicketService.completeJobTaskForTicket(activitiKey, "USER");
					if(rh!=null && StringUtils.isNotBlank(rh.getMsg())){
						msg.raise(rh.getMsg());
					}
					
				}
			}
            
		}catch(Exception e){
			logger.error("门票JOB补偿异常，订单号="+orderId,e);
			msg.raise("处理异常:"+e);
		}
		return msg;
	}
	
	
}
