package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.OrdOrderItemPassCodeSMSService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;
import com.lvmama.vst.order.service.IOrderUpdateService;
/**
 * Create at 2015/9/2
 * @author yangzhenzhong
 * 处理新增的需求（景+酒中，门票不支持当天）
 */
@Component("ordOrderItemPassCodeSMSServiceRemote")
public class OrdOrderItemPassCodeSMSRemoteServiceImpl implements OrdOrderItemPassCodeSMSService {

	@Autowired
	private IOrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSLocalService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;
	@Autowired
	private IComMessageService comMessageService;

	
	
	private static Logger logger = LoggerFactory.getLogger(OrdOrderItemPassCodeSMSRemoteServiceImpl.class);
	/**
	 * 新增发短信逻辑
	 * 如果主订单BU是目的地,品类是自由行（打包门票），门票都可退改且已支付
	 */
	@Override
	public boolean sendPassCodeSMSHandle(Long passCodeId, OrdOrder order,OrdOrderItem orderItem, List<OrdOrderItem> orderItemList) {
		
		boolean sendMsgFlag = true;
		
		boolean isExist = false;
		
		isExist = ordOrderItemPassCodeSMSLocalService.isExistOfFlagData(order.getOrderId());
		
		if(isExist){//如果标识数据存在，则需要延迟发短信
			
			String status= "Y";
			sendMsgFlag = false;
			
			order = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
			
			//申码过程，资源审核过程都是通过JMS实现的异步操作，因此会造成先后不一。
			//如果该订单信息审核和资源审核都已经通过，此时资源审核完成动作早于申码成功的动作，就照常发送短信，不做延迟处理
			if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(order.getInfoStatus()) && OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())){
				sendMsgFlag = true;
				//延迟发送短信的记录status设为N,表示为已处理。
				status="N";
			}
			OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS=new OrdOrderItemPassCodeSMS();
    		ordOrderItemPassCodeSMS.setCreateTime(new Date());
    		ordOrderItemPassCodeSMS.setUpdateTime(new Date());
    		ordOrderItemPassCodeSMS.setOrderId(order.getOrderId());
    		ordOrderItemPassCodeSMS.setPassCodeId(passCodeId);
    		ordOrderItemPassCodeSMS.setStatus(status);
    		ordOrderItemPassCodeSMSLocalService.inert(ordOrderItemPassCodeSMS);
		}
		
		return sendMsgFlag;
	}
	
	/**
	 * 如果主订单BU是目的地,品类是自由行（打包门票），门票都可退改且已支付
	 * 
	 */
	@Override
	public boolean createOrderHandle(OrdOrder order, List<OrdOrderItem> orderItemList) {
		try{
			//如果主订单BU是目的地,品类是自由行（打包门票），门票都可退改且已支付
			if (order != null && orderItemList != null && orderItemList.size()>0 && order.hasPayed() 
					&& order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.name())
					&& BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())) {
				
				boolean flag = false;// 是否有门票
				boolean ifRefund = true;// 是否可退改，且退款金额为0
				boolean allRoomReservations=true; //是否都是保留房
				for(OrdOrderItem orderItem :orderItemList){
					//如果子订单类型是景点门票，其它票
					if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getCategoryId()) 
							|| BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId()) 
							|| BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId()) 
	                        || BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(orderItem.getCategoryId())){
						flag = true;
						List<SuppGoodsRefundVO> refundList = SuppGoodsRefundTools.calcDeductAmt(orderItem, null, null);
						for(SuppGoodsRefundVO suppGoodsRefund: refundList ){
							if(!SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefund.getCancelStrategy())
									|| suppGoodsRefund.getDeductAmt() > 0L){
								ifRefund = false;
								break;
							}
						}
						if(!ifRefund){
							break;
						}
						
						
					}
				}
				
				for(OrdOrderItem orderItem :orderItemList){
					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()) 
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
						if(!orderItem.isRoomReservations()){
							allRoomReservations=false;
							break;
						}
					}
				}
				
				if(flag && ifRefund){// 包含门票，且门票可退改
					return true;
				}
				
				//包含门票，保留房提前申码
				if(flag&&allRoomReservations){
					return true;
				}
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return false;
	}
	
	/**
	 * 插入一条PassCodeId为0的标识，该数据只需存在一条。
	 */
	@Override
	public void inertFlagData(Long orderId) {
			
		OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS=new OrdOrderItemPassCodeSMS();
		ordOrderItemPassCodeSMS.setCreateTime(new Date());
		ordOrderItemPassCodeSMS.setUpdateTime(new Date());
		ordOrderItemPassCodeSMS.setOrderId(orderId);
		ordOrderItemPassCodeSMS.setPassCodeId(0l);
		ordOrderItemPassCodeSMS.setStatus("N");
		
		try {
			ordOrderItemPassCodeSMSLocalService.inert(ordOrderItemPassCodeSMS);
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
	}

	@Override
	public int saveReservationOrder(Long orderId, String auditSubType, String assignor, String memo, boolean bigTrafficValidate) throws Exception {
		return comMessageService.saveReservationOrder(orderId,auditSubType,assignor,memo,bigTrafficValidate);
	}
}