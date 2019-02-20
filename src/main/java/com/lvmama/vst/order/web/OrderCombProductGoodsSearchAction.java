package com.lvmama.vst.order.web;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.comm.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.client.biz.service.BranchPropClientService;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.compShip.service.CompShipProductClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsSaleReClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRelation;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.AdditionalProductVO;
import com.lvmama.vst.back.prod.vo.CompShipProductFrontVO;
import com.lvmama.vst.back.prod.vo.CompShipProductVO;
import com.lvmama.vst.back.prod.vo.ProdGroupDateVO;
import com.lvmama.vst.back.prod.vo.ProdOrderPackVO;
import com.lvmama.vst.back.prod.vo.ShipProductBranchVO;
import com.lvmama.vst.back.prod.vo.VisaProductBranchVO;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.service.impl.OrderTravelContractDataServiceFactory;
import com.lvmama.vst.order.service.ICouponService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderProductQueryService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

/**
 * 组合产品下单
 * 
 * mayonghua
 * @date 2014-4-15
 * 
 */
@Controller
public class OrderCombProductGoodsSearchAction extends BaseOrderAciton implements Serializable{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -4202451464063888166L;
	
	/**
	 * 日志记录器测试用
	 */
	private static final Logger LOG = LoggerFactory.getLogger(OrderCombProductGoodsSearchAction.class);
	
	/**
	 * 日期格式化串
	 */
	private static final String DATE_FORMAT="yyyy-MM-dd";
	
	/**
	 * 产品和商品查询业务接口
	 */
	@Autowired
	private IOrderProductQueryService orderProductQueryService;

	/**
	 * 产品业务接口
	 */
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	/**
	 * 行政区域业务接口
	 */
	@Autowired
	private DistrictClientService districtClientService;
	
	/**
	 * 时间价格业务接口
	 */
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	/**
	 * 产品规格业务接口
	 */
	@Autowired
	private	CategoryPropClientService categoryPropClientService;
	
	/**
	 * 商品规格业务接口
	 */
	@Autowired
	private	BranchPropClientService branchPropClientService;
	
	@Autowired
	private IOrderLocalService ordOrderClientService;
	
	@Autowired
	private CompShipProductClientService compShipProductClientService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderTravelContractDataServiceFactory orderTravelContractDataServiceFactory;
	
	@Autowired
	private ICouponService couponService;
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private SuppGoodsSaleReClientService suppGoodsSaleReClientService;
	
	/**
	 * 跳转到邮轮后台下单
	 * @return
	 */
	@RequestMapping(value="/ord/order/showCombOrderProductQueryList.do")
	public String toCombProductGoodsSearchAction(Model model,HttpServletRequest request){

		if(isRedirectShipBack()){
			String userId = request.getParameter("userId");
			String shipUrl = "redirect:http://super.lvmama.com/ship_back/ord/order/showCombOrderProductQueryList.do";
			if(StringUtils.isNotEmpty(userId)){
				shipUrl+="?userId="+userId;
			}
			return shipUrl;
		}

		UserUser user=null;
		try {
			//从cookie中读取用户信息
			user=readUserCookie();
			if(user==null|| StringUtil.isEmptyString(user.getUserId())){
				String userId = request.getParameter("userId");
				if(StringUtil.isNotEmptyString(userId)){
					user=userUserProxyAdapter.getUserUserByUserNo(userId);
					if(user==null){
						LOG.error("userUserProxyAdapter.getUserUserByUserNo("+userId+") failed , user is null ");
					}
				}
			}
		}catch (Exception e) {
			LOG.error("get user failed , error msg is ", e.getMessage());
		}

		model.addAttribute("user", user);
		return "/order/orderProductQuery/showOrderProductQueryList_curise";
	}
	
	private boolean isRedirectShipBack(){
		return true;
	}
	
	
	/**
	 * 查询邮轮产品
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/queryShipProductList.do")
	public String queryProductList(Model model,String beginDate,String endDate,String productName,Long productId,Long distributionId,HttpServletRequest req,Integer page) throws BusinessException{
		try {
			Date beginDateReal = DateUtil.toDate(beginDate, "yyyy-MM-dd");
			Date endDateReal = DateUtil.toDate(endDate, "yyyy-MM-dd");
			Date date = new Date();
			//判断日期是否合法,如果不合法，则自动增加5天
			if(DateUtil.inAdvance(beginDateReal, date) && beginDateReal.getDay()!=date.getDay()){
				beginDateReal = DateUtil.dsDay_Date(date, 5);
				beginDateReal = CalendarUtils.getDateFormatTime(beginDateReal, 0, 0, 0);
			}
			
			//组装查询条件
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("beginDate", beginDateReal);
			params.put("endDate", endDateReal);
			params.put("productName", productName);
			params.put("productId", productId);
			params.put("distributionId", distributionId);
			//查询总行数
			ResultHandleT<Integer> countResultHandle = prodProductClientService.findProductByGroupDateCounts(params);
			int pagenum = page == null ? 1 : page;
			Page pageParam = Page.page(countResultHandle.getReturnContent(), 4, pagenum);
			pageParam.buildJSONUrl(req);
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());
			ResultHandleT<List<ProdProduct>> resultHandle = prodProductClientService.findProductByGroupDate(params);
			pageParam.setItems(resultHandle.getReturnContent());
			model.addAttribute("pageParam", pageParam);
			model.addAttribute("prodList", resultHandle.getReturnContent());
		} catch (Exception e) {
			LOG.error("{}", e);
		}
		
		return "/order/orderProductQuery/findCombProductList";
	}
	
	
	/**
	 * ajax查询邮轮团期
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/queryShipProductGroupDateList.do")
	public String queryProductGroupDateList(Model model,Long productId,Long distributorId,Date beginDate,Date endDate, HttpServletResponse response) throws BusinessException{
		//判断日期是否合法,如果不合法，则自动增加5天
		Date date = new Date();
		if(DateUtil.inAdvance(beginDate, new Date()) && beginDate.getDay()!=date.getDay()){
			beginDate = DateUtil.dsDay_Date(new Date(), 5);
			beginDate = CalendarUtils.getDateFormatTime(beginDate, 0, 0, 0);
		}
		//组装查询条件
//		Map<String, Object> params = new HashMap<String, Object>();
		ResultHandleT<List<ProdGroupDateVO>> resultHandle =   prodProductClientService.findProdGroupDateAndPrice(beginDate, endDate,productId,distributorId);
		List<ProdGroupDateVO> list = resultHandle.getReturnContent();
		Calendar calendar = Calendar.getInstance();
		for(ProdGroupDateVO prodGroupDateVO : list){
			//获得时间为周几
			if (prodGroupDateVO.getDate() != null) {
				calendar.setTime(prodGroupDateVO.getDate());
				int week = calendar.get(Calendar.DAY_OF_WEEK);
				switch (week) {
				case 1: {
					prodGroupDateVO.setWeek("周日");
				}
					break;
				case 2: {
					prodGroupDateVO.setWeek("周一");
				}
					break;
				case 3: {
					prodGroupDateVO.setWeek("周二");
				}
					break;
				case 4: {
					prodGroupDateVO.setWeek("周三");
				}
					break;
				case 5: {
					prodGroupDateVO.setWeek("周四");
				}
					break;
				case 6: {
					prodGroupDateVO.setWeek("周五");
				}
					break;
				case 7: {
					prodGroupDateVO.setWeek("周六");
				}
					break;
				}
			}
		}
		model.addAttribute("groupDateList", resultHandle.getReturnContent());
		model.addAttribute("productId", productId);
		return "/order/orderProductQuery/findCombGroupDate";
	}
	
	
	/**
	 * 查询详细信息
	 * @return
	 */
	@RequestMapping(value="/ord/order/queryCombDetailList.do")
	public String findCombDetailList(Model model,Long productId,Date specDate,String userId,Long distributionId,HttpServletResponse response){
		//岸上观光
		List<AdditionalProductVO> sightSeeingProductList = null;
		//签证
		List<VisaProductBranchVO> visaProductBranchList = null;
		//邮轮规格
		List<ShipProductBranchVO> shipProdBranchList = null;
		//附加属性
		List<AdditionalProductVO> combAdditionProductList = null;
		
		HashMap<String,String> combOptionType = null;
		List<Person> personList=null;
		
		//产品是否可以订单游玩人后置
		boolean isTravellerDelay = false;
		
		HashMap<String,Object> conditionMap = new HashMap<String,Object>();
		conditionMap.put("hasProp", true);
		conditionMap.put("hasPropValue", true);
		conditionMap.put("specDate", specDate);
		conditionMap.put("distributionId", distributionId);
		List<Long> idsList = new ArrayList<Long>();
		idsList.add(productId);
		ResultHandleT<List<CompShipProductVO>>  compShipProductListHandle = compShipProductClientService.findCompShipProductBackList(idsList, conditionMap);
		List<CompShipProductVO> compShipProductList =  compShipProductListHandle.getReturnContent();
		//因为每次只有一个产品，所以直接获取第一个
		if(compShipProductList!=null && compShipProductList.size()>0){
			CompShipProductVO cv = compShipProductList.get(0);
			//游玩人后置标记
			isTravellerDelay = Constants.Y_FLAG.equals(cv.getTravellerDelayFlag());
			Map<String,CompShipProductFrontVO> dcspMap =  cv.getCompShipProductFrontVoMap();
			//获得指定团期的数据
			String key = CalendarUtils.getDateFormatString(specDate, "yyyy-MM-dd");
			CompShipProductFrontVO cspf = dcspMap.get(key);
			if(cspf !=null){
				//拆分数据
//				shipProdBranchList = cspf.getShipProdBranchList();
				shipProdBranchList = new ArrayList<ShipProductBranchVO>();
				Map<String, List<ShipProductBranchVO>> hashMap = cspf.getShipProdBranchListMap();
				if(hashMap!=null){
					Iterator iter = hashMap.entrySet().iterator();
					while (iter.hasNext()) { 
					    Map.Entry<String, List<ShipProductBranchVO>> entry = (Map.Entry<String, List<ShipProductBranchVO>>) iter.next(); 
					    List<ShipProductBranchVO> list = entry.getValue(); 
					    shipProdBranchList.addAll(list);
					} 
				}
				sightSeeingProductList = cspf.getSightSeeingProductList();
				visaProductBranchList = cspf.getVisaProductBranchList();
				combAdditionProductList = cspf.getComAdditionProductList();
				combOptionType = cspf.getCombOptionTypeMap();
			}
		}
		
		Map<String,Map<String, List<SuppGoods>>> groupDateMap = new HashMap<String, Map<String,List<SuppGoods>>>(); 
		ResultHandleT<List<SuppGoodsSaleRe>> rht = new ResultHandleT<List<SuppGoodsSaleRe>>();
		try {
			rht = suppGoodsSaleReClientService.selectGoodsSaleReAndInsuranceGoodsList(productId, distributionId, specDate, 1L, 0L);
			if(rht != null && rht.getReturnContent() != null && !rht.getReturnContent().isEmpty()){
				for(SuppGoodsSaleRe goodsSaleRe : rht.getReturnContent()){//关联销售对象-保险
					if(goodsSaleRe.getInsSuppGoodsList() != null && !goodsSaleRe.getInsSuppGoodsList().isEmpty()){
						//1.创建保险份数和保险时间-价格关系
						for(SuppGoods insGoods : goodsSaleRe.getInsSuppGoodsList()){//关联销售对象-保险-商品列表
							SuppGoodsNotimeTimePrice insTimePrice = (SuppGoodsNotimeTimePrice)insGoods.getSuppGoodsBaseTimePrice();
							if(insTimePrice != null){
								Map<String,Long> goodsSelectPriceMap = new HashMap<String,Long>();
								goodsSelectPriceMap.put(DateUtil.formatSimpleDate(specDate),  insTimePrice.getPrice());
								insGoods.setSelectPriceMap(goodsSelectPriceMap);//商品 时间-价格
								if(SuppGoodsRelation.RELATIONTYPE.AMOUNT.name().equalsIgnoreCase(goodsSaleRe.getReType())){//等量
									insGoods.setSelectQuantityRange(String.valueOf(1));//商品份数
								}else if(SuppGoodsRelation.RELATIONTYPE.OPTION.name().equalsIgnoreCase(goodsSaleRe.getReType())){//可选
									insGoods.setSelectQuantityRange(0 + "," + String.valueOf(1));
								}else if(SuppGoodsRelation.RELATIONTYPE.OPTIONAL.name().equalsIgnoreCase(goodsSaleRe.getReType())){//任选
									StringBuffer selectQuantityRange = new StringBuffer(); 
									for(int i=0; i < 2; i ++){
										selectQuantityRange.append(i + ",");
									}
									if(selectQuantityRange.length() > 0){
										insGoods.setSelectQuantityRange(selectQuantityRange.substring(0, selectQuantityRange.length() - 1));
									}
								}
							}
						}
						//2.第一个保险默认设置份数顺序为从大到小
						SuppGoods firstInsGoods = goodsSaleRe.getInsSuppGoodsList().get(0);
						String selectQuantityRange = firstInsGoods.getSelectQuantityRange();
						StringBuffer sb = new StringBuffer();
						if(selectQuantityRange != null){
							String[] quantityRange = selectQuantityRange.split(",");
							for(int i = quantityRange.length - 1; i >= 0; i--){
								sb.append(quantityRange[i]).append(",");
							}
							firstInsGoods.setSelectQuantityRange(sb.substring(0, sb.length()-1));									
						}
					}
				}
				//3.组装页面需要的数据
				Map<String, List<SuppGoods>> prodMap = new HashMap<String, List<SuppGoods>>();
				groupDateMap.put(DateUtil.formatSimpleDate(specDate), prodMap);
				for(SuppGoodsSaleRe goodsSaleRe : rht.getReturnContent()){
					if(goodsSaleRe.getInsSuppGoodsList() != null && !goodsSaleRe.getInsSuppGoodsList().isEmpty()){
						for(SuppGoods suppGoods : goodsSaleRe.getInsSuppGoodsList()){
							List<SuppGoods> goodsList = prodMap.get(String.valueOf(suppGoods.getProductId()));
							if(goodsList == null){
								goodsList = new ArrayList<SuppGoods>();
								goodsList.add(suppGoods);
								prodMap.put(String.valueOf(suppGoods.getProductId()), goodsList);
							}else{
								goodsList.add(suppGoods);
							}
						}
					}
				}
			}		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		model.addAttribute("suppGoodsSaleReList", rht.getReturnContent());
		model.addAttribute("groupDateMap", groupDateMap);
		personList=orderService.loadUserReceiversByUserId(userId);
		model.addAttribute("personList",personList);
		model.addAttribute("specDate",specDate);
		model.addAttribute("shipProdBranchList",shipProdBranchList);
		model.addAttribute("combOptionType",combOptionType);
		model.addAttribute("sightSeeingProductList",sightSeeingProductList);
		model.addAttribute("visaProductBranchList",visaProductBranchList);
		model.addAttribute("combAdditionProductList",combAdditionProductList);
		//游玩人后置
		if(isTravellerDelay){
			model.addAttribute("isTravellerDelay",Constants.Y_FLAG);
		}
		return "/order/orderProductQuery/findCombProductDetailList";
	}
	
	/**
	 * 后台下单，创建订单
	 */
	@RequestMapping(value="/ord/order/combBackCreateOrder.do")
	public String createOrder(Model model,BuyInfo buyInfo,HttpServletResponse response,HttpServletRequest req){
			try{
			boolean saveAllFlag = true;
			
			//组装BuyInfo
			createBuyInfo(buyInfo,req);
			initBooker(buyInfo);
			//设置IP
			buyInfo.setIp(req.getRemoteAddr());
			//设置主订单categgoryId
			buyInfo.setCategoryId(8L);
			
			ResultHandle  checkHandle = orderService.checkStock(buyInfo);
			if(checkHandle.isFail()){
				if(checkHandle.isFail()){
					model.addAttribute("ERROR",checkHandle.getMsg());
					return ERROR_PAGE;
				}
			}
			
			//验证
			ProdOrderPackVO popv = createProdOrderPackVO(buyInfo);
			ResultHandle  handle = prodProductClientService.validateOrderPack(popv);
			if(handle.isFail()){
				model.addAttribute("ERROR",handle.getMsg());
				return ERROR_PAGE;
			}
			
			
			//验证通过，可以下单
			ResultHandleT<OrdOrder> orderHandle = null;
			orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
			//判断下单是否成功
			if(orderHandle.isFail()){
				model.addAttribute("ERROR",orderHandle.getMsg());
				return ERROR_PAGE;		
			}
			//存在游玩人待填写时不全部保存
			if(Constants.Y_FLAG.equals(buyInfo.getTravellerDelayFlag()) &&  (!Constants.Y_FLAG.equals(req.getParameter("saveAllFlag"))) ){
				saveAllFlag = false;
			}
			//保存常用联系人
			if(saveAllFlag){
				savePerson(buyInfo.getTravellers(),buyInfo.getUserId());
			}
			
			OrdOrder ordOrder=orderHandle.getReturnContent();
			
			String ordResult = req.getParameter("ordResult");
			String productName = req.getParameter("productName");
			String specDate = req.getParameter("specDate");
			model.addAttribute("ordResult", ordResult);
			model.addAttribute("orderId", ordOrder.getOrderId());
			model.addAttribute("productName", productName);
			Date date = CalendarUtils.getDateFormatDate(specDate, "yyyy-MM-dd");
			model.addAttribute("specDate", CalendarUtils.getDateFormatString(date, "yyyy年MM月dd日"));
			model.addAttribute("contract", buyInfo.getContact());
			model.addAttribute("ordId", ordOrder.getOrderId());
			
			IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory.createTravelContractDataService(ordOrder);
			if (orderTravelContractDataService != null) {
				OrdOrderPack ordOrderPack = ordOrder.getOrderPackList().get(0);
				ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = orderTravelContractDataService.getCombCuriseProducatData(ordOrderPack.getCategoryId(), ordOrderPack.getProductId());
				CuriseProductVO curiseProductVO = resultHandleCuriseProductVO.getReturnContent();
				Map<String, Object> productPropMap = curiseProductVO.getProductPropMap();
				// 退款说明
				String cancelStr = productPropMap.get("change_and_cancellation_instructions")==null ? "无退款说明" : productPropMap.get("change_and_cancellation_instructions").toString();
				if (StringUtils.isNotEmpty(cancelStr)) {
					cancelStr = cancelStr.replace("\r\n", "<br>");
				}
				model.addAttribute("cancelStr", cancelStr);
			}
			
			}catch(Exception e){
				LOG.error("{}", e);
				model.addAttribute("ERROR","下单错误，请检查订单项价格，数量是否正确");
				return ERROR_PAGE;
			}
		return "/order/orderProductQuery/comb_orderresult_dialog";
	}
	
	
	/**
	 * 提交订单
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/completeOrder.do")
	@ResponseBody
	public String completeOrder(Model model,Long orderId, HttpServletResponse response) throws BusinessException{
		 ResultHandle  handler = ordOrderClientService.startBackOrder(orderId, getLoginUserId());
		 if(handler.isSuccess() && handler.getMsg()==null){
			 return "success";
		 }
		return "";
	}
	
	/**
	 * 保存为常用游客
	 * @param travellers
	 * @param userId
	 */
	private void savePerson(List<Person> travellers,String userId){
		//过滤掉已经是常用游客的数据
		List<Person> personList=orderService.loadUserReceiversByUserId(userId);
		if(personList!=null){
			for(int i=0;i<personList.size();i++){
				Person person = personList.get(i);
				for (int j = 0; j < travellers.size(); j++) {
					Person person2 = travellers.get(j);
					if(person.getFullName()!=null && person2.getFullName()!=null){
						if(person.getFullName().equalsIgnoreCase(person2.getFullName())){
							travellers.remove(j);
						}
					}
				}
			}
		}
		for (int i = 0; i < travellers.size(); i++) {
			travellers.get(i).setPeopleType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		}
		orderService.createContact(travellers, userId);
	}
	
	/**
	 * 构造ProdOrderPackVO
	 * @param buyInfo
	 * @return
	 */
	private ProdOrderPackVO createProdOrderPackVO(BuyInfo buyInfo){
		ProdOrderPackVO popv = new ProdOrderPackVO();
		//设置品类
		popv.setCategoryId(buyInfo.getCategoryId());
		//设置产品
		popv.setProductId(buyInfo.getProductId());
		HashMap<Long,Long> goodsQuantityMap = new HashMap<Long,Long>();
		//构造商品:数量对应关系
		for(BuyInfo.Item item : buyInfo.getItemList()){
			goodsQuantityMap.put(item.getGoodsId(), Long.valueOf(item.getQuantity()));
		}
		//设置游玩人数量
		popv.setTravellerCount(buyInfo.getTravellers().size());
		popv.setGoodsQuantityMap(goodsQuantityMap);
		
		return popv;
	}
	
	/**
	 * 下单人
	 * @param buyInfo
	 *//*
	private void initBooker(BuyInfo buyInfo){
		UserUser user = userUserProxyAdapter.getUserUserByUserNo(buyInfo.getUserId());
		Person person = new Person();
		if(user!=null){
			person.setFullName(user.getUserName());
			person.setMobile(user.getMobileNumber());
			buyInfo.setUserNo(user.getId());
			buyInfo.setBooker(person);
		}
	}*/
	
	/**
	 * 组装BuyInfo的信息
	 * @param buyInfo
	 * @param req
	 */
	private void createBuyInfo(BuyInfo buyInfo,HttpServletRequest req){
		List<Person> travellers = buyInfo.getTravellers();
		String[] cabins =  (String[])req.getParameterValues("cabin");
		if(travellers.size()!=cabins.length){
			throw new BusinessException("游玩人指定舱房错误");
		}
		//组装ItemPersonRelation Map
		HashMap<String,List<ItemPersonRelation>> goodsPersonMap = new HashMap<String,List<ItemPersonRelation>>();
		for(int i=0;i<cabins.length;i++){
			String cabinId = cabins[i];
			//如果不存在，新建
			if(goodsPersonMap.get(cabinId)==null){
				List<ItemPersonRelation> tempPersonList = new ArrayList<ItemPersonRelation>();
				goodsPersonMap.put(cabinId, tempPersonList);
				ItemPersonRelation ipr = new ItemPersonRelation();
				ipr.setPerson(travellers.get(i));
				if(travellers.get(i) != null){
					ipr.setRoomNo(travellers.get(i).getRoomNo());
				}				
				tempPersonList.add(ipr);
			}else {
				List<ItemPersonRelation> tempPersonList = goodsPersonMap.get(cabinId);
				ItemPersonRelation ipr = new ItemPersonRelation();
				ipr.setPerson(travellers.get(i));
				if(travellers.get(i) != null){
					ipr.setRoomNo(travellers.get(i).getRoomNo());
				}
				tempPersonList.add(ipr);
			}
		}
		//用于存放所有游玩人的List
		List<ItemPersonRelation> itemPersonRelationAllList = new ArrayList<ItemPersonRelation>();
		for(List<ItemPersonRelation> list  : goodsPersonMap.values()){
			itemPersonRelationAllList.addAll(list);
		}
		//组装Item
		for(BuyInfo.Item item : buyInfo.getItemList()){
			Long goodsId = item.getGoodsId();
			//主订单
			if("true".equalsIgnoreCase(item.getMainItem())){
				item.setItemPersonRelationList(goodsPersonMap.get(goodsId+""));
			}else {
				//子订单设置所有游玩人
				item.setItemPersonRelationList(itemPersonRelationAllList);
			}
		}
	}
	
	
	@RequestMapping("/ord/order/validateCoupon.do")
	@ResponseBody
	public Object validateCoupon(BuyInfo buyInfo,HttpServletRequest req){
//		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	//组装BuyInfo
				createBuyInfo(buyInfo,req);
				initBooker(buyInfo);
			 	ResultHandle resultHandle=couponService.validateCoupon(buyInfo);
			    if(!resultHandle.isSuccess()){
			    	msg.setCode(resultHandle.getMsg());
			    }
		} catch (Exception e) {
			LOG.error("{}", e);
			msg.setCode("验证发生异常");
		}
		 return msg;
	}
	
	@RequestMapping("/ord/order/calCoupon.do")
	@ResponseBody
	public Object calCoupon(BuyInfo buyInfo,HttpServletRequest req){
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	//组装BuyInfo
				createBuyInfo(buyInfo,req);
				initBooker(buyInfo);
				Pair<FavorStrategyInfo,Object> resultPair=orderService.calCoupon(buyInfo);
				if(resultPair.isSuccess()){
				 	FavorStrategyInfo favorStrategyInfo=resultPair.getFirst();
				    attributes.put("favorStrategyInfo", favorStrategyInfo);
				 	msg.setAttributes(attributes);
			    }else{
			    	msg.setCode(resultPair.getMsg());
			    }
		} catch (Exception e) {
			LOG.error("{}", e);
			msg.setCode("计算优惠发生异常");
		}
		 return msg;
	}
}
