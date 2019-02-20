package com.lvmama.vst.order.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import com.lvmama.comm.search.vst.vo.RouteBean;
import com.lvmama.vst.back.biz.po.BizFlight;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdTrafficFlight;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.order.web.vo.OrderLineProductVO;

/**
 * Created by zhouyanqun on 2016/4/11.
 * com.lvmama.vst.order.web.OrderLineProductQueryAction的辅助类，主要做数据复制方面的功能
 */
@Component("orderLineProductQueryUtil")
public class OrderLineProductQueryUtil {
    private static final Log log = LogFactory.getLog(OrderLineProductQueryUtil.class);
    private static final String datePatternHHmm = "HH:mm";
    /**
     * 根据BizFlight和ProdTrafficFlight生成FlightNoVo
     * @param flightNoVo: 如果传入的值为空，则新建一个FlightNoVo，否则对传入的这个FlightNoVo赋值
     * */
    public FlightNoVo fillFlightNoVo(FlightNoVo flightNoVo, BizFlight bizFlight, ProdTrafficFlight prodTrafficFlight, ProdProductBranch prodProductBranch) {
        if(flightNoVo == null){
            flightNoVo = new FlightNoVo();
        }

        flightNoVo.setSeatCode(BizFlight.CABIN.getCode(prodProductBranch.getBranchName()));//舱位
        flightNoVo.setSeatName(prodProductBranch.getBranchName());//舱位
        flightNoVo.setFlightNo(prodTrafficFlight.getFlightNo());//航班号
        flightNoVo.setCompanyCode(bizFlight.getAirline()+"");//航空公司
        flightNoVo.setCompanyName(bizFlight.getAirlineString());//航空公司
        flightNoVo.setPlaneCode(bizFlight.getAirplane()+"");//机型编号
        flightNoVo.setGoTime(bizFlight.getStartTime()==null?null: DateUtil.toDate(bizFlight.getStartTime(), "HH:mm"));//出发时间 日期格式
        flightNoVo.setArriveTime(bizFlight.getArriveTime()==null?null:DateUtil.toDate(bizFlight.getArriveTime(), "HH:mm"));//到达时间
        flightNoVo.setFlyTime(bizFlight.getFlightTime());
        flightNoVo.setFlyTimeStr(String.valueOf(bizFlight.getFlightTime()));//飞行时间
        flightNoVo.setFromAirPort(bizFlight.getStartAirportString());//始发机场
        flightNoVo.setToAirPort(bizFlight.getArriveAirportString());//到达机场
        flightNoVo.setGoodsId(prodProductBranch.getSuppGoodsList().get(0).getSuppGoodsId()); //goodId
        //出发/到达城市，如果prodTrafficFlight.getFromCityName()为空，则取bizFlight.getStartDistrictString()
        String fromCityName = StringUtils.isBlank(prodTrafficFlight.getFromCityName())?bizFlight.getStartDistrictString():prodTrafficFlight.getFromCityName();
        String toCityName = StringUtils.isBlank(prodTrafficFlight.getToCityName())?bizFlight.getArriveDistrictString():prodTrafficFlight.getToCityName();
        flightNoVo.setFromCityName(fromCityName);//出发城市
        flightNoVo.setToCityName(toCityName);//到达城市
        //出发/到达航战楼
        flightNoVo.setStartTerminal(bizFlight.getStartTerminal());
        flightNoVo.setArriveTerminal(bizFlight.getArriveTerminal());

        return flightNoVo;
    }



    /**
     * 修改短信模板的时候，增加了一些String类型的参数来传递时间，现在需要把这个时间转换回去
     * */
    public void convertTime(Collection<BuyInfo.Product> productCollection) {
        if(CollectionUtils.isEmpty(productCollection)){
            return;
        }
        Iterator<BuyInfo.Product> productIterator = productCollection.iterator();
        while (productIterator.hasNext()){
            BuyInfo.Product product = productIterator.next();
            List<BuyInfo.Item> itemList = product.getItemList();
            if(CollectionUtils.isEmpty(itemList)){
                continue;
            }
            Iterator<BuyInfo.Item> itemIterator = itemList.iterator();
            while (itemIterator.hasNext()){
                BuyInfo.Item item = itemIterator.next();
                if(item == null){
                    continue;
                }
                List<FlightNoVo> additionalFlightVoList = item.getAdditionalFlightNoVoList();
                if(CollectionUtils.isEmpty(additionalFlightVoList)){
                    continue;
                }
                Iterator<FlightNoVo> flightNoVoIterator = additionalFlightVoList.iterator();
                while (flightNoVoIterator.hasNext()){
                    FlightNoVo flightNoVo = flightNoVoIterator.next();
                    if(flightNoVo == null){
                        continue;
                    }
                    convertFlightDate(flightNoVo);
                }
            }
        }
    }

    /**
     * 转换FlightNoVo中的时间字符串，因为这些字条串仅仅在传值的时候用，所以收到以后就要转换成日期
     * */
    private void convertFlightDate(FlightNoVo flightNoVo) {
        String goTimeStr = flightNoVo.getGoTimeStr();
        Date goTimeDate = flightNoVo.getGoTime();
        String arriveTimeStr = flightNoVo.getArriveTimeStr();
        Date arriveTime = flightNoVo.getArriveTime();
        if(StringUtils.isNotBlank(goTimeStr) && goTimeDate == null){
            try {
                goTimeDate = DateUtil.toDate(goTimeStr, datePatternHHmm);
            } catch (Exception e) {
            }
            flightNoVo.setGoTime(goTimeDate);
        }
        if(StringUtils.isNotBlank(arriveTimeStr) && arriveTime == null){
            try {
                arriveTime = DateUtil.toDate(arriveTimeStr, datePatternHHmm);
            } catch (Exception e) {
            }
            flightNoVo.setArriveTime(arriveTime);
        }
    }

    /**
     * 把产品搜索结果bean转化为vo
     * */
    public OrderLineProductVO toOrderLineProductVO(RouteBean routeIndexBean){
        if(routeIndexBean == null){
            return null;
        }
        OrderLineProductVO orderLineProductVO = new OrderLineProductVO();
        try {
            BeanUtils.copyProperties(routeIndexBean, orderLineProductVO);
        } catch (BeansException e) {
            log.error("Error copy search result for product " + routeIndexBean.getProductId());
        }
        return orderLineProductVO;
    }

    /**
     * 把产品搜索结果bean的list转化为vo的list
     * */
    public List<OrderLineProductVO> toOrderLineProductVOList(List<RouteBean> routeIndexBeenList){
        if(CollectionUtils.isEmpty(routeIndexBeenList)){
            return null;
        }
        List<OrderLineProductVO> orderLineProductVOList = new ArrayList<OrderLineProductVO>();
        for (RouteBean routeIndexBean : routeIndexBeenList) {
            OrderLineProductVO orderLineProductVO = toOrderLineProductVO(routeIndexBean);
            orderLineProductVOList.add(orderLineProductVO);
        }
        return orderLineProductVOList;
    }
}
