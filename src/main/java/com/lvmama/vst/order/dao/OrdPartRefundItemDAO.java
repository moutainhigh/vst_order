package com.lvmama.vst.order.dao;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdPartRefundItem;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 
 * @author liuxiaoyong
 *
 */
@Repository
public class OrdPartRefundItemDAO extends MyBatisDao{

	public OrdPartRefundItemDAO() {
		super("ORD_PART_REFUND_ITEM");
	}
	public OrdPartRefundItem getOrdPartRefundItemByOrderItemId(final Long orderItemId){
		return super.get("getOrdPartRefundItemByOrderItemId",orderItemId);
	}
	
	public int updateOrdPartRefundItem(OrdPartRefundItem ordPartRefundItem){
		return super.update("updateOrdPartRefundItemByOrderItemId", ordPartRefundItem);
	}
	
	public int insertSelective(OrdPartRefundItem ordPartRefundItem){
		return super.insert("insertSelective", ordPartRefundItem);
	}
}
