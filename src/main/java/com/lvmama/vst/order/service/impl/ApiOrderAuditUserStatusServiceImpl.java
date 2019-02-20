package com.lvmama.vst.order.service.impl;

import com.lvmama.order.vst.api.common.service.IApiOrderAuditUserStatusService;
import com.lvmama.order.vst.api.common.vo.request.OrdAuditUserStatusVo;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author houjian
 * @date 2018/4/27.
 */
@Service("apiOrderAuditUserStatusService")
public class ApiOrderAuditUserStatusServiceImpl implements IApiOrderAuditUserStatusService {
    @Autowired
    private IOrderAuditUserStatusService orderAuditUserStatusService;

    @Override
    public OrdAuditUserStatusVo selectByPrimaryKey(String operatorName) {
        if (StringUtils.isEmpty(operatorName))
            return null;
        OrdAuditUserStatus source = this.orderAuditUserStatusService.selectByPrimaryKey(operatorName);
        if (source == null)
            return null;
        OrdAuditUserStatusVo target = new OrdAuditUserStatusVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @Override
    public List<OrdAuditUserStatusVo> findOrdAuditUserStatusList(Map<String, Object> params) {
        List<OrdAuditUserStatusVo> ordAuditUserStatusVoList = new ArrayList<>();
        if (params.isEmpty())
            return ordAuditUserStatusVoList;
        List<OrdAuditUserStatus> ordAuditUserStatusList = this.orderAuditUserStatusService.findOrdAuditUserStatusList(params);
        if (CollectionUtils.isEmpty(ordAuditUserStatusList))
            return ordAuditUserStatusVoList;
        for (OrdAuditUserStatus source : ordAuditUserStatusList) {
            OrdAuditUserStatusVo target = new OrdAuditUserStatusVo();
            BeanUtils.copyProperties(source, target);
            ordAuditUserStatusVoList.add(target);
        }
        return ordAuditUserStatusVoList;
    }

    @Override
    public int deleteByPrimaryKey(String operatorName) {
        if (StringUtils.isEmpty(operatorName))
            return 0;
        return this.orderAuditUserStatusService.deleteByPrimaryKey(operatorName);
    }

    @Override
    public int insert(OrdAuditUserStatusVo ordAuditUserStatusVo) {
        if (ordAuditUserStatusVo == null)
            return 0;
        OrdAuditUserStatus ordAuditUserStatus = new OrdAuditUserStatus();
        BeanUtils.copyProperties(ordAuditUserStatusVo, ordAuditUserStatus);
        return this.orderAuditUserStatusService.insert(ordAuditUserStatus);
    }

    @Override
    public int updateByPrimaryKey(OrdAuditUserStatusVo ordAuditUserStatusVo) {
        if (ordAuditUserStatusVo == null)
            return 0;
        OrdAuditUserStatus ordAuditUserStatus = new OrdAuditUserStatus();
        BeanUtils.copyProperties(ordAuditUserStatusVo, ordAuditUserStatus);
        return this.orderAuditUserStatusService.updateByPrimaryKey(ordAuditUserStatus);
    }
}
