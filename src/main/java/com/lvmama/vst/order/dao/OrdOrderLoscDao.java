package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderLosc;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 订单审核数据库访问层
 * 
 * @author wenzhengtao
 *
 */
@Repository
public class OrdOrderLoscDao extends MyBatisDao {

	public OrdOrderLoscDao() {
		super("ORD_ORDER_LOSC");
	}
	 
	public int deleteByPrimaryKey(Long orderLoscId){
		return super.delete("deleteByPrimaryKey", orderLoscId);
	}
	
	public int insert(OrdOrderLosc record){
		return super.insert("insert", record);
	}
	
	public int insertSelective(OrdOrderLosc record){
		return super.insert("insertSelective", record);
	}
	
	public OrdOrderLosc selectByPrimaryKey(Long orderLoscId){
		return super.get("selectByPrimaryKey", orderLoscId);
	}
	
	public Integer updateByPrimaryKeySelective(OrdOrderLosc record){
		return super.update("updateByPrimaryKeySelective", record);
	}
	
	public int updateByPrimaryKey(OrdOrderLosc record){
		return super.update("updateByPrimaryKey", record);
	}
	
	/**
	 * 动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<OrdOrderLosc> queryAuditListByCondition(Map<String, Object> param){
		return super.queryForList("queryLoscListByCondition", param);
	}
}