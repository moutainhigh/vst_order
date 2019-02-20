package com.lvmama.vst.order.snapshot.service;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;

/**
 * VST快照对外服务
 */
public interface IVstSnapshotService {
    /**
     * 产品
     * @param ordOrder
     * @param prodProduct
     */
    public void orderSnapshot_prodProduct(OrdOrder ordOrder, ProdProduct prodProduct);
    /**
     * 子产品
     * @param ordOrderItem
     * @param prodProduct
     */
    public void orderSnapshot_prodProduct(OrdOrderItem ordOrderItem, ProdProduct prodProduct);
    /**
     * 产品规格
     * @param ordOrderItem
     * @param prodProductBranch
     */
    public void orderSnapshot_prodProductBranch(OrdOrderItem ordOrderItem, ProdProductBranch prodProductBranch);
    /**
     * 商品
     * @param ordOrderItem
     * @param suppGoods
     */
    public void orderSnapshot_suppSuppGoods(OrdOrderItem ordOrderItem, SuppGoods suppGoods);
}
