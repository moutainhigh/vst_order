package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.service.IOrdAddressService;
import com.lvmama.vst.order.service.IOrdPersonService;

@Service
public class OrdAddressServiceImpl implements IOrdAddressService {

	private static final Log LOG = LogFactory
			.getLog(OrdAddressServiceImpl.class);
	@Autowired
	private OrdAddressDao ordAddressDao;
	
	@Autowired
	private IOrdPersonService ordPersonService;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	
	@Override
	public int addOrdAddress(OrdAddress ordAddress) {
		// TODO Auto-generated method stub
		return ordAddressDao.insert(ordAddress);
	}
	@Override
	public OrdAddress findOrdAddressById(Long id) {
		// TODO Auto-generated method stub
		return ordAddressDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdAddress> findOrdAddressList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordAddressDao.findOrdAddressList(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdAddress ordAddress) {
		// TODO Auto-generated method stub
		return ordAddressDao.updateByPrimaryKeySelective(ordAddress);
	}

	public void updateOrdAddressAndPerson(OrdPerson ordPerson,
			OrdAddress ordAddress, Long orderId,String loginUserId) {
		
		OrdPerson oldPerson=ordPersonService.findOrdPersonById(ordPerson.getOrdPersonId());
		
		ordPersonService.updateByPrimaryKeySelective(ordPerson);
		
		
		OrdAddress  oldOrdAddress =ordAddressDao.selectByPrimaryKey(ordAddress.getOrdAddressId());
		ordAddressDao.updateByPrimaryKeySelective(ordAddress);
		
//		orderUpdateService.updateOrderPerson(ordPerson);
		
		
		
		//添加操作日志
		StringBuffer logStr = new StringBuffer("");
		logStr.append(ComLogUtil.getLogTxt("姓名",ordPerson.getFullName(),oldPerson.getFullName()));
		logStr.append(ComLogUtil.getLogTxt("手机",ordPerson.getMobile(),oldPerson.getMobile()));
		logStr.append(ComLogUtil.getLogTxt("街道",oldOrdAddress.getStreet(),ordAddress.getStreet()));
		logStr.append(ComLogUtil.getLogTxt("邮政编码",oldOrdAddress.getPostalCode(),ordAddress.getPostalCode()));
		
		
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				loginUserId, 
				"将编号为["+orderId+"]的订单，更新快递联系人和快递地址", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新快递联系人和快递地址",
				logStr.toString());
	}
	
}
