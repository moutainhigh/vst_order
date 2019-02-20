package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdAuditConfig;
/**
 * 订单活动权限配置业务
 * 
 * @author wenzhengtao
 * @author zhangwei
 */
public interface IOrdAuditConfigService {
	
	public int saveOrdAuditConfig(OrdAuditConfig ordAuditConfig);

	public List<OrdAuditConfig> findOrdAuditConfigList(Map<String, Object> params);
	
	public List<OrdAuditConfig> findOrdAuditConfigListGroupBy(Map<String, Object> params);

	public OrdAuditConfig findOrdAuditConfigById(Long id);

	public int updateOrdAuditConfig(OrdAuditConfig ordAuditConfig);
	
	public int deleteOrdAuditConfigById(Long id);
	
	public int deleteOrdAuditConfigs (Map<String, Object> params);

	public int findOrdAuditConfigCount(Map<String, Object> params);
	
	public List<OrdAuditConfig> findOrdAuditConfigList(Long categoryId,String functionCode);
	/**
	 * 根据品类ID,活动组，具体人查询
	 * 
	 * @param categoryId
	 * @param functionCode
	 * @param operatorName
	 * @return
	 */
	public List<OrdAuditConfig> findOrdAuditConfigList(Long categoryId,String functionCode,String operatorName);
	
	public OrdAuditConfig findOrdAuditConfig(Long categoryId,String functionCode,String operatorName);
	
	public void saveAndDeleteOrdAuditList(List<OrdAuditConfig> ordAuditConfig,String[] operatorNameArray,Long[] categoryIdArray) ;
	
}
