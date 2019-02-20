/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.lvmama.vst.comm.vo.order.Person;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

/**
 * @author lancey
 * 
 */
public class OrdOrderDTO extends OrdOrder implements Serializable{

	/**
	 * 
	 */

    private static final Logger logger = LoggerFactory.getLogger(OrdOrderDTO.class);

	private static final long serialVersionUID = 6642055919895565179L;

	private BuyInfo buyInfo;
	
	private Map<OrdOrderItem, Map<Date, ItemPrice>> itemPriceTableMap;
	
	
	private int adult;
	private int child;
	
	private boolean validInitOrder;
	
	
	/**
	 * 默认的支付等待时间
	 */
	private int waitPaymentTimeSec=0;

	/**商品库存减少映射表，key-时间价格表ID，value-减少的库存数量**/
	private Map<Long, Long> stockMap = new HashMap<Long, Long>();
	
	private Map<Long, OrdOrderUpdateStockDTO> updateStockDTOMap = new HashMap<Long, OrdOrderUpdateStockDTO>();
	private Map<String,OrdOrderUpdateStockDTO> updateStockMap = new HashMap<String, OrdOrderUpdateStockDTO>();


	/**
	 * 商品和订单联系人关系
	 */
	private Map<Long,List<OrdPerson>> goodsPersonRelationMap = Maps.newHashMap();
	
	private Map<String,List<OrdPromotion>> promotionMap = new HashMap<String,List<OrdPromotion>>();
	private Long validPromtionAmount = 0l;//享受促销的金额
	
	/**
	 * 商品与api关系
	 */
	private Map<Long,Boolean> apiFlagMap = new HashMap<Long, Boolean>();
	
	private OrdOrderItem filterMainOrderItem;
	
	/**
	 * 快递地址
	 */
	private OrdPerson expressAddress;
	
	/**
	 * 是否是在生成订单
	 */
	private boolean createFlag = false;
	
	
	/**商品库存减少映射表，key= 前缀（amount_,inventory_）+ID，value-已使用的量**/
	private Map<String, Long> buyoutMap = new HashMap<String, Long>();
	
	
	public OrdOrderDTO() {
		
	}
	public OrdOrderDTO(BuyInfo buyInfo) {
		super();
		this.buyInfo = SerializationUtils.clone(buyInfo);
	}

	public BuyInfo getBuyInfo() {
		return buyInfo;
	}



	public void addUpdateStock(final SuppGoodsBaseTimePrice timePrice,long updateStock,OrderTimePriceService timePriceService){

        String key=timePriceService.getTimePricePrefix()+"_"+timePrice.getTimePriceId();
		OrdOrderUpdateStockDTO stock = updateStockMap.get(key);
        logger.info("#############key##########"+key);
        logger.info("#############updateStockMap  size##########"+updateStockMap.size());
        logger.info("#############stock##########"+stock);
		if(stock==null){
			stock = new OrdOrderUpdateStockDTO();
			stock.setOrderTimePriceService(timePriceService);
			stock.setUpdateStock(updateStock);
			stock.setOrderTimePriceService(timePriceService);
			stock.setTimePrice(timePrice);
			stock.setTimePriceId(timePrice.getTimePriceId());
			updateStockMap.put(key, stock);
		}else{
			stock.addUpdateStock(updateStock);
		}
	}
	
	public Map<Long, Long> getStockMap() {
		return stockMap;
	}

	public void setStockMap(Map<Long, Long> stockMap) {
		this.stockMap = stockMap;
	}

	public int getWaitPaymentTimeSec() {
		return waitPaymentTimeSec;
	}

	public void setWaitPaymentTimeSec(int waitPaymentTimeSec) {
		this.waitPaymentTimeSec = waitPaymentTimeSec;
	}

	@Deprecated
	public void addItemDateTimeTableForPromotion(OrdOrderItem orderItem, Date date, Long price, Long settlementPrice) {
		//存在促销ID
		if (orderItem != null && date != null && price != null && settlementPrice != null) {
			if (itemPriceTableMap == null) {
				itemPriceTableMap = new HashMap<OrdOrderItem, Map<Date, ItemPrice>>();
			}
			
			Map<Date, ItemPrice> itemPriceMap = itemPriceTableMap.get(orderItem);
			if (itemPriceMap == null) {
				itemPriceMap = new HashMap<Date, ItemPrice>();
				itemPriceTableMap.put(orderItem, itemPriceMap);
			}
			
			ItemPrice itemPrice = new ItemPrice(date);
			itemPrice.setPrice(price);
			itemPrice.setSettlementPrice(settlementPrice);
			itemPriceMap.put(date, itemPrice);
		}
	}

	public Map<OrdOrderItem, Map<Date, ItemPrice>> getItemPriceTableMap() {
		return itemPriceTableMap;
	}

	public void setItemPriceTableMap(
			Map<OrdOrderItem, Map<Date, ItemPrice>> itemPriceTableMap) {
		this.itemPriceTableMap = itemPriceTableMap;
	}

	public void putApiFlag(Long suppGoodsId,boolean f){
		if(!apiFlagMap.containsKey(suppGoodsId)){
			apiFlagMap.put(suppGoodsId, f);
		}
	}

	public Map<Long, Boolean> getApiFlagMap() {
		return apiFlagMap;
	}

	public Map<Long, OrdOrderUpdateStockDTO> getUpdateStockDTOMap() {
		return updateStockDTOMap;
	}

	public void setUpdateStockDTOMap(Map<Long, OrdOrderUpdateStockDTO> updateStockDTOMap) {
		this.updateStockDTOMap = updateStockDTOMap;
	}

	public OrdOrderItem getFilterMainOrderItem() {
		return filterMainOrderItem;
	}

	public void setFilterMainOrderItem(OrdOrderItem filterMainOrderItem) {
		this.filterMainOrderItem = filterMainOrderItem;
	}
	
	public void addOrderPack(OrdOrderPack pack){
		if(orderPackList==null){
			orderPackList = new ArrayList<OrdOrderPack>();
		}
		//统一记录订单子项位置，方便后面统一操作
		if(pack.getOrderItemList()!=null){
			if(orderItemList == null){
				orderItemList = new ArrayList<OrdOrderItem>();
			}
			orderItemList.addAll(pack.getOrderItemList());
		}
		orderPackList.add(pack);
	}
	
	public void addOrderItem(final OrdOrderItem item){
		if(orderItemList==null){
			orderItemList = new ArrayList<OrdOrderItem>();
		}
		if(nopackOrderItemList == null){
			nopackOrderItemList = new ArrayList<OrdOrderItem>();
		}
		nopackOrderItemList.add(item);
		orderItemList.add(item);
	}

	public Map<String, OrdOrderUpdateStockDTO> getUpdateStockMap() {
		return updateStockMap;
	}
	
	
	
	public int getAdult() {
		return adult;
	}

	public void setAdult(int adult) {
		this.adult = adult;
	}

	public void addOrdPromotions(String key,List<OrdPromotion> list){
		if(CollectionUtils.isNotEmpty(list)){
			promotionMap.put(key,list);
		}
	}
	
	public void addOrderAmountItem(final OrdOrderAmountItem item){
		if(orderAmountItemList==null){
			orderAmountItemList = new ArrayList<OrdOrderAmountItem>();
		}
		orderAmountItemList.add(item);
	}

	public OrdPerson getExpressAddress() {
		return expressAddress;
	}

	public void setExpressAddress(OrdPerson expressAddress) {
		this.expressAddress = expressAddress;
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		this.child = child;
	}
	
	/**
	 * 需要的人数
	 * @return
	 */
	public int getTotalPerson(){
		return adult+child;
	}

	public boolean isValidInitOrder() {
		return validInitOrder;
	}

	public void setValidInitOrder(boolean validInitOrder) {
		this.validInitOrder = validInitOrder;
	}
	
	public OrdOrderPack getOrderPackByProductId(final Long productId){
		if(orderPackList!=null){
			for(OrdOrderPack pack:orderPackList){
				if(pack.getProductId().equals(productId)){
					return pack;
				}
			}
		}
		return null;
	}
	
	public OrdOrderItem getOrderItemBySuppGoodsId(final Long suppGoodsId){
		for(OrdOrderItem orderItem:orderItemList){
			if(orderItem.getSuppGoodsId().equals(suppGoodsId)){
				return orderItem;
			}
		}
		return null;
	}
	
	public OrdOrderItem getOrderItemByProductId(final Long productId){ 
		logger.info("------productpromid="+productId);
		Long price = 0L;
		OrdOrderItem mainItem =null;
		for(OrdOrderItem orderItem:orderItemList){
			if(orderItem.getProductId().equals(productId)){
				logger.info("------PromTotalAmount()="+orderItem.getTotalAmount()+"dsfdsf"+orderItem.getPrice());
				if(orderItem.getPrice()!=null){
				if(price<orderItem.getPrice()){
					logger.info("------price()="+orderItem.getPrice());
					price =orderItem.getPrice();
					mainItem = orderItem; 
				}
			}
			}
		}
		return mainItem;
	}

	public Map<String, List<OrdPromotion>> getPromotionMap() {
		return promotionMap;
	}

	public boolean isCreateFlag() {
		return createFlag;
	}

	public void setCreateFlag(boolean createFlag) {
		this.createFlag = createFlag;
	}

	public void addOrdAdditionStatus(OrdAdditionStatus ordAdditionStatus) {
		if (ordAdditionStatus != null) {
			if (ordAdditionStatusList == null) {
				ordAdditionStatusList = new ArrayList<OrdAdditionStatus>();
			}
			String key = ordAdditionStatus.getStatusType()+"_"+ordAdditionStatus.getStatus();
			if(!ordAdditionStatusSet.contains(key)){
				ordAdditionStatusSet.add(key);
				ordAdditionStatusList.add(ordAdditionStatus);
			}
		}
	}
	
	

	public Map<String, Long> getBuyoutMap() {
		return buyoutMap;
	}

	public void setBuyoutMap(Map<String, Long> buyoutMap) {
		this.buyoutMap = buyoutMap;
	}



	private Set<String> ordAdditionStatusSet = new HashSet<String>();


	public Map<Long, List<OrdPerson>> getGoodsPersonRelationMap() {
		return goodsPersonRelationMap;
	}

	public void setGoodsPersonRelationMap(Map<Long, List<OrdPerson>> goodsPersonRelationMap) {
		this.goodsPersonRelationMap = goodsPersonRelationMap;
	}
	public Long getValidPromtionAmount() {
		return validPromtionAmount;
	}
	public void setValidPromtionAmount(Long validPromtionAmount) {
		this.validPromtionAmount = validPromtionAmount;
	}
}
