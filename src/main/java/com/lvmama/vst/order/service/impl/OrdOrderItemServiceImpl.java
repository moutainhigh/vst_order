package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.order.dao.OrdOrderItemExtendDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrderEnum.COURIER_STATUS;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderLocalService;

@Service
public class OrdOrderItemServiceImpl implements IOrdOrderItemService{
	
	private static Logger logger = LoggerFactory.getLogger(OrdOrderItemServiceImpl.class);
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	@Autowired
	private OrdOrderItemExtendDao ordOrderItemExtendDao;

	@Autowired
	private OrdPassCodeDao ordPassCodeDao;
	
	@Autowired
	private IOrderLocalService ordOrderClientService;
	
	private final int QUERY_LIST_COUNT = 1000;
	/**
	 * 根据id集合查询订单子项集合
	 * @param ids
	 * @return
	 */
	@Override
	public List<OrdOrderItem> selectOrderItemsByIds(List<Long> ids) {
		if(ids.size() <= QUERY_LIST_COUNT) {
			return ordOrderItemDao.selectOrderItemsByIds(ids);
		}
		List<OrdOrderItem> list =new ArrayList<OrdOrderItem>();
		int fromIndex = 0;
		int toIndex = 0;
		while(fromIndex < ids.size()) {
			if(fromIndex + QUERY_LIST_COUNT >= ids.size()) {
				toIndex = ids.size();
			} else {
				toIndex = fromIndex + QUERY_LIST_COUNT;
			}
			list.addAll(ordOrderItemDao.selectOrderItemsByIds(ids.subList(fromIndex, toIndex)));
			
			fromIndex = toIndex;
		}
		
		return list;
	}
	
	/**
	 * 根据订单ID查询订单子项集合
	 * @param orderId
	 * @return
	 */
	@Override
	public List<OrdOrderItem> selectByOrderId(Long orderId) {
		List<OrdOrderItem> ordOrderItems = ordOrderItemDao.selectByOrderId(orderId);
		if (CollectionUtils.isNotEmpty(ordOrderItems)) {
			for (OrdOrderItem ordOrderItem : ordOrderItems) {
				OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendDao.selectByPrimaryKey(ordOrderItem.getOrderItemId());
				ordOrderItem.setOrdOrderItemExtend(ordOrderItemExtend);
			}
		}
		return ordOrderItems;
	}
	
	
	/**
	 * 根据下单时间和产品统计orderItem数量
	 * @param params
	 * @return
	 */
	public Integer countOrderItemByCreateTimeAndProductId(Map<String, Object> params) {
		return ordOrderItemDao.countOrderItemByCreateTimeAndProductId(params);
	}

	@Override
	public Integer updateByPrimaryKeySelective(OrdOrderItem ordOrderItem) {
		return ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItem);
	}

	/**
	 * 根据产品ID和门店ID查询指定时间内的有效订单的数量 
	 * @param params
	 * @return
	 */
	@Override
	public Integer countOrderMainItemForO2oTicketByProductId(Map<String, Object> params) {
		if (params.get("productId")==null
			||params.get("distributorId")==null
			||params.get("beginTime")==null
			||params.get("endTime")==null){
			return 0;
		}
		return ordOrderItemDao.countOrderMainItemForO2oTicketByProductId(params);
	}
	
	@Override
	public List<OrdOrderItem> selectOrderItemsByorderIds(List<Long> orderIds) {
		// TODO Auto-generated method stub
		return ordOrderItemDao.selectOrderItemsByorderIds(orderIds);
	}

	@Override
	public OrdOrderItem selectOrderItemByOrderItemId(Long orderItemId) {
		return ordOrderItemDao.selectByPrimaryKey(orderItemId);
	}

	@Override
	public List<OrdOrderItem> queryOrderItemListByCreateTimeBetween(Map<String, Object> paramMap){
		return ordOrderItemDao.queryOrderItemListByCreateTimeBetween(paramMap);
	}

	@Override
	public Long getTicketItemCountByUserIdAndCreateTime(String userId,
			Date createTime) {
		return ordOrderItemDao.getTicketItemCountByUserIdAndCreateTime(userId, createTime);
	}

	@Override
	public List<OrdOrderItem> getTicketItemListByUserId(String userId) {
		return ordOrderItemDao.getTicketItemListByUserId(userId);
	}
	
	@Override
	public List<OrdOrderItem> getPageTicketItemListByUserId(Map<String,Object> paramsMap) {
		return ordOrderItemDao.getPageTicketItemListByUserId(paramsMap);
	}
	@Override
	public Integer getTicketItemCountByUserId(String userId) {
		return ordOrderItemDao.getTicketItemCountByUserId(userId);
	}

	@Override
	public List<OrdOrderItem> selectByParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordOrderItemDao.selectByParams(params);
	}

	@Override
	public String updateCourierStatus(Long orderItemId, String courierStatus) {
		if (orderItemId == null) {
			return null;
		}
		
		String newStatus = COURIER_STATUS.Y.name();
		if (StringUtils.isNotBlank(courierStatus) && courierStatus.trim().equals(COURIER_STATUS.Y.name())) {
			newStatus = COURIER_STATUS.N.name();
		} else if (StringUtils.isBlank(courierStatus) || courierStatus.trim().equals(COURIER_STATUS.N.name())){
			newStatus = COURIER_STATUS.Y.name();
		} else {
			return null;
		}
		
		logger.info("updateCourierStatus, orderItemId:{}, oldStatus:{}, newStatus:{}", 
				new Object[]{orderItemId, courierStatus, newStatus});
		ordOrderItemDao.updateCourierStatus(orderItemId, newStatus);
		return newStatus;
	}

	@Override
	@ReadOnlyDataSource
	public List<OrdOrderItem> queryTicketOrderItem(Map<String, Object> params) {
		return ordOrderItemDao.queryTicketOrderItem(params);
	}

	@Override
	public List<OrdOrderItem> selectSubOrderItemsByIds(List<Long> ids) {
		if(ids.size() <= QUERY_LIST_COUNT) {
			return ordOrderItemDao.selectSubOrderItemsByIds(ids);
		}
		List<OrdOrderItem> list =new ArrayList<OrdOrderItem>();
		int fromIndex = 0;
		int toIndex = 0;
		while(fromIndex < ids.size()) {
			if(fromIndex + QUERY_LIST_COUNT >= ids.size()) {
				toIndex = ids.size();
			} else {
				toIndex = fromIndex + QUERY_LIST_COUNT;
			}
			list.addAll(ordOrderItemDao.selectSubOrderItemsByIds(ids.subList(fromIndex, toIndex)));
			
			fromIndex = toIndex;
		}
		return list;
	}

	@Override
	public List<OrdOrderItem> queryOrderItemListByParams(
			Map<String, Object> params) {
		
		return ordOrderItemDao.queryOrderItemListByParams(params);
	}

	@Override
	public int updateOrdOrderItem(OrdOrderItem ordOrderItem) {
		return ordOrderItemDao.updateByPrimaryKey(ordOrderItem);
	}
	
	 @Override
	 public List<VstOrdOrderItemDateVo> queryTicketOrderItemByVisitDate(Map<String,Object> params){

	        return ordOrderItemDao.queryTicketOrderItemByVisitDate(params);
	    }
	 
	 @Override
		public Integer queryOrderItemCountByVisitDate(Map<String,Object> params) {
			return ordOrderItemDao.getTotalCountForOrdOrderItem(params);
		}
	 
	 /**
	 * 根据orderId获取orderItem list
	 * @param orderId
	 */
	@Override
	public List<Long> getOrderItemIdListByOrderId(Long orderId) {
		return ordOrderItemDao.getOrderItemIdListByOrderId(orderId);
	}
	
	/**
	 * 根据orderItemId获取orderId
	 */
	@Override
	 public Long getOrderIdByOrderItemId(Long orderItemId){
		 return ordOrderItemDao.getOrderIdByOrderItemId(orderItemId);
	 }
	
	/**
	 * 根据OrderItemid获得通关码
	 */
	public OrdPassCode getOrdPassCodeByOrderItemId(final Long orderItemId){
		return ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItemId);
	}
	
	@Override
	public List<OrdOrderItem> queryTicketOrderItemByOrderId(
			Map<String, Object> params) {
		
		return ordOrderItemDao.queryTicketOrderItemByOrderId(params);
	}

	@Override
	public Integer getTotalCount(Map<String, Object> params) {
		return ordOrderItemDao.getTotalCount(params);
	}

	@Override
	public List<OrdETicketOrderItem> selectUserTicketByParams(
			Map<String, Object> params) {
		return ordOrderItemDao.selectUserTicketByParams(params);
	}

	@Override
	public List<OrdOrderItem> queryUserOrderItemList(Map<String, Object> params) {
		return ordOrderItemDao.queryUserOrderItemList(params);
	}
	
	@Override
	public int updatePriceConfirmStatusByOrderId(Long orderId,String priceConfirmStatus) {
		Map<String, Object> params =new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("priceConfirmStatus", priceConfirmStatus);
		int count= ordOrderItemDao.updatePriceConfirmStatusByOrderId(params);
		if(count>0){
			List<OrdOrderItem> list=ordOrderItemDao.selectByOrderId(orderId);
			for(OrdOrderItem item:list){
				Long orderItemId=item.getOrderItemId();
				ordOrderClientService.sendOrdItemPriceConfirmChangeMsg(orderItemId,orderItemId+"|");
			}
		}
		return count;
		
	}

	/**
	 * 查询所有符合条件的子订单集合
	 * 
	 * @param params Map<String, Object>
	 * @return List<OrdOrderItemView>
	 */
	@Override
	public List<OrdOrderItemView> selectListByParams(Map<String, Object> params) {
		return ordOrderItemDao.selectListByParams(params);
	}

	/**
	 * 查询子订单附加表ord_order_item_extend信息
	 * @param
	 * @return OrdOrderItemExtend
	 */
	public OrdOrderItemExtend selectOrdOrderItemExtendByOrderItemId (Long orderItemId){
		return ordOrderItemExtendDao.selectByPrimaryKey(orderItemId);
	}

	
}
