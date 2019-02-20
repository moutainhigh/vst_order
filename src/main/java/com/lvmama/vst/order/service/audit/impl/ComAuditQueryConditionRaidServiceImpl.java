package com.lvmama.vst.order.service.audit.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComAuditRaid;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.order.dao.ComAuditRaidDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.audit.ComAuditQueryConditionRaidService;

@Service
public class ComAuditQueryConditionRaidServiceImpl implements
		ComAuditQueryConditionRaidService {
	private final Logger LOG = LoggerFactory.getLogger(ComAuditQueryConditionRaidServiceImpl.class);
	
	@Autowired
	private ComAuditRaidDao comAuditRaidDao;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private OrdOrderStockDao orderStockDao;
	
	@Async
	@Override
	public void saveQueryConditionRaidData(ComAudit comAudit) {
		if(null !=comAudit && !"SYSTEM".equalsIgnoreCase(comAudit.getAuditFlag()))
		{
			LOG.info("saveQueryConditionRaidData,auditId:" + comAudit.getAuditId());
			//基础属性变量
			Long orderId = null;
			String objectType = null;
			OrdOrderItem ordOrderItem = null;
			
			ComAuditRaid comAuditRaid = new ComAuditRaid();
			comAuditRaid.setAuditId(comAudit.getAuditId());
			
			//判断活动类型属于子订单还是主订单			
			if(OBJECT_ORDER.equals(comAudit.getObjectType())){
				orderId = comAudit.getObjectId();
				objectType = OBJECT_ORDER;
			}else if(OBJECT_ORD_ITEM.equals(comAudit.getObjectType())){
				ordOrderItem = orderService.getOrderItem(comAudit.getObjectId());
				orderId = ordOrderItem.getOrderId();
				objectType = OBJECT_ORD_ITEM;
			}
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			//供应商&BU赋值			
			if(OBJECT_ORDER.equals(objectType)){
				comAuditRaid.setSupplierId(order.getMainOrderItem().getSupplierId());
				comAuditRaid.setBuCode(order.getBuCode());
				comAuditRaid.setCategoryId(order.getCategoryId());
			}else{
				comAuditRaid.setSupplierId(ordOrderItem.getSupplierId());
				comAuditRaid.setBuCode(ordOrderItem.getBuCode());
				comAuditRaid.setCategoryId(ordOrderItem.getCategoryId());
			}	
			
			//是否保留房、是否酒店订单
            if (OBJECT_ORDER.equals(objectType) && OrdOrderUtils.hasHotelItem(order)) {
                comAuditRaid.setStockFlag(OrdOrderUtils.hasStockFlag(order) ? "Y" : "N");
            } 
            if (OBJECT_ORD_ITEM.equals(objectType) && OrdOrderUtils.isHotelItem(ordOrderItem)) {
                List<OrdOrderStock> orderStockList = orderStockDao.selectByOrderItemId(ordOrderItem.getOrderItemId());
                ordOrderItem.setOrderStockList(orderStockList);
                comAuditRaid.setStockFlag(ordOrderItem.isRoomReservations() ? "Y" : "N");
            }
			//联系人联系电话
			setContactPerson(order, comAuditRaid);
			//根据打包类型获取产品ID
			setProductIdByPack(order, comAuditRaid);
			comAuditRaid.setOrdTag(order.getTag());
			comAuditRaid.setOrderCreateTime(order.getCreateTime());
			comAuditRaid.setOrderVisitTime(order.getVisitTime());
			comAuditRaid.setDistributorId(getDistIdOrChannel(order.getDistributorId(), order.getDistributionChannel()));
			comAuditRaid.setCreateTime(new Date());
			comAuditRaid.setTravellerDelayFlag(order.getTravellerDelayFlag());
			comAuditRaid.setTravellerLockFlag(order.getTravellerLockFlag());
			comAuditRaid.setPaymentTime(order.getPaymentTime());
			comAuditRaidDao.insertSelective(comAuditRaid);			
		}
	}
	
	/**
	 * 当distributorId=4时，取distributionChannel存入数据库
	 * @param distributorId 分销商ID
	 * @param distributionChannel 分销商具体渠道来源
	 * @return
	 * @author ltwangwei
	 * @date 2016-5-11 下午12:09:20
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	private Long getDistIdOrChannel(Long distributorId, Long distributionChannel){
		if(distributorId != null && distributorId == 4 && distributionChannel != null){
			return distributionChannel;
		}
		return distributorId;
	}
	
	/**
	 * 根据订单打包类型获取产品ID
	 * @param order
	 * @param comAuditRaid
	 */
	private void setProductIdByPack(OrdOrder order,ComAuditRaid comAuditRaid){
		Long productId = null;
		//打包产品
		Map<String, Object> paramPack = new HashMap<String, Object>();
		paramPack.put("orderId", order.getOrderId());//订单号			
		List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
		//判断是否是打包产品，不是打包产品取主子订单的产品ID
		if(CollectionUtils.isNotEmpty(orderPackList)){
			productId=orderPackList.get(0).getProductId();
		}else{
			productId = order.getMainOrderItem().getProductId();
		}
		comAuditRaid.setProductId(productId);
	}
	
	/**
	 * 获取订单联系人和联系电话
	 * @param order
	 * @param comAuditRaid
	 */
	private void setContactPerson(OrdOrder order,ComAuditRaid comAuditRaid){
		String contactName = null;
		String conectMobile = null;
		//获取联系人
		List<OrdPerson> personList = order.getOrdPersonList();
		if(CollectionUtils.isNotEmpty(personList)){
			for (OrdPerson ordPerson : personList){
				String personType = ordPerson.getPersonType();
				if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(personType)){
					contactName = ordPerson.getFullName();
					conectMobile = ordPerson.getMobile();
				}
			}
			//没有联系人则默认为第一个游玩人
			if(StringUtil.isEmptyString(contactName)){
				contactName = personList.get(0).getFullName();
				conectMobile = personList.get(0).getMobile();
			}
		}
		comAuditRaid.setContactName(contactName);
		comAuditRaid.setContactMobile(conectMobile);
	}
	
}
