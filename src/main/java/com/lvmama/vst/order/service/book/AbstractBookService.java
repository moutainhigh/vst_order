package com.lvmama.vst.order.service.book;

import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;


public class AbstractBookService {

	@Autowired
	protected IOrdMulPriceRateService ordMulPriceRateService;
	protected void throwNullException(String message) {
		throw new OrderException("H0001",message);
	}
	
	protected void throwIllegalException(String message){
		throw new OrderException("H002", message);
	}
	
    protected void throwIllegalException(String code,String message) {
        throw new OrderException(code,message);
    }
	
	@Autowired
	protected OrderOrderFactory orderOrderFactory;
}