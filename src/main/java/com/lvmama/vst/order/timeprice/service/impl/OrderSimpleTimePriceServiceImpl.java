package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.visa.api.base.VisaResultHandleT;
import com.lvmama.visa.api.service.VisaGoodsService;
import com.lvmama.visa.api.vo.price.VisaSuppGoodsSimpleTimePrice;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsSimpleTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * 
 * @author sunjian
 *
 */
@Service("orderSimpleTimePriceService")
public class OrderSimpleTimePriceServiceImpl extends AbstractOrderTimePriceService implements OrderTimePriceService {
	
	private static final Log LOG = LogFactory.getLog(OrderSimpleTimePriceServiceImpl.class);
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Resource(name="goodsSimpleTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
	@Autowired
	private VisaGoodsService visaGoodsService;
	
	@Override
	public void updateStock(Long timePriceId, Long stock, Map<String, Object> dataMap) {
		//TODO-签证库存为FREESALE，无须更新库存
		//goodsTimePriceStockService.updateStock(timePriceId, stock);
	}

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item, OrdOrderItemDTO orderItem, OrdOrderDTO ordOrderDTO) {
		ResultHandle resultHandle = new ResultHandle();
		String errorMsg = null;
		
		if ((orderItem != null) && (item != null) && (ordOrderDTO != null)) {
			SuppGoodsSimpleTimePrice suppGoodsSimpleTimePrice = null;
			Date date = orderItem.getVisitTime();
			//若订单子项的商品为签证时，签证价格取当前时间的价格
			if(BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(orderItem.getCategoryId())){
				VisaResultHandleT<VisaSuppGoodsSimpleTimePrice> visaTimePriceHolder = visaGoodsService.findSuppGoodsSimpleTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
				if(visaTimePriceHolder!=null && visaTimePriceHolder.getReturnContent()!=null){
					suppGoodsSimpleTimePrice = new SuppGoodsSimpleTimePrice();
					BeanUtils.copyProperties(visaTimePriceHolder.getReturnContent(), suppGoodsSimpleTimePrice);
				}
			}else{
				ResultHandleT<SuppGoodsSimpleTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSimpleTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
				if(timePriceHolder!=null && timePriceHolder.isSuccess() && timePriceHolder.getReturnContent()!=null)suppGoodsSimpleTimePrice = timePriceHolder.getReturnContent();
			}
			
			if (suppGoodsSimpleTimePrice != null) {
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				errorMsg = checkTimePriceTable(suppGoodsSimpleTimePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name(), date, orderStockList);
				if (errorMsg == null) {
					//使用时间价格表填充订单子项
					errorMsg = accumulateOrderItemDataWithTimePrice(suppGoodsSimpleTimePrice, orderItem);
					
					if (errorMsg == null) {
						//使用时间价格表填充订单
						OrderUtils.fillOrderWithTimePrice(suppGoodsSimpleTimePrice, ordOrderDTO);

						orderItem.setOrderStockList(orderStockList);
						
						//设置订单子项资源状态
						OrderUtils.setOrderItemResourceStatusByOrderStockList(orderItem, orderStockList);
						
						orderItem.setCancelStrategy(suppGoodsSimpleTimePrice.getCancelStrategy());
					}
				}
			} else {
				errorMsg = "您购买的商品中存在下架商品。";
			}
			fillOrdMulPriceRateListByOrdOrderItem(orderItem);
		} else {
			errorMsg = "您的订单不存在。";
		}
			
		if (errorMsg != null) {
			resultHandle.setMsg(errorMsg);
		}

		return resultHandle;
	}
	
	/**
	 * 验证价格时间表中的库存
	 * 
	 * @param timePrice
	 * @param orderItem
	 * @param ordOrderDTO
	 * @param stockObjectType
	 * @param visitTime
	 * @param orderStockList
	 * @return
	 */
	private String checkTimePriceTable(SuppGoodsSimpleTimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
		String errorMsg = null;
		//订单本地库存记录
		OrdOrderStock ordOrderStock = null;
		//是否需要资源确认
		String needResourceConfirm = null;
		//资源状态
		String resourceStatus = null;
		//下单类型
		String inventory = null;
		
		long stock = orderItem.getQuantity();
		Integer ownerQuantity = (Integer) orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.ownerQuantity.name());
		
		if (ownerQuantity != null) {
			stock = stock - ownerQuantity.intValue();
		}
		
		TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(ordOrderDTO.getCreateTime(), stock);
		if (checkVO != null) {
			LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.isOrderAble=" + checkVO.isOrderAble());
			if (checkVO.isOrderAble()) {
				if (checkVO.getStockReduceList() != null && checkVO.getStockReduceList().size() > 0) {
					for (StockReduceVO stockReduce : checkVO.getStockReduceList()) {
						if (stockReduce != null) {
							LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:stockReduce[isResourceConfirm=" + stockReduce.isResourceConfirm() + 
									",isReduceStock=" + stockReduce.isReduceStock() + ", ReduceType=" + stockReduce.getReduceType() + "]");
							
							//需要资源确认
							if (stockReduce.isResourceConfirm()) {
								//需要资源确认
								needResourceConfirm = "true";
								//需要资源审核
								resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
							//不需要资源确认
							} else {
								//不需要资源确认
								needResourceConfirm = "false";
								//资源审核通过
								resourceStatus = OrderEnum.RESOURCE_STATUS.AMPLE.name();
							}
							
							//是否要减库存
							if (stockReduce.isReduceStock()) {
								//有库存下单
								inventory = OrderEnum.INVENTORY_STATUS.INVENTORY.name();
								//将减少的库存添加到缓存映射表中，方便后续更新库存
								ordOrderDTO.addUpdateStock(timePrice, stockReduce.getStock(), this);
							} else {
								//无库存下单
								inventory = OrderEnum.INVENTORY_STATUS.UNINVENTORY.name();
							}
							//FREESALE的强制为FREESALE下单
							if (stockReduce.getReduceType() == SuppGoodsTimePrice.REDUCETYPE.FREESALE) {
								inventory = OrderEnum.INVENTORY_STATUS.FREESALE.name();
							}
							
							//添加一条订单库存记录
							ordOrderStock = OrderUtils.makeOrdOrderStockRecord(stockObjectType, visitTime, stockReduce.getStock(), inventory, needResourceConfirm, resourceStatus);
							orderStockList.add(ordOrderStock);
						} else {
							errorMsg = "库存未知，无法下单。";
							LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:stockReduce=null,msg=" + errorMsg);
						}
					}
				} else {
					errorMsg = "库存异常，无法下单。";
					if (checkVO.getStockReduceList() == null) {
						LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.getStockReduceList()=null,msg=" + errorMsg);
					} else {
						LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.getStockReduceList().size()=" + checkVO.getStockReduceList().size() + ",msg=" + errorMsg);
					}
				}
			} else {
				errorMsg = checkVO.getNotAbleReason();
				LOG.info("OrderValidCheckBussiness.checkTimePriceTable: checkVO.isOrderAble()=false,msg=" + errorMsg);
			}
		} else {
			errorMsg = "库存检验失败，无法下单。";
			LOG.debug("OrderValidCheckBussiness.checkTimePriceTable: checkVO=null,msg=" + errorMsg);
		}
		
		return errorMsg;
	}
	
	/**
	 * 订单子项各个逻辑项累加
	 * 
	 * @param timePrice
	 * @param orderItem
	 */
	private String  accumulateOrderItemDataWithTimePrice(SuppGoodsSimpleTimePrice timePrice, OrdOrderItem orderItem) {
		String errorMsg = null;
		
		if ((orderItem != null) && (timePrice != null)) {
			/*List<OrdItemPersonRelation> ordItemPersonRelationList = orderItem.getOrdItemPersonRelationList();
			if (ordItemPersonRelationList == null || ordItemPersonRelationList.isEmpty()) {
				errorMsg = "商品" + orderItem.getSuppGoodsName() + "没有入住（游玩）人。";
				return errorMsg;
			}*/
			
			OrderUtils.accumulateOrderItemPrice(orderItem, 
					timePrice.getPrice(), 
					timePrice.getSettlementPrice(), 
					timePrice.getSettlementPrice(), 
					0L);

			
			// 最晚取消时间
			makeOrderItemTime(orderItem,timePrice);
		}
		
		return errorMsg;
	}
	
	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
		ResultHandleT<SuppGoodsBaseTimePrice> resultHandleSuppGoodsBaseTimePrice = new ResultHandleT<SuppGoodsBaseTimePrice>();
		VisaResultHandleT<VisaSuppGoodsSimpleTimePrice> suppGoodsSimpleTimePriceHandleT = visaGoodsService.findSuppGoodsSimpleTimePriceforVisa(null, goodsId, specDate);
		//SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		//酒店走另外一个，此处只有签证
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = null;
		if(suppGoodsSimpleTimePriceHandleT.getReturnContent()!=null){
			suppGoodsBaseTimePrice = new SuppGoodsSimpleTimePrice();
			BeanUtils.copyProperties(suppGoodsSimpleTimePriceHandleT.getReturnContent(), suppGoodsBaseTimePrice);
		}
		if (suppGoodsBaseTimePrice != null) {
			resultHandleSuppGoodsBaseTimePrice.setReturnContent(suppGoodsBaseTimePrice);
		} else {
			resultHandleSuppGoodsBaseTimePrice.setMsg("商品ID=" + goodsId + ",date=" + specDate + ",checkAhead" + checkAhead + ",时间价格表SuppGoodsSimpleTimePrice不存在。");
		}
		
		return resultHandleSuppGoodsBaseTimePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap) {
		//TODO-签证库存为FREESALE，无须更新库存
	}

	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item,Long distributionId, Map<String, Object> dataMap) {

		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
		
		try {
			//商品是否可售
			if (suppGoods != null && suppGoods.isValid()) {
				
				//商品参数验证
				checkParam(suppGoods, item, true);
				
				Date currDate = new Date();
				Date visitDate = item.getVisitTimeDate();
				
				ResultHandleT<SuppGoodsSimpleTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSimpleTimePrice(distributionId, suppGoods.getSuppGoodsId(), visitDate);
				LOG.info("正在进行库存检查，该商品的价格类型为SimpleTimePrice,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));

				if (timePriceHolder.isSuccess() && timePriceHolder.getReturnContent() != null) {
					SuppGoodsSimpleTimePrice timePrice = timePriceHolder.getReturnContent();
					if (this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
						
					} else {
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
						resultHandleT.setMsg("商品 " + suppGoods.getGoodsName() + "(ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
					}
				} else {
					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + currDate + "时间价格表不存在。");
					resultHandleT.setMsg("商品 " + suppGoods.getGoodsName() + "(ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
				}
			}
		} catch (Exception e) {
			resultHandleT.setMsg(e.getMessage());
		}
		
		return resultHandleT;
	}

	@Override
	public String getTimePricePrefix() {
		return "CuriseSimpleTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		throwIllegalException("暂时不支持供应商结算价促销");
	}
}
