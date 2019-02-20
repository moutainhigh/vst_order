package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.PreRefundCounterDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.PreRefundService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

@Service
public class PreRefundServiceImpl implements PreRefundService {

	private static final Log LOG = LogFactory.getLog(PreRefundServiceImpl.class);
	
	@Autowired
	private PreRefundCounterDao preRefundCounterDao;
	
	@Autowired
    private OrderService orderService;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Override
	public int selectPreRefundCountByOrderId(Long orderId) {
		int count1 = 0;
		int count2 = 0;
		LOG.info("selectPreRefundCountByOrderId param:"+orderId);
		try {
			OrdOrder order =orderService.querySimpleOrder(orderId);
			if(order!=null){
				count1 = selectPreRefundCountByUserId(order.getUserNo());
				UserUser user = userUserProxyAdapter.getUserUserByPk(order.getUserNo());
				if(user!=null && StringUtil.isNotEmptyString(user.getMobileNumber())){
					count2 = selectPreRefundCountByMobie(user.getMobileNumber());
				}
			}
		} catch (Exception e) {
			LOG.error("selectPreRefundCountByOrderId error:"+e.getMessage());
		}
		return Math.max(count1, count2);
	}

	@Override
	public int selectPreRefundCountByUserId(Long userId) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return preRefundCounterDao.selectCount(params);
	}

	@Override
	public int selectPreRefundCountByMobie(String mobie) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("mobie", mobie);
		return preRefundCounterDao.selectCount(params);
	}

	@Override
	public void increase(Long orderId) {
		LOG.info("increase param:"+orderId);
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("preRefundStatus", OrderEnum.PRE_REFUND_STATUS.COMPLETE.name());
			params.put("orderId", orderId);
			orderUpdateService.updatePreRefundStatus(params);
			
			OrdOrder order =orderService.querySimpleOrder(orderId);
			if(order !=null){
				UserUser user = userUserProxyAdapter.getUserUserByPk(order.getUserNo());
				String mobie = "";
				if(user!=null){
					mobie = user.getMobileNumber();
				}
				preRefundCounterDao.increase(order.getUserNo(), mobie);
			}
		} catch (Exception e) {
			LOG.error("increase error:"+e.getMessage());
		}
		     
	}
	
	@Override
	public boolean isPreRefundCountLessThan3(Long orderId) {
		return selectPreRefundCountByOrderId(orderId) < 3 ;
	}
	
	@Override
	public boolean canPreRefund(Long orderId){
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		return canPreRefund(order);
	}
	
	
	@Override
	public boolean canPreRefund(OrdOrder order){
		LOG.info("canPreRefund:"+order.getOrderId());	
		
		if(OrderEnum.PAYMENT_STATUS.UNPAY.name().equals(order.getPaymentStatus())
				||SuppGoods.PAYTARGET.PAY.name().equals(order.getPaymentTarget())
				||OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())){ //未支付   现付  已取消
			return false;  
		}
		boolean isLvtu = order.getDistributorCode()!=null?order.getDistributorCode().contains("LVTU"):false;
		boolean isWAP = order.getDistributorCode()!=null?order.getDistributorCode().contains("TOUCH"):false;
		if(!(Constant.DIST_FRONT_END == order.getDistributorId() || Constant.DIST_BACK_END == order.getDistributorId() || isLvtu || isWAP)){ //非前台 ，后台，驴妈妈app ，驴妈妈wap下单
			return false;  
		}
		if(null != order.getOrdOrderPack() && ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(order.getOrdOrderPack().getOwnPack())){ //自主打包
			return false;  
		}
		for(OrdOrderItem orderItem :order.getOrderItemList()){
			
			Map<String,Object> contentMap = orderItem.getContentMap();
			String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			String preRefundFlag = (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.pre_refund_flag.name());
			
			if(!(ProductCategoryUtil.isTicket(categoryCode)) || !"Y".equals(preRefundFlag)){ //子订单包含非门票 或  非提前退
				return false;  
			}
		}
		if(!isPreRefundCountLessThan3(order.getOrderId())){ //提前退次数不满足小于三次
			LOG.info("!isPreRefundCountLessThan3");
			return false;    
		}
		
		return true;
	}
	
}
