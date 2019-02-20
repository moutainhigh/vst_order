package com.lvmama.vst.order.web.ticket;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.service.api.comm.order.IApiOrdOrderPartnerRelationService;
import com.lvmama.order.vo.comm.OrdOrderPartnerRelationVo;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;


/** 
 * @ClassName: TicketOrderPartnerRelationAction 
 * @Description: 合作方对接订单关系查询（目前只有苏宁）
 * @author: lijunshuai
 * @date: 2018年10月15日 上午11:09:05  
 */
@Controller
public class TicketOrderPartnerRelationAction extends BaseOrderAciton {
	
	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(TicketOrderPartnerRelationAction.class);
	/**
	 * 默认分页大小
	 */
	private static final Integer DEFAULT_PAGE_SIZE = 10; 
	
	private final String ERROR_PAGE="/order/error";
	/**
	 * 订单中间表关系服务
	 */
	@Autowired
	private IApiOrdOrderPartnerRelationService apiOrdOrderPartnerRelationService;
	
	
	@RequestMapping("/suning/order/findOrderList.do")
	public String index() {
		return "/order/ticket/ticketOrderRelationInfo";
	}
	/**
	 * 查询
	 * @return
	 */
	@RequestMapping("/suning/order/queryOrderList.do")
	public String findOrderList(String orderCode,Integer page,Integer pageSize,Model model,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		try {
			List<OrdOrderPartnerRelationVo> list = new ArrayList<OrdOrderPartnerRelationVo>();
			long total = 0L;
			//构建分页对象
			Integer currentPage = page == null ? 1 : page;
			Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
			Long start = (long) ((currentPage.intValue()-1)*currentPageSize + 1);
			Long end = (long) ((currentPage.intValue())*currentPageSize);
			if(StringUtils.isNotBlank(orderCode)) {
				//查询单个订单
				try {
					ResponseBody<OrdOrderPartnerRelationVo> res = apiOrdOrderPartnerRelationService.selectRelationByOrderCode(new RequestBody<String>(orderCode));
					if(res != null && res.isSuccess() && res.getT() != null) {
						total = 1L;
						list.add(res.getT());
					}
				} catch (Exception e) {
					LOG.error("TicketOrderPartnerRelation查询失败", e);
				}
			}else {
				try {
					OrdOrderPartnerRelationVo params = new OrdOrderPartnerRelationVo();
					//查询所有
					RequestBody<OrdOrderPartnerRelationVo> req = new RequestBody<OrdOrderPartnerRelationVo>(params);
					ResponseBody<Integer> countNum = apiOrdOrderPartnerRelationService.selectRelationCountForPage(req);
					if(countNum != null && countNum.getT() != null && countNum.getT() > 0) {
						total = countNum.getT();
						params.set_start(start);
						params.set_end(end);
						params.set_orderby("create_time");
						params.set_order("desc");
						ResponseBody<List<OrdOrderPartnerRelationVo>> pageList = apiOrdOrderPartnerRelationService.selectRelationForPage(req);
						if(pageList != null && pageList.getT() != null && pageList.getT().size() > 0) {
							list.addAll(pageList.getT());
						}
					}
				} catch (Exception e) {
					LOG.error("TicketOrderPartnerRelation查询失败", e);
				}
			}
			this.convert(list);
			Page<OrdOrderPartnerRelationVo> resultPage = Page.page(total, currentPageSize, currentPage);
			resultPage.buildUrl(request);
			resultPage.setItems(list);
			resultPage.setTotalResultSize(total);
			//保存查询结果
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("orderCode", orderCode);
			return "/order/ticket/ticketOrderRelationInfo";
		} catch (Exception e) {
			LOG.error("TicketOrderPartnerRelationAction_findOrderList", e);
			return ERROR_PAGE;
		}
	}
	
	/** 
	 * @Title: convert 
	 * @Description:状态装换
	 * @param source
	 * @return: void
	 */
	private void convert(List<OrdOrderPartnerRelationVo> source) {
		if(source != null && source.size() > 0) {
			for (OrdOrderPartnerRelationVo vo : source) {
				//订单状态
				vo.setOrderStatus(this.convertAttributes(vo.getOrderStatus()));
				//对接状态
				vo.setInvokeInterfaceStatus(this.convertAttributes(vo.getInvokeInterfaceStatus()));
				//有无凭证
				vo.setHasPasscode(this.convertAttributes(vo.getHasPasscode()));
				//退款状态
				vo.setRefundStatus(this.convertAttributes(vo.getRefundStatus()));
			}
		}
	}
	
	private String convertAttributes(String attr) {
		if(attr == null) {
			return null;
		}
		String result = null;
		switch(attr) {
			case "Y":
				result ="有";
				break;
			case "N":
				result ="无";
				break;
			case "NORMAL":
				result ="正常";
				break;
			case "CANCEL":
				result ="取消";
				break;
			case "APPLY_Y_NOTIFY_N":
				result ="申码成功-推送失败";
				break;
			case "APPLY_Y_NOTIFY_Y":
				result ="申码成功-推送成功";
				break;
			case "PERFORM_Y_NOTIFY_N":
				result ="已使用-推送失败";
				break;
			case "PERFORM_Y_NOTIFY_Y":
				result ="已使用-推送成功";
				break;
			case "REFUND_FAIL":
				result ="退款失败";
				break;
			case "REFUND_SUCCESS":
				result ="退款成功";
				break;
			default:
				result = attr;
				break;
		}
		return result;
	}
}
