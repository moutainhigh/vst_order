package com.lvmama.vst.order.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;






import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderRefundBatch;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.OrdRefundInfo;
import com.lvmama.vst.comm.vo.order.OrdRefundItemInfo;
import com.lvmama.vst.comm.vo.order.PartRefundAmountInfo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.PartRefundService;
import com.lvmama.vst.order.service.refund.IOrderRefundCommMethodService;
import com.lvmama.vst.order.service.refund.OrderRefundBatchService;

@Controller
public class PartRefundBackAction extends BaseActionSupport {

	private static final Log LOG = LogFactory
			.getLog(PartRefundBackAction.class);

	@Autowired
	private PartRefundService partRefundService;
	
	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	private OrderRefundBatchService orderRefundBatchService;
	
	@Autowired
	private IOrderRefundCommMethodService orderRefundCommMethodService;

	/**
	 * 跳转到申请退款页面
	 */
	@RequestMapping(value = "/partRefundBackAction/toPartRefundPage")
	public String toPartRefundPage(Long orderId, Model model,
			HttpServletRequest request) {
		// 退款明细
		List<OrderRefundBatch> refundDetailList = orderRefundBatchService
				.getRefundBatchAndSuppGoods(orderId);
		model.addAttribute("refundDetailList", refundDetailList);

		// 退款申请信息
		ResultHandleT<List<OrdRefundItemInfo>> result = partRefundService
				.queryOrdRefundInfo(orderId);
		List<OrdRefundItemInfo> refundItemList = result.getReturnContent();
		model.addAttribute("refundItemList", refundItemList);
		model.addAttribute("orderId", orderId);
		Boolean hasRefundApplyProcessing = partRefundService.hasRefundApplyProcessing(orderId);
		Boolean hasSaleNotClosed = partRefundService.hasSaleNotClosed(orderId);
		model.addAttribute("hasRefundApplyProcessing", hasRefundApplyProcessing||hasSaleNotClosed);
		//是否允许申请
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		model.addAttribute("applyReFundButtonCheck",orderRefundCommMethodService.checkReFundButtonShow(order));
		return "/order/partRefund/ApplyReFund";
	}

	/**
	 * 申请退款操作
	 */
	@RequestMapping(value = "/partRefundBackAction/applyRefundOperate")
	@ResponseBody
	public Object applyRefundOperate(String refundItemInfoJson,
			HttpServletRequest request) {		
		try {
			OrdRefundInfo ordRefundInfo = setOrdRefundInfoObject(
					refundItemInfoJson, request);
			LOG.info("---------申请退款操作: applyRefundOperate:"+ordRefundInfo.getOrderId()+"-------------");
			
			ResultHandleT<Map<String, Object>> resultHandle = partRefundService
					.partRefundSubmit(ordRefundInfo);
			if(resultHandle.isFail()){
				LOG.error("applyRefundOperate error:"+ordRefundInfo.getOrderId()+" "+resultHandle.getMsg());
				return new ResultMessage(ResultMessage.ERROR, "申请退款失败!");
			}else{
				Map<String, Object> resultMap = resultHandle.getReturnContent();
				LOG.info("---------申请退款操作结果: " + (String) resultMap.get("MSG"));
				if ((Boolean) resultMap.get("IS_CHANGE_QUANTITY")) {// 退款份数不满足条件
					return new ResultMessage("IS_CHANGE_QUANTITY",
							"可退最大份数变动，请刷新页面");
				} else if ((Boolean) resultMap.get("IS_CHANGE_AMOUNT")) {// 退款金额已发生改变
					return new ResultMessage("IS_CHANGE_AMOUNT",
							"退款金额变动，请重新点击【计算】");
				} else {
					return new ResultMessage(resultMap.get("SUBMIT")+"",
							(String) resultMap.get("MSG"));
				}
			}
			
			
		} catch (Exception e) {
			LOG.info("---------申请退款操作Exception: " + e.getMessage());
			e.printStackTrace();
			return new ResultMessage(ResultMessage.ERROR, "申请退款 : 操作失败!");
		}
	}

	/**
	 * 计算金额
	 */
	@RequestMapping(value = "/partRefundBackAction/calculationAmount")
	@ResponseBody
	public Object calculationAmount(String refundItemInfoJson,
			HttpServletRequest request) {
		String refundAmount = "0";
		try {
			OrdRefundInfo ordRefundInfo = setOrdRefundInfoObject(
					refundItemInfoJson, request);
			ResultHandleT<PartRefundAmountInfo> resultHandleT = partRefundService
					.queryPartRefundAmount(ordRefundInfo,false);
			if (resultHandleT != null && resultHandleT.isSuccess()) {
				
				PartRefundAmountInfo partRefundAmountInfo = (PartRefundAmountInfo) resultHandleT
						.getReturnContent();
				Map<String, Object> attributes=new HashMap<String, Object>();
				attributes.put("refundFormulaDetails", partRefundAmountInfo.getRefundFormulaDetails());
				if (StringUtil.isNotEmptyString(partRefundAmountInfo
						.getRefundAmount())) {
					LOG.info("----------计算金额: refundAmount="
							+ partRefundAmountInfo.getRefundAmount());
					refundAmount = (String) partRefundAmountInfo
							.getRefundAmount();
					return new ResultMessage(attributes,ResultMessage.SUCCESS,
							refundAmount);
				} else {
					return new ResultMessage(attributes,ResultMessage.ERROR, "实退金额需审核!");
				}
			} else {
				return new ResultMessage(null,ResultMessage.ERROR,
						resultHandleT == null ? null : resultHandleT.getMsg());
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ResultMessage(null,ResultMessage.ERROR, "计算金额: 操作失败!");
		}

	}

	/**
	 * 封装OrdRefundInfo对象
	 */
	private OrdRefundInfo setOrdRefundInfoObject(String refundItemInfoJson,
			HttpServletRequest request) {
		Long orderId = Long.parseLong(request.getParameter("orderId"));
		LOG.info("---------封装OrdRefundInfo对象: orderId=" + orderId);
		String reason = request.getParameter("reason");
		String refundAmount = request.getParameter("refundAmount");
		OrdRefundInfo ordRefundInfo = new OrdRefundInfo();
		ordRefundInfo.setNeedVaild(true);
		ordRefundInfo.setOperatorName(getLoginUserId());
		ordRefundInfo.setOrderId(orderId);
		ordRefundInfo.setReason(reason);
		if(StringUtil.isNotEmptyString(refundAmount)){
			ordRefundInfo.setRefundAmount(new BigDecimal(refundAmount).multiply(new BigDecimal("100")).longValue());
		}
		ordRefundInfo.setRefundFrom("PC_BACK");
		List<OrdRefundItemInfo> ordRefundItemList = this
				.jsonToListDatas(refundItemInfoJson);
		ordRefundInfo.setRefundItems(ordRefundItemList);
		return ordRefundInfo;
	}

	/**
	 * 封装退款信息
	 */
	private List<OrdRefundItemInfo> jsonToListDatas(String refundItemInfoJson) {
		List<OrdRefundItemInfo> ordRefundItems = null;
		Map<String, Class> classMap = new HashMap<String, Class>();
		classMap.put("refundPersons", OrdPerson.class);
		if (StringUtil.isNotEmptyString(refundItemInfoJson)) {
			JSONArray jsonArray = (JSONArray) JSONSerializer
					.toJSON(refundItemInfoJson);
			if (jsonArray != null) {
				ordRefundItems = new ArrayList<OrdRefundItemInfo>();
				List list = (List) JSONSerializer.toJava(jsonArray);
				for (Object o : list) {
					JSONObject jsonObject = JSONObject.fromObject(o);
					OrdRefundItemInfo item = (OrdRefundItemInfo) JSONObject
							.toBean(jsonObject, OrdRefundItemInfo.class,
									classMap);
					ordRefundItems.add(item);
				}
			}
		}
		return ordRefundItems;
	}

}
