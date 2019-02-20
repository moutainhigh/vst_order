package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * 
 * @author sunjian
 *
 */
@Repository
public class OrdAdditionStatusDAO extends MyBatisDao {
	
	@Autowired
	private OrdOrderQueryInfoDao orderQueryInfoDao;

	public OrdAdditionStatusDAO() {
		super("ORD_ADDITION_STATUS");
	}
	
	public List<OrdAdditionStatus> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	

    public int deleteByPrimaryKey(Long ordAdditionStatusId) {
    	return super.delete("deleteByPrimaryKey", ordAdditionStatusId);
    }

    public int insert(OrdAdditionStatus record) {
    	//同步更新订单查询信息
    	syncOrderQueryInfo(record);
    			
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdAdditionStatus record) {
    	//同步更新订单查询信息
    	syncOrderQueryInfo(record);
    	
    	return super.insert("insertSelective", record);
    }

    public OrdAdditionStatus selectByPrimaryKey(Long ordAdditionStatusId) {
    	return super.get("selectByPrimaryKey", ordAdditionStatusId);
    }
    
    public OrdAdditionStatus selectByOrderIdKey(Long orderId){
    	return super.get("selectByOrderIdKey",orderId);
    }

    public int updateByPrimaryKeySelective(OrdAdditionStatus record) {
    	//同步更新订单查询信息
    	syncOrderQueryInfo(record);
    	
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdAdditionStatus record) {
    	//同步更新订单查询信息
    	syncOrderQueryInfo(record);
    	
    	return super.update("updateByPrimaryKey", record);
    }
    
    /**
     * 同步更新订单查询信息
     * @param record
     */
    private void syncOrderQueryInfo(OrdAdditionStatus record) {
		if (StringUtils.isNotBlank(record.getStatus())) {
			OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();
			if(record.getOrderId()==null){
				Long statusId=record.getOrdAdditionStatusId();
				Map<String,Object> conditionMap=new HashMap<String,Object>();
				conditionMap.put("ordAdditionStatusId", statusId);
				List<OrdAdditionStatus> statusList=this.selectByParams(conditionMap);
				if(!statusList.isEmpty()){
					orderQueryInfo.setOrderId(statusList.get(0).getOrderId());
				}
			}else{
				orderQueryInfo.setOrderId(record.getOrderId());
			}
			orderQueryInfo.setNoticeRegimentStatus(record.getStatus());
//			orderQueryInfoDao.updateByOrderId(orderQueryInfo);
		}
    }

}
