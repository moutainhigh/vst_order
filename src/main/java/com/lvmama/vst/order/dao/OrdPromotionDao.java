package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdPromotionDao extends MyBatisDao{
	
    public OrdPromotionDao() {
		super("ORD_PROMOTION");
		// TODO Auto-generated constructor stub
	}

	public int deleteByPrimaryKey(Long ordPromotionId){
    	return super.delete("deleteByPrimaryKey", ordPromotionId);
    }

    public int insert(OrdPromotion record){
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdPromotion record){
    	return super.insert("insertSelective", record);
    }

    public OrdPromotion selectByPrimaryKey(Long ordPromotionId){
    	return super.get("selectByPrimaryKey", ordPromotionId);
    }
    /**
     * 查询主订单的促销信息
     * @param orderItemIdList
     * @return
     */
    public List<OrdPromotion> selectOrdPromotionsByOrderItemId(Map<String,Object> params){
    	return super.queryForList("selectOrdPromotionsByOrderItemId", params);
    }

    public int updateByPrimaryKeySelective(OrdPromotion record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdPromotion record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    public List<OrdPromotion> selectOrdPromotionsByOrderId(Long orderId){
    	return super.queryForList("selectOrdPromotionsByOrderId", orderId);
    }
    
}