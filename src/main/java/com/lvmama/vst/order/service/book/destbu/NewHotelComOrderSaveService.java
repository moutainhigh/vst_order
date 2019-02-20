package com.lvmama.vst.order.service.book.destbu;

import java.util.Date;
import java.util.List;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comm.vo.ResultHandleT;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsGroupVO;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsVo;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdAdditionStatusDAO;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdFormInfoDao;
import com.lvmama.vst.order.dao.OrdHotelCombInfoDao;
import com.lvmama.vst.order.dao.OrdItemAdditSuppGoodsDao;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdPromotionDao;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdSettlementPriceRecordService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.PromPromotionService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import static org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.LOG;

@Component("newHotelCombOrderSaveService")
public  class NewHotelComOrderSaveService  {

	private static final Logger logger = LoggerFactory.getLogger(NewHotelComOrderSaveService.class);
	
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
	private  OrderOrderFactory orderOrderFactory;

	@Autowired
	private SuppSettlementEntityClientService suppSettlementEntityClientService;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	/**
	 * 保证事务一置性，保存订单相关信息，
	 * 库存扣除第二部
	 * @param order
	 */
	  public void  saveOrder(OrdOrderDTO order )  throws BusinessException {
		  
			order.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
			order.setCancelCode(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name());
			order.setReason(OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.getCnName());

			Long startTime = System.currentTimeMillis();
			logger.info("saveOrder保存订单开buCode"+order.getBuCode());

			Long orderId = orderDao.saveOrder(order);
			logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));
			
			if(CollectionUtils.isNotEmpty(order.getOrdAdditionStatusList())){
				startTime = System.currentTimeMillis();
				for(OrdAdditionStatus status:order.getOrdAdditionStatusList()){
					status.setOrderId(orderId);
					ordAdditionStatusDAO.insert(status);
				}
				logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_ADDITION_STATUS", "saveOrder.ORD_ADDITION_STATUS", System.currentTimeMillis() - startTime));
			}

			//保存游客相关的信息
 			if(CollectionUtils.isNotEmpty(order.getOrdPersonList())){
				startTime = System.currentTimeMillis();
				doSavePerson(orderId, order.getOrdPersonList());
				logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PERSON", "saveOrder.ORD_PERSON", System.currentTimeMillis() - startTime));
			}

		
			//xiaorui add end
			List<Long> goodList = null;
			List<Date> dateList = null;
			for(OrdOrderItem orderItem:order.getOrderItemList()){
				orderItem.setOrderId(orderId);
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
						throw new BusinessException(" [ NewHotelComOrderSaveService ] , ERROR : suppgoods is is null in orderItem ");
					}

					ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
					if(!suppGoodsResultHandleT.isSuccess()){
						throw new BusinessException(" [ NewHotelComOrderSaveService ] , ERROR : can not find suppGoods by id ("+suppGoodsId+")");
					}

					SuppGoods suppGoods = suppGoodsResultHandleT.getReturnContent();
					String settleEntityCode = suppGoods.getSettlementEntityCode();
					if(StringUtils.isEmpty(settleEntityCode)){
                    	throw new BusinessException(" [ NewHotelComOrderSaveService ] , ERROR : suppGoods has no settlement entity code!!! suppGoods id is " + suppGoods.getSuppGoodsId());
                    }

					ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(settleEntityCode);
					if(!resultHandleT.isSuccess()){
						throw new BusinessException(" [ NewHotelComOrderSaveService ] , ERROR : find SuppSettlementEntities by code ("+settleEntityCode+" ) failed");
					}

					SuppSettlementEntities settlementEntities = resultHandleT.getReturnContent();
					if(null==settlementEntities){
						throw new BusinessException(" [ NewHotelComOrderSaveService ] , ERROR : can not find SuppSettlementEntities by code ("+settleEntityCode+" )");
					}

					// 存入订单表中的值为 ID_CODE形式
					String codeValue = settlementEntities.getId()+"_"+settlementEntities.getCode();
					orderItem.setSettlementEntityCode(codeValue);

				}catch (Exception e){
					logger.error(" [ NewHotelComOrderSaveService ] set orderItem settleEntityCode has exception, error msg : "+e.getMessage());
				}

				logger.info("start save orderItem, orderId:" + orderItem.getOrderId());
				orderItemDao.insert(orderItem);
				logger.info("end save orderItem, orderId:" + orderItem.getOrderId());
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
							
				if(CollectionUtils.isNotEmpty(orderItem.getOrdPromotionList())){
					startTime = System.currentTimeMillis();
					for(OrdPromotion prom:orderItem.getOrdPromotionList()){
						prom.setOrderItemId(orderItem.getOrderItemId());
						promotionDao.insert(prom);
					}
					logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_PROMOTION", "saveOrder.ORD_PROMOTION", System.currentTimeMillis() - startTime));
				}

		OrderItemSaveBussiness saveBussiness = orderOrderFactory.createSaveProduct(orderItem);
			if(saveBussiness!=null){
				startTime = System.currentTimeMillis();
					saveBussiness.saveAddition(order, orderItem);
					logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-saveBussiness", "saveOrder.saveBussiness"+saveBussiness.getClass().getSimpleName(), System.currentTimeMillis() - startTime));
			}

				List<OrdSettlementPriceRecord> ordSettleList = orderItem.getOrdSettlementPriceRecordList();
				if(ordSettleList!=null&&ordSettleList.size()>0){
					startTime = System.currentTimeMillis();
					for(OrdSettlementPriceRecord ordSettle:ordSettleList){
						ordSettle.setOrderItemId(orderItem.getOrderItemId());
						ordSettle.setOrderId(orderId);
						iOrdSettlementPriceRecordService.insert(ordSettle);
					}
					logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_SETTLEMENT_PRICE_RECORD", "saveOrder.ORD_SETTLEMENT_PRICE_RECORD", System.currentTimeMillis() - startTime));
				}

			}

			startTime = System.currentTimeMillis();
			orderDao.updateByPrimaryKey(order);
			logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存.update-ORD_ORDER", "saveOrder.ORD_ORDER", System.currentTimeMillis() - startTime));
			
			
			if(MapUtils.isNotEmpty(order.getPromotionMap())){
				startTime = System.currentTimeMillis();
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
			doSaveOrdTravelContractInOrder(order);
			logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", "saveOrder.ORD_TRAVEL_CONTRACT+ORD_ITEM_CONTRACT_RELATION", System.currentTimeMillis() - startTime));
			
			//保存订单查询的相关信息
			startTime = System.currentTimeMillis();
	
			logger.info(ComLogUtil.printTraceInfo("saveOrder", "保存-ORD_GUARANTEE_CREDIT_CARD", "saveOrder.ORD_GUARANTEE_CREDIT_CARD", System.currentTimeMillis() - startTime));

	        logger.info("===>saveOrder->deductStock"+"    order size = "+order.getUpdateStockMap().size());
	}
	  private void doSavePerson(Long orderId, List<OrdPerson> ordPersonList) {
			for(OrdPerson person:ordPersonList){
				person.setObjectId(orderId);
				personDao.insertSelective(person);
				this.doSaveAddressInPerson(person);
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


}
