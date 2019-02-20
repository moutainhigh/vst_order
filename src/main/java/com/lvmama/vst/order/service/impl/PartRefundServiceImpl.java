package com.lvmama.vst.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.bee.po.ord.OrdRefundApply;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.mark.MarkCouponUsage;
import com.lvmama.comm.pet.refund.vo.OrdRefundmentItemSplit;
import com.lvmama.comm.search.vst.vo.SuppGoodsRefund;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.passport.service.PassportService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_OBJECT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkUserClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.insurant.client.service.InsPolicyClientService;
import com.lvmama.vst.insurant.po.InsPolicy;
import com.lvmama.vst.order.dao.OrdPartRefundItemDAO;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.dao.OrdTicketPerformDao;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.refund.OrderRefundBatchDetailService;
import com.lvmama.vst.order.service.refund.OrderRefundBatchService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.web.OrderDetailAction;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.OrdRefundApplyServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@Service
public class PartRefundServiceImpl implements PartRefundService {

	private static final Logger LOG = LoggerFactory.getLogger(PartRefundServiceImpl.class);
	
	
	@Autowired
    private OrderService orderService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private OrdPassCodeDao ordPassCodeDao;
	@Autowired
	private OrdTicketPerformDao ordTicketPerformDao;
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	@Autowired
	private IOrdOrderService ordOrderService;
	@Autowired
	private IOrdPersonService ordPersonService;
	@Autowired
	private OrdPartRefundItemDAO  ordPartRefundItemDAO;
	@Autowired
    private PassportService passportService;
	@Autowired
    private  InsPolicyClientService  insPolicyClientService;
	@Autowired
    private OrdRefundApplyServiceAdapter ordRefundApplyServiceRemote;
	@Autowired
    private SuppSupplierClientService suppSupplierClientService;
	@Autowired
    private   SuppGoodsClientService  suppGoodsClientService;
	@Autowired
    private OrderRefundBatchService ordRefundBatchService;
	@Autowired
    private OrderRefundBatchDetailService ordRefundBatchDetailService;
	@Autowired
	private FavorServiceAdapter favorService;
	
	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private OrderPromotionService promotionService;	

	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentService;
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;

	@Autowired
	private EbkUserClientService ebkUserClientService;
	
	@Resource(name="orderDetailAction")
	private OrderDetailAction orderDetailAction;
	
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentServiceRemote;
	
	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;
	
	//公共操作日志业务
	@Autowired
	protected ComLogClientService comLogClientService;

	@Override
	public ResultHandleT<List<OrdRefundItemInfo>> queryOrdRefundInfo(Long orderId) {
		return queryOrdRefundInfo(orderId, false);
	}

	@Override
	public ResultHandleT<List<OrdRefundItemInfo>> queryOrdRefundInfo(Long orderId, boolean isFront) {
		ResultHandleT<List<OrdRefundItemInfo>> resultHandle = new ResultHandleT<List<OrdRefundItemInfo>>();
		if (!StringUtils.isNotBlank(orderId + "")) {
			resultHandle.setMsg("订单号不能为空");
			LOG.error("订单号为空！");
			return resultHandle;
		}
		OrdOrder ordOrder = ordOrderService.loadOrderWithItemByOrderId(orderId);
		List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
		if(ordOrderItems.size() == 0) {
			resultHandle.setMsg("获取子订单失败, 订单号:" + orderId + " 无子订单信息！");
			LOG.error("获取子订单失败, 订单号:" + orderId + " 无子订单信息！");
			return resultHandle;
		}
		List<OrdRefundItemInfo> refundItems = new ArrayList<OrdRefundItemInfo>();
		boolean containBx = false;
		for (OrdOrderItem ordOrderItem : ordOrderItems) {
			if(ordOrderItem.getCategoryId() == 3L){
				Map<String,Object> param=new HashMap<String,Object>();
				param.put("ordItemId", ordOrderItem.getOrderItemId());
				List<InsPolicy> insPolicies = insPolicyClientService.queryInsPolicy(param);
				if(insPolicies != null && insPolicies.size() != 0){
					InsPolicy insPolicy = insPolicies.get(0);
					if((!"738".equalsIgnoreCase(insPolicy.getPolicyType())) && (!"739".equalsIgnoreCase(insPolicy.getPolicyType()))){
						containBx = true;
						break;
					}
				}
			}
		}
		LOG.info("containBx:"+ containBx);
		for (OrdOrderItem ordOrderItem : ordOrderItems) {
			LOG.info("OrderItemId :" + ordOrderItem.getOrderItemId());
			boolean canRefund = false;
			Long refundQuantity = 0L;
			String memo = "";
			List<String> refundPersonIds = new ArrayList<String>();
			OrdRefundItemInfo ordRefundItemInfo = new OrdRefundItemInfo();
			ordRefundItemInfo.setOrderItemId(ordOrderItem.getOrderItemId());
			ordRefundItemInfo.setSuppGoodsId(ordOrderItem.getSuppGoodsId());
			ordRefundItemInfo.setSuppGoodsName(ordOrderItem.getSuppGoodsName());
			ordRefundItemInfo.setPrice(ordOrderItem.getPrice());
			if(ordOrderItem.getCategoryId() == 3L){
				LOG.info("Insurance ordOrderItem.");
				Map<String,Object> param=new HashMap<String,Object>();
				param.put("ordItemId", ordOrderItem.getOrderItemId());
				List<InsPolicy> insPolicies = insPolicyClientService.queryInsPolicy(param);
				if(insPolicies != null && insPolicies.size() != 0){
					InsPolicy insPolicy = insPolicies.get(0);
					List<OrdPerson> refundPersons = new ArrayList<OrdPerson>();
					if((!"738".equalsIgnoreCase(insPolicy.getPolicyType())) && (!"739".equalsIgnoreCase(insPolicy.getPolicyType()))){
						ordRefundItemInfo.setInsPolicyType("ACCIDENT");
						OrdItemResidueRefundInfo infoForAccidentInsurance = queryOrdResidueRefundQuantityOrPersonForAccidentInsurance(ordOrderItem.getOrderItemId()).getReturnContent();
						refundQuantity = infoForAccidentInsurance.getQuantity();
						if(refundQuantity > 0){
							canRefund = true;
						}
						refundPersonIds = infoForAccidentInsurance.getListOrdPerson();
						if(refundPersonIds != null){
							for (String refundPersonId : refundPersonIds) {
								OrdPerson refundPerson = ordPersonService.findOrdPersonById(Long.valueOf(refundPersonId));
								refundPersons.add(refundPerson);
							}
						}
						ordRefundItemInfo.setRefundQuantity(refundQuantity);
						ordRefundItemInfo.setRefundPersons(refundPersons);
					} else {
						ordRefundItemInfo.setInsPolicyType("RETREAT");
						Date inceptionDate = insPolicy.getInceptionDate();
						OrdItemResidueRefundInfo infoForReturnInsurance = queryOrdResidueRefundQuantityOrPersonForOtherInsurance(ordOrderItem.getOrderItemId()).getReturnContent();
						refundQuantity = infoForReturnInsurance.getQuantity();
						if((new Date()).before(inceptionDate) && refundQuantity > 0){
							canRefund = true;
						}
						refundPersonIds = infoForReturnInsurance.getListOrdPerson();
						if(refundPersonIds != null){
							for (String refundPersonId : refundPersonIds) {
								OrdPerson refundPerson = ordPersonService.findOrdPersonById(Long.valueOf(refundPersonId));
								refundPersons.add(refundPerson);
							}
						}
						ordRefundItemInfo.setRefundQuantity(refundQuantity);
						ordRefundItemInfo.setRefundPersons(refundPersons);
					}
				}
			}else if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
				LOG.info("Ticket ordOrderItem.");
				String refundWay = OrdPartRefundEnum.RenfundWayEnum.QUANTITY.name();
				Long quantity = queryOrdResidueRefundQuantityOrPerson(ordOrderItem.getOrderItemId(), refundWay).getReturnContent().getQuantity();
				if(quantity > 0){
					if(ordOrderItem.getCancelStrategy() != null){
						if(ordOrderItem.getCancelStrategy().equalsIgnoreCase(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name())
								|| ordOrderItem.getCancelStrategy().equalsIgnoreCase(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.PARTRETREATANDCHANGE.name())){
							canRefund = true;
							Date cancleDate = new Date();
							List<SuppGoodsRefundVO> goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(ordOrderItem, ordOrderItem.getVisitTime(), cancleDate);
							if(null !=goodsRefundList && goodsRefundList.size()>0) {
								for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
									//根据当前时间匹配的规则
									if(null != suppGoodsRefundVO.getIsCurrent()&&suppGoodsRefundVO.getIsCurrent()){
										if(suppGoodsRefundVO.getDeductType().equalsIgnoreCase(SuppGoodsRefund.DEDUCTTYPE.PERCENT.name())
												&& suppGoodsRefundVO.getDeductValue() == 10000L){
											canRefund = false;
											memo = "不支持申请，原因：逾期扣除每张（份）票价百分比100%";
											break;
										}
									}
								}
							}
						}
					} else {
						canRefund = false;
						memo = "不支持申请，原因：该商品为到付商品或无退改政策";
					}
				}
				if(ordOrderItem.hasExpresstypeDisplay()){
					canRefund = false;
					memo = "不支持申请，原因：订单对应商品为实体票";
				}
			}
			
			LOG.info("canRefund:"+ canRefund);
			LOG.info("memo:"+ memo);
			ordRefundItemInfo.setMemo(memo);
			ordRefundItemInfo.setCanRefund(canRefund);
			long adultQuantity=0;
            long childQuantity=0;
            if(ordOrderItem.getContentValueByKey("adult_quantity")!=null){
            	adultQuantity=Long.parseLong(String.valueOf(ordOrderItem.getContentValueByKey("adult_quantity")));
            }
            if(ordOrderItem.getContentValueByKey("child_quantity")!=null){
            	childQuantity=Long.parseLong(String.valueOf(ordOrderItem.getContentValueByKey("child_quantity")));
            }
            ordRefundItemInfo.setAdult(adultQuantity);
            ordRefundItemInfo.setChild(childQuantity);
			ordRefundItemInfo.setQuantity(ordOrderItem.getQuantity());
			ordRefundItemInfo.setRefundType(ordOrderItem.getCancelStrategy()!=null?ordOrderItem.getCancelStrategy():"/");
			ordRefundItemInfo.setCategoryId(ordOrderItem.getCategoryId());
			String refundWay = OrdPartRefundEnum.RenfundWayEnum.QUANTITY.name();
			if(ordOrderItem.getCategoryId() == 3L){
				refundWay = OrdPartRefundEnum.RenfundWayEnum.PERSON.name();
			}else{
				boolean travAllFlag = false;
				if(ordOrderItem.getContentValueByKey("travAllFlag")!=null){
					travAllFlag=(Boolean) ordOrderItem.getContentValueByKey("travAllFlag");
	            }
				if(travAllFlag){
					refundWay = OrdPartRefundEnum.RenfundWayEnum.PERSON.name();
				}else if(isFront && containBx){
					refundWay = OrdPartRefundEnum.RenfundWayEnum.PERSON.name();
				}
			}
			LOG.info("RenfundWay:"+ refundWay);
			ordRefundItemInfo.setRenfundWay(refundWay);
			if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(refundWay)){
					OrdItemResidueRefundInfo info = queryOrdResidueRefundQuantityOrPerson(ordOrderItem.getOrderItemId(), refundWay).getReturnContent();
					refundPersonIds = info.getListOrdPerson();
					refundQuantity = info.getQuantity();
					List<OrdPerson> refundPersons = new ArrayList<OrdPerson>();
					for (String refundPersonId : refundPersonIds) {
						OrdPerson refundPerson = ordPersonService.findOrdPersonById(Long.valueOf(refundPersonId));
						refundPersons.add(refundPerson);
					}
					ordRefundItemInfo.setRefundQuantity(refundQuantity);
					ordRefundItemInfo.setRefundPersons(refundPersons);
				} else {
					refundQuantity = queryOrdResidueRefundQuantityOrPerson(ordOrderItem.getOrderItemId(), refundWay).getReturnContent().getQuantity();
					ordRefundItemInfo.setRefundQuantity(refundQuantity);
				} 
			}
			LOG.info("OrdRefundItemInfo: "+GsonUtils.toJson(ordRefundItemInfo));
			refundItems.add(ordRefundItemInfo);
		}
		resultHandle.setReturnContent(refundItems);
		return resultHandle;
	}


	@Override
	public ResultHandleT<Map<String,Object>> partRefundSubmit(OrdRefundInfo ordRefundInfo){
		return partRefundSubmit(ordRefundInfo, false);
	}

	@Override
	public ResultHandleT<Map<String,Object>> partRefundSubmit(OrdRefundInfo ordRefundInfo, boolean isFront) {
		ResultHandleT<Map<String, Object>> resultHandle =new ResultHandleT<Map<String,Object>>();;
		LOG.info("partRefundSubmit start:"+ordRefundInfo.getOrderId()+"  "+GsonUtils.toJson(ordRefundInfo));
		try {
			Map<String,Object> returnMsg = new HashMap<String, Object>();
			resultHandle.setReturnContent(returnMsg);
			returnMsg.put("SUBMIT", false);
			returnMsg.put("IS_CHANGE_QUANTITY", false);
			returnMsg.put("IS_CHANGE_AMOUNT",false);
			returnMsg.put("NOW_REFUND_AMOUNT",0L);
			returnMsg.put("MSG","");
			
			//1.份数校验
			if(!checkRefundQuantity(ordRefundInfo)){
				returnMsg.put("IS_CHANGE_QUANTITY", true);
				returnMsg.put("MSG","退款份数不满足条件");
				LOG.info("partRefundSubmit:"+ordRefundInfo.getOrderId()+" result:"+GsonUtils.toJson(resultHandle));
				return resultHandle;
			}

			if(isFront){
				//2.金额校验
				Map<String, Object> map = checkRefundAmount(ordRefundInfo);
				Boolean isChangeAmout =  !(Boolean)map.get("success");
				if(isChangeAmout){
					returnMsg.put("IS_CHANGE_AMOUNT",true);
					returnMsg.put("NOW_REFUND_AMOUNT",map.get("newAmount"));
					returnMsg.put("MSG","退款金额已发生改变，新的退款金额为："+map.get("newAmount"));
					LOG.info("partRefundSubmit:"+ordRefundInfo.getOrderId()+" result:"+GsonUtils.toJson(resultHandle));
					return  resultHandle;
				}
			}

			
			OrdOrder ordOrder = ordOrderService.findByOrderId(ordRefundInfo.getOrderId());
			ordRefundInfo.setBuCode(ordOrder.getBuCode());
			long actualAmount = ordOrder.getActualAmount()!= null?ordOrder.getActualAmount().longValue():0;
			
		    List<OrdRefundment> ordRefundments = orderRefundmentServiceRemote
					.queryOrdRefundmentByOrderId(ordRefundInfo.getOrderId());
			long refunds = 0L;
			for (OrdRefundment orf : ordRefundments) {
				//退款总金额
				if(!Constant.REFUNDMENT_STATUS.REJECTED.name().equals(orf.getStatus()) && !Constant.REFUNDMENT_STATUS.CANCEL.name().equals(orf.getStatus())
						&& !Constant.REFUNDMENT_STATUS.FAIL.name().equals(orf.getStatus())){
					refunds += orf.getAmount()!=null? orf.getAmount().longValue():0;
				}
			}
			
			if(ordRefundInfo.getRefundAmount().longValue()>actualAmount-refunds){
				String msg = "实付金额:"+PriceUtil.trans2YuanStr(actualAmount)+",已退款金额:"+PriceUtil.trans2YuanStr(refunds)+",退款金额不能大于【订单总金额】!";
				returnMsg.put("IS_AMOUNT_OUT",true);
				returnMsg.put("MSG",msg);
				LOG.info("partRefundSubmit:"+ordRefundInfo.getOrderId()+" result:"+GsonUtils.toJson(resultHandle));
				return  resultHandle;
			}
			
			
			//3.供应商审核  生成退款申请  退款明细
			LOG.info("partRefundSubmit audit start:");
			String auditStatus = partRefundAudit(ordRefundInfo);
			LOG.info("partRefundSubmit audit result:"+auditStatus);
			
			//4.更新退款锁定份数、人员
            List<OrdRefundUpdateInfo> ordRefundUpdateInfos = new ArrayList<OrdRefundUpdateInfo>();
			for(OrdRefundItemInfo refundItemInfo:ordRefundInfo.getRefundItems() ){
				Long changeQuantity = refundItemInfo.getRefundQuantity();
				
				if(changeQuantity != 0){
					OrdRefundUpdateInfo ordRefundUpdateInfo = new OrdRefundUpdateInfo();
					ordRefundUpdateInfo.setOrderItemId(refundItemInfo.getOrderItemId());
					ordRefundUpdateInfo.setChangeQuantity(changeQuantity);
					if("PERSON".equals(refundItemInfo.getRenfundWay())){ //实名制
						ordRefundUpdateInfo.setRefundWay("PERSON");
						ordRefundUpdateInfo.setPersonBefore("");
						ordRefundUpdateInfo.setPersonAfter(refundItemInfo.getRefundPersonStr());		
					}
					ordRefundUpdateInfos.add(ordRefundUpdateInfo);
				}
			}
			updateRefundLock(ordRefundUpdateInfos);

			//部分退全部数量退款时取消订单
			if (isPartRefundAll(ordRefundInfo)) {
				ordRefundInfo.setPartRefundTag(Constant.PARTREFUNDTAG_ALL);
			}else{
				ordRefundInfo.setPartRefundTag(Constant.PARTREFUNDTAG_PART);
			}

			cancelPartRefundOrder(ordRefundInfo);
			returnMsg.put("SUBMIT", true);
			returnMsg.put("MSG","退款申请已提交");
		} catch (Exception e) {
			LOG.info("partRefundSubmit:"+ordRefundInfo.getOrderId()+" error:"+e.toString());
			resultHandle.setMsg("partRefundSubmit:"+ordRefundInfo.getOrderId()+" error:"+e.getMessage());
		}
		LOG.info("partRefundSubmit:"+ordRefundInfo.getOrderId()+" result:"+GsonUtils.toJson(resultHandle));
		return resultHandle;
	}

	/**
	 * 是否部分退全部数量退款
	 */
	private boolean isPartRefundAll(OrdRefundInfo ordRefundInfo) {
		List<OrdRefundItemInfo> ordRefundItemInfos = ordRefundInfo.getRefundItems();
		for (OrdRefundItemInfo refundItemInfo : ordRefundItemInfos) {
			//门票品类
			if (isTicket(refundItemInfo.getCategoryId())) {
				Long refundQuantity = refundItemInfo.getRefundQuantity();
				ordRefundInfo.setReqRefundQuantity(refundQuantity.toString());
				Long orderItemId = refundItemInfo.getOrderItemId();
				OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
				Long quantity = ordOrderItem.getQuantity();
				//全部数量退款
				if (refundQuantity.equals(quantity)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 取消部分退订单
	 */
	private void cancelPartRefundOrder(OrdRefundInfo ordRefundInfo) {
		Long orderId = ordRefundInfo.getOrderId();
			String code = OrderEnum.ORDER_CANCEL_CODE.SUPER_CANCEL.name();
	        String catReason = "code=" + ordRefundInfo.getCancelCode() + "||" + "reason=" + ordRefundInfo.getReason();
	        LOG.info("cancelPartRefundOrder,订单ID:"+ordRefundInfo.getOrderId()+",原因:"+catReason);
	        if(Constant.PARTREFUNDTAG_ALL.equals(ordRefundInfo.getPartRefundTag())){
	        	ResultHandle cancelHandle= orderService.cancelOrder(orderId,code,catReason,ordRefundInfo.getOperatorName(),"vst在线退款");
	        	if (cancelHandle != null && cancelHandle.isSuccess()) {
	    			LOG.info("订单:" + orderId + "取消成功!");
	    		} else {
	    			LOG.error("订单:" + orderId + "取消失败!");
	    		}
	        }else{
	        	//更新主订单
	        	OrdOrder updateOrdOrder=new OrdOrder();
	        	updateOrdOrder.setOrderId(orderId);
	        	updateOrdOrder.setCancelCode(code);
	        	updateOrdOrder.setReason(catReason);
	        	orderUpdateService.updateOrdOrder(updateOrdOrder);
	        	
	        	//记录日志
	        	OrdOrder order=complexQueryService.queryOrderByOrderId(orderId);
	        	insertOrderLog(order, OrderEnum.ORDER_STATUS.CANCEL.getCode(), ordRefundInfo, "vst在线退款", code, catReason);
	        }
	}
	
	
	
	
	
	/**
	 * 废码需要审核的  废码回调通知
	 */
	@Override
	public void checkPartRefund(Long orderId,Long passSerialno,String status){
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orderId", orderId);
		List<OrdRefundApply> applys = ordRefundApplyServiceRemote.queryRefundApplyByParam(paramMap);
		if(applys != null && !applys.isEmpty()){
			OrdRefundApply apply = applys.get(0);
			if("AUDIT".equals(apply.getRefundType())){
				paramMap.clear();
				paramMap.put("passSerialno", passSerialno);
				List<OrdPassCode> ordPassCodes = ordPassCodeDao.findByParams(paramMap);
				if(ordPassCodes != null && !ordPassCodes.isEmpty()){
					for(OrdPassCode ordPassCode : ordPassCodes){
						updateOrdRefundBatch(apply.getRefundId(),ordPassCode.getOrderItemId(),status);
						checkAuditStatus(apply.getRefundId());
					}
				}
			}
		}
	}
	
	public void checkAuditStatus(Long refundApplyId){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("refundApplyId", refundApplyId);
		List<OrderRefundBatch> batchList = ordRefundBatchService.getOrderRefundBatch(params);
		String auditStatus = calAuditStatus(batchList);
		OrdRefundApply ordRefundApply =  new OrdRefundApply();
		ordRefundApply.setRefundId(refundApplyId);
		if(!OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name().equals(auditStatus)){
			updateAuditStatus(refundApplyId,auditStatus);
			String refundType ="";
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name().equals(auditStatus)){
	        	refundType = "REFUND";
			}else if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(auditStatus)){
				refundType = "SALE";
			}
			ordRefundApply.setRefundType(refundType);
			ordRefundApplyServiceRemote.updateRefundApply(ordRefundApply);
		}		
	}


	@Override
	public String partRefundAudit(OrdRefundInfo ordRefundInfo) {
		
		Map<Long,String[]> statuslist_ = new HashMap<Long, String[]>();
		for(OrdRefundItemInfo item :ordRefundInfo.getRefundItems()){
			if(item.getRefundQuantity() > 0){
				String str = "";
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(item.getRenfundWay())){				
					str = concatRefundPesonNames(ordRefundInfo.getOrderId(), item.getRefundPersons());
				}
				
				statuslist_.put(item.getOrderItemId(), new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name(),str+"提交退款申请"});	
			}
		}
		
		LOG.info("createRefundApply satrt:"+ordRefundInfo.getOrderId());
		//创建退款申请
		Long refundApplyId = createRefundApply(null,ordRefundInfo);
		LOG.info("createRefundApply end:"+refundApplyId);
		
		//生成退款明细   
		saveOrdRefundBatch(ordRefundInfo,statuslist_,OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name(),refundApplyId);
		
		List<OrdRefundItemInfo> qrCodeItems = new ArrayList<OrdRefundItemInfo>();
	    Map<Long,String[]> statuslist = new HashMap<Long, String[]>();
		for(OrdRefundItemInfo item :ordRefundInfo.getRefundItems()){
			if(item.getRefundQuantity() > 0){
				Long ordItemId = item.getOrderItemId();
				if(OrderUtils.isTicketByCategoryId(item.getCategoryId())){//门票
					OrdOrderItem orderItem = orderService.getOrderItem(ordItemId);					
					Map<String, Boolean> noticeMap =  querySupplierNoticeStatus(ordItemId);
					if(orderItem.hasTicketAperiodic() || noticeMap.get("IS_FAX_NOTICE")){// 期票/传真
						statuslist.put(ordItemId, new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name(),"传真订单（期票订单），请联系资审"});
						
					}else if(noticeMap.get("IS_EBK_NOTICE") && noticeMap.get("IS_ENTER_NOT_IN_TIME")){//EBK不能及时通关
						statuslist.put(ordItemId, new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name(),"EBK不能及时通关"});
					}else{// 以上情况均不是 则暂存为成功  如果勾选了对接 要继续判断废码结果
						statuslist.put(ordItemId, new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name(),""});
					}
					
					if(noticeMap.get("IS_SUPPLIER_NOTICE")){//对接  只要勾选了对接  就去废码
						qrCodeItems.add(item);
					}
					
				}else if(item.getCategoryId() == 3L){//保险
					List<Long> ordPersonIds  = new ArrayList<Long>();
					for(OrdPerson p : item.getRefundPersons()){
						ordPersonIds.add(p.getOrdPersonId());
					}
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("ordItemId", ordItemId);
					params.put("ordPersonIds", ordPersonIds);
					ResultMessage  result = insPolicyClientService.abolishPolicy(params).getReturnContent();
					List<Long> successOrdPersonIds = (List<Long>) result.getAttributes().get("successOrdPersonIds");
					if(successOrdPersonIds.size() < ordPersonIds.size()){
						statuslist.put(ordItemId, new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name(),"废保失败"});
					}else{
						statuslist.put(ordItemId, new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name(),""});
					}
				}
			}
			
		}
		Map<String,List<OrdRefundItemInfo>> qrCodeMaps  = new HashMap<String, List<OrdRefundItemInfo>>();
		for(OrdRefundItemInfo item :qrCodeItems){
			
			OrdPassCode ordPassCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(item.getOrderItemId());
			if(ordPassCode == null){
				String status = statuslist.get(item.getOrderItemId())[0];
				if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name().equals(status)){//原状态为审核成功 ,则将审核状态更新为废码结果
					statuslist.put(item.getOrderItemId(), new String[]{OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name(),"查不到申码记录"});
				}else{ //原状态为审核失败，则审核状态不变，并在具备里拼接废码结果
					String desc = statuslist.get(item.getOrderItemId())[1];
					statuslist.put(item.getOrderItemId(), new String[]{status,desc+"(废码结果:查不到申码记录)"});
				}
			}else{
				if(qrCodeMaps.containsKey(ordPassCode.getPassSerialno())){
	            	qrCodeMaps.get(ordPassCode.getPassSerialno()).add(item);
	            }else{
	            	List<OrdRefundItemInfo> list = new ArrayList<OrdRefundItemInfo>();
	            	list.add(item);
	            	qrCodeMaps.put(ordPassCode.getPassSerialno(), list);
	            }
			}
            
		}
		for(Entry<String, List<OrdRefundItemInfo>> entry:qrCodeMaps.entrySet()){
			// 调取废码接口
			String code = partDestroyCode(entry.getKey(), entry.getValue(), ordRefundInfo.getOperatorName());
			LOG.info("partDestroyCode_"+entry.getKey()+" result:"+code);
			for(OrdRefundItemInfo item:entry.getValue()){
				String destoryResult = "";
				String destoryDesc = "";
				if("DESTROYED_SUCCESS".equals(code) || "DESTROYED_PART_SUCCESS_NEW".equals(code)){
					destoryResult = OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name();
					destoryDesc = "废码成功";
				}else if("DESTROYED_AUDIT".equals(code) || "DESTROYED_PART_AUDIT_NEW".equals(code)){
					destoryResult = OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name();
					destoryDesc = "废码需要审核";
				}else{
					destoryResult = OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name();
					destoryDesc = "废码失败";
				}
				
				String status = statuslist.get(item.getOrderItemId())[0];
				if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name().equals(status)){//原状态为审核成功 ,则将审核状态更新为废码结果
					statuslist.put(item.getOrderItemId(), new String[]{destoryResult,
						OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(destoryResult)?"废码失败":""});
				}else{ //原状态为审核失败，则审核状态不变，并在具备里拼接废码结果
					String desc = statuslist.get(item.getOrderItemId())[1];
					statuslist.put(item.getOrderItemId(), new String[]{status,desc+"(废码结果:"+destoryDesc+")"});
				}
			}
		}
		String auditStatus = calAuditStatus(statuslist);
		
		if(!OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(auditStatus)){
			if(hasRetreatInsurance(ordRefundInfo.getOrderId())){
				auditStatus = OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name();//含有意外险  走售后
			}
		}
		
		updateRefundApply(auditStatus, refundApplyId,ordRefundInfo,statuslist);
		
		updateOrdRefundBatch(statuslist,auditStatus,refundApplyId);
		
		return auditStatus;
	}

	public String concatRefundPesonNames(Long orderId,List<OrdPerson> refundPersons){
		String str = "";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("objectId", orderId);
		params.put("personType", ORDER_PERSON_TYPE.TRAVELLER.name());
		List<OrdPerson> ps = ordPersonService.findOrdPersonList(params);
		//拼接退款人员姓名
		if(ps != null && !ps.isEmpty()){ 
			for(OrdPerson p : refundPersons){
				for(OrdPerson p_ : ps){
					if(p.getOrdPersonId().equals(p_.getOrdPersonId())){
						str += p_.getFullName()+",";
						break;
					}
				}
			}
		}
		return str.substring(0, str.length()-1);
	}

	@Override
	public void updateRefundQuantityOrPerson(List<OrdRefundUpdateInfo> ordRefundUpdateInfos) {
		LOG.info("UpdateRefundQuantityOrPerson log start");
		for (OrdRefundUpdateInfo ordRefundUpdateInfo : ordRefundUpdateInfos) {
			Long changeQuantity = ordRefundUpdateInfo.getChangeQuantity();
			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(ordRefundUpdateInfo.getOrderItemId());
			LOG.info("UpdateRefundQuantityOrPerson Get OrderInfo---OrderId :"+ordOrderItem.getOrderId()+", OrderItemId: "+ordOrderItem.getOrderItemId());
			Long itemRefundQuantity = ordOrderItem.getRefundQuantity() != null ? ordOrderItem.getRefundQuantity() : 0L;
			ordOrderItem.setRefundQuantity(itemRefundQuantity + changeQuantity);
			int updateOrdOrderItem = ordOrderItemService.updateOrdOrderItem(ordOrderItem);
			LOG.info("UpdateRefundQuantityOrPerson update return:"+updateOrdOrderItem);
			OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDAO.getOrdPartRefundItemByOrderItemId(ordRefundUpdateInfo.getOrderItemId());
			LOG.info("UpdateRefundQuantityOrPerson Get ordPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
			if(ordPartRefundItem != null){
				Long itemRefundQuantity2 = ordPartRefundItem.getRefundQuantity() != null ? ordPartRefundItem.getRefundQuantity() : 0L;
				ordPartRefundItem.setRefundQuantity(itemRefundQuantity2 + changeQuantity);
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(ordRefundUpdateInfo.getRefundWay())){
					String startRefundPerson = ordPartRefundItem.getRefundPerson() != null ? ordPartRefundItem.getRefundPerson() : "";
					String personAfter = ordRefundUpdateInfo.getPersonAfter() != null ? ordRefundUpdateInfo.getPersonAfter() : "";
					List<String> startRefundPersonList = new ArrayList<String>();
					if(StringUtils.isNotEmpty(startRefundPerson)){
						String[] startRefundPersons = startRefundPerson.split(",");
						for (int i = 0; i < startRefundPersons.length; i++) {
							startRefundPersonList.add(startRefundPersons[i]);
						}
					}
					if(StringUtils.isNotEmpty(personAfter)){
						String[] personsAfter = personAfter.split(",");
						for (int i = 0; i < personsAfter.length; i++) {
							if(startRefundPersonList.contains(personsAfter[i])){
								continue;
							}
							startRefundPersonList.add(personsAfter[i]);
						}
					}
					String finalRefundPerson = "";
					for (int i = 0; i < startRefundPersonList.size(); i++) {
						finalRefundPerson += startRefundPersonList.get(i);
						if(i < startRefundPersonList.size() - 1){
							finalRefundPerson += ",";
						}
					}
					ordPartRefundItem.setRefundPerson(finalRefundPerson);
				}
				LOG.info("UpdateRefundQuantityOrPerson update OrdPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
				ordPartRefundItemDAO.updateOrdPartRefundItem(ordPartRefundItem);
			} else {
				ordPartRefundItem = new OrdPartRefundItem();
				ordPartRefundItem.setOrderItemId(ordRefundUpdateInfo.getOrderItemId());
				ordPartRefundItem.setOrderId(ordOrderItem.getOrderId());
				ordPartRefundItem.setRefundQuantity(changeQuantity);
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(ordRefundUpdateInfo.getRefundWay())){
					String personAfter = ordRefundUpdateInfo.getPersonAfter() != null ? ordRefundUpdateInfo.getPersonAfter() : "";
					ordPartRefundItem.setRefundPerson(personAfter);
				}
				LOG.info("UpdateRefundQuantityOrPerson add OrdPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
				ordPartRefundItemDAO.insertSelective(ordPartRefundItem);
			}
		}
	}


	@Override
	public void updateRefundLock(List<OrdRefundUpdateInfo> ordRefundUpdateInfos) {
		LOG.info("UpdateRefundLock log start");
		for (OrdRefundUpdateInfo ordRefundUpdateInfo : ordRefundUpdateInfos) {
			Long changeQuantity = ordRefundUpdateInfo.getChangeQuantity();
			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(ordRefundUpdateInfo.getOrderItemId());
			LOG.info("UpdateRefundLock Get OrderInfo---OrderId :"+ordOrderItem.getOrderId()+", OrderItemId: "+ordOrderItem.getOrderItemId());
			Long itemRefundLockQuantity = ordOrderItem.getRefundLockQuantity() != null ? ordOrderItem.getRefundLockQuantity() : 0L;
			ordOrderItem.setRefundLockQuantity(itemRefundLockQuantity + changeQuantity);
			int updateOrdOrderItem = 0;
			try {
				updateOrdOrderItem = ordOrderItemService.updateOrdOrderItem(ordOrderItem);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.info("updateRefundLock update ordOrderItem error, OrderId :"+ordOrderItem.getOrderId()+", OrderItemId: "+ordOrderItem.getOrderItemId());
				ExceptionFormatUtil.getTrace(e);
			}
			LOG.info("UpdateRefundLock update return:"+updateOrdOrderItem);
			OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDAO.getOrdPartRefundItemByOrderItemId(ordRefundUpdateInfo.getOrderItemId());
			LOG.info("UpdateRefundLock Get ordPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
			if(ordPartRefundItem != null){
				Long itemRefundLockQuantity2 = ordPartRefundItem.getRefundLockQuantity() != null ? ordPartRefundItem.getRefundLockQuantity() : 0L;
				ordPartRefundItem.setRefundLockQuantity(itemRefundLockQuantity2 + changeQuantity);
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(ordRefundUpdateInfo.getRefundWay())){
					String startRefundLockPerson = ordPartRefundItem.getRefundLockPerson() != null ? ordPartRefundItem.getRefundLockPerson() : "";
					String personBefore = ordRefundUpdateInfo.getPersonBefore() != null ? ordRefundUpdateInfo.getPersonBefore() : "";
					String personAfter = ordRefundUpdateInfo.getPersonAfter() != null ? ordRefundUpdateInfo.getPersonAfter() : "";
					List<String> startRefundLockPersonList = new ArrayList<String>();
					if(StringUtils.isNotEmpty(startRefundLockPerson)){
						String[] startRefundLockPersons = startRefundLockPerson.split(",");
						for (int i = 0; i < startRefundLockPersons.length; i++) {
							startRefundLockPersonList.add(startRefundLockPersons[i]);
						}
					}
					if(StringUtils.isNotEmpty(personBefore)){
						String[] personsBefore = personBefore.split(",");
						for (String str : startRefundLockPersonList) {
							for (int i = 0; i < personsBefore.length; i++) {
								if(str == personsBefore[i]){
									startRefundLockPersonList.remove(str);
									break;
								}
							}
						}
					}
					if(StringUtils.isNotEmpty(personAfter)){
						String[] personsAfter = personAfter.split(",");
						for (int i = 0; i < personsAfter.length; i++) {
							if(startRefundLockPersonList.contains(personsAfter[i])){
								continue;
							}
							startRefundLockPersonList.add(personsAfter[i]);
						}
					}
					String finalRefundLockPerson = "";
					for (int i = 0; i < startRefundLockPersonList.size(); i++) {
						finalRefundLockPerson += startRefundLockPersonList.get(i);
						if(i < startRefundLockPersonList.size() - 1){
							finalRefundLockPerson += ",";
						}
					}
					ordPartRefundItem.setRefundLockPerson(finalRefundLockPerson);
				}
				LOG.info("UpdateRefundLock update OrdPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
				ordPartRefundItemDAO.updateOrdPartRefundItem(ordPartRefundItem);
			} else {
				ordPartRefundItem = new OrdPartRefundItem();
				ordPartRefundItem.setOrderItemId(ordRefundUpdateInfo.getOrderItemId());
				ordPartRefundItem.setOrderId(ordOrderItem.getOrderId());
				ordPartRefundItem.setRefundLockQuantity(changeQuantity);
				if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(ordRefundUpdateInfo.getRefundWay())){
					String personAfter = ordRefundUpdateInfo.getPersonAfter() != null ? ordRefundUpdateInfo.getPersonAfter() : "";
					ordPartRefundItem.setRefundLockPerson(personAfter);
				}
				LOG.info("UpdateRefundLock add OrdPartRefundItem:"+GsonUtils.toJson(ordPartRefundItem));
				ordPartRefundItemDAO.insertSelective(ordPartRefundItem);
			}
			
		}
	}


	@Override
	public ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPerson(
			Long orderItemId,String partType) {
		LOG.info("QueryOrdResidueRefundQuantityOrPerson log start");
		ResultHandleT<OrdItemResidueRefundInfo> resultHandle = new ResultHandleT<OrdItemResidueRefundInfo>();
		if (!StringUtils.isNotBlank(orderItemId+"") || !StringUtils.isNotBlank(partType)) {
			resultHandle.setMsg("子订单和类型不能为空");
			return resultHandle;
		}
		OrdItemResidueRefundInfo info = new OrdItemResidueRefundInfo();
		OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		LOG.info("QueryOrdResidueRefundQuantityOrPerson Get OrderInfo---OrderId :"+ordOrderItem.getOrderId()+", OrderItemId: "+orderItemId);
		info.setOrderItemId(orderItemId);
		info.setCategoryId(ordOrderItem.getCategoryId());
		OrdTicketPerform newOrdTicketPerform = ordTicketPerformDao.selectByOrderItem(orderItemId);
		Long performQuantity = 0L;
		if(newOrdTicketPerform != null){
			if(ordOrderItem.getAdultQuantity() + ordOrderItem.getChildQuantity() != 0l){
				performQuantity = ((newOrdTicketPerform.getActualAdult() == null ? 0: newOrdTicketPerform.getActualAdult())
						+ (newOrdTicketPerform.getActualChild() == null ? 0: newOrdTicketPerform.getActualChild()))
						/ (ordOrderItem.getAdultQuantity() + ordOrderItem.getChildQuantity());
			}
		}
		OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDAO.getOrdPartRefundItemByOrderItemId(orderItemId);
		Long refundLockQuantity = 0L;
		if(ordPartRefundItem != null){
			refundLockQuantity = ordPartRefundItem.getRefundLockQuantity() == null ? 0L : ordPartRefundItem.getRefundLockQuantity();
		}
		LOG.info("QueryOrdResidueRefundQuantityOrPerson ordOrderItemQuantity: "+ordOrderItem.getQuantity()+", refundLockQuantity: "+refundLockQuantity+", performQuantity: "+performQuantity);
		long quantity = ordOrderItem.getQuantity() - refundLockQuantity - performQuantity;
		long processingQuantity=refundLockQuantity-(ordOrderItem.getRefundQuantity()==null?0L:ordOrderItem.getRefundQuantity());
		info.setQuantity(quantity);
		info.setUsedQuantity(performQuantity);
		info.setProcessingQuantity(processingQuantity);
		if (OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(partType)) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
			List<OrdPerson> ordPersonList = order.getOrdTravellerList();
			String refundLockPerson = "";
			if(ordPartRefundItem != null){
				refundLockPerson = ordPartRefundItem.getRefundLockPerson() == null ? "" : ordPartRefundItem.getRefundLockPerson();
			}
			String performPerson = "";
			if(newOrdTicketPerform != null){
				performPerson = newOrdTicketPerform.getPerformPeopleInfo() == null ? "" : newOrdTicketPerform.getPerformPeopleInfo();
			}
			LOG.info("QueryOrdResidueRefundQuantityOrPerson refundLockPerson: "+refundLockPerson+", performPersonId: "+performPerson+", ordPersonList.size: "+ordPersonList.size());
			String[] refundLockPersonId = new String[0];
			String[] performPersonId = new String[0];
			if(StringUtils.isNotEmpty(refundLockPerson)){
				refundLockPersonId = refundLockPerson.split(",");
			}
			if(StringUtils.isNotEmpty(performPerson)){
				performPersonId = performPerson.split(",");
			}
			List<String> listOrdPerson = new ArrayList<String>();
			for (OrdPerson ordPerson :ordPersonList) {
				listOrdPerson.add(ordPerson.getOrdPersonId().toString());
				for (int i = 0 ;i<refundLockPersonId.length;i++){ //减去退款锁定的人
					if (ordPerson.getOrdPersonId().longValue() == Long.parseLong(refundLockPersonId[i])) {
						listOrdPerson.remove(ordPerson.getOrdPersonId().toString());
					}
				}
				for (int i = 0; i < performPersonId.length; i++) { //减去已使用的人
					if (listOrdPerson.contains(performPersonId[i])) {
						listOrdPerson.remove(performPersonId[i]);
					}
				}
			}
			info.setListOrdPerson(listOrdPerson);
		}
		resultHandle.setReturnContent(info);
		return resultHandle;
	}
	
	@Override
	public ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPersonForAccidentInsurance(
			Long orderItemId) {
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForAccidentInsurance log start");
		ResultHandleT<OrdItemResidueRefundInfo> resultHandle = new ResultHandleT<OrdItemResidueRefundInfo>();
		if (!StringUtils.isNotBlank(orderItemId+"")) {
			resultHandle.setMsg("子订单号不能为空");
			return resultHandle;
		}
		OrdItemResidueRefundInfo info = new OrdItemResidueRefundInfo();
		OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDAO.getOrdPartRefundItemByOrderItemId(orderItemId);
		info.setOrderItemId(orderItemId);
		Long orderId = ordOrderItem.getOrderId();
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForAccidentInsurance Get OrderInfo---OrderId :"+orderId+", OrderItemId: "+orderItemId);
		OrdOrder order = complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
		Long performQuantity = 0L;
		String performPerson = "";
		List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
		for (OrdOrderItem ordOrderItem2 : ordOrderItems) {
			if(OrderUtils.isTicketByCategoryId(ordOrderItem2.getCategoryId())){
				OrdTicketPerform ordTicketPerform = ordTicketPerformDao.selectByOrderItem(ordOrderItem2.getOrderItemId());
				if(ordTicketPerform != null){
					if(ordOrderItem2.getAdultQuantity() + ordOrderItem2.getChildQuantity() != 0l){
						performQuantity += ((ordTicketPerform.getActualAdult() == null ? 0: ordTicketPerform.getActualAdult())
								+ (ordTicketPerform.getActualChild() == null ? 0: ordTicketPerform.getActualChild()));
					}
					performPerson += (ordTicketPerform.getPerformPeopleInfo() == null ? "" : ordTicketPerform.getPerformPeopleInfo()+",");
				}
			}
		}
		Long refundLockQuantity = 0L;
		if(ordPartRefundItem != null){
			refundLockQuantity = ordPartRefundItem.getRefundLockQuantity() == null ? 0L : ordPartRefundItem.getRefundLockQuantity();
		}
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForAccidentInsurance ordOrderItemQuantity: "+ordOrderItem.getQuantity()+", refundLockQuantity: "+refundLockQuantity+", performQuantity: "+performQuantity);
		long quantity = ordOrderItem.getQuantity() - refundLockQuantity - performQuantity;
		info.setQuantity(quantity);
		info.setUsedQuantity(performQuantity);
		List<OrdPerson> ordPersonList = order.getOrdTravellerList();
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForAccidentInsurance performPerson: "+performPerson);
		if(StringUtils.isNotEmpty(performPerson)){
			performPerson = performPerson.substring(0, performPerson.length()-1);
		}
		String refundLockPerson = "";
		if(ordPartRefundItem != null){
			refundLockPerson = ordPartRefundItem.getRefundLockPerson() == null ? "" : ordPartRefundItem.getRefundLockPerson();
		}
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForAccidentInsurance refundLockPerson: "+refundLockPerson+", performPersonId: "+performPerson+", ordPersonList.size: "+ordPersonList.size());
		String[] refundLockPersonId = new String[0];
		String[] performPersonId = new String[0];
		if(StringUtils.isNotEmpty(refundLockPerson)){
			refundLockPersonId = refundLockPerson.split(",");
		}
		if(StringUtils.isNotEmpty(performPerson)){
			performPersonId = performPerson.split(",");
		}
		List<String> listOrdPerson = new ArrayList<String>();
		for (OrdPerson ordPerson :ordPersonList) {
			listOrdPerson.add(ordPerson.getOrdPersonId().toString());
			for (int i = 0 ;i<refundLockPersonId.length;i++){ //减去退款锁定的人
				if (ordPerson.getOrdPersonId().longValue() == Long.parseLong(refundLockPersonId[i])) {
					listOrdPerson.remove(ordPerson.getOrdPersonId().toString());
				}
			}
			for (int i = 0; i < performPersonId.length; i++) { //减去已使用的人
				if (listOrdPerson.contains(performPersonId[i])) {
					listOrdPerson.remove(performPersonId[i]);
				}
			}
		}
		info.setListOrdPerson(listOrdPerson);
		resultHandle.setReturnContent(info);
		return resultHandle;
	}

	@Override
	public ResultHandleT<OrdItemResidueRefundInfo> queryOrdResidueRefundQuantityOrPersonForOtherInsurance(
			Long orderItemId) {
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForReturnInsurance log start");
		ResultHandleT<OrdItemResidueRefundInfo> resultHandle = new ResultHandleT<OrdItemResidueRefundInfo>();
		if (!StringUtils.isNotBlank(orderItemId+"")) {
			resultHandle.setMsg("子订单号不能为空");
			return resultHandle;
		}
		OrdItemResidueRefundInfo info = new OrdItemResidueRefundInfo();
		OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		info.setOrderItemId(orderItemId);
		Long orderId = ordOrderItem.getOrderId();
		OrdOrder order = complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForReturnInsurance Get OrderInfo---OrderId :"+orderId+", OrderItemId: "+orderItemId);
		Long performQuantity = 0L;
		List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
		for (OrdOrderItem ordOrderItem2 : ordOrderItems) {
			if(OrderUtils.isTicketByCategoryId(ordOrderItem2.getCategoryId())){
				OrdTicketPerform ordTicketPerform = ordTicketPerformDao.selectByOrderItem(ordOrderItem2.getOrderItemId());
				if(ordTicketPerform != null){
					if(ordOrderItem2.getAdultQuantity() + ordOrderItem2.getChildQuantity() != 0l){
						performQuantity += ((ordTicketPerform.getActualAdult() == null ? 0: ordTicketPerform.getActualAdult())
								+ (ordTicketPerform.getActualChild() == null ? 0: ordTicketPerform.getActualChild()));
					}
				}
			}
		}
		LOG.info("QueryOrdResidueRefundQuantityOrPersonForReturnInsurance performQuantity: "+performQuantity);
		long quantity = 0L;
		if(performQuantity == 0L){
			quantity = 1L;
		}
		info.setQuantity(quantity);
		info.setUsedQuantity(1L - quantity);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
		List<String> listOrdPerson = new ArrayList<String>();
		if(ordItemPersonRelationList != null){
			for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
				listOrdPerson.add(ordItemPersonRelation.getOrdPersonId().toString());
			}
		}
		info.setListOrdPerson(listOrdPerson);
		resultHandle.setReturnContent(info);
		return resultHandle;
	}

	
	private String partDestroyCode(String passSerialNo, List<OrdRefundItemInfo> refundItems, String operatorName){
		if ((!StringUtils.isNotBlank(passSerialNo + "")) || (!StringUtils.isNotBlank(operatorName)) || refundItems == null || refundItems.size() == 0) {
			LOG.error("Part destroy code 参数不能为空!");
		}
		LOG.info("Part destroy code start, SerialNo=" + passSerialNo);
		for (OrdRefundItemInfo ordRefundItemInfo : refundItems) {
			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(ordRefundItemInfo.getOrderItemId());
			ordRefundItemInfo.setQuantity(ordOrderItem.getQuantity());
		}
		return passportService.getPartDestroyResult(passSerialNo, refundItems, operatorName);
	}


	@Override
	public ResultHandleT<Boolean> canPartRefundFront(Long orderId) {
		ResultHandleT<Boolean> resultHandle = new ResultHandleT<Boolean>();
		resultHandle.setReturnContent(false);
		if (!StringUtils.isNotBlank(orderId + "")) {
			resultHandle.setMsg("订单号不能为空");
			return resultHandle;
		}
		OrdOrder ordOrder = ordOrderService.loadOrderWithItemByOrderId(orderId);
		if(!OrderUtils.isTicketByCategoryId(ordOrder.getCategoryId())) {
			resultHandle.setMsg("非门票订单");
			LOG.info("canPartRefundFront orderId=="+orderId+",CategoryId: "+ ordOrder.getCategoryId());
			return resultHandle;
		}
		if(hasRetreatInsurance(orderId)){
			resultHandle.setMsg("订单包含退改险");
			LOG.info("canPartRefundFront orderId=="+orderId+",hasRetreatInsurance: "+ hasRetreatInsurance(orderId));
			return resultHandle;
		}
		Long amountChange = orderDetailAction.getTotalAmountChange(orderId,"ORDER",null);
		if(amountChange != null && amountChange.longValue() != 0L){
			resultHandle.setMsg("订单金额发生改变");
			LOG.info("canPartRefundFront orderId=="+orderId+",AmountChange: "+ amountChange);
			return resultHandle;
		}
		int ticketCount = 0;
		List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
		for (OrdOrderItem ordOrderItem : ordOrderItems) {
			if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
				ticketCount ++;
				if(ticketCount > 1){
					resultHandle.setMsg("多个门票子订单");
					return resultHandle;
				}
				if(ordOrderItem.getLastCancelTime() == null || new Date().after(ordOrderItem.getLastCancelTime())){
					resultHandle.setMsg("最晚取消时间为空或过了最晚取消时间");
					return resultHandle;
				}
				
				if(!ordOrderItem.getCancelStrategy().equalsIgnoreCase(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.PARTRETREATANDCHANGE.name())) {
					resultHandle.setMsg("退改策略非部分退");
					LOG.info("canPartRefundFront orderId=="+orderId+",CancelStrategy: "+ ordOrderItem.getCancelStrategy());
					return resultHandle;
				}
			}
		}
		
		if(!(getLeftQuality(ordOrderItems.get(0).getOrderItemId()).getQuantity() > 0)){
			resultHandle.setMsg("无可退份数");
			LOG.info("canPartRefundFront orderId=="+orderId+",LeftQuality: "+ getLeftQuality(ordOrderItems.get(0).getOrderItemId()).getQuantity());
			return resultHandle;
		}
		
		resultHandle.setReturnContent(true);
		return resultHandle;
	}


	
	public Long createRefundApply(String auditStatus,OrdRefundInfo ordRefundInfo){
		OrdOrder order = orderService.queryOrdorderByOrderId(ordRefundInfo.getOrderId());
		OrdRefundApply refundApply = new OrdRefundApply();
        LOG.info("订单ID:"+ordRefundInfo.getOrderId()+"创建退款申请单");
        refundApply.setOrderId(ordRefundInfo.getOrderId());
        refundApply.setRefundMoney(ordRefundInfo.getRefundAmount());
        refundApply.setSystemType("VST");
        refundApply.setCreateTime(new Date());
        //默认为处理未完成 
        refundApply.setDisposeStatus("N");
        refundApply.setOperatorName(ordRefundInfo.getOperatorName());
        refundApply.setRefundReason(ordRefundInfo.getReason());
        //退款来源
        refundApply.setRefundFrom(ordRefundInfo.getRefundFrom());
        //流转去向
        refundApply.setReturnFrom("");
        refundApply.setPlaceName("");
			
        refundApply.setRefundType("AUDIT");
        refundApply.setRefundStatus(Constant.REFUNDMENT_STATUS.UNVERIFIED.getCode());
        
        refundApply.setActualPay(order.getActualAmount());
        refundApply.setRefundIdentify("");
        refundApply.setPartRefundFlag("Y");
        refundApply.setRefundInfo(getRefundInfoStr(ordRefundInfo.getRefundItems()));
        refundApply.setDeductMoney(ordRefundInfo.getDeductAmount());
        refundApply.setDeductMemo(ordRefundInfo.getDeductMemo());
		return ordRefundApplyServiceRemote.insertRefundApply(refundApply);
	}
	
	public void updateRefundApply(String auditStatus,Long refundApplyId,OrdRefundInfo ordRefundInfo, Map<Long, String[]> statuslist){
		
		OrdRefundApply refundApply = new OrdRefundApply();
		refundApply.setRefundId(refundApplyId);
		
        String refundType = "";
        if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name().equals(auditStatus)){
        	refundType = "REFUND";
		}else if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(auditStatus)){
			refundType = "SALE";
		}else{
			refundType = "AUDIT";
		}
			
        refundApply.setRefundType(refundType);
        //售后
        if(refundType.equals("SALE") || refundType.equals("AUDIT")){
            refundApply.setRefundStatus(Constant.REFUNDMENT_STATUS.UNVERIFIED.getCode());
        }else if(refundType.equals("REFUND")){//退款
            refundApply.setRefundStatus(Constant.REFUNDMENT_STATUS.REFUND_APPLY.getCode());
        }
        refundApply.setRefundInfoDesc(getRefundInfoDesc(ordRefundInfo.getOrderId(), ordRefundInfo.getRefundItems(), statuslist));
        ordRefundApplyServiceRemote.updateRefundApply(refundApply);
	}
	
	
	public Map<String, Boolean> querySupplierNoticeStatus(Long orderItemId){
		LOG.info("============querySupplierNoticeStatus orderItemId={} start",orderItemId);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		Boolean IS_EBK_NOTICE = Boolean.FALSE;       //供应商通知方式是否为EBK
		Boolean IS_FAX_NOTICE = Boolean.FALSE;       //供应商通知方式是否为传真
		Boolean IS_SUPPLIER_NOTICE = Boolean.FALSE;  //通知方式是否为对接
		Boolean IS_SUPPORT_DESTROY_CODE = Boolean.TRUE;  //是否支持废码
		Boolean IS_ENTER_NOT_IN_TIME = Boolean.FALSE;    //不可及时通关
		Boolean IS_ERROR = Boolean.FALSE;
		try{
			String fax = OrderEnum.ORDER_COMMON_TYPE.fax_flag.name();
			String ebk = OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name();
			String supplier = OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name();
			OrdOrderItem ordOrderItem  = orderService.getOrderItem(orderItemId);			
			Map<String,Object> performMap = ordOrderItem.getContentMap();
			String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			if (ProductCategoryUtil.isTicket(categoryCode)) {
				if(!IS_FAX_NOTICE){
					IS_FAX_NOTICE = ordOrderItem.hasContentValue(fax, "Y");
				}

				if(!IS_EBK_NOTICE){
					Map<String,Object> paramUser=new HashMap<String,Object>();
					paramUser.put("cancelFlag", "Y");
					paramUser.put("supplierId", ordOrderItem.getSupplierId());
					List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
					if(ebkUserList!=null&& !ebkUserList.isEmpty()){
						IS_EBK_NOTICE= true;
					}
				}
				if(IS_EBK_NOTICE && !IS_ENTER_NOT_IN_TIME){
					String notInTimeFlag = ordOrderItem
							.getNotInTimeFlag();
					if (notInTimeFlag == null
							|| "".equals(notInTimeFlag)) {
						Long supplierId = ordOrderItem.getSupplierId();
						SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(supplierId).getReturnContent();
						String notInTimeFlag_supplier = suppSupplier.getNotInTimeFlag();
						if("Y".equals(notInTimeFlag_supplier)){
							IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
						}else{
							SuppGoods suppgoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId()).getReturnContent();
							String notInTimeFlag_suppgoods =  suppgoods.getNotInTimeFlag();
							if("Y".equals(notInTimeFlag_suppgoods)){
								IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
							}
						}
					}else{
						if("Y".equals(notInTimeFlag)){
							IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
						}
					}
				}
				if(!IS_SUPPLIER_NOTICE){
					IS_SUPPLIER_NOTICE = ordOrderItem.hasContentValue(supplier, "Y");
				}
				if(!IS_SUPPLIER_NOTICE){
					IS_SUPPLIER_NOTICE = ordOrderItem.hasContentValue(OrderEnum.ORDER_TICKET_TYPE.notify_type.name(), SuppGoods.NOTICETYPE.QRCODE.name());
				}
				if(IS_SUPPORT_DESTROY_CODE){
					//IS_SUPPORT_DESTROY_CODE = supportDestroy(ordOrderItem);
				}
			}
			
		}catch(Exception e){
			LOG.error("querySupplierNoticeStatus orderItemId="+orderItemId+",error", e);
			IS_ERROR = Boolean.TRUE;
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("IS_FAX_NOTICE", IS_FAX_NOTICE);
		map.put("IS_EBK_NOTICE", IS_EBK_NOTICE);
		map.put("IS_ENTER_NOT_IN_TIME",IS_ENTER_NOT_IN_TIME);
		map.put("IS_SUPPLIER_NOTICE", IS_SUPPLIER_NOTICE);
		map.put("IS_SUPPORT_DESTROY_CODE", IS_SUPPORT_DESTROY_CODE);
		LOG.info("============querySupplierNoticeStatus orderItemId={} end, map={}",orderItemId, map.toString());
		return map;
	}
	
	private String calAuditStatus(Map<Long,String[]> statuslist){

		boolean auditFlag = false;
		for(String[] arr : statuslist.values()){
			String  status = arr[0];
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(status)){
				return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name();
			}
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name().equals(status)){
				auditFlag = true;
			}
		}
		if(auditFlag){
			return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name();
		}
		return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name();
	}
	
	private String calAuditStatus(List<OrderRefundBatch> batchs){

		boolean auditFlag = false;
		for(OrderRefundBatch batch : batchs){
			String status = batch.getItemAuditStatus();
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(status)){
				return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name();
			}
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name().equals(status)){
				auditFlag = true;
			}
		}
		if(auditFlag){
			return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name();
		}
		return OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name();
	}
	
	private void saveOrdRefundBatch(OrdRefundInfo ordRefundInfo,Map<Long,String[]> statuslist,String auditStatus,Long refundApplyId){
		for(OrdRefundItemInfo refundItem :ordRefundInfo.getRefundItems()){
			String[] arr = statuslist.get(refundItem.getOrderItemId());
			String itemAuditStatus  = arr[0];
			String memo = arr[1];
			if(itemAuditStatus != null){
				OrderRefundBatch batch =  new OrderRefundBatch();
				batch.setItemAuditStatus(itemAuditStatus);
				batch.setAuditStatus(auditStatus);
				batch.setCreateTime(new Date());
				batch.setMemo(memo);
				if("PC".equals(ordRefundInfo.getRefundFrom())){
					batch.setOperator("前台在线退");
				}else{
					batch.setOperator(ordRefundInfo.getOperatorName());
				}
				batch.setOrderId(ordRefundInfo.getOrderId());
				batch.setOrderItemId(refundItem.getOrderItemId());
				batch.setAuditStatus(null);
				batch.setRefundQuantity(refundItem.getRefundQuantity());
				batch.setRefundApplyId(refundApplyId);
				ordRefundBatchService.insertOrderRefundBatch(batch);
				
				OrderRefundBatchDetail detail = new OrderRefundBatchDetail();
				detail.setCreateTime(new Date());
				detail.setItemAuditStatus(itemAuditStatus);
				detail.setMemo(memo);
				detail.setOrderItemId(refundItem.getOrderItemId());
				detail.setRefundItemBatchId(batch.getId());
				ordRefundBatchDetailService.insertRefundBatchDetail(detail);
			}
		}
	}
	
	private void updateOrdRefundBatch(Map<Long,String[]> statuslist,String auditStatus,Long refundApplyId){
		for(Long ordItemId :statuslist.keySet() ){
			String[] arr = statuslist.get(ordItemId);
			String itemAuditStatus  = arr[0];
			String memo = arr[1];
			if(StringUtil.isNotEmptyString(itemAuditStatus) && !OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_PROCESSING.name().equals(itemAuditStatus)){				
				Map<String,Object> params = new HashMap<String, Object>();
				params.put("refundApplyId", refundApplyId);
				params.put("orderItemId", ordItemId);
				params.put("auditStatus", auditStatus);
				params.put("itemAuditStatus", itemAuditStatus);
				ordRefundBatchService.updateOrderRefundBatch(params);
				
				OrderRefundBatch batch = ordRefundBatchService.getOrderRefundBatch(params).get(0);
				OrderRefundBatchDetail detail = new OrderRefundBatchDetail();
				detail.setCreateTime(new Date());
				detail.setItemAuditStatus(itemAuditStatus);
				detail.setMemo(memo);
				detail.setOrderItemId(batch.getOrderItemId());
				detail.setRefundItemBatchId(batch.getId());
				ordRefundBatchDetailService.insertRefundBatchDetail(detail);
			}
			
		}
		
	}
	
	@Override
	public void updateOrdRefundBatch(Long refundApplyId,String status){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("refundApplyId", refundApplyId);
		params.put("auditStatus", status);
		params.put("itemAuditStatus", status);
		ordRefundBatchService.updateStatus(params);
		
		params.clear();
		params.put("refundApplyId", refundApplyId);
		List<OrderRefundBatch> batchList = ordRefundBatchService.getOrderRefundBatch(params);
		if(batchList!=null && !batchList.isEmpty()){
			String memo = "";
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.RESOURCE_AUDIT.name().equals(status)){
				memo = "本次退款申请中含有审核失败或超时未审核的子订单"; 
				if(hasRetreatInsurance(batchList.get(0).getOrderId())){//有退改限
					memo += "，或含退票险，退款金额需人工核实";
				}
			}
			for(OrderRefundBatch batch :batchList){
				OrderRefundBatchDetail detail = new OrderRefundBatchDetail();
				detail.setCreateTime(new Date());
				detail.setItemAuditStatus(status);
				detail.setMemo(memo);
				detail.setOrderItemId(batch.getOrderItemId());
				detail.setRefundItemBatchId(batch.getId());
				ordRefundBatchDetailService.insertRefundBatchDetail(detail);
			}
		}
		
	}
	
	@Override
	public void updateOrdRefundBatch(Long refundApplyId,Long orderItemId,String status){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		params.put("auditStatus", status);
		params.put("itemAuditStatus", status);
		ordRefundBatchService.updateOrderRefundBatch(params);
		
		params.clear();
		params.put("refundApplyId", refundApplyId);
		params.put("orderItemId", orderItemId);
		List<OrderRefundBatch> batchList = ordRefundBatchService.getOrderRefundBatch(params);
		if(batchList !=null && !batchList.isEmpty()){
			OrderRefundBatch batch= batchList.get(0);
			OrderRefundBatchDetail detail = new OrderRefundBatchDetail();
			detail.setCreateTime(new Date());
			detail.setItemAuditStatus(status);
			detail.setMemo("");
			detail.setOrderItemId(orderItemId);
			detail.setRefundItemBatchId(batch.getId());
			ordRefundBatchDetailService.insertRefundBatchDetail(detail);
		}
	}
	
	/**
	 * 只更新主订单批次审核状态 auditStatus
	 */
	@Override
	public void updateAuditStatus(Long refundApplyId,String status){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("refundApplyId", refundApplyId);
		params.put("auditStatus", status);
		ordRefundBatchService.updateStatus(params);
	}
	
	
	private static String getRefundInfoStr(List<OrdRefundItemInfo> refundItems) {
		StringBuilder refundItemInfo = new StringBuilder();
		for(int i=0; i<refundItems.size(); i++){
			Long orderItemId = refundItems.get(i).getOrderItemId();
			Long refundQuantity = refundItems.get(i).getRefundQuantity();
			String renfundWay = refundItems.get(i).getRenfundWay();
			List<OrdPerson> refundPersons = null;
			if (OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(renfundWay)){
				refundPersons = refundItems.get(i).getRefundPersons();
			}
			refundItemInfo.append(orderItemId + ":" + refundQuantity);
			if(refundPersons != null && !refundPersons.isEmpty()){
				refundItemInfo.append(":");
				for(int j = 0; j<refundPersons.size(); j++) {
					refundItemInfo.append(refundPersons.get(j).getOrdPersonId());
					if(j<refundPersons.size()-1){
						refundItemInfo.append(",");
					}
				}
			}
			if(i<refundItems.size()-1){
				refundItemInfo.append(";");
			}
		}
		return refundItemInfo.toString();
	}

	private  String getRefundInfoDesc(Long orderId,List<OrdRefundItemInfo> refundItems,Map<Long,String[]> statuslist) {
		String str ="";
		for(int i=0; i<refundItems.size(); i++){
			OrdRefundItemInfo item = refundItems.get(i);
			Long orderItemId = refundItems.get(i).getOrderItemId();
			String[] statusStr = statuslist.get(orderItemId);
			
			String strTemp = "子订单号"+orderItemId+",";
			if(OrdPartRefundEnum.RenfundWayEnum.PERSON.name().equalsIgnoreCase(item.getRenfundWay())){
				strTemp += concatRefundPesonNames(orderId, item.getRefundPersons());
			}else{
				strTemp += "申请"+item.getRefundQuantity()+"份";
			}
			
			if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_SUCCESS.name().equals(statusStr[0])){
				strTemp = "【可退】"+strTemp;
			}else if(OrdPartRefundEnum.OrdRefundProcessStatusEnum.AUDIT_FAIL.name().equals(statusStr[0])){
				strTemp = "【待核实】"+strTemp+",原因："+statusStr[1];
			}else{
				strTemp = "【待核实】"+strTemp+",原因：供应商审核中";
			}	
			if(i<refundItems.size()-1){
				strTemp +=";";
			}
			str += strTemp;
		}
		return str;
	}
	


	@Override
	public ResultHandleT<FavorableInfo> queryPartRefundFavorableInfo(
			Long orderId) {
		ResultHandleT<FavorableInfo> result=new ResultHandleT<FavorableInfo>();
		try {
			LOG.info("queryPartRefundFavorableInfo paramorderId=="+orderId);
			if(null!=orderId){
				FavorableInfo favorableInfo=new FavorableInfo();
				//查询优惠卷信息
				List<MarkCouponUsage>  markCouponUsageList=favorService.getMarkCouponUsageByOrderId(orderId);
				//查询促销信息
				OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
				List<OrdOrderItem> orderItems = order.getOrderItemList();
				List<Long> orderItemIdList = new ArrayList<Long>();
				Map<String,Object> params = new HashMap<String, Object>();
				for (OrdOrderItem orderItem : orderItems) {
					orderItemIdList.add(orderItem.getOrderItemId());
				}
				//打包的情况
				Map<String, Object> paramPack = new HashMap<String, Object>();
				//订单号
				paramPack.put("orderId", orderId);
				List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
				if (!orderPackList.isEmpty()) {

				List<Long> orderItemIdList1 = new ArrayList<Long>();
				for (OrdOrderPack ordOrderPack : orderPackList) {
					orderItemIdList1.add(ordOrderPack.getOrderPackId());
				}
				params.put("objectType1","ORDER_PACK");
				params.put("orderItemIdList1", orderItemIdList1);
				}
				params.put("objectType","ORDER_ITEM");
				params.put("orderItemIdList", orderItemIdList);

				List<OrdPromotion> ordPromotions = promotionService.selectOrdPromotionsByOrderItemId(params);
				//是否有优惠信息
				boolean resFlag=false;
				//优惠总金额
				Long totalAmount=0L;
				//优惠信息详情
				List<FavorableInfoDetail> favorableInfoDetailList=new ArrayList<FavorableInfoDetail>();
				if(null!=markCouponUsageList && markCouponUsageList.size()>0){
					resFlag=true;
					FavorableInfoDetail favorableInfoDetail=null;
					for (MarkCouponUsage markCouponUsage : markCouponUsageList) {
						totalAmount+=markCouponUsage.getAmount();
						favorableInfoDetail=new FavorableInfoDetail();
						favorableInfoDetail.setAmount(trans2YuanStr(markCouponUsage.getAmount()));
						favorableInfoDetail.setType("0");
						favorableInfoDetail.setOrderId(orderId);
						favorableInfoDetail.setFavorableTitle("优惠卷");
						favorableInfoDetailList.add(favorableInfoDetail);
					}
					
				}
				if(null!=favorableInfoDetailList && favorableInfoDetailList.size()>0){
					resFlag=true;
					FavorableInfoDetail favorableInfoDetail=null;
					for (OrdPromotion ordPromotion : ordPromotions) {
						totalAmount+=ordPromotion.getFavorableAmount();
						favorableInfoDetail=new FavorableInfoDetail();
						favorableInfoDetail.setAmount(trans2YuanStr(ordPromotion.getFavorableAmount()));
						favorableInfoDetail.setType("1");
						favorableInfoDetail.setOrderId(orderId);
						favorableInfoDetail.setFavorableTitle("促销："+StringUtil.coverNullStrValue(ordPromotion.getPromTitle()));
						favorableInfoDetailList.add(favorableInfoDetail);
					}
				}
				if(resFlag){
					favorableInfo.setOrderId(orderId);
					favorableInfo.setFavorableInfoDetailList(favorableInfoDetailList);
					favorableInfo.setFavorableAmount(trans2YuanStr(totalAmount));
					result.setReturnContent(favorableInfo);
				}
			}else{
				result.setMsg("orderId is null");
			}
		} catch (Exception e) {
			result.setMsg("queryPartRefundFavorableInfo error");
			LOG.error("queryPartRefundFavorableInfo paramorderId=="+orderId+"  ,error=="+e.toString());
		}
		return result;
	}
	
	private static String trans2YuanStr(Long amount) {
		if(amount==null){
			return "";
		}
		final int MULTIPLIER = 100;
		String amountYuan = new BigDecimal(amount).divide(new BigDecimal(MULTIPLIER)).setScale(2).toString();
		return amountYuan;
	}


	private Boolean checkRefundQuantity(OrdRefundInfo ordRefundInfo){
		ResultHandleT<List<OrdRefundItemInfo>> newRefundItems =  queryOrdRefundInfo(ordRefundInfo.getOrderId());
		Map<Long,OrdRefundItemInfo> newRefundItemsMap = new HashMap<Long, OrdRefundItemInfo>(); 
		if(newRefundItems.getReturnContent() != null){
			for(OrdRefundItemInfo item : newRefundItems.getReturnContent()){
				newRefundItemsMap.put(item.getOrderItemId(), item);
			}
		}
		for(OrdRefundItemInfo item:ordRefundInfo.getRefundItems()){
			if(item.getRefundQuantity()>0){
				OrdRefundItemInfo newItem = newRefundItemsMap.get(item.getOrderItemId());
				if(!newItem.isCanRefund() || item.getRefundQuantity()> newItem.getRefundQuantity()){
					return false;
				}
			}
		}
		return true;
	}
	
	private Map<String,Object> checkRefundAmount(OrdRefundInfo ordRefundInfo){
		Map<String,Object> result = new HashMap<String, Object>();
		ResultHandleT<PartRefundAmountInfo> amountInfoRH = queryPartRefundAmountNew(ordRefundInfo,false);
		result.put("success", true);
		result.put("newAmount", null);
		if(amountInfoRH.isSuccess() && amountInfoRH.getReturnContent()!=null){
			PartRefundAmountInfo amountInfo = amountInfoRH.getReturnContent();
			Long amout = amountInfo.getRefundAmount()==null?null:new BigDecimal(amountInfo.getRefundAmount()).multiply(new BigDecimal("100")).longValue();
			if(ordRefundInfo.getRefundAmount() == null || ordRefundInfo.getRefundAmount().longValue() != amout){
				result.put("success", false);
				result.put("newAmount", amout);
			}
//			Long deductMoney = amountInfo.getLossAmount() ==null?null:new BigDecimal(amountInfo.getLossAmount()).multiply(new BigDecimal("100")).longValue();
//			result.put("deductMoney", deductMoney);//扣款金额 分
//			
//			String ticketMemo ="";
//			if(amountInfo.getLossAmountDetailList()!=null && !amountInfo.getLossAmountDetailList().isEmpty()){
//				for(LossAmountDetail detail:amountInfo.getLossAmountDetailList()){
//					ticketMemo +=detail.getLossExplain()+";";
//				}
//				ticketMemo = ticketMemo.substring(0,ticketMemo.length()-1);
//			}
//			String insuranceMemo =amountInfo.getRefundExplain();
//			Map<String,String> map = new HashMap<String, String>();
//			map.put("ticketMemo", ticketMemo);
//			map.put("insuranceMemo", insuranceMemo);
//			result.put("deductMemo", GsonUtils.toJson(map));//扣款说明
			
		}
		LOG.info("checkRefundAmount:"+ordRefundInfo.getOrderId()+" result:"+GsonUtils.toJson(result));
		return result;
	}

	@Deprecated
	@Override
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmount(
			OrdRefundInfo ordRefundInfo,boolean isFront) {
		return calPartRefundAmountComm(ordRefundInfo,isFront,false);
	}
	
	/**
	 * 输出计算金额日志
	 * @param ordRefundInfo
	 * @return
	 */
	private void printParamsLog(OrdRefundInfo ordRefundInfo) {
		if(ordRefundInfo!=null){
			StringBuilder sb=new StringBuilder();
			sb.append("orderId=="+ordRefundInfo.getOrderId()+"  queryPartRefundAmount param==");
			sb.append("refundAmount="+ordRefundInfo.getRefundAmount()+",refundFrom="+ordRefundInfo.getRefundFrom()+",");
			List<OrdRefundItemInfo> refundItems=ordRefundInfo.getRefundItems();
			if(refundItems!=null && !refundItems.isEmpty()){
				for (OrdRefundItemInfo ordRefundItemInfo : refundItems) {
					sb.append("quantity="+ordRefundItemInfo.getQuantity());
					sb.append(",price="+ordRefundItemInfo.getPrice());
					sb.append(",refundQuantity="+ordRefundItemInfo.getRefundQuantity());
					sb.append(",categoryId="+ordRefundItemInfo.getCategoryId());
				}
			}else{
				sb.append("queryPartRefundAmount printParamsLog refundItems is null");
			}
			LOG.info(sb.toString());
		}else{
			LOG.error("queryPartRefundAmount printParamsLog ordRefundInfo is null");
		}
	}

	/**
	 * 计算扣款手续费
	 * @param ordRefundItemInfo
	 * @param orderItem
	 * @param itemLossAmountDetail
	 * @param lossAmountDetailList
	 * @return
	 */
	private Map<String, Object> calculateLossAmount(OrdRefundItemInfo ordRefundItemInfo,
			OrdOrderItem orderItem,
			StringBuilder itemLossAmountDetail,
			List<LossAmountDetail> lossAmountDetailList) {
		Map<String, Object> map = new HashMap<String, Object>();
		Boolean IS_ERROR = Boolean.FALSE;
		Long lossAmount=null;
		try
		{
			LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+"  calculateLossAmount start");
			List<SuppGoodsRefundVO> goodsRefundList = null;
			Date cancleDate = new Date();
			LossAmountDetail lossAmountDetail=new LossAmountDetail();
			lossAmountDetail.setSuppGoodsId(String.valueOf(orderItem.getSuppGoodsId()));
			lossAmountDetail.setSuppGoodsName(orderItem.getSuppGoodsName());
			lossAmountDetail.setOrderItemId(String.valueOf(ordRefundItemInfo.getOrderItemId()));
			lossAmountDetail.setPrice(PriceUtil.trans2YuanStr(orderItem.getPrice()));
			lossAmountDetail.setRefundQuality(String.valueOf(ordRefundItemInfo.getRefundQuantity()));
			//判断是不是期票
			Boolean isAperiodic = orderItem.hasTicketAperiodic();
			if(isAperiodic)
			{
				//取出期票的有效时间
				Date suppGoodExpEndTime = null;
				//首选取快照中的最晚有效期
				Object expEndTime =  orderItem.getContentValueByKey("goodsEndTime");
				if(null != expEndTime){
					String expEndTimeStr = expEndTime.toString();
					SimpleDateFormat sdf=new SimpleDateFormat(DateUtil.HHMM_DATE_FORMAT);
					suppGoodExpEndTime = sdf.parse(expEndTimeStr);
				}
				//历史数据取子订单最晚游玩日有效期
				if(null == suppGoodExpEndTime){
					suppGoodExpEndTime = orderItem.getValidEndTime();
				}
				//最后取商品有效期
				if(null == suppGoodExpEndTime){
					SuppGoodsExp suppGoodExp =  suppGoodsClientService.findTicketSuppGoodsExp(orderItem.getSuppGoodsId());
					suppGoodExpEndTime = suppGoodExp.getEndTime();
				}

				if(null != suppGoodExpEndTime){
					Date lastTime = (Date) suppGoodExpEndTime;
					goodsRefundList = SuppGoodsRefundTools.calcDeductAmtByRefundQuality(orderItem, lastTime, cancleDate,ordRefundItemInfo.getRefundQuantity());
				}else{
					LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount Aperiodic effectiveTime null");
					throw new BusinessException("子订单"+orderItem.getOrderItemId()+"期票有效期为null");
				}
			}else
			{
				goodsRefundList = SuppGoodsRefundTools.calcDeductAmtByRefundQuality(orderItem, orderItem.getVisitTime(), cancleDate,ordRefundItemInfo.getRefundQuantity());
			}
			if(null !=goodsRefundList && goodsRefundList.size()>0){
				//退改信息
				for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
					//根据当前时间匹配的规则
					if(null != suppGoodsRefundVO.getIsCurrent()&&suppGoodsRefundVO.getIsCurrent()){
						if (SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(
								suppGoodsRefundVO.getCancelTimeType())){
							lossAmountDetail.setLossExplain("订单退款,扣除"+
												PriceUtil.trans2YuanStr(suppGoodsRefundVO.getDeductAmt())
												+ "元");
							
						}else{
							Date refundDate = suppGoodsRefundVO.getRefundDate();
							lossAmountDetail.setLossExplain(DateUtil
									.formatDate(refundDate,
											"yyyy年MM月dd日 HH:mm")
									+ "前退款,共扣除"
									+ PriceUtil
											.trans2YuanStr(suppGoodsRefundVO.getDeductAmt())
									+ "元");
						}
						lossAmount=suppGoodsRefundVO.getDeductAmt();
					}
				}
				lossAmountDetailList.add(lossAmountDetail);
				//有退改规则未匹配到
				if(lossAmount==null){
					LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount not valid rules goodsRefundList=="+GsonUtils.toJson(goodsRefundList));
				}else{
					if(lossAmount>0){
						itemLossAmountDetail.append("子订单："+orderItem.getOrderItemId()+"扣款,"+PriceUtil
								.trans2YuanStr(lossAmount)+"=");
					}
					LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount lossAmount=="+lossAmount);
				}
			}else{
				//没有退改规则
				LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount not  rules ");
			}
		}catch(BusinessException be){
			IS_ERROR = Boolean.TRUE;
			map.put("ERROR_MSG", be.getMessage());
			//期票有效期为null
			LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount error  be=="+be.toString());
		}catch(Exception e){
			IS_ERROR = Boolean.TRUE;
			map.put("ERROR_MSG", e.toString());
			//没有退改规则
			LOG.error("OrderItemId=="+ordRefundItemInfo.getOrderItemId()+" calculateLossAmount error  e=="+e.toString());
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("lossAmount", lossAmount);
		return map;
		
		
	}
	
	/**
	 * 获取子订单剩余份数
	 * @param orderItemId
	 * @return
	 */
	private OrdItemResidueRefundInfo getLeftQuality(Long orderItemId) {
		try {
			ResultHandleT<OrdItemResidueRefundInfo>  res=queryOrdResidueRefundQuantityOrPerson(orderItemId,OrdPartRefundEnum.RenfundWayEnum.QUANTITY.name());
			if(res.isSuccess() && res.getReturnContent()!=null){
				LOG.info("orderItemId=="+orderItemId+"  queryPartRefundAmount res=="+GsonUtils.toJson(res.getReturnContent()));
				return res.getReturnContent();
			}
		} catch (Exception e) {
			LOG.error("orderItemId=="+orderItemId+"  queryPartRefundAmount getLeftQuality error=="+e.toString());
		}
		return null;
	}

	@Deprecated
	@Override
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmount(
			OrdRefundInfo ordRefundInfo) {
		return calPartRefundAmountComm(ordRefundInfo,false,true);
	}
	/**
	 * 获取当前订单的原申请退款份数
	 * @param orderId
	 * @param orderItemList 
	 * @return
	 */
	private Map<Long, Long> getOldRefundApplyMap(Long orderId, List<OrdOrderItem> orderItemList) {
		Map<Long,Long> oldRefundApplyMap=new HashMap<Long,Long>();
		OrdRefundApply  ordRefundApply = ordRefundApplyServiceRemote.queryRefundApplyProcessing(orderId);
		if(ordRefundApply !=null){
			if("Y".equals(ordRefundApply.getPartRefundFlag())){
				String[] refundItemInfos = ordRefundApply.getRefundInfo().split(";");
				for(String refundItemInfo:refundItemInfos ){
					String[] arrs = refundItemInfo.split(":");
					Long ordItemId = Long.parseLong(arrs[0]);
					Long refundQuantity = Long.parseLong(arrs[1]);
					oldRefundApplyMap.put(ordItemId, refundQuantity);
				}
			}else{
				LOG.info("calRefundmentAmount orderId=="+orderId+"  ordRefundApply getPartRefundFlag N ");
				//由前台发起的整单退无法使用新计算 经确认采用实付-已退
				oldRefundApplyMap.put(0L, 0L);
			}
		}else{
			LOG.info("calRefundmentAmount orderId=="+orderId+"  ordRefundApplys null ");
		}
		return oldRefundApplyMap;
	}
	
    @Override
	public Boolean hasRefundApplyProcessing(Long orderId){
		return ordRefundApplyServiceRemote.queryRefundApplyProcessing(orderId) != null;
	}
    
    @Override
	public Boolean hasSaleNotClosed(Long orderId){
		return ordRefundApplyServiceRemote.hasSaleNotClosed(orderId);
	}
	
	/**
	 * 获取子订单剩余份数
	 * @param orderItemId
	 * @return
	 */
	private OrdItemResidueRefundInfo getRefundApplyLeftQuality(Long orderItemId,Map<Long,Long> oldRefundApplyMap) {
		try {
			ResultHandleT<OrdItemResidueRefundInfo>  res=queryOrdResidueRefundQuantityOrPerson(orderItemId,OrdPartRefundEnum.RenfundWayEnum.QUANTITY.name());
			if(res.isSuccess() && res.getReturnContent()!=null){
				LOG.info("orderItemId=="+orderItemId+"  queryPartRefundAmount res=="+GsonUtils.toJson(res.getReturnContent()));
				OrdItemResidueRefundInfo ordItemResidueRefundInfo=res.getReturnContent();
				if(oldRefundApplyMap!=null && oldRefundApplyMap.get(orderItemId)!=null){
					ordItemResidueRefundInfo.setQuantity(ordItemResidueRefundInfo.getQuantity()+oldRefundApplyMap.get(orderItemId));
					ordItemResidueRefundInfo.setProcessingQuantity(ordItemResidueRefundInfo.getProcessingQuantity()-oldRefundApplyMap.get(orderItemId));
				}
				return ordItemResidueRefundInfo;
			}
		} catch (Exception e) {
			LOG.error("orderItemId=="+orderItemId+"  queryPartRefundAmount getRefundApplyLeftQuality error=="+e.toString());
		}
		return null;
	}

	@Deprecated
	private ResultHandleT<PartRefundAmountInfo> calPartRefundAmountComm(
			OrdRefundInfo ordRefundInfo,boolean isFront,boolean refundmentFlag) {
		ResultHandleT<PartRefundAmountInfo> result=new ResultHandleT<PartRefundAmountInfo>();
		try {
			printParamsLog(ordRefundInfo);
			PartRefundAmountInfo partRefundAmountInfo=new PartRefundAmountInfo();
			List<LossAmountDetail> lossAmountDetailList =new ArrayList<LossAmountDetail>();
			//计算过程汉字拼接
			StringBuilder refundFormulas=new StringBuilder();
			refundFormulas.append("退款金额=");
			//计算过程数字拼接
			StringBuilder refundFormulaNumber=new StringBuilder();
			refundFormulaNumber.append("退款金额=");
			//计算规则详细展示用List
			List<String> refundFormulaDetails=new ArrayList<String>();
			//获取订单信息
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordRefundInfo.getOrderId());
			//已处理退款的门票订单金额（非实际退款金额）
			Long processedTicketAmount=0L;
			//门票订单总金额(经jiangwenbin确认 改为子订单门票金额累加 )
			Long ticketAmout=0L;
			//子订单list
			List<OrdOrderItem> orderItemList =order.getOrderItemList();
			//拼接个子订单扣款情况
			StringBuilder itemLossAmountDetail=new StringBuilder();
			//申请退款明细
			List<OrdRefundItemInfo> refundItems=ordRefundInfo.getRefundItems();
			//本次退完还剩余的门票子订单金额
			Long leftTicketAmount=0L;
			//已使用门票子订单金额
			Long usedTicketAmount=0L;
			//门票退款扣除金额
			Long ticketLossAmount=0L;
			//可退保险金额
			Long refundInsuranceAmount=0L;
			//本次申请门票退款总金额--用来计算分摊的优惠卷和促销金额
			Long currentRefundTicketAmount=0L;
			//当前在处理的门票退款金额(ps:不算非门票)
			Long refundProcessingAmount=0L;
			//本次总退款金额
			Long currentRefundAmount=0L;
			//本次可退保险数
			int insuranceCount=0;
			Map<Long,Long> oldRefundApplyMap=null;
			if(refundmentFlag){
				LOG.info("orderId="+ordRefundInfo.getOrderId()+" refundmentFlag true getOldRefundApplyMap start");
				oldRefundApplyMap=getOldRefundApplyMap(ordRefundInfo.getOrderId(),orderItemList);
				LOG.info("orderId="+ordRefundInfo.getOrderId()+" refundmentFlag true getOldRefundApplyMap "+GsonUtils.toJson(oldRefundApplyMap));
				if(null!=oldRefundApplyMap && new Long(0).equals(oldRefundApplyMap.get(0L))){
					LOG.info("orderId="+ordRefundInfo.getOrderId()+" calPartRefundAmountComm res getPartRefundFlag n");
				/*	Long actualAmount=order.getActualAmount();
					Long refunds = 0L;
					List<OrdRefundment> ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
					for (OrdRefundment ordRefundment : ordRefundments) {
						//退款总金额
						if(Constant.REFUND_TYPE.ORDER_REFUNDED.name().equals(ordRefundment.getRefundType())){
							refunds += ordRefundment.getAmount();
						}
					}
					currentRefundAmount=actualAmount-refunds;
					String refundAmountStr=PriceUtil.trans2YuanStr(currentRefundAmount);
					partRefundAmountInfo.setOrderId(ordRefundInfo.getOrderId());
					partRefundAmountInfo.setRefundAmount(refundAmountStr);
					LOG.info("orderId="+ordRefundInfo.getOrderId()+" res "+GsonUtils.toJson(partRefundAmountInfo));*/
					result.setMsg("前台发起的整单退调用原方法");
					return result;
				}
			}
			//本次退款是否只包含保险
			boolean onlyInsuranceFlag=true;
			boolean haveInsuranceFlag=false;
			for (OrdOrderItem ordOrderItem : orderItemList) {
				//计算非门票金额 
				if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
					ticketAmout+=ordOrderItem.getPrice()*ordOrderItem.getQuantity();
					if(ordOrderItem.getRefundQuantity()!=null){
						processedTicketAmount+=ordOrderItem.getRefundQuantity()*ordOrderItem.getPrice();
					}
				}
				//该子订单剩余份数和已使用数
				OrdItemResidueRefundInfo ordItemResidueRefundInfo=getRefundApplyLeftQuality(ordOrderItem.getOrderItemId(),oldRefundApplyMap);
				if(ordItemResidueRefundInfo==null){
					//获取剩余份数失败
					result.setErrorCode("0002");
					result.setMsg("获取剩余份数失败");
					LOG.info("calPartRefundAmountComm orderItemId=="+ordOrderItem.getOrderItemId()+"  getLeftQuality fail ");
					return result;
				}
				LOG.info("calPartRefundAmountComm ordItemResidueRefundInfo=="+GsonUtils.toJson(ordItemResidueRefundInfo));
				Long leftQuality=ordItemResidueRefundInfo.getQuantity();
				Long performQuality=ordItemResidueRefundInfo.getUsedQuantity();
				if(OrderUtils.isTicketByCategoryId(ordItemResidueRefundInfo.getCategoryId())){
					refundProcessingAmount+=ordItemResidueRefundInfo.getProcessingQuantity()*ordOrderItem.getPrice();
				}
				boolean haveRefundFlag=false;//当前子订单本次是否提交了退款申请
				//如果有退款申请计算扣款金额
				for (OrdRefundItemInfo ordRefundItemInfo : refundItems) {
					if(ordOrderItem.getOrderItemId().equals(ordRefundItemInfo.getOrderItemId())){
						haveRefundFlag=true;
						//本次退完后还剩余的份数
						Long lastLeftQuality=leftQuality-ordRefundItemInfo.getRefundQuantity();
						//校验当次退款申请份数是否大于剩余份数
						if(lastLeftQuality<0){
							//当前申请退款份数大于剩余份数(数据需要刷新)
							result.setErrorCode("0003");
							result.setMsg("当前申请退款份数大于剩余份数");
							LOG.info("calPartRefundAmountComm orderItemId=="+ordRefundItemInfo.getOrderItemId()+" check quality fail leftQuality=="+leftQuality+",RefundQuantity=="+ordRefundItemInfo.getRefundQuantity());
							return result;
						}else{
							//计算本次退完还剩余的门票子订单金额和本次退款扣除的手续费
							if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId()) ){
								if(null!=ordRefundItemInfo.getRefundQuantity() && ordRefundItemInfo.getRefundQuantity()>0){
									onlyInsuranceFlag=false;
								}
								currentRefundTicketAmount+=ordOrderItem.getPrice()*ordRefundItemInfo.getRefundQuantity();
								if(lastLeftQuality>0){
									//计算剩余门票的价格 对于打包票取打包前价格
									Long price=null;
									if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
										if(ordOrderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.suppGoodsPackageBeforePrice.name())!=null){
											price=Long.valueOf(String.valueOf(ordOrderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.suppGoodsPackageBeforePrice.name())));
											LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  ,packageBeforePrice=="+price);
										}else{
											LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  ,queryPartRefundAmount packageBeforePrice is null");
											//获取打包前价格失败
											result.setErrorCode("0004");
											result.setMsg("获取打包前价格失败");
											return result;
										}
									}else{
										price=ordOrderItem.getPrice();
									}
									LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  ,Price=="+price);
									//本次退完还剩余的门票子订单金额
									leftTicketAmount+=price*lastLeftQuality;
								}else if(performQuality>0){
									usedTicketAmount+=ordOrderItem.getPrice()*performQuality;
								}
								//根据退改规则计算扣款金额
								LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  calculateLossAmount start");
								Map<String, Object>  lossAmountmap=calculateLossAmount(ordRefundItemInfo,ordOrderItem,itemLossAmountDetail,lossAmountDetailList);
								LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  lossAmountmap=="+GsonUtils.toJson(lossAmountmap));
								if((Boolean) lossAmountmap.get("IS_ERROR")){
									LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  calculateLossAmount error=="+lossAmountmap.get("ERROR_MSG"));
									result.setErrorCode("0005");
									result.setMsg(String.valueOf(lossAmountmap.get("ERROR_MSG")));
									return result;
								}else{
									if(lossAmountmap.get("lossAmount")==null){
										//未出错 无退改规则或无匹配退改规则
										LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  calculateLossAmount lossAmount is null");
										partRefundAmountInfo.setRefundAmount(null);
										partRefundAmountInfo.setRefundExplain("本次退款申请无法提交,如有疑问请联系客服!");
										result.setReturnContent(partRefundAmountInfo);
										return result;
									}else{
										Long lossAmount=(Long)lossAmountmap.get("lossAmount");
										LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  calculateLossAmount lossAmount=="+lossAmount);
										ticketLossAmount+=lossAmount;
										
									}
								}
							}else{
								if(ordOrderItem.hasCategory(BIZ_CATEGORY_TYPE.category_insurance)){
									haveInsuranceFlag=true;
									insuranceCount+=ordRefundItemInfo.getRefundQuantity();
								}
								//本次退的保险订单金额 规则门票只有两种险退改险和意外险
								//policy_type= 738取消险 或739退改险 支付后第二天生效 当天可退 
								//意外险游玩日生效 当天也不可退
								//---------------------------------------由于提交时会校验 此处不再校验保险是否可退---------------------
								refundInsuranceAmount+=ordOrderItem.getPrice()*ordRefundItemInfo.getRefundQuantity();
							}
						}
					}
				}
				//当前子订单未提交退款申请 根据剩余份数累加未退的门票金额
				if(!haveRefundFlag && OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
					if(leftQuality>0){
						Long price=null;
						if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
							if(ordOrderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.suppGoodsPackageBeforePrice.name())!=null){
								price=Long.valueOf(String.valueOf(ordOrderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.suppGoodsPackageBeforePrice.name())));
							}else{
								LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  ,queryPartRefundAmount packageBeforePrice is null");
								//获取打包前价格失败
								result.setErrorCode("0004");
								result.setMsg("获取打包前价格失败");
								return result;
							}
						}else{
							price=ordOrderItem.getPrice();
						}
						LOG.info("orderItemId=="+ordOrderItem.getOrderItemId()+"  ,queryPartRefundAmount price=="+price);
						//本次退完还剩余的门票子订单金额
						leftTicketAmount+=price*leftQuality;
					}else if(performQuality>0){
						usedTicketAmount+=ordOrderItem.getPrice()*performQuality;
					}
				}
				
			}
			if(onlyInsuranceFlag && haveInsuranceFlag){
				String currentRefundAmountStr=PriceUtil.trans2YuanStr(refundInsuranceAmount);
				LOG.info("orderId=="+order.getOrderId()+" ,calPartRefundAmountComm onlyInsuranceFlag true" +" currentRefundAmountStr=="+currentRefundAmountStr);
				//各类型空一行
				refundFormulaDetails.clear();
				refundFormulaDetails.add("保险退款:"+currentRefundAmountStr+"元");
				refundFormulaDetails.add(" ");
				refundFormulaDetails.add("退款金额=保险退款");
				refundFormulaDetails.add(" ");
				if(refundInsuranceAmount>0){
					//如果是前台 不返回计算公式
					if(!isFront){
						refundFormulaDetails.add("退款金额="+currentRefundAmountStr+"元");
						partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
						partRefundAmountInfo.setRefundFormulas("退款金额=保险退款");
					}
					if(insuranceCount>0){
						partRefundAmountInfo.setRefundExplain("含可退保险"+insuranceCount+"份");
					}
					partRefundAmountInfo.setOrderId(ordRefundInfo.getOrderId());
					partRefundAmountInfo.setLossAmount("0");
					partRefundAmountInfo.setRefundAmount(currentRefundAmountStr);
					LOG.info("calPartRefundAmountComm res "+GsonUtils.toJson(partRefundAmountInfo));
					result.setReturnContent(partRefundAmountInfo);
					return result;
				
				}else{
					partRefundAmountInfo.setRefundAmount(null);
					partRefundAmountInfo.setRefundExplain("本次退款申请无法提交,如有疑问请联系客服!");
					if(!isFront){
						refundFormulaDetails.add("退款金额="+currentRefundAmountStr+"元(小于等于0,需审核)");
						partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
					}
					result.setReturnContent(partRefundAmountInfo);
					return result;
				}
			}
			LOG.info("calPartRefundAmountComm lossAmountDetailList=="+GsonUtils.toJson(lossAmountDetailList));
			//门票订单总金额
			refundFormulas.append("门票订单总金额");
			refundFormulaNumber.append(PriceUtil.trans2YuanStr(ticketAmout));
			refundFormulaDetails.add("门票订单总金额："+PriceUtil.trans2YuanStr(ticketAmout)+"元");
			if(processedTicketAmount>0){
				refundFormulas.append("-已退款门票金额（包含已扣手续费部分）");
				refundFormulaNumber.append("-"+PriceUtil.trans2YuanStr(processedTicketAmount));
				refundFormulaDetails.add("已退款门票金额（包含已扣手续费部分）："+PriceUtil.trans2YuanStr(processedTicketAmount)+"元");
			}
			if(refundProcessingAmount>0){
				refundFormulas.append("-退款处理中门票金额");
				refundFormulaNumber.append("-"+PriceUtil.trans2YuanStr(refundProcessingAmount));
				refundFormulaDetails.add("退款处理中门票金额："+PriceUtil.trans2YuanStr(refundProcessingAmount)+"元");
			}
			if(usedTicketAmount>0){
				refundFormulas.append("-已使用的门票金额");
				refundFormulaNumber.append("-"+PriceUtil.trans2YuanStr(usedTicketAmount));
				refundFormulaDetails.add("已使用的门票金额："+PriceUtil.trans2YuanStr(usedTicketAmount)+"元");
			}
			if(leftTicketAmount>0){
				refundFormulas.append("-剩余门票金额");
				refundFormulaNumber.append("-"+PriceUtil.trans2YuanStr(leftTicketAmount));
				refundFormulaDetails.add("剩余门票金额："+PriceUtil.trans2YuanStr(leftTicketAmount)+"元");
			}
			//-已退款门票金额-退款处理中金额-剩余门票订单金额-已使用门票订单金额-门票扣款手续费
			currentRefundAmount=ticketAmout	-processedTicketAmount-refundProcessingAmount-leftTicketAmount-usedTicketAmount-ticketLossAmount;
			//各子订单扣款情况
			if(StringUtil.isNotEmptyString(itemLossAmountDetail.toString())){
				String [] itemRefundFormulas=itemLossAmountDetail.toString().split("=");
				if(null!=itemRefundFormulas){
					for(int i=0; i<itemRefundFormulas.length;i++){
						refundFormulas.append("-"+itemRefundFormulas[i].split(",")[0]);
						refundFormulaNumber.append("-"+itemRefundFormulas[i].split(",")[1]);
						refundFormulaDetails.add(itemRefundFormulas[i]+"元");
					}
				}
			}
			if(currentRefundAmount<=0){
				LOG.info("calPartRefundAmountComm ticket amount check fail currentRefundAmount=="+currentRefundAmount);
				partRefundAmountInfo.setRefundAmount(null);
				partRefundAmountInfo.setRefundExplain("本次退款申请无法提交,如有疑问请联系客服!");
				if(!isFront){
					//各类型空一行
					refundFormulaDetails.add(" ");
					refundFormulaDetails.add(refundFormulas.toString());
					refundFormulaDetails.add(" ");
					refundFormulaDetails.add(refundFormulaNumber.toString());
					refundFormulaDetails.add(" ");
					refundFormulaDetails.add("退款金额="+PriceUtil.trans2YuanStr(currentRefundAmount)+"元(小于等于0,需审核)");
					partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
				}
				result.setReturnContent(partRefundAmountInfo);
				return result;
			}else{
				//获取优惠金额,促销金额单位元
				BigDecimal favorTotalAmount=BigDecimal.ZERO;
				//分摊的促销金额 单位分
				Long favorAmount=0L;
				Long favorUsageAmount = favorService.getSumUsageAmount(order.getOrderId());
				Long promotionAmount = order.getOrderAmountCentByType(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name());
				//分销渠道促销优惠金额
				String distributionPrice = orderAmountChangeService.getOrderAmountItemByType(order, OrderEnum.ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name());
				Long distributionCentPrice=0L;
				if(StringUtil.isNotEmptyString(distributionPrice)){
					distributionCentPrice=new BigDecimal(distributionPrice).multiply(new BigDecimal("100")).longValue();
				}
				boolean existFavorAmount=false;
				 if(null!=favorUsageAmount && favorUsageAmount>0){
					 favorTotalAmount=new BigDecimal(favorUsageAmount);
					 existFavorAmount=true;
				 }
				 if(null!=promotionAmount && promotionAmount>0){
					 favorTotalAmount=favorTotalAmount.add(new BigDecimal(promotionAmount));
					 existFavorAmount=true;
				 }
				 if(distributionCentPrice!=0){
					 //分销存在负的促销
					 favorTotalAmount=favorTotalAmount.add(new BigDecimal(distributionCentPrice));
					 existFavorAmount=true;
				 }
				 //存在优惠促销
				 if(existFavorAmount){
					 BigDecimal percent =new BigDecimal(currentRefundTicketAmount).divide(new BigDecimal(ticketAmout), 6, BigDecimal.ROUND_HALF_EVEN);
					 LOG.info("orderId=="+ordRefundInfo.getOrderId()+" calPartRefundAmountComm  percent=="+percent);
					 favorTotalAmount=favorTotalAmount.multiply(percent).setScale(0, BigDecimal.ROUND_HALF_UP);
					 favorAmount=favorTotalAmount.longValue(); 
					 LOG.info("orderId=="+ordRefundInfo.getOrderId()+" calPartRefundAmountComm favorAmount=="+favorAmount);
					 if(favorAmount!=0){
						 refundFormulas.append("-本次退款分摊优惠促销");
						 String favorAmountYuan=PriceUtil.trans2YuanStr(favorAmount);
						 if(favorAmount<0){
							 favorAmountYuan="("+favorAmountYuan+")";
						 }
						 refundFormulaNumber.append("-"+favorAmountYuan); 
						 refundFormulaDetails.add("本次退款分摊优惠促销："+favorAmountYuan+"元");
					 }
				 }
				//+保险费-分摊的促销金额
				currentRefundAmount=currentRefundAmount+refundInsuranceAmount-favorAmount;
				LOG.info("orderId=="+ordRefundInfo.getOrderId()+" calPartRefundAmountComm refundInsuranceAmount=="+refundInsuranceAmount);
				if(refundInsuranceAmount>0){
					 refundFormulas.append("+保险退款");
					 refundFormulaNumber.append("+"+PriceUtil.trans2YuanStr(refundInsuranceAmount));
					 refundFormulaDetails.add("保险退款："+PriceUtil.trans2YuanStr(refundInsuranceAmount)+"元");
				}
				if(currentRefundAmount<=0){
					LOG.info("orderId=="+ordRefundInfo.getOrderId()+" calPartRefundAmountComm ticket amount last check fail currentRefundAmount=="+currentRefundAmount);
					partRefundAmountInfo.setRefundAmount(null);
					partRefundAmountInfo.setRefundExplain("本次退款申请无法提交,如有疑问请联系客服!");
					if(!isFront){
						//各类型空一行
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add(refundFormulas.toString());
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add(refundFormulaNumber.toString());
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add("退款金额="+PriceUtil.trans2YuanStr(currentRefundAmount)+"元(小于等于0,需审核)");
						partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
					}
					result.setReturnContent(partRefundAmountInfo);
					return result;
				}else{
					LOG.error("orderId=="+ordRefundInfo.getOrderId()+" ,calPartRefundAmountComm currentRefundAmount=="+currentRefundAmount+" ,refundFormulas=="+refundFormulas.toString());
					//如果是前台 不返回计算公式
					String currentRefundAmountStr=PriceUtil.trans2YuanStr(currentRefundAmount);
					if(!isFront){
						//各类型空一行
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add(refundFormulas.toString());
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add(refundFormulaNumber.toString());
						refundFormulaDetails.add(" ");
						refundFormulaDetails.add("退款金额="+currentRefundAmountStr+"元");
						partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
						partRefundAmountInfo.setRefundFormulas(refundFormulas.toString());
					}
					if(insuranceCount>0){
						partRefundAmountInfo.setRefundExplain("含可退保险"+insuranceCount+"份");
					}
					partRefundAmountInfo.setOrderId(ordRefundInfo.getOrderId());
					partRefundAmountInfo.setLossAmount(PriceUtil.trans2YuanStr(ticketLossAmount));
					partRefundAmountInfo.setLossAmountDetailList(lossAmountDetailList);
					partRefundAmountInfo.setRefundAmount(currentRefundAmountStr);
					LOG.info("calPartRefundAmountComm res "+GsonUtils.toJson(partRefundAmountInfo));
					result.setReturnContent(partRefundAmountInfo);
					return result;
				}
			}
		} catch (Exception e) {
			result.setMsg("calPartRefundAmountComm error:"+e.toString());
			LOG.error("calPartRefundAmountComm paramorderId=="+"  ,error=="+e.toString());
		}
		return result;
	}
	
	
	public Date getTicketMinLastCancelTime(OrdOrder order) {

		Date minLastCanTime = null;
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			Date lastCanTime = orderItem.getLastCancelTime();

			if (lastCanTime != null) {

				if (minLastCanTime == null) {
					minLastCanTime = lastCanTime;
				} else {

					if (lastCanTime.before(minLastCanTime)) {
						minLastCanTime = lastCanTime;
					}
				}

			}
		}

		return minLastCanTime;
	}
	
	public Boolean hasRetreatInsurance(Long orderId){
		if (StringUtils.isBlank(orderId + "")) {
			LOG.error("Check hasRetreatInsurance failed:订单号为空！");
			return false;
		}
		OrdOrder ordOrder = ordOrderService.loadOrderWithItemByOrderId(orderId);
		List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
		if(ordOrderItems.size() == 0) {
			LOG.error("Check hasRetreatInsurance failed:获取子订单失败, 订单号:" + orderId + " 无子订单信息！");
			return false;
		}
		for (OrdOrderItem ordOrderItem : ordOrderItems) {
			if(ordOrderItem.getCategoryId() == 3L){
				LOG.info("Insurance ordOrderItem.");
				Map<String,Object> param=new HashMap<String,Object>();
				param.put("ordItemId", ordOrderItem.getOrderItemId());
				List<InsPolicy> insPolicies = insPolicyClientService.queryInsPolicy(param);
				if(insPolicies != null && insPolicies.size() != 0){
					InsPolicy insPolicy = insPolicies.get(0);
					if("739".equalsIgnoreCase(insPolicy.getPolicyType())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public OrdPartRefundItem queryOrdPartRefundItem(
			Long orderItemId) {
		OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDAO.getOrdPartRefundItemByOrderItemId(orderItemId);
		return ordPartRefundItem;
	}

	@Override
	public ResultHandleT<PartRefundAmountInfo> queryPartRefundAmountNew(OrdRefundInfo ordRefundInfo, boolean isFront) {
		return calPartRefundAmountCommNew(ordRefundInfo,isFront,false);
	}

	@Override
	public ResultHandleT<PartRefundAmountInfo> calRefundmentAmountNew(OrdRefundInfo ordRefundInfo) {
		return calPartRefundAmountCommNew(ordRefundInfo,false,true);
	}
	
	private ResultHandleT<PartRefundAmountInfo> calPartRefundAmountCommNew(
			OrdRefundInfo ordRefundInfo,boolean isFront,boolean refundmentFlag) {
		try {
			Assert.notNull(ordRefundInfo,"OrdRefundInfo 入参不能为空");
			LOG.info("calPartRefundAmountCommNew input params -> ordRefundInfo = " + JSON.toJSONString(ordRefundInfo) + " ,isFront = " + isFront + " ,refundmentFlag = " + refundmentFlag);

			SumPartRefundAmount sumPartRefundAmount = new SumPartRefundAmount();
			//获取订单信息
			Long orderId = ordRefundInfo.getOrderId();
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			//子订单list
			List<OrdOrderItem> orderItemList = order.getOrderItemList();

			if(refundmentFlag){
				LOG.info("orderId=" + orderId + " refundmentFlag true getOldRefundApplyMap start");
				Map<Long,Long> oldRefundApplyMap = getOldRefundApplyMap(orderId, orderItemList);
				LOG.info("orderId=" + orderId + " refundmentFlag true getOldRefundApplyMap " + GsonUtils.toJson(oldRefundApplyMap));
				if (null != oldRefundApplyMap && new Long(0).equals(oldRefundApplyMap.get(0L))) {
					ResultHandleT<PartRefundAmountInfo> result=new ResultHandleT<PartRefundAmountInfo>();
					LOG.info("orderId=" + orderId + " calPartRefundAmountComm res getPartRefundFlag n");
					result.setMsg("前台发起的整单退调用原方法");
					return result;
				}
			}

			// 所有子订单的扣款明细
			List<LossAmountDetail> lossAmountDetailList = new ArrayList<LossAmountDetail>();
			List<OrdRefundItemInfo> refundItems = ordRefundInfo.getRefundItems();
			if (refundItems != null) {
				for (OrdRefundItemInfo subOrderRefundApplication: refundItems) {
					Long orderItemId = subOrderRefundApplication.getOrderItemId();
					Long refundQuantity = subOrderRefundApplication.getRefundQuantity();
					OrdOrderItem currentSubOrder = getOrdOrderItem(orderItemId, orderItemList);
					Long categoryId = currentSubOrder.getCategoryId();

					if (isTicket(categoryId) || isInsurance(categoryId)) {
						// 门票、组合票、演出票、其他票 或 保险
						OrdRefundmentItemSplit subOrderRefundInfo = orderService.queryOrdItemRefundAmountForTicket(orderItemId, refundQuantity);
						if (subOrderRefundInfo != null) {
							LOG.info(JSON.toJSONString(subOrderRefundInfo));
							//子订单累加金额
							setSumPartRefundAmount(sumPartRefundAmount, refundQuantity, categoryId, subOrderRefundInfo);
							addLossAmountDetail(lossAmountDetailList, subOrderRefundApplication, currentSubOrder, subOrderRefundInfo);
						}
					} else {
						// 其他品类不予处理
						LOG.warn("calPartRefundAmountCommNew order " + order.getOrderId() + " sub-order " + currentSubOrder.getOrderItemId() + " categoryId " + categoryId + " won't be processed!");
					}
				}
			}

			LOG.info("calPartRefundAmountCommNew orderId = " + orderId + JSON.toJSONString(sumPartRefundAmount));

			if(sumPartRefundAmount.getSumOfRefundAmount() <= 0){
				return getNoRefundResult(isFront);
			}else{
				return getCanRefundResult(sumPartRefundAmount, lossAmountDetailList, orderId, isFront);
			}
		} catch (Exception e) {
			LOG.error("calPartRefundAmountCommNew error:", e);
			ResultHandleT<PartRefundAmountInfo> result = getNoRefundResult(isFront);
			result.setMsg("calPartRefundAmountCommNew error:"+e.toString());
			return result;
		}
	}

	private void setSumPartRefundAmount(SumPartRefundAmount sumPartRefundAmount, Long refundQuantity, Long categoryId, OrdRefundmentItemSplit subOrderRefundInfo) {
		//退款份数对应实付总金额
		sumPartRefundAmount.setSumOfActualPaid(sumPartRefundAmount.getSumOfActualPaid() + subOrderRefundInfo.getRefundOughtPrice());
		//退款份数对应扣款总金额
		sumPartRefundAmount.setSumOfDeductedAmount(sumPartRefundAmount.getSumOfDeductedAmount()+ subOrderRefundInfo.getInitActualLoss());
		//退款份数对应退款总金额
		sumPartRefundAmount.setSumOfRefundAmount(sumPartRefundAmount.getSumOfRefundAmount()+ subOrderRefundInfo.getInitRefundedPrice());
		//保险
		if (isInsurance(categoryId)) {
            //保险退款总额
            sumPartRefundAmount.setSumOfInsuranceRefundAmount(sumPartRefundAmount.getSumOfInsuranceRefundAmount()+ subOrderRefundInfo.getInitRefundedPrice());
            //保险退款总份数
            sumPartRefundAmount.setSumOfInsuranceRefundQuantity(sumPartRefundAmount.getSumOfInsuranceRefundQuantity()+ refundQuantity) ;
        }
	}

	private class SumPartRefundAmount {
		//退款总金额
		private Long sumOfRefundAmount = 0L;
		// 各子订单退款份数对应的实付金额之和
		private Long sumOfActualPaid = 0L;
		// 各子订单扣款之和
		private Long sumOfDeductedAmount = 0L;
		// 保险退款总额
		private Long sumOfInsuranceRefundAmount = 0L;
		// 保险退款总份数
		private Long sumOfInsuranceRefundQuantity = 0L;

		public Long getSumOfRefundAmount() {
			return sumOfRefundAmount;
		}

		public void setSumOfRefundAmount(Long sumOfRefundAmount) {
			this.sumOfRefundAmount = sumOfRefundAmount;
		}

		public Long getSumOfActualPaid() {
			return sumOfActualPaid;
		}

		public void setSumOfActualPaid(Long sumOfActualPaid) {
			this.sumOfActualPaid = sumOfActualPaid;
		}

		public Long getSumOfDeductedAmount() {
			return sumOfDeductedAmount;
		}

		public void setSumOfDeductedAmount(Long sumOfDeductedAmount) {
			this.sumOfDeductedAmount = sumOfDeductedAmount;
		}

		public Long getSumOfInsuranceRefundAmount() {
			return sumOfInsuranceRefundAmount;
		}

		public void setSumOfInsuranceRefundAmount(Long sumOfInsuranceRefundAmount) {
			this.sumOfInsuranceRefundAmount = sumOfInsuranceRefundAmount;
		}

		public Long getSumOfInsuranceRefundQuantity() {
			return sumOfInsuranceRefundQuantity;
		}

		public void setSumOfInsuranceRefundQuantity(Long sumOfInsuranceRefundQuantity) {
			this.sumOfInsuranceRefundQuantity = sumOfInsuranceRefundQuantity;
		}
	}

	private ResultHandleT<PartRefundAmountInfo> getCanRefundResult(SumPartRefundAmount sumPartRefundAmount, List<LossAmountDetail> lossAmountDetailList,Long orderId, Boolean isFront) {
		ResultHandleT<PartRefundAmountInfo> result = new ResultHandleT<PartRefundAmountInfo>();
		PartRefundAmountInfo partRefundAmountInfo = new PartRefundAmountInfo();
		//计算过程汉字拼接
		StringBuilder refundFormulas = new StringBuilder();
		refundFormulas.append("退款金额=");
		//计算过程数字拼接
		StringBuilder refundFormulaNumber = new StringBuilder();
		refundFormulaNumber.append("退款金额=");
		//计算规则详细展示用List
		List<String> refundFormulaDetails = new ArrayList<String>();
		//子订单扣款展示
		if (sumPartRefundAmount.getSumOfDeductedAmount() > 0) {
			for (LossAmountDetail lossAmountDetail : lossAmountDetailList) {
				String orderItemId = lossAmountDetail.getOrderItemId();
				Long deductedAmount = lossAmountDetail.getDeductedAmount();
				refundFormulas.append("-子订单：" + orderItemId + "扣款");
				refundFormulaNumber.append("-" + deductedAmount);
				refundFormulaDetails.add("子订单：" + orderItemId + "扣款," + deductedAmount + "元");
			}
		}
		//保险展示
		if (sumPartRefundAmount.getSumOfInsuranceRefundAmount() > 0) {
			refundFormulas.append("+保险退款");
			String yuanStr = PriceUtil.trans2YuanStr(sumPartRefundAmount.getSumOfInsuranceRefundAmount());
			refundFormulaNumber.append("+" + yuanStr);
			refundFormulaDetails.add("保险退款：" + yuanStr + "元");
			partRefundAmountInfo.setRefundExplain("含可退保险" + sumPartRefundAmount.getSumOfInsuranceRefundQuantity() + "份");
		}
		//如果是前台 不返回计算公式
		setFormula(isFront, refundFormulaDetails, refundFormulas, refundFormulaNumber, sumPartRefundAmount.getSumOfRefundAmount(), partRefundAmountInfo);
		partRefundAmountInfo.setOrderId(orderId);
		partRefundAmountInfo.setLossAmount(PriceUtil.trans2YuanStr(sumPartRefundAmount.getSumOfDeductedAmount()));
		partRefundAmountInfo.setRefundAmount(PriceUtil.trans2YuanStr(sumPartRefundAmount.getSumOfRefundAmount()));
		partRefundAmountInfo.setLossAmountDetailList(lossAmountDetailList);
		result.setReturnContent(partRefundAmountInfo);
		LOG.info("calPartRefundAmountCommNew result = " + JSON.toJSONString(result));
		return result;
	}

	private ResultHandleT<PartRefundAmountInfo> getNoRefundResult(Boolean isFront) {
		ResultHandleT<PartRefundAmountInfo> result = new ResultHandleT<PartRefundAmountInfo>();
		PartRefundAmountInfo partRefundAmountInfo = new PartRefundAmountInfo();
		partRefundAmountInfo.setRefundAmount(null);
		partRefundAmountInfo.setRefundExplain("本次退款申请无法提交,如有疑问请联系客服!");
		if (!isFront) {
			List<String> refundFormulaDetails = new ArrayList<String>();
			refundFormulaDetails.add(" ");
			refundFormulaDetails.add("退款金额需审核");
			partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
		}
		result.setReturnContent(partRefundAmountInfo);
		LOG.info("calPartRefundAmountCommNew result = " + JSON.toJSONString(result));
		return result;
	}

	private boolean isTicket(Long categoryId) {
		return OrderUtils.isTicketByCategoryId(categoryId);
	}

	private boolean isInsurance(Long categoryId) {
		return BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(categoryId);
	}

	private OrdOrderItem getOrdOrderItem(Long orderItemId, List<OrdOrderItem> orderItemList) {
		for (OrdOrderItem ordOrderItem : orderItemList) {
			if (ordOrderItem.getOrderItemId().equals(orderItemId)) {
				return ordOrderItem;
			}
		}
		throw new IllegalArgumentException("无法获取 orderItemId = " + orderItemId + " 的对象值!");
	}

	private void addLossAmountDetail(List<LossAmountDetail> lossAmountDetailList, OrdRefundItemInfo subOrderRefundApplication, OrdOrderItem currentSubOrder, OrdRefundmentItemSplit subOrderRefundInfo) {
		LossAmountDetail refundmentAndDeductionDetail = new LossAmountDetail();
		refundmentAndDeductionDetail.setOrderItemId(String.valueOf(subOrderRefundApplication.getOrderItemId()));
		refundmentAndDeductionDetail.setActualAmount(subOrderRefundInfo.getRefundOughtPrice());
		refundmentAndDeductionDetail.setDeductedAmount(subOrderRefundInfo.getInitActualLoss());
		refundmentAndDeductionDetail.setRefundmentAmount(subOrderRefundInfo.getInitRefundedPrice());
		refundmentAndDeductionDetail.setRefundQuality(String.valueOf(subOrderRefundApplication.getRefundQuantity()));
		refundmentAndDeductionDetail.setRefundmentRules(subOrderRefundInfo.getActualRefundRule());
		refundmentAndDeductionDetail.setMatchedRefundmentRuleSnapshot(subOrderRefundInfo.getRefundRuleSnapshot());
		refundmentAndDeductionDetail.setSuppGoodsName(currentSubOrder.getSuppGoodsName());
		refundmentAndDeductionDetail.setPrice(currentSubOrder.getPriceYuan());
		refundmentAndDeductionDetail.setLossExplain(assembleDeductExplaination(subOrderRefundInfo.getRefundRuleSnapshot(), subOrderRefundInfo.getInitActualLoss()));
		lossAmountDetailList.add(refundmentAndDeductionDetail);
	}

	private void setFormula(boolean isFront, List<String> refundFormulaDetails, StringBuilder refundFormulas,
			StringBuilder refundFormulaNumber, Long currentRefundAmount, PartRefundAmountInfo partRefundAmountInfo) {
		if (!isFront) {
			// 各类型空一行
			refundFormulaDetails.add(" ");
			refundFormulaDetails.add(refundFormulas.toString());
			refundFormulaDetails.add(" ");
			refundFormulaDetails.add(refundFormulaNumber.toString());
			refundFormulaDetails.add(" ");
			refundFormulaDetails.add("退款金额=" + PriceUtil.trans2YuanStr(currentRefundAmount) + "元");
			partRefundAmountInfo.setRefundFormulaDetails(refundFormulaDetails);
			partRefundAmountInfo.setRefundFormulas(refundFormulas.toString());
		}
	}
	
	private String assembleDeductExplaination(String refundRuleSnapshot, Long deductAmount) {
		String deductExplaination = "";
		if (refundRuleSnapshot != null && !refundRuleSnapshot.equals("")) {
			SuppGoodsRefundVO goodInfo = JSON.parseObject(refundRuleSnapshot, SuppGoodsRefundVO.class);
			if (goodInfo != null) {
				if (goodInfo.getIsCurrent() != null && goodInfo.getIsCurrent()) {
					if (goodInfo.getCancelTimeType().equals(SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode())) {
						deductExplaination = "订单退款,扣除" + PriceUtil.trans2YuanStr(deductAmount) + "元";
					} else {
						Date refundDate = goodInfo.getRefundDate();
						deductExplaination = DateUtil.formatDate(refundDate, "yyyy年MM月dd日 HH:mm") + "前退款,共扣除"
								+ PriceUtil.trans2YuanStr(deductAmount) + "元";
					}
				}
			}
		}
		return deductExplaination;
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	protected void insertOrderLog( OrdOrder order ,String type,OrdRefundInfo ordRefundInfo,String memo,String cancelCode,String reason){
		try{
			if (order != null) {
				String zhOrderStatus = OrderEnum.ORDER_STATUS.getCnName(type);
				Long orderId=order.getOrderId();
			    //拼接日志内容
				String cancelStr="   取消类型："+ OrderEnum.ORDER_CANCEL_CODE.getCnName(cancelCode) +",取消原因："+reason;
				String content="将编号为["+orderId+"]的订单活动中["+ordRefundInfo.getReqRefundQuantity()+"份]变更为["+ zhOrderStatus +"]"+cancelStr;
				if (order.isSupplierOrder()) {
					content+="。此订单为供应商订单，自动发送消息给供应商，等待供应商确认后才可会真正取消订单";
				}
				
				comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
						orderId, 
						orderId, 
						ordRefundInfo.getOperatorName(), 
						content, 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PART_CANCEL.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PART_CANCEL.getCnName()+"["+ zhOrderStatus +"]",
						memo);
			}
		}catch(Exception e){
			LOG.error("PartRefundServiceImpl#insertOrderLog,orderId="+order.getOrderId(),e);
		}
	}
}
