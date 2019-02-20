package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.back.order.po.BatchApportionOutcome;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/6/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderApportionDepotServiceImplTest {
    @Resource
    private OrderApportionDepotService orderApportionDepotService;

    @Test
    public void testBatchUpdateApportionOutcome(){
        BatchApportionOutcome batchApportionOutcome = new BatchApportionOutcome();
        //无用的订单集合
        List<Long> futileOrderIdList = new ArrayList<>();
        for (long i = -1; i > -10; i--) {
            futileOrderIdList.add(i);
        }
        batchApportionOutcome.setFutileOrderApportionIdList(futileOrderIdList);
        //实付分摊已完成的订单
        List<Long> paymentApportionCompletedOrderIdList = new ArrayList<>();
        for (long i = -11; i > -21; i--) {
            paymentApportionCompletedOrderIdList.add(i);
        }
        batchApportionOutcome.setSuccessOrderApportionIdList(paymentApportionCompletedOrderIdList);
        //下个批次中分摊的订单
        List<Long> waitForNextBatchOrderIdList = new ArrayList<>();
        for (long i = -21; i > -31; i--) {
            waitForNextBatchOrderIdList.add(i);
        }
        batchApportionOutcome.setWaitForNextBatchOrderApportionIdList(waitForNextBatchOrderIdList);
        //下单项已分摊的订单
        List<Long> bookingApportionCompletedOrderIdList = new ArrayList<>();
        for (long i = -31; i > -41; i--) {
            bookingApportionCompletedOrderIdList.add(i);
        }
        batchApportionOutcome.setBookingApportionSucceedOrderApportionIdList(bookingApportionCompletedOrderIdList);
        //分摊失败的订单
        List<OrderApportionDepot> failedOrderIdList = new ArrayList<>();
        for (long i = -41; i > -51; i--) {
            OrderApportionDepot orderApportionDepot = new OrderApportionDepot();
            orderApportionDepot.setOrderApportionId(i);
            orderApportionDepot.setOrderId(i);
            failedOrderIdList.add(orderApportionDepot);
        }
        batchApportionOutcome.setFailedOrderDepotList(failedOrderIdList);
        orderApportionDepotService.batchUpdateApportionOutcome(batchApportionOutcome);
    }
}
