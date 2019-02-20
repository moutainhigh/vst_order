package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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
import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.back.play.connects.client.BizOrderConnectsServiceClientService;
import com.lvmama.vst.back.play.connects.client.SuppGoodsConnectsAdditionalClientService;
import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.back.play.connects.po.SuppGoodsConnectsAdditional;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.back.prod.vo.TicketProductForOrderVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import com.lvmama.vst.order.service.OrderConnectsServicePropService;
import com.lvmama.vst.order.vo.InsuranceSuppGoodsVo;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

@Controller
public class OrderBookCommonAction extends BaseOrderAciton {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderBookCommonAction.class);
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private FavorServiceAdapter favorService;
	
	@Autowired
	private BizOrderConnectsServiceClientService bizOrderConnectsServiceClientService;

	@Resource(name="ordOrderTravellerConfirmService")
	private OrdOrderTravellerConfirmService orderTravellerConfirmServiceLocal;
	
	@Autowired
	private SuppGoodsConnectsAdditionalClientService suppGoodsConnectsAdditionalClientService;
	
	@Autowired
	private OrderConnectsServicePropService orderConnectsServicePropService;
	
	@Resource(name="destCommissionedServiceAgreementService")
	private IOrderElectricContactService destCommissionedServiceAgreementService;//目的地委托服务协议
	
	@Resource(name="commissionedServiceAgreementService")
	private IOrderElectricContactService commissionedServiceAgreementService;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	/**
	 * 生成订单
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/comm/createOrder.do")
	public String createOrder(BuyInfo form,ModelMap model) throws BusinessException{
		//游玩人是否后置  默认为不后置
		model.addAttribute("isTravellerDelay", false);
		BuyInfo buyInfo = converForm(form);
		try{
			checkBuyInfo(buyInfo);
			initBooker(buyInfo);
			
		}catch(IllegalArgumentException ex){
			model.addAttribute("ERROR",ex.getMessage());
			return ERROR_PAGE;
		}

		buyInfo.setIp("180.169.51.82");
		ResultHandleT<OrdOrder> orderHandle = orderService.createOrder(buyInfo, getLoginUserId());
		if(orderHandle.isFail()){
			model.addAttribute("ERROR",orderHandle.getMsg());
			return ERROR_PAGE;
		}
		OrdOrder order=orderLocalService.queryOrdorderByOrderId(orderHandle.getReturnContent().getOrderId());
		
		int personNum=0;
		int insuranceNum=0;//保险份数
		boolean isExpressType=false;
		List<InsuranceSuppGoodsVo> bxGoodsList=new ArrayList<InsuranceSuppGoodsVo>();
        ProdProduct prodProduct = null;

        try{
            long productId=buyInfo.getProductId();
//            ProdProductParam param = new ProdProductParam();
//            param.setBizCategory(true);
            ResultHandleT<ProdProduct> productResultHandleT=prodProductClientService.findProdProductByIdFromCache(productId);
            prodProduct = productResultHandleT.getReturnContent();
            if(productResultHandleT.isSuccess()&&prodProduct!=null){
				 BizCategory bizCategory=prodProduct.getBizCategory();
					if(bizCategory!=null){
						if(ProductCategoryUtil.isTicket(bizCategory.getCategoryCode())){
							 Map<String, Object> productIdsMap = new HashMap<String, Object>();
						     Map<String, Object> suppGoodsIdsMap = new HashMap<String, Object>();
							List<Item> itemList = buyInfo.getItemList();
							if(null!=itemList&&itemList.size()>0){
                                Integer adultQuantity = 0;
                                Integer childQuantity = 0;
								for (Item item : form.getItemList()) {
									if(item.getQuantity()<=0){
										continue;
									}
									ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
									SuppGoods suppGoods=resultHandleT.getReturnContent();
									if(resultHandleT.isSuccess()&&resultHandleT.getReturnContent()!=null){
										if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
											isExpressType=true;
										}
										String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
										if(ProductCategoryUtil.isTicket(categoryCode)){
											item.setAdultQuantity(suppGoods.getAdult().intValue());
											item.setChildQuantity(suppGoods.getChild().intValue());
                                            adultQuantity += suppGoods.getAdult().intValue()*item.getQuantity();
                                            childQuantity += suppGoods.getChild().intValue()*item.getQuantity();
                                            suppGoodsIdsMap.put(String.valueOf(item.getGoodsId()), item.getQuantity());
										}else if(ProductCategoryUtil.isInsurance(categoryCode)){
											InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
											BeanUtils.copyProperties(suppGoods, insuranceVo);
											insuranceVo.setQuantity(item.getQuantity());
											insuranceNum+=item.getQuantity();
											bxGoodsList.add(insuranceVo);
										}
										item.setGoodType(suppGoods.getProdProduct().getBizCategory().getCategoryCode());
									}
								}
                                model.addAttribute("adult",adultQuantity);
                                model.addAttribute("child",childQuantity);
								personNum=getPersonCount(buyInfo);
							}
							
							List<Product> productList = buyInfo.getProductList();
							if(null!=productList&&productList.size()>0){
								for (Product product : productList) {
									if(product.getQuantity()<=0){
										continue;
									}
									Date beginDate=null;
									try {
										if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
											beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
										}else{
											beginDate = CalendarUtils.getDateFormatDate(product.getVisitTime(), "yyyy-MM-dd");// 到访时间
										}
										
									} catch (Exception e1) {
										LOG.error("{}", e1);
									} 
									 ResultHandleT<TicketProductForOrderVO> productVoResultHandleT=prodProductClientService.findTicketProductForOrder(product.getProductId(),beginDate);//
									 TicketProductForOrderVO ticketProductForOrderVO =productVoResultHandleT.getReturnContent();
									 List<SuppGoodsVO> goodsList= ticketProductForOrderVO.getSuppGoodsList();
									 for (SuppGoodsVO suppGoodsVO : goodsList) {
										 if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoodsVO.getGoodsType())){
												isExpressType=true;
										 }
									 }
									 productIdsMap.put(String.valueOf(product.getProductId()), product.getQuantity());
									 personNum+=(ticketProductForOrderVO.getAdultNumber()+ticketProductForOrderVO.getChildNumber())*product.getQuantity();
								}
							}
							if(CollectionUtils.isEmpty(bxGoodsList)){
								 ResultHandleT<Integer> handleT=orderRequiredClientService.findPackageProductTravNum(productIdsMap, suppGoodsIdsMap);
								 personNum=handleT.getReturnContent();
							}
						}else if(ProductCategoryUtil.isVisa(bizCategory.getCategoryCode())){

							List<Item> itemList = buyInfo.getItemList();
							if(null!=itemList&&itemList.size()>0){
								for (Item item : form.getItemList()) {
									if(item.getQuantity()<=0){
										continue;
									}
									ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
									SuppGoods suppGoods=resultHandleT.getReturnContent();
									if(resultHandleT.isSuccess()&&resultHandleT.getReturnContent()!=null){
										if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
											isExpressType=true;
										}
										String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
										if(ProductCategoryUtil.isTicket(categoryCode)){
											item.setAdultQuantity(suppGoods.getAdult().intValue());
											item.setChildQuantity(suppGoods.getChild().intValue());

										}else if(ProductCategoryUtil.isInsurance(categoryCode)){
											InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
											BeanUtils.copyProperties(suppGoods, insuranceVo);
											insuranceVo.setQuantity(item.getQuantity());
											insuranceNum+=item.getQuantity();
											bxGoodsList.add(insuranceVo);
										}
										item.setGoodType(suppGoods.getProdProduct().getBizCategory().getCategoryCode());
									}
								}
								personNum=getPersonCountForVisaCategory(buyInfo);
							}
							
							List<Product> productList = buyInfo.getProductList();
							if(null!=productList&&productList.size()>0){
								for (Product product : productList) {
									if(product.getQuantity()<=0){
										continue;
									}
									Date beginDate=null;
									try {
										if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
											beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
										}else{
											beginDate = CalendarUtils.getDateFormatDate(product.getVisitTime(), "yyyy-MM-dd");// 到访时间
										}
										
									} catch (Exception e1) {
										LOG.error("{}", e1);
									} 
									 ResultHandleT<TicketProductForOrderVO> productVoResultHandleT=prodProductClientService.findTicketProductForOrder(product.getProductId(),beginDate);//
									 TicketProductForOrderVO ticketProductForOrderVO =productVoResultHandleT.getReturnContent();
									 List<SuppGoodsVO> goodsList= ticketProductForOrderVO.getSuppGoodsList();
									 for (SuppGoodsVO suppGoodsVO : goodsList) {
										 if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoodsVO.getGoodsType())){
												isExpressType=true;
										 }
									 }
									 personNum+=(ticketProductForOrderVO.getAdultNumber()+ticketProductForOrderVO.getChildNumber())*product.getQuantity();
								}
							}
						
						}else if(ProductCategoryUtil.isRoute(bizCategory.getCategoryCode())){
								
							 
						}else if(ProductCategoryUtil.isInsurance(bizCategory.getCategoryCode())){
							for (Item item : buyInfo.getItemList()) {
								if(item.getQuantity()<=0){
									continue;
								}
								personNum+=item.getQuantity();
							}
						}else if (ProductCategoryUtil.isConnects(bizCategory.getCategoryCode())){

							for (Item item : buyInfo.getItemList()) {
								if(item.getQuantity()<=0){
									continue;
								}
								ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
								SuppGoods suppGoods=resultHandleT.getReturnContent();
								if(suppGoods!=null){
									if(suppGoods.getProdProduct()!=null){
										if(suppGoods.getProdProduct().getBizCategoryId().equals(41L)){
											personNum+=item.getQuantity();
											ResultHandleT<List<BizOrderConnectsProp>> bizOrderConnectResultHandleT=bizOrderConnectsServiceClientService.findConnectsServiceByBranchId(suppGoods.getBranchId());
											List<BizOrderConnectsProp> bizOrderConnectsPropList=bizOrderConnectResultHandleT.getReturnContent();
											model.addAttribute("bizOrderConnectsPropList", bizOrderConnectsPropList);
											ResultHandleT<SuppGoodsConnectsAdditional> suppGoodsConnectsAdditionalResultHandleT = suppGoodsConnectsAdditionalClientService.selectSuppGoodsConnectsAdditional(suppGoods.getSuppGoodsId());
											SuppGoodsConnectsAdditional suppGoodsConnectsAdditional=suppGoodsConnectsAdditionalResultHandleT.getReturnContent();
											if(suppGoodsConnectsAdditional.getCarpoolFlag()!=null&&suppGoodsConnectsAdditional.getCarpoolFlag().equals("N")){
												
												model.addAttribute("maxPeopleNum", suppGoodsConnectsAdditional.getMaxPeopleNum()*item.getQuantity());
												model.addAttribute("suppGoodsConnectsAdditional", suppGoodsConnectsAdditional);
											}
										}
									}
								}
							}
							
						}else if (ProductCategoryUtil.isPlay(bizCategory.getCategoryCode())){
							// 可以借用交通接驳的属性，但不是全部都一样
							// 判断是否是出境的产品
							if("FOREIGNLINE".equalsIgnoreCase(prodProduct.getProductType())){
								model.addAttribute("isPlayOutType",true);
							}
							for (Item item : buyInfo.getItemList()) {
								if(item.getQuantity()<=0){
									continue;
								}
								ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
								SuppGoods suppGoods=resultHandleT.getReturnContent();
								if(suppGoods!=null){
									if(suppGoods.getProdProduct()!=null){
										if(suppGoods.getProdProduct().getBizCategoryId().equals(43L)||suppGoods.getProdProduct().getBizCategoryId().equals(44L)||suppGoods.getProdProduct().getBizCategoryId().equals(45L)){
											personNum+=item.getQuantity();
											//Prop属性  借用了交通接驳的属性
											ResultHandleT<List<BizOrderConnectsProp>> bizOrderConnectResultHandleT=bizOrderConnectsServiceClientService.findConnectsServiceByBranchId(suppGoods.getBranchId());
											List<BizOrderConnectsProp> bizOrderPlayPropList=bizOrderConnectResultHandleT.getReturnContent();
											model.addAttribute("bizOrderConnectsPropList", bizOrderPlayPropList);
										}
									}
								}
							}
						}
					}
			}
		}catch(Exception e){
			LOG.error("{}", e);
		}
		
		List<Person> personList=null;
		OrderRequiredVO orderRequiredvO=null;
		try {
			orderRequiredvO=queryItemInfo(order);
            if (orderRequiredvO != null && isTicketBU(order)) {
                orderRequiredvO.setIdNumType(orderRequiredClientService.checkTicketBxCountForCredentNumType(orderRequiredvO, countTotalBx(order)));
            }

            /************************门票后台下单取消证件类型'客服联系我'的选项 张晓军 2015-04-15***********************************/
            BizCategory bizCategory=prodProduct.getBizCategory();
            if(bizCategory!=null) {
                if (ProductCategoryUtil.isTicket(bizCategory.getCategoryCode())) {
                    if(null != orderRequiredvO){
                        orderRequiredvO.setProductType("ticket");
                    }
                }
            }
            /******************************************************************************************/
			//获取常用联系人
			personList=orderService.loadUserReceiversByUserId(buyInfo.getUserId());
			
		}catch (Exception e){
			LOG.error("{}", e);
		}
		List<OrdPerson> tavellerList= new ArrayList<OrdPerson>(personNum);
		for (int i=0;i<personNum;i++){
			tavellerList.add(new OrdPerson());
		}		
		//门票和签证		
		Long couponAmount = favorService.getSumUsageAmount(order.getOrderId());
		if(null == couponAmount || couponAmount<0L){
			couponAmount = 0L;
		}
		order.setCouponAmount(couponAmount);
		//LOG.info("#TravNumType#"+orderRequiredvO.getTravNumType()+"#personList#"+personList.size()+"OccupType"+orderRequiredvO.getOccupType()+"ennameType"+orderRequiredvO.getEnnameType()+"#couponAmount#:"+couponAmount);
		model.addAttribute("order", order);
		model.addAttribute("isExpressType", isExpressType);
		model.addAttribute("bxGoodsList", bxGoodsList);
		model.addAttribute("personNum", personNum);
		model.addAttribute("insuranceNum", insuranceNum);
		model.addAttribute("personList", personList);
		model.addAttribute("orderRequiredvO", orderRequiredvO);
		model.addAttribute("contactPerson", new OrdPerson());
		model.addAttribute("emergencyPerson", new OrdPerson());
		model.addAttribute("tavellerList",tavellerList);
		model.addAttribute("categoryId",prodProduct.getBizCategory().getCategoryId());
		model.addAttribute("hasContractOrder",CollectionUtils.isNotEmpty(order.getOrdTravelContractList()));

		return "/order/orderFormInfo";
	}

    private boolean isTicketBU(OrdOrder order) {
        return BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId());
    }

	/**
	 * 保存订单游玩人信息
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/saveOrderPerson.do") 
	@ResponseBody
	public Object saveOrderPerson(BuyInfo buyInfo,Long orderId,String userId,ModelMap model) throws BusinessException{
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
		
		String methodName = "OrderBookCommonAction##saveOrderPerson==>"+buyInfo.getProductId();
		LOG.info(methodName + "saveOrderPerson保存订单游玩人信息方法中接收到的JSON##############"+ GsonUtils.toJson(buyInfo));
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setAttributes(attributes);
		try {
			ResultHandle resultHandle =orderLocalService.saveOrderPerson(orderId, buyInfo, getLoginUserId());
			if(resultHandle==null||resultHandle.isFail()){
				msg.raise(resultHandle == null ? "调用失败" : resultHandle.getMsg());
			}
			//保存常用游客
			savePerson(buyInfo.getTravellers(), userId);

			//游玩人后置的订单，需要保存游玩人确认信息
			if (Constants.Y_FLAG.equalsIgnoreCase(buyInfo.getTravellerDelayFlag())) {
				OrderTravellerOperateDO orderTravellerOperateDO = new OrderTravellerOperateDO();
				orderTravellerOperateDO.setChannelType(String.valueOf(VSTEnum.DISTRIBUTION.LVMAMABACK.getNum()));
				orderTravellerOperateDO.setUserCode(buyInfo.getUserId());
				buyInfo.getOrderTravellerConfirm().setOrderId(orderId);
				orderTravellerOperateDO.setOrderTravellerConfirm(buyInfo.getOrderTravellerConfirm());
				orderTravellerConfirmServiceLocal.saveOrUpdate(orderTravellerOperateDO);
			}
			
			//保存服务信息
			if(buyInfo.getOrderConnectsServicePropList()!=null&&orderId!=null&&orderId>0L){
				List<OrderConnectsServiceProp> connectsServiceList=buyInfo.getOrderConnectsServicePropList();
				for (OrderConnectsServiceProp orderConnectsServiceProp : connectsServiceList) {
					orderConnectsServiceProp.setOrderId(orderId);
					orderConnectsServicePropService.addOrderConnectsServiceProp(orderConnectsServiceProp);
				}
			}
			 OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			 //目的地后台下单
			 if(OrdOrderUtils.isDestBuBackOrder(order)){
				 createDestContract(order);
			 }
		}catch(IllegalArgumentException ex){
			LOG.error("{}", ex);
			msg.raise("保存订单发生异常.");
		}

		return msg;
	}
	
	/**
	 * 跳转到订单核对页
	 * @return
	 */
	@RequestMapping("/ord/book/showVerifyOrder.do")
	public String showVerifyOrder(Long orderId,ModelMap model) throws BusinessException{
		OrdOrder order=null;
		UserUser user=null;
		try {
				order = complexQueryService.queryOrderByOrderId(orderId);
				user=userUserProxyAdapter.getUserUserByUserNo(order.getUserId());
				//优惠总金额
				Long favorUsageAmount = favorService.getSumUsageAmount(order.getOrderId());
				order.setCouponAmount(favorUsageAmount);
		}catch(IllegalArgumentException ex){
			LOG.error("{}", ex);
			model.addAttribute("ERROR",ex.getMessage());
			return ERROR_PAGE;
		}
		//判断目的地订单
        boolean isDestBuOrder =OrdOrderUtils.isDestBuFrontOrder(order);
        model.addAttribute("isDestBuOrder",isDestBuOrder);
		//应付要减去奖金
		
		
		model.addAttribute("weekStr",DateUtil.getZHDay(order.getVisitTime()));
		model.addAttribute("order",order);
		model.addAttribute("user",user);
		return "/order/verifyOrder_AllCategory";
	}
	
	/**
	 * 提交订单
	 *
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/book/completeOrder.do")
	@ResponseBody
	public String completeOrder(Model model,Long orderId, HttpServletResponse response) throws BusinessException{
		 ResultHandle  handler = orderLocalService.startBackOrder(orderId, getLoginUserId());
//		 if(handler.isSuccess() && handler.getMsg()==null){		
//			 return "success";
//		 }
//		return "";
		return "success";
	}
	
	/**
	 * 取消订单
	 * @param model
	 * @param orderId
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/book/cancelOrder.do")
	@ResponseBody
	public String cancelOrder(Model model,Long orderId, HttpServletResponse response) throws BusinessException{
		 ResultHandle  handler = orderService.cancelOrder(orderId, OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name(), "核对订单页面取消", getLoginUserId(), "");
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
		 
		for (int i = 0; i < travellers.size(); i++) {
			Person person=travellers.get(i);
			if(person==null){
				travellers.remove(i);
				i--;
			}
		}
		orderService.createContact(travellers, userId);
	}
	
	private int getPersonCountForVisaCategory(BuyInfo buyInfo){
		List<Integer> list=new ArrayList<Integer>();
		for (Item item : buyInfo.getItemList()) {
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(item.getGoodType())){
				continue;
			}
			list.add(item.getQuantity());
		}
		Collections.sort(list);
		return list.get(list.size()-1);
	}
	
	private int getPersonCount(BuyInfo buyInfo){
		/*List<Integer> list=new ArrayList<Integer>();*/
		int personCount=0;
		for (Item item : buyInfo.getItemList()) {
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(item.getGoodType())){
				continue;
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equalsIgnoreCase(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equalsIgnoreCase(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equalsIgnoreCase(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equalsIgnoreCase(item.getGoodType())){
				/*list.add((item.getAdultQuantity()+item.getChildQuantity())*item.getQuantity());*/
				personCount+=(item.getAdultQuantity()+item.getChildQuantity())*item.getQuantity();
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equalsIgnoreCase(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equalsIgnoreCase(item.getGoodType())||
					BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equalsIgnoreCase(item.getGoodType())){
				/*list.add((item.getAdultQuantity()+item.getChildQuantity()));*/
			}
		}
		/*Collections.sort(list);
		if(list.size()>0){
			return list.get(list.size()-1);
		}*/
		return personCount;
	}
	
	/**
	 * 目的地后台下单工作流取消了人工确认，导致直接生成合同没有甲方（游玩人）
	 * 此处是点击确认订单，补偿生成合同
	 * @param order
	 */
	private void createDestContract(OrdOrder order){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", order.getOrderId());
		List<OrdTravelContract> contractList = ordTravelContractService.findOrdTravelContractList(params);
		if(CollectionUtils.isNotEmpty(contractList)){
			for (OrdTravelContract ordTravelContract : contractList) {
				 if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
					 destCommissionedServiceAgreementService.saveTravelContact(ordTravelContract, "SYSTEM");
				 }
				 if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
					 commissionedServiceAgreementService.saveTravelContact(ordTravelContract, "SYSTEM");
				 }
			}
		}	
		destCommissionedServiceAgreementService.sendOrderEcontractEmail(order, "SYSTEM");
	}

	/**
	 * 保存订单游玩人信息
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/saveNewOrderPerson.do") 
	@ResponseBody
	public Object saveNewOrderPerson(BuyInfo buyInfo,Long orderId,String userId,ModelMap model) throws BusinessException{
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
		
		String methodName = "OrderBookCommonAction##saveNewOrderPerson==>"+buyInfo.getProductId();
		LOG.info(methodName + "saveNewOrderPerson保存订单游玩人信息方法中接收到的JSON##############"+ GsonUtils.toJson(buyInfo));
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setAttributes(attributes);
		try {
//			ResultHandle resultHandle =orderLocalService.saveNewOrderPerson(orderId, buyInfo, getLoginUserId());
//			if(resultHandle==null||resultHandle.isFail()){
//				msg.raise(resultHandle == null ? "调用失败" : resultHandle.getMsg());
//			}
			//保存常用游客
			savePerson(buyInfo.getTravellers(), userId);

			//游玩人后置的订单，需要保存游玩人确认信息
			if (Constants.Y_FLAG.equalsIgnoreCase(buyInfo.getTravellerDelayFlag())) {
				OrderTravellerOperateDO orderTravellerOperateDO = new OrderTravellerOperateDO();
				orderTravellerOperateDO.setChannelType(String.valueOf(VSTEnum.DISTRIBUTION.LVMAMABACK.getNum()));
				orderTravellerOperateDO.setUserCode(buyInfo.getUserId());
				buyInfo.getOrderTravellerConfirm().setOrderId(orderId);
				orderTravellerOperateDO.setOrderTravellerConfirm(buyInfo.getOrderTravellerConfirm());
				orderTravellerConfirmServiceLocal.saveOrUpdate(orderTravellerOperateDO);
			}
			
			//保存服务信息
			if(buyInfo.getOrderConnectsServicePropList()!=null&&orderId!=null&&orderId>0L){
				List<OrderConnectsServiceProp> connectsServiceList=buyInfo.getOrderConnectsServicePropList();
				for (OrderConnectsServiceProp orderConnectsServiceProp : connectsServiceList) {
					orderConnectsServiceProp.setOrderId(orderId);
					orderConnectsServicePropService.addOrderConnectsServiceProp(orderConnectsServiceProp);
				}
			}
			 OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			 //目的地后台下单
			 if(OrdOrderUtils.isDestBuBackOrder(order)){
				 createDestContract(order);
			 }
		}catch(IllegalArgumentException ex){
			LOG.error("{}", ex);
			msg.raise("保存订单发生异常.");
		}

		return msg;
	}
}
