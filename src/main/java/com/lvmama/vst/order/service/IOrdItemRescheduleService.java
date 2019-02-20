package com.lvmama.vst.order.service;

import com.lvmama.vst.back.goods.po.SuppGoodsReschedule;
import com.lvmama.vst.back.order.po.OrdItemReschedule;

public interface IOrdItemRescheduleService {

    int addOrdItemReschedule(OrdItemReschedule ordItemReschedule);

    OrdItemReschedule findOrdItemRescheduleById(Long ordItemRescheduleId);

    int updateOrdItemReschedule(OrdItemReschedule ordItemReschedule);

    OrdItemReschedule findOrdItemRescheduleByOrdItemId(Long ordItemId);

    SuppGoodsReschedule toSuppGoodsReschedule(Long ordItemId);
    
    int updateExchangeCountByOrdItemId(Long ordItemId);
}
