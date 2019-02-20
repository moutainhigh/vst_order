package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;

/** 
 * @Title: IOrdAccInsDelayInfoService.java 
 * @Package com.lvmama.vst.order.service 
 * @Description: TODO 
 * @author Wangsizhi
 * @date 2017-2-14 下午4:51:25 
 * @version V1.0.0 
 */
public interface IOrdAccInsDelayInfoService {
    /**
     * 保存
     */
    int saveOrdAccInsDelayInfo(OrdAccInsDelayInfo ordAccInsDelayInfo);
    
    /**
     *根据orderId查询 
     */
    OrdAccInsDelayInfo selectByOrderId(Long orderId);

    /**
     * 根据条件查询
     */
    public List<OrdAccInsDelayInfo> queryOrdAccInsDelayInfoByParam(Map<String, Object> param);
    
    /**
     * @Description: 更新
     */
    public int updateOrdAccInsDelayInfo(OrdAccInsDelayInfo ordAccInsDelayInfo);
}
