package com.lvmama.vst.order.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.vo.order.OrderWechatAppVo;



@Repository
public class OrdWechatAppDao extends MyBatisDao {
	public OrdWechatAppDao() {
		super("ORD_WECHAT_APP");
	}

	public List<OrderWechatAppVo> search(Long orderId) {
		return super.queryForList("search", orderId);
	}

	public void insert(OrderWechatAppVo orderWechatApp) {
		super.insert("insert", orderWechatApp);
	}
}
