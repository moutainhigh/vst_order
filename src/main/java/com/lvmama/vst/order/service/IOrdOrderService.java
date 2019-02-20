package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.control.vo.ResPrecontrolOrderVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.ebooking.vo.DepartureNoticeVo;

public interface IOrdOrderService {
	
	public List<DepartureNoticeVo> selectDepartureNoticeList(Map<String, Object> paramsMap);
	
	public Long getDepartureNoticeCount(Map<String, Object> params);
	
	/**
	 * 根据添加查询订单信息
	 * @param params
	 * @return
	 */
	public List<OrdOrder> getordOrderList(Map<String, Object> params);

	/**
	 * 根据订单id查询订单集合，单表查询
	 * */
	List<OrdOrder> getOrderList(List<Long> orderIdList);

	List<OrdOrderItem> listOrderItemByConditions(Page<OrdOrderItem> page,Map<String, Object> paramMap);

	/**
	 * 根据查询条件查询总记录数
	 * @param paramMap 查询条件
	 * @return 总记录数
	 * @author Zhang.Wei
	 */
	Long listOrderItemByConditionsCount(Map<String, Object> paramMap);

	/**
	 * 根据订单编号查询订单
	 * @param orderId 订单编号
	 * @return 订单对象，里面包含订单详情
	 * @author Zhang.Wei
	 */
	OrdOrder loadOrderWithItemByOrderId(Long orderId);

	/**
	 * 更新订单权限
	 * @param ordOrder
	 * @return
	 */
	int updateManagerIdPerm(OrdOrder ordOrder);

    /**
     * 得到买断订单列表
     * @param paramsMap
     * @return
     */
    public List<ResPrecontrolOrderVo> findPercontrolGoodsOrderList(Map<String,Object> paramsMap);
    
    /**
     * 得到买断订单列表
     * @param paramsMap
     * @return
     */
    public List<ResPrecontrolOrderVo> findPercontrolHotelGoodsOrderList(Map<String,Object> paramsMap);

    public Long countPercontrolGoodsOrderList(Map<String,Object> paramsMap);

    /**
     * 根据订单id查询订单对象
     * @param orderId
     * @return
     */
	public OrdOrder findByOrderId(Long orderId);
    
    public Long countPercontrolHotelGoodsOrderList(Map<String,Object> paramsMap);

	/**
	 * 查询酒店和酒店套餐相关订单
	 * @param params
	 * @return
	 */
	public List<OrdOrder> findHotelOrderListByParams(Map<String,Object> params);
    
    /**
     * 查询某个预控某个商品下历史推送订单数
     * @param paramsMap
     * @return
     */
    public Long countPercontrolGoodsHisOrder(Map<String,Object> paramsMap);
    /**
     * 查询某个预控某个商品下历史推送订单
     * @param paramsMap
     * @return
     */
    public List<ResPrecontrolOrderVo> findPercontrolGoodsHisOrderList(Map<String,Object> paramsMap);

	/**
	 * 更新订单备注
	 * @param order
	 * @return
	 */
	public int updateOrderMemo(OrdOrder order);

    /**
     * 查询邮寄订单
     * @param param
     * @return
     */
    public List<OrdOrder> selectMailOrderInfoByParams(Map<String, Object> param);
    
    public List<OrdOrder> getOrderIdsForSendMail(Map<String, Object> param);

	/**
	 * 查询距当前时间间隔intervalDays的被取消或者已履行的订单编号
	 * @param intervalDays 距当前时间间隔
	 * @return
	 */
	List<Long> queryOrderForDelWorkflowByParams(Map<String, Object> params);
}
