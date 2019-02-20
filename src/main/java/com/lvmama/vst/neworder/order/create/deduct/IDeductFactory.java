package com.lvmama.vst.neworder.order.create.deduct;

import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/4/24.
 */
public interface IDeductFactory {
    void deductOrder(OrdOrderDTO order);
}
