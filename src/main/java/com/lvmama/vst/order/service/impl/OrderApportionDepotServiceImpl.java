package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.BatchApportionOutcome;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.PageConst;
import com.lvmama.vst.order.constant.ApportionConstants;
import com.lvmama.vst.order.dao.OrderApportionDepotDao;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.utils.OrderApportionDepotUtils;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
@Service
public class OrderApportionDepotServiceImpl implements OrderApportionDepotService {
    private static final Log log = LogFactory.getLog(OrderApportionDepotServiceImpl.class);
    @Resource
    private OrderApportionDepotDao orderApportionDepotDao;

    /**
     * 添加订单记录,返回添加的记录id
     *
     * @param orderApportionDepot
     */
    @Override
    public Long addOrderApportionDepot(OrderApportionDepot orderApportionDepot) {
        orderApportionDepotDao.insertSelective(orderApportionDepot);
        return orderApportionDepot.getOrderApportionId();
    }

    /**
     * 添加订单记录,返回添加的记录id
     *
     * @param orderId
     */
    @Override
    public Long addOrderApportionDepot(Long orderId) {
        if (NumberUtils.isNotAboveZero(orderId)) {
            return null;
        }
        OrderApportionDepot orderApportionDepot = new OrderApportionDepot();
        orderApportionDepot.setOrderId(orderId);
        orderApportionDepot.setValidFlag(Constants.Y_FLAG);
        Date currentDateTime = Calendar.getInstance().getTime();
        orderApportionDepot.setCreateTime(currentDateTime);
        orderApportionDepot.setUpdateTime(currentDateTime);
        return addOrderApportionDepot(orderApportionDepot);
    }

    /**
     * 批量更新分摊仓库表记录，此方法中一定要做参数检查，避免更新了额外的记录
     *
     * @param orderApportionDepotUpdateVO
     */
    @Override
    public int updateOrderApportionDepotList(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO) {
        boolean checkParamResult = OrderApportionDepotUtils.checkParam(orderApportionDepotUpdateVO);
        if (!checkParamResult) {
            String errorMsg = "Parameter " + GsonUtils.toJson(orderApportionDepotUpdateVO) + " lack of required value, can't do update";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return orderApportionDepotDao.updateOrderApportionDepotList(orderApportionDepotUpdateVO);
    }

    /**
     * 移除分摊好的订单
     *
     * @param orderIdList
     */
    @Override
    public void removeCompletedOrder(List<Long> orderIdList) {
        if (CollectionUtils.isEmpty(orderIdList)) {
            return;
        }
        orderApportionDepotDao.batchDeleteByPrimaryKey(orderIdList);
    }

    /**
     * 批量更新订单仓库信息
     * */
    @Override
    public void updateOrderApportionDepotList(List<OrderApportionDepot> orderApportionDepotList) {
        if(CollectionUtils.isEmpty(orderApportionDepotList)) {
            return;
        }

        for (OrderApportionDepot orderApportionDepot : orderApportionDepotList) {
            if(orderApportionDepot == null) {
                continue;
            }

            orderApportionDepotDao.updateByPrimaryKeySelective(orderApportionDepot);
        }
    }

    /**
     * 分页查询分摊记录
     *
     * @param paramMap
     */
    @Override
    public List<OrderApportionDepot> queryOrderApportionDepotList(Map<String, Object> paramMap) {
        //检查参数
        paramMap = this.checkPageParameter(paramMap);
        return orderApportionDepotDao.queryForList(paramMap);
    }

    /**
     * 根据条件查询记录总数
     *
     * @param paramMap
     */
    @Override
    public Long queryRecordCount(Map<String, Object> paramMap) {
        return orderApportionDepotDao.queryRecordCount(paramMap);
    }

 

    /**
     * 单条更新订单仓库信息
     *
     * @param orderApportionDepot
     */
    @Override
    public int updateOrderApportionDepotSelective(OrderApportionDepot orderApportionDepot) {
        return orderApportionDepotDao.updateByPrimaryKeySelective(orderApportionDepot);
    }

    /**
     * 单条更新订单仓库信息(无选择性，即一个字段如果是null，数据库中的记录也会随着变成null)
     *
     * @param orderApportionDepot
     */
    @Override
    public int updateOrderApportionDepot(OrderApportionDepot orderApportionDepot) {
        return orderApportionDepotDao.updateByPrimaryKey(orderApportionDepot);
    }

    /**
     * 批量删除分摊信息
     *
     * @param bookingApportionDepotUpdateVO
     */
    @Override
    public void updateOfClearApportionMessage(OrderApportionDepotUpdateVO bookingApportionDepotUpdateVO) {
        if (bookingApportionDepotUpdateVO == null || CollectionUtils.isEmpty(bookingApportionDepotUpdateVO.getOrderApportionIdList())) {
            return;
        }
        List<List<Long>>  listArray = splitList(bookingApportionDepotUpdateVO.getOrderApportionIdList(),ApportionConstants.BATCH_UPDATE_SIZE);
        for (List<Long> list : listArray) {
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            bookingApportionDepotUpdateVO.setOrderApportionIdList(list);
            orderApportionDepotDao.updateOfClearApportionMessage(bookingApportionDepotUpdateVO);
        }
    }

    
	@Override
	public OrderApportionDepot queryApportionByOrderId(Long orderId) {
		return orderApportionDepotDao.queryApportionByOrderId(orderId);
	}

	@Override
	public int deleteByPrimaryKey(Long orderApportionId) {
		return orderApportionDepotDao.deleteByPrimaryKey(orderApportionId);
	}

    /**
     * 根据订单号删除
     *
     * @param orderId
     */
    @Override
    public int deleteByOrderId(Long orderId) {
        if (NumberUtils.isNotAboveZero(orderId)) {
            return 0;
        }
        return orderApportionDepotDao.deleteByOrderId(orderId);
    }

    /**
     * 检查查询参数的分页数据，如果不合法，则给一个默认值
     * */
	private Map<String, Object> checkPageParameter(Map<String, Object> paramMap){
	    if (paramMap == null) {
	        Page page = Page.page(ApportionConstants.maxPageSize, 1);
	        paramMap = new HashMap<>();
            paramMap.put(PageConst.PARAM_PAGE_START_INDEX, page.getStartRows());
            paramMap.put(PageConst.PARAM_PAGE_END_INDEX, page.getEndRows());
	        return paramMap;
        }

        //得到起始索引，如果没有传，给0
        Object startIndexObj = paramMap.get(PageConst.PARAM_PAGE_START_INDEX);
        Long startIndex = startIndexObj == null ? 1L : (Long)startIndexObj;
        //得到结束索引，如果没有传，如果传的值与起始索引的差值大于页尺寸，用起始索引加页尺寸，
        Object endIndexObj = paramMap.get(PageConst.PARAM_PAGE_END_INDEX);
        Long endIndex = endIndexObj == null ? 1L : (long)endIndexObj;
	    if (endIndex == 0 || (endIndex - startIndex + 1 > ApportionConstants.maxPageSize)) {
	        endIndex = startIndex + ApportionConstants.maxPageSize - 1;
        }
        paramMap.put(PageConst.PARAM_PAGE_START_INDEX, startIndex);
	    paramMap.put(PageConst.PARAM_PAGE_END_INDEX, endIndex);
	    return paramMap;
    }
	
	public void batchUpdateApportionOutcome(BatchApportionOutcome batchApportionOutcome){
	    log.info("Now deal with apportion result");
		if(null != batchApportionOutcome){
		    //批量删除无用的(即不再进行分摊的订单，目前针对超时未支付的订单)订单记录
			if(CollectionUtils.isNotEmpty(batchApportionOutcome.getFutileOrderApportionIdList())){
				this.batchDeleteOrderApportionDepotByPrimaryKey(batchApportionOutcome.getFutileOrderApportionIdList());
                log.info("Futile order deleted, size is " + batchApportionOutcome.getFutileOrderApportionIdList().size());
			}
			//批量删除实付已分摊的订单记录
			if(CollectionUtils.isNotEmpty(batchApportionOutcome.getSuccessOrderApportionIdList())){
				this.batchDeleteOrderApportionDepotByPrimaryKey(batchApportionOutcome.getSuccessOrderApportionIdList());
                log.info("Successful order deleted, size is " + batchApportionOutcome.getSuccessOrderApportionIdList().size());
			}

			//更新下单项分摊完成的订单
			if(CollectionUtils.isNotEmpty(batchApportionOutcome.getBookingApportionSucceedOrderApportionIdList())){
				this.batchUpdateBookingApportionDeport(batchApportionOutcome.getBookingApportionSucceedOrderApportionIdList());
                log.info("Booking apportion completed order updated, size is " + batchApportionOutcome.getBookingApportionSucceedOrderApportionIdList().size());
			}

			//更新分摊失败的订单
			if(CollectionUtils.isNotEmpty(batchApportionOutcome.getFailedOrderDepotList())){
				this.batchUpdateFailedOrder(batchApportionOutcome.getFailedOrderDepotList());
                log.info("Failed order updated, size is " + batchApportionOutcome.getFailedOrderDepotList().size());
			}

			//清除待下次分摊的记录的数据库标识锁
			if (CollectionUtils.isNotEmpty(batchApportionOutcome.getWaitForNextBatchOrderApportionIdList())) {
                OrderApportionDepotUpdateVO orderApportionDepotUpdateVO = new OrderApportionDepotUpdateVO();
                orderApportionDepotUpdateVO.setOrderApportionIdList(batchApportionOutcome.getWaitForNextBatchOrderApportionIdList());
                this.updateOfClearApportionMessage(orderApportionDepotUpdateVO);
                log.info("Waiting for next batch order updated, size is " + batchApportionOutcome.getWaitForNextBatchOrderApportionIdList().size());
            }
		}
		log.info("");
			
	}
	
	/**
     * 批量删除订单表记录，根据主键
     * */
	private int batchDeleteOrderApportionDepotByPrimaryKey(List<Long> orderApportionIdList){
		 List<List<Long>>  listArray = splitList(orderApportionIdList,ApportionConstants.BATCH_UPDATE_SIZE);
		 int resultNum = 0;
		 for (List<Long> list : listArray) {
		     if (CollectionUtils.isEmpty(list)) {
		         continue;
             }
			 resultNum =  orderApportionDepotDao.batchDeleteByPrimaryKey(list);
		}
		return resultNum;
	}
	
	/**
     * 更新下单项已分摊的订单集合
     * */
	private int batchUpdateBookingApportionDeport(List<Long> orderApportionIdList){
	    List<List<Long>> listArray = splitList(orderApportionIdList, ApportionConstants.BATCH_UPDATE_SIZE);
		int resultNum = 0;
        OrderApportionDepotUpdateVO orderApportionDepotUpdateVO = new OrderApportionDepotUpdateVO();
        orderApportionDepotUpdateVO.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name());
        orderApportionDepotUpdateVO.setUpdateTime(Calendar.getInstance().getTime());
        for (List<Long> list : listArray) {
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            orderApportionDepotUpdateVO.setOrderApportionIdList(list);
            resultNum =  orderApportionDepotDao.batchUpdateApportionDeportCreateEnd(orderApportionDepotUpdateVO);
		}
	   return resultNum;
	}
	
	
	private int batchUpdateFailedOrder(List<OrderApportionDepot> orderApportionList){
	    int count = 0;
	    for (OrderApportionDepot deport : orderApportionList) {
	        deport.setValidFlag(Constants.N_FLAG);
	        deport.setUpdateTime(Calendar.getInstance().getTime());
	        deport.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_failed.name());
	        count += orderApportionDepotDao.updateByPrimaryKey(deport);
	    }
		return count;
	}
	
	
	
	public  <T> List<List<T>> splitList( List<T> oriList, int batchSize) {
		List<List<T>> rtnList = new ArrayList<List<T>>();
		int count = oriList.size();
		if(oriList==null || count<=0)   return rtnList;
		for(int i=0;i<count;i=i+batchSize){
			int end = i+batchSize;
			end = end<count?end:count;
			List<T> tmpList = new ArrayList<T>();
			tmpList.addAll(oriList.subList(i, end));
			if(CollectionUtils.isEmpty(tmpList)){
				continue;
			}
			rtnList.add(tmpList);
		}
		return rtnList;
	}
}
