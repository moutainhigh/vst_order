package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.ComAuditSortRule;
import com.lvmama.vst.back.order.po.Confirm_Enum.ARRIVE_TYPE;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.order.dao.ComAuditSortRuleDao;
import com.lvmama.vst.order.service.IOrderAuditSortRuleService;

/**
 * 项目名称：vst_order
 * 类名称：OrderAuditSortRuleServiceImpl
 * 类描述：订单排序规则业务实现
 * 创建人：majunli
 * 创建时间：2016-10-15 上午10:26:52
 * 修改人：majunli
 * 修改时间：2016-10-15 上午10:26:52
 * 修改备注：
 */
@Service("orderAuditSortRuleService")
public class OrderAuditSortRuleServiceImpl implements IOrderAuditSortRuleService{
	
	private static final Log LOGGER = LogFactory.getLog(OrderAuditSortRuleServiceImpl.class);
	//分销，淘宝
    private static final String DISTRIBUTOR_CODE_TAOBAO="DISTRIBUTOR_TAOBAO";
	@Autowired
	private ComAuditSortRuleDao comAuditSortRuleDao;
	
	@Autowired
	private OrderService orderService;

	@Override
	public int saveComAuditSortRule(ComAuditSortRule comAuditSortRule) {
		return comAuditSortRuleDao.insert(comAuditSortRule);
	}

	@Override
	public int saveComAuditSortRuleSelective(ComAuditSortRule comAuditSortRule) {
		return comAuditSortRuleDao.insertSelective(comAuditSortRule);
	}

	@Override
	public int updateComAuditSortRuleByPrimaryKeySelective(
			ComAuditSortRule comAuditSortRule) {
		return comAuditSortRuleDao.updateByPrimaryKeySelective(comAuditSortRule);
	}

	@Override
	public int updateComAuditSortRuleByPrimaryKey(
			ComAuditSortRule comAuditSortRule) {
		return comAuditSortRuleDao.updateByPrimaryKey(comAuditSortRule);
	}
	
	@Override
	public ComAuditSortRule selectComAuditSortRuleByPrimaryKey(Long sortRuleId) {
		return comAuditSortRuleDao.selectByPrimaryKey(sortRuleId);
	}

	@Override
	public List<ComAuditSortRule> queryComAuditSortRuleListByParam(
			Map<String, Object> param) {
		return comAuditSortRuleDao.selectByParams(param);
	}

	@Override
	public Integer getTotalCount(Map<String, Object> param) {
		return comAuditSortRuleDao.getTotalCount(param);
	}

	@Override
	public ComAuditSortRule getComAuditSortRuleByOrderItem(OrdOrderItem ordOrderItem) {
		return getComAuditSortRule(ordOrderItem, false, null);
	}

	@Override
	public ComAuditSortRule getComAuditSortRuleByOrderItemByJob(OrdOrderItem ordOrderItem, Date nowDate) {
		return getComAuditSortRule(ordOrderItem, true, nowDate);
	}

	/**
	 * 计算最优排序规则
	 * @param ordOrderItem
	 * @param recount 是否为重新计算排序规则
	 * @return
	 */
	private ComAuditSortRule getComAuditSortRule(OrdOrderItem ordOrderItem, boolean recount, Date nowDate) {
		if(ordOrderItem == null){
			LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem ordOrderItem is null");
			return null;
		}
		LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem ordOrderItem orderItemId:"
				+ ordOrderItem.getOrderItemId()+ ", confirmStatus:"
				+ ordOrderItem.getConfirmStatus() != null ? ordOrderItem.getConfirmStatus() : "");
		long startTime = System.currentTimeMillis();
		//最优分组
		ComAuditSortRule matchComAuditSortRule = null;
		//最大匹配值
		int maxMatchValue = 0;
		try {
			OrdOrder order = orderService.queryOrdorderByOrderId(ordOrderItem.getOrderId());
			if(order == null){
				LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem order is null");
				return null;
			}
			if (recount) {
				//如果为重新排序，将支付时间设为当前时间，用来计算到店规则
				order.setPaymentTime(nowDate);
			}
			//订单到店时间
			String orderArriveType = getOrderArriveType(order,ordOrderItem);
			if(orderArriveType.equals("")){
				LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem order paytime is null");
				return null;
			}
			Map<String, Object> params = new HashMap<String, Object>();
			int totalCount = comAuditSortRuleDao.getTotalCount(params);
			if(totalCount == 0){
				LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem totalCount = 0 orderItemId:"
						+ ordOrderItem.getOrderItemId());
				return null;
			}
			LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem totalCount:" + totalCount);
			StringBuffer buffer = new StringBuffer();
			buffer.append("getComAuditSortRuleByOrderItem match orderItemId:"+ordOrderItem.getOrderItemId() + " totalCount:" + totalCount);
			//分页查询，循环对比所有分组，找出最优的分组

			Page<Long> resultPage = null;
			int pageSize = 50;
			int pages = totalCount/pageSize + 1;
			for (int i = 1; i <= pages; i++) {
				resultPage = Page.page(totalCount, pageSize, i);
				params.put("_start", resultPage.getStartRows());
				params.put("_end", resultPage.getEndRows());
				List<ComAuditSortRule> comAuditSortRuleList = comAuditSortRuleDao.selectByParams(params);
				if(CollectionUtils.isEmpty(comAuditSortRuleList)){
					continue;
				}
				for(ComAuditSortRule auditSortRule : comAuditSortRuleList){
					buffer.append(" SortRuleId:"+auditSortRule.getSortRuleId());
					//本次循环中的匹配值
					int hits = 0;
					//判断是否为默认分组
					if (maxMatchValue==0 && StringUtil.isEmptyString(auditSortRule.getArriveType())
							&& StringUtil.isEmptyString(auditSortRule.getBu())
							&& auditSortRule.getObjectId() == null
							&& auditSortRule.getSupplierId() == null
							&& StringUtil.isEmptyString(auditSortRule
									.getImmediatelyFlag())) {
						matchComAuditSortRule = auditSortRule;
					}

					if(isStringMatch(orderArriveType, auditSortRule.getArriveType())){
						hits++;
						buffer.append(" ArriveType match");
					}
					if(isStringMatch(ordOrderItem.getRealBuType(), auditSortRule.getBu())){
						hits++;
						buffer.append(" Bu match");
					}
					if(isIdValueMatch(ordOrderItem.getProductId(), auditSortRule.getObjectId())){
						hits++;
						buffer.append(" ObjectId match");
					}
					if(isIdValueMatch(ordOrderItem.getSupplierId(), auditSortRule.getSupplierId())){
						hits++;
						buffer.append(" SupplierId match");
					}
					if(isStringMatch(ordOrderItem.isRoomReservations()?"Y":"N", auditSortRule.getImmediatelyFlag())){
						hits++;
						buffer.append(" ImmediatelyFlag match");
					}
					//增加渠道比较
					if(isIdValueChannelMatch(order, auditSortRule.getOrderChannel())){
						hits++;
						buffer.append(" distributionChannel match");
					}
					//增加订单资源状态比较
					if(isStringMatch(order.getResourceStatus(),auditSortRule.getOrderResourceStatus())){
						hits++;
						buffer.append(" resourceStatus match");
					}
					buffer.append(" hits:"+hits+"||");
					if(hits > maxMatchValue){
						maxMatchValue = hits;
						matchComAuditSortRule = auditSortRule;
						if(maxMatchValue == 7){//修改max匹配值
							LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem matched SortRuleId:"
									+ matchComAuditSortRule.getSortRuleId()
									+ " costTime:"
									+ (System.currentTimeMillis() - startTime));
							return matchComAuditSortRule;
						}
					}
				}
			}
			if(matchComAuditSortRule == null){
				LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem matchComAuditSortRule is null costTime:"
						+ (System.currentTimeMillis() - startTime));
				buffer.append("...match end, not matched costTime:" + (System.currentTimeMillis() - startTime));
			}else{
				LOGGER.info("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem matched, SortRuleId:"
						+ matchComAuditSortRule.getSortRuleId()
						+ " costTime:"
						+ (System.currentTimeMillis() - startTime));
				buffer.append("...match end, matched SortRuleId:"+ matchComAuditSortRule.getSortRuleId() + " costTime:"
						+ (System.currentTimeMillis() - startTime));
			}
			LOGGER.info(buffer.toString());
		} catch (Exception e) {
			LOGGER.error("OrderAuditSortRuleServiceImpl getComAuditSortRuleByOrderItem error orderItemId:"
					+ ordOrderItem.getOrderItemId() + " msg:" + e.getMessage());
			e.printStackTrace();
		}
		return matchComAuditSortRule;
	}

	/**
	 * 计算到店时间
	 * @param order
	 * @param ordOrderItem
	 * @return
	 * @author majunli
	 * @date 2016-10-19 下午8:36:54
	 */
	private String getOrderArriveType(OrdOrder order ,OrdOrderItem ordOrderItem){
		String ArriveTypeCode = "";
		if (order != null){
			Date payDate = order.getPaymentTime();
			if(payDate == null){
				return ArriveTypeCode;
			}
			//支付时间
			long payTime = order.getPaymentTime().getTime();
			//支付时间到次日凌晨0点剩余时间
			long payDayLast = DateUtil.getDateAfter0000Date(order.getPaymentTime(), 1).getTime() - payTime;
			//游玩时间
			long visitTime = ordOrderItem.getVisitTime().getTime();
			//如果是预付+12小时
			if(order.getPaymentTarget().equals(SuppGoods.PAYTARGET.PREPAID)){
				visitTime = visitTime + 1000*60*60*12;
			}else{
				//最晚到店时间
				String lastTime = (String) ordOrderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name());
				if(StringUtil.isNotEmptyString(lastTime) && lastTime.split(":").length>0){
					LOGGER.info("getOrderArriveType lastTime:" + lastTime);
					visitTime = visitTime + 1000*60*60*Integer.parseInt(lastTime.split(":")[0]);
				}else{
					visitTime = visitTime + 1000*60*60*12;
				}
			}
			
			/*if(visitTime - payTime < 1000*60*60*2){
				//马上到店
				ArriveTypeCode = ARRIVE_TYPE.ARRIVE_IMMEDIATELY.name();
			}else */
			if((visitTime - payTime) < payDayLast){
				//今日到店
				ArriveTypeCode = ARRIVE_TYPE.ARRIVE_TODAY.name();
			}else if((visitTime - payTime) >= payDayLast && (visitTime - payTime) < (1000*60*60*24 + payDayLast)){
				//次日到店
				ArriveTypeCode = ARRIVE_TYPE.ARRIVE_MORROW.name();
			}else if((visitTime - payTime) >= (1000*60*60*24 + payDayLast)){
				//次日之后到店
				ArriveTypeCode = ARRIVE_TYPE.ARRIVE_MORROW_AFTER.name();
			}
		}
		return ArriveTypeCode;
	}
	
	private boolean isStringMatch(String str1, String str2){
		boolean result = false;
		if(StringUtil.isEmptyString(str1) && StringUtil.isEmptyString(str2)){
			result = true;
		}else if(StringUtil.isNotEmptyString(str1) && StringUtil.isNotEmptyString(str2)){
			if(str1.equals(str2)){
				result = true;
			}
		}
		return result;
	}
	
	private boolean isIdValueMatch(Long id, String ids){
		boolean result = false;
		if(id == null && StringUtil.isEmptyString(ids)){
			result = true;
		}else if(id != null && StringUtil.isNotEmptyString(ids)){
			StringTokenizer token=new StringTokenizer(ids,",");
	        while(token.hasMoreElements()){
	        	 if(String.valueOf(id).equals(token.nextToken())){
	        		 result = true;
	        	 }
	        }
		}
		return result;
	}
	
	private boolean isIdValueChannelMatch(OrdOrder order,String ids){
		boolean result = false;
		if(order == null && StringUtil.isEmptyString(ids)){
			result = true;
		}else if(StringUtil.isNotEmptyString(ids)){
			//通过id判断渠道
			String distributionType=switchDistributionChannel(order);
			StringTokenizer token=new StringTokenizer(ids,",");
	        while(token.hasMoreElements()){
	        	 if(distributionType.equals(token.nextToken())){
	        		 result = true;
	        	 }
	        }
		}
		return result;
	}
	
	private String switchDistributionChannel(OrdOrder order){
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if(null!=order.getDistributorCode()&&DISTRIBUTOR_CODE_TAOBAO.equals(order.getDistributorCode())){
			return "taobao";
		}else if(Constant.DIST_BACK_END==order.getDistributorId()
				||Constant.DIST_FRONT_END==order.getDistributorId()
				||Constant.DIST_O2O_SELL==order.getDistributorId() 
				||Constant.DIST_O2O_APP_SELL == order.getDistributorId()
				||(null!=order.getDistributionChannel()&&ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue()))){
			return "neither";
		}else{
			return "other";
		}
	}
}
