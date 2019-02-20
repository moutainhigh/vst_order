package newHotelComb;

import com.lvmama.comm.unity.utils.DateUtil;
import com.lvmama.vst.back.client.ord.service.OrderHouseControlService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.OrderTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijuntao on 2017/5/17.
 */
public class OrderHouseControlServiceTest extends OrderTestBase {

    @Autowired
    private OrderHouseControlService orderHouseControlService;

    @Test
    public void closeHouseNoticeTest(){
        SuppGoodsTimePrice suppGoodsTimePrice = new SuppGoodsTimePrice();
        suppGoodsTimePrice.setStockStatus("FULL");
        suppGoodsTimePrice.setStartDate(DateUtil.stringToDate("2014-08-10", "yyyy-MM-dd"));
        suppGoodsTimePrice.setEndDate(DateUtil.stringToDate("2014-08-10", "yyyy-MM-dd"));
        suppGoodsTimePrice.setSuppGoodsId(117438L);
        suppGoodsTimePrice.setSupplierId(1L);
        suppGoodsTimePrice.setSpecDate(DateUtil.stringToDate("2014-08-10", "yyyy-MM-dd"));
        suppGoodsTimePrice.setPermission(true);
        List<Long> suppGoodssIdList = new ArrayList<>();
        suppGoodssIdList.add(117438L);
        suppGoodsTimePrice.setSuppGoodsIdList(suppGoodssIdList);
        List<Integer> weekDay = new ArrayList<>();
        weekDay.add(1);
        weekDay.add(2);
        weekDay.add(3);
        weekDay.add(4);
        weekDay.add(5);
        weekDay.add(6);
        weekDay.add(7);
        suppGoodsTimePrice.setWeekDay(weekDay);
        orderHouseControlService.closeHouseNotice(suppGoodsTimePrice);

    }
}
