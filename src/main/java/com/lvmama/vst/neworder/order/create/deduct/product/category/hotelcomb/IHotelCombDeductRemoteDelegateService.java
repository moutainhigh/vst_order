package com.lvmama.vst.neworder.order.create.deduct.product.category.hotelcomb;

import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/4/24.
 */
public interface IHotelCombDeductRemoteDelegateService {

    void deductStock(OrdOrderDTO order);
}
