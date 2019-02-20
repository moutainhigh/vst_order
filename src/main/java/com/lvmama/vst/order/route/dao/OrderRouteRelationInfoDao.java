package com.lvmama.vst.order.route.dao;

import com.lvmama.vst.comm.mybatis.MyBatisDao;

import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderRouteRelationInfoDao extends MyBatisDao {
    public OrderRouteRelationInfoDao() {
        super("ORDER_ROUTE_RELATION_INFO");
    }

    public int insert(OrderRouteRelationInfo record) {
        return super.insert("insert", record);
    }

    public OrderRouteRelationInfo queryOrderRouteRelationInfo(Map<String, Object> paramMap){
        List<OrderRouteRelationInfo> selectByParam = super.queryForList("selectByParam", paramMap);
        if (CollectionUtils.isEmpty(selectByParam)) {
            return null;
        }
        //取第1个
        return selectByParam.get(0);
    }
}
