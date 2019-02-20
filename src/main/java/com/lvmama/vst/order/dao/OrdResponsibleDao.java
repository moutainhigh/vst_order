package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdResponsibleDao extends MyBatisDao{
	
    public OrdResponsibleDao() {
		super("ORD_RESPONSIBLE");
		// TODO Auto-generated constructor stub
	}
    
    public List<OrdResponsible> selectWaitObjectList(final Map<String,Object> params){
    	return super.queryForList("selectWaitObjectList",params);
    }
    
    public OrdResponsible getOrderItemResponsibleByOrder(final Long objectId){
    	return null;
    }
    
    public OrdResponsible getOrderResponsibleByOrderItem(final Long objectId){
    	return null;
    }
    
    public OrdResponsible getResponsibleByObject(final Long objectId,final String objectType){
    	Map<String,Object> params = new HashMap<String, Object>();
    	params.put("objectId", objectId);
    	params.put("objectType", objectType);
    	return super.get("getResponsibleByObject",params);
    }
    
    public int insert(OrdResponsible record){
    	return super.insert("insert", record);
    }
    
    public int updateByPrimaryKey(OrdResponsible record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    /**
	 * 根据条件统计总数量
	 * 
	 * @param param
	 * @return
	 */
	public Integer selectResponsibleCount(Map<String, Object> param){
		return super.get("selectResponsibleCount", param);
	}
	
	/**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<Map> selectResponsibleList(Map<String, Object> param){
		return super.queryForList("selectResponsibleList", param);
	}
	
	/**
	 * map动态查询
	 * @param params
	 * @return
	 */
	public List<OrdResponsible> selectByParams(final Map<String,Object> params){
    	return super.queryForList("selectByParams",params);
    }
	
	/**
	 * map动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param){
		return super.get("getTotalCount", param);
	}

	/*int deleteByPrimaryKey(Long responsibleId);

    


    OrdResponsible selectByPrimaryKey(Long responsibleId);

    int updateByPrimaryKeySelective(OrdResponsible record);

    */
}