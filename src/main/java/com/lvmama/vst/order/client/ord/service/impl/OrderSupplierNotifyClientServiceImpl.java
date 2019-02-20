package com.lvmama.vst.order.client.ord.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.vst.back.prom.po.PromFlow;
import com.lvmama.vst.order.dao.OrdTicketPerformDao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.ord.service.CategoryOrderService;
import com.lvmama.vst.back.client.ord.service.OrderSupplierNotifyService;
import com.lvmama.vst.back.client.passport.service.RaiyiService;
import com.lvmama.vst.back.client.prom.service.PromBuyPresenterService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.passport.po.PassReport;
import com.lvmama.vst.back.prom.po.PromBuyPresenter;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.PassReportDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderHandleService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated;

@Component("orderSupplierNotifyServiceRemote")
public class OrderSupplierNotifyClientServiceImpl implements OrderSupplierNotifyService{
	private static final Logger LOG = LoggerFactory.getLogger(OrderSupplierNotifyClientServiceImpl.class);

	@Autowired
	private ISupplierOrderHandleService supplierOrderHandleService;

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;

	@Resource
	private PassReportDao passReportDao;

	@Autowired
	protected IComplexQueryService complexQueryService;

	@Resource
	private RaiyiService raiyiService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Resource
	private CategoryOrderService categoryOrderServiceRemote;

	@Resource
	private PromBuyPresenterService promBuyPresenterServiceRemote;

	@Autowired
	private OrdTicketPerformDao ordTicketPerformDao;

	@Override
	public ResultHandle orderNotify(SuppOrderRelated suppOrderRelated) {
		ResultHandle resultHandle = null;
		try {
			if (suppOrderRelated != null) {
				//返回状态
				String status = suppOrderRelated.getStatus();
				LOG.info("OrderSupplierNotifyClientServiceImpl.orderNotify: status=" + status + ",orderId=" + suppOrderRelated.getOrderId());
				//资源审核通过
				if (OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(status)) {
					resultHandle = supplierOrderHandleService.updateAmpleResourceStatus(suppOrderRelated);
				//未执行
				} else if (OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name().equals(status)) {
					resultHandle = supplierOrderHandleService.updateUnperformStatus(suppOrderRelated);
				//已执行
				} else if (OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name().equals(status)) {
					resultHandle = supplierOrderHandleService.updatePerformStatus(suppOrderRelated);
				//订单取消（废单重下）
				} else if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(status)) {
					resultHandle = supplierOrderHandleService.updateOrderCancelStatus(suppOrderRelated);
				} else {
					resultHandle = new ResultHandle();
					resultHandle.setMsg("SuppOrderRelated.status[" + status + "] error code.");

					LOG.debug("OrderSupplierNotifyClientServiceImpl.orderNotify: SuppOrderRelated.status[" + status + "] error code.");
				}
			} else {
				resultHandle = new ResultHandle();
				resultHandle.setMsg("SuppOrderRelated is null.");

				LOG.debug("OrderSupplierNotifyClientServiceImpl.orderNotify: suppOrderRelated=null.");
			}
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			resultHandle = new ResultHandle();
			resultHandle.setMsg(ex.getMessage());

			LOG.debug("OrderSupplierNotifyClientServiceImpl.orderNotify: Exception,msg=" + ex.getMessage());
		}

		if (resultHandle != null) {
			LOG.debug("OrderSupplierNotifyClientServiceImpl.orderNotify: resultHandle.isSuccess=" + resultHandle.isSuccess() + ",resultHandle.getMsg=" + resultHandle.getMsg());
		}

		return resultHandle;
	}

	@Override
	public ResultHandle orderTicketPerform(OrdTicketPerform ordTicketPerform) {
		// TODO Auto-generated method stub
		ResultHandleT<OrdPassCode> resultHandle = null;
		try {
			LOG.info("orderItemId:{}ticket perform,adult:{},child:{}",new Object[]{ordTicketPerform.getOrderItemId(),ordTicketPerform.getActualAdult(),ordTicketPerform.getActualChild()});
			resultHandle = supplierOrderHandleService.saveOrdTicketPerform(ordTicketPerform);
			if(resultHandle.isSuccess()&&!resultHandle.hasNull()){
				String addition = resultHandle.getReturnContent().getCheckingId()+"_"+resultHandle.getReturnContent().getAddCode()+
						          "_"+ordTicketPerform.getDeviceNo()+"_"+ordTicketPerform.getQuantity()+"_"+ordTicketPerform.getOrderId();
				orderMessageProducer.sendMsg(MessageFactory.newOrderItemPerform(ordTicketPerform.getOrderItemId(),addition));
				//发送JMS消息至结算
				orderMessageProducer.sendMsg(MessageFactory.newItemPerformSettle(ordTicketPerform.getOrderItemId(),""));

			}
			if(resultHandle.isSuccess()){
				//对于门票包含买赠流量且赠送节点是游玩后的订单若订单已消费完开始赠送
				dealActivityGiveFlow(ordTicketPerform);
				//所有门票履行消息
				OrdOrderItem orderItem = orderUpdateService.getOrderItem(ordTicketPerform.getOrderItemId());
				OrdTicketPerform newOrdTicketPerform = ordTicketPerformDao.selectByOrderItem(ordTicketPerform.getOrderItemId());
				Long performQuantity = 0L;
				if(isLocalPlayAndNoAdultChild(orderItem.getCategoryId())){
					performQuantity = ((newOrdTicketPerform.getActualAdult() == null ? 0: newOrdTicketPerform.getActualAdult())
							+ (newOrdTicketPerform.getActualChild() == null ? 0: newOrdTicketPerform.getActualChild()));
				}else{
					if(orderItem.getAdultQuantity() + orderItem.getChildQuantity() != 0l){
						performQuantity = ((newOrdTicketPerform.getActualAdult() == null ? 0: newOrdTicketPerform.getActualAdult())
								+ (newOrdTicketPerform.getActualChild() == null ? 0: newOrdTicketPerform.getActualChild()))
								/ (orderItem.getAdultQuantity() + orderItem.getChildQuantity());
					}
				}
				
				String addition = orderItem.getQuantity()+"_"+performQuantity+"_"+orderItem.getPerformStatus();//订购份数_已使用份数_使用状态
				LOG.info("sendMsg newTicketOrderItemPerform,addition=" + addition);
				orderMessageProducer.sendMsg(MessageFactory.newTicketOrderItemPerform(ordTicketPerform.getOrderItemId(),addition));
			}
			LOG.info("orderItemId:{}ticket result:{}",new Object[]{ordTicketPerform.getOrderItemId(),resultHandle.isSuccess()});
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			resultHandle = new ResultHandleT<OrdPassCode>();
			resultHandle.setMsg(ex.getMessage());

			LOG.error("OrderSupplierNotifyClientServiceImpl.orderTicketPerform: Exception,msg=" + ex.getMessage());
		}
		return resultHandle;
	}



	@Override
	public ResultHandleT<List<OrdOrderItem>> checkOrderTicketValid(final String addCode) {
		// TODO Auto-generated method stub
		LOG.info("addCode:"+addCode);
		ResultHandleT<List<OrdOrderItem>> resultHandle = null;
		try {
				resultHandle = supplierOrderHandleService.checkOrderTicketValid(addCode);
			} catch (Exception ex) {
				LOG.error(ExceptionFormatUtil.getTrace(ex));
				resultHandle = new ResultHandleT<List<OrdOrderItem>>();
				resultHandle.setMsg(ex.getMessage());

				LOG.error("OrderSupplierNotifyClientServiceImpl.checkOrderTicketValid: Exception,msg=" + ex.getMessage());
			}
		return resultHandle;
	}

	@Override
	public ResultHandle passCodeNotify(List<OrdPassCode> codeList) {
		ResultHandle handle =new ResultHandle();
		try{
			supplierOrderHandleService.savePassCode(codeList);
			if(codeList!=null){
				for(OrdPassCode code:codeList){
					LOG.info("二维码申请成功后回调发送消息,orderItemId"+code.getOrderItemId());
					LOG.info("passExtid:"+code.getPassExtid());
					orderMessageProducer.sendMsg(MessageFactory.newPasscodeApplyNotifyMessage(code.getOrderItemId()));
				}
			}
		}catch(Exception ex){
			handle.setMsg(ex);
			LOG.error(ExceptionFormatUtil.getTrace(ex));
		}
		return handle;
	}
	
	@Override
	public ResultHandle passCodeNotify4FillPicFilePath(List<OrdPassCode> codeList) {
		ResultHandle handle =new ResultHandle();
		if(!CollectionUtils.isEmpty(codeList)){
			try{
				supplierOrderHandleService.updatePassCode(codeList);
				LOG.info("fill Qcode picFilePath end!");
			}catch(Exception ex){
				handle.setMsg(ex);
				LOG.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
		return handle;
	}


	/**
	 * 查询自主设备每天的通关统计数据
	 * @return
	 */
	@Override
	public List<PassReport> queryPassReportDataForDay(Long passPointId,Date date) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("passPointId", passPointId);
//		params.put("addCodes", addCodes);
		params.put("date", DateUtil.formatSimpleDate(date));
		List<PassReport> list = passReportDao.findList(params);
		return list;
	}

    private  OrdPerson getContract(OrdOrder order) {
        OrdPerson ordPerson = null;
        OrdPerson firstAdultOrdPerson = order.getFirstAdultTravellerPerson();
        OrdPerson firstOrdPerson = order.getFirstTravellerPerson();
        OrdPerson contactPerson = order.getContactPerson();
        if (firstAdultOrdPerson != null) {
            ordPerson = firstAdultOrdPerson;
        } else if (firstOrdPerson != null) {
            ordPerson = firstOrdPerson;
        } else {
            ordPerson = contactPerson;
        }
        if (StringUtils.isEmpty(ordPerson.getPhone()) && contactPerson != null) { // 第一游玩人没有手记号，取联系人手机号
            ordPerson.setPhone(contactPerson.getPhone());
        }
        return ordPerson;
    }

	/**
     * 对于门票包含买赠流量且赠送节点是游玩后的订单若订单已消费完开始赠送.
     * @param ordTicketPerform
     */
	private void dealActivityGiveFlow(OrdTicketPerform ordTicketPerform) {
		try {
			Long orderItemId=ordTicketPerform.getOrderItemId();
			LOG.info("raiyi dealActivityGiveFlow start orderItemId=="+orderItemId);
			OrdOrderItem ordOrderItem=orderUpdateService.getOrderItem(orderItemId);
			//如果品类是门票
			if(OrderUtils.isTicketByCategoryId((ordOrderItem.getCategoryId()))){
				//查询主订单信息
				Long orderId=ordOrderItem.getOrderId();
				List<Long> orderIds=new ArrayList<Long>();
				orderIds.add(orderId);
				LOG.info("raiyi buyflowcheck getorder orderId=="+orderId);
				List<OrdOrder> order=orderUpdateService.queryOrdorderByOrderIdList(orderIds);
				if(order!=null && order.size()>0){
					OrdOrder ordOrder=order.get(0);
					LOG.info("raiyi buyflowcheck getorder orderId=="+orderId+" success PerformStatus=="+ordOrder.getPerformStatus()+" CategoryId=="+ordOrder.getCategoryId());
					//因为只看纯门票的 子订单可能包含保险等等 且已使用的
					if(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name().equals(ordOrder.getPerformStatus()) && OrderUtils.isTicketByCategoryId(ordOrder.getCategoryId())){
						//调用根据订单号查询该订单是否参加流量活动
						Map<String, Object>  res=promBuyPresenterServiceRemote.selectPresentsByOrderId(orderId, 2L);
						if(res!=null && res.get("move")!=null){
							PromBuyPresenter move=(PromBuyPresenter) res.get("move");
							if(!haveEmpty(move.getFlowNoMove())){
								giveFlow(res);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.info("raiyi dealActivityGiveFlow error ItemId=="+ordTicketPerform.getOrderItemId()+" ,error== "+e.toString());
		}

	}
	/**
	 * 必须参数不能为空
	 * @param params
	 * @return
	 */
	public static boolean haveEmpty(Map<String,String> params){

		for(String value:params.values()){
			if(StringUtil.isEmptyString(value))
			return true;
		}
		return false;
	}
	  /**
     * 判断字符串是否有空
     * @param requestParams
     * @return
     */
    private boolean haveEmpty(String... requestParams){
        boolean flag = false;
        for(String param:requestParams){
           if(param==null || param.length()<=0){
               flag=true;
               break;
           }
        }
        return flag;
    }
	/**
	 * 充值流量
	 * @param params
	 */
	public void giveFlow(Map<String, Object> params) {
		try {
			LOG.info("OrderSupplierNotify giveFlow start "+DateUtil.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss"));
			StringBuilder sb=new StringBuilder();
			String orderId=String.valueOf(params.get("orderId"));
			PromBuyPresenter move=(PromBuyPresenter) params.get("move");
			PromBuyPresenter union=(PromBuyPresenter) params.get("union");
			PromBuyPresenter electric=(PromBuyPresenter) params.get("electric");
			sb.append("orderId="+orderId+"&");
			sb.append("movepromOrderActivityUserId="+move.getPromOrderActivityUserId()+"&moveactivity="+move.getPresentActivityId()+"&moveproductId="+move.getFlowNoMove());
			sb.append("unionpromOrderActivityUserId="+union.getPromOrderActivityUserId()+"&unionactivity="+union.getPresentActivityId()+"&unionproductId="+union.getFlowNoUnicom());
			sb.append("electricpromOrderActivityUserId="+electric.getPromOrderActivityUserId()+"&electricctivity="+electric.getPresentActivityId()+"&electricproductId="+electric.getFlowNoElectric());
			LOG.info("OrderSupplierNotify giveFlow start params=="+sb.toString());
			//获取游玩人
			LOG.info("OrderSupplierNotify giveFlow getcontract  start orderId=="+orderId);
			OrdOrder ordOrder=categoryOrderServiceRemote.getCategoryOrdOrder(Long.parseLong(orderId), BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCode());
			//包含买赠商品的订单已经全部使用调用赠送流量
			OrdPerson contract=getContract(ordOrder);
			String mobile=contract.getMobile();
			Map<String, String> request=new HashMap<String, String>();
			if(haveEmpty(mobile)){
				LOG.info("OrderSupplierNotify giveFlow getcontract  mobile is empty orderId=="+orderId);
			}else{
				request.put("mobile", mobile);
				LOG.info("OrderSupplierNotify productListByMobile start mobile=="+mobile);
				ResultHandleT<String>  res=raiyiService.productListByMobile(mobile);
				PromFlow promFlow=null;
				if(res.isSuccess()){
					promFlow=new PromFlow();
					PromBuyPresenter tempPromBuyPresenter=null;
					// 0：电信 1：移动 2：联通
					String operators=res.getReturnContent();
					LOG.info("OrderSupplierNotify productListByMobile end operators=="+operators);
					if("0".equals(operators)){
						request.put("productId", electric.getFlowNoElectric());
						request.put("partnerOrderNo", String.valueOf(electric.getPromOrderActivityUserId()));
						tempPromBuyPresenter=electric;
						promFlow.setFlowType("ELECTRIC");
					}else if("1".equals(operators)){
						request.put("productId", move.getFlowNoMove());
						request.put("partnerOrderNo", String.valueOf(move.getPromOrderActivityUserId()));
						tempPromBuyPresenter=move;
						promFlow.setFlowType("MOVE");
					}else if("2".equals(operators)){
						request.put("productId", union.getFlowNoUnicom());
						request.put("partnerOrderNo", String.valueOf(union.getPromOrderActivityUserId()));
						tempPromBuyPresenter=union;
						promFlow.setFlowType("UNICOM");
					}
					if(null!=tempPromBuyPresenter){
						promFlow.setPromOrderActivityUserId(tempPromBuyPresenter.getPromOrderActivityUserId().intValue());
						promFlow.setPresentId(tempPromBuyPresenter.getPresentBuyPresentId());
						if(StringUtil.isNotEmptyString(request.get("productId"))){
							promFlow.setPromFlowId(Long.parseLong(request.get("productId")));
						}
						savePromFlow(promFlow);
					}
					//调用充值流量接口
					LOG.info("OrderSupplierNotify giveFlow orderbuyflow start params=="+request.toString());
					ResultHandleT<String>  buyflowres=raiyiService.orderBuyFlow(request);
					if(buyflowres.isSuccess()){
						LOG.info("OrderSupplierNotify giveFlow orderbuyflow success orderId=="+orderId);
					}else{
						LOG.info("OrderSupplierNotify giveFlow orderbuyflow fail orderId=="+orderId);
					}

				}else{
					LOG.info("OrderSupplierNotify giveFlow productListByMobile  getoperators fail orderId=="+orderId+",msg=="+res.getMsg());
				}
			}

		} catch (Exception e) {
			LOG.info("OrderSupplierNotify giveFlow error=="+e.toString());
		}
	}
	/**
	 * 保存赠送流量记录
	 * @param promFlow
	 */
	private void savePromFlow(PromFlow promFlow){
		try {
			promBuyPresenterServiceRemote.insertPromFlow(promFlow);
		}catch (Exception e){
			LOG.info("OrderSupplierNotify insertPromFlow PromOrderActivityUserId=="+promFlow.getPromOrderActivityUserId()+ " ,error=="+e.toString());
		}
	}

	@Override
	public ResultHandleT<String> queryTicketPerformStatus(Long orderId) {
		ResultHandleT<String> resultHandleT = new ResultHandleT<String>();
		OrdOrder order=	complexQueryService.queryOrderByOrderId(orderId);
		String perFormStatus ="";
		if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())||
				BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())||
				BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
			LOG.info("queryTicketPerformStatus orderId:" +orderId);
			if(order!=null&&!"".equals(order.getPerformStatus())){
				perFormStatus= order.getPerformStatus();
			}else{
				//门票业务类订单使用状态
				List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>(); 
				List<OrdOrderItem> ordItemsList =order.getOrderItemList();
				//订单使用状态
				List<String> perFormStatusList = new ArrayList<String>();
				if(ordItemsList != null) {
					for (OrdOrderItem ordOrderItem : ordItemsList) {
						//门票业务类订单使用状态
						Map<String,Object> performMap = ordOrderItem.getContentMap();
						String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
						if (ProductCategoryUtil.isTicket(categoryCode)) {
							resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
							String performStatusName=OrderUtils.calPerformStatus(resultList,order,ordOrderItem);
							perFormStatusList.add(performStatusName);
						}
					}
				}
				perFormStatus =  OrderUtils.getMainOrderPerformStatusCode(perFormStatusList);
			}
			perFormStatus = OrderEnum.PERFORM_STATUS_TYPE.getCnName(perFormStatus);
		}
		resultHandleT.setReturnContent(perFormStatus);
		return resultHandleT;	
	}
	
	
	private boolean isLocalPlayAndNoAdultChild(Long categoryId){
		//当地玩乐：美食 娱乐 购物 交通接驳 
		if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(categoryId)||
			BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(categoryId)||
				BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(categoryId)||
					BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(categoryId)||
						BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;
	}
	

	
}
