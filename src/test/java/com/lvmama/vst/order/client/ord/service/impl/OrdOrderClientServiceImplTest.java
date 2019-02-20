package com.lvmama.vst.order.client.ord.service.impl;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.vo.OrdOrderVo;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;

/**
 * Created by zhouyanqun on 2016/6/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrdOrderClientServiceImplTest {
    @Resource(name = "orderServiceRemote")
    private OrderService orderClientService;


    @Test
    public void testCreateOrder(){
        BuyInfo buyInfo = createBuyInfo();
        String operatorId = "123456";
        ResultHandleT<OrdOrder> result=orderClientService.createOrder(buyInfo, operatorId);
    }

    private static BuyInfo createBuyInfo() {
        BuyInfo buyInfo;
        String jsonString = "{\"submitOrderFlag\":false,\"distributionId\":3,\"ip\":\"10.200.2.51\",\"travellers\":[{\"fullName\":\"李易峰\",\"mobile\":\"15927353056\",\"idType\":\"ID_CARD\",\"idNo\":\"110101199308270073\",\"birthday\":\"1993-08-27\",\"peopleType\":\"PEOPLE_TYPE_ADULT\",\"buyInsuranceFlag\":\"N\",\"saveFlag\":\"false\",\"notEmpty\":true,\"birthdayStr\":\"1993-08-27\"},{\"fullName\":\"吴亦凡\",\"mobile\":\"15927353056\",\"idType\":\"ID_CARD\",\"idNo\":\"110101199308270153\",\"birthday\":\"1993-08-27\",\"peopleType\":\"PEOPLE_TYPE_ADULT\",\"buyInsuranceFlag\":\"N\",\"saveFlag\":\"false\",\"notEmpty\":true,\"birthdayStr\":\"1993-08-27\"},{\"fullName\":\"井柏然\",\"mobile\":\"15927353056\",\"idType\":\"ID_CARD\",\"idNo\":\"110101201008270135\",\"birthday\":\"2010-08-27\",\"peopleType\":\"PEOPLE_TYPE_CHILD\",\"buyInsuranceFlag\":\"N\",\"saveFlag\":\"false\",\"notEmpty\":true,\"birthdayStr\":\"2010-08-27\"}],\"booker\":{\"fullName\":\"ceshi\",\"mobile\":\"15927353056\",\"buyInsuranceFlag\":\"N\",\"saveFlag\":\"false\",\"notEmpty\":true},\"contact\":{\"fullName\":\"李易峰\",\"mobile\":\"15927353056\",\"email\":\"125689@sina.com\",\"buyInsuranceFlag\":\"N\",\"saveFlag\":\"false\",\"notEmpty\":true},\"productList\":[{\"itemList\":[{\"goodsId\":2575457,\"visitTime\":\"2016-07-12\",\"quantity\":0,\"checkStockQuantity\":0,\"shareTotalStock\":0,\"shareDayLimit\":0,\"adultAmt\":65000,\"childAmt\":62000,\"ownerQuantity\":0,\"adultQuantity\":2,\"childQuantity\":1,\"gapQuantity\":0,\"flightNoVo\":{\"flightNo\":\"MU5119\",\"planeCode\":\"上海虹桥机场 - 北京首都机场 MU5119 经济舱 Z舱\",\"fromAirPort\":\"虹桥机场\",\"toAirPort\":\"首都机场\",\"fromCityName\":\"上海\",\"toCityName\":\"北京\",\"throughFlag\":false,\"goTime\":\"2016-07-12 05:00:00\",\"foodSupport\":false,\"remain\":100,\"flyTimeStr\":\"无法计算\"},\"totalAmount\":0,\"totalSettlementPrice\":0,\"visitTimeDate\":\"2016-07-12 12:00:00\",\"totalPersonQuantity\":3},{\"goodsId\":2575457,\"visitTime\":\"2016-07-13\",\"quantity\":0,\"checkStockQuantity\":0,\"shareTotalStock\":0,\"shareDayLimit\":0,\"adultAmt\":78900,\"childAmt\":62000,\"ownerQuantity\":0,\"adultQuantity\":2,\"childQuantity\":1,\"gapQuantity\":0,\"flightNoVo\":{\"flightNo\":\"MU5160\",\"planeCode\":\"北京首都机场 - 上海虹桥机场 MU5160 经济舱 R舱\",\"fromAirPort\":\"首都机场\",\"toAirPort\":\"虹桥机场\",\"fromCityName\":\"北京\",\"toCityName\":\"上海\",\"throughFlag\":false,\"goTime\":\"2016-07-13 09:40:00\",\"foodSupport\":false,\"remain\":100,\"flyTimeStr\":\"无法计算\"},\"totalAmount\":0,\"totalSettlementPrice\":0,\"visitTimeDate\":\"2016-07-13 12:00:00\",\"totalPersonQuantity\":3},{\"goodsId\":1441039,\"visitTime\":\"2016-07-11\",\"quantity\":1,\"checkStockQuantity\":1,\"shareTotalStock\":0,\"shareDayLimit\":0,\"productCategoryId\":18,\"ownerQuantity\":0,\"adultQuantity\":2,\"childQuantity\":1,\"gapQuantity\":0,\"totalAmount\":30000,\"totalSettlementPrice\":30000,\"visitTimeDate\":\"2016-07-11 12:00:00\",\"totalPersonQuantity\":3},{\"goodsId\":1441464,\"visitTime\":\"2016-07-15\",\"quantity\":3,\"checkStockQuantity\":3,\"shareTotalStock\":0,\"shareDayLimit\":0,\"productCategoryId\":18,\"ownerQuantity\":0,\"adultQuantity\":0,\"childQuantity\":0,\"gapQuantity\":0,\"totalAmount\":24000,\"totalSettlementPrice\":24000,\"visitTimeDate\":\"2016-07-11 12:00:00\",\"totalPersonQuantity\":0}],\"itemMap\":{},\"visitTime\":\"2016-07-11\",\"adultQuantity\":2,\"childQuantity\":1,\"quantity\":0,\"productId\":380145,\"visitTimeDate\":\"2016-07-11 12:00:00\"}],\"userId\":\"11044\",\"userNo\":11044,\"isTestOrder\":\"\\u0000\",\"promotionMap\":{},\"itemMap\":{},\"personRelationMap\":{},\"categoryId\":18,\"productId\":380145,\"additionalTravel\":\"true\",\"storeCardList\":[],\"giftCardList\":[],\"userCouponVoList\":[],\"smsLvmamaFlag\":\"Y\",\"additionMap\":{},\"productMap\":{},\"visitTime\":\"2016-07-11\",\"adultQuantity\":2,\"childQuantity\":1,\"maxAdultQuantity\":0,\"maxChildQuantity\":0,\"travPersonQuantity\":0,\"spreadQuantity\":0,\"quantity\":0}";
//        jsonString = "{\"distributionId\":\"2342\",\"distributionChannel\":0,\"distributorCode\":\"3\",\"distributorName\":\"0\",\"needGuarantee\":\"0\",\"needInvoice\":\"0\",\"remark\":\"0\",\"ip\":\"10.112.1.39\",\"productList\":[{\"visitTime\":\"2016-08-30\",\"adultQuantity\":\"1\",\"childQuantity\":\"0\",\"quantity\":\"0\",\"productId\":\"636165\",\"buCode\":\"0\",\"taobaoETicket\":\"0\",\"itemList\":[{\"additionalFlightNoVoList\":[{\"flightNo\":\"HO1252\",\"planeCode\":\"北京首都机场-上海虹桥机场HO1252经济舱R舱\",\"fromAirPort\":\"首都机场\",\"toAirPort\":\"虹桥机场\",\"fromCityName\":\"北京\",\"toCityName\":\"虹桥机场\",\"flightType\":1,\"throughFlag\":false,\"companyName\":\"吉祥航空\",\"goTime\":\"2016-08-04 06:50:00\",\"arriveTime\":\"2016-08-04 09:05:00\",\"seatName\":\"经济舱\",\"foodSupport\":false,\"flyTimeStr\":\"02小时15分\",\"flyTime\":135,\"startTerminal\":\"T3\",\"arriveTerminal\":\"T2\"}],\"adultAmt\":0,\"adultQuantity\":1,\"backDate\":\"2016-08-05\",\"buCode\":\"\",\"checkStockQuantity\":1,\"childAmt\":0,\"childQuantity\":0,\"circusActInfo\":null,\"content\":\"\",\"detailId\":12105,\"disneyItemOrderInfo\":\"\",\"displayTime\":\"\",\"flightNoVo\":null,\"gapQuantity\":0,\"goodType\":\"traffic\",\"goodsId\":1641896,\"hotelAdditation\":null,\"hotelcombOptions\":[],\"isDisneyGood\":\"\",\"itemPersonRelationList\":[],\"mainItem\":\"true\",\"orderSubType\":\"\",\"ownerQuantity\":0,\"price\":\"\",\"priceTypeList\":[],\"productCategoryId\":15,\"quantity\":1,\"roomMaxInPerson\":0,\"routeRelation\":\"PACK\",\"settlementPrice\":\"\",\"shareDayLimit\":0,\"shareTotalStock\":0,\"sharedStockList\":[],\"taobaoETicket\":0,\"toDate\":\"2016-08-30\",\"totalAmount\":0,\"totalPersonQuantity\":1,\"totalSettlementPrice\":0,\"visitTime\":\"2016-08-30\",\"visitTimeDate\":\"2016-08-30\",\"wifiAdditation\":null},{\"additionalFlightNoVoList\":[],\"adultAmt\":0,\"adultQuantity\":1,\"backDate\":\"\",\"buCode\":\"\",\"checkStockQuantity\":1,\"childAmt\":0,\"childQuantity\":0,\"circusActInfo\":null,\"content\":\"\",\"detailId\":null,\"disneyItemOrderInfo\":\"\",\"displayTime\":\"\",\"flightNoVo\":null,\"gapQuantity\":0,\"goodType\":\"hotel\",\"goodsId\":1659668,\"hotelAdditation\":{\"leaveTime\":\"2016-08-31\",\"arrivalTime\":\"14:00\"},\"hotelcombOptions\":[],\"isDisneyGood\":\"\",\"itemPersonRelationList\":[],\"mainItem\":\"MAIN\",\"orderSubType\":\"\",\"ownerQuantity\":0,\"price\":\"\",\"priceTypeList\":[],\"productCategoryId\":15,\"quantity\":1,\"roomMaxInPerson\":0,\"routeRelation\":\"PACK\",\"settlementPrice\":\"\",\"shareDayLimit\":0,\"shareTotalStock\":0,\"sharedStockList\":[],\"taobaoETicket\":0,\"toDate\":\"\",\"totalAmount\":0,\"totalPersonQuantity\":1,\"totalSettlementPrice\":0,\"visitTime\":\"2016-08-30\",\"visitTimeDate\":\"2016-08-30\",\"wifiAdditation\":null}]}],\"travellerDelayFlag\":\"N\",\"orderTravellerConfirm\":null,\"travellers\":[{\"birthPlace\":\"\",\"birthday\":\"2004-07-25\",\"birthdayStr\":\"2004-07-25\",\"buyInsuranceFlag\":\"N\",\"email\":\"fdsfds@qq.com\",\"expDate\":null,\"fax\":\"\",\"firstName\":\"Zhou\",\"fullName\":\"李易峰\",\"gender\":\"MAN\",\"idNo\":\"WEQWE111\",\"idType\":\"HUZHAO\",\"issueDate\":null,\"issued\":\"\",\"lastName\":\"Larry\",\"mobile\":\"15927353056\",\"nationality\":\"\",\"notEmpty\":true,\"outboundPhone\":\"\",\"peopleType\":\"PEOPLE_TYPE_ADULT\",\"phone\":\"\",\"receiverId\":\"8a48814b55d9b05d0155de6508370023\",\"roomNo\":0,\"saveFlag\":\"false\"}],\"userId\":\"40288add3ee5b68a013ee9bc83670254\",\"userNo\":\"3895882\",\"buCode\":\"0\",\"faxMemo\":\"0\",\"categoryId\":\"29\",\"productId\":\"636165\",\"orderTotalPrice\":\"0\",\"additionalTravel\":\"true\",\"discountAmount\":\"0\",\"oughtAmount\":\"0\",\"anonymityBookFlag\":\"0\",\"sameVisitTime\":\"0\",\"visitTime\":\"2016-08-30\",\"adultQuantity\":\"1\",\"spreadQuantity\":\"0\",\"quantity\":\"0\",\"waitPayment\":\"0\",\"sendContractFlag\":\"0\"}";
        jsonString = "{\n" +
                "    \"submitOrderFlag\": false,\n" +
                "    \"distributionId\": 3,\n" +
                "    \"ip\": \"10.112.6.57\",\n" +
                "    \"travellers\": [{\n" +
                "        \"fullName\": \"测X\",\n" +
                "        \"mobile\": \"13225656666\",\n" +
                "        \"idType\": \"HUZHAO\",\n" +
                "        \"idNo\": \"DFG457DFGH\",\n" +
                "        \"birthday\": \"1989-11-07\",\n" +
                "        \"peopleType\": \"PEOPLE_TYPE_ADULT\",\n" +
                "        \"buyInsuranceFlag\": \"N\",\n" +
                "        \"saveFlag\": \"false\"\n" +
                "    }],\n" +
                "    \"booker\": {\n" +
                "        \"fullName\": \"zhangxpp\",\n" +
                "        \"mobile\": \"13225656666\",\n" +
                "        \"buyInsuranceFlag\": \"N\",\n" +
                "        \"saveFlag\": \"false\"\n" +
                "    },\n" +
                "    \"contact\": {\n" +
                "        \"fullName\": \"测X\",\n" +
                "        \"mobile\": \"13225656666\",\n" +
                "        \"email\": \"0CC175B9C0F1@qq.com\",\n" +
                "        \"buyInsuranceFlag\": \"N\",\n" +
                "        \"saveFlag\": \"false\"\n" +
                "    },\n" +
                "    \"itemList\": [],\n" +
                "    \"productList\": [{\n" +
                "        \"itemList\": [{\n" +
                "            \"goodsId\": 2575457,\n" +
                "            \"visitTime\": \"2016-11-12\",\n" +
                "            \"quantity\": 0,\n" +
                "            \"adultAmt\": 50300,\n" +
                "\t    \"adultSettlementAmt\":500,\n" +
                "\t    \"childSettlementAmt\":200,\n" +
                "            \"ownerQuantity\": 0,\n" +
                "            \"adultQuantity\": 1,\n" +
                "            \"childQuantity\": 0,\n" +
                "            \"gapQuantity\": 0,\n" +
                "            \"flightNoVo\": {\n" +
                "                \"flightNo\": \"HU7602\",\n" +
                "                \"planeCode\": \"上海虹桥机场 - 北京首都大机场 HU7602 经济舱 T舱\",\n" +
                "                \"fromAirPort\": \"虹桥机场\",\n" +
                "                \"toAirPort\": \"首都大机场\",\n" +
                "                \"fromCityName\": \"上海\",\n" +
                "                \"toCityName\": \"北京\",\n" +
                "                \"flightType\": 1,\n" +
                "                \"throughFlag\": false,\n" +
                "                \"companyName\": \"海南航空\",\n" +
                "                \"goTime\": null,\n" +
                "                \"arriveTime\": \"2016-11-12 23:25:00\",\n" +
                "                \"seatName\": \"经济舱\",\n" +
                "                \"foodSupport\": false,\n" +
                "                \"flyTimeStr\": \"02小时30分\",\n" +
                "                \"flyTime\": 150,\n" +
                "                \"startTerminal\": \"T2\",\n" +
                "                \"arriveTerminal\": \"T1\"\n" +
                "            },\n" +
                "            \"additionalFlightNoVoList\": [{\n" +
                "                \"flightNo\": \"HU7602\",\n" +
                "                \"planeCode\": \"上海虹桥机场 - 北京首都大机场 HU7602 经济舱 T舱\",\n" +
                "                \"fromAirPort\": \"虹桥机场\",\n" +
                "                \"toAirPort\": \"首都大机场\",\n" +
                "                \"fromCityName\": \"上海\",\n" +
                "                \"toCityName\": \"北京\",\n" +
                "                \"flightType\": 1,\n" +
                "                \"throughFlag\": false,\n" +
                "                \"companyName\": \"海南航空\",\n" +
                "                \"goTime\": \"2016-11-12 20:55:00\",\n" +
                "                \"arriveTime\": \"2016-11-12 23:25:00\",\n" +
                "                \"seatName\": \"经济舱\",\n" +
                "                \"foodSupport\": false,\n" +
                "                \"flyTimeStr\": \"02小时30分\",\n" +
                "                \"flyTime\": 150,\n" +
                "                \"startTerminal\": \"T2\",\n" +
                "                \"arriveTerminal\": \"T1\"\n" +
                "            }],\n" +
                "            \"toDate\": \"2016-11-12\"\n" +
                "        }, {\n" +
                "            \"goodsId\": 2575457,\n" +
                "            \"visitTime\": \"2016-11-15\",\n" +
                "            \"quantity\": 0,\n" +
                "            \"adultAmt\": 53300,\n" +
                "            \"ownerQuantity\": 0,\n" +
                "            \"adultQuantity\": 1,\n" +
                "            \"childQuantity\": 0,\n" +
                "\t    \"adultSettlementAmt\":500,\n" +
                "\t    \"childSettlementAmt\":200,\n" +
                "            \"gapQuantity\": 0,\n" +
                "            \"flightNoVo\": {\n" +
                "                \"flightNo\": \"CZ6412\",\n" +
                "                \"planeCode\": \"北京首都大机场 - 上海虹桥机场 CZ6412 经济舱 Z舱\",\n" +
                "                \"fromAirPort\": \"首都大机场\",\n" +
                "                \"toAirPort\": \"虹桥机场\",\n" +
                "                \"fromCityName\": \"北京\",\n" +
                "                \"toCityName\": \"上海\",\n" +
                "                \"flightType\": 2,\n" +
                "                \"throughFlag\": false,\n" +
                "                \"companyName\": \"南方航空\",\n" +
                "                \"goTime\": \"2016-11-15 06:40:00\",\n" +
                "                \"arriveTime\": \"2016-11-15 09:00:00\",\n" +
                "                \"seatName\": \"经济舱\",\n" +
                "                \"foodSupport\": false,\n" +
                "                \"flyTimeStr\": \"02小时20分\",\n" +
                "                \"flyTime\": 140,\n" +
                "                \"startTerminal\": \"T2\",\n" +
                "                \"arriveTerminal\": \"T2\"\n" +
                "            },\n" +
                "            \"additionalFlightNoVoList\": [{\n" +
                "                \"flightNo\": \"CZ6412\",\n" +
                "                \"planeCode\": \"北京首都大机场 - 上海虹桥机场 CZ6412 经济舱 Z舱\",\n" +
                "                \"fromAirPort\": \"首都大机场\",\n" +
                "                \"toAirPort\": \"虹桥机场\",\n" +
                "                \"fromCityName\": \"北京\",\n" +
                "                \"toCityName\": \"上海\",\n" +
                "                \"flightType\": 2,\n" +
                "                \"throughFlag\": false,\n" +
                "                \"companyName\": \"南方航空\",\n" +
                "                \"goTime\": \"2016-11-15 06:40:00\",\n" +
                "                \"arriveTime\": \"2016-11-15 09:00:00\",\n" +
                "                \"seatName\": \"经济舱\",\n" +
                "                \"foodSupport\": false,\n" +
                "                \"flyTimeStr\": \"02小时20分\",\n" +
                "                \"flyTime\": 140,\n" +
                "                \"startTerminal\": \"T2\",\n" +
                "                \"arriveTerminal\": \"T2\"\n" +
                "            }],\n" +
                "            \"backDate\": \"2016-11-15\"\n" +
                "        }, {\n" +
                "            \"goodsId\": 1653610,\n" +
                "            \"visitTime\": \"2016-11-12\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"productCategoryId\": 18,\n" +
                "            \"ownerQuantity\": 0,\n" +
                "            \"adultQuantity\": 1,\n" +
                "            \"childQuantity\": 0,\n" +
                "            \"gapQuantity\": 0,\n" +
                "            \"hotelAdditation\": {\n" +
                "                \"arrivalTime\": \"14:00\",\n" +
                "                \"leaveTime\": \"2016-11-15\"\n" +
                "            },\n" +
                "            \"totalAmount\": 3300,\n" +
                "            \"totalSettlementPrice\": 3000,\n" +
                "            \"aeroHotelPromotionPrice\": 300,\n" +
                "            \"areoHotelTimeRate\": [{\n" +
                "                \"visitTime\": \"2016-11-12 00:00:00\",\n" +
                "                \"price\": 1100,\n" +
                "                \"settlementPrice\": 1000\n" +
                "            }, {\n" +
                "                \"visitTime\": \"2016-11-13 00:00:00\",\n" +
                "                \"price\": 1100,\n" +
                "                \"settlementPrice\": 1000\n" +
                "            }, {\n" +
                "                \"visitTime\": \"2016-11-14 00:00:00\",\n" +
                "                \"price\": 1100,\n" +
                "                \"settlementPrice\": 1000\n" +
                "            }]\n" +
                "        }],\n" +
                "        \"itemMap\": {},\n" +
                "        \"visitTime\": \"2016-11-12\",\n" +
                "        \"adultQuantity\": 1,\n" +
                "        \"childQuantity\": 0,\n" +
                "        \"quantity\": 0,\n" +
                "        \"productId\": 636165,\n" +
                "        \"productName\": \"上海 往返 北京 自由行（2016-11-12至2016-11-15，1成人）\"\n" +
                "    }],\n" +
                "    \"userId\": \"ff8080813fe5b734013ff11dfc3f254a\",\n" +
                "    \"userNo\": 4123260,\n" +
                "    \"isTestOrder\": \"\\u0000\",\n" +
                "    \"promotionMap\": {},\n" +
                "    \"itemMap\": {},\n" +
                "    \"personRelationMap\": {},\n" +
                "    \"prodPackageItemDateMap\": {},\n" +
                "    \"categoryId\": 29,\n" +
                "    \"productId\": 636165,\n" +
                "    \"additionalTravel\": \"true\",\n" +
                "    \"storeCardList\": [],\n" +
                "    \"giftCardList\": [],\n" +
                "    \"userCouponVoList\": [],\n" +
                "    \"smsLvmamaFlag\": \"Y\",\n" +
                "    \"additionMap\": {},\n" +
                "    \"productMap\": {},\n" +
                "    \"visitTime\": \"2016-11-12\",\n" +
                "    \"adultQuantity\": 1,\n" +
                "    \"childQuantity\": 0,\n" +
                "    \"maxAdultQuantity\": 0,\n" +
                "    \"maxChildQuantity\": 0,\n" +
                "    \"travPersonQuantity\": 0,\n" +
                "    \"spreadQuantity\": 0,\n" +
                "    \"quantity\": 0\n" +
                "}";
        buyInfo = GsonUtils.fromJson(jsonString, BuyInfo.class);
        return buyInfo;

    }
    
    @Test
    public void loadOrderWithItemByOrderId(){
    	Long orderId=200618116L;
    	ResultHandleT<OrdOrder> result=orderClientService.loadOrderWithItemByOrderId(orderId);
    	OrdOrder order=result.getReturnContent();
    	assertNotNull(order);
    }

    @Test
    public void testNewCompositeQuery(){
    	Page<OrdOrderVo> page = Page.page(10, 1);
    	ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		//关联订单子项表
		condition.getOrderFlagParam().setOrderItemTableFlag(true);
		//关联游客表
		//condition.getOrderFlagParam().setOrderPersonTableFlag(true);
		//关联单子项的酒店使用情况
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);
		//设置酒店离店时间按时间排序
		condition.getOrderRelationSortParam().setOrderHotelTimeRateSort("  ORD_ORDER_HOTEL_TIME_RATE.VISIT_TIME  ASC  ");
		//关联打包表
		condition.getOrderFlagParam().setOrderPackTableFlag(true);
		//关联寄件地址表
		//condition.getOrderFlagParam().setOrderAddressTableFlag(true);
		//condition.getOrderFlagParam().setOrderAmountItemTableFlag(true);
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setDistributionChannel(2L);
		Set<Long> orderIds = new HashSet<Long>();
//		orderIds.add(200621989L);
//		orderIds.add(200621990L);
		orderIds.add(20065469L);
		orderIndentityParam.setOrderIds(orderIds);
		condition.setOrderIndentityParam(orderIndentityParam);
		ResultHandleT<Page<OrdOrderVo>> result = orderClientService.newCompositeQuery(page, condition);
		page = result.getReturnContent();
		assertNotNull(page);
    }
}
