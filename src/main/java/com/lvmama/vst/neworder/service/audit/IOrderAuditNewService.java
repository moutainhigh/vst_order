package com.lvmama.vst.neworder.service.audit;

import com.lvmama.vst.back.pub.po.ComAudit;

/** 
* @ImplementProject vst_order
* @Description: 新订单活动服务
* @author xiaoyulin
* @date 2017年8月29日 下午3:05:19 
*/
public interface IOrderAuditNewService {
	
	/**
	 * 创建（主、子）订单活动
	 * @param objectId
	 * @param objectType
	 * @param auditType
	 * @param auditSubType
	 * @return
	 */
	public ComAudit saveComAudit(Long objectId,String objectType,String auditType,String auditSubType);
}
