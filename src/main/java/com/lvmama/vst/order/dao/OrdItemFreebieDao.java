package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.lvmama.vst.back.order.po.OrdItemFreebiesRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 酒店订单赠品快照
 * 
 *
 */
@Repository
public class OrdItemFreebieDao extends MyBatisDao{

	
	public OrdItemFreebieDao(){
		super("ORD_ITEM_FREEBIE_RELATION");
	}
	
	public int insert(OrdItemFreebiesRelation freebieRelation) {
		return super.insert("insertFreebieRelation", freebieRelation);
	}
	
	public int batchInsert(List<OrdItemFreebiesRelation> freebieList){
		return super.insert("batchInsert", freebieList);
	}
	
	public int batchUpdate(List<OrdItemFreebiesRelation> freebieList){
		getSqlSession();
		return super.update("batchUpdate", freebieList);
	}
	
	public int updateItemFreebie(OrdItemFreebiesRelation freebieRelation){
		return super.update("updateItemFreebie", freebieRelation);
	}
	
	public List<OrdItemFreebiesRelation> queryFreebieListByItem(Map<String, Object> params){
		return super.queryForList("queryHotelFreebieByParams", params);
	}
	
}
