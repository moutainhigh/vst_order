package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;

/**
 * Created at 2015/9/1
 * @author yangzhenzhong
 *
 */
public interface IOrdOrderItemPassCodeSMSService {
	
	public void updateStatus(Long id);

	public List<OrdOrderItemPassCodeSMS> queryByPassCodeId(List<Long> passCodeIdList);
	
	public void inert(OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS);
	
	public List<OrdOrderItemPassCodeSMS> queryByOrderId(Long orderId);
	
	public List<OrdOrderItemPassCodeSMS> queryByStatus(String status);
	
	public List<OrdOrderItemPassCodeSMS> queryYesterdayByStatus(String status);
	
	public OrdOrderItemPassCodeSMS queryByOrderIdPassCodeId(Long orderId,Long passCodeId);
	
	public boolean isExistOfFlagData(Long orderId);
	

}
