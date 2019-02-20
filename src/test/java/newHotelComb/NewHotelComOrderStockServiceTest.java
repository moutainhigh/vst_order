package newHotelComb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;
import com.lvmama.vst.order.service.book.destbu.NewHotelBookComServiceImpl;
import com.lvmama.vst.order.vo.OrdOrderDTO;
/**
 * 新酒套餐库存校验扣减接口测试用例
 * @author caiyingshi
 *
 */
public class NewHotelComOrderStockServiceTest extends OrderTestBase{
	

	@Autowired
	private NewHotelBookComServiceImpl newHotelBookComServiceImpl;
	

	private OrdOrderDTO order;
	private OrdOrderDTO order2;
	private DestBuBuyInfo destBuBuyInfo;
	@Before
	public void init(){
		
		order = new OrdOrderDTO();
		
		destBuBuyInfo = new DestBuBuyInfo();
		//base
		/**
		 * 共享：
		 * 产品ID:1000251L 
		 * 商品ID:2575967L
		 * 
		 * 独立：
		 * 产品ID:1000041l
		 * 商品ID：2575746L
		 * 
		 */
		destBuBuyInfo.setProductId(1000251L);  //
		destBuBuyInfo.setAdultQuantity(1);
		destBuBuyInfo.setChildQuantity(0);
		destBuBuyInfo.setVisitTime("2016-11-23");
		destBuBuyInfo.setQuantity(1);
		destBuBuyInfo.setDistributionId(2L);
		//destBuBuyInfo.
		destBuBuyInfo.setUserNo(8917L);
		destBuBuyInfo.setUserId("3428a92f475861e20147586a65a70001");
		
		List<Item> itemList = new ArrayList<Item>();
		DestBuBuyInfo.Item item = new DestBuBuyInfo.Item();
		item.setAdultQuantity(1);
		item.setGoodsId(2575967L);
		item.setVisitTime("2016-11-23");
		item.setQuantity(5);
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
	/**
	 * 库存校验接口
	 */
	@Test
	public void testCalOrderStock() {
		newHotelBookComServiceImpl.calOrderStock(destBuBuyInfo,null);
	}
	/**
	 * 库存扣减接口
	 */
	@Test
	public void testDeductStock() {
		newHotelBookComServiceImpl.deductStock(order);
	}
	
	//功能列表
	
	/**
	 * 库存还原
	 * 
	 * com.lvmama.vst.order.service.impl.OrdOrderUpdateServiceImpl.updateOrderForCancel(Long, String, String, String, String);
	 * 
	 */
	
	/**
	 * ebk传真
	 * com.lvmama.vst.back.ebooking.service.proxy.EbkCertificateProxyServiceImpl.orderItemProcess(OrdOrder, Message)
	 * 
	 */
	
}
