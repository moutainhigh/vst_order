/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;

import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * @author lancey
 *
 */
public class OrdOrderItemDTO extends OrdOrderItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8511139649862331620L;

	

	private  OrdOrderDTO orderDTO;
	private OrdOrderItemExtendDTO ordOrderItemExtendDTO;//子订单扩展信息


	public OrdOrderItemExtendDTO getOrdOrderItemExtendDTO() {
		return ordOrderItemExtendDTO;
	}

	public void setOrdOrderItemExtendDTO(OrdOrderItemExtendDTO ordOrderItemExtendDTO) {
		this.ordOrderItemExtendDTO = ordOrderItemExtendDTO;
	}

	public OrdOrderDTO getOrderDTO() {
		return orderDTO;
	}


	public void setOrderDTO(OrdOrderDTO orderDTO) {
		this.orderDTO = orderDTO;
	}
	
	
}
