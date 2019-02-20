package com.lvmama.vst.order.service.apportion.category;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/21.
 * 分摊服务
 */
public interface OrderAmountApportionPerformer {
    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     * */
    void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList);
}
