package com.lvmama.vst.order.web;

import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import com.lvmama.vst.comm.utils.DateUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lvmama.comm.search.vst.vo.HotelSearchFilterBean;
import com.lvmama.comm.search.vst.vo.HotelSearchFilterBean.HOTEL_STAR;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.vst.goods.po.HotelGoodsVstPo;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.searchclient.bean.HotelComprehensiveResponse;
import com.lvmama.searchclient.enums.Sort;
import com.lvmama.searchclient.search.request.enums.CommonSearchConstants;
import com.lvmama.searchclient.search.request.field.HotelField;
import com.lvmama.searchclient.search.request.keyword.HotelKeywordSearchRequest;
import com.lvmama.searchclient.search.request.keyword.HotelKeywordSearchRequest.HotelKeywordSearchRequestBuilder;
import com.lvmama.searchclient.search.response.LvmamaSearchResponse;
import com.lvmama.searchclient.search.service.KeywordSearchService;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.vo.OrderRecommendHotel;

@Controller
public class OrderHotelRecommendAction {
	private static final Log LOG = LogFactory.getLog(OrderHotelRecommendAction.class);
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 6; 
	@Autowired
	private KeywordSearchService keywordSearchService;
	@Autowired
	private IComplexQueryService complexQueryService;
	@Autowired
	private IHotelGoodsQueryVstApiService hotelGoodsQueryVstApiService;
	
	@RequestMapping(value="/ord/order/newOrderRecommendHotelResult")
	public String newOrderRecommendHotelResult(Model model, Integer page, HttpServletRequest req) {
		initQueryForm(model, req);
		String startDate = req.getParameter("startDate");//入住时间
		String endDate = req.getParameter("endDate");//离店时间
		String mapType = req.getParameter("mapType");//地图类型
		String baiduGeo = req.getParameter("baiduGeo");//经纬度
		String starId = req.getParameter("starId");//星级
		String suppGoodsIdStr = req.getParameter("suppGoodsId");//商品ID
		Integer currentPage = page == null ? 1 : page;
		int sortType = req.getParameter("sortType") == null ? Sort.PRODUCT_NUM.getSortId() : NumberUtils.toInt(req.getParameter("sortType"));
		//单酒店的商品ID是一样的
		Long suppGoodsId = NumberUtils.toLong(suppGoodsIdStr);
		HotelGoodsVstPo hotelGoodsVstPo = new HotelGoodsVstPo();
		hotelGoodsVstPo.setSuppGoodsId(suppGoodsId);
		hotelGoodsVstPo.setHasProp(true);
	    hotelGoodsVstPo.setHasPropValue(true);
		RequestBody<HotelGoodsVstPo> requestBody = 
				new RequestBody<HotelGoodsVstPo>();
		requestBody.setT(hotelGoodsVstPo);
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		Long districtId = null;
		try {
			ResponseBody<HotelGoodsVstVo> responseBody = 
					hotelGoodsQueryVstApiService.findSuppGoodsByParam(requestBody);
			HotelGoodsVstVo hotelGoodsVstVo = responseBody.getT();
			districtId = hotelGoodsVstVo.getProdProduct().getBizDistrict().getDistrictId();
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		/**
		 * 针对如果查询的日期的入住时间早于当天，则讲入住时间改为明天，离店时间改为后天
		 */
		if(checkStartDateBeforeNow(startDate)) {
			Date date = new Date();
			startDate = DateUtil.getFormatDate(DateUtil.addDays(date, 1), "yyyy-MM-dd");
			endDate = DateUtil.getFormatDate(DateUtil.addDays(date, 2), "yyyy-MM-dd");
		}
		OrderRecommendHotel orderRecommendHotel = new OrderRecommendHotel();
		orderRecommendHotel.setStartDate(startDate);
		orderRecommendHotel.setEndDate(endDate);
		orderRecommendHotel.setMapType(mapType);
		orderRecommendHotel.setStarId(starId);
		orderRecommendHotel.setSortType(sortType);
		orderRecommendHotel.setBaiduGeo(baiduGeo);
		orderRecommendHotel.setDistrictId(districtId);
		orderRecommendHotel.setSuppGoodsId(suppGoodsIdStr);
		model.addAttribute("orderRecommendHotel", orderRecommendHotel);
		return showOrderHotelRecommendResult(model, orderRecommendHotel, currentPage, DEFAULT_PAGE_SIZE, req);
	}
	
	@RequestMapping(value="/ord/order/showOrderHotelRecommendResult")
	public String showOrderHotelRecommendResult(Model model, OrderRecommendHotel orderRecommendHotel, Integer page,Integer pageSize, HttpServletRequest req){
		initQueryForm(model, req);
		Integer currentPageSize = pageSize == null? DEFAULT_PAGE_SIZE : pageSize;
		Integer currentPage = page == null ? 1 : page;
		LOG.info("酒店推荐OrderRecommendHotel请求参数======" + JSONObject.toJSONString(orderRecommendHotel));
		if(!checkInvalid(orderRecommendHotel)) {
			LOG.info("酒店推荐查询参数缺失，不能查询");
	    	model.addAttribute("orderRecommendHotel", orderRecommendHotel);
			return "/order/recommend/showOrderHotelRecommendResult";
		}
		//获取酒店对应的星级信息
		Long id = NumberUtils.toLong(orderRecommendHotel.getStarId());
		HOTEL_STAR hotelStar = HotelSearchFilterBean.HOTEL_STAR.getHotelStarById(id);
		orderRecommendHotel.setStarId(Long.toString(hotelStar.getStarId()));
		List<HotelComprehensiveResponse> hotelList = null;
		Long totalCount = 0L;
		//目前酒店推荐只根据百度地图的经纬度做 推荐，谷歌地图不考虑
		//百度地图--30006;谷歌地图--30007
		if (ObjectUtils.equals(orderRecommendHotel.getMapType(), "30006")) {
			try {
				HotelKeywordSearchRequestBuilder builder = HotelKeywordSearchRequest.getRequestBuilder("", CommonSearchConstants.RequestSource.pc);	
				//酒店星级，酒店图片，酒店设施，酒店起价，酒店名称
				builder.addFields(HotelField.PRODUCT_ID,HotelField.PRODUCT_NAME,HotelField.STAR_ID,HotelField.FACILITIES,
						HotelField.FILTER_FACILITIES,HotelField.STAR_DESC,HotelField.PHOTO_CONTENT,HotelField.DEST_ID,
						HotelField.PHOTO_URL,HotelField.SALE_PER,HotelField.SELL_PRICE);//需要的返回字段
		    	builder.startDate(orderRecommendHotel.getStartDateStr());
		    	builder.endDate(orderRecommendHotel.getEndDateStr());
		    	builder.addFilter(HotelField.STAR_ID, Long.toString(hotelStar.getStarId()),Long.toString(hotelStar.getBaseId()));
		    	builder.addRangeFilter(HotelField.SELL_PRICE, 0d, Double.MAX_VALUE, false, true);
		    	//latitude 纬度  longitude  经度
		    	builder.isSearchByLocation(true).latitude(orderRecommendHotel.getLatitude()).longitude(orderRecommendHotel.getLongitude()).distance(5D);
		    	//builder.addSort(Sort.PRODUCT_NUM);sort.product_num默认Sort.PRICE_DOWN
		    	builder.addSort(Sort.getSort(orderRecommendHotel.getSortType()));
		    	builder.pageNum(currentPage);
		    	builder.pageSize(currentPageSize);
		    	HotelKeywordSearchRequest request = builder.buildRequest();
		    	LOG.info("请求ES的request参数======" + JSONObject.toJSONString(request));
		    	LvmamaSearchResponse<HotelComprehensiveResponse> response = keywordSearchService.submitAndGet(request);
		    	hotelList = response.getItems();
		    	totalCount = (long) response.getTotalResultSize();
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
		}
		
    	// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(hotelList, currentPage, currentPageSize, totalCount, req);
		//设置当前页面显示数据大小
    	resultPage.setPageSize(currentPageSize);
    	// 存储分页结果
    	model.addAttribute("resultPage", resultPage);
    	model.addAttribute("orderRecommendHotel", orderRecommendHotel);
		return "/order/recommend/showOrderHotelRecommendResult";
	}
	private Boolean checkInvalid(OrderRecommendHotel orderRecommendHotel) {
		if(StringUtil.isNotEmptyString(String.valueOf(orderRecommendHotel.getSortType()))
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getLatitude())
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getBaiduGeo())
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getEndDate())
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getStarId()) 
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getLongitude())
				&& StringUtil.isNotEmptyString(orderRecommendHotel.getStartDate())){
			return true;
		}
		return false;
	}
	private Boolean checkStartDateBeforeNow(String date) {
		try {
			Date startDate = DateUtil.converDateFromStr4(date);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(startDate);
			Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, -10);
			return startCalendar.before(calendar);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	private Page buildResultPage(List list, Integer currentPage, Integer pageSize, Long totalCount, HttpServletRequest request) {
		// 如果当前页是空，默认为1
		Integer currentPageTmp = currentPage == null ? 1 : currentPage;
		// 从配置文件读取分页大小
		Integer defaultPageSize = DEFAULT_PAGE_SIZE;
		Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
		// 构造分页对象
		Page page = Page.page(totalCount, pageSizeTmp, currentPageTmp);
		// 构造分页URL
		page.buildUrl(request);
		// 设置结果集
		page.setItems(list);
		return page;
	}
	private void initQueryForm(Model model, HttpServletRequest req) {
		//星级筛选map
		Map<String, String> starMap = new LinkedHashMap<String, String>();
		starMap.put("109", "其他");
		starMap.put("106", "二星/简约");
		starMap.put("104", "三星/舒适");
		starMap.put("102", "四星/品质");
		starMap.put("100", "五星/豪华");
		model.addAttribute("starMap", starMap);
		//筛选Map
		Map<String, String> sortMap = new LinkedHashMap<String, String>();
		sortMap.put("0", "默认");
		sortMap.put("2", "价格从高到低");
		sortMap.put("3", "价格从低到高");
		sortMap.put("27", "好评优先");
		model.addAttribute("sortMap", sortMap);
		//分页Map
		Map<String, String> pageMap = new LinkedHashMap<String, String>();
		pageMap.put("6", "6");
		pageMap.put("12", "12");
		pageMap.put("18", "18");
		model.addAttribute("pageMap", pageMap);
	}
}
