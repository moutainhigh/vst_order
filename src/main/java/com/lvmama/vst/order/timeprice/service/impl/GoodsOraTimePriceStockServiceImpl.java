package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsTimePriceOraDao;

/**
 * 
 * @author sunjian
 * 
 */
@Service("goodsOraTimePriceStockService")
public class GoodsOraTimePriceStockServiceImpl implements IGoodsTimePriceStockService {

    private static final Log LOG = LogFactory.getLog(GoodsOraTimePriceStockServiceImpl.class);

    @Autowired
    private SuppGoodsTimePriceOraDao suppGoodsTimePriceDao;

    @Autowired
    private SuppGoodsGroupStockOraDao suppGoodsGroupStockDao;

    @Autowired(required = false)
    private ComPushClientService comPushServiceRemote;

    @Autowired
    private OrdOrderGroupStockDao ordOrderGroupStockDao;
    
	@Autowired
	private IGoodsBaseTimePriceStockService goodsBaseTimePriceStockServiceImpl;

    @Override
    public boolean updateStock(Long timePriceId, Long stock) {
        if (timePriceId == null || stock == null) {
            return false;
        }

        SuppGoodsTimePrice sgtp = suppGoodsTimePriceDao.selectByPrimaryKey(timePriceId);
        if (sgtp == null) {
            return false;
        }

        // stock > 0 为恢复库存, stock < 0 为扣减库存
        if (stock > 0L) {
            return restoreStock(sgtp, stock);
        }

        return deductStock(sgtp, stock, null, null);
    }

    /**
     * 更新单库存
     * @param tp
     * @param stock
     * @return
     */
    private boolean updateNoGroup(SuppGoodsTimePrice tp, Long stock) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("timePriceId", tp.getTimePriceId());
        params.put("stock", stock);
        Integer rs = suppGoodsTimePriceDao.updateTimePriceStock(params);
        if (LOG.isInfoEnabled()) {
            LOG.info("updateNoGroup: [rs=" + rs + ",timePriceId=" + tp.getTimePriceId() + ",stock=" + stock + "]");
        }
        if (Integer.valueOf(1).equals(rs)) {
//            comPushServiceRemote.pushTimePrice(tp.getSuppGoodsId(), Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        	goodsBaseTimePriceStockServiceImpl.pushTimePrice(tp.getSuppGoodsId(), Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
            return true;
        }
        return false;
    }
    
    /**
     * 更新共享库存
     * @param tp
     * @param stock
     * @param totalStockAble
     * @param groupId 共享组ID
     * @return
     */
    private boolean updateGroup(SuppGoodsTimePrice tp, Long stock, Boolean totalStockAble, Long groupId){
        Integer rs = updateGoodsGroupStockByGoodsId(tp.getSuppGoodsId(), tp.getSpecDate(), stock, totalStockAble);
        //推送消息
        if(Integer.valueOf(1).equals(rs)){
        	List<Long> goodsId = getSuppGoodsByGroupId(groupId);
//        	comPushServiceRemote.pushTimePrice(goodsId, Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        	goodsBaseTimePriceStockServiceImpl.pushTimePrice(goodsId, Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        	return true;
        }
        return false;
    }

    /**
     * 判断该时间价格所属商品是否属有共享库存
     * @param sgtp
     * @param stock
     * @return groupId 共享组ID
     */
    public Long isShareStockAvailable(SuppGoodsTimePrice sgtp, Long stock) {
        if (sgtp == null || sgtp.getSuppGoodsId() == null || sgtp.getSpecDate() == null || stock == null) {
            throw new BusinessException("SuppGoodsTimePrice and stock can not be null");
        }

        Long suppGoodsId = sgtp.getSuppGoodsId();
        Date specDate = sgtp.getSpecDate();
        Long groupId = suppGoodsTimePriceDao.findGroupIdBySuppGoodsIdAndSpecDate(suppGoodsId, specDate);
        if (groupId == null) {
            return null;
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        params.put("specDate", specDate);
        Long shareStock = suppGoodsGroupStockDao.findShareStock(params);
        if (LOG.isInfoEnabled()) {
            LOG.info("isShareStockAvailable: [suppGoodsId=" + sgtp.getSuppGoodsId() + ",specDate=" + sgtp.getSpecDate()
                    + ",stock=" + stock + "]");
        }

        if (shareStock == null) {
            return null;
        }

        if (Long.valueOf(0).compareTo(stock) < 0) {
            return groupId;
        }

        if (shareStock.compareTo(Math.abs(stock)) < 0) {
            throw new BusinessException("Share stock is not enough to be deducted, share stock is:" + shareStock
                    + ",stock is:" + stock);
        }

        return groupId;
    }

    /**
     * 根据商品ID更新共享库存
     * 
     * @param groupId
     * @param specDate
     * @param stock
     * @param totalStockAble
     * @return
     */
    private int updateGoodsGroupStockByGoodsId(Long suppGoodsId, Date specDate, Long stock, Boolean totalStockAble) {
        if (LOG.isInfoEnabled()) {
            LOG.info("updateShareStock: [suppGoodsId=" + suppGoodsId + ",specDate=" + specDate + ",specDate="
                    + ",stock=" + stock + "]");
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", suppGoodsId);
        params.put("specDate", specDate);
        params.put("stock", stock);
        if(totalStockAble != null) params.put("totalStockAble", totalStockAble);
        return suppGoodsGroupStockDao.updateGroupStockByGoodsId(params);
    }

    @Override
    public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
        return suppGoodsTimePriceDao.getTimePrice(goodsId, specDate, checkAhead);
    }

    @Override
    public boolean updateGroupStock(Long timePriceId, Long stock, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        if (timePriceId == null || stock == null || Integer.valueOf(0).equals(stock)) {
            return false;
        }

        SuppGoodsTimePrice sgtp = suppGoodsTimePriceDao.selectByPrimaryKey(timePriceId);
        if (sgtp == null) {
            return false;
        }

        // stock > 0 为恢复库存;stock < 0 为扣减库存
        if (stock > 0L) {
            return restoreStock(sgtp, stock);
        }

        return deductStock(sgtp, stock, orderItemId, ordOrderGroupStockList);
    }

    private boolean deductStock(SuppGoodsTimePrice sgtp, Long stock, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        if (sgtp == null || stock == null) {
            return false;
        }

        //共享组ID
        Long groupId = isShareStockAvailable(sgtp, stock);
        boolean isShareStockAvailable = groupId != null;
        if (LOG.isInfoEnabled()) {
            LOG.info("deductStock.isShareStockAvailable returns: " + isShareStockAvailable);
        }
        if (!isShareStockAvailable) {
            return updateNoGroup(sgtp, stock);
        }
        
        boolean success = updateGroup(sgtp, stock, null, groupId);
		if (LOG.isInfoEnabled()) {
		    LOG.info("deductStock: [success=" + success + ",suppGoodsId=" + sgtp.getSuppGoodsId() + ",specDate="
		            + sgtp.getSpecDate() + ",stock=" + stock + "]");
		}
//        if (success && orderItemId != null && ordOrderGroupStockList != null) {
//            saveOrderGroupStock(sgtp, orderItemId, ordOrderGroupStockList);
//        }

        return success;
    }

    // 恢复库存
    private boolean restoreStock(SuppGoodsTimePrice sgtp, Long stock) {
    	//共享组ID
    	Long groupId = isShareStockAvailable(sgtp, stock);
        boolean isShareStockAvailable = groupId != null;
        if (LOG.isInfoEnabled()) {
            LOG.info("restoreStock.isShareStockAvailable: " + isShareStockAvailable);
        }

        if (isShareStockAvailable) {
            if ("N".equals(sgtp.getRestoreFlag())) {
                return false;
            }
            return updateGroup(sgtp, stock, false ,groupId);
        }
        
        if (!("Y".equals(sgtp.getRestoreFlag()) && "Y".equals(sgtp.getStockFlag()))) {
            return false;
        }
        
        return updateNoGroup(sgtp, stock);

    }

    private void saveOrderGroupStock(SuppGoodsTimePrice sgtp, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        if(null == sgtp.getStock())
            return;
        OrdOrderGroupStock ordOrderGroupStock = new OrdOrderGroupStock();
        ordOrderGroupStock.setOrderItemSuppGoodsId(sgtp.getSuppGoodsId());
        ordOrderGroupStock.setSuppGoodsId(sgtp.getSuppGoodsId());
        ordOrderGroupStock.setQuantity(Math.abs(sgtp.getStock()));
        ordOrderGroupStock.setVisitTime(sgtp.getSpecDate());
        ordOrderGroupStock.setOrderItemId(orderItemId);
        ordOrderGroupStockDao.insert(ordOrderGroupStock);
        ordOrderGroupStockList.add(ordOrderGroupStock);
    }

    public Long getShareStock(Long groupId, Date specDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        params.put("specDate", specDate);
        if (LOG.isInfoEnabled()) {
            LOG.info("getShareStock: groupId=" + groupId + ", specDate=" + specDate);
        }
        return suppGoodsGroupStockDao.findShareStock(params);
    }
    
    /**
     * 获取商品ID集合
     * @param groupId 共享组ID
     * @return
     */
    private List<Long> getSuppGoodsByGroupId(Long groupId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("getSuppGoodsByGroupId: [groupId=" + groupId + "]");
        }
        return suppGoodsGroupStockDao.findSuppGoodsByGroupId(groupId);
    }

	@Override
	public boolean updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
