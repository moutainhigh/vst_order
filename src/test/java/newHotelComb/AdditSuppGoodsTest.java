package newHotelComb;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.order.dao.OrdItemAdditSuppGoodsDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class AdditSuppGoodsTest {
	@Autowired
	OrdItemAdditSuppGoodsDao  ordItemAdditSuppGoodsDao ;
	
	OrderItemAdditSuppGoods ordeitemAdd = new OrderItemAdditSuppGoods();
	
	@Test
	public void test(){
	OrderItemAdditSuppGoods ordeitemAdd = new OrderItemAdditSuppGoods();
	ordeitemAdd.setOrderItemId(5555555L);
	ordeitemAdd.setAddItSuppGoodsId(333333L);
	ordeitemAdd.setCreateDay(new Date());
//	ordeitemAdd.setQuamtity(4L);
	ordeitemAdd.setOrderItemAdditId(88888L);
	
	ordItemAdditSuppGoodsDao.insertOrdItemAdditSuppGoods(ordeitemAdd);
	System.out.println("success");
	}

}
