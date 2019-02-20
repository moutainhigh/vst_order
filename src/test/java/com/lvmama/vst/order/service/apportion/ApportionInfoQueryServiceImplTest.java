package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.po.OrderApportionInfoPO;
import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/5/4.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class ApportionInfoQueryServiceImplTest {
    private static final Log log = LogFactory.getLog(ApportionInfoQueryServiceImplTest.class);
    @Resource
    private ApportionInfoQueryService apportionInfoQueryService;

    @Test
    public void testCalculateOrderApportionInfo(){
        Long orderId = 42935296L;
        OrderApportionInfoQueryVO orderApportionInfoQueryVO = new OrderApportionInfoQueryVO();
        orderApportionInfoQueryVO.setOrderId(orderId);
        OrderApportionInfoPO orderApportionInfoPO = apportionInfoQueryService.calculateOrderApportionInfo(orderApportionInfoQueryVO);
        log.info("Order [" + orderId + "] apportion info is " + GsonUtils.toJson(orderApportionInfoPO));
    }
}
