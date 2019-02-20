package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.eventbus;

import com.google.common.eventbus.EventBus;
import com.lvmama.vst.neworder.order.cancel.category.hotelcomb.handler.CancelForStockProcessor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/2/21.
 * 处理订单取消后 异步相关操作逻辑
 */
@Component
public class OrderCancelEventBus extends EventBus  implements InitializingBean{

	@Resource
	CancelForStockProcessor returnStockProcessor ;


    
	@Override
    public void afterPropertiesSet() throws Exception {
		  this.register(returnStockProcessor);
    }
}
