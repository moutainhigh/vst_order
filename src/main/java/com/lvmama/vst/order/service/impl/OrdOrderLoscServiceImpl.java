/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderLosc;
import com.lvmama.vst.order.dao.OrdOrderLoscDao;
import com.lvmama.vst.order.service.IOrdOrderLoscService;

/**
 * @author pengyayun
 *
 */
@Service
public class OrdOrderLoscServiceImpl implements IOrdOrderLoscService {
	
	@Autowired
	private OrdOrderLoscDao ordOrderLoscDao;
	
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.IOrdOrderLoscService#addOrderLosc(com.lvmama.vst.back.order.po.OrdOrderLosc)
	 */
	@Override
	public int addOrderLosc(OrdOrderLosc ordOrderLosc) {
		// TODO Auto-generated method stub
		return ordOrderLoscDao.insert(ordOrderLosc);
	}

}
