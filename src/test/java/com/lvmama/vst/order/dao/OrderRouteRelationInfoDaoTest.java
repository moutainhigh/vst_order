package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.route.dao.OrderRouteRelationInfoDao;
import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;
import com.lvmama.vst.comm.utils.Constants;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

public class OrderRouteRelationInfoDaoTest extends OrderTestBase {
    @Resource
    private OrderRouteRelationInfoDao orderRouteRelationInfoDao;
    @Test
    public void testInsert(){
        Date time = Calendar.getInstance().getTime();
        OrderRouteRelationInfo orderRouteRelationInfo = new OrderRouteRelationInfo(322342L, 234234243234L, 15L, 1L, 4L, "DISTRIBUTOR_API", 'Y', 'Y', time, time);
        int insert = orderRouteRelationInfoDao.insert(orderRouteRelationInfo);
        Assert.assertEquals(1, insert);
    }
}
