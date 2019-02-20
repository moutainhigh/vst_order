package com.lvmama.vst.order.processer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.supp.service.SuppContractClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;

/**
 * 
 * 订单分摊完成推送分摊数据到财务
 *
 */
public class OrderApportionSendFinanceProcesser implements MessageProcesser{

	protected transient final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private OrderService orderService;
	
    @Autowired
    private SettlementService settlementService;
    
    @Autowired
	private IOrdTravelContractService ordTravelContractService;
    
    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
    
    @Autowired
	private ApportionInfoQueryService apportionInfoQueryService;
    
    @Autowired
    ApiSuppOrderService apiSuppOrderService;
    
	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterService;
	
	@Autowired
	private SuppContractClientService  suppContractClientService;
	
	@Override
	public void process(Message message) {
		
		if(MessageUtils.isOrderApportionSuccess(message)){
			// 废弃
			//excuteApportionMsg(message);
		}		
	}

	
	/**
	 * 处理分摊数据推送给财务
	 * @param message
	 */
	public void excuteApportionMsg(Message message){		
		Long orderId = message.getObjectId();
		logger.info("excuteApportionMsg orderid ="+orderId);
		OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		List<SetSettlementItem> settlementList = new ArrayList<SetSettlementItem>();
		if(CollectionUtils.isNotEmpty(orderItemList)){
			for (OrdOrderItem ordOrderItem : orderItemList) {
				SetSettlementItem settlementItem = new SetSettlementItem();
				settlementItem.setOrderId(orderId);
				settlementItem.setOrderItemMetaId(ordOrderItem.getOrderItemId());
				settlementItem.setApportionMessage(true);
				settlementItem.setPureApportion(true);
				ResultHandleT<SuppGoods> resultSuppGoods = suppGoodsHotelAdapterService.findSuppGoodsById(ordOrderItem.getSuppGoodsId());				
				if(null != resultSuppGoods && null != resultSuppGoods.getReturnContent()){
					SuppGoods suppGoods = resultSuppGoods.getReturnContent();
					Long contractId =suppGoods.getContractId();
					ResultHandleT<SuppContract> suppContractHandle = suppContractClientService.findSuppContractByContractId(contractId);
					if(null != suppContractHandle && null != suppContractHandle.getReturnContent()){
						SuppContract suppContract = suppContractHandle.getReturnContent();
						//合同编号
						settlementItem.setContractCode(suppContract.getContractNo());
					}
				}
				
				settlementItem.setOrderIsTermBill((byte) 0);
				Map<String, Object> contentMap = ordOrderItem.getContentMap();
				if(null != contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())){
					//是否期票
					String aperiodicFlag =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name());
					if(StringUtils.isNotEmpty(aperiodicFlag) && "Y".equalsIgnoreCase(aperiodicFlag)){
						settlementItem.setOrderIsTermBill((byte) 1);
					}
				}
				//酒店取间夜数
				if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())){
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderItemId", ordOrderItem.getOrderItemId());
					List<OrdOrderHotelTimeRate> listHotelRate = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
					if(CollectionUtils.isNotEmpty(listHotelRate)){
						settlementItem.setOrderItemRoomNight(listHotelRate.size());
					}					
				}	
				//调分摊接口
				OrderItemApportionInfoQueryVO orderApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
				orderApportionInfoQueryVO.setOrderId(orderId);
				orderApportionInfoQueryVO.setOrderItemId(ordOrderItem.getOrderItemId());
				OrderItemApportionInfoPO orderItemApportion = apportionInfoQueryService.calcOrderItemApportionInfo(orderApportionInfoQueryVO);
				//子单优惠总额
				settlementItem.setCouponAmount(orderItemApportion.getItemTotalCouponAmount());
				//子单促销金额
				settlementItem.setPromotionAmount(orderItemApportion.getItemTotalPromotionAmount());
				//子单手动改价金额
				settlementItem.setManualChangeAmount(orderItemApportion.getItemTotalManualChangeAmount());
				//子单实收金额
				settlementItem.setOrderItemActualReceived(orderItemApportion.getItemTotalActualPaidAmount());
				//子单退款总额
				settlementItem.setItemRefundedAmount(orderItemApportion.getTotalRefundAmount());
				//子单销售金额
				settlementItem.setOrdeItemSaleAmount(ordOrderItem.getTotalAmount());
				RequestSuppOrder requestSuppOrder = apiSuppOrderService.getSuppOrderByOrderItemId(ordOrderItem.getOrderItemId());
				if(null != requestSuppOrder){
					//供应商订单号
					settlementItem.setSupplierOrderCode(requestSuppOrder.getSuppOrderId());
				}
				settlementList.add(settlementItem);
				
			}
		}
		logger.info("excuteApportionMsg insertOrUpdateSettlementItem and settlementList size ="+settlementList.size());
		try {
			settlementService.insertOrUpdateSettlementItem(settlementList, com.lvmama.comm.vo.Constant.EVENT_TYPE.ORDER_APPORTION_SUCCESS_MSG);
		}catch (Exception e){
			logger.error("excuteApportionMsg , error msg is "+e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
}
