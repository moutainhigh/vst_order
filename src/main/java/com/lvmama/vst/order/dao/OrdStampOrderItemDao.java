/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.Date;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdStampOrder;
import com.lvmama.vst.back.order.po.OrdStampOrderItem;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * @author chenlizhao
 *
 */
@Repository
public class OrdStampOrderItemDao extends MyBatisDao {

	public OrdStampOrderItemDao() {
		super("ORD_STAMP_ORDER_ITEM");
	}
	
	public int insertSelective(OrdStampOrderItem item) {
		return super.insert("insertSelective", item);
	}
	
	public int updateByPrimaryKeySelective(OrdStampOrderItem item) {
		item.setUpdateTime(new Date());
		return super.update("updateByPrimaryKeySelective", item);
	}
	
	public OrdStampOrderItem selectByPrimaryKey(Long orderItemId) {
		return super.get("selectByPrimaryKey", orderItemId);
	}
	
	public OrdStampOrderItem selectByOrderId(Long orderId) {
		return super.get("selectByOrderId", orderId);
	}
	
	public Long countByStampDefinitionId(String stampDefinitionId) {
		return super.get("countByStampDefinitionId", stampDefinitionId);
	}
}
