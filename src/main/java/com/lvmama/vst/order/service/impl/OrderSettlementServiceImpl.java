package com.lvmama.vst.order.service.impl;


import com.alibaba.fastjson.JSON;
import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.finance.po.SettlementPriceChange;
import com.lvmama.finance.comm.finance.service.SetSettlementItemService;
import com.lvmama.finance.comm.finance.service.SettlementPriceChangeService;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.supp.service.SuppContractClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.OrderSettlementService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenlong on 2016/10/17.
 * 订单结算推送, 调用finance_interface
 */
@Service("orderSettlementService")
public class OrderSettlementServiceImpl implements OrderSettlementService {

    private static final Logger logger = LoggerFactory.getLogger(OrderSettlementServiceImpl.class);

    private final String BIZ_TYPE="NEW_SUPPLIER_BUSINESS";

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SetSettlementItemService setSettlementItemService;

    @Autowired
    private SettlementPriceChangeService settlementPriceChangeService;
    
    @Autowired
	private ApportionInfoQueryService apportionInfoQueryService;
    
    @Autowired
    ApiSuppOrderService apiSuppOrderService;
    
	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterService;
	
    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
    
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SuppContractClientService  suppContractClientService;

    @Override
    public void insertOrUpdateSettlementItem(List<SettlementItem> setSettlementItems, Constant.EVENT_TYPE messageType) {
        List<SetSettlementItem> setSettlementItemList = new ArrayList<SetSettlementItem>();
        for (SettlementItem settlementItem : setSettlementItems) {
            SetSettlementItem setSettlementItem = new SetSettlementItem();
            logger.info("vst_order-手机号" + settlementItem.getContactMobileNo());
            BeanUtils.copyProperties(settlementItem, setSettlementItem);
            setSettlementItemList.add(setSettlementItem);
            logger.info("actprice:{},total:{}======================", new Object[]{settlementItem.getActualSettlementPrice(), settlementItem.getTotalSettlementPrice()});
            logger.info("actprice:{},total:{}======================", new Object[]{setSettlementItem.getActualSettlementPrice(), setSettlementItem.getTotalSettlementPrice()});

            try {
                logger.info("vst_order----insertOrUpdateSettlementItem订单结算子项详情----" + setSettlementItem.toString());
                //logger.info("----订单结算子项详情----"+org.apache.commons.beanutils.BeanUtils.describe(setSettlementItem));
            } catch (Exception e) {
                logger.error(ExceptionFormatUtil.getTrace(e));
            }
        }
        com.lvmama.comm.vo.Constant.EVENT_TYPE t = null;
        switch (messageType) {
            case ORDER_REFUNDED_MSG:
                t = com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_REFUNDED;
                break;
            case ORDER_PAYMENT_MSG:
                t = com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_PAYMENT;
                break;
            case ORDER_CANCEL_MSG:
                t = com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_CANCEL;
                break;
            case ORDER_MODIFY_SETTLEMENT_PRICE_MSG:
                t = com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_MODIFY_SETTLEMENT_PRICE;
                break;
            case ORDER_ITEM_SETTLE_MSG:
                t = com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_ITEM_META_SETTLE;
                break;
            default:
                t=null;
                break;
        }
        if(t==null){
            throw new IllegalArgumentException("消息不符合");
        }
        for(SetSettlementItem setset :setSettlementItemList){
            logger.info("vst_order--businessName======================"+setset.getBusinessName());
            try {
                // 如果 子单实收金额为空,则重新补偿获取分摊相关信息
                if(null==setset.getOrderItemActualReceived()){
                    compensateFinanceSettlementData(setset);
                }
                logger.info("vst_order---insertOrUpdateSettlementItem-订单结算子项详情----"+setset.toString());
            } catch (Exception e) {
                logger.error(ExceptionFormatUtil.getTrace(e));
            }

            try {
                logger.info("vst_order----订单结算子项详情----"+setset.toString());
            } catch (Exception e) {
                logger.error(ExceptionFormatUtil.getTrace(e));
            }
        }
        logger.info(" [ multiple settlement log 1 ] setSettlementItemList data is "+ JSON.toJSONString(setSettlementItemList));        
        settlementService.insertOrUpdateSettlementItem(setSettlementItemList, t);
    }



    @Override
    public int updateSettlementItem(final Long orderId,final Long countSettleAmount){
        try {
            logger.info("vst_order---updateSettlementItem----orderId:"+orderId+"--countSettleAmount:"+countSettleAmount);
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
        return setSettlementItemService.updateSettlementItem(orderId, countSettleAmount);
    }

    @Override
    public List<SetSettlementItem> findSetSettlementItemByParams(Long orderId, Long orderItemId) {
        List<SetSettlementItem> list = settlementService.findSetSettlementItemByParams(orderId, orderItemId, BIZ_TYPE);
        List<SetSettlementItem> setSettlementItemList=new ArrayList<SetSettlementItem>();
        if(list!=null && list.size()>0){
            for(SetSettlementItem setSettlementItem : list){
                setSettlementItemList.add(setSettlementItem);
            }
        }
        return setSettlementItemList;
    }

    @Override
    public void saveOrUpdateSetSettlementItem(List<SetSettlementItem> setSettlementItems) {
        try {
            logger.info("vst_order----saveSettlementItem订单结算子项详情----" + GsonUtils.toJson(setSettlementItems));
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
        logger.info(" [ multiple settlement log 2 ] setSettlementItems data is "+ JSON.toJSONString(setSettlementItems));
        settlementService.saveOrUpdateSetSettlementItem(setSettlementItems);
    }

    @Override
    public void saveSettlementItem(SettlementItem settlementItem) {
        SetSettlementItem setSettlementItem=new com.lvmama.finance.comm.finance.po.SetSettlementItem();
        logger.info("***vst_order-手机号"+ settlementItem.getContactMobileNo());
        BeanUtils.copyProperties(settlementItem, setSettlementItem);
        try {
            logger.info("vst_order----saveSettlementItem订单结算子项详情----" + setSettlementItem.toString() );
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
        logger.info(" [ multiple settlement log 3 ] setSettlementItem data is "+ JSON.toJSONString(setSettlementItem));
        settlementService.saveSettlementItem(setSettlementItem, BIZ_TYPE);
    }

    @Override
    public boolean searchSettlementPayByOrderItemMetaId(Long orderItemMetaId) {
        return settlementService.searchSettlementPayByOrderItemMetaId(orderItemMetaId, BIZ_TYPE);
    }

    @Override
    public void batchInsertSettlementItem(List<SettlementItem> setSettlementItems) {
        List<SetSettlementItem> list = new ArrayList<SetSettlementItem>();
        for (SettlementItem settlementItem : setSettlementItems) {
            try {
                if(settlementItem!=null){
                    SetSettlementItem s= new SetSettlementItem();
                    BeanUtils.copyProperties(settlementItem,s);
                    list.add(s);
                }
                logger.info("vst_order----batchInsertSettlementItem订单结算子项详情----"+ GsonUtils.toJson(settlementItem));
            } catch (Exception e) {
                logger.error(ExceptionFormatUtil.getTrace(e));
            }
        }
        logger.info(" [ multiple settlement log 4 ] list data is "+ JSON.toJSONString(list));
        settlementService.batchInsertSettlementItem(list, BIZ_TYPE);
    }

    @Override
    public void insertRecord(OrdSettlementPriceRecord priceRecord) {
        SettlementPriceChange settlementPriceChange = new SettlementPriceChange();
        settlementPriceChange.setOrdRecordId(priceRecord.getRecordId());
        settlementPriceChange.setApproveRemark(priceRecord.getApproveRemark());
        settlementPriceChange.setChangeRemark(priceRecord.getChangeRemark());
        settlementPriceChange.setChangeResult(priceRecord.getChangeResult());
        settlementPriceChange.setChangeType(priceRecord.getChangeType());
        settlementPriceChange.setCreateTime(new Date());
        settlementPriceChange.setIsApprove(priceRecord.getIsApprove());
        settlementPriceChange.setNewActualSettlementPrice(priceRecord.getNewActualSettlementPrice());
        settlementPriceChange.setNewTotalSettlementPrice(priceRecord.getNewTotalSettlementPrice());
        settlementPriceChange.setOldActualSettlementPrice(priceRecord.getOldActualSettlementPrice());
        settlementPriceChange.setOldTotalSettlementPrice(priceRecord.getOldTotalSettlementPrice());


        settlementPriceChange.setNewBudgetTotalSettlementPrice(priceRecord.getNewBudgetTotalSettlementPrice());
        settlementPriceChange.setOldBudgetTotalSettlementPrice(priceRecord.getOldBudgetTotalSettlementPrice());
        settlementPriceChange.setNewBudgetUnitSettlementPrice(priceRecord.getNewBudgetUnitSettlementPrice());
        settlementPriceChange.setOldBudgetUnitSettlementPrice(priceRecord.getOldBudgetUnitSettlementPrice());
        settlementPriceChange.setChangeFlag(priceRecord.getChangeFlag());

        settlementPriceChange.setOperator(priceRecord.getOperator());
        settlementPriceChange.setOperatorApprove(priceRecord.getOperatorApprove());
        settlementPriceChange.setOrderId(priceRecord.getOrderId());
        settlementPriceChange.setOrderItemId(priceRecord.getOrderItemId());
        settlementPriceChange.setPriceType(priceRecord.getPriceType());
        settlementPriceChange.setReason(priceRecord.getReason());
        settlementPriceChange.setRemark(priceRecord.getRemark());
        settlementPriceChange.setSuppGoodsId(priceRecord.getSuppGoodsId());
        settlementPriceChange.setSupplierId(priceRecord.getSupplierId());
        settlementPriceChange.setStatus(priceRecord.getStatus());
        settlementPriceChange.setVisitTime(priceRecord.getVisitTime());
        logger.info("vst_order=invoke settlementPriceChangeService.insertRecord order_item_id=" + settlementPriceChange.getOrderItemId());
        settlementPriceChangeService.insertRecord(settlementPriceChange);
    }
    @Override
    public int dynmicUpdateSetSettlementItem(Map<String, Object> params) {
        Integer result= 0;
        try {
        	//certi_email 分支报错，暂时注释
//            result = settlementService.dynamicUpdateSetSettlementItem(params);
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
        return result;
    }

    /**
     * 补偿填充分摊信息
     * @param item
     */
    public void compensateFinanceSettlementData(SetSettlementItem item){
        OrdOrderItem orderItem = orderService.getOrderItem(item.getOrderItemMetaId());
        logger.info(" ********* CompensateFinanceSettlementData start,orderItemId:"+orderItem.getOrderItemId()+" ************* ");
        try{
            item.setApportionMessage(false); // 表示非分摊
            item.setPureApportion(false);    // 表示非纯分摊
            if(StringUtils.isEmpty(item.getContractCode())){
                ResultHandleT<SuppGoods> resultSuppGoods = suppGoodsHotelAdapterService.findSuppGoodsById(orderItem.getSuppGoodsId());
                if(resultSuppGoods.isSuccess() && null != resultSuppGoods.getReturnContent()){
                    Long contractId =resultSuppGoods.getReturnContent().getContractId();
                    ResultHandleT<SuppContract> suppContractHandle = suppContractClientService.findSuppContractByContractId(contractId);
                    if(null != suppContractHandle && null != suppContractHandle.getReturnContent()){
                        //合同编号
                        item.setContractCode(suppContractHandle.getReturnContent().getContractNo());
                    }
                }else{
                    throw new BusinessException(" [CompensateFinanceSettlementData] Can not find suppgoods by id("+orderItem.getSuppGoodsId()+")");
                }
            }

            item.setOrderIsTermBill((byte) 0);
            Map<String, Object> contentMap = orderItem.getContentMap();
            if(null != contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())){
                //是否期票
                String aperiodicFlag =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name());
                if(StringUtils.isNotEmpty(aperiodicFlag) && "Y".equalsIgnoreCase(aperiodicFlag)){
                    item.setOrderIsTermBill((byte) 1);
                }
            }

            //酒店取间夜数
            if(null == item.getOrderItemRoomNight() && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
                Map<String, Object> params = new HashMap<>();
                params.put("orderItemId", orderItem.getOrderItemId());
                List<OrdOrderHotelTimeRate> hotelTimeRates = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
                if(CollectionUtils.isNotEmpty(hotelTimeRates)){
                    int hotelTimeRatesSize = hotelTimeRates.size();
                    if(null!=orderItem.getQuantity()){
                        hotelTimeRatesSize = hotelTimeRatesSize*Integer.parseInt(String.valueOf(orderItem.getQuantity()));
                    }
                    item.setOrderItemRoomNight(hotelTimeRatesSize);
                }else{
                    logger.info("[CompensateFinanceSettlementData] Find OrdOrderHotelTimeRate list by(orderItemId"+orderItem.getOrderItemId()+") size is 0!");
                }
            }


            //调分摊接口
            if(null == item.getOrderItemActualReceived()){ // 重新填充分摊信息
                OrderItemApportionInfoQueryVO orderApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
                orderApportionInfoQueryVO.setOrderId(orderItem.getOrderId());
                orderApportionInfoQueryVO.setOrderItemId(orderItem.getOrderItemId());
                long startTime = System.currentTimeMillis();
                logger.info(" Calc orderItem apportion info start");
                OrderItemApportionInfoPO orderItemApportion = apportionInfoQueryService.calcOrderItemApportionInfo(orderApportionInfoQueryVO);
                long endTime = System.currentTimeMillis();
                logger.info(" CalcOrderItemApportionInfo method cast "+(endTime-startTime)+" ms");
                //子单优惠总额
                item.setCouponAmount(orderItemApportion.getItemTotalCouponAmount());
                //子单促销金额
                item.setPromotionAmount(orderItemApportion.getItemTotalPromotionAmount());
                //子单手动改价金额
                item.setManualChangeAmount(orderItemApportion.getItemTotalManualChangeAmount());
                //子单实收金额
                item.setOrderItemActualReceived(orderItemApportion.getItemTotalActualPaidAmount());
                //子单退款总额
                item.setItemRefundedAmount(orderItemApportion.getTotalRefundAmount());
                //子单销售金额
                item.setOrdeItemSaleAmount(orderItem.getTotalAmount());
            }

            if(StringUtils.isEmpty(item.getSupplierOrderCode())){
                logger.info(" [CompensateFinanceSettlementData] ApiSuppOrderService getSuppOrderByOrderItemId(orderItemId:"+orderItem.getOrderItemId()+") start");
                RequestSuppOrder requestSuppOrder = apiSuppOrderService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
                if(null != requestSuppOrder && StringUtils.isNotEmpty(requestSuppOrder.getSuppOrderId())){
                    item.setSupplierOrderCode(requestSuppOrder.getSuppOrderId());  //供应商订单号
                }else if(null != requestSuppOrder &&  StringUtils.isEmpty(requestSuppOrder.getSuppOrderId()) ){
                    logger.info("[CompensateFinanceSettlementData] SuppOrder is not null, but suppOrderId is null!(orderItemId:"+orderItem.getOrderItemId()+") ");
                }else{
                    logger.info("[CompensateFinanceSettlementData] SuppOrder is null!(orderItemId:"+orderItem.getOrderItemId()+") ");
                }
            }

        }catch (Exception e){
            logger.error(" CompensateFinanceSettlementData method has exception, error msg is "+e.getMessage());
        }

        logger.info(" ********* CompensateFinanceSettlementData end ,orderItemId:"+orderItem.getOrderItemId()+" ************* ");
    }

    public void financeSettlementDataByOrderItem(SetSettlementItem settlementItem,OrdOrderItem ordOrderItem){
    	 logger.info("financeSettlementDataByOrderItem start orderId="+settlementItem.getOrderId());
    	settlementItem.setApportionMessage(true);
    	settlementItem.setPureApportion(false);
		ResultHandleT<SuppGoods> resultSuppGoods = suppGoodsHotelAdapterService.findSuppGoodsById(ordOrderItem.getSuppGoodsId());				
		if(null != resultSuppGoods && null != resultSuppGoods.getReturnContent()){
			SuppGoods suppGoods = resultSuppGoods.getReturnContent();
			Long contractId =suppGoods.getContractId();
			ResultHandleT<SuppContract> suppContractHandle = suppContractClientService.findSuppContractByContractId(contractId);
			if(null != suppContractHandle && null != suppContractHandle.getReturnContent()){
				SuppContract suppContract = suppContractHandle.getReturnContent();
				//合同编号
				settlementItem.setContractCode(suppContract.getContractNo());
			}
		}
		
		settlementItem.setOrderIsTermBill((byte) 0);
		Map<String, Object> contentMap = ordOrderItem.getContentMap();
		if(null != contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())){
			//是否期票
			String aperiodicFlag =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name());
			if(StringUtils.isNotEmpty(aperiodicFlag) && "Y".equalsIgnoreCase(aperiodicFlag)){
				settlementItem.setOrderIsTermBill((byte) 1);
			}
		}
		//酒店取间夜数
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderItemId", ordOrderItem.getOrderItemId());
			List<OrdOrderHotelTimeRate> listHotelRate = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
			if(CollectionUtils.isNotEmpty(listHotelRate)){
				settlementItem.setOrderItemRoomNight(listHotelRate.size());
			}					
		}	
		//调分摊接口
		OrderItemApportionInfoQueryVO orderApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
		orderApportionInfoQueryVO.setOrderId(ordOrderItem.getOrderId());
		orderApportionInfoQueryVO.setOrderItemId(ordOrderItem.getOrderItemId());
		OrderItemApportionInfoPO orderItemApportion = apportionInfoQueryService.calcOrderItemApportionInfo(orderApportionInfoQueryVO);
		//子单优惠总额
		settlementItem.setCouponAmount(orderItemApportion.getItemTotalCouponAmount());
		//子单促销金额
		settlementItem.setPromotionAmount(orderItemApportion.getItemTotalPromotionAmount());
		//子单手动改价金额
		settlementItem.setManualChangeAmount(orderItemApportion.getItemTotalManualChangeAmount());
		//子单实收金额
		settlementItem.setOrderItemActualReceived(orderItemApportion.getItemTotalActualPaidAmount());
		//子单退款总额
		settlementItem.setItemRefundedAmount(orderItemApportion.getTotalRefundAmount());
		//子单销售金额
		settlementItem.setOrdeItemSaleAmount(ordOrderItem.getTotalAmount());
		
		RequestSuppOrder requestSuppOrder = apiSuppOrderService.getSuppOrderByOrderItemId(ordOrderItem.getOrderItemId());
		if(null != requestSuppOrder){
			//供应商订单号
			settlementItem.setSupplierOrderCode(requestSuppOrder.getSuppOrderId());
		}
		 logger.info("financeSettlementDataByOrderItem end orderId="+settlementItem.getOrderId());
    }
}
