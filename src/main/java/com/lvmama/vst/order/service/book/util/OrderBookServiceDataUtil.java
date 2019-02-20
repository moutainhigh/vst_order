package com.lvmama.vst.order.service.book.util;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.collection.mutable.StringBuilder;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/3/17.
 * 跟com.lvmama.vst.order.service.book.OrderBookServiceImpl相关的数据辅助类
 */
@Service("orderBookServiceDataUtil")
public class OrderBookServiceDataUtil {
    private static final Logger logger = LoggerFactory.getLogger(OrderBookServiceDataUtil.class);
    /*当同一个订单中有多个机票信息时，第二个机票加后缀*/
    private static final String additionalFlightSuffix = ".2";
    private static final String datePatternHHmm = "HH:mm";
    private static final String datePatternFullHHmm = "yyyy-MM-dd HH:mm";
    /**
     * 自动打包交通产品的属性键
     * */
    private static final String autoPackTrafficProductKey = "auto_pack_traffic";

    @Resource
    private ProdProductClientService productClientService;

    /**
     * 将flightNoVo中的数据，填充到orderItem中
     * 由于填充是以key-value的形式填充的，当一次填充多个航班号信息时，可以通过加前缀的方式
     * 如果判断前缀非空，可以调用些方法，加前缀填充，效果类似“flightNo:**** 和 flightNo.2: ******”
     * @param leaveDate:出发日期，可能是去程的日期，也可能是返回日期
     * */
    public void fillOrderItemData(OrdOrderItem orderItem, FlightNoVo flightNoVo, String leaveDate, String suffix){
        if(orderItem == null || flightNoVo == null){
            return;
        }
        if(suffix == null){
            suffix = "";
        }
        StringBuilder logInfoStringBuilder = new StringBuilder();
        //日志信息字符串
        logInfoStringBuilder.append("正在复制航班信息到子订单,出发日期为" + leaveDate + ",航班信息为[").append(flightNoVo.toString()).append("]，子订单商品号(suppGoodsId)为[").append(orderItem.getSuppGoodsId()).append("]");
        logger.info(logInfoStringBuilder.toString());
        //航班号
        if(StringUtils.isNotBlank(flightNoVo.getFlightNo())) {
            if(null != orderItem.getContentMap() && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name() + suffix) && null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name() + suffix)){
                StringBuffer sb = new StringBuffer("");
                String firstFlightNo = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.flightNo.name() + suffix);
                sb.append(firstFlightNo + "-" +flightNoVo.getFlightNo());
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.flightNo.name() + suffix, sb.toString());
            }else{
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.flightNo.name() + suffix, flightNoVo.getFlightNo());
            }

        }
        //出发时间
        String goDateTimeStr = null;
        String arriveTimeStr = null;
        String firstArriveTime = "";
        if(flightNoVo.getGoTime() != null) {
            try {
                String leaveTime = DateUtil.formatDate(flightNoVo.getGoTime(), datePatternHHmm);
                logger.info("设定航班[" + flightNoVo.getFlightNo() + "]出发日期，出发日期是：" + leaveDate + ",出发时间是" + leaveTime);
                goDateTimeStr = generateDateTime(leaveDate, leaveTime);
                //如果出发时间已经存在，说明这是又一次出发。那么此次出发属于中转或者经停
                if(null != orderItem.getContentMap() && !orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.departureTime.name() + suffix)){
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.departureTime.name() + suffix, goDateTimeStr);
                }

            } catch (Exception e) {
                logger.error("设定航班[" + flightNoVo.getFlightNo() + "]出发日期出错，出发日期是：" + leaveDate, e);
            }
        }



        //到达时间，需要把飞行时间跟起飞时间相加，最后得到到达时间
        if(flightNoVo.getArriveTime() != null && StringUtils.isNotBlank(goDateTimeStr)) {
            logger.info("设定航班[" + flightNoVo.getFlightNo() + "]到达日期，出发日期时间是：" + goDateTimeStr + ",飞行时间(分钟)是" + flightNoVo.getFlyTime());
            try {
                Date arriveDate = calculateArriveDate(flightNoVo.getFlyTime(), DateUtil.toDate(goDateTimeStr, datePatternFullHHmm));
                arriveTimeStr = DateUtil.formatDate(arriveDate, datePatternFullHHmm);
                if(null != orderItem.getContentMap() && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.arriveTime.name() + suffix)){
                    firstArriveTime = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.arriveTime.name() + suffix);
                }
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveTime.name() + suffix, arriveTimeStr);
            } catch (Exception e) {
                logger.error("设定航班[" + flightNoVo.getFlightNo() + "]到达日期出错，出发日期是：" + goDateTimeStr, e);
            }
        }

        //出发机场
        if(StringUtils.isNotBlank(flightNoVo.getFromAirPort())) {
            if(null != orderItem.getContentMap() && !orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.fromAirport.name() + suffix)){
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fromAirport.name() + suffix, flightNoVo.getFromAirPort());
            }
        }

        //到达机场
        if(StringUtils.isNotBlank(flightNoVo.getToAirPort())) {
            //如果出发机场已经存在，说明这是又一次出发。那么此次属于中转或者经停
            if(hasFlightChange(orderItem,suffix)){
                if(!flightNoVo.isThroughFlag()){ //非经停，默认中转
                    StringBuffer sb = new StringBuffer("");
                    String firstToAirport = orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.toAirport.name() + suffix).toString();
                    String firstArriveTerminal = null;
                    if(orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name() + suffix)){
                        firstArriveTerminal = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name() + suffix);
                    }
                    String secondArriveTerminal = flightNoVo.getArriveTerminal();
                    if(StringUtils.isBlank(firstToAirport)){
                        firstToAirport = flightNoVo.getFromAirPort();
                    }
                    if(StringUtils.isBlank(firstArriveTerminal)){
                        firstArriveTerminal = flightNoVo.getStartTerminal();
                    }
                    String[] dateAndTime = goDateTimeStr.split(" ");
                    String[] firstArriveTimeArr = firstArriveTime.split(" ");
                    sb.append(firstArriveTimeArr[1] + "到达 " + firstToAirport + firstArriveTerminal + " "+dateAndTime[1] +"出发-"+flightNoVo.getToAirPort() +" "+ secondArriveTerminal);
                    orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.toAirport.name() + suffix, sb.toString());
                }
            }else{
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.toAirport.name() + suffix, flightNoVo.getToAirPort());
            }
        }

        //出发城市
        if(StringUtils.isNotBlank(flightNoVo.getFromCityName())){
            if(null != orderItem.getContentMap() && !orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.departureCity.name() + suffix)){
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.departureCity.name() + suffix, flightNoVo.getFromCityName());
            }
        }
        //到达城市
        if(StringUtils.isNotBlank(flightNoVo.getToCityName())){
            if(hasFlightChange(orderItem,suffix)){
                StringBuffer sb = new StringBuffer("");
                String firstArriveCity = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() + suffix);
                if(StringUtils.isBlank(firstArriveCity)){
                    firstArriveCity = flightNoVo.getFromCityName();
                }
                sb.append("【中转】" + firstArriveCity + "-" +flightNoVo.getToCityName());
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() + suffix, sb.toString());
            }else{
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() + suffix, flightNoVo.getToCityName());
            }
        }

        //出发航站楼
        if(StringUtils.isNotBlank(flightNoVo.getStartTerminal())) {
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.startTerminal.name() + suffix, flightNoVo.getStartTerminal());
        }

        //到达航站楼
        if(StringUtils.isNotBlank(flightNoVo.getArriveTerminal())) {
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.arriveTerminal.name() + suffix, flightNoVo.getArriveTerminal());

        }
        //航空公司
        if(StringUtils.isNotBlank(flightNoVo.getCompanyName())){
            if(null != orderItem.getContentMap() && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.airCompany.name() + suffix)){
                StringBuffer sb = new StringBuffer("");
                String firstAirCompany = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.airCompany.name() + suffix);
                sb.append(firstAirCompany +"-" +flightNoVo.getCompanyName());
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.airCompany.name() + suffix, sb.toString());
            }else{
                orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.airCompany.name() + suffix, flightNoVo.getCompanyName());
            }

        }
        //往返类型
        if(flightNoVo.getFlightType() != null){
            orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.flightType.name() + suffix, flightNoVo.getFlightType());
        }
    }

    //特定条件下，判断是否有中转或经停
    private boolean hasFlightChange(OrdOrderItem orderItem, String suffix){
        if(null != orderItem && null != orderItem.getContentMap()
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.fromAirport.name()+suffix)
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.toAirport.name()+suffix)
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.departureTime.name()+suffix)
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.arriveTime.name()+suffix)
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.departureCity.name() + suffix)
                && orderItem.getContentMap().containsKey(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() + suffix)){
            if(null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.fromAirport.name()+suffix)
            || null !=  orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.toAirport.name()+suffix)
            || null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.departureTime.name()+suffix)
            || null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.arriveTime.name()+suffix)
            || null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.departureCity.name() + suffix)
            || null != orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.arriveCity.name() + suffix)){
                return true;
            }
        }
        return false;
    }

    /**
     * 将日期字符和时间字符拼接成日期时间
     * */
    private String generateDateTime(String leaveDate, String leaveTime) {
        Date date = null, time = null;
        //如果传入的日期字符串为空串或者非法字符串，则设置日期为初始日期(1970-1-1),否则将传入的日期转化成yyyy-MM-dd格式的日期
        try {
            date = DateUtil.toDate(leaveDate, DateUtil.SIMPLE_DATE_FORMAT);
        } catch (Exception e) {
            date = new Date(0l);
        }
        //如果传入的时间字符串为空串或者非法字符串，则设置时间为初始时间(00:00),否则将传入的日期转化成HH:mm格式的日期
        try {
            time = DateUtil.toDate(leaveTime, datePatternHHmm);
        } catch (Exception e) {
            time = DateUtil.toDate("00:00", datePatternHHmm);
        }
        String dateTimeStr = DateUtil.formatDate(date, DateUtil.SIMPLE_DATE_FORMAT) + " " + DateUtil.formatDate(time, datePatternHHmm);
        return dateTimeStr;
    }

    /**
     * 计算航班的到达时间
     * @param flyTime:飞行时间(单位是分钟)
     * @param departureTime:起飞时间
     * */
    public Date calculateArriveDate(long flyTime, Date departureTime){
        long flyTimeCopy = 0l;
        try {
            flyTimeCopy = Long.valueOf(flyTime);
        } catch (Exception e) {
        }
        if(flyTimeCopy <= 0){
            return departureTime;
        }
        try {
            //飞行时间，单位为分钟
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(departureTime);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + flyTimeCopy * 60 * 1000);
            return calendar.getTime();
        } catch (Exception e) {
            return departureTime;
        }
    }

    /**
     * 判断传入的订单是否不为“交通+X”的订单，仅当订单存在，而且类型不为“交通+X”时，返回true,否则返回false
     * */
    public boolean isNotAeroHotel(OrdOrderDTO order){
        if(order == null || order.getBuyInfo() == null){
            return false;
        }
        Long categoryId = order.getBuyInfo().getCategoryId();
        if(categoryId == null) {
            return false;
        }
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue() == categoryId.longValue()){
            return false;
        }
        return true;
    }

    /**
     * 判断传入的子订单是否是机票或者其它机票类型，只有订单存在，且类型为机票/其它机票时，才返回true，否则返回false
     * */
    public boolean isAeroPlaneOrOther(OrdOrderItem orderItem) {
        if(orderItem == null || orderItem.getCategoryId() == null){
            return  false;
        }

        if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().longValue() != orderItem.getCategoryId().longValue()
                && BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!= orderItem.getCategoryId().longValue()){
            return false;
        }
        return true;
    }

    /**
     * 填充orderItem中的机票信息
     * */
    public void fillOrderItemFlightsData(OrdOrderItem orderItem){
        if(orderItem == null) {
            logger.warn("子订单信息为空，无法填充机票信息，请检查");
            return;
        }
        BuyInfo.Item buyInfoItem = orderItem.getItem();
        if(buyInfoItem == null){
            return;
        }
        List<FlightNoVo> additionalFlightNoVoList = buyInfoItem.getAdditionalFlightNoVoList();
        if(CollectionUtils.isEmpty(additionalFlightNoVoList)){
            return;
        }
        for (FlightNoVo additionalFlightNoVo : additionalFlightNoVoList) {
            if(additionalFlightNoVo == null || additionalFlightNoVo.getFlightType() == null){
                continue;
            }
            try {
                BuyInfo.Item item = orderItem.getItem();
                if (item != null) {
                    logger.info("航班信息列表的size为" + additionalFlightNoVoList.size());
                    String tripType = "单程";//单程还是往返
                    String currentTripType = "去程";//当前航班是去程还是返程
                    String suffix = null;//后缀,当航班信息不止一个时，返航信息要加后缀,插入数据库时是类似“flightNo:**** 和 flightNo.2: ******”的效果
                    String flightTakeOffDateStr = item.getToDate();//航班出发日期，可能是去程的出发日期，也可能是返程的出发日期

                    if(additionalFlightNoVoList.size() > 1){
                        tripType = "往返";
                        if(additionalFlightNoVo.getFlightType().longValue() == 2){
                            suffix = additionalFlightSuffix;
                        }
                    }
                    if(additionalFlightNoVo.getFlightType().longValue() == 2){
                        currentTripType = "返程";
                        flightTakeOffDateStr = item.getBackDate();
                    }

                    //打印日志
                    StringBuilder logInfoSb = new StringBuilder();//日志信息，典型输出为“订单航班为往返航班，且本航班是返程，返程日期:2016-04-18”
                    logInfoSb.append("订单航班为").append(tripType).append("航班,本航班是").append(currentTripType).append(",").append(currentTripType).append("日期:").append(flightTakeOffDateStr);
                    logger.info(logInfoSb.toString());

                    //填充数据
                    fillOrderItemData(orderItem, additionalFlightNoVo, flightTakeOffDateStr, suffix);
                }
            } catch (Exception e) {
                logger.warn("复制航班信息到子订单记录时抛出异常,子订单商品suppGoodsId是[" + orderItem.getSuppGoodsId() + "]航班号是[" + additionalFlightNoVo.getFlightNo()+"]", e);
            }
        }
    }

    /**
     * 分销的价格与主站不同，但分销下单时，[ORD_MUL_PRICE_RATE]表中的价格还依然是按照主站的价格来计算的，合同中的价格取也取自这张表，导致分销的订单合同价格不对，此方法用于修改子订单中[ORD_MUL_PRICE_RATE]表对应的数据对象的价格信息，使分销的合同价格正确
     * 此方法仅仅对分销的，而且是供应商打包的跟团游的子订单计算，其它的类型的订单应该放过
     * */
    public void convertDistributorOrderPrice(BuyInfo buyInfo, OrdOrder order){
        if(order == null || buyInfo == null){
            return;
        }
        Long productId = null;
        try {
            productId = order.getProductId();
        } catch (Exception e) {
            logger.error("Error get product id:", e);
        }
        //拼接订单信息字符串，供打印日志用
        StringBuilder orderInfoStringBuilder = new StringBuilder("product:[").append(productId).append("],distributorId:[").append(order.getDistributorId()).append("],distributorCode:[").append(order.getDistributorCode()).append("]");
        String orderInfoString = orderInfoStringBuilder.toString();
        logger.info("Now convert the price of contract, order information is " + orderInfoString);
        if(!isDistributorOrder(order)){
            logger.info("order is not from distributor, order info is " + orderInfoString);
            return;
        }
        logger.info("order is from distributor, order info is " + orderInfoString);
        if(!isSupplierOrder(order)){
            logger.info("order is not packaged by supplier, order info is " + orderInfoString);
            return;
        }
        logger.info("order is packaged by supplier, order info is " + orderInfoString);

        if(!isRouteGroupOrder(order)){
            logger.info("order's type is not route group, " + orderInfoString);
            return;
        }
        logger.info("order's type is route group, the price of contract will change, " + orderInfoString);

        logger.info("Begin to modify the price of order," + orderInfoString);
        try {
            List<BuyInfo.PriceType> priceTypeList = buyInfo.getProductList().get(0).getItemList().get(0).getPriceTypeList();
            logger.info("Order info is " + orderInfoString + ", in buyInfo, size of priceList is:" + priceTypeList.size());
            List<OrdMulPriceRate> ordMulPriceRateList = order.getOrderItemList().get(0).getOrdMulPriceRateList();
            //拼接修改前后的日志信息
            StringBuilder logStringBuilder = new StringBuilder();
            for (BuyInfo.PriceType priceType : priceTypeList) {
                logStringBuilder.append("Modifying the price of order ").append(orderInfoString).append(", price type is[").append(priceType.getPriceKey()).append("]");
                for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
                    if(ordMulPriceRate.getPriceType().equalsIgnoreCase(priceType.getPriceKey())){
                        logStringBuilder.append(",").append("old value is [").append(ordMulPriceRate.getPrice()).append("],new value is [").append(priceType.getPrice()).append("]");
                        ordMulPriceRate.setPrice(priceType.getPrice());
                        break;
                    }
                }
                logger.info(logStringBuilder.toString());
            }
        } catch (Exception e) {
            logger.error("Error modifying the price of distributor order:", e);
        }
    }

    /**
     * 判断是否是分销商的订单
     * */
    public boolean isDistributorOrder(OrdOrder order){
        if(order == null) {
            return false;
        }
        if(order.getDistributorId() == null){
            return false;
        }
        if(order.getDistributorId() != 4){
            return false;
        }
        try {
            //从const.properties文件中取配置的distributorCodes值，把这些值与订单中的distributorCode字段比对
            String distributorCodes = Constant.getInstance().getProperty("distributorCodes");
            String[] distributorCodeArray = StringUtils.split(distributorCodes, ",");
            for (String distributorCode : distributorCodeArray) {
                StringUtils.trimToEmpty(distributorCode);
                if(distributorCode.equalsIgnoreCase(order.getDistributorCode())){
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("取所有分销code时出错", e);
        }
        return false;
    }

    /**
     * 判断是否供应商打包的订单
     * */
    private boolean isSupplierOrder(OrdOrder order){
        if(order == null||order.getOrdOrderPack() == null || order.getOrdOrderPack().hasOwn()){
            return false;
        }
        return true;
    }

    /**
     * 判断是否是跟团游订单
     * */
    private boolean isRouteGroupOrder(OrdOrder order){
        if(order == null || order.getCategoryId() == null || order.getCategoryId() <= 0){
            return false;
        }
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue() != order.getCategoryId().longValue()){
            return false;
        }
        return true;
    }

    /**
     * 判断商品是否是对接机票,仅当商品非空，非是对接机票时，返回true，否则返回false
     * */
    public boolean isApiFlight(SuppGoods suppGoods){
        if(suppGoods == null){
            return false;
        }
        if(!"Y".equalsIgnoreCase(suppGoods.getApiFlag())){
            return false;
        }
        Long categoryId = suppGoods.getCategoryId();
        if(categoryId == null || BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue() != categoryId){
            return false;
        }
        return true;
    }


    /**
     * 判断商品是否是自动打包交通产品的
     * */
    public Boolean isAutoPackTrafficProduct(Long productId) {
        if(productId == null){
            return false;
        }
        if(productId < 0){
            return false;
        }
        try {
            logger.info("Now begin to query product properties for product " + productId);
            ProdProductParam productParam = new ProdProductParam();
            productParam.setProductProp(true);
            ResultHandleT<ProdProduct> prodProduct = productClientService.findProdProductWithBuById(productId, productParam);
            if(prodProduct.getReturnContent() == null){
                return false;
            }
            ProdProduct product = prodProduct.getReturnContent();
            if(product == null){
                return false;
            }
            Map<String, Object> propValueMap = product.getPropValue();
            if(MapUtils.isEmpty(propValueMap)){
                logger.info("propValueMap is empty, product is " + productId);
                return false;
            }
            logger.info("propValueMap is now available, size is " + propValueMap.size() + ",product is " + productId);
            String autoPackTraffic = String.valueOf(propValueMap.get(autoPackTrafficProductKey));
            logger.info("autoPackTraffic of product " + productId + " is " + autoPackTraffic);
            if(StringUtils.equalsIgnoreCase(autoPackTraffic, Constants.Y_FLAG)){
                return true;
            }
        } catch (Exception e){
            logger.error("Error occurs while judging whether product " + productId + "'s traffic package type is auto:", e);
            return false;
        }
        return false;
    }



    /**
     * 判断传入的品类是否是交通工具
     * 目前根据是否是机票判断，后续如果需要把火车票、船票包括进来，需要在些方法中增加品类的判断
     * */
    public boolean isTrafficGoods(Long categoryId){
        if(categoryId == null){
            return false;
        }
        if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().longValue() != categoryId.longValue()
                && BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!= categoryId.longValue()){
            return false;
        }
        return true;
    }
}
