package com.lvmama.vst.neworder.order.router;

import com.lvmama.vst.back.goods.po.SuppGoods;

/**
 * Created by dengcheng on 17/3/22.
 * 商品抽象路由服务层
 */
public interface IGoodsRouterService {
    SuppGoods findGoodsById(Long id);
}
