package com.lvmama.vst.order.processer;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.job.CheckOrdOrderItemNoCodeJob;
import com.lvmama.vst.order.job.ClearOrderMessageInfoJob;
import com.lvmama.vst.order.job.SendFailedMessageInfoJob;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sangbowei on 2017/8/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderSettlementProcesserTest {

    @Autowired
    private OrderSettlementProcesser orderSettlementProcesser;

    @Autowired
    private ClearOrderMessageInfoJob clearOrderMessageInfoJob;

    @Autowired
    private CheckOrdOrderItemNoCodeJob checkOrdOrderItemNoCodeJob;

    @Autowired
    private SendFailedMessageInfoJob sendFailedMessageInfoJob;

    /**
     * 测试 消息处理器
     */
    @Test
    public void testOrderSettlementProcesser() {
        batchTestProcesser();
    }

    /**
     * 测试 检查子订单CODE为空个数
     */
    @Test
    public void testCheckOrdOrderItemNoCodeJob(){
        checkOrdOrderItemNoCodeJob.run();
    }

    /**
     * 测试  补偿推送 失败的订单信息
     */
    @Test
    public void testSendFailedMessageInfoJob(){
        sendFailedMessageInfoJob.run();
    }

    /**
     * 测试 清理订单消息表数据JOB
     */
    @Test
    public void testClearOrderMessageInfoJob(){
        clearOrderMessageInfoJob.run();
    }

    private void batchTestProcesser(){
        List<Long> orderIdList = getOrderIdList();
        if(CollectionUtils.isEmpty(orderIdList)){
            return;
        }

        for(Long orderId:orderIdList){
            Message message = MessageFactory.newOrderPaymentMessage(orderId, "PAYED");
            orderSettlementProcesser.process(message);
        }

    }

    private List<Long> getOrderIdList(){
        Long[] orderIdArr = {
                62964079L,
                62964082L,
                41939286L,
                62964211L,
                62964221L,
                62964232L,
                62963891L,
                62964096L,
                62963957L,
                62964086L,
                62964087L,
                62964091L,
                62964105L,
                62964267L,
                62964271L,
                62964272L,
                62964320L,
                62964096L,
                62964357L,
                62964096L,
                62964088L,
                62964092L,
                62964096L,
                62964097L,
                62964098L,
                62964108L,
                62964110L,
                62964112L,
                62964113L,
                62964115L,
                62964117L,
                62964121L,
                62964122L,
                62964124L,
                62964126L,
                62964129L,
                62964137L,
                62964143L,
                62964144L,
                62964145L,
                62964146L,
                62964147L,
                62964148L,
                62964150L,
                62964154L,
                62964157L,
                62964158L,
                62964159L,
                62964160L,
                62964161L,
                62964162L,
                62964163L,
                62964165L,
                62964173L,
                62964174L,
                62964177L,
                62964182L,
                62964184L,
                62964187L,
                62964188L,
                62964192L,
                62964194L,
                62964195L,
                62964198L,
                62964200L,
                62964202L,
                62964204L,
                62964206L,
                62964306L,
                62964308L,
                62964310L,
                62964312L,
                62964271L,
                62964299L,
                62964317L,
                62964072L,
                62964073L,
                62964095L,
                62964130L,
                62964135L,
                62964138L,
                62964139L,
                62964140L,
                62964141L,
                62964142L,
                62964153L,
                62964189L,
                62964190L,
                62964193L,
                62964151L,
                62964235L,
                62964327L
        };
        List<Long> orderIdList = Arrays.asList(orderIdArr);
        return orderIdList;
    }
}
