/**
 * 
 */
package com.lvmama.vst.order.web.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.OrderRequiredClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.back.prod.vo.TicketProductForOrderVO;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.vo.InsuranceSuppGoodsVo;

/**
 * @author pengyayun
 *
 */
@Controller
public class TicketOrderCreateAction extends BaseOrderAciton {
	
	private static final Log LOG = LogFactory.getLog(TicketOrderCreateAction.class);
	
	private final String ERROR_PAGE="/order/error";
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private IOrderLocalService ordOrderClientService;
	
	/**
	 * 生成订单
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/ticket/createOrder.do")
	public String createOrder(BuyInfo form,ModelMap model) throws BusinessException{
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
		OrdOrder order=orderHandle.getReturnContent();
		
		int personNum=0;//游玩人数
		int insuranceNum=0;//保险份数
		List<InsuranceSuppGoodsVo> bxGoodsList=new ArrayList<InsuranceSuppGoodsVo>();
		List<Long> productIdList=null;
		List<Long> suppGoodsIdList=null;
		
		try{
			List<Item> itemList = buyInfo.getItemList();
			if(null!=itemList&&itemList.size()>0){
					for (Item item : form.getItemList()) {
						if(item.getQuantity()<=0){
							continue;
						}
						if(suppGoodsIdList==null){
							suppGoodsIdList=new ArrayList<Long>();
						}
						suppGoodsIdList.add(item.getGoodsId());
						ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
						SuppGoods suppGoods=resultHandleT.getReturnContent();
						if(resultHandleT.isSuccess()&&resultHandleT.getReturnContent()!=null){
							String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
							if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equalsIgnoreCase(categoryCode)
                                    ||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equalsIgnoreCase(categoryCode)
                                    ||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equalsIgnoreCase(categoryCode)
                                    ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equalsIgnoreCase(categoryCode)){
							if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(categoryCode)){
								InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
								BeanUtils.copyProperties(suppGoods, insuranceVo);
								insuranceVo.setQuantity(item.getQuantity());
								insuranceNum+=item.getQuantity();
								bxGoodsList.add(insuranceVo);
							}else{
								item.setAdultQuantity(suppGoods.getAdult().intValue());
								item.setChildQuantity(suppGoods.getChild().intValue());
							}
							item.setGoodType(suppGoods.getProdProduct().getBizCategory().getCategoryCode());
						}
					}
					personNum+=getPersonCount(buyInfo);
				}
			}
			List<Product> productList = buyInfo.getProductList();
			if(null!=productList&&productList.size()>0){
				for (Product product : productList) {
					if(product.getQuantity()<=0){
						continue;
					}
					if(productIdList==null){
						productIdList=new ArrayList<Long>();
					}
					productIdList.add(product.getProductId());
					Date beginDate=null;
					try {
						if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
							beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
						}else{
							beginDate = CalendarUtils.getDateFormatDate(product.getVisitTime(), "yyyy-MM-dd");// 到访时间
						}
						
					} catch (Exception e1) {
						LOG.error(ExceptionFormatUtil.getTrace(e1));
					} 
					 ResultHandleT<TicketProductForOrderVO> productResultHandleT=prodProductClientService.findTicketProductForOrder(product.getProductId(),beginDate);//
					 TicketProductForOrderVO ticketProductForOrderVO =productResultHandleT.getReturnContent();
					 
					 personNum+=(ticketProductForOrderVO.getAdultNumber()*ticketProductForOrderVO.getChildNumber())*product.getQuantity();
				}
			}
			
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		OrdOrderItem orderItem=order.getMainOrderItem();
	 
		
		List<Person> personList=null;
		OrderRequiredVO orderRequiredvO=null;
		try {
			orderRequiredvO=queryItemInfo(order);
			
			//获取常用联系人
			personList=orderService.loadUserReceiversByUserId(getLoginUserId());
			
		}catch (Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		 
		model.addAttribute("orderItem", orderItem);
		model.addAttribute("order", order);
		model.addAttribute("bxGoodsList", bxGoodsList);
		model.addAttribute("personNum", personNum);
		model.addAttribute("insuranceNum", insuranceNum);
		model.addAttribute("personList", personList);
		model.addAttribute("orderRequiredvO", orderRequiredvO);
		model.addAttribute("weekStr",DateUtil.getZHDay(DateUtil.getDateByStr(buyInfo.getVisitTime(), "yyyy-MM-dd")));
		return "/order/ticket/ticketOrderFormInfo";
	}
	
	
	/**
	 * 保存为常用游客
	 * @param travellers
	 * @param userId
	 */
	private void savePerson(List<Person> travellers,String userId){
		//过滤掉已经是常用游客的数据
		/*for (int i = 0; i < travellers.size(); i++) {
			Person person=travellers.get(i);
			if("false".equals(person.getSaveFlag())){
				travellers.remove(i);
				i--;
			}
		}*/
		orderService.createContact(travellers, userId);
	}
	
	private int getPersonCount(BuyInfo buyInfo){
		List<Integer> list=new ArrayList<Integer>();
		for (Item item : buyInfo.getItemList()) {
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(item.getGoodType())){
				continue;
			}
			list.add((item.getAdultQuantity()+item.getChildQuantity())*item.getQuantity());
		}
		Collections.sort(list);
		return list.get(list.size()-1);
	}
	
	/*public static void main(String[] args) {
		BuyInfo buyInfo=new BuyInfo();
		List<Item> itemList=new ArrayList<Item>();
		Item item=new Item();
		item.setGoodsId(463350L);
		item.setQuantity(2);
		item.setAdultQuantity(1);
		itemList.add(item);
		Item item1=new Item();
		item1.setGoodsId(465212L);
		item1.setQuantity(1);
		item1.setAdultQuantity(1);
		itemList.add(item1);
		buyInfo.setItemList(itemList);
		//JSONObject jsObject=JSONObject.fromObject(buyInfo);
		//System.out.println(jsObject.toString());
		//{"additionalTravel":"true","booker":null,"categoryId":0,"contact":null,
		//"couponList":[],"distributionChannel":0,"distributionId":0,"distributorCode":"",
		//"emergencyPerson":null,"expressage":null,"faxMemo":"","guarantee":null,
		//"guaranteeRate":0,"invoiceInfo":null,"ip":"",
		//"itemList":[{"adultQuantity":0,"childQuantity":0,"goodType":"",
		//"goodsId":463350,"hotelAdditation":null,"itemPersonRelationList":[],
		//"mainItem":"","ownerQuantity":0,"priceTypeList":[],"quantity":2,
		//"visitTime":"","visitTimeDate":null},
		 //{"adultQuantity":0,"childQuantity":0,"goodType":"","goodsId":465212,
		//"hotelAdditation":null,"itemPersonRelationList":[],"mainItem":"",
		//"ownerQuantity":0,"priceTypeList":[],"quantity":2,"visitTime":"",
		//"visitTimeDate":null}],"itemMap":{},"needGuarantee":"","needInvoice":"",
		//"orderTotalPrice":0,"personRelationMap":{},"productId":0,"productList":[],
		///"productMap":{},"promotionIdList":[],"promotionMap":{},
		//"remark":"","sameVisitTime":"","submitOrderFlag":false,"travellers":[],
		//"userId":"","userNo":0,"visitTime":""}
		//JSONArray array=JSONArray.fromObject(jsObject.get("itemList"));
		// 
		 int result=new TicketOrderCreateAction().getPersonCount(buyInfo);
		System.out.println(result);
	}*/
}
