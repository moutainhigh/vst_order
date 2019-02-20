/**
 * 
 */
package com.lvmama.vst.order.web.visa;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizDictExtend;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsSaleReClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdPackLineClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.po.SuppGoodsSimpleTimePrice;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.web.visa.vo.OrdVisaProductQueryVo;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

/**
 * @author pengyayun
 *
 */
@Controller
public class VisaBookAction extends BaseActionSupport {
	
	/**
	 * 日志
	 */
	private static final Logger LOG = LoggerFactory.getLogger(VisaBookAction.class);
	
	/**
	 * 
	 */
	private final String VISA_BOOK_PAGE="/order/visa/showVisaBookInfo";
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private ProdPackLineClientService prodPackLineClientRemote;
	
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
	
	/*@Autowired
	private ProdProductBranchClientService prodProductBranchClientService;
	
	@Autowired
	private ProdProductNoticeClientService prodProductNoticeClientService;*/
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private DictClientService dictClientService;
	
	@Autowired
	private DistrictClientService districtClientService;
	
	/**
	 * 进入产品商品查询页面
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/visa/showVisaQueryList.do")
	public String showTicketQueryList(Model model,HttpServletRequest request){
		UserUser user=null;
		List<BizDictExtend> vistTypeList=null;
		List<BizDictExtend> vistCityList =null;
		List<BizDictExtend> visaRangeList=null;
		try {
			//查询签证类型字典
			Map<String, Object> params1 = new HashMap<String, Object>();
			params1.put("dictCode", "VISA_TYPE");
			ResultHandleT<List<BizDictExtend>> typeResultHandleT = dictClientService.findBizDictExtendList(params1);
			vistTypeList=typeResultHandleT.getReturnContent();
			//查询送签城市字典
			Map<String, Object> params2 = new HashMap<String, Object>();
			params2.put("dictCode", "VISA_CITY");
			ResultHandleT<List<BizDictExtend>> cityResultHandleT= dictClientService.findBizDictExtendList(params2);
			vistCityList =cityResultHandleT.getReturnContent();
			//查询受理领区字典
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("dictCode", "VISA_RANGE");
			ResultHandleT<List<BizDictExtend>> rangeResultHandleT= dictClientService.findBizDictExtendList(params);
			visaRangeList=rangeResultHandleT.getReturnContent();
			
			//从cookie中读取用户信息
			user=readUserCookie();
			if(user==null||StringUtil.isEmptyString(user.getUserId())){
				String userId = request.getParameter("userId");
				if(StringUtil.isNotEmptyString(userId)){
					user=userUserProxyAdapter.getUserUserByUserNo(userId);
				}
			}
		}catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		model.addAttribute("vistTypeList", vistTypeList);
		model.addAttribute("vistCityList", vistCityList);
		model.addAttribute("visaRangeList", visaRangeList);
		model.addAttribute("user", user);
		model.addAttribute("vo", new OrdVisaProductQueryVo());
		return "/order/orderProductQuery/visa/showVisaProductQueryList";
	}
	
	/**
	 * 进入产品商品查询页面
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/visa/findVisaList.do")
	public String findVisaList(OrdVisaProductQueryVo vo,Integer page,Model model,HttpServletRequest request) {
		 
		UserUser user=null;
		Page pageParam=null;
		List<BizDictExtend> visaRangeList=null;
		try {
			//构造查询条件
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<String> visaTypes=null;
			List<String> visaRanges=null;
			if(StringUtil.isNotEmptyString(vo.getProduct())){
				params.put("productId", vo.getProduct());
			}
			if(StringUtil.isNotEmptyString(vo.getVisaCountry())){
				params.put("visaCountry", vo.getVisaCountry());
			}
			if(StringUtil.isNotEmptyString(vo.getVisaType())){
				String[] visaType=vo.getVisaType().split(",");
				if(visaTypes==null){
					visaTypes=new ArrayList<String>();
				}
				for (String str : visaType) {
					visaTypes.add(str);
				}
				params.put("visaTypes", visaTypes);
			}
			if(StringUtil.isNotEmptyString(vo.getVisaRange())){
				String[] visaRange=vo.getVisaRange().split(",");
				if(visaRanges==null){
					visaRanges=new ArrayList<String>();
				}
				for (String str : visaRange) {
					visaRanges.add(str);
				}
				params.put("visaRanges", visaRanges);
			}
			if(StringUtil.isNotEmptyString(vo.getRecommendLevel())){
				params.put("recommendLevel", vo.getRecommendLevel());
			}
			//统计满足条件的产品数
			/*int count = prodProductClientService.findVisaProductCount(params);*/
			
			/*
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());*/
			
			params.put("distributorId", Constant.DIST_BACK_END);
			
			List<ProdProduct> prodProductList=prodProductClientService.findVisaProductByCondition(params);
			
			List<ProdProduct> result=new ArrayList<ProdProduct>();
			
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
			//查询受理领区字典
			params.clear();
			params.put("dictCode", "VISA_RANGE");
			ResultHandleT<List<BizDictExtend>> rangeResultHandleT= dictClientService.findBizDictExtendList(params);
			visaRangeList=rangeResultHandleT.getReturnContent();
			if(pageParam != null) {
				pageParam.setItems(result);
			}
			
			//从cookie中读取用户信息
			user=readUserCookie();
			
		}catch (Exception e){
			LOG.error("{}", e);
		}
		
		//保存分页对象
		model.addAttribute("result", pageParam);
		model.addAttribute("visaRangeList", visaRangeList);
		model.addAttribute("user", user);
		model.addAttribute("vo", vo);
		return "/order/orderProductQuery/visa/visa_product_query_result";
	}
	
	@RequestMapping(value = "/ord/book/visa/infoFillIn.do")
	public String infoFillIn(HttpServletRequest request, ModelMap model) {
		
		String goodsId = request.getParameter("goodsId");
		String userId = request.getParameter("userId");
		SuppGoods suppGoods=null;
		SuppGoodsSimpleTimePrice suppGoodsSimpleTimePrice=null;
		List<SuppGoodsSaleRe> suppGoodsSaleReList = null;
		try {
			if(StringUtil.isNotEmptyString(goodsId)){
				ResultHandleT<SuppGoods> resultHandleT=distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END,Long.valueOf(goodsId));
				if(resultHandleT.getReturnContent()==null){
					model.addAttribute("ERROR","商品不可售");
					return ERROR_PAGE;
				}
				suppGoods = resultHandleT.getReturnContent();
				
				ResultHandleT<SuppGoodsSimpleTimePrice> timePriceResultHandleT=distGoodsTimePriceClientService.findFirstBookSuppGoodsSimpleTimePrice(Constant.DIST_BACK_END,suppGoods.getSuppGoodsId(), new Date());
				if(timePriceResultHandleT.hasNull()||timePriceResultHandleT.getReturnContent()==null){
					model.addAttribute("ERROR","商品没有价格");
					return "order/error";
				}
				suppGoodsSimpleTimePrice=timePriceResultHandleT.getReturnContent();
				
				suppGoodsSaleReList = getInsuranceList(suppGoods.getProductId(), suppGoodsSimpleTimePrice.getSpecDate());
			}
			
		}catch (Exception e) {
			LOG.error("{}", e);
		}

		//从cookie中读取用户信息
		UserUser user=null;
		if(StringUtil.isEmptyString(userId)){
			 user=readUserCookie();
		}else{
			user=userUserProxyAdapter.getUserUserByUserNo(userId);
		}
		model.addAttribute("suppGoodsSimpleTimePrice", suppGoodsSimpleTimePrice);
		model.addAttribute("suppGoods", suppGoods);
		model.addAttribute("user", user);
		model.put("canUseCoupons", PropertiesUtil.getValue("canUseCoupons").trim());		
		if(org.apache.commons.collections.CollectionUtils.isNotEmpty(suppGoodsSaleReList)){
			boolean flag=false;
			for(SuppGoodsSaleRe re:suppGoodsSaleReList){
				if(org.apache.commons.collections.CollectionUtils.isNotEmpty(re.getInsSuppGoodsList())){
					flag=true;
					break;
				}
			}
			model.addAttribute("suppGoodsSaleReList", suppGoodsSaleReList);
			model.addAttribute("existsInsurance",flag);
		}
		
		return VISA_BOOK_PAGE;
	}
	
	/**
	 * 保险
	 * @param productId
	 * @return
	 */
	@RequestMapping("/ord/book/visa/refereshInsurance.do")
	public String refereshInsurance(ModelMap model, Long productId,
			String visitTime) {
		Date visitDate = DateUtil.getDateByStr(visitTime, "yyyy-MM-dd");
		
		List<SuppGoodsSaleRe> suppGoodsSaleReList = null;
		try {
			suppGoodsSaleReList = getInsuranceList(productId, visitDate);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		model.addAttribute("suppGoodsSaleReList", suppGoodsSaleReList);
		
		return "/order/ticket/inc/insurance";
	}
	
	private List<SuppGoodsSaleRe> getInsuranceList(Long productId, Date visitDate) throws Exception{
		
		List<SuppGoodsSaleRe> goodsSaleReList = new ArrayList<SuppGoodsSaleRe>();
		if(productId == null){
			return goodsSaleReList;
		}
		
		if(visitDate == null || visitDate.getTime() < DateUtil.getTodayDate().getTime() ){
			return goodsSaleReList;
		}
		
		return prodPackLineClientRemote.getSuppGoodsSaleReList(productId, Constant.DIST_BACK_END, visitDate, 1L, 0L);
	}
	
	
	
	private List<Long> strConverList(String str){
		List<Long> idList = null;
		if(StringUtil.isNotEmptyString(str)){
			idList=new ArrayList<Long>();
			String[] ids=str.split(",");
			if(null!=ids&&ids.length>0){
				for (String item : ids) {
					if(StringUtil.isNotEmptyString(item)){
						idList.add(Long.valueOf(item));
					}
				}
			}
		}
		return idList;
	}
	
	@RequestMapping("/ord/book/visa/refereshTimePrice.do")
	@ResponseBody
	public Object refereshTimePrice(ModelMap model,String suppGoodsIds,String visitTime){
		List<SuppGoodsSimpleTimePrice> visaTimePriceList=new ArrayList<SuppGoodsSimpleTimePrice>();
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			List<Long> suppGoodsIdList = strConverList(suppGoodsIds);
			for (Long suppGoodsId : suppGoodsIdList) {
				ResultHandleT<SuppGoodsSimpleTimePrice> timePriceResultHandleT=distGoodsTimePriceClientService.findFirstBookSuppGoodsSimpleTimePrice(Constant.DIST_BACK_END,suppGoodsId, DateUtil.getDateByStr(visitTime, "yyyy-MM-dd"));
				if(timePriceResultHandleT.hasNull()||timePriceResultHandleT.getReturnContent()==null){
					throw new IllegalArgumentException("此商品"+visitTime+"无价格");
				}
				SuppGoodsSimpleTimePrice suppGoodsSimpleTimePrice=timePriceResultHandleT.getReturnContent();
				visaTimePriceList.add(suppGoodsSimpleTimePrice);
			}
			
		}catch (IllegalArgumentException e){
			LOG.error("{}", e);
			msg.raise(e.getMessage());
		}catch (Exception e){
			LOG.error("{}", e);
			msg.raise("查询时间价格表发生异常");
		}
		msg.addObject("visaTimePriceList", visaTimePriceList);
		return msg;
	}
	
	
	/**
	 * ajax查询签证产品名称
	 * @param search
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/visa/queryVisaProductList.do")
	public void queryVisaProductList(String search,HttpServletResponse response) throws BusinessException{
		//组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		if(NumberUtils.isNumber(search)){
			Long productId=Long.parseLong(search);
			params.put("productId", productId);
		}else{
			params.put("productName", search);
		}
		params.put("bizCategoryId", 4);
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
	
	/**
	 * ajax查询查询行政区域
	 * @param search
	 * @param response
	 * @throws BusinessException
	 *//*
	@RequestMapping(value="/ord/order/visa/queryVisaProductList.do")
	public void queryProductList(String search,HttpServletResponse response) throws BusinessException{
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("districtName", search);
		parameters.put("cancelFlag", "Y");
		List<BizDistrict> list=null;
		ResultHandleT<List<BizDistrict>>  resulthandleT= districtClientService.findDistrictList(parameters);
		if(resulthandleT.isSuccess()&&resulthandleT.getReturnContent()!=null){
			list=resulthandleT.getReturnContent();
		}
		
		//组装JSON数据BIZ_DISTRICT
		JSONArray jsonArray = new JSONArray();
		if(null != list && !list.isEmpty()){
			for(BizDistrict district:list){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", district.getDistrictId());
				jsonObject.put("text", district.getCityName());
				jsonArray.add(jsonObject);
			}
		}
		//返回JSON数据
		JSONOutput.writeJSON(response, jsonArray);
	}*/
}
