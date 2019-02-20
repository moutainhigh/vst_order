package com.lvmama.vst.order.timeprice.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.price.api.strategy.model.bizenum.ExchangeRateModeEnum;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.vo.OrdOrderItemExtendDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_SETTLEMENT_PRICE_REASON;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

public abstract class AbstractOrderTimePriceService extends AbstractBookService implements OrderTimePriceService {
	
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractOrderTimePriceService.class);
	
	/**
	 * 检验库存
	 * @return
	 */
	protected boolean checkTimePrice(SuppGoodsBaseTimePrice timePrice, Date date, Long stock) {
		boolean isSuccess = false;
		//时间价格表验证
		TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(date, stock);
		if (checkVO != null) {
			if (checkVO.isOrderAble()) {
				isSuccess = true;
			} else {
				LOG.info("AbstractOrderTimePriceService.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO.isOrderAble()=false, notAbleReason=" + checkVO.getNotAbleReason());
			}
		} else {
			LOG.info("AbstractOrderTimePriceService.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO=null");
		}
		
		return isSuccess;
	}
	/***
	 * 处理价格明细
	 * @param orderItem
	 */
	protected  void fillOrdMulPriceRateListByOrdOrderItem(OrdOrderItem orderItem){
		List<OrdMulPriceRate> listOrdMul =new ArrayList<OrdMulPriceRate>();
		if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) {
			addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name(),orderItem.getQuantity(),orderItem.getPrice(),OrdMulPriceRate.AmountType.PRICE.name());
			addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.MARKET_ADULT.name(),orderItem.getQuantity(),orderItem.getMarketPrice(),OrdMulPriceRate.AmountType.MARKET.name());
			addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name(),orderItem.getQuantity(),orderItem.getSettlementPrice(),OrdMulPriceRate.AmountType.SETTLEMENT.name());
			
		}else {
			
			if (!BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()) &&orderItem.getBuyoutQuantity()!=null && orderItem.getBuyoutQuantity()>0) {
				addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name(),orderItem.getBuyoutQuantity(),orderItem.getBuyoutPrice(),OrdMulPriceRate.AmountType.SETTLEMENT.name());
				if (orderItem.getQuantity()>orderItem.getBuyoutQuantity()) {
					addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.SETTLEMENT_PRICE.name(),orderItem.getQuantity()-orderItem.getBuyoutQuantity(),orderItem.getNotBuyoutSettleAmout()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()),OrdMulPriceRate.AmountType.SETTLEMENT.name());
				}

			}else {
				addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.SETTLEMENT_PRICE.name(),orderItem.getQuantity(),orderItem.getSettlementPrice(),OrdMulPriceRate.AmountType.SETTLEMENT.name());

			}
			
			addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.PRICE.name(),orderItem.getQuantity(),orderItem.getPrice(),OrdMulPriceRate.AmountType.PRICE.name());
			addMulPriceRate(listOrdMul,ORDER_PRICE_RATE_TYPE.MARKET_PRICE.name(),orderItem.getQuantity(),orderItem.getMarketPrice(),OrdMulPriceRate.AmountType.MARKET.name());
		}
		
		orderItem.setOrdMulPriceRateList(listOrdMul);
			
	}

	/**
	 * @param list   
	 * @param priceType   价格类型
	 * @param quantity    数量
	 * @param settlePrice 结算价
	 * @param amoutType   
	 */
	protected void addMulPriceRate(List<OrdMulPriceRate> list,String priceType ,Long quantity ,Long settlePrice ,String amoutType){
		OrdMulPriceRate price2 = new OrdMulPriceRate();
		//非买断结算价
		price2.setPriceType(priceType);
		price2.setQuantity((long)quantity);
		price2.setPrice(settlePrice);
		price2.setAmountType(amoutType);
		list.add(price2);
	}
	
    /**
     * 检验共享库存
     * @return
     */
    protected boolean checkSharedTimePrice(SuppGoodsBaseTimePrice timePrice, Date date, Long stock,Long sharedStock) {
        boolean isSuccess = false;
        //时间价格表验证
        TimePriceCheckVO checkVO = timePrice.checkTimePriceForShareOrder(date,stock,sharedStock);
        if (checkVO != null) {
            if (checkVO.isOrderAble()) {
                isSuccess = true;
            } else {
                LOG.info("AbstractOrderTimePriceService.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO.isOrderAble()=false, notAbleReason=" + checkVO.getNotAbleReason());
            }
        } else {
            LOG.info("AbstractOrderTimePriceService.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO=null");
        }

        return isSuccess;
    }
	
	/**
	 * 商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	protected void checkParam(final SuppGoods suppGoods, final BuyInfo.Item item, boolean ck){
		
		if(suppGoods==null){
//			throw new IllegalArgumentException("商品ID=" + item.getGoodsId() + "不存在");
			throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIT_SUPPGOODS.getErrorCode(),
                    "商品ID=" + item.getGoodsId() + "不存在");
		}
		if(ck){
			if (item.getQuantity() <= 0) {
				throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于等于零");
			}

            if((null != suppGoods.getMaxQuantity()) && (item.getQuantity() > suppGoods.getMaxQuantity())){
//              throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.OVER_MAX_BOOK_VALUE.getErrorCode(),
                                                    "商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
            }
            
            if((null != suppGoods.getMinQuantity()) && (item.getQuantity() <suppGoods.getMinQuantity())){
//                throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于最小值");
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LESS_MIN_BOOK_VALUE.getErrorCode(),
                        "商品 " + suppGoods.getGoodsName() + " 订购数量小于最小值");
            }
			
			if (item.getOwnerQuantity() > item.getQuantity()) {
				throw new IllegalArgumentException("商品" + suppGoods.getGoodsName() + "  实际订购数量小于零");
			}
		}
	}

	protected void setOrdOrderItemExtendDTOInfo(SuppGoodsBaseTimePrice baseTimePrice, SuppGoods suppGoods, OrdOrderItemDTO orderItem) {
		if (baseTimePrice == null || suppGoods == null || orderItem == null) {
			return ;
		}
		try {
			LOG.info(suppGoods.getSuppGoodsId() + "时间价格信息=" +  GsonUtils.toJson(baseTimePrice));

			OrdOrderItemExtendDTO scenicOrdOrderItemExtendDTO = new OrdOrderItemExtendDTO();

			//拷贝 外币结算价,外币市场价,外币销售价,汇率模式,自定义汇率
			EnhanceBeanUtils.copyProperties(baseTimePrice, scenicOrdOrderItemExtendDTO);
			scenicOrdOrderItemExtendDTO.setForeignSettlementPrice(baseTimePrice.getForeignCurrencySettlementPrice());
			scenicOrdOrderItemExtendDTO.setForeignMarketPrice(baseTimePrice.getForeignCurrencyMarkerPrice());
			scenicOrdOrderItemExtendDTO.setForeignPrice(baseTimePrice.getForeignCurrencyPrice());
			scenicOrdOrderItemExtendDTO.setForeignActualSettlementPrice(baseTimePrice.getForeignActualSettlementPrice());
			scenicOrdOrderItemExtendDTO.setSettlementPriceRate(baseTimePrice.getSettlementExchangeRate());//结算汇率
			scenicOrdOrderItemExtendDTO.setPriceRate(baseTimePrice.getSaleExchangeRate());//销售汇率
			//币种 和 币种名称
			if(StringUtils.isEmpty(suppGoods.getCurrencyType())){//如果商品基本信息未返回币种信息，默认人民币
				LOG.info("设置子单附加信息商品基本信息未返回币种信息，默认人民币");
				scenicOrdOrderItemExtendDTO.setCurrencyCode(SuppGoods.CURRENCYTYPE.CNY.getCode());
				scenicOrdOrderItemExtendDTO.setCurrencyName(com.lvmama.vst.back.goods.po.SuppGoods.CURRENCYTYPEFORFOREIGNSETTLEMENT.getCnName(SuppGoods.CURRENCYTYPE.CNY.getCode()));
				scenicOrdOrderItemExtendDTO.setSettlementPriceRate(new BigDecimal(1));//结算汇率
				scenicOrdOrderItemExtendDTO.setPriceRate(new BigDecimal(1));//销售汇率
			}else{
				scenicOrdOrderItemExtendDTO.setCurrencyCode(suppGoods.getCurrencyType());
				scenicOrdOrderItemExtendDTO.setCurrencyName(com.lvmama.vst.back.goods.po.SuppGoods.CURRENCYTYPEFORFOREIGNSETTLEMENT.getCnName(suppGoods.getCurrencyType()));
			}
			//对于时间价格返回结果没有ForeignActualSettlementPrice（外币实际结算单价）的，默认取ForeignCurrencySettlementPrice
			if (scenicOrdOrderItemExtendDTO.getForeignActualSettlementPrice() == null) {
				scenicOrdOrderItemExtendDTO.setForeignActualSettlementPrice(baseTimePrice.getForeignCurrencySettlementPrice());
			}
			LOG.info("设置子单附加信息第一步:" + GsonUtils.toJson(scenicOrdOrderItemExtendDTO));

			//外币结算总价,  数量 * 外币结算单价
			Long settlementPrice = baseTimePrice.getForeignCurrencySettlementPrice();
			scenicOrdOrderItemExtendDTO.setForeignTotalSettlementPrice(orderItem.getQuantity() * (settlementPrice == null ? 0 : settlementPrice));
			scenicOrdOrderItemExtendDTO.setForeignActTotalSettlePrice(scenicOrdOrderItemExtendDTO.getForeignTotalSettlementPrice());//外币实际结算总价默认等于外币结算总价
			LOG.info("设置子单附加信息第二步:" + GsonUtils.toJson(scenicOrdOrderItemExtendDTO));
			//设置子单的附属信息对象
			orderItem.setOrdOrderItemExtendDTO(scenicOrdOrderItemExtendDTO);
		} catch (Exception e) {
			LOG.error("商品ID=" + suppGoods.getSuppGoodsId() + "设置子单附加信息发生异常:" + e, e);
		}
	}
	
	
	protected SuppGoodsBaseTimePrice getTimePriceAndCheck(SuppGoods suppGoods,
			BuyInfo.Item item, Date visitTime) {
		ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = getTimePrice(suppGoods.getSuppGoodsId(), visitTime, true);
		
		if (timePriceHandle == null||timePriceHandle.hasNull()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("getTimePriceAndCheck(SuppGoods, BuyInfo.Item, Date) - timePriceHandle == null||timePriceHandle.hasNull(),suppGoodsId={},visitTime={}",new Object[]{suppGoods.getSuppGoodsId(),visitTime}); //$NON-NLS-1$
			}

			throwNullException(suppGoods.getSuppGoodsId()+"商品无时间价格");
		}
		SuppGoodsBaseTimePrice timePrice = timePriceHandle.getReturnContent();
		checkOnsaleFlag(timePrice);
		checkParam(suppGoods, item, true);

		return timePrice;
	}
	
	protected SuppGoodsBaseTimePrice getTimePriceAndCheck(SuppGoods suppGoods,Long distributionId,
			BuyInfo.Item item, Date visitTime) {
//		if()
		return getTimePriceAndCheck(suppGoods, item, visitTime);
	}
	
	protected void checkOnsaleFlag(SuppGoodsBaseTimePrice timePrice){
		if (!"Y".equalsIgnoreCase(timePrice.getOnsaleFlag())) {
			throwIllegalException("商品游玩日期不可售");
		}
	}
	

	protected OrdOrderStock createStock(Date visitTime,long quantity){
		OrdOrderStock stock = new OrdOrderStock();
		stock.setQuantity(quantity);
//		stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setVisitTime(visitTime);
//		stock.setNeedResourceConfirm("true");
//		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
		return stock;
	}
	
	/**
	 * 不需要资源确认
	 * @param stock
	 */
	protected void makeNotNeedResourceConfirm(final OrdOrderStock stock){
		stock.setNeedResourceConfirm("false");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
	}

	/**
	 * 需要资源审核的库存项
	 * @param stock
	 */
	protected void makeNeedResourceConfirm(final OrdOrderStock stock){
		stock.setNeedResourceConfirm("true");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
	}
	
	protected void makeInventoryFlag(final OrdOrderStock stock){
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
	}
	
	protected void fillPackageOrderItemPrice(final OrdOrderItem orderItem,final ProdPackageDetail detail){
		orderItem.setPrice(getPackagePrice(detail, orderItem.getPrice(), orderItem.getSettlementPrice()));
	}
	
	protected void fillPackageOrderItemPrice(final OrdOrderItem orderItem,final ProdPackageDetailAddPrice detailAddPrice){
		orderItem.setPrice(getPackagePrice(detailAddPrice, orderItem.getPrice(), orderItem.getSettlementPrice()));
	}
	
	protected void fillPackageOrderItemPrice(final OrdMulPriceRate rate,final ProdPackageDetail detail,long settlementPrice){
		rate.setPrice(getPackagePrice(detail, rate.getPrice(), settlementPrice));
	}
	
	protected void fillPackageOrderItemPrice(final OrdMulPriceRate rate,final ProdPackageDetailAddPrice detailAddPrice,long settlementPrice){
		rate.setPrice(getPackagePrice(detailAddPrice, rate.getPrice(), settlementPrice));
	}
	
	protected void fillPackageOrderItemPrice(final TimePrice timePrice,final ProdPackageDetail detail){
		timePrice.setPrice(getPackagePrice(detail, timePrice.getPrice(), timePrice.getSettlementPrice()));
	}
	
	protected void fillPackageOrderItemPrice(final TimePrice timePrice,final ProdPackageDetailAddPrice detailAddPrice){
		timePrice.setPrice(getPackagePrice(detailAddPrice, timePrice.getPrice(), timePrice.getSettlementPrice()));
	}
	
	private long getPackagePrice(final ProdPackageDetail detail,final long sourcePrice,long settlementPrice){
		long resultPrice=0;
		if("FIXED_PRICE".equalsIgnoreCase(detail.getPriceType())){
			resultPrice = (settlementPrice+detail.getPrice().longValue());
		}else if("MAKEUP_PRICE".equalsIgnoreCase(detail.getPriceType())){
			resultPrice = ((long)(settlementPrice+((sourcePrice-settlementPrice)*detail.getPrice()/10000)));
		}else if("FIXED_PERCENT".equalsIgnoreCase(detail.getPriceType())){
			double adPri=   new BigDecimal(detail.getPrice()).divide(new BigDecimal(10000),3,BigDecimal.ROUND_HALF_UP).doubleValue();
			//结算价恒定，按比例加价   商品销售价=商品结算价*（1+N%）
			double nprice=settlementPrice * (1+adPri);
		    DecimalFormat format = new DecimalFormat("#");
		    String sMoney = format.format(nprice);
		    resultPrice= Long.parseLong(sMoney+"");	
			
	    }else{
//			throwIllegalException("打包价格类型数据为空");
			resultPrice = sourcePrice;
		}
		return resultPrice;
	}
	
	private long getPackagePrice(final ProdPackageDetailAddPrice detailAddPrice,final long sourcePrice,long settlementPrice){
		long resultPrice=0;
		if("FIXED_PRICE".equalsIgnoreCase(detailAddPrice.getPriceType())){
			resultPrice = (settlementPrice+detailAddPrice.getPrice().longValue());
		}else if("MAKEUP_PRICE".equalsIgnoreCase(detailAddPrice.getPriceType())){
			resultPrice = ((long)(settlementPrice+((sourcePrice-settlementPrice)*detailAddPrice.getPrice()/10000)));
		}else if("FIXED_PERCENT".equalsIgnoreCase(detailAddPrice.getPriceType())){
			double adPri=   new BigDecimal(detailAddPrice.getPrice()).divide(new BigDecimal(10000),3,BigDecimal.ROUND_HALF_UP).doubleValue();
			//结算价恒定，按比例加价   商品销售价=商品结算价*（1+N%）
			double nprice=settlementPrice * (1+adPri);
		    DecimalFormat format = new DecimalFormat("#");
		    String sMoney = format.format(nprice);
		    resultPrice= Long.parseLong(sMoney+"");	
			
	    }else{
//			throwIllegalException("打包价格类型数据为空");
			resultPrice = sourcePrice;
		}
		return resultPrice;
	}
	
	protected void makeOrderItemTime(OrdOrderItem item,
			SuppGoodsBaseTimePrice timePrice) {
		if(item.getVisitTime()==null||timePrice.getSpecDate().before(item.getVisitTime())){
			item.setVisitTime(timePrice.getSpecDate());
		}
		if(timePrice.getAheadBookTime()!=null){
			Date aheadTime = DateUtils.addMinutes(timePrice.getSpecDate(),-timePrice.getAheadBookTime().intValue());
			if(item.getAheadTime() == null||aheadTime.before(item.getAheadTime())){
				item.setAheadTime(aheadTime);
			}
		}
		if(timePrice.getLatestCancelTime()!=null){
			Date lastCancelTime = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getLatestCancelTime().intValue());
			if(item.getLastCancelTime()==null||lastCancelTime.before(item.getLastCancelTime())){
				item.setLastCancelTime(lastCancelTime);
			}
		}
	}
	
	

	
	/**
	 * 设置订单那子项是否需要资源确认
	 * 
	 * @param needResourceConfirm
	 * @param orderItem
	 */
	protected void setOrderItemsNeedResourceConfirm(String needResourceConfirm, OrdOrderItem orderItem) {
		if ("true".equals(orderItem.getNeedResourceConfirm())) {
			orderItem.setNeedResourceConfirm(needResourceConfirm);
		}
	}
	
	protected void makeNeedResourceConfirm(OrdOrderItem orderItem,
			List<OrdOrderStock> stockList) {
		for(OrdOrderStock stock:stockList){
			setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(), orderItem);
		}
	}
	
	/**
	 * 从销售价格类型转换成结算价格类型
	 * @param priceType
	 * @return
	 */
	protected String converSettlement(String priceType){
		String suffix = priceType.substring(priceType.indexOf("_"));
		return "SETTLEMENT"+suffix;
	}
	
	/**
	 * 结算价修改记录
	 * @param oldTalSettlementPrice
	 * @param orderItem
	 */
	public void  initOrdSettlementPriceRecord(String priceType,long oldActualSettlementPrice, long oldTalSettlementPrice,OrdOrderItem orderItem){
		OrdSettlementPriceRecord ordSettltRecord = new OrdSettlementPriceRecord();
		ordSettltRecord.setPriceType(priceType);
		ordSettltRecord.setOldActualSettlementPrice(oldActualSettlementPrice);
		ordSettltRecord.setOldTotalSettlementPrice(oldTalSettlementPrice);
		ordSettltRecord.setNewTotalSettlementPrice(orderItem.getTotalSettlementPrice());
		ordSettltRecord.setChangeType(ORD_SETTLEMENT_PRICE_CHANGE_TYPE.UNIT_PRICE.name());
		ordSettltRecord.setChangeResult(ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.name());
		ordSettltRecord.setReason(ORDER_SETTLEMENT_PRICE_REASON.SUPPLIER_DISCOUNT.name());
		ordSettltRecord.setOperator("system");
		ordSettltRecord.setCreateTime(new Date());
		ordSettltRecord.setRemark("结算价促销");
		ordSettltRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
		ordSettltRecord.setVisitTime(orderItem.getVisitTime());
		if(orderItem.getOrdSettlementPriceRecordList()==null){
			List<OrdSettlementPriceRecord> list = new ArrayList<OrdSettlementPriceRecord>();
			list.add(ordSettltRecord);
			orderItem.setOrdSettlementPriceRecordList(list);
		}else{
			orderItem.getOrdSettlementPriceRecordList().add(ordSettltRecord);
		}
	}
	
	/**
	 * 目的地BU保险库存校验接口
	 * @param suppGoods
	 * @param item
	 * @param distributionId
	 * @param dataMap
	 * @return
	 */
	public ResultHandleT<Object> destBucheckStock(SuppGoods suppGoods, com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item item, Long distributionId, Map<String, Object> dataMap){
		return null;
	}
	
	/**
	 * 目的地BU保险商品校验
	 * @param suppGoods
	 * @param item
	 * @return
	 */
	public ResultHandle validate(SuppGoods suppGoods, DestBuBuyInfo.Item item, OrdOrderItemDTO orderItem, OrdOrderDTO order){
		return null;
	}

}
