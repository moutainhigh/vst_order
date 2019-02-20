package com.lvmama.vst.order.service.impl;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.comm.vst.vo.VstInvoiceAmountVo;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.back.order.po.OrdInvoiceRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.ResultHandle;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdInvoiceDao;
import com.lvmama.vst.order.dao.OrdInvoiceRelationDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.service.IOrdInvoiceService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrderInvoiceRelationService;
import com.lvmama.vst.order.service.IOrderSmsSendService;
import com.lvmama.vst.order.utils.HTTPClientUtil;
import com.lvmama.vst.order.utils.InvoiceResult;
import com.lvmama.vst.order.utils.InvoiceUtil;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.utils.ResultHttpRequest;
import com.lvmama.vst.order.utils.XMLUtil;
import com.lvmama.vst.order.vo.remote.CallServiceRequest;
import com.lvmama.vst.order.vo.remote.CallServiceResponse;
import com.lvmama.vst.order.vo.remote.GetCodeAndNoRequest;
import com.lvmama.vst.order.vo.remote.GetCodeAndNoResponse;
import com.lvmama.vst.order.vo.remote.InvoiceData;
import com.lvmama.vst.order.vo.remote.InvoiceDetail;
import com.lvmama.vst.order.vo.remote.RePrintInvoiceData;
import com.lvmama.vst.order.vo.remote.RePrintRequest;
import com.lvmama.vst.order.vo.remote.RePrintResponse;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.pet.adapter.refund.OrderRefundSplitServiceAdapter;

@Service
public class OrdInvoiceServiceImp implements IOrdInvoiceService {
	private static Logger logger = LoggerFactory.getLogger(OrdInvoiceServiceImp.class);
	@Autowired
	private IOrdPersonService ordPersonService;
	
	@Autowired
	private IOrderInvoiceRelationService orderInvoiceRelationService;

	@Autowired
	private OrdOrderDao orderDao;
	
	@Autowired
	private OrdInvoiceDao ordInvoiceDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;
	
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	
	@Autowired
	private OrderRefundSplitServiceAdapter orderRefundSplitServiceAdapter;
	
	@Autowired
	private OrdInvoiceRelationDao ordInvoiceRelationDao;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	
    @Autowired
    private OrdPersonDao ordPersonDao;
    
    @Autowired
    private OrdAddressDao ordAddressDao;
    
    @Autowired
    private OrderService orderService;
	
	@Override
	public int deleteByPrimaryKey(Long ordInvoiceId) {
		return ordInvoiceDao.deleteByPrimaryKey(ordInvoiceId);
	}

	@Override
	public int insert(OrdInvoice record) {
		return ordInvoiceDao.insert(record);
	}

	@Override
	public int insertSelective(OrdInvoice record) {
		return ordInvoiceDao.insertSelective(record);
	}

	@Override
	public OrdInvoice selectByPrimaryKey(Long ordInvoiceId) {
		return ordInvoiceDao.selectByPrimaryKey(ordInvoiceId);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdInvoice record) {
		return ordInvoiceDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(OrdInvoice record) {
		return ordInvoiceDao.updateByPrimaryKey(record);
	}

	@Override
	public Long getInvoiceAmountSum(Map<String,Object> map){
		return ordInvoiceDao.getInvoiceAmountSum(map);
	}
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	
	/**
	 * 退款
	 */
	public long getSumCompensationAndRefundment(Long orderId){
		OrdOrder order=orderDao.selectByPrimaryKey(orderId);
		long sum=0;

		Map<String,Object> param = new HashMap<String,Object>();
		param.put("orderId", orderId);
		param.put("status", Constant.REFUNDMENT_STATUS.REFUNDED.name());
		List<OrdRefundment> listOrdRefundment = orderRefundmentServiceAdapter.findOrderRefundmentByOrderIdStatus(orderId, Constant.REFUNDMENT_TYPE.ORDER_REFUNDED.name());
		if(CollectionUtils.isNotEmpty(listOrdRefundment)){
			for (OrdRefundment refundment : listOrdRefundment) {
				if (Constant.REFUNDMENT_TYPE.ORDER_REFUNDED.name().equalsIgnoreCase(refundment.getRefundType())) {
					sum += refundment.getAmount();
				}				
			}
		}
		//找出用户为储值卡支付的金额
//		Long sumCardAmount = payPaymentServiceAdapter.selectCardPaymentSuccessSumAmount(orderId);
//		sum = sum + sumCardAmount;
		
		List<OrdOrderItem> list = ordOrderItemDao.selectByOrderId(orderId);
		for(OrdOrderItem item:list){
			if(item.getCategoryId().equals(Long.valueOf(90))&&item.getCategoryId().equals(Long.valueOf(3))){
				sum+=item.getPrice()*item.getQuantity();
			}
		}
		return sum;
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String, Object> param) {
		return ordInvoiceDao.getOrdInvoiceListByParam(param);
	}
	
	@Override
	public List<OrdInvoice> getOrdInvoiceListByParam2(Map<String,Object> param){
		return ordInvoiceDao.getOrdInvoiceListByParam2(param);
	}
	
	

	@Override
	public InvoiceResult update(String status, Long invoiceId, String operatorId) {
		String operator = operatorId;
		InvoiceResult result = new InvoiceResult();

		// 如果发票是变更为取消或审核通过状态
		if (status.equals(Constant.INVOICE_STATUS.CANCEL.name()) || status.equals(Constant.INVOICE_STATUS.APPROVE.name())
				||status.equals(Constant.INVOICE_STATUS.RED.name())) {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("ordInvoiceId", invoiceId);
			List<OrdInvoiceRelation> list = ordInvoiceRelationDao.getListByParam(map);
			if (CollectionUtils.isNotEmpty(list)) {
				// 如果是取消，需要修改订单相关的标记
				OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(invoiceId);
				boolean needUpdateInvoice=true;
				if (status.equals(Constant.INVOICE_STATUS.APPROVE.name())) {// 审核通过的状态
					try {
						long amount = 0;
						boolean changeAmount=false;//只有在发票金额全部为true才更新发票金额
						for (OrdInvoiceRelation relation : list) {
							List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
							OrdOrder order = orderDao.selectByPrimaryKey(relation.getOrderId());
							List<OrdOrderItem> ordOrderItem = ordOrderItemService.selectByOrderId(relation.getOrderId());
							if (ordOrderItem != null && ordOrderItem.size() > 0) {
								orderItemList.addAll(ordOrderItem);
							}
							order.setOrderItemList(orderItemList);
							
							if("true".equals(order.getNeedInvoice())){
//								long orderAmount = order.getOughtAmount();
//								if(order.getBonusAmount()!=null){
//									orderAmount-=order.getBonusAmount();
//								}
//								if(order.getActualAmount() <= order.getOughtAmount() && order.getRefundedAmount()!=null){
//									orderAmount-=order.getRefundedAmount();
//								}
//								if(order.getInsuranceAmount()!=0){
//									orderAmount-=order.getInsuranceAmount();
//								}
								//计算可开票金额  20170109 by ccj
								VstInvoiceAmountVo vstInvoiceAmountVo = this.getInvoiceAmount(order.getOrderId());
								long orderAmount = vstInvoiceAmountVo.getInvoiceAmount();
								
								long otherOrderAmount = getOrderInvoiceAmountNotInvoiceId(relation.getOrderId(),ordInvoice.getOrdInvoiceId());
								boolean updateNeedInvoice=orderAmount<=otherOrderAmount;
								orderAmount-=otherOrderAmount;
								if (orderAmount < 1) {
									throw new InvoiceResult.CancelException(relation.getOrderId(),updateNeedInvoice);
								}
								amount += orderAmount;
								changeAmount = true;
							}else if("part".equals(order.getNeedInvoice())){
								changeAmount=false;
							}
						}
						// 金额不一样，需要修改发票金额
						if (!ordInvoice.getAmount().equals(amount)&&changeAmount) {
							long oldAmount = ordInvoice.getAmount();
							ordInvoice.setAmount(amount);
							update(ordInvoice, "SYSTEM", "修改发票原金额:" + PriceUtil.convertToYuan(oldAmount) + "至" + PriceUtil.convertToYuan(amount));
						}
					} catch (InvoiceResult.CancelException ex) {
						result.rasieCancel(ex);
						// 直接废掉,并且将订单当中的开票状态变成false;
						status = Constant.INVOICE_STATUS.CANCEL.name();
						operator = "SYSTEM";
						if(ex.isUpdateNeedInvoice()){
							needUpdateInvoice=false;
						}
					}
				}
				if (status.equals(Constant.INVOICE_STATUS.CANCEL.name())||status.equals(Constant.INVOICE_STATUS.RED.name())) {
					if(list.size()==1){  //有可能存在多张分开开票
						if(needUpdateInvoice){//如果其他的发票金额已经多余总金额，不变更状态。
							Long orderId= list.get(0).getOrderId();
							String needInvoice="false";
							if(ordInvoiceRelationDao.selectInvoiceCountByOrderId(orderId)>1){
								needInvoice="part";
							}
							OrdOrder order = new OrdOrder();
							order.setOrderId(orderId);
							order.setNeedInvoice(needInvoice);
							order.setPaymentStatus(OrderEnum.ORDER_VIEW_STATUS.PAYED.name());
							orderDao.updateOrder(order);
						}
					}else{
						for (OrdInvoiceRelation relation : list) {
							OrdOrder order = new OrdOrder();
							order.setOrderId(relation.getOrderId());
							order.setNeedInvoice("false");
							order.setPaymentStatus(OrderEnum.ORDER_VIEW_STATUS.PAYED.name());
							orderDao.updateOrder(order);
						}
					}
				}
			}
		}
		boolean f = updateInvoice(status, invoiceId, operator);
		if (!f) {
			result.rasieError(new Exception("更新到" + status + " 失败"));
		}
		return result;
	}
	
	public boolean updateInvoice(String status, Long invoiceId, String operatorId) {
		OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(invoiceId);
		if(ordInvoice==null){
			return false;
		}
		ordInvoice.setStatus(status);
		if(status.equals(Constant.INVOICE_STATUS.BILLED.name())){
			ordInvoice.setBillDate(new Date());
		}
		String updateContent = "set status = " + status;
		if (StringUtils.equals(status, Constant.INVOICE_STATUS.CANCEL.name()) && StringUtils.equals("SYSTEM", operatorId)) {
			updateContent += " 因订单当中出现0或负数的发票金额自动取消";
		}
		return update(ordInvoice, operatorId, updateContent);
	}
	
	public boolean update(OrdInvoice ordInvoice, String operatorId, String updateContent) {
		ordInvoiceDao.updateByPrimaryKey(ordInvoice);
		insertLog(ordInvoice.getOrdInvoiceId(), "ORD_INVOICE", ordInvoice.getOrderId(), "ORD_ORDER", operatorId, "修改订单发票", Constant.COM_LOG_ORDER_EVENT.updateOrderInvoice.name(), updateContent);
		return true;
	}
	
	public long getOrderInvoiceAmountNotInvoiceId(Long orderId, Long excludeInvoiceId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("orderId", orderId);
		if (excludeInvoiceId != null) {
			param.put("excludeInvoiceId", excludeInvoiceId);
		}
		Long sum = ordInvoiceDao.getInvoiceAmountSum(param);
		if (sum == null || sum == 0) {
			return 0;
		}
		return ordInvoiceDao.getInvoiceAmountSum(param);
	}
	
	@Override
	public void updateRedFlag(Long invoiceId, String redFlag, String operatorId) {
		OrdInvoice ordInvoice = new OrdInvoice();
		ordInvoice.setRedFlag(redFlag);
		ordInvoice.setOrdInvoiceId(invoiceId);
		ordInvoice.setUserId(operatorId);
		updateInvoice(redFlag, invoiceId, operatorId);
	}
	
	public InvoiceResult updateInvoiceRedFlag(Long invoiceId, String redFlag, String operatorId) {
		InvoiceResult result = new InvoiceResult();
		OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(invoiceId);
		if(null==ordInvoice){
			result.rasieError(new Exception("发票不存在"));
			return result;
		}
		
		ordInvoice.setRedFlag(redFlag);
		ordInvoice.setRedReqTime(new Date());
		logger.info("用户进行了红冲操作,发票id为" + ordInvoice.getOrdInvoiceId() + " 操作的时间为： " + new Date());
		int count = updateRedFlag(ordInvoice,operatorId);
		if (count < 0) {
			result.rasieError(new Exception("更新到发票id：" + ordInvoice.getOrdInvoiceId()+ " 失败"));
		}
		return result;
	}
	
	@Override
	public int updateRedFlag(OrdInvoice ordInvoice, String operatorId) {
		int count = ordInvoiceDao.updateByPrimaryKeySelective(ordInvoice);	
		if(count > 0){
			insertLog(ordInvoice.getOrdInvoiceId(), "ORD_INVOICE", ordInvoice.getOrderId(), "ORD_ORDER", operatorId,
					"设置发票红冲", Constant.COM_LOG_ORDER_EVENT.updateOrder.name(), " 更新发票红冲状态到 " + (StringUtils.equals("true", ordInvoice.getRedFlag())?"申请":"关闭"));
		}
		return count;
	}
	/**
	 * 修改快递单号
	 */
	@Override
	public void updateInvoiceExpressNo(Long invoiceId, String expressNo,String operatorId) {
		boolean isOk = updateExpressNo(invoiceId, expressNo, operatorId);
		/*if(isOk){
			//如果更新快递单号成功 发送短信
			sendSms(invoiceId,expressNo);
		}
		return isOk;*/
	}
	
	/*public void sendSms(Long invoiceId,String expressNo){
		String content = "";
		try{
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("express_no", expressNo);
			content = orderSmsSendService.getSmsContent("", parameters);
			String mobile = getInvoicePersonMobile(invoiceId);
			smsRemoteService.sendSms(content, mobile,1);
		} catch(Exception e){
			LOG.error("发票出票发短信失败  发票ID=" + invoiceId +" 发票号 ="+expressNo + " 发送内容=" + content,e);
		}
	}
	
	private String getInvoicePersonMobile(Long invoiceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectType", "ORD_INVOICE");
		map.put("objectId", ""+invoiceId);
		map.put("personType", "ADDRESS");
		List<OrdPerson> list = ordPersonService.findOrdPersonList(map);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else {
			return list.get(0).getMobile();
		}
	}*/
	
	public boolean updateExpressNo(Long invoiceId, String expressNo,String operatorId) {
		OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(invoiceId);	
		if(InvoiceUtil.checkChangeExpressNo(ordInvoice)){
			logger.info("当前的发票"+invoiceId+"不可以变更快递号");
			return false;
		}
		
		StringBuffer content=new StringBuffer();
		if(StringUtils.isNotEmpty(ordInvoice.getExpressNo())){
			content.append("快递单号:");
			content.append(ordInvoice.getExpressNo());
			content.append("至");
			content.append(expressNo);
		}else{
			content.append("添加快递单号:");
			content.append(expressNo);
		}
		ordInvoice.setExpressNo(expressNo);
		if(StringUtils.equals(ordInvoice.getLogisticsStatus(),Constant.INVOICE_LOGISTICS.NONE.name())){
			content.append("更改物流状态至已快递");
			ordInvoice.setDeliverStatus(Constant.INVOICE_LOGISTICS.POST.name());
			ordInvoice.setLogisticsStatus(Constant.INVOICE_LOGISTICS.POST.name());
		}
		
		return update(ordInvoice, operatorId, content.toString());
	}
	
	/**
	 * 修改发票单号
	 */
	@Override
	public void updateInvoiceNo(Long invoiceId, String invoiceNo,String operatorId) {
		if (StringUtils.isEmpty(invoiceNo))
			return;
		update(invoiceNo, new Date(), invoiceId, operatorId);
	}
	
	public boolean update(String invoiceNo, Date billDate, Long invoiceId, String operatorId) {		
		OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(invoiceId);	
		if(InvoiceUtil.checkChangeInvoiceNo(ordInvoice)){
			logger.info("当前的发票"+invoiceId+"不可以变更发票号");
			return false;
		}
		StringBuffer content=new StringBuffer("变更 ");
		if(StringUtils.isNotEmpty(ordInvoice.getInvoiceNo())){
			content.append("发票号:");
			content.append(ordInvoice.getInvoiceNo());
			content.append("至");
			content.append(invoiceNo);
		}
		ordInvoice.setInvoiceNo(invoiceNo);
		ordInvoice.setBillDate(billDate);
		
		if(!ordInvoice.getStatus().equals(Constant.INVOICE_STATUS.BILLED.name())){
			content.append("之前状态:");
			content.append(ordInvoice.getStatus());
			content.append("至");
			content.append(Constant.INVOICE_STATUS.BILLED.name());
			ordInvoice.setStatus(Constant.INVOICE_STATUS.BILLED.name());
		}
		return update(ordInvoice, operatorId, content.toString());
	}
	
	public void insertLog(Long objectId, String objectType, Long parentId, String parentType, String operatorName, String logName, String logType, String content) {
		ComLog log = new ComLog();
		log.setObjectId(objectId);
		log.setObjectType(objectType);
		log.setParentId(parentId);
		log.setParentType(parentType);
		log.setOperatorName(operatorName);
		log.setLogName(logName);
		log.setLogType(logType);
		log.setContent(content);
		lvmmLogClientService.sendLog(log);
	}
	
	public OrdInvoice insert(Pair<OrdInvoice,OrdPerson> invoice, List<OrdOrder> orderIds,String operatorId){
//		long amount=0;
		String userId=null;
		for(OrdOrder order:orderIds){
			if(StringUtils.isEmpty(userId)){
				userId=order.getUserId();//发票用户ID以第一个订单的userId				
			}
			if(order.getOughtAmount()<1){//如果订单的可开票金额小于1时异常抛出
				throw new RuntimeException ("订单:"+order.getOrderId()+", 当前的金额不可以开出发票");
			}
//			amount+=order.getOughtAmount();//以应用金额为准
		}
		
//		invoice.getFirst().setAmount(amount);
		OrdInvoice ordInvoice = insertInvoiceInfo(invoice,userId,operatorId);	
		for(OrdOrder order:orderIds){			
			insertRelation(ordInvoice.getOrdInvoiceId(), order.getOrderId());			
			if(!StringUtils.equals("true",order.getNeedInvoice())){
				order.setNeedInvoice("true");
				orderDao.updateByPrimaryKey(order);
			}		
		}
		return ordInvoice;
	}
	
	/**
	 * 判断一个订单是否已经存在未取消的发票
	 * @param orderId
	 * @return true为已经存在
	 */
	boolean hasNotCancelInvoice(Long orderId){
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("orderId", orderId);
		param.put("status", Constant.INVOICE_STATUS.CANCEL.name());
		return ordInvoiceDao.selectNotCancelInvoiceCountByOrderId(param)>0L;
	}
	
	public void insert(List<Pair<OrdInvoice,OrdPerson>> invoices,Long orderId,String operatorId){
		OrdOrder order = orderDao.selectByPrimaryKey(orderId);
		
		if(StringUtils.equals("true",order.getNeedInvoice())){
			throw new IllegalArgumentException("This orderId:"+orderId+" cancel is billed.");
		}
//		if(hasNotCancelInvoice(orderId)){	
//			throw new IllegalArgumentException("This orderId:"+orderId+" form includes the receipt which has not cancelled, cannot increase");
//		}
		
		long orderAmount = order.getOughtAmount();
		if (orderAmount < 1) {// 如果订单的可开票金额小于1时异常抛出
			throw new RuntimeException("订单:" + orderId + ", 当前的金额不可以开出发票");
		}

		long total = 0L;
		for (Pair<OrdInvoice, OrdPerson> invoice : invoices) {
			OrdInvoice ordInvoice = insertInvoiceInfo(invoice,order.getUserId(), operatorId);
			total += ordInvoice.getAmount();
			insertRelation(ordInvoice.getOrdInvoiceId(), orderId);
		}
		if (total > orderAmount) {
			throw new RuntimeException("开票的总金额超出了订单的金额");
		}

		if (!StringUtils.equals("true", order.getNeedInvoice())) {
			order.setNeedInvoice("true");
			orderDao.updateByPrimaryKey(order);
		}
	}
	
	
	/**
	 * 添加关联商品
	 * @param invoiceId
	 * @param orderId
	 */
	private void insertRelation(Long invoiceId,Long orderId){
		OrdInvoiceRelation relation=new OrdInvoiceRelation();
		relation.setOrderId(orderId);
		relation.setOrdInvoiceId(invoiceId);
		orderInvoiceRelationService.insert(relation);
	}
	
	/**
	 * 写入发票并记日志
	 */
	private OrdInvoice insertInvoiceInfo(Pair<OrdInvoice,OrdPerson> invoice,String userId,String operatorId){
		OrdInvoice ordInvoice=converInvoice(invoice.getFirst());
		ordInvoice.setUserId(userId);
		ordInvoice.setLogisticsStatus(Constant.INVOICE_LOGISTICS.NONE.name());
		ordInvoiceDao.insert(ordInvoice);	
		
		if (ordInvoice != null) {
			if (!invoice.getFirst().getDeliveryType().equals("SELF") && invoice.getSecond().getAddressList().size() != 0) {// 如果不是自取的情况下需要写入用户信息
				ordPersonService.insertInvoicePerson(invoice.getSecond(), ordInvoice.getOrdInvoiceId(), operatorId);
			}
		}
		
		insertLog(ordInvoice.getOrdInvoiceId(), "ORD_INVOICE", ordInvoice.getOrderId(), "ORD_ORDER", operatorId, 
				"新增订单发票", Constant.COM_LOG_ORDER_EVENT.insertOrderInvoice.name(), "新增订单发票:" +ordInvoice.getOrdInvoiceId() );
		
		return ordInvoice;
	}
	
	private OrdInvoice converInvoice(OrdInvoice invoice){
		OrdInvoice ordInvoice = new OrdInvoice();
		ordInvoice.setTitle(invoice.getTitle());
		ordInvoice.setMemo(invoice.getMemo());
		ordInvoice.setAmount(invoice.getAmount());
		ordInvoice.setContent(invoice.getContent());
		ordInvoice.setCreateTime(new Date());
		ordInvoice.setDeliverStatus(Constant.INVOICE_LOGISTICS.NONE.name());
//		ordInvoice.setCompany(invoice.getCompany());
		ordInvoice.setStatus(Constant.INVOICE_STATUS.UNBILL.name());
		ordInvoice.setDeliveryType(invoice.getDeliveryType());
		ordInvoice.setCompanyType(invoice.getCompanyType());
		ordInvoice.setPurchaseWay(invoice.getPurchaseWay());//购买方式
		ordInvoice.setTaxNumber(invoice.getTaxNumber());//纳税人识别号
		ordInvoice.setBuyerAddress(invoice.getBuyerAddress());//购买方地址
		ordInvoice.setBuyerTelephone(invoice.getBuyerTelephone());//购买方电话
		ordInvoice.setBankAccount(invoice.getBankAccount());//开户银行
		ordInvoice.setAccountBankAccount(invoice.getAccountBankAccount());//开户银行账号
		return ordInvoice;
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByOrderId(Map<String,Object> param) {
		return ordInvoiceDao.getOrdInvoiceListByOrderId( param);
	}

	@Override
	public Long getInvoiceCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordInvoiceDao.getInvoiceCount(param);
	}

	@Override
	public List<OrdInvoice> getStatusOrdInvoiceListByParam(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordInvoiceDao.getStatusOrdInvoiceListByParam(param);
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByOrderIdList(List<Long> orderIds) {
		// TODO Auto-generated method stub
		return ordInvoiceDao.getOrdInvoiceListByOrderIdList(orderIds);
	}

    /**
     * 调用第三方接口（百旺）重新打印发票
     * @param invoiceIdsList
     */
    @Override
    public ResultHandle reprintInvoice(final List<String> invoiceIdsList){
        String url = PropertiesUtil.getValue("reprint.url").trim();
        String clientNo = PropertiesUtil.getValue("tax.disk.client.no").trim();//开票端编号
        String machineIp = PropertiesUtil.getValue("tax.machine.ip").trim();//开票端IP
        
        ResultHandle resultHandle = new ResultHandle();
        try {
            List<OrdInvoice> ordInvoiceList = getInvoiceFullInfo(invoiceIdsList);
            if (null != ordInvoiceList && ordInvoiceList.size() != 0) {
                for (OrdInvoice ordInvoice : ordInvoiceList) {
                    RePrintRequest rePrintRequest = new RePrintRequest();
                    rePrintRequest.setClientNo(clientNo);
                    rePrintRequest.setTaxMachineIP(machineIp);
                    RePrintInvoiceData rePrintInvoiceData = new RePrintInvoiceData();
                    
                    rePrintInvoiceData.setInvType(ordInvoice.getInvoiceType());
                    rePrintInvoiceData.setInvCode(ordInvoice.getInvoiceCode());
                    rePrintInvoiceData.setInvNo(ordInvoice.getInvoiceNo());
                    rePrintInvoiceData.setPrintType(0);//设置答应发票，打印类型 0-打印发票；1-打印清单
                    rePrintRequest.setRePrintInvoiceData(rePrintInvoiceData);
                    String requestBody = rePrintRequest.toString();
                    
                    logger.info("请求百旺重新打印发票接口： url = " + url + "requestBody = " + requestBody);
                    ResultHttpRequest doPost = HTTPClientUtil.doPost(url, requestBody);
                    if (isNullResultHttpRequest(doPost)) {
                        String errorMsg = "调用百旺重新打印发票接口，返回失败.";
                        logger.info(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                    int statusCode = doPost.getStatusCode();
                    
                    if (statusCode == 200) {
                        logger.info("百旺重新打印发票接口响应： statusCode = " + statusCode);
                        String result = doPost.getResponseBody();
                        if (StringUtil.isEmptyString(result)) {
                            String errorMsg = "百旺重新打印发票接口响应内容为空 ，result = " + result;
                            logger.info(errorMsg);
                            throw new RuntimeException(errorMsg);
                        }
                        String content = getMatchedContent(formatResult(result));
                        
                        if (StringUtil.isNotEmptyString(content)) {
                            content = content.replace("<output>", "<rePrintResponse>");
                            content = content.replace("</output>", "</rePrintResponse>");
                            
                            RePrintResponse rePrintResponse = XMLUtil.fromXML(content, RePrintResponse.class);
                            
                            if (null != rePrintResponse) {
                                if (rePrintResponse.getOperateFlag() != 0) {
                                    String errorMsg = "调用百旺重新打印发票接口，返回失败. rePrintResponse = " + rePrintResponse.toString();
                                    logger.info(errorMsg);
                                    throw new RuntimeException(errorMsg);
                                }else {
                                    logger.info("调用百旺重新打印发票接口成功");
                                }
                            }else {
                                String errorMsg = "调用百旺重新打印发票接口，返回失败, rePrintResponse为空。";
                                logger.info(errorMsg);
                                throw new RuntimeException(errorMsg);
                            }
                        }else {
                            String errorMsg = "调用百旺重新打印发票接口，返回内容错误。 responseResult = " + result;
                            logger.info(errorMsg);
                            throw new RuntimeException(errorMsg);
                        }
                        
                    }else {
                        String errorMsg = "调用百旺重新打印发票接口，返回失败. statusCode = " + statusCode;
                        logger.info(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                }
            }
        } catch (Exception e) {
            resultHandle.setMsg(e.getMessage());
        }
        return resultHandle;
        
    }
    /**
     * 调用第三方接口（百旺）开具发票
     * @param invoiceIdsList
     */
    @Override
	public ResultHandle issueInvoice(final List<String> invoiceIdsList, String operator) {
		String url = PropertiesUtil.getValue("callservices.url").trim();
		String clientNo = PropertiesUtil.getValue("tax.disk.client.no").trim();//开票端编号
		String machineIp = PropertiesUtil.getValue("tax.machine.ip").trim();//开票端IP

		ResultHandle resultHandle = new ResultHandle();
		try {
			List<OrdInvoice> ordInvoiceList = getInvoiceFullInfo(invoiceIdsList);
			if (null != ordInvoiceList && ordInvoiceList.size() != 0) {
				for (OrdInvoice ordInvoice : ordInvoiceList) {
					String invoiceTypeCode = ordInvoice.getInvoiceTypeCode();
					Long ordInvoiceId = ordInvoice.getOrdInvoiceId();
					Long orderId = ordInvoice.getOrderId();
					String prefixLog = "IssueInvoice orderId=" + orderId + "   ordInvoiceId=" + ordInvoiceId + "   ";
					GetCodeAndNoResponse getCodeAndNoResponse = doGetCodeAndNo(clientNo, machineIp, invoiceTypeCode);
					if (null != getCodeAndNoResponse) {
						if (getCodeAndNoResponse.getOperateFlag() == 0) {
							CallServiceRequest callServiceRequest = initCallServiceRequest(clientNo, machineIp, ordInvoice);
							String requestBody = callServiceRequest.toString();

							logger.info(prefixLog + "请求百旺发票开具接口： url = " + url + "requestBody = " + requestBody);
							ResultHttpRequest doPost = HTTPClientUtil.doPost(url, requestBody);

							if (isNullResultHttpRequest(doPost)) {
								String errorMsg = prefixLog +  "百旺发票开具接口，发票开具接口返回失败";
								logger.info(errorMsg);
								throw new RuntimeException(errorMsg);
							}

							int statusCode = doPost.getStatusCode().intValue();

							if (200 == statusCode) {
								logger.info(prefixLog + "百旺发票开具接口响应： statusCode = " + statusCode);
								String result = doPost.getResponseBody();
								if (StringUtil.isEmptyString(result)) {
									String errorMsg = prefixLog +  "百旺发票开具接口响应内容为空 ，result = " + result;
									logger.info(errorMsg);
									throw new RuntimeException(errorMsg);
								}
								String content = getMatchedContent(formatResult(result));

								if (StringUtil.isNotEmptyString(content)) {
									content = content.replace("<output>", "<callServiceResponse>");
									content = content.replace("</output>", "</callServiceResponse>");
									CallServiceResponse callServiceResponse = XMLUtil.fromXML(content, CallServiceResponse.class);

									if (null != callServiceResponse) {
										if (callServiceResponse.getOperateFlag() != 0) {
											String errorMsg = prefixLog +  "调用百旺发票开具接口，写盘失败，返回数据. callServiceResponse = " + callServiceResponse.toString();
											logger.info(errorMsg);
											throw new RuntimeException(errorMsg);
										}else {
											if (callServiceResponse.getPrintFlag() != 0) {
												String errorMsg = prefixLog +  "调用百旺发票开具接口，发送打印失败，返回数据. callServiceResponse = " + callServiceResponse.toString();
												logger.info(errorMsg);
												throw new RuntimeException(errorMsg);
											} else {
												logger.info(prefixLog + "请求百旺发票开具接口成功. callServiceResponse = " + callServiceResponse.toString());
												logger.info(prefixLog + "发票开具成功. 更新发票表，更改发票状态，增加发票代码、发票号码");
												OrdInvoice toUpdateOrdInvoice = ordInvoiceDao.selectByPrimaryKey(ordInvoice.getOrdInvoiceId());
												toUpdateOrdInvoice.setStatus(Constant.INVOICE_STATUS.BILLED.getCode());
												toUpdateOrdInvoice.setInvoiceNo(getCodeAndNoResponse.getDqfphm());
												toUpdateOrdInvoice.setInvoiceCode(getCodeAndNoResponse.getDqfpdm());
												Date billTime = new Date();
												toUpdateOrdInvoice.setBillDate(billTime);
												try {
													ordInvoiceDao.updateByPrimaryKeySelective(toUpdateOrdInvoice);
													insertLog(ordInvoice.getOrdInvoiceId(), "ORD_INVOICE", ordInvoice.getOrderId(), "ORD_ORDER", operator,
															"开具发票", Constant.COM_LOG_ORDER_EVENT.updateOrderInvoice.name(), "新增订单发票:" +ordInvoice.getOrdInvoiceId() );
												} catch (Exception e) {
													String errorMsg = prefixLog +  "回写开票状态失败";
													logger.info(errorMsg);
												}

											}
										}
									}else {
										String errorMsg = prefixLog +  "调用百旺发票开具接口，返回失败, callServiceResponse为空。";
										logger.info(errorMsg);
										throw new RuntimeException(errorMsg);
									}
								}else {
									String errorMsg = prefixLog +  "调用百旺发票开具接口，返回内容错误。 responseResult = " + result;
									logger.info(errorMsg);
									throw new RuntimeException(errorMsg);
								}
							}else {
								String errorMsg = prefixLog +  "调用百旺发票开具接口，返回失败. statusCode = " + statusCode;
								logger.info(errorMsg);
								throw new RuntimeException(errorMsg);
							}
						}else {
							String errorMsg = prefixLog +  "百旺税控盘查询接口，查询税控盘返回失败. getCodeAndNoResponse = " + getCodeAndNoResponse.toString();
							logger.info(errorMsg);
							throw new RuntimeException(errorMsg);
						}
					}else {
						String errorMsg = prefixLog +  "百旺税控盘查询接口，查询税控盘返回失败";
						logger.info(errorMsg);
						throw new RuntimeException(errorMsg);
					}
				}
			}
		} catch (Exception e) {
			resultHandle.setMsg(e.getMessage());
		}
		return resultHandle;
	}

	private CallServiceRequest initCallServiceRequest(String clientNo, String machineIp, OrdInvoice ordInvoice) {
        /**销货单位识别号*/
        String venderTaxNo = PropertiesUtil.getValue("vender.tax.no").trim();
        /**销货单位名称*/
        String venderName = PropertiesUtil.getValue("vender.name").trim(); 
        /**销货单位地址*/
        String venderAddress = PropertiesUtil.getValue("vender.address").trim();
        /**销货单位电话*/
        String venderTel = PropertiesUtil.getValue("vender.tel").trim();
        /**销货单位银行帐号*/
        String venderBankNameNo = PropertiesUtil.getValue("vender.bank.name.no").trim();
        /**销货单位银行帐号*/
        String venderBankName = PropertiesUtil.getValue("vender.bank.name").trim();
        /**税率*/
        Double taxRate = Double.valueOf(PropertiesUtil.getValue("tax.rate").trim());
        /**收款人*/
        String receiver = PropertiesUtil.getValue("receiver").trim();
        /**复核人*/
        String checker = PropertiesUtil.getValue("checker").trim();
        /**开票人*/
        String issuer = PropertiesUtil.getValue("issuer").trim();
        
        CallServiceRequest callServiceRequest = new CallServiceRequest();
        callServiceRequest.setClientNo(clientNo);
        callServiceRequest.setTaxMachineIP(machineIp);
        callServiceRequest.setSysInvNo(ordInvoice.getOrdInvoiceId().toString());
        callServiceRequest.setInvoiceList(0);//设置不打印清单, 1 打印清单 0 不打印清单
        callServiceRequest.setInvoiceSplit(0);//设置不拆分, 1 拆分  0 不拆分
        callServiceRequest.setInvoiceConsolidate(1);//设置合并, 1 合并  0 不合并
        
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setSwiftNumber(null);//每次请求的交易流水号，若为空,百旺则会自动生成
        invoiceData.setInvType(ordInvoice.getInvoiceType());
        invoiceData.setCreditNoteInv(0);//红冲标志, 0-正常发票;1-红冲发票
        invoiceData.setCancelInvType(0);//作废类型, 0正常发票1-作废空白发票;2-作废已开发票
        invoiceData.setVenderTaxNo(venderTaxNo);
        invoiceData.setVenderName(venderName);
        invoiceData.setVenderAddressTel((venderAddress == null ? "" : venderAddress) + " " + (venderTel == null ? "" : venderTel));
        invoiceData.setVenderBankNameNo((venderBankName == null ? "" :venderBankName) + " " + (venderBankNameNo == null ? "" : venderBankNameNo));
        invoiceData.setCustomerTaxNo(ordInvoice.getTaxNumber());
        invoiceData.setCustomerName(ordInvoice.getTitle());
        invoiceData.setCustomerAddressTel((ordInvoice.getBuyerAddress() == null ? "" : ordInvoice.getBuyerAddress()) 
                                            + " " + (ordInvoice.getBuyerTelephone() == null ? "" : ordInvoice.getBuyerTelephone()));
        invoiceData.setCustomerBankNameNo((ordInvoice.getBankAccount() == null ? "" : ordInvoice.getBankAccount())
                                            + " " + (ordInvoice.getAccountBankAccount() == null ? "" : ordInvoice.getAccountBankAccount()));
        ArrayList<InvoiceDetail> invoiceDetail = new ArrayList<InvoiceDetail>();
        InvoiceDetail invoiceDetailItem = new InvoiceDetail();
        invoiceDetailItem.setXh(1);
        invoiceDetailItem.setFphxz(0);//发票行性质, 默认传“0”
        invoiceDetailItem.setProductName(ordInvoice.getContent().trim());
        invoiceDetailItem.setProductAmount(1l);
        Long invoiceAmount = ordInvoice.getAmount();
        if (null == invoiceAmount) {
            invoiceAmount = 0l;
        }
        Double amount = new Double(invoiceAmount);
        //数据库发票金额以分为单位存储需要转换成以元为单位
        invoiceDetailItem.setUnitPrice(amount/100);
        invoiceDetailItem.setTotalAmount(invoiceDetailItem.getProductAmount() * invoiceDetailItem.getUnitPrice());
        invoiceDetailItem.setTaxRate(taxRate);//税率默认6%
        invoiceDetailItem.setTaxAmount(invoiceDetailItem.getTotalAmount() * taxRate);
        invoiceDetailItem.setTaxMark(1);//默认含税含税标志, 0-不含税 1-含税
        invoiceDetail.add(invoiceDetailItem);
        invoiceData.setInvoiceDetail(invoiceDetail);
        invoiceData.setSumTotalAmount(invoiceDetailItem.getProductAmount() * invoiceDetailItem.getUnitPrice());
        invoiceData.setSumTaxAmount(invoiceData.getSumTotalAmount() * taxRate);
        invoiceData.setTotal(invoiceData.getSumTaxAmount() + invoiceData.getSumTotalAmount());//价税合计
        invoiceData.setRemark(ordInvoice.getMemo());
        invoiceData.setIssuer(issuer);
        invoiceData.setReceiver(receiver);
        invoiceData.setChecker(checker);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(ordInvoice.getCreateTime());
        invoiceData.setMonth(month);
        callServiceRequest.setInvoiceData(invoiceData);
        
        return callServiceRequest;
    }

    /**
     * 调用百旺“税控盘查询接口”，获取当前发票代码和当前发票号码
     * @param invoiceTypeCode
     * @return 
     * @throws Exception
     */
    private GetCodeAndNoResponse doGetCodeAndNo(String clientNo, String machineIp, String invoiceTypeCode) throws Exception {
        String url = PropertiesUtil.getValue("getcodeandno.url").trim();
        
        GetCodeAndNoResponse getCodeAndNoResponse = null;
        GetCodeAndNoRequest getCodeAndNoRequest = new GetCodeAndNoRequest();
        getCodeAndNoRequest.setClientNo(clientNo);
        getCodeAndNoRequest.setTaxMachineIP(machineIp);
        getCodeAndNoRequest.setFplxdm(invoiceTypeCode);
        
        String requestBody = getCodeAndNoRequest.toString();
        
        logger.info("请求百旺税控盘查询接口： url = " + url + "requestBody = " + requestBody);
        ResultHttpRequest doPost = HTTPClientUtil.doPost(url, requestBody);
        
        if (isNullResultHttpRequest(doPost)) {
            return getCodeAndNoResponse;
        }
        
        int statusCode = doPost.getStatusCode().intValue();
        
        if (200 == statusCode) {
            logger.info("百旺税控盘查询接口响应： statusCode = " + statusCode);
            String result = doPost.getResponseBody();
            if (StringUtil.isEmptyString(result)) {
                throw new RuntimeException("百旺税控盘查询接口响应内容为空 ，result = " + result);
            }
            String content = getMatchedContent(formatResult(result));
            
            if (StringUtil.isNotEmptyString(content)) {
                
                if (content != null && !("".equals(content))) {
                    content = content.replace("<output>", "<getCodeAndNoResponse>");
                    content = content.replace("</output>", "</getCodeAndNoResponse>");
                    getCodeAndNoResponse = XMLUtil.fromXML(content, GetCodeAndNoResponse.class);
                }
            }else {
                throw new RuntimeException("调用百旺税控盘查询接口，返回内容错误。 responseResult = " + result);
            }
        }else {
            throw new RuntimeException("调用百旺税控盘查询接口，返回失败. statusCode = " + statusCode);
        }
        return getCodeAndNoResponse;
    }

    private boolean isNullResultHttpRequest(ResultHttpRequest doPost) {
        return null == doPost || null == doPost.getStatusCode() || StringUtil.isEmptyString(doPost.getResponseBody());
    }

    /**
     * 将字符串中"&lt;"转换成"<"、"&gt;"转换成 ">"
     * @param respContent
     * @return
     */
    private String formatResult(String respContent) {
        respContent = respContent.replace("&lt;", "<");
        respContent = respContent.replace("&gt;", ">");
        return respContent;
    }
    /**
     * 从返回的响应内容中获取有用信息部分
     * @param result
     * @return
     */
    private String getMatchedContent(String result) {
        String regex = "<output>([\\s\\S]*?)</output>";
        String content = "";
        
        Pattern pat = Pattern.compile(regex);
        Matcher matcher = pat.matcher(result);
        boolean find = matcher.find();
        
        if (find) {
         if (matcher.groupCount() > 0) {
             content = matcher.group(0);
         }
        }
        return content;
    }
    /**
     * 通过invoiceIdsList获取invoice对象的list
     * @param invoiceIdsList
     * @return
     */
    private List<OrdInvoice> getInvoiceFullInfo(final List<String> invoiceIdsList) {
        if (null == invoiceIdsList || invoiceIdsList.size() <= 0) {
            return null;
        }
        
        List<OrdInvoice> invoiceList = new ArrayList<OrdInvoice>();
        //根据invoiceId的list获取发票信息
        for (String invoiceId : invoiceIdsList) {
            OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(Long.parseLong(invoiceId));
            if (null != ordInvoice.getDeliveryType()) {
                if (!Constant.DELIVERY_TYPE.SELF.name().equals(ordInvoice.getDeliveryType())) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("objectId", ordInvoice.getOrdInvoiceId());
                    params.put("objectType", "ORD_INVOICE");
                    List<OrdPerson> ordPersonList = ordPersonDao.findOrdPersonList(params);
                    if (null != ordPersonList && ordPersonList.size() > 0) {
                        ordInvoice.setDeliveryAddress(ordPersonList.get(0));
                        params.put("ordPersonId", ordPersonList.get(0).getOrdPersonId());
                        List<OrdAddress> list = ordAddressDao.findOrdAddressList(params);
                        if (null != list && list.size() >0) {
                            ordInvoice.getDeliveryAddress().setAddressList(list);
                        }
                    } 
                }
            }
            
            Map<String, Object> map = new HashMap<String, Object>();
            ArrayList<OrdOrder> ordOrderList = new ArrayList<OrdOrder>();
            map.put("ordInvoiceId", ordInvoice.getOrdInvoiceId());
            List<OrdInvoiceRelation> orderIdList = ordInvoiceRelationDao.getListByParam(map);
            for (OrdInvoiceRelation ordInvoiceRelation : orderIdList) {
                OrdOrder ordorder = orderService.queryOrdorderByOrderId(ordInvoiceRelation.getOrderId());
                if (ordorder != null) {
                    ordOrderList.add(ordorder);
                }
            }
            ordInvoice.setOrderList(ordOrderList);
            invoiceList.add(ordInvoice);
        }
        return invoiceList;
    }
    
    /**
     * 维度一： 当实收总额小于或等于应收总额时 ：  
     *        当实收<= 应收- 银行立减 时 ，  发票金额 = 应收 - 保险- 银行立减 - 退款-奖金 -驴游天下卡；    
	 * 维度二： 当实收总额大于应收总额时：
	 *        当实收 > 应收- 银行立减 时    
     *           如实收 -退款 >= 应收- 银行立减 ，    开票金额= 应收- 保险- 银行立减 -奖金 -驴游天下卡；
     *           如实收 -退款 <  应收- 银行立减  ，    开票金额= 实收- 保险- 退款 -奖金 -驴游天下卡；
     * 20170109 creat by ccj
     */
	@Override
	public VstInvoiceAmountVo getInvoiceAmount(Long orderId){
		VstInvoiceAmountVo invoiceAmountVO=new VstInvoiceAmountVo();
		logger.info("start getInvoiceAmount 查询订单号="+orderId+"的可开发票金额");
		Long InvoiceAmountFen=0L;
		if(null==orderId) 
			return new VstInvoiceAmountVo();;
		long startTime0 = System.currentTimeMillis();
		OrdOrder ordorder = orderDao.selectByPrimaryKey(orderId);
		if(ordorder==null){
			return new VstInvoiceAmountVo();
		}
		List<OrdOrderItem> orderItemList = ordOrderItemDao.selectByOrderId(orderId);
		ordorder.setOrderItemList(orderItemList);
//		OrdOrder ordorder = orderService.queryOrdorderByOrderId(orderId); //综合查询
//		if(ordorder==null){
//			return 0L;
//		}
		long startTime1 = System.currentTimeMillis();
		logger.debug("getInvoiceAmount 查询OrdOrder耗时："+ (startTime1 - startTime0) + "毫秒");
		
		//银行立减
		long payPromotionAmountTemp = ordorder.getPayPromotionAmount()==null ? 0 : ordorder.getPayPromotionAmount();
		//退款
		long refundedAmountTemp = 0; // 由于需要减去保险，而退款中可能包含退保险的，因此需要排查保险退款金额
		long categoryId = BIZ_CATEGORY_TYPE.category_insurance.getCategoryId();
		if(orderItemList != null && orderItemList.size() > 0)
		{
			for(OrdOrderItem orderItem : orderItemList)
			{
				if(orderItem != null && categoryId != orderItem.getCategoryId().longValue())
				{
					refundedAmountTemp += getItemRefundAmount(orderItem.getOrderItemId());
				}
			}
		}
		
		
		
		//应收金额
		long oughtAmount = ordorder.getOughtAmount();
		//实收金额
		long actualAmount = ordorder.getActualAmount();
		//保险金额
		long insuranceAmount=ordorder.getInsuranceAmount();
		long temp = oughtAmount;
		//1、当实收<= 应收- 银行立减 时, 发票金额 = 应收 - 银行立减 - 保险 - 退款-奖金 -驴游天下卡；
		//2、当实收 > 应收- 银行立减 时,
		//	2.1 如果实收 -退款 >= 应收-银行立减   则开票金额= 应收-银行立减- 保险-奖金 -驴游天下卡
		//	2.2 如果实收 -退款 <  应收-银行立减   则开票金额= 实收- 保险- 退款-奖金 -驴游天下卡；
		if (actualAmount <= oughtAmount-payPromotionAmountTemp) {
			temp = oughtAmount - payPromotionAmountTemp - insuranceAmount - refundedAmountTemp;
		}else{
			if(actualAmount-refundedAmountTemp >= oughtAmount-payPromotionAmountTemp)
				temp = oughtAmount - payPromotionAmountTemp - insuranceAmount;
			else
				temp = actualAmount - insuranceAmount - refundedAmountTemp;
		}
		long startTime3 = System.currentTimeMillis();		
		
		//奖金支付总金额
		Long bonusPayAmountFen=0L;
		List<PayPayment> bonusPaymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(orderId, Constant.PAYMENT_GATEWAY.CASH_BONUS.name(),"SUCCESS");
		if(bonusPaymentList!=null && bonusPaymentList.size()>0){
			for (PayPayment p : bonusPaymentList) {
				bonusPayAmountFen += p.getAmount();
			}
		}
		long startTime4 = System.currentTimeMillis();		
		logger.debug("getInvoiceAmount 查询 奖金支付总金额 耗时："+ (startTime4 - startTime3) + "毫秒");
		//驴行天下卡支付总金额
		Long lytxkPayAmountFen=0L;
		List<PayPayment> lytxkPaymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(orderId, Constant.PAYMENT_GATEWAY.LYTXK_STORED_CARD.name(),"SUCCESS");
		if(lytxkPaymentList!=null && lytxkPaymentList.size()>0){
			for (PayPayment p : lytxkPaymentList) {
				lytxkPayAmountFen += p.getAmount();
			}
		}
		long startTime5 = System.currentTimeMillis();		
		logger.debug("getInvoiceAmount 查询 驴行天下卡支付总金额 耗时："+ (startTime5 - startTime4) + "毫秒");
		InvoiceAmountFen = temp - bonusPayAmountFen - lytxkPayAmountFen;
		
		//组装发票金额数据
		invoiceAmountVO.setOrderId(orderId);
		invoiceAmountVO.setActualAmount(actualAmount);
		invoiceAmountVO.setOughtAmount(oughtAmount);
		invoiceAmountVO.setBonusAmount(bonusPayAmountFen);
		invoiceAmountVO.setInsuranceAmount(insuranceAmount);
		invoiceAmountVO.setLytxkAmount(lytxkPayAmountFen);
		invoiceAmountVO.setPayPromotionAmount(payPromotionAmountTemp);
		invoiceAmountVO.setRefundedAmount(refundedAmountTemp);
		invoiceAmountVO.setInvoiceAmount(InvoiceAmountFen);

		logger.info("end getInvoiceAmount 查询订单号="+orderId+"的可开发票金额，耗时："+ (startTime5 - startTime0) + "毫秒");
//		return InvoiceAmountFen;	
		return invoiceAmountVO;
	}

	/**
     * 子订单的退款金额
     * @param orderItemId
     * @return Long 子单的实际退款金额
     */
    private Long getItemRefundAmount(Long orderItemId)
    {
    	logger.info("getItemRefundAmount start: " + orderItemId);

    	Long refundAmount = 0L;
    	
    	ResultHandleT<List<RefundOrderItemSplit>> resultHandle = orderRefundSplitServiceAdapter.queryOrdRefundmentItemSplitAllByOrderItemId(orderItemId);

    	if(resultHandle.isSuccess()&& null != resultHandle.getReturnContent())
    	{
    		List<RefundOrderItemSplit> itemSplitList =  resultHandle.getReturnContent();

    		logger.info("getItemRefundAmount itemSplitList = "+GsonUtils.toJson(itemSplitList));

    		if(CollectionUtils.isNotEmpty(itemSplitList))
    		{    			
    			for (RefundOrderItemSplit refundOrderItemSplit : itemSplitList)
    			{
					if(null != refundOrderItemSplit.getRefundPrice())
					{
						refundAmount +=refundOrderItemSplit.getRefundPrice();
					}
				}
    		}
    	}

    	logger.info("getItemRefundAmount end and result:"+refundAmount);

    	return refundAmount;
    }
    
}
