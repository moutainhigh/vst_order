package com.lvmama.vst.back.order.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.vo.PayAndPreVO;
/**
 * 支付业务接口单元测试
 * 
 * @author wenzhengtao
 *
 */
public class PayPaymentServiceAdapterTest extends OrderTestBase{
	
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	
	@Before
	public void prepare() {
		super.prepare();
		if(null != applicationContext){
			payPaymentServiceAdapter = (IPayPaymentServiceAdapter) applicationContext.getBean(IPayPaymentServiceAdapter.class);
		}
	}
	
	@Test
	public void testFindPaymentInfo() {
		try {
			Long orderId = 349455L;
			String bizType = "SUPER_ORDER";
			List<PayAndPreVO> payAndPreVOList = payPaymentServiceAdapter.findPaymentInfo(orderId,bizType);
			System.out.println(payAndPreVOList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
