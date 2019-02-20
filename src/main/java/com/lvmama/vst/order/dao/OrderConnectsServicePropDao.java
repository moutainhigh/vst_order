package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderConnectsServicePropDao  extends MyBatisDao{
	public OrderConnectsServicePropDao() {
		super("ORDER_CONNECTS_SERVICE_PROP");
	}
	
	/**
	 * 查询列表
	 * @param params
	 * @return list
	 */
	public List<OrderConnectsServiceProp> findOrderConnectsServicePropList(Map<String, Object> params) {
		List<OrderConnectsServiceProp> list = super.queryForList("selectByParams", params);
		return list;
	}
	
	/**
	 * 查询
	 * @param orderServiceId
	 * @return OrderConnectsServiceProp
	 */
	public OrderConnectsServiceProp selectByPrimaryKey(Long orderServiceId) {
		return super.get("selectByPrimaryKey", orderServiceId);
	}
	
	
	
	/**
	 * 插入
	 * @param orderConnectsServiceProp
	 * @return Integer
	 */
	public Integer insert(OrderConnectsServiceProp orderConnectsServiceProp) {
		return super.insert("insert", orderConnectsServiceProp);
	}
	
	
	/**
	 * 插入
	 * @param orderConnectsServiceProp
	 * @return Integer
	 */
	
	public Integer insertSelective(OrderConnectsServiceProp orderConnectsServiceProp){
		return super.insert("insertSelective", orderConnectsServiceProp);
	}
	
	/**
	 * 删除
	 * @param params
	 * @return Integer
	 */
	public Integer deleteByParams(Map<String, Object> params) {
        return super.delete("deleteByParams", params);
    }
	
	/**
	 * 删除
	 * @param orderServiceId
	 * @return Integer
	 */
	public Integer deleteByPrimaryKey(Long orderServiceId) {
        return super.delete("deleteByPrimaryKey", orderServiceId);
    }
	
	/**
	 * 更新
	 * @param orderConnectsServiceProp
	 * @return Integer
	 */
    public Integer updateBySelective(OrderConnectsServiceProp orderConnectsServiceProp) {
        return super.update("updateByPrimaryKeySelective", orderConnectsServiceProp);
    }
	
    /**
	 * 更新
	 * @param orderConnectsServiceProp
	 * @return Integer
	 */
    public Integer update(OrderConnectsServiceProp orderConnectsServiceProp) {
		return super.update("update", orderConnectsServiceProp);
	}

	/**
	 * 关联查询属性字典表
	 * @param params
	 * @return
     */
	public List<OrderConnectsServiceProp> queryOrderConnectsPropByParams(Map<String,Object> params){
		return super.queryForList("queryOrderConnectsPropByParams",params);
	}
	
	public Integer updateOrderConnectsServicePropByOrderId(OrderConnectsServiceProp orderConnectsServiceProp){
		return super.update("updateOrderConnectsServicePropByOrderId", orderConnectsServiceProp);
	}

}
