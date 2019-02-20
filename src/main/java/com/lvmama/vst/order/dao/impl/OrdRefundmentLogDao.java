package com.lvmama.vst.order.dao.impl;

import org.springframework.stereotype.Repository;

import com.lvmama.comm.bee.po.ord.OrdRefundmentLog;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdRefundmentLogDao  extends MyBatisDao{

    public OrdRefundmentLogDao() {
		super("ORD_REFUNDMENT_LOG");
	}
	
	
    public int insertSelective(OrdRefundmentLog ordRefundmentLog){
    	 return super.insert("insertSelective", ordRefundmentLog);
    }
    
    
}
