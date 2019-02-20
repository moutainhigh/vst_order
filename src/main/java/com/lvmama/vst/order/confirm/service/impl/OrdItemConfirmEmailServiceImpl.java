package com.lvmama.vst.order.confirm.service.impl;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizOrganization;
import com.lvmama.vst.back.client.biz.service.IBizOrganizationClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_OBJECT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmEmailService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 子订单确认邮件相关接口
 * Created by dongningbo on 2017/3/28.
 */
@Service
public class OrdItemConfirmEmailServiceImpl implements IOrdItemConfirmEmailService {

    private static Logger log = LoggerFactory.getLogger(OrdItemConfirmEmailServiceImpl.class);

    @Autowired
    private PermUserServiceAdapter permUserServiceAdapter;
    @Autowired
    private VstEmailServiceAdapter vstEmailService;
    @Autowired
    private IComplexQueryService complexQueryService;
    @Autowired
    private LvmmLogClientService lvmmLogClientService;
    @Autowired
    private IBizOrganizationClientService bizOrganizationClientRemote;
    @Autowired
    private SuppSupplierClientService suppSupplierClientService;
    @Autowired
	private IOrdPersonService ordPersonService;

    @Override
    public void notifyManager(String confirmStatus, OrdOrderItem ordOrderItem, String operateName, String orderMemo) {
        String fromAddress = "cshoutai@lvmama.com";
        String fromName = "";
        String toAddress = "";
        String toName = "";
        String ccAddress = "";
        String subject = "";
        String contentText = "";
        //邮件发送人相关
        try {
            //子订单所属产品经理
            Long managerId = ordOrderItem.getManagerId();
            PermUser manager = permUserServiceAdapter.getPermUserByUserId(managerId);
            if (manager != null) {
                toAddress = manager.getEmail();
                toName = manager.getRealName();
                log.info("OrdItemConfirmEmailServiceImpl.notifyManager toAddress="+toAddress);
            }
            //获得产品组下人员的邮箱地址
            ccAddress = getCcAddress(toAddress);

            PermUser loginUser = permUserServiceAdapter.getPermUserByUserName(operateName);
            if (loginUser != null) {
                fromAddress += ","+loginUser.getEmail();
                fromName = loginUser.getRealName();
                log.info("OrdItemConfirmEmailServiceImpl.notifyManager fromAddress="+fromAddress);
            }
            //收件人为空
            if (StringUtil.isEmptyString(toAddress)) {
                toAddress = "cshoutai@lvmama.com";
            } else {
                toAddress = "cshoutai@lvmama.com,"+toAddress;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("OrdItemConfirmEmailServiceImpl.notifyManager getPermUserByUserId error. ", e);
        }
        OrdOrder order = null;
        try {
            order = complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                if (order != null && CollectionUtils.isNotEmpty(order.getOrderItemList())) {
                    //入离时间用到
                    for (OrdOrderItem oi : order.getOrderItemList()) {
                        if (oi.getOrderItemId().equals(ordOrderItem.getOrderItemId())) {
                            ordOrderItem.setOrderHotelTimeRateList(oi.getOrderHotelTimeRateList());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("OrdItemConfirmEmailServiceImpl.notifyManager findByOrderId error. ", e);
        }
        boolean hasStock = OrdOrderUtils.hasStockFlag(order);
        //邮件内容
        contentText = getContentText(ordOrderItem, hasStock, confirmStatus, orderMemo);
        //邮件主题
        subject = getSubject(ordOrderItem, hasStock, confirmStatus, fromName, toName);

        //发送邮件对象
        EmailContent emailContent = new EmailContent();
        emailContent.setFromAddress(fromAddress);
        emailContent.setFromName(fromName);
        emailContent.setSubject(subject);
        emailContent.setToAddress(toAddress);
        emailContent.setCcAddress(ccAddress);
        emailContent.setContentText(contentText);
        sendEmail(emailContent);

        insertChildSendEmailLog(order.getOrderId(), ordOrderItem.getOrderItemId(), confirmStatus, operateName, orderMemo);
    }

    /**
     * 组装邮件内容
     * @param ordOrderItem
     * @param hasStock
     * @return
     */
    private String getContentText(OrdOrderItem ordOrderItem, boolean hasStock, String confirmStatus, String orderMemo) {
        StringBuffer content = new StringBuffer();
        try {
            content.append("<br/>订单号："+ordOrderItem.getOrderId());
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                content.append("<br/>酒店名称：" + ordOrderItem.getProductName());
            }
            String supplierName = "";
            ResultHandleT<SuppSupplier> handleT = suppSupplierClientService.findSuppSupplierById(ordOrderItem.getSupplierId());
            if (handleT.isSuccess() && handleT.getReturnContent() != null) {
                SuppSupplier supplier = handleT.getReturnContent();
                supplierName = supplier.getSupplierName();
            }
            content.append("<br/>供应商名称："+supplierName);
            content.append("<br/>商品名称："+ordOrderItem.getSuppGoodsName());
            if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())){
            	Map<String, Object> params = new HashMap<String, Object>();
        		params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
        		params.put("objectId", ordOrderItem.getOrderId());
        		params.put("personType", ORDER_PERSON_TYPE.TRAVELLER.name());
        		List<OrdPerson> travellerPersonList=ordPersonService.findOrdPersonList(params);
	            for(OrdPerson person:travellerPersonList){
	            	content.append("<br/>出游人："+(person.getFullName()==null?"空":person.getFullName()));
	            }
	            content.append("<br/>出游日期："+DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()));
	            content.append("<br/>份数："+ordOrderItem.getQuantity());
            }
            if (!hasStock && !Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {  //非保留房满房
                content.append("<br/>满房时间：" + DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()));
            }
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                OrdOrderHotelTimeRate lastOrderHotelTimeRate = getOrdOrderHotelTimeRate(ordOrderItem);
                content.append("<br/>入离时间：" + DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()) + "--" + DateUtil.formatSimpleDate(lastOrderHotelTimeRate.getVisitTime()));
                content.append("<br/>入住间数："+ ordOrderItem.getQuantity());
            }
            content.append("<br/>结算价："+ PriceUtil.trans2YuanStr(ordOrderItem.getTotalSettlementPrice()));
            content.append("<br/>下单时间："+ DateUtil.formatSimpleDate(ordOrderItem.getCreateTime()));
            content.append("<br/>备注："+ orderMemo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("OrdItemConfirmEmailServiceImpl.notifyManager getContentText error. ", e);
        }
        return content.toString();
    }

    /**
     * 获得离店时间
     * @param ordOrderItem
     * @return
     */
    private OrdOrderHotelTimeRate getOrdOrderHotelTimeRate(OrdOrderItem ordOrderItem) {
        List<OrdOrderHotelTimeRate> orderHotelTimeRateList=ordOrderItem.getOrderHotelTimeRateList();
        OrdOrderHotelTimeRate lastOrderHotelTimeRate=new OrdOrderHotelTimeRate();
        if (CollectionUtils.isNotEmpty(orderHotelTimeRateList)) {
            lastOrderHotelTimeRate=orderHotelTimeRateList.get(orderHotelTimeRateList.size()-1);
        }
        if (lastOrderHotelTimeRate.getVisitTime()!=null) {
            Date visitTime= DateUtils.addDays(lastOrderHotelTimeRate.getVisitTime(), 1);
            lastOrderHotelTimeRate.setVisitTime(visitTime);
        }
        return lastOrderHotelTimeRate;
    }

    /**
     * 邮件标题
     * @param ordOrderItem
     * @param hasStock
     * @param confirmStatus
     * @param fromName
     * @param toName
     * @return
     */
    private String getSubject(OrdOrderItem ordOrderItem, boolean hasStock, String confirmStatus, String fromName, String toName) {
        String subject = "", confirmStatusStr = "";
        String stockFlag = hasStock ? "保留房" : "非保留房";

        if (Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {
            confirmStatusStr = "变价";
        } else {
            confirmStatusStr = "满房";
        }
        //邮件标题  例如：XX资审发送给XX产品经理--订单XXX保留房满房拒单，请处理

        if (!hasStock && Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {
            subject = fromName+"发送给"+toName+"--商品"+ordOrderItem.getSuppGoodsName()+ "供应商变价，请处理";
        } else {
            subject = fromName+"发送给"+toName+"--订单"+ordOrderItem.getOrderId()+ stockFlag + confirmStatusStr +"拒单，请处理";
        }
        return subject;
    }

    /**
     * 发送邮件
     * @param emailContent
     */
    private void sendEmail(EmailContent emailContent) {
        try {
            log.info("工作台（房态/价格）变更提醒邮件");
            log.info("FromAddress:"+emailContent.getFromAddress());
            log.info("FromName:"+emailContent.getFromName());
            log.info("Subject:"+emailContent.getSubject());
            log.info("ToAddress:"+emailContent.getToAddress());
            log.info("CcAddress:"+emailContent.getCcAddress());
            log.info("ContentText:" + emailContent.getContentText());
            vstEmailService.sendEmail(emailContent);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(ExceptionFormatUtil.getTrace(e));
        }
    }

    /**
     * 插入日志
     * @param orderId
     * @param orderItemId
     * @param confirmStatus
     * @param assignor
     * @param memo
     */
    private void insertChildSendEmailLog(final Long orderId,final Long orderItemId,String confirmStatus,String assignor,String memo){
        try {
            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    orderId,
                    orderItemId,
                    assignor,
                    "编号为["+orderItemId+"]的子订单发送"+ Confirm_Enum.CONFIRM_STATUS.getCnName(confirmStatus)+"邮件",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    "发送["+ Confirm_Enum.CONFIRM_STATUS.getCnName(confirmStatus)+"]邮件",
                    memo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(ExceptionFormatUtil.getTrace(e));
        }
    }

    /**
     * 获得产品组下人员的邮箱地址
     * @param toAddress
     * @return
     */
    private String getCcAddress(String toAddress) {
        if (StringUtil.isEmptyString(toAddress)) {
            log.info("OrdItemConfirmEmailServiceImpl.notifyManager getCcAddress toAddress= "+toAddress);
            return "";
        }
        StringBuffer ccAddress = new StringBuffer("");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", toAddress);
        ResultHandleT<List<BizOrganization>> handleT = bizOrganizationClientRemote.findBizOrganizationListByParams(params);
        params.clear();
        if (handleT.isSuccess()) {
            List<BizOrganization> organizationList = handleT.getReturnContent();
            if (CollectionUtils.isNotEmpty(organizationList) && organizationList.size() == 1) {
                String parent = organizationList.get(0).getParent();
                if (StringUtil.isNotEmptyString(parent)) {
                    params.put("parent", parent);
                }
            }
        }
        //  没有查询到上级
        if (params.size() > 0) {
            ResultHandleT<List<BizOrganization>> handleT1 = bizOrganizationClientRemote.findBizOrganizationListByParams(params);
            if (handleT1.isSuccess()) {
                List<BizOrganization> organizationList = handleT1.getReturnContent();
                if (CollectionUtils.isNotEmpty(organizationList)) {
                    for (BizOrganization org : organizationList) {
                        if (toAddress.equalsIgnoreCase(org.getEmail())) {
                            continue;   //跳过当前收件人
                        }
                        ccAddress.append(","+org.getEmail());
                    }
                }
            }
        }

        return ccAddress.length()==0?"":ccAddress.substring(1);
    }

    /**
     * 若订单来源<>分销商，或（订单来源=分销商且 CHANNEL_ID = 106 or 10000）， 则发件方为cshoutai@lvmama.com，收件方为产品经理+cshoutai@lvmama.com，

		反之，发件方为：mddfxzs@lvmama.com，收件方为产品经理+mddfxzs@lvmama.com

     */
	@Override
	public void notifyManagerByEmailAddress(OrdOrder order, String confirmStatus, OrdOrderItem ordOrderItem,
			String operateName, String orderMemo) {
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
			Long distributionChannel = order.getDistributionChannel();
			if(distributionChannel == null){
				distributionChannel = 0L;
			}
			//若订单来源分销商101、102、103、104、109、113，发件方为：mddfxzs@lvmama.com，收件方为产品经理+mddfxzs@lvmama.com
			if(isTNTDistributionChannel(order)){
				//todo...101、102、103、104、109、113
				notifyManager("mddfxzs@lvmama.com","mddfxzs@lvmama.com",confirmStatus,ordOrderItem,operateName,orderMemo);
			}else{
				//todo..
				// 则发件方为cshoutai@lvmama.com，收件方为产品经理+cshoutai@lvmama.com
				notifyManager("cshoutai@lvmama.com","cshoutai@lvmama.com",confirmStatus,ordOrderItem,operateName,orderMemo);
			}
		}else{
			notifyManager(confirmStatus,ordOrderItem,operateName,orderMemo);
		}
	}
	
	//分销，淘宝
    private static final String DISTRIBUTOR_CODE_TAOBAO="DISTRIBUTOR_TAOBAO";
	/**
     * 判断是否分销，true为是，false为不是
     * @return
     */
    public static boolean isTNTDistributionChannel(final OrdOrder order){
    	Long distributionChannel = order.getDistributionChannel();
		if(distributionChannel == null){
			distributionChannel = 0L;
		}
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		//DIST_FRONT_END，DIST_BACK_END，DIST_O2O_SELL，DIST_O2O_APP_SELL----pc
		if(Constant.DIST_FRONT_END == order.getDistributorId() 
			    ||Constant.DIST_BACK_END == order.getDistributorId()
			    ||Constant.DIST_O2O_SELL == order.getDistributorId()
			    ||Constant.DIST_O2O_APP_SELL == order.getDistributorId()){
			return false;
		}else if(ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, distributionChannel.longValue())){
			//10000L,107L,108L,110L,10001L,10002L---app
			return false;
		}else if(null!=order.getDistributorCode()&&DISTRIBUTOR_CODE_TAOBAO.equals(order.getDistributorCode())){//分销，淘宝
			return false;
		}else{
			//其他分销
			return true;
		}
    }
	
	private  void notifyManager(String fromAddress,String additionalAddress,String confirmStatus, OrdOrderItem ordOrderItem, String operateName, String orderMemo) {
        String fromName = "";
        String toAddress = "";
        String toName = "";
        String ccAddress = "";
        String subject = "";
        String contentText = "";
        //邮件发送人相关
        try {
            //子订单所属产品经理
            Long managerId = ordOrderItem.getManagerId();
            PermUser manager = permUserServiceAdapter.getPermUserByUserId(managerId);
            if (manager != null) {
                toAddress = manager.getEmail();
                toName = manager.getRealName();
                log.info("OrdItemConfirmEmailServiceImpl.notifyManager toAddress="+toAddress);
            }
            //获得产品组下人员的邮箱地址
            ccAddress = getCcAddress(toAddress);

            PermUser loginUser = permUserServiceAdapter.getPermUserByUserName(operateName);
            if (loginUser != null) {
                fromAddress += ","+loginUser.getEmail();
                fromName = loginUser.getRealName();
                log.info("OrdItemConfirmEmailServiceImpl.notifyManager fromAddress="+fromAddress);
            }
            //收件人为空
            if (StringUtil.isEmptyString(toAddress)) {
                toAddress = additionalAddress;
            } else {
                toAddress = additionalAddress+","+toAddress;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("OrdItemConfirmEmailServiceImpl.notifyManager getPermUserByUserId error. ", e);
        }
        OrdOrder order = null;
        try {
            order = complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                if (order != null && CollectionUtils.isNotEmpty(order.getOrderItemList())) {
                    //入离时间用到
                    for (OrdOrderItem oi : order.getOrderItemList()) {
                        if (oi.getOrderItemId().equals(ordOrderItem.getOrderItemId())) {
                            ordOrderItem.setOrderHotelTimeRateList(oi.getOrderHotelTimeRateList());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("OrdItemConfirmEmailServiceImpl.notifyManager findByOrderId error. ", e);
        }
        boolean hasStock = OrdOrderUtils.hasStockFlag(order);
        //邮件内容
        contentText = getContentText(ordOrderItem, hasStock, confirmStatus, orderMemo);
        //邮件主题
        subject = getSubject(ordOrderItem, hasStock, confirmStatus, fromName, toName);

        //发送邮件对象
        EmailContent emailContent = new EmailContent();
        emailContent.setFromAddress(fromAddress);
        emailContent.setFromName(fromName);
        emailContent.setSubject(subject);
        emailContent.setToAddress(toAddress);
        emailContent.setCcAddress(ccAddress);
        emailContent.setContentText(contentText);
        sendEmail(emailContent);

        insertChildSendEmailLog(order.getOrderId(), ordOrderItem.getOrderItemId(), confirmStatus, operateName, orderMemo);
    }
}
