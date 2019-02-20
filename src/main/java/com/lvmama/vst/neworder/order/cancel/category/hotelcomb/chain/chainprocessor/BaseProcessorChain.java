package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.chainprocessor;

import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.timeprice.service.impl.NewOrderHotelCompTimePriceServiceImpl;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dengcheng on 17/4/12.
 */
@Component
public class BaseProcessorChain {

    @Autowired
    private NewHotelComOrderBussiness newHotelComOrderBussiness;
    @Autowired
    private OrdOrderStockDao orderStockDao;
    @Autowired
    private NewHotelComOrderInitService newHotelComOrderInitService;
    @Autowired
    private NewOrderHotelCompTimePriceServiceImpl orderHotelComp2HotelTimePriceService;


    @Autowired
    private ComActivitiRelationService comActivitiRelationService;

    private static ConcurrentMap<String, Long> bookUniqueMap = new ConcurrentHashMap<String, Long>();

    @Autowired
    private OrderSaveService orderSaveService;

    @Autowired
    private IOrdOrderTrackingService ordOrderTrackingService;

    @Autowired
    protected IComplexQueryService complexQueryService;


    @Resource(name = "orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;

    @Autowired
    private IOrderUpdateService orderUpdateService;


    @Autowired
    private OrderTicketNoTimePriceServiceImpl orderTicketNoTimePriceServiceImpl;


    @Resource
    IOrdOrderService orderService;

}
