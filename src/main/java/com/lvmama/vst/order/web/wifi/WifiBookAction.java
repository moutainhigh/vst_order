/**
 * 
 */
package com.lvmama.vst.order.web.wifi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.comm.search.vst.vo.VstTicketSearchVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsSaleReClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prom.service.MarkCouponLimitClientService;
import com.lvmama.vst.back.client.wifi.service.WifiClientService;
import com.lvmama.vst.back.client.wifi.service.WifiSuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.vo.SuppGoodsVO;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.wifi.po.SuppGoodsRentedLimit;
import com.lvmama.vst.back.wifi.po.WifiPickingPoint;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.MarkcouponLimitInfo;
import com.lvmama.vst.comm.vo.wifi.WifiPickingPointVo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

/**
 * @author pengyayun
 *
 */
@Controller
public class WifiBookAction extends BaseActionSupport {
	
	
	private static final long serialVersionUID = 2165432125863218855L;

	/**
	 * 日志
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WifiBookAction.class);
	
	/**
	 * 
	 */
	private final String WIFI_BOOK_PAGE="/order/wifi/showWifiBookInfo";
	
	private final String PHONE_BOOK_PAGE="/order/wifi/showPhoneBookInfo";
	
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	protected OrderService orderService;
	
	@Autowired
	private SuppGoodsSaleReClientService suppGoodsSaleReClientService;
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;//时间价格表
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private ProdProductNoticeClientService prodProductNoticeClientService;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;
	
	@Autowired
    private SuppGoodsClientService suppGoodsClientService;
	    
	@Autowired
	private WifiClientService wifiClientService;
	    
	@Autowired
    private DistrictClientService districtClientService;
	
	@Autowired
	private WifiSuppGoodsClientService  wifiSuppGoodsClientService;
	
	/**
	 * 进入产品商品查询页面
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/wifi/showWifiQueryList.do")
	public String showWifiQueryList(Model model,HttpServletRequest request){
		UserUser user=null;
		try {
			//从cookie中读取用户信息
			user=readUserCookie();
			if(user==null||StringUtil.isEmptyString(user.getUserId())){
				String userId = request.getParameter("userId"); 
						
				if(StringUtil.isNotEmptyString(userId)){
					user=userUserProxyAdapter.getUserUserByUserNo(userId);
				}
			}
			
		}catch (Exception e) {
			LOG.error("{}", e);
		}
		model.addAttribute("user", user);
		model.addAttribute("vo", new VstTicketSearchVO());
		return "/order/orderProductQuery/wifi/showWifiProductQueryList";
	}
	
	/**
	 * 进入产品商品查询页面
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/wifi/searchWifiList.do")
	public String searchTicketList(Long productId,String productName,Integer page,Model model,HttpServletRequest request) {
		LOG.debug("searchWifiList start");
		UserUser user=null;
		Page pageParam=null;
		try {
			 Map<String,Object> params = new HashMap<String, Object>();
			 
			 if(productId==null){
				 if(StringUtils.isNumeric(productName)){
					 params.put("productId",productName);
				 }else{
					 params.put("productName",productName);
				 }
			 }else{
				 params.put("productId",productId);
				 if(StringUtils.isNotBlank(productName)){
					 params.put("productName",productName);
				 }
			 }
			 
			 params.put("distributorId", Constant.DIST_BACK_END);
			 params.put("bizCategoryId",BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId());
			ResultHandleT<List<ProdProduct>> resultHandleT = prodProductClientService.findWifiProductByparams(params);
			if(resultHandleT!=null && resultHandleT.getReturnContent()!=null&&resultHandleT.getReturnContent().size()>0){
				List<ProdProduct> prodProductList = resultHandleT.getReturnContent();
				List<ProdProduct> result =new ArrayList<ProdProduct>();
				
				if(!CollectionUtils.isEmpty(prodProductList)){
					for (int i = 0; i < prodProductList.size(); i++) {
						ProdProduct prodProduct=prodProductList.get(i);
						if(CollectionUtils.isEmpty(prodProduct.getProdProductBranchList())||prodProduct.getProdProductBranchList().size()<=0){
							prodProductList.remove(i);
							i--;
						}
					}
					//设置分页参数
					int pagenum = page == null ? 1 : page;
					pageParam = Page.page(prodProductList.size(), 10, pagenum);
					pageParam.buildJSONUrl(request,true);
					
					
					if(prodProductList.size()<=10){
						result=prodProductList;
					}else{
						result=prodProductList.subList(Integer.parseInt(String.valueOf(pageParam.getStartRows())), Integer.parseInt(String.valueOf(pageParam.getEndRows())));
					}
				}
				
				if(pageParam != null) {
					pageParam.setItems(result);
				}
			}
			//从cookie中读取用户信息
			user=readUserCookie();
			
		}catch (Exception e){
			LOG.error("{}", e);
		}
		
		model.addAttribute("result", pageParam);
		model.addAttribute("user", user);
		return "/order/orderProductQuery/wifi/wifi_product_query_result";
	}
	
	
	
	@Autowired
	private MarkCouponLimitClientService markCouponLimitClientService;
	@RequestMapping(value = "/ord/book/wifi/infoFillIn.do")
	public String infoFillIn(HttpServletRequest request,Long goodsId,ModelMap model) {
		
		String userId = request.getParameter("userId");
		
		String result=ERROR_PAGE;
		int checkLimit =  0 ;
		Long checkId = null ;
		if(goodsId!=null){
			result=loadGoods(model, goodsId);
			checkId = Long.valueOf(goodsId);
			
		}
		
		//判断是否支持优惠券			
		try {
			String  islimit ="N";
			if(null != checkId){					
				ResultHandleT<MarkcouponLimitInfo> resultCouponInfo =
						markCouponLimitClientService.couponIslimitInfo(checkId,checkLimit);
				if(resultCouponInfo.isSuccess() && resultCouponInfo.getReturnContent()!=null){
					MarkcouponLimitInfo markInfo = resultCouponInfo.getReturnContent();
					islimit= markInfo.getIslimit();
				}
			}
			model.put("productCouponLimit" , islimit);
			
		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		

		//从cookie中读取用户信息
		UserUser user=null;
		if(StringUtil.isEmptyString(userId)){
			 user=readUserCookie();
		}else{
			user=userUserProxyAdapter.getUserUserByUserNo(userId);
		}
		 
		model.addAttribute("user", user);
		model.addAttribute("isFaxBreakRemark",true);
		return result;
	}
	
	
	
	
	
	/**
	 * wifi/电话卡页面跳转
	 * @param model
	 * @param goodsId
	 * @return
	 */
	private String loadGoods(ModelMap model,Long goodsId){

		String result = ERROR_PAGE;
		SuppGoods suppGoods = null;
		ProdProduct product = null;
		SuppGoodsRentedLimit suppGoodsRentedLimit = null;
		ResultHandleT<SuppGoods> suppGoodsResultHandleT;
		try {
			suppGoodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END,goodsId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
		}
		if(suppGoodsResultHandleT.hasNull()||suppGoodsResultHandleT.getReturnContent()==null){
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
		}
		
    	ResultHandleT<SuppGoodsRentedLimit> suppGoodsRentedLimitResult = wifiSuppGoodsClientService.findSuppGoodsRentedLimit(goodsId);
    	if(suppGoodsRentedLimitResult.hasNull()||suppGoodsRentedLimitResult.getReturnContent()==null){
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
		}
		suppGoods = suppGoodsResultHandleT.getReturnContent();
		suppGoodsRentedLimit = suppGoodsRentedLimitResult.getReturnContent();
		
		if(suppGoodsRentedLimit==null){
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
		}
		if("Y".equalsIgnoreCase(suppGoods.getPackageFlag())){
			model.addAttribute("ERROR","此商品仅组合销售!");
			return ERROR_PAGE;
		}
		model.addAttribute("goodsRentedLimit", suppGoodsRentedLimit);
		
		ResultHandleT<ProdProduct> productResultHandleT = prodProductClientService.findProdProductByIdFromCache(suppGoods.getProductId());
		if(productResultHandleT.hasNull()||productResultHandleT.getReturnContent()==null){
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
		}
		product = productResultHandleT.getReturnContent();
		
		if(ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(product.getProductType())){
			
			result = loadWifiSuppGoods(model,suppGoods,suppGoodsRentedLimit,product);
			
		}else if(ProdProduct.WIFIPRODUCTTYPE.PHONE.name().equals(product.getProductType())){
					
			result  = 	loadPhoneSuppGoods(model,suppGoods,suppGoodsRentedLimit,product);
		}
		
		return result;
		
	
		
	}
	
	
	
	
	/**
	 * wifi商品信息
	 * @param model
	 * @param suppGoods
	 * @param suppGoodsRentedLimit
	 * @param product
	 * @return
	 */
	private String loadWifiSuppGoods(ModelMap model,SuppGoods suppGoods,SuppGoodsRentedLimit suppGoodsRentedLimit,ProdProduct product){
		try {
			
			
			if(!ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(product.getProductType()) || (!BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(product.getBizCategoryId()) )){
				model.addAttribute("ERROR","商品不可售");
				return ERROR_PAGE;
			}
			List<WifiPickingPointVo> WifiPickingPointVoList = new ArrayList<WifiPickingPointVo>();
			if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
				ResultHandleT<List<WifiPickingPoint>> wifiPickingPointResult = wifiClientService.findWifiPickingPoint(suppGoods.getSuppGoodsId(),null,null,suppGoods.getGoodsType());
				if(wifiPickingPointResult==null || wifiPickingPointResult.getReturnContent()==null||wifiPickingPointResult.getReturnContent().size()==0){
						model.addAttribute("ERROR","商品不可售");
						return ERROR_PAGE;
						
				}
				for(WifiPickingPoint wifiPickingPoint : wifiPickingPointResult.getReturnContent() ){
					WifiPickingPointVo wifiPickingPointVo = new WifiPickingPointVo();
					BeanUtils.copyProperties(wifiPickingPoint, wifiPickingPointVo);
					ResultHandleT<BizDistrict> result = districtClientService.findDistrictById(wifiPickingPoint.getDistrictId());
					if(result!=null && result.getReturnContent()!=null){
						wifiPickingPointVo.setDistrictName(result.getReturnContent().getDistrictName());
					}
					WifiPickingPointVoList.add(wifiPickingPointVo);
					
				}
				model.addAttribute("wifiPickingPointList",WifiPickingPointVoList);
				
				
			}else if(SuppGoods.GOODSTYPE.NOTICETYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
				
				ResultHandleT<List<BizDistrict>> citylistResult = wifiClientService.findWifiCityByGoods(suppGoods.getSuppGoodsId(),suppGoods.getGoodsType());
				
				if(citylistResult==null || citylistResult.getReturnContent() ==null || citylistResult.getReturnContent().size()==0){
					
					model.addAttribute("ERROR","商品不可售");
					return ERROR_PAGE;
				}else{
					List<BizDistrict> cityList = citylistResult.getReturnContent();
						model.addAttribute("cityList", cityList);
						if(org.apache.commons.collections.CollectionUtils.isEmpty(cityList)){
							model.addAttribute("ERROR","商品不可售");
							return ERROR_PAGE;
						}
						
						
						ResultHandleT<List<WifiPickingPoint>> wifiPickingPointResult = wifiClientService.findWifiPickingPoint(suppGoods.getSuppGoodsId(),null,null,suppGoods.getGoodsType());
						if(wifiPickingPointResult==null || wifiPickingPointResult.getReturnContent()==null||wifiPickingPointResult.getReturnContent().size()==0){
								model.addAttribute("ERROR","商品不可售");
								return ERROR_PAGE;
								
						}
						
						model.addAttribute("wifiPickingPointList",wifiPickingPointResult.getReturnContent());
					}
				
				
				
			}
			
			
			 HashMap<String,Object> params = new HashMap<String,Object>();
		        params.put("suppGoodsId", suppGoods.getSuppGoodsId());
		        params.put("date", new Date());
		        params.put("orderByClause", "sgr.SPEC_DATE");
		        
		        try {
		        	
					ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT=distGoodsTimePriceClientService.getBaseTimePriceByGoodsIdAndDate(Constant.DIST_BACK_END,suppGoods.getSuppGoodsId(), new Date(),12);//DateUtil.getDateByStr(visitTime, "yyyy-MM-dd")
					if(!resultHandleT.isFail()&&resultHandleT.getReturnContent()!=null){
						List<SuppGoodsAddTimePrice> timeList = resultHandleT.getReturnContent();
						
						int minQuantity = 0;
						if(suppGoodsRentedLimit.getMinRentedDays()!=null){
							minQuantity = suppGoodsRentedLimit.getMinRentedDays().intValue();
						}
						if(minQuantity==0){
							model.addAttribute("ERROR","商品不可售");
							return ERROR_PAGE;
						}
						if(timeList!=null && timeList.size()>0){
							boolean flag = false;
							int minPos = 0;
							int maxCount = 0;
							
							List<SuppGoodsAddTimePrice> tempList = new ArrayList<SuppGoodsAddTimePrice>(timeList);
							for(int i=0;i<timeList.size();i++){
							
							/*Date date	= DateUtil.getDateByStr("2015-12-19", "yyyy-MM-dd");	
							if(date.getTime()== timeList.get(i).getSpecDate().getTime()){
								System.out.println("");
							}*/
							int count =0;
							for(int j =0 ;j<minQuantity;j++){
								
								Date afterDay = DateUtil.getDateAfterDays(timeList.get(i).getSpecDate(),j);
								for(int k=0;k<tempList.size();k++){
									
									if(tempList.get(k).getSpecDate().getTime() == afterDay.getTime()){
										++count;
										if(count>=minQuantity){
											minPos = i;
											flag = true;
											break;
										}
										 break;
									}
								}
								maxCount = count;
							}
							
							
							if(flag){
								break;
							}
							
							}
							if(flag){
								Date startDate = timeList.get(minPos).getSpecDate();
								Date endDate = DateUtil.getDateAfterDays(timeList.get(minPos).getSpecDate(),maxCount-1);
								model.addAttribute("startDate",startDate );
								model.addAttribute("endDate",endDate);
								SuppGoodsVO depositGood = null;
									
									if("Y".equals(suppGoodsRentedLimit.getDepositFlag())){
										
										ResultHandleT<SuppGoodsVO> deposResulthlet = suppGoodsClientService.findDepositSuppGoods(suppGoods.getSuppGoodsId(), startDate);
										if(deposResulthlet==null || deposResulthlet.getReturnContent()==null){
											model.addAttribute("ERROR","商品不可售");
											return ERROR_PAGE;
										}
										depositGood = deposResulthlet.getReturnContent();
										 if(depositGood!=null){
											 SuppGoodsNotimeTimePrice goodTimePrice = depositGood.getSuppGoodsNotimeTimePrice();
											 if(goodTimePrice!=null){
												 if(goodTimePrice.getPrice()==null || goodTimePrice.getPrice()<0  || goodTimePrice.getSettlementPrice()==null||goodTimePrice.getSettlementPrice()<0){
													 model.addAttribute("ERROR","商品不可售");
													 return ERROR_PAGE;
												 }
												 
											 }
											 model.addAttribute("depositGood", depositGood);
										 }
										
									}
								
							}else{
								model.addAttribute("ERROR","此商品最近不可售!");
								return ERROR_PAGE;
							}
							
						}else{
							model.addAttribute("ERROR","此商品最近不可售!");
							return ERROR_PAGE;
						}
					}
				}catch (Exception e){
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}   
			
		}catch (Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		 
		model.addAttribute("suppGoods",suppGoods);
		model.addAttribute("product",product);
		return WIFI_BOOK_PAGE;
	}
	
	
	/**
	 * 电话卡商品信息
	 * @param model
	 * @param suppGoods
	 * @param suppGoodsRentedLimit
	 * @param product
	 * @return
	 */
	private String loadPhoneSuppGoods(ModelMap model,SuppGoods suppGoods,SuppGoodsRentedLimit suppGoodsRentedLimit,ProdProduct product){
		SuppGoodsVO depositGood = null;
		SuppGoodsAddTimePrice timePrice = null;
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice=null;
		try {
			if(!ProdProduct.WIFIPRODUCTTYPE.PHONE.name().equals(product.getProductType())){
				model.addAttribute("ERROR","商品不可售");
				return ERROR_PAGE;
			}
			HashMap<String,Object> paramss = new HashMap<String,Object>();
			paramss.put("suppGoodsId", suppGoods.getSuppGoodsId());
			paramss.put("date", new Date());
			paramss.put("orderByClause", "sgr.SPEC_DATE");
	        ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT=suppGoodsTimePriceClientRemote.getFirstTimePrice(paramss);
	        suppGoodsBaseTimePrice=timePriceResultHandleT.getReturnContent();
	        if(timePriceResultHandleT.isFail()||suppGoodsBaseTimePrice==null){
			model.addAttribute("ERROR","商品不可售");
			return ERROR_PAGE;
	        }
	        timePrice =  (SuppGoodsAddTimePrice)suppGoodsBaseTimePrice;
	        
				if("Y".equals(suppGoodsRentedLimit.getDepositFlag())){
					
					ResultHandleT<SuppGoodsVO> deposResulthlet = suppGoodsClientService.findDepositSuppGoods(suppGoods.getSuppGoodsId(), timePrice.getSpecDate());
					if(deposResulthlet==null || deposResulthlet.getReturnContent()==null){
						model.addAttribute("ERROR","商品不可售");
						return ERROR_PAGE;
					}
					depositGood = deposResulthlet.getReturnContent();
					 if(depositGood!=null){
						 SuppGoodsNotimeTimePrice goodTimePrice = depositGood.getSuppGoodsNotimeTimePrice();
						 if(goodTimePrice!=null){
							 if(goodTimePrice.getPrice()==null  || goodTimePrice.getSettlementPrice()==null){
								 model.addAttribute("ERROR","商品不可售");
								 return ERROR_PAGE;
							 }
							 
						 }
						 model.addAttribute("depositGood", depositGood);
					 }
					
				}
	        
		}catch (Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		model.addAttribute("suppGoods",suppGoods);
		model.addAttribute("product",product);
		model.addAttribute("startDate",timePrice.getSpecDate());
		return PHONE_BOOK_PAGE;
	}
	
	
	
	/**
	 * wifi日历价格
	 * @param model
	 * @param suppGoodsId
	 * @param visitTime
	 * @param minDay
	 * @param endFlag
	 * @return
	 */
	@RequestMapping("/ord/book/wifi/getWifiTimePrice.do")
	@ResponseBody
	public Object getWifiTimePrice(ModelMap model,Long suppGoodsId,String visitTime,String endFlag){
		ResultMessage msg = ResultMessage.createResultMessage();
		SuppGoodsRentedLimit suppGoodsRentedLimit = null;
	
    	
		ResultHandleT<SuppGoodsRentedLimit> suppGoodsRentedLimitResult = wifiSuppGoodsClientService.findSuppGoodsRentedLimit(suppGoodsId);
    	if(suppGoodsRentedLimitResult.hasNull()||suppGoodsRentedLimitResult.getReturnContent()==null){
    		msg.raise("商品附加参数有误");
			return msg;
		}
		suppGoodsRentedLimit = suppGoodsRentedLimitResult.getReturnContent();
		int maxBookday =0;
		int minBookday = 0;
		if(suppGoodsRentedLimit.getMaxRentedDays()!=null){
			maxBookday = suppGoodsRentedLimit.getMaxRentedDays().intValue();
		}
		if(suppGoodsRentedLimit.getMinRentedDays()!=null){
			minBookday = suppGoodsRentedLimit.getMinRentedDays().intValue();
		}
		if(maxBookday ==0 || minBookday==0){
			msg.raise("商品限制天数异常");
			return msg;
		}
		
			//第二个日历框时间价格加载
			if(StringUtils.isNotBlank(visitTime)&&StringUtils.isNotBlank(endFlag)&& "Y".equals(endFlag)){
				
				Date start = null;
				try {
					start = DateUtil.toSimpleDate(visitTime);
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
					msg.raise("日期参数错误");
					return msg;
				}
				
				try {
					ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT=distGoodsTimePriceClientService.getBaseTimePriceByGoodsIdAndDate(Constant.DIST_BACK_END, suppGoodsId, start,12);//DateUtil.getDateByStr(visitTime, "yyyy-MM-dd")
					if(resultHandleT.isFail()||resultHandleT.getReturnContent()==null){
						msg.raise(resultHandleT.getMsg());
					}
					List<SuppGoodsAddTimePrice> timelist = resultHandleT.getReturnContent();
					if (timelist != null && timelist.size() > 0) {
						Date maxDate = null;
						int maxPos = 0;
						int removePos = 0;
						boolean flag = false;
						List<Date> minBookDays = new ArrayList<Date>();
						List<SuppGoodsAddTimePrice> tempList = new ArrayList<SuppGoodsAddTimePrice>(
								timelist);
						for (int j = 0; j < maxBookday; j++) {

							boolean fristFlag = true;
							Date afterDay = DateUtil.getDateAfterDays(
									start, j);
							for (int i = 0; i < tempList.size(); i++) {

								if (tempList.get(i).getSpecDate().getTime() == afterDay
										.getTime()) {
									flag = true;
									removePos = i;
									if(j<minBookday-1){
										minBookDays.add(tempList.get(i).getSpecDate());
									}
									break;
								} else {
									if (fristFlag) {
										
										flag = false;
										maxDate = tempList.get(i)
												.getSpecDate();
										fristFlag = false;

									}
								}
							}
							if (flag && tempList.size() > 0) {
								tempList.remove(removePos);
							} else {
								break;
							}
						}
						for (int i = 0; i < timelist.size(); i++) {
							 for(Date date : minBookDays){
								 if(date.getTime() == timelist.get(i).getSpecDate().getTime()){
									 timelist.remove(i);
								 }
							 }
							if (maxDate != null&& maxDate.getTime() == timelist.get(i).getSpecDate().getTime()) {
								maxPos = i;
							}

						}

						if (!flag && maxPos < maxBookday) {
							timelist = timelist.subList(0, maxPos);
						} else {
							if (timelist.size() >= maxBookday) {
								if(minBookday>0){
									timelist = timelist.subList(0, maxBookday-(minBookday-1));
								}else{
									timelist = timelist.subList(0, maxBookday);
								}
								
							} else {
								
								timelist = timelist.subList(0,timelist.size());
								
							}

						}
					}
				msg.addObject("timePriceList", timelist);
			}catch (Exception e){
				LOG.error(ExceptionFormatUtil.getTrace(e));
				msg.raise("查询时间价格发生异常了.");
			}
				
				
			//第一个日历时间价格表加载	
			}else{
				
				try {
					
		        	
					ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT=distGoodsTimePriceClientService.getBaseTimePriceByGoodsIdAndDate(Constant.DIST_BACK_END,suppGoodsId, new Date(),12);//DateUtil.getDateByStr(visitTime, "yyyy-MM-dd")
					if(!resultHandleT.isFail()&&resultHandleT.getReturnContent()!=null){
						List<SuppGoodsAddTimePrice> timeList = resultHandleT.getReturnContent();
						
						
						if(timeList!=null && timeList.size()>0){
							List<SuppGoodsAddTimePrice> removeTimePriceList = new ArrayList<SuppGoodsAddTimePrice>();
							List<SuppGoodsAddTimePrice> tempList = new ArrayList<SuppGoodsAddTimePrice>(timeList);
							for(int i=0;i<timeList.size();i++){
							int count =0;
							for(int j =0 ;j<minBookday;j++){
								
								Date afterDay = DateUtil.getDateAfterDays(timeList.get(i).getSpecDate(),j);
								for(int k=0;k<tempList.size();k++){
									
									if(tempList.get(k).getSpecDate().getTime() == afterDay.getTime()){
										++count;
									}
								}
								
							}
							if(count<minBookday){
								removeTimePriceList.add(timeList.get(i));
							}
							
							
							}
							for(int i=0;i<timeList.size();i++){
								for(int j=0;j<removeTimePriceList.size();j++){
									
									if(removeTimePriceList.get(j).getSpecDate().getTime() == timeList.get(i).getSpecDate().getTime()){
										timeList.remove(i);
									}
									
									
								}
								
							}
							msg.addObject("timePriceList", timeList);
					
						}
					}
					
				}catch (Exception e){
					LOG.error(ExceptionFormatUtil.getTrace(e));
					msg.raise("查询时间价格发生异常了.");
				}
				
			}
		
	 msg.setCode(msg.SUCCESS);
	return msg;
	 
	 }
	
	
	/***
	 * 电话卡日历价格表
	 * @param model
	 * @param suppGoodsId
	 * @param visitTime
	 * @return
	 */
	@RequestMapping("/ord/book/wifi/getPhoneCardTimePrice.do")
	@ResponseBody
	public Object getPhoneTimePrice(ModelMap model,Long suppGoodsId,String visitTime){
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT=distGoodsTimePriceClientService.getBaseTimePriceByGoodsIdAndDate(Constant.DIST_BACK_END, suppGoodsId, new Date(),12);//DateUtil.getDateByStr(visitTime, "yyyy-MM-dd")
			if(resultHandleT.isFail()||resultHandleT.getReturnContent()==null){
				msg.raise(resultHandleT.getMsg());
			}
			msg.addObject("timePriceList", resultHandleT.getReturnContent());
		}catch (Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			msg.raise("查询时间价格发生异常了.");
		}
		
		msg.setCode(msg.SUCCESS);

		return msg;
	}
	
	
	
	
	/**
	 * wifi网点加载
	 * @param cityId
	 * @param suppGoodsId
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/ord/book/wifi/pickingPoint.do")
	public String pickingPoint(Long cityId,Long suppGoodsId,HttpServletRequest request, ModelMap model) {
		
		
		ResultHandleT<SuppGoods> suppGoodsResultHandleT = null;
		try {
			suppGoodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, suppGoodsId);
		} catch (Exception e) {
			
		}
		if(suppGoodsResultHandleT!=null && suppGoodsResultHandleT.getReturnContent()!=null){
			SuppGoods good = suppGoodsResultHandleT.getReturnContent();
			if(good ==null){
				return "";
			}
			ResultHandleT<List<WifiPickingPoint>> wifiPickingPointResult = wifiClientService.findWifiPickingPoint(suppGoodsId,cityId,null,good.getGoodsType());
			
			if(wifiPickingPointResult!=null && wifiPickingPointResult.getReturnContent().size()>0){
			
				model.addAttribute("wifiPickingPointList",wifiPickingPointResult.getReturnContent());
				
				if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(good.getGoodsType())){
					model.addAttribute("pointType","huan");
					
				}else if(SuppGoods.GOODSTYPE.NOTICETYPE_DISPLAY.name().equals(good.getGoodsType())){
					
					model.addAttribute("pointType","quhuan");
				}
				model.addAttribute("suppGoodsId",good.getSuppGoodsId());
			
			}
			
		}
		
		return "/order/wifi/inc/pickingPoint";
	}
	
	/**
	 * ajax查询wifi产品名称
	 * @param search
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/wifi/queryWifiProductList.do")
	public void queryVisaProductList(String search,HttpServletResponse response) throws BusinessException{
		//组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		if(NumberUtils.isNumber(search)){
			Long productId=Long.parseLong(search);
			params.put("productId", productId);
		}else{
			params.put("productName", search);
		}
		params.put("bizCategoryId",BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId());
		//调用联想查询接口
		List<ProdProduct> productList = prodProductClientService.findProdProductListByCondition(params);
		//组装JSON数据BIZ_DISTRICT
		JSONArray jsonArray = new JSONArray();
		if(null != productList && !productList.isEmpty()){
			for(ProdProduct product:productList){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", product.getProductId());
				jsonObject.put("text", product.getProductName());
				jsonArray.add(jsonObject);
			}
		}
		//返回JSON数据
		JSONOutput.writeJSON(response, jsonArray);
	}
	


}
