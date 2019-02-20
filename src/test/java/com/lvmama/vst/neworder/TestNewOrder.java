package com.lvmama.vst.neworder;
import com.lvmama.dest.hotel.trade.vo.base.Person;

import com.google.common.collect.Lists;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;

import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.hotelcomb.vo.CalAmountResponse;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.interfaces.IHotelCombTradeOrderService;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderCheckRequest;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderPriceListVo;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/3/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class TestNewOrder {

    @Resource
    IHotelCombOrderService hotelCombOrderService;

    @Resource
    IHotelCombProductService hotelCombProductService;

    @Resource
    IHotelCombTradeOrderService hotelCombTradeOrderService;

    @Test
    public  void testOrder(){
        RequestBody<List<HotelCombBuyInfoVo>> requestBody = new RequestBody<List<HotelCombBuyInfoVo>>();
        List<HotelCombBuyInfoVo> hotelCombBuyInfoVos = Lists.newArrayList();
        HotelCombBuyInfoVo hotelCombBuyInfoVo = new HotelCombBuyInfoVo();
//        hotelCombBuyInfoVo.setGoodsList();
//
        HotelCombBuyInfoVo.GoodsItem goodsItem = hotelCombBuyInfoVo.new GoodsItem();
        goodsItem.setQuantity(2L);

        goodsItem.setPricePlanId(9L);
        goodsItem.setCheckInDate(new Date());
        goodsItem.setGoodsId(2583696L);
        List<HotelCombBuyInfoVo.GoodsItem> goodsList = Lists.newArrayList();
        goodsList.add(goodsItem);

        hotelCombBuyInfoVo.setGoodsList(goodsList);
        hotelCombBuyInfoVos.add(hotelCombBuyInfoVo);
        requestBody.setT(hotelCombBuyInfoVos);
        ResponseBody<List<CalAmountResponse>> responseBody =  hotelCombOrderService.calGoodsAmount(requestBody);
        System.out.println("=========================");
        for (CalAmountResponse calAmount :responseBody.getT() ) {
            System.out.println(calAmount.getItemTotalAmount());
        }
        System.out.println("=========================");
    }

    @Test
    public void testOrderAmountCal(){
        HotelCombTradeBuyInfoVo hotelCombBuyInfoVo = new HotelCombTradeBuyInfoVo();

        HotelCombTradeBuyInfoVo.GoodsItem goodsItem =  hotelCombBuyInfoVo.new GoodsItem();

        HotelCombTradeBuyInfoVo.ProductItem productItem =  hotelCombBuyInfoVo.new ProductItem();

        goodsItem.setQuantity(2L);
        goodsItem.setPricePlanId(15L);
        goodsItem.setGoodsId(2583922L);
        goodsItem.setSubCategoryId(32L);
        goodsItem.setCheckInDate(new Date());
        goodsItem.setCategoryId(1L);
        goodsItem.setSubCategoryId(32L);
        productItem.setProductId(1000041L);

        List<HotelCombTradeBuyInfoVo.GoodsItem> goodsItems = Lists.newArrayList();
        goodsItems.add(goodsItem);

        List<HotelCombTradeBuyInfoVo.ProductItem> productItems = Lists.newArrayList();
        productItems.add(productItem);






        hotelCombBuyInfoVo.setGoodsList(goodsItems);
        hotelCombBuyInfoVo.setProductList(productItems);

        com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo> request  = new
                com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo>();

        request.setUserId("ff8080812f2dcd1e012f2e95afb203d5");
        request.setUserNo(829815L);

        request.setTFlowStyle(hotelCombBuyInfoVo, NewOrderConstant.VST_ORDER_TOKEN);
        com.lvmama.dest.hotel.trade.common.ResponseBody<OrderPriceListVo> orderPriceListVoResponseBody =  hotelCombTradeOrderService.calOrderPriceList(request);
        System.out.println("==================== price"+orderPriceListVoResponseBody.getT().getTotalAmount());
        Assert.assertNotNull(orderPriceListVoResponseBody.getT());
    }

    @Test
    public void testCreateOrder(){

        try {
            // 产品ID
            Long productId = 1003858l;
            // 酒套餐商品Id
            Long suppGoodsId = 2584173l;
            // 保险的商品ID
//            Long insuranceIds[] = new Long[]{2583289l};

            com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo> request  = new
                    com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo>();
            HotelCombTradeBuyInfoVo hotelCombBuyInfoVo = new HotelCombTradeBuyInfoVo();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            // 添加酒套餐商品
            if(true){
                if(hotelCombBuyInfoVo.getGoodsList() == null){
                    // 如果参数中商品的集合为空，则创建一个集合
                    List<HotelCombTradeBuyInfoVo.GoodsItem> goodsList = new ArrayList<HotelCombTradeBuyInfoVo.GoodsItem>();
                    hotelCombBuyInfoVo.setGoodsList(goodsList);
                }
                // 品类的ID
                Long categoryId = 32l;
                // 二级品类的ID
                Long subCategoryId = 32l;
                // 创建一个商品对象
                HotelCombTradeBuyInfoVo.GoodsItem goodsItem = hotelCombBuyInfoVo.new GoodsItem();
                // 商品ID
                goodsItem.setGoodsId(suppGoodsId);
                // 品类
                goodsItem.setCategoryId(categoryId);
                // 子品类
                goodsItem.setSubCategoryId(subCategoryId);
                // 价格计划ID
                goodsItem.setPricePlanId(34l);
                // 数量等于套餐人数乘以预定分数
                goodsItem.setQuantity(4l);
                // 入住时间
                goodsItem.setCheckInDate(df.parse("2017-05-1"));
                // 离开时间
                goodsItem.setCheckOutDate(df.parse("2017-05-2"));
                // 添加到参数中的商品列表中
                hotelCombBuyInfoVo.getGoodsList().add(goodsItem);
            }

//            // 判断用户是否选择保险
//            if(insuranceIds != null && insuranceIds.length > 0){
//                if(hotelCombBuyInfoVo.getGoodsList() == null){
//                    // 如果参数中商品的集合为空，则创建一个集合
//                    List<HotelCombTradeBuyInfoVo.GoodsItem> goodsList = new ArrayList<HotelCombTradeBuyInfoVo.GoodsItem>();
//                    hotelCombBuyInfoVo.setGoodsList(goodsList);
//                }
//                // 遍历保险ID
//                for(Long insuranceId : insuranceIds){
//                    // 品类ID
//                    Long categoryId = 3l;
//                    // 二级品类ID
//                    Long subCategoryId = 3l;
//                    // 创建一个保险商品对象
//                    HotelCombTradeBuyInfoVo.GoodsItem goodsItem = hotelCombBuyInfoVo.new GoodsItem();
//                    // 商品ID
//                    goodsItem.setGoodsId(insuranceId);
//                    // 品类
//                    goodsItem.setCategoryId(categoryId);
//                    // 商品二级品类
//                    goodsItem.setSubCategoryId(subCategoryId);
//                    // 数量等于套餐人数乘以预定分数
//                    goodsItem.setQuantity(4l);
//                    // 入住时间
//                    goodsItem.setCheckInDate(df.parse("2017-05-1"));
//                    // 离开时间
//                    goodsItem.setCheckOutDate(df.parse("2017-05-2"));
//                    // 添加到参数中的商品列表中
//                    hotelCombBuyInfoVo.getGoodsList().add(goodsItem);
//                }
//            }

            Person person = new Person();
            person.setFullName("test001");
            person.setMobile("15155882067");
            person.setPersonType(Person.PERSON_TYPE.BOOKER.name());

            List<Person> travellers = new ArrayList<Person>();
            travellers.add(person);
            hotelCombBuyInfoVo.setTravellers(travellers);


            // 产品列表
            HotelCombTradeBuyInfoVo.ProductItem productItem = hotelCombBuyInfoVo.new ProductItem();
            productItem.setProductId(productId);
            List<HotelCombTradeBuyInfoVo.ProductItem> productItems = Lists.newArrayList();
            productItems.add(productItem);
            hotelCombBuyInfoVo.setProductList(productItems);

            // 请求验证
            request.setTFlowStyle(hotelCombBuyInfoVo, "Sb^VyHlgPwpJP89ZWPh$ctAdyxFGS45E");
            request.setUserId("ff8080812f2dcd1e012f2e95afb203d5");
            request.setUserNo(829815L);
            request.setDistributionChannel(1l);
            request.setDistributionId(3L);
            request.setDistributorCode("0");
            request.setToken("Sb^VyHlgPwpJP89ZWPh$ctAdyxFGS45E");
            com.lvmama.dest.hotel.trade.common.ResponseBody<HotelOrdOrderVo> responseBody =  hotelCombTradeOrderService.submitOrder(request);
            System.out.println("========"+responseBody.getT().getOrderId());
        } catch (ParseException e) {
            e.printStackTrace();
        }
//
//        com.lvmama.dest.hotel.trade.common.ResponseBody<HotelOrdOrderVo> responseBody =  hotelCombTradeOrderService.submitOrder(request);
//        System.out.println("========"+responseBody.getT().getOrderId());
//        Assert.assertNotNull(responseBody.getT());
    }


    @Test
    public void testCheckOrder(){
        HotelCombTradeBuyInfoVo hotelCombBuyInfoVo = new HotelCombTradeBuyInfoVo();

        HotelCombTradeBuyInfoVo.GoodsItem goodsItem =  hotelCombBuyInfoVo.new GoodsItem();

        HotelCombTradeBuyInfoVo.ProductItem productItem =  hotelCombBuyInfoVo.new ProductItem();

        goodsItem.setQuantity(2L);
        goodsItem.setPricePlanId(15L);
        goodsItem.setGoodsId(2583922L);
        goodsItem.setSubCategoryId(32L);
        goodsItem.setCheckInDate(new Date());
        goodsItem.setCategoryId(1L);
        goodsItem.setSubCategoryId(32L);
        productItem.setProductId(1000041L);

        List<HotelCombTradeBuyInfoVo.GoodsItem> goodsItems = Lists.newArrayList();
        goodsItems.add(goodsItem);

        List<HotelCombTradeBuyInfoVo.ProductItem> productItems = Lists.newArrayList();
        productItems.add(productItem);






        hotelCombBuyInfoVo.setGoodsList(goodsItems);
        hotelCombBuyInfoVo.setProductList(productItems);

        com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo> request  = new
                com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo>();

//        request.setUserId("ff8080812f2dcd1e012f2e95afb203d5");
//        request.setUserNo(829815L);

        request.setTFlowStyle(hotelCombBuyInfoVo, NewOrderConstant.VST_ORDER_TOKEN);

        hotelCombTradeOrderService.checkOrder(request);
//        System.out.println(responseBody.getCode());
    }

    @Test
    public void testCalPrice(){
        try {
            // 产品ID
            Long productId = 1000041l;
            // 酒套餐商品Id
            Long suppGoodsId = 2583895l;
            // 保险的商品ID
            Long insuranceIds[] = new Long[]{2583289l};

            com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo> request  = new
                    com.lvmama.dest.hotel.trade.common.RequestBody<HotelCombTradeBuyInfoVo>();
            HotelCombTradeBuyInfoVo hotelCombBuyInfoVo = new HotelCombTradeBuyInfoVo();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            // 添加酒套餐商品
            if(true){
                if(hotelCombBuyInfoVo.getGoodsList() == null){
                    // 如果参数中商品的集合为空，则创建一个集合
                    List<HotelCombTradeBuyInfoVo.GoodsItem> goodsList = new ArrayList<HotelCombTradeBuyInfoVo.GoodsItem>();
                    hotelCombBuyInfoVo.setGoodsList(goodsList);
                }
                // 品类的ID
                Long categoryId = 32l;
                // 二级品类的ID
                Long subCategoryId = 32l;
                // 创建一个商品对象
                HotelCombTradeBuyInfoVo.GoodsItem goodsItem = hotelCombBuyInfoVo.new GoodsItem();
                // 商品ID
                goodsItem.setGoodsId(suppGoodsId);
                // 品类
                goodsItem.setCategoryId(categoryId);
                // 子品类
                goodsItem.setSubCategoryId(subCategoryId);
                // 价格计划ID
                goodsItem.setPricePlanId(1l);
                // 数量等于套餐人数乘以预定分数
                goodsItem.setQuantity(4l);
                // 入住时间
                goodsItem.setCheckInDate(df.parse("2017-04-10"));
                // 离开时间
                goodsItem.setCheckOutDate(df.parse("2017-04-11"));
                // 添加到参数中的商品列表中
                hotelCombBuyInfoVo.getGoodsList().add(goodsItem);
            }

            // 判断用户是否选择保险
            if(insuranceIds != null && insuranceIds.length > 0){
                if(hotelCombBuyInfoVo.getGoodsList() == null){
                    // 如果参数中商品的集合为空，则创建一个集合
                    List<HotelCombTradeBuyInfoVo.GoodsItem> goodsList = new ArrayList<HotelCombTradeBuyInfoVo.GoodsItem>();
                    hotelCombBuyInfoVo.setGoodsList(goodsList);
                }
                // 遍历保险ID
                for(Long insuranceId : insuranceIds){
                    // 品类ID
                    Long categoryId = 3l;
                    // 二级品类ID
                    Long subCategoryId = 3l;
                    // 创建一个保险商品对象
                    HotelCombTradeBuyInfoVo.GoodsItem goodsItem = hotelCombBuyInfoVo.new GoodsItem();
                    // 商品ID
                    goodsItem.setGoodsId(insuranceId);
                    // 品类
                    goodsItem.setCategoryId(categoryId);
                    // 商品二级品类
                    goodsItem.setSubCategoryId(subCategoryId);
                    // 数量等于套餐人数乘以预定分数
                    goodsItem.setQuantity(4l);
                    // 入住时间
                    goodsItem.setCheckInDate(df.parse("2017-04-10"));
                    // 离开时间
                    goodsItem.setCheckOutDate(df.parse("2017-04-11"));
                    // 添加到参数中的商品列表中
                    hotelCombBuyInfoVo.getGoodsList().add(goodsItem);
                }
            }

            Person person = new Person();
            person.setMobile("15155882067");
            List<Person> travellers = new ArrayList<Person>();
            travellers.add(person);
            hotelCombBuyInfoVo.setTravellers(travellers);


            // 产品列表
            HotelCombTradeBuyInfoVo.ProductItem productItem = hotelCombBuyInfoVo.new ProductItem();
            productItem.setProductId(productId);
            List<HotelCombTradeBuyInfoVo.ProductItem> productItems = Lists.newArrayList();
            productItems.add(productItem);
            hotelCombBuyInfoVo.setProductList(productItems);

            // 请求验证
            request.setTFlowStyle(hotelCombBuyInfoVo, "Sb^VyHlgPwpJP89ZWPh$ctAdyxFGS45E");
            request.setUserId("ff8080812f2dcd1e012f2e95afb203d5");
            request.setUserNo(829815L);
            request.setDistributionChannel(1l);
            request.setDistributorCode("0");
            request.setToken("Sb^VyHlgPwpJP89ZWPh$ctAdyxFGS45E");
            com.lvmama.dest.hotel.trade.common.ResponseBody<OrderPriceListVo>  responseBody = hotelCombTradeOrderService.calOrderPriceList(request);
            System.out.println(responseBody);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
