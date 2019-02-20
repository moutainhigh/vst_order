/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.dest.hotel.trade.hotelcomb.vo.UserCouponVO;
import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 订单下单接口
 * 
 * @author lancey
 * 
 */
public interface IBookService {

	/**
	 * 订单生成接口
	 * 
	 * @param buyInfo
	 *            订单数据
	 * @param operatorId
	 *            下单人,针对前台是下单人本人，后台是客服人员
	 * @return 订单创建成功后返回对应的订单
	 */
	ResultHandleT<OrdOrder> createOrder(final BuyInfo buyInfo, final String operatorId);

	
	ResultHandle saveOrderPerson(final Long orderId,final BuyInfo buyInfo);
	
	OrdOrderDTO initOrderAndCalc(BuyInfo buyInfo);

	//只初始化items和应付金额
	OrdOrderDTO initOrderBasic(BuyInfo buyInfo);
	
	OrdOrderDTO initOrderWithBuyInfo(BuyInfo buyInfo);
	
	/***
	 * 验证优惠券、奖金、现金、礼品卡、储值卡使用情况
	 * @param buyInfo
	 * @return
	 */
	String chechOrderPayForOther(BuyInfo buyInfo); 
	
	/**
     * 加载订单相关人员
     */
    public OrderRelatedPersonsVO loadOrderRelatedPersons(OrdPersonQueryTO ordPersonQueryTO);


	OrdOrder initOrderItems(BuyInfo buyInfo);
    
    
    
    /***
	 * 验证优惠券、奖金、现金、礼品卡、储值卡使用情况
	 * @param buyInfo
	 * @return
	 */
	String chechOrderPayForOther(BuyInfo buyInfo,List<UserCouponVO> userCouponVOList );


	ResultMessage checkFlightTicket(BuyInfo buyInfo, String flightTicketPrice); 
	
	ResultHandle saveNewOrderPerson(final Long orderId,final BuyInfo buyInfo);
}
