package com.lvmama.vst.order.client.ord.service.impl.refund;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.passport.service.PassportService;
import com.lvmama.vst.back.order.po.OrdRefundSaleRecord;
import com.lvmama.vst.order.service.refund.OrdRefundSaleRecordService;
import com.lvmama.vst.pet.adapter.refund.OrdRefundSaleRecordClientService;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 在线退款记录服务(vst_workflow)
 * @version 1.0
 */
@Component("OrdRefundSaleRecordServiceRemote")
public class OrdRefundSaleRecordClientServiceImpl implements OrdRefundSaleRecordClientService {

    private final static Logger LOG = LoggerFactory.getLogger(OrdRefundSaleRecordClientServiceImpl.class);

    @Autowired
    private OrdRefundSaleRecordService ordRefundSaleRecordService;

    @Autowired
    private PassportService passportService;

    @Override
    public int updateRefundSaleRecord(OrdRefundSaleRecord ordRefundSaleRecord) {
        LOG.info("updateRefundSaleRecord : " + ordRefundSaleRecord.toString());
        return ordRefundSaleRecordService.updateByOrderItemIdSelective(ordRefundSaleRecord);
    }

    @Override
    public boolean isRefundSaleByOrderId(Long orderId) {
        LOG.info("isRefundSaleByOrderId : " + orderId);
        Map<String, Object> params = new HashedMap();
        params.put("orderId", orderId);
        List<OrdRefundSaleRecord> recordList = ordRefundSaleRecordService.findOrdRefundSaleRecordList(params);
        if(recordList != null){
            LOG.info("recordList.size = "+recordList.size());
            for(OrdRefundSaleRecord record : recordList){
                LOG.info("record = " + record.toString());
                if(!OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue().equals(record.getStatus())){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isCancelConfirm(Long orderId) {
        LOG.info("isCancelConfirm : " + orderId);
        return isRefundSaleByOrderId(orderId);
    }

    @Override
    public void cancelConfirm(Long orderId) {
        LOG.info("cancelConfirm : " + orderId);
        boolean result = false;
        try{
            result = passportService.checkPassCodeDestroyStatus(orderId);
        }catch (Exception e){
            LOG.error("cancelConfirm ERROR : orderId = " + orderId, e.getStackTrace() );
        }

        if(result){
            Map<String, Object> params = new HashedMap();
            params.put("orderId", orderId);
            List<OrdRefundSaleRecord> recordList = ordRefundSaleRecordService.findOrdRefundSaleRecordList(params);
            if(recordList != null){
                LOG.info("recordList.size = "+recordList.size());
                for(OrdRefundSaleRecord record : recordList){
                    LOG.info("record = " + record.toString());
                    if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(record.getCategoryId()) ||
                            BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(record.getCategoryId()) ||
                            BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(record.getCategoryId())){
                        record.setStatus(OrdRefundSaleRecord.STATUS.REFUNDFORM.getValue());
                        updateRefundSaleRecord(record);
                    }
                }
            }
        }

    }

    @Override
    public List<OrdRefundSaleRecord> findOrdRefundSaleRecordList(Map<String, Object> params) {
        LOG.info("findOrdRefundSaleRecordList  : params =" + params);
        return ordRefundSaleRecordService.findOrdRefundSaleRecordList(params);
    }
}
