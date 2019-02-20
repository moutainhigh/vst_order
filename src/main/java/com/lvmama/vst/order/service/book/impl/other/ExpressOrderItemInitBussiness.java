/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.other;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("expressOrderItemInitBussiness")
public class ExpressOrderItemInitBussiness extends AbstractBookService implements OrderInitBussiness{
	
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		
		if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
			return true;
		}
		
		if(order.isCreateFlag()){
			if(order.getExpressAddress()==null){
//				if("Y".equals(order.getNeedInvoice())){
//					
//				}else{
//					throwIllegalException("快递商品缺失快递信息");
//				}
			}
		}
		return true;
	}

}
