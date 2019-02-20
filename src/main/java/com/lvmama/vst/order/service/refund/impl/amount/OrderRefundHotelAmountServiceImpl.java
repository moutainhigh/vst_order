package com.lvmama.vst.order.service.refund.impl.amount;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.lvmama.order.apportion.IApiOrderApportionService;
import com.lvmama.vst.api.vo.prod.SuppGoodsBaseTimePriceVo;
import com.lvmama.vst.comm.vo.order.OrderHotelTimeRateInfo;
import com.lvmama.vst.order.web.service.OrderDetailApportionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderAmountChangeClientService;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.refund.IOrderRefundAmountService;

/**
 * 单酒
 * @version 1.0
 */
@Service("orderRefundHotelAmountService")
public class OrderRefundHotelAmountServiceImpl implements IOrderRefundAmountService{
	private static final Log LOG = LogFactory.getLog(OrderRefundHotelAmountServiceImpl.class);
	@Autowired
	private OrderAmountChangeClientService orderAmountChangeClientService;

    @Autowired
    private OrderDetailApportionService orderDetailApportionService;

	@Override
	public Long getOrderTotalChangeMount(Long orderId) {
		HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("orderId",orderId);
        params.put("approveStatus", "APPROVE_PASSED");
        List<OrdAmountChange> list = orderAmountChangeClientService.findOrdAmountChangeList(params);

        //订单价格修改总计
        long totalAmountChange = 0;
        for (int i = 0; i < list.size(); i++) {

            OrdAmountChange ordAmountChange = list.get(i);

            // 订单价格减少
            if (StringUtils.isNotEmpty(ordAmountChange.getFormulas())
                    && "SUBTRACT".equals(ordAmountChange.getFormulas())) {
                if (i < 1) {
                    totalAmountChange = -ordAmountChange.getAmount();
                } else {
                    totalAmountChange -= ordAmountChange.getAmount();
                }

            } else {
                if (i < 1) {
                    totalAmountChange = ordAmountChange.getAmount();
                } else {
                    totalAmountChange += ordAmountChange.getAmount();
                }

            }

        }
        LOG.info("orderId="+ orderId +",totalAmountChange=" +totalAmountChange);
        return totalAmountChange;
	}

    @Override
    public Long getRefundAmount(OrdOrder ordOrder, Date applyDate) {
        //默认值
        long refundAmount = 0L;

        LOG.info("orderId=" +ordOrder.getOrderId()+",categoryId=" +ordOrder.getCategoryId());
        //单酒店
        if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrder.getCategoryId())) {
            //获取主子订单(酒店子单)
            Long hotelTotalDeductAmount = 0L;
            OrdOrderItem ordOrderItem = ordOrder.getMainOrderItem();
            if (ordOrderItem != null) {
                LOG.info("用户申请时间applyDate="+ applyDate + "，酒店最晚无损取消时间lastCancelTime="+ordOrderItem.getLastCancelTime());
                if(applyDate.after(ordOrderItem.getLastCancelTime())){//如果当前时间在最晚取消时间之后
                    //hotelTotalDeductAmount = (ordOrderItem.getTotalAmount() < ordOrderItem.getDeductAmount()) ? ordOrderItem.getTotalAmount():ordOrderItem.getDeductAmount();
                    //获取优惠分摊后的酒店订单实付金额
                    List<OrderHotelTimeRateInfo> rateInfos = orderDetailApportionService.generateHotelTimeRateInfoList(ordOrderItem);
                    if(CollectionUtils.isEmpty(rateInfos) || (CollectionUtils.isNotEmpty(rateInfos) && rateInfos.get(0).getActualPaidAmount() == 0L)){
                        LOG.error("订单orderId:" + ordOrder.getOrderId() + "未进行优惠分摊");
                    }
                    OrderHotelTimeRateInfo orderHotelTimeRateInfo = rateInfos.get(0);
                    //实际子单扣款应为实付和扣款中的小者
                    hotelTotalDeductAmount = (orderHotelTimeRateInfo.getActualPaidAmount() < ordOrderItem.getDeductAmount()) ? orderHotelTimeRateInfo.getActualPaidAmount() : ordOrderItem.getDeductAmount();
                }
            } else {
                LOG.info("订单orderId=" + ordOrder.getOrderId() + "主订单不存在");
            }
            //若有保单
            boolean havePolicyItem = false;
            Long policyTotalDeductAmount = 0L;
            if(CollectionUtils.isNotEmpty(ordOrder.getOrderItemList())){
                for (OrdOrderItem orderItem : ordOrder.getOrderItemList()) {
                    if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
                        havePolicyItem = true;
                        break;
                    }
                }
            }
            if(havePolicyItem){
                LOG.info("订单id为:" + ordOrder.getOrderId() + "有保险子单");
                //1.查保单扣款金额,保单不进行优惠分摊,退款最大金额就是实付金额
                for (OrdOrderItem orderItem : ordOrder.getOrderItemList()) {
                    if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
                        if(SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){    //可退改,计算扣款金额
                            if(applyDate.after(orderItem.getLastCancelTime())){
                                policyTotalDeductAmount += orderItem.getDeductAmount();
                            }
                        }else if(SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){    //不可退改,全部算到扣款金额
                            policyTotalDeductAmount += orderItem.getTotalAmount();
                        }else{
                            policyTotalDeductAmount += orderItem.getTotalAmount();
                        }
                    }
                }
                LOG.info("所有保险子单的扣款金额为:" + policyTotalDeductAmount);
            }

            //计数退款金额
            Long actualAmount = ordOrder.getActualAmount();//实收金额
            Long totalDeductAmount = hotelTotalDeductAmount + policyTotalDeductAmount; //总扣款金额
            refundAmount = actualAmount - totalDeductAmount;//实收金额-扣款金额
            LOG.info("订单orderId：" + ordOrderItem.getOrderId() + "的退款金额为:" + refundAmount
                    + ",实付金额:" + actualAmount + ",扣款金额:" + totalDeductAmount);
        }
        //计算线路退款金额
        return refundAmount;
    }

}
