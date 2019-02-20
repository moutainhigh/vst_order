package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrderItemApportionStateDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
@Repository
public class OrderItemApportionStateDaoImpl extends MyBatisDao implements OrderItemApportionStateDao {
    public OrderItemApportionStateDaoImpl() {
        super("ORDER_ITEM_APPORTION_STATE");
    }

    @Override
    public int deleteByPrimaryKey(Long orderItemApportionStateId) {
        return super.delete("deleteByPrimaryKey", orderItemApportionStateId);
    }

    @Override
    public int insert(OrderItemApportionState record) {
        return super.insert("insert", record);
    }

    @Override
    public int insertSelective(OrderItemApportionState record) {
        return super.insert("insertSelective", record);
    }

    @Override
    public OrderItemApportionState selectByPrimaryKey(Long orderItemApportionStateId) {
        return super.get("selectByPrimaryKey", orderItemApportionStateId);
    }

    /**
     * 根据条件查询结果集
     *
     * @param orderItemApportionStateQueryVO 查询参数
     */
    @Override
    public List<OrderItemApportionState> selectListByQueryVO(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO) {
        return super.getList("queryResultListByParam", orderItemApportionStateQueryVO);
    }

    /**
     * 根据条件作废记录
     *
     * @param orderItemApportionStateQueryVO
     */
    @Override
    public int updateByQueryVO4Invalid(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO) {
        return super.update("invalidRecords", orderItemApportionStateQueryVO);
    }

    @Override
    public int updateByPrimaryKeySelective(OrderItemApportionState record) {
        return super.update("updateByPrimaryKeySelective", record);
    }

    @Override
    public int updateByPrimaryKey(OrderItemApportionState record) {
        return super.update("updateByPrimaryKey", record);
    }

    /**
     * 根据参数更新分摊金额
     * create by liyanpeng on 2017/07/04
     */
    @Override
    public int updateAmountByParam(OrderItemApportionState record) {
        return super.update("updateAmountByParam", record);
    }
}
