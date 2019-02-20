package com.lvmama.vst.order.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.lvmama.crm.enumerate.CsVipStaffUserRelationEnum;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.service.IOrderAuditSortRuleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.lvmama.vst.back.prod.po.ProdDestRe;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;


import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComAuditActiviNum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

@Service("orderAuditService")
public class OrderAuditServiceImpl implements IOrderAuditService{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderAuditServiceImpl.class);

	//品类ids
	public final static List<Long> categoryIds = Arrays.asList(
			BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(),
			BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId(),
			BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId(),
			BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId());

	@Autowired
	private ComAuditDao comAuditDao;

	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private OrdUserCounterDao userCounterDao;
	
	@Autowired
	private OrdResponsibleDao responsibleDao;
		
	@Autowired
	private  OrdOrderItemDao ordOrderItemDao;

	@Autowired
	private OrdOrderStockDao orderStockDao;

	@Autowired
	private IOrderAuditSortRuleService orderAuditSortRuleService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	@Override
	public int saveAudit(ComAudit comAudit) {
		return comAuditDao.insert(comAudit);
	}
	
	@Autowired
	private OrdAuditUserStatusDAO auditUserStatusDAO;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private ProdPackageGroupClientService prodPackageGroupClientRemote;
	
	/**
	 * 产生订单活动并且保存 待分配 状态
	 * 
	 * @param
	 */
	public ComAudit saveCreateOrderAudit(Long objectId,String objectType,String auditType){
		
		ComAudit audit =null;
		if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.trim())) {
			
			audit= this.saveCreateOrderAudit(objectId, auditType);
			
		}else if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType.trim())) {
			
			OrdOrderItem ordOrderItem=ordOrderItemDao.selectByPrimaryKey(objectId);
			Long orderId=ordOrderItem.getOrderId();
			
			audit=this.saveCreateChildOrderAudit(orderId, objectId, auditType);
			
		}
		
		return audit;
	}
	/**
	 * 产生活动并且保存
	 * 
	 * @param
	 */
	public ComAudit saveCreateOrderAudit(Long orderId,String auditType){
		
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setObjectId(orderId);
		audit.setAuditType(auditType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		comAuditDao.insert(audit);
		
		this.insertOrderLog(orderId, auditType);
		logger.info("audit:" + audit);
		return audit;
		
	}
	
	public ComAudit saveOrderAudit(Long orderId,String auditType,String  auditSubType){
		
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setObjectId(orderId);
		audit.setAuditType(auditType);
		audit.setAuditSubtype(auditSubType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		comAuditDao.insert(audit);
		
		this.insertOrderLog(orderId, auditType);
		
		return audit;
		
	}
	/**
	 * 产生子订单活动并且保存
	 * 
	 * @param
	 */
	public ComAudit saveCreateChildOrderAudit(Long orderId,Long orderItmeId,String auditType){
		logger.info("orderId:" + orderId + ", orderItemId:" + orderItmeId + ", auditType:" + auditType);
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		audit.setObjectId(orderItmeId);
		audit.setAuditType(auditType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		comAuditDao.insert(audit);
		
		this.insertChildOrderLog(orderId, orderItmeId, auditType);
		logger.info("audit:" + audit);
		return audit;
		
	}
	
	/**
	 * 产生子订单活动并且保存
	 * 
	 * @param
	 */
	public ComAudit saveChildOrderAudit(Long orderId,Long orderItmeId,String auditType,String auditSubType){
		
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		audit.setObjectId(orderItmeId);
		audit.setAuditType(auditType);
		audit.setAuditSubtype(auditSubType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		comAuditDao.insert(audit);
		
		this.insertChildOrderLog(orderId, orderItmeId, auditType);
		
		return audit;
		
	}
	

	public ComAudit queryAuditById(Long auditId){
		return comAuditDao.selectByPrimaryKey(auditId);
	}
	
	@Override
	public int updateComAuditByCondition(ComAudit comAudit) {
		return comAuditDao.updateByPrimaryKeySelective(comAudit);
	}
	
	/**
	 * 按条件更新订单审核信息
	 * 
	 * @param comAudit
	 * @return
	 */
	public int updateByPrimaryKey(ComAudit comAudit){
		return comAuditDao.updateByPrimaryKey(comAudit);
	}
	
	/*
	 *  只更新未取消，非已处理的订单
	 * @see com.lvmama.vst.order.service.IOrderAuditService#updateByPrimaryKeyNew(com.lvmama.vst.back.pub.po.ComAudit)
	 */
	public int updateByPrimaryKeyNew(ComAudit comAudit){
		return comAuditDao.updateByPrimaryKeyNew(comAudit);
	}
	
	
	@Override
	public List<ComAudit> queryAuditListByCondition(Map<String, Object> param) {
		return comAuditDao.queryAuditListByCondition(param);
	}

	/**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByParam(Map<String, Object> param){
		return comAuditDao.queryAuditListByParam(param);
	}

	/**
	 * map动态查询（我的工作台）
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByCriteria(Map<String, Object> param){
		return comAuditDao.queryAuditListByCriteria(param);
	}

	/**
	 * 每天更新最近出游的已审活动排序值
	 */
	public void updateOrderAuditSeqByJob(Long orderItemId, String nowDate) {
		//组装订单审核列表条件
		Map<String, Object> auditParam = new HashMap<String, Object>();
		auditParam.put("categoryIds", categoryIds);

		auditParam.put("orderItemId", orderItemId);

		auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		auditParam.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name());
		auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		auditParam.put("isReturnBack", false);

		Date visitTimeEnd = DateUtil.getDayStart(DateUtil.addDays(new Date(), 1));
		Date visitTimeBegin = DateUtil.getDayStart(new Date());
		auditParam.put("visitTimeBegin", visitTimeBegin);
		auditParam.put("visitTimeEnd", visitTimeEnd);

		logger.info("auditParams="+auditParam);
		//查询订单审核列表集合
		List<ComAudit> auditList = this.queryDestAuditListByCriteria(auditParam);

		if(CollectionUtils.isNotEmpty(auditList)){
			Long seq;
			Date now = null;
			if (StringUtil.isNotEmptyString(nowDate)) {
				try {
					now = DateUtil.stringToDate(nowDate, DateUtil.SIMPLE_DATE_FORMAT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (now == null) {
				now = new Date();
			}
			for (ComAudit audit : auditList) {
				OrdOrderItem ordOrderItem = ordOrderItemDao.selectByPrimaryKey(audit.getObjectId());
				if(ordOrderItem != null){
					List<OrdOrderStock> orderStockList = orderStockDao.selectByOrderItemId(ordOrderItem.getOrderItemId());
					ordOrderItem.setOrderStockList(orderStockList);
					ComAuditSortRule comAuditSortRule = orderAuditSortRuleService.getComAuditSortRuleByOrderItemByJob(ordOrderItem, now);
					if(comAuditSortRule != null){
						seq = audit.getSeq();
						audit.setSeq(comAuditSortRule.getSeq());
						int result = comAuditDao.updateComAuditSeqByJob(audit);
						logger.info("updateSeq，objectId:" +audit.getObjectId() +",result:" +result);
						if(result > 0){
							//创建日志
							createUpdateAuditSeqLog(ordOrderItem.getOrderId(), audit, seq);
						}
					}
				}
			}
		} else {
			logger.info("updateOrderAuditSeqByJob auditList is null");
		}
	}
	/**
	 * 预约中的订单
	 * @param param
	 * @return
	 */
	public int updateRemindTimeByAuditId(Map<String, Object> param) throws Exception {
		return comAuditDao.updateRemindTimeByAuditId(param);
	}
	
	public Integer getTotalCount(Map<String, Object> param) {
		
		
		return comAuditDao.getTotalCount(param);
	}
	@Override
	public int countAuditByCondition(Map<String, Object> param) {
		Integer count = comAuditDao.countAuditByCondition(param);
		if(count==null){
			return 0;
		}
		return count;
	}
	
	@Override
	public int countAuditByRaid(Map<String, Object> param) {
		Integer count = comAuditDao.countAuditByRaid(param);
		if(count==null){
			return 0;
		}
		return count;
	}

	/**
	 * 动态统计我的工作台查询记录数
	 * @param param
	 * @return
	 */
	public int countAuditByMyWork(Map<String, Object> param){	
		Integer count = comAuditDao.countAuditByMyWork(param);
		if(count==null){
			return 0;
		}
		return count;
	}
	
	public int countAuditByDestWork(Map<String, Object> param){	
		Integer count = comAuditDao.countAuditByDestWork(param);
		if(count==null){
			return 0;
		}
		return count;
	}
	
	public List<ComAudit> queryDestAuditListByCriteria(Map<String, Object> param){
		return comAuditDao.queryDestAuditListByCriteria(param);
	}
	
	@Override
	public List<ComAudit> queryComAuditListByPool(Map<String, Object> param) {
		return comAuditDao.queryComAuditListByPool(param);
	}
	
	@Override
	public List<ComAudit> queryComAuditListByProcessed(Map<String, Object> param) {
		return comAuditDao.queryComAuditListByProcessed(param);
	}

	@Override
	public int updateComAuditByPool(Map<String, Object> param){
		return comAuditDao.updateComAuditByPool(param);
	}
	
	@Override
	public int updateComAuditByUnProcessed(Map<String, Object> param) {
		return comAuditDao.updateComAuditByUnProcessed(param);
	}
	
	@Override
	public int updateComAuditByProcessed(Map<String, Object> param) {
		return comAuditDao.updateComAuditByProcessed(param);
	}
		
	
	/**
	 * 根据条件统计活动数量
	 * 
	 * @param param
	 * @return
	 */
	public int countActivityNum(Map<String, Object> param){
		return comAuditDao.countActivityNum(param);
	}
	
	/**
	 * 根据条件统计订单数量
	 * @param param
	 * @return
	 */
	public int countOrderNum(Map<String, Object> param){
		return comAuditDao.countOrderNum(param);
	}

	@Override
	public int updateComAuditToProcessed(Long orderId,String auditType,String operatorName) {
		return comAuditDao.updateComAuditToProcessed(orderId, auditType, operatorName);
	}
	
	public int updateChildOrderAuditToProcessed(Long orderId,String auditType,String operatorName) {
		return comAuditDao.updateComAuditToProcessed(orderId, auditType, operatorName);
	}
	
	/**
	 * 
	 * 保存分单日志
	 * 
	 */
	private void insertOrderAssignLog(final Long orderId,Long objectId,String objectTypeStr,String auditType,String operator){
		ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		if(objectTypeStr!=null && OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectTypeStr)){
			objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}
		lvmmLogClientService.sendLog(objectType,
				orderId, 
				objectId, 
				Constants.SYSTEM, 
				"将编号为["+objectId+"]的订单分给员工["+operator+"]进行["+ ConfirmEnumUtils.getCnName(auditType)+"]任务",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.getCnName()+"["+auditType+"]",
				"系统自动分单");
	}
	private void insertChildConfirmAssignLog(final Long orderId,Long objectId,String objectTypeStr,String auditType,String operator){
		ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		if(objectTypeStr!=null && OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectTypeStr)){
			objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}
		lvmmLogClientService.sendLog(objectType,
				orderId, 
				objectId, 
				Constants.SYSTEM, 
				"将编号为["+objectId+"]的订单分给员工["+operator+"]进行["+Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(auditType)+"]任务", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.getCnName()+"["+auditType+"]",
				"系统自动分单");
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertOrderLog(final Long orderId,String auditType){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				Constants.SYSTEM, 
				"编号为["+orderId+"]的订单,系统自动创建订单活动["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				null);
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertChildOrderLog(final Long orderId,final Long orderItemId,String auditType){
		ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		if(orderItemId!=null){
			objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}
		lvmmLogClientService.sendLog(objectType,
				orderId, 
				orderItemId, 
				Constants.SYSTEM, 
				"将编号为["+orderItemId+"]的订单,系统自动创建订单活动["+ ConfirmEnumUtils.getCnName(auditType) +"]",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				null);
	}

	@Override
	public void markValid(Long auditId) {
		comAuditDao.markValid(auditId);
	}
	
	@Override
	public void markCanNotReaudit(Long auditId) {
		comAuditDao.markCanNotReaudit(auditId);
	}

	@Override
	public ComAudit updateAuditAssign(ComAudit audit, OrdOrderItem item,boolean isNew,boolean hasCustomeUser,OrdAuditUserStatus user,boolean isOrderAndItemBuEqual, boolean isCsvip, boolean isLocalVip, String csUserId) {
		String operator = null;
		if(isNew) // 新分单规则
			operator = audit.getOperatorName();
		else
			operator = user.getOperatorName();
	
		audit.setOperatorName(operator);
		audit.setUpdateTime(new Date());
		int ret = updateByPrimaryKeyNew(audit);
		logger.info("OrderAuditServiceImpl updateAuditAssign ===orderId:"+item.getOrderId()+",===OperatorName"+operator+",auditId="+audit.getAuditId()+",auditStatus="+audit.getAuditStatus()+",auditType="+audit.getAuditType()+",ret:"+ret);
		if (ret < 1) {
			return null;
		}
		if(Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
			//保存分单日志
			insertChildConfirmAssignLog(item.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}else{
			//保存分单日志
			insertOrderAssignLog(item.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}
		OrdResponsible res = responsibleDao.getResponsibleByObject(audit.getObjectId(),audit.getObjectType());
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(item.getOrderId());
		boolean isDriectSale = isDriectSale(ordOrder,audit);
		boolean isLocalO2O = isLocalO2O(ordOrder);
		boolean isOutBoundO2O = isOutBoundO2O(ordOrder);
		//是否直营门店分店
		logger.info("===orderId:"+ordOrder.getOrderId()+".isDriectSale:"+isDriectSale+"===");
		//渠道为门店、门店APP的国内度假事业部 跟团SBU、机酒SBU订单屏蔽预定通知
		logger.info("===orderId:"+ordOrder.getOrderId()+".isLocalO2O:"+isLocalO2O+"===");
		//渠道为门店、门店APP的出境跟团，自由行，当地游，邮轮订单,主订单负责人分配给指定工号
		logger.info("===orderId:"+ordOrder.getOrderId()+".isOutBoundO2O:"+isOutBoundO2O+"===");
		boolean isbusAndHotel=isBusAndhotel(ordOrder);
		logger.info("===orderId:"+ordOrder.getOrderId()+",auditId:"+audit.getAuditId()+ ",hasCustomeUser:" + hasCustomeUser + "===");
		//是否走国内机酒分单规则
		boolean isNewAllocationRule = isNewAllocationRule(ordOrder,item,audit);
		logger.info("===orderId:"+ordOrder.getOrderId()+".isNewAllocationRule:"+isNewAllocationRule+"===");
		if(isDriectSale && res == null && OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
			saveOrderResponsibleForDriectSale(audit, ordOrder,res);
		}
        if(isOutBoundO2O){
            OrdResponsible ordRes = responsibleDao.getResponsibleByObject(item.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
            if(ordRes == null){
                saveOrderResponsibleForOutBoundO2O(audit, ordOrder);
            }
        }
        
		//判断当前组织属于客服,定义成订单负责人,并且不存在负责人
        if(!OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())&&res==null&&(isbusAndHotel||isLocalO2O||isLocalVip)){
			if(isbusAndHotel){
				saveResponsibleForBusAndHotel(audit, item, ordOrder);
			}else if(isLocalO2O){
				saveResponsibleForLocalO2O(audit, item,user, ordOrder,hasCustomeUser);
			}else if(isLocalVip&&hasCustomeUser){
				saveResponsibleForLocalVip(audit, item, ordOrder,user );
			}
		}else if(!OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())&&res==null&&hasCustomeUser){
			if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(ordOrder.getBuCode())){
				saveResponsibleForOutBoundBu(audit, item, user, ordOrder, isOrderAndItemBuEqual, isOutBoundO2O, isCsvip,isDriectSale);
			} else {
				saveResponsible(audit, item, user, isOrderAndItemBuEqual,isDriectSale,isNewAllocationRule);
			}
		}else if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("auditId", audit.getAuditId());
			List<ComMessage> list = comMessageDao.findComMessageList(params);
			if(list.isEmpty()){//如果是预定通知，通知不存在就直接关掉
				comAuditDao.markValid(audit.getAuditId());
				if (logger.isWarnEnabled()) {
					logger.warn("updateAuditAssign(ComAudit, OrdOrderItem, boolean, boolean, OrdAuditUserStatus) - 预订通知不存在 auditId:"+audit.getAuditId()); //$NON-NLS-1$
				}
			}else{
				ComMessage msg = list.get(0);
				if(OrderEnum.MESSAGE_STATUS.PROCESSED.name().equalsIgnoreCase(msg.getMessageStatus())){
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateByPrimaryKey(audit);
				}else{
					msg.setReceiver(audit.getOperatorName());
					comMessageDao.updateByPrimaryKey(msg);
				}
			}
		}
		return audit;
	}
	
	private void saveOrderResponsibleForDriectSale(ComAudit audit,
			OrdOrder order, OrdResponsible res) {
		if(StringUtils.isNotEmpty(order.getBackUserId())) {
			PermUser permUser =permUserServiceAdapter.getPermUserByUserName(order.getBackUserId());
			if (permUser != null) {
			    OrdResponsible	ordRes = new OrdResponsible();
				ordRes.setObjectId(audit.getObjectId());
				ordRes.setObjectType(audit.getObjectType());
				ordRes.setOperatorName(permUser.getUserName());
				ordRes.setOrgId(permUser.getDepartmentId());
				ordRes.setCreateTime(new Date());
				int ret = responsibleDao.insert(ordRes);
				if(ret>0){
					res=ordRes;
				}
				userCounterDao.increase(ordRes.getOperatorName(), ordRes.getObjectType());
			}
		}
		
	}
	private boolean isDriectSale(OrdOrder order, ComAudit audit) {
		Long distributorId = order.getDistributorId();
		String distributorCode = order.getDistributorCode();
		if(distributorId==null||distributorCode==null){
			return false;
		}
		String[] DISTRIBUTION_CODE_LIST ={"E0001","E0002","E0003","E0004","E0005","E0008"};
		if(Constant.DIST_BACK_END==distributorId&&ArrayUtils.contains(DISTRIBUTION_CODE_LIST, distributorCode)){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId().equals(order.getCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId().equals(order.getCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId()) 
					||BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCategoryId().equals(order.getCategoryId()) 
					||BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(order.getCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) 
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(order.getCategoryId()) 
					||(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
					&&BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode()))
					||(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId()) 
					&&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()))){
				return true;
			}
		}
		return false;
	}
	@Override
	public ComAudit updateAuditAssignForDriectSale(ComAudit audit,
			OrdOrder order, String operatorName) {
		audit.setUpdateTime(new Date());
		int ret = updateByPrimaryKeyNew(audit);
		if (ret < 1) {
			return null;
		}
		if(Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
			//保存分单日志
			insertChildConfirmAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}else{
			//保存分单日志
			insertOrderAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}
		if(StringUtils.isNotEmpty(order.getBackUserId())) {
			PermUser permUser =permUserServiceAdapter.getPermUserByUserName(order.getBackUserId());
			OrdResponsible ordRes = responsibleDao.getResponsibleByObject(audit.getObjectId(), audit.getObjectType());
			if (ordRes == null && permUser != null) {
				ordRes = new OrdResponsible();
				ordRes.setObjectId(audit.getObjectId());
				ordRes.setObjectType(audit.getObjectType());
				ordRes.setOperatorName(permUser.getUserName());
				ordRes.setOrgId(permUser.getDepartmentId());
				ordRes.setCreateTime(new Date());
				responsibleDao.insert(ordRes);
				userCounterDao.increase(ordRes.getOperatorName(), ordRes.getObjectType());
				logger.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " updateAuditAssignForDriectSale insert ordRes success");
			}
		}
		return audit;
	}

	@Override
	public ComAudit updateAuditAssignForVip(ComAudit audit, OrdOrder order, String csUserId, String operatorName) {
		audit.setUpdateTime(new Date());
		int ret = updateByPrimaryKeyNew(audit);
		if (ret < 1) {
			return null;
		}
		if(Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
			//保存分单日志
			insertChildConfirmAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}else{
			//保存分单日志
			insertOrderAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}
		if((OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(audit.getAuditType()))
				|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType()))
				|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType()))){
			saveOrdResForVip(audit, order);
		}else if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("auditId", audit.getAuditId());
			List<ComMessage> list = comMessageDao.findComMessageList(params);
			if(list.isEmpty()){//如果是预定通知，通知不存在就直接关掉
				comAuditDao.markValid(audit.getAuditId());
				if (logger.isWarnEnabled()) {
					logger.warn("updateAuditAssign(ComAudit, OrdOrderItem, boolean, boolean, OrdAuditUserStatus) - 预订通知不存在 auditId:"+audit.getAuditId()); //$NON-NLS-1$
				}
			}else{
				ComMessage msg = list.get(0);
				if(OrderEnum.MESSAGE_STATUS.PROCESSED.name().equalsIgnoreCase(msg.getMessageStatus())){
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateByPrimaryKey(audit);
				}else{
					msg.setReceiver(audit.getOperatorName());
					comMessageDao.updateByPrimaryKey(msg);
				}
			}
		}
		return audit;
	}
	
	@Override
	public ComAudit updateAuditAssignForLocalVip(ComAudit audit,
			OrdOrder order, String csUserId, String operatorName) {
		audit.setUpdateTime(new Date());
		int ret = updateByPrimaryKeyNew(audit);
		if (ret < 1) {
			return null;
		}
		if(Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
			//保存分单日志
			insertChildConfirmAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}else{
			//保存分单日志
			insertOrderAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}
		if(!OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			saveOrdResForVip(audit, order);
		}else if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("auditId", audit.getAuditId());
			List<ComMessage> list = comMessageDao.findComMessageList(params);
			if(list.isEmpty()){//如果是预定通知，通知不存在就直接关掉
				comAuditDao.markValid(audit.getAuditId());
				if (logger.isWarnEnabled()) {
					logger.warn("updateAuditAssign(ComAudit, OrdOrderItem, boolean, boolean, OrdAuditUserStatus) - 预订通知不存在 auditId:"+audit.getAuditId()); //$NON-NLS-1$
				}
			}else{
				ComMessage msg = list.get(0);
				if(OrderEnum.MESSAGE_STATUS.PROCESSED.name().equalsIgnoreCase(msg.getMessageStatus())){
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateByPrimaryKey(audit);
				}else{
					msg.setReceiver(audit.getOperatorName());
					comMessageDao.updateByPrimaryKey(msg);
				}
			}
		}
		return audit;
	}

	@Autowired
	private ComMessageDao comMessageDao;

	/**
	 * 保存订单负责人
	 * @param audit
	 * @param order
	 */
	private void saveOrdResForVip(ComAudit audit, OrdOrder order){
		logger.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " ordResName:"+audit.getOperatorName());
		if(StringUtils.isNotEmpty(audit.getOperatorName())) {
			PermUser permUser =permUserServiceAdapter.getPermUserByUserName(audit.getOperatorName());
			OrdResponsible ordRes = responsibleDao.getResponsibleByObject(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if (ordRes == null && permUser != null) {
				ordRes = new OrdResponsible();
				ordRes.setObjectId(order.getOrderId());
				ordRes.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				ordRes.setOperatorName(permUser.getUserName());
				ordRes.setOrgId(permUser.getDepartmentId());
				ordRes.setCreateTime(new Date());
				responsibleDao.insert(ordRes);
				userCounterDao.increase(ordRes.getOperatorName(), ordRes.getObjectType());
				logger.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " updateAuditAssignForVip insert ordRes success");
			}
		}
	}

	private boolean isLocalO2O(OrdOrder order) {
		if(Constant.DIST_O2O_SELL==order.getDistributorId()||Constant.DIST_O2O_APP_SELL==order.getDistributorId()){
			if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				return true;
			}
		}
		return false;
	}
	
	private boolean isOutBoundO2O(OrdOrder order) {
		if(Constant.DIST_O2O_SELL==order.getDistributorId()||Constant.DIST_O2O_APP_SELL==order.getDistributorId()){
			if(Constant.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId().longValue())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId().longValue())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId().longValue())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId().longValue())){
					return true;
				}
			}
		}
		return false;
	}

	public boolean isBusAndhotel(OrdOrder order){
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
				&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}
		if(isInclueLocalBuOrDestinationBu(order)){
			logger.info("===orderId:"+order.getOrderId()+".productId:"+order.getProductId()+"===");
			long productId=0l;
			if(null!=order.getOrdOrderPack()){
				productId=order.getOrdOrderPack().getProductId();
			}else{
				productId=order.getMainOrderItem().getProductId();
			}
			ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(productId);
			List<ProdPackageGroup> prodPackgeGroupList = prodPackageGroupClientRemote.getProdPackageGroupByProductId(productId);
			List<ProdProduct> listChildProduct=prodProductClientService.findProductByProductId(productId);
			ProdProduct product = result.getReturnContent();
			if(product==null){
				logger.error("====orderId:"+order.getOrderId()+"====product is null===");
				return false;
			}
			if(null!=prodPackgeGroupList){
				product.setProdPackgeGroupList(prodPackgeGroupList);
			}
			if(!isCategory(order,product)){
				return false;
			}
			//自主打包，交通只包含bus
			if(null!=product.getPackageType()&&ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(product.getPackageType())){
				if(null!=listChildProduct){
					boolean isBusOther=false;
					boolean isHotel=false;
					boolean isOther=false;
					for (ProdProduct element : listChildProduct) {
						if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().longValue()==element.getBizCategoryId().longValue()){
							isBusOther=true;
						}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getBizCategoryId().longValue()){
							isHotel=true;
						}else{
							isOther=true;
						}
					}
					if(isBusOther&&isHotel&&!isOther){
						return true;
					}
				}
			}else if(null!=product.getPackageType()&&ProdProduct.PACKAGETYPE.SUPPLIER.getCode().equals(product.getPackageType())){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId().longValue())
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId().longValue())){
					if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断是否国内bu或者目的地bu
	 */
	public boolean isInclueLocalBuOrDestinationBu(OrdOrder order){
		//如果是目的地bu或者是国内bu
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
				Constant.BU_NAME.DESTINATION_BU.equals(order.getBuCode())){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断品类含单酒、景酒、酒套餐、国内跟团-长线、国内跟团-短线、国内当地游-长线、国内当地游-短线、机酒、交通+服务
	 */
	public boolean isCategory(OrdOrder order,ProdProduct product){
		long categoryId=order.getCategoryId().longValue();
		
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)){//单酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)){//酒套餐
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())){//景酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)){//国内跟团
			return findProductType(order,product);
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)){//国内当地游
			return findProductType(order,product);
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())){//机酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().equals(order.getSubCategoryId())){//交通+服务
			return true;
		}
		return false;
	}
	
	/**
	 * 查询产品类型是否属于长线或短线
	 * @param order
	 * @return
	 */
	private boolean findProductType(OrdOrder order,ProdProduct product) {
		//-长线-短线
		if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType()) 
				|| PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(product.getProductType())){	
			return true;
		}
		return false;
	}
	
	private void saveResponsibleForLocalO2O(ComAudit audit, OrdOrderItem item,
			OrdAuditUserStatus user, OrdOrder ordOrder, boolean hasCustomeUser) {
		PermUser permUser =permUserServiceAdapter.getPermUserByUserName("cs6053");
		if(null==permUser){
			permUser=permUserServiceAdapter.getPermUserByUserId(ordOrder.getManagerId());
		}
		logger.info("orderId:" + ordOrder.getOrderId() +  "auditType:" + audit.getObjectType() + ", isLocalO2O:true" );
		OrdResponsible res;
		Date createTime = new Date();
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())&&hasCustomeUser){
			res = new OrdResponsible();
			res.setObjectId(audit.getObjectId());
			res.setObjectType(audit.getObjectType());
			res.setOperatorName(user.getOperatorName());
			res.setOrgId(user.getOrgId());
			res.setCreateTime(createTime);
			responsibleDao.insert(res);
			userCounterDao.increase(res.getOperatorName(),res.getObjectType());
		}
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
			res = responsibleDao.getResponsibleByObject(item.getOrderId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if(res==null){
				logger.info("orderId:" + ordOrder.getOrderId() + "res:" + res);
				res = new OrdResponsible();
				res.setObjectId(item.getOrderId());
				res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				res.setOperatorName(permUser.getUserName());
				res.setOrgId(permUser.getDepartmentId());
				res.setCreateTime(createTime);
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(),res.getObjectType());
			}
		}else{
			res = new OrdResponsible();
			res.setObjectId(audit.getObjectId());
			res.setObjectType(audit.getObjectType());
			res.setOperatorName(permUser.getUserName());
			res.setOrgId(permUser.getDepartmentId());
			res.setCreateTime(createTime);
			responsibleDao.insert(res);
			userCounterDao.increase(res.getOperatorName(),res.getObjectType());
		}
	}

	private void saveOrderResponsibleForOutBoundO2O(ComAudit audit, OrdOrder ordOrder) {
		PermUser permUser =permUserServiceAdapter.getPermUserByUserName("cs6053");
		if(null==permUser){
			permUser=permUserServiceAdapter.getPermUserByUserId(ordOrder.getManagerId());
		}
		logger.info("orderId:" + ordOrder.getOrderId() +  "auditId:" + audit.getAuditId() + ", isOutBoundO2O:true" );
		OrdResponsible res = responsibleDao.getResponsibleByObject(ordOrder.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		if (res == null && permUser != null) {
			logger.info("orderId:" + ordOrder.getOrderId() + "auditId::" + audit.getAuditId());
			res = new OrdResponsible();
			res.setObjectId(ordOrder.getOrderId());
			res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			res.setOperatorName(permUser.getUserName());
			res.setOrgId(permUser.getDepartmentId());
			res.setCreateTime(new Date());
			responsibleDao.insert(res);
			userCounterDao.increase(res.getOperatorName(),res.getObjectType());
		}
	}
	
	private void saveResponsibleForBusAndHotel(ComAudit audit, OrdOrderItem item,
			 OrdOrder ordOrder ) {
		PermUser permUser =permUserServiceAdapter.getPermUserByUserId(ordOrder.getManagerId());
		logger.info("orderId:" + ordOrder.getOrderId() +  "auditType:" + audit.getObjectType() + ", isbusAndHotel:true" );
		OrdResponsible res;
		Date createTime = new Date();
		res = new OrdResponsible();
		res.setObjectId(audit.getObjectId());
		res.setObjectType(audit.getObjectType());
		res.setOperatorName(permUser.getUserName());
		res.setOrgId(permUser.getDepartmentId());
		res.setCreateTime(createTime);
		responsibleDao.insert(res);
		userCounterDao.increase(res.getOperatorName(),res.getObjectType());
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
			res = responsibleDao.getResponsibleByObject(item.getOrderId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if(res==null){
				logger.info("orderId:" + ordOrder.getOrderId() + "res:" + res);
				res = new OrdResponsible();
				res.setObjectId(item.getOrderId());
				res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				res.setOperatorName(permUser.getUserName());
				res.setOrgId(permUser.getDepartmentId());
				res.setCreateTime(createTime);
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(),res.getObjectType());
			}
		}
	}
	
	private void saveResponsibleForLocalVip(ComAudit audit, OrdOrderItem item,
			OrdOrder ordOrder, OrdAuditUserStatus user) {
		OrdResponsible res;
		Date createTime = new Date();
		res = new OrdResponsible();
		res.setObjectId(audit.getObjectId());
		res.setObjectType(audit.getObjectType());
		res.setOperatorName(user.getOperatorName());
		res.setOrgId(user.getOrgId());
		res.setCreateTime(createTime);
		responsibleDao.insert(res);
		userCounterDao.increase(res.getOperatorName(),res.getObjectType());
	}
	
	private void saveResponsibleForOutBoundBu(ComAudit audit, OrdOrderItem item,
			OrdAuditUserStatus user, OrdOrder ordOrder, boolean isOrderAndItemBuEqual, boolean isOutBoundO2O, boolean isCsVip, boolean isDriectSale) {
		boolean isOutBoundChangtanCateGory = isOutBoundChangtanCateGory(ordOrder);
		boolean isOutBoundSomeCateGory = isOutBoundSomeCateGory(ordOrder);
		logger.info("===orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",operatorName:" + audit.getOperatorName() + ",isOutBoundChangtanCateGory:" + isOutBoundChangtanCateGory + ",isOutBoundSomeCateGory:" + isOutBoundSomeCateGory + "===");
		//如果是出境长滩自营产品
		if (isOutBoundChangtanCateGory) {
			if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())
					&& (OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(audit.getAuditType()) || OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equalsIgnoreCase(audit.getAuditType()))) {
				OrdResponsible res;
				Date createTime = new Date();
				res = new OrdResponsible();
				res.setObjectId(audit.getObjectId());
				res.setObjectType(audit.getObjectType());
				res.setOperatorName(user.getOperatorName());
				res.setOrgId(user.getOrgId());
				res.setCreateTime(createTime);
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(),res.getObjectType());
			}
			OrdResponsible res = responsibleDao.getResponsibleByObject(item.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if (res == null && !isOutBoundO2O && !isCsVip) {
				PermUser permUser = permUserServiceAdapter.getPermUserByUserId(ordOrder.getManagerId());
				logger.info("orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",res:" + res);
				res = new OrdResponsible();
				res.setObjectId(item.getOrderId());
				res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				logger.info("orderId:" + ordOrder.getOrderId() + "=========permUser.getUserName==============" + permUser.getUserName());
				res.setOperatorName(permUser.getUserName());
				res.setOrgId(permUser.getDepartmentId());
				res.setCreateTime(new Date());
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(), res.getObjectType());
			}
		} else if (isOutBoundSomeCateGory) {
			if (OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equalsIgnoreCase(audit.getAuditType())
					|| OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equalsIgnoreCase(audit.getAuditType())) {
				if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())) {
					logger.info("orderId:" + ordOrder.getOrderId() + ",orderItemId:" + item.getOrderItemId() + ",auditId:" + audit.getAuditId());
					OrdResponsible res;
					Date createTime = new Date();
					res = new OrdResponsible();
					res.setObjectId(audit.getObjectId());
					res.setObjectType(audit.getObjectType());
					res.setOperatorName(user.getOperatorName());
					res.setOrgId(user.getOrgId());
					res.setCreateTime(createTime);
					responsibleDao.insert(res);
					userCounterDao.increase(res.getOperatorName(), res.getObjectType());
					if (null != ordOrder.getMainOrderItem()) {
						if (ordOrder.getMainOrderItem().getOrderItemId().equals(audit.getObjectId())) {
							res = responsibleDao.getResponsibleByObject(item.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
							if (res == null && !isOutBoundO2O && !isCsVip) {
								logger.info("orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",res:" + res);
								res = new OrdResponsible();
								res.setObjectId(item.getOrderId());
								res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
								res.setOperatorName(user.getOperatorName());
								res.setOrgId(user.getOrgId());
								res.setCreateTime(createTime);
								responsibleDao.insert(res);
								userCounterDao.increase(res.getOperatorName(), res.getObjectType());
							}
						}
					}
				}
			}
		} else {
			if ((OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType()) && !isOutBoundO2O && !isCsVip)
					|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(audit.getAuditType()))
					|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equals(audit.getAuditType()))) {
				OrdResponsible res;
				Date createTime = new Date();
				res = new OrdResponsible();
				res.setObjectId(audit.getObjectId());
				res.setObjectType(audit.getObjectType());
				res.setOperatorName(user.getOperatorName());
				res.setOrgId(user.getOrgId());
				res.setCreateTime(createTime);
				logger.info("=3==orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",operatorName:" + user.getOperatorName());
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(), res.getObjectType());
				logger.info("orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",insert success");
			}
			//如果父订单也没有被分负责人，且主子单BU一致,父订单也分配
			if ((OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(audit.getAuditType()) || OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType()))
					&& isOrderAndItemBuEqual) {
				OrdResponsible res = responsibleDao.getResponsibleByObject(item.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				if (res == null && !isOutBoundO2O && !isCsVip && !isDriectSale) {
					res = new OrdResponsible();
					res.setObjectId(item.getOrderId());
					res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
					res.setOperatorName(user.getOperatorName());
					res.setOrgId(user.getOrgId());
					res.setCreateTime(new Date());
					responsibleDao.insert(res);
					userCounterDao.increase(res.getOperatorName(), res.getObjectType());
					logger.info("orderId:" + ordOrder.getOrderId() + ",auditId:" + audit.getAuditId() + ",insert ordRes success");
				}
			}
		}
	}

	private void saveResponsible(ComAudit audit, OrdOrderItem item,
								 OrdAuditUserStatus user, boolean isOrderAndItemBuEqual, boolean isDriectSale, boolean isNewAllocationRule) {
		OrdResponsible res;
		Date createTime = new Date();
		res = new OrdResponsible();
		res.setObjectId(audit.getObjectId());
		res.setObjectType(audit.getObjectType());
		res.setOperatorName(user.getOperatorName());
		res.setOrgId(user.getOrgId());
		res.setCreateTime(createTime);
		responsibleDao.insert(res);
		userCounterDao.increase(res.getOperatorName(),res.getObjectType());
		//如果为子订单分单，如果父订单也没有被分负责人，且主子单BU一致,父订单也分配
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && isOrderAndItemBuEqual && !isDriectSale && !isNewAllocationRule){
			res = responsibleDao.getResponsibleByObject(item.getOrderId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if(res==null){
				res = new OrdResponsible();
				res.setObjectId(item.getOrderId());
				res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				res.setOperatorName(user.getOperatorName());
				res.setOrgId(user.getOrgId());
				res.setCreateTime(createTime);
				responsibleDao.insert(res);
				userCounterDao.increase(res.getOperatorName(),res.getObjectType());
			}
		}
	}

	//出境若干品类（单酒店、酒店套餐、景+酒）主单负责人不可挂客服，同主子订单负责人保持一致
	private boolean isOutBoundSomeCateGory(OrdOrder order){

		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())){

			//排除分销的订单(不包括无线)
			Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
			if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
					&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
				return false;
			}

			//出境酒店套餐、景+酒
			if(new Long(17L).equals(order.getCategoryId())||
					(new Long(18L).equals(order.getCategoryId())
							&&new Long(181L).equals(order.getSubCategoryId()))){
				return true;
			}
		}

		return false;
	}


	//出境长滩若干品类  产品的目的地是长滩岛  主订单负责人为产品经理
	private boolean isOutBoundChangtanCateGory(OrdOrder order){

		//排除分销的订单(不包括无线)
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
				&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}


		logger.info("===orderId:"+order.getOrderId()+"===getBuCode===="+order.getBuCode());

		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())){
			ProdProductParam param = new ProdProductParam();
			ResultHandleT<ProdProduct> result=prodProductClientService.findProdProductById(order.getProductId(), param);
			ProdProduct prodProduct = new ProdProduct();
			if(result!=null&&result.isSuccess()){
				prodProduct = result.getReturnContent();
			}

			if(prodProduct!=null){

				logger.info("===orderId:"+order.getOrderId()+"===prodProduct===="+prodProduct.getProdDestReList());
				//出境长滩若干品类  产品的目的地是长滩
				Long[] CHANGTAN_CATEGROY ={41L,31L,45L,43L,44L,26L,28L,12L};
				if(ArrayUtils.contains(CHANGTAN_CATEGROY, order.getCategoryId())){
					if(prodProduct.getProdDestReList()!=null&&!prodProduct.getProdDestReList().isEmpty()){
						logger.info("=1==orderId:"+order.getOrderId()+"===prodProduct.getProdDestReList().size===="+prodProduct.getProdDestReList().size());
						for (ProdDestRe re : prodProduct.getProdDestReList()) {
							if (new Long(3607L).equals(re.getDestId())) {
								return true;
							}
						}
					}
				}

				logger.info("===orderId:"+order.getOrderId()+"===prodProduct.getBizCategoryId()===="+prodProduct.getBizCategoryId());
				if(new Long(16L).equals(prodProduct.getBizCategoryId()) ||
						new Long(15L).equals(prodProduct.getBizCategoryId())){
					if(prodProduct.getProdDestReList()!=null&&!prodProduct.getProdDestReList().isEmpty()){
						logger.info("=2==orderId:"+order.getOrderId()+"===prodProduct.getProdDestReList().size===="+prodProduct.getProdDestReList().size());
						logger.info("=2==orderId:"+order.getOrderId()+"===prodProduct.getProducTourtType()===="+prodProduct.getProducTourtType());
						for (ProdDestRe re : prodProduct.getProdDestReList()) {
							if ("ONEDAYTOUR".equals(prodProduct.getProducTourtType()) &&
									new Long(3607L).equals(re.getDestId())) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public int updateValid(Long audit) {
		return comAuditDao.updateComAuditValid(audit);
	}
	
	@Override
	public List<ComAudit> queryComAuditByObjectId(Long objectId) {
		return comAuditDao.queryComAuditByObjectId(objectId);
	}
	

	@Override
	public int updateComAuditStatusByAuditId(Long auditId) {
		return comAuditDao.updateComAuditStatusByAuditId(auditId);
	}

	@Override
	public List<Map<String, Object>> queryMyOrderListByCondition(
			Map<String, Object> param) {
		return comAuditDao.queryMyOrderListByCondition(param);
	}

	@Override
	public Integer countMyOrderByCondition(Map<String, Object> param) {
		return comAuditDao.countMyOrderByCondition(param);
	}

	@Override
	public Integer countMyOrderByRaid(Map<String, Object> param) {
		return comAuditDao.countMyOrderByRaid(param);
	}
	
	@Override
	public List<Map<String, Object>> queryMyOrderListByRaid(Map<String, Object> param) {
		return comAuditDao.queryMyOrderListByRaid(param);
	}

	@Override
	public List<Map<String, Object>> countGroupActivityNum(
			Map<String, Object> param) {
		return comAuditDao.countGroupActivityNum(param);
	}

	@Override
	public List<Map<String, Object>> countGroupOrderNum(
			Map<String, Object> param) {
		return comAuditDao.countGroupOrderNum(param);
	}

	@Override
	public int updateComAuditStatus(Map<String, Object> param) {
		return comAuditDao.updateComAuditStatus(param);
	}

	@Override
	public int updateNextAssignTime(Map<String, Object> param) {
		return comAuditDao.updateNextAssignTime(param);
	}
	@Override
	public int queryOrderListCount(Map<String, Object> param) {
		return comAuditDao.queryOrderListCount(param);
	}
	@Override
	public List<ComAudit> queryOrderAuditList(Map<String, Object> param) {
		return comAuditDao.queryOrderAuditList(param);
	}
	@Override
	public List<ComAuditActiviNum> countActivityUnprocessedNum(Map<String, Object> param) {
		return comAuditDao.countActivityUnprocessedNum(param);
	}
	@Override
	public int countAuditByNewConsole(Map<String, Object> param) {
		Integer count = comAuditDao.queryAuditListByConditionByNewConsoleCount(param);
		if(count==null){
			return 0;
		}
		return count;
	}
	@Override
	public List<ComAudit> queryAuditByNewConsole(Map<String, Object> param) {
		return comAuditDao.queryAuditListByConditionByNewConsole(param);
	}
	@Override
	public int updateComAuditByAuditlist(List<Long> auditIdlist) {
		return comAuditDao.updateComAuditByAuditlist(auditIdlist);
	}
	
	@Override
	public ComAudit updateAuditAssignForNewRule(ComAudit audit, OrdOrder order,
			String operatorName) {
		audit.setUpdateTime(new Date());
		int ret = updateByPrimaryKeyNew(audit);
		if (ret < 1) {
			return null;
		}
		if(Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
			//保存分单日志
			insertChildConfirmAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}else{
			//保存分单日志
			insertOrderAssignLog(order.getOrderId(), audit.getObjectId(), audit.getObjectType(), audit.getAuditType(), audit.getOperatorName());
		}
		saveOrdResForNewRule(audit, order);
		if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("auditId", audit.getAuditId());
			List<ComMessage> list = comMessageDao.findComMessageList(params);
			if(list.isEmpty()){//如果是预定通知，通知不存在就直接关掉
				comAuditDao.markValid(audit.getAuditId());
				if (logger.isWarnEnabled()) {
					logger.warn("updateAuditAssign(ComAudit, OrdOrderItem, boolean, boolean, OrdAuditUserStatus) - 预订通知不存在 auditId:"+audit.getAuditId()); //$NON-NLS-1$
				}
			}else{
				ComMessage msg = list.get(0);
				if(OrderEnum.MESSAGE_STATUS.PROCESSED.name().equalsIgnoreCase(msg.getMessageStatus())){
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateByPrimaryKey(audit);
				}else{
					msg.setReceiver(audit.getOperatorName());
					comMessageDao.updateByPrimaryKey(msg);
				}
			}
		}
		return audit;
	}
	
	private void saveOrdResForNewRule(ComAudit audit, OrdOrder order) {
		logger.info("saveOrdResForNewRule auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " ordResName:"+audit.getOperatorName());
		if(StringUtils.isNotEmpty(audit.getOperatorName())) {
			OrdResponsible ordRes = responsibleDao.getResponsibleByObject(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
				PermUser permUser =permUserServiceAdapter.getPermUserByUserName(audit.getOperatorName());
				if (ordRes == null && permUser != null) {
					ordRes = new OrdResponsible();
					ordRes.setObjectId(order.getOrderId());
					ordRes.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
					ordRes.setOperatorName(permUser.getUserName());
					ordRes.setOrgId(permUser.getDepartmentId());
					ordRes.setCreateTime(new Date());
					responsibleDao.insert(ordRes);
					userCounterDao.increase(ordRes.getOperatorName(), ordRes.getObjectType());
					logger.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " updateAuditAssignForNewRule insert ordRes success");
				}
			}
			//子订单主负责人默认赋值主订单负责人
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
				OrdResponsible res = responsibleDao.getResponsibleByObject(audit.getObjectId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
				if(res==null && ordRes!=null){
					PermUser permUser =permUserServiceAdapter.getPermUserByUserName(ordRes.getOperatorName());
					if (permUser == null) {
						return;
					}
					res = new OrdResponsible();
					res.setObjectId(audit.getObjectId());
					res.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
					res.setOperatorName(permUser.getUserName());
					res.setOrgId(permUser.getDepartmentId());
					res.setCreateTime(new Date());
					responsibleDao.insert(res);
					userCounterDao.increase(res.getOperatorName(),res.getObjectType());
					logger.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " orderItemId:" + audit.getObjectId() + " updateAuditAssignForNewRule insert ordRes success");
				}
			}
		}
	}
	
	private boolean isNewAllocationRule(OrdOrder order, OrdOrderItem item, ComAudit audit) {
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
				&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date myDate = dateFormat.parse("2018-03-27 10:00:00");
			if(order.getCreateTime().before(myDate)){
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
				&&BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
				&&(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().equals(order.getSubCategoryId()))){
			return true;
		}
		return false;
	}
	/**
	 * 生成更新活动SEQ日志
	 * @param orderId
	 * @param audit 活动
	 * @param  seq
	 */
	private void createUpdateAuditSeqLog(Long orderId, ComAudit audit, Long seq) {
		try {
			ComLog.COM_LOG_OBJECT_TYPE logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
				logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
			}
			lvmmLogClientService.sendLog(logObjectType,
					orderId,
					audit.getObjectId(),
					"SYSTEM",
					"更新编号为["+audit.getObjectId()+"]的订单活动["+ ComLogUtil.getLogTxt("排序值", audit.getSeq(), seq)+"]",
					"更新订单活动排序值",
					"更新订单活动排序值["+ Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(audit.getAuditType())+"]",
					"");
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}
}
