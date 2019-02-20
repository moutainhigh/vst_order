/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;

/**
 * @author liuxiuxiu
 *
 */
public class ordSettlementPriceRecordListVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5079501787196067686L;

	private List<OrdSettlementPriceRecord> form = new ArrayList<OrdSettlementPriceRecord>();

	public List<OrdSettlementPriceRecord> getForm() {
		return form;
	}

	public void setForm(List<OrdSettlementPriceRecord> form) {
		this.form = form;
	}
	
	
	
}
