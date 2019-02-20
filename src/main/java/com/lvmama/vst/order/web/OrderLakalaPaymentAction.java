/**
 * 
 */
package com.lvmama.vst.order.web;



import java.util.HashMap;
import java.util.Map;

import com.lvmama.vst.order.utils.OrderUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pay.vo.PaymentConstant;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.MD5;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * 拉卡拉支付
 * @author pengyayun
 *
 */
@Controller
public class OrderLakalaPaymentAction extends BaseActionSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9186650132334237697L;
	
	/**
	 * 对象类型(订单).
	 */
	private  final String objectType = OrderEnum.PAYMENT_OBJECT_TYPE.ORD_ORDER.name();
	/**
	 * 业务类型.
	 */
	private final String bizType = Constant.PAYMENT_BIZ_TYPE.VST_ORDER.name();
	
	/**
	 * 支付类型(正常支付/预授权).
	 */
	private final String paymentType = Constant.PAYMENT_OPERATE_TYPE.PAY.name();
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@RequestMapping(value = "/ord/order/createLakalaPaymentUrl.do")
	@ResponseBody
	public Object createLakalaPaymentUrl(Model model,Long orderId) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		
		 try {
			    if(null==orderId){
			    	throw new IllegalArgumentException("订单Id不能为空");
			    }
			    
			    OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
				
				//订单金额.
				String paytotal="";
				if(order!=null){
					paytotal=String.valueOf(order.getOughtAmount()-order.getActualAmount());
				}
			 	Map<String, String> dataStr = new HashMap<String, String>();
			 	dataStr.put("orderId", String.valueOf(orderId));
			 	dataStr.put("objectType", objectType);
			 	dataStr.put("payAmountFen", String.valueOf(paytotal));
			 	dataStr.put("paymentType", paymentType);
			 	dataStr.put("bizType", bizType);
			 	String signature = OrderUtils.signature(dataStr);
				
				String params = "objectId=" + orderId + "&objectType=" + objectType + "&amount=" + paytotal + "&paymentType=" + paymentType + "&bizType=" + bizType + "&signature=" + signature;
				
			 	attributes.put("params",params);
				msg.setAttributes(attributes);
				msg.setCode("success");
		} catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("支付url生成异常:"+e.getMessage());
			log.error(e);
		}
		 
		return msg;
	}
}
