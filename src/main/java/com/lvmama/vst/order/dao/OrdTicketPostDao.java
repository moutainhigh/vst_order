package com.lvmama.vst.order.dao;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTicketPost;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdTicketPostDao extends MyBatisDao {

	public OrdTicketPostDao() {
		super("ORD_TICKET_POST");
	}

	public int insert(OrdTicketPost ordTicketPost) {
		return super.insert("insert", ordTicketPost);
	}
	
}