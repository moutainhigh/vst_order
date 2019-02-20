package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdSettlementPriceRecordService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.IRemoteOrderSettlementService;
import com.lvmama.vst.order.service.OrderSettlementService;

@Component("remoteOrderSettlementService")
public class RemoteOrderSettlementServiceImpl implements IRemoteOrderSettlementService {

	private static final Log LOG = LogFactory.getLog(RemoteOrderSettlementServiceImpl.class);

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrdSettlementPriceRecordService ordSettlementPriceRecordService;
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private OrderSettlementService orderSettlementService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private ResPreControlService resControlBudgetRemote;

	@Override
	public ResultHandle updateTotalSettlementChange(Long orderItemId,
			String totalSettlementPrice, String oldTotalSettlementPrice, String userId) {
		ResultHandle result = new ResultHandle();
		LOG.info("RemoteOrderSettlementServiceImpl.updateTotalSettlementChange.orderItemId=" + orderItemId + ", totalSettlementPrice=" + totalSettlementPrice + ", userId=" + userId);
		try {
		// 查询子订单
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if(null!=orderItem){
			if(StringUtil.isNotEmptyString(orderItem.getBuyoutFlag())&&orderItem.getBuyoutFlag().equals("Y")){
				//买断订单修改结算价
				result=modifyPurchaseOrderSettlePrice(orderItemId,totalSettlementPrice,oldTotalSettlementPrice,userId);
			}else{
				//非买断订单修改结算价
				result=modifyUnPurchaseOrderSettlePrice(orderItemId,totalSettlementPrice,oldTotalSettlementPrice,userId);
			}
		}
		 return result;
		}catch(Exception e){
			LOG.error(ExceptionUtil.getExceptionDetails(e));
		}
		result.setMsg("系统内部异常");
		return result;
	}
	
	/****
	 * 修改非买断订单结算价
	 * @param orderItemId
	 * @param totalSettlementPrice
	 * @param oldTotalSettlementPrice
	 * @param userId
	 */
	private ResultHandle modifyUnPurchaseOrderSettlePrice(Long orderItemId, String totalSettlementPrice,
			String oldTotalSettlementPrice, String userId) {
		ResultHandle result = new ResultHandle();
		boolean isAmount=false;
		OrdOrderItem  orderItem  = null;
		List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
		StringBuffer logContent = new StringBuffer();
		
		
		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		paramsMulPriceRate.put("orderItemId", orderItemId); 

		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
				//非买断结算总价
				//ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
				//ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

		paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
		List<OrdMulPriceRate> ordMulPriceRateListTemp = ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
		LOG.info("RemoteOrderSettlementServiceImpl.updateTotalSettlementChange.ordMulPriceRateListCount=" + ordMulPriceRateListTemp.size());
		Long ordMulPriceRateListCount = Long.parseLong(String.valueOf(ordMulPriceRateListTemp.size()));

		if (!StringUtils.isEmpty(totalSettlementPrice) && StringUtil.isNumberAmount(totalSettlementPrice)) {
			isAmount = true;
		} else {
			isAmount = false;
		}

		if (!isAmount) {
			result.setMsg("传入的结算价有误");
			return result;
		}

		if (StringUtils.isEmpty(oldTotalSettlementPrice) || !StringUtil.isNumberAmount(oldTotalSettlementPrice)) {
			result.setMsg("传入的原结算价有误");
			return result;
		}

		// 查询子订单
		orderItem = orderUpdateService.getOrderItem(orderItemId);
		if (null == orderItem) {
			result.setMsg("子订单不存在");
			return result;
		}

		LOG.info("RemoteOrderSettlementServiceImpl.updateTotalSettlementChange.oldTotalSettlementPrice=" + oldTotalSettlementPrice + ", orderItem.totalSettlementPrice=" + orderItem.getTotalSettlementPrice());
		// 判断结算总价是否和传入进来的原结算总价一致
		if (PriceUtil.convertToFen(oldTotalSettlementPrice) != orderItem.getTotalSettlementPrice()) {
			result.setMsg("传入进来的原结算总价和数据库的结算总价不一致");
			return result;
		}

		Long orderId = orderItem.getOrderId();

		Long buyoutTotalPrice = 0L;
		Long buyNum = orderItem.getBuyoutQuantity();
		buyNum = buyNum==null?0L:buyNum;
		if("Y".equals(orderItem.getBuyoutFlag())){
			buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
			orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);

			Long buyoutQuantity = orderItem.getBuyoutQuantity();
			buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
			Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
			notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));

		}

		OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
		newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
		newOrdSettlementPriceRecord.setOperator(userId);
		newOrdSettlementPriceRecord.setCreateTime(new Date());
		newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
		newOrdSettlementPriceRecord.setOrderId(orderId);
		newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
		newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
		newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
		newOrdSettlementPriceRecord.setIsApprove("Y");
		newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
		//newOrdSettlementPriceRecord.setReason(reason);
		//newOrdSettlementPriceRecord.setRemark(remark);
		newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

		// 修改之前的结算单价
		newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
		// 修改之前的结算总价
		newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());

		//计算后的单价格
		OrdOrderItem item = ordSettlementPriceRecordService.resetOrderItem4Settlement(orderItem, PriceUtil.convertToFen(totalSettlementPrice));

		// 修改之后的结算单价
		newOrdSettlementPriceRecord.setNewActualSettlementPrice(item.getActualSettlementPrice());

		// 修改之后的结算总价
		newOrdSettlementPriceRecord.setNewTotalSettlementPrice(item.getTotalSettlementPrice());
		// 修改价格类型
		newOrdSettlementPriceRecord.setPriceType("PRICE");

		newOrdSettlementPriceRecord.setOperator(userId);
//		newOrdSettlementPriceRecord.setOperatorApprove("system");
		//newOrdSettlementPriceRecord.setUpdateTime(new Date());

		list.add(newOrdSettlementPriceRecord);

		logContent.append("原结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0)
				.append("新结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);

		List<OrdMulPriceRate> ordMulPriceRateList = null;
		if (ordMulPriceRateListCount >= 1) {
			list.clear();
			//计算后的多价格
			ordMulPriceRateList = ordSettlementPriceRecordService.calcSettlementUnitPrice(orderItemId, PriceUtil.convertToFen(totalSettlementPrice));
			if (ordMulPriceRateList != null && ordMulPriceRateList.size() > 0) {
				for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
					//修改总价的时候记录单价的变更记录，目的是为了取得原始的价格记录
					OrdSettlementPriceRecord ordSettlementPriceRecordObj = new OrdSettlementPriceRecord();
					BeanUtils.copyProperties(newOrdSettlementPriceRecord, ordSettlementPriceRecordObj);
					// 修改之前的结算单价
					ordSettlementPriceRecordObj.setOldActualSettlementPrice(ordMulPriceRate.getOrigPrice());
					// 修改之前的结算总价
					ordSettlementPriceRecordObj.setOldTotalSettlementPrice(ordMulPriceRate.getOrigPrice() * ordMulPriceRate.getQuantity());

					// 修改之后的结算单价,多价格的时候设置修改后的结算单价为null，以此为依据判断历史记录中是否显示
				    //ordSettlementPriceRecordObj.setNewActualSettlementPrice(null);
					ordSettlementPriceRecordObj.setPriceType(ordMulPriceRate.getPriceType());
					
					// 修改之后的结算总价
					//newTotalSettlementPrice = ordSettlementPriceRecordObj.getNewActualSettlementPrice()* ordMulPriceRate.getQuantity();
				    //ordSettlementPriceRecordObj.setNewTotalSettlementPrice(newTotalSettlementPrice);

					list.add(ordSettlementPriceRecordObj);
				}
			}
		}

		//处理结算总价的记录
		String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
		OrdSettlementPriceRecord record = list.get(0);
		if ((record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
			changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
		}
		record.setChangeResult(changeResult);
		//boolean needApprove = isNeedApprove(orderItem, record);
		// 订单结算状态校验
		ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
		//保存结算记录和价格变更记录
		this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderId);

		//修改子订单
		//如果含买断的，要将买断的加上去
		if("Y".equals(orderItem.getBuyoutFlag())){
			orderItem.setTotalSettlementPrice(PriceUtil.convertToFen(totalSettlementPrice) + buyoutTotalPrice);
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
		}
		LOG.info("RemoteOrderSettlementServiceImpl.updateTotalSettlementChange.orderItem=" + JSON.toJSONString(orderItem));
		ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
		if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单
			//修改多价格记录
			ordSettlementPriceRecordService.updateMulPriceRates(ordMulPriceRateList);
			if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
				record.setChangeRemark("0");//结算前修改
				//推送结算
				sendOrdSettlementPriceChangeMsg(record.getOrderItemId(), userId);

			} else {
				record.setChangeRemark("1");//结算后修改
			}
			
			//调用结算系统生成结算单
			record.setChangeFlag("Normal");
			orderSettlementService.insertRecord(record);
		}
		try {
			updateOrdItemPriceConfirm(orderItemId, userId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}

		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				userId, 
				"将编号为["+orderId+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算价价格修改申请","");

		String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

		if (resultMessage.isSuccess()) {
			info = "结算价修改成功";
		} else {
			info = "该订单已经结算过！";
		}

		return result;

	
		
	}

	/******
	 * 修改买断订单结算价
	 * @param orderItemId
	 * @param totalSettlementPrice
	 * @param oldTotalSettlementPrice
	 * @param userId
	 */
	private ResultHandle modifyPurchaseOrderSettlePrice(Long orderItemId, String totalSettlementPrice,
			String oldTotalSettlementPrice, String userId) {
		ResultHandle result = new ResultHandle();
    	String change_flag = "BUYOUT";
        boolean isAmount=false;
        OrdOrderItem  orderItem  = null;
        List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
        StringBuffer logContent=new StringBuffer();

        Long oldBuyoutTotalPrice = 0L;
        Long newBuyoutTotalPrice = 0L;
        
        if (!StringUtils.isEmpty(totalSettlementPrice) && StringUtil.isNumberAmount(totalSettlementPrice)) {
            isAmount = true;
        } else {
            isAmount = false;
        }

        if (!isAmount) {
        	result.setMsg("请至少填写一个为正数(或者2位小数)金额");
            return result;
        }
        if (StringUtils.isEmpty(oldTotalSettlementPrice) || !StringUtil.isNumberAmount(oldTotalSettlementPrice)) {
			result.setMsg("传入的原结算价有误");
			return result;
		}

        // 查询子订单
        orderItem = this.orderUpdateService.getOrderItem(orderItemId);
        
        LOG.info("RemoteOrderSettlementServiceImpl.updateTotalSettlementChange.oldTotalSettlementPrice=" + oldTotalSettlementPrice + ", orderItem.totalSettlementPrice=" + orderItem.getTotalSettlementPrice());
		// 判断结算总价是否和传入进来的原结算总价一致
		if (PriceUtil.convertToFen(oldTotalSettlementPrice) != orderItem.getTotalSettlementPrice()) {
			result.setMsg("传入进来的原结算总价和数据库的结算总价不一致");
			return result;
		}
		
        oldBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
        oldBuyoutTotalPrice = oldBuyoutTotalPrice==null?0L:oldBuyoutTotalPrice;
        //得到新订单新的结算价
        OrdSettlementPriceRecord newOrdSettlementPriceRecord = getNewOrdSettlementPriceRecord(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode(),orderItem,"","",PriceUtil.convertToFen(Float.parseFloat(totalSettlementPrice)),userId);

        list.add(newOrdSettlementPriceRecord);

        if(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() != null){
            logContent.append("原非买断结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0);
        }
        if(newOrdSettlementPriceRecord.getOldActualSettlementPrice() != null){
            logContent.append("原非买断结算单价：").append(newOrdSettlementPriceRecord.getOldActualSettlementPrice()/100.0);
        }
        if(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice() != null){
            logContent.append("原买断结算总价：").append(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice()/100.0);
        }
        if(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice() != null){
            logContent.append("原买断结算单价：").append(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice()/100.0);
        }
        if(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() != null){
            logContent.append("新非买断结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
        }
        if(newOrdSettlementPriceRecord.getNewActualSettlementPrice() != null){
            logContent.append("新非买断结算单价：").append(newOrdSettlementPriceRecord.getNewActualSettlementPrice()/100.0);
        }
        if(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice() != null){
            logContent.append("新买断结算总价：").append(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice()/100.0);
        }
        if(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice() != null){
            logContent.append("新买断结算单价：").append(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice()/100.0);
        }


        //处理结算总价的记录
        String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
        OrdSettlementPriceRecord record = list.get(0);
        Long newBudgetTotalSettlementPrice = record.getNewBudgetTotalSettlementPrice()!=null?record.getNewBudgetTotalSettlementPrice():0;
        Long newTotalSettlementPrice = record.getNewTotalSettlementPrice()!=null?record.getNewTotalSettlementPrice():0;
        
        Long oldTotalSettlePrice = record.getOldTotalSettlementPrice()!=null?record.getOldTotalSettlementPrice():0;
        Long oldBudgetTotalSettlementPrice = record.getOldBudgetTotalSettlementPrice()!=null?record.getOldBudgetTotalSettlementPrice():0;
        
        if ((newBudgetTotalSettlementPrice+newTotalSettlementPrice - oldTotalSettlePrice-oldBudgetTotalSettlementPrice) < 0) {
            changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
        }
        record.setChangeResult(changeResult);
        //订单结算状态校验
        ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
        //保存结算记录和价格变更记录
        this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderItem.getOrderId());

        //修改子订单
        ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
        if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单
            //修改多价格记录
            if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
            	
            	newBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
                newBuyoutTotalPrice = newBuyoutTotalPrice==null?0L:newBuyoutTotalPrice;
                if(newBuyoutTotalPrice.longValue() != oldBuyoutTotalPrice.longValue()){
                	Date visitDate = orderItem.getVisitTime();
                	Long goodsId = orderItem.getSuppGoodsId();
                	GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
                	long p = Math.min(oldBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount()) - newBuyoutTotalPrice;
                	if(goodsResPrecontrolPolicyVO!=null && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
                		Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
                		Long controlId = goodsResPrecontrolPolicyVO.getId();
                		Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
                		LOG.info(goodsId+"差价" + p);
                		LOG.info(goodsId+"剩余金额" + leftAmount);
                		Long leftValue = leftAmount+p;
                		leftValue = leftValue< 0? 0L:leftValue;
                		leftValue = leftValue> goodsResPrecontrolPolicyVO.getAmount()? goodsResPrecontrolPolicyVO.getAmount():leftValue;
                		resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
                		if(leftValue == 0L){
                			resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO, orderItem.getVisitTime(), goodsId);
                		}
                	}
                	
                }
                
                record.setChangeRemark("0");//结算前修改
                //推送结算
                sendOrdSettlementPriceChangeMsg(record.getOrderItemId(),userId);
            } else {
                record.setChangeRemark("1");//结算后修改
            }
            
            record.setChangeFlag(change_flag);
            
            //调用结算系统生成结算单
            orderSettlementService.insertRecord(record);
        }

        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                orderItem.getOrderId(),
                orderItemId,
                userId,
                "将编号为["+orderItem.getOrderId()+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算价价格修改申请","");

        String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

        if (resultMessage.isSuccess()) {
            info = "结算价修改成功";
        } else {
            info = "该订单已经结算过！";
        }
		return result;
	}

	private ResultMessage validateOrderSettlementStatus(OrdOrderItem orderItem) {
		String itemStatus=getSetSettlementItemStatus(orderItem.getOrderItemId());
		if(OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCode().equals(itemStatus)){
			return new ResultMessage(ResultMessage.ERROR,"该子订单已经处于已结算状态不可进行此操作");
		}
		return ResultMessage.CHECK_SUCCESS_RESULT;
	}

	//获取结算状态
	private String getSetSettlementItemStatus(Long itemId){
		try {
			List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
				List<Long> itemIds = new ArrayList<Long>();
				itemIds.add(itemId);
				setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
				if(null!=setSettlementItems&&setSettlementItems.size()>0){
					return setSettlementItems.get(0).getSettlementStatus();
				}
		} catch (Exception e) {
			throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
		}
		return  OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}

	private void sendOrdSettlementPriceChangeMsg(Long orderItemId, String userId) {
		/*List<OrdOrderItem> ordItemList=order.getOrderItemList();
		List<Long> itemIdList=new ArrayList<Long>();
		for (int i = 0; i < ordItemList.size(); i++) {
			OrdOrderItem orderItem=ordItemList.get(i);
			itemIdList.add(orderItem.getOrderItemId());
		}*/

//		String addition=new StringBuffer(StringUtils.join(itemIdList, ",")).append("|").append(this.getLoginUserId()).toString();
		String addition=new StringBuffer(orderItemId+"").append("|").append(userId).toString();
		orderLocalService.sendOrdSettlementPriceChangeMsg(orderItemId,addition);
	}

	/**
	 * 修改子订单价格确认状态
	 * @param  orderItemId
	 * @return void
	 */
	private  Integer updateOrdItemPriceConfirm(Long orderItemId, String userId){
		OrdOrderItem ordOrderItem=new OrdOrderItem();
		ordOrderItem.setOrderItemId(orderItemId);
		ordOrderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());

		int result=this.orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
		if(result>0){
			sendOrdItemPriceConfirmChangeMsg(orderItemId, userId);
		}
		return result;
	}

	private void sendOrdItemPriceConfirmChangeMsg(Long orderItemId, String userId){
		String addition=new StringBuffer(orderItemId+"").append("|").append(userId).toString();
		orderLocalService.sendOrdSettlementPriceChangeMsg(orderItemId,addition);
	}
	
	
	 /**
     * 得到新的结算价
     * @param priceModel
     * @param orderItem
     * @param reason
     * @param remark
     * @param settlementPrice
     * @return
     */
    private OrdSettlementPriceRecord getNewOrdSettlementPriceRecord(String priceModel,OrdOrderItem orderItem,String reason,String remark,Long settlementPrice,String userId){
        OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();

        
        newOrdSettlementPriceRecord.setChangeType(priceModel);
        newOrdSettlementPriceRecord.setOperator(userId);
        newOrdSettlementPriceRecord.setCreateTime(new Date());
        newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
        newOrdSettlementPriceRecord.setOrderId(orderItem.getOrderId());
        newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
        newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
        newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
        newOrdSettlementPriceRecord.setIsApprove("Y");
        newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
        newOrdSettlementPriceRecord.setReason(reason);
        newOrdSettlementPriceRecord.setRemark(remark);
        newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

        //修改之前买断单价
        newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
        //修改之前买断总价
        newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

        if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
            //修改之前的非买断总价
            newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
            //修改之前的非买断单价
            newOrdSettlementPriceRecord.setOldActualSettlementPrice(newOrdSettlementPriceRecord.getOldTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
        }

        //设置子订单结算价
        setCalcSettlementPrice(priceModel,settlementPrice,orderItem);

        //修改之后的买断单价
        newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
        //修改之后的买断总价
        newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

        if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
            //修改之后的非买断总价
            newOrdSettlementPriceRecord.setNewTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
            //修改之后的非买断单价
            newOrdSettlementPriceRecord.setNewActualSettlementPrice(newOrdSettlementPriceRecord.getNewTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
        }

        //修改价格类型
        newOrdSettlementPriceRecord.setPriceType("PRICE");

        newOrdSettlementPriceRecord.setOperator(userId);

        return newOrdSettlementPriceRecord;
    }
    
    //计算结算价
    private void setCalcSettlementPrice(String priceModel,Long settlementPrice,OrdOrderItem orderItem){
        if("BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel)){
            //修改买断单价
            //设置结算总价
            //先计算非买断的总价(总价-买断总价)
            Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
            LOG.info("修改单价非买断总价：" + unBudgetTotalPrice);
            //买断的总价
            Long budgetTotalPrice = settlementPrice*orderItem.getBuyoutQuantity();
            LOG.info("修改单价买断总价：" + budgetTotalPrice);
            //设置新的结算总价
            orderItem.setTotalSettlementPrice(unBudgetTotalPrice+budgetTotalPrice);
            //设置结算单价
            orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
            //设置买断结算单价
            orderItem.setBuyoutPrice(settlementPrice);
            //设置买断结算总价
            orderItem.setBuyoutTotalPrice(budgetTotalPrice);
        }else{
            //修改买断总价
            //设置新的结算总价
            //先计算非买断的总价(总价-买断总价)
            Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
            LOG.info("修改总价非买断总价：" + unBudgetTotalPrice);
            orderItem.setTotalSettlementPrice(unBudgetTotalPrice+settlementPrice);
            //设置结算单价
            orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
            //设置买断结算总价
            orderItem.setBuyoutTotalPrice(settlementPrice);
            LOG.info("修改总价买断总价：" + orderItem.getBuyoutTotalPrice());
            //设置买断结算单价
            orderItem.setBuyoutPrice(settlementPrice/orderItem.getBuyoutQuantity());
        }
    }
    
}
