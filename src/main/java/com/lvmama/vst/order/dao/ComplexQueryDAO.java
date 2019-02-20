package com.lvmama.vst.order.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.impl.SQLStatementAdapter;

/**
 * 综合查询数据库访问层
 * 
 * @author wenzhengtao
 * 
 */

@Repository("complexQueryDAO")
public final class ComplexQueryDAO extends MyBatisDao implements IComplexQuerySQLConstant {
	/**
	 * 日志记录器
	 */
	private static final Log LOG = LogFactory.getLog(ComplexQueryDAO.class);

	/**
	 * 构造器
	 */
	public ComplexQueryDAO() {
		super("COMPLEX_QUERY");
	}

	/**
	 * 由构造器生成完整动态SQL查询
	 * 
	 * @param clazz
	 * @param completeSQL
	 * @return
	 */
	public <T> List<T> queryList(final Class<T> clazz, final String sqlStatement) {
		final String sqlMap = this.getListSqlMapByPO(clazz);
		List<T> list;
		if (UtilityTool.isValid(sqlMap)) {
			list = super.getSqlSession().selectList(sqlMap, new SQLStatementAdapter(sqlStatement));
		} else {
			list = new ArrayList<T>();
		}
		return list;
	}

	/**
	 * 由构造器生成完整动态SQL统计
	 * 
	 * @param clazz
	 * @param completeSQL
	 * @return
	 */
	public <T> Long queryCount(final Class<T> clazz, final String sqlStatement) {
		final String sqlMap = this.getCountSqlMapByPO(clazz);
		Long count;
		if (UtilityTool.isValid(sqlMap)) {
			count = (Long) super.getSqlSession().selectOne(sqlMap, new SQLStatementAdapter(sqlStatement));
		} else {
			count = 0L;
		}
		return count;
	}

	/**
	 * 根据查询出来的订单关联查询相关信息
	 * 
	 * @param clazz
	 * @param queryOrderSQL
	 * @return
	 */
	public <T> List<T> queryOrderRelatedInfo(final Class<T> clazz, final String queryOrderSql,final ComplexQuerySQLCondition condition) {
		final String sqlStatement = this.getRalatedCompleteSQL(clazz, queryOrderSql,condition);
		List<T> list;
		if (UtilityTool.isValid(sqlStatement)) {
			list = this.queryList(clazz, sqlStatement);
		} else {
			list = new ArrayList<T>();
		}
		return list;
	}

	/**
	 * 根据po获得相应结果集的sqlmap
	 * 
	 * @param clazz
	 * @return
	 */
	private <T> String getListSqlMapByPO(final Class<T> clazz) {
		String sqlMap = null;
		// 订单表
		if (clazz.getSimpleName().equals(OrdOrder.class.getSimpleName())) {
			sqlMap = "queryOrderList";
			// 订单子项表
		} else if (clazz.getSimpleName().equals(OrdOrderItem.class.getSimpleName())) {
			sqlMap = "queryOrderItemListByOrderId";
			// 订单人员表
		} else if (clazz.getSimpleName().equals(OrdPerson.class.getSimpleName())) {
			sqlMap = "queryOrderPersonListByOrderId";
		//订单快递地址表
		}else if (clazz.getSimpleName().equals(OrdAddress.class.getSimpleName())) {
			sqlMap = "queryOrderAddressListByPersonId";
		//订单信用卡担保表
		}else if(clazz.getSimpleName().equals(OrdGuaranteeCreditCard.class.getSimpleName())){
			sqlMap = "queryOrderGuaranteeCreditCardListByOrderId";
		//订单打包表
		}else if(clazz.getSimpleName().equals(OrdOrderPack.class.getSimpleName())){
			sqlMap = "queryOrderPackListByOrderId";
		//订单金额转换表
		}else if(clazz.getSimpleName().equals(OrdOrderAmountItem.class.getSimpleName())){
			sqlMap = "queryOrderAmountItemListByOrderId";
		//订单库存情况表
		}else if(clazz.getSimpleName().equals(OrdOrderStock.class.getSimpleName())){
			sqlMap = "queryOrderStockListByOrderItemId";//由订单子项转换
		//订单酒店每天使用情况表
		}else if(clazz.getSimpleName().equals(OrdOrderHotelTimeRate.class.getSimpleName())){
			sqlMap = "queryOrderHotelTimeRateListByOrderItemId";//由订单子项转换
		//状态表	
		}else if (clazz.getSimpleName().equals(OrdAdditionStatus.class.getSimpleName())) {
			sqlMap = "queryOrdAdditionStatusListByOrderId";
		//订单合同表	
		}else if (clazz.getSimpleName().equals(OrdTravelContract.class.getSimpleName())) {
			sqlMap = "queryOrdTravelContractListByOrderId";
		}else if(clazz.getSimpleName().equals(OrdItemPersonRelation.class.getSimpleName())){//子项人员关联
			sqlMap = "queryOrdItemPersonRelationListByOrderId";
		}else if(clazz.getSimpleName().equals(OrdCourierListing.class.getSimpleName())){//快递寄件清单
			sqlMap = "queryOrdCourierListingListByOrderId";
		}else if(clazz.getSimpleName().equals(OrdFormInfo.class.getSimpleName())){
			sqlMap = "queryOrdFormInfoListByOrderId";
		}		
		
		return sqlMap;
	}

	/**
	 * 根据po获得相应总数的sqlmap
	 * 
	 * @param clazz
	 * @return
	 */
	private <T> String getCountSqlMapByPO(final Class<T> clazz) {
		String sqlMap = null;
		// 订单表
		if (clazz.getSimpleName().equals(OrdOrder.class.getSimpleName())) {
			sqlMap = "queryOrderCount";
		}
		return sqlMap;
	}

	/**
	 * 构造关联查询SQL
	 * 
	 * @param clazz
	 * @param orderIdInsql
	 * @return
	 */
	private <T> String getRalatedCompleteSQL(final Class<T> clazz, final String queryOrderSQL,final ComplexQuerySQLCondition condition) {
		String completeSQL = null;
		// 订单子项SQL
		if (clazz.getSimpleName().equals(OrdOrderItem.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_ORDER_ITEM_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		// 订单人员SQL
		} else if (clazz.getSimpleName().equals(OrdPerson.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_PERSON_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		//订单快递地址SQL
		}else if (clazz.getSimpleName().equals(OrdAddress.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_ADDRESS_SQL).append(queryOrderSQL).append(C_END_SQL_1).toString();
		//订单打包SQL
		}else if(clazz.getSimpleName().equals(OrdOrderPack.class.getSimpleName())){
			completeSQL = new StringBuilder().append(C_ORD_ORDER_PACK_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		//订单金额SQL
		}else if(clazz.getSimpleName().equals(OrdOrderAmountItem.class.getSimpleName())){
			completeSQL = new StringBuilder().append(C_ORD_ORDER_AMOUNT_ITEM_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		//订单担保SQL
		}else if(clazz.getSimpleName().equals(OrdGuaranteeCreditCard.class.getSimpleName())){
			completeSQL = new StringBuilder().append(C_ORD_GUARANTEE_CREDIT_CARD_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		/*****************************以下表需要使用订单子项关联******************************************/
		//订单酒店使用情况SQL
		}else if(clazz.getSimpleName().equals(OrdOrderHotelTimeRate.class.getSimpleName())){
			if(UtilityTool.isValid(condition.getOrderRelationSortParam().getOrderHotelTimeRateSort())){
				//带排序
				completeSQL = new StringBuilder().append(C_ORD_ORDER_HOTEL_TIME_RATE_SQL).append(queryOrderSQL).append(C_END_SQL_1).append(K_ORDER_BY).append(condition.getOrderRelationSortParam().getOrderHotelTimeRateSort()).toString();
			}else{
				completeSQL = new StringBuilder().append(C_ORD_ORDER_HOTEL_TIME_RATE_SQL).append(queryOrderSQL).append(C_END_SQL_1).toString();
			}
			
		//订单库存SQL
		}else if(clazz.getSimpleName().equals(OrdOrderStock.class.getSimpleName())){
			completeSQL = new StringBuilder().append(C_ORD_ORDER_STOCK_SQL).append(queryOrderSQL).append(C_END_SQL_1).toString();
		//订单相关状态	
		}else if (clazz.getSimpleName().equals(OrdAdditionStatus.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_ADDITION_STATUS_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		//订单合同	
		}else if (clazz.getSimpleName().equals(OrdTravelContract.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_TRAVEL_CONTRACT_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
			
		}else if (clazz.getSimpleName().equals(OrdItemPersonRelation.class.getSimpleName())) {
			completeSQL = new StringBuilder().append(C_ORD_ORDER_ITEM_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		}else if(clazz.getSimpleName().equals(OrdCourierListing.class.getSimpleName())){//快递寄件清单
			completeSQL = new StringBuilder().append(C_ORD_COURIER_LISTING_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		}else if(clazz.getSimpleName().equals(OrdFormInfo.class.getSimpleName())){
			completeSQL = new StringBuffer().append(C_ORD_FORM_INFO_SQL).append(queryOrderSQL).append(C_END_SQL).toString();
		}
		// 打印关联信息查询的SQL
		if (LOG.isDebugEnabled()) {
			LOG.debug("ORDER RALATION SQL:"+completeSQL);
		}
		return completeSQL;
	}
	
	public List<Map<String, Object>> selectListBySql(Map<String, Object> params) {
		return super.queryForList("selectListBySql", params);
	}
	public List<Map<String, Object>> selectOrdOrderByOrderIds(Map<String, Object> params) {
		return super.queryForList("selectOrdOrderByOrderIds", params);
	}
}
