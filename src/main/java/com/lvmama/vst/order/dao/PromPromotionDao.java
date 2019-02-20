package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class PromPromotionDao extends MyBatisDao{
	
    public PromPromotionDao() {
		super("PROM_PROMOTION");
	}
	
    public int addPromAmount(Map<String, Object> params){
    	return super.update("addPromAmount", params);
    }
    
    public int subtractPromAmount(Map<String, Object> params){
    	return super.update("subtractPromAmount", params);
    }
    
	
	
}