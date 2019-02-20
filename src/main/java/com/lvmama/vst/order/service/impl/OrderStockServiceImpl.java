package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.comlog.LvmmLogEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.log.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;
import com.lvmama.comm.utils.JsonUtil;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsStockApiService;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.dest.api.vst.goods.po.HotelSuppGoodsVstStock;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.dest.api.vst.prod.service.IHotelProductQrVstApiService;
import com.lvmama.dest.api.vst.prod.vo.HotelProductVstVo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsDailyStock;
import com.lvmama.vst.back.goods.po.SuppGoodsStock;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.pub.po.ComPush;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.utils.order.ProductUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.flight.client.product.service.FlightSearchService;
import com.lvmama.vst.order.dao.OrdComplexSqlDao;
import com.lvmama.vst.order.exception.ErrorCodeEnum;
import com.lvmama.vst.order.exception.HasRecommendFlightException;
import com.lvmama.vst.order.service.IOrderInitService;
import com.lvmama.vst.order.service.IOrderStockService;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.util.OrderBookServiceDataUtil;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketAddTimePriceServiceImpl;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;
import com.lvmama.vst.pet.adapter.PetMessageServiceAdapter;
import com.lvmama.vst.supp.client.service.SupplierStockCheckService;
import com.lvmama.vst.suppTicket.client.product.po.IntfGoodsPriceIdRela;
import com.lvmama.vst.suppTicket.client.product.service.SuppTicketProductClientService;

import net.sf.json.JSONArray;
@Service
public class OrderStockServiceImpl extends AbstractBookService implements IOrderStockService {

	private static Logger logger = LoggerFactory.getLogger(OrderPriceServiceImpl.class);

	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	@Autowired
	@Qualifier("flightSearchService")
	protected FlightSearchService flightSearchService;
	
	@Autowired
	protected OrderLvfTimePriceServiceImpl orderLvfTimePriceServiceImpl;

    @Resource(name="goodsOraTimePriceStockService")
    protected IGoodsTimePriceStockService iGoodsTimePriceStockService;
    
    @Resource(name="supplierStockCheckService")
	private SupplierStockCheckService supplierStockCheckService;
    
    @Autowired
	private PetMessageServiceAdapter petMessageService;
    
    @Autowired
    private OrdComplexSqlDao ordComplexSqlDao;

	@Autowired
	private OrderBookServiceDataUtil orderBookServiceDataUtil;
	
	@Autowired
	SuppTicketProductClientService suppTicketProductClientService;

	@Autowired
	private IHotelGoodsStockApiService hotelGoodsStockApiService;
	
	@Autowired
	private IHotelGoodsQueryVstApiService hotelGoodsQueryVstApiServiceRemote;
	
	@Autowired
	private IHotelProductQrVstApiService hotelProductQrVstApiServiceRemote;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Resource
	private DestHotelAdapterUtils destHotelAdapterUtils;

	@Autowired
	private ProdProductClientService productClientService;

	/**
	 * 商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	private void checkParam(final SuppGoods suppGoods,final BuyInfo.Item item,boolean ck){
		
		if(suppGoods==null){
			throw new IllegalArgumentException("商品"+item.getGoodsId()+"不存在");
		}
		if(ck){
			if(item.getQuantity() > suppGoods.getMaxQuantity()){
				throw new IllegalArgumentException("商品"+item.getGoodsId()+" 订购数量超出最大值");
			}
			
			if(item.getQuantity() <suppGoods.getMinQuantity()){
				throw new IllegalArgumentException("商品"+item.getGoodsId()+" 订购数量小于最小值");
			}
			
			if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(suppGoods.getProdProduct().getBizCategory().getCategoryCode())){
				if(item.getHotelAdditation()==null||item.getHotelAdditation().getLeaveTimeDate()==null){
					throw new IllegalArgumentException("酒店商品"+item.getGoodsId()+" 缺少离店日期");
				}
				int days=DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
				if(days>suppGoods.getMaxStayDay()){
					throw new IllegalArgumentException("商品"+item.getGoodsId()+" 超出最大可下单天数");
				}
				
				if(days<suppGoods.getMinStayDay()){
					throw new IllegalArgumentException("商品"+item.getGoodsId()+" 少于最少入住天数");
				}
			}
		}

	}

	/**
	 * 检查一个酒店商品能否下单
	 * @param distributionId
	 * @param item
	 * @return
	 */
	@Override
	public ResultHandleT<Boolean> checkStock(Long distributionId, BuyInfo.Item item, boolean checkParamFlag) {
		ResultHandleT<Boolean> resultHandleT = new ResultHandleT<>();
		ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(distributionId, item.getGoodsId(), item.getVisitTimeDate());
		logger.info("OrderStockServiceImpl.checkStock(Date=" + item.getVisitTimeDate() + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
		if(timePriceHolder.isFail()){
			resultHandleT.setMsg("从对接酒店系统取商品[" + item.getGoodsId() + "], 时间[" + item.getVisitTime() + "]的时间价格表时出错，错误信息是:" + timePriceHolder.getMsg());
			return resultHandleT;
		}
		SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
		if(timePrice == null){
			resultHandleT.setMsg("从对接酒店系统取商品[" + item.getGoodsId() + "], 时间[" + item.getVisitTime() + "]的时间价格表为空");
			resultHandleT.setReturnContent(Boolean.FALSE);
			return resultHandleT;
		}

		try {
			SuppGoods suppGoods = null;
			SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
			suppGoodsParam.setProduct(true);
			suppGoodsParam.setProductBranch(true);
			suppGoodsParam.getProductParam().setBizCategory(true);
			ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsHotelAdapterClientService.findSuppGoodsById(item.getGoodsId(), suppGoodsParam);
			if(suppGoodsResultHandleT.isFail()){
				resultHandleT.setMsg(suppGoodsResultHandleT.getMsg());
				return resultHandleT;
			}			
			suppGoods = suppGoodsResultHandleT.getReturnContent();
			checkParam(suppGoods, item,checkParamFlag);
			TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(new Date(), (long) (item.getQuantity()));
			if(checkVO == null){
				logger.info("OrderStockServiceImpl.checkStock(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO=null");
				resultHandleT.setReturnContent(Boolean.FALSE);
				return resultHandleT;
			}
			if (!checkVO.isOrderAble()) {
				logger.info("OrderStockServiceImpl.checkStock(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO.isOrderAble()=false, notAbleReason=" + checkVO.getNotAbleReason());
				resultHandleT.setReturnContent(Boolean.FALSE);
				return resultHandleT;
			}
			resultHandleT.setReturnContent(Boolean.TRUE);
		} catch (Exception e) {
			logger.info("OrderStockServiceImpl.checkStock:resultHandleSuppGoods Exception,msg=" + e.getMessage());
			resultHandleT.setMsg(e);
		}
		return resultHandleT;
	}
	
	/**
	 * 酒店过了库存保留时间改库存为0
	 * @param timePrice
	 */
	private void converHoldStock(SuppGoodsTimePrice timePrice){
		//保留房才有最晚保留时间
		if ("Y".equalsIgnoreCase(timePrice.getStockFlag())) {
			java.util.Date date = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getLatestHoldTime().intValue());
			if(date.before(new java.util.Date())){
				timePrice.setStock(0L);
			}
		}
	}
	@Autowired
	private IOrderInitService orderInitService;

	@Override
	public ResultHandleT<SupplierProductInfo> checkStock(BuyInfo buyInfo) {
		ResultHandleT<SupplierProductInfo> result = new ResultHandleT<SupplierProductInfo>();
		Long distributionId = buyInfo.getDistributionId();
		SupplierProductInfo supplierProductInfo = null;
		String methodName = "OrderStockServiceImpl#checkStock【"+ buyInfo.getProductId() +"】";
		Long startTime = null;
		try {
			ResultHandleT<SuppGoods> resultHandleSuppGoods;
			OrderTimePriceService orderTimePriceService = null;
			SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
			suppGoodsParam.setProduct(true);
			suppGoodsParam.setProductBranch(true);
			suppGoodsParam.getProductParam().setBizCategory(true);
			suppGoodsParam.setSupplier(true);
			
			OrdOrderDTO order = new OrdOrderDTO(buyInfo);
			
			order = orderInitService.initOrder(order, false);
			if(CollectionUtils.isEmpty(order.getOrderItemList())){
				result.setMsg("订单结构不正常");
				return result;
			}
			
			Map<Long, Long> shareTotalStockMap = new HashMap<Long, Long>();
			Map<Long, Long> shareDayLimitMap = new HashMap<Long, Long>();

			ProdProductParam param = new ProdProductParam();
			param.setBizCategory(true);
			ProdProduct prodProduct = productClientService.findLineProductByProductId(buyInfo.getProductId(), param).getReturnContent();
			for (OrdOrderItem orderItem:order.getOrderItemList()) {
				
				if(orderLvfTimePriceServiceImpl.isLvfItemByCatetory(orderItem)){
					continue;
				}
				
				resultHandleSuppGoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), suppGoodsParam);
				if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
					SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
					if (suppGoods.isValid()) {
						orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);//createTimePriceService(suppGoods.getProdProduct().getBizCategory());
						OrdOrderItemDTO item = (OrdOrderItemDTO)orderItem;
						
						//表示是打包的产品
						if(item.getOrderPack()!=null){
							ProdPackageDetail detail = null;
							if(item.getItem().getDetailId() == null || item.getItem().getDetailId() <=0) {
								//门票自主打包才会出现直接打包到商品
								detail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO)orderItem.getOrderPack(), orderItem.getSuppGoodsId());
								if(detail != null && detail.getPackageCount() >= 0) {
									item.getItem().setCheckStockQuantity(Long.valueOf(item.getItem().getQuantity())*detail.getPackageCount());
								}
							}
						}
						
						//对于普通门票，需要校验共享总库存&日限制
						if(orderTimePriceService instanceof OrderTicketAddTimePriceServiceImpl && item.getItem() != null ) {
							logger.info("库存共享数据汇总");
							ResultHandleT<SuppGoodsBaseTimePrice> timePriceHandle = orderTimePriceService.getTimePrice(suppGoods.getSuppGoodsId(), item.getItem().getVisitTimeDate(), true);
							if(timePriceHandle != null && !timePriceHandle.hasNull()) {
								SuppGoodsBaseTimePrice timePrice = timePriceHandle.getReturnContent();
								if(timePrice != null && timePrice instanceof SuppGoodsAddTimePrice) {
									SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;
									//对共享总库存进行汇总
									if(addTimePrice.getShareTotalStockId() > 0) {
										if(shareTotalStockMap.containsKey(addTimePrice.getShareTotalStockId())) {
											item.getItem().setShareTotalStock(shareTotalStockMap.get(addTimePrice.getShareTotalStockId())
																	+ item.getItem().getCheckStockQuantity());
											shareTotalStockMap.put(addTimePrice.getShareTotalStockId(), shareTotalStockMap.get(addTimePrice.getShareTotalStockId()) + item.getItem().getCheckStockQuantity());
										} else {
											item.getItem().setShareTotalStock(item.getItem().getCheckStockQuantity());
											shareTotalStockMap.put(addTimePrice.getShareTotalStockId(), item.getItem().getCheckStockQuantity());
										}
									}
									
									//对共享日限制进行汇总
									if(addTimePrice.getShareDayLimitId() > 0) {
										if(shareDayLimitMap.containsKey(addTimePrice.getShareDayLimitId())) {
											item.getItem().setShareDayLimit(shareDayLimitMap.get(addTimePrice.getShareDayLimitId())
																	+ item.getItem().getCheckStockQuantity());
											shareDayLimitMap.put(addTimePrice.getShareDayLimitId(), shareDayLimitMap.get(addTimePrice.getShareDayLimitId()) + item.getItem().getCheckStockQuantity());
										} else {
											item.getItem().setShareDayLimit(item.getItem().getCheckStockQuantity());
											shareDayLimitMap.put(addTimePrice.getShareDayLimitId(), item.getItem().getCheckStockQuantity());
										}
									}
								}
							}
						}
						startTime = System.currentTimeMillis();

						boolean isNeedSkipCheckDistributorId= ProductUtil.isNeedSkipCheckDistributorId(prodProduct,distributionId);
						if(isNeedSkipCheckDistributorId){
							logger.info("product ["+prodProduct.getProductId()+"] isNeedSkipCheckDistributorId=true");
						}
						ResultHandleT<Object> resultHandleObject ;
						if(isNeedSkipCheckDistributorId){
							resultHandleObject = orderTimePriceService.checkStock(suppGoods, item.getItem(), null, null);
						}else{
							resultHandleObject = orderTimePriceService.checkStock(suppGoods, item.getItem(), distributionId, null);
						}

						Log.info(ComLogUtil.printTraceInfo(methodName,"库存检查", 
								"orderTimePriceService.checkStock", System.currentTimeMillis() - startTime));

						logger.info("正在进行库存检查，对单订单子项("+JSONArray.fromObject(item.getItem())+")本地检查结果为"+JSONArray.fromObject(resultHandleObject));
						
						//马戏票场次信息不为空，则需要进行对接校验
						if(item.getItem().getCircusActInfo() != null && StringUtils.isNotBlank(item.getItem().getCircusActInfo().getCircusActId())) {
                            logger.info("开始时间"+item.getItem().getCircusActInfo()
                                    .getCircusActStartTime());
                            if (StringUtils
									.isBlank(item.getItem().getCircusActInfo()
											.getCircusActStartTime())) {
								throwIllegalException("马戏票库存检查失败,马戏开始时间/结束时间为空!");
							}
							
							Long actStock = queryCircusActStock(
									orderItem.getSuppGoodsId(), item.getItem()
											.getVisitTimeDate(), item.getItem()
											.getCircusActInfo()
											.getCircusActId(), true);
							
							
							
							if(actStock < 0) {
								result.setMsg("马戏票库存获取失败");
								return result;
							}
							
							if(actStock < item.getItem().getQuantity()) {
								throwIllegalException("库存不足");
							}
						}
						if (resultHandleObject.isSuccess()) {
							if(!resultHandleObject.hasNull()){
								Object obj = resultHandleObject.getReturnContent();
								if (obj instanceof com.lvmama.vst.comm.vo.SupplierProductInfo.Item) {
									if (supplierProductInfo == null) {
										supplierProductInfo = new SupplierProductInfo();
									}
									com.lvmama.vst.comm.vo.SupplierProductInfo.Item it = (com.lvmama.vst.comm.vo.SupplierProductInfo.Item)obj;
									it.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
									supplierProductInfo.put(""+suppGoods.getSupplierId(), it);
								}
							}//else{ see log in vst-back
						} else {
							result.setMsg(resultHandleObject.getMsg());
							if (StringUtils.isNotBlank(resultHandleObject.getErrorCode())) {
                                result.setErrorCode(resultHandleObject.getErrorCode());
                            }
							return result;
						}
					} else {
						result.setMsg("您购买的商品-" + suppGoods.getGoodsName()  + "(ID=" + suppGoods.getSuppGoodsId() + ")不可售。");
						return result;
					}
					
				} else {
					result.setMsg("商品ID=" + orderItem.getSuppGoodsId() + "不存在。");
					result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIT_SUPPGOODS.getErrorCode());
					return result;
				}
			}

			Long productId = buyInfo.getProductId();
			//交通+x和自动打包交通的产品，不做对接机票库存校验，只打日志
			if(orderLvfTimePriceServiceImpl.isAutoPackProductOrder(buyInfo) || orderBookServiceDataUtil.isAutoPackTrafficProduct(productId)){
				logger.info("Product " + productId + " category is category_route_aero_hotel, or is package auto, will not do stock check.");
			} else {//其它情况做对接机票库存校验
				startTime = System.currentTimeMillis();
				orderLvfTimePriceServiceImpl.checkStock_remoteLVF(order.getOrderItemList());
				Log.info(ComLogUtil.printTraceInfo(methodName,"【对接】对接机票库存检查",
						"orderLvfTimePriceServiceImpl.checkStock_remoteLVF", System.currentTimeMillis() - startTime));
			}


			result.setReturnContent(supplierProductInfo);
		} catch (IllegalArgumentException ex) {
			result.setMsg(ex.getMessage());
		} catch (HasRecommendFlightException hasRecFlightEx){
			result.setErrorCode(ErrorCodeEnum.ErrorCode.HAS_RECOMMEND_FLIGHT.getErrorCode());
			result.setMsg(hasRecFlightEx.getMessage());
		} catch (Exception e) {
			result.setMsg(e.getMessage());
		} catch (Throwable e) {
			//记录响应日志
			LvmmLogEnum.recordLvmmLog(new Exception(e), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name());
			logger.error(methodName + "： " + e);
			result.setMsg(e.getMessage());
		}
		
		return result;
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
	 * 获取库存余量
	 * @param suppGoodsId
	 * @param visitTime
	 * @param actId
	 * @param retry
	 * @return
	 */
	private Long queryCircusActStock(Long suppGoodsId, Date visitTime, String actId, boolean retry) {
		Long actStock = -1l;
		try {
			actStock = supplierStockCheckService.getActCount(suppGoodsId, visitTime, actId);
		} catch (Exception e) {
			if(retry) {
				String exceptionDetails = ExceptionUtils.getFullStackTrace(e);
				if(exceptionDetails != null && exceptionDetails.indexOf("java.net.SocketTimeoutException") > -1) {
					return queryCircusActStock(suppGoodsId, visitTime, actId, false);
				}
			}
			logger.error("{}", e);
		}
		return actStock;
	}
	
	private com.lvmama.vst.comm.vo.SupplierProductInfo.Item makeSupplierItem(Item buyInfoItem) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.Item item = null;
		
		if (buyInfoItem != null) {
			item = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(null, buyInfoItem.getVisitTimeDate());
			item.setQuantity(new Long(buyInfoItem.getQuantity()));
			item.setSuppGoodsId(buyInfoItem.getGoodsId());
			item.setHotelAdditation(makeSupplierHotelAdditation(buyInfoItem.getHotelAdditation()));
		}
		
		return item;
	}
	
	private SupplierProductInfo makeSupplierProductInfo(BuyInfo buyInfo) {
		SupplierProductInfo supplierProductInfo = null;
		if (buyInfo != null) {
			supplierProductInfo = new SupplierProductInfo();
			if (buyInfo.getItemList() != null) {
				for (Item item : buyInfo.getItemList()) {
					if (item != null) {
						//目测现在没有用上产品及规格的内容，那就不用加载了。
						ResultHandleT<SuppGoods> resultHandleSuppGoods;
						try {
							resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(item.getGoodsId(), Boolean.FALSE, Boolean.FALSE);
							if (resultHandleSuppGoods.isSuccess()) {
								SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
								if (suppGoods != null && suppGoods.getSupplierId() != null && suppGoods.getSuppSupplier() != null) {
									if ("Y".equals(suppGoods.getSuppSupplier().getApiFlag())) {
										com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item);
										if (supplierItem != null && supplierItem.getHotelAdditation() == null) {
											throw new IllegalArgumentException("商品ID:" + item.getGoodsId() + "没有填写酒店信息。");
										}
										supplierProductInfo.put(suppGoods.getSupplierId().toString(), supplierItem);
									}
									
								} else {
									throw new IllegalArgumentException("商品ID:" + item.getGoodsId() + "供应商不存在。");
								}
							} else {
								logger.info("method makeSupplierProductInfo:商品ID:" + item.getGoodsId() + "获取失败，msg=" + resultHandleSuppGoods.getMsg());
								throw new RuntimeException("商品ID:" + item.getGoodsId() + "获取失败，msg=" + resultHandleSuppGoods.getMsg());
							}
						} catch (Exception e) {
							logger.error(ExceptionFormatUtil.getTrace(e));
							logger.info("method makeSupplierProductInfo:商品ID:" + item.getGoodsId() + "获取失败，Exception msg=" + e.getMessage());
							throw new RuntimeException("商品ID:" + item.getGoodsId() + "获取失败，msg=" + e.getMessage());
						}
						
					}
				}
			}
		}
		
		return supplierProductInfo;
	}

	@Override
	public List<OrdOrderStock> findOrderStockListByOrderItemId(Long orderItemId) {
		return ordComplexSqlDao.selectDistinctStocksByOrderItemId(orderItemId);
	}

    /**
     * 单酒店，根据商品ID，入住日期，离店日期，分销商ID查询库存
     * @param suppGoodsId 商品ID
     * @param visitTimeDate 入住日期
     * @param leaveTimeDate 离店日期
     * @param distributionId 分销商ID
     * @return
     */
    @Override
    public ResultHandleT<SuppGoodsStock> getHotelSuppGoodsStock(Long suppGoodsId, Date visitTimeDate, Date leaveTimeDate, Long distributionId) {
		//走vst接口
		if(suppGoodsId == null || DynamicRouterUtils.getInstance().isGrayGoodsId(suppGoodsId)){
			return getHotelSuppGoodsStockFromVst(suppGoodsId, visitTimeDate, leaveTimeDate, distributionId);
		}

		//走对接酒店接口
		return getHotelSuppGoodsStockFromDest(suppGoodsId, visitTimeDate, leaveTimeDate, distributionId);
    }

    /**
	 * 从vst中获取酒店商品库存
	 * */
    private ResultHandleT<SuppGoodsStock> getHotelSuppGoodsStockFromVst(Long suppGoodsId, Date visitTimeDate, Date leaveTimeDate, Long distributionId){
		ResultHandleT<SuppGoodsStock> resultHandleT = new ResultHandleT<SuppGoodsStock>();
		try {
			ResultHandleT<SuppGoods> resultHandleSuppGoods;
			SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
			suppGoodsParam.setProduct(true);
			suppGoodsParam.setProductBranch(true);
			suppGoodsParam.getProductParam().setBizCategory(true);
			suppGoodsParam.setSupplier(true);

			resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(suppGoodsId, suppGoodsParam);
			if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
				SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
				ResultHandleT<SuppGoodsStock> suppGoodsStock = this.getHotelSuppGoodsStock(suppGoods,
						visitTimeDate, leaveTimeDate, distributionId);
				if (suppGoodsStock.isFail() || null == suppGoodsStock.getReturnContent()) {
					logger.info("商品ID=" + suppGoods.getSuppGoodsId() + ",获取入住日期" + visitTimeDate + "至离店日期"
							+ leaveTimeDate + "的每日库存不存在。");
					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + "),获取入住日期"
							+ visitTimeDate + "至离店日期" + leaveTimeDate + "的每日库存不存在。");
					return resultHandleT;
				} else {
					resultHandleT.setReturnContent(suppGoodsStock.getReturnContent());
				}

			} else {
				resultHandleT.setMsg("商品ID=" + suppGoodsId + "不存在。");
				return resultHandleT;
			}
		}catch(Exception e){
			resultHandleT.setMsg(e.getMessage());
		}
		return resultHandleT;
	}

	/**
	 * 从对接酒店系统中获取
	 * */
	private ResultHandleT<SuppGoodsStock> getHotelSuppGoodsStockFromDest(Long suppGoodsId, Date visitTimeDate, Date leaveTimeDate, Long distributionId){
		RequestBody<HotelSuppGoodsVstStock> requestBody = new RequestBody<>();
		HotelSuppGoodsVstStock hotelSuppGoodsVstStock = new HotelSuppGoodsVstStock();
		hotelSuppGoodsVstStock.setSuppGoodsId(suppGoodsId);
		hotelSuppGoodsVstStock.setVisitTimeDate(visitTimeDate);
		hotelSuppGoodsVstStock.setLeaveTimeDate(leaveTimeDate);
		hotelSuppGoodsVstStock.setDistributionId(distributionId);
		requestBody.setT(hotelSuppGoodsVstStock);
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<HotelSuppGoodsVstStock> hotelSuppGoodsVstStockResponseBody = hotelGoodsStockApiService.getHotelSuppGoodsStock(requestBody);

		ResultHandleT<SuppGoodsStock> suppGoodsStockResultHandleT = new ResultHandleT<>();
		if(hotelSuppGoodsVstStockResponseBody == null || hotelSuppGoodsVstStockResponseBody.isFailure()){
			logger.error("Error occurs while trying to get hotel stock from dest api for goods " + suppGoodsId + ", visit time "
			+ DateUtil.formatDate(visitTimeDate, DateUtil.SIMPLE_DATE_FORMAT) +
			", leave time " + DateUtil.formatDate(leaveTimeDate, DateUtil.SIMPLE_DATE_FORMAT));
			suppGoodsStockResultHandleT.setMsg(hotelSuppGoodsVstStockResponseBody == null ? "Unknown error" : hotelSuppGoodsVstStockResponseBody.getErrorMessage());
			return suppGoodsStockResultHandleT;
		}

		hotelSuppGoodsVstStock = hotelSuppGoodsVstStockResponseBody.getT();
		SuppGoodsStock suppGoodsStock = new SuppGoodsStock();
		EnhanceBeanUtils.copyProperties(hotelSuppGoodsVstStock, suppGoodsStock);
		suppGoodsStockResultHandleT.setReturnContent(suppGoodsStock);
		return suppGoodsStockResultHandleT;
	}

    /**
     * 单酒店，根据商品，入住日期，离店日期，分销商ID查询库存
     * @param suppGoods 商品
     * @param visitTimeDate 入住日期
     * @param leaveTimeDate 离店日期
     * @param distributionId 分销商ID
     * @return
     */
    public ResultHandleT<SuppGoodsStock> getHotelSuppGoodsStock(SuppGoods suppGoods, Date visitTimeDate, Date leaveTimeDate, Long distributionId) {
        ResultHandleT<SuppGoodsStock> resultHandleT = new ResultHandleT<SuppGoodsStock>();
        try {
            //商品是否可售
            if(suppGoods==null || !suppGoods.isValid()){
                resultHandleT.setMsg("商品不存在或无效。");
                return resultHandleT;
            }

            SuppGoodsStock suppGoodsStock = new SuppGoodsStock();
            suppGoodsStock.setSuppGoodsId(suppGoods.getSuppGoodsId());
            List<SuppGoodsDailyStock> dailyStock = new ArrayList<SuppGoodsDailyStock>();
            
            int days = DateUtil.getDaysBetween(visitTimeDate, leaveTimeDate);
            logger.info("入住时间天数："+days);
            for(int i = 0; i < days; i++){
                Date oneDate = DateUtils.addDays(visitTimeDate, i);
                
                SuppGoodsDailyStock suppGoodsDailyStock = new SuppGoodsDailyStock();
                suppGoodsDailyStock.setDate(oneDate);
                
                logger.info("#######LAST酒店通过日期判定是否有共享库存库存LAST########");
                Long groupId = suppGoods.getGroupId();
                Long shareStock = null;
                logger.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
                if(null != groupId){
                    logger.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
                    shareStock = iGoodsTimePriceStockService.getShareStock(groupId,oneDate);
                    logger.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
                }
                if(null != shareStock){
                    //如果为共享库存，则取共享库存值
                    suppGoodsDailyStock.setInventory(shareStock);
                }else{
                    ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), oneDate);
                    logger.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
                    logger.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
                    if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
                        logger.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
                        resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
                        return resultHandleT;
                    }
                    SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
                    //如果为非共享库存，则取时间价格表中库存
                    suppGoodsDailyStock.setInventory(timePrice.getStock());
                }
                dailyStock.add(suppGoodsDailyStock);
            }
            
            suppGoodsStock.setSuppGoodsDailyStocks(dailyStock);
            resultHandleT.setReturnContent(suppGoodsStock);
        } catch (Exception e) {
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }
    
    public ProdProduct findProdProductById(Long productId) {
		if(destHotelAdapterUtils.checkHotelRouteEnableByProductId(productId)){
			try {
				RequestBody<Long> requestBody = new RequestBody<Long>();
				requestBody.setT(productId);
				requestBody.setToken(com.lvmama.vst.comm.vo.Constant.DEST_BU_HOTEL_TOKEN);
				com.lvmama.dest.api.common.ResponseBody<HotelProductVstVo> responseBody = hotelProductQrVstApiServiceRemote.get(requestBody);
				if (responseBody==null || responseBody.isFailure()) {
					logger.debug("use new service hotelProductQueryApiService#findProdProductListById fail!");
					return null;
				}
				if (responseBody.isSuccess()) {
					ProdProduct prodProduct = new ProdProduct();
					HotelProductVstVo vo = responseBody.getT();
					EnhanceBeanUtils.copyProperties(vo, prodProduct);
					logger.debug("use new service hotelProductQueryApiService#findProdProductListById success!");
					return prodProduct;
				}else {
					return null;
				}
			} catch (Exception e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
				logger.error(e.getMessage());
				return null;
			}
		}
		return null;
	}
	
  private SuppGoods getSuppGoodsIncludeProduct(Long suppGoodsId){
		SuppGoods goods = null;
		//如果当前酒店迁移系统已上线
		if(destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(suppGoodsId)){
			RequestBody<Long> requestBody = new RequestBody<Long>();
			requestBody.setT(suppGoodsId);
			requestBody.setToken(com.lvmama.vst.comm.vo.Constant.DEST_BU_HOTEL_TOKEN);
			com.lvmama.dest.api.common.ResponseBody<HotelGoodsVstVo> responseBody= hotelGoodsQueryVstApiServiceRemote.findSuppGoodsById(requestBody);
			HotelGoodsVstVo hotelGoodsVstVo= responseBody.getT();
			if(hotelGoodsVstVo != null){
				goods = new SuppGoods();
				EnhanceBeanUtils.copyProperties(hotelGoodsVstVo,goods);
			}
			ProdProduct product = findProdProductById(goods.getProductId());
			if(product != null){
				BizCategory category =  categoryClientService.findCategoryById(
						Long.valueOf(goods.getCategoryId())).getReturnContent();;
				product.setBizCategory(category);	
			}
			goods.setProdProduct(product);
		}
		return goods;
	}
	
}
