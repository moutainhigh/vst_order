package com.lvmama.vst.neworder.service.audit.impl;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_OBJECT_TYPE;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.neworder.service.audit.IOrderAuditNewService;
import com.lvmama.vst.order.dao.ComAuditDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;

/** 
* @ImplementProject vst_order
* @Description: 订单活动新服务
* @author xiaoyulin
* @date 2017年8月29日 下午3:10:52 
*/
@Service("orderAuditNewService")
public class OrderAuditNewServiceImpl implements IOrderAuditNewService {
	
	@Autowired
	private ComAuditDao comAuditDao;

	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private  OrdOrderItemDao ordOrderItemDao;

	@Override
	public ComAudit saveComAudit(Long objectId, String objectType, String auditType, String auditSubType) {
		ComAudit audit = new ComAudit();
		audit.setObjectType(objectType);
		audit.setObjectId(objectId);
		audit.setAuditType(auditType);
		audit.setAuditSubtype(auditSubType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		int num = comAuditDao.insert(audit);
		
		if(num > 0){
			// 生成活动创建操作日志
			this.createComAuditLog(objectId, objectType, auditType);
		}
		
		return audit;
	}
	
	/**
	 * 生成活动创建操作日志
	 * @param objectId
	 * @param objectType
	 * @param auditType
	 */
	private void createComAuditLog(Long objectId, String objectType, String auditType){
		// 保存操作日志
		Long orderId = null;
		COM_LOG_OBJECT_TYPE logObjectType = null;
		if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType.trim())) {
			OrdOrderItem ordOrderItem = ordOrderItemDao.selectByPrimaryKey(objectId);
			orderId = ordOrderItem.getOrderId();
			logObjectType = ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.trim())){
			orderId = objectId;
			logObjectType = ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		}
		
		lvmmLogClientService.sendLog(logObjectType,
				orderId, 
				objectId, 
				Constants.SYSTEM, 
				"将编号为["+objectId+"]的订单,系统自动创建订单活动["+ ConfirmEnumUtils.getCnName(auditType) +"]",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				null);
	}

}
