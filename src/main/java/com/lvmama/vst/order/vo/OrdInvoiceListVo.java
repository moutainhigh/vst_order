/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.back.order.po.OrdInvoice;

/**
 * @author liuxiuxiu
 *
 */
public class OrdInvoiceListVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5079501787196067686L;

	private List<OrdInvoice> form = new ArrayList<OrdInvoice>();

	public List<OrdInvoice> getForm() {
		return form;
	}

	public void setForm(List<OrdInvoice> form) {
		this.form = form;
	}
	
	
	
}
