package com.lvmama.vst.order.contract.service.impl;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.order.contract.service.IOrderContractSnapshotService;
import com.lvmama.vst.order.dao.OrderContactSnapshotDao;

/**
 * @author jswangxiaowei
 *
 */
@Service
public class OrderContractSnapshotServiceImpl implements IOrderContractSnapshotService {
	
	private static final Log LOG = LogFactory.getLog(OrderContractSnapshotServiceImpl.class);
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private OrderContactSnapshotDao orderContactSnapshotDao;

	@Override
	public int saveContractSnapshot(OrdContractSnapshotData ordContractSnapshotData,String operatorName) {
		LOG.info("");
		int result = 0;
		if (ordContractSnapshotData != null) {
			result = orderContactSnapshotDao.insertSelective(ordContractSnapshotData);
			if (result > 0) {
				String content = "合同快照记录生成：" + getContractSnapshotContent(ordContractSnapshotData);
				insertOrderLog(ordContractSnapshotData.getOrdContractId(), ordContractSnapshotData.getJsonFileId(), operatorName, 
						content, COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE, null);
			}
		}
		return result;
	
	}
	
	private String getContractSnapshotContent(OrdContractSnapshotData ordContractSnapshotData) {

		String createTime = null;
		if (ordContractSnapshotData.getCreateTime() != null) {
			createTime = DateUtil.formatDate(ordContractSnapshotData.getCreateTime(), "yyyy-MM-dd hh:mm:ss");
		}
		String content = "合同快照ID=" + ordContractSnapshotData.getSnapshotDataId()
				+ ",合同ID=" + ordContractSnapshotData.getOrdContractId()
				+ ",快照json文件名=" + ordContractSnapshotData.getJsonFileId()
				+ ", 创建时间=" + createTime ;
		return content;
	}

	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertOrderLog(final Long orderId, Long contractId,String operatorName,String content, COM_LOG_LOG_TYPE logType, String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ECONTRACT,
				orderId, 
				contractId, 
				operatorName, 
				content, 
				logType.name(), 
				logType.getCnName(),
				memo);
	}
	
	@Override
	public OrdContractSnapshotData selectByParam(Map<String, Object> params) {
		List<OrdContractSnapshotData> list = orderContactSnapshotDao.selectByParam(params);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
}
