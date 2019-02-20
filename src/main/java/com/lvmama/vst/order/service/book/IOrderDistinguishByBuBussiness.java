package com.lvmama.vst.order.service.book;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
/**
 * @author 
 * 为BU单独写的服务类，现包含功能:
 * 1:获取支付等待时间默认时长.
 * 2:获得所属BU
 */
public interface IOrderDistinguishByBuBussiness {
	/**
	 * @param categoryCode
	 * @param prodProduct
	 * @return 获得所属BU<实际上为所属Bu下限制的>
	 * 附：为支付等待时长写的方法,在获得所属BU时，加了另外条件的限制
	 */
	String getBuType(String categoryCode,Long productId,OrdOrder ordOrder);

	/**
	 * @param buName
	 * @param order
	 * @return 是否符合该BU的计算规则
	 */
	boolean isConformBuRule(String currentBuName,String targetBuName,OrdOrder order);
	

	/**
	 * @param defaultWaitPaymentMinute: 默认的支付等待时长
	 * @param ordOrderItem :子订单信息
	 * @param prodProduct : 产品类型
	 * @return
	 */
	int getOrderDefaultWaitPaymentTimeMinuteByBu(int defaultWaitPaymentMinute,String buType);

	/**
	 * @param defaultWaitPaymentMinute
	 * @param ordOrderItem
	 * @return
	 */
	int getHotelDefaultWaitPaymentTimeMinuteByBu(int defaultWaitPaymentMinute,OrdOrderItem ordOrderItem);
}