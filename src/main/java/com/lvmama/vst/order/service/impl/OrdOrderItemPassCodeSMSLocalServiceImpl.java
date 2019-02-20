package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;
import com.lvmama.vst.order.dao.OrdOrderItemPassCodeSMSDao;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;

/**
 * Created at 2015/9/6
 * @author yangzhenzhong
 *
 */
@Service
public class OrdOrderItemPassCodeSMSLocalServiceImpl implements IOrdOrderItemPassCodeSMSService {

	@Autowired
	private OrdOrderItemPassCodeSMSDao ordOrderItemPassCodeSMSDao;
	
	/**
	 * 根据id，set status=N
	 */
	@Override
	public void updateStatus(Long id) {
		// TODO Auto-generated method stub
		ordOrderItemPassCodeSMSDao.updateStatus(id);
		
	}

	@Override
	public List<OrdOrderItemPassCodeSMS> queryByPassCodeId(List<Long> passCodeIdList) {
		// TODO Auto-generated method stub
		return ordOrderItemPassCodeSMSDao.queryByPassCodeId(passCodeIdList);
	}

	@Override
	public void inert(OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS) {
		// TODO Auto-generated method stub
		ordOrderItemPassCodeSMSDao.insert(ordOrderItemPassCodeSMS);
	}
	
	/**
	 * 根据orderId 查询出status=Y，passCodeId !=0的数据
	 */
	@Override
	public List<OrdOrderItemPassCodeSMS> queryByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return ordOrderItemPassCodeSMSDao.queryByOrderId(orderId);
	}
	
	@Override
	public OrdOrderItemPassCodeSMS queryByOrderIdPassCodeId(Long orderId,Long passCodeId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public boolean isExistOfFlagData(Long orderId) {
		
		int count = ordOrderItemPassCodeSMSDao.queryCountOfFlagData(orderId);
		if(count==0){
			return false;
		}else{
			return true;
		}
	}

	/**
	 *查询出所有status=Y的数据 
	 */
	@Override
	public List<OrdOrderItemPassCodeSMS> queryByStatus(String status) {
		// TODO Auto-generated method stub
		return ordOrderItemPassCodeSMSDao.queryByStatus(status);
	}

	/**
	 * 根据status查询出所有创建时间为昨天的数据
	 */
	@Override
	public List<OrdOrderItemPassCodeSMS> queryYesterdayByStatus(String status) {
		// TODO Auto-generated method stub
		return ordOrderItemPassCodeSMSDao.queryYesterdayByStatus(status);
	}


	
}
