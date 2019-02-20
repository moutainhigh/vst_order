package com.lvmama.vst.order.service.book;


import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;

import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.pet.vo.CouponCheckParam;
import com.lvmama.vst.pet.vo.UserCouponVO;

public interface NewHotelComOrderInitService {

	/**
	 *初始化订单和子订单（以及各种计算） 
	 *@author FANGXIANG
	 *@param DestBuBuyInfo
	 */
	public OrdOrderDTO	initOrderAndCalc(DestBuBuyInfo buyInfo,OrdOrderDTO order) throws BusinessException;
	/**
	 * 子订单验证
	 * @author FANGXIANG
	 * @param suppGoods
	 * @param item
	 *@param orderItem
	 *@param   order
	 */
	public ResultHandle validate(SuppGoods suppGoods, Item item,
			OrdOrderItemDTO orderItem, OrdOrderDTO order)  throws BusinessException;
	/**
	 * 计算订单奖金抵扣相关
	 * @author fangxiang
	 * @param  order
	 * 
	 */
	public void  calcRebate(OrdOrderDTO order);
	
	/**
	 * 根据用户输入优惠券号（集合）check是否满足该笔订单若满足返回优惠券信息（集合）
	 * @author yangguanyu
	 * @param  destBuBuyInfo
	 * 
	 */
	public List<UserCouponVO> getUserCouponVOList(DestBuBuyInfo destBuBuyInfo,OrdOrderDTO order);
	
	/**
	 *初始化订单和子订单（以及各种计算）--仅前台 
	 *@author yanguanyu
	 *@param DestBuBuyInfo
	 */
	public OrdOrderDTO	initOrderAndCalcForFront(DestBuBuyInfo buyInfo,OrdOrderDTO order) throws BusinessException;
}
