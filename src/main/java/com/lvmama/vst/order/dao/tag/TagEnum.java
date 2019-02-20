package com.lvmama.vst.order.dao.tag;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/6/12.
 */
public class TagEnum implements Serializable {

    /**
     * 订单扩容字段枚举类型
     */
    public static enum ORD_ORDER_TAG {
        ORD_BRAND_TAG,//品牌馆(Y/N)
        ORD_CREDIT_TAG;//信用住(Y/N)
        private ORD_ORDER_TAG() {}
    }
    public static enum ORD_OBJECT_TAG{
        ORD_ORDER_ITEM,
        ORD_ORDER;
        private ORD_OBJECT_TAG(){}
    }

}
