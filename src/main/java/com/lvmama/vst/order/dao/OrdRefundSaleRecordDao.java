package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lijuntao on 2017/1/16.
 */
@Repository
public class OrdRefundSaleRecordDao extends MyBatisDao {
    public OrdRefundSaleRecordDao(){
        super("ORD_REFUND_SALE_RECORD");
    }

    public int insert(OrdRefundSaleRecord ordRefundSaleRecord) {
        return super.insert("insert", ordRefundSaleRecord);
    }

    public OrdRefundSaleRecord selectByPrimaryKey(Long ordRefundSaleRecordId) {
        return super.get("selectByPrimaryKey", ordRefundSaleRecordId);
    }

    public int updateByPrimaryKeySelective(OrdRefundSaleRecord ordRefundSaleRecord) {
        return super.update("updateByPrimaryKeySelective", ordRefundSaleRecord);
    }

    public int updateByOrderItemIdSelective(OrdRefundSaleRecord ordRefundSaleRecord) {
        return super.update("updateByOrderItemIdSelective", ordRefundSaleRecord);
    }

    public List<OrdRefundSaleRecord> findOrdRefundSaleRecordList(Map<String,Object> params){
        return super.getList("selectByParams", params);
    }
}
