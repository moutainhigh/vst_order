package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderPrice;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;

public interface IOrderPriceService {

	/**
	 * 价格计算
	 * 
	 * @param buyInfo
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	public PriceInfo countPrice(BuyInfo buyInfo);
	
	/**
	 * 新价格计算
	 * 
	 * @param buyInfo
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	public PriceInfo countPriceComb(DestBuBuyInfo buyInfo);
	
	/**
	 * 退改金额计算
	 * 
	 * @param buyInfo
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	public Long cancelOrderDeductAmount(BuyInfo buyInfo);
	/**
	 * 根据buyInfo查询促销列表
	 * @param buyInfo
	 * @return
	 */
	public ResultHandleT<List<PromPromotion>> queryPromPromotion(BuyInfo buyInfo);
	
	/**
	 *查询订单最高使用奖金数
	 * @param buyInfo
	 * @return
	 */
	public long queryMaxBounsAmount(BuyInfo buyInfo);
	
	/**
	 * 检查促销余额是否满足当前促销
	 * @param buyInfo
	 * @return
	 */
	public ResultHandleT<List<PromPromotion>> checkPromAmount(BuyInfo buyInfo);
	
    /**
	  * 查询现金、奖金账户.
	  *
	  * @param userId
	  *            用户ID
	  * @return 现金账户
	  */
	 public VstCashAccountVO queryMoneyAccountByUserId(final Long userId);
	 
	 /**
		* 描述：
		* @param @param bizType 业务类型
		* @param @param userId 用户ID
		* @param @param orderId 订单Id
		* @param @param payAmount 支付金额，以分为单位
		* @param @return 
		* @return boolean  
		* @exception:  
		* 创建人：yefengyun   
		* 创建时间：2015-7-10 下午1:26:34
		 */
		public boolean vstPayFromMoneyAccount(String bizType,final Long userId,final Long orderId,final Long payAmount);
		/**
		* 描述：奖金抵扣
		* @param @param bizType
		* @param @param orderId
		* @param @param userId
		* @param @param payAmount
		* @param @return 
		* @return boolean  
		* @exception:  
		* 创建人：yefengyun   
		* 创建时间：2015-7-15 下午5:35:28
		 */
		public boolean vstPayFromBonusAccount(String bizType,Long orderId,  Long userId,Long payAmount);
		
		/**
		 * 根据订单信息查询促销列表
		 * @param ordOrderPrice
		 * @return
		 */
		public List<PromPromotion> vstFindPromPromotion(OrdOrderPrice ordOrderPrice);
		
		
		/**
		 * 根据订单品类进行相应的价格计算
		 * @param buyInfo
		 * @return
		 */
		public PriceInfo countPriceBase(BuyInfo buyInfo);
			
		public List<PromPromotion> getPromotions(OrdOrderDTO order,PriceInfo priceInfo);
		

}
