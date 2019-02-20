/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.OrdFunctionInfo;

/**
 * @author pengyayun
 *
 */
public class InsuranceSuppGoodsVo extends SuppGoods implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5060143365381566925L;
	
	private int quantity; //数量

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	
}
