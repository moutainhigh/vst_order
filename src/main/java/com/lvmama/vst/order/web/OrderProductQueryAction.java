package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.po.mark.MarkChannel;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizBranchProp;
import com.lvmama.vst.back.biz.po.BizCategoryProp;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.client.biz.service.BranchPropClientService;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.vo.CtripHotelPromVo;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.adaptor.hotel.OrderProductQueryServiceAdaptor;
import com.lvmama.vst.order.service.IOrderStockService;
import com.lvmama.vst.pet.adapter.MarkChannelServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;


/**
 * 后台下单-供驴妈妈客服给客户下单时使用
 * 
 * @author wenzhengtao
 * 
 */

@Controller
public class OrderProductQueryAction extends BaseActionSupport {
	/**
	 * 日志记录器,测试用
	 */
	private static final Log LOG = LogFactory.getLog(OrderProductQueryAction.class);

	/**
	 * 酒店产品商品查询适配器
	 * */
	@Autowired
	private OrderProductQueryServiceAdaptor orderProductQueryServiceAdaptor;
	
	/**
	 * 产品规格接口
	 */
	@Autowired
	private	CategoryPropClientService categoryPropClientService;
	
	/**
	 * 商品规格接口
	 */
	@Autowired
	private	BranchPropClientService branchPropClientService;
	
	/**
	 * 库存检查接口
	 */
	@Autowired
	private IOrderStockService orderStockService;
	
	/**
	 * 时间价格接口
	 */
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;

	/**
	 * 时间价格表适配器接口
	 * */
	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	/**
	 * 资源预控
	 */
	@Autowired
	private ResPreControlService resControlBudgetRemote;
	/**
	 * 产品公告接口
	 */
	@Autowired 
	private ProdProductNoticeClientService prodProductNoticeClientService ;

	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
    @Autowired
    private MarkChannelServiceAdapter markChannelServiceAdapter;

	/**
	 * 进入产品商品查询页面
	 * @author wenzhengtao 重构此查询方法
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/showOrderProductQueryList.do")
	public String showOrderProductQueryList(Model model, OrdOrderProductQueryVO ordOrderProductQueryVO,HttpServletRequest request) {	
		//初始化查询表单
		initQueryForm(model,request);
		UserUser user=null;
		try {
			//从cookie中读取用户信息
			user=readUserCookie();
			if(user==null|| StringUtil.isEmptyString(user.getUserId())){
				String userId = request.getParameter("userId");
				if(StringUtil.isNotEmptyString(userId)){
					user=userUserProxyAdapter.getUserUserByUserNo(userId);
					if(user==null){
						LOG.error(" userUserProxyAdapter.getUserUserByUserNo("+userId+") failed , user is null ");
					}
				}
			}
		}catch (Exception e) {
			LOG.error("get user failed , error msg is "+ e.getMessage());
		}
		model.addAttribute("user", user);	
		
		try{
			Map<String, Object> params = new HashMap<>();
			params.put("channelCode", "O2ONewRetail");
			// 一级门店渠道
			List<MarkChannel> firstMarkChannels = markChannelServiceAdapter.search(params);
			if (CollectionUtils.isNotEmpty(firstMarkChannels)) {
				List<MarkChannel> secondMarkChannels = new ArrayList<>();
				List<MarkChannel> markChannels = null;
				for (MarkChannel markChannel : firstMarkChannels) {
					if (null != markChannel.getChannelId()) {
						params.clear();
						params.put("fatherId", markChannel.getChannelId());
						params.put("valid", "Y");
						params.put("layer", 2);
						// 二级门店渠道
						markChannels = markChannelServiceAdapter.search(params);
						if (CollectionUtils.isNotEmpty(markChannels)) {
							secondMarkChannels.addAll(markChannels);
						}
					}
				}
				model.addAttribute("markChannels", secondMarkChannels);
			}
		}catch (Exception e){
			LOG.error("markChannelServiceAdapter.search(),", e);
		}
		
		//跳转到后台下单页面
		return "/order/orderProductQuery/showOrderProductQueryList";
	}
	
	/**
	 * 查询产品商品列表
	 * @author wenzhengtao 重构此查询方法
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ord/productQuery/findOrderGoodsList.do")
	public String findOrderGoodsList(Model model, 
			Integer page, OrdOrderProductQueryVO ordOrderProductQueryVO, HttpServletRequest request) throws BusinessException{
		//开始时间戳
		long start = System.currentTimeMillis();
		
		//初始化查询表单
		initQueryForm(model,request);
		
		//构造查询条件
		HashMap<String,Object> paramOrderProductQuery = new HashMap<String,Object>();
		
		//入住日期
		paramOrderProductQuery.put("startDate", ordOrderProductQueryVO.getStartDate());
		
		//离店日期
		paramOrderProductQuery.put("endDate", DateUtil.formatDate(DateUtils.addDays(ordOrderProductQueryVO.getEndDateDate(),-1),"yyyy-MM-dd"));

		//入住天数
		if(ordOrderProductQueryVO.getDays()!=null){
			paramOrderProductQuery.put("days", ordOrderProductQueryVO.getDays()); 
		}
		
		//酒店ID
		if(ordOrderProductQueryVO.getProductId()!=null){
			paramOrderProductQuery.put("productId", ordOrderProductQueryVO.getProductId());
		}
		
		//支付方式
		if(ordOrderProductQueryVO.getPayTargets()!=null && ordOrderProductQueryVO.getPayTargets().length>0){
			paramOrderProductQuery.put("payTargets", ordOrderProductQueryVO.getPayTargets());
		}
		
		//开业标志
		if(ordOrderProductQueryVO.getEstablishmentDateFlag()){
			paramOrderProductQuery.put("establishmentDateFlag", ordOrderProductQueryVO.getEstablishmentDateFlag());
		}
		
		//装修标志
		if(ordOrderProductQueryVO.getRenovationDateFlag()){
			paramOrderProductQuery.put("renovationDateFlag", ordOrderProductQueryVO.getRenovationDateFlag());
		}
		
		//开业或装修的限制标志
		paramOrderProductQuery.put("renovationInnerDate", ordOrderProductQueryVO.getRenovationInnerDate());
		
		//房型要求，单选条件查询
		if(UtilityTool.isValid(ordOrderProductQueryVO.getBedType())){
			paramOrderProductQuery.put("bedType", ordOrderProductQueryVO.getBedType());
		}
		
		//入住城市ID
		if(ordOrderProductQueryVO.getDistrictId()!=null){
			paramOrderProductQuery.put("districtId", ordOrderProductQueryVO.getDistrictId());
		}
		
		//无需担保标志
		if(ordOrderProductQueryVO.isBookLimitTypeFlag()){
			paramOrderProductQuery.put("bookLimitTypeFlag", ordOrderProductQueryVO.isBookLimitTypeFlag());
		}
		
		//酒店星级
		if(StringUtils.isNotEmpty(ordOrderProductQueryVO.getStarRate())){
			String[] starRates = ordOrderProductQueryVO.getStarRate().split(",");
			ordOrderProductQueryVO.setStarRates(starRates);
		}
		
		//价格区间
		if(UtilityTool.isValid(ordOrderProductQueryVO.getPriceRange())){
			paramOrderProductQuery.put("priceRange", ordOrderProductQueryVO.getPriceRange());
		}
		
		//自定义价格区间
		if(ordOrderProductQueryVO.getPriceBegin()!=null){//开始
			paramOrderProductQuery.put("priceBegin", ordOrderProductQueryVO.getPriceBegin());
		}
		if(ordOrderProductQueryVO.getPriceEnd()!=null){//结束
			paramOrderProductQuery.put("priceEnd", ordOrderProductQueryVO.getPriceEnd());
		}
		
		//酒店的推荐级别
		if(null != ordOrderProductQueryVO.getRecommendLevel()){
			paramOrderProductQuery.put("recommendLevel", ordOrderProductQueryVO.getRecommendLevel());
		}
		
		//酒店设施列表
		if(ordOrderProductQueryVO.getFacilitieses()!=null && ordOrderProductQueryVO.getFacilitieses().length>0){
			paramOrderProductQuery.put("facilitieses", ordOrderProductQueryVO.getFacilitieses());
		}
		
		//驴妈妈后台分销
		paramOrderProductQuery.put("distributorId", Constant.DIST_BACK_END);
		
		//将VO保存，用于mybatis条件判断
		paramOrderProductQuery.put("ordOrderProductQueryVO", ordOrderProductQueryVO);
		
		filterParam(paramOrderProductQuery,ordOrderProductQueryVO);
		//统计满足条件的产品数
		int count = orderProductQueryServiceAdaptor.countOrderProductList(paramOrderProductQuery);
		if(LOG.isDebugEnabled()){
			LOG.debug("酒店产品统计完毕,总记录数为"+count);
		}
		
		//当有酒店产品的时候，走下面逻辑
		if(count>0){
			//设置分页参数
			int pagenum = page == null ? 1 : page;
			@SuppressWarnings("rawtypes")
			Page pageParam = Page.page(count, 10, pagenum);
			pageParam.buildJSONUrl(request,true);
			
			paramOrderProductQuery.put("_start", pageParam.getStartRows());
			paramOrderProductQuery.put("_end", pageParam.getEndRows());
			
			//分页查询酒店集合-产品集合
			List<OrdOrderProductVO> ordOrderProductList = orderProductQueryServiceAdaptor.findOrderProductVOList(paramOrderProductQuery);
			if(LOG.isDebugEnabled()){
				LOG.debug("酒店产品集合查询完毕!");
			}
			
			//得到当前页所有的产品ID，根据产品ID查询产品下的商品
			StringBuffer productIdsTemp = new StringBuffer();
			if(!CollectionUtils.isEmpty(ordOrderProductList)){
				for(OrdOrderProductVO orderProduct:ordOrderProductList){
					productIdsTemp.append(orderProduct.getProductId());
					productIdsTemp.append(",");
				}
			}
			productIdsTemp.setLength(productIdsTemp.length()-1);//去掉最后一个逗号
			String[] productIds = productIdsTemp.toString().split(",");
			
			//设置查询商品的产品ID数组
			ordOrderProductQueryVO.setProductIds(productIds);
			
			//设置在参数Map里
			paramOrderProductQuery.put("ordOrderProductQueryVO", ordOrderProductQueryVO);
			
			//查询酒店下的房间--商品集合
			List<OrdOrderGoodsVO> ordOrderGoodsList = orderProductQueryServiceAdaptor.findOrderGoodsVOList(paramOrderProductQuery);
			
			//判断这些商品中有没有买断价
			if(ordOrderGoodsList!=null && ordOrderGoodsList.size()>0)
			for(int x=0,y=ordOrderGoodsList.size();x<y;x++){
				OrdOrderGoodsVO vo = ordOrderGoodsList.get(x);
				/** 开始资源预控买断价格  **/
				List<ResPreControlTimePriceVO> resPriceList =null;
				Long precontrolSalePrice = null;
				Long goodsId = vo.getSuppGoodsId();
				Date visitDate = vo.getSpceDate();
				//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
				GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
				//如果能找到该有效预控的资源
				boolean hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
				
				if(hasControled ){
					// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
					resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(visitDate,1L, goodsId);
					if(CollectionUtils.isEmpty(resPriceList)){
						hasControled = false;
					}else{
						LOG.info("***资源预控***");
						LOG.info("单酒店后台：" + goodsId + "存在预控资源");
						for(int m=0,n=resPriceList.size();m<n;m++){
							ResPreControlTimePrice restimePrice = resPriceList.get(m);
							//销售价
							if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(restimePrice.getPriceClassificationCode())){
								precontrolSalePrice = restimePrice.getValue();
							}
						}
					}
					if(hasControled == false){
						precontrolSalePrice = null;
					}
					if(precontrolSalePrice!=null){
						vo.setPrice(precontrolSalePrice.intValue());
					}
					
					
				}
				/** end **/
			}
			
			if(LOG.isDebugEnabled()){
				LOG.debug("酒店房间集合查询完毕!");
			}
			
			//构建某一页的产品，商品查询结果
			List<OrdOrderProductVO> ordOrderProductVOList = this.buildProductAndGoodsList(ordOrderProductList,ordOrderGoodsList,request);
			
			//结果集二次处理
			for(OrdOrderProductVO orderProductVO:ordOrderProductVOList){
				//检查酒店是否有重要通知,也就是酒店公告
				List<ProdProductNotice> prodProductNoticeList = this.getProductNoticeByCondition(
						orderProductVO.getProductId(),ordOrderProductQueryVO.getStartDateDate(),ordOrderProductQueryVO.getEndDateDate());
				if(CollectionUtils.isNotEmpty(prodProductNoticeList)){
					orderProductVO.setHasNotice("Y");
				}else{
					orderProductVO.setHasNotice("N");
				}
				
				//检查房间本地库存,远程供应商的不查
				List<OrdOrderGoodsVO> orderGoodsVOList = orderProductVO.getOrdOrderGoodsVOList();
				if(CollectionUtils.isNotEmpty(orderGoodsVOList)){
					for(OrdOrderGoodsVO goodsVO:orderGoodsVOList){
						//初始化参数
						BuyInfo.Item item = new BuyInfo.Item();
						//商品ID
						item.setGoodsId(goodsVO.getSuppGoodsId());
						//入住日期
						item.setVisitTime(ordOrderProductQueryVO.getStartDate());
						//商品数量
						item.setQuantity(1);
						BuyInfo.HotelAdditation hotelAdditation = new BuyInfo.HotelAdditation();
						//离店日期
						hotelAdditation.setLeaveTime(ordOrderProductQueryVO.getEndDate());
						item.setHotelAdditation(hotelAdditation);
						//检查和设置库存标志位
						ResultHandleT<Boolean> resultHandleT = orderStockService.checkStock(Constant.DIST_BACK_END, item, false);
						boolean isHasStock = resultHandleT != null && resultHandleT.isSuccess() && resultHandleT.getReturnContent();
						if(isHasStock){
							goodsVO.setIsHasStock("Y");
						}else{
							goodsVO.setIsHasStock("N");
						}
						
						//获取担保类型和扣款类型
						Date beginDate = DateUtil.toDate(ordOrderProductQueryVO.getStartDate(), "yyyy-MM-dd"); //开始入住时间
						Date endDate = DateUtils.addDays(ordOrderProductQueryVO.getEndDateDate(), -1); //离店时间，注意要-1哦
						ResultHandleT<String> guaranteeRuleResultHandleT = distGoodsTimePriceClientServiceAdaptor.generateGuaranteeRuleStr(Constant.DIST_BACK_END, goodsVO.getSuppGoodsId(), beginDate, endDate);
						if(guaranteeRuleResultHandleT == null){
							log.warn("Can't get guarantee rule, guaranteeRuleResultHandleT is null");
						} else if(guaranteeRuleResultHandleT.isFail()){
							log.warn("generate guarantee rule failed, msg is " + guaranteeRuleResultHandleT.getInfoMsg());
						} else {
							String guaranteeRule = guaranteeRuleResultHandleT.getReturnContent();
							goodsVO.setGuarRuleStr(guaranteeRule);

							//获取宽带名称
							goodsVO.setStrInternet(this.getInternetName(goodsVO.getInternet(), request));

							//获取床型名称
							goodsVO.setStrBedType(this.getBedTypeName(goodsVO.getBedType(), request));
						}
						
					}
				}
			}
			
			// 携程酒店商品促销map，key是商品Id，value是商品促销集合的json序列化
			Map<String, String> ctripPromoJsonMap = new HashMap<String, String>();
			if (CollectionUtils.isNotEmpty(ordOrderProductVOList)) {
				// 遍历产品集合
				for (OrdOrderProductVO ordOrderProductVO : ordOrderProductVOList) {
					// 判断商品集合是否为空
					if (ordOrderProductVO != null && CollectionUtils.isNotEmpty(ordOrderProductVO.getOrdOrderGoodsVOList())) {
						// 遍历商品集合
						for (OrdOrderGoodsVO ordOrderGoodsVO : ordOrderProductVO.getOrdOrderGoodsVOList()) {
							// 酒店商品携程促销集合
							List<CtripHotelPromVo> crtipHotelPromList = ordOrderGoodsVO.getCtripHotelPromVoList();
							if (CollectionUtils.isNotEmpty(crtipHotelPromList)) {
								// 携程酒店促销对象集合序列化json
								String ctripPromJson = GsonUtils.toJson(crtipHotelPromList);
								ctripPromoJsonMap.put("Prom"+ordOrderGoodsVO.getSuppGoodsId(), ctripPromJson);
							}
						}
					}
				}
			}
			
			//存储产品商品结果集
			pageParam.setItems(ordOrderProductVOList);
			
			//保存分页对象
			model.addAttribute("pageParam", pageParam);
			// 携程促销对象map
			model.addAttribute("ctripPromoJsonMap", ctripPromoJsonMap);
		}
		
		
		//打印耗时
		if(LOG.isDebugEnabled()){
			LOG.debug("后台下单产品商品查询完毕-耗时"+(System.currentTimeMillis()-start)+"毫秒!");
		}
		
		//返回查询结果页面
		return "/order/orderProductQuery/order_product_query_result";
	}
	final String[] prop_param_list=new String[]{"establishmentDateFlag",
			"renovationDateFlag",
			"facilitieses"};
	private void filterParam(Map<String,Object> param,OrdOrderProductQueryVO ordOrderProductQueryVO){
		boolean usePropParam=false;
		for(String key:prop_param_list){
			if(param.containsKey(key)){
				usePropParam=true;
				break;
			}
		}
		if(!usePropParam){
			if(ArrayUtils.isNotEmpty(ordOrderProductQueryVO.getStarRates())){
				usePropParam=true;
			}
		}
		param.put("usePropParam", usePropParam);
	}
	
	/**
	 * 将房型商品集合转化为map
	 * @param ordOrderGoodsVOList
	 * @return
	 */
	private Map<Long, List<OrdOrderGoodsVO>> getOrderGoodsMap(List<OrdOrderGoodsVO> ordOrderGoodsVOList){
		Map<Long, List<OrdOrderGoodsVO>> map = new HashMap<Long, List<OrdOrderGoodsVO>>();
		for (OrdOrderGoodsVO orderGoodsVO : ordOrderGoodsVOList) {
			final List<OrdOrderGoodsVO> list;
			if (map.containsKey(orderGoodsVO.getProductId())) {
				list = map.get(orderGoodsVO.getProductId());
			} else {
				list = new ArrayList<OrdOrderGoodsVO>();
			}
			list.add(orderGoodsVO);
			map.put(orderGoodsVO.getProductId(), list);
		}
		return map;
	}
	
	/**
	 * 组装产品商品查询结果
	 * @param ordOrderProductVOList
	 * @return
	 */
	private List<OrdOrderProductVO> buildProductAndGoodsList(List<OrdOrderProductVO> ordOrderProductVOList,
			List<OrdOrderGoodsVO> ordOrderGoodsVOList,HttpServletRequest request){
		//将商品集合转化为MAP,方便取值
		Map<Long, List<OrdOrderGoodsVO>> ordOrderGoodsMap = this.getOrderGoodsMap(ordOrderGoodsVOList);
		
		//填充每个产品的商品集合
		for(OrdOrderProductVO ordOrderProductVO:ordOrderProductVOList){
			if(!ordOrderGoodsMap.isEmpty()&&ordOrderGoodsMap.containsKey(ordOrderProductVO.getProductId())){
				//获取该产品的商品集合
				List<OrdOrderGoodsVO>  ordOrderGoodsListTmp = ordOrderGoodsMap.get(ordOrderProductVO.getProductId());
				//取到第一个房型商品
				OrdOrderGoodsVO ordOrderGoodsVO = ordOrderGoodsListTmp.get(0);//必须存在，否则查不出来
				//设置商品集合
				ordOrderProductVO.setOrdOrderGoodsVOList(ordOrderGoodsListTmp);
				//设置产品的其他属性
				ordOrderProductVO.setDistrictName(ordOrderGoodsVO.getDistrictName());//设置城市
				ordOrderProductVO.setStarRateName(this.getStarRateName(ordOrderGoodsVO.getStarRate(), request));//设置酒店星级
				ordOrderProductVO.setAddress(ordOrderGoodsVO.getAddress());//设置酒店地址
				ordOrderProductVO.setEstablishmentDate(ordOrderGoodsVO.getEstablishmentDate());//设置开业时间
				ordOrderProductVO.setRenovationDate(ordOrderGoodsVO.getRenovationDate());//设置装修时间
			}
		}
		return ordOrderProductVOList;
	}
	
	/**
	 * 初始化查询表单
	 * @author wenzhengtao
	 * @param model
	 */
	private void initQueryForm(Model model,HttpServletRequest request) {
		//酒店星级字典
		HashMap<String,Object> paramStarRate = new HashMap<String,Object>();
		paramStarRate.put("propCode", "star_rate");
		List<BizCategoryProp> bizStarRates = categoryPropClientService.findAllPropsByParams(paramStarRate).getReturnContent();
		model.addAttribute("bizStarRate", (BizCategoryProp)bizStarRates.get(0));
		Map<String, String> starRateMap = new HashMap<String, String>();
		BizCategoryProp bizCategoryProp = bizStarRates.get(0);
		List<BizDict> bizDicts = bizCategoryProp.getBizDictList();
		for(BizDict bizDict:bizDicts){
			starRateMap.put(bizDict.getDictId()+"", bizDict.getDictName());
		}
		request.setAttribute("starRateMap", starRateMap);
	
		//酒店床型字典
		Map<String, String> bedTypeMap = new LinkedHashMap<String, String>();
		bedTypeMap.put("", "不限");
		bedTypeMap.put("大床房", "大床房");
		bedTypeMap.put("双人间", "双人间");
		bedTypeMap.put("三人间", "三人间");
		bedTypeMap.put("家庭房", "家庭房");
		bedTypeMap.put("套房", "套房");
		model.addAttribute("bedTypeMap", bedTypeMap);
		
		//支付方式字典
		Map<String,String> payTargetMap = new HashMap<String, String>();
		for(SuppGoods.PAYTARGET payTarget:SuppGoods.PAYTARGET.values()){
			payTargetMap.put(payTarget.name(), payTarget.getCnName());
		}
		model.addAttribute("payTargetMap", payTargetMap);
		
		//酒店设置字典
		Map<String,Object> facilitiesParams = new HashMap<String, Object>();
		facilitiesParams.put("propCode", "facilities");
		List<BizCategoryProp> categoryProps = categoryPropClientService.findAllPropsByParams(facilitiesParams).getReturnContent();
		BizCategoryProp categoryProp = new BizCategoryProp();
		if(!CollectionUtils.isEmpty(categoryProps)){
			categoryProp = categoryProps.get(0);
		}
		Map<String, Object> facilitiesMap = new HashMap<String,Object>();
		List<BizDict> dictsForFacilities = categoryProp.getBizDictList();
		if(!CollectionUtils.isEmpty(dictsForFacilities)){
			for(BizDict dict:dictsForFacilities){
				facilitiesMap.put(String.valueOf(dict.getDictId()), dict.getDictName());
			}
		}
		model.addAttribute("facilitiesMap", facilitiesMap);
		
		//价格区间字段
		Map<String,Object> priceRangeMap = new LinkedHashMap<String, Object>();
		priceRangeMap.put("", "不限");
		priceRangeMap.put("1", "200以下");
		priceRangeMap.put("2", "200-300");
		priceRangeMap.put("3", "300-400");
		priceRangeMap.put("4", "400-600");
		priceRangeMap.put("5", "600-800");
		priceRangeMap.put("6", "800以上");
		priceRangeMap.put("7", "自定义");
		model.addAttribute("priceRangeMap", priceRangeMap);
		
		//宽带字典
		Map<String,Object> paramInternet = new HashMap<String,Object>();
		paramInternet.put("propCode", "internet");
		List<BizBranchProp> bizInternets = branchPropClientService.findBranchPropList(paramInternet).getReturnContent();
		BizBranchProp bizBranchPropInternet = new BizBranchProp();
		if(!CollectionUtils.isEmpty(bizInternets)){
			bizBranchPropInternet = bizInternets.get(0);
		}
		Map<String, String> internetDic = new HashMap<String, String>();
		List<BizDict> dictInternetList = bizBranchPropInternet.getDictList();
		if(!CollectionUtils.isEmpty(dictInternetList)){
			for(BizDict dict : dictInternetList){
				internetDic.put(String.valueOf(dict.getDictId()), dict.getDictName());
			}
		}
		request.setAttribute("internetDic", internetDic);
		
		//房型字典
		Map<String,Object> paramBedType = new HashMap<String,Object>();
		paramBedType.put("propCode", "bed_type");
		List<BizBranchProp> bizBedTypes = branchPropClientService.findBranchPropList(paramBedType).getReturnContent();
		BizBranchProp bizBranchPropBedType = new BizBranchProp();
		if(!CollectionUtils.isEmpty(bizBedTypes)){
			bizBranchPropBedType = bizBedTypes.get(0);
		}
		Map<String, String> bedTypeDic = new HashMap<String, String>();
		List<BizDict> dictBedTypeList = bizBranchPropBedType.getDictList();
		if(!CollectionUtils.isEmpty(dictBedTypeList)){
			for(BizDict dict : dictBedTypeList){
				bedTypeDic.put(String.valueOf(dict.getDictId()), dict.getDictName());
			}
		}
		request.setAttribute("bedTypeDic", bedTypeDic);
	}
	
	/**
	 * 获取宽带名称
	 * @param internet
	 * @return
	 */
	private String getInternetName(String internet,HttpServletRequest request){
		@SuppressWarnings("unchecked")
		Map<String, String> internetDic = (Map<String, String>)request.getAttribute("internetDic");
		return internetDic.get(internet);
	}
	
	/**
	 * 获取床型名称
	 * @param bedType
	 * @param request
	 * @return
	 */
	private String getBedTypeName(String bedType,HttpServletRequest request){
		@SuppressWarnings("unchecked")
		Map<String, String> bedTypeDic = (Map<String, String>)request.getAttribute("bedTypeDic");
		return bedTypeDic.get(bedType);
	}
	
	/**
	 * 获得酒店星级名称
	 * @param starRate
	 */
	private String getStarRateName(String starRate,HttpServletRequest request){
		String starRateName = "未知星级";
		@SuppressWarnings("unchecked")
		Map<String, String> starRateMap = (Map<String, String>)request.getAttribute("starRateMap");
		if(starRateMap.get(starRate) != null){
			starRateName = starRateMap.get(starRate);
		}
		return starRateName;
	}
	
	/**
	 * 获取产品公告
	 * @param productId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private List<ProdProductNotice> getProductNoticeByCondition(Long productId,Date startTime,Date endTime){
		Map<String, Object> paramProductNotice = new HashMap<String, Object>();
		paramProductNotice.put("searchTimeBegin",startTime);
		paramProductNotice.put("searchTimeEnd",endTime);
		paramProductNotice.put("productId", productId);
		paramProductNotice.put("cancelFlag","Y" );
		ResultHandleT<List<ProdProductNotice>> resultHandleT= prodProductNoticeClientService.findProductNoticeList(paramProductNotice);
		List<ProdProductNotice> productNoticeList = resultHandleT.getReturnContent();
		return productNoticeList;
	}
	
}
