package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.utils.BusinessException;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.pub.po.ComPush;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.order.ProductUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.VstCheckStockService;
import com.lvmama.vst.neworder.order.router.ILookUpService;
import com.lvmama.vst.order.exception.ErrorCodeEnum;
import com.lvmama.vst.order.exception.HasRecommendFlightException;
import com.lvmama.vst.order.service.impl.OrderPriceServiceImpl;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketAddTimePriceServiceImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.pet.adapter.PetMessageServiceAdapter;
import com.lvmama.vst.supp.client.service.SupplierStockCheckService;

import net.sf.json.JSONArray;

@Component("vstCheckOrderStockService")
public class VstCheckOrderStockServiceImpl implements VstCheckStockService {

	private static Logger logger = LoggerFactory.getLogger(VstCheckOrderStockServiceImpl.class);

	@Autowired
	private ProdProductClientService productClientService;

	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

	@Autowired
	OrderTimePriceService orderTicketNoTimePriceService;

	@Autowired
	private PetMessageServiceAdapter petMessageService;

	@Resource(name = "supplierStockCheckService")
	private SupplierStockCheckService supplierStockCheckService;
    
	@Resource
	ILookUpService lookUpService;
	@Override
	public ResultHandleT<SupplierProductInfo> checkStock(HotelCombTradeBuyInfoVo.Item item, Long distributionId) {

		ResultHandleT<SupplierProductInfo> result = new ResultHandleT<SupplierProductInfo>();

		SupplierProductInfo supplierProductInfo = null;

		Long startTime = null;
		try {
			ResultHandleT<SuppGoods> resultHandleSuppGoods;
		//	OrderTimePriceService orderTimePriceService = orderTicketNoTimePriceService;
			SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
			suppGoodsParam.setProduct(true);
			suppGoodsParam.setProductBranch(true);
			suppGoodsParam.getProductParam().setBizCategory(true);
			suppGoodsParam.setSupplier(true);

			Map<Long, Long> shareTotalStockMap = new HashMap<Long, Long>();
			Map<Long, Long> shareDayLimitMap = new HashMap<Long, Long>();
		//	OrderTimePriceService orderTimePriceService = null;
			ProdProductParam param = new ProdProductParam();
			param.setBizCategory(true);
			ProdProduct prodProduct = null;
			resultHandleSuppGoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(item.getGoodsId(),
					suppGoodsParam);
			if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
				SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
				prodProduct = productClientService.findLineProductByProductId(suppGoods.getProductId(), param)
						.getReturnContent();
				OrderTimePriceService orderTimePriceService  = lookUpService.lookupTicketTimePrice(suppGoods.getCategoryId());
				if (suppGoods.isValid()) {
					startTime = System.currentTimeMillis();
					boolean isNeedSkipCheckDistributorId = ProductUtil.isNeedSkipCheckDistributorId(prodProduct,
							distributionId);
					if (isNeedSkipCheckDistributorId) {
						logger.info("product [" + prodProduct.getProductId() + "] isNeedSkipCheckDistributorId=true");
					}
					BuyInfo.Item buyInfoItem = new BuyInfo.Item();
					EnhanceBeanUtils.copyProperties(item, buyInfoItem);
					ResultHandleT<Object> resultHandleObject;
					if (isNeedSkipCheckDistributorId) {
						resultHandleObject = orderTimePriceService.checkStock(suppGoods, buyInfoItem, null, null);
					} else {
						resultHandleObject = orderTimePriceService.checkStock(suppGoods, buyInfoItem, distributionId,null);
					}

					Log.info(ComLogUtil.printTraceInfo("checkStock", "库存检查", "orderTimePriceService.checkStock",
							System.currentTimeMillis() - startTime));

					logger.info("正在进行库存检查，对单订单子项(" + JSONArray.fromObject(buyInfoItem) + ")本地检查结果为"
							+ JSONArray.fromObject(resultHandleObject));

					// 马戏票场次信息不为空，则需要进行对接校验
					if (buyInfoItem.getCircusActInfo() != null
							&& StringUtils.isNotBlank(buyInfoItem.getCircusActInfo().getCircusActId())) {
						logger.info("开始时间" + buyInfoItem.getCircusActInfo().getCircusActStartTime());
						if (StringUtils.isBlank(buyInfoItem.getCircusActInfo().getCircusActStartTime())) {
							throw new BusinessException("马戏票库存检查失败,马戏开始时间/结束时间为空!");
						}

						Long actStock = queryCircusActStock(item.getGoodsId(), buyInfoItem.getVisitTimeDate(),
								buyInfoItem.getCircusActInfo().getCircusActId(), true);

						// 推送库存信息至分销
						if (false && actStock <= 500) {
							petMessageService.sendPerformanceTicketMessage(item.getGoodsId(),
									ComPush.OBJECT_TYPE.PERFORMANCE_TICKET.name(),
									ComPush.PUSH_CONTENT.SUPP_GOODS_PERFORMANCE_STOCK.name(),
									DateUtil.formatSimpleDate(buyInfoItem.getVisitTimeDate()) + ","
											+ buyInfoItem.getCircusActInfo().getCircusActId() + "," + actStock);
						}

						if (actStock < 0) {
							result.setMsg("马戏票库存获取失败");
							result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
							return result;
						}

						if (actStock < buyInfoItem.getQuantity()) {
							result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
							throw new BusinessException("库存不足");
						}
					}

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
						if (StringUtils.isNotBlank(resultHandleObject.getErrorCode())) {
							result.setErrorCode(resultHandleObject.getErrorCode());
						}
						return result;
					}
				} else {
					//result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_ON_SALE.getErrorCode());
					result.setMsg("您购买的商品-" + suppGoods.getGoodsName() + "(ID=" + suppGoods.getSuppGoodsId() + ")不可售。");
					return result;
				}

			} else {
				result.setMsg("商品ID=" + item.getGoodsId() + "不存在。");
				result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIT_SUPPGOODS.getErrorCode());
				return result;
			}

			result.setReturnContent(supplierProductInfo);
		} catch (IllegalArgumentException ex) {
			result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.SYSTEM_INTERNAL_ERROR.getErrorCode());

			result.setMsg(ex.getMessage());
		} catch (HasRecommendFlightException hasRecFlightEx) {
			result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.SYSTEM_INTERNAL_ERROR.getErrorCode());
			result.setErrorCode(ErrorCodeEnum.ErrorCode.HAS_RECOMMEND_FLIGHT.getErrorCode());
			result.setMsg(hasRecFlightEx.getMessage());
		} catch (Exception e) {
			result.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.SYSTEM_INTERNAL_ERROR.getErrorCode());
			result.setMsg(e.getMessage());
		}

		return result;
	}

	/**
	 * 获取库存余量
	 * 
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
			if (retry) {
				String exceptionDetails = ExceptionUtils.getFullStackTrace(e);
				if (exceptionDetails != null && exceptionDetails.indexOf("java.net.SocketTimeoutException") > -1) {
					return queryCircusActStock(suppGoodsId, visitTime, actId, false);
				}
			}
			logger.error("{}", e);
		}
		return actStock;
	}

}
