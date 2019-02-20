package com.lvmama.vst.order.web.localplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.comm.search.vst.vo.VstTicketSearchVO;
import com.lvmama.vst.back.client.biz.service.OrderRequiredClientService;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.MarkCouponLimitClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * @author chenguangyao
 * @version 1.0
 * 2016-06-07
 * */
@Controller
public class LocalPlayBookAction extends BaseActionSupport{

	
	private static final long serialVersionUID = 7176008731841398822L;
	/**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(BaseActionSupport.class);
    
    private final String ERROR_PAGE = "/order/error";
    
    private final String LOCAL_PLAY_PAGE="/order/localplay/showlocalplayBookInfo";
    
    @Autowired
    private UserUserProxyAdapter userUserProxyAdapter;
    
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private MarkCouponLimitClientService markCouponLimitClientService;
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;//时间价格表

	@Autowired
	protected OrderRequiredClientService orderRequiredClientService;

    @RequestMapping(value = "/ord/productQuery/localPlay/showLocalPlayQueryList.do")
    public String showLocalPlayQueryList(Model model, HttpServletRequest reques){
    	
    	UserUser user = null;
        try {
            // 从cookie中读取用户信息
            user = readUserCookie();
            if (user == null || StringUtil.isEmptyString(user.getUserId())) {
                String userId = HttpServletLocalThread.getRequest().getParameter("userId");
                if (StringUtil.isNotEmptyString(userId)) {
                    user = userUserProxyAdapter.getUserUserByUserNo(userId);
                }
            }

        } catch (Exception e) {
            LOG.error("{}", e);
        }
        model.addAttribute("user", user);
        model.addAttribute("vo", new VstTicketSearchVO());
        
    	return "/order/orderProductQuery/localPlay/showLocalPlayQueryList";
    }
    
    
    @RequestMapping(value = "/ord/productQuery/localPlay/searchLocalPlayList.do")
    public String searchLocalPlayList(Long productId,String productName,Integer page,Model model,HttpServletRequest request){
    	
    	LOG.debug("searchLocalPlayList start");
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
			 List<Long> bizCategoryIdLst = new ArrayList<Long>();
	    	 bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId());
	    	 bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId());
	    	 bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId());
	    	 bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId());
			 params.put("bizCategoryIdLst",bizCategoryIdLst);
			ResultHandleT<List<ProdProduct>> resultHandleT = prodProductClientService.findWifiProductByparams(params);
			if(resultHandleT!=null && resultHandleT.getReturnContent()!=null&&resultHandleT.getReturnContent().size()>0){
				List<ProdProduct> prodProductList = resultHandleT.getReturnContent();
				List<ProdProduct> result =new ArrayList<ProdProduct>();
				
				if(!CollectionUtils.isEmpty(prodProductList)){
					for (int i = 0; i < prodProductList.size(); i++) {
						ProdProduct prodProduct=prodProductList.get(i);
						model.addAttribute("categoryId", prodProduct.getBizCategoryId());
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
    	
    	return "/order/orderProductQuery/localPlay/localplay_product_query_result";
    }
    
    
    /**
     * ajax查询当地玩产品名称
     * @param search
     * @param response
     * @throws BusinessException
     * */
    @RequestMapping(value = "/ord/productQuery/localPlay/queryLocalPlayProductList.do")
    public void queryLocalPlayProductList(String search,HttpServletResponse response)throws BusinessException{
    	
    	Map<String, Object> params = new HashMap<String, Object>();
    	
    	if(NumberUtils.isNumber(search)){
			Long productId=Long.parseLong(search);
			params.put("productId", productId);
		}else{
			params.put("productName", search);
		}
    	List<Long> bizCategoryIdLst = new ArrayList<Long>();
    	bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId());
    	bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId());
    	bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId());
    	bizCategoryIdLst.add(BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId());
		params.put("bizCategoryIdLst",bizCategoryIdLst);
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
    
    
    
    @RequestMapping(value = "/ord/book/localPlay/infoFillIn.do")
	public String infoFillIn(HttpServletRequest request,Long goodsId,ModelMap model) throws Exception {
    	
    	
    	String userId = request.getParameter("userId");
		
		String result=ERROR_PAGE;
		int checkLimit =  0 ;
		Long checkId = null ;
		if(goodsId!=null){
			result=loadGoods(model, goodsId);
			checkId = Long.valueOf(goodsId);
			
		}

		ResultHandleT<SuppGoods> resultHandleT = null;
		resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, goodsId);
		SuppGoods suppGoods = resultHandleT.getReturnContent();
		Long categoryId = suppGoods.getCategoryId();


		OrderRequiredVO orderRequiredVO = getOrderRequiredVO(goodsId);
		String useTimeFlag = orderRequiredVO.getUseTimeFlag();
		String localHotelAddressFlag = orderRequiredVO.getLocalHotelAddressFlag();
		if(suppGoods.getProdProduct().getBizCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId())
			||suppGoods.getProdProduct().getBizCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId())){
			model.addAttribute("categoryId",categoryId);
			model.addAttribute("useTimeFlag",useTimeFlag);
			model.addAttribute("localHotelAddressFlag",localHotelAddressFlag);
		}

		//判断是否支持优惠券			
		/*try {
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
		}*/
		

		//从cookie中读取用户信息
		UserUser user=null;
		if(StringUtil.isEmptyString(userId)){
			 user=readUserCookie();
		}else{
			user=userUserProxyAdapter.getUserUserByUserNo(userId);
		}
		 
		model.addAttribute("user", user);
		model.addAttribute("isFaxBreakRemark",false);
		model.addAttribute("islocalPlay",true);
		useTime(goodsId,model);
		return result;
    }

	private OrderRequiredVO getOrderRequiredVO(Long goodsId){
		List<Long> suppGoodsIdList = new ArrayList<Long>();
		suppGoodsIdList.add(goodsId);
		ResultHandleT<OrderRequiredVO> orderRequiredVO = null;
		orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(null, suppGoodsIdList);
		OrderRequiredVO requiredVO = orderRequiredVO.getReturnContent();
		return requiredVO;
	}


    private String loadGoods(ModelMap model,Long goodsId){
    	
    	String result = ERROR_PAGE;
		SuppGoods suppGoods = null;
		ProdProduct product = null;
		ResultHandleT<SuppGoods> suppGoodsResultHandleT;
		try {
			suppGoodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END,goodsId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			model.addAttribute("ERROR","商品不可售(查询商品数据异常)");
			return ERROR_PAGE;
		}
		if(suppGoodsResultHandleT.hasNull()||suppGoodsResultHandleT.getReturnContent()==null){
			model.addAttribute("ERROR","商品不可售(商品不存在)");
			return ERROR_PAGE;
		}
    	
		
    	suppGoods = suppGoodsResultHandleT.getReturnContent();
		
		if("Y".equalsIgnoreCase(suppGoods.getPackageFlag())){
			model.addAttribute("ERROR","此商品仅组合销售!");
			return ERROR_PAGE;
		}

		ResultHandleT<ProdProduct> productResultHandleT = prodProductClientService.findProdProductByIdFromCache(suppGoods.getProductId());
		if(productResultHandleT.hasNull()||productResultHandleT.getReturnContent()==null){
			model.addAttribute("ERROR","商品不可售(产品不存在)");
			return ERROR_PAGE;
		}
		product = productResultHandleT.getReturnContent();
		if(!"Y".equalsIgnoreCase(product.getSaleFlag())){
			model.addAttribute("ERROR","该商品不可售!");
			return ERROR_PAGE;
		}
		result=loadLocalPlaySuppGoods(model, suppGoods, product);

		return result;
    }
    
    private String loadLocalPlaySuppGoods(ModelMap model,SuppGoods suppGoods,ProdProduct product){

		SuppGoodsAddTimePrice timePrice = null;
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice=null;
		
    	if(!BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(product.getBizCategoryId()) 
    			&&!BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(product.getBizCategoryId()) 
    			&&!BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(product.getBizCategoryId()) 
    			&&!BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(product.getBizCategoryId()) 
    			){
			model.addAttribute("ERROR","商品不可售(不是该品类下的商品)");
			return ERROR_PAGE;
		}
    	
    	HashMap<String,Object> paramss = new HashMap<String,Object>();
		paramss.put("suppGoodsId", suppGoods.getSuppGoodsId());
		paramss.put("date", new Date());
		paramss.put("orderByClause", "sgr.SPEC_DATE");
        ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT=suppGoodsTimePriceClientRemote.getFirstTimePrice(paramss);
        suppGoodsBaseTimePrice=timePriceResultHandleT.getReturnContent();
        if(timePriceResultHandleT.isFail()||suppGoodsBaseTimePrice==null){
			model.addAttribute("ERROR","商品不可售(时间价格未设置)");
			return ERROR_PAGE;
        }
        timePrice =  (SuppGoodsAddTimePrice)suppGoodsBaseTimePrice;
    	
    	model.addAttribute("suppGoods",suppGoods);
		model.addAttribute("product",product);
		model.addAttribute("startDate",timePrice.getSpecDate());
    	return LOCAL_PLAY_PAGE;
    }
    
    @RequestMapping("/ord/book/localPlay/getLocalPlayTimePrice.do")
	@ResponseBody
    public Object getLocalPlayTimePrice(ModelMap model,Long suppGoodsId,String visitTime){
    	
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
	 * 使用时间加载
	 * @param suppGoodsId
	 * @param request
	 * @param model
	 * @return
	 */    
    public void useTime(Long suppGoodsId, ModelMap model) {
    	
		ResultHandleT<SuppGoods> suppGoodsResultHandleT = null;
		try {
			suppGoodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, suppGoodsId);
		} catch (Exception e) {
			
		}
		if(suppGoodsResultHandleT!=null && suppGoodsResultHandleT.getReturnContent()!=null){
			SuppGoods good = suppGoodsResultHandleT.getReturnContent();
			if(good ==null){
				return;
			}
			String useTime = good.getGoodsDesc();
			List<String> times =null;
			if (StringUtils.isNotEmpty(useTime)) {
					String[] usetimeArray = useTime.split(",");
					times=Arrays.asList(usetimeArray);
			}
			if (CollectionUtils.isNotEmpty(times)) {
				model.addAttribute("useTimeList", times);
			}
			
			model.addAttribute("suppGoodsId",good.getSuppGoodsId());
			
		}
    	
    }
    
}
