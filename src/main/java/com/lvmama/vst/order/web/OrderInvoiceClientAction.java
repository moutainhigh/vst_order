/**
 * 
 */
package com.lvmama.vst.order.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.back.prod.po.ProdProduct.COMPANY_TYPE_DIC;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.json.JSONResult;
import com.lvmama.vst.order.service.IOrdInvoiceService;

/**
 * @author lancey
 *
 */

@Controller
@RequestMapping("/order/invoice/get.do")
public class OrderInvoiceClientAction {

	@Autowired
	private IOrdInvoiceService ordInvoiceService;
	
	/**
	 * 更新发票单号
	 * @param invoiceId
	 * @param invoiceNo
	 * @return
	 */
	@RequestMapping(params="method=changeStatus")
	public void changeStatus(HttpServletResponse res,Long invoiceId,String invoiceNo){
		JSONResult result = new JSONResult();
		ordInvoiceService.updateInvoiceNo(invoiceId, invoiceNo, "SYSTEM");
		result.output(res);
	}
	
	@RequestMapping(params="method=queryInvoiceList")
	public void queryInvoiceList(HttpServletResponse response,String endTime, String companyType){
		JSONResult result = new JSONResult();
		Date date = DateUtil.toSimpleDate(endTime);
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("endVisitTime", DateUtil.getDayEnd(date));
		// 公司主体参数处理
		if (StringUtils.isNotBlank(companyType)) {
			for (COMPANY_TYPE_DIC item : COMPANY_TYPE_DIC.values()) {
				if (item.name().equals(companyType)) {
					param.put("companyType", companyType);
				}
			}
		}
		List<OrdInvoice> list = ordInvoiceService.getOrdInvoiceListByParam2(param);
		JSONArray array = new JSONArray();
		for(OrdInvoice invoice:list){
			array.add(conver(invoice));
		}
		result.put("size", list.size());
		result.put("list", array);
		result.output(response);
	}
	@RequestMapping(params="method=queryInvoiceDetail")
	public void queryInvoiceDetail(HttpServletResponse response,long invoiceId){
		JSONResult result = new JSONResult();
		OrdInvoice invoice = ordInvoiceService.selectByPrimaryKey(invoiceId);
		if(invoice==null){
			result.put("find", false);
		}else{
			result.put("detail", conver(invoice));
			result.put("find", true);
		}
		result.output(response);
	}
	
	private JSONObject conver(OrdInvoice invoice){
		JSONObject info=new JSONObject();
		info.put("amount",invoice.getAmountYuan());
		info.put("title",invoice.getTitle());
		info.put("detail",invoice.getContent());
		info.put("invoiceId",invoice.getOrdInvoiceId());
		if(invoice.getMemo() != null){
			info.put("memo",invoice.getMemo());
		}
		info.put("companyType", invoice.getCompanyType()!=null?invoice.getCompanyType():"");
		return info;
	}
}





