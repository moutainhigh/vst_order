package com.lvmama.vst.order.service.refund;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * 在线退款公共服务
 * @version 1.0
 */
@Service("orderRefundComService")
public class OrderRefundComService {
	/*退款服务MAP*/
	private static Map<ORDER_REFUND_SERVICE_TYPE_KEY, String> refundServiceMap =new HashMap<ORDER_REFUND_SERVICE_TYPE_KEY, String>();
	@Resource(name="orderRefundFactoryMap")
	private Map<String,Object> orderRefundFactoryMap;
	
	static{
		refundServiceMap.put(ORDER_REFUND_SERVICE_TYPE_KEY.AMOUNT
				, "order_refund_amount");//退款金额
		refundServiceMap.put(ORDER_REFUND_SERVICE_TYPE_KEY.PROCESS
				, "order_refund_process");//退款流程
		refundServiceMap.put(ORDER_REFUND_SERVICE_TYPE_KEY.FRONT
				, "order_refund_front");//我的工作台
	}
	
	/**
	 * 实例化退款服务
	 * @param ordOrder
	 * @param serviceKey
	 */
	public Object newInstall(OrdOrder ordOrder, ORDER_REFUND_SERVICE_TYPE_KEY serviceKey){
		Long categoryId =ordOrder.getCategoryId();
		 if(BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(ordOrder.getSubCategoryId())){
			categoryId =ordOrder.getSubCategoryId();
		}
		String key =refundServiceMap.get(serviceKey) +"_" +categoryId;
		if(!orderRefundFactoryMap.containsKey(key)){
			throw new RuntimeException("newInstall error serviceKey=" +serviceKey
					+",key="+key
					+",orderId=" +ordOrder.getOrderId());
		}
		return orderRefundFactoryMap.get(key);
	}
	
	/**
	 * 退款服务类型
	 */
	public static enum ORDER_REFUND_SERVICE_TYPE_KEY{
		AMOUNT,PROCESS,FRONT
	}
	
}
