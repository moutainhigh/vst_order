package com.lvmama.vst.order.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * 易宝电话支付
 * @author xiaoyulin
 *
 */
@Controller
public class OrderYeepayPhonePaymentAction extends BaseActionSupport{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 8655210072919048063L;


	private static final Logger LOG = LoggerFactory.getLogger(OrderYeepayPhonePaymentAction.class);
	
	/**
	 * 订单综合查询服务.
	 */
	@Autowired
	private IComplexQueryService complexQueryService;
	
	/**
	 * 对象类型(订单).
	 */
	private  final String objectType = "ORD_ORDER";
	
	/**
	 * 业务类型.
	 */
	private final String bizType = Constant.PAYMENT_BIZ_TYPE.VST_ORDER.name();
	
	/**
	 * 支付类型(正常支付/预授权).
	 */
	private final String paymentType = Constant.PAYMENT_OPERATE_TYPE.PAY.name();
	
	/**
	 * 支付页面地址
	 */
	private final String YEEPAY = "/order/yeepay/yeepay_phone_postdata_pay";
	
	@RequestMapping(value="/order/yeepay/yeepayPhone",method=RequestMethod.GET)
	public String createPayment(@RequestParam("orderId")Long orderId, ModelMap model) {
		// 订单对象
		OrdOrder order = null;
		// 订单金额
		String paytotal = null;
		// 客服号码
		String csno = null;
//		// 手机号
//		String mobilenumber = null;
		String signature = null;
		// 产品订金(分)
		String payDeposit="0";
		try {
			order = complexQueryService.queryOrderByOrderId(orderId);
			paytotal = String.valueOf(PriceUtil.convertToYuan(order.getOughtAmount() - order.getActualAmount()));
			csno = getLoginUser().getUserName();
			signature = String.valueOf(order.getOughtAmountYuan())+String.valueOf(order.getOrderId())+objectType+paymentType;
			float actualPayFloat = PriceUtil.convertToYuan(order.getActualAmount());
			
			model.addAttribute("orderId", orderId.toString());
			model.addAttribute("paytotal", paytotal);
			model.addAttribute("csno", csno);
			model.addAttribute("signature", signature);
			model.addAttribute("objectType", objectType);
			model.addAttribute("bizType", bizType);
			model.addAttribute("paymentType", paymentType);
			model.addAttribute("payDeposit", payDeposit);
			model.addAttribute("actualPayFloat", actualPayFloat);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		return YEEPAY;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public String getBizType() {
		return bizType;
	}
}
