/**
 * 
 */
package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.order.po.OrdInvoice;


/**
 * 判断能否对状态修改.
 * @author yangbin
 *
 */
public interface InvoiceComp {

	/**
	 * 回调的判断操作.
	 * @param ordInvoice
	 * @return
	 */
	boolean hasChangeAble(OrdInvoice ordInvoice);
}
