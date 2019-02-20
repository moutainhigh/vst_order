package com.lvmama.vst.order.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.comm.pet.po.mark.MarkChannel;
import com.lvmama.vst.back.goods.service.ISuppGoodsBranchPropClientService;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.pet.adapter.MarkChannelServiceAdapter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.comm.search.vst.vo.RouteBean;
import com.lvmama.comm.search.vst.vo.VstRouteSearchVO;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsPropApiService;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsPropDescriptionApiService;
import com.lvmama.dest.api.goods.vo.HotelSuppGoodsPropVo;
import com.lvmama.dest.api.goods.vo.SuppGoodsPropDescriptionVo;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.product.interfaces.IHotelProductQueryApiService;
import com.lvmama.dest.api.product.vo.HotelProdLineRouteVo;
import com.lvmama.dest.api.product.vo.HotelProductBaseVo;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizBranchProp;
import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.biz.po.BizFlight;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.biz.service.BranchPropClientService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsGroupStockAdapterClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsSaleReClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceAdapterClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrdFormInfoQueryPO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.client.ord.service.OrdFormInfoClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdAdditionFlagClientService;
import com.lvmama.vst.back.client.prod.service.ProdCalClientService;
import com.lvmama.vst.back.client.prod.service.ProdCalPriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdGroupDateAddtionalClientService;
import com.lvmama.vst.back.client.prod.service.ProdGroupDateClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductAdditionClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchPropClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prod.service.ProdRefundRuleClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.client.prod.service.ProdVisaDocDateClientService;
import com.lvmama.vst.back.client.pub.service.ComPhotoQueryClientService;
import com.lvmama.vst.back.client.visa.service.VsiaService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.goods.po.SuppGoodsGroupStock;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRelation;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.utils.SuppGoodsExpTools;
import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.intentionOrder.po.IntentionOrder;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.ProdGroupDate;
import com.lvmama.vst.back.prod.po.ProdGroupDateAddtional;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroup.GROUPTYPE;
import com.lvmama.vst.back.prod.po.ProdPackageGroupHotel;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTicket;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTransport;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductAddtional;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdProductBranchProp;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.back.prod.po.ProdProductSaleRe;
import com.lvmama.vst.back.prod.po.ProdStartDistrictAdditionalVO;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.prod.po.ProdTrafficBus;
import com.lvmama.vst.back.prod.po.ProdTrafficFlight;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.prod.po.ProdVisaDocDate;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.back.prod.vo.PackageTourProductVo;
import com.lvmama.vst.back.prod.vo.ProdAdditionFlag;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.pub.po.ComPhoto;
import com.lvmama.vst.back.supp.vo.SuppGoodsLineTimePriceVo;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.InternetProtocol;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.WineSplitConstants;
import com.lvmama.vst.comm.utils.front.AutoPackageUtil;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.SUB_PRODUCT_TYPE;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemRelation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.flight.client.branch.vo.BranchVo;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.flight.client.goods.vo.FlightNodeVo;
import com.lvmama.vst.flight.client.product.service.FlightSearchService;
import com.lvmama.vst.flight.client.product.vo.AirLineDayVo;
import com.lvmama.vst.flight.client.product.vo.LvfSignVo;
import com.lvmama.vst.flight.client.product.vo.TrafficGroupVo;
import com.lvmama.vst.flight.client.product.vo.TrafficVo;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.service.impl.OrderTravelContractDataServiceFactory;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.ICouponService;
import com.lvmama.vst.order.service.IOrdFormInfoService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrderIntentionService;
import com.lvmama.vst.order.service.IOrderProductQueryService;
import com.lvmama.vst.order.service.flight.AirlineService;
import com.lvmama.vst.order.service.flight.info.PlaneTypeInfo;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.utils.TestOrderUtil;
import com.lvmama.vst.order.vo.FlightParam;
import com.lvmama.vst.order.vo.InsuranceSuppGoodsVo;
import com.lvmama.vst.order.web.line.LineProdPackageGroupContainer;
import com.lvmama.vst.order.web.line.LineUtils;
import com.lvmama.vst.order.web.line.service.LineProdPackageGroupService;
import com.lvmama.vst.order.web.util.OrderLineProductQueryUtil;
import com.lvmama.vst.order.web.vo.LineBackOneKeyOrderVO;
import com.lvmama.vst.order.web.vo.OrderLineProductVO;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.search.api.common.SearchConstants.TOURE_ROUTE;
import com.lvmama.vst.search.lvmamaback.service.LvmamaBackSearchService;
import com.lvmama.vst.search.util.PageConfig;

/**
 * 后台下单-供驴妈妈客服给客户下单时使用
 *
 * @author spyu
 *
 */

@Controller
public class OrderLineProductQueryAction extends BaseOrderAciton {

	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -4202451464063888166L;

	/**
	 * 日志记录器测试用
	 */
	private static final Logger LOG = LoggerFactory.getLogger(OrderLineProductQueryAction.class);

	/**
	 * 快递品类的BranchId，从配置文件中获取
	 */
//	public static String expressBranchId = Constant.getInstance().getProperty("expressBranchId");

	private static final String[] WEEK_ARRAY = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
	
	private static final String datePatternHHmm = "HH:mm";

	/**
	 * 产品和商品查询业务接口
	 */
	@Autowired
	private IOrderProductQueryService orderProductQueryService;


	 @Autowired
	    private ProdProductClientService prodProductClientRemote;		// 产品数据

	/**
	 * 关联销售
	 */
	@Autowired
	private SuppGoodsSaleReClientService suppGoodsSaleReClientService;

	/**
	 * 常用联系人
	 */
	@Autowired
	private OrderTravelContractDataServiceFactory orderTravelContractDataServiceFactory;

	//意向单查询接口
	@Autowired
	private IOrderIntentionService orderIntentionService;

	@Autowired
	private ICouponService couponService;

	@Autowired
	private ProdProductBranchClientService prodBranchClientService;

	/**
	 * luncece查询商品列表接口
	 */
	@Autowired
	private LvmamaBackSearchService lvmamaBackSearchService;

	private final String ERROR_PAGE = "/order/error";

	/**
	 * 查询商品接口
	 */
	@Autowired
	private ProdProductClientService prodProductClientService;
	/**
	 * 保存订单接口
	 */
	@Autowired
	private OrderService orderService;
	/**
	 * 商品操作接口
	 */
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	/**
	 * 规格操作接口
	 */
	@Autowired
	private BranchClientService branchClientRemote;
	/**
	 * 时间价格表统一操作接口
	 */
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	/**
	 * loading商品接口
	 */
	@Autowired
	private ProdCalClientService prodCalClientRemote;

	/**
	 * 计算打包产品时间价格表接口
	 */
	@Autowired
	private LineProdPackageGroupService lineProdPackageGroupServiceImpl;

	/**
	 * 查询产品规格服务接口
	 */
	@Autowired
	private ProdProductBranchClientService prodProductBranchClientService;

	/**
	 * 根据规格id查询pop属性id服务接口
	 */
	@Autowired
	private BranchPropClientService branchPropClientService;
	/**
	 *查询产品规格属性服务接口
	 */
	@Autowired
	private ProdProductBranchPropClientService prodProductBranchPropClientRemote;

	@Autowired
	private ProdGroupDateClientService prodGroupDateClientService;

	@Autowired
	private VsiaService vsiaService;

	@Autowired
	private DistrictClientService districtClientRemote;

	@Autowired
	private ProdTrafficClientService prodTrafficClientServiceRemote;// 得到交通的详细的数据

    @Autowired
    ProdProductAdditionClientService prodProductAdditionClientRemote;

    @Autowired
    @Qualifier("flightSearchService")
    FlightSearchService flightSearchService;//机票对接接口

    @Autowired
    private AirlineService airlineService;

    @Autowired
    ProdCalPriceClientService prodCalPriceClientService;

	@Autowired
	protected ProdPackageGroupClientService prodPackageGroupClientService;

	@Autowired
	private FavorServiceAdapter favorService;

	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private  ProdProductNoticeClientService prodProductNoticeService1;

	@Autowired
	private IOrdFormInfoService ordFormInfoService;

	@Autowired
	private ProdVisaDocDateClientService prodVisaDocDateClientService;

	@Resource(name="orderLineProductQueryUtil")
	private OrderLineProductQueryUtil orderLineProductQueryUtil;

	@Resource
	private PermUserServiceAdapter permUserServiceAdapter;
	@Resource
	private SuppGoodsClientService suppGoodsClientRemote;
	@Resource
	private BizBuEnumClientService buEnumClientService;

	@Autowired
	private IBookService bookService;

    @Autowired
    private IOrdMulPriceRateService ordMulPriceRateService;

    @Autowired
	private OrdFormInfoClientService ordFormInfoClientService;

    @Autowired
	private CategoryClientService categoryClientRemote;

    @Autowired
    private ProdGroupDateAddtionalClientService prodGroupDateAddtionalClientService;

    @Autowired
    private SuppGoodsTimePriceAdapterClientService suppGoodsTimePriceAdapterClientRemote;

    @Autowired
    private SuppGoodsGroupStockAdapterClientService suppGoodsGroupStockAdapterClientRemote;

    @Autowired
    private ProdRefundRuleClientService prodRefundRuleClientServiceImpl;

    @Autowired
	private IHotelProductQueryApiService hotelProductQueryApiService;

    @Autowired
    private IHotelCombProductService hotelCombProductService;

    @Autowired
    private MarkChannelServiceAdapter markChannelServiceAdapter;

	@Autowired
	private ISuppGoodsBranchPropClientService suppGoodsBranchPropClientServiceRemote;
	
	@Autowired
	private ProdAdditionFlagClientService prodAdditionFlagClientService;
	
	@Autowired
	private IHotelGoodsPropDescriptionApiService hotelGoodsPropDescriptionApiService;

	@Autowired
	private IHotelGoodsPropApiService hotelGoodsPropApiServiceRemote;
	
	@Autowired
	private SuppGoodsTimePriceAdapterClientService suppGoodsTimePriceAdapterClientService;
	
	@Autowired
	private ComPhotoQueryClientService comPhotoQueryClientService;

	/**
	 * 进入产品商品查询页面
	 *
	 * @author wenzhengtao 重构此查询方法
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/showLineOrderProductQueryList.do")
	public String showOrderProductQueryList(Model model,
			OrdOrderProductQueryVO ordOrderProductQueryVO,
			HttpServletRequest request) {
		// 初始化查询表单
		initQueryForm(model, request);
		UserUser user=null;
		try {
			// 从cookie中读取用户信息
			String userId = request.getParameter("userId");
			if(StringUtil.isNotEmptyString(userId)){
				user=userUserProxyAdapter.getUserUserByUserNo(userId);
			}else{
				user = readUserCookie();
			}
		}catch (Exception e) {
			LOG.error("{}", e);
		}

		model.addAttribute("user", user);
		model.addAttribute("isExit", request.getParameter("isExit"));
		try{
			Map<String, Object> params = new HashMap<>();
			params.put("channelCode","O2ONewRetail");
			List<MarkChannel> firstMarkChannels = markChannelServiceAdapter.search(params);
			if(CollectionUtils.isNotEmpty(firstMarkChannels)){

				List<MarkChannel> secondMarkChannels = new ArrayList<>();
				List<MarkChannel> markChannels = null;
				for(MarkChannel markChannel : firstMarkChannels){
					params.clear();
					params.put("fatherId",markChannel.getChannelId());
					params.put("valid","Y");
					params.put("layer",2);
					markChannels = markChannelServiceAdapter.search(params);
					if(CollectionUtils.isNotEmpty(markChannels)){
						secondMarkChannels.addAll(markChannels);
					}
				}
				model.addAttribute("markChannels",secondMarkChannels);
			}
		}catch (Exception e){
			LOG.error("markChannelServiceAdapter.search(),", e);
		}
		// 跳转到后台下单页面
		return "/order/orderProductQuery/showOrderProductQueryList_line";
	}

	/**
	 * 查询产品商品列表
	 *
	 * @author wenzhengtao 重构此查询方法
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/findLineOrderProductList.do")
	public String findOrderGoodsList(Model model, Integer page,
			OrdOrderProductQueryVO ordOrderProductQueryVO,
			HttpServletRequest request) throws BusinessException {
		int pageSize=10;
		VstRouteSearchVO searchVo = new VstRouteSearchVO();

		// 开始时间戳
		long start = System.currentTimeMillis();
		// 初始化查询表单
		initQueryForm(model, request);

		// 构造查询条件
		HashMap<String, Object> paramOrderProductQuery = new HashMap<String, Object>();

		//级别
		if(ordOrderProductQueryVO.getRecommendLevel()!=null&&ordOrderProductQueryVO.getRecommendLevel()>0){
//			searchVo.setRecommendLevel(ordOrderProductQueryVO.getRecommendLevel()+"");
		}
		// 游玩天数
		if (ordOrderProductQueryVO.getDays() != null) {
			paramOrderProductQuery
					.put("days", ordOrderProductQueryVO.getDays());
			searchVo.setRouteNum(ordOrderProductQueryVO.getDays() + "");
		}
		// 出发地城市ID
		if (ordOrderProductQueryVO.getDistrictId() != null) {
			paramOrderProductQuery.put("districtId",
					ordOrderProductQueryVO.getDistrictId());
			searchVo.setFromDestId(ordOrderProductQueryVO.getDistrictId() + "");
		}
		// 产品ID/名称/目的地
		if (StringUtils.isNotEmpty(ordOrderProductQueryVO.getKeyWords())) {
			paramOrderProductQuery.put("keyWords",
					ordOrderProductQueryVO.getKeyWords());
			searchVo.setKeyword(ordOrderProductQueryVO.getKeyWords());
		}
		// 设置品类id
		TOURE_ROUTE enumValue = TOURE_ROUTE.ROUTE;
		if (ordOrderProductQueryVO.getCategoryIds() != null
				&& ordOrderProductQueryVO.getCategoryIds().length > 0) {
			if (ordOrderProductQueryVO.getCategoryIds()[0]
					.equals(TOURE_ROUTE.ROUTE.name())) {
				enumValue = TOURE_ROUTE.ROUTE;
			} else if (ordOrderProductQueryVO.getCategoryIds()[0]
					.equals(TOURE_ROUTE.GROUP.name())) {
				enumValue = TOURE_ROUTE.GROUP;
			} else if (ordOrderProductQueryVO.getCategoryIds()[0]
					.equals(TOURE_ROUTE.FREETOUR.name())) {
				enumValue = TOURE_ROUTE.FREESCENICTOUR;
				//183
				if (WineSplitConstants.TRAFFIC_SERVICE_CATEGORY_ID.toString().equals(ordOrderProductQueryVO.getSubCategoryId())) {
					enumValue = TOURE_ROUTE.PLAY;
				} else if (WineSplitConstants.WINE_SPLIT_CATEGORY_ID.toString().equals(ordOrderProductQueryVO.getSubCategoryId())) {
					enumValue = TOURE_ROUTE.SCENICTOUR;
				} else if (WineSplitConstants.PLANE_SPLIT_CATEGORY_ID.toString().equals(ordOrderProductQueryVO.getSubCategoryId())) {
					enumValue = TOURE_ROUTE.FREETOUR;
				}
			} else if (ordOrderProductQueryVO.getCategoryIds()[0]
					.equals(TOURE_ROUTE.AROUND.name())) {
				enumValue = TOURE_ROUTE.AROUND;
			} else if (ordOrderProductQueryVO.getCategoryIds()[0]
					.equals(TOURE_ROUTE.LOCAL.name())) {
				enumValue = TOURE_ROUTE.LOCAL;
			}
		}
		// 价格区间
		if (StringUtils.isNotEmpty(ordOrderProductQueryVO.getPriceRange())) {
			paramOrderProductQuery.put("priceRange",
					ordOrderProductQueryVO.getPriceRange());
			if (ordOrderProductQueryVO.getPriceRange().endsWith("1")) {
				searchVo.setPrice("0,100");
			} else if (ordOrderProductQueryVO.getPriceRange().endsWith("2")) {
				searchVo.setPrice("100,200");
			} else if (ordOrderProductQueryVO.getPriceRange().endsWith("3")) {
				searchVo.setPrice("200," + Integer.MAX_VALUE);
			} else if (ordOrderProductQueryVO.getPriceRange().endsWith("7")) {
				// 自定义价格区间
				if (ordOrderProductQueryVO.getPriceBegin() != null
						&& ordOrderProductQueryVO.getPriceEnd() != null) {// 开始
					paramOrderProductQuery.put("priceBegin",
							ordOrderProductQueryVO.getPriceBegin());
					searchVo.setPrice(ordOrderProductQueryVO.getPriceBegin()
							+ "," + ordOrderProductQueryVO.getPriceEnd());
				}
			}
		}

		//二次过滤使用的设置交通
		String traffic=request.getParameter("traffic");
		if(StringUtils.isNotEmpty(traffic)){
			searchVo.setTraffic(traffic);
		}
		//二次过滤使用的打包内容包含
		String packagesTypes=request.getParameter("packagesTypes");
		if(StringUtils.isNotEmpty(packagesTypes)){
			searchVo.setPackagesTypes(packagesTypes);
		}

		int pagenum = page == null ? 1 : page;
		searchVo.setPage(pagenum);
		searchVo.setPageSize(pageSize);
		searchVo.setDistributors(String.valueOf(Constant.DIST_BACK_END));
		// searchVo.setFromDestId(9 + "");
		log.info("------searchVo:"+JSONObject.fromObject(searchVo));
		PageConfig<RouteBean> pageConfigResult = null;
		try {
			pageConfigResult = lvmamaBackSearchService.getRouteIndexBeans(
					enumValue.name(), searchVo);
		} catch (Exception e) {
			LOG.error("{}", e);
			pageConfigResult = new PageConfig<RouteBean>(0);
		}

		List<RouteBean> resultList = null;
		if (pageConfigResult != null) {
			resultList = pageConfigResult.getItems();
		}

		//把搜索结果转化为vo
		List<OrderLineProductVO> lineProductVOList = orderLineProductQueryUtil.toOrderLineProductVOList(resultList);

		Page<OrderLineProductVO> pageParam = Page.page(
				pageConfigResult == null ? 0 : pageConfigResult.getTotalResultSize(), pageSize, pagenum);
		pageParam.buildJSONUrl(request);
		pageParam.setItems(lineProductVOList);




		Map<String,ProdGroupDate> prodGroupDateMap = new HashMap<String, ProdGroupDate>();
		Map<String,List<ProdLineRouteVO>> productPropMap = new HashMap<String, List<ProdLineRouteVO>>();
		Map<String,ProdProduct> productMap = new HashMap<String, ProdProduct>();//存放产品信息
		Map<String,Long> startDistrictIdMap = new HashMap<String, Long>();//存放多出发ID
		//所属bu的列表，用来页面上转换所属BU的code到中文名称
		List<BizBuEnum> buEnumList = null;
		ResultHandleT<List<BizBuEnum>> resultHandleT = buEnumClientService.getAllBizBuEnumList();
		if(resultHandleT!=null && resultHandleT.isSuccess()){
			buEnumList = resultHandleT.getReturnContent();
		}

		if(CollectionUtils.isNotEmpty(lineProductVOList)){
			ProdProductParam param = new ProdProductParam();
			param.setLineRoute(true);
			param.setAddtion(true);
			param.setHotelCombFlag(true);
			for(OrderLineProductVO lineProductVO:lineProductVOList){
				// 产品Id
				Long productId = NumberUtils.toLong(lineProductVO.getProductId());
				// 品类Id
				Long categoryId = NumberUtils.toLong(lineProductVO.getCategoryId());
				// 酒套餐
				if(Long.valueOf(32).equals(categoryId)){
					com.lvmama.dest.api.common.RequestBody<Long> requestBody = new com.lvmama.dest.api.common.RequestBody<Long>();
					requestBody.setT(productId);
					requestBody.setToken(NewOrderConstant.VST_ORDER_TOKEN);
					// 调用酒店子系统api查询酒套餐产品信息
					com.lvmama.dest.api.common.ResponseBody<HotelProductBaseVo> responseBody = hotelProductQueryApiService
							.findProdProductSimpleWithLineRouteById(requestBody);
					HotelProductBaseVo hotelProductBaseVo = responseBody.getT();
					if(hotelProductBaseVo != null){
						// 转换成vst系统product
						ProdProduct prodProduct = new ProdProduct();
						prodProduct.setProductId(hotelProductBaseVo.getProductId());// 产品Id
						prodProduct.setProductName(hotelProductBaseVo.getProductName());// 产品名称
						prodProduct.setBu(hotelProductBaseVo.getBu());// bu
						prodProduct.setBizCategoryId(hotelProductBaseVo.getBizCategoryId());// 品类
						prodProduct.setManagerId(hotelProductBaseVo.getManagerId());// 产品经理Id
						prodProduct.setRecommendLevel(hotelProductBaseVo.getRecommendLevel());// 推荐级别
						prodProduct.setPackageType(hotelProductBaseVo.getPackageType());// 打包类型
						List<HotelProdLineRouteVo> hotelProdLineRouteVoList = hotelProductBaseVo.getProdLineRouteList();// 行程
						// 封装行程信息
						if(hotelProdLineRouteVoList != null && hotelProdLineRouteVoList.size() > 0){
							List<ProdLineRouteVO> prodLineRouteList = new ArrayList<ProdLineRouteVO>();
							for(HotelProdLineRouteVo hotelProdLineRouteVo : hotelProdLineRouteVoList){
								ProdLineRouteVO prodLineRouteVO = new ProdLineRouteVO();
								BeanUtils.copyProperties(hotelProdLineRouteVo, prodLineRouteVO);
								prodLineRouteList.add(prodLineRouteVO);
							}
							prodProduct.setProdLineRouteList(prodLineRouteList);
						}

						// 产品经理Id
						lineProductVO.setManagerId(prodProduct.getManagerId() + "");

						//取产品经理名字，并设定到resultList
						PermUser permUser = permUserServiceAdapter.getPermUserByUserId(prodProduct.getManagerId());
						LOG.info("product [" + productId + "]'s product manager id is [" + prodProduct.getManagerId() + "], manager's name is " + permUser.getRealName());
						lineProductVO.setManagerName(permUser.getRealName());

						// 查询酒套餐产品团期信息
						com.lvmama.dest.api.hotelcomb.vo.GroupDateRequest groupDateRequest = new com.lvmama.dest.api.hotelcomb.vo.GroupDateRequest();
						groupDateRequest.setProductId(productId);// 产品Id
						groupDateRequest.setStartSpecDate(new Date());// 从当前日期开始查询
						com.lvmama.dest.api.common.RequestBody<com.lvmama.dest.api.hotelcomb.vo.GroupDateRequest> groupDateRequestBody
							= new com.lvmama.dest.api.common.RequestBody<com.lvmama.dest.api.hotelcomb.vo.GroupDateRequest>();
						groupDateRequestBody.setT(groupDateRequest);
						groupDateRequestBody.setToken(NewOrderConstant.VST_ORDER_TOKEN);
						com.lvmama.dest.api.common.ResponseBody<List<com.lvmama.dest.api.hotelcomb.vo.ProdGroupDateResponse>> groupDateResponse
							= hotelCombProductService.loadHotelCombGroupDate(groupDateRequestBody);
						List<com.lvmama.dest.api.hotelcomb.vo.ProdGroupDateResponse> prodGroupDateResponseList =  groupDateResponse.getT();
						com.lvmama.dest.api.hotelcomb.vo.ProdGroupDateResponse defaultGroupDate = null;// 默认的团期
						Collections.reverse(prodGroupDateResponseList);// 反转集合（查出来的团期是按照日期倒叙排列的，需要反转取当前日期最近的那个团期）
						for(com.lvmama.dest.api.hotelcomb.vo.ProdGroupDateResponse groupDate : prodGroupDateResponseList){
							if(groupDate.getStock() != null && groupDate.getStock().intValue() != -2
									&& groupDate.getLowestSaledPrice() != null && groupDate.getLowestSaledPrice() > 0){
								defaultGroupDate = groupDate;
								break;
							}
						}
						if(defaultGroupDate != null){
							ProdGroupDate prodGroupDate = new ProdGroupDate();
							prodGroupDate.setProductId(Long.valueOf(defaultGroupDate.getProductId()));// 产品Id
							prodGroupDate.setLineRouteId(Long.valueOf(defaultGroupDate.getLineRouteId()));// 线路Id
							prodGroupDate.setLowestSaledPrice(Long.valueOf(defaultGroupDate.getLowestSaledPrice()));// 起价
							prodGroupDate.setSpecDate(defaultGroupDate.getSpecDate());// 日期
							prodGroupDate.setStock(Long.valueOf(defaultGroupDate.getStock()));// 库存
							prodGroupDateMap.put(lineProductVO.getProductId(), prodGroupDate);
						}

						// 行程
						productPropMap.put(lineProductVO.getProductId(), prodProduct.getProdLineRouteList());

						ProdProductAddtional addtional = new ProdProductAddtional();
						Long lowestSaledPrice = 0l;
						if(defaultGroupDate != null && defaultGroupDate.getLowestSaledPrice() != null)
							lowestSaledPrice = Long.valueOf(defaultGroupDate.getLowestSaledPrice());
						addtional.setLowestSaledPrice(lowestSaledPrice);// 最低起价
						prodProduct.setProductAddtional(addtional);
						productMap.put(lineProductVO.getProductId(), prodProduct);
					}else{
						LOG.info("New hotelcomb product is null!");
					}
				}else{// 线路其它品类
					ResultHandleT<ProdGroupDate> prodGroupDate;
					ProdProductParam pparam = new ProdProductParam();
					pparam.setHotelCombFlag(false);
					ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(productId, pparam);
					//设定产品经理和所属BU到resultList中
					LOG.info("No begin to fill product manager and BU information for product " + productId);
					if(resultHandle!=null && resultHandle.getReturnContent()!=null){
						//取产品经理id，并设定到resultList
						ProdProduct prodProduct = resultHandle.getReturnContent();
						lineProductVO.setManagerId(prodProduct.getManagerId() + "");
						//取产品经理名字，并设定到resultList
						PermUser permUser = permUserServiceAdapter.getPermUserByUserId(prodProduct.getManagerId());
						LOG.info("product [" + productId + "]'s product manager id is [" + prodProduct.getManagerId() + "], manager's name is " + permUser.getRealName());
						lineProductVO.setManagerName(permUser.getRealName());

					}

					if(resultHandle.getReturnContent()!=null&&"Y".equalsIgnoreCase(resultHandle.getReturnContent().getMuiltDpartureFlag()) ){
						if (ordOrderProductQueryVO.getDistrictId() == null) {
							//设置默认出发地
							ProdStartDistrictAdditionalVO	startDistrictVo = adapterStartDistrict(resultHandle.getReturnContent(), null);
							ordOrderProductQueryVO.setDistrictId(startDistrictVo.getStartDistrictId());
						}
						startDistrictIdMap.put(lineProductVO.getProductId(), ordOrderProductQueryVO.getDistrictId());
						prodGroupDate = prodGroupDateClientService.findSimpleProdGroupDate(productId,ordOrderProductQueryVO.getDistrictId());
					} else{
						prodGroupDate = prodGroupDateClientService.findSimpleProdGroupDate(productId);
					}



					if(prodGroupDate!=null&&!prodGroupDate.hasNull()){
						prodGroupDateMap.put(lineProductVO.getProductId(), prodGroupDate.getReturnContent());
					}

					ResultHandleT<ProdProduct> product = prodProductClientRemote.findLineProductByProductId(productId, param);
					if(product!=null && product.getReturnContent()!=null && CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList())){
						productPropMap.put(lineProductVO.getProductId(), product.getReturnContent().getProdLineRouteList());
						ProdProductAddtional Addtional =new ProdProductAddtional();
						try {
							Addtional =prodProductAdditionClientRemote.selectByPrimaryKey(product.getReturnContent().getProductId());
						} catch (Exception e) {
							log.error(e.getMessage());
						}
						ProdProduct prodProduct = product.getReturnContent();
						prodProduct.setProductAddtional(Addtional);
						productMap.put(lineProductVO.getProductId(), prodProduct);
					}
				}

			}
		}

		Map<String, Map<String, Map<String, Object>>> seacherMap=pageConfigResult.getSelectMap();
		Map<String, Map<String, Object>> map=null;

		Map<String, Map<String, Map<String, Object>>> filterMap=new TreeMap<String, Map<String,Map<String,Object>>>();
		//打包类型
		if(MapUtils.isNotEmpty(seacherMap)){
			map=seacherMap.get("packagesTypes");
			if(MapUtils.isNotEmpty(map)){
				filterMap.put("包含项目", map);
			}
		}
		//交通
		if(MapUtils.isNotEmpty(seacherMap)){
			map=seacherMap.get("traffic");
			if(MapUtils.isNotEmpty(map)){
				filterMap.put("往返交通", map);
			}
		}
//		//目的地
//		if(MapUtils.isNotEmpty(seacherMap)){
//			map=seacherMap.get("districtId");
//			if(MapUtils.isNotEmpty(map)){
//				filterMap.put("目的地", map);
//			}
//		}
//		//品类
//		if(MapUtils.isNotEmpty(seacherMap)){
//			map=seacherMap.get("categoryId");
//			if(MapUtils.isNotEmpty(map)){
//				filterMap.put("品类", map);
//			}
//		}
//		System.out.println(JSONObject.fromObject(filterMap).toString());
		model.addAttribute("filter",JSONObject.fromObject(filterMap).toString());

		model.addAttribute("pageParam", pageParam);
		model.addAttribute("prodList", lineProductVOList);
		model.addAttribute("prodGroupDateMap",prodGroupDateMap);
		model.addAttribute("productPropMap",productPropMap);
		model.addAttribute("productMap",productMap);
		model.addAttribute("startDistrictIdMap",startDistrictIdMap);
		model.addAttribute("buList", buEnumList);
		pageParam.buildJSONUrl(request);

		// 回填查询条件
		model.addAttribute("ordOrderProductQueryVO", ordOrderProductQueryVO);
		// 打印耗时
		if (LOG.isDebugEnabled()) {
			LOG.debug("后台下单产品商品查询完毕-耗时" + (System.currentTimeMillis() - start)
					+ "毫秒!");
		}
		return "/order/orderProductQuery/findLineProductList";
	}

	/**
	 * 查询产品商品列表
	 *
	 * @author wenzhengtao 重构此查询方法
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ord/productQuery/findLineOrderProductList1.do")
	public String findOrderGoodsList1(Model model, Integer page,
			OrdOrderProductQueryVO ordOrderProductQueryVO,
			HttpServletRequest request) throws BusinessException {
		// 开始时间戳
		long start = System.currentTimeMillis();
		// 初始化查询表单
		initQueryForm(model, request);

		// 构造查询条件
		HashMap<String, Object> paramOrderProductQuery = new HashMap<String, Object>();

		// 游玩天数
		if (ordOrderProductQueryVO.getDays() != null) {
			paramOrderProductQuery
					.put("days", ordOrderProductQueryVO.getDays());
		}
		// 出发地城市ID
		if (ordOrderProductQueryVO.getDistrictId() != null) {
			paramOrderProductQuery.put("districtId",
					ordOrderProductQueryVO.getDistrictId());
		}
		// 目的地城市ID
		if (ordOrderProductQueryVO.getDistrictId() != null) {
			paramOrderProductQuery.put("destDistrictId",
					ordOrderProductQueryVO.getDistrictId());
		}
		// 价格区间
		if (UtilityTool.isValid(ordOrderProductQueryVO.getPriceRange())) {
			paramOrderProductQuery.put("priceRange",
					ordOrderProductQueryVO.getPriceRange());
		}
		// 自定义价格区间
		if (ordOrderProductQueryVO.getPriceBegin() != null) {// 开始
			paramOrderProductQuery.put("priceBegin",
					ordOrderProductQueryVO.getPriceBegin());
		}
		if (ordOrderProductQueryVO.getPriceEnd() != null) {// 结束
			paramOrderProductQuery.put("priceEnd",
					ordOrderProductQueryVO.getPriceEnd());
		}
		Date beginDateReal = new Date();
		Date endDateReal = DateUtil.toDate("2014-12-30", "yyyy-MM-dd");

		// 判断日期是否合法,如果不合法，则自动增加5天
		if (DateUtil.inAdvance(beginDateReal, new Date())) {
			beginDateReal = DateUtil.dsDay_Date(new Date(), 5);
			beginDateReal = CalendarUtils.getDateFormatTime(beginDateReal, 0,
					0, 0);
		}
		// 组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("beginDate", beginDateReal);
		params.put("endDate", endDateReal);
		params.put("productId", ordOrderProductQueryVO.getProductId());
		params.put("distributionId", Constant.DIST_BACK_END);
		// 查询总行数
		ResultHandleT<Integer> countResultHandle = prodProductClientService
				.findProductByGroupDateCounts(params);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(countResultHandle.getReturnContent(), 4,
				pagenum);
		pageParam.buildJSONUrl(request);
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		ResultHandleT<List<ProdProduct>> resultHandle = prodProductClientService
				.findProductByGroupDate(params);
		pageParam.setItems(resultHandle.getReturnContent());
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("prodList", resultHandle.getReturnContent());

		// 回填查询条件
		model.addAttribute("ordOrderProductQueryVO", ordOrderProductQueryVO);
		// 打印耗时
		if (LOG.isDebugEnabled()) {
			LOG.debug("后台下单产品商品查询完毕-耗时" + (System.currentTimeMillis() - start)
					+ "毫秒!");
		}
		return "/order/orderProductQuery/findLineProductList";
	}

	/**
	 * 自主打包商品查询更多规格
	 *
	 * @return
	 */
	@RequestMapping(value = "/ord/order/queryPackageMoreProduct.do")
	public String queryPackageMoreProductInfo(Long productId, Long packageProductId, Long groupId,
			Long currentProductBranchId, Date specDate, Long adultQuantity,
			Long childQuantity, String pageFlag,Long productItemIdIndex,Long totalAmount,Long currentSuppgoodsId, Date selectedDate,HttpServletRequest request) {
		try {

			ProdProductParam param = new ProdProductParam();
			param.setHotelCombFlag(true);
			ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(productId, param);
			ProdProduct prodProduct = null;
			if (resultHandle != null && resultHandle.isSuccess() && !resultHandle.hasNull()) {
				prodProduct = resultHandle.getReturnContent();
			} else {
				HttpServletLocalThread.getModel().addAttribute("ERROR", "商品Loading接口拨错，商品不可售");
				return "order/error";
			}
			Long startDistrictId = null;
			try {
				startDistrictId = request.getParameter("startDistrictId") == null ? null : Long.valueOf(request.getParameter("startDistrictId").trim());
			}catch(Exception ex){LOG.error(ExceptionFormatUtil.getTrace(ex));}
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("productId", productId);
			parameterMap.put("supplierProductId", packageProductId);
			parameterMap.put("groupId", groupId);
			parameterMap.put("currentProductBranchId", currentProductBranchId);
			parameterMap.put("specDate", specDate);
			parameterMap.put("adultQuantity", adultQuantity);
			parameterMap.put("childQuantity", childQuantity);
			parameterMap.put("distributorId", Constant.DIST_BACK_END);
			parameterMap.put("startDistrictId", startDistrictId);

			PackageTourProductVo lineProductVo=null;
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo = prodCalClientRemote.getChangePackProduct(parameterMap);
			Map<String, List<ProdPackageGroup>> packageMap = null;
			List<ProdPackageGroup> tempList = null;

			if (ReturnlineProductVo.getReturnContent()!= null) {
				// 初始化打包组
				lineProductVo=ReturnlineProductVo.getReturnContent();
				packageMap = lineProductVo.getProdPackageGroupMap();
			}
			
			
			/**********************自由行 机+酒 酒店+国内 展示信息 Start**********************/
			if(null != prodProduct.getSubCategoryId() &&
				null != prodProduct.getProductType() &&
				prodProduct.getSubCategoryId() == 182L && prodProduct.getProductType().equals("INNERLINE")) {
			    List<Map<String,Object>>hotelLists = new ArrayList<Map<String,Object>>();
				Map<String,List<ProdPackageGroup>> prodPackageGroupMap = null;
			        if(lineProductVo != null){
			            prodPackageGroupMap = lineProductVo.getProdPackageGroupMap();
			        }
			      //如果有组，组合组的信息
			        if(prodPackageGroupMap != null && prodPackageGroupMap.size() > 0){
			            Set<String> keySet = prodPackageGroupMap.keySet();
			            for(String key : keySet){
			        	//list的获得必须放在上面对接的处理之后,因为prodPackageGroupMap有可能改变
			                List<ProdPackageGroup> prodPackageGroupList = prodPackageGroupMap.get(key);
			                if(prodPackageGroupList == null || prodPackageGroupList.size() == 0){
			                    continue;
			                }
			                Map<String, Object>assembleMap = new HashMap<String, Object>();
			              //下面的都是酒店组
			                //HOTEL属于打包的产品就是酒店
			                //_group和freedom自主打包打包的供应商打包的跟团游---自主打包打包的供应商打包的自由行--现在都只能是可换酒店
			                if(ProdPackageGroup.GROUPTYPE.HOTEL.getCode().equalsIgnoreCase(key)){
			                	assembleMap.clear();
			                	assembleMap.put("prodPackageGroupList", prodPackageGroupList);
			                	assembleMap.put("selectDate", specDate);
			                	assembleMap.put("hotelList", hotelLists);
			                	this.moreAssembleHotelGroup(assembleMap);
			                }
			            }
			        }
			        HttpServletLocalThread.getModel().addAttribute("hotelInfoList",hotelLists);
			}
			
			/**********************机+酒 酒店展示信息 End**********************/
			
			if (packageMap != null) {
				boolean isSupplier=true;
				if(prodProduct.getPackageType().equalsIgnoreCase("LVMAMA")){
					isSupplier=false;
				}
				LineProdPackageGroupContainer container = lineProdPackageGroupServiceImpl
						.initPackageProductMap(specDate, packageMap,isSupplier);
				if (container.isHasPackage()) {
					tempList = container.getAllPackageList();
				}
                //检查期票有效期及时间
                Map<String, String> msg = getAperLineTicketValidMsg(specDate, container.getTicketProdPackageList());
                HttpServletLocalThread.getModel().addAttribute("lineTicketValidMsgMap", msg);
            }
			if (tempList == null) {
				HttpServletLocalThread.getModel().addAttribute("ERROR", "没有更多的商品");
				return "order/error";
			}

			/**
			 * 国内自主打包 自由行酒景 后台下单，酒店信息与前台商品页展示效果保持一致
			 */
			if ("LVMAMA".equalsIgnoreCase(prodProduct.getPackageType())
					&&(ProdProduct.PRODUCTTYPE.INNERLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNER_BORDER_LINE.name().equalsIgnoreCase(prodProduct.getProductType()))
					&&(prodProduct.getBizCategoryId() == 18L && prodProduct.getSubCategoryId() == 181L)
					&&"DESTINATION_BU".equalsIgnoreCase(prodProduct.getBu())) {
				if("changeHotelJingjiu".equals(pageFlag)){
					List<ProdPackageGroup> prodPackageGroupList = packageMap.get(ProdPackageGroup.GROUPTYPE.HOTEL.name());
					if(prodPackageGroupList != null && prodPackageGroupList.size() > 0){
						List<Map<String,Object>> hotelList = new ArrayList<Map<String,Object>>();
						ProdPackageGroup prodPackageGroup = prodPackageGroupList.get(0);//只会有一个组
						setChangeHotelList(hotelList,prodPackageGroup,currentProductBranchId,specDate,currentSuppgoodsId);
						HttpServletLocalThread.getModel().addAttribute("hotelList", hotelList);
					}
	             }
			}

			Long categoryFlight = 21L;
			for (ProdPackageGroup prodPackageGroup : tempList) {
				if(!CollectionUtils.isEmpty(prodPackageGroup.getProdPackageDetails()) &&
						prodPackageGroup.getProdPackageDetails().get(0)!=null &&
						prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch()!=null &&
						prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getBizBranch()!=null &&
						categoryFlight.equals(prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())){
					HttpServletLocalThread.getModel().addAttribute("isFlight","true");
				}
				if(prodPackageGroup.getProdPackageGroupTransport()!=null &&
						"TOBACK".equals(prodPackageGroup.getProdPackageGroupTransport().getTransportType())){
					HttpServletLocalThread.getModel().addAttribute("transportType", prodPackageGroup.getProdPackageGroupTransport().getTransportType());
					break;
				}
			}
			boolean isHotel = false;
			Long baseChildNum = 0L;
			Long baseAdultNum = 0L;
			if(prodProduct.getHotelCombFlag().equals("N") ){
				isHotel = true;
				baseChildNum = prodProduct.getBaseChildQuantity();
				baseAdultNum = prodProduct.getBaseAdultQuantity();
				HttpServletLocalThread.getModel().addAttribute("baseChildNum", baseChildNum);
				HttpServletLocalThread.getModel().addAttribute("baseAdultNum", baseAdultNum);
			}
			ProdProductAddtional productAddtional = prodProductAdditionClientRemote.selectByPrimaryKey(productId);
			HttpServletLocalThread.getModel().addAttribute("isHotel", isHotel);
			HttpServletLocalThread.getModel().addAttribute("productAddtional", productAddtional);
			HttpServletLocalThread.getModel().addAttribute("productId", productId);
			HttpServletLocalThread.getModel().addAttribute("prodProduct", prodProduct);
			HttpServletLocalThread.getModel().addAttribute("groupId", groupId);
			HttpServletLocalThread.getModel().addAttribute("currentProductBranchId", currentProductBranchId);
			HttpServletLocalThread.getModel().addAttribute("specDate", specDate);
			HttpServletLocalThread.getModel().addAttribute("adultQuantity", adultQuantity);
			HttpServletLocalThread.getModel().addAttribute("childQuantity", childQuantity);
			HttpServletLocalThread.getModel().addAttribute("adultNum", adultQuantity);
			HttpServletLocalThread.getModel().addAttribute("childNum", childQuantity);
			HttpServletLocalThread.getModel().addAttribute("moreProductList", tempList);
			HttpServletLocalThread.getModel().addAttribute("productItemIdIndex", productItemIdIndex);
			HttpServletLocalThread.getModel().addAttribute("sourceTotalAmount",totalAmount);
			HttpServletLocalThread.getModel().addAttribute("packageProductId",packageProductId);
			if(selectedDate != null) {
				HttpServletLocalThread.getModel().addAttribute("selectedDate", DateUtil.formatSimpleDate(selectedDate));
			}

			// 这里需要考虑刷新可选服务列表

		} catch (Exception e1) {
			LOG.error("{}", e1);
			HttpServletLocalThread.getModel().addAttribute("ERROR", "没有更多的商品");
			return "order/error";
		}

		return "/order/orderProductQuery/line/" + pageFlag + "_moreProduct";
	}

	/**
     * 填充更换酒店列表
     * @param hotelList
     * @param prodPackageGroup 酒店打包组
     * @param currentProductBranchId
     * @param selectDate1 出行日期
     */
    private void setChangeHotelList(List<Map<String,Object>> hotelList, ProdPackageGroup prodPackageGroup, Long currentProductBranchId, Date selectDate1,Long currentSuppgoodsId) {
    	List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
        if(prodPackageDetails != null && prodPackageDetails.size() > 0){
	       	 //各房型商品按产品不同划分
	       	 Map<String,Map<String,Object>> productMap = new HashMap<String, Map<String,Object>>();
	       	 Map<String,Object> firstMap = null;
	       	 List<Map<String,Object>> mapListB = null;
	       	 int count = 0;//同一产品商品计数
	       	 for(ProdPackageDetail prodPackageDetail : prodPackageDetails){
	       		Map<String,Object> map = null;
	       		Long currentProductId = prodPackageDetail.getProdProduct().getProductId();
	       		if(productMap.get("product_"+currentProductId) != null){
	       			map = productMap.get("product_"+currentProductId);
	       			firstMap = (Map<String, Object>) map.get("first_goods");
	       			mapListB = (List<Map<String, Object>>) map.get("others");
	       			count = (Integer) map.get("count");
	       		}else{
	       			map = new HashMap<String,Object>();
	       			productMap.put("product_"+currentProductId, map);
	       			hotelList.add(map);
	       			firstMap = new HashMap<String,Object>();
	                mapListB = new ArrayList<Map<String,Object>>();
	                map.put("first_goods",firstMap);//该产品第一个商品
	                map.put("others",mapListB);//其他未选中的商品列表
	                count = 0;
	                map.put("count", count);
	       		}

		        ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
		        if(prodProductBranch != null){
		             List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
		             if(suppGoodsList != null && suppGoodsList.size() > 0){
                         for (SuppGoods suppGoods : suppGoodsList) {
//                             if(count == 0){
//                                 if((currentSuppgoodsId==null && currentProductBranchId.longValue() == prodProductBranch.getProductBranchId().longValue())
//                                         || (currentSuppgoodsId!=null && suppGoods.getSuppGoodsId().longValue()==currentSuppgoodsId.longValue())
//                                         ){//选中的商品
//                                     map.put("has_current_selected", "Y");
//                                     firstMap.put("is_current_selected", "Y");
//                                 }
//
//                                 firstMap.put("selectedCurrentProductBranchId",currentProductBranchId);//被选中的产品规格
//                                 Long stock = fillHotelMap(firstMap, prodPackageGroup, selectDate1, prodPackageDetail, prodProductBranch, suppGoods);
//                                 if(stock <= 0L){
//                                     firstMap.clear();
//                                 }else{
//                                     count++;
//                                 }
//                             }else{
                                 Map<String,Object> otherMap = new HashMap<String,Object>();
                                 if((currentSuppgoodsId==null && currentProductBranchId.longValue() == prodProductBranch.getProductBranchId().longValue())
                                         || (currentSuppgoodsId!=null && suppGoods.getSuppGoodsId().longValue()==currentSuppgoodsId.longValue())
                                         ){//选中的商品
                                     map.put("has_current_selected", "Y");
                                     otherMap.put("is_current_selected", "Y");
                                 }
                                 otherMap.put("selectedCurrentProductBranchId",currentProductBranchId);//被选中的产品规格
                                 Long stock = fillHotelMap(otherMap, prodPackageGroup, selectDate1, prodPackageDetail, prodProductBranch, suppGoods);
                                 if(stock > 0L){
                                     mapListB.add(otherMap);
                                     count++;
                                 }
                             }
//                         }
		                 map.put("count", count);
		           }
		      }
       	  }

       	  //价格排序
            Iterator<Map.Entry<String,Map<String,Object>>> iter = productMap.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,Map<String,Object>> entry = iter.next();
                Map<String,Object> map = entry.getValue();
                firstMap = (Map<String, Object>) map.get("first_goods");
                mapListB = (List<Map<String, Object>>) map.get("others");
                Collections.sort(mapListB, new Comparator<Map<String,Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Long price1 =Long.MAX_VALUE;
                        Long price2 =Long.MAX_VALUE;
                        if(o1!=null && o1.get("price")!=null){
                            price1= (Long) o1.get("price");
                        }
                        if(o2!=null && o2.get("price")!=null){
                            price2= (Long) o2.get("price");
                        }
                        if(price2>price1){
                            return -1;
                        }else if(price2<price1){
                            return 1;
                        }else{
                            return 0;
                        }
                    }
                });
                //取第一个为选中项
                if(mapListB.size()>0){
                    map.put("first_goods",mapListB.get(0));
                    mapListB.remove(0);
                }
            }

       }
	}

	/**
	 * 新获取更多交通
	 * @param model
	 * @param backline
	 * @param req
	 * @param Resp
	 * @param adultQuantity	成人数
	 * @param childQuantity	儿童数
	 * @param selectDate	选择日期
	 * @param selectedFlag	选择商品标识  = 出发时间+航班号 + 商品ID
	 * @param quantity	份数
	 * @param changeType	更换类型
	 * @param haveChangeButton
	 * @param lvmamaProductId	父产品ID
	 * @param selectedSuppGoodsId	当前商品ID
	 * @param oldPrice	变换之前价格
	 * @return
	 */
	@RequestMapping(value = "/ord/order/getBasicJpData1.do")
	public String getBasicJpData1(Model model,String backline,HttpServletRequest req,HttpServletResponse Resp,
             Long adultQuantity,Long childQuantity,String selectDate,String selectedFlag,
             Long packageProductId,Long toPrice,Long packageProductBranchId,Long selectedSuppGoodsId,Long packageGroupId,
             Long quantity,String changeType,String haveChangeButton,@RequestParam Long lvmamaProductId,Long oldPrice,String transportType) {
		String methodName = "OrderLineProductQueryAction#getBasicJpData1-->productId = " + lvmamaProductId;
		if(packageProductId == null && toPrice ==null && packageProductBranchId == null && selectedSuppGoodsId==null && packageGroupId == null){
			return "/order/orderProductQuery/line/realTimeFlight";
		}
		long start=System.currentTimeMillis();
		quantity = quantity!=null ?quantity:0L;
		//因为后台下单，人数已经是乘以份数的，为了兼容，先把成人数和儿童数除以这个份数
		if(quantity != 0){
			adultQuantity = adultQuantity/quantity;
			childQuantity = childQuantity/quantity;
		}
		Long productId = packageProductId;
		Long currentProductBranchId = packageProductBranchId;

		model.addAttribute("BFD_product_id",productId);
		model.addAttribute("selectedFlag",selectedFlag);
		model.addAttribute("adultQuantity",adultQuantity);//承认人数
		model.addAttribute("childQuantity",childQuantity);//儿童人数
		model.addAttribute("quantity",quantity == 0 ? "1" : quantity);//份数
		model.addAttribute("selectDate",selectDate);//游玩日期
		model.addAttribute("haveChangeButton",haveChangeButton);//选中的规格
		model.addAttribute("selectedCurrentProductBranchId",currentProductBranchId);//选中的规格
		model.addAttribute("selectedSuppGoodsId", selectedSuppGoodsId);
		model.addAttribute("transportType",transportType);

		TrafficGroupVo toGroup = new TrafficGroupVo();//对接去程
		TrafficGroupVo toGroupNotduijei = new TrafficGroupVo();//非对接去程

		Date selectDate1 = DateUtil.toDate(selectDate, "yyyy-MM-dd");
		String fromDistrict = null;
		String toDistrict = null ;

		long adultNum = adultQuantity;
		long childNum = childQuantity ;
		if(quantity > 0){
			adultNum = adultQuantity * quantity;
			childNum = childQuantity * quantity;
		}

		//对接
		ProdPackageGroup specGroup = null ;
		try{
			long d1 = new Date().getTime();
			//得到线路的产品信息
			ProdProductParam param = new ProdProductParam();
			param.setActivity(true);
			param.setComPhoto(true);
			param.setFeature(true);
			param.setViewSpot(true);
			param.setServiceRe(true);
			param.setHotelCombFlag(true);//用来判断酒店套餐，如果没有，不进行判断
			//这里的对数要用打包的产品ID而不是被打包的产品ID
			Long startTime = System.currentTimeMillis();
			ResultHandleT<ProdProduct> productHandleT = prodProductClientService.findLineProductByProductId(lvmamaProductId, param);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询线路产品详情",
					"prodProductClientService.findLineProductByProductId", System.currentTimeMillis()-startTime));

			ProdProduct product = productHandleT.getReturnContent();
			Long startDistrictId = null;
			if( product.isMultiDparture() ){	//多出发地处理----设置默认出发地
				if(req.getParameter("startDistrictId")!=null && !"".equals(req.getParameter("startDistrictId").toString().trim())) {
					try {
						startDistrictId = Long.valueOf(req.getParameter("startDistrictId"));
					}catch (Exception ex){
					}
				}
			}
			/*====================非对接start=================================*/
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("productId", lvmamaProductId);//当前产品ID
			parameterMap.put("supplierProductId", productId);//当前展示的交通产品ID
			parameterMap.put("currentProductBranchId", currentProductBranchId);
			parameterMap.put("specDate", selectDate1);
			parameterMap.put("adultQuantity", adultQuantity);
			parameterMap.put("childQuantity", childQuantity);
			parameterMap.put("distributorId", Constant.DIST_BACK_END);
			parameterMap.put("startDistrictId", startDistrictId);
			parameterMap.put("groupId", packageGroupId);
			parameterMap.put("goOrBack", packageGroupId==null?"GO":null);//去程还是返程

			PackageTourProductVo lineProductVo=null;
			startTime = System.currentTimeMillis();
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo = prodCalClientRemote.getChangePackProduct(parameterMap);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】点击“更换商品”时根据产品规格Id获取可售商品列表",
					"prodCalClientRemote.getChangePackProduct", System.currentTimeMillis()-startTime));

			Map<String, List<ProdPackageGroup>> packageMap = new HashMap<String, List<ProdPackageGroup>>();
			List<ProdPackageGroup> tempList = null;

			if (ReturnlineProductVo.getReturnContent()!= null) {
				// 初始化打包组
				lineProductVo=ReturnlineProductVo.getReturnContent();
				if(lineProductVo.getProdPackageGroupMap()!=null){
					List<ProdPackageGroup> transGroupList = lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
					if(transGroupList!=null){
						List<ProdPackageGroup> transGroupListTemp = new ArrayList<ProdPackageGroup>();
						for (ProdPackageGroup p : transGroupList) {
							if(p.getProdPackageGroupTransport()!=null && !"TOBACK".equals(p.getProdPackageGroupTransport().getTransportType())){
								transGroupListTemp.add(p);
							}
						}
						packageMap.put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), transGroupListTemp);
					}
				}
			}
			if (packageMap != null) {
				boolean isSupplier=true;
				if(product.getPackageType().equalsIgnoreCase("LVMAMA")){
					isSupplier=false;
				}

				startTime = System.currentTimeMillis();
				LineProdPackageGroupContainer container = lineProdPackageGroupServiceImpl
						.initPackageProductMap(selectDate1, packageMap,isSupplier);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】计算打包产品所有组的时间价格表",
						"lineProdPackageGroupServiceImpl.initPackageProductMap", System.currentTimeMillis()-startTime));

				if (container.isHasPackage()) {
					tempList = container.getAllPackageList();
				}
			}
			/*======================非对接END===================================*/

			ResultHandleT<List<ProdPackageGroup>> groupLists =  null ;
			try{
				startTime = System.currentTimeMillis();
				Map<String, Object> pMap = new HashMap<String, Object>();
				pMap.put("lvmamaProductId", lvmamaProductId);
				pMap.put("specDate", selectDate1);
				pMap.put("adultQuantity", adultNum);
				pMap.put("childQuantity", childNum);
				pMap.put("distributorId", Constant.DIST_FRONT_END);
				pMap.put("startDistrictId", startDistrictId);
				groupLists = prodCalClientRemote.getApiFlightProductBranch(pMap);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】更换交通时 查询对接机票规格",
						"ProdCalClientService.getApiFlightProductBranch", System.currentTimeMillis()-startTime));
			}catch(Exception e ){
				LOG.error("调用实时机票接口失败，错误信息：" + e.getMessage());
			}
			List<ProdPackageGroup> groups = null ;
			if(groupLists != null && groupLists.isSuccess()){
				groups = groupLists.getReturnContent();
			}

			//对接
			if(groups != null ){
				for(int i=0;i<groups.size();i++){
					ProdPackageGroup ppg = groups.get(i);
					if(ppg!=null && ppg.getProdPackageGroupTransport()!=null){
						if(ppg.getProdPackageGroupTransport().getfToStartDate()!=null){
							specGroup = ppg;
						}

					}
				}
			}

			//非对接
			ProdPackageGroup specGroup2 = null ;
			if(tempList != null){
				for(int i=0;i<tempList.size();i++){
					ProdPackageGroup ppg = tempList.get(i);
					if(ppg!=null && ppg.getProdPackageGroupTransport()!=null){
						if(ppg.getProdPackageGroupTransport().getToStartDate()!=null){
							specGroup2 = ppg;
						}

					}
				}
			}
			assembleGroup(specGroup, fromDistrict, toDistrict, selectDate1,toGroup,d1,true); //对接去程
			assembleGroup(specGroup2, fromDistrict, toDistrict, selectDate1,toGroupNotduijei,d1,false);// 组装非对接group信息 去程

		}catch(Exception e ){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("拼装group,调用远程接口有问题");
		}

		//获取远程用户的IP和sessionId
		LvfSignVo sign = new LvfSignVo();
		sign.setIp(req.getRemoteAddr());
		sign.setSessionId(req.getSession().getId());
		Long startTime = null;
		try{
			long d1 = new Date().getTime();
			startTime = System.currentTimeMillis();
			ResultHandleT<TrafficGroupVo> result = flightSearchService.queryFlightMsgByDateAndBranch(sign ,selectDate1, adultQuantity, childQuantity, toGroup);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【对接】查询航班信息",
					"flightSearchService.queryFlightMsgByDateAndBranch", System.currentTimeMillis()-startTime));
			if(result.isSuccess()){
				toGroup = result.getReturnContent();
			}
			LOG.info("时间戳"+start+"@后台下单更换交通调用机票组接口用时@FlightSearchService.queryFlightMsgByDateAndBranch@" + String.valueOf(new Date().getTime() -  d1) );
		}catch(Exception e ){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("获取航班信息失败~");
		}
		//商品IDselectedSuppGoodsId 改为 选择标识 selectedFlag 因为非对接机票商品ID不能唯一识别
		try {
			if(specGroup != null) {
				AutoPackageUtil.filterOneGroupAirLineGoods(toGroup, specGroup.getProdPackageGroupTransport());
			}
			getFlights(model,toGroup, toGroupNotduijei, fromDistrict, toDistrict, selectedFlag, toPrice, adultNum, childNum, "TO");
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("组装前台去程展示数据flights/data 等 时出现异常");
		}
		LOG.info("时间戳"+start+"@后台下单更换交通总用时@OrderLineProductQueryAction.getBasicJpData@" + (System.currentTimeMillis()-start) );

		return "/order/orderProductQuery/line/realTimeFlight";
	}

	@RequestMapping(value = "/ord/order/getBasicJpData1Back.do")
	public String getBasicJpData1Back(Model model,String backline,HttpServletRequest req,HttpServletResponse Resp,
             Long adultQuantity,Long childQuantity,String selectDate,String selectedFlag,
             Long backPackageProductId,Long backPrice,Long backPackageProductBranchId,Long backSelectedSuppGoodsId,Long backPackageGroupId,
             Long quantity,String changeType,String haveChangeButton,Long lvmamaProductId,Long oldPrice,String transportType) {

		long start=System.currentTimeMillis();
		if(backPackageProductId == null && backPrice ==null && backPackageProductBranchId == null && backSelectedSuppGoodsId==null && backPackageGroupId == null){
			return "/order/orderProductQuery/line/backline_table";
		}
		quantity = quantity!=null ?quantity:0L;
		//因为后台下单，人数已经是乘以份数的，为了兼容，先把成人数和儿童数除以这个份数
		if(quantity != 0){
			adultQuantity = adultQuantity/quantity;
			childQuantity = childQuantity/quantity;
		}
		Long productId = backPackageProductId;

		model.addAttribute("BFD_product_id",productId);
		model.addAttribute("selectedFlag",selectedFlag);
		model.addAttribute("adultQuantity",adultQuantity);//承认人数
		model.addAttribute("childQuantity",childQuantity);//儿童人数
		model.addAttribute("quantity",quantity == 0 ? "1" : quantity);//份数
		model.addAttribute("selectDate",selectDate);//游玩日期
		model.addAttribute("haveChangeButton",haveChangeButton);
		model.addAttribute("transportType",transportType);

		Long backCurrentProductBranchId = backPackageProductBranchId;
		model.addAttribute("backSelectedCurrentProductBranchId",backCurrentProductBranchId);//选中的规格
		model.addAttribute("backSelectedSuppGoodsId", backSelectedSuppGoodsId);


		TrafficGroupVo backGroup = new TrafficGroupVo();//对接返程
		TrafficGroupVo backGroupNotduijei = new TrafficGroupVo();//非对接返程

		Date selectDate1 = DateUtil.toDate(selectDate, "yyyy-MM-dd");
		String backFromDistrict = null;
		String backToDistrict = null ;

		long adultNum = adultQuantity;
		long childNum = childQuantity ;
		if(quantity > 0){
			adultNum = adultQuantity * quantity;
			childNum = childQuantity * quantity;
		}
		//对接
		ProdPackageGroup backSpecGroup = null ;
		try{
			long d1 = new Date().getTime();
			//得到线路的产品信息
			ProdProductParam param = new ProdProductParam();
			param.setActivity(true);
			param.setComPhoto(true);
			param.setFeature(true);
			param.setViewSpot(true);
			param.setServiceRe(true);
			param.setHotelCombFlag(true);//用来判断酒店套餐，如果没有，不进行判断
			//这里的对数要用打包的产品ID而不是被打包的产品ID
			ResultHandleT<ProdProduct> productHandleT = prodProductClientService.findLineProductByProductId(lvmamaProductId, param);
			ProdProduct product = productHandleT.getReturnContent();
			Long startDistrictId = null;
			if( product.isMultiDparture() ){	//多出发地处理----设置默认出发地
				if(req.getParameter("startDistrictId")!=null && !"".equals(req.getParameter("startDistrictId").toString().trim())) {
					try {
						startDistrictId = Long.valueOf(req.getParameter("startDistrictId"));
					}catch (Exception ex){

					}
				}
			}
			/*====================非对接start=================================*/
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("productId", lvmamaProductId);//当前产品ID
			parameterMap.put("supplierProductId", productId);//当前展示的交通产品ID
			parameterMap.put("currentProductBranchId", backCurrentProductBranchId);
			parameterMap.put("specDate", selectDate1);
			parameterMap.put("adultQuantity", adultQuantity);
			parameterMap.put("childQuantity", childQuantity);
			parameterMap.put("distributorId", Constant.DIST_BACK_END);
			parameterMap.put("startDistrictId", startDistrictId);
			parameterMap.put("groupId", backPackageGroupId);
			parameterMap.put("goOrBack", backPackageGroupId==null?"BACK":null);//去程还是返程


			PackageTourProductVo lineProductVo=null;
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo = prodCalClientRemote.getChangePackProduct(parameterMap);
			Map<String, List<ProdPackageGroup>> packageMap = new HashMap<String, List<ProdPackageGroup>>();

			if (ReturnlineProductVo.getReturnContent()!= null) {
				// 初始化打包组
				lineProductVo=ReturnlineProductVo.getReturnContent();
				if(lineProductVo.getProdPackageGroupMap()!=null){
					//这里只需要使用交通组
					List<ProdPackageGroup> transGroupList = lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
					packageMap.put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), transGroupList);
				}
			}
			List<ProdPackageGroup> transGroupListTemp = null;
			if (packageMap != null) {
				boolean isSupplier=true;
				if(product.getPackageType().equalsIgnoreCase("LVMAMA")){
					isSupplier=false;
				}
				LineProdPackageGroupContainer container = lineProdPackageGroupServiceImpl
						.initPackageProductMap(selectDate1, packageMap,isSupplier);
				if (container.isHasPackage()) {
					transGroupListTemp = container.getAllPackageList();
				}
			}

			List<ProdPackageGroup> tempList = new ArrayList<ProdPackageGroup>();
			if(transGroupListTemp!=null){
				for (ProdPackageGroup p : transGroupListTemp) {
					//这里只需要单程交通，去掉往返交通
					if(p.getProdPackageGroupTransport()!=null && !"TOBACK".equals(p.getProdPackageGroupTransport().getTransportType())){
						tempList.add(p);
					}
				}
			}
			/*======================非对接END===================================*/

			ResultHandleT<List<ProdPackageGroup>> groupLists =  null ;
			try{
				long startK=System.currentTimeMillis();
				Map<String, Object> pMap = new HashMap<String, Object>();
				pMap.put("lvmamaProductId", lvmamaProductId);
				pMap.put("specDate", selectDate1);
				pMap.put("adultQuantity", adultNum);
				pMap.put("childQuantity", childNum);
				pMap.put("distributorId", Constant.DIST_FRONT_END);
				pMap.put("startDistrictId", startDistrictId);
				groupLists = prodCalClientRemote.getApiFlightProductBranch(pMap);
				long endK=System.currentTimeMillis();
				LOG.info("时间戳"+start+"@线路后台更换对接机票交通调用用时@ProdCalClientService.getApiFlightProductBranch@"+(endK-startK));
			}catch(Exception e ){
				LOG.error("调用实时机票接口失败，错误信息：" + e.getMessage());
			}
			List<ProdPackageGroup> groups = null ;
			if(groupLists != null && groupLists.isSuccess()){
				groups = groupLists.getReturnContent();
			}

			//对接
			if(groups != null ){
				for(int i=0;i<groups.size();i++){
					ProdPackageGroup ppg = groups.get(i);
					if(ppg!=null && ppg.getProdPackageGroupTransport()!=null){
						if(ppg.getProdPackageGroupTransport().getfBackStartDate()!=null){
							backSpecGroup = ppg;
						}

					}
				}
			}

			//非对接
			ProdPackageGroup backSpecGroup2 = null ;
			if(tempList != null){
				for(int i=0;i<tempList.size();i++){
					ProdPackageGroup ppg = tempList.get(i);
					if(ppg!=null && ppg.getProdPackageGroupTransport()!=null){
						if(ppg.getProdPackageGroupTransport().getBackStartDate()!=null){
							backSpecGroup2 = ppg;
						}

					}
				}
			}

//			String tDistrict = "";
//			String bDistrict = "";
//			String tDuijieDistrict = "";
//			String bDuijieDistrict = "";
			assembleGroup(backSpecGroup, backFromDistrict, backToDistrict, selectDate1,backGroup,d1,true);//对接返程
			assembleGroup(backSpecGroup2, backFromDistrict, backToDistrict, selectDate1,backGroupNotduijei,d1,false);// 组装非对接group信息 返程

			//设置往返程的出发目的地 start
//			if(tDuijieDistrict!=null){
//				fromDistrict = tDuijieDistrict.split("--")[0];
//				toDistrict = tDuijieDistrict.split("--")[1];
//			}else if(tDistrict!=null){
//				fromDistrict = tDistrict.split("--")[0];
//				toDistrict = tDistrict.split("--")[1];
//			}
//			if(bDuijieDistrict!=null){
//				backFromDistrict = tDuijieDistrict.split("--")[0];
//				backToDistrict = tDuijieDistrict.split("--")[1];
//			}else if(bDistrict!=null){
//				backFromDistrict = tDistrict.split("--")[0];
//				backToDistrict = tDistrict.split("--")[1];
//			}
			//设置往返程的出发目的地 start

		}catch(Exception e ){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("拼装group,调用远程接口有问题");
		}

		//获取远程用户的IP和sessionId
		LvfSignVo sign = new LvfSignVo();
		sign.setIp(req.getRemoteAddr());
		sign.setSessionId(req.getSession().getId());
		try{
			long d1 = new Date().getTime();
			ResultHandleT<TrafficGroupVo> backResult = flightSearchService.queryFlightMsgByDateAndBranch(sign ,selectDate1, adultQuantity, childQuantity, backGroup);
			if(backResult.isSuccess()){
				backGroup = backResult.getReturnContent();
			}
			LOG.info("时间戳"+start+"@后台下单更换交通调用机票组接口用时@FlightSearchService.queryFlightMsgByDateAndBranch@" + String.valueOf(new Date().getTime() -  d1) );
		}catch(Exception e ){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("获取航班信息失败~");
		}

		//
		try {
			if(backSpecGroup != null) {
				AutoPackageUtil.filterOneGroupAirLineGoods(backGroup, backSpecGroup.getProdPackageGroupTransport());
			}
			getFlights(model, backGroup, backGroupNotduijei, backFromDistrict, backToDistrict, selectedFlag, backPrice, adultNum, childNum, "BACK");
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("组装前台返程展示数据flights/data 等 时出现异常");
		}
		LOG.info("时间戳"+start+"@后台下单更换交通总用时@OrderLineProductQueryAction.getBasicJpData@" + (System.currentTimeMillis()-start) );

		return "/order/orderProductQuery/line/backline_table";
	}

	/**
	 * 组装前台展示数据0
	 * @param toGroup
	 * @param toGroupNotduijei
	 * @param fromDistrict
	 * @param toDistrict
	 * @param oldPrice
	 * @param adultNum
	 * @param childNum
	 * @param type
	 */
	private void getFlights(Model model, TrafficGroupVo toGroup,TrafficGroupVo toGroupNotduijei,String fromDistrict,String toDistrict,
			String selectedFlag,long oldPrice,long adultNum,long childNum,String type)throws Exception{

		if(toGroup!=null && toGroup.getTrafficVoList()==null &&
				toGroupNotduijei!=null && toGroupNotduijei.getTrafficVoList()==null){
			return;
		}

		Map<String, String> suppGoodsDJFlagMap = new HashMap<String, String>();
		assembleDJFlag(suppGoodsDJFlagMap, toGroup, "Y");
		assembleDJFlag(suppGoodsDJFlagMap, toGroupNotduijei, "N");
		model.addAttribute("suppGoodsDJFlagMap", suppGoodsDJFlagMap);
		String suppGoodsDJFlagMapStr = JSONObject.fromObject(suppGoodsDJFlagMap).toString();
		model.addAttribute("suppGoodsDJFlagMapStr", suppGoodsDJFlagMapStr);
		if(LOG.isDebugEnabled()) {
			LOG.debug(suppGoodsDJFlagMapStr);
		}

		/*
		 * 将对接与非对接组装到一起
		 */
		if(toGroup == null){
			toGroup = toGroupNotduijei;
		}else if(toGroup.getTrafficVoList()!=null && toGroupNotduijei.getTrafficVoList()!=null){
			toGroup.getTrafficVoList().addAll(toGroupNotduijei.getTrafficVoList());
		}else if(toGroup.getTrafficVoList() == null && toGroupNotduijei.getTrafficVoList() != null){
			toGroup.setAdultAmt(toGroupNotduijei.getAdultAmt());
			toGroup.setBackDate(toGroupNotduijei.getBackDate());
			toGroup.setCheapestGoodsId(toGroupNotduijei.getCheapestGoodsId());
			toGroup.setChildAmt(toGroupNotduijei.getChildAmt());
			toGroup.setGoDate(toGroupNotduijei.getGoDate());
			toGroup.setGroupId(toGroupNotduijei.getGroupId());
			toGroup.setTrafficVoList(toGroupNotduijei.getTrafficVoList());
		}

		if(toGroup == null){
			return;
		}

		Date fromDate = toGroup.getGoDate();
		String from = fromDistrict;
		String to = toDistrict;


		Map<String, Object> planeTicketMap = new HashMap<String,Object>();    //最终返回该Map
		List<String> airCompanies = new ArrayList<String>();      //航空公司
		List<String> spaceList = new ArrayList<String>();
		planeTicketMap.put("airCompanies", airCompanies);
		planeTicketMap.put("spaces", spaceList);

		if(fromDate == null ){
			fromDate = toGroup.getBackDate();
		}
		planeTicketMap.put("day", DateUtil.getZHDay(fromDate)); //飞机去程 星期
		planeTicketMap.put("date", DateUtil.formatDate(fromDate,"yyyy-MM-dd"));
		planeTicketMap.put("from", from);
		planeTicketMap.put("to", to);


		List<Map<String, Object>> flights = scaleFlight(toGroup,airCompanies,spaceList,selectedFlag,false,oldPrice,adultNum,childNum);

		//航班信息
		JSONArray jsonArray = JSONArray.fromObject(flights);

		String jsonstr = jsonArray.toString();
		if("TO".equals(type)){
			model.addAttribute("flights", flights);
			model.addAttribute("DJJPhasBackLine", false);//是否有返程
			model.addAttribute("flightJsonStr", jsonstr);
			model.addAttribute("data", planeTicketMap);
		}else if("BACK".equals(type)){
			model.addAttribute("backFlights", flights);
			model.addAttribute("backDJJPhasBackLine", false);//是否有返程
			model.addAttribute("backFlightJsonStr", jsonstr);
			model.addAttribute("backData", planeTicketMap);
		}
	}
	/**
	 * 拼装group信息
	 * @param specGroup
	 * @param fromDistrict
	 * @param toDistrict
	 * @param selectDate1
	 * @param group
	 * @param d1
	 * @return
	 */
	public String assembleGroup(ProdPackageGroup specGroup, String fromDistrict,
							String toDistrict, Date selectDate1, TrafficGroupVo group,Long d1,
							boolean isDuijie){
		if (specGroup != null) {
			ProdPackageGroup prodPackageGroup = specGroup;

			ProdPackageGroupTransport prodPackageGroupTransport = prodPackageGroup.getProdPackageGroupTransport();
			Long toStartDays = prodPackageGroupTransport.getToStartDays();// 第几天走
			Long backStartDays = prodPackageGroupTransport.getBackStartDays();// 第几天回

			// 设置出发地和目的地,单程
			if (prodPackageGroupTransport != null && prodPackageGroupTransport.getTransportType().equals("TO")) {
			// 判断有没有去程
			if (prodPackageGroupTransport.getToStartPointDistrict() != null) {
			    if (prodPackageGroupTransport.getToStartPointDistrict().getDistrictName() != null) fromDistrict = prodPackageGroupTransport.getToStartPointDistrict().getDistrictName();
			    if (prodPackageGroupTransport.getToDestinationDistrict().getDistrictName() != null) toDistrict = prodPackageGroupTransport.getToDestinationDistrict().getDistrictName();
			} else {
			    // 没有去程就是返程
			    if (prodPackageGroupTransport.getBackStartPointDistrict().getDistrictName() != null) fromDistrict = prodPackageGroupTransport.getBackStartPointDistrict().getDistrictName();
			    if (prodPackageGroupTransport.getBackDestinationDistrict().getDistrictName() != null) toDistrict = prodPackageGroupTransport.getBackDestinationDistrict().getDistrictName();
			}
			// 往返程
			}

			Date date1 = null;
			if (toStartDays != null) {
				date1 = DateUtils.addDays(selectDate1, Long.valueOf(toStartDays).intValue() - 1);
			}

			Date date2 = null ;
			if (backStartDays != null) {
			date2 = DateUtils.addDays(selectDate1, Long.valueOf(backStartDays).intValue() - 1);
			}

			// ************************
			// groupId
			group.setGroupId(prodPackageGroup.getGroupId());
			// 去程时间
			if(date1 !=null ){
			group.setGoDate(date1);
			}
			if(date2 !=null ){
			group.setBackDate(date2);
			}
			List<TrafficVo> trafficVoList = null;
			TrafficVo trafficVo = null;
			List<BranchVo> branchVoList = null;
			BranchVo branchVo = null;

			List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();

			if (prodPackageDetails != null && prodPackageDetails.size() > 0) {
			// 先对productid排序，分组
			Collections.sort(prodPackageDetails, new Comparator<ProdPackageDetail>() {

			    @Override
			    public int compare(ProdPackageDetail o1, ProdPackageDetail o2) {
			        if(o1.getProdProductBranch()==null){
			            return -1;
			        }
			        if(o2.getProdProductBranch()==null ){
			            return 1;
			        }
			        if (o1.getProdProductBranch().getProductId() >= o2.getProdProductBranch().getProductId()) {
			            return 1;
			        }
			        return -1;
			    }

			});
			// **********************************************************
			// 关系
			trafficVoList = new ArrayList<TrafficVo>();
			ProdProductBranch branchTemp = null;
			for (ProdPackageDetail prodPackageDetail : prodPackageDetails) {
				List<FlightNoVo>goodsList = new ArrayList<FlightNoVo>();
				Long detailId = prodPackageDetail.getDetailId();
				//只有非对接的时候这里才需要组装
				if(!isDuijie && prodPackageDetail.getProdProduct()!=null &&
						prodPackageDetail.getProdProduct().getProdTrafficVO()!=null){
					List<ProdTrafficGroup> prodTrafficGrouptList = prodPackageDetail.getProdProduct().getProdTrafficVO().getProdTrafficGroupList();
					if(prodTrafficGrouptList != null){
						for (ProdTrafficGroup prodTrafficGroup : prodTrafficGrouptList) {
							List<ProdTrafficFlight> pfs = prodTrafficGroup.getProdTrafficFlightList();
							if(pfs!=null){
								for (ProdTrafficFlight p : pfs) {
									BizFlight bf = p.getBizFlight();
									if(bf!=null){
										FlightNoVo fVo = new FlightNoVo();
										fVo.setSeatCode(BizFlight.CABIN.getCode(prodPackageDetail.getProdProductBranch().getBranchName()));//舱位
										fVo.setSeatName(prodPackageDetail.getProdProductBranch().getBranchName());//舱位
										fVo.setFlightNo(p.getFlightNo());//航班号
										fVo.setCompanyCode(bf.getAirline()+"");//航空公司
										fVo.setCompanyName(bf.getAirlineString());//航空公司
										fVo.setPlaneCode(bf.getAirplane()+"");//机型编号
										fVo.setGoTime(bf.getStartTime()==null?null:DateUtil.toDate(bf.getStartTime(), "hh:mm"));//出发时间 日期格式
										fVo.setArriveTime(bf.getArriveTime()==null?null:DateUtil.toDate(bf.getArriveTime(), "hh:mm"));//到达时间
										fVo.setFromAirPort(bf.getStartAirportString());//始发机场
										fVo.setToAirPort(bf.getArriveAirportString());//到达机场
										fVo.setGoodsId(prodPackageDetail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId()); //goodId
										fVo.setFromCityName(p.getFromCityName());//出发城市
										fVo.setToCityName(p.getToCityName());//到达城市

										SuppGoodsBaseTimePrice baseTimePrice = new SuppGoodsLineTimePriceVo();
										baseTimePrice = prodPackageDetail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice();
										SuppGoodsLineTimePriceVo lineTimePrice = (SuppGoodsLineTimePriceVo) baseTimePrice;
										if(lineTimePrice!=null &&
												(SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_NO_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())
												|| ("Y".equalsIgnoreCase(lineTimePrice.getOversellFlag())
														&& (lineTimePrice.getStock() == 0 || lineTimePrice.getStock() == null)))){
											fVo.setRemain(-1l);//剩余数
										}else{
											fVo.setRemain(prodPackageDetail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice().getStock());//剩余数
										}
										fVo.setFoodSupport(false);//餐食
										fVo.setAdultAmt(prodPackageDetail.getProdProductBranch().getAdultPrice());//成人价
										fVo.setChildAmt(prodPackageDetail.getProdProductBranch().getChildPrice());//儿童价
										goodsList.add(fVo);
									}
								}
							}
						}
					}
				}
			    ProdProductBranch branch = prodPackageDetail.getProdProductBranch();
			    if (branch != null) {

			        // 和之前记录的tmp比较，如果不相等，那么重新new trafficVoList
			        if (branchTemp != null && !branchTemp.getProductId().equals(branch.getProductId())) {
			            // 产品属性
			            trafficVo = new TrafficVo();
			            trafficVo.setTrafficId(branch.getProductId());
			            trafficVoList.add(trafficVo);
			            branchVoList = new ArrayList<BranchVo>();
			            trafficVo.setBranchVoList(branchVoList);
			        } else if (branchTemp == null) {
			            trafficVo = new TrafficVo();
			            trafficVo.setTrafficId(branch.getProductId());
			            trafficVoList.add(trafficVo);
			            branchVoList = new ArrayList<BranchVo>();
			            trafficVo.setBranchVoList(branchVoList);
			        }

			        // 规格属性
			        boolean branchExits = false;
			        for(BranchVo v : branchVoList){
			            if(v.getBranchId() == branch.getProductBranchId()){
			                branchExits = true;
			            }
			        }
			        //不存在，则添加
			        if(!branchExits){
			            branchVo = new BranchVo();
			            branchVo.setBranchId(branch.getProductBranchId());
			            branchVo.setDetailId(detailId);
			            if(!isDuijie){
			            	branchVo.setGoodsList(goodsList);// 非对接才需要设置
			            }
			            branchVoList.add(branchVo);
			        }
			    }
			    // 记录当前的product
			    branchTemp = branch;

			}
			branchTemp = null;
			group.setTrafficVoList(trafficVoList);
		}
			LOG.info("拼装group信息，耗时：" + String.valueOf(new Date().getTime() -  d1) );
			return fromDistrict+"--"+toDistrict;
		}else{
			LOG.info("没有该打包组");
			HttpServletLocalThread.getModel().addAttribute("ERROR", "没有找到该交通组");
			return "order/error";
		}
	}

	/**
	 * 选择替换商品，请求附加信息列表
	 *
	 * @return
	 */
	@RequestMapping(value = "/ord/order/queryPackageMoreAddition.do")
	public String queryPackageMoreAdditionInfo(ModelMap model, Long productId, Long packageProductId, Long groupId, Long productBranchId,
			Long currentSuppGoodsId, Date specDate, Long adultQuantity,
			Long childQuantity,Long productItemIdIndex,Date realSpecDate) {
		try {
			PackageTourProductVo lineProductVo=null;
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo = prodCalClientRemote
					.getChangeSuppGoods(productId,
								packageProductId,
								groupId,
								productBranchId,
								currentSuppGoodsId,
								specDate,
								adultQuantity,
								childQuantity,
								Constant.DIST_BACK_END);
			List<ProdProductBranch> changeAdditionList=null;
			if(ReturnlineProductVo.getReturnContent()!=null)
			{
				lineProductVo=ReturnlineProductVo.getReturnContent();
				//附加信息
				 changeAdditionList= lineProductVo.getProductBranchList();
				this.initAdditionProductBranchList(new HashMap<Long, SuppGoodsRelation>(), changeAdditionList,
						realSpecDate,null);
			}


			lineProductVo.setProductId(productId);
			model.addAttribute("packageTourProductVo", lineProductVo);
			model.addAttribute("productId", productId);
			model.addAttribute("groupId", groupId);
			model.addAttribute("productBranchId", productBranchId);
			model.addAttribute("currentSuppGoodsId", currentSuppGoodsId);
			model.addAttribute("specDate", specDate);
			model.addAttribute("adultQuantity", adultQuantity);
			model.addAttribute("childQuantity", childQuantity);


			model.addAttribute("changeAdditionList", changeAdditionList);
			model.addAttribute("isLvmamaProduct", true);
			model.addAttribute("productItemIdIndex", productItemIdIndex);
		} catch (Exception e1) {
			LOG.error("{}", e1);
		}
		return "/order/orderProductQuery/line/changeAdditionU";
	}


	/***
	 * 根据产品或者商品ID查询当前产品/商品的最大数据  最小1
	 * @param model
	 * @param productId
	 */
	private void loadPriceAllMonthById(ModelMap model,String productId,Date specDate){

		 ResultMessage msg = ResultMessage.createResultMessage();
			try {
				Map<String, Object> params = new HashMap<String, Object>();
		        params.put("productId", productId);
		        //以当前的时间为开始点
		        params.put("beginDate", new Date());
		        Long productIdL=Long.parseLong(productId);
		        ResultHandleT<List<ProdGroupDate>> resultHandleT = prodGroupDateClientService.findProdGroupDateListByParam(productIdL, params);

		        if(resultHandleT.isFail()||resultHandleT.getReturnContent()==null){
					msg.raise(resultHandleT.getMsg());
					LOG.info("in /ord/order/queryLineDetailList.do load timePriceList is null");
				}

		        int mothResult = 0;
				int yearResult=0;
				Calendar c1 = Calendar.getInstance();
				Calendar c2 = Calendar.getInstance();
				if (resultHandleT!=null && resultHandleT.getReturnContent()!=null) {
					if (resultHandleT.getReturnContent().size()>0) {
						c1.setTime(resultHandleT.getReturnContent().get(0).getSpecDate());
						Date lastDate=resultHandleT.getReturnContent().get(resultHandleT.getReturnContent().size()-1).getSpecDate();
						c2.setTime(lastDate);//获取最后一条数据的日期

						mothResult = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
						yearResult=c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
						mothResult=yearResult*12+mothResult;
						model.addAttribute("startDaeResult", resultHandleT.getReturnContent().get(0).getSpecDate().getTime());

					}
				}

				if( mothResult<0 )	mothResult = 0;
				mothResult ++;

				model.addAttribute("mothResult", mothResult);

			}catch (Exception e){
				LOG.error(ExceptionFormatUtil.getTrace(e));
				LOG.info("in /ord/order/queryLineDetailList.do load timePriceList have exception");
			}

		/**日历框根据时间价计算总数据月数 by 李志强  2015-03-09**/
	}

	/**
	 * 查询产品的可售出发地信息
	 * @param prodProduct			产品信息
	 * @param firstDistrictId		第一出发地
	 * @return
	 */
	private ProdStartDistrictAdditionalVO adapterStartDistrict(ProdProduct prodProduct, Long firstDistrictId){
		List<BizDistrict> districts = prodProduct.getBizDistricts();
		Map<Long, BizDistrict> mapCache = new HashMap<Long, BizDistrict>();
		if(districts != null) {
			for(BizDistrict d : districts) {
				d.setCancelFlag("N");
				mapCache.put(d.getDistrictId(), d);
			}
		}

		if( !"Y".equalsIgnoreCase( prodProduct.getMuiltDpartureFlag() ) || prodProduct.getProductId()==null ){
			return null;
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("productId", prodProduct.getProductId());
		//查询可售出发地列表
		ResultHandleT<List<ProdStartDistrictAdditionalVO>> resultT = prodCalClientRemote.getProdStartDistrictForSale(paramMap);
		if(resultT.isFail())	return null;
		List<ProdStartDistrictAdditionalVO> retList = resultT.getReturnContent();
		if(retList==null || retList.size()<=0)	return null;

		//判断出发地是否可售
		if(districts != null) {
			for(ProdStartDistrictAdditionalVO startVo: retList) {
				BizDistrict d = mapCache.get(startVo.getStartDistrictId());
				if(d != null) {
					d.setCancelFlag("Y");
				}
			}
		}

		for(ProdStartDistrictAdditionalVO startVo: retList){
			if(firstDistrictId==null && startVo.getStartDistrictId()!=null && startVo.getStartDistrictId().longValue()==9){	//如果没有设置出发地ID，优先返回 上海出发（9）
				return startVo;
			}
			if(firstDistrictId!=null && firstDistrictId.equals( startVo.getStartDistrictId() ) ){
				return startVo;
			}
		}
		return retList.get(0);															//如果没有匹配的出发地，取第一条出发地
	}

	/**
	 * 查询详细信息
	 *
	 * @return
	 */
	@RequestMapping(value = "/ord/order/queryLineDetailList.do")
	public String infoFillIn(LineBackOneKeyOrderVO oneKeyOrderVO,
			ModelMap model, String productId, Date specDate, String userId, Long adultNum, Long childNum, Long copies,
			Long intentionOrderId,String fromSearch, HttpServletResponse response,HttpServletRequest req) {
		//如果是一键下单，设置一键下单方式
		Long originalOrderId = null;
		if(oneKeyOrderVO != null){
			originalOrderId = oneKeyOrderVO.getOriginalOrderId();
			model.put("orderCreatingManner", oneKeyOrderVO.getOrderCreatingManner());
			model.put("originalOrderId", originalOrderId);
		}
		//判断是否是人数更改后重新提交的表单
		boolean noChangeAdu=adultNum!=null;
		boolean noChangeChild=childNum!=null;
		if(adultNum==null && childNum==null){
			model.put("checkOldOughtAmount", "0");
		}else{
			model.put("checkOldOughtAmount", "1");
		}

		// 首先第一次查询商品，判断是否为酒店套餐，以便计算成人数儿童数
		ProdProductParam param = new ProdProductParam();
		param.setHotelCombFlag(true);
		Long startTime = null;
		String methodName = "OrderLineProductQueryAction#infoFillIn-->productId = " + productId;
		startTime = System.currentTimeMillis();
		ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(Long.parseLong(productId), param);
		LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询线路产品详情", "prodProductClientService.findLineProductByProductId", System.currentTimeMillis()-startTime));

		ProdProduct prodProduct = null;
		if (resultHandle != null && resultHandle.isSuccess() && !resultHandle.hasNull()) {
			prodProduct = resultHandle.getReturnContent();
		} else {
			model.addAttribute("ERROR", "商品Loading接口拨错，是否包含酒店套餐，商品不可售");
			return "order/error";
		}

		Long startDistrictId = null;
		ProdStartDistrictAdditionalVO startDistrictVo = null;	//多出发地时使用
		if("Y".equalsIgnoreCase( prodProduct.getMuiltDpartureFlag() ) ){	//多出发地处理----设置默认出发地
			try {
				if(req.getParameter("startDistrictId")!=null && !"".equals(req.getParameter("startDistrictId").toString().trim())) {
					try {
						startDistrictId = Long.valueOf(req.getParameter("startDistrictId"));
					}catch (Exception ex){}
				}
				startDistrictVo = adapterStartDistrict(prodProduct, startDistrictId);
				if(startDistrictVo!=null)	startDistrictId = startDistrictVo.getStartDistrictId();
			}catch(Exception ex){
				startDistrictId = null;
				log.info("未输入出发地");
			}
		}
		model.addAttribute("startDistrictVo", startDistrictVo);

		if(StringUtils.isNotBlank(req.getParameter("channel_code")) && !"no_o2o".equals(req.getParameter("channel_code"))){
			model.addAttribute("channel_code",req.getParameter("channel_code").toString());
		}
		//取到产品附加
		startTime = System.currentTimeMillis();
		ProdProductAddtional productAddtional = prodProductAdditionClientRemote.selectByPrimaryKey(Long.parseLong(productId));
		LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询线路产品附加信息", "prodProductAdditionClientRemote.selectByPrimaryKey", System.currentTimeMillis()-startTime));
		
		//添加锁仓前置的标识
		String isPreLockSeatBack =  Constant.getInstance().getProperty("isPreLockSeatBack");
		if("Y".equals(isPreLockSeatBack)){
			try {
				if(Constant.BU_NAME.LOCAL_BU.getCode().equals(prodProduct.getBu())){
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(prodProduct.getBizCategoryId())
							&&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(prodProduct.getSubCategoryId())){
						model.put("isPreLockSeat", true);
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(prodProduct.getBizCategoryId())){
						ResultHandleT<ProdAdditionFlag> resultHandleT = prodAdditionFlagClientService.selectByProductId(Long.parseLong(productId));
						if(resultHandleT!=null&&resultHandleT.getReturnContent()!=null&&"Y".equals(resultHandleT.getReturnContent().getSeatFlag())){
							model.put("isPreLockSeat", true);
						}
					}
				}
			} catch (Exception e) {
				LOG.info(e.getMessage());
			}		
		}

		Long adultMinNum00 = 1L;
		Long childMinNum00 = 0L;
        if(productAddtional != null){
    		//酒店套餐份数 或 成人数
        	if(productAddtional.getAdultMinQuantity() != null){
        		adultMinNum00 = productAddtional.getAdultMinQuantity();
        	}
    		//儿童数
        	if(productAddtional.getChildMinQuantity() != null){

        		childMinNum00 = productAddtional.getChildMinQuantity();
        	}
        }

		//是否包含酒店套餐,N包含，Y不包含
		boolean isHotel = false;
		Long baseChildNum = 0L;
		Long baseAdultNum = 0L;
		boolean saleCopies=false;
		boolean isDestinationBU = ProductPreorderUtil.isDestinationBUDetail(prodProduct);


		if(CollectionUtils.isNotEmpty(prodProduct.getProdProductSaleReList())){
			String saleType=prodProduct.getProdProductSaleReList().get(0).getSaleType();
			saleCopies=ProdProductSaleRe.SALETYPE.COPIES.name().equals(saleType);
			LOG.info("---------------是否按份计算------------------"+saleCopies+"");
		}
		if (prodProduct.getHotelCombFlag().equals("N") || saleCopies ) {
			LOG.info("---------------开始按份计算------------------"+saleCopies+"");
			model.put("productSaleType","COPIES");
			if(saleCopies){
				baseAdultNum=prodProduct.getProdProductSaleReList().get(0).getAdult().longValue();
				baseChildNum=prodProduct.getProdProductSaleReList().get(0).getChild().longValue();
			}else if(prodProduct.getHotelCombFlag().equals("N")){
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
			if(!noChangeAdu){
				adultNum = baseAdultNum * copies;
			}
			if(!noChangeChild){
				childNum = baseChildNum * copies;
			}
			if(prodProduct.getHotelCombFlag().equals("N") ){
				isHotel = true;
			}

		} else {
			if (!noChangeAdu) {
				adultNum = adultMinNum00;
				if(isDestinationBU && adultMinNum00 < 2){//目的地BU，最小默认值选择为2
					adultNum = 2L;
				}
			}

			if (!noChangeChild) {

				childNum = childMinNum00==-1?0:childMinNum00;
			}
		}
		//后台下单，机+酒、自由行  儿童数=成人数*2
		boolean isRoute = false;
		if(prodProduct.isLocalBuProduct() && !(prodProduct.getBizCategoryId() == 18L && prodProduct.getSubCategoryId() == 183L)){
			isRoute = true;
		}
		model.put("isRoute", isRoute);
		model.put("categoryId", prodProduct.getCategoryId());
		// 返回单份成人数儿童数
		model.put("baseChildNum", baseChildNum);
		model.put("baseAdultNum", baseAdultNum);
		// 返回总的成人数儿童数
		model.put("childNum", (childNum==null || childNum<0)?0:childNum);
		model.put("adultNum", adultNum);

		//添加产品公告

		HashMap<String, Object> params = new HashMap<String,Object>();
		String  startTimeStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		params.put("productId", productId);
		params.put("startTimeStr", startTimeStr);
		params.put("cancelFlag","Y" );

		ResultHandleT<List<ProdProductNotice>> productNoticeResult = prodProductNoticeService1.findProductNoticeList_asc(params);
		List<ProdProductNotice> noticeList = productNoticeResult.getReturnContent();
		if(CollectionUtils.isNotEmpty(noticeList)){
			model.put("productNoticList", noticeList);
		}
		LOG.info("-----------公告长度,公告长度---------"+noticeList.size());
		LOG.info("-----------公告长度,公告长度---------"+noticeList.size());
		LOG.info("-----------公告长度,公告长度---------"+noticeList.size());
		LOG.info("-----------公告长度,公告长度---------"+noticeList.size());

		String result = "/order/orderProductQuery/findLineProductDetailList";

		//一键下单数量
        HashMap<String,Long> addtionalQuantity= new HashMap<String, Long>();
        Long oldOrderFangchaQuantity = 0l;

		//如果是一键下单，设置原订单的成人数、儿童数
		OrdOrder order = null;
		if(oneKeyOrderVO != null && originalOrderId != null){  //一键下单
			ResultHandleT<OrdOrder> resultOrderHandle = orderService.loadOrderWithItemByOrderId(originalOrderId);
			if(resultOrderHandle==null || resultOrderHandle.isFail() || resultOrderHandle.hasNull()){
				model.addAttribute("ERROR", "订单不存在");
				return "order/error";
			}
			order = resultOrderHandle.getReturnContent();
			OrdPersonQueryTO ordPersonQueryTO = new OrdPersonQueryTO();
			ordPersonQueryTO.setOrderId(oneKeyOrderVO.getOriginalOrderId());

			int adultAmount = -1;
			int childAmount = -1;
			OrdFormInfoQueryPO ordFormInfoQueryPO = new OrdFormInfoQueryPO();
			ordFormInfoQueryPO.setOrderId(order.getOrderId());
			ResultHandleT<List<OrdFormInfo>> ordFormHandleT = ordFormInfoClientService.findOrdFormInfoList(ordFormInfoQueryPO);
			if(ordFormHandleT != null && ordFormHandleT.isSuccess()){
				List<OrdFormInfo> ordFormInfoList = ordFormHandleT.getReturnContent();
				for(OrdFormInfo ordFormInfo : ordFormInfoList){
					if(StringUtils.equalsIgnoreCase(ordFormInfo.getContentType(),
							OrderEnum.OrdFormInfoContentTypeEnum.ADULT_AMOUNT.getContentType())){
						if(StringUtils.isNumeric(ordFormInfo.getContent())){
							adultAmount = Integer.parseInt(ordFormInfo.getContent());
						}
					} else if (StringUtils.equalsIgnoreCase(ordFormInfo.getContentType(),
							OrderEnum.OrdFormInfoContentTypeEnum.CHILD_AMOUNT.getContentType())){
						if(StringUtils.isNumeric(ordFormInfo.getContent())){
							childAmount = Integer.parseInt(ordFormInfo.getContent());
						}
					}
				}
			}
			if(adultAmount == -1 || childAmount == -1){ //如果上述方法没有找到成人数、儿童数，从游玩人中计算成人数、儿童数
				OrderRelatedPersonsVO orderRelatedPersonsVO = null;
				try {
					orderRelatedPersonsVO = bookService.loadOrderRelatedPersons(ordPersonQueryTO);
					if(orderRelatedPersonsVO != null){
						if(orderRelatedPersonsVO.isHasNullPeopleTypeTraveller()){
							model.addAttribute("ERROR", "原订单游玩人信息未填写完整，无法计算成人数和儿童数");
							return "order/error";
						}
						adultAmount = orderRelatedPersonsVO.getAdultAmount();
						childAmount = orderRelatedPersonsVO.getChildAmount();
					}
				} catch (Exception e) {
					LOG.error("获取订单的联系人异常：", e);
				}
			}
			if(!noChangeAdu && adultAmount != -1){
				adultNum = new Long(adultAmount);
			}
			if(!noChangeChild && childAmount != -1){
				childNum = new Long(childAmount);
			}

			//获取订单备注信息
			model.put("remark", order.getRemark());
			String faxMemo=null;
			String intentionOrderFlag=null;
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			if(CollectionUtils.isNotEmpty(orderItemList)){
				for (OrdOrderItem ordOrderItem : orderItemList) {
					Map<String,Object> contentMap=ordOrderItem.getContentMap();
					if(contentMap!=null){
						if(faxMemo==null){
							faxMemo=(String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name());
						}
						if(intentionOrderFlag==null){
							intentionOrderFlag=(String) contentMap.get(OrderEnum.ORDER_ROUTE_TYPE.intention_order_flag.name());
						}
					}

					//自备签，婴儿，税金会使用这个变量，并且在成人数字段里面；
                    addtionalQuantity.put(ordOrderItem.getSuppGoodsId().toString(), ordOrderItem.getAdultQuantity());

					//房差,由于后面有产品其他判断保证只有主产品上有房差的时候才会执行房差SELCET代码
			        long spreadQuantity = ordOrderItem.getSpreadQuantity();
			        if (spreadQuantity > 0) {
			            oldOrderFangchaQuantity = spreadQuantity;
			        }
				}
			}

            model.addAttribute("addtionalQuantity", addtionalQuantity);

			if(faxMemo!=null){
				model.put("faxMemo", faxMemo);
			}
			if(intentionOrderFlag!=null){
				model.put("intentionOrderFlag", intentionOrderFlag);
			}
			//获取上下车地点
			try {
				if (order != null){
					Map<String,String> busmap=ordFormInfoClientService.findFrontBusStop(order.getOrderId());
					if(busmap!=null){
						String frontBusStop=busmap.get(BuyInfoAddition.frontBusStop.name());
						if(StringUtils.isNotBlank(frontBusStop)){
							model.put("frontBusStop", frontBusStop);
						}
						String backBusStop=busmap.get(BuyInfoAddition.backBusStop.name());
						if(StringUtils.isNotBlank(backBusStop)){
							model.put("backBusStop", backBusStop);
						}
					}
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			Date visitTime = order.getVisitTime();
			if(DateUtil.compareDateLessOneDayMore(new Date(), visitTime)){  //游玩日期在当前日期之前
				order = null;
			}
		}


		Long thisMaxChildNum = 0L;
		if(isRoute){
			if(adultNum > 9){
				adultNum = 9L;
			}
			thisMaxChildNum = adultNum*2;
			if((thisMaxChildNum + adultNum) > 9){
				thisMaxChildNum = 9 - adultNum;
			}
			if(thisMaxChildNum < 0){
				thisMaxChildNum = 0L;
			}
			if(thisMaxChildNum < childNum){
				childNum = 0L;
			}
		}
		/**
		 * 判断是否第一次进入订单页，如果是，则只显示搜索框，不加载默认条件下的订单详情页
		 */
		if(StringUtil.isEmptyString(fromSearch)){
			model.addAttribute("productId", productId);
			model.addAttribute("specDate", specDate);
			model.put("userId", userId);
			model.put("intentionOrderId", intentionOrderId);
			model.addAttribute("prodProduct", prodProduct);
			// 是否包含酒店套餐
			model.put("isHotel", isHotel);

			//是否按份销售
			model.put("saleCopies", saleCopies);
			try {
				UserUser user=userUserProxyAdapter.getUserUserByUserNo(userId);
				model.put("user", user);
			} catch (Exception e) {
				LOG.error("{}", e);
			}
			adultNum = 2L;
			//酒店套餐份数/成人数选择列表
			StringBuffer adultSelect = new StringBuffer();
			//儿童数选择列表
			StringBuffer childSelect = new StringBuffer();
	        if(productAddtional != null){
	    		PackageTourProductVo lineProductVo = (PackageTourProductVo)model.get("packageTourProductVo");
	    		if(lineProductVo != null){
	    			lineProductVo.setProductAddtional(productAddtional);
	    		}
	        	if((isHotel || saleCopies) && !isRoute){
	        		//酒店套餐份数
	        		Long adultMinQuantity = productAddtional.getAdultMinQuantity();
	        		Long adultMaxQuantity= productAddtional.getAdultMaxQuantity();
	        		if(adultMinQuantity == null){
	        			adultMinQuantity = 1L;
	        		}
	        		if(adultMaxQuantity == null){
	        			adultMaxQuantity = 1L;
	        		}
	        		for(int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++){
	        			if(i==Integer.parseInt(copies.toString()))
	        			{
	        				adultSelect.append("<option  selected='selected' value=\"" + i + "\">" + i + "</option>");
	        			}else
	        			{
	        				adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
	        			}

	        		}
	        	}else{
	        		//成人
	        		Long adultMinQuantity = productAddtional.getAdultMinQuantity();
	        		Long adultMaxQuantity= productAddtional.getAdultMaxQuantity();
	        		if(adultMinQuantity == null){
	        			adultMinQuantity = 1L;
	        		}
	        		if(adultMaxQuantity == null){
	        			adultMaxQuantity = 1L;
	        		}
	        		if(isRoute){
	        			if(adultMaxQuantity > 9){
	        				adultMaxQuantity = 9L;
	        			}
	        		}

					Long minOrderQuantity = prodProduct.getMinOrderQuantity();
					String packageType = prodProduct.getPackageType();
					Long categoryId=prodProduct.getBizCategoryId();
					String productType=prodProduct.getProductType();

					//出境跟团游自主打包产品
					if(minOrderQuantity!=null&&minOrderQuantity.intValue()>1&& Objects.equals(packageType,"LVMAMA")&&categoryId!=null&&categoryId.intValue()==15&&Objects.equals(productType,"FOREIGNLINE")) {
						if(minOrderQuantity.intValue()>adultMaxQuantity.intValue()){
							minOrderQuantity=adultMaxQuantity;
						}

						for (int i = minOrderQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++) {
							if (i == minOrderQuantity.intValue()) {
								adultSelect.append("<option value=\"" + i + "\" selected = \"selected\">" + i + "</option>");
							} else {
								adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
							}
						}
					}else{
							for(int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++){
								if(adultNum!=null && i == adultNum ){
									adultSelect.append("<option value=\""+i+"\" selected = \"selected\">"+i+"</option>");
								}else{
									adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
								}

							}

						}
	        		//儿童
	        		Long childMinQuantity= productAddtional.getChildMinQuantity();
	        		Long childMaxQuantity= productAddtional.getChildMaxQuantity();
	        		if(childMinQuantity == null){
	        			childMinQuantity = 0L;
	        		}
	        		if(childMaxQuantity == null){
	        			childMaxQuantity = 0L;
	        		}
	        		if(isRoute){
	        			childMaxQuantity = thisMaxChildNum;
	        		}
	        		model.put("childMinQuantity", childMinQuantity);
	        		for(int i = childMinQuantity.intValue(); i < childMaxQuantity.intValue() + 1; i++){
	        			if(childNum!=null && i == childNum){
	        				childSelect.append("<option value=\"" + i + "\" selected = \"selected\">" + i + "</option>");
	        			}else{
	        				childSelect.append("<option value=\"" + i + "\">" + i + "</option>");
	        			}
	        		}
	        	}
	        }

			model.put("adultSelect", adultSelect.toString());
			model.put("childSelect", childSelect.toString());
			// 返回单份成人数儿童数
			model.put("baseChildNum", baseChildNum);
			model.put("baseAdultNum", baseAdultNum);
			// 返回总的成人数儿童数
			model.put("childNum", (childNum==null || childNum<0)?0:childNum);
			model.put("adultNum", adultNum);
			loadPriceAllMonthById(model,productId,specDate);
			return "/order/orderProductQuery/findLineProductDetailListForSearch";
		}
		// 根据产品id，加载产品对应的所有商品信息
		if (StringUtils.isNotEmpty(productId)) {
			Map<String, Object> resMap=this.loadProduct(model, productId, specDate, adultNum,
					childNum, isHotel, startDistrictId, order, req,prodProduct);
			result = (String) resMap.get("resultUrl");
			specDate=(Date)resMap.get("specDate");//重新给团期日期的开始时间赋值
		} else {
			model.addAttribute("ERROR", "商品不可售");
			return "order/error";
		}

        //线路下单失败打点统计
        String errorParam01 = getErrTrackInfo(model, prodProduct);
        model.put("lineBackOrderTrackParam01", errorParam01);

		loadPriceAllMonthById(model,productId,specDate);
		// 获取常用联系人
		List<Person> personList = null;
		UserUser user=null;
		try {
			user=userUserProxyAdapter.getUserUserByUserNo(userId);
			personList = orderService.loadUserReceiversByUserId(userId);
		} catch (Exception e) {
			LOG.error("{}", e);
		}
		model.put("user", user);
		model.put("personList", personList);

		//房差份数列表
		StringBuffer sb = new StringBuffer();
		String selectRangeStr = (String)model.get("selectRange");
		if(selectRangeStr == null){
			selectRangeStr = "";
		}
		String[] selectRangeArr = selectRangeStr.split(",");
		for(String str : selectRangeArr){
			Pattern pattern = Pattern.compile("^\\d+");
			Matcher isNum = pattern.matcher(str);
			if(isNum.matches()){
			    if(oldOrderFangchaQuantity>0&&oldOrderFangchaQuantity.longValue() == Long.parseLong(str)){
	                 sb.append("<option selected=\"selected\" value=\"" + str + "\">" + str + "</option>");
			    }else{
			        sb.append("<option value=\"" + str + "\">" + str + "</option>");
			    }
			}
		}
		model.addAttribute("fangchaQuantity", sb.toString());


		//酒店套餐份数/成人数选择列表
		StringBuffer adultSelect = new StringBuffer();
		//儿童数选择列表
		StringBuffer childSelect = new StringBuffer();
        if(productAddtional != null){
    		PackageTourProductVo lineProductVo = (PackageTourProductVo)model.get("packageTourProductVo");
    		if(lineProductVo != null){
    			lineProductVo.setProductAddtional(productAddtional);
    		}
        	if((isHotel || saleCopies) && !isRoute){
        		//酒店套餐份数
        		Long adultMinQuantity = productAddtional.getAdultMinQuantity();
        		Long adultMaxQuantity= productAddtional.getAdultMaxQuantity();
        		if(adultMinQuantity == null){
        			adultMinQuantity = 1L;
        		}
        		if(adultMaxQuantity == null){
        			adultMaxQuantity = 1L;
        		}
        		for(int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++){
        			if(i==Integer.parseInt(copies.toString()))
        			{
        				adultSelect.append("<option  selected='selected' value=\"" + i + "\">" + i + "</option>");
        			}else
        			{
        				adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
        			}

        		}
        	}else{
        		//成人
        		Long adultMinQuantity = productAddtional.getAdultMinQuantity();
        		Long adultMaxQuantity= productAddtional.getAdultMaxQuantity();
        		if(adultMinQuantity == null){
        			adultMinQuantity = 1L;
        		}
        		if(adultMaxQuantity == null){
        			adultMaxQuantity = 1L;
        		}
        		if(isRoute){
        			if(adultMaxQuantity > 9){
        				adultMaxQuantity = 9L;
        			}
        		}
        		//出境跟团游自主打包
        		Long minOrderQuantity2=prodProduct.getMinOrderQuantity();
				String packageType2 = prodProduct.getPackageType();
				Long categoryId2=prodProduct.getBizCategoryId();
				String productType2=prodProduct.getProductType();
				if(minOrderQuantity2!=null&&minOrderQuantity2.intValue()>1&& Objects.equals(packageType2,"LVMAMA")&&categoryId2!=null&&categoryId2.intValue()==15&&Objects.equals(productType2,"FOREIGNLINE")) {
					if(minOrderQuantity2.intValue()>adultMaxQuantity.intValue()){
						minOrderQuantity2=adultMaxQuantity;
					}
					for(int i = minOrderQuantity2.intValue(); i < adultMaxQuantity.intValue() + 1; i++){
						if(adultNum!=null && i == adultNum ){
							adultSelect.append("<option value=\""+i+"\" selected = \"selected\">"+i+"</option>");
						}else{
							adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
						}

					}
				}else{
					for(int i = adultMinQuantity.intValue(); i < adultMaxQuantity.intValue() + 1; i++){
						if(adultNum!=null && i == adultNum ){
							adultSelect.append("<option value=\""+i+"\" selected = \"selected\">"+i+"</option>");
						}else{
							adultSelect.append("<option value=\"" + i + "\">" + i + "</option>");
						}

					}
				}




        		//儿童
        		Long childMinQuantity= productAddtional.getChildMinQuantity();
        		Long childMaxQuantity= productAddtional.getChildMaxQuantity();
        		if(childMinQuantity == null){
        			childMinQuantity = 0L;
        		}
        		if(childMaxQuantity == null){
        			childMaxQuantity = 0L;
        		}
        		if(isRoute){
        			childMaxQuantity = thisMaxChildNum;
        		}
        		model.put("childMinQuantity", childMinQuantity);
        		for(int i = childMinQuantity.intValue(); i < childMaxQuantity.intValue() + 1; i++){
        			if(childNum!=null && i == childNum){
        				childSelect.append("<option value=\"" + i + "\" selected = \"selected\">" + i + "</option>");
        			}else{
        				childSelect.append("<option value=\"" + i + "\">" + i + "</option>");
        			}
        		}
        	}
        }

		model.put("adultSelect", adultSelect.toString());
		model.put("childSelect", childSelect.toString());

		// 是否包含酒店套餐
		model.put("isHotel", isHotel);

		//是否按份销售
		model.put("saleCopies", saleCopies);
		//是否是目的地产品
		model.put("destinationBU", isDestinationBU);
		// 酒店套餐返回份数
		if (isHotel) {
			model.put("hotelNum", copies);
		}
		// 返回单份成人数儿童数
		model.put("baseChildNum", baseChildNum);
		model.put("baseAdultNum", baseAdultNum);
		// 返回总的成人数儿童数
		model.put("childNum", (childNum==null || childNum<0)?0:childNum);
		model.put("adultNum", adultNum);

		if (order != null) {
			model.put("oughtAmount", order.getOughtAmount());
			model.put("isTestOrder", order.getIsTestOrder());
			model.put("oldDistributorId",order.getDistributorId());
			model.put("oldDistributorCode",order.getDistributorCode());
			model.put("oldDistributionChannel",order.getDistributionChannel());
		}

        model.addAttribute("prodProduct", prodProduct);
		model.addAttribute("productId", productId);
		model.addAttribute("specDate", specDate);
		model.put("userId", userId);
		model.put("intentionOrderId", intentionOrderId);
		model.put("canUseCoupons", PropertiesUtil.getValue("canUseCoupons").trim());

		childNum = (Long) model.get("childNum");
		model.put("youhuiQuantity",copies);
		Long personNums= childNum+adultNum;
		model.put("youhuiperson", personNums);
		//******获取签证材料解释收取时间  开始
		Map<String, Object> pvddParam=new HashMap<String, Object>();
		pvddParam.put("productId",productId);//
		pvddParam.put("specDate", specDate);
		List<ProdVisaDocDate> pvddList=prodVisaDocDateClientService.findProdVisaDocDate(pvddParam,false).getReturnContent();//根据产品Id和游玩日期获取签证材料截止收取时间,根据这两个条件可以取到一条数据
		LOG.info("-----pvddList---"+pvddList);
		if(CollectionUtils.isNotEmpty(pvddList)){
			ProdVisaDocDate prodVisaDocDate= pvddList.get(0);
			LOG.info("prodVisaDocDate.getLastDate()"+prodVisaDocDate.getLastDate());
			model.addAttribute("visaDocLastTime", prodVisaDocDate.getLastDate());
		}
		//******获取签证材料解释收取时间 结束
		LOG.info("---------------加载完成------------------"+saleCopies+"");
		return result;
	}

    /**
     * 获取下单失败打点统计错误信息
     * @param prodProduct
     * @return BU_品类_预订失败页
     */
    private String getErrTrackInfo(ModelMap model, ProdProduct prodProduct) {
        StringBuffer errorParam01 = new StringBuffer();

        try {
            //获取BU
            String bu = null;
            if ("LVMAMA".equals(prodProduct.getPackageType())) {
                bu = prodProduct.getBu();
            } else {
                //供应商打包，BU从商品上获取
                PackageTourProductVo lineProductVo = (PackageTourProductVo)model.get("packageTourProductVo");
                if(lineProductVo!=null) {
                    List<ProdProductBranch> branchs = lineProductVo.getProdProductBranchList();
                    if(CollectionUtils.isNotEmpty(branchs)) {
                        List<SuppGoods> goods = branchs.get(0).getSuppGoodsList();
                        if (CollectionUtils.isNotEmpty(goods)) {
                            bu = goods.get(0).getBu();
                        }
                    }
                }
            }

            if (bu != null) {
                errorParam01.append(Constant.BU_NAME.getCnName(bu));
            } else {
                errorParam01.append("未知BU");
            }
            errorParam01.append("_");

            //获取分类
            Long categoryId = null;
            if (prodProduct != null && prodProduct.getBizCategoryId() != null) {
                categoryId = prodProduct.getBizCategoryId();
            }
            if (categoryId == null && prodProduct.getBizCategory() != null) {
                categoryId = prodProduct.getBizCategory().getCategoryId();
            }
            if (categoryId == null && prodProduct.getCategoryId() != null) {
                categoryId = prodProduct.getCategoryId();
            }
            if (categoryId != null) {
                errorParam01.append(BIZ_CATEGORY_TYPE.getCnName(categoryId));
            } else {
                errorParam01.append("未知分类");
            }

            errorParam01.append("_后台保存订单");
        } catch (Exception e) {
            LOG.error("线路后台打点信息获取异常");
            LOG.error(ExceptionFormatUtil.getTrace(e));
        }

        return errorParam01.toString();
    }

    @RequestMapping("/ord/order/lineBackPriceInfo.do")
	@ResponseBody
	public Object queryPriceInfo(BuyInfo buyInfo, HttpServletRequest req) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			createBuyInfo(buyInfo, req);
			initBooker(buyInfo);
			PriceInfo pi = orderService.countPrice(buyInfo);
			String choosePromotion = buyInfo.getChoosePromotion();
			if(pi.isSuccess()){
				Map<String, Object> attributes = new HashMap<String, Object>();
				List<PromPromotion> promList = pi.getPromotionList();
				attributes.put("promList", promList);
				msg.setAttributes(attributes);
				msg.addObject("priceInfo", pi);
				msg.addObject("couponExclusion",pi.getCouponExclusion());
				msg.addObject("choosePromotion",choosePromotion);
				attributes.put("buyPersentActivity", pi.getBuyPresentActivityInfo());
			}else{
				msg.raise(pi.getMsg());
			}
		}catch (Exception e) {
			msg.raise("价格计算发生异常.");
		}
		return msg;
	}


	/**
	 * 线路打包的快递查询
	 * @param form
	 * @param req
	 * @return
	 */
	@RequestMapping("/ord/order/findOrderExpressGoods.do")
	@ResponseBody
	public Object findOrderExpressGoods(BuyInfo form,HttpServletRequest req){
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	createBuyInfo(form, req);
				 ResultHandleT<List<ExpressSuppGoodsVO>> resultHandle=orderService.findOrderExpressGoods(form);
			    if(!resultHandle.isSuccess()){
			    	msg.setCode(resultHandle.getMsg());
			    }else{
			    	msg.addObject("expressSuppGoodsVOList", resultHandle.getReturnContent());
			    	msg.addObject("visitTime", DateUtil.formatDate(DateUtil.dsDay_Date(new Date(), 1), "yyyy-MM-dd"));
			    }
		} catch (Exception e) {
			LOG.error("{}", e);
			msg.setCode("查找快递发生异常");
		}
		 return msg;
	}

	@RequestMapping("/ord/order/lineBackCheckStore.do")
	@ResponseBody
	public Object lineBackCheckStore(BuyInfo form, HttpServletRequest req) {

		ResultMessage msg = ResultMessage.createResultMessage();
		try{
			createBuyInfo(form, req);
			//库存检查时，所有的产品认为都是非后置产品，否则对接机票库存检查时会抛异常(因为含对接机票的订单不允许游玩人后置)
			form.setTravellerDelayFlag(Constants.N_FLAG);
			ResultHandle handle = orderService.checkStock(form);
			msg.raise(handle);
		}catch (Exception e) {
			msg.raise("线路库存检查发生异常.");
		}
		return msg;
	}

	@RequestMapping("/ord/order/changeStockSelectOption.do")
	@ResponseBody
	public Object changeStockSelectOption(String suppGoodsId,String specDate,String ticketMax){
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
            Long stockNum = 0L;
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("suppGoodsId", suppGoodsId);
            parameters.put("specDate", DateUtil.getDateByStr(specDate, "yyyy-MM-dd"));

            ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(Long.valueOf(suppGoodsId));
            SuppGoods suppGoods = suppGoodsResultHandleT.getReturnContent();
            String oversellFlag = "";
            if (Constants.Y_FLAG.equalsIgnoreCase(suppGoods.getAperiodicFlag())) {
                //期票
                ResultHandleT<List<SuppGoodsNotimeTimePrice>> resultHandleT = suppGoodsTimePriceClientService.findSuppGoodsNotimeTimePriceList(parameters);
                if(resultHandleT!=null && resultHandleT.isSuccess()){
                    List<SuppGoodsNotimeTimePrice> noTimePriceList = resultHandleT.getReturnContent();
                    SuppGoodsNotimeTimePrice noTimePrice = noTimePriceList.get(0);
                    String stockFlag = noTimePrice.getStockFlag();
                    oversellFlag = noTimePrice.getOversellFlag();
                    if(!Constants.Y_FLAG.equalsIgnoreCase(oversellFlag)){
                    	stockNum = cheatStock(suppGoods, stockFlag, noTimePrice.getStock());
                    }
                }
            } else { //非期票
                ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT = suppGoodsTimePriceClientService.findSuppGoodsAddTimePriceList(parameters);
                if(resultHandleT!=null && resultHandleT.isSuccess()){
                    List<SuppGoodsAddTimePrice> addTimePriceList = resultHandleT.getReturnContent();
                    SuppGoodsAddTimePrice addTimePrice = addTimePriceList.get(0);
                    String stockFlag = addTimePrice.getStockFlag();
                    oversellFlag = addTimePrice.getOversellFlag();
                    if(!Constants.Y_FLAG.equalsIgnoreCase(oversellFlag)){
                    	stockNum = cheatStock(suppGoods, stockFlag, addTimePrice.getStock());
                    }
                }
            }

            //设置前端可选张数
            Long maxQuantity = suppGoods.getMaxQuantity();//最大起订量
            Long minQuantity = suppGoods.getMinQuantity();//最少起订量
			Map<String, Object> attributes = new HashMap<String, Object>();
            List<String> selectNumList = new ArrayList<String>();
            Long ticketMaxLimit = 0L;
            //如果不可超卖
            if(!Constants.Y_FLAG.equalsIgnoreCase(oversellFlag)){
            	ticketMaxLimit = stockNum;
            	if(ticketMaxLimit > maxQuantity){//取最小值
            		ticketMaxLimit = maxQuantity;
            	}
            }else{
            	ticketMaxLimit = maxQuantity;
            }
            Long ticketMaxL = 0L;
            if(!StringUtil.isEmptyString(ticketMax)){
            	ticketMaxL = Long.valueOf(ticketMax);
            }
            if(ticketMaxLimit > ticketMaxL && ticketMaxL > 0){//取最小值
            	ticketMaxLimit = ticketMaxL;
            }
            for(int i=minQuantity.intValue();i <= ticketMaxLimit.intValue();i++){
            	selectNumList.add(i + "");
            }

            attributes.put("selectNumList", selectNumList);
            msg.setAttributes(attributes);
		} catch (Exception e) {
			// TODO: handle exception
 			msg.raise("库存获取发生异常. ID：【"+suppGoodsId+"】");
 			LOG.error("库存获取发生异常. ID：【"+suppGoodsId+"】。异常信息为："+e);
		}
		return msg;
	}

    /**
     * 库存欺骗,此方法与DestBuRouteBookAction中的方法完全一致，由于之前是分离的，没合并，人工保持一致
     * DestBuRouteBookAction#cheatStock(com.lvmama.vst.back.goods.po.SuppGoods, java.lang.String, java.lang.Long)
     * @param suppGoods 商品
     * @param stockFlag
     * @param stock
     * @see DestBuRouteBookAction#cheatStock(com.lvmama.vst.back.goods.po.SuppGoods, java.lang.String, java.lang.Long)
     * @return
     */
    private Long cheatStock(SuppGoods suppGoods, String stockFlag, Long stock) {
        Long stockNum;
        if (suppGoods.getSpecialTicketType() != null &&
                suppGoods.getSpecialTicketType().equals(SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_TICKET.getCode())) {
            //不校验库存或者库存大于5，前端最多取五张
            if ("N".equals(stockFlag) || stock > 5) {
                stockNum = 5L;
            } else {
                stockNum = stock;
            }
        } else {//普通门票
            //不校验库存或者库存大于50，前端最多取五十张
            if ("N".equals(stockFlag) || stock > 50) {
                stockNum = 50L;
            } else {
                stockNum = stock;
            }
        }
        return stockNum;
    }

    /**
	 * 后台下单，创建订单
	 */
	@RequestMapping(value = "/ord/order/beforeCreateOrder.do")
	public String beforeCreateOrder(Model model, BuyInfo buyInfo,
			HttpServletResponse response, HttpServletRequest req, @RequestParam(required=false)String travellerDelayFlag) {
		boolean isTravellerDelay = StringUtils.isNotBlank(travellerDelayFlag)
				&& "Y".equals(travellerDelayFlag);
		model.addAttribute("isTravellerDelay", isTravellerDelay);
		if(req.getParameter("hasApiFlight")!=null&&""!=req.getParameter("hasApiFlight")){
			model.addAttribute("hasApiFlight", req.getParameter("hasApiFlight"));
		}
		try {
			/******************************/
			Long startTime = null;
			String methodName = "OrderLineProductQueryAction#createOrder【"+ buyInfo.getProductId() +"】";
			// 组装BuyInfo
			startTime = System.currentTimeMillis();
			//放入一键下单标志位
			String originalOrderId = req.getParameter("originalOrderId");
			if(StringUtils.isNotBlank(originalOrderId)){
				buyInfo.setOrderCreatingManner(OrderEnum.ORDER_CREATING_MANNER.backOneKeyRecreating.getCode());
			}
			createBuyInfo(buyInfo, req);
			if(StringUtils.isNotBlank(originalOrderId)){
				//一键重下订单重新设置渠道
				Long oldDistributorId= NumberUtils.toLong(req.getParameter("oldDistributorId"));
				Long oldDistributionChannel=NumberUtils.toLong(req.getParameter("oldDistributionChannel"));
				String oldDistributorCode=req.getParameter("oldDistributorCode");
				if(oldDistributorId.longValue()!=0L){
					buyInfo.setDistributionId(oldDistributorId);
					buyInfo.setDistributorCode(oldDistributorCode);
					if(oldDistributionChannel.longValue()!=0L){
						buyInfo.setDistributionChannel(oldDistributionChannel);
					}
				}
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "组装BuyInfo",
					"this.createBuyInfo", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			initBooker(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "初始化 buyInfo信息",
					"this.initBooker", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			buyInfo.setIp(InternetProtocol.getRemoteAddr(req));
			int personNum = buyInfo.getAdultQuantity()
					+ buyInfo.getChildQuantity();
			List<OrdPerson> tavellerList = new ArrayList<OrdPerson>(personNum);
			for (int i = 0; i < personNum; i++) {
				OrdPerson person = new OrdPerson();
				if(i< buyInfo.getAdultQuantity()) {
					person.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.getCode());
				} else {
					person.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.getCode());
				}
				tavellerList.add(person);
			}
			int insuranceNum = 0;// 保险份数
			List<InsuranceSuppGoodsVo> bxGoodsList = new ArrayList<InsuranceSuppGoodsVo>();
			List<Person> personList = null;
			OrderRequiredVO orderRequiredvO = null;
			List<InsuranceSuppGoodsVo> LocalRouteSuppGoodsList = new ArrayList<InsuranceSuppGoodsVo>();//关联销售当地游
			try {
				List<Long> productIdList = new ArrayList<Long>();
				List<Long> suppGoodsIdList = new ArrayList<Long>();
				List<Item> itemList = new ArrayList<Item>();

				List<Item> items = buyInfo.getItemList();
				if (CollectionUtils.isNotEmpty(items)) {
					for (Item item : items) {
						itemList.add(item);
					}
				}
				List<Product> productList = buyInfo.getProductList();
				if (null != productList && !productList.isEmpty()) {
					for (Product product : productList) {
						productIdList.add(product.getProductId());
						List<Item> proItems = product.getItemList();
						for (Item item : proItems) {
							itemList.add(item);
						}
					}
				}
				if(null!=itemList&&itemList.size()>0){
					startTime = System.currentTimeMillis();
					for (Item orderitem : itemList) {
						ResultHandleT<SuppGoods> resultHandleT = suppGoodsClientService.findSuppGoodsById(orderitem.getGoodsId(), Boolean.TRUE, Boolean.TRUE);
						SuppGoods suppGoods=resultHandleT.getReturnContent();
						if(resultHandleT.isSuccess()&&resultHandleT.getReturnContent()!=null){
							String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
							if(ProductCategoryUtil.isInsurance(categoryCode)){
								InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, insuranceVo);
								insuranceVo.setQuantity(orderitem.getQuantity());
								insuranceNum+=orderitem.getQuantity();
								bxGoodsList.add(insuranceVo);
//							}else if(orderitem.hasContentValue(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), "自备签")){
							}else if(suppGoods.getProdProductBranch().getBranchName().equals("自备签")){
								InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, insuranceVo);
								insuranceVo.setQuantity(orderitem.getQuantity());
								bxGoodsList.add(insuranceVo);
							}else if("localRoute".equals(orderitem.getGoodType())){//当地游
								InsuranceSuppGoodsVo LoocalRouteVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, LoocalRouteVo);
								LoocalRouteVo.setQuantity(orderitem.getQuantity());
								LocalRouteSuppGoodsList.add(LoocalRouteVo);
							}
							productIdList.add(suppGoods.getProductId());
							suppGoodsIdList.add(suppGoods.getSuppGoodsId());
						}
					}
					LOG.info(ComLogUtil.printTraceInfo(methodName, "获取的SuppGoods, 已装载供应商,合同,产品,商品信息====>【此处For循环】" + itemList.size() + "次",
							"suppGoodsClientService.findSuppGoodsById", (System.currentTimeMillis()-startTime)));
				}

				startTime = System.currentTimeMillis();
				orderRequiredvO = queryItemInfo2(buyInfo,productIdList,suppGoodsIdList);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "获取订单必填项信息",
						"this.queryItemInfo", (System.currentTimeMillis()-startTime))); //：

				// 获取常用联系人
				startTime = System.currentTimeMillis();
				personList = orderService.loadUserReceiversByUserId(buyInfo.getUserId());
				LOG.info(ComLogUtil.printTraceInfo(methodName, "查找相关联系人 ",
						"orderService.loadUserReceiversByUserId", (System.currentTimeMillis()-startTime)));

			} catch (Exception e) {
				LOG.error("{}", e);
			}

			//后台下单，当门票产品下单必须项为 全部游玩人时，重置personNum

			String ticketPersonNumStr = req.getParameter("ticketPersonNum");
			if(StringUtil.isNotEmptyString(ticketPersonNumStr)){
				int ticketPersonNum = Integer.parseInt(ticketPersonNumStr);
				if(ticketPersonNum >0){
					personNum = ticketPersonNum;
				}
			}

			model.addAttribute("LocalRouteSuppGoodsList", LocalRouteSuppGoodsList);
			model.addAttribute("order", new OrdOrder());
			model.addAttribute("bxGoodsList", bxGoodsList);
			model.addAttribute("personNum", personNum);
			model.addAttribute("insuranceNum", insuranceNum);
			model.addAttribute("personList", personList);
			model.addAttribute("orderRequiredvO", orderRequiredvO);
			model.addAttribute("contactPerson", new OrdPerson());
			model.addAttribute("emergencyPerson", new OrdPerson());
			model.addAttribute("tavellerList", tavellerList);
			model.addAttribute("adult",buyInfo.getAdultQuantity());
			model.addAttribute("child", buyInfo.getChildQuantity());

			model.addAttribute("bonusAmountHidden", buyInfo.getBonusAmountHidden());
			model.addAttribute("hasContractOrder",true);
			//意向单号
			String intentionId =req.getParameter("intentionOrderId");
			//***********意向单订单*************
			if(StringUtils.isNotEmpty(intentionId)){
				startTime = System.currentTimeMillis();
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("intentionId", intentionId);
				List<IntentionOrder> intenetions = orderIntentionService.queryIntentionsByCriteria(param);
				OrdPerson contactPerson= new OrdPerson();
				//意向单联系人Id Number
				contactPerson.setOrdPersonId(buyInfo.getUserNo());
				contactPerson.setFullName(intenetions.get(0).getContactsName());
				contactPerson.setMobile(intenetions.get(0).getTel());
				contactPerson.setEmail(intenetions.get(0).getEmail());
				model.addAttribute("contactPerson", contactPerson);
			}
			ProdProductParam param = new ProdProductParam();
	        ResultHandleT<ProdProduct> productHandleT = prodProductClientRemote.findLineProductByProductId(buyInfo.getProductId(), param);
	        if(productHandleT!=null){
		        ProdProduct product = productHandleT.getReturnContent();
				model.addAttribute("subCategoryId",product.getSubCategoryId());
				model.addAttribute("productType",product.getProductType());
	        }

			//***********意向单订单*************
			if (TestOrderUtil.isTravellerToFirst()) {
				return "/order/orderProductQuery/newOrderFormInfo";
			}else {
				return "/order/orderProductQuery/orderFormInfo";
			}
		} catch (Exception e) {
			LOG.error("{}", e);
			model.addAttribute("ERROR", "下单错误，请检查订单项价格，数量是否正确");
			return ERROR_PAGE;
		}
	}

	private OrderRequiredVO queryItemInfo2(BuyInfo buyInfo, List<Long> productIdList , List<Long> suppGoodsIdList){

        boolean isTicketBu = ProductCategoryUtil.isTicket(buyInfo.getCategoryId());
        CommEnumSet.BU_NAME buName = isTicketBu ? CommEnumSet.BU_NAME.TICKET_BU : null;
        ResultHandleT<OrderRequiredVO> orderRequiredVO = null;
        if(buyInfo.getCategoryId().equals(41L)
        		||buyInfo.getCategoryId().equals(43L)
        		||buyInfo.getCategoryId().equals(44L)
        		||buyInfo.getCategoryId().equals(45L)  ){

        	if (suppGoodsIdList!=null) {
				orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(null, suppGoodsIdList);
			}

        }
        else{
        	orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(productIdList, suppGoodsIdList, buName);
        }
        OrderRequiredVO vo = orderRequiredVO.getReturnContent();
        if (vo != null) {
            //门票取消证件类型'客服联系我'的选项
            if (isTicketBu) {
                vo.setProductType("ticket");
            }
            //wifi下单必填项设置
            if (BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(buyInfo.getCategoryId())) {
                vo.setTravNumType("TRAV_NUM_ONE");
                vo.setPhoneType("TRAV_NUM_ONE");
            }
        }
		return vo;
	}



	/**
	 * 后台下单，创建订单
	 */
	@RequestMapping(value = "/ord/order/lineBackCreateOrderResponseBody.do")
	@ResponseBody
	public Object createOrderResponseBody(Model model, BuyInfo buyInfo,
			HttpServletResponse response, HttpServletRequest req, @RequestParam(required=false)String travellerDelayFlag) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			/******************************/
			Long startTime = null;
			String methodName = "OrderLineProductQueryAction#createOrder【"+ buyInfo.getProductId() +"】";
			// 组装BuyInfo
			startTime = System.currentTimeMillis();
			//放入一键下单标志位
			String originalOrderId = req.getParameter("originalOrderId");
			if(StringUtils.isNotBlank(originalOrderId)){
				buyInfo.setOrderCreatingManner(OrderEnum.ORDER_CREATING_MANNER.backOneKeyRecreating.getCode());
			}
			createBuyInfo(buyInfo, req);

			//转换新的promotionMap
			changeNewPromotionNewMap(buyInfo);

			if(StringUtils.isNotBlank(originalOrderId)){
				//一键重下订单重新设置渠道
				Long oldDistributorId= NumberUtils.toLong(req.getParameter("oldDistributorId"));
				Long oldDistributionChannel=NumberUtils.toLong(req.getParameter("oldDistributionChannel"));
				String oldDistributorCode=req.getParameter("oldDistributorCode");
				if(oldDistributorId.longValue()!=0L){
					buyInfo.setDistributionId(oldDistributorId);
					buyInfo.setDistributorCode(oldDistributorCode);
					if(oldDistributionChannel.longValue()!=0L){
						buyInfo.setDistributionChannel(oldDistributionChannel);
					}
				}
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "组装BuyInfo",
					"this.createBuyInfo", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			initBooker(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "初始化 buyInfo信息",
					"this.initBooker", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			buyInfo.setIp(InternetProtocol.getRemoteAddr(req));
			ResultHandleT<OrdOrder> orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
			LOG.info(ComLogUtil.printTraceInfo(methodName, "订单生成接口",
					"orderService.createOrder", (System.currentTimeMillis()-startTime)));


			if (orderHandle.isFail()) {
				msg.raise(orderHandle.getMsg());
				return msg;
			}
			OrdOrder ordOrder = orderHandle.getReturnContent();
			msg.addObject("orderId", ordOrder.getOrderId());
			msg.addObject("userId", ordOrder.getUserId());


			//意向单号
			String intentionId =req.getParameter("intentionOrderId");

			startTime = System.currentTimeMillis();
			orderTravelContractDataServiceFactory.createTravelContractDataService(ordOrder);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "生成合同接口bean",
					"orderTravelContractDataServiceFactory.createTravelContractDataService",
					(System.currentTimeMillis()-startTime)));

			//***********意向单订单*************
			if(StringUtils.isNotEmpty(intentionId)){
				startTime = System.currentTimeMillis();
				//把订单号更新到意向单表中
				IntentionOrder intentionOrder = new IntentionOrder();
				intentionOrder.setOrderId(ordOrder.getOrderId());
				intentionOrder.setIntentionOrderId(Long.valueOf(intentionId));
				//更新已下单
				intentionOrder.setState("1");
				orderIntentionService.updateIntention(intentionOrder);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "意向单获取与修改 ",
						"orderIntentionService.queryIntentionsByCriteria/orderIntentionService.updateIntention", (System.currentTimeMillis()-startTime)));
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "整个创建订单耗费时间",
					"createOrder", (System.currentTimeMillis()-startTime)));
		} catch (Exception e) {
			LOG.error("{}", e);
			msg.raise("下单错误，请检查订单项价格，数量是否正确");
		}
		return msg;

	}


	//后台下单转换新promotionMap
	private void changeNewPromotionNewMap(BuyInfo buyInfo) {
		//判断是否优惠互斥
		if(buyInfo.getCouponExclusion()!=null){
			//新版促销
			if(buyInfo.getCouponExclusion()){
				//排斥
				if("promotion".equalsIgnoreCase(buyInfo.getChoosePromotion())){
					//选择了促销
					if(buyInfo.getUserCouponVoList()!=null){
						buyInfo.getUserCouponVoList().clear();
					}
				}else{
					//选择了账户优惠
					if(buyInfo.getPromotionIdList()!=null){
						buyInfo.getPromotionIdList().clear();
					}
					if(buyInfo.getPromotionMap()!=null){
						buyInfo.getPromotionMap().clear();
					}
				}
			}

			//转换新版map
			if(buyInfo.getPromotionNewMap()==null || buyInfo.getPromotionNewMap().size()==0){
				Map<Long, Set<Long>> newPromotionMap = new HashMap<>();
				Map<String, List<Long>> oldMap = buyInfo.getPromotionMap();
				if(oldMap!=null && oldMap.size()>0){
					Iterator<Map.Entry<String,List<Long>>> iter = oldMap.entrySet().iterator();
					while(iter.hasNext()){
						Map.Entry<String,List<Long>> entry = iter.next();
						String key = entry.getKey();
						List<Long> ids = entry.getValue();
						String keys[]=key.split("_");
						Long promotionProductId = buyInfo.getProductId();
						try {
							if(keys!=null && keys.length>=6){
								promotionProductId = Long.parseLong(keys[5]);
							}
						}catch (Exception e){
							LOG.error("parse PromotionNewKey error:"+ ExceptionUtil.getExceptionDetails(e));
						}
						Set<Long> promotionIds = newPromotionMap.get(promotionProductId);
						if(promotionIds==null){
							promotionIds = new LinkedHashSet<>();
							newPromotionMap.put(promotionProductId,promotionIds);
						}
						promotionIds.addAll(ids);
					}
				}
				buyInfo.setPromotionNewMap(newPromotionMap);
				buyInfo.setPromotionMap(null);
			}
		}
	}


	/**
	 * 后台下单，创建订单
	 */
	@RequestMapping(value = "/ord/order/lineBackCreateOrder.do")
	public String createOrder(Model model, BuyInfo buyInfo,
			HttpServletResponse response, HttpServletRequest req, @RequestParam(required=false)String travellerDelayFlag) {

		boolean isTravellerDelay = StringUtils.isNotBlank(travellerDelayFlag)
				&& "Y".equals(travellerDelayFlag);
		model.addAttribute("isTravellerDelay", isTravellerDelay);
		if(req.getParameter("hasApiFlight")!=null&&""!=req.getParameter("hasApiFlight")){
			model.addAttribute("hasApiFlight", req.getParameter("hasApiFlight"));
		}
		try {
			/******************************/
			Long startTime = null;
			String methodName = "OrderLineProductQueryAction#createOrder【"+ buyInfo.getProductId() +"】";
			// 组装BuyInfo
			startTime = System.currentTimeMillis();
			//放入一键下单标志位
			String originalOrderId = req.getParameter("originalOrderId");
			if(StringUtils.isNotBlank(originalOrderId)){
				buyInfo.setOrderCreatingManner(OrderEnum.ORDER_CREATING_MANNER.backOneKeyRecreating.getCode());
			}
			createBuyInfo(buyInfo, req);
			if(StringUtils.isNotBlank(originalOrderId)){
				//一键重下订单重新设置渠道
				Long oldDistributorId= NumberUtils.toLong(req.getParameter("oldDistributorId"));
				Long oldDistributionChannel=NumberUtils.toLong(req.getParameter("oldDistributionChannel"));
				String oldDistributorCode=req.getParameter("oldDistributorCode");
				if(oldDistributorId.longValue()!=0L){
					buyInfo.setDistributionId(oldDistributorId);
					buyInfo.setDistributorCode(oldDistributorCode);
					if(oldDistributionChannel.longValue()!=0L){
						buyInfo.setDistributionChannel(oldDistributionChannel);
					}
				}
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "组装BuyInfo",
					"this.createBuyInfo", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			initBooker(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "初始化 buyInfo信息",
					"this.initBooker", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			buyInfo.setIp(InternetProtocol.getRemoteAddr(req));
			ResultHandleT<OrdOrder> orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
			LOG.info(ComLogUtil.printTraceInfo(methodName, "订单生成接口",
					"orderService.createOrder", (System.currentTimeMillis()-startTime)));


			if (orderHandle.isFail()) {
				model.addAttribute("ERROR", orderHandle.getMsg());
				return ERROR_PAGE;
			}
			OrdOrder order =orderHandle.getReturnContent(); //orderService.queryOrdorderByOrderId(orderHandle.getReturnContent().getOrderId());
			model.addAttribute("orderId", orderHandle.getReturnContent().getOrderId());
			// 以下是返回界面的信息
			OrdOrder ordOrder = orderHandle.getReturnContent();
			String ordResult = req.getParameter("ordResult");
			String productName = req.getParameter("productName");
			String specDate = req.getParameter("visitTime");
			//意向单号
			String intentionId =req.getParameter("intentionOrderId");
			model.addAttribute("ordResult", ordResult);
			model.addAttribute("orderId", ordOrder.getOrderId());
			model.addAttribute("productName", productName);
			Date date = CalendarUtils.getDateFormatDate(specDate, "yyyy-MM-dd");
			model.addAttribute("specDate",
					CalendarUtils.getDateFormatString(date, "yyyy年MM月dd日"));
			model.addAttribute("contract", buyInfo.getContact());
			model.addAttribute("ordId", ordOrder.getOrderId());

			startTime = System.currentTimeMillis();
			IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory
					.createTravelContractDataService(ordOrder);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "生成合同接口bean",
					"orderTravelContractDataServiceFactory.createTravelContractDataService",
					(System.currentTimeMillis()-startTime)));

			if (orderTravelContractDataService != null) {
				OrdOrderPack ordOrderPack = ordOrder.getOrderPackList().get(0);
				ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = orderTravelContractDataService
						.getCombCuriseProducatData(
								ordOrderPack.getCategoryId(),
								ordOrderPack.getProductId());
				LOG.info(ComLogUtil.printTraceInfo(methodName, "获取合同数据",
						"orderTravelContractDataService.getCombCuriseProducatData", (System.currentTimeMillis()-startTime)));
				CuriseProductVO curiseProductVO = resultHandleCuriseProductVO
						.getReturnContent();
				Map<String, Object> productPropMap = curiseProductVO
						.getProductPropMap();
				// 退款说明
				String cancelStr = productPropMap
						.get("change_and_cancellation_instructions") == null ? "无退款说明"
						: productPropMap.get(
								"change_and_cancellation_instructions")
								.toString();
				if (StringUtils.isNotEmpty(cancelStr)) {
					cancelStr = cancelStr.replace("\r\n", "<br>");
				}
				model.addAttribute("cancelStr", cancelStr);
			}
			int personNum = buyInfo.getAdultQuantity()
					+ buyInfo.getChildQuantity();
			List<OrdPerson> tavellerList = new ArrayList<OrdPerson>(personNum);
			for (int i = 0; i < personNum; i++) {
				OrdPerson person = new OrdPerson();
				if(i< buyInfo.getAdultQuantity()) {
					person.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.getCode());
				} else {
					person.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.getCode());
				}
				tavellerList.add(person);
			}
			int insuranceNum = 0;// 保险份数
			List<InsuranceSuppGoodsVo> bxGoodsList = new ArrayList<InsuranceSuppGoodsVo>();
			List<Person> personList = null;
			OrderRequiredVO orderRequiredvO = null;
			List<InsuranceSuppGoodsVo> LocalRouteSuppGoodsList = new ArrayList<InsuranceSuppGoodsVo>();//关联销售当地游
			try {

				List<OrdOrderItem> orderItemList = order.getOrderItemList();
				if(null!=orderItemList&&orderItemList.size()>0){
					startTime = System.currentTimeMillis();
					for (OrdOrderItem orderitem : orderItemList) {
						ResultHandleT<SuppGoods> resultHandleT = suppGoodsClientService.findSuppGoodsById(orderitem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
						SuppGoods suppGoods=resultHandleT.getReturnContent();
						if(resultHandleT.isSuccess()&&resultHandleT.getReturnContent()!=null){
							String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
							if(ProductCategoryUtil.isInsurance(categoryCode)){
								InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, insuranceVo);
								insuranceVo.setQuantity(orderitem.getQuantity().intValue());
								insuranceNum+=orderitem.getQuantity();
								bxGoodsList.add(insuranceVo);
							}else if(orderitem.hasContentValue(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), "自备签")){
								InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, insuranceVo);
								insuranceVo.setQuantity(orderitem.getQuantity().intValue());
								bxGoodsList.add(insuranceVo);
							}else if(orderitem.getItem() != null && "localRoute".equals(orderitem.getItem().getGoodType())){//当地游
								InsuranceSuppGoodsVo LoocalRouteVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, LoocalRouteVo);
								LoocalRouteVo.setQuantity(orderitem.getQuantity().intValue());
								LocalRouteSuppGoodsList.add(LoocalRouteVo);
							}
						}
					}
					LOG.info(ComLogUtil.printTraceInfo(methodName, "获取的SuppGoods, 已装载供应商,合同,产品,商品信息====>【此处For循环】" + orderItemList.size() + "次",
							"suppGoodsClientService.findSuppGoodsById", (System.currentTimeMillis()-startTime)));
				}

				startTime = System.currentTimeMillis();
				orderRequiredvO = queryItemInfo(order);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "获取订单必填项信息",
						"this.queryItemInfo", (System.currentTimeMillis()-startTime))); //：

				// 获取常用联系人
				startTime = System.currentTimeMillis();
				personList = orderService.loadUserReceiversByUserId(buyInfo.getUserId());
				LOG.info(ComLogUtil.printTraceInfo(methodName, "查找相关联系人 ",
						"orderService.loadUserReceiversByUserId", (System.currentTimeMillis()-startTime)));

			} catch (Exception e) {
				LOG.error("{}", e);
			}

			//应付要减去奖金

			if(CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())){
				if(null != ordOrder.getCouponAmount() && ordOrder.getCouponAmount()!=0L){
					order.setCouponAmount(ordOrder.getCouponAmount());
				}
			}

			model.addAttribute("LocalRouteSuppGoodsList", LocalRouteSuppGoodsList);
			model.addAttribute("order", order);
			model.addAttribute("bxGoodsList", bxGoodsList);
			model.addAttribute("personNum", personNum);
			model.addAttribute("insuranceNum", insuranceNum);
			model.addAttribute("personList", personList);
			model.addAttribute("orderRequiredvO", orderRequiredvO);
			model.addAttribute("contactPerson", new OrdPerson());
			model.addAttribute("emergencyPerson", new OrdPerson());
			model.addAttribute("tavellerList", tavellerList);
			model.addAttribute("adult",buyInfo.getAdultQuantity());
			model.addAttribute("child", buyInfo.getChildQuantity());

			model.addAttribute("bonusAmountHidden", buyInfo.getBonusAmountHidden());
			model.addAttribute("hasContractOrder",CollectionUtils.isNotEmpty(order.getOrdTravelContractList()));
			//***********意向单订单*************
			if(StringUtils.isNotEmpty(intentionId)){
				startTime = System.currentTimeMillis();
				Map<String, Object> param = new HashMap<String, Object>();
				param.put(IOrderIntentionService.ORD_NO, intentionId);
				List<IntentionOrder> intenetions = orderIntentionService.queryIntentionsByCriteria(param);
				OrdPerson contactPerson= new OrdPerson();
				//意向单联系人Id Number
				contactPerson.setOrdPersonId(buyInfo.getUserNo());
				contactPerson.setFullName(intenetions.get(0).getContactsName());
				contactPerson.setMobile(intenetions.get(0).getTel());
				contactPerson.setEmail(intenetions.get(0).getEmail());
				model.addAttribute("contactPerson", contactPerson);
				//把订单号更新到意向单表中
				IntentionOrder intentionOrder = new IntentionOrder();
				intentionOrder.setOrderId(order.getOrderId());
				intentionOrder.setIntentionOrderId(Long.valueOf(intentionId));
				//更新已下单
				intentionOrder.setState("1");
				orderIntentionService.updateIntention(intentionOrder);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "意向单获取与修改 ",
						"orderIntentionService.queryIntentionsByCriteria/orderIntentionService.updateIntention", (System.currentTimeMillis()-startTime)));
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "整个创建订单耗费时间",
					"createOrder", (System.currentTimeMillis()-startTime)));

			//***********意向单订单*************
			return "/order/orderFormInfo";
		} catch (Exception e) {
			LOG.error("{}", e);
			model.addAttribute("ERROR", "下单错误，请检查订单项价格，数量是否正确");
			return ERROR_PAGE;
		}

	}

	/**
	 * 根据规格id得到规格的描述信息
	 */
	@RequestMapping(value="/ord/order/queryProdBranchDesc.do")
	public void getDescByProdBranchId(HttpServletRequest req,HttpServletResponse respons,String prodBranchId){
		String prodBranchDesc="";

		ProdProductBranch prodBranchObj=null;
		ResultHandleT<ProdProductBranch> rh;
		try {
			rh = prodProductBranchClientService.findProdProductBranchById(Long.parseLong(prodBranchId));
			if(rh!=null&&rh.isSuccess()&&rh.getReturnContent()!=null){
				prodBranchObj=rh.getReturnContent();
			}
			if(prodBranchObj!=null){
				Long branchId=prodBranchObj.getBranchId();

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("branchId", branchId);
				params.put("propCode", "branch_desc");
				//查询该规格下面所有的产品规格（包括无效的）
				ResultHandleT<List<BizBranchProp>> bizRh = branchPropClientService.findBranchPropList(params);
				BizBranchProp bizBranchPop=null;
				if(bizRh!=null&&bizRh.isSuccess()&&bizRh.getReturnContent()!=null){
					if(bizRh.getReturnContent().size()>0){
						bizBranchPop=bizRh.getReturnContent().get(0);
						params.clear();
						params.put("productBranchId", Long.parseLong(prodBranchId));
						params.put("propId", bizBranchPop.getPropId());
						ResultHandleT<List<ProdProductBranchProp>> branchPropRh=prodProductBranchPropClientRemote.findProdProductBranchPropList(params);
						if(branchPropRh!=null&&branchPropRh.isSuccess()&&branchPropRh.getReturnContent()!=null){
							if(branchPropRh.getReturnContent().size()>0){
								prodBranchDesc=branchPropRh.getReturnContent().get(0).getProdValue();
							}
						}
					}
				}
			}
		}  catch (Exception e) {
			LOG.error("{}", e);
			prodBranchDesc="";
		}


		this.sendAjaxMsg(prodBranchDesc);
	}
	/**
	 * 组装BuyInfo的信息
	 *
	 * @param buyInfo
	 * @param req
	 */
	private void createBuyInfo(BuyInfo buyInfo, HttpServletRequest req) {

		// 现在所有页面都有计算好的成人数和儿童数
		// 对于酒店套餐是根据基础儿童数*份数/基础成人数*份数
		String adultQuantityValue = req.getParameter("adultNumValue");
		String childNumValue = req.getParameter("childNumValue");
		converBuyInfo(buyInfo, adultQuantityValue, childNumValue);
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
					setHotelcombProductCategoryId(item,buyInfo);
					itemList.add(item);
				}
			}
			buyInfo.setItemList(itemList);
			buyInfo.getItemMap().clear();
		}

		if (MapUtils.isNotEmpty(buyInfo.getProductMap())) {
			Long productId = null;
			Product product = null;
			List<Item> itemList = null;
			Collection<Product> products = buyInfo.getProductMap().values();
			//转换短信模板中需要用到的航班信息的出发和到达日期
			orderLineProductQueryUtil.convertTime(products);
			Iterator<Long> itr = buyInfo.getProductMap().keySet().iterator();
			while (itr.hasNext()) {
				productId = itr.next();
				product = buyInfo.getProductMap().get(productId);
				product.setProductId(productId);
				itemList = new ArrayList<Item>();
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
	 * 宋城酒套可订校验修复 20180508
	 * @param item
	 * @param buyInfo
	 */
	private void setHotelcombProductCategoryId(Item item, BuyInfo buyInfo) {
		if(buyInfo.getCategoryId() != null && buyInfo.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId())){
			item.setProductCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId());
		}
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
	private Map<String,Object> loadProduct(ModelMap model, String productId, Date specDate,
			Long adultNum, Long childNum, boolean isHotel, Long startDistrictId, OrdOrder order,
			HttpServletRequest req, ProdProduct prodProduct) {
		String resultUrl="";
		long startAll=System.currentTimeMillis();
		String methodName = "OrderLineProductQueryAction#loadProduct-->productId = " + productId;
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
		//关联销售门票的所有
		List<ProdProductBranch> reTicketBranchList=null;
		//关联销售当地游的所有
		List<ProdProductBranch> reLineBranchList=null;
		//关联销售交通的所有
		List<ProdProductBranch> reTransportBranchList=null;
		//关联销售交通的所有
		List<ProdProductBranch> visaBranchList=null;
		//关联销售门票的详细信息
		Map<String, Object> relSaleDetailMap = null;
		//关联销售当地游的详细信息
		List<Map<String, Object>>  relSaleLocalDetailList = null;

		List<String> relSaleDisplayOrder = new ArrayList<String>();
		//是否存在关联销售
		boolean hasRelation=false;

		Map<Long, SuppGoodsRelation> relationMap = null;

		Long pdId = Long.valueOf(productId);
		Long categoryId = null;
		boolean isLvmamaProduct = false;
		boolean havechangeButtonFlag = false;//是否有更换按钮标识
		boolean ifQueryDuijie = false;//是否查询对接标识，此查询只为判断交通是否可更换


		/*判断是否含有往返交通*/
		boolean hasRoundTypeTrans = false;
		/*判断是否含有单程交通*/
		boolean hasOneWayTrans = false;

		boolean hasPackage = false;
		// 跟团游、自由行,供应商打包需要通过打包的方式进行商品加载
		Map<String, List<ProdPackageGroup>> packageMap = null;
		//以下两按钮只在判断可否更换交通时使用
	    PackageTourProductVo lineProductVoTemp = null;
	    List<ProdPackageGroup>plistTemp = null;
	  //自主打包酒店
	        List<Map<String,Object>>hotelList = new ArrayList<Map<String,Object>>();
	    /** 判断是否有对接机票 */
		boolean isDuijie = false;//用来判断走不走对接机票查询
		try {
			// 传入成人数、儿童数
			long s1=System.currentTimeMillis();
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("productId", pdId);
			paramMap.put("specDate", specDate);
			paramMap.put("adultQuantity", adultNum);
			paramMap.put("childQuantity", childNum);
			paramMap.put("distributorId", Constant.DIST_BACK_END);
			paramMap.put("startDistrictId", startDistrictId);
			ResultHandleT<PackageTourProductVo> ReturnlineProductVo= prodCalClientRemote.getPackageTourProductVo(paramMap);
			/***
			if (ReturnlineProductVo!=null && ReturnlineProductVo.getReturnContent() == null) {
				//重新赋值给specDate
				List<ProdGroupDate> aryProdGroupDates=prodGroupDateClientService.findProdGroupDatesList(pdId).getReturnContent();
				for(ProdGroupDate prodGroupDate : aryProdGroupDates){
					specDate=prodGroupDate.getSpecDate();
					paramMap.put("specDate", specDate);
					ReturnlineProductVo= prodCalClientRemote.getPackageTourProductVo(paramMap);
					if(ReturnlineProductVo!=null && ReturnlineProductVo.getReturnContent()!=null ){
						break;
					}
				}
			}*/

			//如果没有查询到线路产品   返回页面 提示库存不足
			if (ReturnlineProductVo!=null && ReturnlineProductVo.getReturnContent() == null) {
				Map<String,Object> resMap=new HashMap<String, Object>();
				resMap.put("specDate", specDate);
				resMap.put("resultUrl","/order/orderProductQuery/findLineProductDetailList");
				return resMap;

			}

			long s2=System.currentTimeMillis();
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询线路产品打包信息", "prodCalClientRemote.getPackageTourProductVo", s2-s1));
			if (ReturnlineProductVo.getReturnContent() == null) {
				model.addAttribute("ERROR", "商品不可售");
				resultUrl="order/error";
			}else{
				lineProductVo=ReturnlineProductVo.getReturnContent();
			}


			String apiFlag=ReturnlineProductVo.getMsg();
			if(ReturnlineProductVo != null ){
				if(apiFlag != null && ErrorCodeMsg.ERR_FAPI_FRONT_001.equalsIgnoreCase(apiFlag)){
					isDuijie = true;
				}

				if(Constants.TRANSPORT_CHANGE_001.equalsIgnoreCase(ReturnlineProductVo.getInfoMsg())){
            		havechangeButtonFlag = true;
					hasRoundTypeTrans = true;
            	}else if(Constants.TRANSPORT_CHANGE_002.equalsIgnoreCase(ReturnlineProductVo.getInfoMsg())){
            		ifQueryDuijie = true;
					hasRoundTypeTrans = true;
					hasOneWayTrans = true;
            	}

				LOG.info(productId+ " havechangeButton -->prodCalClientRemote.getPackageTourProductVo: infoMsg【"+ ReturnlineProductVo.getInfoMsg() +"】****"
						+ "errorMsg【"+ ReturnlineProductVo.getMsg() +"】");
			}
			//行程信息
			ProdLineRouteVO prodLineRoute=new ProdLineRouteVO();
			if(CollectionUtils.isNotEmpty(lineProductVo.getProdLineRouteList())){
				prodLineRoute=lineProductVo.getProdLineRouteList().get(0);
			}
			model.addAttribute("hasApiFlight", isDuijie?"Y":"N");
			//不是对接且该产品支持游玩人后置
			model.addAttribute("isTravellerDelay", !isDuijie && "Y".equals(lineProductVo.getTravellerDelayFlag()));
			model.addAttribute("prodLineRoute",prodLineRoute);
		    //调用康宝霞接口获取排序好的组(查询产品所有组)
			long startTime=System.currentTimeMillis();
		    ResultHandleT<List<ProdPackageGroup>> allTrificListReturn=prodPackageGroupClientService.getProdPackageGroupList(pdId, ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
		    LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询线路产品下打包的组列表,用于排序",
		    		"prodPackageGroupClientService.getProdPackageGroupList", System.currentTimeMillis() - startTime));

		    List<ProdPackageGroup> plist = allTrificListReturn.getReturnContent();
		    //如果既有往返又有单程则更多交通里面既有往返推荐又有自由组合
		    if(plist!=null){
		    	model.addAttribute("groupSize",plist.size());
		    }

		    //以下两按钮只在判断可否更换交通时使用
		    lineProductVoTemp = lineProductVo;
		    plistTemp = plist;

			//如果非对接不满足，则查询对接机票
			if(isDuijie){
				//含有对接机票时处理方法
                String apiFlightResult= havaAPIMesssetHandle(pdId, specDate, adultNum, childNum,lineProductVo,req,startAll, startDistrictId,plist);

               	if (apiFlightResult!=null) {
               		model.addAttribute("ERROR", apiFlightResult);
               		resultUrl="order/error";
				}
            }

			List<ProdProductBranch> prodProductBranchList = lineProductVo.getProdProductBranchList();
			if (prodProductBranchList == null || prodProductBranchList.size() == 0) {
				model.addAttribute("ERROR", "商品不可售");
				resultUrl="order/error";
			}
			if ("LVMAMA".equalsIgnoreCase(lineProductVo.getPackageType())) {
				isLvmamaProduct = true;
			}
			// 初始化打包组
			packageMap = lineProductVo.getProdPackageGroupMap();
			
			/**********************自由行机+酒 酒店+国内 展示信息 Start**********************/
			Map<String,List<ProdPackageGroup>> prodPackageGroupMap = null;
		        if(lineProductVo != null){
		            prodPackageGroupMap = lineProductVo.getProdPackageGroupMap();
		        }
		      //如果有组，组合组的信息
		        if(null != prodProduct.getSubCategoryId() &&
				null != prodProduct.getProductType() &&
				prodPackageGroupMap != null && prodPackageGroupMap.size() > 0
		        	&& prodProduct.getSubCategoryId() == 182L
		        	&& "INNERLINE".equals(prodProduct.getProductType())){
		            Set<String> keySet = prodPackageGroupMap.keySet();
		            for(String key : keySet){
		        	//list的获得必须放在上面对接的处理之后,因为prodPackageGroupMap有可能改变
		                List<ProdPackageGroup> prodPackageGroupList = prodPackageGroupMap.get(key);
		                if(prodPackageGroupList == null || prodPackageGroupList.size() == 0){
		                    continue;
		                }
		                Map<String, Object>assembleMap = new HashMap<String, Object>();
		              //下面的都是酒店组
		                //HOTEL属于打包的产品就是酒店
		                //_group和freedom自主打包打包的供应商打包的跟团游---自主打包打包的供应商打包的自由行--现在都只能是可换酒店
		                if(ProdPackageGroup.GROUPTYPE.HOTEL.getCode().equalsIgnoreCase(key)){
		                	assembleMap.clear();
		                	assembleMap.put("prodPackageGroupList", prodPackageGroupList);
		                	assembleMap.put("selectDate", specDate);
		                	assembleMap.put("hotelList", hotelList);
		                	this.assembleHotelGroup(assembleMap);
		                }
		            }
		        }
			/**********************机+酒 酒店展示信息 End**********************/
			// 把标配的信息先拿出来，其中酒店套餐只有套餐
			// 对于自主打包的产品没有标配信息
			long s3=System.currentTimeMillis();
			Map<String, List<ProdProductBranch>> map = this.findProdProductBranchVoMap(prodProductBranchList);

			long s4=System.currentTimeMillis();
			LOG.info("时间戳"+startAll+"@线路后台加载商品调用按照规格类型对规格进行分类用时@OrderLineProductQueryAction.findProdProductBranchVoMap@"+(s4-s3));
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】线路后台加载商品调用按照规格类型对规格进行分类",
		    		"OrderLineProductQueryAction.findProdProductBranchVoMap", (s4-s3)));

			if (map == null) {
				model.addAttribute("ERROR", "加载商品信息出错");
				resultUrl="order/error";
			}

			additionList = map.get("addition");
			adultChildDiffList = map.get("adult_child_diff");
			comboDinnerList = map.get("combo_dinner");

			categoryId = lineProductVo.getBizCategoryId();

			/**
			 * 国内自主打包 自由行酒景 后台下单，酒店信息与前台商品页展示效果保持一致
			 */
			if ("LVMAMA".equalsIgnoreCase(lineProductVo.getPackageType())
					&&(ProdProduct.PRODUCTTYPE.INNERLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equalsIgnoreCase(prodProduct.getProductType())
	                ||ProdProduct.PRODUCTTYPE.INNER_BORDER_LINE.name().equalsIgnoreCase(prodProduct.getProductType()))
					&&(prodProduct.getBizCategoryId() == 18L && prodProduct.getSubCategoryId() == 181L)
					&&"DESTINATION_BU".equalsIgnoreCase(prodProduct.getBu())) {
				model.addAttribute("hotelList", getHotelList(lineProductVo,specDate));
			}

			// 自主打包只需要关注打包信息加载，对打包信息需要按照规格进行分组MAP(自主打包肯定有品类id)
			// 根据产品得到组，根据组得到改组的行程，根据行程得到该行程下的规格，根据规格得到产品
			LineProdPackageGroupContainer container =null;
            List<SuppGoodsItem> oneKeyOrderChangedGoods =null;

			if (packageMap != null) {
				boolean isSupplier=true;
				if(lineProductVo.getPackageType().equalsIgnoreCase("LVMAMA")){
					isSupplier=false;
				}
				long s5=System.currentTimeMillis();

				container = lineProdPackageGroupServiceImpl.initPackageProductMap(specDate, packageMap,isSupplier);
				long s6=System.currentTimeMillis();
				LOG.info("时间戳"+startAll+"@线路后台加载商品调用计算打包产品所有组的时间价格表用时@LineProdPackageGroupService.initPackageProductMap@"+(s6-s5));


				// 供应商打包的升级和可换酒店
				// 得到升级
				updateProdPackageList = container.getUpdateProdPackageList();
				// 得到跟换酒店
				changeProdPackageList = container.getChangeProdPackageList();

				// 得到酒店打包信息
				hotelProdPackageList = container.getHotelProdPackageList();
				// 得到交通
				transprotProdPackageList = container
						.getTransprotProdPackageList();

				// 得到门票打包信息
				ticketProdPackageList = container.getTicketProdPackageList();

                //验证线路期票有效期信息
                Map<String, String> lineTicketValidMsgMap = getAperLineTicketValidMsg(specDate, ticketProdPackageList);
                model.put("lineTicketValidMsgMap", lineTicketValidMsgMap);

				//一键下单门票出游时间设置
				if(order != null){
					setOrderTicketSuppGoods(model, ticketProdPackageList, order);
				}

				// 得到线路酒店套餐
				hotelCombPackageList = container.getLineHotelCombPackageList();
				// 得到线路跟团游
				groupPackageList = container.getLineGroupPackageList();
				// 得到线路自由行
				freedomPackageList = container.getLineFreedomPackageList();
				// 得到线路当地游
				localPackageList = container.getLineLocalPackageList();

				//如果是一键下单，把订单的商品设置为推荐商品
				if(order != null){
					Map<String, List<OrdOrderItem>> ordItemMap = getOrdOrderItems(order);
					if(ordItemMap != null){
						oneKeyOrderChangedGoods = new ArrayList<SuppGoodsItem>();
						//修改升级的推荐商品
						List<OrdOrderItem> orderItemList = ordItemMap.get("update");
						setOrderSuppGoods(oneKeyOrderChangedGoods, updateProdPackageList, orderItemList,
								model, "update");
						//修改更换酒店的推荐商品
						orderItemList = ordItemMap.get("change");
						setOrderSuppGoods(oneKeyOrderChangedGoods, changeProdPackageList, orderItemList);
						//修改酒店的推荐商品
						orderItemList = ordItemMap.get("hotel");
						setOrderSuppGoods(oneKeyOrderChangedGoods, hotelProdPackageList, orderItemList);
						//修改门票的推荐商品
						orderItemList = ordItemMap.get("ticket");
						setOrderSuppGoods(oneKeyOrderChangedGoods, ticketProdPackageList, orderItemList);
						//修改交通的推荐商品
						orderItemList = ordItemMap.get("transprot");
						setOrderSuppGoods(oneKeyOrderChangedGoods, transprotProdPackageList, orderItemList);
						//修改酒店套餐的推荐商品
						orderItemList = ordItemMap.get("hotelComb");
						setOrderSuppGoods(oneKeyOrderChangedGoods, hotelCombPackageList, orderItemList);
						//修改跟团游的推荐商品
						orderItemList = ordItemMap.get("group");
						setOrderSuppGoods(oneKeyOrderChangedGoods, groupPackageList, orderItemList);
						//修改自由行的推荐商品
						orderItemList = ordItemMap.get("freedom");
						setOrderSuppGoods(oneKeyOrderChangedGoods, freedomPackageList, orderItemList);
						//修改当地游的推荐商品
						orderItemList = ordItemMap.get("local");
						setOrderSuppGoods(oneKeyOrderChangedGoods, localPackageList, orderItemList);
					}
					//检查行程是否有变化
					if(prodLineRoute != null && order.getLineRouteId() != null
			   				 && prodLineRoute.getLineRouteId() != null){
			       		if(prodLineRoute.getLineRouteId().longValue() != order.getLineRouteId().longValue()){
			       			SuppGoodsItem item = new SuppGoodsItem();
							item.setCategoryName("行程");
							item.setOrderSuppGoodsName("行程Id:"+ order.getLineRouteId().toString());
							item.setOrderSuppGoodId(order.getLineRouteId());
							oneKeyOrderChangedGoods.add(item);
			       		}
			   		}
				}

				// 如果的是非对接机票 组装成对接机票格式 add by zm 2015-10-14
				try {
					this.jipiaoChangeDuijie(transprotProdPackageList);
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
					LOG.error("将非对接机票转换为对接机票格式的数据 时，发生异常");
				}

				hasPackage=container.isHasPackage();
				if(!hasPackage){
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.name().equals(lineProductVo.getBizCategory().getCategoryCode())
							||BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(lineProductVo.getBizCategory().getCategoryCode())
							){
						hasPackage=true;
					}
				}
			}

			// 处理关联销售信息
			List<SuppGoodsSaleRe> suppGoodsSaleReList = lineProductVo.getSuppGoodsSaleReList();

			Map<String, List<ProdProductBranch>> reSaleBranchListMap=this.getReSaleCategoryMap(suppGoodsSaleReList);
			if(reSaleBranchListMap!=null&&reSaleBranchListMap.size()>0){
				//关联销售门票的所有
				reTicketBranchList=reSaleBranchListMap.get("ticket");
				//关联销售当地游的所有
				reLineBranchList=reSaleBranchListMap.get("line");
				//关联销售交通的所有
				reTransportBranchList=reSaleBranchListMap.get("transport");
				//关联销售签证的所有
				visaBranchList=reSaleBranchListMap.get("visa");
				hasRelation=true;
			}

			//构建关联销售门票的详细信息
			relSaleDetailMap = buildRelSaleDetailInfo(suppGoodsSaleReList, order, lineProductVo, specDate);
			setRelSaleDisplayOrder(relSaleDisplayOrder);
			//构建关联销售当地游的详细信息
			relSaleLocalDetailList = buildRelSaleLocalDetailInfo(suppGoodsSaleReList, lineProductVo, specDate,order);

			//处理附加信息
			if (isLvmamaProduct&&hasPackage && CollectionUtils.isNotEmpty(lineProductVo.getProductBranchList())) {
				additionList = lineProductVo.getProductBranchList();
			}

			// 次规格对应主规格关系Map
			relationMap = this.findProdProductRelationMap(prodProductBranchList);
			// 为所有的附加信息关联父信息对象、时间价格表


			long s7=System.currentTimeMillis();
			this.initAdditionProductBranchList(relationMap, additionList, specDate,container);

			long s8=System.currentTimeMillis();
			LOG.info("时间戳"+startAll+"@线路后台加载商品调用为附加信息增加父对象用时@OrderLineProductQueryAction.initAdditionProductBranchList@"+(s8-s7));

			List<SuppGoodsSaleRe> insuranceList = getInsuranceList(suppGoodsSaleReList);
			if(CollectionUtils.isNotEmpty(insuranceList)){
				boolean flag=false;
				for(SuppGoodsSaleRe re:insuranceList){
					if(CollectionUtils.isNotEmpty(re.getInsSuppGoodsList())){
						flag=true;
						break;
					}
				}
				model.addAttribute("existsInsurance",flag);
			}
			model.addAttribute("suppGoodsSaleReList", insuranceList);

			if(order!=null){
                processChangedGoods(model, map, oneKeyOrderChangedGoods, insuranceList, additionList);
			}

			 //解决包含项目的问题
	        if(lineProductVo.getPropValue() != null){
	            StringBuffer containedItem = new StringBuffer("");
	            Map<String, Object> propValueMap = lineProductVo.getPropValue();
	            //处理供应商打包的并且包含交通信息的
	            String trafficFlag = (String)propValueMap.get("traffic_flag");
	            if(categoryId==16L){ //由于当地游产品属性里没有往返上下车属性，故在此直接设置去取交通信息
	            	trafficFlag="Y";
	            }
	            if(trafficFlag != null && "Y".equalsIgnoreCase(trafficFlag)){
	            	//获取打包中的交通信息
	            	List<Map> trafficInfoList=getBusStopAddress(lineProductVo.getProductId(),categoryId);
	            	model.addAttribute("trafficInfoList", trafficInfoList);
	            }
	        }
		} catch (Exception e) {
			LOG.error("{}", e);
			model.addAttribute("ERROR", e.getMessage());
			resultUrl="order/error";
		}

		//房差总单价
        Long total = 0L;
        //房差最小份数
        StringBuffer fangchaMin=new StringBuffer("0");
        //房差份数选择
        List<String> selectRange = new ArrayList<String>();
        //房差隐藏字符串
        StringBuffer sb = new StringBuffer("");
        Map<String,String> hiddenMain = new HashMap<String, String>();
		//自主打包
		if ("LVMAMA".equalsIgnoreCase(lineProductVo.getPackageType())) {
			if (packageMap != null && packageMap.size() > 0) {
				Set<String> keys = packageMap.keySet();
				for (String key : keys) {
					List<ProdPackageGroup> prodPackageGroupList = packageMap.get(key);
					if (prodPackageGroupList == null || prodPackageGroupList.size() == 0) {
						continue;
					}
					if ((ProdPackageGroup.GROUPTYPE.LINE.name() + "_group").equals(key)
							|| (ProdPackageGroup.GROUPTYPE.LINE.name() + "_freedom").equals(key)
							|| (ProdPackageGroup.GROUPTYPE.LINE.name() + "_local").equals(key)
							|| ProdPackageGroup.GROUPTYPE.CHANGE.getCode().equalsIgnoreCase(key)
							|| ProdPackageGroup.GROUPTYPE.UPDATE.getCode().equals(key)) {
						String type = key.toLowerCase();
						total = total + this.calGapPrice(prodPackageGroupList, sb, type,fangchaMin, selectRange);
					}
				}
			}
		} else if ("SUPPLIER".equalsIgnoreCase(lineProductVo.getPackageType())) {// 供应商打包
			BizCategory bizCategory = lineProductVo.getBizCategory();
			if ("category_route_group".equalsIgnoreCase(bizCategory.getCategoryCode())
				|| "category_route_local".equalsIgnoreCase(bizCategory.getCategoryCode())
				|| "category_route_freedom".equalsIgnoreCase(bizCategory.getCategoryCode())) {

				long s9=System.currentTimeMillis();
				//主规格房差
				if (adultChildDiffList != null && adultChildDiffList.size() > 0) {
					for (int i = 0; i < adultChildDiffList.size(); i++) {
						//主规格
						ProdProductBranch prodProductBranch = adultChildDiffList.get(i);
						BizBranch bizBranch = branchClientRemote.findBranchById(prodProductBranch.getBranchId()).getReturnContent();
						String attachFlag = bizBranch.getAttachFlag();
						if ("Y".equalsIgnoreCase(attachFlag)) {
							boolean hasGap = false;
							//取房差值
							Map<String, Long> selectGapPriceMap = prodProductBranch.getSelectGapPriceMap();
							String selectGapQuantityRange = prodProductBranch.getSelectGapQuantityRange();
							Long price = 0L;
							if (selectGapPriceMap != null && selectGapPriceMap.size() > 0 && selectGapQuantityRange != null) {
								//每个房差的价格
								Set<String> keys = selectGapPriceMap.keySet();
								for (String key : keys) {
									if(key != null){
										price = selectGapPriceMap.get(key);
										if(price != null){
											total = total + price;
											hasGap = true;
											break;
										}
									}
								}
								if(hasGap){
									//房差份数选择
									selectRange.clear();
									selectRange.add(selectGapQuantityRange.trim());
									//每个房差的份数
									int fangChaQuantity = 0;
									if (selectGapQuantityRange.startsWith("1")) {
										fangChaQuantity = 1;
										fangchaMin.deleteCharAt(0);
										fangchaMin.append("1");
									}
									sb.append("<div class=\"lvmama-fangcha-price\" data-type=\""
											+ "adultChild"
											+ "\" groupId=\""
											+ "001"
											+ "\" id=\""
											+ "001-001gap"
											+ "\" data-status=\""
											+ "Y"
											+ "\" data-quantity=\""
											+ fangChaQuantity
											+ "\" data-fangcha=\""
											+ PriceUtil.trans2YuanStr(price)
											+ "\"></div>");
								}
							}
						}
					}
				}

				long s10=System.currentTimeMillis();
				LOG.info("时间戳"+startAll+"@线路后台加载商品调用主规格房差用时@OrderLineProductQueryAction@"+(s10-s9));

				//升级和跟换酒店
				total = total + this.calGapPrice(updateProdPackageList, sb, "update",fangchaMin, selectRange);
				total = total + this.calGapPrice(changeProdPackageList, sb, "change",fangchaMin, selectRange);
			}
		}
		//房差总单价
        model.addAttribute("fanchaTotalPrice",PriceUtil.trans2YuanStr(total));
        //房差最小份数
        model.addAttribute("fangchaMin", fangchaMin.toString());
        //房差份数选择
        if(selectRange.size() > 0){
        	model.addAttribute("selectRange", selectRange.get(0).toString());
        }else{
        	model.addAttribute("selectRange", "0");
        }
        //每份房差隐藏
        hiddenMain.put("fangchaDiv", ("".equals(sb.toString()) ? null : sb.toString()));
        model.addAttribute("hiddenMain",hiddenMain);

		model.addAttribute("packageTourProductVo", lineProductVo);

		// 标配信息
		model.addAttribute("additionList", additionList);
		model.addAttribute("adultChildDiffList", adultChildDiffList);
		model.addAttribute("comboDinnerList", comboDinnerList);

		//自主打包
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
			LOG.info("dealJipiaoData处理之前，" + productId + " havechangeButton: model.get('flightMoreGoods')【"+ model.get("flightMoreGoods") +"】");
			this.dealJipiaoData(model,transprotProdPackageList,productId,isDuijie);
			LOG.info("dealJipiaoData处理之后，" + productId + " havechangeButton: model.get('flightMoreGoods')【"+ model.get("flightMoreGoods") +"】");
			if(havechangeButtonFlag){
				model.addAttribute("flightMoreGoods", "Y");
			}else if(!"Y".equals(model.get("flightMoreGoods")) && ifQueryDuijie){
				LOG.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：产品有可售的往返，存在单程但不存在单程非对接，若不可更换，则查询单程对接情况");
            	try {
                    havaAPIMesssetHandle(pdId, specDate, adultNum, childNum,lineProductVoTemp,req,startAll, startDistrictId,plistTemp);
                    Map<String, List<ProdPackageGroup>> packageMapTemp = lineProductVo.getProdPackageGroupMap();
                    List<ProdPackageGroup> prodPackageGroupListTemp =
                    		packageMapTemp.get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
                    if(CollectionUtils.isNotEmpty(prodPackageGroupListTemp)){
                    	for (ProdPackageGroup p : prodPackageGroupListTemp) {

							List<ProdPackageDetail>details =  p.getProdPackageDetails();
							String jiPiaoDuiJieFlag = p.getJiPiaoDuiJieFlag();
							if(CollectionUtils.isNotEmpty(details)){
								LOG.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：details.size()【"+details.size()+"】");
								if(details.size()>1){
									model.addAttribute("flightMoreGoods", "Y");
								}
								for (ProdPackageDetail detail : details) {

									ProdProductBranch prodProductBranch = detail.getProdProductBranch();
									if(prodProductBranch != null){
										List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
										if(suppGoodsList != null && suppGoodsList.size() > 0){

											//如果是机票对接的组
											if("Y".equalsIgnoreCase(jiPiaoDuiJieFlag)){
												Map<String, FlightNoVo> flightNoVoMap = prodProductBranch.getSuppGoodsMap();
												if(flightNoVoMap != null && flightNoVoMap.size() > 0){
													LOG.info(productId + " havechangeButton: 进入对接查询,TRANSPORT_CHANGE_002：flightNoVoMap.size()【"+flightNoVoMap.size()+"】");
													//规格下商品多于一个则表示交通可更换,
													if(flightNoVoMap.size() > 1 ){
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
                	LOG.info("方法dealJipiaoData," + productId + " havechangeButton: 异常【"+ e.getMessage() +"】");
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.error("设置flightMoreGoods/transportType/toFlightParam/backFlightParam出现异常。");
		}

		//如果既有章程交通，又有往返交通，设定havechangeButton为"Y"
		if((hasOneWayTrans && hasRoundTypeTrans) || havechangeButtonFlag){
			model.addAttribute("haveChangeButton", "Y");
		}

		model.addAttribute("transprotProdPackageList", transprotProdPackageList);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("isLvmamaProduct", isLvmamaProduct);

		//关联销售
		model.addAttribute("reTicketBranchList", reTicketBranchList);
		model.addAttribute("reLineBranchList", reLineBranchList);
		model.addAttribute("reTransportBranchList", reTransportBranchList);
		model.addAttribute("visaBranchList", visaBranchList);
		//关联销售的详细信息
		model.addAttribute("relSaleDetailMap", relSaleDetailMap);
		model.addAttribute("relSaleDisplayOrder", relSaleDisplayOrder);
		model.addAttribute("relSaleLocalDetailList", relSaleLocalDetailList);

		//是否存在关联销售
		model.addAttribute("hasRelation", hasRelation);
		model.addAttribute("hotelLists", hotelList);
		//单房差
		Long gapPrice=lineProductVo.getGapPrice();
		if(gapPrice==null){
			gapPrice=0L;
		}
		String gapPriceStr=PriceUtil.trans2YuanStr(gapPrice);

		model.addAttribute("gapPrice",gapPriceStr);

		/*********************************需要快递的商品id列表*******************************/
		List<Long> needExpressGoodsList=new ArrayList<Long>();
		List<Long> tempList=new ArrayList<Long>();

		//自主打包
		//升级
		tempList=LineUtils.getNeedExpressGoodsListForGroup(updateProdPackageList,false);
		needExpressGoodsList.addAll(tempList);

		//更换酒店
		tempList=LineUtils.getNeedExpressGoodsListForGroup(changeProdPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//门票
		tempList=LineUtils.getNeedExpressGoodsListForGroup(ticketProdPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//酒店套餐
		tempList=LineUtils.getNeedExpressGoodsListForGroup(hotelCombPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//跟团游
		tempList=LineUtils.getNeedExpressGoodsListForGroup(groupPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//自由行
		tempList=LineUtils.getNeedExpressGoodsListForGroup(freedomPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//当地游
		tempList=LineUtils.getNeedExpressGoodsListForGroup(localPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//酒店
		tempList=LineUtils.getNeedExpressGoodsListForGroup(hotelProdPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//大交通
		tempList=LineUtils.getNeedExpressGoodsListForGroup(transprotProdPackageList,true);
		needExpressGoodsList.addAll(tempList);

		//供应商打包
		//成人儿童房差
		tempList=LineUtils.getNeedExpressGoodsListForBranch(adultChildDiffList);
		needExpressGoodsList.addAll(tempList);
		//酒店套餐
		tempList=LineUtils.getNeedExpressGoodsListForBranch(comboDinnerList);
		needExpressGoodsList.addAll(tempList);

		//附加信息
		tempList=LineUtils.getNeedExpressGoodsListForBranch(additionList);
		needExpressGoodsList.addAll(tempList);

		//判断是否含有需要快递的商品
		boolean hasExpress=false;
		if(needExpressGoodsList!=null&&needExpressGoodsList.size()>0){
			hasExpress=true;
		}
		model.addAttribute("hasExpress", hasExpress);

		/*********************************需要快递的商品id列表*******************************/
		long endAll=System.currentTimeMillis();
		LOG.info("时间戳"+startAll+"@线路后台加载商品all用时@OrderLineProductQueryAction.loadProduct@"+(endAll-startAll));
		if (hasPackage) {
			resultUrl= "/order/orderProductQuery/findLineProductPackageDetailList";

		} else {
			resultUrl= "/order/orderProductQuery/findLineProductDetailList";
		}
		Map<String,Object> resMap=new HashMap<String, Object>();
		resMap.put("specDate", specDate);
		resMap.put("resultUrl", resultUrl);

		return resMap;
	}

	private List<Map<String,Object>> getHotelList(PackageTourProductVo packageTourProductVo, Date selectDate){
		//酒店
        List<Map<String,Object>> hotelList = new ArrayList<Map<String,Object>>();
      //供应商打包
        if(packageTourProductVo != null){
            Map<String,List<ProdPackageGroup>> prodPackageGroupMap = packageTourProductVo.getProdPackageGroupMap();
            //如果有组，组合组的信息
	        if(prodPackageGroupMap != null && prodPackageGroupMap.size() > 0){
	            Set<String> keySet = prodPackageGroupMap.keySet();
	            List<String> keyList = new ArrayList<String>(keySet);
	            Collections.sort(keyList);
	            Long hotelNum = null;
	            for(String key : keyList){
	                //list的获得必须放在上面对接的处理之后,因为prodPackageGroupMap有可能改变
	                List<ProdPackageGroup> prodPackageGroupList = prodPackageGroupMap.get(key);
	                if(prodPackageGroupList == null || prodPackageGroupList.size() == 0){
	                    continue;
	                }

                    //prodPackageGroupMap中的key值为'HOTEL'为酒店产品
	                //下面的都是酒店组，HOTEL属于打包的产品就是酒店
	                if(ProdPackageGroup.GROUPTYPE.HOTEL.getCode().equalsIgnoreCase(key)){
	                	setHotelList(hotelList, selectDate, prodPackageGroupList);
	                }
	            }
	        }
        }
        return hotelList;
	}

	/**
     * 填充打包酒店列表
     * @param hotelList
     * @param selectDateStr
     * @param prodPackageGroupList
     */
    private void setHotelList(List<Map<String,Object>> hotelList,Date selectDate, List<ProdPackageGroup> prodPackageGroupList){
    	for(ProdPackageGroup prodPackageGroup : prodPackageGroupList){
    		Map<String,Object> map = new HashMap<String,Object>();
    		int productCount = 0;
            List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
            if(prodPackageDetails != null && prodPackageDetails.size() > 0){
            	Long defaultProductId = prodPackageDetails.get(0).getProdProduct().getProductId();
            	hotelList.add(map);
                List<Map<String,Object>> mapListB = new ArrayList<Map<String,Object>>();
                map.put("others",mapListB);

                //全部加入ListB

                for(ProdPackageDetail prodPackageDetail : prodPackageDetails){
                	if(defaultProductId.longValue() != prodPackageDetail.getProdProduct().getProductId().longValue()){// 不和默认选中房型同一酒店产品
                		productCount++;
                		continue;
                	}
	                ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
	                if(prodProductBranch != null){
	                    List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
	                    if(suppGoodsList==null){
                            LOG.info("景酒加载酒店,prodPackageGroupid："+prodPackageGroup.getGroupId()+" DetailId："+prodPackageDetail.getDetailId()+" suppGoodsList.size: 0");
                        }else{
                            LOG.info("景酒加载酒店,prodPackageGroupid："+prodPackageGroup.getGroupId()+" DetailId："+prodPackageDetail.getDetailId()+" suppGoodsList.size: "+suppGoodsList.size());
                        }
	                    if(suppGoodsList != null && suppGoodsList.size() > 0){
                            for (SuppGoods suppGoods : suppGoodsList) {
                                Map<String,Object> map2 = new HashMap<String,Object>();
                                Long stock = fillHotelMap(map2, prodPackageGroup, selectDate, prodPackageDetail, prodProductBranch, suppGoods);
                                if(stock > 0L){
                                    mapListB.add(map2);
                                }
                            }
                    	}
	                }
                }
                LOG.info("景酒加载酒店,prodPackageGroupid："+prodPackageGroup.getGroupId()+" mapListB.size："+mapListB.size());
                Collections.sort(mapListB, new Comparator<Map<String,Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Long price1 =Long.MAX_VALUE;
                        Long price2 =Long.MAX_VALUE;
                        if(o1!=null && o1.get("price")!=null){
                            price1= (Long) o1.get("price");
                        }
                        if(o2!=null && o2.get("price")!=null){
                            price2= (Long) o2.get("price");
                        }
                        if(price2>price1){
                            return -1;
                        }else if(price2<price1){
                            return 1;
                        }else{
                            return 0;
                        }
                    }
                });

                //取第一个为选中项
                if(mapListB.size()>0){
                    map.put("default_selected",mapListB.get(0));
                    mapListB.remove(0);
                }

            }
            if(productCount > 0){
        		map.put("haveChangeButton","Y");//这一个组里有的可换的产品总数
            }else{
            	map.put("haveChangeButton","N");//这一个组里有的可换的产品总数
            }
        }
    }

    /**
     * 填充酒店商品信息
     * @param map1
     * @param prodPackageGroup
     * @param selectDate
     * @param prodPackageDetail
     * @param prodProductBranch
     * @param suppGoods
     */
    private Long fillHotelMap(Map<String,Object> map1, ProdPackageGroup prodPackageGroup, Date selectDate, ProdPackageDetail prodPackageDetail,
    		 ProdProductBranch prodProductBranch, SuppGoods suppGoods){
    	//得到打包的酒店产品
        ProdProduct hotelProdProduct = prodPackageDetail.getProdProduct();
       //得到酒店的属性
        Map<String,Object> hotelMap = hotelProdProduct.getPropValue();
        String hotelAddress = "";
        if(hotelMap != null){
            hotelAddress = (String)hotelMap.get("address");
        }
        map1.put("hotel_address",hotelAddress);

    	ProdPackageGroupHotel prodPackageGroupHotel =  prodPackageGroup.getProdPackageGroupHotel();//本身打包的就是酒店的东西就在这里
        //生成可订的日期
        String dayStr = prodPackageGroupHotel.getStayDays();

        String checkInDate = "";
        String checkOutDate = "";
        Date startDate = null; // 入住第一天
        Date endDate=null;
        if(dayStr != null && !"".equals(dayStr)){
            String dayArray[] = dayStr.split(",");
            if(dayArray != null && dayArray.length > 0){
                map1.put("days",dayArray.length);//入住几晚
                startDate = DateUtils.addDays(selectDate,Integer.valueOf(dayArray[0])-1);
                Date outDate = DateUtils.addDays(startDate,dayArray.length);
                endDate=DateUtils.addDays(startDate,dayArray.length-1);
                //入住日期一般与所选日期一致
                checkInDate = DateUtil.formatSimpleDate(startDate);
                //在连续入住的情况下,离店日期
                checkOutDate =  DateUtil.formatSimpleDate(outDate);
                // 入住离店年月日星期
                fillCheckInAndOutDate(map1, startDate, outDate);
            }
        }
        map1.put("check_in",checkInDate);
        map1.put("check_out",checkOutDate);
        map1.put("hotel_name",hotelProdProduct.getProductName());

//        String photoUrl = getProductImage(hotelProdProduct.getProductId());
//        if(photoUrl != null && !"".equals(photoUrl)){
//            map1.put("hotel_photo",photoUrl);
//        }
        map1.put("groupId",prodPackageGroup.getGroupId());
        map1.put("packge_type",hotelProdProduct.getPackageType());
        map1.put("productId",hotelProdProduct.getProductId());
        map1.put("room_amenities", hotelMap.get("room_amenities"));
        try {
			List<PropValue> propValueList = (List<PropValue>)hotelMap.get("star_rate");
			if(propValueList != null && propValueList.size() > 0){
				map1.put("star_rate", hotelMap.get("star_rate"));//酒店星级
			}
		} catch (Exception e) {
		}
        map1.put("detailId",prodPackageDetail.getDetailId());
        map1.put("currentProductBranchId",prodProductBranch.getProductBranchId());
        map1.put("groupDivId",prodPackageGroup.getGroupId()+"_"+prodProductBranch.getProductBranchId());
        map1.put("suppGoodsId",suppGoods.getSuppGoodsId());
//        map1.put("name",prodProductBranch.getBranchName());
        //展示到商品，显示商品名称
        map1.put("name",prodProductBranch.getBranchName()+"-"+suppGoods.getGoodsName());

        //单酒店退改规则  TODO hotel_api
        Map<String, String> refundRulesMap=prodRefundRuleClientServiceImpl.getRefundRules(prodPackageGroup.getProductId(), suppGoods.getSuppGoodsId(), selectDate, startDate, endDate);
        if(MapUtils.isNotEmpty(refundRulesMap)){
        	map1.put("type",refundRulesMap.get("type"));
        	map1.put("content",refundRulesMap.get("content"));
        	String type = refundRulesMap.get("type");
        	String content = refundRulesMap.get("content");
        	if(StringUtils.isNotBlank(type)){
	   	       if(type.equals("不可退改") || type.equals("限时退改") || type.equals("不退不改")){
		   	    	if(StringUtils.isNotBlank(content)){
		   	    	    if(content.contains("如您尚未确认行程，建议购买取消险。")){
		   	    		    content = content.replace("如您尚未确认行程，建议购买取消险。","若您的旅程尚未确定,建议购买取消险。");
		   		        }else{
		   		        	content+="若您的旅程尚未确定,建议购买取消险。";
		   		        }
		   	    	  map1.put("content",content);
		   	    	}
	   	       }
        	}
        }else{
        	LOG.info("退改规则获取为空....，productID="+prodPackageGroup.getProductId());
        }

        Long stockNum = 0L;// 库存数
        if(startDate!=null){// 是否含有早餐及库存数
        	//hotel_api
        	ResultHandleT<SuppGoodsTimePrice> resultHandleT = suppGoodsTimePriceAdapterClientRemote.getTimePrice(suppGoods.getSuppGoodsId(), startDate, false);
        	if(resultHandleT!=null && resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null){
        		SuppGoodsTimePrice suppGoodsTimePrice = resultHandleT.getReturnContent();
        		map1.put("breakfast",suppGoodsTimePrice.getBreakfast());
        		Long auditPrice = suppGoodsTimePrice.getGoodsAudltPrice();
        		if(auditPrice==null){
        			auditPrice=0L;
        		}
        		map1.put("auditPriceYuan",PriceUtil.trans2YuanStr(auditPrice));

        		boolean isGroupStock = false;// 是否共享库存
        		Long groupId = suppGoods.getGroupId();
        		if(groupId != null && startDate!=null){
        			//TODO hotel_api
        			ResultHandleT<List<SuppGoodsGroupStock>> suppGoodsGroupStockRHT
        				= suppGoodsGroupStockAdapterClientRemote.selectBySpecDateRangeAndGroupId(groupId, startDate, startDate);
        		if(suppGoodsGroupStockRHT!=null && suppGoodsGroupStockRHT.isSuccess()){
        			List<SuppGoodsGroupStock> suppGoodsGroupStockList = suppGoodsGroupStockRHT.getReturnContent();
	        			if(suppGoodsGroupStockList!=null && suppGoodsGroupStockList.size()>0){
	        				Long stock = suppGoodsGroupStockList.get(0).getStock();
	        				if(stock != null && stock > 0){
	        					stockNum = stock.longValue();
	        				}else{
	        					stockNum = 0L;
	        				}
	        				isGroupStock = true;
 	        			}
        			}
        		}
        		if(!isGroupStock){
	        		String oversellFlag = suppGoodsTimePrice.getOvershellFlag();//是否可超卖
	        		if(StringUtil.isEmptyString(oversellFlag)){//不可超卖
	        			oversellFlag = "N";
	        		}
	        		String freesaleFlag = suppGoodsTimePrice.getFreeSaleFlag();//是否敞开卖
	        		String stockFlag = suppGoodsTimePrice.getStockFlag();
	        		if("Y".equals(oversellFlag) || "Y".equals(freesaleFlag) || "N".equals(stockFlag)){
	        			stockNum = 1000L;// 表示充足
	        		}else if("Y".equals(stockFlag)){
	        			Long stock = suppGoodsTimePrice.getStock();
	        			if(stock == null || stock <=0){
	        				stockNum = 0L;
	        			}else{
	        				stockNum = stock.longValue();
	        				map1.put("mmediate_confirmation", "Y");
	        			}
	        		}
        		}
        		map1.put("stock", getGoodsStockDesc(stockNum.longValue()));
        		map1.put("stockNum", stockNum.intValue());
        	}
        	if(stockNum == 0L){
        		return stockNum;
        	}
        }

//        Map<String,Object> branchQuantityMap = prodProductBranch.getPropValue();
////                        String stayMaxChild = (String)branchQuantityMap.get("stay_max_child");
//        if(prodProductBranch.getMaxVisitor() != null){
//            String stayMaxAudlt = String.valueOf(prodProductBranch.getMaxVisitor());
//            map1.put("capacity",stayMaxAudlt+"人");
//        }

        map1.put("price",suppGoods.getDailyLowestPrice());
        //add by changf on 2018/7/12 携程酒店商品属性
		ResultHandleT<Map<String, Object>> suppGoodsPropResult = suppGoodsBranchPropClientServiceRemote.findSuppGoodsPropByGoodsId(suppGoods.getSuppGoodsId());
		boolean useProductBranchFlag=true;
		Map<String,Object> props=null;
		if(suppGoodsPropResult.isSuccess()){
			Map<String, Object> returnContent = suppGoodsPropResult.getReturnContent();
			if(returnContent!=null){
				useProductBranchFlag=false;
				props=returnContent;
			}
		}
		if(useProductBranchFlag) props=prodProductBranch.getPropValue();
		List<PropValue> propValueList = new ArrayList();
		List<PropValue> bedList=new ArrayList();
		List<PropValue> smoList=new ArrayList();
		List<PropValue> internetList=new ArrayList();
		List<PropValue> windowList=new ArrayList();
		if(!useProductBranchFlag){
			List<com.lvmama.dest.comm.po.prod.PropValue > destpropValueList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("bed_type");
			List<com.lvmama.dest.comm.po.prod.PropValue > destBedList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("add_bed_flag");
			List<com.lvmama.dest.comm.po.prod.PropValue > destSmoList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("smokeless_room");
			List<com.lvmama.dest.comm.po.prod.PropValue > destInternetList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("internet");
			List<com.lvmama.dest.comm.po.prod.PropValue > destWindowList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("window");
			copyListPropValues(destpropValueList,propValueList);
			copyListPropValues(destBedList,bedList);
			copyListPropValues(destSmoList,smoList);
			copyListPropValues(destInternetList,internetList);
			copyListPropValues(destWindowList,windowList);
		}else{
			propValueList = (List<PropValue>)props.get("bed_type");
			bedList = (List<PropValue>)props.get("add_bed_flag");
			smoList = (List<PropValue>)props.get("smokeless_room");
			internetList = (List<PropValue>)props.get("internet");
			windowList = (List<PropValue>)props.get("window");
		}
		//end
        if(propValueList != null && propValueList.size() > 0){
            String bedTypeDesc = propValueList.get(0).getAddValue();
            if(bedTypeDesc != null && !"".equals(bedTypeDesc)){
                map1.put("bed_type",propValueList.get(0).getName()+"("+bedTypeDesc+")");//床型
            }else{
                map1.put("bed_type",propValueList.get(0).getName());//床型
            }
            map1.put("bed_type_no_desc",propValueList.get(0).getName());//床型
        }
        map1.put("area",props.get("area"));
        if(bedList != null && bedList.size() > 0){
        	map1.put("add_bed_flag",bedList.get(0).getName());
        }

        //是否有窗（单酒店）
        if(windowList != null && windowList.size() > 0){
            map1.put("window",windowList.get(0).getName());
        }

        //宽带信息
        if(internetList != null && internetList.size() > 0){
            String internetDesc = internetList.get(0).getAddValue();
            if(internetDesc != null && !"".equals(internetDesc)){
                map1.put("internet_desc",internetDesc);//宽带描述
            }else{

            }
            map1.put("internet_no_desc",internetList.get(0).getName());//宽带显示值 (收费，无，免费)
        }

        //无烟房信息
        if(smoList != null && smoList.size() > 0){
            map1.put("smokeless_room",smoList.get(0).getName());
        }

        //最大可住人数
		if(useProductBranchFlag) {
			Long maxVisitor = prodProductBranch.getMaxVisitor();
			map1.put("maxVisitor", maxVisitor);
		}else{
			map1.put("maxVisitor", props.get("max_occupancy"));
		}
        //楼层信息
        map1.put("floor",props.get("floor"));
        //生成可选间数的select的组装数据
        Map<String, Long> selectPriceMap = suppGoods.getSelectPriceMap();
        if(selectPriceMap != null  && selectPriceMap.size() > 0){
            map1.put("selectPriceMap",selectPriceMap);//可选择的房间数量-价格
            List<String> selectNumList = new ArrayList<String>();
            if(selectPriceMap != null  && selectPriceMap.size() > 0){
            	Object[] key = selectPriceMap.keySet().toArray();
            	long[] keys = new long[key.length];
            	for(int i = 0;i < key.length;i++){
            		keys[i] = Integer.parseInt(key[i]+"");
            	}
		        Arrays.sort(keys);
		        for(int i = 0;i < keys.length;i++){
		        	if(keys[i] <= stockNum.longValue()){
		        		selectNumList.add(keys[i] + "");
		        	}
		        }
		        map1.put("defaultQuantity",keys[0]);
		        map1.put("priceYuanFirst",PriceUtil.trans2YuanStr(selectPriceMap.get(String.valueOf(keys[0]))));
		        map1.put("priceYuan",PriceUtil.trans2YuanStr(selectPriceMap.get(String.valueOf(keys[0]))/keys[0]));
            }
            map1.put("selectNumList",selectNumList);//可选择的房间数列表
        }
        return stockNum;
    }

  //获取库存的描述
    private String getGoodsStockDesc(Long stock) {
        if(stock == null || stock <= 0) {
            return "余0间";
        }else if(stock >= 10) {
            return "充足";
        }else{
            return "余" + stock + "间";
        }
    }

    /**
     * 酒店入住年、月、日、星期
     * @param map
     * @param checkInDateDate
     * @param checkOutDateDate
     */
    private void fillCheckInAndOutDate(Map map, Date checkInDateDate, Date checkOutDateDate){
		Calendar checkInCalendar = Calendar.getInstance();
		checkInCalendar.setTime(checkInDateDate);
		map.put("check_in_year", checkInCalendar.get(Calendar.YEAR));
		map.put("check_in_week", WEEK_ARRAY[checkInCalendar.get(Calendar.DAY_OF_WEEK)-1]);
		map.put("check_in_month", checkInCalendar.get(Calendar.MONTH) + 1);
		map.put("check_in_day", checkInCalendar.get(Calendar.DAY_OF_MONTH));

		Calendar checkOutCalendar = Calendar.getInstance();
		checkOutCalendar.setTime(checkOutDateDate);
		map.put("check_out_year", checkOutCalendar.get(Calendar.YEAR));
		map.put("check_out_week", WEEK_ARRAY[checkOutCalendar.get(Calendar.DAY_OF_WEEK)-1]);
		map.put("check_out_month", checkOutCalendar.get(Calendar.MONTH) + 1);
		map.put("check_out_day", checkOutCalendar.get(Calendar.DAY_OF_MONTH));

	}

    /**
     * 验证期票有效期,验证信息放入model中 lineTicketValidMsgMap
     *
     * @param specDate
     * @param ticketProdPackageList
     */
    private Map<String, String> getAperLineTicketValidMsg(Date specDate, List<ProdPackageGroup> ticketProdPackageList) {
        Map<String, String> lineTicketValidMsgMap = new HashMap<>();

        if (CollectionUtils.isEmpty(ticketProdPackageList)) {
            return lineTicketValidMsgMap;
        }

        Date currentDate = new Date();

        for (ProdPackageGroup prodPackageGroup : ticketProdPackageList) {
            List<ProdPackageDetail> packDetailList = prodPackageGroup.getProdPackageDetails();
            if (CollectionUtils.isEmpty(packDetailList)) {
                continue;
            }

            ProdPackageGroupTicket prodPackageGroupTicket = prodPackageGroup.getProdPackageGroupTicket();
            if (prodPackageGroupTicket == null) {
                continue;
            }

            String dayStr = prodPackageGroupTicket.getStartDay();
            String dayArray[] = null;
            if (dayStr != null && !"".equals(dayStr)) {
                dayArray = dayStr.split(",");
            }

            for (ProdPackageDetail prodPackageDetail : packDetailList) {
                ProdProductBranch productBranch = prodPackageDetail.getProdProductBranch();
                if (productBranch == null) {
                    continue;
                }
                List<SuppGoods> goodsList = productBranch.getSuppGoodsList();
                if (CollectionUtils.isEmpty(goodsList)) {
                    continue;
                }

                //构建检测批量查询参数LIST
                List<Map<String, Object>> queryList = new ArrayList<>();
                for (SuppGoods suppGoods : goodsList) {
                    if (Constants.Y_FLAG.equalsIgnoreCase(suppGoods.getAperiodicFlag())) {
                        queryList.add(getAperMsgQueryMap(specDate, dayArray, currentDate, suppGoods));
                    }
                }

                Map<String, String> msgMap = new HashMap<>();
                ResultHandleT<Map<String, String>> result = prodCalClientRemote.checkLineAperTicketDate(queryList);
                if (result != null && result.getReturnContent() != null && result.getMsg() == null) {
                    msgMap = result.getReturnContent();
                }

                for (SuppGoods suppGoods : goodsList){
                    String msg = msgMap.get(suppGoods.getSuppGoodsId().toString());
                    if (null != msg) {
                        StringBuffer keyBuffer = new StringBuffer();
                        keyBuffer.append(prodPackageGroup.getGroupId()).append("_")
                                .append(productBranch.getProductBranchId()).append("_")
                                .append(suppGoods.getSuppGoodsId().toString());
                        lineTicketValidMsgMap.put(keyBuffer.toString(), msg);
                    }
                }
            }
        }

        return lineTicketValidMsgMap;
    }

    /**
     * 封装查询Map
     * @param selectDate
     * @param dayArray
     * @param currentDate
     * @param suppGoods
     * @return
     */
    private Map<String, Object> getAperMsgQueryMap(Date selectDate, String[] dayArray, Date currentDate, SuppGoods suppGoods) {
        Map<String,Object> map = new HashMap<>();
        map.put("selectDate",selectDate);
        map.put("dayArray",dayArray);
        map.put("suppGoodsExp",suppGoods.getSuppGoodsExp());
		map.put("suppGoodsName",suppGoods.getGoodsName());
        map.put("orderTime", currentDate);
        return map;
    }

    /**
	 * 设置一键下单门票出游日期信息
	 * @param model
	 * @param ticketProdPackageList
	 * @param order
	 */
	private void setOrderTicketSuppGoods(ModelMap model, List<ProdPackageGroup> ticketProdPackageList, OrdOrder order) {
		if(order==null || CollectionUtils.isEmpty(ticketProdPackageList) || CollectionUtils.isEmpty(order.getOrderItemList())){
			return;
		}
		Map<String,String> ticketDateMap=new HashMap<String, String>();
		for (ProdPackageGroup prodPackageGroup : ticketProdPackageList) {
			List<ProdPackageDetail> packDetailList=prodPackageGroup.getProdPackageDetails();
			if(packDetailList!=null){
				for (ProdPackageDetail prodPackageDetail : packDetailList) {
					ProdProductBranch productBranch=prodPackageDetail.getProdProductBranch();
					if(productBranch!=null){
						List<SuppGoods> goodsList=productBranch.getSuppGoodsList();
						if(CollectionUtils.isNotEmpty(goodsList)){
							for (SuppGoods suppGoods : goodsList) {
								OrdOrderItem orderItem=findOrdOrderItemBySuppGoodsId(order, suppGoods.getSuppGoodsId());
								if(orderItem!=null){
									String visitTime=DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd");
									ticketDateMap.put(suppGoods.getSuppGoodsId()+"", visitTime);
								}
							}
						}
					}
				}
			}
		}
		if(ticketDateMap.size()>0){
			model.put("oneKeyOrderTicketDateMap", ticketDateMap);
		}
	}

	/**
	 * 处理一键下单商品变动提醒信息
	 * @param model
	 * @param map
	 * @param changedGoods 变动的商品信息
	 * @param insuranceList 保险
	 * @param additionList 附加信息
	 */
    private void processChangedGoods(ModelMap model, Map<String, List<ProdProductBranch>> map,
            List<SuppGoodsItem> changedGoods, List<SuppGoodsSaleRe> insuranceList,
            List<ProdProductBranch> additionList) {

        if (CollectionUtils.isNotEmpty(changedGoods)) {
            Iterator<SuppGoodsItem> changedGoodsIter = changedGoods.iterator();

            OUTER: while (changedGoodsIter.hasNext()) {
                SuppGoodsItem changedGood = changedGoodsIter.next();
                Long changedGoodId = changedGood.getOrderSuppGoodId();
                Collection<List<ProdProductBranch>> prodCollection = map.values();

                for (List<ProdProductBranch> prodList : prodCollection) {
                    for (ProdProductBranch prodProductBranch : prodList) {
                        List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();

                        if (CollectionUtils.isNotEmpty(suppGoodsList)) {
                            for (SuppGoods suppGoods : suppGoodsList) {
                                if (suppGoods.getSuppGoodsId() != null
                                        && suppGoods.getSuppGoodsId().longValue() == changedGoodId.longValue()) {
                                    changedGoodsIter.remove();
                                    continue OUTER;
                                }
                            }
                        }
                    }
                }

                // 处理保险
                for (SuppGoodsSaleRe insurance : insuranceList) {
                    List<SuppGoods> goodList = insurance.getInsSuppGoodsList();
                    if (CollectionUtils.isNotEmpty(goodList)) {
                        for (SuppGoods suppGoods : goodList) {
                            if (suppGoods.getSuppGoodsId() != null
                                    && suppGoods.getSuppGoodsId().longValue() == changedGoodId.longValue()) {
                                changedGoodsIter.remove();
                                continue OUTER;
                            }
                        }
                    }
                }

                //处理附加，由于附加在一些特殊的情况下不在map中
                if (CollectionUtils.isNotEmpty(additionList)) {
                    for (ProdProductBranch addition : additionList) {
                        List<SuppGoods> goodList = addition.getSuppGoodsList();
                        if (CollectionUtils.isNotEmpty(goodList)) {
                            for (SuppGoods suppGoods : goodList) {
                                if (suppGoods.getSuppGoodsId() != null
                                        && suppGoods.getSuppGoodsId().longValue() == changedGoodId.longValue()) {
                                    changedGoodsIter.remove();
                                    continue OUTER;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 数据传入FREEMARKER
        if (CollectionUtils.isNotEmpty(changedGoods)) {
            model.addAttribute("unRecommendSuppGoods", JSON.toJSONString(changedGoods));
        }
    }

    /**
     * 根据订单获取各个品类的子订单
     * @param order 订单对象
     * @return Map<品类, List<OrdOrderItem>>
     */
    private Map<String, List<OrdOrderItem>> getOrdOrderItems(OrdOrder order){
    	Map<String, List<OrdOrderItem>> resultMap = new HashMap<String, List<OrdOrderItem>>();

		List<OrdOrderItem> updateOrderItemList = new ArrayList<OrdOrderItem>();
		List<OrdOrderItem> changeOrderItemList = new ArrayList<OrdOrderItem>();
		// 自主打包产品信息（一个组对应一个行程，一个组下有多个规格)
		List<OrdOrderItem> hotelOrderItemList = null;
		List<OrdOrderItem> ticketOrderItemList = null;
		List<OrdOrderItem> transprotOrderItemList = null;
		// 线路
		List<OrdOrderItem> hotelCombOrderItemList = null;
		List<OrdOrderItem> groupOrderItemList = null;
		List<OrdOrderItem> freedomOrderItemList = null;
		List<OrdOrderItem> localOrderItemList = null;

    	List<OrdOrderItem> orderItemList = order.getOrderItemList();
    	if (CollectionUtils.isNotEmpty(orderItemList)) {
			//遍历订单的每个子订单
			for(OrdOrderItem orderItem: orderItemList){
				if(orderItem == null || orderItem.getBranchId() == null){
					continue;
				}
				if(orderItem.getCategoryId() != null) {
					String categoryId = orderItem.getCategoryId().toString();
					if(categoryId != null){
						if(Constant.VST_CATEGORY.CATEGORY_ROUTE_HOTELCOMB.getCategoryId().equalsIgnoreCase(categoryId)){
							//酒店套餐
							if(hotelCombOrderItemList == null){
								hotelCombOrderItemList = new ArrayList<OrdOrderItem>();
							}
							hotelCombOrderItemList.add(orderItem);
						}else if(Constant.VST_CATEGORY.CATEGORY_ROUTE_GROUP.getCategoryId().equalsIgnoreCase(categoryId)){
							//如果是upgrad或changed_hotel，设置updateProdPackageList或changeProdPackageList
							if(!isUpgradOrChangeGroup(orderItem, updateOrderItemList, changeOrderItemList)){
								if(groupOrderItemList == null){
									groupOrderItemList = new ArrayList<OrdOrderItem>();
								}
								//如果不是upgrad或changed_hotel，设置groupPackageList
								groupOrderItemList.add(orderItem);
							}
						}else if(Constant.VST_CATEGORY.CATEGORY_ROUTE_FREEDOM.getCategoryId().equalsIgnoreCase(categoryId)){
							//如果是upgrad或changed_hotel，设置updateProdPackageList或changeProdPackageList
							if(!isUpgradOrChangeGroup(orderItem, updateOrderItemList, changeOrderItemList)){
								if(freedomOrderItemList == null){
									freedomOrderItemList = new ArrayList<OrdOrderItem>();
								}
								//如果不是upgrad或changed_hotel，设置freedomPackageList
								freedomOrderItemList.add(orderItem);
							}
						}else if(Constant.VST_CATEGORY.CATEGORY_ROUTE_LOCAL.getCategoryId().equalsIgnoreCase(categoryId)){
							//当地游
							if(localOrderItemList == null){
								localOrderItemList = new ArrayList<OrdOrderItem>();
							}
							localOrderItemList.add(orderItem);
						}
						else if(Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId().equalsIgnoreCase(categoryId)){
							//酒店
							if(hotelOrderItemList == null){
								hotelOrderItemList = new ArrayList<OrdOrderItem>();
							}
							hotelOrderItemList.add(orderItem);
						}
						else if(Constant.VST_CATEGORY.CATEGORY_SINGLE_TICKET.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_OTHER_TICKET.getCategoryId().equalsIgnoreCase(categoryId)){
							//门票
							if(ticketOrderItemList == null){
								ticketOrderItemList = new ArrayList<OrdOrderItem>();
							}
							ticketOrderItemList.add(orderItem);
						}
						else if(Constant.VST_CATEGORY.CATEGORY_TRANFFIC_AEROPLANE_OTHER.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_AEROPLANE.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_TRAIN.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_TRAIN_OTHER.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_BUS.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_BUS_OTHER.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_SHIP.getCategoryId().equalsIgnoreCase(categoryId)
								|| Constant.VST_CATEGORY.CATEGORY_TRANFFIC_SHIP_OTHER.getCategoryId().equalsIgnoreCase(categoryId)){
							//交通
							if(transprotOrderItemList == null){
								transprotOrderItemList = new ArrayList<OrdOrderItem>();
							}
							transprotOrderItemList.add(orderItem);
						}
					}
				}

			}
    	}
    	resultMap.put("update", updateOrderItemList);
    	resultMap.put("change", changeOrderItemList);
    	resultMap.put("hotel", hotelOrderItemList);
    	resultMap.put("ticket", ticketOrderItemList);
    	resultMap.put("transprot", transprotOrderItemList);
    	resultMap.put("hotelComb", hotelCombOrderItemList);
    	resultMap.put("group", groupOrderItemList);
    	resultMap.put("freedom", freedomOrderItemList);
    	resultMap.put("local", localOrderItemList);
    	return resultMap;
    }

    /**
     * 检查子订单是否是upgrad或changed_hotel，如果是把子订单存放到updateProdPackageList或changeProdPackageList
     * @param orderItem
     * @param updateProdPackageList
     * @param changeProdPackageList
     * @return true：是upgrad或changed_hotel，false：不是upgrad或changed_hotel
     */
    private boolean isUpgradOrChangeGroup(OrdOrderItem orderItem, List<OrdOrderItem> updateProdPackageList,
    		List<OrdOrderItem> changeProdPackageList){
    	boolean result = false;
	    try {
			ProdProductBranch productBranch = prodBranchClientService.findProdProductBranchById(orderItem.getBranchId()).getReturnContent();
			if(null != productBranch){
				Long branchId = productBranch.getBranchId();
				BizBranch bizBranch = branchClientRemote.findBranchById(branchId).getReturnContent();
				//设置产品规格
			    if(null != bizBranch){
					if("upgrad".equalsIgnoreCase(bizBranch.getBranchCode())){
						result = true;
						updateProdPackageList.add(orderItem);
					} else if("changed_hotel".equalsIgnoreCase(bizBranch.getBranchCode())){
						result = true;
						changeProdPackageList.add(orderItem);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("OrderLineProductQueryAction.getOrdOrderItems() error", e);
		}
	    return result;
    }

    /**
	 * 在一键下单时，把子订单中的商品设置为各个品类的推荐商品。会改变packGroupList的recommendSuppGoodsList结构。
	 * 如果在packGroupList没找到对应的订单商品，把订单商品加入到unRecommendItemSuppGoodsList，用于页面提示。
	 * @param unRecommendItemSuppGoodsList 在packGroupList没找到对应的订单商品列表
	 * @param packGroupList ProdPackageGroup列表
	 * @param orderItemList 相应品类的子订单列表
	 */
	private void setOrderSuppGoods(List<SuppGoodsItem> unRecommendItemSuppGoodsList,
			List<ProdPackageGroup> packGroupList, List<OrdOrderItem> orderItemList){
		setOrderSuppGoods(unRecommendItemSuppGoodsList, packGroupList, orderItemList,
				null, null);
	}

	/**
	 * 在一键下单时，把子订单中的商品设置为各个品类的推荐商品。会改变packGroupList的recommendSuppGoodsList结构。
	 * 如果在packGroupList没找到对应的订单商品，把订单商品加入到unRecommendItemSuppGoodsList，用于页面提示。
	 * @param unRecommendItemSuppGoodsList 在packGroupList没找到对应的订单商品列表
	 * @param packGroupList ProdPackageGroup列表
	 * @param orderItemList 相应品类的子订单列表
	 */
	private void setOrderSuppGoods(List<SuppGoodsItem> unRecommendItemSuppGoodsList,
			List<ProdPackageGroup> packGroupList, List<OrdOrderItem> orderItemList,
			ModelMap model, String type){
		if (CollectionUtils.isEmpty(orderItemList) || CollectionUtils.isEmpty(packGroupList)){
			return;
		}
		Map<Long, String> matchedSuppGoods = new HashMap<Long, String>();
		for(ProdPackageGroup group : packGroupList){
			List<SuppGoods> orderSuppGoodsList = new ArrayList<SuppGoods>();
			List<ProdPackageDetail> packageDetailList = group.getProdPackageDetails();
			if(CollectionUtils.isNotEmpty(packageDetailList)){
				for(int index = 0;index<packageDetailList.size();index++){
					ProdPackageDetail detail = packageDetailList.get(index);
					if(detail == null)
						continue;
					ProdProductBranch prodProductBranch = detail.getProdProductBranch();
					if(prodProductBranch == null)
						continue;
					List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
					//List<SuppGoods> recommendSuppGoodsList = prodProductBranch.getRecommendSuppGoodsList();
					//每个品类只有第一个推荐有用
					if(CollectionUtils.isNotEmpty(suppGoodsList)){
						for(SuppGoods suppGoods : suppGoodsList){
							//遍历子订单的商品号，对每个子订单的商品，把container中对应的规格中的商品写成推荐商品
							for(OrdOrderItem orderItem: orderItemList){
								if(orderItem == null || orderItem.getSuppGoodsId() == null)
									continue;
								Long itemSuppGoodsId = orderItem.getSuppGoodsId();
								if(suppGoods != null & suppGoods.getSuppGoodsId() != null &&
										suppGoods.getSuppGoodsId().longValue() == itemSuppGoodsId.longValue()){
									matchedSuppGoods.put(itemSuppGoodsId, "");
									orderSuppGoodsList.add(suppGoods);
									prodProductBranch.setRecommendSuppGoodsList(orderSuppGoodsList);
									//把匹配的ProdPackageDetail放到第一个位置。
									if(index != 0){
										packageDetailList.remove(index);
										packageDetailList.add(0, detail);
									}
									if("update".equalsIgnoreCase(type) && model != null){
										model.put("oneKeyOrderUpdate", "true");
									}
									//每个打包中不会出现相同的商品，跳出ProdPackageGroup循环。
									//break outerLoop;
								}
							}
						}
						//把订单的SuppGoods放在suppGoodsList的前面位置
						if(CollectionUtils.isNotEmpty(orderSuppGoodsList)){
							suppGoodsList.removeAll(orderSuppGoodsList);
							suppGoodsList.addAll(0, orderSuppGoodsList);
						}
					}
				}
			}
		}
		//订单中有，但加载出来的商品中没有的商品Id, 存入到unRecommendItemSuppGoodsList，前端页面提示给用户。
		if(matchedSuppGoods.size() < orderItemList.size()){  //部分商品没有匹配上
			for(OrdOrderItem orderItem: orderItemList){
				Long itemSuppGoodsId = orderItem.getSuppGoodsId();
				if(!matchedSuppGoods.containsKey(itemSuppGoodsId)){  //该商品没有匹配上
					LOG.warn("一键下单时，原订单中有（但加载出来的商品中没有）商品Id:" + itemSuppGoodsId);
					SuppGoods suppGoods = getCategoryName(itemSuppGoodsId);
					if(suppGoods != null){
						String categoryName = "";
						if(suppGoods.getProdProduct() != null){
							Long categoryId = suppGoods.getProdProduct().getBizCategoryId();
							if(categoryId != null)
								categoryName = BizEnum.BIZ_CATEGORY_TYPE.getCnName(categoryId);
						}
						SuppGoodsItem item = new SuppGoodsItem();
						item.setCategoryName(categoryName);
						item.setOrderSuppGoodsName(suppGoods.getGoodsName());
						item.setOrderSuppGoodId(itemSuppGoodsId);
						unRecommendItemSuppGoodsList.add(item);
					}
				}
			}
		}
	}

	/**
	 * 根据商品ID获取SuppGoods对象
	 * @param suppGoodsId
	 * @return
	 */
	private SuppGoods getCategoryName(Long suppGoodsId){
		SuppGoods suppGoods = null;
		try {
			SuppGoodsParam param = new SuppGoodsParam();
			param.setProduct(true);
			suppGoods = suppGoodsClientService.findSuppGoodsById(suppGoodsId, param).getReturnContent();
		} catch (Exception e) {
			LOG.error("getCategoryName error.", e);
		}
		return suppGoods;
	}

	class SuppGoodsItem{
		private String categoryName;
		private String orderSuppGoodsName;
	    private Long orderSuppGoodId;


		public SuppGoodsItem(){
		}
		public String getOrderSuppGoodsName() {
			return orderSuppGoodsName;
		}
		public void setOrderSuppGoodsName(String orderSuppGoodsName) {
			this.orderSuppGoodsName = orderSuppGoodsName;
		}
		public String getCategoryName() {
			return categoryName;
		}
		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}
        public Long getOrderSuppGoodId() {
            return orderSuppGoodId;
        }
        public void setOrderSuppGoodId(Long orderSuppGoodId) {
            this.orderSuppGoodId = orderSuppGoodId;
        }

	}

	private List<Map<String, Object>> buildRelSaleLocalDetailInfo(List<SuppGoodsSaleRe> suppGoodsSaleReList, ProdProduct product,Date visitDate,OrdOrder order) {
    	if(suppGoodsSaleReList == null || suppGoodsSaleReList.size() <= 0) {
    		return null;
    	}
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	Map<String, Object> productMap = null;//产品详细信息，元素Map的key为产品ID，value为该产品下的商品列表
    	List<Map<String, Object>> goodsList = null; //商品列表
    	Map<String, Object> suppGoodsDetailMap = null;//商品详细信息

    	Map<String,String> auditPriceMap = null;
    	Map<String,String> childPriceMap = null;
    	Map<String,String> gapPriceMap = null;
    	List<String> selectDateList = null;

    	String selectAdultQuantityRange = null;
    	String selectChildQuantityRange = null;
    	String selectGapQuantityRange = null;

    	goodsList = new ArrayList<Map<String,Object>>();

        for(SuppGoodsSaleRe suppGoodsSaleRe : suppGoodsSaleReList){
            ProdProductBranch prodProductBranch = suppGoodsSaleRe.getReProductBranch();
            if(prodProductBranch == null){
                continue;
            }
            //判断商品的品类
            BizCategory bizCategory = prodProductBranch.getProduct().getBizCategory();
            //用来判断是否是当地游
            Long categoryId = bizCategory.getCategoryId();
            //只处理当地游
			if (!BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)) {
				continue;
			}


            suppGoodsDetailMap = new HashMap<String, Object>();

            List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();//推荐商品
            SuppGoods suppGoods = suppGoodsList.get(0);
            suppGoodsDetailMap.put("suppGoods",suppGoods);
        	suppGoodsDetailMap.put("productId",prodProductBranch.getProduct().getProductId());
        	suppGoodsDetailMap.put("productName",prodProductBranch.getProduct().getProductName());

        	Map<String,SuppGoodsLineTimePriceVo> localSelectPriceMap = suppGoods.getGoodsLocalSelectPriceMap();//获取时间价格表

        	selectDateList = new ArrayList<String>();//生成可选select
        	auditPriceMap = new  TreeMap<String, String>();//成人价map
        	childPriceMap = new  TreeMap<String, String>();//儿童价map
        	gapPriceMap   = new  TreeMap<String, String>();//儿童价map

        	selectAdultQuantityRange = suppGoods.getSelectAdultQuantityRange();//成人数量select
        	selectChildQuantityRange = suppGoods.getSelectChildQuantityRange();//儿童数量select
        	selectGapQuantityRange = suppGoods.getSelectGapQuantityRange();//房差数量select

        	SuppGoodsLineTimePriceVo suppGoodsLineTimePriceVo = new SuppGoodsLineTimePriceVo();
            DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance();
            df.applyPattern("0.00");
        	if(localSelectPriceMap != null  && localSelectPriceMap.size() > 0){
        		Set<String> keys = localSelectPriceMap.keySet();
        		for(String key :keys){
        			selectDateList.add(key);//添加日期
        			suppGoodsLineTimePriceVo = localSelectPriceMap.get(key);//获取具体某一天的时间价格表

        			if(suppGoodsLineTimePriceVo.getAuditPrice() != null){
        				auditPriceMap.put(key,df.format(suppGoodsLineTimePriceVo.getAuditPrice()/100.00));
        			}
        			if(suppGoodsLineTimePriceVo.getChildPrice() != null){
        				childPriceMap.put(key,df.format(suppGoodsLineTimePriceVo.getChildPrice()/100.00));
        			}
        			if(suppGoodsLineTimePriceVo.getGapPrice() != null){
        				gapPriceMap.put(key,df.format(suppGoodsLineTimePriceVo.getGapPrice()/100.00));
        			}
        		}
        	}

        	//关联销售当地游增加出游时间限制
        	filterOptinalRouteLocal(product, selectDateList, null, suppGoodsSaleRe.getLimitDays(), visitDate);
        	if(selectDateList.isEmpty()) {
        		continue;
        	}
        	suppGoodsDetailMap.put("selectDateList", selectDateList);
        	//一键重下重设默认值
        	if(order!=null){
        		OrdOrderItem ordOrderItem=findRelationOrdOrderItemBySuppGoodsId(order, suppGoods.getSuppGoodsId());
        		if(ordOrderItem!=null){
        			String visitTime=DateFormatUtils.format(ordOrderItem.getVisitTime(), "yyyy-MM-dd");
        			suppGoodsDetailMap.put("selectDefaultDate", visitTime);
					suppGoodsDetailMap.put("defaultAuditQuantity", ordOrderItem.getAdultQuantity());
					suppGoodsDetailMap.put("defaultChildQuantity", ordOrderItem.getChildQuantity());
					suppGoodsDetailMap.put("defaultGapQuantity", ordOrderItem.getSpreadQuantity());
        		}
        	}

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
	 * 将非对接机票转换为对接机票格式的数据
	 * @param transprotProdPackageList
	 */
	private void jipiaoChangeDuijie(List<ProdPackageGroup> transprotProdPackageList) throws Exception{
		Long tempCategary = 21L;
		if(transprotProdPackageList!=null && transprotProdPackageList.size()>0 ){
			//只有所有的group 都是其他机票时才能转对接机票
			boolean allPlaneTicket = true;
			for (ProdPackageGroup packageGroup : transprotProdPackageList) {
				if (packageGroup.getProdPackageDetails() != null
						&& !packageGroup.getProdPackageDetails().isEmpty()
						&& packageGroup.getProdPackageDetails().get(0).getProdProduct() != null
						&& !tempCategary.equals(packageGroup.getProdPackageDetails().get(0).getProdProduct()
								.getBizCategoryId())) {
					allPlaneTicket = false;
					break;
				}
			}
			if (!allPlaneTicket) {
				return;
			}

			for (ProdPackageGroup packageGroup : transprotProdPackageList) {
				if("Y".equals(packageGroup.getJiPiaoDuiJieFlag())){
					continue;
				}
				if(packageGroup.getProdPackageGroupTransport()!=null && StringUtils.equals(packageGroup.getProdPackageGroupTransport().getTransportType(), "TOBACK")){
					continue;
				}
				packageGroup.setJiPiaoDuiJieFlag("Y");
				List<ProdPackageDetail>details = packageGroup.getProdPackageDetails();
				if(details!=null){
					for (ProdPackageDetail detail : details) {
						FlightNoVo flightNoVo = new FlightNoVo();
						if(detail!=null && detail.getProdProductBranch()!=null && detail.getProdProductBranch().getSuppGoodsList()!=null){
							Map<String, FlightNoVo> suppGoodsMap = new HashMap<String, FlightNoVo>();
							for (SuppGoods goods : detail.getProdProductBranch().getSuppGoodsList()) {
								suppGoodsMap.put(goods.getSuppGoodsId()+"", flightNoVo);//组装详情页suppGoodsMap
								detail.getProdProductBranch().setSuppGoodsMap(suppGoodsMap);
							}
						}

						if(detail.getProdProduct()!=null &&
								detail.getProdProduct().getProdTrafficVO()!=null){
							List<ProdTrafficGroup> prodTrafficGrouptList = detail.getProdProduct().getProdTrafficVO().getProdTrafficGroupList();
							if(prodTrafficGrouptList != null){
								for (ProdTrafficGroup prodTrafficGroup : prodTrafficGrouptList) {
									List<ProdTrafficFlight> pfs = prodTrafficGroup.getProdTrafficFlightList();
									if(pfs!=null){
										for (ProdTrafficFlight prodTrafficFlight : pfs) {
											BizFlight bizFlight = prodTrafficFlight.getBizFlight();
											if(bizFlight!=null){

												flightNoVo = orderLineProductQueryUtil.fillFlightNoVo(flightNoVo, bizFlight, prodTrafficFlight, detail.getProdProductBranch());

												SuppGoodsBaseTimePrice baseTimePrice = detail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice();
												SuppGoodsLineTimePriceVo lineTimePrice = (SuppGoodsLineTimePriceVo) baseTimePrice;
												if(lineTimePrice!=null &&
														(SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_NO_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())
														|| ("Y".equalsIgnoreCase(lineTimePrice.getOversellFlag())
																&& (lineTimePrice.getStock() == 0 || lineTimePrice.getStock() == null)))){
													flightNoVo.setRemain(-1l);//剩余数
												}else{
													flightNoVo.setRemain(detail.getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsBaseTimePrice().getStock());//剩余数
												}
												flightNoVo.setFoodSupport(false);//餐食
												flightNoVo.setAdultAmt(detail.getProdProductBranch().getAdultPrice());//成人价
												flightNoVo.setChildAmt(detail.getProdProductBranch().getChildPrice());//儿童价
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
	 * 交通类型为其他机票时处理价格，商品ID等数据
	 * @param transprotProdPackageList
	 */
	private void dealJipiaoData(ModelMap model,List<ProdPackageGroup> transprotProdPackageList,String productId,boolean isDuijie) throws Exception{
		Long tempCategary = 21l;
		//如果品类是其他机票则处理数据，否则直接结束本方法
		if(CollectionUtils.isNotEmpty(transprotProdPackageList) &&
				CollectionUtils.isNotEmpty(transprotProdPackageList.get(0).getProdPackageDetails())&&
				transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch()!=null &&
				transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch()!=null){

			//只有所有的group 都是其他机票时才能转对接机票
			boolean allPlaneTicket = true;
			for (ProdPackageGroup packageGroup : transprotProdPackageList) {
				if (packageGroup.getProdPackageDetails() != null
						&& !packageGroup.getProdPackageDetails().isEmpty()
						&& packageGroup.getProdPackageDetails().get(0).getProdProduct() != null
						&& !tempCategary.equals(packageGroup.getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())) {
					allPlaneTicket = false;
					break;
				}
			}

			if (!allPlaneTicket) {
				//BUG#96822 打包对接机票和其他交通，后台下单的时候，对接机票有数据，但是后台更换不了
				if(isDuijie){
					FlightParam toFlightParam = new FlightParam();
					FlightParam backFlightParam = new FlightParam();
					boolean isTo = false;
					boolean isBack = false;
					for (ProdPackageGroup prodPackageGroup : transprotProdPackageList) {
						if (prodPackageGroup.getProdPackageDetails() != null
								&& !prodPackageGroup.getProdPackageDetails().isEmpty()
								&& prodPackageGroup.getProdPackageDetails().get(0).getProdProduct() != null
								&& tempCategary.equals(prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())) {
							if(prodPackageGroup.getProdPackageGroupTransport()!=null &&
									prodPackageGroup.getProdPackageGroupTransport().getTransportType()!=null&&
									"TO".equals(prodPackageGroup.getProdPackageGroupTransport().getTransportType())){
								List<ProdPackageDetail> details = prodPackageGroup.getProdPackageDetails();
								if(CollectionUtils.isNotEmpty(details)){
									LOG.info(productId + " havechangeButton:组【"+prodPackageGroup.getGroupId()+"】下details.size()【"+details.size()+"】");
									if(details.size()>1){
										model.addAttribute("flightMoreGoods", "Y");
										model.addAttribute("onlyOneDuiJieJiPiao", "Y");
									}else{
										for (ProdPackageDetail ppd : details) {
											ProdProductBranch branch =  ppd.getProdProductBranch();
											Map<String, FlightNoVo> suppGoodsMap = null;
											if(branch!=null){
												suppGoodsMap = branch.getSuppGoodsMap();
												int size = suppGoodsMap==null?0:suppGoodsMap.size();
												LOG.info(productId + " havechangeButton:组【"+prodPackageGroup.getGroupId()+
														"】下detail【"+ppd.getDetailId()+"】下branch【"+branch.getBranchId()+
														"】下suppGoodsMap.size()【"+size+"】");

												if(suppGoodsMap!=null && suppGoodsMap.size()>1){
													model.addAttribute("flightMoreGoods", "Y");
													model.addAttribute("onlyOneDuiJieJiPiao", "Y");
												}
											}
										}
									}
								}
								ProdPackageDetail detail = prodPackageGroup.getProdPackageDetails().get(0);
								ProdPackageGroupTransport transport =  prodPackageGroup.getProdPackageGroupTransport();
								LOG.info("productId["+ productId +
										"] ProdPackageGroupTransport.toString()【"+ transport.toString() + "】");
								if(isToType(transport) ){
									Long suppId = prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
									FlightNoVo goods = prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId+"");
									toFlightParam.setAdultPrice(goods.getAdultAmt());
									toFlightParam.setChildPrice(goods.getChildAmt());
									toFlightParam.setPackageGroupId(detail.getGroupId());
									toFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
									toFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
									toFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
									isTo = true;
								}
								if(isBackType(transport)){
									Long suppId = prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
									FlightNoVo goods = prodPackageGroup.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId+"");
									backFlightParam.setAdultPrice(goods.getAdultAmt());
									backFlightParam.setChildPrice(goods.getChildAmt());
									backFlightParam.setPackageGroupId(detail.getGroupId());
									backFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
									backFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
									backFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
									isBack = true;
								}
							}
						}
					}
					LOG.info("productId["+ productId +"],toFlightParam== " + toFlightParam.toString()+
							"\n backFlightParam== " + backFlightParam.toString());
					if(isTo){model.addAttribute("toFlightParam", toFlightParam);}
					if(isBack){model.addAttribute("backFlightParam", backFlightParam);}

				}
				return;
			}

			model.addAttribute("isFlight","true");

			//往返程还是单程标识
			String flag = null;
			for (ProdPackageGroup prodPackageGroup : transprotProdPackageList) {
				List<ProdPackageDetail> details = prodPackageGroup.getProdPackageDetails();
				if(CollectionUtils.isNotEmpty(details)){
					LOG.info(productId + " havechangeButton:组【"+prodPackageGroup.getGroupId()+"】下details.size()【"+details.size()+"】");
					if(details.size()>1){
						model.addAttribute("flightMoreGoods", "Y");
					}else{
						for (ProdPackageDetail ppd : details) {
							ProdProductBranch branch =  ppd.getProdProductBranch();
							Map<String, FlightNoVo> suppGoodsMap = null;
							if(branch!=null){
								int size = suppGoodsMap==null?0:suppGoodsMap.size();
								LOG.info(productId + " havechangeButton:组【"+prodPackageGroup.getGroupId()+
										"】下detail【"+ppd.getDetailId()+"】下branch【"+branch.getBranchId()+
										"】下suppGoodsMap.size()【"+size+"】");

								suppGoodsMap = branch.getSuppGoodsMap();
								if(suppGoodsMap!=null && suppGoodsMap.size()>1){
									model.addAttribute("flightMoreGoods", "Y");
								}
							}
						}
					}
				}

				if(prodPackageGroup.getProdPackageGroupTransport()!=null &&
						prodPackageGroup.getProdPackageGroupTransport().getTransportType()!=null){
					flag = prodPackageGroup.getProdPackageGroupTransport().getTransportType();
				}
			}

			model.addAttribute("transportType", flag);
			if("TOBACK".equals(flag)){//往返程，价格取其一半
				long price = transprotProdPackageList.get(0).
						getProdPackageDetails().get(0).getProdProductBranch().getAdultPrice();
				model.addAttribute("toPrice", price/2);
				model.addAttribute("backPrice", price/2);
			}else if ("TO".equals(flag)){//单程，设置往返程的价格产品ID商品ID等信息

				FlightParam toFlightParam = new FlightParam();
				FlightParam backFlightParam = new FlightParam();
				for (ProdPackageGroup p : transprotProdPackageList) {
					ProdPackageDetail detail = p.getProdPackageDetails().get(0);
					ProdPackageGroupTransport transport =  p.getProdPackageGroupTransport();
					LOG.info("productId["+ productId +
							"] ProdPackageGroupTransport.toString()【"+ transport.toString() + "】");
					if(isToType(transport) ){
						Long suppId = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
						FlightNoVo goods = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId+"");
						toFlightParam.setAdultPrice(goods.getAdultAmt());
						toFlightParam.setChildPrice(goods.getChildAmt());
						toFlightParam.setPackageGroupId(detail.getGroupId());
						toFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
						toFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
						toFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
					}
					if(isBackType(transport)){
						Long suppId = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsList().get(0).getSuppGoodsId();
						FlightNoVo goods = p.getProdPackageDetails().get(0).getProdProductBranch().getSuppGoodsMap().get(suppId+"");
						backFlightParam.setAdultPrice(goods.getAdultAmt());
						backFlightParam.setChildPrice(goods.getChildAmt());
						backFlightParam.setPackageGroupId(detail.getGroupId());
						backFlightParam.setPackageProductBranchId(detail.getProdProductBranch().getProductBranchId());
						backFlightParam.setPackageProductId(detail.getProdProductBranch().getProductId());
						backFlightParam.setSelectedSuppGoodsId(goods.getGoodsId());
					}

					if(!isToType(transport) && !isBackType(transport)){
						LOG.error("productId["+ productId +"]加载默认交通时ERROR 单程交通 既无返程也无去程信息。");
					}
				}

				//交通组排序，去程排在返程前面
				Collections.sort(transprotProdPackageList,new Comparator<ProdPackageGroup>() {
					public int compare(ProdPackageGroup o1, ProdPackageGroup o2) {
						if(o1.getProdPackageGroupTransport()!=null &&
								isToType(o1.getProdPackageGroupTransport())){
							return -1;
						}else{
							return 1;
						}
					}
				});
				LOG.error("productId["+ productId +"],toFlightParam== " + toFlightParam.toString()+
						"\n backFlightParam== " + backFlightParam.toString());
				model.addAttribute("toFlightParam", toFlightParam);
				model.addAttribute("backFlightParam", backFlightParam);
			}
		}else{
			if(CollectionUtils.isEmpty(transprotProdPackageList)){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList为空");
				return;
			}
			if(CollectionUtils.isNotEmpty(transprotProdPackageList) && CollectionUtils.isEmpty(transprotProdPackageList.get(0).getProdPackageDetails())){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails() 为空");
				return;
			}
			if(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch()==null ){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch() 为空");
				return;
			}
			if(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch()==null ){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch() 为空");
				return;
			}
			if(!tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId())){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProductBranch().getBizBranch().getCategoryId() 不是机票21L");
				return;
			}
			if(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct()==null){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct() 为空");
				return;
			}
			if(!tempCategary.equals(transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct().getBizCategoryId())){
				LOG.error("productId["+ productId +"]加载默认交通时ERROR transprotProdPackageList.get(0).getProdPackageDetails().get(0).getProdProduct().getBizCategoryId()不是 机票21L");
				return;
			}
		}
	}
	/**
	 * 判断是否为去程
	 * @param transport
	 * @return
	 */
	private boolean isToType(ProdPackageGroupTransport transport) {
		if(transport == null){
			return false;
		}
		if(transport.getfToStartDate()!=null ||
				transport.getToStartDate()!=null ||
				transport.getToStartDays()!=null ||
				transport.getToDestination()!=null||
				transport.getToDestinationDistrict()!=null ||
				transport.getToDestinationDistrictMuch()!=null ||
				transport.getToStartPoint()!=null ||
				transport.getToStartPointDistrict()!=null ||
				transport.getToStartPointDistrictList()!=null ||
				transport.getToStartPointIds()!=null){
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为返程
	 * @param transport
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private boolean isBackType(ProdPackageGroupTransport transport) {
		if(transport == null){
			return false;
		}
		if(transport.getfBackStartDate()!=null ||
				transport.getBackStartDate()!=null ||
				transport.getBackStartDays()!=null ||
				transport.getBackDestination()!=null||
				transport.getBackDestinationDistrict()!=null ||
				transport.getBackStartPoint()!=null ||
				transport.getBackStartPointDistrict()!=null){
			return true;
		}
		return false;
	}

	/***
	 * 处理含有机票信息的数据
	 * @param pdId
	 * @param specDate
	 * @param adultNum
	 * @param childNum
	 * @param lineProductVo
	 */
	private String havaAPIMesssetHandle(Long pdId, Date specDate, Long adultNum,
			Long childNum, PackageTourProductVo lineProductVo,HttpServletRequest req,Long startAll, Long startDistrictId,
			List<ProdPackageGroup> plist) {
		long startFunc=System.currentTimeMillis();
		Long startTime = System.currentTimeMillis();
		String methodName = "OrderLineProductQueryAction#havaAPIMesssetHandle-->productId = " + pdId;
		String resultStr=null;
		//打包线路中其他机票组的对接商品的规格列表 这里的成人数儿童数要改成从前台得到，
		List<ProdPackageGroup> listpr=new ArrayList<ProdPackageGroup>();

		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("lvmamaProductId", pdId);
		pMap.put("specDate", specDate);
		pMap.put("adultQuantity", adultNum);
		pMap.put("childQuantity", childNum);
		pMap.put("distributorId", Constant.DIST_BACK_END);
		pMap.put("startDistrictId", startDistrictId);
		ResultHandleT<List<ProdPackageGroup>>  returnListpr = new ResultHandleT<List<ProdPackageGroup>>();
		try{
			returnListpr=  prodCalClientRemote.getApiFlightProductBranch(pMap);
		}catch(Exception ex){
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			returnListpr.setMsg(ex);
		}
		LOG.info(ComLogUtil.printTraceInfo(methodName, "【对接】查询对接机票产品规格信息",
				"ProdCalClientService.getApiFlightProductBranch", (System.currentTimeMillis()-startTime)));

		List<ProdPackageGroup> trificList= lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
	    List<ProdPackageGroup> plistFilter = new ArrayList<ProdPackageGroup>();
	    if (returnListpr.getReturnContent()!=null) {
			listpr=returnListpr.getReturnContent();

		    AirLineDayVo airLineDayVo=new AirLineDayVo();
		    airLineDayVo.setDay(specDate);
		    List<TrafficGroupVo> listTrafficGroupVo = new ArrayList<TrafficGroupVo>();
		    for(ProdPackageGroup prodPackageGroup:listpr)
		    {
		        //生成交通产品的List
		    	prodPackageGroup.setJiPiaoDuiJieFlag("Y");
		        List<TrafficVo> trafficVoList = createTrafficVoList(prodPackageGroup);
		        TrafficGroupVo trafficGroupVo = new TrafficGroupVo();
		        trafficGroupVo.setTrafficVoList(trafficVoList);
		        if(prodPackageGroup.getProdPackageGroupTransport().getfToStartDate()!=null)
		        {
		        	trafficGroupVo.setGoDate(prodPackageGroup.getProdPackageGroupTransport().getfToStartDate());
		        }else{
		        	trafficGroupVo.setBackDate(prodPackageGroup.getProdPackageGroupTransport().getfBackStartDate());
		        }

		        trafficGroupVo.setGroupId(prodPackageGroup.getGroupId());
		        listTrafficGroupVo.add(trafficGroupVo);
		    }
		    airLineDayVo.setTrafficGroupVoList(listTrafficGroupVo);
		    airLineDayVo.setAdultNum(adultNum);
		    airLineDayVo.setChildNum(childNum);
		    //调用机票组实现的接口 airLineDayVo
		    List<AirLineDayVo> listAirline=new ArrayList<AirLineDayVo>();
		    listAirline.add(airLineDayVo);
		    LvfSignVo sign =new LvfSignVo();
		    sign.setIp(req.getRemoteAddr());
	        sign.setSessionId(req.getSession().getId());
	        JSONArray.fromObject(listAirline).toString();

	        try {
				LOG.info("查询最低价航班.sign:" + JSONObject.fromObject(sign).toString());
				LOG.info("查询最低价航班.listAirline:" + JSONArray.fromObject(listAirline).toString());
			} catch (Exception e) {
				LOG.info("{}", e);
			}
	        startTime=System.currentTimeMillis();
			ResultHandleT<List<AirLineDayVo>> listAirlineRetrun  =flightSearchService.queryGroupLowerSalePriceByAirLineDayVoList(sign,listAirline,OrderEnum.Requset_TYPE_FLAG.flightsearch.name());
			LOG.info(ComLogUtil.printTraceInfo(methodName, "【对接】查询最低价航班 (求日期最低核算价)",
						"FlightSearchService.queryGroupLowerSalePriceByAirLineDayVoList", (System.currentTimeMillis()-startTime)));
			try {
				LOG.info("查询最低价航班.result:"	+ JSONArray.fromObject(listAirlineRetrun).toString());
			} catch (Exception e) {
				LOG.info("{}", e);
			}

			if(listAirlineRetrun.getReturnContent()!=null &&listAirlineRetrun.getReturnContent().size()>0)
		    {
		    	airLineDayVo=listAirlineRetrun.getReturnContent().get(0);//因为只传入一个对象
		    }
			AutoPackageUtil.filterAirLineGoods(airLineDayVo, listpr);
		    //转换为现有数据模式，(将TrafficGroupVo转换为ProdPackageGroup)，同时按照成人最低价进行排序为 List<ProdPackageDetail>
		    changeAirLineDayVoToprodPackageGroupList(airLineDayVo,listpr);//changPlatformDataStructure(listpr,airLineDayVo);

		    for (ProdPackageGroup pp:listpr) {
		    		if (pp.getProdPackageDetails()==null ||pp.getProdPackageDetails().size()<1) {
		    			DateFormat sdf =new SimpleDateFormat("yyyy年MM月dd日");
		    			String dateStr =sdf.format(specDate);
		    			resultStr="线路产品ID:"+pdId+",所对应的组ID"+pp.getGroupId()+",打包了对接机票"+"在需要查询的"+dateStr+"游玩日无满足条件的可售航班,后台无法对该产品进行下单请客服在前台进行下单";
		    			break;
					}
			}
		    if(resultStr!=null){
		    	return resultStr;
		    }
		    List<ProdPackageGroup> ApitrificList=listpr;//

		    if(!CollectionUtils.isEmpty(plist)){
		    	for (ProdPackageGroup p : plist) {
			    	//过滤掉往返程，因为进这个方法的都是单程
			    	if(p!=null && p.getProdPackageGroupTransport()!=null
			    			&& !"TOBACK".equals(p.getProdPackageGroupTransport().getTransportType())){
			    		plistFilter.add(p);
			    	}
			    }
		    }
		    if (CollectionUtils.isNotEmpty(trificList)) {

				   List<ProdPackageGroup> allTrificList=null;

				   allTrificList=new ArrayList<ProdPackageGroup>();//获取正确排序的组信息
//					   //用于存放费非对接和对接组的MAP
				   Map<Long,ProdPackageGroup> allMapProdPackageGroup=new HashMap<Long, ProdPackageGroup>();
		    	   for(ProdPackageGroup prodPackageGroup:ApitrificList ){//将对接组信息放入MAP
		    		   allMapProdPackageGroup.put(prodPackageGroup.getGroupId(), prodPackageGroup);
		    	   }
		    	   for(ProdPackageGroup prodPackageGroup:trificList ){//将非对接组信息放入MAP
		    		   allMapProdPackageGroup.put(prodPackageGroup.getGroupId(), prodPackageGroup);
		    	   }
		    	   Iterator it = allMapProdPackageGroup.keySet().iterator();
		    	   while(it.hasNext()){
		    		   allTrificList.add(allMapProdPackageGroup.get(it.next()));
		    	   }
		    	   lineProductVo.getProdPackageGroupMap().put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), allTrificList);
				  //将对接机票转换组数据和现有费对接数据按照上面排序好的组进行赋值
			}else{
				if(CollectionUtils.isNotEmpty(ApitrificList)){
					lineProductVo.getProdPackageGroupMap().put(ProdPackageGroup.GROUPTYPE.TRANSPORT.name(), ApitrificList);
				}
			}
		    //如果打包单程所有交通的组数 与 可售的交通组数不相等，则当前交通不可售，不展示数据
		    if(plistFilter!=null && lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name())!=null ){
		    	if(plistFilter.size() != lineProductVo.getProdPackageGroupMap().get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name()).size()){
		    		lineProductVo.getProdPackageGroupMap().remove(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
		    		resultStr="产品存在无数据交通组";
		    	}
		    }

		}else if(CollectionUtils.isEmpty(trificList)){
			resultStr="产品存在无数据组";
		}else if(!CollectionUtils.isEmpty(trificList) && trificList.size()!=plistFilter.size()){
			resultStr="产品存在无数据组";
		}

		long endFunc=System.currentTimeMillis();
		LOG.info("时间戳"+startAll+"@线路后台加载商品用时@OrderLineProductQueryAction.havaAPIMesssetHandle@"+(endFunc-startFunc));
		return resultStr;
	}

	/**
     * 转换为现有数据模式，(将AirLineDayVo转换为prodPackageGroupList)，同时按照成人最低价进行排序为 List<ProdPackageDetail>
     * @param airLineDayVo
     * @param planePackageGroupList
     */
    private void changeAirLineDayVoToprodPackageGroupList(AirLineDayVo airLineDayVo,List<ProdPackageGroup> planePackageGroupList){
        //用来存储产品ID对应规格列表的Map
        Map<Long,List<BranchVo>> transformResultMap = new HashMap<Long, List<BranchVo>>();
        List<TrafficGroupVo> trafficGroupVoList = airLineDayVo.getTrafficGroupVoList();
        Long audlltNum=airLineDayVo.getAdultNum();
        Long childNum=airLineDayVo.getChildNum();

        if(trafficGroupVoList != null && trafficGroupVoList.size() > 0){
            for(TrafficGroupVo trafficGroupVo : trafficGroupVoList){
                if(trafficGroupVo != null){
                    List<TrafficVo> trafficVoList = trafficGroupVo.getTrafficVoList();
                    if(trafficVoList != null && trafficVoList.size() > 0){
                        for(TrafficVo trafficVo : trafficVoList){
                            Long productId = trafficVo.getTrafficId();
                            List<BranchVo> branchVoList = trafficVo.getBranchVoList();
                           // branchVoList = transformResultMap.get(productId);
                            transformResultMap.put(productId,branchVoList);
                        }
                    }
                }
            }
        }
        //修改商品的成人价 并对商品进行排序
        if(planePackageGroupList != null && planePackageGroupList.size() > 0){
            for(ProdPackageGroup prodPackageGroup : planePackageGroupList){
                List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
                List<ProdPackageDetail> prodNoPackageDetails = new ArrayList<ProdPackageDetail>();
                if(prodPackageDetails != null && prodPackageDetails.size() > 0){
                    for(ProdPackageDetail prodPackageDetail : prodPackageDetails){
                        ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
                        Long productBranchId = prodProductBranch.getProductBranchId();
                        Long productId = prodProductBranch.getProductId();
                        List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
                        Map<String,FlightNoVo> flightNoVoMap = prodProductBranch.getSuppGoodsMap();
                        List<BranchVo> branchVoList = transformResultMap.get(productId);
                        flightNoVoMap = new HashMap<String, FlightNoVo>();
                        if(branchVoList != null && branchVoList.size() > 0){
                            for(BranchVo branchVo : branchVoList){
                                Long branchId = branchVo.getBranchId();
                                if(productBranchId.intValue() == branchId.intValue()){
                                    List<FlightNoVo> flightNoVoList = branchVo.getGoodsList();
                                    if(flightNoVoList != null && flightNoVoList.size() > 0){
                                        for(FlightNoVo flightNoVo : flightNoVoList){
                                            Long goodsId = flightNoVo.getGoodsId();

                                            flightNoVoMap.put(goodsId+"",flightNoVo);
                                        }

                                        prodProductBranch.setSuppGoodsMap(flightNoVoMap);

                                    }

                                    /*====原逻辑是取vst和机票接口的交集,现在改为并集 start=====*/
                                    Iterator<String> goodsIds = flightNoVoMap.keySet().iterator();
                                	while (goodsIds.hasNext()) {
                                		SuppGoods tempGoods = new SuppGoods();
                                		tempGoods.setSuppGoodsId(Long.parseLong(goodsIds.next()));
                                		if(suppGoodsList ==null){
                                			suppGoodsList = new ArrayList<SuppGoods>();
                                		}
                                		suppGoodsList.add(tempGoods);
									}
                                	/*====原逻辑是取vst和机票接口的交集,现在改为并集 end =====*/

                                    if(suppGoodsList != null && suppGoodsList.size() > 0){
                                        Long lowestAdultAmt = -9999L;//最低价
                                        SuppGoods lowestSuppGoods = null;//最低价的商品
                                        for(SuppGoods suppGoods :suppGoodsList){
                                            Long suppGoodsId = suppGoods.getSuppGoodsId();
                                            FlightNoVo flightNoVo = flightNoVoMap.get(suppGoodsId+"");
                                            if(flightNoVo!=null&&flightNoVo.getAdultAmt()!=null){
                                            	 //成人价
                                                Long adultAmt = flightNoVo.getAdultAmt();
                                                //儿童价
                                               Long childAmt = flightNoVo.getChildAmt();
                                                suppGoods.setDailyLowestPrice(adultAmt);

                                                if(childNum!=null && childNum>0){

                                                	 if(lowestAdultAmt.intValue() == -9999 && childAmt!=null && childAmt>=0){
                                                         lowestAdultAmt = adultAmt;
                                                         lowestSuppGoods = suppGoods;
                                                     }
                                                	if(lowestAdultAmt.intValue() > adultAmt.intValue() && childAmt!=null&&flightNoVo.getRemain()>audlltNum){
                                                        lowestAdultAmt = adultAmt;
                                                        lowestSuppGoods = suppGoods;
                                                    }
                                                }else{
                                                	 if(lowestAdultAmt.intValue() == -9999){
                                                         lowestAdultAmt = adultAmt;
                                                         lowestSuppGoods = suppGoods;
                                                     }

                                                	if(lowestAdultAmt.intValue() > adultAmt.intValue() &&flightNoVo.getRemain()>audlltNum){
                                                        lowestAdultAmt = adultAmt;
                                                        lowestSuppGoods = suppGoods;
                                                    }
                                                }

                                            }


                                        }

                                        if(lowestSuppGoods!=null){
                                        	  //把最低价的商品放在第一个
                                            suppGoodsList.remove(lowestSuppGoods);
                                            suppGoodsList.add(0,lowestSuppGoods);
                                        }
                                        //如果最低成人价小于0，则说明从机票返回的数据中未找到任何满足条件的商品，将当前打包明细的商品删除
                                        if(lowestAdultAmt!=null&& lowestAdultAmt < 0){
                                        	suppGoodsList.removeAll(suppGoodsList);
                                        }

                                    }
                                }
                            }
                        }

                        if(flightNoVoMap.size()==0){
                        	prodNoPackageDetails.add(prodPackageDetail);
                        }
                    }
                }

                if(prodNoPackageDetails.size()>0){
                	prodPackageDetails.removeAll(prodNoPackageDetails);
                }
                //对这一组按价格对规格进行排序
                if(prodPackageDetails != null && prodPackageDetails.size() > 0){
                    Long lowestAdultAmt = 0L;//最低价
                    SuppGoods lowestSuppGoods = null;//最低价的商品
                    ProdPackageDetail firstProdPackageDetail = null;
                    for(ProdPackageDetail prodPackageDetail : prodPackageDetails){
                        if(prodPackageDetail.getProdProductBranch() != null &&
                                prodPackageDetail.getProdProductBranch().getSuppGoodsList() != null &&
                                prodPackageDetail.getProdProductBranch().getSuppGoodsList().size() > 0){
                            lowestSuppGoods = prodPackageDetail.getProdProductBranch().getSuppGoodsList().get(0);
                            if(lowestSuppGoods != null){
                                Long dailyLowestPrice = lowestSuppGoods.getDailyLowestPrice();
                                if(lowestAdultAmt.intValue() == 0){
                                    lowestAdultAmt = dailyLowestPrice;
                                }
                                if(dailyLowestPrice != null){
                                    if(lowestAdultAmt.intValue() > dailyLowestPrice.intValue()){
                                        lowestAdultAmt = dailyLowestPrice;
                                        firstProdPackageDetail = prodPackageDetail;
                                    }
                                }
                            }
                        }

                    }
                    //把组里价格最低的产品放在第一位置
                    if(firstProdPackageDetail != null){
                        prodPackageDetails.remove(firstProdPackageDetail);
                        prodPackageDetails.add(0,firstProdPackageDetail);
                    }
                }
            }


        }

    }








	private Long calGapPrice(List<ProdPackageGroup> prodPackageGroups, StringBuffer sb, String type,StringBuffer fancha, List<String> selectRange){
		Long totalPrice = 0L;
		if(prodPackageGroups != null && prodPackageGroups.size() > 0){
			//1.供应商打包(升级/可换酒店) 2.自主打包的跟团游、自由行、升级和可换酒店
			for(ProdPackageGroup packageGroup : prodPackageGroups){
				List<ProdPackageDetail> packageDetails = packageGroup.getProdPackageDetails();
				if(packageDetails != null && packageDetails.size() > 0){
					for(int i = 0; i < packageDetails.size(); i++){
						ProdPackageDetail ppd = packageDetails.get(i);
						ProdProductBranch prodProductBranch = ppd.getProdProductBranch();
						if(prodProductBranch != null){
							String status = "Y";
							boolean hasGap = false;
							//取房差值
							Map<String, Long> selectGapPriceMap = prodProductBranch.getSelectGapPriceMap();
							String selectGapQuantityRange = prodProductBranch.getSelectGapQuantityRange();
							Long price = 0L;
							Long price1 = 0L;
							StringBuffer timePriceSelect = new StringBuffer("");
							if (selectGapPriceMap != null && selectGapPriceMap.size() > 0 && selectGapQuantityRange != null) {
								//每个房差的价格
								Set<String> keys = selectGapPriceMap.keySet();
								for (String key : keys) {
									if(key != null){
										price = selectGapPriceMap.get(key);
										if(price != null){
											if("update".equalsIgnoreCase(type)){
												status = "N";
											}
											if("change".equalsIgnoreCase(type) && i != 0){
												status = "N";
											}
											if("line_local".equalsIgnoreCase(type)){
												if(i != 0){
													status = "N";
												}else{
													//取日期列表 (仅当地游有日期选择),不是日期列表第一个日期对应的房差则默认不计算总房差价
													List<String> selectDateList = prodProductBranch.getSelectDateList();
													if(selectDateList != null && selectDateList.size() > 0){
														if(!key.equalsIgnoreCase(selectDateList.get(0))){
															status = "N";
														}
													}
												}
											}
											if("Y".equals(status)){
												totalPrice = totalPrice + price;
											}
											hasGap = true;
											break;
										}
									}
								}
								//满足当地游日期选择后房差价随之改变
								if("line_local".equalsIgnoreCase(type)){
									//head部：时间对应价格
									if(timePriceSelect.length() < 1){
										timePriceSelect.append("<select class=\"timePriceSelect\" style=\"display:none;\" id=\""
												+ ppd.getGroupId() + "-" + ppd.getDetailId() + "tps"
												+ "\">");
									}
									for (String key : keys) {
										if(key != null){
											price1 = selectGapPriceMap.get(key);
											if(price1 != null){
												timePriceSelect.append("<option class=\""
														+ ppd.getGroupId() + "-" + ppd.getDetailId() + "tpo"
														+ "\" groupId=\""
														+ ppd.getGroupId()
														+ "\" detailId=\""
														+ ppd.getDetailId()
														+ "\" theTime=\""
														+ key
														+ "\" thePrice=\""
														+ PriceUtil.trans2YuanStr(price1)
														+ "\"></option>");
											}
										}
									}
									//尾部：时间对应价格
									timePriceSelect.append("</select>");
								}
								if(hasGap){
									//房差份数选择
									selectRange.clear();
									selectRange.add(selectGapQuantityRange.trim());
									//每个房差的份数
									int fangChaQuantity = 0;
									if (selectGapQuantityRange.startsWith("1")) {
										fangChaQuantity = 1;
										fancha.deleteCharAt(0);
										fancha.append("1");
									}
									sb.append("<div class=\"lvmama-fangcha-price\" data-type=\""
											+ type
											+ "\" groupId=\""
											+ ppd.getGroupId()
											+ "\" id=\""
											+ ppd.getGroupId() + "-" + ppd.getDetailId() + "gap"
											+ "\" data-status=\""
											+ status
											+ "\" data-quantity=\""
											+ fangChaQuantity
											+ "\" data-fangcha=\""
											+ PriceUtil.trans2YuanStr(price)
											+ "\"></div>");
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
	 * 显示门票凭证的有效时间
	 * @return
	 */
	@RequestMapping("/ord/order/queryLineTicketCertValid.do")
	@ResponseBody
	public ResultMessage showTicketCertValid(String suppGoodsIds){
		ResultMessage message = ResultMessage.createResultMessage();
		String[] ids=suppGoodsIds.split(",");
		if(ArrayUtils.isNotEmpty(ids)){
			for(String id:ids){
				Long num = NumberUtils.toLong(id);
				ResultHandleT<SuppGoods> suppGoodsHandle = suppGoodsClientService.findSuppGoodsById(num);
				SuppGoodsExp suppGoodsExp = suppGoodsClientService.findTicketSuppGoodsExp(num);
                if (suppGoodsHandle == null || suppGoodsExp == null) {
                    continue;
                }

                SuppGoods suppGoods = suppGoodsHandle.getReturnContent();
                StringBuffer msgBuffer = new StringBuffer();
                msgBuffer.append(suppGoods.getGoodsName());
                if (Constants.N_FLAG.equalsIgnoreCase(suppGoods.getAperiodicFlag())) {
                    msgBuffer.append(" 有效期:").append(suppGoodsExp.getDays()).append("天");
                    message.addObject("suppGoods:" + id, msgBuffer.toString());
                } else {
                    msgBuffer.append(SuppGoodsExpTools.getAperiodicExpDesc(suppGoodsExp, new Date()));
                    if (suppGoodsExp.getUnvalidDesc() != null) {
                        msgBuffer.append(suppGoodsExp.getUnvalidDesc()).append("。");
                    }
                    message.addObject("suppGoods:" + id, msgBuffer.toString());
                }
            }
		}
		return message;
	}

	/****************************** 补充产品信息 ************************************/
	/**
	 * 按照规格类型对规格进行分类
	 *
	 * @param list
	 * @return
	 */
	private Map<String, List<ProdProductBranch>> findProdProductBranchVoMap(
			List<ProdProductBranch> list) {
		Map<String, List<ProdProductBranch>> map = new HashMap<String, List<ProdProductBranch>>();
		String branchCode = null;
		List<ProdProductBranch> tempList = null;
		for (ProdProductBranch vo : list) {
			if (vo.getBizBranch() == null) {
				// 加载bizBranch信息
				vo.setBizBranch(branchClientRemote.findBranchById(
						vo.getBranchId()).getReturnContent());
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
	 * 对所有主规格进行遍历，制作次规格对应主规格的对应关系<次规格商品id，对应关系对象>
	 *
	 * @param list
	 * @return
	 */
	private Map<Long, SuppGoodsRelation> findProdProductRelationMap(
			List<ProdProductBranch> list) {
		Map<Long, SuppGoodsRelation> relationMap = new HashMap<Long, SuppGoodsRelation>();
		for (ProdProductBranch vo : list) {
			if (vo.getBizBranch() == null) {
				continue;
			}
			if (vo.getBizBranch().getAttachFlag().equals("Y")
					&& CollectionUtils.isNotEmpty(vo.getSuppGoodsList())) {
				for (SuppGoods goods : vo.getSuppGoodsList()) {
					if (CollectionUtils.isNotEmpty(goods
							.getSuppGoodsRelationList())) {
						for (SuppGoodsRelation relation : goods
								.getSuppGoodsRelationList()) {
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
	private void initAdditionProductBranchList(
			Map<Long, SuppGoodsRelation> relationMap,
			List<ProdProductBranch> additionList, Date specDate,LineProdPackageGroupContainer container) {

		SuppGoodsRelation relation=null;
		// 为附加信息增加父对象
		if (CollectionUtils.isNotEmpty(additionList)) {
			for (ProdProductBranch branch : additionList) {
				if (branch.getSuppGoodsList() != null) {
					for (SuppGoods goods : branch.getSuppGoodsList()) {
						relation=relationMap.get(goods
								.getSuppGoodsId());
						if(relation==null){
							relation=new SuppGoodsRelation();
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
					if(container!=null){
						Date tempDate=container.getProductTimeMapItem(branch.getProductId());
						if(tempDate!=null){
							specDate=tempDate;
						}
					}

					this.lineProdPackageGroupServiceImpl
							.initSupplierProdBranchTimePrice(branch, specDate,
									GROUPTYPE.LINE);
					//设置时间单价
					Map newMap=LineUtils.change(branch.getSelectPriceMap());
					branch.setSelectPriceMap(newMap);
				}
			}
		}
	}

	/****************************** 补充产品信息结束 ************************************/

	/**
	 * 初始化查询表单
	 *
	 * @author wenzhengtao
	 * @param model
	 */
	private void initQueryForm(Model model, HttpServletRequest request) {
		// 品类字典
		Map<String, String> categoryMap = new LinkedHashMap<String, String>();
		categoryMap.put("", "全部线路");
		categoryMap.put(TOURE_ROUTE.GROUP.name(), "出发地跟团");
		categoryMap.put(TOURE_ROUTE.FREETOUR.name(), "自由行");
		categoryMap.put(TOURE_ROUTE.AROUND.name(), "周边跟团游");
		categoryMap.put(TOURE_ROUTE.LOCAL.name(), "目的地跟团游");
		model.addAttribute("categoryMap", categoryMap);

		//自由行bizFreedomList
		List<BizCategory> bizFreedomList = categoryClientService.getBizCategorysByParentCategoryId(WineSplitConstants.ROUTE_FREEDOM).getReturnContent();
		model.addAttribute("bizFreedomList", bizFreedomList);

		// 价格区间字段
		Map<String, Object> priceRangeMap = new LinkedHashMap<String, Object>();
		priceRangeMap.put("", "不限");
		priceRangeMap.put("1", "100以下");
		priceRangeMap.put("2", "100-200");
		priceRangeMap.put("3", "200以上");
		priceRangeMap.put("7", "自定义");
		model.addAttribute("priceRangeMap", priceRangeMap);
	}


	private List<SuppGoodsSaleRe> getInsuranceList(List<SuppGoodsSaleRe> suppGoodsSaleReList){
		List<SuppGoodsSaleRe> list = new ArrayList<SuppGoodsSaleRe>();
		if(CollectionUtils.isNotEmpty(suppGoodsSaleReList)){
			for(SuppGoodsSaleRe saleRe:suppGoodsSaleReList){
				if(CollectionUtils.isNotEmpty(saleRe.getInsSuppGoodsList())){
					list.add(saleRe);
				}
			}
		}
		return list;
	}


	/**
	 * 对关联销售信息进行分类处理
	 * @param suppGoodsSaleReList
	 * @return
	 */
	private Map<String,List<ProdProductBranch>> getReSaleCategoryMap(List<SuppGoodsSaleRe> suppGoodsSaleReList){
		HashMap<String,List<ProdProductBranch>> reSaleCategoryMap=new HashMap<String,List<ProdProductBranch>>();

		//门票
		List<ProdProductBranch> ticketBranchList=new ArrayList<ProdProductBranch>();
		//当地游
		List<ProdProductBranch> lineLocalBranchList=new ArrayList<ProdProductBranch>();
		//交通
		List<ProdProductBranch> transportBranchList=new ArrayList<ProdProductBranch>();
		//签证
		List<ProdProductBranch> visaBranchList=new ArrayList<ProdProductBranch>();

		if(CollectionUtils.isNotEmpty(suppGoodsSaleReList)){
			for(SuppGoodsSaleRe saleRe:suppGoodsSaleReList){
				//得到关联销售的品类id，按照品类id归类
				if(saleRe.getReProductBranch()==null){
					continue;
				}

				BizCategory bizCategory=saleRe.getReProductBranch().getProduct().getBizCategory();

				//门票的价格是在商品上、只有一个价格销售价，价格区分规则是成人票和儿童票，在票种上区分
				//当地游主规格是多价格，分为成人价和儿童价
				//交通是分为成人价和儿童价

				//门票
				if(ProductCategoryUtil.isTicket(bizCategory.getCategoryCode())){
					ticketBranchList.add(saleRe.getReProductBranch());
					if(reSaleCategoryMap.get("ticket")==null){
						reSaleCategoryMap.put("ticket", ticketBranchList);
					}
				}
				//线路
				if(ProductCategoryUtil.isRoute(bizCategory.getCategoryCode())){
					lineLocalBranchList.add(saleRe.getReProductBranch());
					if(reSaleCategoryMap.get("line")==null){
						reSaleCategoryMap.put("line", lineLocalBranchList);
					}
				}
				//交通
				if(ProductCategoryUtil.isRouteTraffic(bizCategory.getCategoryCode())){
					transportBranchList.add(saleRe.getReProductBranch());
					if(reSaleCategoryMap.get("transport")==null){
						reSaleCategoryMap.put("transport", transportBranchList);
					}
				}
				//签证
				if(ProductCategoryUtil.isVisa(bizCategory.getCategoryCode())){
					visaBranchList.add(saleRe.getReProductBranch());
					if(reSaleCategoryMap.get("visa")==null){
						reSaleCategoryMap.put("visa", visaBranchList);
					}
				}

			}
		}
		return reSaleCategoryMap;
	}

	 /**
     * 获取时间价格表的数据
     */
    @RequestMapping("/ord/order/route/CalendarJsonData.do")
    @ResponseBody
    public Object getCalendarJsonData(HttpServletRequest request,Model model,Long productId,String currentDate){
    	long startTimeFunc=System.currentTimeMillis();
        ResultMessage msg = ResultMessage.createResultMessage();
		try {
			Long startDistrictId = null;
			try{
				startDistrictId = request.getParameter("startDistrictId")==null?null:Long.valueOf( request.getParameter("startDistrictId").toString().trim() );
			}catch (Exception ex){LOG.error(ExceptionFormatUtil.getTrace(ex));}
			Long adultNum=null;
			Long childNum=null;
			//下面注释代码考虑后续酒店套餐时产品上的成人数儿童数暂时用不到
		/*	ResultHandleT<CalPriceQuantityVO>  returnCalPriceQuantityVO=prodCalPriceClientService.getCalPriceQuantity(productId);
			CalPriceQuantityVO calPriceQuantityVO=null;
			if(returnCalPriceQuantityVO!=null && returnCalPriceQuantityVO.getReturnContent()!=null)
			{
				calPriceQuantityVO=returnCalPriceQuantityVO.getReturnContent();
				adultNum=calPriceQuantityVO.getAdultQuantityPriceParam();
				childNum=calPriceQuantityVO.getChildQuantityPriceParam();
			}*/
			if(adultNum==null)//避免null值出现
			{
				adultNum=1L;
			}
			if(childNum==null){//避免null值出现
				childNum=0L;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
			Date beginDate = sdf.parse(currentDate);
			Date endDate=DateUtils.addDays(DateUtils.addMonths(beginDate, 1), -1);
			Map<String, Object> params = new HashMap<String, Object>();
	        params.put("productId",productId);
	        //以当前的时间为开始点
	        params.put("beginDate",beginDate);
	        params.put("endDate", endDate);
			params.put("startDistrictId", startDistrictId);
	        ResultHandleT<List<ProdGroupDate >> resultHandleT = prodGroupDateClientService.findProdGroupDateListByParam(productId, params);
	        if(resultHandleT == null || resultHandleT.isFail()||resultHandleT.getReturnContent()==null){
				msg.raise(resultHandleT.getMsg());
			}


            if(resultHandleT != null && resultHandleT.getReturnContent() != null){
                List<ProdGroupDate> prodGroupDateList = resultHandleT.getReturnContent();
                //用来收集团期表中需要调用机票接口的返回的日期
                List<Date> listApiDate= new ArrayList<Date>();
                for(ProdGroupDate prodGroupDate : prodGroupDateList){
                    //根据对接的成人价格来判断是不是对接
                    if(prodGroupDate.getApiSaledPrice()!=null ) {
                        listApiDate.add(prodGroupDate.getSpecDate());
                    }
                }
                //listApiDate = null;//先让他为空避免影响其它开发
                if(listApiDate != null && listApiDate.size() > 0){
                    //打包线路中其他机票组的对接商品的规格列表 这里的成人数儿童数要改成从前台得到，
                	long startreturnMapproduPack=System.currentTimeMillis();
					Map<String, Object> pMap = new HashMap<String, Object>();
					pMap.put("lvmamaProductId", Long.valueOf(productId));
					pMap.put("specDates", listApiDate);
					pMap.put("adultQuantity", adultNum);
					pMap.put("childQuantity", childNum);
					pMap.put("distributorId", Constant.DIST_BACK_END);
					pMap.put("startDistrictId", startDistrictId);
//					ResultHandleT<Map<Date,List<ProdPackageGroup>>> returnMapproduPack=prodCalClientRemote.getApiFlightProductBranchs(Long.valueOf(productId),listApiDate, adultNum, childNum, Constant.DIST_BACK_END);
					ResultHandleT<Map<Date,List<ProdPackageGroup>>> returnMapproduPack=prodCalClientRemote.getApiFlightProductBranchs(pMap);
                	long endreturnMapproduPack=System.currentTimeMillis();
            		LOG.info("时间戳"+startTimeFunc+"@线路后台时间价格表调用vst_back用时@ProdCalClientService.getApiFlightProductBranchs@"+(endreturnMapproduPack-startreturnMapproduPack));
                    Map<Date,List<ProdPackageGroup>>  mapproduPack=  returnMapproduPack.getReturnContent();
                    Set<Date> keys =null;
                    if(mapproduPack!=null){
                    	 keys = mapproduPack.keySet();
                    }
                    List<AirLineDayVo> listAirline=new ArrayList<AirLineDayVo>();
                    for (Date date :keys) {
                        AirLineDayVo  airLineDayVo =new AirLineDayVo();
                        airLineDayVo.setDay(date);
                        airLineDayVo.setAdultNum(adultNum);
                        airLineDayVo.setChildNum(childNum);
                        List<TrafficGroupVo> listTrafficGroupVo = new ArrayList<TrafficGroupVo>();
                        List<ProdPackageGroup> listpr=mapproduPack.get(date);
                        for(ProdPackageGroup prodPackageGroup:listpr)
                        {
                            //生成交通产品的List
                            List<TrafficVo> trafficVoList = createTrafficVoList(prodPackageGroup);
                            TrafficGroupVo trafficGroupVo = new TrafficGroupVo();
                            trafficGroupVo.setTrafficVoList(trafficVoList);
                            trafficGroupVo.setGoDate(prodPackageGroup.getProdPackageGroupTransport().getfToStartDate());
                            trafficGroupVo.setBackDate(prodPackageGroup.getProdPackageGroupTransport().getfBackStartDate());
                            trafficGroupVo.setGroupId(prodPackageGroup.getGroupId());
                            listTrafficGroupVo.add(trafficGroupVo);
                        }
                        airLineDayVo.setTrafficGroupVoList(listTrafficGroupVo);
                        listAirline.add(airLineDayVo);
                    }
                    //调用机票组实现的接口 listAirline

                    long startreturnListAirline=System.currentTimeMillis();
                    LOG.info("Before invoking interface flightSearchService.queryGroupLowerSalePriceByAirLineDayVoList, request is ," + JSONArray.fromObject(listAirline).toString());
                   ResultHandleT<List<AirLineDayVo>> returnListAirline=flightSearchService.queryGroupLowerSalePriceByAirLineDayVoList(null,listAirline,OrderEnum.Requset_TYPE_FLAG.lowprice.name());
                   LOG.info("After invoking interface flightSearchService.queryGroupLowerSalePriceByAirLineDayVoList, result is, " + JSONArray.fromObject(returnListAirline).toString());
                   long endreturnListAirline=System.currentTimeMillis();
           			LOG.info("时间戳"+startTimeFunc+"@线路后台时间价格表调用机票用时@FlightSearchService.queryGroupLowerSalePriceByAirLineDayVoList@--------------------------"+(endreturnListAirline-startreturnListAirline));
                   if(returnListAirline.getReturnContent()!=null && returnListAirline.getReturnContent().size()>0)
                    {
                    	listAirline=returnListAirline.getReturnContent();
                    }
                   // listAirline= queryGroupLowerSalePriceByAirLineDayVoList(listAirline);
                    //把返回的值生成一个日期map的形式，便于下面的使用
                    Map<String,Map<String,Long>> dateTotalPriceMap = new HashMap<String,Map<String,Long>>();
                    Map<Date,Date> haveNotStock=new HashMap<Date,Date>();
                    if(listAirline != null){

                    	 for(AirLineDayVo airLineDayVo : listAirline){
                            Date specDate = airLineDayVo.getDay();
                            String specDateStr = DateUtil.formatDate(specDate, "yyyy-MM-dd");
                            List<TrafficGroupVo> trafficGroupVoList = airLineDayVo.getTrafficGroupVoList();
                            Long groupAdultTotalPrice = 0L;
                            Long groupChildTotalPrice = 0L;
                            Map<String,Long> trafficGroupVoMap = new HashMap<String,Long>();
                            if(trafficGroupVoList != null && trafficGroupVoList.size() > 0){
                              listtrafficGroupVo:  for(TrafficGroupVo trafficGroupVo : trafficGroupVoList){
                                    Long adultAmt = trafficGroupVo.getAdultAmt();//成人价
                                    Long chindAmt = trafficGroupVo.getChildAmt();//儿童价
                                    Long lowGoodId = trafficGroupVo.getCheapestGoodsId();//儿童价
                                    if(lowGoodId !=null)
                                    {
                                    	groupAdultTotalPrice = groupAdultTotalPrice + adultAmt;
                                    	if(chindAmt!=null){
                                    		groupChildTotalPrice = groupChildTotalPrice + chindAmt;
                                    	}

                                    }else{
                                    	haveNotStock.put(specDate, specDate);
                                    	break listtrafficGroupVo;
                                    }

                                }
                            }
                            trafficGroupVoMap.put("groupAdultTotalPrice",groupAdultTotalPrice);
                            trafficGroupVoMap.put("groupChildTotalPrice",groupChildTotalPrice);
                            dateTotalPriceMap.put(specDateStr,trafficGroupVoMap);
                        }
                    }
                    //调用机票实现的接口之后，修改原来的价格

                    List<ProdGroupDate> removeProdGroupDateList =new ArrayList<ProdGroupDate>();
                    for(ProdGroupDate prodGroupDate : prodGroupDateList){
                        Long lowestSaledPrice = prodGroupDate.getLowestSaledPrice();
                        Long lowestSaledChildPrice = prodGroupDate.getLowestSaledChildPrice();
                        final Date specDate = prodGroupDate.getSpecDate();
                        Date flagDate=haveNotStock.get(specDate);
                        if(flagDate!=null)
                        {
                        	removeProdGroupDateList.add(prodGroupDate);
                        	continue;
                        }

                        //String specDateStr = DateUtil.formatDate(specDate,"yyyy-MM-dd");
                        Long apiSaledPrice = prodGroupDate.getApiSaledPrice();//对接的成人价
                        Long apiChildPrice = prodGroupDate.getApiChildPrice();//对接的儿童价
                        if(lowestSaledPrice != null && apiSaledPrice != null){
                            Map<String,Long> trafficGroupVoMap = dateTotalPriceMap.get(DateUtil.formatDate(specDate, "yyyy-MM-dd"));
                            Long groupAdultTotalPrice = trafficGroupVoMap.get("groupAdultTotalPrice");
                            Long groupChildTotalPrice = trafficGroupVoMap.get("groupChildTotalPrice");
                            if(groupAdultTotalPrice != null && lowestSaledPrice !=null){
                                lowestSaledPrice = lowestSaledPrice -  apiSaledPrice + groupAdultTotalPrice;
                                prodGroupDate.setLowestSaledPrice(lowestSaledPrice);
                            }
                            if(groupChildTotalPrice != null && lowestSaledChildPrice!=null){

                                lowestSaledChildPrice = lowestSaledChildPrice - apiChildPrice + groupChildTotalPrice;
                                prodGroupDate.setLowestSaledChildPrice(lowestSaledChildPrice);
                            }
                        }
                    }
                    prodGroupDateList.removeAll(removeProdGroupDateList);
                }
                msg.addObject("timePriceList", prodGroupDateList);

                //查询成团率
                if("Y".equals(request.getParameter("queryGroupRateFlag"))){
                	LOG.info("query group rate");
                	Map<String, Object> params_ = new HashMap<String, Object>();
                	params_.put("productId",productId);
                	params_.put("beginDate",beginDate);
                	params_.put("endDate", endDate);
                	List<ProdGroupDateAddtional> groupRateList = prodGroupDateAddtionalClientService.findRangeDataByParamsBaseList(params_).getReturnContent();
                	Map<String,ProdGroupDateAddtional> groupRateMap = new HashMap<String,ProdGroupDateAddtional>();
                	if(groupRateList !=null && groupRateList.size()>0){
                		for(ProdGroupDateAddtional prodGroupDateAddtional:groupRateList){
                			groupRateMap.put(DateUtil.formatDate(prodGroupDateAddtional.getSpecDate(), "yyyy-MM-dd"), prodGroupDateAddtional);
                		}
                	}
                	msg.addObject("groupRateMap", groupRateMap);
                }
            }




		}catch (Exception e){
			LOG.error("{}", e);
			msg.raise("查询线路团期发生异常.");
		}
		long endTimeFunc=System.currentTimeMillis();
		LOG.info("时间戳" + startTimeFunc + "@线路后台调取时间价格表总用时@OrderLineProductQueryAction.getCalendarJsonData@" + (endTimeFunc - startTimeFunc));
        return msg;
    }

    /**
     * 查询线路成团率
     */
    @RequestMapping("/ord/order/route/queryGroupRate.do")
    public String queryGroupRate(HttpServletRequest request,Model model,Long userId,Long productId,Integer currentYear,Integer currentMonth,String startDate,Integer monthResult,String searchFlag){
    	model.addAttribute("userId", userId);
    	model.addAttribute("productId", productId);
    	model.addAttribute("startDate", startDate);
    	if("Y".equals(searchFlag)){
    		String currentDate = currentYear+"年"+(currentMonth+1)+"月";
    		ResultMessage msg = (ResultMessage) getCalendarJsonData(request,model,productId,currentDate);
    		if(msg.isSuccess()){
    			model.addAttribute("timePriceList", msg.getAttributes().get("timePriceList"));
        		model.addAttribute("groupRateMap", msg.getAttributes().get("groupRateMap"));
    		}
    		return "/order/orderProductQuery/showRouteGroupRateListBack";
    	}
    	model.addAttribute("currentYear", currentYear);
    	model.addAttribute("currentMonth", currentMonth);
    	model.addAttribute("monthResult", monthResult);
    	return "/order/orderProductQuery/showRouteGroupRateBack";
    }

		/**
	     * 生成交通产品的VO，填充其规格
	     * @param prodPackageGroup
	     * @return
	     */
	    private List<TrafficVo> createTrafficVoList(ProdPackageGroup prodPackageGroup){
	        List<TrafficVo> trafficVoList = new ArrayList<TrafficVo>();
	        if(prodPackageGroup == null){
	            return trafficVoList;
	        }
	        //用来存储产品ID对应规格列表的Map
	        Map<Long,List<BranchVo>> transformResultMap = new HashMap<Long, List<BranchVo>>();
	        List<ProdPackageDetail> prodPackageDetailList = prodPackageGroup.getProdPackageDetails();
	        if(prodPackageDetailList != null && prodPackageDetailList.size() > 0){
	            List<BranchVo> branchVoList = null;
	            for(ProdPackageDetail prodPackageDetail : prodPackageDetailList){
	                Long productId = prodPackageDetail.getProdProductBranch().getProductId();
	                Long productBranchId =  prodPackageDetail.getProdProductBranch().getProductBranchId();
	                BranchVo branchVo = new BranchVo();
	                branchVo.setBranchId(productBranchId);
	                branchVoList = transformResultMap.get(productId);
	                if(branchVoList == null || branchVoList.size() == 0){
	                    branchVoList = new ArrayList<BranchVo>();
	                    transformResultMap.put(productId,branchVoList);
	                }
	                branchVoList.add(branchVo);
	            }
	        }
	        if(transformResultMap != null && transformResultMap.size() > 0){
	            Set<Long> keys = transformResultMap.keySet();
	            for(Long key : keys){
	                TrafficVo trafficVo = new TrafficVo();
	                trafficVo.setTrafficId(key);//设置产品ID
	                trafficVo.setBranchVoList(transformResultMap.get(key));
	                trafficVoList.add(trafficVo);
	            }
	        }
	        return trafficVoList;
	    }


		// 交通
	    private List<Map> getBusStopAddress(Long productId,Long categoryId) {
		List<Map> trafficList = new ArrayList<Map>();
		ProdTrafficVO trafficVO = prodTrafficClientServiceRemote.getProdTrafficVOByProductId(productId);
		if(trafficVO==null){
			return null;
		}
		ProdTraffic prodTraffic = trafficVO.getProdTraffic();// 交通信息表
		Map<String, Object> suppGoodsInfoMap = new HashMap<String, Object>();

		trafficList.add(suppGoodsInfoMap);

		Map<String, Object> trafficMap = new HashMap<String, Object>();
		suppGoodsInfoMap.put("trafficMap", trafficMap);
		Map<String, Object> toMap = new HashMap<String, Object>();// 去返程信息
		Map<String, Object> backMap = new HashMap<String, Object>();// 返程信息
		List<Map<String,Object>> toBuses = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> backBuses = new ArrayList<Map<String,Object>>();
		trafficMap.put("toMap", toMap);
		trafficMap.put("backMap", backMap);

		String backType = prodTraffic.getBackType();
		String toType = prodTraffic.getToType();

		if(SUB_PRODUCT_TYPE.BUS.getCode().equals(backType) || SUB_PRODUCT_TYPE.BUS.getCode().equals(toType)){
	        trafficMap.put("trafficType","bus");
	        List<Map<String,String>> buses = new ArrayList<Map<String,String>>();
	        List<ProdTrafficGroup> prodTrafficGroups = trafficVO.getProdTrafficGroupList();
	        int toCount = 0;
            int backCount = 0;
	        for(ProdTrafficGroup ptg : prodTrafficGroups) {
    	        List<ProdTrafficBus> prodTrafficBusList = ptg.getProdTrafficBusList();
    	        if(prodTrafficBusList != null && prodTrafficBusList.size() > 0){

    	            //现在最多只会有一个中转,所以可以按照下面的写法做
    	            for(ProdTrafficBus prodTrafficBus : prodTrafficBusList){
    	                if("TO".equals(prodTrafficBus.getTripType())){
    	                    Map<String, String> bus = new HashMap<String,String>();
    	                    /*bus.put("date", startDate);// 去程日期
*/    	                    bus.put("address",prodTrafficBus.getAdress());//上车地点
    	                    bus.put("startTime",prodTrafficBus.getStartTime());//上车时间
    	                    bus.put("memo",prodTrafficBus.getMemo());
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
                    for(ProdTrafficBus prodTrafficBus : prodTrafficBusList){
                        if ("BACK".equals(prodTrafficBus.getTripType())) {
                            /*backMap.put("date", endDate);// 返程日期*/
                        	 Map<String, String> bus = new HashMap<String,String>();
                        	backMap.put("address", prodTrafficBus.getAdress());// 上车地点
                            backMap.put("startTime", prodTrafficBus.getStartTime());// 上车时间
                            if(categoryId==16L){ //如果品类id为当地游时
                            	Map<String,Object> backItem=new HashMap<String, Object>();
                            	backItem.put("address",prodTrafficBus.getAdress());//上车地点
								backItem.put("startTime",prodTrafficBus.getStartTime());//上车时间
								backItem.put("memo",prodTrafficBus.getMemo());
								backBuses.add(backItem);
							}
                            backCount++;
                        }
                        if ("TO".equals(prodTrafficBus.getTripType())) {
                        	//toMap.put("date", startDate);// 去程日期
							toMap.put("address",prodTrafficBus.getAdress());//上车地点
							toMap.put("startTime",prodTrafficBus.getStartTime());//上车时间
							if(categoryId==16L){
								Map<String,Object> toItem=new HashMap<String, Object>();
								//backItem.put("date", startDate);// 去程日期
								toItem.put("address",prodTrafficBus.getAdress());//上车地点
								toItem.put("startTime",prodTrafficBus.getStartTime());//上车时间
								toItem.put("memo",prodTrafficBus.getMemo());
								toBuses.add(toItem);
							}
                        }
                    }
                }
            }
	        if(toCount > 0 && backCount > 0){
                trafficMap.put("toBackTyep","Y");
            }
	        trafficMap.put("bus", buses);
	        trafficMap.put("toBuses", toBuses);
			trafficMap.put("backBuses", backBuses);
	    }
		LOG.info("==trafficList=="+trafficList);
		return trafficList;
	}


	/**
	 * 从order中查找对应符合goodsId的item
	 * @param order
	 * @param goodsId
	 * @return
	 */
	private OrdOrderItem findRelationOrdOrderItemBySuppGoodsId(OrdOrder order,Long goodsId){
		if(order==null ||goodsId==null){
			return null;
		}
		List<OrdOrderItem> orderItems=order.getOrderItemList();
		if(CollectionUtils.isEmpty(orderItems)){
			return null;
		}
		for (OrdOrderItem ordOrderItem : orderItems) {
			if(ordOrderItem.getOrderPackId()!=null){
				continue;
			}
			if(ordOrderItem.getSuppGoodsId()!=null && goodsId.equals(ordOrderItem.getSuppGoodsId())){
				return ordOrderItem;
			}
		}
		return null;
	}


	private OrdOrderItem findOrdOrderItemBySuppGoodsId(OrdOrder order,Long goodsId){
		if(order==null ||goodsId==null){
			return null;
		}
		List<OrdOrderItem> orderItems=order.getOrderItemList();
		if(CollectionUtils.isEmpty(orderItems)){
			return null;
		}
		for (OrdOrderItem ordOrderItem : orderItems) {
			if(ordOrderItem.getSuppGoodsId()!=null && goodsId.equals(ordOrderItem.getSuppGoodsId())){
				return ordOrderItem;
			}
		}
		return null;
	}

    /**
     * 按照页面显示结构组装关联销售(门票)信息
     * @param suppGoodsSaleReList
     * @return
     */
    private Map<String, Object> buildRelSaleDetailInfo(List<SuppGoodsSaleRe> suppGoodsSaleReList, OrdOrder order, ProdProduct product,Date visitDate ) {
    	if(suppGoodsSaleReList == null || suppGoodsSaleReList.size() <= 0) {
    		return null;
    	}
    	Map<String, Object> relSaleDetailMap = new HashMap<String, Object>();
//    	Map<String, Object> relSaleDetailMap = new TreeMap<String, Object>(new Comparator<Object>() {
//			@Override
//			public int compare(Object o1, Object o2) {
//				if(o1 != null && o1.equals(o2)) {
//					return 0;
//				}
//				if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCnName().equals(o1)) {
//					return -1;
//				} else if(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName().equals(o1)) {
//					return 1;
//				} else {
//					if(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName().equals(o2)) {
//						return -1;
//					}
//				}
//
//				return 1;
//			}
//    	}); //门票详细信息，元素Map的key为门票分类，value为该分类下的门票产品列表
    	Map<String, Object> productMap = null;//产品详细信息，元素Map的key为产品ID，value为该产品下的商品列表
    	Map<String, Object> suppGoodsCategoryMap = null;//商品分类信息(默认显示的/不显示的)
    	Map<String, Object> suppGoodsDetailMap = null;//商品详细信息
    	List<Map<String, Object>> defaultGoodsList = null; //默认显示的(第一条)商品信息
    	List<Map<String, Object>> moreGoodsList = null; //点击“更多”显示的商品信息
        for(SuppGoodsSaleRe suppGoodsSaleRe : suppGoodsSaleReList){
            ProdProductBranch prodProductBranch = suppGoodsSaleRe.getReProductBranch();
            if(prodProductBranch == null){
                continue;
            }

            //判断商品的品类
            BizCategory bizCategory = prodProductBranch.getProduct().getBizCategory();
            //用来判断单门票，其它票，组合套餐票
            Long categoryId = bizCategory.getCategoryId();

            //目前只处理门票 & 签证
			if (!BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket
					.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket
							.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket
							.getCategoryId().equals(categoryId)
					&& !BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId()
							.equals(categoryId)) {
				continue;
			}
            suppGoodsDetailMap = new HashMap<String, Object>();
            List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();//推荐商品
            SuppGoods suppGoods = suppGoodsList.get(0);
            String defaultSinglePrice = "0";//默认单价，页面加载之后显示
            String defaultSelectDate = "";//默认选择的日期
        	suppGoodsDetailMap.put("productId",prodProductBranch.getProduct().getProductId());
        	suppGoodsDetailMap.put("productName",prodProductBranch.getProduct().getProductName());
        	//增加任选可选属性
        	suppGoodsDetailMap.put("reType",suppGoodsSaleRe.getReType());
            //可选择的日期下拉选项列表
            Map<String, Long> selectPriceMap = suppGoods.getSelectPriceMap();//生成可选select
            Map<String, String> selectDateMap = new TreeMap<String, String>();
            DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance();
            df.applyPattern("0.00");
            if(selectPriceMap != null  && selectPriceMap.size() > 0){
                Set<String> keys = selectPriceMap.keySet();
                for(String key : keys) {
                	if(selectPriceMap.get(key) != null) {
                		selectDateMap.put(key, df.format(selectPriceMap.get(key)/100.00));
                	} else {
                		selectDateMap.put(key, "0.00");
                	}
                }
            }
            Set<String> dateKeys = selectDateMap.keySet();
            for(String key : dateKeys) {
            	defaultSelectDate = key;
            	defaultSinglePrice = selectDateMap.get(key);
            	break;
            }

            //一键下单重设默认值
            if(order!=null){
            	OrdOrderItem ordOrderItem=findRelationOrdOrderItemBySuppGoodsId(order, suppGoods.getSuppGoodsId());
            	if(ordOrderItem!=null){
            		String visitTime=DateFormatUtils.format(ordOrderItem.getVisitTime(), "yyyy-MM-dd");
                	for (String key : dateKeys) {
    					if(StringUtils.equals(key, visitTime)){
    						defaultSelectDate = key;
    		            	defaultSinglePrice = selectDateMap.get(key);
    		            	break;
    					}
    				}
            	}
            }


            filterOptinalRouteLocal(product, null, selectDateMap, suppGoodsSaleRe.getLimitDays(), visitDate);

            //所有的天都被排除了，该关联不要显示了
            if(selectDateMap.isEmpty()) {
            	continue;
            }

            suppGoodsDetailMap.put("selectDateMap", selectDateMap);
            suppGoodsDetailMap.put("suppGoods", suppGoods);
//            suppGoodsDetailMap.put("categoryId", categoryId);
//            suppGoodsDetailMap.put("suppGoodsId",suppGoods.getSuppGoodsId());
//            suppGoodsDetailMap.put("suppGoodsName",suppGoods.getGoodsName());
            if(suppGoods.getSuppGoodsAddition() != null){
            	suppGoodsDetailMap.put("lowestSaledPrice",suppGoods.getSuppGoodsAddition().getLowestSaledPrice());
            }

            if(prodProductBranch.getPropValue() != null){
            	suppGoodsDetailMap.put("description",prodProductBranch.getPropValue().get("branch_desc"));
            }else{
            	suppGoodsDetailMap.put("description","");
            }

            String selectQuantityRange = suppGoods.getSelectQuantityRange();
            int defaultQuantity = 0;
            List<String> selectQuantityList = new ArrayList<String>();
            suppGoodsDetailMap.put("selectQuantityList",selectQuantityList);
            if(selectQuantityRange != null){
                String[] ranges = selectQuantityRange.split(",");
                if(ranges != null && ranges.length > 0){
                	defaultQuantity = Integer.parseInt(ranges[0]);//得到默认的选择的份数
                    //默认加载时的份数
                	suppGoodsDetailMap.put("defaultQuantity",defaultQuantity);
                    for(String qunantity : ranges){
                    	selectQuantityList.add(qunantity);
                    }
                    //一键下单重设默认值
                    if(order!=null){
                    	OrdOrderItem ordOrderItem=findRelationOrdOrderItemBySuppGoodsId(order, suppGoods.getSuppGoodsId());
                    	if(ordOrderItem!=null){
                    		for(String qunantity : ranges){
                    			if(ordOrderItem.getQuantity()!=null && NumberUtils.toLong(qunantity)==ordOrderItem.getQuantity().longValue()){
                    				defaultQuantity=NumberUtils.toInt(qunantity);
                    				suppGoods.setFitQuantity(NumberUtils.toLong(qunantity));
                    				suppGoodsDetailMap.put("defaultQuantity",defaultQuantity);
                    			}
                    		}
                    	}
                    }
                }
            }
            suppGoodsDetailMap.put("prodProductBranch", prodProductBranch);
            suppGoodsDetailMap.put("defaultSelectDate", defaultSelectDate);
            suppGoodsDetailMap.put("defaultSinglePrice", defaultSinglePrice);
            double defaultTotalPrice = Double.parseDouble(defaultSinglePrice) * defaultQuantity;
            suppGoodsDetailMap.put("defaultTotalPrice",df.format(defaultTotalPrice));

            productMap = (Map<String, Object>)relSaleDetailMap.get(bizCategory.getCategoryName());
            if(productMap == null) {
            	productMap = new HashMap<String, Object>();
            	relSaleDetailMap.put(bizCategory.getCategoryName(), productMap);
            }
            suppGoodsCategoryMap = (Map<String, Object>)productMap.get(String.valueOf(prodProductBranch.getProduct().getProductId()));
            if(suppGoodsCategoryMap == null) {
            	suppGoodsCategoryMap = new HashMap<String, Object>();
            	suppGoodsCategoryMap.put("product", prodProductBranch.getProduct());
            	productMap.put(String.valueOf(prodProductBranch.getProduct().getProductId()), suppGoodsCategoryMap);
            }

            defaultGoodsList = (List<Map<String, Object>>)suppGoodsCategoryMap.get("defaultGoodsList");
            moreGoodsList = (List<Map<String, Object>>)suppGoodsCategoryMap.get("moreGoodsList");


            if(defaultGoodsList == null || defaultGoodsList.size() <= 0) {
            	defaultGoodsList = new ArrayList<Map<String, Object>>();
            	suppGoodsCategoryMap.put("defaultGoodsList", defaultGoodsList);
            	defaultGoodsList.add(suppGoodsDetailMap);
            } else {
            	if(moreGoodsList == null) {
            		moreGoodsList = new ArrayList<Map<String, Object>>();
            		suppGoodsCategoryMap.put("moreGoodsList", moreGoodsList);
            	}
            	moreGoodsList.add(suppGoodsDetailMap);
            }

        }
        return relSaleDetailMap;
    }

    private void setRelSaleDisplayOrder(List<String> relSaleDisplayOrder) {
    	if(relSaleDisplayOrder == null) {
    		return;
    	}

    	relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCnName());
    	relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCnName());
    	relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCnName());
    	relSaleDisplayOrder.add(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCnName());
    }

    /**
     * 组装前台展示数据
     * @param group
     * @param airCompanies
     * @param spaceList
     * @param selectedFlag 选择商品标识  = 出发时间+航班号 + 商品ID
     * @param backline
     * @param oldPrice
     * @param adultNum
     * @param childNum
     * @return
     */
    private List<Map<String, Object>> scaleFlight(TrafficGroupVo group,List<String> airCompanies,List<String> spaceList,
    		String selectedFlag,boolean backline,Long oldPrice,Long adultNum,Long childNum)throws Exception{
        int thisIndex = 0;
        List<String> seatName = new ArrayList<String>();

        List<Map<String, Object>> flights = new ArrayList<Map<String,Object>>();  //全部row

        List<Map<String,String>> middleList = null;
        Map<String, String > middleMap = null;

        Map<String , Object> flight = null;
        Map<String , String> startFrom = null;
        Map<String , String> endTo = null;
        List<Map<String, String>> seatList = null;
        Map<String, String> seatMap = null;

        List<TrafficVo> trafficVoList = group.getTrafficVoList();
        String flightType = "1";
        if(group.getBackDate() != null){
            flightType = "2";//1：去程  2：返程
        }

        Long startTime = null;
        String methodName = "OrderLineProductQueryAction#scaleFlight";
        for(int a=0,aa= trafficVoList !=null?trafficVoList.size() : 0;a < aa;a++){
            TrafficVo trafficVo = trafficVoList.get(a);
            //查询当前交通产品ID
            startTime = System.currentTimeMillis();
            //ProdProduct trafficProduct = prodProductClientService.findProdProductById(trafficVo.getTrafficId()).getReturnContent();
            ProdProduct trafficProduct = prodProductClientService.findProdProductByIdFromCache(trafficVo.getTrafficId()).getReturnContent();

            LOG.info(ComLogUtil.printTraceInfo(methodName, "【非对接】查询当前交通产品ID ",
					"prodProductClientService.findProdProductById", System.currentTimeMillis()-startTime));

            List<BranchVo> branchVoList = trafficVo.getBranchVoList();
                for (int b = 0,bb=branchVoList!=null? branchVoList.size():0;b < bb; b++) {
                    BranchVo branchVo = branchVoList.get(b);

                    //组装前台舱位的帅选条件
                    if(branchVo.getName()!= null && !"null".equalsIgnoreCase(String.valueOf(branchVo.getName())) && !spaceList.contains(branchVo.getName())){
                        spaceList.add(branchVo.getName());
                    }

                    List<FlightNoVo> flightNoVoList = branchVo.getGoodsList();
                    for (int c = 0,cc=flightNoVoList!=null?flightNoVoList.size():0;c < cc; c++) {
                        FlightNoVo flightNoVo = flightNoVoList.get(c);
                        if(flightNoVo == null ){
                            LOG.info("没有找到该商品");
                            continue ;
                        }
                        if(flightNoVo.getSeatName()!= null && !"null".equalsIgnoreCase(String.valueOf(flightNoVo.getSeatName())) &&!seatName.contains(flightNoVo.getSeatName())){
                            seatName.add(flightNoVo.getSeatName());
                        }

                        //判断该航空公司是否已经加入到map中
                        Map<String, Object> oldFlight = getAirCompanyInMap(flights,flightNoVo.getFlightNo());
                        if (oldFlight == null ) {
                            flight = new HashMap<String, Object>(); // 每条row
                            flight.put("productName", trafficProduct!=null?trafficProduct.getProductName():"");
                            flight.put("selected", -1);
                            seatList = new ArrayList<Map<String, String>>();
                            startFrom = new HashMap<String, String>();
                            endTo = new HashMap<String, String>();
                            middleList = new ArrayList<Map<String,String>>();
                            middleMap = new HashMap<String, String>();

                            flight.put("companyCode", flightNoVo.getCompanyCode());
                            flight.put("airCompany", flightNoVo.getCompanyName());
                            //组装前台的航空公司 条件帅选
                            if(flightNoVo.getCompanyName()!= null && !"null".equalsIgnoreCase(String.valueOf(flightNoVo.getCompanyName())) &&!airCompanies.contains(flightNoVo.getCompanyName())){
                                airCompanies.add(flightNoVo.getCompanyName());
                            }

                            // flight.put("logo", "logo");
                            flight.put("flightNo", flightNoVo.getFlightNo());
                            flight.put("planStyle", flightNoVo.getPlaneCode());
                            //机型信息
                            flight.put("styleName", "");// 机型名称
                            flight.put("planeStyle", "");// 窄体，宽体
                            flight.put("leastSeats", "");// 最少座位
                            flight.put("mostSeats", "");// 最多座位
                            flight.put("totalTime", flightNoVo.getFlyTimeStr());

							/*线路后台下单机票短信改造增加的变量--起*/
							flight.put("planeCode", flightNoVo.getPlaneCode());
							//飞行时间，单位是分钟
							flight.put("flyTime", flightNoVo.getFlyTime());
							//起飞/到达城市
							flight.put("fromCityName", flightNoVo.getFromCityName());
							flight.put("toCityName", flightNoVo.getToCityName());
							//起飞/到达航站楼
							flight.put("startTerminal", flightNoVo.getStartTerminal());
							flight.put("arriveTerminal", flightNoVo.getArriveTerminal());
							/*线路后台下单机票短信改造增加的变量--止*/

                            int hour = flightNoVo.getGoTime().getHours();
                            String period = "";
                            if(hour >= 6 && hour <12){
                                period = "上午";
                            }if(hour >=12 && hour< 13){
                                period="中午";
                            }if(hour>=13 && hour<18){
                                period="下午";
                            }if(hour>=18 && hour<24){
                                period = "晚上";
                            }
                            startFrom.put("starttype", period);
                            startFrom.put("starttime", DateUtil.formatDate(flightNoVo.getGoTime(), "HH:mm"));
                            startFrom.put("airport", flightNoVo.getFromAirPort());

                            endTo.put("airport", flightNoVo.getToAirPort());
                            endTo.put("datetime", DateUtil.formatDate(flightNoVo.getArriveTime(), "HH:mm"));

                            seatMap = new HashMap<String, String>();
                            seatMap.put("seat", flightNoVo.getSeatName());
                            seatMap.put("code", flightNoVo.getSeatCode());
                            seatMap.put("groupId", group.getGroupId()+"");
                            seatMap.put("branchId",branchVo.getBranchId()+"");
                            seatMap.put("goodId", flightNoVo.getGoodsId()+"");
                            long adPrice = (flightNoVo.getAdultAmt()==null?0:flightNoVo.getAdultAmt() );
                            seatMap.put("price",  adPrice+ "");
                            long clPrice = flightNoVo.getChildAmt()==null?0:flightNoVo.getChildAmt();
                            seatMap.put("childPrice", clPrice + "");
                            long ticketRemain = flightNoVo.getRemain()==null?0:flightNoVo.getRemain();
                            seatMap.put("ticketLeft", ticketRemain + "");
                            seatMap.put("priceDiff", (adPrice*adultNum + clPrice*childNum - oldPrice)/100 +"");
                            seatMap.put("food", flightNoVo.isFoodSupport() ? "Y" : "N");
                            seatMap.put("trafficId",trafficVo.getTrafficId()+"");
                            seatMap.put("detailId", ""+branchVo.getDetailId());
                            seatMap.put("flightType", flightType);

                            //如果 有儿童数，且该商品儿童价不为null，那么加入航班 ;或者没有选择儿童
                            if((childNum != 0 && flightNoVo.getChildAmt() != null)  || childNum == 0 ){
                                seatList.add(seatMap);
                            }



                            //中转站
                            List<FlightNodeVo> flightNodeVoList = flightNoVo.getFlightNodeVoList();
                            for(int d=0;flightNodeVoList!=null && d< flightNodeVoList.size();d++){
                                FlightNodeVo node = flightNodeVoList.get(d);
                                middleMap = new HashMap<String, String>();
                                middleMap.put("city", node.getCity());
                                middleMap.put("d", d+"");
                                middleMap.put("airport", node.getAirport());
                                middleMap.put("arriveTime", DateUtil.formatDate(node.getArriveTime(), "yyyy-MM-dd HH:mm"));
                                middleMap.put("goTime", DateUtil.formatDate(node.getGoTime(), "yyyy-MM-dd HH:mm"));
                                if(middleList!=null&&!middleList.contains(middleMap)){
                                	middleList.add(middleMap);
                                }
                            }

                            flight.put("startFrom", startFrom); // flight 加入飞机起飞信息
                            flight.put("middle", middleList); // 中转信息
                            flight.put("seatInfo", seatList); // 舱位信息
                            flight.put("endTo", endTo); // 飞机降落信息

                            //bf.getStartTime()==null?null:DateUtil.toDate(bf.getStartTime(), "hh:mm")
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            String goTime = flightNoVo.getGoTime()==null?null:sdf.format(flightNoVo.getGoTime());
                            flight.put("selectedFlag", goTime + flightNoVo.getFlightNo() + flightNoVo.getGoodsId());

                            if((goTime + flightNoVo.getFlightNo()+flightNoVo.getGoodsId()).equals(selectedFlag)){
                                List<Map<String,String>> list = (List<Map<String, String>>) flight.get("seatInfo");
                                flight.put("selected", list.size() - 1 );
                                flights.add(flight);
                                thisIndex = flights.size() - 1;
                            }else{
                                flights.add(flight);
                            }

                        } else {
                            List<Map<String,String>> list = (List<Map<String, String>>) oldFlight.get("seatInfo");
                            seatMap = new HashMap<String, String>();
                            seatMap.put("seat", flightNoVo.getSeatName());
                            seatMap.put("code", flightNoVo.getSeatCode());
                            seatMap.put("groupId", group.getGroupId()+"");
                            seatMap.put("branchId",branchVo.getBranchId()+"");
                            seatMap.put("goodId", flightNoVo.getGoodsId()+"");

                            long adPrice = (flightNoVo.getAdultAmt()==null?0:flightNoVo.getAdultAmt() );
                            long clPrice = flightNoVo.getChildAmt()==null?0:flightNoVo.getChildAmt();
                            seatMap.put("price", adPrice + "");
                            seatMap.put("childPrice", clPrice + "");
                            long ticketRemain = flightNoVo.getRemain()==null?0:flightNoVo.getRemain();
                            seatMap.put("ticketLeft", ticketRemain + "");
                            seatMap.put("priceDiff", (adPrice*adultNum + clPrice*childNum - oldPrice)/100 + "");
                            seatMap.put("food", flightNoVo.isFoodSupport() ? "Y" : "N");
                            seatMap.put("trafficId",trafficVo.getTrafficId()+"");
                            seatMap.put("detailId", ""+branchVo.getDetailId());
                            seatMap.put("flightType", flightType);

                            //如果 有儿童数，且该商品儿童价不为null，那么加入航班 ;或者没有选择儿童
                            if((childNum != 0 && flightNoVo.getChildAmt() != null)  || childNum == 0 ){
                                list.add(seatMap);
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            String goTime = flightNoVo.getGoTime()==null?null:sdf.format(flightNoVo.getGoTime());
                            if((goTime + flightNoVo.getFlightNo()+flightNoVo.getGoodsId()).equals(selectedFlag)){
                                list = (List<Map<String, String>>) oldFlight.get("seatInfo");
                                oldFlight.put("selected", list.size() - 1 );
                                thisIndex = flights.indexOf(oldFlight);
                            }
                        }
                    }
                }

            }

        if(spaceList.size()<=0){
            spaceList.addAll(seatName);
        }
        Map<String,Object> theflight = null ;
        if(thisIndex>=1){
            theflight = flights.get(thisIndex);
            flights.remove(thisIndex);
            flights.add(0,theflight);
        }else{
        	if(flights!=null && flights.size()>0){
        		theflight = flights.get(0);
        	}
        }

        //把默认选中的座位放第一
      if(theflight != null ){
          try{
              int seatIndex = ((Integer)theflight.get("selected")).intValue();
              if(seatIndex != 0 ){
                  List<Map<String,String>> theSeatInfos = (List<Map<String, String>>) theflight.get("seatInfo");
                  Map<String,String> theSeat = theSeatInfos.get(seatIndex);
                  theSeatInfos.remove(seatIndex);
                  theSeatInfos.add(0, theSeat);
                  theflight.put("selected", 0);
              }
          }catch(Exception e){
              LOG.error("获取到的座位，不能转成int 类型。 ");
          }
      }


        //最终返回的航班
        List<Map<String, Object>> lastflights = new ArrayList<Map<String,Object>>();
        //因为儿童价为null的不能加入到航班中，会导致可能航班中没有座位，所以要将航班中没有座位的航班去除
        for(Map<String,Object> flightTemp : flights){
            List<Map<String,String>> listTemp = (List<Map<String, String>>) flightTemp.get("seatInfo");
            if(listTemp != null && listTemp.size() > 0){
                lastflights.add(flightTemp);
            }
        }
        //释放
        flights = null ;
        //每个舱位只取价格最低的
        if(CollectionUtils.isNotEmpty(lastflights)){
        	for (Map<String, Object> map1 : lastflights) {
        		List<Map<String, String>> seatListt = (List<Map<String, String>>) map1.get("seatInfo");

        		if(CollectionUtils.isNotEmpty(seatListt)){
        			Collections.sort(seatListt,new Comparator<Map<String, String>>() {
        				@Override
        				public int compare(
        						Map<String, String> o1,
        						Map<String, String> o2) {
        					if(Long.valueOf(o1.get("priceDiff")) >= Long.valueOf(o2.get("priceDiff"))){
        						return 1;
        					}else{
        						return -1;
        					}
        				}
        			});
        		}
        		List<Map<String, String>> seatListtTemp = new ArrayList<Map<String,String>>();
        		seatListtTemp.add(seatListt.get(0));
        		map1.put("seatInfo", seatListtTemp);
			}
        }
        return lastflights;
    }
    /** 是否是同一架飞机
     * @param flights
     * @param key
     * @return
     * @author
     */
    private Map<String, Object> getAirCompanyInMap(List<Map<String, Object>> flights,String key ){
        for(int i=0;i<flights.size();i++){
            Map<String,Object> flight = flights.get(i);
            if(key.equals(flight.get("flightNo"))){
                return flight;
            }

        }
        return null;
    }

    /**ajax 获取机型信息
     * @param model
     * @param planStyle  机型名称
     * @return
     * @throws UnsupportedEncodingException
     * @author
     */
    @RequestMapping("/ord/order/getAirplaneJXinfo.do")
    @ResponseBody
    public String getAirplaneJXinfo(Model model ,String planStyle) throws UnsupportedEncodingException{
        //根据飞机去remote获取飞机机型信息
        HttpServletRequest request = getRequest();
        Map<String,String> planeJXInfo = new HashMap<String,String>();
        String jsonstr ="";
        planStyle = URLDecoder.decode(planStyle,"UTF-8");
        try{
            long d1 = new Date().getTime();
            PlaneTypeInfo planTypeInfo = airlineService.findPlaneTypeByCode(planStyle);
            long d2 = new Date().getTime();
            LOG.debug("查询机型-" + planStyle + "信息，耗时" + (d2-d1));

            if(planTypeInfo!=null){
                planeJXInfo.put("planStyle", planStyle);
                planeJXInfo.put("styleName", planTypeInfo.getName());
                planeJXInfo.put("planeStyle", planTypeInfo.getTypeDesp());
                planeJXInfo.put("leastName", planTypeInfo.getMinSeats()+"");
                planeJXInfo.put("mostSeats", planTypeInfo.getMaxSeats()+"");
                JSONArray jsonArray = JSONArray.fromObject(planeJXInfo);
                jsonstr = jsonArray.toString();
            }
        }catch(Exception e){
            LOG.error("获取机型信息失败");
        }
        return jsonstr;
    }

    /**
     * 根据出游时间限制，过滤当地游
     */
	private void filterOptinalRouteLocal(ProdProduct product,
			List<String> selectDateList, Map<String,String> selectDateMap,
			String limitDays,  Date visitDate) {
		long categoryId = product.getBizCategoryId();
		long subCategoryId = product.getSubCategoryId() == null ? 0L : product.getSubCategoryId();

		//跟团游
		boolean isRouteGroup = BIZ_CATEGORY_TYPE.category_route_group
				.getCategoryId() == categoryId;

		//当地游
		boolean isLocalTour = BIZ_CATEGORY_TYPE.category_route_local
						.getCategoryId() == categoryId;

		// 机+酒
		boolean isRouteFlightPlusHotel = BIZ_CATEGORY_TYPE.category_route_freedom
				.getCategoryId() == categoryId
				&& BIZ_CATEGORY_TYPE.category_route_flight_hotel
						.getCategoryId() == subCategoryId;
		//交通+服务
		boolean isRouteTrafficPlusService = BIZ_CATEGORY_TYPE.category_route_freedom
				.getCategoryId() == categoryId
				&& BIZ_CATEGORY_TYPE.category_route_traffic_service
						.getCategoryId() == subCategoryId;
		List<String> availables = formatLimtDays(limitDays, visitDate);

		if (LOG.isInfoEnabled()) {
			LOG.info("productId:" + product.getProductId() + " isRouteGroup:" + isRouteGroup
					+ " isRouteFlightPlusHotel:" + isRouteGroup + " isRouteTrafficPlusService:" + isRouteGroup);
		}

		if (isRouteGroup || isLocalTour || isRouteFlightPlusHotel || isRouteTrafficPlusService) {
			//判断事由有限制条件
			if (!availables.isEmpty()) {
				if(selectDateList != null) {
					for (String date : selectDateList.toArray(new String[0])) {
						if (!availables.contains(date)) {
							selectDateList.remove(date);
						}
					}
				}
				if(selectDateMap != null) {
					for (String date : selectDateMap.keySet().toArray(new String[0])) {
						if (!availables.contains(date)) {
							selectDateMap.remove(date);
						}
					}
				}
			}
		}
	}

	private List<String> formatLimtDays(String limitDays, Date visitDate) {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isNotBlank(limitDays)) {
			String[] arr = limitDays.trim().split(",");
			for (String str : arr) {
				if (NumberUtils.isDigits(str)) {
					Date optionalDate = DateUtils.addDays(visitDate,
							Integer.valueOf(str) - 1);
					list.add(DateUtil.formatSimpleDate(optionalDate));
				}
			}
		}
		return list;
	}

	//将某商品是对接，非对接的标记封装成map,传到前台
	private void assembleDJFlag(Map<String, String> suppGoodsDJFlagMap, TrafficGroupVo trafficGroupVo, String isDJ) {
		if (trafficGroupVo != null) {
			List<TrafficVo> trafficVoList = trafficGroupVo.getTrafficVoList();
			if (trafficVoList != null) {
				for (TrafficVo trafficVo : trafficVoList) {
					List<BranchVo> branchVoList = trafficVo.getBranchVoList();
					if (branchVoList != null) {
						for (BranchVo branchVo : branchVoList) {
							List<FlightNoVo> flightNoVoList = branchVo.getGoodsList();
							if(flightNoVoList != null) {
								for (FlightNoVo flightNoVo : flightNoVoList) {
									suppGoodsDJFlagMap.put(String.valueOf(flightNoVo.getGoodsId()), isDJ);
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 *
	 * @param propValueList
	 * @param destpropValueList  酒店子后台的数据类型
	 */
	private void copyListPropValues(List<com.lvmama.dest.comm.po.prod.PropValue> destpropValueList,List<PropValue> propValueList) {
		if(propValueList==null) propValueList=new ArrayList();
		if(destpropValueList!=null &&destpropValueList.size()>0){
			PropValue vstPropValue=new PropValue();
			for(com.lvmama.dest.comm.po.prod.PropValue destPropValue:destpropValueList){
				BeanUtils.copyProperties(destPropValue,vstPropValue);
				propValueList.add(vstPropValue);
			}
		}
	}

	/**
	 * vo中的Person对象转换成po中的OrdPerson对象。
	 * 
	 * @param person
	 * @return
	 */
	private OrdPerson getOrdPersonFromPerson(Person person) {
		OrdPerson ordPerson = null;

		if (person != null) {
			ordPerson = new OrdPerson();
			ordPerson.setEmail(person.getEmail());
			ordPerson.setFax(person.getFax());
			ordPerson.setFirstName(person.getFirstName());

			ordPerson.setGender(person.getGender());

			ordPerson.setLastName(person.getLastName());
			ordPerson.setMobile(person.getMobile());
			ordPerson.setNationality(person.getNationality());
			ordPerson.setPhone(person.getPhone());
			ordPerson.setIdNo(person.getIdNo());
			ordPerson.setIdType(person.getIdType());
			
			ordPerson.setPeopleType(person.getPeopleType());
			//导游证号
			ordPerson.setGuideCertificate(person.getGuideCertificate());
			//交通接驳 出境手机号
			ordPerson.setOutboundPhone(person.getOutboundPhone());
			if(person.getBirthday() != null && !"".equals(person.getBirthday().trim())) {
				ordPerson.setBirthday(DateUtil.toDate(person.getBirthday().trim(), "yyyy-MM-dd"));
			}

			ordPerson.setPassportUrl(person.getPassportUrl());

			//台胞证和回乡证设置签发地和有效期
			if(!StringUtil.isEmptyString(person.getIdType())){
//
//				if(person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name())||
//						person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name())){
//					
					//有效期
					if(person.getExpDate() != null && !"".equals(person.getExpDate())) {
						ordPerson.setExpDate(person.getExpDate());
					}
					//签发地
					ordPerson.setIssued(person.getIssued());
//				}
					//出生地
					ordPerson.setBirthPlace(person.getBirthPlace());
					//签发日期
					if(person.getIssueDate() != null && !"".equals(person.getIssueDate())) {
						ordPerson.setIssueDate(person.getIssueDate());
					}
			}
			// 设置全名
			boolean isChineseName = false;
			String lastName = ordPerson.getLastName();
			String firstName = ordPerson.getFirstName();
			if (lastName != null) {
				if (StringUtil.hasChinese(lastName)) {
					isChineseName = true;
				}
			}

			if (!isChineseName && (firstName != null)) {
				if (StringUtil.hasChinese(firstName)) {
					isChineseName = true;
				}
			}
			ordPerson.setFullName(person.getFullName());
			if(StringUtils.isEmpty(person.getFullName())){
				if (isChineseName) {
					StringBuffer sb=new StringBuffer();
					if(StringUtils.isNotEmpty(lastName)){
						sb.append(lastName);
					}
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					ordPerson.setFullName(sb.toString());
				} else {
					StringBuffer sb=new StringBuffer();
					
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					if(StringUtils.isNotEmpty(lastName)){
						if(sb.length()>1){
							sb.append("/");
						}
						sb.append(lastName);
					}
					ordPerson.setFullName(sb.toString());
				}
			}
			
//			if(!FieldValidUtils.checkFieldLength(ordPerson.getFullName(), 150)){
//				throwIllegalException("游玩人中文姓名长度过长。");
//			}
		}

		return ordPerson;
	}

	/**
	 * 后台下单，创建订单
	 */
	@RequestMapping(value = "/ord/order/lineBackCreateNewOrderResponseBody.do")
	@ResponseBody
	public Object createNewOrderResponseBody(Model model, BuyInfo buyInfo,
			HttpServletResponse response, HttpServletRequest req, @RequestParam(required=false)String travellerDelayFlag) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			/******************************/
			Long startTime = null;
			String methodName = "OrderLineProductQueryAction#createNewOrderResponseBody【"+ buyInfo.getProductId() +"】";

//			List<Person> personList = buyInfo.getTravellers();
//			// 页面传进来的时候，tracellers设置为空
//			buyInfo.setTravellers(null);


			// 组装BuyInfo
			startTime = System.currentTimeMillis();
			//放入一键下单标志位
			String originalOrderId = req.getParameter("originalOrderId");
			if(StringUtils.isNotBlank(originalOrderId)){
				buyInfo.setOrderCreatingManner(OrderEnum.ORDER_CREATING_MANNER.backOneKeyRecreating.getCode());
			}
			createBuyInfo(buyInfo, req);

			//转换新的promotionMap
			changeNewPromotionNewMap(buyInfo);

			if(StringUtils.isNotBlank(originalOrderId)){
				//一键重下订单重新设置渠道
				Long oldDistributorId= NumberUtils.toLong(req.getParameter("oldDistributorId"));
				Long oldDistributionChannel=NumberUtils.toLong(req.getParameter("oldDistributionChannel"));
				String oldDistributorCode=req.getParameter("oldDistributorCode");
				if(oldDistributorId.longValue()!=0L){
					buyInfo.setDistributionId(oldDistributorId);
					buyInfo.setDistributorCode(oldDistributorCode);
					if(oldDistributionChannel.longValue()!=0L){
						buyInfo.setDistributionChannel(oldDistributionChannel);
					}
				}
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "组装BuyInfo",
					"this.createBuyInfo", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			initBooker(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "初始化 buyInfo信息",
					"this.initBooker", (System.currentTimeMillis()-startTime)));

			startTime = System.currentTimeMillis();
			buyInfo.setIp(InternetProtocol.getRemoteAddr(req));
			ResultHandleT<OrdOrder> orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
			LOG.info(ComLogUtil.printTraceInfo(methodName, "订单生成接口",
					"orderService.createOrder", (System.currentTimeMillis()-startTime)));


			if (orderHandle.isFail()) {
				msg.raise(orderHandle.getMsg());
				return msg;
			}
			OrdOrder ordOrder = orderHandle.getReturnContent();
			msg.addObject("orderId", ordOrder.getOrderId());
			msg.addObject("userId", ordOrder.getUserId());


//			if (personList != null && !personList.isEmpty()) {
//				List<OrdPerson> ordTravellerList = new ArrayList<OrdPerson>();
//				OrdPerson ordPerson = null;
//				for (Person traveller : personList) {
//					// 从vo的Person对象转换成po的OrdPerson对象。
//					ordPerson = getOrdPersonFromPerson(traveller);
//					
//					if (ordPerson != null) {
//						// 设置游玩人与订单关联
//						ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
//						// 设置游玩人类型。
//						ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
//						OrderUtils.resetPersonInfo(ordPerson);
//						ordTravellerList.add(ordPerson);
//					}
//				}
//				orderSaveService.doSavePerson(ordOrder.getOrderId(), ordTravellerList);
//			}

			//意向单号
			String intentionId =req.getParameter("intentionOrderId");

			startTime = System.currentTimeMillis();
			orderTravelContractDataServiceFactory.createTravelContractDataService(ordOrder);
			LOG.info(ComLogUtil.printTraceInfo(methodName, "生成合同接口bean",
					"orderTravelContractDataServiceFactory.createTravelContractDataService",
					(System.currentTimeMillis()-startTime)));

			//***********意向单订单*************
			if(StringUtils.isNotEmpty(intentionId)){
				startTime = System.currentTimeMillis();
				//把订单号更新到意向单表中
				IntentionOrder intentionOrder = new IntentionOrder();
				intentionOrder.setOrderId(ordOrder.getOrderId());
				intentionOrder.setIntentionOrderId(Long.valueOf(intentionId));
				//更新已下单
				intentionOrder.setState("1");
				orderIntentionService.updateIntention(intentionOrder);
				LOG.info(ComLogUtil.printTraceInfo(methodName, "意向单获取与修改 ",
						"orderIntentionService.queryIntentionsByCriteria/orderIntentionService.updateIntention", (System.currentTimeMillis()-startTime)));
			}
			LOG.info(ComLogUtil.printTraceInfo(methodName, "整个创建订单耗费时间",
					"createOrder", (System.currentTimeMillis()-startTime)));
		} catch (Exception e) {
			LOG.error("{}", e);
			msg.raise("下单错误，请检查订单项价格，数量是否正确");
		}
		return msg;

	}
	
	/**
	 * 锁仓前置产品发起锁仓
	 * @param form
	 * @return
	 */
	@RequestMapping("/ord/order/preLockSeat.do")
	@ResponseBody
	public Object preLockSeat(BuyInfo buyInfo, HttpServletRequest req){
		ResultHandleT<Object> resultHandleT=new ResultHandleT<>();
		
		createBuyInfo(buyInfo, req);
		//转换新的promotionMap
		changeNewPromotionNewMap(buyInfo);
		initBooker(buyInfo);
		buyInfo.setIp(InternetProtocol.getRemoteAddr(req));
		
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(buyInfo != null){
			List<Person> travellers = buyInfo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}		
		
		try {
			resultHandleT = orderService.preLockSeat(buyInfo, getLoginUserId());
		} catch (Exception e) {
			LOG.info("{}", e);
			resultHandleT.setMsg("前置锁舱通知失败");
		}
		return resultHandleT;
	}	
	
	/**
	 * 锁仓前置产品查询锁仓结果
	 * @param form
	 * @return
	 */
	@RequestMapping("/ord/order/queryLockSeatResult.do")
	@ResponseBody
	public Object queryLockSeatResult(String lockSetOrderId){
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			LOG.info("queryLockSeatResult:lockSetOrderId  "+lockSetOrderId);
			ResultHandleT<Object> resultHandleT=new ResultHandleT<>();
			resultHandleT = orderService.queryLockSeatResult(lockSetOrderId);
			if(resultHandleT==null){
				msg.raise("锁仓失败");
			}else{
				String token = UUID.randomUUID().toString()+getLoginUserId();
				MemcachedUtil.getInstance().set(token, 60, lockSetOrderId);
				msg.setMessage(token);
			}
		} catch (Exception e) {
			msg.raise(e.getMessage());
		}
		return msg;
	}
	
	
	//自由行 机酒 列表展示
	@SuppressWarnings("all")
	private void assembleHotelGroup(Map<String, Object> assembleMap) {

		List<ProdPackageGroup> prodPackageGroupList = (List<ProdPackageGroup>) assembleMap.get("prodPackageGroupList");
		Date selectDate = (Date) assembleMap.get("selectDate");
		List<Map> hotelList = (List<Map>) assembleMap.get("hotelList");
		Long productBranchId = (Long) assembleMap.get("productBranchId");
		Long productId = (Long) assembleMap.get("productId");
		Long groupId = (Long) assembleMap.get("groupId");
		String selectedQuantity =  (String) assembleMap.get("selectedQuantity");
		Long detailId = (Long) assembleMap.get("detailId");
		final Long selectedSuppgoodsid = (Long) assembleMap.get("selectedSuppgoodsid");
		
	    for(ProdPackageGroup prodPackageGroup : prodPackageGroupList){
	        List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
	        if(prodPackageDetails != null && prodPackageDetails.size() > 0){
	        	
	        	Map<Long, Object>mapTemp = new HashMap<Long, Object>();
	        	for (ProdPackageDetail detail : prodPackageDetails) {
	        		if(detail.getProdProduct()!=null){
	        			Long productIdTemp = detail.getProdProduct().getProductId();
	        			if(productIdTemp!=null){
	        				mapTemp.put(productIdTemp, "t");        				
	        			}
	        		}
				}
	        	
	        	ProdPackageDetail prodPackageDetail = null;
	        	ProdProduct hotelProdProduct = null;
	        	
	        	//如果交通组ID不等于所传组ID，则过滤，保证只有一条数据 
	        	if(prodPackageGroup!=null && groupId!=null
	        			&& prodPackageGroup.getGroupId()!=null
	        			&& groupId.intValue() != prodPackageGroup.getGroupId().intValue()){
	        		continue;
	        	}
	        	//如果参数存在productId则表示是更换商品
	        	if(productId!=null && productBranchId!=null){
	        		for (ProdPackageDetail prodPackageDetail2 : prodPackageDetails) {
	        			if(prodPackageDetail2!=null && prodPackageDetail2.getProdProduct()!=null && 
	        					prodPackageDetail2.getProdProduct().getProductId()!=null &&
	        					productId.intValue() == prodPackageDetail2.getProdProduct().getProductId().intValue() && 
	        							prodPackageDetail2.getProdProductBranch()!=null && 
	        							prodPackageDetail2.getProdProductBranch().getProductBranchId()!=null &&
	        							productBranchId.intValue() == prodPackageDetail2.getProdProductBranch().getProductBranchId().intValue()){
	        				prodPackageDetail = prodPackageDetail2;
	        			}
	        		}
	        	}else{
	        		//默认加载逻辑
	        		prodPackageDetail = prodPackageDetails.get(0);
	        	}
	        	
	        	//得到打包的酒店产品
	        	hotelProdProduct = prodPackageDetail.getProdProduct();
	            //得到酒店的属性
	            Map<String,Object> hotelMap = hotelProdProduct.getPropValue();
	            String hotelAddress = "";
	            if(hotelMap != null){
	                hotelAddress = (String)hotelMap.get("address");
	            }
	            ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
	            if(prodProductBranch != null){
	                List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();
	                
	                List<Map>fangXinList = new ArrayList<Map>();
	                
	                Map map1 = new HashMap();
	                if(mapTemp.size() > 1){
	                	map1.put("haveChangeButton","Y");//这一个组里有的可换的产品总数
	                }else{
	                	map1.put("haveChangeButton","N");//这一个组里有的可换的产品总数
	                }
	                ProdPackageGroupHotel prodPackageGroupHotel1 =  prodPackageGroup.getProdPackageGroupHotel();//本身打包的就是酒店的东西就在这里
	                //生成可订的日期
	                String dayStrs = prodPackageGroupHotel1.getStayDays();

	                Date startDates=selectDate;
	                if(dayStrs != null && !"".equals(dayStrs)){
	                    String dayArray[] = dayStrs.split(",");
	                    if(dayArray != null && dayArray.length > 0){
	                        startDates = DateUtils.addDays(selectDate,Integer.valueOf(dayArray[0])-1);
	                    }
	                }
	                
	                Map<String,List<Map>> branchNameGoodsMap = new LinkedHashMap<String, List<Map>>();
	                for (ProdPackageDetail detail : prodPackageDetails) {
	    				if(prodPackageDetail.getProdProduct().getProductId().intValue() == detail.getProdProduct().getProductId().intValue()){
	    					List<Map> fangxingMap = fangxinMore(detail,false,selectedSuppgoodsid,startDates,prodPackageGroup.getProdProduct(),selectedQuantity);
	    					branchNameGoodsMap.put(detail.getProdProductBranch().getBranchName(),fangxingMap);
	    				}
	    			}
	                fangXinList.add(branchNameGoodsMap);	
	                
	               // selectedSuppgoodsid不为空则表示当前为更换商品，需要将当前选中规格、商品放到第一个位置
	                Map<String,List<Map>> hotelGoodsMap = new LinkedHashMap<String, List<Map>>();
					if (branchNameGoodsMap.size() > 0 && selectedSuppgoodsid != null) {
						Iterator entries = branchNameGoodsMap.entrySet().iterator(); 
						while (entries.hasNext()) {  
						    Map.Entry entry = (Map.Entry) entries.next();  
						    String hotelType = (String)entry.getKey();  
						    List<Map> suppGoodsLists = (List<Map>)entry.getValue();
						    for (int i = 0; i < suppGoodsLists.size(); i++) {
								Map fangxin = suppGoodsLists.get(i);
								Long detailIdTemp = (Long) fangxin.get("detailId");
								Long suppGoodsIdTemp = (Long) fangxin.get("suppGoodsId");
								if(detailId != null && detailIdTemp.intValue() == detailId.intValue()
										&& suppGoodsIdTemp != null && suppGoodsIdTemp.intValue() == selectedSuppgoodsid.intValue()){
									suppGoodsLists.remove(fangxin);
									suppGoodsLists.add(0, fangxin);
									hotelGoodsMap.put(fangxin.get("branchName").toString(), suppGoodsLists);
									map1.put("defaultSuppGoodsId", suppGoodsIdTemp);
									entries.remove();
								}
							}
						  
						}
						
						
						for (Object hotelType : branchNameGoodsMap.keySet()) {
							List<Map> suppGoodsLists = (List<Map>) branchNameGoodsMap.get(hotelType);
							hotelGoodsMap.put(hotelType.toString(), suppGoodsLists);
						}

						fangXinList.clear();	
						fangXinList.add(hotelGoodsMap);		
					}
					
					
	                
					map1.put("fangXinList",	fangXinList);	

	                {
		                ProdPackageGroupHotel prodPackageGroupHotel =  prodPackageGroup.getProdPackageGroupHotel();//本身打包的就是酒店的东西就在这里
		                //生成可订的日期
		                String dayStr = prodPackageGroupHotel.getStayDays();
		
		                String checkInDate = "";
		                String checkOutDate = "";
		                if(dayStr != null && !"".equals(dayStr)){
		                    String dayArray[] = dayStr.split(",");
		                    if(dayArray != null && dayArray.length > 0){
		                        map1.put("days",dayArray.length);//入住几晚
		                        Date startDate = DateUtils.addDays(selectDate,Integer.valueOf(dayArray[0])-1);
		                        //入住日期一般与所选日期一致
		                        checkInDate = DateUtil.formatSimpleDate(startDate);
		                        //在连续入住的情况下,离店日期
		                        checkOutDate =  DateUtil.formatSimpleDate(DateUtils.addDays(startDate,dayArray.length));
		                    }
		                }
		                map1.put("check_in",checkInDate);
		                map1.put("check_out",checkOutDate);
		                map1.put("selectedQuantity", selectedQuantity);
		                
		                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		                try {
							Date stdate = sdf.parse(checkInDate);
							Date etdate = sdf.parse(checkOutDate);  
							map1.put("week_in", DateUtil.getZhouDay(stdate));
							map1.put("week_out", DateUtil.getZhouDay(etdate));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                map1.put("hotel_name",prodPackageDetail.getProdProduct().getProductName());
		                List<String> starList=  (List<String>) prodPackageDetail.getProdProduct().getPropValue().get("star_rate");
		                if(starList == null || starList.isEmpty()) {
		                	starList = new ArrayList<String>();
		                	starList.add("其他");
		                }
		                map1.put("star",starList);
		                map1.put("hotel_address",hotelAddress);
	                    map1.put("groupId",prodPackageGroup.getGroupId());
	                    map1.put("packge_type",prodPackageDetail.getProdProduct().getPackageType());
	                    map1.put("productId",prodPackageDetail.getProdProduct().getProductId());
	                    map1.put("detailId",prodPackageDetail.getDetailId());
	                    map1.put("pid", prodPackageGroup.getProductId());
	                    
	                    if(productBranchId!=null){//productBranchId不为空则表示为更换商品，需要指定为当前选中规格，否则指定为第一个产品规格规格ID
	                    	map1.put("currentProductBranchId",productBranchId);//指定为当前选中规格
	                    }else{
	                    	map1.put("currentProductBranchId",prodProductBranch.getProductBranchId());
	                    }
	                    map1.put("groupDivId",prodPackageGroup.getGroupId()+"_"+prodProductBranch.getProductBranchId());

	                }
	                hotelList.add(map1);
	            }
	        }
	    }
	}
	
	
	
	/**
	 * 机酒 更多酒店打包
	 * @param assembleMap
	 */
	@SuppressWarnings("all")
	private void moreAssembleHotelGroup(Map<String, Object> assembleMap) {

		List<ProdPackageGroup> prodPackageGroupList = (List<ProdPackageGroup>) assembleMap.get("prodPackageGroupList");
		Date selectDate = (Date) assembleMap.get("selectDate");
		List<Map> hotelList = (List<Map>) assembleMap.get("hotelList");
		Long productBranchId = (Long) assembleMap.get("productBranchId");
		Long productId = (Long) assembleMap.get("productId");
		Long groupId = (Long) assembleMap.get("groupId");
		String selectedQuantity =  (String) assembleMap.get("selectedQuantity");
		Long detailId = (Long) assembleMap.get("detailId");
		final Long selectedSuppgoodsid = (Long) assembleMap.get("selectedSuppgoodsid");
		
	    for(ProdPackageGroup prodPackageGroup : prodPackageGroupList){
	        List<ProdPackageDetail> prodPackageDetails = prodPackageGroup.getProdPackageDetails();
	        if(prodPackageDetails != null && prodPackageDetails.size() > 0){
	        	
	            Map<Long, Object> mapInfo = new HashMap<Long, Object>();
	            for (ProdPackageDetail detail : prodPackageDetails) {
    	            	if(detail.getProdProduct()!=null){
    	            	Long productIdTemp = detail.getProdProduct().getProductId();
    	            	    if(productIdTemp!=null){
    	            		mapInfo.put(productIdTemp, detail);        				
    	            	    }
    	            	}
	            }
	            int index = 0;
	            for(Map.Entry<Long, Object> mapEntry : mapInfo.entrySet()) {
	        	Long pid = mapEntry.getKey();
	        	ProdPackageDetail prodPackageDetail = (ProdPackageDetail) mapEntry.getValue();
	        	//得到打包的酒店产品
	        	ProdProduct hotelProdProduct = prodPackageDetail.getProdProduct();
	        	//得到酒店的属性
		        Map<String,Object> hotelMap = hotelProdProduct.getPropValue();
		        String hotelAddress = "";
		        if(hotelMap != null){
		            hotelAddress = (String)hotelMap.get("address");
		        }
		        ProdProductBranch prodProductBranch = prodPackageDetail.getProdProductBranch();
		        if(prodProductBranch != null){
		            List<SuppGoods> suppGoodsList = prodProductBranch.getRecommendSuppGoodsList();
		                
		            List<Map>fangXinList = new ArrayList<Map>();
		                
		            Map map1 = new HashMap();
		            if(mapInfo.size() > 1){
		                map1.put("haveChangeButton","Y");//这一个组里有的可换的产品总数
		            }else{
		                map1.put("haveChangeButton","N");//这一个组里有的可换的产品总数
		            }
		            ProdPackageGroupHotel prodPackageGroupHotel1 =  prodPackageGroup.getProdPackageGroupHotel();//本身打包的就是酒店的东西就在这里
		            //生成可订的日期
		            String dayStrs = prodPackageGroupHotel1.getStayDays();

		            Date startDates=selectDate;
		            if(dayStrs != null && !"".equals(dayStrs)){
		                String dayArray[] = dayStrs.split(",");
		            if(dayArray != null && dayArray.length > 0){
		                startDates = DateUtils.addDays(selectDate,Integer.valueOf(dayArray[0])-1);
		             	}
		            }
		            Map<String,List<Map>> branchNameGoodsMap = null;
		                
		            branchNameGoodsMap = new LinkedHashMap<String, List<Map>>();
		            for (ProdPackageDetail detail : prodPackageDetails) {
		                if(pid.intValue() == detail.getProdProduct().getProductId().intValue()){
	    			    List<Map> fangxingMap = fangxinMore(detail,false,selectedSuppgoodsid,startDates,prodPackageGroup.getProdProduct(),selectedQuantity);
	    			    branchNameGoodsMap.put(detail.getProdProductBranch().getBranchName(),fangxingMap);
	    			}
		            }
		            fangXinList.add(branchNameGoodsMap);
			    map1.put("fangXinList",	fangXinList);	
		            {
			        ProdPackageGroupHotel prodPackageGroupHotel =  prodPackageGroup.getProdPackageGroupHotel();//本身打包的就是酒店的东西就在这里
			        //生成可订的日期
			        String dayStr = prodPackageGroupHotel.getStayDays();
			        String checkInDate = "";
			            String checkOutDate = "";
			            if(dayStr != null && !"".equals(dayStr)){
			                String dayArray[] = dayStr.split(",");
			                if(dayArray != null && dayArray.length > 0){
			                   map1.put("days",dayArray.length);//入住几晚
			                   Date startDate = DateUtils.addDays(selectDate,Integer.valueOf(dayArray[0])-1);
			                   //入住日期一般与所选日期一致
			                   checkInDate = DateUtil.formatSimpleDate(startDate);
			                   //在连续入住的情况下,离店日期
			                   checkOutDate =  DateUtil.formatSimpleDate(DateUtils.addDays(startDate,dayArray.length));
			                }
			            }
			                map1.put("check_in",checkInDate);
			                map1.put("check_out",checkOutDate);
			                map1.put("selectedQuantity", selectedQuantity);
			                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
			            try {
			                Date stdate = sdf.parse(checkInDate);
			                Date etdate = sdf.parse(checkOutDate);  
			                map1.put("week_in", DateUtil.getZhouDay(stdate));
			                map1.put("week_out", DateUtil.getZhouDay(etdate));
			            } catch (ParseException e) {
					e.printStackTrace();
				    }
			            map1.put("hotel_name",prodPackageDetail.getProdProduct().getProductName());
			            List<String> starList=  (List<String>) prodPackageDetail.getProdProduct().getPropValue().get("star_rate");
			            if(starList == null || starList.isEmpty()) {
			                starList = new ArrayList<String>();
			                starList.add("其他");
			            }
			            map1.put("star",starList);
			            map1.put("hotel_address",hotelAddress);
		                    map1.put("groupId",prodPackageGroup.getGroupId());
		                    map1.put("packge_type",prodPackageDetail.getProdProduct().getPackageType());
		                    map1.put("productId",prodPackageDetail.getProdProduct().getProductId());
		                    map1.put("detailId",prodPackageDetail.getDetailId());
		                    map1.put("pid", prodPackageGroup.getProductId());
		                    
		                    if(productBranchId!=null){//productBranchId不为空则表示为更换商品，需要指定为当前选中规格，否则指定为第一个产品规格规格ID
		                    	map1.put("currentProductBranchId",productBranchId);//指定为当前选中规格
		                    }else{
		                    	map1.put("currentProductBranchId",prodProductBranch.getProductBranchId());
		                    }
		                    map1.put("groupDivId",prodPackageGroup.getGroupId()+"_"+prodProductBranch.getProductBranchId());

		                }
		                hotelList.add(index, map1);
		                index++;
		            }
	            }
	        }
	    }
//	    LOG.info(com.lvmama.vst.front.utils.GsonUtils.toJson(hotelList));

	}
	
	
	
	
	/**
	 * <p>统计推荐酒店产品更多房型，loading页面展示</p>
	 * @User : ZM
	 * @Date : 2015年12月7日下午4:31:02
	 * @param prodProductBranch
	 * @param detailId
	 * @return List<Map>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> fangxinMore(ProdPackageDetail detail,boolean isFirst,Long selectedSuppGoodsId,Date selectDate,ProdProduct product,String selectedQuantity){

		ProdProductBranch prodProductBranch = detail.getProdProductBranch();
		Long detailId = detail.getDetailId();
		List<Map>fangXinListMore = new ArrayList<Map>();
		List<SuppGoods> suppGoodsList = prodProductBranch.getSuppGoodsList();
		if(suppGoodsList != null && suppGoodsList.size() > 0){
			Collections.sort(suppGoodsList, new Comparator<SuppGoods>() {
	            @Override
	            public int compare(SuppGoods o1, SuppGoods o2) {
	                if (o1.getDailyLowestPrice() >= o2.getDailyLowestPrice()) {
	                    return 1;
	                }
	                return -1;
	            }
	        });
//			SuppGoods suppGoods=suppGoodsList.get(0);
			boolean hasPriceFlag = false;
			for (SuppGoods suppGoods : suppGoodsList) {
				Map fangXin = new HashMap<String, Object>();
				
				fangXin.put("currentProductBranchId",prodProductBranch.getProductBranchId());
				fangXin.put("detailId",detailId);
				fangXin.put("branchName",prodProductBranch.getBranchName());
				fangXin.put("goodsName",suppGoods.getGoodsName());
				fangXin.put("suppGoodsId",suppGoods.getSuppGoodsId());
				fangXin.put("price",suppGoods.getDailyLowestPrice());
				//add by changf on 2018/7/12 携程酒店商品规格属性
	            RequestBody<Long> hotelRequestBody=new RequestBody<Long>();
	            hotelRequestBody.setT(suppGoods.getSuppGoodsId());
	            hotelRequestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
	            com.lvmama.dest.api.common.ResponseBody<List<HotelSuppGoodsPropVo>> suppGoodsPropResult = hotelGoodsPropApiServiceRemote.findSuppGoodsPropByGoodsId(hotelRequestBody);
	            boolean useProductBranchFlag=true;
	            Map<String,Object> props=null;
	            if(suppGoodsPropResult.isSuccess()){
	                List<HotelSuppGoodsPropVo> suppGoodsProps = suppGoodsPropResult.getT();
	                if(suppGoodsProps!=null && suppGoodsProps.size()>0){
	                    props=new HashMap<>();
	                    useProductBranchFlag=false;
	                    for(HotelSuppGoodsPropVo hotelSuppGoodsPropVo:suppGoodsProps){
	                        String propCode = hotelSuppGoodsPropVo.getPropCode();
	                        props.put(propCode,hotelSuppGoodsPropVo.getSuppGoodsPropValue().get(propCode));
	                    }

	                }
	            }
	            if(useProductBranchFlag) props=prodProductBranch.getPropValue();
	            List<PropValue> propValueList = new ArrayList();
	            List<PropValue> bedList=new ArrayList();
	            List<PropValue> smoList=new ArrayList();
	            List<PropValue> internetList=new ArrayList();
	            if(!useProductBranchFlag){
	                List<com.lvmama.dest.comm.po.prod.PropValue > destpropValueList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("bed_type");
	                List<com.lvmama.dest.comm.po.prod.PropValue > destBedList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("add_bed_flag");
	                List<com.lvmama.dest.comm.po.prod.PropValue > destSmoList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("smokeless_room");
	                List<com.lvmama.dest.comm.po.prod.PropValue > destInternetList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) props.get("internet");
	                copyListPropValues(destpropValueList,propValueList);
	                copyListPropValues(destBedList,bedList);
	                copyListPropValues(destSmoList,smoList);
	                copyListPropValues(destInternetList,internetList);
	            }else{
	                propValueList = (List<PropValue>)props.get("bed_type");
	                bedList = (List<PropValue>)props.get("add_bed_flag");
	                smoList = (List<PropValue>)props.get("smokeless_room");
	                internetList = (List<PropValue>)props.get("internet");
	            }
	            //end
	            if(propValueList != null && propValueList.size() > 0){
					String bedTypeDesc = propValueList.get(0).getAddValue();
					if(bedTypeDesc != null && !"".equals(bedTypeDesc)){
						fangXin.put("bed_type",propValueList.get(0).getName()+"("+bedTypeDesc+")");//床型
					}else{
						fangXin.put("bed_type",propValueList.get(0).getName());//床型
					}
					fangXin.put("bed_type_no_desc",propValueList.get(0).getName());//床型
				}
				String branchDesc = (String)props.get("branch_desc");//其他信息
				//String addBedFlag = (String)prodProductBranch.getPropValue().get("add_bed_flag");//加床信息
	            if(internetList != null && internetList.size() > 0){
	                fangXin.put("internet",internetList.get(0).getName());//宽带
	            }
				if(bedList != null && bedList.size() > 0){
					fangXin.put("add_bed_flag",bedList.get(0).getName());//加床信息
				}
				String area = (String)props.get("area");//面积信息
				String floor = (String)props.get("floor");//楼层信息
				//String smokelessRoom = (String)prodProductBranch.getPropValue().get("smokeless_room");//无烟房
				
				if(smoList != null && smoList.size() > 0){
					fangXin.put("smokeless_room",smoList.get(0).getName());//无烟房
				}
				
				if (area != null) {
					fangXin.put("area",area);
				}
				
				if (floor != null) {
					fangXin.put("floor",floor);
				}
				
				/*if (smokelessRoom != null) {
					fangXin.put("smokeless_room",smokelessRoom);
				}*/
	            if(useProductBranchFlag) {
	                if(prodProductBranch.getMaxVisitor() != null){
	                    String stayMaxAudlt = String.valueOf(prodProductBranch.getMaxVisitor());
	                    fangXin.put("capacity",stayMaxAudlt+"人");
	                }
	            }else{
	                if(props.get("occupant_number")!="")
	                fangXin.put("capacity",props.get("max_occupancy")+"人");
	            }
				if(branchDesc != null){
					fangXin.put("description",branchDesc);
				}
				 List<ComPhoto> photos = new ArrayList<ComPhoto>();
				ResultHandleT<List<ComPhoto>> photoResponse = comPhotoQueryClientService.findImageList("PRODUCT_BRANCH_ID", prodProductBranch.getProductBranchId());
				if(photoResponse != null && photoResponse.isSuccess()) {
					photos = photoResponse.getReturnContent();
				}
				
				if(photos!=null && photos.size()>0){
					fangXin.put("photo_url",photos.get(0).getPhotoUrl());
				}
				
				//国内
				if(((ProdProduct.PRODUCTTYPE.INNERLINE.getCode().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERSHORTLINE.getCode().equals(product.getProductType())||
						ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(product.getProductType()))
						&&BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==product.getBizCategoryId().longValue())
					||(ProdProduct.PRODUCTTYPE.INNERLINE.getCode().equals(product.getProductType())
						&&(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==product.getSubCategoryId().longValue()
	            		||BizEnum.BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().longValue()==product.getSubCategoryId().longValue()))){
	                ResultHandleT<SuppGoodsTimePrice> suppGoodsTimePrice=suppGoodsTimePriceAdapterClientService.getTimePrice(suppGoods.getSuppGoodsId(), selectDate, true);
	                if(suppGoodsTimePrice!=null&&suppGoodsTimePrice.getReturnContent()!=null){
	                	//suppGoodsTimePrice.getReturnContent().getBreakfast() 存在为空
	                	String breakfast = "";
	                	if(suppGoodsTimePrice.getReturnContent().getBreakfast()==null){
	                		breakfast = "不含早";
	                	}else{
	                		int breakfastValue = suppGoodsTimePrice.getReturnContent().getBreakfast().intValue();
	                		if(breakfastValue>0){
	                			breakfast = "含"+breakfastValue+"份早餐";
	                		}else{
	                			breakfast = "不含早";
	                		}
	                	}
	                	fangXin.put("isbreakfast", breakfast);
	                	
	                	Map<String,String>  suppGoodsCancelStrategyMap = SuppGoodsTimePrice.getHotelCancelStrategyDesc(suppGoodsTimePrice.getReturnContent(),suppGoods.getPayTarget());
	                	if(suppGoodsCancelStrategyMap != null && suppGoodsCancelStrategyMap.size() > 0){
	                		for (Map.Entry<String, String> entry : suppGoodsCancelStrategyMap.entrySet()) { 
	                    		if("content".equals(entry.getKey())){
	                    			fangXin.put("content", entry.getValue());
	                    		}
	                    		if("type".equals(entry.getKey())){
	                    			fangXin.put("type", entry.getValue());
	                    		}
	                    	}
	                	}
	                }else{
	                	fangXin.put("isbreakfast", "不含早");
	                }
	            }


	            //商品描述信息，图片展示 2018年6月9日11:11:04
	            com.lvmama.dest.api.common.ResponseBody<SuppGoodsPropDescriptionVo> responseBody =hotelGoodsPropDescriptionApiService.selectHotelGoodsPropDescriptionList(hotelRequestBody);
	            if (responseBody==null) {
	                LOG.error("hotelGoodsPropDescriptionApiService.selectHotelGoodsPropDescriptionList ！responseBody is null! for goods  " + suppGoods.getSuppGoodsId() + " fail!");
	            }else if (responseBody.isFailure()) {
	                LOG.error("hotelGoodsPropDescriptionApiService.selectHotelGoodsPropDescriptionList  for goods " + suppGoods.getSuppGoodsId() + " fail!"+" message is "+responseBody.getMessage());
	            }else {
	                SuppGoodsPropDescriptionVo descriptionVo=responseBody.getT();
	                if (null!=descriptionVo){
	                    fangXin.put("suppGoodsPropDescriptionVo",responseBody.getT());
	                }
	            }

				Map<String, Long> selectPriceMap = suppGoods.getSelectPriceMap();//生成可选间数的select
				if(selectPriceMap != null  && selectPriceMap.size() > 0){
					//生成一个可以算钱的select
					StringBuffer select = new StringBuffer("");
					StringBuffer selectOption = new StringBuffer("");
					if(selectedSuppGoodsId!=null && selectedSuppGoodsId.intValue() == suppGoods.getSuppGoodsId().intValue()){
						select.append("<select class=\"lvmama-price-flag room_num js_hotel_quantity_selector\"").append(" ");
					}else if(isFirst && !hasPriceFlag){
						hasPriceFlag = true;
						select.append("<select class=\"lvmama-price-flag room_num js_hotel_quantity_selector\"").append(" ");
					}else{
						select.append("<select class=\"lvmama-price-flagMore room_num js_hotel_quantity_selector\"").append(" ");
					}
					//select.append("name=productMap[\"").append(productId).append("\"]").append(".itemMap[\"").append(suppGoods.getSuppGoodsId()).append("\"]").append(".quantity").append(" ");
					//用来区分是哪一部分的选择框,因为算总价的时候,每一个选择框要做的事情是不一样的
					select.append("data-type=\"").append("hotel").append("\"").append(" ");
					//用来得到和它有关糸的组件
					select.append("data-detailid-suppgoodsid=\"").append(detailId+"-"+suppGoods.getSuppGoodsId()).append("\"").append(" ");
//		            Set<String> keys = selectPriceMap.keySet();
					List<String> keys = new ArrayList<String>();
					keys.addAll(selectPriceMap.keySet());
					Collections.sort(keys, new Comparator<String>() {
						public int compare(String o1, String o2) {
							try {
								return Integer.parseInt(o1.trim()) - Integer.parseInt(o2.trim());
							}catch(Exception ex){return 0;}
						}
					});
					int defaultQuantityCount = 0;
					StringBuffer dataPrice = new StringBuffer();
					for(String keyTemp : keys){
						if(defaultQuantityCount == 0){
							//参数div里的默认份数
							fangXin.put("defaultQuantity",keyTemp);
						}
						select.append("data-price-").append(keyTemp).append("=").append(selectPriceMap.get(keyTemp)).append(" ");
						dataPrice.append("data-price-").append(keyTemp).append("=").append(selectPriceMap.get(keyTemp)).append(" ");
				
						if(selectedQuantity != null && keyTemp.equals(selectedQuantity)){
							selectOption.append("<option selected=\"selected\"  value=\"").append(keyTemp).append("\">").append(keyTemp).append("</option>");
						}else{
							selectOption.append("<option value=\"").append(keyTemp).append("\">").append(keyTemp).append("</option>");
						}
						defaultQuantityCount++;
					}
					select.append("data-adultPrice=").append(suppGoods.getAdultPrice()).append(" ");
					select.append(">").append(" ");
					select.append(selectOption);
					select.append("</select>");
					fangXin.put("select",select.toString());//可选择的房间数量
					fangXin.put("selectOption", selectOption.toString());
					fangXin.put("data-price", dataPrice.toString());
					fangXin.put("data-adultPrice", suppGoods.getAdultPrice());
					fangXinListMore.add(fangXin);
				}
			}
		}
		return fangXinListMore;

	}

}
