/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.express;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("expressItemSaveBussiness")
public class ExpressItemSaveBussiness extends AbstractBookService implements OrderItemSaveBussiness{
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		// TODO Auto-generated method stub
		
	}
}
