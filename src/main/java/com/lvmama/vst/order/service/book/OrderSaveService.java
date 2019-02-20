/**
 * 
 */
package com.lvmama.vst.order.service.book;


import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;
import com.lvmama.commons.logging.LvmamaLog;
import com.lvmama.commons.logging.LvmamaLogFactory;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.order.enums.OrdProcessKeyEnum;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderTravellerConfirmClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.precontrol.service.ResWarmRuleClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResWarmRule;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.dao.tag.OrderTagDao;
import com.lvmama.vst.order.dao.tag.TagEnum;
import com.lvmama.vst.order.dao.tag.po.OrderTag;
import com.lvmama.vst.order.route.constant.VstRouteConstants;
import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TAG;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comlog.LvmmLogEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.DestBuOrderPropUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelcombOption;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdAdditionStatusDAO;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdFormInfoDao;
import com.lvmama.vst.order.dao.OrdGuaranteeCreditCardDao;
import com.lvmama.vst.order.dao.OrdHotelCombInfoDao;
import com.lvmama.vst.order.dao.OrdItemRescheduleDao;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdOrderSharedStockDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdProcessKeyDao;
import com.lvmama.vst.order.dao.OrdPromotionDao;
import com.lvmama.vst.order.route.service.IOrderRouteRelationInfoService;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.vo.*;
import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;
import com.lvmama.vst.suppTicket.client.product.po.IntfGoodsPriceIdRela;
import com.lvmama.vst.suppTicket.client.product.service.SuppTicketProductClientService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.lvmama.vst.back.prod.po.ProdProduct;

/**
 * 订单保存操作
 * @author lancey
 *
 */
@Service
public class OrderSaveService extends AbstractBookService{

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderSaveService.class);
	private static final LvmamaLog lvmamaLog = LvmamaLogFactory.getLog(OrderSaveService.class);

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
	private OrdGuaranteeCreditCardDao guaranteeCreditCardDao;

	@Autowired
	private IOrdTravelContractService ordTravelContractService;

	@Autowired
	private OrdPromotionDao ordPromotionDao;

	@Autowired
	private OrdAdditionStatusDAO ordAdditionStatusDAO;

	@Autowired
	private OrdFormInfoDao ordFormInfoDao;

	@Autowired
	private IOrdItemContractRelationService iOrdItemContractRelationService;

	@Autowired
	private IOrdSettlementPriceRecordService iOrdSettlementPriceRecordService;
	@Autowired
	private PromPromotionService promPromotionService;
	

	@Autowired
	private ResPreControlService resControlBudgetRemote;

    @Autowired
    private ResWarmRuleClientService resWarmRuleClientService;

    @Resource
    private TopicMessageProducer resPreControlEmailMessageProducer;

    @Autowired
    private IOrdOrderDisneyInfoService ordOrderDisneyInfoService;

	@Resource(name="orderTravellerConfirmClientServiceRemote")
	private OrderTravellerConfirmClientService orderTravellerConfirmClientService;
	
	@Autowired
	SuppTicketProductClientService suppTicketProductClientService;
	
	@Autowired
	OrdItemShowTicketInfoService ordItemShowTicketInfoService;
    
	@Autowired
	private IHotelTradeApiService hotelTradeApiService;
	
	@Resource(name="orderTimePriceService")
	private OrderTimePriceService orderTimePriceService;

	@Resource
	private OrdOrderAdditionalInfoService ordOrderAdditionalInfoService;
    
    @Autowired
    private OrdOrderSharedStockDao ordOrderSharedStockDao;


    @Autowired
    private OrdItemRescheduleDao ordItemRescheduleDao;

	@Autowired
	private DestHotelAdapterUtils destHotelAdapterUtils;

	@Autowired
	private SuppSettlementEntityClientService suppSettlementEntityClientService;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private IOrderRouteRelationInfoService orderRouteRelationInfoService;
	
	
	@Autowired
	private PromotionService promotionService;
	
	@Autowired
    private ProdProductClientService productClientService;

	@Resource(name = "orderTagDao")
	private OrderTagDao orderTagDao;
	
	@Resource(name = "ordProcessKeyDao")
	private OrdProcessKeyDao ordProcessKeyDao;

	@Autowired
	private OrdOrderItemExtendDao ordOrderItemExtendDao;


	/**
	 * 保证事务一置性，保存订单相关信息，
	 * 库存扣除
	 * @param order
	 */
	public void saveOrder(OrdOrderDTO order){
		order.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
		order.setCancelCode(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name());
		order.setReason(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.getCnName());

		Long startTime = System.currentTimeMillis();
		Log.info("distributionCpsID-------------"+order.getDistributionCpsID());
		Long orderId = null;
		
		//设置 团结算标识 涉及品类 线路  和游轮
		setGroupSettleFlag(order);
		
		if(order.getOrderId()!=null){
			orderId = orderDao.saveOrderForPreLockSeat(order);
		}else{
			orderId = orderDao.saveOrder(order);
		}
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));
		if(CollectionUtils.isNotEmpty(order.getOrdAdditionStatusList())){
			startTime = System.currentTimeMillis();
			for(OrdAdditionStatus status:order.getOrdAdditionStatusList()){
				status.setOrderId(orderId);
				ordAdditionStatusDAO.insert(status);
			}
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ADDITION_STATUS", "saveOrder.ORD_ADDITION_STATUS", System.currentTimeMillis() - startTime));
		}

		//保存游客相关的信息
		if(CollectionUtils.isNotEmpty(order.getOrdPersonList())){
			startTime = System.currentTimeMillis();
			doSavePerson(orderId, order.getOrdPersonList());
			lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.CREATE_ORDER.name(), order.getOrderId(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "插入游玩人成功(", "插入游玩人成功，人数为：" + order.getOrdPersonList().size());
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PERSON", "saveOrder.ORD_PERSON", System.currentTimeMillis() - startTime));
		}

		//保存游玩人确认信息，仅仅后置的订单保存，其它的订单不保存
		BuyInfo buyInfo = order.getBuyInfo();
		if(buyInfo!=null && buyInfo.getOrderTravellerConfirm() != null && Constants.Y_FLAG.equalsIgnoreCase(buyInfo.getTravellerDelayFlag())){
			OrdOrderTravellerConfirm ordOrderTravellerConfirm= buyInfo.getOrderTravellerConfirm();
			ordOrderTravellerConfirm.setOrderId(orderId);

			OrderTravellerOperateDO ordOrderTravellerConfirmVo = new OrderTravellerOperateDO();
			ordOrderTravellerConfirmVo.setOrderTravellerConfirm(ordOrderTravellerConfirm);
			//设定操作人
			ordOrderTravellerConfirmVo.setUserCode(String.valueOf(order.getUserId()));
			orderTravellerConfirmClientService.saveOrUpdateOrderTravellerConfirmInfo(ordOrderTravellerConfirmVo);
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORDER_TRAVELLER_CONFIRM_VO", "saveOrder.ORDER_TRAVELLER_CONFIRM_VO", System.currentTimeMillis() - startTime));
		}

		//保存额外信息(比如订单目的地)
		if (OrderUtil.isTrafficPlusXOrder(order)) {
			OrdOrderAdditionalInfo ordOrderAdditionalInfo = this.createOrderAdditionalInfo(order);
			if (ordOrderAdditionalInfo != null) {
				ordOrderAdditionalInfoService.insert(ordOrderAdditionalInfo);
			}
		}

		if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
			startTime = System.currentTimeMillis();
			for(OrdOrderPack orderPack:order.getOrderPackList()){
				orderPack.setOrderId(orderId);
				/**分销回传的产品名称**/
				if (buyInfo != null && buyInfo.getProductNewNameMap() != null
						&& buyInfo.getProductNewNameMap().size() > 0) {
					Set<Entry<Long, String>> set = buyInfo.getProductNewNameMap().entrySet();
					for (Entry<Long, String> productNewName : set) {
						Long key = productNewName.getKey();//product id
						String value = productNewName.getValue();//product name
						if(key!=null&&key.equals(orderPack.getProductId())){
							orderPack.setProductName(value);
							logger.info("tnt send new productName to table ord_order_pack:productId="+key+" ,productName="+value);
							break;
						}
					}
				}
				orderPackDao.insert(orderPack);
			}
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER_PACK", "saveOrder.ORD_ORDER_PACK", System.currentTimeMillis() - startTime));
		}
		
		
		//TODO xiaorui add begin
		String orderDisneyInfo=order.getDisneyOrderInfo();
		logger.info("order check shanghaiDisney showticket info :" + orderDisneyInfo);
		if(StringUtils.isNotEmpty(orderDisneyInfo)){
			OrdOrderDisneyInfo disInfo=new OrdOrderDisneyInfo();
			disInfo.setOrderId(order.getOrderId());
			disInfo.setContent(orderDisneyInfo);
			ordOrderDisneyInfoService.insert(disInfo);
			logger.info("order save shanghaiDisney showticket info end...." + disInfo.getId());
		}
		//xiaorui add end
		List<Long> goodList = null;
		List<Date> dateList = null;
		for(OrdOrderItem orderItem:order.getOrderItemList()){
			orderItem.setOrderId(orderId);

            //设置该订单所属的用户组
            SuppGoods suppGoods = orderItem.getSuppGoods();
            if(null != suppGoods){
                orderItem.setEbkSupplierGroupId(suppGoods.getEbkSupplierGroupId());
            }else{
                orderItem.setEbkSupplierGroupId(0L);
            }

			if(orderItem.getOrderPack()!=null){
				orderItem.setOrderPackId(orderItem.getOrderPack().getOrderPackId());
			}
			//如果是券不走买断库存
			if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(orderItem.getOrderSubType())){
				orderItem.setBuyoutFlag("N");
			}
			// 如果是预控资源那么进行扣减
			if("Y".equals(orderItem.getBuyoutFlag())){
				SuppGoods goods = orderItem.getSuppGoods();
				Long goodsId = goods.getSuppGoodsId();
				Date visitDate = orderItem.getVisitTime();
				
				int thisOrderItemCategoryId = orderItem.getCategoryId().intValue();
				switch (thisOrderItemCategoryId) {
				case 1:
					logger.info("酒店更新预控资源请查看saveBussiness.saveAddition(order, orderItem)方法");
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
								logger.info("按金额预控-更新成功");
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
								logger.info("按库存预控-更新成功");
								sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO,leftQuantity,leftStore);
							}
							//如果预控库存已经没了，清空该商品在这一天的预控缓存
							if(leftStore == 0 && reduceResult&&!cancelFlag){
								resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,visitDate,goodsId);
							}
						}
						if(reduceResult){
							logger.info("扣减预控资源成功，订单号："+orderItem.getOrderId()+"子订单号："+orderItem.getOrderItemId()+",商品id:"+orderItem.getSuppGoodsId()+"，数量："+orderItem.getBuyoutQuantity()+",总价："+orderItem.getBuyoutTotalPrice());
						}
					}
					break;
				}
				
			}
			/**分销回传的产品名称**/
			if (buyInfo != null && buyInfo.getProductNewNameMap() != null
					&& buyInfo.getProductNewNameMap().size() > 0) {
				Set<Entry<Long, String>> set = buyInfo.getProductNewNameMap().entrySet();
				for (Entry<Long, String> productNewName : set) {
					Long key = productNewName.getKey();//product id
					String value = productNewName.getValue();//product name
					if(key!=null&&key.equals(orderItem.getProductId())){
						orderItem.setProductName(value);
						logger.info("tnt send new productName info :productId="+key+" ,productName="+value);
						break;
					}
				}
			}
			
			//改----这边只扣减预控的资源
			//end

			// 保存子订单中 结算对象ID及CODE
			try{
				String settleEntityCode = suppGoods.getSettlementEntityCode();
				if(org.apache.commons.lang3.StringUtils.isEmpty(settleEntityCode)){
					Long suppGoodsId = suppGoods.getSuppGoodsId();
					ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
					if(suppGoodsResultHandleT.isSuccess() && suppGoodsResultHandleT.getReturnContent()!=null){
						SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
						settleEntityCode = targetSuppGoods.getSettlementEntityCode();
					}else{
						throw new BusinessException(" [ OrderSaveService ] , ERROR : suppGoods has no settlement entity code!!! suppGoods id is "+suppGoods.getSuppGoodsId());
					}
				}

				ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(settleEntityCode);
				if(!resultHandleT.isSuccess()){
					throw new BusinessException(" [ OrderSaveService ] , ERROR : find SuppSettlementEntities by code ("+settleEntityCode+" ) failed");
				}

				SuppSettlementEntities settlementEntities = resultHandleT.getReturnContent();
				if(null==settlementEntities){
					throw new BusinessException(" [ OrderSaveService ] , ERROR : can not find SuppSettlementEntities by code ("+settleEntityCode+" )");
				}

				// 存入订单表中的值为 ID_CODE形式
				String codeValue = settlementEntities.getId()+"_"+settlementEntities.getCode();
				orderItem.setSettlementEntityCode(codeValue);

			}catch (Exception e){
				logger.error(" [ OrderSaveService ] set orderItem settleEntityCode has exception, error msg : "+e.getMessage());
			}

			logger.info("start save orderItem, orderId:" + orderItem.getOrderId());
			if(orderItem.getOrderItemId()!=null){
				orderItemDao.insertForPreLockSeat(orderItem);
			}else{
				orderItemDao.insert(orderItem);
			}
			logger.info("end save orderItem, orderId:" + orderItem.getOrderId());
			//保存订单路由信息
			Date currentTime = Calendar.getInstance().getTime();
			orderRouteRelationInfoService.insert(new OrderRouteRelationInfo(orderId, orderItem.getOrderItemId(), order.getCategoryId(), orderItem.getCategoryId(), order.getDistributorId(), order.getDistributorCode(), order.getIsTestOrder()=='\0'?'N':order.getIsTestOrder(), 'Y', currentTime, currentTime));
			logger.info("Order route relation info saved, order id is " + orderId);
			// 订单快照
			if (orderItem.getHotelcombOptions()!=null && orderItem.getHotelcombOptions().size()>0) {
				startTime = System.currentTimeMillis();
				for (HotelcombOption hotelcomb : orderItem.getHotelcombOptions()) {
					hotelcomb.setOrderId(orderItem.getOrderId());
					hotelcomb.setOrderItemId(orderItem.getOrderItemId());
					if(hotelcomb.getType()!=null){
						if(hotelcomb.getType().equalsIgnoreCase(Constant.PRODUCT_TYPE.HOTEL.getCode())){
							hotelcomb.setCategoryId(1L);
						}
						if(hotelcomb.getType().equalsIgnoreCase(Constant.PRODUCT_TYPE.TICKET.getCode())){
							hotelcomb.setCategoryId(5L);
						}
					}
					ordHotelCombInfoDao.insert(hotelcomb);
				}
				Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_HOTELCOMB_INFO", "saveOrder.ORD_HOTELCOMB_INFO", System.currentTimeMillis() - startTime));
			}
						
			if(CollectionUtils.isNotEmpty(orderItem.getOrdPromotionList())){
				startTime = System.currentTimeMillis();
				for(OrdPromotion prom:orderItem.getOrdPromotionList()){
					prom.setOrderItemId(orderItem.getOrderItemId());
					promotionDao.insert(prom);
				}
				Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PROMOTION", "saveOrder.ORD_PROMOTION", System.currentTimeMillis() - startTime));
			}

			OrderItemSaveBussiness saveBussiness = orderOrderFactory.createSaveProduct(orderItem);
			if(saveBussiness!=null){
				startTime = System.currentTimeMillis();
				saveBussiness.saveAddition(order, orderItem);
				Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-saveBussiness", "saveOrder.saveBussiness"+saveBussiness.getClass().getSimpleName(), System.currentTimeMillis() - startTime));
			}

			List<OrdSettlementPriceRecord> ordSettleList = orderItem.getOrdSettlementPriceRecordList();
			if(ordSettleList!=null&&ordSettleList.size()>0){
				startTime = System.currentTimeMillis();
				for(OrdSettlementPriceRecord ordSettle:ordSettleList){
					ordSettle.setOrderItemId(orderItem.getOrderItemId());
					ordSettle.setOrderId(orderId);
					iOrdSettlementPriceRecordService.insert(ordSettle);
				}
				Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_SETTLEMENT_PRICE_RECORD", "saveOrder.ORD_SETTLEMENT_PRICE_RECORD", System.currentTimeMillis() - startTime));
			}
			
			//保存永乐演出票信息
			String specialTicketType = suppGoods.getSpecialTicketType();
			Long categoryId = orderItem.getCategoryId();
			if("YONGLE_SHOW_TICKET".equals(specialTicketType)&&categoryId==31L){
				Long suppGoodsId = suppGoods.getSuppGoodsId();
				Date visitTime = orderItem.getVisitTime();
				Map<String,Object> objectMap = new HashMap<String,Object>();
				objectMap.put("suppGoodsId", suppGoodsId);
				objectMap.put("visitDate", visitTime);
				try {
					IntfGoodsPriceIdRela intfGoodsPriceIdRela = suppTicketProductClientService.queryGoodsPriceIdRela(objectMap);
					OrdItemShowTicketInfoVO showTicketInfo = new OrdItemShowTicketInfoVO();
					showTicketInfo.setOrderItemId(orderItem.getOrderItemId());
					showTicketInfo.setPriceId(Long.valueOf(intfGoodsPriceIdRela.getPriceId()));
					showTicketInfo.setPrice(orderItem.getSettlementPrice());
					showTicketInfo.setPriceType(Long.valueOf(intfGoodsPriceIdRela.getPriceType()));
					ordItemShowTicketInfoService.insert(showTicketInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

            OrdItemReschedule ordItemReschedule = orderItem.getOrdItemReschedule();
            if(ordItemReschedule!=null){
                startTime = System.currentTimeMillis();
                ordItemReschedule.setOrderItemId(orderItem.getOrderItemId());
                ordItemRescheduleDao.insertSelective(ordItemReschedule);
                Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ITEM_RESCHEDULE", "saveOrder.ORD_ITEM_RESCHEDULE", System.currentTimeMillis() - startTime));
            }
            //保存信用住标识
			Long orderItemId=orderItem.getOrderItemId();
			String creditTag=orderItem.getCreditTag()==null?"N":orderItem.getCreditTag();
            Log.info("保存信用住信息开始{}={}",orderItemId,creditTag);
            if("Y".equalsIgnoreCase(creditTag)){
				orderTagDao.insert(new OrderTag(orderItemId, TagEnum.ORD_OBJECT_TAG.ORD_ORDER_ITEM.name(),creditTag,TagEnum.ORD_ORDER_TAG.ORD_CREDIT_TAG.name()));
			}
			Log.info("保存子订单工作流信息开始{},{}", orderId, orderItemId);
			if (CollectionUtils.isNotEmpty(orderItem.getOrdProcessKeyList())) {
				List<OrdProcessKey> itemOrdProcessKeys = orderItem.getOrdProcessKeyList();
				for (OrdProcessKey ordProcessKey : itemOrdProcessKeys) {
					ordProcessKey.setOrderId(orderId);
					ordProcessKey.setObjectId(orderItemId);
					ordProcessKeyDao.insert(ordProcessKey);
				}
			}
        }

		startTime = System.currentTimeMillis();
		orderDao.updateByPrimaryKey(order);
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存.update-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));
			
		if(MapUtils.isNotEmpty(order.getPromotionMap())){
			startTime = System.currentTimeMillis();
			List<Long> listProms = new ArrayList<>();
			for(String key:order.getPromotionMap().keySet()){
				List<OrdPromotion> list = order.getPromotionMap().get(key);
				for(OrdPromotion op:list){
					listProms.add(op.getPromPromotionId());
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
			//保存促销升级信息
			promotionService.addPromOrderAndPromUserNumber(order.getUserNo(), listProms);
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-PROM_PROMOTION", "saveOrder.PROM_PROMOTION", System.currentTimeMillis() - startTime));
		}

		startTime = System.currentTimeMillis();
		for(OrdOrderAmountItem item:order.getOrderAmountItemList()){
			item.setOrderId(orderId);
			orderAmountItemDao.insert(item);
		}
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER_AMOUNT_ITEM", "saveOrder.ORD_ORDER_AMOUNT_ITEM", System.currentTimeMillis() - startTime));

		if(CollectionUtils.isNotEmpty(order.getFormInfoList())){
			startTime = System.currentTimeMillis();
			for(OrdFormInfo info:order.getFormInfoList()){
				logger.info("orderId=" + orderId + "contentType=" + info.getContentType() + "content=" + info.getContent() + "===>" );
				info.setOrderId(orderId);
				ordFormInfoDao.insert(info);
			}
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_FORM_INFO", "saveOrder.ORD_FORM_INFO", System.currentTimeMillis() - startTime));
		}

		startTime = System.currentTimeMillis();
		doSaveGuaranteeCreditCardInOrder(order);
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_GUARANTEE_CREDIT_CARD", "saveOrder.ORD_GUARANTEE_CREDIT_CARD", System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		doSaveOrdTravelContractInOrder(order);
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", "saveOrder.ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", System.currentTimeMillis() - startTime));
		
		//保存订单查询的相关信息
		startTime = System.currentTimeMillis();
		doSaveOrderQueryInfo(orderId, order);
		Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_GUARANTEE_CREDIT_CARD", "saveOrder.ORD_GUARANTEE_CREDIT_CARD", System.currentTimeMillis() - startTime));

        logger.info("===>saveOrder->deductStock"+"    order size = "+order.getUpdateStockMap().size());


		//保存子单附加项ordOrderItemExtend信息
		doSaveOrdOrderItemExtendInfo(order);
        Log.info("保存订单工作流信息开始{}", orderId);
        if (CollectionUtils.isNotEmpty(order.getOrdProcessKeyList())) {
			startTime = System.currentTimeMillis();
			List<OrdProcessKey> ordProcessKeyList = order.getOrdProcessKeyList();
			for(OrdProcessKey ordProcessKey:ordProcessKeyList){
				ordProcessKey.setOrderId(orderId);
				ordProcessKey.setObjectId(orderId);
				ordProcessKeyDao.insert(ordProcessKey);
			}
			Log.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PROCESS_KEY", "saveOrder.ORD_PROCESS_KEY", System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * 保存子单附加项ordOrderItemExtend信息
	 * @param order
	 */
	private void doSaveOrdOrderItemExtendInfo(OrdOrderDTO order) {
		//开始于外币项目，存放外币结算,币种等附加信息 开始 at 2018.10.8
		// 保存子单附加项信息
		logger.info("开始保存子单附加项信息,orderId=" + order.getOrderId());
		if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
			for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
				try{
					logger.info("子单附加项信息保存,orderItemId=" + ordOrderItem.getOrderItemId());
					OrdOrderItemDTO scenicOrdOrderItemDTO = (OrdOrderItemDTO) ordOrderItem;
					OrdOrderItemExtendDTO scenicOrdOrderItemExtendDTO = scenicOrdOrderItemDTO.getOrdOrderItemExtendDTO();
					OrdOrderItemExtend ordOrderItemExtend = new OrdOrderItemExtend();
					EnhanceBeanUtils.copyProperties(scenicOrdOrderItemExtendDTO, ordOrderItemExtend);
					ordOrderItemExtend.setOrderItemId(ordOrderItem.getOrderItemId());//设置子单id
					Date date = new Date();
					ordOrderItemExtend.setCreateTime(date);
					ordOrderItemExtend.setUpdateTime(date);
					if (logger.isDebugEnabled()) {
						logger.debug("保存子单orderItemId=" + ordOrderItem.getOrderItemId() + ",附加信息ordOrderItemExtend=" + GsonUtils.toJson(ordOrderItemExtend));
					}
					//保存子单附加项
					logger.info("附加项信息"+ GsonUtils.toJson(ordOrderItemExtend));
					ordOrderItemExtendDao.insert(ordOrderItemExtend);
				} catch (Exception e) {
					logger.error("保存子单" + ordOrderItem.getOrderItemId() + "附加项信息发生异常" + e, e);
				}
			}
		}
		logger.info("完成保存子单附加项信息,orderId=" + order.getOrderId());
		//开始于外币项目，存放外币结算，币种等附加信息 结束 at 2018.10.8

	}

	//从订单中创建附加信息实体
	private OrdOrderAdditionalInfo createOrderAdditionalInfo(OrdOrderDTO order) {
		if(order == null || order.getOrderId() == null){
			return null;
		}
		if (order.getBuyInfo() == null) {
			logger.warn("order " + order.getOrderId() + " buyInfo is null, can't generate additional info");
			return null;
		}
		Long orderDestination = order.getBuyInfo().getOrderDestination();
		if (orderDestination == null) {
			logger.info("order " + order.getOrderId() + " destination is null, will not generate additional info");
			return null;
		}

		OrdOrderAdditionalInfo ordOrderAdditionalInfo = new OrdOrderAdditionalInfo();
		ordOrderAdditionalInfo.setOrderId(order.getOrderId());
		ordOrderAdditionalInfo.setOrderDestination(orderDestination);
		return ordOrderAdditionalInfo;
	}

    /**
     * 发送预控消息
     * @param goodsResPrecontrolPolicyVO
     * @param currentAmount 当前剩余金额/库存
     * @param leftAmount  剩余金额/库存
     */
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
	                	logger.info("按日-消耗完毕-发邮件");
	                    resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_DAILY_EMAIL", DateUtil.formatSimpleDate((new Date()))));
	                }
	            }
	            //按周期
	            if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
	                //买断“金额/库存”全部消耗完时，发邮件提醒
	                if(rules.contains("lossAll") && leftAmount.longValue() == 0){
	                	logger.info("按周期-消耗完毕-发邮件");
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
	                    	logger.info("按周期-消耗完百分比-发邮件");
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
	                    		logger.info("按周期-消耗完百分比-发邮件");
	                    		resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                    	}
	                    }
	                    
	                    /*for(int i = 1;totalAmount-reduce*i>=0;i++){
	                        if(currentAmount >= totalAmount-reduce*i && leftAmount < totalAmount-reduce*i){
	                        	resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                            break;
	                        }
	                    }*/
	                }
	            }
	        }
    	}catch(Exception e){
    		logger.error("买断预控，发送邮件出错："+e.getMessage());
    	}
    }
    
	/**
	 * 将订单重置为正常状态
	 * @param orderId
	 */
	public void resetOrderToNormal(Long orderId) {
		logger.info("resetOrderToNormal start, orderId:" + orderId);
		OrdOrder updateOrder = new OrdOrder();
		updateOrder.setOrderId(orderId);
		updateOrder.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
		updateOrder.setCancelCode("");
		updateOrder.setReason("");
		
		//更行订单
		orderDao.updateByPrimaryKeySelective(updateOrder);
		logger.info("resetOrderToNormal end, orderId:" + orderId);
	}
	
	/**
	 * 订单添加迪士尼标识-修改表字段tag值
	 * @return
	 * @author ltwangwei
	 * @date 2016-3-11 下午2:50:48
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	public boolean updateTagAndWaitPaymentTime4ShanghaiDisneyOrder(OrdOrder order) {
		logger.info("updateTagAndWaitPaymentTime4ShanghaiDisneyOrder start, orderId:" + order.getOrderId());
		// 支付前置订单且目的地BU自由行(含酒店或酒店套餐)或酒店套餐，或单酒店 PS:add by xiaoyulin
		if(OrdOrderUtils.isDestBuFrontOrder(order)){
			if(OrdOrderUtils.hasStockFlag(order)){
				order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), 30));
			}
			else{
				order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), 10));
			}
		}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equals(order.getPaymentType())){
			order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), 30));
		}
		logger.info("updateTagAndWaitPaymentTime4ShanghaiDisneyOrder orderWaitPaymentTime : " + order.getWaitPaymentTime());
		order.setTag(ORDER_TAG.DISNEY.getValue());
		//更新订单
		int updateCount = orderDao.updateTagAndWaitPaymentTime(order);
		logger.info("updateTagAndWaitPaymentTime4ShanghaiDisneyOrder end, orderId:" + order.getOrderId() + ",updateCount:" + updateCount);
		return updateCount > 0;
	}
	
	/**
	 * 保存订单查询的相关信息
	 * @param order
	 */
	private void doSaveOrderQueryInfo(Long orderId, OrdOrder order) {
		logger.info("save order query info start, orderId:" + orderId);
		String noticeRegimentStatus = null;
		Long mainProductId = null;
		String mainProductName = null;
		String bookerName = null;
		String bookerMobile = null;
		String contactName = null;
		String contactMobile = null;
		String contactPhone = null;
		String contactEmail = null;
		
		if(CollectionUtils.isNotEmpty(order.getOrdAdditionStatusList())){
			for(OrdAdditionStatus status:order.getOrdAdditionStatusList()){
				if(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name().equals(status.getStatusType())) {
					noticeRegimentStatus = status.getStatus();
				}
			}
		}

		//保存游客相关的信息
		if(CollectionUtils.isNotEmpty(order.getOrdPersonList())){
			for(OrdPerson person:order.getOrdPersonList()){
				if(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(person.getPersonType())) {
					bookerName = person.getFullName();
					bookerMobile = person.getMobile();
				}
				if(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(person.getPersonType())) {
					contactName = person.getFullName();
					contactMobile = person.getMobile();
					contactPhone = person.getPhone();
					contactEmail = person.getEmail();
				}
			}
		}

		if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
			for(OrdOrderPack orderPack:order.getOrderPackList()){
				mainProductId = orderPack.getProductId();
				mainProductName = orderPack.getProductName();
			}
		}

		for(OrdOrderItem orderItem:order.getOrderItemList()){
			OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();
			orderQueryInfo.setOrderId(orderItem.getOrderId());
			orderQueryInfo.setOrderItemId(orderItem.getOrderItemId());
			orderQueryInfo.setBackUserId(order.getBackUserId());
			orderQueryInfo.setCreateTime(order.getCreateTime());
			orderQueryInfo.setVisitTime(orderItem.getVisitTime());
			orderQueryInfo.setOrderStatus(order.getOrderStatus());
			orderQueryInfo.setInfoStatus(order.getInfoStatus());
			orderQueryInfo.setResourceStatus(order.getResourceStatus());
			orderQueryInfo.setPaymentStatus(order.getPaymentStatus());
			orderQueryInfo.setCertConfirmStatus(order.getCertConfirmStatus());
			orderQueryInfo.setNoticeRegimentStatus(noticeRegimentStatus);
			orderQueryInfo.setDistributorId(order.getDistributorId());
			orderQueryInfo.setMainProductId(mainProductId);
			orderQueryInfo.setProductId(orderItem.getProductId());
			orderQueryInfo.setMainProductName(mainProductName);
			orderQueryInfo.setProductName(orderItem.getProductName());
			orderQueryInfo.setSuppGoodsName(orderItem.getSuppGoodsName());
			orderQueryInfo.setSuppGoodsId(orderItem.getSuppGoodsId());
			orderQueryInfo.setMainManagerId(order.getManagerId());
			orderQueryInfo.setManagerId(orderItem.getManagerId());
			orderQueryInfo.setMainBuCode(order.getBuCode());
			orderQueryInfo.setBuCode(orderItem.getBuCode());
			orderQueryInfo.setMainCategoryId(order.getCategoryId());
			orderQueryInfo.setCategoryId(orderItem.getCategoryId());
			orderQueryInfo.setSupplierId(orderItem.getSupplierId());
			orderQueryInfo.setPaymentTarget(order.getPaymentTarget());
			orderQueryInfo.setFilialeName(order.getFilialeName());
			orderQueryInfo.setBookerName(bookerName);
			orderQueryInfo.setBookerMobile(bookerMobile);
			orderQueryInfo.setContactName(contactName);
			orderQueryInfo.setContactMobile(contactMobile);
			orderQueryInfo.setContactPhone(contactPhone);
			orderQueryInfo.setContactEmail(contactEmail);
			
//			orderQueryInfoService.saveOrderQueryInfo(orderQueryInfo);
		}
		
		logger.info("save order query info end, orderId:" + orderId);
	}

	private void doSavePerson(Long orderId, List<OrdPerson> ordPersonList) {
		for(OrdPerson person:ordPersonList){
			person.setObjectId(orderId);
			personDao.insertSelective(person);
			this.doSaveAddressInPerson(person);
		}
	}

	/**
	 * 保存订单的游玩人信息及与产品商品的关系
	 * @param order
	 */
	public void savePersonAndRelation(final Long orderId,OrdOrderDTO order){
		doSavePerson(orderId, order.getOrdPersonList());
		for(OrdOrderItem orderItem:order.getOrderItemList()){
			if(CollectionUtils.isNotEmpty(orderItem.getOrdItemPersonRelationList())){
				OrderItemSaveBussiness saveBussiness = orderOrderFactory.createSaveProduct(orderItem);
				saveBussiness.saveOrderItemPersonRelation(orderItem);
			}
		}
		
		//如果有游玩人确认信息，save
		BuyInfo buyInfo = order.getBuyInfo();
		if (buyInfo != null) {
			OrdOrderTravellerConfirm orderTravellerConfirm = buyInfo.getOrderTravellerConfirm();
			if (orderTravellerConfirm != null) {
				boolean effectiveValue = StringUtils.isNotEmpty(orderTravellerConfirm.getContainForeign())
						|| StringUtils.isNotEmpty(orderTravellerConfirm.getContainOldMan())
						|| StringUtils.isNotEmpty(orderTravellerConfirm.getContainPregnantWomen());
				if (effectiveValue) {
					orderTravellerConfirm.setOrderId(orderId);
//					orderTravellerConfirmClientService.saveOrderTravellerConfirmInfo(orderTravellerConfirm);
				}
			}
		}
	}

	/**
	 * 保存合同表
	 *
	 * @param order
	 */
	private void doSaveOrdTravelContractInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdTravelContractList() != null)) {
			for (OrdTravelContract ordTravelContract : order.getOrdTravelContractList()) {
				ordTravelContract.setOrderId(order.getOrderId());
				int cid = ordTravelContractService.saveOrdTravelContract(ordTravelContract, "SYSTEM");
				List<OrdOrderItem> items = ordTravelContract.getOrderItems();
				if(items!=null){
					for(OrdOrderItem item:items){
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



	/**
	 * 保存担保信用卡信息
	 *
	 * @param order
	 */
	private void doSaveGuaranteeCreditCardInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdGuaranteeCreditCardList() != null)) {
			for (OrdGuaranteeCreditCard card : order.getOrdGuaranteeCreditCardList()) {
				card.setOrderId(order.getOrderId());
				guaranteeCreditCardDao.insertSelective(card);
			}
		}
	}
	
	

	/**
	 * 库存扣减操作
	 */
	public List<HotelOrderUpdateStockDTO> deductStock(OrdOrderDTO order,List<HotelOrderUpdateStockDTO> asynchronousOrdUpdateStockList){
		Map<String,Object> dataMap;
		List<HotelOrderUpdateStockDTO> deductStocksResult = new ArrayList<HotelOrderUpdateStockDTO>();

		for(String key:order.getUpdateStockMap().keySet()){
            logger.info("#############deductStock############key="+key);
			OrdOrderUpdateStockDTO stock = order.getUpdateStockMap().get(key);
			Assert.notNull(stock.getTimePriceId());
			Assert.notNull(stock.getUpdateStock());
            logger.info("#############deductStock############stock="+stock.getUpdateStock());


			SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = stock.getTimePrice();
			OrdOrderItem orderItem = getOrderItem(order,suppGoodsBaseTimePrice);

			dataMap = new HashMap<String, Object>();
			if(orderItem.getCategoryId().equals(17L)){
				dataMap.put("isUpdateSuperStock", new Boolean(false));
			}else if (orderItem.getCategoryId().equals(1L)) {
				dataMap.put("isUpdateSuperStock", new Boolean(true));
			}
			dataMap.put("suppGoodsId", suppGoodsBaseTimePrice.getSuppGoodsId());
			dataMap.put("beginDate", suppGoodsBaseTimePrice.getSpecDate());
			dataMap.put("endDate", suppGoodsBaseTimePrice.getSpecDate());
			dataMap.put("orderItemId", orderItem.getOrderItemId());
            dataMap.put("orderItem", orderItem);
            if(suppGoodsBaseTimePrice != null && suppGoodsBaseTimePrice instanceof SuppGoodsAddTimePrice) {
            	dataMap.put("shareTotalStockId", ((SuppGoodsAddTimePrice)suppGoodsBaseTimePrice).getShareTotalStockId());
            	dataMap.put("shareDayLimitId", ((SuppGoodsAddTimePrice)suppGoodsBaseTimePrice).getShareDayLimitId());
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderItemId", orderItem.getOrderItemId());
            params.put("stockStatus", OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name());
            try {
            	boolean isEnable = false;
            	// 路由&灰度
            	isEnable = destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(orderItem.getSuppGoodsId());
				 logger.info("#############deductStock############orderItemid="+orderItem.getOrderItemId()+"isEnable="+isEnable);
				// 酒店品类，库存扣减
				if (isEnable) {
					HotelOrderUpdateStockDTO hotelOrderUpdateStockDTO = new HotelOrderUpdateStockDTO();
					hotelOrderUpdateStockDTO.setTimePriceId(stock.getTimePriceId());
					hotelOrderUpdateStockDTO.setUpdateStock(stock.getUpdateStock());
					hotelOrderUpdateStockDTO.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId());
					hotelOrderUpdateStockDTO.setOrderItemId(orderItem.getOrderItemId());
					hotelOrderUpdateStockDTO.setSpecDate(suppGoodsBaseTimePrice.getSpecDate());
					hotelOrderUpdateStockDTO.setSuppGoodsId(suppGoodsBaseTimePrice.getSuppGoodsId());
					ResultHandleT<HotelOrderUpdateStockDTO> result = hotelTradeApiService
							.deductStock(hotelOrderUpdateStockDTO);
					if (result.isSuccess()) {
						 logger.info("#############deductStock############updateSharedStock TimePriceId ="+stock.getTimePriceId());
						this.updateSharedStock(stock.getTimePriceId(), (OrdOrderItem) dataMap.get("orderItem"));
						// 库存可恢复
						if("Y".equals(suppGoodsBaseTimePrice.getRestoreFlag())){
							asynchronousOrdUpdateStockList.add(result.getReturnContent());
						}
					}else{
						logger.error("hotelTradeApiService ==> deductStock error:"+result.getMsg());
						throw new RuntimeException("hotelTradeApiService ==> deductStock error:"+result.getMsg());
					}
				} else {
					// 当扣减/返还库存为景点门票，其它票,组合套餐票商品时
					if(orderItem.getCategoryId().intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().intValue()
							||orderItem.getCategoryId().intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().intValue()
							||orderItem.getCategoryId().intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().intValue()){
						dataMap.put("categoryId",orderItem.getCategoryId());
						dataMap.put("aperiodicFlag",orderItem.getSuppGoods().getAperiodicFlag());
					}
					stock.getOrderTimePriceService().updateStock(stock.getTimePriceId(), stock.getUpdateStock(),
							dataMap);
					if(null != dataMap.get("orderTradeUpdateStockList") && "Y".equals(suppGoodsBaseTimePrice.getRestoreFlag())){
						List<HotelOrderUpdateStockDTO> deductStocks = (List<HotelOrderUpdateStockDTO>) dataMap.get("orderTradeUpdateStockList");
						deductStocksResult.addAll(deductStocks);
					}else{
						 logger.info("#############deductStock############orderTradeUpdateStockList size is 0 =");
					}
					
				}      	
            } catch (RuntimeException re) {
            	orderStockDao.updateStockStatusByOrderItemId(params);
            	throw re;
            }
            orderStockDao.updateStockStatusByOrderItemId(params);
		}
		return deductStocksResult;
	}

	
	
//	private OrdOrderItem getOrderItem(OrdOrder order,Long suppGoodsId){
//		for(OrdOrderItem orderItem:order.getOrderItemList()){
//			if(orderItem.getSuppGoodsId().equals(suppGoodsId)){
//				return orderItem;
//			}
//		}
//		return null;
//	}
	
	private OrdOrderItem getOrderItem(OrdOrder order,SuppGoodsBaseTimePrice suppGoodsBaseTimePrice){
		logger.info("...........deductStock........... getOrderItem start");
		if(CollectionUtils.isNotEmpty(order.getOrderItemList()))
		{
			String specDateStr = DateUtil.formatSimpleDate(suppGoodsBaseTimePrice.getSpecDate());
			List<OrdOrderItem> orderHotelItemList = new ArrayList<OrdOrderItem>();
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if(orderItem.getSuppGoodsId().equals(suppGoodsBaseTimePrice.getSuppGoodsId())){
					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
						orderHotelItemList.add(orderItem);
					}else{
						logger.info("...........deductStock........... return orderItem is not hotel ");
						return orderItem;
					}
				}
			}
			 
			if(CollectionUtils.isNotEmpty(orderHotelItemList)){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())){
					logger.info("...........deductStock........... return orderItem is  hotel ");
					return orderHotelItemList.get(0);
				}else{
					for (OrdOrderItem ordOrderItem : orderHotelItemList) {
						if(CollectionUtils.isNotEmpty(ordOrderItem.getOrderHotelTimeRateList())){
							for (OrdOrderHotelTimeRate hotelRate : ordOrderItem.getOrderHotelTimeRateList()) {
								String visitDateStr = DateUtil.formatSimpleDate(hotelRate.getVisitTime());
								logger.info("...........deductStock........... HotelTimeRateList  specDateStr: "+specDateStr+"__visitDateStr"+visitDateStr);
								if(visitDateStr.equals(specDateStr)){
									return ordOrderItem;
								}
							}
						}else{
							logger.info("...........deductStock........... HotelTimeRateList  is  null ");
						}
					}
				}
			}
			
		}		
		return null;
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

	/**
	 * 
	 * @Title: updateStock
	 * @Description: 扣减库存成功，将记录插入ORD_ORDER_SHARED_STOCK表中
	 * @param timePriceId
	 * @param orderItem
	 * @return void 返回类型
	 */
	private void updateSharedStock(Long timePriceId, OrdOrderItem orderItem) {
		logger.info("updateSharedStock=====> orderItem=" + orderItem.getOrderId());
		if (null != orderItem.getOrderStockList()) {
			Long groupId = orderItem.getSuppGoods().getGroupId();
			Long suppGoodsId = orderItem.getSuppGoods().getSuppGoodsId();
			logger.info("updateSharedStock=====> groupId=" + groupId + " SuppGoodsId=" + suppGoodsId);

			if(CollectionUtils.isNotEmpty(orderItem.getOrderStockList())){
				for (OrdOrderStock hotelOrderStock : orderItem.getOrderStockList()) {
					Date visitTime = hotelOrderStock.getVisitTime();
					SuppGoodsTimePrice timePrice = null;
					if(destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(suppGoodsId)){
						timePrice = hotelTradeApiService.getHotelGoodsTimePrice(suppGoodsId, visitTime);
						logger.info("timePrice json is:"+GsonUtils.toJson(timePrice));
					}else{
						timePrice = (SuppGoodsTimePrice) orderTimePriceService.getTimePrice(suppGoodsId, visitTime, true).getReturnContent();
					}
					
					if (null != timePrice && timePriceId.equals(timePrice.getTimePriceId())) {
						if (null == timePrice.getLatestHoldTime()) {
							timePrice.setLatestHoldTime(0L);
						}
						boolean hasSharedStock = false;
						Long stockGroup = hotelTradeApiService.hasSharedStock(groupId, visitTime, orderItem.getSuppGoodsId());
						if(null != stockGroup){
							hasSharedStock = true;
						}
						if (hasSharedStock) {
							// 设置共享库存非保留房当日扣共享库存
							DestBuOrderPropUtil.setCurrReduceShareStock(timePrice);
						}
						// 是否过了最晚预定时间
						if (timePrice.isBeforeLastHoldTime(new Date())) {
							OrdOrderSharedStock hotelOrderShareStock = new OrdOrderSharedStock();
							hotelOrderShareStock.setVisitTime(visitTime);
							hotelOrderShareStock.setOrderItemId(orderItem.getOrderItemId());
							hotelOrderShareStock.setQuantity(hotelOrderStock.getQuantity());
							hotelOrderShareStock.setInventory(hotelOrderStock.getInventory());
							hotelOrderShareStock.setResourceStatus(hotelOrderStock.getResourceStatus());
							hotelOrderShareStock.setNeedResourceConfirm(hotelOrderStock.getNeedResourceConfirm());
							if (hasSharedStock) {
								hotelOrderShareStock.setGroupId(groupId);
							}
							logger.info("ordOrderSharedStockDao insert :"+orderItem.getOrderItemId());
							ordOrderSharedStockDao.insert(hotelOrderShareStock);
						}
					}
				}
			}else{
				logger.info("updateSharedStock=====> OrderStockList is null =");
			}
		}
	}
	
	/***
	 * 设置团结算标识(游轮和线路)
	 * @param order
	 */
	private void setGroupSettleFlag(OrdOrderDTO order) {
		//该订单是游轮或者线路的品类,则反查产品的团结算属性,并赋值订单对象
		if(null!=order.getCategoryId()){
			if(order.getCategoryId()==8L||order.getCategoryId()==15L||order.getCategoryId()==16L||order.getCategoryId()==17L||order.getCategoryId()==18L){
				ProdProduct prod=productClientService.findProdProductById(order.getProductId(), true, true);
				if(null!=prod&&!prod.getPropValue().isEmpty()&&prod.getPropValue().containsKey("group_settle_flag")){
					logger.info("===================getGroupSettleFlag======productid"+order.getProductId()+" GroupSettleFlag:"+prod.getPropValue().containsKey("group_settle_flag"));
					order.setGroupSettleFlag(String.valueOf(prod.getPropValue().get("group_settle_flag")));
				}
				
			}
		}
		
	}

}
