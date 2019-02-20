package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.service.apportion.ApportionOrderDataPrepareService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionCalculateService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionSaveService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.utils.ApportionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhouyanqun on 2017/4/25.
 */
@Service
public class OrderAmountApportionServiceImpl implements OrderAmountApportionService {
    private static final Log log = LogFactory.getLog(OrderAmountApportionServiceImpl.class);
    //分摊数据准备服务
    @Resource
    private ApportionOrderDataPrepareService apportionOrderDataPrepareService;
    //分摊计算服务
    @Resource
    private OrderAmountApportionCalculateService orderAmountApportionCalculateService;
    //分摊保存服务
    @Resource
    private OrderAmountApportionSaveService orderAmountApportionSaveService;
    //分摊仓库服务
    @Resource
    private OrderApportionDepotService orderApportionDepotService;

    /**
     * 计算并且保存订单的分摊信息
     *
     * @param orderId
     */
    @Override
    public void calcAndSaveBookingApportionAmount(Long orderId) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, please check, orderId is " + orderId);
            return;
        }
        
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }

        if(NumberUtils.equalsOrBelowZero(orderId)) {
            return;
        }

        OrdOrder order = apportionOrderDataPrepareService.prepareApportionDataForBookingApportion(orderId);
        orderAmountApportionCalculateService.apportionOrderAmount(order);
        orderAmountApportionSaveService.saveAllApportion(order);
    }

    /**
     * 计算并且保存订单的分摊信息，分摊优惠、促销、渠道优惠信息
     * 分摊的金额，以及，多价格、入住记录等信息必须已经提前准备好
     *
     */
    public void calcAndSaveBookingApportionAmount(OrdOrder order) {
        if(order == null) {
            return;
        }
        Long orderId = order.getOrderId();
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, please check, order is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }

        if(NumberUtils.equalsOrBelowZero(orderId)) {
            return;
        }
        orderAmountApportionCalculateService.apportionOrderAmount(order);
        orderAmountApportionSaveService.saveAllApportion(order);
    }

    /**
     * 分摊并保存订单的实付金额
     *
     * @param orderId
     */
    @Override
    public void apportionAndSaveActualPaidAmount(Long orderId) {

        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check, orderId is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }
        //计算分摊信息
        OrdOrder orderWithApportionInfo = orderAmountApportionCalculateService.apportionActualPaidAmount(orderId);
        //保存分摊信息
        orderAmountApportionSaveService.savePaymentApportion(orderWithApportionInfo);
    }

    /**
     * 分摊并且保存订单的实付金额
     *
     * @param order
     */
    public void apportionAndSaveActualPaidAmount(OrdOrder order) {
        if (order == null) {
            return;
        }
        Long orderId = order.getOrderId();
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check, orderId is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }
        //计算分摊信息
        orderAmountApportionCalculateService.apportionActualPaidAmount(order);
        //保存分摊信息
        orderAmountApportionSaveService.savePaymentApportion(order);
    }

    /**
     * 分摊并且保存订单的手工改价分摊信息
     *
     * @param orderId
     */
    @Override
    public void apportionAndSaveManualChangeAmount(Long orderId, OrdAmountChange ordAmountChange) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled please check, orderId is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }
        OrdOrder order = orderAmountApportionCalculateService.apportionOrderManualAmount(orderId, ordAmountChange);
        //保存手工分摊信息
        orderAmountApportionSaveService.saveAllApportion(order);
    }

    /**
     * 分摊并且保存订单所有的手工改价分摊信息
     *
     * @param orderId
     */
    @Override
    public void apportionAndSaveFullManualChangeAmount(Long orderId) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, please check, orderId is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }
        //计算分摊信息
        OrdOrder order = orderAmountApportionCalculateService.apportionFullManualAmount(orderId);
        //保存分摊信息
        orderAmountApportionSaveService.saveAllApportion(order);
    }

    /**
     * 添加订单到分摊仓库表，等待分摊
     *
     * @param orderId 将要添加的订单id
     */
    @Override
    public void addToOrderApportionDepot(Long orderId) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check, orderId is " + orderId);
            return;
        }
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return;
        }
        if (NumberUtils.isNotAboveZero(orderId)) {
            return;
        }
        OrderApportionDepot orderApportionDepot = new OrderApportionDepot();
        orderApportionDepot.setOrderId(orderId);
        orderApportionDepot.setValidFlag(Constants.Y_FLAG);
        Date currentDateTime = Calendar.getInstance().getTime();
        orderApportionDepot.setCreateTime(currentDateTime);
        orderApportionDepot.setUpdateTime(currentDateTime);
        orderApportionDepotService.addOrderApportionDepot(orderApportionDepot);
    }
}
