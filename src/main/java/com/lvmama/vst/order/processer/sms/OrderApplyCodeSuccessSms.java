package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
/**
 * 申码成功 (暴露远程供调用，而非jms消息触发调用)
 * @author zhaomingzhu
 *
 */
public class OrderApplyCodeSuccessSms implements AbstractSms {
	
	private final static Log logger=LogFactory.getLog(OrderApplyCodeSuccessSms.class);
	
	//支付对象(现付)
	public boolean isPay(OrdOrder order){
		if(order.hasNeedPay()){
			return true;
		}else{
			return false;
		}
	}
	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	//订单提交 - 审核通过
	public boolean hasInfoAndResourcePass(OrdOrder order){
		if(order.hasInfoAndResourcePass()){
			return true;
		}else{
			return false;
		}
	}
	//门票 并且 期票并且二维码(订单提交)
	public boolean isTicketAndQrCode(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_show_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_local_play)
					){
					if(item.hasTicketAperiodic()){
						if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
								SuppGoods.NOTICETYPE.QRCODE.name())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//门票 并且 非期票并且二维码(订单提交)
	public boolean isNoTicketAndQrCode(OrdOrder order){
		for(OrdOrderItem item : order.getOrderItemList()){
			if(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_show_ticket)
					||item.hasCategory(BIZ_CATEGORY_TYPE.category_local_play)
					){
					if(!item.hasTicketAperiodic()){
						if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
								SuppGoods.NOTICETYPE.QRCODE.name())){
						return true;
					}
				}
			}
		}
		return false;
	}		
	//供应商 (北京春秋永乐文化传播有限公司)的二维码门票  or 供应商 (广州银旅通国际旅行社有限公司)的二维码门票
	public boolean hasSupplier(OrdOrder order,String supplierId){
		int num = 0;//门票item个数
		int count = 0;//二维码门票item个数
		if(supplierId == null){
			return false;
		}
		for(OrdOrderItem item : order.getOrderItemList()){
			if(supplierId != null && supplierId.equalsIgnoreCase(String.valueOf(item.getSupplierId())) 
					&&(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket))){
				num ++;
				if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
						SuppGoods.NOTICETYPE.QRCODE.name())){
					count++;
				}
			}
		}
		if(count >0 && count < num){
			return true;
		}
		return false;
	}	
	//有且只有供应商 (北京春秋永乐文化传播有限公司)的二维码门票  or 有且只有供应商 (广州银旅通国际旅行社有限公司)的二维码门票
	public boolean hasOnlySupplier(OrdOrder order,String supplierId){
		int total = 0;//总的商品个数
		int num = 0;//门票item个数
		int count = 0;//二维码门票item个数
		if(supplierId == null){
			return false;
		}
		for(OrdOrderItem item : order.getOrderItemList()){
			total++;
			if(supplierId != null && supplierId.equalsIgnoreCase(String.valueOf(item.getSupplierId())) 
					&&(item.hasCategory(BIZ_CATEGORY_TYPE.category_single_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_other_ticket)
							||item.hasCategory(BIZ_CATEGORY_TYPE.category_comb_ticket))){
				num++;
				if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), 
						SuppGoods.NOTICETYPE.QRCODE.name())){
					count++;
				}
			}
		}
		if(total == num && total == count){
			return true;
		}
		return false;
	}	
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderApplyCodeSuccessSms ===>>> hasInfoAndResourcePass(order)=" + hasInfoAndResourcePass(order)
					+ "isTicketAndQrCode(order)=" + isTicketAndQrCode(order)
					+ "isNoTicketAndQrCode(order)=" + isNoTicketAndQrCode(order)
					+ "isPay(order)=" + isPay(order)
					+ "isPrepaid(order)=" + isPrepaid(order)
					+ "hasSupplier(order, bj.supplierId)=" + hasSupplier(order, Constant.getInstance().getProperty("bj.supplierId"))
					+ "hasSupplier(order, gz.supplierId)=" + hasSupplier(order, Constant.getInstance().getProperty("gz.supplierId"))
					+ "hasOnlySupplier(order, bj.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId"))
					+ "hasOnlySupplier(order, gz.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId"))
					+"orderidexeSmsRule="+order.getOrderId()
				);	
		
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		if(hasInfoAndResourcePass(order)){
			//1.申码成功+[主订单]已审核+期票+二维码 	(申码成功(订单提交)+[主订单]已审核+期票+二维码+门票+现付)
			if(isTicketAndQrCode(order) && isPay(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name());
			}
			//2.订单提交+[主订单]已审核+非期票+二维码 	(申码成功(订单提交)+[主订单]已审核+非期票+二维码+门票+现付)
			if(isNoTicketAndQrCode(order) && isPay(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name());
			}
//		//3.[主订单]已审核+期票+二维码			(申码成功(已审核)[主订单]已审核+期票+二维码+门票+现付)
//		if(isTicketAndQrCode(order) && isPay(order)){
//			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE.name());
//		}
//		//4.[主订单]已审核+非期票+二维码			(申码成功(已审核)[主订单]已审核+非期票+二维码+门票+现付)
//		if(isNoTicketAndQrCode(order) && isPay(order)){
//			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE.name());
//		}
			//5.[主订单]支付完成+期票+二维码			(申码成功(支付完成)[主订单]支付完成+期票+二维码+门票+预付)
			if(isTicketAndQrCode(order) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name());
			}
			//6.[主订单]支付完成+非期票+二维码		(申码成功(支付完成)[主订单]支付完成+非期票+二维码+门票+预付)
			if(isNoTicketAndQrCode(order) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name());
			}
			//7.[主订单]支付完成+供应商（北京春秋永乐文化传播有限公司）+（商品类型=电子凭证二维码）+申码成功+门票+预付
			if(hasSupplier(order, Constant.getInstance().getProperty("bj.supplierId")) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name());
			}
			//8.[主订单]支付完成+供应商（广州银旅通国际旅行社有限公司）+（商品类型=电子凭证二维码）+申码成功+门票+预付
			if(hasSupplier(order, Constant.getInstance().getProperty("gz.supplierId")) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name());
			}
			//9.当订单有且仅有供应商（北京春秋永乐文化传播有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
			if(hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId")) && isPrepaid(order)){
				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name());
			}
			//10.当订单有且仅有供应商（广州银旅通国际旅行社有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
			if(hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId")) && isPrepaid(order)){
				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name());
			}
		}
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		return sendList;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}
	
	public List<String> exeSmsRule(OrdOrder order,String smsSender) {
		logger.info("OrderApplyCodeSuccessSms ===>>> hasInfoAndResourcePass(order)=" + hasInfoAndResourcePass(order)
					+ "isTicketAndQrCode(order)=" + isTicketAndQrCode(order)
					+ "isNoTicketAndQrCode(order)=" + isNoTicketAndQrCode(order)
					+ "isPay(order)=" + isPay(order)
					+ "isPrepaid(order)=" + isPrepaid(order)
					+ "hasSupplier(order, bj.supplierId)=" + hasSupplier(order, Constant.getInstance().getProperty("bj.supplierId"))
					+ "hasSupplier(order, gz.supplierId)=" + hasSupplier(order, Constant.getInstance().getProperty("gz.supplierId"))
					+ "hasOnlySupplier(order, bj.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId"))
					+ "hasOnlySupplier(order, gz.supplierId)=" + hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId"))
				);	
		
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		if(hasInfoAndResourcePass(order)){
			//1.申码成功+[主订单]已审核+期票+二维码 	(申码成功(订单提交)+[主订单]已审核+期票+二维码+门票+现付)
			if(isTicketAndQrCode(order) && isPay(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name());
			}
			//2.订单提交+[主订单]已审核+非期票+二维码 	(申码成功(订单提交)+[主订单]已审核+非期票+二维码+门票+现付)
			if(isNoTicketAndQrCode(order) && isPay(order)){
				if("LVMAMA".equalsIgnoreCase(smsSender)){
					sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name());
				}
				if("PARTNER".equalsIgnoreCase(smsSender)){
					sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name());
				}
			}
//		//3.[主订单]已审核+期票+二维码			(申码成功(已审核)[主订单]已审核+期票+二维码+门票+现付)
//		if(isTicketAndQrCode(order) && isPay(order)){
//			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_APERIODIC_QRCODE.name());
//		}
//		//4.[主订单]已审核+非期票+二维码			(申码成功(已审核)[主订单]已审核+非期票+二维码+门票+现付)
//		if(isNoTicketAndQrCode(order) && isPay(order)){
//			sendList.add(OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNAPERIODIC_QRCODE.name());
//		}
			//5.[主订单]支付完成+期票+二维码			(申码成功(支付完成)[主订单]支付完成+期票+二维码+门票+预付)
			if(isTicketAndQrCode(order) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name());
			}
			//6.[主订单]支付完成+非期票+二维码		(申码成功(支付完成)[主订单]支付完成+非期票+二维码+门票+预付)
			if (isNoTicketAndQrCode(order) && isPrepaid(order)) {
				if ("PARTNER".equalsIgnoreCase(smsSender)) {// 服务商发码
					sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE_PROVIDER
							.name());
				}
				if ("LVMAMA".equalsIgnoreCase(smsSender)) {// 驴妈妈发码
					sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE
							.name());
				}
			}
			if(isPrepaid(order) && Long.valueOf(41l).equals(order.getCategoryId()) && "PAYED".equals(order.getPaymentStatus())){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_CONNECTS.name());
			}
			//7.[主订单]支付完成+供应商（北京春秋永乐文化传播有限公司）+（商品类型=电子凭证二维码）+申码成功+门票+预付
			if(hasSupplier(order, Constant.getInstance().getProperty("bj.supplierId")) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name());
			}
			//8.[主订单]支付完成+供应商（广州银旅通国际旅行社有限公司）+（商品类型=电子凭证二维码）+申码成功+门票+预付
			if(hasSupplier(order, Constant.getInstance().getProperty("gz.supplierId")) && isPrepaid(order)){
				sendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name());
			}
			//9.当订单有且仅有供应商（北京春秋永乐文化传播有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
			if(hasOnlySupplier(order, Constant.getInstance().getProperty("bj.supplierId")) && isPrepaid(order)){
				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_APERIODIC_QRCODE.name());
			}
			//10.当订单有且仅有供应商（广州银旅通国际旅行社有限公司）的（商品类型=电子凭证二维码）门票，则不发送，[主订单]支付完成 + 预付
			if(hasOnlySupplier(order, Constant.getInstance().getProperty("gz.supplierId")) && isPrepaid(order)){
				noneSendList.add(OrdSmsTemplate.SEND_NODE.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name());
			}
		}
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		return sendList;
	}
	
}
