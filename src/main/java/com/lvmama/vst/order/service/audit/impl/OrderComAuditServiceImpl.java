package com.lvmama.vst.order.service.audit.impl;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderComAuditService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author : liuchenggui
 * @Description:
 * @Date : Create in 上午10:43 2018/4/10
 */
@Service("orderComAuditService")
public class OrderComAuditServiceImpl implements IOrderComAuditService {

    @Autowired
    private IOrderAuditService orderAuditService;

    @Override
    public int changeAuditSatusByParam(Map<String, Object> param) {

        param.put("auditStatus",OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
        List<ComAudit> list =  orderAuditService.queryAuditListByParam(param);

        if(CollectionUtils.isEmpty(list)) return 0;

        ComAudit comAudit = list.get(0);
        comAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());

        return orderAuditService.updateByPrimaryKey(comAudit);
    }
}
