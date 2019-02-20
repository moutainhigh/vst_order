package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.dao.OrdOrderTrackingDao;
import com.lvmama.vst.order.service.IOrdOrderTrackingService;

@Service
public class OrdOrderTrackingServiceImpl implements IOrdOrderTrackingService {
	
	private Log log = LogFactory.getLog(OrdOrderTrackingServiceImpl.class);

	@Autowired
	private OrdOrderTrackingDao ordOrderTrackingDao;
	
	@Override
	public int updateByPrimaryKeySelective(OrdOrderTracking ordOrderTracking) {
		return ordOrderTrackingDao.updateByPrimaryKeySelective(ordOrderTracking);
	}
	
	@Override
	public int insert(OrdOrderTracking ordOrderTracking) {
		return ordOrderTrackingDao.insert(ordOrderTracking);
	}
	
	@Override
	public int updateOrderStatusByOrderIdAndStatus(Map<String, Object> paramsMap) {
		return ordOrderTrackingDao.updateOrderStatusByOrderIdAndStatus(paramsMap);
	}
	
	@Override
	public List<OrdOrderTracking> selectByOrderIdAndStatus(
			Map<String, Object> paramsMap) {
		return ordOrderTrackingDao.selectByOrderIdAndStatus(paramsMap);
	}
	
	@Override
	public int deleteByPrimaryKey(Long trackingId) {
		return ordOrderTrackingDao.deleteByPrimaryKey(trackingId);
	}
	
	@Override
	public List<OrdOrderTracking> findNowOrderStatusByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return ordOrderTrackingDao.selectNowOrderStatusByOrderId(orderId);
	}
	
	@Override
	public void saveOrderTracking(OrdOrderTracking ordOrderTracking) {
		if(null==ordOrderTracking){ 
			log.info("OrdOrderTrackingServiceImpl#saveOrderTracking: ordOrderTracking is null");
			return;
		}
		Map<String,Object> paramsMap = new HashMap<String,Object>();
		boolean isExistsOrderStatus = false;  //updateOrInsert 区分是否update
		paramsMap.put("orderId", ordOrderTracking.getOrderId()); //查询订单是否已有跟踪状态
		List<OrdOrderTracking> ordTrackingList= this.selectByOrderIdAndStatus(paramsMap); //查询出订单跟踪的信息  
		log.info("OrdOrderTrackingServiceImpl#saveOrderTracking:"+ordOrderTracking.getOrderId()+",orderStatus:"+ordOrderTracking.getOrderStatus());
		if(CollectionUtils.isNotEmpty(ordTrackingList)){ 
			for (OrdOrderTracking trackingVo: ordTrackingList) {
				log.info("OrdOrderTrackingServiceImpl#saveOrderTracking  orderId:"+trackingVo.getOrderId()+",orderStatus:"+trackingVo.getOrderStatus());
				if(OrderEnum.ORDER_TRACKING_STATUS.UNPAY.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.PART_PAY.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.TRANSFERRED.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.PAYED.getCode().equals(trackingVo.getOrderStatus())){  //支付、未支付、部分支付、资产转移判断更新状态
							if(OrderEnum.ORDER_TRACKING_STATUS.UNPAY.getCode().equals(ordOrderTracking.getOrderStatus())||
									OrderEnum.ORDER_TRACKING_STATUS.PART_PAY.getCode().equals(ordOrderTracking.getOrderStatus())||
									OrderEnum.ORDER_TRACKING_STATUS.TRANSFERRED.getCode().equals(ordOrderTracking.getOrderStatus())||
									OrderEnum.ORDER_TRACKING_STATUS.PAYED.getCode().equals(ordOrderTracking.getOrderStatus())){
								isExistsOrderStatus = true;
								trackingVo.setOrderStatus(ordOrderTracking.getOrderStatus());
								trackingVo.setUpdateTime(new Date());
								this.updateByPrimaryKeySelective(trackingVo);
								continue;
							}				
				}
				if(OrderEnum.ORDER_TRACKING_STATUS.CANCEL.getCode().equals(trackingVo.getOrderStatus())&&
						OrderEnum.ORDER_TRACKING_STATUS.CANCEL.getCode().equals(ordOrderTracking.getOrderStatus())){ //如果为取消订单
					isExistsOrderStatus = true;
					trackingVo.setUpdateTime(new Date());
					this.updateByPrimaryKeySelective(trackingVo);
					continue;
				}
				
				if(OrderEnum.ORDER_TRACKING_STATUS.CREDITED.getCode().equals(trackingVo.getOrderStatus())&&
						OrderEnum.ORDER_TRACKING_STATUS.CREDITED.getCode().equals(ordOrderTracking.getOrderStatus())){ //如果为凭证已生成
					isExistsOrderStatus = true;
					trackingVo.setUpdateTime(new Date());
					this.updateByPrimaryKeySelective(trackingVo);
					continue;
				}
			
				if(OrderEnum.ORDER_TRACKING_STATUS.ORDER_REFUNDMENT_PROCESSING.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.REFUND_SUCCESS_CALL.getCode().equals(trackingVo.getOrderStatus())){	//在线退款状态更新
						if(OrderEnum.ORDER_TRACKING_STATUS.ORDER_REFUNDMENT_PROCESSING.getCode().equals(ordOrderTracking.getOrderStatus())||
								OrderEnum.ORDER_TRACKING_STATUS.REFUND_SUCCESS_CALL.getCode().equals(ordOrderTracking.getOrderStatus())){
							isExistsOrderStatus = true;
							trackingVo.setOrderStatus(ordOrderTracking.getOrderStatus());
							trackingVo.setUpdateTime(new Date());
							this.updateByPrimaryKeySelective(trackingVo);
							continue;
						}
				}
				
				if(OrderEnum.ORDER_TRACKING_STATUS.COMMENT.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.COMMENT_SUCCESS.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.COMMENT_REFUND.getCode().equals(trackingVo.getOrderStatus())||
						OrderEnum.ORDER_TRACKING_STATUS.COMMENT_AUDIT_SUCCESS.getCode().equals(trackingVo.getOrderStatus())){//点评及未点评
						if(OrderEnum.ORDER_TRACKING_STATUS.COMMENT.getCode().equals(ordOrderTracking.getOrderStatus())||
								OrderEnum.ORDER_TRACKING_STATUS.COMMENT_SUCCESS.getCode().equals(ordOrderTracking.getOrderStatus())||
								OrderEnum.ORDER_TRACKING_STATUS.COMMENT_REFUND.getCode().equals(ordOrderTracking.getOrderStatus())||
								OrderEnum.ORDER_TRACKING_STATUS.COMMENT_AUDIT_SUCCESS.getCode().equals(ordOrderTracking.getOrderStatus())){
							isExistsOrderStatus = true;
							trackingVo.setOrderStatus(ordOrderTracking.getOrderStatus());
							trackingVo.setAdditionalContent(ordOrderTracking.getAdditionalContent());
							trackingVo.setUpdateTime(new Date());
							this.updateByPrimaryKeySelective(trackingVo);
							continue;
						}
				}
			}
		}else{
			this.insert(ordOrderTracking); //初次创建订单保存订单跟踪信息
			log.info("Insert trackingVo  orderId:"+ordOrderTracking.getOrderId()+",orderStatus:"+ordOrderTracking.getOrderStatus());
		}
		if(!isExistsOrderStatus&&CollectionUtils.isNotEmpty(ordTrackingList)){//如果不存在此订单状态，则直接save
			log.info("Insert trackingVo  orderId:"+ordOrderTracking.getOrderId()+",orderStatus:"+ordOrderTracking.getOrderStatus());
			this.insert(ordOrderTracking);
		}
		
	}
}
