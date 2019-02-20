package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.adapter;

import com.lvmama.comm.bee.po.prod.ProdProduct;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;

import java.util.Date;

/**
 * Created by dengcheng on 17/3/21.
 * 酒店子系统产品适配服务
 */
public interface IHotelSysProductAdpaterService {

    /**
     * 适配各个系统商品服务
     * @param id
     * @param categoryId
     * @return
     */
    SuppGoods routerToSuppGoods(Long id,Long subCategoryId);

    BaseTimePrice routerToGoodsTimePrice(Long id,Long planId,Date specDate, Long subCategoryId,Long quantity);

    /**
     * 适配各个系统产品服务
     * @param id
     * @param categoryId
     * @return
     */
    ProdProduct findProductById(Long id,Long subCategoryId);

    /**
     * 适配各个系统时间价格服务
     * @param id
     * @param specDate
     * @param categoryId
     * @return
     */
    SuppGoodsTimePrice findGoodsTimePriceByParams(Long id, Date specDate, Long subCategoryId);
}
