package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdOrderItemExtend;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class OrdOrderItemExtendDao extends MyBatisDao {

	public OrdOrderItemExtendDao() {
		super("ORD_ORDER_ITEM_EXTEND");
	}

	public int deleteByPrimaryKey(Long orderItemId) {
		return super.delete("deleteByPrimaryKey", orderItemId);
	}

	public int insert(OrdOrderItemExtend ordOrderItemExtend) {
		return super.insert("insert", ordOrderItemExtend);
	}

	public OrdOrderItemExtend selectByPrimaryKey(Long orderItemId) {
		return super.get("selectByPrimaryKey", orderItemId);
	}

	public int updateByPrimaryKeySelective(OrdOrderItemExtend ordOrderItemExtend) {
		ordOrderItemExtend.setUpdateTime(new Date());
		return super.update("updateByPrimaryKeySelective", ordOrderItemExtend);
	}

}