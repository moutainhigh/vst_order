package com.lvmama.vst.order.confirm.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.ebooking.client.ebk.serivce.EbkMailTaskClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.google.common.collect.Lists;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.dest.api.vst.pack.service.IHotelGroupPackGoodslVstApiService;
import com.lvmama.dest.api.vst.pack.vo.HotelGoodsBranchVstVo;
import com.lvmama.scenic.api.back.exception.BusinessException;
import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.DestOrderService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_CHANNEL_OPERATE;
import com.lvmama.vst.back.order.po.OrdItemAddition;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_OBJECT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_TYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ComAuditInfo;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkFaxTaskClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertifItem;
import com.lvmama.vst.ebooking.ebk.vo.ProdSuppGoodsVO;
import com.lvmama.vst.ebooking.fax.po.EbkFaxRecv;
import com.lvmama.vst.ebooking.fax.po.EbkFaxRecvItem;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmEmailService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdItemAdditionService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;

/**
 * 项目名称：vst_order
 * 类名称：OrderItemConfirmStatusAction
 * 类描述：子订单子确认状态action（目的地员工库）
 * 创建人：majunli
 * 创建时间：2016-10-28 上午11:16:08
 * 修改人：majunli
 * 修改时间：2016-10-28 上午11:16:08
 * 修改备注：
 */
@Controller
@RequestMapping("/ord/order/confirm")
public class OrderItemConfirmStatusAction extends BaseActionSupport {

    private static final long serialVersionUID = 529631359158457689L;

    //日志记录器
    private static final Log LOGGER = LogFactory.getLog(OrderItemConfirmStatusAction.class);

    // 默认分页大小配置名称
    private static final Integer DEFAULT_PAGE_SIZE = 10;
    
    //分销，淘宝
    private static final String DISTRIBUTOR_CODE_TAOBAO="DISTRIBUTOR_TAOBAO";

    //品类ids
    public final static List<Long> categoryIds = Arrays.asList(
            BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(),
            BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId(),
            BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId(),
            BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId());

    @Autowired
    private BizBuEnumClientService bizBuEnumClientService;

    @Autowired
    private IOrderAuditService orderAuditService;

    @Autowired
    private IComMessageService comMessageService;

    @Autowired
    private IOrdOrderService ordOrderService;

    @Autowired
    private IOrdOrderItemService ordOrderItemService;

    //订单综合查询
    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private SuppSupplierClientService suppSupplierClientService;

    @Autowired
    private IOrdItemConfirmStatusService ordItemConfirmStatusService;

    @Autowired
    private IOrdItemConfirmProcessService ordItemConfirmProcessService;

    @Autowired
    private IOrdStatusManageConfirmProcessService ordStatusManageConfirmProcessService;

    @Autowired
    private DictDefClientService dictDefClientService;

    @Autowired
    private DestOrderService destOrderService;

    @Autowired
    private OrderRefundProcesserAdapter orderRefundProcesserAdapter;

    @Autowired
    private EbkFaxTaskClientService ebkFaxTaskClientService;

    @Autowired
    private EbkMailTaskClientService ebkMailTaskClientService;

    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Autowired
    private IOrdItemPersonRelationService ordItemPersonRelationService;

    @Autowired
    private IOrdPersonService ordPersonService;

    @Autowired
    private IOrdItemAdditionService ordItemAdditionService;

    @Autowired
    private IOrdItemConfirmEmailService ordItemConfirmEmailService;
    @Autowired
    private IOrderUpdateService ordOrderUpdateService;
    @Autowired
    private SuppGoodsClientService suppGoodsClientService;
    @Autowired
    private ComLogClientService comLogClientService;
    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
    @Autowired
    private IHotelGoodsQueryVstApiService hotelGoodsQueryVstApiService;
    @Autowired
    private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;
    @Autowired
    private IHotelGroupPackGoodslVstApiService hotelGroupPackGoodslVstApiService;
    /**
     * 员工库查询
     *
     * @param model
     * @param page
     * @param pageSize
     * @param monitorCnd
     * @param checkedTab
     * @param isDelay
     * @param mainCheckedTab
     * @param req
     * @param res
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:29:53
     */
    @RequestMapping(value = "/queryDestTaskList.do")
    public String queryDestTaskList(Model model, Integer page, Integer pageSize, OrderMonitorCnd monitorCnd, String operatorName,
                                    String checkedTab, String isDelay, String mainCheckedTab, String mainTab, HttpServletRequest req, HttpServletResponse res) {
        try {
        	model.addAttribute("checkedTab",checkedTab);
            long startTimeMillis = System.currentTimeMillis();
            //是否保留房
            Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
            stockFlagMap.put("", "全部");
            stockFlagMap.put("Y", "保留房");
            stockFlagMap.put("N", "非保留房");
            model.addAttribute("stockFlagMap", stockFlagMap);

            //组装订单审核列表条件
            Map<String, Object> auditParam = new HashMap<String, Object>();
            auditParam.put("categoryIds", categoryIds);
            //订单负责人 dongningbo 切换工作台保留订单负责人
            if (StringUtil.isEmptyString(operatorName)) {
                operatorName = getLoginUserId();
            }
            if (!"admin".equals(operatorName)) {
                auditParam.put("operatorName", operatorName);
            }
            model.addAttribute("operatorName", operatorName);

            //待处理和暂缓bespokeOrder=Y 显示暂缓，为""显示待处理，点击已审库是始终显示待处理(暂缓参数isDelay)
            String bespokeOrder = "";
            if ("N".equals(isDelay)) {
                monitorCnd.setBespokeOrder(bespokeOrder);
            } else {
                if (StringUtil.isNotEmptyString(monitorCnd.getBespokeOrder())) {
                    bespokeOrder = monitorCnd.getBespokeOrder();
                } else {
                    monitorCnd.setBespokeOrder(bespokeOrder);
                }
            }

            //计算各tab的记录数
            countTabs(model, auditParam, bespokeOrder);

            String orderby = "COM_AUDIT.CREATE_TIME asc";
            //选择 酒店 或 主单预订通知
            if (StringUtil.isEmptyString(mainTab) || "HOTEL".equals(mainTab)) {
                auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
                // 选择tab
                if (StringUtil.isNotEmptyString(checkedTab)) {
                    switchTheTab(model, auditParam, checkedTab);
                } else {
                    //默认显示已审
                    checkedTab = Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name();
                    switchTheTab(model, auditParam, checkedTab);
                    model.addAttribute("checkedTab", checkedTab);
                }
                //只有已审时才显示暂缓相关
                if (checkedTab.equals(Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name())) {
                    auditParam.put("bespokeOrder", bespokeOrder);
                    orderby = orderby + ",COM_AUDIT.SEQ desc";
                }
            } else {
                model.addAttribute("mainTab", mainTab);
                auditParam.remove("auditType");
                auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
                auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
                auditParam.put("mainAudittype", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
            }

            //只有已审时才显示暂缓相关
            if (checkedTab.equals(Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name())) {
                auditParam.put("bespokeOrder", bespokeOrder);
                orderby = "SEQ desc,"+orderby;
            }
            auditParam.put("_orderby", orderby);
            String logs = "select with stockFlag and orderItemId params:";
            //保留房
            if (StringUtil.isNotEmptyString(monitorCnd.getStockFlag())) {
                if ("Y".equals(monitorCnd.getStockFlag())) {
                    auditParam.put("stockFlag", "-1");
                } else if ("N".equals(monitorCnd.getStockFlag())) {
                    auditParam.put("stockFlag", "-2");
                }
                logs = logs + auditParam.get("stockFlag");
            }

            //子订单ID
            if (monitorCnd.getOrderItemId() != null) {
                auditParam.put("orderItemId", monitorCnd.getOrderItemId());
                logs = logs + "," + auditParam.get("orderItemId");
                LOGGER.info(logs);
            }
            //总记录数
            int auditTotalCount = orderAuditService.countAuditByDestWork(auditParam);
            int currentPage = page == null ? 1 : page;
            int currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

            Page<ComAuditInfo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
            pageParam.buildUrl(req);

            auditParam.put("_start", pageParam.getStartRows());
            auditParam.put("_end", pageParam.getEndRows());

            //查询订单审核列表集合
            List<ComAudit> auditList = orderAuditService.queryDestAuditListByCriteria(auditParam);

            Set<Long> orderItemIds = new TreeSet<Long>();
            orderItemIds.add(0L);
            Set<Long> orderIds = new TreeSet<Long>();   //主单预订通知
            orderIds.add(0L);
            for (ComAudit comAudit : auditList) {
                if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())) {
                    orderIds.add(comAudit.getObjectId());
                } else {
                    orderItemIds.add(comAudit.getObjectId());
                }
            }

            if (orderIds.size() != 1 && orderItemIds.size() == 1) {
                orderItemIds = null;
            } else {
                orderIds = null;
            }

            /** 查询订单信息 Start*/
            // 根据页面条件组装综合查询接口条件
            ComplexQuerySQLCondition orderItemCondition = buildQueryCondition(0, 0, orderIds, orderItemIds);
            // 根据条件获取订单集合
            List<OrdOrder> orderItemList = complexQueryService.queryOrderListByCondition(orderItemCondition);
            // 根据页面展示特色组装其想要的结果
            List<OrderMonitorRst> orderItemResultList = buildQueryOrderItemResult(orderItemList, req);
            //将订单转化为map,方便数据整合
            Map<Long, OrderMonitorRst> orderItemResultMap = new HashMap<Long, OrderMonitorRst>(orderItemList.size() * 2);
            for (OrderMonitorRst orderMonitorRst : orderItemResultList) {
                if (orderIds != null && orderIds.contains(orderMonitorRst.getOrderId())) {
                    orderItemResultMap.put(orderMonitorRst.getOrderId(), orderMonitorRst);
                } else {
                    orderItemResultMap.put(orderMonitorRst.getOrderItemId(), orderMonitorRst);
                }
            }
            /** 查询订单信息 End*/

            //将订单对象整合到审核对象里
            List<ComAuditInfo> comAuditInfoList = new ArrayList<ComAuditInfo>();
            for (ComAudit comAudit : auditList) {
                ComAuditInfo comAuditInfo = new ComAuditInfo();
                BeanUtils.copyProperties(comAudit, comAuditInfo);
                comAuditInfo.setOrderMonitorRst(orderItemResultMap.get(comAudit.getObjectId()));
                //dongningbo 工作台【下单时长】的数据显示取消，替换为【入库时长】显示内容为订单进入员工工作台时间。显示格式不变。
                if (comAudit.getRemindTime() != null) {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getRemindTime()));
                } else {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getCreateTime()));
                }
                if (StringUtil.isNotEmptyString(checkedTab) && "CONFIRM_OTHER_AUDIT".equals(checkedTab)) {
                    Map<String, Object> messageParams = new HashMap<String, Object>();
                    messageParams.put("auditId", comAudit.getAuditId());
                    List<ComMessage> comMessageList = comMessageService.findComMessageList(messageParams);
                    if (CollectionUtils.isNotEmpty(comMessageList)) {
                        comAuditInfo.setComMessage(comMessageList.get(0));
                    }
                }

                comAuditInfoList.add(comAuditInfo);
            }
            // 组装分页结果
            @SuppressWarnings("rawtypes")
            Page resultPage = buildResultPage(comAuditInfoList, currentPage, pageSize, NumberUtils.toLong(auditTotalCount + "", 0), req);
            // 存储分页结果
            model.addAttribute("resultPage", resultPage);
            model.addAttribute("checkedTab",checkedTab);
            LOGGER.info("OrderItemConfirmStatusAction.queryDestTaskList TimeMillis="+(System.currentTimeMillis()-startTimeMillis));
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("服务器内部异常");
            }
            LOGGER.error(ExceptionFormatUtil.getTrace(e));
        }
        // 查询条件回显
        model.addAttribute("monitorCnd", monitorCnd);
        //嵌套我的工作台头部
        if ("MYDESTTASK".equals(mainCheckedTab)) {
            model.addAttribute("mainCheckedTab", mainCheckedTab);
            return "/order/confirm/destTaskListWithWorkbench";
        }
        return "/order/confirm/destTaskList";
    }

    /**
     * @Description: 选择我的工作台tab
     * @author majunli
     * @date 2017-2-16 下午5:47:27
     */
    @RequestMapping(value = "/selectMainCheckedTab.do")
    private String selectMainCheckedTab(Model model, String mainCheckedTab, RedirectAttributes attr, String operatorName) {
        try {
            if ("MYTASK".equals(mainCheckedTab) || "MYORDER".equals(mainCheckedTab) || "MYTASKFORPAY".equals(mainCheckedTab)) {
                attr.addAttribute("checkedTab", mainCheckedTab);
                attr.addAttribute("operatorName", operatorName);
                return "redirect:/ord/order/selectTabInWorkBench.do";
            } else if ("MYDESTTASK".equals(mainCheckedTab)) {
                attr.addAttribute("mainCheckedTab", mainCheckedTab);
                attr.addAttribute("operatorName", operatorName);
                return "redirect:/ord/order/confirm/queryDestTaskList.do";
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("服务器内部异常");
            }
            LOGGER.error(ExceptionFormatUtil.getTrace(e));
        }
        attr.addAttribute("mainCheckedTab", "MYDESTTASK");
        return "redirect:/ord/order/confirm/queryDestTaskList.do";
    }

    /**
     * 切换tab
     *
     * @param model
     * @param auditParam
     * @param checkedTab
     * @author majunli
     * @date 2016-10-28 上午11:35:25
     */
    private void switchTheTab(Model model, Map<String, Object> auditParam, String checkedTab) {
        //未处理
        auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
        if (Confirm_Enum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT.name().equals(checkedTab)) {
            //新单库，支付时间+5分钟
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT.name());
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name().equals(checkedTab)) {
            //取消确认库，需要排除子订单确认状态为空的
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name());
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name().equals(checkedTab)) {
            //已审
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name());
            auditParam.put("isReturnBack", false);
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.FULL_AUDIT.name().equals(checkedTab)) {
            //订单满房
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.FULL_AUDIT.name());
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT.name().equals(checkedTab)) {
            //特殊满房
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT.name());
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT.name().equals(checkedTab)) {
            //订单变价
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT.name());
        } else if (Confirm_Enum.CONFIRM_AUDIT_TYPE.INQUIRY_AUDIT.name().equals(checkedTab)) {
            //询位库
            auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INQUIRY_AUDIT.name());
        } else if ("CONFIRM_OTHER_AUDIT".equals(checkedTab)) {
            //其它预订通知
            auditParam.remove("auditType");
            auditParam.put("mainAudittype", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
            auditParam.put("auditSubTypes", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name());
        } else if ("INCONFIRM_BACK_AUDIT".equals(checkedTab)) {
        	auditParam.put("isReturnBack", true);
        }
        if (model != null) {
            model.addAttribute("checkedTab", checkedTab);
        }
    }

    /**
     * 查询tab记录数
     *
     * @param model
     * @param param
     * @param bespokeOrder
     * @author majunli
     * @date 2016-10-28 上午11:35:49
     */
    private void countTabs(Model model, Map<String, Object> param, String bespokeOrder) {
        Map<String, Object> auditParam = new HashMap<String, Object>();
        auditParam.putAll(param);
        auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());

        //新单库，支付时间+5分钟
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT.name());
        int newOrderAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("newOrderAuditNum", newOrderAuditNum);

        //取消确认库，需要排除子订单确认状态为空的
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name());
        int cancelAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("cancelAuditNum", cancelAuditNum);

        //已审库
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name());
        auditParam.put("isReturnBack", false);
        //添加暂缓
        auditParam.put("bespokeOrder", bespokeOrder);
        int auditCount = orderAuditService.countAuditByDestWork(auditParam);
        int pendingNum = 0;
        int delayNum = 0;
        //计算处理中和暂缓计数
        if ("".equals(bespokeOrder)) {
            pendingNum = auditCount;
            model.addAttribute("pendingNum", pendingNum);
            auditParam.put("bespokeOrder", "Y");
            delayNum = orderAuditService.countAuditByDestWork(auditParam);
            model.addAttribute("delayNum", delayNum);
        } else {
            delayNum = auditCount;
            model.addAttribute("delayNum", delayNum);
            auditParam.put("bespokeOrder", "");
            pendingNum = orderAuditService.countAuditByDestWork(auditParam);
            model.addAttribute("pendingNum", pendingNum);
        }
        model.addAttribute("inconfirmAuditNum", pendingNum + delayNum);
        //删除暂缓
        auditParam.remove("bespokeOrder");
        auditParam.remove("isReturnBack");

        //已回传库
        auditParam.put("isReturnBack", true);
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name());
        int inconfirmBackAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("inconfirmBackAuditNum", inconfirmBackAuditNum);
        auditParam.remove("isReturnBack");
        //满房库
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.FULL_AUDIT.name());
        int fullAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("fullAuditNum", fullAuditNum);

        //特满库
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT.name());
        int peculiarFullAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("peculiarFullAuditNum", peculiarFullAuditNum);

        //变价库
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT.name());
        int changePriceAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("changePriceAuditNum", changePriceAuditNum);

        //询位库
        auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INQUIRY_AUDIT.name());
        int inquiryAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("inquiryAuditNum", inquiryAuditNum);

        //其它预订通知
        auditParam.put("auditType", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
        auditParam.put("auditSubTypes", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name());
        int confirmOtherAuditNum = orderAuditService.countAuditByDestWork(auditParam);
        model.addAttribute("confirmOtherAuditNum", confirmOtherAuditNum);
        auditParam.remove("auditSubTypes");

        //酒店
        int hotelNum = pendingNum + delayNum + newOrderAuditNum + cancelAuditNum + changePriceAuditNum + fullAuditNum + peculiarFullAuditNum + inquiryAuditNum + confirmOtherAuditNum;
        model.addAttribute("hotelNum", hotelNum);

        //主单预订通知
        auditParam.remove("auditType");
        auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
        //主单预订通知不使用auditType
        auditParam.put("mainAudittype", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
        int mainNum = orderAuditService.countAuditByDestWork(auditParam);;
        auditParam.remove("mainAudittype");
        model.addAttribute("mainNum", mainNum);
    }

    /**
     * 修改子订单确认状态
     *
     * @param model
     * @param orderItemId   子订单ID
     * @param initStatus    页面初始化状态
     * @param updateStatus  更新状态
     * @param orderMemo     订单备注
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:36:36
     */
    @RequestMapping("/updateConfirmStatus.do")
    @ResponseBody
    public Object updateConfirmStatus(Model model, Long orderItemId, String initStatus, String updateStatus, String orderMemo, String confirmId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null || StringUtil.isEmptyString(updateStatus)) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId or confirmStatus is null");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId:" + orderItemId + ",confirmStatus:" + updateStatus);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }

            Confirm_Enum.CONFIRM_STATUS status = Confirm_Enum.CONFIRM_STATUS.getCode(updateStatus);
            if (status == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("状态参数错误");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }
            Confirm_Enum.CONFIRM_STATUS init_status = Confirm_Enum.CONFIRM_STATUS.getCode(initStatus);
            if (init_status == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("状态参数错误");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }
            //状态一致
            if (initStatus.equalsIgnoreCase(ordOrderItem.getConfirmStatus())) {
                String operateName = getLoginUserId();
                if (StringUtil.isEmptyString(orderMemo)) {
                    orderMemo = "";
                }

                if (StringUtil.isEmptyString(confirmId)) {
                    confirmId = "";
                }
                if (Confirm_Enum.CONFIRM_STATUS.INCONFIRM.name().equals(ordOrderItem.getConfirmStatus())) {
                    if(Confirm_Enum.CONFIRM_STATUS.FULL.name().equals(updateStatus)&&!ordOrderItem.hasSupplierApi()){
                        //1.自动关房该商品的订单，并禁售该商品
                        try{
                            ResultHandle result= ordItemConfirmStatusService.closeFullhotelAndForbidSale(ordOrderItem,getLoginUserId(), "满房自动关房(人工操作满房库)",Confirm_Enum.CONFIRM_STATUS.getCnName(updateStatus),null,null,null);
                            if(result.isFail()){
                                msg.setMessage("自动禁售商品:"+ordOrderItem.getSuppGoodsId()+"失败，需手动禁售！");
                            }
                        }catch(Exception e){
                            log.error("===orderItemId:"+ordOrderItem.getOrderItemId(),e);
                        }
                    }
                }
                ResultHandleT<ComAudit> handleT = ordItemConfirmStatusService.workbenchHandle(ordOrderItem, orderMemo, confirmId, status, operateName, null, null);
                if (handleT != null && handleT.isFail()) {
                    msg.setMessage(handleT.getMsg());
                }
            } else {
                //订单状态已更改，提示用户刷新后操作
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("订单状态已更改，请刷新后操作");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }

        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateConfirmStatus error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }


    /**
     * 修改订单备注，不更新状态
     * @param model
     * @param orderItemId
     * @param orderMemo
     * @param request
     * @return
     */
    @RequestMapping("/updateOrderMemo.do")
    @ResponseBody
    public Object updateOrderMemo(Model model, Long orderItemId, String orderMemo, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId:" + orderItemId + ", orderMemo="+orderMemo);
            if (orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId or confirmStatus is null");
                return msg;
            }
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }

            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = " ";
            }
        	ordOrderItem.setOrderMemo(orderMemo);
        	int result = ordOrderItemService.updateOrdOrderItem(ordOrderItem);
        	
            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    ordOrderItem.getOrderId(),
                    ordOrderItem.getOrderItemId(),
                    operateName,
                    "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                    orderMemo);

            if (result != 1) {
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus update orderMemo error orderItemId:" + orderItemId);
            }
            return msg;
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateConfirmStatus error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 修改子订单确认状态并发送邮件
     *
     * @param model
     * @param orderItemId
     * @param confirmStatus
     * @param orderMemo
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:36:36
     */
    @RequestMapping("/updateConfirmStatusAndSendEmail.do")
    @ResponseBody
    public Object updateConfirmStatusAndSendEmail(Model model, Long orderItemId, String confirmStatus, String orderMemo, String confirmId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null || StringUtil.isEmptyString(confirmStatus)) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId or confirmStatus is null");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId:" + orderItemId + ",confirmStatus:" + confirmStatus);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }

            Confirm_Enum.CONFIRM_STATUS status = Confirm_Enum.CONFIRM_STATUS.getCode(confirmStatus);
            if (status == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("状态参数错误");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }
            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = "";
            }
            //状态没有变化，更新备注
            if (confirmStatus.equals(ordOrderItem.getConfirmStatus())) {
                if (StringUtil.isNotEmptyString(orderMemo)) {
                    ordOrderItem.setOrderMemo(orderMemo);
                    int result = ordOrderItemService.updateOrdOrderItem(ordOrderItem);

                    lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                            ordOrderItem.getOrderId(),
                            ordOrderItem.getOrderItemId(),
                            operateName,
                            "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                            ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                            ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                            orderMemo);

                    if (result != 1) {
                        LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus update orderMemo error orderItemId:" + orderItemId);
                    }
                }
                return msg;
            }

            if (StringUtil.isEmptyString(confirmId)) {
                confirmId = "";
            }


            ResultHandleT<ComAudit> handleT = ordItemConfirmStatusService.workbenchHandle(ordOrderItem, orderMemo, confirmId, status, operateName, null, null);
            if (handleT != null && handleT.isFail()) {
                msg.setMessage(handleT.getMsg());
            }
            //如果修改状态为 满房、特殊满房、变价 发送邮件通知产品经理 2017年3月24日13:41:41 start
            if (Confirm_Enum.CONFIRM_STATUS.FULL.name().equals(confirmStatus)
                    || Confirm_Enum.CONFIRM_STATUS.PECULIAR_FULL.name().equals(confirmStatus)
                    || Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {
                //酒店、酒店套餐
                if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId()) || BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                	OrdOrder order=ordOrderService.loadOrderWithItemByOrderId(ordOrderItem.getOrderId());
                    ordItemConfirmEmailService.notifyManagerByEmailAddress(order,confirmStatus, ordOrderItem, operateName, orderMemo);
                }
            }
            // end
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateConfirmStatus error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 发送邮件给供应商
     * @return
     */
    @RequestMapping("/notifyManager.do")
    @ResponseBody
    public Object notifyManager(Model model, Long orderItemId, String confirmStatus, String orderMemo, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null || StringUtil.isEmptyString(confirmStatus)) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId or confirmStatus is null");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus orderItemId:" + orderItemId + ",confirmStatus:" + confirmStatus);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }

            Confirm_Enum.CONFIRM_STATUS status = Confirm_Enum.CONFIRM_STATUS.getCode(confirmStatus);
            if (status == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("状态参数错误");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }
            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = "";
            }

            if (StringUtil.isNotEmptyString(orderMemo)) {
                ordOrderItem.setOrderMemo(orderMemo);
                int result = ordOrderItemService.updateOrdOrderItem(ordOrderItem);

                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                        ordOrderItem.getOrderId(),
                        ordOrderItem.getOrderItemId(),
                        operateName,
                        "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                        orderMemo);

                if (result != 1) {
                    LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus update orderMemo error orderItemId:" + orderItemId);
                }
            }

            //如果修改状态为 满房、特殊满房、变价 发送邮件通知产品经理 2017年3月24日13:41:41 start
            if (Confirm_Enum.CONFIRM_STATUS.FULL.name().equals(confirmStatus)
                    || Confirm_Enum.CONFIRM_STATUS.PECULIAR_FULL.name().equals(confirmStatus)
                    || Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {
                //酒店、酒店套餐
                if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId()) || BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                	OrdOrder order=ordOrderService.loadOrderWithItemByOrderId(ordOrderItem.getOrderId());
                    ordItemConfirmEmailService.notifyManagerByEmailAddress(order,confirmStatus, ordOrderItem, operateName, orderMemo);
                }
            }
            // end
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateConfirmStatus error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 取消确认
     *
     * @param model
     * @param auditId
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:36:56
     */
    @RequestMapping("/orderCancelConfirm.do")
    @ResponseBody
    public Object orderCancelConfirm(Model model, Long orderItemId, Long auditId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (auditId == null || orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId + ",auditId:" + auditId);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction orderCancelConfirm ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            String operateName = getLoginUserId();
            ResultHandle handle = ordItemConfirmStatusService.cancelConfirm(auditId, operateName);
            if (handle.isFail()) {
                LOGGER.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId + ",cancelConfirm error!msg:" + handle.getMsg());
            }
            LOGGER.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId);
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction orderCancelConfirm error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 新订单重新发送通知
     *
     * @param model
     * @param orderItemId
     * @param request
     * @param auditId
     * @return
     * @author majunli
     * @date 2016-10-31 下午5:44:56
     */
    @RequestMapping("/resendNotification.do")
    @ResponseBody
    public Object resendNotification(Model model, Long orderItemId, Long auditId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction resendNotification orderItemId:" + orderItemId);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction resendNotification ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            String operateName = getLoginUserId();
            ResultHandle handle = ordItemConfirmStatusService.createConfirmOrder(ordOrderItem, CONFIRM_CHANNEL_OPERATE.CREATE, operateName);
            if (handle.isFail()) {
                LOGGER.info("OrderItemConfirmStatusAction resendNotification orderItemId:" + orderItemId + ",createConfirmOrder error!msg:" + handle.getMsg());
            } else {
                if (auditId != null) {
                    ComAudit comAudit = orderAuditService.queryAuditById(auditId);
                    if (comAudit != null && comAudit.getRemindTime() != null) {
                        Date now = new Date();
                        if (comAudit.getRemindTime().after(now)) {
                            msg.setMessage("此订单的提醒时间未到，将会显示在已审库-暂缓中");
                        }
                    }
                }
            }
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction resendNotification error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 显示暂缓对话框
     *
     * @param model
     * @param auditId
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:37:31
     */
    @RequestMapping("/showDelayRemindTimeDialog.do")
    public String showDelayRemindTimeDialog(Model model, String auditId, HttpServletRequest request) {
        if (StringUtil.isNotEmptyString(auditId)) {
            ComAudit comAudit = orderAuditService.queryAuditById(Long.parseLong(auditId));
            if (comAudit != null) {
                model.addAttribute("auditId", auditId);
                model.addAttribute("remindTime", DateUtil.formatDate(comAudit.getRemindTime(), DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss));
            }
        }
        return "/order/confirm/delay_remindTime_dialog";
    }

    /**
     * 更新暂缓提醒时间
     *
     * @param model
     * @param auditId
     * @param remindTimeStr
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:38:01
     */
    @RequestMapping("/updateDelayRemindTime.do")
    @ResponseBody
    public Object updateDelayRemindTime(Model model, String auditId, String remindTimeStr, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (StringUtil.isNotEmptyString(auditId) && StringUtil.isNotEmptyString(remindTimeStr)) {
                Date now = new Date();
                Date remindTime = DateUtil.getDateByStr(remindTimeStr, DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss);
                if (remindTime.after(now)) {
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("auditId", auditId);
                    param.put("remindTime", remindTime);
                    param.put("updateTime", now);
                    param.put("deferFlag", "Y");
                    //存入数据库
                    int upadteCount = orderAuditService.updateRemindTimeByAuditId(param);
                    if (upadteCount != 1) {
                        msg.setCode(ResultMessage.ERROR);
                        msg.setMessage("更新出现错误");
                    }
                }
            } else {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
            }
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateDelayRemindTime error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 显示取消订单对话框
     *
     * @param model
     * @param orderItemId
     * @param request
     * @return
     * @author majunli
     * @date 2016-11-3 下午6:29:06
     */
    @RequestMapping("/showCancelOrderDialog.do")
    public String showCancelOrderDialog(Model model, Long orderItemId, HttpServletRequest request) {
        if (orderItemId != null) {
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem != null) {
                OrdOrder order = ordOrderService.findByOrderId(ordOrderItem.getOrderId());
                if (order != null) {
                    Map<String, Object> dictDefPara = new HashMap<String, Object>();
                    dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
                    dictDefPara.put("cancelFlag", "Y");
                    List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
                    boolean isStart = false;
                    //当订单为目的地BU，且是酒店
                    if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())
                            && CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
                            && order.hasPayed()) {
                        //是否开启退款流程
                        isStart = orderRefundProcesserAdapter.isStartProcessByRefund(order, getLoginUserId());
                    }
                    List<BizDictDef> dictDefList = new ArrayList<BizDictDef>();
                    for (BizDictDef def : dictDefs) {
                        if (!isStart && VstOrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM_REFUND_PROCESS.equals(def.getDictDefId())) {
                            continue;
                        }
                        dictDefList.add(def);
                    }
                    model.addAttribute("orderCancelTypeList", dictDefList);
                    model.addAttribute("orderItemId", orderItemId);
                    model.addAttribute("order", order);
                    model.addAttribute("isSupplierOrder", order.isSupplierOrder() + "");
                }
            }
        }
        return "/order/confirm/cancelOrder_dialog";
    }

    /**
     * 取消订单
     *
     * @param model
     * @param orderId
     * @param cancelCode
     * @param cancleReasonText
     * @param orderRemark
     * @param request
     * @return
     * @author majunli
     * @date 2016-11-3 下午6:29:12
     */
    @RequestMapping("/cancelOrder.do")
    @ResponseBody
    public Object cancelOrder(Model model, Long orderId, String cancelCode, String cancleReasonText, String orderRemark, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderId != null && StringUtil.isNotEmptyString(cancelCode) && StringUtil.isNotEmptyString(cancleReasonText)) {
                if (StringUtil.isEmptyString(orderRemark)) {
                    orderRemark = "";
                }
                String loginUserId = this.getLoginUserId();
                LOGGER.info("OrderItemConfirmStatusAction cancelOrder loginUserId:" + loginUserId + " orderId:" + orderId);
                ResultHandle resultHandle = ordItemConfirmStatusService.cancelOrder(orderId, cancelCode, cancleReasonText, loginUserId, orderRemark);
                if (resultHandle.isFail()) {
                    msg.setCode(ResultMessage.ERROR);
                    msg.setMessage("取消出现异常" + resultHandle.getMsg());
                    LOGGER.info("OrderItemConfirmStatusAction cancelOrder orderId:" + orderId + ",cancelOrder error!msg:" + resultHandle.getMsg());
                }
            } else {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
            }
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction cancelOrder error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 查看传真回传
     *
     * @param model
     * @param orderId
     * @param orderItemId
     * @param certifId
     * @param req
     * @return
     * @author majunli
     * @date 2016-11-2 下午4:59:30
     */
    @RequestMapping("/showFaxRecvDialog.do")
    public String showFaxRecvDialog(Model model, Long orderId, Long orderItemId, Long certifId, HttpServletRequest req) {
        try {
            if (orderId != null && orderItemId != null && certifId != null) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("orderId", orderId);
                params.put("certifId", certifId);
                params.put("cancelFlag", "Y");
                ResultHandleT<List<EbkFaxRecv>> resultHandle = ebkFaxTaskClientService.selectEbkFaxRecvListByPrams(params);
                if (resultHandle.isSuccess()) {
                    List<EbkFaxRecv> ebkFaxRecvs = resultHandle.getReturnContent();
                    if (CollectionUtils.isNotEmpty(ebkFaxRecvs)) {
                        EbkFaxRecv targetEbkFaxRecv = ebkFaxRecvs.get(0);
                        List<EbkFaxRecvItem> targetEbkFaxRecvItems = targetEbkFaxRecv.getEbkFaxRecvItems();
                        if (CollectionUtils.isNotEmpty(targetEbkFaxRecvItems)) {
                            if (null != targetEbkFaxRecv) {
                                String fileUrl = targetEbkFaxRecv.getFileUrl();
                                // 未上传回传件不进入
                                if (StringUtil.isNotEmptyString(fileUrl)) {
                                    int index = fileUrl.lastIndexOf(".");
                                    if (index != -1) {
                                        String fileType = fileUrl.substring(index + 1);
                                        model.addAttribute("fileType", StringUtil.isNotEmptyString(fileType) ? fileType.toUpperCase() : "");
                                    }
                                }
                                // EBK回传分页信息大小
                                List<EbkFaxRecvItem> ebkFaxRecvItems = targetEbkFaxRecvItems;
                                if (null != ebkFaxRecvItems && !ebkFaxRecvItems.isEmpty()) {
                                    model.addAttribute("FaxRecvitemSize", ebkFaxRecvItems.size());
                                }

                                String readUserId = targetEbkFaxRecv.getReadUserId();// 第一查看人
                                if (StringUtil.isEmptyString(readUserId)) {
                                    //没有被查看，修改传真回传表第一查看人信息、与查看时间
                                    String userName = getLoginUserId();
                                    LOGGER.info("OrderItemConfirmStatusAction showFaxRecvDialog login userName is{" + userName + "} orderId:" + orderId);
                                    targetEbkFaxRecv.setReadUserId(userName);
                                    targetEbkFaxRecv.setReadTime(new Date());
                                    ebkFaxTaskClientService.updateEbkFaxRecv(targetEbkFaxRecv);
                                }
                                model.addAttribute("readUserId", readUserId);
                            }
                            String confirmStatus = "";
                            String orderMemo = "";
                            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
                            if (ordOrderItem != null) {
                                if (StringUtil.isNotEmptyString(ordOrderItem.getConfirmStatus())) {
                                    confirmStatus = ordOrderItem.getConfirmStatus();
                                }
                                if (StringUtil.isNotEmptyString(ordOrderItem.getOrderMemo())) {
                                    orderMemo = ordOrderItem.getOrderMemo();
                                }
                            }
                            model.addAttribute("ebkFaxRecv", targetEbkFaxRecv);
                            model.addAttribute("ebkFaxRecvItems", targetEbkFaxRecvItems);
                            model.addAttribute("FaxRecvitemSize", targetEbkFaxRecvItems.size());
                            model.addAttribute("orderItemId", orderItemId);
                            model.addAttribute("confirmStatus", confirmStatus);
                            model.addAttribute("orderMemo", orderMemo);
                        } else {
                            LOGGER.info("OrderItemConfirmStatusAction showFaxRecvDialog not found! orderItemId:" + orderItemId);
                        }
                    }
                } else {
                    LOGGER.info("OrderItemConfirmStatusAction showFaxRecvDialog not found! orderId:" + orderId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("OrderItemConfirmStatusAction showFaxRecvDialog error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return "/order/confirm/showFaxRecv";
    }

    /**
     * 询位单 审核通过
     *
     * @param model
     * @param orderItemId
     * @param request
     * @param orderMemo
     * @return
     * @author majunli
     * @date 2016-10-31 下午5:44:56
     */
    @RequestMapping("/orderPassInquiryAudit.do")
    @ResponseBody
    public Object orderPassInquiryAudit(Model model, Long orderItemId, String orderMemo, Long auditId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                return msg;
            }
            if (auditId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("活动ID不能为空");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction orderPassInquiryAudit orderItemId:" + orderItemId + ", auditId="+auditId);
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction orderPassInquiryAudit ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",ordOrderItem:" + JSON.toJSONString(ordOrderItem));
            if (Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name().equalsIgnoreCase(ordOrderItem.getConfirmStatus())) {
                String operateName = getLoginUserId();
                String resourceRetentionTime=request.getParameter("resourceRetentionTime");
                Long orderId=ordOrderItem.getOrderId();
                OrdOrder order=ordOrderService.findByOrderId(orderId);
                ResultHandle handle = ordItemConfirmStatusService.inquiryConfirm(ordOrderItem, auditId, operateName, orderMemo,resourceRetentionTime);
                LOGGER.info("OrderItemConfirmStatusAction orderPassInquiryAudit orderItemId:" + orderItemId + ",executeUpdateOrderResourceStatusAmple msg:" + handle.getMsg());

                if(handle.isFail()){
                    msg.setCode(ResultMessage.ERROR);
                    msg.setMessage(handle.getMsg());
                    return msg;
                }
                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                        ordOrderItem.getOrderId(),
                        ordOrderItem.getOrderItemId(),
                        operateName,
                        "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                        orderMemo);
            }else{
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("子订单确认状态已更改，请刷新页面");
                return msg;
            }

        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage(e.getMessage());
            LOGGER.error("OrderItemConfirmStatusAction orderPassInquiryAudit error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 更新传真回传(待确认)
     *
     * @param model
     * @param orderId
     * @param linkId
     * @param confirmStatus
     * @param confirmId
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:38:01
     */
    @RequestMapping("/updateFaxRecv.do")
    @ResponseBody
    public Object updateFaxRecv(Model model, Long orderId, Long orderItemId, Long linkId, String confirmStatus, String orderMemo, String confirmId, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderId == null || orderItemId == null || StringUtil.isEmptyString(confirmStatus)) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                return msg;
            }

            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction updateFaxRecv ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction updateFaxRecv ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateFaxRecv OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }

            Confirm_Enum.CONFIRM_STATUS status = Confirm_Enum.CONFIRM_STATUS.getCode(confirmStatus);
            if (status == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("状态参数错误");
                LOGGER.info("OrderItemConfirmStatusAction updateFaxRecv confirmStatus error orderItemId:" + orderItemId);
                return msg;
            }
            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = "";
            }

            if (StringUtil.isEmptyString(confirmId)) {
                confirmId = "";
            }

            //状态没有变化，更新备注
            if (confirmStatus.equals(ordOrderItem.getConfirmStatus())) {
                if (StringUtil.isNotEmptyString(orderMemo)) {
                    ordOrderItem.setOrderMemo(orderMemo);
                    int result = ordOrderItemService.updateOrdOrderItem(ordOrderItem);

                    lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                            ordOrderItem.getOrderId(),
                            ordOrderItem.getOrderItemId(),
                            operateName,
                            "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                            ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                            ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                            orderMemo);
                    if (result != 1) {
                        LOGGER.info("OrderItemConfirmStatusAction updateFaxRecv update orderMemo error orderItemId:" + orderItemId);
                    }
                }
                return msg;
            }

            if (StringUtil.isEmptyString(confirmId)) {
                confirmId = "";
            }

            if (Confirm_Enum.CONFIRM_STATUS.INCONFIRM.name().equals(ordOrderItem.getConfirmStatus())) {
                //已审更新状态
                ResultHandleT<ComAudit> handle = ordItemConfirmStatusService.updateInConfirmStatusByUser(
                        ordOrderItem, status, confirmId, operateName, orderMemo, linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL.FAX);
                if (handle.isSuccess() && handle.getReturnContent() != null) {
                    ordItemConfirmProcessService.completeTaskByAuditHasCompensated(ordOrderItem, handle.getReturnContent());
                } else {
                    LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus update updateInConfirmStatusByUser error!msg:" + handle.getMsg() + "orderItemId:" + orderItemId);
                }
            }
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction updateFaxRecv error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 联系供应商
     *
     * @param request
     * @param model
     * @param orderItemId
     * @return
     */
    @RequestMapping("/handleSupplier.do")
    @ResponseBody
    public Object handleSupplier(HttpServletRequest request, Model model, Long orderItemId, String orderMemo) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            if (orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                return msg;
            }
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderItemConfirmStatusAction handleSupplier ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderItemConfirmStatusAction handleSupplier ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderItemConfirmStatusAction updateConfirmStatus OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }
            OrdItemAddition ordItemAddition = ordItemAdditionService.findOrdItemAdditionById(orderItemId);
            if (ordItemAddition == null) {
                LOGGER.info("OrderItemConfirmStatusAction handleSupplier ordItemAddition is null orderItemId:" + orderItemId);
                //插入信息
                ordItemAddition = new OrdItemAddition();
                ordItemAddition.setOrderItemId(orderItemId);
                ordItemAddition.setHandleTime(new Date());
                ordItemAddition.setHandleUser(getLoginUserId());
                ordItemAdditionService.addOrdItemAddition(ordItemAddition);
            } else {
                LOGGER.info("OrderItemConfirmStatusAction handleSupplier ordItemAddition orderItemId:" + ordItemAddition.getOrderItemId());
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("供应商已联系，无需重复操作");
                return msg;
            }
            if (StringUtil.isNotEmptyString(orderMemo)) {
                ordOrderItem.setOrderMemo(orderMemo);
                int result = ordOrderItemService.updateOrdOrderItem(ordOrderItem);

                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                        ordOrderItem.getOrderId(),
                        ordOrderItem.getOrderItemId(),
                        this.getLoginUserId(),
                        "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                        orderMemo);

                if (result != 1) {
                    LOGGER.info("OrderItemConfirmStatusAction handleSupplier update orderMemo error orderItemId:" + orderItemId);
                }
            }
            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    ordOrderItem.getOrderId(),
                    ordOrderItem.getOrderItemId(),
                    this.getLoginUserId(),
                    "-电话确认",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    "子订单[" + ordOrderItem.getOrderItemId() + "]",
                    "工作台电话确认操作");
            return msg;
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderItemConfirmStatusAction handleSupplier error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 根据页面条件组装综合查询接口条件
     *
     * @param currentPage
     * @param pageSize
     * @param orderIds
     * @param orderItemIds
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:38:51
     */
    private ComplexQuerySQLCondition buildQueryCondition(Integer currentPage, Integer pageSize, Set<Long> orderIds, Set<Long> orderItemIds) {
        //保证每次请求都是一个新的对象
        ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
        //组装订单标志类条件
        condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
        condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
        condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
        condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
        condition.getOrderFlagParam().setOrderPackTableFlag(true);
        condition.getOrderFlagParam().setOrderPageFlag(false);//需要分页
        condition.getOrderFlagParam().setOrderStockTableFlag(true);
        //组装订单排序类条件
        condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
        //订单间夜数
        condition.getOrderRelationSortParam().setOrderHotelTimeRateSort("  ORD_ORDER_HOTEL_TIME_RATE.VISIT_TIME  ASC  ");
        //组装订单ID类条件
        condition.getOrderIndentityParam().setOrderIds(orderIds);
        condition.getOrderIndentityParam().setOrderItems(orderItemIds);
        return condition;
    }

    /**
     * 根据页面展示特色组装其想要的结果
     *
     * @param orderList
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:39:45
     */
    private List<OrderMonitorRst> buildQueryOrderItemResult(List<OrdOrder> orderList, HttpServletRequest request) {
        List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
        for (OrdOrder order : orderList) {
            for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
                if (ordOrderItem.getCategoryId() != 1L && ordOrderItem.getCategoryId() != 17L && ordOrderItem.getCategoryId() != 32L) {
                    continue;
                }
                OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
                orderMonitorRst.setIsTestOrder(order.getIsTestOrder()); //是否是测试单
                //订单来源
                orderMonitorRst.setDistributorName(order.getDistributorId()+"");
                //设置子订单号
                orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
                orderMonitorRst.setOrderId(order.getOrderId());
                orderMonitorRst.setOrderMemo(order.getOrderMemo());
                orderMonitorRst.setSuppGoodsId(ordOrderItem.getSuppGoodsId());
                orderMonitorRst.setProductName(this.buildProductName(order, ordOrderItem, request.getParameter("mainTab")));
                //设置渠道号
                orderMonitorRst.setDistributionChannel(calcOrderDistributionChannel(order));
                //结算价格
                orderMonitorRst.setSettlementPrice(this.buildSettlementPrice(ordOrderItem));
                if (request.getParameter("mainTab") != null && request.getParameter("mainTab").toString().equals("MAIN")) {
                    try {
                        //应收款
                        orderMonitorRst.setSettlementPrice(order.getOughtAmount().toString());

                        //已收款
                        orderMonitorRst.setActualSettlementPrice(order.getActualAmount().toString());
                    } catch (Exception e){
                        log.error("order amount exception.", e);
                    }

                }
                orderMonitorRst.setCreateTime(this.buildCreateTime(order.getCreateTime()));
                orderMonitorRst.setVisitTime(this.buildVisitTime(ordOrderItem));
                orderMonitorRst.setContactName(this.buildContactName(order, ordOrderItem.getOrderItemId()));
                if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                    orderMonitorRst.setCategoryType("category_hotel");
                    //酒店订单单价显示间夜数
                    orderMonitorRst.setQuantity(ordOrderItem.getQuantity());    //房间数量

                    //入住晚数
                    List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
                    OrdOrderHotelTimeRate lastOrderHotelTimeRate = new OrdOrderHotelTimeRate();
                    if (CollectionUtils.isNotEmpty(orderHotelTimeRateList)) {
                        lastOrderHotelTimeRate = orderHotelTimeRateList.get(orderHotelTimeRateList.size() - 1);
                    }
                    if (lastOrderHotelTimeRate.getVisitTime() != null) {
                        Date visitTime = DateUtils.addDays(lastOrderHotelTimeRate.getVisitTime(), 1);
                        lastOrderHotelTimeRate.setVisitTime(visitTime);

                        int arrivalDays = CalendarUtils.getDayCounts(ordOrderItem.getVisitTime(), visitTime);
                        orderMonitorRst.setArrivalDays(arrivalDays);
                    }
                } else if (BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())) {
                    orderMonitorRst.setCategoryType("category_route_hotelcomb");
                    orderMonitorRst.setQuantity(ordOrderItem.getQuantity());    //房间数量
                }
                //游客备注
                orderMonitorRst.setRemark(order.getRemark());
                OrdPerson orderPerson = order.getContactPerson();
                if (null != orderPerson) {
                    orderMonitorRst.setContactMobile(orderPerson.getMobile());
                }
                //显示担保信息
                orderMonitorRst.setGuarantee(order.getGuarantee());
                orderMonitorRst.setOrderSubType(order.getOrderSubType());
                //资源审核状态
                orderMonitorRst.setResourceStatus(ordOrderItem.getResourceStatus());
                //信息审核状态
                orderMonitorRst.setInfoStatus(ordOrderItem.getInfoStatus());
                //子订单审核状态
                orderMonitorRst.setOrderItemConfirmStatus(ordOrderItem.getConfirmStatus());
                //子订单后台备注
                orderMonitorRst.setOrderItemMemo(ordOrderItem.getOrderMemo());
                //子订单是否为对接
                if (ordOrderItem.isSupplierOrderItem()) {
                    orderMonitorRst.setIsSupplierOrderItem("Y");
                } else {
                    orderMonitorRst.setIsSupplierOrderItem("N");
                }
                //传真数量
                Integer recvCount = 0;
                Integer ebkMailCount = 0;
                String certifIdStr = "";
                if ("N".equals(orderMonitorRst.getIsSupplierOrderItem())) {
                    //子订单凭证
                    List<EbkCertifItem> list = ebkFaxTaskClientService.selectEbkCertifItemListByOrderItemId(ordOrderItem.getOrderItemId()).getReturnContent();
                    if (list != null && list.size() > 0) {
                        Long certifId = list.get(0).getCertifId();
                        if (certifId != null) {
                            certifIdStr = certifId.toString();
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("orderId", ordOrderItem.getOrderId());//订单号
                            params.put("certifId", list.get(0).getCertifId());//是否有效
                            params.put("cancelFlag", "Y");//是否有效

                            ResultHandleT<Integer> resultHandleInteger = ebkFaxTaskClientService.findEbkFaxRecvCount(params);
                            if (resultHandleInteger.isSuccess()) {
                                recvCount = resultHandleInteger.getReturnContent();
                            }

                            ResultHandleT<Integer> resultHandleInteger1 = ebkMailTaskClientService.findEbkMailRecvCount(params);
                            if (resultHandleInteger1.isSuccess()) {
                                ebkMailCount = resultHandleInteger1.getReturnContent();
                            }
                        }
                    }
                }
                orderMonitorRst.setEbkFaxCount(recvCount);
                orderMonitorRst.setEbkMailCount(ebkMailCount);
                orderMonitorRst.setCertifId(certifIdStr);
                //所属BU
                ResultHandleT<BizBuEnum> resultHandleT = bizBuEnumClientService.getBizBuEnumByBuCode(ordOrderItem.getBuCode());
                if (resultHandleT.isSuccess()) {
                    orderMonitorRst.setBelongBU(resultHandleT.getReturnContent().getCnName());
                }
                //保留房标识
                if (OrdOrderUtils.isHotelItem(ordOrderItem)) {
                    String res = ordOrderItem.getRoomReservations();
                    if (StringUtil.isEmptyString(res)) {
                        res = "non";
                    }
                    orderMonitorRst.setStockFlag(res);
                }
                /*查询供应商 start*/
                ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(ordOrderItem.getSupplierId());
                if (resultHandleSuppSupplier.isSuccess()) {
                    SuppSupplier suppSupplier = resultHandleSuppSupplier.getReturnContent();
                    if (null != suppSupplier && !StringUtils.isEmpty(suppSupplier.getSupplierName())) {
                        orderMonitorRst.setSupplierName(suppSupplier.getSupplierName());
                        orderMonitorRst.setSupplierId(suppSupplier.getSupplierId());
                        orderMonitorRst.setTel(suppSupplier.getTel());
                    }
                }
				/*查询供应商 end*/
				/*查询子订单附加信息*/
                Long ts = System.currentTimeMillis();
                OrdItemAddition ordItemAddition = ordItemAdditionService.findOrdItemAdditionById(ordOrderItem.getOrderItemId());
                if (ordItemAddition != null && ordItemAddition.getHandleTime() != null)
                    orderMonitorRst.setIsHandleSupplier("Y");
                LOGGER.info("ordItemAdditionService.findOrdItemAdditionById TimeMillis=" + (System.currentTimeMillis() - ts));
                /*end*/
                resultList.add(orderMonitorRst);
            }
        }
        return resultList;
    }

    /**
     * 组装分页结果
     *
     * @param list
     * @param currentPage
     * @param pageSize
     * @param totalCount
     * @param request
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:40:25
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Page buildResultPage(List list, Integer currentPage, Integer pageSize, Long totalCount, HttpServletRequest request) {
        // 如果当前页是空，默认为1
        Integer currentPageTmp = currentPage == null ? 1 : currentPage;
        // 从配置文件读取分页大小
        Integer defaultPageSize = DEFAULT_PAGE_SIZE;
        Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
        // 构造分页对象
        Page page = Page.page(totalCount, pageSizeTmp, currentPageTmp);
        // 构造分页URL
        page.buildUrl(request);
        // 设置结果集
        page.setItems(list);
        return page;
    }

    /**
     * 根据订单和订单子项商品名称
     *
     * @param orderItem
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:41:35
     */
    private String buildProductName(OrdOrder order, OrdOrderItem orderItem, Object mainTab) {
        String productName = "未知产品名称";
        if (null != orderItem) {
            if (mainTab == null || mainTab == "" || "HOTEL".equals(mainTab)) {
                productName = orderItem.getProductName() +  "-" + orderItem.getSuppGoodsName();
            } else {
                //主单预订通知取主单产品名称
                productName = order.getOrderProductName();
            }
        }
        return productName;
    }

    /**
     * 主单预订通知显示订单实付金额
     * @param order
     * @return
     */
    private String buildSettlementPrice(OrdOrderItem orderItem) {
        String settlementPrice = "";
        settlementPrice = orderItem.getSettlementPrice().toString();
        return settlementPrice;
    }

    /**
     * 处理下单时间（格式：n天n时n分）
     *
     * @param createTime
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:44:59
     */
    private String buildCreateTime(Date createTime) {
        String createTimeStr = "未知下单时间";

        if (null != createTime) {
            // 保留年月日时分
            createTimeStr = DateUtil.dateDiff(createTime, new Date());
        }
        return createTimeStr;
    }

    /**
     * 根据订单和订单子项构建入离时间
     *
     * @param orderItem
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:47:27
     */
    private String buildVisitTime(OrdOrderItem orderItem) {
        String visitTime = "未知日期";
        if (null != orderItem) {
            List<OrdOrderHotelTimeRate> orderHotelTimeRate = orderItem.getOrderHotelTimeRateList();
            String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
            visitTime = firstDay;
            if (null != orderHotelTimeRate && orderHotelTimeRate.size() > 0) {
                String lastDay = DateUtil.formatDate(DateUtil.dsDay_Date(orderItem.getVisitTime(), orderHotelTimeRate.size()), "yyyy-MM-dd");
                visitTime += "<br>" + lastDay;
            }
        }
        return visitTime;
    }

    /**
     * 处理联系人
     *
     * @param order
     * @param orderItemId
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:47:55
     */
    private String buildContactName(OrdOrder order, Long orderItemId) {
        String contactTel = "";
        OrdPerson orderPerson = order.getContactPerson();
        if (null != orderPerson) {
            contactTel = orderPerson.getMobile();

        }
        //游客姓名
        String personNames = "";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
        params.put("objectId", order.getOrderId());
        params.put("personType", ORDER_PERSON_TYPE.TRAVELLER.name());
        List<OrdPerson> travellerPersonList = ordPersonService.findOrdPersonList(params);
        if (CollectionUtils.isNotEmpty(travellerPersonList)) {
            for (OrdPerson ordPerson : travellerPersonList) {
                if (ordPerson != null && StringUtil.isNotEmptyString(ordPerson.getFullName())) {
                    if ("".equals(personNames)) {
                        personNames = ordPerson.getFullName();
                    } else {
                        personNames = personNames + "," + ordPerson.getFullName();
                    }
                }
            }
        }
        personNames = personNames + "<br>" + contactTel;
        return personNames;
    }

    /**
     * 同供应商订单查询
     *
     * @param model
     * @param page
     * @param pageSize
     * @param monitorCnd
     * @param checkedTab
     * @param isDelay
     * @param mainCheckedTab
     * @param mainTab
     * @param supplierId
     * @param req
     * @param res
     * @return
     * @author fangmeixiu
     * @date 2017-09-08 下午16:40:53
     */
    @RequestMapping(value = "/querySameSupplierOrderList.do")
    public String querySameSupplier(Model model, Integer page, Integer pageSize, OrderMonitorCnd monitorCnd, String operatorName,
                                    String checkedTab, String isDelay, String mainCheckedTab, String mainTab,Long supplierId,String supplierName,HttpServletRequest req, HttpServletResponse res) {
        try {
            long startTimeMillis = System.currentTimeMillis();
            //是否保留房
            Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
            stockFlagMap.put("", "全部");
            stockFlagMap.put("Y", "保留房");
            stockFlagMap.put("N", "非保留房");
            model.addAttribute("stockFlagMap", stockFlagMap);
            //组装订单审核列表条件
            Map<String, Object> auditParam = new HashMap<String, Object>();
            auditParam.put("categoryIds", categoryIds);
            //订单负责人 dongningbo 切换工作台保留订单负责人
            if (StringUtil.isEmptyString(operatorName)) {
                operatorName = getLoginUserId();
            }
            if (!"admin".equals(operatorName)) {
                auditParam.put("operatorName", operatorName);
            }
            model.addAttribute("operatorName", operatorName);



            //待处理和暂缓bespokeOrder=Y 显示暂缓，为""显示待处理，点击已审库是始终显示待处理(暂缓参数isDelay)

            String bespokeOrder = "";
            if ("N".equals(isDelay)) {
                monitorCnd.setBespokeOrder(bespokeOrder);
            } else {
                if (StringUtil.isNotEmptyString(monitorCnd.getBespokeOrder())) {
                    bespokeOrder = monitorCnd.getBespokeOrder();
                } else {
                    monitorCnd.setBespokeOrder(bespokeOrder);
                }
            }

            //计算各tab的记录数
            countTabs(model, auditParam, bespokeOrder);
            String orderby = "COM_AUDIT.CREATE_TIME asc";
            //选择 酒店 或 主单预订通知

            if (StringUtil.isEmptyString(mainTab) || "HOTEL".equals(mainTab)) {
                auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
                // 选择tab
                if (StringUtil.isNotEmptyString(checkedTab)) {
                    switchTheTab(model, auditParam, checkedTab);
                } else {
                    //默认显示已审
                    checkedTab = Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name();
                    switchTheTab(model, auditParam, checkedTab);
                    model.addAttribute("checkedTab", checkedTab);
                }
                //只有已审时才显示暂缓相关
                if (checkedTab.equals(Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name())) {
                    auditParam.put("bespokeOrder", bespokeOrder);
                    orderby = orderby + ",COM_AUDIT.SEQ desc";
                }
            } else {
                model.addAttribute("mainTab", mainTab);
                auditParam.remove("auditType");
                auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
                auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
                auditParam.put("mainAudittype", Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
            }

            //只有已审时才显示暂缓相关
            if (checkedTab.equals(Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name())) {
                auditParam.put("bespokeOrder", bespokeOrder);
                orderby = "COM_AUDIT.SEQ desc,"+orderby;
            }
            auditParam.put("_orderby", orderby);

            String logs = "select with stockFlag and orderItemId params:";

            //保留房
            if (StringUtil.isNotEmptyString(monitorCnd.getStockFlag())) {
                if ("Y".equals(monitorCnd.getStockFlag())) {
                    auditParam.put("stockFlag", "-1");
                } else if ("N".equals(monitorCnd.getStockFlag())) {
                    auditParam.put("stockFlag", "-2");
                }
                logs = logs + auditParam.get("stockFlag");
            }

            //子订单ID
            if (monitorCnd.getOrderItemId() != null) {
                auditParam.put("orderItemId", monitorCnd.getOrderItemId());
                logs = logs + "," + auditParam.get("orderItemId");
                LOGGER.info(logs);
            }
            //添加供应商id条件
            auditParam.put("supplierId",supplierId);
            //总记录数


            int auditTotalCount =  orderAuditService.countAuditByDestWork(auditParam);
            int currentPage = page == null ? 1 : page;
            int currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

            Page<ComAuditInfo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
            pageParam.buildUrl(req);

            auditParam.put("_start", pageParam.getStartRows());
            auditParam.put("_end", pageParam.getEndRows());
            //查询订单审核列表集合
            List<ComAudit> auditList = orderAuditService.queryDestAuditListByCriteria(auditParam);
            Set<Long> orderItemIds = new TreeSet<Long>();
            orderItemIds.add(0L);
            Set<Long> orderIds = new TreeSet<Long>();   //主单预订通知
            orderIds.add(0L);
            for (ComAudit comAudit : auditList) {
                if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())) {
                    orderIds.add(comAudit.getObjectId());
                } else {
                    orderItemIds.add(comAudit.getObjectId());
                }
            }

            if (orderIds.size() != 1 && orderItemIds.size() == 1) {
                orderItemIds = null;
            } else {
                orderIds = null;
            }

            /** 查询订单信息 Start*/
            // 根据页面条件组装综合查询接口条件
            ComplexQuerySQLCondition orderItemCondition = buildQueryCondition(0, 0, orderIds, orderItemIds);
            // 根据条件获取订单集合
            List<OrdOrder> orderItemList = complexQueryService.queryOrderListByCondition(orderItemCondition);

            for (OrdOrder ordOrder:orderItemList
                    ) {
                ordOrder.getOrderItemList();

            }




            // 根据页面展示特色组装其想要的结果
            List<OrderMonitorRst> orderItemResultList = buildQueryOrderItemResult(orderItemList, req);
            //将订单转化为map,方便数据整合

            Map<Long, OrderMonitorRst> orderItemResultMap = new HashMap<Long, OrderMonitorRst>(orderItemList.size() * 2);
            for (OrderMonitorRst orderMonitorRst : orderItemResultList) {
                if (orderIds != null && orderIds.contains(orderMonitorRst.getOrderId())) {
                    orderItemResultMap.put(orderMonitorRst.getOrderId(), orderMonitorRst);
                } else {
                    orderItemResultMap.put(orderMonitorRst.getOrderItemId(), orderMonitorRst);
                }
            }


            model.addAttribute("supplierName",orderItemResultList.get(0).getSupplierName());

            /** 查询订单信息 End*/

            //将订单对象整合到审核对象里
            List<ComAuditInfo> comAuditInfoList = new ArrayList<ComAuditInfo>();
            for (ComAudit comAudit : auditList) {
                ComAuditInfo comAuditInfo = new ComAuditInfo();
                BeanUtils.copyProperties(comAudit, comAuditInfo);
                comAuditInfo.setOrderMonitorRst(orderItemResultMap.get(comAudit.getObjectId()));
                //dongningbo 工作台【下单时长】的数据显示取消，替换为【入库时长】显示内容为订单进入员工工作台时间。显示格式不变。
                if (comAudit.getRemindTime() != null) {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getRemindTime()));
                } else {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getCreateTime()));
                }
                if (StringUtil.isNotEmptyString(checkedTab) && "CONFIRM_OTHER_AUDIT".equals(checkedTab)) {
                    Map<String, Object> messageParams = new HashMap<String, Object>();
                    messageParams.put("auditId", comAudit.getAuditId());
                    List<ComMessage> comMessageList = comMessageService.findComMessageList(messageParams);
                    if (CollectionUtils.isNotEmpty(comMessageList)) {
                        comAuditInfo.setComMessage(comMessageList.get(0));
                    }
                }

                comAuditInfoList.add(comAuditInfo);
            }
            // 组装分页结果
            @SuppressWarnings("rawtypes")
            Page resultPage = buildResultPage(comAuditInfoList, currentPage, pageSize, NumberUtils.toLong(auditTotalCount + "", 0), req);
            // 存储分页结果
            model.addAttribute("resultPage", resultPage);

            LOGGER.info("OrderItemConfirmStatusAction.queryDestTaskList TimeMillis="+(System.currentTimeMillis()-startTimeMillis));
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("服务器内部异常");
            }
            LOGGER.error(ExceptionFormatUtil.getTrace(e));
        }
        // 查询条件回显
        model.addAttribute("monitorCnd", monitorCnd);


        return "/order/confirm/supplierOrderList";
    }



    /**
     * 显示订单的资源保留时间
     *
     * @param order
     * @param orderItemId
     * @return
     * @author fangmeixiu
     * @date 2017-09-14 上午10:44:55
     */
    @RequestMapping(value = "/showUpdateRetentionTime")
    public String showUpdateRetentionTime(Model model, HttpServletRequest request,Long orderItemId,Long orderId){
        if (LOGGER .isDebugEnabled()) {
            LOGGER .debug("start method<showUpdateRetentionTime>");
        }

        OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);

        Map<String,Object> contentMap = orderItem.getContentMap();
        String resourceRetentionTime =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());

        model.addAttribute("resourceRetentionTime", resourceRetentionTime);
        model.addAttribute("kssj",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm")+":00");
        model.addAttribute("jssj",DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd")+" 23:59:00");
        model.addAttribute("orderMemo",request.getParameter("orderMemo"));
        model.addAttribute("auditId",request.getParameter("auditId"));
        model.addAttribute("orderItemId",orderItemId);

        return "/order/confirm/showUpdateRetentionTime";
    }


    /**
     * 员工库查询
     *
     * @param model
     * @param page
     * @param pageSize
     * @param monitorCnd
     * @param checkedTab
     * @param isDelay
     * @param mainCheckedTab
     * @param req
     * @param res
     * @return
     * @author majunli
     * @date 2016-10-28 上午11:29:53
     */
    @RequestMapping(value = "/queryDestHotelTaskList.do")
    public String queryDestHotelTaskList(Model model, Integer page, Integer pageSize, OrderMonitorCnd monitorCnd, String orderChannel,
    		HttpServletRequest req, HttpServletResponse res) {
        try {
            long startTimeMillis = System.currentTimeMillis();
            //组装订单审核列表条件
            Map<String, Object> auditParam = new HashMap<String, Object>();
            auditParam.put("categoryIds", categoryIds);
            
            //获取供应商Id
            if(monitorCnd.getSupplierId()!=null){
            	auditParam.put("supplierId", monitorCnd.getSupplierId());
            }
            //资源审核状态
            if(StringUtil.isNotEmptyString(monitorCnd.getResourceStatus())){
            	auditParam.put("resourceStatus",monitorCnd.getResourceStatus());
            }
            
            //获取入住时间
            if(monitorCnd.getVisitTimeBegin()!=null){
            	auditParam.put("visitTime",monitorCnd.getVisitTimeBegin());
            }
            //获取渠道
            if(StringUtil.isNotEmptyString(orderChannel)){
            	List<Long> list=new ArrayList<Long>();
            	if(orderChannel.indexOf("neither")!=-1
            			&&orderChannel.indexOf("taobao")!=-1
            			&&orderChannel.indexOf("other")!=-1){
            		
            	}else{
            		auditParam.put("channel",true);
            		boolean isvst=false;
            		boolean istnt=false;
            		boolean istaobao=false;
	            	if(orderChannel.indexOf("neither")!=-1){
	            		isvst=true;
	            	}
	            	if(orderChannel.indexOf("other")!=-1){
	            		istnt=true;
	            	}
	            	if(orderChannel.indexOf("taobao")!=-1){
	            		istaobao=true;
	            	}
	            	//主站&分销
	            	if(isvst&&istnt){
	            		auditParam.put("VSTAndTNT",true);
	            		auditParam.put("distributorlist",Arrays.asList(Constant.DIST_BACK_END,Constant.DIST_FRONT_END,Constant.DIST_O2O_APP_SELL,Constant.DIST_O2O_SELL,Constant.DIST_BRANCH_SELL));
	            		auditParam.put("channeltaobao",DISTRIBUTOR_CODE_TAOBAO);
	            	}
	            	//主站&淘宝
	            	else if(isvst&&istaobao){
	            		auditParam.put("distributorlist",Arrays.asList(Constant.DIST_BACK_END,Constant.DIST_FRONT_END,Constant.DIST_O2O_APP_SELL,Constant.DIST_O2O_SELL));
	            		auditParam.put("distributionIdFour",Constant.DIST_BRANCH_SELL);
	            		auditParam.put("distributionChannel",Arrays.asList(10000L,107L,108L,110L,10001L,10002L));
	            		auditParam.put("channeltaobao",DISTRIBUTOR_CODE_TAOBAO);
	            		auditParam.put("VSTAndTaobao",true);
	            	}
	            	//淘宝&分销
	            	else if(istnt&&istaobao){
	            		auditParam.put("distributorlist",Arrays.asList(Constant.DIST_BRANCH_SELL));
	            		auditParam.put("distributionChannel",Arrays.asList(10000L,107L,108L,110L,10001L,10002L));
	            		auditParam.put("channeltaobao",DISTRIBUTOR_CODE_TAOBAO);
	            		auditParam.put("TNTAndTaobao",true);
	            	}else if(isvst){
	            		auditParam.put("distributorlist",Arrays.asList(Constant.DIST_BACK_END,Constant.DIST_FRONT_END,Constant.DIST_O2O_APP_SELL,Constant.DIST_O2O_SELL));
	            		auditParam.put("distributionChannel",Arrays.asList(10000L,107L,108L,110L,10001L,10002L));
	            		auditParam.put("distributionIdFour",Constant.DIST_BRANCH_SELL);
	            		auditParam.put("isvst",true);
	            	}else if(istnt){
	            		auditParam.put("distributorlist",Arrays.asList(Constant.DIST_BRANCH_SELL));
	            		auditParam.put("distributionChannel",Arrays.asList(10000L,107L,108L,110L,10001L,10002L));
	            		auditParam.put("channeltaobao",DISTRIBUTOR_CODE_TAOBAO);
	            		auditParam.put("istnt",true);
	            	}else if(istaobao){
	            		auditParam.put("channeltaobao",DISTRIBUTOR_CODE_TAOBAO);
	            		auditParam.put("istaobao",true);
	            	}
	            	
            	}
            	model.addAttribute("orderChannel",orderChannel);
            }
            
            String orderby = " c.seq desc";

            auditParam.put("_orderby", orderby);
            String logs = "select with stockFlag and orderItemId params:";

            //订单ID
            if (monitorCnd.getOrderId() != null) {
                auditParam.put("orderId", monitorCnd.getOrderId());
                logs = logs + "," + auditParam.get("orderId");
                LOGGER.info(logs);
            }
            //订单ID
            if (monitorCnd.getOrderItemId() != null) {
                auditParam.put("orderItemId", monitorCnd.getOrderItemId());
                logs = logs + "," + auditParam.get("orderItemId");
                LOGGER.info(logs);
            }
            //总记录数
            int auditTotalCount = orderAuditService.queryOrderListCount(auditParam);
            int currentPage = page == null ? 1 : page;
            int currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

            Page<ComAuditInfo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
            pageParam.buildUrl(req);

            auditParam.put("_start", pageParam.getStartRows());
            auditParam.put("_end", pageParam.getEndRows());

            //查询订单审核列表集合
            List<ComAudit> auditList = orderAuditService.queryOrderAuditList(auditParam);

            Set<Long> orderItemIds = new TreeSet<Long>();
            orderItemIds.add(0L);
            Set<Long> orderIds = new TreeSet<Long>();   //主单预订通知
            orderIds.add(0L);
            for (ComAudit comAudit : auditList) {
                if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())) {
                    orderIds.add(comAudit.getObjectId());
                } else {
                    orderItemIds.add(comAudit.getObjectId());
                }
            }

            if (orderIds.size() != 1 && orderItemIds.size() == 1) {
                orderItemIds = null;
            } else {
                orderIds = null;
            }

            /** 查询订单信息 Start*/
            // 根据页面条件组装综合查询接口条件
            ComplexQuerySQLCondition orderItemCondition = buildQueryCondition(0, 0, orderIds, orderItemIds);
            // 根据条件获取订单集合
            List<OrdOrder> orderItemList = complexQueryService.queryOrderListByCondition(orderItemCondition);
            // 根据页面展示特色组装其想要的结果
            List<OrderMonitorRst> orderItemResultList = buildQueryOrderItemResult(orderItemList, req);
            //将订单转化为map,方便数据整合
            Map<Long, OrderMonitorRst> orderItemResultMap = new HashMap<Long, OrderMonitorRst>(orderItemList.size() * 2);
            for (OrderMonitorRst orderMonitorRst : orderItemResultList) {
                if (orderIds != null && orderIds.contains(orderMonitorRst.getOrderId())) {
                    orderItemResultMap.put(orderMonitorRst.getOrderId(), orderMonitorRst);
                } else {
                    orderItemResultMap.put(orderMonitorRst.getOrderItemId(), orderMonitorRst);
                }
            }
            /** 查询订单信息 End*/

            //将订单对象整合到审核对象里
            List<ComAuditInfo> comAuditInfoList = new ArrayList<ComAuditInfo>();
            for (ComAudit comAudit : auditList) {
                ComAuditInfo comAuditInfo = new ComAuditInfo();
                BeanUtils.copyProperties(comAudit, comAuditInfo);
                comAuditInfo.setOrderMonitorRst(orderItemResultMap.get(comAudit.getObjectId()));
                //dongningbo 工作台【下单时长】的数据显示取消，替换为【入库时长】显示内容为订单进入员工工作台时间。显示格式不变。
                if (comAudit.getRemindTime() != null) {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getRemindTime()));
                } else {
                    comAuditInfo.setAuditCreateTime(this.buildCreateTime(comAudit.getCreateTime()));
                }
                comAuditInfoList.add(comAuditInfo);
            }
            // 组装分页结果
            @SuppressWarnings("rawtypes")
            Page resultPage = buildResultPage(comAuditInfoList, currentPage, pageSize, NumberUtils.toLong(auditTotalCount + "", 0), req);
            // 存储分页结果
            model.addAttribute("resultPage", resultPage);
            LOGGER.info("OrderItemConfirmStatusAction.queryDestTaskList TimeMillis="+(System.currentTimeMillis()-startTimeMillis));
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("服务器内部异常");
            }
            LOGGER.error(ExceptionFormatUtil.getTrace(e));
        }
        // 查询条件回显
        model.addAttribute("monitorCnd", monitorCnd);
        return "/order/confirm/queryInconfirm";
    }
    
    @RequestMapping(value = "/queryInconfirm.do")
    public String redictDestTask(Model model,HttpServletRequest req, HttpServletResponse res) {
        model.addAttribute("monitorCnd", new OrderMonitorCnd());
    	return "/order/confirm/queryInconfirm";
    }
    
    private String calcOrderDistributionChannel(OrdOrder order){
    	Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
    	if(order!=null){
    		if(null!=order.getDistributorCode()&&DISTRIBUTOR_CODE_TAOBAO.equals(order.getDistributorCode())){
    			return "taobao";
    		}else if(Constant.DIST_O2O_SELL==order.getDistributorId() ||
    				Constant.DIST_O2O_APP_SELL == order.getDistributorId()){
    			return "O2O";
    		}else if(Constant.DIST_BACK_END==order.getDistributorId()
    				||Constant.DIST_FRONT_END==order.getDistributorId()
    				||(null!=order.getDistributionChannel()&&ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue()))){
    			
    		}else{
    			return "other";
    		}
    	}
    	return null;
    }
    @RequestMapping(value = "/queryHotelInfo.do")
    public String queryHotelInfoAndSuppGoodsInfo(Model model,HttpServletRequest req, HttpServletResponse res,Long orderItemId,String checkedTab){
    	if(orderItemId==null){
    		throw new BusinessException("子订单Id为空");
    	}
    	model.addAttribute("checkedTab",checkedTab);
    	OrdOrderItem orderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
    	if(orderItem!=null){
    		SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId()).getReturnContent();
    		orderItem.setSupplierName(suppSupplier.getSupplierName());
    		model.addAttribute("ordOrderItem", orderItem);
    		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
    			List<Date> leaveDateList=ordOrderHotelTimeRateService.findOrdOrderItemHotelLastLeaveTimeByItemId(orderItem.getOrderItemId());
                List<String> strList= Lists.newArrayList();
                if(leaveDateList.size() > 0){
    			    for(Date date:leaveDateList){
                        strList.add(DateFormatUtils.format(date,"yyyy-MM-dd"));
                    }
                }
                if(strList.size() > 0){
                    model.addAttribute("orditemVisitTime", JSONArray.fromObject(strList).toString());
                }
    			//查询商品list信息
   /* 			com.lvmama.dest.api.common.ResponseBody<List<HotelGoodsVstVo>> responseBody=hotelGoodsQueryVstApiService.findSuppGoodsByProductId(new RequestBody<Long>().setTFlowStyle(orderItem.getProductId(), Constant.DEST_BU_HOTEL_TOKEN));
        		if(responseBody!=null&&responseBody.isSuccess()){
        			List<Long> suppGoodsIds=new ArrayList<>();
        			List<HotelGoodsVstVo> list=responseBody.getT();
        			for (HotelGoodsVstVo hotelGoodsVstVo : list) {
        				if(hotelGoodsVstVo.getSupplierId().equals(orderItem.getSupplierId())){
        					suppGoodsIds.add(hotelGoodsVstVo.getSuppGoodsId());
        				}
    				}*/
                	SuppGoodsParam param=new SuppGoodsParam();
                	param.setProductBranch(true);
                	com.lvmama.dest.api.common.ResponseBody<List<HotelGoodsBranchVstVo>> suppGoodsListResult=hotelGroupPackGoodslVstApiService.findBranchAndGoodsByProductId(new RequestBody<Long>().setTFlowStyle(orderItem.getProductId(),Constant.DEST_BU_HOTEL_TOKEN));
        	    	if(suppGoodsListResult!=null&&suppGoodsListResult.isSuccess()){
        	    		/*List<SuppGoods> listGoods=new ArrayList<>();
        	    		for (SuppGoods suppGoods : suppGoodsListResult.getReturnContent()) {
        	    			if(suppGoods==null || suppGoods.getSuppGoodsId()==null || !"Y".equals( suppGoods.getCancelFlag())){
        	    				continue;
        	    			}
        	    			listGoods.add(suppGoods);
						}*/
        	    		model.addAttribute("suppGoodsList", suppGoodsListResult.getT());
        	    	}
        		//}
    		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
    			model.addAttribute("orditemVisitTime", JSONArray.fromObject( Arrays.asList(DateFormatUtils.format(orderItem.getVisitTime()," yyyy-MM-dd"))).toString());
    			//查询商品信息
    			Map<String, Object> paramsMap=new HashMap<>();
    	    	paramsMap.put("supplierId", orderItem.getSupplierId());
    	    	paramsMap.put("productId", orderItem.getProductId());
    	    	ResultHandleT<List<SuppGoods>> suppGoodsListResult=suppGoodsClientService.findSuppGoodsListbySupplier(paramsMap);
    	    	if(suppGoodsListResult!=null&&suppGoodsListResult.isSuccess()){
    	    		List<SuppGoods> listGoods=new ArrayList<>();
    	    		for (SuppGoods suppGoods : suppGoodsListResult.getReturnContent()) {
    	    			if(suppGoods==null || suppGoods.getSuppGoodsId()==null || !"Y".equals( suppGoods.getCancelFlag())){
    	    				continue;
    	    			}
    	    			listGoods.add(suppGoods);
					}
    	    		model.addAttribute("suppGoodsList", listGoods);
    	    	}
    		}
    	}
    	return "/order/confirm/library/closeHouse_dialog";
    }
    @RequestMapping(value = "/closeHouse.do")
    @ResponseBody
    public Object closeHouse(HttpServletRequest req, HttpServletResponse res,Long orderItemId,String suppGoodsIdListStr,String closeDateListStr,String sourceType,String orderMemo) throws Exception{
    	if(orderItemId==null){
    		throw new BusinessException("子订单Id为空");
    	}
    	if(StringUtil.isEmptyString(suppGoodsIdListStr)){
    		throw new BusinessException("未勾选商品");
    	}
    	if(StringUtil.isEmptyString(closeDateListStr)){
    		throw new BusinessException("未选择时间商品");
    	}
    	String[] dateListStr=closeDateListStr.split(",");
    	List<Date> listDate=new ArrayList<>(); 
    	for (String dateStr : dateListStr) {
    		listDate.add(DateUtil.parse(dateStr, DateUtil.PATTERN_yyyy_MM_dd));
		}
    	String[] suppGoodsIdlist =suppGoodsIdListStr.split(",");
    	List<Long> listGoodsList=new ArrayList<>();
    	for (String str : suppGoodsIdlist) {
    		listGoodsList.add(Long.parseLong(str));
		}
    	OrdOrderItem ordOrderItem=ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		ResultHandle result= ordItemConfirmStatusService.closeFullhotelAndForbidSale(ordOrderItem, getLoginUserId(), "手动关房",sourceType,listDate,listGoodsList,orderMemo);
		if(result.isFail()){
			result.setMsg("自动禁售商品:"+ordOrderItem.getSuppGoodsId()+"失败，需手动禁售！");
			return result;
		}
		if (StringUtil.isNotEmptyString(orderMemo)) {
			ordOrderItem.setOrderMemo(orderMemo);
			ordOrderItemService.updateOrdOrderItem(ordOrderItem);
		}
    	return result;
    }
}