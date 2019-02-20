package com.lvmama.vst.order.service.refund.impl;

import com.lvmama.comm.utils.StringUtil;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.po.HotelOrderRefundSplitInfoPO;
import com.lvmama.vst.order.service.IHotelOrderRefundSplitService;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.vo.HotelOrderRefundSplitQueryVO;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by lijuntao on 2017/4/28.
 */
@Service("hotelOrderRefundSplitService")
public class HotelOrderRefundSplitServiceImpl implements IHotelOrderRefundSplitService{

    private static final Logger LOG = LoggerFactory.getLogger(HotelOrderRefundSplitServiceImpl.class);

    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

    @Autowired
    private OrderService orderService;

    @Override
    public List<HotelOrderRefundSplitInfoPO> getHotelOrderRefundSplitInfo(Long orderId, List<HotelOrderRefundSplitQueryVO> hotelOrderRefundSplitQueryVOList,Date applyTime) throws BusinessException {

        List<HotelOrderRefundSplitInfoPO> splitInfoPOList = new ArrayList<>();

        boolean isAll = true;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderItemId", hotelOrderRefundSplitQueryVOList.get(0).getOrderItemId());
        List<OrdOrderHotelTimeRate> hotelTimeRateList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateListByParams(params);
        if(hotelOrderRefundSplitQueryVOList.size() != hotelTimeRateList.size()){
            isAll = false;
        }
        Map<Date, HotelOrderRefundSplitQueryVO> splitQueryVOMap = new HashMap<>();
        for(HotelOrderRefundSplitQueryVO vo : hotelOrderRefundSplitQueryVOList){
            splitQueryVOMap.put(vo.getVisitTime(), vo);
        }
        for(OrdOrderHotelTimeRate timeRate : hotelTimeRateList){
            if(splitQueryVOMap.get(timeRate.getVisitTime()) == null){
                isAll = false;
                break;
            }
            if(splitQueryVOMap.get(timeRate.getVisitTime()).getRefundQuantity() < timeRate.getQuantity()){
                isAll = false;
                break;
            }
        }

        OrdOrder ordOrder = orderService.queryOrderWithItems(orderId);
        Long orderActualAmount = ordOrder.getActualAmount();
        Long orderTotalAmount = 0L;
        Long totalDeductAmount = 0L;
        for(OrdOrderItem orderItem : ordOrder.getOrderItemList()){
            orderTotalAmount += orderItem.getTotalAmount();
            totalDeductAmount += orderItem.getDeductAmount();
        }


        if(isAll){
            for(OrdOrderHotelTimeRate timeRate : hotelTimeRateList){
                if(applyTime.before(ordOrder.getLastCancelTime())){
                    HotelOrderRefundSplitInfoPO splitInfoPO = new HotelOrderRefundSplitInfoPO();
                    splitInfoPO.setOrderItemId(timeRate.getOrderItemId());
                    splitInfoPO.setVisitTime(timeRate.getVisitTime());
                    splitInfoPO.setRefundQuantity(timeRate.getQuantity());
                    Long oughtPrice = Math.round(timeRate.getPrice().doubleValue() * orderActualAmount.doubleValue() / orderTotalAmount.doubleValue());
                    //实付金额
                    splitInfoPO.setRefundOughtPrice(oughtPrice);
                    //退款金额
                    splitInfoPO.setInitRefundedPrice(oughtPrice);
                    //扣款金额
                    splitInfoPO.setInitActualLoss(0L);
                    splitInfoPOList.add(splitInfoPO);
                }else{
                    HotelOrderRefundSplitInfoPO splitInfoPO = new HotelOrderRefundSplitInfoPO();
                    splitInfoPO.setOrderItemId(timeRate.getOrderItemId());
                    splitInfoPO.setVisitTime(timeRate.getVisitTime());
                    splitInfoPO.setRefundQuantity(timeRate.getQuantity());
                    Long oughtPrice = Math.round(timeRate.getPrice().doubleValue() * orderActualAmount.doubleValue() / orderTotalAmount.doubleValue());
                    //实付金额
                    splitInfoPO.setRefundOughtPrice(oughtPrice);
                    if(totalDeductAmount > oughtPrice){
                        //扣款金额
                        splitInfoPO.setInitActualLoss(oughtPrice);
                        //退款金额
                        splitInfoPO.setInitRefundedPrice(0L);
                        totalDeductAmount -= oughtPrice;
                    }else{
                        //扣款金额
                        splitInfoPO.setInitActualLoss(totalDeductAmount);
                        //退款金额
                        splitInfoPO.setInitRefundedPrice(oughtPrice - totalDeductAmount);
                        totalDeductAmount = 0L;
                    }

                    splitInfoPOList.add(splitInfoPO);
                }

            }
        }else{
            for(OrdOrderHotelTimeRate timeRate : hotelTimeRateList){
                HotelOrderRefundSplitInfoPO splitInfoPO = new HotelOrderRefundSplitInfoPO();
                splitInfoPO.setOrderItemId(timeRate.getOrderItemId());
                splitInfoPO.setVisitTime(timeRate.getVisitTime());
                splitInfoPO.setRefundQuantity(timeRate.getQuantity());
                Long oughtPrice = Math.round(timeRate.getPrice().doubleValue() * orderActualAmount.doubleValue() / orderTotalAmount.doubleValue());
                //实付金额
                splitInfoPO.setRefundOughtPrice(oughtPrice);
                //退款金额
                splitInfoPO.setInitRefundedPrice(0L);
                //扣款金额
                splitInfoPO.setInitActualLoss(oughtPrice);
                splitInfoPOList.add(splitInfoPO);
            }
        }
        return splitInfoPOList;
    }

    /**废弃方法**/
    @Deprecated
    private Long getRefundSplitDeductAmount(Long orderItemId, Date visitTime, Long quantity, Date applyTime) throws BusinessException {

        LOG.info("getRefundSplitDeductAmount : orderItemId = " + orderItemId + ", visitTime = " + visitTime + ", quantity = " + quantity + ", applyTime = " + applyTime);
        Long deductAmount = 0L;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderItemId", orderItemId);
        params.put("visitTime", visitTime);
        List<OrdOrderHotelTimeRate> hotelTimeRateList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateListByParams(params);

        if(hotelTimeRateList == null || hotelTimeRateList.size() ==0 ){
            LOG.error("OrdOrderHotelTimeRate查询为空, orderItemId= " + orderItemId);
            throw new BusinessException("getRefundSplitDeductAmountERROR","OrdOrderHotelTimeRate查询为空");
        }

        String cancelStrategy = hotelTimeRateList.get(0).getCancelStrategy();
        if(StringUtil.isEmptyString(cancelStrategy)){
            LOG.error("cancelStrategy为空, orderItemId= " + orderItemId);
            throw new BusinessException("getRefundSplitDeductAmountERROR","cancelStrategy为空");
        }

        String deductType = hotelTimeRateList.get(0).getDeductType();
        Long deductValue = hotelTimeRateList.get(0).getDeductValue();
        Long lastCancelTime = hotelTimeRateList.get(0).getLatestCancelTime();
        Long price = hotelTimeRateList.get(0).getPrice();
        Long totalQuantity = hotelTimeRateList.get(0).getQuantity();
        if(quantity > totalQuantity){
            LOG.error("申请取消的数量不能大于最大数量, orderItemId= " + orderItemId);
            throw new BusinessException("getRefundSplitDeductAmountERROR","申请取消的数量不能大于最大数量");
        }


        try{
            if(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(cancelStrategy)){//不退不改
                deductAmount = price * quantity;
            }else if(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(cancelStrategy)){//可退该
                //计算出游时间和申请时间相差的秒数
                Long seconds = (visitTime.getTime() - applyTime.getTime())/1000;
                if(seconds > lastCancelTime * 60){//未超过最晚无损取消时间
                    deductAmount = 0L;
                }else if(SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.getCode().equals(deductType) ||
                        SuppGoodsTimePrice.DEDUCTTYPE.FULL.getCode().equals(deductType)){//扣首日或全额
                    deductAmount = price * quantity;
                }else if(SuppGoodsTimePrice.DEDUCTTYPE.NONE.getCode().equals(deductType)){//扣款类型为否
                    deductAmount = 0L;
                }else if(SuppGoodsTimePrice.DEDUCTTYPE.MONEY.getCode().equals(deductType)){//扣固定金额
                    deductAmount = deductValue * quantity;
                }else if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.getCode().equals(deductType)){//扣百分比
                    deductAmount = Math.round(price.doubleValue() * (deductValue.doubleValue() / 100));
                }
            }
        }catch (Exception e){
            LOG.error("计算扣款金额出现错误, orderItemId= " + orderItemId + ", visitTime = " + visitTime);
            throw new BusinessException("计算扣款金额出现错误, orderItemId= " +orderItemId, ExceptionUtils.getStackTrace(e));
        }

        return deductAmount;
    }
}
