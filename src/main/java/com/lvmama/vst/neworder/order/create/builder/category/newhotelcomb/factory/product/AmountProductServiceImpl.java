package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dengcheng on 17/2/23.
 */
@Component
public class AmountProductServiceImpl implements IAmountProduct {

    @Override
    public List<SaleAmount> buildSaleAmount(DestBuBuyInfo buyInfo) {
        return null;
    }

    @Override
    public List<ExpressAmount> buildExpressAmount(DestBuBuyInfo buyInfo) {
        return null;
    }

    @Override
    public List<GoodsAmount> buildGoodsAmount(DestBuBuyInfo buyInfo) {
        return null;
    }
}
