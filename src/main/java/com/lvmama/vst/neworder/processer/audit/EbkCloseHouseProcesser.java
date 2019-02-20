package com.lvmama.vst.neworder.processer.audit;

import java.util.Map;

import com.lvmama.vst.comm.vo.ResultHandle;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
* @Description: ebk酒店关房 lvmm_order_process消息=>lvmm_order_schedule 消息=>vst order
* queue ActiveMQ.VST_ORDER_CLOSEHOUSE_EBK.order
* @author guobiao
* @date 2017年12月29日
*/
public class EbkCloseHouseProcesser implements MessageProcesser {
    private static final Logger LOG = LoggerFactory.getLogger(EbkCloseHouseProcesser.class);
    @Autowired
    IOrdItemConfirmStatusService ordItemConfirmStatusService;
    @Autowired
    private IOrderUpdateService orderUpdateService;

	@Override
	public void process(Message message) {
	  	LOG.info("ebkcloseHouse ConfirmStatusProcesser start attr:"+message.getAttributes());
		if(MessageUtils.isCloseHouseEbkMsg(message)){
			Long itemId = message.getObjectId();
			try{
			    Map<String,Object> attributes=message.getAttributes();
				OrdOrderItem orderItem=orderUpdateService.getOrderItem(itemId);
			 	LOG.info("ebkcloseHouse ConfirmStatusProcesser orderItem:"+itemId);
				Long categoryId=Long.valueOf(attributes.get("categoryId").toString());
				String memo=(String)attributes.get("memo");
				String confirmUser=(String)attributes.get("confirmUser");
				String confirmStatusCnName=(String)attributes.get("confirmStatusCnName");
				boolean hasCertificateStatusAccept=Boolean.parseBoolean(attributes.get("hasCertificateStatusAccept").toString());
                ResultHandle resultHandle = null;
            	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
                        .getCategoryId().equals(categoryId)
                        &&!hasCertificateStatusAccept
                        &&(memo==null || memo.toString().length() ==0)){
                    resultHandle = ordItemConfirmStatusService.closeFullhotelAndForbidSale(orderItem, confirmUser, "满房自动关房(供应商EBK操作)", confirmStatusCnName, null, null, null);
                }else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()
                        .equals(categoryId)
                       && EbkCertif.REASON_DESC.ROOM_FULL.getCode().equals((String)attributes.get("reason"))
                       &&!orderItem.hasSupplierApi()
            			&&(memo==null || memo.toString().length() ==0)){
                    resultHandle = ordItemConfirmStatusService.closeFullhotelAndForbidSale(orderItem, confirmUser,"满房自动关房(供应商EBK操作)",confirmStatusCnName ,null,null,null);
            	}
            	LOG.info("closeFullhotelAndForbidSale.result="+resultHandle.getMsg());
            }catch(Exception e){
            	LOG.error("ebkcloseHouse====orderItemId:"+itemId+",close hotel fail===",e);
            }
		}
	}

	/**
	 *
	 * @param attributes
	 * (objectId,objectType,auditType,auditSubType)
	 * @return
	 */
	private boolean isValid(Map<String, Object> attributes) {
		if(attributes == null){
			return false;
		}
		Long objectId = (Long) attributes.get("objectId");
		String objectType = (String) attributes.get("objectType");
		String auditType = (String) attributes.get("auditType");
		if(objectId == null || StringUtils.isEmpty(objectType) || StringUtils.isEmpty(auditType)){
			return false;
		}
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.trim())
				|| OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType.trim())){
			return true;
		}else{
			return false;
		}

	}



}
