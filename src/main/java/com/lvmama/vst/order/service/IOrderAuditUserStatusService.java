package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;

/**
 * 员工状态业务接口
 * 
 * @author wenzhengtao
 *
 */
public interface IOrderAuditUserStatusService {
	int deleteByPrimaryKey(String operatorName);

    int insert(OrdAuditUserStatus record);

    int insertSelective(OrdAuditUserStatus record);

    OrdAuditUserStatus selectByPrimaryKey(String operatorName);

    int updateByPrimaryKeySelective(OrdAuditUserStatus record);

    int updateByPrimaryKey(OrdAuditUserStatus record);
    
    
    
    List<OrdAuditUserStatus> findOrdAuditUserStatusList(Map<String, Object> params);
    
    int findOrdAuditUserStatusCount(Map<String, Object> params);
    
    @ReadOnlyDataSource
    public OrdAuditUserStatus getRandomUserByUsers(String objectType,List<String> userIds);
}
