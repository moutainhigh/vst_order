package newHotelComb;

import java.util.*;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.destbu.NewHotelBookComServiceImpl;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.vo.UserCouponVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class CreateOrder {
	
	private DestBuBuyInfo destBuBuyInfo;
	
	private OrdOrderDTO order;
	
	private OrdOrderDTO order2;
	
	private NewHotelBookComServiceImpl newHotelBookComService;
	
	@Autowired
	private NewHotelComOrderInitService newHotelComOrderInitService;
	
	@Before
	public void init(){
		
		order = new OrdOrderDTO();
		
		destBuBuyInfo = new DestBuBuyInfo();
		//base
		
		destBuBuyInfo.setProductId(1000041l);
		destBuBuyInfo.setAdultQuantity(1);
		destBuBuyInfo.setChildQuantity(0);
		destBuBuyInfo.setVisitTime("2016-11-10");
		destBuBuyInfo.setQuantity(1);
		//destBuBuyInfo.
		destBuBuyInfo.setUserNo(8917L);
		destBuBuyInfo.setUserId("3428a92f475861e20147586a65a70001");
		
		List<Item> itemList = new ArrayList<Item>();
		DestBuBuyInfo.Item item = new DestBuBuyInfo.Item();
		item.setAdultQuantity(1);
		item.setGoodsId(567423L);
		item.setVisitTime("2016-11-11");
		itemList.add(item);
		destBuBuyInfo.setItemList(itemList);
		
		Person person = new Person();
		person.setFullName("sumail");
		person.setMobile("13800138000");
		destBuBuyInfo.setContact(person);
		
		Person person2 = new Person();
		person.setFullName("universe");
		person.setMobile("13800138001");
		
		List<Person> personList = new ArrayList<Person>();
		person = new Person();
		person.setFullName("universe");
		person.setMobile("13800138001");
		personList.add(person);
		personList.add(person2);
		destBuBuyInfo.setTravellers(personList);
		
		destBuBuyInfo.setBooker(person);
		
		order2 = new OrdOrderDTO();
		
		ProdProduct product = new ProdProduct();
		product.setProductType("test");
		SuppGoods sg = new SuppGoods();
		sg.setCategoryId(32l);
		sg.setProdProduct(product);
		OrdOrderItem orderItem = new OrdOrderItem();
		orderItem.setPrice(100l);
		orderItem.setQuantity(1l);
		orderItem.setMainItem("true");
		orderItem.setCategoryId(32l);
		orderItem.setSuppGoods(sg);
		List<OrdOrderItem> ordOrderItemList = new ArrayList<OrdOrderItem>();
		ordOrderItemList.add(orderItem);
		order2.setOrderItemList(ordOrderItemList);
		order2.setCategoryId(32l);
		order2.setOughtAmount(1000l);
		
	}
	
	@Test
	public void testMainCreateOrder(){
		String operatorId = "3428a92f475861e20147586a65a70001";
		ResultHandleT<OrdOrder>  result  =	newHotelBookComService.createOrder(destBuBuyInfo, operatorId) ;
		System.out.println(result.getMsg());
		Assert.assertTrue(result.isSuccess());
	}
	
	@Test
	public void testInitOrderAndCalc() {
		
		newHotelComOrderInitService.initOrderAndCalc(destBuBuyInfo, order);
		
		Long orderId = order.getOrderId();
		Assert.assertTrue(orderId!=null);
		
	}
	
	@Test
	public void testGetUserCouponVOList(){
		//newHotelComOrderInitService.initOrderAndCalc(destBuBuyInfo, order);
		List<UserCouponVO> couponList = newHotelComOrderInitService.getUserCouponVOList(destBuBuyInfo, order2);
		
		if(couponList!=null && couponList.size()>0){
			//有优惠券即有券码
			Assert.assertTrue(couponList.get(0).getCouponCode()!=null);
		}
	}
}
