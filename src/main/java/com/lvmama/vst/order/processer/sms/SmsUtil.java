package com.lvmama.vst.order.processer.sms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.api.ticket.prod.vo.SuppGoodsRefundVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBus;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.order.service.ISupplierOrderHandleService;
import com.lvmama.vst.order.utils.RestClient;

import net.sf.json.JSONObject;

public class SmsUtil {
	private static final String RIGHT_BRACKET = "）";
	private static final String UNTIL = "至";
	private static final String FLY_UNTIL = "起飞";
	private static final String SEMI = "；";
	private static final String COMMA = "，";
	private static final String COMMAND = "、";
	private static final String LEFT_BRACKET = "（";
	private static final String SPACE = " ";//中文空格
	private static final String LINE = "-";
	private static final String PERIOD="。";
	private static final String  SMSLVMAMAFLAG = "SmsLvmamaFlag";
	private static final String  N = "N";
	public static final String REPLACEAPI = "下载APP订1元门票，9元周边游，享更多优惠http://t.cn/RLm0dfw。";
	public static final String[] DISTRIBUTOR_CODE_ARRAY = {"DISTRIBUTOR_API","DISTRIBUTOR_B2B","DISTRIBUTOR_DAOMA","DISTRIBUTOR_YUYUE","DISTRIBUTOR_TAOBAO","DISTRIBUTOR_SG"};
	public static final String ADVANCE_PAY_SMS_TEMP="驴妈妈会根据您的付款方式进行订单全款扣除，如订单不确认将全额退款至您的支付账户。 订单是否生效以驴妈妈最终通知为准。";
	public static final String ADVANCE_PAY_SMS_TEMP_TIMEOUT_CECANEL="您的订单号${orderId} 已过支付等待时间，已被自动取消。如需帮助请致电客服电话：${clientSign}【(免长话费)】";
	public static final String PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE_LVMAMA="您的订单号${orderId}已生成，出游日期${visitDate}。${productInfo}。请于起飞当日携带相关登机证件，提前120分钟到达机场办理登机手续！客服电话：${customerServicePhone}(免长话费)。【${clientSign}】";
	public static final String PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE_SUPPLIER="您的订单号${orderId}已生成，出游日期${visitDate}。预定产品：${productName}，客服电话：${customerServicePhone}(免长话费)。【${clientSign}】";
	public static final String YONGLE_SHOW_TICKET_IN = "，本演出为实体票入场，请在入场前取票入场（或注意快递查收实体票） ";
	public static final String YONGLE_SHOW_TICKET_TEL = "(免长话费)或直接咨询服务委托方永乐票务：4006-228-228 ";
	private final static Log LOG=LogFactory.getLog(SmsUtil.class);
	/**
	 * 特卖会渠道代码
	 */
	private final static String DISTRIBUTOR_TEMAI_CODE="TEMAI";
	/**
	 * 驴途渠道代码
	 */
	private final static String DISTRIBUTOR_LVTU_CODE="LVTU";
	/**
	 * 分销商ID
	 */
	private final static Long DISTRIBUTOR = 4l;
	
	@Autowired
	private static ISupplierOrderHandleService supplierOrderHandleService;
	
	/**
	 * 订单客服电话
	 * @param order
	 * @param buyInfoAddition
	 * @return
	 */
	private static String getFormInfo(OrdOrder order, BuyInfoAddition buyInfoAddition){
//		if(!CollectionUtils.isEmpty(order.getFormInfoList())){
//			for (OrdFormInfo ordFormInfo : order.getFormInfoList())
//			{
//				LOG.info("contentType is "+ordFormInfo.getContentType()+"& content is "+ordFormInfo.getContent());
//			}
//		}
		OrdFormInfo ordFormInfo = order.getFormInfo(buyInfoAddition.name());
		if(ordFormInfo != null && ordFormInfo.getContent() != null){
			return ordFormInfo.getContent();
		}
		return null;
	}

	/**
	 * 门店系统短信内容调整
	 * @return
	 */
	private static String getSmsContentForO2O(String content){
		String[] reg = {"如需帮助请联系客服电话[:|：]?\\$\\{(.+?)\\}[\\(|（]免长话费[\\)|）][.|。]?",
						"如需帮助可致电驴妈妈客服[:|：]?\\$\\{(.+?)\\}[\\(|（]免长话费[\\)|）][.|。]?",
						"如需帮助请致电客服电话[:|：]?\\$\\{(.+?)\\}[\\(|（]免长话费[\\)|）][.|。]?",
						"如需帮助请致电[:|：]?\\$\\{(.+?)\\}[\\(|（]免长话费[\\)|）][.|。]?",
						"客服电话[:|：]?\\$\\{(.+?)\\}[\\(|（]免长话费[\\)|）][.|。]?"};
		for(int i = 0; i < 5; i++){
			content = content.replaceAll(reg[i], "");
		}
		return content;
	}

	/**
	 * 简单填充 (订单取消、订单退款、催支付、预授权成功(支付完成)、部分订单提交)
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSms(String content, OrdOrder order, Map<String,Object> param) {
		if(Constants.DISTRIBUTOR_10.longValue() == order.getDistributorId().longValue()){
			content = getSmsContentForO2O(content);
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		String regex = "\\$\\{(.+?)\\}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		String waitPaymentTime=DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm");
		while (matcher.find()) {
			String keyField = matcher.group(1);
			//订单编号orderId
			if(OrdSmsTemplate.FIELD.ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField, order.getOrderId());
			}
			//最后支付时间
			if(OrdSmsTemplate.FIELD.WAIT_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, waitPaymentTime);
			}
			
			double oughtAmount=order.getOughtAmount() == null ? 0 : order.getOughtAmount(); //订单金额
			double actualAmount=order.getActualAmount() == null ? 0 : order.getActualAmount(); //已支付金额
			//订单金额
			if(OrdSmsTemplate.FIELD.ORDER_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField,oughtAmount/100);
			}
			//尾款金额
			if(OrdSmsTemplate.FIELD.RESIDUAL_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, (oughtAmount - actualAmount)/100);
			}
			// 入园方式
			if(OrdSmsTemplate.FIELD.ENTER_STYLE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.ENTER_STYLE.getField()));
			}
			//客服电话customerServicePhone
			if(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField().equals(keyField)){
				//定制游客服电话
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(order.getCategoryId())){
					dataMap.put(keyField, Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
				}else{
					if(getFormInfo(order, BuyInfoAddition.customerServiceTel) != null){
						dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.customerServiceTel));
						LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "供应商客服电话=" + getFormInfo(order, BuyInfoAddition.customerServiceTel));
					}else{
						dataMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE);
						LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMER_SERVICE_PHONE);
					}
				}
			}
			// 境外客服电话
			if(OrdSmsTemplate.FIELD.OUTBOUND_SERVICE_PHONE.getField().equals(keyField)) {
				dataMap.put(keyField, Constant.OUTBOUND_SERVICE_PHONE);
			}
			//客户端签名clientSign
			if(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField().equals(keyField)){
				if(getFormInfo(order, BuyInfoAddition.distributionSuffix) != null){
					//分销商的短信。需要把最后末尾的签名去掉
					if(N.equalsIgnoreCase(order.getSmsLvmamaFlag())){
						dataMap.put(keyField, "");
					}else{
						dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.distributionSuffix));
					}
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "供应商客服签名=" + getFormInfo(order, BuyInfoAddition.distributionSuffix));
				}else{
					if(N.equalsIgnoreCase(order.getSmsLvmamaFlag())){
						dataMap.put(keyField, "");
					}else{
						dataMap.put(keyField, Constant.LVMAMA);
					}
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服签名=" + Constant.LVMAMA);
				}
			}
			/* ****************************催支付start**************************** */
			//最晚支付时间latestPaymentTime
			if(OrdSmsTemplate.FIELD.LATEST_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			/* ****************************催支付end**************************** */


			/* ****************************二维码start**************************** */
			//二维码qrcode
			if(OrdSmsTemplate.FIELD.QRCODE.getField().equals(keyField)){
			}

			//辅助码auxiliaryCode
			if(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField()));
			}

			//有效期validBeginTime~validEndTime
			if(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField()));
			}
			if(OrdSmsTemplate.FIELD.VALID_END_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_END_TIME.getField()));
			}

			//不使用日期invalidTime
			if(OrdSmsTemplate.FIELD.INVALID_TIME.getField().equals(keyField)){
				if(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()) == null && "".equals(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()))){
					dataMap.put(keyField, "无");
				}else{
					dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()));
				}
			}
			//游玩时间visitTime
			if(OrdSmsTemplate.FIELD.VISIT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField()));
			}
			//离店时间endTime
			if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField()));
			}
			//证件号certificateNo
			if(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField()));
			}
			//张数ticketSheets
			if(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField()));
			}
			//产品名称productName
			if(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()));
			}
			//商品名称suppGoodsName
			if(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField()));
			}
			//入园地址gardenAddress
			if(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField()));
			}
			/* ****************************二维码end**************************** */

			/* ****************************订单提交start**************************** */
			//产品信息productInfo
			if(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField()));
			}
			//总价oughtAmount
			if(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField()));
			}
			//数量QUANTITY
			if(OrdSmsTemplate.FIELD.QUANTITY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.QUANTITY.getField()));
			}
			//离店时间departureDate
			if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField()));
			}
			//退改信息
			if(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField()));
			}
			//酒店地址departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField()));
			}
			//酒店电话departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField()));
			}
			//最晚保留时间latestUnguarTime
			if(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField()));
			}
			//Added by yangzhenzhong  最晚无损取消时间
			if (OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField()));
			}
			//end
			
			//Add by xiachengliang 预定人姓名和酒店预定号
			if (OrdSmsTemplate.FIELD.CONTACT_NAME.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.CONTACT_NAME.getField()));
			}
			if (OrdSmsTemplate.FIELD.SUPP_ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.SUPP_ORDER_ID.getField()));
			}
			//end
			/* ****************************订单提交end**************************** */
			
			/* ****************************预售start**************************** */
			//券信息
			if (OrdSmsTemplate.FIELD.STAMP_CODES.getField().equals(keyField)) {
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.STAMP_CODES.getField()));
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + ",key=" + keyField + ",stamp codes=" + param.get(keyField));
			}
			if(OrdSmsTemplate.FIELD.SUPPLIER_ORDER_EXPPESS_INFO.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.SUPPLIER_ORDER_EXPPESS_INFO));
			}
			/* ****************************预售end**************************** */

			
			/* ****************************在线退款start 2016年11月7日14:52:44 yanghaifeng**************************** */
			if (OrdSmsTemplate.FIELD.REFUND_AMOUNT.getField().equals(keyField)) {
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.REFUND_AMOUNT.getField()));
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + ",key=" + keyField + ",refument amount=" + param.get(keyField));
			}
			/* ****************************在线退款end**************************** */

			
			/*意外险后置，补充游玩人，提醒时间*/
			if (OrdSmsTemplate.FIELD.HALF_WAIT_SUPPLY_TRAV.getField().equals(keyField)) {
                dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.HALF_WAIT_SUPPLY_TRAV.getField()));//halfWaitSupplyTrav
                LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + ",key=" + keyField + ",stamp codes=" + param.get(keyField));
            }
			
			/*康旅卡支付提醒*/
			if (OrdSmsTemplate.FIELD.KL_TYPE.getField().equals(keyField)) {
			    dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.KL_TYPE.getField()));
			    LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + ",key=" + keyField + ",stamp codes=" + param.get(keyField));
            }

		}
		if(MapUtils.isNotEmpty(param)){
			dataMap.putAll(param);
		}
		dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), assembleSms(order, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField())));

		// FIXME 预售订单未生成，无法取到
//		fillStampProductInfo(order, dataMap);

		String result = StringUtil.composeMessage(content, dataMap);
		
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			int i =result.lastIndexOf("。");
			String strs=result.substring(i+1);
			String strs2=result.substring(0, i);
			result = strs2 + "，境外请拨打："+Constant.OUTBOUND_SERVICE_PHONE+"。"+strs;
		}else{
			String isToForeign = (String)param.get("isToForeign");
			if("Y".equals(isToForeign)){
				int t =result.lastIndexOf("。");
				String str1=result.substring(t+1);
				String str2=result.substring(0, t);
				result = str2 + "，境外请拨打："+Constant.OUTBOUND_SERVICE_PHONE+"。"+str1;
			}
		}
		
		LOG.info("fillSms: " + content + ", dateMap=" + JSONUtil.bean2Json(dataMap) + ", result=" + result);
		return result;
	}
	
	/**
	 * 简单填充 (订单取消、订单退款、催支付、预授权成功(支付完成)、部分订单提交)--永乐演出票
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSmsYL(String content, OrdOrder order, Map<String,Object> param) {
		LOG.info("SmsUtil.fillSmsYL:orderId="+ order.getOrderId());
		if(Constants.DISTRIBUTOR_10.longValue() == order.getDistributorId().longValue()){
			content = getSmsContentForO2O(content);
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		String regex = "\\$\\{(.+?)\\}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		String waitPaymentTime=DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm");
		while (matcher.find()) {
			String keyField = matcher.group(1);
			//订单编号orderId
			if(OrdSmsTemplate.FIELD.ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField, order.getOrderId());
			}
			//最后支付时间
			if(OrdSmsTemplate.FIELD.WAIT_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, waitPaymentTime);
			}
			
			double oughtAmount=order.getOughtAmount() == null ? 0 : order.getOughtAmount(); //订单金额
			double actualAmount=order.getActualAmount() == null ? 0 : order.getActualAmount(); //已支付金额
			//订单金额
			if(OrdSmsTemplate.FIELD.ORDER_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField,oughtAmount/100);
			}
			//尾款金额
			if(OrdSmsTemplate.FIELD.RESIDUAL_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, (oughtAmount - actualAmount)/100);
			}
			// 入园方式
			if(OrdSmsTemplate.FIELD.ENTER_STYLE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.ENTER_STYLE.getField()));
			}
			//客服电话customerServicePhone
			if(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField().equals(keyField)){
				//定制游客服电话
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(order.getCategoryId())){
					dataMap.put(keyField, Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
					LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
				}else{
					if(getFormInfo(order, BuyInfoAddition.customerServiceTel) != null){
						dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.customerServiceTel));
						LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + "供应商客服电话=" + getFormInfo(order, BuyInfoAddition.customerServiceTel));
					}else{
						dataMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE + YONGLE_SHOW_TICKET_TEL);
						LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMER_SERVICE_PHONE+YONGLE_SHOW_TICKET_TEL);
					}
				}
			}
			// 境外客服电话
			if(OrdSmsTemplate.FIELD.OUTBOUND_SERVICE_PHONE.getField().equals(keyField)) {
				dataMap.put(keyField, Constant.OUTBOUND_SERVICE_PHONE);
			}
			//客户端签名clientSign
			if(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField().equals(keyField)){
				if(getFormInfo(order, BuyInfoAddition.distributionSuffix) != null){
					//分销商的短信。需要把最后末尾的签名去掉
					if(N.equalsIgnoreCase(order.getSmsLvmamaFlag())){
						dataMap.put(keyField, "");
					}else{
						dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.distributionSuffix));
					}
					LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + "供应商客服签名=" + getFormInfo(order, BuyInfoAddition.distributionSuffix));
				}else{
					if(N.equalsIgnoreCase(order.getSmsLvmamaFlag())){
						dataMap.put(keyField, "");
					}else{
						dataMap.put(keyField, Constant.LVMAMA);
					}
					LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + "lvmama客服签名=" + Constant.LVMAMA);
				}
			}
			/* ****************************催支付start**************************** */
			//最晚支付时间latestPaymentTime
			if(OrdSmsTemplate.FIELD.LATEST_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			/* ****************************催支付end**************************** */


			/* ****************************二维码start**************************** */
			//二维码qrcode
			if(OrdSmsTemplate.FIELD.QRCODE.getField().equals(keyField)){
			}

			//辅助码auxiliaryCode
			if(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField()));
			}

			//有效期validBeginTime~validEndTime
			if(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField()));
			}
			if(OrdSmsTemplate.FIELD.VALID_END_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_END_TIME.getField()));
			}

			//不使用日期invalidTime
			if(OrdSmsTemplate.FIELD.INVALID_TIME.getField().equals(keyField)){
				if(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()) == null && "".equals(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()))){
					dataMap.put(keyField, "无");
				}else{
					dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()));
				}
			}
			//游玩时间visitTime
			if(OrdSmsTemplate.FIELD.VISIT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField()));
			}
			//离店时间endTime
			if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField()));
			}
			//证件号certificateNo
			if(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField()));
			}
			//张数ticketSheets
			if(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField()));
			}
			//产品名称productName
			if(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()));
			}
			//商品名称suppGoodsName
			if(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField()));
			}
			//入园地址gardenAddress
			if(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField()));
			}
			/* ****************************二维码end**************************** */

			/* ****************************订单提交start**************************** */
			//产品信息productInfo
			if(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField()));
			}
			//总价oughtAmount
			if(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField()));
			}
			//数量QUANTITY
			if(OrdSmsTemplate.FIELD.QUANTITY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.QUANTITY.getField()));
			}
			//离店时间departureDate
			if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField()));
			}
			//退改信息
			if(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField()));
			}
			//酒店地址departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField()));
			}
			//酒店电话departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField()));
			}
			//最晚保留时间latestUnguarTime
			if(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField()));
			}
			//Added by yangzhenzhong  最晚无损取消时间
			if (OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField()));
			}
			//end
			
			//Add by xiachengliang 预定人姓名和酒店预定号
			if (OrdSmsTemplate.FIELD.CONTACT_NAME.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.CONTACT_NAME.getField()));
			}
			if (OrdSmsTemplate.FIELD.SUPP_ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.SUPP_ORDER_ID.getField()));
			}
			//end
			/* ****************************订单提交end**************************** */
			
			/* ****************************预售start**************************** */
			//券信息
			if (OrdSmsTemplate.FIELD.STAMP_CODES.getField().equals(keyField)) {
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.STAMP_CODES.getField()));
				LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + ",key=" + keyField + ",stamp codes=" + param.get(keyField));
			}
			if(OrdSmsTemplate.FIELD.SUPPLIER_ORDER_EXPPESS_INFO.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.SUPPLIER_ORDER_EXPPESS_INFO));
			}
			/* ****************************预售end**************************** */

			
			/* ****************************在线退款start 2016年11月7日14:52:44 yanghaifeng**************************** */
			if (OrdSmsTemplate.FIELD.REFUND_AMOUNT.getField().equals(keyField)) {
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.REFUND_AMOUNT.getField()));
				LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + ",key=" + keyField + ",refument amount=" + param.get(keyField));
			}
			/* ****************************在线退款end**************************** */

			
			/*意外险后置，补充游玩人，提醒时间*/
			if (OrdSmsTemplate.FIELD.HALF_WAIT_SUPPLY_TRAV.getField().equals(keyField)) {
                dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.HALF_WAIT_SUPPLY_TRAV.getField()));//halfWaitSupplyTrav
                LOG.info("SmsUtil.fillSmsYL:orderId=" + order.getOrderId() + ",key=" + keyField + ",stamp codes=" + param.get(keyField));
            }
			

		}
		if(MapUtils.isNotEmpty(param)){
			dataMap.putAll(param);
		}
		dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), assembleSms(order, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField())));

		// FIXME 预售订单未生成，无法取到
//		fillStampProductInfo(order, dataMap);

		String result = StringUtil.composeMessage(content, dataMap);
		result = result.replaceFirst("\\(免长话费\\)", "20170905");
		result = result.replaceFirst("\\(免长话费\\)", "");
		result = result.replaceFirst("20170905", "\\(免长话费\\)");
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			int i =result.lastIndexOf("。");
			String strs=result.substring(i+1);
			String strs2=result.substring(0, i);
			result = strs2 + "，境外请拨打："+Constant.OUTBOUND_SERVICE_PHONE+"。"+strs;
		}else{
			String isToForeign = (String)param.get("isToForeign");
			if("Y".equals(isToForeign)){
				int t =result.lastIndexOf("。");
				String str1=result.substring(t+1);
				String str2=result.substring(0, t);
				result = str2 + "，境外请拨打："+Constant.OUTBOUND_SERVICE_PHONE+"。"+str1;
			}
		}
		
		LOG.info("fillSms: " + content + ", dateMap=" + JSONUtil.bean2Json(dataMap) + ", result=" + result);
		return result;
	}

	/**
	 * 加载预售券信息
	 */
	private static void fillStampProductInfo(OrdOrder order, Map<String, Object> dataMap) {
		if (order.getCategoryId().intValue() != 99) {
			return;
		}
		LOG.info("load stamp order for productInfo," + order.getOrderId());
		JSONObject json = null;
		try {   
		    LOG.info("------------------5------------------");
			String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/{orderId}";
			json = RestClient.getClient().getForObject(url, JSONObject.class, String.valueOf(order.getOrderId()));
		} catch (Throwable e) {
			LOG.error("load stamp order info failed, orderId:" + order.getOrderId(), e);
		}

		if (json == null || json.getJSONObject("stamp") == null) {
			return;
		}
		JSONObject jsonStamp = json.getJSONObject("stamp");
		if (jsonStamp == null || jsonStamp.get("name") == null) {
			return;
		}
		String name = (String) jsonStamp.get("name");
		dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), name);
		LOG.info("SmsUtil.fillSmsForPrepaidStamp:orderId=" + order.getOrderId() + ",productInfo=" + name);
	}

	public static String fillSmsToPay(String content, OrdOrder order,
			HashMap<String, Object> hashMap) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 分销短信"productInfo"优化
	 * @param order
	 * @param obj
	 * @return
	 */
	public static Object assembleSms(OrdOrder order, Object obj) {
		LOG.info(order.getOrderId() + "分销短信处理前："+obj);
		//如果内容为空，则返回空
		if(obj==null){
			return null;
		}
		//如果订单为空，返回空
		if(order==null){
			return null;
		}
		//如果不是分销商，返回原值。
		if(order.getDistributorId() != DISTRIBUTOR){
			LOG.info(order.getOrderId() + "分销短信处理后,不是分销："+obj);
			return obj;
		}

		boolean isLvtu = order.getDistributorCode()!=null?order.getDistributorCode().contains(DISTRIBUTOR_LVTU_CODE):false;
		boolean isTemai = order.getDistributorCode()!=null?order.getDistributorCode().contains(DISTRIBUTOR_TEMAI_CODE):false;
		//如果是驴途或特卖会分销短信，返回原值。否则判断产品名称括号中是否包含优惠、立减字段
		if(isLvtu || isTemai){
			LOG.info(order.getOrderId() + "分销短信处理后,驴途或者特卖会："+obj);
			return obj;
		}else{
			Pattern pattern = Pattern.compile("\\【(.*?)(优惠)(.*?)\\】");
			Pattern pattern1 = Pattern.compile("\\【(.*?)(立减)(.*?)\\】");
			obj =  filterProductInfo(pattern,obj);
			obj = filterProductInfo(pattern1,obj);
			LOG.info(order.getOrderId() + "分销短信处理后："+obj);
			return obj;

		}
	}
	/**
	 * 如果产品名称括号中有立减、优惠字段，去掉括号中内容，包括括号，否则返回原值
	 * @param pattern
	 * @param obj
	 * @return
	 */
	private static Object matcher(Pattern pattern,Object obj){
		Matcher matcher = pattern.matcher(obj.toString());
		while(matcher.find()) {
			obj = obj.toString().replace(matcher.group(), "");
		}
		return obj;
	}
	/**
	 * 分销短信优化 按中括号切分成最小块处理，避免相互影响
	 * @param pattern
	 * @param obj
	 * @return
	 */
	private static Object filterProductInfo(Pattern pattern,Object obj){
		String[]str = obj.toString().split("【");
		StringBuilder  sb = new StringBuilder();
		for (String string : str) {
			if(string.contains("】")){
				string = "【" + string;
			}
			if(!pattern.matcher(string).find()){
				sb.append(string);
			}else{
				sb.append(matcher(pattern,string));
			}
		}
		return sb;
	}
	/**
	 * 填充订单内容-电子合同修改通知
	 * @param content
	 * @param order
	 * @param in 为${visitTime}、${productName}、${邮箱地址}有调入方传入
	 * @return
	 */
	public static String fillSmsForelEcontractUpdate(String content, OrdOrder order, String email) {

		LOG.info("SmsUtil.fillSmsForelEcontractUpdate:orderId=" + order.getOrderId());

		Map <String,Object> param = new HashMap<String, Object>();
	    //订单编号orderId
//
//	    //修改时间visitTime
//		 param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), in.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField()).toString());
//
//		//产品名称productName
//		 param.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(), in.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()).toString());
//
//		//邮箱地址email
//		 param.put(OrdSmsTemplate.FIELD.EMAIL.getField(), in.get(OrdSmsTemplate.FIELD.EMAIL.getField()).toString());

		String productName = null;
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		if(pack.getProductName() != null){
	    			productName = pack.getProductName();
	    			break;
	    		}
	    	}
	    }
	    if(productName == null){
	    	productName = order.getMainOrderItem().getProductName();
	    }

	    //订单编号orderId

	    //出团时间visitTime
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));

		//产品名称productName
		param.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(), productName);

		//邮箱地址email
		param.put(OrdSmsTemplate.FIELD.EMAIL.getField(), email);

		//客服电话customerServicePhone

		return SmsUtil.fillSms(content, order, param);
	}

	/**
	 * 填充订单内容-出游短信-门票
	 * @param content        模板内容
	 * @param order          主订单
	 * @param addressMap     取票地址
	 * @param enterStyleMap  入园方式
	 * @param auxiliaryCodeMap 辅助码
	 * @param customSmsMap   自定义短线
	 * @return
	 */
	public static String fillSmsForTravelTicket(String content, OrdOrder order, Map<String,String> addressMap,
												Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> auxiliaryCodeMap, Map<String,String> customSmsMap , Map<String,Object> map, Map<String,ProdProduct> produProductMap , Map<Long,String> specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForTravelTicket:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		boolean isdisneyshow=isDisneyShowOrder(order);
		
		boolean aperiodicFlag = true;//期票不发短信

	    //打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		if(pack == null) {
	    			continue;
	    		}

				if (CollectionUtils.isEmpty(pack.getOrderItemList())) {
					continue;
				}
				for (OrdOrderItem item : pack.getOrderItemList()) {

					//Added by  yangzhenzhong
					if(item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId() || item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()
							|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
							|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()){

						String ticketTime = "";
						if(ticketTime != null){
							ticketTime = timeMap.get(item.getOrderItemId().toString());
							if(ticketTime != null && !"".equals(ticketTime.trim())){
								ticketTime = "取票时间：" + ticketTime+",";
							}else{
								ticketTime = "";
							}
						}

						String address = "";
						String visitAddress = "";
						if(addressMap != null && addressMap.isEmpty()==false){
	    					address = addressMap.get(item.getOrderItemId().toString());
	    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
	    					if(address != null && !"".equals(address.trim())){
	    	    				address = "，取票地点：" + address;
	    	    			}else{
	    	    				//address = "";
	    	    				if(StringUtils.isNotBlank(visitAddress)){
	    	    					address = "，入园地点：" + visitAddress;
	    	    				}
                            }
	    				}

	    				String enterStyle = ""; // 入园方式
	    				if(enterStyleMap != null){
		    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if(enterStyle != null && !"".equals(enterStyle.trim())){
								enterStyle = "，入园方式：" + enterStyle;
		    				}else{
		    					enterStyle = "";
		    				}
	    				}

	    				String visitTime = "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");
	    				String customSms = "";

	    				//自定义短信的赋值
						if (customSmsMap != null) {
							customSms = customSmsMap.get(item.getOrderItemId().toString());
							if (!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)) {
								customSms = customSms.substring(0, 199);
							} else if (StringUtil.isEmptyString(customSms.trim())) {
								customSms = "";
							}
						}

	    				String disney="";
	    				String time="";
	    				String section="";
	    				//增加上海迪士尼 演出时间和区域详情
	    				if(map!=null){
	    					String showtime=(String)map.get("SHOWTIME");
	    					String sectioninf=(String)map.get("SECTIONINF");
	    					if(!"".equals(showtime)&&showtime!=null){
	    						time=COMMA+"演出时间:"+showtime;
	    					}
	    					if(!"".equals(sectioninf)&&sectioninf!=null){
	    						section="区域详情:"+sectioninf;
	    					}
	    					if (StringUtils.isEmpty(address)
									&& StringUtils.isEmpty(enterStyle)&&StringUtils.isEmpty(customSms)) {
								section = section + PERIOD;
							}
	    					disney=time+section;
	    				}
	    				LOG.info("order id is disney per"+order.getOrderId()+"disney:-"+disney);
						String auxiliaryCode = ""; //通关辅助码
						if (auxiliaryCodeMap != null) {
							auxiliaryCode = auxiliaryCodeMap.get(item.getOrderItemId().toString());
							if (StringUtils.isNotBlank(auxiliaryCode))
								auxiliaryCode = "" + auxiliaryCode;
							else
								auxiliaryCode = "";
						}

						//不是期票
						String strName = "";
						if(item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()){
							strName = replaceGoodsName(item.getSuppGoodsName());
						}else{
							strName = item.getProductName() + replaceGoodsName(item.getSuppGoodsName());
						}

		 				String personContent = "";
	    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
	    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
	    				if(adultNum!=0||childNum!=0){
	    					personContent = "，包含人数";
	    					if(adultNum!=0&&childNum!=0){
		    					personContent = personContent + adultNum + "成人、" + childNum + "儿童。";
	    					}
	    					if(adultNum!=0&&childNum==0){
	    						personContent = personContent + adultNum + "成人。";
	    					}
	    					if(adultNum==0&&childNum!=0){
	    						personContent = personContent + childNum + "儿童。";
	    					}
	    				}

                        String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");
                        LOG.info("fillSmsForTravelTicket() : Lvji supplierId is :" + lvjiSupplierId);
                        boolean isLvji = (null != lvjiSupplierId && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
                        LOG.info("打包fillSmsForTravelTicket() : isLvji value is :" + isLvji);
                        LOG.info("打包item supplierId is :" + item.getSupplierId());

                        if (!item.hasTicketAperiodic()) {

                            LOG.info("fillSmsForTravelTicket() : item.hasTicketAperiodic()");
                            aperiodicFlag = false;

							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
                                sb.append(auxiliaryCode + strName + "，份数：" + item.getQuantity() + personContent + ticketTime + visitTime + address + enterStyle);
                            } else {

                                if (isLvji) {

                                    LOG.info("打包fillSmsForTravelTicket() : if  value is :" + isLvji);
                                    sb.append(strName + "，份数：" + item.getQuantity() + "。");
                                    
                                    OrdPassCode ordPassCode = (OrdPassCode)map.get(String.valueOf(item.getOrderItemId()));
                                    //OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
                                    if(ordPassCode!=null&&!"".equals(ordPassCode.getContent())){
	                                    JSONObject josnObject = JSONObject.fromObject(ordPassCode.getContent());
	                                    sb.append("智能导游服务在线使用地址：");
	                                    sb.append(josnObject.getString("url") + " 授权码：");
	                                    sb.append(josnObject.getString("code"));
	                                    sb.append("(使用截止时间：");
	                                    sb.append(josnObject.getString("endDate"));
	                                    sb.append("),");
	                                    sb.append("一个授权码只能使用一次, ");
	                                    sb.append("有效期：");
	                                    sb.append(josnObject.getString("effDays"));
	                                    sb.append("天。");
                                    }

                                } else {
                                    LOG.info("打包fillSmsForTravelTicket() : 不是旅迹, address");
                                    sb.append(auxiliaryCode + strName + "，份数：" + item.getQuantity() + ticketTime + visitTime + address + enterStyle);
                                }
                            }
                            if (timeMap != null) {
                                String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                if(StringUtils.isNotBlank(passLimitTime)){
                                    passLimitTime= ","+passLimitTime;
                                    sb.append(passLimitTime+PERIOD);

                                }
                            }

                            if (StringUtils.isNotEmpty(customSms)) {
                                sb.append(COMMA + customSms + " ");
                            } else {
                                sb.append(" ");
                            }
                        }
                    }
				}
			}
	    }
	    //非打包
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	for(OrdOrderItem item : order.getOrderItemList()){
	    		if(item.getOrderPackId() == null){//非打包产品
	    			if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L||item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){//门票
	    				String address = "";
	    				String visitAddress = "";
	    				String customSms = "";
	    				if(addressMap != null && addressMap.isEmpty()==false){
	    					address = addressMap.get(item.getOrderItemId().toString());
	    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
	    					if(address != null && !"".equals(address.trim())){
	    	    				address = "，取票地点：" + address;
	    	    			}else{
	    	    				//address = "";
	    	    				if(StringUtils.isNotBlank(visitAddress))
	    	    				address = "，入园地点：" + visitAddress;
	    	    			}
	    				}

						String ticketTime = getTicketTimeStr(timeMap, item);

	    				String enterStyle = ""; // 入园方式
	    				if(enterStyleMap != null){
		    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if(enterStyle != null && !"".equals(enterStyle.trim())){
								enterStyle = "，入园方式：" + enterStyle;
		    				}else{
		    					enterStyle = "";
		    				}
	    				}

	    				String auxiliaryCode = ""; //通关辅助码
	    				if(auxiliaryCodeMap != null){
	    					auxiliaryCode = auxiliaryCodeMap.get(item.getOrderItemId().toString());
	    					if(StringUtils.isNotBlank(auxiliaryCode))
	    						auxiliaryCode = "" + auxiliaryCode;
	    					else
	    						auxiliaryCode = "";
	    				}

	    				if(customSmsMap != null){
	    					customSms = customSmsMap.get(item.getOrderItemId().toString());
	    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
	    	    				customSms = customSms.substring(0, 199);
	    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
	    	    				customSms = "";
	    	    			}
	    				}
	    				String visitTime = "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");	    				
	    				
	    				String disney="";
	    				String time="";
	    				String section="";
	    				//增加上海迪士尼 演出时间和区域详情
	    				if(map!=null){
	    					String showtime=(String)map.get("SHOWTIME");
	    					String sectioninf=(String)map.get("SECTIONINF");
	    					if("".equals(showtime)==false&&showtime!=null){
	    						time=COMMA+"演出时间:"+showtime;
	    					}
	    					if("".equals(sectioninf)==false&&sectioninf!=null){
	    						section="区域详情:"+sectioninf;
	    					}

							if (StringUtils.isEmpty(address) && StringUtils.isEmpty(enterStyle)&& StringUtils.isEmpty(customSms)) {
								section = section + PERIOD;
							}
		    				disney = time + section;

	    				}
	    				LOG.info("order id is disney per not pack"+order.getOrderId()+"disney:-"+disney);

	    				//不是期票
	    				String strName = "";
						if(item.getCategoryId() == 13L){
							strName = replaceGoodsName(item.getSuppGoodsName());
						}else{
							strName = item.getProductName() + replaceGoodsName(item.getSuppGoodsName());
						}

						String personContent = "";
	    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
	    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
	    				if(adultNum!=0||childNum!=0){
	    					personContent = "，包含人数";
	    					if(adultNum!=0&&childNum!=0){
	    						personContent = personContent + adultNum + "成人、" + childNum + "儿童。";
	    					}
	    					if(adultNum!=0&&childNum==0){
	    						personContent = personContent + adultNum + "成人。";
	    					}
	    					if(adultNum==0&&childNum!=0){
	    						personContent = personContent + childNum + "儿童。";
	    					}
	    				}
	    				
	    				
                        String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");

                        LOG.info("Lvji supplierId is :" + lvjiSupplierId);
                        boolean isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && StringUtils.isNumeric(lvjiSupplierId) && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
                        LOG.info("非打包item supplierId is :" + item.getSupplierId());

                        if (!item.hasTicketAperiodic()) {
                            aperiodicFlag = false;
                            LOG.info("非打包fillSmsForTravelTicket() : item.hasTicketAperiodic()");
                            if (!isdisneyshow) {

								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
                                    sb.append(auxiliaryCode + strName + "，份数：" + item.getQuantity() + personContent + ticketTime + visitTime +fillShowticketSb(produProductMap, item)+ address + enterStyle);
                                } else {
                                    LOG.info("非打包fillSmsForTravelTicket() : isLvji value is :" + isLvji);
                                    if (isLvji) {
                                        sb.append(strName + "，份数：" + item.getQuantity() + "。");

                                        OrdPassCode ordPassCode = (OrdPassCode)map.get(String.valueOf(item.getOrderItemId()));
                                        if(ordPassCode!=null&&!"".equals(ordPassCode.getContent())){
	                                        JSONObject josnObject = JSONObject.fromObject(ordPassCode.getContent());
	                                        sb.append("智能导游服务在线使用地址：");
	                                        sb.append(josnObject.getString("url") + " 授权码：");
	                                        sb.append(josnObject.getString("code"));
	                                        sb.append("(使用截止时间：");
	                                        sb.append(josnObject.getString("endDate"));
	                                        sb.append("),");
	                                        sb.append("一个授权码只能使用一次, ");
	                                        sb.append("有效期：");
	                                        sb.append(josnObject.getString("effDays"));
	                                        sb.append("天。");
                                        }

                                    } else {
                                        LOG.info("非打包fillSmsForTravelTicket() : 不是旅迹 address");
                                        sb.append(auxiliaryCode + strName + "，份数：" + item.getQuantity() + "。" + ticketTime + visitTime +fillShowticketSb(produProductMap, item)+ address + enterStyle);
                                    }
                                }
                            } else {
								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
                                    String[] quantity = produtInfoDisneyShowList(order);
                                    sb.append(auxiliaryCode + item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + quantity[0] + "，包含人数"
                                            + quantity[1] + "成人、"
                                            + quantity[2] + "儿童。" + ticketTime + visitTime + disney + address + enterStyle);
                                } else {
                                    sb.append(auxiliaryCode + item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() +
                                            ticketTime + visitTime + disney + address + enterStyle);
                                }
                            }
                            if (timeMap != null) {
                                String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                if(StringUtils.isNotBlank(passLimitTime)){
                                    passLimitTime= ","+passLimitTime;
                                    sb.append(passLimitTime+"。");

                                }
                            }


                            if (StringUtils.isNotEmpty(customSms)) {
                                sb.append(COMMA + customSms + " ");
                            } else {
                                sb.append(" ");
                            }
                        }
                    }
	    		}
	    	if(isdisneyshow)
	    		break;
	    	}
	    }
	    
	    if(aperiodicFlag){//期票无需发送短信
	    	return "";
	    }
	    
	    //游玩时间
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));

	    //订单编号orderId

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		
		String isToForeign = isToForeign(order);
		param.put("isToForeign",isToForeign);
		
		//客服电话customerServicePhone

		if("Y".equalsIgnoreCase(order.getSupplierApiFlag())){
			//分销去掉这一句话。因为加入了驴妈妈的推广链接。分销不服！
			String replaceFillsms = fillSms(content, order, param).replace(REPLACEAPI, "");
			return replaceFillsms;
		}
		return fillSms(content, order, param);

	}


	/**
	 * 填充订单内容-出游短信-线路
	 * @param content        模板内容
	 * @param order          主订单
	 * @param addressMap     取票地址
	 * @param enterStyleMap  入园方式
	 * @param auxiliaryCodeMap 辅助码
	 * @param customSmsMap   自定义短线
	 * @return
	 */
	public static String fillSmsForTravelLine(String content, OrdOrder order,
			Map<String, String> addressMap, Map<String, String> enterStyleMap,
			Map<String, String> auxiliaryCodeMap,
			Map<String, String> customSmsMap) {
		LOG.info("SmsUtil.fillSmsForTravelLine : orderId = "+order.getOrderId());
		StringBuffer sb = new StringBuffer();
		Map<String,Object> param = new HashMap<String,Object>();
		//打包
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		if(pack == null) {
	    			continue;
	    		}

				if (CollectionUtils.isEmpty(pack.getOrderItemList())) {
					continue;
				}
				for (OrdOrderItem item : pack.getOrderItemList()) {

					//Added by  yangzhenzhong
					if(item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId() || item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()
							|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
							|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()){

						String address = "";
						String customSms = "";
						if (addressMap != null && addressMap.isEmpty()==false) {
							address = addressMap.get(item.getOrderItemId().toString());
							if (address != null && !"".equals(address.trim())) {
								address = "，取票地址：" + address;
							} else {
								address = "";
							}
						}

						String enterStyle = ""; // 入园方式
						if (enterStyleMap != null) {
							enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if (enterStyle != null && !"".equals(enterStyle.trim())) {
								enterStyle = "，入园方式：" + enterStyle;
							} else {
								enterStyle = "";
							}
						}

						String auxiliaryCode = ""; //通关辅助码
						if (auxiliaryCodeMap != null) {
							auxiliaryCode = auxiliaryCodeMap.get(item.getOrderItemId().toString());
							if (StringUtils.isNotBlank(auxiliaryCode))
								auxiliaryCode = "" + auxiliaryCode;
							else
								auxiliaryCode = "";
						}


						if (customSmsMap != null) {
							customSms = customSmsMap.get(item.getOrderItemId().toString());
							if (!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)) {
								customSms = customSms.substring(0, 199);
							} else if (StringUtil.isEmptyString(customSms.trim())) {
								customSms = "";
							}
						}

						if (!item.hasTicketAperiodic()) {//不是期票
								Date visitTime = item.getVisitTime();
								String VisitTime ="游玩时间：" + DateUtil.formatDate(visitTime, "yyyy-MM-dd") + COMMA;
								sb.append(VisitTime + auxiliaryCode + item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数"
										+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、"
										+ calculateLong(item.getQuantity(), item.getChildQuantity())
										+ "儿童" + address + enterStyle);
								if (StringUtils.isNotEmpty(customSms)) {
									sb.append(COMMA + customSms + SEMI);
								} else {
									sb.append(SEMI);
								}
						}
					}
				}
			}
	    }

	    //非打包
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	for(OrdOrderItem item : order.getOrderItemList()){
	    		if(item.getOrderPackId() == null){//非打包产品
	    			if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
	    				String address = "";
	    				String customSms = "";
	    				if(addressMap != null && addressMap.isEmpty()==false){
	    					address = addressMap.get(item.getOrderItemId().toString());
	    					if(address != null && !"".equals(address.trim())){
	    	    				address = "，取票地址：" + address;
	    	    			}else{
	    	    				address = "";
	    	    			}
	    				}

	    				String enterStyle = ""; // 入园方式
	    				if(enterStyleMap != null){
		    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if(enterStyle != null && !"".equals(enterStyle.trim())){
								enterStyle = "，入园方式：" + enterStyle;
		    				}else{
		    					enterStyle = "";
		    				}
	    				}

	    				String auxiliaryCode = ""; //通关辅助码
	    				if(auxiliaryCodeMap != null){
	    					auxiliaryCode = auxiliaryCodeMap.get(item.getOrderItemId().toString());
	    					if(StringUtils.isNotBlank(auxiliaryCode))
	    						auxiliaryCode = "" + auxiliaryCode;
	    					else
	    						auxiliaryCode = "";
	    				}

	    				if(customSmsMap != null){
	    					customSms = customSmsMap.get(item.getOrderItemId().toString());
	    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
	    	    				customSms = customSms.substring(0, 199);
	    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
	    	    				customSms = "";
	    	    			}
	    				}

	    				//游玩时间
	    				if(!item.hasTicketAperiodic()){//不是期票
								Date visitTime = item.getVisitTime();
								String VisitTime ="游玩时间：" + DateUtil.formatDate(visitTime, "yyyy-MM-dd") + COMMA;
								sb.append(VisitTime + auxiliaryCode + item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数"
										+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、"
										+ calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童"
										+ address + enterStyle);
								if(StringUtils.isNotEmpty(customSms)){
									sb.append(COMMA + customSms + SEMI);
								}else{
									sb.append(SEMI);
								}
	    				}
	    			}
	    		}
	    	}
	    }

	    //订单编号orderId

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//客服电话customerServicePhone
		return fillSms(content, order, param);

	}

	/**Modified by yangzhenzhong
	 * 填充订单内容-出游短信-酒店
	 * @param content
	 * @param order
	 * @param prodProductMap
	 * @return
	 */
	public static String fillSmsForTravelHotel(String content, OrdOrder order, Map<String,ProdProduct> prodProductMap) {
		LOG.info("SmsUtil.fillSmsForTravelHotel:orderId=" + order.getOrderId());
		return SmsUtil.fillSmsForPayHotel(content, order, prodProductMap);
	}
	
	/**Modified by yangzhenzhong
	 * 填充订单内容-出游短信-酒店(新的)
	 * @param content
	 * @param order
	 * @param prodProductMap
	 * @return
	 */
	public static String fillSmsForTravelHotelNew(String content, OrdOrder order, Map<String,ProdProduct> prodProductMap) {
		LOG.info("SmsUtil.fillSmsForTravelHotel:orderId=" + order.getOrderId());
		return SmsUtil.fillSmsForPayHotelNew(content, order, prodProductMap);
	}
	
	/**
	 * 填充订单内容-出团通知
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSmsForGroupNotice(String content, OrdOrder order, String email) {
		LOG.info("SmsUtil.fillSmsForGroupNotice:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		String productName = null;
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		if(pack.getProductName() != null){
	    			productName = pack.getProductName();
	    			break;
	    		}
	    	}
	    }
	    if(productName == null){
	    	productName = order.getMainOrderItem().getProductName();
	    }

	    //订单编号orderId

	    //出团时间visitTime
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));

		//产品名称productName
		param.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(), productName);

		//邮箱地址email
		param.put(OrdSmsTemplate.FIELD.EMAIL.getField(), email);

		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}

	/**
	 * 申码成功(支付完成)-支付完成+预付+交通接驳
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSmsForConnectsNotice(String content, OrdOrder order) {
		LOG.info("SmsUtil.fillSmsForConnectsNotice:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		param.put("isToForeign", "Y");
		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}

	/**
	 * 填充订单内容-订单提交-现付-门票
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSmsForPayTickets(String content, OrdOrder order, Map<String,String>addressMap,Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap,Map<String,ProdProduct> produProductMap,Map<Long,String> specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForPayTickets:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		String str1 = "，份数：";
		String str2 = "，张数：";
	    //打包
		//String PERIOD = "。";
		String  termofValidityStr = "有效期：";
		if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()) {
				if (BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
						|| BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
						|| BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
						|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
						) {
					for (OrdOrderItem item : pack.getOrderItemList()) {
                        String termOfvalidity = getGoodsExpString(item);
						String invalidDate = "";
						if (item.getAperiodicUnvalidDesc() != null && !"".equals(item.getAperiodicUnvalidDesc())) {
							invalidDate = "不适用日期 ：" + item.getAperiodicUnvalidDesc();
						} else if (item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())) {
							invalidDate = "不适用日期 ：" + item.getInvalidDate();
						}
						String ticketTime = getTicketTimeStr(timeMap, item);
						String enterStyle = ""; // 入园方式
						if (enterStyleMap != null) {
							enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if (enterStyle != null && !"".equals(enterStyle.trim())) {
								enterStyle = "，入园方式：" + enterStyle;
							} else {
								enterStyle = "";
							}
						}
						String address = "";
						String visitAddress = "";
						String customSms = "";
						if (addressMap != null && addressMap.isEmpty() == false) {
							address = addressMap.get(item.getOrderItemId().toString());
							visitAddress = addressMap.get(item.getOrderItemId().toString() + "visitAddress");
							if (address != null && !"".equals(address.trim())) {
								address = "，取票地点：" + address;
							} else {
								//address = "";
								if (StringUtils.isNotBlank(visitAddress)) {
									address = "，入园地点：" + visitAddress;
								}
							}
						}
						if (customSmsMap != null) {
							customSms = customSmsMap.get(item.getOrderItemId().toString());
							if (!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)) {
								customSms = COMMA + customSms.substring(0, 199);
							} else if (StringUtil.isEmptyString(customSms.trim())) {
								customSms = "";
							}
						}

						String personContent = "";
						long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
						long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
						if (adultNum != 0 || childNum != 0) {
							personContent = "，包含人数";
							if (adultNum != 0 && childNum != 0) {
								personContent = personContent + adultNum + "成人、" + childNum + "儿童。";
							}
							if (adultNum != 0 && childNum == 0) {
								personContent = personContent + adultNum + "成人。";
							}
							if (adultNum == 0 && childNum != 0) {
								personContent = personContent + childNum + "儿童。";
							}
						}
						String passLimitTime = "";
						if (timeMap != null) {
							passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
							if (passLimitTime == null) {
								passLimitTime = "";
							}
							if (StringUtils.isNotBlank(passLimitTime)) {
								passLimitTime = passLimitTime + "。";
							}
						}

						if (item.hasTicketAperiodic()) {
							if (item.getCategoryId() == 13L) {
//								String oldYouxiaoqi = ticketTime + termofValidityStr + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd");
								if (specialSmsMap.get(item.getSuppGoodsId()) == null || "".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + personContent  + termOfvalidity + invalidDate + address + enterStyle + passLimitTime + customSms + " ");

								} else {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + PERIOD  + termOfvalidity +  invalidDate + address + enterStyle + passLimitTime + customSms + " ");
								}
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
							} else {
								if (specialSmsMap.get(item.getSuppGoodsId()) == null || "".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
									sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + personContent + termOfvalidity +  invalidDate + address + enterStyle + passLimitTime + customSms + " ");
								}
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
								else {
									sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + PERIOD + termOfvalidity +  invalidDate + address + enterStyle + passLimitTime + customSms + " ");
								}
							}
						} else {
							if (item.getCategoryId() == 13L) {
								if (specialSmsMap.get(item.getSuppGoodsId()) == null || "".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + personContent + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
								} else {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + PERIOD + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
								}
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
							} else {
								if (specialSmsMap.get(item.getSuppGoodsId()) == null || "".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
									sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + personContent + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
								} else {
									sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + PERIOD + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
								}
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
							}
						}
					}
				}
			}
		}
	    //非打包
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	for(OrdOrderItem item : order.getOrderItemList()){
	    		if(item.getOrderPackId() == null){//非打包产品
	    			if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId() ){
						//门票
                        String termOfvalidity = getGoodsExpString(item);
						String invalidDate = "";
                        if(item.getAperiodicUnvalidDesc()!=null &&!"".equals(item.getAperiodicUnvalidDesc())){
                            invalidDate = "不适用日期 ：" + item.getAperiodicUnvalidDesc();
                        }else if(item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())){
                            invalidDate = "不适用日期 ：" + item.getInvalidDate();
                        }
	    				String address = "";
	    				String visitAddress = "";
	    				String customSms = "";
	    				if(addressMap != null && addressMap.isEmpty()==false){
	    					address = addressMap.get(item.getOrderItemId().toString());
	    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
	    					if(address != null && !"".equals(address.trim())){
	    	    				address = "，取票地点：" + address;
	    	    			}else{
	    	    				//address = "";
	    	    				if(StringUtils.isNotBlank(visitAddress)){
	    	    					address = "，入园地点：" + visitAddress;
	    	    				}
	    	    			}
	    				}
						String ticketTime = getTicketTimeStr(timeMap, item);
						String enterStyle = ""; // 入园方式
	    				if(enterStyleMap != null){
		    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if(enterStyle != null && !"".equals(enterStyle.trim())){
								enterStyle = "，入园方式：" + enterStyle;
		    				}else{
		    					enterStyle = "";
		    				}
	    				}
	    				if(customSmsMap != null){
	    					customSms = customSmsMap.get(item.getOrderItemId().toString());
	    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
	    	    				customSms = COMMA+customSms.substring(0, 199);
	    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
	    	    				customSms = "";
	    	    			}
	    				}
						//入园限制时间
						String passLimitTime="";
						if (timeMap != null) {
							passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
							if (StringUtils.isNotBlank(passLimitTime)) {
								passLimitTime =	passLimitTime+"。";
							}else if( passLimitTime==null){
								passLimitTime="";
							}
						}

	    				String personContent = "";
	    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
	    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());

	    				if(adultNum!=0||childNum!=0){
	    					personContent = "，包含人数";
	    					if(adultNum!=0&&childNum!=0){
	    						personContent = personContent + adultNum + "成人、" + childNum + "儿童。";
	    					}
	    					if(adultNum!=0&&childNum==0){
	    						personContent = personContent + adultNum + "成人。";
	    					}
	    					if(adultNum==0&&childNum!=0){
	    						personContent = personContent + childNum + "儿童。";
	    					}
	    				}

	    				
	    				String fillShowticketString = fillShowticketSb(produProductMap, item);
	    				
	    				if(item.hasTicketAperiodic()){
							//是期票
	    					if(item.getCategoryId()==13L){
								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
		    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + personContent  + termOfvalidity + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
	    						}else{
		    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + PERIOD + ticketTime + termOfvalidity + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
	    						}
		    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
	    					}else{
								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
		    						sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + personContent   + termOfvalidity + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
	    						}else{
		    						sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + PERIOD  + termOfvalidity + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
	    						}
	    						//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
	    					}
	    				}else{
	    					if(item.getCategoryId()==13L){
								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
		    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + personContent + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
	    						}else{
		    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + PERIOD + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
	    						}
	    						//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str1 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
	    					}else{
								if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
		    						sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + personContent + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +fillShowticketString+ address + enterStyle + passLimitTime + customSms + " ");
	    						}
	    						else{
		    						sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + PERIOD + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+ fillShowticketString + address + enterStyle + passLimitTime + customSms + " ");

	    						}

	    						//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + str2 + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
	    //订单编号orderId

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		
		String isToForeign = isToForeign(order);
		param.put("isToForeign", isToForeign);

		//客服电话customerServicePhone
		if("Y".equalsIgnoreCase(order.getSupplierApiFlag())){
			return fillSms(content, order, param).replace(REPLACEAPI, "");
		}
		
		return fillSms(content, order, param);
	}

	private static String getTicketTimeStr(Map<String, String> timeMap, OrdOrderItem item) {
		String ticketTime = "";
		if (timeMap != null) {
            ticketTime = timeMap.get(item.getOrderItemId().toString());
            if (ticketTime != null && !"".equals(ticketTime.trim())) {
                ticketTime = "取票时间：" + ticketTime + COMMA;
            } else {
                ticketTime = "";
            }
        }
		return ticketTime;
	}


	/**
	 * 填充订单内容-订单提交-现付-酒店
	 * @param content
	 * @param order
	 * @return
	 */
	public static String fillSmsForPayHotel(String content, OrdOrder order, ProdProduct product,String conTactName) {
		LOG.info("SmsUtil.fillSmsForPayHotel:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	for(OrdOrderItem item : order.getOrderItemList()){
	    		if(item.getCategoryId() == 1L){//酒店
	    				//订单编号orderId
                      
	    				//产品名称productName
	    				param.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(), item.getProductName());

	    				//商品名称suppGoodsName
	    				param.put(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField(), item.getSuppGoodsName());

	    				//房间数quantity
	    				param.put(OrdSmsTemplate.FIELD.QUANTITY.getField(), item.getQuantity());

	    				//入住时间visitTime
	    				param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

	    				//离店时间
	    				List<OrdOrderHotelTimeRate> oohtsList = item.getOrderHotelTimeRateList();
	    				Date tempTime = oohtsList.get(oohtsList.size() - 1).getVisitTime();
	    				Calendar calendar = Calendar.getInstance();
	    				calendar.setTime(tempTime);
	    				int day = calendar.get(Calendar.DATE);
	    			    calendar.set(Calendar.DATE, day + 1);
	    				Date departureDate = calendar.getTime();
	    				param.put(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField(), DateUtil.formatDate(departureDate, "yyyy-MM-dd"));

	    				//总价oughtAmount
	    				param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());
	    				//酒店地址productAddress
	    				param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), product.getPropValue().get("address"));


	    				//酒店电话productTelephone
	    				param.put(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField(), product.getPropValue().get("telephone"));
	    				
	    				//add by xiachengliang start
	    				//预定人姓名
	    				if(StringUtil.isNotEmptyString(conTactName)){
	    					param.put(OrdSmsTemplate.FIELD.CONTACT_NAME.getField(), conTactName);
	    				}
	    				//end

	    				//客服电话customerServicePhone

		    			if(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equalsIgnoreCase(order.getGuarantee())){//担保
		    				//担保方式cancelStrategy
		    				if (!isCancelStrategy(order)) {
		    					String cancelStrategy = order.getRealCancelStrategy();
		    					if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
		    						cancelStrategy = order.getMainOrderItem().getCancelStrategy();
		    					}
		    					if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
		    						cancelStrategy = order.getCancelStrategy();
		    					}
		    					
		    					if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
		    						cancelStrategy = order.getMainOrderItem().getCancelStrategy();
		    					}
		    					//当地玩乐 美食 娱乐 购物 退改策略
		    					if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
		    					 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
		    					 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
		    						cancelStrategy = order.getMainOrderItem().getCancelStrategy();
		    					}
		    					param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
		    				}

							//Added by yangzhenzhong 增加最晚无损取消时间
							param.put(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField(),DateUtil.formatSimpleDate(order.getLastCancelTime()));
							//end

		    			}else{//非担保
		    				String[] orderLastArrival =item.getContentValueByKey(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name()).toString().split(":");
		    				if(orderLastArrival.length>1){
		    					Calendar visitTime = Calendar.getInstance();
		    					visitTime.setTime(item.getVisitTime());
		    					visitTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(orderLastArrival[0]));
		    					visitTime.set(Calendar.MINUTE, Integer.parseInt(orderLastArrival[1]));
		    					visitTime.set(Calendar.SECOND, 0);
		    					visitTime.set(Calendar.MILLISECOND, 0);
			    				//最晚保留时间latestUnguarTime
			    				param.put(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField(), DateUtil.formatDate(visitTime.getTime(), "yyyy-MM-dd HH:mm:ss"));
		    				}else{
		    					param.put(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField(), "");
		    				}
		    			}
	    		}
	    	}
	    }
	    if("Y".equalsIgnoreCase(order.getSupplierApiFlag())){
	    	return fillSms(content, order, param).replace(REPLACEAPI, "");
	    }
		return fillSms(content, order, param);
	}

	/**Created by yangzhenzhong
	 * 方法重载 修改参数为prodProductMap
	 * 针对于打包产品
	 * 填充订单内容-订单提交-现付-酒店
	 * @param content
	 * @param order
	 * @param prodProductMap
	 * @return
	 */
	public static String fillSmsForPayHotelNew(String content, OrdOrder order, Map<String,ProdProduct> prodProductMap) {
		LOG.info("SmsUtil.fillSmsForPayHotel:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			for(OrdOrderItem item : order.getOrderItemList()){
				if(item.getCategoryId() == 1L){//酒店
					//产品名称productName
					sb.append(item.getProductName()+COMMA);
					
					//商品名称suppGoodsName
					sb.append(item.getSuppGoodsName()+COMMA);

					//房间数quantity
					sb.append(item.getQuantity()+"间"+COMMA);

					//入住时间visitTime
					List<OrdOrderHotelTimeRate> oohtsList = item.getOrderHotelTimeRateList();
					Date tempTime = oohtsList.get(oohtsList.size() - 1).getVisitTime();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(tempTime);
					int day = calendar.get(Calendar.DATE);
					calendar.set(Calendar.DATE, day + 1);
					Date departureDate = calendar.getTime();
					sb.append(DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+"至"+DateUtil.formatDate(departureDate, "yyyy-MM-dd")+COMMA);

					if(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equalsIgnoreCase(order.getGuarantee())){//担保
						if(order.getLastCancelTime() != null){
							sb.append("在"+DateUtil.formatDate(order.getLastCancelTime(), "yyyy-MM-dd HH:mm:ss")+"前可无损取消。"+COMMA);
						}
					}

					//酒店地址productAddress
					sb.append("酒店地址："+prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("address")+COMMA);

					//酒店电话productTelephone
					if(prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone")!=null
							&& !prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone").equals("")){
						sb.append("酒店电话："+prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone")+COMMA);
					}else {
						sb.append("酒店电话："+"无"+COMMA);
					}
				}
			}
		}
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		return fillSms(content, order, param);
	}
	
	/**Created by yangzhenzhong
	 * 方法重载 修改参数为prodProductMap
	 * 针对于打包产品
	 * 填充订单内容-订单提交-现付-酒店
	 * @param content
	 * @param order
	 * @param prodProductMap
	 * @return
	 */
	public static String fillSmsForPayHotel(String content, OrdOrder order, Map<String,ProdProduct> prodProductMap) {
		LOG.info("SmsUtil.fillSmsForPayHotel:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			for(OrdOrderItem item : order.getOrderItemList()){
				if(item.getCategoryId() == 1L){//酒店
					//订单编号orderId

					//产品名称productName
					param.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(), item.getProductName());

					//商品名称suppGoodsName
					param.put(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField(), item.getSuppGoodsName());

					//房间数quantity
					param.put(OrdSmsTemplate.FIELD.QUANTITY.getField(), item.getQuantity());

					//入住时间visitTime
					param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

					//离店时间
					List<OrdOrderHotelTimeRate> oohtsList = item.getOrderHotelTimeRateList();
					Date tempTime = oohtsList.get(oohtsList.size() - 1).getVisitTime();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(tempTime);
					int day = calendar.get(Calendar.DATE);
					calendar.set(Calendar.DATE, day + 1);
					Date departureDate = calendar.getTime();
					param.put(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField(), DateUtil.formatDate(departureDate, "yyyy-MM-dd"));

					//总价oughtAmount
					param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

					//酒店地址productAddress

					param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("address"));

					//酒店电话productTelephone
					if(prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone")!=null
							&& !prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone").equals("")){
						param.put(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField(), prodProductMap.get(item.getOrderItemId().toString()).getPropValue().get("telephone"));
					}else {
						param.put(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField(), "无");
					}
					if(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equalsIgnoreCase(order.getGuarantee())){//担保
						//担保方式cancelStrategy
						if (!isCancelStrategy(order)) {
							String cancelStrategy = order.getRealCancelStrategy();
							if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
								cancelStrategy = order.getMainOrderItem().getCancelStrategy();
							}
							if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
								cancelStrategy = order.getCancelStrategy();
							}
							if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
								cancelStrategy = order.getMainOrderItem().getCancelStrategy();
							}
							//当地玩乐 美食 娱乐 购物 退改策略
							if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
							 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
							 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
								cancelStrategy = order.getMainOrderItem().getCancelStrategy();
							}
							param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
						}

						if(order.getLastCancelTime() != null){
							param.put(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField(),"在"+DateUtil.formatDate(order.getLastCancelTime(), "yyyy-MM-dd HH:mm:ss")+"前可无损取消。");
						}else{
							param.put(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField(),"");
						}

					}else{//非担保
						String[] orderLastArrival =item.getContentValueByKey(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name()).toString().split(":");
						if(orderLastArrival.length>1){
							Calendar visitTime = Calendar.getInstance();
							visitTime.setTime(item.getVisitTime());
							visitTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(orderLastArrival[0]));
							visitTime.set(Calendar.MINUTE, Integer.parseInt(orderLastArrival[1]));
							visitTime.set(Calendar.SECOND, 0);
							visitTime.set(Calendar.MILLISECOND, 0);
							//最晚保留时间latestUnguarTime
							param.put(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField(), DateUtil.formatDate(visitTime.getTime(), "yyyy-MM-dd HH:mm:ss"));
						}else{
							param.put(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField(), "");
						}
					}
				}
			}
		}
		return fillSms(content, order, param);
	}


	/**
	 * 填充订单内容拼接,线路打包+其他非打包
	 * @param sb
	 * @param item          子订单
	 * @param product       酒店产品
	 * @param sendNode      发送节点
	 * @param addressMap    取票地址
	 * @param enterStyleMap 入园方式
	 * @param customSmsMap  自定义短信
	 */
	private static void appendCommon(StringBuffer sb,OrdOrderItem item,OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap
			, Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<String, SuppSupplier> itemSuppsupplier,Integer buildBuyCount,boolean isPayed,OrdOrder order,Map<Long,String> specialSmsMap,boolean ifYL){
		if(item.getCategoryId() == 1L){//酒店
			List<OrdOrderHotelTimeRate> oohtsList = item.getOrderHotelTimeRateList();
			Date tempTime = oohtsList.get(oohtsList.size() - 1).getVisitTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tempTime);
			int day = calendar.get(Calendar.DATE);
		    calendar.set(Calendar.DATE, day + 1);
			Date departureDate = calendar.getTime();
			sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "间，" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + UNTIL + DateUtil.formatDate(departureDate, "yyyy-MM-dd") );
			if(sendNode != null){
				ProdProduct product = itemProdProducts.get(item.getOrderItemId().toString());
				if(product != null){
					if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)){
						sb.append(",酒店地址：" + (product.getPropValue() == null ? null : product.getPropValue().get("address")) + ",酒店电话：" + (product.getPropValue() == null ? null : product.getPropValue().get("telephone")));
					}
				}
			}
			sb.append("。");
		}
        if(item.getCategoryId() == 2L){//游轮
        	sb.append(item.getProductName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 3L){//保险
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 4L){//签证
        	sb.append(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 8L){//游轮组合
        	sb.append(item.getProductName() + "。");
        }
        if(item.getCategoryId() == 9L){//岸上观光
        	sb.append(item.getProductName() + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId()==10L){//游轮附加
        	sb.append(item.getProductName() + COMMA + item.getQuantity() + "份。");
        }
		if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L || item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
			//门票
			
			boolean isLvji = false;
			String lvjiSupplierId = Constant.getInstance().getProperty(
					"lvji.supplierId");
			try {
				isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && Long
						.parseLong(lvjiSupplierId) == item.getSupplierId());
			} catch (Exception e) {
				e.printStackTrace();
				isLvji = false;
			}


            String invalidDate = "";
            if(item.getAperiodicUnvalidDesc()!=null &&!"".equals(item.getAperiodicUnvalidDesc())){
                invalidDate = "不适用日期 ：" + item.getAperiodicUnvalidDesc();
            }else if(item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())){
                invalidDate = "不适用日期 ：" + item.getInvalidDate();
            }
			String ticketTime = getTicketTimeStr(timeMap, item);
			String enterStyle = ""; // 入园方式
			if(enterStyleMap != null){
				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
				if(enterStyle != null && !"".equals(enterStyle.trim())){
					enterStyle = "，入园方式：" + enterStyle;
				}else{
					enterStyle = "";
				}
			}
            String goodExpInfo = getGoodsExpString(item);

			String address = "";
			String visitAddress = "";
			String customSms = "";
			if(addressMap != null && addressMap.isEmpty()==false){
				address = addressMap.get(item.getOrderItemId().toString());
				visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
				if(address != null && !"".equals(address.trim())){
    				address = "，取票地点：" + address;
    			}else{
    				//address = "";
    				if(StringUtils.isNotBlank(visitAddress)){
    					address = "，入园地点：" + visitAddress;
    				}
    			}
			}
			if(customSmsMap != null){
				customSms = customSmsMap.get(item.getOrderItemId().toString());
    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
    				customSms = COMMA+customSms.substring(0, 199);
    			}else if(StringUtil.isEmptyString(customSms.trim())){
    				customSms = "";
    			}
			}

			String personContent = "";
			long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
			long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
			if(adultNum!=0||childNum!=0){
				personContent = "，包含人数";
				if(adultNum!=0&&childNum!=0){
					personContent = personContent + adultNum + "成人、" + childNum + "儿童";
				}
				if(adultNum!=0&&childNum==0){
					personContent = personContent + adultNum + "成人";
				}
				if(adultNum==0&&childNum!=0){
					personContent = personContent + childNum + "儿童";

				}
			}

			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)) {


				String passLimitTime = "";
				if (timeMap != null) {
					passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
					if (StringUtils.isBlank(passLimitTime)) {
						passLimitTime = "";
					}else{
						passLimitTime=","+passLimitTime;
					}
				}
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
						|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)){
					passLimitTime="";
				}
                if(item.hasTicketAperiodic()){
					if(item.getCategoryId()==13L){
						String oldticketTIme = ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd");
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + goodExpInfo + invalidDate + address + enterStyle  + passLimitTime + customSms + " ");
    						}else{
                                //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                if (isLvji) {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
                                } else {
                                    sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime +customSms + " ");
                                }
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
						}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" +  goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}
    						else
    						{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" +  goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" +  "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
						}
					}else{
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){

							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
    						}else {

                                //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                if (isLvji) {
                                    sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
                                } else {
                                    sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
                                }
    						}
    					}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。"  + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}else{
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "。"  + goodExpInfo + invalidDate + passLimitTime + customSms + " ");

    						}
    					}
					}
				}else{
					if(item.getCategoryId()==13L){
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
    						}
    						else{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
    						}
    					}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  passLimitTime + customSms + " ");

    						}else{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  passLimitTime + customSms + " ");
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"+ "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  customSms + " ");
						}
					}else{
						if(isPayed){
							//增加演出票，支付成功，节点的信息
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
								LOG.info("增加演出票，支付成功，节点的信息,orderID"+order.getOrderId() + " ifYL"+ ifYL);
									sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +fillShowticketSb(itemProdProducts, item) + address + enterStyle + passLimitTime + customSms + " ");
    						}else{
    								sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + fillShowticketSb(itemProdProducts, item) + address + enterStyle + passLimitTime +  customSms + " ");
    						}
						}else{
							//增加演出票，审核成功+预付，节点的逻辑
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){ //need alter
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+ fillShowticketShowTime( item) + passLimitTime + customSms + " ");
    						}else{
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + fillShowticketShowTime(item) + passLimitTime + customSms + " ");
    						}
						}
					}
				}
			}else{
				String passLimitTime = "";
				if (timeMap != null) {
					passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
					if (StringUtils.isBlank(passLimitTime)) {
						passLimitTime = "";
					}else{
						passLimitTime=","+passLimitTime;
					}
				}
				if(item.hasTicketAperiodic()){
					if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "," +goodExpInfo + invalidDate + passLimitTime);
						}else{
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "," + goodExpInfo + invalidDate + passLimitTime);
						}
						//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
    				}else{
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "," + goodExpInfo + invalidDate + passLimitTime);
						}else{
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "," + goodExpInfo + invalidDate + passLimitTime);
						}
    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
    				}
    			}else{
    				if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}else{
	    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}
    					//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
    				}else{
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}else{
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}
    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
    				}
    			}
			}
			if(sendNode != null && addressMap != null && addressMap.isEmpty()==false && enterStyleMap != null && enterStyleMap.isEmpty()==false && customSmsMap != null && customSmsMap.isEmpty()==false){
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)){
					
					address = addressMap.get(item.getOrderItemId().toString());
					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					if(address != null && !"".equals(address.trim())){
    					address = "，取票地址：" + address;
    				}else{
    					//address = "";
    					if(StringUtils.isNotBlank(visitAddress)){
    						address = "，入园地址：" + visitAddress;
    					}
    				}

					enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
					if(enterStyle != null && !"".equals(enterStyle.trim())){
						enterStyle = "，入园方式：" + enterStyle;
    				}else{
    					enterStyle = "";
    				}
					sb.append(address + enterStyle);

    				customSms = customSmsMap.get(item.getOrderItemId().toString());
    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
    					customSms = customSms.substring(0, 199);
    				}else if(StringUtil.isEmptyString(customSms.trim())){
    					customSms = "";
    				}
    				//入园限制时间
                    if (timeMap != null) {
                        String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                        if(StringUtils.isNotBlank(passLimitTime)){
                            passLimitTime= ","+passLimitTime;
                            sb.append(passLimitTime);
                        }
                    }


                    if(StringUtils.isNotEmpty(customSms)){
    					sb.append(COMMA + customSms + " ");
    				}else{
    					sb.append(" ");
    				}
				}
			}else{
	//			sb.append("。");
			}
		}
        if(item.getCategoryId() == 15L || item.getCategoryId()==16L || item.getCategoryId() == 17L//线路大类
        		|| item.getCategoryId() == 18L || item.getCategoryId() == 42L){//新增加定制游
        	ProdProduct product = itemProdProducts.get(item.getOrderItemId().toString());

        	LOG.info("SmsUtil-->appendCommon-->orderId:"
    				+ item.getOrderId()
    				+ "item info:" + item.getOrderItemId() + "||" + item.getProductId()
    				+ "||" + item.getCategoryId() + "||" + item.getBranchId()
    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
    				+ "||product whether exist:" + (product == null ? "null" : "is not null")
    			);

        	if(product != null){

        		sb.append(item.getProductName() + "，出游日期：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
        		if(!StringUtils.isEmpty(frontBusStop)){
        			sb.append(","+frontBusStop);
        		}



        		//地址
        		if(product.getPropValue() != null && (product.getPropValue().get("address") != null) && (StringUtil.isNotEmptyString(product.getPropValue().get("address").toString()))){
        			sb.append(COMMA + product.getPropValue().get("address"));
        		}

        		//获得供应商紧急电话   添加条件 ：跟团游，国内短线，供应商打包
        		if((item.getCategoryId() == 15L && product.getPackageType().equals("SUPPLIER") &&
        				product.getProductType().equals("INNERSHORTLINE"))){
        			if(buildBuyCount != null){
                		sb.append("，订购"+buildBuyCount+"份");
                	}

        			Date beginDate = item.getVisitTime();
        			Calendar date = Calendar.getInstance();
        			date.setTime(beginDate);
        			date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);

        			String str = DateUtil.formatDate(date.getTime(), "yyyy-MM-dd");

        			sb.append("，我们将在"+ str + "，21:00前，通过短信将车牌号及座位号发送给您，请保持手机畅通！");
        			if (itemSuppsupplier!=null && itemSuppsupplier.size()>0) {
        				SuppSupplier suppSupplier = itemSuppsupplier.get(item.getOrderItemId().toString());
        				if(!StringUtils.isEmpty(suppSupplier.getSupplierTel()) && !"".equals(suppSupplier.getSupplierTel())){
        					sb.append("应急电话："+suppSupplier.getSupplierTel()+"。");
        				}
					}
        		}
        		//获得供应商紧急电话   添加条件 ：当地游，国内短线（含国内），供应商打包
        		if((item.getCategoryId() == 16L && product.getPackageType().equals("SUPPLIER") &&
        				(product.getProductType().equals("INNERSHORTLINE") || product.getProductType().equals("INNERLINE")))){
        			if(buildBuyCount != null){
        				sb.append("，订购"+buildBuyCount+"份");
        			}
        			
        			Date beginDate = item.getVisitTime();
        			Calendar date = Calendar.getInstance();
        			date.setTime(beginDate);
        			date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
        			
        			String str = DateUtil.formatDate(date.getTime(), "yyyy-MM-dd");

                   ProdTrafficVO prodTrafficVO =	product.getProdTrafficVO();
       
 			     if(prodTrafficVO!=null && prodTrafficVO.getProdTrafficGroupList()!=null &&
                		prodTrafficVO.getProdTrafficGroupList().get(0)!=null){ 
 				   LOG.info("ProdTrafficGroup==");
                	    ProdTrafficGroup group  =	prodTrafficVO.getProdTrafficGroupList().get(0);
                	   LOG.info("ProdTrafficGroup=="+group);
                		if(group.getProdTrafficBusList()!=null
                		&&group.getProdTrafficBusList().size()>0){
    			        sb.append("，我们将在"+ str + "，21:00前，通过短信将车牌号及座位号发送给您，请保持手机畅通！");
    			        }
                }
        			LOG.info("get 16 itemSuppsupplier ="+ itemSuppsupplier);
        			LOG.info("get 16 item.getOrderItemId ="+ item.getOrderItemId());
        			if (itemSuppsupplier!=null && itemSuppsupplier.size()>0) {
        				SuppSupplier suppSupplier = itemSuppsupplier.get(item.getOrderItemId().toString());
        				LOG.info("get 16 suppSupplier ="+ suppSupplier);
        				if(!StringUtils.isEmpty(suppSupplier.getSupplierTel()) && !"".equals(suppSupplier.getSupplierTel())){
        					sb.append("应急电话："+suppSupplier.getSupplierTel()+"。");
        				}
					}
        		}
        	}
        }
        //交通接驳
        if(item.getCategoryId()==41L){
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份"+COMMA+"使用时间 ："+ DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+"");
        }
        //当地玩乐 美食 娱乐 购物
        if(Long.valueOf(43L).equals(item.getCategoryId()) || Long.valueOf(44L).equals(item.getCategoryId()) || Long.valueOf(45L).equals(item.getCategoryId())){
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "份"+COMMA+"使用时间 ："+ DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)) {
				
				try {
					sb.append(getPlayCustomSms(customSmsMap,item));
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}
				
			}
        }
		if(item.getCategoryId()	==	19L
				|| item.getCategoryId() == 22L || item.getCategoryId() == 23L
				|| item.getCategoryId() == 24L || item.getCategoryId() == 25L
				|| item.getCategoryId() == 26L || item.getCategoryId() == 27L
				|| item.getCategoryId() == 90L||item.getCategoryId() == 28L){
			sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
		}else if(item != null
        		&& (BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(item.getCategoryId())
        		|| BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(item.getCategoryId()))){//@todo机票分支
        	appendFlightCommon(sb,item);
		}
	}
	
	/**
	 * 填充订单内容拼接,线路打包+其他非打包(跟团游)
	 * @param sb
	 * @param item          子订单
	 * @param product       酒店产品
	 * @param sendNode      发送节点
	 * @param addressMap    取票地址
	 * @param enterStyleMap 入园方式
	 * @param customSmsMap  自定义短信
	 */
	private static void appendCommonForRouteGroup(StringBuffer sb,OrdOrderItem item,OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap
			, Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<String, SuppSupplier> itemSuppsupplier,Integer buildBuyCount,boolean isPayed,OrdOrder order,Map<Long,String> specialSmsMap){
		if(item.getCategoryId() == 1L){//酒店
			List<OrdOrderHotelTimeRate> oohtsList = item.getOrderHotelTimeRateList();
			Date tempTime = oohtsList.get(oohtsList.size() - 1).getVisitTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tempTime);
			int day = calendar.get(Calendar.DATE);
		    calendar.set(Calendar.DATE, day + 1);
			Date departureDate = calendar.getTime();
			sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "间，" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + UNTIL + DateUtil.formatDate(departureDate, "yyyy-MM-dd") );
			if(sendNode != null){
				ProdProduct product = itemProdProducts.get(item.getOrderItemId().toString());
				if(product != null){
					if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)
							|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)){
						sb.append(",酒店地址：" + (product.getPropValue() == null ? null : product.getPropValue().get("address")) + ",酒店电话：" + (product.getPropValue() == null ? null : product.getPropValue().get("telephone")));
					}
				}
			}
			sb.append("。");
		}
        if(item.getCategoryId() == 2L){//游轮
        	sb.append(item.getProductName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 3L){//保险
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 4L){//签证
        	sb.append(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId() == 8L){//游轮组合
        	sb.append(item.getProductName() + "。");
        }
        if(item.getCategoryId() == 9L){//岸上观光
        	sb.append(item.getProductName() + COMMA + item.getQuantity() + "份。");
        }
        if(item.getCategoryId()==10L){//游轮附加
        	sb.append(item.getProductName() + COMMA + item.getQuantity() + "份。");
        }
		if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L || item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
			//门票
			
			boolean isLvji = false;
			String lvjiSupplierId = Constant.getInstance().getProperty(
					"lvji.supplierId");
			try {
				isLvji = (StringUtils.isNotBlank(lvjiSupplierId) && Long
						.parseLong(lvjiSupplierId) == item.getSupplierId());
			} catch (Exception e) {
				e.printStackTrace();
				isLvji = false;
			}


            String invalidDate = "";
            if(item.getAperiodicUnvalidDesc()!=null &&!"".equals(item.getAperiodicUnvalidDesc())){
                invalidDate = "不适用日期 ：" + item.getAperiodicUnvalidDesc();
            }else if(item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())){
                invalidDate = "不适用日期 ：" + item.getInvalidDate();
            }
			String ticketTime = getTicketTimeStr(timeMap, item);
			String enterStyle = ""; // 入园方式
			if(enterStyleMap != null){
				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
				if(enterStyle != null && !"".equals(enterStyle.trim())){
					enterStyle = "，入园方式：" + enterStyle;
				}else{
					enterStyle = "";
				}
			}
            String goodExpInfo = getGoodsExpString(item);

			String address = "";
			String visitAddress = "";
			String customSms = "";
			if(addressMap != null && addressMap.isEmpty()==false){
				address = addressMap.get(item.getOrderItemId().toString());
				visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
				if(address != null && !"".equals(address.trim())){
    				address = "，取票地点：" + address;
    			}else{
    				//address = "";
    				if(StringUtils.isNotBlank(visitAddress)){
    					address = "，入园地点：" + visitAddress;
    				}
    			}
			}
			if(customSmsMap != null){
				customSms = customSmsMap.get(item.getOrderItemId().toString());
    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
    				customSms = COMMA+customSms.substring(0, 199);
    			}else if(StringUtil.isEmptyString(customSms.trim())){
    				customSms = "";
    			}
			}

			String personContent = "";
			long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
			long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
			if(adultNum!=0||childNum!=0){
				personContent = "，包含人数";
				if(adultNum!=0&&childNum!=0){
					personContent = personContent + adultNum + "成人、" + childNum + "儿童";
				}
				if(adultNum!=0&&childNum==0){
					personContent = personContent + adultNum + "成人";
				}
				if(adultNum==0&&childNum!=0){
					personContent = personContent + childNum + "儿童";

				}
			}

			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)) {


				String passLimitTime = "";
				if (timeMap != null) {
					passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
					if (StringUtils.isBlank(passLimitTime)) {
						passLimitTime = "";
					}else{
						passLimitTime=","+passLimitTime;
					}
				}
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
						|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)){
					passLimitTime="";
				}
                if(item.hasTicketAperiodic()){
					if(item.getCategoryId()==13L){
						String oldticketTIme = ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd");
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + goodExpInfo + invalidDate + address + enterStyle  + passLimitTime + customSms + " ");
    						}else{
                                //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                if (isLvji) {
									sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
                                } else {
                                    sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime +customSms + " ");
                                }
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
						}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" +  goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}
    						else
    						{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" +  goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" +  "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
						}
					}else{
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){

							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
    						}else {

                                //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                if (isLvji) {
                                    sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
                                } else {
                                    sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + goodExpInfo + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
                                }
    						}
    					}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。"  + goodExpInfo + invalidDate + passLimitTime + customSms + " ");
    						}else{
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "。"  + goodExpInfo + invalidDate + passLimitTime + customSms + " ");

    						}
    					}
					}
				}else{
					if(item.getCategoryId()==13L){
						if(isPayed){
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
    						}
    						else{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime + customSms + " ");
    						}
    					}else{
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  passLimitTime + customSms + " ");

    						}else{
    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  passLimitTime + customSms + " ");
    						}
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"+ "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  customSms + " ");
						}
					}else{
						if(isPayed){
							//增加演出票，支付成功，节点的信息
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +fillShowticketSb(itemProdProducts, item) + address + enterStyle + passLimitTime + customSms + " ");
    						}else{
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + fillShowticketSb(itemProdProducts, item) + address + enterStyle + passLimitTime +  customSms + " ");
    						}
						}else{
							//增加演出票，审核成功+预付，节点的逻辑
							if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){ //need alter
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+ fillShowticketShowTime( item) + passLimitTime + customSms + " ");
    						}else{
    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + fillShowticketShowTime(item) + passLimitTime + customSms + " ");
    						}
						}
					}
				}
			}else{
				String passLimitTime = "";
				if (timeMap != null) {
					passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
					if (StringUtils.isBlank(passLimitTime)) {
						passLimitTime = "";
					}else{
						passLimitTime=","+passLimitTime;
					}
				}
				if(item.hasTicketAperiodic()){
					if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "," +goodExpInfo + invalidDate + passLimitTime);
						}else{
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "," + goodExpInfo + invalidDate + passLimitTime);
						}
						//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
    				}else{
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "," + goodExpInfo + invalidDate + passLimitTime);
						}else{
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "," + goodExpInfo + invalidDate + passLimitTime);
						}
    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
    				}
    			}else{
    				if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}else{
	    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}
    					//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
    				}else{
						if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
	    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}else{
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime);
						}
    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
    				}
    			}
			}
			if(sendNode != null && addressMap != null && addressMap.isEmpty()==false && enterStyleMap != null && enterStyleMap.isEmpty()==false && customSmsMap != null && customSmsMap.isEmpty()==false){
				if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)){
					
					address = addressMap.get(item.getOrderItemId().toString());
					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					if(address != null && !"".equals(address.trim())){
    					address = "，取票地址：" + address;
    				}else{
    					//address = "";
    					if(StringUtils.isNotBlank(visitAddress)){
    						address = "，入园地址：" + visitAddress;
    					}
    				}

					enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
					if(enterStyle != null && !"".equals(enterStyle.trim())){
						enterStyle = "，入园方式：" + enterStyle;
    				}else{
    					enterStyle = "";
    				}
					sb.append(address + enterStyle);

    				customSms = customSmsMap.get(item.getOrderItemId().toString());
    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
    					customSms = customSms.substring(0, 199);
    				}else if(StringUtil.isEmptyString(customSms.trim())){
    					customSms = "";
    				}
    				//入园限制时间
                    if (timeMap != null) {
                        String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                        if(StringUtils.isNotBlank(passLimitTime)){
                            passLimitTime= ","+passLimitTime;
                            sb.append(passLimitTime);
                        }
                    }


                    if(StringUtils.isNotEmpty(customSms)){
    					sb.append(COMMA + customSms + " ");
    				}else{
    					sb.append(" ");
    				}
				}
			}else{
	//			sb.append("。");
			}
		}
        if(item.getCategoryId() == 15L || item.getCategoryId()==16L || item.getCategoryId() == 17L//线路大类
        		|| item.getCategoryId() == 18L || item.getCategoryId() == 42L){//新增加定制游
        	ProdProduct product = itemProdProducts.get(item.getOrderItemId().toString());

        	LOG.info("SmsUtil-->appendCommon-->orderId:"
    				+ item.getOrderId()
    				+ "item info:" + item.getOrderItemId() + "||" + item.getProductId()
    				+ "||" + item.getCategoryId() + "||" + item.getBranchId()
    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
    				+ "||product whether exist:" + (product == null ? "null" : "is not null")
    			);

        	if(product != null){

        		sb.append(item.getProductName() + "，出游日期：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
        		if(!StringUtils.isEmpty(frontBusStop)){
        			sb.append(","+frontBusStop);
        		}



        		//地址
        		if(product.getPropValue() != null && (product.getPropValue().get("address") != null) && (StringUtil.isNotEmptyString(product.getPropValue().get("address").toString()))){
        			sb.append(COMMA + product.getPropValue().get("address"));
        		}

        		//获得供应商紧急电话   添加条件 ：跟团游，国内短线，供应商打包
        		if((item.getCategoryId() == 15L && product.getPackageType().equals("SUPPLIER") &&
        				product.getProductType().equals("INNERSHORTLINE"))){
        			if(buildBuyCount != null){
                		sb.append("，订购"+buildBuyCount+"份");
                	}

        			Date beginDate = item.getVisitTime();
        			Calendar date = Calendar.getInstance();
        			date.setTime(beginDate);
        			date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);

        			String str = DateUtil.formatDate(date.getTime(), "yyyy-MM-dd");

        			
        			sb.append("，我们将在"+ str + "，21:00前，通过短信将车牌号及座位号发送给您，请保持手机畅通！");

        			if (itemSuppsupplier!=null && itemSuppsupplier.size()>0) {
        				SuppSupplier suppSupplier = itemSuppsupplier.get(item.getOrderItemId().toString());
        				if(!StringUtils.isEmpty(suppSupplier.getSupplierTel()) && !"".equals(suppSupplier.getSupplierTel())){
        					sb.append("应急电话："+suppSupplier.getSupplierTel()+"。");
        				}
					}
        		}
        		//获得供应商紧急电话   添加条件 ：当地游，国内短线（含国内），供应商打包
        		if((item.getCategoryId() == 16L && product.getPackageType().equals("SUPPLIER") &&
        				(product.getProductType().equals("INNERSHORTLINE") || product.getProductType().equals("INNERLINE")))){
        			if(buildBuyCount != null){
        				sb.append("，订购"+buildBuyCount+"份");
        			}
        			
        			Date beginDate = item.getVisitTime();
        			Calendar date = Calendar.getInstance();
        			date.setTime(beginDate);
        			date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
        			
        			String str = DateUtil.formatDate(date.getTime(), "yyyy-MM-dd");
        			
        			ProdTrafficVO prodTrafficVO =	product.getProdTrafficVO();
        			   if(prodTrafficVO!=null && prodTrafficVO.getProdTrafficGroupList()!=null &&
                       		prodTrafficVO.getProdTrafficGroupList().get(0)!=null){ 
        				   LOG.info("ProdTrafficGroup==");
                       	    ProdTrafficGroup group  =	prodTrafficVO.getProdTrafficGroupList().get(0);
                       	   LOG.info("ProdTrafficGroup=="+group);
                       		if(group.getProdTrafficBusList()!=null
                       		&&group.getProdTrafficBusList().size()>0){
           			        sb.append("，我们将在"+ str + "，21:00前，通过短信将车牌号及座位号发送给您，请保持手机畅通！");
           			        }
                       }
        			LOG.info("get 16 itemSuppsupplier ="+ itemSuppsupplier);
        			LOG.info("get 16 item.getOrderItemId ="+ item.getOrderItemId());
        			if (itemSuppsupplier!=null && itemSuppsupplier.size()>0) {
        				SuppSupplier suppSupplier = itemSuppsupplier.get(item.getOrderItemId().toString());
        				LOG.info("get 16 suppSupplier ="+ suppSupplier);
        				if(!StringUtils.isEmpty(suppSupplier.getSupplierTel()) && !"".equals(suppSupplier.getSupplierTel())){
        					sb.append("应急电话："+suppSupplier.getSupplierTel()+"。");
        				}
					}
        		}
        	}
        }
        //交通接驳
        if(item.getCategoryId()==41L){
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份"+COMMA+"使用时间 ："+ DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+"");
        }
        //当地玩乐 美食 娱乐 购物
        if(Long.valueOf(43L).equals(item.getCategoryId()) || Long.valueOf(44L).equals(item.getCategoryId()) || Long.valueOf(45L).equals(item.getCategoryId())){
        	sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "份"+COMMA+"使用时间 ："+ DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
			if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)
					|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)) {
				
				try {
					sb.append(getPlayCustomSms(customSmsMap,item));
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}
				
			}
        }
		if(item.getCategoryId()	==	19L
				|| item.getCategoryId() == 22L || item.getCategoryId() == 23L
				|| item.getCategoryId() == 24L || item.getCategoryId() == 25L
				|| item.getCategoryId() == 26L || item.getCategoryId() == 27L
				|| item.getCategoryId() == 90L||item.getCategoryId() == 28L){
			sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份。");
		}else if(item != null
        		&& (BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(item.getCategoryId())
        		|| BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(item.getCategoryId()))){//@todo机票分支
        	appendFlightCommonForRouteGroup(sb,item);
		}
	}

    private static String getGoodsExpString(OrdOrderItem item) {
        String goodExpInfo = item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name());
        if(goodExpInfo==null){
            return "";
        }
        return "有效期:"+goodExpInfo;
    }

    /**
	 * 拼接机票相关短信信息
	 * @param sb
	 * @param item
	 */
	private static StringBuffer appendFlightCommon(StringBuffer sb,OrdOrderItem item){
		if(item == null){
			return sb;
		}
		final String BACK = ".2";//返程的字段是去程后面加.2
		boolean isApiFlight = item.isApiFlightTicket();//是否是对接机票
		if (StringUtil.isNotEmptyString(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name()))
				|| StringUtil.isNotEmptyString(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name()+BACK))) {
			int j = isApiFlight ? 0 : 1;
			for(int i=0 ; i <= j ; i++){
				String flightNo = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.flightNo.name() : OrderEnum.ORDER_COMMON_TYPE.flightNo.name()+BACK;
				String airCompany = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.airCompany.name() : OrderEnum.ORDER_COMMON_TYPE.airCompany.name()+BACK;
				String fromAirport = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.fromAirport.name() : OrderEnum.ORDER_COMMON_TYPE.fromAirport.name()+BACK;
				String startTerminal = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.startTerminal.name() : OrderEnum.ORDER_COMMON_TYPE.startTerminal.name()+BACK;
				String departureTime = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.departureTime.name() : OrderEnum.ORDER_COMMON_TYPE.departureTime.name()+BACK;
				String toAirport = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.toAirport.name() : OrderEnum.ORDER_COMMON_TYPE.toAirport.name()+BACK;
				String arriveTerminal = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name() : OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name()+BACK;
				String arriveTime = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveTime.name() : OrderEnum.ORDER_COMMON_TYPE.arriveTime.name()+BACK;
				String departureCity = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.departureCity.name() : OrderEnum.ORDER_COMMON_TYPE.departureCity.name()+BACK;
				String arriveCity = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() : OrderEnum.ORDER_COMMON_TYPE.arriveCity.name()+BACK;

				if(StringUtil.isEmptyString(item.getContentStringByKey(flightNo))){
					continue;
				}else{
					String departureDayTime = item.getContentStringByKey(departureTime);//起飞日期与时间
					String arriveDayTime =  item.getContentStringByKey(arriveTime);//到达日期与时间
					if(StringUtil.isNotEmptyString(departureDayTime)){
						String[] fromDateTime = departureDayTime.split(SPACE);
						if(fromDateTime != null && fromDateTime.length == 2){
							sb.append(fromDateTime[0]).append(SPACE);
						}
					}
					//往返城市信息如:长春-杭州
					/*if(StringUtil.isNotEmptyString(item.getProductName())){
						sb.append(item.getProductName()).append(SPACE);
					}*/
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(departureCity)) && StringUtil.isNotEmptyString(item.getContentStringByKey(arriveCity))){
						sb.append(item.getContentStringByKey(departureCity)).append(LINE).append(item.getContentStringByKey(arriveCity)).append(SPACE);
					}
					
					//份数
					if(item.getQuantity() != null && item.getQuantity().intValue() > 0){
						sb.append(item.getQuantity()).append("份").append(SPACE);
					}
						
					//航空公司
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(airCompany))){
						sb.append(item.getContentStringByKey(airCompany)).append(SPACE);
					}
					//航班号如:HU7365-U
					/*if(StringUtil.isNotEmptyString(item.getSuppGoodsName())){
						sb.append(item.getSuppGoodsName()).append(SPACE);
					}*/
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(flightNo))){
						sb.append(item.getContentStringByKey(flightNo)).append(SPACE);
					}
					//起飞机场
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(fromAirport))){
						sb.append(item.getContentStringByKey(fromAirport));
					}
					//起飞航站楼
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(startTerminal))){
						sb.append(item.getContentStringByKey(startTerminal)).append(SPACE);
					}
					//起飞时间
					if(StringUtil.isNotEmptyString(departureDayTime)){
						String[] fromDateTime = departureDayTime.split(SPACE);
						if(fromDateTime != null && fromDateTime.length == 2){
							sb.append(fromDateTime[1]).append(LINE);
						}
					}
					//到达机场
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(toAirport))){
						sb.append(item.getContentStringByKey(toAirport));
					}
					//到达航站楼
					if(StringUtil.isNotEmptyString(item.getContentStringByKey(arriveTerminal))){
						sb.append(item.getContentStringByKey(arriveTerminal)).append(SPACE);
					}
					//到达时间
					if(StringUtil.isNotEmptyString(arriveDayTime)){
						String[] toDateTime = arriveDayTime.split(SPACE);
						if(toDateTime != null && toDateTime.length == 2){
							sb.append(toDateTime[1]).append(SPACE);
						}
					}
				}
			}
		}else{
			sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份");
		}
		return sb;
	}

	 /**
		 * 拼接机票相关短信信息
		 * @param sb
		 * @param item
		 */
		private static StringBuffer appendFlightCommonForRouteGroup(StringBuffer sb,OrdOrderItem item){
			if(item == null){
				return sb;
			}
			final String BACK = ".2";//返程的字段是去程后面加.2
			boolean isApiFlight = item.isApiFlightTicket();//是否是对接机票
			if (StringUtil.isNotEmptyString(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name()))
					|| StringUtil.isNotEmptyString(item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name()+BACK))) {
				int j = isApiFlight ? 0 : 1;
				for(int i=0 ; i <= j ; i++){
					String flightNo = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.flightNo.name() : OrderEnum.ORDER_COMMON_TYPE.flightNo.name()+BACK;
					String airCompany = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.airCompany.name() : OrderEnum.ORDER_COMMON_TYPE.airCompany.name()+BACK;
					String fromAirport = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.fromAirport.name() : OrderEnum.ORDER_COMMON_TYPE.fromAirport.name()+BACK;
					String startTerminal = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.startTerminal.name() : OrderEnum.ORDER_COMMON_TYPE.startTerminal.name()+BACK;
					String departureTime = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.departureTime.name() : OrderEnum.ORDER_COMMON_TYPE.departureTime.name()+BACK;
					String toAirport = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.toAirport.name() : OrderEnum.ORDER_COMMON_TYPE.toAirport.name()+BACK;
					String arriveTerminal = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name() : OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name()+BACK;
					String arriveTime = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveTime.name() : OrderEnum.ORDER_COMMON_TYPE.arriveTime.name()+BACK;
					String departureCity = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.departureCity.name() : OrderEnum.ORDER_COMMON_TYPE.departureCity.name()+BACK;
					String arriveCity = i == 0 ? OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() : OrderEnum.ORDER_COMMON_TYPE.arriveCity.name()+BACK;

					if(StringUtil.isEmptyString(item.getContentStringByKey(flightNo))){
						continue;
					}else{
						String departureDayTime = item.getContentStringByKey(departureTime);//起飞日期与时间
						String arriveDayTime =  item.getContentStringByKey(arriveTime);//到达日期与时间
						if(StringUtil.isNotEmptyString(departureDayTime)){
							String[] fromDateTime = departureDayTime.split(SPACE);
							if(fromDateTime != null && fromDateTime.length == 2){
								sb.append(fromDateTime[0]).append(SPACE);
							}
						}
						//往返城市信息如:长春-杭州
						/*if(StringUtil.isNotEmptyString(item.getProductName())){
							sb.append(item.getProductName()).append(SPACE);
						}*/
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(departureCity)) && StringUtil.isNotEmptyString(item.getContentStringByKey(arriveCity))){
							sb.append(item.getContentStringByKey(departureCity)).append(LINE).append(item.getContentStringByKey(arriveCity)).append(SPACE);
						}
						
						//航空公司
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(airCompany))){
							sb.append(item.getContentStringByKey(airCompany)).append(SPACE);
						}
						//航班号如:HU7365-U
						/*if(StringUtil.isNotEmptyString(item.getSuppGoodsName())){
							sb.append(item.getSuppGoodsName()).append(SPACE);
						}*/
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(flightNo))){
							sb.append(item.getContentStringByKey(flightNo)).append(SPACE);
						}
						//起飞机场
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(fromAirport))){
							sb.append(item.getContentStringByKey(fromAirport));
						}
						//起飞航站楼
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(startTerminal))){
							sb.append(item.getContentStringByKey(startTerminal)).append(SPACE);
						}
						//起飞时间
						if(StringUtil.isNotEmptyString(departureDayTime)){
							String[] fromDateTime = departureDayTime.split(SPACE);
							if(fromDateTime != null && fromDateTime.length == 2){
								sb.append(fromDateTime[1]).append(LINE);
							}
						}
						//到达机场
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(toAirport))){
							sb.append(item.getContentStringByKey(toAirport));
						}
						//到达航站楼
						if(StringUtil.isNotEmptyString(item.getContentStringByKey(arriveTerminal))){
							sb.append(item.getContentStringByKey(arriveTerminal)).append(SPACE);
						}
						//到达时间
						if(StringUtil.isNotEmptyString(arriveDayTime)){
							String[] toDateTime = arriveDayTime.split(SPACE);
							if(toDateTime != null && toDateTime.length == 2){
								sb.append(toDateTime[1]).append(SPACE);
							}
						}
					}
				}
			}else{
				sb.append(item.getProductName() + COMMA + item.getSuppGoodsName() + COMMA + item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name()) + COMMA + item.getQuantity() + "份");
			}
			return sb;
		}
	
	/**
	 * 填充订单内容-订单提交-预付-未支付    或者    审核成功-预付-未支付
	 * @param content
	 * @param order
	 * @param product
	 * @param sendNode           发送节点
	 * @param addressMap         取票地址
	 * @param customSmsMap       自定义短信
	 * @param hotelProdProducts  酒店产品列表
	 * @param frontBusStop       上车地点信息
	 * @return
	 */
	public static String fillSmsForPrepaidUnPayed(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> customSmsMap, Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<Long,String > specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForPrepaidUnPayed:orderId=" + order.getOrderId());
		return fillSmsForPrepaid(content, order, sendNode, addressMap,null,null, customSmsMap, itemProdProducts, frontBusStop,null,null,false,specialSmsMap,false);
	}

	public static String fillSmsForTicketPrepaidUnPayed(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap,Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap, Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<Long,String> specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForPrepaidUnPayed:orderId=" + order.getOrderId());
		return fillSmsForPrepaid(content, order, sendNode, addressMap, enterStyleMap, timeMap, customSmsMap, itemProdProducts, frontBusStop,null,null,false,specialSmsMap,false);
	}


	/**
	 * 支付完成-预付
	 * @param content
	 * @param order
	 * @param product
	 * @param sendNode           发送节点
	 * @param addressMap         取票地址
	 * @param enterStyleMap      入园方式
	 * @param customSmsMap       自定义短信
	 * @param hotelProdProducts  酒店产品列表
	 * @param frontBusStop       上车地点信息
	 * @return
	 */
	public static String fillSmsForPrepaidPayed(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap,Map<String,String> customSmsMap,
			Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<String, SuppSupplier> itemSuppsupplier,Integer buildBuyCount,Map<Long,String> specialSmsMap,boolean ifYL) {
		LOG.info("SmsUtil.fillSmsForPrepaidPayed:orderId=" + order.getOrderId());
		LOG.info("itemSuppsupplier info  is "+ itemSuppsupplier);
		LOG.info("buildBuyCount info  is "+ buildBuyCount);
		return fillSmsForPrepaid(content, order, sendNode, addressMap, enterStyleMap, timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,true,specialSmsMap,ifYL);
	}

	/**
	 * 填充订单内容-订单提交-预付-未支付    或者    审核成功-预付-未支付   或者 	支付完成-预付
	 * @param content
	 * @param order
	 * @param product
	 * @param sendNode           发送节点
	 * @param addressMap         取票地址
	 * @param enterStyleMap      入园方式
	 * @param customSmsMap       自定义短信
	 * @param hotelProdProducts  酒店产品列表
	 * @param frontBusStop       上车地点信息
	 * @return
	 */
	public static String fillSmsForPrepaid(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap,
			Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<String, SuppSupplier> itemSuppsupplier,Integer buildBuyCount,boolean isPayed,Map<Long,String> specialSmsMap,boolean ifYL) {
		LOG.info("SmsUtil.fillSmsForPrepaid:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
	    //打包
		int itemSize = 0;
		int i = 0;
		List<OrdOrderPack> packList = order.getOrderPackList();
		if(packList!=null&&packList.size()>0){
			OrdOrderPack ordPack =  packList.get(0);
			List<OrdOrderItem> itemList = ordPack.getOrderItemList();
			if(itemList!=null&&itemList.size()>0){
				itemSize = itemList.size();
			}
		}
	    if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
	    	for(OrdOrderPack pack : order.getOrderPackList()){
	    		if(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
	    			for(OrdOrderItem item : pack.getOrderItemList()){
	    				i++;
	    				if(itemSize>1){
	    					sb.append("门票"+i+"：");
	    				}
                        String invalidDate = "";
                        if(item.getAperiodicUnvalidDesc()!=null &&!"".equals(item.getAperiodicUnvalidDesc())){
                            invalidDate = "不适用日期 ：" + item.getAperiodicUnvalidDesc();
                        }else if(item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())){
                            invalidDate = "不适用日期 ：" + item.getInvalidDate();
                        }
                        String goodsExpInfo = getGoodsExpString(item);
						String ticketTime = getTicketTimeStr(timeMap, item);
						String enterStyle = ""; // 入园方式
	    				if(enterStyleMap != null){
		    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
							if(enterStyle != null && !"".equals(enterStyle.trim())){
								enterStyle = "，入园方式：" + enterStyle;
		    				}else{
		    					enterStyle = "";
		    				}
	    				}
	    				String address = "";
	    				String visitAddress = "";
	    				String customSms = "";
	    				if(addressMap != null && addressMap.isEmpty()==false){
	    					address = addressMap.get(item.getOrderItemId().toString());
	    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
	    					if(address != null && !"".equals(address.trim())){
	    	    				address = "，取票地点：" + address;
	    	    			}else{
	    	    				//address = "";
	    	    				if(StringUtils.isNotBlank(visitAddress)){
	    	    					address = "，入园地点：" + visitAddress;
	    	    				}
	    	    			}
	    				}
	    				//门票增加入园限制时间
                        String passLimitTime="";
                        if (timeMap != null) {
                             passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                            if(StringUtils.isNotBlank(passLimitTime)){
                                passLimitTime= ","+passLimitTime;
                            }
                        }


                        if(customSmsMap != null){
	    					customSms = customSmsMap.get(item.getOrderItemId().toString());
	    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
	    	    				customSms = COMMA+customSms.substring(0, 199);
	    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
	    	    				customSms = "";
	    	    			}
	    				}

	    				String personContent = "";
	    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
	    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
	    				if(adultNum!=0||childNum!=0){
	    					personContent = "，包含人数";
	    					if(adultNum!=0&&childNum!=0){
	    						personContent = personContent + adultNum + "成人、" + childNum + "儿童";
	    					}
	    					if(adultNum!=0&&childNum==0){
	    						personContent = personContent + adultNum + "成人";
	    					}
	    					if(adultNum==0&&childNum!=0){
	    						personContent = personContent + childNum + "儿童";
	    					}
	    				}

						if (sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID)
								|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)
								|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY)
								|| sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_OUTBOUND_FREED)) {

							if(sendNode.equals(OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID) || sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID)){
								passLimitTime="";
							}
                            String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");
                            LOG.info("fillSmsForPrepaid: lvji supplier id:" + lvjiSupplierId);

                            boolean isLvji = (null != lvjiSupplierId && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
		    				if(item.hasTicketAperiodic()){
		    					if(item.getCategoryId()==13L){
		    						if(isPayed){
										 String expiryDate = ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd");
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
											sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + goodsExpInfo + invalidDate + address + enterStyle + passLimitTime + customSms + " ");
			    						}else{
                                            //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                            if (isLvji) {
                                                sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodsExpInfo + invalidDate + passLimitTime+ customSms + " ");
                                            } else {
                                                sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + goodsExpInfo + invalidDate + address + enterStyle + passLimitTime+ customSms + " ");
                                            }
                                        }
		    							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + goodsExpInfo + invalidDate + address + enterStyle  + customSms + " ");
		    						}else{
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + goodsExpInfo + invalidDate +  passLimitTime+ customSms + " ");
			    						}
			    						else{
			    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "。" + goodsExpInfo + invalidDate +  passLimitTime+ customSms + " ");
			    						}
		    							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" +  "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
		    						}
		    					}else{
		    						if(isPayed){
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + goodsExpInfo + invalidDate + address + enterStyle + passLimitTime+ customSms + " ");
			    						}else
			    						{
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "。" + goodsExpInfo + invalidDate + address + enterStyle + passLimitTime+  customSms + " ");
			    						}
		    							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
		    						}else{
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + goodsExpInfo + invalidDate +  passLimitTime+ customSms + " ");
			    						}else{
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "。" + goodsExpInfo + invalidDate + passLimitTime+ customSms + " ");

			    						}
		    							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"  + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
		    						}
		    					}
		    				}else{
		    					if(item.getCategoryId()==13L){
		    						if(isPayed){
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime+ customSms + " ");
			    						}else{
                                            //lvji guide ticket, do not need get ticket address, enter style, enter address.
                                            if (isLvji) {
                                                sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime+ customSms + " ");
                                            } else {
                                                sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime+ customSms + " ");
                                            }
			    						}
		    							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
		    						}else{
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime+  customSms + " ");
			    						}else{
			    							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime+  customSms + " ");

			    						}
		    							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"+ "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  customSms + " ");
		    						}
		    					}else{
		    						if(isPayed){
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime+  customSms + " ");
			    						}else{
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + passLimitTime+ customSms + " ");

			    						}
		    							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
		    						}else{
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime+ customSms + " ");
			    						}else{
			    							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + passLimitTime+ customSms + " ");

			    						}
		    							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + customSms + " ");
		    						}
		    					}
		    				}
						}else{
							if(item.hasTicketAperiodic()){
		    					if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
									if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
			    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "," +goodsExpInfo+ invalidDate +passLimitTime);
		    						}
		    						else{
			    						sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "," +goodsExpInfo + invalidDate+" "+passLimitTime);
		    						}
		    						//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
			    				}else{
									if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
				    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "," +goodsExpInfo + invalidDate + passLimitTime);
		    						}else{
				    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity()  + "," +goodsExpInfo + passLimitTime);

		    						}
			    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
			    				}
			    			}else{
			    				if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
									if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
				    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+" "+passLimitTime);
		    						}else{
				    					sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity()  + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+" "+passLimitTime);

		    						}
			    					//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
			    				}else{
									if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
				    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+" "+passLimitTime);

		    						}else{
				    					sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")+" "+passLimitTime);
		    						}
			    					//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
			    				}
			    			}
						}
	    				if(sendNode != null && addressMap != null && addressMap.isEmpty()==false && enterStyleMap != null && enterStyleMap.isEmpty()==false && customSmsMap != null && customSmsMap.isEmpty()==false){
	    					if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
	    						|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)){
	    						address = addressMap.get(item.getOrderItemId().toString());
	    						visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
	    						if(address != null && !"".equals(address.trim())){
	    	    					address = "，取票地址：" + address;
	    	    				}else{
	    	    					//address = "";
	    	    					if(StringUtils.isNotBlank(visitAddress)){
	    	    						address = "，入园地址：" + visitAddress;
	    	    					}
	    	    				}

	    						enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
	    						if(enterStyle != null && !"".equals(enterStyle.trim())){
	    							enterStyle = "，入园方式：" + enterStyle;
	    	    				}else{
	    	    					enterStyle = "";
	    	    				}
	    						sb.append(address + enterStyle);
								if(StringUtils.isNotBlank(passLimitTime)) {
									sb.append(","+passLimitTime);
								}
	    	    				customSms = customSmsMap.get(item.getOrderItemId().toString());
	    	    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
	    	    					customSms = customSms.substring(0, 199);
	    	    				}else if(StringUtil.isEmptyString(customSms.trim())){
	    	    					customSms = "";
	    	    				}
	    	    				if(StringUtils.isNotEmpty(customSms)){
	    	    					sb.append(COMMA + customSms + " ");
	    	    				}else{
	    	    					sb.append(" ");
	    	    				}
	    					}
	    				}else{
	    			//		sb.append("。");
	    				}
	    			}
	    		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    					||BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    					||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    					||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
	    					||BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))//新增加定制游
	    				){
	    			LOG.info("SmsUtil-->fillSmsForPrepaid-->orderId:" + order.getOrderId()
	    						+ "line product pack"
	    					);
	    			//排序是为了保证短信内容一致
	    			Collections.sort(pack.getOrderItemList(), new Comparator<OrdOrderItem>() {
						@Override
						public int compare(OrdOrderItem arg0, OrdOrderItem arg1) {
							return (int)(arg0.getProductId()-arg1.getProductId());
						}
					});
	    			boolean isLocalOrDestination=false;
	    			if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
							Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
	    				isLocalOrDestination=true;
	    			}
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))&&isLocalOrDestination){
	    				List<OrdOrderItem> items=new ArrayList<OrdOrderItem>();
	    				boolean isback=true;
	    				for (OrdOrderItem item : pack.getOrderItemList()) {
							/*i++;
		    				if(itemSize>1){
		    					sb.append("门票"+i+"：");
		    				}*/
							LOG.info("SmsUtil-->fillSmsForPrepaid-->orderId:"
				    				+ order.getOrderId()
				    				+ "pack item info:" + item.getOrderItemId() + "||" + item.getProductId()
				    				+ "||" + item.getCategoryId() + "||" + item.getBranchId()
				    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
				    			);
							String flightType = item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightType.name());
							if(null!=flightType&&flightType.equals("2")&&isback){
								items.add(item);
							}else{
								appendCommonForRouteGroup(sb, item, sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap);
								if(null!=flightType&&flightType.equals("1")){
									isback=false;
									if(null!=items&&items.size()>0){
										appendCommonForRouteGroup(sb, items.get(0), sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap);
									}
								}
							}	
						}	
	    			}else{
						for (OrdOrderItem item : pack.getOrderItemList()) {
							/*i++;
		    				if(itemSize>1){
		    					sb.append("门票"+i+"：");
		    				}*/
							LOG.info("SmsUtil-->fillSmsForPrepaid-->orderId:"
				    				+ order.getOrderId()
				    				+ "pack item info:" + item.getOrderItemId() + "||" + item.getProductId()
				    				+ "||" + item.getCategoryId() + "||" + item.getBranchId()
				    				+ "||" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd")
				    			);
							appendCommon(sb, item, sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap,ifYL);
						}	
	    			}	
	    		}else{
	    			sb.append(pack.getProductName()+ "，出游日期：" + DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd") + "。");
	    		}
	    	}
	    }
	    if(OrderUtil.isWifiCategory(order.getMainOrderItem())){
	    	OrderUtil.wifiItemSort(order);
	    }
	    //非打包
	    if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
	    	boolean isLocalOrDestination=false;
			if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
					Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
				isLocalOrDestination=true;
			}
	    	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(order.getCategoryId().longValue())&&isLocalOrDestination){
	    		List<OrdOrderItem> items=new ArrayList<OrdOrderItem>();
	    		boolean isback=true;
	    		for(OrdOrderItem item : order.getOrderItemList()){
	    			String flightType = item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightType.name());
					if(null!=flightType&&flightType.equals("2")&&isback){
						items.add(item);
					}else{
						appendCommonForRouteGroup(sb, item, sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap);
						if(null!=flightType&&flightType.equals("1")){
							isback=false;
							if(null!=items&&items.size()>0){
								appendCommonForRouteGroup(sb, item, sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap);
							}
						}
					}
	    		}
	    	}else{
	    		for(OrdOrderItem item : order.getOrderItemList()){
		    		if(item.getOrderPackId() == null){//非打包产品
		    			appendCommon(sb, item, sendNode, addressMap, enterStyleMap,timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,isPayed,order,specialSmsMap,ifYL);
		    		}
		    	}
	    	}
	    }
	    //订单编号orderId

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

		//最晚支付时间latestPaymentTime

		//担保方式cancelStrategy
		if (!isCancelStrategy(order)) {
			String cancelStrategy = order.getRealCancelStrategy();
			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			if (BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(order.getCategoryId())) {//定制游
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				cancelStrategy = order.getCancelStrategy();
			}
			if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			//当地玩乐 美食 娱乐 购物 退改策略
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
		}

		String isToForeign = isToForeign(order);
		param.put("isToForeign", isToForeign);
		
		//客服电话customerServicePhone
		if("Y".equalsIgnoreCase(order.getSupplierApiFlag())){
			return fillSms(content, order, param).replace(REPLACEAPI, "");
		}
		
		return fillSms(content, order, param);
	}

	/**
	 * 填充订单内容-申码成功-现付-期票
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPayAperiodicTicket(String content, OrdOrder order, List<Long> orderItemList, Map<String, Object> map ,Map<String,String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<Long,String> specialSmsMap) {
		Map <String,Object> param = null;
		boolean isOne = (Boolean) map.get("ISONE");
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPayAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){//orderItemList只会存在一条匹配记录和order.getOrderItemList()匹配
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name())
									,SuppGoods.NOTICETYPE.QRCODE.name())){
								if(order.hasNeedPay()){//现付
									if(item.hasTicketAperiodic()){//期票

										if(item.getProductId() != null){
											if(!proSet.contains(item.getProductId())){
												proSet.add(item.getProductId());
												//产品名称
												if(item.getCategoryId()!=13){
												    sb.append(item.getProductName());
												}
											}
										}
										//商品名称
										sb.append(replaceGoodsName(item.getSuppGoodsName()));
										//份数
										sb.append("，份数：" + item.getQuantity());
										//包含人
										//当票种中人群数量为0时，则不显示。例如1成人0儿童则短信中显示1成人 ；0成人1儿童则显示1儿童；0成人0儿童则不做调取。
										String personContent = "";
										long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
										long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
										if(adultNum!=0||childNum!=0){
											personContent = "，包含人数";
											if(adultNum!=0&&childNum!=0){
												personContent = personContent + adultNum + "成人," + childNum + "儿童。";
											}
											if(adultNum!=0&&childNum==0){
												personContent = personContent + adultNum + "成人。";
											}
											if(adultNum==0&&childNum!=0){
												personContent = personContent + childNum + "儿童。";
											}
										}
										//sb.append("，包含人数"+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人,"+ calculateLong(item.getQuantity(), item.getChildQuantity())+ "儿童。" );

										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))) {
			    							sb.append(personContent);
										}

										//取票时间
//										String ticketTime = "";
//										if(ticketTime != null){
//											ticketTime = timeMap.get(item.getOrderItemId().toString());
//											if(ticketTime != null && !"".equals(ticketTime.trim())){
//												ticketTime = "取票时间：" + ticketTime+",";
//											}else{
//												ticketTime = "";
//											}
//										}
//										sb.append(ticketTime);
										//有效期
//										sb.append("有效期："+DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd")+"-"+DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd")+",");

										String termOfvalidity = getGoodsExpString(item);
										//有效期
										sb.append(termOfvalidity);

										//不适用日期
										if(null != item.getAperiodicUnvalidDesc() && !"".equals(item.getAperiodicUnvalidDesc())){
											sb.append("不适用日期："+item.getAperiodicUnvalidDesc());
										}else if(null != item.getInvalidDate() && !"".equals(item.getInvalidDate())){
                                            sb.append("不适用日期："+item.getInvalidDate());
                                        }

										String address = "";
										String visitAddress ="";
										if(addressMap != null && addressMap.isEmpty()==false){
					    					address = addressMap.get(item.getOrderItemId().toString());
					    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					    					if(address != null && !"".equals(address.trim())){
					    	    				address = "，取票地点：" + address;
					    	    			}else{
					    	    				//address = "";
					    	    				if(StringUtils.isNotBlank(visitAddress)){
					    	    					address = "，入园地点：" + visitAddress;
					    	    				}
					    	    			}
					    				}
										sb.append(address);

					    				String enterStyle = ""; // 入园方式
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
						    				}else{
						    					enterStyle = "";
						    				}
					    				}
					    				//入园方式
										sb.append(enterStyle);

										param = new HashMap<String, Object>();
										//二维码qrcode
										param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

										//辅助码auxiliaryCode
										if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
											if(isOne){
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
											}else{
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(i));
											}
										}
										//入园限制时间
                                        if (timeMap != null) {
                                            String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if(StringUtils.isNotBlank(passLimitTime)){
                                                passLimitTime= ","+passLimitTime;
                                                sb.append(passLimitTime);

                                            }
                                        }

                                        //商品自定义短信
										String customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
										if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
											customSms = customSms.substring(0, 199);
										}else if(StringUtil.isEmptyString(customSms.trim())){
											customSms = "";
										}
										param.put(OrdSmsTemplate.FIELD.GOODS_CUSTOM_SMS.getField(), customSms);
										param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
										//分销那边发短信，需要把最后的签名去掉
										if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
											param.put(SMSLVMAMAFLAG, N);
										}
										//客服电话customerServicePhone
										return fillSms(content, order, param);
									}
								}
							}
						}
					}
				}
			}

		}
		return content;
	}
	/**
	 * 填充订单内容-申码成功-现付-非期票
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPayNoAperiodicTicket(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map, Map<String,String> goodsCustomSmsMap) {
		Map <String,Object> param = null;
		boolean isOne = (Boolean) map.get("ISONE");
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPayNoAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name())
									,SuppGoods.NOTICETYPE.QRCODE.name())){
								if(order.hasNeedPay()){//现付
									if(!item.hasTicketAperiodic()){//非期票
										param = new HashMap<String, Object>();
										//二维码qrcode
										param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

										//辅助码auxiliaryCode
										if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
											if(isOne){
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
											}else{
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(i));
											}
										}

										//订单编号orderId

										//游玩时间activeTime
										param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

										//商品自定义短信
					    				String customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    					customSms = customSms.substring(0, 199);
					    				}else if(StringUtil.isEmptyString(customSms.trim())){
					    					customSms = "";
					    				}
										param.put(OrdSmsTemplate.FIELD.GOODS_CUSTOM_SMS.getField(), customSms);

										String isToForeign = item.getContentStringByKey(VstOrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name());
										param.put("isToForeign", isToForeign);
										
										//客服电话customerServicePhone
										return fillSms(content, order, param);
									}
								}
							}
						}
					}
				}
			}

		}
		return content;
	}



	/**
	 * 填充订单内容-申码成功-现付-非期票(修改内容)
	 * @param content
	 * @param order
	 * @param orderItemList
	 * @param map
	 * @param goodsCustomSmsMap
	 * @return
	 */
	public static String fillSmsForPayNoAperiodicTicket(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,Map<String,String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<String,ProdProduct> produProductMap,Map<Long,String> specialSmsMap){
 		Map <String,Object> param = null;
		boolean isOne = (Boolean) map.get("ISONE");
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPayNoAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13||item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name())
									,SuppGoods.NOTICETYPE.QRCODE.name())){ 
								if(order.hasNeedPay()){//现付     
									if(!item.hasTicketAperiodic()){//非期票 

										if(item.getProductId() != null){
											if(!proSet.contains(item.getProductId())){
												proSet.add(item.getProductId());
												//产品名称
												if(item.getCategoryId()!=13){
												    sb.append(item.getProductName());
												}
											}
										}
										//商品名称
										sb.append(replaceGoodsName(item.getSuppGoodsName()));
										//份数
										sb.append("，份数：" + item.getQuantity());

										String personContent = "";
										long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
										long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
										if(adultNum!=0||childNum!=0){
											personContent = "，包含人数";
											if(adultNum!=0&&childNum!=0){
												personContent = personContent + adultNum + "成人," + childNum + "儿童。";
											}
											if(adultNum!=0&&childNum==0){
												personContent = personContent + adultNum + "成人。";
											}
											if(adultNum==0&&childNum!=0){
												personContent = personContent + childNum + "儿童。";
											}
										}
										//包含人
										//sb.append("，包含人数"+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人,"+ calculateLong(item.getQuantity(), item.getChildQuantity())+ "儿童。" );

										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
											sb.append(personContent);
			    						}
										//取票时间
										String ticketTime = getTicketTimeStr(timeMap, item);
										sb.append(ticketTime);
										//游玩时间
										sb.append("游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
										//如果是演出票增加这一段逻辑
										String showTicketSb = fillShowticketSb(produProductMap, item);
										
										sb.append(showTicketSb);
										
										String address = "";
										String visitAddress = "";
										if(addressMap != null && addressMap.isEmpty()==false){
					    					address = addressMap.get(item.getOrderItemId().toString());
					    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					    					if(address != null && !"".equals(address.trim())){
					    	    				address = "，取票地点：" + address;
					    	    			}else{
					    	    				//address = "";
					    	    				if(StringUtils.isNotBlank(visitAddress)){
					    	    					address = "，入园地点：" + visitAddress;
					    	    				}
					    	    			}
					    				}
										sb.append(address);

					    				String enterStyle = ""; // 入园方式
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
						    				}else{
						    					enterStyle = "";
						    				}
					    				}
					    				//入园方式
										sb.append(enterStyle);

										param = new HashMap<String, Object>();
										//二维码qrcode
										param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

										//辅助码auxiliaryCode
										if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
											if(isOne){
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
											}else{
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(i));
											}
										}

										//入园限制时间
                                        if (timeMap != null) {
                                            String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if(StringUtils.isNotBlank(passLimitTime)){
                                                passLimitTime= ","+passLimitTime;
                                                sb.append(passLimitTime);
                                            }
                                        }


										//商品自定义短信
					    				String customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    					customSms = customSms.substring(0, 199);
					    				}else if(StringUtil.isEmptyString(customSms.trim())){
					    					customSms = "";
					    				}
					    				//产品信息productInfo
					    				param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
										param.put(OrdSmsTemplate.FIELD.GOODS_CUSTOM_SMS.getField(), customSms);

										//分销那边发短信，需要把最后的签名去掉
										if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
											param.put(SMSLVMAMAFLAG, N);
										}
										//客服电话customerServicePhone
										return fillSms(content, order, param);
									}
								}
							}
						}
					}
				}
			}

		}
		return content;
	}

	/**
	 * 填充演出票内容-方法
	 * @param produProductMap
	 * @param sb
	 * @param orderItemList订单子项列表
	 */
	private static  String fillShowticketSb(
			Map<String, ProdProduct> produProductMap, 
			OrdOrderItem item) {
		if(item==null){
			return "";
		}
		
		String visitTime = DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");
		if(StringUtils.isBlank(visitTime)){
			visitTime="";
		}else{
			visitTime =visitTime+",";
		}

		StringBuffer sb=new StringBuffer();
		if (item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket
				.getCategoryId()) {
			// 演出时间
			String showTicketShowTime = "";
			String startTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventStartTime
							.name());
			String endTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventEndTime
							.name());

			if (StringUtils.isNotBlank(endTime) && StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime + "至" + endTime;
			} else if (StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime;
			}

			if (StringUtils.isNotBlank(showTicketShowTime)) {
				sb.append("，演出时间："+ visitTime + showTicketShowTime);
			}
			if (produProductMap != null) {
				ProdProduct prodProduct = produProductMap.get(item.getOrderItemId().toString());
				if (prodProduct != null) {
					Map<String, Object> propValue = prodProduct.getPropValue();
					if (propValue != null) {
						Object takePlace = propValue.get("entrance_place");// 入场地点
						Object entranceMethod = propValue.get("entrance_method");// 入场方式
						if (takePlace!=null && StringUtils.isNotBlank(takePlace.toString())) {
							sb.append("，入场地点："+ takePlace);
						}
						if (entranceMethod!=null && StringUtils.isNotBlank(entranceMethod.toString())) {
							sb.append("，入场方式："+ entranceMethod);
						}
					}
				}
			}
		}
		return sb.toString();
	}
	
	
	/**
	 * 填充演出票内容-方法(永乐演出票)
	 * @param produProductMap
	 * @param sb
	 * @param orderItemList订单子项列表
	 */
	private static  String fillShowticketSbYL(
			Map<String, ProdProduct> produProductMap, 
			OrdOrderItem item) {
		if(item==null){
			return "";
		}
		LOG.info("fillShowticketSbYL,填充演出票内容-方法(永乐演出票),orderID:"+item.getOrderId());
		String visitTime = DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");
		if(StringUtils.isBlank(visitTime)){
			visitTime="";
		}else{
			visitTime =visitTime+",";
		}

		StringBuffer sb=new StringBuffer();
		if (item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket
				.getCategoryId()) {
			// 演出时间
			String showTicketShowTime = "";
			String startTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventStartTime
							.name());
			String endTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventEndTime
							.name());

			if (StringUtils.isNotBlank(endTime) && StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime + "至" + endTime;
			} else if (StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime;
			}

			if (StringUtils.isNotBlank(showTicketShowTime)) {
				sb.append("，演出时间："+ visitTime + showTicketShowTime);
			}
			if (produProductMap != null) {
				ProdProduct prodProduct = produProductMap.get(item.getOrderItemId().toString());
				if (prodProduct != null) {
					Map<String, Object> propValue = prodProduct.getPropValue();
					if (propValue != null) {
						Object takePlace = propValue.get("entrance_place");// 入场地点
						Object entranceMethod = propValue.get("entrance_method");// 入场方式
						if (takePlace!=null && StringUtils.isNotBlank(takePlace.toString())) {
							sb.append("，入场地点："+ takePlace);
						}
						if (entranceMethod!=null && StringUtils.isNotBlank(entranceMethod.toString())) {
							sb.append(YONGLE_SHOW_TICKET_IN);
						}
					}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 填充演出票演出时间-方法
	 * @param produProductMap
	 * @param sb
	 * @param orderItemList订单子项列表
	 * @return String
	 */
	private static  String fillShowticketShowTime(OrdOrderItem item) {
		StringBuffer sb=new StringBuffer();
		String visitTime = DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd");
		if(StringUtils.isBlank(visitTime)){
			visitTime="";
		} else {
			visitTime=visitTime+",";
		}

		if (item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket
				.getCategoryId()) {
			// 演出时间
			String showTicketShowTime = "";
			String startTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventStartTime
							.name());
			String endTime = item
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.showTicketEventEndTime
							.name());

			if (StringUtils.isNotBlank(endTime) && StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime + "至" + endTime;
			} else if (StringUtils.isNotBlank(startTime)) {
				showTicketShowTime = startTime;
			}

			if (StringUtils.isNotBlank(showTicketShowTime)) {
				sb.append("，演出时间："+ visitTime + showTicketShowTime);
			}


		}
		return sb.toString();
	}
	
	
	/**
	 * 填充演出票入场方式，入场地点-方法
	 * @param produProductMap
	 * @param sb
	 * @param orderItemList订单子项列表
	 * @return String
	 */
	private static  String fillShowticketPlace(
			Map<String, ProdProduct> produProductMap, 
			OrdOrderItem item) {
		StringBuffer sb=new StringBuffer();
		if (item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()) {
			if (produProductMap != null) {
				ProdProduct prodProduct = produProductMap.get(item.getOrderItemId().toString());
				if (prodProduct != null) {
					Map<String, Object> propValue = prodProduct.getPropValue();
					if (propValue != null) {
						Object takePlace = propValue.get("take_place");// 入场地点
						Object entranceMethod = propValue.get("entrance_method");// 入场方式
						if (StringUtils.isNotBlank(takePlace.toString())) {
							sb.append("，入场地点："+ takePlace);
						}
						if (StringUtils.isNotBlank(entranceMethod.toString())) {
							sb.append("，入场方式："+ entranceMethod);
						}
					}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 填充订单内容-申码成功-预付-北京春秋
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPrepaidTicketBJ(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map, Map<String,String> goodsCustomSmsMap) {
		Map <String,Object> param = null;
		boolean isOne = (Boolean) map.get("ISONE");
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPrepaidTicketBJ:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		if(Constant.getInstance().getProperty("gz.supplierId") == null){
			return content;
		}

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name())
									,SuppGoods.NOTICETYPE.QRCODE.name())){
								if(order.hasNeedPrepaid()){//预付
									if(Constant.getInstance().getProperty("bj.supplierId").equalsIgnoreCase(String.valueOf(item.getSupplierId()))){
										param = new HashMap<String, Object>();
										//订单编号orderId

										//证件号
										param.put(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField(), order.getContactPerson().getIdNo());

										//商品自定义短信
					    				String customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    					customSms = customSms.substring(0, 199);
					    				}else if(StringUtil.isEmptyString(customSms.trim())){
					    					customSms = "";
					    				}
										param.put(OrdSmsTemplate.FIELD.GOODS_CUSTOM_SMS.getField(), customSms);
										//分销的短信，需要把最后的签名去掉
										if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
											param.put(SMSLVMAMAFLAG, N);
										}
										return fillSms(content, order, param);
									}
								}
							}
						}
					}
				}
			}

		}
		return content;
	}
	/**
	 * 填充订单内容-申码成功-预付-广州银旅
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPrepaidTicketGZ(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
			Map<String,String> enterStyleMap, Map<String,String> goodsCustomSmsMap) {
		Map <String,Object> param = null;
		boolean isOne = (Boolean) map.get("ISONE");
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPrepaidTicketGZ:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		if(Constant.getInstance().getProperty("gz.supplierId") == null){
			return content;
		}

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name())
									,SuppGoods.NOTICETYPE.QRCODE.name())){
								if(order.hasNeedPrepaid()){//预付
									if(Constant.getInstance().getProperty("gz.supplierId").equalsIgnoreCase(String.valueOf(item.getSupplierId()))){
										param = new HashMap<String, Object>();

										// 入园方式
										String enterStyle = "";
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
												param.put(OrdSmsTemplate.FIELD.ENTER_STYLE.getField(), enterStyle);
						    				}else{
						    					enterStyle = "";
						    				}
					    				}

										//订单编号orderId
										//张数
										param.put(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField(), item.getQuantity());

										//二维码qrcode
										param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

										//辅助码auxiliaryCode
										if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
											if(isOne){
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
											}else{
												param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(i));
											}
										}

										//游玩时间
										param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

										//商品自定义短信
					    				String customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    				if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    					customSms = customSms.substring(0, 199);
					    				}else if(StringUtil.isEmptyString(customSms.trim())){
					    					customSms = "";
					    				}
										param.put(OrdSmsTemplate.FIELD.GOODS_CUSTOM_SMS.getField(), customSms);
										//分销那边发短信，需要把最后的签名去掉
										if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
											param.put(SMSLVMAMAFLAG, N);
										}
										return fillSms(content, order, param);
									}
								}
							}
						}
					}
				}
			}

		}
		return content;
	}
	/**
	 * 填充订单内容-申码成功-预付-期票
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPrepaidAperiodicTicket(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
			Map<String,String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<Long,String> specialSmsMap) {
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPrepaidAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));

		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()),SuppGoods.NOTICETYPE.QRCODE.name())){
								if(order.hasNeedPrepaid()){//预付
									if(item.hasTicketAperiodic()){//期票

										if(item.getProductId() != null){
											if(!proSet.contains(item.getProductId())){
												proSet.add(item.getProductId());

												//产品名称
												if(item.getCategoryId()!=13){
												    sb.append(item.getProductName());
												}
											}
										}

										//商品名称
										sb.append(replaceGoodsName(item.getSuppGoodsName()));
										//份数
										sb.append("，份数：" + item.getQuantity());
										//包含人
										String personContent = "";
					    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
					    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
					    				if(adultNum!=0||childNum!=0){
					    					personContent = "，包含人数";
					    					if(adultNum!=0&&childNum!=0){
					    						personContent = personContent + adultNum + "成人," + childNum + "儿童。";
					    					}
					    					if(adultNum!=0&&childNum==0){
					    						personContent = personContent + adultNum + "成人。";
					    					}
					    					if(adultNum==0&&childNum!=0){
					    						personContent = personContent + childNum + "儿童。";
					    					}
					    				}
										//sb.append("，包含人数"+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人,"+ calculateLong(item.getQuantity(), item.getChildQuantity())+ "儿童。" );


										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
						    				sb.append(personContent);
			    						}
										//取票时间
//										String ticketTime = "";
//										if(ticketTime != null){
//											ticketTime = timeMap.get(item.getOrderItemId().toString());
//											if(ticketTime != null && !"".equals(ticketTime.trim())){
//												ticketTime = "取票时间：" + ticketTime+",";
//											}else{
//												ticketTime = "";
//											}
//										}
//										sb.append(ticketTime);
										//有效期
//										sb.append("有效期："+DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd")+"-"+DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd")+",");
										String termOfvalidity = getGoodsExpString(item);
										//有效期
										sb.append(termOfvalidity);
										//不适用日期
                                        if(null != item.getAperiodicUnvalidDesc() && !"".equals(item.getAperiodicUnvalidDesc())){
                                            sb.append("不适用日期："+item.getAperiodicUnvalidDesc());
                                        }else if(null != item.getInvalidDate() && !"".equals(item.getInvalidDate())){
											sb.append("不适用日期："+item.getInvalidDate());
										}

										String address = "";
										String visitAddress = "";
										if(addressMap != null && addressMap.isEmpty()==false){
					    					address = addressMap.get(item.getOrderItemId().toString());
					    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					    					if(address != null && !"".equals(address.trim())){
					    	    				address = "，取票地点：" + address;
					    	    			}else{
					    	    				//address = "";
					    	    				if(StringUtils.isNotBlank(visitAddress)){
					    	    					address = "，入园地点：" + visitAddress;
					    	    				}
					    	    			}
					    				}
										sb.append(address);
										//入园限制时间
										if (timeMap != null) {
                                            String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if(StringUtils.isNotBlank(passLimitTime)){
                                                passLimitTime= ","+passLimitTime;
                                                sb.append(passLimitTime);

                                            }
										}


					    				String enterStyle = ""; // 入园方式
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
						    				}else{
						    					enterStyle = "";
						    				}
					    				}
					    				//入园方式
										sb.append(enterStyle);

										String customSms = "";
										if(goodsCustomSmsMap != null){
					    					customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    	    				customSms = customSms.substring(0, 199);
					    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
					    	    				customSms = "";
					    	    			}
					    				}
										if(StringUtils.isNotEmpty(customSms)){
											//商品自定义短信
											sb.append(COMMA + customSms + " ");
										}else{
											sb.append(" ");
										}
									}
								}
							}
						}
					}
				}
			}

		}
		//二维码qrcode
		param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

		//辅助码auxiliaryCode
		if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
			param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
		}
		//订单编号orderId

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//客服电话customerServicePhone
		//分销那边发短信，需要把最后的签名去掉
		if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
			param.put(SMSLVMAMAFLAG, N);
		}
		
		String isToForeign = isToForeign(order);
		param.put("isToForeign", isToForeign);

		return fillSms(content, order, param);
	}

	/**
	 * 填充订单内容-申码成功-预付-期票(服务商为虚拟凭证码库时)
	 */
	public static String fillSmsForPrepaidAperiodicTicketForVirtualCode(String content, OrdOrder order) {
		Map <String,Object> param = new HashMap<String, Object>();
		param.put("isToForeign", "N");
		return fillSms(content, order, param);
	}

	/**
	 * 填充订单内容-申码成功-预付-非期票
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPrepaidNoAperiodicTicket(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
			Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<String,ProdProduct> produProductMap,Map<Long,String> specialSmsMap,boolean ifYL) {
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		boolean isdisneyShow=isDisneyShowOrder(order);
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();
        boolean hasLvJiProduct = false;

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
                String lvjiSupplierId = Constant.getInstance().getProperty("lvji.supplierId");
                boolean isLvji = (StringUtils.isNotBlank(lvjiSupplierId)  && Long.parseLong(lvjiSupplierId) == item.getSupplierId());
                hasLvJiProduct = isLvji;

                for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13||item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()),SuppGoods.NOTICETYPE.QRCODE.name())){ //一定要改回来
								if(order.hasNeedPrepaid()){//预付
									if(!item.hasTicketAperiodic()){//非期票
										if(item.getProductId() != null){
											if(!proSet.contains(item.getProductId())){
												proSet.add(item.getProductId());
												//产品名称
												if(item.getCategoryId()!=13){
												    sb.append(item.getProductName());
												}
											}
										}
										if(!isdisneyShow){
										//商品名称
										sb.append(replaceGoodsName(item.getSuppGoodsName()));
										//份数
										sb.append("，份数：" + item.getQuantity());

                                        if(isLvji) {
                                            sb.append("，");
                                        }
										//包含人
										String personContent = "";
					    				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
					    				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
					    				if(adultNum!=0||childNum!=0){
					    					personContent = "，包含人数";
					    					if(adultNum!=0&&childNum!=0){
					    						personContent = personContent + adultNum + "成人," + childNum + "儿童。";
					    					}
					    					if(adultNum!=0&&childNum==0){
					    						personContent = personContent + adultNum + "成人。";
					    					}
					    					if(adultNum==0&&childNum!=0){
					    						personContent = personContent + childNum + "儿童。";
					    					}
					    				}
										//sb.append("，包含人数"+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人,"+ calculateLong(item.getQuantity(), item.getChildQuantity())+ "儿童。" );
											if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
                                            sb.append(personContent);
                                        }
										}else{
											sb.append(produtInfoDisneyShow(order));
										}

										//取票时间
										String ticketTime = getTicketTimeStr(timeMap, item);

                                        if(!isLvji) {
                                            sb.append(ticketTime);
                                        }
										//游玩时间
										sb.append("游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

										//增加玩乐演出票的信息  申码成功(支付完成)-支付完成+预付+非期票+二维码 
										if(ifYL){
											LOG.info("======= fillShowticketSbYL:order:" + order.getOrderId());
											sb.append(fillShowticketSbYL(produProductMap, item));
										}else{
											LOG.info("======= fillShowticketSb:order:" + order.getOrderId());
											sb.append(fillShowticketSb(produProductMap, item));
										}

                                        if(isLvji) {
                                            sb.append("。");
                                        }

										String circusActEndtime = (String) item
												.getContentValueByKey("circusActEndtime");
										String circusActStartTime = item
												.getContentStringByKey("circusActStartTime");

										//增加上海迪士尼的演出时间
										String showtime = (String) map
												.get("SHOWTIME");
										String sectionInf = (String) map
												.get("SECTIONINF");
										if ("".equals(showtime) == false
												&& showtime != null) {
											sb.append(COMMA+"演出时间:");
											sb.append(showtime);
										}
										LOG.info("showtiem"+showtime);
										//增加上海迪士尼区域详情
										if ("".equals(sectionInf) == false
												&& sectionInf != null) {
											sb.append("区域详情:");
											sb.append(sectionInf);
											if(StringUtils.isEmpty(circusActEndtime)
													&&StringUtils.isEmpty(circusActStartTime)
													&&CollectionUtils.isEmpty(addressMap)
													&&CollectionUtils.isEmpty(enterStyleMap)
													&&CollectionUtils.isEmpty(goodsCustomSmsMap)){
												sb.append(PERIOD);
											}
										}
										LOG.info("sectionInf"+sectionInf);

										// 添加场次时间
										LOG.info("circusActEndTime --------------"
												+ circusActEndtime
												+ "circusActStartTime-------------:"
												+ circusActStartTime);

										if (circusActEndtime == null
												|| "".equals(circusActEndtime)) {
											if (circusActStartTime != null
													&& "".equals(circusActStartTime) == false) {
												String[] split = circusActStartTime
														.split(" ");
												if (split.length > 0)
													sb.append("，场次：" + split[1]);
											}
										} else {
											if (circusActStartTime != null
													&& "".equals(circusActStartTime) == false) {
												String[] circusActStartTimeArray = circusActStartTime
														.split(" ");
												String[] circusActEndtimeArray = circusActEndtime
														.split(" ");
												if (circusActStartTimeArray.length > 0
														&& circusActEndtimeArray.length > 0) {
													String start = circusActStartTimeArray[1];
													String end = circusActEndtimeArray[1];
													sb.append("，场次：" + start
															+ "-" + end);
												}
											}
										}

										//
										String address = "";
										String visitAddress = "";
										if(addressMap != null && addressMap.isEmpty()==false){
					    					address = addressMap.get(item.getOrderItemId().toString());
					    					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					    					if(address != null && !"".equals(address.trim())){
					    	    				address = "，取票地点：" + address;
					    	    			}else{
					    	    				if(visitAddress!=null && !("").equals(visitAddress.trim()))
					    	    				address = "，入园地点：" + visitAddress;
					    	    			}
					    				}

                                        if(!isLvji) {
                                            sb.append(address);
                                        }

					    				String enterStyle = ""; // 入园方式
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
						    				}else{
						    					enterStyle = "";
						    				}
					    				}
					    				//入园方式

                                        if(!isLvji) {
                                            sb.append(enterStyle);
                                        }

										String customSms = "";
					    				if(goodsCustomSmsMap != null){
					    					customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    	    				customSms = customSms.substring(0, 199);
					    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
					    	    				customSms = "";
					    	    			}
					    				}
										//入园限制时间
                                        if (timeMap != null) {
                                            String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if(StringUtils.isNotBlank(passLimitTime)){
                                                passLimitTime= ","+passLimitTime;
                                                sb.append(passLimitTime);
                                            }
                                        }


                                        if(StringUtils.isNotEmpty(customSms)){
											//商品自定义短信
											sb.append(COMMA + customSms + " ");
										}else{
											sb.append(" ");
										}

                                        if(isLvji) {
                                            LOG.info("----------------#####");  
                                        	//OrdPassCode ordPassCode = supplierOrderHandleService.getOrdPassCodeByOrderItemId(item.getOrderItemId());
                                        	OrdPassCode ordPassCode = (OrdPassCode)map.get(String.valueOf(item.getOrderItemId()));
                                        	if(ordPassCode!=null&&!"".equals(ordPassCode.getContent())){
	                                        	LOG.info("ordPassCode----------------#####"+ordPassCode);  
	                                            JSONObject josnObject = JSONObject.fromObject(ordPassCode.getContent());
	                                            LOG.info("josnObject----------------#####"+josnObject);  
	                                            sb.append("智能导游服务在线使用地址：");
	                                            sb.append(josnObject.getString("url") + " 授权码：");
	                                            sb.append(josnObject.getString("code"));
	                                            sb.append("(使用截止时间：");
	                                            sb.append(josnObject.getString("endDate"));
	                                            sb.append("),");
	                                            sb.append("一个授权码只能使用一次, ");
	                                            sb.append("有效期：");
	                                            sb.append(josnObject.getString("effDays"));
	                                            sb.append("天");
                                        	}
                                        }
										//特殊处理产品438022、438021
										if(order.getOrderPackList() != null && order.getOrderPackList().size() > 0){
											LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicket:orderId=" + order.getOrderId() + "orderPackList.size=" + order.getOrderPackList().size());
											OrdOrderPack ordOrderPack = order.getOrderPackList().get(0);
											if(ordOrderPack != null && ordOrderPack.getProductId() != null){
												LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicket:orderId=" + order.getOrderId() + "productID=" + ordOrderPack.getProductId());
												if((ordOrderPack.getProductId().longValue() == 438022L)){
													sb.append("赠品提示：【一套东方CJ电视产品：1间房+2张成人票】默认赠送1只Hello Kitty公仔，请凭此短信至凯蒂猫家园园内客服中心领取，取消行程或未在游玩当天领取则视为自动放弃赠品。 ");
												}
												if((ordOrderPack.getProductId().longValue() == 438021L)){
													sb.append("赠品提示：【一套东方CJ网站产品：1间房+2张成人票】默认赠送1只Hello Kitty妈咪包，请凭此短信至凯蒂猫家园园内客服中心领取，取消行程或未在游玩当天领取则视为自动放弃赠品。");
												}
											}
										}
									}
								}
							}
						}
					}
				if(isdisneyShow)
					break;
				}
			if(isdisneyShow)
				break;
			}

		}
		//二维码qrcode
		param.put(OrdSmsTemplate.FIELD.QRCODE.getField(), "");

		//辅助码auxiliaryCode
		if(auxiliaryCodeList != null && auxiliaryCodeList.size() > 0){
			param.put(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField(), auxiliaryCodeList.get(0));
		}

		//订单编号orderId

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		//分销那边发短信，需要把最后的签名去掉
		if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
			param.put(SMSLVMAMAFLAG, N);
		}
		
		String isToForeign = isToForeign(order);
		param.put("isToForeign", isToForeign);
		
		//客服电话customerServicePhone
		String fillSms = "";
		if(ifYL){
			fillSms = fillSmsYL(content, order, param);
			LOG.info("======= fillSmsYL:order:" + order.getOrderId() +" fillSms:"+fillSms);
		}else{
			fillSms = fillSms(content, order, param);
			LOG.info("======= fillSms:order:" + order.getOrderId() +" fillSms:"+fillSms);
		}
		if(fillSms.indexOf(COMMA)==0){
			fillSms= removeCharAt(fillSms,0);
		}if(isDisneyShowOrderItemId(order) || hasLvJiProduct){
			fillSms = fillSms.replace(Constant.SMS_REMOVE_CONTENT_BY_DISNEY, "");
		}
		
		return 	fillSms;
	}


	/**
	 * 填充订单内容-申码成功-预付-非期票(服务商发码)
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForPrepaidNoAperiodicTicketProvider(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
			Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<Long,String> specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicketProvider:orderId=" + order.getOrderId());

		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
							if(StringUtils.equals(item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()),SuppGoods.NOTICETYPE.QRCODE.name())){

								if(order.hasNeedPrepaid()){//预付
									if(!item.hasTicketAperiodic()){//非期票
										if(item.getProductId() != null){
											if(!proSet.contains(item.getProductId())){
												proSet.add(item.getProductId());
												//产品名称
												sb.append(item.getProductName());
											}
										}

										//商品名称
										sb.append(replaceGoodsName(item.getSuppGoodsName()));
										//份数
										sb.append("，份数：" + item.getQuantity());
										//包含人
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
											sb.append("，包含人数"+ calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人,"+ calculateLong(item.getQuantity(), item.getChildQuantity())+ "儿童。" );
			    						}else{
											sb.append("，");

			    						}
										//取票时间
										String ticketTime = getTicketTimeStr(timeMap, item);
										sb.append(ticketTime);
										//游玩时间
										sb.append("游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));

										String address = "";
										if(addressMap != null && addressMap.isEmpty()==false){
					    					address = addressMap.get(item.getOrderItemId().toString());
					    					if(address != null && !"".equals(address.trim())){
					    	    				address = "，取票地点：" + address;
					    	    			}else{
					    	    				address = "";
					    	    			}
					    				}
										sb.append(address);

					    				String enterStyle = ""; // 入园方式
					    				if(enterStyleMap != null){
						    				enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
											if(enterStyle != null && !"".equals(enterStyle.trim())){
												enterStyle = "，入园方式：" + enterStyle;
						    				}else{
						    					enterStyle = "";
						    				}
					    				}
					    				//入园方式
										sb.append(enterStyle);

										//入园限制时间
                                        if (timeMap != null) {
                                            String passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if(StringUtils.isNotBlank(passLimitTime)){
                                                passLimitTime= ","+passLimitTime;
                                                sb.append(passLimitTime);

                                            }
                                        }


                                        String customSms = "";
					    				if(goodsCustomSmsMap != null){
					    					customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
					    	    			if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
					    	    				customSms = customSms.substring(0, 199);
					    	    			}else if(StringUtil.isEmptyString(customSms.trim())){
					    	    				customSms = "";
					    	    			}
					    				}


										if(StringUtils.isNotEmpty(customSms)){
											//商品自定义短信
											sb.append(COMMA + customSms + " ");
										}else{
											sb.append(" ");
										}

									}
								}
							}
						}
					}
				}
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		//分销那边发短信，需要把最后的签名去掉
		if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
			param.put(SMSLVMAMAFLAG, N);
		}
		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}

    /**
     * 
     * @param content
     * @param order
     * @param orderItemList
     * @param map
     * @param addressMap
     * @param enterStyleMap
     * @param timeMap
     * @param goodsCustomSmsMap
     * @param specialSmsMap
     * @return
     */
    public static String fillSmsForPrepaidNoAperiodicTicketReschedule(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
                                                                    Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<Long,String> specialSmsMap) {
        LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicketReschedule:orderId=" + order.getOrderId());

        Map <String,Object> param = new HashMap<String, Object>();
        StringBuffer sb = new StringBuffer();
        Set<Long> proSet = new HashSet<Long>();

        List<OrdOrderItem> ordItemList = order.getOrderItemList();
        if(ordItemList != null && ordItemList.size() > 0){
            for(OrdOrderItem item : ordItemList){
                        //1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
                        if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
                                if(order.hasNeedPrepaid()){//预付
                                    if(!item.hasTicketAperiodic()){//非期票
                                        if(item.getProductId() != null){
                                            if(!proSet.contains(item.getProductId())){
                                                proSet.add(item.getProductId());
                                                //产品名称
                                                sb.append(item.getProductName());
                                            }
                                        }

                                        //商品名称
                                        sb.append(replaceGoodsName(item.getSuppGoodsName()));
                                        //份数
                                        sb.append("，份数：" + item.getQuantity());
                                        //包含人
                                        String personContent = "";
                                        long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
                                        long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
                                        if(adultNum!=0||childNum!=0){
                                            personContent = "，包含人数";
                                            if(adultNum!=0&&childNum!=0){
                                                personContent = personContent + adultNum + "成人," + childNum + "儿童。";
                                            }
                                            if(adultNum!=0&&childNum==0){
                                                personContent = personContent + adultNum + "成人。";
                                            }
                                            if(adultNum==0&&childNum!=0){
                                                personContent = personContent + childNum + "儿童。";
                                            }
                                        }
                                        if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
                                            sb.append(personContent);
                                        }
                                        //取票时间
										String ticketTime = getTicketTimeStr(timeMap, item);
                                        sb.append(ticketTime);
                                        //游玩时间
                                        if(null!=item.getVisitTime()){
                                            sb.append("游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
                                        }

                                        String address = "";
                                        String visitAddress = "";
                                        if(addressMap != null && addressMap.isEmpty()==false){
                                            address = addressMap.get(item.getOrderItemId().toString());
                                            visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
                                            if(address != null && !"".equals(address.trim())){
                                                address = "，取票地点：" + address;
                                            }else{
                                                if(StringUtils.isNotBlank(visitAddress)){
                                                    address = "，入园地点：" + visitAddress;
                                                }
                                            }
                                        }
                                        sb.append(address);

                                        String enterStyle = ""; // 入园方式
                                        if(enterStyleMap != null){
                                            enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
                                            if(enterStyle != null && !"".equals(enterStyle.trim())){
                                                enterStyle = "，入园方式：" + enterStyle;
                                            }else{
                                                enterStyle = "";
                                            }
                                        }
                                        //入园方式
                                        sb.append(enterStyle);

                                        //入园限制时间
                                        String passLimitTime = "";
                                        if (timeMap != null) {
                                            passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
                                            if (passLimitTime!=null && !"".equals(passLimitTime.trim())) {
                                                passLimitTime = "，"+passLimitTime;
                                            }else{
                                                passLimitTime = "";
                                            }
                                        }
                                        sb.append(passLimitTime);

                                        String customSms = "";
                                        if(goodsCustomSmsMap != null){
                                            customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
                                            if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
                                                customSms = customSms.substring(0, 199);
                                            }else if(StringUtil.isEmptyString(customSms.trim())){
                                                customSms = "";
                                            }
                                        }


                                        if(StringUtils.isNotEmpty(customSms)){
                                            //商品自定义短信
                                            sb.append(COMMA + customSms + " ");
                                        }else{
                                            sb.append(" ");
                                        }

                                    }
                                }
                        }
            }
        }
        //产品信息productInfo
        param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
        //客服电话customerServicePhone
        return fillSms(content, order, param);
    }
	/**
	 * 数据计算
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static Long calculateLong(Long l1, Long l2){
		if(l1 != null && l2 != null){
			BigDecimal bd1 = new BigDecimal(l1.longValue());
			BigDecimal bd2 = new BigDecimal(l2.longValue());
			return bd1.multiply(bd2).longValue();
		}
		return 0L;
	}

	/**
	 * 门票商品名称只保留主标题，去掉副标题
	 * @param str
	 * @return
	 */
	private static String replaceGoodsName(String goodsName) {
		if(StringUtil.isEmptyString(goodsName)){
			return "";
		}
		int a = goodsName.indexOf("【");
		int b = 0;
		for(int i=0; i < goodsName.length(); i++){
			if(goodsName.charAt(i) == '】'){
				if(i != goodsName.length()-1){
					int temp = goodsName.substring(i + 1).indexOf("】");
					if(temp != -1){
						continue;
					}else{
						b = i;
						break;
					}
				}else{
					b = i;
				}
			}
		}
		if((a != -1) && (a < b)){
			goodsName = goodsName.replace(goodsName.substring(a, b + 1), "");
		}
		return goodsName;
	}

	//预付-提交退款申请
	public static String fillSmsForRefundBack(String content, OrdOrder order, HashMap<String,Object> param) {

		Map<String, Object> dataMap = new HashMap<String, Object>();
		String regex = "\\$\\{(.+?)\\}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String keyField = matcher.group(1);
			//订单编号orderId
			if(OrdSmsTemplate.FIELD.ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField, order.getOrderId());
			}
			// 入园方式
			if(OrdSmsTemplate.FIELD.ENTER_STYLE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.ENTER_STYLE.getField()));
			}
			//客服电话customerServicePhone
			if(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField().equals(keyField)){
				//定制游客服电话
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(order.getCategoryId())){
					dataMap.put(keyField, Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMIZED_CUSTOMER_SERVICE_PHONE);
				}else{
					if(getFormInfo(order, BuyInfoAddition.customerServiceTel) != null){
						dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.customerServiceTel));
						LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "供应商客服电话=" + getFormInfo(order, BuyInfoAddition.customerServiceTel));
					}else{
						dataMap.put(keyField, Constant.CUSTOMER_SERVICE_PHONE);
						LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服电话=" + Constant.CUSTOMER_SERVICE_PHONE);
					}
				}
			}
			//客户端签名clientSign
			if(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField().equals(keyField)){
				if(getFormInfo(order, BuyInfoAddition.distributionSuffix) != null){
					dataMap.put(keyField, getFormInfo(order, BuyInfoAddition.distributionSuffix));
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "供应商客服签名=" + getFormInfo(order, BuyInfoAddition.distributionSuffix));
				}else{
					dataMap.put(keyField, Constant.LVMAMA);
					LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "lvmama客服签名=" + Constant.LVMAMA);
				}
			}
			/* ****************************催支付start**************************** */
			//最晚支付时间latestPaymentTime
			if(OrdSmsTemplate.FIELD.LATEST_PAYMENT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			/* ****************************催支付end**************************** */


			/* ****************************二维码start**************************** */
			//二维码qrcode
			if(OrdSmsTemplate.FIELD.QRCODE.getField().equals(keyField)){
			}

			//辅助码auxiliaryCode
			if(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.AUXILIARY_CODE.getField()));
			}

			//有效期validBeginTime~validEndTime
			if(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_BEGIN_TIME.getField()));
			}
			if(OrdSmsTemplate.FIELD.VALID_END_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VALID_END_TIME.getField()));
			}

			//不使用日期invalidTime
			if(OrdSmsTemplate.FIELD.INVALID_TIME.getField().equals(keyField)){
				if(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()) == null && "".equals(param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()))){
					dataMap.put(keyField, "无");
				}else{
					dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.INVALID_TIME.getField()));
				}
			}
			//游玩时间visitTime
			if(OrdSmsTemplate.FIELD.VISIT_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField()));
			}
			//证件号certificateNo
			if(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CERTIFICATE_NO.getField()));
			}
			//张数ticketSheets
			if(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.TICKET_SHEETS.getField()));
			}
			//产品名称productName
			if(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()));
			}
			//商品名称suppGoodsName
			if(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField()));
			}
			//入园地址gardenAddress
			if(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.GARDEN_ADDRESS.getField()));
			}
			/* ****************************二维码end**************************** */

			/* ****************************订单提交start**************************** */
			//产品信息productInfo
			if(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField()));
			}
			//总价oughtAmount
			if(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField()));
			}
			//数量QUANTITY
			if(OrdSmsTemplate.FIELD.QUANTITY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.QUANTITY.getField()));
			}
			//离店时间departureDate
			if(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField()));
			}
			//担保departureDate
			if(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField()));
			}
			//酒店地址departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField()));
			}
			//酒店电话departureDate
			if(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_TELEPHONE.getField()));
			}
			//最晚保留时间latestUnguarTime
			if(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.LATEST_UNGUAR_TIME.getField()));
			}
			//Added by yangzhenzhong  最晚无损取消时间
			if (OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField().equals(keyField)){
				dataMap.put(keyField,param.get(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField()));
			}
			//end
			/* ****************************订单提交end**************************** */
		}
		if(MapUtils.isNotEmpty(param)){
			dataMap.putAll(param);
		}
		dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), assembleSms(order, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField())));

		return StringUtil.composeMessage(content, dataMap);

	}

	/**
	 * 预付支付前置
	 * @param content
	 * @param order
	 * @param sendNode
	 * @param product
	 * @param customSmsMap 添加自定义短信
	 * @return
	 * @author ltwangwei
	 * @date 2016-5-16 下午2:37:47
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	public static String fillSmsForPayAhead(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode, 
			ProdProduct product, Map<String, String> customSmsMap) {
		LOG.info("SmsUtil.fillSmsForPayAhead:orderId=" + order.getOrderId()+"==fillSmsForPayAhead:visitTime="+order.getVisitTime()+"==fillSmsForPayAhead:endTime="+order.getEndTime());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			int count = 0;
			for(OrdOrderItem item : order.getOrderItemList()){
				if(count > 0){
					sb.append(",");
				}
				sb.append(item.getProductName()).append(",").append(item.getSuppGoodsName()).append(item.getQuantity());
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()){
					sb.append("份");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()){
					sb.append("间");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId() == item.getCategoryId()
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() == item.getCategoryId()
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() == item.getCategoryId()
                        ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId() == item.getCategoryId()){
					sb.append("张").append(getCustomSms(customSmsMap, item.getOrderItemId().toString()));
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId() == item.getCategoryId()){
					sb.append("份").append(getCustomSms(customSmsMap, item.getOrderItemId().toString()));
				}
				else{
					sb.append("份");
				}
				count++;
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//出游时间
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
	    
	    //离店时间
	    param.put(OrdSmsTemplate.FIELD.DEPARTURE_DATE.getField(), DateUtil.formatDate(order.getEndTime(), "yyyy-MM-dd"));

	    //酒店地址productAddress
	    if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == order.getCategoryId()){
	    	param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), "，酒店地址："+((product.getPropValue().get("address") != null) ? product.getPropValue().get("address") : "暂未提供"));
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()){
			param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), "，酒店地址："+((product.getPropValue().get("address") != null) ? product.getPropValue().get("address") : "暂未提供"));
			content = content.replace("出游日期", "入住日期");
		}

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

		//担保方式cancelStrategy
		if (!isCancelStrategy(order)) {
			String cancelStrategy = order.getRealCancelStrategy();
			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				cancelStrategy = order.getCancelStrategy();
			}
			
			if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			//当地玩乐 美食 娱乐 购物 退改策略
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(order.getCategoryId())){
				param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), "");
			} else {
				param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
			}
		}

		return fillSms(content, order, param);
	}
	
	public static String fillDisineySmsForPayAhead(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode, 
			ProdProduct product, Map<String, String> customSmsMap,String conTactName,String mobile) {
		LOG.info("SmsUtil.fillSmsForPayAhead:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			int count = 0;
			for(OrdOrderItem item : order.getOrderItemList()){
				if(count > 0){
					sb.append(",");
				}
				sb.append(item.getProductName()).append(",").append(item.getSuppGoodsName()).append(item.getQuantity());
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()){
					sb.append("份");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()){
					sb.append("间");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId() == item.getCategoryId()
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() == item.getCategoryId()
	    				||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() == item.getCategoryId()
                        ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId() == item.getCategoryId()){
					sb.append("张").append(getCustomSms(customSmsMap, item.getOrderItemId().toString()));
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId() == item.getCategoryId()){
					sb.append("份").append(getCustomSms(customSmsMap, item.getOrderItemId().toString()));
				}
				else{
					sb.append("份");
				}
				count++;
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());

		//出游时间
	    param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));

	    //酒店地址productAddress
	    if(product != null && product.getPropValue() != null){
	    	param.put(OrdSmsTemplate.FIELD.PRODUCT_ADDRESS.getField(), (product.getPropValue().get("address") != null) ? product.getPropValue().get("address") : "暂未提供");
	    }

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());
		//预定人姓名
		if(StringUtil.isNotEmptyString(conTactName)){
			param.put(OrdSmsTemplate.FIELD.CONTACT_NAME.getField(), conTactName);
		}
		
		/*//供应商订单号
		if(StringUtil.isNotEmptyString(suppOrderId)){
			param.put(OrdSmsTemplate.FIELD.SUPP_ORDER_ID.getField(), suppOrderId);
		}*/
		
		//更改需求，去掉供应商订单号，改为联系人和手机号
        if(StringUtil.isNotEmptyString(mobile)){
            param.put(OrdSmsTemplate.FIELD.MOBILE.getField(), mobile);
        }
		
		//担保方式cancelStrategy
		if (!isCancelStrategy(order)) {
		   String cancelStrategy = order.getRealCancelStrategy();
		   if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
		   if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				cancelStrategy = order.getCancelStrategy();
			}
					
			if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			//当地玩乐 美食 娱乐 购物 退改策略
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())
			 ||BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())){
				cancelStrategy = order.getMainOrderItem().getCancelStrategy();
			}
			param.put(OrdSmsTemplate.FIELD.CANCEL_STRATEGY.getField(), SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategy)+"。");
			}
		return fillSms(content, order, param);
	}

	/**
	 * 是否可退改
	 * 条件：设置退改规则是可退改（含可退改、阶梯退改、同步商品退改），并且可退改一定设置最晚无损取消时间。
	 * @param order
	 * @return
	 */
	public static boolean isCancelStrategy(OrdOrder order) {
		if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				return true;
			} else if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy()) 
					&& ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getHotelCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 预付支付前置-退改规则 
	 * @param content
	 * @param order
	 * @param sendNode
	 * @param product
	 * @return
	 * @author dongningbo
	 * @date 2016年5月26日 17:16:33
	 */
	public static String fillSmsForCancelStrategy(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode, 
			ProdProduct product, Map<String, String> customSmsMap) {
		LOG.info("SmsUtil.fillSmsForCancelStrategy:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			int count = 0;
			for(OrdOrderItem item : order.getOrderItemList()){
				if (item.getCategoryId() != BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId()
						&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() != item.getCategoryId()) {
					continue;
				}
				if(count > 0){
					sb.append(",");
				}
				sb.append(item.getProductName()).append(" ").append(item.getSuppGoodsName()).append(" ");
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()){
					sb.append(item.getContentStringByKey("branchName")).append(" ").append(item.getQuantity()).append("份");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()){
					sb.append(item.getContentStringByKey("branchName")).append(" ").append(item.getQuantity()).append("间");
				}
				else{
					sb.append("份");
				}
				count++;
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		
		//出游时间
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
		
		//最晚无损取消时间lastCancelTime
		param.put(OrdSmsTemplate.FIELD.LAST_CANCEL_TIME.getField(), DateUtil.formatDate(order.getLastCancelTime(), DateUtil.HHMMSS_DATE_FORMAT));
		//最晚无损取消时间不为空且早于发送短信时间,替换短信模版文案内容
		if(null!=order.getLastCancelTime()&&DateUtil.inAdvance(order.getLastCancelTime(),new Date())){
			content = content.replace("订单的最晚取消修改时间是${lastCancelTime}，过此时间取消或未入住将扣除${oughtAmount}，如变更或取消，", "订单不可取消修改，");
		}
		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());
		
		//客服电话
		param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		
		return fillSms(content, order, param);
	}
	
	/**
	 * 预付支付前置-退改规则 
	 * @param content
	 * @param order
	 * @param sendNode
	 * @param product
	 * @return
	 * @author dongningbo
	 * @date 2016年5月26日 17:16:33
	 */
	public static String fillSmsForHotelCombCancelStrategy(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode, 
			ProdProduct product, Map<String, String> customSmsMap) {
		LOG.info("SmsUtil.fillSmsForHotelCombCancelStrategy:orderId=" + order.getOrderId());
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			int count = 0;
			for(OrdOrderItem item : order.getOrderItemList()){
				if (item.getCategoryId() != BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId()
						&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() != item.getCategoryId()) {
					continue;
				}
				if(count > 0){
					sb.append(",");
				}
				sb.append(item.getProductName()).append(" ").append(item.getSuppGoodsName()).append(" ");
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()){
					sb.append(item.getContentStringByKey("branchName")).append(" ").append(item.getQuantity()).append("份");
				}
				else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()){
					sb.append(item.getContentStringByKey("branchName")).append(" ").append(item.getQuantity()).append("间");
				}
				else{
					sb.append(item.getQuantity()).append("份");
				}
				count++;
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		
		//出游时间
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
		
		//退改文案第一句话
		String firstCancelStrategy = getFirstCancelStrategy(order); 
		param.put(OrdSmsTemplate.FIELD.FIRST_CANCEL_STRATEGY.getField(), firstCancelStrategy);
		Date lastCancelTime = getLastCancelTime(order);
		//最晚无损取消时间不为空且早于发送短信时间,替换短信模版文案内容
		if(null!=lastCancelTime&&DateUtil.inAdvance(lastCancelTime,new Date())){
			content = content.replace("${firstCancelStrategy}具体退改费用请在订单详情中查看。", "订单退改查询，请登录驴妈妈账户或致电客服，");
		}
		//客服电话
		param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		
		return fillSms(content, order, param);
	}
	/**
	 * 获得退改文案第一句话
	 * @param order
	 * @return
	 */
	private static String getFirstCancelStrategy(OrdOrder order) {
		String content = null;
		if (order == null) {
			return content;
		}
		try {
			String jsonStr = order.getMainOrderItem().getRefundRules();
			
			//List<SuppGoodsRefundVO> refundRules = JSONArray.parseArray(jsonStr, SuppGoodsRefundVO.class);
			List<SuppGoodsRefundVO> refundRules =  new ArrayList<SuppGoodsRefundVO>();
			if(StringUtil.isNotEmptyString(jsonStr)){
				List<SuppGoodsRefundVO> refundList_ = JSONArray.parseArray(jsonStr, SuppGoodsRefundVO.class);
				if(refundList_ != null){
					refundRules = refundList_;
				}
			}
			if(refundRules.size() == 1){//退改规则只有一条
				SuppGoodsRefundVO refundRule = refundRules.get(0);
				if(refundRule.getDeductValue() == 0){//扣款为0
					content = "如需取消、修改订单，请在"+ DateUtil.getFormatDate(order.getVisitTime(), DateUtil.HHMM_DATE_FORMAT)+"（出游日）前申请退款，可免费取消。";
				}else if(ProdRefundRule.DEDUCTTYPE.PERCENT.getCode().equals(refundRule.getDeductType()) && refundRule.getDeductValue() == 10000){//扣款100%
					content = "该产品一经预订，不支持退改。";
				}else {
					content = refundRule.getRuleDesc(order.getVisitTime())+"。";
				}
			}else{
				StringBuffer temp = new StringBuffer("如需取消、修改订单，请在");
				for(int i = 0; i < 1; i++){
					temp.append(refundRules.get(i).getRuleDesc(order.getVisitTime()));

					temp.append("。");
				}
				content = temp.toString();
			}
			
		} catch (Exception e) {
			LOG.error("SmsUtil getFirstCancelStrategy error.");
		}
		
		return content;
	}

	/**
	 * 获取最晚无损取消时间
	 * @param order
	 * @return
	 */
	private static Date getLastCancelTime(OrdOrder order) {
		Date date = null;
		try {
			String jsonStr = order.getMainOrderItem().getRefundRules();
			List<SuppGoodsRefundVO> refundRules =  new ArrayList<SuppGoodsRefundVO>();
			if(StringUtil.isNotEmptyString(jsonStr)){
				List<SuppGoodsRefundVO> refundList_ = JSONArray.parseArray(jsonStr, SuppGoodsRefundVO.class);
				if(refundList_ != null){
					refundRules = refundList_;
				}
			}
			if(refundRules.size() == 1){//退改规则只有一条,获取最晚无损取消时间
				SuppGoodsRefundVO refundRule = refundRules.get(0);
				if(refundRule.getDeductValue() == 0){//扣款为0
					date = order.getVisitTime();
				}else if(ProdRefundRule.DEDUCTTYPE.PERCENT.getCode().equals(refundRule.getDeductType()) && refundRule.getDeductValue() == 10000){//扣款100%
					date = null;
				}else {
					if(!SuppGoodsRefundVo.CANCEL_TIME_TYPE.OTHER.getCode().equals(refundRule.getCancelTimeType())){
						 date = org.apache.commons.lang3.time.DateUtils.addMinutes(order.getVisitTime(), -refundRule.getLatestCancelTime().intValue());
					}
				}
			}else{
				//退改规则多条,默认取第一条,获取最晚无损取消时间
				if(!SuppGoodsRefundVo.CANCEL_TIME_TYPE.OTHER.getCode().equals(refundRules.get(0).getCancelTimeType())){
					date = org.apache.commons.lang3.time.DateUtils.addMinutes(order.getVisitTime(), -refundRules.get(0).getLatestCancelTime().intValue());
				}
			}
		} catch (Exception e) {
			LOG.error("SmsUtil getLastCancelTime error.");
		}
		return date;
	}
	/**
	 * 根据子订单ID组装自定义短信
	 * @param customSmsMap
	 * @param orderItemId
	 * @return
	 * @author ltwangwei
	 * @date 2016-5-16 下午2:44:11
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	private static String getCustomSms(Map<String, String> customSmsMap, String orderItemId){
		String customSms = "";
		if(customSmsMap != null){
			customSms = customSmsMap.get(orderItemId);
			if(StringUtils.isNotBlank(customSms)){
				customSms = customSms.trim();
				if(customSms.length() > 200){
					customSms = COMMA + customSms.substring(0, 199);
				}else{
					customSms = COMMA + customSms;
				}
			}else{
				customSms = "";
			}
		}
		return customSms;
	}

	/**
	 * 填充订单内容-交通+X品类专用.
	 * @param content
	 * @param order
	 * @param sendNode
	 * @param itemProdProducts
	 * @return
	 */
	public static String fillSmsForRouteAeroHotel(String content, OrdOrder order, List<OrdOrderItem> orderItems,OrdSmsTemplate.SEND_NODE sendNode,Map<String, String> addressMap,
			Map<String, String> enterStyleMap,Map<String, String> customSmsMap,Map<String, String> timeMap,Map<String,ProdProduct> itemProdProducts) {
		LOG.info("SmsUtil.fillSmsForRouteAeroHotel:orderId=" + order.getOrderId() + " sendNode=" + sendNode);
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuilder productInfo = new StringBuilder();
		if (StringUtil.isNotEmptyString(order.getProductName())) {
			productInfo.append(order.getProductName());
		}
		//成人数儿童数
		StringBuilder travellerInfo = new StringBuilder();
		if (!CollectionUtils.isEmpty(orderItems)) {
			for (OrdOrderItem orderItem : orderItems) {
				if (BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(orderItem.getCategoryId())) {
					if (StringUtils.isEmpty(travellerInfo.toString())) {
						if (orderItem.getAdultQuantity() > 0) {
							travellerInfo.append(orderItem.getAdultQuantity()).append("成人");
						}
						if (orderItem.getChildQuantity() > 0) {
							travellerInfo.append(orderItem.getChildQuantity()).append("儿童");
						}
					} else {
						break;
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(travellerInfo.toString())) {
			productInfo.append(COMMA);
			productInfo.append(travellerInfo);
		}
		boolean isPayed=false;
		if (OrdSmsTemplate.SEND_NODE.PAY_ROUTE_AERO_HOTEL.equals(sendNode)) {
			isPayed=true;
			//update-renjiangyi-支付后短信模板调整
			getProductInfoForPayRouteAeroHotel(productInfo, orderItems,itemProdProducts);
			//获取门票信息
			getTicketInfo(productInfo,sendNode,orderItems,timeMap,enterStyleMap,customSmsMap,addressMap,isPayed);
			if(!hasHotel) content=noHotel+content;

		}
	    //订单编号orderId
	    param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(), order.getOrderId());

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), productInfo.toString());

		//总价oughtAmount
		param.put(OrdSmsTemplate.FIELD.OUGHT_AMOUNT.getField(), order.getOughtAmountYuan());

		//出游日期
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateFormatUtils.format(order.getVisitTime(), "yyyy-MM-dd"));
		LOG.info("SmsUtil.fillSmsForRouteAeroHotel:orderId=" + order.getOrderId() +
				" content=" + content + " param=" + param.toString());
		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}

	//判断是否有酒店预订
	private static boolean hasHotel=false;
	private static final String noHotel="【无酒店】";
	/**
	 *  * 交通+X，支付完成模板短信信息构建
	 * @param orderItems
	 * @param productInfo
	 */
	private static void getProductInfoForPayRouteAeroHotel(StringBuilder productInfo, List<OrdOrderItem> orderItems,Map<String,ProdProduct> itemProdProducts) {
		LOG.info("SmsUtil.getProductInfoForPayRouteAeroHotel::productInfo=" + productInfo + " orderItems.size()=" + orderItems.size());
		//保存机票信息
		List<Map<String, String>> aeroInfoList = new ArrayList<Map<String, String>>();
		//保存酒店信息
		List<Map<String, String>> hotelInfoList = new ArrayList<Map<String, String>>();
		if (!CollectionUtils.isEmpty(orderItems)) {
			for (OrdOrderItem orderItem : orderItems) {
				if (BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(orderItem.getCategoryId())) {
					Map<String, String> aeroMap = new HashMap<String, String>();
					//获取起飞机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("fromAirport"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("fromAirport"));
						 aeroMap.put("fromAirport", orderItem.getContentStringByKey("fromAirport"));
					 }
					//获取起飞航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("startTerminal"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("startTerminal"));
						 aeroMap.put("startTerminal", orderItem.getContentStringByKey("startTerminal"));
					 }
					//获取到达机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("toAirport"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("toAirport"));
						 aeroMap.put("toAirport", orderItem.getContentStringByKey("toAirport"));
					 }
					//获取到达航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTerminal"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("arriveTerminal", orderItem.getContentStringByKey("arriveTerminal"));
					 }
					//获取起飞时间
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureTime"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("departureTime", orderItem.getContentStringByKey("departureTime"));
					 }
					//获取航班号
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("flightNo", orderItem.getContentStringByKey("flightNo"));
					 }
					 if(aeroMap.size()>0) aeroInfoList.add(aeroMap);
				} else if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
					hasHotel=true;
					Map<String, String> hotelMap = new HashMap<String, String>();
					//酒店名称
					hotelMap.put("hotelName", orderItem.getProductName());
					//房型
					if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("branchName"))) {
						hotelMap.put("branchName", orderItem.getContentStringByKey("branchName"));
					}
					//数量
					if (orderItem.getQuantity() != null) {
						hotelMap.put("quantity", orderItem.getQuantity().toString());
					}
					//入住日期
					if (orderItem.getVisitTime() != null) {
						hotelMap.put("visitDate", DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd"));
					}
					String leaveDate = getLeaveDateOfHotel(orderItem);
					if (leaveDate != null) {
						hotelMap.put("leaveDate", leaveDate);
					}
					ProdProduct product = itemProdProducts.get(orderItem.getOrderItemId().toString());
					if(product != null){
						hotelMap.put("address", product.getPropValue() == null ? null : product.getPropValue().get("address").toString());
						hotelMap.put("telephone", product.getPropValue() == null ? null : product.getPropValue().get("telephone")==null?null:product.getPropValue().get("telephone").toString());
					}
					hotelInfoList.add(hotelMap);
				}

			}
		}
		//buildProductInfo(productInfo, aeroInfoList, hotelInfoList);
		buildProductInfoUpdate(productInfo, aeroInfoList, hotelInfoList);
		//获取门票信息
	}

	/**
	 * 交通+X，构建productInfo by need change by renjiangyi 16.3.28
	 * @param productInfo
	 * @param aeroInfoList
	 * @param hotelInfoList
	 */
	private static void buildProductInfoUpdate(StringBuilder productInfo,
			List<Map<String, String>> aeroInfoList,
			List<Map<String, String>> hotelInfoList) {
		LOG.info("SmsUtil.buildProductInfo::productInfo=" + productInfo + " aeroInfoList=" + aeroInfoList.toString()
				+ " hotelInfoList=" + hotelInfoList.toString());
		if (!CollectionUtils.isEmpty(aeroInfoList)) {
			productInfo.append(COMMA).append("机票：");
			int i=1;
			for (Map<String, String> itemMap : aeroInfoList) {
				String time="";
				//起飞时间年月日
				if (StringUtil.isNotEmptyString(itemMap.get("departureTime"))) {
					String year=itemMap.get("departureTime").substring(0, 10);
					time=itemMap.get("departureTime").substring(11);
					productInfo.append(year);
				}
				//起飞机场
				String fromAirport = itemMap.get("fromAirport");
				if (StringUtil.isNotEmptyString(fromAirport)) {
					productInfo.append(fromAirport);
					//起飞航站楼
					if (StringUtil.isNotEmptyString(itemMap.get("startTerminal"))) {
						productInfo.append(itemMap.get("startTerminal"));
					}
				}

				//到达机场
				String toAirport = itemMap.get("toAirport");
				if (StringUtil.isNotEmptyString(toAirport)) {
					productInfo.append(UNTIL);
					productInfo.append(toAirport);
					if (StringUtil.isNotEmptyString(itemMap.get("arriveTerminal"))) {
						productInfo.append(itemMap.get("arriveTerminal"));
					}
				}

				//航班号
				if (StringUtil.isNotEmptyString(itemMap.get("flightNo"))) {
					productInfo.append(itemMap.get("flightNo"));
				}
				if(!time.isEmpty()) productInfo.append(COMMA).append(time).append(FLY_UNTIL);
				if(aeroInfoList.size()>1&&aeroInfoList.size()!=i) productInfo.append(COMMAND);
				i++;
			}
		}
		LOG.info("SmsUtil.buildProductInfo_aero::productInfo=" + productInfo);
		if (!CollectionUtils.isEmpty(hotelInfoList)) {
			//酒店：海口美兰宾馆，高级大床房，1间，2016-02-02至2016-02-04），
			productInfo.append(SEMI).append("酒店：");
			for (Map<String, String> itemMap : hotelInfoList) {
				//酒店名称
				String hotelName = itemMap.get("hotelName");
				if (StringUtil.isNotEmptyString(hotelName)) {
					productInfo.append(hotelName);
					//房型
					if (StringUtil.isNotEmptyString(itemMap.get("branchName"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("branchName"));
					}
					//数量
					if (StringUtil.isNotEmptyString(itemMap.get("quantity"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("quantity"));
						productInfo.append("间");
					}
					//入住日期离店日期
					if (StringUtil.isNotEmptyString(itemMap.get("visitDate"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("visitDate"));
						if (StringUtil.isNotEmptyString(itemMap.get("leaveDate"))) {
							productInfo.append(UNTIL);
							productInfo.append(itemMap.get("leaveDate"));
						}
					}
					//酒店地址
					if(StringUtil.isNotEmptyString(itemMap.get("address"))){
						productInfo.append(COMMA).append("地址：").append(itemMap.get("address"));
					}
					//酒店电话
					if(StringUtil.isNotEmptyString(itemMap.get("telephone"))){
						productInfo.append(COMMA).append("电话：").append(itemMap.get("telephone"));
					}
				}
			}
			LOG.info("SmsUtil.buildProductInfo_hotel::productInfo=" + productInfo);
		}
		//if (productInfo.indexOf(LEFT_BRACKET) >= 0) {
			//productInfo.append(RIGHT_BRACKET);
		//}
	}
	/**
	 * 交通+X，构建productInfo
	 * @param productInfo
	 * @param aeroInfoList
	 * @param hotelInfoList
	 */
	@Deprecated
	private static void buildProductInfo(StringBuilder productInfo,
			List<Map<String, String>> aeroInfoList,
			List<Map<String, String>> hotelInfoList) {
		LOG.info("SmsUtil.buildProductInfo::productInfo=" + productInfo + " aeroInfoList=" + aeroInfoList.toString()
				+ " hotelInfoList=" + hotelInfoList.toString());
		if (!CollectionUtils.isEmpty(aeroInfoList)) {
			productInfo.append(LEFT_BRACKET).append("机票：");
			for (Map<String, String> itemMap : aeroInfoList) {
				//起飞机场
				String fromAirport = itemMap.get("fromAirport");
				if (StringUtil.isNotEmptyString(fromAirport)) {
					productInfo.append(fromAirport);
					//起飞航站楼
					if (StringUtil.isNotEmptyString(itemMap.get("startTerminal"))) {
						productInfo.append(itemMap.get("startTerminal"));
					}
				}
				//到达机场
				String toAirport = itemMap.get("toAirport");
				if (StringUtil.isNotEmptyString(toAirport)) {
					productInfo.append(UNTIL);
					productInfo.append(toAirport);
					if (StringUtil.isNotEmptyString(itemMap.get("arriveTerminal"))) {
						productInfo.append(itemMap.get("arriveTerminal"));
					}
				}
				//起飞时间
				if (StringUtil.isNotEmptyString(itemMap.get("departureTime"))) {
					productInfo.append(COMMA).append(itemMap.get("departureTime"));
				}
				//航班号
				if (StringUtil.isNotEmptyString(itemMap.get("flightNo"))) {
					productInfo.append(COMMA).append(itemMap.get("flightNo"));
				}
				productInfo.append(SEMI);
			}
		}
		LOG.info("SmsUtil.buildProductInfo_aero::productInfo=" + productInfo);
		if (!CollectionUtils.isEmpty(hotelInfoList)) {
			//酒店：海口美兰宾馆，高级大床房，1间，2016-02-02至2016-02-04），
			productInfo.append("酒店：");
			for (Map<String, String> itemMap : hotelInfoList) {
				//酒店名称
				String hotelName = itemMap.get("hotelName");
				if (StringUtil.isNotEmptyString(hotelName)) {
					productInfo.append(hotelName);
					//房型
					if (StringUtil.isNotEmptyString(itemMap.get("branchName"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("branchName"));
					}
					//数量
					if (StringUtil.isNotEmptyString(itemMap.get("quantity"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("quantity"));
						productInfo.append("间");
					}
					//入住日期离店日期
					if (StringUtil.isNotEmptyString(itemMap.get("visitDate"))) {
						productInfo.append(COMMA);
						productInfo.append(itemMap.get("visitDate"));
						if (StringUtil.isNotEmptyString(itemMap.get("leaveDate"))) {
							productInfo.append(UNTIL);
							productInfo.append(itemMap.get("leaveDate"));
						}
					}
					productInfo.append(SEMI);
				}
			}
			LOG.info("SmsUtil.buildProductInfo_hotel::productInfo=" + productInfo);
		}
		if (productInfo.indexOf(LEFT_BRACKET) >= 0) {
			productInfo.append(RIGHT_BRACKET);
		}
	}

	/**
	 * 获取门票信息
	 * @param sb
	 * @param sendNode
	 * @param orderItems
	 * @param itemProdProducts
	 * @param timeMap
	 * @param enterStyleMap
	 * @param customSmsMap
	 * @param addressMap
	 * @param isPayed
	 * @return
	 */
	private static void getTicketInfo(StringBuilder sb,OrdSmsTemplate.SEND_NODE sendNode, List<OrdOrderItem> orderItems,Map<String,String> timeMap,Map<String,String> enterStyleMap, Map<String,String> customSmsMap,Map<String, String> addressMap,boolean isPayed){
		int i=0;
		boolean tempFlag=true;
		for (OrdOrderItem item : orderItems) {
			if(item.getCategoryId() ==11L || item.getCategoryId() == 12L ||item.getCategoryId() == 13L){//门票
				if(tempFlag)sb.append(SEMI).append("门票：");
				tempFlag=false;
                String invalidDate = "";
                if(item.getAperiodicUnvalidDesc()!=null &&!"".equals(item.getAperiodicUnvalidDesc())){
                    invalidDate = "，不适用日期 ：" + item.getAperiodicUnvalidDesc();
                }else if(item.getInvalidDate() != null && !"".equals(item.getInvalidDate().trim())){
                     invalidDate = "，不适用日期 ：" + item.getInvalidDate();
                }
				String ticketTime = getTicketTimeStr(timeMap, item);
				String enterStyle = ""; // 入园方式
				if(enterStyleMap != null){
					enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
					if(enterStyle != null && !"".equals(enterStyle.trim())){
						enterStyle = "，入园方式：" + enterStyle;
					}else{
						enterStyle = "";
					}
				}
				String address = "";
				String visitAddress = "";
				String customSms = "";
				if(addressMap != null && addressMap.isEmpty()==false){
					address = addressMap.get(item.getOrderItemId().toString());
					visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
					if(address != null && !"".equals(address.trim())){
						address = "，取票地点：" + address;
					}else{
						//address = "";
						if(StringUtil.isEmptyString(visitAddress))address = "";
						else address = "，入园地点：" + visitAddress;
					}
				}
				if(customSmsMap != null){
					customSms = customSmsMap.get(item.getOrderItemId().toString());
					if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
						customSms = COMMA+customSms.substring(0, 199);
					}else if(StringUtil.isEmptyString(customSms.trim())){
						customSms = "";
					}
				}

				String personContent = "";
				long adultNum = calculateLong(item.getQuantity(), item.getAdultQuantity());
				long childNum = calculateLong(item.getQuantity(), item.getChildQuantity());
				if(adultNum!=0||childNum!=0){
					personContent = "，包含人数";
					if(adultNum!=0&&childNum!=0){
						personContent = personContent + adultNum + "成人、" + childNum + "儿童";
					}
					if(adultNum!=0&&childNum==0){
						personContent = personContent + adultNum + "成人";
					}
					if(adultNum==0&&childNum!=0){
						personContent = personContent + childNum + "儿童";
					}
				}

				if (sendNode.equals(OrdSmsTemplate.SEND_NODE.PAY_ROUTE_AERO_HOTEL)) {
					if(item.hasTicketAperiodic()){
						if(item.getCategoryId()==13L){
							if(isPayed){
								sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
								//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle  + customSms + " ");
							}else{
								sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" +  "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
								//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" +  "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
							}
						}else{
							if(isPayed){
								sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + ticketTime + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate + address + enterStyle + customSms + " ");
							}else{
								sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"  + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。"  + "有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate +  customSms + " ");
							}
						}
					}else{
						if(item.getCategoryId()==13L){
							if(isPayed){
								sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
								//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
							}else{
								sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。"+ "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  customSms + " ");
								//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") +  customSms + " ");
							}
						}else{
							if(isPayed){
								sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" +ticketTime+ "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + ticketTime + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + address + enterStyle + customSms + " ");
							}else{
								sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + customSms + " ");
								//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "。" + "游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd") + customSms + " ");
							}
						}
					}
				}else{
					if(item.hasTicketAperiodic()){
						if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。取票时间：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
						}else{
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。取票时间：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "，有效期：" + DateUtil.formatDate(item.getValidBeginTime(), "yyyy-MM-dd") + "~" + DateUtil.formatDate(item.getValidEndTime(), "yyyy-MM-dd") + invalidDate);
						}
					}else{
						if(item.getCategoryId() == 13L){//组合套餐票供应商打包，不需要产品名称
							sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
							//sb.append(replaceGoodsName(item.getSuppGoodsName()) + "，份数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
						}else{
							sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + "，包含人数" + calculateLong(item.getQuantity(), item.getAdultQuantity()) + "成人、" + calculateLong(item.getQuantity(), item.getChildQuantity()) + "儿童。游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
							//sb.append(item.getProductName() + replaceGoodsName(item.getSuppGoodsName()) + "，张数：" + item.getQuantity() + personContent + "，游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
						}
					}
				}
				if(sendNode != null && addressMap != null && addressMap.isEmpty()==false && enterStyleMap != null && enterStyleMap.isEmpty()==false && customSmsMap != null && customSmsMap.isEmpty()==false){
					if(sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_PREPAY)
						|| sendNode.equals(OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID_PREAUTH_FAILED)){
						address = addressMap.get(item.getOrderItemId().toString());
						visitAddress = addressMap.get(item.getOrderItemId().toString()+"visitAddress");
						if(address != null && !"".equals(address.trim())){
							address = "，取票地址：" + address;
						}else{
							//address = "";
							if(StringUtils.isNotBlank(visitAddress))
							address = "，入园地址：" + visitAddress;

						}

						enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
						if(enterStyle != null && !"".equals(enterStyle.trim())){
							enterStyle = "，入园方式：" + enterStyle;
						}else{
							enterStyle = "";
						}
						sb.append(address + enterStyle);

						customSms = customSmsMap.get(item.getOrderItemId().toString());
						if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
							customSms = customSms.substring(0, 199);
						}else if(StringUtil.isEmptyString(customSms.trim())){
							customSms = "";
						}
						if(StringUtils.isNotEmpty(customSms)){
							sb.append(COMMA + customSms + " ");
						}else{
							sb.append(" ");
						}
					}
				}else{
	//				sb.append("。");
				}
			}
			i++;
		}
	}
	/**
	 * 计算酒店子订单的离店日期.
	 * @param orderItem
	 * @return
	 */
	private static String getLeaveDateOfHotel(OrdOrderItem orderItem) {
		List<OrdOrderHotelTimeRate> ordOrderHotelTimeRates = orderItem.getOrderHotelTimeRateList();
		if(!CollectionUtils.isEmpty(ordOrderHotelTimeRates)){
			Collections.sort(ordOrderHotelTimeRates, new Comparator<OrdOrderHotelTimeRate>() {
				@Override
				public int compare(OrdOrderHotelTimeRate o1, OrdOrderHotelTimeRate o2) {
					return o2.getVisitTime().compareTo(o1.getVisitTime());
				}
			});

			return DateFormatUtils.format(DateUtils.addDays(ordOrderHotelTimeRates.get(0).getVisitTime(), 1), "yyyy-MM-dd");
		}
		return null;
	}


	/**
	 * 含机票的订单短信预订成功后短信模板
	 * @param content
	 * @param order
	 * @param sendNode
	 * @return
	 */
	public static String fillSmsForOrderPayedIncludeFlight(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap, Map<String,String> enterStyleMap,Map<String,String> timeMap,Map<String,String> customSmsMap,
			Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<String, SuppSupplier> itemSuppsupplier,Integer buildBuyCount ,Map<Long,String> specialSmsMap) {
		return fillSmsForPrepaid(content, order, sendNode, addressMap, enterStyleMap, timeMap, customSmsMap, itemProdProducts, frontBusStop,itemSuppsupplier,buildBuyCount,true,specialSmsMap,false);
	}

	/**
	 * 填充"工作时间下单 资源紧张短信"的数据
	 * @param content
	 * @param order
	 * @param param
	 * @return
	 */
	public static String fillSmsForOrderBusyIncludeFlight(String content, OrdOrder order, Map<String,Object> param){
		return SmsUtil.fillSms(content, order,new HashMap<String, Object>());
	}

	/**
	 * 预订成功后短信模板
	 * @param content
	 * @param order
	 * @param sendNode
	 * @param addressMap
	 * @param enterStyleMap
	 * @param timeMap
	 * @param customSmsMap
	 * @param itemProdProducts
	 * @param frontBusStop
	 * @return
	 */
	public static String fillSmsForOrderCreatedIncludeFlight(String content, OrdOrder order, OrdSmsTemplate.SEND_NODE sendNode,
			Map<String, String> addressMap,Map<String,String> enterStyleMap,Map<String,String> timeMap, Map<String,String> customSmsMap
			, Map<String,ProdProduct> itemProdProducts, String frontBusStop,Map<Long,String> specialSmsMap){
		return fillSmsForPrepaid(content, order, sendNode, addressMap, enterStyleMap, timeMap, customSmsMap, itemProdProducts, frontBusStop,null,null,false,specialSmsMap,false);
	}

	public static String removeCharAt(String s, int pos) {
	      return s.substring(0, pos) + s.substring(pos + 1);
	   }

	public static boolean isDisneyShowOrderItemId(OrdOrder order) {
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			String specialTicketType = orderItem
					.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.specialTicketType
                            .name());
			if (SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_SHOW.name().equals(
					specialTicketType)
					|| SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_TICKET.name()
                    .equals(specialTicketType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDisneyShowOrder(OrdOrder order) {
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			String specialTicketType = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name());
			if (SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_SHOW.name()
                    .equals(specialTicketType)) {
				return true;
			}
		}
		return false;
	}

	public static String produtInfoDisneyShow(OrdOrder order) {
		String[] list=produtInfoDisneyShowList(order);
		StringBuffer sb = new StringBuffer();
			sb.append("，份数：");
			sb.append(list[0]);
			sb.append("，包含人数");
			sb.append(list[1]);
			sb.append("成人，");
			sb.append(list[2]);
			sb.append("儿童。");
		return sb.toString();
	}

	public static String[] produtInfoDisneyShowList(OrdOrder order) {
		StringBuffer sb = new StringBuffer();
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if (!CollectionUtils.isEmpty(ordItemList)) {
			// 产品名字
			Long quantity = 0L;
			Long quantityAdultPeople = 0L;
			Long quantityChildPeople = 0L;
			for (OrdOrderItem item : ordItemList) {
				// 份数
				quantity += item.getQuantity()==null?0L:item.getQuantity();
				// 成人
				quantityAdultPeople += calculateLong(item.getQuantity(),
						item.getAdultQuantity());
				// 儿童
				quantityChildPeople += calculateLong(item.getQuantity(),
						item.getChildQuantity());
			}
			sb.append(quantity);
			sb.append(",");
			sb.append(quantityAdultPeople);
			sb.append(",");
			sb.append(quantityChildPeople);
		}
		return 	sb.toString().split(",");
	}
	/**
	 * 游玩人后置未锁定短信模板填写
	 * @param content
	 * @param order
	 * @param param
	 * @return
	 */
	public static String fillSmsForPostposition(String content, OrdOrder order, Map<String,Object> param){
		
		if((order.getSubCategoryId()!=null
				&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId().longValue()
				&&!order.isContainApiFlightTicket())){
			return appendFlightStr(content, order, param);
		}
		if (order.getCategoryId()!=29){
			for (OrdOrderItem element : order.getOrderItemList()) {
				if(element.getCategoryId()==20 ||element.getCategoryId()==21){
					return appendFlightStr(content, order, param);
				}
			}
		}
		
		//获取产品信息
		StringBuilder productInfo = new StringBuilder();
		Map<String, Object> dataMap=new HashMap<String, Object>();
		if (StringUtil.isNotEmptyString(order.getProductName())) {
			dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField(),order.getProductName());//产品名称
		}
		//获取出游日期
		dataMap.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(),DateUtil.formatDate(order.getVisitTime(),"yyyy-MM-dd"));
		for (OrdOrderItem item : order.getOrderItemList()) {
			//保险名称
			if(item.getCategoryId()==3){
				dataMap.put(OrdSmsTemplate.FIELD.INSURANCE_NAME.getField(), item.getProductName());
				//保险规则
				dataMap.put(OrdSmsTemplate.FIELD.INSURANCE_SUPP_GOODS_NAME.getField(), item.getSuppGoodsName());
				//份数
				dataMap.put(OrdSmsTemplate.FIELD.INSURANCE_NUM.getField(), item.getQuantity());
			}
		}
		//拼装productInfo
		productInfo.append(dataMap.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()));
		productInfo.append(COMMA);
		productInfo.append("出游日期：");
		if(dataMap.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField())!=null){
			productInfo.append(dataMap.get(OrdSmsTemplate.FIELD.VISIT_TIME.getField()));
		}
		if(param.get("PRODUCTTYPE").equals(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name())){
			if(param.get("findFrontBusStop")!=null){
				//上车地点
				productInfo.append(COMMA);
				productInfo.append(param.get("findFrontBusStop").toString());
			}
		}
		if(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_NAME.getField())!=null){
			productInfo.append(COMMA);
			productInfo.append(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_NAME.getField()));
		}
		if(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_SUPP_GOODS_NAME.getField())!=null){
			productInfo.append(COMMA);
			productInfo.append(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_SUPP_GOODS_NAME.getField()));
		}
		if(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_NUM.getField())!=null){
			productInfo.append(COMMA);
			productInfo.append(dataMap.get(OrdSmsTemplate.FIELD.INSURANCE_NUM.getField())+"份");
		}
		
		//订单编号orderId
	    param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(), order.getOrderId());
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), productInfo.toString());
		
		//如果是出境的，添加出境联系方式
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE +"，境外请拨打：" + Constant.OUTBOUND_SERVICE_PHONE);
		}else{
			param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		}
		
		param.put(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField(), Constant.LVMAMA);
		return StringUtil.composeMessage(content, param);
	}

	public static void fillSmsForStampOrderPaid(String content) {
		
	}

	private static String appendFlightStrInfo(String content){
		StringBuilder sb = new StringBuilder();
        int index=content.indexOf("{productInfo}。");
        sb.append(content.substring(0, (index+14)));
        sb.append("请于起飞当日携带相关登记证件，提前90分钟到达机场办理登机手续！");
        sb.append(content.substring(index+14));
        return sb.toString();
	}
	private static String appendFlightStr(String content, OrdOrder order, Map<String, Object> param) {
		content=appendFlightStrInfo(content);
		StringBuffer sb=new StringBuffer();
		for (OrdOrderItem element : order.getOrderItemList()) {
			appendFlightCommon(sb,element);
			sb.append(";");
		}
		sb.deleteCharAt(sb.length()-1);
		param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(), order.getOrderId());
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		param.put(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField(), Constant.LVMAMA);
		return StringUtil.composeMessage(content, param);
	}
	
	public static String fillSmsForPostpositionLock(String content, OrdOrder order){
		Map<String,Object> param=new HashMap<String, Object>();
		StringBuilder sb=new StringBuilder();
		//根据orderId查询合同
		//根据orderId获取游玩人
		String email=null;
		if(order.getOrdPersonList()!=null&&order.getOrdPersonList().size()>0){
			for (OrdPerson person : order.getOrdPersonList()) {
				if(VstOrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(person.getPersonType())){
					sb.append("游玩人:"+person.getFullName());
					if(StringUtil.isNotEmptyString(person.getEnglishName()))
						sb.append("/【英文名:"+person.getEnglishName()+"】");
					if(StringUtil.isNotEmptyString(person.getIdNo()))
						sb.append("/"+person.getIdTypeName()+"："+person.getIdNo());
					if(StringUtil.isNotEmptyString(person.getMobile()))
						sb.append("/手机号码："+(person.getMobile()));
					sb.append(";");
				}
				if(VstOrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(person.getPersonType())){
					email=person.getEmail();
				}
			}
			if(sb.indexOf(";")>-1)
				sb.deleteCharAt(sb.length()-1);
		}
		//订单编号orderId
	    param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(), order.getOrderId());
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.TRAVELLER_INFO.getField(), sb.toString());
		if(order.getOrdTravelContractList()!=null&&order.getOrdTravelContractList().size()>0){
			String contract="";
			for (OrdTravelContract element : order.getOrdTravelContractList()) {
				contract+=element.getVersion();
				contract+=",";
			}
			contract=contract.substring(0, contract.length()-1);
			param.put(OrdSmsTemplate.FIELD.ECONTRACT.getField(), contract);
		}
		param.put(OrdSmsTemplate.FIELD.EMAIL.getField(), StringUtil.isEmptyString(email)?"":email);
		
		//如果是出境的，添加出境联系方式
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE +"，境外请拨打：" + Constant.OUTBOUND_SERVICE_PHONE);
		}else{
			param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		}
		
		param.put(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField(), Constant.LVMAMA);
		if(!order.hasPayed()){
			content=content.replace("已生效并", "");
		}
		return StringUtil.composeMessage(content, param);
	}
	/**
	 * 取消订单（关房）
	 * @param content
	 * @param order
	 * @return
	 * */
	public static String fillSmsOfCloseHouser(String content, OrdOrder order, Map<String,Object> param){
		if(Constants.DISTRIBUTOR_10.longValue() == order.getDistributorId().longValue()){
			content = getSmsContentForO2O(content);
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		String regex = "\\$\\{(.+?)\\}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String keyField = matcher.group(1);
			//订单编号orderId
			if(OrdSmsTemplate.FIELD.ORDER_ID.getField().equals(keyField)){
				dataMap.put(keyField, order.getOrderId());
			}
			//产品名称productName
			if(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.PRODUCT_NAME.getField()));
			}
			//商品名称suppGoodsName
			if(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.SUPP_GOODS_NAME.getField()));
			}
		
			if(OrdSmsTemplate.FIELD.ROOM_TYPE.getField().equals(keyField)){
				dataMap.put(keyField, param.get(OrdSmsTemplate.FIELD.ROOM_TYPE.getField()));
			}
		}
		if(MapUtils.isNotEmpty(param)){
			dataMap.putAll(param);
		}
		dataMap.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), assembleSms(order, param.get(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField())));

		// FIXME 预售订单未生成，无法取到
//		fillStampProductInfo(order, dataMap);

		String result = StringUtil.composeMessage(content, dataMap);
		LOG.info("fillSms: " + content + ", dateMap=" + JSONUtil.bean2Json(dataMap) + ", result=" + result);
		return result;
	}
	public static String fillSpecialFailSms(String content, OrdOrder order, List<Long> orderItemIdList){
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();//Set<Long> proSet = new HashSet<Long>();
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(org.apache.commons.collections.CollectionUtils.isNotEmpty(ordItemList)){
			for(OrdOrderItem item : ordItemList){
				for(Long orderItemId : orderItemIdList){
					if(item.getOrderItemId().equals(orderItemId)){//orderItemList只会存在一条匹配记录和order.getOrderItemList()匹配

							if(item.getProductId() != null){
								sb.append(item.getProductName());
							}
							//商品名称
							sb.append(replaceGoodsName(item.getSuppGoodsName()));

							param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
							//分销那边发短信，需要把最后的签名去掉
							if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
								param.put(SMSLVMAMAFLAG, N);
							}
							//客服电话customerServicePhone
							return fillSms(content, order, param);

					}
				}
			}
		}


		return content;
	}
	/**
	 * 玩乐商品自定义短信
	 * @param customMap
	 * @param item
	 */
	public static String  getPlayCustomSms(Map<String,String> customSmsMap,OrdOrderItem item){
		if(customSmsMap==null){
			return COMMA;
		}
		String customSms = customSmsMap.get(item.getOrderItemId().toString());
		if (StringUtils.isNotBlank(customSms) && customSms.trim().length() > 100) {
			customSms = customSms.trim().substring(0, 100);
		}
		if (StringUtils.isNotBlank(customSms)) {
			return COMMA + customSms.trim() + PERIOD;
		} else {
			return COMMA;
		}
	}
	/**
	 * 玩乐出游短信
	 * @param content
	 * @param order
	 * @param customMap
	 */
	public static String fillSmsForPlay(String content,OrdOrder order,Map<String,String> customMap){
		
		Map<String, Object> param = new HashMap<String,Object>();
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(),DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
		param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(),order.getOrderId());
		param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		param.put(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField(),Constant.CLIENT_SIGN);
		StringBuffer sb=new StringBuffer("");
		if(order!=null && !CollectionUtils.isEmpty(order.getOrderItemList())){
			for(OrdOrderItem item:order.getOrderItemList()){
				sb.append(item.getProductName()+COMMA + item.getSuppGoodsName() + COMMA + item.getQuantity() + "份");
				try {
					sb.append(SmsUtil.getPlayCustomSms(customMap,item));
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}
			}
			
		}
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(),sb);
		List<String> codeList = Arrays.asList(DISTRIBUTOR_CODE_ARRAY);
		if(order.getDistributorId() == 4L && codeList.contains(order.getDistributorCode())){
			content = content.replace(SmsUtil.REPLACEAPI,"");
		}
		return SmsUtil.fillSms(content, order, param);	
		
	}

	
	//机+酒专用
	public static String fillSmsForRouteAeroHotel(String content,
			OrdOrder order, List<OrdOrderItem> orderItems, SEND_NODE sendNode,
			Map<String, String> goodsCustomSms,
			Map<String, ProdProduct> itemProdProducts) {
		LOG.info("SmsUtil.fillSmsForRouteAeroHotel:orderId=" + order.getOrderId() + " sendNode=" + sendNode);
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuilder productInfo = new StringBuilder();
		
		//update-renjiangyi-支付后短信模板调整
		getProductInfoForPayRouteAeroHotelInsurance(productInfo, orderItems,itemProdProducts);
		
	    //订单编号orderId
	    param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(), order.getOrderId());

		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), productInfo.toString());

		//出游日期
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(), DateFormatUtils.format(order.getVisitTime(), "yyyy-MM-dd"));
		LOG.info("SmsUtil.fillSmsForRouteAeroHotel:orderId=" + order.getOrderId() +
				" content=" + content + " param=" + param.toString());
		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}
	/**
	 *  * 机+酒，已支付资审通过模板短信信息构建
	 * @param orderItems
	 * @param productInfo
	 */
	private static void getProductInfoForPayRouteAeroHotelInsurance(
			StringBuilder productInfo, List<OrdOrderItem> orderItems,
			Map<String, ProdProduct> itemProdProducts) {
		LOG.info("SmsUtil.getProductInfoForPayRouteAeroHotel::productInfo=" + productInfo + " orderItems.size()=" + orderItems.size());
		//保存机票信息
		List<Map<String, String>> aeroInfoList = new ArrayList<Map<String, String>>();
		//保存酒店信息
		List<Map<String, String>> hotelInfoList = new ArrayList<Map<String, String>>();
		//保存保险信息
		List<Map<String, String>> insuranceList = new ArrayList<Map<String, String>>();
		if (!CollectionUtils.isEmpty(orderItems)) {
			for (OrdOrderItem orderItem : orderItems) {
				if (BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(orderItem.getCategoryId())) {
					Map<String, String> aeroMap = new HashMap<String, String>();
					Map<String, String> aeroMap2 = new HashMap<String, String>();
					//获取起飞机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("fromAirport"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("fromAirport"));
						 aeroMap.put("fromAirport", orderItem.getContentStringByKey("fromAirport"));
					 }
					//获取起飞航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("startTerminal"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("startTerminal"));
						 aeroMap.put("startTerminal", orderItem.getContentStringByKey("startTerminal"));
					 }
					//获取到达机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("toAirport"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("toAirport"));
						 aeroMap.put("toAirport", orderItem.getContentStringByKey("toAirport"));
					 }
					//获取到达航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTerminal"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("arriveTerminal", orderItem.getContentStringByKey("arriveTerminal"));
					 }
					//获取起飞时间
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureTime"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("departureTime", orderItem.getContentStringByKey("departureTime"));
					 }
					//获取降落时间
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTime"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("arriveTime", orderItem.getContentStringByKey("arriveTime"));
					 }
					//获取航班号
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("flightNo", orderItem.getContentStringByKey("flightNo"));
					 }
					//获取航空公司
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("airCompany"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("airCompany", orderItem.getContentStringByKey("airCompany"));
					 }
					//获取人数
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("adult_quantity"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("adult_quantity", orderItem.getContentStringByKey("adult_quantity"));
					 }
					//获取出发地
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureCity"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("departureCity", orderItem.getContentStringByKey("departureCity"));
					 }
					//获取目的地
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveCity"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap.put("arriveCity", orderItem.getContentStringByKey("arriveCity"));
					 }
					 if(aeroMap.size()>0) aeroInfoList.add(aeroMap);
					 //获取起飞机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("fromAirport.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("fromAirport"));
						 aeroMap2.put("fromAirport", orderItem.getContentStringByKey("fromAirport.2"));
					 }
					//获取起飞航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("startTerminal.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("startTerminal"));
						 aeroMap2.put("startTerminal", orderItem.getContentStringByKey("startTerminal.2"));
					 }
					//获取到达机场
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("toAirport.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("toAirport"));
						 aeroMap2.put("toAirport", orderItem.getContentStringByKey("toAirport.2"));
					 }
					//获取到达航站楼
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTerminal.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("arriveTerminal", orderItem.getContentStringByKey("arriveTerminal.2"));
					 }
					//获取起飞时间
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureTime.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("departureTime", orderItem.getContentStringByKey("departureTime.2"));
					 }
					//获取降落时间
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTime.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("arriveTime", orderItem.getContentStringByKey("arriveTime.2"));
					 }
					//获取航班号
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("flightNo", orderItem.getContentStringByKey("flightNo.2"));
					 }
					//获取航空公司
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("airCompany.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("airCompany", orderItem.getContentStringByKey("airCompany.2"));
					 }
					//获取人数
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("adult_quantity", orderItem.getContentStringByKey("adult_quantity"));
					 }
					//获取出发地
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureCity.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("departureCity", orderItem.getContentStringByKey("departureCity.2"));
					 }
					//获取目的地
					 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveCity.2"))) {
						 //aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal"));
						 aeroMap2.put("arriveCity", orderItem.getContentStringByKey("arriveCity.2"));
					 }
					 if(aeroMap2.size()>0) aeroInfoList.add(aeroMap2);
				} else if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
					hasHotel=true;
					Map<String, String> hotelMap = new HashMap<String, String>();
					//酒店名称
					hotelMap.put("hotelName", orderItem.getProductName());
					//入住日期
					if (orderItem.getVisitTime() != null) {
						hotelMap.put("visitDate", DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd"));
					}
					String leaveDate = getLeaveDateOfHotel(orderItem);
					if (leaveDate != null) {
						hotelMap.put("leaveDate", leaveDate);
					}
					ProdProduct product = itemProdProducts.get(orderItem.getOrderItemId().toString());
					hotelInfoList.add(hotelMap);
				} else if(BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
					Map<String, String> insuranceMap = new HashMap<String, String>();
					//保险名称
					insuranceMap.put(OrdSmsTemplate.FIELD.INSURANCE_NAME.getField(), orderItem.getProductName());
					//保险规则
					insuranceMap.put(OrdSmsTemplate.FIELD.INSURANCE_SUPP_GOODS_NAME.getField(), orderItem.getSuppGoodsName());
					//份数
					insuranceMap.put(OrdSmsTemplate.FIELD.INSURANCE_NUM.getField(), orderItem.getQuantity().toString());
					insuranceList.add(insuranceMap);
				}
			}
		}
		//buildProductInfo(productInfo, aeroInfoList, hotelInfoList，insuranceList);
		buildProductInfoUpdate(productInfo, aeroInfoList, hotelInfoList,insuranceList);
	}

	private static void buildProductInfoUpdate(StringBuilder productInfo,
			List<Map<String, String>> aeroInfoList,
			List<Map<String, String>> hotelInfoList,
			List<Map<String, String>> insuranceList) {
		LOG.info("SmsUtil.buildProductInfo::productInfo=" + productInfo + " aeroInfoList=" + aeroInfoList.toString()
				+ " hotelInfoList=" + hotelInfoList.toString()+" insuranceList=" + insuranceList.toString());
		if (!CollectionUtils.isEmpty(aeroInfoList)) {
			productInfo.append("去程航班：");
			int i=1;
			for (Map<String, String> itemMap : aeroInfoList) {
				if(i==2){
					productInfo.append("回程航班：");
				}
				String departureTime="";
				String arriveTime="";
				//起飞时间年月日
				if (StringUtil.isNotEmptyString(itemMap.get("departureTime"))) {
					String year=itemMap.get("departureTime").substring(0, 10);
					departureTime=itemMap.get("departureTime").substring(11);
					productInfo.append(year);
				}
				//到达时间
				if (StringUtil.isNotEmptyString(itemMap.get("arriveTime"))) {
					arriveTime=itemMap.get("arriveTime").substring(11);
				}
				//出发地
				if (StringUtil.isNotEmptyString(itemMap.get("departureCity"))) {
					productInfo.append(itemMap.get("departureCity"));
				}
				//目的地
				if (StringUtil.isNotEmptyString(itemMap.get("arriveCity"))) {
					productInfo.append(LINE);
					productInfo.append(itemMap.get("arriveCity"));
				}
				//数量
				if (StringUtil.isNotEmptyString(itemMap.get("adult_quantity"))) {
					productInfo.append(itemMap.get("adult_quantity"));
					productInfo.append("份");
				}
				//航空公司
				if (StringUtil.isNotEmptyString(itemMap.get("airCompany"))) {
					productInfo.append(itemMap.get("airCompany"));
				}
				//航班号
				if (StringUtil.isNotEmptyString(itemMap.get("flightNo"))) {
					productInfo.append(itemMap.get("flightNo"));
				}
				//起飞机场
				String fromAirport = itemMap.get("fromAirport");
				if (StringUtil.isNotEmptyString(fromAirport)) {
					productInfo.append(fromAirport);
					//起飞航站楼
					if (StringUtil.isNotEmptyString(itemMap.get("startTerminal"))) {
						productInfo.append(itemMap.get("startTerminal"));
					}
					productInfo.append(SPACE);
					productInfo.append(departureTime);
				}
				//到达机场
				String toAirport = itemMap.get("toAirport");
				if (StringUtil.isNotEmptyString(toAirport)) {
					productInfo.append(LINE);
					productInfo.append(toAirport);
					if (StringUtil.isNotEmptyString(itemMap.get("arriveTerminal"))) {
						productInfo.append(itemMap.get("arriveTerminal"));
					}
					productInfo.append(SPACE);
					productInfo.append(arriveTime);
				}
				if(aeroInfoList.size()>1&&aeroInfoList.size()!=i){
					productInfo.append(COMMA);}
				else{
					productInfo.append(PERIOD);}
				i++;
			}
		}
		LOG.info("SmsUtil.buildProductInfo_aero::productInfo=" + productInfo);
		if (!CollectionUtils.isEmpty(hotelInfoList)) {
			int i=1;
			//酒店：三亚亚龙湾红森林度假酒店 2016-12-05至 2016-12-09。
			productInfo.append("酒店：");
			for (Map<String, String> itemMap : hotelInfoList) {
				//酒店名称
				String hotelName = itemMap.get("hotelName");
				if (StringUtil.isNotEmptyString(hotelName)) {	
					productInfo.append(hotelName);
					//入住日期离店日期
					if (StringUtil.isNotEmptyString(itemMap.get("visitDate"))) {
						productInfo.append(itemMap.get("visitDate"));
						if (StringUtil.isNotEmptyString(itemMap.get("leaveDate"))) {
							productInfo.append(UNTIL);
							productInfo.append(itemMap.get("leaveDate"));
						}
					}
				}
				if(hotelInfoList.size()>1&&hotelInfoList.size()!=i){
					productInfo.append(COMMA);}
				else{
					productInfo.append(PERIOD);}
				i++;
			}
			LOG.info("SmsUtil.buildProductInfo_hotel::productInfo=" + productInfo);
		}
		
		if (!CollectionUtils.isEmpty(insuranceList)) {
			int i=1;
			//保险订购为：人保驴妈妈国内5日游意外险，全面型，30万元保额，3份。
			productInfo.append("保险订购为：");
			for (Map<String, String> itemMap : insuranceList) {
				//保险名字
				if (StringUtil.isNotEmptyString(itemMap.get("insuranceName"))) {
					productInfo.append(itemMap.get("insuranceName"));
				}
				//保险规则
				if (StringUtil.isNotEmptyString(itemMap.get("insuranceSuppGoodsName"))) {
					productInfo.append(itemMap.get("insuranceSuppGoodsName"));
				}
				//保险份数
				if (StringUtil.isNotEmptyString(itemMap.get("insuranceNum"))) {
					productInfo.append(itemMap.get("insuranceNum"));
					productInfo.append("份");
				}
				if(insuranceList.size()>1&&insuranceList.size()!=i){
					productInfo.append(COMMA);}
				else{
					productInfo.append(PERIOD);}
				i++;
			}
		}
	}
	
	/**
	 * 增加支付前置短信模板
	 * @return
	 */
	public static String orderAdvancePaySmsTemplate(){
		return ADVANCE_PAY_SMS_TEMP;
	}
	/**
	 * 自主打包短信
	 * 国内机酒订单支付前置支付,资源审核通过短信
	 * @param context
	 * @param order
	 * @return
	 */
	public static String orderAdvancePaySmsResourceAmplTemplateForLVMAMA(OrdOrder order){
		if(null == order){
			return null;
		}
		Map<String, Object> map = new HashMap<String,Object>();
		//获取订单号
		map.put("orderId", order.getOrderId());
		//获取出游日期
		map.put("visitDate", DateFormatUtils.format(order.getVisitTime(), "yyyy-MM-dd"));
		//保存机票信息
		StringBuilder aeroInfo = new StringBuilder();
		//保存酒店信息
		StringBuilder hotelInfo = new StringBuilder();
		//保存保险信息
		StringBuilder insuranceInfo = new StringBuilder();
		List<OrdOrderItem> items=new ArrayList<OrdOrderItem>();
		boolean isback=true;
		LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "自由行机酒短信内容拼接");
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			if (BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(orderItem.getCategoryId())) {
				String flightType = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightType.name());
				if(null!=flightType&&flightType.equals("2")&&isback){
					items.add(orderItem);
				}else{
					getAeroInfo(aeroInfo, orderItem);
					if(null!=flightType&&flightType.equals("1")){
						isback=false;
						if(null!=items&&items.size()>0){
							getAeroInfo(aeroInfo, items.get(0));
						}
					}
				}
			}else if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==orderItem.getCategoryId().longValue()){
				//酒店名称
				hotelInfo.append(orderItem.getProductName()+SPACE);
				//入住日期
				if (orderItem.getVisitTime() != null) {
					hotelInfo.append(DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd"));
				}
				String leaveDate = getLeaveDateOfHotel(orderItem);
				if (leaveDate != null) {
					hotelInfo.append(UNTIL+leaveDate);
				}
				hotelInfo.append(PERIOD);
				/*//房型
				if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("branchName"))) {
					hotelMap.put("branchName", orderItem.getContentStringByKey("branchName"));
				}*/
				//数量
				/*if (orderItem.getQuantity() != null) {
					hotelMap.put("quantity", orderItem.getQuantity().toString());
				}*/
			}else if(BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().longValue()==orderItem.getCategoryId().longValue()){
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "保险数据");
				insuranceInfo.append("保险订购为：");
				insuranceInfo.append(orderItem.getProductName()+COMMA);
				insuranceInfo.append(orderItem.getSuppGoodsName()+COMMA);
				insuranceInfo.append(orderItem.getQuantity()+"份");
				insuranceInfo.append(PERIOD);
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + insuranceInfo.toString());
			}
		}
		return buidSMSInfo(map,PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE_LVMAMA,aeroInfo,hotelInfo,insuranceInfo);
	}
	/**
	 * 
	 * @param context
	 * @param aeroInfo
	 * @param hotelInfoList
	 * @return
	 */
	private static String buidSMSInfo(Map<String, Object> data,String context,StringBuilder aeroInfo,StringBuilder hotelInfo,StringBuilder insuranceInfo){
		StringBuilder productInfo=new StringBuilder();
		if(aeroInfo!=null && aeroInfo.length() > 0){
			productInfo.append(aeroInfo.toString());
		}
		if(null != hotelInfo && hotelInfo.length() > 0){
			productInfo.append(hotelInfo.toString());
		}
		if(null != insuranceInfo && insuranceInfo.length() > 0){
			productInfo.append(insuranceInfo.toString());
		}
		data.put("productInfo", productInfo.toString());
		data.put("customerServicePhone",Constant.CUSTOMER_SERVICE_PHONE);
		data.put("clientSign", Constant.LVMAMA);
		return StringUtil.composeMessage(context, data);
	}
	//获取机票信息
	private static void getAeroInfo(StringBuilder aeroInfo, OrdOrderItem orderItem) {
		String minuteSecFrom="";
		String minuteSecTo="";
		if(StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightType"))){
			 String flightType = orderItem.getContentStringByKey("flightType");
			 if(null!=flightType&&flightType.equals("2")){
				 aeroInfo.append("回程航班：");
			 }else{
				 aeroInfo.append("去程航班：");
			 }
		 }	
		//获取起飞时间
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureTime"))) {
			 String[] dates=orderItem.getContentStringByKey("departureTime").split(" ");
			 if(dates != null && dates.length > 1){
				 minuteSecFrom=dates[1];
			 }
			 aeroInfo.append(dates[0]+SPACE);
		 }
		//获取出发地
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureCity"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("departureCity"));
		 }
		//获取目的地
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveCity"))) {
			 aeroInfo.append(LINE+orderItem.getContentStringByKey("arriveCity")+SPACE);
		 }
		//获取人数
		/* if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("adult_quantity"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("adult_quantity")+"份"+SPACE);
		 }*/
		//获取航空公司
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("airCompany"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("airCompany")+SPACE);
		 }
		//获取航班号
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("flightNo")+SPACE);
		 }
		//获取起飞机场
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("fromAirport"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("fromAirport")+SPACE);
		 }
		//获取起飞航站楼
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("startTerminal"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("startTerminal")+SPACE);
		 }
		 if(minuteSecFrom.length()>0){
			 aeroInfo.append(minuteSecFrom+LINE);
		 }
		//获取到达机场
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("toAirport"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("toAirport"));
		 }
		//获取到达航站楼
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTerminal"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal")+SPACE);
		 }
		//获取降落时间
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTime"))) {
			 String[] dates=orderItem.getContentStringByKey("arriveTime").split(" ");
			 if(dates != null && dates.length > 1){
				 aeroInfo.append(dates[1]);
			 }else{
				 aeroInfo.append(dates[0]);
			 }
		 }
		
		
		 aeroInfo.append(PERIOD);
		 if(StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightType.2"))){
			 aeroInfo.append("回程航班：");
		 }
		//获取起飞时间
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureTime.2"))) {
			 String[] dates=orderItem.getContentStringByKey("departureTime.2").split(" ");
			 if(dates != null && dates.length > 1){
				 minuteSecTo=dates[1];
			 }
			 aeroInfo.append(dates[0]+SPACE);
		 }
		//获取出发地
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("departureCity.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("departureCity.2"));
		 }
		//获取目的地
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveCity.2"))) {
			 aeroInfo.append(LINE+orderItem.getContentStringByKey("arriveCity.2")+SPACE);
		 }
		//获取人数
		/* if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("adult_quantity"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("adult_quantity")+"份"+SPACE);
		 }*/
		//获取航空公司
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("airCompany.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("airCompany.2")+SPACE);
		 }
		//获取航班号
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightNo.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("flightNo.2")+SPACE);
		 }
		 //获取起飞机场
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("fromAirport.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("fromAirport.2")+SPACE);
		 }
		//获取起飞航站楼
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("startTerminal.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("startTerminal.2")+SPACE);
		 }
		 if(minuteSecTo.length()>0){
			 aeroInfo.append(minuteSecTo+LINE);
		 }
		//获取到达机场
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("toAirport.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("toAirport.2")+SPACE);
		 }
		//获取到达航站楼
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTerminal.2"))) {
			 aeroInfo.append(orderItem.getContentStringByKey("arriveTerminal.2")+SPACE);
		 }
		
		//获取降落时间
		 if (StringUtil.isNotEmptyString(orderItem.getContentStringByKey("arriveTime.2"))) {
			 String[] dates=orderItem.getContentStringByKey("arriveTime.2").split(" ");
			 if(dates != null && dates.length > 1){
				 aeroInfo.append(dates[1]);
			 }else{
				 aeroInfo.append(dates[0]);
			 }
		 }
		 if(StringUtil.isNotEmptyString(orderItem.getContentStringByKey("flightType.2"))){
			 aeroInfo.append(PERIOD);
		 }
	}


	public static String orderAdvancePaySmsResourceAmplTemplateForSUPPLIER(OrdOrder order,ProdProduct product){
		if(null == order){
			return null;
		}
		Map<String, Object> map = new HashMap<String,Object>();
		//获取订单号
		map.put("orderId", order.getOrderId());
		//获取出游日期
		map.put("visitDate", DateFormatUtils.format(order.getVisitTime(), "yyyy-MM-dd"));
		//获取产品名称
		map.put("productName", order.getProductName());
		return buidSMSInfo(map,PAYAHEAD_PAY_AERO_HOTEL_RESOURCE_AMPLE_SUPPLIER,null,null,null);
	}
	
	/**
	 * 支付前置-机+酒订单支付超时取消
	 * @param order
	 * @return
	 */
	public static String orderAdvancePaySmsTemplateCancel(OrdOrder order){
		Map<String, Object> map = new HashMap<String,Object>();
		//获取订单号
		map.put("orderId", order.getOrderId());
		return buidSMSInfo(map,ADVANCE_PAY_SMS_TEMP_TIMEOUT_CECANEL,null,null,null);
	}

	
	//判断订单是否为境外产品
	private static String isToForeign(OrdOrder order){
		String isToForeign = "N";
		
		//打包产品
		if (order.getOrderPackList() != null && order.getOrderPackList().size() > 0) {
			for(OrdOrderPack pack : order.getOrderPackList()) {
				for (OrdOrderItem item : pack.getOrderItemList()) {
					isToForeign = item.getContentStringByKey(VstOrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name());
					if("Y".equals(isToForeign)){
		            	break;
		            }
				}
			}
		}
		
		//非打包产品
		if(order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
			for(OrdOrderItem item : order.getOrderItemList()){
				if(item.getOrderPackId() == null){
					isToForeign = item.getContentStringByKey(VstOrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name());
					if("Y".equals(isToForeign)){
		            	break;
		            }
				}
			}
		}
	    	
		return isToForeign;
	}

	public static String orderAdvancePaySmsTemplateForBusHotel(String content, OrdOrder order, Map<Long, ProdTraffic> prodMap) {
		if(null == order){
			return null;
		}
		Map<String, Object> map = new HashMap<String,Object>();
		//获取订单号
		map.put("orderId", order.getOrderId());
		//获取总价
		map.put("oughtAmount", order.getOughtAmountYuan());
		//获取退改规则
		map.put("cancelStrategy", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(order.getCancelStrategy()));
		//获取出游日期
		map.put("visitDate", DateFormatUtils.format(order.getVisitTime(), "yyyy-MM-dd"));
		//保存巴士信息
		StringBuilder busInfo = new StringBuilder();
		//保存酒店信息
		StringBuilder hotelInfo = new StringBuilder();
		//保存保险信息
		StringBuilder insuranceInfo = new StringBuilder();
		LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "自由行巴士+酒短信内容拼接");
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
				busInfo.append(orderItem.getProductName()+SPACE);
				busInfo.append(orderItem.getSuppGoodsName());
				ProdTraffic prodTraffic = prodMap.get(orderItem.getProductId());
				if(prodTraffic.getBackType()!=null && prodTraffic.getToType()!=null){
					busInfo.append("往返巴士票");
				}else {
					busInfo.append("单程巴士票");
				}
				busInfo.append(orderItem.getQuantity()+"张");
				busInfo.append(COMMA);
			}else if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==orderItem.getCategoryId().longValue()){
				//酒店名称
				hotelInfo.append(orderItem.getProductName()+SPACE);
				//商品名称
				hotelInfo.append(orderItem.getSuppGoodsName());
				//房间数
				hotelInfo.append(orderItem.getQuantity()+"间"+PERIOD);
			}else if(BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().longValue()==orderItem.getCategoryId().longValue()){
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "保险数据");
				insuranceInfo.append(orderItem.getProductName()+SPACE);
				insuranceInfo.append(orderItem.getSuppGoodsName()+SPACE);
				insuranceInfo.append(orderItem.getQuantity()+"份");
				insuranceInfo.append(PERIOD);
				LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + insuranceInfo.toString());
			}
		}
		return buidSMSInfo(map,content,busInfo,hotelInfo,insuranceInfo);
	}

	public static String orderAdvancePaySmsTemplateForBus(String content,
			OrdOrder order, Map<Long, ProdTraffic> prodTrafficMap, Map<Long, ProdProductBranch> prodProductBranchMap, Map<Long, SuppGoodsBus> suppGoodsBusMap, Map<String, ProdProduct> itemProdProducts) {
		if(null == order){
			return null;
		}
		Map<String, Object> map = new HashMap<String,Object>();
		//获取订单号
		map.put("orderId", order.getOrderId());
		//保存巴士信息
		StringBuilder busInfo = new StringBuilder();
		//保存酒店信息
		StringBuilder hotelInfo = new StringBuilder();
		OrdOrderItem orderItem1=null;
		OrdOrderItem orderItem2=null;
		LOG.info("SmsUtil.fillSms:orderId=" + order.getOrderId() + "自由行巴士+酒 巴士部分短信内容拼接");
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			if (BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().equals(orderItem.getCategoryId())) {
				if(null==orderItem1){
					orderItem1=orderItem;
				}else{
					orderItem2=orderItem;
				}
			}
			if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==orderItem.getCategoryId().longValue()){
				//酒店名称
				hotelInfo.append(orderItem.getProductName()+COMMA);
				//商品名称
				hotelInfo.append(orderItem.getSuppGoodsName()+COMMA);
				//入住日期
				if (orderItem.getVisitTime() != null) {
					hotelInfo.append(DateFormatUtils.format(orderItem.getVisitTime(), "yyyy-MM-dd"));
				}
				String leaveDate = getLeaveDateOfHotel(orderItem);
				if (leaveDate != null) {
					hotelInfo.append(UNTIL+leaveDate);
				}
				ProdProduct product = itemProdProducts.get(orderItem.getOrderItemId().toString());
				if(product != null&&product.getPropValue()!=null){
					hotelInfo.append(product.getPropValue().get("address") == null ? "" : ",酒店地址："+product.getPropValue().get("address").toString());
					hotelInfo.append(product.getPropValue().get("telephone")==null ? "" : ",酒店电话："+product.getPropValue().get("telephone").toString());
				}
				hotelInfo.append(PERIOD);
			}	
		}
		if(orderItem2==null){
		    ProdProductBranch prodProductBranch=prodProductBranchMap.get(orderItem1.getProductId());
	    	SuppGoodsBus suppGoodsBus = suppGoodsBusMap.get(orderItem1.getProductId());
			busInfo.append(orderItem1.getProductName()+SPACE);
			busInfo.append(orderItem1.getSuppGoodsName());
			ProdTraffic prodTraffic = prodTrafficMap.get(orderItem1.getProductId());
			boolean isToBack=true;
			if(prodTraffic.getBackType()!=null && prodTraffic.getToType()!=null){
				busInfo.append("往返巴士票");
				isToBack=true;
			}else {
				busInfo.append("单程巴士票");
				isToBack=false;
			}
			busInfo.append(orderItem1.getQuantity()+"张");
			busInfo.append(COMMA);
			busInfo.append("份数："+orderItem1.getQuantity()+",包含人数"+orderItem1.getQuantity()+"人"+PERIOD);
			busInfo.append("游玩时间："+DateUtil.formatDate(orderItem1.getVisitTime(), "yyyy-MM-dd")+COMMA);
			busInfo.append("去程：上车时间："+ suppGoodsBus.getToDepartureTime()+SPACE+"上车地点："+prodProductBranch.getDepartureStation()+PERIOD);
			if(isToBack){
				busInfo.append("回程：上车时间："+ suppGoodsBus.getBackDepartureTime()+SPACE+"上车地点："+prodProductBranch.getArrivalStation()+PERIOD);
			}
		}else{
			Date visitTime1 = orderItem1.getVisitTime();
			Date visitTime2 = orderItem2.getVisitTime();
			if(visitTime1.after(visitTime2)){
				OrdOrderItem orderItem3=orderItem1;
				orderItem1=orderItem2;
				orderItem2=orderItem3;
			}
		    ProdProductBranch prodProductBranch=prodProductBranchMap.get(orderItem1.getProductId());
	    	SuppGoodsBus suppGoodsBus = suppGoodsBusMap.get(orderItem1.getProductId());
			busInfo.append(orderItem1.getProductName()+SPACE);
			busInfo.append(orderItem1.getSuppGoodsName());
			busInfo.append("单程巴士票");
			busInfo.append(orderItem1.getQuantity()+"张");
			busInfo.append(COMMA);
			busInfo.append("份数："+orderItem1.getQuantity()+",包含人数"+orderItem1.getQuantity()+"人"+PERIOD);
			busInfo.append("去程日期："+DateUtil.formatDate(orderItem1.getVisitTime(), "yyyy-MM-dd")+COMMA);
			busInfo.append("去程：上车时间："+ suppGoodsBus.getToDepartureTime()+SPACE+"上车地点："+prodProductBranch.getDepartureStation()+PERIOD);
			
		    ProdProductBranch prodProductBranch1=prodProductBranchMap.get(orderItem2.getProductId());
	    	SuppGoodsBus suppGoodsBus1 = suppGoodsBusMap.get(orderItem2.getProductId());;
			busInfo.append(orderItem2.getProductName()+SPACE);
			busInfo.append(orderItem2.getSuppGoodsName());
			busInfo.append("单程巴士票");
			busInfo.append(orderItem2.getQuantity()+"张");
			busInfo.append(COMMA);
			busInfo.append("份数："+orderItem2.getQuantity()+",包含人数"+orderItem2.getQuantity()+"人"+PERIOD);
			busInfo.append("返程日期："+DateUtil.formatDate(orderItem2.getVisitTime(), "yyyy-MM-dd")+COMMA);
			busInfo.append("回程：上车时间："+ suppGoodsBus1.getToDepartureTime()+SPACE+"上车地点："+prodProductBranch1.getDepartureStation()+PERIOD);
		}
		
		return buidSMSInfo(map,content,busInfo,hotelInfo,null);
		
	}

	/**
	 * 长隆特殊短信
	 * @param content
	 * @param order
	 * @param orderItemList订单子项列表
	 * @param map辅助码
	 * @return
	 */
	public static String fillSmsForChimeLongProvider(String content, OrdOrder order, List<Long> orderItemList,Map<String, Object> map,
																	Map<String,String> addressMap, Map<String,String> enterStyleMap, Map<String,String> timeMap, Map<String,String> goodsCustomSmsMap,Map<Long,String> specialSmsMap) {
		LOG.info("SmsUtil.fillSmsForChimeLongProvider:orderId=" + order.getOrderId());
		List<String> auxiliaryCodeList = (List<String>)map.get("auxiliaryCodeList");
		LOG.info("SmsUtil.fillSmsForPrepaidNoAperiodicTicket:orderId=" + order.getOrderId() + "auxiliaryCode:" + (auxiliaryCodeList != null && auxiliaryCodeList.size() > 0? auxiliaryCodeList.get(0) : null));
		Map <String,Object> param = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		String chimeLongCode=null;
		if(map.get("chimeLongCode")!=null){
			 chimeLongCode = (String)map.get("chimeLongCode");
		}
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if(ordItemList != null && ordItemList.size() > 0){
			for(OrdOrderItem item : ordItemList){
				for(int i=0;i<orderItemList.size();i++){
					if(item.getOrderItemId().equals(orderItemList.get(i))){
						//1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
						if(item.getCategoryId()==11||item.getCategoryId()==12||item.getCategoryId()==13){
										//商品名称
										String goodsName = replaceGoodsName(item.getSuppGoodsName());
										if(specialSmsMap.get(item.getSuppGoodsId())==null||"".equalsIgnoreCase(specialSmsMap.get(item.getSuppGoodsId()))){
											Long adultquantity = calculateLong(item.getQuantity(), item.getAdultQuantity());
											Long childrenQuantity = calculateLong(item.getQuantity(), item.getChildQuantity());
											sb.append("“"+goodsName+"全票”"+ adultquantity+"张" );
											sb.append("“"+goodsName+"儿童长者票”"+ childrenQuantity+"张,");
										}else{
											sb.append("，");

										}
										//使用日期
										sb.append("使用日期：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
										//取票时间
										String ticketTime = getTicketTimeStr(timeMap, item);
										sb.append(ticketTime);
										if(auxiliaryCodeList!=null){
											for (String s : auxiliaryCodeList) {
												sb.append(s);
											}
										}
										String address = "到长隆园区自助取票机上或售票处扫码换取纸质门票";
										sb.append(address);
										String customSms = "";
										if(goodsCustomSmsMap != null){
											customSms = goodsCustomSmsMap.get(item.getOrderItemId().toString());
											if(!StringUtil.isEmptyString(customSms.trim()) && (customSms.length() > 200)){
												customSms = customSms.substring(0, 199);
											}else if(StringUtil.isEmptyString(customSms.trim())){
												customSms = "";
											}
										}
										if(StringUtils.isNotEmpty(customSms)){
											//商品自定义短信
											sb.append(COMMA + customSms + " ");
										}else{
											sb.append(" ");
										}
						}
					}
				}
			}
		}
		//产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		//通关信息
		param.put(OrdSmsTemplate.FIELD.QRCODE.getField(),chimeLongCode);
		//分销那边发短信，需要把最后的签名去掉
		if("N".equalsIgnoreCase(order.getSmsLvmamaFlag())){
			param.put(SMSLVMAMAFLAG, N);
		}
		//客服电话customerServicePhone
		return fillSms(content, order, param);
	}

	//国内出游前一日关怀短信
	public static String fillSmsForPerformPreviousDay(OrdOrder order) {
		StringBuffer sb = new StringBuffer();
		sb.append("【温馨提醒】尊敬的贵宾，您好！您预订的");
		String visitTime = DateUtil.SimpleFormatDateToString(order.getVisitTime());
		sb.append(visitTime+"出发的旅游产品，订单号"+order.getOrderId()+"。请您带齐所有出游有效证件，如您是飞机行程，请您务必提前2小时到达机场候机，如有疑问请致电驴妈妈24小时客服热线"+Constant.CUSTOMER_SERVICE_PHONE);
		sb.append("，您的旅途即将启程，祝您出游愉快。"+Constant.LVMAMA);
		return sb.toString();
	}

	//出境出游前一日关怀短信
	public static String fillSmsForPerformPreviousDay_outBu(String content, OrdOrder order) {
		Map <String,Object> param = new HashMap<String, Object>();
		param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(),order.getOrderId());
		param.put(OrdSmsTemplate.FIELD.VISIT_TIME.getField(),DateUtil.SimpleFormatDateToString(order.getVisitTime()));
		param.put(OrdSmsTemplate.FIELD.CUSTOMER_SERVICE_PHONE.getField(),Constant.CUSTOMER_SERVICE_PHONE);
		param.put(OrdSmsTemplate.FIELD.CLIENT_SIGN.getField(),Constant.LVMAMA);
		return fillSms(content, order, param);
	}

	public static String fillSmsForPaySuccessWifi(String content, OrdOrder order) {
		Map <String,Object> param = new HashMap<String, Object>();
		param.put(OrdSmsTemplate.FIELD.ORDER_ID.getField(),order.getOrderId());

		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		StringBuilder stringBuilder = new StringBuilder();
		for (OrdOrderItem ordOrderItem : orderItemList) {
			if(BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(ordOrderItem.getCategoryId())) {
				stringBuilder.append(ordOrderItem.getSuppGoodsName());
				stringBuilder.append(",");
			}
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-1);

		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(),stringBuilder.toString());
		return fillSms(content, order, param);

	}

	/**
	 * 填充订单改期模板内容
	 * @param content
	 * @param order
	 * @param addressMap
	 * @param enterStyleMap
	 * @param timeMap
	 * @return
	 */
	public static String fillSmsForOrdReviseDate(String content, OrdOrder order, Map<String, String> addressMap
			, Map<String, String> enterStyleMap, Map<String, String> timeMap) {
		Map<String, Object> param = new HashMap<String, Object>();
		// 拼装短信内容
		StringBuffer sb = new StringBuffer();
		Set<Long> proSet = new HashSet<Long>();

		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		if (!CollectionUtils.isEmpty(ordItemList)) {
			for (OrdOrderItem item : ordItemList) {
				// 1现付(order)、2门票(item)、3期票(item)、4供应商(item)、5二维码(item)
				if (item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()
						|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
						|| item.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()) {
					// 预付
					if (order.hasNeedPrepaid()) {
						// 非期票
						if (!item.hasTicketAperiodic()) {
							if (item.getProductId() != null) {
								if (!proSet.contains(item.getProductId())) {
									proSet.add(item.getProductId());
									// 产品名称
									sb.append(item.getProductName());
								}
							}
							// 商品名称
							sb.append(SmsUtil.replaceGoodsName(item.getSuppGoodsName()));
							// 份数
							sb.append("，份数：" + item.getQuantity());
							String personContent = "";
							long adultNum = SmsUtil.calculateLong(item.getQuantity(), item.getAdultQuantity());
							long childNum = SmsUtil.calculateLong(item.getQuantity(), item.getChildQuantity());
							if (adultNum != 0 || childNum != 0) {
								personContent = "，包含人数";
								if (adultNum != 0 && childNum != 0) {
									personContent = personContent + adultNum + "成人," + childNum + "儿童。";
								}
								if (adultNum != 0 && childNum == 0) {
									personContent = personContent + adultNum + "成人。";
								}
								if (adultNum == 0 && childNum != 0) {
									personContent = personContent + childNum + "儿童。";
								}
							}
							// 取票时间
							String ticketTime = SmsUtil.getTicketTimeStr(timeMap, item);
							sb.append(ticketTime);
							// 游玩时间
							if (null != item.getVisitTime()) {
								sb.append("游玩时间：" + DateUtil.formatDate(item.getVisitTime(), "yyyy-MM-dd"));
							}
							// 添加场次时间
							String circusActStartTime = item.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.circusActStartTime.name());
							String circusActEndtime = (String) item
									.getContentValueByKey(OrderEnum.ORDER_TICKET_TYPE.circusActEndtime.name());
							LOG.info("circusActEndTime --------------" + circusActEndtime
									+ "circusActStartTime-------------:" + circusActStartTime);

							if (circusActEndtime == null || "".equals(circusActEndtime)) {
								if (circusActStartTime != null && "".equals(circusActStartTime) == false) {
									String[] split = circusActStartTime.split(" ");
									if (split.length > 0)
										sb.append("，场次：" + split[1]);
								}
							} else {
								if (circusActStartTime != null && "".equals(circusActStartTime) == false) {
									String[] circusActStartTimeArray = circusActStartTime.split(" ");
									String[] circusActEndtimeArray = circusActEndtime.split(" ");
									if (circusActStartTimeArray.length > 0
											&& circusActEndtimeArray.length > 0) {
										String start = circusActStartTimeArray[1];
										String end = circusActEndtimeArray[1];
										sb.append("，场次：" + start + "-" + end);
									}
								}
							}
							String address = "";
							String visitAddress = "";
							if (addressMap != null && addressMap.isEmpty() == false) {
								address = addressMap.get(item.getOrderItemId().toString());
								visitAddress = addressMap.get(item.getOrderItemId().toString() + "visitAddress");
								if (address != null && !"".equals(address.trim())) {
									address = "，取票地点：" + address;
								} else {
									if (StringUtils.isNotBlank(visitAddress)) {
										address = "，入园地点：" + visitAddress;
									}
								}
							}
							sb.append(address);
							String enterStyle = "";
							if (enterStyleMap != null) {
								enterStyle = enterStyleMap.get(item.getOrderItemId().toString());
								if (enterStyle != null && !"".equals(enterStyle.trim())) {
									enterStyle = "，入园方式：" + enterStyle;
								} else {
									enterStyle = "";
								}
							}
							// 入园方式
							sb.append(enterStyle);

							// 入园限制时间
							String passLimitTime = "";
							if (timeMap != null) {
								passLimitTime = timeMap.get(item.getOrderItemId().toString() + "passLimitTime");
								if (passLimitTime != null && !"".equals(passLimitTime.trim())) {
									passLimitTime = "，" + passLimitTime;
								} else {
									passLimitTime = "";
								}
							}
							sb.append(passLimitTime);

						}
					}
				}
			}
		}
		// 产品信息productInfo
		param.put(OrdSmsTemplate.FIELD.PRODUCT_INFO.getField(), sb.toString());
		// 客服电话customerServicePhone
		return SmsUtil.fillSms(content, order, param);
	}
}
