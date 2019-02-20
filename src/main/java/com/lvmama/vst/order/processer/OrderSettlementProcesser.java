/**
 *
 */
package com.lvmama.vst.order.processer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.api.order.service.VstCommOrderQueryService;
import com.lvmama.vst.api.vo.order.OrderBaseVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.supp.service.SuppContractClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettleRuleClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.service.impl.OrdPersonServiceImpl;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.pet.adapter.TntOrderQueryServiceAdapter;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.bee.po.ord.OrdRefundMentItem;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.back.supp.po.SuppSettleRule;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.EVENT_TYPE;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.dao.OrdTicketPerformDao;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.SettlementServiceAdapter;
import com.lvmama.vst.pet.adapter.refund.OrderRefundSplitServiceAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import scala.actors.threadpool.Arrays;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * 订单结算推送
 *
 * @author lancey
 */
public class OrderSettlementProcesser implements MessageProcesser {

    protected transient final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private SettlementServiceAdapter settlementServiceAdapter;

    @Autowired
    private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;

    @Autowired
    private IOrderAmountChangeService orderAmountChangeService;

    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

    @Autowired
    private SuppSupplierClientService suppSupplierClientService;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private IOrderUpdateService orderUpdateService;

    @Autowired
    private PermUserServiceAdapter permUserServiceAdapter;


    @Autowired
    private ProdProductClientService prodProductClientService;


    @Autowired
    private CategoryClientService categoryClientService;

    @Autowired
    private OrdOrderAmountItemDao ordOrderAmountItemDao;

    @Autowired
    private IOrdSettlementPriceRecordService ordSettlementPriceRecordService;

    @Autowired
    private OrdPassCodeDao ordPassCodeDao;

    // 注入分销商业务接口(订单来源、下单渠道)
    @Autowired
    private DistributorClientService distributorClientService;

    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Autowired
    private OrdPassCodeDao ordorderCodeDao;

    @Autowired
    private IOrdOrderItemService iOrdOrderItemService;

    @Autowired
    private ComJobConfigService comJobConfigService;

    @Autowired
    private IOrdOrderService iOrdOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdTicketPerformDao ordTicketPerformDao;

    @Autowired
    private OrderSettlementService orderSettlementService;

    //结算状态改造 从支付获取
    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SuppSettlementEntityClientService suppSettlementEntityClientService;

    @Autowired
    private SuppSettleRuleClientService suppSettleRuleClientService;

    @Autowired
    private OrderRefundSplitServiceAdapter orderRefundSplitServiceAdapter;

    @Autowired
    private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterService;

    @Autowired
    private SuppContractClientService suppContractClientService;

    @Autowired
    private ApportionInfoQueryService apportionInfoQueryService;

    @Autowired
    private ApiSuppOrderService apiSuppOrderService;

    @Autowired
    private TntOrderQueryServiceAdapter tntOrderQueryServiceRemote;

    @Autowired
    private VstCommOrderQueryService vstCommOrderQueryService;

    @Autowired
    private IJmsOrderMessageInfoService jmsOrderMessageInfoService;

    @Autowired
    private ISendFailedMessageInfoService sendFailedMessageInfoService;

    @Autowired
    private IOrdPersonService ordPersonService;
    @Resource
    protected IOrder2RouteService order2RouteService;

    @Resource(name="myJmsTemplate")
    private JmsTemplate myJmsTemplate;

    private static final String ORDER_ID_KEY = "ORDER_ID";
    private static final String ORDER_ITEM_ID_KEY = "ORDER_ITEM_ID";
    private final ThreadLocal<Boolean> isFirstExecuteThreadLocal = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true;
        }
    };


    public void process(final Message message) {
        logger.info("OrderSettlementProcesser,objectId=" + message.getObjectId() + ",eventType=" + message.getEventType() + ",addition=" + message.getAddition() + ",objectType=" + message.getObjectType());
        //订单消息迁移开关
        boolean msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
        //是否来自新订单系统
        boolean isFromNewOrderSys=NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType());
        logger.info("msgAndJobSwitch:"+msgAndJobSwitch+",isFromNewOrderSys:"+isFromNewOrderSys+",message:"+message);

        if(MessageUtils.isOrderPaymentMsg(message)
                || MessageUtils.isOrderResourcePassMsg(message)
                || MessageUtils.isOrderInfoPassMsg(message)
                || MessageUtils.isOrderCancelMsg(message)
                || MessageUtils.isOrderModifySettlementPriceMsg(message)
                || MessageUtils.isPasscodeApplyNotifyMsg(message)
                || MessageUtils.isItemPerformSettleMsg(message)){
            //如果消息开关打开且消息来自新系统，则消息在新系统处理，否则继续执行
            if(msgAndJobSwitch&&isFromNewOrderSys){
                return;
            }
        }


        Map<Long, Long> countSettlePriceMap = new HashMap<Long, Long>();
        /**
         * 		关于漏单补偿机制，目前问题系统出现异常，导致订单结算未完成，但是消息已丢失,导致执行不成功
         * 		目前解决办法补偿机制加上定时任务，执行时间五分钟一次，一共执行五次。如果其中有一次成功，就执行成功,for update Zhang.Wei
         */
        //保存定时任务记录数，首次接受消息才执行
        Boolean isFirstExecute = isFirstExecuteThreadLocal.get();
        saveComJobConfig(message, isFirstExecute);

        // 保存消息
        saveOrderMessageInfo(message);

           if(tpByfish(message))
               return;

        if (MessageUtils.isOrderPaymentMsg(message)
                || MessageUtils.isOrderResourcePassMsg(message)
                || MessageUtils.isOrderInfoPassMsg(message)
                || MessageUtils.isOrderApportionSuccess(message)) {
            //  支付成功、信息审核通过、资源审核通过、分摊成功 执行
            executePayedMsg(message, isFirstExecute);

        } else if (MessageUtils.isOrderCancelMsg(message)) {
            //  订单取消
            executeOrderCancelMsg(message, isFirstExecute);

        } else if (MessageUtils.isOrderRefumentMsg(message)) {
            //  订单退款
            executeOrderRefumentMsg(message, isFirstExecute, countSettlePriceMap);

        } else if (MessageUtils.isOrderModifySettlementPriceMsg(message)) {
            //  订单修改结算价
            modifySettlementPriceMsg(message, countSettlePriceMap, isFirstExecute);

        } else if (MessageUtils.isOrderItemSettleMsg(message)) {
            //  手动生成结算单
            executeOrderItemSettleMsg(message, isFirstExecute, countSettlePriceMap);

        } else if (MessageUtils.isPasscodeApplyNotifyMsg(message)) {
            //  申请通关码成功回调
            executePasscodeApplyNotifyMsg(message);

        } else if (MessageUtils.isItemPerformSettleMsg(message)) {
            //  子单履行成功
            executeItemPerformSettleMsg(message);

        } else if (MessageUtils.isOrdItemPriceConfirmStatusMsg(message)) {
            //  子订单价格状态变动
            executeItemSetPriceConfirmMsg(message);
        }

        //更新结算CountSettleAmount
        updateItemSettleCountAmount(countSettlePriceMap);
        if (isFirstExecute)//首次执行成功，删除job记录，表示不需要进行补偿机制
            deleteComJobConfigByCondition(message);
    }


    /**
     * 所有进入此Processer的消息均保存
     *
     * @param message
     */
    private void saveOrderMessageInfo(Message message) {
        try {
            JmsOrderMessageInfo messageInfo = new JmsOrderMessageInfo();
            Map<String, Long> resultMap = getOrderIdByMessage(message);
            if (null == resultMap.get(ORDER_ID_KEY)) { // 订单ID 不为NULL
                return;
            }
            messageInfo.setOrderId(resultMap.get(ORDER_ID_KEY));
            if (null != resultMap.get(ORDER_ITEM_ID_KEY)) {
                messageInfo.setOrderItemId(resultMap.get(ORDER_ITEM_ID_KEY));
            }
            messageInfo.setMessageType(message.getEventType());
            logger.info("saveOrderMessageInfo, orderId=" + messageInfo.getOrderId() + ", messageInfo json string is " + JSON.toJSONString(messageInfo));
            int result = jmsOrderMessageInfoService.saveJmsOrderMessaeInfo(messageInfo);
            if (result > 0) {
                logger.debug("saveJmsOrderMessaeInfo success,  orderId=" + messageInfo.getOrderId() + ", messageInfo json string is " + JSON.toJSONString(messageInfo));
            }
        } catch (Exception e) {
            logger.error("saveOrderMessageInfo has exception, orderId=" + message.getObjectId() + ", message json string is " + JSON.toJSONString(message));
        }

    }

    /**
     * 保存所有推送异常的消息信息
     *
     * @param message
     */
    private void saveSendFailedMessageInfo(Message message, Exception e) {
        SendFailedMessaeInfo failedMessaeInfo = new SendFailedMessaeInfo();
        Map<String, Long> resultMap = getOrderIdByMessage(message);
        if (null == resultMap.get(ORDER_ID_KEY)) { // 订单ID 不为NULL
            return;
        }
        failedMessaeInfo.setOrderId(resultMap.get(ORDER_ID_KEY));
        if (null != resultMap.get(ORDER_ITEM_ID_KEY)) {
            failedMessaeInfo.setOrderItemId(resultMap.get(ORDER_ITEM_ID_KEY));
        }
        failedMessaeInfo.setMessageType(message.getEventType());
        failedMessaeInfo.setMessageStatus(SendFailedMessaeInfo.FAILED_MESSAGE_INFO_STATUS.FAILED.name()); // 默认失败的
        failedMessaeInfo.setExceptionContent(e.getMessage());
        logger.error("saveSendFailedMessageInfo, orderId=" + failedMessaeInfo.getOrderId() + ", failedMessaeInfo json string is " + JSON.toJSONString(failedMessaeInfo));
        int result = sendFailedMessageInfoService.saveSendFailedMessaeInfo(failedMessaeInfo);
        if (result > 0) {
            logger.info("saveSendFailedMessageInfo, orderId=" + failedMessaeInfo.getOrderId() + ", failedMessaeInfo json string is " + JSON.toJSONString(failedMessaeInfo));
        }
    }

    /**
     * 根据消息中参数获取 订单ID
     *
     * @param message
     * @return
     */
    private Map<String, Long> getOrderIdByMessage(Message message) {
        Map<String, Long> orderMap = new HashMap<>();
        Long orderId = null;
        Long orderItemId = null;
        if (MessageUtils.isOrderPaymentMsg(message)
                || MessageUtils.isOrderResourcePassMsg(message)
                || MessageUtils.isOrderInfoPassMsg(message)
                || MessageUtils.isOrderApportionSuccess(message)
                || MessageUtils.isOrderCancelMsg(message)) {
            //  支付成功、信息审核通过、资源审核通过、分摊成功、订单取消
            orderId = message.getObjectId();
        } else if (MessageUtils.isOrderRefumentMsg(message)) {
            //  订单退款
            orderId = orderRefundmentServiceAdapter.queryOrdRefundmentById(message.getObjectId()).getOrderId();
        } else if (MessageUtils.isOrderModifySettlementPriceMsg(message)
                || MessageUtils.isOrderItemSettleMsg(message)
                || MessageUtils.isPasscodeApplyNotifyMsg(message)
                || MessageUtils.isItemPerformSettleMsg(message)
                || MessageUtils.isOrdItemPriceConfirmStatusMsg(message)) {
            //  订单修改结算价、手动生成结算单、申请通关码成功回调、子单履行成功、子订单价格状态变动
            orderItemId = message.getObjectId();
            orderId = orderUpdateService.getOrderItem(orderItemId).getOrderId();
        }

        if (null == orderId) {
            logger.error("can not get orderId by message, message json string is " + JSON.toJSONString(message));
        }

        orderMap.put(ORDER_ID_KEY, orderId);
        if (null != orderItemId) {
            orderMap.put(ORDER_ITEM_ID_KEY, orderItemId);
        }
        return orderMap;
    }

    public void updateItemSettleCountAmount(Map countSettlePriceMap) {
        Iterator<Long> iterator = countSettlePriceMap.keySet().iterator();
        while (iterator.hasNext()) {
            Long orderId = iterator.next();
            Long countSettleAmount = 0L;

            OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderId);

            List<OrdOrderItem> ordItemList = orderObj.getOrderItemList();
            for (OrdOrderItem ordOrderItem : ordItemList) {
                countSettleAmount += ordOrderItem.getTotalSettlementPrice();
            }

            orderSettlementService.updateSettlementItem(orderId, countSettleAmount);
        }
    }

    private boolean initItemValidate(OrdOrder order, OrdOrderItem ordOrderItem) {
        return true;
    }


    private List<SettlementItem> initItem(Long refundmentId, OrdOrder order, Map countSettlePriceMap) {
        String refundMemo = null;

        String distribuType = null;
        String orderChannel = null;
        Map<String, String> result = this.getDistribuTypeAndOrderChannelByOrderId(order);
        if (result != null) {
            distribuType = result.get("distribuType");
            orderChannel = result.get("orderChannel");
        }

        if (order.getRefundedAmount() != null && order.getRefundedAmount() > 0) {// 存在退款，查询退款明细
            StringBuffer refundMemoBuff = new StringBuffer();
            List<OrdRefundment> refundmentList = orderRefundmentServiceAdapter.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
            for (OrdRefundment or : refundmentList) {
                refundMemoBuff.append("[").append(or.getMemo()).append("] ");
            }
            refundMemo = refundMemoBuff.toString();
        }
        List<OrdRefundMentItem> oriList = orderRefundmentServiceAdapter.queryOrdRefundmentItemById(refundmentId);
        logger.info("查询退款子项表数据 OrdRefundMentItemList 大小：" + oriList.size());
        List<OrdOrderItem> metas = order.getOrderItemList();
        //List<OrdOrderItemMeta> metas = orderItemMetaDAO.selectByOrderId(order.getOrderId());
        Map<String, Long> new_settlement_map = new HashMap<String, Long>();
        Map<String, Long> new_unit_settlement_map = new HashMap<String, Long>();
        Map<Long, OrdOrderItem> orderItemMap = new HashMap<Long, OrdOrderItem>();
        for (OrdOrderItem item : metas) {
            orderItemMap.put(item.getOrderItemId(), item);
        }
        Long countSettleAmount = 0L;
        for (OrdRefundMentItem ordRefundMentItem : oriList) {
            OrdOrderItem orderItem = null;
            for (OrdOrderItem meta : metas) {
                if (meta.getOrderItemId().longValue() == ordRefundMentItem.getOrderItemMetaId().longValue()) {
                    orderItem = meta;
                }
                if (initItemValidate(order, meta)) {
                    logger.info("当前countSettleAmount：" + countSettleAmount);
                    countSettleAmount += meta.getTotalSettlementPrice();
                }
            }
            logger.info("总的countSettleAmount：" + countSettleAmount);
            countSettlePriceMap.put(order.getOrderId(), countSettleAmount);
            //OrdOrderItemMeta ooim = orderItemMetaDAO.selectByPrimaryKey(ori.getOrderItemMetaId());
            //结算价不为0（虚拟库存的结算价为0），团号为空或者是保险,或者游客损失大于0
            if (initItemValidate(order, orderItem)
                    && (!(Constant.REFUND_ITEM_TYPE.SUPPLIER_BEAR.getCode()
                    .equals(ordRefundMentItem.getType())
                    && ordRefundMentItem.getAmount() <= 0 && order.isCancel()))) {
                String key = ordRefundMentItem.getOrderItemMetaId().toString();
                Long new_settlementPrice = null;
                if (new_settlement_map.get(key) == null) {
                    Long settlementPrice = orderItem.getTotalSettlementPrice();
                    logger.info("key 为null的时候  TotalSettlementPrice：" + settlementPrice);
                    if (Constant.REFUND_ITEM_TYPE.VISITOR_LOSS.getCode().equals(ordRefundMentItem.getType())) {// 退款明细类型为游客损失
                        new_settlementPrice = ordRefundMentItem.getActualLoss();
                        logger.info("key 为null的时候  退款明细类型为游客损失：new_settlementPrice：" + new_settlementPrice);
                    } else {
                        new_settlementPrice = settlementPrice - ordRefundMentItem.getAmount();
                        logger.info("key 为null的时候  退款明细类型为 非 游客损失：new_settlementPrice：" + new_settlementPrice);
                    }
                } else {
                    Long settlementPrice = new_settlement_map.get(key);
                    logger.info("key 为 非 null的时候  settlementPrice：" + settlementPrice);
                    if (Constant.REFUND_ITEM_TYPE.VISITOR_LOSS.getCode().equals(ordRefundMentItem.getType())) {// 退款明细类型为游客损失
                        new_settlementPrice = ordRefundMentItem.getActualLoss();
                        logger.info("key 为非null的时候  退款明细类型为游客损失：new_settlementPrice：" + new_settlementPrice);
                    } else {
                        logger.info("key 为非null的时候  ordRefundMentItem.getAmount()：" + ordRefundMentItem.getAmount());
                        new_settlementPrice = settlementPrice - ordRefundMentItem.getAmount();
                        logger.info("key 为非null的时候  退款明细类型为 非 游客损失：new_settlementPrice：" + new_settlementPrice);
                    }
                }
                new_settlement_map.put(key, new_settlementPrice);
                new_unit_settlement_map.put(key, Math.round(Double.longBitsToDouble(new_settlementPrice) / Double.longBitsToDouble((orderItem.getQuantity()))));
            }
        }
        List<SettlementItem> settlementItemList = new ArrayList<SettlementItem>();

        for (Map.Entry<String, Long> entry : new_settlement_map.entrySet()) {
            logger.info(" new_settlement_map：entry.getKey()：" + entry.getKey());
            Long orderItemMetaId = Long.parseLong(entry.getKey());
            SettlementItem settlementItem = new SettlementItem();
            List<OrdRefundMentItem> refundmentItemList = orderRefundmentServiceAdapter.findOrderRefundMentItemByOrderItemMetaId(orderItemMetaId);
            if (refundmentItemList != null && refundmentItemList.size() > 0) {
                settlementItem.setOrderRefund(true);
            } else {
                settlementItem.setOrderRefund(false);
            }
            logger.info(" new_settlement_map：entry.getValue()：" + entry.getValue());
            if (entry.getValue() != null && entry.getValue() > 0) {
                settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.CANCEL.name());
            }
            if (StringUtils.isEmpty(refundMemo)) {
                refundMemo = "退款修改结算价";
            }
            //orderChannel
            settlementItem.setOrderChannel(orderChannel);
            settlementItem.setDistribuType(distribuType);
            settlementItem.setRefundMemo(refundMemo);
            settlementItem.setOrderItemMetaId(orderItemMetaId);
            settlementItem.setTotalSettlementPrice(entry.getValue());

            logger.info(" new_unit_settlement_map.get(entry.getKey())：" + new_unit_settlement_map.get(entry.getKey()));
            settlementItem.setActualSettlementPrice(new_unit_settlement_map.get(entry.getKey()));

            settlementItem.setOrderRefund(true);
            Map<String, Object> apply = getOrderAmountApply(order.getOrderId());
            settlementItem.setAdjustmentAmount(null != apply.get("amountApply") ? (Long) apply.get("amountApply") : null);
            settlementItem.setAdjustmentRemark(null != apply.get("remark") ? (String) apply.get("remark") : null);
            settlementItem.setRefundedAmount(order.getRefundedAmount());
            settlementItem.setOughtPay(order.getOughtAmount());
            settlementItem.setActualPay(order.getActualAmount());//订单实付金额（分）
            settlementItem.setUpdateRemark("退款修改结算价");//getUpdateRemark(settlementItem.getOrderItemMetaId())
            settlementItem.setCountSettleAmount(countSettleAmount);
            settlementItem.setDistributorId(order.getDistributorId());
            OrdOrderItem item = orderItemMap.get(orderItemMetaId);
            setQuantityValue(item,settlementItem);
            settlementItem.setOrderId(item.getOrderId());
            logger.info("processer2>>orderId = " + order.getOrderId() + "orderStatus = " + order.getOrderStatus());
            settlementItem.setOrderStatus(order.getOrderStatus());
            settlementItem.setOrderCreateTime(order.getCreateTime());
            settlementItem.setOrderPaymentStatus(order.getPaymentStatus());
            settlementItem.setOrderItemProdId(item.getOrderItemId());
            settlementItem.setProductId(item.getProductId());
            settlementItem.setProductName(item.getProductName());
            settlementItem.setProductType(this.getProductType(item));
            settlementItem.setMetaProductId(item.getProductId());
            settlementItem.setMetaProductName(item.getProductName());
            settlementItem.setMetaBranchId(item.getSuppGoodsId());
            settlementItem.setMetaFilialeName(order.getFilialeName());
            settlementItem.setFilialeName(order.getFilialeName());
            settlementItem.setBelongBU(item.getRealBuType());
            settlementItem.setBelongMainBU(order.getBuCode());
            settlementItem.setPriceConfirmStatus(item.getPriceConfirmStatus());
            String branchName = (String) item.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
            settlementItem.setMetaBranchName(branchName);
            settlementItem.setSupplierId(item.getSupplierId());
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contractId", item.getContractId());
            params.put("supplierId", item.getSupplierId());

            //-----组装买断相关信息-------begin--------
            if ("Y".equals(item.getBuyoutFlag())) {
                settlementItem.setBudgetTotalSettlementlPrice(entry.getValue());
                settlementItem.setBudgetUnitSettlementPrice(settlementItem.getBudgetTotalSettlementlPrice() / item.getBuyoutQuantity());
                settlementItem.setBudgetQuantity(item.getBuyoutQuantity());
                settlementItem.setBudgetFlag(item.getBuyoutFlag());
            }
            //-----组装买断相关信息-------end-----------

            logger.info("***************退款推送-总价：" + settlementItem.getTotalSettlementPrice() + ",单价：" + settlementItem.getActualSettlementPrice());
            logger.info("***************退款推送-买断总价：" + settlementItem.getBudgetTotalSettlementlPrice() + ",买断单价：" + settlementItem.getBudgetUnitSettlementPrice());


            // 保存结算对象ID, 一供多结项目
            saveSettleEntityCode(settlementItem, item, order);

            // 保存分摊信息
            saveApportionInfo(settlementItem, item, order);

            if (settlementItem.getTargetId() == null) {
                ResultHandleT<List<SuppSettleRule>> suppSettleRuleList = suppSupplierClientService.findSuppSettleRuleList(params);
                SuppSettleRule suppSettleRule = suppSettleRuleList.getReturnContent().get(0);
                settlementItem.setTargetId(suppSettleRule.getSettleRuleId());
                //结算周期
                settlementItem.setSettlementPeriod(suppSettleRule.getSettlementPeriod());
            }

            settlementItem.setProductQuantity(1L);
            settlementItem.setVisitTime(item.getVisitTime());
            settlementItem.setSettlementType(OrderEnum.SETTLEMENT_TYPE.ORDER.getCode());
            settlementItem.setBusinessName("NEW_SUPPLIER_BUSINESS");
            settlementItemList.add(settlementItem);
        }
        return settlementItemList;
    }


    public Map<String, Object> getOrderAmountApply(final Long orderId) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        return parameter;
    }

    //批量获取结算状态
    public List<SetSettlementItem> getSetSettlementItem(List<OrdOrderItem> orderItemList) {
        List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
        try {
            List<Long> itemIds = new ArrayList<Long>();
            for (OrdOrderItem ordOrderItem : orderItemList) {
                itemIds.add(ordOrderItem.getOrderItemId());
            }
            setSettlementItems = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
        } catch (Exception e) {
            throw new RuntimeException("调用支付接口获取结算状态异常---" + e.getMessage());
        }
        return setSettlementItems;
    }


    public String getSettlementStatus(List<SetSettlementItem> setSettlementItems, Long orderItemId) {
        if (null != setSettlementItems && setSettlementItems.size() > 0) {
            for (int i = 0; i < setSettlementItems.size(); i++) {
                if (setSettlementItems.get(i).getOrderItemMetaId().equals(orderItemId)) {
                    return setSettlementItems.get(i).getSettlementStatus();
                }
            }
        }
        return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
    }

    /**
     * 填充数据SettlementItem
     *
     * @param order
     * @return
     */
    private List<SettlementItem> fillSettlementItemList(OrdOrder order) {

        //查询分销
        Map<String, String> result = this.getDistribuTypeAndOrderChannelByOrderId(order);
        String distribuType = null;
        String orderChannel = null;
        if (result != null) {
            distribuType = result.get("distribuType");
            orderChannel = result.get("orderChannel");
        }

        List<SettlementItem> settlementItemList = new ArrayList<SettlementItem>();

        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        Date visitTime = null;
        Long productId = null;
        Long countSettleAmount = 0L;//整个订单结算总额（分）
        Long orderTotalSettlementPrice = 0L;
        for (OrdOrderItem ordOrderItem : orderItemList) {

            countSettleAmount += ordOrderItem.getTotalSettlementPrice();
            orderTotalSettlementPrice += ordOrderItem.getQuantity() * ordOrderItem.getSettlementPrice();
        }
        //优惠金额
        HashMap<String, Object> amountItemParams = new HashMap<String, Object>();
        amountItemParams.put("orderId", order.getOrderId());
        amountItemParams.put("itemName", OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.name());
        List<OrdOrderAmountItem> list = orderAmountChangeService.findOrderAmountItemList(amountItemParams);

        Long orderCouponAmount = 0L;
        if (list != null && list.size() > 0) {
            for (OrdOrderAmountItem ordOrderAmountItem : list) {
                orderCouponAmount = -ordOrderAmountItem.getItemAmount() + orderCouponAmount;
            }
        }

        //优惠券
        //前台下单存储了优惠券使用的新的ORDER_AMOUNT_NAME 增加此处代码  by  李志强 2015-09-28
        HashMap<String, Object> amountItemParamsForNew = new HashMap<String, Object>();
        amountItemParamsForNew.put("orderId", order.getOrderId());
        amountItemParamsForNew.put("itemName", OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_COUPON
                .name());
        List<OrdOrderAmountItem> listOrderAmount = orderAmountChangeService.findOrderAmountItemList(amountItemParamsForNew);

        if (listOrderAmount != null && listOrderAmount.size() > 0) {
            for (OrdOrderAmountItem ordOrderAmountItem : listOrderAmount) {
                //新的优惠券优惠金额为负整数 单位分
                orderCouponAmount += -ordOrderAmountItem.getItemAmount();
            }
        }
        //手机号
        order.setOrdPersonList(order.getOrdPersonList());
        OrdPerson contactPerson = order.getContactPerson();
        String contactMobileNo = null;
        if (contactPerson != null) {
            contactMobileNo = contactPerson.getMobile();
        }
        logger.info("***手机号***" + contactMobileNo);

        //支付获取结算状态
        List<SetSettlementItem> setSettlementItems = getSetSettlementItem(orderItemList);

        for (OrdOrderItem orderItem : orderItemList) {

            StringBuffer str = new StringBuffer();
            Long orderItemMetaPayedAmount = 0L;

            if (orderTotalSettlementPrice > 0) {

                //订单销售分拆后的支付金额   订单应付总金额*（当前订单子项单家*数量/所有订单子项数量*单价和）
                //Long orderItemMetaPayedAmount=order.getOughtAmount()*(orderItem.getQuantity()*orderItem.getSettlementPrice()/orderTotalSettlementPrice);
                double payedAmountPer = orderItem.getQuantity() * orderItem.getSettlementPrice() * 1.0 / orderTotalSettlementPrice * 1.0;
                orderItemMetaPayedAmount = (long) (order.getOughtAmount() * payedAmountPer);

            }


            String branchName = (String) orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());


            ResultHandleT<SuppGoods> suppGoodsHandle = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
            SuppGoods suppGoods = suppGoodsHandle.getReturnContent();
            PermUser permUser = permUserServiceAdapter.getPermUserByUserId(suppGoods.getManagerId());
            String productManager = "";
            if (permUser != null && StringUtils.isNotEmpty(permUser.getUserName())) {
                productManager = permUser.getUserName();
            }


            SettlementItem setSettlementItem = new SettlementItem();

            //订单出发日期
            if (order != null && order.getVisitTime() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                visitTime = order.getVisitTime();
                String dateString = formatter.format(visitTime);
                str.append(dateString);
                str.append("-");
            }
            //产品ID
            if (order.getProductId() != null) {
                productId = order.getProductId();
                str.append(productId);
            }
            logger.info("----结算团号----" + str.toString() + "----出团日期----" + visitTime + "----productName----:" + order.getProductName());

            //设置分销的DISTRIBU_TYPE 和 OERDER_CHANNEL
            setSettlementItem.setDistribuType(distribuType);
            setSettlementItem.setOrderChannel(orderChannel);

            setSettlementItem.setContactMobileNo(contactMobileNo);
            setSettlementItem.setOrderId(order.getOrderId());
            setSettlementItem.setTravelGroupCode(str.toString()); // 结算团号
            setSettlementItem.setOutGroupDate(visitTime); //出团日期
            setSettlementItem.setActChild(orderItem.getChildQuantity());
            setSettlementItem.setActAdult(orderItem.getAdultQuantity());
            logger.info("processer3>>orderId = " + order.getOrderId() + "orderStatus = " + order.getOrderStatus());
            setSettlementItem.setOrderStatus(order.getOrderStatus());
            setSettlementItem.setOrderPaymentTime(order.getPaymentTime());
            setSettlementItem.setOrderCreateTime(order.getCreateTime());
            setSettlementItem.setOrderPaymentStatus(order.getPaymentStatus());
            setSettlementItem.setDistributorId(order.getDistributorId());
            if (order.getContactPerson() != null && StringUtils.isNotBlank(order.getContactPerson().getFullName())) {
                setSettlementItem.setOrderContactPerson(order.getContactPerson().getFullName());
            } else {
                setSettlementItem.setOrderContactPerson(order.getContactPerson().getMobile());
            }
            // 将游玩人推送给结算系统
            List<OrdPerson> travellerList = order.getOrdTravellerList();
            if (CollectionUtils.isNotEmpty(travellerList)) {
                //产品经理lvhao要求只拿第一个游玩人的名字
                String travellerName = travellerList.get(0).getFullName();
                setSettlementItem.setTravelingPerson(travellerName);
            }

            setSettlementItem.setOrderCouponAmount(orderCouponAmount);//订单的优惠券金额
            setSettlementItem.setOrderRefund(Constants.ORDER_REFUND_FALSE);//是否订单有退款 0.没有 1.有退款
            setSettlementItem.setOrderItemProdId(orderItem.getOrderItemId());//订单子项ID
            setSettlementItem.setProductId(order.getProductId());
            setSettlementItem.setProductName(order.getProductName());
            setSettlementItem.setProductType(this.getProductType(orderItem));//销售产品类型  orderItem.getCategoryId()+""
            setSettlementItem.setProductBranchId(suppGoods.getSuppGoodsId());
            setSettlementItem.setProductBranchName(suppGoods.getGoodsName());
            setSettlementItem.setProductPrice(orderItem.getPrice());
            setSettlementItem.setFilialeName(order.getFilialeName());
            setSettlementItem.setBelongBU(orderItem.getRealBuType());
            setSettlementItem.setBelongMainBU(order.getBuCode());
            setSettlementItem.setMetaFilialeName(order.getFilialeName());
            setSettlementItem.setOrderItemMetaId(orderItem.getOrderItemId());//订单子子项ID
            setSettlementItem.setOrderItemMetaPayedAmount(orderItemMetaPayedAmount);//订单销售分拆后的支付金额
            setSettlementItem.setMetaProductId(orderItem.getProductId());//采购产品ID
            setSettlementItem.setMetaProductName(orderItem.getProductName());//采购产品名称
            setSettlementItem.setMetaBranchId(suppGoods.getSuppGoodsId());//采购产品分类ID  BRANCH_ID

            setSettlementItem.setMetaBranchName(suppGoods.getGoodsName());//采购产品分类名称
            setSettlementItem.setMetaProductManager(productManager);
            setSettlementItem.setSettlementPrice(orderItem.getSettlementPrice());
            setSettlementItem.setSupplierId(orderItem.getSupplierId());

            // 结算子订单，公司主体
            setSettlementItem.setCompanyType(orderItem.getCompanyType() == null ? "XINGLV" : orderItem.getCompanyType());
            setSettlementItem.setCompanyId(orderItem.getCompanyType() == null ? "XINGLV" : orderItem.getCompanyType());

            // 保存结算对象ID, 一供多结项目
            saveSettleEntityCode(setSettlementItem, orderItem, order);

            // 保存分摊信息
            saveApportionInfo(setSettlementItem, orderItem, order);

            setSettlementItem.setProductQuantity(1L);//打包数量

            Long quantity = orderItem.getQuantity();
            setQuantityValue(orderItem,setSettlementItem);
            setSettlementItem.setVisitTime(order.getVisitTime());


            setSettlementItem.setSettlementStatus(getSettlementStatus(setSettlementItems, orderItem.getOrderItemId()));
            setSettlementItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice());//子项结束总额
            setSettlementItem.setSettlementType(OrderEnum.SETTLEMENT_TYPE.ORDER.getCode());//结算项类别（GROUP  OR  ORDER）

            setSettlementItem.setActualSettlementPrice(orderItem.getActualSettlementPrice());
            setSettlementItem.setBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
            setSettlementItem.setBudgetTotalSettlementlPrice(orderItem.getBuyoutTotalPrice());
            setSettlementItem.setBudgetQuantity(orderItem.getBuyoutQuantity());
            setSettlementItem.setBudgetFlag(orderItem.getBuyoutFlag());

            setSettlementItem.setOughtPay(order.getOughtAmount());//订单应付总额（分）
            setSettlementItem.setActualPay(order.getActualAmount());//订单实付金额（分）
            setSettlementItem.setCountSettleAmount(countSettleAmount);//整个订单结算总额（分）

            setSettlementItem.setBusinessName("NEW_SUPPLIER_BUSINESS");
            //通知类型
            setSettlementItem.setNotifyType(orderItem.getNotifyType());

            //银行立减金额
            setSettlementItem.setPayPromotionAmount(order.getPayPromotionAmount());
            //优惠金额list
            setSettlementItem.setOrdPromotionList(orderItem.getOrdPromotionList());

            //解决通关码推送结算问题
            OrdPassCode passCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItem.getOrderItemId());
            if (passCode != null) {
                setSettlementItem.setPassCode(passCode.getCode());// 通关码
                setSettlementItem.setPassAddCode(passCode.getAddCode());//辅助码
                setSettlementItem.setPassSerialno(passCode.getPassSerialno());//通关流水号
                setSettlementItem.setPassExtid(passCode.getPassExtid());//供应商回调信息
            }

            settlementItemList.add(setSettlementItem);

        }

        return settlementItemList;
    }


    /**
     * 根据OrderId返回单个用订单那子项Order对象
     *
     * @param orderId
     * @return
     */
    private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
        OrdOrder order = null;
        ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

        OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
        orderIndentityParam.setOrderId(orderId);

        OrderFlagParam orderFlagParam = new OrderFlagParam();
        orderFlagParam.setOrderItemTableFlag(true);
        orderFlagParam.setOrderPersonTableFlag(true);
        orderFlagParam.setOrderPackTableFlag(true);

        condition.setOrderIndentityParam(orderIndentityParam);
        condition.setOrderFlagParam(orderFlagParam);
        //以订单为中心综合查询
        //ord_order
        List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
        if (orderList != null && orderList.size() == 1) {
            order = orderList.get(0);
        }
        return order;

    }

    /**
     * 根据orderID 查询DISTRIBU_TYPE，order_channel
     */
    private Map<String, String> getDistribuTypeAndOrderChannelByOrderId(OrdOrder order) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            ResultHandleT<String> tntOrderChannelResult = tntOrderQueryServiceRemote.getTntOrderChannel(order.getOrderId());
            com.lvmama.vst.api.vo.ResultHandleT<OrderBaseVo> OrderBaseVoResult = vstCommOrderQueryService.findOrderDetail(order.getDistributorId(), order.getOrderId());
            String distribuType = null;
            String orderChannel = null;
            if (tntOrderChannelResult != null && tntOrderChannelResult.getReturnContent() != null) {
                orderChannel = tntOrderChannelResult.getReturnContent();
            }
            if (OrderBaseVoResult != null && OrderBaseVoResult.getReturnContent() != null) {
                distribuType = OrderBaseVoResult.getReturnContent().getDistributorCode();
            }
            map.put("distribuType", distribuType);
            map.put("orderChannel", orderChannel);
        } catch (Exception e) {
            logger.error("获取orderChannel和distribuType出错:" + e.getMessage());

        }

        return map;
    }


    /**
     * 财务结算系统处理完成之后，发送消息 收到消息后更新订单、订单子项的结算状态
     *
     * @param message
     */
    private void modifySettlementPriceMsg(Message message, Map countSettlePriceMap, boolean isFirstExecute) {
        Long orderId = 0L;
        OrdOrderItem ordOrderItem = null;
        Long orderItemId = message.getObjectId();
        ordOrderItem = this.orderUpdateService.getOrderItem(orderItemId);
        orderId = ordOrderItem.getOrderId();
        logger.info("modifySettlementPriceMsg 接受消息 orderId=" + ordOrderItem.getOrderId() + ", eventType=" + message.getEventType());
        logger.info("isOrderModifySettlementPriceMsg ordOrderItemId:" + ordOrderItem.getOrderItemId() + " & actualSettlementPrice" + ordOrderItem.getActualSettlementPrice() + " & totalSettlementPrice" + ordOrderItem.getTotalSettlementPrice());
        OrdOrder order = this.getOrderWithOrderItemByOrderId(orderId);
        if (!order.isNeedSettlement()) {
            logger.info("预付订单才需要进入结算");
            return;
        }

        List<SettlementItem> settlementItemList = new ArrayList<SettlementItem>();

        if (order.hasPayed() && order.isPayToLvmama() && order.hasInfoAndResourcePass()) {

            order.setOrdPersonList(order.getOrdPersonList());
            OrdPerson contactPerson = order.getContactPerson();
            String contactMobileNo = null;
            if (contactPerson != null) {
                contactMobileNo = contactPerson.getMobile();
            }
            logger.info("***手机号***" + contactMobileNo);
            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            Long countSettleAmount = 0L;
            for (OrdOrderItem ordItem : orderItemList) {
                if (initItemValidate(order, ordItem)) {
                    countSettleAmount += ordItem.getTotalSettlementPrice();
                }
            }
            countSettlePriceMap.put(orderId, countSettleAmount);
            Date visitTime;
            Long productId;
            logger.info("***手机号***" + contactMobileNo);
            if (MessageUtils.isOrderModifySettlementPriceMsg(message)) {
                orderItemList.clear();
                orderItemList.add(ordOrderItem);
                logger.info("isOrderModifySettlementPriceMsg orderItemList size=" + orderItemList.size());
            }
            for (OrdOrderItem orderItem : orderItemList) {

                StringBuffer str = new StringBuffer();
                SettlementItem item = new SettlementItem();
                item.setOrderId(orderItem.getOrderId());
                item.setOrderItemMetaId(orderItem.getOrderItemId());

                logger.info("processer1>>orderId = " + order.getOrderId() + "orderStatus = " + order.getOrderStatus());
                if (StringUtils.isEmpty(order.getOrderStatus())) {
                    order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
                }
                item.setOrderStatus(order.getOrderStatus());

                item.setContactMobileNo(contactMobileNo);


                item.setTotalSettlementPrice(orderItem.getTotalSettlementPrice());
                item.setActualSettlementPrice(orderItem.getActualSettlementPrice());

                item.setBudgetTotalSettlementlPrice(orderItem.getBuyoutTotalPrice());
                item.setBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
                item.setBudgetQuantity(orderItem.getBuyoutQuantity());
                item.setBudgetFlag(orderItem.getBuyoutFlag());
                item.setVisitTime(orderItem.getVisitTime());
                //订单出发日期
                if (order != null && order.getVisitTime() != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    visitTime = order.getVisitTime();
                    String dateString = formatter.format(visitTime);
                    str.append(dateString);
                    str.append("-");
                }
                //产品ID
                if (order.getProductId() != null) {
                    productId = order.getProductId();
                    str.append(productId);
                }
                item.setProductType(this.getProductType(orderItem));//销售产品类型  orderItem.getCategoryId()+""
                item.setTravelGroupCode(str.toString());
                item.setProductId(order.getProductId());
                item.setProductName(order.getProductName());
                logger.info("----------结算价修改团号---------:" + str.toString() + "-----productName-----:" + order.getProductName());

                setQuantityValue(orderItem,item);
                item.setUpdateRemark("结算价修改");
                item.setCountSettleAmount(countSettleAmount);
                item.setSupplierId(orderItem.getSupplierId());
                item.setOrderCreateTime(orderItem.getCreateTime());
                item.setPriceConfirmStatus(orderItem.getPriceConfirmStatus());
                logger.info("message addition is" + GsonUtils.toJson(message.getAddition()) + " and orderItemId = " + orderItem.getOrderItemId());
                //"system" 为退款自动修改结算价的消息,采用枚举带"_"ebk不解析
                if (StringUtil.isNotEmptyString(message.getAddition()) && MessageUtils.isOrderModifySettlementPriceMsg(message) && message.getAddition().contains("system")) {
                    item.setItemRefundedAmount(getItemRefundAmount(orderItem.getOrderItemId()));
                }
                settlementItemList.add(item);
            }

        } else {
            logger.info("message type:" + message.getEventType() + "order:" + order.getOrderId() + " isPayToLvmama:" + order.isPayToLvmama() + " hasPayed status:" + order.hasPayed() + " order status:" + order.getOrderStatus() + ", don't need to settlement!");
        }

        if (settlementItemList.size() > 0) {
            orderSettlementService.insertOrUpdateSettlementItem(settlementItemList, EVENT_TYPE.valueOf(message.getEventType()));
        }
    }


    private SettlementItem initItem(final OrdOrder order, OrdOrderItem orderItem, final Map countSettlePriceMap, Message message) {

        Long orderItemId = orderItem.getOrderItemId();
        List<SettlementItem> items = initItem(order, countSettlePriceMap, orderItem);
        for (SettlementItem item : items) {
            if (orderItemId.longValue() == item.getOrderItemMetaId().longValue()) {
                return item;
            }
        }
        return null;
    }

    private List<SettlementItem> initItem(OrdOrder order, Map countSettlePriceMap, final OrdOrderItem orderItem) {

        Long orderId = orderItem.getOrderId();
        List<SettlementItem> items = new ArrayList<SettlementItem>();

		/*--------------------查询订单的退款备注--------------------------*/
        String refundMemo = null;
        if (order.getRefundedAmount() != null && order.getRefundedAmount() > 0) {// 存在退款，查询退款明细
            StringBuffer refundMemoBuff = new StringBuffer();
            List<OrdRefundment> refundmentList = orderRefundmentServiceAdapter.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
            for (OrdRefundment or : refundmentList) {
                refundMemoBuff.append("[").append(or.getMemo()).append("] ");
            }
            refundMemo = refundMemoBuff.toString();
        }

        List<OrdOrderItem> orderItemList = order.getOrderItemList();

        Long countSettleAmount = 0L;//整个订单结算总额（分）
        Long orderTotalSettlementPrice = 0L;
        for (OrdOrderItem ordOrderItem : orderItemList) {

            countSettleAmount += ordOrderItem.getTotalSettlementPrice();
            orderTotalSettlementPrice += ordOrderItem.getQuantity() * ordOrderItem.getSettlementPrice();
        }
        Long orderItemMetaPayedAmount = 0L;

        if (orderTotalSettlementPrice > 0) {

            //订单销售分拆后的支付金额   订单应付总金额*（当前订单子项单家*数量/所有订单子项数量*单价和）
            //Long orderItemMetaPayedAmount=order.getOughtAmount()*(orderItem.getQuantity()*orderItem.getSettlementPrice()/orderTotalSettlementPrice);
            double payedAmountPer = orderItem.getQuantity() * orderItem.getSettlementPrice() * 1.0 / orderTotalSettlementPrice * 1.0;
            orderItemMetaPayedAmount = (long) (order.getOughtAmount() * payedAmountPer);

        }

        Long couponAmount = this.getMarkCouponAmount(orderId);
        countSettleAmount += orderItem.getTotalSettlementPrice();
        countSettlePriceMap.put(orderItem.getOrderId(), countSettleAmount);

        String contactName = "";
        if (order.getContactPerson() != null) {
            contactName = order.getContactPerson().getFullName();
        }
        // 将游玩人推送给结算系统
        String travellerName = "";
        List<OrdPerson> travellerList = order.getOrdTravellerList();
        if (travellerList != null && !travellerList.isEmpty()) {
            StringBuilder travellerBulider = new StringBuilder();
            for (OrdPerson travellerPerson : travellerList) {
                travellerBulider.append(travellerPerson.getFullName()).append(
                        ",");
            }
            travellerName = travellerBulider.toString().substring(0,
                    travellerBulider.toString().length() - 1);
        }
        ResultHandleT<SuppGoods> suppGoodsHandle = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
        SuppGoods suppGoods = suppGoodsHandle.getReturnContent();
        PermUser permUser = permUserServiceAdapter.getPermUserByUserId(suppGoods.getManagerId());
        String productManager = "";
        if (permUser != null && StringUtils.isNotEmpty(permUser.getUserName())) {
            productManager = permUser.getUserName();
        }
        order.setOrdPersonList(order.getOrdPersonList());
        OrdPerson contactPerson = order.getContactPerson();
        String contactMobileNo = null;
        if (contactPerson != null) {
            contactMobileNo = contactPerson.getMobile();
        }
        logger.info("***手机号***" + contactMobileNo);
        SettlementItem item = new SettlementItem();

        item.setContactMobileNo(contactMobileNo);

        item.setOrderId(orderId);
        logger.info("processer6>>orderId = " + order.getOrderId() + "orderStatus = " + order.getOrderStatus());
        item.setOrderStatus(order.getOrderStatus());
        item.setOrderPaymentTime(order.getPaymentTime());
        item.setOrderCreateTime(order.getCreateTime());
        item.setOrderPaymentStatus(order.getPaymentStatus());
        item.setOrderContactPerson(contactName);
        item.setTravelingPerson(travellerName);
        item.setOrderCouponAmount(couponAmount);
        item.setDistributorId(order.getDistributorId());
        if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())) {
            item.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.CANCEL.name());
        }
        List<OrdRefundMentItem> refundmentItemList = orderRefundmentServiceAdapter.findOrderRefundMentItemByOrderItemMetaId(orderItem.getOrderItemId());
        if (refundmentItemList != null && refundmentItemList.size() > 0) {
            item.setOrderRefund(true);
        } else {
            item.setOrderRefund(false);
        }
        item.setRefundMemo(refundMemo);

        item.setOrderItemProdId(orderItem.getOrderItemId());//订单子项ID
        item.setProductId(orderItem.getProductId());
        item.setProductName(orderItem.getProductName());
        item.setProductType(this.getProductType(orderItem));//销售产品类型  orderItem.getCategoryId()+"" Constant.PRODUCT_TYPE.HOTEL.getCode()
        item.setProductBranchId(suppGoods.getSuppGoodsId());
        item.setProductBranchName(suppGoods.getGoodsName());
        item.setProductPrice(orderItem.getPrice());

        item.setFilialeName(order.getFilialeName());
        item.setBelongBU(orderItem.getRealBuType());
        item.setBelongMainBU(order.getBuCode());
        item.setMetaFilialeName(order.getFilialeName());
        item.setOrderItemMetaId(orderItem.getOrderItemId());
        item.setOrderItemMetaPayedAmount(orderItemMetaPayedAmount);
        item.setMetaProductManager(productManager);
        item.setMetaProductId(orderItem.getProductId());
        item.setMetaProductName(orderItem.getProductName());
        item.setMetaBranchId(orderItem.getSuppGoodsId());

        String branchName = (String) orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
        item.setMetaBranchName(branchName);
        item.setSettlementPrice(orderItem.getSettlementPrice());
        item.setActualSettlementPrice(orderItem.getActualSettlementPrice());
        item.setSupplierId(orderItem.getSupplierId());

        // 保存结算对象ID, 一供多结项目
        saveSettleEntityCode(item, orderItem, order);

        // 保存分摊信息
        saveApportionInfo(item, orderItem, order);

        item.setProductQuantity(1L);

        setQuantityValue(orderItem,item);

        item.setVisitTime(orderItem.getVisitTime());
        Long totalSettlementPrice = orderItem.getTotalSettlementPrice();
        if (totalSettlementPrice == null) {
            totalSettlementPrice = item.getQuantity() * item.getActualSettlementPrice();
        }
        item.setTotalSettlementPrice(totalSettlementPrice);
        item.setSettlementType(OrderEnum.SETTLEMENT_TYPE.ORDER.getCode());

        Map<String, Object> apply = getOrderAmountApply(order.getOrderId());
        item.setAdjustmentAmount(null != apply.get("amountApply") ? (Long) apply.get("amountApply") : null);
        item.setAdjustmentRemark(null != apply.get("remark") ? (String) apply.get("remark") : null);
        item.setRefundedAmount(order.getRefundedAmount());
        item.setOughtPay(order.getOughtAmount());
        item.setActualPay(order.getActualAmount());
//		item.setUpdateRemark(getUpdateRemark(item.getOrderItemMetaId()));
        item.setUpdateRemark("生成结算单");
        item.setCountSettleAmount(countSettleAmount);

        item.setBusinessName("NEW_SUPPLIER_BUSINESS");
        items.add(item);


        return items;
    }

    /**
     * 设置 Quantity值
     */
    private void setQuantityValue(OrdOrderItem orderItem, SettlementItem item ){
        Long quantity = orderItem.getQuantity();
        logger.info(Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId() + "--" + orderItem.getCategoryId());
        if (Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId().equals(String.valueOf(orderItem.getCategoryId()))) {
            Map<String, Object> maps = new HashMap<String, Object>();
            maps.put("orderItemId", orderItem.getOrderItemId());
            List<OrdOrderHotelTimeRate> lists = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateListByParams(maps);
            if (lists != null && lists.size() > 0) {
                item.setNightNum(lists.size());
            }
        }
        item.setQuantity(quantity);
    }

    /**
     * 保存结算对象信息
     *
     * @param item
     * @param orderItem
     */
    private void saveSettleEntityCode(SettlementItem item, OrdOrderItem orderItem, OrdOrder order) {
        // 从子订单对象中 获取 结算对象CODE
        try {
            String settleEntityCodeValue = orderItem.getSettlementEntityCode();

            SuppSettlementEntities settlementEntities = null;

            if (!StringUtils.isEmpty(settleEntityCodeValue)) {
                // 子订单中 结算对象ID_CODE 已绑定
                String[] arr = settleEntityCodeValue.split("_");
                if (arr.length != 2) {
                    throw new BusinessException(" [ OrderSettlementProcesser ],Error msg : settleEntityCodeValue is " + settleEntityCodeValue + " , is not ID_CODE formate!!! ");
                }

                Long settleEntityID = Long.parseLong(arr[0]);
                String settleEntityCode = arr[1];
                ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCodeAndId(settleEntityID, settleEntityCode);
                if (!resultHandleT.isSuccess()) {
                    throw new BusinessException(" [ OrderSettlementProcesser ],Error msg : can not find settleEntity by code (" + settleEntityCode + ") and ID(" + settleEntityID + ")");
                }

                settlementEntities = resultHandleT.getReturnContent();
            } else {
                // 子订单中 结算对象ID_CODE 未绑定
                logger.info(" [ OrderSettlementProcesser ] : settleEntityCodeValue is null in orderItem(" + orderItem.getOrderItemId() + ")");
                Long suppGoodsId = orderItem.getSuppGoodsId();
                String suppGoodsSettleEntityCode = null;
                ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId, new SuppGoodsParam());
                if (suppGoodsResultHandleT.isSuccess() && suppGoodsResultHandleT.getReturnContent() != null) {
                    SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
                    suppGoodsSettleEntityCode = targetSuppGoods.getSettlementEntityCode();
                } else {
                    throw new BusinessException(" [ OrderSettlementProcesser ] , ERROR : suppGoods(" + suppGoodsId + ") has no settlement entity code!!!");
                }

                ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(suppGoodsSettleEntityCode);
                if (!resultHandleT.isSuccess()) {
                    throw new BusinessException(" [ OrderSettlementProcesser ] , ERROR : find SuppSettlementEntities by code (" + suppGoodsSettleEntityCode + " ) failed");
                }

                settlementEntities = resultHandleT.getReturnContent();
                if (null != settlementEntities) {
                    String orderItemCode = settlementEntities.getId() + "_" + settlementEntities.getCode();
                    if (StringUtils.isNotEmpty(orderItemCode) && orderItemCode.split("_").length > 1) {
                        orderItem.setSettlementEntityCode(orderItemCode);
                        orderUpdateService.updateOrderItemByIdSelective(orderItem);
                    }
                }

            }

            if (null == settlementEntities) {
                throw new BusinessException(" [ OrderSettlementProcesser ],Error msg : settlementEntities is null !!!");
            }
            Map<String,Object> personInfoMap = getBookerInfo(order);

            logger.info("***************  saveSettleEntityCode  start  *****************  ");
            item.setTargetId(settlementEntities.getId());

            // 保存结算对象各属性信息
            item.setSettleEntityId(settlementEntities.getId());
            item.setSuppSettleRuleId(settlementEntities.getSuppSettleRuleId());
            saveSupplierInfo(orderItem, item); // 保存 供应商信息
            item.setName(settlementEntities.getName());
            item.setTargetName(settlementEntities.getName());
            item.setCode(settlementEntities.getCode());
            item.setAccountName(settlementEntities.getAccountName());
            item.setBankName(settlementEntities.getBankName());
            item.setBankAccountNo(settlementEntities.getBankAccountNo());
            item.setSettlementClasification(settlementEntities.getSettlementClasification());
            item.setSettleCycle(settlementEntities.getSettleCycle());
            item.setSettlementMethods(settlementEntities.getSettlementMethods());
            item.setEbkNo(settlementEntities.getEbkNo());
            item.setEffectedDate(settlementEntities.getEffectedDate());
            item.setExpiryDate(settlementEntities.getExpiryDate());
            item.setFareClearingTime(settlementEntities.getFareClearingTime());
            //银行信息
            item.setBankAddress(settlementEntities.getBankAddress());
            item.setSwiftCode(settlementEntities.getSwiftCode());
            //退回标志
            item.setExpiredRefundFlag(orderItem.getExpiredRefundFlag());

            //设置book 信息
            if (personInfoMap != null) {
                OrdPerson ordPerson = (OrdPerson) personInfoMap.get("bookerPerson");
                if(ordPerson!=null){
                    item.setBookerEmail(ordPerson.getEmail());
                    item.setBookerMobile(ordPerson.getMobile());
                    item.setBookerName(ordPerson.getFullName());
                }
                int travellerNum = (int) personInfoMap.get("travellerNum");
                item.setTravellerNum(travellerNum);
            }
            item.setUserId(order.getUserId());
            item.setInvoiceStatus(order.getInvoiceStatus());
            char isTestOrder = order.getIsTestOrder();
            String isTestStr = String.valueOf(isTestOrder);
            item.setIsTestOrder(isTestStr);
            // 结算周期
            item.setSettlementPeriod(settlementEntities.getSettlementClasification());
            item.setSuppSettlementEntities(settlementEntities);

            // 价格确认状态
            item.setPriceConfirmStatus(orderItem.getPriceConfirmStatus());

            // SBU 需求
            setSbuInfo(item, orderItem, order);

            logger.info("***************  saveSettleEntityCode  end  *****************  ");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 保存分摊信息
     */
    private void saveApportionInfo(SettlementItem item, OrdOrderItem orderItem, OrdOrder order) {
        logger.info(" ********* OrderSettlementProcesser.saveApportionInfo start,orderItemId:" + orderItem.getOrderItemId() + " ************* ");
        try {
            item.setApportionMessage(false); // 表示非分摊
            item.setPureApportion(false);    // 表示非纯分摊
            ResultHandleT<SuppGoods> resultSuppGoods = suppGoodsHotelAdapterService.findSuppGoodsById(orderItem.getSuppGoodsId());
            if (resultSuppGoods.isSuccess() && null != resultSuppGoods.getReturnContent()) {
                Long contractId = resultSuppGoods.getReturnContent().getContractId();
                ResultHandleT<SuppContract> suppContractHandle = suppContractClientService.findSuppContractByContractId(contractId);
                if (null != suppContractHandle && null != suppContractHandle.getReturnContent()) {
                    //合同编号
                    item.setContractCode(suppContractHandle.getReturnContent().getContractNo());
                }
            } else {
                logger.error("Can not find suppgoods by id(" + orderItem.getSuppGoodsId() + ")");
            }

            item.setOrderIsTermBill((byte) 0);
            Map<String, Object> contentMap = orderItem.getContentMap();
            if (null != contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())) {
                //是否期票
                String aperiodicFlag = (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name());
                if (StringUtils.isNotEmpty(aperiodicFlag) && "Y".equalsIgnoreCase(aperiodicFlag)) {
                    item.setOrderIsTermBill((byte) 1);
                }
            }

            //酒店取间夜数
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
                Map<String, Object> params = new HashMap<>();
                params.put("orderItemId", orderItem.getOrderItemId());
                List<OrdOrderHotelTimeRate> hotelTimeRates = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
                if (CollectionUtils.isNotEmpty(hotelTimeRates)) {
                    int hotelTimeRatesSize = hotelTimeRates.size();
                    if (null != orderItem.getQuantity()) {
                        hotelTimeRatesSize = hotelTimeRatesSize * Integer.parseInt(String.valueOf(orderItem.getQuantity()));
                    }
                    item.setOrderItemRoomNight(hotelTimeRatesSize);
                } else {
                    logger.info(" Find OrdOrderHotelTimeRate list by(orderItemId" + orderItem.getOrderItemId() + ") size is 0!");
                }
            }

            //调分摊接口
            OrderItemApportionInfoQueryVO orderApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
            orderApportionInfoQueryVO.setOrderId(order.getOrderId());
            orderApportionInfoQueryVO.setOrderItemId(orderItem.getOrderItemId());
            long startTime = System.currentTimeMillis();
            logger.info(" Calc orderItem apportion info start");
            OrderItemApportionInfoPO orderItemApportion = apportionInfoQueryService.calcOrderItemApportionInfo(orderApportionInfoQueryVO);
            long endTime = System.currentTimeMillis();
            logger.info(" CalcOrderItemApportionInfo method cast " + (endTime - startTime) + " ms");
            //子单优惠总额
            item.setCouponAmount(orderItemApportion.getItemTotalCouponAmount());
            //子单促销金额
            item.setPromotionAmount(orderItemApportion.getItemTotalPromotionAmount());
            //子单手动改价金额
            item.setManualChangeAmount(orderItemApportion.getItemTotalManualChangeAmount());
            //子单实收金额
            item.setOrderItemActualReceived(orderItemApportion.getItemTotalActualPaidAmount());
            //子单退款总额
            item.setItemRefundedAmount(orderItemApportion.getTotalRefundAmount());
            //子单销售金额
            item.setOrdeItemSaleAmount(orderItem.getTotalAmount());
            logger.info(" ApiSuppOrderService getSuppOrderByOrderItemId(orderItemId:" + orderItem.getOrderItemId() + ") start");
            RequestSuppOrder requestSuppOrder = apiSuppOrderService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
            if (null != requestSuppOrder && StringUtils.isNotEmpty(requestSuppOrder.getSuppOrderId())) {
                item.setSupplierOrderCode(requestSuppOrder.getSuppOrderId());  //供应商订单号
            } else if (null != requestSuppOrder && StringUtils.isEmpty(requestSuppOrder.getSuppOrderId())) {
                logger.info("SuppOrder is not null, but suppOrderId is null!(orderItemId:" + orderItem.getOrderItemId() + ") ");
            } else {
                logger.info("SuppOrder is null!(orderItemId:" + orderItem.getOrderItemId() + ") ");
            }
        } catch (Exception e) {
            logger.error("SaveApportionInfo method has exception, error msg is " + e.getMessage());
        }

        logger.info(" ********* OrderSettlementProcesser.saveApportionInfo end ,orderItemId:" + orderItem.getOrderItemId() + " ************* ");
    }

    /**
     * SBU 需求
     *
     * @param item
     * @param orderItem
     */
    private void setSbuInfo(SettlementItem item, OrdOrderItem orderItem, OrdOrder order) {

        ProdProduct curProduct = findProductByOrderItem(orderItem);
        Long subCategoryId = curProduct.getSubCategoryId();
        if (null == subCategoryId) {
            subCategoryId = curProduct.getBizCategoryId();
            logger.info(" [ setSbuInfo ] : order id is (" + order.getOrderId() + ") , order item id is (" + orderItem.getOrderItemId() + "), productSubType is " + subCategoryId);
        }
        item.setProductSubType(String.valueOf(subCategoryId)); // 三级品类(没有就是二级品类，再没有就是一级)

        // 主订单对应的品类ID
        Long orderCategoryId = order.getCategoryId();
        if (orderCategoryId != null) {
            item.setMainProductSubType(String.valueOf(orderCategoryId));
            logger.info(" [ setSbuInfo ] : main order id is (" + order.getOrderId() + ") , mainProductSubType is " + orderCategoryId);
            return;
        }

        // 如果订单中 品类ID 为空,再次查询库中是否 含有品类ID
        Long orderId = order.getOrderId();
        OrdOrder ordOrder = iOrdOrderService.findByOrderId(orderId);
        if (ordOrder == null) {
            logger.error(" [ setSbuInfo ] : can not find order by orderId (" + orderId + ") !!! ");
            return;
        }

        if (ordOrder.getCategoryId() == null) {
            logger.error(" [ setSbuInfo ] : order has no category ID ( order id " + orderId + ") !!! ");
            return;
        } else {
            item.setMainProductSubType(String.valueOf(ordOrder.getCategoryId()));
            logger.info(" [ setSbuInfo ] :new main order id is (" + ordOrder.getOrderId() + ") , mainProductSubType is " + ordOrder.getCategoryId());
            return;
        }
    }

    private ProdProduct findProductByOrderItem(OrdOrderItem orderItem) {
        Long productID = orderItem.getProductId();
        if (null == productID) {
            throw new BusinessException(" [ OrderSettlementProcesser ], Error msg : orderItem( " + orderItem.getOrderItemId() + " ) productId is null ");
        }

        ResultHandleT<ProdProduct> curOrderProductRes = prodProductClientService.findProdProductById(productID);
        if (!curOrderProductRes.isSuccess()) {
            throw new BusinessException(" [ OrderSettlementProcesser ], Error msg 1 : prodProductClientService.findProdProductById(" + productID + ") failed ");
        }

        ProdProduct curProduct = curOrderProductRes.getReturnContent();
        if (null == curProduct) {
            throw new BusinessException(" [ OrderSettlementProcesser ],Error msg : curProduct is null !!!");
        }
        return curProduct;
    }


    /**
     * 保存 供应商信息
     *
     * @param orderItem
     * @return
     */
    private void saveSupplierInfo(OrdOrderItem orderItem, SettlementItem item) {

        if (null == orderItem) {
            logger.error("[ saveSupplierInfo ]: orderItem is null!");
            return;
        }

        Long supplierId = orderItem.getSupplierId();
        if (null != supplierId) {
            item.setSupplierId(supplierId);
            ResultHandleT<SuppSupplier> resultHandleT = suppSupplierClientService.findSuppSupplierById(supplierId);
            if (resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null) {
                SuppSupplier suppSupplier = resultHandleT.getReturnContent();
                item.setSupplierName(suppSupplier.getSupplierName());
            }
        } else {
            logger.error("[ saveSupplierInfo ]: orderItem(" + orderItem.getOrderItemId() + ") supplierId is null!");
        }

        Long contractId = orderItem.getContractId();
        if (null != contractId) {
            item.setContractId(contractId);
        } else {
            logger.error("[ saveSupplierInfo ]: orderItem(" + orderItem.getOrderItemId() + ") contractId is null!");
        }
    }

    /**
     * 获取订单的优惠券金额
     *
     * @param orderId 订单号
     * @return 优惠券使用金额
     */
    private Long getMarkCouponAmount(final Long orderId) {
        long amount = 0l;
        try {
            // 查询订单优惠券使用情况
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderId", orderId);
            List<OrdOrderAmountItem> listAmountItem = ordOrderAmountItemDao.findOrderAmountItemList(params);
            //List<OrdOrderAmountItem> listAmountItem =queryDAO.queryOrdOrderAmountItem(orderId,"ALL");
            if (null != listAmountItem && !listAmountItem.isEmpty()) {
                for (OrdOrderAmountItem item : listAmountItem) {
					/*if (item.isCouponItem()) {
						amount += Math.abs(item.getItemAmount());
					}*/
                    //增加优惠券判断类型 by 李志强 2015-9-29
                    if (OrderEnum.ORDER_AMOUNT_TYPE.COUPON_PRICE
                            .name().equals(item.getOrderAmountType()) ||
                            OrderEnum.ORDER_AMOUNT_TYPE.COUPON_AMOUNT
                                    .name().equals(item.getOrderAmountType()) ||
                            OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE
                                    .name().equals(item.getOrderAmountType())) {
                        amount += Math.abs(item.getItemAmount());
                    }
                }
            }
            amount = 0 - amount;
        } catch (Exception ex) {
            logger.error(ExceptionFormatUtil.getTrace(ex));
        }
        return amount;
    }

    private String getProductType(OrdOrderItem orderItem) {

        Long categoryId = orderItem.getCategoryId();
        return categoryId + "";
    }

    /**
     * 处理漏单方法，保存记录定时任务记录数
     *
     * @param message        消息对象
     * @param isFirstExecute 是否首次执行，如果是首次执行。保存记录数
     * @author Zhang.Wei
     */
    public void process(Message message, boolean isFirstExecute) {
        isFirstExecuteThreadLocal.set(isFirstExecute);
        saveComJobConfig(message, isFirstExecute);
        process(message);
    }

    /**
     * 保存定时任务的记录
     *
     * @param message 保存数据来源
     * @author Zhang.Wei
     */
    private void saveComJobConfig(Message message, boolean isFirstExecute) {
        if (isFirstExecute) {
            logger.info("OrderSettlementProcesser.saveComJobConfig,save record for com_job_config table");
            ComJobConfig jobConfig = new ComJobConfig();
            jobConfig.setObjectId(message.getObjectId());
            jobConfig.setCreateTime(new Date());
            if (message != null) {
                jobConfig.setObjectType(message.getEventType());
            }
            jobConfig.setJobType(ComJobConfig.JOB_TYPE.ORDER_SETTLEMENT.name());
            jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
            jobConfig.setRetryCount(5L);
            comJobConfigService.saveComJobConfig(jobConfig);
        }
    }

    /**
     * 根据条件删除job记录数
     *
     * @param message 条件来源
     * @author Zhang.Wei
     */
    private void deleteComJobConfigByCondition(Message message) {
        ComJobConfig comJobConfig = new ComJobConfig();
        comJobConfig.setObjectId(message.getObjectId());
        comJobConfig.setObjectType(message.getEventType());
        comJobConfig.setJobType(ComJobConfig.JOB_TYPE.ORDER_SETTLEMENT.name());
        comJobConfigService.deleteComJobConfigByCondition(comJobConfig);
    }

    private boolean tpByfish(final Message message ) {
        try {
            myJmsTemplate.send("ActiveMQ.FINANCE.FISH", new MessageCreator() {
                @Override
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(message.getObjectId()));
                    textMessage.setStringProperty("eventType", message.getEventType());
                    return textMessage;
                }
            });
            return true;
        }catch (Exception e) {
            logger.error("消息发送到fish 失败:" , e);
            logger.info("改供应商不符合走fish系统条件，messageId :"+message.getObjectId()+"消息类型："+message.getEventType());
            return false;
        }
    }

    /**
     * 处理支付成功结算信息
     *
     * @param message        消息对象
     * @param isFirstExecute 是否首次接受消息处理
     */
    private void executePayedMsg(Message message, boolean isFirstExecute) {
        Long orderId = message.getObjectId();
        logger.info("executePayedMsg 接受消息 orderId=" + orderId + ", eventType=" + message.getEventType());
        OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderId);
        if (orderObj != null) {
            logger.info("orderId =" + orderObj.getOrderId() + ",hasInfoAndResourcePass=" +
                    orderObj.hasInfoAndResourcePass() + ",hasPayed=" + orderObj.hasPayed());
        }

        if (!orderObj.isNeedSettlement()) {
            logger.info("预付订单才需要进入结算");
            return;
        }

        if (orderObj.hasInfoAndResourcePass() && orderObj.hasPayed()) {
            logger.info("结算支付成功结算信息 处理开始");
            boolean isApportion = MessageUtils.isOrderApportionSuccess(message);
            List<SettlementItem> settlementItemList = this.fillSettlementItemList(orderObj);
            for (SettlementItem settlementItem : settlementItemList) {
                List<SetSettlementItem> setSettleList = orderSettlementService.findSetSettlementItemByParams(orderId, settlementItem.getOrderItemProdId());
                //状态   订单支付成功和订单取消后 分别是正常和取消
                if (orderObj.isCancel()) {
                    settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.CANCEL.name());
                } else {
                    settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.NORMAL.name());
                }
                logger.info("开始保存结算信息");
                if (setSettleList == null || setSettleList.size() <= 0 || isApportion) {
                        orderSettlementService.saveSettlementItem(settlementItem);
                    logger.info("结算详细信息:" + JSONObject.fromObject(settlementItem).toString());
                }
                logger.info("结束保存结算信息");
            }

            logger.info("结算支付成功结算信息 处理完成");
        }
        try {
            pushOrdItemGroupSettleFlag(orderId);
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }


    }

    /**
     * 处理订单取消消息逻辑
     *
     * @param message        消息对象
     * @param isFirstExecute
     */
    private void executeOrderCancelMsg(Message message, boolean isFirstExecute) {
        Long orderId = message.getObjectId();
        logger.info("executeOrderCancelMsg 接受消息 orderId=" + orderId + ", eventType=" + message.getEventType());
        OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderId);
        if (!orderObj.isNeedSettlement()) {
            logger.info("预付订单才需要进入结算");
            return;
        }

        if (orderObj.hasInfoAndResourcePass() && orderObj.hasCanceled() && orderObj.getActualAmount() > 0) {
            logger.info("开始订单取消结算信息处理");

            List<SettlementItem> settlementItemList = this.fillSettlementItemList(orderObj);
            for (SettlementItem settlementItem : settlementItemList) {

                settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.CANCEL.name());//状态   订单支付成功和订单取消后 分别是正常和取消
                logger.info("开始保存结算信息");
                    orderSettlementService.saveSettlementItem(settlementItem);
                logger.info("结束保存结算信息");
            }
            logger.info("结束订单取消结算信息处理");
        }


    }

    /**
     * 处理退款推算订单结算逻辑
     *
     * @param message
     * @param isFirstExecute
     * @param countSettlePriceMap
     */
    private void executeOrderRefumentMsg(Message message, Boolean isFirstExecute, Map<Long, Long> countSettlePriceMap) {
        logger.info("退款推算订单结算逻辑处理开始");
        Long refundmentId = message.getObjectId();
        List<SettlementItem> settlementItemList = new ArrayList<SettlementItem>();
        OrdRefundment refundment = orderRefundmentServiceAdapter.queryOrdRefundmentById(refundmentId);
        logger.info("executeOrderRefumentMsg 接受消息 orderId=" + refundment.getOrderId() + ", eventType=" + message.getEventType());
        OrdOrder orderObj = getOrderWithOrderItemByOrderId(refundment.getOrderId());
        if (!orderObj.isNeedSettlement()) {
            logger.info("预付订单才需要进入结算");
            return;
        }

        // 信息资源审核通过且全额支付
        if (orderObj.hasInfoAndResourcePass() && orderObj.hasPayed() && orderObj.getRefundedAmount() > 0) {

            logger.info("开始组装settlementItemList");

            settlementItemList = initItem(refundmentId, orderObj, countSettlePriceMap);

            logger.info("结束组装settlementItemList.size():" + settlementItemList.size());

            if (settlementItemList != null && settlementItemList.size() > 0) {

                logger.info("调用方法insertOrUpdateSettlementItem");

                List<SettlementItem> items = new ArrayList<>();
                for (SettlementItem settlementItem : settlementItemList) {

                        items.add(settlementItem);

                }
                if (items.size() > 0) {
                    orderSettlementService.insertOrUpdateSettlementItem(items, EnumUtils.getEnum(Constant.EVENT_TYPE.class, message.getEventType()));

                    for (SettlementItem settlementItem : items) {

                        Long orderItemMetaId = settlementItem.getOrderItemMetaId();

                        //是否结算打款
                        boolean isPayment = orderSettlementService.searchSettlementPayByOrderItemMetaId(orderItemMetaId);

                        logger.info("是否结算打款：" + isPayment);

                        if (!isPayment) {

                            OrdOrderItem oldOrderItem = this.orderUpdateService.getOrderItem(orderItemMetaId);

                            Long totalSettlementPrice = settlementItem.getTotalSettlementPrice();
                            Long actualSettlementPrice = totalSettlementPrice / settlementItem.getQuantity();

                            OrdOrderItem ordOrderItem = new OrdOrderItem();
                            ordOrderItem.setOrderItemId(orderItemMetaId);
                            ordOrderItem.setActualSettlementPrice(actualSettlementPrice);
                            ordOrderItem.setTotalSettlementPrice(totalSettlementPrice);

                            if ("Y".equals(oldOrderItem.getBuyoutFlag())) {
                                ordOrderItem.setBuyoutTotalPrice(totalSettlementPrice);
                                ordOrderItem.setBuyoutPrice(ordOrderItem.getBuyoutTotalPrice() / oldOrderItem.getBuyoutQuantity());
                            }


                            logger.info("更新订单子项结算单价：" + actualSettlementPrice + ",结算总价：" + totalSettlementPrice);
                            //未打款的情况下      修改订单子项的结算价
                            int n = orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);

                            logger.info("updateOrderItemByIdSelective:" + n);
                            //订单子项更新成功后
                            //记录结算价变更记录OrdSettlementPriceRecord  暂时空着
                            if (n >= 1) {


                                OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();

                                newOrdSettlementPriceRecord.setRecordId(null);
                                newOrdSettlementPriceRecord.setOrderItemId(orderItemMetaId);
                                newOrdSettlementPriceRecord.setPriceType("PRICE");
                                newOrdSettlementPriceRecord.setOldActualSettlementPrice(oldOrderItem.getActualSettlementPrice());
                                newOrdSettlementPriceRecord.setOldTotalSettlementPrice(oldOrderItem.getTotalSettlementPrice());
                                newOrdSettlementPriceRecord.setNewActualSettlementPrice(actualSettlementPrice);
                                newOrdSettlementPriceRecord.setNewTotalSettlementPrice(totalSettlementPrice);

                                newOrdSettlementPriceRecord.setChangeType(ORD_SETTLEMENT_PRICE_CHANGE_TYPE.UNIT_PRICE.getCode());

                                String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
                                if ((totalSettlementPrice - oldOrderItem.getTotalSettlementPrice()) < 0) {
                                    changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
                                }
                                newOrdSettlementPriceRecord.setChangeResult(changeResult);
                                newOrdSettlementPriceRecord.setReason(OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.REFUND_SUCCESS.name());
                                newOrdSettlementPriceRecord.setOperator("system");
                                newOrdSettlementPriceRecord.setCreateTime(new Date());
                                newOrdSettlementPriceRecord.setRemark("退款修改结算价");
                                newOrdSettlementPriceRecord.setApproveRemark("退款修改结算价成功");
                                newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
                                newOrdSettlementPriceRecord.setUpdateTime(new Date());
                                newOrdSettlementPriceRecord.setOperatorApprove("system");
                                newOrdSettlementPriceRecord.setOrderId(oldOrderItem.getOrderId());
                                newOrdSettlementPriceRecord.setSuppGoodsId(oldOrderItem.getSuppGoodsId());
                                newOrdSettlementPriceRecord.setIsApprove("Y");
                                newOrdSettlementPriceRecord.setVisitTime(oldOrderItem.getVisitTime());
                                newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(ordOrderItem.getBuyoutTotalPrice());
                                newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(ordOrderItem.getBuyoutPrice());
                                newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(oldOrderItem.getBuyoutTotalPrice());
                                newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(oldOrderItem.getBuyoutPrice());

                                int m = ordSettlementPriceRecordService.insert(newOrdSettlementPriceRecord);

                                logger.info("insert ordSettlementPriceRecord:" + m);

                            }
                        }
                        // 修改订单子项的结算价
                        //settlementServiceAdapter.updateSettlementPrice(settlementItem.getOrderItemMetaId(), Constant.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE, Constant.ORD_SETTLEMENT_PRICE_REASON.REFUND_SUCCESS, "退款成功修改结算总价", settlementItem.getTotalSettlementPrice(), "SYSTEM",true);
                    }
                }
            }
        }
        logger.info("退款推算订单结算逻辑处理结束");

    }


    private void executeOrderItemSettleMsg(Message message, Boolean isFirstExecute, Map<Long, Long> countSettlePriceMap) {
        logger.info("手动生成结算单处理开始");

        Long orderItemId = message.getObjectId();
        OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
        logger.info("executeOrderItemSettleMsg 接受消息 orderId=" + orderItem.getOrderId() + ", eventType=" + message.getEventType());
        OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderItem.getOrderId());
        if (!orderObj.isNeedSettlement()) {
            logger.info("预付订单才需要进入结算");
            return;
        }

        String info = "";
        List<SettlementItem> ssItems = new ArrayList<SettlementItem>();
        boolean statusPass = false;
        //
        statusPass = OrderEnum.ORDER_STATUS.NORMAL.name().equalsIgnoreCase(orderObj.getOrderStatus())
                || OrderEnum.ORDER_STATUS.COMPLETE.name().equalsIgnoreCase(orderObj.getOrderStatus())
                || OrderEnum.ORDER_STATUS.CANCEL.name().equalsIgnoreCase(orderObj.getOrderStatus());
        if (orderObj.hasInfoAndResourcePass() && orderObj.hasPayed() && statusPass) {// 资源审核通过且全额支付且状态正常或完成(取消的消息不需要判断)
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contractId", orderItem.getContractId());
            params.put("supplierId", orderItem.getSupplierId());
            ResultHandleT<List<SuppSettleRule>> result = suppSupplierClientService.findSuppSettleRuleList(params);
            if (CollectionUtils.isEmpty(result.getReturnContent())) {
                info = "失败，子订单id：" + orderItem.getOrderItemId() + ",合同id:" + orderItem.getContractId() + ",供应商id:" + orderItem.getSupplierId() + "，未发现对应结算规则";
            } else {
                SettlementItem item = initItem(orderObj, orderItem, countSettlePriceMap, message);
                if (null != item) {
                        ssItems.add(item);
                        try {
                            logger.info("----订单子项详情----" + org.apache.commons.beanutils.BeanUtils.describe(item));
                        } catch (Exception e) {
                            logger.error(ExceptionFormatUtil.getTrace(e));
                        }

                        orderSettlementService.insertOrUpdateSettlementItem(ssItems,
                                EVENT_TYPE.valueOf(message.getEventType()));

                        logger.info("手动保存结算子项 子订单id：" + orderItem.getOrderItemId() + "成功");

                    info = "成功";

                }
            }


        } else {
            logger.info("不满足条件不能生成结算子项   子订单id：" + orderItem.getOrderItemId());
            info = "失败，订单只有信息和资源审核通过且全额支付且状态正常或完成才满足条件，现在条件:信息状态：" + OrderEnum.INFO_STATUS.getCnName(orderObj.getInfoStatus()) + ", 资源状态：" + OrderEnum.RESOURCE_STATUS.getCnName(orderObj.getResourceStatus()) + ",支付状态：" + OrderEnum.PAYMENT_STATUS.getCnName(orderObj.getPaymentStatus()) + ",订单状态：" + OrderEnum.ORDER_STATUS.getCnName(orderObj.getOrderStatus());
        }

        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                orderObj.getOrderId(),
                orderItemId,
                "system",
                "将编号为[" + orderItemId + "]的订单子项，手动保存结算子项" + info,
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "手动保存结算子项",
                null);

        logger.info("手动生成结算单处理结束");


    }


    private void executePasscodeApplyNotifyMsg(Message message) {
        Long orderItemId = message.getObjectId();
        logger.info("executePasscodeApplyNotifyMsg 接受消息 orderItemId=" + orderItemId + ", eventType=" + message.getEventType());
        List<Long> ids = new ArrayList<Long>();
        ids.add(orderItemId);
        List<OrdOrderItem> orderItems = iOrdOrderItemService.selectOrderItemsByIds(ids);
        Long orderId = null;
        if (orderItems != null && orderItems.size() > 0) {
            orderId = orderItems.get(0).getOrderId();
        }
        List<SetSettlementItem> setSettleList = orderSettlementService.findSetSettlementItemByParams(orderId, orderItemId);

        if (setSettleList != null) {
            logger.info("setSettleList数量:" + setSettleList.size());
        }

        if (setSettleList != null) {
            for (SetSettlementItem item : setSettleList) {
                long orderItemId_1 = item.getOrderItemProdId();
                OrdPassCode passCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItemId_1);
                if (passCode != null) {
                    item.setPassCode(passCode.getCode());// 通关码
                    item.setPassAddCode(passCode.getAddCode());//辅助码
                    item.setPassSerialno(passCode.getPassSerialno());//通关流水号
                    item.setPassExtid(passCode.getPassExtid());//供应商回调信息
                }
                logger.info("结算详细信息:" + JSONObject.fromObject(item).toString());
            }
            logger.info("开始更新结算信息通关码通关流水号");
            orderSettlementService.saveOrUpdateSetSettlementItem(setSettleList);
            logger.info("结束保存结算信息通关码通关流水号");
        }

    }

    /**
     * 子订单履行状态更新，级联更新结算系统
     *
     * @param message
     */
    private void executeItemPerformSettleMsg(Message message) {
        Long orderItemId = message.getObjectId();
        logger.info("executeItemPerformSettleMsg 接受消息 orderItemId=" + orderItemId + ", eventType=" + message.getEventType());
        OrdOrderItem orderItem = this.orderUpdateService
                .getOrderItem(orderItemId);
        OrdOrder ordOrder = getOrderWithOrderItemByOrderId(orderItem
                .getOrderId());
        ResultHandleT<List<OrdTicketPerform>> ordTicketPerformsRes = orderService
                .selectByOrderItem(orderItemId);
        List<OrdTicketPerform> ordTicketPerforms = null;
        if (ordTicketPerformsRes != null) {
            ordTicketPerforms = ordTicketPerformsRes.getReturnContent();
        }
        String performStatus = orderService.calPerformStatus(ordTicketPerforms,
                ordOrder, orderItem);
        List<Long> ids = new ArrayList<Long>();
        ids.add(orderItemId);
        List<OrdOrderItem> orderItems = iOrdOrderItemService.selectOrderItemsByIds(ids);
        Long orderId = null;
        if (orderItems != null && orderItems.size() > 0) {
            orderId = orderItems.get(0).getOrderId();

        }

        List<SetSettlementItem> setSettleList = orderSettlementService.findSetSettlementItemByParams(orderId, orderItemId);
        if (setSettleList != null) {
            for (SetSettlementItem item : setSettleList) {
                item.setPerformStatus(performStatus);
                try {
                    //增加通关时间
                    OrdTicketPerform ordTicketPerform = ordTicketPerformDao.selectByOrderItem(orderItem.getOrderItemId());
                    item.setPassTime(ordTicketPerform.getPerformTime());
                } catch (Exception e) {
                    logger.info("OrderItemId:" + orderItem.getOrderItemId() + " getordTicketPerform error:" + e.toString());
                }
            }
            logger.info("开始更新结算子订单履行状态");
            orderSettlementService.saveOrUpdateSetSettlementItem(setSettleList);
            logger.info("结束更新结算子订单履行状态");
        }
    }

    private void pushOrdItemGroupSettleFlag(Long orderId) {
        logger.info("push groupSettleFlag start orderId" + orderId);
        //ORD_ORDER_ITEM
        List<OrdOrderItem> itemList = this.orderUpdateService.queryOrderItemByOrderId(orderId);
        if (CollectionUtils.isEmpty(itemList)) {
            logger.info("get ordItemList is null orderId" + orderId);
            return;
        }
        for (OrdOrderItem item : itemList) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                String groupSettleFlag = item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.group_settle_flag.name());
                params.put("orderId", orderId);
                params.put("orderItemMetaId", item.getOrderItemId());
                params.put("groupSettleFlag", groupSettleFlag);
                if (MapUtils.isNotEmpty(params) && StringUtils.isNotBlank(groupSettleFlag)) {
                    int result = orderSettlementService.dynmicUpdateSetSettlementItem(params);
                    logger.info("update GroupSettleFlag orderItemId:" + item.getOrderItemId() + "result:" + result);
                }
            } catch (Exception e) {
                logger.error(ExceptionFormatUtil.getTrace(e));
            }

        }


    }

    /**
     * 推送价格确认状态
     *
     * @param message;
     * @return void
     */
    private void executeItemSetPriceConfirmMsg(Message message) {
        Long orderItemId = message.getObjectId();
        logger.info("executeItemSetPriceConfirmMsg 接受消息 orderItemId=" + orderItemId + ", eventType=" + message.getEventType());
        try {
            OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderId", orderItem.getOrderId());
            params.put("orderItemMetaId", orderItem.getOrderItemId());
            params.put("priceConfirmStatus", orderItem.getPriceConfirmStatus());

            if (MapUtils.isNotEmpty(params)) {
                int result = orderSettlementService.dynmicUpdateSetSettlementItem(params);
                if (result > 0) {
                    logger.info("update PriceConfirmMsg orderItemId:" + orderItemId + "success!");
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
    }

    /**
     * 子订单的退款金额
     *
     * @param orderItemId
     * @return
     */
    private Long getItemRefundAmount(Long orderItemId) {
        logger.info("getItemRefundAmount start:" + orderItemId);
        Long refundAmount = 0L;
        ResultHandleT<List<RefundOrderItemSplit>> resultHandle = orderRefundSplitServiceAdapter.queryOrdRefundmentItemSplitAllByOrderItemId(orderItemId);
        if (resultHandle.isSuccess() && null != resultHandle.getReturnContent()) {
            List<RefundOrderItemSplit> itemSplitList = resultHandle.getReturnContent();
            logger.info("getItemRefundAmount itemSplitList = " + GsonUtils.toJson(itemSplitList));
            if (CollectionUtils.isNotEmpty(itemSplitList)) {
                for (RefundOrderItemSplit refundOrderItemSplit : itemSplitList) {
                    if (null != refundOrderItemSplit.getRefundPrice()) {
                        refundAmount += refundOrderItemSplit.getRefundPrice();
                    }
                }
            }
        }
        logger.info("getItemRefundAmount end and result:" + refundAmount);
        return refundAmount;
    }

    private Map<String,Object> getBookerInfo(OrdOrder order) {
        Map<String,Object> resultMap = new HashedMap();
        int travellNum =0;
        if (order.getOrdPersonList().isEmpty()) {
            //查询book 信息
            Map<String, Object> map = new HashedMap();
            map.put("orderId", order.getOrderId());
            List<OrdPerson> personList = ordPersonService.getBookPersonInfoByOrderId(map);
            resultMap = getBookerPersonAndTravellNum(personList);
            return resultMap;
        } else {
            resultMap = getBookerPersonAndTravellNum(order.getOrdPersonList());
        }
        return resultMap;
    }

    public Map<String,Object> getBookerPersonAndTravellNum(List<OrdPerson> personList){
        List<OrdPerson> travePersons =new ArrayList<OrdPerson>();
        Map<String,Object> resultMap =new HashMap<String,Object>();
        int travellerNum = 0;
        OrdPerson bookerPerson = new OrdPerson();
        if(personList!=null && !personList.isEmpty()){
            for(OrdPerson ordPerson : personList){
                if("BOOKER".equals(ordPerson.getPersonType())){
                    bookerPerson = ordPerson;
                }
                if("TRAVELLER".equals(ordPerson.getPersonType())){
                    travePersons.add(ordPerson);
                }
            }
        }
        if(travePersons!=null && !travePersons.isEmpty()){
            travellerNum = travePersons.size();
        }
        resultMap.put("bookerPerson",bookerPerson);
        resultMap.put("travellerNum",travellerNum);
        return resultMap;
    }


}

