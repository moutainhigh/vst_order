package com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsGroupVO;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsVo;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.neworder.order.create.persistance.category.IOrderDbStoreFactory;
import com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory.product.IOrderStoreProduct;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dengcheng on 17/2/22.
 */
@Service("hotelCombDbStroeFactory")
public class HotelCombDbStroeFactory implements IOrderDbStoreFactory {

    @Resource
    IOrderStoreProduct storeProductService;

    @Resource
    protected IComplexQueryService complexQueryService;

    @Autowired
    private OrderSaveService orderSaveService;

    @Override
    @Transactional
    public OrdOrder persistanceOrder(OrdOrderDTO order) {
        storeProductService.orderHeaderDbStore(order);
        storeProductService.orderItemDbStore(order);
        storeProductService.ordePromotionDbStore(order);
        storeProductService.ordTravelContractDbStore(order);
        storeProductService.orderItemTravelDbStore(order);
        // ORD_ORDER_AMOUNT_ITEM表数据重复插入，促销信息展示错误
        // storeProductService.orderAmountTravelDbStore(order);
        OrdOrder ordorder = complexQueryService.queryOrderByOrderId(order.getOrderId());
        return ordorder;
    }

    @Override
    public void setOrderNormal(OrdOrderDTO order) {
        orderSaveService.resetOrderToNormal(order.getOrderId());
    }
}
