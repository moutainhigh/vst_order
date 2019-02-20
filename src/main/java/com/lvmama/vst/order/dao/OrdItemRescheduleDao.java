package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdItemReschedule;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrdItemRescheduleDao extends MyBatisDao {

	public OrdItemRescheduleDao() {
		super("ORD_ITEM_RESCHEDULE");
	}

    public int deleteByPrimaryKey(Long ordItemRescheduleId) {
		return super.delete("deleteByPrimaryKey", ordItemRescheduleId);
	}

    public int insert(OrdItemReschedule ordItemReschedule) {
		return super.insert("insert", ordItemReschedule);
	}

    public int insertSelective(OrdItemReschedule ordItemReschedule) {
		return super.insert("insertSelective", ordItemReschedule);
	}

	public OrdItemReschedule selectByPrimaryKey(Long ordItemRescheduleId) {
		return super.get("selectByPrimaryKey", ordItemRescheduleId);
	}

	public int updateByPrimaryKeySelective(OrdItemReschedule ordItemReschedule) {
		return super.update("updateByPrimaryKeySelective", ordItemReschedule);
	}

    public int updateByPrimaryKey(OrdItemReschedule ordItemReschedule) {
		return super.update("updateByPrimaryKey", ordItemReschedule);
	}
	public OrdItemReschedule selectByOrderItemId(Long orderItemId){
        return super.get("selectByOrderItemId",orderItemId);
    }

    public int updateExchangeCountByOrdItemId(Long orderItemId) {
        return super.update("updateExchangeCountByOrdItemId",orderItemId);
    }
}