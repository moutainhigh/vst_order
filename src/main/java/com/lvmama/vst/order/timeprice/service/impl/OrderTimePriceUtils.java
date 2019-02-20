/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Calendar;
import java.util.List;

import com.lvmama.vst.back.goods.po.SuppGoodsReschedule;
import com.lvmama.vst.back.order.po.OrdItemReschedule;
import com.lvmama.vst.comm.vo.ResultHandleT;
import net.sf.json.JSONArray;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;

/**
 * @author lancey
 *
 */
public abstract class OrderTimePriceUtils {

	private static final Log LOGGER = LogFactory.getLog(OrderTimePriceUtils.class);

	/**
	 * 门票退改策略
	 * @param orderItem
	 * @param suppGoodsClientService
	 */
	public static void setTicketRefund(OrdOrderItem orderItem,SuppGoodsClientService suppGoodsClientService){
		if( BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().longValue() != orderItem.getSuppGoods().getCategoryId().longValue() &&		//景点门票
				BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue() != orderItem.getSuppGoods().getCategoryId().longValue() &&	//组合套餐票
				BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().longValue() != orderItem.getSuppGoods().getCategoryId().longValue()&&   //其他票
				BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().longValue() != orderItem.getSuppGoods().getCategoryId().longValue()){	//演出票
			setOtherRefund(orderItem, suppGoodsClientService);
			return;
		}
		List<SuppGoodsRefund> list = suppGoodsClientService
				.getTicketRefund(orderItem.getSuppGoodsId());
		SuppGoodsRefund refund = null;
		Integer lastestCancelTime =null;//默认最晚取消时间
		boolean isOther=false;
		Calendar cal = Calendar.getInstance();
		//找到最匹配的最晚取消时间
		for (SuppGoodsRefund suppGoodsRefund : list) {
			refund = suppGoodsRefund;
			if (SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(
					suppGoodsRefund.getCancelTimeType())) {
				isOther=true;
				cal.set(Calendar.YEAR, 2099);//默认设置为2099年
				break;
			} else {
				if( suppGoodsRefund.getLatestCancelTime()!=null){
					int tempCancelTime = suppGoodsRefund.getLatestCancelTime()
							.intValue();
					if (lastestCancelTime==null||tempCancelTime < lastestCancelTime) {
						lastestCancelTime = tempCancelTime;
					}
				}
			}
		}
		if(lastestCancelTime!=null){
			if(isOther){
				orderItem.setLastCancelTime(cal.getTime());
			}else{
				orderItem.setLastCancelTime(DateUtils.addMinutes(orderItem.getVisitTime(), -lastestCancelTime));
			}
		}else{
			if(isOther){
				orderItem.setLastCancelTime(cal.getTime());
			}
		}
		if(refund!=null){
			orderItem.setCancelStrategy(refund.getCancelStrategy());
			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(refund.getCancelStrategy())){
				orderItem.setDeductType(refund.getDeductType());
				if(refund.getDeductValue()!=null && refund.getDeductType()!=null) {
					if (SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(refund.getDeductType())) {
						orderItem.setDeductAmount(orderItem.getPrice() * orderItem.getQuantity() * refund.getDeductValue() / 10000);
					} else {
						orderItem.setDeductAmount(orderItem.getQuantity() * refund.getDeductValue());
					}
				}
			}
			//商品退改规则快照-----品类@门票
			if( BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getSuppGoods().getCategoryId())
                    ||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getSuppGoods().getCategoryId())
                    ||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getSuppGoods().getCategoryId())
                    ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(orderItem.getSuppGoods().getCategoryId())
					){
				if(list!=null && list.size()>0){
					try {
						String jsonSt = JSONArray.fromObject(list).toString();
						if (jsonSt.length() >= 4000) {
							LOGGER.warn("SuppGoods [" + orderItem.getSuppGoodsId() + "]'s refundRules_json is out of size 4000/" + jsonSt.length() + " .Has [" + list.size() + "] refund rules.");
							jsonSt = jsonSt.substring(0, 3999);
						}
						orderItem.setRefundRules(jsonSt);
	//					orderItem.setDeductAmount(null);
	//					orderItem.setDeductType(null);
					}catch (Exception ex){
						LOGGER.error(ExceptionFormatUtil.getTrace(ex));
						LOGGER.error("设置商品["+orderItem.getSuppGoodsId()+"]退改规则快照时异常。"+ex.getMessage());
					}
				}
			}
		}
	}
	
	
	
	/**
	 * 其他退改策略
	 * @param orderItem
	 * @param suppGoodsClientService
	 */
	public static void setOtherRefund(OrdOrderItem orderItem,SuppGoodsClientService suppGoodsClientService){
		
		List<SuppGoodsRefund> list = suppGoodsClientService.getTicketRefund(orderItem.getSuppGoodsId());
		if(!list.isEmpty()){
			SuppGoodsRefund suppGoodsRefund = list.get(0);
			orderItem.setCancelStrategy(suppGoodsRefund.getCancelStrategy());
			if(suppGoodsRefund.getLatestCancelTime()!=null){
				orderItem.setLastCancelTime(DateUtils.addMinutes(orderItem.getVisitTime(), -suppGoodsRefund.getLatestCancelTime().intValue()));
			}
			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefund.getCancelStrategy())){
				orderItem.setDeductType(suppGoodsRefund.getDeductType());
				if(suppGoodsRefund.getDeductValue()!=null && suppGoodsRefund.getDeductType()!=null) {
					if (SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefund.getDeductType())) {
						orderItem.setDeductAmount(orderItem.getPrice() * orderItem.getQuantity() * suppGoodsRefund.getDeductValue() / 10000);
					} else {
						orderItem.setDeductAmount(orderItem.getQuantity() * suppGoodsRefund.getDeductValue());
					}
				}
			}
			//商品退改规则快照-----品类@门票
			if( BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue() ||		//景点门票
					BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue() ||	//组合套餐票
					BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue()	||	//其他票
					BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue()	){ //演出票
				if(list!=null && list.size()>0){
					try {
						String jsonSt = JSONArray.fromObject(list).toString();
						if (jsonSt.length() >= 4000) {
							LOGGER.warn("SuppGoods [" + orderItem.getSuppGoodsId() + "]'s refundRules_json is out of size 4000/" + jsonSt.length() + " .Has [" + list.size() + "] refund rules.");
							jsonSt = jsonSt.substring(0, 3999);
						}
						orderItem.setRefundRules(jsonSt);
//						orderItem.setDeductAmount(null);
//						orderItem.setDeductType(null);
					}catch (Exception ex){
						LOGGER.error(ExceptionFormatUtil.getTrace(ex));
						LOGGER.error("设置商品["+orderItem.getSuppGoodsId()+"]退改规则快照时异常。"+ex.getMessage());
					}
				}
			}
		}
	}
    //
    /**
     * @param orderItem
     * @param suppGoodsClientService
     */
    public static void setTicketReschedule(OrdOrderItem orderItem,SuppGoodsClientService suppGoodsClientService){
        if( BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue() ||		//景点门票
                BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue() ||	//组合套餐票
                BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().longValue() == orderItem.getSuppGoods().getCategoryId().longValue()){   //其他票

            try {
                ResultHandleT<SuppGoodsReschedule> suppGoodsReschedule = suppGoodsClientService.findSuppGoodsReschedule(orderItem.getSuppGoodsId());
                if(suppGoodsReschedule!=null && suppGoodsReschedule.isSuccess() && !suppGoodsReschedule.hasNull()){
                    SuppGoodsReschedule returnContent = suppGoodsReschedule.getReturnContent();
                    String jsonStr = JSONObject.fromObject(returnContent).toString();
                    OrdItemReschedule ordItemReschedule = new OrdItemReschedule();
                    ordItemReschedule.setRescheduleRules(jsonStr);
                    ordItemReschedule.setExchangeCount(0L);
                    orderItem.setOrdItemReschedule(ordItemReschedule);
                }
            }catch (Exception e){
                LOGGER.error("setTicketReschedule error: "+e.getMessage(),e);
            }
        }
    }
	
}
