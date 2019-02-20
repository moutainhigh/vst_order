package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdETicketOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItemView;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.VstOrdOrderItemDateVo;

public class IOrdOrderItemServiceMocker implements IOrdOrderItemService {
	
	private OrdOrderItem suborder;

	@Override
	public Integer updateByPrimaryKeySelective(OrdOrderItem ordOrderItem) {
		return null;
	}

	@Override
	public List<OrdOrderItem> queryOrderItemListByCreateTimeBetween(Map<String, Object> paramMap) {
		return null;
	}

	@Override
	public List<OrdOrderItem> selectOrderItemsByIds(List<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> selectByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateOrdOrderItem(OrdOrderItem ordOrderItem) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer countOrderItemByCreateTimeAndProductId(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> selectOrderItemsByorderIds(List<Long> orders) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer countOrderMainItemForO2oTicketByProductId(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrdOrderItem selectOrderItemByOrderItemId(Long orderItemId) {
		return suborder;
	}

	@Override
	public Long getTicketItemCountByUserIdAndCreateTime(String userId, Date createTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> getTicketItemListByUserId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> selectByParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateCourierStatus(Long orderItemId, String courierStatus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> selectSubOrderItemsByIds(List<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> queryTicketOrderItem(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> queryOrderItemListByParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> getPageTicketItemListByUserId(Map<String, Object> paramsMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getTicketItemCountByUserId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VstOrdOrderItemDateVo> queryTicketOrderItemByVisitDate(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getOrderItemIdListByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getOrderIdByOrderItemId(Long orderItemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrdPassCode getOrdPassCodeByOrderItemId(Long orderItemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> queryTicketOrderItemByOrderId(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer queryOrderItemCountByVisitDate(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getTotalCount(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdETicketOrderItem> selectUserTicketByParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdOrderItem> queryUserOrderItemList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public OrdOrderItem getSuborder() {
		return suborder;
	}

	public void setSuborder(OrdOrderItem suborder) {
		this.suborder = suborder;
	}

	@Override
	public List<OrdOrderItemView> selectListByParams(Map<String, Object> params) {
		return null;
	}

	@Override
	public int updatePriceConfirmStatusByOrderId(Long orderId, String priceConfirmStatus) {
		return 0;
	}

}
