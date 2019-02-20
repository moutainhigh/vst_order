package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdOrderSharedStock;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by luoweiyi on 2015/4/8.
 */
@Repository
public class OrdOrderSharedStockDao extends MyBatisDao {

    public OrdOrderSharedStockDao() {
        super("ORD_ORDER_SHARED_STOCK");
    }

    public int deleteByPrimaryKey(Long orderSharedStockId) {
        return super.delete("deleteByPrimaryKey", orderSharedStockId);
    }

    public int insert(OrdOrderSharedStock ordOrderSharedStock) {
        return super.insert("insert", ordOrderSharedStock);
    }

    public OrdOrderSharedStock selectByPrimaryKey(Long orderSharedStockId) {
        return super.get("selectByPrimaryKey", orderSharedStockId);
    }

    public List<OrdOrderSharedStock> selectByParams(Map<String,Object> params) {
        return super.queryForList("selectByParams", params);
    }

}
