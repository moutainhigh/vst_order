/**
 * 
 */
package com.lvmama.vst.order.web;

import java.math.BigDecimal;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.comm.utils.StringUtil;
import com.lvmama.order.trade.hotel.api.price.IApiHotelOrderTradePriceService;
import com.lvmama.order.trade.hotel.api.price.vo.HotelPriceInfo;
import com.lvmama.order.trade.hotel.api.stock.IApiHotelOrderStockCheckService;
import com.lvmama.order.trade.vo.comm.BaseBuyInfoVo;
import com.lvmama.order.trade.vo.comm.OrdPersonVo;
import com.lvmama.order.trade.vo.hotel.HotelBuyInfoVo;
import com.lvmama.order.trade.vo.hotel.HotelItem;
import com.lvmama.order.trade.vo.supplier.CtripHotelPromVo;
import com.lvmama.order.trade.vo.stock.SupplierProductInfoVo;
import com.lvmama.order.trade.vo.stock.SupplierProductItemVo;
import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.ord.service.OrdUserCouponOrderClientService;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;

import com.lvmama.vst.order.utils.PropertiesUtil;
import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemRelation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;


/**
 * @author pengyayun
 *
 */
@Controller
public class AjaxBookQueryAction extends BaseOrderAciton {
	private static Logger logger = LoggerFactory.getLogger(AjaxBookQueryAction.class);
	@Autowired
	protected OrderService orderService;
	@Autowired
	protected OrdUserCouponOrderClientService ordUserCouponOrderService;
	//时间价格表适配器
	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	protected PromotionService promotionService;
	
	@Autowired
	protected DistrictClientService districtClientRemote;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientRemote;
	
	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;//调用vstpet获取现金和奖金余额接口


	@Resource(name = "apiHotelOrderTradePriceService")
	private IApiHotelOrderTradePriceService apiHotelOrderTradePriceService;
	@Resource(name = "apiHotelOrderStockCheckService")
	private IApiHotelOrderStockCheckService apiHotelOrderStockCheckService;
	
	@RequestMapping("/ord/book/ajax/priceInfo.do")
	@ResponseBody
	public Object queryPriceInfo(@ModelAttribute("orderForm")BuyInfo form){
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			BuyInfo buyInfo=converForm(form);
			initBooker(buyInfo);
			PriceInfo pi = orderService.countPrice(buyInfo);		
			if(pi.isSuccess()){
				Map<String, Object> attributes = new HashMap<String, Object>();
				List<PromPromotion> promList = pi.getPromotionList();
				attributes.put("promList", promList);
				msg.setAttributes(attributes);
				msg.addObject("priceInfo", pi);
				attributes.put("buyPersentActivity", pi.getBuyPresentActivityInfo());
			}else{
				msg.raise(pi.getMsg());
			}
		}catch (Exception e){
			// TODO: handle exception
			msg.raise("价格计算发生异常.");
		}
		return msg;
	}

	@RequestMapping("/ord/book/ajax/hotelBackPriceInfo.do")
	@ResponseBody
	public Object queryPriceInfo(@ModelAttribute("orderForm")HotelBuyInfoVo form){
		if (form.getCategoryId() != null && form.getCategoryId().longValue() == 4) {
			removeNullGoodsItem(form);
		}
		HotelBuyInfoVo buyInfo = converToHotelBackForm(form);
		ResultMessage msg = getPriceInfo(buyInfo);
		return msg;
	}

	/**
	 * 转换页面上提交的表单，页面上以map的方式提交数据
	 * 
	 * @param info
	 * @return
	 */
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	protected HotelBuyInfoVo converToHotelBackForm(HotelBuyInfoVo hotelBuyInfoVo) {
		// 身份证输入规范中的字母为大写，所以在此统一转为大写
		if (hotelBuyInfoVo != null) {
			List<OrdPersonVo> travellers = hotelBuyInfoVo.getTravellers();
			if (travellers != null && travellers.size() > 0) {
				for (OrdPersonVo person : travellers) {
					if (person != null && StringUtil.isNotEmptyString(person.getIdNo())) {
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}

		if (MapUtils.isNotEmpty(hotelBuyInfoVo.getItemMap())) {
			hotelBuyInfoVo.setItemList(new ArrayList<HotelItem>(hotelBuyInfoVo.getItemMap().values()));
			hotelBuyInfoVo.getItemMap().clear();
		}
		if (MapUtils.isNotEmpty(hotelBuyInfoVo.getProductMap())) {
			Collection<BaseBuyInfoVo.Product> productCollection = hotelBuyInfoVo.getProductMap().values();

			logger.info("开始转换日期-----");
			if (!CollectionUtils.isEmpty(productCollection)) {
				logger.info("productCollection.size=" + productCollection.size());
			}
			//航班信息不需要
//			convertTime(productCollection);
			logger.info("完成转换日期-----");
			hotelBuyInfoVo.setProductList(new ArrayList<BaseBuyInfoVo.Product>(productCollection));
			hotelBuyInfoVo.getProductMap().clear();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		if (hotelBuyInfoVo.getSuppGoodsFlag() != null && "true".equals(hotelBuyInfoVo.getSuppGoodsFlag())) {
			// 当为商品时
			params.put("suppGoodsId", hotelBuyInfoVo.getSuppGoodsId());
			params.put("cancelFlag", "Y");
//			try {
//				 com.lvmama.dest.comm.vo.ResultHandleT<List<DistDistributorGoods>> hander = distributorGoodsService.findDistDistributorGoodsList(params);
//				if (hander.getReturnContent() != null && hander.getReturnContent().size() > 0) {
//					for (DistDistributorGoods distDistributorGoods : hander.getReturnContent()) {
//						if (distDistributorGoods.getDistributorId() == Constant.DIST_OFFLINE_EXTENSION) {
//							hotelBuyInfoVo.setDistributionId(Constant.DIST_OFFLINE_EXTENSION);
//							break;
//						} else {
//							hotelBuyInfoVo.setDistributionId(Constant.DIST_FRONT_END); // 其他情况下还是按照前台下单处理
//						}
//					}
//				} else {
//					logger.info("distributorGoodsClientRemote.findDistDistributorGoodsList:" + hander.getMsg());
//				}
//			} catch (Exception e) {
//				BusinessException businessException = new BusinessException(e.getMessage());
//				logger.error("{}", businessException);
//			}
			hotelBuyInfoVo.setDistributionId(Constant.DIST_BACK_END); 
		} else if (hotelBuyInfoVo.getProductFlag() != null && "true".equals(hotelBuyInfoVo.getProductFlag())) {
			// 酒店产品的销售渠道都挂载在商品上面，此逻辑代码可注释
			/*
			 * params.put("productId", info.getProductId());
			 * params.put("cancelFlag", "Y"); try {
			 * ResultHandleT<List<DistDistributorProd>> hander =
			 * distDistributorProdClientRemote
			 * .findDistDistributorProdByParams(params); if
			 * (hander.getReturnContent() != null &&
			 * hander.getReturnContent().size() > 0) { for (DistDistributorProd
			 * distDistributorProd : hander.getReturnContent()) { if
			 * (distDistributorProd.getDistributorId() ==
			 * Constant.DIST_OFFLINE_EXTENSION) {
			 * info.setDistributionId(Constant.DIST_OFFLINE_EXTENSION); break; }
			 * else { info.setDistributionId(Constant.DIST_FRONT_END); //
			 * 其他情况下还是按照前台下单处理 } } } } catch (Exception e) { BusinessException
			 * businessException = new BusinessException(e.getMessage(),
			 * e.getCause()); logger.error("{}", businessException); }
			 */
		} else {
			hotelBuyInfoVo.setDistributionId(Constant.DIST_BACK_END);
		}
		UserUser user = userUserProxyAdapter.getUserUserByUserNo(hotelBuyInfoVo.getUserId());
		if (null != user) {
			hotelBuyInfoVo.setUserId(user.getUserId());
			hotelBuyInfoVo.setUserNo(user.getId());
		}
		if (CollectionUtils.isNotEmpty(hotelBuyInfoVo.getItemList())) {

			List<HotelItem> items = hotelBuyInfoVo.getItemList();
			// 将标记成主商品的Item放到itemList的第一位置
			if (items != null && items.size() > 1) {
				HotelItem mainItem = null;
				int mainItemIndex = -1;
				Iterator<HotelItem> iterator = items.iterator();
				while (iterator.hasNext()) {
					HotelItem item = iterator.next();
					if ("true".equals(item.getMainItem())) {
						mainItem = item;
						mainItemIndex = items.indexOf(item);
						break;
					}
				}
				if (mainItem != null && mainItemIndex != -1) {
					hotelBuyInfoVo.getItemList().remove(mainItemIndex);
					hotelBuyInfoVo.getItemList().add(0, mainItem);
				}
			}

			hotelBuyInfoVo.getItemList().get(0).setMainItem("true");
		}

		createDisneyOrderItem(hotelBuyInfoVo);

		return hotelBuyInfoVo;
	}
	
	/**
	 * 重新生成迪士尼子单
	 */
	private void createDisneyOrderItem(HotelBuyInfoVo hotelBuyInfoVo) {

		// 如果buInfo中的disneyOrderInfo不等于空，说明该订单是迪士尼的订单
		if (StringUtils.isNotEmpty(hotelBuyInfoVo.getDisneyOrderInfo())) {
			Long suppGoodsId = hotelBuyInfoVo.getItemList().get(0).getGoodsId();
			JSONObject jsonObj = (JSONObject) JSONObject.parse(hotelBuyInfoVo.getDisneyOrderInfo());

			hotelBuyInfoVo.setVisitTime((String) jsonObj.get("VisitDate"));
			JSONArray array = (JSONArray) jsonObj.get("OrderItems");
			hotelBuyInfoVo.getItemList().clear();

			for (int i = 0; i < array.size(); i++) {
				JSONObject jsonItem = (JSONObject) array.get(i);
				Integer quantity = (Integer) jsonItem.get("Quantity");
				Object ObjPrice = jsonItem.get("Price");
				Long price = null;
				if (ObjPrice instanceof Integer) {
					price = ((Integer) jsonItem.get("Price")).longValue() * 100;
				} else {
					price = ((BigDecimal) jsonItem.get("Price")).multiply(new BigDecimal(100)).longValue();
				}
				HotelItem hotelItem = new HotelItem();
				if (i == 0) {
					hotelItem.setMainItem("true");
				}
				hotelItem.setDisneyItemOrderInfo(JSONObject.toJSONString(jsonItem));
				hotelItem.setGoodsId(suppGoodsId);// 对于迪士尼演出票，不同区域的商品对应驴妈妈的商品是同一个商品ID
				hotelItem.setQuantity(quantity);
				hotelItem.setPrice(String.valueOf(price));
				hotelItem.setTotalAmount(price * quantity);
				hotelItem.setSettlementPrice(String.valueOf(price));
				hotelItem.setTotalSettlementPrice(price * quantity);
				hotelBuyInfoVo.getItemList().add(hotelItem);
			}
		}
	}

	/******
	 * 获取价格计算信息
	 * @param buyInfo
	 * @return
	 */
	private ResultMessage getPriceInfo(HotelBuyInfoVo hotelBuyInfo) {
		ResultMessage msg = ResultMessage.createResultMessage();
		UserUser loginUser =  userUserProxyAdapter.getUserUserByUserNo(hotelBuyInfo.getUserId());
		if (null != loginUser) {
			hotelBuyInfo.setUserId(loginUser.getUserId());
			hotelBuyInfo.setUserNo(loginUser.getId());
		}
		try {
			com.lvmama.order.api.base.vo.ResponseBody<HotelPriceInfo> responseBody = apiHotelOrderTradePriceService
					.countPrice(new RequestBody<HotelBuyInfoVo>(hotelBuyInfo));
			if (responseBody == null || responseBody.getT() == null) {
				logger.error("[价格计算发生异常][getPriceInfo]DestHotelPriceInfo is null!");
				msg.raise("[价格计算发生异常][getPriceInfo]DestHotelPriceInfo is null!");
				return msg;
			}
			
			HotelPriceInfo pi = responseBody.getT();
			if (responseBody.isSuccess()) {
				hotelBuyInfo.setOrderTotalPrice(PriceUtil.convertToFen(pi.getPrice()));
				Map<String, Object> attributes = new HashMap<String, Object>();
//				List<com.lvmama.order.vst.api.common.prom.vo.PromPromotionVo> promList = pi.getPromotionList();
				attributes.put("promList", pi.getPromotionDataList());
				attributes.put("price", pi.getPrice());
				attributes.put("buyPersentActivity", pi.getBuyPresentActivityInfo());
				msg.setAttributes(attributes);
				msg.addObject("priceInfo", pi);
			} else {
				msg.raise(responseBody.getMessage());
			}
		} catch (Exception e) {
			String emsg = ExceptionFormatUtil.getTrace(e);
			logger.error("[价格计算发生异常][getPriceInfo]" + emsg);
			msg.raise("[价格计算发生异常][getPriceInfo]" + emsg);
		}
		return msg;
	}

	private void removeNullGoodsItem(HotelBuyInfoVo form) {
		Map<Long, HotelItem> result = new HashMap<Long, HotelItem>();
		Map<Long, HotelItem> temp = form.getItemMap();
		String visitTime = "";
		for (Long key : temp.keySet()) {
			if (temp.get(key).getQuantity() > 0) {
				result.put(key, temp.get(key));
			}

			if (StringUtil.isNotEmptyString(temp.get(key).getVisitTime())) {
				visitTime = temp.get(key).getVisitTime();
			}
		}
		form.setItemMap(result);
		form.setSameVisitTime("true");
		form.setVisitTime(visitTime);
	}

	
	@RequestMapping("/ord/book/ajax/queryPromotion.do")
	@ResponseBody
	public Object queryPromotion(@ModelAttribute("orderForm")BuyInfo form){
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			BuyInfo buyInfo=converForm(form);
			 ResultHandleT<List<PromPromotion>> result = orderService.queryPromPromotion(buyInfo);
			 Map<String, Object> attributes = new HashMap<String, Object>();
			if(result.isSuccess()){
					 attributes.put("promList", result.getReturnContent());
				}else{
					 msg.raise(result.getMsg());
				}
			msg.setAttributes(attributes);
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
			msg.raise("查询促销发生异常.");
		}
		return msg;
	}
	
	@RequestMapping("/ord/book/ajax/queryMaxBonusAmount.do")
	@ResponseBody
	public Object queryMaxBonusAmount(@ModelAttribute("orderForm")BuyInfo form){
		BuyInfo buyInfo=converForm(form);
		initBooker(buyInfo);
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			long maxBouns = orderService.queryMaxBounsAmount(buyInfo);
			return maxBouns;
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
			msg.raise("查询奖金最高抵扣数异常.");
		}
		return 0;
	}
	
	
	@RequestMapping("/ord/book/ajax/checkStock.do")
	@ResponseBody
	public Object checkStock(@ModelAttribute("orderForm")BuyInfo form){
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			BuyInfo buyInfo=converForm(form);
			ResultHandle handle = orderService.checkStock(buyInfo);
			msg.raise(handle);
		}catch (Exception e) {
			// TODO: handle exception
			msg.raise("库存检查发生异常.");
		}
		return msg;
	}
	
	
	/**
	 * 库存校验
	 * @param form
	 * @return
	 */
	@RequestMapping("/ord/book/ajax/checkHotelBackStock.do")
	@ResponseBody
	public Object checkHotelBackStock(@ModelAttribute("orderForm") HotelBuyInfoVo form) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			// 签证
			if (form.getCategoryId() != null && form.getCategoryId().longValue() == 4) {
				removeNullGoodsItem(form);
			}
			HotelBuyInfoVo hotelBuyInfo = converToHotelBackForm(form);
			// 调用交易系统查询库存状态
			com.lvmama.order.api.base.vo.ResponseBody<SupplierProductInfoVo> responseBody = apiHotelOrderStockCheckService
					.checkStock(new RequestBody<HotelBuyInfoVo>(hotelBuyInfo));
			
			// 酒店没有推荐航班的信息，所以此逻辑取消
			if (responseBody.isFailure()) {
				msg.raise(responseBody.getMessage());
			}
			
			//处理对接返回价格以及满房，变价
			processResult(msg, responseBody, form);
			// 携程促销处理
			if (responseBody != null && responseBody.getT() != null) {
				SupplierProductInfoVo supplierProductInfoVo = responseBody.getT();
				// 商品对应的携程促销对象
				List<CtripHotelPromVo> ctripHotelPromVoList = supplierProductInfoVo.getCtripHotelPromVoList();
				if (CollectionUtils.isNotEmpty(ctripHotelPromVoList)) {
					// 判断商品是否满足它对应的所有携程促销规则的
					boolean flag = true;
					// 遍历携程促销集合，判断是否符合促销规则
					for (CtripHotelPromVo ctripHotelPromVo : ctripHotelPromVoList) {
						if (!ctripHotelPromVo.isMeet()) {
							flag = false;
							break;
						}
					}
					msg.addObject("isMeetCtripProm", flag);
					msg.addObject("ctripHotelPromVoList", ctripHotelPromVoList);
				}
			}

		} catch (Exception e) {
			msg.raise("库存检查发生异常. ID：【" + form.getProductId() + "】");
			logger.error("库存检查发生异常. ID：【" + form.getProductId() + "】。异常信息为：" + e, e);
		}
		return msg;
	}


	private void processResult(ResultMessage msg, com.lvmama.order.api.base.vo.ResponseBody<SupplierProductInfoVo> responseBody,
			HotelBuyInfoVo form){
		logger.info("processResult start---------------------");
		if (msg == null){
			msg = ResultMessage.createResultMessage();
		}
		if (form == null){
			msg.raise("库存检查发生异常,form请求为空");
			logger.error("库存检查发生异常,form请求为空");
		}
		if (responseBody == null){
			msg.raise("库存检查发生异常. ID：【" + form.getProductId() + "】");
			logger.error("库存检查发生异常. ID：【" + form.getProductId() + "】。异常信息为：" + "远端服务result为null");
		}
		Map<String, Object> attributes = new HashMap<>();
		logger.info("processResult result" + GsonUtils.toJson(responseBody));
		if (responseBody.isFailure()){
			attributes.put("errorCode", responseBody.getCode());
		}
		if (responseBody.getT() != null && !responseBody.getT().isEmpty()){
			logger.info("processResult returnContent = " + GsonUtils.toJson(responseBody.getT()));
			String totalChange = null;
			List<SupplierProductItemVo> itemVoList = null;
			Map<String, List<SupplierProductItemVo>> map = responseBody.getT().getMap();
			for (Map.Entry<String, List<SupplierProductItemVo>> e : map.entrySet()) {
				itemVoList = e.getValue();
			}
			long sellPrice = 0L;
			long oldPrice = 0L;
			if (itemVoList == null){
				msg.raise("库存检查发生异常. ID：【" + form.getProductId() + "】");
				logger.error("库存检查发生异常. ID：【" + form.getProductId() + "】。异常信息为：" + "获取对接DestHotelSupplierProductInfo.item为空");
			}
			logger.info("processResult DestHotelSupplierProductInfo.item = " + GsonUtils.toJson(itemVoList));
			Map<String, Long> sellPriceMap = itemVoList.get(0).getSellPriceMap();
			Map<String, Long> oldSellPriceMap = itemVoList.get(0).getOldSellPriceMap();
			if (sellPriceMap != null && !sellPriceMap.isEmpty()) {
				for (Map.Entry<String, Long> e : sellPriceMap.entrySet()){
					Long sell = e.getValue();
					sellPrice += sell.longValue();
				}
			}
			if (oldSellPriceMap != null && !oldSellPriceMap.isEmpty()) {
				for (Map.Entry<String, Long> e : oldSellPriceMap.entrySet()){
					Long sell = e.getValue();
					oldPrice += sell.longValue();
				}
			}
			if (oldPrice > sellPrice){
				totalChange = "酒店价格发生变更，房费总价下降" + (oldPrice - sellPrice) / 100 * itemVoList.get(0).getQuantity() + "元";
				attributes.put("errorCode", attributes.get("errorCode") + "_REDUCE");
			}else if (oldPrice < sellPrice){
				totalChange = "酒店价格发生变更，房费总价上升" + (sellPrice - oldPrice) / 100 * itemVoList.get(0).getQuantity() + "元";
				attributes.put("errorCode", attributes.get("errorCode") + "_ADD");
			}
			if (totalChange != null){
				attributes.put("totalChange", totalChange);
			}

			logger.info(" checkStock result = " + GsonUtils.toJson(msg));
		}
		msg.setAttributes(attributes);
	}

	/**
	 * 查询数据字典值
	 * 
	 * @param req
	 * @param response
	 */
	@RequestMapping(value = "/ord/book/ajax/expressDic.do")
	@ResponseBody
	public Object findBizDictData(HttpServletRequest req,
			HttpServletResponse response) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			// 组装查询条件
			Map<String, Object> params = new HashMap<String, Object>();
			String parentId = HttpServletLocalThread.getRequest().getParameter("parentId").split("@")[0];
			params.put("parentId", parentId);
			// 调用联想查询接口
			List<BizDistrict> provienceList = districtClientRemote
					.findDistrictList(params).getReturnContent();
			Collections.sort(provienceList, new Comparator<BizDistrict>() {
				@Override
				public int compare(BizDistrict o1, BizDistrict o2) {
					// TODO Auto-generated method stub
					if (o1.getDistrictId() > o2.getDistrictId()) {
						return 1;
					} else if (o1.getDistrictId() < o2.getDistrictId()) {
						return -1;
					}
					return 0;
				}
			});
			msg.addObject("provienceList", provienceList);
		}catch (Exception e) {
			// TODO: handle exception
			msg.raise("行政区域查询异常.");
		}
		return msg;
	}
	
	/**
	 * 快递价格计算。
	 * @param req
	 * @param orderId
	 * @param provinceCode
	 * @param cityCode
	 * @return
	 */
	@RequestMapping(value = "/ord/book/ajax/countExpressPrice.do")
	@ResponseBody
	public Object countExpressPrice(HttpServletRequest req,
			Long orderId,String provinceCode,String cityCode) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			List<Long> productIdsList = new ArrayList<Long>();
			long totalPrice = 0;
			List<Long> goodsIdList=new ArrayList<Long>();
			
			// 调用远程服务，进行价格查询
			ResultHandleT<Map<Long, ExpressSuppGoodsVO>> resultHandler = suppGoodsClientRemote
					.findSuppGoodsExpreeCost(productIdsList, provinceCode, cityCode);
			if (resultHandler != null&&resultHandler.isSuccess()) {
				Map<Long, ExpressSuppGoodsVO> map = resultHandler.getReturnContent();
				if (MapUtils.isNotEmpty(map)) {
					ExpressSuppGoodsVO item = null;
					Iterator<ExpressSuppGoodsVO> itr = map.values().iterator();
					while (itr.hasNext()) {
						item = itr.next();
						SuppGoodsNotimeTimePrice suppGoodsNotimeTimePrice = item
								.getSuppGoodsNotimeTimePrice();
						if (suppGoodsNotimeTimePrice != null
								&& suppGoodsNotimeTimePrice.getPrice() > 0) {
							totalPrice += suppGoodsNotimeTimePrice.getPrice();
							goodsIdList.add(item.getSuppGoodsId());
							/*itemPriceMap.put(item.getSuppGoodsId(),
									suppGoodsNotimeTimePrice.getPrice() + "");*/
						}
					}
				}
			}
			msg.addObject("totalPrice", PriceUtil.trans2YuanStr(totalPrice));
			msg.addObject("goodsIdList", goodsIdList);
		}catch (Exception e) {
			// TODO: handle exception
			msg.raise("快递价格计算发生异常.");
		}
		return msg;
	}
	
	@RequestMapping("/ord/book/ajax/findOrderExpressGoods.do")
	@ResponseBody
	public Object findOrderExpressGoods(BuyInfo form,HttpServletRequest req){
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
				BuyInfo buyInfo = converForm(form);
				 ResultHandleT<List<ExpressSuppGoodsVO>> resultHandle=orderService.findOrderExpressGoods(buyInfo);
			    if(!resultHandle.isSuccess()){
			    	msg.setCode(resultHandle.getMsg());
			    }else{
			    	msg.addObject("expressSuppGoodsVOList", resultHandle.getReturnContent());
			    	msg.addObject("visitTime", DateUtil.formatDate(DateUtil.dsDay_Date(new Date(), 1), "yyyy-MM-dd"));
			    }
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
			msg.setCode("查找快递发生异常");
		}
		 return msg;
	}

/*	@RequestMapping("/ord/book/ajax/queryPromPromotion.do")
	@ResponseBody
	public Object queryPromPromotion(@ModelAttribute("orderForm")BuyInfo form){
		BuyInfo buyInfo=converForm(form);
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			    List<PromPromotion> promList = loadGoodsPromInfo(buyInfo);
			 	//得到商品促销信息
			 	attributes.put("promList", promList);
			 	attributes.put("isExclusive", isExclusive(promList));
			 	msg.setAttributes(attributes);
		} catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
		}
		 return msg;
	}*/
	
	private boolean isExclusive(List<PromPromotion> promList){
		for(PromPromotion pp : promList){
			if(pp.isExclusivePromotion().equals("Y")){
				return true;
			}
		}
		return false;
	}
	/**
	 * 得到商品促销信息
	 * @param buyInfo
	 */
/*	private List<PromPromotion> loadGoodsPromInfo(BuyInfo buyInfo){
		
		List<PromPromotion> promList = new ArrayList<PromPromotion>();
		
		if (buyInfo.getItemList() != null) {
			for (BuyInfo.Item item : buyInfo.getItemList()) {
				Date beginDate =DateUtil.toDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
				
				ResultHandleT<SuppGoods> resultHandleT = null;
				try {
					resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, item.getGoodsId());
				} catch (Exception e) {
					e.printStackTrace();
				}
				SuppGoods suppGoods = resultHandleT.getReturnContent();
				BuyInfoPromotion.Item promItem = null;
				if(suppGoods!=null){
					promItem = getBuyInfoPromotionItem(suppGoods, item, buyInfo);
				}
				ResultHandleT<SuppGoodsAddTimePrice> timePriceResultHandleT=distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(Constant.DIST_FRONT_END, Long.valueOf(item.getGoodsId()), new Date());
				List<PromPromotion> list = promotionService.orderCreatePromList(suppGoods.getSuppGoodsId(), Constants.PROM_GOODS, buyInfo.getDistributionId(), beginDate, buyInfo, null, suppGoods, timePriceResultHandleT.getReturnContent(), false,promItem);
				if (list != null && list.size() > 0)
					promList.addAll(list);
			}
			
		}
		
		if (buyInfo.getProductList() != null) {
			for (BuyInfo.Product product : buyInfo.getProductList()) {
				ResultHandleT<TicketProductForOrderVO> productResultHandleT=prodProductClientService.findTicketProductForOrder(Long.valueOf(product.getProductId()), new Date());
				List<PromPromotion> list = promotionService.orderCreatePromList(product.getProductId(), Constants.PROM_PRODUCT, buyInfo.getDistributionId(), buyInfo.getProductList().get(0).getVisitTimeDate(), buyInfo, productResultHandleT.getReturnContent(), null, null, false,null);
				if (list != null && list.size() > 0)
					promList.addAll(list);
			}
		}
		
		for(int i = 0;i<promList.size();i++){
			PromPromotion promPromotion = promList.get(i);
			for(int j = i+1;j<promList.size();i++){
				PromPromotion subPP = promList.get(j);
				if(promPromotion.getPromitionType().equals(subPP.getPromitionType())){
					if(promPromotion.getPrice()>=subPP.getPrice()){
						promList.remove(j);
					}else{
						promList.remove(i);
					}
				}
			}
		}
		
		return promList;
	}*/
	
	
	protected BuyInfoPromotion.Item getBuyInfoPromotionItem(SuppGoods suppGoods,BuyInfo.Item item,BuyInfo buyInfo) {
		Date beginDate;
		if(buyInfo.getVisitTime()!=null){
			beginDate =DateUtil.toDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
		}else{
			beginDate =DateUtil.toDate(item.getVisitTime(), "yyyy-MM-dd");// 到访时间
		}
		BuyInfoPromotion.Item promItem = new BuyInfoPromotion.Item();
		BizCategory category=suppGoods.getProdProduct().getBizCategory();
		// 酒店情况的时候
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode().equalsIgnoreCase(category.getCategoryCode())){
			HotelAdditation hotelAdditation = item.getHotelAdditation();
			Long itemsPrice =0L;
			Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
			if (hotelAdditation != null) {
				String endDateStr = hotelAdditation.getLeaveTime();
				Calendar endCal = Calendar.getInstance();
				try {
					endCal.setTime(CalendarUtils.getDateFormatDate(endDateStr, "yyyy-MM-dd"));
				} catch (Exception e) {
					logger.error(ExceptionFormatUtil.getTrace(e));
				}
				Date endDate = DateUtils.addDays(endCal.getTime(), -1);
				Long distributorId = buyInfo.getDistributionId();
				ResultHandleT<List<TimePrice>> resultHandleList = distGoodsTimePriceClientServiceAdaptor.findTimePriceList(distributorId, suppGoods.getSuppGoodsId(), beginDate, endDate);
				
				List<TimePrice> timePriceList = resultHandleList.getReturnContent();
				if (resultHandleList.hasNull() ||timePriceList.isEmpty()) {
					StringBuffer buffer = new StringBuffer();
					buffer.append("未找到该商品信息 distributorId:").append(distributorId).append(",goodsId:").append(suppGoods.getSuppGoodsId()).append(",beginDate:").append(beginDate).append(",endDate：").append(endDate);
					logger.info(buffer.toString());
				}else{
					int quantity = item.getQuantity();// 购买商品数量 
					for (TimePrice timePriceObj : timePriceList) {
						long itemPrice = timePriceObj.getPrice() * quantity;//每个商品的价格
						itemsPrice += itemPrice;//同类商品的总价
						//商品促销优惠用
						BuyInfoPromotion.ItemPrice itemPc=new BuyInfoPromotion.ItemPrice(timePriceObj.getSpecDate());
						itemPc.setPrice(timePriceObj.getPrice());
						itemPriceMap.put(timePriceObj.getSpecDate(), itemPc);
					}
					promItem.setGoodsId(item.getGoodsId());
					promItem.setVisitTime(beginDate);
					promItem.setLeaveTime(endCal.getTime());
					promItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
					promItem.setPayTarget(suppGoods.getPayTarget());
					promItem.setQuantity(quantity);
					promItem.setTotalAmount(itemsPrice);
					promItem.setSettlementAmount(itemsPrice);
					promItem.setItemPriceMap(itemPriceMap);
				}
		}
			}
		return promItem;
	}
	
	/**
	 * 验证下单所使用的优惠券、奖金、现金情况
	 * 
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/book/ajax/checkOrderForPayOther.do")
	@ResponseBody
	public Object checkOrderForPayOther(@ModelAttribute("orderForm")BuyInfo form){
		ResultMessage msg = ResultMessage.createResultMessage();
		 Map<String, Object> attributes = new HashMap<String, Object>();
		 BuyInfo buyInfo=converForm(form);
		 String checkResult="";
		 Long bonusBalance=0L;
		 Long maxPayMoney=0L;
		 	try {
		 		checkResult =orderService.checkOrderForPayOther(buyInfo);
		 		if(checkResult!=""){
		 			VstCashAccountVO  vstCashAccountVO=  ordUserOrderServiceAdapter.queryMoneyAccountByUserId(buyInfo.getUserNo());
					bonusBalance=vstCashAccountVO.getBonusBalance();//获取奖金余额
					maxPayMoney=vstCashAccountVO.getMaxPayMoney();//获取可用于支付的现金余额
					attributes.put("bonusBalance", bonusBalance);
					attributes.put("maxPayMoney", maxPayMoney);
		 		}
		 	} catch (Exception e) {
				// TODO: handle exception
				msg.raise("获取登陆用户绑定优惠券信息异常");
				logger.error("获取登陆用户绑定优惠券信息异常",e);
			}finally{
				attributes.put("checkResult", checkResult);
			}
		 msg.setAttributes(attributes);
		return msg;
	}
	
	/**
	 * 添加优惠券
	 * @param form
	 * @return
	 */
	@RequestMapping("/book/ajax/insertCoupon.do")
	@ResponseBody
	public Object insertCoupon(@ModelAttribute("orderForm")BuyInfo form){
		logger.info("insertCoupon下单方法中接收到的JSON"+form.toJsonStr());
		String coupon = HttpServletLocalThread.getRequest().getParameter("coupon");
		String errMessage = "";
		Map<String, Object> attributes = new HashMap<String, Object>();
		BuyInfo buyInfo=form;
		Long categoryId = getCategoryIdByBuyInfo(buyInfo);
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route.getCategoryId().equals(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)){
			initBooker(buyInfo);
			createBuyInfo(buyInfo);
		}else
		{
			buyInfo = converForm(form);
		}
		if(StringUtils.isNotEmpty(coupon)){
			UserCouponVO userCouponVO = new UserCouponVO();
			userCouponVO.setCouponCode(coupon);
			List<UserCouponVO> userCouponvos = new ArrayList<UserCouponVO>();
			userCouponvos.add(userCouponVO);
			buyInfo.getUserCouponVoList().removeAll(buyInfo.getUserCouponVoList());
			buyInfo.setUserCouponVoList(userCouponvos);
		}
		List<UserCouponVO> couponList = null;
		try
		{
			couponList = orderService.getUserCouponVOList(buyInfo);
		}catch(Exception e)
		{
			errMessage = e.getMessage();
			logger.error("method getUserCouponVOList() error, ", e);
			attributes.put("errMessage", errMessage);
		}
		
		if(CollectionUtils.isNotEmpty(couponList)){
			
			for (UserCouponVO userCouponVO : couponList) {
				logger.info("返回的优惠券结果："+userCouponVO.getCouponCode());
				if(StringUtils.isNotEmpty(userCouponVO.getValidInfo())){
					logger.info("错误的优惠券："+userCouponVO.getCouponCode()+"错误原因："+userCouponVO.getValidInfo());
					errMessage= userCouponVO.getValidInfo();
					attributes.put("errMessage", errMessage);
				}
			}
			attributes.put("myCouponList", couponList);
		}
		
		return attributes;
	}
	
	@RequestMapping("/book/ajax/checkPromotionList.do")
	@ResponseBody
	public Object checkPromotionList(@ModelAttribute("orderForm")BuyInfo form){
		ResultMessage msg = ResultMessage.createResultMessage();
		 Map<String, Object> attributes = new HashMap<String, Object>();

		 String pomMessage="";

			
		 try {

			BuyInfo buyInfo=converForm(form);
			ResultHandleT<List<PromPromotion>> handle = orderService.checkPromAmount(buyInfo);
			if(handle.isFail()){
				msg.raise("促销检查异常.");
			}
			if (CollectionUtils.isNotEmpty(handle.getReturnContent())) {
				 attributes.put("promList", handle.getReturnContent());
				for(int i =0;i<handle.getReturnContent().size();i++)
				{
					if(i==0)
					{
						pomMessage+=handle.getReturnContent().get(i).getTitle();
					}else{
						pomMessage+="、"+handle.getReturnContent().get(i).getTitle();
					}	
				}
				if(StringUtils.isNotEmpty(pomMessage))
				{
					pomMessage="【"+pomMessage+"】";
				}
			}
		} catch (Exception e) {
			msg.raise("促销检查异常.");
			logger.error("促销检查异常.",e);
		}finally{
			attributes.put("pomMessage", pomMessage);
			 msg.setAttributes(attributes);
		}
		return msg;
	}
	
	/***
	 * 获取用户绑定优惠券以及奖金账户现金账户情况
	 * @param form
	 * @return
	 */
	@RequestMapping("/book/ajax/getLoginUserAccountInformation.do")
	@ResponseBody
	public Object getLoginUserAccountInformation(@ModelAttribute("orderForm")BuyInfo buyInfo){
		logger.info("getLoginUserAccountInformation下单方法中接收到的JSON"+buyInfo.toJsonStr());
		ResultMessage msg = ResultMessage.createResultMessage();
		 Map<String, Object> attributes = new HashMap<String, Object>();
		 Long bonusBalance=0L;
		 Long maxPayMoney=0L;
		 List<UserCouponVO> userCouponVOList=new ArrayList<UserCouponVO>();
		 	try 
		 	{	
		 		Long categoryId = getCategoryIdByBuyInfo(buyInfo);
		 		initBooker(buyInfo);
		 		if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
						||BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(buyInfo.getCategoryId())
						||BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(buyInfo.getCategoryId())
                        ||BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(buyInfo.getCategoryId())){
		 			buyInfo=converForm(buyInfo);
		 			logger.info("门票......getLoginUserAccountInformation下单方法中接收到的JSON"+buyInfo.toJsonStr());
		 		}else{
		 			createBuyInfo(buyInfo);
		 			logger.info("门票xxxxxxxxxgetLoginUserAccountInformation下单方法中接收到的JSON"+buyInfo.toJsonStr());
		 		}		 				 		
				VstCashAccountVO  vstCashAccountVO=  ordUserOrderServiceAdapter.queryMoneyAccountByUserId(buyInfo.getUserNo());
				bonusBalance=vstCashAccountVO.getNewBonusBalance();//获取奖金余额
				maxPayMoney=vstCashAccountVO.getMaxPayMoney();//获取可用于支付的现金余额
				userCouponVOList=orderService.getUserCouponList(buyInfo);
				if(limitCoupon(buyInfo.getUserNo())){
					userCouponVOList.clear();
					attributes.put("greaterThanMaxCouponAccount", true);
				}
				logger.info("userNo="+buyInfo.getUserNo()+"&bonusBalance="+bonusBalance+"maxPayMoney="+maxPayMoney);
			} catch (Exception e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
				msg.raise("获取登陆用户绑定优惠券信息异常");
			}finally{
				attributes.put("userCouponVOList", userCouponVOList);
				attributes.put("bonusBalance", bonusBalance.intValue());
				attributes.put("maxPayMoney", maxPayMoney.intValue());
				attributes.put("loginUserId", buyInfo.getUserNo());

			}
		 msg.setAttributes(attributes);
		return msg;
	}

	/**
	 * 校验优惠卷
	 * 1.必须是相同用户
	 * 2.默认是当天订单(天数为0)
	 * 3.默认最多可使用的优惠卷次数为6
	 * 4.订单支付状态为未支付或已支付
	 * 5.订单状态正常(sql里面已经做了)
	 * 6.下单渠道为后台下单
	 * @param userId
	 * @return
	 */
	private boolean limitCoupon(Long userId) {
		//从sweet配置中的const.properties获取配置的数据
		String couponDays = PropertiesUtil.getValue("couponDays");//使用优惠卷订单天数(0,代表当天,-1,代表昨天)
		String couponCount = PropertiesUtil.getValue("couponCount");//使用优惠卷订单次数(默认6次)
		String couponOrderPaymentStatus = PropertiesUtil.getValue("couponOrderPaymentStatus");//使用优惠卷订单支付状态(UNPAY/PAYED)
		logger.info("limitCoupon.....userId:"+userId+",couponDays:"+couponDays+",couponCount:"+couponCount+",couponOrderPaymentStatus:"+couponOrderPaymentStatus);
		if(couponDays == null){
			couponDays = "0";
		}
		if(couponCount == null){
			couponCount = "6";
		}
		if(couponOrderPaymentStatus == null){
			couponOrderPaymentStatus = "UNPAY|PAYED";
		}
		String[] split = couponOrderPaymentStatus.split("\\|");
		List<String> paymentStatus = Arrays.asList(split);
		Date startDate = new Date();//订单开始日期
		Date endDate = DateUtil.dsDay_Date(new Date(),Integer.parseInt(couponDays));//订单结束日期
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("createTime",startDate);
		params.put("endTime",endDate);
		params.put("paymentStatus",paymentStatus);

		Long counts = ordUserCouponOrderService.queryUserOrderCountByParams(params);
		if(counts >= (Long.parseLong(couponCount))){
			return true;
		}
		return false;
	}
	
	/**
	 * 组装BuyInfo的信息
	 * 
	 * @param buyInfo
	 * @param req
	 */
	private void createBuyInfo(BuyInfo buyInfo) {

		// 现在所有页面都有计算好的成人数和儿童数
		// 对于酒店套餐是根据基础儿童数*份数/基础成人数*份数
		converBuyInfo(buyInfo, String.valueOf(buyInfo.getAdultQuantity()), String.valueOf(buyInfo.getChildQuantity()));
	}

	public void converBuyInfo(BuyInfo buyInfo, String adultQuantityValue,
			String childNumValue) {
		if (MapUtils.isNotEmpty(buyInfo.getItemMap())) {
			buyInfo.setItemList(new ArrayList(buyInfo.getItemMap().values()));
			List<Item> itemList = new ArrayList<Item>();
			for (Item item : buyInfo.getItemList()) {
				if (item.getRouteRelation() != null
						&& item.getRouteRelation().equals(ItemRelation.MAIN)) {
					item.setAdultQuantity(Integer.parseInt(adultQuantityValue));
					item.setChildQuantity(Integer.parseInt(childNumValue));
				}
				if (item.getQuantity() > 0 || item.getAdultQuantity() > 0
						|| item.getChildQuantity() > 0) {
					if (item.getQuantity() == 0) {
						item.setQuantity(item.getAdultQuantity()
								+ item.getChildQuantity());
					}
					if(StringUtils.isEmpty(item.getVisitTime())){
						item.setVisitTime(buyInfo.getVisitTime());
					}					
					itemList.add(item);
				}
			}
			buyInfo.setItemList(itemList);
			buyInfo.getItemMap().clear();
		}
		log.info("buyinfo中的productMap是否为空：" + MapUtils.isNotEmpty(buyInfo.getProductMap()) );
		
		if (MapUtils.isNotEmpty(buyInfo.getProductMap())) {
			Long productId = null;
			Product product = null;
			List<Item> itemList = null;
			Iterator<Long> itr = buyInfo.getProductMap().keySet().iterator();
			while (itr.hasNext()) {
				productId = itr.next();
				product = buyInfo.getProductMap().get(productId);
				product.setProductId(productId);
				itemList = new ArrayList<Item>();
				if(CollectionUtils.isNotEmpty(product.getItemList())){
					for (Item item : product.getItemList()) {
						if (item.getRouteRelation() != null
								&& item.getRouteRelation()
										.equals(ItemRelation.MAIN)) {
							continue;
						}
						if (item.getQuantity() > 0 || item.getAdultQuantity() > 0
								|| item.getChildQuantity() > 0) {
							if (item.getQuantity() == 0) {
								item.setQuantity(item.getAdultQuantity()
										+ item.getChildQuantity());
							}
							if(StringUtils.isEmpty(item.getVisitTime())){
								item.setVisitTime(buyInfo.getVisitTime());
							}	
							itemList.add(item);
						}
					}
				}

				product.setItemList(itemList);

				if (StringUtils.isNotEmpty(adultQuantityValue)) {
					product.setAdultQuantity(Integer
							.parseInt(adultQuantityValue));
				}
				if (StringUtils.isNotEmpty(childNumValue)) {
					product.setChildQuantity(Integer.parseInt(childNumValue));
				}
				product.setQuantity(product.getAdultQuantity()
						+ product.getChildQuantity());
				product.setVisitTime(buyInfo.getVisitTime());

			}
			buyInfo.setProductList(new ArrayList<BuyInfo.Product>(buyInfo
					.getProductMap().values()));
			buyInfo.getProductMap().clear();
		}
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
	}


	/**
	 * 获取产品品类ID
	 * @param buyInfo
	 * @return
	 */
	private Long getCategoryIdByBuyInfo(BuyInfo buyInfo){
		Log.info("-------------start getCategoryIdByBuyInfo-----------");
		Long categoryId = buyInfo.getCategoryId();
		ProdProduct product = null;
		if(null ==categoryId || categoryId ==0L){
			 product = buyInfo.getProdProduct();
			if(null!=product){
				categoryId = product.getBizCategoryId();
			}else
			{
				if(null !=buyInfo.getProductId()){
//					ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductById(buyInfo.getProductId());
					ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(buyInfo.getProductId());
					if(null != restult && null != restult.getReturnContent()){
						product = restult.getReturnContent();
						categoryId = product.getBizCategoryId();
					}
				}else{
					List<Product> productList = buyInfo.getProductList();
					Long productId = 0L;
					if(CollectionUtils.isNotEmpty(productList)){
						productId = productList.get(0).getProductId();
//						ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductById(productId);
						ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(productId);
						if(null != restult){
							ProdProduct productBiz = restult.getReturnContent();
							categoryId = productBiz.getBizCategoryId();
						}
					}
				}
			}
		}
		return categoryId;		
	}
}
