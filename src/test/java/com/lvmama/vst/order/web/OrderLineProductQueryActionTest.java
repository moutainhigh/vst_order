package com.lvmama.vst.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderLineProductQueryActionTest {
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Test
	public void test() {
	
		 Map<String,Object> params = new HashMap<String, Object>();
		 params.put("productName","测试产品");
		 params.put("productId","429446");
		 params.put("distributorId", Constant.DIST_BACK_END);
		 params.put("bizCategoryId",BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId());
		 ResultHandleT<List<ProdProduct>> resultHandleT = prodProductClientService.findWifiProductByparams(params);
		 List<ProdProduct> prodProductList = resultHandleT.getReturnContent();
		 System.out.println("******************************************************************************");
		 System.out.println("**********************prodProductList******************************"+prodProductList.size());
		 for (ProdProduct prodProduct : prodProductList) {
			System.out.println("prodProduct:"+prodProduct.getProductName());
		}
		 
	}

}
