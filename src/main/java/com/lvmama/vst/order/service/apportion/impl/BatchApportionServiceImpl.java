package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.comm.vo.PageConst;
import com.lvmama.vst.comm.vo.page.Page;
import com.lvmama.vst.order.constant.ApportionConstants;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.service.apportion.BatchApportionService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.service.apportion.category.OrderManualChangeApportionPerformer;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import com.lvmama.vst.order.vo.OrderApportionDepotBatchVO;
import com.lvmama.vst.order.vo.OrderApportionDepotListVO;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/7/3.
 */
@Component
public class BatchApportionServiceImpl implements BatchApportionService {

    private static final Log log = LogFactory.getLog(BatchApportionServiceImpl.class);
    @Resource
    private OrderApportionDepotService orderApportionDepotService;
    @Resource
    private OrderAmountApportionService orderAmountApportionService;
    @Resource
    private ApportionDataAssist apportionDataAssist;
    @Resource
    private IOrdOrderService ordOrderService;
    @Resource(name="orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;
    @Resource
    private OrderManualChangeApportionPerformer orderManualChangeApportionPerformer;

    /**
     * 批次分摊
     */
    @Override
    public void batchApportionOrders() {

        String uuid = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check, batch no is " + uuid);
            return;
        }
        //订单分摊如果走新系统（订单分摊子系统lvmm_order_apportion）
        if(ApportionUtil.isLvmmOrderApportion()) {
            log.error("LvmmOrderApportion is  enabled, please check, batch no is " + uuid);
            return;
        }
        
        log.info("Now apportion job running, batch id is " + uuid);
        Map<String, Object> paramMap = new HashMap<>();
        //总记录数
        Long recordCount = orderApportionDepotService.queryRecordCount(paramMap);
        if (NumberUtils.isNotAboveZero(recordCount)) {
            log.info("Order depot record count is not above zero, will return, batch no is " + uuid);
            return;
        }

        Page page = Page.page(ApportionConstants.maxPageSize, recordCount);
        long pageCount = page.getPageCount();
        if (pageCount <= 0) {
            log.info("Page count is below 0, will do nothing, batch no is " + uuid);
            return;
        }
        log.info("Page info is " + GsonUtils.toJson(page) + ", batch no is " + uuid);
        BatchApportionOutcome allBatchApportionOutcome = new BatchApportionOutcome();
        for (long pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            page.setCurrentPage(pageNumber);
            log.info("Now begin to apportion by page, total record count is " + recordCount
                    + ", page size is " + ApportionConstants.maxPageSize
                    + ", page no is " + pageNumber + ", batch no is " + uuid);
            try {
                BatchApportionOutcome batchApportionOutcome = this.apportionCurrentPage(page, uuid);
                allBatchApportionOutcome.union(batchApportionOutcome);
            } catch (Exception e) {
                log.error("Error apportion for batch " + uuid + ",page " + GsonUtils.toJson(page), e);
                continue;
            }
            log.info("Completed apportion of current page, total record count is " + recordCount
                    + ", page size is " + ApportionConstants.maxPageSize
                    + ", page no is " + pageNumber + ", batch no is " + uuid);
            //把本页中的分摊结果记录，合并到所有的分摊集合实体
        }
        log.info("Now apportion job completed, batch id is " + uuid);
        //更新数据库中的记录
        orderApportionDepotService.batchUpdateApportionOutcome(allBatchApportionOutcome);
        log.info("Database record updated, now unlock record from cache, batch id is " + uuid);
        //解除Memory cache锁
        this.unlockIdleOrder(allBatchApportionOutcome);
        log.info("Unlock record from cache completed, batch no is " + uuid);
    }

    /**
     * 分摊当前页的数据
     * */
    private BatchApportionOutcome apportionCurrentPage(Page page, String uuid) {
        BatchApportionOutcome batchApportionOutcome = new BatchApportionOutcome();
        //分页加载分摊仓库表记录
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(PageConst.PARAM_PAGE_START_INDEX, page.getStartIndex());
        paramMap.put(PageConst.PARAM_PAGE_END_INDEX, page.getEndIndex());
        paramMap.put("waitTime", ApportionConstants.waitTime);
        List<OrderApportionDepot> apportionDepotList = orderApportionDepotService.queryOrderApportionDepotList(paramMap);
        if (CollectionUtils.isEmpty(apportionDepotList)) {
            log.info("Batch no " + uuid + " queried empty record for page " + GsonUtils.toJson(page));
            return batchApportionOutcome;
        }

        //判断其它任务是否在执行此订单的分摊，如果有，此订单在这个批次中，不执行任何操作，如果没有，给这个订单打上标签，防止其它任务再操作分摊
        log.info("Now judge whether order is executing by other task, batch no is " + uuid + ", list size is " + apportionDepotList.size());
        OrderApportionDepotBatchVO idleOrderApportionDepotBatchVO = this.selectIdleOrderList(apportionDepotList);
        //把空闲的订单号保存下来，等分摊完成后，解锁用
        batchApportionOutcome.setIdleOrderIdList(idleOrderApportionDepotBatchVO.getOrderIdList());
        log.info("Judge whether order is executing by other task completed, batch no is " + uuid + ", idle order id is " + idleOrderApportionDepotBatchVO.getOrderIdListJson() + ", now get candidate order list");
        //从空闲订单仓库记录中，筛掉不再需要分摊的记录和下单项已分摊，但尚未支付的订单记录，得到待分摊的记录
        OrderApportionDepotListVO candidateOrderApportionDepotListVO = this.selectCandidateOrderList(idleOrderApportionDepotBatchVO);
        //如果没有待分摊的订单，把其它需要更新的订单返回
        if (CollectionUtils.isEmpty(candidateOrderApportionDepotListVO.getOrderList())) {
            log.info("Candidate order list is empty. Batch no is " + uuid);
            batchApportionOutcome.setWaitForNextBatchOrderApportionIdList(candidateOrderApportionDepotListVO.getWaitForNextBatchOrderApportionIdList());
            batchApportionOutcome.setFutileOrderApportionIdList(candidateOrderApportionDepotListVO.getFutileOrderDepotIdList());
            return batchApportionOutcome;
        }

        //待分摊的仓库表记录
        Map<Long, OrderApportionDepot> candidateOrderApportionDepotMap = candidateOrderApportionDepotListVO.getOrderApportionDepotMap();
        //待分摊的订单集合
        List<OrdOrder> candidateOrderList = candidateOrderApportionDepotListVO.getOrderList();
        //待分摊的订单id集合
        List<Long> candidateOrderIdList = candidateOrderApportionDepotListVO.getOrderIdList();
        //补全多价格和入住记录
        apportionDataAssist.assignPercentCases(candidateOrderList, candidateOrderIdList);
        //补全优惠促销渠道优惠信息
        apportionDataAssist.assignBookingAmount(candidateOrderList, candidateOrderIdList);
        //包含未分摊的改价请求的订单集合
        List<OrdOrder> priceChangeOrderList = candidateOrderApportionDepotListVO.getPriceChangeOrderList();
        //包含未分摊的改价请求的订单id集合
        List<Long> priceChangeOrderIdList = candidateOrderApportionDepotListVO.getPriceChangeOrderIdList();
        if (CollectionUtils.isNotEmpty(priceChangeOrderIdList)) {
            //作废包含未分摊的改价请求的订单集合，对应的分摊信息
            ApportionQueryVO apportionQueryVO = new ApportionQueryVO();
            apportionQueryVO.setOrderIdList(priceChangeOrderIdList);
            apportionDataAssist.invalidOrderApportionData(apportionQueryVO);
        }
        //查询所有主单上的改价记录
        Map<Long, List<OrdAmountChange>> ordAmountChangeMap = apportionDataAssist.catchPriceChangeAmountMap(priceChangeOrderList, priceChangeOrderIdList);

        //下单项成功分摊的订单id集合
        List<Long> bookingApportionCompletedOrderDepotIdList = new ArrayList<>();
        //实付已分摊的订单id集合
        List<Long> paymentApportionCompletedOrderApportionIdList = new ArrayList<>();
        //下单项在其它任务中已经分摊，并且已经支付的订单集合、订单id集合
        List<OrdOrder> bookingApportionAheadDoneAndPaidOrderList = new ArrayList<>();
        List<Long> bookingApportionAheadDoneAndPaidOrderIdList = new ArrayList<>();
        //下单项在本批次中分摊的，并且已经支付的订单集合、订单id集合,需要补充手工改价的分摊数据，然后进行实付分摊
        List<OrdOrder> bookingApportionDoneAndPaidOrderList = new ArrayList<>();
        List<Long> bookingApportionDoneAndPaidOrderIdList = new ArrayList<>();
        //本批次中改价分摊了，而且支付的订单(无须再补充改价分摊信息)
        List<OrdOrder> priceChangePaidOrderList = new ArrayList<>();
        //失败的订单仓库记录
        List<OrderApportionDepot> failedOrderDepotList = new ArrayList<>();
        for (OrdOrder order : candidateOrderList) {
            Long orderId = order.getOrderId();
            log.info("Executing apportion for order " + orderId + " in job");
            OrderApportionDepot orderApportionDepot = candidateOrderApportionDepotMap.get(orderId);
            Long orderApportionId = orderApportionDepot.getOrderApportionId();
            try {
                //如果分摊来源是订单改价，要重新分摊手工改价金额
                String apportionOrigin = orderApportionDepot.getApportionOrigin();
                //是否需要在本批次中分摊手工改价的订单，如果订单在手工改价时，被其它任务抢先占据分摊锁了，就需要在批次中分摊手工改价
                boolean isPriceChangeOrder = StringUtils.equals(apportionOrigin, OrderEnum.APPORTION_ORIGIN.apportion_origin_price_change.getApportionOriginName());
                if (isPriceChangeOrder) {
                    List<OrdAmountChange> ordAmountChanges = ordAmountChangeMap.get(orderId);
                    List<OrdOrderItem> orderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());
                    orderManualChangeApportionPerformer.reDoManualChangeApportion(order, orderItemList, ordAmountChanges);
                    //让下单项可以重新分摊
                    orderApportionDepot.setApportionStatus(null);
                }
                String apportionStatus = orderApportionDepot.getApportionStatus();
                String paymentStatus = order.getPaymentStatus();
                if (apportionStatus == null) {
                    log.info("Order " + orderId + ", has not apportioned yet, now doing");
                    //下单项未分摊的订单，进行下单项分摊
                    orderAmountApportionService.calcAndSaveBookingApportionAmount(order);
                    log.info("Order " + orderId + ", booking apportion completed, payment status is " + paymentStatus);
                    if (StringUtils.equals(paymentStatus, OrderEnum.PAYMENT_STATUS.PAYED.getCode())) {
                        if (isPriceChangeOrder) {
                            priceChangePaidOrderList.add(order);
                        } else {
                            //已经支付的订单，加入集合，准备补充手工改价的分摊信息，然后进行实付分摊
                            bookingApportionDoneAndPaidOrderList.add(order);
                            bookingApportionDoneAndPaidOrderIdList.add(orderId);
                        }
                    } else {
                        //下单项在本批次中分摊，但尚未支付的订单，加入下单项分摊成功列表
                        bookingApportionCompletedOrderDepotIdList.add(orderApportionId);
                    }
                }
                if (StringUtils.equals(apportionStatus, OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name())
                        && StringUtils.equals(paymentStatus, OrderEnum.PAYMENT_STATUS.PAYED.getCode())) {
                    //下单项在其它批次中已分摊，且已支付的订单，加入对应列表，准备先补充数据，然后执行分摊
                    bookingApportionAheadDoneAndPaidOrderList.add(order);
                    bookingApportionAheadDoneAndPaidOrderIdList.add(orderId);
                }
                log.info("Executing apportion for order " + orderId + " in job completed");
            } catch (Exception e) {
                log.error("Order " + orderId + " have meet some trouble while apportion, batch id is " + uuid, e);
                //分摊失败的订单，准备把分摊标识设定为下单项分摊失败，有效标识置为N
                orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_failed.name());
                orderApportionDepot.setValidFlag(Constants.N_FLAG);
                orderApportionDepot.setApportionMessage("{\"UUID\" : \"" + uuid + "\", \"errorMsg\" : \"" + e.getMessage() + "\"}");
                failedOrderDepotList.add(orderApportionDepot);
            }
        }

        log.info("Booking amount apportion completed, now prepare paid order, batch no is " + uuid);
        //所有已支付的订单集合，包括在本批次中分摊下单项的，和在其它任务中分摊下单项的
        List<OrdOrder> allPaidOrderList = new ArrayList<>();
        //添加在本批次中分摊下单项，且已支付的订单
        if (CollectionUtils.isNotEmpty(bookingApportionDoneAndPaidOrderList)) {
            apportionDataAssist.assignApportionItemOfManualChange(bookingApportionDoneAndPaidOrderList, bookingApportionDoneAndPaidOrderIdList);
            allPaidOrderList.addAll(bookingApportionDoneAndPaidOrderList);
        }
        //添加本批次中分摊下单项，已支付，并且在本批次中分摊改价的订单
        if (CollectionUtils.isNotEmpty(priceChangePaidOrderList)) {
            allPaidOrderList.addAll(priceChangeOrderList);
        }
        //添加在其它任务中分摊下单项，且已支付的订单
        if (CollectionUtils.isNotEmpty(bookingApportionAheadDoneAndPaidOrderList)) {
            apportionDataAssist.assignApportionItemExceptActualPaid(bookingApportionAheadDoneAndPaidOrderList, bookingApportionAheadDoneAndPaidOrderIdList);
            allPaidOrderList.addAll(bookingApportionAheadDoneAndPaidOrderList);
        }

        log.info("Paid order prepared. Batch no is " + uuid);
        if (CollectionUtils.isNotEmpty(allPaidOrderList)) {
            for (OrdOrder order : allPaidOrderList) {
                Long orderId = order.getOrderId();
                OrderApportionDepot orderApportionDepot = candidateOrderApportionDepotMap.get(orderId);
                Long orderApportionId = orderApportionDepot.getOrderApportionId();
                try {
                    orderAmountApportionService.apportionAndSaveActualPaidAmount(order);
                    log.info("Order " + orderId + " actual paid apportion completed");
                    this.createApportionSuccessMsg(orderId);
                    paymentApportionCompletedOrderApportionIdList.add(orderApportionId);
                } catch (Exception e) {
                    log.error("Order " + orderId + " have meet some trouble while apportion actual payment, batch id is " + uuid, e);
                    //分摊失败的订单，准备把分摊标识设定为下单项分摊失败，有效标识置为N
                    orderApportionDepot.setApportionMessage("{\"UUID\" : \"" + uuid + "\", \"errorMsg\" : \"" + e.getMessage() + "\"}");
                    failedOrderDepotList.add(orderApportionDepot);
                }
            }
        }

        log.info("Completed apportioning orders which has apportioned by other task, now hand in apportion result, batch no is " + uuid);
        batchApportionOutcome.setBookingApportionSucceedOrderApportionIdList(bookingApportionCompletedOrderDepotIdList);
        batchApportionOutcome.setFailedOrderDepotList(failedOrderDepotList);
        batchApportionOutcome.setFutileOrderApportionIdList(candidateOrderApportionDepotListVO.getFutileOrderDepotIdList());
        batchApportionOutcome.setSuccessOrderApportionIdList(paymentApportionCompletedOrderApportionIdList);
        batchApportionOutcome.setWaitForNextBatchOrderApportionIdList(candidateOrderApportionDepotListVO.getWaitForNextBatchOrderApportionIdList());

        return batchApportionOutcome;
    }

    /**
     * 从空闲的订单分摊仓库记录中，挑选出待分摊的订单集合，不再需要分摊的订单，加入无用列表
     * */
    private OrderApportionDepotListVO selectCandidateOrderList(OrderApportionDepotBatchVO idleOrderApportionDepotBatchVO) {
        OrderApportionDepotListVO candidateOrderApportionDepotListVO = new OrderApportionDepotListVO();
        if (idleOrderApportionDepotBatchVO == null || CollectionUtils.isEmpty(idleOrderApportionDepotBatchVO.getOrderIdList())) {
            return candidateOrderApportionDepotListVO;
        }
        //根据空闲的订单id查询出订单集合
        List<Long> idleOrderIdList = idleOrderApportionDepotBatchVO.getOrderIdList();
        if (CollectionUtils.isEmpty(idleOrderIdList)) {
            log.info("Idle order list is empty, will do nothing");
            return candidateOrderApportionDepotListVO;
        }
        List<OrdOrder> idleOrderList = ordOrderService.getOrderList(idleOrderIdList);
        if (CollectionUtils.isEmpty(idleOrderList)) {
            log.warn("Order list catch from database is empty");
            return candidateOrderApportionDepotListVO;
        }
        //空闲的分摊仓库记录集合
        Map<Long, OrderApportionDepot> idleOrderApportionDepotMap = idleOrderApportionDepotBatchVO.getOrderApportionDepotMap();

        //无用的订单集合
        List<Long> futileOrderDepotIdList = new ArrayList<>();

        //待分摊的订单仓库记录Map
        Map<Long, OrderApportionDepot> candidateOrderApportionDepotMap = new HashMap<>();
        //待分摊的订单集合
        List<OrdOrder> candidateOrderList = new ArrayList<>();
        //待分摊的订单id集合
        List<Long> candidateOrderIdList = new ArrayList<>();
        //有价格改动的订单集合
        List<OrdOrder> priceChangeOrderList = new ArrayList<>();
        //有价格改动的订单id集合
        List<Long> priceChangeOrderIdList = new ArrayList<>();
        //下单项已经分摊，但尚未支付，而且没有过支付超时的时间的订单集合，收集起来，准备解锁，此批次中不做处理
        List<Long> waitForNextBatchOrderApportionIdList = new ArrayList<>();
        for (OrdOrder order : idleOrderList) {
            if (order == null) {
                continue;
            }
            Long orderId = order.getOrderId();
            boolean doNotNeedApportionAnyMore = ApportionUtil.doNotNeedApportionAnyMore(order);
            log.info("Now judge order " + orderId + " do not need to apportion any more, result is " + doNotNeedApportionAnyMore);
            OrderApportionDepot orderApportionDepot = idleOrderApportionDepotMap.get(orderId);
            Long orderApportionId = orderApportionDepot.getOrderApportionId();
            if (doNotNeedApportionAnyMore) {
                //如果订单不再需要分摊，加入无用列表
                futileOrderDepotIdList.add(orderApportionId);
            } else {
                String apportionStatus = orderApportionDepot.getApportionStatus();
                String paymentStatus = order.getPaymentStatus();
                if (apportionStatus == null || StringUtils.equals(paymentStatus, OrderEnum.PAYMENT_STATUS.PAYED.getCode())) {
                    //尚未分摊和已支付的订单，才会加入待分摊列表
                    candidateOrderApportionDepotMap.put(orderId, orderApportionDepot);
                    candidateOrderList.add(order);
                    candidateOrderIdList.add(orderId);
                }

                if (StringUtils.equals(apportionStatus, OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name())
                        && !StringUtils.equals(paymentStatus, OrderEnum.PAYMENT_STATUS.PAYED.getCode())) {
                    //下单项已经分摊过，但尚未支付的订单，加入等待下次分摊列表，准备解锁。
                    waitForNextBatchOrderApportionIdList.add(orderApportionId);
                }

                //分摊来源，如果来源于订单改价，则加入特定的列表
                String apportionOrigin = orderApportionDepot.getApportionOrigin();
                if (StringUtils.equals(apportionOrigin, OrderEnum.APPORTION_ORIGIN.apportion_origin_price_change.getApportionOriginName())) {
                    priceChangeOrderList.add(order);
                    priceChangeOrderIdList.add(orderId);
                }
            }
        }
        String candidateOrderIdListJson = GsonUtils.toJson(candidateOrderIdList);
        log.info("Candidate order list selected, result is " + candidateOrderIdListJson);
        candidateOrderApportionDepotListVO.setFutileOrderDepotIdList(futileOrderDepotIdList);
        candidateOrderApportionDepotListVO.setOrderApportionDepotMap(candidateOrderApportionDepotMap);
        candidateOrderApportionDepotListVO.setOrderList(candidateOrderList);
        candidateOrderApportionDepotListVO.setOrderIdList(candidateOrderIdList);
        candidateOrderApportionDepotListVO.setWaitForNextBatchOrderApportionIdList(waitForNextBatchOrderApportionIdList);
        candidateOrderApportionDepotListVO.setOrderIdListJson(candidateOrderIdListJson);
        candidateOrderApportionDepotListVO.setPriceChangeOrderList(priceChangeOrderList);
        candidateOrderApportionDepotListVO.setPriceChangeOrderIdList(priceChangeOrderIdList);
        return candidateOrderApportionDepotListVO;
    }

    /**
     * 挑选出空闲的订单集合，空闲的也即没有正在被其它任务分摊的订单记录集合，挑选的同时，给空闲的记录加上锁，防止其它任务再操作这个记录
     * */
    private OrderApportionDepotBatchVO selectIdleOrderList(List<OrderApportionDepot> apportionDepotList) {
        OrderApportionDepotBatchVO orderApportionDepotBatchVO = new OrderApportionDepotBatchVO();
        if (CollectionUtils.isEmpty(apportionDepotList)) {
            return orderApportionDepotBatchVO;
        }
        //空闲的分摊仓库记录Map
        Map<Long, OrderApportionDepot> idleOrderApportionDepotMap = new HashMap<>();
        //空闲的分摊仓库记录的订单id集合
        List<Long> idleOrderIdList = new ArrayList<>();

        for (OrderApportionDepot orderApportionDepot : apportionDepotList) {
            if (orderApportionDepot == null) {
                continue;
            }
            Long orderId = orderApportionDepot.getOrderId();
            String memoryCacheKey = MemcachedEnum.OrderApportionExecution.getKey() + orderId;
            //缓存锁的值
            String memCachedValue = MemcachedUtil.getInstance().get(memoryCacheKey);
            //数据库锁的值
            String apportionMessage = orderApportionDepot.getApportionMessage();
            if (!StringUtils.equals(memCachedValue, Constants.Y_FLAG)
                    && !StringUtils.equals(apportionMessage, OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name())) {
                //本订单没有被其它任务分摊，先加锁，然后添加到空闲列表
                MemcachedUtil.getInstance().set(memoryCacheKey, Constants.Y_FLAG);
                idleOrderApportionDepotMap.put(orderId, orderApportionDepot);
                idleOrderIdList.add(orderApportionDepot.getOrderId());
            }
        }
        if (CollectionUtils.isEmpty(idleOrderIdList)) {
            return orderApportionDepotBatchVO;
        }
        //缓存锁已经加上，现在加上数据库标识
        String idleOrderIdListJson = GsonUtils.toJson(idleOrderIdList);
        log.info("All idle record locked by memory cache, now add database flag, idle order id list is " + idleOrderIdListJson);
        OrderApportionDepotUpdateVO orderApportionDepotUpdateVO = new OrderApportionDepotUpdateVO();
        orderApportionDepotUpdateVO.setOrderIdList(idleOrderIdList);
        orderApportionDepotUpdateVO.setApportionMessage(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name());
        orderApportionDepotUpdateVO.setUpdateTime(Calendar.getInstance().getTime());
        orderApportionDepotService.updateOrderApportionDepotList(orderApportionDepotUpdateVO);
        log.info("Add database flag completed, idle order id list is " + idleOrderIdListJson);
        orderApportionDepotBatchVO.setOrderApportionDepotMap(idleOrderApportionDepotMap);
        orderApportionDepotBatchVO.setOrderIdList(idleOrderIdList);
        orderApportionDepotBatchVO.setOrderIdListJson(idleOrderIdListJson);
        return orderApportionDepotBatchVO;
    }

    //解除Memory cache锁
    private void unlockIdleOrder(BatchApportionOutcome allBatchApportionOutcome) {
        if (allBatchApportionOutcome == null) {
            return;
        }
        //无用的，即超时未支付的订单
        List<Long> idleOrderIdList = allBatchApportionOutcome.getIdleOrderIdList();
        if (CollectionUtils.isNotEmpty(idleOrderIdList)) {
            for (Long orderId : idleOrderIdList) {
                MemcachedUtil.getInstance().remove(MemcachedEnum.OrderApportionExecution.getKey() + orderId);
            }
        }
    }

    /**
     * 创建分摊完成消息
     * @param orderId
     */
    public void createApportionSuccessMsg(Long orderId){
        log.info("createApportionSuccessMsg createMsg  ORDER_APPORTION_SUCCESS_MSG"+orderId);
        orderMessageProducer.sendMsg(MessageFactory.newOrderApportionSuccessMessage(orderId));
    }
}
