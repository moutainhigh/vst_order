package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.lvmama.vst.back.order.po.OrdOrderNotice;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdNoticeDao extends MyBatisDao {

	
	public OrdNoticeDao() {
		super("ORD_ORDER_NOTICE");
	}
	
	public int insert(OrdOrderNotice record) {
		return super.insert("insert", record);
	}
	
	public List<OrdOrderNotice> findOrdNoticeList(Map<String, Object> params){
		return super.queryForList("selectOrdNoticeList", params);
	}
	  
	public List<OrdOrderNotice> findOrdNoticeList_notice(Map<String, Object> params){
		return super.queryForList("selectOrdNoticeList_notice", params);
	}
}
