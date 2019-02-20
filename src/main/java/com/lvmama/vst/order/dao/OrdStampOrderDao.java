/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdStampOrder;
import com.lvmama.vst.back.order.vo.StampOrderVo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * @author chenlizhao
 *
 */
@Repository
public class OrdStampOrderDao extends MyBatisDao {

	public OrdStampOrderDao() {
		super("ORD_STAMP_ORDER");
	}
	
	public int insertSelective(OrdStampOrder order) {
		return super.insert("insertSelective", order);
	}

	public int updateByPrimaryKeySelective(OrdStampOrder order) {
		order.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		return super.update("updateByPrimaryKeySelective", order);
	}
	
	public OrdStampOrder selectByPrimaryKey(Long orderId) {
		return super.get("selectByPrimaryKey", orderId);
	}
	
	public Long countStampOrder(Map<String, Object> paramMap) {
		return super.get("countStampOrder", paramMap);
	}
	
	public List<StampOrderVo> queryStampOrder(Map<String, Object> paramMap) {
		return super.queryForList("queryStampOrder", paramMap);
	}
}
