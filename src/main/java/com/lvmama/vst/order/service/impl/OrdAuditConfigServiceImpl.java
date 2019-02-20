package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdAuditConfig;
import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.order.dao.OrdAuditConfigDao;
import com.lvmama.vst.order.service.IOrdAuditConfigService;
import com.lvmama.vst.order.service.IOrdFunctionService;

/**
 * 订单活动权限配置业务实现
 * 
 * @author wenzhengtao
 * @author zhangwei
 */
@Service
public class OrdAuditConfigServiceImpl implements IOrdAuditConfigService {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(OrdAuditConfigServiceImpl.class);
	
	@Autowired
	private OrdAuditConfigDao ordAuditConfigDao;

	@Autowired
	private IOrdFunctionService ordFunctionService;
	
	
	@Override
	public List<OrdAuditConfig> findOrdAuditConfigList(
			Map<String, Object> params) {
		return ordAuditConfigDao.findOrdAuditConfigList(params);
	}

	public List<OrdAuditConfig> findOrdAuditConfigListGroupBy(Map<String, Object> params){
		
		return ordAuditConfigDao.findOrdAuditConfigListGroupBy(params);
	}
	public int deleteOrdAuditConfigs (Map<String, Object> params)
	{
		return ordAuditConfigDao.deleteOrdAuditConfigs(params);
	}
	@Override
	public int findOrdAuditConfigCount(Map<String, Object> params) {
		return ordAuditConfigDao.getTotalCount(params);
	}

	@Override
	public int saveOrdAuditConfig(OrdAuditConfig ordAuditConfig) {
		return ordAuditConfigDao.insert(ordAuditConfig);
	}

	@Override
	public OrdAuditConfig findOrdAuditConfigById(Long id) {
		return ordAuditConfigDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateOrdAuditConfig(OrdAuditConfig ordAuditConfig) {
		return ordAuditConfigDao.updateByPrimaryKeySelective(ordAuditConfig);
	}
	
	public int deleteOrdAuditConfigById(Long id){
		
		return  ordAuditConfigDao.deleteByPrimaryKey(id);
	}
	
	
	public List<OrdAuditConfig> findOrdAuditConfigList(Long categoryId,String functionCode){
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("functionCode", functionCode);
		
		List<OrdFunction> ordFunctionList =ordFunctionService.findOrdFunctionList(parameters);
		
		if(null != ordFunctionList && !ordFunctionList.isEmpty()){
			OrdFunction ordFunction=ordFunctionList.get(0);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("categoryId", categoryId);
			params.put("ordFunctionId", ordFunction.getOrdFunctionId());
			
			return ordAuditConfigDao.findOrdAuditConfigList(params);
		}else{
			return null;
		}
	}
	
	public List<OrdAuditConfig> findOrdAuditConfigList(Long categoryId,String functionCode,String operatorName){
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("functionCode", functionCode);
		
		List<OrdFunction> ordFunctionList =ordFunctionService.findOrdFunctionList(parameters);
		
		if(null != ordFunctionList && !ordFunctionList.isEmpty()){
			OrdFunction ordFunction=ordFunctionList.get(0);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("categoryId", categoryId);
			params.put("operatorName", operatorName);
			params.put("ordFunctionId", ordFunction.getOrdFunctionId());
			
			return ordAuditConfigDao.findOrdAuditConfigList(params);
		}else{
			return null;
		}
	}
	
	public OrdAuditConfig findOrdAuditConfig(Long categoryId,String functionCode,String operatorName){
		
		List<OrdAuditConfig>  ordAuditConfigList=this.findOrdAuditConfigList(categoryId, functionCode, operatorName);
		if (ordAuditConfigList.size()==0) {
			return  null;
		}else {
			return ordAuditConfigList.get(0);
		}
		
		
	}
	public void saveAndDeleteOrdAuditList(List<OrdAuditConfig> ordAuditConfigList,
			String[] operatorNameArray,Long[] categoryIdArray){
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryIdArray", categoryIdArray);
		params.put("operatorNameArray", operatorNameArray);
		ordAuditConfigDao.deleteOrdAuditConfigs(params);
		
		for (int i = 0; i < categoryIdArray.length; i++)
		{
			Long categoryId = categoryIdArray[i];
			for (int j = 0; j < operatorNameArray.length;j++) 
			{
				String operatorName=operatorNameArray[j];
				for (OrdAuditConfig ordAuditConfig : ordAuditConfigList) 
				{
					if (ordAuditConfig.getOrdFunctionId()!=null) 
					{
						ordAuditConfig.setCategoryId(categoryId);
						ordAuditConfig.setOperatorName(operatorName);
						ordAuditConfigDao.insert(ordAuditConfig);
					}
				}
			}
		}
		
	}
}
