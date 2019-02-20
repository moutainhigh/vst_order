package com.lvmama.vst.order.dao;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
@Repository
public class OrdPayPromotionDao extends  MyBatisDao{
	
	
	public OrdPayPromotionDao(){
	   super("ORD_PAY_PROMOTION");
	}
	public int insert(OrdPayPromotion ordPayPromotion) {
		return super.insert("insert", ordPayPromotion);
		
	}
	
	public OrdPayPromotion queryOrdPayPromotionById(Long id) {
		OrdPayPromotion ordPayPromotion = super.get("selectByPrimaryKey",id);
		return ordPayPromotion;
	}
	
	public OrdPayPromotion queryOrdPayPromotionByOrderId(Long orderId) {
		OrdPayPromotion ordPayPromotion = super.get("selectByOrderId",orderId);
		return ordPayPromotion;
	}

	

			
	
    
    
    

}
