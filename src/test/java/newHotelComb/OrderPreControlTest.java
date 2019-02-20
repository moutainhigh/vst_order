package newHotelComb;

import com.lvmama.precontrol.vo.VstOrderItemVo;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.precontrol.service.RemoteResPreControlBindGoodsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by zhouguoliang on 2017/4/27.
 */
public class OrderPreControlTest extends OrderTestBase {

    @Autowired
    private RemoteResPreControlBindGoodsService remoteResPreControlBindGoodsService;
    @Test
    public  void test(){
        Map<String, Object> objectObjectHashMap = new HashMap<>();

        objectObjectHashMap.put("goodId",969594L);
        objectObjectHashMap.put("tradeEffectDate", new Date());
        objectObjectHashMap.put("tradeExpiryDate", new Date());
        Long orderItemNum = remoteResPreControlBindGoodsService.getOrderItemNum(objectObjectHashMap);
        System.out.println(orderItemNum);
    }

    @Test
    public  void test2(){
        Map<String, Object> params = new HashMap<String, Object>();
        List<Long>  goodIds=new ArrayList<Long>();
        goodIds.add(969594L);
        params.put("preControlPolicyID", 123);
        params.put("goodIds", goodIds);
        params.put("startDate", new Date());
        params.put("endDate", new Date());
        List<VstOrderItemVo> vstNotBuyoutOrder = remoteResPreControlBindGoodsService.getVstNotBuyoutOrder(params);
        System.out.println(vstNotBuyoutOrder);
    }

}
