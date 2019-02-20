package com.lvmama.vst.order.web;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizFlight;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdCalClientService;
import com.lvmama.vst.back.client.prod.service.ProdGroupDateClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductAdditionClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.client.prod.service.ProdVisaDocDateClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRelation;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdGroupDate;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTransport;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductAddtional;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.back.prod.po.ProdProductSaleRe;
import com.lvmama.vst.back.prod.po.ProdStartDistrictAdditionalVO;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.prod.po.ProdTrafficBus;
import com.lvmama.vst.back.prod.po.ProdTrafficFlight;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.prod.po.ProdVisaDocDate;
import com.lvmama.vst.back.prod.po.ProdPackageGroup.GROUPTYPE;
import com.lvmama.vst.back.prod.vo.PackageTourProductVo;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.supp.vo.SuppGoodsLineTimePriceVo;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.Constant.SUB_PRODUCT_TYPE;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.flight.client.branch.vo.BranchVo;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.flight.client.product.service.FlightSearchService;
import com.lvmama.vst.flight.client.product.vo.AirLineDayVo;
import com.lvmama.vst.flight.client.product.vo.LvfSignVo;
import com.lvmama.vst.flight.client.product.vo.TrafficGroupVo;
import com.lvmama.vst.flight.client.product.vo.TrafficVo;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.vo.FlightParam;
import com.lvmama.vst.order.web.line.LineProdPackageGroupContainer;
import com.lvmama.vst.order.web.line.LineUtils;
import com.lvmama.vst.order.web.line.service.LineProdPackageGroupService;
import com.lvmama.vst.order.web.util.OrderLineProductQueryUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @author xiechenglong 定制游后台下单
 */
@Controller
@RequestMapping("/ord/productQuery/customized")
public class OrderCustomizedProductQueryAction extends BaseOrderAciton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 669281824439702195L;

	private static final Logger log = LoggerFactory.getLogger(OrderCustomizedProductQueryAction.class);

	@Autowired
	private ProdProductClientService prodProductClientService;
	/**
	 * loading商品接口
	 */
	@Autowired
	private ProdCalClientService prodCalClientRemote;

	@Autowired
	private ProdGroupDateClientService prodGroupDateClientService;

	@Autowired
	private ProdProductClientService prodProductClientRemote; // 产品数据

	@Autowired
	ProdProductAdditionClientService prodProductAdditionClientRemote;

	@Autowired
	private ProdProductNoticeClientService prodProductNoticeService1;

	@Autowired
	private ProdVisaDocDateClientService prodVisaDocDateClientService;

	/**
	 * 保存订单接口
	 */
	@Autowired
	private OrderService orderService;

	@Autowired
	protected ProdPackageGroupClientService prodPackageGroupClientService;

	/**
	 * 计算打包产品时间价格表接口
	 */
	@Autowired
	private LineProdPackageGroupService lineProdPackageGroupServiceImpl;

	/**
	 * 规格操作接口
	 */
	@Autowired
	private BranchClientService branchClientRemote;

	@Autowired
	@Qualifier("flightSearchService")
	FlightSearchService flightSearchService;// 机票对接接口

	@Resource(name = "orderLineProductQueryUtil")
	private OrderLineProductQueryUtil orderLineProductQueryUtil;

	@Autowired
	private ProdTrafficClientService prodTrafficClientServiceRemote;// 得到交通的详细的数据

	/**
	 * 进入定制游产品商品查询页面
	 * 
	 * @param model
	 * @param req
	 * @return
	 */
	@RequestMapping("/showCustomizedOrderProductQueryList")
	public String showCustomizedOrderProductQueryList(Model model, HttpServletRequest req) {
		UserUser user = null;
		try {
			// 从cookie中读取用户信息
			user = readUserCookie();
			if (user == null || StringUtil.isEmptyString(user.getUserId())) {
				String userId = req.getParameter("userId");

				if (StringUtil.isNotEmptyString(userId)) {
					user = userUserProxyAdapter.getUserUserByUserNo(userId);
				}
			}

		} catch (Exception e) {
			log.error("{}", e);
		}
		model.addAttribute("user", user);
		return "/order/orderProductQuery/customized/showCustomizedOrderProductQueryList";
	}

	/**
	 * 查询定制游产品商品列表
	 * 
	 * @param model
	 * @param req
	 * @return
	 */
	@RequestMapping("/findCustomizedProductList")
	public String findCustomizedProductList(Model model, HttpServletRequest req, Long productId, String productName, Integer page) {
		log.debug("findCustomizedProductList start");
		UserUser user = null;
		Page pageParam = null;

		Map<String, ProdGroupDate> prodGroupDateMap = new HashMap<String, ProdGroupDate>();
		Map<String, List<ProdLineRouteVO>> productPropMap = new HashMap<String, List<ProdLineRouteVO>>();
		Map<String, ProdProduct> productMap = new HashMap<String, ProdProduct>();// 存放产品信息
		Map<String, Long> startDistrictIdMap = new HashMap<String, Long>();// 存放多出发ID

		try {
			Map<String, Object> params = new HashMap<String, Object>();

			if (productId == null) {
				if (StringUtils.isNumeric(productName)) {
					params.put("productId", productName);
				} else {
					params.put("productName", productName);
				}
			} else {
				params.put("productId", productId);
				if (StringUtils.isNotBlank(productName)) {
					params.put("productName", productName);
				}
			}
			
			/*Long[] bizCategoryIdArray = new Long[2];
			bizCategoryIdArray[0] = BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId();
			bizCategoryIdArray[1] = BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId();
			params.put("bizCategoryIdArray", bizCategoryIdArray);*/
			
			params.put("bizCategoryId", BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId());
			ResultHandleT<List<ProdProduct>> resultHandleT = prodProductClientService.findCustomizedProdProductListForParam(params);

			if (resultHandleT != null && resultHandleT.getReturnContent() != null && resultHandleT.getReturnContent().size() > 0) {
				List<ProdProduct> prodProductList = resultHandleT.getReturnContent();
				List<ProdProduct> result = new ArrayList<ProdProduct>();

				// 设置分页参数
				int pagenum = page == null ? 1 : page;
				pageParam = Page.page(prodProductList.size(), 10, pagenum);
				pageParam.buildJSONUrl(req, true);

				if (prodProductList.size() <= 10) {
					result = prodProductList;
				} else {
					result = prodProductList.subList(Integer.parseInt(String.valueOf(pageParam.getStartRows())), Integer.parseInt(String.valueOf(pageParam.getEndRows())));
				}
				pageParam.setItems(result);

				ProdProductParam param = new ProdProductParam();
				param.setLineRoute(true);
				param.setAddtion(true);
				param.setHotelCombFlag(true);

				for (ProdProduct prodProduct : result) {
					Long productID = prodProduct.getProductId();
					ResultHandleT<ProdGroupDate> prodGroupDate;
					ProdProductParam pparam = new ProdProductParam();
					pparam.setHotelCombFlag(false);
					ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(productID, pparam);
					if (resultHandle.getReturnContent() != null && "Y".equalsIgnoreCase(resultHandle.getReturnContent().getMuiltDpartureFlag())) {
						// 设置默认出发地
						ProdStartDistrictAdditionalVO startDistrictVo = adapterStartDistrict(resultHandle.getReturnContent(), null);
						startDistrictIdMap.put(prodProduct.getProductId() + "", startDistrictVo.getStartDistrictId());
						prodGroupDate = prodGroupDateClientService.findSimpleProdGroupDate(productID, startDistrictVo.getStartDistrictId());
					} else {
						prodGroupDate = prodGroupDateClientService.findSimpleProdGroupDate(productID);
					}

					if (prodGroupDate != null && !prodGroupDate.hasNull()) {
						prodGroupDateMap.put(prodProduct.getProductId() + "", prodGroupDate.getReturnContent());
					}

					ResultHandleT<ProdProduct> product = prodProductClientRemote.findLineProductByProductId(productID, param);
					if (product != null && product.getReturnContent() != null && CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList())) {
						productPropMap.put(prodProduct.getProductId() + "", product.getReturnContent().getProdLineRouteList());
						ProdProductAddtional Addtional = new ProdProductAddtional();
						try {
							Addtional = prodProductAdditionClientRemote.selectByPrimaryKey(product.getReturnContent().getProductId());
						} catch (Exception e) {
							log.error(e.getMessage());
						}
						ProdProduct prodProduct2 = product.getReturnContent();
						prodProduct2.setProductAddtional(Addtional);
						productMap.put(prodProduct2.getProductId() + "", prodProduct2);
					}

				}

			}
			// 从cookie中读取用户信息
			user = readUserCookie();

		} catch (Exception e) {
			log.error("{}", e);
		}

		model.addAttribute("result", pageParam);
		model.addAttribute("prodGroupDateMap", prodGroupDateMap);
		model.addAttribute("productPropMap", productPropMap);
		model.addAttribute("productMap", productMap);
		model.addAttribute("startDistrictIdMap", startDistrictIdMap);
		model.addAttribute("user", user);
		return "/order/orderProductQuery/customized/findCustomizedProductList";
	}

	/**
	 * 查询详细信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryCustomizedProductDetailList.do")
	public String queryCustomizedProductDetailList(ModelMap model, String productId, Date specDate, String userId, Long adultNum, Long childNum, Long copies, Long intentionOrderId,
			HttpServletResponse response, HttpServletRequest req) {

		// 首先第一次查询商品，判断是否为酒店套餐，以便计算成人数儿童数
		ProdProductParam param = new ProdProductParam();
		param.setHotelCombFlag(true);
		Long startTime = null;
		String methodName = "OrderCustomizedProductQueryAction#infoFillIn-->productId = " + productId;
		startTime = System.currentTimeMillis();
		ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(Long.parseLong(productId), param);
		log.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询定制游产品详情", "prodProductClientService.findLineProductByProductId", System.currentTimeMillis() - startTime));

		ProdProduct prodProduct = null;
		if (resultHandle != null && resultHandle.isSuccess() && !resultHandle.hasNull()) {
			prodProduct = resultHandle.getReturnContent();
		} else {
			model.addAttribute("ERROR", "商品Loading接口拨错，是否包含酒店套餐，商品不可售");
			return "order/error";
		}

		Long startDistrictId = null;
		ProdStartDistrictAdditionalVO startDistrictVo = null; // 多出发地时使用
		if ("Y".equalsIgnoreCase(prodProduct.getMuiltDpartureFlag())) { // 多出发地处理----设置默认出发地
			try {
				if (req.getParameter("startDistrictId") != null && !"".equals(req.getParameter("startDistrictId").toString().trim())) {
					try {
						startDistrictId = Long.valueOf(req.getParameter("startDistrictId"));
					} catch (Exception ex) {
					}
				}
				startDistrictVo = adapterStartDistrict(prodProduct, startDistrictId);
				if (startDistrictVo != null)
					startDistrictId = startDistrictVo.getStartDistrictId();
			} catch (Exception ex) {
				startDistrictId = null;
				log.info("未输入出发地");
			}
		}
		model.addAttribute("startDistrictVo", startDistrictVo);

		// 取到产品附加
		startTime = System.currentTimeMillis();
		ProdProductAddtional productAddtional = prodProductAdditionClientRemote.selectByPrimaryKey(Long.parseLong(productId));
		log.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询定制游产品附加信息", "prodProductAdditionClientRemote.selectByPrimaryKey", System.currentTimeMillis() - startTime));

		Long adultMinNum00 = 1L;
		Long childMinNum00 = 0L;
		if (productAddtional != null) {
			// 酒店套餐份数 或 成人数
			if (productAddtional.getAdultMinQuantity() != null) {
				adultMinNum00 = productAddtional.getAdultMinQuantity();
			}
			// 儿童数
			if (productAddtional.getChildMinQuantity() != null) {

				childMinNum00 = productAddtional.getChildMinQuantity();
			}
		}

		// 是否包含酒店套餐,N包含，Y不包含
		boolean isHotel = false;
		Long baseChildNum = 0L;
		Long baseAdultNum = 0L;
		boolean saleCopies = false;
		boolean isDestinationBU = ProductPreorderUtil.isDestinationBUDetail(prodProduct);

		if (CollectionUtils.isNotEmpty(prodProduct.getProdProductSaleReList())) {
			String saleType = prodProduct.getProdProductSaleReList().get(0).getSaleType();
			saleCopies = ProdProductSaleRe.SALETYPE.COPIES.name().equals(saleType);
			log.info("---------------是否按份计算------------------" + saleCopies + "");
		}
		if (prodProduct.getHotelCombFlag().equals("N") || saleCopies) {
			log.info("---------------开始按份计算------------------" + saleCopies + "");
			model.put("productSaleType", "COPIES");
			if (saleCopies) {
				baseAdultNum = prodProduct.getProdProductSaleReList().get(0).getAdult().longValue();
				baseChildNum = prodProduct.getProdProductSaleReList().get(0).getChild().longValue();
			} else if (prodProduct.getHotelCombFlag().equals("N")) {
				baseChildNum = prodProduct.getBaseChildQuantity();
				baseAdultNum = prodProduct.getBaseAdultQuantity();
			}

			if (copies == null) {
				copies = adultMinNum00;
			}
			if (baseChildNum == null || baseAdultNum == null) {
				model.addAttribute("ERROR", "商品Loading接口报错，酒店套餐成人儿童基准值");
				return "order/error";
			}
			adultNum = baseAdultNum * copies;
			childNum = baseChildNum * copies;
			if (prodProduct.getHotelCombFlag().equals("N")) {
				isHotel = true;
			}

		} else {
			if (adultNum == null) {
				adultNum = adultMinNum00;
				if (isDestinationBU && adultMinNum00 < 2) {// 目的地BU，最小默认值选择为2
					adultNum = 2L;
				}
			}

			if (childNum == null) {

				childNum = childMinNum00 == -1 ? 0 : childMinNum00;
			}
		}

		// 添加产品公告

		HashMap<String, Object> params = new HashMap<String, Object>();
		String startTimeStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		params.put("productId", productId);
		params.put("startTimeStr", startTimeStr);
		params.put("cancelFlag", "Y");

		ResultHandleT<List<ProdProductNotice>> productNoticeResult = prodProductNoticeService1.findProductNoticeList_asc(params);
		List<ProdProductNotice> noticeList = productNoticeResult.getReturnContent();
		if (CollectionUtils.isNotEmpty(noticeList)) {
			model.put("productNoticList", noticeList);
		}
		log.info("-----------公告长度,公告长度---------" + noticeList.size());
		log.info("-----------公告长度,公告长度---------" + noticeList.size());
		log.info("-----------公告长度,公告长度---------" + noticeList.size());
		log.info("-----------公告长度,公告长度---------" + noticeList.size());

		String result = "/order/orderProductQuery/customized/findCustomizedProductDetailList";

		// 根据产品id，加载产品对应的所有商品信息
		if (StringUtils.isNotEmpty(productId)) {
			Map<String, Object> resMap = this.loadProduct(model, productId, specDate, adultNum, childNum, isHotel, startDistrictId, req);
			result = (String) resMap.get("resultUrl");
			specDate = (Date) resMap.get("specDate");// 重新给团期日期的开始时间赋值
		} else {
			model.addAttribute("ERROR", "商品不可售");
			return "order/error";
		}
		loadPriceAllMonthById(model, productId, specDate);
		// 获取常用联系人
		List<Person> personList = null;
		UserUser user = null;
		try {
			user = userUserProxyAdapter.getUserUserByUserNo(userId);
			personList = orderService.loadUserReceiversByUserId(userId);
		} catch (Exception e) {
			log.error("{}", e);
		}
		model.put("user", user);
		model.put("personList", personList);

		// 房差份数列表
		StringBuffer sb = new StringBuffer();
		String selectRangeStr = (String) model.get("selectRange");
		if (selectRangeStr == null) {
			selectRangeStr = "";
		}
		String[] selectRangeArr = selectRangeStr.split(",");
		for (String str : selectRangeArr) {
			Pattern pattern = Pattern.compile("^\\d+");
			Matcher isNum = pattern.matcher(str);
			if (isNum.matches()) {
				sb.append("<option value=\"" + str + "\">" + str + "</option>");
			}
		}
		model.addAttribute("fangchaQuantity", sb.toString());

		// 酒店套餐份数/成人数选择列表
		StringBuffer adultSelect = new StringBuffer();
		// 儿童数选择列表
		StringBuffer childSelect = new StringBuffer();
		if (productAddtional != null) {
			PackageTourProductVo lineProductVo = (PackageTourProductVo) model.get("packageTourProductVo");
			if (lineProductVo != null) {
				lineProductVo.setProductAddtional(productAddtional);
			}
			if (isHotel || saleCopies) {
				// 酒店套餐份数
				Long adultMinQuantity = productAddtional.getAdultMinQuantity();
				Long adultMaxQuantity = productAddtional.getAdultMaxQuantity();
				if (adultMinQuantity == null) {
					adultMinQuantity = 1L;
				}
				if (adultMaxQuantity == null) {
					adultMaxQuantity = 1L;
				}
				for (int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++) {
					if (i == Integer.parseInt(copies.toString())) {
						adultSelect.append("<option  selected='selected' value=\"" + i + "\">" + i + "</option>");
					} else {
						adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
					}

				}
			} else {
				// 成人
				Long adultMinQuantity = productAddtional.getAdultMinQuantity();
				Long adultMaxQuantity = productAddtional.getAdultMaxQuantity();
				if (adultMinQuantity == null) {
					adultMinQuantity = 1L;
				}
				if (adultMaxQuantity == null) {
					adultMaxQuantity = 1L;
				}
				for (int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++) {
					if (isDestinationBU && i == adultNum) {// 目的地产品默认成人为2
						adultSelect.append("<option value=\"" + i + "\" selected = \"selected\">" + i + "</option>");
					} else {
						adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
					}

				}
				// 儿童
				Long childMinQuantity = productAddtional.getChildMinQuantity();
				Long childMaxQuantity = productAddtional.getChildMaxQuantity();
				if (childMinQuantity == null) {
					childMinQuantity = 0L;
				}
				if (childMaxQuantity == null) {
					childMaxQuantity = 0L;
				}
				model.put("childMinQuantity", childMinQuantity);
				for (int i = childMinQuantity.intValue(); i < childMaxQuantity.intValue() + 1; i++) {
					childSelect.append("<option value=\"" + i + "\">" + i + "</option>");
				}
			}
		}

		model.put("adultSelect", adultSelect.toString());
		model.put("childSelect", childSelect.toString());

		// 返回单份成人数儿童数
		model.put("baseChildNum", baseChildNum);
		model.put("baseAdultNum", baseAdultNum);
		// 返回总的成人数儿童数
		model.put("childNum", (childNum == null || childNum < 0) ? 0 : childNum);
		model.put("adultNum", adultNum);
		// 是否包含酒店套餐
		model.put("isHotel", isHotel);

		// 是否按份销售
		model.put("saleCopies", saleCopies);
		// 是否是目的地产品
		model.put("destinationBU", isDestinationBU);
		// 酒店套餐返回份数
		if (isHotel) {
			model.put("hotelNum", copies);
		}

		model.addAttribute("prodProduct", prodProduct);
		model.addAttribute("productId", productId);
		model.addAttribute("specDate", specDate);
		model.put("userId", userId);
		model.put("intentionOrderId", intentionOrderId);
		model.put("canUseCoupons", PropertiesUtil.getValue("canUseCoupons").trim());

		childNum = (Long) model.get("childNum");
		model.put("youhuiQuantity", copies);
		Long personNums = childNum + adultNum;
		model.put("youhuiperson", personNums);
		// ******获取签证材料解释收取时间 开始
		Map<String, Object> pvddParam = new HashMap<String, Object>();
		pvddParam.put("productId", productId);//
		pvddParam.put("specDate", specDate);
		List<ProdVisaDocDate> pvddList = prodVisaDocDateClientService.findProdVisaDocDate(pvddParam, false).getReturnContent();// 根据产品Id和游玩日期获取签证材料截止收取时间,根据这两个条件可以取到一条数据
		log.info("-----pvddList---" + pvddList);
		if (CollectionUtils.isNotEmpty(pvddList)) {
			ProdVisaDocDate prodVisaDocDate = pvddList.get(0);
			log.info("prodVisaDocDate.getLastDate()" + prodVisaDocDate.getLastDate());
			model.addAttribute("visaDocLastTime", prodVisaDocDate.getLastDate());
		}
		// ******获取签证材料解释收取时间 结束
		log.info("---------------加载完成------------------" + saleCopies + "");
		return result;
	}
	
	
	/**
	 * ajax查询定制游产品名称
	 * 
	 * @param search
	 * @param response
	 */
	@RequestMapping("/queryCustomizedProductList")
	public void queryCustomizedProductList(String search, HttpServletResponse response) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		if (NumberUtils.isNumber(search)) {
			long productId = Long.parseLong(search);
			params.put("productId", productId);
		} else {
			params.put("productName", search);
		}
		/*Long[] bizCategoryIdArray = new Long[2];
		bizCategoryIdArray[0] = BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId();
		bizCategoryIdArray[1] = BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId();
		params.put("bizCategoryIdArray", bizCategoryIdArray);*/
		// 定制游
		params.put("bizCategoryId", BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId());
		// 调用定制游联想查询接口
		List<ProdProduct> productList = prodProductClientService.findCustomizedProdProductListByCondition(params);
		// 组装JSON数据BIZ_DISTRICT
		JSONArray jsonArray = new JSONArray();
		if (null != productList && !productList.isEmpty()) {
			for (ProdProduct product : productList) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", product.getProductId());
				jsonObject.put("text", product.getProductName());
				jsonArray.add(jsonObject);
			}
		}
		// 返回JSON数据
		JSONOutput.writeJSON(response, jsonArray);
	}

	/**
	 * 查询产品的可售出发地信息
	 * 
	 * @param prodProduct
	 *            产品信息
	 * @param firstDistrictId
	 *            第一出发地
	 * @return
	 */
	private ProdStartDistrictAdditionalVO adapterStartDistrict(ProdProduct prodProduct, Long firstDistrictId) {
		List<BizDistrict> districts = prodProduct.getBizDistricts();
		Map<Long, BizDistrict> mapCache = new HashMap<Long, BizDistrict>();
		if (districts != null) {
			for (BizDistrict d : districts) {
				d.setCancelFlag("N");
				mapCache.put(d.getDistrictId(), d);
			}
		}

		if (!"Y".equalsIgnoreCase(prodProduct.getMuiltDpartureFlag()) || prodProduct.getProductId() == null) {
			return null;
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("productId", prodProduct.getProductId());
		// 查询可售出发地列表
		ResultHandleT<List<ProdStartDistrictAdditionalVO>> resultT = prodCalClientRemote.getProdStartDistrictForSale(paramMap);
		if (resultT.isFail())
			return null;
		List<ProdStartDistrictAdditionalVO> retList = resultT.getReturnContent();
		if (retList == null || retList.size() <= 0)
			return null;

		// 判断出发地是否可售
		if (districts != null) {
			for (ProdStartDistrictAdditionalVO startVo : retList) {
				BizDistrict d = mapCache.get(startVo.getStartDistrictId());
				if (d != null) {
					d.setCancelFlag("Y");
				}
			}
		}

		for (ProdStartDistrictAdditionalVO startVo : retList) {
			if (firstDistrictId == null && startVo.getStartDistrictId() != null && startVo.getStartDistrictId().longValue() == 9) { // 如果没有设置出发地ID，优先返回
																																	// 上海出发（9）
				return startVo;
			}
			if (firstDistrictId != null && firstDistrictId.equals(startVo.getStartDistrictId())) {
				return startVo;
			}
		}
		return retList.get(0); // 如果没有匹配的出发地，取第一条出发地
	}

	

	

	/**
	 * 
	 * 1.当地游、酒店套餐 ：主规格（成人儿童房差、套餐+附加规格 2.跟团游、自由行：主规格+附加规格 +供应商打包需要通过打包的方式进行商品加载
	 * 3.4种品类的自主打包:需要加载打包信息（自主打包没有主规格和附加规格）
	 * 
	 * @param model
	 * @param productId
	 * @return
	 */
	private Map<String, Object> loadProduct(ModelMap model, String productId, Date specDate, Long adultNum, Long childNum, boolean isHotel, Long startDistrictId,
			HttpServletRequest req) {
		String resultUrl = "";
		long startAll = System.currentTimeMillis();
		String methodName = "OrderCustomizedProductQueryAction#loadProduct-->productId = " + productId;
		// 如果是当地游、酒店套餐走常规门票的处理方式
		// 如果是跟团游、自由行，需要按照自主打包的方式进行处理
		// vo定义
		// 1.包含产品信息、2.包含所有可用规格信息、3.
		PackageTourProductVo lineProductVo = null;

		// 1.供应商产品的标配信息
		List<ProdProductBranch> additionList = null;// 附加
		List<ProdProductBranch> adultChildDiffList = null;// 成人儿童房差
		List<ProdProductBranch> comboDinnerList = null;// 套餐

		// 2.供应商打包的跟团游和自由行的（更换酒店和升级）,（更换酒店一个组对应一个行程，一个组下有多个规格)
		List<ProdPackageGroup> updateProdPackageList = null;
		List<ProdPackageGroup> changeProdPackageList = null;

		// 3.自主打包产品信息（一个组对应一个行程，一个组下有多个规格)
		List<ProdPackageGroup> hotelProdPackageList = null;
		List<ProdPackageGroup> ticketProdPackageList = null;
		List<ProdPackageGroup> transprotProdPackageList = null;

		// 4.线路
		List<ProdPackageGroup> hotelCombPackageList = null;
		List<ProdPackageGroup> groupPackageList = null;
		List<ProdPackageGroup> freedomPackageList = null;
		List<ProdPackageGroup> localPackageList = null;

		// 5.关联销售
		// 关联销售门票的所有
		List<ProdProductBranch> reTicketBranchList = null;
		// 关联销售当地游的所有
		List<ProdProductBranch> reLineBranchList = null;
		// 关联销售交通的所有
		List<ProdProductBranch> reTransportBranchList = null;
		// 关联销售交通的所有
		List<ProdProductBranch> visaBranchList = null;
		// 关联销售门票的详细信息
		Map<String, Object> relSaleDetailMap = null;
		// 关联销售当地游的详细信息
		List<Map<String, Object>> relSaleLocalDetailList = null;

		List<String> relSaleDisplayOrder = new ArrayList<String>();
		// 是否存在关联销售
		boolean hasRelation = false;

		Map<Long, SuppGoodsRelation> relationMap = null;

		Long pdId = Long.valueOf(productId);
		Long categoryId = null;
		boolean isLvmamaProduct = false;
		boolean havechangeButtonFlag = false;// 是否有更换按钮标识
		boolean ifQueryDuijie = false;// 是否查询对接标识，此查询只为判断交通是否可更换

		/* 判断是否含有往返交通 */
		boolean hasRoundTypeTrans = false;
		/* 判断是否含有单程交通 */
		boolean hasOneWayTrans = false;

		boolean hasPackage = false;
		// 跟团游、自由行,供应商打包需要通过打包的方式进行商品加载
		Map<String, List<ProdPackageGroup>> packageMap = null;
		// 以下两按钮只在判断可否更换交通时使用
		PackageTourProductVo lineProductVoTemp = null;
		List<ProdPackageGroup> plistTemp = null;
		try {
			// 传入成人数、儿童数
			long s1 = System.currentTimeMillis();
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("productId", pdId);
			paramMap.put("specDate", specDate);
			paramMap.put("adultQuantity", adultNum);
			paramMap.put("childQuantity", childNum);
			paramMap.put("distributorId", Constant.DIST_BACK_END);
			paramMap.put("startDistrictId", startDistrictId);
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo = prodCalClientRemote.getPackageTourProductVo(paramMap);
			/***
			 * if (ReturnlineProductVo!=null &&
			 * ReturnlineProductVo.getReturnContent() == null) { //重新赋值给specDate
			 * List<ProdGroupDate> aryProdGroupDates=prodGroupDateClientService.
			 * findProdGroupDatesList(pdId).getReturnContent();
			 * for(ProdGroupDate prodGroupDate : aryProdGroupDates){
			 * specDate=prodGroupDate.getSpecDate(); paramMap.put("specDate",
			 * specDate); ReturnlineProductVo=
			 * prodCalClientRemote.getPackageTourProductVo(paramMap);
			 * if(ReturnlineProductVo!=null &&
			 * ReturnlineProductVo.getReturnContent()!=null ){ break; } } }
			 */

			// 如果没有查询到线路产品 返回页面 提示库存不足
			if (ReturnlineProductVo != null && ReturnlineProductVo.getReturnContent() == null) {
				Map<String, Object> resMap = new HashMap<String, Object>();
				resMap.put("specDate", specDate);
				resMap.put("resultUrl", "/order/orderProductQuery/customized/findCustomizedProductDetailList");
				return resMap;

			}

			long s2 = System.currentTimeMillis();
			log.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询定制游产品打包信息", "prodCalClientRemote.getPackageTourProductVo", s2 - s1));
			if (ReturnlineProductVo.getReturnContent() == null) {
				model.addAttribute("ERROR", "商品不可售");
				resultUrl = "order/error";
			} else {
				lineProductVo = ReturnlineProductVo.getReturnContent();
			}
			/** 判断是否有对接机票 */
			boolean isDuijie = false;// 用来判断走不走对接机票查询

			String apiFlag = ReturnlineProductVo.getMsg();
			if (ReturnlineProductVo != null) {
				if (apiFlag != null && ErrorCodeMsg.ERR_FAPI_FRONT_001.equalsIgnoreCase(apiFlag)) {
					isDuijie = true;
				}

				if (Constants.TRANSPORT_CHANGE_001.equalsIgnoreCase(ReturnlineProductVo.getInfoMsg())) {
					havechangeButtonFlag = true;
					hasRoundTypeTrans = true;
				} else if (Constants.TRANSPORT_CHANGE_002.equalsIgnoreCase(ReturnlineProductVo.getInfoMsg())) {
					ifQueryDuijie = true;
					hasRoundTypeTrans = true;
					hasOneWayTrans = true;
				}

				log.info(productId + " havechangeButton -->prodCalClientRemote.getPackageTourProductVo: infoMsg【" + ReturnlineProductVo.getInfoMsg() + "】****" + "errorMsg【"
						+ ReturnlineProductVo.getMsg() + "】");
			}
			// 行程信息
			ProdLineRouteVO prodLineRoute = new ProdLineRouteVO();
			if (CollectionUtils.isNotEmpty(lineProductVo.getProdLineRouteList())) {
				prodLineRoute = lineProductVo.getProdLineRouteList().get(0);
			}

			// 不是对接且该产品支持游玩人后置
			model.addAttribute("isTravellerDelay", !isDuijie && "Y".equals(lineProductVo.getTravellerDelayFlag()));
			model.addAttribute("prodLineRoute", prodLineRoute);
			// 调用康宝霞接口获取排序好的组(查询产品所有组)
			long startTime = System.currentTimeMillis();
			ResultHandleT<List<ProdPackageGroup>> allTrificListReturn = prodPackageGroupClientService.getProdPackageGroupList(pdId, ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
			log.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询定制游产品下打包的组列表,用于排序", "prodPackageGroupClientService.getProdPackageGroupList",
					System.currentTimeMillis() - startTime));

			List<ProdPackageGroup> plist = allTrificListReturn.getReturnContent();
			// 如果既有往返又有单程则更多交通里面既有往返推荐又有自由组合
			if (plist != null) {
				model.addAttribute("groupSize", plist.size());
			}

			// 以下两按钮只在判断可否更换交通时使用
			lineProductVoTemp = lineProductVo;
			plistTemp = plist;

			// 如果非对接不满足，则查询对接机票
			if (isDuijie) {
				// 含有对接机票时处理方法
				String apiFlightResult = havaAPIMesssetHandle(pdId, specDate, adultNum, childNum, lineProductVo, req, startAll, startDistrictId, plist);

				if (apiFlightResult != null) {
					model.addAttribute("ERROR", apiFlightResult);
					resultUrl = "order/error";
				}
			}

			List<ProdProductBranch> prodProductBranchList = lineProductVo.getProdProductBranchList();
			if (prodProductBranchList == null || prodProductBranchList.size() == 0) {
				model.addAttribute("ERROR", "商品不可售");
				resultUrl = "order/error";
			}
			if ("LVMAMA".equalsIgnoreCase(lineProductVo.getPackageType())) {
				isLvmamaProduct = true;
			}
			// 初始化打包组
			packageMap = lineProductVo.getProdPackageGroupMap();

			// 把标配的信息先拿出来，其中酒店套餐只有套餐
			// 对于自主打包的产品没有标配信息
			long s3 = System.currentTimeMillis();
			Map<String, List<ProdProductBranch>> map = this.findProdProductBranchVoMap(prodProductBranchList);
			long s4 = System.currentTimeMillis();
			log.info("时间戳" + startAll + "@线路后台加载商品调用按照规格类型对规格进行分类用时@OrderLineProductQueryAction.findProdProductBranchVoMap@" + (s4 - s3));
			log.info(ComLogUtil.printTraceInfo(methodName, "【非对接】线路后台加载商品调用按照规格类型对规格进行分类", "OrderLineProductQueryAction.findProdProductBranchVoMap", (s4 - s3)));

			if (map == null) {
				model.addAttribute("ERROR", "加载商品信息出错");
				resultUrl = "order/error";
			}

			additionList = map.get("addition");
			adultChildDiffList = map.get("adult_child_diff");
			comboDinnerList = map.get("combo_dinner");

			categoryId = lineProductVo.getBizCategoryId();

			// 自主打包只需要关注打包信息加载，对打包信息需要按照规格进行分组MAP(自主打包肯定有品类id)
			// 根据产品得到组，根据组得到改组的行程，根据行程得到该行程下的规格，根据规格得到产品
			LineProdPackageGroupContainer container = null;
			if (packageMap != null) {
				boolean isSupplier = true;
				if (lineProductVo.getPackageType().equalsIgnoreCase("LVMAMA")) {
					isSupplier = false;
				}
				long s5 = System.currentTimeMillis();

				container = lineProdPackageGroupServiceImpl.initPackageProductMap(specDate, packageMap, isSupplier);
				long s6 = System.currentTimeMillis();
				log.info("时间戳" + startAll + "@线路后台加载商品调用计算打包产品所有组的时间价格表用时@LineProdPackageGroupService.initPackageProductMap@" + (s6 - s5));

				// 供应商打包的升级和可换酒店
				// 得到升级
				updateProdPackageList = container.getUpdateProdPackageList();
				// 得到跟换酒店
				changeProdPackageList = container.getChangeProdPackageList();

				// 得到酒店打包信息
				hotelProdPackageList = container.getHotelProdPackageList();
				// 得到交通
				transprotProdPackageList = container.getTransprotProdPackageList();

				// 如果的是非对接机票 组装成对接机票格式 add by zm 2015-10-14
				try {
					this.jipiaoChangeDuijie(transprotProdPackageList);
				} catch (Exception e) {
					log.error(ExceptionFormatUtil.getTrace(e));
					log.error("将非对接机票转换为对接机票格式的数据 时，发生异常");
				}

				// 得到门票打包信息
				ticketProdPackageList = container.getTicketProdPackageList();

				// 得到线路酒店套餐
				hotelCombPackageList = container.getLineHotelCombPackageList();
				// 得到线路跟团游
				groupPackageList = container.getLineGroupPackageList();
				// 得到线路自由行
				freedomPackageList = container.getLineFreedomPackageList();
				// 得到线路当地游
				localPackageList = container.getLineLocalPackageList();
				hasPackage = container.isHasPackage();
				if (!hasPackage) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group.name().equals(lineProductVo.getBizCategory().getCategoryCode())
							|| BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(lineProductVo.getBizCategory().getCategoryCode()) || BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.name().equalsIgnoreCase(lineProductVo.getBizCategory().getCategoryCode())) {
						hasPackage = true;
					}
				}
			}

			// 处理关联销售信息
			List<SuppGoodsSaleRe> suppGoodsSaleReList = lineProductVo.getSuppGoodsSaleReList();

			Map<String, List<ProdProductBranch>> reSaleBranchListMap = this.getReSaleCategoryMap(suppGoodsSaleReList);
			if (reSaleBranchListMap != null && reSaleBranchListMap.size() > 0) {
				// 关联销售门票的所有
				reTicketBranchList = reSaleBranchListMap.get("ticket");
				// 关联销售当地游的所有
				reLineBranchList = reSaleBranchListMap.get("line");
				// 关联销售交通的所有
				reTransportBranchList = reSaleBranchListMap.get("transport");
				// 关联销售签证的所有
				visaBranchList = reSaleBranchListMap.get("visa");
				hasRelation = true;
			}

			// 构建关联销售门票的详细信息
			relSaleDetailMap = buildRelSaleDetailInfo(suppGoodsSaleReList);
			setRelSaleDisplayOrder(relSaleDisplayOrder);
			// 构建关联销售当地游的详细信息
			relSaleLocalDetailList = buildRelSaleLocalDetailInfo(suppGoodsSaleReList, lineProductVo, specDate);

			// 处理附加信息
			if (isLvmamaProduct && hasPackage && CollectionUtils.isNotEmpty(lineProductVo.getProductBranchList())) {
				additionList = lineProductVo.getProductBranchList();
			}

			// 次规格对应主规格关系Map
			relationMap = this.findProdProductRelationMap(prodProductBranchList);
			// 为所有的附加信息关联父信息对象、时间价格表

			long s7 = System.currentTimeMillis();
			this.initAdditionProductBranchList(relationMap, additionList, specDate, container);

			long s8 = System.currentTimeMillis();
			log.info("时间戳" + startAll + "@线路后台加载商品调用为附加信息增加父对象用时@OrderLineProductQueryAction.initAdditionProductBranchList@" + (s8 - s7));

			List<SuppGoodsSaleRe> insuranceList = getInsuranceList(suppGoodsSaleReList);
			if (CollectionUtils.isNotEmpty(insuranceList)) {
				boolean flag = false;
				for (SuppGoodsSaleRe re : insuranceList) {
					if (CollectionUtils.isNotEmpty(re.getInsSuppGoodsList())) {
						flag = true;
						break;
					}
				}
				model.addAttribute("existsInsurance", flag);
			}
			model.addAttribute("suppGoodsSaleReList", insuranceList);

			// 解决包含项目的问题
			if (lineProductVo.getPropValue() != null) {
				StringBuffer containedItem = new StringBuffer("");
				Map<String, Object> propValueMap = lineProductVo.getPropValue();
				// 处理供应商打包的并且包含交通信息的
				String trafficFlag = (String) propValueMap.get("traffic_flag");
				if (categoryId == 16L) { // 由于当地游产品属性里没有往返上下车属性，故在此直接设置去取交通信息
					trafficFlag = "Y";
				}
				if (trafficFlag != null && "Y".equalsIgnoreCase(trafficFlag)) {
					List<Map> trafficInfoList = getBusStopAddress(lineProductVo.getProductId(), categoryId);
					model.addAttribute("trafficInfoList", trafficInfoList);
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
			model.addAttribute("ERROR", e.getMessage());
			resultUrl = "order/error";
		}

		// 房差总单价
		Long total = 0L;
		// 房差最小份数
		StringBuffer fangchaMin = new StringBuffer("0");
		// 房差份数选择
		List<String> selectRange = new ArrayList<String>();
		// 房差隐藏字符串
		StringBuffer sb = new StringBuffer("");
		Map<String, String> hiddenMain = new HashMap<String, String>();
		// 自主打包
		if ("LVMAMA".equalsIgnoreCase(lineProductVo.getPackageType())) {
			if (packageMap != null && packageMap.size() > 0) {
				Set<String> keys = packageMap.keySet();
				for (String key : keys) {
					List<ProdPackageGroup> prodPackageGroupList = packageMap.get(key);
					if (prodPackageGroupList == null || prodPackageGroupList.size() == 0) {
						continue;
					}
					if ((ProdPackageGroup.GROUPTYPE.LINE.name() + "_group").equals(key) || (ProdPackageGroup.GROUPTYPE.LINE.name() + "_freedom").equals(key)
							|| (ProdPackageGroup.GROUPTYPE.LINE.name() + "_local").equals(key) || ProdPackageGroup.GROUPTYPE.CHANGE.getCode().equalsIgnoreCase(key)
							|| ProdPackageGroup.GROUPTYPE.UPDATE.getCode().equals(key)) {
						String type = key.toLowerCase();
						total = total + this.calGapPrice(prodPackageGroupList, sb, type, fangchaMin, selectRange);
					}
				}
			}
		} else if ("SUPPLIER".equalsIgnoreCase(lineProductVo.getPackageType())) {// 供应商打包
			BizCategory bizCategory = lineProductVo.getBizCategory();
			if ("category_route_group".equalsIgnoreCase(bizCategory.getCategoryCode()) || "category_route_local".equalsIgnoreCase(bizCategory.getCategoryCode())
					|| "category_route_freedom".equalsIgnoreCase(bizCategory.getCategoryCode()) || "category_route_customized".equalsIgnoreCase(bizCategory.getCategoryCode())) {

				long s9 = System.currentTimeMillis();
				// 主规格房差
				if (adultChildDiffList != null && adultChildDiffList.size() > 0) {
					for (int i = 0; i < adultChildDiffList.size(); i++) {
						// 主规格
						ProdProductBranch prodProductBranch = adultChildDiffList.get(i);
						BizBranch bizBranch = branchClientRemote.findBranchById(prodProductBranch.getBranchId()).getReturnContent();
						String attachFlag = bizBranch.getAttachFlag();
						if ("Y".equalsIgnoreCase(attachFlag)) {
							boolean hasGap = false;
							// 取房差值
							Map<String, Long> selectGapPriceMap = prodProductBranch.getSelectGapPriceMap();
							String selectGapQuantityRange = prodProductBranch.getSelectGapQuantityRange();
							Long price = 0L;
							if (selectGapPriceMap != null && selectGapPriceMap.size() > 0 && selectGapQuantityRange != null) {
								// 每个房差的价格
								Set<String> keys = selectGapPriceMap.keySet();
								for (String key : keys) {
									if (key != null) {
										price = selectGapPriceMap.get(key);
										if (price != null) {
											total = total + price;
											hasGap = true;
											break;
										}
									}
								}
								if (hasGap) {
									// 房差份数选择
									selectRange.clear();
									selectRange.add(selectGapQuantityRange.trim());
									// 每个房差的份数
									int fangChaQuantity = 0;
									if (selectGapQuantityRange.startsWith("1")) {
										fangChaQuantity = 1;
										fangchaMin.deleteCharAt(0);
										fangchaMin.append("1");
									}
									sb.append("<div class=\"lvmama-fangcha-price\" data-type=\"" + "adultChild" + "\" groupId=\"" + "001" + "\" id=\"" + "001-001gap"
											+ "\" data-status=\"" + "Y" + "\" data-quantity=\"" + fangChaQuantity + "\" data-fangcha=\"" + PriceUtil.trans2YuanStr(price)
											+ "\"></div>");
								}
							}
						}
					}
				}

				long s10 = System.currentTimeMillis();
				log.info("时间戳" + startAll + "@线路后台加载商品调用主规格房差用时@OrderLineProductQueryAction@" + (s10 - s9));

				// 升级和跟换酒店
				total = total + this.calGapPrice(updateProdPackageList, sb, "update", fangchaMin, selectRange);
				total = total + this.calGapPrice(changeProdPackageList, sb, "change", fangchaMin, selectRange);
			}
		}
		// 房差总单价
		model.addAttribute("fanchaTotalPrice", PriceUtil.trans2YuanStr(total));
		// 房差最小份数
		model.addAttribute("fangchaMin", fangchaMin.toString());
		// 房差份数选择
		if (selectRange.size() > 0) {
			model.addAttribute("selectRange", selectRange.get(0).toString());
		} else {
			model.addAttribute("selectRange", "0");
		}
		// 每份房差隐藏
		hiddenMain.put("fangchaDiv", ("".equals(sb.toString()) ? null : sb.toString()));
		model.addAttribute("hiddenMain", hiddenMain);

		model.addAttribute("packageTourProductVo", lineProductVo);

		// 标配信息
		model.addAttribute("additionList", additionList);
		model.addAttribute("adultChildDiffList", adultChildDiffList);
		model.addAttribute("comboDinnerList", comboDinnerList);

		// 自主打包
		model.addAttribute("updateProdPackageList", updateProdPackageList);
		model.addAttribute("changeProdPackageList", changeProdPackageList);

		model.addAttribute("ticketProdPackageList", ticketProdPackageList);

		model.addAttribute("hotelCombPackageList", hotelCombPackageList);
		model.addAttribute("groupPackageList", groupPackageList);
		model.addAttribute("freedomPackageList", freedomPackageList);
		model.addAttribute("localPackageList", localPackageList);

		model.addAttribute("hotelProdPackageList", hotelProdPackageList);

		// add by zm 2015-10-14
		try {
			log.info("dealJipiaoData处理之前，" + productId + " havechangeButton: model.get('flightMoreGoods')【" + model.get("flightMoreGoods") + "】");
			this.dealJipiaoData(model, transprotProdPackageList, productId);
			log.info("dealJipiaoData处理之后，" + productId + " havechangeButton: model.get('flightMoreGoods')【" + model.get("flightMoreGoods") + "】");
			if (havechangeButtonFlag) {
				model.addAttribute("flightMoreGoods", "Y");
			} else if (!"Y".equals(model.get("flightMoreGoods")) && ifQueryDuijie) {
				log.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：产品有可售的往返，存在单程但不存在单程非对接，若不可更换，则查询单程对接情况");
				try {
					havaAPIMesssetHandle(pdId, specDate, adultNum, childNum, lineProductVoTemp, req, startAll, startDistrictId, plistTemp);
					Map<String, List<ProdPackageGroup>> packageMapTemp = lineProductVo.getProdPackageGroupMap();
					List<ProdPackageGroup> prodPackageGroupListTemp = packageMapTemp.get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
					if (CollectionUtils.isNotEmpty(prodPackageGroupListTemp)) {
						for (ProdPackageGroup p : prodPackageGroupListTemp) {

							List<ProdPackageDetail> details = p.getProdPackageDetails();
							String jiPiaoDuiJieFlag = p.getJiPiaoDuiJieFlag();
							if (CollectionUtils.isNotEmpty(details)) {
								log.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：details.size()【" + details.size() + "】");
								if (details.size() > 1) {
									model.addAttribute("flightMoreGoods", "Y");
								}
								for (ProdPackageDetail detail : details) {

									ProdProductBranch prodProductBranch = detail.getProdProductBranch();
									if (prodProductBranch != null) {
										List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
										if (suppGoodsList != null && suppGoodsList.size() > 0) {

											// 如果是机票对接的组
											if ("Y".equalsIgnoreCase(jiPiaoDuiJieFlag)) {
												Map<String, FlightNoVo> flightNoVoMap = prodProductBranch.getSuppGoodsMap();
												if (flightNoVoMap != null && flightNoVoMap.size() > 0) {
													log.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：flightNoVoMap.size()【" + flightNoVoMap.size() + "】");
													// 规格下商品多于一个则表示交通可更换,
													if (flightNoVoMap.size() > 1) {
														model.addAttribute("flightMoreGoods", "Y");
													}
												}
											}
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					log.info("方法dealJipiaoData," + productId + " havechangeButton: 异常【" + e.getMessage() + "】");
				}
			}
		} catch (Exception e) {
			log.error(ExceptionFormatUtil.getTrace(e));
			log.error("设置flightMoreGoods/transportType/toFlightParam/backFlightParam出现异常。");
		}

		// 如果既有章程交通，又有往返交通，设定havechangeButton为"Y"
		if ((hasOneWayTrans && hasRoundTypeTrans) || havechangeButtonFlag) {
			model.addAttribute("haveChangeButton", "Y");
		}

		model.addAttribute("transprotProdPackageList", transprotProdPackageList);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("isLvmamaProduct", isLvmamaProduct);

		// 关联销售
		model.addAttribute("reTicketBranchList", reTicketBranchList);
		model.addAttribute("reLineBranchList", reLineBranchList);
		model.addAttribute("reTransportBranchList", reTransportBranchList);
		model.addAttribute("visaBranchList", visaBranchList);
		// 关联销售的详细信息
		model.addAttribute("relSaleDetailMap", relSaleDetailMap);
		model.addAttribute("relSaleDisplayOrder", relSaleDisplayOrder);
		model.addAttribute("relSaleLocalDetailList", relSaleLocalDetailList);

		// 是否存在关联销售
		model.addAttribute("hasRelation", hasRelation);

		// 单房差
		Long gapPrice = lineProductVo.getGapPrice();
		if (gapPrice == null) {
			gapPrice = 0L;
		}
		String gapPriceStr = PriceUtil.trans2YuanStr(gapPrice);

		model.addAttribute("gapPrice", gapPriceStr);

		/********************************* 需要快递的商品id列表 *******************************/
		List<Long> needExpressGoodsList = new ArrayList<Long>();
		List<Long> tempList = new ArrayList<Long>();

		// 自主打包
		// 升级
		tempList = LineUtils.getNeedExpressGoodsListForGroup(updateProdPackageList, false);
		needExpressGoodsList.addAll(tempList);

		// 更换酒店
		tempList = LineUtils.getNeedExpressGoodsListForGroup(changeProdPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 门票
		tempList = LineUtils.getNeedExpressGoodsListForGroup(ticketProdPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 酒店套餐
		tempList = LineUtils.getNeedExpressGoodsListForGroup(hotelCombPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 跟团游
		tempList = LineUtils.getNeedExpressGoodsListForGroup(groupPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 自由行
		tempList = LineUtils.getNeedExpressGoodsListForGroup(freedomPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 当地游
		tempList = LineUtils.getNeedExpressGoodsListForGroup(localPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 酒店
		tempList = LineUtils.getNeedExpressGoodsListForGroup(hotelProdPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 大交通
		tempList = LineUtils.getNeedExpressGoodsListForGroup(transprotProdPackageList, true);
		needExpressGoodsList.addAll(tempList);

		// 供应商打包
		// 成人儿童房差
		tempList = LineUtils.getNeedExpressGoodsListForBranch(adultChildDiffList);
		needExpressGoodsList.addAll(tempList);
		// 酒店套餐
		tempList = LineUtils.getNeedExpressGoodsListForBranch(comboDinnerList);
		needExpressGoodsList.addAll(tempList);

		// 附加信息
		tempList = LineUtils.getNeedExpressGoodsListForBranch(additionList);
		needExpressGoodsList.addAll(tempList);

		// 判断是否含有需要快递的商品
		boolean hasExpress = false;
		if (needExpressGoodsList != null && needExpressGoodsList.size() > 0) {
			hasExpress = true;
		}
		model.addAttribute("hasExpress", hasExpress);

		/********************************* 需要快递的商品id列表 *******************************/
		long endAll = System.currentTimeMillis();
		log.info("时间戳" + startAll + "@线路后台加载商品all用时@OrderLineProductQueryAction.loadProduct@" + (endAll - startAll));
		if (hasPackage) {
			resultUrl = "/order/orderProductQuery/customized/findCustomizedProductPackageDetailList";

		} else {
			resultUrl = "/order/orderProductQuery/customized/findCustomizedProductDetailList";
		}
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("specDate", specDate);
		resMap.put("resultUrl", resultUrl);

		return resMap;
	}

	/***
	 * 根据产品或者商品ID查询当前产品/商品的最大数据 最小1
	 * 
	 * @param model
	 * @param productId
	 */
	private void loadPriceAllMonthById(ModelMap model, String productId, Date specDate) {

		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("productId", productId);
			// 以当前的时间为开始点
			params.put("beginDate", new Date());
			Long productIdL = Long.parseLong(productId);
			ResultHandleT<List<ProdGroupDate>> resultHandleT = prodGroupDateClientService.findProdGroupDateListByParam(productIdL, params);

			if (resultHandleT.isFail() || resultHandleT.getReturnContent() == null) {
				msg.raise(resultHandleT.getMsg());
				log.info("in /ord/productQuery/customized/queryCustomizedDetailList.do load timePriceList is null");
			}

			int mothResult = 0;
			int yearResult = 0;
			Calendar c1 = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			if (resultHandleT != null && resultHandleT.getReturnContent() != null) {
				if (resultHandleT.getReturnContent().size() > 0) {
					c1.setTime(resultHandleT.getReturnContent().get(0).getSpecDate());
					Date lastDate = resultHandleT.getReturnContent().get(resultHandleT.getReturnContent().size() - 1).getSpecDate();
					c2.setTime(lastDate);// 获取最后一条数据的日期

					mothResult = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
					yearResult = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
					mothResult = yearResult * 12 + mothResult;
					model.addAttribute("startDaeResult", resultHandleT.getReturnContent().get(0).getSpecDate().getTime());

				}
			}

			if (mothResult < 0)
				mothResult = 0;
			mothResult++;

			model.addAttribute("mothResult", mothResult);

		} catch (Exception e) {
			log.error(ExceptionFormatUtil.getTrace(e));
			log.info("in /ord/productQuery/customized/queryCustomizedDetailList.do load timePriceList have exception");
		}

		/** 日历框根据时间价计算总数据月数 by 李志强 2015-03-09 **/
	}

	/***
	 * 处理含有机票信息的数据
	 * 
	 * @param pdId
	 * @param specDate
	 * @param adultNum
	 * @param childNum
	 * @param lineProductVo
	 */
	private String havaAPIMesssetHandle(Long pdId, Date specDate, Long adultNum, Long childNum, PackageTourProductVo lineProductVo, HttpServletRequest req, Long startAll,
			Long startDistrictId, List<ProdPackageGroup> plist) {
		long startFunc = System.currentTimeMillis();
		Long startTime = System.currentTimeMillis();
		String methodName = "OrderLineProductQueryAction#havaAPIMesssetHandle-->productId = " + pdId;
		String resultStr = null;
		// 打包线路中其他机票组的对接商品的规格列表 这里的成人数儿童数要改成从前台得到，
		List<ProdPackageGroup> listpr = new ArrayList<ProdPackageGroup>();

		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("lvmamaProductId", pdId);
		pMap.put("specDate", specDate);
		pMap.put("adultQuantity", adultNum);
		pMap.put("childQuantity", childNum);
		pMap.put("distributorId", Constant.DIST_BACK_END);
		pMap.put("startDistrictId", startDistrictId);
		ResultHandleT<List<ProdPackageGroup>> returnListpr = new ResultHandleT<List<ProdPackageGroup>>();
		try {
			returnListpr = prodCalClientRemote.getApiFlightProductBranch(pMap);
		} catch (Exception ex) {
			log.error(ExceptionFormatUtil.getTrace(ex));
			returnListpr.setMsg(ex);
		}
		log.info(ComLogUtil.printTraceInfo(methodName, "【对接】查询对接机票产品规格信息", "ProdCalClientService.getApiFlightProductBranch", (System.currentTimeMillis() - startTime)));

		List<ProdPackageGroup> trificList = lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
		List<ProdPackageGroup> plistFilter = new ArrayList<ProdPackageGroup>();
		if (returnListpr.getReturnContent() != null) {
			listpr = returnListpr.getReturnContent();

			AirLineDayVo airLineDayVo = new AirLineDayVo();
			airLineDayVo.setDay(specDate);
			List<TrafficGroupVo> listTrafficGroupVo = new ArrayList<TrafficGroupVo>();
			for (ProdPackageGroup prodPackageGroup : listpr) {
				// 生成交通产品的List
				prodPackageGroup.setJiPiaoDuiJieFlag("Y");
				List<TrafficVo> trafficVoList = createTrafficVoList(prodPackageGroup);
				TrafficGroupVo trafficGroupVo = new TrafficGroupVo();
				trafficGroupVo.setTrafficVoList(trafficVoList);
				if (prodPackageGroup.getProdPackageGroupTransport().getfToStartDate() != null) {
					trafficGroupVo.setGoDate(prodPackageGroup.getProdPackageGroupTransport().getfToStartDate());
				} else {
					trafficGroupVo.setBackDate(prodPackageGroup.getProdPackageGroupTransport().getfBackStartDate());
				}

				trafficGroupVo.setGroupId(prodPackageGroup.getGroupId());
				listTrafficGroupVo.add(trafficGroupVo);
			}
			airLineDayVo.setTrafficGroupVoList(listTrafficGroupVo);
			airLineDayVo.setAdultNum(adultNum);
			airLineDayVo.setChildNum(childNum);
			// 调用机票组实现的接口 airLineDayVo
			List<AirLineDayVo> listAirline = new ArrayList<AirLineDayVo>();
			listAirline.add(airLineDayVo);
			LvfSignVo sign = new LvfSignVo();
			sign.setIp(req.getRemoteAddr());
			sign.setSessionId(req.getSession().getId());
			JSONArray.fromObject(listAirline).toString();

			try {
				log.info("查询最低价航班.sign:" + JSONObject.fromObject(sign).toString());
				log.info("查询最低价航班.listAirline:" + JSONArray.fromObject(listAirline).toString());
			} catch (Exception e) {
				log.info("{}", e);
			}
			startTime = System.currentTimeMillis();
			ResultHandleT<List<AirLineDayVo>> listAirlineRetrun = flightSearchService.queryGroupLowerSalePriceByAirLineDayVoList(sign, listAirline,
					OrderEnum.Requset_TYPE_FLAG.flightsearch.name());
			log.info(ComLogUtil.printTraceInfo(methodName, "【对接】查询最低价航班 (求日期最低核算价)", "FlightSearchService.queryGroupLowerSalePriceByAirLineDayVoList",
					(System.currentTimeMillis() - startTime)));
			try {
				log.info("查询最低价航班.result:" + JSONArray.fromObject(listAirlineRetrun).toString());
			} catch (Exception e) {
				log.info("{}", e);
			}

			if (listAirlineRetrun.getReturnContent() != null && listAirlineRetrun.getReturnContent().size() > 0) {
				airLineDayVo = listAirlineRetrun.getReturnContent().get(0);// 因为只传入一个对象
			}
			// 转换为现有数据模式，(将TrafficGroupVo转换为ProdPackageGroup)，同时按照成人最低价进行排序为
			// List<ProdPackageDetail>
			changeAirLineDayVoToprodPackageGroupList(airLineDayVo, listpr);// changPlatformDataStructure(listpr,airLineDayVo);

			for (ProdPackageGroup pp : listpr) {
				if (pp.getProdPackageDetails() == null || pp.getProdPackageDetails().size() < 1) {
					DateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
					String dateStr = sdf.format(specDate);
					resultStr = "线路产品ID:" + pdId + ",所对应的组ID" + pp.getGroupId() + ",打包了对接机票" + "在需要查询的" + dateStr + "游玩日无满足条件的可售航班,后台无法对该产品进行下单请客服在前台进行下单";
					break;
				}
			}
			if (resultStr != null) {
				return resultStr;
			}
			List<ProdPackageGroup> ApitrificList = listpr;//

			if (!CollectionUtils.isEmpty(plist)) {
				for (ProdPackageGroup p : plist) {
					// 过滤掉往返程，因为进这个方法的都是单程
					if (p != null && p.getProdPackageGroupTransport() != null && !"TOBACK".equals(p.getProdPackageGroupTransport().getTransportType())) {
						plistFilter.add(p);
					}
				}
			}
			if (CollectionUtils.isNotEmpty(trificList)) {

				List<ProdPackageGroup> allTrificList = null;

				allTrificList = new ArrayList<ProdPackageGroup>();// 获取正确排序的组信息
				// //用于存放费非对接和对接组的MAP
				Map<Long, ProdPackageGroup> allMapProdPackageGroup = new HashMap<Long, ProdPackageGroup>();
				for (ProdPackageGroup prodPackageGroup : ApitrificList) {// 将对接组信息放入MAP
					allMapProdPackageGroup.put(prodPackageGroup.getGroupId(), prodPackageGroup);
				}
				for (ProdPackageGroup prodPackageGroup : trificList) {// 将非对接组信息放入MAP
					allMapProdPackageGroup.put(prodPackageGroup.getGroupId(), prodPackageGroup);
				}
				Iterator it = allMapProdPackageGroup.keySet().iterator();
				while (it.hasNext()) {
					allTrificList.add(allMapProdPackageGroup.get(it.next()));
				}
				lineProductVo.getProdPackageGroupMap().put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), allTrificList);
				// 将对接机票转换组数据和现有费对接数据按照上面排序好的组进行赋值
			} else {
				if (CollectionUtils.isNotEmpty(ApitrificList)) {
					lineProductVo.getProdPackageGroupMap().put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), ApitrificList);
				}
			}
			// 如果打包单程所有交通的组数 与 可售的交通组数不相等，则当前交通不可售，不展示数据
			if (plistFilter != null && lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name()) != null) {
				if (plistFilter.size() != lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name()).size()) {
					lineProductVo.getProdPackageGroupMap().remove(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
					resultStr = "产品存在无数据交通组";
				}
			}

		} else if (CollectionUtils.isEmpty(trificList)) {
			resultStr = "产品存在无数据组";
		} else if (!CollectionUtils.isEmpty(trificList) && trificList.size() != plistFilter.size()) {
			resultStr = "产品存在无数据组";
		}

		long endFunc = System.currentTimeMillis();
		log.info("时间戳" + startAll + "@线路后台加载商品用时@OrderLineProductQueryAction.havaAPIMesssetHandle@" + (endFunc - startFunc));
		return resultStr;
	}

	/**
	 * 按照规格类型对规格进行分类
	 * 
	 * @param list
	 * @return
	 */
	private Map<String, List<ProdProductBranch>> findProdProductBranchVoMap(List<ProdProductBranch> list) {
		Map<String, List<ProdProductBranch>> map = new HashMap<String, List<ProdProductBranch>>();
		String branchCode = null;
		List<ProdProductBranch> tempList = null;
		for (ProdProductBranch vo : list) {
			if (vo.getBizBranch() == null) {
				// 加载bizBranch信息
				vo.setBizBranch(branchClientRemote.findBranchById(vo.getBranchId()).getReturnContent());
			}
			if (vo.getBizBranch() == null) {
				return null;
			}
			branchCode = vo.getBizBranch().getBranchCode();
			tempList = map.get(branchCode);
			if (tempList == null) {
				tempList = new ArrayList<ProdProductBranch>();
			}
			tempList.add(vo);
			map.put(branchCode, tempList);
		}
		return map;
	}

	/**
	 * 将非对接机票转换为对接机票格式的数据
	 * 
	 * @param transprotProdPackageList
	 */
	private void jipiaoChangeDuijie(List<ProdPackageGroup> transprotProdPackageList) throws Exception {
		Long tempCategary = 21L;
		if (transprotProdPackageList != null && transprotProdPackageList.size() > 0 && transprotProdPackageList.get(0).getProdPackageDetails() != null
				&& transprotProdPackageList.get(0).getProdPackageDetails().size() > 0 && transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct() != null
				&& tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct().getBizCategoryId())) {
			for (ProdPackageGroup packageGroup : transprotProdPackageList) {
				if ("Y".equals(packageGroup.getJiPiaoDuiJieFlag())) {
					continue;
				}
				if (packageGroup.getProdPackageGroupTransport() != null && StringUtils.equals(packageGroup.getProdPackageGroupTransport().getTransportType(), "TOBACK")) {
					continue;
				}
				packageGroup.setJiPiaoDuiJieFlag("Y");
				List<ProdPackageDetail> details = packageGroup.getProdPackageDetails();
				if (details != null) {
					for (ProdPackageDetail detail : details) {
						FlightNoVo flightNoVo = new FlightNoVo();
						if (detail != null && detail.getProdProductBranch() != null && detail.getProdProductBranch().getSuppGoodsList() != null) {
							Map<String, FlightNoVo> suppGoodsMap = new HashMap<String, FlightNoVo>();
							for (SuppGoods goods : detail.getProdProductBranch().getSuppGoodsList()) {
								suppGoodsMap.put(goods.getSuppGoodsId() + "", flightNoVo);// 组装详情页suppGoodsMap
								detail.getProdProductBranch().setSuppGoodsMap(suppGoodsMap);
							}
						}

						if (detail.getProdProduct() != null && detail.getProdProduct().getProdTrafficVO() != null) {
							List<ProdTrafficGroup> prodTrafficGrouptList = detail.getProdProduct().getProdTrafficVO().getProdTrafficGroupList();
							if (prodTrafficGrouptList != null) {
								for (ProdTrafficGroup prodTrafficGroup : prodTrafficGrouptList) {
									List<ProdTrafficFlight> pfs = prodTrafficGroup.getProdTrafficFlightList();
									if (pfs != null) {
										for (ProdTrafficFlight prodTrafficFlight : pfs) {
											BizFlight bizFlight = prodTrafficFlight.getBizFlight();
											if (bizFlight != null) {

												flightNoVo = orderLineProductQueryUtil.fillFlightNoVo(flightNoVo, bizFlight, prodTrafficFlight, detail.getProdProductBranch());

												SuppGoodsBaseTimePrice baseTimePrice = detail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice();
												SuppGoodsLineTimePriceVo lineTimePrice = (SuppGoodsLineTimePriceVo) baseTimePrice;
												if (lineTimePrice != null
														&& (SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_NO_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())
																|| ("Y".equalsIgnoreCase(lineTimePrice.getOversellFlag())
																		&& (lineTimePrice.getStock() == 0 || lineTimePrice.getStock() == null)))) {
													flightNoVo.setRemain(-1l);// 剩余数
												} else {
													flightNoVo.setRemain(detail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice().getStock());// 剩余数
												}
												flightNoVo.setFoodSupport(false);// 餐食
												flightNoVo.setAdultAmt(detail.getProdProductBranch().getAdultPrice());// 成人价
												flightNoVo.setChildAmt(detail.getProdProductBranch().getChildPrice());// 儿童价
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 对关联销售信息进行分类处理
	 * 
	 * @param suppGoodsSaleReList
	 * @return
	 */
	private Map<String, List<ProdProductBranch>> getReSaleCategoryMap(List<SuppGoodsSaleRe> suppGoodsSaleReList) {
		HashMap<String, List<ProdProductBranch>> reSaleCategoryMap = new HashMap<String, List<ProdProductBranch>>();

		// 门票
		List<ProdProductBranch> ticketBranchList = new ArrayList<ProdProductBranch>();
		// 当地游
		List<ProdProductBranch> lineLocalBranchList = new ArrayList<ProdProductBranch>();
		// 交通
		List<ProdProductBranch> transportBranchList = new ArrayList<ProdProductBranch>();
		// 签证
		List<ProdProductBranch> visaBranchList = new ArrayList<ProdProductBranch>();

		if (CollectionUtils.isNotEmpty(suppGoodsSaleReList)) {
			for (SuppGoodsSaleRe saleRe : suppGoodsSaleReList) {
				// 得到关联销售的品类id，按照品类id归类
				if (saleRe.getReProductBranch() == null) {
					continue;
				}

				BizCategory bizCategory = saleRe.getReProductBranch().getProduct().getBizCategory();

				// 门票的价格是在商品上、只有一个价格销售价，价格区分规则是成人票和儿童票，在票种上区分
				// 当地游主规格是多价格，分为成人价和儿童价
				// 交通是分为成人价和儿童价

				// 门票
				if (ProductCategoryUtil.isTicket(bizCategory.getCategoryCode())) {
					ticketBranchList.add(saleRe.getReProductBranch());
					if (reSaleCategoryMap.get("ticket") == null) {
						reSaleCategoryMap.put("ticket", ticketBranchList);
					}
				}
				// 线路
				if (ProductCategoryUtil.isRoute(bizCategory.getCategoryCode())) {
					lineLocalBranchList.add(saleRe.getReProductBranch());
					if (reSaleCategoryMap.get("line") == null) {
						reSaleCategoryMap.put("line", lineLocalBranchList);
					}
				}
				// 交通
				if (ProductCategoryUtil.isRouteTraffic(bizCategory.getCategoryCode())) {
					transportBranchList.add(saleRe.getReProductBranch());
					if (reSaleCategoryMap.get("transport") == null) {
						reSaleCategoryMap.put("transport", transportBranchList);
					}
				}
				// 签证
				if (ProductCategoryUtil.isVisa(bizCategory.getCategoryCode())) {
					visaBranchList.add(saleRe.getReProductBranch());
					if (reSaleCategoryMap.get("visa") == null) {
						reSaleCategoryMap.put("visa", visaBranchList);
					}
				}

			}
		}
		return reSaleCategoryMap;
	}

	/**
	 * 按照页面显示结构组装关联销售(门票)信息
	 * 
	 * @param suppGoodsSaleReList
	 * @return
	 */
	private Map<String, Object> buildRelSaleDetailInfo(List<SuppGoodsSaleRe> suppGoodsSaleReList) {
		if (suppGoodsSaleReList == null || suppGoodsSaleReList.size() <= 0) {
			return null;
		}
		Map<String, Object> relSaleDetailMap = new HashMap<String, Object>();
		// Map<String, Object> relSaleDetailMap = new TreeMap<String,
		// Object>(new Comparator<Object>() {
		// @Override
		// public int compare(Object o1, Object o2) {
		// if(o1 != null && o1.equals(o2)) {
		// return 0;
		// }
		// if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCnName().equals(o1))
		// {
		// return -1;
		// } else
		// if(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName().equals(o1))
		// {
		// return 1;
		// } else {
		// if(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName().equals(o2))
		// {
		// return -1;
		// }
		// }
		//
		// return 1;
		// }
		// }); //门票详细信息，元素Map的key为门票分类，value为该分类下的门票产品列表
		Map<String, Object> productMap = null;// 产品详细信息，元素Map的key为产品ID，value为该产品下的商品列表
		Map<String, Object> suppGoodsCategoryMap = null;// 商品分类信息(默认显示的/不显示的)
		Map<String, Object> suppGoodsDetailMap = null;// 商品详细信息
		List<Map<String, Object>> defaultGoodsList = null; // 默认显示的(第一条)商品信息
		List<Map<String, Object>> moreGoodsList = null; // 点击“更多”显示的商品信息
		for (SuppGoodsSaleRe suppGoodsSaleRe : suppGoodsSaleReList) {
			ProdProductBranch prodProductBranch = suppGoodsSaleRe.getReProductBranch();
			if (prodProductBranch == null) {
				continue;
			}

			// 判断商品的品类
			BizCategory bizCategory = prodProductBranch.getProduct().getBizCategory();
			// 用来判断单门票，其它票，组合套餐票
			Long categoryId = bizCategory.getCategoryId();

			// 目前只处理门票 & 签证
			if (!BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(categoryId)) {
				continue;
			}
			suppGoodsDetailMap = new HashMap<String, Object>();
			List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();// 推荐商品
			SuppGoods suppGoods = suppGoodsList.get(0);
			String defaultSinglePrice = "0";// 默认单价，页面加载之后显示
			String defaultSelectDate = "";// 默认选择的日期
			suppGoodsDetailMap.put("productId", prodProductBranch.getProduct().getProductId());
			suppGoodsDetailMap.put("productName", prodProductBranch.getProduct().getProductName());
			// 可选择的日期下拉选项列表
			Map<String, Long> selectPriceMap = suppGoods.getSelectPriceMap();// 生成可选select
			Map<String, String> selectDateMap = new TreeMap<String, String>();
			DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
			df.applyPattern("0.00");
			if (selectPriceMap != null && selectPriceMap.size() > 0) {
				Set<String> keys = selectPriceMap.keySet();
				for (String key : keys) {
					if (selectPriceMap.get(key) != null) {
						selectDateMap.put(key, df.format(selectPriceMap.get(key) / 100.00));
					} else {
						selectDateMap.put(key, "0.00");
					}
				}
			}
			Set<String> dateKeys = selectDateMap.keySet();
			for (String key : dateKeys) {
				defaultSelectDate = key;
				defaultSinglePrice = selectDateMap.get(key);
				break;
			}
			suppGoodsDetailMap.put("selectDateMap", selectDateMap);
			suppGoodsDetailMap.put("suppGoods", suppGoods);
			// suppGoodsDetailMap.put("categoryId", categoryId);
			// suppGoodsDetailMap.put("suppGoodsId",suppGoods.getSuppGoodsId());
			// suppGoodsDetailMap.put("suppGoodsName",suppGoods.getGoodsName());
			if (suppGoods.getSuppGoodsAddition() != null) {
				suppGoodsDetailMap.put("lowestSaledPrice", suppGoods.getSuppGoodsAddition().getLowestSaledPrice());
			}

			if (prodProductBranch.getPropValue() != null) {
				suppGoodsDetailMap.put("description", prodProductBranch.getPropValue().get("branch_desc"));
			} else {
				suppGoodsDetailMap.put("description", "");
			}

			String selectQuantityRange = suppGoods.getSelectQuantityRange();
			int defaultQuantity = 0;
			List<String> selectQuantityList = new ArrayList<String>();
			suppGoodsDetailMap.put("selectQuantityList", selectQuantityList);
			if (selectQuantityRange != null) {
				String[] ranges = selectQuantityRange.split(",");
				if (ranges != null && ranges.length > 0) {
					defaultQuantity = Integer.parseInt(ranges[0]);// 得到默认的选择的份数
					// 默认加载时的份数
					suppGoodsDetailMap.put("defaultQuantity", defaultQuantity);
					for (String qunantity : ranges) {
						selectQuantityList.add(qunantity);
					}
				}
			}
			suppGoodsDetailMap.put("prodProductBranch", prodProductBranch);
			suppGoodsDetailMap.put("defaultSelectDate", defaultSelectDate);
			suppGoodsDetailMap.put("defaultSinglePrice", defaultSinglePrice);
			double defaultTotalPrice = Double.parseDouble(defaultSinglePrice) * defaultQuantity;
			suppGoodsDetailMap.put("defaultTotalPrice", df.format(defaultTotalPrice));

			productMap = (Map<String, Object>) relSaleDetailMap.get(bizCategory.getCategoryName());
			if (productMap == null) {
				productMap = new HashMap<String, Object>();
				relSaleDetailMap.put(bizCategory.getCategoryName(), productMap);
			}
			suppGoodsCategoryMap = (Map<String, Object>) productMap.get(String.valueOf(prodProductBranch.getProduct().getProductId()));
			if (suppGoodsCategoryMap == null) {
				suppGoodsCategoryMap = new HashMap<String, Object>();
				suppGoodsCategoryMap.put("product", prodProductBranch.getProduct());
				productMap.put(String.valueOf(prodProductBranch.getProduct().getProductId()), suppGoodsCategoryMap);
			}

			defaultGoodsList = (List<Map<String, Object>>) suppGoodsCategoryMap.get("defaultGoodsList");
			moreGoodsList = (List<Map<String, Object>>) suppGoodsCategoryMap.get("moreGoodsList");

			if (defaultGoodsList == null || defaultGoodsList.size() <= 0) {
				defaultGoodsList = new ArrayList<Map<String, Object>>();
				suppGoodsCategoryMap.put("defaultGoodsList", defaultGoodsList);
				defaultGoodsList.add(suppGoodsDetailMap);
			} else {
				if (moreGoodsList == null) {
					moreGoodsList = new ArrayList<Map<String, Object>>();
					suppGoodsCategoryMap.put("moreGoodsList", moreGoodsList);
				}
				moreGoodsList.add(suppGoodsDetailMap);
			}

		}
		return relSaleDetailMap;
	}

	private void setRelSaleDisplayOrder(List<String> relSaleDisplayOrder) {
		if (relSaleDisplayOrder == null) {
			return;
		}

		relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCnName());
		relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCnName());
		relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName());
		relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCnName());
	}

	private List<Map<String, Object>> buildRelSaleLocalDetailInfo(List<SuppGoodsSaleRe> suppGoodsSaleReList, ProdProduct product, Date visitDate) {
		if (suppGoodsSaleReList == null || suppGoodsSaleReList.size() <= 0) {
			return null;
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> productMap = null;// 产品详细信息，元素Map的key为产品ID，value为该产品下的商品列表
		List<Map<String, Object>> goodsList = null; // 商品列表
		Map<String, Object> suppGoodsDetailMap = null;// 商品详细信息

		Map<String, String> auditPriceMap = null;
		Map<String, String> childPriceMap = null;
		Map<String, String> gapPriceMap = null;
		List<String> selectDateList = null;

		String selectAdultQuantityRange = null;
		String selectChildQuantityRange = null;
		String selectGapQuantityRange = null;

		goodsList = new ArrayList<Map<String, Object>>();

		for (SuppGoodsSaleRe suppGoodsSaleRe : suppGoodsSaleReList) {
			ProdProductBranch prodProductBranch = suppGoodsSaleRe.getReProductBranch();
			if (prodProductBranch == null) {
				continue;
			}
			// 判断商品的品类
			BizCategory bizCategory = prodProductBranch.getProduct().getBizCategory();
			// 用来判断是否是当地游
			Long categoryId = bizCategory.getCategoryId();
			// 只处理当地游
			if (!BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)) {
				continue;
			}

			suppGoodsDetailMap = new HashMap<String, Object>();

			List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();// 推荐商品
			SuppGoods suppGoods = suppGoodsList.get(0);
			suppGoodsDetailMap.put("suppGoods", suppGoods);
			suppGoodsDetailMap.put("productId", prodProductBranch.getProduct().getProductId());
			suppGoodsDetailMap.put("productName", prodProductBranch.getProduct().getProductName());

			Map<String, SuppGoodsLineTimePriceVo> localSelectPriceMap = suppGoods.getGoodsLocalSelectPriceMap();// 获取时间价格表

			selectDateList = new ArrayList<String>();// 生成可选select
			auditPriceMap = new TreeMap<String, String>();// 成人价map
			childPriceMap = new TreeMap<String, String>();// 儿童价map
			gapPriceMap = new TreeMap<String, String>();// 儿童价map

			selectAdultQuantityRange = suppGoods.getSelectAdultQuantityRange();// 成人数量select
			selectChildQuantityRange = suppGoods.getSelectChildQuantityRange();// 儿童数量select
			selectGapQuantityRange = suppGoods.getSelectGapQuantityRange();// 房差数量select

			SuppGoodsLineTimePriceVo suppGoodsLineTimePriceVo = new SuppGoodsLineTimePriceVo();
			DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
			df.applyPattern("0.00");
			if (localSelectPriceMap != null && localSelectPriceMap.size() > 0) {
				Set<String> keys = localSelectPriceMap.keySet();
				for (String key : keys) {
					selectDateList.add(key);// 添加日期
					suppGoodsLineTimePriceVo = localSelectPriceMap.get(key);// 获取具体某一天的时间价格表

					if (suppGoodsLineTimePriceVo.getAuditPrice() != null) {
						auditPriceMap.put(key, df.format(suppGoodsLineTimePriceVo.getAuditPrice() / 100.00));
					}
					if (suppGoodsLineTimePriceVo.getChildPrice() != null) {
						childPriceMap.put(key, df.format(suppGoodsLineTimePriceVo.getChildPrice() / 100.00));
					}
					if (suppGoodsLineTimePriceVo.getGapPrice() != null) {
						gapPriceMap.put(key, df.format(suppGoodsLineTimePriceVo.getGapPrice() / 100.00));
					}
				}
			}

			// 关联销售当地游增加出游时间限制
			filterOptinalRouteLocal(product, selectDateList, suppGoodsSaleRe.getLimitDays(), visitDate);
			if (selectDateList.isEmpty()) {
				continue;
			}
			suppGoodsDetailMap.put("selectDateList", selectDateList);

			suppGoodsDetailMap.put("auditPriceMap", auditPriceMap);
			suppGoodsDetailMap.put("childPriceMap", childPriceMap);
			suppGoodsDetailMap.put("gapPriceMap", gapPriceMap);

			suppGoodsDetailMap.put("selectAdultQuantityRange", selectAdultQuantityRange);
			suppGoodsDetailMap.put("selectChildQuantityRange", selectChildQuantityRange);
			suppGoodsDetailMap.put("selectGapQuantityRange", selectGapQuantityRange);

			goodsList.add(suppGoodsDetailMap);

		}
		return goodsList;
	}

	/**
	 * 对所有主规格进行遍历，制作次规格对应主规格的对应关系<次规格商品id，对应关系对象>
	 * 
	 * @param list
	 * @return
	 */
	private Map<Long, SuppGoodsRelation> findProdProductRelationMap(List<ProdProductBranch> list) {
		Map<Long, SuppGoodsRelation> relationMap = new HashMap<Long, SuppGoodsRelation>();
		for (ProdProductBranch vo : list) {
			if (vo.getBizBranch() == null) {
				continue;
			}
			if (vo.getBizBranch().getAttachFlag().equals("Y") && CollectionUtils.isNotEmpty(vo.getSuppGoodsList())) {
				for (SuppGoods goods : vo.getSuppGoodsList()) {
					if (CollectionUtils.isNotEmpty(goods.getSuppGoodsRelationList())) {
						for (SuppGoodsRelation relation : goods.getSuppGoodsRelationList()) {
							relationMap.put(relation.getSecGoodsId(), relation);
						}
					}
				}
			}
		}
		return relationMap;
	}

	/**
	 * 为附加信息增加父对象
	 * 
	 * @param relationMap
	 * @param additionList
	 * @param specDate
	 */
	private void initAdditionProductBranchList(Map<Long, SuppGoodsRelation> relationMap, List<ProdProductBranch> additionList, Date specDate,
			LineProdPackageGroupContainer container) {

		SuppGoodsRelation relation = null;
		// 为附加信息增加父对象
		if (CollectionUtils.isNotEmpty(additionList)) {
			for (ProdProductBranch branch : additionList) {
				if (branch.getSuppGoodsList() != null) {
					for (SuppGoods goods : branch.getSuppGoodsList()) {
						relation = relationMap.get(goods.getSuppGoodsId());
						if (relation == null) {
							relation = new SuppGoodsRelation();
							relation.setRelationType("OPTIONAL");
						}
						goods.setParentGoodsRelation(relation);
					}
				}
			}
		}
		// 为附加信息增加时间价格表,能够更具附加信息的产品id找到该产品主规格的默认出游时间
		if (CollectionUtils.isNotEmpty(additionList)) {
			for (ProdProductBranch branch : additionList) {
				if (branch.getSuppGoodsList() != null) {
					if (container != null) {
						Date tempDate = container.getProductTimeMapItem(branch.getProductId());
						if (tempDate != null) {
							specDate = tempDate;
						}
					}

					this.lineProdPackageGroupServiceImpl.initSupplierProdBranchTimePrice(branch, specDate, GROUPTYPE.LINE);
					// 设置时间单价
					Map newMap = LineUtils.change(branch.getSelectPriceMap());
					branch.setSelectPriceMap(newMap);
				}
			}
		}
	}

	private List<SuppGoodsSaleRe> getInsuranceList(List<SuppGoodsSaleRe> suppGoodsSaleReList) {
		List<SuppGoodsSaleRe> list = new ArrayList<SuppGoodsSaleRe>();
		if (CollectionUtils.isNotEmpty(suppGoodsSaleReList)) {
			for (SuppGoodsSaleRe saleRe : suppGoodsSaleReList) {
				if (CollectionUtils.isNotEmpty(saleRe.getInsSuppGoodsList())) {
					list.add(saleRe);
				}
			}
		}
		return list;
	}

	// 交通
	private List<Map> getBusStopAddress(Long productId, Long categoryId) {
		List<Map> trafficList = new ArrayList<Map>();
		ProdTrafficVO trafficVO = prodTrafficClientServiceRemote.getProdTrafficVOByProductId(productId);
		if (trafficVO == null) {
			return null;
		}
		ProdTraffic prodTraffic = trafficVO.getProdTraffic();// 交通信息表
		Map<String, Object> suppGoodsInfoMap = new HashMap<String, Object>();

		trafficList.add(suppGoodsInfoMap);

		Map<String, Object> trafficMap = new HashMap<String, Object>();
		suppGoodsInfoMap.put("trafficMap", trafficMap);
		Map<String, Object> toMap = new HashMap<String, Object>();// 去返程信息
		Map<String, Object> backMap = new HashMap<String, Object>();// 返程信息
		List<Map<String, Object>> toBuses = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> backBuses = new ArrayList<Map<String, Object>>();
		trafficMap.put("toMap", toMap);
		trafficMap.put("backMap", backMap);

		String backType = prodTraffic.getBackType();
		String toType = prodTraffic.getToType();

		if (SUB_PRODUCT_TYPE.BUS.getCode().equals(backType) || SUB_PRODUCT_TYPE.BUS.getCode().equals(toType)) {
			trafficMap.put("trafficType", "bus");
			List<Map<String, String>> buses = new ArrayList<Map<String, String>>();
			List<ProdTrafficGroup> prodTrafficGroups = trafficVO.getProdTrafficGroupList();
			int toCount = 0;
			int backCount = 0;
			for (ProdTrafficGroup ptg : prodTrafficGroups) {
				List<ProdTrafficBus> prodTrafficBusList = ptg.getProdTrafficBusList();
				if (prodTrafficBusList != null && prodTrafficBusList.size() > 0) {

					// 现在最多只会有一个中转,所以可以按照下面的写法做
					for (ProdTrafficBus prodTrafficBus : prodTrafficBusList) {
						if ("TO".equals(prodTrafficBus.getTripType())) {
							Map<String, String> bus = new HashMap<String, String>();
							/*
							 * bus.put("date", startDate);// 去程日期
							 */ bus.put("address", prodTrafficBus.getAdress());// 上车地点
							bus.put("startTime", prodTrafficBus.getStartTime());// 上车时间
							bus.put("memo", prodTrafficBus.getMemo());
							buses.add(bus);
							toCount++;
							// break;
						}
					}

				}
			}

			for (ProdTrafficGroup ptg : prodTrafficGroups) {
				List<ProdTrafficBus> prodTrafficBusList = ptg.getProdTrafficBusList();
				if (prodTrafficBusList != null && prodTrafficBusList.size() > 0) {
					for (ProdTrafficBus prodTrafficBus : prodTrafficBusList) {
						if ("BACK".equals(prodTrafficBus.getTripType())) {
							/* backMap.put("date", endDate);// 返程日期 */
							Map<String, String> bus = new HashMap<String, String>();
							backMap.put("address", prodTrafficBus.getAdress());// 上车地点
							backMap.put("startTime", prodTrafficBus.getStartTime());// 上车时间
							if (categoryId == 16L) { // 如果品类id为当地游时
								Map<String, Object> backItem = new HashMap<String, Object>();
								backItem.put("address", prodTrafficBus.getAdress());// 上车地点
								backItem.put("startTime", prodTrafficBus.getStartTime());// 上车时间
								backItem.put("memo", prodTrafficBus.getMemo());
								backBuses.add(backItem);
							}
							backCount++;
						}
						if ("TO".equals(prodTrafficBus.getTripType())) {
							// toMap.put("date", startDate);// 去程日期
							toMap.put("address", prodTrafficBus.getAdress());// 上车地点
							toMap.put("startTime", prodTrafficBus.getStartTime());// 上车时间
							if (categoryId == 16L) {
								Map<String, Object> toItem = new HashMap<String, Object>();
								// backItem.put("date", startDate);// 去程日期
								toItem.put("address", prodTrafficBus.getAdress());// 上车地点
								toItem.put("startTime", prodTrafficBus.getStartTime());// 上车时间
								toItem.put("memo", prodTrafficBus.getMemo());
								toBuses.add(toItem);
							}
						}
					}
				}
			}
			if (toCount > 0 && backCount > 0) {
				trafficMap.put("toBackTyep", "Y");
			}
			trafficMap.put("bus", buses);
			trafficMap.put("toBuses", toBuses);
			trafficMap.put("backBuses", backBuses);
		}
		log.info("==trafficList==" + trafficList);
		return trafficList;
	}

	private Long calGapPrice(List<ProdPackageGroup> prodPackageGroups, StringBuffer sb, String type, StringBuffer fancha, List<String> selectRange) {
		Long totalPrice = 0L;
		if (prodPackageGroups != null && prodPackageGroups.size() > 0) {
			// 1.供应商打包(升级/可换酒店) 2.自主打包的跟团游、自由行、升级和可换酒店
			for (ProdPackageGroup packageGroup : prodPackageGroups) {
				List<ProdPackageDetail> packageDetails = packageGroup.getProdPackageDetails();
				if (packageDetails != null && packageDetails.size() > 0) {
					for (int i = 0; i < packageDetails.size(); i++) {
						ProdPackageDetail ppd = packageDetails.get(i);
						ProdProductBranch prodProductBranch = ppd.getProdProductBranch();
						if (prodProductBranch != null) {
							String status = "Y";
							boolean hasGap = false;
							// 取房差值
							Map<String, Long> selectGapPriceMap = prodProductBranch.getSelectGapPriceMap();
							String selectGapQuantityRange = prodProductBranch.getSelectGapQuantityRange();
							Long price = 0L;
							Long price1 = 0L;
							StringBuffer timePriceSelect = new StringBuffer("");
							if (selectGapPriceMap != null && selectGapPriceMap.size() > 0 && selectGapQuantityRange != null) {
								// 每个房差的价格
								Set<String> keys = selectGapPriceMap.keySet();
								for (String key : keys) {
									if (key != null) {
										price = selectGapPriceMap.get(key);
										if (price != null) {
											if ("update".equalsIgnoreCase(type)) {
												status = "N";
											}
											if ("change".equalsIgnoreCase(type) && i != 0) {
												status = "N";
											}
											if ("line_local".equalsIgnoreCase(type)) {
												if (i != 0) {
													status = "N";
												} else {
													// 取日期列表
													// (仅当地游有日期选择),不是日期列表第一个日期对应的房差则默认不计算总房差价
													List<String> selectDateList = prodProductBranch.getSelectDateList();
													if (selectDateList != null && selectDateList.size() > 0) {
														if (!key.equalsIgnoreCase(selectDateList.get(0))) {
															status = "N";
														}
													}
												}
											}
											if ("Y".equals(status)) {
												totalPrice = totalPrice + price;
											}
											hasGap = true;
											break;
										}
									}
								}
								// 满足当地游日期选择后房差价随之改变
								if ("line_local".equalsIgnoreCase(type)) {
									// head部：时间对应价格
									if (timePriceSelect.length() < 1) {
										timePriceSelect.append(
												"<select class=\"timePriceSelect\" style=\"display:none;\" id=\"" + ppd.getGroupId() + "-" + ppd.getDetailId() + "tps" + "\">");
									}
									for (String key : keys) {
										if (key != null) {
											price1 = selectGapPriceMap.get(key);
											if (price1 != null) {
												timePriceSelect.append("<option class=\"" + ppd.getGroupId() + "-" + ppd.getDetailId() + "tpo" + "\" groupId=\"" + ppd.getGroupId()
														+ "\" detailId=\"" + ppd.getDetailId() + "\" theTime=\"" + key + "\" thePrice=\"" + PriceUtil.trans2YuanStr(price1)
														+ "\"></option>");
											}
										}
									}
									// 尾部：时间对应价格
									timePriceSelect.append("</select>");
								}
								if (hasGap) {
									// 房差份数选择
									selectRange.clear();
									selectRange.add(selectGapQuantityRange.trim());
									// 每个房差的份数
									int fangChaQuantity = 0;
									if (selectGapQuantityRange.startsWith("1")) {
										fangChaQuantity = 1;
										fancha.deleteCharAt(0);
										fancha.append("1");
									}
									sb.append("<div class=\"lvmama-fangcha-price\" data-type=\"" + type + "\" groupId=\"" + ppd.getGroupId() + "\" id=\"" + ppd.getGroupId() + "-"
											+ ppd.getDetailId() + "gap" + "\" data-status=\"" + status + "\" data-quantity=\"" + fangChaQuantity + "\" data-fangcha=\""
											+ PriceUtil.trans2YuanStr(price) + "\"></div>");
									sb.append(timePriceSelect.toString());
								}
							}
						}
					}
				}
			}
			return totalPrice;
		}
		return totalPrice;
	}

	/**
	 * 交通类型为其他机票时处理价格，商品ID等数据
	 * 
	 * @param transprotProdPackageList
	 */
	private void dealJipiaoData(ModelMap model, List<ProdPackageGroup> transprotProdPackageList, String productId) throws Exception {
		Long tempCategary = 21l;
		// 如果品类是其他机票则处理数据，否则直接结束本方法
		if (CollectionUtils.isNotEmpty(transprotProdPackageList) && CollectionUtils.isNotEmpty(transprotProdPackageList.get(0).getProdPackageDetails())
				&& transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch() != null
				&& transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch() != null
				&& tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())) {
			model.addAttribute("isFlight", "true");

			// 往返程还是单程标识
			String flag = null;
			for (ProdPackageGroup prodPackageGroup : transprotProdPackageList) {
				List<ProdPackageDetail> details = prodPackageGroup.getProdPackageDetails();
				if (CollectionUtils.isNotEmpty(details)) {
					log.info(productId + " havechangeButton:组【" + prodPackageGroup.getGroupId() + "】下details.size()【" + details.size() + "】");
					if (details.size() > 1) {
						model.addAttribute("flightMoreGoods", "Y");
					} else {
						for (ProdPackageDetail ppd : details) {
							ProdProductBranch branch = ppd.getProdProductBranch();
							Map<String, FlightNoVo> suppGoodsMap = null;
							if (branch != null) {
								int size = suppGoodsMap == null ? 0 : suppGoodsMap.size();
								log.info(productId + " havechangeButton:组【" + prodPackageGroup.getGroupId() + "】下detail【" + ppd.getDetailId() + "】下branch【" + branch.getBranchId()
										+ "】下suppGoodsMap.size()【" + size + "】");

								suppGoodsMap = branch.getSuppGoodsMap();
								if (suppGoodsMap != null && suppGoodsMap.size() > 1) {
									model.addAttribute("flightMoreGoods", "Y");
								}
							}
						}
					}
				}

				if (prodPackageGroup.getProdPackageGroupTransport() != null && prodPackageGroup.getProdPackageGroupTransport().getTransportType() != null) {
					flag = prodPackageGroup.getProdPackageGroupTransport().getTransportType();
				}
			}

			model.addAttribute("transportType", flag);
			if ("TOBACK".equals(flag)) {// 往返程，价格取其一半
				long price = transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getAdultPrice();
				model.addAttribute("toPrice", price / 2);
				model.addAttribute("backPrice", price / 2);
			} else if ("TO".equals(flag)) {// 单程，设置往返程的价格产品ID商品ID等信息

				FlightParam toFlightParam = new FlightParam();
				FlightParam backFlightParam = new FlightParam();
				for (ProdPackageGroup p : transprotProdPackageList) {
					ProdPackageDetail detail = p.getProdPackageDetails().get(0);
					ProdPackageGroupTransport transport = p.getProdPackageGroupTransport();
					log.info("productId[" + productId + "] ProdPackageGroupTransport.toString()【" + transport.toString() + "】");
					if (isToType(transport)) {
						Long suppId = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
						FlightNoVo goods = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId + "");
						toFlightParam.setAdultPrice(goods.getAdultAmt());
						toFlightParam.setChildPrice(goods.getChildAmt());
						toFlightParam.setPackageGroupId(detail.getGroupId());
						toFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
						toFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
						toFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
					}
					if (isBackType(transport)) {
						Long suppId = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
						FlightNoVo goods = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId + "");
						backFlightParam.setAdultPrice(goods.getAdultAmt());
						backFlightParam.setChildPrice(goods.getChildAmt());
						backFlightParam.setPackageGroupId(detail.getGroupId());
						backFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
						backFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
						backFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
					}

					if (!isToType(transport) && !isBackType(transport)) {
						log.error("productId[" + productId + "]加载默认交通时ERROR 单程交通 既无返程也无去程信息。");
					}
				}

				// 交通组排序，去程排在返程前面
				Collections.sort(transprotProdPackageList, new Comparator<ProdPackageGroup>() {
					public int compare(ProdPackageGroup o1, ProdPackageGroup o2) {
						if (o1.getProdPackageGroupTransport() != null && isToType(o1.getProdPackageGroupTransport())) {
							return -1;
						} else {
							return 1;
						}
					}
				});
				log.error("productId[" + productId + "],toFlightParam== " + toFlightParam.toString() + "\n backFlightParam== " + backFlightParam.toString());
				model.addAttribute("toFlightParam", toFlightParam);
				model.addAttribute("backFlightParam", backFlightParam);
			}
		} else {
			if (CollectionUtils.isEmpty(transprotProdPackageList)) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList为空");
				return;
			}
			if (CollectionUtils.isNotEmpty(transprotProdPackageList) && CollectionUtils.isEmpty(transprotProdPackageList.get(0).getProdPackageDetails())) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails() 为空");
				return;
			}
			if (transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch() == null) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch() 为空");
				return;
			}
			if (transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch() == null) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch() 为空");
				return;
			}
			if (!tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())) {
				log.error("productId[" + productId
						+ "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId() 不是机票21L");
				return;
			}
			if (transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct() == null) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct() 为空");
				return;
			}
			if (!tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct().getBizCategoryId())) {
				log.error("productId[" + productId + "]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct().getBizCategoryId()不是 机票21L");
				return;
			}
		}
	}

	/**
	 * 生成交通产品的VO，填充其规格
	 * 
	 * @param prodPackageGroup
	 * @return
	 */
	private List<TrafficVo> createTrafficVoList(ProdPackageGroup prodPackageGroup) {
		List<TrafficVo> trafficVoList = new ArrayList<TrafficVo>();
		if (prodPackageGroup == null) {
			return trafficVoList;
		}
		// 用来存储产品ID对应规格列表的Map
		Map<Long, List<BranchVo>> transformResultMap = new HashMap<Long, List<BranchVo>>();
		List<ProdPackageDetail> prodPackageDetailList = prodPackageGroup.getProdPackageDetails();
		if (prodPackageDetailList != null && prodPackageDetailList.size() > 0) {
			List<BranchVo> branchVoList = null;
			for (ProdPackageDetail prodPackageDetail : prodPackageDetailList) {
				Long productId = prodPackageDetail.getProdProductBranch().getProductId();
				Long productBranchId = prodPackageDetail.getProdProductBranch().getProductBranchId();
				BranchVo branchVo = new BranchVo();
				branchVo.setBranchId(productBranchId);
				branchVoList = transformResultMap.get(productId);
				if (branchVoList == null || branchVoList.size() == 0) {
					branchVoList = new ArrayList<BranchVo>();
					transformResultMap.put(productId, branchVoList);
				}
				branchVoList.add(branchVo);
			}
		}
		if (transformResultMap != null && transformResultMap.size() > 0) {
			Set<Long> keys = transformResultMap.keySet();
			for (Long key : keys) {
				TrafficVo trafficVo = new TrafficVo();
				trafficVo.setTrafficId(key);// 设置产品ID
				trafficVo.setBranchVoList(transformResultMap.get(key));
				trafficVoList.add(trafficVo);
			}
		}
		return trafficVoList;
	}

	/**
	 * 转换为现有数据模式，(将AirLineDayVo转换为prodPackageGroupList)，同时按照成人最低价进行排序为 List
	 * <ProdPackageDetail>
	 * 
	 * @param airLineDayVo
	 * @param planePackageGroupList
	 */
	private void changeAirLineDayVoToprodPackageGroupList(AirLineDayVo airLineDayVo, List<ProdPackageGroup> planePackageGroupList) {
		// 用来存储产品ID对应规格列表的Map
		Map<Long, List<BranchVo>> transformResultMap = new HashMap<Long, List<BranchVo>>();
		List<TrafficGroupVo> trafficGroupVoList = airLineDayVo.getTrafficGroupVoList();
		Long audlltNum = airLineDayVo.getAdultNum();
		Long childNum = airLineDayVo.getChildNum();

		if (trafficGroupVoList != null && trafficGroupVoList.size() > 0) {
			for (TrafficGroupVo trafficGroupVo : trafficGroupVoList) {
				if (trafficGroupVo != null) {
					List<TrafficVo> trafficVoList = trafficGroupVo.getTrafficVoList();
					if (trafficVoList != null && trafficVoList.size() > 0) {
						for (TrafficVo trafficVo : trafficVoList) {
							Long productId = trafficVo.getTrafficId();
							List<BranchVo> branchVoList = trafficVo.getBranchVoList();
							// branchVoList = transformResultMap.get(productId);
							transformResultMap.put(productId, branchVoList);
						}
					}
				}
			}
		}
		// 修改商品的成人价 并对商品进行排序
		if (planePackageGroupList != null && planePackageGroupList.size() > 0) {
			for (ProdPackageGroup prodPackageGroup : planePackageGroupList) {
				List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
				List<ProdPackageDetail> prodNoPackageDetails = new ArrayList<ProdPackageDetail>();
				if (prodPackageDetails != null && prodPackageDetails.size() > 0) {
					for (ProdPackageDetail prodPackageDetail : prodPackageDetails) {
						ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
						Long productBranchId = prodProductBranch.getProductBranchId();
						Long productId = prodProductBranch.getProductId();
						List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
						Map<String, FlightNoVo> flightNoVoMap = prodProductBranch.getSuppGoodsMap();
						List<BranchVo> branchVoList = transformResultMap.get(productId);
						flightNoVoMap = new HashMap<String, FlightNoVo>();
						if (branchVoList != null && branchVoList.size() > 0) {
							for (BranchVo branchVo : branchVoList) {
								Long branchId = branchVo.getBranchId();
								if (productBranchId.intValue() == branchId.intValue()) {
									List<FlightNoVo> flightNoVoList = branchVo.getGoodsList();
									if (flightNoVoList != null && flightNoVoList.size() > 0) {
										for (FlightNoVo flightNoVo : flightNoVoList) {
											Long goodsId = flightNoVo.getGoodsId();

											flightNoVoMap.put(goodsId + "", flightNoVo);
										}

										prodProductBranch.setSuppGoodsMap(flightNoVoMap);

									}

									/* ====原逻辑是取vst和机票接口的交集,现在改为并集 start===== */
									Iterator<String> goodsIds = flightNoVoMap.keySet().iterator();
									while (goodsIds.hasNext()) {
										SuppGoods tempGoods = new SuppGoods();
										tempGoods.setSuppGoodsId(Long.parseLong(goodsIds.next()));
										if (suppGoodsList == null) {
											suppGoodsList = new ArrayList<SuppGoods>();
										}
										suppGoodsList.add(tempGoods);
									}
									/* ====原逻辑是取vst和机票接口的交集,现在改为并集 end ===== */

									if (suppGoodsList != null && suppGoodsList.size() > 0) {
										Long lowestAdultAmt = -9999L;// 最低价
										SuppGoods lowestSuppGoods = null;// 最低价的商品
										for (SuppGoods suppGoods : suppGoodsList) {
											Long suppGoodsId = suppGoods.getSuppGoodsId();
											FlightNoVo flightNoVo = flightNoVoMap.get(suppGoodsId + "");
											if (flightNoVo != null && flightNoVo.getAdultAmt() != null) {
												// 成人价
												Long adultAmt = flightNoVo.getAdultAmt();
												// 儿童价
												Long childAmt = flightNoVo.getChildAmt();
												suppGoods.setDailyLowestPrice(adultAmt);

												if (childNum != null && childNum > 0) {

													if (lowestAdultAmt.intValue() == -9999 && childAmt != null && childAmt >= 0) {
														lowestAdultAmt = adultAmt;
														lowestSuppGoods = suppGoods;
													}
													if (lowestAdultAmt.intValue() > adultAmt.intValue() && childAmt != null && flightNoVo.getRemain() > audlltNum) {
														lowestAdultAmt = adultAmt;
														lowestSuppGoods = suppGoods;
													}
												} else {
													if (lowestAdultAmt.intValue() == -9999) {
														lowestAdultAmt = adultAmt;
														lowestSuppGoods = suppGoods;
													}

													if (lowestAdultAmt.intValue() > adultAmt.intValue() && flightNoVo.getRemain() > audlltNum) {
														lowestAdultAmt = adultAmt;
														lowestSuppGoods = suppGoods;
													}
												}

											}

										}

										if (lowestSuppGoods != null) {
											// 把最低价的商品放在第一个
											suppGoodsList.remove(lowestSuppGoods);
											suppGoodsList.add(0, lowestSuppGoods);
										}
										// 如果最低成人价小于0，则说明从机票返回的数据中未找到任何满足条件的商品，将当前打包明细的商品删除
										if (lowestAdultAmt != null && lowestAdultAmt < 0) {
											suppGoodsList.removeAll(suppGoodsList);
										}

									}
								}
							}
						}

						if (flightNoVoMap.size() == 0) {
							prodNoPackageDetails.add(prodPackageDetail);
						}
					}
				}

				if (prodNoPackageDetails.size() > 0) {
					prodPackageDetails.removeAll(prodNoPackageDetails);
				}
				// 对这一组按价格对规格进行排序
				if (prodPackageDetails != null && prodPackageDetails.size() > 0) {
					Long lowestAdultAmt = 0L;// 最低价
					SuppGoods lowestSuppGoods = null;// 最低价的商品
					ProdPackageDetail firstProdPackageDetail = null;
					for (ProdPackageDetail prodPackageDetail : prodPackageDetails) {
						if (prodPackageDetail.getProdProductBranch() != null && prodPackageDetail.getProdProductBranch().getSuppGoodsList() != null
								&& prodPackageDetail.getProdProductBranch().getSuppGoodsList().size() > 0) {
							lowestSuppGoods = prodPackageDetail.getProdProductBranch().getSuppGoodsList().get(0);
							if (lowestSuppGoods != null) {
								Long dailyLowestPrice = lowestSuppGoods.getDailyLowestPrice();
								if (lowestAdultAmt.intValue() == 0) {
									lowestAdultAmt = dailyLowestPrice;
								}
								if (dailyLowestPrice != null) {
									if (lowestAdultAmt.intValue() > dailyLowestPrice.intValue()) {
										lowestAdultAmt = dailyLowestPrice;
										firstProdPackageDetail = prodPackageDetail;
									}
								}
							}
						}

					}
					// 把组里价格最低的产品放在第一位置
					if (firstProdPackageDetail != null) {
						prodPackageDetails.remove(firstProdPackageDetail);
						prodPackageDetails.add(0, firstProdPackageDetail);
					}
				}
			}

		}

	}

	/**
	 * 根据出游时间限制，过滤当地游
	 */
	private void filterOptinalRouteLocal(ProdProduct product, List<String> selectDateList, String limitDays, Date visitDate) {
		long categoryId = product.getBizCategoryId();
		long subCategoryId = product.getSubCategoryId() == null ? 0L : product.getSubCategoryId();

		// 跟团游
		boolean isRouteGroup = BIZ_CATEGORY_TYPE.category_route_group.getCategoryId() == categoryId;
		// 定制游
		boolean isCustomized = BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId() == categoryId;
		// 机+酒
		boolean isRouteFlightPlusHotel = BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == categoryId
				&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId() == subCategoryId;
		// 交通+服务
		boolean isRouteTrafficPlusService = BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == categoryId
				&& BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId() == subCategoryId;
		List<String> availables = formatLimtDays(limitDays, visitDate);

		if (log.isInfoEnabled()) {
			log.info("productId:" + product.getProductId() + " isRouteGroup:" + isRouteGroup + " isRouteFlightPlusHotel:" + isRouteGroup + " isRouteTrafficPlusService:"
					+ isRouteGroup);
		}

		if (isRouteGroup || isRouteFlightPlusHotel || isRouteTrafficPlusService || isCustomized) {
			// 判断事由有限制条件
			if (!availables.isEmpty()) {
				for (String date : selectDateList.toArray(new String[0])) {
					if (!availables.contains(date)) {
						selectDateList.remove(date);
					}
				}
			}
		}
	}

	/**
	 * 判断是否为去程
	 * 
	 * @param transport
	 * @return
	 */
	private boolean isToType(ProdPackageGroupTransport transport) {
		if (transport == null) {
			return false;
		}
		if (transport.getfToStartDate() != null || transport.getToStartDate() != null || transport.getToStartDays() != null || transport.getToDestination() != null
				|| transport.getToDestinationDistrict() != null || transport.getToDestinationDistrictMuch() != null || transport.getToStartPoint() != null
				|| transport.getToStartPointDistrict() != null || transport.getToStartPointDistrictList() != null || transport.getToStartPointIds() != null) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为返程
	 * 
	 * @param transport
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private boolean isBackType(ProdPackageGroupTransport transport) {
		if (transport == null) {
			return false;
		}
		if (transport.getfBackStartDate() != null || transport.getBackStartDate() != null || transport.getBackStartDays() != null || transport.getBackDestination() != null
				|| transport.getBackDestinationDistrict() != null || transport.getBackStartPoint() != null || transport.getBackStartPointDistrict() != null) {
			return true;
		}
		return false;
	}

	private List<String> formatLimtDays(String limitDays, Date visitDate) {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isNotBlank(limitDays)) {
			String[] arr = limitDays.trim().split(",");
			for (String str : arr) {
				if (NumberUtils.isDigits(str)) {
					Date optionalDate = DateUtils.addDays(visitDate, Integer.valueOf(str) - 1);
					list.add(DateUtil.formatSimpleDate(optionalDate));
				}
			}
		}
		return list;
	}
}