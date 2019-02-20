package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrderConnectsServicePropDao;
import com.lvmama.vst.order.service.OrderConnectsServicePropService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hanlei 
 */
@Service
public class OrderConnectsServicePropServiceImpl implements OrderConnectsServicePropService {
	private static final Log LOG = LogFactory.getLog(OrderConnectsServicePropServiceImpl.class);
	@Autowired
	private OrderConnectsServicePropDao orderConnectsServicePropDao;
	
	
	@Override
	public List<OrderConnectsServiceProp> findOrderConnectsServicePropList(Map<String, Object> params)
			throws BusinessException {
		List<OrderConnectsServiceProp> orderConnectsServicePropList = null;
		try {
			orderConnectsServicePropList = orderConnectsServicePropDao.findOrderConnectsServicePropList(params);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return orderConnectsServicePropList;
	}

	@Override
	public OrderConnectsServiceProp findOrderConnectsServicePropById(Long orderServiceId) throws BusinessException {
		OrderConnectsServiceProp orderConnectsServiceProp = null;
		try {
			orderConnectsServiceProp = orderConnectsServicePropDao.selectByPrimaryKey(orderServiceId);
		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return orderConnectsServiceProp;
	}

	@Override
	public Integer addOrderConnectsServiceProp(OrderConnectsServiceProp orderConnectsServiceProp)
			throws BusinessException {
		Integer result=0;
		try {
			result  = orderConnectsServicePropDao.insert(orderConnectsServiceProp);
		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
		
	}

	@Override
	public Integer deleteOrderConnectsServicePropById(Long orderServiceId) throws BusinessException {
		Integer result=0;
		try {
			result  = orderConnectsServicePropDao.deleteByPrimaryKey(orderServiceId);
		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
		

	}

	@Override
	public Integer updateOrderConnectsServicePropById(OrderConnectsServiceProp orderConnectsServiceProp) throws BusinessException {
		Integer result=0;
		try {
			result  = orderConnectsServicePropDao.updateBySelective(orderConnectsServiceProp);
		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
		
	}

	@Override
	public List<OrderConnectsServiceProp> queryOrderConnectsPropByParams(Map<String,Object> params){
		return orderConnectsServicePropDao.queryOrderConnectsPropByParams(params);
	}

	@Override
	public Integer updateOrderConnectsServicePropByOrderId(OrderConnectsServiceProp orderConnectsServiceProp)
			throws BusinessException {
		
		Integer result=0;
		try {
			result  = orderConnectsServicePropDao.updateOrderConnectsServicePropByOrderId(orderConnectsServiceProp);

		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

}
