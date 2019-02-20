package com.lvmama.vst.back.order.processer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderItemView;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderItemService;

public class OrderExpiredRefundProcesserTest extends OrderTestBase {
	
	private IOrdOrderItemService orderService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderService = (IOrdOrderItemService) applicationContext.getBean("ordOrderItemServiceImpl");
		}
	}
	
	@Test
	public void dataFetchTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", 62966861L);
		params.put("buCode", CommEnumSet.BU_NAME.TICKET_BU.getCode());
		params.put("categoryIds", Constant.TICKET_CATEGORY_IDS);
		params.put("orderStatus", OrderEnum.ORDER_STATUS.NORMAL.getCode());
		params.put("unDistributorId", Constant.DIST_OFFLINE_EXTENSION);
		params.put("expiredRefundFlagN", Constant.N_FLAG);
		
		List<OrdOrderItemView> list = orderService.selectListByParams(params);
		Assert.assertNotNull(list);
	}

}
