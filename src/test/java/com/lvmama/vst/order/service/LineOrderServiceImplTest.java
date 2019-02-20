package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.utils.MemoryTestObject;
import com.lvmama.vst.order.web.line.service.LineProdPackageGroupService;

public class LineOrderServiceImplTest extends OrderTestBase {

	
	
	private LineProdPackageGroupService lineProdPackageGroupServiceImpl;
	
	
	private OrderService orderService;

	@Before
	public void prepare() {
		super.prepare();
		if (this.applicationContext != null) {
			this.lineProdPackageGroupServiceImpl = (LineProdPackageGroupService) this.applicationContext
					.getBean("lineProdPackageGroupServiceImpl");
			this.orderService=(OrderService)this.applicationContext.getBean("orderServiceRemote");
		}
		
	}
	
	
	
	
	
	//@Before
	public void init(){
		System.out.println("111");
	}
	
	@Test
	public void testCreateProduct(){
		
		BuyInfo buyInfo=(BuyInfo)MemoryTestObject.getObject("buyInfo");
		orderService.countPrice(buyInfo);
		
		// 95930 当地游
					// 95001 酒店套餐
					// 95747跟团游升级
					// 95721酒店升级
					// 95722自主打包
//		this.orderLineProductQueryAction.getTestVo(95930, new Date());
		
		
		//单线程线程池
		ExecutorService service2=Executors.newSingleThreadExecutor();
		//无上限自动回收的线程池
		ExecutorService service=Executors.newCachedThreadPool();
		//常规数量线程池
		ExecutorService service1=Executors.newFixedThreadPool(10);	
		//timer线程池
		ExecutorService servic3=Executors.newScheduledThreadPool(10);
		
	}
	
	public static void main(String[] args){
		ArrayList test=new ArrayList();		
		System.out.println(CollectionUtils.isNotEmpty(test));
	}
}
