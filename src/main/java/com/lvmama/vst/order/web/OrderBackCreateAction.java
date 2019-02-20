/**
 * 
 */
package com.lvmama.vst.order.web;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.dest.dock.response.order.ResponseCreditCardValidate;
import com.lvmama.dest.dock.service.interfaces.ApiCreditCardValidate;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.trade.hotel.api.service.IApiHotelOrderTradeService;
import com.lvmama.order.trade.hotel.api.user.IApiHotelOrderReceiverUserService;
import com.lvmama.order.trade.vo.comm.OrdOrderItemVo;
import com.lvmama.order.trade.vo.comm.OrdOrderVo;
import com.lvmama.order.trade.vo.comm.OrdPersonVo;
import com.lvmama.order.trade.vo.hotel.HotelBuyInfoVo;
import com.lvmama.order.trade.vo.hotel.HotelItem;
import com.lvmama.order.trade.vo.hotel.OrdOrderHotelTimeRateVo;
import com.lvmama.order.trade.vo.user.OrderReceiverUserVo;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.GuaranteeCreditCardUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.HotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 订单创建交互
 * @author lancey
 *
 */
@Controller
public class OrderBackCreateAction extends BaseOrderAciton {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderBackCreateAction.class);
	
	private static final String DATE_FORMAT="yyyy-MM-dd";
	
	private static final String ORDER_INFO_SESSION_KEY="WRITE_INFO_PAGE";
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	protected OrderService orderService;

	//时间价格表适配器
	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	@Autowired
	private ProdProductNoticeClientService prodProductNoticeClientService;
	
	@Autowired
	private PromotionService promotionService;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	
	@Autowired
	ApiCreditCardValidate apiCreditCardValidate;
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private DistrictClientService districtClientService;
	@Resource
	private IApiHotelOrderTradeService apiHotelOrderTradeService;
	@Resource
	private IApiHotelOrderReceiverUserService apiHotelOrderReceiverUserService;
	@Autowired
	private IComplexQueryService complexQueryService;

	/**
	 * 根据输入的商品信息填充下单页面内容
	 * @param req
	 * @return
	 */
	@RequestMapping("/ord/book/selectGoods.do")
	public String selectGoods(HttpServletRequest request, ModelMap model){
		String visitTime = request.getParameter("visitTime");// 入住时间
		String leaveTime = request.getParameter("leaveTime");// 离开时间
		String goods = request.getParameter("goodsId");
		String quantity = request.getParameter("quantity");
		String userId = request.getParameter("userId");
		boolean prepaidFalg = false;
		ResultHandleT<SuppGoods> resultHandleT = null;
		List<Person> personList=null;
		UserUser user=null;
		try {
			resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, Long.parseLong(goods));
			if(resultHandleT.getReturnContent()==null){
				model.addAttribute("ERROR","商品不可售");
				return "order/error";
			}
			if(!StringUtils.isEmpty(userId)){
				
				user=userUserProxyAdapter.getUserUserByUserNo(userId);

				//获取常用联系人
				personList=orderService.loadUserReceiversByUserId(userId);
			}
			 
		} catch (Exception e) {
			logger.error("{}", e);
		}
		
		String foreignFlag = "N"; // 是否境外
		SuppGoods suppGoods =null;
		if(null!=resultHandleT){
			suppGoods = resultHandleT.getReturnContent();
			ProdProduct product = suppGoods.getProdProduct();
			String productType = product.getProductType();
			if(StringUtils.isBlank(productType)){
				if(product.getBizDistrictId()!=null){
					ResultHandleT<BizDistrict> handle = districtClientService.findDistrictById(product.getBizDistrictId());
					if(!handle.hasNull()){
						if(handle.getReturnContent().getForeighFlag() != null && handle.getReturnContent().getForeighFlag().equals("Y"))
							foreignFlag = "Y";
					}
				}
			}else{
				if("FOREIGNLINE".equals(productType)){
					foreignFlag = "Y";
				}
			}
		}
		try {
			model.put("earliestArriveTime", getEarliestArriveTime(visitTime,suppGoods));//最早到店时间
		} catch (Exception e) {
			logger.error("{}", e);
		}
		//获取产品公告
		List<ProdProductNotice> productNoticeList = null;
		
		if(suppGoods != null) {
			productNoticeList = getProductNoticeList(suppGoods.getProductId(), visitTime, leaveTime);
		} else {
			logger.error("suppGoods is null");
		}
		if(StringUtils.isNotBlank(request.getParameter("channelCode")) && !"no_o2o".equals(request.getParameter("channelCode"))){
			model.addAttribute("channelCode",request.getParameter("channelCode").toString());
		}
		model.put("visitTime", visitTime);// 入住时间
		model.put("leaveTime", leaveTime);// 离开时间
		model.put("suppGoods", suppGoods);// 商品id
		model.put("quantity", getQuantity(quantity,null==suppGoods?new SuppGoods():suppGoods));// 数量
		model.put("personList", personList);//常用联系人
		model.put("userId", userId);//订票客户ID
		model.put("user", null==user?new UserUser():user);//订票客户
		model.put("productNoticeList", productNoticeList);//产品公告
		model.put("foreignFlag", foreignFlag); // 是否境外
		
		//是否可使用奖金优惠标示
    	if (StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())) {
			prepaidFalg = true;
		}
    	model.put("prepaidFalg", prepaidFalg);
		//清空session
		request.getSession().setAttribute(ORDER_INFO_SESSION_KEY, new BuyInfo());
		return "/order/showBookInfo";
	}
	
	private long getQuantity(String quantity,final SuppGoods goods){
		long q=NumberUtils.toLong(quantity);
		if(q>goods.getMaxQuantity()){
			q=goods.getMaxQuantity();
		}else if(q<goods.getMinQuantity()){
			q=goods.getMinQuantity();
		}
		return q;
	}
	
	
	/**
	 * 订单验证
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/verifyOrder.do")
	public String verifyOrder(BuyInfo form, ModelMap model){
		BuyInfo buyInfo = converForm(form);
		ResultHandleT<SuppGoods> resultHandleT = null;
		try{
			checkBuyInfo(buyInfo);
			initBooker(buyInfo);
			Long goodsId=null;
			for (int i = 0; i < buyInfo.getItemList().size(); i++) {
				if("true".equals(buyInfo.getItemList().get(i).getMainItem())){
					goodsId=buyInfo.getItemList().get(i).getGoodsId();
					break;
				}
			}
			resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, goodsId);
			if(resultHandleT.getReturnContent()==null){
				model.addAttribute("ERROR","商品不可售");
				return ERROR_PAGE;
			}
		}catch(IllegalArgumentException ex){
			logger.error(ExceptionFormatUtil.getTrace(ex));
			model.addAttribute("ERROR",ex.getMessage());
			return ERROR_PAGE;
		}catch(Exception ex){
			logger.error(ExceptionFormatUtil.getTrace(ex));
			model.addAttribute("ERROR",ex.getMessage());
			return ERROR_PAGE;
		}
		ResultHandle handle = orderService.checkStock(buyInfo);
		if(handle.isFail()){
			model.addAttribute("ERROR",handle.getMsg());
//			System.out.println("dddddddddddddddddddddddddddddd"+handle.getMsg());
			return ERROR_PAGE;
		}
		
		SuppGoods suppGoods = resultHandleT.getReturnContent();
//		Date endDate=DateUtils.addDays(
//				DateUtil.stringToDate(buyInfo.getItemList().get(0).getHotelAdditation().getLeaveTime(), DATE_FORMAT), -1);
		/*ResultHandleT<List<TimePrice>> timePriceResultHandleT =
				distGoodsTimePriceClientService.findTimePriceList(Constant.DIST_FRONT_END, suppGoods.getSuppGoodsId(),
						buyInfo.getItemList().get(0).getVisitTimeDate(), endDate);
		
		List<TimePrice> timePriceList = timePriceResultHandleT.getReturnContent();
		
		PriceInfo pi = orderService.countPrice(buyInfo);
		 
		if(pi.isSuccess()){
			model.put("priceInfo", pi);
		}else{
			model.put("priceInfo", new PriceInfo());
		}*/
		
		model.put("suppGoods", suppGoods);// 商品id
		//model.put("timePriceList", timePriceList);// 商品id
		model.put("stayCount", stayCount(buyInfo));//住几晚
		model.addAttribute("visitTime",DateUtil.getFormatDate(buyInfo.getItemList().get(0).getVisitTimeDate(),"MM月dd日"));
		model.addAttribute("weekStr",DateUtil.getZHDay(buyInfo.getItemList().get(0).getVisitTimeDate()));
		return "/order/verifyOrder";
	}
	
	/**
	 * 生成订单
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/createOrder.do")
	public String createOrder(HotelBuyInfoVo form,ModelMap model) throws BusinessException{
		HotelBuyInfoVo buyInfo = converHotelForm(form);
		try{
			checkHotelBuyInfo(buyInfo);
			initHotelBooker(buyInfo);
			
			//验证信用信息
			//GuaranteeCreditCardUtil.validCheck(buyInfo,apiCreditCardValidate);
			validHotelCheck(buyInfo, apiCreditCardValidate);
			
		}catch(IllegalArgumentException ex){
			model.addAttribute("ERROR",ex.getMessage());
			return ERROR_PAGE;
		}
		
		buyInfo.setIp("180.169.51.82");
		
		// 重新设置登录用户id
		String operatorId = getLoginUserId();
		buyInfo.setBackUserId(operatorId);

		 com.lvmama.order.api.base.vo.ResponseBody<OrdOrderVo> orderResponseBody = null;
		OrdOrderVo ordOrderVo = null;
		// 调用下单接口
		if (buyInfo != null) {
			// 调用酒店下单接口
			orderResponseBody = apiHotelOrderTradeService.submitOrder(new RequestBody<HotelBuyInfoVo>(buyInfo));
			// 判断下单是否为空
			if (orderResponseBody.isFailure() || orderResponseBody.getT() == null) {
				model.addAttribute("ERROR", orderResponseBody.getErrorMessage());
				logger.info("----------CreateOrderException--------------");
				return ERROR_PAGE;
			}
			ordOrderVo = orderResponseBody.getT();
		}

//		ResultHandleT<OrdOrder> orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
//		if(orderHandle.isFail()){
//			model.addAttribute("ERROR",orderHandle.getMsg());
//			return ERROR_PAGE;
//		}
		//OrdOrder order=orderHandle.getReturnContent();
		OrdOrder order = complexQueryService.queryOrderByOrderId(ordOrderVo.getOrderId());
		ResultHandleT<SuppGoods> resultHandleT = null;
		try{
			resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, order.getMainOrderItem().getSuppGoodsId());
			if(resultHandleT.getReturnContent()==null){
				model.addAttribute("ERROR","商品不可售");
			}
			try {
				// 保存常用游客
				OrderReceiverUserVo orderReceiverUserVo = new OrderReceiverUserVo();
				// 下单用户Id
				orderReceiverUserVo.setUserId(order.getUserId());
				// 游玩人
				orderReceiverUserVo.setTravellers(buyInfo.getTravellers());
				// 更新常用联系人
				apiHotelOrderReceiverUserService.createContact(new RequestBody<OrderReceiverUserVo>(orderReceiverUserVo));
			} catch (Throwable e) {
				logger.error("保存常用游客 错误！",e);
			}
			//保存常用游客
			//savePerson(buyInfo.getTravellers(), orderHandle.getReturnContent().getUserId());
		}catch(Exception e){
			logger.error("{}", e);
		}
		
		OrdOrderItem orderItem=order.getMainOrderItem();
		
		List<OrdOrderHotelTimeRate> orderHotelTimeRateList=orderItem.getOrderHotelTimeRateList();
		
		List<HotelTimeRateInfo> hotelTimeRateInfoList = handleHouseTimeRateInfo(orderHotelTimeRateList, orderItem);
		if(order.hasNeedPay()){
			/*if(!order.hasResourceAmple()){
				model.addAttribute("payStr", "提前预授权支付");
			}else{
				model.addAttribute("payStr", "届时到前台现付");
			}*/
			model.addAttribute("payStr", "酒店现付");
		}else{
			model.addAttribute("payStr", "网站预付");
		}
		//是否超时
		if(null!=orderItem.getLastCancelTime()&&new Date().compareTo(orderItem.getLastCancelTime())<0){
			model.addAttribute("isTimeOut", "true");
		}else{
			model.addAttribute("isTimeOut", "false");
		}
		model.addAttribute("deductAmountToYuan", getDeductAmountToYuan(order, orderItem));
		if(resultHandleT != null) {
			model.addAttribute("suppGoods", resultHandleT.getReturnContent());
		}
		model.addAttribute("hotelTimeRateInfoList", hotelTimeRateInfoList);
		model.addAttribute("orderItem", orderItem);
		model.addAttribute("order", order);
		model.addAttribute("weekStr",DateUtil.getZHDay(buyInfo.getItemList().get(0).getVisitTimeDate()));
		
		//判断目的地订单
        boolean isDestBuOrder = OrdOrderUtils.isDestBuFrontOrder(order);
//        boolean isDestBuOrder = isHotelDestBuFrontOrder(order);
        model.addAttribute("isDestBuOrder",isDestBuOrder);
        
		return "/order/verifyOrder";
	}
	
	/**
	 * 计算退改金额
	 * @return
	 */
	private String getDeductAmountToYuan(OrdOrder order,OrdOrderItem orderItem){
		
		if(null==order||null==orderItem||null==orderItem.getDeductAmount()){
			return "0";
		}
		if(null!=order.getOughtAmount()&&orderItem.getDeductAmount().longValue()>order.getOughtAmount()){
			return order.getOughtAmountYuan();
		}
		return PriceUtil.trans2YuanStr(orderItem.getDeductAmount());
	}

	@RequestMapping("/ord/book/queryRate.do")
	public String getTimePrice(String visitTime, String leaveTime,
			long suppGoods, int quantity, ModelMap model) {
		Date beginDate = DateUtil.stringToDate(visitTime, DATE_FORMAT);
		Date endDate = DateUtils.addDays(
				DateUtil.stringToDate(leaveTime, DATE_FORMAT), -1);// 离开日期不能算入住,所以减一天
		ResultHandleT<List<TimePrice>> resultHandleT = distGoodsTimePriceClientServiceAdaptor
				.findTimePriceList(Constant.DIST_BACK_END, suppGoods,
						beginDate, endDate);
		List<TimePrice> timePriceList = resultHandleT.getReturnContent();
		
		model.addAttribute("timePriceList", calcSeq(timePriceList,beginDate,endDate));
		return "/order/hotel_time_price";
	}
	
	
	@RequestMapping("/ord/book/queryPromPromotion.do")
	@ResponseBody
	public Object queryPromPromotion(@ModelAttribute("orderForm")BuyInfo form){
		BuyInfo buyInfo=converForm(form);
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	//得到商品促销信息
			 	attributes.put("buyInfoPromotion", loadGoodsPromInfo(buyInfo));
			 	msg.setAttributes(attributes);
				msg.setCode("success");
		} catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
		}
		 return msg;
	}
	
	@RequestMapping("/ord/book/checkNeedCvv.do")
	@ResponseBody
	public Object checkNeedCvv(String card)throws Exception{
		ResultMessage msg = ResultMessage.createResultMessage();
		com.lvmama.dest.dock.response.ResponseBody<ResponseCreditCardValidate> result = apiCreditCardValidate.creditCardValidate(card);
		if(result.isFailure()){
			msg.raise(result.getErrorMessage());
		}else{
			msg.addObject("valid", result.getT().isValid());
			msg.addObject("needCvv", result.getT().isNeedVerifyCode());
		}
		return msg;
	}
	
	/**
	 * 得到商品促销信息
	 * @param buyInfo
	 */
	private BuyInfoPromotion loadGoodsPromInfo(BuyInfo buyInfo){
		
		BuyInfoPromotion buyInfoPromotion= new BuyInfoPromotion();
		
		List<BuyInfoPromotion.Item> goodsItems=new ArrayList<BuyInfoPromotion.Item>();
		
		for (BuyInfo.Item item : buyInfo.getItemList()) {
			BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
			Long goods=item.getGoodsId();
			Date beginDate = null;
			Date endDate = null;
			HotelAdditation hotelAdditation = item.getHotelAdditation();
			beginDate =DateUtil.toDate(item.getVisitTime(), "yyyy-MM-dd");// 到访时间
			int quantity = item.getQuantity();// 购买商品数量

			if (hotelAdditation != null) {// 酒店情况的时候
				String endDateStr = hotelAdditation.getLeaveTime();
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(DateUtil.toDate(endDateStr, "yyyy-MM-dd"));
				endDate = endCal.getTime();
			}
			ResultHandleT<SuppGoods> resultHandleT = null;
			try {
				resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, goods);
			} catch (Exception e) {
				logger.error("{}", e);
			}
			if(resultHandleT != null) {
				SuppGoods suppGoods = resultHandleT.getReturnContent();
				promItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
				promItem.setPayTarget(suppGoods.getPayTarget());
			}
			promItem.setGoodsId(goods);
			promItem.setVisitTime(beginDate);
			promItem.setLeaveTime(endDate);
			promItem.setQuantity(quantity);
			goodsItems.add(promItem);
			buyInfoPromotion.setGoodsItems(goodsItems);
		}
		 ResultHandleT<BuyInfoPromotion> result=promotionService.calcPromotion(buyInfoPromotion);
		 return result.getReturnContent();
	}
	
	/**
	 * 转换页面上提交的表单，页面上以map的方式提交数据
	 * @param info
	 * @return
	 *//*
	BuyInfo converForm(BuyInfo info){
		info.setItemList(new ArrayList(info.getItemMap().values()));
		info.getItemMap().clear();
		info.setDistributionId(Constant.DIST_BACK_END);
		info.getItemList().get(0).setMainItem("true");
		return info;
	}*/
	
	/**
	 * 
	 * @param timePriceList
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private List<TimePrice> calcSeq(List<TimePrice> timePriceList,Date beginDate,Date endDate){
		Map<Date,TimePrice> map = new HashMap<Date,TimePrice>();
		if(CollectionUtils.isNotEmpty(timePriceList)){
			for(TimePrice tp:timePriceList){
				map.put(tp.getSpecDate(), tp);
			}
		}
		
		List<TimePrice> result = new ArrayList<TimePrice>();
		for(Date t=beginDate;!t.after(endDate);){
			if(map.containsKey(t)){
				result.add(map.get(t));
			}else{
				TimePrice tp = new TimePrice();
				tp.setSpecDate(t);
				tp.setNullTimePrice(true);
				result.add(tp);
			}
			t=DateUtils.addDays(t, 1);
		}
		return result;
	}
	
	/**
	 * 取产品公告
	 * @param productId
	 * @return
	 */
	private List<ProdProductNotice> getProductNoticeList(Long productId,String startDate,String endDate){
		Map<String, Object> paramProductNotice = new HashMap<String, Object>();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");	
		try {
			Date start = sFormat.parse(startDate);
			Date end = sFormat.parse(endDate);
			paramProductNotice.put("startTime",start);
			paramProductNotice.put("endTime",end);
		} catch (ParseException e) {
			logger.error("{}", e);
		}
		paramProductNotice.put("productId", productId);
		paramProductNotice.put("cancelFlag","Y" );
		paramProductNotice.put("_orderby", "CREATE_TIME asc");
		
		ResultHandleT<List<ProdProductNotice>> resultHandleT = prodProductNoticeClientService.findProductNoticeList(paramProductNotice);
		List<ProdProductNotice> productNoticeList=null;
		if(resultHandleT.isSuccess()){
			productNoticeList=resultHandleT.getReturnContent();
		}
		return productNoticeList;
	}
	/**
	 * 检查form参数必填项是否满足
	 * @param buyInfo
	 */
	@Override
	 protected void checkBuyInfo(BuyInfo buyInfo){
		Assert.notNull(buyInfo.getContact(),"联系人信息为空");
		if(CollectionUtils.isNotEmpty(buyInfo.getTravellers())){
			for(Iterator<Person> it=buyInfo.getTravellers().iterator();it.hasNext();){
				Person person = it.next();
				if(!person.isNotEmpty()){
					it.remove();
				}
			}
		}
		BuyInfo.Item mainItem = buyInfo.getItemList().get(0);
		if(buyInfo.getTravellers().size()<mainItem.getQuantity()){
			throw new IllegalArgumentException("每个房间至少有一个入住人");
		}
		
		if(StringUtils.isEmpty(buyInfo.getContact().getMobile())){
			throw new IllegalArgumentException("手机号不可以为空");
		}
		
		if(null!=buyInfo.getGuarantee()&&!StringUtils.isEmpty(buyInfo.getGuarantee().getCardNo())&&null!=buyInfo.getGuarantee().getExpirationYear()&&null!=buyInfo.getGuarantee().getExpirationMonth()){
			String now=DateUtil.formatDate(new Date(), "yyyyMM");
			Long year=buyInfo.getGuarantee().getExpirationYear();
			Long month=buyInfo.getGuarantee().getExpirationMonth();
			String expirationDate=year.toString()+(month>9?month.toString():"0"+month.toString());
			if(Long.valueOf(expirationDate)<Long.valueOf(now)){
				throw new IllegalArgumentException("信用卡有效期，不能小于当前日期");
			}
		}
		
		if(!StringUtils.isEmpty(buyInfo.getRemark())){
			List<String> excludeList=new ArrayList<String>();
			excludeList.add("<");
			excludeList.add(">");
			excludeList.add("&");
			excludeList.add("\\");
			excludeList.add("\"");
			excludeList.add(";");
			excludeList.add("\\\\");
			excludeList.add(":");
			excludeList.add("=");
			excludeList.add("'");
			for (int i = 0; i < excludeList.size(); i++) {
				if(buyInfo.getRemark().contains(excludeList.get(i))){
					throw new IllegalArgumentException("补充说明不能包含特殊字符 。如：< > & \\ \" \' ; \\\\ : = ");
				}
			}
		}
	}

	/**
	 * 检查form参数必填项是否满足
	 * @param buyInfo
	 */
	protected void checkHotelBuyInfo(HotelBuyInfoVo buyInfo){
		Assert.notNull(buyInfo.getContact(),"联系人信息为空");
		if(CollectionUtils.isNotEmpty(buyInfo.getTravellers())){
			for(Iterator<OrdPersonVo> it=buyInfo.getTravellers().iterator();it.hasNext();){
				OrdPersonVo person = it.next();
				if (!(StringUtils.isNotEmpty(person.getFullName()) || StringUtils.isNotEmpty(person.getLastName()) || StringUtils.isNotEmpty(person.getFirstName()))) {
					it.remove();
				}
			}
		}
		HotelItem mainItem = buyInfo.getItemList().get(0);
		if(buyInfo.getTravellers().size()<mainItem.getQuantity()){
			throw new IllegalArgumentException("每个房间至少有一个入住人");
		}
		
		if(StringUtils.isEmpty(buyInfo.getContact().getMobile())){
			throw new IllegalArgumentException("手机号不可以为空");
		}
		
		if(null!=buyInfo.getGuarantee()&&!StringUtils.isEmpty(buyInfo.getGuarantee().getCardNo())&&null!=buyInfo.getGuarantee().getExpirationYear()&&null!=buyInfo.getGuarantee().getExpirationMonth()){
			String now=DateUtil.formatDate(new Date(), "yyyyMM");
			Long year=buyInfo.getGuarantee().getExpirationYear();
			Long month=buyInfo.getGuarantee().getExpirationMonth();
			String expirationDate=year.toString()+(month>9?month.toString():"0"+month.toString());
			if(Long.valueOf(expirationDate)<Long.valueOf(now)){
				throw new IllegalArgumentException("信用卡有效期，不能小于当前日期");
			}
		}
		
		if(!StringUtils.isEmpty(buyInfo.getRemark())){
			List<String> excludeList=new ArrayList<String>();
			excludeList.add("<");
			excludeList.add(">");
			excludeList.add("&");
			excludeList.add("\\");
			excludeList.add("\"");
			excludeList.add(";");
			excludeList.add("\\\\");
			excludeList.add(":");
			excludeList.add("=");
			excludeList.add("'");
			for (int i = 0; i < excludeList.size(); i++) {
				if(buyInfo.getRemark().contains(excludeList.get(i))){
					throw new IllegalArgumentException("补充说明不能包含特殊字符 。如：< > & \\ \" \' ; \\\\ : = ");
				}
			}
		}
	}
	
	/**
	 * 保存为常用游客
	 * @param travellers
	 * @param userId
	 */
	private void savePerson(List<Person> travellers,String userId){
		//过滤掉已经是常用游客的数据
		for (int i = 0; i < travellers.size(); i++) {
			if(!"".equals(travellers.get(i).getReceiverId())){
				travellers.remove(i);
				i--;
			}
		}
		orderService.createContact(travellers, userId);
	}
	
	/**
	 * 预定天数
	 * @param buyInfo
	 */
	private long stayCount(BuyInfo buyInfo){
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");
		String visitTime=sFormat.format(buyInfo.getItemList().get(0).getVisitTimeDate());
		String leaveTime=sFormat.format(buyInfo.getItemList().get(0).getHotelAdditation().getLeaveTimeDate());
		return Long.valueOf(leaveTime).intValue()-Long.valueOf(visitTime).intValue();
	}
	/**
	 * 
	 * @param buyInfo
	 *//*
	private void initBooker(BuyInfo buyInfo){
		UserUser user = userUserProxyAdapter.getUserUserByUserNo(buyInfo.getUserId());
		Person person = new Person();
		person.setFullName(user.getUserName());
		person.setMobile(user.getMobileNumber());
		buyInfo.setUserNo(user.getId());
		buyInfo.setBooker(person);
	}*/
	
	private List<HotelTimeRateInfo>  handleHouseTimeRateInfo(
			List<OrdOrderHotelTimeRate> orderHotelTimeRateList,OrdOrderItem orderItem) {
		List<ArrayList> settleList=new ArrayList<ArrayList>();
		ArrayList<OrdOrderHotelTimeRate> hotelTimeTateList=null;
		for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
			
			if (settleList.size()==0) {
				hotelTimeTateList=new ArrayList();
				hotelTimeTateList.add(orderHotelTimeRate);
				settleList.add(hotelTimeTateList);
			}else{
				OrdOrderHotelTimeRate preOrderHotelTimeRate=hotelTimeTateList.get(hotelTimeTateList.size()-1);
				Long prePrice=preOrderHotelTimeRate.getPrice();
				Long price=orderHotelTimeRate.getPrice();
				
				Long preBreakfastTicket= preOrderHotelTimeRate.getBreakfastTicket();
				Long breakfastTicket= orderHotelTimeRate.getBreakfastTicket();
				if (!prePrice.equals(price) || !preBreakfastTicket.equals(breakfastTicket)) {
					hotelTimeTateList=new ArrayList();
					hotelTimeTateList.add(orderHotelTimeRate);
					settleList.add(hotelTimeTateList);
					
				}else{
					hotelTimeTateList.add(orderHotelTimeRate);
				}
			}
		}
		
		
		ResultHandleT<SuppGoodsTimePrice> resultHandleT=suppGoodsTimePriceClientService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), false);
		
		SuppGoodsTimePrice suppGoodsTimePrice=null;
		if(resultHandleT.isSuccess()){
			suppGoodsTimePrice=resultHandleT.getReturnContent();
		}
		
		String lastTime="";
		String guaranteeTime="";
		if (suppGoodsTimePrice!=null) {
			if (suppGoodsTimePrice.getAheadBookTime()!=null) {
				//最晚预定时间
				Date time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getAheadBookTime());
				lastTime=CalendarUtils.getDateFormatString(time, CalendarUtils.YYYY_MM_DD_HH_MM_PATTERN);
			}
			
			if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name()
					.equals(suppGoodsTimePrice.getBookLimitType())
					&& suppGoodsTimePrice.getLatestUnguarTime() != null
					&&suppGoodsTimePrice.getLatestUnguarTime().longValue()>0) {
				// 担保时间
				guaranteeTime=suppGoodsTimePrice.getLatestUnguarTime()+"";
			}
		}
		
		
		List<HotelTimeRateInfo> resultList=new ArrayList<HotelTimeRateInfo>();
		for (ArrayList settleObjList : settleList) {
			
			OrdOrderHotelTimeRate orderHotelTimeRateFirst=(OrdOrderHotelTimeRate)settleObjList.get(0);
			OrdOrderHotelTimeRate orderHotelTimeRateLast=(OrdOrderHotelTimeRate)settleObjList.get(settleObjList.size()-1);
			
			HotelTimeRateInfo hotelTimeRateInfo=new HotelTimeRateInfo();
			hotelTimeRateInfo.setHouseType("");//房型暂时显示为商品名称
			hotelTimeRateInfo.setStartDate(orderHotelTimeRateFirst.getVisitTime());
			hotelTimeRateInfo.setEndDate(DateUtils.addDays(orderHotelTimeRateLast.getVisitTime(), 1));
			hotelTimeRateInfo.setHousePrice(orderHotelTimeRateFirst.getPrice());
			hotelTimeRateInfo.setBreakfastTicket(orderHotelTimeRateFirst.getBreakfastTicket());
			
			if (resultList.size()==0) {
				hotelTimeRateInfo.setGuaranteeTime(guaranteeTime);
				hotelTimeRateInfo.setLastTime(lastTime);
			}else{
				hotelTimeRateInfo.setGuaranteeTime("");
				hotelTimeRateInfo.setLastTime("");
			}
			
			resultList.add(hotelTimeRateInfo);
			
		}
		
		return resultList;
	}

	private List<HotelTimeRateInfo>  handleHouseTimeRateHotelInfo(
			List<OrdOrderHotelTimeRateVo> orderHotelTimeRateList, OrdOrderItemVo orderItem) {
		List<ArrayList> settleList=new ArrayList<ArrayList>();
		ArrayList<OrdOrderHotelTimeRateVo> hotelTimeTateList=null;
		for (OrdOrderHotelTimeRateVo orderHotelTimeRate : orderHotelTimeRateList) {
			
			if (settleList.size()==0) {
				hotelTimeTateList=new ArrayList();
				hotelTimeTateList.add(orderHotelTimeRate);
				settleList.add(hotelTimeTateList);
			}else{
				OrdOrderHotelTimeRateVo preOrderHotelTimeRate=hotelTimeTateList.get(hotelTimeTateList.size()-1);
				Long prePrice=preOrderHotelTimeRate.getPrice();
				Long price=orderHotelTimeRate.getPrice();
				
				Long preBreakfastTicket= preOrderHotelTimeRate.getBreakfastTicket();
				Long breakfastTicket= orderHotelTimeRate.getBreakfastTicket();
				if (!prePrice.equals(price) || !preBreakfastTicket.equals(breakfastTicket)) {
					hotelTimeTateList=new ArrayList();
					hotelTimeTateList.add(orderHotelTimeRate);
					settleList.add(hotelTimeTateList);
					
				}else{
					hotelTimeTateList.add(orderHotelTimeRate);
				}
			}
		}
		
		
		ResultHandleT<SuppGoodsTimePrice> resultHandleT=suppGoodsTimePriceClientService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), false);
		
		SuppGoodsTimePrice suppGoodsTimePrice=null;
		if(resultHandleT.isSuccess()){
			suppGoodsTimePrice=resultHandleT.getReturnContent();
		}
		
		String lastTime="";
		String guaranteeTime="";
		if (suppGoodsTimePrice!=null) {
			if (suppGoodsTimePrice.getAheadBookTime()!=null) {
				//最晚预定时间
				Date time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getAheadBookTime());
				lastTime=CalendarUtils.getDateFormatString(time, CalendarUtils.YYYY_MM_DD_HH_MM_PATTERN);
			}
			
			if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name()
					.equals(suppGoodsTimePrice.getBookLimitType())
					&& suppGoodsTimePrice.getLatestUnguarTime() != null
					&&suppGoodsTimePrice.getLatestUnguarTime().longValue()>0) {
				// 担保时间
				guaranteeTime=suppGoodsTimePrice.getLatestUnguarTime()+"";
			}
		}
		
		
		List<HotelTimeRateInfo> resultList=new ArrayList<HotelTimeRateInfo>();
		for (ArrayList settleObjList : settleList) {
			
			OrdOrderHotelTimeRateVo orderHotelTimeRateFirst=(OrdOrderHotelTimeRateVo)settleObjList.get(0);
			OrdOrderHotelTimeRateVo orderHotelTimeRateLast=(OrdOrderHotelTimeRateVo)settleObjList.get(settleObjList.size()-1);
			
			HotelTimeRateInfo hotelTimeRateInfo=new HotelTimeRateInfo();
			hotelTimeRateInfo.setHouseType("");//房型暂时显示为商品名称
			hotelTimeRateInfo.setStartDate(orderHotelTimeRateFirst.getVisitTime());
			hotelTimeRateInfo.setEndDate(DateUtils.addDays(orderHotelTimeRateLast.getVisitTime(), 1));
			hotelTimeRateInfo.setHousePrice(orderHotelTimeRateFirst.getPrice());
			hotelTimeRateInfo.setBreakfastTicket(orderHotelTimeRateFirst.getBreakfastTicket());
			
			if (resultList.size()==0) {
				hotelTimeRateInfo.setGuaranteeTime(guaranteeTime);
				hotelTimeRateInfo.setLastTime(lastTime);
			}else{
				hotelTimeRateInfo.setGuaranteeTime("");
				hotelTimeRateInfo.setLastTime("");
			}
			
			resultList.add(hotelTimeRateInfo);
			
		}
		
		return resultList;
	}
	
	/**
	 * 
	 * @param visitTime
	 */
	private boolean isToday(String visitTime){
		Date visitDate=DateUtil.stringToDate(visitTime, "yyyy-MM-dd");
		visitTime=DateUtil.formatDate(visitDate, "yyyyMMdd");
		String now=DateUtil.formatDate(new Date(), "yyyyMMdd");
		return now.equals(visitTime)?true:false;
			
	}
	
	/**
	 * 
	 * @return
	 */
	private String getCurrentTime(){
		Calendar calendar = Calendar.getInstance();
		Date now=new Date();
		calendar.setTime(now);
		int minutes = calendar.get(Calendar.MINUTE);
		 if(minutes>=0&&minutes<=30){
			 calendar.set(Calendar.MINUTE, 60);
		 }else if(minutes>30&&minutes<=59){
			 calendar.set(Calendar.MINUTE, 90);
		 }
		return DateUtil.formatDate(calendar.getTime(), "HH:mm");
	}
	
	private String getEarliestArriveTime(String visitTime,SuppGoods suppGoods){
		String time=getCurrentTime();
		String earliestArriveTime=(String) suppGoods.getProdProduct().getPropValue().get("earliest_arrive_time");
		if(isToday(visitTime)){
			if(time.compareTo(earliestArriveTime)>0){
				return time;
			}
			return earliestArriveTime;
		}
		
		return (String) suppGoods.getProdProduct().getPropValue().get("earliest_arrive_time");
	}

//	/**
//	 * 目的地BU预付的自由行、酒店套餐，或单酒店,且是前台,后台下单
//	 * 
//	 * @param order
//	 * @return
//	 */
//	public static boolean isHotelDestBuFrontOrder(final OrdOrderVo order){
//		if(isBusHotelOrder(order)){
//			return false;
//		}
//		
//		Long distributionChannel = order.getDistributionChannel();
//		if(distributionChannel == null){
//			distributionChannel = 0L;
//		}
//		//分销商渠道ID
//		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
//		if(Constant.DIST_FRONT_END == order.getDistributorId()
//				||Constant.DIST_BACK_END == order.getDistributorId()
//				||ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, distributionChannel.longValue())){
//			//目的地BU,国内BU 品类
//			
//			if(order.hasNeedPrepaid()
//					&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode())
//					&& (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()
//					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == order.getCategoryId()
//					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
//					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==order.getCategoryId())
//					){
//				return true;
//			}
//		}
//		return false;
//		
//	}

//	public static boolean isBusHotelOrder(OrdOrderVo order) {
//		if((order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode())
//				||order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.LOCAL_BU.getCode()))
//				&&BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
//				&&BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().equals(order.getSubCategoryId())){
//			return true;
//		}
//		return false;
//	}
}
