package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.book.destbu.NewHotelBookComServiceImpl;
@Controller
public class OrderTestBackTest  extends BaseActionSupport  {
   
	private static final Log logger = LogFactory.getLog(OrderTestBackTest.class);
	@Autowired
	NewHotelBookComServiceImpl  newHotelBookComService;
	@RequestMapping("/ord/orde/testCreate.do") 
	@ResponseBody
	public Object createOrder(HttpServletRequest request){
		ResultMessage msg = ResultMessage.createResultMessage();
		try{
		DestBuBuyInfo buyInfo = new DestBuBuyInfo();
		buyInfo.setProductId(Long.valueOf(request.getParameter("productId")));
		buyInfo.setVisitTime(request.getParameter("vistiTime"));
		List<DestBuBuyInfo.Item> list = new ArrayList<DestBuBuyInfo.Item>();
		DestBuBuyInfo.Item item = new DestBuBuyInfo.Item();
		item.setGoodsId(Long.valueOf(request.getParameter("goodId")));
		item.setVisitTime(request.getParameter("vistiTime"));
		item.setProductCategoryId(32L);
		item.setQuantity(2);
	//	item.setHotelAdditation(add);
		list.add(item);
		DestBuBuyInfo.Item item2 = new DestBuBuyInfo.Item();
		item2.setGoodsId(Long.valueOf(request.getParameter("goodId2")));
		item2.setVisitTime(request.getParameter("vistiTime"));
		item2.setQuantity(2);
	//	item.setProductCategoryId(32L);
		list.add(item2);
		
		buyInfo.setItemList(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
	//	buyInfo.setDistributionChannel(distributionChannel)
	//	buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(4998810L);
		buyInfo.setUserId("8a80824b4e0a6fae014e0a6fae460000");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		logger.info("OrderTestBackTest----action -start--");
		ResultHandleT<OrdOrder>  orderresult =		newHotelBookComService.createOrder(buyInfo, "8a80824b4e0a6fae014e0a6fae460000");
		OrdOrder result = orderresult.getReturnContent();
		logger.info("OrderTestBackTest----action -end--result"+result.getOrderId());
		if(result.getOrderId()>0){
			logger.info("创建订单成功");
			msg.setMessage("sucess");
		}else{
			
			msg.setMessage("fail");
		}
	
   
	}catch(Exception e){
		msg.setMessage(e.getMessage());
	}
		return msg;

	}
	
}
