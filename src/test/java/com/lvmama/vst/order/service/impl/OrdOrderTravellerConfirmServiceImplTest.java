package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.vst.back.client.ord.service.OrderTravellerConfirmClientService;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2016/4/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrdOrderTravellerConfirmServiceImplTest {
    @Resource(name = "ordOrderTravellerConfirmService")
    private OrdOrderTravellerConfirmService orderTravellerConfirmService;
    @Resource(name = "orderTravellerConfirmClientServiceRemote")
    private OrderTravellerConfirmClientService orderTravellerConfirmClientService;
//    @Test
    public void test() {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        OrderTravellerOperateDO orderTravellerConfirmDO = createOrderTravellerConfirmDO();
        System.out.println("创建vo:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        orderTravellerConfirmService.save(orderTravellerConfirmDO);
        System.out.println("save后vo:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        orderTravellerConfirmDO.getOrderTravellerConfirm().setContainForeign("N");
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("修改vo[1]:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        orderTravellerConfirmService.update(orderTravellerConfirmDO);
        System.out.println("update后vo:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        orderTravellerConfirmDO.getOrderTravellerConfirm().setContainOldMan("N");
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("修改vo[2]:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        orderTravellerConfirmService.saveOrUpdate(orderTravellerConfirmDO);
        System.out.println("saveOrUpdate后vo:" + JSONUtil.bean2Json(orderTravellerConfirmDO));
        System.out.println("============================================================================");
        System.out.println("============================================================================");
    }

    @Test
    public void testSaveOrUpdate(){
        orderTravellerConfirmClientService.saveOrUpdateOrderTravellerConfirmInfo(createOrderTravellerConfirmDO());
    }

    private OrderTravellerOperateDO createOrderTravellerConfirmDO(){
        OrderTravellerOperateDO orderTravellerConfirmDO = new OrderTravellerOperateDO();
//        orderTravellerConfirm.setOrderId(Long.valueOf(new Random().nextInt()));
        OrdOrderTravellerConfirm orderTravellerConfirm = new OrdOrderTravellerConfirm();
        orderTravellerConfirm.setOrderId(200611191L);
        orderTravellerConfirm.setContainPregnantWomen("Y");
        orderTravellerConfirm.setContainOldMan("Y");
        orderTravellerConfirm.setContainForeign("Y");
        orderTravellerConfirmDO.setOrderTravellerConfirm(orderTravellerConfirm);
        orderTravellerConfirmDO.setChannelType(String.valueOf(VSTEnum.DISTRIBUTION.LVMAMABACK.getNum()));
        orderTravellerConfirmDO.setUserCode("TestUser");
        return orderTravellerConfirmDO;
    }
}
