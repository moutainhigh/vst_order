package com.lvmama.vst.order.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.order.dao.OrderAttachmentDao;
import com.lvmama.vst.order.service.IOrderAttachmentService;
/**
 * 订单附件业务实现类
 * @author wenzhengtao
 *
 */
@Service("orderAttachmentService")
public class OrderAttachmentServiceImpl implements IOrderAttachmentService {
	//注入DAO层
	@Autowired
	private OrderAttachmentDao orderAttachmentDao;
	//注入日志DAO
	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Override
	public void saveOrderAttachment(OrderAttachment orderAttachment,String operatorName,String memo) {
		orderAttachmentDao.insert(orderAttachment);
		//创建订单操作日志
		ComLog log = new ComLog();
		log.setParentType(ComLog.COM_LOG_PARENT_TYPE.ORD_ORDER.name());
		log.setParentId(orderAttachment.getOrdAttachmentId());
		log.setObjectType(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.name());
		log.setObjectId(orderAttachment.getOrderId());
		log.setLogType(ComLog.COM_LOG_LOG_TYPE.UPLOAD_FILE.name());
		log.setLogName(ComLog.COM_LOG_LOG_TYPE.UPLOAD_FILE.getCnName());
		log.setOperatorName(operatorName);
		log.setContentType(ComLog.COM_LOG_CONTENT_TYPE.VARCHAR.name());
		log.setContent("给编号为["+orderAttachment.getOrderId()+"]的订单上传了附件,附件名为["+orderAttachment.getAttachmentName()+"],文件号为["+orderAttachment.getFileId()+"]的附件");
		log.setCreateTime(Calendar.getInstance().getTime());//当前时间
		log.setMemo("附件备注为["+memo+"]");//附件备注，这里也要存一份
		lvmmLogClientService.sendLog(log);
	}
	
	/**
	 * 由一个事务控制回滚
	 */
	public void saveOrderAttachment(OrderAttachment orderAttachment,ComLog comLog) {
		orderAttachmentDao.insert(orderAttachment);
		lvmmLogClientService.sendLog(comLog);
	}

	@Override
	public List<OrderAttachment> queryOrderAttachment(Long orderId) {
		return orderAttachmentDao.selectByOrderId(orderId);
	}

	@Override
	public int countOrderAttachment(Long orderId) {
		return orderAttachmentDao.countByOrderId(orderId);
	}

	@Override
	public List<OrderAttachment> findOrderAttachmentByCondition(Map<String, Object> params) {
		return orderAttachmentDao.findOrderAttachmentByCondition(params);
	}

	@Override
	public int countOrderAttachmentByCondition(Map<String, Object> params) {
		return orderAttachmentDao.countOrderAttachmentByCondition(params);
	}

	@Override
	public int updateOrderAttachmentFlag(Map<String, Object> param) {
		return orderAttachmentDao.updateOrderAttachmentFlag(param);
	}
}
