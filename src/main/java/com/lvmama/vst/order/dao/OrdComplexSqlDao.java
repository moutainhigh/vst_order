package com.lvmama.vst.order.dao;

import java.util.List;

import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdComplexSqlDao extends MyBatisDao {

	public OrdComplexSqlDao() {
		super("ORD_COMPLEX_SQL");
	}

	public List<OrdAdditionStatus> selectDistinctAdditionStatusByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctAdditionStatusByOrderIds",
				orderIds);
	}

	public List<OrdAddress> selectDistinctAddressByorderIds(List<Long> orderIds) {
		return super.queryForList("selectDistinctAddressByorderIds", orderIds);
	}

	public List<OrdCourierListing> selectDistinctCourierListingByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctCourierListingByOrderIds",
				orderIds);
	}

	public List<OrdFormInfo> selectDistinctFormInfoByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctFormInfoByOrderIds", orderIds);
	}

	public List<OrdGuaranteeCreditCard> selectDistinctCreditCardByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctCreditCardByOrderIds",
				orderIds);
	}

	public List<OrdItemPersonRelation> selectPersonRelationByorderIds(
			List<Long> orderIds) {
		return super.queryForList("selectPersonRelationByorderIds", orderIds);
	}

	public List<OrdOrderAmountItem> selectDistinctAmountItemByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctAmountItemByOrderIds",
				orderIds);
	}

	public List<OrdOrderHotelTimeRate> selectDistinctHotelTimeRatesByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctHotelTimeRatesByOrderIds",
				orderIds);
	}
	
	public List<OrdOrderHotelTimeRate> selectDistinctHotelTimeRatesSortByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctHotelTimeRatesSortByOrderIds",
				orderIds);
	}
	
	public List<OrdOrderWifiTimeRate> selectDistinctWifiTimeRatesByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctWifiTimeRatesByOrderIds",
				orderIds);
	}
	
	public List<OrdOrderWifiTimeRate> selectDistinctWifiTimeRatesSortByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctWifiTimeRatesSortByOrderIds",
				orderIds);
	}
	
	

	public List<OrdOrderItem> selectDistinctOrderItemsByorderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctOrderItemsByorderIds",
				orderIds);
	}

	public List<OrdOrderPack> selectDistinctOrderPacksByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctOrderPacksByOrderIds",
				orderIds);
	}

	public List<OrdOrderStock> selectDistinctStocksByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctStocksByOrderIds", orderIds);
	}
	
	public List<OrdOrderStock> selectDistinctStocksByOrderItemId(Long orderItemId) {
		return super.queryForList("selectDistinctStocksByOrderItemId", orderItemId);
	}

	public List<OrdPerson> selectDistinctOrderPersonsByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctOrderPersonsByOrderIds",
				orderIds);
	}

	public List<OrdTravelContract> selectDistinctTravelContractByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctTravelContractByOrderIds",
				orderIds);
	}
	
	public List<OrdOrderWifiPickingPoint> selectDistinctPickingPointByOrderIds(
			List<Long> orderIds) {
		return super.queryForList("selectDistinctPickingPointByOrderIds",
				orderIds);
	}

	/**
	 * 查询交通接驳的服务信息
	 * @param orderIds
	 * @return
     */
	public List<OrderConnectsServiceProp> selectConnectsServicePropByOrderIds(List<Long> orderIds){
		return super.queryForList("selectConnectsServicePropByOrderIds", orderIds);
	}

}