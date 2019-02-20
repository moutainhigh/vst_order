package com.lvmama.vst.order.service.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderSupplierNotifyService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.order.service.IOrderPriceService;
import com.lvmama.vst.order.service.IOrderStockService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class SupplierOrderHandleServiceImplTest {
	
	@Autowired
	private OrderSupplierNotifyService orderSupplierNotifyService;
	
	 
	@Test
	public void testSavePerson(){
		String addCode="578488";
		ResultHandle handle = orderSupplierNotifyService.checkOrderTicketValid(addCode);
		Assert.assertNotNull(handle);
		System.out.println(handle.getMsg());
		Assert.assertTrue(handle.isSuccess());
	}
}
