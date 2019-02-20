package com.lvmama.vst.order.confirm.job;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.utils.ConfirmUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件确认
 * @version 1.0
 */
public class ConfirmEmailJob implements Runnable{
	private static final Log LOG =LogFactory.getLog(ConfirmEmailJob.class);
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
    private VstEmailServiceAdapter vstEmailService;
	
	@Override
	public void run() {
		boolean enabled = ConfirmUtils.isNewWorkflowStart();
		String date =DateUtil.formatDate(new Date(), DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss);
		LOG.info("ConfirmEmailJob&run,date=" +date +",enabled=" +enabled);
		if(!enabled) return ;
		try{
			Map<String, Object> params =new HashMap<String, Object>();
			params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			params.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT.name());
			params.put("auditStatusArray", Arrays.asList(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()
					,OrderEnum.AUDIT_STATUS.POOL.name()));
			int count =orderAuditService.getTotalCount(params);
			// 当订单数 阀值 20 
			if(count >20){
				//发送邮件
				String content ="当前新单库订单量为" +count +"，已超过阀值20，请处理";
				sendEmail(content);
				
				//发送短信 当前新单库订单量为XX，已超过阀值20，请处理
			}
		}catch(Exception e){
			LOG.error("ConfirmEmailJob&run,date=" +date 
					+",exception：" +ExceptionFormatUtil.getTrace(e));
		}
		
	}
	private void sendEmail(String content){
		try {
			EmailContent emailContent = new EmailContent();
	        emailContent.setFromAddress("service@cs.lvmama.com");
	        emailContent.setFromName("驴妈妈旅游网");
	        emailContent.setSubject("新单库订单量已超过阀值20，请处理");
	        emailContent.setToAddress("wangfeixiang@lvmama.com;qianfang@lvmama.com;luwei@lvmama.com;dengchen@lvmama.com;xiaoyulin@lvmama.com;");
	        emailContent.setContentText(content.toString());
	        vstEmailService.sendEmail(emailContent);
		} catch (Exception ex) {
			LOG.error("ConfirmEmailJob sendEmail：" +ExceptionFormatUtil.getTrace(ex));
		}
	}

}
