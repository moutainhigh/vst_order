package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderHouseControlService;
import com.lvmama.vst.back.client.ord.service.OrderMintiorService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.HotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdOrderService;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by lijuntao on 2016/11/2.
 */
@Component("orderHouseControlServiceRemote")
public class OrderHouseControlServiceImpl implements OrderHouseControlService {

    private static final Log LOG = LogFactory.getLog(OrderHouseControlServiceImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private IOrdOrderService ordOrderService;

    @Autowired
    private OrderMintiorService orderMintorService;

    @Override
    public void closeHouseNotice(SuppGoodsTimePrice suppGoodsTimePrice) {

        //满房的时间集合
        LOG.info("OrderHouseControlServiceImpl.closeHouseNotice start,hotel SuppGoodsIdList"+suppGoodsTimePrice.getSuppGoodsIdList()+",hotelcomb SuppGoodsIdList"+suppGoodsTimePrice.getHotelcombGoodsIdList());
        List<Date> dateList = CalendarUtils.getDates(suppGoodsTimePrice.getStartDate(), suppGoodsTimePrice.getEndDate(), suppGoodsTimePrice.getWeekDay());
        if((SuppGoodsTimePrice.STOCKSTATUS.FULL.name()).equals(suppGoodsTimePrice.getStockStatus())){
            try{
            	List<Long> suppGoodsIdList = suppGoodsTimePrice.getSuppGoodsIdList();
            	List<Long> hotelcombGoodsIdList = suppGoodsTimePrice.getHotelcombGoodsIdList();
            	//单酒店关房
            	if(suppGoodsIdList != null && suppGoodsIdList.size() >0){
            		LOG.info("OrderHouseControlServiceImpl.closeHouseNotice single hotel,goodsId:"+suppGoodsIdList.get(0));
            		suppGoodsIdList = this.removeEmptyGoodsId(suppGoodsIdList);
            		this.closeHouse(suppGoodsIdList,dateList,BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(),suppGoodsTimePrice);
            	}
            	//酒套餐关房
            	if(hotelcombGoodsIdList != null && hotelcombGoodsIdList.size() > 0){
            		LOG.info("OrderHouseControlServiceImpl.closeHouseNotice hotelcomb,goodsId:"+hotelcombGoodsIdList.get(0));
            		suppGoodsIdList = this.removeEmptyGoodsId(suppGoodsIdList);
            		this.closeHouse(hotelcombGoodsIdList,dateList,BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId(),suppGoodsTimePrice);
            	}

            }catch(BusinessException e){
                LOG.error("OrderHouseControlServiceImpl.closeHouseNotice error,SuppGoodsIdList:"+suppGoodsTimePrice.getSuppGoodsIdList()+",error message:"+e.getMessage());
            }
        }
    }

    
    public void closeHouse(List<Long> goodsIdList,List<Date> dateList,Long categoryId,SuppGoodsTimePrice suppGoodsTimePrice){
    	for(Long goodsId : goodsIdList){
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("suppGoodsId", goodsId);
            params.put("categoryId", categoryId);
            params.put("supplierId", suppGoodsTimePrice.getSupplierId());
            params.put("certConfirmStatus", OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
            params.put("orderStatus", OrderEnum.ORDER_STATUS.NORMAL.name());
            Long startTime  = System.currentTimeMillis();
            LOG.info("ordOrderService.findHotelOrderListByParams method start,goodsId:"+goodsId+",CategoryId:"+categoryId);
            List<OrdOrder> orderList = ordOrderService.findHotelOrderListByParams(params);
            LOG.info("ordOrderService.findHotelOrderListByParams end:"+orderList.size()+",goodsId:"+goodsId+",CategoryId:"+categoryId);
            Long costTime =System.currentTimeMillis()-startTime;
            LOG.info("costTime:"+costTime);
            if(orderList!=null&&orderList.size()>0){
                for(OrdOrder ordOrder:orderList){
                    OrdOrderItem orderItem=ordOrder.getMainOrderItem();
                    //获取客户入住的期间
                    if(categoryId == 1){//单酒店
                    	 List<OrdOrderHotelTimeRate> orderHotelTimeRateList=orderItem.getOrderHotelTimeRateList();
                         LOG.info("orderHotelTimeRateList orderHotelTimeRateList.size()="+orderHotelTimeRateList.size()+",goodsId:"+goodsId+",CategoryId:"+categoryId);
                         List<HotelTimeRateInfo> hotelTimeRateInfoList=handleHouseTimeRateInfo(orderHotelTimeRateList,orderItem);
                         //判断订房时间和满房时间的关系
                         if(isFullHouse(dateList,hotelTimeRateInfoList)){
                           this.remindOrCancelOrder(ordOrder, goodsId, categoryId);
                         }
                    }else if(categoryId == 32){//酒套餐
                    	LOG.info("remindOrCancelOrder start,dateList:"+dateList+",orderItem visttime:"+orderItem.getVisitTime()+",goodsId:"+goodsId+",ordOrderId:"+ordOrder.getOrderId());
                    	if(isFullHouseHotelComb(dateList,orderItem)){
                    		this.remindOrCancelOrder(ordOrder, goodsId, categoryId);
                    	}
                    }
                }
            }
    	}
    }
    
    public void remindOrCancelOrder(OrdOrder ordOrder,Long goodsId,Long categoryId){
    	Long ordOrderId = ordOrder.getOrderId();
    	LOG.info("compareTime isFullHouse success,goodsId:"+goodsId+",CategoryId:"+categoryId);
        //判断支付状态
        if(ordOrder.hasPayed()||ordOrder.hasPartPayed()){
            //推送通知
            LOG.info("sendMessage reminderOrderOfCloseHouse ordOrderId"+ordOrderId);
            //	int  falg =	orderMintorService.reminderOrderOfCloseHouse(ordOrderId, this.getLoginUser().getUserName());
            int  falg =	orderMintorService.reminderOrderOfCloseHouse(ordOrderId, "System");

            LOG.info("StockStatus full message:"+falg+"ordOrderId:"+ordOrderId+",goodsId:"+goodsId+",CategoryId:"+categoryId);
        }else if(OrderEnum.PAYMENT_STATUS.UNPAY.name().equals(ordOrder.getPaymentStatus())){
            //取消订单，并发送短信
            try{
                //取消订单
                LOG.info("cancelOrder-method-stard-ordOrderId:"+ordOrderId+",goodsId:"+goodsId+",CategoryId:"+categoryId);
                orderService.cancelOrder(ordOrderId, OrderEnum.CANCEL_CODE_TYPE.CLOSE_HOUSE.name(), "酒店已经关房需取消订单", "System", "酒店已关房");
                LOG.info("cancelOrder--end-param:"+ordOrderId+",goodsId:"+goodsId+",CategoryId:"+categoryId);
                //发送短信
                orderMintorService.handle(MessageFactory.newOrderCancleOfCloseHouse(ordOrderId, Constant.EVENT_TYPE.ORDER_CANCEL_CLOSEHOUSE_MSG.name()), ordOrder);
                LOG.info("send message success orderId："+ordOrder.getOrderId()+",goodsId:"+goodsId+",CategoryId:"+categoryId);
            }catch(BusinessException e){
                LOG.error("cancel order exception ordOrderId:" +
                        ""+ordOrderId+",error message:"+e.getMessage()+",goodsId:"+goodsId+",CategoryId:"+categoryId);
            }
        }
    }
    
    private ComplexQuerySQLCondition buildQueryConditionForMonitor(OrderMonitorCnd monitorCnd) {
        ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
        //组装订单内容类条件
        condition.getOrderContentParam().setSuppGoodstId(monitorCnd.getSuppGoodsId());

        //组装订单标志类条件
        condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
        condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
        condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
        condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
        condition.getOrderFlagParam().setOrderPackTableFlag(true);
        condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
        condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);

        //组装订单ID类条件
        condition.getOrderIndentityParam().setSupplierId(monitorCnd.getSupplierId());
        condition.getOrderIndentityParam().setCategoryIds(String.valueOf(monitorCnd.getCategoryId()));
        //组装订单排序类条件
        condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);

        //组装订单状态类条件
        condition.getOrderStatusParam().setOrderStatus(monitorCnd.getOrderStatus());

        condition.getOrderStatusParam().setCertConfirmStatus(monitorCnd.getCertConfirmStatus());
        return condition;
    }

    private List<HotelTimeRateInfo>  handleHouseTimeRateInfo(
            List<OrdOrderHotelTimeRate> orderHotelTimeRateList,OrdOrderItem orderItem) {
        List<ArrayList> settleList=new ArrayList<ArrayList>();
        ArrayList<OrdOrderHotelTimeRate> hotelTimeTateList=null;
        if (orderHotelTimeRateList!=null) {
            for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {

                if (settleList.size()==0) {
                    hotelTimeTateList=new ArrayList();
                    hotelTimeTateList.add(orderHotelTimeRate);
                    settleList.add(hotelTimeTateList);
                }else{
                    OrdOrderHotelTimeRate preOrderHotelTimeRate=hotelTimeTateList.get(hotelTimeTateList.size()-1);
                    Long prePrice=preOrderHotelTimeRate.getPrice();
                    Long price=orderHotelTimeRate.getPrice();

                    Long preBreakfastTicket= preOrderHotelTimeRate.getBreakfastTicket();
                    Long breakfastTicket= orderHotelTimeRate.getBreakfastTicket();
                    if (!prePrice.equals(price) || !preBreakfastTicket.equals(breakfastTicket)) {
                        hotelTimeTateList=new ArrayList();
                        hotelTimeTateList.add(orderHotelTimeRate);
                        settleList.add(hotelTimeTateList);

                    }else{
                        hotelTimeTateList.add(orderHotelTimeRate);
                    }
                }
            }
        }

        List<HotelTimeRateInfo> resultList=new ArrayList<HotelTimeRateInfo>();
        for (ArrayList settleObjList : settleList) {

            OrdOrderHotelTimeRate orderHotelTimeRateFirst=(OrdOrderHotelTimeRate)settleObjList.get(0);
            OrdOrderHotelTimeRate orderHotelTimeRateLast=(OrdOrderHotelTimeRate)settleObjList.get(settleObjList.size()-1);

            HotelTimeRateInfo hotelTimeRateInfo=new HotelTimeRateInfo();
            hotelTimeRateInfo.setHouseType("");//房型暂时显示为商品名称
            hotelTimeRateInfo.setStartDate(orderHotelTimeRateFirst.getVisitTime());
            hotelTimeRateInfo.setEndDate(DateUtils.addDays(orderHotelTimeRateLast.getVisitTime(), 1));
            hotelTimeRateInfo.setHousePrice(orderHotelTimeRateFirst.getPrice());
            hotelTimeRateInfo.setSettlementPrice(orderHotelTimeRateFirst.getSettlementPrice());
            hotelTimeRateInfo.setBreakfastTicket(orderHotelTimeRateFirst.getBreakfastTicket());

            resultList.add(hotelTimeRateInfo);

        }

        return resultList;
    }
    /**
     * 去除空的商品id
     * @param suppGoodsIdList
     */
    public List<Long>  removeEmptyGoodsId(List<Long> suppGoodsIdList){
    	List<Long> suppGoodsIdListNew = new ArrayList<Long>();
    	for(Long goodsId : suppGoodsIdList){
    		if(goodsId != null){
    			suppGoodsIdListNew.add(goodsId);
    		}
    	}
    	return suppGoodsIdListNew;
    }
    
    private boolean isFullHouse(List<Date> list,	List<HotelTimeRateInfo> hotelTimeRateInfoList){
        boolean flag= false ;
        for(Date date:list){
            for(HotelTimeRateInfo hotelTimeRateInfo:hotelTimeRateInfoList){
                Date endDate = hotelTimeRateInfo.getEndDate();
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(endDate);
                cal1.set(Calendar.DATE, cal1.get(Calendar.DATE) - 1);
                Date lastDate = cal1.getTime();
                if(DateUtils.isSameDay(date, hotelTimeRateInfo.getStartDate())||DateUtils.isSameDay(date,lastDate)){
                    flag = true;
                    break;
                }
            }
            if(flag)
                break ;
        }
        return flag;
    }
    
    /**
     * 判断酒套餐关房是否需要取消订单
     * @param list
     * @param orderItem
     * @return
     */
    private boolean isFullHouseHotelComb(List<Date> list,OrdOrderItem orderItem){
    	boolean flag= false ;
    	for(Date date:list){
    		if(DateUtils.isSameDay(date,orderItem.getVisitTime())){
    			flag = true;
    			break;
    		}
    	}
    	return flag;
    }
}
