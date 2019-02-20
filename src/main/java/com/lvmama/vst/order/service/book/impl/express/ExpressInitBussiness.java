/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.express;

import org.springframework.stereotype.Component;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 快递数据初始化
 * @author lancey
 *
 */
@Component("expressInitBussiness")
public class ExpressInitBussiness extends AbstractBookService implements OrderInitBussiness {	
	
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.book.OrderInitBussiness#initOrderItem(com.lvmama.vst.back.order.po.OrdOrderItem, com.lvmama.vst.order.vo.OrdOrderDTO)
	 */
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		return false;
	}
}
