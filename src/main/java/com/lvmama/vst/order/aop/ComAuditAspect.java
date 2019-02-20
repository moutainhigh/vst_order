package com.lvmama.vst.order.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.audit.ComAuditQueryConditionRaidService;


/**
 * 订单活动数据操作切面
 * @author xiaoyulin
 *
 */
@Aspect
@Order(0)
@Component
public class ComAuditAspect {
	private final Logger LOG = LoggerFactory.getLogger(ComAuditAspect.class);
	/**
	 * 订单活动数据冗余服务
	 */
	@Autowired
	private ComAuditQueryConditionRaidService comAuditQueryConditionRaidService;

	 
	@AfterReturning(value = "execution(* com.lvmama.vst.order.dao.ComAuditDao.insert(..))", argNames = "rtv", returning = "rtv")
	public void doAfterReturningInsert(JoinPoint jp, Object rtv) throws Throwable {
		Object[] args = jp.getArgs();
		LOG.info("ComAuditAspect.doAfterReturningInsert start,args:" + args + ",rtv:" + rtv);
		//调用异步冗余数据入口服务
		try {
			ComAudit comAudit = null;
			if (args != null && args.length > 0) {
				for (Object o : args) {
					if (o != null && o instanceof ComAudit) {
						comAudit = (ComAudit) o;
						comAuditQueryConditionRaidService.saveQueryConditionRaidData(comAudit);
						break;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("ComAuditQueryConditionRaidService invoke error,{}",e);
		}
		
	}
	
	

}
