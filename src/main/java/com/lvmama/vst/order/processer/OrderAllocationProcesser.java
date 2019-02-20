package com.lvmama.vst.order.processer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;

public class OrderAllocationProcesser implements MessageProcesser{
	protected transient final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
	
	//订单活动审核业务
	@Autowired
	private IOrderAuditService orderAuditService;

	@Override
	public void process(Message message) {
//		logger.info("start OrderAllocationProcesser.process");
		if(MessageUtils.isOrderAllocationMsg(message)){
			Long auditId = message.getObjectId();
			String addition = message.getAddition();
			ComAudit comAudit = orderAuditService.queryAuditById(auditId);
			if(comAudit!=null){
				logger.info("===orderId:"+comAudit.getObjectId()+"=objectType:"+comAudit.getObjectType()+"comaudit:"+comAudit.getOperatorName());
			}else{
				logger.info("===auditId:"+auditId+",comAudit is null");
			}
			if(StringUtils.isEmpty(comAudit.getOperatorName()) || "SYSTEM".equals(comAudit.getOperatorName())){
				try{
					logger.info("OrderAllocationProcesser-auditId:"+auditId+",addition"+addition);
					if("TRUE".equals(addition)){
						distributionBusiness.makeOrderAudit(comAudit);
					}
					else if("FALSE".equals(addition)){//分销自动过分人
						distributionBusiness.makeOrderAuditForDistribution(comAudit);
					}
				}catch(Exception ex){
					logger.info(ex.getMessage());
				}
			}
		}
//		logger.info("end OrderAllocationProcesser.process");
	}

}
