/**
 *
 */
package com.lvmama.vst.order.service.book;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.back.goods.po.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;
import com.lvmama.comm.bee.po.freebie.HotelFreebiePO;
import com.lvmama.comm.utils.JsonUtil;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.comm.vst.vo.CardInfo;
import com.lvmama.commons.logging.LvmamaLog;
import com.lvmama.commons.logging.LvmamaLogFactory;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelFreebieQueryApiService;
import com.lvmama.dest.api.goods.vo.HotelFreebieVo;
import com.lvmama.dest.api.order.vo.HotelOrdItemFreebiesRelation;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsTimePriceQVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelCurrencyInfoVstVo;
import com.lvmama.order.enums.ApiEnum;
import com.lvmama.order.enums.OrdProcessKeyEnum;
import com.lvmama.order.service.api.comm.workflow.IApiOrdProcessKeyService;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum.SUPPGOODS_KEY;
import com.lvmama.order.vo.comm.workflow.OrdProcessKeyVo;
import com.lvmama.scenic.api.prod.service.ScenicTicketProductService;
import com.lvmama.vst.api.route.prod.vo.RouteProductVo;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDest;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.biz.po.BizOrderRequired;
import com.lvmama.vst.back.biz.po.BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.biz.service.DestClientService;
import com.lvmama.vst.back.client.biz.service.DestContentClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.biz.service.OrderRequiredClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsRebateClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceAdapterClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.hotelFreebie.service.HotelFreebieClientService;
import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.client.ord.service.OrderRelatedPersonClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.prod.service.ProdDestReClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductSaleReClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.client.pub.service.ComOrderRequiredClientService;
import com.lvmama.vst.back.client.supp.service.SuppContractClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettleRuleClientService;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.utils.SuppGoodsExpTools;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.back.order.po.OrdItemFreebiesRelation;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdTravAdditionConf;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_CHANNEL;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_CONTRACT_SIGNING_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.REBATE_TYPE;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.adapter.ProdProductHotelAdapterClientService;
import com.lvmama.vst.back.prod.po.ProdDestRe;
import com.lvmama.vst.back.prod.po.ProdEcontract;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PACKAGETYPE;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.prod.po.ProdProductSaleRe;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.prom.po.SuppGoodsRebate;
import com.lvmama.vst.back.pub.po.ComOrderRequired;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.back.supp.po.SuppSettleRule;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.back.utils.ConfirmUtils;
import com.lvmama.vst.comlog.LvmmLogEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.enumeration.CommEnumSet.BU_NAME;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.TimePriceUtils;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.DestBuOrderPropUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.DefaultRebateConfig;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.BuyInfoAddition;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.order.cache.OrderContextCache;
import com.lvmama.vst.order.exception.GetVerifiedFlightInfoFailException;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.route.constant.VstRouteConstants;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IHotelTradeApiService;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;
import com.lvmama.vst.order.service.IOrdItemFreebieService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdTravAdditionConfService;
import com.lvmama.vst.order.service.IOrderInitService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.book.util.ConfirmEnum;
import com.lvmama.vst.order.service.book.util.OrderBookServiceDataUtil;
import com.lvmama.vst.order.service.impl.PromotionBussiness;
import com.lvmama.vst.order.service.util.PromtionUtil;
import com.lvmama.vst.order.snapshot.service.IVstSnapshotService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.order.utils.BlackListBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;
import com.lvmama.vst.prom.dto.PromotionInfo;
import com.lvmama.vst.prom.dto.PromotionInfo.PromotionData;
import com.lvmama.vst.prom.dto.PromotionQueryDTO;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemData;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemData.SALE_UNIT;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemPromotionInfo;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemPromotionInfo.ITEM_TYPE;
import com.lvmama.vst.prom.response.Response;
import com.lvmama.vst.prom.response.ResponseInfoable;
import com.lvmama.vst.suppTicket.client.product.po.IntfStylProdRela;
import com.lvmama.vst.suppTicket.client.product.service.SuppTicketProductClientService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import scala.actors.threadpool.Arrays;


/**
 * 下单接口重构
 * @author lancey
 *
 */
@Component("orderNewBookService")
public class OrderBookServiceImpl extends AbstractBookService implements IBookService,IOrderInitService,InitializingBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderBookServiceImpl.class);

    private static final Logger LOGTSET = LoggerFactory.getLogger(OrderBookServiceImpl.class);
    
    //新加事件日志
  	private static final LvmamaLog lvmamaLog = LvmamaLogFactory.getLog(OrderBookServiceImpl.class);
  	
    
    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterService;

    @Autowired
    private ProdProductClientService productClientService;

    @Autowired
    private OrderPersonInitBussiness personInitBussiness;

    @Autowired
    private IOrderUpdateService orderUpdateService;

    @Autowired
    private OrderSaveService orderSaveService;
    
    @Autowired
    private SuppSettleRuleClientService suppSettleRuleClientService;
    
    @Autowired
    private SuppContractClientService  suppContractClientService;
    
    @Autowired
    private IOrderSaveService orderSaveServiceImpl;

    @Autowired
    private IOrdUserOrderServiceAdapter ordUserOrderService;

    @Autowired
    private GuaranteeBussiness guaranteeBussiness;

    @Autowired
    private CategoryComparator categoryComparator;

    @Autowired
    private SuppGoodsRebateClientService suppGoodsRebateClientService;

    @Autowired
    private BlackListBussiness blackListBussiness;

    @Autowired
    private BranchClientService branchClientService;

    @Autowired
    private PromotionBussiness promotionBussiness;
    @Autowired
    private DestContentClientService destContentClientService;
    @Autowired
    private DestClientService destClientService;
    @Autowired
    private DistrictClientService districtClientService;

    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;

    @Autowired
    private SuppGoodsTimePriceAdapterClientService suppGoodsTimePriceClientAdapter;

    @Autowired
    private DistGoodsClientService distGoodsClientService;// 商品

    @Autowired
    private OrderLvfTimePriceServiceImpl orderLvfTimePriceServiceImpl;

    @Autowired
    private ProdLineRouteClientService prodLineRouteClientService;
    @Autowired
    private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;//调用vstpet获取现金和奖金余额接口
    @Autowired
    protected OrderService orderService;

    @Autowired
    private IPayPaymentServiceAdapter payPaymentServiceAdapter;

    @Autowired
    private HotelFreebieClientService hotelFreebieClientService;

    @Autowired
    private IOrdItemFreebieService ordItemFreebieService;


    @Autowired
    private ResPreControlService resControlBudgetRemote;

//    @Autowired
//    private IOrdOrderItemService ordOrderItemService;

    @Autowired
    protected OrderRequiredClientService orderRequiredClientService;
    
    @Autowired
    private ComOrderRequiredClientService comOrderRequiredClientService;

    //对本类中的数据作处理的辅助对象
    @Resource(name="orderBookServiceDataUtil")
    private OrderBookServiceDataUtil orderBookServiceDataUtil;

    @Autowired
    private OrderRelatedPersonClientService orderRelatedPersonClientService;
    @Autowired
    SuppTicketProductClientService suppTicketProductClientService;
    
    @Autowired
    private IHotelFreebieQueryApiService hotelFreebieQueryApiRemote;
    
    //交通+X中酒店行政地区对应产品经理map
    @Resource(name="attributionManagerMap")
    private Map<Long,Long> attributionManagerMap;
    //destBu 意外险 需要后置的游玩人信息表
    @Autowired
    private IOrdTravAdditionConfService ordTravAdditionConfService;

    @Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;

    @Autowired
    private DestHotelAdapterUtils destHotelAdapterUtils;

    @Autowired
    private ProdDestReClientService prodDestReClientRemote;

    @Autowired
    private ProdProductHotelAdapterClientService productHotelAdapterClientService;
    
    @Autowired
    private IHotelTradeApiService hotelTradeApiService;
    
    @Autowired
    private ProdPackageGroupClientService prodPackageGroupClientService;

    @Autowired
    private SuppSettlementEntityClientService suppSettlementEntityClientService;

    @Autowired
    private ProdProductBranchClientService prodProductBranchClientService;

    /**
     * 目的地对接酒店接口
     * */
    @Resource
    private IHotelGoodsTimePriceQVstApiService hotelGoodsTimePriceQVstApiRemote;
    @Resource
    private IVstSnapshotService vstSnapshotService;
    
    @Resource
	private IVstOrderRouteService vstOrderRouteService;

    @Autowired
    private ScenicTicketProductService scenicTicketProductService;
    
	@Autowired
	private PromotionService promotionService;
	@Autowired
	private IApiOrdProcessKeyService apiOrdProcessKeyService;
    
    /**
     * 支持
     * @param buyInfo
     */
    private void converOldCurise(BuyInfo buyInfo){
        if(CollectionUtils.isEmpty(buyInfo.getProductList())&&buyInfo.getCategoryId()!=null&&buyInfo.getProductId()!=null&&buyInfo.getCategoryId()==8L){
            List<BuyInfo.Product> productList = new ArrayList<BuyInfo.Product>();
            BuyInfo.Product product = new BuyInfo.Product();
            product.setProductId(buyInfo.getProductId());
            List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
            for(BuyInfo.Item item:buyInfo.getItemList()){
                itemList.add(item);
            }
            product.setItemList(itemList);
            productList.add(product);
            buyInfo.getItemList().clear();
            buyInfo.setProductList(productList);
        }
        if(CollectionUtils.isNotEmpty(buyInfo.getPromotionIdList())&&MapUtils.isEmpty(buyInfo.getPromotionMap())&&!buyInfo.getItemList().isEmpty()&&buyInfo.getItemList().size()==1){
            Map<String,List<Long>> map = new HashMap<String, List<Long>>();
            map.put("GOODS_"+buyInfo.getItemList().get(0).getGoodsId()+"_GOODS", new ArrayList<Long>(buyInfo.getPromotionIdList()));
            buyInfo.getPromotionIdList().clear();
            buyInfo.setPromotionMap(map);
        }
    }

    private static ConcurrentMap<String, Long> bookUniqueMap = new ConcurrentHashMap<String, Long>();
    private final JsonConfig config = new JsonConfig();

    private synchronized String getBuyInfoHashCode(BuyInfo buyInfo){
        JSONObject obj = JSONObject.fromObject(buyInfo,config);
        return DigestUtils.shaHex(obj.toString());
    }

    private void checkBuyInfo(BuyInfo buyInfo){
        if(buyInfo.getUserNo()==null&&StringUtils.isEmpty(buyInfo.getUserId())){
            throwNullException("订单所属人不可以为空");
        }

        if(buyInfo.getDistributionId()==null){
            throwNullException("下单渠道为空");
        }

        if(buyInfo.getDistributionId()==4L){
            if(buyInfo.getDistributionChannel()==null){
                throwNullException("分销商渠道为空");
            }
        }

        if(StringUtils.isEmpty(buyInfo.getIp())){
            throwNullException("下单IP地址为空");
        }
    }

    private void valitTravellers(OrdOrderDTO order) {
        //门票前台下单，判断游玩人信息是否是必填
        if ((Constants.DISTRIBUTOR_3.equals(order.getDistributorId()))
                && ProductCategoryUtil.isTicket(order.getCategoryId())) {
            if (CollectionUtils.isEmpty(order.getOrdTravellerList())) {
                List<OrdOrderItem> orderItemList = order.getOrderItemList();
                List<Long> productIdList = new ArrayList<Long>();
                List<Long> suppGoodsIdList = new ArrayList<Long>();
                for (OrdOrderItem ordOrderItem : orderItemList) {
                    suppGoodsIdList.add(ordOrderItem.getSuppGoodsId());
                    productIdList.add(ordOrderItem.getProductId());
                }
                List<OrdOrderPack> packList = order.getOrderPackList();
                if (CollectionUtils.isNotEmpty(packList)) {
                    for (OrdOrderPack ordOrderPack : packList) {
                        productIdList.add(ordOrderPack.getProductId());
                    }
                }

                ResultHandleT<OrderRequiredVO> orderRequiredVO = orderRequiredClientService
                        .findOrderRequiredListId(productIdList,
                                suppGoodsIdList, BU_NAME.TICKET_BU);
                OrderRequiredVO vo = orderRequiredVO.getReturnContent();
                if (vo != null && Constants.Y_FLAG.equals(vo.getNeedTravFlag())) {
                    throw new IllegalArgumentException("游玩人不能为空");
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.lvmama.vst.order.service.IBookService#createOrder(com.lvmama.vst.comm.vo.order.BuyInfo, java.lang.String)
     */
    @Override
    public ResultHandleT<OrdOrder> createOrder(BuyInfo buyInfo,
            String operatorId) {


        logger.info("OrderBookServiceImpl.createOrder buyInfo"+buyInfo.toJsonStr());
        logger.info("--------------------实付金额"+buyInfo.getActualAmount());
        ResultHandleT<OrdOrder> result = new ResultHandleT<OrdOrder>();
        boolean needClearToken=true;
        String key = getBuyInfoHashCode(buyInfo);
        try{
            checkBuyInfo(buyInfo);
            long time = getTime();
            if(bookUniqueMap.putIfAbsent(key, time)!=null){
                needClearToken=false;
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CREATE_ORDER.getErrorCode(), "重复创建订单");
            }
            
            converOldCurise(buyInfo);
            OrdOrderDTO order = new OrdOrderDTO(buyInfo);
            order.setCreateFlag(true);//设置当前是在订单生成
            order.setBackUserId(operatorId);
            initOrder(order);
            calcOrder(order);
            valitTravellers(order);
            valitOrderForCounpon(order);
            initOrdTravelContract(order);
            initOrdTicketPerformStatus(order);
            //目的地订单设置订单结束时间和订单展示状态
            initDestBuViewStatusAndEndTime(order);
            //目的地 酒+景 酒店套餐 意外险游玩人后置 设置子单需要后置标识
            initDestBuAccTravDelayOrderItem(buyInfo, order);
            //目的地 单酒店 境外酒店下单
            initDestBuForeighHotelItem(order);

            //分销的价格与主站不同，但合同价上是按照主站的价格显示的，应分销的要求，把合同的价格修改为分销传递过来的价格
            orderBookServiceDataUtil.convertDistributorOrderPrice(buyInfo, order);
            //设置 App版本号
            String appVersion = buyInfo.getAppVersion();
            logger.info("=========OrderBookServiceImpl.createOrder设置 App版本号: "+appVersion);
            if(StringUtil.isNotEmptyString(appVersion)){
                order.setAppVersion(appVersion);
            }
            //是否发送合同标识
            String sendContractFlag = buyInfo.getSendContractFlag();
            if(StringUtil.isNotEmptyString(sendContractFlag)){
                order.setSendContractFlag(sendContractFlag);
            }
            //设备指纹
            String orderDfp = buyInfo.getOrderDfp();
            if(StringUtil.isNotEmptyString(orderDfp)){
                order.setOrderDfp(orderDfp);
            }
            //锁仓前置的订单需要设置订单号和子订单号和工作流
            initPreLockSeatOrder(buyInfo,order);
            
            lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.INIT_ORDER.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "OrderBookServiceImpl-订单初始化成功", "OrderBookServiceImpl-订单初始化成功");
            
            saveOrder(order);
            logger.info("=========OrderBookServiceImpl.createOrder. "+order.getOrderId()+",WaitPaymentTime="+order.getWaitPaymentTime());
            result.setReturnContent(order);
            //单酒店订单扣减赠品库存
            if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())){
                deductFreebieStock(order,result);
            }
        }catch(OrderException ex){
            String errorCode = ex.getErrorCode();

            if (StringUtil.isNotEmptyString(errorCode)) {
                result.setErrorCode(errorCode);
            }
            result.setMsg(ex);
            
            //记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
            
        }catch(IllegalArgumentException ex){
            result.setMsg(ex);
            
            //记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
            
        }catch(Exception ex){
            logger.error(ExceptionFormatUtil.getTrace(ex));
            result.setMsg(ex);
            
            //记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
        }finally{
            if(needClearToken){
                bookUniqueMap.remove(key);
            }
        }
        return result;
    }
    
    private void initPreLockSeatOrder(BuyInfo buyInfo, OrdOrderDTO order) {
    	if(StringUtils.isNotBlank(buyInfo.getIsPreLockSeat())&&"true".equals(buyInfo.getIsPreLockSeat())){
    		Map<String, String> map=new HashMap<String, String>();
        	String lockSetOrderId = buyInfo.getLockSetOrderId();
        	if(StringUtils.isNotBlank(lockSetOrderId)){
        		JSONObject json = JSONObject.fromObject(lockSetOrderId);
            	map=(Map<String, String>) JSONObject.toBean(json,Map.class);
            	Long orderId = Long.valueOf(map.get("0"));
            	order.setOrderId(orderId);
        	}
        	order.setResourceStatus(null);
        	order.setProcessKey("local_order_prelockseat_main");
        	for (OrdOrderItem orderItem : order.getOrderItemList()) {
        		if(orderItem.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()){
        			orderItem.setContent(orderItem.getContent());
        			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "local_flight_prelockseat");
				}
        		String suppGoodsId = map.get(orderItem.getSuppGoodsId().toString());
        		if(suppGoodsId!=null){
        			Long orderItemId =  Long.valueOf(suppGoodsId);
            		if(orderItemId!=null){
            			orderItem.setOrderItemId(orderItemId);
            			orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
            			orderItem.setResourceAmpleTime(new Date());
            			orderItem.setNeedResourceConfirm("false");
            			orderItem.setInfoStatus(OrderEnum.INFO_STATUS.INFOPASS.name());
            			orderItem.setInfoPassTime(new Date());
            		}
        		}
				//设置新酒店工作流
    			order.setWorkVersion(OrdOrderUtils.WORK_VERSION);
    			if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
    				orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "local_hotel_new");
                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
    			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())
    					||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
    				 orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "local_hotelcomb_new");
                     orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
    			}
        		 //计算主订单的资源状态
                setOrderResourceStatus(orderItem, order);
			}
        	order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 30));
            order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.name());
        }
	}

	/**
     * 加载订单相关人员
     */
    @Override
    public OrderRelatedPersonsVO loadOrderRelatedPersons(OrdPersonQueryTO ordPersonQueryTO) {
        if(ordPersonQueryTO == null || ordPersonQueryTO.getOrderId() == null || ordPersonQueryTO.getOrderId() < 0){
            return null;
        }
        ResultHandleT<OrderRelatedPersonsVO> resultHandleT = orderRelatedPersonClientService.loadOrderRelatedPersons(ordPersonQueryTO);
        if(resultHandleT == null){
            logger.error("Error get related persons for order " + ordPersonQueryTO.getOrderId() + ", remote interface returned null value");
            throw new RuntimeException("获取订单相关人信息返回结果为空，请检查");
        }
        if(resultHandleT.isFail()){
            logger.error("Error get related persons for order " + ordPersonQueryTO.getOrderId() + ", msg is " + resultHandleT.getMsg());
            throw new RuntimeException("获取订单相关人信息出错，错误信息是" + resultHandleT.getMsg());
        }
        return resultHandleT.getReturnContent();
    }
    
    private void valitOrderForCounpon(OrdOrderDTO order) {
        List<String> usedConList=new ArrayList<String>();
        if (order!=null &&order.getBuyInfo()!=null) {
            if (CollectionUtils.isEmpty(order.getBuyInfo().getUserCouponVoList())) {
                        if (Long.valueOf(11).equals(order.getCategoryId())
                    ||Long.valueOf(12).equals(order.getCategoryId())
                    ||Long.valueOf(13).equals(order.getCategoryId())) {
                    if (order.getBuyInfo().getUserCouponVoList().size()>1) {
                        throw new IllegalArgumentException("门票订单中不允许使用多张优惠券");
                    }
                }

                if(Long.valueOf(28).equals(order.getCategoryId())){
                    if (order.getBuyInfo().getUserCouponVoList().size()>1) {
                        throw new IllegalArgumentException("wifi电话卡订单中不允许使用多张优惠券");
                    }
                }

                if(Long.valueOf(41).equals(order.getCategoryId())){
                    if (order.getBuyInfo().getUserCouponVoList().size()>1) {
                        throw new IllegalArgumentException("交通接驳订单中不允许使用多张优惠券");
                    }
                }
                
                if(Long.valueOf(43).equals(order.getCategoryId()) 
                        || Long.valueOf(43).equals(order.getCategoryId()) 
                        || Long.valueOf(45).equals(order.getCategoryId()) ){
                    if (order.getBuyInfo().getUserCouponVoList().size()>1) {
                        throw new IllegalArgumentException("当地玩乐订单中不允许使用多张优惠券");
                    }
                }

                for (UserCouponVO  c:order.getBuyInfo().getUserCouponVoList()) {
                    if (!usedConList.contains(c.getCouponCode())) {
                        usedConList.add(c.getCouponCode());
                    }else {
                        throw new IllegalArgumentException("同比订单中不允许使用相同的优惠券");
                    }
                }
            }
        }

    }

    private synchronized long getTime() {
        long time = new Date().getTime();
        return time;
    }

    protected void calcBlackList(OrdOrder order) {
        String err = blackListBussiness.isBlackList(order);
        if (StringUtils.isNotEmpty(err)) {
            throwIllegalException(err);
        }
    }

    @Override
    public OrdOrderDTO initOrderAndCalc(BuyInfo buyInfo) {
        converOldCurise(buyInfo);
        OrdOrderDTO order = new OrdOrderDTO(buyInfo);
        initOrder(order);
        calcOrderVisitTime(order);
        calcMainItem(order);
        calcOrderCategroy(order);
        //Added by yangzhenzhong 添加bu计算，for目的地bu，自由行产品 start
        calcBuCode(order);
        calcPaymentType(order);
        calcResourceConfirm(order);
        calcRebate(order);//订单返现计算
        calcOrderAmount(order);
        calcPromition(order);

        return order;
    }
    @Override
    public OrdOrderDTO initOrderAndCalcWithOutPromotion(BuyInfo buyInfo) {
        converOldCurise(buyInfo);
        OrdOrderDTO order = new OrdOrderDTO(buyInfo);
        initOrder(order);
        calcOrderVisitTime(order);
        calcMainItem(order);
        calcOrderCategroy(order);
        calcBuCode(order);
        calcPaymentType(order);
        calcResourceConfirm(order);
        calcRebate(order);//订单返现计算
        calcOrderAmount(order);

        return order;
    }
    @Override
    public OrdOrderDTO initOrderBasic(BuyInfo buyInfo) {
    	converOldCurise(buyInfo);
    	OrdOrderDTO order = new OrdOrderDTO(buyInfo);
    	initOrder(order);
    	calcOrderVisitTime(order);
    	calcMainItem(order);
    	calcOrderCategroy(order);
    	calcBuCode(order);
    	//calcPaymentType(order);
    	//calcResourceConfirm(order);
    	calcOrderAmount(order);
    	//calcRebate(order);//订单返现计算
    	//calcPromition(order);
    	
    	return order;
    }

    @Override
	public OrdOrderDTO initOrderItems(BuyInfo buyInfo) {
    	converOldCurise(buyInfo);
        OrdOrderDTO order = new OrdOrderDTO(buyInfo);
        initOrder(order);
        calcOrderVisitTime(order);
        calcMainItem(order);
        calcOrderCategroy(order);
        //Added by yangzhenzhong 添加bu计算，for目的地bu，自由行产品 start
        //calcBuCode(order);
        //end
        //calcPaymentType(order);
        //calcResourceConfirm(order);
        calcOrderAmount(order);
        //calcRebate(order);//订单返现计算
//      calcPromition(order);

        return order;
	}

    
    @Override
    public OrdOrderDTO initOrderWithBuyInfo(BuyInfo buyInfo) {
        OrdOrderDTO orderDto = new OrdOrderDTO(buyInfo);
        initOrder(orderDto);
        calcOrderAmount(orderDto);//计算订单金额
        return orderDto;
    }
    

    private void initBuyInfo(BuyInfo buyInfo){
        //判断是否共用一个游玩日期
        if(StringUtils.equals("true", buyInfo.getSameVisitTime())){
            //如果是,游玩日期不能为空
            if(StringUtils.isEmpty(buyInfo.getVisitTime())){
                throwNullException("游玩日期不存在");
            }
            //如果是,判断每个子项是否使用共用的游玩日期
            if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
                for(BuyInfo.Item item:buyInfo.getItemList()){
                    if(!"N".equals(item.getUseSameVisitTimeFlag())){
                        //如果使用共用游玩日期则为每个子项设置游玩日期
                        item.setVisitTime(buyInfo.getVisitTime());
                    }
                    
                }
            }
            //如果是,判断打包产品列表是否为空
            if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
                //如果非空,为每个产品设置游玩日期
                for(BuyInfo.Product product:buyInfo.getProductList()){
                    product.setVisitTime(buyInfo.getVisitTime());
                }
            }
        }
    }
    /**
     * 计算订单的支付类型
     * @param order
     */
    private void calcPaymentType(OrdOrderDTO order){
        //预付订单并且默认不是强制预授权才需要计算
        if(order.hasNeedPrepaid()){
            boolean setWaitPaymentTimeFlag = false; //是否需要设置WaitPaymentTime
            Date aheadTime = null;
            if(!order.isPayMentType()){ //不是强制预授权
                boolean needPreAuth = false; //是否强制预授权
                boolean isNotPreAuth = false;  //订单中是否包含预授权类型为：不使用预授权的商品
                
                for(OrdOrderItem ordItem : order.getOrderItemList()){
                    if (ordItem.getAheadTime() != null
                            && (aheadTime == null || aheadTime.after(ordItem
                                    .getAheadTime()))) {
                        aheadTime = ordItem.getAheadTime();
                    }
                }

                for(OrdOrderItem orderItem : order.getOrderItemList()){
                    //取到商品的时间价格表
                    ResultHandleT<SuppGoodsBaseTimePrice> baseTimePriceResultHandleT = suppGoodsTimePriceClientRemote.getBaseTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime());
                    SuppGoodsBaseTimePrice baseTimePrice = baseTimePriceResultHandleT == null ? null : baseTimePriceResultHandleT.getReturnContent();
                    //判断商品找中是否包含  预授权类型为：不使用预授权   的商品
                    if(baseTimePrice != null && SuppGoodsTimePrice.BOOKLIMITTYPE.NOT_PREAUTH.name().equalsIgnoreCase(baseTimePrice.getBookLimitType())) {
                        isNotPreAuth = true;
                    }
                    //如果是国内BU，只要酒店且是不退不改，就需要预授权，归属地是海南/三亚的除外
                    boolean isLocalAndHotel = false;//是否是国内线路不退不改的酒店子单,默认不是false
                    if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
                        //酒店并且是不退不改
                        if(1 == orderItem.getCategoryId() 
                                && SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())){
                            Long orderCategoryId = order.getCategoryId();
                            boolean  isExclude = false;//是否例外
                            //先判断品类(跟团游15/自由行18)
                            if(orderCategoryId != null 
                                    && (orderCategoryId.longValue() == 15 || orderCategoryId.longValue() == 18)){
                                //归属地是海南和三亚的，此规则不生效
                                isLocalAndHotel = true;
                            }
                            if(!isExclude){
                                needPreAuth = true;
                                orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), false);
                            }
                        }
                    }
                    if (orderItem.hasSupplierApi()
                            && "true".equalsIgnoreCase(orderItem
                                    .getNeedResourceConfirm())
                            && SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE
                                    .name().equalsIgnoreCase(
                                            orderItem.getCancelStrategy())) {//对接并且不退不改，直接强制预授权                     
                        if(!isLocalAndHotel){
                        	//跟团游保险与机票不走预授权
                        	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
                        			&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
                        			&&orderItem.getCategoryId()!=null&&
                        			(3 == orderItem.getCategoryId().intValue()||21 == orderItem.getCategoryId().intValue())){
                        		continue;
                        	}
                            needPreAuth = true;
                            orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), false);
                        }
                    } else if (orderItem.hasSupplierApi() && "true".equalsIgnoreCase(orderItem.getNeedResourceConfirm())) {// 需要按时间来更改强制预授权的订单
                        if(1 == orderItem.getCategoryId()){
                            //酒店的改为提前2小时
                            if(TimePriceUtils.hasPreauthBookHotel(order.getLastCancelTime(),order.getCreateTime())){
                                needPreAuth = true;//对接并且满足下单时间+取消时间的条件,更改强制预授权
                                order.setWaitPaymentTime(aheadTime);//等待时间
                            }
                        }else{
                        	//跟团游保险与机票不走预授权
                        	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
                        			&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
                        			&&orderItem.getCategoryId()!=null
                        			&&(3 == orderItem.getCategoryId().intValue()||21 == orderItem.getCategoryId().intValue())){
                        		continue;
                        	}
                            //其余的默认仍然是1小时
                            if(TimePriceUtils.hasPreauthBook(order.getLastCancelTime(),order.getCreateTime())){
                                needPreAuth = true;//对接并且满足下单时间+取消时间的条件,更改强制预授权
                                order.setWaitPaymentTime(aheadTime);//等待时间
                            }
                        }
                    } else {
                        //非对接或者不满足不退不改/下单时间+取消时间的条件
                        //根据订单下的商品判断是否需要强制 预授权(只要有一个商品是强制预授权，则认为是强制预授权)
//                      HashMap<String,Object> params = new HashMap<String,Object>();
//                      params.put("suppGoodsId", orderItem.getSuppGoods().getSuppGoodsId());
//                      params.put("date", orderItem.getVisitTime());
//                      params.put("orderByClause", "sgr.SPEC_DATE");
//                      ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT=suppGoodsTimePriceClientRemote.getFirstTimePrice(params);
                        //1.初步判断酒店
                        if(1 == orderItem.getCategoryId()){
                            //酒店的改为提前2小时
                            if(TimePriceUtils.hasPreauthBookHotel(order.getLastCancelTime(),order.getCreateTime())){
                                needPreAuth = true;
                                setWaitPaymentTimeFlag = true;
                            }
                        }
                        //2.对于酒店判断不满足,再次判断时间价格
                        if(!needPreAuth){
                            //ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientRemote.getBaseTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime());
                            //取得出游日期对应的时间价格数据
                            //SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
                            if(baseTimePrice != null && SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(baseTimePrice.getBookLimitType())) {
                                needPreAuth = true;
                                setWaitPaymentTimeFlag = true;
                            }
                        }
                        /**
                         *20150609以下情况取消强制预授权
                         *1---非对接
                         *2---单酒店
                         *3---保留房
                         *4---有库存
                         *5---超过最晚无损预定时间，且未超过最晚下定时间
                         */

                        if(needPreAuth){
                            long bale_start = System.currentTimeMillis();
                            logger.info("==================取消强制预授权，判断是否满足取消条件START=============suppGoodsID="+orderItem.getSuppGoodsId());
                            ResultHandleT<SuppGoodsTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientAdapter.getTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime(),Boolean.TRUE);
                            //取得出游日期对应的时间价格数据
                            SuppGoodsTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
                            //2---酒店
                            if(1 == orderItem.getCategoryId()){
                                logger.info("==================取消强制预授权，判断是否满足取消条件====非对接，单酒店========suppGoodsID="+orderItem.getSuppGoodsId());
                                //3---保留房
                                if ("Y".equalsIgnoreCase(timePrice.getStockFlag())) {
                                    logger.info("==================取消强制预授权，判断是否满足取消条件=====保留房=======suppGoodsID="+orderItem.getSuppGoodsId());
                                    //4---有库存
                                    if(TimePriceUtils.hasSuppStock(timePrice, Integer.parseInt(String.valueOf(orderItem.getQuantity())))){
                                        logger.info("==================取消强制预授权，判断是否满足取消条件======有库存=======suppGoodsID="+orderItem.getSuppGoodsId());

                                        //Added by yangzhenzhong  start
                                        //5-----如果是国内，大交通为否，BU是目的地，产品是自由行
                                        logger.info("==================取消强制预授权，判断是否满足取消条件======BU是目的地，产品是自由行=======suppGoodsID="+orderItem.getSuppGoodsId());
                                        if(CommEnumSet.BU_NAME.DESTINATION_BU.name().equalsIgnoreCase(order.getBuCode()) && BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(order.getCategoryId())){
                                            needPreAuth = false;
                                            logger.info("===============取消强制预授权，取消END=========花费时间="+(System.currentTimeMillis()-bale_start)+"======suppGoodsID="+orderItem.getSuppGoodsId());
                                        }else{ //Added by yangzhenzhong end

                                            //6---超过最晚无损预定时间，且未超过最晚下定时间
                                            Date order_date = new Date();//下单时间，取当前时间
//                                          Date lastHoldTime = CalendarUtils.getEndDateByMinute(timePrice.getSpecDate(), timePrice.getLatestHoldTime());
                                            logger.info("==================取消强制预授权，判断时间=============suppGoodsID="+orderItem.getSuppGoodsId()+"====isbeforelastHoldTime="+timePrice.isBeforeLastHoldTime(order_date)+"==aheadTime:"+aheadTime+"=====lastCancelTime:"+orderItem.getLastCancelTime());
                                            if(order_date.after(orderItem.getLastCancelTime())&&timePrice.isBeforeLastHoldTime(order_date)){
                                                needPreAuth = false;
                                                logger.info("===============取消强制预授权，取消END=========花费时间="+(System.currentTimeMillis()-bale_start)+"======suppGoodsID="+orderItem.getSuppGoodsId());
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    
                    //跟团游对接
                    if(orderItem.hasSupplierApi() && orderItem.getCategoryId()==15L && SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())){
                        needPreAuth = false;
                        order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NOT_PREAUTH.name());
                        break;
                    }
                    
                    if(needPreAuth) {
                        order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
                        break;
                    }
                }
                //如果包含不使用预授权商品，并且根据上面的逻辑订单为不强制预授权，并且paymenttype的值为NONE,设置订单为不使用预授权
                if(!needPreAuth && isNotPreAuth
                        && SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equalsIgnoreCase(order.getPaymentType())){
                    order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NOT_PREAUTH.name());
                }
                //非强制预授权并且所有资源满足
                if(!needPreAuth && order.hasResourceAmple()) {
                    //调用方发起的时间修改
                    if(order.getBuyInfo().getWaitPayment()!=null&&order.getBuyInfo().getWaitPayment()>0){
                        Date waitPayment = DateUtils.addMinutes(order.getCreateTime(), order.getBuyInfo().getWaitPayment().intValue());
//                      if(order.getLastCancelTime()!=null){
//                          if(order.getLastCancelTime().after(waitPayment)){
                                order.setWaitPaymentTime(waitPayment);
                                order.setApproveTime(order.getCreateTime());
//                          }
//                      }
                    }else{
                        //资源默认通过的修改
                    	//线路下单默认
                    	if((order.getCategoryId()==15L||order.getCategoryId()==16L||order.getCategoryId()==17L||order.getCategoryId()==18L)){
                    		//并且是团结算产品
                    		if(isGroupSettle(order.getProductId())){
                    			//团结算订单支付等待时间=订单创建日期+24h
                    			Date waitPaymentTime=DateUtils.addMinutes(order.getCreateTime(), 24*60);
                    			order.setWaitPaymentTime(waitPaymentTime);
                    		}else{
                    			orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), true);
                    		}
                    	}else{
                    		orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), true);
                    	}
                    	order.setApproveTime(order.getCreateTime());
                        
                    }
                }
            }else{//表示就是一律预授权的订单，直接设置
                setWaitPaymentTimeFlag = true;
            }

            
            //设置支付等待时间
            if(setWaitPaymentTimeFlag) {
                if(order.getWaitPaymentTime()==null){
                	if(isGroupSettle(order.getProductId())){
            			//团结算订单支付等待时间=订单创建日期+24h
            			Date waitPaymentTime=DateUtils.addMinutes(order.getCreateTime(), 24*60);
            			order.setWaitPaymentTime(waitPaymentTime);
            		}else{
            			 orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), false);
            		}
                    /*if(order.getLastCancelTime()!=null){
                        Date lastTime = TimePriceUtils.getLastCancelTime(order);
                        if(order.getWaitPaymentTime().after(lastTime)){
                            order.setWaitPaymentTime(lastTime);
                        }
                    }*/
                }
                order.setApproveTime(order.getCreateTime());
            }

            if(ProductCategoryUtil.isTicket(order.getCategoryId())){
                if(order.getWaitPaymentTime()!=null){
                    // 门票品类支付等待时间业务变更 20141205 start
                    logger.info("门票品类支付等待时间业务变更 20141205 orderId = "+order.getOrderId() +"start");
                    List<Integer> minutes = new ArrayList<Integer>();
                    //是否是门票品类
                    boolean isTicketFlag = false;
                    //默认设为通用的支付等待时间
                    Date newWaitPaymentTime = order.getWaitPaymentTime();
                    logger.info("处理前支付等待时间是"+ newWaitPaymentTime );
                    for(OrdOrderItem orderItem : order.getOrderItemList()){
                        if(orderItem.hasTicketAperiodic()){
                            continue;
                        }
                        String categoryCode = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
                        logger.info("品类code是"+ categoryCode);
                        if(ProductCategoryUtil.isTicket(categoryCode)){
                            //取得出游日期对应的时间价格数据
                            ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientRemote
                                    .getBaseTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime());
                            SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
                            //取得出游日期对应的时间价格数据
                            if(timePrice != null ) {
                                //时间价格表中提前预定时间
                                minutes.add(timePrice.getAheadBookTime().intValue());
                                logger.info("时间价格表中提前预定时间是"+ timePrice.getAheadBookTime().intValue());
                            }
                            isTicketFlag = true;
                        }
                        logger.info("门票品类判断结果"+ isTicketFlag);
                    }
                    if(isTicketFlag){
                        logger.info("门票品类判断结果"+ isTicketFlag);
                        //下单时间到出游日的剩余时间
                        long leftTime =(long)(DateUtil.getMinute(order.getCreateTime(),order.getVisitTime()));
                        //品类中最大提前预定时间和下单时间之差
                        Long waitMinute = leftTime - Collections.max(minutes);
                        logger.info("提前预定时间和下单时间之差是"+ waitMinute );
                        //提前预定时间和下单时间之差大于0小于120分钟的时候
                        //if(waitMinute < 120 && waitMinute >0){

                        //设置新的支付等待时间
                        newWaitPaymentTime = DateUtils.addMinutes(order.getCreateTime(), waitMinute.intValue());
                        //
                        if(newWaitPaymentTime.before(order.getWaitPaymentTime())){
                            order.setWaitPaymentTime(newWaitPaymentTime);
                        }

                        logger.info("处理后门票的支付等待时间是"+ newWaitPaymentTime);
                            //}
                        
                        if(BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(order.getCategoryId())){
                            //永乐演出票
                            IntfStylProdRela intfStylProdRela = suppTicketProductClientService.queryExistSuppProdRela(order.getProductId());
                            if(intfStylProdRela!=null){
                                order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), 15));
                            }
                        }
                    }
                    logger.info("处理后的支付等待时间是"+ newWaitPaymentTime);
                    //门票品类支付等待时间业务变更 20141205 end
                    logger.info("门票品类支付等待时间业务变更 20141205 end");
                }
            }

			if(!order.isPayMentType()){//不是强制预授权
				//对包含对接机票的订单做特殊处理：包含对接机票的订单在资源审核之前不设置支付等待时间
				if (order.isContainApiFlightTicket() && !order.hasResourceAmple()) {//订单资源审核未审核
					order.setWaitPaymentTime(null);
				}
			}else{
				//强制预授权,包含对接机票订单，支付等待时间统一设置为30分钟
				if(order.isContainApiFlightTicket()){
					order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), 30));
				}
			}
			// 分销下单且目的地BU的单酒店,支付等待时间默认为2小时，如果有预支付时间，取较小者   PS:add by wuxz
			if(Constant.DIST_BRANCH_SELL == order.getDistributorId() && CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
			        && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()){
				order.setApproveTime(order.getCreateTime());
				order.setWaitPaymentTime(calcWaitPaymentTime(aheadTime, order.getCreateTime(), 120));//等待时间
			}
			
			// 前台下单且目的地BU自由行(含酒店或酒店套餐)或酒店套餐，或单酒店,支付等待时间统一为2小时   PS:add by xiaoyulin
			if(OrdOrderUtils.isDestBuFrontOrder(order)){
				order.setApproveTime(order.getCreateTime());
				order.setWaitPaymentTime(OrdOrderUtils.calcWaitPaymentTimeForDestBu(aheadTime, order.getCreateTime()));//等待时间
			}
			if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
				boolean isInquiry =isInquiry(order);
				//TODO 应分销高亮要求，只要走询位，都将主单支付时间设置为空
	           if(isInquiry){
	        	   order.setWaitPaymentTime(null);
	           }
	           else if(isInquiryTaoBao(order)){
	        	   logger.info("isInquiryTaoBao"+isInquiryTaoBao(order));
	        	   order.setWaitPaymentTime(OrdOrderUtils.calcWaitPaymentTimeForDestBuTaoBao(aheadTime, order.getCreateTime()));//等待时间
	           }else{
	        	   // O2O景+酒支付时间为半小时 add by renjiangyi
	        	   order.setApproveTime(order.getCreateTime());
	        	   order.setWaitPaymentTime(OrdOrderUtils.calcWaitPaymentTimeForDestBu(aheadTime, order.getCreateTime()));//等待时间
	           }
	           logger.info("isInquiry =" +isInquiry);
			}
			
			//B2B渠道 国内酒店、酒店套餐、酒+景 订单支付等待时间修改为30分钟
			if(isTravelAround(order)){
				order.setApproveTime(order.getCreateTime());
				order.setWaitPaymentTime(calcWaitPaymentTime(aheadTime, order.getCreateTime(), 30));
			}
		}

	}
    
    /**
     * 周边游
     * @param order
     * @return
     */
    private boolean isTravelAround(OrdOrderDTO order) {
    	return VstOrderEnum.DISTRIBUTION_CHANNEL.DISTRIBUTOR_B2B.getCode().equals(order.getDistributorCode())
    		   && (CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
    		   ||  CommEnumSet.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode()))
    		   && (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())
    		   ||  BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
    		   ||  BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId()));
	}
  
	/***
     * 判断产品是否是团结算
     * @param productId
     * @return
     */
    private boolean isGroupSettle(Long productId) {
    	ProdProduct prod=productClientService.findProdProductById(productId, true, true);
		if(null!=prod&&!prod.getPropValue().isEmpty()&&prod.getPropValue().containsKey("group_settle_flag")){
			if(StringUtil.isNotEmptyString(String.valueOf(prod.getPropValue().get("group_settle_flag")))&&"Y".equals(String.valueOf(prod.getPropValue().get("group_settle_flag")))){
				return true;
			}
		}
		return false;
	}

	/**
     * 是否走询位
     * @param order
     * @return 
     */
    private boolean isInquiry(OrdOrderDTO order){
        if(CollectionUtils.isEmpty(order.getOrderItemList())){
            return false;
        }
        boolean isInquiry =false;
        for(OrdOrderItem orderItem :order.getOrderItemList()){
            //非对接，需要资审
            logger.info("orderItem.hasSupplierApi()=="+orderItem.hasSupplierApi());
             logger.info("orderItem.hasResourceAmple()==orderItem.getResourceStatus()"+orderItem.getResourceStatus());
            if(!orderItem.hasSupplierApi() && !orderItem.hasResourceAmple()){
                isInquiry =true;
                break;
            }
        }
        return isInquiry;
    }
    
    private boolean isInquiryTaoBao(OrdOrderDTO order){
        if(CollectionUtils.isEmpty(order.getOrderItemList()) ||!"DISTRIBUTOR_TAOBAO".equals(order.getDistributorCode())){
              return false;
            }
             //全部，非对接，保留房
              boolean isInquiryTaoBao =true;
              for(OrdOrderItem orderItem :order.getOrderItemList()){
               logger.info("orderItem.hasSupplierApi()=DISTRIBUTOR_TAOBAO="+orderItem.hasSupplierApi());
                logger.info("orderItem.hasResourceAmple()==DISTRIBUTOR_TAOBAO"+orderItem.getResourceStatus());
                 if(orderItem.hasSupplierApi()|| !orderItem.hasResourceAmple() ){
	                    isInquiryTaoBao =false;
                    break;
                   }
              }
            return isInquiryTaoBao;
           }
	/**
	 * 支付前置订单支付等待时间计算
	 * @param aheadTime
	 * @param createTime
	 * @param defaultWaitPaymentMinute
	 * @return
	 */
	public static Date calcWaitPaymentTime(Date aheadTime, Date createTime, int defaultWaitPaymentMinute) {
		Date waitPaymentTime = DateUtils.addMinutes(createTime, defaultWaitPaymentMinute);
		if(aheadTime != null && waitPaymentTime.after(aheadTime)){
			waitPaymentTime = aheadTime;
		}
		return waitPaymentTime;
	}
	/**
	 * 设置强制预授权判断
	 * 
	 * 
	 * 前提条件：
	 * 1、子单包含机票和酒店的主订单
	 * 2、bu:local_bu
	 * 
	 * 满足以下任意一个条件：
	 * 1、任何一个商品设置为强制预授权
	 * 2、任何一个商品设置为不退不改
	 * 3、所有商品的支付等待时间>0(规则是只要有支付等待闪小于0的，即为强制预授权)
	 * 
	 * 支付等待时间计算：
	 * 酒店支付等待时间=入住时间-下单时间-最晚无损取消时间-10分钟
	 * 机票支付等待时间=1小时（含30分钟锁仓时间）
	 * 门票支付等待时间大于2小时（默认）
	 * 酒店套餐与酒店等待时间计算规则相同
	 * 线路支付等待时间大于2小时（默认）
	 * 非对接机票支付等待时间大于2小时（默认）
	 * 
	 * 最晚无损取消时间=下单时间+所有商品的最小的支付等待时间
	 * 
	 * @param order
	 */
	private void calcFlightAndHotelPaymentType(OrdOrderDTO order){
		logger.info("method:calcFlightAndHotelPaymentType begin...order PayMentType is " + (order !=null ? order.isPayMentType() : ""));
		//前提条件+预付订单并且默认不是强制预授权才需要设置强制预授权
		if(judgmentCondition(order) && order.hasNeedPrepaid()){
			if(!order.isPayMentType()){
				boolean needPreAuth = false;//是否强制预授权
				// 酒店支付等待时间
				int hotelLastCancelTime = 0;
				// 酒店最晚无损取消时间
				int minHotelLastCancelTime = 0;
				// 酒店最早入住时间
				Date minHotelVisitTime = null;
				// 门票支付等待时间
				int ticketLastCancelTime = 0;
				// 门票最晚无损取消时间
				int minTicketLastCancelTime = 0;
				// 门票最早游玩时间
				Date minTicketVisitTime = null;
				boolean flag = true;
				boolean isApiFlightTicketFlag = false;
				for(OrdOrderItem orderItem : order.getOrderItemList()){
					// 取到商品的时间价格表
					ResultHandleT<SuppGoodsBaseTimePrice> baseTimePriceResultHandleT = suppGoodsTimePriceClientRemote
							.getBaseTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime());
					SuppGoodsBaseTimePrice baseTimePrice = baseTimePriceResultHandleT == null ? null : baseTimePriceResultHandleT.getReturnContent();
					if(baseTimePrice != null && baseTimePrice.getLatestCancelTime() != null){
						// 酒店或者酒店套餐
						if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())
								|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) {
							if(minHotelLastCancelTime < baseTimePrice.getLatestCancelTime().intValue()){
								minHotelLastCancelTime = baseTimePrice.getLatestCancelTime().intValue();
							}
							if(orderItem.getVisitTime() != null){
								if(minHotelVisitTime == null){
									minHotelVisitTime = orderItem.getVisitTime();
								}else{
									if(minHotelVisitTime.after(orderItem.getVisitTime())){
										minHotelVisitTime = orderItem.getVisitTime();
									}
								}
							}
						}
						if(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId().equals(orderItem.getCategoryId())
								|| BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
								||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId())
								|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getCategoryId())){
							if(minTicketLastCancelTime < baseTimePrice.getLatestCancelTime().intValue()){
								minTicketLastCancelTime = baseTimePrice.getLatestCancelTime().intValue();
							}
							if(orderItem.getVisitTime() != null){
								if(minTicketVisitTime == null){
									minTicketVisitTime = orderItem.getVisitTime();
								}else{
									if(minTicketVisitTime.after(orderItem.getVisitTime())){
										minTicketVisitTime = orderItem.getVisitTime();
									}
								}
							}
						}
					}
					if(orderItem.isApiFlightTicket()) {
						isApiFlightTicketFlag = true;
					}
				}
				boolean hotelFlag = false;
				if(minHotelVisitTime != null){
					hotelFlag = true;
					hotelLastCancelTime = (int) ((minHotelVisitTime.getTime() - order.getCreateTime().getTime())/(1000*60)) - minHotelLastCancelTime - 10;
					logger.info("酒酒店最早入住时间是：" + minHotelVisitTime);
					logger.info("酒店最晚无损取消时间是：" + minHotelLastCancelTime);
					logger.info("酒店或酒店套餐的支付等待时间是：" + hotelLastCancelTime);
				}
				boolean ticketFlag = false;
				if(minTicketVisitTime != null){
					ticketFlag = true;
					ticketLastCancelTime  = (int) ((minTicketVisitTime.getTime() - order.getCreateTime().getTime())/(1000*60)) - minTicketLastCancelTime;
					logger.info("门票最早游玩时间是：" + minTicketVisitTime);
					logger.info("门票最晚无损取消时间是：" + minTicketLastCancelTime);
					logger.info("门票的支付等待时间是：" + ticketLastCancelTime);
				}
				// 对接
				int lastCancelTime = 60;
				if(!isApiFlightTicketFlag){
					// 非对接
					lastCancelTime = 120;
				}
				logger.info("lastCancelTime is :" + lastCancelTime + " isApiFlightTicketFlag is :" + isApiFlightTicketFlag);
				if(hotelFlag && lastCancelTime > hotelLastCancelTime){
					lastCancelTime = hotelLastCancelTime;
				}
				if(ticketFlag && lastCancelTime > ticketLastCancelTime){
					lastCancelTime = ticketLastCancelTime;
				}
				if(hotelLastCancelTime < 0 || ticketLastCancelTime < 0){
					order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
				}
				logger.info("订单id:" + order.getOrderId() + ",支付等待时间:" + order.getLastCancelTime()+ ",是否预授权:" + order.getPaymentType());
				for(OrdOrderItem orderItem : order.getOrderItemList()){
					//取到商品的时间价格表
					ResultHandleT<SuppGoodsBaseTimePrice> baseTimePriceResultHandleT = suppGoodsTimePriceClientRemote
							.getBaseTimePrice(orderItem.getSuppGoods().getSuppGoodsId(), orderItem.getVisitTime());
					SuppGoodsBaseTimePrice baseTimePrice = baseTimePriceResultHandleT == null ? null : baseTimePriceResultHandleT.getReturnContent();
					if(baseTimePrice != null && SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(baseTimePrice.getBookLimitType())) {
						//满足条件1(任何一个商品设置为强制预授权)
						needPreAuth = true;
					}
					//如果是国内BU，只要酒店且是不退不改，就需要预授权，归属地是海南/三亚的除外
					boolean isLocalAndHotel = false;//是否是国内线路不退不改的酒店子单,默认不是false
					if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
						//酒店并且是不退不改
						if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())
								&& SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())){
							boolean  isExclude = false; //是否例外
							// 归属地是海南和三亚的，此规则不生效
							isLocalAndHotel = true;
							if(!isExclude){
								needPreAuth = true;
							}
						}	
					}
					if (orderItem.hasSupplierApi()
							&& "true".equalsIgnoreCase(orderItem
									.getNeedResourceConfirm())
							&& SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE
									.name().equalsIgnoreCase(
											orderItem.getCancelStrategy())) { //对接并且不退不改，直接强制预授权						
						if(!isLocalAndHotel){
							needPreAuth = true;
						}
					}
					if(flag && needPreAuth) {
						order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
						flag = false;
						break;
					}
				}
				logger.info("最晚无损时间是:"+ lastCancelTime);
				if(StringUtils.isNotEmpty(order.getPaymentType()) && order.getPaymentType().equalsIgnoreCase(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name())){
					Date waitPaymentTime = DateUtils.addMinutes(order.getCreateTime(), lastCancelTime);
					logger.info("新的支付等待时间是:" + waitPaymentTime);
					order.setWaitPaymentTime(waitPaymentTime);
				}
				logger.info("calcFlightAndHotelPaymentTypeEnd1：{产品id:" + order.getProductId() + ",订单id:" + order.getOrderId() + ",LastCancelTime():" + order.getLastCancelTime()
					+ ",WaitPaymentTime():" + order.getWaitPaymentTime() + ",是否预授权:" + order.getPaymentType() + "}");
			} else {
				// 不需要审核的逻辑，支付等待时间设置为默认
				boolean isApiFlightTicketFlag = false;
				for(OrdOrderItem item : order.getOrderItemList()) {
					if(item.isApiFlightTicket()) {
						isApiFlightTicketFlag = true;
						break;
					}
				}
				int minutes = 60;
				if(!isApiFlightTicketFlag){
					// 非对接机票默认为2小时
					minutes = minutes * 2;
				}
				order.setWaitPaymentTime(DateUtils.addMinutes(order.getCreateTime(), minutes));
				logger.info("calcFlightAndHotelPaymentTypeEnd2：{产品id:" + order.getProductId() + ",订单id:" + order.getOrderId() + ",LastCancelTime():" + order.getLastCancelTime()
					+ ",WaitPaymentTime():" + order.getWaitPaymentTime() + ",是否预授权:" + order.getPaymentType() + "}");
			}
		}
		if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			Date waitPaymentTime = order.getWaitPaymentTime();
			if(waitPaymentTime!=null&&waitPaymentTime.before(new Date())){
				order.setWaitPaymentTime(DateUtils.addMinutes(new Date(),120));
			}
		}
	}
	
	/**
	 * 判断前提条件
	 * 1、国内bu
	 * 2、包含机票和酒店
	 * 
	 * @param order
	 * @return
	 */
	private boolean judgmentCondition(OrdOrderDTO order){
		boolean flag = false;
		if(order != null && CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			List<Long> categoryIds = Lists.newArrayList();
			if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				for(OrdOrderItem orderItem : order.getOrderItemList()){
					if(orderItem != null){
						categoryIds.add(orderItem.getCategoryId());
					}
				}
			}
			if(CollectionUtils.isNotEmpty(categoryIds)){
				if(categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId())
						&& categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId())){
					flag = true;
				}
			}
		}
		return flag;
	}
	/**
     * 出境单酒店判断
     * @return
     */
    private boolean isOutboundBuAndPrepaidHotel(OrdOrderDTO order){     
        if(order.hasNeedPrepaid() 
                && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())
                && (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue() == order.getCategoryId().longValue() )
                    ){
        	logger.info("[isOutboundBuAndPrepaidHotel]");
              return true;
             }
        return false;
    }
    
    /**
     * 出境景+酒店判断
     * @return
     */
    private boolean isOutboundBuAndPrepaidHotelAndTicket(OrdOrderDTO order){ 
    	//出境 自由行 景+酒
        if(order.hasNeedPrepaid() 
                && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())
                && (BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue() == order.getCategoryId().longValue() )
                && (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().longValue() == order.getSubCategoryId().longValue() )
                    ){ 
        		logger.info("[isOutboundBuAndPrepaidHotelAndTicket]");
	        	List<OrdOrderItem>itemList = order.getOrderItemList();
	            for(OrdOrderItem item:itemList){
	            	if( item.hasCategory(BIZ_CATEGORY_TYPE.category_hotel)  && item.hasSupplierApi() ){
	            		logger.info("[isNotOutboundBuAndPrepaidHotelAndTicket ]"+order.getOrderId());
	            		return true;
	            	}
	            }
	            return false;              
             }
        return false;
    }
    
    /**
     * 是否为对接订单
     * @return
     */
    private boolean isSupplierOrder(OrdOrderDTO order){
        logger.info("[isSupplierOrder]"+order.getSupplierApiFlag());        
        if( "Y".equals(order.getSupplierApiFlag())){
            return true;
        }
        return order.isSupplierOrder();
    }
    
    private boolean isSetOutboundBuHotelItemWorkflow(OrdOrderDTO order){
    	String subProcessKey ="outboundbu_hotel_dock";
    	List<OrdOrderPack> packList = order.getOrderPackList();
        if( null != packList && !packList.isEmpty() ){
        	for(OrdOrderPack ordOrderPack:packList ){
        		Long itemProductId = ordOrderPack.getProductId();
        		logger.info("[itemProductId]"+itemProductId);
        		List<OrdOrderItem> orderItemList = ordOrderPack.getOrderItemList();
        		for ( OrdOrderItem ordOrderItem:orderItemList  ){
        			logger.info("[itemCategoryId]"+ordOrderItem.getCategoryId());
        			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==ordOrderItem.getCategoryId().longValue()){
        				logger.info("[putContent]"+ordOrderItem.getContent());
        				ordOrderItem.putContent("processKey", subProcessKey);
        				ordOrderItem.setSubProcessKey(subProcessKey);
        				this.outboundBuRouteHotelTotalFSettlement(ordOrderItem,ordOrderItem.getSuppGoodsId(),new Date());
        				logger.info("[putContent]"+ordOrderItem.getContent());
        				return true;
        			} 
        		}
        	}
        }
        return false;
    }
    
    private boolean outboundBuRouteHotelTotalFSettlement(OrdOrderItem ordOrderItem,Long suppGoodsId,Date specDate){
		ResponseBody<HotelCurrencyInfoVstVo> response = new ResponseBody<>();
		RequestBody<Map<String, Object>> request = new RequestBody<>();
        Map<String, Object> paramMap = new HashMap<>();
        List<Date> visitTimeList = new ArrayList<Date>();
        Date specYMDDDate = DateUtil.toYMDDate(specDate);
        paramMap.put("suppGoodsId", suppGoodsId);
        paramMap.put("specDate", specYMDDDate);
        //必须包含指定日期
        List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
        if (null != orderHotelTimeRateList && orderHotelTimeRateList.size() > 0) {
            for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : orderHotelTimeRateList) {
                visitTimeList.add(ordOrderHotelTimeRate.getVisitTime());
            }
        }
        paramMap.put("visitTimeList", visitTimeList);
        request.setT(paramMap);
        request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        response = hotelGoodsTimePriceQVstApiRemote.findCurrencyCodeBySuppGoodsId(request);
		HotelCurrencyInfoVstVo hotelCurrencyInfo = response.getT();
		logger.info("[outboundBuRouteHotelTotalFSettlement][specDate]"+specDate+"[suppGoodsId]"+suppGoodsId);
		logger.info("[hotelCurrencyInfo]"+hotelCurrencyInfo+"[paramMap]"+paramMap);
		if (null != hotelCurrencyInfo) {
            String currencyCode = hotelCurrencyInfo.getCurrencyCode();
            BigDecimal exRateBig = hotelCurrencyInfo.getCashSellRate();
            Long firstDaySettlement = hotelCurrencyInfo.getFirstDaySettlement();
            Long totalFSettlement = hotelCurrencyInfo.getTotalFSettlement();
            Map<String, Long> dailySettlement = hotelCurrencyInfo.getDailySettlement();
            logger.info("[specDate]"+specDate+"[suppGoodsId]"+suppGoodsId+"[totalFSettlement]"+totalFSettlement);
            logger.info("[specDate]"+specDate+"[suppGoodsId]"+suppGoodsId+"[dailySettlement]"+JSONUtil.bean2Json(dailySettlement));
            if (null != exRateBig) {
                ordOrderItem.putContent("currencyCode", currencyCode);
                ordOrderItem.putContent("cashSellRate", exRateBig);
                ordOrderItem.putContent("firstDaySettlement", firstDaySettlement);
                ordOrderItem.putContent("totalFSettlement", totalFSettlement);
                if (null != dailySettlement && dailySettlement.size() > 0) {
                    ordOrderItem.putContent("dailySettlement", JSONUtil.bean2Json(dailySettlement));                                
                }else{
                	logger.info("dailySettlement is null or size is zero suppGoodsId: " + suppGoodsId);
                	return false;
                }
            }
        }
		return true;
		
	}
    
    /**
     * 出境酒店相关工作流
     * @param order
     * @return
     */
    private boolean isSetOutboundBuHotelWorkflow(OrdOrderDTO order){
    	//出境单酒店流程判断
        if( this.isOutboundBuAndPrepaidHotel(order) ){              
            if( this.isSupplierOrder(order)){
                //走对接流程
                order.setProcessKey("outboundbu_single_hotel_prepaid_order");
                return true;
            }
        }
        //出境景+酒流程判断
        if( this.isOutboundBuAndPrepaidHotelAndTicket(order) ){
        	//走对接流程
            order.setProcessKey("outboundbu_ticket_hotel_order_prepaid_main");                
            this.isSetOutboundBuHotelItemWorkflow(order);
            return true;
        }
        
        return false;
    }
	private void calcWorkflow(OrdOrderDTO order){
		logger.info("ActivitiAble="+Constant.getInstance().isActivitiAble());
		if (Constant.getInstance().isActivitiAble()) {
			//出境单酒店流程判断
            if( this.isSetOutboundBuHotelWorkflow(order) ){              
                    return;
            }
            boolean isRequestRouteToNewSys = vstOrderRouteService.isRequestRouteToNewSys();
			if(OrdOrderUtils.isTicketOrderNew(order)&&order.hasNeedPrepaid()){
				if(ConfirmEnum.isTicketOrderNew(order)){
					// 适配新订单系统-订单流程 by xiaoyulin
//					order.setProcessKey("ticket_new_order_prepaid_main");
					if(isRequestRouteToNewSys){
						order.setProcessKey("ticket_neworder_prepaid_main");
					}else{
						order.setProcessKey("ticket_new_order_prepaid_main");
					}
				}else{
					order.setProcessKey("ticket_order_prepaid_main");
				}
				for (OrdOrderItem item : order.getOrderItemList()) {
					if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(item.getCategoryId())
							||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(item.getCategoryId())
							||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(item.getCategoryId())){
						// 适配新订单系统-订单流程
//						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "ticket_new");
						if(isRequestRouteToNewSys){
							item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "ticket_neworder");
						}else{
							item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "ticket_new");
						}
					}
				}
				return;
			}
			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()
					.equals(order.getCategoryId())) {
				if (order.hasNeedPrepaid()) {
                    if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
                        order.setProcessKey("o2o_new_single_hotel_pre_order");
                        order.setWorkVersion(ConfirmUtils.WORK_VERSION_NEW_O2O);
                        calcDestBuSubWorkflow(order);
                    }else if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
						// 适配新订单系统-订单流程 by xiaoyulin
                    	if(isRequestRouteToNewSys){
                            //TODO 这里改造使用新版单酒店工作流
//                    	    if(isRouteToNewWorkflow(order)){
//                                order.setNewWorkflowFlag("Y");
//                                String version=vstOrderRouteService.getVersion();
//                                String processKey="single_hotel_main_"+version;
//                                printLog(order,processKey);
//                                order.setProcessKey(processKey);
//                            }else{
                                order.setProcessKey("single_hotel_pre_order_v3");
//                            }

                    	}else{
                    		order.setProcessKey("destbu_new_single_hotel_pre_order");
                    	}
						order.setWorkVersion(ConfirmUtils.WORK_VERSION_NEW);
						if(order.getOrderItemList() !=null){
							order.getOrderItemList().get(0).setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
						}
					}else if(OrdOrderUtils.isDestBuFrontOrder(order)){// 目的地酒店，前台,后台下单走预付支付前置流程
						order.setProcessKey("destbu_single_hotel_pre_order");
					}else{
						order.setProcessKey("single_hotel_pre_order");
					}
				} else {
					order.setProcessKey("single_hotel_pay_order");
				}
			} else {
				if (order.hasNeedPrepaid()) {
					if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
						order.setProcessKey("o2o_order_prepaid_main");
						order.setWorkVersion(ConfirmUtils.WORK_VERSION_NEW_O2O);
					}else if(OrdOrderUtils.isDestBuFrontOrderNew(order)) {//// 的地前台,后台下单走预付支付前置流程
						order.setProcessKey("destbu_order_prepaid_main");
						order.setWorkVersion(ConfirmUtils.WORK_VERSION_NEW);
					}else if(OrdOrderUtils.isDestBuFrontOrder(order)){//// 目的地前台,后台下单走预付支付前置流程
						order.setProcessKey("destbu_order_prepaid_main");
					}else{
						order.setProcessKey("order_prepaid_main");
					}
					calcDestBuSubWorkflow(order);
				} else {
					order.setProcessKey("order_pay_main_process");
				}
			}
        }

    }

	private void calcWorkflow4Ord2(OrdOrderDTO order) {
		if (vstOrderRouteService.isRequestRouteToNewSys4Ord2() &&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())) {
			if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
				logger.info("new workflow flag is: " + VstRouteConstants.FLAG_S);
				order.setNewWorkflowFlag(VstRouteConstants.FLAG_S);
				String processKey="_hotelcomb_main_v1";
				List<OrdProcessKey> ordProcessKeys = new ArrayList<OrdProcessKey>();
				OrdProcessKey ordProcessKey;
				// 保存审核工作流
				ordProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(), OrdProcessKeyEnum.KEY_TYPE.approve.name());
				ordProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.approve.name() + processKey);
				ordProcessKeys.add(ordProcessKey);
				// 保存支付工作流
				ordProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(), OrdProcessKeyEnum.KEY_TYPE.payment.name());
				ordProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.payment.name() + processKey);
				ordProcessKeys.add(ordProcessKey);
				// 保存取消工作流
				ordProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(), OrdProcessKeyEnum.KEY_TYPE.cancel.name());
				ordProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.cancel.name() + processKey);
				ordProcessKeys.add(ordProcessKey);
				order.setOrdProcessKeyList(ordProcessKeys);
				if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
					for (OrdOrderItem item : order.getOrderItemList()) {
						String subProcessKey = item.getSubProcessKey();
						if ("destbu_hotelcomb_new_sub_process".equals(subProcessKey)) {
							subProcessKey = "hotelcomb_v1_sub";
						}
						List<OrdProcessKey> itemOrdProcessKeys = new ArrayList<OrdProcessKey>();
						OrdProcessKey itemOrdProcessKey;
						// 保存审核工作流
						itemOrdProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name(), OrdProcessKeyEnum.KEY_TYPE.approve.name());
						itemOrdProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.approve.name() + "_" + subProcessKey);
						itemOrdProcessKeys.add(itemOrdProcessKey);
						// 保存支付工作流
						itemOrdProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name(), OrdProcessKeyEnum.KEY_TYPE.payment.name());
						itemOrdProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.payment.name() + "_" + subProcessKey);
						itemOrdProcessKeys.add(itemOrdProcessKey);
						// 保存取消工作流
						itemOrdProcessKey = new OrdProcessKey(null, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name(), OrdProcessKeyEnum.KEY_TYPE.cancel.name());
						itemOrdProcessKey.setKeyValue(OrdProcessKeyEnum.KEY_TYPE.cancel.name() + "_" + subProcessKey);
						itemOrdProcessKeys.add(itemOrdProcessKey);
						item.setOrdProcessKeyList(itemOrdProcessKeys);
					}
				}
			}
		}
	}

	/**
     * 计算资源相关
     * @param order
     */
    private void calcResourceConfirm(OrdOrderDTO order){
        Log.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++"+order);
        if (order!=null&&CollectionUtils.isNotEmpty(order.getOrderItemList())) {
            Log.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++"+order.getOrderItemList().size());
            for(OrdOrderItem orderItem:order.getOrderItemList()){
                if(CollectionUtils.isNotEmpty(orderItem.getOrderStockList())){

                    for(OrdOrderStock stock:orderItem.getOrderStockList()){
                        if("true".equalsIgnoreCase(stock.getNeedResourceConfirm())){
                            orderItem.setNeedResourceConfirm("true");
                            break;
                        }
                    }
                    String status=orderItem.getOrderStockList().get(0).getResourceStatus();
                    int size = orderItem.getOrderStockList().size();
                    if(size>1){
                        for(int i=1;i<orderItem.getOrderStockList().size();i++){
                            status = getOrderResourceStatus(status, orderItem.getOrderStockList().get(i).getResourceStatus());
                        }
                    }
                    if(orderItem.getCategoryId().equals(99L)){
                        orderItem.setResourceStatus("AMPLE");
                    }else{
                        orderItem.setResourceStatus(status);
                    }
                    //计算主订单的资源状态
                    setOrderResourceStatus(orderItem, order);

                }


            }

        }
    }
    
    /**
     * 计算促销活动相关的东西
     * @param order
     */
    private void calcPromition(OrdOrderDTO order){
        long discountAmount=0;
    	if(MapUtils.isNotEmpty(order.getBuyInfo().getPromotionNewMap())){//促销3.0
            initPromotion(order);//计算完订单总价后，才能获取促销
    		 for(Map.Entry<String, List<OrdPromotion>> entry:order.getPromotionMap().entrySet()){
                 List<OrdPromotion> list = entry.getValue();
                 for(OrdPromotion op:list){
                	 discountAmount+=op.getFavorableAmount();
                 }
             }
    	}else if(MapUtils.isNotEmpty(order.getPromotionMap())){
            //支付渠道
            String paymentChannel=null;
            for(String key:order.getPromotionMap().keySet()){
                List<OrdPromotion> list = order.getPromotionMap().get(key);
                for(OrdPromotion op:list){
                    if(op.getPromFavorable().hasApplyAble()){
                        long amount = op.getPromFavorable().getDiscountAmount();
                        discountAmount+=amount;
                        op.setFavorableAmount(amount);
                        //orderSaveService.setOrdPromotionFavorableAmount(op);
                        if(Constant.ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.name().equalsIgnoreCase(op.getPromotion().getPromitionType())){
                            if(StringUtils.isNotEmpty(paymentChannel)){
                                throwIllegalException("渠道促销一订单只允许使用一次");
                            }
                            paymentChannel = op.getPromotion().getChannelOrder();
                        }
                    }
                }
            }
            if(paymentChannel!=null){
                order.setPromPaymentChannel(paymentChannel);
            }
            
        }else{
            logger.info("order.getPromotionMap() is null");
        }
    	if(discountAmount>0){
            if(discountAmount>order.getOughtAmount()){
                discountAmount = order.getOughtAmount();
            }
            order.setOughtAmount(order.getOughtAmount()-discountAmount);
            logger.info("calcPromition扣除促销:"+discountAmount+"后,应付金额："+order.getOughtAmount());
            OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION,-discountAmount);
            order.addOrderAmountItem(item);
        }
    }
    
    
    
    /**
    *计算自驾游儿童价
    * @param order
    * @date 2016-12-19 下午3:13:20
     */
    private void calcSelfDrivingChildPriceAmount(OrdOrderDTO order){
        BuyInfo info = order.getBuyInfo();
        try {
            Boolean destBuOrder = OrdOrderUtils.isDestBuFrontOrder(order);
            if(destBuOrder && info!=null && info.getSelfDrivingChildQuantity()>0){
                ProdProduct prodProduct =null;
                ResultHandleT<ProdProduct> bugProduct = productClientService.findProdProductByIdFromCache(info.getProductId());
                if (bugProduct.isFail() || bugProduct.getReturnContent() == null) {
                    return;
                }
                prodProduct=bugProduct.getReturnContent();
                long selfDrivingChildPrice=0L;
                long selfDrivingChildPriceAmount=0L;
                if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(prodProduct.getBizCategoryId())
                        && BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(prodProduct.getSubCategoryId())){
                    ResultHandleT<List<ProdProductSaleRe>> resultHandleT = prodProductSaleReClientService.queryByProductId(prodProduct.getProductId());
                    if(resultHandleT != null && resultHandleT.isSuccess()){
                        List<ProdProductSaleRe> prodProductSaleRes = resultHandleT.getReturnContent();
                        if(!CollectionUtils.isEmpty(prodProductSaleRes)){
                            if(ProdProductSaleRe.SALETYPE.PEOPLE.name().equals(prodProductSaleRes.get(0).getSaleType()) &&
                                ProdProductSaleRe.HOUSEDIFFTYPE.AMOUNT.name().equals(prodProductSaleRes.get(0).getChildPriceType()) && 
                                prodProductSaleRes.get(0).getChildPriceAmount()!=null && prodProductSaleRes.get(0).getChildPriceAmount()>0){
                                selfDrivingChildPriceAmount=prodProductSaleRes.get(0).getChildPriceAmount();
                            }
                        }
                    }
                }
                if(selfDrivingChildPriceAmount>0){
                    selfDrivingChildPrice=selfDrivingChildPriceAmount*info.getSelfDrivingChildQuantity();
                    if(selfDrivingChildPrice>0){
                        order.setOughtAmount(order.getOughtAmount()+selfDrivingChildPrice);
                        OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.SELFDRIVING_CHILDPRICE,OrderEnum.ORDER_AMOUNT_NAME.SELFDRIVING_CHILD,selfDrivingChildPrice);
                        order.addOrderAmountItem(item);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }   
    }
    
    private void calcSupplierPromotion(OrdOrderDTO order) {
        long promoSettleprice = countSupplierPromotionAmount(order);
        if(promoSettleprice>0){
            for(String key:order.getPromotionMap().keySet()){
                if(key.startsWith("supplier_")){
                    List<OrdPromotion> list = order.getPromotionMap().get(key);
                    OrdOrderItem target = (OrdOrderItem)list.get(0).getTarget();
                    OrderTimePriceService timePriceService = orderOrderFactory.createTimePrice(target);
                    timePriceService.calcSettlementPromotion(target, list);
                }
            }
            OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_SETTLEPRICE, OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION, -promoSettleprice);
            order.addOrderAmountItem(item);
        }
    }

    /**
     * 计算订单奖金抵扣相关
     */
    private void calcBonus(OrdOrderDTO order){
        BuyInfo info = order.getBuyInfo();
        if(info!=null){
            String youhuiType = info.getYouhui();
            if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
                Float bonusYuan = info.getBonusYuan();
                if(bonusYuan!=null){
                    order.setBonusAmount(PriceUtil.convertToFen(bonusYuan));
                }
            }
        }

    }


	/**
	 * 计算订单金额
	 * @param order
	 */
	private void calcOrderAmount(OrdOrderDTO order){
		long totalAmount=0L;
		long totalSettlement=0L;
		long unvalidPromtionAmount=0L;//无效促销金额
		for(OrdOrderItem orderItem:order.getOrderItemList()){
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
			if(orderItem.getTotalSettlementPrice()==null){
				orderItem.setTotalSettlementPrice(OrderUtils.getLongByDefault(orderItem.getActualSettlementPrice())*orderItem.getQuantity());
			}
			if(orderItem.getTotalAmount()==null){
				orderItem.setTotalAmount(OrderUtils.getLongByDefault(orderItem.getPrice())*orderItem.getQuantity());

                Log.info("-------------------------------------------------------"+orderItem.getTotalAmount()+"orderItem.getPrice()"+orderItem.getPrice()+"orderItem.getQuantity()");
            }
            // 判断该商品是否是买断资源，是否有库存或者金额，如果是那么重新设置改子订单的买断数量buyoutQuantity和单价 buyoutPrice【原先价格不变】
            //reduceResPrecontrol( orderItem );
            if("Y".equals(orderItem.getBuyoutFlag())){
                long totalQuantity = orderItem.getQuantity();
                long preQuantity = orderItem.getBuyoutQuantity();
                if(totalQuantity>preQuantity){
                    Long notBuyoutTotalPrice = orderItem.getNotBuyoutSettleAmout();
                    notBuyoutTotalPrice = notBuyoutTotalPrice==null?0L:notBuyoutTotalPrice;
                    orderItem.setTotalSettlementPrice(orderItem.getBuyoutTotalPrice() + notBuyoutTotalPrice);
                    orderItem.setSettlementPrice((long)orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
                    orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
                }
            }

            logger.info("cal each category:"+orderItem.getCategoryId()+",suppgoodsId:"+orderItem.getSuppGoodsId()+"["+orderItem.hashCode()+"]\t"+orderItem.getSuppGoodsName()+"\ttotalamount="+orderItem.getTotalAmount());
            if(!PromtionUtil.validPromtionItem(orderItem)){//不参与促销
            	unvalidPromtionAmount+=orderItem.getTotalAmount();
                logger.info("cal unvalidPromtionAmount="+unvalidPromtionAmount);
            }

            //如果扣减买断资源成功，那么总订单  是按买断价格计算
            totalAmount+=orderItem.getTotalAmount();
            totalSettlement+=orderItem.getTotalSettlementPrice();
        }

        OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER,totalAmount);
        order.addOrderAmountItem(item);
        item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE, OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER, totalSettlement);
        order.addOrderAmountItem(item);
        //如果是券兑换的
        if("STAMP_PROD".equalsIgnoreCase(order.getOrderSubType())){
            //order.setOughtAmount();
            logger.info("----------------------------------------------应付金额"+order.getOughtAmount());
        }
        else
            order.setOughtAmount(totalAmount);
        order.setValidPromtionAmount(totalAmount-unvalidPromtionAmount);
    }

    private void reduceResPrecontrol(OrdOrderItem orderItem) {
        // OrdOrderItem orderItem =
        // ordOrderItemService.selectOrderItemByOrderItemId(ordOrderItemId);
        if (orderItem == null) {
            logger.info("不存在该子单");
            return;
        }
        if (orderItem.getBuyoutPrice() == null
                || orderItem.getBuyoutQuantity() == null) {
            logger.info("没有买断价");
            return;
        }
        SuppGoods goods = orderItem.getSuppGoods();
        Long goodsId = goods.getSuppGoodsId();
        Date visitDate = orderItem.getVisitTime();
        // 通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
        GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote
                .getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
        // 如果能找到该有效预控的资源
        if (goodsResPrecontrolPolicyVO != null
                && goodsResPrecontrolPolicyVO.isControl()) {
            String resType = goodsResPrecontrolPolicyVO.getControlType();
            // 购买该商品的数量
            Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
            Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();

            boolean reduceResult = false;
            if ("amount".equalsIgnoreCase(resType) && leftAmount != null
                    && leftAmount > 0) {
                reduceResult = true;
                if (reduceResult) {
                    logger.info("按金额预控");
                }
            } else if ("inventory".equalsIgnoreCase(resType)
                    && leftQuantity != null && leftQuantity > 0) {
                reduceResult = true;
                if (reduceResult) {
                    logger.info("按库存预控");
                }
            }
            // 初始化子单的时候，设置了买断价而且尚有买断资源
            if (reduceResult && orderItem.getBuyoutPrice() != null
                    && orderItem.getBuyoutQuantity() != null) {
                // 设置为使用买断价格
                orderItem.setBuyoutFlag("Y");
                // 如果是酒店的话，更改成使用买断价，那么要使用该买断价格，更新退单时应扣减的金额
                /*
                 * String
                 * key="timePrice_"+orderItem.getContentValueByKey(OrderEnum
                 * .ORDER_COMMON_TYPE.categoryCode.name());
                 * if("timePrice_category_hotel".equals(key) ||
                 * "timePrice_category_other_ticket".equals(key) ||
                 * "timePrice_category_single_ticket".equals(key)||
                 * "timePrice_category_comb_ticket".equals(key)){ Long
                 * deductAmount = orderItem.getDeductBuyoutAmout();
                 * if(deductAmount!=null && deductAmount > 0){
                 * orderItem.setDeductAmount(deductAmount); } }
                 */

                logger.info("扣减预控资源成功，订单号：" + orderItem.getOrderId() + "子订单号："
                        + orderItem.getOrderItemId() + ",商品id:"
                        + orderItem.getSuppGoodsId() + "，数量："
                        + orderItem.getBuyoutQuantity() + "；设置为可卖");
            }
        }

    }

    private OrdOrderAmountItem makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE type,OrderEnum.ORDER_AMOUNT_NAME name,long totalAmount) {
        OrdOrderAmountItem item = new OrdOrderAmountItem();
        item.setItemAmount(totalAmount);
        item.setOrderAmountType(type.name());
        item.setItemName(name.getCode());
        return item;
    }

    //计算订单相关的数据
    private void calcOrder(OrdOrderDTO order){
        calcMainItem(order);//计算哪个订单子项是主商品订单项
        calcOrderCategroy(order);//计算订单的品类
        calcOrderVisitTime(order);//计算各商品的游玩时间
        calcBlackList(order);
        calcResourceConfirm(order);
        calcBuCode(order);//处理订单以及子订单的buCode信息  && 设置主订单“公司主体” && 重新设置国内bu订单的分公司信息
        calcPaymentType(order);//依赖资源审核的状态做支付时间
        /*
         * create by lh begin
         */
        calcFlightAndHotelPaymentType(order);
        // 设置酒店套餐、酒景 且是砍价和小程序分销渠道设置走3.0工作流
        calcWorkVersion(order);
        calcWorkflow(order);
        calcWorkflow4Ord2(order);
        //计算国内工作流
        calcWorkFlowForLocalBu(order);
        //计算途牛跟团
        calWorkFlowForTuniu(order);
        calcOrderAmount(order);//计算订单金额
        calcSelfDrivingChildPriceAmount(order);//自驾游儿童价     
        calcRebate(order);//订单返现计算
        calcBonus(order);//订单抵扣相关
        calcPromition(order);//计算促销活动相关的东西
        if(order.getBuyInfo().getPromotionNewMap().isEmpty()){
        	calcSupplierPromotion(order);
        }
        calcOrderGuaranteeType(order);
        calcDepositsAmount(order);
        calcDistribution(order);
        calcManagerId(order);//处理产品经理信息
        calOrderPesornList(order);//计算订单是否需要填充紧急联系人
        calOrderLastConfirmTime(order);//计算订单最晚确认时间
        calcOrderItemAheadTime(order);//设置子单的提前预订时间
        calcOrderLocalWaitPayTime(order);//设置支付国内订单机酒等待时间为30分钟
        calcO2OTicketAndHotelSetTicketInfo(order);//设置国内o2o景加酒景点门票信息自动过
    }

    private void calcWorkVersion(OrdOrderDTO order) {
		// 酒店套餐或者景酒  且  砍价或小程序 设置工作流版本为3.0
    	if ((BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId()) ||
    			(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId()) && 
    					BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId()))
    			) && (Constant.MINI_PROGRAM == order.getDistributorId() || Constant.BARGAIN == order.getDistributorId())) {
    		order.setWorkVersion(ConfirmUtils.WORK_VERSION_NEW);
    	}
	}

	//途牛跟团新工作流 供应商打包
    private void calWorkFlowForTuniu(OrdOrderDTO order) {
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())){
            //通关sweet获取途牛供应商id
            String tuniuSupplierId=ZooKeeperConfigProperties.getProperties("tuniu.supplierId");
            List<OrdOrderItem> orderItemList = order.getOrderItemList();
            if(orderItemList!=null && orderItemList.size()>0){
                boolean isTuniuGroup=false;
                for(OrdOrderItem ordOrderItem:orderItemList) {
                    if (StringUtils.isNotEmpty(tuniuSupplierId) && (tuniuSupplierId.equals(ordOrderItem.getSupplierId()+""))) {
                        logger.info("calWorkFlowForTuniu>>途牛跟团供应商下单>>orderId:" + order.getOrderId());
                        isTuniuGroup = true;
                        ordOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "grouptour_tuniu");
                    }
                }
                if(isTuniuGroup){
                    //只有途牛跟团的订单 有保险设置子流程processKey
                    for(OrdOrderItem ordOrderItem:orderItemList) {
                        if(BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(ordOrderItem.getCategoryId())){
                            logger.info("calWorkFlowForTuniu>>途牛跟团供应商下单>>包含保险>>orderId:" + order.getOrderId());
                            ordOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(),"insurance");
                        }
                    }
                    order.setProcessKey("grouptour_tuniu_main");
                    order.setNewWorkflowFlag("Y");
                }
            }
        }
    }

    //设置国内o2o景加酒景点门票信息自动过
    private void calcO2OTicketAndHotelSetTicketInfo(OrdOrderDTO order){
    	if(ConfirmUtils.WORK_VERSION_NEW_O2O.equals(order.getWorkVersion())){
    		for (OrdOrderItem item : order.getOrderItemList()) {
				if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(item.getCategoryId())
						||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(item.getCategoryId())
						||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(item.getCategoryId())){
					item.setInfoStatus(OrderEnum.INFO_STATUS.INFOPASS.name());
				}
			}
    	}
    }
    
	//设置国内工作流
	private void calcWorkFlowForLocalBu(OrdOrderDTO order){
		if(BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			if(order.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()
					&&order.getSubCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()){
				order.setProcessKey("local_order_bus_hotel_prepaid_main");
				order.setWorkVersion(null);
				for (OrdOrderItem item : order.getOrderItemList()) {
					if(item.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().longValue()){
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "bus");
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()) {// 酒店子单
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "hotel");
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == item.getCategoryId()) {// 酒店套餐子单
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "route");
					}
				}
				return;
			}
		}	
		if(BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			if(order.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()
					&&order.getSubCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()){
				order.setProcessKey("local_order_bus_hotel_prepaid_main");
				order.setWorkVersion(null);
				for (OrdOrderItem item : order.getOrderItemList()) {
					if(item.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().longValue()){
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "bus");
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == item.getCategoryId()) {// 酒店子单
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "hotel");
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == item.getCategoryId()
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == item.getCategoryId()) {// 酒店套餐子单
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "route");
					}
				}
				return;
			}
			//区分来源渠道
			if(OrdOrderUtils.isLocalBuFrontOrder(order)){//区分来源渠道
				//to do
				order.setProcessKey("local_order_prepaid_main");
				order.setWorkVersion("2.5");
				for (OrdOrderItem item : order.getOrderItemList()) {
					if(item.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()){
						item.setContent(item.getContent());
						item.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "local_flight");
					}
				}
			}
		}
	}
		
    //设置支付国内订单机酒等待时间为30分钟
    private void calcOrderLocalWaitPayTime(OrdOrderDTO order){
        if(BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
            //区分品类
            if((BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
                    &&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
                    ||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue()){//跟团游对接
                if(OrdOrderUtils.isLocalBuFrontOrder(order)){//区分来源渠道
                    order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 60));
                    order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.name());
                }
            }
        }
        //设置巴士+酒支付等待时间
        if(OrdOrderUtils.isBusHotelOrder(order)){
        	if(order.getWaitPaymentTime()==null){
        		order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 60));
                order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.name());
        	}
        }
    }
        



    /***

         * 计算订单是否需要填充紧急联系人
         * @param order
         */
        private void calOrderPesornList(OrdOrderDTO order) {
            List <OrdPerson> listOrdPerson=order.getOrdPersonList();
            if (CollectionUtils.isNotEmpty(listOrdPerson)) {
                // TODO Auto-generated method stub
                boolean havaEmergencyContact=false;
                for (OrdPerson person : order.getOrdPersonList()) {
                     if(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equalsIgnoreCase(person.getPersonType())){
                         havaEmergencyContact=true;
                    }
                }
                if (order.getDistributorId()!=null && order.getDistributorId() !=2L) {
                    if (order.getCategoryId()!=null&&(15L==order.getCategoryId() || 16L==order.getCategoryId() || 17L==order.getCategoryId() || 18L==order.getCategoryId() || 8L == order.getCategoryId())) {
                        if(!havaEmergencyContact){
                            OrdPerson emergencyContact =new OrdPerson();
                            emergencyContact.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
                            emergencyContact.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
                            listOrdPerson.add(emergencyContact);
                        }
                    }
                }
                order.setOrdPersonList(listOrdPerson);
            }

    }

        /***
         * 处理订单以及子订单的buCode信息   && 设置主订单“公司主体” && 如果订单BU是国内BU需重新设置订单的所属公司信息:设置为 产品的分公司
         * @param order
         */
    private void calcBuCode(OrdOrderDTO order){
        try {

            List<OrdOrderPack> orderPackList = order.getOrderPackList();
            String buCode = null;
            //归属地
            Long attributionId=null;

            // 公司主体.是否已处理
            boolean companyTypeFlag = false;
            // 公司主体
            String companyType = null;
            ProdProduct product =null;
            ProdProduct mainProduct = null;

            // 公司主体：1次处理
            if (order.getBuyInfo()!=null && order.getBuyInfo().getProductId()!=null) {
                //ResultHandleT<ProdProduct> bugProduct = productClientService.findProdProductById(order.getBuyInfo().getProductId());
                ResultHandleT<ProdProduct> bugProduct = productClientService.findProdProductByIdFromCache(order.getBuyInfo().getProductId());
                if (bugProduct.isSuccess()&& bugProduct.getReturnContent() != null) {
                    companyTypeFlag = true;
                    companyType = bugProduct.getReturnContent().getCompanyType();
                    product =bugProduct.getReturnContent();
                    mainProduct = bugProduct.getReturnContent();
                    
                    setOrderStartDistrictId(order, product);
                }
            }

            boolean flag=true; //标记buCode的取值来源  若为false则说明取自主商品，此时订单子项的其他项取商品本身所属BU
            //1.根据打包记录来获得所属BU
            if(CollectionUtils.isNotEmpty(orderPackList)){
                OrdOrderPack ordOrderPack = orderPackList.get(0);
                //ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductById(ordOrderPack.getProductId());
                ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductByIdFromCache(ordOrderPack.getProductId());
                if (resultProduct.isSuccess()
                        && resultProduct.getReturnContent() != null
                        && StringUtils.isNotBlank(resultProduct.getReturnContent().getBu())
                        && ProdProduct.PACKAGETYPE.LVMAMA.name()
                                .equalsIgnoreCase(
                                        resultProduct.getReturnContent()
                                                .getPackageType())) {
                    buCode = resultProduct.getReturnContent().getBu();
                    attributionId=resultProduct.getReturnContent().getAttributionId();
                    product =resultProduct.getReturnContent();
                    setOrderStartDistrictId(order, product);
                }

                // 公司主体：2次处理
                if (!companyTypeFlag && resultProduct.isSuccess()&& resultProduct.getReturnContent() != null) {
                    companyTypeFlag = true;
                    companyType = resultProduct.getReturnContent().getCompanyType();
                }

            }
            DestBuOrderPropUtil.setSubCategoryIdToOrder(order, product);

            //2.根据主订单项来获得所属BU
            if(StringUtils.isBlank(buCode) || !companyTypeFlag){
                OrdOrderItem mainOrderItem = null;
                List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
                if(CollectionUtils.isNotEmpty(ordOrderItemList)){//获取商品的主商品
                    for(OrdOrderItem item : ordOrderItemList){
                        if("true".equalsIgnoreCase(item.getMainItem())){
                            mainOrderItem = item;
                            break;
                        }
                    }
                }
                if(mainOrderItem != null){//根据主商品查询所属BU
                    logger.info("tell me mainOrderItem'suppGoodsId() when mainItem is not null,suppGoods:------:"+mainOrderItem.getSuppGoodsId());
                    SuppGoods suppGoods = mainOrderItem.getSuppGoods();
                    logger.info("tell me suppGoods messgae when mainItem is not null,suppGoods:------:"+suppGoods);

                    if (StringUtils.isBlank(buCode)) {
                        if(suppGoods!=null){
                            logger.info("tell me suppGoods messgae when mainItem is not null,suppGoods:------:"+suppGoods.getBu());
                            buCode = suppGoods.getBu();
                            if(attributionId==null)
                            {
                                attributionId=suppGoods.getAttributionId();
                            }
                            flag=false;
                        }
                    }

                    // 公司主体：3次处理
                    if (!companyTypeFlag) {
                        ProdProduct prodProduct = suppGoods.getProdProduct();
                        if (prodProduct != null) {
                            companyTypeFlag = true;
                            companyType = prodProduct.getCompanyType();
                        }
                    }
                }
            }

            // 3.给order赋值“公司主体”
            if (StringUtils.isNotBlank(companyType)) {
                order.setCompanyType(companyType);
            } else {
                logger.info("no companyType to this order:{}", order.getOrderId());
            }

            //3.给order  以及orderpack 和orderItem赋值
            logger.info("calcBuCode  and tell me buCode from product or mainItem "
                    + "( flag is ture from product else from mainItem) result:------:"+buCode+","+flag
                    );
            if(StringUtils.isNotBlank(buCode)){
                order.setBuCode(buCode);//主订单赋值buCode
                order.setAttributionId(attributionId);
                DestBuOrderPropUtil.setCancelStrategyToOrder(order, product, productClientService);
                //订单快照设置产品信息
                DestBuOrderPropUtil.setProductInfoToOrder(order, product);
                orderSnapshot(order, product);

                List<Long> list= new ArrayList<Long>();
                if (flag) {//走此处说明buCode取自产品，则其他商品的buCode使用产品的BUCODE
                    /******获取order。buyInfo中的打包产品的product的中的ITEM BEGIN***/
                    if(CollectionUtils.isNotEmpty(order.getBuyInfo().getProductList()))
                    {
                        if(CollectionUtils.isNotEmpty(order.getBuyInfo().getProductList().get(0).getItemList()))
                        {
                            for(Item item: order.getBuyInfo().getProductList().get(0).getItemList())
                            {
                                list.add(item.getGoodsId());
                            }
                        }
                    }
                    /******获取order。buyInfo中的打包产品的product的中的ITEM  END***/
                    if(CollectionUtils.isNotEmpty(order.getOrderItemList()))
                    {
                        boolean isOrNot=false;
                        logger.info("-------------------order.getOrderItemList().SIZE():"+order.getOrderItemList().size());
                        logger.info("-------------------list.SIZE():"+list.size());
                        for(OrdOrderItem item :  order.getOrderItemList()){
                            isOrNot=list.contains(item.getSuppGoodsId());//判断该商品是否属于产品中的商品
                            if(isOrNot)
                            {//如果是则存放产品的BU
                                item.setBuCode(buCode);
                                isOrNot=false;
                            }
                            //@主单所属BU为国内BU,子单为对接机票则机票子单的规属地要设为主单所在的规属地
                            if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
                                if(OrderLvfTimePriceServiceImpl.isLvfItemByCatetory(item)){
                                    item.setAttributionId(order.getAttributionId());
                                }
                            }
                        }
                    }
                }

                if(CollectionUtils.isNotEmpty(order.getOrderPackList()))
                {
                for(OrdOrderPack ordOrderPack : order.getOrderPackList()){
                    ordOrderPack.setBuCode(buCode);
                }
                }
            }else{
                logger.info("no buCode to this order");
            }
            //如果订单BU是国内BU/出境BU并且是自主打包需重新设置订单的所属公司信息:设置为 产品的分公司
            if(mainProduct != null && order != null){
                if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
                        || BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())){
                    if(PACKAGETYPE.LVMAMA.getCode().equals(mainProduct.getPackageType())){
                        if(null != mainProduct.getFiliale()){
                            order.setFilialeName(mainProduct.getFiliale());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("{}", e);
            logger.error("no buCode to this order and calcBuCode has Exception");
        }

    }

    /**
     * 订单快照
     * @param order
     * @param mainProdProduct
     */
    private void orderSnapshot(OrdOrderDTO order, ProdProduct mainProdProduct) {
        if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
            // 仅保存财务结算code快照
            for (OrdOrderItem item : order.getOrderItemList()) {
                item.putContent(SUPPGOODS_KEY.settlementCode.name(), item.getSuppGoods().getSettlementEntityCode());
                item.putContent(SUPPGOODS_KEY.buyoutSettlementCode.name(), item.getSuppGoods().getBuyoutSettlementEntityCode());
            }
        }
        if(mainProdProduct ==null || CollectionUtils.isEmpty(order.getOrderItemList())) return ;
        if(mainProdProduct.getBu() ==null) {
            mainProdProduct.setBu(order.getBuCode());
        }
        if(CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(mainProdProduct.getBu())
        		|| CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(mainProdProduct.getBu()) ){
	        //init
	        //OrdSnapshotUtils.setSnapshot(mainProdProduct);
	        //if(!OrdSnapshotUtils.isSnapshot()) return;
	        //主单
            vstSnapshotService.orderSnapshot_prodProduct(order, mainProdProduct);
	        //子单
	        for(OrdOrderItem item :  order.getOrderItemList()){
                //产品
                vstSnapshotService.orderSnapshot_prodProduct(item, item.getSuppGoods().getProdProduct());
                //规格
                vstSnapshotService.orderSnapshot_prodProductBranch(item, item.getSuppGoods().getProdProductBranch());
                //商品
                vstSnapshotService.orderSnapshot_suppSuppGoods(item, item.getSuppGoods());
	        }
        }
    }

    private void setOrderStartDistrictId(OrdOrderDTO order, ProdProduct product) {
        if(product!=null
            &&null!=product.getMuiltDpartureFlag()
            &&"N".equalsIgnoreCase(product.getMuiltDpartureFlag())
            &&!"Y".equalsIgnoreCase(product.getIsMuiltDeparture())){
            logger.info("product id"+product.getProductId()+",enter set MuiltDparture");
            if(null!=product.getBizDistrictId()){
                logger.info("product id"+product.getProductId()+",set MuiltDparture:"+product.getBizDistrictId());
                order.setStartDistrictId(product.getBizDistrictId());
            }
        }
    }

    private SuppGoods getSuppGoods(Long distributorId,Long goodsId) throws Exception {
        ResultHandleT<SuppGoods> goodsResultHandleT = distGoodsClientService.findSuppGoodsById(distributorId, goodsId);
        if (goodsResultHandleT.isFail() || goodsResultHandleT.getReturnContent() == null) {
            return null;
        }
        SuppGoods suppGoods = goodsResultHandleT.getReturnContent();
        return suppGoods;
    }

    /**
     * @param order
     * 根据规则计算产品经理ID
     */
    private void calcManagerId(OrdOrderDTO order){
        List<OrdOrderPack> orderPackList = order.getOrderPackList();
        Long managerId = null;
        //1.根据打包记录来获得
        if(CollectionUtils.isNotEmpty(orderPackList)){
            OrdOrderPack ordOrderPack = orderPackList.get(0);
            //交通+X替换逻辑, 29品类2018年1月后切换为超级自由行，不需要这个逻辑。
            /*
            if(order.getCategoryId()!=null && BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue()){
                //有酒店商品使用酒店上的行政区域，查找对应产品经理
                List<OrdOrderItem> orderItemList=order.getOrderItemList();
                if(CollectionUtils.isNotEmpty(orderItemList)){
                    for (OrdOrderItem ordOrderItem : orderItemList) {
                        if(ordOrderItem.getCategoryId()!=null && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==ordOrderItem.getCategoryId().longValue()){
                            managerId=attributionManagerMap.get(ordOrderItem.getSuppGoods().getAttributionId());
                            if(managerId!=null){
                                break;
                            }
                        }
                    }
                }
            }
            */
            if(managerId==null){
                //ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductById(ordOrderPack.getProductId());
                ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductByIdFromCache(ordOrderPack.getProductId());
                if(resultProduct.isSuccess() && resultProduct.getReturnContent() != null && resultProduct.getReturnContent().getManagerId()!=null){
                        if(ProdProduct.PACKAGETYPE.LVMAMA.name().equalsIgnoreCase(resultProduct.getReturnContent().getPackageType())){
                            managerId = resultProduct.getReturnContent().getManagerId();
                        }
                        if(resultProduct.getReturnContent().getBizCategoryId() != null && resultProduct.getReturnContent().getBizCategoryId().longValue() == 8L){
                            managerId = resultProduct.getReturnContent().getManagerId();
                        }
                }
            }
        }
        //2.根据主订单项来获得
        if(managerId == null){
            managerId=findManagerIdByMainOrderItem(order);
        }

        //3.设置值.
        if(managerId != null){
            order.setManagerId(managerId);
        }else{
            logger.error("no manager_id");
        }
    }
    
    /**
     * 根据主订单项来获得
     * @param order
     * @return
     */
    private Long findManagerIdByMainOrderItem(OrdOrderDTO order){
        Long managerId=null;
        OrdOrderItem mainOrderItem = null;
        List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
        if(CollectionUtils.isNotEmpty(ordOrderItemList)){
            for(OrdOrderItem item : ordOrderItemList){
                if("true".equalsIgnoreCase(item.getMainItem())){
                    mainOrderItem = item;
                    break;
                }
            }
        }
        if(mainOrderItem != null){
            managerId = mainOrderItem.getManagerId();
        }
        return managerId;
    }

    private void calcDistribution(OrdOrderDTO order){
        if(order.getDistributorId().equals(4L)){
            if (order.getBuyInfo().getDiscountAmount() != null
                    && order.getBuyInfo().getOughtAmount() != null) {
                long amount = order.getBuyInfo().getOughtAmount()+order.getBuyInfo().getDiscountAmount();
                if(amount!=order.getOughtAmount()){
                    throwIllegalException("分销价格无法与订单实际金额保持一致");
                }
                order.setOughtAmount(order.getBuyInfo().getOughtAmount());
                OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE, OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION, order.getBuyInfo().getDiscountAmount());
                order.addOrderAmountItem(item);
            }
        }
    }

    /**
     * 重新检查并重设定金
     * @param order
     */
    void calcDepositsAmount(OrdOrderDTO order){
        if(order.getDepositsAmount()!=null&&order.getDepositsAmount()>0L){
            if(order.getDepositsAmount()>order.getOughtAmount()){
                order.setDepositsAmount(order.getOughtAmount());
            }
        }
    }
    public void calcOrderGuaranteeType(OrdOrderDTO order){
        if(!order.hasNeedPay()){
            return;
        }
        guaranteeBussiness.calcOrderGuaranteeType(order);
        String error = guaranteeBussiness.initGuaranteeCreditCard(order);
        if(StringUtils.isNotEmpty(error)){
            throwIllegalException(error);
        }
    }

    /**
     *  订单返现计算
     */
    private void calcRebate(OrdOrderDTO order){
        if(DefaultRebateConfig.getInstance().isNewRebate()){
            calcRebateBaseNew(order);
        }else{
            calcRebateBaseOld(order);
        }

    }
    @Autowired
    private OrderRebateBussiness orderRebateBussiness;
    private void calcRebateBaseNew(OrdOrderDTO order){
        orderRebateBussiness.calcRebate(order);
    }

    private void calcRebateBaseOld(OrdOrderDTO order){
        try {
            long totalRebateAmount=0L;
            Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
            Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
            String channel="";
            if(distributorId==2||distributorId==3){
                channel="pc";
            }
            if(distributorId==4){
                if(channelId==10000||channelId==10001||channelId==10002){
                    channel="mobile";
                }
                if(channelId==107||channelId==108||channelId==110||channelId==103){
                    channel="pc";
                }
            }
            if(channel.equals("")){
                return;
            }
            for(OrdOrderItem orderItem:order.getOrderItemList()){

                SuppGoodsRebate rebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(orderItem.getSuppGoodsId(), channel).getReturnContent();
                if(distributorId==2){
                    if(rebate==null){
                        logger.info("无数据不返现");
                        setRebateAmountZero(order);
                        return;
                    }
                    if(StringUtils.isEmpty(rebate.getIsBackRebate())){
                        logger.info("后台默认不返现");
                        setRebateAmountZero(order);
                        return;
                    }
                    if("N".equalsIgnoreCase(rebate.getIsBackRebate())){
                        logger.info("后台设置不返现");
                        setRebateAmountZero(order);
                        return;
                    }
                }
                if(rebate!=null){
                    //pc端下单
                    if(channel.equals(OrderEnum.ORDER_CHANNEL.pc.getCode())){
                        totalRebateAmount+=pcGoodsRebate(orderItem, rebate);
                    }
                     //手机端下单
                    if(channel.equals(OrderEnum.ORDER_CHANNEL.mobile.getCode())){
                        //固定金额返现
                        if(rebate.getRebateType().equals(REBATE_TYPE.fixed.getCode())){
                            totalRebateAmount+=(rebate.getFixedAmount()*orderItem.getQuantity());
                        }else{
                            SuppGoodsRebate pcRebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(orderItem.getSuppGoodsId(), ORDER_CHANNEL.pc.getCode()).getReturnContent();
                            long pcRebateAmount = pcGoodsRebate(orderItem, pcRebate);

                            //全局倍率返现
                            if(rebate.getRebateType().equals(REBATE_TYPE.global.getCode())){
                                float globalRebate = suppGoodsRebateClientService.getGlobalRateRebate();
                                totalRebateAmount+= pcRebateAmount*globalRebate;
                            }
                            if(rebate.getRebateType().equals(REBATE_TYPE.more.getCode())){
                                long moreAmount = rebate.getMoreAmount();
                                totalRebateAmount+=pcRebateAmount+(moreAmount*orderItem.getQuantity());
                            }
                            if(rebate.getRebateType().equals(REBATE_TYPE.multiplyingPower.getCode())){
                                totalRebateAmount+=pcRebateAmount*rebate.getMultiplyingPowerAmount();
                            }
                        }

                    }
                }
            }
            totalRebateAmount =new BigDecimal(Math.ceil( PriceUtil.convertToYuan(totalRebateAmount))*100).longValue();
            //保存订单返现信息
            //修改点评返现金额大于100元的情况，默认为100元，rebateAmount保存到分
            if (totalRebateAmount > 10000) {
                totalRebateAmount = 10000;
            }
            order.setRebateAmount(totalRebateAmount);
            order.setRebateFlag("N");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void setRebateAmountZero(OrdOrderDTO order){
        order.setRebateAmount(0l);
        order.setRebateFlag("N");
    }



    public long pcGoodsRebate(OrdOrderItem orderItem,SuppGoodsRebate rebate){
        if(rebate!=null){
            //是否到付门票
            boolean payTicketFlag = suppGoodsClientService.checkPayTicket(orderItem.getSuppGoodsId());
            //固定金额返现
            if(REBATE_TYPE.fixed.getCode().equals(rebate.getRebateType())){
                long fixedAmount = rebate.getFixedAmount()==null?0:rebate.getFixedAmount();
                if(payTicketFlag){
                    //到付门票
                    return fixedAmount;
                }else{
                    //非到付门票固定金额返现返现金额*商品数量
                    return (fixedAmount*orderItem.getQuantity());
                }
            }else if(rebate.getRebateType().equals(REBATE_TYPE.rate.getCode())){
                //房差售价
                long priceSpread=0;
                //房差结算价
                long settlementSpread=0;
                long spreadQuantity = 0;
                //单个商品返现=毛利*折扣比例
                if(orderItem.getOrdMulPriceRateList()!=null){
                    for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
                        if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
                            priceSpread=rate.getPrice();
                            spreadQuantity = rate.getQuantity();
                        }
                        if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
                            settlementSpread=rate.getPrice();
                        }
                    }
                }
                //房差毛利
                long spreadProfit= (priceSpread-settlementSpread)*spreadQuantity;
                //返现毛利=总毛利-房差毛利
                long profit = orderItem.getTotalAmount()-orderItem.getTotalSettlementPrice()-spreadProfit;
                if(profit<0){
                    profit=0;
                }
                Long rebateFen =new BigDecimal((profit*(rebate.getRateAmount().floatValue()/100))).setScale(0,BigDecimal.ROUND_HALF_UP).longValue();
                long totalRebate = (rebateFen+99)/100*100;
                return totalRebate;
            }
        }
        return 0;
    }

    private void calcMainItem(OrdOrderDTO order){
        //关联销售判断主子订单
        boolean mainItemByCategory = order.getBuyInfo()!=null && "Y".equalsIgnoreCase(order.getBuyInfo().getHasMainItemFlag());
        if(order.getOrderItemList().size()==1){
            order.getOrderItemList().get(0).setMainItem("true");
            mainItemByCategory = true;
        }
        if(!mainItemByCategory){
            Queue<OrdOrderItem> queue = new PriorityQueue<OrdOrderItem>(order.getOrderItemList().size(), categoryComparator);
            if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
                for(OrdOrderPack pack:order.getOrderPackList()){
                    if(!pack.hasOwn()){
                        queue.addAll(pack.getOrderItemList());
                    }
                }

            }
            if(queue.isEmpty()){
                queue.addAll(order.getOrderItemList());
            }

            if(!queue.isEmpty()){
                OrdOrderItem orderItem = queue.poll();
                orderItem.setMainItem("true");
            }
        }

        setMainOrderItem(order);
        order.setFilialeName(order.getFilterMainOrderItem().getSuppGoods().getFiliale());

        //由于无线端供应商打包商品的成人数儿童数只有传到了itemlist中，所以需要在计算好主商品之后再次赋值成人数儿童数。
        //shanping
        resetAdultInfoAndChildInfo(order);
    }
    /**
     * 从主子单中获取成人数儿童数重新赋值（核查主子单）
     */
    private OrdOrderItem checkMainItemForResetAdultInfoAndChildInfo(OrdOrderDTO order,OrdOrderItem mainOrderItem){
        //寻找主子单，在无线安卓调用时，之前主子单计算不正确，这里临时反查处理
        //线路产品排序后 15 16 18品类会定位主子单，此时需要进一步核实主子单是否正确
        if(mainOrderItem.getCategoryId()!=null &&
                ( mainOrderItem.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId()
                        || mainOrderItem.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId()
                        || mainOrderItem.getCategoryId().longValue()==BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){
            Long productBranchId = mainOrderItem.getBranchId();
            ResultHandleT<String> handle = prodProductBranchClientService.findBranchCodeByProductBranchId(productBranchId);
            String code = handle.getReturnContent();
            if(StringUtils.isBlank(code)){
                logger.error("findBranchCodeByProductBranchId error,productBranchId:"+productBranchId+"  code:"+code);
                logger.error("findBranchCodeByProductBranchId error:"+handle.getMsg());
            }else if(RouteProductVo.BRANCHCODE.adult_child_diff.getCode().equalsIgnoreCase(code)){
                //主子单正确
                return mainOrderItem;
            }
            for(OrdOrderItem orderItem:order.getOrderItemList()){
                productBranchId = orderItem.getBranchId();
                code = prodProductBranchClientService.findBranchCodeByProductBranchId(productBranchId).getReturnContent();

                if(StringUtils.isBlank(code)){
                    logger.error("findBranchCodeByProductBranchId error,productBranchId:"+productBranchId+"  code:"+code);
                    logger.error("findBranchCodeByProductBranchId error:"+handle.getMsg());
                }else if(RouteProductVo.BRANCHCODE.adult_child_diff.getCode().equalsIgnoreCase(code)){
                    //找到主子单
                    return orderItem;
                }
            }
            //都没找到,返回原始的
            return mainOrderItem;
        }else{
            //非线路，不检查修正
            return mainOrderItem;
        }
    }

    /**
     * 从主子单中获取成人数儿童数重新赋值
     * @param order
     */
    private void resetAdultInfoAndChildInfo(OrdOrderDTO order) {
        if(order.getFormInfoList()!=null){
            OrdFormInfo childInfo = findOrdFormInfoByOrder(order,OrderEnum.OrdFormInfoContentTypeEnum.CHILD_AMOUNT.getContentType());
            OrdFormInfo adultInfo = findOrdFormInfoByOrder(order,OrderEnum.OrdFormInfoContentTypeEnum.ADULT_AMOUNT.getContentType());
            //只有成人数儿童数同时为0 或者不存在，此时需要重新从主子单中赋值成人数儿童数
            if( (adultInfo==null || "0".equals(adultInfo.getContent()) && (childInfo==null || "0".equals(childInfo.getContent())))){
                OrdOrderItem mainOrderItem = null;
                for(OrdOrderItem orderItem:order.getOrderItemList()){
                    if(orderItem.hasMainItem()){
                        mainOrderItem = orderItem;
                        break;
                    }
                }
                long beginTime = System.currentTimeMillis();
                mainOrderItem=checkMainItemForResetAdultInfoAndChildInfo(order,mainOrderItem);
                long endTime = System.currentTimeMillis();
                logger.info(" checkMainItemForResetAdultInfoAndChildInfo use: "+(endTime-beginTime));
                if(adultInfo==null){
                    OrdFormInfo info = new OrdFormInfo(OrderEnum.OrdFormInfoContentTypeEnum.ADULT_AMOUNT.getContentType(), String.valueOf(mainOrderItem.getAdultQuantity()));
                    order.getFormInfoList().add(info);
                }else{
                    adultInfo.setContent(String.valueOf(mainOrderItem.getAdultQuantity()));
                }

                if(childInfo==null){
                    OrdFormInfo info = new OrdFormInfo(OrderEnum.OrdFormInfoContentTypeEnum.CHILD_AMOUNT.getContentType(), String.valueOf(mainOrderItem.getChildQuantity()));
                    order.getFormInfoList().add(info);
                }else{
                    childInfo.setContent(String.valueOf(mainOrderItem.getChildQuantity()));
                }
            }
        }
    }

    private OrdFormInfo findOrdFormInfoByOrder(OrdOrderDTO order,String key){
        if(order==null || key==null || order.getFormInfoList()==null){
            return null;
        }
        List<OrdFormInfo> list = order.getFormInfoList();
        for (OrdFormInfo ordFormInfo : list) {
            if(ordFormInfo!=null && key.equalsIgnoreCase(ordFormInfo.getContentType())){
                return ordFormInfo;
            }
        }
        return null;
    }

    private void calcOrderCategroy(OrdOrderDTO order){
        //如果order中设置了品类是交通+X，则order的品类为 交通+X
        if(order!=null && order.getCategoryId()!=null && BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue()){
            return;
        }
        if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
            order.setCategoryId(order.getOrderPackList().get(0).getCategoryId());
        }
        if(order.getCategoryId()==null&&order.getFilterMainOrderItem()!=null){
            order.setCategoryId(order.getFilterMainOrderItem().getCategoryId());
        }

    }

    private void calcOrderVisitTime(OrdOrderDTO order){
        int size = order.getOrderItemList().size();
        Date visitTime = order.getOrderItemList().get(0).getVisitTime();
        Date lastCancelTime = order.getOrderItemList().get(0).getLastCancelTime();
        // 获取第一个有效时间（PS：为了排除快递非单卖的情况）
        if (size > 1
                && order.getOrderItemList().get(0).getCategoryId().longValue() == BizEnum.BIZ_CATEGORY_TYPE.category_other
                        .getCategoryId().longValue()) {
            for(int i=1;i<size;i++){
                OrdOrderItem orderItem = order.getOrderItemList().get(i);
                if(orderItem.getCategoryId().longValue() != BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().longValue()){
                    visitTime = order.getOrderItemList().get(i).getVisitTime();
                    lastCancelTime = order.getOrderItemList().get(i).getLastCancelTime();
                    break;
                }

            }
        }
        for(int i=1;i<size;i++){
            OrdOrderItem orderItem = order.getOrderItemList().get(i);
            logger.info("Now check visit time for item, item size is " + order.getOrderItemList().size() + ", visitTime is " + visitTime);
            // PS：为了排除快递非单卖的情况
            if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
                continue;
            }
            if(orderItem.getVisitTime().before(visitTime)){
                visitTime = orderItem.getVisitTime();
            }
            if(orderItem.getLastCancelTime()!=null){
                if(lastCancelTime==null){
                    lastCancelTime = orderItem.getLastCancelTime();
                }else if(lastCancelTime.after(orderItem.getLastCancelTime())){
                    lastCancelTime = orderItem.getLastCancelTime();
                }
            }
        }
        order.setLastCancelTime(lastCancelTime);
        order.setVisitTime(visitTime);
    }

    private void setMainOrderItem(OrdOrderDTO order){
        for(OrdOrderItem orderItem:order.getOrderItemList()){
            if(orderItem.hasMainItem()){
                order.setFilterMainOrderItem(orderItem);
            }
        }
    }

    private void saveOrder(OrdOrderDTO order){
        List<HotelOrderUpdateStockDTO> ordUpdateStockList = null;
        List<HotelOrderUpdateStockDTO> asynchronousOrdUpdateStockList = new ArrayList<>();  //异步扣减库存补偿list
        try
        {
            logger.info("start save order");
            //将订单保存分为两个步骤，第一步初始为取消状态，并保存相关信息；第二步扣减库存，并恢复订单为正常状态
            orderSaveService.saveOrder(order);
            
            lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.STORAGE_ORDER.name(), order.getOrderId(), LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "订单收单持久化成功", "订单收单持久化成功");
            
            //扣除库存，恢复订单为正常状态，走事务
            ordUpdateStockList = orderSaveServiceImpl.saveOrder(order,asynchronousOrdUpdateStockList);
            logger.info("end save order, orderId:" + order.getOrderId()+",wait");
            
            lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.STOCK_DEDUCT.name(), order.getOrderId(), LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "订单库存扣减成功", "订单库存扣减成功");
            
            Long categoryId=getOrderCategoryId(order);
            ordUserOrderService.insertOrdUserOrder(order.getCreateTime(), order.getOrderId(), String.valueOf(categoryId), order.getUserNo());
        }catch(Exception e){
            //回滚酒店
            if(CollectionUtils.isNotEmpty(ordUpdateStockList)){
                hotelTradeApiService.revertStockForDeductFail(ordUpdateStockList);
            }
            if(CollectionUtils.isNotEmpty(asynchronousOrdUpdateStockList)){
                hotelTradeApiService.revertStockForDeductFail(asynchronousOrdUpdateStockList);
            }
            throw e;
        }
    }
    

    private Long getOrderCategoryId(OrdOrderDTO order){
        //如果order中设置了品类是交通+X，则order的品类为 交通+X
        if(order!=null && order.getCategoryId()!=null && BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue()){
            return order.getCategoryId().longValue();
        }
        OrdOrderItem mainItem = order.getFilterMainOrderItem();
        if(mainItem.getOrderPack()!=null){
            return mainItem.getOrderPack().getCategoryId();
        }
        return mainItem.getCategoryId();
    }

    @Override
    public OrdOrderDTO initOrder(OrdOrderDTO order, boolean validInitOrder) {
        //初始化游玩日期
        initBuyInfo(order.getBuyInfo());
        //初始化订单基本信息
        initOrderBase(order);
        order.setValidInitOrder(validInitOrder);
        BuyInfo buyInfo = order.getBuyInfo();
        if(validInitOrder){
            personInitBussiness.initPerson(order);
        }

		//xiaorui add begin
		String orderDisneyInfo=buyInfo.getDisneyOrderInfo();
		logger.info("order check shanghaiDisney showticket info :" + orderDisneyInfo);
		if(StringUtils.isNotEmpty(orderDisneyInfo)){
			order.setDisneyOrderInfo(orderDisneyInfo);
		}
		
		//初始化pack和item
		doInitOrderItems(order);

		
		//计算应付金额
		try{
			if(order.getBuyInfo().getCategoryId()==18L){
				long totalAmount=0L;
                if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
                    for(OrdOrderItem orderItem:order.getOrderItemList()){
                        if(orderItem.getTotalAmount()==null){
                            totalAmount += orderItem.getPrice()*orderItem.getQuantity();
                            Log.info("-------------------------------------------------------initOrder"+orderItem.getTotalAmount()+"orderItem.getPrice()"+orderItem.getPrice()+"orderItem.getQuantity()");
                        }else{
                            totalAmount+=orderItem.getTotalAmount();
                        }
                        logger.debug("........initOrder.calc........."+orderItem.getSuppGoodsId()+"["+orderItem.hashCode()+"]\t"+orderItem.getSuppGoodsName()+"\ttotalamount="+orderItem.getTotalAmount());
                        //如果扣减买断资源成功，那么总订单  是按买断价格计算
                    }
                }
				//如果是券兑换的
				if("STAMP_PROD".equalsIgnoreCase(order.getOrderSubType()))
					logger.info("----------------------------------------------initOrder应付金额"+order.getOughtAmount());
				else
					order.setOughtAmount(totalAmount);
			}
		} catch (Exception ex){
			logger.info("$$$$$$$setOughtAmount&&&&&&&&&&"+ex.getMessage());
		}

        String remoteFlag = "O";
        logger.info("remoteFlag start: [" + remoteFlag + "], productId: [" + buyInfo.getProductId() + "]");
        long startTime = System.currentTimeMillis();
		long startLVFTime = System.nanoTime();
		if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
			Long productId = buyInfo.getProductId();
			if(OrderLvfTimePriceServiceImpl.isAutoPackProductOrder(buyInfo)) {
				OrderLvfTimePriceServiceImpl.checkAutoPackProductFlightGoods(order.getOrderItemList());
				orderLvfTimePriceServiceImpl.initPriceByAutoPackLVF(order.getOrderItemList());
			} else if(orderBookServiceDataUtil.isAutoPackTrafficProduct(productId)){
				orderLvfTimePriceServiceImpl.initPriceByAutoPackLVF(order.getOrderItemList());
			} else {
				if(StringUtils.isNotBlank(buyInfo.getNewCountPrice())&&"true".equals(buyInfo.getNewCountPrice())){
					try {
                        remoteFlag = "A";
	    				orderLvfTimePriceServiceImpl.initPriceByRemoteLVFNew(order.getOrderItemList());
	    			} catch (GetVerifiedFlightInfoFailException ex) {
                        remoteFlag = "B";
                        logger.info("remoteFlag: [" + remoteFlag + "], productId: [" + buyInfo.getProductId() + "]");
	    				throw ex;
	    			} catch (Exception ex) {
                        remoteFlag = "C";
	    				orderLvfTimePriceServiceImpl.initPriceByRemoteLVF(order.getOrderItemList());
	    			}
				}else{
                    remoteFlag = "D";
					orderLvfTimePriceServiceImpl.initPriceByRemoteLVF(order.getOrderItemList());
				}	
			}
		}

        logger.info("remoteFlag end: [" + remoteFlag + "], productId: [" + buyInfo.getProductId() + "]");
        lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.STOCK_CHECK.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "库存检查成功(remoteFlag)", "库存检查成功，用时：" + (System.currentTimeMillis() - startTime));

		logger.info("对接机票验仓验价_orderLvfTimePriceServiceImpl.initPriceByRemoteLVF_[" + (System.nanoTime() - startLVFTime) / 1000000 + "] milliseconds.["+buyInfo.getProductId()+"]");
		logger.info(ComLogUtil.printTraceInfo("OrderBookServiceImpl.initOrder", "对接机票验仓验价", "orderLvfTimePriceServiceImpl.initPriceByRemoteLVF[" + buyInfo.getProductId() + "]", (System.nanoTime() - startLVFTime) / 1000000));


		//设置是否需要调用对接平台的标志
		order.setSupplierApiFlag(OrderEnum.SUPPLIER_API_FLAG.N.name());
		if(order.isSupplierOrder()) {
			order.setSupplierApiFlag(OrderEnum.SUPPLIER_API_FLAG.Y.name());
		}
		//初始化调用对接平台的状态
		order.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.INITIAL.name());
		if(validInitOrder){
			if (order!=null&&CollectionUtils.isNotEmpty(order.getOrderPackList())&&order.getBuyInfo().getCategoryId()!=null&&18L == order.getBuyInfo().getCategoryId()) {
				for (OrdOrderPack orderPack :order.getOrderPackList()) {
					initOughtAmt(order,orderPack);
					initParams(order,orderPack);
				}
			}
			

			if(MapUtils.isEmpty(buyInfo.getPromotionNewMap())&&MapUtils.isNotEmpty(buyInfo.getPromotionMap())){
				boolean haveExcludeProm=false;
				boolean haveChannelProm=false;
				int promCount=0;
				List<Long> promotionKeys = new ArrayList<Long>();
				for(String key:buyInfo.getPromotionMap().keySet()){
					boolean flag =false;
					List<Long> promotionIds = buyInfo.getPromotionMap().get(key);
					if(CollectionUtils.isNotEmpty(promotionIds)){
						//对促销的list进行去重
						List<Long> prodIdsNew = new ArrayList<Long>();
						for(Long id:promotionIds){
							if(!prodIdsNew.contains(id)){
								prodIdsNew.add(id);
							}
						}
						
						for (Long newId : prodIdsNew) {
							for (Long old : promotionKeys) {
								if(newId.equals(old)){
									flag = true;
								}
							}
						}
						
						if(flag){
							continue;
						}
						promotionKeys.addAll(prodIdsNew);
						
						OrderPromotionBussiness bussiness = orderOrderFactory.createInitPromition(key);
						logger.info("bussiness.initPromotion params:key="+key+",promotionIds="+prodIdsNew);
						List<OrdPromotion> list = bussiness.initPromotion(order,key,prodIdsNew);
                     
                        order.addOrdPromotions(key,list);
                    }
                }
            }else{
                logger.info("buyInfo.getPromotionMap().isEmpty()");
            }
        }
        return order;
    }
    
    private void initPromotion(OrdOrderDTO order){
		BuyInfo buyInfo=order.getBuyInfo();
    	if(!order.isValidInitOrder()){
    		return ;
    	}
    	logger.info("initPromotion  map"+buyInfo.getPromotionNewMap());
		try{
			PromotionQueryDTO pq=new PromotionQueryDTO();
			pq.setDistributorChannel(buyInfo.getDistributionChannel());
			pq.setDistributorId(buyInfo.getDistributionId());
			pq.setUserNo(buyInfo.getUserNo());
			List<ItemPromotionInfo> itemPromotionInfoList=new ArrayList<ItemPromotionInfo>();
			List<OrdOrderPack> orderPackList=order.getOrderPackList();
			if(orderPackList!=null&&orderPackList.size()>0){//自由行 跟团游
				OrdOrderPack orderPack=orderPackList.get(0);
				Set<Long> set =buyInfo.getPromotionNewMap().get(orderPack.getProductId());
				if(set!=null&&set.size()>0){//选了促销
					if(orderPack.getVisitTime()==null){
						orderPack.setVisitTime(DateUtil.toSimpleDate(buyInfo.getVisitTime()));
					}	
					Map<String, Object> params = PromtionUtil.calcRouteAmount(orderPack);
					ItemPromotionInfo ipi= new ItemPromotionInfo();
					ipi.setPackageType(ITEM_TYPE.PRODUCT);
					ipi.setItemPromTarget(ITEM_TYPE.PRODUCT);
					ipi.setCategoryId(orderPack.getCategoryId());
					ipi.setSubCategoryId(buyInfo.getSubCategoryId());
					ipi.setOwnPack(orderPack.getOwnPack());
					ipi.setItemId(orderPack.getProductId());
					ipi.setPromotionIdList(new ArrayList<Long>(set));
					ItemData data=new ItemData();
					data.setVisitTime(orderPack.getVisitTime());
					if("true".equals(params.get("categoryIsRoute").toString())){
						
						data.setSaleUnit(SALE_UNIT.PEOPLE);
					}else{
						data.setSaleUnit(SALE_UNIT.COPIES);
					}
					data.setAdultPrice(Long.valueOf(params.get("adultPrice").toString()));
					if(params.get("adultQuantity")!=null){
						data.setAdultQuantity(Integer.valueOf(params.get("adultQuantity").toString()));
					}
					if(params.get("childQuantity")!=null){
						data.setChildQuantity(Integer.valueOf(params.get("childQuantity").toString()));
					}
					
					data.setChildPrice(Long.valueOf(params.get("childPrice").toString()));
					data.setNoMultiPrice(Long.valueOf(params.get("noMulPrice").toString()));
					data.setSaleType( (String)orderPack.getContentValueByKey("saleType"));
					data.setCopyQuantity(buyInfo.getQuantity());
					data.setVisitTime( orderPack.getVisitTime());
					data.setOrderDate(new Date());
					if(orderPack.getContentValueByKey("quantity")!=null){
						data.setQuantity(Integer.valueOf(orderPack.getContentValueByKey("quantity").toString()));
					}
					if(orderPack.getContentValueByKey("actualAmt")!=null){
						data.setActualAmount(Long.valueOf(orderPack.getContentValueByKey("actualAmt").toString()));
					}
					data.setTotalPrice(order.getValidPromtionAmount());
					ipi.setItemData(data);
					itemPromotionInfoList.add(ipi);
					pq.setItemPromotionInfoList(itemPromotionInfoList);
					logger.info("initPromotion queryPromotion request"+JsonUtil.getJsonString4JavaPOJO(pq));
					Response<ResponseInfoable, PromotionInfo> r=promotionService.queryPromotion(pq);
					logger.info("initPromotion queryPromotion response"+JsonUtil.getJsonString4JavaPOJO(r));
					PromotionInfo dataInfo=r.getData();
					if(dataInfo!=null&&dataInfo.getPromotionDataList()!=null){
						List<OrdPromotion> list =convertPromotionListByPack(dataInfo.getPromotionDataList(), orderPack);
						order.addOrdPromotions(String.valueOf(orderPack.getProductId()),list);
					}
				}
			}else if(order.getOrderItemList()!=null){//当地游 ,酒店套餐（供应商打包）
				Map<Long,OrdOrderItem> itemMap=new HashMap<Long,OrdOrderItem>();
				for(OrdOrderItem item:order.getOrderItemList()){
					if(PromtionUtil.validPromtionItem(item)){
						ItemPromotionInfo ipi= new ItemPromotionInfo();
						ipi.setPackageType(ITEM_TYPE.GOODS);
						ProdProduct p=item.getSuppGoods().getProdProduct();
						Set<Long> set =buyInfo.getPromotionNewMap().get(p.getProductId());
						if(set!=null&&set.size()>0){//选了促销
							BizCategory bc =p.getBizCategory();
							if("PRODUCT".equals(bc.getPromTarget())){
								ipi.setItemPromTarget(ITEM_TYPE.PRODUCT);
								itemMap.put(p.getProductId(), item);
							}else{
								ipi.setItemPromTarget(ITEM_TYPE.GOODS);
							}
							ipi.setCategoryId(item.getCategoryId());
							ipi.setItemId(item.getProductId());
							ipi.setPromotionIdList(new ArrayList<Long>(set));
							ItemData data=new ItemData();
							Map<String, Object> params = PromtionUtil.calcItemAmount(item);
							if(item.getCategoryId()==16l){//当地游
								data.setSaleUnit(SALE_UNIT.PEOPLE);
							}else{//酒店套餐
								data.setSaleUnit(SALE_UNIT.COPIES);
							}
							data.setAdultPrice(Long.valueOf(params.get("adultPrice").toString()));
							if(params.get("adultQuantity")!=null){
								data.setAdultQuantity(Integer.valueOf(params.get("adultQuantity").toString()));
							}
							if(params.get("childQuantity")!=null){
								data.setChildQuantity(Integer.valueOf(params.get("childQuantity").toString()));
							}
							data.setChildPrice(Long.valueOf(params.get("childPrice").toString()));
							data.setNoMultiPrice(Long.valueOf(params.get("noMulPrice").toString()));
							data.setVisitTime(item.getVisitTime());
							data.setOrderDate(new Date());
							data.setCopyQuantity(buyInfo.getQuantity());
							data.setTotalPrice(order.getValidPromtionAmount());
							ipi.setItemData(data);
							itemPromotionInfoList.add(ipi);
						}else{
							logger.info("initPromotion queryPromotion category:"+item.getCategoryId());
						}
					}
				}
				pq.setItemPromotionInfoList(itemPromotionInfoList);
				logger.info("initPromotion queryPromotion request"+JsonUtil.getJsonString4JavaPOJO(pq));
				Response<ResponseInfoable, PromotionInfo> r=promotionService.queryPromotion(pq);
				logger.info("initPromotion queryPromotion response"+JsonUtil.getJsonString4JavaPOJO(r));
				PromotionInfo dataInfo=r.getData();
				if(dataInfo!=null&&dataInfo.getPromotionDataList()!=null){
					List<OrdPromotion> list =convertPromotionListByItem(dataInfo.getPromotionDataList(), itemMap);
					order.addOrdPromotions(String.valueOf(buyInfo.getProductId()),list);
				}
			}
		}catch(Exception e){
			logger.error("initPromotion queryPromotion",e);
		}
		
		
	}
    
    private List<OrdPromotion>  convertPromotionListByPack(List<PromotionData> dataList,OrdOrderPack orderPack){
    	List<OrdPromotion> list =new ArrayList<OrdPromotion>();
    	OrdPromotion  op=null;
		for(PromotionData pd:dataList){
			op=new OrdPromotion();
		    op.setFavorableAmount(pd.getFavorableAmount());
			op.setObjectType(OrdPromotion.ObjectType.ORDER_PACK.name());
			op.setPromPromotionId(pd.getPromotionId());
			if(pd.getOccupyCouponAmount()!=null){
				op.setOccupyAmountFlag("true".equals(pd.getOccupyCouponAmount().toString())?"Y":"N");
			}
			op.setPromTitle(pd.getPromotionTitle());
			op.setCode(pd.getCode());
			op.setPriceType(pd.getPriceType());
			op.setTarget(orderPack);
			op.setOrderItemId(orderPack.getProductId());
			list.add(op);
		}
		return list;
	}
    
    private List<OrdPromotion>  convertPromotionListByItem(List<PromotionData> dataList,Map<Long,OrdOrderItem> itemMap){
    	List<OrdPromotion> list =new ArrayList<OrdPromotion>();
    	OrdPromotion  op=null;
		for(PromotionData pd:dataList){
			op=new OrdPromotion();
		    op.setFavorableAmount(pd.getFavorableAmount());
			op.setObjectType(OrdPromotion.ObjectType.ORDER_ITEM.name());
			op.setPromPromotionId(pd.getPromotionId());
			if(pd.getOccupyCouponAmount()!=null){
				op.setOccupyAmountFlag("true".equals(pd.getOccupyCouponAmount().toString())?"Y":"N");
			}
			op.setPromTitle(pd.getPromotionTitle());
			op.setCode(pd.getCode());
			op.setPriceType(pd.getPriceType());
			op.setTarget(itemMap.get(pd.getItemTd()));
			op.setOrderItemId(pd.getItemTd());
			list.add(op);
		}
		return list;
	}

    private void initOrder(OrdOrderDTO order){
        //初始化订单
        initOrder(order,true);
        List<OrdFormInfo> list = new ArrayList<OrdFormInfo>();
        if(!order.getBuyInfo().getAdditionMap().isEmpty()){
            for(BuyInfoAddition bia:order.getBuyInfo().getAdditionMap().keySet()){
                OrdFormInfo info = new OrdFormInfo();
                info.setContentType(bia.name());
                info.setContent(order.getBuyInfo().getAdditionMap().get(bia));
                list.add(info);
            }
        }
        BuyInfo buyInfo = order.getBuyInfo();
        OrdFormInfo info;
        //保存原订单id
        if (buyInfo.getOriginalOrderId() != null && buyInfo.getOriginalOrderId() > 0) {
            info = new OrdFormInfo(OrderEnum.OrdFormInfoContentTypeEnum.ORIGINAL_ORDER_ID.getContentType(), String.valueOf(buyInfo.getOriginalOrderId()));
            list.add(info);
        }
        //保存成人数
        info = new OrdFormInfo(OrderEnum.OrdFormInfoContentTypeEnum.ADULT_AMOUNT.getContentType(), String.valueOf(buyInfo.getAdultQuantity()));
        list.add(info);
        //保存儿童数
        info = new OrdFormInfo(OrderEnum.OrdFormInfoContentTypeEnum.CHILD_AMOUNT.getContentType(), String.valueOf(buyInfo.getChildQuantity()));
        list.add(info);
        order.setFormInfoList(list);
    }
    public BuyInfo initOrderBase(OrdOrderDTO order) {
        order.setCreateTime(new Date());//设置创建时间
        order.setOrderUpdateTime(new Date());//设置订单更新时间
        order.setBonusAmount(0L);//设置奖金支付金额
        order.setAdult(0);//设置成人数量
        order.setChild(0);//设置小孩数量
        order.setRebateAmount(0L);//设置点评返现金额
        order.setRebateFlag("N");//设置是否返现
        order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());//设置订单的状态
        order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());//设置订单的支付状态
        order.setPaymentTime(new Date());
        order.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());//设置确认状态
        order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());//设置审核状态

        order.setActualAmount(0L);//设置实际支付款项
        order.setDepositsAmount(0L);//设置定金金额
        order.setNeedInvoice("false");//是否需要发票
        order.setCurrencyCode(OrderEnum.ORDER_CURRENCY_CODE.RMB.name());//设置币种
        

        BuyInfo buyInfo = order.getBuyInfo();
        //设置下单方式
        if(StringUtils.isNotBlank(buyInfo.getOrderCreatingManner())){
            order.setOrderCreatingManner(buyInfo.getOrderCreatingManner());
        }else{
            order.setOrderCreatingManner(OrderEnum.ORDER_CREATING_MANNER.normal.getCode());
        }
        order.setTravellerDelayFlag(buyInfo.getTravellerDelayFlag());//设置是否游玩人后置
        
        //如果是游玩人后置订单，默认为游玩人未锁定
        if("Y".equals(buyInfo.getTravellerDelayFlag())){
            order.setTravellerLockFlag("N");
        }
        
        if(null!=buyInfo.getDistributionCpsID()){
            order.setDistributionCpsID(buyInfo.getDistributionCpsID());
            logger.info("DistributionCpsID = "+buyInfo.getDistributionCpsID());
        }

		//设置预售的应付金额
		if("STAMP_PROD".equalsIgnoreCase(buyInfo.getOrderSubType())){
			//设置预售券的金额
			order.setStampsAmount(buyInfo.getActualAmount());
			order.setOughtAmount(buyInfo.getOughtAmount());
		}
		//手机设备号
		order.setMobileEquipmentNo(buyInfo.getMobileEquipmentNo());
		//设置匿名下单标志
		order.setAnonymityBookFlag(buyInfo.getAnonymityBookFlag());
		//设置分销商id
		order.setDistributorId(buyInfo.getDistributionId());
		//设置分销商代码
		order.setDistributorCode(buyInfo.getDistributorCode());
		//设置分销商名称
		order.setDistributorName(buyInfo.getDistributorName());
		//分销下单工作流版本
		order.setWorkVersion(buyInfo.getWorkVersion());
		//设置行程id
		order.setLineRouteId(buyInfo.getLineRouteId());
		if(logger.isInfoEnabled()){
			logger.info("distributorCode=========="+buyInfo.getDistributorCode());
		}
		order.setRemark(buyInfo.getRemark());
		//设置分销商ID
		order.setDistributionChannel(buyInfo.getDistributionChannel());
		if(logger.isInfoEnabled()){
			logger.info("distributionChannel=========="+buyInfo.getDistributionChannel());
		}
		//设置用户id
		order.setUserId(buyInfo.getUserId());
		//设置用户编号
		order.setUserNo(buyInfo.getUserNo());
		//设置是否需要担保
		if(StringUtils.isNotEmpty(buyInfo.getNeedGuarantee())){
			order.setGuarantee(buyInfo.getNeedGuarantee());
		}else{
			order.setGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.UNGUARANTEE.name());
		}
		//设置是否需要发票
		if(StringUtils.isNotEmpty(buyInfo.getNeedInvoice())){
			order.setInvoiceStatus(buyInfo.getNeedInvoice());
		}else{
			order.setInvoiceStatus(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		}
		//留言
		order.setRemark(buyInfo.getRemark());
		order.setClientIpAddress(buyInfo.getIp());

        //设置凭证确认状态
        order.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
        //设置取消凭证确认状态
        order.setCancelCertConfirmStatus(OrderEnum.CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());
        //设置现付预定限制
        order.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
        //设置支付类型
        order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name());
        //设置出发地id
        order.setStartDistrictId(buyInfo.getStartDistrictId());
        //设置是否测试单
        order.setIsTestOrder(buyInfo.getIsTestOrder());
        //设置分销短信通道--是否只认驴妈妈标识
        order.setSmsLvmamaFlag(buyInfo.getSmsLvmamaFlag());
        //order添加预售信息
        order.setOrderSubType(buyInfo.getOrderSubType());
        //如果品类是交通+X则设置订单的品类id
        if(buyInfo.getCategoryId()!=null && BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==buyInfo.getCategoryId().longValue()){
            order.setCategoryId(buyInfo.getCategoryId().longValue());
        }
        return buyInfo;
    }

    /**
     * 初始化订单电子合同
     */
    public void initOrdTravelContract(OrdOrderDTO order){

        try {
            List<CreateOrdTravelContractData> contractDataList = new ArrayList<CreateOrdTravelContractData>();
            List<CreateOrdTravelContractData> localContractDataList = new ArrayList<CreateOrdTravelContractData>();

            //订单所有合同集合
            List<OrdTravelContract> contracts = order.getOrdTravelContractList();
            List<OrdTravelContract> contractsLocalRoute =  new ArrayList<OrdTravelContract>();

            if(contracts==null){
                contracts = new ArrayList<OrdTravelContract>();
            }
            ResultHandleT<ProdLineRoute> lineRouteResult = null;
            ProdLineRoute prodLineRoute=null;

            Long lineRouteId =order.getLineRouteId();
            if(lineRouteId!=null){
                try {
                    lineRouteResult = this.prodLineRouteClientService.findProdLineRouteById(lineRouteId);
                    if (lineRouteResult!=null) {
                        prodLineRoute=lineRouteResult.getReturnContent();
                    }
                } catch (Exception e) {
                    logger.error("order找不到对应的行程，行程ID：" + lineRouteId + "，异常信息： {}", e);
                }
            }else {
                logger.error("order中，无行程ID：" + lineRouteId + "，异常信息： {}");
            }

            boolean hasPreauthBook=false;//强制预授权是否
            if (TimePriceUtils.hasPreauthBook(order.getLastCancelTime(),order.getCreateTime())) {
                hasPreauthBook=true;
            }
            String paymentType = order.getPaymentType();
            if(StringUtils.isNotEmpty(paymentType)){
                if(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equals(paymentType)){
                    hasPreauthBook=true;
                }
            }
            logger.info("强制预授权状态："+hasPreauthBook);
            List<OrdOrderPack> orderPackList = order.getOrderPackList();
            CreateOrdTravelContractData appointContractData = null;
            if(orderPackList!=null){
                logger.info("loop orderPackList start...");
                for(OrdOrderPack orderPack:orderPackList){
                    List<OrdOrderItem> orderItemList = orderPack.getOrderItemList();
                    Long parentProductId= orderPack.getProductId();
                    appointContractData = initData(appointContractData, parentProductId, orderItemList, contractDataList,false);
                    if(appointContractData!=null){
                        break;
                    }                                  
                }
                logger.info("loop orderPackList end...");
            }
            List<OrdOrderItem> listMainOrderItem = new ArrayList<OrdOrderItem>();
            List<OrdOrderItem> locaOrderItem = new ArrayList<OrdOrderItem>();
            if(appointContractData == null) {
                if(CollectionUtils.isNotEmpty(order.getNopackOrderItemList())){
                    //关联销售
                    List<OrdOrderItem> nopackOrderItemList = order.getNopackOrderItemList();
                    logger.info("order.getNopackOrderItemList() size = " + nopackOrderItemList.size());

                    for (OrdOrderItem orderItem:nopackOrderItemList) {
                        if ("localRoute".equals(orderItem.getItem().getGoodType())) {
                            locaOrderItem.add(orderItem);
                        }else {
                            listMainOrderItem.add(orderItem);
                        }
                    }

                    if(CollectionUtils.isNotEmpty(listMainOrderItem)){
                        logger.info("listMainOrderItem size = "+listMainOrderItem.size());
                        for(OrdOrderItem item :listMainOrderItem){
                            if(item.hasMainBranchAttach()){
                                Long productId = item.getProductId();
                                List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
                                itemList.add(item);
                                appointContractData = initData(appointContractData, productId, itemList, contractDataList, false);
                                if(appointContractData != null)
                                    break;
                            }
                        }
                    }

                    if(CollectionUtils.isNotEmpty(locaOrderItem)){
                        logger.info("locaOrderItem size = "+locaOrderItem.size());
                        for(OrdOrderItem item :locaOrderItem){
                            if(item.hasMainBranchAttach()){
                                Long productId = item.getProductId();
                                List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
                                itemList.add(item);
                                appointContractData = initData(appointContractData, productId, itemList, localContractDataList, true);
                            }
                        }
                    }
                }
            }
        
        
        boolean relatedMarketingFlag = isExsitLocalRouteItemInOrder(order);//判断订单是否有"关联销售当地游"订单
        boolean orderItemLocalOrGroupRouteFlag = isLocalOrGroupRouteOrderItem(order.getOrderItemList());//该子订单是否包含当地游，跟团游产品
        
        Long distributorId = order.getDistributorId();//销售渠道ID
        
        //旅游产品预售协议/赴台旅游预订须知/浙江省赴台旅游合同
        if(appointContractData != null){
            List<OrdOrderItem> itemList = appointContractData.getOrderItemList();
            createOrderTravelContract(appointContractData.getEcontractTemplate(), contracts, itemList,distributorId);
        }else {
            prodLineRoute = makeContracData(contractDataList, contracts,prodLineRoute, hasPreauthBook,distributorId,relatedMarketingFlag,orderItemLocalOrGroupRouteFlag);
        }

        //线路关联当地游合同构建走此逻辑
        if(CollectionUtils.isNotEmpty(locaOrderItem)){
            prodLineRoute = makeContracData(localContractDataList, contractsLocalRoute,prodLineRoute, hasPreauthBook,distributorId,relatedMarketingFlag,orderItemLocalOrGroupRouteFlag);
        }
        contracts.addAll(contractsLocalRoute);

        if(CollectionUtils.isNotEmpty(contracts)){
            logger.info("contracts size = "+contracts.size());
        } else {
            logger.info("contracts is null");
        }
        //订单合同
         order.setOrdTravelContractList(contracts);
        } catch (Exception e) {
            logger.error("电子合同调取规则异常",e);
        }
    }
    
    //判断订单是否有"关联销售当地游"订单
    protected boolean isExsitLocalRouteItemInOrder(OrdOrder order) {
        boolean relatedMarketingFlag = false;
        List<OrdOrderItem> OrdOrderItemList = order.getOrderItemList();
        
        for(OrdOrderItem ordOrderItem : OrdOrderItemList){
            if(ordOrderItem.getContent() != null  && ordOrderItem.getContent().length()>0){
                String relatedMarketingFlagStr = (String)ordOrderItem.getContentValueByKey("relatedMarketingFlag");
                if(relatedMarketingFlagStr != null && "localRoute".equals(relatedMarketingFlagStr)){
                    relatedMarketingFlag = true;
                }
            }
        }
        return relatedMarketingFlag;
    }
    //该子订单是否包含当地游，跟团游产品
    protected boolean isLocalOrGroupRouteOrderItem(List<OrdOrderItem> ordOrderItem) {
        boolean orderItemLocalRouteFlag = false;
        if (ordOrderItem != null && ordOrderItem.size() > 0) {
            for (OrdOrderItem item : ordOrderItem) {
                if (item != null && (item.getCategoryId().longValue() == 16 || item.getCategoryId().longValue() == 15)) {
                    orderItemLocalRouteFlag = true;
                }
            }
        }
        return orderItemLocalRouteFlag;
    }

    private ProdLineRoute makeContracData(
            List<CreateOrdTravelContractData> contractDataList,
            List<OrdTravelContract> contracts, ProdLineRoute prodLineRoute,
            boolean hasPreauthBook, Long distributorId,boolean relatedMarketingFlag,boolean orderItemLocalOrGroupRouteFlag) {
        
        logger.info("OrderBookServiceImpl.makeContracData==>distributorId="+distributorId+",relatedMarketingFlag="+relatedMarketingFlag+",orderItemLocalOrGroupRouteFlag="+orderItemLocalOrGroupRouteFlag);
        
        ResultHandleT<ProdLineRoute> lineRouteResult;
        Long lineRouteId;
        for(CreateOrdTravelContractData createData:contractDataList){
            ProdProduct product = createData.getParentProduct();
            ProdEcontract prodEcontract = product.getProdEcontract();

            String categoryCode = product.getBizCategory().getCategoryCode();
            String productType =  product.getProductType();
            String packageType = product.getPackageType();
            List<OrdOrderItem> itemList = createData.getOrderItemList();
            logger.info("---------------------------------------合同的log-------------------------"+categoryCode+"++++++++++"+productType+"-------------"+packageType);

            //跟团游
             if("category_route_group".equals(categoryCode)){
                 //国内短线
                 if("INNERSHORTLINE".equals(productType)){
                    //国内团队旅游合同
                    createOrderTravelContract( ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);
                }
                 //国内长线
                 if(PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){
                     //国内团队旅游合同
                     createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);

                 }

                //出境/港澳台
                 if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType) || PRODUCTTYPE.INNER_BORDER_LINE.getCode().equals(productType)){
                    //出境旅游合同
                     if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType) && ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
                         logger.info("makeContracData group_foreign and productId is "+product.getProductId());
                        //委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                     } else {
                         //自主打包并且是上海总公司的才需要推送至金棕榈
                         if (PACKAGETYPE.LVMAMA.getCode()
                                 .equals(packageType)
                                 && (StringUtils.isBlank(product
                                 .getFiliale()) || CommEnumSet.FILIALE_NAME.SH_FILIALE
                                 .name().equalsIgnoreCase(
                                         product.getFiliale()))) {
                             createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode(), contracts, itemList, Constant.CONTRACT_NEED_SYNC,distributorId);
                         } else {
                             createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode(), contracts, itemList,distributorId);
                         }
                     }
                    // 预付款协议
                    if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
                        createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode(), contracts, itemList,distributorId);
                    }
                 }
            }
             //交通+X     updated by Libing 2015-12-02
             if(BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCode().equals(categoryCode)){
                 createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
             }
             
             //自由行（机酒、景酒、交通+服务）
             if(BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(categoryCode)){
                 logger.info("makeContracData product is" + product.getProductId() + ",categoryCode="+categoryCode);
                    //委托服务协议
                     if(ProductPreorderUtil.isDestinationBUDetail(product)){
                         logger.info("makeContracData product isDestinationBUDetail and productId is "+product.getProductId());
                         //目的地委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                     }else{
                         //下单是否包含（关联销售（当地游），选择线路（当地游，跟团游））
                        if(PRODUCTTYPE.INNERLINE.getCode().equals(productType) && (relatedMarketingFlag || orderItemLocalOrGroupRouteFlag)){
                            //国内团队旅游合同
                            createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);
                        }else{
                            //委托服务协议
                             createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                        }
                     }
                     //国外
                    if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
                        // 预付款协议
                        if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
                            createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode(), contracts, itemList,distributorId);
                        }
                    }
            }
             
             
             
             //酒店套餐
             if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryCode)){
                //委托服务协议
                 if(ProductPreorderUtil.isDestinationBUDetail(product)){
                     logger.info("makeContracData product isDestinationBUDetail and productId is "+product.getProductId());
                     //目的地委托服务协议
                     createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                 }else{
                     //委托服务协议
                     createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                 }
                 //国外
                if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
                    // 预付款协议
                    if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
                        createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode(), contracts, itemList,distributorId);
                    }
                }
            }
             
            
             

            //当地游
             if(BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)){

                 for(OrdOrderItem  ordOrderItem: itemList){
                     Item item= ordOrderItem.getItem();
                     String goodType = item.getGoodType();
                     if(goodType != null && "localRoute".equals(goodType)){//若为关联销售当地游，取当地游产品的行程

                        Long productId = ordOrderItem.getProductId();

                        ProdProductParam param = new ProdProductParam();
                        param.setProductProp(true);
                        param.setProductBranchValue(true);
                        param.setProdEcontract(true);
                        param.setLineRoute(true);
                        ResultHandleT<ProdProduct> resultHandle = productClientService.findLineProductByProductId(productId, param);

                        ProdProduct prodProduct = resultHandle.getReturnContent();

                        ProdLineRouteVO prodLineRouteVO =null;
                        if(CollectionUtils.isNotEmpty(prodProduct.getProdLineRouteList())) {
                            prodLineRouteVO = prodProduct.getProdLineRouteList().get(0);
                        }

                        lineRouteId = prodLineRouteVO.getLineRouteId();//当地游产品的行程ID

                        if(lineRouteId!=null){
                            try {
                                lineRouteResult = this.prodLineRouteClientService.findProdLineRouteById(lineRouteId);
                                if (lineRouteResult!=null) {
                                    prodLineRoute=lineRouteResult.getReturnContent();
                                }
                            } catch (Exception e) {
                                logger.error("order找不到对应的行程，行程ID：" + lineRouteId + "，异常信息： {}", e);
                            }
                        }else {
                            logger.error("order中，无行程ID：" + lineRouteId + "，异常信息： {}");
                        }

                     }


                     //国内
                     if(PRODUCTTYPE.INNERLINE.getCode().equals(productType) || PRODUCTTYPE.INNERSHORTLINE.getCode().equals(productType) || PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){

                        //国内团队旅游合同
                        createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);

                     }
                    //国外
                     if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
                         //委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);

                        // 预付款协议
                        if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
                            createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode(), contracts, itemList,distributorId);
                        }
                     }
                 }
             }

             //签证
             if(BIZ_CATEGORY_TYPE.category_visa.getCode().equals(categoryCode)){
                 //委托服务协议
                 createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
             }
             
            /**
             * 定制游
             */
            if (BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.name().equalsIgnoreCase(categoryCode)) {
                //国内短线
                 if("INNERSHORTLINE".equals(productType)){
                    
                    if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
                        //委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                    }else{
                        //国内团队旅游合同
                        createOrderTravelContract( ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);
                    }
                 }
                 //国内长线
                 if(PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){
                     
                     if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
                         //委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                     }else{
                        //国内团队旅游合同
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, itemList,distributorId);
                         
                     }
                 }
                 
                //出境/港澳台
                 if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
                    
                     if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
                         //委托服务协议
                         createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, itemList,distributorId);
                     }else{
                        //出境旅游合同
                         //自主打包并且是上海总公司的才需要推送至金棕榈
                         if (PACKAGETYPE.LVMAMA.getCode().equals(packageType) && (StringUtils.isBlank(product.getFiliale()) || CommEnumSet.FILIALE_NAME.SH_FILIALE.name().equalsIgnoreCase(product.getFiliale()))) {
                             createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode(), contracts, itemList, Constant.CONTRACT_NEED_SYNC,distributorId);
                         } else {
                             createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode(), contracts, itemList,distributorId);
                         }
                         
                        // 预付款协议
                        if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
                            createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode(), contracts, itemList,distributorId);
                        }
                     }
                 }
            }

        }
        return prodLineRoute;
    }



    public CreateOrdTravelContractData initData(CreateOrdTravelContractData appointContractData,Long parentProductId,List<OrdOrderItem> orderItemList,
            List<CreateOrdTravelContractData> contractDataList,boolean flag){
        ProdProductParam param = new ProdProductParam();
         param.setDest(true);
         param.setLineRoute(true);
         param.setProdEcontract(true);
         ProdProduct product = null;
         if(destHotelAdapterUtils.checkHotelRouteEnableByProductId(parentProductId))
         {
             ResultHandleT<ProdProduct> result = productHotelAdapterClientService.findProdProductByIdFromCache(parentProductId);
             product = result.getReturnContent();
         }else{
             product = productClientService.findLineProductByProductId(parentProductId, param).getReturnContent();
         }
         if(product.getProdEcontract()!=null && (ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.name().equals(product.getProdEcontract().getEcontractTemplate())
                 || ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equals(product.getProdEcontract().getEcontractTemplate())
                 || ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equals(product.getProdEcontract().getEcontractTemplate()))){
             appointContractData= new CreateOrdTravelContractData();
             appointContractData.setOrderItemList(orderItemList);
             appointContractData.setParentProduct(product);
             appointContractData.setEcontractTemplate(product.getProdEcontract().getEcontractTemplate());
             return appointContractData;
         }else{
             CreateOrdTravelContractData data =new CreateOrdTravelContractData();
             data.setOrderItemList(orderItemList);
             data.setParentProduct(product);
             contractDataList.add(data);
             if (flag) {
                 return appointContractData;
            }
             return null;
         }
    }

    /**
     * 查询生成预付款协议的目的地列表
     * @return
     */
    public List<String> getCreatePrePayEcontractDest(String code){
        String prePayDestStr = Constant.getInstance().getProperty(code);
        List<String> prePayDestIds = new ArrayList<String>();
        if(StringUtils.isNotEmpty(prePayDestStr)){
            JSONObject json = JSONObject.fromObject(prePayDestStr);
            Set<String> keys = json.keySet();
            for(String key:keys){
                prePayDestIds.add(json.getString(key));
            }
        }
        logger.info("prePayDestIds========================"+prePayDestIds);
        return  prePayDestIds;
    }


    /**
     * 初始化关联销售商品对应国内团队旅游合同信息
     */
    public void initRelationSalesContract(OrdOrderDTO order,List<OrdTravelContract> contracts,Long distributorId){
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        if(orderItemList!=null){
            for(OrdOrderItem item :orderItemList){
                if(item.getOrderPackId()==null){
                    Long productId = item.getProductId();
                    //ProdProduct product = productClientService.findProdProductById(productId).getReturnContent();
                    ProdProduct product = productClientService.findProdProductByIdFromCache(productId).getReturnContent();
                    String category = product.getBizCategory().getCategoryCode();
                    String productType = product.getProductType();
                    //如果是国内当地游，签署国内团队旅游合同
                    if(BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(category)&&"INNERLINE".equals(productType)){
                        //国内团队旅游合同
                        List<OrdOrderItem> orderItem= new ArrayList<OrdOrderItem>();
                        orderItem.add(item);
                        createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode(), contracts, orderItem,distributorId);
                    }
                }
            }
        }
    }

    /**
     *初始化订单合同信息
     * @param distributorId
     */
    public void createOrderTravelContract(String template,List<OrdTravelContract> contracts,List<OrdOrderItem> orderItemList, Long distributorId){
        OrdTravelContract travel = new OrdTravelContract();
        travel.setContractTemplate(template);
        travel.setCreateTime(new Date());
        if (distributorId!=null &&(distributorId.longValue()==Constant.DIST_O2O_SELL || distributorId.longValue()==Constant.DIST_O2O_APP_SELL)) {
            travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.getCode());
        }else {
            travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.ONLINE.getCode());
        }
        travel.setStatus(ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode());
        travel.setOrderItems(orderItemList);
        contracts.add(travel);
    }

    public void createOrderTravelContract(String template,List<OrdTravelContract> contracts,List<OrdOrderItem> orderItemList, int needSync, Long distributorId){
        OrdTravelContract travel = new OrdTravelContract();
        travel.setContractTemplate(template);
        travel.setCreateTime(new Date());
       if(distributorId!=null && (distributorId.longValue()==Constant.DIST_O2O_SELL || distributorId.longValue()==Constant.DIST_O2O_APP_SELL)) {
            travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.getCode());
        }else {
            travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.ONLINE.getCode());
        }
        travel.setStatus(ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode());
        travel.setOrderItems(orderItemList);
        travel.setNeedSync(needSync);
        contracts.add(travel);
    }



    /**
     * 判断目的地是否包含指定地区中
     * @param
     * @param
     * @return
     */
    public boolean isExistAssignDestination(ProdProduct product,List<String> destIdList){
        List<ProdDestRe> relist = product.getProdDestReList();
        if(relist!=null&&relist.size()>0){
            for(ProdDestRe re:relist){
                if(re.getDestId()==null){
                    return false;
                }
                Map<String, Object> foreignParams = new HashMap<String, Object>();
                foreignParams.put("destId", re.getDestId());
                foreignParams.put("destIdList", destIdList);
                int result = destContentClientService.hasExistAssignDest(foreignParams).getReturnContent();
                if(result!=0)
                    return true;
            }
        }
        return false;
    }           
    /**
     * 判断行程中是否包含北京一日游
     * @param product
     * @return
     */
    public boolean isBeijingOneDayTravel(ProdProduct product,ProdLineRoute prodLineRoute){
         //行程
        ProdLineRoute reute = prodLineRoute;
        //始发地
         if(product.getBizDistrict()==null){
             return false;
         }
         long begin = product.getBizDistrict().getDistrictId();

         String beijinDistrictIdStr = Constant.getInstance().getProperty("create_beijin_econtract_district");
         if(StringUtils.isEmpty(beijinDistrictIdStr)){
             logger.error("始发地区域北京Id未配置");
                //throwIllegalException("始发地区域北京Id未配置");
         }
         long beijinDistrictId = Integer.parseInt(beijinDistrictIdStr);

        String beijinDestId = Constant.getInstance().getProperty("create_beijin_econtract_dest");
        if(StringUtils.isEmpty(beijinDestId)){
            logger.error("目的地北京Id未配置");
            //throwIllegalException("目的地北京Id未配置");
        }
        List<String> dest = new ArrayList<String>();
        dest.add(beijinDestId);

        //目的地是否包含北京
        boolean destBeijing = isExistAssignDestination(product, dest);
        //始发地北京 目的地北京  行程1天，入住0
        if(destBeijing&&begin==beijinDistrictId&&reute.getRouteNum()==1&&reute.getStayNum()==0){
            return true;
        }
        return false;
    }

    @Autowired
    private ProdProductSaleReClientService prodProductSaleReClientService;

	/**
	 * 初始化pack相关数据
	 */
	private OrdOrderPack initPack(OrdOrderDTO order,BuyInfo.Product itemProduct) {
		OrdOrderPackDTO orderPack = new OrdOrderPackDTO();
		ProdProduct product = productClientService.findProdProductById(itemProduct.getProductId(), false, false);
		checkSaleAble(product);
		orderPack.setProduct(product);
		orderPack.setCategoryId(product.getBizCategoryId());
		
		orderPack.setProductId(itemProduct.getProductId());
		orderPack.setOrder(order);
		orderPack.setProductName(product.getProductName());
		//如果是机酒动态打包
		if(OrderLvfTimePriceServiceImpl.isAutoPackProductOrder(order.getBuyInfo())){
			orderPack.setProductName(itemProduct.getProductName());
		}
		orderPack.setOwnPack(product.getPackageType());
		orderPack.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(), product.getBizCategory().getCategoryCode());
		OrderPackInitBussiness business = null;
		
		//获取不同品类的业务处理类
		business = orderOrderFactory.createInitPackProduct(orderPack);
		
		business.initOrderPack(orderPack,itemProduct);
		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		logger.info("----itemProduct getItemList size is ----" + itemProduct.getItemList().size()) ;

		//如果为门票下 组合套餐票 add 熊佳冰
		if("Y".equals(order.getBuyInfo().getIslvmamaPackge())){
			for(BuyInfo.Item item:itemProduct.getItemList()){			
				if(item.getTaobaoETicket()==null){
					item.setTaobaoETicket(itemProduct.getTaobaoETicket());
				}		
				item.setVisitTime(order.getBuyInfo().getProdPackageItemDateMap().get(item.getGoodsId()));						
				OrdOrderItem orderItem = initItem(order, item,orderPack);
				if(orderItem!=null){
					logger.info("----添加orderItem----");
					orderItemList.add(orderItem);
				}
			}
		}else{		
			for(BuyInfo.Item item:itemProduct.getItemList()){			
				if(item.getTaobaoETicket()==null){
					item.setTaobaoETicket(itemProduct.getTaobaoETicket());
				}
				OrdOrderItem orderItem = initItem(order, item,orderPack);
				if(orderItem!=null){
					logger.info("----添加orderItem----");
					orderItemList.add(orderItem);
				}
			}		
		}		
//		orderLvfTimePriceServiceImpl.initPriceByRemoteLVF(orderItemList);
		orderPack.setOrderItemList(orderItemList);
		//供应商产品名称
		if(StringUtils.isNotEmpty(product.getSuppProductName())) {
			orderPack.putContent(OrderEnum.ORDER_COMM_TYPE.supp_product_name.name(), product.getSuppProductName());
		}
		
		// This part is for lvmama package bug, start ->
		if(orderPack.hasOwn() && orderPack.getCategoryId() == 18L) {
			logger.info("$$------$$ itemProduct.getProductId() = " + itemProduct.getProductId());
			List<ProdProductSaleRe> productSaleReList = prodProductSaleReClientService.queryByProductId(itemProduct.getProductId()).getReturnContent();
			if(null == productSaleReList) {
				logger.info("$$------$$ query productSaleRe for productId(" + productSaleReList + ") is null");
			}
			if(productSaleReList.size() > 0) {
				logger.info("$$------$$ productSaleReList.size() = " + productSaleReList.size());
				ProdProductSaleRe prodProductSaleRe = productSaleReList.get(0);
				if(null != prodProductSaleRe.getSaleType() && "COPIES".equals(prodProductSaleRe.getSaleType())) {
					orderPack.putContent("package_lvmama_saleType", prodProductSaleRe.getSaleType());
					Short packageAdultQuatity = prodProductSaleRe.getAdult();
					Integer avaliableAdultQuatity = (Integer)orderPack.getContentValueByKey(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name());
					if((packageAdultQuatity != 0) && (avaliableAdultQuatity != 0)) {
						if(avaliableAdultQuatity % packageAdultQuatity == 0) {
							int packageNums = avaliableAdultQuatity / packageAdultQuatity;
							logger.info("$$------$$ packageNums = " + packageNums);
							orderPack.putContent("packageNums_lvmama", packageNums);
						} else {
							logger.info("$$------$$ avaliableAdultQuatity is not in multiple of packageAdultQuatity!");
						}
					} else {
						logger.info("$$------$$ packageAdultQuatity is 0 or avaliableAdultQuatity is 0!");
					}
				}
			} else {
				logger.info("$$------$$ productSaleReList size() is not equal 1, maybe somthing wrong at server(vst_back) side!");
			}
		}
		// This part is for lvmama package bug, end <-
		
		//冗余下单时间
		orderPack.setCreateTime(order.getCreateTime());
		return orderPack;
	}
	
	private void initOughtAmt(final OrdOrderDTO order, final OrdOrderPack orderPack){
		//计算应付金额
		try{
			long totalAmount=0L;
			for(OrdOrderItem orderItem:orderPack.getOrderItemList()){
				//过滤保险和快递 add by lujie
				if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())||BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
					continue;
				}
				
				if(orderItem.getTotalAmount()==null){
					totalAmount += orderItem.getPrice()*orderItem.getQuantity();
					Log.info("-------------------------------------------------------initOrder"+orderItem.getTotalAmount()+"orderItem.getPrice()"+orderItem.getPrice()+"orderItem.getQuantity()");
				}else{
					totalAmount+=orderItem.getTotalAmount();
				}
				logger.debug("........initOrder.calc........."+orderItem.getSuppGoodsId()+"["+orderItem.hashCode()+"]\t"+orderItem.getSuppGoodsName()+"\ttotalamount="+orderItem.getTotalAmount());
				//如果扣减买断资源成功，那么总订单  是按买断价格计算
			}
			//如果是券兑换的
			if("STAMP_PROD".equalsIgnoreCase(order.getOrderSubType()))
				logger.info("----------------------------------------------initOrder应付金额"+order.getOughtAmount());
			else
				order.setOughtAmount(totalAmount);
		} catch (Exception ex){
			logger.error("$$$$$$$setOughtAmount&&&&&&&&&&"+ex.getMessage());
		}
	}
	
	private void initParams(final OrdOrderDTO order, final OrdOrderPack orderPack){
		
		String saleType = ProdProductSaleRe.SALETYPE.COPIES.name();

		ResultHandleT<List<ProdProductSaleRe>> resultHandleT = prodProductSaleReClientService.queryByProductId(order.getBuyInfo().getProductId());
		if(resultHandleT != null && resultHandleT.isSuccess()){
			List<ProdProductSaleRe> prodProductSaleRes = resultHandleT.getReturnContent();
			if(!CollectionUtils.isEmpty(prodProductSaleRes)){
				saleType = prodProductSaleRes.get(0).getSaleType();
			}
		}
		orderPack.putContent("saleType", saleType);
		
		String quantity = "0";
		if(saleType.equals(ProdProductSaleRe.SALETYPE.COPIES.name())){
			quantity = String.valueOf(order.getBuyInfo().getQuantity());
			if(quantity==null||quantity.equals("0"))
				quantity = String.valueOf(order.getBuyInfo().getProductList().get(0).getQuantity());
			Integer adultQuantity = order.getBuyInfo().getAdultQuantity();
			if(quantity==null||quantity.equals("0")){
				quantity = String.valueOf(adultQuantity);
			}else{
				Integer q = Integer.valueOf(quantity);
				if(q>adultQuantity){
					quantity = String.valueOf(adultQuantity);
				}
			}
		} else if(saleType.equals(ProdProductSaleRe.SALETYPE.PEOPLE.name())){
			quantity = String.valueOf(order.getBuyInfo().getAdultQuantity());
		}
		
		Long actualAmt = order.getOughtAmount();
//		for(OrdOrderItem orderItem:orderPack.getOrderItemList()){
//			//去除保险费
//			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
//				Long insuranceQuantity = 1L;
//				if(null != orderItem.getQuantity()){
//					insuranceQuantity = orderItem.getQuantity();
//				}
//				logger.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
//				actualAmt = actualAmt-orderItem.getPrice()*insuranceQuantity;
//			}
//			//去除快递押金的费用
//			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
//				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
//					Long depositQuantity = 1L;
//					if(null != orderItem.getQuantity()){
//						depositQuantity = orderItem.getQuantity();
//					}
//					logger.info("扣除押金费 :"+orderItem.getPrice());
//					actualAmt = actualAmt - orderItem.getPrice()*depositQuantity;
//				}else{
//					logger.info("扣除快递费 :"+orderItem.getPrice());
//					actualAmt = actualAmt - orderItem.getPrice();
//				}
//			}
//		}
		logger.info("---------------------order.getActualAmount()=" + actualAmt);
		orderPack.putContent("actualAmt", actualAmt);
		//设置购买份数
		orderPack.putContent("quantity", quantity);
		
	}
	

	public void initItemDetail(final OrdOrderDTO order,final OrdOrderItem orderItem,final SuppGoods suppGoods){
		initOrderItemBase(order, orderItem, suppGoods);
		OrderInitBussiness bussiness = orderOrderFactory.createInitProduct(orderItem);
		bussiness.initOrderItem(orderItem, order);

        fillOrderPaymentTarget(order,suppGoods);
    }

    private void checkSaleAble(SuppGoods suppGoods){
        logger.info("scheckSaleAble开始");
        ResultHandleT<BizBranch> branch = branchClientService.findBranchById(suppGoods.getProdProductBranch().getBranchId());
        if(branch==null||branch.hasNull()){
            throwIllegalException("商品不可售");
        }
        suppGoods.getProdProductBranch().setBizBranch(branch.getReturnContent());
        if(!suppGoods.isValid()) {
            throwIllegalException("商品不可售");
        }
        logger.info("scheckSaleAble结束");
    }
    private void checkSaleAble(ProdProduct product){
        if(product==null || !"Y".equalsIgnoreCase(product.getCancelFlag())){
            throwIllegalException("商品不可售");
        }
    }

    private OrdOrderItem initItem(final OrdOrderDTO order,BuyInfo.Item item, OrdOrderPack orderPack) {
        logger.info("----start initItem-----");
        if(item.getQuantity()>0 || (item.getAdultQuantity() + item.getChildQuantity()) > 0){
            OrdOrderItemDTO orderItem = new OrdOrderItemDTO();
            orderItem.setOrderPack(orderPack);
            //保存子订单是否是预售券
            orderItem.setOrderSubType(item.getOrderSubType());
            orderItem.setVisitTime(item.getVisitTimeDate());
            orderItem.setSuppGoodsId(item.getGoodsId());
            orderItem.setQuantity((long)item.getQuantity());
            orderItem.setItem(item);
            orderItem.setUseTime(item.getUseTime());
            orderItem.setLocalHotelAddress(item.getLocalHotelAddress());
            orderItem.setTotalAmount(item.getTotalAmount());
            orderItem.setTotalSettlementPrice(item.getTotalSettlementPrice());
            orderItem.setSharedStockList(item.getSharedStockList());
            // 结构化酒店套餐,具体选择规格
            orderItem.setHotelcombOptions(item.getHotelcombOptions());
            SuppGoodsParam param = new SuppGoodsParam();
            param.setProduct(true);
            ProdProductParam ppp = new ProdProductParam();
            ppp.setBizCategory(true);
            ppp.setProductProp(true);
            ppp.setProductPropValue(true);
            param.setProductBranch(true);
            param.setSupplier(true);
            param.setProductParam(ppp);
            param.setSuppGoodsExp(true);
            param.setSuppGoodsEventAndRegion(true);
            //信用住
            orderItem.setCreditTag(item.getCreditTag());

            logger.info("suppGoodsClientService.findSuppGoodsById start---goodsId="+item.getGoodsId()+item.getOrderSubType());

            ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsHotelAdapterService.findSuppGoodsById(item.getGoodsId(), param);
            SuppGoods suppGoods = suppGoodsResultHandleT.getReturnContent();
            //如果商品是对接机票，但订单的游玩人后置标识为Y，则抛出异常，阻止下单
            if(order == null){
                throwNullException("订单为空，不能下单");
            }
            if(orderBookServiceDataUtil.isApiFlight(suppGoods) && "Y".equalsIgnoreCase(order.getTravellerDelayFlag())){
                //日志打印，通常的格式是"订单产品[123]中的商品[13342]是对接机票，因此订单游玩人不能后置!"
                StringBuilder sb = new StringBuilder("订单产品[").append(order.getProductId()).append("]中的商品[").append(suppGoods.getSuppGoodsId()).append("]是对接机票，因此订单游玩人不能后置!");
                logger.error(sb.toString());
                throwNullException("含对接机票的产品，游玩人不能后置");
            }

            logger.info("suppGoodsClientService.findSuppGoodsById返回");
            if(suppGoods==null){
                throwNullException("商品信息不存在");
            }
            logger.info("---suppGoodsId="+suppGoods.getSuppGoodsId()+",mailFlag="+suppGoods.getMailFlag());
            //如果为景点门票 判断是否可以可以开发票 并存入子订单的大字段中
            String ticketCategoryId=suppGoods.getCategoryId()==null?"":suppGoods.getCategoryId().toString();
            if(Constant.VST_CATEGORY.CATEGORY_SINGLE_TICKET.getCategoryId().equals(ticketCategoryId)){
                ArrayList<Long> list =Lists.newArrayList();
                list.add(item.getGoodsId());
                com.lvmama.scenic.api.vo.ResultHandleT<Map<Long, Boolean>> ResultHandleT=scenicTicketProductService.queryProductInvoceByGoodsIds(list,null);
                if(ResultHandleT !=null && ResultHandleT.getReturnContent()!=null){
                    Boolean needInvoince=ResultHandleT.getReturnContent().get(item.getGoodsId());
                    if(null!=needInvoince && needInvoince){
                        orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.needInvoice.name(), "Y");
                    }else {
                        orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.needInvoice.name(), "N");
                    }
                }else {
                    orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.needInvoice.name(), "N");
                }
            }
            ProdProduct prodProduct = null; 
            if(null == orderPack)
                orderItem.setCategoryId(item.getProductCategoryId());
            else{
                orderItem.setCategoryId(suppGoods.getCategoryId());
                item.setProductCategoryId(suppGoods.getCategoryId());
                prodProduct = orderPack.getProduct();
            }
            
            //对于国内景酒产品，检查酒店的入住日期是否正确 
            checkPackageGroupStartDate(order, item, orderItem, suppGoods, prodProduct);
            
            try {
                if(suppGoods.getProdProduct()!=null){
                    //冗余产品类型
                    orderItem.setProductType(suppGoods.getProdProduct().getProductType());
                    //冗余目的地信息
                    Map<String, Object> prodDestReParams = new HashMap<String, Object>();
                    prodDestReParams.put("productId", suppGoods.getProdProduct().getProductId());
                    ResultHandleT<List<ProdDestRe>> resultHandleT = prodDestReClientRemote.selectWithDestListByParams(prodDestReParams);
                    int maxDestNamelength = 490;  //目的地冗余最大长度
                    if(resultHandleT!=null && resultHandleT.isSuccess() && !resultHandleT.hasNull()){
                        List<ProdDestRe> destList = resultHandleT.getReturnContent();
                        if(destList==null){
                            destList = Collections.emptyList();
                        }
                        List<Long> destIds = new ArrayList<>();
                        for (ProdDestRe prodDestRe : destList) {
                            if(prodDestRe==null){
                                continue;
                            }
                            Long destId  = prodDestRe.getDestId();
                            if(destId!=null && !destIds.contains(destId)){
                                destIds.add(destId);
                            }
                        }
                        List<String> nameList = Collections.emptyList();
                        ResultHandleT<List<String>> nameResult = destClientService.findDestNamesByDestIds(destIds);
                        if(nameResult!=null && nameResult.isSuccess() && !nameResult.hasNull()){
                            nameList = nameResult.getReturnContent();
                        }
                        StringBuilder destSb = new StringBuilder();
                        for (String destName : nameList) {
                            if(destName==null){
                                continue;
                            }
                            if(destSb.length()+destName.length()+1<maxDestNamelength){
                                destSb.append(",").append(destName);
                            }else{
                                break;
                            }
                        }
                        if(destSb.length()>0){
                            destSb.append(",");
                        }
                        orderItem.setDestName(destSb.toString());
                        //出境指定品类和指定目的地设置预审标识
                        //香港迪士尼（酒店）,指定目的地id
                        Long[] HOTEL_DESTID ={11304709L,10028598L};
                        //长滩岛、香港迪士尼（酒店）、毛里求斯、马尔代夫四个境外目的地Id
                        Long[] HOTELCOMB_DESTID ={3607L,11304709L,10028598L,3629L,3546L};
                        logger.info("content pretrialFlag start,distributorId="+order.getDistributorId()+" productType=" + suppGoods.getProdProduct().getProductType()+" destIds"+destIds);
                        if(CollectionUtils.isNotEmpty(destIds)
                                && PRODUCTTYPE.FOREIGNLINE.getCode().equals(suppGoods.getProdProduct().getProductType())
                                && Constant.DIST_BRANCH_SELL == order.getDistributorId()){
                            logger.info("content pretrialFlag start,producTourtType="+suppGoods.getProdProduct().getProducTourtType()+" categoryId=" + suppGoods.getProdProduct().getBizCategoryId());
                            for (Long destId : destIds) {
                                if (BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(suppGoods.getProdProduct().getBizCategoryId())
                                        && !new Long(3607L).equals(destId) && "ONEDAYTOUR".equals(suppGoods.getProdProduct().getProducTourtType())) {
                                    logger.info("category_route_local putContent pretrialFlag");
                                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.pretrialFlag.name(),"Y");
                                    break;
                                }  else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(suppGoods.getProdProduct().getBizCategoryId())
                                        && !ArrayUtils.contains(HOTEL_DESTID, destId)){
                                    logger.info("category_hotel putContent pretrialFlag");
                                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.pretrialFlag.name(),"Y");
                                    break;
                                } else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(suppGoods.getProdProduct().getBizCategoryId())
                                        && !ArrayUtils.contains(HOTELCOMB_DESTID, destId)){
                                    logger.info("category_route_hotelcomb putContent pretrialFlag");
                                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.pretrialFlag.name(),"Y");
                                    break;
                                }
                            }
                        }
                        logger.info("content pretrialFlag end");
                    }

                }
            }catch (Exception e){
                logger.error("下单冗余产品类型和目的地信息出错",e);
            }

            // 设置子单“公司主体”--SuppGoodsId--
//          if (StringUtils.isNotBlank(suppGoods.getCompanyType())) {
//              orderItem.setCompanyType(suppGoods.getCompanyType());
//          }
            
           //下单时，如果供应商合同审核通过，子订单合同主体取“供应商结算策略”中的“我方结算主体”，否则取商品中的合同主体 20161124
            if(suppGoods.getContractId()!=null){
                Long contractId = suppGoods.getContractId();
                ResultHandleT<SuppContract> suppContract = suppContractClientService.findSuppContractByContractId(contractId);
                if(SuppContract.CONTRACT_AUDIT.PASS.name().equals(suppContract.getReturnContent().getContractStatus())){
                    SuppSettleRule suppSettleRule = suppSettleRuleClientService.findSuppSettleRuleByContractId(contractId);
                    if(suppSettleRule!=null){
                        if(SuppSettleRule.LVACC_SUBJECT.DEFAULT.name().equals(suppSettleRule.getLvAccSubject())){
                            orderItem.setCompanyType(ProdProduct.COMPANY_TYPE_DIC.JOYU.name());
                            
                        }else if (SuppSettleRule.LVACC_SUBJECT.LVMAMA.name().equals(suppSettleRule.getLvAccSubject())){
                            orderItem.setCompanyType(ProdProduct.COMPANY_TYPE_DIC.GUOLV.name());
                            
                        }else if(SuppSettleRule.LVACC_SUBJECT.LVMAMAXINGLV.name().equals(suppSettleRule.getLvAccSubject())){
                            orderItem.setCompanyType(ProdProduct.COMPANY_TYPE_DIC.XINGLV.name());
                        }
                    }
                }else if(StringUtils.isNotBlank(suppGoods.getCompanyType())){
                    orderItem.setCompanyType(suppGoods.getCompanyType());
                }
            }else if(StringUtils.isNotBlank(suppGoods.getCompanyType())){
                orderItem.setCompanyType(suppGoods.getCompanyType());
            }

            //如果订单是自动打包交通的产品的订单，放过可售性校验(因为自动打包交通的产品，打包的交通商品系统中没有)
            Long productId = null;
            try {
                productId = order.getBuyInfo().getProductId();
            } catch (Exception e) {
                logger.error("Error get product id for goods " + suppGoods.getSuppGoodsId());
            }
            logger.info("Now begin to check whether goods " + suppGoods.getSuppGoodsId() + " is available to sale, category id is " + suppGoods.getCategoryId() + ", product id is " + productId);
            if(orderBookServiceDataUtil.isAutoPackTrafficProduct(productId) && orderBookServiceDataUtil.isTrafficGoods(suppGoods.getCategoryId())){
                logger.info("Product " + productId + " is auto package product, and goods category is traffic , will not check");
            } else {
                logger.info("Product " + productId + " is not auto package product, will check manually");
                checkSaleAble(suppGoods);
            }
            logger.info("Check whether goods " + suppGoods.getSuppGoodsId() + " is available to sale is completed");
            orderItem.setItem(item);
            initItemDetail(order, orderItem, suppGoods);

            if(item.getGoodType()!=null &&  "localRoute".equals(item.getGoodType())){//orderitem标识是否为关联销售商品
                orderItem.putContent("relatedMarketingFlag","localRoute");
            }

            if(item.getTaobaoETicket()!=null && item.getTaobaoETicket().intValue()==1){ //如果是淘宝分销的电子票   ----2015-05-26 李兵
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.taobaoEticket.name(),1);
            }
            
            //门票是否全部游玩人
            if(suppGoods.getSuppGoodsId() != null){ 
                boolean travellerNum = false;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("objectId", suppGoods.getSuppGoodsId());
                LOGTSET.info(params.toString());
                List<ComOrderRequired> comOrderRequireds = comOrderRequiredClientService.findComOrderRequiredList(params);
                LOGTSET.info(GsonUtils.toJson(comOrderRequireds));
                if(comOrderRequireds !=null && !comOrderRequireds.isEmpty()){
                    LOGTSET.info(comOrderRequireds.size()+"");
                    ComOrderRequired comOrderRequired = comOrderRequireds.get(0);
                    if (BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.TRAV_NUM_ALL.name().equalsIgnoreCase(comOrderRequired.getTravNumType())) {
                        travellerNum = true;
                    }
                }               
                
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.travAllFlag.name(), travellerNum);
            }
            
            //xiaorui add 把商品上的 特殊門票類型 加到order item上
            if(StringUtils.isNotBlank(suppGoods.getSpecialTicketType())){
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name(), suppGoods.getSpecialTicketType());
            }


            if(StringUtils.isNotBlank(suppGoods.getRegion())){
                // 把演出票 的区域包攒到子订单content上
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.showTicketRegion.name(), suppGoods.getRegion());
            }
            if(StringUtils.isNotBlank(suppGoods.getEventStartTime())){
                // 把演出票 的场次开始时间信息 包攒到子订单content上
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.showTicketEventStartTime.name(), suppGoods.getEventStartTime());
            }

            if(StringUtils.isNotBlank(suppGoods.getEventEndTime())){
                // 把演出票 的场次结束时间信息 包攒到子订单content上
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.showTicketEventEndTime.name(), suppGoods.getEventEndTime());
            }

            if (StringUtils.isNotEmpty(item.getDisneyItemOrderInfo())) {
                // xiaorui add 保存上海迪士尼 剧场票  區域座次排號 等信息。
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.DisneyItemInfo.name(), item.getDisneyItemOrderInfo());
            }
            //wp add 20160812 把商品上的通知类型 加到order item上
            if(StringUtils.isNotBlank(suppGoods.getNoticeType())){
                orderItem.setNotifyType(suppGoods.getNoticeType());
            }
            
            if(StringUtils.isNotBlank(suppGoods.getAllDayFlag())){
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.allDayFlag.name(), suppGoods.getAllDayFlag());
            }
            if(StringUtils.isNotBlank(suppGoods.getTrafficFlag())){
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.trafficFlag.name(), suppGoods.getTrafficFlag());
            }
            if(StringUtils.isNotBlank(suppGoods.getMealFlag())){
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.mealFlag.name(), suppGoods.getMealFlag());
            }

            //马戏票场次信息赋值
            if(item.getCircusActInfo() != null && StringUtils.isNotBlank(item.getCircusActInfo().getCircusActId())) {
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.circusActId.name(), item.getCircusActInfo().getCircusActId());
                if(StringUtils.isNotBlank(item.getCircusActInfo().getCircusActStartTime()) && !"null".equalsIgnoreCase(item.getCircusActInfo().getCircusActStartTime())) {
                    orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.circusActStartTime.name(), item.getCircusActInfo().getCircusActStartTime());
                }
                if(StringUtils.isNotBlank(item.getCircusActInfo().getCircusActEndTime()) && !"null".equalsIgnoreCase(item.getCircusActInfo().getCircusActEndTime())) {
                    orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.circusActEndtime.name(), item.getCircusActInfo().getCircusActEndTime());
                }
            }

            //门票天牛计划
            if(StringUtils.isNotBlank(suppGoods.getTianNiuFlag())){
                orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.tianNiuFlag.name(), suppGoods.getTianNiuFlag());
            }
            
            if(order.getBuyInfo().hasAdditionalTravel()&&order.isValidInitOrder()){
                List<BuyInfo.ItemPersonRelation> personRelation = personInitBussiness.getPersonRelation(order.getBuyInfo(), orderItem, orderPack);
                orderItem.setOrdItemPersonRelationList(personInitBussiness.initPersonRelation(order,personRelation));
                Log.info("OrderBookServiceImpl.initItem");
                //是否邮轮组合产品
                Long categoryId = order.getCategoryId();
                if(categoryId == null){
                    if(orderPack != null){
                        categoryId = orderPack.getCategoryId();
                    }
                }
                if(categoryId != null && categoryId.longValue() == 8){
                    //是否邮轮产品
                    Long itemCategoryId = orderItem.getCategoryId();
                    if(itemCategoryId != null && itemCategoryId.longValue() == 2){
                        //间数
                        Long quantity = orderItem.getQuantity();
                        //人数
                        Integer num = null;
                        List<OrdItemPersonRelation> itemPersonRelations = orderItem.getOrdItemPersonRelationList();
                        if(itemPersonRelations != null && itemPersonRelations.size() > 0){
                            if(itemPersonRelations.get(0) != null && itemPersonRelations.get(0).getRoomNo() == null){
                                num = itemPersonRelations.size();
                            }
                        }
                        List<Integer> result = new ArrayList<Integer>();
                        if (num != null && quantity != null) {
                            Double roomNm = Double.valueOf(quantity);
                            Integer total = num;
                            while (total > 0 && roomNm > 0) {
                                int personNum = (int) Math.ceil(total / roomNm);
                                result.add(personNum);
                                total -= personNum;
                                roomNm--;
                            }
                        }
                        if(result.size() > 0){
                            int index = 0;
                            for(int i = 0; i < result.size(); i++){
                                int k = result.get(i);
                                for(int p = 0; p < k; p++){
                                    if(index < num){
                                         OrdItemPersonRelation itemPersonRelation = itemPersonRelations.get(index);
                                         itemPersonRelation.setRoomNo(Long.valueOf(i+1));
                                         index ++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //交通+X退改规则
            if(orderLvfTimePriceServiceImpl.isAutoPackProductOrder(order.getBuyInfo()) && orderBookServiceDataUtil.isTrafficGoods(suppGoods.getCategoryId())){
                orderItem.setCancelStrategy(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.name());
//                FlightNoVo flightNoVo = null;
//                if(CollectionUtils.isNotEmpty(item.getAdditionalFlightNoVoList())){
//                    flightNoVo= item.getAdditionalFlightNoVoList().get(0);
//                }else if(item.getFlightNoVo()!=null){
//                    flightNoVo = item.getFlightNoVo();
//                }
//                if(flightNoVo!=null){
//                    orderItem.setCancelStrategy(flightNoVo.getCancelStrategy());
//                }
                logger.info("***交通+X 机票人工退改***");
            }

            //如果是自动打包产品，而且本商品是交通时，放过校验，并在content大字段中加入自动打包交通标识
            if(orderBookServiceDataUtil.isAutoPackTrafficProduct(productId) && orderBookServiceDataUtil.isTrafficGoods(suppGoods.getCategoryId())){
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.autoPackTrafficFlag.name(), Constants.Y_FLAG);
                return orderItem;
            }

            //如果是“其他机票  && 对接”
            if(OrderLvfTimePriceServiceImpl.isLvfItemByCatetory(orderItem)){
                return orderItem;
            }
            /***线路后台下单在orderItem表中增加意向单来源标记字段   张晓军   2015-04-14****/
            if(null != order.getBuyInfo().getIntentionOrderFlag())
                orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.intention_order_flag.name(),order.getBuyInfo().getIntentionOrderFlag());
            /*****************************************************************************/

            /** 开始资源预控买断价格  **/
            List<ResPreControlTimePriceVO> resPriceList = null;
            Long goodsId = suppGoods.getSuppGoodsId();
            Date visitDate = orderItem.getVisitTime();
            //通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
            GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
            //如果能找到该有效预控的资源
            boolean hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
            if(hasControled ){
                // --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
                resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
                if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
                    hasControled = false;
                }else{
                    logger.info("***资源预控***");
                    logger.info( orderItem.getSuppGoodsId() + "存在预控资源,需要价格计算");
                }
            }
            /** end **/

            //是否需要重新计算价格
            if(order.isValidInitOrder() || hasControled){
                //时间价格表处理
                OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
                //时间价格当中处理门票的特殊性的时间价格问题
//                logger.info("orderTimePriceService.validate开始");
                logger.info("orderTimePriceService.validate开始"+orderItem.getOrderItemId() + "orderTimePriceService真实类名--->" + orderTimePriceService.getClass().getCanonicalName());
                ResultHandle handle = orderTimePriceService.validate(suppGoods, item, orderItem, order);
                logger.info("orderTimePriceService.validate返回");
                if(handle.isFail()){
                    throwIllegalException(handle.getMsg());
                }
                //外币项目与供应商退改规则，币种 处理
                currencyHandle(suppGoods, orderItem, order);
            }
            logger.info("---initItem return orderItem----");


            return orderItem;
        }else{
            logger.info("---initItem return null----");
            return null;
        }
    }

    private void currencyHandle(SuppGoods suppGoods, OrdOrderItemDTO orderItem, OrdOrderDTO order) {
        try {
            logger.info("外币项目与供应商退改规则和币种处理,suppGoods=" + GsonUtils.toJson(suppGoods)
                    + "\norderItem.getOrdOrderItemExtendDTO()=" + GsonUtils.toJson(orderItem.getOrdOrderItemExtendDTO()));

            //外币项目存放币种 开始
            if (orderItem.getOrdOrderItemExtendDTO() != null) {
                orderItem.getOrdOrderItemExtendDTO().setCurrencyCode(suppGoods.getCurrencyType());//币种
            }
            if (StringUtils.isEmpty(orderItem.getRefundRules())) {
                return;
            }

            String json = orderItem.getRefundRules();
            logger.info("退改规则" + GsonUtils.toJson(json));
            com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(json);
            for (int i = 0; i < jsonArray.size(); i++) {//对币种名字的容错处理
                com.alibaba.fastjson.JSONObject tempJSONObject = jsonArray.getJSONObject(i);
                if (StringUtils.isEmpty(suppGoods.getCurrencyType())
                        || suppGoods.getCurrencyType().equals(SuppGoods.CURRENCYTYPE.getCnName(suppGoods.getCurrencyType()))) {
                    tempJSONObject.put("currencyName", "元");
                } else {
                    tempJSONObject.put("currencyName", SuppGoods.CURRENCYTYPE.getCnName(suppGoods.getCurrencyType()));
                }
            }
            orderItem.setRefundRules(jsonArray.toString());
//            //退改规则
//            List<SuppGoodsRefund> suppGoodsRefundList = suppGoods.getGoodsReFundList();
//            if (CollectionUtils.isEmpty(suppGoodsRefundList)) {
//                return;
//            }
//
//            for (SuppGoodsRefund suppGoodsRefund : suppGoodsRefundList) {
//                suppGoodsRefund.setCurrencyName(SuppGoods.CURRENCYTYPE.getCnName(suppGoods.getCurrencyType()));
//                String str = JSONUtil.bean2Json(suppGoodsRefund);
//                com.alibaba.fastjson.JSONObject tempJson = com.alibaba.fastjson.JSONObject.parseObject(str);
//                jsonArray.add(tempJson);
//            }
//
//            orderItem.setRefundRules(jsonArray.toString());
            //外币项目存放币种 结束
        } catch (Exception e) {
            logger.error("商品id=" + suppGoods.getSuppGoodsId() + "外币结算项目与供应商退改规则和币种处理发生异常" + e, e);
        }
    }

    /**
     * 对于国内景酒、机酒、跟团游产品，检查酒店的入住日期、包线路的出游日期是否正确。 如果酒店的入住日期不正确或线路的出游日期不正确，抛出OrderException。
     * @param order OrdOrderDTO对象
     * @param item BuyInfo.Item对象
     * @param orderItem OrdOrderItemDTO对象
     * @param suppGoods SuppGoods对象
     * @param prodProduct ProdProduct对象
     * @throws OrderException 如果酒店的入住日期不正确，抛出OrderException
     */
    private void checkPackageGroupStartDate(final OrdOrderDTO order, BuyInfo.Item item, 
            OrdOrderItemDTO orderItem, SuppGoods suppGoods, ProdProduct prodProduct) throws OrderException {
        if(prodProduct != null && order != null && order.getDistributorId() == Constant.DIST_FRONT_END){
            String productType = prodProduct.getProductType();              
            Long categoryId = null;
            if (prodProduct.getBizCategoryId() != null) {
                categoryId = prodProduct.getBizCategoryId();
            }
            if (categoryId == null && prodProduct.getBizCategory() != null) {
                categoryId = prodProduct.getBizCategory().getCategoryId();
            }
            if (categoryId == null && prodProduct.getCategoryId() != null) {
                categoryId = prodProduct.getCategoryId();
            }
            long subCategoryId = prodProduct.getSubCategoryId() == null? 0L : prodProduct.getSubCategoryId();
            Long detailId = item.getDetailId();
            //检查产品类型是否是国内景酒、机酒、跟团游产品
            if(detailId != null && order.getVisitTime() != null && 
            		(StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNERLONGLINE.name(), productType)
                    || StringUtils.equalsIgnoreCase(ProdProduct.PRODUCTTYPE.INNER_BORDER_LINE.name(), productType))){
            	if(categoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId() ||
            			(categoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() 
	                    && (subCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId()
	                        || subCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId()))) {
                	long goodsCategoryId = suppGoods.getCategoryId();
            		//检查酒店入住日期
            		if(goodsCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()){
	                    ResultHandleT<Boolean> flagHandler =prodPackageGroupClientService.checkHotelVisitTime(detailId, orderItem.getVisitTime(), order.getVisitTime());
	                    if(flagHandler.getReturnContent() != null && !flagHandler.getReturnContent()) {
	                        StringBuffer buf = new StringBuffer("Hotel visitTime is wrong. goodId=");
	                        buf.append(suppGoods.getSuppGoodsId());
	                        buf.append(",visitTime=").append(item.getVisitTime());
	                        logger.error(buf.toString());
	                        throwIllegalException("酒店的入住时间不正确。");
	                    }
	                } else if(goodsCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId()
	                		|| goodsCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId()
	                		|| goodsCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId()
	                		|| goodsCategoryId == BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId()){
	                	//检查线路的出游日期
	                    ResultHandleT<Boolean> flagHandler =prodPackageGroupClientService.checkGroupLineVisitTime(detailId, orderItem.getVisitTime(), order.getVisitTime());
	                    if(flagHandler.getReturnContent() != null && !flagHandler.getReturnContent()) {
	                        StringBuffer buf = new StringBuffer("Line visitTime is wrong. goodId=");
	                        buf.append(suppGoods.getSuppGoodsId());
	                        buf.append(",visitTime=").append(item.getVisitTime());
	                        logger.error(buf.toString());
	                        throwIllegalException("线路商品的出游日期不正确。");
	                    }
	                }
	            }
            }
        }
    }

    /**
     * 计算目的地BU子订单流程 (酒店或酒店套餐)
     * <br/>PS:add by xiaoyulin
     * @param order
     */
    private void calcDestBuSubWorkflow(OrdOrder order) {
        if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()) {// 酒店子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "o2o_hotel");
                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
                }else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem.getCategoryId()
                        ||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem.getCategoryId()) {// 酒店套餐子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "o2o_hotelcomb");
                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
                }
            }
        }else if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()) {// 酒店子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotel_new");
                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
                }else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem.getCategoryId()
                        ||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem.getCategoryId()) {// 酒店套餐子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb_new");
                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());
                }
            }
        }else if (OrdOrderUtils.isDestBuFrontOrder(order)) {
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()) {// 酒店子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotel");
                }else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem.getCategoryId()
                        ||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem.getCategoryId()) {// 酒店套餐子单
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb");
                }
            }
        }
    }

    private void initOrderItemBase(final OrdOrderDTO order,
            OrdOrderItem orderItem, SuppGoods suppGoods) {
        orderItem.setSuppGoods(suppGoods);
        orderItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
        orderItem.setBranchId(suppGoods.getProdProductBranch().getProductBranchId());
        //产品经理ID
        if(suppGoods.getManagerId() == null){
            logger.error("supp_goods_id:"+suppGoods.getSuppGoodsId()+" have no manager_id");
        }else{
            orderItem.setManagerId(suppGoods.getManagerId());
        }
        orderItem.setBuCode(suppGoods.getBu());//赋予商品真实BU，改字段值根据业务逻辑判断是否改变
        orderItem.setRealBuType(suppGoods.getBu());//赋予商品真实BU
        orderItem.setAttributionId(suppGoods.getAttributionId());//赋予商品归属地
        //凭证确认状态
        orderItem.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.UNCONFIRMED.name());
        
        //取消凭证确认
        orderItem.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());

        //现付担保类型
        orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());

        //扣款类型
        orderItem.setDeductType(SuppGoodsTimePrice.DEDUCTTYPE.NONE.name());

        orderItem.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());

        orderItem.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());

        //调用对接平台的状态
        orderItem.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.INITIAL.name());

        //担保总量
        orderItem.setDeductAmount(0L);
        //关联销售已经有主订单设置
        if(order.getBuyInfo()==null || !"Y".equalsIgnoreCase(order.getBuyInfo().getHasMainItemFlag()) || !"true".equalsIgnoreCase(orderItem.getItem().getMainItem())){
            orderItem.setMainItem("false");
        }else{
            orderItem.setMainItem("true");
        }
        //传真备注，设置在订单子项中
        String faxMemo = order.getBuyInfo().getFaxMemo();
        if (faxMemo != null && !"".equals(faxMemo)) {
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxMemo);
        }
        //合同ID
        orderItem.setContractId(suppGoods.getContractId());

        //产品ID
        orderItem.setProductId(suppGoods.getProductId());

        //供应商ID
        orderItem.setSupplierId(suppGoods.getSupplierId());

        //产品名称
        orderItem.setProductName(suppGoods.getProdProduct().getProductName());

        //商品名称
        orderItem.setSuppGoodsName(suppGoods.getGoodsName());

        //供应商产品名称
        orderItem.setSuppProductName(suppGoods.getProdProduct().getSuppProductName());

        //商品英文名称
        orderItem.setGoodsEnglishName(suppGoods.getGoodsEnglishName());
        
        //履行状态
        orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());

        //结算状态
        orderItem.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name());

        // 结算对象
        String settleEntityCode = suppGoods.getSettlementEntityCode();
        if(StringUtils.isNotEmpty(settleEntityCode)){
            ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(settleEntityCode);
            if(resultHandleT.isSuccess()){
                orderItem.setSuppSettlementEntities(resultHandleT.getReturnContent());
            }
        }

        // 信息状态-未确认
        orderItem.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());

        //品类code
        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(), suppGoods.getProdProduct().getBizCategory().getCategoryCode());

        //添加子订单流程key
        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), suppGoods.getProdProduct().getBizCategory().getProcessKey().trim());
		

        //供应商标识
        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name(), suppGoods.getApiFlag());

        //期票时，存入期票截止日期 yyyy-MM-dd HH:mm:ss
        if("Y".equals(suppGoods.getAperiodicFlag())){
            try {
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.goodsEndTime.name(), DateUtil.getFormatDate(suppGoods.getSuppGoodsExp().getEndTime(), "yyyy-MM-dd HH:mm:ss"));
                SuppGoodsExp exp = suppGoods.getSuppGoodsExp();
                if(exp != null){
                    String expire = SuppGoodsExpTools.getAperiodicExpDesc(exp, order.getCreateTime());
                    if(StringUtil.isNotEmptyString(expire)){
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.goodsExpiryDate.name(), expire);
                    }
                    String inapplicableDate = StringUtil.isNotEmptyString(exp.getUnvalidDesc())? exp.getUnvalidDesc():null;
                    if(StringUtil.isNotEmptyString(inapplicableDate)){
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.goodsUnvalidDate.name(), inapplicableDate);
                    }
                }
            }catch(Exception ex){logger.error(ExceptionFormatUtil.getTrace(ex));}
        }

        order.putApiFlag(suppGoods.getSuppGoodsId(),"Y".equals(suppGoods.getApiFlag()));

        String branchName = suppGoods.getProdProductBranch().getBranchName();

        ResultHandleT<BizBranch> branch = branchClientService.findBranchById(orderItem.getSuppGoods().getProdProductBranch().getBranchId());
        if(branch==null||branch.hasNull()){
            throwNullException("规格不存在");
        }
        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchAttachFlag.name(), branch.getReturnContent().getAttachFlag());
        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchCode.name(), branch.getReturnContent().getBranchCode());

        //传真规则
        if(suppGoods.getFaxRuleId()!=null){
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_rule.name(), suppGoods.getFaxRuleId());
        }
		
		//邮件规则
		logger.info("supp_goods_id:"+suppGoods.getSuppGoodsId()+" getMailRuleId"+suppGoods.getMailRuleId());
		if(suppGoods.getMailRuleId()!=null && (ConfirmUtils.checkDistributorIds(Confirm_Enum.ids_distributor, order.getDistributorId())
                || ConfirmUtils.checkDistributorIdChannels(Confirm_Enum.ids_list_distributionChannel_mail, order.getDistributionChannel())
                /**增加分销渠道判断 by xiexun*/
                || Arrays.asList(Confirm_Enum.ids_list_distributionCode_mail_add).contains(order.getDistributorCode()) )){
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.mail_rule.name(), suppGoods.getMailRuleId());
		}
		//是否使用传真
		if(suppGoods.getMailFlag()!=null && (ConfirmUtils.checkDistributorIds(Confirm_Enum.ids_distributor, order.getDistributorId())
                || ConfirmUtils.checkDistributorIdChannels(Confirm_Enum.ids_list_distributionChannel_mail, order.getDistributionChannel())
                /**增加分销渠道判断 by xiexun*/
                || Arrays.asList(Confirm_Enum.ids_list_distributionCode_mail_add).contains(order.getDistributorCode()) )){
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.mail_flag.name(), suppGoods.getMailFlag());
		}
		
		/**增加分销渠道判断 by xiexun*/
		if(suppGoods.getMailFlag()!=null && Arrays.asList(Confirm_Enum.ids_list_distributionCode_mail_add).contains(order.getDistributorCode())){
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.distribution_mail_flag.name(), "Y");
		}
		
        //是否使用传真
        if(suppGoods.getFaxFlag()!=null){
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name(), suppGoods.getFaxFlag());
        }

        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), branchName.trim());
        Long vGoodsId = Constant.getVirtualSuppGoodsId_Flight();
        //如果是虚拟产品--机票
        if(orderItem.getSuppGoodsId()!=null && vGoodsId!=null && vGoodsId.longValue()==orderItem.getSuppGoodsId().longValue()){
            FlightNoVo flightNoVo = orderItem.getItem().getFlightNoVo();
            if(flightNoVo!=null) {
                try{
                    if(flightNoVo.getSeatName()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), flightNoVo.getSeatName());
                    if(flightNoVo.getFlightNo()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.flightNo.name(), flightNoVo.getFlightNo());
                    if(flightNoVo.getPlaneCode()!=null)
                        orderItem.setSuppGoodsName(flightNoVo.getPlaneCode());  //虚拟商品借用 航班编号
                    if(flightNoVo.getGoTime()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.departureTime.name(), DateFormatUtils.format(flightNoVo.getGoTime(), "yyyy-MM-dd HH:mm"));
                    if(flightNoVo.getFromCityName()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.departureCity.name(), flightNoVo.getFromCityName());
                    if(flightNoVo.getToCityName()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name(), flightNoVo.getToCityName());
                    if(flightNoVo.getFromAirPort()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fromAirport.name(), flightNoVo.getFromAirPort());
                    if(flightNoVo.getToAirPort()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.toAirport.name(), flightNoVo.getToAirPort());
                    if(flightNoVo.getStartTerminal()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.startTerminal.name(), flightNoVo.getStartTerminal());
                    if(flightNoVo.getArriveTerminal()!=null)
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name(), flightNoVo.getArriveTerminal());
                    if(flightNoVo.getSeatCode()!=null){
                        orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.seatClassCode.name(), flightNoVo.getSeatCode());
                    }

                    //自动打包交通的产品，需要把产品名称重新设定为“起飞城市-到达城市”(因为自动打包交通的产品，机票suppGoods固定是一个虚拟机票的产品，产品名称是“虚拟机票”，显示到页面上不好看)
                    if (orderBookServiceDataUtil.isAutoPackTrafficProduct(order.getBuyInfo().getProductId())) {
                        String planeProductName = flightNoVo.getFromCityName() + "-" + flightNoVo.getToCityName();
                        orderItem.setProductName(planeProductName);
                    }
                }catch (Exception ex){
                    logger.error(ExceptionFormatUtil.getTrace(ex));
                }
            }
        }

        //针对非"交通+x"的品类，如果含有品类"机票"或者"其它机票"，则补充机票信息到content
        logger.info("==============================Begin to fill flight info=============================================================");
        try {
            logger.info("Now copy flight info for product[" + order.getBuyInfo().getProductId() + "],supply goods[" + suppGoods.getSuppGoodsId() + "]");
        } catch (Exception e) {
                logger.error("Error occurs while converting to Json");
        logger.error(ExceptionFormatUtil.getTrace(e));
        }
        if(orderBookServiceDataUtil.isNotAeroHotel(order) && orderBookServiceDataUtil.isAeroPlaneOrOther(orderItem)) {
            try {
                orderBookServiceDataUtil.fillOrderItemFlightsData(orderItem);
            } catch (Exception e) {
                logger.error("Error occurs while copy flight info for goods [" + suppGoods.getSuppGoodsId() + "]", e);
            }
        }
        logger.info("==============================fill flight info finished=============================================================");

        orderItem.setNeedResourceConfirm("false");
        if(order.isCreateFlag()){
            setFromTo(orderItem);
        }
        //wif其他品类设置产品类型
        if((BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(suppGoods.getCategoryId())
           ||BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(suppGoods.getCategoryId())
           )&&suppGoods.getProdProduct()!=null){
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.productType.name(),suppGoods.getProdProduct().getProductType());
        }
        //冗余下单时间
        orderItem.setCreateTime(order.getCreateTime());
        orderItem.setOrderUpdateTime(order.getOrderUpdateTime());
        //冗余产品打包方式
        ResultHandleT<ProdProduct> resultHandle = productHotelAdapterClientService.findProductById(orderItem.getProductId());
        if(resultHandle.isSuccess()){
            ProdProduct product = resultHandle.getReturnContent();
            if(StringUtil.isNotEmptyString(product.getPackageType())){
                orderItem.setPackageType(product.getPackageType());
            }
        }
    }

    private void setFromTo(OrdOrderItem orderItem){
        ProdProduct product = orderItem.getSuppGoods().getProdProduct();
        List<ProdDestRe> list = product.getProdDestReList();
        String flag="N";
        BizDest bizDest = new BizDest();
        for(ProdDestRe pdr:list){
            bizDest.setParentId(pdr.getDestId());
            ResultHandleT<BizDest> handle = destClientService.findParentDest(bizDest);
            if(!handle.hasNull()){
                if("Y".equalsIgnoreCase(handle.getReturnContent().getForeighFlag())){
                    flag = "Y";
                    break;
                }
            }
        }
        //目的地
        orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name(), flag);
        if(product.getBizDistrictId()!=null){
            ResultHandleT<BizDistrict> handle = districtClientService.findDistrictById(product.getBizDistrictId());
            if(!handle.hasNull()){
                orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.is_from_foreign.name(), handle.getReturnContent().getForeighFlag());
            }
        }
    }


    private void fillOrderPaymentTarget(final OrdOrderDTO order,SuppGoods suppGoods){
        if(StringUtils.isEmpty(suppGoods.getPayTarget())){
            if (logger.isDebugEnabled()) {
                logger.debug("fillOrderPaymentTarget(OrdOrderDTO, SuppGoods) - suppGoods.getPayTarget==null"); //$NON-NLS-1$
            }
            throwNullException("支付对象为空");
        }

        if(StringUtils.isEmpty(order.getPaymentTarget())){
            order.setPaymentTarget(suppGoods.getPayTarget());
        }else if(!StringUtils.equalsIgnoreCase(order.getPaymentTarget(), suppGoods.getPayTarget())){
            if (logger.isDebugEnabled()) {
                logger.debug("fillOrderPaymentTarget(OrdOrderDTO, SuppGoods) - order.getPaymentTarget!=suppGoods.getPayTarget"); //$NON-NLS-1$
            }

            throwIllegalException("支付对象不一置，不可以成单");
        }
    }

    private void setOrderResourceStatus(final OrdOrderItem orderItem, final OrdOrderDTO order){
        String status = getOrderResourceStatus(order.getResourceStatus(), orderItem.getResourceStatus());
        if(StringUtils.isNotEmpty(status)){
            order.setResourceStatus(status);
        }
    }

    private String getOrderResourceStatus(final String resourceStatus, final String  newResourceStatus){
        if(StringUtils.isEmpty(resourceStatus)){
            return newResourceStatus;
        }
        OrderEnum.RESOURCE_STATUS newStatus = OrderEnum.RESOURCE_STATUS.valueOf(newResourceStatus);
        OrderEnum.RESOURCE_STATUS orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(resourceStatus);
        int newPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY, newStatus);
        int oldPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY, orderResourceStatus);
        if(newPos<oldPos){
            return newStatus.name();
        }else{
            return orderResourceStatus.name();
        }
    }

    private static OrderEnum.RESOURCE_STATUS[] RESOURCE_STATUS_ARRAY={OrderEnum.RESOURCE_STATUS.LOCK,OrderEnum.RESOURCE_STATUS.UNVERIFIED,OrderEnum.RESOURCE_STATUS.AMPLE};

    @Override
    public ResultHandle saveOrderPerson(Long orderId, BuyInfo buyInfo) {
        ResultHandle result = new ResultHandle();
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
        if(order==null){
            result.setMsg("订单不存在");
            return result;
        }
        if(order.isCancel()){
            result.setMsg("订单已经取消");
            return result;
        }
        List<OrdPerson> ordPersonList = ordPersonService.getOrderPersonListWithAddress(orderId, OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
        if(!ordPersonList.isEmpty()){
            throwIllegalException("已经添加了游客不能再操作");
        }
        OrdOrderDTO orderDTO = new OrdOrderDTO(buyInfo);
        ordPersonList = new ArrayList<OrdPerson>();
        personInitBussiness.initAdditionalTravel(orderDTO, ordPersonList);
        if(ordPersonList.isEmpty()){
            result.setMsg("游客相关人员信息为空");
            return result;
        }
        orderDTO.setOrdPersonList(ordPersonList);

        List<OrdOrderItem> orderItemList = orderUpdateService.queryOrderItemByOrderId(orderId);
        for(OrdOrderItem orderItem:orderItemList){
            List<BuyInfo.ItemPersonRelation> personRelation = personInitBussiness.getPersonRelation(buyInfo, orderItem, null);
            orderItem.setOrdItemPersonRelationList(personInitBussiness.initPersonRelation(orderDTO,personRelation));
            orderDTO.addOrderItem(orderItem);
        }
        try {
            calcBlackList(orderDTO);
        } catch (Exception e) {
            logger.error("{}", e);
            result.setMsg(e);
            return result;
        }

        orderSaveService.savePersonAndRelation(orderId,orderDTO);

        //保存预订人和联系人相关的订单查询信息
        if(CollectionUtils.isNotEmpty(ordPersonList)) {
            OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();

            for(OrdPerson person:ordPersonList){
                if(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(person.getPersonType())) {
                    orderQueryInfo.setOrderId(orderId);
                    orderQueryInfo.setBookerName(person.getFullName());
                    orderQueryInfo.setBookerMobile(person.getMobile());
                }
                if(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(person.getPersonType())) {
                    orderQueryInfo.setOrderId(orderId);
                    orderQueryInfo.setContactName(person.getFullName());
                    orderQueryInfo.setContactMobile(person.getMobile());
                    orderQueryInfo.setContactPhone(person.getPhone());
                    orderQueryInfo.setContactEmail(person.getEmail());
                }
            }
//          if(orderQueryInfo.getOrderId() != null) {
//              orderQueryInfoService.updateQueryInfoByOrderId(orderQueryInfo);
//          }
        }
        return result;
    }

    /**
     * 计算供应商结算价促销
     * @param order
     * @return
     */
    private long countSupplierPromotionAmount(OrdOrderDTO order){
        long totalAmount =0;
        try{
            if(org.apache.commons.collections.CollectionUtils.isNotEmpty(order.getOrderItemList())){
                for(OrdOrderItem orderItem:order.getOrderItemList()){
                        List<PromPromotion> list = promotionBussiness.makeSuppGoodsPromotion(order,
                                orderItem,PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name());
                        if(!list.isEmpty()){
                            List<OrdPromotion> ordPromList = new ArrayList<OrdPromotion>();
                            for(PromPromotion promotion:list){
                                OrdPromotion op = new OrdPromotion();
                                op.setCode(promotion.getCode());
                                op.setPromPromotionId(promotion.getPromPromotionId());
                                op.setPriceType(promotion.getPriceType());
                                op.setPromTitle(promotion.getTitle());
                                op.setTarget(orderItem);
                                op.setObjectType(OrdPromotion.ObjectType.ORDER_ITEM.name());
                                op.setFavorableAmount(promotion.getDiscountAmount());
                                op.setOrderItemId(orderItem.getOrderItemId());
                                op.setPromotion(promotion);
                                ordPromList.add(op);
                                totalAmount+=promotion.getDiscountAmount();

                            }
                            order.addOrdPromotions("supplier_"+orderItem.getSuppGoodsId(), ordPromList);
                        }
                }
            }
        }catch (Exception e) {
            logger.error(ExceptionFormatUtil.getTrace(e));
        }
        return totalAmount;
    }

    @Autowired
    private IOrdPersonService ordPersonService;

    @Override
    public void afterPropertiesSet() throws Exception {
        config.setAllowNonStringKeys(true);//因为additionMap的key是enum
    }

    /**
     * 扣减酒店赠品库存
     * @param order
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    private void deductFreebieStock(OrdOrderDTO order, ResultHandleT<OrdOrder> result){
        logger.info("-------------开始进入酒店赠品库存-------------------"+order.getOrderId());
        Long quantity = 0L;
        boolean canConsume = true;
        List<OrdItemFreebiesRelation> ordItemfreebies = new ArrayList<OrdItemFreebiesRelation>();
        List<HotelFreebiePO> freebieList = null;
        boolean isNewHotel = false;//是否走新酒店路由
        Long productId =null;
        productId = order.getProductId();
        logger.debug("deductFreebieStock first set productId="+productId);
        if(null == productId){
            logger.debug("deductFreebieStock second set productId");
            productId = order.getFilterMainOrderItem().getProductId();
        }
        isNewHotel = destHotelAdapterUtils.checkHotelRouteEnableByProductId(productId);
        logger.info("deductFreebieStock checkHotel is "+isNewHotel+" productId="+productId);
        //单酒店的子订单应该是1个
        for (OrdOrderItem orderItem : order.getOrderItemList()) {
            if("true".equals(orderItem.getMainItem())){
                Long goodsId = orderItem.getSuppGoodsId();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("goodsId", goodsId);
                params.put("hasStock", "Y");
                params.put("startDate", order.getVisitTime());
                params.put("endDate", order.getEndTime());
                //根据商品ID查询对应的赠品列表
                if(isNewHotel){
                    freebieList = queryFreebieListByParamsToApi(params);
                }else{
                    freebieList = hotelFreebieClientService.queryFreebieListByParams(params);
                }           
                quantity = orderItem.getQuantity();
                if(quantity==0L){
                    quantity=1L;
                }
                OrdItemFreebiesRelation freebieRelation = null;
                //遍历酒店商品绑定的赠品
                for (HotelFreebiePO hotelFreebie : freebieList) {
                    if(null != hotelFreebie.getStockNum()){
                        if(hotelFreebie.getStockNum().intValue() ==-1 || hotelFreebie.getStockNum().intValue()>=1){
                             //设置快照
                             freebieRelation = new OrdItemFreebiesRelation();
                             freebieRelation.setOrdItemId(orderItem.getOrderItemId());
                             logger.info("-------------itemId-------------------"+freebieRelation.getOrdItemId());
                             if(null !=hotelFreebie.getConsume() ){
                                 freebieRelation.setConsumeNum(hotelFreebie.getConsume()*quantity);
                                 logger.info("-------------ConsumeNum-------------------"+freebieRelation.getConsumeNum()+"---quantity---"+quantity);
                             }else{
                                 logger.info("-------------消费数量为空-------------------"+order.getOrderId());

                             }
                             freebieRelation.setFreebieId(hotelFreebie.getFreebieId());
                             logger.info("-------------FreebieId-------------------"+freebieRelation.getFreebieId());
                             freebieRelation.setCreateTime(new Date());
                             freebieRelation.setOrderId(order.getOrderId());
                             freebieRelation.setUseNotice(hotelFreebie.getUseNotice());
                             freebieRelation.setFreebieDesc(hotelFreebie.getFreebieDesc());
                             freebieRelation.setFreebieName(hotelFreebie.getFreebieName());
                             ordItemfreebies.add(freebieRelation);
                        }
                    }
                }
            }
        }

        //更新赠品
        if(CollectionUtils.isNotEmpty(ordItemfreebies)){
            List<OrdItemFreebiesRelation> freebiesRelationList = new ArrayList<OrdItemFreebiesRelation>();
            logger.info("reday update freebie param is:"+GsonUtils.toJson(ordItemfreebies));
            for (OrdItemFreebiesRelation ordItemRelation : ordItemfreebies) {
                if(null != ordItemRelation.getConsumeNum()){
                    ordItemRelation.setConsumeNum(0-ordItemRelation.getConsumeNum());
                    long resultNum = 0;
                    if(isNewHotel){
                        try
                        {
                            RequestBody<HotelOrdItemFreebiesRelation> request = new RequestBody<HotelOrdItemFreebiesRelation>();
                            HotelOrdItemFreebiesRelation hotelOrdItemFreebiesRelation = new HotelOrdItemFreebiesRelation();
                            EnhanceBeanUtils.copyProperties(ordItemRelation, hotelOrdItemFreebiesRelation);
                            request.setT(hotelOrdItemFreebiesRelation);
                            request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
                            logger.info("reday hotelFreebieQueryApiRemote updateStockNum  param is:"+GsonUtils.toJson(hotelOrdItemFreebiesRelation));
                            ResponseBody<Long> response = hotelFreebieQueryApiRemote.updateStockNum(request);
                            if(StringUtils.isEmpty(response.getErrorMessage())){
                                resultNum = response.getT();
                            }
                        }catch(Exception e){
                            logger.info("hotelFreebieQueryApiRemote.updateStockNum error:"+e);
                            e.printStackTrace();
                        }
                    }else
                    {
                        resultNum = hotelFreebieClientService.updateStockNum(ordItemRelation);
                    }
                    if(resultNum>0){
                        logger.info("deductFreebieStock is successful !");
                        freebiesRelationList.add(ordItemRelation);
                    }                   
                }else{
                    canConsume = false;
                }
            }
            if(CollectionUtils.isNotEmpty(freebiesRelationList) && canConsume ){
                try
                {
                    //设置赠品的快照
                    ordItemFreebieService.batchInsertOrdItemFreebie(freebiesRelationList);
                } catch (Exception e) {
                    logger.error("批量添加报错"+e.getMessage());
                    for (OrdItemFreebiesRelation ordItemFreebiesRelation : freebiesRelationList) {
                        logger.info("-------------单个设置快照START-------------------");
                        int r = ordItemFreebieService.insert(ordItemFreebiesRelation);
                        logger.info("-------------单个设置快照END-------------------");
                    }
                }

            }else{
                logger.error("------消费数目为空，不批量更新赠品--------");
            }

        }
        logger.info("-------------结束酒店赠品库存-------------------");
    }
    
    public List<HotelFreebiePO> queryFreebieListByParamsToApi(Map<String, Object> param){
        logger.info("-------------queryFreebieListByParamsToApi start -------------------");
        List<HotelFreebiePO> listFreebie = new ArrayList<HotelFreebiePO>();
        try{
            RequestBody<Map<String, Object>> request = new RequestBody<Map<String,Object>>();
            request.setT(param);
            request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
            ResponseBody<ArrayList<HotelFreebieVo>> response = hotelFreebieQueryApiRemote.queryFreebieListByParams(request);
            if(StringUtils.isEmpty(response.getErrorMessage())){
                List<HotelFreebieVo> list = response.getT();
                if(CollectionUtils.isNotEmpty(list)){
                    logger.info("-------------queryFreebieListByParamsToApi is not empty -------------------");
                    HotelFreebiePO freebie = null;
                    for (HotelFreebieVo hotelFreebieVo : list) {
                        freebie = new HotelFreebiePO();
                        EnhanceBeanUtils.copyProperties(hotelFreebieVo, freebie);
                        listFreebie.add(freebie);
                    }
                }
            }
        }catch(Exception e){
            logger.error("hotelFreebieQueryApiRemote.queryFreebieListByParams exception"+e);
        }
        return listFreebie;
    }
    
    @Override
    public String chechOrderPayForOther(BuyInfo buyInfo) {
        // TODO Auto-generated method stub
        String erroFlag="";
        //根据用户NO获取奖金账户和现金账户信息

        if((buyInfo.getBonusAmountHidden()!=null&&buyInfo.getBonusAmountHidden().intValue()>0) ||(buyInfo.getCashAmountHidden()!=null&& buyInfo.getCashAmountHidden().intValue()>0)){
            VstCashAccountVO  vstCashAccountVO=  ordUserOrderServiceAdapter.queryMoneyAccountByUserId(buyInfo.getUserNo());
            Long bonusBalance=vstCashAccountVO.getNewBonusBalance();//获取奖金余额
            Long MaxPayMoney=vstCashAccountVO.getMaxPayMoney();//获取可用于支付的现金余额
            if(buyInfo.getBonusAmountHidden()!=null&&bonusBalance.intValue()<buyInfo.getBonusAmountHidden().intValue()){
                logger.error(buyInfo.getUserNo()+"您的账户奖金金额发生变化,请重新输入"+"账户奖金余额"+bonusBalance.intValue()+"该笔订单需要支付的金额"+buyInfo.getBonusAmountHidden().intValue());

                erroFlag="您的账户奖金金额发生变化,请重新输入";
                return erroFlag;
            }
            if(buyInfo.getCashAmountHidden()!=null&& MaxPayMoney.intValue()<buyInfo.getCashAmountHidden().intValue()){
                logger.error(buyInfo.getUserNo()+"您的账户奖金金额发生变化,请重新输入"+"账户存款余额"+MaxPayMoney.intValue()+"该笔订单需要存款的金额"+buyInfo.getCashAmountHidden().intValue());

                erroFlag="您的账户存款金额发生变化,请重新输入";
                return erroFlag;
            }

        }


        if (CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())) {
            List<UserCouponVO> userCouponVOList=orderService.getUserCouponVOList(buyInfo,true);
            List<UserCouponVO> listTmp=new ArrayList<UserCouponVO>();
            for(UserCouponVO c:userCouponVOList){
                if(StringUtil.isNotEmptyString(c.getValidInfo())){
                    erroFlag=c.getValidInfo();
                    return erroFlag;
                }

                for(UserCouponVO cc:buyInfo.getUserCouponVoList()){
                    if(cc.getCouponCode().equals(c.getCouponCode())){
                        listTmp.add(c);
                    }
                }
            }
            buyInfo.setUserCouponVoList(listTmp);
        }

        if(CollectionUtils.isNotEmpty(buyInfo.getGiftCardList())){
            Map<String, String> map =new HashMap<String, String>();
            for(CardInfo c:buyInfo.getGiftCardList()){
                String keystr = c.getCardNo()+"_lvmama";
                try {
                    map.put(c.getCardNo(), DESCoder.decrypt(c.getPassWd(),keystr));
                } catch (Exception e) {

                    logger.error("DES 解密失败");
                    erroFlag="礼品卡密码解密失败";
                    return erroFlag;
                }
            }
            List<CardInfo> listGifCardInfo=null;
                try {
                    listGifCardInfo =payPaymentServiceAdapter.getLvmamaStoredCardListByCardNo(map);
                } catch (Exception e) {
                    erroFlag="获取礼品卡信息失败";
                    return erroFlag;

                }
            if (CollectionUtils.isEmpty(listGifCardInfo) || (listGifCardInfo.size()!=buyInfo.getGiftCardList().size())) {
                erroFlag="礼品卡验证结果与选择礼品卡不匹配";
                return erroFlag;
            }
            for(CardInfo c:listGifCardInfo){
                if ("0".equals(c.getStatus())) {
                    erroFlag=c.getBakWord();
                    return erroFlag;
                }
            }

        }

        if(CollectionUtils.isNotEmpty(buyInfo.getStoreCardList())){
            List<String> listCardNo =new ArrayList<String>();
            for(CardInfo c:buyInfo.getStoreCardList()){
                listCardNo.add(c.getCardNo());
            }
            List<CardInfo> listtStoreCardInfo=null;
                try {
                    listtStoreCardInfo =payPaymentServiceAdapter.getStoredCardListByCardNo(listCardNo);
                } catch (Exception e) {
                    erroFlag="获取储值卡信息失败";
                    return erroFlag;

                }
            if (CollectionUtils.isEmpty(listtStoreCardInfo) || (listtStoreCardInfo.size()!=buyInfo.getStoreCardList().size())) {
                erroFlag="储值卡验证结果与选择礼品卡不匹配";
                return erroFlag;
            }
            for(CardInfo c:listtStoreCardInfo){
                if ("0".equals(c.getStatus())) {
                    erroFlag=c.getBakWord();
                    return erroFlag;
                }
            }

        }


        return erroFlag;
    }
    public void initOrdTicketPerformStatus(OrdOrderDTO order){
        if(order.getCategoryId()==11L||order.getCategoryId()==12L ||order.getCategoryId()==13L){
            order.setPerformStatus(OrderEnum.PERFORM_STATUS_TYPE.UNPERFORM.name());
//          List<OrdOrderItem> itemList=order.getOrderItemList();
//          for (OrdOrderItem item :itemList){
//              String faxFlag=item.getContentStringByKey("fax_flag");
//              if(StringUtils.isNotEmpty(faxFlag) && "Y".equals(faxFlag)){
//                  order.setPerformStatus(OrderEnum.PERFORM_STATUS_TYPE.NEED_CONFIRM.name());
//              }
//              break;
//          }
        }

    }

    /**
     * 计算目的地订单展示状态和结束时间
     * @param order
     */
    public void initDestBuViewStatusAndEndTime(OrdOrder order){
        Log.info("######start initDestBuViewStatusAndEndTime and orderId is"+order.getOrderId()+"categoryId is"+order.getCategoryId());
        boolean isDestBu = false;
        isDestBu = OrdOrderUtils.isAllDestBuFrontOrder(order);
        if(isDestBu){
            //设置订单展示状态
            if(SuppGoods.PAYTARGET.PREPAID.getCode().equals(order.getPaymentTarget())){
                order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.getCode());
            }else
            {
                if(null != order.getVisitTime()){
                    order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.getCode());
                }
            }
            Log.info("setDsetOrderViewStatus method result is "+order.getViewOrderStatus()+"paymentTarget="+order.getPaymentTarget());
            //设置订单结束时间
            calDestBuOrderEndTime(order);
        }

    }

    /**
     * 计算目的地BU订单的结束时间/离店时间
     * @param order
     */
    private void calDestBuOrderEndTime(OrdOrder order){
        logger.info("-----------START METHOD calDestBuOrderEndTime() by DESTBU ORDER-----------");
        Date endTime = null;//订单结束时间
        Long categoryId = 1L;
        //酒店订单
        if(OrderUtils.isHotelByCategoryId(order.getCategoryId())){
            List<Date> days = new ArrayList<Date>();
            if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
                for (OrdOrderItem orderItem : order.getOrderItemList()) {
                    if(categoryId.equals(orderItem.getCategoryId())){
                        List<OrdOrderHotelTimeRate> orderHotelTimeRateList = orderItem.getOrderHotelTimeRateList();
                        if(CollectionUtils.isNotEmpty(orderHotelTimeRateList)){
                            for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : orderHotelTimeRateList) {
                                days.add(ordOrderHotelTimeRate.getVisitTime());
                            }
                        }
                    }
                }
            }

            if(CollectionUtils.isNotEmpty(days)){
                Date time = Collections.max(days);
                endTime = DateUtil.addDays(time, 1);
                endTime = DateUtil.toYMDDate(endTime);
                order.setEndTime(endTime);
            }
        }else{
            //线路订单
            Long lineRouteId = order.getLineRouteId();
            int routeNum = 0;
            if(null != lineRouteId){
                ResultHandleT<ProdLineRoute> resultHandle = prodLineRouteClientService.findByProdLineRouteId(lineRouteId);
                if(null != resultHandle.getReturnContent()){
                    ProdLineRoute prodLineRoute = resultHandle.getReturnContent();
                    routeNum = prodLineRoute.getRouteNum();
                }
                if(routeNum>0)
                {
                    endTime = DateUtil.addDays(order.getVisitTime(), routeNum);
                    endTime = DateUtil.toYMDDate(endTime);
                    order.setEndTime(endTime);
                }
            }
        }
        if(null != endTime){
            logger.info("endTime is "+DateUtil.formatSimpleDate(endTime));
        }
        logger.info("-----------END METHOD calDestBuOrderEndTime() by DESTBU ORDER-----------");
    }

    @Override
    public OrdOrderDTO initOrderLightly(OrdOrderDTO order){
        initBuyInfo(order.getBuyInfo());
        initOrderBase(order);
        doInitOrderItems(order);

        return order;
    }


	//初始化订单的子单相关的信息
	private void doInitOrderItems(OrdOrderDTO order){
		BuyInfo buyInfo = order.getBuyInfo();
		
		logger.info("############order.getBuyInfo().getCategoryId() = "+order.getBuyInfo().getCategoryId());
		logger.info("############order.getCategoryId() = "+order.getCategoryId());

		if(order.getBuyInfo().getCategoryId()!=null&&18L==order.getBuyInfo().getCategoryId()){
			
			logger.info("--------------------------order.getBuyInfo().getCategoryId()--------------------------------");
			
			if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
				for(BuyInfo.Item item:buyInfo.getItemList()){
					OrdOrderItem orderItem = initItem(order, item, null);
					if(orderItem != null){
						order.addOrderItem(orderItem);
					}
				}
			}
		}
		
		if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
			try {
				order.setVisitTime(DateUtil.converDateFromStr4(buyInfo.getVisitTime()));
			} catch (ParseException e) {
				logger.error("buyInfo.getVisitTime() error.", e);
			}
			for(BuyInfo.Product bp:buyInfo.getProductList()){
				if(bp.getQuantity() > 0 || CollectionUtils.isNotEmpty(bp.getItemList())) {					
					//初始化pack
					OrdOrderPack orderPack =  initPack(order,bp);
					logger.info("--------------------------initPack--------------------end");
					order.addOrderPack(orderPack);
				}
			}
		}else{
			logger.info("buyInfo.getProductList() is null");
		}

		logger.info("-------------------------buyInfo.getItemList()----------------------------start");
		
		if(null==order.getBuyInfo().getCategoryId()||18L!=order.getBuyInfo().getCategoryId()){
			logger.info("-------------------------buyInfo.getItemList()----------------------------end");
			if(CollectionUtils.isEmpty(buyInfo.getItemList())){
				return;
			}
			for(BuyInfo.Item item:buyInfo.getItemList()){
				OrdOrderItem orderItem = initItem(order, item, null);
				if(orderItem != null){
					order.addOrderItem(orderItem);
				}
			}
		}
	}
	
	
	/**
	 * 计算订单的最晚确认时间
	 * @param order
	 */
	private void calOrderLastConfirmTime(OrdOrder order){
		logger.info("------------------start calOrderLastConfirmTime method-------------------");
		Date lastConfirmTime=null;
		OrdOrderItem orderItem = null;
		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
			for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
				if(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId())){
					orderItemList.add(ordOrderItem);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(orderItemList))
		{
			calItemLastAheadTime(orderItemList);
			for(int i = 0;i<orderItemList.size()-1;i++){
				for(int j=0;j<orderItemList.size()-1-i;j++){
					if(orderItemList.get(j).getLastAheadTime().after(orderItemList.get(j+1).getLastAheadTime())){
						orderItem = orderItemList.get(j);
						orderItemList.set(j, orderItemList.get(j+1));
						orderItemList.set(j+1, orderItem);
					}
				}
			}
			lastConfirmTime = orderItemList.get(0).getLastAheadTime();
			String lastConfirmTimetr = DateUtil.formatDate(lastConfirmTime, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
			lastConfirmTime = DateUtil.stringToDate(lastConfirmTimetr, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
		}
		order.setTicketLastConfirmTime(lastConfirmTime);
	}
	
	private void calItemLastAheadTime(List<OrdOrderItem> orderItemList){
		logger.info("------------------start calItemLastAheadTime method-------------------");
		for (OrdOrderItem ordOrderItem : orderItemList) {
			if(null != ordOrderItem.getAheadTime()){
				ordOrderItem.setLastAheadTime(ordOrderItem.getAheadTime());
			}else{
				logger.info("orderItem suppGoodsId is:"+ordOrderItem.getSuppGoodsId()+"itemLastAheadTime is null");
			}
		}
	}

    /**
     * @Description: 设置子单的提前预定时间
     * @author Wangsizhi
     * @date 2016-12-15 下午7:55:40
     */
    private void calcOrderItemAheadTime(OrdOrder order) {
        logger.info("------------------start calcOrderItemAheadTime method-------------------");
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        for (OrdOrderItem ordOrderItem : orderItemList) {
            Date aheadTime = ordOrderItem.getAheadTime();
            if(null != ordOrderItem.getAheadTime()){
                ordOrderItem.setLastAheadTime(ordOrderItem.getAheadTime());
            }else{
                logger.info("orderItem suppGoodsId is:"+ordOrderItem.getSuppGoodsId()+"itemLastAheadTime is null");
            }
        }
    }


    /**
     * 下单优化 缓存读数据,减少fillAccInOrNoAccinVO调用次数
     * @param buyInfo
     * @return
     */
    private List<OrderRequiredVO> fillAccInAndNoAccinVO(BuyInfo buyInfo){
        List<OrderRequiredVO> retList = new ArrayList<OrderRequiredVO>();
        List<Long> productIdList = new ArrayList<Long>();
        List<Long> suppGoodsIdList = new ArrayList<Long>();
        OrderRequiredVO accInsuranceVo;
        OrderRequiredVO noAccInsuranceVo;
        suppGoodsListHandle(buyInfo, productIdList, suppGoodsIdList);

        logger.info("productIdList:"+com.alibaba.fastjson.JSONObject.toJSONString(productIdList));
        logger.info("suppGoodsIdList:"+com.alibaba.fastjson.JSONObject.toJSONString(suppGoodsIdList));

        Object accInsuranceObj = OrderContextCache.get(OrderContextCache.KeyEnum.AccInsuranceVo.getName());
        if(accInsuranceObj != null) {
            accInsuranceVo = (OrderRequiredVO)accInsuranceObj;
        }else {
            Long startFillTime1 = System.currentTimeMillis();
            accInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, true);
            logger.info("startFillTime1耗时"+(System.currentTimeMillis() - startFillTime1));
            OrderContextCache.set(OrderContextCache.KeyEnum.AccInsuranceVo.getName(),accInsuranceVo);
        }

        Object noAccInsuranceObj = OrderContextCache.get(OrderContextCache.KeyEnum.NoAccInsuranceVo.getName());
        if(noAccInsuranceObj != null){
            noAccInsuranceVo = (OrderRequiredVO)noAccInsuranceObj;
        }else{
            Long startFillTime2 = System.currentTimeMillis();
            noAccInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, false);
            logger.info("startFillTime2耗时"+(System.currentTimeMillis() - startFillTime2));
            OrderContextCache.set(OrderContextCache.KeyEnum.NoAccInsuranceVo.getName(),noAccInsuranceVo);
        }

        logger.info("accInsuranceVo : " + accInsuranceVo);
        logger.info("noAccInsuranceVo : " + noAccInsuranceVo);
        retList.add(accInsuranceVo);
        retList.add(noAccInsuranceVo);
        return retList;
    }


    /**
     * 下单优化——减少delyflag计算次数
     * @param buyInfo
     * @return
     */
    private String calDelyFlag(BuyInfo buyInfo){

        Object obj = OrderContextCache.get(OrderContextCache.KeyEnum.DelyFlag.getName());
        if(obj != null) {
            return (String)obj;
        }
        String isNeedDelayed = "N";

        /*List<Long> productIdList = new ArrayList<Long>();
        List<Long> suppGoodsIdList = new ArrayList<Long>();
        suppGoodsListHandle(buyInfo, productIdList, suppGoodsIdList);
        Long startFillTime1 = System.currentTimeMillis();
        OrderRequiredVO accInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, true);
        Long startFillTime2 = System.currentTimeMillis();
        OrderRequiredVO noAccInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, false);;
        logger.info("startFillTime耗时"+(System.currentTimeMillis() - startFillTime2)+" "+(startFillTime2-startFillTime1));
        logger.info("accInsuranceVo : " + accInsuranceVo);
        logger.info("noAccInsuranceVo : " + noAccInsuranceVo);*/


        List<OrderRequiredVO> orderRequiredVOs = fillAccInAndNoAccinVO(buyInfo);
        OrderRequiredVO accInsuranceVo = orderRequiredVOs.get(0);
        OrderRequiredVO noAccInsuranceVo = orderRequiredVOs.get(1);

        //判断是否需要后置
        if (null != accInsuranceVo && null != noAccInsuranceVo) {

            int noAccInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getTravNumType());
            int accInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getTravNumType());

            Map<String, Map<String, Object>> calcTravellerDelayMap = orderRequiredClientService.calcTravellerDelayMap(noAccInsuranceVo, accInsuranceVo);
            if (null != calcTravellerDelayMap && calcTravellerDelayMap.size() > 0) {
                isNeedDelayed = "Y";
                //意外险不需要游玩人
                if (accInsTravNumTypeW == 2) {
                    isNeedDelayed = "N";
                }
                if (accInsTravNumTypeW == 3 || accInsTravNumTypeW == 4) {
                    isNeedDelayed = "Y";
                }
            }else{
                if (noAccInsTravNumTypeW == 3 && accInsTravNumTypeW == 4) {
                    //计算结果无差异 非意外险一个游玩人  意外险全部游玩人  则存travPersonQuantity - 1个 同一订单 姓名 无结算结果的
                    isNeedDelayed = "Y";
                }
            }

        }
        OrderContextCache.set(OrderContextCache.KeyEnum.DelyFlag.getName(),isNeedDelayed);
        return isNeedDelayed;
    }


   /**
    *
    * @Description: 目的地 酒+景 酒店套餐 意外险游玩人后置 设置主单游玩人后置
    * @author Wangsizhi
    * @date 2016-11-21 下午4:48:14
    */
    @Override
    public void initDestBuAccTravDelayed(BuyInfo buyInfo, OrdOrder order) {
        logger.info("目的地 酒+景 酒店套餐 意外险游玩人后置 设置主单游玩人后置" );
        logger.info("initDestBuAccTravDelayed orderId = " + order.getOrderId());
        String newAppVersion = buyInfo.getNewAppVersion();
        logger.info("newAppVersion = " + newAppVersion);
        if (OrdOrderUtils.isDestBuFrontAppOrder(order)) {
          //如果是无线老版本请求，走非后置
            if (StringUtils.isNotBlank(newAppVersion) && newAppVersion.equalsIgnoreCase("N")) {
                return;
            }
            String isNeedDelayed = calDelyFlag(buyInfo);
            //下单提速_重复调用代码段抽取start01
            /*String isNeedDelayed = "N";
            List<Long> productIdList = new ArrayList<Long>();
            List<Long> suppGoodsIdList = new ArrayList<Long>();
            suppGoodsListHandle(buyInfo, productIdList, suppGoodsIdList);

            OrderRequiredVO accInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, true);
            OrderRequiredVO noAccInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, false);;

            //判断是否需要后置
            if (null != accInsuranceVo && null != noAccInsuranceVo) {

                int noAccInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getTravNumType());
                int accInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getTravNumType());

                Map<String, Map<String, Object>> calcTravellerDelayMap = orderRequiredClientService.calcTravellerDelayMap(noAccInsuranceVo, accInsuranceVo);
                if (null != calcTravellerDelayMap && calcTravellerDelayMap.size() > 0) {
                    isNeedDelayed = "Y";
                    //意外险不需要游玩人
                    if (accInsTravNumTypeW == 2) {
                        isNeedDelayed = "N";
                    }
                    if (accInsTravNumTypeW == 3 || accInsTravNumTypeW == 4) {
                        isNeedDelayed = "Y";
                    }
                }else{
                    if (noAccInsTravNumTypeW == 3 && accInsTravNumTypeW == 4) {
                        //计算结果无差异 非意外险一个游玩人  意外险全部游玩人  则存travPersonQuantity - 1个 同一订单 姓名 无结算结果的
                        isNeedDelayed = "Y";
                    }
                }

            }*/
            //下单提速_重复调用代码段抽取end01

            /*List<OrdTravAdditionConf> toBeTravellerDayledList = calcTravellerDayled(noAccInsuranceVo, noAccInsuranceVo, buyInfo.getTravellers().size(), order, buyInfo);

            logger.info("toBeTravellerDayledList");
            if (toBeTravellerDayledList == null || toBeTravellerDayledList.size() == 0 ) {
                logger.info("toBeTravellerDayledList is null");
                isNeedDelayed = "N";
            } else {
                for (OrdTravAdditionConf ordTravAdditionConf : toBeTravellerDayledList) {
                    logger.info(ordTravAdditionConf.toString());
                }
            }

            if (null != toBeTravellerDayledList && toBeTravellerDayledList.size() > 0) {
                String tag = "N";
                for (OrdTravAdditionConf ordTravAdditionConf : toBeTravellerDayledList) {
                    if (null != ordTravAdditionConf) {
                        String userName = ordTravAdditionConf.getUserName();
                        String phoneNum = ordTravAdditionConf.getPhoneNum();
                        String email = ordTravAdditionConf.getEmail();
                        String enName = ordTravAdditionConf.getEnName();
                        String occup = ordTravAdditionConf.getOccup();
                        String idType = ordTravAdditionConf.getIdType();
                        String n = "N";
                        if (StringUtils.isNotBlank(userName) && userName.equalsIgnoreCase(n)
                                && StringUtils.isNotBlank(phoneNum) && phoneNum.equalsIgnoreCase(phoneNum)
                                && StringUtils.isNotBlank(email) && email.equalsIgnoreCase(n)
                                && StringUtils.isNotBlank(enName) && enName.equalsIgnoreCase(n)
                                && StringUtils.isNotBlank(occup) && occup.equalsIgnoreCase(n)
                                && StringUtils.isNotBlank(idType) && idType.equalsIgnoreCase(n)
                                ) {
                            tag = "N";
                        }else{
                            tag = "Y";
                        }
                    }
                    if (tag.equals("Y")) {
                        break;
                    }
                }

                logger.info("tag = " + tag);
                if (tag.equals("N")) {
                    isNeedDelayed = "N";
                }
            }
            */
            logger.info("isNeedDelayed = " + isNeedDelayed);

            if ("Y".equals(isNeedDelayed)) {
                OrdAccInsDelayInfo ordAccInsDelayInfo = new OrdAccInsDelayInfo();

                ordAccInsDelayInfo.setOrderId(order.getOrderId());
                ordAccInsDelayInfo.setTravDelayFlag(isNeedDelayed);
                ordAccInsDelayInfo.setTravDelayStatus(OrderEnum.ORDER_TRAV_DELAY_STATUS.UNCOMPLETED.name());

                //保存意外险后置订单后置信息
                logger.info("saveOrdAccInsDelayInfo orderId = " + order.getOrderId());
                ordAccInsDelayInfoService.saveOrdAccInsDelayInfo(ordAccInsDelayInfo);
            }
        }
    }

       /**
        *
        * @Description: 目的地 酒+景 酒店套餐 意外险游玩人后置 设置子单需要后置标识
        * @author Wangsizhi
        * @date 2016-11-21 下午4:48:14
        */
        private void initDestBuAccTravDelayOrderItem(BuyInfo buyInfo, OrdOrderDTO order) {
            logger.info("目的地 酒+景 酒店套餐 意外险游玩人后置 设置意外险子订单 游玩人后置标识" );

            /*String tag1 = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
            String tag = tag1 + "\n" + tag1 + "\n" + tag1 + "\n" + tag1 + "\n" + tag1 + "\n" + tag1 + "\n";
            logger.info(tag);*/

            logger.info("OrderId = " + order.getOrderId());
            logger.info("DistributorId = " + order.getDistributorId());
            logger.info("hasNeedPrepaid = " + order.hasNeedPrepaid());
            logger.info("BuCode = " + order.getBuCode());
            logger.info("CategoryId = " + order.getCategoryId());
            logger.info("SubCategoryId = " + order.getSubCategoryId());

            String newAppVersion = buyInfo.getNewAppVersion();
            logger.info("newAppVersion = " + newAppVersion);
            if (OrdOrderUtils.isDestBuFrontAppOrder(order)) {
                //如果是无线老版本请求，走非后置
                if (StringUtils.isNotBlank(newAppVersion) && newAppVersion.equalsIgnoreCase("N")) {
                    return;
                }
                String isNeedDelayed = calDelyFlag(buyInfo);
                //下单提速_重复调用代码段抽取start02
                /*String isNeedDelayed = "N";
                List<Long> productIdList = new ArrayList<Long>();
                List<Long> suppGoodsIdList = new ArrayList<Long>();
                suppGoodsListHandle(buyInfo, productIdList, suppGoodsIdList);

                OrderRequiredVO accInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, true);
                OrderRequiredVO noAccInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, false);;

                logger.info("accInsuranceVo : " + accInsuranceVo);
                logger.info("noAccInsuranceVo" + noAccInsuranceVo);

                //判断是否需要后置
                if (null != accInsuranceVo && null != noAccInsuranceVo) {

                    int noAccInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getTravNumType());
                    int accInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getTravNumType());

                    Map<String, Map<String, Object>> calcTravellerDelayMap = orderRequiredClientService.calcTravellerDelayMap(noAccInsuranceVo, accInsuranceVo);
                    if (null != calcTravellerDelayMap && calcTravellerDelayMap.size() > 0) {
                        isNeedDelayed = "Y";
                        //意外险不需要游玩人
                        if (accInsTravNumTypeW == 2) {
                            isNeedDelayed = "N";
                        }
                        if (accInsTravNumTypeW == 3 || accInsTravNumTypeW == 4) {
                            isNeedDelayed = "Y";
                        }
                    }else{
                        if (noAccInsTravNumTypeW == 3 && accInsTravNumTypeW == 4) {
                            //计算结果无差异 非意外险一个游玩人  意外险全部游玩人  则存travPersonQuantity - 1个 同一订单 姓名 无结算结果的
                            isNeedDelayed = "Y";
                        }
                    }
                }*/
                //下单提速_重复调用代码段抽取end02
                logger.info("isNeedDelayed = " + isNeedDelayed);

                if ("Y".equals(isNeedDelayed)) {
                    /*判断是否为目的地意外险， 是 则设置destBuAccFlag=Y到contentMap中， 开始*/
                    List<OrdOrderItem> orderItemList = order.getOrderItemList();
                    logger.info("orderItemList.size = " + orderItemList.size());

                    if (null != orderItemList && orderItemList.size() > 0) {
                        for (OrdOrderItem ordOrderItem : orderItemList) {
                            SuppGoods suppGoods = ordOrderItem.getSuppGoods();
                            if (null == suppGoods) {
                                SuppGoodsParam param = new SuppGoodsParam();
                                param.setProduct(true);
                                ProdProductParam ppp = new ProdProductParam();
                                ppp.setBizCategory(true);
                                ppp.setProductProp(true);
                                ppp.setProductPropValue(true);
                                param.setProductBranch(true);
                                param.setSupplier(true);
                                param.setProductParam(ppp);
                                param.setSuppGoodsExp(true);
                                param.setSuppGoodsEventAndRegion(true);

                                suppGoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId(), param).getReturnContent();
                            }

                            if (isDestBuAcc(suppGoods, order, "Y")) {
                                ordOrderItem.putContent("destBuAccFlag", "Y");
                            }
                        }
                    }
                }
            }
        }
       /**
        *
        * @Description: 判断商品是否为目的地 酒+景 酒店套餐 自由行订单的意外险
        * @author Wangsizhi
        * @date 2016-11-21 下午5:36:09
        */
       private boolean isDestBuAcc(SuppGoods suppGoods, OrdOrder order, String travDelayFlag) {
           boolean result = false;
           String productType = suppGoods.getProdProduct().getProductType();
           String buCode = suppGoods.getBu();
           Long orderCategoryId = order.getCategoryId();
           Long subCategoryId = order.getSubCategoryId();

           //TODO: For denug
           /*String tag = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
           logger.info(tag + "\n" + tag + "\n" + tag + "\n" + tag + "\n" + tag + "\n" + tag + "\n" + tag + "\n");
           logger.info("productType = " + productType);
           logger.info("buCode = " + buCode);
           logger.info("orderCategoryId = " + orderCategoryId);
           logger.info("travellerDelayFlag = " + travDelayFlag);

           */

           if (StringUtils.isNotBlank(productType) && "INSURANCE_730".equalsIgnoreCase(productType)
                   && StringUtils.isNotBlank(travDelayFlag) && "Y".equalsIgnoreCase(travDelayFlag)
                   && StringUtils.isNotBlank(order.getBuCode())
                   && order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode())
                   && null != order.getCategoryId()
                   && (   (BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
                           && null != order.getSubCategoryId()
                           && BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())
                           )
                       ||  BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
                       )
                   ) {
                   result = true;
               }

           return result;
       }

       /**
        * 处理购买商品数据以及需要展现的数据(订单必填项使用)
        * @param model
        * @param buyInfo
        * @param productIdList
        * @param suppGoodsIdList
        * @param defaultErrorPage
        * @param errorMsg
        * @return
        * @author Wangsizhi
        * @throws Exception
        */
       private void suppGoodsListHandle(BuyInfo buyInfo,List<Long> productIdList,List<Long> suppGoodsIdList) {

           List<Item> itemList = new ArrayList<Item>();
           List<Item> buyInfoItemList = buyInfo.getItemList();
           if (null != buyInfoItemList && buyInfoItemList.size() > 0) {
               itemList.addAll(buyInfoItemList);
           }

           List<Product> buyInfoProductList = buyInfo.getProductList();
           if (null != buyInfoProductList && buyInfoProductList.size() > 0) {
               for (Product product : buyInfoProductList) {
                   productIdList.add(product.getProductId());
                   List<Item> proItems = product.getItemList();
                   for (Item item : proItems) {
                       itemList.add(item);
                   }
               }
           }

           if (null != itemList && !itemList.isEmpty()) {
               for(Item item : itemList){
                   suppGoodsIdList.add(item.getGoodsId());
                   SuppGoods suppGoods = getSuppGoods(item.getGoodsId());
                   if (null == suppGoods) {
                       logger.info("goodsId=" + item.getGoodsId() + "获取商品失败");
                   }
                   productIdList.add(suppGoods.getProductId());
               }
           }

       }

       /**
        *
        * @Description: 根据goodsId获取goods
        * @author Wangsizhi
        * @date 2016-11-21 下午6:00:54
        */
       private SuppGoods getSuppGoods(Long goodsId) {
           ResultHandleT<SuppGoods> goodsResultHandleT = null;
           try {
//             goodsResultHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, goodsId);
               goodsResultHandleT = suppGoodsClientService.findSuppGoodsById(goodsId);
           } catch (Exception e) {
               logger.info("goodsId=" + goodsId + "获取商品失败");
           }
           if (goodsResultHandleT.isFail() || goodsResultHandleT.getReturnContent() == null) {
               return null;
           }
           SuppGoods suppGoods = goodsResultHandleT.getReturnContent();
           return suppGoods;
       }

       /**
        *
        * @Description: 目的地 自由行 酒+景 酒店套餐 计算需要后置的意外险游玩人
        * @author Wangsizhi
        * @date 2016-11-22 上午10:41:22
        */
       @Override
       public void saveDestBuAccTrav(BuyInfo buyInfo, OrdOrder ordOrder){
           logger.info("------------------saveDestBuAccTrav orderId = " + ordOrder.getOrderId());
           /*计算订单总人数 开始*/
           int adultQuantity = buyInfo.getAdultQuantity();
           int childQuantity = buyInfo.getChildQuantity();
           int travPersonQuantity = adultQuantity+childQuantity;
           if(buyInfo.getTravPersonQuantity() > travPersonQuantity){
               travPersonQuantity = buyInfo.getTravPersonQuantity();
           }
           /*计算订单总人数 结束*/

           /*获取非意外险，意外险露出必选项vo 开始*/
           /*List<Long> productIdList = new ArrayList<Long>();
           List<Long> suppGoodsIdList = new ArrayList<Long>();
           suppGoodsListHandle(buyInfo, productIdList, suppGoodsIdList);
           OrderRequiredVO accInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, true);
           OrderRequiredVO noAccInsuranceVo = orderRequiredClientService.fillAccInOrNoAccinVO(productIdList, suppGoodsIdList, false);*/

           List<OrderRequiredVO> orderRequiredVOs = fillAccInAndNoAccinVO(buyInfo);
           OrderRequiredVO accInsuranceVo = orderRequiredVOs.get(0);
           OrderRequiredVO noAccInsuranceVo = orderRequiredVOs.get(1);

           String tag0 = "##########################################################";
           String tag = tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n";

           logger.info(tag + "accInsuranceVo : " + accInsuranceVo + "\n noAccInsuranceVo" + noAccInsuranceVo);

           /*获取非意外险，意外险露出必选项vo 结束*/
           List<OrdTravAdditionConf> toDelayTravellerList = null;
           if (null != accInsuranceVo && null != noAccInsuranceVo) {
               toDelayTravellerList = calcTravellerDayled(noAccInsuranceVo, accInsuranceVo, travPersonQuantity, ordOrder, buyInfo);
           }
           //TODO: For debug

           if (null == toDelayTravellerList || toDelayTravellerList.size() == 0) {
             //将该订单后置信息表中后置状态置为“N”
               OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(ordOrder.getOrderId());
               ordAccInsDelayInfo.setTravDelayFlag("N");
               ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
               logger.info("Set ordAccInsDelayInfo TravDelayFlag N orderId = " + ordOrder.getOrderId());
               logger.info("ordAccInsDelayInfo " + ordAccInsDelayInfo.toString());
            }

           List<OrdPerson> ordTravellerList = ordOrder.getOrdTravellerList();

           if (null != ordTravellerList && ordTravellerList.size() == 1) {
               String y = "N";
               OrdPerson ordPerson = ordTravellerList.get(0);
               for (OrdTravAdditionConf ordTravAdditionConf : toDelayTravellerList) {
                   logger.info(ordTravAdditionConf.toString());
                       if (null != ordTravAdditionConf) {
                           String userName = ordTravAdditionConf.getUserName();
                           if (StringUtils.isNotBlank(userName)) {
                               if (ordTravAdditionConf.getPhoneNum().equalsIgnoreCase("Y")) {
                                   if (StringUtils.isBlank(ordPerson.getMobile())) {
                                       y = "Y";
                                   }
                               }

                               if (ordTravAdditionConf.getEnName().equalsIgnoreCase("Y")) {
                                   if (StringUtils.isBlank(ordPerson.getFirstName()) || StringUtils.isBlank(ordPerson.getLastName())) {
                                       y = "Y";
                                    }
                                }

                               if (ordTravAdditionConf.getEmail().equalsIgnoreCase("Y")) {
                                    if (StringUtils.isBlank(ordPerson.getEmail())) {
                                        y = "Y";
                                    }
                                }

                               if (ordTravAdditionConf.getOccup().equalsIgnoreCase("Y")) {
                                   if (StringUtils.isBlank(ordPerson.getPeopleType())) {
                                       y = "Y";
                                    }
                                }

                               if (ordTravAdditionConf.getIdType().equalsIgnoreCase("Y")) {
                                    if (StringUtils.isBlank(ordPerson.getIdType())) {
                                        y = "Y";
                                    }
                                }


                           }



                       }
                  }

               if (y.equalsIgnoreCase("N")) {
                 //将该订单后置信息表中后置状态置为“N”
                   OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(ordOrder.getOrderId());
                   ordAccInsDelayInfo.setTravDelayFlag("N");
                   ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
                   logger.info("Set ordAccInsDelayInfo TravDelayFlag N orderId = " + ordOrder.getOrderId());
                   logger.info("ordAccInsDelayInfo " + ordAccInsDelayInfo.toString());
               }

            }

           boolean isNeedDelay = false;
           if (null != toDelayTravellerList && toDelayTravellerList.size() > 0) {
               //保存需要后置的游玩人信息。
               for (OrdTravAdditionConf toOrdTravAdditionConf : toDelayTravellerList) {

                   String enName = toOrdTravAdditionConf.getEnName();
                   String email = toOrdTravAdditionConf.getEmail();
                   String idType = toOrdTravAdditionConf.getIdType();
                   String occup = toOrdTravAdditionConf.getOccup();
                   String userName = toOrdTravAdditionConf.getUserName();
                   String phoneNum = toOrdTravAdditionConf.getPhoneNum();

                   Long orderPersonId = toOrdTravAdditionConf.getOrderPersonId();
                   OrdPerson ordPersonFirst =null;
                   for (OrdPerson o : ordTravellerList) {
                       if (o.getOrdPersonId().equals(orderPersonId)) {
                            ordPersonFirst = o;
                            break;
                        }
                    }

                   logger.info("saveDestBuAccTrav toOrdTravAdditionConf " + toOrdTravAdditionConf.toString());

                   boolean userNameTag = true;
                   boolean phoneNumTag = true;
                   boolean occupTag = true;
                   boolean idTypeTag = true;
                   boolean emailTag = true;
                   boolean enNameTag = true;

                   if (null != ordPersonFirst) {

                       logger.info("OrdPersonId = " + ordPersonFirst.getOrdPersonId());
                       logger.info("FullName = " + ordPersonFirst.getFullName());
                       logger.info("Mobile = " + ordPersonFirst.getMobile());
                       logger.info("PeopleType = " + ordPersonFirst.getPeopleType());
                       logger.info("IdType = " + ordPersonFirst.getIdType());
                       logger.info("IdNo = " + ordPersonFirst.getIdNo());
                       logger.info("Email = " + ordPersonFirst.getEmail());
                       logger.info("FirstName = " + ordPersonFirst.getFirstName());
                       logger.info("LastName = " + ordPersonFirst.getLastName());

                       if ("Y".equalsIgnoreCase(userName)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getFullName())) {
                               userNameTag = false;
                            }
                        }else {
                            userNameTag = false;
                        }
                       if ("Y".equalsIgnoreCase(phoneNum)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getMobile())) {
                               phoneNumTag = false;
                           }
                        }else {
                            phoneNumTag = false;
                        }
                       if ("Y".equalsIgnoreCase(occup)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getPeopleType())) {
                               occupTag = false;
                           }
                        }else {
                            occupTag = false;
                        }
                       if ("Y".equalsIgnoreCase(idType)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getIdType())) {
                               if ( StringUtils.isNotBlank(ordPersonFirst.getIdNo())) {
                                   idTypeTag = false;
                            }

                           }
                        }else {
                            idTypeTag = false;
                        }
                       if ("Y".equalsIgnoreCase(email)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getEmail())) {
                               emailTag = false;
                           }
                        }else {
                            emailTag = false;
                        }
                       if ("Y".equalsIgnoreCase(enName)) {
                           if (StringUtils.isNotBlank(ordPersonFirst.getFirstName()) && StringUtils.isNotBlank(ordPersonFirst.getLastName())) {
                               enNameTag = false;
                           }
                        }else {
                            enNameTag = false;
                        }

                    }


                   logger.info("enNameTag = " + enNameTag);
                   logger.info("emailTag = " + emailTag);
                   logger.info("idTypeTag = " + idTypeTag);
                   logger.info("occupTag = " + occupTag);
                   logger.info("phoneNumTag = " + phoneNumTag);
                   logger.info("userNameTag = " + userNameTag);

                   /*if (!enNameTag && !emailTag && !idTypeTag && !occupTag && !phoneNumTag && !userNameTag) {
                     logger.info("OrdTravAdditionConf is not to be supply");
                    }else {
                        ordTravAdditionConfService.saveTravAdditionConf(toOrdTravAdditionConf);
                        isNeedDelay = true;
                    }*/
                   ordTravAdditionConfService.saveTravAdditionConf(toOrdTravAdditionConf);
                   isNeedDelay = true;
            }

           if (!isNeedDelay) {
                //通过计算需补充的对象和游玩人对应属性是否有值，若某属性需要补充，单游玩人中有值则不需要补充
               //将该订单后置信息表中后置状态置为“N”
               OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(ordOrder.getOrderId());
               ordAccInsDelayInfo.setTravDelayFlag("N");
               ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
               logger.info("Set ordAccInsDelayInfo TravDelayFlag N orderId = " + ordOrder.getOrderId());
               logger.info("ordAccInsDelayInfo " + ordAccInsDelayInfo.toString());
            }

           }

       }

       /**
        *
        * @Description: 计算出需要后置补充的游玩人
        * @author Wangsizhi
        * @date 2016-11-22 下午1:55:10
        */
       public List<OrdTravAdditionConf> calcTravellerDayled(OrderRequiredVO noAccInsuranceVo, OrderRequiredVO accInsuranceVo,
                                                               int travPersonQuantity, OrdOrder ordOrder, BuyInfo buyInfo) {
           List<OrdTravAdditionConf> toDelayTravellerList = new ArrayList<OrdTravAdditionConf>();

           int noAccInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getTravNumType());
           int accInsTravNumTypeW = BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getTravNumType());

           Map<String, Map<String, Object>> calcTravellerDelayInfoMap = orderRequiredClientService.calcTravellerDelayMap(noAccInsuranceVo, accInsuranceVo);
           Map<String, Map<String, Object>> hasNeedAllTypeMap = orderRequiredClientService.hasNeedAllTypeList(calcTravellerDelayInfoMap);
           Map<String, Map<String, Object>> hasNonNeedAllTypeMap = orderRequiredClientService.hasNonNeedAllTypeList(calcTravellerDelayInfoMap);

           logger.info("Other 5  need to delayed map calcTravellerDelayInfoMap = " + calcTravellerDelayInfoMap);
           logger.info("Other 5  need all type to delayed map hasNeedAllTypeMap = " + hasNeedAllTypeMap);
           logger.info("Other 5  need non all type to delayed map hasNonNeedAllTypeMap = " + hasNonNeedAllTypeMap);

           List<Person> travellersd = buyInfo.getTravellers();

           travPersonQuantity = travellersd.size();

           logger.info("travPersonQuantity = buyInfo.getTravellers()buyInfo.getTravellers().size()========travPersonQuantity = " + travPersonQuantity);

           logger.info("buyInfo.getTravellers");
           for (Person person : travellersd) {
                String fullName = person.getFullName();
                String relateContactId = person.getReceiverId();
                logger.info("fullName = " + fullName + "\t relateContactId = " + relateContactId);
           }

           logger.info("ordOrder.getOrdPersonList");
           for (OrdPerson ordPerson : ordOrder.getOrdPersonList()) {
               String fullName = ordPerson.getFullName();
               logger.info("fullName = " + fullName);
           }


           Long orderId = ordOrder.getOrderId();
           String Y = "Y";
           switch (noAccInsTravNumTypeW) {
               case 2:/* 非意外险人数类型为不需要 */
                   switch (accInsTravNumTypeW) {
                   case 2:/* 意外险人数类型为不需要 */
                       //不需要补
                       break;
                   case 3:/* 意外险人数类型为需要一个 */
                       logger.info("非意外险人数类型为不需要, 意外险人数类型为需要一个, 用意外险vo补一个 orderId = " + orderId);
                       OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                       ordTravAdditionConf.setOrderId(orderId);
                       ordTravAdditionConf.setUserName(Y);

                       OrdPerson ordPerson = ordOrder.getOrdTravellerList().get(0);
                       Long ordPersonId = ordPerson.getOrdPersonId();
                       ordTravAdditionConf.setOrderPersonId(ordPersonId);

                       if (null != accInsuranceVo.getPhoneType()
                               && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2) {
                           ordTravAdditionConf.setPhoneNum(Y);
                       }
                       if (null != accInsuranceVo.getEnnameType()
                               && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2) {
                           ordTravAdditionConf.setEnName(Y);
                       }
                       if (null != accInsuranceVo.getEmailType()
                               && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2) {
                           ordTravAdditionConf.setEmail(Y);
                       }
                       if (null != accInsuranceVo.getOccupType()
                               && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2) {
                           ordTravAdditionConf.setOccup(Y);
                       }
                       if (null != accInsuranceVo.getIdNumType()
                               && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2) {
                           ordTravAdditionConf.setIdType(Y);
                       }

                       toDelayTravellerList.add(ordTravAdditionConf);
                       break;
                   case 4:/* 意外险人数类型为全部需要 */
                       logger.info("非意外险人数类型为不需要, 意外险人数类型为全部需要, 用意外险vo补所有 orderId = " + orderId);
                       for (int i = 0; i < travPersonQuantity; i++) {
                           OrdTravAdditionConf ordTravAdditionConf2 = new OrdTravAdditionConf();
                           ordTravAdditionConf2.setOrderId(orderId);
                           ordTravAdditionConf2.setUserName(Y);

                           OrdPerson ordPerson2 = ordOrder.getOrdTravellerList().get(i);
                           Long ordPersonId2 = ordPerson2.getOrdPersonId();
                           ordTravAdditionConf2.setOrderPersonId(ordPersonId2);

                           if (null != accInsuranceVo.getPhoneType()
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2) {
                               ordTravAdditionConf2.setPhoneNum(Y);
                           }
                           if (null != accInsuranceVo.getEnnameType()
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2) {
                               ordTravAdditionConf2.setEnName(Y);
                           }
                           if (null != accInsuranceVo.getEmailType()
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2) {
                               ordTravAdditionConf2.setEmail(Y);
                           }
                           if (null != accInsuranceVo.getOccupType()
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2) {
                               ordTravAdditionConf2.setOccup(Y);
                           }
                           if (null != accInsuranceVo.getIdNumType()
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2) {
                               ordTravAdditionConf2.setIdType(Y);
                           }
                           toDelayTravellerList.add(ordTravAdditionConf2);
                       }
                       break;
                   default:
                       break;
                   }
                   break;
               case 3:/* 非意外险人数类型为需要一个 */
                   switch (accInsTravNumTypeW) {
                       case 2:/* 意外险人数类型为不需要 */
                           //不需要补
                           break;
                       case 3:/* 意外险人数类型为需要一个 */
                           /*若有差异，则用差异list补那一个游玩人*/
                           if (null != calcTravellerDelayInfoMap && !calcTravellerDelayInfoMap.isEmpty()) {
                               logger.info("非意外险人数类型为需要一个, 意外险人数类型为需要一个, 若有差异，则用差异list补那一个游玩人, orderId = " + orderId);
                               OrdPerson ordPerson = ordOrder.getOrdTravellerList().get(0);
                               Long ordPersonId = ordPerson.getOrdPersonId();
                               Person traveller = buyInfo.getTravellers().get(0);
                               String relateContactId = traveller.getReceiverId();
                               logger.info("RelateContactId = " + relateContactId);
                               logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPerson.getFullName());

                               OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                               ordTravAdditionConf.setOrderId(orderId);
                               ordTravAdditionConf.setOrderPersonId(ordPersonId);
                               ordTravAdditionConf.setRelateContactId(relateContactId);

                               if (calcTravellerDelayInfoMap.containsKey("phoneType")) {
                                   ordTravAdditionConf.setPhoneNum(Y);
                               }
                               if (calcTravellerDelayInfoMap.containsKey("ennameType")) {
                                   ordTravAdditionConf.setEnName(Y);
                               }
                               if (calcTravellerDelayInfoMap.containsKey("emailType")) {
                                   ordTravAdditionConf.setEmail(Y);
                               }
                               if (calcTravellerDelayInfoMap.containsKey("occupType")) {
                                   ordTravAdditionConf.setOccup(Y);
                               }
                               if (calcTravellerDelayInfoMap.containsKey("idNumType")) {
                                   ordTravAdditionConf.setIdType(Y);
                               }

                               toDelayTravellerList.add(ordTravAdditionConf);
                           }else {
                               /*若无差异，则不需要补*/
                               logger.info("非意外险人数类型为需要一个, 意外险人数类型为需要一个, 若无差异，则不需要补, orderId = " + orderId);
                           }
                           break;
                       case 4:/* 意外险人数类型为全部需要 */
                           /*如有差异，用差异list补已有的那一个游玩人；其余的all-1个游玩人，用意外险vo补*/
                           if (null != calcTravellerDelayInfoMap && !calcTravellerDelayInfoMap.isEmpty()) {
                               logger.info("非意外险人数类型为需要一个, 非意外险人数类型为全部需要,  orderId = " + orderId);
                               logger.info("用差异list补已有的那一个游玩人；其余的all-1个游玩人，用意外险vo补,  orderId = " + orderId);
                               OrdPerson ordPerson = ordOrder.getOrdTravellerList().get(0);
                               Long ordPersonId = ordPerson.getOrdPersonId();
                               Person traveller = buyInfo.getTravellers().get(0);
                               String relateContactId = traveller.getReceiverId();
                               logger.info("RelateContactId = " + relateContactId);
                               logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPerson.getFullName());

                               OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                               ordTravAdditionConf.setOrderId(orderId);
                               ordTravAdditionConf.setOrderPersonId(ordPersonId);
                               ordTravAdditionConf.setRelateContactId(relateContactId);

                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getPhoneType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2
                                  ) {
                                   ordTravAdditionConf.setPhoneNum(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEnnameType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2
                                  ) {
                                   ordTravAdditionConf.setEnName(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEmailType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2
                                       ) {
                                   ordTravAdditionConf.setEmail(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getOccupType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2
                                       ) {
                                   ordTravAdditionConf.setOccup(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getIdNumType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2
                                       ) {
                                   ordTravAdditionConf.setIdType(Y);
                               }
                               toDelayTravellerList.add(ordTravAdditionConf);
                               logger.info("补充其余的all-1个时，用意外险vo中，这 （ 手机号数量类型、 英文名选择类型、 邮箱选择类型、人群选择类型、证件数量类型 ）5项里需要所有的，只需要一个的在已有的那个联系人中已有,  orderId = " + orderId);
                               for (int i = 0; i < travPersonQuantity - 1; i++) {
                                   OrdTravAdditionConf ordTravAdditionConf2 = new OrdTravAdditionConf();
                                   ordTravAdditionConf2.setOrderId(orderId);
                                   ordTravAdditionConf2.setUserName(Y);

                                   OrdPerson ordPerson2 = ordOrder.getOrdTravellerList().get(i + 1);
                                   Long ordPersonId2 = ordPerson2.getOrdPersonId();
                                   ordTravAdditionConf2.setOrderPersonId(ordPersonId2);

                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) == 4) {
                                       ordTravAdditionConf2.setPhoneNum(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) == 4) {
                                       ordTravAdditionConf2.setEnName(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) == 4) {
                                       ordTravAdditionConf2.setEmail(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) == 4) {
                                       ordTravAdditionConf2.setOccup(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) == 4) {
                                       ordTravAdditionConf2.setIdType(Y);
                                   }
                                   toDelayTravellerList.add(ordTravAdditionConf2);
                               }
                           }else {
                               /*若无差异，其余的all-1个游玩人，用意外险vo中类型为需要全部的补*/
                               logger.info("补充其余的all-1个时，用意外险vo中，这 （ 手机号数量类型、 英文名选择类型、 邮箱选择类型、人群选择类型、证件数量类型 ）5项里需要所有的，只需要一个的在已有的那个联系人中已有,  orderId = " + orderId);
                               logger.info("ordOrder.getOrdTravellerList");
                               for (OrdPerson t : ordOrder.getOrdTravellerList()) {
                                   String fullName = t.getFullName();
                                   Long ordPersonId2 = t.getOrdPersonId();
                                   logger.info("ordPersonId2 = " + ordPersonId2 + "  fullName = " + fullName);
                               }
                               for (int i = 0; i < travPersonQuantity - 1; i++) {
                                   OrdTravAdditionConf ordTravAdditionConf2 = new OrdTravAdditionConf();
                                   ordTravAdditionConf2.setOrderId(orderId);
                                   ordTravAdditionConf2.setUserName(Y);

                                   OrdPerson ordPerson2 = ordOrder.getOrdTravellerList().get(i + 1);
                                   Long ordPersonId2 = ordPerson2.getOrdPersonId();
                                   ordTravAdditionConf2.setOrderPersonId(ordPersonId2);

                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) == 4) {
                                       ordTravAdditionConf2.setPhoneNum(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) == 4) {
                                       ordTravAdditionConf2.setEnName(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) == 4) {
                                       ordTravAdditionConf2.setEmail(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) == 4) {
                                       ordTravAdditionConf2.setOccup(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) == 4) {
                                       ordTravAdditionConf2.setIdType(Y);
                                   }
                                   toDelayTravellerList.add(ordTravAdditionConf2);
                               }
                           }
                           break;
                       default:
                           break;
                   }
                   break;
               case 4:/* 非意外险人数类型为全部需要 */
                   switch (accInsTravNumTypeW) {
                   case 2:/* 意外险人数类型为不需要 */
                       //不需要补
                       break;
                   case 3:/* 意外险人数类型为需要一个 */
                       logger.info("非意外险人数类型为全部需要，意外险人数类型为需要一个 ,  orderId = " + orderId);
                       if (null != calcTravellerDelayInfoMap && !calcTravellerDelayInfoMap.isEmpty()) {
                           logger.info("用差值list补已有第一个联系人，默认第一个游玩人信息最全,  orderId = " + orderId);
                           OrdPerson ordPerson = ordOrder.getOrdTravellerList().get(0);
                           Long ordPersonId = ordPerson.getOrdPersonId();

                           Person traveller = buyInfo.getTravellers().get(0);
                           String relateContactId = traveller.getReceiverId();
                           logger.info("RelateContactId = " + relateContactId);
                           logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPerson.getFullName());

                           OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                           ordTravAdditionConf.setOrderId(orderId);
                           ordTravAdditionConf.setOrderPersonId(ordPersonId);
                           ordTravAdditionConf.setRelateContactId(relateContactId);

                           if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getPhoneType()) == 2
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2
                              ) {
                               ordTravAdditionConf.setPhoneNum(Y);
                           }
                           if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEnnameType()) == 2
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2
                              ) {
                               ordTravAdditionConf.setEnName(Y);
                           }
                           if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEmailType()) == 2
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2
                                   ) {
                               ordTravAdditionConf.setEmail(Y);
                           }
                           if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getOccupType()) == 2
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2
                                   ) {
                               ordTravAdditionConf.setOccup(Y);
                           }
                           if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getIdNumType()) == 2
                                   && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2
                                   ) {
                               ordTravAdditionConf.setIdType(Y);
                           }
                           toDelayTravellerList.add(ordTravAdditionConf);
                       }else {
                           /*若无差异，则不需要补*/
                           logger.info("若无差异，则不需要补");
                       }
                       break;
                   case 4:/* 意外险人数类型为全部需要 */
                       logger.info("非意外险人数类型为全部需要，意外险人数类型为全部需要 ,  orderId = " + orderId);
                       List<OrdPerson> ordPersonList = ordOrder.getOrdTravellerList();
                       /*若有差异*/
                       if (null != calcTravellerDelayInfoMap && !calcTravellerDelayInfoMap.isEmpty()) {
                           /*差异list中，这（手机号数量类型、 英文名选择类型、 邮箱选择类型、人群选择类型、证件数量类型 ）5相中有需要所有的，则用差异差异list中需要所有的补充全部游玩，
                            *                                                                  有不需要所有的，则用用差异差异list中不需要所有的补充游玩人里默认的第一个游玩人*/
                           if (null != hasNeedAllTypeMap && hasNeedAllTypeMap.size() > 0) {
                               if (null != hasNonNeedAllTypeMap && hasNonNeedAllTypeMap.size() > 0) {
                                /*部分需要不所有，部分只补第一个*/
                                   Long ordPersonId = ordPersonList.get(0).getOrdPersonId();

                                   Person traveller = buyInfo.getTravellers().get(0);
                                   String relateContactId = traveller.getReceiverId();
                                   logger.info("RelateContactId = " + relateContactId);
                                   logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPersonList.get(0).getFullName());

                                   OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                                   ordTravAdditionConf.setOrderId(orderId);
                                   ordTravAdditionConf.setOrderPersonId(ordPersonId);
                                   ordTravAdditionConf.setRelateContactId(relateContactId);

                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getPhoneType()) == 2
                                           && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2
                                      ) {
                                       ordTravAdditionConf.setPhoneNum(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEnnameType()) == 2
                                           && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2
                                      ) {
                                       ordTravAdditionConf.setEnName(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEmailType()) == 2
                                           && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2
                                           ) {
                                       ordTravAdditionConf.setEmail(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getOccupType()) == 2
                                           && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2
                                           ) {
                                       ordTravAdditionConf.setOccup(Y);
                                   }
                                   if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getIdNumType()) == 2
                                           && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2
                                           ) {
                                       ordTravAdditionConf.setIdType(Y);
                                   }
                                   toDelayTravellerList.add(ordTravAdditionConf);

                                   for (int i = 1; i < ordPersonList.size(); i++) {
                                       OrdPerson ordPerson = ordPersonList.get(i);
                                       Long ordPersonId2 = ordPerson.getOrdPersonId();

                                       Person traveller2 = buyInfo.getTravellers().get(i);
                                       String relateContactId2 = traveller2.getReceiverId();
                                       logger.info("RelateContactId = " + relateContactId2);
                                       logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller2.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPerson.getFullName());

                                       OrdTravAdditionConf ordTravAdditionConf2 = new OrdTravAdditionConf();
                                       ordTravAdditionConf2.setOrderId(orderId);
                                       ordTravAdditionConf2.setOrderPersonId(ordPersonId2);
                                       ordTravAdditionConf.setRelateContactId(relateContactId2);

                                       if (hasNeedAllTypeMap.containsKey("phoneType")) {
                                           ordTravAdditionConf2.setPhoneNum(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("ennameType")) {
                                           ordTravAdditionConf2.setEnName(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("emailType")) {
                                           ordTravAdditionConf2.setEmail(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("occupType")) {
                                           ordTravAdditionConf2.setOccup(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("idNumType")) {
                                           ordTravAdditionConf2.setIdType(Y);
                                       }
                                       toDelayTravellerList.add(ordTravAdditionConf2);
                                   }
                               }else {
                                /*全部为需要补所有*/
                                   for (int i = 0; i < ordPersonList.size(); i++) {
                                       OrdPerson ordPerson = ordPersonList.get(i);
                                       Long ordPersonId2 = ordPerson.getOrdPersonId();

                                       Person traveller = buyInfo.getTravellers().get(i);
                                       String relateContactId = traveller.getReceiverId();
                                       logger.info("RelateContactId = " + relateContactId);
                                       logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPerson.getFullName());

                                       OrdTravAdditionConf ordTravAdditionConf2 = new OrdTravAdditionConf();
                                       ordTravAdditionConf2.setOrderId(orderId);
                                       ordTravAdditionConf2.setOrderPersonId(ordPersonId2);
                                       ordTravAdditionConf2.setRelateContactId(relateContactId);

                                       if (hasNeedAllTypeMap.containsKey("phoneType")) {
                                           ordTravAdditionConf2.setPhoneNum(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("ennameType")) {
                                           ordTravAdditionConf2.setEnName(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("emailType")) {
                                           ordTravAdditionConf2.setEmail(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("occupType")) {
                                           ordTravAdditionConf2.setOccup(Y);
                                       }
                                       if (hasNeedAllTypeMap.containsKey("idNumType")) {
                                           ordTravAdditionConf2.setIdType(Y);
                                       }
                                       toDelayTravellerList.add(ordTravAdditionConf2);
                                   }
                               }
                           }else {
                               /*差异list中，这（手机号数量类型、 英文名选择类型、 邮箱选择类型、人群选择类型、证件数量类型 ）5相中没有需要所有的，则用差异补游玩人里默认的第一个游玩人*/
                               Long ordPersonId = ordPersonList.get(0).getOrdPersonId();

                               Person traveller = buyInfo.getTravellers().get(0);
                               String relateContactId = traveller.getReceiverId();
                               logger.info("RelateContactId = " + relateContactId);
                               logger.info("buyInfo traveller 中此游玩人姓名 = " + traveller.getFullName() + "order ordPerson 中此游玩人姓名 = " + ordPersonList.get(0).getFullName());

                               OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
                               ordTravAdditionConf.setOrderId(orderId);
                               ordTravAdditionConf.setOrderPersonId(ordPersonId);
                               ordTravAdditionConf.setRelateContactId(relateContactId);

                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getPhoneType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getPhoneType()) != 2
                                  ) {
                                   ordTravAdditionConf.setPhoneNum(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEnnameType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEnnameType()) != 2
                                  ) {
                                   ordTravAdditionConf.setEnName(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getEmailType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getEmailType()) != 2
                                       ) {
                                   ordTravAdditionConf.setEmail(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getOccupType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getOccupType()) != 2
                                       ) {
                                   ordTravAdditionConf.setOccup(Y);
                               }
                               if (BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(noAccInsuranceVo.getIdNumType()) == 2
                                       && BizOrderRequired.BIZ_ORDER_REQUIRED_TRAV_NUM_LIST.getWeightByCode(accInsuranceVo.getIdNumType()) != 2
                                       ) {
                                   ordTravAdditionConf.setIdType(Y);
                               }
                               toDelayTravellerList.add(ordTravAdditionConf);
                           }
                       }
                       break;

                   default:
                       break;
                   }
                   break;
               default:
                   break;
           }
           logger.info("All toDelayTravellerList");

           for (OrdTravAdditionConf ordTravAdditionConf : toDelayTravellerList) {
            logger.info(ordTravAdditionConf.toString());
           }

           for (Iterator<OrdTravAdditionConf> it = toDelayTravellerList.iterator(); it.hasNext();) {
               OrdTravAdditionConf ordTravAdditionConf = it.next();
               boolean notNeedSupply = isNotNeedSupply(ordTravAdditionConf);
               if (notNeedSupply) {
                   it.remove();
               }
           }
           return toDelayTravellerList;
       }

   /**
    *
    * @Description: 如果 phoneNum、enName、email、occup、idType都不需要补充，则此条游玩人信息不需要补充
    * @author Wangsizhi
    * @date 2016-12-22 下午1:56:21
    */
    private boolean isNotNeedSupply(OrdTravAdditionConf ordTravAdditionConf) {
        boolean isNotNeedSupply = false;

        String phoneNum = ordTravAdditionConf.getPhoneNum();
        String enName = ordTravAdditionConf.getEnName();
        String email = ordTravAdditionConf.getEmail();
        String occup = ordTravAdditionConf.getOccup();
        String idType = ordTravAdditionConf.getIdType();

        boolean noNeedPhoneNum = true;
        boolean noNeedEnName = true;
        boolean noNeedEmail = true;
        boolean noNeedOccup = true;
        boolean noNeedIdType = true;

        if (StringUtils.isNotBlank(phoneNum) && StringUtils.equalsIgnoreCase(phoneNum, "Y")) {
           noNeedPhoneNum = false;
        }
        if (StringUtils.isNotBlank(enName) && StringUtils.equalsIgnoreCase(enName, "Y")) {
           noNeedEnName = false;
        }
        if (StringUtils.isNotBlank(email) && StringUtils.equalsIgnoreCase(email, "Y")) {
           noNeedEmail = false;
        }
        if (StringUtils.isNotBlank(occup) && StringUtils.equalsIgnoreCase(occup, "Y")) {
           noNeedOccup = false;
        }
        if (StringUtils.isNotBlank(idType) && StringUtils.equalsIgnoreCase(idType, "Y")) {
           noNeedIdType = false;
        }

        if (noNeedPhoneNum && noNeedEnName && noNeedEmail && noNeedOccup && noNeedIdType) {
           isNotNeedSupply = true;
        }
        return isNotNeedSupply;
    }


    /**
     * 
     * @Description:目的地 单酒店 境外酒店下单,子单设置货币类型和汇率快照到大字段中 
     * @author Wangsizhi
     * @date 2017-5-23 下午5:43:03
     */
    private void initDestBuForeighHotelItem(OrdOrderDTO order) {
        boolean flag = false;
        logger.info("initDestBuForeighHotelItem order:" + order.getOrderId());
        if (order.hasNeedPrepaid() 
                && CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode()) 
                && BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()) {
            flag = true;
        }
        if((CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode())
        		||CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode()))
        		&&BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
        		&&BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())){
        	flag=true;
        }
        List<Long> allowProductList = new ArrayList<Long>();
        allowProductList.add(new Long(1059660));
        allowProductList.add(new Long(58962));
        allowProductList.add(new Long(45504));
        
        Long productId = order.getProductId();
        
        if (allowProductList.contains(productId)) {
            flag = true;
        }
       
        List<OrdOrderItem> orderItemList = order.getOrderItemList();
        if( null != orderItemList && !orderItemList.isEmpty()){
        	for( Long allowProductId : allowProductList ){
        		OrdOrderPack oop = order.getOrderPackByProductId(allowProductId);
        		if( null != oop ){
        			flag = true;
        		}
        	}
        }
        if(flag)
        {
            //单酒店 只有一个子单            
            if (null != orderItemList && orderItemList.size() > 0) {
                for (OrdOrderItem ordOrderItem : orderItemList) {
                    Long suppGoodsId = ordOrderItem.getSuppGoodsId();
                    Date specDate = DateUtil.toYMDDate(new Date());
                                      
                    ResponseBody<HotelCurrencyInfoVstVo> response = new ResponseBody<>();
                    RequestBody<Map<String, Object>> request = new RequestBody<>();
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("suppGoodsId", suppGoodsId);
                    paramMap.put("specDate", specDate);
                    
                    List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
                    List<Date> visitTimeList = new ArrayList<Date>();
                    if (null != orderHotelTimeRateList && orderHotelTimeRateList.size() > 0) {
                        for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : orderHotelTimeRateList) {
                            visitTimeList.add(ordOrderHotelTimeRate.getVisitTime());
                        }
                    }
                    paramMap.put("visitTimeList", visitTimeList);
                    
                    request.setT(paramMap);
                    request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
                    
                    logger.info("Goods get foreign settlement price:" + suppGoodsId);
                    logger.info("specDate:" + specDate);
                    logger.info("visitTimeList~~~~~~~~");
                    
                    for (Date date : visitTimeList) {
                        logger.info(date.toString());
                    }
                    
                    response = hotelGoodsTimePriceQVstApiRemote.findCurrencyCodeBySuppGoodsId(request);
                    HotelCurrencyInfoVstVo hotelCurrencyInfo = response.getT();
                    
                    logger.info("hotelCurrencyInfo = " + hotelCurrencyInfo);
                    
                    if (null != hotelCurrencyInfo) {
                        String currencyCode = hotelCurrencyInfo.getCurrencyCode();
                        BigDecimal exRateBig = hotelCurrencyInfo.getCashSellRate();
                        Long firstDaySettlement = hotelCurrencyInfo.getFirstDaySettlement();
                        Long totalFSettlement = hotelCurrencyInfo.getTotalFSettlement();
                        Map<String, Long> dailySettlement = hotelCurrencyInfo.getDailySettlement();
                        if (null != exRateBig) {
                            ordOrderItem.putContent("currencyCode", currencyCode);
                            ordOrderItem.putContent("cashSellRate", exRateBig);
                            ordOrderItem.putContent("firstDaySettlement", firstDaySettlement);
                            ordOrderItem.putContent("totalFSettlement", totalFSettlement);
                            if (null != dailySettlement && dailySettlement.size() > 0) {
                                ordOrderItem.putContent("dailySettlement", JSONUtil.bean2Json(dailySettlement));                                
                            }else{
                                logger.info("dailySettlement is null or size is zero suppGoodsId: " + suppGoodsId);
                            }
                        }
                    }
                    
                    logger.info("OrderItemId" + ordOrderItem.getOrderItemId());
                    logger.info("currencyCode:" + ordOrderItem.getContentValueByKey("currencyCode"));
                    logger.info("cashSellRate:" + ordOrderItem.getContentValueByKey("cashSellRate"));
                    logger.info("firstDaySettlement:" + ordOrderItem.getContentValueByKey("firstDaySettlement"));
                    logger.info("totalFSettlement:" + ordOrderItem.getContentValueByKey("totalFSettlement"));
                    logger.info("dailySettlement:" + ordOrderItem.getContentValueByKey("dailySettlement"));
                }
            }
            
        }
        
    }




	@Override
	public String chechOrderPayForOther(BuyInfo buyInfo,
			List<com.lvmama.dest.hotel.trade.hotelcomb.vo.UserCouponVO> userCouponVOList) {
		  // TODO Auto-generated method stub
        String erroFlag="";
        //根据用户NO获取奖金账户和现金账户信息

        if((buyInfo.getBonusAmountHidden()!=null&&buyInfo.getBonusAmountHidden().intValue()>0) ||(buyInfo.getCashAmountHidden()!=null&& buyInfo.getCashAmountHidden().intValue()>0)){
            VstCashAccountVO  vstCashAccountVO=  ordUserOrderServiceAdapter.queryMoneyAccountByUserId(buyInfo.getUserNo());
            Long bonusBalance=vstCashAccountVO.getNewBonusBalance();//获取奖金余额
            Long MaxPayMoney=vstCashAccountVO.getMaxPayMoney();//获取可用于支付的现金余额
            if(buyInfo.getBonusAmountHidden()!=null&&bonusBalance.intValue()<buyInfo.getBonusAmountHidden().intValue()){
                logger.error(buyInfo.getUserNo()+"您的账户奖金金额发生变化,请重新输入"+"账户奖金余额"+bonusBalance.intValue()+"该笔订单需要支付的金额"+buyInfo.getBonusAmountHidden().intValue());

                erroFlag="您的账户奖金金额发生变化,请重新输入";
                return erroFlag;
            }
            if(buyInfo.getCashAmountHidden()!=null&& MaxPayMoney.intValue()<buyInfo.getCashAmountHidden().intValue()){
                logger.error(buyInfo.getUserNo()+"您的账户奖金金额发生变化,请重新输入"+"账户存款余额"+MaxPayMoney.intValue()+"该笔订单需要存款的金额"+buyInfo.getCashAmountHidden().intValue());

                erroFlag="您的账户存款金额发生变化,请重新输入";
                return erroFlag;
            }

        }


        if (CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())) {
         // .b  List<UserCouponVO> userCouponVOList=orderService.getUserCouponVOList(buyInfo);
          List<UserCouponVO> listTmp=new ArrayList<UserCouponVO>();
          List<UserCouponVO> userCouponVoList=new ArrayList<UserCouponVO>();
           for(com.lvmama.dest.hotel.trade.hotelcomb.vo.UserCouponVO userCounponVo:userCouponVOList){
        	
        	com.lvmama.vst.pet.vo.UserCouponVO petUserCouponVO = new com.lvmama.vst.pet.vo.UserCouponVO();
			EnhanceBeanUtils.copyProperties(userCounponVo, petUserCouponVO);
			userCouponVoList.add(petUserCouponVO);
           }
            for(UserCouponVO c:userCouponVoList){
                if(StringUtil.isNotEmptyString(c.getValidInfo())){
                    erroFlag="优惠券("+c.getCouponCode()+"),"+c.getValidInfo();
                    return erroFlag;
                }

                for(UserCouponVO cc:buyInfo.getUserCouponVoList()){
                    if(cc.getCouponCode().equals(c.getCouponCode())){
                        listTmp.add(c);
                    }
                }
            }
            buyInfo.setUserCouponVoList(listTmp);
        }

        if(CollectionUtils.isNotEmpty(buyInfo.getGiftCardList())){
            Map<String, String> map =new HashMap<String, String>();
            for(CardInfo c:buyInfo.getGiftCardList()){
                String keystr = c.getCardNo()+"_lvmama";
                try {
                    map.put(c.getCardNo(), DESCoder.decrypt(c.getPassWd(),keystr));
                } catch (Exception e) {

                    logger.error("DES 解密失败");
                    erroFlag="礼品卡密码解密失败";
                    return erroFlag;
                }
            }
            List<CardInfo> listGifCardInfo=null;
                try {
                    listGifCardInfo =payPaymentServiceAdapter.getLvmamaStoredCardListByCardNo(map);
                } catch (Exception e) {
                    erroFlag="获取礼品卡信息失败";
                    return erroFlag;

                }
            if (CollectionUtils.isEmpty(listGifCardInfo) || (listGifCardInfo.size()!=buyInfo.getGiftCardList().size())) {
                erroFlag="礼品卡验证结果与选择礼品卡不匹配";
                return erroFlag;
            }
            for(CardInfo c:listGifCardInfo){
                if ("0".equals(c.getStatus())) {
                    erroFlag=c.getBakWord();
                    return erroFlag;
                }
            }

        }

        if(CollectionUtils.isNotEmpty(buyInfo.getStoreCardList())){
            List<String> listCardNo =new ArrayList<String>();
            for(CardInfo c:buyInfo.getStoreCardList()){
                listCardNo.add(c.getCardNo());
            }
            List<CardInfo> listtStoreCardInfo=null;
                try {
                    listtStoreCardInfo =payPaymentServiceAdapter.getStoredCardListByCardNo(listCardNo);
                } catch (Exception e) {
                    erroFlag="获取储值卡信息失败";
                    return erroFlag;

                }
            if (CollectionUtils.isEmpty(listtStoreCardInfo) || (listtStoreCardInfo.size()!=buyInfo.getStoreCardList().size())) {
                erroFlag="储值卡验证结果与选择礼品卡不匹配";
                return erroFlag;
            }
            for(CardInfo c:listtStoreCardInfo){
                if ("0".equals(c.getStatus())) {
                    erroFlag=c.getBakWord();
                    return erroFlag;
                }
            }

        }


        return erroFlag;
	}

	@Override
	public ResultMessage checkFlightTicket(BuyInfo buyInfo, String flightTicketPrice) {
		ResultMessage msg = ResultMessage.createResultMessage();
		converOldCurise(buyInfo);
        OrdOrderDTO order = new OrdOrderDTO(buyInfo);
        //初始化游玩日期
    	initBuyInfo(order.getBuyInfo());
    	//初始化订单基本信息
        initOrderBase(order);
    	//xiaorui add begin
    	String orderDisneyInfo=buyInfo.getDisneyOrderInfo();
    	logger.info("order check shanghaiDisney showticket info :" + orderDisneyInfo);
    	if(StringUtils.isNotEmpty(orderDisneyInfo)){
    		order.setDisneyOrderInfo(orderDisneyInfo);
    	}

    	//初始化pack和item
    	doInitOrderItems(order);
    	Map<String, Object> attributes = new HashMap<String, Object>();
    	long startLVFTime = System.nanoTime();
    	if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
    		Long productId = buyInfo.getProductId();
    		if(!OrderLvfTimePriceServiceImpl.isAutoPackProductOrder(buyInfo)&&!orderBookServiceDataUtil.isAutoPackTrafficProduct(productId)) {
    			orderLvfTimePriceServiceImpl.initPriceByRemoteLVFNew(order.getOrderItemList(),attributes);
    		}else{
    			msg.raise("checkFlightTicket--------验舱验价不需要提示提高");
    			return msg;
    		}
    	}else{
    		msg.raise("checkFlightTicket--------验舱验价不需要提示提高");
			return msg;
    	}
    	logger.info("对接机票验仓验价_orderLvfTimePriceServiceImpl.initPriceByRemoteLVFNew_[" + (System.nanoTime() - startLVFTime) / 1000000 + "] milliseconds.["+buyInfo.getProductId()+"]");
    	logger.info(ComLogUtil.printTraceInfo("OrderBookServiceImpl.checkFlightTicket", "对接机票验仓验价", "orderLvfTimePriceServiceImpl.initPriceByRemoteLVFNew[" + buyInfo.getProductId() + "]", (System.nanoTime() - startLVFTime) / 1000000));
    	Map<String, String> flightTicketPriceMap = new HashMap<String, String>();
		JSONObject json = JSONObject.fromObject(flightTicketPrice);
		flightTicketPriceMap=(Map<String, String>) JSONObject.toBean(json,Map.class);
		Long cost=0l;
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			String amount = flightTicketPriceMap.get(orderItem.getSuppGoodsId().toString());
			if(amount!=null){
				Long oldTotalAmount = Long.valueOf(amount);
				if(oldTotalAmount!=null){
					cost+=orderItem.getTotalAmount()-oldTotalAmount;
				}
			}
		}
		if(cost>0){
			logger.info("checkFlightTicket-------cost:"+cost);
			attributes.put("cost", cost);
			msg.setAttributes(attributes);
		}else{
			msg.raise("checkFlightTicket--------验舱验价不需要提示提高");
		}
		return msg;
	}
    /**
     * 1.包含开放品类
     * 2.预付(驴妈妈)
     * 3.下单渠道 ,剔除后台下单
     * @param order
     * @return
     */
    private boolean isRouteToNewWorkflow(OrdOrderDTO order) {
        return this.vstOrderRouteService.isRouteToNewWorkflow(order.getCategoryId()) && order.hasNeedPrepaid()
                && (!Constants.DISTRIBUTOR_2.equals(order.getDistributorId()));
    }
	
	
    private void printLog(OrdOrderDTO order, String processKey){
        StringBuffer bs=null;
        BuyInfo buyInfo=order.getBuyInfo();
        if(null!=buyInfo){
            bs=new StringBuffer();
            bs.append("工作流观察下单参数>");
            Long productId=buyInfo.getProductId();
            bs.append("productId=").append(productId).append(",");
            List<Item> items=buyInfo.getItemList();
            if(null!=items&&!items.isEmpty()){
                bs.append("goodsId=[");
                for(Item item:items){
                    Long goodsId=item.getGoodsId();
                    bs.append(goodsId).append(",");
                }
                bs.append("]");
            }
            bs.setLength(0);
            logger.info(bs.toString()+",processKey"+processKey);
        }
    }

    @Override
    public ResultHandle saveNewOrderPerson(Long orderId, BuyInfo buyInfo) {
        ResultHandle result = new ResultHandle();
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
        if(order==null){
            result.setMsg("订单不存在");
            return result;
        }
        if(order.isCancel()){
            result.setMsg("订单已经取消");
            return result;
        }


		OrdOrderDTO orderDTO = new OrdOrderDTO(buyInfo);
		
		List<OrdPerson> travellersList = ordPersonService.getOrderPersonListWithAddress(orderId, OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
		if(travellersList.isEmpty()){
			throwIllegalException("未添加游客不能再操作");
		}
		List<Person> personList = orderDTO.getBuyInfo().getTravellers();
		orderDTO.getBuyInfo().setTravellers(null);
		List<OrdPerson> ordPersonList = new ArrayList<OrdPerson>();
		personInitBussiness.initAdditionalTravel(orderDTO, ordPersonList);
		orderDTO.setOrdTravellerList(travellersList);
		orderDTO.setOrdPersonList(ordPersonList);
		orderDTO.getBuyInfo().setTravellers(personList);

		logger.info("OrderBookServiceImpl.saveNewOrderPerson.ordPersonList=" + ordPersonList);
        List<OrdOrderItem> orderItemList = orderUpdateService.queryOrderItemByOrderId(orderId);
        for(OrdOrderItem orderItem:orderItemList){
            List<BuyInfo.ItemPersonRelation> personRelation = personInitBussiness.getPersonRelation(buyInfo, orderItem, null);
            orderItem.setOrdItemPersonRelationList(personInitBussiness.initPersonRelation(orderDTO,personRelation));
            orderDTO.addOrderItem(orderItem);
        }
        try {
            calcBlackList(orderDTO);
        } catch (Exception e) {
            logger.error("{}", e);
            result.setMsg(e);
            return result;
        }

        orderSaveService.savePersonAndRelation(orderId,orderDTO);

        //保存预订人和联系人相关的订单查询信息
        if(CollectionUtils.isNotEmpty(ordPersonList)) {
            OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();

            for(OrdPerson person:ordPersonList){
                if(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(person.getPersonType())) {
                    orderQueryInfo.setOrderId(orderId);
                    orderQueryInfo.setBookerName(person.getFullName());
                    orderQueryInfo.setBookerMobile(person.getMobile());
                }
                if(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(person.getPersonType())) {
                    orderQueryInfo.setOrderId(orderId);
                    orderQueryInfo.setContactName(person.getFullName());
                    orderQueryInfo.setContactMobile(person.getMobile());
                    orderQueryInfo.setContactPhone(person.getPhone());
                    orderQueryInfo.setContactEmail(person.getEmail());
                }
            }
//          if(orderQueryInfo.getOrderId() != null) {
//              orderQueryInfoService.updateQueryInfoByOrderId(orderQueryInfo);
//          }
        }
        return result;
    }
}
