package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.order.dao.OrdOrderCostSharingItemDao;
import com.lvmama.vst.order.service.OrdOrderCostSharingItemService;
import com.lvmama.vst.order.utils.OrdOrderCostSharingItemUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
@Service
public class OrdOrderCostSharingItemServiceImpl implements OrdOrderCostSharingItemService {
    @Resource
    private OrdOrderCostSharingItemDao orderCostSharingItemDao;

    /**
     * 插入新记录
     *
     * @param orderCostSharingItem
     */
    @Override
    public int saveOrdOrderCostSharingItem(OrdOrderCostSharingItem orderCostSharingItem) {
        return orderCostSharingItemDao.insert(orderCostSharingItem);
    }

    /**
     * 作废原有记录
     * */
    public int updateRecords4Invalid(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO){
        if (! OrdOrderCostSharingItemUtils.checkParam(orderCostSharingItemQueryVO)) {
            throw new RuntimeException("作废记录时，必须指定订单或者子单信息");
        }
        return orderCostSharingItemDao.updateRecords4Invalid(orderCostSharingItemQueryVO);
    }

    /**
     * 作废原订单的分摊记录
     *
     * @param orderId
     */
    @Override
    public int updateOrderApportionRecords4Invalid(Long orderId) {
        if (orderId == null) {
            return 0;
        }
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        orderCostSharingItemQueryVO.setOrderId(orderId);
        return updateRecords4Invalid(orderCostSharingItemQueryVO);
    }

    /**
     * 根据参数查询记录
     *
     * @param orderCostSharingItemQueryVO 查询参数
     */
    @Override
    public List<OrdOrderCostSharingItem> queryOrdOrderCostSharingItemList(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO) {
        if (! OrdOrderCostSharingItemUtils.checkParam(orderCostSharingItemQueryVO)) {
            throw new RuntimeException("查询条件不全");
        }
        return orderCostSharingItemDao.queryList(orderCostSharingItemQueryVO);
    }

    /**
     * 更新分销(门票)子单的分摊金额
     */
    @Override
    public int updateAmountByParam(OrdOrderCostSharingItem orderCostSharingItem) {
        return orderCostSharingItemDao.updateAmountByParam(orderCostSharingItem);
    }
}
