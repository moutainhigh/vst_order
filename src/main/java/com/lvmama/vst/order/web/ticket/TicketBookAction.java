/**
 * 
 */
package com.lvmama.vst.order.web.ticket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.lvmama.comm.search.vst.vo.VstTicketSearchVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsSaleReClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prom.service.MarkCouponLimitClientService;
import com.lvmama.vst.back.client.ticket.service.SuppGoodsSkuRelationClientService;
import com.lvmama.vst.back.dist.po.DistDistributorProd;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsTicketDetailVO;
import com.lvmama.vst.back.goods.vo.SuppGoodsVO;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.back.prod.vo.TicketProductForOrderVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.PageConfigs;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.MarkcouponLimitInfo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import com.lvmama.vst.search.lvmamaback.service.LvmamaBackSearchService;
import com.lvmama.vst.search.util.PageConfig;

/**
 * @author pengyayun
 *
 */
@Controller
public class TicketBookAction extends BaseActionSupport {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(TicketBookAction.class);

    /**
	 * 
	 */
    private final String TICKET_BOOK_PAGE = "/order/ticket/showTicketBookInfo";

    private final String ERROR_PAGE = "/order/error";

    @Autowired
    private DistGoodsClientService distGoodsClientService;// 商品

    @Autowired
    protected OrderService orderService;

    @Autowired
    private SuppGoodsSaleReClientService suppGoodsSaleReClientService;

    @Autowired
    private DistGoodsTimePriceClientService distGoodsTimePriceClientService;// 时间价格表

    @Autowired
    private ProdProductClientService prodProductClientService;

    @Autowired
    private ProdProductNoticeClientService prodProductNoticeClientService;

    @Autowired
    private LvmamaBackSearchService lvmamaBackSearchService;

    @Autowired
    private UserUserProxyAdapter userUserProxyAdapter;

    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;

    @Autowired
    private SuppGoodsSkuRelationClientService skuRelationService;

    @Autowired
    private ProdPackageGroupClientService prodPackageGroupClientRemote;

    /**
     * 进入产品商品查询页面
     * 
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/ord/productQuery/ticket/showTicketQueryList.do")
    public String showTicketQueryList(Model model, HttpServletRequest request) {
        UserUser user = null;
        try {
            // 从cookie中读取用户信息
            user = readUserCookie();
            if (user == null || StringUtil.isEmptyString(user.getUserId())) {
                String userId = request.getParameter("userId");
                if (StringUtil.isNotEmptyString(userId)) {
                    user = userUserProxyAdapter.getUserUserByUserNo(userId);
                }
            }

        } catch (Exception e) {
            LOG.error("{}", e);
        }
        model.addAttribute("user", user);
        model.addAttribute("vo", new VstTicketSearchVO());
        return "/order/orderProductQuery/ticket/showTicketProductQueryList";
    }

    /**
     * 进入产品商品查询页面
     * 
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/ord/productQuery/ticket/searchTicketList.do")
    public String searchTicketList(VstTicketSearchVO vo, Model model, HttpServletRequest request) {
        LOG.debug("searchTicketList start");
        PageConfigs<Map<String, Object>> result = null;
        PageConfig<Map<String, Object>> ticketResult = null;
        UserUser user = null;
        try {
            if (vo.getPage() == null) {
                vo.setPage(1);
            }
            if (vo.getPageSize() == null) {
                vo.setPageSize(10);
            }
            vo.setDistributors(String.valueOf(Constant.DIST_BACK_END));
            ticketResult = lvmamaBackSearchService.getTicketIndexBeans(vo);
            LOG.info("TicketBookAction searchTicketList log,searchapiReform, monitor: service:lvmamaBackSearchService, method:getTicketIndexBeans");
            if (ticketResult != null) {
            	result = new PageConfigs<Map<String, Object>>(ticketResult.getTotalResultSize(),ticketResult.getPageSize(),ticketResult.getCurrentPage());
            	result.setItems(ticketResult.getItems());
                result.setUrl(request.getRequestURI());
                // 构造分页URL
                result.buildUrl(request);
            }
            // 从cookie中读取用户信息
            user = readUserCookie();

        } catch (Exception e) {
            LOG.error("{}", e);
        }

        model.addAttribute("result", result);
        model.addAttribute("user", user);
        model.addAttribute("vo", vo);
        return "/order/orderProductQuery/ticket/ticke_product_query_result";
    }

    @Autowired
    private MarkCouponLimitClientService markCouponLimitClientService;

    @RequestMapping(value = "/ord/book/ticket/infoFillIn.do")
    public String infoFillIn(HttpServletRequest request, ModelMap model) {

        String goodsId = request.getParameter("goodsId");

        String productId = request.getParameter("productId");

        String userId = request.getParameter("userId");

        String result = TICKET_BOOK_PAGE;
        int checkLimit = 0;
        Long checkId = null;
        if (StringUtils.isNotEmpty(goodsId)) {
            result = loadSuppGoods(model, goodsId);
            model.put("suppGoodsFlag", true);
            checkId = Long.valueOf(goodsId);

        } else if (StringUtils.isNotEmpty(productId)) {
            result = loadProduct(model, productId);
            model.put("productFlag", true);
            checkLimit = 1;
            checkId = Long.valueOf(productId);
        }

        // 判断是否支持优惠券
        try {
            String islimit = "N";
            if (null != checkId) {
                ResultHandleT<MarkcouponLimitInfo> resultCouponInfo = markCouponLimitClientService.couponIslimitInfo(checkId, checkLimit);
                if (resultCouponInfo.isSuccess() && resultCouponInfo.getReturnContent() != null) {
                    MarkcouponLimitInfo markInfo = resultCouponInfo.getReturnContent();
                    islimit = markInfo.getIslimit();
                }
            }
            model.put("productCouponLimit", islimit);

        } catch (Exception e) {
        	LOG.error(ExceptionFormatUtil.getTrace(e));
        }

        // 从cookie中读取用户信息
        UserUser user = null;
        if (StringUtil.isEmptyString(userId)) {
            user = readUserCookie();
        } else {
            user = userUserProxyAdapter.getUserUserByUserNo(userId);
        }

        model.addAttribute("user", user);
        model.put("visitTime", DateUtil.formatDate(new Date(), "yyyy-MM-dd"));

        return result;
    }

    private String loadSuppGoods(ModelMap model, String goodsId) {

        SuppGoods suppGoods = null;
        SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = null;
        SuppGoodsTicketDetailVO suppGoodsTicketDetailVO = null;
        List<SuppGoodsSaleRe> suppGoodsSaleReList = null;
        List<SuppGoods> relateList = null;
        List<SuppGoodsTicketDetailVO> relateTicketDetailVOList = new ArrayList<SuppGoodsTicketDetailVO>();
        List<SuppGoodsAddTimePrice> timePriceList = new ArrayList<SuppGoodsAddTimePrice>();
        // 预付和现付
        boolean prepaidFalg = false;
        try {
            ResultHandleT<SuppGoods> suppGoodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_BACK_END, Long.parseLong(goodsId));
            if (suppGoodsResultHandleT.getReturnContent() == null) {
                model.addAttribute("ERROR", "商品不可售");
                return ERROR_PAGE;
            }

            suppGoods = suppGoodsResultHandleT.getReturnContent();
            if ("Y".equalsIgnoreCase(suppGoods.getPackageFlag())) {
                model.addAttribute("ERROR", "此商品仅组合销售!");
                return ERROR_PAGE;
            }

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("suppGoodsId", suppGoods.getSuppGoodsId());
            params.put("date", new Date());
            params.put("orderByClause", "sgr.SPEC_DATE");
            ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientRemote.getFirstTimePrice(params);
            suppGoodsBaseTimePrice = timePriceResultHandleT.getReturnContent();
            if (timePriceResultHandleT.isFail() || suppGoodsBaseTimePrice == null) {
                model.addAttribute("ERROR", "商品不可售");
                return "order/error";
            }

            // 商品详情和退改
            ResultHandleT<SuppGoodsTicketDetailVO> ticketDetailVoResultHandleT = distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END,
                    Long.parseLong(goodsId));
            suppGoodsTicketDetailVO = ticketDetailVoResultHandleT.getReturnContent();

            /*
             * ResultHandleT<SuppGoodsAddTimePrice> timePriceResultHandleT
             * =distGoodsTimePriceClientService
             * .findSuppGoodsTicketTimePriceList(Constant.DIST_BACK_END, Long.parseLong(goodsId),
             * new Date()); suppGoodsAddTimePrice=timePriceResultHandleT.getReturnContent();
             */
            // 关联商品
            if (SuppGoods.PAYTARGET.PREPAID.name().equals(suppGoods.getPayTarget()) && !"Y".equalsIgnoreCase(suppGoods.getAperiodicFlag())) {
                relateList = getRelateTicket(suppGoods.getSuppGoodsId());
                if (null != relateList) {
                    filterRelateGoodsList(relateList, timePriceList, relateTicketDetailVOList, suppGoodsBaseTimePrice.getSpecDate());
                    /*
                     * for (SuppGoods sg : relateList) { ResultHandleT<SuppGoodsTicketDetailVO>
                     * detailVoResult
                     * =distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END,
                     * sg.getSuppGoodsId());
                     * if(detailVoResult.isSuccess()&&detailVoResult.getReturnContent()!=null){
                     * relateTicketDetailVOList.add(detailVoResult.getReturnContent()); }
                     * ResultHandleT<SuppGoodsAddTimePrice>
                     * timePriceResult=distGoodsTimePriceClientService
                     * .findSuppGoodsTicketTimePriceList(Constant.DIST_BACK_END,
                     * sg.getSuppGoodsId(), suppGoodsBaseTimePrice.getSpecDate());
                     * if(timePriceResult.isSuccess()&&timePriceResult.getReturnContent()!=null){
                     * timePriceList.add(timePriceResult.getReturnContent()); } }
                     */
                }

                // 查询保险
                if (SuppGoods.PAYTARGET.PREPAID.name().equals(suppGoods.getPayTarget()) && !"Y".equalsIgnoreCase(suppGoods.getAperiodicFlag())) {
                    // suppGoodsSaleReList=getSaleReByProductId(suppGoods.getProductId());
                }
            }

            // 出境门票商品判断有无主从关联商品
            if (CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(suppGoods.getBu())) {
                // 查询选择商品是否有主商品
                Long masterSuppGoodsId = skuRelationService.selectMasterSuppGoodsIdBySlaveGoodsId(suppGoods.getSuppGoodsId());
                LOG.info("OUTBOUND BU TICKET:{}" , masterSuppGoodsId);
                if (masterSuppGoodsId != null) {
                    model.addAttribute("masterSuppGoodsId", masterSuppGoodsId);
                }
            }
            // 是否可使用奖金优惠标示
            if (StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())) {
                prepaidFalg = true;
            }

        } catch (Exception e) {
            LOG.error("{}", e);
        }

        model.addAttribute("suppGoods", suppGoods);
        model.addAttribute("suppGoodsBaseTimePrice", suppGoodsBaseTimePrice);
        model.addAttribute("ticketDetailVO", suppGoodsTicketDetailVO);
        model.addAttribute("suppGoodsSaleReList", suppGoodsSaleReList);
        model.addAttribute("relateList", relateList);
        model.addAttribute("relateDetailVOList", relateTicketDetailVOList);
        model.addAttribute("timePriceList", timePriceList);
        model.addAttribute("prepaidFalg", prepaidFalg);
        return TICKET_BOOK_PAGE;
    }

    private String loadProduct(ModelMap model, String productId) {

        TicketProductForOrderVO ticketProductForOrderVO = null;
        List<SuppGoodsSaleRe> suppGoodsSaleReList = null;
        ProdProduct prodProduct = null;
        String aperiodPackageFlag ="N";
        try {
            Long pdId = Long.valueOf(productId);
            ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findProdProductByIdFromCache(pdId);
            if (resultHandle.isSuccess() && resultHandle.getReturnContent() != null) {
                prodProduct = resultHandle.getReturnContent();
            }
            ResultHandleT<Boolean> flagHandler =prodPackageGroupClientRemote.checkAllAperiodic(prodProduct.getProductId());
            if (flagHandler.getReturnContent() == null) {
                model.addAttribute("ERROR", flagHandler.getMsg());
                return ERROR_PAGE;
            }
            if(flagHandler.getReturnContent()) aperiodPackageFlag="Y";
            ResultHandleT<TicketProductForOrderVO> productResultHandleT;
            if("Y".equals(aperiodPackageFlag)){
                productResultHandleT = prodProductClientService.findAperiodTicketProductForOrder(pdId, new Date());
            }else{
                productResultHandleT = prodProductClientService.findTicketProductForOrder(pdId, new Date());
            }
            if (productResultHandleT.hasNull() || productResultHandleT.getReturnContent() == null) {
                model.addAttribute("ERROR", "商品不可售");
                return ERROR_PAGE;
            }
            ticketProductForOrderVO = productResultHandleT.getReturnContent();
            boolean existFlge = false;
            if (!CollectionUtils.isEmpty(ticketProductForOrderVO.getProduct().getDistDistributorProds())) {
                for (DistDistributorProd distributor : ticketProductForOrderVO.getProduct().getDistDistributorProds()) {
                    if (distributor.getDistributorId().longValue() == Constant.DIST_BACK_END) {
                        existFlge = true;
                    }
                }
            }
            if (!existFlge) {
                model.addAttribute("ERROR", "商品不可售");
                return ERROR_PAGE;
            }
            StringBuilder combVisitTime=new StringBuilder();
            for (SuppGoodsVO suppGoodsVO : ticketProductForOrderVO.getSuppGoodsList()) {
                // 商品详情和退改
                ResultHandleT<SuppGoodsTicketDetailVO> ticketDetailVoResultHandleT = distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END,
                        suppGoodsVO.getSuppGoodsId());
                suppGoodsVO.setSuppGoodsTicketDetailVO(ticketDetailVoResultHandleT.getReturnContent());

                SuppGoodsExp exp = suppGoodsVO.getSuppGoodsExp();
                if("Y".equals(aperiodPackageFlag)) {
                    combVisitTime.append(exp.getStartTimeStr()).append(" 至 ").append(exp.getEndTimeStr()).append(" , ");
                }
            }
            // 查询保险
            // suppGoodsSaleReList=getSaleReByProductId(pdId);
            if("Y".equals(aperiodPackageFlag)) {
                model.addAttribute("combVisitTime", combVisitTime.substring(0, combVisitTime.length() - 1));
            }
        } catch (Exception e) {
            LOG.error("{}", e);
            model.addAttribute("ERROR", "获取商品出现异常");
            return ERROR_PAGE;
        }

        model.addAttribute("ticketCombProductVO", ticketProductForOrderVO);
        model.addAttribute("categoryId", prodProduct.getBizCategoryId());
        model.addAttribute("suppGoodsSaleReList", suppGoodsSaleReList);

        model.addAttribute("prepaidFalg", true);
        model.addAttribute("aperiodPackageFlag",aperiodPackageFlag);
        return "/order/ticket/showTicketBookInfo_package";
    }

    /**
     * 关联门票
     * 
     * @param
     * @return
     */
    private List<SuppGoods> getRelateTicket(Long suppGoodsId) {
        List<SuppGoods> list = null;
        ResultHandleT<List<SuppGoods>> resultHandleT = distGoodsClientService.findRelateTicketSuppGoods(suppGoodsId, Constant.DIST_BACK_END);
        if (resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null) {
            list = resultHandleT.getReturnContent();
        }
        return list;
    }

    private void filterRelateGoodsList(List<SuppGoods> relateList, List<SuppGoodsAddTimePrice> timePriceList, List<SuppGoodsTicketDetailVO> relateTicketDetailVOList, Date visitDate) {
        if (!CollectionUtils.isEmpty(relateList)) {
            for (int i = 0; i < relateList.size(); i++) {
                SuppGoods sg = relateList.get(i);
                ResultHandleT<SuppGoodsAddTimePrice> timePriceResult = distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(Constant.DIST_BACK_END,
                        sg.getSuppGoodsId(), visitDate);
                if (timePriceResult.isFail() || timePriceResult.getReturnContent() == null) {
                    relateList.remove(i);
                    i--;
                    continue;
                }
                timePriceList.add(timePriceResult.getReturnContent());
                ResultHandleT<SuppGoodsTicketDetailVO> detailVoResult = distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END, sg.getSuppGoodsId());
                if (detailVoResult.isSuccess() && detailVoResult.getReturnContent() != null) {
                    relateTicketDetailVOList.add(detailVoResult.getReturnContent());
                }
            }
        }
    }

    @RequestMapping("/ord/book/ticket/refereshOtherTicket.do")
    public String refereshOtherTicket(ModelMap model, Long suppGoodsId, String visitTime) {
        Date visitDate = DateUtil.getDateByStr(visitTime, "yyyy-MM-dd");
        List<SuppGoods> relateList = getRelateTicket(suppGoodsId);
        List<SuppGoodsTicketDetailVO> relateTicketDetailVOList = new ArrayList<SuppGoodsTicketDetailVO>();
        List<SuppGoodsAddTimePrice> timePriceList = new ArrayList<SuppGoodsAddTimePrice>();
        filterRelateGoodsList(relateList, timePriceList, relateTicketDetailVOList, visitDate);
        model.addAttribute("relateList", relateList);
        model.addAttribute("relateDetailVOList", relateTicketDetailVOList);
        model.addAttribute("timePriceList", timePriceList);
        return "/order/ticket/inc/other_goods_info";
    }

    /**
     * 保险
     * 
     * @param productId
     * @return
     */
    private List<SuppGoodsSaleRe> getSaleReByProductId(Long productId, Date visitTime) {
        List<SuppGoodsSaleRe> list = new ArrayList<SuppGoodsSaleRe>();
        ResultHandleT<List<SuppGoodsSaleRe>> resultHandleT = suppGoodsSaleReClientService.selectListByProductId(productId, Constant.DIST_BACK_END, visitTime);
        if (resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null) {
            list = resultHandleT.getReturnContent();
        }
        return list;
    }

    @RequestMapping("/ord/book/ticket/refereshInsurance.do")
    public String refereshInsurance(ModelMap model, Long productId, String visitTime) {
        Date visitDate = DateUtil.getDateByStr(visitTime, "yyyy-MM-dd");
        List<SuppGoodsSaleRe> suppGoodsSaleReList = getSaleReByProductId(productId, visitDate);
        model.addAttribute("suppGoodsSaleReList", suppGoodsSaleReList);
        return "/order/ticket/inc/insurance";
    }

  
    @RequestMapping("/ord/book/ticket/refereshTimePrice.do")
    @ResponseBody
    public Object refereshTimePrice(ModelMap model, String productIds, String suppGoodsIds, String visitTime) {
        List<Long> productIdList = strConverList(productIds);
        List<Long> suppGoodsIdList = strConverList(suppGoodsIds);
        List<SuppGoodsAddTimePrice> ticketVoList = new ArrayList<SuppGoodsAddTimePrice>();
        TicketProductForOrderVO ticketProductForOrderVO = null;
        ResultMessage msg = ResultMessage.createResultMessage();
        try {
            if (suppGoodsIdList != null) {
                for (Long goodsId : suppGoodsIdList) {

                    ResultHandleT<SuppGoodsAddTimePrice> timePriceResultHandleT = distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(Constant.DIST_BACK_END, goodsId,
                            DateUtil.getDateByStr(visitTime, "yyyy-MM-dd"));
                    if (timePriceResultHandleT.isSuccess() && timePriceResultHandleT.getReturnContent() != null) {
                        ticketVoList.add(timePriceResultHandleT.getReturnContent());
                    }
                }

            }

            if (productIdList != null) {
                for (Long productId : productIdList) {
                    ResultHandleT<TicketProductForOrderVO> productResultHandleT = prodProductClientService.findTicketProductForOrder(Long.valueOf(productId),
                            DateUtil.getDateByStr(visitTime, "yyyy-MM-dd"));
                    ticketProductForOrderVO = productResultHandleT.getReturnContent();
                    break;
                }
            }

        } catch (Exception e) {
            LOG.error("{}", e);
        }
        msg.addObject("ticketVoList", ticketVoList);
        msg.addObject("ticketProductVO", ticketProductForOrderVO);
        return msg;
    }

    @RequestMapping("/ord/book/ticket/getTicketTimePrice.do")
    @ResponseBody
    public Object getTicketTimePrice(ModelMap model, Long suppGoodsId, String visitTime) {
        ResultMessage msg = ResultMessage.createResultMessage();
        try {
            ResultHandleT<List<SuppGoodsAddTimePrice>> resultHandleT = distGoodsTimePriceClientService.getTimePriceByGoodsIdAndDate(Constant.DIST_FRONT_END, suppGoodsId,
                    new Date(), 6);// DateUtil.getDateByStr(visitTime, "yyyy-MM-dd")
            if (resultHandleT.isFail() || resultHandleT.getReturnContent() == null) {
                msg.raise(resultHandleT.getMsg());
            }
            msg.addObject("timePriceList", resultHandleT.getReturnContent());
        } catch (Exception e) {
            LOG.error("{}", e);
            msg.raise("查询时间价格发生异常.");
        }
        return msg;
    }

    private List<Long> strConverList(String str) {
        List<Long> idList = null;
        if (StringUtil.isNotEmptyString(str)) {
            idList = new ArrayList<Long>();
            String[] ids = str.split(",");
            if (null != ids && ids.length > 0) {
                for (String item : ids) {
                    if (StringUtil.isNotEmptyString(item)) {
                        idList.add(Long.valueOf(item));
                    }
                }
            }
        }
        return idList;
    }

    /**
     * 取产品公告
     * 
     * @param productId
     * @return
     */
    private List<ProdProductNotice> getProductNoticeList(Long productId, String startDate) {
        Map<String, Object> paramProductNotice = new HashMap<String, Object>();
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sFormat.parse(startDate);
            paramProductNotice.put("startTime", start);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOG.error("{}", e);
        }
        paramProductNotice.put("productId", productId);
        paramProductNotice.put("cancelFlag", "Y");
        paramProductNotice.put("_orderby", "CREATE_TIME asc");

        ResultHandleT<List<ProdProductNotice>> resultHandleT = prodProductNoticeClientService.findProductNoticeList(paramProductNotice);
        List<ProdProductNotice> productNoticeList = null;
        if (resultHandleT.isSuccess()) {
            productNoticeList = resultHandleT.getReturnContent();
        }
        return productNoticeList;
    }

    @RequestMapping("/ord/book/ticket/getTicketGoodsDetail.do")
    @ResponseBody
    public Object getTicketGoodsDetail(ModelMap model, Long suppGoodsId, String aperiodicFlag) {
        ResultMessage msg = ResultMessage.createResultMessage();
        try {
            // 商品详情和退改
            ResultHandleT<SuppGoodsTicketDetailVO> resultHandleT = distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END, suppGoodsId);
            if (resultHandleT.isFail() || resultHandleT.getReturnContent() == null) {
                msg.raise(resultHandleT.getMsg());
            }
            SuppGoodsTicketDetailVO detailVo = resultHandleT.getReturnContent();
            List<SuppGoodsRefund> list = detailVo.getSuppGoodsRefundList();
            String str = SuppGoodsRefundTools.SuppGoodsRefundVOToStrForFront(list, aperiodicFlag);// SuppGoodsRefund.getCancelStrategyDesc(list,
                                                                                                  // aperiodicFlag);
            msg.addObject("suppGoodsRefundStr", str);
            msg.addObject("suppGoodsTicketDetailVO", detailVo);
        } catch (Exception e) {
            LOG.error("{}", e);
            msg.raise("查询商品详情发生异常.");
        }
        return msg;
    }

    @RequestMapping("/ord/book/ticket/getCombTicketGoodsDetail.do")
    @ResponseBody
    public Object getCombTicketGoodsDetail(ModelMap model, Long productId) {
        ResultMessage msg = ResultMessage.createResultMessage();
        try {
            StringBuffer sb = new StringBuffer();
            ResultHandleT<TicketProductForOrderVO> productResultHandleT = prodProductClientService.findTicketProductForOrder(productId, new Date());
            if (productResultHandleT.isSuccess() && productResultHandleT.getReturnContent() != null) {
                TicketProductForOrderVO ticketProductForOrderVO = productResultHandleT.getReturnContent();
                for (SuppGoodsVO suppGoodsVO : ticketProductForOrderVO.getSuppGoodsList()) {
                    // 商品详情和退改
                    ResultHandleT<SuppGoodsTicketDetailVO> ticketDetailVoResultHandleT = distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_BACK_END,
                            suppGoodsVO.getSuppGoodsId());
                    SuppGoodsTicketDetailVO detailVo = ticketDetailVoResultHandleT.getReturnContent();

                    List<SuppGoodsRefund> list = detailVo.getSuppGoodsRefundList();
                    String str = SuppGoodsRefundTools.SuppGoodsRefundVOToStrForFront(list, suppGoodsVO.getAperiodicFlag());// SuppGoodsRefund.getCancelStrategyDesc(list,
                                                                                                                           // suppGoodsVO.getAperiodicFlag());

                    sb.append(str + "<br/>");
                }
            }
            msg.addObject("suppGoodsRefundStr", sb.toString());
        } catch (Exception e) {
            LOG.error("{}", e);
            msg.raise("查询门票套餐商品详情发生异常.");
        }
        return msg;
    }
}
