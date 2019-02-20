package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.ExpiredRefundEnum;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundCnd;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundRst;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.constant.config.DynConfigProp;
import com.lvmama.vst.order.job.overdue.service.impl.OverdueTicketProcessingServiceImpl;
import com.lvmama.vst.order.job.overdue.service.impl.OverdueTicketSynStateTaskServiceImpl;
import com.lvmama.vst.order.service.OrdExpiredRefundService;
import com.lvmama.vst.supp.client.po.RefundResultPo;
import com.lvmama.vst.supp.client.service.OverdueRefundService;

/**
 * 门票过期退 Controller
 * 
 * <p>
 * 查询(分页/条件)、刷新(废码失败/审核中)、废码日志、人工废码链接
 * </p>
 */
@Controller
@RequestMapping("/order/expiredRefund")
public class OrdExpiredRefundAction extends BaseActionSupport {

	private static final long serialVersionUID = 5976428489854933974L;
	
	private static final Log LOG = LogFactory.getLog(OrdExpiredRefundAction.class);
	
	private static final String EXPIRED_REFUND_LIST = "/order/expiredRefund/ordExpiredRefundList";
	
	@Autowired
	private OrdExpiredRefundService ordExpiredRefundService;
	
	@Autowired(required=true)
	private OverdueTicketSynStateTaskServiceImpl overdueTicketSynStateJob;
	
	@Resource
	private OverdueRefundService overdueRefundService;
	
	@Resource
	private OverdueTicketProcessingServiceImpl overdueTicketProcessingService;
	
	@RequestMapping("/intoList.do")
	public String intoList(Model model, HttpServletRequest request, OrdExpiredRefundCnd erCnd) throws BusinessException {
		
		// 初始化查询表单
		initQueryForm(model);
		
		model.addAttribute("erCnd", erCnd);
		
		return EXPIRED_REFUND_LIST;
	}
	
	/**
	 * 过期退数据列表
	 * 
	 * @param model Model
	 * @param request HttpServletRequest
	 * @param page Integer
	 * @param pageSize Integer
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/list.do")
	@SuppressWarnings("rawtypes")
	public String ordExpiredRefundList(Model model, HttpServletRequest request, OrdExpiredRefundCnd erCnd, Integer page, Integer pageSize) 
			throws BusinessException {
		
		// 初始化查询表单
		initQueryForm(model);
		
		// 分页设置
		Integer curPageSize = pageSize == null ? Constant.DEFAULT_PAGE_SIZE : pageSize;
		Integer curPage = page == null ? 1 : page;
		Page pageData = Page.page(curPageSize, curPage);
		
		List<OrdExpiredRefundRst> resultList = new ArrayList<OrdExpiredRefundRst>();
		Long totalCount = 0L;
		
		// 分页查询
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", erCnd.getOrderId());
			params.put("productId", erCnd.getProductId());
			params.put("productName", erCnd.getProductName());
			params.put("suppGoodsId", erCnd.getSuppGoodsId());
			params.put("distChnlId", erCnd.getDistChnlId());
			params.put("supplierName", erCnd.getSupplierName());
			params.put("orderItemStatus", erCnd.getOrderItemStatus());
			params.put("processStatus", erCnd.getProcessStatus());
			params.put("_start", pageData.getStartRows());
			params.put("_end", pageData.getEndRows());
			params.put("_ROWNUM", DynConfigProp.getInstance().getErRownumMax());
			
			resultList = ordExpiredRefundService.queryListForPage(params);
			
			int rSize = resultList.size();
			if (page == null && rSize < Constant.DEFAULT_PAGE_SIZE) {
				totalCount = (long) rSize;
		    } else {
				totalCount = ordExpiredRefundService.queryTotalCountForPage(params);
		    }
			
			// 调PASSPORT接口获取CODE_ID(废码日志)
			if (!CollectionUtils.isEmpty(resultList)) {
				remoteCallForCodeIds(resultList);
			}
		} catch (Exception e) {
			LOG.error("Query OrdExpiredRefundList is error!", e);
		}
		
		// 分页结果集
		Page resultPage = buildResultPage(resultList, pageData, totalCount, request);
		model.addAttribute("resultPage", resultPage);
		
		// 查询条件回显
		model.addAttribute("erCnd", erCnd);
		
		return EXPIRED_REFUND_LIST;
	}
	
	/**
	 * 刷新：手工触发处理过期退意向单, 包含"审核中"、"处理次数超限"、"处理失败"
	 * 
	 * @return String
	 */
	@RequestMapping(value = "/refresh.do")
	@ResponseBody
	public int refresh() {
		MemcachedUtil memcached = MemcachedUtil.getInstance();
		String cachekey = Constant.ER_CACHE_REFRESH_KEY;
		if (!memcached.keyExists(cachekey)) {
			memcached.set(cachekey, DynConfigProp.getInstance().getErPageRefreshTime(), Constant.Y_FLAG);
			
			try {
				new Thread() {
					@Override
					public void run() {
						// 手工触发JOB刷新过期退意向单
					    overdueTicketSynStateJob.doRefreshState(null);
					}
				}.start();
			} catch (Exception e) {
				LOG.error("Refresh OrderExpiredRefund is error!", e);
				memcached.remove(cachekey);
				return -1;
			}
		} else {
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * 初始化查询表单
	 * <p>
	 * 1、表单字典项初始化
	 * </p>
	 * 
	 * @param model Model
	 */
	private void initQueryForm(Model model) {
		
		// (1)销售渠道字典
		model.addAttribute("distChnlMap", ExpiredRefundEnum.DIST_CHNL.getDistChnlMap(true));
		
		// (2)子订单状态字典
		// model.addAttribute("orderItemStatusMap", ExpiredRefundEnum.ORDER_ITEM_STATUS.getOrderItemStatusMap(true));
	
	    // (3)处理状态字典
		model.addAttribute("processStatusMap", ExpiredRefundEnum.PROCESS_STATUS.getProcessStatusMap(true));
	}
	
	/**
	 * 构造分页对象
	 * 
	 * @param list list
	 * @param pageData Page
	 * @param totalCount Long
	 * @param request HttpServletRequest
	 * @return Page
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Page buildResultPage(List list, Page pageData, Long totalCount, HttpServletRequest request) {
		pageData.setTotalResultSize(totalCount);
		pageData.buildUrl(request);
		pageData.setItems(list);
		return pageData;
	}
	
	/**
	 * 调用PASSPORT接口获取INTF_PASS_CODE.CODE_ID，废码日志查看
	 * 
	 * @param resultList List<OrdExpiredRefundRst>
	 */
	private void remoteCallForCodeIds(List<OrdExpiredRefundRst> resultList) {
		List<Long> params = new ArrayList<Long>();
		for (OrdExpiredRefundRst item : resultList) {
			params.add(item.getOrderItemId());
		}
		
		try {
			ResultHandleT<Map<Long, Long>> resultHandle = overdueRefundService.queryBatchCodeIds(params);
			Map<Long, Long> resultMap = resultHandle.getReturnContent();
			if (CollectionUtils.isEmpty(resultMap)) {
				return;
			}
			
			for (OrdExpiredRefundRst item : resultList) {
				Long codeId = resultMap.get(item.getOrderItemId());
				if (codeId != null) {
					item.setCodeId(codeId);
				}
			}
		} catch (Exception e) {
			LOG.error("OrderExpiredRefund#remoteCallForCodeIds() is error! params=" + params, e);
		}
	}
	
	/**
	 * 人工废码
	 * 
	 * @param orderItemId Long
	 * @return String
	 */
	@RequestMapping(value = "/destroyCode.do")
	@ResponseBody
	public String destroyCode(Long orderItemId) {
		if (orderItemId == null || orderItemId <= 0) {
			return "-1";
		}
		
		try {
			PermUser user = (PermUser) getSession(BaseActionSupport.SESSION_BACK_USER);
			
			ResultHandleT<RefundResultPo> result = overdueRefundService.doDestroy(orderItemId, user.getUserName());
			RefundResultPo po = result.getReturnContent();
			String status = po.getStatus();
			LOG.info("OrderExpiredRefund#destroyCode() orderItemId=" + orderItemId + ", status=" + status);
		    
			if (StringUtils.equals(status, "DESTROYED_SUCCESS")) {
				Boolean bol = overdueTicketProcessingService.updateOverdueTicketSubOrderInOneShot(orderItemId);
				
				LOG.info("OrderExpiredRefund#destroyCode()#updateERState is " + bol + ", orderItemId=" + orderItemId);
			    if (!bol) {
			    	return "-3";
			    }
			}
			
		    return status;
		} catch (Exception e) {
			LOG.error("OrderExpiredRefund#destroyCode() is error! orderItemId=" + orderItemId, e);
			return "-2";
		}
	}
	
}
