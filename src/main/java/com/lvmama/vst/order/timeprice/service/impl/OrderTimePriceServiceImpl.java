package com.lvmama.vst.order.timeprice.service.impl;


import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsTimePriceQVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelCurrencyInfoVstVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceHotelClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderSharedStock;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.DestBuOrderPropUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.AreoHotelTimeRateVo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderSharedStockDao;
import com.lvmama.vst.order.service.IHotelTradeApiService;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("orderTimePriceService")
public class OrderTimePriceServiceImpl extends AbstractOrderTimePriceService implements OrderTimePriceService{
	
	private static final Log LOG = LogFactory.getLog(OrderTimePriceServiceImpl.class);
	/**
	 * 分销商商品时间价格表查询的适配器
	 * */
	@Resource
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	
	@Resource(name="goodsOraTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
	@Autowired
	private  OrdOrderGroupStockDao ordOrderGroupStockDao;
	@Autowired
	private ResPreControlService resControlBudgetRemote;
    @Autowired
    private OrdOrderSharedStockDao ordOrderSharedStockDao;
    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

    @Resource(name="goodsOraTimePriceStockService")
    protected IGoodsTimePriceStockService iGoodsTimePriceStockService;

	@Autowired
	private OrderLvfTimePriceServiceImpl orderLvfTimePriceServiceImpl;
	
	@Autowired
	private IHotelTradeApiService hotelTradeApiService;


	/**
	 * 目的地酒店时间价格表接口代理
	 * */
	@Autowired
	private DistGoodsTimePriceHotelClientService distGoodsTimePriceHotelClientService;


	/**
     * 目的地对接酒店接口
     * */
    @Resource
    private IHotelGoodsTimePriceQVstApiService hotelGoodsTimePriceQVstApiRemote;

	@Override
	public void updateStock(Long timePriceId, Long stock, Map<String, Object> dataMap) {
		if (timePriceId != null && stock != null) {
			Long orderItemId=null;
			if (MapUtils.isNotEmpty(dataMap)) {
				orderItemId=(Long)dataMap.get("orderItemId");
			}
			List<OrdOrderGroupStock> ordOrderGroupStockList=new ArrayList<OrdOrderGroupStock>(5);
			LOG.info("updateStock=====>timePriceId="+timePriceId+"   stock="+stock+"    orderItemId");
			if (!goodsTimePriceStockService.updateGroupStock(timePriceId, -stock, orderItemId,ordOrderGroupStockList)) {
				throw new RuntimeException("更新TimePrice库存失败。timePriceId=" + timePriceId);
			}else{
				//如果扣减库存成功，如果是的话需要将记录插入ORD_ORDER_SHARED_STOCK表中（update by luoweiyi）
				LOG.info("dataMap="+dataMap);
				if(null != dataMap && null != dataMap.get("orderItem")){
					OrdOrderItem orderItem = (OrdOrderItem)dataMap.get("orderItem");
					LOG.info("updateStock=====> orderItem="+orderItem);
					Long groupId = orderItem.getSuppGoods().getGroupId();
					LOG.info("updateStock=====> groupId=" + groupId + "    SuppGoodsId" + orderItem.getSuppGoods().getSuppGoodsId());
					if (null != orderItem.getOrderStockList()) {
						List<OrdOrderStock> ordOrderStockList = orderItem.getOrderStockList();
						for (OrdOrderStock oos : ordOrderStockList) {
							Date visitTime = oos.getVisitTime();

							SuppGoodsTimePrice timePrice = (SuppGoodsTimePrice)getTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), visitTime, true).getReturnContent();
							if(null == timePrice)
								continue;
							if(timePrice.getTimePriceId().longValue() != timePriceId.longValue())
								continue;
							if(null == timePrice.getLatestHoldTime())
								timePrice.setLatestHoldTime(0L);
							//共享库存
							Long shareStock = null;
							if (null != groupId) {
								LOG.info("updateStock=====> 查询共享库存：groupId=" + groupId + "    visitTime=" + visitTime);
								shareStock = iGoodsTimePriceStockService.getShareStock(groupId, visitTime);
								LOG.info("updateStock=====> oneDate=" + visitTime + "    groupId=" + groupId + "     shareStock=" + shareStock);
							}
							if(shareStock != null){
								//设置非保留房当日扣共享库存
								DestBuOrderPropUtil.setCurrReduceShareStock(timePrice);
							}
							if(!timePrice.isBeforeLastHoldTime(new Date()))
								continue;

							OrdOrderSharedStock ooss = new OrdOrderSharedStock();
							ooss.setVisitTime(visitTime);
							ooss.setQuantity(oos.getQuantity());
							ooss.setOrderItemId(orderItem.getOrderItemId());
							ooss.setInventory(oos.getInventory());
							ooss.setResourceStatus(oos.getResourceStatus());
							ooss.setNeedResourceConfirm(oos.getNeedResourceConfirm());

							if (null == shareStock) {
								ooss.setGroupId(null);
							} else {
								ooss.setGroupId(groupId);
							}
							ordOrderSharedStockDao.insert(ooss);
						}
					}

//                    if(null != orderItem.getSharedStockList()) {
//                        LOG.info("SharedStockList="+orderItem.getSharedStockList()+"  SharedStockListSize="+orderItem.getSharedStockList().size());
//
//                        for (OrdOrderSharedStock item : orderItem.getSharedStockList()) {
//                        	if(!item.isSaveFlag()){
//                        		  item.setOrderItemId(orderItemId);
//                                  item.setResourceStatus(orderItem.getResourceStatus());
//                                  item.setNeedResourceConfirm(orderItem.getNeedResourceConfirm());
//                                  ordOrderSharedStockDao.insert(item);
//                                  item.setSaveFlag(true);
//                        	}
//
//
//                        }
//                    }
				}
			}

//			if (dataMap != null) {
//				Boolean isUpdateSuperStock = (Boolean)dataMap.get("isUpdateSuperStock");
//				if (isUpdateSuperStock != null && isUpdateSuperStock.booleanValue()) {
//					Long suppGoodsId = (Long)dataMap.get("suppGoodsId");
//					Date beginDate = (Date)dataMap.get("beginDate");
//					Date endDate = (Date)dataMap.get("endDate");
//					if(LOG.isDebugEnabled()){
//						LOG.debug("orderItemId:"+orderItemId);
//					}
//					if (suppGoodsId != null && beginDate != null && endDate != null&&orderItemId!=null) {
//						if(CollectionUtils.isNotEmpty(ordOrderGroupStockList)){
//							for (OrdOrderGroupStock ordOrderGroupStock : ordOrderGroupStockList) {
//								petProdGoodsAdapter.updateStockByOrder(ordOrderGroupStock.getSuppGoodsId(), -ordOrderGroupStock.getQuantity(), ordOrderGroupStock.getVisitTime(), ordOrderGroupStock.getVisitTime());
//							}
//						}else{
//							petProdGoodsAdapter.updateStockByOrder(suppGoodsId, -stock, beginDate, endDate);
//						}
//					} else {
//						throw new RuntimeException("更新super库存失败。");
//					}
//				}
//			}
		}
	}

	/**
	 * 交通+X （酒店子单）设置价格为buyinfo入参时为准
	 * @param orderItem
	 * @param item
	 */
	private void accumulateOrderItemPriceWithItem(OrdOrderItem orderItem,BuyInfo.Item item){
		if ((orderItem != null) && (item!=null)) {
			// 单价
			orderItem.setPrice(item.getTotalAmount());

			// 结算单价
			long settlementPrice=0;
			for (AreoHotelTimeRateVo areoHotelTimeRateVo : item.getAreoHotelTimeRate()) {
				if(areoHotelTimeRateVo.getSettlementPrice()!=null){
					settlementPrice=settlementPrice+ areoHotelTimeRateVo.getSettlementPrice();
				}
			}
			orderItem.setSettlementPrice(settlementPrice);

			// 实际结算单价
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice()*item.getQuantity());

			orderItem.setTotalAmount(item.getTotalAmount()*item.getQuantity());

			// 市场单价（先默认值0）
			orderItem.setMarketPrice(0L);

		}
	}
	
	private void accumulateOrderItemDataWithTimePrice(TimePrice timePrice, OrdOrderItem orderItem) {
		if ((orderItem != null) && (timePrice != null)) {
			// 单价
			if (orderItem.getPrice() == null) {
				orderItem.setPrice(timePrice.getPrice());
			} else {
				orderItem.setPrice(orderItem.getPrice() + timePrice.getPrice());
			}

			// 结算单价
			if (orderItem.getSettlementPrice() == null) {
				orderItem.setSettlementPrice(timePrice.getSettlementPrice());
			} else {
				orderItem.setSettlementPrice(orderItem.getSettlementPrice() + timePrice.getSettlementPrice());
			}

			// 实际结算单价
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());

			// 市场单价（先默认值0）
			orderItem.setMarketPrice(0L);
			
			// 最晚取消时间
			makeOrderItemTime(orderItem,timePrice);
		}
	}
	
	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item, OrdOrderItemDTO orderItem, OrdOrderDTO ordOrderDTO) {
		ResultHandle resultHandle = new ResultHandle();
		String errorMsg = null;
		OrdOrderDTO order =  ordOrderDTO;
		if ((orderItem != null) && (item != null) && (item.getHotelAdditation() != null) && (ordOrderDTO != null)) {
			TimePrice timePrice = null;
			TimePrice stockFlagTimePrice = null;
			TimePrice deductTimePrice = null;
			ResultHandleT<TimePrice> timePriceHolder = null;
			ResultHandleT<TimePrice> guaranteeTimePriceHolder = null;
			
			List<TimePrice> everydayTimePriceList = new ArrayList<TimePrice>();
			List<TimePrice> allTimeGuaranteeTimePriceList = new ArrayList<TimePrice>();

			//酒店类型的商品每天使用的状况表
			List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList = new ArrayList<OrdOrderHotelTimeRate>();

			//酒店额外信息
			HotelAdditation hotleAdditation = item.getHotelAdditation();
			
			//前台保证入店日期和离店日期都是日期类型，即时间为00:00:00
			Date startDate = item.getVisitTimeDate();
			Date endDate = hotleAdditation.getLeaveTimeDate();

			/*//获取入住时间日期列表
			List<Date> dateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);*/
			
			//获取入住时间日期列表
			List<Date> dateList = null;
			if (startDate == null || endDate==null || (endDate.getTime() - startDate.getTime())/(24*60*60*1000) > 30 ) {
				errorMsg = "入驻时间日期超长";	
				LOG.error("入驻时间日期超长, orderNo:" + orderItem.getOrderId() + ", startDate:" + startDate + ", endDate:" + endDate);
			} else {
				dateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);
			}
			
			if (dateList != null && dateList.size()>0) {
				//退改策略,默认为"可退改"
				String cancelStrategyTmp = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
				List<OrdOrderStock> allList = new ArrayList<OrdOrderStock>();
				List<ResPreControlTimePriceVO> resPriceList =null;
				Long precontrolSettlePrice = null;
				Long precontrolSalePrice = null;
				Long precontrolMarketPrice = null;
				long buyoutTotalPrice = 0;
				long notBuyoutTotalPrice = 0;
				Long leftMoney = null;
				long buyoutNum = 0;
				for (int i = 0; i < dateList.size(); i++) {
					long notMDnum = 0;
					Date date = dateList.get(i);
					//取得入住 每天的时间价格表
					timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
					
					/** 开始资源预控买断价格  **/
					SuppGoods goods = orderItem.getSuppGoods();
					Long goodsId = goods.getSuppGoodsId();
					Date visitDate = date;
					boolean hasControled=false;
					GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO=new GoodsResPrecontrolPolicyVO();
					//如果是预售券的兑换订单就不走买断
					if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
					    //通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
						goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
						//如果能找到该有效预控的资源
						hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
						LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
					
					}
			
					
					/*Map<String, Long> map = order.getBuyoutMap();
					String k = "";
					Long usedQuantity = -1L;
					if(map.size()>0 && i==0  && hasControled){
						if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
							k = "amount_" + goodsResPrecontrolPolicyVO.getAmountId();
							usedQuantity = map.get(k);
							if(usedQuantity == null){
								usedQuantity = 0L;
							}
							hasControled = hasControled && goodsResPrecontrolPolicyVO.getLeftAmount() > usedQuantity;
							if(hasControled){
								goodsResPrecontrolPolicyVO.setLeftAmount(goodsResPrecontrolPolicyVO.getLeftAmount() - usedQuantity);
							}
						}else{
							k = "inventory_" + goodsResPrecontrolPolicyVO.getStoreId();
							usedQuantity = map.get(k);
							if(usedQuantity == null){
								usedQuantity = 0L;
							}
							hasControled = hasControled && goodsResPrecontrolPolicyVO.getLeftNum() > usedQuantity;
							if(hasControled){
								goodsResPrecontrolPolicyVO.setLeftNum(goodsResPrecontrolPolicyVO.getLeftNum() - usedQuantity);
							}
						}
					}*/
					
					
					
					if(hasControled ){
						// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
						resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(visitDate,orderItem.getCategoryId(), orderItem.getSuppGoodsId());
						if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
							hasControled = false;
						}else{
							LOG.info("***资源预控***");
							LOG.info("单酒店：" + orderItem.getSuppGoodsId() + "存在预控资源");
							for(int m=0,n=resPriceList.size();m<n;m++){
								ResPreControlTimePrice restimePrice = resPriceList.get(m);
								//销售价
								if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(restimePrice.getPriceClassificationCode())){
									precontrolSalePrice = restimePrice.getValue();
								}
								//结算价
								if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(restimePrice.getPriceClassificationCode())){
									precontrolSettlePrice = restimePrice.getValue();
								}
								//市场价
								if(OrderEnum.ORDER_PRICE_RATE_TYPE.MARKERPRICE_PRE.name().equals(restimePrice.getPriceClassificationCode())){
									precontrolMarketPrice = restimePrice.getValue();
								}
							}
						}
						
						
					}
					if(hasControled == false){
						precontrolSalePrice = null;
						precontrolSettlePrice = null;
						precontrolMarketPrice = null;
					}
					
					/** end **/
					
					
					if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
						//构造订单本地库存列表
						List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
						timePrice = timePriceHolder.getReturnContent();
						
						orderItem.putContent(OrderEnum.HOTEL_CONTENT.guarType.name(), timePrice.getGuarType());
						//只取首日最晚保留时间快照
						if(i==0){
							orderItem.putContent("latestUnguarDate", timePrice.getLatestUnguarDate());
						}			
						//将原结算价，放入备用计算字段
						timePrice.setBakPrice(timePrice.getSettlementPrice());
						//买断销售价
						if(precontrolSalePrice!=null){
							timePrice.setPrice(precontrolSalePrice);
						}
						//买断结算价
						if(precontrolSettlePrice!=null){
							timePrice.setSettlementPrice(precontrolSettlePrice);
						}
						if(timePrice !=null && stockFlagTimePrice == null){
							stockFlagTimePrice = timePrice;
						}

						 //添加促销的多价格属性
						List<OrdMulPriceRate> rates = new ArrayList<OrdMulPriceRate>();
						try {
							if (null != timePrice.getSettlementPrice() && null != timePrice.getPrice()) {
								// price.setPriceType();
								OrdMulPriceRate price = new OrdMulPriceRate();
								price.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
								price.setQuantity(orderItem.getQuantity());
								price.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
								price.setPrice(timePrice.getPrice());
								rates.add(price);
								OrdMulPriceRate settlePrice = new OrdMulPriceRate();
								settlePrice.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
								settlePrice.setQuantity(orderItem.getQuantity());
								settlePrice.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name());
								settlePrice.setPrice(timePrice.getSettlementPrice());
								rates.add(settlePrice);
								orderItem.setOrdMulPriceRateList(rates);
							} else {
								LOG.info("$$-----------$$ 没有priceInfo信息" + timePrice.getSuppGoodsId());
							}
						} catch (Exception e) {
							LOG.info("$$-----------$$ 添加促销多价格时间价格表异常" + timePrice.getSuppGoodsId()+e);
						}
						
						//时间价格表验证
						errorMsg = checkTimePriceTable(timePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.HOTEL_TIME_RATE.name(), date, orderStockList);
						if (errorMsg != null) {
							break;
						}
						
						OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;
						ProdPackageDetail detail = null;
						ProdPackageDetailAddPrice detailAddPrice = null;
						if(orderItem.getOrderPack()!=null&&orderItemDTO.getItem().getDetailId()!=null){
							//设置特殊加价规则，特殊加价到商品
							detailAddPrice = OrderUtils.getProdPackageDetailAddPriceByDetailIdSuppGoodsId((OrdOrderPackDTO)orderItemDTO.getOrderPack(), orderItemDTO.getItem().getDetailId(), date,goodsId);
							
							if(detailAddPrice==null){
							   detailAddPrice = OrderUtils.getProdPackageDetailAddPriceByDetailId((OrdOrderPackDTO)orderItemDTO.getOrderPack(), orderItemDTO.getItem().getDetailId(), date);
							}
							
							if(detailAddPrice == null) {
								detail = OrderUtils.getProdPackageDetailByDetailId((OrdOrderPackDTO)orderItemDTO.getOrderPack(), orderItemDTO.getItem().getDetailId());
								if(detail == null){					
									throwNullException("被打包的产品数据不存在");
								}
							}
						}
						
						if(detailAddPrice != null) {
							fillPackageOrderItemPrice(timePrice, detailAddPrice);
						} else if(detail != null){
							fillPackageOrderItemPrice(timePrice, detail);
						}
						//如果是券
						if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
							Map<String,Object> map =new HashMap<String, Object>();
							map.put("goodsId", goodsId);
							map.put("applyDate", visitDate);
							List<PresaleStampTimePrice> settlePrice=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
							timePrice.setSettlementPrice(settlePrice.get(0).getValue());
							//timePrice.setPrice(Long.valueOf(item.getPrice()));
						}
						
						//使用时间价格表填充订单子项
						accumulateOrderItemDataWithTimePrice(timePrice, orderItem);

						if(hasControled){
							
							/*Map<String, Long> buyoutMap = order.getBuyoutMap();
							String key = "";*/
							String preControlType = goodsResPrecontrolPolicyVO.getControlType();
							if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
								/*key = "amount_"+goodsResPrecontrolPolicyVO.getAmountId();
								Long usedAmount = 0L; 
								if(buyoutMap.size()>0 && i==0){
									usedAmount = buyoutMap.get(key);
									if(usedAmount ==null){
										usedAmount = 0L;
									}
								}*/
								
								
								//记录买断和非买断的结算总额
								leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
								if(leftMoney.longValue()>0||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
									buyoutNum = buyoutNum + orderItem.getQuantity();
								}
								//如果是按周期，那么应该讲之前扣减的，先扣减
								if(buyoutTotalPrice>0 && i > 0 && ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equals(goodsResPrecontrolPolicyVO.getControlClassification())){
									leftMoney = leftMoney - buyoutTotalPrice;
								}
								long shouldSettleTotalPrice = orderItem.getQuantity()*timePrice.getSettlementPrice();
								if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&"N".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
									long tmp = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
									buyoutNum = buyoutNum - orderItem.getQuantity() + tmp;
									//买断+非买断
									buyoutTotalPrice = buyoutTotalPrice + tmp*timePrice.getSettlementPrice();
									long notBuyNum = (orderItem.getQuantity() - tmp);
									if(notBuyNum>0){
										notMDnum = notBuyNum;
										notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * timePrice.getBakPrice();
									}
									
									
								}else if(shouldSettleTotalPrice<=leftMoney||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
									//买断
									buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
								}else{
									//只有非买断
									notMDnum = orderItem.getQuantity();
									buyoutNum = buyoutNum - notMDnum;
									shouldSettleTotalPrice = (orderItem.getQuantity()* timePrice.getBakPrice());
									notBuyoutTotalPrice = notBuyoutTotalPrice + shouldSettleTotalPrice;
								}
								if(buyoutNum>0){
									orderItem.setBuyoutQuantity(buyoutNum);
									orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
									orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
									leftMoney = leftMoney - shouldSettleTotalPrice;
									orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
								}
								
								/*buyoutMap.put(key, usedAmount + orderItem.getBuyoutTotalPrice());*/
								
							}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
								
								/*key = "inventory_"+goodsResPrecontrolPolicyVO.getStoreId();
								Long usedAmount = 0L; 
								if(buyoutMap.size()>0){
									usedAmount = buyoutMap.get(key);
									if(usedAmount ==null){
										usedAmount = 0L;
									}
								}*/
								
								
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
									buyoutsaledNum = orderItem.getBuyoutQuantity().longValue();
									//leftQuantity = leftQuantity - buyoutsaledNum;
									//如果是按周期，那么应该讲之前扣减的，先扣减
									if(buyoutsaledNum>0 && i > 0 && ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equals(goodsResPrecontrolPolicyVO.getControlClassification())){
										leftQuantity = leftQuantity - buyoutsaledNum;
									}
								}
								if(roomNum>leftQuantity && leftQuantity>0&&"N".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
									orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
									buyoutTotalPrice = buyoutTotalPrice + leftQuantity*timePrice.getSettlementPrice();
									notMDnum = roomNum - leftQuantity;
									notBuyoutTotalPrice = notBuyoutTotalPrice + (timePrice.getBakPrice() * notMDnum);
									//酒店设置非买断的总价
									orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
									//设置买断的总价
									orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
								}else if(roomNum<=leftQuantity||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
									//只有买断
									orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
									buyoutTotalPrice = buyoutTotalPrice + roomNum*timePrice.getSettlementPrice();
									orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
								}else{
									//只有非买断
									notMDnum = orderItem.getQuantity();
									long shouldSettleTotalPrice = (orderItem.getQuantity()* timePrice.getBakPrice());
									notBuyoutTotalPrice = notBuyoutTotalPrice + shouldSettleTotalPrice;
								}
								
								orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
								
								/*buyoutMap.put(key, usedAmount + orderItem.getBuyoutQuantity());*/
								
							}
							orderItem.setBuyoutFlag("Y");
							orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
						}
						
						everydayTimePriceList.add(timePrice);
						
						//预付
						if (SuppGoods.PAYTARGET.PREPAID.name().equals(suppGoods.getPayTarget())) {
							//首日
							if (i == 0) {
								orderItem.setDeductType(timePrice.getDeductType());
								deductTimePrice = timePrice;
							}
							
							//订单的预授权
							if (timePrice.getBookLimitType() != null) {
								if (!SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(ordOrderDTO.getPaymentType())) {
									ordOrderDTO.setPaymentType(timePrice.getBookLimitType());
								}
							}
							
							//预付时,如果时间价格表的退改策略存在“不退不改”，则订单子项的退改策略就为"不退不改"
							String cancelStrategy = timePrice.getCancelStrategy();
							if(StringUtils.isNotEmpty(cancelStrategy) && cancelStrategy.equals(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
								cancelStrategyTmp = cancelStrategy;
							}
						//现付
						} else if (SuppGoods.PAYTARGET.PAY.name().equals(suppGoods.getPayTarget())) {
							//现付时，订单子项的担保规则取的是哪一天的时间价格，退改策略就设置为哪一天的退改策略
							//首日
							if (i == 0) {
								orderItem.setDeductType(timePrice.getDeductType());
								
								guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(orderItem, item, timePrice);
								if (guaranteeTimePriceHolder.isFail()) {
									errorMsg = guaranteeTimePriceHolder.getMsg();
									break;
								}
								deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
								
								if (deductTimePrice != null
										&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
									allTimeGuaranteeTimePriceList.add(deductTimePrice);
								}
								
								//逻辑是在这一天设置的担保类型，那么就在这一天取退改策略
								String cancelStrategy = timePrice.getCancelStrategy();
								if(StringUtils.isNotEmpty(cancelStrategy)){
									cancelStrategyTmp = cancelStrategy;
								}
							} else {
								if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(timePrice.getBookLimitType())) {
									guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(orderItem, item, timePrice);
									if (guaranteeTimePriceHolder.isFail()) {
										errorMsg = guaranteeTimePriceHolder.getMsg();
										break;
									}
									deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
									if (deductTimePrice != null
											&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
										allTimeGuaranteeTimePriceList.add(deductTimePrice);
									}
								}
							}
						} else {
							errorMsg = "商品（ID=" + suppGoods.getSuppGoodsId() + "）" + suppGoods.getGoodsName() + "支付对象不存在。";
							cancelStrategyTmp = null;//出错了就不设置退改策略
							break;
						}
						//使用时间价格表填充订单
						OrderUtils.fillOrderWithTimePrice(timePrice, ordOrderDTO);
						
						

						// 酒店类型订单子项中，添加各天使用情况表记录
						OrdOrderHotelTimeRate ordOrderHotelTimeRate = OrderUtils.makeOrdOrderHotelTimeRateRecord(date, orderItem.getQuantity(), timePrice.getPrice(), timePrice.getSettlementPrice(), 0L,
								new Long(timePrice.getBreakfast()));
						ordOrderHotelTimeRate.setTimePrice(timePrice);
						ordOrderHotelTimeRate.setOrderStockList(orderStockList);
						ordOrderHotelTimeRate.setBuyoutFlag(hasControled?"Y":"N");
						ordOrderHotelTimeRate.setBuyoutNum(orderItem.getQuantity() - notMDnum);
						/*ordOrderHotelTimeRate.setPrePrice(precontrolSalePrice);
						ordOrderHotelTimeRate.setPreSettlementPrice(precontrolSettlePrice);
						ordOrderHotelTimeRate.setPreMarketPrice(precontrolMarketPrice);*/
						allList.addAll(orderStockList);
						
						makeOrderItemTime(orderItem,timePrice);
						
						ordOrderHotelTimeRateList.add(ordOrderHotelTimeRate);
						//为优惠信息设置时间价格表
						ordOrderDTO.addItemDateTimeTableForPromotion(orderItem, date, timePrice.getPrice(), timePrice.getSettlementPrice());
					} else {
						errorMsg = "您购买的商品中存在下架商品。";
						break;
					}
				}

				orderItem.setOrderStockList(allList);
				
				if (errorMsg == null) {
					//酒店各天使用状况和订单子项关联
					orderItem.setOrderHotelTimeRateList(ordOrderHotelTimeRateList);
					
					//设置订单子项资源状态
					setOrderItemResourceStatusByHotelRateTimeList(orderItem, ordOrderHotelTimeRateList);
					//大字段记录资源是否需要资源审核
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.needResourceConfirm.name(), orderItem.getNeedResourceConfirm());
					//大字段记录是否是保留房
					String stockFlag = "N";
					if(stockFlagTimePrice != null && StringUtil.isNotEmptyString(stockFlagTimePrice.getStockFlag())){
						stockFlag = stockFlagTimePrice.getStockFlag();
					}
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.stockFlag.name(), stockFlag.toUpperCase());//填充是否保留房
					orderItem.setStockFlag(stockFlag.toUpperCase());//是否保留房
					
					//存在全程担保
					if (allTimeGuaranteeTimePriceList.size() > 0) {
						deductTimePrice = getMaxDeductAmountTimePrice(orderItem, allTimeGuaranteeTimePriceList, everydayTimePriceList);
						orderItem.setDeductType(deductTimePrice.getDeductType());
					}
					
					//计算退改价格
					long deductAmount = 0;
					if (deductTimePrice != null) {
						deductAmount = computeOrderItemDeductAmount(orderItem, deductTimePrice, everydayTimePriceList);
						
						//如果是超时担保，获取最晚保留时间
						if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
							orderItem.putContent(OrderEnum.HOTEL_CONTENT.latestUnguarTime.name(), deductTimePrice.getLatestUnguarTime());
						}
					}
					orderItem.setDeductAmount(deductAmount);
					
					//设置订单子项最终的退改策略
					orderItem.setCancelStrategy(cancelStrategyTmp);

					//是否使用交通+X（酒店子单）的特殊处理逻辑
					try{
						boolean aeroHotelAction=needUserAeroHotelAction(ordOrderDTO,item,suppGoods);
						if(aeroHotelAction) {
							accumulateOrderItemPriceWithItem(orderItem, item);
							List<OrdOrderHotelTimeRate> hotelRateList=orderItem.getOrderHotelTimeRateList();
							List<AreoHotelTimeRateVo> areoHotelTimeRateVoList =item.getAreoHotelTimeRate();
							if (deductTimePrice != null) {
								deductAmount = computeOrderItemDeductAmountWithItem(orderItem, deductTimePrice,areoHotelTimeRateVoList);
								orderItem.setDeductAmount(deductAmount);
							}
							SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
							if(hotelRateList!=null && areoHotelTimeRateVoList !=null){
								for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : hotelRateList) {
									for (AreoHotelTimeRateVo areoHotelTimeRateVo : areoHotelTimeRateVoList) {
										if(areoHotelTimeRateVo.getVisitTime()==null || ordOrderHotelTimeRate.getVisitTime()==null){
											continue;
										}
										if(StringUtils.equals(format.format(ordOrderHotelTimeRate.getVisitTime()),format.format(areoHotelTimeRateVo.getVisitTime()))){
											ordOrderHotelTimeRate.setSettlementPrice(areoHotelTimeRateVo.getSettlementPrice());
											ordOrderHotelTimeRate.setPrice(areoHotelTimeRateVo.getPrice());
											break;
										}
									}
								}
							}

							OrdMulPriceRate rate=new OrdMulPriceRate();
							rate.setPrice(item.getAeroHotelPromotionPrice()*item.getQuantity());
							rate.setQuantity(1L);
							rate.setOrderItemId(orderItem.getOrderItemId());
							rate.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_HOTEL_PROMOTION.getCode());
							List<OrdMulPriceRate> rateList=orderItem.getOrdMulPriceRateList();
							if(rateList==null){
								rateList=new ArrayList<OrdMulPriceRate>();
								orderItem.setOrdMulPriceRateList(rateList);
							}
							rateList.add(rate);
						}
					}catch (BusinessException e){
						resultHandle.setMsg(e.getMessage());
					}

				}
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

	private long computeOrderItemDeductAmountWithItem(OrdOrderItemDTO orderItem, TimePrice applyTimePrice, List<AreoHotelTimeRateVo> areoHotelTimeRateVoList) {
		long deductAmount = 0;
		if (applyTimePrice.getDeductType() != null) {
			if (SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {

			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {

				deductAmount = orderItem.getPrice() * orderItem.getQuantity();

			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				if(areoHotelTimeRateVoList !=null && areoHotelTimeRateVoList.size()>0){
					deductAmount = areoHotelTimeRateVoList.get(0).getPrice() * orderItem.getQuantity();
				}
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {

				if(applyTimePrice.getDeductValue()==null){
					deductAmount = orderItem.getPrice() * orderItem.getQuantity();
					return 	deductAmount;
			    }
				deductAmount = applyTimePrice.getDeductValue() * orderItem.getQuantity();

			} else if (SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {

				if(applyTimePrice.getDeductValue()==null){
					deductAmount = orderItem.getPrice() * orderItem.getQuantity();
					return 	deductAmount;
				}
				deductAmount = (long) ((orderItem.getPrice() * orderItem.getQuantity()) * applyTimePrice.getDeductValue() / 100.0 + 0.5);

			} else {
				throw new IllegalArgumentException("TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=" + applyTimePrice.getDeductType() + ", is illegal.");
			}
		} else {
			LOG.info("OrderValidCheckBussiness.computeOrderItemDeductAmount: TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=null.");
		}
		return deductAmount;
	}

	/**
	 * 判断是否要在交通+X中 对酒店子单进行价格特殊处理
	 * @param ordOrderDTO
	 * @param item
	 * @return
	 */
	private boolean needUserAeroHotelAction(OrdOrderDTO ordOrderDTO, Item item,SuppGoods goods) {
		boolean isAutoPackProductOrder=orderLvfTimePriceServiceImpl.isAutoPackProductOrder(ordOrderDTO.getBuyInfo());
		if(!isAutoPackProductOrder){
			return Boolean.FALSE;
		}
		if(goods==null || goods.getCategoryId()==null || !BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(goods.getCategoryId())){
			return Boolean.FALSE;
		}
		if(item==null || item.getAeroHotelPromotionPrice()==null || item.getTotalAmount()==null || item.getTotalSettlementPrice()==null){
			throw new BusinessException("AeroHotel error：[AeroHotelPromotionPrice|TotalAmount|TotalSettlementPrice] is null ");
		}
		if(item.getTotalAmount().longValue()==0){
			throw new BusinessException("AeroHotel error：totalAmount is 0");
		}
		long price=0;
		long settlementPrice=0;
		for (AreoHotelTimeRateVo areoHotelTimeRateVo : item.getAreoHotelTimeRate()) {
			if(areoHotelTimeRateVo.getPrice()!=null){
				price=price+ areoHotelTimeRateVo.getPrice();
			}
			if(areoHotelTimeRateVo.getSettlementPrice()!=null){
				settlementPrice=settlementPrice+ areoHotelTimeRateVo.getSettlementPrice();
			}
		}
		if(item.getTotalAmount()!=price || item.getTotalSettlementPrice()!=settlementPrice*item.getQuantity()){
			LOG.error("cal amount and settlementPrice is error, calprice:"+price+" calSettlementPrice:"+settlementPrice+" TotalAmount():"+item.getTotalAmount()+" TotalSettlementPrice:"+item.getTotalSettlementPrice());
			throw new BusinessException("cal amount and settlementPrice is error, calprice:"+price+" calSettlementPrice:"+settlementPrice+" TotalAmount():"+item.getTotalAmount()+" TotalSettlementPrice:"+item.getTotalSettlementPrice());
		}
		return Boolean.TRUE;
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
	private String checkTimePriceTable(TimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
		String errorMsg = null;
		//订单本地库存记录
		OrdOrderStock ordOrderStock = null;
		//是否需要资源确认
		String needResourceConfirm = null;
		//资源状态
		String resourceStatus = null;
		//下单类型
		String inventory = null;

        LOG.info("#######酒店通过日期判定是否有共享库存库存########");
        Long groupId = orderItem.getSuppGoods().getGroupId();
        Long shareStock = null;
        LOG.info("SuppGoodsId"+orderItem.getSuppGoodsId());
        if(null != groupId){
            LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+visitTime);            
            shareStock = hotelTradeApiService.hasSharedStock(groupId, visitTime, orderItem.getSuppGoodsId());
            LOG.info("visitTime="+visitTime+"    groupId="+groupId+"     shareStock="+shareStock);
        }
        TimePriceCheckVO checkVO = null;
        if(null != shareStock){
            checkVO = timePrice.checkTimePriceForShareOrder(new Date(), (long) (orderItem.getQuantity()), shareStock);
        }else{
        	// 新增供应商对接酒店 一次最大可预订房间数
        	Long supplierId = orderItem.getSupplierId();
        	if (supplierId != null && !supplierId.equals(0)) {
        		String maxBookingStock = PropertiesUtil.getValue("maxQuantity_" + supplierId);
        		if (StringUtils.isNumeric(maxBookingStock)) {
        			long maxBookingStockLong = Long.valueOf(maxBookingStock);
        			if (timePrice.getStock() > maxBookingStockLong) {
        				LOG.info("供应商ID:[" + supplierId + "]对接酒店，一次最大可预订房间数为:[" + maxBookingStock + "]");
        				timePrice.setStock(maxBookingStockLong);
					}
				}
			}
            checkVO = timePrice.checkTimePriceForOrder(new Date(), (long) (orderItem.getQuantity()));
        }
		
		//TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(ordOrderDTO.getCreateTime(), orderItem.getQuantity());
		if (checkVO != null) {
			LOG.info("checkVO is not null"+GsonUtils.toJson(checkVO));
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
		
        if (StringUtil.isNotEmptyString(checkVO.getErrorCode())) {
            throwIllegalException(checkVO.getErrorCode(), checkVO.getNotAbleReason());
        }
		
		return errorMsg;
	}

	/**
	 * 设置担保类型
	 * 
	 * @param orderItem
	 * @param item
	 * @param timePrice
	 * @return
	 */
	private ResultHandleT<TimePrice> setHotelOrderItemGuaranteeInfo(OrdOrderItem orderItem, Item item, TimePrice timePrice) {
		ResultHandleT<TimePrice> guaranteeTimePriceHolder = new ResultHandleT<TimePrice>();
		
		if (item.getHotelAdditation() != null) {
			HotelAdditation hotelAdditation = item.getHotelAdditation();
			String bookLimitType = timePrice.getBookLimitType();
			//全程担保
			if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.ALLTIMEGUARANTEE.name());
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//一律担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.ALLGUARANTEE.name());
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//超时担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
				if (timePrice.getLatestUnguarTime() != null && timePrice.getLatestUnguarTime() > 0) {
					int totalMinute = 0;
					String arrivaltime = hotelAdditation.getArrivalTime();
					String[] timeStrings = arrivaltime.split(":");
					int hour = Integer.valueOf(timeStrings[0]).intValue();
					int minute = Integer.valueOf(timeStrings[1]).intValue();
					totalMinute = hour * 60 + minute;
					
					if (totalMinute > timePrice.getLatestUnguarTime() * 60) {
						//超时担保
						orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.TIMEOUTGUARANTEE.name());
						guaranteeTimePriceHolder.setReturnContent(timePrice);
					} 
					
//					if (timePrice.getLatestUnguarTime() > 0) {
//						int totalMinute = 0;
//						String arrivaltime = hotelAdditation.getArrivalTime();
//						String[] timeStrings = arrivaltime.split(":");
//						int hour = Integer.valueOf(timeStrings[0]).intValue();
//						int minute = Integer.valueOf(timeStrings[1]).intValue();
//						totalMinute = hour * 60 + minute;
//						
//						if (totalMinute > timePrice.getLatestUnguarTime() * 60) {
//							//超时担保
//							orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.TIMEOUTGUARANTEE.name());
//							guaranteeTimePriceHolder.setReturnContent(timePrice);
//							//扣款规则
//							orderItem.setDeductType(timePrice.getDeductType());
//						} 
//					} 
//					else {
//						errorMsg = "时间价格表（ID=" + timePrice.getTimePriceId() + "）最晚保留时间为" + timePrice.getLatestUnguarTime();
//						guaranteeTimePriceHolder.setMsg(errorMsg);
//						LOG.info("method processHotelTimePriceTable: " + errorMsg);
//					}
				}
			//房量担保
			} else if (timePrice.getGuarQuantity() != null && timePrice.getGuarQuantity() > 0) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
				if (orderItem.getQuantity() > timePrice.getGuarQuantity()) {
					//房量担保
					orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.QUANTITYGUARANTEE.name());
					guaranteeTimePriceHolder.setReturnContent(timePrice);
				}
			} else if (bookLimitType == null || SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equalsIgnoreCase(bookLimitType)) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
			}
		}
		
		return guaranteeTimePriceHolder;
	}
	
	/**
	 * 根据OrdOrderHotelTimeRate列表中的各个OrdOrderStock状态，设置订单子项资源状态
	 * 
	 * @param orderItem
	 * @param hotelRateTimeList
	 */
	private void setOrderItemResourceStatusByHotelRateTimeList(OrdOrderItem orderItem, List<OrdOrderHotelTimeRate> hotelRateTimeList) {
		if (orderItem != null && hotelRateTimeList != null) {
			for (OrdOrderHotelTimeRate hotelTimeRate : hotelRateTimeList) {
				OrderUtils.setOrderItemResourceStatusByOrderStockList(orderItem, hotelTimeRate.getOrderStockList());
			}
		}
	}
	
	/**
	 * 在时间价格表List中，找出退改最大的时间价格表
	 * 
	 * @param orderItem
	 * @param timePriceList
	 * @param everydayTimePriceList
	 * @return
	 */
	private TimePrice getMaxDeductAmountTimePrice(OrdOrderItem orderItem, List<TimePrice> timePriceList, List<TimePrice> everydayTimePriceList) {
		TimePrice maxDeductTimePrice = null;
		long maxDeductAmount = -1;
		long deductAmount = -1;
		for (TimePrice timePrice : timePriceList) {
			deductAmount = computeOrderItemDeductAmount(orderItem, timePrice, everydayTimePriceList);
			if (maxDeductTimePrice == null) {
				maxDeductTimePrice = timePrice;
				maxDeductAmount = deductAmount;
			} else {
				if (deductAmount > maxDeductAmount) {
					maxDeductTimePrice = timePrice;
					maxDeductAmount = deductAmount;
				}
			}
		}
		
		return maxDeductTimePrice;
	}
	
	/**
	 * 计算退改金额
	 * 
	 * @param orderItem
	 * @param applyTimePrice
	 * @param everydayTimePriceList
	 * @return
	 */
	private long computeOrderItemDeductAmount(OrdOrderItem orderItem, TimePrice applyTimePrice, List<TimePrice> everydayTimePriceList) {
		long deductAmount = 0;
		

		if (applyTimePrice.getDeductType() != null) {
			if (SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = orderItem.getPrice() * orderItem.getQuantity();
				/*Long deductBuyoutAmout = orderItem.getBuyoutPrice();
				if(deductBuyoutAmout!=null){
					orderItem.setDeductBuyoutAmout(deductBuyoutAmout);
				}*/
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if (everydayTimePriceList.get(0) != null) {
					deductAmount = everydayTimePriceList.get(0).getPrice() * orderItem.getQuantity();
					
					/*Long buyoutPrice = everydayTimePriceList.get(0).getBuyoutPrice();
					buyoutPrice = buyoutPrice== null ? everydayTimePriceList.get(0).getPrice() : buyoutPrice;
					orderItem.setDeductBuyoutAmout(buyoutPrice * orderItem.getQuantity());*/
				}
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
			
				if(applyTimePrice.getDeductValue()==null){
						deductAmount = orderItem.getPrice() * orderItem.getQuantity();
						return 	deductAmount;
				}
				deductAmount = applyTimePrice.getDeductValue() * orderItem.getQuantity();
				/*orderItem.setDeductBuyoutAmout(deductAmount);*/
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if(applyTimePrice.getDeductValue()==null){
					deductAmount = orderItem.getPrice() * orderItem.getQuantity();
					return 	deductAmount;
				}
				deductAmount = (long) ((orderItem.getPrice() * orderItem.getQuantity()) * applyTimePrice.getDeductValue() / 100.0 + 0.5);
				/*Long deductBuyoutAmout = orderItem.getBuyoutPrice();
				if(deductBuyoutAmout!=null){
					orderItem.setDeductBuyoutAmout((long)(deductBuyoutAmout* applyTimePrice.getDeductValue() / 100.0 + 0.5));
				}*/
				
			} else {
				throw new IllegalArgumentException("TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=" + applyTimePrice.getDeductType() + ", is illegal.");
			}
		} else {
			LOG.info("OrderValidCheckBussiness.computeOrderItemDeductAmount: TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=null.");
		}
		
		return deductAmount;
	}

	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
		ResultHandleT<SuppGoodsBaseTimePrice> resultHandleSuppGoodsBaseTimePrice = new ResultHandleT<SuppGoodsBaseTimePrice>();
		
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		if (suppGoodsBaseTimePrice != null) {
			resultHandleSuppGoodsBaseTimePrice.setReturnContent(suppGoodsBaseTimePrice);
		} else {
			resultHandleSuppGoodsBaseTimePrice.setMsg("商品ID=" + goodsId + ",date=" + specDate + ",checkAhead" + checkAhead + ",时间价格表SuppGoodsTimePrice不存在。");
		}
		
		return resultHandleSuppGoodsBaseTimePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap) {
		if (suppGoodsId != null && specDate != null && stock != null) {
			if (stock > 0) {
				SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
				if (suppGoodsBaseTimePrice != null) {
					SuppGoodsTimePrice timePrice = (SuppGoodsTimePrice) suppGoodsBaseTimePrice;
					//if ("Y".equalsIgnoreCase(timePrice.getRestoreFlag())) {
						if ("Y".equalsIgnoreCase(timePrice.getFreeSaleFlag())) {
							LOG.info("method:updateRevertStock[库存返回],message=商品（ID=" + suppGoodsId + "）日期（"  + specDate + ")时间价格表已经FreeSale状态。");
						} else {
							if (dataMap != null&&!dataMap.isEmpty()) {
								Long orderItemId=(Long)dataMap.get("orderItemId");
								Map<String, Object> params=new HashMap<String, Object>(1);
								params.put("orderItemId", orderItemId);
								params.put("visitTime", specDate);
								List<OrdOrderGroupStock> groupStockList=ordOrderGroupStockDao.selectByParams(params);
								//共享库存修改start
//								if(CollectionUtils.isNotEmpty(groupStockList)){ 
//									SuppGoodsBaseTimePrice itemTimePrice=null;
//									for (OrdOrderGroupStock ordOrderGroupStock : groupStockList) {
//										itemTimePrice=goodsTimePriceStockService.getTimePrice(ordOrderGroupStock.getSuppGoodsId(), ordOrderGroupStock.getVisitTime(), false);
//										//恢复库存
//										goodsTimePriceStockService.updateStock(itemTimePrice.getTimePriceId(), ordOrderGroupStock.getQuantity());
//									}
//								}else{
									//恢复库存
									goodsTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
//								}
									//共享库存修改end
//								
//								Boolean isUpdateSuperStock = (Boolean)dataMap.get("isUpdateSuperStock");
//								
//								if (isUpdateSuperStock != null && isUpdateSuperStock.booleanValue()) {
//									
//									if(CollectionUtils.isNotEmpty(groupStockList)){
//										for (OrdOrderGroupStock ordOrderGroupStock : groupStockList) {
//											petProdGoodsAdapter.updateStockByOrder(ordOrderGroupStock.getSuppGoodsId(), ordOrderGroupStock.getQuantity(), ordOrderGroupStock.getVisitTime(), ordOrderGroupStock.getVisitTime());
//										}
//									}else{
//										petProdGoodsAdapter.updateStockByOrder(suppGoodsId, stock, specDate, specDate);
//									}
//								}
							}else{
								//恢复库存
								goodsTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
							}
						}
					//}
				} else {
					throw new IllegalArgumentException("商品(ID=" + suppGoodsId + ")，时间(" + specDate + ")时间价格表不存在。");
				}
			} else {
				throw new IllegalArgumentException("返回的库存数要大于0(stock=" + stock + ")");
			}
		}
	}

	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId, Map<String, Object> dataMap) {
		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
		try {
			//商品是否可售
			if(suppGoods==null || !suppGoods.isValid()){
				resultHandleT.setMsg("商品不存在或无效。");
				return resultHandleT;
			}
			//商品参数验证
			checkParam(suppGoods, item, true);
			Date currDate = new Date();
			Date visitDate = item.getVisitTimeDate();

			int days = DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
			LOG.info("入住时间天数："+days);
			long totalSettlementPrice = 0L;
			Map<String,Long> settlementPriceMap=new HashMap<String, Long>();
			for(int i = 0; i < days; i++){
				Date oneDate = DateUtils.addDays(visitDate, i);
				ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), oneDate);
				LOG.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
				LOG.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
				if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
					return resultHandleT;
				}

				SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
				LOG.info("#######LAST酒店通过日期判定是否有共享库存库存LAST########");
				Long groupId = suppGoods.getGroupId();
				Long shareStock = null;
				LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
				if(null != groupId){
					LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
//					shareStock = iGoodsTimePriceStockService.getShareStock(groupId,oneDate);
					ResultHandleT<Long> shareStockHandleT = distGoodsTimePriceHotelClientService.getShareStock(groupId,oneDate);
					shareStock = shareStockHandleT.getReturnContent();
					LOG.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
				}
				if(null != shareStock){
					if(!this.checkSharedTimePrice(timePrice,currDate,(long)(item.getQuantity()),shareStock)){
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
						resultHandleT.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
						return resultHandleT;
					}
				}else{
					if (!this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
						resultHandleT.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
						return resultHandleT;
					}
				}
				LOG.info("[outboundBuRouteHotelTotalFSettlement] start");
				Long totalFSettlement = this.outboundBuRouteHotelTotalFSettlement(suppGoods.getSuppGoodsId(),oneDate);
				if( null != totalFSettlement) {
					totalSettlementPrice+=totalFSettlement;
					settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), totalSettlementPrice);
				} else {
					totalSettlementPrice+=timePrice.getSettlementPrice();
					settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
				}
				
			}

			if (suppGoods != null && suppGoods.getSupplierId() != null && suppGoods.getSuppSupplier() != null) {
				if ("Y".equals(suppGoods.getStockApiFlag())) {
					com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item,totalSettlementPrice,settlementPriceMap);
					if (supplierItem != null && supplierItem.getHotelAdditation() == null) {
						LOG.info("商品ID:" + item.getGoodsId() + "没有填写酒店信息。");
						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")没有填写酒店信息。");
						return resultHandleT;
					}
					resultHandleT.setReturnContent(supplierItem);
				}
			}
		} catch (OrderException e) {
            String errorCode = e.getErrorCode();
            if (StringUtil.isNotEmptyString(errorCode)) {
                resultHandleT.setErrorCode(errorCode);
            }
            resultHandleT.setMsg(e.getMessage());
        }catch (Exception e2) {
            resultHandleT.setMsg(e2.getMessage());
        }

		return resultHandleT;
	}

	private Long outboundBuRouteHotelTotalFSettlement(Long suppGoodsId,Date specDate){
		ResponseBody<HotelCurrencyInfoVstVo> response = new ResponseBody<>();
		RequestBody<Map<String, Object>> request = new RequestBody<>();
        Map<String, Object> paramMap = new HashMap<>();
        List<Date> visitTimeList = new ArrayList<Date>();
        paramMap.put("suppGoodsId", suppGoodsId);
        paramMap.put("specDate", specDate);
        //必须包含指定日期
        visitTimeList.add(specDate);
        paramMap.put("visitTimeList", visitTimeList);
        request.setT(paramMap);
        request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        response = hotelGoodsTimePriceQVstApiRemote.findCurrencyCodeBySuppGoodsId(request);
		HotelCurrencyInfoVstVo hotelCurrencyInfo = response.getT();
		LOG.info("[outboundBuRouteHotelTotalFSettlement][specDate]"+specDate+"[suppGoodsId]"+suppGoodsId);
		LOG.info("[outboundBuRouteHotelTotalFSettlement][currencyCodeIsNull]"+ (hotelCurrencyInfo==null? "": hotelCurrencyInfo.getCurrencyCode()));
		if (null != hotelCurrencyInfo && hotelCurrencyInfo.getCurrencyCode() != null) {
            String currencyCode = hotelCurrencyInfo.getCurrencyCode();
            BigDecimal exRateBig = hotelCurrencyInfo.getCashSellRate();
            Long firstDaySettlement = hotelCurrencyInfo.getFirstDaySettlement();
            Long totalFSettlement = hotelCurrencyInfo.getTotalFSettlement();
            Map<String, Long> dailySettlement = hotelCurrencyInfo.getDailySettlement();
            LOG.info("[specDate]"+specDate+"[suppGoodsId]"+suppGoodsId+"[totalFSettlement]"+totalFSettlement);
            LOG.info("[specDate]"+specDate+"[suppGoodsId]"+suppGoodsId+"[dailySettlement]"+JSONUtil.bean2Json(dailySettlement));
            if (null != exRateBig) {            	
                return totalFSettlement;
            }
        }
		return null;
		
	}
	
//	@Override
//	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId, Map<String, Object> dataMap) {
//		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
//
//		try {
//			//商品是否可售
//			if (suppGoods != null && suppGoods.isValid()) {
//
//				//商品参数验证
//				checkParam(suppGoods, item, true);
//
//				Date currDate = new Date();
//				Date visitDate = item.getVisitTimeDate();
//
//				ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), visitDate);
//
//				if (timePriceHolder.isSuccess() && timePriceHolder.getReturnContent() != null) {
//					SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
//					// 入住日期内总结算价
//					long totalSettlementPrice=timePrice.getSettlementPrice();
//					Map<String,Long> settlementPriceMap=new HashMap<String, Long>();
//					settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
//					if (this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
//						int days = DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
//						LOG.info("入住时间天数："+days);
//                        //Date oneDate = null;
//                        List<OrdOrderSharedStock> sharedStockList = new ArrayList<OrdOrderSharedStock>();
//						for(int i = 0; i < days; i++){
//                            Date oneDate = DateUtils.addDays(visitDate, i);
//							timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), oneDate);
//
//							LOG.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
//							if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
//								LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
//								resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
//								return resultHandleT;
//							}
//
//							timePrice = timePriceHolder.getReturnContent();
//
//                            LOG.info("#######LAST酒店通过日期判定是否有共享库存库存LAST########");
//                            Long groupId = suppGoods.getGroupId();
//                            Long shareStock = null;
//                            LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
//                            if(null != groupId){
//                                LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
//                                shareStock = iGoodsTimePriceStockService.getShareStock(groupId,oneDate);
//                                LOG.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
//                            }
//                            if(null != shareStock){
//                                if(!this.checkSharedTimePrice(timePrice,currDate,(long)(item.getQuantity()),shareStock)){
//                                    LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//                                    resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//                                    return resultHandleT;
//                                }else {
//                                    if(timePrice.isBeforeLastHoldTime(currDate)){
//                                        //设置库存
//                                        LOG.info("共享库存：商品ID=" + suppGoods.getSuppGoodsId() + "   VisitTime=" + oneDate + "   Quantity="+(long) item.getQuantity());
////                                        OrdOrderSharedStock sharedStock = new OrdOrderSharedStock();
////                                        sharedStock.setGroupId(groupId);
////                                        sharedStock.setVisitTime(oneDate);
////                                        sharedStock.setGoodsId(suppGoods.getSuppGoodsId());
////                                        sharedStock.setQuantity((long) item.getQuantity());
////                                        sharedStockList.add(sharedStock);
//                                    }
//                                }
//                            }else{
//                                if (!this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
//                                    LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//                                    resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//                                    return resultHandleT;
//                                }else{
//                                    //设置库存
//                                    LOG.info("库存：商品ID=" + suppGoods.getSuppGoodsId() + "   VisitTime=" + oneDate + "   Quantity="+(long) item.getQuantity());
////                                    OrdOrderSharedStock sharedStock = new OrdOrderSharedStock();
////                                    sharedStock.setGroupId(null);
////                                    sharedStock.setVisitTime(oneDate);
////                                    sharedStock.setGoodsId(suppGoods.getSuppGoodsId());
////                                    sharedStock.setQuantity((long)item.getQuantity());
////                                    sharedStockList.add(sharedStock);
//                                }
//                            }
//
//							totalSettlementPrice+=timePrice.getSettlementPrice();
//							settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
//						}
//                        item.setSharedStockList(sharedStockList);
//					} else {
//						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//					}
//
//					if (suppGoods != null && suppGoods.getSupplierId() != null && suppGoods.getSuppSupplier() != null) {
//						if ("Y".equals(suppGoods.getStockApiFlag())) {
//							com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item,totalSettlementPrice,settlementPriceMap);
//							if (supplierItem != null && supplierItem.getHotelAdditation() == null) {
//								LOG.info("商品ID:" + item.getGoodsId() + "没有填写酒店信息。");
//								resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")没有填写酒店信息。");
//								return resultHandleT;
//							}
//
//							resultHandleT.setReturnContent(supplierItem);
//						}
//
//					}
//				} else {
//					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + currDate + "时间价格表不存在。");
//					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
//				}
//			}
//		} catch (Exception e) {
//			resultHandleT.setMsg(e.getMessage());
//		}
//
//		return resultHandleT;
//	}
	
	private com.lvmama.vst.comm.vo.SupplierProductInfo.Item makeSupplierItem(Item buyInfoItem,Long totalSettlementPrice,Map<String,Long> settlementPriceMap) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.Item item = null;
		
		if (buyInfoItem != null) {
			item = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(null, buyInfoItem.getVisitTimeDate());
			item.setQuantity(new Long(buyInfoItem.getQuantity()));
			item.setSuppGoodsId(buyInfoItem.getGoodsId());
			item.setHotelAdditation(makeSupplierHotelAdditation(buyInfoItem.getHotelAdditation()));
			item.setSettlementPrice(totalSettlementPrice);
			item.setSettlementPriceMap(settlementPriceMap);
		}
		
		return item;
	}
	
	private com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation makeSupplierHotelAdditation(HotelAdditation buyInfoHotelAdditation) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation hotelAdditation = null;
		if (buyInfoHotelAdditation != null) {
			hotelAdditation = new com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation();
			BeanUtils.copyProperties(buyInfoHotelAdditation, hotelAdditation);
		}
		
		return hotelAdditation;
	}
	
	/**
	 * 商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	protected void checkParam(final SuppGoods suppGoods, final BuyInfo.Item item, boolean ck){
		
		super.checkParam(suppGoods, item, ck);
		
		if(ck){
			if(item.getHotelAdditation()==null||item.getHotelAdditation().getLeaveTimeDate()==null){
				throw new IllegalArgumentException("酒店商品 "+suppGoods.getGoodsName()+" 缺少离店日期");
			}
			int days=DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
            if((null != suppGoods.getMaxStayDay()) && (days>suppGoods.getMaxStayDay())){
//              throw new IllegalArgumentException("商品 "+suppGoods.getGoodsName()+" 超出最大可下单天数");
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.OUT_MAXIMUM_DAY.getErrorCode(), 
                                                "商品 "+suppGoods.getGoodsName()+" 超出最大可下单天数");
            }
            
            if((null != suppGoods.getMinStayDay()) && (days<suppGoods.getMinStayDay())){
//                throw new IllegalArgumentException("商品 "+suppGoods.getGoodsName()+" 少于最少入住天数");
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LESS_MIN_BOOK_DAYS.getErrorCode(), 
                        "商品 "+suppGoods.getGoodsName()+" 少于最少入住天数");
            }
		}
	}

	@Override
	public String getTimePricePrefix() {
		return "HotelTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		long amount = 0;
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			amount += fa.getAdultAmount();
		}
		long totalPrice =orderItem.getTotalSettlementPrice()-amount;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
	}
}
