package com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory.product;

import com.google.common.base.Preconditions;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsGroupVO;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsVo;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory.HotelCombDbStroeFactory;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdSettlementPriceRecordService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.PromPromotionService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/2/23.
 */
@Component
public class OrderStoreProductServiceImpl implements  IOrderStoreProduct{

    @Autowired
    private OrdOrderDao orderDao;
    @Autowired
    private OrdOrderPackDao orderPackDao;
    @Autowired
    private OrdOrderItemDao orderItemDao;
    @Autowired
    private OrdHotelCombInfoDao ordHotelCombInfoDao;
    @Autowired
    private OrdOrderAmountItemDao orderAmountItemDao;
    @Autowired
    private OrdPersonDao personDao;
    @Autowired
    private OrdAddressDao ordAddressDao;
    @Autowired
    private OrdPromotionDao promotionDao;

    @Autowired
    private OrdOrderStockDao orderStockDao;

    @Autowired
    private IOrdTravelContractService ordTravelContractService;

    @Autowired
    private OrdPromotionDao ordPromotionDao;

    @Autowired
    private OrdAdditionStatusDAO ordAdditionStatusDAO;

    @Autowired
    private OrdFormInfoDao ordFormInfoDao;

    @Autowired
    private PromPromotionService promPromotionService;

    @Autowired
    OrdItemAdditSuppGoodsDao ordItemAdditSuppGoodsDao;


    @Autowired
    private IOrdSettlementPriceRecordService iOrdSettlementPriceRecordService;

    @Autowired
    private IOrdItemContractRelationService iOrdItemContractRelationService;

    @Autowired
    private SuppSettlementEntityClientService suppSettlementEntityClientService;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private OrderOrderFactory orderOrderFactory;

    private static final Logger logger = LoggerFactory
            .getLogger(HotelCombDbStroeFactory.class);

    @Override
    public void orderHeaderDbStore(OrdOrderDTO order) {
        order.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
        order.setCancelCode(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name());
        order.setReason(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.getCnName());

        Long startTime = System.currentTimeMillis();
        logger.info("saveOrder保存订单开buCode"+order.getBuCode());

        Long orderId = orderDao.saveOrder(order);

        Preconditions.checkNotNull(orderId,"orderId not been null");

        //设置到orderDTO
        order.setOrderId(orderId);

        logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));

        if(CollectionUtils.isNotEmpty(order.getOrdAdditionStatusList())){
            startTime = System.currentTimeMillis();
            for(OrdAdditionStatus status:order.getOrdAdditionStatusList()){
                status.setOrderId(orderId);
                ordAdditionStatusDAO.insert(status);
            }
            logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ADDITION_STATUS", "saveOrder.ORD_ADDITION_STATUS", System.currentTimeMillis() - startTime));
        }



//        startTime = System.currentTimeMillis();
//        orderDao.updateByPrimaryKey(order);
//
//        logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存.update-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));
//



        startTime = System.currentTimeMillis();
        for(OrdOrderAmountItem item:order.getOrderAmountItemList()){
            item.setOrderId(orderId);
            orderAmountItemDao.insert(item);
        }

        logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER_AMOUNT_ITEM", "saveOrder.ORD_ORDER_AMOUNT_ITEM", System.currentTimeMillis() - startTime));

        if(CollectionUtils.isNotEmpty(order.getFormInfoList())){
            startTime = System.currentTimeMillis();
            for(OrdFormInfo info:order.getFormInfoList()){
                logger.info("orderId=" + orderId + "contentType=" + info.getContentType() + "content=" + info.getContent() + "===>" );
                info.setOrderId(orderId);
                ordFormInfoDao.insert(info);
            }
            logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_FORM_INFO", "saveOrder.ORD_FORM_INFO", System.currentTimeMillis() - startTime));
        }

        startTime = System.currentTimeMillis();
        //		doSaveGuaranteeCreditCardInOrder(order);
        //	logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_GUARANTEE_CREDIT_CARD", "saveOrder.ORD_GUARANTEE_CREDIT_CARD", System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
//        doSaveOrdTravelContractInOrder(order);
        logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", "saveOrder.ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", System.currentTimeMillis() - startTime));

        //保存订单查询的相关信息
        startTime = System.currentTimeMillis();

        logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_GUARANTEE_CREDIT_CARD", "saveOrder.ORD_GUARANTEE_CREDIT_CARD", System.currentTimeMillis() - startTime));

        logger.info("===>saveOrder->deductStock"+"    order size = "+order.getUpdateStockMap().size());
    }

    @Override
    public void orderItemDbStore(OrdOrderDTO order) {

        for(OrdOrderItem orderItem:order.getOrderItemList()){
            orderItem.setOrderId(order.getOrderId());
            if(orderItem.getOrderPack()!=null){
                orderItem.setOrderPackId(orderItem.getOrderPack().getOrderPackId());
            }
            //如果是券不走买断库存
            if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(orderItem.getOrderSubType())){
                orderItem.setBuyoutFlag("N");
            }

            // 保存子订单中 结算对象ID及CODE
            try{
                Long suppGoodsId = orderItem.getSuppGoodsId();
                if(null==suppGoodsId){
                    throw new BusinessException(" [ OrderStoreProductServiceImpl ] , ERROR : suppgoods is is null in orderItem ");
                }

                ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
                if(!suppGoodsResultHandleT.isSuccess()){
                    throw new BusinessException(" [ OrderStoreProductServiceImpl ] , ERROR : can not find suppGoods by id ("+suppGoodsId+")");
                }

                SuppGoods suppGoods = suppGoodsResultHandleT.getReturnContent();
                String settleEntityCode = suppGoods.getSettlementEntityCode();
                if(StringUtils.isEmpty(settleEntityCode)){
                    throw new BusinessException(" [ OrderStoreProductServiceImpl ] , ERROR : suppGoods has no settlement entity code!!! suppGoods id is " + suppGoods.getSuppGoodsId());
                }

                ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(settleEntityCode);
                if(!resultHandleT.isSuccess()){
                    throw new BusinessException(" [ OrderStoreProductServiceImpl ] , ERROR : find SuppSettlementEntities by code ("+settleEntityCode+" ) failed");
                }

                SuppSettlementEntities settlementEntities = resultHandleT.getReturnContent();
                if(null==settlementEntities){
                    throw new BusinessException(" [ OrderStoreProductServiceImpl ] , ERROR : can not find SuppSettlementEntities by code ("+settleEntityCode+" )");
                }

                // 存入订单表中的值为 ID_CODE形式
                String codeValue = settlementEntities.getId()+"_"+settlementEntities.getCode();
                orderItem.setSettlementEntityCode(codeValue);

            }catch (Exception e){
                logger.error(" [ NewHotelComOrderSaveService ] set orderItem settleEntityCode has exception, error msg : "+e.getMessage());
            }

            logger.info("start save orderItem, orderId:" + orderItem.getOrderId());
            orderItemDao.insert(orderItem);
//
//            //设置到 id orderItemDTO
//            orderItem.setOrderItemId((long)orderItemId);
            logger.info("end save orderItem, orderId:" + orderItem.getOrderId()+" orderItemId:"+orderItem.getOrderItemId());

//            com.google.common.base.Preconditions.checkArgument();

            Preconditions.checkNotNull(orderItem,"orderItem not been null");

            if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==order.getCategoryId()){
                if(orderItem.getAdditSuppGoodsGroupVO()!=null){
                    for(AdditSuppGoodsGroupVO additSuppGoodsGroupVO: orderItem.getAdditSuppGoodsGroupVO()){
                        for(AdditSuppGoodsVo additSuppGoodsVo:additSuppGoodsGroupVO.getAdditSuppGoodsVo()){
                            logger.info("start save ordeItemAdditSuppGoods, orderItemId:"+orderItem.getOrderItemId()+"additSuppGoodsId:"+additSuppGoodsVo.getAdditSuppGoodsId());
                            OrderItemAdditSuppGoods ordItemAdditSuppGoods = new OrderItemAdditSuppGoods();
                            ordItemAdditSuppGoods.setOrderItemId(orderItem.getOrderItemId());
                            ordItemAdditSuppGoods.setAddItSuppGoodsId(additSuppGoodsVo.getAdditSuppGoodsId());
                            ordItemAdditSuppGoods.setQuantity(additSuppGoodsVo.getQuantity());
                            ordItemAdditSuppGoods.setCreateDay(new Date());
                            ordItemAdditSuppGoodsDao.insertOrdItemAdditSuppGoods(ordItemAdditSuppGoods);
                            logger.info("end save ordeItemAdditSuppGoods, orderItemId:"+orderItem.getOrderItemId()+"additSuppGoodsId:"+additSuppGoodsVo.getAdditSuppGoodsId());

                        }


                    }
                }
            }



            OrderItemSaveBussiness saveBussiness = orderOrderFactory.createSaveProduct(orderItem);
            if(saveBussiness!=null){
                long startTime = System.currentTimeMillis();
                saveBussiness.saveAddition(order, orderItem);
                logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-saveBussiness", "saveOrder.saveBussiness"+saveBussiness.getClass().getSimpleName(), System.currentTimeMillis() - startTime));
            }

            List<OrdSettlementPriceRecord> ordSettleList = orderItem.getOrdSettlementPriceRecordList();
            if(ordSettleList!=null&&ordSettleList.size()>0){
                long startTime = System.currentTimeMillis();
                for(OrdSettlementPriceRecord ordSettle:ordSettleList){
                    ordSettle.setOrderItemId(orderItem.getOrderItemId());
                    ordSettle.setOrderId(order.getOrderId());
                    iOrdSettlementPriceRecordService.insert(ordSettle);
                }
                logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_SETTLEMENT_PRICE_RECORD", "saveOrder.ORD_SETTLEMENT_PRICE_RECORD", System.currentTimeMillis() - startTime));
            }

        }
        //这里刷新一下order 表   源代码copy 过来的 可能前面的操作有需要持久 化的对象
        orderDao.updateByPrimaryKey(order);
        //这里刷新一下order 表   源代码copy 过来的 可能前面的操作有需要持久 化的对象
    }

    @Override
    public void orderItemTravelDbStore(OrdOrderDTO order) {
        //保存游客相关的信息
        if(CollectionUtils.isNotEmpty(order.getOrdPersonList())){
            long startTime = System.currentTimeMillis();
            for(OrdPerson person:order.getOrdPersonList()){
                person.setObjectId(order.getOrderId());
                person.setObjectType("ORDER");
                personDao.insertSelective(person);
                this.doSaveAddressInPerson(person);
            }
            logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PERSON", "saveOrder.ORD_PERSON", System.currentTimeMillis() - startTime));
        }

    }

    /**
     * 保存联系人的地址
     *
     * @param person
     */
    private void doSaveAddressInPerson(OrdPerson person) {
        if ((person != null) && (person.getAddressList() != null)) {
            for (OrdAddress address : person.getAddressList()) {
                address.setOrdPersonId(person.getOrdPersonId());
                ordAddressDao.insertSelective(address);
            }
        }
    }


    @Override
    public void orderAmountTravelDbStore(OrdOrderDTO order) {

        for(OrdOrderAmountItem amountItem:order.getOrderAmountItemList()){
        	amountItem.setOrderId(order.getOrderId());
			orderAmountItemDao.insert(amountItem);
		}
        
    }




//    @Override
//    public void orderSaleDbStore(OrdOrderDTO order) {
//
//    }

    @Override
    public void ordePromotionDbStore(OrdOrderDTO order) {
        for (OrdOrderItem orderItem:order.getOrderItemList()) {
            if (CollectionUtils.isNotEmpty(orderItem.getOrdPromotionList())) {
                long startTime = System.currentTimeMillis();
                for (OrdPromotion prom : orderItem.getOrdPromotionList()) {
                    prom.setOrderItemId(orderItem.getOrderItemId());
                    promotionDao.insert(prom);
                }
                logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PROMOTION", "saveOrder.ORD_PROMOTION", System.currentTimeMillis() - startTime));
            }
        }

        if(MapUtils.isNotEmpty(order.getPromotionMap())){
            long  startTime = System.currentTimeMillis();
            for(String key:order.getPromotionMap().keySet()){
                List<OrdPromotion> list = order.getPromotionMap().get(key);
                for(OrdPromotion op:list){
                    if(OrdPromotion.ObjectType.ORDER_PACK.name().equals(op.getObjectType())){
                        OrdOrderPack pack = (OrdOrderPack)op.getTarget();
                        op.setOrderItemId(pack.getOrderPackId());
                    }else{
                        OrdOrderItem pack = (OrdOrderItem)op.getTarget();
                        op.setOrderItemId(pack.getOrderItemId());
                    }
                    ordPromotionDao.insert(op);
                    if("Y".equals(op.getOccupyAmountFlag())){
                        promPromotionService.addPromAmount(op.getFavorableAmount(), op.getPromPromotionId());
                    }
                }
            }
            logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-PROM_PROMOTION", "saveOrder.PROM_PROMOTION", System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public void ordTravelContractDbStore(OrdOrderDTO order) {
        if ((order != null) && (order.getOrdTravelContractList() != null)) {
            for (OrdTravelContract ordTravelContract : order.getOrdTravelContractList()) {
                ordTravelContract.setOrderId(order.getOrderId());
                int cid = ordTravelContractService.saveOrdTravelContract(ordTravelContract, "SYSTEM");
                List<OrdOrderItem> items = ordTravelContract.getOrderItems();
                if(items!=null){
                    for(OrdOrderItem item:items){
                        if("true".equals(item.getMainItem())){
                            OrdItemContractRelation relation = new OrdItemContractRelation();
                            relation.setOrdContractId(Long.valueOf(cid));
                            relation.setOrderItemId(item.getOrderItemId());
                            relation.setCreateTime(new Date());
                            iOrdItemContractRelationService.insert(relation);
                        }
                    }
                }
            }
        }
    }

    private OrdOrderAmountItem makeOrderAmountItem(
            OrderEnum.ORDER_AMOUNT_TYPE type, OrderEnum.ORDER_AMOUNT_NAME name,
            long totalAmount) {
        OrdOrderAmountItem item = new OrdOrderAmountItem();
        item.setItemAmount(totalAmount);
        item.setOrderAmountType(type.name());
        item.setItemName(name.getCode());
        return item;
    }

}
