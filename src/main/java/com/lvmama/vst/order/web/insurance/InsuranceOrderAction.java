package com.lvmama.vst.order.web.insurance;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductPropClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.web.insurance.vo.OrderInsuranceProductVo;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
/**
 * 
 * 订单-保险业务
 * @author zhaomingzhu
 * 
 */
@Controller
public class InsuranceOrderAction extends BaseActionSupport{
	
	private static final long serialVersionUID = -7995320384912471134L;
	
	private static final Log logger = LogFactory.getLog(InsuranceOrderAction.class);
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	@Autowired
	private DictClientService dictClientService;
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
	private DistGoodsClientService distGoodsClientService;
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	@Resource(name="goodsOraTicketNotimeTimePriceStockService")
	private IGoodsTimePriceStockService goodsTicketNoTimePriceStockService;
	@Autowired
    private ProdProductPropClientService prodProductPropClientService;
	
	private UserUser getUserFromCookie(){
		UserUser user = new UserUser();
		Cookie userId = getCookie("phone_order_userId");
		Cookie userName = getCookie("phone_order_userName");
		
		if(userId != null && StringUtils.isNotEmpty(userId.getValue())){
			user.setUserId(userId.getValue());
		}
		if(userName != null && StringUtils.isNotEmpty(userName.getValue())){
			try {
				user.setUserName(URLDecoder.decode(userName.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
			}
		}
		logger.info("user login info,userId:=" + user.getUserId() + "userName:=" + user.getUserName());
		return user;
	}
	
	/**
	 * 准备查询保险业务
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/insurance/preQuery")
	public String preInsuranceQuery(Model model, HttpServletRequest request){
		
		//取到用户信息
		UserUser user = getUserFromCookie();
		if(StringUtil.isEmptyString(user.getUserId())){
			if(StringUtil.isNotEmptyString(request.getParameter("userId"))){
				user=userUserProxyAdapter.getUserUserByUserNo(request.getParameter("userId"));
			}
		}
		//险种
		List<BizDict> bizDictList = dictClientService.findDictListByDefId(new Long(516)).getReturnContent();
		//后台下单不能单独下取消险|门票退改险的单子
		Iterator<BizDict> bizDictIt = bizDictList.iterator();
		BizDict tempBizDict = null;
		while(bizDictIt.hasNext()) {
		    tempBizDict = bizDictIt.next();
		    if(null != tempBizDict.getDictId() && (tempBizDict.getDictId().intValue() == 738 || tempBizDict.getDictId().intValue() == 739)) {
		        bizDictIt.remove();
		    }
		}
		model.addAttribute("insurTypeDictList", bizDictList);
		
		model.addAttribute("user", user);
		
		return "/order/insurance/showInsuranceQuery";
	}
	
	@RequestMapping(value = "/ord/insurance/query")
	public String queryInsuranceList(OrderInsuranceProductVo vo, Integer page, Model model, HttpServletRequest request){
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		if(StringUtil.isNotEmptyString(vo.getProductId())){
			params.put("productId", vo.getProductId());
		}
		if(StringUtil.isNotEmptyString(vo.getProductName())){
			params.put("productName", vo.getProductName().trim());
		}
		if(StringUtil.isNotEmptyString(vo.getDaysType())){
			params.put("daysType", vo.getDaysType());
		}
		if(StringUtil.isNotEmptyString(vo.getInsurType())){
			String[] insurTypes=vo.getInsurType().split(",");
			List<String> prodValues=null;
			if(prodValues==null){
				prodValues=new ArrayList<String>();
			}
			for (String str : insurTypes) {
				prodValues.add(str);
			}
			params.put("prodValues", prodValues);
		}
		
		//产品
		List<ProdProduct> prodProductList=prodProductClientService.findInsuranceProductByCondition(params);
		
		HashMap<String,Object> p = new HashMap<String,Object>();
		HashMap<String,List<SuppGoods>> p2 = new HashMap<String,List<SuppGoods>>();
		for(ProdProduct pro : prodProductList){
			p.clear();
			p.put("productId", pro.getProductId());
			List<ProdProductProp> prodProductPropList = prodProductPropClientService.findProdProductPropListDetail(p).getReturnContent();
			pro.setProdProductPropList(prodProductPropList);
			//排除取消险和门票退改险
			List<String> excludeProductTypes = new ArrayList<String>(2);
			excludeProductTypes.add(ProdProduct.EX_INS_PRODUCTTYPE+"738");
			excludeProductTypes.add(ProdProduct.EX_INS_PRODUCTTYPE+"739");
			p.put("excludeProductTypes", excludeProductTypes);
			List<SuppGoods> sgntList = prodProductClientService.findSuppGoodsNotimeTimePriceListForInsurance(p);
			
			p2.put(pro.getProductId().toString(), sgntList);
		}
		
		List<ProdProduct> result=new ArrayList<ProdProduct>();
		//设置分页参数
		int pagenum = (page == null) ? 1 : page;
		Page pageParam = Page.page(prodProductList.size(), 10, pagenum);
		if(prodProductList.size()<=10){
			result=prodProductList;
		}else{
			result=prodProductList.subList(Integer.parseInt(String.valueOf(pageParam.getStartRows())), Integer.parseInt(String.valueOf(pageParam.getEndRows())));
		}
		pageParam.buildJSONUrl(request,true);
		pageParam.setItems(result);
		model.addAttribute("result", pageParam);
		model.addAttribute("sgntMap", p2);
		
		return "/order/insurance/queryInsuranceResult";
	}
	
	@RequestMapping(value = "/ord/insurance/book/authentication")
	public String preInsuranceBook(HttpServletRequest request, ModelMap model){
		String goodsId = request.getParameter("goodsId");
		String userId = request.getParameter("userId");
		SuppGoods suppGoods=null;
		SuppGoodsNotimeTimePrice suppGoodsSimpleTimePrice=null;
		try{
			if(StringUtil.isNotEmptyString(goodsId)){
				ResultHandleT<SuppGoods> resultHandleT=distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END,Long.valueOf(goodsId));
				if(resultHandleT.getReturnContent()==null){
					model.addAttribute("ERROR","商品不可售");
					return "order/error";
				}
				suppGoods = resultHandleT.getReturnContent();
				
				ResultHandleT<SuppGoodsNotimeTimePrice> timePriceResultHandleT = distGoodsTimePriceClientService.findSuppGoodsNotimeTimePriceList(Constant.DIST_BACK_END,suppGoods.getSuppGoodsId(), new Date());
				if(timePriceResultHandleT.hasNull()||timePriceResultHandleT.getReturnContent()==null){
					model.addAttribute("ERROR","商品没有价格");
					return "order/error";
				}
				suppGoodsSimpleTimePrice=timePriceResultHandleT.getReturnContent();
				
				Date date = new Date();
				if(suppGoodsSimpleTimePrice.getAheadBookTime()!=null){//如果存在提前预订时间，加上提交时间
					date = DateUtils.addMinutes(date, suppGoodsSimpleTimePrice.getAheadBookTime().intValue());
				}
				//避免时间上冲突，延后30分钟
				date = DateUtils.addMinutes(date, 30);
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				if(c.get(Calendar.HOUR_OF_DAY)>0||c.get(Calendar.MINUTE)>0){
					date = DateUtils.addDays(date, 1);
				}
				model.addAttribute("bookOrderVisitTime",date);				
			}
		}catch(Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		//取到用户信息
		UserUser user = getUserFromCookie();
		if(StringUtil.isEmptyString(user.getUserId())){
			if(StringUtil.isNotEmptyString(request.getParameter("userId"))){
				user=userUserProxyAdapter.getUserUserByUserNo(request.getParameter("userId"));
			}
		}
		
//		OrdOrderItem orderItem = new OrdOrderItem();
//		Date date  =orderItem.getVisitTime();
//		if(date == null){
//			date = DateUtil.getDayStart(DateUtils.addDays(new Date(),1));
//		}
//		SuppGoodsBaseTimePrice timePrice = goodsTicketNoTimePriceStockService.getTimePrice(suppGoods.getSuppGoodsId(),date, true);
//		SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice)timePrice;
//		
//		List<SuppGoodsRefund> list = suppGoodsClientService.getTicketRefund(suppGoods.getSuppGoodsId());
//		if(!list.isEmpty()){
//			SuppGoodsRefund suppGoodsRefund = list.get(0);
//			orderItem.setCancelStrategy(suppGoodsRefund.getCancelStrategy());
//			if(suppGoodsRefund.getLatestCancelTime()!=null){
//				orderItem.setLastCancelTime(DateUtils.addMinutes(orderItem.getVisitTime(), -suppGoodsRefund.getLatestCancelTime().intValue()));
//			}
//			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefund.getCancelStrategy())){
//				orderItem.setDeductType(suppGoodsRefund.getDeductType());
//				if(SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefund.getDeductType())){
//					orderItem.setDeductAmount(notimeTimePrice.getPrice()*orderItem.getQuantity()*suppGoodsRefund.getDeductValue()/10000);
//				}else{
//					orderItem.setDeductAmount(orderItem.getQuantity()*suppGoodsRefund.getDeductValue());
//				}
//			}			
//		}
		
		model.addAttribute("suppGoodsSimpleTimePrice", suppGoodsSimpleTimePrice);
		model.addAttribute("suppGoods", suppGoods);
		model.addAttribute("user", user);
		
		return "/order/insurance/showInsuranceBookInfo";
	}
}
