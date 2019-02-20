package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdTravAdditionConf;


/**
 * 
 * 目的地出游人补全表单配置业务
 * 
 * @author CHENHAO
 *
 */
public interface IOrdTravAdditionConfService {
	/**
	 * 保存出游人补全表单配置
	 * 
	 * @param comAudit
	 */
	int saveTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf);
	

	/**
	 * 根据条件查询出游人补全表单配置记录
	 * 
	 * @param param
	 * @return
	 */
	public List<OrdTravAdditionConf> queryOrdTravAdditionConfByParam(Map<String, Object> param);
    
    /**
     * 
     * @Description: 更新
     * @author Wangsizhi
     * @date 2016-11-29 下午3:20:25
     */
    public int updateTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf);
	
}
