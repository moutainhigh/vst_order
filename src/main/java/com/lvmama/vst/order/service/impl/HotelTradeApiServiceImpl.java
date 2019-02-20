package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Resource;

import kafka.utils.threadsafe;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.comm.utils.StringUtil;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.order.interfaces.IHotelOrderService;
import com.lvmama.dest.api.order.vo.HotelOrderRevertStock;
import com.lvmama.dest.api.order.vo.HotelOrderStock;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStock;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.dest.api.order.vo.buy.HotelBuyInfo;
import com.lvmama.dest.api.order.vo.supp.HotelSupplierProductInfo;
import com.lvmama.dest.api.price.vo.HotelPriceInfo;
import com.lvmama.dest.api.prom.vo.FavorStrategyInfoVo;
import com.lvmama.dest.api.prom.vo.PairVo;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsTimePriceVstVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.IHotelTradeApiService;
import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;

/**
 * 处理hotelTrade业务接口
 * @author chenpingfan
 *
 */
@Service
public class HotelTradeApiServiceImpl implements IHotelTradeApiService{

	private static final Logger LOG = LoggerFactory.getLogger(HotelTradeApiServiceImpl.class);
	@Autowired
	private IHotelOrderService hotelOrderService;
	
	@Autowired
	private DestHotelAdapterUtils destHotelAdapterUtils;
	
	@Resource(name="goodsOraTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
	@Autowired
	private OrdOrderStockDao ordOrderStockDao;
	
	@Override
	public PriceInfo countPriceByHotel(BuyInfo buyInfo) {
		LOG.info(":start hotelOrderService.countPriceByHotel....., buyInfo is " + GsonUtils.toJson(buyInfo));
		PriceInfo priceInfo = new PriceInfo();
		RequestBody<HotelBuyInfo> request = new RequestBody<HotelBuyInfo>();
		HotelBuyInfo hotelBuyInfo = transHotelBuyInfo(buyInfo);
		request.setT(hotelBuyInfo);
		request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<HotelPriceInfo> response = hotelOrderService.countPrice(request);
		LOG.info(":end hotelOrderService.countPriceByHotel.....result is " + GsonUtils.toJson(response));
		if(StringUtils.isEmpty(response.getErrorMessage())){
			HotelPriceInfo hotelPriceInfo = response.getT();
			priceInfo = transHotelPriceInfo(hotelPriceInfo,priceInfo);
		}else{
			LOG.info("countPriceByHotel is error。。。" + response.getErrorMessage());
		}	
		return priceInfo;
	}
	@Override
	public ResultHandleT<SupplierProductInfo> checkStock(BuyInfo buyInfo) {
		ResultHandleT<SupplierProductInfo> resultHandleT = new ResultHandleT<SupplierProductInfo>();
		RequestBody<HotelBuyInfo> request = new RequestBody<HotelBuyInfo>();
		request.setT(this.transHotelBuyInfo(buyInfo));
		LOG.info(":start hotelOrderService.checkStock.....");
		request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<HotelSupplierProductInfo> response = hotelOrderService.checkStock(request);
		LOG.info(":end hotelOrderService.checkStock.....");
		if (response.isFailure()) {
			resultHandleT.setMsg(response.getMessage());
			resultHandleT.setErrorCode(response.getErrorMessage());
		} else {
			HotelSupplierProductInfo hotelSupplierProductInfo = response.getT();
			if (null != hotelSupplierProductInfo) {
				resultHandleT.setReturnContent(this.transHotelSupplierProductInfo(hotelSupplierProductInfo));
			}
		}	
		LOG.info(":return hotelOrderService.checkStock.....");
		return resultHandleT;
	}

	@Override
	public ResultHandleT<HotelOrderUpdateStockDTO> deductStock(HotelOrderUpdateStockDTO deductStock) {
		ResultHandleT<HotelOrderUpdateStockDTO> resultHandleT = new ResultHandleT<HotelOrderUpdateStockDTO>();
		RequestBody<HotelOrderUpdateStockDTO> request = new RequestBody<HotelOrderUpdateStockDTO>();
		request.setT(deductStock);
		request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		try{
			ResponseBody<HotelOrderUpdateStockDTO> response = hotelOrderService.deductStock(request);
			if (response.isFailure()|| StringUtil.isNotEmptyString(response.getMessage())) {
				resultHandleT.setMsg(response.getMessage());
			} else {
				resultHandleT.setReturnContent(response.getT());
			}
		}catch(Exception e){
			resultHandleT.setMsg(e);
			throw e;
		}
		
		return resultHandleT;
	}

	@Override
	public ResultHandle updateRevertStock(Map<String, Map<Date, List<OrdOrderStock>>> hotelStockMap) {
		ResultHandle resultHandle = new ResultHandle();
		RequestBody<HotelOrderRevertStock> request = new RequestBody<HotelOrderRevertStock>();
		request.setT(this.transOrdOrderStock(hotelStockMap));
		request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<String> response = hotelOrderService.updateRevertStock(request);
		if (response.isFailure()) {
			resultHandle.setMsg(response.getMessage());
			resultHandle.setErrorCode(response.getErrorMessage());
		}
		return resultHandle;
	}
	

	@Override
	public ResultHandle revertStockForDeductFail(List<HotelOrderUpdateStockDTO> revertStocks) {
		ResultHandle resultHandle = new ResultHandle();
		RequestBody<HotelOrderUpdateStock> request = new RequestBody<HotelOrderUpdateStock>();
		HotelOrderUpdateStock hotelOrderUpdateStock = new HotelOrderUpdateStock();
		hotelOrderUpdateStock.setHotelOrderUpdateStockList(revertStocks);
		request.setT(hotelOrderUpdateStock);
		request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<String> response = hotelOrderService.revertStockForDeductFail(request);
		if (response.isFailure() || StringUtils.isNotEmpty(response.getMessage())) {
			resultHandle.setMsg(response.getMessage());
			resultHandle.setErrorCode(response.getErrorMessage());
			LOG.info("revertStockForDeductFail error:"+response.getMessage());
		}else
		{
			LOG.info("revertStockForDeductFail success and update OrdOrderStock status");
			for (HotelOrderUpdateStockDTO hotelOrderUpdateStockDTO : revertStocks) 
			{
				OrdOrderStock orderStock = new OrdOrderStock();
				orderStock.setOrderItemId(hotelOrderUpdateStockDTO.getOrderItemId());
				List<OrdOrderStock> ordOrderStockList = ordOrderStockDao.selectByOrderItemId(hotelOrderUpdateStockDTO.getOrderItemId());
				if(CollectionUtils.isNotEmpty(ordOrderStockList))
				{
					for (OrdOrderStock ordOrderStock : ordOrderStockList) {
						if((null != ordOrderStock.getVisitTime() && null != hotelOrderUpdateStockDTO.getSpecDate()) 
								&& ordOrderStock.getVisitTime().equals(hotelOrderUpdateStockDTO.getSpecDate())){
							ordOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.RESTOCK.name());
							ordOrderStockDao.updateByPrimaryKey(ordOrderStock);
							LOG.info("revertStockForDeductFail success and update OrdOrderStock orderItemId ="+ordOrderStock.getOrderItemId());
						}
					}
				}
			}
		}
		return resultHandle;
	}
	/**
	 * vst buyInfo 转为hotel buyinfo
	 * @param buyInfo
	 * @return
	 */
	private HotelBuyInfo transHotelBuyInfo(BuyInfo buyInfo){
		HotelBuyInfo hotelBuyInfo = new HotelBuyInfo();		
		EnhanceBeanUtils.copyProperties(buyInfo, hotelBuyInfo);	
		return hotelBuyInfo;		
	}
	

	/**
	 * priceInfo对象转化
	 * @param hotelPriceInfo
	 * @return
	 */
	private PriceInfo transHotelPriceInfo(HotelPriceInfo hotelPriceInfo,PriceInfo priceInfo){
		EnhanceBeanUtils.copyProperties(hotelPriceInfo, priceInfo);
		Long price = 0L;
		Long oughtPay = 0L;
		//预防copy遗漏
		if(null != hotelPriceInfo.getPrice()){
			price = hotelPriceInfo.getPrice();
			priceInfo.setPrice(price);
		}
		if(null != hotelPriceInfo.getOughtPay()){
			oughtPay = hotelPriceInfo.getOughtPay();
			priceInfo.setOughtPay(oughtPay);
		}			
		if(null != hotelPriceInfo.getCoupon()){
			priceInfo.setCoupon(hotelPriceInfo.getCoupon());
		}
		priceInfo.setItemPriceMap(hotelPriceInfo.getItemPriceMap());
		try
		{
			List<ResponseBody> listCoupons = hotelPriceInfo.getCouponResponse();	
			if(CollectionUtils.isNotEmpty(listCoupons)){
				List<ResultHandle> couponResultHandles = new ArrayList<ResultHandle>(2);
				for (ResponseBody<PairVo<FavorStrategyInfoVo, Long>> responseBody : listCoupons) {
					Pair<FavorStrategyInfo, Long> resultPair = new Pair<FavorStrategyInfo, Long>();						
					if(null !=responseBody.getT()){
						LOG.info("countPriceByHotel coupon responseBody is not null");
						PairVo<FavorStrategyInfoVo, Long> resultPairVO =responseBody.getT();
						FavorStrategyInfo first = new FavorStrategyInfo();
						EnhanceBeanUtils.copyProperties(resultPairVO.getFirst(), first);
						resultPair.setFirst(first);
					}					
					couponResultHandles.add(resultPair);
					priceInfo.setCouponResutHandles(couponResultHandles);
				}
			}	
		}catch(Exception e){
			LOG.info("countPriceByHotel error:"+e);
			e.printStackTrace();
		}
		LOG.info("countPriceByHotel priceInfo is after set"+GsonUtils.toJson(priceInfo));
		return priceInfo;		
	}
	
	private List<HotelOrderStock> transOrderStockToHotel(List<OrdOrderStock> ordStockList){
		List<HotelOrderStock> hotelOrderStockList = null;
		if(CollectionUtils.isNotEmpty(ordStockList)){
			hotelOrderStockList = new ArrayList<HotelOrderStock>();
			HotelOrderStock hotelStock = null;
			for (OrdOrderStock ordOrderStock : ordStockList) {
				hotelStock = new HotelOrderStock();
				EnhanceBeanUtils.copyProperties(ordOrderStock, hotelStock);
				hotelOrderStockList.add(hotelStock);
			}
		}
		return hotelOrderStockList;
	
	}
	/**
	 * 
	 * @Title: transHotelSupplierProductInfo
	 * @Description: SupplierProductInfo对象转化
	 * @param hotelSupplierProductInfo
	 * @return SupplierProductInfo 返回类型
	 */
	private SupplierProductInfo transHotelSupplierProductInfo(HotelSupplierProductInfo hotelSupplierProductInfo) {
		SupplierProductInfo supplierProductInfo = new SupplierProductInfo();
		EnhanceBeanUtils.copyProperties(hotelSupplierProductInfo, supplierProductInfo);
		return supplierProductInfo;

	}
	/**
	 * 
	 * @Title: transOrdOrderStock
	 * @Description: Map参数类型封装成HotelOrderRevertStock
	 * @param hotelStockMap
	 * @return HotelOrderRevertStock 返回类型
	 */
	private HotelOrderRevertStock transOrdOrderStock(Map<String, Map<Date, List<OrdOrderStock>>> hotelStockMap) {
		HotelOrderRevertStock hotelOrderRevertStock = new HotelOrderRevertStock();
		Map<String, Map<Date, List<HotelOrderStock>>> hotelDataOrderStockMap = new HashMap<String, Map<Date, List<HotelOrderStock>>>();
		//key:orderItemId value:Map<Date, List<OrdOrderStock> map size 3
		for (Entry<String, Map<Date, List<OrdOrderStock>>> entry : hotelStockMap.entrySet()) {
			List<HotelOrderStock> hotelOrderStocks = new ArrayList<HotelOrderStock>();
			Map<Date, List<HotelOrderStock>> subMap = new HashMap<Date, List<HotelOrderStock>>();
			//key:date value: List<OrdOrderStock>
			for (Entry<Date, List<OrdOrderStock>> subEntry : entry.getValue().entrySet()) {
				for (OrdOrderStock ordOrderStock : subEntry.getValue()) {
					if (null != ordOrderStock) {
						hotelOrderStocks.add(this.transHotelOrderStock(ordOrderStock));
					}
				}
				//key:date value:listStocks size 1
				subMap.put(subEntry.getKey(), hotelOrderStocks);
			}
			hotelDataOrderStockMap.put(entry.getKey(), subMap);
		}
		hotelOrderRevertStock.setHotelDataOrderStockMap(hotelDataOrderStockMap);
		return hotelOrderRevertStock;
	}
	/**
	 * 
	 * @Title: transHotelOrderStock
	 * @Description: OrdOrderStock对象转换
	 * @param ordOrderStock
	 * @return HotelOrderStock 返回类型
	 */
	private HotelOrderStock transHotelOrderStock(OrdOrderStock ordOrderStock) {
		HotelOrderStock hotelOrderStock = new HotelOrderStock();
		EnhanceBeanUtils.copyProperties(ordOrderStock, hotelOrderStock);
		return hotelOrderStock;
	}
	@Override
	public boolean checkIsHotelProduct(BuyInfo buyInfo) {
		LOG.info("...start checkIsHotelProduct...");
		boolean isHotel = false;
		if(null != buyInfo.getProductId()){
			LOG.info("...buyInfo ProductId  is "+buyInfo.getProductId());
			if(destHotelAdapterUtils.checkHotelRouteEnableByProductId(buyInfo.getProductId())){
				isHotel = true;
			}
		}else
		{
			if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
				if(buyInfo.getItemList().size()==1){
					LOG.info("...buyInfo getItemList size is 1...");
					for (BuyInfo.Item item : buyInfo.getItemList()) {
						if(destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(item.getGoodsId())){
							isHotel = true;
						}
					}
				}
			}			
		}
		return isHotel;
	}
	
	@Override
	public Long getHotelShareStock(Long groupId, Date specDate) {
		LOG.info("...getHotelShareStock start...");
		RequestBody<Map<String, Object>> requestBody = new RequestBody<Map<String, Object>>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("groupId", groupId);
		requestMap.put("specDate", specDate);
		requestBody.setT(requestMap);
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<Long> response = hotelOrderService.getShareStock(requestBody);
		Long shareStock = response.getT();
		return shareStock;
	}

	
	public SuppGoodsTimePrice getHotelGoodsTimePrice(Long goodsId,Date specDate){
		RequestBody<Map<String, Object>> requestBody = new RequestBody<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("goodsId", goodsId);
		map.put("specDate", specDate);
		requestBody.setT(map);
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		ResponseBody<HotelGoodsTimePriceVstVo> responseBody = hotelOrderService.getHotelGoodsTimePrice(requestBody);
		if(StringUtil.isEmptyString(responseBody.getMessage())){
			SuppGoodsTimePrice suppGoodsTimePrice = new SuppGoodsTimePrice();
			HotelGoodsTimePriceVstVo goodsTimePriceVO = responseBody.getT();
			EnhanceBeanUtils.copyProperties(goodsTimePriceVO,suppGoodsTimePrice);
			LOG.info("getHotelGoodsTimePrice result:"+GsonUtils.toJson(suppGoodsTimePrice));
			return suppGoodsTimePrice;
		}else{
			LOG.info("...getHotelGoodsTimePrice error..."+responseBody.getMessage());
		}
		return null;
	}
	

	/**
	 * 
	 * @Title: hasSharedStock
	 * @Description: 是否存在共享库存
	 * @param groupId
	 * @param visitTime
	 * @return boolean 返回类型
	 */
	public Long hasSharedStock(Long groupId, Date visitTime,Long suppGoodsId) {
		Long shareStock = null;
		boolean flag = DynamicRouterUtils.getInstance().isHotelSystemOnlineEnabled();
		if (null != groupId) {
			LOG.info("查询共享库存：groupId=" + groupId + " visitTime=" + visitTime);
			if(destHotelAdapterUtils.checkHotelRouteEnableByGoodsId(suppGoodsId)){
				shareStock = getHotelShareStock(groupId, visitTime);
			}else{
				shareStock = goodsTimePriceStockService.getShareStock(groupId, visitTime);
			}			
			LOG.info("updateSharedStock=====> oneDate=" + visitTime + " groupId=" + groupId + " shareStock="
					+ shareStock);
		}
		return shareStock;
	}
	
	
	
	@Override
	public Integer revertHotelCombGroupStock(HotelOrderUpdateStockDTO hotelOrderUpdateStock) {
		ResponseBody<Integer> responseBody = new ResponseBody<Integer>();
		RequestBody<HotelOrderUpdateStockDTO> requestBody = new RequestBody<HotelOrderUpdateStockDTO>();
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		requestBody.setT(hotelOrderUpdateStock);
		LOG.info("revertStock revertHotelCombGroupStock start。。。");
		responseBody = hotelOrderService.revertHotelCombGroupStock(requestBody);
		LOG.info("revertStock revertHotelCombGroupStock end。。。");
		Integer result = 0;
		if(StringUtil.isEmptyString(responseBody.getMessage())){
			result = responseBody.getT();
		}
		return result;
	}
	
	
}
