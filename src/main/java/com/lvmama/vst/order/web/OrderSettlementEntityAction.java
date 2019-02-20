package com.lvmama.vst.order.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppContractClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.IOrderAmountChangeService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrderSettlementService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 结算对象相关
 * @author sangbowei
 */
@Controller
@RequestMapping("/order/ordersettlemententity")
public class OrderSettlementEntityAction extends BaseActionSupport {
	// 日志记录器
	protected static final Logger LOG = LoggerFactory.getLogger(OrderSettlementEntityAction.class);


	private final static String RESULT_SUCCESS = "SUCCESS";
	private final static String RESULT_FAILED = "FAILED";

	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;

	//注入供应商业务接口
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;

	@Autowired
	private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

	@Autowired
	private IOrdOrderItemService ordOrderItemService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private IOrdOrderService iOrdOrderService;

	@Autowired
	private ProdProductClientService prodProductClientService;

	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private SuppSettlementEntityClientService suppSettlementEntityClientService;

	@Autowired
	private OrderSettlementService orderSettlementService;

	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;

	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;

	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterService;

	@Autowired
	private SuppContractClientService suppContractClientService;

	@Autowired
	private ApportionInfoQueryService apportionInfoQueryService;

	@Autowired
	private ApiSuppOrderService apiSuppOrderService;

	@Autowired
	private OrdPassCodeDao ordPassCodeDao;

	/**
	 * 修补子订单中 结算对象CODE为空或者错误 数据
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/modifyOrderItemEntityCode")
	@ResponseBody
	public Object modifyOrderItemEntityCode(Long orderItemId) throws BusinessException{
		JSONObject obj = new JSONObject();
		String msg = " [ modifyOrderItemEntityCode ] ";
		if(null==orderItemId){
			msg += " orderItemId is null , please confirm you have set orderItemId value!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		if(null == ordOrderItem){
			msg += " can not find ordOrderItem by orderItemId("+orderItemId+")!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		// 修复子订单中结算CODE
		Long suppGoodsId = ordOrderItem.getSuppGoodsId();
		ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
		if(!suppGoodsResultHandleT.isSuccess() || suppGoodsResultHandleT.getReturnContent()==null){
			msg += " can not find suppGoods by suppGoodsId("+suppGoodsId+")!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
		String suppGoodsSettleEntityCode = targetSuppGoods.getSettlementEntityCode();
		if(StringUtils.isEmpty(suppGoodsSettleEntityCode)){
			msg += "suppGoods("+suppGoodsId+") have no entity code, please confirm the suppGoods had bind entity code!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(suppGoodsSettleEntityCode);
		if(!resultHandleT.isSuccess() || resultHandleT.getReturnContent()==null){
			msg += "find SuppSettlementEntities by code ("+suppGoodsSettleEntityCode+" ) failed!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		SuppSettlementEntities settlementEntities = resultHandleT.getReturnContent();
		String orderItemEntityCode = settlementEntities.getId()+"_"+settlementEntities.getCode();
		if(StringUtils.isEmpty(orderItemEntityCode) || orderItemEntityCode.split("_").length<2){
			msg += " generate orderItemEntityCode has error! orderItemEntityCode is "+orderItemEntityCode;
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		ordOrderItem.setSettlementEntityCode(orderItemEntityCode);
		int updateRes = orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
		if(updateRes>0){
			msg += " update orderItem entity code success !!! orderItemEntityCode is "+orderItemEntityCode;
			obj.put("result",RESULT_SUCCESS );
			obj.put("msg",msg);
			LOG.info(msg);
		}else{
			msg += " update orderItem entity code failed !!! orderItemEntityCode is "+orderItemEntityCode;
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
		}
		return obj;
	}

	/**
	 * 批量修补子订单中 结算对象CODE为空或者错误 数据(开始时间和结束时间中间的子订单)
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/modifyOrderItemEntityCodeByTime")
	@ResponseBody
	public Object modifyOrderItemEntityCodeByTime(@RequestParam(required=true) String startTime, @RequestParam(required=true) String endTime) throws BusinessException{
		JSONObject obj = new JSONObject();
		//总子订单数量
		int totalNum = 0;
		//更新成功的子订单数量
		int doneNum = 0, unDoneNum = 0;
		int pageSize = 50000;
		int startIndex = 0;
		StringBuilder unDoneItems  = new StringBuilder();
		unDoneItems.append("更新失败的订单id:");
		StringBuilder doneItems  = new StringBuilder();
		doneItems.append("更新成功的订单id:");
		ArrayList<Long> doneArray = new ArrayList<Long>();
		ArrayList<Long> unDoneArray = new ArrayList<Long>();

		StringBuilder noSettlementPriceItems  = new StringBuilder();
		noSettlementPriceItems.append("未绑定结算对象的订单id:");
		ArrayList<Long> noSettlementPriceArray = new ArrayList<Long>();

		String msg = " [ modifyOrderItemEntityCodeByTime ] ";
		LOG.info(msg + "开始了");
		Long d1 = new Date().getTime();
		try {
			if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime) ) {
				return buildErrorResultJSONObject(" startTime或者endTime为空", 0, 0, 0, unDoneItems, doneItems,noSettlementPriceItems, d1);
			}

			Date startTimeDate = null;
			Date endTimeDate = null;

			try {
				startTimeDate = DateUtil.getDateByStr(startTime, DateUtil.SIMPLE_DATE_FORMAT);
				endTimeDate = DateUtil.getDateByStr(endTime, DateUtil.SIMPLE_DATE_FORMAT);
			} catch (Exception e) {
				LOG.error("格式化时间入参出错", e);
				return buildErrorResultJSONObject("格式化时间入参出错", 0, 0, 0, unDoneItems, doneItems,noSettlementPriceItems, d1);
			} finally {
				if (startTimeDate == null || endTimeDate == null) {
					String errorStr = "格式化时间入参出错startTimeDate=" + startTimeDate + "endTimeDate=" + endTimeDate;
					return buildErrorResultJSONObject(errorStr, 0, 0, 0, unDoneItems, doneItems,noSettlementPriceItems, d1);
				}
			}

			if (startTimeDate.before(DateUtil.toDate("2018-04-01", DateUtil.SIMPLE_DATE_FORMAT))) {
				String errorStr = "开始时间入参出错startTimeDate=" + startTimeDate + "不能在2018-04-01之前";
				return buildErrorResultJSONObject(errorStr, 0, 0, 0, unDoneItems, doneItems,noSettlementPriceItems, d1);
			}


			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("startTime", DateUtil.getFormatDate(startTimeDate, DateUtil.SIMPLE_DATE_FORMAT));
			paramMap.put("endTime", DateUtil.getFormatDate(endTimeDate, DateUtil.SIMPLE_DATE_FORMAT));

			while(true) {
				paramMap.put("_start", startIndex * pageSize + 1);
				paramMap.put("_end", (startIndex + 1) * pageSize);
				LOG.info("查询条件:" + JSONObject.toJSONString(paramMap));
				List<OrdOrderItem> orderItems = ordOrderItemService.queryOrderItemListByCreateTimeBetween(paramMap);
				if (CollectionUtils.isEmpty(orderItems)) {
					msg += "子单遍历完毕,startIndex="+startIndex;
					obj.put("unDoneItems",unDoneItems.append(ArrayUtils.toString(unDoneArray)).toString());
					obj.put("doneItems", doneItems.append(ArrayUtils.toString(doneArray)).toString());
					obj.put("noSettlementPriceItems", noSettlementPriceItems.append(ArrayUtils.toString(noSettlementPriceArray)).toString());
					obj.put("noSettlementPriceItemsNum", noSettlementPriceArray.size());
					obj.put("totalNum",totalNum);
					obj.put("doneNum",doneNum);
					obj.put("unDoneNum",unDoneNum);
					obj.put("msg", msg);
					obj.put("result",RESULT_SUCCESS );
					obj.put("time", String.valueOf(System.currentTimeMillis() - d1));
					return obj;
				}
				//对当前页累加
				startIndex++;

				totalNum += orderItems.size();

				for (OrdOrderItem orderItem : orderItems) {

					//如果大字段中存在结算code和买断code中随意一个，则继续循环
					if (StringUtils.isNotEmpty(orderItem.getContentStringByKey(Snapshot_Detail_Enum.SUPPGOODS_KEY.settlementCode.name()))
							|| StringUtils.isNotEmpty(orderItem.getContentStringByKey(Snapshot_Detail_Enum.SUPPGOODS_KEY.buyoutSettlementCode.name()))) {
//						LOG.info("子订单=" + orderItem.getOrderItemId() + "已修复过");
						continue;
					}
					Long suppGoodsId = orderItem.getSuppGoodsId();

					if (suppGoodsId == null) {
						continue;
					}

					//根据商品id查询商品信息
					ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId);
					//如果失败了，则结束
					if (suppGoodsResultHandleT.isFail()) {
						String errorMsg = "can not find suppGoods by suppGoodsId(" + suppGoodsId + ")!";
						LOG.info("查询商品信息失败！" + errorMsg + "\nsuppGoodsResultHandleT.getMsg="+suppGoodsResultHandleT.getMsg()+"\nsuppGoodsResultHandleT.getInfoMsg()="+suppGoodsResultHandleT.getInfoMsg());
						continue;
					}
					//如果成功了，但是没返回商品信息，则继续循环
					if (suppGoodsResultHandleT.isSuccess() && suppGoodsResultHandleT.getReturnContent() == null) {
						LOG.info("没有返回商品信息子单号=" + orderItem.getOrderItemId());
						continue;
					}

					SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
					//更新子订单的content字段
					OrdOrderItem newOrdOrderItem = new OrdOrderItem();
					newOrdOrderItem.setOrderItemId(orderItem.getOrderItemId());
					orderItem.putContent(Snapshot_Detail_Enum.SUPPGOODS_KEY.settlementCode.name(), targetSuppGoods.getSettlementEntityCode());
					orderItem.putContent(Snapshot_Detail_Enum.SUPPGOODS_KEY.buyoutSettlementCode.name(), targetSuppGoods.getBuyoutSettlementEntityCode());
					if (StringUtils.isEmpty(targetSuppGoods.getSettlementEntityCode()) && StringUtils.isEmpty(targetSuppGoods.getBuyoutSettlementEntityCode())) {
						if (!noSettlementPriceArray.contains(orderItem.getOrderId())) {
							noSettlementPriceArray.add(orderItem.getOrderId());
						}
						continue;
					}
					newOrdOrderItem.setContent(orderItem.getContent());
					LOG.info("子订单=" + orderItem.getOrderItemId() + "suppGoodsId=" + suppGoodsId);

					//选择更新子订单中的content大字段
					int result = ordOrderItemService.updateByPrimaryKeySelective(newOrdOrderItem);
					if (result == 1) {
						if (!doneArray.contains(orderItem.getOrderId())) {
							doneArray.add(orderItem.getOrderId());
						}
						doneNum++;
						LOG.info("结算code和买断code更新成功！子订单id=" + orderItem.getOrderItemId() + "\n订单id=" + orderItem.getOrderId() + "\n大字段更新前的值=" + orderItem.getContent() +
								"\n更新后的值=" + newOrdOrderItem.getContent());
					} else {
						if (!unDoneArray.contains(orderItem.getOrderId())) {
							unDoneArray.add(orderItem.getOrderId());
						}
						unDoneNum++;
						LOG.info("结算code和买断code更新失败！子订单id=" + orderItem.getOrderItemId() + "\n订单id=" + orderItem.getOrderId() + "\n大字段更新前的值=" + orderItem.getContent() +
								"\n更新后的值=" + newOrdOrderItem.getContent());
					}
				}
			}
		}catch (Exception e){
			msg += "发生异常" + e.getMessage();
			LOG.error(msg, e);
		}

		obj.put("unDoItems",unDoneItems.append(ArrayUtils.toString(unDoneArray)).toString());
		obj.put("doneItems", doneItems.append(ArrayUtils.toString(doneArray)).toString());
		obj.put("noSettlementPriceItems", noSettlementPriceItems.append(ArrayUtils.toString(noSettlementPriceArray)).toString());
		obj.put("noSettlementPriceItemsNum", noSettlementPriceArray.size());
		obj.put("totalNum",totalNum);
		obj.put("doneNum",doneNum);
		obj.put("unDoneNum",unDoneNum);
		obj.put("msg", msg);
		obj.put("time", String.valueOf(System.currentTimeMillis() - d1));
		obj.put("result",RESULT_FAILED );

		return obj.toString();
	}

	/**
	 * 修复订单的子订单大字段内容中 结算对象CODE为空数据
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value="/fixOrderItemEntityCode", produces="application/json; charset=utf-8")
	@ResponseBody
	public Object fixOrderItemEntityCode(@RequestParam(required=true) String orderId) throws BusinessException{
		JSONObject obj = new JSONObject();

		StringBuilder unDoneItems  = new StringBuilder();
		unDoneItems.append("更新失败的子订单id:");
		StringBuilder itemsNums  = new StringBuilder();
		itemsNums.append("子订单id:");
		StringBuilder doneitems = new StringBuilder();
		doneitems.append("更新成功的子订单id:");
		StringBuilder noSettlementPriceItems  = new StringBuilder();
		noSettlementPriceItems.append("未绑定结算对象的子订单id:");
		ArrayList<Long> noSettlementPriceArray = new ArrayList<Long>();//商品没有结算对象的集合（存子订单）

		int totalNum = 0;
		boolean isSuccess = true;//返回结果是否成功
		ArrayList<Long> doneArray = new ArrayList<Long>();
		ArrayList<Long> unDoneArray = new ArrayList<Long>();
//		ArrayList<OrdOrderItem> aleadyDoneArray = new ArrayList<OrdOrderItem>();//已经修复过的子订单


		String msg = " [ fixOrderItemEntityCode ] ";
		LOG.info(msg + "开始了");
		Long d1 = new Date().getTime();
		try {
			if (StringUtils.isEmpty(orderId) ) {
				return buildErrorResultJSONObjectForFixOrderItemEntityCodeFun("orderId为空",totalNum, d1);
			}

			Long orderIdLong = Long.parseLong(orderId);

			List<OrdOrderItem> orderItems = ordOrderItemService.selectByOrderId(orderIdLong);
			if (CollectionUtils.isEmpty(orderItems)) {
				LOG.info("根据订单id=" + orderIdLong + "未查到子订单");
				return buildErrorResultJSONObjectForFixOrderItemEntityCodeFun(orderIdLong + "未查到子订单", totalNum, d1);
			}
			totalNum = orderItems.size();

			for (OrdOrderItem orderItem : orderItems) {
				itemsNums.append(orderItem.getOrderItemId()+",");

				//如果大字段中存在结算code和买断code中随意一个，则继续循环
//				if (StringUtils.isNotEmpty(orderItem.getContentStringByKey(Snapshot_Detail_Enum.SUPPGOODS_KEY.settlementCode.name()))
//						|| StringUtils.isNotEmpty(orderItem.getContentStringByKey(Snapshot_Detail_Enum.SUPPGOODS_KEY.buyoutSettlementCode.name()))) {
//					aleadyDoneArray.add(orderItem);
//					continue;
//				}
				Long suppGoodsId = orderItem.getSuppGoodsId();

				if (suppGoodsId == null) {
					isSuccess = false;
					continue;
				}

				//根据商品id查询商品信息
				ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId);
				//如果失败了
				if (suppGoodsResultHandleT.isFail()) {
					String errorMsg = "can not find suppGoods by suppGoodsId(" + suppGoodsId + ")!";
					LOG.info("查询商品信息失败！" + errorMsg + "\nsuppGoodsResultHandleT.getMsg="+suppGoodsResultHandleT.getMsg()+"\nsuppGoodsResultHandleT.getInfoMsg()="+suppGoodsResultHandleT.getInfoMsg());
					isSuccess = false;
					continue;
				}
				//如果成功了，但是没返回商品信息，则继续循环
				if (suppGoodsResultHandleT.isSuccess() && suppGoodsResultHandleT.getReturnContent() == null) {
					LOG.info("没有返回商品信息子单号=" + orderItem.getOrderItemId());
					isSuccess = false;
					continue;
				}

				SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
				//更新子订单的content字段
				OrdOrderItem newOrdOrderItem = new OrdOrderItem();
				newOrdOrderItem.setOrderItemId(orderItem.getOrderItemId());
				orderItem.putContent(Snapshot_Detail_Enum.SUPPGOODS_KEY.settlementCode.name(), targetSuppGoods.getSettlementEntityCode());
				orderItem.putContent(Snapshot_Detail_Enum.SUPPGOODS_KEY.buyoutSettlementCode.name(), targetSuppGoods.getBuyoutSettlementEntityCode());
				if (StringUtils.isEmpty(targetSuppGoods.getSettlementEntityCode()) && StringUtils.isEmpty(targetSuppGoods.getBuyoutSettlementEntityCode())) {
					if (!noSettlementPriceArray.contains(orderItem.getOrderItemId())) {
						noSettlementPriceArray.add(orderItem.getOrderItemId());
						isSuccess = false;
					}
					continue;
				}
				newOrdOrderItem.setContent(orderItem.getContent());
				LOG.info("子订单=" + orderItem.getOrderItemId() + "suppGoodsId=" + suppGoodsId);

				//选择更新子订单中的content大字段
				int result = ordOrderItemService.updateByPrimaryKeySelective(newOrdOrderItem);
				if (result == 1) {
					if (!doneArray.contains(orderItem.getOrderItemId())) {
						doneArray.add(orderItem.getOrderItemId());
					}
					LOG.info("结算code和买断code更新成功！子订单id=" + orderItem.getOrderItemId() + "\n订单id=" + orderItem.getOrderId() + "\n大字段更新前的值=" + orderItem.getContent() +
							"\n更新后的值=" + newOrdOrderItem.getContent());
				} else {
					if (!unDoneArray.contains(orderItem.getOrderItemId())) {
						unDoneArray.add(orderItem.getOrderItemId());
					}
					LOG.info("结算code和买断code更新失败！子订单id=" + orderItem.getOrderItemId() + "\n订单id=" + orderItem.getOrderId() + "\n大字段更新前的值=" + orderItem.getContent() +
							"\n更新后的值=" + newOrdOrderItem.getContent());
					isSuccess = false;
				}
			}
//			String aleadyFix = "订单id=" + orderIdLong + "已修复过！";
//			if (aleadyDoneArray.size() == totalNum && isSuccess) {
//				msg += aleadyFix;
//			}
		}catch (Exception e){
			msg += "发生异常" + e.getMessage();
			LOG.error(msg, e);
			isSuccess = false;
		}

		obj.put("unDoItems",unDoneItems.append(ArrayUtils.toString(unDoneArray)).toString());
		obj.put("doneItems", doneitems.append(ArrayUtils.toString(doneArray)).toString());
		obj.put("noSettlementPriceItems", noSettlementPriceItems.append(ArrayUtils.toString(noSettlementPriceArray)).toString());
		obj.put("noSettlementPriceItemsNum", noSettlementPriceArray.size());
		obj.put("totalNum",totalNum);
		obj.put("doneNum",doneArray.size());
		obj.put("unDoneNum", totalNum - doneArray.size());
		obj.put("msg", msg);
		obj.put("time", String.valueOf(System.currentTimeMillis() - d1));
		if (isSuccess) {
			obj.put("result", RESULT_SUCCESS);
		}else{
			obj.put("result", RESULT_FAILED);
		}

		return obj.toString();
	}



	/**
	 * 构建错误返回对象
	 *
	 * @param errorStr
	 * @param doneNum
	 * @param totalNum
	 * @param unDoneItems
	 * @param doneItems
	 * @param d1
	 * @return
	 */
	private JSONObject buildErrorResultJSONObject(String errorStr, int doneNum, int unDoneNum, int totalNum, StringBuilder unDoneItems, StringBuilder doneItems, StringBuilder noSettlementPriceItems, Long d1) {
		JSONObject obj = new JSONObject();
		String msg = " [ modifyOrderItemEntityCodeByTime ] ";
		msg += errorStr;
		obj.put("result", RESULT_FAILED);
		obj.put("msg", msg);
		obj.put("doneNum", doneNum);
		obj.put("unDoneNum", unDoneNum);
		obj.put("totalNum", totalNum);
		obj.put("unDoneItems", unDoneItems.toString());
		obj.put("doneItems", doneItems.toString());
		obj.put("noSettlementPriceItems", noSettlementPriceItems.toString());
		obj.put("noSettlementPriceItemsNum", 0);
		obj.put("time", String.valueOf(System.currentTimeMillis() - d1));
		LOG.error(msg);
		return obj;
	}

	/**
	 * 构建错误返回对象,给fixOrderItemEntityCode方法使用
	 * @return
	 */
	private JSONObject buildErrorResultJSONObjectForFixOrderItemEntityCodeFun(String errorStr,int totalNum, Long d1) {
		JSONObject obj = new JSONObject();
		String msg = " [ fixOrderItemEntityCode ] ";
		msg += errorStr;
		obj.put("result", RESULT_FAILED);
		obj.put("msg", msg);
		obj.put("totalNum", totalNum);
		obj.put("time", String.valueOf(System.currentTimeMillis() - d1));
		LOG.error(msg);
		return obj;
	}



	/**
	 * 向财务手动推送子订单信息
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/sendOrderItemInfoToFinance")
	@ResponseBody
	public JSONObject sendOrderItemInfoToFinance(Long orderItemId,String printJson,Long orderId) throws BusinessException{
		JSONObject obj = new JSONObject();
		String msg = " [ sendOrderItemInfoToFinance ] ";
		if(null == orderId){
			if(null==orderItemId){
				msg += " orderItemId is null , please confirm you have set orderItemId value!";
				LOG.error(msg);
				obj.put("result",RESULT_FAILED );
				obj.put("msg",msg);
				return obj;
			}

			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
			if(null == ordOrderItem){
				msg += " can not find ordOrderItem by orderItemId("+orderItemId+")!";
				LOG.error(msg);
				obj.put("result",RESULT_FAILED );
				obj.put("msg",msg);
				return obj;
			}

			orderId = ordOrderItem.getOrderId();
		}

		OrdOrder orderObj = getOrderWithOrderItemByOrderId(orderId);
		if (orderObj != null) {
			msg += " orderId =" + orderObj.getOrderId() + ",hasInfoAndResourcePass=" + orderObj.hasInfoAndResourcePass() + ",hasPayed=" + orderObj.hasPayed();
			LOG.info(msg);
		}

		if (!orderObj.isNeedSettlement()) {
			msg += " only prepay order need enter settle!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
			return obj;
		}

		// 必须确保订单已支付 且 资源信息审核均通过
		if (orderObj.hasInfoAndResourcePass() && orderObj.hasPayed()) {
			try {
				List<SettlementItem> settlementItemList = this.fillSettlementItemListForAction(orderObj);
				LOG.info(" [ sendOrderItemInfoToFinance ] save settlement info start!");
				for (SettlementItem settlementItem : settlementItemList) {
					List<SetSettlementItem> setSettleList = orderSettlementService.findSetSettlementItemByParams(orderId, settlementItem.getOrderItemProdId());
					//状态   订单支付成功和订单取消后 分别是正常和取消
					if(orderObj.isCancel()){
						settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.CANCEL.name());
					}else{
						settlementItem.setStatus(Constant.SET_SETTLEMENT_ITEM_STATUS.NORMAL.name());
					}
					orderSettlementService.saveSettlementItem(settlementItem);
					msg += " orderSettlementService saveSettlementItem success!";
					if(StringUtils.isNotEmpty(printJson) && printJson.equalsIgnoreCase("true")){
						msg += "settlement("+settlementItem.getOrderItemMetaId()+") detail info is :"+JSONObject.toJSONString(settlementItem);
					}
				}
				obj.put("result",RESULT_SUCCESS );
				obj.put("msg",msg);
				LOG.info("[ sendOrderItemInfoToFinance ] save settlement info end!");
			}catch (Exception e){
				msg += " fillSettlementItemListForAction may has exception! msg is "+e.getMessage();
				obj.put("result",RESULT_FAILED );
				obj.put("msg",msg);
				LOG.error(msg);
			}

		}else{
			msg += " order may not yet payed or infoAndResource not yet pass!";
			obj.put("result",RESULT_FAILED );
			obj.put("msg",msg);
			LOG.error(msg);
		}
		return obj;
	}


	/**
	 * 填充数据SettlementItem
	 *
	 * @param order
	 * @return
	 */
	private List<SettlementItem> fillSettlementItemListForAction(OrdOrder order) {
		LOG.info(" [ OrderSettlementEntityAction ] fillSettlementItemListForAction start ");
		List<SettlementItem> settlementItemList = new ArrayList<>();

		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		Date visitTime=null;
		Long productId=null;
		Long countSettleAmount = 0L;//整个订单结算总额（分）
		Long orderTotalSettlementPrice = 0L;
		for (OrdOrderItem ordOrderItem : orderItemList) {
			countSettleAmount += ordOrderItem.getTotalSettlementPrice();
			orderTotalSettlementPrice += ordOrderItem.getQuantity() * ordOrderItem.getSettlementPrice();
		}

		HashMap<String, Object> amountItemParams = new HashMap<>();
		amountItemParams.put("orderId", order.getOrderId());
		amountItemParams.put("itemName", OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.name());
		List<OrdOrderAmountItem> list = orderAmountChangeService.findOrderAmountItemList(amountItemParams);
		Long orderCouponAmount = 0L;
		if (list != null && list.size() > 0) {
			for (OrdOrderAmountItem ordOrderAmountItem : list) {
				orderCouponAmount = -ordOrderAmountItem.getItemAmount() + orderCouponAmount;
			}
		}

		//前台下单存储了优惠券使用的新的ORDER_AMOUNT_NAME 增加此处代码  by  李志强 2015-09-28
		HashMap<String, Object> amountItemParamsForNew = new HashMap<>();
		amountItemParamsForNew.put("orderId", order.getOrderId());
		amountItemParamsForNew.put("itemName", OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_COUPON.name());
		List<OrdOrderAmountItem> listOrderAmount = orderAmountChangeService.findOrderAmountItemList(amountItemParamsForNew);

		if (listOrderAmount != null && listOrderAmount.size() > 0) {
			for (OrdOrderAmountItem ordOrderAmountItem : listOrderAmount) {
				//新的优惠券优惠金额为负整数 单位分
				orderCouponAmount += -ordOrderAmountItem.getItemAmount();
			}
		}
		order.setOrdPersonList(order.getOrdPersonList());
		OrdPerson contactPerson = order.getContactPerson();
		String contactMobileNo = null;
		if(contactPerson!=null){
			contactMobileNo = contactPerson.getMobile();
		}
		LOG.info("[ OrderSettlementEntityAction ] ***手机号***"+contactMobileNo);
		//支付获取结算状态

		List<SetSettlementItem> setSettlementItems =  getSetSettlementItem(orderItemList);

		for (OrdOrderItem orderItem : orderItemList) {
			StringBuffer str=new StringBuffer();
			Long orderItemMetaPayedAmount = 0L;
			if (orderTotalSettlementPrice > 0) {
				//订单销售分拆后的支付金额   订单应付总金额*（当前订单子项单家*数量/所有订单子项数量*单价和）
				double payedAmountPer = orderItem.getQuantity() * orderItem.getSettlementPrice() * 1.0 / orderTotalSettlementPrice * 1.0;
				orderItemMetaPayedAmount = (long) (order.getOughtAmount() * payedAmountPer);
			}

			String branchName = (String) orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
			ResultHandleT<SuppGoods> suppGoodsHandle = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
			SuppGoods suppGoods = suppGoodsHandle.getReturnContent();
			PermUser permUser = permUserServiceAdapter.getPermUserByUserId(suppGoods.getManagerId());
			String productManager = "";
			if (permUser != null && StringUtils.isNotEmpty(permUser.getUserName())) {
				productManager = permUser.getUserName();
			}

			SettlementItem setSettlementItem = new SettlementItem();

			//订单出发日期
			if(order != null && order.getVisitTime() != null){
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				visitTime=order.getVisitTime();
				String dateString = formatter.format(visitTime);
				str.append(dateString);
				str.append("-");
			}
			//产品ID
			if(order.getProductId() != null){
				productId = order.getProductId();
				str.append(productId);
			}
			LOG.info("[ OrderSettlementEntityAction ] ----结算团号----"+str.toString()+"----出团日期----"+visitTime+"----productName----:"+order.getProductName());

			setSettlementItem.setContactMobileNo(contactMobileNo);

			setSettlementItem.setOrderId(order.getOrderId());
			setSettlementItem.setTravelGroupCode(str.toString()); // 结算团号
			setSettlementItem.setOutGroupDate(visitTime); //出团日期
			setSettlementItem.setActChild(orderItem.getChildQuantity());
			setSettlementItem.setActAdult(orderItem.getAdultQuantity());
			LOG.info("[ OrderSettlementEntityAction ] processer3>>orderId = "+order.getOrderId()+"orderStatus = "+order.getOrderStatus());
			setSettlementItem.setOrderStatus(order.getOrderStatus());
			setSettlementItem.setOrderPaymentTime(order.getPaymentTime());
			setSettlementItem.setOrderCreateTime(order.getCreateTime());
			setSettlementItem.setOrderPaymentStatus(order.getPaymentStatus());
			setSettlementItem.setDistributorId(order.getDistributorId());
			if (order.getContactPerson() != null && StringUtils.isNotBlank(order.getContactPerson().getFullName())) {
				setSettlementItem.setOrderContactPerson(order.getContactPerson().getFullName());
			} else {
				setSettlementItem.setOrderContactPerson(order.getContactPerson().getMobile());
			}
			// 将游玩人推送给结算系统
			List<OrdPerson> travellerList = order.getOrdTravellerList();
			if (CollectionUtils.isNotEmpty(travellerList)) {
				//产品经理lvhao要求只拿第一个游玩人的名字
				String travellerName = travellerList.get(0).getFullName();
				setSettlementItem.setTravelingPerson(travellerName);
			}

			setSettlementItem.setOrderCouponAmount(orderCouponAmount);//订单的优惠券金额
			setSettlementItem.setOrderRefund(Constants.ORDER_REFUND_FALSE);//是否订单有退款 0.没有 1.有退款
			setSettlementItem.setOrderItemProdId(orderItem.getOrderItemId());//订单子项ID
			setSettlementItem.setProductId(order.getProductId());
			setSettlementItem.setProductName(order.getProductName());
			setSettlementItem.setProductType(orderItem.getCategoryId()+"");//销售产品类型  orderItem.getCategoryId()+""
			setSettlementItem.setProductBranchId(suppGoods.getSuppGoodsId());
			setSettlementItem.setProductBranchName(suppGoods.getGoodsName());
			setSettlementItem.setProductPrice(orderItem.getPrice());
			setSettlementItem.setFilialeName(order.getFilialeName());
			setSettlementItem.setBelongBU(orderItem.getRealBuType());
			setSettlementItem.setBelongMainBU(order.getBuCode());
			setSettlementItem.setMetaFilialeName(order.getFilialeName());
			setSettlementItem.setOrderItemMetaId(orderItem.getOrderItemId());//订单子子项ID
			setSettlementItem.setOrderItemMetaPayedAmount(orderItemMetaPayedAmount);//订单销售分拆后的支付金额
			setSettlementItem.setMetaProductId(orderItem.getProductId());//采购产品ID
			setSettlementItem.setMetaProductName(orderItem.getProductName());//采购产品名称
			setSettlementItem.setMetaBranchId(suppGoods.getSuppGoodsId());//采购产品分类ID  BRANCH_ID

			setSettlementItem.setMetaBranchName(suppGoods.getGoodsName());//采购产品分类名称
			setSettlementItem.setMetaProductManager(productManager);
			setSettlementItem.setSettlementPrice(orderItem.getSettlementPrice());
			setSettlementItem.setSupplierId(orderItem.getSupplierId());

			// 结算子订单，公司主体
			setSettlementItem.setCompanyType(orderItem.getCompanyType() == null ? "XINGLV" : orderItem.getCompanyType());
			setSettlementItem.setCompanyId(orderItem.getCompanyType() == null ? "XINGLV" : orderItem.getCompanyType());

			// 保存结算对象ID, 一供多结项目
			saveSettleEntityCode(setSettlementItem,orderItem,order);

			// 保存分摊信息
			saveApportionInfo(setSettlementItem,orderItem,order);
			setSettlementItem.setProductQuantity(1L);//打包数量

			setQuantityValue(orderItem,setSettlementItem);
			setSettlementItem.setVisitTime(order.getVisitTime());
			setSettlementItem.setSettlementStatus(getSettlementStatus(setSettlementItems, orderItem.getOrderItemId()));
			setSettlementItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice());//子项结束总额
			setSettlementItem.setSettlementType(OrderEnum.SETTLEMENT_TYPE.ORDER.getCode());//结算项类别（GROUP  OR  ORDER）
			setSettlementItem.setActualSettlementPrice(orderItem.getActualSettlementPrice());
			setSettlementItem.setBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
			setSettlementItem.setBudgetTotalSettlementlPrice(orderItem.getBuyoutTotalPrice());
			setSettlementItem.setBudgetQuantity(orderItem.getBuyoutQuantity());
			setSettlementItem.setBudgetFlag(orderItem.getBuyoutFlag());
			setSettlementItem.setOughtPay(order.getOughtAmount());//订单应付总额（分）
			setSettlementItem.setActualPay(order.getActualAmount());//订单实付金额（分）
			setSettlementItem.setCountSettleAmount(countSettleAmount);//整个订单结算总额（分）

			setSettlementItem.setBusinessName("NEW_SUPPLIER_BUSINESS");
			//通知类型
			setSettlementItem.setNotifyType(orderItem.getNotifyType());
			//银行立减金额
			setSettlementItem.setPayPromotionAmount(order.getPayPromotionAmount());
			//优惠金额list
			setSettlementItem.setOrdPromotionList(orderItem.getOrdPromotionList());

			//解决通关码推送结算问题
			OrdPassCode passCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItem.getOrderItemId());
			if (passCode != null) {
				setSettlementItem.setPassCode(passCode.getCode());// 通关码
				setSettlementItem.setPassAddCode(passCode.getAddCode());//辅助码
				setSettlementItem.setPassSerialno(passCode.getPassSerialno());//通关流水号
				setSettlementItem.setPassExtid(passCode.getPassExtid());//供应商回调信息
			}
			settlementItemList.add(setSettlementItem);

		}

		return settlementItemList;
	}

	/**
	 * 设置 Quantity值
	 */
	private void setQuantityValue(OrdOrderItem orderItem, SettlementItem item ){
		Long quantity = orderItem.getQuantity();
		LOG.info(Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId() + "--" + orderItem.getCategoryId());
		if (Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId().equals(String.valueOf(orderItem.getCategoryId()))) {
			Map<String, Object> maps = new HashMap<String, Object>();
			maps.put("orderItemId", orderItem.getOrderItemId());
			List<OrdOrderHotelTimeRate> lists = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateListByParams(maps);
			if (lists != null && lists.size() > 0) {
				item.setNightNum(lists.size());
			}
		}
		item.setQuantity(quantity);
	}

	public String getSettlementStatus(List<SetSettlementItem> setSettlementItems,Long orderItemId){
		if(null!=setSettlementItems && setSettlementItems.size()>0){
			for(int i=0;i<setSettlementItems.size();i++){
				if(setSettlementItems.get(i).getOrderItemMetaId().equals(orderItemId)){
					return setSettlementItems.get(i).getSettlementStatus();
				}
			}
		}
		return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}

	//批量获取结算状态
	public List<SetSettlementItem> getSetSettlementItem(List<OrdOrderItem> orderItemList){
		List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
		try {
			List<Long> itemIds = new ArrayList<Long>();
			for (OrdOrderItem ordOrderItem : orderItemList) {
				itemIds.add(ordOrderItem.getOrderItemId());
			}
			setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
		} catch (Exception e) {
			throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
		}
		return setSettlementItems;
	}

	/**
	 * 根据OrderId返回单个用订单那子项Order对象
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		LOG.info(" [ OrderSettlementEntityAction ] find order by orderId("+orderId+") start!");
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderPackTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		LOG.info(" [ OrderSettlementEntityAction ] find order by orderId("+orderId+") end!");
		return order;
	}

	/**
	 * 保存结算对象信息
	 * @param item
	 * @param orderItem
	 */
	private void saveSettleEntityCode(SettlementItem item,OrdOrderItem orderItem,OrdOrder order){
		// 从子订单对象中 获取 结算对象CODE
		try{
			String settleEntityCodeValue = orderItem.getSettlementEntityCode();
			SuppSettlementEntities settlementEntities = null;
			if(!StringUtils.isEmpty(settleEntityCodeValue)){
				// 子订单中 结算对象ID_CODE 已绑定
				String[] arr = settleEntityCodeValue.split("_");
				if(arr.length!=2){
					throw new BusinessException(" [ OrderSettlementEntityAction ],Error msg : settleEntityCodeValue is "+settleEntityCodeValue+" , is not ID_CODE formate!!! ");
				}

				Long settleEntityID = Long.parseLong(arr[0]);
				String settleEntityCode = arr[1];
				ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCodeAndId(settleEntityID,settleEntityCode);
				if(!resultHandleT.isSuccess()){
					throw new BusinessException(" [ OrderSettlementEntityAction ] ,Error msg : can not find settleEntity by code ("+settleEntityCode+") and ID("+settleEntityID+")");
				}
				settlementEntities = resultHandleT.getReturnContent();
			}else{
				// 子订单中 结算对象ID_CODE 未绑定
				LOG.info(" [ OrderSettlementEntityAction ]  : settleEntityCodeValue is null in orderItem("+orderItem.getOrderItemId()+")");
				Long suppGoodsId = orderItem.getSuppGoodsId();
				String suppGoodsSettleEntityCode = null;
				ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
				if(suppGoodsResultHandleT.isSuccess() && suppGoodsResultHandleT.getReturnContent()!=null){
					SuppGoods targetSuppGoods = suppGoodsResultHandleT.getReturnContent();
					suppGoodsSettleEntityCode = targetSuppGoods.getSettlementEntityCode();
				}else{
					throw new BusinessException(" [ OrderSettlementEntityAction ]  , ERROR : suppGoods("+suppGoodsId+") has no settlement entity code!!!");
				}

				ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(suppGoodsSettleEntityCode);
				if(!resultHandleT.isSuccess()){
					throw new BusinessException(" [ OrderSettlementEntityAction ]  , ERROR : find SuppSettlementEntities by code ("+suppGoodsSettleEntityCode+" ) failed");
				}

				settlementEntities = resultHandleT.getReturnContent();
				if(null!=settlementEntities){
					String orderItemCode = settlementEntities.getId()+"_"+settlementEntities.getCode();
					if(StringUtils.isNotEmpty(orderItemCode) && orderItemCode.split("_").length>1){
						orderItem.setSettlementEntityCode(orderItemCode);
						ordOrderItemService.updateOrdOrderItem(orderItem);
					}
				}

			}

			if(null==settlementEntities){
				throw new BusinessException(" [ OrderSettlementEntityAction ] ,Error msg : settlementEntities is null !!!");
			}

			LOG.info(" [ OrderSettlementEntityAction ] ***************************************************************  ");
			LOG.info(" [ OrderSettlementEntityAction ] ***************  saveSettleEntityCode  start  *****************  ");
			LOG.info(" [ OrderSettlementEntityAction ] ***************************************************************  ");

			LOG.info(" [ OrderSettlementEntityAction ] settlementEntities json String is "+ JSON.toJSONString(settlementEntities));
			item.setTargetId(settlementEntities.getId());

			// 保存结算对象各属性信息
			item.setSettleEntityId(settlementEntities.getId());
			item.setSuppSettleRuleId(settlementEntities.getSuppSettleRuleId());
			saveSupplierInfo(orderItem,item); // 保存 供应商信息
			item.setName(settlementEntities.getName());
			item.setTargetName(settlementEntities.getName());
			item.setCode(settlementEntities.getCode());
			item.setAccountName(settlementEntities.getAccountName());
			item.setBankName(settlementEntities.getBankName());
			item.setBankAccountNo(settlementEntities.getBankAccountNo());
			item.setSettlementClasification(settlementEntities.getSettlementClasification());
			item.setSettleCycle(settlementEntities.getSettleCycle());
			item.setSettlementMethods(settlementEntities.getSettlementMethods());
			item.setEbkNo(settlementEntities.getEbkNo());
//            item.setSettlementStatus(settlementEntities.getStatus()); // 无须推送结算对象状态
			item.setEffectedDate(settlementEntities.getEffectedDate());
			item.setExpiryDate(settlementEntities.getExpiryDate());
			item.setFareClearingTime(settlementEntities.getFareClearingTime());

			// 结算周期
			item.setSettlementPeriod(settlementEntities.getSettlementClasification());
			item.setSuppSettlementEntities(settlementEntities);

			// 价格确认状态
			item.setPriceConfirmStatus(orderItem.getPriceConfirmStatus());

			// SBU 需求
			setSbuInfo(item,orderItem,order);

			LOG.info(" [ OrderSettlementEntityAction ] ***************************************************************  ");
			LOG.info(" [ OrderSettlementEntityAction ] ***************  saveSettleEntityCode  end  *****************  ");
			LOG.info(" [ OrderSettlementEntityAction ] ***************************************************************  ");
		}catch (Exception e){
			LOG.error(e.getMessage());
		}
	}

	/**
	 * SBU 需求
	 * @param item
	 * @param orderItem
	 */
	private void setSbuInfo(SettlementItem item,OrdOrderItem orderItem,OrdOrder order){

		ProdProduct curProduct = findProductByOrderItem(orderItem);
		Long subCategoryId = curProduct.getSubCategoryId();
		if(null==subCategoryId){
			subCategoryId = curProduct.getBizCategoryId();
			LOG.info(" [ OrderSettlementEntityAction.setSbuInfo ] : order id is ("+order.getOrderId()+") , order item id is ("+orderItem.getOrderItemId()+"), productSubType is "+subCategoryId);
		}
		item.setProductSubType(String.valueOf(subCategoryId)); // 三级品类(没有就是二级品类，再没有就是一级)

		// 主订单对应的品类ID
		Long orderCategoryId = order.getCategoryId();
		if(orderCategoryId!=null){
			item.setMainProductSubType(String.valueOf(orderCategoryId));
			LOG.info(" [ OrderSettlementEntityAction.setSbuInfo ] : main order id is ("+order.getOrderId()+") , mainProductSubType is "+orderCategoryId);
			return;
		}

		// 如果订单中 品类ID 为空,再次查询库中是否 含有品类ID
		Long orderId = order.getOrderId();
		OrdOrder ordOrder = iOrdOrderService.findByOrderId(orderId);
		if(ordOrder ==null){
			LOG.error(" [ OrderSettlementEntityAction.setSbuInfo ] : can not find order by orderId ("+orderId+") !!! ");
			return;
		}

		if(ordOrder.getCategoryId()==null){
			LOG.error(" [ OrderSettlementEntityAction.setSbuInfo ] : order has no category ID ( order id "+orderId+") !!! ");
			return;
		}else{
			item.setMainProductSubType(String.valueOf(ordOrder.getCategoryId()));
			LOG.info(" [ OrderSettlementEntityAction.setSbuInfo ] :new main order id is ("+ordOrder.getOrderId()+") , mainProductSubType is "+ordOrder.getCategoryId());
			return;
		}
	}

	private ProdProduct findProductByOrderItem(OrdOrderItem orderItem){
		Long productID = orderItem.getProductId();
		if(null==productID){
			throw new BusinessException(" [ OrderSettlementEntityAction.findProductByOrderItem ], Error msg : orderItem( "+orderItem.getOrderItemId()+" ) productId is null ");
		}

		ResultHandleT<ProdProduct> curOrderProductRes = prodProductClientService.findProdProductById(productID);
		if(!curOrderProductRes.isSuccess()){
			throw new BusinessException(" [ OrderSettlementEntityAction.findProductByOrderItem ], Error msg 1 : prodProductClientService.findProdProductById("+productID+") failed ");
		}

		ProdProduct curProduct = curOrderProductRes.getReturnContent();
		if(null==curProduct){
			throw new BusinessException(" [ OrderSettlementEntityAction.findProductByOrderItem ],Error msg : curProduct is null !!!");
		}
		return curProduct;
	}

	/**
	 * 保存 供应商信息
	 * @param orderItem
	 * @return
	 */
	private void saveSupplierInfo(OrdOrderItem orderItem,SettlementItem item){

		if(null == orderItem ){
			LOG.error("[ OrderSettlementEntityAction.saveSupplierInfo ]: orderItem is null!");
			return;
		}

		Long supplierId = orderItem.getSupplierId();
		if(null!=supplierId){
			item.setSupplierId(supplierId);
			ResultHandleT<SuppSupplier> resultHandleT = suppSupplierClientService.findSuppSupplierById(supplierId);
			if(resultHandleT.isSuccess()&& resultHandleT.getReturnContent()!=null){
				SuppSupplier suppSupplier = resultHandleT.getReturnContent();
				item.setSupplierName(suppSupplier.getSupplierName());
			}
		}else{
			LOG.error("[ OrderSettlementEntityAction.saveSupplierInfo ]: orderItem("+orderItem.getOrderItemId()+") supplierId is null!");
		}

		Long contractId = orderItem.getContractId();
		if(null!=contractId){
			item.setContractId(contractId);
		}else {
			LOG.error("[ OrderSettlementEntityAction.saveSupplierInfo ]: orderItem("+orderItem.getOrderItemId()+") contractId is null!" );
		}
	}

	/**
	 *  保存分摊信息
	 */
	private void saveApportionInfo(SettlementItem item,OrdOrderItem orderItem,OrdOrder order){

		LOG.info(" ********* OrderSettlementEntityAction.saveApportionInfo start,orderItemId:"+orderItem.getOrderItemId()+" ************* ");
		try{
			item.setApportionMessage(false); // 表示非分摊
			item.setPureApportion(false);    // 表示非纯分摊
			ResultHandleT<SuppGoods> resultSuppGoods = suppGoodsHotelAdapterService.findSuppGoodsById(orderItem.getSuppGoodsId());
			if(resultSuppGoods.isSuccess() && null != resultSuppGoods.getReturnContent()){
				Long contractId =resultSuppGoods.getReturnContent().getContractId();
				ResultHandleT<SuppContract> suppContractHandle = suppContractClientService.findSuppContractByContractId(contractId);
				if(null != suppContractHandle && null != suppContractHandle.getReturnContent()){
					//合同编号
					item.setContractCode(suppContractHandle.getReturnContent().getContractNo());
				}
			}else{
				LOG.error("OrderSettlementEntityAction.saveApportionInfo, Can not find suppgoods by id("+orderItem.getSuppGoodsId()+")");
			}

			item.setOrderIsTermBill((byte) 0);
			Map<String, Object> contentMap = orderItem.getContentMap();
			if(null != contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())){
				//是否期票
				String aperiodicFlag =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name());
				if(org.apache.commons.lang3.StringUtils.isNotEmpty(aperiodicFlag) && "Y".equalsIgnoreCase(aperiodicFlag)){
					item.setOrderIsTermBill((byte) 1);
				}
			}

			//酒店取间夜数
			if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
				Map<String, Object> params = new HashMap<>();
				params.put("orderItemId", orderItem.getOrderItemId());
				List<OrdOrderHotelTimeRate> hotelTimeRates = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
				if(CollectionUtils.isNotEmpty(hotelTimeRates)){
					int hotelTimeRatesSize = hotelTimeRates.size();
					if(null!=orderItem.getQuantity()){
						hotelTimeRatesSize = hotelTimeRatesSize*Integer.parseInt(String.valueOf(orderItem.getQuantity()));
					}
					item.setOrderItemRoomNight(hotelTimeRatesSize);
				}else{
					LOG.info(" OrderSettlementEntityAction, Find OrdOrderHotelTimeRate list by(orderItemId"+orderItem.getOrderItemId()+") size is 0!");
				}
			}

			//调分摊接口
			OrderItemApportionInfoQueryVO orderApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
			orderApportionInfoQueryVO.setOrderId(order.getOrderId());
			orderApportionInfoQueryVO.setOrderItemId(orderItem.getOrderItemId());
			long startTime = System.currentTimeMillis();
			LOG.info(" Calc orderItem apportion info start");
			OrderItemApportionInfoPO orderItemApportion = apportionInfoQueryService.calcOrderItemApportionInfo(orderApportionInfoQueryVO);
			long endTime = System.currentTimeMillis();
			LOG.info(" CalcOrderItemApportionInfo method cast "+(endTime-startTime)+" ms");
			//子单优惠总额
			item.setCouponAmount(orderItemApportion.getItemTotalCouponAmount());
			//子单促销金额
			item.setPromotionAmount(orderItemApportion.getItemTotalPromotionAmount());
			//子单手动改价金额
			item.setManualChangeAmount(orderItemApportion.getItemTotalManualChangeAmount());
			//子单实收金额
			item.setOrderItemActualReceived(orderItemApportion.getItemTotalActualPaidAmount());
			//子单退款总额
			item.setItemRefundedAmount(orderItemApportion.getTotalRefundAmount());
			//子单销售金额
			item.setOrdeItemSaleAmount(orderItem.getTotalAmount());
			LOG.info(" ApiSuppOrderService getSuppOrderByOrderItemId(orderItemId:"+orderItem.getOrderItemId()+") start");
			RequestSuppOrder requestSuppOrder = apiSuppOrderService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
			if(null != requestSuppOrder && org.apache.commons.lang3.StringUtils.isNotEmpty(requestSuppOrder.getSuppOrderId())){
				item.setSupplierOrderCode(requestSuppOrder.getSuppOrderId());  //供应商订单号
			}else if(null != requestSuppOrder &&  org.apache.commons.lang3.StringUtils.isEmpty(requestSuppOrder.getSuppOrderId()) ){
				LOG.info("SuppOrder is not null, but suppOrderId is null!(orderItemId:"+orderItem.getOrderItemId()+") ");
			}else{
				LOG.info("SuppOrder is null!(orderItemId:"+orderItem.getOrderItemId()+") ");
			}
		}catch (Exception e){
			LOG.error("SaveApportionInfo method has exception, error msg is "+e.getMessage());
		}

		LOG.info(" ********* OrderSettlementEntityAction.saveApportionInfo end ,orderItemId:"+orderItem.getOrderItemId()+" ************* ");
	}

}
