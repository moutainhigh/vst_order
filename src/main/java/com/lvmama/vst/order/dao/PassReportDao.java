package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.passport.po.PassReport;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class PassReportDao extends MyBatisDao{
	
    public PassReportDao() {
		super("PASS_REPORT");
	}
	
    
    public List<PassReport> findList(Map<String, Object> params){
    	return super.queryForList("selectListByParams", params);
    }
    
	
}