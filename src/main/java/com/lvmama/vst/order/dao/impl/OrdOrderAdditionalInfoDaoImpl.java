package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrdOrderAdditionalInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrdOrderAdditionalInfoDao;
import org.springframework.stereotype.Repository;

/**
 * Created by zhouyanqun on 2017/2/6.
 */
@Repository
public class OrdOrderAdditionalInfoDaoImpl extends MyBatisDao implements OrdOrderAdditionalInfoDao {
    public OrdOrderAdditionalInfoDaoImpl() {
        super("ORD_ORDER_ADDITIONAL_INFO");
    }

    @Override
    public int insert(OrdOrderAdditionalInfo ordOrderAdditionalInfo) {
        return super.insert("insertSelective", ordOrderAdditionalInfo);
    }
}
