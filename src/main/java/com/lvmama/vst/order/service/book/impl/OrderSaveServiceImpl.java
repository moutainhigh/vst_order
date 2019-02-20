/**
 * 
 */
package com.lvmama.vst.order.service.book.impl;


import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.IOrderSaveService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.ticket.utils.DisneyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 为了将部分操作放在同一事务中，由于OrderSaveService不走事务，所以单独创建该service
 * @author lancey
 *
 */
@Service
public class OrderSaveServiceImpl extends AbstractBookService implements IOrderSaveService{

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderSaveServiceImpl.class);

	@Autowired
	private OrderSaveService orderSaveService;
	 @Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	 
	@Autowired
	protected IComplexQueryService complexQueryService;
	/**
	 * 保证事务一置性，保存订单相关信息，
	 * 库存扣除
	 * @param order
	 * @param asynchronousOrdUpdateStockList
	 * 异步扣除补偿恢复
	 */
	public List<HotelOrderUpdateStockDTO> saveOrder(OrdOrderDTO order,List<HotelOrderUpdateStockDTO> asynchronousOrdUpdateStockList){
		List<HotelOrderUpdateStockDTO> orderStockList = new ArrayList<HotelOrderUpdateStockDTO>();
		orderStockList = orderSaveService.deductStock(order,asynchronousOrdUpdateStockList);
		OrdOrder ord = queryOrdorderByOrderId(order.getOrderId());
		String specialTicket = getSpecialTicket(ord);
		logger.info("OrderSaveServiceImpl saveOrder specialTicket========="+specialTicket);
		
		//if(order.getDistributorId() == Constant.DIST_BACK_END||order.getDistributorId() == Constant.DIST_OFFLINE_EXTENSION || !isShanghaiDisneyOrder(order)){
		if(order.getDistributorId() == Constant.DIST_BACK_END||order.getDistributorId() == Constant.DIST_OFFLINE_EXTENSION || StringUtils.isEmpty(specialTicket)){
			 logger.info("不是ShanghaiDisney 订单 设置订单回复正常状态");
			 orderSaveService.resetOrderToNormal(order.getOrderId());
		}
		logger.info("save order, orderId:" + order.getOrderId() + " end...");
		return orderStockList;
	}
	
	private boolean isShanghaiDisneyOrder(OrdOrderDTO order){
		logger.info("check ShanghaiDisneyOrder-==============" + order.getOrderId());
		boolean flag=false;
		 List<OrdOrderItem> itemList=order.getOrderItemList();
		 for(OrdOrderItem item:itemList){
			 if(DisneyUtils.isDisney(item)){
				 flag=true;
				 break;
			 }
		 }
		 logger.info("check ShanghaiDisneyOrder-==============" + order.getOrderId()+"success+=========="+flag);
		return flag;
		
	}
	
	public OrdOrder queryOrdorderByOrderId(Long orderId) {
		return complexQueryService.queryOrderByOrderId(orderId);
	}
	
	public String getSpecialTicket(OrdOrder order) {
		logger.info("check getSpecialTicket-==============" + order.getOrderId());
		String specialTicket = "";
		if (CollectionUtils.isEmpty(order.getOrderItemList())) {
			return specialTicket;
		}
		for (OrdOrderItem item : order.getOrderItemList()) {
			String specialTicketType = item
					.getContentStringByKey("specialTicketType");
			if (SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_SHOW.getCode().equals(
					specialTicketType)
					|| SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_TICKET.getCode()
							.equals(specialTicketType)) {
				specialTicket = "上海迪士尼";
				break;
			}
			if (SuppGoods.SPECIAL_TICKET_TYPE.DALI_TICKET.getCode().equals(
					specialTicketType)) {
				specialTicket = "大理旅游联票";
				break;
			}
			if (SuppGoods.SPECIAL_TICKET_TYPE.AI_PIAO_TICKET.getCode().equals(
					specialTicketType)) {
				specialTicket = "爱票网";
				break;
			}
			if(SuppGoods.SPECIAL_TICKET_TYPE.XIAO_JING_TICKET.getCode().equals(specialTicketType)){
    			specialTicket = "小径平台";
    			break;
    		}
			if(SuppGoods.SPECIAL_TICKET_TYPE.ZHI_YOU_BAO_CHECK_TICKET.getCode().equals(specialTicketType)){
    			specialTicket = "智游宝";
    			break;
    		}
		}
		logger.info("check getSpecialTicket-==============" + order.getOrderId()
				+ "success+==========" + specialTicket);
		return specialTicket;
	}
}
