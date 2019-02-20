package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.order.dao.ComFileMapDAO;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IOrdTravelContractService;

@Service
public class OrdTravelContractServiceImpl implements IOrdTravelContractService,DisposableBean {

	private static final Log LOG = LogFactory
			.getLog(OrdTravelContractServiceImpl.class);
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDao;
	
	@Autowired
	protected ComFileMapDAO comFileMapDAO;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	@Override
	public void destroy() throws Exception {
		LOG.info("ShutdownHook in OrdTravelContractServiceImpl is executing.....");
		List<Runnable> list = executorService.shutdownNow();
		if(list != null){
			LOG.info("cancel Runnable :" + list.size());
		}
	}

	@Override
	public int addOrdTravelContract(OrdTravelContract ordTravelContract) {
		// TODO Auto-generated method stub
		return ordTravelContractDao.insert(ordTravelContract);
	}

	@Override
	public OrdTravelContract findOrdTravelContractById(Long id) {
		// TODO Auto-generated method stub
		return ordTravelContractDao.selectByPrimaryKey(id);
	}

	@Override
	public List<OrdTravelContract> findOrdTravelContractList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordTravelContractDao.selectByParam(params);
	}

	@Override
	public List<Map<String, Object>> findPushDataByList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordTravelContractDao.selectPushDataByParam(params);
	}
	
	@Override
	public int updateByPrimaryKeySelective(OrdTravelContract ordTravelContract) {
		// TODO Auto-generated method stub
		return ordTravelContractDao.updateByPrimaryKeySelective(ordTravelContract);
	}
	
	@Override
	public int updatePushDataByContractId(Map<String, Object> params) {
		return ordTravelContractDao.updatePushDataByContractId(params);
	}
	
	@Override
	public int saveOrdTravelContract(final OrdTravelContract ordTravelContract, final String operatorName) {
		int result = 0;
		if (ordTravelContract != null) {
			result = ordTravelContractDao.insertSelective(ordTravelContract);
			if (result > 0) {
				final String content = "订单合同记录生成：" + getContracteContent(ordTravelContract);
				//asynchronously save log message
				executorService.execute(new Runnable(){
					@Override
					public void run() {
						OrdTravelContractServiceImpl.this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), 
								operatorName, content, COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE, null);
					}
				});
				
				LOG.info("Order_Log_info : orderId:" + ordTravelContract.getOrderId() + ", OrdContractId:" + ordTravelContract.getOrdContractId()
						+ ", operatorName:" + operatorName + ", content:" + content 
						+ ", logType:" + COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE.name());
				
				return ordTravelContract.getOrdContractId().intValue();
			}
		}
		
		return result;
	}

	@Override
	public int updateByPrimaryKeySelective(OrdTravelContract ordTravelContract, String operatorName) {
		int result = 0;
		if (ordTravelContract != null) {
			result = ordTravelContractDao.updateByPrimaryKeySelective(ordTravelContract);
			if (result > 0) {
				String content = "订单合同记录更新：" + getContracteContent(ordTravelContract);
				insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, 
						content, COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_CHANGE, null);
			}
		}
		
		return result;
	}

	@Override
	public int deleteByPrimaryKey(Long contractId, String operatorName) {
		int result = 0;
		if (contractId != null) {
			OrdTravelContract ordTravelContract = ordTravelContractDao.selectByPrimaryKey(contractId);
			result = deleteByPrimaryKey(ordTravelContract, operatorName);
		}
		return result;
	}
	
	@Override
	public int deleteByPrimaryKey(OrdTravelContract ordTravelContract, String operatorName) {
		int result = 0;
		if (ordTravelContract != null) {
			result = ordTravelContractDao.deleteByPrimaryKey(ordTravelContract.getOrdContractId());
			if (result > 0) {
				String content = "订单合同记录删除：" + getContracteContent(ordTravelContract);
				insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, 
						content, COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_CHANGE, null);
			}
		}
		
		return result;
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
	
	private String getContracteContent(OrdTravelContract ordTravelContract) {
		String createTime = null;
		if (ordTravelContract.getCreateTime() != null) {
			createTime = DateUtil.formatDate(ordTravelContract.getCreateTime(), "yyyy-MM-dd hh:mm:ss");
		}
		
		String content = "订单ID=" + ordTravelContract.getOrderId() + "，合同名称=" + ordTravelContract.getContractName() 
				+ ", 模板=" + ordTravelContract.getContractTemplate() + "，签约方式=" + ordTravelContract.getSigningType()
				+ "，合同状态=" + ordTravelContract.getStatus() + "，文件ID=" + ordTravelContract.getFileId() + "，修订版本=" 
				+ ordTravelContract.getVersion() + ", 创建时间=" + createTime + "，附件URL=" + ordTravelContract.getAttachementUrl();
		
		
		return content;
	}
	

	/**
	 * 根据订单id修改合同状态
	 * @param params
	 * @return
	 */
	@Override
	public int updateContractStatusByOrderId(Map<String, Object> params) {
		return ordTravelContractDao.updateContractStatusByOrderId(params);
	}
	
	@Override
	public ComFileMap getComFileMapByFileName(String fileName){
		return comFileMapDAO.getByFileName(fileName);
	}
	
	@Override
	public int updateSendEmailFlag(Set<Long> ids) {
		return ordTravelContractDao.updateSendEmailFlag(ids);
	}
	
	
}
