package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.client.ord.service.OrderRelatedPersonClientService;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2016/10/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderRelatedPersonClientServiceImplTest {
    private static final Log log = LogFactory.getLog(OrderRelatedPersonClientServiceImplTest.class);
    @Resource
    private OrderRelatedPersonClientService orderRelatedPersonClientService;

    @Test
    public void testLoadOrderRelatedPersons(){
        OrdPersonQueryTO orderPersonQueryTO = new OrdPersonQueryTO();
        orderPersonQueryTO.setOrderId(200619119L);
        ResultHandleT<OrderRelatedPersonsVO> resultHandleT = orderRelatedPersonClientService.loadOrderRelatedPersons(orderPersonQueryTO);
        log.info("OrderRelatedPersonClientServiceImplTest test result is ============" + GsonUtils.toJson(resultHandleT.getReturnContent()));
    }
}
