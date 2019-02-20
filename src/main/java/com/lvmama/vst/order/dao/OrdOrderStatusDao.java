package com.lvmama.vst.order.dao;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderStatus;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderStatusDao extends MyBatisDao {

    public OrdOrderStatusDao(){
        super("ORD_ORDER_STATUS");
    }
    
    public int deleteByPrimaryKey(Long ordStatusId) {
        return super.delete("deleteByPrimaryKey", ordStatusId);
    }
    
/*    public int insert(OrdOrderStatus ordOrderStatus) {
        return super.insert("insert", ordOrderStatus);
    }*/

    public int insertSelective(OrdOrderStatus ordOrderStatus) {
        return super.insert("insertSelective", ordOrderStatus);
    }
    
/*    public OrdOrderStatus selectByPrimaryKey(Long orderItemId) {
        return super.get("selectByPrimaryKey", orderItemId);
    }*/
    
    public OrdOrderStatus selectByOrderId(Long orderId) {
    	List<OrdOrderStatus> statusList = super.getList("selectByOrderId", orderId);
    	if(CollectionUtils.isEmpty(statusList))
    		return null;
        return statusList.get(0);
    }
    
}
