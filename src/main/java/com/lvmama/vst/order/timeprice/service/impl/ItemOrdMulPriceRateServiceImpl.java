package com.lvmama.vst.order.timeprice.service.impl;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.timeprice.po.OrderItemPricePO;
import com.lvmama.vst.order.timeprice.service.ItemOrdMulPriceRateService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created  on 2016/11/7.
 */
@Component("itemOrdMulPriceRateServiceImpl")
public class ItemOrdMulPriceRateServiceImpl implements ItemOrdMulPriceRateService {
    @Override
    public void calculateOrdMulPriceRate(OrdOrderItem orderItem, OrderItemPricePO orderItemPricePO) {
        if (orderItem == null || orderItemPricePO == null || orderItemPricePO.getAdultPrice() == null) {
            return;
        }

        long allPriceAmount = 0;
        long allSettlementPriceAmount = 0;
        long allMarketPriceAmount = 0;

        List<OrdMulPriceRate> ordMulPriceRateList = new ArrayList<OrdMulPriceRate>();

        OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
        int adultNum = orderItemDto.getItem().getAdultQuantity();    //不乘份数
        int childNum = orderItemDto.getItem().getChildQuantity();    //不乘份数

        if (adultNum>0) {
            Long adultPrice = orderItemPricePO.getAdultPrice();
            Long adultSettlementPrice = 0L;
            if(orderItemPricePO.getAdultSettlementPrice() == null){
                adultSettlementPrice=adultPrice;
            }else{
                adultSettlementPrice=orderItemPricePO.getAdultSettlementPrice();
            }
            OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultPrice, (long) adultNum, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultSettlementPrice, (long) adultNum, OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultPrice, (long) adultNum, OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_ADULT.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            allPriceAmount += adultPrice * adultNum;
            allSettlementPriceAmount += adultSettlementPrice * adultNum;
            allMarketPriceAmount += adultPrice * adultNum;
        }

        if (childNum>0) {
            Long childPrice = orderItemPricePO.getChildPrice();
            Long childSettlementPrice = 0L;
            if(orderItemPricePO.getChildSettlementPrice() == null){
                childSettlementPrice=childPrice;
            }else{
                childSettlementPrice=orderItemPricePO.getChildSettlementPrice();
            }
            OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childPrice, (long) childNum, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childSettlementPrice, (long) childNum, OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childPrice, (long) childNum, OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_CHILD.name());
            ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
            ordMulPriceRateList.add(ordMulPriceRate);

            allPriceAmount += childPrice * childNum;
            allSettlementPriceAmount += childSettlementPrice * childNum;
            allMarketPriceAmount += childPrice * childNum;
        }

        //把总价格除以总人数，然后4舍5入
        long priceAmount = new BigDecimal(allPriceAmount / orderItem.getQuantity()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        long settlementPriceAmount = new BigDecimal(allSettlementPriceAmount / orderItem.getQuantity()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        long marketPriceAmount = new BigDecimal(allMarketPriceAmount / orderItem.getQuantity()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

        OrderUtils.accumulateOrderItemPrice(orderItem,
                priceAmount,
                settlementPriceAmount,
                settlementPriceAmount,
                marketPriceAmount);

        orderItem.setOrdMulPriceRateList(ordMulPriceRateList);
    }
}
