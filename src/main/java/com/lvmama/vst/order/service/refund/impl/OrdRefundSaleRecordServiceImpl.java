package com.lvmama.vst.order.service.refund.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.order.dao.OrdRefundSaleRecordDao;
import com.lvmama.vst.order.service.refund.OrdRefundSaleRecordService;
import com.lvmama.vst.pet.adapter.refund.OrderRefundServiceAdapter;

/**
 * Created by lijuntao on 2017/1/16.
 */
@Service
public class OrdRefundSaleRecordServiceImpl implements OrdRefundSaleRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(OrdRefundSaleRecordServiceImpl.class);

    @Autowired
    private OrdRefundSaleRecordDao ordRefundSaleRecordDao;

    @Autowired
    private OrderRefundServiceAdapter orderRefundServiceAdapter;

    @Override
    public int insert(OrdRefundSaleRecord ordRefundSaleRecord) {
        return ordRefundSaleRecordDao.insert(ordRefundSaleRecord);
    }

    @Override
    public OrdRefundSaleRecord selectByPrimaryKey(Long ordRefundSaleRecordId) {
        return ordRefundSaleRecordDao.selectByPrimaryKey(ordRefundSaleRecordId);
    }

    @Override
    public int updateByPrimaryKeySelective(OrdRefundSaleRecord ordRefundSaleRecord) {
        return ordRefundSaleRecordDao.updateByPrimaryKeySelective(ordRefundSaleRecord);
    }

    @Override
    public int updateByOrderItemIdSelective(OrdRefundSaleRecord ordRefundSaleRecord) {
        return ordRefundSaleRecordDao.updateByOrderItemIdSelective(ordRefundSaleRecord);
    }

    @Override
    public List<OrdRefundSaleRecord> findOrdRefundSaleRecordList(Map<String, Object> params) {
        return ordRefundSaleRecordDao.findOrdRefundSaleRecordList(params);
    }

    @Override
    public void init(OrdOrder ordOrder, Date applyTime) {
        List<OrdRefundSaleRecord> saleRecordList = this.getOrdRefundSaleRecordByOrder(ordOrder, applyTime);

        Boolean ticketRefundStatus = false;
        try{
            String result = orderRefundServiceAdapter.getTicketRefundType(ordOrder.getOrderId());
            if(Constant.REFUND_APPLY_TYPE.REFUND.name().equals(result)){
                ticketRefundStatus = true;
            }
        }catch (Exception e){
            LOG.info("调用门票可退改状态ERROR : orderId = "+ ordOrder.getOrderId(),e.getStackTrace());
        }
        for(OrdRefundSaleRecord saleRecord : saleRecordList){
            if( ticketRefundStatus && saleRecord.isTicket()){
                saleRecord.setStatus(OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue());
            }
            Map<String, Object> params = new HashMap<>();
            params.put("orderItemId", saleRecord.getOrderItemId());
            List<OrdRefundSaleRecord> ordRefundSaleRecordList = this.findOrdRefundSaleRecordList(params);
            if(ordRefundSaleRecordList != null && ordRefundSaleRecordList.size() == 1){
                ordRefundSaleRecordDao.updateByOrderItemIdSelective(saleRecord);
            }else {
                ordRefundSaleRecordDao.insert(saleRecord);
            }

        }

    }

    @Override
    public List<OrdRefundSaleRecord> getOrdRefundSaleRecordByOrder(OrdOrder ordOrder, Date applyTime){
        List<OrdRefundSaleRecord> saleRecordList = new ArrayList<>();
        LOG.info("getRefundAmount : orderId = "+ordOrder.getOrderId()+", applyTime = "+applyTime);

        List<OrdOrderItem> packageOrderItems = new ArrayList<>();
        List<OrdOrderItem> insuranceOrderItems = new ArrayList<>();
        List<OrdOrderItem> otherOrderItems = new ArrayList<>();
        Long orderActualAmount = ordOrder.getActualAmount();
        for(OrdOrderItem orderItem : ordOrder.getOrderItemList()){
            if(orderItem.getOrderPackId() != null){
                packageOrderItems.add(orderItem);
            }else if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
                insuranceOrderItems.add(orderItem);
                orderActualAmount = orderActualAmount - orderItem.getTotalAmount();
            }else{
                otherOrderItems.add(orderItem);
                orderActualAmount = orderActualAmount - orderItem.getTotalAmount();
            }
        }



        if(packageOrderItems != null){
            List<OrdRefundSaleRecord> packageSaleRecord = this.getPackageOrdRefundSaleRecordList(packageOrderItems, ordOrder, applyTime,orderActualAmount);
            saleRecordList.addAll(packageSaleRecord);
        }
        if(insuranceOrderItems != null){
            List<OrdRefundSaleRecord> insuranceSaleRecord = this.getInsuranceOrdRefundSaleRecordList(insuranceOrderItems, applyTime);
            saleRecordList.addAll(insuranceSaleRecord);
        }
        if(otherOrderItems != null){
            List<OrdRefundSaleRecord> otherSaleRecord = this.getOtherOrdRefundSaleRecordList(otherOrderItems,applyTime);
            saleRecordList.addAll(otherSaleRecord);
        }


        return saleRecordList;
    }


    /*******************************************************************私有方法*********************************************************************/

    /**
     * 获取保险退改记录
     * @param insuranceOrderItems 保险子订单列表
     * @param applyTime 申请时间
     * @return
     */
    private List<OrdRefundSaleRecord> getInsuranceOrdRefundSaleRecordList(List<OrdOrderItem> insuranceOrderItems, Date applyTime){
        List<OrdRefundSaleRecord> saleRecordList = new ArrayList<>();

        for(OrdOrderItem orderItem : insuranceOrderItems){

            OrdRefundSaleRecord ordRefundSaleRecord = new OrdRefundSaleRecord();
            ordRefundSaleRecord.setCategoryId(orderItem.getCategoryId());
            ordRefundSaleRecord.setOrderId(orderItem.getOrderId());
            ordRefundSaleRecord.setOrderItemId(orderItem.getOrderItemId());
            ordRefundSaleRecord.setStatus(OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue());
            ordRefundSaleRecord.setApplyTime(applyTime);
            ordRefundSaleRecord.setTotalAmount(orderItem.getTotalAmount());
            ordRefundSaleRecord.setCancelStrategy(orderItem.getCancelStrategy());


            Map<String, Object> content = new HashMap<>();
            content.put("quantity", orderItem.getQuantity());
            content.put("cancelStrategy",orderItem.getCancelStrategy());

            //实际支付金额
            Long oughtAmount = orderItem.getTotalAmount();
            ordRefundSaleRecord.setOughtAmount(oughtAmount);
            content.put("oughtAmount",oughtAmount);
            LOG.info("订单实际支付金额 oughtAmount = " + oughtAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());


            //子订单根据退改规则应扣金额
            Long itemDeductAmount = orderItem.getTotalAmount();


            content.put("lastCancelTime", orderItem.getLastCancelTime());
            content.put("deductAmount", orderItem.getDeductAmount());
            content.put("productType", orderItem.getProductType());

            if(!"INSURANCE_738".equals(orderItem.getProductType())){//非取消险
                if(orderItem.getLastCancelTime() !=null){
                    if(DateUtil.isCompareTime(applyTime, orderItem.getLastCancelTime())){//没有超过最晚取消时间
                        itemDeductAmount = 0L;
                    }else{
                        itemDeductAmount = orderItem.getDeductAmount();
                    }
                }
            }


            LOG.info("订单应扣金额 itemDeductAmount = " + itemDeductAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());
            ordRefundSaleRecord.setDeductAmount(itemDeductAmount);

            //子订单实际退款金额
            Long itemRefundActualAmount = orderItem.getTotalAmount() - itemDeductAmount;
            LOG.info("子订单实际退款金额 itemRefundActualAmount = " + itemRefundActualAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());
            ordRefundSaleRecord.setRefundMoney(itemRefundActualAmount);


            ordRefundSaleRecord.setContent(JSON.toJSONString(content));
            saleRecordList.add(ordRefundSaleRecord);
        }
        return saleRecordList;
    }


    /**
     * 除去保险和被打包的剩余子订单
     * @param otherOrderItems
     * @param applyTime
     * @return
     */
    private List<OrdRefundSaleRecord> getOtherOrdRefundSaleRecordList(List<OrdOrderItem> otherOrderItems, Date applyTime){

        List<OrdRefundSaleRecord> saleRecordList = new ArrayList<>();

        for(OrdOrderItem orderItem : otherOrderItems){

            OrdRefundSaleRecord ordRefundSaleRecord = new OrdRefundSaleRecord();
            ordRefundSaleRecord.setCategoryId(orderItem.getCategoryId());
            ordRefundSaleRecord.setOrderId(orderItem.getOrderId());
            ordRefundSaleRecord.setOrderItemId(orderItem.getOrderItemId());
            ordRefundSaleRecord.setStatus(OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue());
            if(orderItem.isTicket()){
                ordRefundSaleRecord.setStatus(OrdRefundSaleRecord.STATUS.SALEORDER.getValue());
            }
            ordRefundSaleRecord.setApplyTime(applyTime);
            ordRefundSaleRecord.setTotalAmount(orderItem.getTotalAmount());
            ordRefundSaleRecord.setCancelStrategy(orderItem.getCancelStrategy());

            Map<String, Object> content = new HashMap<>();
            content.put("quantity", orderItem.getQuantity());
            content.put("cancelStrategy",orderItem.getCancelStrategy());

            //实际支付金额
            Long oughtAmount = orderItem.getTotalAmount();
            ordRefundSaleRecord.setOughtAmount(oughtAmount);
            content.put("oughtAmount",oughtAmount);
            LOG.info("订单实际支付金额 oughtAmount = " + oughtAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());

            //子订单根据退改规则应扣金额
            Long itemDeductAmount = orderItem.getTotalAmount();

            if(!ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
                if(orderItem.isTicket()){
                    itemDeductAmount = getItemDeductAmount(orderItem,orderItem.getTotalAmount(),"GOODSRETREATANDCHANGE",
                            applyTime,orderItem.getVisitTime(), content);
                }
            }


            LOG.info("订单应扣金额 itemDeductAmount = " + itemDeductAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());
            ordRefundSaleRecord.setDeductAmount(itemDeductAmount);


            Long itemRefundActualAmount = orderItem.getTotalAmount() - itemDeductAmount;
            LOG.info("子订单实际退款金额 itemRefundActualAmount = " + itemRefundActualAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + orderItem.getOrderId());
             ordRefundSaleRecord.setRefundMoney(itemRefundActualAmount);

            ordRefundSaleRecord.setContent(JSON.toJSONString(content));
            saleRecordList.add(ordRefundSaleRecord);
        }
        return saleRecordList;
    }


    /**
     * 获取被打包的子订单退款记录
     * @param packageOrderItems 被打包的子订单列表
     * @param ordOrder
     * @param applyTime
     * @param orderActualAmount 子订单列表包含的子订单实际支付金额
     * @return
     */
    private List<OrdRefundSaleRecord> getPackageOrdRefundSaleRecordList(List<OrdOrderItem> packageOrderItems, OrdOrder ordOrder, Date applyTime, Long orderActualAmount){
        List<OrdRefundSaleRecord> saleRecordList = new ArrayList<>();

        LOG.info("订单实际支付金额 orderActualAmount = "+ orderActualAmount +" orderId = " + ordOrder.getOrderId());

        //订单实际总金额(被打包子订单实际金额相加)
        Long orderTotalAmount = 0L;
        for(OrdOrderItem orderItem : packageOrderItems){
            //计算订单实际总金额
            orderTotalAmount += orderItem.getTotalAmount();
        }
        LOG.info("订单总金额(被打包子订单实际金额相加) orderTotalAmount = " + orderTotalAmount +" orderId = " + ordOrder.getOrderId());

        int count = 0;
        Long totalOughtAmount = 0L;
        for(OrdOrderItem orderItem : packageOrderItems){
            count ++;
            OrdRefundSaleRecord ordRefundSaleRecord = new OrdRefundSaleRecord();
            ordRefundSaleRecord.setCategoryId(orderItem.getCategoryId());
            ordRefundSaleRecord.setOrderId(orderItem.getOrderId());
            ordRefundSaleRecord.setOrderItemId(orderItem.getOrderItemId());
            ordRefundSaleRecord.setStatus(OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue());
            if(orderItem.isTicket()){
                ordRefundSaleRecord.setStatus(OrdRefundSaleRecord.STATUS.SALEORDER.getValue());
            }
            ordRefundSaleRecord.setApplyTime(applyTime);
            ordRefundSaleRecord.setTotalAmount(orderItem.getTotalAmount());
            ordRefundSaleRecord.setCancelStrategy(orderItem.getCancelStrategy());


            Map<String, Object> content = new HashMap<>();
            content.put("quantity", orderItem.getQuantity());
            content.put("cancelStrategy",orderItem.getCancelStrategy());

            //实际支付金额 = 子订单总金额 * 被打包子订单实际支付金额 / 被打包子订单总金额
            Long oughtAmount = Math.round(orderItem.getTotalAmount().doubleValue() * orderActualAmount.doubleValue() / orderTotalAmount.doubleValue());
            if(count == packageOrderItems.size()){
                oughtAmount = orderActualAmount - totalOughtAmount;
            }
            ordRefundSaleRecord.setOughtAmount(oughtAmount);
            content.put("oughtAmount",oughtAmount);

            totalOughtAmount += oughtAmount;


            //子订单根据退改规则应扣金额
            Long itemDeductAmount = orderItem.getTotalAmount();

            if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())) {//可退该
                itemDeductAmount = getItemDeductAmount(orderItem,orderTotalAmount,"RETREATANDCHANGE",
                        applyTime,ordOrder.getVisitTime(), content);
            }else if(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())) {//同步商品退改
                if(!ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
                    if(orderItem.isTicket()){
                        itemDeductAmount = getItemDeductAmount(orderItem,orderItem.getTotalAmount(),"GOODSRETREATANDCHANGE",
                                applyTime,ordOrder.getVisitTime(), content);
                    }else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
                        if(orderItem.getLastCancelTime() !=null){
                            content.put("lastCancelTime", orderItem.getLastCancelTime());
                            content.put("deductAmount", orderItem.getDeductAmount());
                            if(DateUtil.isCompareTime(applyTime, orderItem.getLastCancelTime())){
                                itemDeductAmount = 0L;
                            }else {
                                itemDeductAmount = orderItem.getDeductAmount();
                            }
                        }
                    }
                }
            }


            LOG.info("订单应扣金额 itemDeductAmount = " + itemDeductAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + ordOrder.getOrderId());

            //子订单应退金额 = 子订单实际支付金额 - 扣款金额
            Long itemRefundActualAmount  = oughtAmount - itemDeductAmount ;

            if(itemRefundActualAmount < 0 ){
                itemRefundActualAmount = 0L;
                itemDeductAmount = oughtAmount;
                LOG.info("实际扣款金额 itemDeductAmount = " + itemDeductAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + ordOrder.getOrderId());
            }
            ordRefundSaleRecord.setDeductAmount(itemDeductAmount);

            LOG.info("子订单实际退款金额 itemRefundActualAmount = " + itemRefundActualAmount + ", orderItemId = " + orderItem.getOrderItemId() +" orderId = " + ordOrder.getOrderId());

            ordRefundSaleRecord.setRefundMoney(itemRefundActualAmount);
            ordRefundSaleRecord.setContent(JSON.toJSONString(content));
            saleRecordList.add(ordRefundSaleRecord);

        }

        return saleRecordList;
    }


    /**
     * 根据退款规则获取扣款金额
     * @param ordOrderItem 	子订单
     * @param orderTotalAmount	参与平摊的子订单总金额
     * @param cancelStrategy 主订单退改策略
     * @param applyDate		申请日期
     * @param visitDate		游玩日期
     * @return
     */
    private Long getItemDeductAmount(OrdOrderItem ordOrderItem, Long orderTotalAmount, String cancelStrategy,
                                     Date applyDate, Date visitDate, Map<String, Object> content){
        //将退款规则的json的转化为List
        //List<ProdRefundRule> rulesList = JSONArray.parseArray(ordOrderItem.getRefundRules(), ProdRefundRule.class);
        List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
		if(StringUtil.isNotEmptyString(ordOrderItem.getRefundRules())){
			List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(ordOrderItem.getRefundRules(), ProdRefundRule.class);
			if(refundList_ != null){
				rulesList = refundList_;
			}
		}
        //扣款金额
        Long deductAmount = ordOrderItem.getTotalAmount();
        //计算出游时间和申请时间相差的秒数
        Long seconds = (visitDate.getTime() - applyDate.getTime())/1000;
        //符合条件的退款规则
        ProdRefundRule accordRule = null;
        for(ProdRefundRule refundRule : rulesList){
            //因为不满足以上条件是最后一条，所以如果循环到不满足以上条件则直接匹配
            if(ProdRefundRule.CANCEL_TIME_TYPE.OTHER.getCode().equals(refundRule.getCancelTimeType())){
                accordRule = refundRule;
                break;
            }
            //选择出符合当前时间的退改规则
            if(seconds >= refundRule.getCancelTime()*60){
                accordRule = refundRule;
                break;
            }
        }
        if(accordRule != null){
            content.put("accordRule", JSON.toJSONString(accordRule));
            if(ProdRefundRule.DEDUCTTYPE.AMOUNT.getCode().equals(accordRule.getDeductType())){//扣固定金额
                if("RETREATANDCHANGE".equals(cancelStrategy)){
                    //扣款金额 = 子订单总金额 * (扣款金额 / 订单总金额)
                    deductAmount = Math.round(ordOrderItem.getTotalAmount().doubleValue()*(accordRule.getDeductValue().doubleValue()/orderTotalAmount.doubleValue()));
                }else if("GOODSRETREATANDCHANGE".equals(cancelStrategy)){
                    //退款金额 = 扣款金额 * 份数
                    deductAmount = accordRule.getDeductValue() * ordOrderItem.getQuantity();
                }
            }else if(ProdRefundRule.DEDUCTTYPE.PERCENT.getCode().equals(accordRule.getDeductType())){//扣比例
                //扣款金额 = 子订单总金额 * (扣款比例 / 10000)
                deductAmount = Math.round(ordOrderItem.getTotalAmount().doubleValue()*(accordRule.getDeductValue().doubleValue()/10000));
            }
        }
        return deductAmount;
    }
}
