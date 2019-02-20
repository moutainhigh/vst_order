package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderMark;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PAYMENT_STATUS;
import com.lvmama.vst.back.rest.po.SoaSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdOrderMarkReService;
import com.lvmama.vst.order.service.IOrdOrderMarkService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderMarkVo;

/**
 * 订单可搬单
 * 
 * @author zhangpanfeng
 * 
 */
@Controller
@RequestMapping("/ord/order")
public class OrderMarkAction extends BaseActionSupport {
    private static final long serialVersionUID = 8506556301129390732L;
    private static final Log LOG = LogFactory.getLog(OrderMarkAction.class);

    @Autowired
    private IOrdOrderMarkReService ordOrderMarkReService;

    @Autowired
    private IOrdOrderMarkService ordOrderMarkService;

    @Autowired
    private IOrdOrderItemService ordOrderItemService;
    // 注入综合查询业务接口
    @Autowired
    private IComplexQueryService complexQueryService;
    // 注入分销商业务接口(订单来源、下单渠道)
    @Autowired
    private DistributorClientService distributorClientService;

    /**
     * 进入订单可搬单列表
     * 
     * @param model
     * @return
     * @throws BusinessException
     */
    @RequestMapping("/intoOrderMark.do")
    public String intoOrderMark(Model model, OrdOrderMarkVo ordOrderMarkVo, HttpServletRequest request)
            throws BusinessException {
        // 初始化查询表单,给字典项赋值
        initQueryForm(model, request);
        model.addAttribute("ordOrderMarkVo", ordOrderMarkVo);

        return "/order/query/orderMarkList";
    }

    /**
     * 订单可搬单综合查询
     * 
     * @param model
     * @param page
     * @param monitorCnd
     * @param request
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/orderMarkList.do")
    public String orderMarkList(Model model, Integer page, OrdOrderMarkVo ordOrderMarkVo, HttpServletRequest request)
            throws BusinessException {

        // 初始化查询表单,给字典项赋值
        initQueryForm(model, request);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", ordOrderMarkVo.getOrderId());
        if(StringUtil.isNotEmptyString(ordOrderMarkVo.getCreateTimeBegin()))
        	params.put("createTimeBegin", DateUtil.getDateByStr(ordOrderMarkVo.getCreateTimeBegin(), DateUtil.HHMMSS_DATE_FORMAT));
        if(StringUtil.isNotEmptyString(ordOrderMarkVo.getCreateTimeEnd()))
        	params.put("createTimeEnd", DateUtil.getDateByStr(ordOrderMarkVo.getCreateTimeEnd(), DateUtil.HHMMSS_DATE_FORMAT));
        params.put("managerId", ordOrderMarkVo.getManagerId());
        params.put("orderStatus", ordOrderMarkVo.getOrderStatus());
        params.put("paymentStatus", ordOrderMarkVo.getPaymentStatus());
        params.put("markFlag", ordOrderMarkVo.getMarkFlag());
        
        //下单渠道
        List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>()).getReturnContent();
        Map<Long, String> distributorMap = new HashMap<Long, String>();
        for(Distributor distributor:distributorList){
            distributorMap.put(distributor.getDistributorId(), distributor.getDistributorName());
        }
        

        int count = ordOrderMarkReService.getTotalCount(params);

        int pagenum = page == null ? 1 : page;
        Page<OrdOrderMarkVo> pageParam = Page.page(count, 10, pagenum);
        pageParam.buildUrl(request);
        params.put("_start", pageParam.getStartRows());
        params.put("_end", pageParam.getEndRows());
        params.put("_orderby", "ORDER_ID");
        params.put("_order", "desc");

        List<OrdOrderMarkVo> list = ordOrderMarkReService.findOrdOrderMarkResByParams(params);
        if (list != null) {
            for (OrdOrderMarkVo vo : list) {
                Integer buyCount = 0;
                List<OrdOrderItem> itemList = ordOrderItemService.selectByOrderId(vo.getOrderId());
                if (list != null) {

                    OrdOrderItem mainOrderItem = null;
                    for (OrdOrderItem item : itemList) {
                        buyCount += Integer.valueOf(item.getQuantity().toString());
                        if ("true".equals(item.getMainItem())) {
                            mainOrderItem = item;
                            if (StringUtil.isEmptyString(vo.getProductName())) {
								vo.setProductName(mainOrderItem.getProductName());
							}
                        }
                    }
                    //为了让没有订单子项的时候页面显示为空
                    if(itemList.size() == 0){
                        buyCount = null;
                    }
                    vo.setBuyCount(buyCount);
                    String currentStatus = buildCurrentStatus(vo, itemList, mainOrderItem);
                    vo.setCurrentStatus(currentStatus);
                    
                    vo.setDistributorName(distributorMap.get(vo.getDistributorId()));
                }
            }
        }

        pageParam.setItems(list);

        CommEnumSet.BU_NAME[] buSet = CommEnumSet.BU_NAME.values();
        SuppGoods.PAYTARGET[] payTargetSet = SuppGoods.PAYTARGET.values();

        model.addAttribute("payTargetSet", payTargetSet);
        model.addAttribute("buSet", buSet);
        model.addAttribute("pageParam", pageParam);
        model.addAttribute("managerName", request.getParameter("managerName"));
        return "/order/query/orderMarkList";
    }
    
    @RequestMapping(value = "/updateMarkFlag")
    @ResponseBody
    public Object updateMarkFlag(OrdOrderMark orderMark) throws BusinessException {
        log.info("start method<updateMarkFlag>");
        ResultMessage result = ResultMessage.SET_SUCCESS_RESULT;
        if (orderMark != null) {
            OrdOrderMark dbOrderMark = ordOrderMarkService.findOrdOrderMarkByOrderId(orderMark.getOrderId());
            if (dbOrderMark != null) {
                dbOrderMark.setMarkFlag(orderMark.getMarkFlag());
                int num = ordOrderMarkService.updateOrdOrderMark(dbOrderMark);
                if(num != 1){
                    log.info("orderMark is not exist!");
                    result = ResultMessage.SET_FAIL_RESULT;
                }
            } else {
                Long markId = ordOrderMarkService.saveOrdOrderMark(orderMark);
                if(markId == null){
                    log.info("orderMark save failed!");
                    result = ResultMessage.SET_FAIL_RESULT;
                }
            }
        } else {
            log.error("orderMark is null!");
            result = ResultMessage.SET_FAIL_RESULT;
        }

        return result;
    }

    /**
     * 初始化查询表单
     * 
     * @param model
     * @param request
     * @throws BusinessException
     */
    private void initQueryForm(Model model, HttpServletRequest request) throws BusinessException {
        // 订单状态字典
        Map<String, String> orderStatusMap = new LinkedHashMap<String, String>();
        orderStatusMap.put("", "全部");
        for (ORDER_STATUS item : ORDER_STATUS.values()) {
            orderStatusMap.put(item.getCode(), item.getCnName());
        }
        model.addAttribute("orderStatusMap", orderStatusMap);

        // 支付状态字典
        Map<String, String> paymentStatusMap = new LinkedHashMap<String, String>();
        paymentStatusMap.put("", "全部");
        for (PAYMENT_STATUS item : PAYMENT_STATUS.values()) {
            paymentStatusMap.put(item.getCode(), item.getCnName());
        }

        model.addAttribute("paymentStatusMap", paymentStatusMap);
    }

    /**
     * 处理订单的当前状态
     * 
     * @param order
     * @return
     */
    private String buildCurrentStatus(OrdOrderMarkVo ordOrderMarkVo, List<OrdOrderItem> orderItemList,
            OrdOrderItem mainOrderItem) {
        StringBuilder builder = new StringBuilder();
        // 组装订单状态
        if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(ordOrderMarkVo.getOrderStatus())) {
            builder.append("取消");
        } else if (OrderEnum.ORDER_STATUS.NORMAL.name().equals(ordOrderMarkVo.getOrderStatus())) {
            builder.append("正常");
        } else if (OrderEnum.ORDER_STATUS.COMPLETE.name().equals(ordOrderMarkVo.getOrderStatus())) {
            builder.append("完成");
        } else {
            builder.append(ordOrderMarkVo.getOrderStatus());
        }

        builder.append("<br>");

        // 组装审核状态
        if (OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(ordOrderMarkVo.getInfoStatus())
                && OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(ordOrderMarkVo.getResourceStatus())) {
            builder.append("未审核");
        } else if (OrderEnum.INFO_STATUS.INFOFAIL.name().equals(ordOrderMarkVo.getInfoStatus())
                || OrderEnum.RESOURCE_STATUS.LOCK.name().equals(ordOrderMarkVo.getResourceStatus())) {
            builder.append("审核不通过");
        } else if (OrderEnum.INFO_STATUS.INFOPASS.name().equals(ordOrderMarkVo.getInfoStatus())
                && OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(ordOrderMarkVo.getResourceStatus())) {
            builder.append("审核通过");
        } else {
            builder.append("审核中");
        }

        builder.append(" | ");

        // 组装凭证确认状态
        if (OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(ordOrderMarkVo.getCertConfirmStatus())) {
            builder.append("未确认");
        } else if (OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(ordOrderMarkVo.getCertConfirmStatus())) {
            builder.append("已确认");
        } else {
            builder.append("未确认");
        }

        builder.append("<br>");

        // 组装支付状态
        builder.append(OrderEnum.PAYMENT_STATUS.getCnName(ordOrderMarkVo.getPaymentStatus()));
        String categoryType = "";
        if (mainOrderItem != null) {
            Map<String, Object> contentMap = mainOrderItem.getContentMap();
            categoryType = (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
        }
        
        if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryType)) {
            builder.append(" | ");
            // 出团通知书状态
            String noticeStatusName = OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(ordOrderMarkVo
                    .getNoticeRegimentStatus());
            builder.append(noticeStatusName);
        }

        // 门票业务类订单使用状态

        if (OrderUtils.isTicketByCategoryId(ordOrderMarkVo.getCategoryId())) {
            builder.append(" | ");

            // 门票业务类订单使用状态
            List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
            // 订单使用状态
            List<String> perFormStatusList = new ArrayList<String>();
            for (OrdOrderItem ordOrderItem : orderItemList) {
                // 门票业务类订单使用状态
                Map<String, Object> performMap = ordOrderItem.getContentMap();
                String categoryCode = (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
                if (ProductCategoryUtil.isTicket(categoryCode)) {
                    resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
                    String performStatusName = OrderEnum.PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(
                            resultList, null, ordOrderItem));
                    perFormStatusList.add(performStatusName);
                }
            }
            // 门票业务类订单使用状态
            builder.append(OrderUtils.getMainOrderPerformStatus(perFormStatusList));

        }
        // 酒店业务类订单使用状态
        if (OrderUtils.isHotelByCategoryId(ordOrderMarkVo.getCategoryId())) {
            builder.append(" | ");
            builder.append(OrderEnum.PERFORM_STATUS_TYPE.getCnName(mainOrderItem.getPerformStatus()));
        }
        return builder.toString();
    }
}
