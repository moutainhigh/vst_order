package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsLineTimePriceOraDao;

@Component("goodsOraLineTimePriceStockService")
public class GoodsOraLineTimePriceStockServiceImpl implements IGoodsTimePriceStockService {

    private static final Log LOG = LogFactory.getLog(GoodsOraLineTimePriceStockServiceImpl.class);

    @Autowired
    private SuppGoodsLineTimePriceOraDao suppGoodsLineTimePriceDao;

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

        SuppGoodsLineTimePrice sgltp = suppGoodsLineTimePriceDao.selectByPrimaryKey(timePriceId);
        if (sgltp == null) {
            return false;
        }

        //共享组ID
        Long groupId = isShareStockAvailable(sgltp, stock);
        boolean isShareStockAvailable = groupId != null;
        if (LOG.isInfoEnabled()) {
            LOG.info("updateStock.isShareStockAvailable: " + isShareStockAvailable);
        }
        if (isShareStockAvailable) {
            return updateGroup(sgltp, stock, null, null, groupId);
        }

        return updateSelfStock(sgltp, stock);
    }

    @Override
    public boolean updateGroupStock(Long timePriceId, Long stock, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        if (timePriceId == null || stock == null) {
            return false;
        }

        SuppGoodsLineTimePrice sgltp = suppGoodsLineTimePriceDao.selectByPrimaryKey(timePriceId);
        if (sgltp == null) {
            return false;
        }

        //共享组ID
        Long groupId = isShareStockAvailable(sgltp, stock);
        boolean isShareStockAvailable = groupId != null;
        if (LOG.isInfoEnabled()) {
            LOG.info("updateGroupStock.isShareStockAvailable: " + isShareStockAvailable);
        }
        if (isShareStockAvailable) {
            return updateGroup(sgltp, stock, orderItemId, ordOrderGroupStockList, groupId);
        }

        return updateSelfStock(sgltp, stock);
    }

    private boolean updateSelfStock(SuppGoodsLineTimePrice timePrice, Long stock) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("timePriceId", timePrice.getTimePriceId());
        params.put("stock", stock);
        int rs = suppGoodsLineTimePriceDao.updateStockForOrder(params);
        if (LOG.isInfoEnabled()) {
            LOG.info("updateShareStock: [rs=" + rs + ",timePriceId=" + timePrice.getTimePriceId() + ",stock=" + stock
                    + "]");
        }

        if (rs == 1) {
//            comPushServiceRemote.pushTimePrice(timePrice.getSuppGoodsId(),
//                    Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        	goodsBaseTimePriceStockServiceImpl.pushTimePrice(timePrice.getSuppGoodsId(),
                  Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        }
        return rs == 1;
    }

    @Override
    public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", goodsId);
        params.put("specDate", specDate);
        SuppGoodsLineTimePrice timePrice = suppGoodsLineTimePriceDao.selectByGoodsSpecDate(params);
        if (timePrice != null && checkAhead) {
            Long aheadBookTime = timePrice.getAheadBookTime();
            if (aheadBookTime == null) {
                aheadBookTime = 0L;
            }
            Date date = DateUtils.addMinutes(timePrice.getSpecDate(), -aheadBookTime.intValue());
            if (date.before(new Date())) {
                return null;
            }
        }

        Long groupId = suppGoodsLineTimePriceDao.findGroupIdBySuppGoodsIdAndSpecDate(goodsId, specDate);
        if (groupId == null) {
            return timePrice;
        }

        params.put("groupId", groupId);
        Long shareStock = suppGoodsGroupStockDao.findShareStock(params);
        if (shareStock == null) {
            return timePrice;
        }

        timePrice.setStock(shareStock);
        return timePrice;
    }

    /**
     * 更新共享库存
     * @param tp
     * @param stock
     * @param groupId 共享组ID
     * @return
     */
    private boolean updateGroup(SuppGoodsLineTimePrice tp, Long stock, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList, Long groupId){
        if(!updateShareStock(tp, stock, orderItemId, ordOrderGroupStockList)) return false;
        
        //推送消息
        List<Long> goodsId = getSuppGoodsByGroupId(groupId);
//    	comPushServiceRemote.pushTimePrice(goodsId, Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        goodsBaseTimePriceStockServiceImpl.pushTimePrice(goodsId, Collections.singletonList(tp.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
        return true;
    }
    
    private boolean updateShareStock(SuppGoodsLineTimePrice sgltp, Long stock, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", sgltp.getSuppGoodsId());
        params.put("specDate", sgltp.getSpecDate());
        params.put("stock", stock);
        Integer rs = suppGoodsGroupStockDao.updateGroupStockByGoodsId(params);
        boolean success = Integer.valueOf(1).equals(rs);
        if (LOG.isInfoEnabled()) {
            LOG.info("updateShareStock: [success=" + success + ",suppGoodsId=" + sgltp.getSuppGoodsId() + ",specDate="
                    + sgltp.getSpecDate() + ",stock=" + stock + "]");
        }

//        if (success && orderItemId != null && ordOrderGroupStockList != null) {
//            saveOrderGroupStock(sgltp, orderItemId, ordOrderGroupStockList);
//        }

        return success;
    }

    /**
     * 判断该时间价格所属商品是否属有共享库存
     */
    public Long isShareStockAvailable(SuppGoodsLineTimePrice sgltp, Long stock) {
        if (sgltp == null || sgltp.getSuppGoodsId() == null || sgltp.getSpecDate() == null || stock == null) {
            throw new BusinessException("SuppGoodsLineTimePrice and stock can not be null");
        }

        Long suppGoodsId = sgltp.getSuppGoodsId();
        Date specDate = sgltp.getSpecDate();
        Long groupId = suppGoodsLineTimePriceDao.findGroupIdBySuppGoodsIdAndSpecDate(suppGoodsId, specDate);
        if (groupId == null) {
            return null;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        params.put("specDate", specDate);
        Long shareStock = suppGoodsGroupStockDao.findShareStock(params);
        if (LOG.isInfoEnabled()) {
            LOG.info("isShareStockAvailable: [suppGoodsId=" + sgltp.getSuppGoodsId() + ",specDate="
                    + sgltp.getSpecDate() + ",stock=" + stock + "]");
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

    private void saveOrderGroupStock(SuppGoodsLineTimePrice sgltp, Long orderItemId,
            List<OrdOrderGroupStock> ordOrderGroupStockList) {
        OrdOrderGroupStock ordOrderGroupStock = new OrdOrderGroupStock();
        ordOrderGroupStock.setOrderItemSuppGoodsId(sgltp.getSuppGoodsId());
        ordOrderGroupStock.setSuppGoodsId(sgltp.getSuppGoodsId());
        if(sgltp.getStock() != null){
            ordOrderGroupStock.setQuantity(Math.abs(sgltp.getStock()));
        }
        ordOrderGroupStock.setVisitTime(sgltp.getSpecDate());
        ordOrderGroupStock.setOrderItemId(orderItemId);
        ordOrderGroupStockDao.insert(ordOrderGroupStock);
        ordOrderGroupStockList.add(ordOrderGroupStock);
    }

    public Long getShareStock(Long groupId, Date specDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        params.put("specDate", specDate);
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
