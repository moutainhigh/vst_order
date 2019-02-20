package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.ComMessageDao;
import com.lvmama.vst.order.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComMessageServiceImpl implements IComMessageService {

	private static final Log LOG = LogFactory
			.getLog(ComMessageServiceImpl.class);
	@Autowired
	private ComMessageDao comMessageDao;

	@Autowired
	private IOrderDistributionBusiness orderDistributionBusiness;

	@Autowired
	private IOrderAuditService orderAuditService;

	// 公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	

	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	
	@Autowired
	private ProdPackageGroupClientService prodPackageGroupClientRemote;
	
	@Override
	public int findComMessageCount(Map<String, Object> params) {
		// TODO Auto-generated method stub

		return comMessageDao.getTotalCount(params);
	}

	@Override
	public List<ComMessage> findComMessageList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		List<ComMessage> ComMessageList = null;
		ComMessageList = comMessageDao.findComMessageList(params);

		return ComMessageList;
	}

	@Override
	public int addComMessage(ComMessage ComMessage) {
		// TODO Auto-generated method stub
		return comMessageDao.insert(ComMessage);
	}

	@Override
	public ComMessage findComMessageById(Long id) {
		// TODO Auto-generated method stub
		return comMessageDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateComMessage(ComMessage ComMessage) {
		// TODO Auto-generated method stub
		return comMessageDao.updateByPrimaryKeySelective(ComMessage);
	}

	public int updateReservationListProcessed(String orderId,
			String[] messageIds, String[] auditIdArray, String assignor,
			String memo) {

		Long orderIdLong = new Long(orderId);
		int n = 0;
		for (int i = 0; i < messageIds.length; i++) {
			String messageId = messageIds[i];
			ComMessage record = new ComMessage();
			record.setMessageId(new Long(messageId));
			record.setMessageStatus(OrderEnum.MESSAGE_STATUS.PROCESSED
					.getCode());
			int m = comMessageDao.updateByPrimaryKeySelective(record);

			ComAudit comAudit = orderAuditService.queryAuditById(new Long(
					auditIdArray[i]));
			// comAudit.setAuditId(new Long(auditIdArray[i]));
			comAudit.setOperatorName(assignor);
			comAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.getCode());
			comAudit.setUpdateTime(Calendar.getInstance().getTime());

			orderAuditService.updateByPrimaryKey(comAudit);

			if ("ORDER_ITEM".equals(comAudit.getObjectType().trim())) {
				
				this.insertChildOrderLog(orderIdLong, comAudit.getObjectId(), OrderEnum.MESSAGE_STATUS.PROCESSED.name(), assignor, memo);
				
			}else{

				this.insertOrderLog(orderIdLong,
						OrderEnum.MESSAGE_STATUS.PROCESSED.name(), assignor, memo);
			}
			

			if (m == 1) {
				n++;
			}

		}

		return n;

	}

	/**
	 * 新增预定通知（订单详情页面）
	 * 
	 * @param comMessage
	 */
	public int saveReservation(ComMessage comMessage, String auditType,
			Long orderId, String assignor, String memo)
			throws BusinessException {
		
		if(null!=assignor&&assignor.equalsIgnoreCase("system")){
			OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
			boolean isbusAndHotel=isBusAndhotel(ordOrder);
			LOG.info("===orderId:"+ordOrder.getOrderId()+".isbusAndHotel:"+isbusAndHotel+"===");
			if(isbusAndHotel){
				return 0;
			}
		}
		
		int n = 0;
		String isGroup = "";
		String groupOrOperator = "";
		if (!StringUtils.isEmpty(auditType)) {// 选择组
			isGroup = OrderEnum.IS_GROUP.YES.name();
			groupOrOperator = auditType;
		} else {// 选择指定人
			isGroup = OrderEnum.IS_GROUP.NO.name();
			// comMessage.setReceiver(receiver);
			groupOrOperator = comMessage.getReceiver();
		}

//		ComAudit audit = orderAuditService.saveCreateOrderAudit(orderId,
//				OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		ComAudit audit = new ComAudit();
		audit.setObjectId(orderId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		if (OrderEnum.AUDIT_TYPE.FULL_HOUSE_AUDIT.name().equals(groupOrOperator)) {
			audit.setAuditSubtype(groupOrOperator);
		}else if(OrderEnum.AUDIT_TYPE.ClOSE_HOUSE.name().equals(groupOrOperator)){
			audit.setAuditSubtype(groupOrOperator);
		} else if (Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name().equals(groupOrOperator)) {
            audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
            audit.setAuditType(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
            audit.setAuditSubtype(groupOrOperator);
        }
		

		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						groupOrOperator);
		if (comAuditObj == null) {
			//throw new BusinessException("新增预定通知的时候分单无人");
			comMessage.setReceiver("NO_PERSON");
		} else {
			comMessage.setAuditId(comAuditObj.getAuditId());
			comMessage.setReceiver(comAuditObj.getOperatorName());
		}

		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		n = comMessageDao.insert(comMessage);

		this.insertOrderLog(orderId,
				OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);
		
		this.addOrderMessageToLog(orderId, comMessage, memo, null, groupOrOperator, isGroup);

		return n;
	}

	/**
	 * 单酒店-新增预定通知（订单详情页面）
	 *
	 * @param comMessage
	 */
	public int saveChildReservation(ComMessage comMessage, String auditType,
			Long orderId, Long orderItemId, String assignor, String memo)
			throws BusinessException {

		if(null!=assignor&&assignor.equalsIgnoreCase("system")){
			OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
			boolean isbusAndHotel=isBusAndhotel(ordOrder);
			LOG.info("===orderId:"+ordOrder.getOrderId()+".isbusAndHotel:"+isbusAndHotel+"===");
			if(isbusAndHotel){
				return 0;
			}
		}

		int n = 0;
		String isGroup = "";
		String groupOrOperator = "";
		String type = null;
		if (!StringUtils.isEmpty(auditType)) {// 选择组
			isGroup = OrderEnum.IS_GROUP.YES.name();
			groupOrOperator = auditType;
		} else {// 选择指定人
			isGroup = OrderEnum.IS_GROUP.NO.name();
			// comMessage.setReceiver(receiver);
			groupOrOperator = comMessage.getReceiver();
		}

//		ComAudit audit = orderAuditService.saveCreateOrderAudit(orderId,
//				OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		ComAudit audit = new ComAudit();
		audit.setObjectId(orderId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		if (OrderEnum.AUDIT_TYPE.FULL_HOUSE_AUDIT.name().equals(groupOrOperator)) {
			audit.setAuditSubtype(groupOrOperator);
		}else if(OrderEnum.AUDIT_TYPE.ClOSE_HOUSE.name().equals(groupOrOperator)){
			audit.setAuditSubtype(groupOrOperator);
		} else if (Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name().equals(groupOrOperator)) {
            audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			audit.setObjectId(orderItemId);
            audit.setAuditType(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
			type = audit.getAuditType();
            audit.setAuditSubtype(groupOrOperator);
        }



        LOG.info("audit="+audit+",orderItemId="+orderItemId+",assignor="+assignor+",memo="+ memo);
		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						groupOrOperator);
		if (comAuditObj == null) {
			//throw new BusinessException("新增预定通知的时候分单无人");
			comMessage.setReceiver("NO_PERSON");
		} else {
			comMessage.setAuditId(comAuditObj.getAuditId());
			comMessage.setReceiver(comAuditObj.getOperatorName());
			//其它预订通知，单独记录主单日志
			if (Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name().equals(groupOrOperator)) {
				insertHotelOrderLog(orderId, comAuditObj, assignor);
			}
		}

		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		n = comMessageDao.insert(comMessage);

		this.insertOrderLog(orderId, OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);

		this.addOrderMessageToLog(orderId, comMessage, memo, type, groupOrOperator, isGroup);

		return n;
	}

	
	public boolean isBusAndhotel(OrdOrder order){
		if(isInclueLocalBuOrDestinationBu(order)){
			LOG.info("===orderId:"+order.getOrderId()+".productId:"+order.getProductId()+"===");
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
				LOG.error("====orderId:"+order.getOrderId()+"====product is null===");
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
	
	/**
	 * 新增预定通知（子订单订单详情页面）
	 * @param comMessage
	 */
	public int saveReservationChildOrder(ComMessage comMessage,String auditType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException
	{
		return saveReservationChildOrder(comMessage, auditType, null, orderId, orderItmeId, assignor, memo);
	}
	
	/**
	 * 订单取消成功后产生预定通知
	 * @param orderId
	 * @param loginUserId
	 */
	public int saveReservationAfterCan(Long orderId, String loginUserId) {
		//发送给主订单
		String objectType="ORDER";
		Long objectId=orderId;
		PermUser permUserPrincipal=orderResponsibleService.getOrderPrincipal(objectType, objectId);
		String orderPrincipal=permUserPrincipal.getUserName();
		
		String receiver=loginUserId;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver=orderPrincipal;
		}
		String messageContent="订单取消成功，请及时处理订单退款";
		
		
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		
		return this.saveReservation(comMessage, null,OrderEnum.AUDIT_SUB_TYPE.CANCEL_ORDER.getCode(),
						orderId, loginUserId,"订单取消成功后产生预定通知，内容："+messageContent);
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertOrderLog(final Long orderId, String auditType,
			String assignor, String memo) {
		lvmmLogClientService.sendLog(
				ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId,
				orderId,
				assignor,
				"将订单编号为["
						+ orderId
						+ "]的预定通知状态变更["
						+ OrderEnum.MESSAGE_STATUS.UNPROCESSED
								.getCnName(auditType) + "]",
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName()
						+ "["
						+ OrderEnum.MESSAGE_STATUS.UNPROCESSED
								.getCnName(auditType) + "]", memo);
	}
	
	/**
	 * 添加生成订单预定通知的日志
	 * @param orderId
	 * @param comMessage
	 * @param memo
	 * @param messageType
	 * @param groupOrOperator
	 * @param isGroup
	 */
	private void addOrderMessageToLog(Long orderId, ComMessage comMessage, String memo, String messageType, String groupOrOperator, String isGroup) {
		lvmmLogClientService
				.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId,
						orderId,
						comMessage.getSender(),
						ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED
								.getCnName(),
						ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED.name(),
						ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED
								.getCnName()
								+ "[通知类型：" + ConfirmEnumUtils.getCnName(messageType)
								+ "， 接收组: " + ((OrderEnum.IS_GROUP.YES.name().equals(isGroup)) ? ConfirmEnumUtils.getCnName(groupOrOperator) : "")
								+ "， 接收人：" + (OrderEnum.IS_GROUP.NO.name().equals(isGroup) ? comMessage.getReceiver() : "") + "]",
						StringUtils.isBlank(memo) ? comMessage
								.getMessageContent()
								: (memo.indexOf(comMessage.getMessageContent()) > -1
										|| StringUtils.isBlank(comMessage
												.getMessageContent()) ? memo
										: comMessage.getMessageContent()
												+ " | " + memo));
	}

	/**
	 * 添加生成子订单预定通知的日志
	 * @param orderId
	 * @param orderItemId
	 * @param comMessage
	 * @param memo
	 * @param messageType
	 * @param groupOrOperator
	 * @param isGroup
	 */
	private void addChildOrderMessageToLog(Long orderId, Long orderItemId, ComMessage comMessage, String memo, String messageType, String groupOrOperator, String isGroup) {
		lvmmLogClientService
		.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId,
				orderItemId,
				comMessage.getSender(),
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED
						.getCnName(),
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_ADDED
						.getCnName()
						+ "[通知类型：" + ConfirmEnumUtils.getCnName(messageType)
						+ "， 接收组: " + ((OrderEnum.IS_GROUP.YES.name().equals(isGroup)) ? ConfirmEnumUtils.getCnName(groupOrOperator) : "")
						+ "， 接收人：" + (OrderEnum.IS_GROUP.NO.name().equals(isGroup) ? comMessage.getReceiver() : "") + "]",
				StringUtils.isBlank(memo) ? comMessage
						.getMessageContent()
						: (memo.indexOf(comMessage.getMessageContent()) > -1
								|| StringUtils.isBlank(comMessage
										.getMessageContent()) ? memo
								: comMessage.getMessageContent()
										+ " | " + memo));
	}
	

	/**
	 * 新增预定通知（订单详情页面）
	 * 
	 * @param comMessage
	 */
	public int saveReservation(ComMessage comMessage, String auditType,String auditSubType,
			Long orderId, String assignor, String memo)
			throws BusinessException {

		if(null!=assignor&&assignor.equalsIgnoreCase("system")){
			OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
			boolean isbusAndHotel=isBusAndhotel(ordOrder);
			LOG.info("===orderId:"+ordOrder.getOrderId()+".isbusAndHotel:"+isbusAndHotel+"===");
			if(isbusAndHotel){
				return 0;
			}
		}

		if (StringUtils.isEmpty(comMessage.getReceiver())) {
			String objectType="ORDER";
			PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, orderId);
			comMessage.setReceiver(permUserPrincipal.getUserName());
		}
		
		
		int n = 0;
		String isGroup = "";
		String groupOrOperator = "";
		if (!StringUtils.isEmpty(auditType)) {// 选择组
			isGroup = OrderEnum.IS_GROUP.YES.name();
			groupOrOperator = auditType;
		} else {// 选择指定人
			isGroup = OrderEnum.IS_GROUP.NO.name();
			// comMessage.setReceiver(receiver);
			groupOrOperator = comMessage.getReceiver();
		}

//		ComAudit audit = orderAuditService.saveOrderAudit(orderId,
//				OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name(),auditSubType);
		
		ComAudit audit = new ComAudit();
		audit.setObjectId(orderId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(auditSubType);

		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						groupOrOperator);
		setReceiver(comMessage, isGroup, groupOrOperator, comAuditObj);

		comMessage.setAuditId(comAuditObj.getAuditId());
		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		n = comMessageDao.insert(comMessage);

		this.insertOrderLog(orderId,
				OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);

		this.addOrderMessageToLog(orderId, comMessage, memo, auditSubType, groupOrOperator, isGroup);
		return n;
	}

	
	/**
	 * 新增预定通知（子订单订单详情页面）
	 * @param comMessage
	 */
	public int saveReservationChildOrder(ComMessage comMessage,String auditType,String auditSubType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException
	{
		LOG.info("comMessage="+comMessage+",auditType="+auditType+",auditSubType="+auditSubType+",orderId="+orderId+",orderItmeId="+orderItmeId+",assignor="+assignor+",memo="+memo);
		if (StringUtils.isEmpty(comMessage.getReceiver())) {
			String objectType="ORDER_ITEM";
			PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, orderItmeId);
			comMessage.setReceiver(permUserPrincipal.getUserName());
		}
		
		
		int n = 0;
		String isGroup = "";
		String groupOrOperator = "";
		if (!StringUtils.isEmpty(auditType)) {// 选择组
			isGroup = OrderEnum.IS_GROUP.YES.name();
			groupOrOperator = auditType;
		} else {// 选择指定人
			isGroup = OrderEnum.IS_GROUP.NO.name();
			// comMessage.setReceiver(receiver);
			groupOrOperator = comMessage.getReceiver();
		}

//		ComAudit audit = orderAuditService.saveChildOrderAudit(orderId,orderItmeId,
//				OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name(),auditSubType);
		
		ComAudit audit = new ComAudit();
		audit.setObjectId(orderItmeId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
        audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(auditSubType);
		if (Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL.name().equals(auditSubType)) {
			auditType = Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name();
			if (isGroup.equals(OrderEnum.IS_GROUP.YES.name())) {// 选择组
				groupOrOperator = auditSubType;
			}
			audit.setAuditType(auditType);
		}
		OrdOrder order = new OrdOrder();
		order.setOrderId(orderId);
		audit.setOrder(order);

		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						groupOrOperator);
		
		
		setReceiver(comMessage, isGroup, groupOrOperator, comAuditObj);

		comMessage.setAuditId(comAuditObj.getAuditId());
		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		n = comMessageDao.insert(comMessage);

		
		this.insertChildOrderLog(orderId,orderItmeId,
				OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);
		
		this.addChildOrderMessageToLog(orderId, orderItmeId, comMessage, memo, auditType, groupOrOperator, isGroup);

		return n;
	}
	
	/**
	 * 新增预定通知（子订单订单详情页面）
	 * @param comMessage
	 */
	public ComAudit newReservationChildOrder(ComMessage comMessage,String auditType,String auditSubType,Long orderId,Long orderItmeId,String assignor,String memo)throws BusinessException
	{
		if (StringUtils.isEmpty(comMessage.getReceiver())) {
			String objectType="ORDER_ITEM";
			PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, orderItmeId);
			comMessage.setReceiver(permUserPrincipal.getUserName());
		}
		String isGroup = "";
		String groupOrOperator = "";
		if (!StringUtils.isEmpty(auditType)) {// 选择组
			isGroup = OrderEnum.IS_GROUP.YES.name();
			groupOrOperator = auditType;
		} else {// 选择指定人
			isGroup = OrderEnum.IS_GROUP.NO.name();
			groupOrOperator = comMessage.getReceiver();
		}

		ComAudit audit = new ComAudit();
		audit.setObjectId(orderItmeId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(auditSubType);
		OrdOrder order = new OrdOrder(); 
		order.setOrderId(orderId);
		audit.setOrder(order);

		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						groupOrOperator);
		
		
		setReceiver(comMessage, isGroup, groupOrOperator, comAuditObj);

		comMessage.setAuditId(comAuditObj.getAuditId());
		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		comMessageDao.insert(comMessage);

		this.insertChildOrderLog(orderId,orderItmeId,
				OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);
		
		this.addChildOrderMessageToLog(orderId, orderItmeId, comMessage, memo, auditSubType, groupOrOperator, isGroup);

		return comAuditObj;
	}

	public void setReceiver(ComMessage comMessage, String isGroup,
			String groupOrOperator, ComAudit comAuditObj) {
		if (comAuditObj == null) {
			comMessage.setReceiver(Constants.NO_PERSON);
		} else {
			comMessage.setReceiver(comAuditObj.getOperatorName());
		}
	}
	
	
	/**
	 * 新增主订单预定通知 分单对象，主订单负责人
	 * @param comMessage
	 * @throws Exception 
	 */
	public int saveReservationOrder(Long orderId,String auditSubType,String assignor,String memo,boolean bigTrafficValidate) throws Exception
	{
		int n=0;
//		boolean traffic=false;
		if (bigTrafficValidate) {//主订单类型，为线路大类，且其产品属性“是否含大交通=是”
			
			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
			if(!Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				//主订单类型，为线路大类?
				boolean isLineOrder=false;
				if(BIZ_CATEGORY_TYPE.category_route.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())){
					isLineOrder=true;
					LOG.info("orderId"+order.getOrderId()+"isLineOrder is true");
				}
				//邮轮组合产品订单,增加预订通知, 避免因为非线路品类退出
				if (isLineOrder) {
					ProdProduct prodProduct=prodProductClientService.findProdProductById(order.getProductId(), Boolean.TRUE, Boolean.TRUE);
					Map<String, Object> propValue=prodProduct.getPropValue();				
					String trafficFlag=(String)propValue.get("traffic_flag");
					if (trafficFlag==null || "N".equals(trafficFlag)) {//是否含大交通
						return n;
					}
				}else if(!BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())){
					return n;
				}
			}	
		}
		
		
//		ComAudit audit = orderAuditService.saveOrderAudit(orderId,
//				OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name(),auditSubType);
		
		ComAudit audit = new ComAudit();
		audit.setObjectId(orderId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(auditSubType);

		String objectType="ORDER";
		Long objectId=orderId;
		PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
		if(permUserPrincipal==null){//没有主订单负责人
			String message="产生预定通知时候未发现主订单负责人，无法创建预定通知";
			this.lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, 
					orderId, 
					assignor, 
					message, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ message +"]",
					memo);
			return -1;
		}
		
		String isGroup = OrderEnum.IS_GROUP.NO.name();
		ComAudit comAuditObj = orderDistributionBusiness
				.makeOrderAuditForBookingAudit(audit, assignor, isGroup,
						permUserPrincipal.getUserName());
//		if (comAuditObj == null) {//找不到可以分担的人
//			throw new BusinessException(Constants.NO_PERSON);
//		}
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(memo);
//		comMessage.setReceiver(permUserPrincipal.getUserName());
		if (comAuditObj == null) {
			//throw new BusinessException("新增预定通知的时候分单无人");
			comMessage.setReceiver("NO_PERSON");
			comMessage.setAuditId(null);
		} else {
			comMessage.setReceiver(comAuditObj.getOperatorName());
			comMessage.setAuditId(comAuditObj.getAuditId());
		}
		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCode());
		n = comMessageDao.insert(comMessage);
				
//		this.insertOrderLog(orderId, OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode(), assignor, memo);
		
		this.addOrderMessageToLog(orderId, comMessage, memo, auditSubType, comMessage.getReceiver(), isGroup);
		
		return n;
	}

	/**
	 * 新增主订单预定通知 分单对象，主订单负责人
	 * @throws Exception
	 */
	public int saveReservationOrderNew(Long orderId,String auditSubType,String assignor,String memo,boolean bigTrafficValidate) throws Exception
	{
		LOG.info("saveReservationOrderNew.orderId="+orderId+",auditSubType="+auditSubType);
		int n=0;
		if (bigTrafficValidate) {//主订单类型，为线路大类，且其产品属性“是否含大交通=是”

			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
			if(!Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				//主订单类型，为线路大类?
				boolean isLineOrder=false;
				if(BIZ_CATEGORY_TYPE.category_route.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())){
					isLineOrder=true;
					LOG.info("orderId"+order.getOrderId()+"isLineOrder is true");
				}
				//邮轮组合产品订单,增加预订通知, 避免因为非线路品类退出
				if (isLineOrder) {
					ProdProduct prodProduct=prodProductClientService.findProdProductById(order.getProductId(), Boolean.TRUE, Boolean.TRUE);
					Map<String, Object> propValue=prodProduct.getPropValue();
					String trafficFlag=(String)propValue.get("traffic_flag");
					if (trafficFlag==null || "N".equals(trafficFlag)) {//是否含大交通
						return n;
					}
				}else if(!BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())){
					return n;
				}
			}
		}


		ComAudit audit = new ComAudit();
		audit.setObjectId(orderId);
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(auditSubType);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(Calendar.getInstance().getTime());
		orderAuditService.saveAudit(audit);
		LOG.info("saveReservationOrderNew.orderId="+orderId+",auditId="+audit.getAuditId());
		String objectType="ORDER";
		Long objectId=orderId;
		PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
		if(permUserPrincipal==null){//没有主订单负责人
			String message="产生预定通知时候未发现主订单负责人，无法创建预定通知";
			this.lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId,
					orderId,
					assignor,
					message,
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ message +"]",
					memo);
			return -1;
		}

		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(memo);
		comMessage.setAuditId(audit.getAuditId());
		comMessage.setSender(assignor);
		comMessage.setCreateTime(Calendar.getInstance().getTime());
		comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
		n = comMessageDao.insert(comMessage);

		String isGroup = OrderEnum.IS_GROUP.NO.name();
		ComAudit comAuditObj = orderDistributionBusiness.makeOrderAudit(audit);
		//.makeOrderAuditForBookingAudit(audit, assignor, isGroup, permUserPrincipal.getUserName());
		try {
			if (!StringUtil.isEmptyString(comAuditObj.getOperatorName())) {
				ComMessage m = comMessageDao.selectByPrimaryKey(comMessage.getMessageId());
				LOG.info("saveReservationOrderNew.orderId="+orderId+",auditId="+audit.getAuditId()+",messageId="+m.getMessageId());
				if (m != null && m.getReceiver() == null) {
					m.setReceiver(comAuditObj.getOperatorName());
					comMessageDao.updateByPrimaryKeySelective(m);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.addOrderMessageToLog(orderId, comMessage, memo, auditSubType, comMessage.getReceiver(), isGroup);

		return n;
	}
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertChildOrderLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				assignor, 
				"将子订单为["+orderItemId+"]的预定通知状态变更["+OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCnName(auditType)+"]", 
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName()+"["+OrderEnum.MESSAGE_STATUS.UNPROCESSED
				.getCnName(auditType)+"]",
				memo);
	}

	/**
	 * 酒店审核new 创建预订通知
	 * @param orderId
	 * @param audit
	 */
	private void insertHotelOrderLog(Long orderId, ComAudit audit, String assignor) {
		try {
			String auditTypeName = ConfirmEnumUtils.getCnName(audit.getAuditType());

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, // parentId 与 objectId 相同
					audit.getObjectId(),
					"SYSTEM",
					"将编号为["+orderId+"]的订单分给员工["+audit.getOperatorName()+"]进行["+auditTypeName+"]任务",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.getCnName()+"["+auditTypeName+"]",
					null);
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
	}
	/**
	 * 批量预定通知的处理
	 * @param comAuditList 审核信息列表
	 * @param assignor 操作人
	 * @param memo 备注
	 * @return
	 */
	public int updateBatchReservationListProcessed(List<ComAudit> comAuditList,String assignor, String memo){
		int batchSucNum = 0;
		int sucNum = 0 ;
		if(comAuditList != null && comAuditList.size() > 0){
			for(ComAudit comAudit : comAuditList){
				sucNum = this.updateReservationListProcessed(comAudit, assignor, memo);
				if (sucNum == 1) {
					batchSucNum++;
				}
			}
		}
		return batchSucNum;
	}
	/**
	 * 预定通知的处理
	 * 一.如果处理的为主订单则
	 * 		1.查找相关通知信息
	 * 		2.更新后台任务消息信息和审核信息的壮态
	 * 		3.写操作业务日志
	 * 二.如果处理的为子订单则
	 * 		1.在子订单表中根据子订单id查找相关记录
	 * 		2.查找相关通知信息
	 * 		3.更新后台任务消息信息和审核信息的壮态
	 * 		4.写操作业务日志
	 * 
	 * @param comAudit 审核信息
	 * @param assignor 操作人
	 * @param memo 备注
	 * @return
	 */
	public int updateReservationListProcessed(ComAudit comAudit,String assignor, String memo){
		int sucNum = 0;
		try {
			if(comAudit != null){
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("auditId", comAudit.getAuditId());
				parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
				List<ComMessage> comMessageList = comMessageDao.findComMessageList(parameters);
				if(comMessageList != null && comMessageList.size() >0){
					ComMessage comMessage = comMessageList.get(0);
					comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.PROCESSED.getCode());
					sucNum = comMessageDao.updateByPrimaryKeySelective(comMessage);
				}
				comAudit.setOperatorName(assignor);
				comAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.getCode());
				comAudit.setUpdateTime(Calendar.getInstance().getTime());
				orderAuditService.updateByPrimaryKey(comAudit);
				if("ORDER_ITEM".equals(comAudit.getObjectType())){
					OrdOrderItem orderItem = iOrdOrderItemService.selectOrderItemByOrderItemId(comAudit.getObjectId());
					if(orderItem != null){
						this.insertChildOrderLog(orderItem.getOrderId(), comAudit.getObjectId(), OrderEnum.MESSAGE_STATUS.PROCESSED.name(), assignor, memo);
					}
				}
				else if("ORDER".equals(comAudit.getObjectType())){
					this.insertOrderLog(comAudit.getObjectId(),OrderEnum.MESSAGE_STATUS.PROCESSED.name(), assignor, memo);
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return sucNum;
	}
	/**
	  关房通知
*/
	@Override
	public int savaReservationAfterCalOfCloseHourse(Long orderId,
			String loginUserId) {
		//发送给主订单
				String objectType="ORDER";
				Long objectId=orderId;
//				PermUser permUserPrincipal=orderResponsibleService.getOrderPrincipal(objectType, objectId);
//				String orderPrincipal=permUserPrincipal.getUserName();
//				
//				String receiver=loginUserId;
//				if (!StringUtils.isEmpty(orderPrincipal)) {
//					receiver=orderPrincipal;
//				}
				String messageContent="酒店已关房，请进行催单操作";
				ComMessage comMessage=new ComMessage();
				comMessage.setMessageContent(messageContent);
		//		comMessage.setReceiver(receiver);
				
				return this.saveReservation(comMessage,OrderEnum.AUDIT_SUB_TYPE.ClOSE_HOUSE.name(),
								orderId, loginUserId,"关房通知，内容："+messageContent);
	}
}
