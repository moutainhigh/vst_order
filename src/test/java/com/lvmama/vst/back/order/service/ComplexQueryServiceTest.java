package com.lvmama.vst.back.order.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderActivityParam;
import com.lvmama.vst.comm.vo.order.OrderContentParam;
import com.lvmama.vst.comm.vo.order.OrderExcludedParam;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.comm.vo.order.OrderPageIndexParam;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.vo.order.OrderStatusParam;
import com.lvmama.vst.comm.vo.order.OrderTimeRangeParam;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * 订单综合查询单元测试
 * 
 * @author wenzhengtao
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class ComplexQueryServiceTest{
	@Autowired
	private IComplexQueryService complexQueryService;
	
	/**
	 * 加载spring配置文件
	 * 
	 */
	@Before
	public void prepare() {
		
	}
	
	/**
	 * 测试根据条件查询订单集合
	 * 
	 */
	//@Test
	public void testQueryOrderListByCondition(){
		try {
			long startTime = System.currentTimeMillis();
			
			//分类组装条件
			OrderActivityParam orderActivityParam = new OrderActivityParam();//订单活动
			OrderContentParam orderContentParam = new OrderContentParam();//订单内容
			OrderExcludedParam orderExcludedParam = new OrderExcludedParam();//订单排除
			OrderIndentityParam orderIndentityParam = new OrderIndentityParam();//订单主键
			OrderPageIndexParam orderPageIndexParam = new OrderPageIndexParam();//订单分页
			OrderStatusParam orderStatusParam = new OrderStatusParam();//订单状态
			OrderTimeRangeParam orderTimeRangeParam = new OrderTimeRangeParam();//订单时间
			
			OrderFlagParam orderFlagParam = new OrderFlagParam();//订单表标志
			orderFlagParam.setOrderItemTableFlag(true);
			orderFlagParam.setOrderPackTableFlag(true);
			orderFlagParam.setOrderPersonTableFlag(true);
			orderFlagParam.setOrderAmountItemTableFlag(true);
			orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
			orderFlagParam.setOrderPageFlag(false);
			
			//与订单子项ID关联的表
			orderFlagParam.setOrderHotelTimeRateTableFlag(true);
			
			List<OrderSortParam> orderSortParamList = new ArrayList<OrderSortParam>();//订单排序
			orderSortParamList.add(OrderSortParam.CREATE_TIME_DESC);
			
			//设置到接口参数
			ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
			condition.setOrderActivityParam(orderActivityParam);
			condition.setOrderContentParam(orderContentParam);
			condition.setOrderExcludedParam(orderExcludedParam);
			condition.setOrderFlagParam(orderFlagParam);
			condition.setOrderIndentityParam(orderIndentityParam);
			condition.setOrderIndentityParam(orderIndentityParam);
			condition.setOrderPageIndexParam(orderPageIndexParam);
			condition.setOrderSortParams(orderSortParamList);
			condition.setOrderStatusParam(orderStatusParam);
			condition.setOrderTimeRangeParam(orderTimeRangeParam);
			
			//调用接口
			List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
			
			long endTime = System.currentTimeMillis();
			System.out.println("耗时:"+(endTime-startTime)+"毫秒");
			
			//验证结果
			System.out.println(ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE, true));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("系统发生内部错误"+e.getMessage());
		}
	}
	
	/**
	 * 根据条件查询订单总数
	 */
	@Test
	public void testQueryOrderCountByCondition(){
		try {
			long startTime = System.currentTimeMillis();
			
			//分类组装条件
			OrderActivityParam orderActivityParam = new OrderActivityParam();//订单活动
			OrderContentParam orderContentParam = new OrderContentParam();//订单内容
			OrderExcludedParam orderExcludedParam = new OrderExcludedParam();//订单排除
			OrderFlagParam orderFlagParam = new OrderFlagParam();//订单表标志
			OrderIndentityParam orderIndentityParam = new OrderIndentityParam();//订单主键
			OrderPageIndexParam orderPageIndexParam = new OrderPageIndexParam();//订单分页
			OrderStatusParam orderStatusParam = new OrderStatusParam();//订单状态
			OrderTimeRangeParam orderTimeRangeParam = new OrderTimeRangeParam();//订单时间
			
			List<OrderSortParam> orderSortParamList = new ArrayList<OrderSortParam>();//订单排序
			orderSortParamList.add(OrderSortParam.CREATE_TIME_DESC);
			
			//设置到接口参数
			ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
			condition.setOrderActivityParam(orderActivityParam);
			condition.setOrderContentParam(orderContentParam);
			condition.setOrderExcludedParam(orderExcludedParam);
			condition.setOrderFlagParam(orderFlagParam);
			condition.setOrderIndentityParam(orderIndentityParam);
			condition.setOrderIndentityParam(orderIndentityParam);
			condition.setOrderPageIndexParam(orderPageIndexParam);
			condition.setOrderSortParams(orderSortParamList);
			condition.setOrderStatusParam(orderStatusParam);
			condition.setOrderTimeRangeParam(orderTimeRangeParam);
			
			//调用接口
			Long totalCount = complexQueryService.queryOrderCountByCondition(condition);
			
			long endTime = System.currentTimeMillis();
			System.out.println("耗时:"+(endTime-startTime)+"毫秒");
			
			//校验结果
			System.out.println(totalCount);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("系统发生内部错误"+e.getMessage());
		}
	}
	
	/**
	 * 根据某一订单ID查询订单对象
	 * 
	 */
	//@Test
	public void testQueryOrderByOrderId(){
		try {
			long startTime = System.currentTimeMillis();
			
			Long orderId = 289L;
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			
			long endTime = System.currentTimeMillis();
			System.out.println("耗时:"+(endTime-startTime)+"毫秒");
			
			System.out.println(ToStringBuilder.reflectionToString(order, ToStringStyle.MULTI_LINE_STYLE));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("系统发生内部错误:"+e.getMessage());
		}
	}
	
	//@Test
	public void testQueryOrder(){
		Long orderId=42005L;
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		Assert.assertNotNull(order);
		boolean f=false;
		for(OrdOrderItem orderItem:order.getOrderItemList()){
			if(orderItem.getOrdItemPersonRelationList()!=null&&!orderItem.getOrdItemPersonRelationList().isEmpty()){
				f=true;
			}
		}
		Assert.assertTrue(f);
	}
}
