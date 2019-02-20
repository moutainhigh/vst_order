package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrdRefundItem;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrdRefundItemDao;
import org.springframework.stereotype.Repository;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
@Repository
public class OrdRefundItemDaoImpl extends MyBatisDao implements OrdRefundItemDao {
    public OrdRefundItemDaoImpl() {
        super("ORD_REFUND_ITEM");
    }

    /**
     * 插入新记录
     *
     * @param ordRefundItem
     */
    @Override
    public int insert(OrdRefundItem ordRefundItem) {
        return super.insert("insert", ordRefundItem);
    }

	@Override
	public int insertSelective(OrdRefundItem ordRefundItem) {
		 return super.insert("insertSelective", ordRefundItem);
	}
}
