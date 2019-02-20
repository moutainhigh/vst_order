package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdRefundItem;
import com.lvmama.vst.order.dao.OrdRefundItemDao;
import com.lvmama.vst.order.service.OrdRefundItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
@Service
public class OrdRefundItemServiceImpl implements OrdRefundItemService {
    @Resource
    private OrdRefundItemDao ordRefundItemDao;

    /**
     * 保存新记录
     *
     * @param ordRefundItem
     */
    @Override
    public int saveOrdRefundItem(OrdRefundItem ordRefundItem) {
        return ordRefundItemDao.insert(ordRefundItem);
    }
    
    
    
    /**
     * 判空保存
     */
    public int saveOrdRefundItemSelective(OrdRefundItem ordRefundItem){
    	 return ordRefundItemDao.insertSelective(ordRefundItem);
    }
    
    
}
