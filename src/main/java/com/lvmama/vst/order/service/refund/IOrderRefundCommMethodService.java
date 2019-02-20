package com.lvmama.vst.order.service.refund;

import com.lvmama.vst.back.order.po.OrdOrder;
/**
 * @author huangxin
 * 退款单公共的方法
 */
public interface IOrderRefundCommMethodService {
	
	 /*
	  * 是否显示退款申请按钮(订单已支付（指全部支付）2.订单中无投诉退款单 满足这两个条件才能显示退款申请按钮)
	  */
	public String checkReFundButtonShow(OrdOrder order);

}
