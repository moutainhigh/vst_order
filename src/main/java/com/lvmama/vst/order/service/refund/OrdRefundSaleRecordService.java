package com.lvmama.vst.order.service.refund;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lijuntao on 2017/1/16.
 */
public interface OrdRefundSaleRecordService {
    public int insert(OrdRefundSaleRecord ordRefundSaleRecord);

    public OrdRefundSaleRecord selectByPrimaryKey(Long ordRefundSaleRecordId);

    public int updateByPrimaryKeySelective(OrdRefundSaleRecord ordRefundSaleRecord);

    public int updateByOrderItemIdSelective(OrdRefundSaleRecord ordRefundSaleRecord);

    public List<OrdRefundSaleRecord> findOrdRefundSaleRecordList(Map<String,Object> params);

    /**
     * 根据订单初始化记录
     * @param ordOrder
     */
    public void init(OrdOrder ordOrder, Date applyTime);

    public List<OrdRefundSaleRecord> getOrdRefundSaleRecordByOrder(OrdOrder ordOrder, Date applyTime);
}
