package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.supp.service.SuppFaxClientService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrdTravAdditionConf;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_ID_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_PEOPLE_TYPE;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.OrdOrderPersonVO;
import com.lvmama.vst.comm.vo.OrdOrderPersonVO.ItemPersonRelation;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdTravAdditionConfService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderStatusManageService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdOrderTravellerService;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;

/**
 * 人工分单action
 * 
 * @author zhangwei
 * @param <E>
 * 
 */
@Controller
public class OrderDetailUpdateAction extends BaseOrderAciton {

	private static final Log LOG = LogFactory.getLog(OrderDetailUpdateAction.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Autowired
	private OrderEcontractGeneratorService orderEcontractGeneratorService;
	@Autowired
	private IOrderSendSmsService iOrderSendSmsService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private OrdOrderTravellerService ordOrderTravellerService;
	
	@Autowired
	private IOrdPersonService ordPersonService;
	
	@Autowired
	private IOrderStatusManageService orderStatusManageService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private SuppFaxClientService suppFaxClientService;
	
	@Autowired
    private IOrdTravAdditionConfService ordTravAdditionConfService;
	
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	@RequestMapping(value = "/ord/order/update/showUpdateTourist")
	public String showUpdateTourist(Model model, HttpServletRequest request,Long orderId,String isSupplyFlag){
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdPerson> ordPersonList = order.getOrdPersonList();
		int personNum=0;
		if(CollectionUtils.isNotEmpty(order.getOrdTravellerList())){
			personNum=order.getOrdTravellerList().size();
			for(OrdPerson ordPerson : order.getOrdTravellerList()){
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("orderId", ordPerson.getObjectId());
				param.put("orderPersonId", ordPerson.getOrdPersonId());
				List<OrdTravAdditionConf> ordTravAdditionConfList = ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param);
				if(ordTravAdditionConfList!=null && ordTravAdditionConfList.size() > 0){
					ordPerson.setOrdTravAdditionConf(ordTravAdditionConfList.get(0));
				}
			}
		}
		OrdPerson emergencyPerson=new OrdPerson();
		if(CollectionUtils.isNotEmpty(ordPersonList)){
			for (OrdPerson ordPerson : ordPersonList) {
				if(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equals(ordPerson.getPersonType())){
					emergencyPerson=ordPerson;
				}
			}
		}

		OrderRequiredVO vo=queryItemInfo(order);
        if (vo != null) {
        	if(isTicketBU(order)) {
        		vo.setIdNumType(orderRequiredClientService.checkTicketBxCountForCredentNumType(vo, countTotalBx(order)));
        	}
        	
        }
        if(BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(order.getCategoryId())){
        	vo.setTravNumType("TRAV_NUM_ONE");
        	vo.setPhoneType("TRAV_NUM_ONE");
        	
        }
        
        //游玩人人数
		model.addAttribute("personNum", personNum);
		//下单必填项
		model.addAttribute("orderRequiredvO", vo);
		//联系人为null,取第一个游玩人
		model.addAttribute("contactPerson", order.getContactPerson());
		//紧急联系人
		model.addAttribute("emergencyPerson", emergencyPerson);
		//游玩人
		Collections.sort(order.getOrdTravellerList(), new Comparator<OrdPerson>() {
			@Override
			public int compare(OrdPerson o1, OrdPerson o2) {
				if(o1.getOrdPersonId()>o2.getOrdPersonId()){
					return 1;
				}
				return -1;
			}
		});
		//如果是游玩人名称为“待填写” 就置空
		if(order.getOrdTravellerList()!=null&&!order.getOrdTravellerList().isEmpty()){
			for (OrdPerson ordPerson : order.getOrdTravellerList()) {
				if(ordPerson.getFullName()!=null){
					if(ordPerson.getFullName().equals("待填写")){
						ordPerson.setFullName("");
					}
				}
				
			}
		}

		//计算子订单产品种类 是门票的订单个数
		int productOrderItemCount = 0;
		LOG.info("子订单产品种类=门票的订单个数 orderItemSize:" + order.getOrderItemList().size());
		if (order.getOrderItemList() != null) {
			for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
				if (ordOrderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() || ordOrderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() || ordOrderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()) {
					productOrderItemCount++;
				}
			}
		}

		model.addAttribute("tavellerList", order.getOrdTravellerList());
		//主订单包含的子订单列表
		model.addAttribute("orderItemList", order.getOrderItemList());
		//电子合同
		model.addAttribute("hasContractOrder",CollectionUtils.isNotEmpty(order.getOrdTravelContractList()));
		//常用联系人
		model.addAttribute("personList", null);
		model.addAttribute("order", order);
		model.addAttribute("travellerDelayFlag", order.getTravellerDelayFlag());
		model.addAttribute("travellerLockFlag", order.getTravellerLockFlag());
		//model.addAttribute("travellerDelayFlag", "Y");
		
		if(StringUtil.isNotEmptyString(isSupplyFlag) && "Y".equals(isSupplyFlag)){
			model.addAttribute("isSupplyFlag", isSupplyFlag);
			return "/order/orderStatusManage/allCategory/showSupplyTourist";
		}
		//种类是门票  单独做重复证件校验处理
		if (order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() || order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() || order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()) {
			model.addAttribute("productOrderItemCount", productOrderItemCount);
			return "/order/orderStatusManage/allCategory/showUpdateTicketTourist";
		}

		return "/order/orderStatusManage/allCategory/showUpdateTourist";
	}

    private boolean isTicketBU(OrdOrder order) {
        return BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId());
    }
	
	/**
	 * 修改订单游玩人信息
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/order/update/updateTourist.do") 
	@ResponseBody
	public Object updateTourist(OrdOrderPersonVO orderPersonVO,Long orderId,ModelMap model, HttpServletRequest request) throws BusinessException{
		PermUser user = (PermUser) ServletUtil.getSession(
				HttpServletLocalThread.getRequest(),
				HttpServletLocalThread.getResponse(),
				Constant.SESSION_BACK_USER);
		LOG.info("updateTouristOperator"
				+ (user == null ? "no login!" : user.getUserName())+",orderId:"+orderId);
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(orderPersonVO != null){
			List<OrdPerson> travellers = orderPersonVO.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(OrdPerson person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			
			if(LOG.isDebugEnabled()){
				JSONObject obj = JSONObject.fromObject(orderPersonVO);
				LOG.debug(obj.toString());
			}
			
			
			for (OrdOrderItem ordOrderItem : orderItemList) {
				List<OrdItemPersonRelation> relationList=ordOrderItem.getOrdItemPersonRelationList();
					if(relationList!=null&&relationList.size()>0){
						Map<String, ItemPersonRelation>  relationMap=orderPersonVO.getPersonRelationMap();
						ItemPersonRelation itemRelation=relationMap.get("ORDERITEM_"+ordOrderItem.getOrderItemId());
						if(itemRelation!=null){
							List<OrdItemPersonRelation> ordItemPersonRelationList=itemRelation.getOrdItemPersonRelationList();
							if(ordItemPersonRelationList==null){
								throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
							}
							for(Iterator<OrdItemPersonRelation> it=ordItemPersonRelationList.iterator();it.hasNext();){
								OrdItemPersonRelation rr = it.next();
								if(rr.getOrdPersonId()==null){
									it.remove();
								}
							}
							if(ordItemPersonRelationList.size()!=relationList.size()){
								throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
							}
						}else{
							throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
						}
					}
				}
			final String ORDER_UPDATE_REPEAT_KEY = "order_update_repeat_";
			String key = ORDER_UPDATE_REPEAT_KEY+this.getClass()+orderId.toString();
            ResultHandle resultHandle = null;
            String flag = request.getParameter("updateSendContract");
            String trallverLog ="";
            List<OrdPerson> newTrallver = orderPersonVO.getTravellers();
        	if(MemcachedUtil.getInstance().addSynchronized(key,120,"true")){
        		for (OrdPerson ordPerson : newTrallver) {
        			OrdPerson oldOrdPerson =ordPersonService.findOrdPersonById(ordPerson.getOrdPersonId());

        			if(oldOrdPerson!=null){
        				trallverLog=getTrallverLog(trallverLog,ordPerson, oldOrdPerson);
        			}else{
        				LOG.info("=======ordPerson.getOrdPersonId:"+ordPerson.getOrdPersonId());	
        			}

				}
        		resultHandle =orderLocalService.updateOrderperonTravellerDelayFlag(orderId, orderPersonVO,flag);
        	}else{
	    		LOG.info("重复提交，请等待两分钟后再提交，orderId="+orderId);
	    	}
        	if(resultHandle==null){
        		msg.raise("重复提交，请等待两分钟后再提交");
        	}else if(resultHandle.isFail()){
				msg.raise("修改游玩人信息失败");
			}else{
				String loginUserId=this.getLoginUserId();
				
				if(flag != null && "Y".equals(flag)){
					
					//如果是游玩人后置订单
					if("Y".equals(order.getTravellerDelayFlag())){
						//如果是游玩人未锁定的订单
						if(order.getTravellerLockFlag().isEmpty() || "N".equals(order.getTravellerLockFlag())){							
							LOG.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===order.getCategoryId():"+order.getCategoryId()+"==order.getMainOrderItemProductType():"+order.getMainOrderItemProductType());							
							// 改变订单游玩人锁定状态
							int re=ordOrderTravellerService.updateOrderLockTraveller(orderId);

							//推动工作流
							LOG.info("OrderDetailUpdateAction:orderId:"+order.getOrderId()+"===order.getCategoryId():"+order.getCategoryId()+"==start===");
							orderLocalService.travellerLockAudit(orderId, getLoginUserId());
							LOG.info("OrderDetailUpdateAction:orderId:"+order.getOrderId()+"===order.getCategoryId():"+order.getCategoryId()+"==end===");

							if(re>0){
								order.setTravellerLockFlag("Y");
							}
						}


						
					}
					
					//发送变更单
					/*if("Y".equals(order.getTravellerDelayFlag())&&"Y".equals(order.getTravellerLockFlag())){
						String addition="";
						ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(order.getMainOrderItem().getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
						if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
							SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
							if("Y".equals(suppGoods.getFaxFlag())){
								Long faxRuleId= suppGoods.getFaxRuleId();
								if(faxRuleId!=null&&!faxRuleId.equals(0L)){
									ResultHandleT<SuppFaxRule> resultHandleSuppFaxRule = suppFaxClientService.findSuppFaxRuleById(faxRuleId);
									String toFax=resultHandleSuppFaxRule.getReturnContent().getFax();
									OrdOrderItem orderItem=orderUpdateService.getOrderItem(order.getMainOrderItem().getOrderItemId());
									ResultHandle result=orderStatusManageService.manualSendOrderItemFax(orderItem, toFax, "", getLoginUserId(), EbkCertif.EBK_CERTIFICATE_TYPE.getCnName("CHANGE"));
									String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
									if (result.isSuccess()) {
										if ("Y".equals(faxFlag)) {
											addition="CHANGE"+"_"+toFax;
										}else if("N".equals(faxFlag)){
											addition="CHANGE";
										}
										LOG.info("===orderEcontractGeneratorService.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);		
										orderLocalService.sendOrderItemSendFaxMsg(orderItem,addition);
										LOG.info("===orderEcontractGeneratorService.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);
									}
								}
							}else{
								OrdOrderItem orderItem=orderUpdateService.getOrderItem(order.getMainOrderItem().getOrderItemId());
								LOG.info("===orderEcontractGeneratorService.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);		
								orderLocalService.sendOrderItemSendFaxMsg(orderItem,"CHANGE");
								LOG.info("===orderEcontractGeneratorService.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);		
							}
						}
					}*/
					
					LOG.info("===orderEcontractGeneratorService.generateEcontract:orderId:"+order.getOrderId());		
					//生成并发送合同 生成合同需要判断游玩人是否锁定，所以先锁定后再生成合同
					orderEcontractGeneratorService.generateEcontract(orderId, loginUserId);
					//发送短信，需要有合同编号，先生成合同再发送
					if("Y".equals(order.getTravellerDelayFlag())){
						if(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==order.getCategoryId().longValue()
								||BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().longValue()==order.getCategoryId().longValue()
								||BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().longValue()==order.getCategoryId().longValue()
								||(order.getSubCategoryId() != null && BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId().longValue() && !order.isContainApiFlightTicket())
								||(order.getSubCategoryId() != null && BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().longValue()==order.getSubCategoryId().longValue())){
								//确定游玩人后置订单锁定模板
								LOG.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===>>PAY_PAYED_DELAY_TRAVELLER_CONFIRM==游玩人后置订未锁定模板===");
								iOrderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM);
								LOG.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===>>PAY_PAYED_DELAY_TRAVELLER_CONFIRM==游玩人后置订单锁定模板=end==");
						}
					}
					
				}
				
				//日志
				Map<String, String> logMap = getChangeLogMap(order, orderPersonVO);
				LOG.info("updateTourist==logMap:"+logMap+" ,trallverLog:"+trallverLog+",loginUserId"+loginUserId);
				//添加日志
				if(!trallverLog.equals("")){
					if("N".equals(flag)){
						if(logMap.get("remark") != null) {
							lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
									orderId, 
									orderId, 
									loginUserId, 
									trallverLog, 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[修改游客信息-"+logMap.get("infoType")+"]",
									"修改游客信息");
						}
					}else{
						if("Y".equals(order.getTravellerDelayFlag())){
							lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
									orderId, 
									orderId, 
									loginUserId, 
									trallverLog+",锁定出游人并更新发送合同", 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[锁定出游人并更新发送合同]",
									"锁定出游人并更新发送合同");
						}else{
							lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
									orderId, 
									orderId, 
									loginUserId, 
									trallverLog+",重新生成合同并发送", 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
									ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[保存并发送合同]",
									"保存并发送合同");
						}
					}	
				/*if(logMap.get("remark") != null && logMap.get("remark").length() > 0) {
					//添加日志
					if(!trallverLog.equals("")){
						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
								orderId, 
								orderId, 
								loginUserId, 
								trallverLog, 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[修改游客信息-"+logMap.get("infoType")+"]",
								"");
					}
				}*/
				}
			}
		}catch(IllegalArgumentException ex){
			LOG.error("===========updateTourist=========IllegalArgumentException,ex is", ex);
			msg.raise(ex.getMessage());
		}catch(Exception ex){
			LOG.error("===========updateTourist=========,ex is", ex);
			msg.raise("修改游玩人信息发生异常:"+ex.getMessage());
		}
		return msg;
	}
	
	/**
	 * 补全订单游玩人信息
 	 * @param orderPersonVO
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/ord/order/supply/supplyTourist.do") 
	@ResponseBody
	public Object supplyTourist(OrdOrderPersonVO orderPersonVO,Long orderId) throws BusinessException{
		PermUser user = (PermUser) ServletUtil.getSession(
				HttpServletLocalThread.getRequest(),
				HttpServletLocalThread.getResponse(),
				Constant.SESSION_BACK_USER);
		LOG.info("supplyTourist" + (user == null ? "no login!" : user.getUserName())+",orderId:"+orderId);
		List<OrdPerson> travellers = new ArrayList<OrdPerson>();
		
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			
			if(LOG.isDebugEnabled()){
				JSONObject obj = JSONObject.fromObject(orderPersonVO);
				LOG.debug(obj.toString());
			}
			
			for (OrdOrderItem ordOrderItem : orderItemList) {
				List<OrdItemPersonRelation> relationList = ordOrderItem.getOrdItemPersonRelationList();
				if(relationList!=null&&relationList.size() > 0){
					Map<String, ItemPersonRelation>  relationMap = orderPersonVO.getPersonRelationMap();
					ItemPersonRelation itemRelation = relationMap.get("ORDERITEM_"+ordOrderItem.getOrderItemId());
					if(itemRelation!=null){
						List<OrdItemPersonRelation> ordItemPersonRelationList=itemRelation.getOrdItemPersonRelationList();
						if(ordItemPersonRelationList == null){
							throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
						}
						for(Iterator<OrdItemPersonRelation> it = ordItemPersonRelationList.iterator();it.hasNext();){
							OrdItemPersonRelation rr = it.next();
							if(rr.getOrdPersonId() == null){
								it.remove();
							}
						}
						if(ordItemPersonRelationList.size() != relationList.size()){
							throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
						}
					}else{
						throw new IllegalArgumentException(ordOrderItem.getProductName()+" 预订数量与关联游玩人数不一致.");
					}
				}
			}
            ResultHandle resultHandle = null;
            
            if(orderPersonVO != null){
       			travellers = orderPersonVO.getTravellers();
       			if(travellers != null && travellers.size() > 0){
	       			for (OrdPerson ordPerson : travellers) {
	       				//身份证输入规范中的字母为大写，所以在此统一转为大写
	       				if(ordPerson != null && !StringUtil.isEmptyString(ordPerson.getIdNo())){
	       					ordPerson.setIdNo(ordPerson.getIdNo().toUpperCase());
						}
	       				ordPerson.setObjectId(orderId);
	       				ordPerson.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
	       				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
	       				resultHandle = orderLocalService.supplyDestBuAccTrav(ordPerson);
					}
       			}
       		}
	       	
	       	if(resultHandle.isFail()){
				msg.raise("补全游玩人信息失败");
			}
		}catch(IllegalArgumentException ex){
			LOG.error("===========supplyTourist=========IllegalArgumentException,ex is", ex);
			msg.raise(ex.getMessage());
		}catch(Exception ex){
			LOG.error("===========supplyTourist=========,ex is", ex);
			msg.raise("补全游玩人信息发生异常:"+ex.getMessage());
		}
		return msg;
	}

	//日志
	private Map<String, String> getChangeLogMap(OrdOrder order, OrdOrderPersonVO orderPersonVO) {
		Map<String, String> logMap = new HashMap<String, String>();
		try {
			Map<Long, OrdPerson> ordPersonMap = new HashMap<Long, OrdPerson>();
			for(OrdPerson ordPerson : order.getOrdPersonList()) {
				ordPersonMap.put(ordPerson.getOrdPersonId(), ordPerson);
			}

			StringBuffer emerLog = new StringBuffer();
			OrdPerson newEmerPerson = orderPersonVO.getEmergencyPerson();
			String emerRemark = "";
			if(newEmerPerson != null) {
				OrdPerson oldEmerPerson = ordPersonMap.get(newEmerPerson.getOrdPersonId());
				if (oldEmerPerson!=null) {
					emerLog.append(ComLogUtil.getChangeLog("联系人", oldEmerPerson.getFullName(), newEmerPerson.getFullName()));
					emerLog.append(ComLogUtil.getChangeLog("联系人手机", oldEmerPerson.getMobile(), newEmerPerson.getMobile()));
				}else {
					emerLog.append(ComLogUtil.getChangeLog("联系人", null, newEmerPerson.getFullName()));
					emerLog.append(ComLogUtil.getChangeLog("联系人手机",null, newEmerPerson.getMobile()));
				}
				
				if(emerLog.length() > 0) {
					emerRemark = "紧急联系人：" + emerLog.substring(1) + "<br/>";
				}
			}
			
			StringBuffer contactStr = new StringBuffer();
			OrdPerson newContactPerson = orderPersonVO.getContact();
			String contactRemark = "";
			if(newContactPerson != null) {
				OrdPerson oldContactPerson = ordPersonMap.get(newContactPerson.getOrdPersonId());
				if(oldContactPerson!=null){
					contactStr.append(ComLogUtil.getChangeLog("联系人", oldContactPerson.getFullName(), newContactPerson.getFullName()));
					contactStr.append(ComLogUtil.getChangeLog("联系人手机", oldContactPerson.getMobile(), newContactPerson.getMobile()));	
					contactStr.append(ComLogUtil.getChangeLog("邮箱地址", oldContactPerson.getEmail(), newContactPerson.getEmail()));

				}else {
					contactStr.append(ComLogUtil.getChangeLog("联系人",null, newContactPerson.getFullName()));
					contactStr.append(ComLogUtil.getChangeLog("联系人手机",null, newContactPerson.getMobile()));	
					contactStr.append(ComLogUtil.getChangeLog("邮箱地址", null, newContactPerson.getEmail()));
				}
			
				if(contactStr.length() > 0) {
					contactRemark = "订单联系人：" + contactStr.substring(1) + "<br/>";
				}
			}
			
			String travellerRemark = "";
			List<OrdPerson> travellers = orderPersonVO.getTravellers();
			if(CollectionUtils.isNotEmpty(travellers)) {
				for(int i = 0; i < travellers.size(); i++) {
					StringBuffer travellerStr = new StringBuffer();
					OrdPerson newTraveller = travellers.get(i);
					OrdPerson oldTraveller = ordPersonMap.get(newTraveller.getOrdPersonId());
					travellerStr.append(ComLogUtil.getChangeLog("中文姓名", oldTraveller.getFullName(), newTraveller.getFullName()));
					travellerStr.append(ComLogUtil.getChangeLog("英文姓名", oldTraveller.getEnglishName(), newTraveller.getEnglishName()));
					travellerStr.append(ComLogUtil.getChangeLog("人群", ORDER_PERSON_PEOPLE_TYPE.getCnName(oldTraveller.getPeopleType()), ORDER_PERSON_PEOPLE_TYPE.getCnName(newTraveller.getPeopleType())));
					travellerStr.append(ComLogUtil.getChangeLog("手机号码", oldTraveller.getMobile(), newTraveller.getMobile()));
					travellerStr.append(ComLogUtil.getChangeLog("邮箱地址", oldTraveller.getEmail(), newTraveller.getEmail()));
					travellerStr.append(ComLogUtil.getChangeLog("证件类型-类型", ORDER_PERSON_ID_TYPE.getCnName(oldTraveller.getIdType()), ORDER_PERSON_ID_TYPE.getCnName(newTraveller.getIdType())));
					travellerStr.append(ComLogUtil.getChangeLog("证件类型-号码", oldTraveller.getIdNo(), newTraveller.getIdNo()));
					travellerStr.append(ComLogUtil.getChangeLog("证件附加-生日", DateUtil.formatDate(oldTraveller.getBirthday(), "yyyy-MM-dd"), DateUtil.formatDate(newTraveller.getBirthday(), "yyyy-MM-dd")));
					if(oldTraveller.getBirthday() != null) {
						travellerStr.append(ComLogUtil.getChangeLog("证件附加-性别", oldTraveller.getGenderName(), newTraveller.getGenderName()));
					}
					if(travellerStr.length() > 0) {
						travellerRemark += "游玩人-游玩人" + (i + 1) +"：" + travellerStr.substring(1) + "<br/>";
					}
				}
			}
			//紧急联系人&订单联系人&游玩人
			String infoType = "";
			if(StringUtil.isNotEmptyString(emerRemark)) {
				infoType += "紧急联系人&";
			}
			if(StringUtil.isNotEmptyString(contactRemark)) {
				infoType += "订单联系人&";
			}
			if(StringUtil.isNotEmptyString(travellerRemark)) {
				infoType += "游玩人&";
			}
			if(StringUtil.isNotEmptyString(infoType)) {
				infoType = infoType.substring(0, infoType.length() - 1);
			}
			
			logMap.put("infoType", infoType);
			logMap.put("remark", emerRemark + contactRemark + travellerRemark);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return logMap;
	}
	
	/**
	 * @author chenguangyao
	 * 拼装游玩人修改信息 新旧值对比
	 * @param 
	 * newOrdPerson 修改后游玩人
	 * oldOrdPerson 修改前游玩人
	 * */
	private String getTrallverLog(String oldLog,OrdPerson newOrdPerson,OrdPerson oldOrdPerson){
		
		StringBuffer logContent= new StringBuffer("");
		if(null != newOrdPerson && null != oldOrdPerson){
			logContent.append("修改了游玩人信息(");
			logContent.append(ComLogUtil.getChangeLog("中文名",oldOrdPerson.getFullName(),newOrdPerson.getFullName()));
			if(this.isNotNull(oldOrdPerson.getLastName(), oldOrdPerson.getFirstName()) || this.isNotNull(newOrdPerson.getLastName(), newOrdPerson.getFirstName())){
				logContent.append(ComLogUtil.getChangeLog("英文名",oldOrdPerson.getLastName()+"."+oldOrdPerson.getFirstName(),newOrdPerson.getLastName()+"."+newOrdPerson.getFirstName()));
			}
			logContent.append(ComLogUtil.getChangeLog("手机号码",oldOrdPerson.getMobile(),newOrdPerson.getMobile()));
			if(this.isNotNull(oldOrdPerson.getPeopleType(), newOrdPerson.getPeopleType())){
				logContent.append(ComLogUtil.getChangeLog("人群",oldOrdPerson.getPeopleTypeName(),newOrdPerson.getPeopleTypeName()));
			}
			if(this.isNotNull(oldOrdPerson.getIdType(), newOrdPerson.getIdType())){
				logContent.append(ComLogUtil.getChangeLog("证件类型",oldOrdPerson.getIdTypeName(),newOrdPerson.getIdTypeName()));
			}
			logContent.append(ComLogUtil.getChangeLog("证件号码",oldOrdPerson.getIdNo(),newOrdPerson.getIdNo()));
			logContent.append(ComLogUtil.getLogTxtDate("出生日期",oldOrdPerson.getBirthday(),newOrdPerson.getBirthday()));
			logContent.append(ComLogUtil.getChangeLog("性别",oldOrdPerson.getGenderName(),newOrdPerson.getGenderName()));
			logContent.append(ComLogUtil.getChangeLog("邮箱",oldOrdPerson.getEmail(),newOrdPerson.getEmail()));
			logContent.append(")");
		}
		return logContent.toString();
	}
	private boolean isNotNull(String oldValue,String newValue){
		return (null != oldValue&&!"".equals(oldValue))||(null != newValue&&!"".equals(newValue));
	}

	@RequestMapping(value = "/ord/order/update/showUpdateInsurePersonList")
	public String showUpdateInsurePersonList(Model model, HttpServletRequest request, Long orderId){
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdPerson> ordPersonList = order.getOrdPersonList();
		List<OrdPerson> insurePersonList = new ArrayList<OrdPerson>();
		int personNum=0;
//		if(CollectionUtils.isNotEmpty(order.getOrdTravellerList())){
//			personNum=order.getOrdTravellerList().size();
//			for(OrdPerson ordPerson : order.getOrdTravellerList()){
//				Map<String, Object> param = new HashMap<String, Object>();
//				param.put("orderId", ordPerson.getObjectId());
//				param.put("orderPersonId", ordPerson.getOrdPersonId());
//				List<OrdTravAdditionConf> ordTravAdditionConfList = ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param);
//				if(ordTravAdditionConfList!=null && ordTravAdditionConfList.size() > 0){
//					ordPerson.setOrdTravAdditionConf(ordTravAdditionConfList.get(0));
//				}
//			}
//		}
		
		if(CollectionUtils.isNotEmpty(ordPersonList)){
			for(OrdPerson ordPerson : ordPersonList){
				if (OrderEnum.ORDER_PERSON_TYPE.INSURER.name().equals(ordPerson.getPersonType())) {
					insurePersonList.add(ordPerson);
					personNum++;
//					Map<String, Object> param = new HashMap<String, Object>();
//					param.put("orderId", ordPerson.getObjectId());
//					param.put("orderPersonId", ordPerson.getOrdPersonId());
//					List<OrdTravAdditionConf> ordTravAdditionConfList = ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param);
//					if(ordTravAdditionConfList!=null && ordTravAdditionConfList.size() > 0){
//						ordPerson.setOrdTravAdditionConf(ordTravAdditionConfList.get(0));
//					}
				}
			}
		}
		
//		OrdPerson emergencyPerson=new OrdPerson();
//		if(CollectionUtils.isNotEmpty(ordPersonList)){
//			for (OrdPerson ordPerson : ordPersonList) {
//				if(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equals(ordPerson.getPersonType())){
//					emergencyPerson=ordPerson;
//				}
//			}
//		}

		OrderRequiredVO vo=queryItemInfo(order);
		vo.setTpFlag("N");
		//只显示证件类型：身份证，护照，港澳通行证，回乡证，台胞证，军官证
		vo.setIdFlag("Y");
		vo.setPassportFlag("Y");
		vo.setPassFlag("Y");
		vo.setHkResidentFlag("Y");
		vo.setTwResidentFlag("Y");
		vo.setOfficerFlag("Y");
		vo.setTwPassFlag("N");
		vo.setBirthCertFlag("N");
		vo.setHouseholdRegFlag("N");
		vo.setSoldierFlag("N");
		vo.setPhoneType("TRAV_NUM_ALL");
//        if (vo != null) {
//        	if(isTicketBU(order)) {
//        		vo.setIdNumType(orderRequiredClientService.checkTicketBxCountForCredentNumType(vo, countTotalBx(order)));
//        	}
//        	
//        }
//        if(BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(order.getCategoryId())){
//        	vo.setTravNumType("TRAV_NUM_ONE");
//        	vo.setPhoneType("TRAV_NUM_ONE");
//        	
//        }
        
        //游玩人人数
		model.addAttribute("personNum", personNum);
		//下单必填项
		model.addAttribute("orderRequiredvO", vo);
		//联系人为null,取第一个游玩人
		model.addAttribute("contactPerson", order.getContactPerson());
//		//紧急联系人
//		model.addAttribute("emergencyPerson", emergencyPerson);
		//游玩人
		Collections.sort(order.getOrdTravellerList(), new Comparator<OrdPerson>() {
			@Override
			public int compare(OrdPerson o1, OrdPerson o2) {
				if(o1.getOrdPersonId()>o2.getOrdPersonId()){
					return 1;
				}
				return -1;
			}
		});
		//如果是游玩人名称为“待填写” 就置空
		if(order.getOrdTravellerList()!=null&&!order.getOrdTravellerList().isEmpty()){
			for (OrdPerson ordPerson : order.getOrdTravellerList()) {
				if(ordPerson.getFullName()!=null){
					if(ordPerson.getFullName().equals("待填写")){
						ordPerson.setFullName("");
					}
				}
				
			}
		}
		
		model.addAttribute("tavellerList", insurePersonList);
		//主订单包含的子订单列表
		model.addAttribute("orderItemList", order.getOrderItemList());
		//电子合同
//		model.addAttribute("hasContractOrder",CollectionUtils.isNotEmpty(order.getOrdTravelContractList()));
		//常用联系人
		model.addAttribute("personList", null);
		model.addAttribute("order", order);
//		model.addAttribute("travellerDelayFlag", order.getTravellerDelayFlag());
//		model.addAttribute("travellerLockFlag", order.getTravellerLockFlag());
		//model.addAttribute("travellerDelayFlag", "Y");
		
		
		return "/order/orderStatusManage/allCategory/showUpdateInsurePersonList";
	}

	/**
	 * 修改订单游玩人信息
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/order/update/updateInsurePersonList.do") 
	@ResponseBody
	public Object updateInsurePersonList(OrdOrderPersonVO orderPersonVO,Long orderId,ModelMap model, HttpServletRequest request) throws BusinessException{
		PermUser user = (PermUser) ServletUtil.getSession(
				HttpServletLocalThread.getRequest(),
				HttpServletLocalThread.getResponse(),
				Constant.SESSION_BACK_USER);
		LOG.info("updateInsurePersonListOperator"
				+ (user == null ? "no login!" : user.getUserName())+",orderId:"+orderId);
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(orderPersonVO != null){
			//List<OrdPerson> travellers = orderPersonVO.getTravellers();
			List<OrdPerson> insurers = orderPersonVO.getInsurers();
			if(insurers != null && insurers.size() > 0){
				for(OrdPerson person : insurers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}

		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);

			if(LOG.isDebugEnabled()){
				JSONObject obj = JSONObject.fromObject(orderPersonVO);
				LOG.debug(obj.toString());
			}

			final String ORDER_UPDATE_REPEAT_KEY = "order_update_repeat_insurer_";
			String key = ORDER_UPDATE_REPEAT_KEY+this.getClass()+orderId.toString();
			ResultHandle resultHandle = null;
			String insurerLog = "";
			List<OrdPerson> newTrallver = orderPersonVO.getInsurers();
			//if(MemcachedUtil.getInstance().addSynchronized(key,120,"true")){
				for (OrdPerson ordPerson : newTrallver) {
							OrdPerson oldOrdPerson = ordPersonService.findOrdPersonById(ordPerson.getOrdPersonId());

					if(oldOrdPerson!=null){
						String insurerLogTemp = getInsurerLog(insurerLog,ordPerson, oldOrdPerson);
						if (!"修改了投保人信息()".equals(insurerLogTemp)) {
							insurerLog = insurerLog + insurerLogTemp;
						}
					}else{
						LOG.info("=======ordPerson.getOrdPersonId:"+ordPerson.getOrdPersonId());	
					}

				}
				resultHandle = orderLocalService.updateOrdInsurers(orderId, orderPersonVO);
			//}else{
				LOG.info("重复提交，请等待两分钟后再提交，orderId="+orderId);
			//}
			if(resultHandle==null){
				msg.raise("重复提交，请等待两分钟后再提交");
			}else if(resultHandle.isFail()){
				msg.raise("修改游玩人信息失败");
			}else{
				String loginUserId = this.getLoginUserId();

				//日志
				Map<String, String> logMap = getChangeLogMap(order, orderPersonVO);
				LOG.info("updateInsurePersonList==logMap:" + logMap + " ,insurerLog:" + insurerLog + ", loginUserId" + loginUserId);
				//添加日志
				if(!insurerLog.equals("")){
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, orderId, orderId, 
						loginUserId, insurerLog + ",重新投保", 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "[保存并重新投保]", "保存并重新投保");
				}
			}
		}catch(IllegalArgumentException ex){
			LOG.error("===========updateInsurePersonList=========IllegalArgumentException,ex is", ex);
			msg.raise(ex.getMessage());
		}catch(Exception ex){
			LOG.error("===========updateInsurePersonList=========,ex is", ex);
			msg.raise("修改保险人信息发生异常:"+ex.getMessage());
		}
		return msg;
	}

	/**
	 * @author 
	 * 拼装投保人修改信息 新旧值对比
	 * @param 
	 * newOrdPerson 修改后投保人
	 * oldOrdPerson 修改前投保人
	 * */
	private String getInsurerLog(String oldLog, OrdPerson newOrdPerson, OrdPerson oldOrdPerson){
		
		StringBuffer logContent= new StringBuffer("");
		if(null != newOrdPerson && null != oldOrdPerson){
			logContent.append("修改了投保人信息(");
			logContent.append(ComLogUtil.getChangeLog("中文名",oldOrdPerson.getFullName(),newOrdPerson.getFullName()));
			if(this.isNotNull(oldOrdPerson.getLastName(), oldOrdPerson.getFirstName()) || this.isNotNull(newOrdPerson.getLastName(), newOrdPerson.getFirstName())){
				logContent.append(ComLogUtil.getChangeLog("英文名",oldOrdPerson.getLastName()+"."+oldOrdPerson.getFirstName(),newOrdPerson.getLastName()+"."+newOrdPerson.getFirstName()));
			}
			logContent.append(ComLogUtil.getChangeLog("手机号码",oldOrdPerson.getMobile(),newOrdPerson.getMobile()));
			if(this.isNotNull(oldOrdPerson.getPeopleType(), newOrdPerson.getPeopleType())){
				logContent.append(ComLogUtil.getChangeLog("人群",oldOrdPerson.getPeopleTypeName(),newOrdPerson.getPeopleTypeName()));
			}
			if(this.isNotNull(oldOrdPerson.getIdType(), newOrdPerson.getIdType())){
				logContent.append(ComLogUtil.getChangeLog("证件类型",oldOrdPerson.getIdTypeName(),newOrdPerson.getIdTypeName()));
			}
			logContent.append(ComLogUtil.getChangeLog("证件号码",oldOrdPerson.getIdNo(),newOrdPerson.getIdNo()));
			logContent.append(ComLogUtil.getLogTxtDate("出生日期",newOrdPerson.getBirthday(), oldOrdPerson.getBirthday()));
			logContent.append(ComLogUtil.getChangeLog("性别",oldOrdPerson.getGenderName(),newOrdPerson.getGenderName()));
			logContent.append(ComLogUtil.getChangeLog("邮箱",oldOrdPerson.getEmail(),newOrdPerson.getEmail()));
			logContent.append(")");
		}
		return logContent.toString();
	}
}
