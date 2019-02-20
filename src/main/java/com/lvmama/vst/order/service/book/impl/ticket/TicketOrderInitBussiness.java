/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.ticket;

import com.lvmama.vst.back.goods.utils.SuppGoodsExpTools;
import com.lvmama.vst.comm.utils.CalendarUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import java.util.Date;

/**
 * 基础门票数据初始化
 * @author lancey
 *
 */
@Component("ticketOrderInitBussiness")
public class TicketOrderInitBussiness extends AbstractBookService implements OrderInitBussiness {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	

	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.book.OrderInitBussiness#initOrderItem(com.lvmama.vst.back.order.po.OrdOrderItem, com.lvmama.vst.order.vo.OrdOrderDTO)
	 */
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		SuppGoods suppGoods = orderItem.getSuppGoods();
		//成人数
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name(), suppGoods.getAdult());
		//儿童数
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.child_quantity.name(), suppGoods.getChild());
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.goodsSpec.name(), suppGoods.getGoodsSpec());
		
		//是否是期票
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name(), suppGoods.getAperiodicFlag());
        if("Y".equals(suppGoods.getAperiodicFlag()) && suppGoods.getSuppGoodsExp() != null){
            orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name(), SuppGoodsExpTools.getAperiodicExpDesc(suppGoods.getSuppGoodsExp(), orderItem.getCreateTime()));
        }

		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.notify_type.name(), suppGoods.getNoticeType());
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_spec.name(), suppGoods.getGoodsSpec());
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name(), suppGoods.getEbkFlag());
		//商品提前退标识
		orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.pre_refund_flag.name(), suppGoods.getPreRefundFlag()); 
		//实体票
		if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equalsIgnoreCase(suppGoods.getGoodsType())){
			orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.express_type.name(), suppGoods.getExpressType());
		}
		
		//EBK是否支持发邮件
		if(null!=suppGoods && null!=suppGoods.getEbkEmailFlag()){
			orderItem.setEbkEmailFlag(suppGoods.getEbkEmailFlag());
		}
		
		SuppGoodsExp suppGoodsExp = suppGoodsClientService.findTicketSuppGoodsExp(suppGoods.getSuppGoodsId());
		//期票处理
		if(orderItem.hasTicketAperiodic()){
            //设置期票有效期
            this.setOrderItemValidTime(orderItem,suppGoodsExp);

			if(StringUtils.isNotEmpty(suppGoodsExp.getUnvalid())){
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.aperiodic_unvalid.name(), suppGoodsExp.getUnvalid());
			}
			//TODO 不可通关日期描述
			if(StringUtils.isNotEmpty(suppGoodsExp.getUnvalidDesc())){
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name(), suppGoodsExp.getUnvalidDesc().replace("不可使用", ""));
			}
			
			//默认为最后一天的日期下单，后面实际使用按正式刷码日期再更改

			setVisitTime(orderItem);

		}else{
			//凭证有效天数
			if(suppGoodsExp != null){
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.name(), suppGoodsExp.getDays());
			}
		}
		
		if(CollectionUtils.isEmpty(order.getBuyInfo().getProductList())){
			order.setAdult(Math.max((int)(suppGoods.getAdult()*orderItem.getQuantity()),order.getAdult()));
			order.setChild(Math.max((int)(suppGoods.getChild()*orderItem.getQuantity()),order.getChild()));
		}
		
		orderItem.setTicketType(suppGoods.getGoodsType());
		//由于门票部分退计算金额对于打包商品会取原单价 所以下单时存入快照 suppGoodsPackageBeforePrice
		try {
			ResultHandleT<Long>  res=suppGoodsTimePriceClientService.getTicketGoodsTimePrice(suppGoods.getSuppGoodsId(), suppGoods.getAperiodicFlag().equalsIgnoreCase("Y")?true:false);
			if(res.isSuccess() && res.getReturnContent()!=null){
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.suppGoodsPackageBeforePrice.name(), res.getReturnContent());
			}else{
				log.error("getTicketGoodsTimePrice suppGoodsId=="+suppGoods.getSuppGoodsId()+"  ,fail=="+res.getMsg());
			}
		} catch (Exception e) {
			log.error("getTicketGoodsTimePrice suppGoodsId=="+suppGoods.getSuppGoodsId()+"  ,error=="+e.toString());
		}
		return true;
	}

    private void setVisitTime(OrdOrderItem orderItem){
        Object endDateObj = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.aperiodic_end.name());
        if(endDateObj != null){
            orderItem.setVisitTime(DateUtil.toSimpleDate(endDateObj.toString()));
        }
    }

    private void setOrderItemValidTime(OrdOrderItem orderItem,SuppGoodsExp suppGoodsExp){
        String startTime = null;
        String endTime = null;
        if("N".equals(suppGoodsExp.getTakeFlag())){
            if(SuppGoodsExp.EXP_TIME_ENUM.PERIOD.getCode().equals(suppGoodsExp.getUseType())){
                startTime = DateUtil.formatSimpleDate(suppGoodsExp.getStartTime());
                endTime = DateUtil.formatSimpleDate(suppGoodsExp.getEndTime());
            }else{
                Date date = orderItem.getVisitTime();
                if(SuppGoodsExp.ACT_ENUM.INNER.getCode().equals(suppGoodsExp.getUseAct())){
                    startTime = DateUtil.formatHHMMDate(date);
                    endTime = calExpDate(date,suppGoodsExp);
                }else{
                    startTime = calExpDate(date,suppGoodsExp);
                    endTime = DateUtil.formatSimpleDate(suppGoodsExp.getUseDeadLineDay())+(suppGoodsExp.getUseDeadLineTime()!=null?" "+suppGoodsExp.getUseDeadLineTime():"");
                }
            }
        }else{
            if(SuppGoodsExp.EXP_TIME_ENUM.PERIOD.getCode().equals(suppGoodsExp.getUseType())){
                startTime = DateUtil.formatSimpleDate(suppGoodsExp.getStartTime());
                endTime = DateUtil.formatSimpleDate(suppGoodsExp.getEndTime());
            }else{
                if(SuppGoodsExp.ACT_ENUM.AFTER.getCode().equals(suppGoodsExp.getUseAct())){
                    endTime = DateUtil.formatSimpleDate(suppGoodsExp.getUseDeadLineDay())+(suppGoodsExp.getUseDeadLineTime()!=null?" "+suppGoodsExp.getUseDeadLineTime():"");
                }
            }
        }
        orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.aperiodic_start.name(), startTime);
        orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.aperiodic_end.name(), endTime);
    }

    private String calExpDate(Date visitTime,SuppGoodsExp suppGoodsExp){
        if("N".equals(suppGoodsExp.getTakeFlag())){
            if(SuppGoodsExp.TIME_TYPE.DAY.getCode().equals(suppGoodsExp.getUseTimeType())){
                return DateUtil.formatSimpleDate(CalendarUtils.addDates(visitTime,suppGoodsExp.getUseDay()-1))+(suppGoodsExp.getUseTime()!=null?" "+suppGoodsExp.getUseTime():"");
            }else{
                if(SuppGoodsExp.TIME_TYPE.HOUR.getCode().equals(suppGoodsExp.getUseTimeType())){
                    return DateUtil.formatHHMMDate(CalendarUtils.addHours(visitTime, suppGoodsExp.getUseDay()));
                }else{
                    return DateUtil.formatHHMMDate(CalendarUtils.addMinutes(visitTime, suppGoodsExp.getUseDay()));
                }
            }
        }else{
            if(SuppGoodsExp.TIME_TYPE.DAY.getCode().equals(suppGoodsExp.getTakeTimeType())){
                return DateUtil.formatSimpleDate(CalendarUtils.addDates(visitTime,suppGoodsExp.getTakeDay()-1))+(suppGoodsExp.getTakeTime()!=null?" "+suppGoodsExp.getTakeTime():"");
            }else{
                if(SuppGoodsExp.TIME_TYPE.HOUR.getCode().equals(suppGoodsExp.getTakeTimeType())){
                    return DateUtil.formatHHMMDate(CalendarUtils.addHours(visitTime, suppGoodsExp.getTakeDay()));
                }else{
                    return DateUtil.formatHHMMDate(CalendarUtils.addMinutes(visitTime, suppGoodsExp.getTakeDay()));
                }
            }
        }
    }
}
