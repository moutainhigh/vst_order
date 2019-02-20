package com.lvmama.vst.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.VstSearchSaleService;

@Controller
public class CalculateProductSalesAction extends BaseActionSupport{
	
	@Autowired
	private VstSearchSaleService vstSearchSaleService;	
	
	@RequestMapping("/ord/order/calculateProductSalesJob.do")
	public void execute(){
		vstSearchSaleService.createVstSearchSale();
	}
}
