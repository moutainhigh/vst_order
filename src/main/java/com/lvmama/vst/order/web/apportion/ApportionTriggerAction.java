package com.lvmama.vst.order.web.apportion;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.BatchApportionOutcome;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.po.OrderApportionInfoPO;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.service.apportion.job.OrderApportionJob;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import com.lvmama.vst.order.web.service.ApportionTriggerService;
import com.lvmama.vst.order.web.vo.OrderApportionMsgVO;
import com.lvmama.vst.order.web.vo.OrderApportionVO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/5/5.
 */
@Controller
public class ApportionTriggerAction extends BaseActionSupport {
  
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(ApportionTriggerAction.class);

    @Resource
    private ApportionInfoQueryService apportionInfoQueryService;

    @Resource
    private ApportionDataAssist apportionDataAssist;

    @Resource
    private ApportionTriggerService apportionTriggerService;
    @Resource
    private OrderApportionDepotService orderApportionDepotService;
    @Resource
    private OrderApportionJob orderApportionJob;

    @RequestMapping(value = "/apportion/queryApportion")
    public String queryApportion(Model model, String orderId, String orderItemId){
        Long orderIdLong = StringUtils.isBlank(orderId) ? 0L : Long.valueOf(orderId);
        Long orderItemIdLong = StringUtils.isBlank(orderItemId) ? 0L : Long.valueOf(orderItemId);
        OrderApportionInfoQueryVO orderApportionInfoQueryVO = new OrderApportionInfoQueryVO();
        if (NumberUtils.isAboveZero(orderIdLong)){
            orderApportionInfoQueryVO.setOrderId(orderIdLong);
        }
        if (NumberUtils.isAboveZero(orderItemIdLong)) {
            orderApportionInfoQueryVO.setOrderItemId(orderItemIdLong);
        }
        try {
            OrderApportionInfoPO orderApportionInfoPO = apportionInfoQueryService.calculateOrderApportionInfo(orderApportionInfoQueryVO);
            model.addAttribute("resultMsg", "孙赛刚大神说，查询这么简单的事，还要来麻烦他，真是一脸不悦。" + GsonUtils.toJson(orderApportionInfoPO));
        } catch (Exception e) {
            log.error("Error query apportion info for order " + orderId + ", item " + orderItemId, e);
            model.addAttribute("resultMsg", e.getMessage());
        }
        return "/order/apportion/apportionResult";
    }

    @RequestMapping(value = "/apportion/apportionAmount")
    public String apportionAmount(Model model, OrderApportionVO orderApportionVO) {
        Long orderId = orderApportionVO.getOrderId();
        log.info("小贱人，你又来调用我们的分摊全部服务了, 现在就开始，order id is " + orderId);
        if(ApportionUtil.isLvmmOrderApportion()) {
            model.addAttribute("resultMsg", "订单分摊已迁移至订单分摊子系统。请使用订单分摊子系统的补偿连接");
            log.info("LvmmOrderApportion is enabled,please use lvmm-order-apportion project, orderId is " + orderId);
            return "/order/apportion/apportionResult";
        }
        try {
            apportionTriggerService.apportionAndSaveFullAmount(orderApportionVO);
            log.info("小贱人，你调用我们的分摊全部服务，已经结束，滚去看看数据库吧，订单号是:" + orderId);
            model.addAttribute("resultMsg", "孙赛刚大神说, order id " + orderId + ", 优惠、促销、渠道优惠、手工改价、实付都已分摊，损耗了不少元气，暂时别来找他分摊了");
        } catch (Exception e) {
            log.info("小贱人，你调用我们的分摊全部服务，出错了，滚去排查吧吧，订单号是:" + orderId, e);
            model.addAttribute("resultMsg", "孙赛刚大神说, order id " + orderId + ",分摊全部出错了，错误信息是 " + e.getMessage());
        }
        return "/order/apportion/apportionResult";
    }

    @RequestMapping(value = "/apportion/doTask")
    public String doTask(Model model){
        log.info("小贱人，你又来调用我们的分摊job服务了, 现在就开始");
        try {
            orderApportionJob.execute(null, null);
            log.info("小贱人，你调用我们的分摊job服务，已经结束，滚去看看数据库吧");
            model.addAttribute("resultMsg", "孙赛刚大神说,分摊job已经触发，其它与自己无关");
        } catch (Exception e) {
            log.info("小贱人，你调用我们的分摊job服务，出错了，滚去排查吧吧", e);
            model.addAttribute("resultMsg", "孙赛刚大神说, 分摊job出错了，错误信息是 " + e.getMessage());
        }
        return "/order/apportion/apportionResult";
    }


    /**
     * 作废以前的分摊记录
     */
    @RequestMapping(value = "/apportion/invalidPreviousApportion")
    public String invalidPreviousApportion(Model model, String orderId) {
        log.info("小贱人，你又来调用我们的作废分摊记录服务了, 现在就开始，order id is " + orderId);
        Long orderIdLong = Long.valueOf(orderId);
        ApportionQueryVO apportionQueryVO = new ApportionQueryVO();
        apportionQueryVO.setOrderId(orderIdLong);
        apportionDataAssist.invalidOrderApportionData(apportionQueryVO);
        log.info("小贱人，你调用我们的作废分摊记录服务，已经结束，滚去看看数据库吧，订单号是:" + orderId);
        model.addAttribute("resultMsg", "孙赛刚大神说, order id " + orderId + ", 作废分摊记录已完成，这种小事以后别来找他了");
        return "/order/apportion/apportionResult";
    }

    /**
     * 去掉缓存的值
     * */
    @RequestMapping(value = "/apportion/invalidCachedValue")
    public String invalidMemoryCachedValue(Model model, String key) {
        log.info("小贱人，你又来调用我们的作废缓存服务了, 现在就开始，key is " + key);
        MemcachedUtil.getInstance().remove(key);
        log.info("小贱人，你调用我们的作废缓存服务，已经结束，key:" + key);
        model.addAttribute("resultMsg", "孙赛刚大神说, key " + key + ", 缓存已作废，这种小事以后别来找他了");
        return "/order/apportion/apportionResult";
    }

    /**
     * 查看缓存的值
     * */
    @RequestMapping(value = "/apportion/getCachedValue")
    public String getMemoryCachedValue(Model model, String key) {
        log.info("小贱人，你又来调用我们的查看缓存服务了, 现在就开始，key is " + key);
        String memoryCachedValue = MemcachedUtil.getInstance().get(key);
        log.info("小贱人，你调用我们的查看缓存服务，已经结束，value:" + memoryCachedValue);
        model.addAttribute("resultMsg", "孙赛刚大神说, key " + key + ", 缓存的值是" + memoryCachedValue + "，这种小事以后别来找他了");
        return "/order/apportion/apportionResult";
    }

    /**
     * 发送消息
     * */
    @RequestMapping(value = "/apportion/sendMsg")
    public String sendMessage(Model model, OrderApportionMsgVO orderApportionMsgVO){
        String msgJson = GsonUtils.toJson(orderApportionMsgVO);
        log.info("小贱人，你又来调用我们的发送消息服务了, 现在就开始，参数: " + msgJson);
        apportionTriggerService.sendOrderMsg(orderApportionMsgVO);
        log.info("小贱人，你又来调用我们的发送消息服务了，已经结束，参数:" + msgJson);
        model.addAttribute("resultMsg", "孙赛刚大神说, 消息 " + msgJson + ", 已经触发, 这种小事以后别来找他了");
        return "/order/apportion/apportionResult";
    }

    @RequestMapping(value = "/apportion/triggerTest")
    public String triggerTest(Model model){

        BatchApportionOutcome batchApportionOutcome = new BatchApportionOutcome();
        //无用的订单集合
        List<Long> futileOrderIdList = new ArrayList<>();
        for (long i = -1; i > -10; i--) {
            futileOrderIdList.add(i);
        }
        batchApportionOutcome.setFutileOrderApportionIdList(futileOrderIdList);
        //实付分摊已完成的订单
        List<Long> paymentApportionCompletedOrderIdList = new ArrayList<>();
        for (long i = -11; i > -21; i--) {
            paymentApportionCompletedOrderIdList.add(i);
        }
        batchApportionOutcome.setSuccessOrderApportionIdList(paymentApportionCompletedOrderIdList);
        //下个批次中分摊的订单
        List<Long> waitForNextBatchOrderIdList = new ArrayList<>();
        for (long i = -21; i > -31; i--) {
            waitForNextBatchOrderIdList.add(i);
        }
        batchApportionOutcome.setWaitForNextBatchOrderApportionIdList(waitForNextBatchOrderIdList);
        //下单项已分摊的订单
        List<Long> bookingApportionCompletedOrderIdList = new ArrayList<>();
        for (long i = -31; i > -41; i--) {
            bookingApportionCompletedOrderIdList.add(i);
        }
        batchApportionOutcome.setBookingApportionSucceedOrderApportionIdList(bookingApportionCompletedOrderIdList);
        //分摊失败的订单
        List<OrderApportionDepot> failedOrderIdList = new ArrayList<>();
        for (long i = -41; i > -51; i--) {
            OrderApportionDepot orderApportionDepot = new OrderApportionDepot();
            orderApportionDepot.setOrderApportionId(i);
            orderApportionDepot.setOrderId(i);
            failedOrderIdList.add(orderApportionDepot);
        }
        batchApportionOutcome.setFailedOrderDepotList(failedOrderIdList);
        orderApportionDepotService.batchUpdateApportionOutcome(batchApportionOutcome);
        model.addAttribute("resultMsg", "孙赛刚大神说,测试已触发");

        return "/order/apportion/apportionResult";
    }
}
