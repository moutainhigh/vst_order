package com.lvmama.vst.neworder.order.cal.category.hotelcomb;

import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.neworder.order.cal.IOrderCalFactory;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.BonusAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.CouponAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ExpressAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.GoodsAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.SaleAmount;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.service.impl.PromBuyPresentBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dengcheng on 17/2/22.
 */
@Component("newHotelCombCalFactory")
public class NewHotelCombCalFactory implements IOrderCalFactory {

    @Autowired
    ICalProduct calProductService;
    
    @Autowired
	private PromBuyPresentBussiness promBuyPresentBussiness;

    @Override
	public ProductAmountItem buildProductAmountItem(OrdOrderDTO order,BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo) {
        ProductAmountItem ammountItem = new ProductAmountItem();
        OrderHotelCombBuyInfo buyInfo =  baseBuyInfo.getT();
        Long subDistributionId = null;
        List<GoodsAmount> goodsAmmountList =  calProductService.buildGoodsAmount(order);
        List<ExpressAmount> expressAmmountList =  calProductService.buildExpressAmount(order);
    //     促销代码暂时不提交
        List<SaleAmount> saleAmmountList =  calProductService.buildSaleAmount(order,buyInfo,ammountItem);
        //满赠
        BuyPresentActivityInfo buyPresentInfo = promBuyPresentBussiness.findPromBuyPresentForOrder(order, buyInfo.getUserNo());
        //优惠
        List<CouponAmount> couponAmounts =  calProductService.buildCouponAmount(order, buyInfo);
        BonusAmount  bonusAmount = calProductService.buildBonusAmount(order, buyInfo);
        ammountItem.setBonusAmount(bonusAmount);
        ammountItem.setCouponAmountList(couponAmounts);
        ammountItem.setExpressAmountList(expressAmmountList);
        ammountItem.setGoodsAmount(goodsAmmountList);
        ammountItem.setSaleAmountList(saleAmmountList);
        ammountItem.setBuyPresentInfo(buyPresentInfo);

       
        return ammountItem;
    }


}
