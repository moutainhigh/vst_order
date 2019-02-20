package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.order.OrderAttachmentVO;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderAttachmentService;

/**
 * 订单附件上传
 * 
 * 1,普通附件
 * 2,凭证确认附件
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderAttachmentAction extends BaseActionSupport{
	//日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderAttachmentAction.class);
	//上传附件页面
	private static final String UPLOAD_ORDER_ATTACHMENT_PAGE = "/order/orderAttachment/uploadOrderAttachment";
	//查看附件页面
	private static final String VIEW_ORDER_ATTACHMENT_PAGE = "/order/orderAttachment/viewOrderAttachment";
	//注入业务层
	@Autowired
	private IOrderAttachmentService orderAttachmentService;
	
	@Autowired
	private ComLogClientService comLogClientService;

	/**
	 * 进入上传附件页面
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoUploadOrderAttachmentPage.do")
	public String intoUploadOrderAttachmentPage(Model model,Long orderId) throws BusinessException{
		model.addAttribute("orderId",orderId);
		return UPLOAD_ORDER_ATTACHMENT_PAGE;
	}
	
	/**
	 * 
	 * @param model
	 * @param orderId
	 * @param fileId
	 * @param fileName
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/addOrderAttachment.do")
	public void addOrderAttachment(Model model,OrderAttachmentVO orderAttachmentVO,HttpServletResponse response,HttpServletRequest req) throws BusinessException{
		//构造返回的json数据
		JSONObject jsonObject = new JSONObject();
		try {
			String orderType=orderAttachmentVO.getOrderType();
			
			Long orderId = orderAttachmentVO.getOrderId();
			Long fileId = orderAttachmentVO.getFileId();
			String fileName = orderAttachmentVO.getFileName();
			String memo = orderAttachmentVO.getMemo();
			
			//创建附件表记录
			OrderAttachment orderAttachment = new OrderAttachment();
			orderAttachment.setOrderId(orderAttachmentVO.getOrderId());
			orderAttachment.setAttachmentType(OrderEnum.ATTACHMENT_TYPE.COMMON.name());
			orderAttachment.setAttachmentName(orderAttachmentVO.getFileName());
			orderAttachment.setMemo(orderAttachmentVO.getMemo());
			orderAttachment.setCreateTime(Calendar.getInstance().getTime());
			orderAttachment.setFileId(orderAttachmentVO.getFileId());
			orderAttachment.setConfirmType(null);//普通附件没有
			//orderAttachment.setOrderItemId(orderAttachmentVO.getOrderItemId());
			
			if (StringUtils.isEmpty(orderType)) {
				
				orderAttachment.setOrderItemId(null);
				orderAttachmentService.saveOrderAttachment(orderAttachment,this.getLoginUserId(),memo);
				
			}else if ( "child".equals(orderType) || "parent".equals(orderType)){
				
				Long orderItemId=orderAttachmentVO.getOrderItemId();
				orderAttachment.setOrderItemId(orderItemId);
				
				Long obojectId=null;
				if ( "parent".equals(orderType)){
					obojectId=orderId;
				}else{
					obojectId=orderItemId;
				}
				//创建订单操作日志
				ComLog log = new ComLog();
				log.setParentType(ComLog.COM_LOG_PARENT_TYPE.ORD_ORDER.name());
				log.setParentId(orderAttachment.getOrdAttachmentId());
				String objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.name();
				if ( "child".equals(orderType)){
					objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM.name();
				}
				log.setObjectType(objectType);
				log.setObjectId(obojectId);
				log.setLogType(ComLog.COM_LOG_LOG_TYPE.UPLOAD_FILE.name());
				log.setLogName(ComLog.COM_LOG_LOG_TYPE.UPLOAD_FILE.getCnName());
				log.setOperatorName(getLoginUserId());
				log.setContentType(ComLog.COM_LOG_CONTENT_TYPE.VARCHAR.name());
				log.setContent("给编号为["+orderAttachment.getOrderId()+"]的订单上传了附件,附件名为["+orderAttachment.getAttachmentName()+"],文件号为["+orderAttachment.getFileId()+"]的附件");
				log.setCreateTime(Calendar.getInstance().getTime());//当前时间
				log.setMemo("附件备注为["+memo+"]");//附件备注，这里也要存一份
				
				orderAttachmentService.saveOrderAttachment(orderAttachment, log);
			}
			
			
			
			//构造json数据
			jsonObject.put("result", "success");
			JSONOutput.writeJSON(response, jsonObject);
		} catch (Exception e) {
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
			jsonObject.put("result", "failure");
			JSONOutput.writeJSON(response, jsonObject);
		}
	}
	
	/**
	 * 查询订单附件
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryOrderAttachment.do")
	public String queryOrderAttachment(Model model,Long orderId,Long orderItemId,HttpServletRequest req) throws BusinessException{
		
		List<OrderAttachment> orderAttachmentList ;
		String orderType=req.getParameter("orderType");
		
		if ("parent".equals(orderType) ||  "child".equals(orderType) ) {
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("orderId", orderId);
			param.put("orderItemId", orderItemId);
			param.put("_orderby","ORD_ATTACHMENT.create_time desc");
			orderAttachmentList = orderAttachmentService.findOrderAttachmentByCondition(param);
		}else{
			
			orderAttachmentList = orderAttachmentService.queryOrderAttachment(orderId);
			
		}
		
		List<OrderAttachmentVO> resultList=new ArrayList<OrderAttachmentVO>();
		
		for (OrderAttachment orderAttachment : orderAttachmentList) {
			
			String comtent="";
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.name());
			parameters.put("parentId",orderAttachment.getOrdAttachmentId());
			parameters.put("parentType",ComLog.COM_LOG_PARENT_TYPE.ORD_ORDER.name());
			
//			List<ComLog> logList=comLogClientService.queryComLogListByMap(parameters).getReturnContent();
//			if (!CollectionUtils.isEmpty(logList)) {
//				ComLog comLog=logList.get(0);
//				comtent=comLog.getContent();
//			}
			
			
			OrderAttachmentVO orderAttachmentVO=new OrderAttachmentVO();
			BeanUtils.copyProperties(orderAttachment, orderAttachmentVO);
			orderAttachmentVO.setLogContent(comtent);
			
			resultList.add(orderAttachmentVO);
			
		}
//		model.addAttribute("orderAttachmentList", orderAttachmentList);
		model.addAttribute("orderAttachmentList", resultList);
		
		return VIEW_ORDER_ATTACHMENT_PAGE;
	}
}
