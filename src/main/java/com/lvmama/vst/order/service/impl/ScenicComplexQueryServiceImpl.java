package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrdTicketPerformDetail;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.ScenicComplexQueryService;

@Service("scenicComplexQuery")
public class ScenicComplexQueryServiceImpl implements ScenicComplexQueryService {
    @Autowired
    private IComplexQueryService complexQueryService;

    @Override
    public List<OrdOrder> queryOrderListByCondition(OrderMonitorCnd orderMonitorCnd, ComplexQuerySQLCondition condition) {
        return complexQueryService.queryOrderListByCondition(orderMonitorCnd, condition);
    }

    @Override
    public Long queryOrderCountByCondition(OrderMonitorCnd orderMonitorCnd) {
        return complexQueryService.queryOrderCountByCondition(orderMonitorCnd);
    }

    @Override
    public List<OrdOrder> queryOrderListByCondition(ComplexQuerySQLCondition condition) {
        return complexQueryService.queryOrderListByCondition(condition);
    }

    @Override
    public Long checkOrderCountFromReadDB(ComplexQuerySQLCondition condition) {
        return complexQueryService.checkOrderCountFromReadDB(condition);
    }

    @Override
    public List<OrdOrder> checkOrderListFromReadDB(ComplexQuerySQLCondition condition) {
        return complexQueryService.checkOrderListFromReadDB(condition);
    }

    @Override
    public Long queryOrderCountByCondition(ComplexQuerySQLCondition condition) {
        return complexQueryService.queryOrderCountByCondition(condition);
    }

    @Override
    public OrdOrder queryOrderByOrderId(Long orderId) {
        return complexQueryService.queryOrderByOrderId(orderId);
    }

    @Override
    public List<OrdTicketPerform> findOrdTicketPerformList(Long orderId) {
        return complexQueryService.findOrdTicketPerformList(orderId);
    }

    @Override
    public List<OrdTicketPerform> selectByOrderItem(Long orderItemId) {
        return complexQueryService.selectByOrderItem(orderItemId);
    }

    @Override
    public List<OrdTicketPerform> selectByOrderItems(List<Long> orderItemIds) {
        return complexQueryService.selectByOrderItems(orderItemIds);
    }

    @Override
    public List<OrdTicketPerformDetail> selectPerformDetailByOrderItem(Long orderItemId) {
        return complexQueryService.selectPerformDetailByOrderItem(orderItemId);
    }

    @Override
    public List<Map<String, Object>> findAllObjectsBySql(Map<String, Object> params) {
        return complexQueryService.findAllObjectsBySql(params);
    }

    @Override
    public List<Map<String, Object>> findAllObjectsBySqlFromReadDB(Map<String, Object> params) {
        return complexQueryService.findAllObjectsBySqlFromReadDB(params);
    }

    @Override
    public List<Long> findNeedGenWorkflowOrders() {
        return complexQueryService.findNeedGenWorkflowOrders();
    }

    @Override
    public List<Long> findNeedCreateSupplierOrders() {
        return complexQueryService.findNeedCreateSupplierOrders();
    }

    @Override
    public List<Long> findNeedCancelSupplierOrders() {
        return complexQueryService.findNeedCancelSupplierOrders();
    }

    @Override
    public List<Long> findNeedTiggerPayProcOrders() {
        return complexQueryService.findNeedTiggerPayProcOrders();
    }

    @Override
    public int updatePayProcTriggeredByOrderID(Map<String, Object> paramsMap) {
        return complexQueryService.updatePayProcTriggeredByOrderID(paramsMap);
    }

    @Override
    public String findMobileId(Long orderId) {
        return complexQueryService.findMobileId(orderId);
    }

    @Override
    public List<OrdOrder> checkOnlyOrderListFromReadDB(ComplexQuerySQLCondition condition) {
        return complexQueryService.checkOnlyOrderListFromReadDB(condition);
    }
}
