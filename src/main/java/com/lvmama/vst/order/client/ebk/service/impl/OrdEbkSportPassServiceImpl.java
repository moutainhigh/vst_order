/**
 * 
 */
package com.lvmama.vst.order.client.ebk.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.comm.vo.pass.EbkTicketPassVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketPostVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketStatisVO;
import com.lvmama.vst.ebooking.ebk.po.EbkOrdTransfer;
import com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService;
import com.lvmama.vst.order.dao.EbkSportPassDao;
import com.lvmama.vst.order.utils.EbkCurrencyUtils;

/**
 * @author chenlizhao
 *
 */
@Service("ordEbkSportPassService")
public class OrdEbkSportPassServiceImpl implements OrdEbkSportPassService {

	private Log log = LogFactory.getLog(OrdEbkSportPassServiceImpl.class);

	@Autowired
	private EbkSportPassDao ebkPassDao;
	
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPassStatisListByPrams(java.util.Map)
	 */
	@Override
	public List<EbkTicketStatisVO> selectEbkPassStatisListByPrams(Map<String, Object> params) {
		if(params != null){
			List<EbkTicketStatisVO> ebkTicketStatisVOList =ebkPassDao.selectEbkPassStatisListByPrams(params);
			EbkCurrencyUtils.dealForeignRMBType((String)params.get("currencyCode"), ebkTicketStatisVOList);
			return ebkTicketStatisVOList;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPassStatisCount(java.util.Map)
	 */
	@Override
	public Integer selectEbkPassStatisCount(Map<String, Object> params) {
		if(params != null){
			return ebkPassDao.selectEbkPassStatisCount(params);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPassListByPrams(java.util.Map)
	 */
	@Override
	public List<EbkTicketPassVO> selectEbkPassListByPrams(Map<String, Object> params) {
		if(log.isInfoEnabled()){
			log.info("start method<selectEbkPassListByPrams>  ==>"+params);
		}
		List<EbkTicketPassVO> ticketPassList = new ArrayList<EbkTicketPassVO>();
		if(params != null){
			ticketPassList = ebkPassDao.selectEbkPassListByPrams(params);
			if(ticketPassList != null && !ticketPassList.isEmpty()){
				//判断通关明细
				if(params.get("detailFlag") != null){
					boolean detailFlag = (Boolean)params.get("detailFlag");
					if(detailFlag){
						Map<String, Object> ticketOrderItem = new HashMap<String, Object>();
						ticketOrderItem.put("ordOrderFlag", "Y");

						ticketOrderItem.put("suppGoodsIdStr", params.get("suppGoodsIdStr"));

						//通关明细列表
						List<EbkTicketPassVO> ticketPassDetailList = new ArrayList<EbkTicketPassVO>(); 
						for(EbkTicketPassVO ticketPass : ticketPassList){
							Long orderId = ticketPass.getOrderId();
							ticketOrderItem.put("orderId", orderId);
							// 通关列表查询  支持多次通关：未游玩和部分游玩
							if(params.get("passMTFlag") != null){
								ticketOrderItem.put("passMTFlag", params.get("passMTFlag"));
							}
							//游玩状态：未游玩、部分游玩、已游玩
							if(params.get("performStatus") != null){
								ticketOrderItem.put("performStatus", params.get("performStatus"));
							}
							
							log.info("find ebkPass List params ==>"+ticketOrderItem);
							//根据订单Id查询订单子项列表
							List<EbkTicketPassVO> ticketOrderItemPassList = ebkPassDao.selectEbkPassListByPrams(ticketOrderItem);
							if(ticketOrderItemPassList != null && !ticketOrderItemPassList.isEmpty()){
								ticketPassDetailList.addAll(ticketOrderItemPassList);
							}
						}
						ticketPassList.clear();
						ticketPassList.addAll(ticketPassDetailList);
					}
				}
			}
		}
		EbkCurrencyUtils.dealForeignPriceIsNull(ticketPassList);
		log.info("end method<selectEbkPassListByPrams> ");
		return ticketPassList;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPassListCount(java.util.Map)
	 */
	@Override
	public Integer selectEbkPassListCount(Map<String, Object> params) {
		if(params != null){
			return ebkPassDao.selectEbkPassListCount(params);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPassListForReportByPrams(java.util.Map)
	 */
	@Override
	public List<EbkTicketPassVO> selectEbkPassListForReportByPrams(Map<String, Object> params) {
		List<EbkTicketPassVO> ticketPassList = new ArrayList<EbkTicketPassVO>();
		if(params != null){
			params.put("exportFlag", "Y");
			ticketPassList = ebkPassDao.selectEbkPassListForReportByPrams(params);
		}
		EbkCurrencyUtils.dealForeignPriceIsNull(ticketPassList);
		return ticketPassList;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkMiddleListByPrams(java.util.Map)
	 */
	@Override
	@ReadOnlyDataSource
	public List<EbkTicketPassVO> selectEbkMiddleListByPrams(Map<String, Object> params) {
		if(log.isInfoEnabled()){
			log.info("start method<selectEbkMiddleListByPrams>  ==>"+params);
		}
		List<EbkTicketPassVO> ticketPassList = new ArrayList<EbkTicketPassVO>();
		if(params != null){
			ticketPassList = ebkPassDao.selectEbkMiddleListByPrams(params);
		}
		EbkCurrencyUtils.dealForeignPriceIsNull(ticketPassList);
		log.info("end method<selectEbkMiddleListByPrams> ");
		return ticketPassList;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkMiddleListCount(java.util.Map)
	 */
	@Override
	@ReadOnlyDataSource
	public Integer selectEbkMiddleListCount(Map<String, Object> params) {
		if(params != null){
			return ebkPassDao.selectEbkMiddleListCount(params);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkMiddleListForReportByPrams(java.util.Map)
	 */
	@Override
	@ReadOnlyDataSource
	public List<EbkTicketPassVO> selectEbkMiddleListForReportByPrams(Map<String, Object> params) {
		List<EbkTicketPassVO> ticketPassList = new ArrayList<EbkTicketPassVO>();
		if(params != null){
			params.put("exportFlag", "Y");
			ticketPassList = ebkPassDao.selectEbkMiddleListForReportByPrams(params);	
		}
		EbkCurrencyUtils.dealForeignPriceIsNull(ticketPassList);
		return ticketPassList;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPostListByPrams(java.util.Map)
	 */
	@Override
	public List<EbkTicketPostVO> selectEbkPostListByPrams(Map<String, Object> params) {
		if(log.isInfoEnabled()){
			log.info("start method<selectEbkPostListByPrams>  ==>"+params);
		}
		List<EbkTicketPostVO> ticketPostList = new ArrayList<EbkTicketPostVO>();
		if(params != null){
			ticketPostList = ebkPassDao.selectEbkPostListByPrams(params);
			if(ticketPostList != null && !ticketPostList.isEmpty()){
				//判断通关明细
				if(params.get("detailFlag") != null){
					boolean detailFlag = (Boolean)params.get("detailFlag");
					if(detailFlag){
						Map<String, Object> ticketOrderItem = new HashMap<String, Object>();
						ticketOrderItem.put("ordOrderFlag", "Y");

						ticketOrderItem.put("suppGoodsIdStr", params.get("suppGoodsIdStr"));

						//通关明细列表
						List<EbkTicketPostVO> ticketPostDetailList = new ArrayList<EbkTicketPostVO>(); 
						for(EbkTicketPostVO ticketPost : ticketPostList){
							Long orderId = ticketPost.getOrderId();
							ticketOrderItem.put("orderId", orderId);
							
							log.info("find EbkPost List params ==>"+ticketOrderItem);
							//根据订单Id查询订单子项列表
							List<EbkTicketPostVO> ticketOrderItemPostList = ebkPassDao.selectEbkPostListByPrams(ticketOrderItem);
							if(ticketOrderItemPostList != null && !ticketOrderItemPostList.isEmpty()){
								ticketPostDetailList.addAll(ticketOrderItemPostList);
							}
						}
						ticketPostList.clear();
						ticketPostList.addAll(ticketPostDetailList);
					}
				}
			}
		}
		EbkCurrencyUtils.dealForeignPriceIsNullForPost(ticketPostList);
		log.info("end method<selectEbkPostListByPrams> ");
		return ticketPostList;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPostListCount(java.util.Map)
	 */
	@Override
	public Integer selectEbkPostListCount(Map<String, Object> params) {
		if(params != null){
			return ebkPassDao.selectEbkPostListCount(params);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.client.service.ebk.OrdEbkSportPassService#selectEbkPostListForReportByPrams(java.util.Map)
	 */
	@Override
	public List<EbkTicketPostVO> selectEbkPostListForReportByPrams(Map<String, Object> params) {
		List<EbkTicketPostVO> ticketPostList = new ArrayList<EbkTicketPostVO>();
		if(params != null){
			params.put("exportFlag", "Y");
			ticketPostList = ebkPassDao.selectEbkPostListForReportByPrams(params);
		}
		EbkCurrencyUtils.dealForeignPriceIsNullForPost(ticketPostList);
		return ticketPostList;
	}

	@Override
	public int transferOrder(Map<String, Object> params) {
		return ebkPassDao.transferOrder(params);
	}

	@Override
	public List<EbkOrdTransfer> queryEbkOrdTransferList(Map<String, Object> params) {
		return ebkPassDao.queryEbkOrdTransferList(params);
	}

	@Override
	public void updateTransferOrder(Map<String, Object> params) {
		ebkPassDao.updateTransferOrder(params);
	}

}
