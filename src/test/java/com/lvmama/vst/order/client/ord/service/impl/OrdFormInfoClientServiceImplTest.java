package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.client.ord.po.OrdFormInfoQueryPO;
import com.lvmama.vst.back.client.ord.service.OrdFormInfoClientService;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouyanqun on 2016/9/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrdFormInfoClientServiceImplTest {
    private static final Log log = LogFactory.getLog(OrdFormInfoClientServiceImplTest.class);

    @Resource
    private OrdFormInfoClientService ordFormInfoClientService;

    @Test
    public void testFindOrdFormInfoList(){
        OrdFormInfoQueryPO ordFormInfoQueryPO = new OrdFormInfoQueryPO();
        ordFormInfoQueryPO.setOrderId(200618680L);
        ResultHandleT<List<OrdFormInfo>> ordFormInfoListResultHandleT = ordFormInfoClientService.findOrdFormInfoList(ordFormInfoQueryPO);
        List<OrdFormInfo> ordFormInfoList = ordFormInfoListResultHandleT.getReturnContent();
        log.info("ordFormInfoList is:" + GsonUtils.toJson(ordFormInfoList));
    }
}
