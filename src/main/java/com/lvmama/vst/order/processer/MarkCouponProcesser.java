/**
 * 
 */
package com.lvmama.vst.order.processer;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jszhangwei
 *
 */
public class MarkCouponProcesser implements MessageProcesser{

	@Autowired
	private FavorServiceAdapter favorServiceAdapter;
	
	@Autowired
	private IOrderUpdateService	orderUpdateService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MarkCouponProcesser.class);
	
	@Override
	public void process(Message message) {
		if(MessageUtils.isOrderCancelMsg(message)){
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(message.getObjectId());
			LOGGER.info("order id:{},prepaid",new Object[]{order.getOrderId(),order.hasNeedPrepaid()});
			if(order.hasNeedPrepaid()){
				favorServiceAdapter.updateFavorAfterCancelOrder(order);
			}
		}
	}

}
