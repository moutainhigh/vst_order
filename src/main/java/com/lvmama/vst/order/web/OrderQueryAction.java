package com.lvmama.vst.order.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderLVCCVO;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComplexQueryService;
/**
 * 新老系统对接订单查询业务
 * pet_back lvcc
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderQueryAction extends BaseActionSupport{
	//日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderQueryAction.class);
	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	
	/**
	 * call center页面新订单查询
	 * 
	 * @param model
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryOrderForLVCC.do")
	@ResponseBody
	public void queryOrderForLVCC(Model model,String userId,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		//如果用户ID为空，直接返回
		if(!UtilityTool.isValid(userId)){
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("parameter is error");
			}
			return;
		}
		//为spring准备json的map
		Map<String,Object> map = new HashMap<String, Object>();
		//构造查询条件
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		//设置前台下单人
		condition.getOrderContentParam().setUserId(userId);
		//只取最新的5条
		condition.getOrderPageIndexParam().setBeginIndex(1);
		condition.getOrderPageIndexParam().setEndIndex(5);
		//订单表必查
		condition.getOrderFlagParam().setOrderTableFlag(true);
		//关联订单子项表
		condition.getOrderFlagParam().setOrderItemTableFlag(true);
		//关联订单人员表
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);
		//按照下单时间倒排序
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
		//设置分页参数
		condition.getOrderFlagParam().setOrderPageFlag(true);
		//调用查询接口查询该人最新的订单
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		//组装查询结果
		List<OrderLVCCVO> resultList = this.builderResult(orderList);
		//设置订单状态
		condition.getOrderStatusParam().setOrderStatus(OrderEnum.ORDER_STATUS.COMPLETE.name());
		//关闭分页标志
		condition.getOrderFlagParam().setOrderPageFlag(false);
		//调用接口查询该人已完成的订单
		List<OrdOrder> completeOrderList = complexQueryService.queryOrderListByCondition(condition);
		//完成订单数
		Integer completeOrderNum = 0;
		//完成订单金额
		BigDecimal completeOrderMoney = BigDecimal.valueOf(0);
		if(null != completeOrderList && !completeOrderList.isEmpty()){
			completeOrderNum = completeOrderList.size();
			for(OrdOrder order:completeOrderList){
				completeOrderMoney = completeOrderMoney.add(BigDecimal.valueOf(order.getActualAmount()));
			}
		}
		map.put("completeOrderNum", completeOrderNum);
		map.put("completeOrderMoney", completeOrderMoney.toString());
		map.put("orderList", resultList);
		
		String jsonStr = JSONObject.fromObject(map).toString();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(jsonStr);
		}
		
		JSONOutput.writeJSONP(response, jsonStr, request.getParameter("callback"));
	}

	/**
	 * 为json传准备结果
	 * 
	 * @param orderList
	 */
	private List<OrderLVCCVO> builderResult(List<OrdOrder> orderList) {
		List<OrderLVCCVO> resultList = new ArrayList<OrderLVCCVO>();
		if(null!=orderList && !orderList.isEmpty()){
			for(OrdOrder order:orderList){
				OrderLVCCVO orderVO = new OrderLVCCVO();
				//订单编号
				orderVO.setOrderId(String.valueOf(order.getOrderId()));
				if(null != order.getMainOrderItem()){
					//产品名称
					orderVO.setProductName(order.getMainOrderItem().getProductName()+order.getMainOrderItem().getSuppGoodsName());
					//购买数量
					orderVO.setBuyCount(String.valueOf(order.getMainOrderItem().getQuantity()));
				}
				//下单时间
				orderVO.setOrderTime(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
				
				//联系人手机号码
				String mobile="";
				if (order.getContactPerson()!=null) {
					mobile=order.getContactPerson().getMobile();
				}
				orderVO.setContactMobile(mobile);
				//订单金额
				orderVO.setOrderMoney(order.getOughtAmountYuan());
				resultList.add(orderVO);
			}
		}
		return resultList;
	}
}
