package com.lvmama.vst.order.dao;

import org.springframework.stereotype.Repository;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;
@Repository
public class OrdItemShowTicketInfoDAO extends MyBatisDao{
	public OrdItemShowTicketInfoDAO(){
		super("ORD_ITEM_SHOW_TICKET_INFO");
	}
	
	public int insert(OrdItemShowTicketInfoVO showTicketInfo){
		return super.insert("insert", showTicketInfo);
	}
	
	public OrdItemShowTicketInfoVO queryByOrdItemId(Long orderItemId) {
		return super.get("queryByOrdItemId", orderItemId);
	}
}
