/**
 * 
 */
package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.lvmama.vst.order.utils.OrderUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.pay.PayGlobalParamsControl;
import com.lvmama.comm.pet.service.pay.PayGlobalParamsControlService;
import com.lvmama.comm.vst.vo.OrderSandPayTypeVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * @author pengyayun
 *
 */
@Controller
public class OrderPOSPaymentAction extends BaseActionSupport {
	
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
	
	@Autowired
	private PayGlobalParamsControlService payGlobalParamsControlService;
	
	@RequestMapping(value = "/ord/order/initPosPaymentRecord")
	public String initPosPaymentRecord(Model model,Long orderId,String posType) {
		 
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		
		//订单金额.
		String paytotal="";
		if(order!=null){
			paytotal=PriceUtil.trans2YuanStr(order.getOughtAmount()-order.getActualAmount());
		}
		
		model.addAttribute("order", order);
		model.addAttribute("paytotal", paytotal);
		model.addAttribute("orderId", orderId);
		model.addAttribute("objectType", objectType);
		model.addAttribute("paymentType", paymentType);
		model.addAttribute("bizType", bizType);
		
		if(OrderEnum.POS_PAY_TYPE.COMM_POS.name().equals(posType)){
			return "/order/orderPayment/inc/init_comm_pos_pay";
		}else if(OrderEnum.POS_PAY_TYPE.SAND_POS.name().equals(posType)){
			List<OrderSandPayTypeVO> sandPosSubChannelList = new ArrayList<OrderSandPayTypeVO>();
			PayGlobalParamsControl sandPosCtrl = payGlobalParamsControlService.selectByCode("LVMAMA_SANDPOS_CHANNEL_SWITCH");
			if(null!=sandPosCtrl){
				String sandPosSubChannel = sandPosCtrl.getValue();
				String target = sandPosSubChannel.substring(1, sandPosSubChannel.length()-1);
				String[] targetArray = target.split(",");
				for(int i=0;i < targetArray.length;i++){
					OrderSandPayTypeVO orderSandPayTypeVO = new OrderSandPayTypeVO();
					String replaceStr = targetArray[i].replace("\"", "");
					orderSandPayTypeVO.setKey(replaceStr);
					orderSandPayTypeVO.setValue(OrderEnum.SAND_POS_PAY_TYPE.getCnName(replaceStr));
					sandPosSubChannelList.add(orderSandPayTypeVO);
				}
				model.addAttribute("sandPosSubChannelList", sandPosSubChannelList);
			}
			return "/order/orderPayment/inc/init_sand_pos_pay";
		}
		
		return "NOKNOW_PAGE";
	}
	
	/**
	 * 生成MD5签名
	 * @param request
	 * @param amount
	 * @param orderId
	 * @return
	 */
	@RequestMapping(value = "/ord/order/posReGenerateSignature")
	@ResponseBody
	public Object reGenerateSignature(HttpServletRequest request,String amount,Long orderId){
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		
		 try {
			    if(null==orderId){
			    	throw new IllegalArgumentException("订单Id不能为空");
			    }
			    if(StringUtils.isEmpty(amount)){
			    	throw new IllegalArgumentException("支付金额不能为空");
			    }
			 	Long amountFen=PriceUtil.convertToFen(amount);
			 	Map<String, String> dataStr = new HashMap<String, String>();
			    dataStr.put("orderId", String.valueOf(orderId));
			    dataStr.put("objectType", objectType);
			    dataStr.put("payAmountFen", String.valueOf(amountFen));
			    dataStr.put("paymentType", paymentType);
			    dataStr.put("bizType", bizType);

			 	String signature = OrderUtils.signature(dataStr);

			 	attributes.put("newSignature",signature);
			 	attributes.put("amount",String.valueOf(amountFen));
				msg.setAttributes(attributes);
				msg.setCode("success");
				msg.setMessage("RSA签名成功");
		} catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("RSA签名失败:"+e.getMessage());
			log.error(e);
		}
		 
		return msg;
	}
}
