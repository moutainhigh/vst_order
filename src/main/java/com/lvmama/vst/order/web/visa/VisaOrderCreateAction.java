/**
 * 
 */
package com.lvmama.vst.order.web.visa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
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
public class VisaOrderCreateAction extends BaseOrderAciton {
	
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
	private OrderRequiredClientService orderRequiredClientService;
	
	@Autowired
	private IOrderLocalService ordOrderClientService;
	
	private static final Log LOG = LogFactory.getLog(VisaOrderCreateAction.class);
	/**
	 * 生成订单
	 * @param buyInfo
	 * @return
	 */
	@RequestMapping("/ord/book/visa/createOrder.do")
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
		Map<String,Long> insuranceNumMap=new HashMap<String, Long>();//保险份数
		List<InsuranceSuppGoodsVo> bxGoodsList=new ArrayList<InsuranceSuppGoodsVo>();
		try{
			List<Item> itemList = buyInfo.getItemList();
			if(null!=itemList&&itemList.size()>0){
				for (Item item : itemList) {
					if(item.getQuantity()<=0){
						continue;
					}
					ResultHandleT<SuppGoods> resultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, item.getGoodsId());
					SuppGoods suppGoods=resultHandleT.getReturnContent();
					if(resultHandleT.isSuccess()&&suppGoods!=null){
						String categoryCode=suppGoods.getProdProduct().getBizCategory().getCategoryCode();
						if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(categoryCode)){
							InsuranceSuppGoodsVo insuranceVo=new InsuranceSuppGoodsVo();
							BeanUtils.copyProperties(suppGoods, insuranceVo);
							insuranceVo.setQuantity(item.getQuantity());
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
			
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		/*OrdOrderItem orderItem=order.getMainOrderItem();*/
	 
		
		List<Person> personList=null;
		OrderRequiredVO orderRequiredvO=null;
		try {
			 
			orderRequiredvO=queryItemInfo(order);
			
			//获取常用联系人
			personList=orderService.loadUserReceiversByUserId(getLoginUserId());
			
		}catch (Exception e){
			
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		 
		/*model.addAttribute("orderItem", orderItem);*/
		model.addAttribute("order", order);
		model.addAttribute("personNum", personNum);
		model.addAttribute("bxGoodsList", bxGoodsList);
		model.addAttribute("personList", personList);
		model.addAttribute("weekStr",DateUtil.getZHDay(buyInfo.getItemList().get(0).getVisitTimeDate()));
		model.addAttribute("orderRequiredvO", orderRequiredvO);
		
		return "/order/visa/visaOrderFormInfo";
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
	
	/*private OrderRequiredVO queryItemInfo(List<Long> productIdList,List<Long> suppGoodsIdList){
		OrderRequiredVO vo = new OrderRequiredVO();
		if(productIdList!=null||suppGoodsIdList!=null){
			ResultHandleT<OrderRequiredVO> orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(productIdList, suppGoodsIdList);
			vo = (OrderRequiredVO)orderRequiredVO.getReturnContent();
		}
		vo.setTravNumType("TRAV_NUM_ALL");
		vo.setOccupType("TRAV_NUM_ONE");
		vo.setPhoneType("TRAV_NUM_ONE");
		vo.setEmailType("TRAV_NUM_ONE");
		vo.setIdNumType("TRAV_NUM_ONE");
		vo.setIdFlag("Y");
		vo.setPassportFlag("Y");
		vo.setPassFlag("Y");
		vo.setNeedTravFlag("Y");
		return vo;
	}*/
	
	
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
	
}
