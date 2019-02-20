package com.lvmama.vst.order.service.route;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class IVstOrderRouteServiceTest extends OrderTestBase {
    @Resource
    private IVstOrderRouteService vstOrderRouteService;

    //订单id
    private Long orderId = 62968809L;
    //子单id
    private Long orderItemId = 5144755L;

    @Test
    public void testIsJobRouteToNewSys(){
        boolean jobRouteToNewSys = vstOrderRouteService.isJobRouteToNewSys();
        Assert.assertTrue(jobRouteToNewSys);
    }

    @Test
    public void testIsOrderRouteToNewSys(){
        boolean orderRouteToNewSys = vstOrderRouteService.isOrderRouteToNewSys(orderId);
        Assert.assertTrue(orderRouteToNewSys);
    }

    @Test
    public void testIsOrderItemRouteToNewSys(){
        boolean orderItemRouteToNewSys = vstOrderRouteService.isOrderItemRouteToNewSys(orderItemId);
        Assert.assertTrue(orderItemRouteToNewSys);
    }

    @Test
    public void testIsRequestRouteToNewSys(){
        boolean requestRouteToNewSys = vstOrderRouteService.isRequestRouteToNewSys();
        Assert.assertTrue(requestRouteToNewSys);
    }
}
