package com.lvmama.vst.order.service.book.impl;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.OrderCheckAheadTimeVo;
import com.lvmama.vst.order.service.book.OrderCheckService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单下单检测
 *
 * @author Rumly
 */
@Service("orderCheckService")
public class OrderCheckServiceImpl implements OrderCheckService {
    private static Logger logger = LoggerFactory.getLogger(OrderCheckServiceImpl.class);

    @Autowired
    private ProdProductClientService prodProductClientService;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

    final static String GOODS_SALE_OUT = "对不起，您所预订的产品已经订完。";
    final static String INSURANCE_CANNOT_SALE = "对不起，您所预订的产品保险无法购买，是否要继续预定？<br />" +
            "继续预订：系统自动去除保险，需要您核对产品以及总金额<br />" +
            "返回修改：您自行修改";

    @Override
    public ResultHandleT<OrderCheckAheadTimeVo> checkAheadTime(BuyInfo buyInfo) {
        ResultHandleT<OrderCheckAheadTimeVo> handle = new ResultHandleT();

        OrderCheckAheadTimeVo checkAheadTimeVo = new OrderCheckAheadTimeVo();
        checkAheadTimeVo.setAheadCheckMsg("");
        checkAheadTimeVo.setHasContinue(false);
        checkAheadTimeVo.setInsuranceList(new ArrayList<Long>());
        handle.setReturnContent(checkAheadTimeVo);

        ProdProduct mainProduct = null;
        if (buyInfo.getProductId() != null) {
            mainProduct = getProduct(buyInfo.getProductId());
        }

        if (null == mainProduct) {
            String msg = "校验提前预定时间获取产品异常";
            logger.error(msg);
            handle.setMsg(msg);
            return handle;
        }

        long mainCategoryId = mainProduct.getBizCategoryId();

        if (!isCheckAheadTime(mainProduct)) {
            return handle;
        }

        List<SuppGoodsBaseTimePrice> timepriceList = getGoodsTimePrices(buyInfo);

        for (SuppGoodsBaseTimePrice timeprice : timepriceList) {
            Date specDate = timeprice.getSpecDate();
            if (timeprice instanceof SuppGoodsNotimeTimePrice) {
                specDate = ((SuppGoodsNotimeTimePrice) timeprice).getEndDate();
            }

            Date date = DateUtils.addMinutes(specDate, -timeprice.getAheadBookTime().intValue());
            date = addAheadTime(date, Constants.AHEAD_TIME_ON_INFO_FILLIN);

            if (date.before(new Date())) { //当前时间在提前预定时间点之后，不能预定
                ProdProduct goodsProduct;
                SuppGoodsParam param = new SuppGoodsParam();
                param.setProduct(true);
                ProdProductParam ppp = new ProdProductParam();
                param.setProductParam(ppp);
                Long suppGoodsId = timeprice.getSuppGoodsId();
                ResultHandleT<SuppGoods> prodResultHandleT = suppGoodsClientService.findSuppGoodsById(
                        suppGoodsId, param);
                if (prodResultHandleT == null || prodResultHandleT.isFail() ||
                        prodResultHandleT.getReturnContent() == null) {
                    continue;
                }
                goodsProduct = prodResultHandleT.getReturnContent().getProdProduct();

                long packCategoryId = goodsProduct.getBizCategoryId();

                // 如果是机酒、酒景、交通+服务产品中非保险其他产品时间过期，则提示已订完，不能下单；
                // 如果是保险产品，则提示保险已超过投保时间,可继续下单
                if (mainCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId()) {
                    if (packCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId()) {
                        checkAheadTimeVo.setAheadCheckMsg(INSURANCE_CANNOT_SALE);
                        checkAheadTimeVo.setHasContinue(true);
                        checkAheadTimeVo.getInsuranceList().add(suppGoodsId);
                    } else {
                        checkAheadTimeVo.setAheadCheckMsg(GOODS_SALE_OUT);
                        checkAheadTimeVo.setHasContinue(false);
                        break; //售完优先
                    }
                } else if (mainCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId()) {
                    //如果是跟团则提示已订完
                    checkAheadTimeVo.setAheadCheckMsg(GOODS_SALE_OUT);
                    checkAheadTimeVo.setHasContinue(false);
                    break; //售完优先
                }
            }
        }

        //机酒,并且出现保险不可售特殊处理
        if (mainProduct.getSubCategoryId() == null) {
            return handle;
        }

        if (mainProduct.getSubCategoryId().longValue() == BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId()
                && checkAheadTimeVo.getAheadCheckMsg().equalsIgnoreCase(INSURANCE_CANNOT_SALE)) {
            boolean isSaleOut = false;

            List<BuyInfo.Item> allItemList = getItems(buyInfo);

            for (SuppGoodsBaseTimePrice timeprice : timepriceList) {
                ProdProduct goodsProduct = null;
                SuppGoodsParam param = new SuppGoodsParam();
                param.setProduct(true);
                ProdProductParam ppp = new ProdProductParam();
                param.setProductParam(ppp);
                ResultHandleT<SuppGoods> prodResultHandleT = suppGoodsClientService.findSuppGoodsById(
                        timeprice.getSuppGoodsId(), param);
                if (prodResultHandleT == null || prodResultHandleT.isFail() ||
                        prodResultHandleT.getReturnContent() == null) {
                    continue;
                }
                goodsProduct = prodResultHandleT.getReturnContent().getProdProduct();

                long packCategoryId = goodsProduct.getBizCategoryId();
                //机酒打包、关联了跟团游、当地游
                if (packCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId()
                        || packCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId()) {
                    isSaleOut = true;
                    break;
                }

                //机酒打包的景点门票、其他票，提示已售完
                /* reference to http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=13800
                if (packCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()
                        || packCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()) {
                    for (BuyInfo.Item item : allItemList) {
                        if (item.getGoodsId() == timeprice.getSuppGoodsId().longValue()
                                && item.getRouteRelation() != null
                                && item.getRouteRelation().equals(BuyInfo.ItemRelation.PACK)) {
                            isSaleOut = true;
                            break;
                        }
                    }
                }*/
            }

            if (isSaleOut) {
                checkAheadTimeVo.setAheadCheckMsg(GOODS_SALE_OUT);
                checkAheadTimeVo.setHasContinue(false);
            }
        }
        return handle;
    }


    /**
     * 获取BUYINFO中所有商品的GOODS时间价格表
     *
     * @param buyInfo
     * @return
     */
    private List<SuppGoodsBaseTimePrice> getGoodsTimePrices(BuyInfo buyInfo) {
        //获取itemMap中的值，放入itemlist
        if (MapUtils.isNotEmpty(buyInfo.getItemMap())) {
            buyInfo.setItemList(new ArrayList<>(buyInfo.getItemMap().values()));
            buyInfo.getItemMap().clear();
        }

        //转换ProductMap，放入itemlist
        if (MapUtils.isNotEmpty(buyInfo.getProductMap())) {
            buyInfo.setProductList(new ArrayList<>(buyInfo.getProductMap().values()));
            buyInfo.getProductMap().clear();
        }

        //获取所有的item
        List<BuyInfo.Item> allItemList = getItems(buyInfo);

        //构造timeprice查询参数
        List<SuppGoodsBaseTimePrice> timepriceList = new ArrayList<>();
        for (BuyInfo.Item item : allItemList) {
            Long goodsId = item.getGoodsId();
            Date specDate = item.getVisitTimeDate();
            if (null != goodsId && null != specDate) {
                SuppGoodsAddTimePrice timePrice = new SuppGoodsAddTimePrice();
                timePrice.setSuppGoodsId(goodsId);
                timePrice.setSpecDate(specDate);
                timepriceList.add(timePrice);
            }
        }

        ResultHandleT<List<SuppGoodsBaseTimePrice>> handle = suppGoodsTimePriceClientService
                .getBaseTimePriceList(timepriceList);
        if (handle != null && handle.isSuccess() && handle.getReturnContent() != null) {
            timepriceList = handle.getReturnContent();
        } else {
            timepriceList.clear(); //获取不到，则直接清空，不返回错误信息，保证用户可以下单；
        }
        return timepriceList;
    }

    /**
     * 获取所有的 ITEMLIST
     *
     * @param buyInfo
     * @return
     */
    private List<BuyInfo.Item> getItems(BuyInfo buyInfo) {
        List<BuyInfo.Item> allItemList = new ArrayList<>();

        List<BuyInfo.Item> itemList = buyInfo.getItemList();
        if (CollectionUtils.isNotEmpty(itemList)) {
            allItemList.addAll(itemList);
        }

        List<BuyInfo.Product> productList = buyInfo.getProductList();
        if (CollectionUtils.isNotEmpty(productList)) {
            for (BuyInfo.Product product : productList) {
                List<BuyInfo.Item> proItems = product.getItemList();
                if (CollectionUtils.isNotEmpty(proItems)) {
                    allItemList.addAll(proItems);
                }
            }
        }
        return allItemList;
    }

    /**
     * 检测是否需要检测提前预定时间
     *
     * @param buyInfo
     * @return
     */
    private boolean isCheckAheadTime(ProdProduct prodProduct) {
        boolean isCheck = false;

        String packageType = prodProduct.getPackageType();
        //检查打包类型，只有自主打包才检查
        if (StringUtils.equalsIgnoreCase(ProdProduct.PACKAGETYPE.LVMAMA.name(), packageType)) {
            String productType = prodProduct.getProductType();
            //检查产品类型，只有国内产品才检查
            if (StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERLONGLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNER_BORDER_LINE.name(), productType)) {
                long categoryId = prodProduct.getBizCategoryId();
                //校验品类：只有跟团游、自由行才检查
                if (categoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId() ||
                        categoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId()) {
                    isCheck = true;
                }
            }
        }

        return isCheck;
    }

    /**
     * 向提前预定时间中增加新的提前预定时间
     *
     * @param aheadTime 提前毫秒数
     * @param date
     * @return
     */
    public Date addAheadTime(Date date, long aheadTime) {
        long time = date.getTime() - aheadTime;
        return new Date(time);
    }

    /**
     * 根据产品Id获取产品
     *
     * @param productId
     * @return
     */
    private ProdProduct getProduct(Long productId) {
        ProdProduct prodProduct = null;
        try {
            ProdProductParam param = new ProdProductParam();
            param.setBizCategory(false);
            ResultHandleT<ProdProduct> prodResultHandleT = prodProductClientService.findProdProductById(productId, param);
            if (prodResultHandleT != null && prodResultHandleT.isSuccess() && prodResultHandleT.getReturnContent() != null) {
                prodProduct = prodResultHandleT.getReturnContent();
            }
        } catch (Exception e) {
            logger.error("AjaxBookQueryAction#getProduct error \n", e);
        }
        return prodProduct;
    }
}
