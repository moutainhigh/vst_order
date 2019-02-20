package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.utils.MemoryTestObject;
import com.lvmama.vst.order.web.line.service.LineProdPackageGroupService;

public class OrderExpressServiceImplTest extends OrderTestBase {

	
	
	private SuppGoodsClientService suppGoodsClientRemote;
	
	
	private OrderService orderService;

	@Before
	public void prepare() {
		super.prepare();
		if (this.applicationContext != null) {
			this.suppGoodsClientRemote = (SuppGoodsClientService) this.applicationContext
					.getBean("suppGoodsClientService");
		}
		
	}
	
	//@Before
	public void init(){
		System.out.println("111");
	}
	
	@Test
	public void testCreateProduct(){
		
		List<Long> suppGoodsIds=new ArrayList<Long>();
		suppGoodsIds.add(567263L);
		ResultHandleT<Map<Long, ExpressSuppGoodsVO>> resultHandler =suppGoodsClientRemote.findSuppGoodsExpreeCost(suppGoodsIds,"9","526");
		if (resultHandler != null&&resultHandler.isSuccess()) {
			Map<Long, ExpressSuppGoodsVO> map = resultHandler.getReturnContent();
			if (MapUtils.isNotEmpty(map)) {
				ExpressSuppGoodsVO item = null;
				Iterator<ExpressSuppGoodsVO> itr = map.values().iterator();
				while (itr.hasNext()) {
					item = itr.next();
					SuppGoodsNotimeTimePrice suppGoodsNotimeTimePrice = item
							.getSuppGoodsNotimeTimePrice();
					if (suppGoodsNotimeTimePrice != null
							&& suppGoodsNotimeTimePrice.getPrice() > 0) {
						System.out.println(suppGoodsNotimeTimePrice.getPrice());
					}
				}
			}
		}
	}
	
	public static void main(String[] args){
		ArrayList test=new ArrayList();		
		System.out.println(CollectionUtils.isNotEmpty(test));
	}
}
