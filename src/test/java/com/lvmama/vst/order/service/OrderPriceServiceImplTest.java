/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.service.impl.OrderPriceServiceImpl;

/**
 * @author pengyayun
 *
 */
public class OrderPriceServiceImplTest extends OrderTestBase{
	
	private IOrderPriceService orderPriceService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderPriceService = (IOrderPriceService) applicationContext.getBean("orderPriceServiceImpl");
		}
	}
	
	@Test
	public void testCancelOrderDeductAmount(){
		BuyInfo buyInfo=new BuyInfo();
		buyInfo.setIp("180.169.51.82");
		buyInfo.setUserId("3428a92f4393d4070143949c467f0001");
		buyInfo.setDistributionId(3L);
		List<Item> itemList=new ArrayList<Item>();
		Item item=new Item();
		item.setGoodsId(472L);
		item.setQuantity(2);
		item.setVisitTime("2014-03-20");
		HotelAdditation hotelAdditation=new HotelAdditation();
		hotelAdditation.setLeaveTime("2014-03-22");
		hotelAdditation.setArrivalTime("19:30");
		item.setHotelAdditation(hotelAdditation);
		itemList.add(item);
		buyInfo.setItemList(itemList);
		Long result=orderPriceService.cancelOrderDeductAmount(buyInfo);
		System.out.println("结果："+result);
		//此商品的此日期内 为超时担保且扣款类型为首日所以扣款价格应为：15800(单位分)*2= 31600
		Assert.assertEquals(31600, result.longValue());
	}
	
	@Test
	public void testGetDoBookPolicy(){
		
		TimePrice timePrice=new TimePrice();
		timePrice.setTimePriceId(53713944L);
		timePrice.setSuppGoodsId(454250L);
		timePrice.setSupplierId(14544L);
		timePrice.setSpecDate(DateUtil.getDateByStr("2014-6-27", "yyyy-MM-dd"));
		timePrice.setStock(0L);
		timePrice.setPrice(9900L);
		timePrice.setSettlementPrice(9000L);
		timePrice.setRestoreFlag("Y");
		timePrice.setAheadBookTime(0L);
		timePrice.setOvershellFlag("Y");
		timePrice.setBreakfast(1);
		timePrice.setLatestHoldTime(0L);
		timePrice.setGuarType("CREDITCARD"); //CREDITCARD
		timePrice.setDeductType("FULL");
		timePrice.setLatestCancelTime(1440L);
		timePrice.setBookLimitType("TIMEOUTGUARANTEE");
		timePrice.setLatestUnguarTime(2L);
		timePrice.setStockStatus("NORMAL");
		timePrice.setOnsaleFlag("Y");
		timePrice.setGuarQuantity(null);
		timePrice.setDeductValue(null);
		timePrice.setFreeSaleFlag("N");
		timePrice.setStockFlag("N");
		timePrice.setCancelStrategy("RETREATANDCHANGE");
		String  payTarget="PAY";
		Date visitTime=DateUtil.getDateByStr("2014-6-27", "yyyy-MM-dd");
		int quantity=0;
		String arrivalTime="14:00";
		String cancelStrategy=SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
		Date highPriceDate=DateUtil.getDateByStr("2014-6-27", "yyyy-MM-dd");
		long maxPrice=0;
		OrderPriceServiceImpl orderPriceService=new OrderPriceServiceImpl();
		
		String resultStr=orderPriceService.getDoBookPolicy(timePrice, visitTime, quantity, arrivalTime, cancelStrategy, payTarget, highPriceDate, maxPrice,null);
		
		System.out.println(resultStr);
	}
}
