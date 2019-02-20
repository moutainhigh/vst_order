package com.lvmama.vst.order.web;

import java.io.Serializable;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.utils.ApportionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.tnt.po.OrderPrice;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.pet.adapter.PetMessageServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

/**
 * 组合产品下单
 * 
 * mayonghua
 * @date 2014-4-15
 * 
 */
@Controller
@RequestMapping("/order/orderAmountChange")
public class OrderAmountChangeAction extends BaseActionSupport implements Serializable{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -4202451464063888166L;
	
	/**
	 * 日志记录器测试用
	 */
	private static final Log LOG = LogFactory.getLog(OrderAmountChangeAction.class);
	
	
	/**
	 * 产品和商品查询业务接口
	 */
	@Autowired
	private IOrderProductQueryService orderProductQueryService;

	@Autowired
	private IOrderLocalService ordOrderClientService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IComMessageService comMessageService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private PetMessageServiceAdapter petMessageServiceAdapter;
	
	@Resource(name="orderPriceMessageProducer")
	private TopicMessageProducer orderPriceMessageProducer;
	
	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

	@Autowired
	private OrderAmountApportionService orderAmountApportionService;

	@Resource
	private OrderApportionDepotService orderApportionDepotService;

	

	@RequestMapping(value = "/showAddAmountChange")
	public String showAddAmountChange(Model model, HttpServletRequest request,Long orderId,Long orderItemId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddAmountChange>");
		}
		String orderType=request.getParameter("orderType");
		
		if("child".equals(orderType)){
			
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItemId); 
			
			String[] priceTypeArray = getPriceTypeArray();
			paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			model.addAttribute("ordMulPriceRateList",ordMulPriceRateList);
			
			model.addAttribute("orderPriceRateTypeList", OrderEnum.ORDER_PRICE_RATE_TYPE.values());
		}
		

		model.addAttribute("orderFormulasList", OrderEnum.ORDER_AMOUNT_FORMULAS.values());
		model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_AMOUNT_CHANGE_TYPE.values());
		
		return "/order/orderProductQuery/showAddAmountChange";
	}


	
	
	/**
	 * 保存
	 * @return
	 */
	@RequestMapping(value="/addOrdAmountChange")
	@ResponseBody
	public Object addOrdAmountChange(Model model,HttpServletRequest request,OrdAmountChange ordAmountChange,Long orderId,Long orderItemId,String orderType){
		try{
			LOG.info("addOrdAmountChange--------------------start");
			ResultMessage resultMessage=this.validateAmountChange(orderId, orderItemId, orderType);
			if (!resultMessage.isSuccess()) {
				
				return resultMessage;
			}
			StringBuffer orderItemAmountChangLog=new StringBuffer();
			//查询订单
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			
			LOG.info("get order is " + order);
			if("parent".equals(orderType)){
				
				String  amountChange=request.getParameter("amountChange");
				
				//Long amountLong=new Double(amountChange).longValue()*100;
//				Long amountLong=NumberUtils.toInt(amountChange)*100;
				Long amountLong=PriceUtil.convertToFen(amountChange);
//				Long amountLong=NumberUtils.toLong(amountChange)*100;

				LOG.info("order.getOughtAmount is" + order.getOughtAmount());
				//判断金额是否合法
				if(order.getOughtAmount() < amountLong){
					return new ResultMessage(ResultMessage.ERROR,"提交金额不得大于订单金额");
				}

				//设置主订单ID
				ordAmountChange.setOrderId(orderId);
				ordAmountChange.setObjectId(orderId);
				ordAmountChange.setObjectType(OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name());
				
				
				
				ordAmountChange.setAmount(amountLong);//订单总价变化值
				
			}else{
				
				OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
				LOG.info("orderItem Id is " + orderItem.getOrderId());
				boolean hasAmount=false;
				String [] amountChanges=request.getParameterValues("amountChange");
				for (int i = 0; i < amountChanges.length; i++) {
					String amount = amountChanges[i];
					if (!StringUtils.isEmpty(amount) && StringUtil.isNumberAmount(amount)) {
						hasAmount=true;
						break;
					}else{
						hasAmount=false;
					}
				}
				LOG.info("hasAmount is " + hasAmount);
				if (!hasAmount) {

					return new ResultMessage(ResultMessage.ERROR," 请至少填写一个金额为正数(或2位小数)");
				
				}
				
				String [] priceTypes=request.getParameterValues("priceType");
				
				if (priceTypes==null || priceTypes.length==0) {
					/*String change=""+NumberUtils.toDouble(amounts[0])*100;
					change=change.substring(0, change.length()-2);*/
//					Long amountLong=PriceUtil.convertToFen(amountChanges[0]);
					
					ordAmountChange.putContent("price",PriceUtil.convertToFen(amountChanges[0])+"");//销售单价变化值
					
					ordAmountChange.setAmount(PriceUtil.convertToFen(amountChanges[0])*orderItem.getQuantity());//订单总价变化值
//					ordAmountChange.setAmount(PriceUtil.convertToFen(amountChanges[0]));
					
					orderItemAmountChangLog.append("销售单价");
					orderItemAmountChangLog.append(amountChanges[0]).append("元");
				}else{
					Long amount=0L;//订单总价变化值
					
					for (int i = 0; i < priceTypes.length; i++) {
						
						if (StringUtils.isEmpty(amountChanges[i])) {
							continue;
						}
						ordAmountChange.putContent(priceTypes[i],PriceUtil.convertToFen(amountChanges[i]+""));//销售单价变化值
						
						if (orderItemAmountChangLog.length()>0) {
							orderItemAmountChangLog.append(",");
						}
						orderItemAmountChangLog.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCnName(priceTypes[i])).append(":");
						orderItemAmountChangLog.append(amountChanges[i]);
						/*Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
						paramsMulPriceRate.put("orderItemId", orderItemId); 
						paramsMulPriceRate.put("priceType",priceTypes[i]); 
						List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
						OrdMulPriceRate ordMulPriceRate=ordMulPriceRateList.get(0);
						
						amount+=PriceUtil.convertToFen(amountChanges[i])*ordMulPriceRate.getQuantity();*/
					}
//					ordAmountChange.setAmount(amount);//订单总价变化值
					
					
					
					Map<String, Object> map=ordAmountChange.getContentMap();
					
					
					String[] priceTypeArray = getPriceTypeArray();
					
					Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
					paramsMulPriceRate.put("orderItemId", orderItemId); 
					paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
					List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
					
//					Long newOrdItemTotalAmount=0L;
					Long totalChangeAmount = 0L;
					for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {

						String priceType = ordMulPriceRate.getPriceType();
//						if (map.containsKey(priceType)) {
//							
//							Long  oldPrice=ordMulPriceRate.getPrice();
//							Long  changPrice=NumberUtils.toLong(map.get(priceType)+ "");
//							if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(ordAmountChange.getFormulas())) {
//								changPrice=-changPrice;
//							}
//
//							newOrdItemTotalAmount += (oldPrice+changPrice)* ordMulPriceRate.getQuantity();
//						} else {
//							newOrdItemTotalAmount += ordMulPriceRate.getPrice()
//									* ordMulPriceRate.getQuantity();
//						}
						if (map.containsKey(priceType)) {
							Long  changPrice=NumberUtils.toLong(map.get(priceType)+ "");
							totalChangeAmount += changPrice * ordMulPriceRate.getQuantity();
						}
						// quantity+=ordMulPriceRate.getQuantity();
					}
					
					ordAmountChange.setAmount(totalChangeAmount);//此处是订单金额的变化值
					
//					Long newOughtAmount=countOrderOughtAmount( orderItemId, order);
//					
//					newOughtAmount+=newOrdItemTotalAmount;
//					
//					ordAmountChange.setAmount(Math.abs(order.getOughtAmount()-newOughtAmount));//此处是订单金额的变化值
				
				}
				
				
				//设置主订单ID
				ordAmountChange.setOrderId(orderId);
				ordAmountChange.setObjectId(orderItemId);
				ordAmountChange.setObjectType(OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER_ITEM.name());
				
				
			}
			//设置分销渠道ID和分销代码
			ordAmountChange.setDistributorId(order.getDistributorId());
			ordAmountChange.setDistributorCode(order.getDistributorCode());
			
			//获得主订单项
			OrdOrderItem orderItem = order.getMainOrderItem();
			LOG.info("OrdOrderItem ID is "+orderItem.getOrderItemId());
			//查询商品
//			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId());
			SuppGoodsParam param = new SuppGoodsParam();
			LOG.info("orderItem.getSuppGoodsId is "+orderItem.getSuppGoodsId());
			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), param,order.getCategoryId());
			if(resultHandleSuppGoods!=null && resultHandleSuppGoods.getReturnContent()!=null){
				SuppGoods goods = resultHandleSuppGoods.getReturnContent();
				ordAmountChange.setOperatorName(getLoginUserId());
				ordAmountChange.setCreateTime(new Date());
				ordAmountChange.setApproveStatus(OrdAmountChange.APPROVESTATUS.TOAPPROVE.name());
				ordAmountChange.setOrgId(goods.getOrgId());
				LOG.info("...addOrdAmountChange start insert ...");
				int result = orderAmountChangeService.insert(ordAmountChange);
				LOG.info("result is " + result);
				
				Long amountChange=ordAmountChange.getAmount();
				if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(ordAmountChange.getFormulas())) {
					amountChange=-amountChange;
				}
				
				if(result == 1){
					
					String formulasName= OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.getCnName(ordAmountChange.getFormulas());
					if("parent".equals(orderType)){
						
						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
								orderId, orderId, getLoginUserId(), "将编号为["+orderId+"]的订单，修改订单金额，修改值："+formulasName+" "+ PriceUtil.trans2YuanStr(amountChange), 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单价格修改申请", "");
					}else{
						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
								orderId, 
								orderItemId, 
								getLoginUserId(), 
								"将编号为["+orderId+"]的子订单，修改子订单销售单价，修改值："+formulasName+" "+orderItemAmountChangLog, 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单价格修改申请","");
						
					}

					LOG.info("addOrdAmountChange--------------------end");
					return new ResultMessage(ResultMessage.SUCCESS," 提交成功");
				}
			}else {
				LOG.info("resultHandleSuppGoods is "+resultHandleSuppGoods+"。and resultHandleSuppGoods.getReturnContent is"+resultHandleSuppGoods.getReturnContent());
			}
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		LOG.info("addOrdAmountChange--------------------end");
		return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
	}




	private String[] getPriceTypeArray() {
		String[] priceTypeArray = new String[] {
				ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()
		};
		return priceTypeArray;
	}




	private Long countOrderOughtAmount(Long orderItemId, OrdOrder order) {
		
		Long oughtAmount=0L;
		List<OrdOrderItem>  orderItemList=order.getOrderItemList();
		for (OrdOrderItem ordOrderItem : orderItemList) {
			
			if (orderItemId.equals(ordOrderItem.getOrderItemId())) {
				continue;
			}
			Long ordItemTotalAmount=0L;
			
			String[] priceTypeArray = getPriceTypeArray();
			
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", ordOrderItem.getOrderItemId()); 
			paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			
			if (CollectionUtils.isEmpty(ordMulPriceRateList)) {
				ordItemTotalAmount+=ordOrderItem.getPrice()*ordOrderItem.getQuantity();
			}else{
				for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {

					String priceType = ordMulPriceRate.getPriceType();
					ordItemTotalAmount += ordMulPriceRate.getPrice()
							* ordMulPriceRate.getQuantity();
					// quantity+=ordMulPriceRate.getQuantity();
				}
			}
			
			
			oughtAmount+=ordItemTotalAmount;
		}
		
		return oughtAmount;
		
	}
	
	
	/**
	 * 查询列表
	 * @return
	 */
	@RequestMapping(value="/showAmountChangeQueryList")
	public String showOrdAmountChangeQueryList(Model model,Long objectId,String approveStatus,String objectType, String prodManagerId, String managerName, HttpServletRequest req,Integer page){
		
		if(approveStatus==null)
			approveStatus = OrdAmountChange.APPROVESTATUS.TOAPPROVE.name();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("objectType", objectType);
		params.put("objectId", objectId);
		params.put("approveStatus", approveStatus);
		params.put("prodManagerId", prodManagerId);
		//查询总行数
		Integer counts = orderAmountChangeService.findOrdAmountChangeCounts(params);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(counts, 20, pagenum);
		pageParam.buildUrl(req);
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		List<OrdAmountChange> list = orderAmountChangeService.findOrdAmountChangeList(params);
		pageParam.setItems(list);
		
		for (int i = 0; i < list.size(); i++) {
			
			OrdAmountChange ordAmountChange=list.get(i);
			
			Map<String, Object> map=ordAmountChange.getContentMap();
			
			if ("ORDER_ITEM".equals(ordAmountChange.getObjectType()) && !map.isEmpty()) {
				
				StringBuffer amountChangeDesc=new StringBuffer();
				for (String key : map.keySet()) {
					
					if ("price".equals(key)) {
						amountChangeDesc.append("销售价");
					}else{
						amountChangeDesc.append(OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(key));
					}
					
					Long amountChange=NumberUtils.toLong(map.get(key)+"");
					
					amountChangeDesc.append(":").append(PriceUtil.trans2YuanStr(amountChange));
					amountChangeDesc.append("</br>");
				}
				
				ordAmountChange.setAmountChangeDesc(amountChangeDesc.toString());
				
//				OrdOrderItem ordItem=this.orderUpdateService.getOrderItem(ordAmountChange.getObjectId());
				
			}else{
				
				ordAmountChange.setAmountChangeDesc(PriceUtil.trans2YuanStr(ordAmountChange.getAmount()));
			}
		}
		
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("objectId", objectId);
		model.addAttribute("approveStatus", approveStatus);
		model.addAttribute("prodManagerId", prodManagerId);
		model.addAttribute("managerName", managerName);
		return "/order/orderProductQuery/showAmountChangeQueryList";
	}
	
	/**
	 * 保存
	 * @return
	 */
	/*
	@RequestMapping(value="/ord/order/addOrdAmountChange.do")
	@ResponseBody
	public Object addOrdAmountChange(Model model,HttpServletRequest request,OrdAmountChange ordAmountChange,Long orderId){
		try{
			//首先判断该订单有没有正在审核的项目
			if(orderAmountChangeService.isOrderApproving(orderId) > 0){
				return new ResultMessage(ResultMessage.ERROR," 操作失败，该订单有正在审核中的请求");
			}
			//查询订单
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			//判断金额是否合法
			if(order.getOughtAmount() <= ordAmountChange.getAmount()){
				return new ResultMessage(ResultMessage.ERROR,"提交金额不得大于订单金额");
			}
			//获得主订单项
			OrdOrderItem orderItem = order.getMainOrderItem();
			//查询商品
			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.FALSE, Boolean.FALSE);
			if(resultHandleSuppGoods!=null && resultHandleSuppGoods.getReturnContent()!=null){
				SuppGoods goods = resultHandleSuppGoods.getReturnContent();
				//查询是否有审核中的请求
				ordAmountChange.setOperatorName(getLoginUserId());
				ordAmountChange.setCreateTime(new Date());
				ordAmountChange.setApproveStatus(OrdAmountChange.APPROVESTATUS.TOAPPROVE.name());
				ordAmountChange.setOrgId(goods.getOrgId());
				int result = orderAmountChangeService.insert(ordAmountChange);
				if(result == 1){
					comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
							orderId, orderId, getLoginUserId(), "将编号为["+orderId+"]的订单，修改订单金额，修改值："+ordAmountChange.getAmount().doubleValue()/100, 
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"：更新订单金额申请", "");
					return new ResultMessage(ResultMessage.SUCCESS," 提交成功");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
	}
*/
	/**
	 * 保存
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/approveOrdAmountChange")
	@ResponseBody
	public Object approveOrdAmountChange(Model model,HttpServletRequest request,OrdAmountChange ordAmountChange) throws Exception{
		if(ordAmountChange==null)
			return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
		
		OrdAmountChange oldOrdAmountChange = orderAmountChangeService.selectByPrimaryKey(ordAmountChange.getAmountChangeId());
		if(!OrdAmountChange.APPROVESTATUS.TOAPPROVE.name().equalsIgnoreCase(oldOrdAmountChange.getApproveStatus())){
			return new ResultMessage(ResultMessage.SUCCESS," 审批已经完成");
		}

		Long orderId=0L;
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(ordAmountChange.getApproveStatus())){
			String objectType=oldOrdAmountChange.getObjectType();
			if (OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name().equals(objectType)) {
				orderId=oldOrdAmountChange.getObjectId();
			}else{
				Long orderItemId=oldOrdAmountChange.getObjectId();
				
				OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
				orderId=orderItem.getOrderId();
			}
			//校验订单状态
			ResultMessage resultMessage=this.validateOrderStatus(orderId);
			if (!resultMessage.isSuccess()) {
				
				return resultMessage;
			}
		}

		oldOrdAmountChange.setApproveStatus(ordAmountChange.getApproveStatus());
		oldOrdAmountChange.setMemo(ordAmountChange.getMemo());
		oldOrdAmountChange.setApproveOperator(getLoginUserId());
		oldOrdAmountChange.setApproveTime(new Date());
		int result = 0;
		String messge = "";
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(ordAmountChange.getApproveStatus())){
            //拼接orderPrice对象给分销的
            OrderPrice orderPrice=new OrderPrice();
            orderPrice.setReason(oldOrdAmountChange.getReason());
            orderPrice.setCreateDate(oldOrdAmountChange.getCreateTime());
            orderAmountChangeService.updateOrderAmount(oldOrdAmountChange,orderPrice);
			//分摊分手工修改金额
			if (ApportionUtil.isApportionEnabled()) {

				//子单上的改价，如果有其它任务抢先分摊了，还要增加一条分摊记录，让其它任务来执行重新分摊功能
				String memKey = MemcachedEnum.OrderApportionExecution.name() + orderId;
				String memValue = MemcachedUtil.getInstance().get(memKey);
				//缓存锁、数据库标识锁状态
				boolean isMemCacheKeyEnabled = false, isDatabaseKeyEnabled = false;
				//分摊仓库记录
				OrderApportionDepot orderApportionDepot = null;
				if (StringUtils.equals(Constants.Y_FLAG, memValue)) {
					isMemCacheKeyEnabled = true;
				} else {
					//
					orderApportionDepot = orderApportionDepotService.queryApportionByOrderId(orderId);
					log.info("Previous depot is " + GsonUtils.toJson(orderApportionDepot));
					if (orderApportionDepot == null || StringUtil.isNotEmptyString(orderApportionDepot.getApportionMessage())) {
						isDatabaseKeyEnabled = true;
					}
				}
				if (isMemCacheKeyEnabled || isDatabaseKeyEnabled) {
					log.info("Order " + orderId + " is apportion by another task, memcached key is " + isMemCacheKeyEnabled + ", db key is " + isDatabaseKeyEnabled);
					orderApportionDepotService.deleteByOrderId(orderId);
					OrderApportionDepot newOrderApportionDepot = new OrderApportionDepot();
					newOrderApportionDepot.setOrderId(orderId);
					newOrderApportionDepot.setValidFlag(Constants.Y_FLAG);
					Date time = Calendar.getInstance().getTime();
					newOrderApportionDepot.setCreateTime(time);
					newOrderApportionDepot.setUpdateTime(time);
					newOrderApportionDepot.setApportionOrigin(OrderEnum.APPORTION_ORIGIN.apportion_origin_price_change.getApportionOriginName());
					orderApportionDepotService.addOrderApportionDepot(newOrderApportionDepot);
					log.info("New order apportion depot add, order id is " + orderId);
					return null;
				}
				//加锁，有缓存锁、数据库标识两道锁
				MemcachedUtil.getInstance().set(memKey, MemcachedEnum.OrderApportionExecution.getSec(), Constants.Y_FLAG);
				orderApportionDepot.setApportionMessage(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name());
				orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
				orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
				//如果系统设置用老系统（vst_order）做订单改价分摊计算[新系统是lvmm-order-apportion]
		        if(!ApportionUtil.isLvmmOrderApportion()) {
					orderAmountApportionService.apportionAndSaveManualChangeAmount(orderId, oldOrdAmountChange);
		        }
				//解锁
				orderApportionDepot.setApportionMessage(null);
				orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
				//如果修改的是子单的价格，还要把分摊状态修改掉
				if (StringUtils.equals(oldOrdAmountChange.getObjectType(), OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER_ITEM.name())) {
					orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name());
				}
				orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
				MemcachedUtil.getInstance().remove(memKey);
			}
			messge = "审批通过";
            result = 1;
            try {
                petMessageServiceAdapter.sendChangeOrderPrice(orderPrice.getOrderId(), orderPrice);
            } catch (Exception e) {
                LOG.info("发送消息失败"+ordAmountChange.getOrderId());
            }
//				String addition=ordAmountChange.getAmount()+"_"+oldOrdAmountChange.getAmountType().trim();
            //OrderEnum.ORDER_AMOUNT_CHANGE_TYPE.getCnName();

//				orderLocalService.sendOrdAmountChangeMsg(order,addition);
            //o2o订单发送消息
            sendJmsforO2oOrder(oldOrdAmountChange.getOrderId());
        }else {
            result = orderAmountChangeService.updateByPrimaryKey(oldOrdAmountChange);
            messge = "审批未通过";
        }

		//发消息
		String assignor=getLoginUserId();
		int n=orderAmountChangeService.saveReservationForOrderAmountChang(oldOrdAmountChange, assignor, messge);
		if(n<=0){
            return new ResultMessage(ResultMessage.SUCCESS,"审批操作完成[预定通知创建失败，找不到可以接单的人]");
        }

		return new ResultMessage(ResultMessage.SUCCESS," 审批操作完成");

	}

	@RequestMapping(value = "/validateOrderAmountChange")
	@ResponseBody
	public Object validateOrderAmountChange( Model model, HttpServletRequest request,Long orderId,Long orderItemId){
		
		
		String orderType=request.getParameter("orderType");
		
		 return this.validateAmountChange(orderId, orderItemId, orderType);
		
	}

	
	private ResultMessage validateAmountChange(Long orderId, Long orderItemId,
			String orderType) {
		//订单正常 未支付完成状态 才可以发起申请
		
		ResultMessage resultMessage=validateOrderStatus(orderId);
		LOG.info("resultMessage is " + resultMessage.getMessage());
		if (!resultMessage.isSuccess()) {
			
			return resultMessage;
		}
		
		//是否 有正在审核记录 
		Long objectId;
		String objectType;
		if("child".equals(orderType)){
			objectId=orderItemId;
			objectType=OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER_ITEM.name();
		}else{
			objectId=orderId;
			objectType=OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name();
		}
		
		//首先判断该订单有没有正在审核的项目
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", objectId); 
		params.put("objectType",objectType); 
		params.put("approveStatus", OrdAmountChange.APPROVESTATUS.TOAPPROVE.name());
		int m=orderAmountChangeService.findOrdAmountChangeCounts(params);
		LOG.info("m is " + m);
		if( m>0 ){
			return new ResultMessage(ResultMessage.ERROR,"有正在审核中的请求");
		}

		LOG.info("validateAmountChange success");
		return ResultMessage.CHECK_SUCCESS_RESULT;
	}



	

	private ResultMessage validateOrderStatus(Long orderId) {
		OrdOrder order=orderLocalService.queryOrdorderByOrderId(orderId);
		if (!( order.isNormal() && OrderEnum.PAYMENT_STATUS.UNPAY.name().equals(order.getPaymentStatus())) ) {
			String orderStatus=OrderEnum.ORDER_STATUS.CANCEL.getCnName(order.getOrderStatus());
			String  payStatus=OrderEnum.PAYMENT_STATUS.PAYED.getCnName(order.getPaymentStatus());
			return new ResultMessage(ResultMessage.ERROR,"订单状态正常且未支付完成才可申请。当前订单状态："+orderStatus+"且"+payStatus);
		}
		return ResultMessage.CHECK_SUCCESS_RESULT;
	}
	
	/**
	 * 价格修改发送消息 o2o订单
	 * @param orderId
	 */
	private void sendJmsforO2oOrder(Long orderId){
		try {
			OrdOrder order=orderService.querySimpleOrder(orderId);
			if(order != null && (order.getDistributorId() == com.lvmama.vst.comm.vo.Constant.DIST_O2O_SELL 
					|| order.getDistributorId() == com.lvmama.vst.comm.vo.Constant.DIST_O2O_APP_SELL)){
				LOG.info("o2o order send amount change msg orderId::"+orderId);
				orderPriceMessageProducer.sendMsg(MessageFactory.sendOrderPrice(orderId, ""));
			}
		} catch (Exception e) {
			LOG.error("send amount change msg error: orderId="+orderId, e);
		}
		
	}

}
