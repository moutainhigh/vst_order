package com.lvmama.vst.order.service.reschedule.impl;

import com.lvmama.log.util.LogTrackContext;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.service.api.ticket.IApiOrderRescheduleDateService;
import com.lvmama.order.vo.ticket.OrderItemRescheduleDateVo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.precontrol.service.ResWarmRuleClientService;
import com.lvmama.vst.back.client.prod.service.ProdCalSplitClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.po.ResWarmRule;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsReschedule;
import com.lvmama.vst.back.goods.po.SuppGoodsSplitTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.goods.utils.SuppGoodsRescheduleTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsRescheduleVO;
import com.lvmama.vst.back.order.po.OrdItemAdditionStatus;
import com.lvmama.vst.back.order.po.OrdItemReschedule;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderSharedStock;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderRescheduleInfo;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.TicketProductForOrderVO;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.RescheduleTimePrice;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkUserClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderSharedStockDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdTicketPerformDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdItemAdditionStatusService;
import com.lvmama.vst.order.service.IOrdItemRescheduleService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrdPassCodeService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdPayPromotionService;
import com.lvmama.vst.order.service.OrderPromotionService;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.service.reschedule.IOrderRescheduleService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketAddTimePriceServiceImpl;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.supp.client.service.SupplierStockCheckService;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderRescheduleServiceImpl implements IOrderRescheduleService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderRescheduleServiceImpl.class);

    @Resource(name = "supplierStockCheckService")
    private SupplierStockCheckService supplierStockCheckService;

    @Autowired
    private OrdOrderSharedStockDao ordOrderSharedStockDao;

    @Autowired
    private OrderOrderFactory orderOrderFactory;

    @Autowired
    private EbkUserClientService ebkUserClientService;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

    @Autowired
    private IOrdOrderPackService ordOrderPackService;

    @Autowired
    private OrdPayPromotionService ordPayPromotionService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private OrderPromotionService ordPromotionService;

    @Autowired
    private SuppSupplierClientService suppSupplierClientService;

    @Autowired
    private OrdOrderStockDao orderStockDao;

    @Autowired
    private OrdOrderItemDao ordOrderItemDao;

    @Autowired
    private IOrdItemAdditionStatusService ordItemAdditionStatusService;

    @Autowired
    private IOrderUpdateService orderUpdateService;

    @Autowired
    private CategoryClientService categoryClientService;

    @Autowired
    private IOrdPassCodeService ordPassCodeService;

    @Autowired
    private SupplierOrderOtherService supplierOrderOtherService;

    @Autowired
    private IOrderSendSmsService orderSendSmsService;

    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Autowired
    private DistGoodsTimePriceClientService distGoodsTimePriceClientService;

    @Autowired
    private ProdCalSplitClientService prodCalSplitClientRemote;

    @Autowired
    private ProdProductClientService prodProductClientService;

    @Autowired
    private ProdPackageGroupClientService prodPackageGroupClientService;

    @Autowired
    private IOrderUpdateService ordOrderUpdateService;

    @Autowired
    private OrdOrderStockDao ordOrderStockDao;

    @Resource(name="goodsOraTicketAddTimePriceStockService")
    private IGoodsTimePriceStockService goodsTicketAddTimePriceStockService;

    @Resource(name="goodsOraTicketNotimeTimePriceStockService")
    private IGoodsTimePriceStockService goodsTicketNotimeTimePriceStockService;
    
    @Autowired
    private IOrdItemRescheduleService ordItemRescheduleService;

    @Autowired
    private OrdTicketPerformDao ordTicketPerformDao;

    @Autowired
    private OrdOrderDao orderDao;

    @Autowired
    private IOrderLocalService orderLocalService;

    @Autowired
    private ResPreControlService resControlBudgetRemote;

    @Autowired
    private ResWarmRuleClientService resWarmRuleClientService;

    @Resource
    private TopicMessageProducer resPreControlEmailMessageProducer;

    @Autowired
    private ComPushClientService comPushClientService;

    @Autowired
    private IApiOrderRescheduleDateService apiOrderRescheduleDateService;
    /**
     * 是否可以改期
     *
     * @param order
     * @return 
     */
    @Override
    public String rescheduleFlag(OrdOrder order) {
        LOG.info("rescheduleFlag orderId:"+order.getOrderId());
            if (!isRescheduleCategory(order.getCategoryId())) {
                return Constants.N_FLAG;
            }
            Long changeCount = getSuppChangeCount(order);
            Long ordChangeCount = getOrdChangeCount(order);
            if (ordChangeCount >= changeCount) {
                LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " changeCount:" + changeCount + " ordChangeCount:" + ordChangeCount);
                return Constants.N_FLAG;
            }
            ResultHandle resultHandle = checkOrderStatus(order);
            if (resultHandle.isFail()) {
                LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " errorMsg:" + resultHandle.getMsg());
                return Constants.N_FLAG;
            }
            ResultHandleT<String> stringResultHandleT = checkRescheduleStatus(order);
            if(stringResultHandleT.isFail()){
                LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " errorMsg:" + stringResultHandleT.getMsg());
                return Constants.N_FLAG;
            }
        return Constants.Y_FLAG;
    }
    
    @Override
    public String rescheduleMsg(OrdOrder order) {
        LOG.info("rescheduleFlag orderId:"+order.getOrderId());
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        for(OrdOrderItem orderItem:orderItemList){
            if (!isRescheduleCategory(orderItem.getCategoryId())) {
                return "此订单含保险，不支持在线改期";
            }
            SuppGoodsReschedule suppGoodsReschedule = ordItemRescheduleService.toSuppGoodsReschedule(orderItem.getOrderItemId());
            if(suppGoodsReschedule == null || (!"0".equalsIgnoreCase(suppGoodsReschedule.getChangeDesc()))){
                return Constants.N_FLAG;
            }

        }
        Long changeCount = getSuppChangeCount(order);
        Long ordChangeCount = getOrdChangeCount(order);
        if (ordChangeCount >= changeCount) {
            LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " changeCount:" + changeCount + " ordChangeCount:" + ordChangeCount);
            return "订单已达最大可改期次数，不能再次在线改期";
        }
        ResultHandle resultHandle = checkOrderStatus(order);
        if (resultHandle.isFail()) {
            LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " errorMsg:" + resultHandle.getMsg());
            return Constants.N_FLAG;
        }
        ResultHandleT<String> stringResultHandleT = checkRescheduleStatus(order);
        if(stringResultHandleT.isFail()){
            LOG.info("rescheduleFlag orderId:" + order.getOrderId() + " errorMsg:" + stringResultHandleT.getMsg());
            return stringResultHandleT.getMsg();
        }
        return Constants.Y_FLAG;
    }

    public ResultHandle rescheduleCheck(Long orderId, List<SuppGoodsRescheduleVO.Item> list) {
        Log.info("rescheduleCheck orderId:"+orderId+" list:"+list);
        ResultHandle resultHandle = new ResultHandle();
        try {
            if (null == orderId) {
                LOG.info("rescheduleCheck orderId is null");
                resultHandle.setMsg("订单号为空");
                return resultHandle;
            }
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            Long changeCount = getSuppChangeCount(order);
            Long ordChangeCount = getOrdChangeCount(order);
            if (ordChangeCount >= changeCount) {
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " changeCount:" + changeCount + " ordChangeCount:" + ordChangeCount);
                resultHandle.setMsg("已超过最大改期次数");
                return resultHandle;
            }
            if ((!OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus())) || !((SuppGoods.PAYTARGET.PREPAID.getCode().equalsIgnoreCase(order.getPaymentTarget()) && OrderEnum.PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus())))) {
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " order status not normal or payment status not payed");
                resultHandle.setMsg("订单状态不正确");
                return resultHandle;
            }
            Map<Long, Date> longDateMap = filterOrderItemList(order, list);
            ResultHandle checkPriceResultHandle = checkPrice(order);
            if (checkPriceResultHandle.isFail()) {
                LOG.info("rescheduleCheck checkPrice errorMsg:" + checkPriceResultHandle.getMsg());
                return checkPriceResultHandle;
            }
            ResultHandle checkDateResultHandle = checkDate(order, longDateMap);
            if (checkDateResultHandle.isFail()) {
                LOG.info("rescheduleCheck checkDate errorMsg:" + checkPriceResultHandle.getMsg());
                return checkDateResultHandle;
            }

            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            if (null != orderItemList && orderItemList.size() > 0) {
                Map<Long, Boolean> resultMap = new HashMap<Long, Boolean>();
                StringBuilder errorMsg = new StringBuilder();
                for (OrdOrderItem ordOrderItem : orderItemList) {
                    String apiFlag = getApiFlag(ordOrderItem);
                    if ("Y".equalsIgnoreCase(apiFlag)) {
                        if (!resultMap.containsKey(ordOrderItem.getOrderItemId())) {
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("orderItemId", ordOrderItem.getOrderItemId());
                            List<OrdPassCode> ordPassCodeList = ordPassCodeService.findByParams(params);
                            if (CollectionUtils.isEmpty(ordPassCodeList)) {
                                LOG.info("rescheduleCheck orderItemId:" + ordOrderItem.getOrderItemId() + " passcode status not applied success");
                                resultHandle.setMsg("passCode status not applied success");
                                return resultHandle;
                            }
                            List<SuppOrderResult> resultList = supplierOrderOtherService.getTicketExchangeStatus(order.getOrderId(), ordOrderItem.getOrderItemId());
                            if (CollectionUtils.isNotEmpty(resultList)) {
                                for (SuppOrderResult suppOrderResult : resultList) {
                                    if (suppOrderResult.isSuccess()) {
                                        resultMap.put(suppOrderResult.getOrderItemId(), true);
                                    } else {
                                        resultMap.put(suppOrderResult.getOrderItemId(), false);
                                        errorMsg.append(suppOrderResult.getOrderItemId());
                                        errorMsg.append(": ");
                                        errorMsg.append(suppOrderResult.getErrMsg());
                                        errorMsg.append(", ");
                                    }
                                }
                            } else {
                                errorMsg.append("no SuppOrderResult");
                                resultMap.put(ordOrderItem.getOrderItemId(), false);
                            }
                        }
                    } else {
                        resultMap.put(ordOrderItem.getOrderItemId(), true);
                    }
    
                }
                if (resultMap.containsValue(new Boolean("false"))) {
                    resultHandle.setMsg(errorMsg.toString());
                    LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " received error msg from vst_passport, Msg: " + errorMsg);
                    return resultHandle;
                }
                ResultHandle rescheduleResultHandle = rescheduleCheckStock(order);
                if (!rescheduleResultHandle.isSuccess()) {
                    resultHandle.setMsg(rescheduleResultHandle.getMsg());
                    return resultHandle;
                }
            }
        } catch (Exception e) {
            LOG.error("rescheduleCheck error:"+e.getMessage(),e);
            resultHandle.setMsg(e.getMessage());
        }
        return resultHandle;
    }

    /**
     * 是否可以改期
     *
     * @param orderId
     * @return
     */
    @Override
    public String rescheduleFlag(Long orderId) {
        OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
        return rescheduleFlag(order);
    }

    public Long getSuppChangeCount(OrdOrder ordOrder) {
        List<Long> list = new ArrayList<Long>();
        for(OrdOrderItem orderItem:ordOrder.getOrderItemList()){
            SuppGoodsReschedule suppGoodsReschedule = ordItemRescheduleService.toSuppGoodsReschedule(orderItem.getOrderItemId());
            if(null!=suppGoodsReschedule && null!=suppGoodsReschedule.getChangeCount()){
                list.add(suppGoodsReschedule.getChangeCount());
            }
        }
        if(CollectionUtils.isNotEmpty(list)){
           return Collections.min(list); 
        }
        return 0L;
    }

    public Long getOrdChangeCount(OrdOrder order) {
        List<Long> countList = new ArrayList<Long>();
        if (null != order) {
            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            if (null != orderItemList && orderItemList.size() > 0) {
                for (OrdOrderItem orderItem : orderItemList) {
//                    Map<String, Object> params = new HashMap<String, Object>();
//                    params.put("orderItemId", orderItem.getOrderItemId());
//                    params.put("statusType", OrderEnum.ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
//                    params.put("status", OrderEnum.ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());
//                    List<OrdItemAdditionStatus> list = ordItemAdditionStatusService.findOrdItemAdditionStatusList(params);
                    OrdItemReschedule ordItemReschedule = ordItemRescheduleService.findOrdItemRescheduleByOrdItemId(orderItem.getOrderItemId());
                    if (null != ordItemReschedule && null != ordItemReschedule.getExchangeCount()) {
                        //Long count = ordItemReschedule.getExchangeCount();
                        LOG.info("OrderRescheduleServiceImpl getOrdChangeCount orderId:" + order.getOrderId() + " OrderItemId:" + orderItem.getOrderItemId() + " exchangeCount:" + ordItemReschedule.getExchangeCount());
                        countList.add(ordItemReschedule.getExchangeCount());
                    } else {
                        countList.add(0L);
                    }
                }
            }
        }
        return Collections.max(countList);
    }
    @Override
    public ResultHandleT<List<SuppGoodsRescheduleVO.Item>> updateOrderItemVisitTime(Long orderId, List<SuppGoodsRescheduleVO.Item> list) {
        LOG.info("updateOrderItemVisitTime orderId:"+orderId+" list:" + list);
        ResultHandleT<List<SuppGoodsRescheduleVO.Item>> resultHandleT = new ResultHandleT<List<SuppGoodsRescheduleVO.Item>>();
        List<SuppGoodsRescheduleVO.Item> returnContent = new ArrayList<SuppGoodsRescheduleVO.Item>();
        OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
        Map<Long, Date> longDateMap = filterOrderItemList(order, list);
        if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
            Set<Long> set = new HashSet<Long>();
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                Long orderItemId = orderItem.getOrderItemId();
                Date changeVisitDate = orderItem.getVisitTime();
                Date oldVisitDate = longDateMap.get(orderItemId);
                if (null != orderItemId && null != changeVisitDate) {
                    String apiFlag = getApiFlag(orderItem);
                    if ("Y".equalsIgnoreCase(apiFlag)) {
                        if (!set.contains(orderItemId)) {
                            List<SuppOrderResult> resultList = supplierOrderOtherService.ticketExchange(orderId, changeVisitDate, orderItemId);
                            if(CollectionUtils.isNotEmpty(resultList)){
                                for (SuppOrderResult suppOrderResult : resultList) {
                                    if (suppOrderResult.isSuccess()) {
                                        set.add(suppOrderResult.getOrderItemId());
                                        updateStock(returnContent, orderItem, orderItemId, changeVisitDate, oldVisitDate);
                                    } else {
                                        LOG.error("updateOrderItemVisitTime resultList errMsg:"+suppOrderResult.getErrMsg());
                                        throw new RuntimeException(suppOrderResult.getErrMsg());
                                    }
                                }
                            }else{
                                LOG.error("updateOrderItemVisitTime resultList is null");
                                throw new RuntimeException("返回结果为空");
                            }
                        }
                    } else {
                        OrdTicketPerform ordTicketPerform = ordTicketPerformDao.selectByOrderItem(orderItemId);
                        if(null!=ordTicketPerform && null==ordTicketPerform.getPerformTime()){
                            ordTicketPerform.setVisitTime(changeVisitDate);
                            ordTicketPerformDao.updateByPrimaryKeySelective(ordTicketPerform);
                        }
                        updateStock(returnContent, orderItem, orderItemId, changeVisitDate, oldVisitDate);
                    }

                }
            }
            //更新次数
            for(SuppGoodsRescheduleVO.Item item:list){
                //addOrderItemAdditionStatus(item.getOrderItemId());
                ordItemRescheduleService.updateExchangeCountByOrdItemId(item.getOrderItemId());
            }
//            LOG.info("updateOrderItemVisitTime buyout start");
//            boolean ret = orderLocalService.updateResBackToPrecontrol(orderId);
//            LOG.info("updateOrderItemVisitTime buyout end" + ret );
            updateOrderVisitTime(orderId,list);
            //添加改期记录
            RequestBody<OrderItemRescheduleDateVo> requestBody = new RequestBody<>();
            OrderItemRescheduleDateVo vo;
            for(SuppGoodsRescheduleVO.Item item:list){
                vo = new OrderItemRescheduleDateVo();
                vo.setOrderId(orderId);
                vo.setOrderItemId(item.getOrderItemId());
                vo.setUserNo("");
                vo.setRescheduleType("");
                vo.setPreviousDate(item.getOldVisitDate());
                vo.setRescheduleDate(item.getChangeVisitDate());
                requestBody.setT(vo);
                ResponseBody<Integer> responseBody = apiOrderRescheduleDateService.saveOrdRescheduleDate(requestBody);
                LOG.info(orderId+"___"+ item.getOrderItemId()+ "gaiqi改期reps="+ responseBody);
            }
            //发送短信
            sendRescheduleSms(order);
        }
        LOG.info("updateOrderItemVisitTime returnContent:"+returnContent);
        resultHandleT.setReturnContent(returnContent);
        return resultHandleT;
    }

    private void updateOrderVisitTime(Long orderId, List<SuppGoodsRescheduleVO.Item> list) {
        List<OrdOrderItem> itemList = ordOrderItemDao.selectByOrderId(orderId);
        boolean updateOrderFlag = true;
        Date changeOrdVisitDate = null;
        for (SuppGoodsRescheduleVO.Item item : list) {
            if (changeOrdVisitDate == null) {
                changeOrdVisitDate = item.getChangeVisitDate();
            } else {
                if (!changeOrdVisitDate.equals(item.getChangeVisitDate())) {
                    return;
                }
            }
        }
        for (OrdOrderItem ordOrderItem : itemList) {
            if (changeOrdVisitDate != null && !changeOrdVisitDate.equals(ordOrderItem.getVisitTime())) {
                return;
            }
        }
        OrdOrder updateOrder = new OrdOrder();
        updateOrder.setVisitTime(changeOrdVisitDate);
        updateOrder.setOrderId(orderId);
        orderDao.updateByPrimaryKeySelective(updateOrder);
    }

    private void updateStock(List<SuppGoodsRescheduleVO.Item> returnContent, OrdOrderItem orderItem, Long orderItemId, Date changeVisitDate, Date oldVisitDate) {
        //返还库存
        orderItem.setVisitTime(oldVisitDate);
        //buildBuyoutOrderItem(orderItem);
        if("Y".equals(orderItem.getBuyoutFlag())){
            revertBuyout(orderItem);
        }else{
            revertStock(orderItem,changeVisitDate);
        }
        //扣减库存
        orderItem.setVisitTime(changeVisitDate);
        buildBuyoutOrderItem(orderItem);
        if("Y".equals(orderItem.getBuyoutFlag())){
            deductBuyout(orderItem);
        }else{
            deductStock(orderItem);
        }
        OrdOrderItem updateItem = new OrdOrderItem();
        updateItem.setOrderItemId(orderItemId);
        updateItem.setVisitTime(changeVisitDate);
        if(StringUtils.equalsIgnoreCase("Y",orderItem.getBuyoutFlag())){
            updateItem.setBuyoutPrice(orderItem.getBuyoutPrice());
            updateItem.setBuyoutQuantity(orderItem.getBuyoutQuantity());
            updateItem.setBuyoutTotalPrice(orderItem.getBuyoutTotalPrice()); 
        }
        updateItem.setBuyoutFlag(orderItem.getBuyoutFlag());
        ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
        SuppGoodsRescheduleVO.Item rescheduleRes = new SuppGoodsRescheduleVO.Item();
        rescheduleRes.setChangeVisitDate(changeVisitDate);
        rescheduleRes.setOrderItemId(orderItemId);
        rescheduleRes.setOldVisitDate(oldVisitDate);
        returnContent.add(rescheduleRes);
    }
    private void revertBuyout(OrdOrderItem orderItem){
            LOG.info("revertBuyout "+ orderItem.getOrderItemId() +"start ");
            Long suppGoodsId = orderItem.getSuppGoodsId();
            Date visitDate = orderItem.getVisitTime();
            GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = null;
            boolean ret = true;
            goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(suppGoodsId, visitDate);
            ret = updatePrecontrolResource(orderItem,goodsResPrecontrolPolicyVO, orderItem.getBuyoutQuantity(), orderItem.getBuyoutTotalPrice(),visitDate);
            if(ret == false){
                throw new RuntimeException("revertBuyout failed ");
            }
            LOG.info("revertBuyout "+ orderItem.getOrderItemId() +"end ");
    }
    private boolean updatePrecontrolResource(OrdOrderItem orderItem ,GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO,Long buyoutQuantity ,Long buyoutTotalAmount,Date visitDate ){
        boolean ret =false;
        boolean reduceResult = false;
        if(goodsResPrecontrolPolicyVO!=null ){
            List<Long> goodList = null;
            List<Date> dateList = null;
            Long controlId = goodsResPrecontrolPolicyVO.getId();
            String resType = goodsResPrecontrolPolicyVO.getControlType();
            boolean isSaledOver = false;
            Long goodsId = goodsResPrecontrolPolicyVO.getSuppGoodsId();
            //购买该商品的数量
            Long reduceNum = buyoutQuantity;
            reduceNum = reduceNum==null ? 0: reduceNum;
            Long buyoutTotalPrice = buyoutTotalAmount;
            buyoutTotalPrice = buyoutTotalPrice==null ?0L:buyoutTotalPrice;
            if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equalsIgnoreCase(resType)){
                Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
                //该商品在该时间内的剩余金额
                Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
                isSaledOver = leftAmount==null? false:leftAmount==0;
                leftAmount = leftAmount==null? 0L:leftAmount;
                Long leftValue = leftAmount + buyoutTotalPrice;
                leftValue = leftValue< 0? 0L:leftValue;

                if(leftValue>goodsResPrecontrolPolicyVO.getAmount()){
                    //退回的时候，不能超过原先设置的大小
                    leftValue = goodsResPrecontrolPolicyVO.getAmount();
                }
                if(buyoutTotalPrice > 0){
                    reduceResult = resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
                }else{
                    LOG.info("不需要退回到预控资源中去");
                }
                if(reduceResult == false){
                    LOG.error("退回买断资源失败"+ goodsId + ",按日预控，日剩余量ID:" + amountId);
                }
            }else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equalsIgnoreCase(resType)){
                Long storeId = goodsResPrecontrolPolicyVO.getStoreId();
                //该商品在该时间内的剩余库存
                Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
                isSaledOver = leftQuantity==null? false:leftQuantity==0;
                leftQuantity = leftQuantity==null ? 0L:leftQuantity;


                Long leftStore = leftQuantity + reduceNum;
                leftStore = leftStore < 0? 0L:leftStore;
                if(leftStore>goodsResPrecontrolPolicyVO.getAmount()){
                    //退回的时候，不能超过原先设置的大小
                    leftStore = goodsResPrecontrolPolicyVO.getAmount();
                }
                if(reduceNum > 0){
                    //按库存预控
                    reduceResult = resControlBudgetRemote.updateStoreResPrecontrolPolicy(storeId,controlId, visitDate, leftStore);
                    if(reduceResult == false){
                        LOG.error("退回买断资源失败"+ goodsId + ",按日预控，日库存ID:" + storeId);
                    }
                }
            }
            String  logStr = "改期订单成功，退回资源到预控中去成功，订单号："+orderItem.getOrderId()+"子订单号："+orderItem.getOrderItemId()+",商品id:"+orderItem.getSuppGoodsId()+",日期："+new SimpleDateFormat("yyyy-MM-dd").format(visitDate)+"，数量："+buyoutQuantity+",总价："+buyoutTotalAmount;
            if(reduceResult){
                LOG.info(logStr);
                //如果退回之前发现是true,也就是退回之前是0，那么退回之后要重新计算价格：因为有买断的商品存在了
                if(isSaledOver){
                    goodList = new ArrayList<Long>();
                    goodList.add(goodsId);
                    dateList = new ArrayList<Date>();
                    String dayOrCycle = goodsResPrecontrolPolicyVO.getControlClassification();
                    if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equals(dayOrCycle)){
                        dateList.clear();
                        Date  begin = goodsResPrecontrolPolicyVO.getTradeEffectDate();
                        Date  end = goodsResPrecontrolPolicyVO.getTradeExpiryDate();
                        while(begin.compareTo(end)<=0){
                            dateList.add(begin);
                            begin = DateUtil.addDays(begin, 1);
                        }
                    }else{
                        dateList.clear();
                        dateList.add(visitDate);
                    }


                    try{
                        ResultHandleT<Integer> resultT = comPushClientService.pushTimePrice(goodList, dateList, ComIncreament.DATA_SOURCE_TYPE.CAL_BUSNINESS_DATA_JOB, true);
                        Integer result = resultT.getReturnContent();
                        if(result!=null && result.intValue()>0){
                            LOG.info(goodsId + "买断库存卖完后，退回了，发送消息进行变价，发送消息OK");
                        }
                        LOG.info(logStr);
                        //comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), orderItem.getOrderItemId(), "SYSTEM", logStr, ComLog.COM_LOG_LOG_TYPE.RES_PRECONTROL_POLICY_CHANGE.getCnName(), "子订单退回预控资源", "");
                    }catch(Exception e){
                        LOG.error(goodsId + "买断库存买完后退回了，发送消息进行变价，发送消息失败");
                    }
                }
            }else{
                LOG.error("退回买断资源失败！");
            }
        }else{
            LOG.error(orderItem.getSuppGoodsId() + ","+(visitDate.getMonth()+1)+""+visitDate.getDate() +"该商品在这一天的对应的策略找不到了！！");
        }
        ret = reduceResult;
        return ret;
    }
    
    private void addOrderItemAdditionStatus(Long itemId) {
        OrdItemAdditionStatus ordItemAdditionStatus = new OrdItemAdditionStatus();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderItemId", itemId);
        params.put("statusType", OrderEnum.ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
        params.put("status", OrderEnum.ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());

        List<OrdItemAdditionStatus> list = ordItemAdditionStatusService.findOrdItemAdditionStatusList(params);
        if (CollectionUtils.isNotEmpty(list)) {
            ordItemAdditionStatus = list.get(0);
            ordItemAdditionStatus.setExchangeCount((ordItemAdditionStatus.getExchangeCount() == null ? 0 : (ordItemAdditionStatus.getExchangeCount() + 1)));
            ordItemAdditionStatusService.updateByPrimaryKeySelective(ordItemAdditionStatus);
        } else {
            ordItemAdditionStatus.setOrderItemId(itemId);
            ordItemAdditionStatus.setStatus(OrderEnum.ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());
            ordItemAdditionStatus.setStatusType(OrderEnum.ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
            ordItemAdditionStatus.setExchangeCount(1L);
            ordItemAdditionStatusService.addOrdItemAdditionStatus(ordItemAdditionStatus);
        }

    }
    private String getApiFlag(OrdOrderItem orderItem) {
        String apiFlag = "N";
        if (null != orderItem) {
            Map<String, Object> contentMap = orderItem.getContentMap();
            String categoryType = (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
            ResultHandleT<BizCategory> result = categoryClientService.findCategoryByCode(categoryType);
            BizCategory bizCategory = result.getReturnContent();
            if (bizCategory.getParentId() != null && bizCategory.getParentId().equals(5L)) {

                if (StringUtils.equals(orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()),
                        SuppGoods.NOTICETYPE.QRCODE.name())) {

                    apiFlag = "Y";
                }
            }
        }
        return apiFlag;
    }

    /**
     * 校验库存(复制下单)
     *
     * @param order
     * @return
     */
    public ResultHandle rescheduleCheckStock(OrdOrder order) {
        String methodName = "OrderRescheduleServiceImpl#rescheduleCheckStock";
        Long startTime = System.currentTimeMillis();
        ResultHandleT<SupplierProductInfo> handleContent = this.checkStock(order);
        Log.info(ComLogUtil.printTraceInfo(methodName, "检查商品库存情况", "OrderRescheduleServiceImpl.checkStock", System.currentTimeMillis() - startTime));

        LOG.info("OrderRescheduleServiceImpl rescheduleCheckStock handleContent=" + JSONArray.fromObject(handleContent).toString());
        LOG.info("OrderRescheduleServiceImpl rescheduleCheckStock isSuccess=" + handleContent.isSuccess() + ",msg=" + handleContent.getMsg());
        if (handleContent.isSuccess()) {
            SupplierProductInfo supplierProductInfo = handleContent.getReturnContent();
            if (supplierProductInfo != null) {
                LOG.info("local:------OrderRescheduleServiceImpl.checkStock: handleContent.hasNull=" + handleContent.hasNull() + ", supplierProductInfo.isEmpty=" + supplierProductInfo.isEmpty());
                if (!handleContent.hasNull() && !supplierProductInfo.isEmpty()) {
                    startTime = System.currentTimeMillis();
                    BuyInfo buyInfo = new BuyInfo();//TODO
                    handleContent = supplierStockCheckService.checkStock(buyInfo, supplierProductInfo);
                    Log.info(ComLogUtil.printTraceInfo(methodName, "库存检查，并且返回库存检查无库存不足的产品", "supplierStockCheckService.checkStock", System.currentTimeMillis() - startTime));
                    LOG.info("server:------This log tell us that inventory has been conducted, ID【" + buyInfo.getProductId() + "】调用第三方服务检查库存返回结果详细：for buyInfo  the result is" + JSONArray.fromObject(handleContent).toString());
                }
            } else {
                LOG.info("OrderRescheduleServiceImpl.checkStock: supplierProductInfo=null");
            }
        }
        return handleContent;
    }

    private ResultHandle checkPrice(OrdOrder order) {
        ResultHandle resultHandle = new ResultHandle();
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("商品有差价,");
        boolean flag = true;
        Long productId = order.getProductId();
        Long categoryId = order.getCategoryId();
        Long distributorId = order.getDistributorId();
        OrdOrderPack ordOrderPack = order.getOrdOrderPack();
        for (OrdOrderItem orderItem : order.getOrderItemList()) {
            boolean packageFlag = false;
            SuppGoodsAddTimePrice returnContent = null;
            if( BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId) && null!=ordOrderPack&&ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(ordOrderPack.getOwnPack())){
                packageFlag = true;
            }
            ResultHandleT<SuppGoodsAddTimePrice> suppGoodsTicketResultHandle = suppGoodsTimePriceClientService.getSuppGoodsTicketTimePriceList(orderItem.getSuppGoodsId(), orderItem.getVisitTime());//改期后的日期
            if(null!=suppGoodsTicketResultHandle && !suppGoodsTicketResultHandle.hasNull()){
                returnContent =  suppGoodsTicketResultHandle.getReturnContent();
            }
            if (null != returnContent) {
                if(packageFlag){
                    buildSplitPrice(returnContent,productId,orderItem.getSuppGoodsId());
                }
                LOG.info("checkPrice orderItemId:"+orderItem.getOrderItemId()+" returnContent:"+returnContent);
                Long price = returnContent.getPrice();
                Long oldPrice = orderItem.getPrice();
                if (!oldPrice.equals(price)) {
                    flag = false;
                    errorMsg.append("orderItemId:" + orderItem.getOrderItemId()).append(ComLogUtil.getLogTxt("价格", returnContent.getPriceYuan(), orderItem.getPriceYuan()));
                }
            } else {
                flag = false;
                errorMsg.append("orderItemId:" + orderItem.getOrderItemId()).append(orderItem.getVisitTime()).append(" 无库存");
            }
        }
        if (!flag) {
            resultHandle.setMsg(errorMsg.toString());
        }
        return resultHandle;
    }

    /**
     * 检查游玩时间和最晚可改时间
     *
     * @param order
     * @return
     */
    private ResultHandle checkDate(OrdOrder order, Map<Long, Date> longDateMap) {
        ResultHandle resultHandle = new ResultHandle();
        StringBuilder errorMsg = new StringBuilder();
        Date currentDate = new Date();
        boolean flag = true;
        Map<Long, Object> promotionDateMap = this.getPromotionDate(order);
        for(OrdOrderItem orderItem : order.getOrderItemList()){
            SuppGoodsReschedule suppGoodsReschedule = ordItemRescheduleService.toSuppGoodsReschedule(orderItem.getOrderItemId());
            if(null!=suppGoodsReschedule){
                Long latestChangeTime = suppGoodsReschedule.getLatestChangeTime();
                Date date = longDateMap.get(orderItem.getOrderItemId());
                Date latestChangeDate = DateUtils.addMinutes(date, -latestChangeTime.intValue());
                if(currentDate.after(latestChangeDate)){
                    flag = false;
                    LOG.info("OrderItemId:" + orderItem.getOrderItemId() + ", checkDate failed, latestChangeDate:" + DateFormatUtils.format(latestChangeDate,"yyyy-MM-dd HH:mm:ss"));
                    errorMsg.append("此订单最晚改期时间：").append(DateFormatUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss"));
                    break;
                }
                Object obj = promotionDateMap.get(orderItem.getOrderItemId());
                if(null!=obj){
                    Date start = ((Map<String, Date>) obj).get("start");
                    Date end = ((Map<String, Date>) obj).get("end");
                    if(!(end.getTime()>=start.getTime())){
                        flag = false;
                        LOG.info("OrderItemId:" + orderItem.getOrderItemId() + ", checkDate failed, start:" + DateFormatUtils.format(start,"yyyy-MM-dd HH:mm:ss") + ", end:" + DateFormatUtils.format(end,"yyyy-MM-dd HH:mm:ss"));
                        errorMsg.append("日期检查异常");
                        break;
                    }
                    if(orderItem.getVisitTime().after(end) || orderItem.getVisitTime().before(start)){
                        flag = false;
                        LOG.info("OrderItemId:" + orderItem.getOrderItemId() + ", checkDate failed, date range:" + DateFormatUtils.format(start,"yyyy-MM-dd HH:mm:ss") + "~" + DateFormatUtils.format(end,"yyyy-MM-dd HH:mm:ss") + ", changeVisitDate:" + DateFormatUtils.format(orderItem.getVisitTime(),"yyyy-MM-dd HH:mm:ss"));
                        errorMsg.append("此订单可改日期范围：").append(DateFormatUtils.format(start,"yyyy-MM-dd HH:mm:ss"))
                                .append(" ~ ").append(DateFormatUtils.format(end,"yyyy-MM-dd HH:mm:ss"));
                        break;
                    }
                }
            }
            }
        if(!flag){
            resultHandle.setMsg(errorMsg.toString());
        }
        return resultHandle;
    }
    private ResultHandle checkOrderStatus(OrdOrder order){
        ResultHandle resultHandle = new ResultHandle();
        if ((!OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus())) || !(OrderEnum.PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus()))) {
            LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " order status not normal or payment status not payed");
            resultHandle.setMsg("订单状态不正确");
            return resultHandle;
        }
        if(!order.hasInfoAndResourcePass()){
            LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " order infoAndResourcePass false");
            resultHandle.setMsg("资源或信息审核中");
            return resultHandle;
        }
        if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
            Long productId = order.getProductId();
            ResultHandleT<ProdProduct> prodProductBy = prodProductClientService.getProdProductBy(productId);
            if(prodProductBy!=null && !prodProductBy.hasNull()){
                ProdProduct returnContent = prodProductBy.getReturnContent();
                if(!"Y".equalsIgnoreCase(returnContent.getCancelFlag()) || !"Y".equalsIgnoreCase(returnContent.getSaleFlag())){
                    LOG.info("rescheduleCheck orderId:" + order.getOrderId() +"productId="+productId+" cancelFlag="+returnContent.getCancelFlag()+" saleFlag="+returnContent.getSaleFlag());
                    resultHandle.setMsg("产品无效或不可售");
                    return resultHandle;
                }
            }else{
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " prodProductBy is null");
                resultHandle.setMsg("产品不存在");
                return resultHandle;
            }
        }
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        for(OrdOrderItem orderItem:orderItemList){
            String performStatus = orderItem.getPerformStatus();
            if(StringUtils.isNotBlank(performStatus) && !OrderEnum.PERFORM_STATUS_TYPE.UNPERFORM.name().equals(performStatus)){
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+"is used");
                resultHandle.setMsg("子订单:"+orderItem.getOrderItemId()+"已使用");
                resultHandle.setErrorCode(SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_6.getCode());
                break;
            }
            if(!isEbkAndEnterInTime(orderItem)){
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+" is not EbkAndEnterInTime");
                resultHandle.setMsg("子订单:"+orderItem.getOrderItemId()+"商品不是EBK可及时通关");
                break;
            }
            if("Y".equals(getApiFlag(orderItem))){
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+"apiFlag=Y");
                resultHandle.setMsg("子订单:+orderItem.getOrderItemId()+商品为对接商品");
                break;
            }
            ResultHandleT<ProdProduct> prodProductResult = prodProductClientService.getProdProductBy(orderItem.getProductId());
            if(prodProductResult!=null && !prodProductResult.hasNull()){
                ProdProduct returnContent = prodProductResult.getReturnContent();
                if(!"Y".equalsIgnoreCase(returnContent.getCancelFlag())){
                    LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+"productId="+orderItem.getProductId()+" cancelFlag="+returnContent.getCancelFlag());
                    resultHandle.setMsg("子订单:" + orderItem.getOrderItemId()+"产品无效");
                    break;  
                }
            }else{
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+"prodProductResult is null");
                resultHandle.setMsg("子订单:"+orderItem.getOrderItemId()+"产品不存在");
                break;
            }
            ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId());
            if(suppGoodsResultHandleT!=null && !suppGoodsResultHandleT.hasNull()){
                SuppGoods returnContent = suppGoodsResultHandleT.getReturnContent();
                if(!"Y".equalsIgnoreCase(returnContent.getOnlineFlag()) || !"Y".equalsIgnoreCase(returnContent.getCancelFlag())){
                    LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+" onlineFlag="+returnContent.getOnlineFlag()+" cancelFalg="+returnContent.getCancelFlag());
                    resultHandle.setMsg("子订单:"+orderItem.getOrderItemId()+"商品不可售");
                    break;
                }
            }else{
                LOG.info("rescheduleCheck orderId:" + order.getOrderId() + " orderItemId:"+orderItem.getOrderItemId()+"suppGoodsResultHandleT is null");
                resultHandle.setMsg("子订单:"+orderItem.getOrderItemId()+"商品存在");
                break;
            }
            
        }
        return resultHandle;
    }

    private ResultHandleT<String> checkRescheduleStatus(OrdOrder order) {
        ResultHandleT<String> resultHandle = new ResultHandleT<String>();
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        String changeTipStr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(calendar.MILLISECOND, 0);
        Date latestDate = null;
        Long latestTime = null;
        Long count = null;
        String template;
        String name;
        int priority=0;
        int[] priorityArr = new int[]{0,1,9,10};
        Map<String, Object> map = new HashMap<String, Object>();
        boolean flag = true;
        for (OrdOrderItem orderItem : orderItemList) {
            Date visitTime = orderItem.getVisitTime();
            SuppGoodsReschedule suppGoodsReschedule = ordItemRescheduleService.toSuppGoodsReschedule(orderItem.getOrderItemId());
            List<SuppGoodsRefund> ticketRefund = suppGoodsClientService.getTicketRefund(orderItem.getSuppGoodsId());
            if(orderItem.hasTicketAperiodic()){
                suppGoodsReschedule=null; 
            }
            if (suppGoodsReschedule!=null && StringUtils.isNotBlank(suppGoodsReschedule.getChangeDesc())) {
                LOG.info("checkRescheduleStatus orderId:" + order.getOrderId() + " orderItemId:" + orderItem.getOrderItemId() + " suppGoodsReschedule" + suppGoodsReschedule);
                String changeDesc = suppGoodsReschedule.getChangeDesc();
                Long latestChangeTime = suppGoodsReschedule.getLatestChangeTime();
                Date date1 = DateUtils.addMinutes(visitTime, -latestChangeTime.intValue());
                Long changeCount = suppGoodsReschedule.getChangeCount();
                if ("0".equals(changeDesc)) {
                    if(priorityArr[Integer.parseInt(changeDesc)]>=priority){
                        if (latestDate == null) {
                            latestDate = date1;
                            latestTime = latestChangeTime;
                        } else {
                            if (date1.compareTo(latestDate) < 0) {
                                latestDate = date1;
                                latestTime = latestChangeTime;
                            }
                        }
                    }
                } else if ("1".equals(changeDesc)) {
                    if(priorityArr[Integer.parseInt(changeDesc)]>=priority){
                        flag = false;
                        priority = priorityArr[Integer.parseInt(changeDesc)];
                        if (latestTime == null) {
                            latestDate = date1;
                            latestTime = latestChangeTime;
                        } else {
                            if (date1.compareTo(latestDate) < 0) {
                                latestDate = date1;
                                latestTime = latestChangeTime;
                            }
                        }
                        if (count == null) {
                            count = changeCount;
                        } else {
                            if (changeCount < count) {
                                count = changeCount;
                            }
                        }
                        resultHandle.setMsg("不能改期,changeDesc:1");
                    }
                } else if ("2".equals(changeDesc)) {
                    if(priorityArr[Integer.parseInt(changeDesc)]>priority){
                        flag = false;
                        priority = priorityArr[Integer.parseInt(changeDesc)];
                        template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_2.getTemplate();
                        changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, null);
                        resultHandle.setMsg("不能改期,changeDesc:2");
                        break;
                    }
                } else if ("3".equals(changeDesc)) {
                    if(priorityArr[Integer.parseInt(changeDesc)]>priority){
                        flag = false;
                        priority = priorityArr[Integer.parseInt(changeDesc)];
                        template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_3.getTemplate();
                        changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, null);
                        resultHandle.setMsg("不能改期,changeDesc:3");
                    }
                }else {
                    resultHandle.setMsg("不能改期,changeDesc:null");
                    break;
                }
            } else {
                if (null != ticketRefund && ticketRefund.size() > 0) {
                    SuppGoodsRefund suppGoodsRefund = ticketRefund.get(0);
                    if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefund.getCancelStrategy()) && priorityArr[2]>priority) {
                        priority = priorityArr[2];
                        template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_2.getTemplate();
                        changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, null);
                        resultHandle.setMsg("ticketRefund:UNRETREATANDCHANGE");
                        break;
                    }
                    if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefund.getCancelStrategy()) && priorityArr[3]>priority) {
                        priority = priorityArr[3];
                        template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_3.getTemplate();
                        changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, null);
                        resultHandle.setMsg("ticketRefund:RETREATANDCHANGE");
                    }
                }
            }
        }
        if (flag && latestDate != null) {
            if (latestDate.compareTo(calendar.getTime()) >= 0) {
                template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_0.getTemplate();
                map.clear();
                map.put("timeInfo", DateFormatUtils.format(latestDate, "yyyy年MM月dd日 HH:mm"));
            } else {
                template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_5.getTemplate();
                resultHandle.setMsg("此订单已过最晚改期时间");
                resultHandle.setErrorCode(SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_5.getCode());
            }
            changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, map);
        }
        if (!flag && latestTime != null && count > 0 && 1==priority) {
            if (latestDate.compareTo(calendar.getTime()) >= 0) {
                template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_1.getTemplate();
                map.clear();
                map.put("timeInfo", SuppGoodsRescheduleTools.getTimeInfo(latestTime));
                map.put("count",count);
                resultHandle.setMsg("不能改期,changeDesc:1");
            } else {
                template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_5.getTemplate();
                resultHandle.setMsg("此订单已过最晚改期时间");
                resultHandle.setErrorCode(SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_5.getCode());
            }
            changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, map);
        }
        resultHandle.setReturnContent(changeTipStr);
        return resultHandle;
    }

    /**
     * 获取促销活动游玩时间
     *
     * @param order
     * @return
     */
    public Map<Long, Object> getPromotionDate(OrdOrder order) {
        Date start = DateUtil.clearDateAfterDay(new Date());
        Date end = DateUtils.addYears(start, 1);
        Map<Long, Object> result = new HashMap<Long, Object>();
        if ("PAY_PROMOTION".equals(order.getOrderSubType())) {
            try {
                OrdPayPromotion ordPayPromotion = ordPayPromotionService.queryOrdPayPromotionByOrderId(order.getOrderId());
                String url = "/payment/ChannelRuleVo/" + ordPayPromotion.getPayPromotionId();
                SuppGoodsRescheduleVO.ChannelRuleVo forObject = RestClient.getClient().getForObject(Constant.getInstance().getPayPromotionBaseUrl() + url, SuppGoodsRescheduleVO.ChannelRuleVo.class);
                Date tourFromDate = forObject.getTourFromDate();
                Date tourThruDate = forObject.getTourThruDate();
                if (null != tourFromDate) {
                    start = tourFromDate;
                }
                if (null != tourThruDate) {
                    end = tourThruDate;
                }
            } catch (Exception e) {
                Log.info("getPromotionDate error:" + e.getMessage(), e);
            }
        }
        //初始化
        for (OrdOrderItem orderItem : order.getOrderItemList()) {
            Map<String, Date> dateMap = new HashMap<String, Date>();
            dateMap.put("start", start);
            dateMap.put("end", end);
            result.put(orderItem.getOrderItemId(), dateMap);
        }
        
        List<OrdOrderItem> orderItems = order.getOrderItemList();
        List<Long> orderItemIdList = new ArrayList<Long>();
        Map<String,Object> params = new HashMap<String, Object>();
        for (OrdOrderItem orderItem : orderItems) {
            orderItemIdList.add(orderItem.getOrderItemId());
        }
        Map<String, Object> paramPack = new HashMap<String, Object>();
        paramPack.put("orderId", order.getOrderId());
        List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
        if (!orderPackList.isEmpty()) {
            List<Long> orderItemIdList1 = new ArrayList<Long>();
            for (OrdOrderPack ordOrderPack : orderPackList) {
                orderItemIdList1.add(ordOrderPack.getOrderPackId());
            }
            params.put("objectType1","ORDER_PACK");
            params.put("orderItemIdList1", orderItemIdList1);
        }
        params.put("objectType","ORDER_ITEM");
        params.put("orderItemIdList", orderItemIdList);
        List<OrdPromotion> ordPromotions = ordPromotionService.selectOrdPromotionsByOrderItemId(params);
        
        if (null != ordPromotions && ordPromotions.size() > 0) {
            for (OrdPromotion ordPromotion : ordPromotions) {
                Object obj = result.get(ordPromotion.getOrderItemId());
                if (null == obj) {
                    continue;
                }
                Map<String, Date> dateMap = (Map<String, Date>) result.get(ordPromotion.getOrderItemId());
                Date orderItemStart = dateMap.get("start");
                Date orderItemEnd = dateMap.get("end");
                Long promPromotionId = ordPromotion.getPromPromotionId();
                PromPromotion promPromotion = promotionService.getPromPromotionById(promPromotionId);
                Date startVistTime = promPromotion.getStartVistTime();
                Date endVistTime = promPromotion.getEndVistTime();
                if (null != startVistTime) {
                    long ms = orderItemStart.getTime() - startVistTime.getTime();
                    if (ms < 0) {
                        dateMap.put("start", startVistTime);
                    }
                }
                if (null != endVistTime) {
                    long ms = (orderItemEnd.getTime() - endVistTime.getTime());
                    if (ms > 0) {
                        dateMap.put("end", endVistTime);
                    }
                }
                result.put(ordPromotion.getOrderItemId(), dateMap);
            }
        }
        return result;
    }

    /**
     * @param order
     * @return
     */
    private ResultHandleT<SupplierProductInfo> checkStock(OrdOrder order) {
        ResultHandleT<SupplierProductInfo> result = new ResultHandleT<SupplierProductInfo>();
        Long distributionId = order.getDistributorId();
        SupplierProductInfo supplierProductInfo = null;
        String methodName = "OrderRescheduleServiceImpl#checkStock【" + order.getProductId() + "】";
        Long startTime;
        try {
            ResultHandleT<SuppGoods> resultHandleSuppGoods;
            OrderTimePriceService orderTimePriceService;
            SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
            suppGoodsParam.setProduct(true);
            suppGoodsParam.setProductBranch(true);
            suppGoodsParam.getProductParam().setBizCategory(true);
            suppGoodsParam.setSupplier(true);
            if (CollectionUtils.isEmpty(order.getOrderItemList())) {
                result.setMsg("订单结构不正常");
                return result;
            }

            Map<Long, Long> shareTotalStockMap = new HashMap<Long, Long>();
            Map<Long, Long> shareDayLimitMap = new HashMap<Long, Long>();
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
//                if (orderLvfTimePriceServiceImpl.isLvfItemByCatetory(orderItem)) {
//                    continue;
//                }

                resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), suppGoodsParam);
                if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
                    SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
                    if (suppGoods.isValid()) {
                        orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
                        BuyInfo.Item item = new BuyInfo.Item();
                        item.setGoodsId(orderItem.getSuppGoodsId());
                        item.setQuantity(Integer.parseInt(String.valueOf(orderItem.getQuantity())));
                        item.setVisitTime(DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd HH:mm:ss"));

                        //对于普通门票，需要校验共享总库存&日限制
                        if (orderTimePriceService instanceof OrderTicketAddTimePriceServiceImpl) {
                            LOG.info("库存共享数据汇总");
                            ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = orderTimePriceService.getTimePrice(suppGoods.getSuppGoodsId(), orderItem.getVisitTime(), true);
                            if (timePriceHandle != null && !timePriceHandle.hasNull()) {
                                SuppGoodsBaseTimePrice timePrice = timePriceHandle.getReturnContent();
                                if (timePrice != null && timePrice instanceof SuppGoodsAddTimePrice) {
                                    SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice) timePrice;
                                    //对共享总库存进行汇总
                                    if (addTimePrice.getShareTotalStockId() > 0) {
                                        if (shareTotalStockMap.containsKey(addTimePrice.getShareTotalStockId())) {
                                            item.setShareTotalStock(shareTotalStockMap.get(addTimePrice.getShareTotalStockId())
                                                    + item.getCheckStockQuantity());
                                            shareTotalStockMap.put(addTimePrice.getShareTotalStockId(), shareTotalStockMap.get(addTimePrice.getShareTotalStockId()) + item.getCheckStockQuantity());
                                        } else {
                                            item.setShareTotalStock(item.getCheckStockQuantity());
                                            shareTotalStockMap.put(addTimePrice.getShareTotalStockId(), item.getCheckStockQuantity());
                                        }
                                    }

                                    //对共享日限制进行汇总
                                    if (addTimePrice.getShareDayLimitId() > 0) {
                                        if (shareDayLimitMap.containsKey(addTimePrice.getShareDayLimitId())) {
                                            item.setShareDayLimit(shareDayLimitMap.get(addTimePrice.getShareDayLimitId())
                                                    + item.getCheckStockQuantity());
                                            shareDayLimitMap.put(addTimePrice.getShareDayLimitId(), shareDayLimitMap.get(addTimePrice.getShareDayLimitId()) + item.getCheckStockQuantity());
                                        } else {
                                            item.setShareDayLimit(item.getCheckStockQuantity());
                                            shareDayLimitMap.put(addTimePrice.getShareDayLimitId(), item.getCheckStockQuantity());
                                        }
                                    }
                                }
                            }
                        }
                        startTime = System.currentTimeMillis();
                        ResultHandleT<Object> resultHandleObject = orderTimePriceService.checkStock(suppGoods, item, distributionId, null);
                        Log.info(ComLogUtil.printTraceInfo(methodName, "库存检查",
                                "orderTimePriceService.checkStock", System.currentTimeMillis() - startTime));

                        LOG.info("正在进行库存检查，对单订单子项(" + JSONArray.fromObject(item) + ")本地检查结果为" + JSONArray.fromObject(resultHandleObject));
                        if (resultHandleObject.isSuccess()) {
                            if (!resultHandleObject.hasNull()) {
                                Object obj = resultHandleObject.getReturnContent();
                                if (obj instanceof com.lvmama.vst.comm.vo.SupplierProductInfo.Item) {
                                    if (supplierProductInfo == null) {
                                        supplierProductInfo = new SupplierProductInfo();
                                    }
                                    com.lvmama.vst.comm.vo.SupplierProductInfo.Item it = (com.lvmama.vst.comm.vo.SupplierProductInfo.Item) obj;
                                    it.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
                                    supplierProductInfo.put("" + suppGoods.getSupplierId(), it);
                                }
                            }
                        } else {
                            result.setMsg(resultHandleObject.getMsg());
//                            if (StringUtils.isNotBlank(resultHandleObject.getErrorCode())) {
//                                result.setErrorCode(resultHandleObject.getErrorCode());
//                            }
                            result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
                            return result;
                        }
                    } else {
                        result.setMsg("您购买的商品-" + suppGoods.getGoodsName() + "(ID=" + suppGoods.getSuppGoodsId() + ")不可售。");
                        return result;
                    }
                } else {
                    result.setMsg("商品ID=" + orderItem.getSuppGoodsId() + "不存在。");
                    return result;
                }
            }
            result.setReturnContent(supplierProductInfo);
        } catch (IllegalArgumentException ex) {
            result.setMsg(ex.getMessage());
        }
        return result;
    }

    private boolean isRescheduleCategory(Long categoryId) {
        return categoryId != null && (
                BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId) ||
                        BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId) ||
                        BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId));
    }

    @Override
    public boolean isEbkAndEnterInTime(OrdOrderItem ordOrderItem) {

        boolean isEBKAndEnterInTime = false;
        try {
            Boolean IS_EBK_NOTICE = Boolean.FALSE;
            String fax = OrderEnum.ORDER_COMMON_TYPE.fax_flag.name();
            Boolean IS_FAX_NOTICE = ordOrderItem.hasContentValue(fax,
                    "Y");
            if (!IS_FAX_NOTICE && isRescheduleCategory(ordOrderItem.getCategoryId())) {
                if (!IS_EBK_NOTICE) {
                    Map<String,Object> paramUser=new HashMap<String,Object>();
                    paramUser.put("cancelFlag", "Y");
                    paramUser.put("supplierId", ordOrderItem.getSupplierId());
                    List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
                    if(ebkUserList!=null&& !ebkUserList.isEmpty()){
                        IS_EBK_NOTICE= true;
                    }
                }
                if (IS_EBK_NOTICE) {
                    String notInTimeFlag = ordOrderItem
                            .getNotInTimeFlag();
                    if (notInTimeFlag == null
                            || "".equals(notInTimeFlag)) {
                        Long supplierId = ordOrderItem.getSupplierId();
                        SuppSupplier suppSupplier = suppSupplierClientService
                                .findSuppSupplierById(supplierId)
                                .getReturnContent();
                        String notInTimeFlag_supplier = suppSupplier.getNotInTimeFlag();
                        if (!"Y".equals(notInTimeFlag_supplier)) {
                            SuppGoods suppgoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId()).getReturnContent();
                            String notInTimeFlag_suppgoods = suppgoods.getNotInTimeFlag();
                            if (!"Y".equals(notInTimeFlag_suppgoods)) {
                                isEBKAndEnterInTime = true;
                            }
                        }
                    } else {
                        if (!"Y".equals(notInTimeFlag)) {
                            isEBKAndEnterInTime = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("OrderRescheduleServiceImpl isEbkAndEnterInTime error:", e);
        }
        return isEBKAndEnterInTime;
    }

    @Override
    public void deductStock(OrdOrder order) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        for (OrdOrderItem orderItem : order.getOrderItemList()) {
            OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
            ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = orderTimePriceService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), true);
            if (null != timePriceHandle && !timePriceHandle.hasNull()) {
                SuppGoodsBaseTimePrice baseTimePrice = timePriceHandle.getReturnContent();
                if (null != baseTimePrice && baseTimePrice instanceof SuppGoodsAddTimePrice) {
                    if (!"Y".equalsIgnoreCase(((SuppGoodsAddTimePrice) baseTimePrice).getStockFlag())) {
                        return;
                    }
                }
                dataMap.put("suppGoodsId", baseTimePrice.getSuppGoodsId());
                dataMap.put("beginDate", baseTimePrice.getSpecDate());
                dataMap.put("endDate", baseTimePrice.getSpecDate());
                dataMap.put("orderItemId", orderItem.getOrderItemId());
                dataMap.put("orderItem", orderItem);
                if (baseTimePrice != null && baseTimePrice instanceof SuppGoodsAddTimePrice) {
                    dataMap.put("shareTotalStockId", ((SuppGoodsAddTimePrice) baseTimePrice).getShareTotalStockId());
                    dataMap.put("shareDayLimitId", ((SuppGoodsAddTimePrice) baseTimePrice).getShareDayLimitId());
                }
                //Map<String, Object> params = new HashMap<String, Object>();
                //params.put("orderItemId", orderItem.getOrderItemId());
                //params.put("stockStatus", OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name());
                try {
                    orderTimePriceService.updateStock(baseTimePrice.getTimePriceId(), orderItem.getQuantity(), dataMap);
                } catch (RuntimeException re) {
                    //orderStockDao.updateStockStatusByOrderItemId(params);
                    throw re;
                }
                //orderStockDao.updateStockStatusByOrderItemId(params);
            }

        }
    }
    
    private void deductBuyout(OrdOrderItem orderItem){
        // 如果是预控资源那么进行扣减
            Long goodsId = orderItem.getSuppGoodsId();
            Date visitDate = orderItem.getVisitTime();

            int thisOrderItemCategoryId = orderItem.getCategoryId().intValue();
            switch (thisOrderItemCategoryId) {
                case 1:
                    LOG.info("deductBuyout 酒店更新预控资源请查看saveBussiness.saveAddition(order, orderItem)方法");
                    break;

                default:
                    //通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
                    GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
                    //如果能找到该有效预控的资源  
                    //   --不在检验是否还有金额或者库存的剩余  (goodsResPrecontrolPolicyVO.getLeftNum() >0 || goodsResPrecontrolPolicyVO.getLeftAmount()>0)
                    if(goodsResPrecontrolPolicyVO != null  ){
                        Long controlId = goodsResPrecontrolPolicyVO.getId();
                        String resType = goodsResPrecontrolPolicyVO.getControlType();
                        //购买该商品的数量
                        Long reduceNum = orderItem.getBuyoutQuantity();
                        Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
                        Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
                        boolean cancelFlag = "Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())?true:false;
                        boolean reduceResult = false;

                        if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equalsIgnoreCase(resType) && leftAmount!=null &&(leftAmount>0||cancelFlag)){
                            //该商品在该时间内的剩余库存
                            Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
                            //按金额预控
                            Long value = orderItem.getBuyoutTotalPrice();
                            Long leftValue = leftAmount - value;
                            //金额预控最小只能是0
                            leftValue = leftValue< 0? 0L:leftValue;
                            reduceResult = resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
                            if(reduceResult){
                                LOG.info("deductBuyout 按金额预控-更新成功");
                                sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO,leftAmount,leftValue);
                            }
                            //如果预控金额已经没了，清空该商品在这一天的预控缓存
                            if(leftValue == 0 && reduceResult&&!cancelFlag){
                                resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,visitDate,goodsId);

                            }
                        }else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equalsIgnoreCase(resType) && leftQuantity!=null &&(leftQuantity>0||cancelFlag)){
                            //该商品在该时间内的剩余库存
                            Long leftStore = leftQuantity - reduceNum;
                            //库存最小只能是0
                            leftStore = leftStore < 0? 0L:leftStore;
                            Long storeId = goodsResPrecontrolPolicyVO.getStoreId();
                            //按库存预控
                            reduceResult = resControlBudgetRemote.updateStoreResPrecontrolPolicy(storeId,controlId, visitDate, leftStore);
                            if(reduceResult){
                                Log.info("deductBuyout 按库存预控-更新成功");
                                sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO,leftQuantity,leftStore);
                            }
                            //如果预控库存已经没了，清空该商品在这一天的预控缓存
                            if(leftStore == 0 && reduceResult&&!cancelFlag){
                                resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,visitDate,goodsId);
                            }
                        }
                        if(reduceResult){
                            LOG.info("deductBuyout 扣减预控资源成功，订单号："+orderItem.getOrderId()+"子订单号："+orderItem.getOrderItemId()+",商品id:"+orderItem.getSuppGoodsId()+"，数量："+orderItem.getBuyoutQuantity()+",总价："+orderItem.getBuyoutTotalPrice());
                        }
                    }
                    break;
            }
    }

    private void sendBudgetMsgToSendEmail(GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO,Long currentAmount,Long leftAmount){
        try{
            List<ResWarmRule> resWarmRules = resWarmRuleClientService.findAllRulesById(goodsResPrecontrolPolicyVO.getId());
            List<String> rules = new ArrayList<String>();
            for(ResWarmRule rule : resWarmRules){
                rules.add(rule.getName());
            }
            if(!DateUtil.accurateToDay(new Date()).after(goodsResPrecontrolPolicyVO.getTradeExpiryDate())){
                //按日预控
                if(ResControlEnum.CONTROL_CLASSIFICATION.Daily.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
                    //买断“金额/库存”全部消耗完时，发邮件提醒
                    if(rules.contains("lossAll") && leftAmount.longValue() == 0) {
                        LOG.info("按日-消耗完毕-发邮件");
                        resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_DAILY_EMAIL", DateUtil.formatSimpleDate((new Date()))));
                    }
                }
                //按周期
                if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
                    //买断“金额/库存”全部消耗完时，发邮件提醒
                    if(rules.contains("lossAll") && leftAmount.longValue() == 0){
                        LOG.info("按周期-消耗完毕-发邮件");
                        resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
                    }
                    //每当“金额/库存”减少${10%}，发邮件提醒销量。${10%}为变量，根据用户实际选择为准。
                    if(rules.contains("loss")){
                        String valueStr = null;
                        for(ResWarmRule rule : resWarmRules){
                            if("loss".equals(rule.getName())){
                                valueStr = rule.getValue();
                            }
                        }
                        if(null == valueStr){
                            return;
                        }
                        Long totalAmount = goodsResPrecontrolPolicyVO.getAmount();
                        Integer value = Integer.valueOf(valueStr);
                        double reduce = totalAmount*(value)/100;
                        //本次使用数量
                        Long usedNum = currentAmount - leftAmount;
                        //本次使用占比
                        double percent = usedNum/totalAmount.doubleValue();

                        //使用占比 大于等于 设置的比例 就应该发送邮件
                        if(percent * 100 >= value.doubleValue()){
                            LOG.info("按周期-消耗完百分比-发邮件");
                            resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
                        }else{
                            double ceil =  currentAmount/totalAmount.doubleValue();
                            BigDecimal b = new BigDecimal(ceil);
                            b = b.setScale(1, BigDecimal.ROUND_FLOOR);
                            double floor =  leftAmount/totalAmount.doubleValue();
                            BigDecimal d = new BigDecimal(floor);
                            d = d.setScale(1, BigDecimal.ROUND_DOWN);
                            double split = totalAmount * (d.doubleValue() +(b.doubleValue()-d.doubleValue()));
                            if(currentAmount>=split && split>leftAmount){
                                LOG.info("按周期-消耗完百分比-发邮件");
                                resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            LOG.error("买断预控，发送邮件出错："+e.getMessage());
        }
    }
    private void buildBuyoutOrderItem(OrdOrderItem orderItem){
        LOG.info("buildBuyoutOrderItem start");
        SuppGoodsAddTimePrice addTimePrice=null;
        //SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item,orderItem.getVisitTime());
        LOG.info("buildBuyoutOrderItem getTimePrice");
        OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
        ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = orderTimePriceService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), true);
        if(timePriceHandle!=null && !timePriceHandle.hasNull()){
            SuppGoodsBaseTimePrice baseTimePrice = timePriceHandle.getReturnContent();
            addTimePrice = (SuppGoodsAddTimePrice)baseTimePrice;
        }
        //如果是迪士尼剧场票，销售价和结算价从item里获取
//        if(DisneyUtils.isDisneyShow(suppGoods)){
//            if(StringUtils.isNotEmpty(item.getPrice()) && StringUtils.isNotEmpty(item.getSettlementPrice())){
//                addTimePrice.setPrice(Long.valueOf(item.getPrice()));
//                addTimePrice.setSettlementPrice(Long.valueOf(item.getSettlementPrice()));
//                addTimePrice.setMarkerPrice(Long.valueOf(item.getPrice()));
//            }
//        }

        List<ResPreControlTimePriceVO> resPriceList = null;
        long buyoutTotalPrice = 0;
        long notBuyoutTotalPrice = 0;
        Long leftMoney = null;
        long buyoutNum = 0;
        /** 开始资源预控买断价格  **/
        Long goodsId = orderItem.getSuppGoodsId();
        Date visitDate = orderItem.getVisitTime();
        GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO =new GoodsResPrecontrolPolicyVO();
        boolean hasControled=false;
        String overbuy ="N";

        //如果是预售券的兑换订单就不走买断
        if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(orderItem.getOrderSubType())){
            //通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
            goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
            //如果能找到该有效预控的资源
            hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
            overbuy = goodsResPrecontrolPolicyVO == null ? "N":goodsResPrecontrolPolicyVO.getIsCanDelay();
        }
        if(hasControled ){
            // --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
            resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
            if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
                hasControled = false;
            }else{
                LOG.info("***资源预控***");
                LOG.info("add time 门票：" + orderItem.getSuppGoodsId() + "存在预控资源");
            }
            Long prePrice = null;
            Long preSettlePrice = null;
            Long preMarketPrice = null;
            if(resPriceList!=null && resPriceList.size()>0){
                for(int m=0,n=resPriceList.size();m<n;m++){
                    ResPreControlTimePrice resTimePrice = resPriceList.get(m);
                    if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
                        preSettlePrice = resTimePrice.getValue();
                        addTimePrice.setBakPrice(addTimePrice.getSettlementPrice());
                        addTimePrice.setSettlementPrice(preSettlePrice);
                    }
                    if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
                        prePrice = resTimePrice.getValue();
                        addTimePrice.setPrice(prePrice);
                    }
                    if(OrderEnum.ORDER_PRICE_RATE_TYPE.MARKERPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
                        preMarketPrice = resTimePrice.getValue();
                        addTimePrice.setMarkerPrice(preMarketPrice);
                    }
                }
            }
        }
        /** 结束  **/
        //如果是券
        if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(orderItem.getOrderSubType())){
            Map<String,Object> map =new HashMap<String, Object>();
            map.put("goodsId", goodsId);
            map.put("applyDate", visitDate);
            //Long settlePrice=suppGoodsTimePriceClientService.getGoodsSettlePrice(map).getReturnContent();
            List<PresaleStampTimePrice> settlePrePrice=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
            //addTimePrice.setSettlementPrice(settlePrePrice.get(0).getValue());
            orderItem.setSettlementPrice(settlePrePrice.get(0).getValue());
            //addTimePrice.setPrice(Long.valueOf(item.getPrice()));
        }
        if(hasControled){
            String preControlType = goodsResPrecontrolPolicyVO.getControlType();
            if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
                //记录买断和非买断的结算总额
                if(leftMoney==null ){
                    leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
                }
                long shouldSettleTotalPrice = orderItem.getQuantity()*addTimePrice.getSettlementPrice();
                if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&"N".equalsIgnoreCase(overbuy)){
                    buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
                    //买断+非买断
                    buyoutTotalPrice = buyoutTotalPrice + buyoutNum * addTimePrice.getSettlementPrice();
                    long notBuyNum = (orderItem.getQuantity() - buyoutNum);
                    if(notBuyNum>0){
                        notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * addTimePrice.getBakPrice();
                    }
                }else if(shouldSettleTotalPrice<=leftMoney||"Y".equalsIgnoreCase(overbuy)){
                    //买断
                    buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
                    buyoutNum = orderItem.getQuantity();
                }
                orderItem.setBuyoutQuantity(buyoutNum);
                orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
                orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
                leftMoney = leftMoney - shouldSettleTotalPrice;
                orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
            }else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
                //记录买断的库存，以及各自的结算总额
                long roomNum = 0;
                if(orderItem.getQuantity()!=null ){
                    roomNum = orderItem.getQuantity().longValue();
                }
                long leftQuantity = 0;
                if(goodsResPrecontrolPolicyVO.getLeftNum()!=null){
                    leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum().longValue();
                }
                long buyoutsaledNum = 0;
                if(orderItem.getBuyoutQuantity()!=null ){
                    //buyoutsaledNum = orderItem.getBuyoutQuantity().longValue();
                }
                if(roomNum>leftQuantity&&"N".equalsIgnoreCase(overbuy)){
                    orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
                    buyoutTotalPrice = buyoutTotalPrice + leftQuantity*addTimePrice.getSettlementPrice();
                    notBuyoutTotalPrice = notBuyoutTotalPrice + (addTimePrice.getBakPrice() * (roomNum-leftQuantity));
                    //酒店设置非买断的总价
                    orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
                    //设置买断的总价
                    orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
                }else{
                    orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
                    buyoutTotalPrice = buyoutTotalPrice + roomNum*addTimePrice.getSettlementPrice();
                    orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
                }
                orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
            }
            orderItem.setBuyoutFlag("Y");
        }else{
            orderItem.setBuyoutFlag("N");
        }
        
    }
    public void deductStock(OrdOrderItem orderItem) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
        ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = orderTimePriceService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), true);
        if (null != timePriceHandle && !timePriceHandle.hasNull()) {
            SuppGoodsBaseTimePrice baseTimePrice = timePriceHandle.getReturnContent();
            if (null != baseTimePrice && baseTimePrice instanceof SuppGoodsAddTimePrice) {
                if (!"Y".equalsIgnoreCase(((SuppGoodsAddTimePrice) baseTimePrice).getStockFlag())) {
                    return;
                }
            }
            dataMap.put("suppGoodsId", baseTimePrice.getSuppGoodsId());
            dataMap.put("beginDate", baseTimePrice.getSpecDate());
            dataMap.put("endDate", baseTimePrice.getSpecDate());
            dataMap.put("orderItemId", orderItem.getOrderItemId());
            dataMap.put("orderItem", orderItem);
            if (baseTimePrice != null && baseTimePrice instanceof SuppGoodsAddTimePrice) {
                Long shareTotalStockId = ((SuppGoodsAddTimePrice) baseTimePrice).getShareTotalStockId();
                Long shareDayLimitId = ((SuppGoodsAddTimePrice) baseTimePrice).getShareDayLimitId();
                dataMap.put("shareTotalStockId", shareTotalStockId);
                dataMap.put("shareDayLimitId", shareDayLimitId);
                //
                HashMap<String, Object> pramsMap = new HashMap<String, Object>();
                pramsMap.put("orderItemId",orderItem.getOrderItemId());
                pramsMap.put("shareTotalStockId",shareTotalStockId==null?0L:shareTotalStockId);
                pramsMap.put("shareDayLimitId",shareDayLimitId==null?0L:shareDayLimitId);
                orderStockDao.updateShareIdByOrderItemId(pramsMap);
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderItemId", orderItem.getOrderItemId());
            params.put("stockStatus", OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name());
            try {
                orderTimePriceService.updateStock(baseTimePrice.getTimePriceId(), orderItem.getQuantity(), dataMap);
            } catch (RuntimeException re) {
                orderStockDao.updateStockStatusByOrderItemId(params);
                throw re;
            }
            orderStockDao.updateStockStatusByOrderItemId(params);
        }
    }
    
    public void revertStock(OrdOrderItem orderItem,Date changeVisiDate){
        List<OrdOrderStock> orderStockList = orderItem.getOrderStockList();
        if(CollectionUtils.isNotEmpty(orderStockList)){
            for(OrdOrderStock ordOrderStock:orderStockList){
                ordOrderStock.setVisitTime(orderItem.getVisitTime());
            }
        }
        Map<Date, List<OrdOrderStock>> dateOrderStockMap = buildDateStockMap(orderItem.getOrderStockList());
        if (dateOrderStockMap != null) {
            Long suppGoodsId = orderItem.getSuppGoodsId();
            Long categoryId = orderItem.getCategoryId();
            //取得商品上的组ID
            ResultHandleT<SuppGoods> suppgoods=suppGoodsClientService.findSuppGoodsById(suppGoodsId);

            Long suppGoupId =0L;

            if(suppgoods.getReturnContent() != null && suppgoods.getReturnContent().getGroupId() != null) {
                suppGoupId = suppgoods.getReturnContent().getGroupId();
            }

            if (categoryId != null) {
                Map<String, Object> dataMap = new HashMap<String, Object>();
                OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);

                Set<Map.Entry<Date, List<OrdOrderStock>>>  entrySet = dateOrderStockMap.entrySet();
                Date visitDate = null;
                for (Map.Entry<Date, List<OrdOrderStock>> entry : entrySet) {
                    if (entry != null) {
                        visitDate = entry.getKey();
                        Long updateStockTotal = countOrderStockTotal(entry.getValue());
                        if (updateStockTotal != null && updateStockTotal > 0) {
                            if (!orderItem.hasSupplierApi()) {
                                dataMap.put("isUpdateSuperStock", new Boolean(true));
                            } else {
                                dataMap.put("isUpdateSuperStock", new Boolean(false));
                            }
                            Long orderStockId = entry.getValue().get(0).getOrderStockId();
                            dataMap.put("orderItemId", entry.getValue().get(0).getOrderItemId());
                            dataMap.put("suppGoodsId", suppGoodsId);
                            dataMap.put("shareTotalStockId", entry.getValue().get(0).getShareTotalStockId());
                            dataMap.put("shareDayLimitId", entry.getValue().get(0).getShareDayLimitId());
                            dataMap.put("orderStockId", orderStockId);
                            dataMap.put("visitDate", visitDate);
                            dataMap.put("orderItem", orderItem);
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("orderItemId", entry.getValue().get(0).getOrderItemId());
                            params.put("visitTime", visitDate);
                            //清除缓存
                            String key="VST_ORDER_STOCK_REVERT_"+orderStockId;
                            SynchronizedLock.releaseMemCached(key);
                            //取得共享组中的库存记录
                            List<OrdOrderSharedStock> ordOrderSharedStockList = ordOrderSharedStockDao.selectByParams(params);
                            Long returnGroupId=0L;
                            Date returnDate =null;
                            Boolean isSharedStockFlg=false;
                            Boolean isReturnFlg=false;
                            if(ordOrderSharedStockList!=null&&ordOrderSharedStockList.size()>0){
                                returnGroupId =ordOrderSharedStockList.get(0).getGroupId();
                                if(returnGroupId!=null&&!"".equals(returnGroupId)){
                                    isSharedStockFlg =true;
                                }else{
                                    returnGroupId=0L;
                                }
                            }
                            Long hasShareStock =null;
                            if(orderItem.hasTicketAperiodic())
                            hasShareStock = goodsTicketAddTimePriceStockService.getShareStock(suppGoupId,visitDate);
                            //下单前不在共享组，取消时也不再共享组
                            if((hasShareStock==null&&isSharedStockFlg==false)){
                                isReturnFlg =true;
                                //下单前在共享组，取消时也在共享组
                            }else if (hasShareStock!=null&&isSharedStockFlg==true){
                                //商品所在的组相同
                                if(suppGoupId.equals(returnGroupId)){
                                    isReturnFlg =true;
                                }

                            }
                            LOG.info("返还判断***hasShareStock="+hasShareStock +"返还判断***isSharedStockFlg="+isSharedStockFlg);
                            //下单前后商品所在的组相同
                            if(isReturnFlg){
                                //设置了共享组
                                if(isSharedStockFlg){
                                    for(OrdOrderSharedStock  shareStock : ordOrderSharedStockList){

                                        if(shareStock.getVisitTime()!=null){
                                            //returnGroupId =shareStock.getGroupId();
                                            returnDate =shareStock.getVisitTime();
                                        }
                                        //日期相同
                                        if(visitDate.equals(returnDate)){
                                            orderTimePriceService.updateRevertStock(suppGoodsId, visitDate, updateStockTotal, dataMap);

                                            //更新订单本地库存为：库存已返还
                                            OrdOrderStock updateOrderStock = new OrdOrderStock();
                                            //updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.RESTOCK.name());
                                            updateOrderStock.setVisitTime(changeVisiDate);
                                            for (OrdOrderStock orderStock : entry.getValue()) {
                                                if (orderStock != null) {
                                                    updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
                                                    ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    orderTimePriceService.updateRevertStock(suppGoodsId, visitDate, updateStockTotal, dataMap);
                                    //更新订单本地库存为：库存已返还
                                    OrdOrderStock updateOrderStock = new OrdOrderStock();
                                    //updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.RESTOCK.name());
                                    updateOrderStock.setVisitTime(changeVisiDate);
                                    for (OrdOrderStock orderStock : entry.getValue()) {
                                        if (orderStock != null) {
                                            updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
                                            ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
                                        }
                                    }
                                }

                            }else{
                                    
//                                OrdOrderStock updateOrderStock = new OrdOrderStock();
//                                updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.UNRESTOCK.name());
//                                for (OrdOrderStock orderStock : entry.getValue()) {
//                                    if (orderStock != null) {
//                                        updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
//                                        ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
//                                    }
//                                }
                            }
                            //清除缓存
                            SynchronizedLock.releaseMemCached(key);
                        }
                    }
                }

            } else {
                throw new RuntimeException("OrdOrderItem(ID=" + orderItem.getOrderItemId() + ")的CategoryId不存在。");
            }
        }
        
    }
    private Long countOrderStockTotal(List<OrdOrderStock> orderStockList) {
        Long total = 0L;
        if (orderStockList != null) {
            for (OrdOrderStock orderStock : orderStockList) {
                if (orderStock != null) {
                    total = total + orderStock.getQuantity();
                }
            }
        }

        return total;
    }
    private Map<Date, List<OrdOrderStock>> buildDateStockMap(List<OrdOrderStock> orderStockList) {
        HashMap<Date, List<OrdOrderStock>> dateStockMap = null;

        if (orderStockList != null) {
            dateStockMap = new HashMap<Date, List<OrdOrderStock>>();
            Boolean isSharedStockFlg=false;
            for (OrdOrderStock orderStock : orderStockList) {
                if (orderStock != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("orderItemId", orderStock.getOrderItemId());
                    params.put("visitTime", orderStock.getVisitTime());
                    //只要ord_order_shared_stock表中同一个子订单,同一天的所有记录
                    List<OrdOrderSharedStock> ordOrderSharedStockList = ordOrderSharedStockDao.selectByParams(params);
                    isSharedStockFlg =false;
                    if(ordOrderSharedStockList!=null&&ordOrderSharedStockList.size()>0){
                        for(OrdOrderSharedStock sharedStock:ordOrderSharedStockList){
                            //group_id不为空就是共享库存
                            if(sharedStock.getGroupId()!=null){
                                isSharedStockFlg =true;
                                break;
                            }
                        }
                    }

                    if (((orderStock.getShareTotalStockId() == null || orderStock
                            .getShareTotalStockId() <= 0))
                            || isSharedStockFlg
                            || OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name().equals(orderStock.getInventory())
                            || OrderEnum.INVENTORY_STATUS.RESTOCK.name().equals(orderStock.getInventory())
                            || OrderEnum.INVENTORY_STATUS.FREESALE.name().equals(orderStock.getInventory())) {
                        List<OrdOrderStock> sameDateStockList = dateStockMap.get(orderStock.getVisitTime());
                        if (sameDateStockList == null) {
                            sameDateStockList = new ArrayList<OrdOrderStock>();
                            dateStockMap.put(orderStock.getVisitTime(), sameDateStockList);
                        }

                        sameDateStockList.add(orderStock);
                    }
                }
            }
        }

        return dateStockMap;
    }
    private Map<Long, Date> filterOrderItemList(OrdOrder order, List<SuppGoodsRescheduleVO.Item> list) {
        Map<Long, Date> orderItemVisitTimeMap = new HashMap<Long, Date>();
        if (CollectionUtils.isNotEmpty(list) && null != order) {
            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            Iterator<OrdOrderItem> iterator = orderItemList.iterator();
            Map<Long, Date> map = new HashMap<Long, Date>();
            for (SuppGoodsRescheduleVO.Item item : list) {
                map.put(item.getOrderItemId(), item.getChangeVisitDate());
            }
            while (iterator.hasNext()) {
                OrdOrderItem next = iterator.next();
                Long orderItemId = next.getOrderItemId();
                if (map.containsKey(orderItemId)) {
                    if ((map.get(orderItemId).compareTo(next.getVisitTime())!=0)) {
                        orderItemVisitTimeMap.put(orderItemId, next.getVisitTime());
                        next.setVisitTime(map.get(orderItemId));
                    } else {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }

            }
        }
        return orderItemVisitTimeMap;
    }

    private void sendRescheduleSms(OrdOrder order) {
        List<Long> list = new ArrayList<Long>();
        Map<String, Object> map = new HashMap<String, Object>() {
            {
                put("RESCHEDULE_FLAG", true);
            }
        };
        if (null != order) {
            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            if (CollectionUtils.isNotEmpty(orderItemList)) {
                try {
                    for (OrdOrderItem orderItem : orderItemList) {
                        list.add(orderItem.getOrderItemId());
                        map.put(String.valueOf(orderItem.getOrderItemId()), orderItem.getVisitTime());
                    }
                    String mobile = "";
                    OrdPerson contactPerson = order.getContactPerson();
                    List<OrdPerson> ordTravellerList = order.getOrdTravellerList();
                    if (null != contactPerson && StringUtils.isNotBlank(contactPerson.getMobile())) {
                        mobile = contactPerson.getMobile();
                    } else if (CollectionUtils.isNotEmpty(ordTravellerList) && StringUtils.isNotBlank(ordTravellerList.get(0).getMobile())) {
                        mobile = ordTravellerList.get(0).getMobile();
                    }
                    String content = orderSendSmsService.getTicketCertContent(order.getOrderId(), list, map);
                    LOG.info("sendRescheduleSms mobile:" + mobile + ", orderId:" + order.getOrderId() + ", map:" + map + ", content:" + content);
                   /* 
                    * update by xiexun 此处传入业务类型用于标识调用方
                    * orderSendSmsService.sendSMS(content, mobile, order.getOrderId());*/
                    orderSendSmsService.sendSMS(content, mobile, "RESCHEDULE", order.getOrderId());
                    List<Long> orderIdList = new ArrayList<Long>();
                    orderIdList.add(order.getOrderId());
                    ordOrderUpdateService.markRemindSmsNoSend(orderIdList);
                } catch (Exception e) {
                    LOG.info("sendRescheduleSms error:" + e.getMessage(), e);
                }
            }
        }
    }

    public List<String> getOrdChangeDescStrList(OrdOrder ordOrder) {
        List<String> list = new ArrayList<String>();
        try {
            for (OrdOrderItem orderItem : ordOrder.getOrderItemList()) {
                SuppGoodsReschedule suppGoodsReschedule = ordItemRescheduleService.toSuppGoodsReschedule(orderItem.getOrderItemId());
                if (null != suppGoodsReschedule) {
                    String s = SuppGoodsRescheduleTools.convertChangeDesc(suppGoodsReschedule,false,orderItem.getVisitTime());
                    if (StringUtils.isNotBlank(s)) {
                        list.add(s);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("getOrderChangeDesc error:" + e.getMessage(), e);
        }
        return list;
    }
    @Override
    public SuppGoodsRescheduleVO.OrdRescheduleStatus getOrderRescheduleStatus(OrdOrder ordOrder) {
        SuppGoodsRescheduleVO.OrdRescheduleStatus ordRescheduleStatus = new SuppGoodsRescheduleVO.OrdRescheduleStatus();
        try {
            if(null!=ordOrder){
                Long ordChangeCount = this.getOrdChangeCount(ordOrder);
                Long suppChangeCount = this.getSuppChangeCount(ordOrder);
                List<String> ordChangeDescStrList = this.getOrdChangeDescStrList(ordOrder);
                ordRescheduleStatus.setOrdChangeCount(ordChangeCount);
                ordRescheduleStatus.setSuppChangeCount(suppChangeCount.longValue());
                ordRescheduleStatus.setOrdChangeDesStrList(ordChangeDescStrList);
                ordRescheduleStatus.setOrdRescheduleFlag(Constants.Y_FLAG);
                if (!isRescheduleCategory(ordOrder.getCategoryId())) {
                    ordRescheduleStatus.setOrdRescheduleFlag(Constants.N_FLAG);
                    return ordRescheduleStatus;
                }
                if (ordChangeCount >= suppChangeCount) {
                    LOG.info("rescheduleFlag orderId:" + ordOrder.getOrderId() + " suppChangeCount:" + suppChangeCount + " ordChangeCount:" + ordChangeCount);
                    ordRescheduleStatus.setOrdRescheduleFlag(Constants.N_FLAG);
                    return ordRescheduleStatus;
                }
                ResultHandle resultHandle = checkOrderStatus(ordOrder);
                if (resultHandle.isFail()) {
                    LOG.info("rescheduleFlag orderId:" + ordOrder.getOrderId() + " errorMsg:" + resultHandle.getMsg());
                    ordRescheduleStatus.setOrdRescheduleFlag(Constants.N_FLAG);
                    return ordRescheduleStatus;
                }
                ResultHandleT<String> stringResultHandleT = checkRescheduleStatus(ordOrder);
                if(stringResultHandleT.isFail()){
                    LOG.info("rescheduleFlag orderId:" + ordOrder.getOrderId() + " errorMsg:" + stringResultHandleT.getMsg());
                    ordRescheduleStatus.setOrdRescheduleFlag(Constants.N_FLAG);
                    return ordRescheduleStatus;
                }
            }
        } catch (Exception e) {
            LOG.error("getOrderRescheduleStatus error:"+e.getMessage(),e);
        }
        return ordRescheduleStatus;
    }

    @Override
    public void addRescheduleLog(ResultHandleT<List<SuppGoodsRescheduleVO.Item>> resultHandleT,Long orderId,Map<String,Object> map) {
        StringBuilder logContent = new StringBuilder();
        try {
            List<SuppGoodsRescheduleVO.Item> returnContent = resultHandleT.getReturnContent();
            if(resultHandleT.isSuccess()){
                //日志
                logContent.append("【改期成功】子订单");
                for(SuppGoodsRescheduleVO.Item rescheduleRes:returnContent){
                    logContent.append(rescheduleRes.getOrderItemId())
                            .append(ComLogUtil.getLogTxt("游玩日期", DateFormatUtils.format(rescheduleRes.getChangeVisitDate(), "yyyy-MM-dd"), DateFormatUtils.format(rescheduleRes.getOldVisitDate(), "yyyy-MM-dd")));
                }
            }else{
                logContent.append("【改期失败】子订单");
                if(!resultHandleT.hasNull()){
                    for(SuppGoodsRescheduleVO.Item rescheduleRes: returnContent){
                        logContent.append(rescheduleRes.getOrderItemId()).append(",");
                    } 
                }
                logContent.append("errorMsg:").append(resultHandleT.getMsg());
            }
            ComLog.COM_LOG_LOG_TYPE logType = resultHandleT.isSuccess() ? ComLog.COM_LOG_LOG_TYPE.CHANGE_VISIT_DATE_SUCCESS : ComLog.COM_LOG_LOG_TYPE.CHANGE_VISIT_DATE_FAIL;
            StringBuilder memo = new StringBuilder();
            String trackNumber = LogTrackContext.getTrackNumber();
            if (trackNumber != null && trackNumber.length() > 0) {
                memo.append(" TrackNumber:");
                memo.append(trackNumber);
                try {
                    memo.append(",RunMachineIp:");
                    memo.append(InetAddress.getLocalHost().getHostAddress());
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    LOG.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                    orderId,orderId,
                    (String)map.get("userName"),
                    logContent.toString(),
                    logType.getCode(),
                    logType.getCnName(),memo.toString());
        } catch (Exception e) {
            LOG.error("addRescheduleLog error:"+e.getMessage(),e);
        }
    }
    
    @Override
    public List<RescheduleTimePrice> getRescheduleTimePriceList(Long suppGoodsId, Long productId, Long distributorId, Map<String, Object> map) {
        LOG.info("getRescheduleTimePriceList suppGoodsId:"+suppGoodsId+" productId:"+productId+" distributorId:"+distributorId+" map:"+map);
        Long orderId = (Long)map.get("orderId");
        String packageFlag = (String)map.get("packageFlag");
        List<RescheduleTimePrice> rescheduleTimePriceList = new ArrayList<RescheduleTimePrice>();
        if(null!=orderId){
            OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
            map.put("order",ordOrder);
            if(StringUtils.isNotBlank(packageFlag) && "Y".equalsIgnoreCase(packageFlag)){
                RescheduleTimePrice rescheduleTimePriceForPackage = getRescheduleTimePriceForPackage(suppGoodsId, productId, distributorId, map);
                rescheduleTimePriceList.add(rescheduleTimePriceForPackage);
            }else{
                List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
                for(OrdOrderItem orderItem:orderItemList){
                    RescheduleTimePrice rescheduleTimePrice = getRescheduleTimePrice(orderItem.getSuppGoodsId(), distributorId, map);
                    rescheduleTimePrice.getAttributes().setOrderItemId(orderItem.getOrderItemId());
                    rescheduleTimePriceList.add(rescheduleTimePrice);
                }
            }
        }
        return rescheduleTimePriceList;
    }
    
    private RescheduleTimePrice getRescheduleTimePriceForPackage(Long suppGoodsId,Long productId,Long distributorId,Map<String,Object> map){
        LOG.info("getRescheduleTimePrice suppGoodsId:"+suppGoodsId+" productId:"+productId+" distributorId:"+distributorId+" map:"+map);
        RescheduleTimePrice result = new RescheduleTimePrice();
        RescheduleTimePrice.RescheduleAttribute attributes = result.getAttributes();
        if(distributorId==null){
            distributorId = Constant.DIST_FRONT_END;
        }
        try {
            Long itemProductId = (Long)map.get("itemProductId");
            OrdOrder ordorder = (OrdOrder)map.get("order");
            ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT = distGoodsTimePriceClientService.getTimePriceByGoodsIdAndDate(distributorId, suppGoodsId,
                    new Date(), 12);
            if(resultHandleT.isSuccess() && !resultHandleT.hasNull()){
                    List<SuppGoodsAddTimePrice> suppGoodsItems = resultHandleT.getReturnContent();
                    if(suppGoodsItems != null && suppGoodsItems.size() >0){
                        Map<String,Object> reqMap = new HashMap<String,Object>();
                        for (SuppGoodsAddTimePrice suppGoodsAddTimePrice : suppGoodsItems) {
                            buildSplitPrice(suppGoodsAddTimePrice,productId,suppGoodsId);
                            reqMap.put("suppGoodsId", suppGoodsId);
                            reqMap.put("specDate", suppGoodsAddTimePrice.getSpecDate());
                            reqMap.put("productId",itemProductId);
                            ResultHandleT<SuppGoodsSplitTimePrice> suppGoodsSplitTimePriceHandle = prodCalSplitClientRemote.selectSuppGoodsSplitTimePrice(reqMap);
                            SuppGoodsSplitTimePrice uppGoodsSplitTimePrice = suppGoodsSplitTimePriceHandle.getReturnContent();
                            if(uppGoodsSplitTimePrice == null || uppGoodsSplitTimePrice.getStock() == null){
                                if("Y".equals(suppGoodsAddTimePrice.getStockFlag())){
                                    suppGoodsAddTimePrice.setStockFlag("N");
                                }
                            }
                            if(uppGoodsSplitTimePrice != null && uppGoodsSplitTimePrice.getStock() != null && uppGoodsSplitTimePrice.getStock() == 0l){
                                suppGoodsAddTimePrice.setStock(suppGoodsAddTimePrice.getStock() == null ? 0l : suppGoodsAddTimePrice.getStock());
                                if(suppGoodsAddTimePrice.getStock() > 0l){
                                    continue;
                                }
                            }
                            suppGoodsAddTimePrice.setStock(uppGoodsSplitTimePrice == null || uppGoodsSplitTimePrice.getStock() == null ? null : uppGoodsSplitTimePrice.getStock());
                        }
                    }
                    attributes.setTimePriceList(suppGoodsItems);
                    buildPromotionList(productId,distributorId,attributes,ordorder);
                    result.setCode(RescheduleTimePrice.SUCCESS);
                    result.setMessage("操作成功");
                }else{
                    result.setCode(RescheduleTimePrice.ERROR);
                    result.setMessage("商品时间价格为空");
                }
        } catch (Exception e) {
            LOG.error("getRescheduleTimePrice"+e.getMessage(),e);
            result.setCode(RescheduleTimePrice.ERROR);
            result.setMessage("查询商品时间价格异常");
        }
        return result;
    }
    private RescheduleTimePrice getRescheduleTimePrice(Long suppGoodsId,Long distributorId,Map<String,Object> map){
        LOG.info("getRescheduleTimePrice suppGoodsId:"+suppGoodsId+" distributorId:"+distributorId+" map:"+map);
        RescheduleTimePrice result = new RescheduleTimePrice();
        RescheduleTimePrice.RescheduleAttribute attributes = result.getAttributes();
        if(distributorId==null){
            distributorId = Constant.DIST_FRONT_END;
        }
        try {
            OrdOrder ordorder = (OrdOrder)map.get("order");
            ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT = distGoodsTimePriceClientService.getTimePriceByGoodsIdAndDate(distributorId, suppGoodsId,
                    new Date(), 12);
                if(resultHandleT.isSuccess() && !resultHandleT.hasNull()){
                    List<SuppGoodsAddTimePrice> returnContent = resultHandleT.getReturnContent();
                    attributes.setTimePriceList(returnContent);
                    buildPromotionList(suppGoodsId,distributorId, attributes,ordorder);
                    result.setCode(RescheduleTimePrice.SUCCESS);
                    result.setMessage("操作成功");
                }else{
                    result.setCode(RescheduleTimePrice.ERROR);
                    result.setMessage("商品时间价格为空");
                }
        } catch (Exception e) {
            LOG.error("getRescheduleTimePrice"+e.getMessage(),e);
            result.setCode(RescheduleTimePrice.ERROR);
            result.setMessage("查询商品时间价格异常");
        }
        return result;
    }
//    @Override
//    public RescheduleTimePrice getRescheduleTimePrice(Long suppGoodsId,Long productId,Long distributorId,Map<String,Object> map) {
//        LOG.info("getRescheduleTimePrice suppGoodsId:"+suppGoodsId+" productId:"+productId+" distributorId:"+distributorId+" map:"+map);
//        RescheduleTimePrice result = new RescheduleTimePrice();
//        RescheduleTimePrice.RescheduleAttribute attributes = result.getAttributes();
//        if(distributorId==null){
//            distributorId = Constant.DIST_FRONT_END;
//        }
//        try {
//            String packageFlag = (String)map.get("packageFlag");
//            Long itemProductId = (Long)map.get("itemProductId");
//            Long orderId = (Long)map.get("orderId"); 
//            ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT = distGoodsTimePriceClientService.getTimePriceByGoodsIdAndDate(distributorId, suppGoodsId,
//                    new Date(), 12);
//            if(StringUtils.isNotBlank(packageFlag) && "Y".equalsIgnoreCase(packageFlag)){
//                if(resultHandleT.isSuccess() && !resultHandleT.hasNull()){
//                    List<SuppGoodsAddTimePrice> suppGoodsItems = resultHandleT.getReturnContent();
//                    if(suppGoodsItems != null && suppGoodsItems.size() >0){
//                        Map<String,Object> reqMap = new HashMap<String,Object>();
//                        for (SuppGoodsAddTimePrice suppGoodsAddTimePrice : suppGoodsItems) {
//                            buildSplitPrice(suppGoodsAddTimePrice,productId,suppGoodsId);
//                            reqMap.put("suppGoodsId", suppGoodsId);
//                            reqMap.put("specDate", suppGoodsAddTimePrice.getSpecDate());
//                            reqMap.put("productId",itemProductId);
//                            ResultHandleT<SuppGoodsSplitTimePrice> suppGoodsSplitTimePriceHandle = prodCalSplitClientRemote.selectSuppGoodsSplitTimePrice(reqMap);
//                            SuppGoodsSplitTimePrice uppGoodsSplitTimePrice = suppGoodsSplitTimePriceHandle.getReturnContent();
//                            if(uppGoodsSplitTimePrice == null || uppGoodsSplitTimePrice.getStock() == null){
//                                if("Y".equals(suppGoodsAddTimePrice.getStockFlag())){
//                                    suppGoodsAddTimePrice.setStockFlag("N");
//                                }
//                            }
//                            if(uppGoodsSplitTimePrice != null && uppGoodsSplitTimePrice.getStock() != null && uppGoodsSplitTimePrice.getStock() == 0l){
//                                suppGoodsAddTimePrice.setStock(suppGoodsAddTimePrice.getStock() == null ? 0l : suppGoodsAddTimePrice.getStock());
//                                if(suppGoodsAddTimePrice.getStock() > 0l){
//                                    continue;
//                                }
//                            }
//                            suppGoodsAddTimePrice.setStock(uppGoodsSplitTimePrice == null || uppGoodsSplitTimePrice.getStock() == null ? null : uppGoodsSplitTimePrice.getStock());
//                        }
//                    }
//                    attributes.setTimePriceList(suppGoodsItems);
//                    buildPromotionList(productId,distributorId,attributes,orderId);
//                    result.setCode(RescheduleTimePrice.SUCCESS);
//                    result.setMessage("操作成功");
//                }else{
//                    result.setCode(RescheduleTimePrice.ERROR);
//                    result.setMessage("商品时间价格为空");
//                }
//            }else{
//                if(resultHandleT.isSuccess() && !resultHandleT.hasNull()){
//                    List<SuppGoodsAddTimePrice> returnContent = resultHandleT.getReturnContent();
//                    attributes.setTimePriceList(returnContent);
//                    buildPromotionList(suppGoodsId,distributorId, attributes,orderId);
//                    result.setCode(RescheduleTimePrice.SUCCESS);
//                    result.setMessage("操作成功");
//                }else{
//                    result.setCode(RescheduleTimePrice.ERROR);
//                    result.setMessage("商品时间价格为空");
//                }
//            }
//        } catch (Exception e) {
//            LOG.error("getRescheduleTimePrice"+e.getMessage(),e);
//            result.setCode(RescheduleTimePrice.ERROR);
//            result.setMessage("查询商品时间价格异常");
//        }
//        return result;
//    }
    private void buildPromotionList(Long id,Long distributorId, RescheduleTimePrice.RescheduleAttribute attributes,OrdOrder ordOrder) {
        LOG.info("buildPromotionList id:"+id+" distributorId:"+distributorId);
        try {
            Map<Long, Object> promotionDate = this.getPromotionDate(ordOrder);
            List<RescheduleTimePrice.PromInfo> promotions = new ArrayList<RescheduleTimePrice.PromInfo>();
            for(Map.Entry<Long,Object> entry:promotionDate.entrySet()){
                RescheduleTimePrice.PromInfo promInfo = new RescheduleTimePrice.PromInfo();
                promInfo.setOrderItemId(entry.getKey());
                Map<String,Date> dataMap = (Map<String,Date>)entry.getValue();
                promInfo.setStartVistTime(dataMap.get("start"));
                promInfo.setEndVistTime(dataMap.get("end"));
                promotions.add(promInfo);
            }
            attributes.setPromList(promotions);
        } catch (Exception e) {
            LOG.error("buildPromotionList error:"+e.getMessage(),e);
        }
    }
    private void buildSplitPrice(SuppGoodsAddTimePrice timePrice,Long productId,Long suppGoodsId){
        Map<String,Object> map = new HashMap<String,Object>();
        List<ProdPackageGroup> groupList = prodPackageGroupClientService.getProdPackageGroupByProductId(productId);
        if(CollectionUtils.isNotEmpty(groupList)){
            map.put("groupId",groupList.get(0).getGroupId());
            map.put("objectId", suppGoodsId);
            List<ProdPackageDetail> prodPackageDetailList = prodPackageGroupClientService.findProdPackageDetailByParams(map);
            if(CollectionUtils.isNotEmpty(prodPackageDetailList)) {
                ProdPackageDetail detail = prodPackageDetailList.get(0);
                //Long count = detail.getPackageCount();
                Long count = 1L;//打包后的单价
                String priceType = detail.getPriceType();
                if("FIXED_PRICE".equals(priceType)){
                    timePrice.setPrice((timePrice.getSettlementPrice()+detail.getPrice())*count);
                }else if("MAKEUP_PRICE".equals(priceType)){
                    timePrice.setPrice((timePrice.getSettlementPrice() + (timePrice.getPrice() - timePrice.getSettlementPrice())*detail.getPrice()/10000)*count);
                }
                LOG.info("buildSplitPrice visitDate = " + timePrice.getSpecDate() + "   suppGoodsId = " + suppGoodsId + "   priceType = " + priceType + "   settlementPrice = " + timePrice.getSettlementPrice() + "   sellPrice = " + timePrice.getPrice() + "   detailPrice = " + detail.getPrice() + "   count = " + count);
            }
        }
        
    }
    private void buildSplitPrice(SuppGoodsAddTimePrice suppGoodsAddTimePrice,Long productId,Date visitTime){
        TicketProductForOrderVO ticketProductForOrderVO = null;
        Long suppGoodsId = suppGoodsAddTimePrice.getSuppGoodsId();
        ResultHandleT<TicketProductForOrderVO> productResultHandleT = prodProductClientService.findTicketProductForOrder(productId,visitTime);
        ticketProductForOrderVO = productResultHandleT.getReturnContent();
        ticketProductForOrderVO.setGoodsIdAspriceMap(new HashMap<String,Float>());
        ticketProductForOrderVO.getSplitTotalPrice("Y");
        Map<String, Float> goodsIdAspriceMap = ticketProductForOrderVO.getGoodsIdAspriceMap();
        suppGoodsAddTimePrice.setPrice(goodsIdAspriceMap.get(suppGoodsId.toString()).longValue());
    }
    @Override
    public OrderRescheduleInfo queryOrderRescheduleInfo(Long orderId, Map<String, Object> map) {
        OrderRescheduleInfo orderRescheduleInfo = new OrderRescheduleInfo();
        LOG.info("queryOrderRescheduleInfo orderId:"+orderId+" map:"+map);
        if (null != orderId) {
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            if (!isRescheduleCategory(order.getCategoryId())) {
                orderRescheduleInfo.setRescheduleFlag("N");
                return orderRescheduleInfo;
            }
            ResultHandle resultHandle = checkOrderStatus(order);
            if (resultHandle.isFail()) {
                LOG.info("queryOrderRescheduleInfo orderId:" + order.getOrderId() + " errorMsg:" + resultHandle.getMsg());
                orderRescheduleInfo.setRescheduleFlag("N");
                if(StringUtils.isNotBlank(resultHandle.getErrorCode())){
                    orderRescheduleInfo.setChangeTipStr(SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.getDescTemplate(resultHandle.getErrorCode()));
                }
                return orderRescheduleInfo;
            }
            ResultHandleT<String> stringResultHandleT = checkRescheduleStatus(order);
            if(stringResultHandleT.isSuccess()){
                orderRescheduleInfo.setRescheduleFlag("Y");
                orderRescheduleInfo.setChangeTipStr(stringResultHandleT.getReturnContent());
            }else{
                orderRescheduleInfo.setRescheduleFlag("N");
                orderRescheduleInfo.setChangeTipStr(stringResultHandleT.getReturnContent());
                return orderRescheduleInfo;
            }
            Long changeCount = getSuppChangeCount(order);
            Long ordChangeCount = getOrdChangeCount(order);
            if(ordChangeCount>0){
                orderRescheduleInfo.setOrdChangeCount(ordChangeCount);
            }
            if(changeCount>0){
                orderRescheduleInfo.setSuppChangeCount(changeCount.longValue());
            }else{
                LOG.info("queryOrderRescheduleInfo orderId:" + order.getOrderId() + " changeCount:" + changeCount);
                orderRescheduleInfo.setRescheduleFlag("N");
                return orderRescheduleInfo;
            }
            if (ordChangeCount >= changeCount) {
                LOG.info("queryOrderRescheduleInfo orderId:" + order.getOrderId() + " changeCount:" + changeCount + " ordChangeCount:" + ordChangeCount);
                HashMap<String, Object> templateMap = new HashMap<String, Object>();
                templateMap.put("count", changeCount);
                String template = SuppGoodsRescheduleVO.CHANGE_DESC_TIP_TEMPLATE.TEMPLATE_4.getTemplate();
                String changeTipStr = SuppGoodsRescheduleTools.fillTemplate(template, templateMap);
                orderRescheduleInfo.setRescheduleFlag("N");
                orderRescheduleInfo.setChangeTipStr(changeTipStr);
                return orderRescheduleInfo;
            }
            orderRescheduleInfo.setIsMerge(isMerge(order));
        }
        LOG.info("queryOrderRescheduleInfo orderId:"+orderId+"  orderRescheduleInfo:"+orderRescheduleInfo);
        return orderRescheduleInfo;
    }
    public Boolean isMerge(OrdOrder ordOrder){
//        Boolean isMerge = Boolean.FALSE;
//        List<Long> list = new ArrayList<Long>();
//        List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
//        if(CollectionUtils.isNotEmpty(orderItemList)){
//            for(OrdOrderItem orderItem:orderItemList){
//                list.add(orderItem.getSuppGoodsId());
//            }
//        }
//        ResultHandleT<Boolean> booleanResultHandleT = supplierOrderOtherService.checkProviderIsMergeByGoodsId(list);
//        if(booleanResultHandleT.isSuccess() && !booleanResultHandleT.hasNull()){
//            isMerge = booleanResultHandleT.getReturnContent();
//        }
        return false;
    }

}