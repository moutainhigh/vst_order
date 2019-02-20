package com.lvmama.vst.order.job;

import com.lvmama.log.util.LogTrackContext;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrdOrderPackClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CategoryUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.processer.sms.OrderTravelSms;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSmsSendService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * 履行前一天JOB
 * @author chenkeke
 *
 */
public class OrderPerformPreviousDayJob implements Runnable{

	private static final Log LOG = LogFactory.getLog(OrderPerformPreviousDayJob.class);
	@Autowired
	IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
	private OrdOrderPackClientService orderPackClientService;
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	@Resource
	private IVstOrderRouteService vstOrderRouteService;
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			int currHour = DateUtil.getHour(new Date());
			if(currHour < 12 || (currHour > 16 && currHour <= 18) || currHour > 22) {
				return;
			}
			LogTrackContext.initTrackNumber();
			LOG.info("OrderPerformPreviousDayJob Start");
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 12);
			if(currHour >= 19) {
				cal.set(Calendar.HOUR_OF_DAY, 19);
			}
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			
			List<OrdOrder> ordOrders= orderUpdateService.queryPerformPreviousDayOrderIdList(cal.getTime());
			List<Long> sentOrderIdList = new ArrayList<Long>();
			
			// 需要增加 路由大开关判断 &&　2017-09-20 add by zhujingfeng
			boolean isJobRouteToNewSys = vstOrderRouteService.isJobRouteToNewSys();
			LOG.info("isJobRouteToNewSys="+isJobRouteToNewSys);
			
			int sentCount = 0;
			for (OrdOrder ordOrder : ordOrders) {
				// 需要增加 路由大开关判断 &&　2017-09-20 add by zhujingfeng
				if(isJobRouteToNewSys && CategoryUtils.isHotelOrTicket(ordOrder.getCategoryId())){
					LOG.info("OrderPerformPreviousDayJob not execute,orderId="+ordOrder.getOrderId()+",categoryId="+ordOrder.getCategoryId());
					continue;
				}
				
				
				List<String> statusList = this.getPassCodeList(ordOrder);
				if(null != statusList && statusList.size()>0){
					LOG.info("statusList_size:"+statusList.size());
					//合并申码或者只有一个子订单
					if(statusList.size() == 1){
						if(!"APPLIED_SUCCESS".equals(statusList.get(0))){
							continue;
						}
					//独立申码多个子订单
					}else if(statusList.size() > 1){
						Boolean flag = false;
						for(String status : statusList){
							LOG.info("passCode#orderId:"+ordOrder.getOrderId()+",status:"+status);
							//多个子订单，有一个申码失败 就不发短信
							if(!"APPLIED_SUCCESS".equals(status)){
								flag = true;
								break;
							}
						}
						if(flag){
							continue;
						}
					}
					LOG.info("OrderPerformPreviousDayJob#orderId:"+ordOrder.getOrderId()+",有短信发送");
				}
				//发送短信常规-入住前一天提醒
				try {
					if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
						orderSmsSendService.sendSms(ordOrder.getOrderId(),OrdSmsTemplate.SEND_NODE.COMMON_ARRIVAL_DAY_BEFORE_REMIND);
					}else{	//新
						// 出游短信
						OrderTravelSms sms = new OrderTravelSms();
						//取到短信发送规则
						List<String> smsNodeList = sms.exeSmsRule(ordOrder);
						
						List<String> shortLineNodeList = new ArrayList<String>();
						//国内bu，属于跟团游-短线和当地游-短线发送此节点短信
						if (CommEnumSet.BU_NAME.LOCAL_BU.getCode().equals(ordOrder.getBuCode())) {
							LOG.info("属于国内BU，订单信息为ProductId = "+ordOrder.getProductId()+"OrderId"+ordOrder.getOrderId());
							if(ordOrder.getCategoryId().longValue() == 15 || ordOrder.getCategoryId().longValue() == 16){
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("orderId", ordOrder.getOrderId());
								List<OrdOrderPack> orderPackList = orderPackClientService.findOrdOrderPackList(params);
								ordOrder.setOrderPackList(orderPackList);
								LOG.info("根据productId查询product信息，productId = "+ordOrder.getProductId());
								if (ordOrder.getProductId()!=null) {
									ProdProduct product = prodProductClientService.findProdProductSimpleById(ordOrder.getProductId()).getReturnContent();
									LOG.info("prodProductClientService.findProdProductSimpleById，product = "+product);
									shortLineNodeList = sms.getShortLineNodeList(ordOrder,product);
									LOG.info("query getShortLineNodeList，shortLineNodeList = "+shortLineNodeList);
								}
							}
							//国内除了供应商打包的当地游和跟团游短线的订单需要发送关怀短信
							if(CollectionUtils.isEmpty(shortLineNodeList)){
								shortLineNodeList.add(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY.name());
							}
						}
						//出境跟团游，自由行，当地游
						if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(ordOrder.getBuCode())){
							if(ordOrder.getCategoryId().longValue() == 15 || ordOrder.getCategoryId().longValue() == 16|| ordOrder.getCategoryId().longValue() == 18) {
								shortLineNodeList.add(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY_OUBBOUNDBU.name());
							}
						}

						//邮轮
						if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(ordOrder.getBuCode())){
							if(ordOrder.getCategoryId().longValue() == 8) {
								shortLineNodeList.add(OrdSmsTemplate.SEND_NODE.PERFORM_PREVIOUS_DAY_SHIP.name());
							}
						}
						//合并节点
						if (CollectionUtils.isNotEmpty(shortLineNodeList)) {
							LOG.info("query shortLineNodeList size = "+shortLineNodeList.size());
							for (String smsNode : shortLineNodeList) {
								smsNodeList.add(smsNode);
							}
						}
						//有短信发送
						if(smsNodeList != null && smsNodeList.size() > 0){
							for(String smsNode : smsNodeList){
								LOG.info("ORDER ID IS SEND SMS YOUWANQIANYTIAN ="+ordOrder.getOrderId());
								orderSendSmsService.sendSms(ordOrder.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
							}
							sentCount ++;
						}
					}
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
					LOG.info("发送短信消息出错："+e.getMessage()+"订单号："+ordOrder.getOrderId());
                    sentOrderIdList.add(ordOrder.getOrderId());
                    continue;
				}
				sentOrderIdList.add(ordOrder.getOrderId());
				if(sentCount >= 500) {
					break;
				}
			}
			if(CollectionUtils.isNotEmpty(sentOrderIdList)) {
				orderUpdateService.markRemindSmsSent(sentOrderIdList);
			}
			LOG.info("OrderPerformPreviousDayJob End");
		}
	}

	private List<String> getPassCodeList(OrdOrder ordOrder){
		List<String> statusList = null;
		LOG.info("getPassCodeList#orderId:"+ordOrder.getOrderId());
		//判断订单是否是门票订单
		if(ordOrder.getCategoryId() == 11L || ordOrder.getCategoryId() == 12L || ordOrder.getCategoryId() == 13L){
			LOG.info("getPassCodeList#categoryId:"+ordOrder.getCategoryId());
			//对接订单
			if(ordOrder.isSupplierOrder()){
				LOG.info("getPassCodeList#isSupplierOrder");
				Map<String, Object> params = new HashMap<String,Object>();
				params.put("orderId", ordOrder.getOrderId());
				//根据订单id查询通关信息
				ResultHandleT<List<String>> resultHandleT = supplierOrderOtherService.getPassCodeListByOrderId(params);
				statusList = resultHandleT.getReturnContent();
			}
		}	
		return statusList;
	}
	

}
