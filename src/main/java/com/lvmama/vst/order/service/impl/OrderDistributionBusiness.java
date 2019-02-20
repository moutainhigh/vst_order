package com.lvmama.vst.order.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.lvmama.comm.pet.po.perm.PermRole;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.order.base.jedis.JedisClusterAdapter;
import com.lvmama.order.utils.DateUtil;
import com.lvmama.vst.allocation.jms.AllocationLogMessageProducer;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.back.client.ord.service.AllocationClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdAuditAllocation;
import com.lvmama.vst.back.order.po.OrdAuditConfig;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_STATUS;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DistributionUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderOperatorVo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdAuditAllocationDao;
import com.lvmama.vst.order.dao.OrdAuditUserStatusDAO;
import com.lvmama.vst.order.dao.OrdResponsibleDao;
import com.lvmama.vst.order.dao.OrdUserCounterDao;
import com.lvmama.vst.order.service.IBusinessRuleService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAuditConfigService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.utils.OrderPropertyRule;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.pet.adapter.ConnRecordServiceAdapter;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserRoleProxyAdapter;

/**
 * 订单分单业务组件
 * 
 * @author wenzhengtao
 *
 */
@Service
public final class OrderDistributionBusiness implements IOrderDistributionBusiness,InitializingBean{
	//日志记录器,整个类共享
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderDistributionBusiness.class);
	
	//订单权限配置业务
	@Autowired
	private IOrdAuditConfigService ordAuditConfigService;
	
	//订单活动审核业务
	@Autowired
	private IOrderAuditService orderAuditService;
	
	//订单简单操作业务
	@Autowired
	private IOrderUpdateService ordOrderUpdateService;
	
	//订单综合操作业务
	@Autowired
	private IComplexQueryService complexQueryService;
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	//用户状态业务
	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	
	@Autowired
	private  IBusinessRuleService businessRuleService;
	
	@Autowired
	private OrdResponsibleDao responsibleDao;
	
	@Autowired
	private ComLogClientService comLogClientService;
	
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;

	@Autowired
	private CsVipDubboService csVipDubboService;

	@Autowired
	private ConnRecordServiceAdapter connRecordService;
	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	@Autowired
	private AllocationClientService allocationClientService;
	@Autowired
	private PermUserRoleProxyAdapter permUserRoleProxyAdapter;
	@Resource(name="allocationLogMessageProducer")
	private AllocationLogMessageProducer allocationLogMessageProducer;
	
	@Resource(name="allocationMessageProducer")
	private TopicMessageProducer allocationMessageProducer;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private JedisClusterAdapter jedisCluster;
	
	/**
	 * 信息审核自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForInfoAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		//根据订单ID和活动类型查找待分配的订单池对象
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，找后台下单人
		operator = this.findOperatorForInfoAudit(order);
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "第一选择-找后台下单人[有被分单权限、在线或忙碌]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "第一选择-找后台下单人[有被分单权限、在线或忙碌]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "第一选择-找后台下单人[有被分单权限、在线或忙碌]", "处理人不存在");
		}
		
		//第二选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "第二选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "第二选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
		
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}

	/**
	 * 资源审核自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForResourceAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，找信息待审核人
		operator = this.findOperatorByAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(), OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第一选择-找信息待审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第一选择-找信息待审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第一选择-找信息待审核人[有被分单权限、在线或忙碌]", "处理人不存在");
		}
		
		//第二选择，找信息审核人
		operator = this.findOperatorByAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(), OrderEnum.AUDIT_STATUS.PROCESSED.name());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人不存在");
		}
		
		//第三选择，找同商品ID人
		operator = this.findOperatorByGoodsId(order.getOrderId());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人不存在");
		}
		
		//第四选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第四选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "第四选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
	
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}

	/**
	 * 凭证确认自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForCertificateAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，找资源审核人,排除保留房的系统审核人SYSTEM
		operator = this.findOperatorByAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), OrderEnum.AUDIT_STATUS.PROCESSED.name(),"SYSTEM");
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第一选择-找资源审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"符合条件");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第一选择-找资源审核人[有被分单权限、在线或忙碌]", "处理人不存在");
			}
		}
		
		//第二选择，找信息审核人
		operator = this.findOperatorByAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(), OrderEnum.AUDIT_STATUS.PROCESSED.name());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第二选择-找信息审核人[有被分单权限、在线或忙碌]", "处理人不存在");
		}
		
		//第三选择，找同商品ID的人
		operator = this.findOperatorByGoodsId(order.getOrderId());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第三选择-找同商品ID的人[有被分单权限、在线]", "处理人不存在");
		}
		
		//第四选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第四选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "第四选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
		
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}

	/**
	 * 催支付自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForPaymentAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCnName(), "第一选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCnName(), "第一选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
		
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}
	
	/**
	 * 售后自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForSaleAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.SALE_AUDIT.getCnName(), "第一选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.SALE_AUDIT.getCnName(), "第一选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
		
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.SALE_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}

	/**
	 * 取消确认自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	public ComAudit makeOrderAuditForCancelAudit(final ComAudit audit) {
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null==order||null == order.getMainOrderItem()||null==order.getMainOrderItem().getCategoryId()){
			return null;
		}
		
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		//定义处理人
		String operator = null;
		
		//第一选择，找订单取消人
		operator = this.findOperatorForCancelAudit(order.getOrderId());
		if(UtilityTool.isValid(operator)){
			//判断权限
			if(this.judgeOperatorPerm(order.getMainOrderItem().getCategoryId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), operator)){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByPool(audit);
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "第一选择-找订单取消人[有被分单权限、在线或忙碌]", "处理人"+operator+"满足");
				return audit;
			}else{
				this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "第一选择-找订单取消人[有被分单权限、在线或忙碌]", "处理人"+operator+"无权限");
			}
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "第一选择-找订单取消人[有被分单权限、在线或忙碌]", "处理人不存在");
		}
		
		//第二选择，平均分配给小于接单上限的在线组员
		operator = this.findOperatorByGroup(order,audit.getAuditType());
		if(UtilityTool.isValid(operator)){
			audit.setOperatorName(operator);
			//更新订单池
			this.updateOrderAuditByPool(audit);
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "第二选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人"+operator+"满足");
			return audit;
		}else{
			this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "第二选择-随机找小于接单上限的且活动数最少的在线的分单组内人员[有被分单权限、在线]", "处理人不存在");
		}
		
		this.printDebugLog(order,OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCnName(), "分单规则执行完毕", "没找到任何满足条件的处理人");
		
		return null;
	}
	
	/**
	 * 预订通知自动分单
	 * 预订通知无自动分单
	 * @param audit
	 * @return
	 */
	@Deprecated
	@Override
	public ComAudit makeOrderAuditForBookingAudit(final ComAudit audit) {
		return null;
	}
	
	/**
	 * 预订通知人工分单-重载方法，给订单详情页预订通知使用
	 * 返回null代表没有分单成功
	 * 
	 * @param groupOrOperator 订单编号
	 * @param assignor 指派人
	 * @param isGroup 是指定组还是指定人 参考枚举值OrderEnum.IS_GROUP
	 * @return operator 被指派人
	 */
	public ComAudit makeOrderAuditForBookingAudit(final ComAudit comAudit,final String assignor,final String isGroup,final String groupOrOperator) {
		ComAudit audit = null;
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())){
			if(!StringUtils.isEmpty(comAudit.getAuditSubtype())){
				audit = orderAuditService.saveOrderAudit(comAudit.getObjectId(),
						comAudit.getAuditType(),comAudit.getAuditSubtype());
			}else{
				audit = orderAuditService.saveCreateOrderAudit(comAudit.getObjectId(),
						comAudit.getAuditType());
			}
		}else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(comAudit.getObjectType())){
			Long orderId = 0L;
			if(comAudit.getOrder() != null){
				orderId = comAudit.getOrder().getOrderId();
			}
			if(!StringUtils.isEmpty(comAudit.getAuditSubtype())){
				audit = orderAuditService.saveChildOrderAudit(orderId,comAudit.getObjectId(),
						comAudit.getAuditType(),comAudit.getAuditSubtype());
			}else{
				audit = orderAuditService.saveCreateChildOrderAudit(orderId,comAudit.getObjectId(),
						comAudit.getAuditType());
			}
		}
		
		if(audit == null){
			if(LOGGER.isInfoEnabled()){
				LOGGER.error("===objectId:"+comAudit.getObjectId()+"audit is null!");
			}
			return audit;
		}
		LOGGER.info("audit="+audit);
		if(!UtilityTool.isValid(assignor)){
			if(LOGGER.isInfoEnabled()){
				LOGGER.error("===objectId:"+comAudit.getObjectId()+"the parameter 'assignor' is error!");
			}
			return audit;
		}
		
		if(!UtilityTool.isValid(isGroup)){
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("===objectId:"+comAudit.getObjectId()+"the parameter 'isGroup' is error!");
			}
			return audit;
		}
		
		//找到订单对象
		OrdOrder order = this.findOrderFromOrder(audit);
		if(null == order || null == order.getMainOrderItem() || null == order.getMainOrderItem().getCategoryId()){
			return audit;
		}
		if(!order.isContainApiFlightTicket()){
			if(!UtilityTool.isValid(groupOrOperator)){
				if(LOGGER.isInfoEnabled()){
					LOGGER.error("==orderId:"+order.getOrderId()+"the parameter 'operator' is error!");
				}
				return audit;
			}
		}
		List<OrdOrderItem> ordOrderItemList = iOrdOrderItemService.selectByOrderId(order.getOrderId());//查询子订单信息
		order.setOrderItemList(ordOrderItemList);
		OrdOrderItem item = getOrderItemByAudit(order, audit);
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		boolean isNewRule = true;
//		try {
//			isNewRule = allocationClientService.isNewAllocationRule(order, audit);
//		} catch (Exception e) {
//			LOGGER.error("Vst-allocation is bad,audit:"+audit.getAuditId());
//			return audit;
//		}
		if(isNewRule){
			Map<String, Object> outBoundVipMap = new HashMap<String, Object>();
			boolean isOutBoundVip = false;
			//查看缓存VIP客服开关状态
			boolean isOutBuVipStatus = isOutBuVipStatus();
			String csUserId = "";
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isOutBuVipStatus:"+isOutBuVipStatus+" makeOrderAuditForBookingAudit");
			if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode()) && isOutBuVipStatus){
				outBoundVipMap = getCsVipMap(order, audit);
				if(outBoundVipMap.containsKey("isCsVipAndAllotCsVip") && Boolean.TRUE.equals(outBoundVipMap.get("isCsVipAndAllotCsVip"))){
					isOutBoundVip = true;
					csUserId = (String) outBoundVipMap.get("csUserId");
				}else if(outBoundVipMap.containsKey("isCsVipCsStaff") && Boolean.TRUE.equals(outBoundVipMap.get("isCsVipCsStaff"))){
					isOutBoundVip = true;
					csUserId = order.getBackUserId();
				}else if(outBoundVipMap.containsKey("isError") && Boolean.TRUE.equals(outBoundVipMap.get("isError"))){
					return null;
				}
			}
			//是否国内VIP客服主单
			Map<String, Object> localVipMap = new HashMap<String, Object>();
			boolean isLocalVip = false;
			String csLocalUserId = "";
			if(needVipOrder(order)){
				localVipMap = getCsVipMap(order, audit);
				if(localVipMap.containsKey("isCsVipAndAllotCsVip") && Boolean.TRUE.equals(localVipMap.get("isCsVipAndAllotCsVip"))){
					isLocalVip = true;
					csLocalUserId = (String) localVipMap.get("csUserId");
				}else if(localVipMap.containsKey("isCsVipCsStaff") && Boolean.TRUE.equals(localVipMap.get("isCsVipCsStaff"))){
					isLocalVip = true;
					csLocalUserId = order.getBackUserId();
				}else if(outBoundVipMap.containsKey("isError") && Boolean.TRUE.equals(outBoundVipMap.get("isError"))){
					return null;
				}
			}
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isLocalVip:"+isLocalVip+" csLocalUserId:"+csLocalUserId+" makeOrderAuditForBookingAudit");
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isOutBoundVip:"+isOutBoundVip+" csUserId:"+csUserId+" makeOrderAuditForBookingAudit");
			//门店和门店APP订单部分活动分配到指定账号
			boolean isLocalO2O = isLocalO2O(order);
			boolean isOutBoundO2O = isOutBoundO2O(order);
			boolean o2oFlag=false;
			//渠道为门店、门店APP的国内度假事业部 跟团SBU、机酒SBU订单屏蔽预定通知
			LOGGER.info("===orderId:"+order.getOrderId()+".isLocalO2O:"+isLocalO2O+"==="+".isOutBoundO2O:"+isOutBoundO2O+"===");
			if(isLocalO2O||isOutBoundO2O){
				String auditType = audit.getAuditType();
				String auditSubtype = audit.getAuditSubtype();
				if(OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.name().equalsIgnoreCase(auditType)){
					o2oFlag=true;
				}else if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(auditType)){
					if(OrderEnum.AUDIT_SUB_TYPE.ORDER_AMOUNT_CHANGE.name().equalsIgnoreCase(auditSubtype)
							||OrderEnum.AUDIT_SUB_TYPE.CHILD_ORDER_AMOUNT_CHANGE.name().equalsIgnoreCase(auditSubtype)
							||OrderEnum.AUDIT_SUB_TYPE.REMINDER_LAST_PAY.name().equalsIgnoreCase(auditSubtype)
							||OrderEnum.AUDIT_SUB_TYPE.REMINDER_EXCHANGE_STAMP.name().equalsIgnoreCase(auditSubtype)){
						o2oFlag=true;
					}
				}
			}
			String operator = null;
			if(o2oFlag){
				operator="cs6053";
				audit.setOperatorName(operator);
				this.updateOrderAuditByPool(audit);//更新该订单活动
				this.insertOrderLog(audit.getObjectType(), audit.getObjectId(), audit.getAuditType(), assignor, operator);
				allocationLogMessageProducer.newAllocationLogMessage(audit, "预订通知人工分单:" + operator);
			}else{
				//是否走国内机酒分单规则
				boolean isNewAllocationRule = isNewAllocationRule(order,item,audit);
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isNewAllocationRule:"+isNewAllocationRule+" makeOrderAuditForBookingAudit");
				if(isOutBoundVip && (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())
						|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType())))){
					operator = allocationClientService.makeOrderAuditForBookingAuditForVip(order, audit, assignor, isGroup, groupOrOperator, item, csUserId);
				}else if(isLocalVip&&OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
					operator = allocationClientService.makeOrderAuditForBookingAuditForVip(order, audit, assignor, isGroup, groupOrOperator, item, csLocalUserId);
				}/*update by xiexun --此处需求变更，由页面配置无需用代码实现绑定
				else if(isNewAllocationRule){
					operator = allocationClientService.makeOrderAuditForBookingAuditForNewRule(order, audit, assignor, isGroup, groupOrOperator, item);
				}*/else {
					operator = allocationClientService.makeOrderAuditForBookingAudit(order, audit, assignor, isGroup, groupOrOperator, item);
				}
				LOGGER.info("makeOrderAuditForBookingAudit auditId="+audit.getAuditId()+",operator="+operator);
				OrdAuditUserStatus user = auditUserStatusDAO.selectByPrimaryKey(operator);
				if(user != null){
					audit.setOperatorName(operator);
					this.updateOrderAuditByPool(audit);//更新该订单活动
					this.insertOrderLog(audit.getObjectType(), audit.getObjectId(), audit.getAuditType(), assignor, operator);
					allocationLogMessageProducer.newAllocationLogMessage(audit, "预订通知人工分单:" + operator);
				}else{
					allocationLogMessageProducer.newAllocationLogMessage(audit, "预订通知人工分单,接单人不在线:" + operator);
					LOGGER.info("===comAuditId:"+comAudit.getObjectId()+"==audit.getAuditId"+(audit!=null?audit.getAuditId():null));
					allocationMessageProducer.sendMsg(MessageFactory.newOrderAllocationMessage(audit.getAuditId(), "TRUE"));
				}
			}	
			return audit;
		}else{
			//如果是指定的活动组，则根据分单规则平均分配
			if(OrderEnum.IS_GROUP.YES.name().equals(isGroup)){
//				ComAudit result = makeOrderAudit(audit);
				//随机查找指定活动组中的在线的小于接单上限的且当前接单数最少的人
				OrdAuditUserStatus user = findTaskOperatorByGroup2(audit, item, groupOrOperator, order);
				if(user!=null){
					//设置处理人
					audit.setOperatorName(user.getOperatorName());
					//更新该订单活动
					this.updateOrderAuditByPool(audit);
					//将系统日志入库
					this.insertOrderLog(audit.getObjectType(), audit.getObjectId(), audit.getAuditType(), assignor, user.getOperatorName());
					return audit;
				}
			}
			
			//如果是指定的某一个人
			if(OrderEnum.IS_GROUP.NO.name().equals(isGroup)){
				//判断指定人是否在线或忙碌
				OrdAuditUserStatus user = auditUserStatusDAO.selectByPrimaryKey(groupOrOperator);
				if(user!=null){//现在不判断权限，直接分配。只需要当前人在线即可
					//设置处理人
					audit.setOperatorName(user.getOperatorName());
					//更新该订单活动
					this.updateOrderAuditByPool(audit);
					//将系统日志入库
					this.insertOrderLog(audit.getObjectType(), audit.getObjectId(), audit.getAuditType(), assignor, user.getOperatorName());
					return audit;
				}
			}
		}
		return audit;
	}
	
	/**
	 * 人工分单
	 * @param audit
	 * @param assignor
	 * @param operator
	 * @return
	 */
	@Override
	public ComAudit makeOrderAuditForManualAudit(final ComAudit audit, final String auditStatus,final String assignor, final String operator) {
		//判断输入参数
		if(null == audit || !UtilityTool.isValid(assignor) || !UtilityTool.isValid(operator)){
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("参数有错误，请好好检查!");
			}
			return null;
		}
		
		//数据库和页面一致才做以下操作
		if(audit.getAuditStatus().equals(auditStatus)){
			if(auditStatus.equals(OrderEnum.AUDIT_STATUS.POOL.name())){
				audit.setOperatorName(operator);
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
				//更新订单池
				this.updateOrderAuditByPool(audit);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("编号为["+audit.getObjectId()+"]的[待分配]订单成功分给了"+operator+",并将活动状态更新为[未处理]!");
				}
			}else if(auditStatus.equals(OrderEnum.AUDIT_STATUS.UNPROCESSED.name())){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByUnProcessed(audit);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("编号为["+audit.getObjectId()+"]的[未处理]订单成功分给了"+operator+"!");
				}
			}else if(auditStatus.equals(OrderEnum.AUDIT_STATUS.PROCESSED.name())){
				audit.setOperatorName(operator);
				//更新订单池
				this.updateOrderAuditByProcessed(audit);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("编号为["+audit.getObjectId()+"]的[已处理]订单成功分给了"+operator+"!");
				}
			}
		}else{
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("编号为["+audit.getObjectId()+"]的订单已抢先被SYSTEM自动分单，并且客服人员已处理完毕!");
			}
		}
		
		//将系统日志入库
		this.insertOrderLog(audit.getObjectType(), audit.getObjectId(), audit.getAuditType(), assignor, operator);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("编号为["+audit.getObjectId()+"]的订单分单日志入库成功,你可以打开订单处理页查看了!");
		}
		return audit;
	}
	
	private List<Long> findOrgIds(List<Long> source,Set<Long> orgids){
		List<Long> list = new ArrayList<Long>();
		
		for(Long id:source){
			if(orgids.contains(id)){
				list.add(id);
			}
		}
		return list;
	}
	
	/**
	 * 人工分单流程
	 * @param audit
	 * @param orgIds
	 * @param operator
	 * @return
	 */
	public ComAudit makeOrderAuditForManualAudit(final OrdOrder order,final ComAudit audit,final String status,final List<Long> orgIds,String operator,String assigner,boolean isForce){
		List<OrdOrderItem> ordOrderItemList = iOrdOrderItemService.selectByOrderId(order.getOrderId());//查询子订单信息
		order.setOrderItemList(ordOrderItemList);
		OrdOrderItem item = getOrderItemByAudit(order, audit);
		boolean isNewRule = true;
//		try {
//			isNewRule = allocationClientService.isNewAllocationRule(order, audit);
//		} catch (Exception e) {
//			LOGGER.error("Vst-allocation is bad,audit:"+audit.getAuditId());
//			return null;
//		}
		if(isNewRule){
			return makeOrderAuditForManualAuditNew(order, audit, orgIds, operator, assigner, item);
		}else{
			if(StringUtils.isEmpty(operator)){
				Assert.notEmpty(orgIds);
				List<Long> allocOrgids = findOperatorOrgIds(audit, item,audit.getAuditType(), order);
				if(allocOrgids==null){
					return null;
				}
				Set<Long> set = new HashSet<Long>(orgIds);
				List<Long> list  =findOrgIds(allocOrgids, set);
				if(list.isEmpty()){
					return null;
				}
				OrdAuditUserStatus user = auditUserStatusDAO.getMinTaskCountRandomUserByOrgIds(audit.getObjectType(),list);
				if(user==null){
					return null;
				}
				return makeOrderAuditForManualAudit(audit,status,assigner,user.getOperatorName());
			}else{
				if(!isForce){
					Map<String,Object> params = fillParam(audit, order, item, orgIds.get(0));
					if(ordAuditAllocationDao.selectCount(params)==0){
						LOGGER.error("人工分单权限错误-orderId:"+item.getOrderId()+" orderItemId:"+item.getOrderItemId()+" auditId:"+
								audit.getAuditId()+" functionCode:"+
								params.get("functionCode:")+" distributionChannel:"+params.get("distributionChannel")+" orgId:"+
								params.get("orgId")+" businessRuleIds:"+params.get("businessRuleIds"));
						return null;
					}
				}
				return makeOrderAuditForManualAudit(audit,status,assigner,operator);
			}
		}
	}
	
	/**
	 * 新的人工分单流程
	 * @param order 
	 * @param audit
	 * @param orgIds
	 * @param operator
	 * @param assigner
	 * @param item 
	 * @return
	 */
	private ComAudit makeOrderAuditForManualAuditNew(final OrdOrder order,final ComAudit audit,final List<Long> orgIds,String operator,String assigner,OrdOrderItem item){
		List<Long> roleIds = new ArrayList<Long>();
		List<PermRole> permRoleList = new ArrayList<PermRole>();
		// 获取角色列表
		if(StringUtils.isEmpty(operator)){
			Assert.notEmpty(orgIds);
			// 通过组织查询角色列表
			ResultHandleT<List<PermRole>> resultHandleT = permUserRoleProxyAdapter.getPermRoleByOrgId(orgIds);
			if(resultHandleT.isSuccess()){
				permRoleList = resultHandleT.getReturnContent();
			}
		}else{
//			// 通过分单人查询角色列表
//			ResultHandleT<List<PermRole>> resultHandleT = permUserRoleProxyAdapter.getPermRoleByUserno(operator);
//			if(resultHandleT.isSuccess()){
//				permRoleList = resultHandleT.getReturnContent();
//			}
			allocationLogMessageProducer.newAllocationLogMessage(audit, "新人工分单-operatorName:"+operator);
			return makeOrderAuditForManualAudit(audit, audit.getAuditStatus(), assigner, operator);
		}
		if(permRoleList != null && !permRoleList.isEmpty()){
			for(PermRole permRole : permRoleList){
				roleIds.add(permRole.getRoleId());
			}
		}
		String operatorName = allocationClientService.makeOrderAuditForManualAudit(order, item, audit, operator, assigner, roleIds);
		allocationLogMessageProducer.newAllocationLogMessage(audit, "新人工分单-operatorName:"+operatorName);
		return makeOrderAuditForManualAudit(audit, audit.getAuditStatus(), assigner, operatorName);
	}
	
	/**
	 * 更新订单池对象
	 * 
	 * @param audit
	 * @return
	 */
	private int updateOrderAuditByPool(ComAudit audit){
		//打印当前活动数据
		LOGGER.debug(audit.toString());
		//更新分单缓存
		this.updateCacheCountByName(audit.getOperatorName(),audit);	
		//更新当前活动数据
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("auditId", audit.getAuditId());
		param.put("auditStatus", audit.getAuditStatus());
		param.put("operatorName", audit.getOperatorName());
		return orderAuditService.updateComAuditByPool(param);
	}
	
	/**
	 * 更新订单池对象
	 * 
	 * @param audit
	 * @return
	 */
	private int updateOrderAuditByUnProcessed(ComAudit audit){
		//打印当前活动数据
		LOGGER.debug(audit.toString());
		//更新分单缓存
		this.updateCacheCountByName(audit.getOperatorName(),audit);
		//更新当前活动数据
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("auditId", audit.getAuditId());
		param.put("operatorName", audit.getOperatorName());
		return orderAuditService.updateComAuditByUnProcessed(param);
	}
	
	/**
	 * 更新订单池对象
	 * 
	 * @param audit
	 * @return
	 */
	private int updateOrderAuditByProcessed(ComAudit audit){
		//打印当前活动数据
		LOGGER.debug(audit.toString());
		//更新分单缓存
		this.updateCacheCountByName(audit.getOperatorName(),audit);
		//更新当前活动数据
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("auditId", audit.getAuditId());
		param.put("operatorName", audit.getOperatorName());
		return orderAuditService.updateComAuditByProcessed(param);
	}	
	
	/**
	 * 
	 * 保存人工分单日志
	 * 
	 */
	private void insertOrderLog(String objectType, Long objectId,String auditType,String assignor,String operator){
		String auditTypeName = ConfirmEnumUtils.getCnName(auditType);

		lvmmLogClientService.sendLog(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name()
				.equals(objectType) ? ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM
				: ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				objectId, // parentId 与 objectId 相同
				objectId, 
				assignor, 
				"将编号为["+objectId+"]的订单分给员工["+operator+"]进行["+auditTypeName+"]任务",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_DISTRIBUTION.getCnName()+"["+auditType+"]",
				null);
	}
	
	/**
	 * 根据订单ID查找订单对象
	 * 
	 * @param audit
	 * @return
	 */
	@Override
	public OrdOrder findOrderFromOrder(ComAudit audit){
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(audit.getObjectType())){
			condition.getOrderIndentityParam().setOrderId(audit.getObjectId());
		}else{
			condition.getOrderIndentityParam().setOrderItemId(audit.getObjectId());
		}
		condition.getOrderFlagParam().setOrderItemTableFlag(true);
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);
		condition.getOrderFlagParam().setOrderPackTableFlag(true);
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if(null != orderList && !orderList.isEmpty() && null != orderList.get(0)){
			return orderList.get(0);
		}else{
			LOGGER.info("comaudit id:"+audit.getAuditId()+" find order==null,objectId:"+audit.getObjectId()+" ,"+audit.getObjectType());
			return null;
		}
	}
	
	/**
	 * 根据auditId查找订单对象
	 * @param auditId
	 * @return
	 */
	public ComAudit findOrderFromOrderPoolAndUnprocess(Long auditId){
		//定义查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("auditId", auditId);
		
		//根据auditId查询唯一的一个对象
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		LOGGER.debug(audit.toString());
		
		return audit;
	}
	
	/**
	 * 
	 * 根据订单ID找到订单对象的后台下单人
	 * 
	 * @param order
	 * @return
	 */
	private String findOperatorForInfoAudit(final OrdOrder order){
		String operator = order.getBackUserId();
		//判断在线状态，在线或忙碌
		if(!this.judgeWorkStatus(operator).equals(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name())){
			return operator;
		}else{
			return null;
		}
	}
	
	/**
	 * 根据订单ID查找订单的后台取消人
	 * 数据源为操作日志表
	 * @param orderId
	 * @return
	 */
	private String findOperatorForCancelAudit(final Long orderId){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", orderId);
		param.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.name());
		param.put("logType", ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name());
		List<ComLog> logList = comLogClientService.queryComLogListByCondition(param).getReturnContent();
		if(null!=logList && !logList.isEmpty()){
			//一个订单只可能被取消一次，所以日志里一个订单的取消日志只能存在一条
			String operator = logList.get(0).getOperatorName();
			//判断在线状态，在线或忙碌
			if(!this.judgeWorkStatus(operator).equals(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name())){
				return operator;
			}else{
				return null;
			}
		}
		return null;
	}
	
	/**
	 * 找到同商品ID的订单处理人
	 * 只有商品和供应商有关系,产品是我们自己划分的，斌哥说没意义
	 * @param orderId
	 * @return
	 */
	private String findOperatorByGoodsId(final Long orderId){
		List<String> operators = new ArrayList<String>();
		//查询同商品的订单ID,但不包含该订单本身
		List<OrdOrderItem> orderItems = ordOrderUpdateService.queryOrderIdByOrderId(orderId);
		if(null != orderItems && !orderItems.isEmpty()){
			Long[] orderIds = new Long[orderItems.size()];
			for(int i=0;i<orderItems.size();i++){
				orderIds[i] = orderItems.get(i).getOrderId();
			}
			
			if(!ArrayUtils.isEmpty(orderIds)){
				//查询同商品的操作人群
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("objectIds", orderIds);
				param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
				param.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
				List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(param);
				if(null != auditList && !auditList.isEmpty()){
					for(ComAudit audit:auditList){
						String operator = audit.getOperatorName();
						//判断在线状态,必须在线
						if(this.judgeWorkStatus(operator).equals(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name())){
							operators.add(audit.getOperatorName());
						}
					}
				}
			}
		}
		
		//同商品ID的人可能有多个，随机找一个同商品ID的人
		//朱海说这里不考虑接单上限
		if(null != operators && !operators.isEmpty()){
			return this.findOperatorByRandom(operators);
		}
		
		return null;
	}
	
	/**
	 * 
	 * 根据订单ID、活动类型、活动状态查找相应的处理人
	 *
	 * 
	 * @param auditType
	 * @param auditStatus
	 * @return
	 */
	private String findOperatorByAudit(final Long orderId,final String auditType,final String auditStatus){
		//定义查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", orderId);
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		param.put("auditType", auditType);
		param.put("auditStatus", auditStatus);
		
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(param);
		if(null != auditList && auditList.size()>0){
			//每个订单的每个活动应该在audit表里只有一条记录
			String operator = auditList.get(0).getOperatorName();
			//判断在线状态，在线或忙碌
			if(!this.judgeWorkStatus(operator).equals(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name())){
				return operator;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * 
	 * 根据订单ID、活动类型、活动状态查找相应的处理人
	 *
	 * 排除SYSTEM,供查找资源审核人用
	 * 
	 * @param auditType
	 * @param auditStatus
	 * @return
	 */
	private String findOperatorByAudit(final Long orderId,final String auditType,final String auditStatus,final String excludeOperator){	
		//定义查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", orderId);
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		param.put("auditType", auditType);
		param.put("auditStatus", auditStatus);
		param.put("excludeOperator", excludeOperator);
		
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(param);
		if(null != auditList && auditList.size()>0){
			//每个订单的每个活动应该在audit表里只有一条记录
			String operator = auditList.get(0).getOperatorName();
			//判断在线状态，在线或忙碌
			if(!this.judgeWorkStatus(operator).equals(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name())){
				return operator;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	
	
	/**
	 * 
	 * 根据可以干活的组内人员随机查找一个处理人
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String findOperatorByGroup(final OrdOrder order,final String auditType){
		//找到可以干活的人群(有接单权限，在线，并且没有小于接单上限)
		final List<OrderOperatorVo> operatorVoList = this.buildValidOperator(order,auditType);
		
		//打印当前可以干活的一群人
		LOGGER.debug(operatorVoList.toString());
		
		//按照当前活动数自定义排序
		Collections.sort(operatorVoList);
		//打印排序后的集合
		LOGGER.debug(operatorVoList.toString());
		
		//找到所有活动数最少的人群,也就是和第一个对象活动数相等的所有元素
		ArrayList<OrderOperatorVo> tempList = new ArrayList<OrderOperatorVo>();
		if(null!=operatorVoList&&operatorVoList.size()>0){
			OrderOperatorVo operatorVo1 = operatorVoList.get(0);
			tempList.add(operatorVo1);
			for(int i=1;i<operatorVoList.size();i++){
				OrderOperatorVo operatorVoOther = operatorVoList.get(i);
				if(operatorVoOther.getCurrentAuditCount().intValue() != operatorVo1.getCurrentAuditCount().intValue()){
					break;
				}
				tempList.add(operatorVoOther);
			}
		}
		
		//打印活动数最小的人群
		if(LOGGER.isDebugEnabled()){
			String auditTypeName = ConfirmEnumUtils.getCnName(auditType);
			LOGGER.debug("自动订单["+order.getOrderId()+"],活动["+auditTypeName+"]，小于接单上限的且当前活动数最少的在线的人群为["+operatorVoList+"]");
		}
		
		//在活动数最小的人群众随机找一个人
		if(null != tempList && tempList.size()>0){
			return this.findOperatorByRandomAndLimit(tempList);
		}
		
		return null;
	}
	
	private OrdAuditUserStatus findTaskOperatorByGroup2(final ComAudit audit,final OrdOrderItem item,final String auditType, OrdOrder order){
		List<Long> orgIds = findOperatorOrgIds(audit, item,auditType, order);
		if(CollectionUtils.isEmpty(orgIds)){
			return null;
		}
		
		OrdAuditUserStatus  user = auditUserStatusDAO.getMinTaskCountRandomUserByOrgIds(audit.getObjectType(),orgIds);
		if(user==null){
			return null;
		}
		return user;
	}
	
	/**
	 * 根据活动权限和在线状态清洗人工分单的人群
	 * 
	 * @param operatorList
	 * @return
	 */
	public List<String> findOperatorByClean(Long categoryId,String auditType,List<PermUser> operatorList,boolean isForce){
		List<String> operatorListNew = new ArrayList<String>();
		for(PermUser permUser:operatorList){
			//判断是否拥有活动权限
			List<OrdAuditConfig> auditConfigList = ordAuditConfigService.findOrdAuditConfigList(categoryId, auditType, permUser.getUserName());
			if(null != auditConfigList && !auditConfigList.isEmpty()){
				//只可能存在一条
				OrdAuditConfig auditConfig = auditConfigList.get(0);
				String operatorName = auditConfig.getOperatorName();
				//强制分单
				if(isForce){
					operatorListNew.add(operatorName);
				}else{
					//判断在线状态,根据朱海的需求，选择在线或者忙碌
					if(!this.judgeWorkStatus(operatorName).equals(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name())){
						operatorListNew.add(operatorName);
					}
				}
			}
		}
		return operatorListNew;
	}
	
	/**
	 * 随机从集合中找到一个处理人
	 * 
	 * @param operatorList
	 * @return
	 */
	public String findOperatorByRandom(List<String> operatorList){
		if(null != operatorList && !operatorList.isEmpty()){
			Random random = new Random();
			//随机找一个处理人
			int i = random.nextInt(operatorList.size());
			return operatorList.get(i);
		}else{
			return null;
		}
	}
	
	/**
	 * 从当前活动接单数最少的人群中随机找一个人
	 * 
	 * @param operatorList
	 * @return
	 */
	private String findOperatorByRandomAndLimit(List<OrderOperatorVo> operatorList){
		if(null != operatorList && !operatorList.isEmpty()){
			Random random = new Random();
			//随机找一个处理人
			int i = random.nextInt(operatorList.size());
			return operatorList.get(i).getOperator();
		}else{
			return null;
		}
	}
	
	/**
	 * 查找拥有某一活动组权限的人群
	 * 
	 * @param auditType
	 * @return
	 */
	private List<OrdAuditConfig> findAuditConfigList(final OrdOrder order,final String auditType){
		try {
			//找到该订单的品类ID
			Long categoryId = order.getMainOrderItem().getCategoryId();
			//调业务层
			List<OrdAuditConfig> auditConfigList = ordAuditConfigService.findOrdAuditConfigList(categoryId,auditType);
			return auditConfigList;
		} catch (Exception e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("audit config is null");
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
			return null;
		}
	}
	
	/**
	 * 根据处理人、活动类型、活动状态查找当前正在进行中的活动数量
	 * 
	 * @param operator
	 * @param auditType
	 * @param auditStatus
	 * @return
	 */
	private int findAuditCount(final String operator,final String auditType,final String auditStatus){
		//定义查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("operatorName", operator);
		param.put("auditType", auditType);
		param.put("auditStatus", auditStatus);
		return orderAuditService.countAuditByCondition(param);
	}
	
	/**
	 * 判断当前正在处理的活动数量是否达到活动的分单上限
	 * 
	 * @param limitAuditCount
	 * @param currentAuditCount
	 * @return
	 */
	private boolean judgeAuditCount(int limitAuditCount,int currentAuditCount){
		return currentAuditCount<limitAuditCount?true:false;
	}
	
	/**
	 * 判断处理人的在线状态
	 * 
	 * @param operator
	 * @return
	 */
	private String judgeWorkStatus(final String operator){
		//在线、忙碌、离线
		OrdAuditUserStatus ordAuditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(operator);
		if(null != ordAuditUserStatus){
			return ordAuditUserStatus.getUserStatus();
		}
		return OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
	}

	/**
	 * 
	 * 根据活动组类型构建满足条件的所有活动组内人员
	 * 
	 * @return
	 */
	private List<OrderOperatorVo> buildValidOperator(final OrdOrder order,final String auditType){
		List<OrderOperatorVo> operatorVoList = new ArrayList<OrderOperatorVo>();
		//查找具有该分单权限的人群
		List<OrdAuditConfig> auditConfigList = this.findAuditConfigList(order,auditType);
		if(null != auditConfigList && auditConfigList.size()>0){
			for(OrdAuditConfig auditConfig:auditConfigList){
				String operator = auditConfig.getOperatorName();
				Long limitAuditCount = auditConfig.getTaskLimit();
				Integer currentAuditCount = this.findAuditCount(operator, auditType, AUDIT_STATUS.UNPROCESSED.name());
				String workStatus = this.judgeWorkStatus(operator);
				//判断是否超过接单上限
				boolean isWorkable = this.judgeAuditCount(limitAuditCount.intValue(), currentAuditCount);
				//当活动为售后时，去掉小于接单上限的约束 added by wenzhengtao 20131217
				if(OrderEnum.AUDIT_TYPE.SALE_AUDIT.name().equals(auditType)){
					isWorkable = true;
				}
				//如果在线并且可以工作，就加入候选队列
				if(workStatus.equals(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name()) && isWorkable){
					OrderOperatorVo operatorVo = new OrderOperatorVo(operator, auditType, limitAuditCount.intValue(), currentAuditCount, workStatus, isWorkable);
					operatorVoList.add(operatorVo);
				}
			}
		}
		return operatorVoList;
	}
	
	/**
	 * 判断找到的处理人是否有相应的权限
	 * 
	 * @param categoryId 品类ID
	 * @param auditType 审核类型
	 * @param operatorName 处理人
	 * @return
	 */
	private boolean judgeOperatorPerm(Long categoryId,String auditType,String operatorName){
		List<OrdAuditConfig> auditConfigList = ordAuditConfigService.findOrdAuditConfigList(categoryId, auditType, operatorName);
		if(CollectionUtils.isNotEmpty(auditConfigList)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * debug模式下记录自动分单日志
	 * 
	 * @param auditType
	 * @param step
	 * @param content
	 */
	private void printDebugLog(OrdOrder order,String auditType,String step,String content){
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("自动分单["+order.getOrderId()+"],活动["+auditType+"],规则["+step+"],描述["+content+"]");
		}
	}
	
	@Autowired
	private OrdAuditAllocationDao ordAuditAllocationDao;
	
	@Autowired
	private OrdAuditUserStatusDAO auditUserStatusDAO;
	
	@Autowired
	private OrdUserCounterDao userCounterDao;
	
	/**
	 * 同产品同供应商
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean isSameProduct(OrdOrderItem t1,OrdOrderItem t2){
		return(t1.getSupplierId().equals(t2.getSupplierId())&&t1.getProductId().equals(t2.getProductId()));	
	}
	
	private List<Long> getRuleIds(OrdOrderItem orderItem){
		List<Long> ids = new ArrayList<Long>();
		for(Long key:businessRuleMap.keySet()){
			BusinessRule rule = businessRuleMap.get(key);
			if(orderItem.getCategoryId().equals(rule.getCategoryId())&&OrderPropertyRule.calculateRule(
					rule.getRuleCode(), orderItem)){
				ids.add(key);
			}
		}
		
		// 列表空值处理
		if (CollectionUtils.isEmpty(ids)) {
			ids = null;
		}
		
		return ids;
	}
	
	@Override
	public ComAudit makeOrderAudit(ComAudit audit) {
		//新增判断，是否采用新的规则
		OrdOrder order = findOrderFromOrder(audit);
		List<OrdOrderItem> ordOrderItemList = iOrdOrderItemService.selectByOrderId(order.getOrderId());//查询子订单信息
		order.setOrderItemList(ordOrderItemList);
		OrdOrderItem item = getOrderItemByAudit(order,audit);
		if(item == null){
			LOGGER.error("audit:"+audit.getAuditId()+" can not find ord_order_item");
			return null;
		}
		boolean isNewRule = true;
//		try {
//			isNewRule = allocationClientService.isNewAllocationRule(order, audit);
//		} catch (Exception e) {
//			LOGGER.error("Vst-allocation is bad,audit:"+audit.getAuditId());
//			return null;
//		}
		//LOGGER.info("分单开始-makeOrderAudit,分单 id:"+audit.getAuditId()+" objectId:"+audit.getObjectId()+",distributorId:"+order.getDistributorId()+",isNewRule:"+isNewRule);
		if(isNewRule){
			String operatorName = null;
			Map<String, Object> outBoundVipMap = new HashMap<String, Object>();
			//是否出境VIP客服主单
			boolean isOutBoundVip = false;
			//查看缓存VIP客服开关状态
			boolean isOutBuVipStatus = isOutBuVipStatus();
			String csUserId = "";
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isOutBuVipStatus:"+isOutBuVipStatus+" makeOrderAudit");
			if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode()) && isOutBuVipStatus){
				outBoundVipMap = getCsVipMap(order, audit);
				if(outBoundVipMap.containsKey("isCsVipAndAllotCsVip") && Boolean.TRUE.equals(outBoundVipMap.get("isCsVipAndAllotCsVip"))){
					isOutBoundVip = true;
					csUserId = (String) outBoundVipMap.get("csUserId");
				}else if(outBoundVipMap.containsKey("isCsVipCsStaff") && Boolean.TRUE.equals(outBoundVipMap.get("isCsVipCsStaff"))){
					isOutBoundVip = true;
					csUserId = order.getBackUserId();
				}else if(outBoundVipMap.containsKey("isError") && Boolean.TRUE.equals(outBoundVipMap.get("isError"))){
					return null;
				}
			}
			//是否国内VIP客服主单
			Map<String, Object> localVipMap = new HashMap<String, Object>();
			boolean isLocalVip = false;
			String csLocalUserId = "";
			if(needVipOrder(order)){
				localVipMap = getCsVipMap(order, audit);
				if(localVipMap.containsKey("isCsVipAndAllotCsVip") && Boolean.TRUE.equals(localVipMap.get("isCsVipAndAllotCsVip"))){
					isLocalVip = true;
					csLocalUserId = (String) localVipMap.get("csUserId");
				}else if(localVipMap.containsKey("isCsVipCsStaff") && Boolean.TRUE.equals(localVipMap.get("isCsVipCsStaff"))){
					isLocalVip = true;
					csLocalUserId = order.getBackUserId();
				}else if(outBoundVipMap.containsKey("isError") && Boolean.TRUE.equals(outBoundVipMap.get("isError"))){
					return null;
				}
			}
			//是否直营门店分店
			boolean isDriectSale = false;
			isDriectSale = isDriectSale(order,audit);
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isDriectSale:"+isDriectSale+" distributorCode:"+order.getDistributorCode()+" makeOrderAudit");
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isLocalVip:"+isLocalVip+" csLocalUserId"+csLocalUserId+" makeOrderAudit");
			LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isOutBoundVip:"+isOutBoundVip+" csUserId:"+csUserId+" makeOrderAudit");
			if(isDriectSale && OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
				String ordOperatorName = null;
				OrdResponsible ordRes = responsibleDao.getResponsibleByObject(order.getOrderId(), audit.getObjectType());
				if (ordRes != null) {
					ordOperatorName = ordRes.getOperatorName();
				}
				operatorName = allocationClientService.autoMakeAllocationDivideForDriectSale(order,item,audit,ordOperatorName);
				if (!StringUtils.isEmpty(operatorName)) {
					if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
							|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())) {
						audit.setAuditStatus(null);// 表示不更新状态
					} else {
						audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
					}
					audit.setOperatorName(operatorName);
					this.updateCacheCountByName(operatorName,audit);
					return orderAuditService.updateAuditAssignForDriectSale(audit, order, operatorName);
				}
			}else if(isOutBoundVip && (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())
				|| (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType()) && OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType())))){
				String ordOperatorName = null;
				OrdResponsible ordRes = responsibleDao.getResponsibleByObject(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				if (ordRes != null) {
					ordOperatorName = ordRes.getOperatorName();
				}
				operatorName = allocationClientService.autoMakeAllocationDivideForVip(order, item, audit, ordOperatorName, csUserId);
				if (!StringUtils.isEmpty(operatorName)) {
					if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
							|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())) {
						audit.setAuditStatus(null);// 表示不更新状态
					} else {
						audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
					}
					audit.setOperatorName(operatorName);
					this.updateCacheCountByName(operatorName,audit);
					return orderAuditService.updateAuditAssignForVip(audit, order, csUserId, operatorName);
				}
			} else if(isLocalVip&&OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
				String ordOperatorName = null;
				OrdResponsible ordRes = responsibleDao.getResponsibleByObject(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				if (ordRes != null) {
					ordOperatorName = ordRes.getOperatorName();
				}
				operatorName = allocationClientService.autoMakeAllocationDivideForVip(order, item, audit, ordOperatorName, csLocalUserId);
				if (!StringUtils.isEmpty(operatorName)) {
					if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
							|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())) {
						audit.setAuditStatus(null);// 表示不更新状态
					} else {
						audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
					}
					audit.setOperatorName(operatorName);
					this.updateCacheCountByName(operatorName,audit);
					return orderAuditService.updateAuditAssignForLocalVip(audit, order, csLocalUserId, operatorName);
				}
			}else{
				boolean isLocalO2O = isLocalO2O(order);
				boolean isOutBoundO2O = isOutBoundO2O(order);
				boolean o2oFlag=false;
				//渠道为门店、门店APP的国内度假事业部 跟团SBU、机酒SBU订单屏蔽预定通知
				LOGGER.info("===orderId:"+order.getOrderId()+".isLocalO2O:"+isLocalO2O+"===");
				//渠道为门店、门店APP的出境跟团，自由行，当地游，邮轮订单,主订单负责人分配给指定工号
				LOGGER.info("===orderId:"+order.getOrderId()+".isOutBoundO2O:"+isOutBoundO2O+"===");
				//门店和门店APP订单部分活动分配到指定账号
				if(isLocalO2O||isOutBoundO2O){
					String auditType = audit.getAuditType();
					String auditSubtype = audit.getAuditSubtype();
					if(OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.name().equalsIgnoreCase(auditType)){
						o2oFlag=true;
					}else if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(auditType)){
						if(OrderEnum.AUDIT_SUB_TYPE.ORDER_AMOUNT_CHANGE.name().equalsIgnoreCase(auditSubtype)
								||OrderEnum.AUDIT_SUB_TYPE.CHILD_ORDER_AMOUNT_CHANGE.name().equalsIgnoreCase(auditSubtype)
								||OrderEnum.AUDIT_SUB_TYPE.REMINDER_LAST_PAY.name().equalsIgnoreCase(auditSubtype)
								||OrderEnum.AUDIT_SUB_TYPE.REMINDER_EXCHANGE_STAMP.name().equalsIgnoreCase(auditSubtype)){
							o2oFlag=true;
						}
					}
				}
				//是否走国内机酒分单规则
				boolean isNewAllocationRule = isNewAllocationRule(order,item,audit);
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" isNewAllocationRule:"+isNewAllocationRule+" makeOrderAudit");
				if(o2oFlag){
					operatorName="cs6053";
					allocationLogMessageProducer.newAllocationLogMessage(audit, "活动处理人为："+operatorName);
				}else{
					/* update by xiexun --此处需求变更，由页面配置无需用代码实现绑定
					 * if(isNewAllocationRule){
						operatorName = allocationClientService.autoMakeAllocationDivideForNewRule(order, item, audit);
					}else{*/
						operatorName = allocationClientService.autoMakeAllocationDivide(order, item, audit);
						LOGGER.info("auditId:" + audit.getAuditId() + " orderId:" + order.getOrderId() + " operatorName:" + operatorName + " auditStatus:" + audit.getAuditStatus() + " makeOrderAudit");
//					}
				}
				if (!StringUtils.isEmpty(operatorName)) {
					if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
							|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())) {
						audit.setAuditStatus(null);// 表示不更新状态
					} else {
						audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
					}
					audit.setOperatorName(operatorName);
		/*	update by xiexun --此处需求变更，由页面配置无需用代码实现绑定		
		 * if(isNewAllocationRule){
						return orderAuditService.updateAuditAssignForNewRule(audit, order, operatorName);
					}else{*/
						OrdResponsible res = responsibleDao.getResponsibleByObject(audit.getObjectId(), audit.getObjectType());
						if (res == null) {
							return doAuditAssignNew(audit, order, item, true, isOutBoundVip,isLocalVip,csLocalUserId);
						} else {
							return doAuditAssignNew(audit, order, item, false, isOutBoundVip,isLocalVip,csLocalUserId);
						}
//					}	
				}
			}
			if (StringUtils.isEmpty(operatorName)) {
				//如果分单失败的话 就修改下次分单时间  为当前时间的半小时之后
				Map<String, Object> map = new HashMap<String, Object>();
				Calendar nowTime = Calendar.getInstance();
				nowTime.add(Calendar.MINUTE, 30);
				map.put("nextAssignTime", nowTime.getTime());
				map.put("auditId", audit.getAuditId());
				orderAuditService.updateNextAssignTime(map);
			}
		}else{
			//allocationLogMessageProducer.newAllocationLogMessage(audit, "自动分单开始，老分单规则");
			OrdResponsible res = responsibleDao.getResponsibleByObject(audit.getObjectId(),audit.getObjectType());
			if(OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
					|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())){
				audit.setAuditStatus(null);// 表示不更新状态
			}else{
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
			}
			
			//如果负责人不存在,按当前的活动查找处理活动的组织
			if(res==null){
				return doAuditAssign(audit, order,item,true);
			}else{//如果已经存在负责人
				Map<String, Object> params = fillParam(audit, order, item,res.getOrgId());
				boolean flag = ordAuditAllocationDao.selectCount(params)>0;
				if(flag){
					audit.setOperatorName(res.getOperatorName());
					int ret = orderAuditService.updateByPrimaryKeyNew(audit);
					if (ret < 1) {
						return null;
					}
					return audit;
				}else{
					boolean success=false;
					//子订单分配规则
					if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
						//首先分给主订单负责人
						res = responsibleDao.getResponsibleByObject(item.getOrderId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
						if(res!=null){
							params.put("orgId", res.getOrgId());
							flag = ordAuditAllocationDao.selectCount(params)>0;
							if(flag){
								audit.setOperatorName(res.getOperatorName());
								int ret = orderAuditService.updateByPrimaryKeyNew(audit);
								if (ret < 1) {
									return null;
								}
								return audit;
							}
						}
						if(!success){
							//最后的操作
							return doAuditAssign(audit,order, item,false);
						}
					}
				}
			}
		}
		return null;
	}

	private boolean isNewAllocationRule(OrdOrder order, OrdOrderItem item, ComAudit audit) {
		printAllocationLog(order,item,audit);
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
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())&&
					item != null && item.getCategoryId() != null){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(item.getCategoryId()) 
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(item.getCategoryId())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(item.getCategoryId())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(item.getCategoryId())
						|| (CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(item.getRealBuType())
								&&(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(item.getCategoryId())
										||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(item.getCategoryId())
										||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(item.getCategoryId())))
						|| (CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(item.getRealBuType())
								&&(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(item.getCategoryId())
										||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(item.getCategoryId())))){
					//return true;
					return false;
				}
			}else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
				return true;
			}
		}
		return false;
	}

	private void printAllocationLog(OrdOrder order, OrdOrderItem item, ComAudit audit) {
		StringBuffer bs=new StringBuffer();
		bs.append("机酒订单子订单活动原由角色变更 >");
		if(null!=order){
			String order_bu_code=order.getBuCode();
			Long order_category_id=order.getCategoryId();
			Long order_sub_category_id=order.getSubCategoryId();
			Long distributor_id=order.getDistributorId();
			Long distribution_channel=order.getDistributionChannel();
			Date createTime=order.getCreateTime();
			bs.append("order_bu_code=").append(order_bu_code)
					.append(",order_category_id=").append(order_category_id)
					.append(",order_sub_category_id=").append(order_sub_category_id)
					.append(",distributor_id=").append(distributor_id)
					.append(",distribution_channel=").append(distribution_channel)
					.append(",createTime=").append(createTime);
		}

		if(null!=item){
			Long item_category_id=item.getCategoryId();
			String item_real_bu_type=item.getRealBuType();
			bs.append(",item_category_id=").append(item_category_id)
					.append(",item_real_bu_type=").append(item_real_bu_type);
		}

		if(null!=audit){
			String object_type=audit.getObjectType();
			bs.append(",object_type=").append(object_type);
		}
		LOGGER.info(bs.toString());
		bs.setLength(0);
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
				if(OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType())
						||OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name().equals(audit.getAuditType())
						||OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(audit.getAuditType())
						||OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equals(audit.getAuditType())
						||OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.name().equals(audit.getAuditType())){
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, Object> fillParam(ComAudit audit,
			OrdOrder order, OrdOrderItem item,Long orgId) {
        if(null == item)
            throw new BusinessException("item can not be null!!!");
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("categoryId", item.getCategoryId());
		params.put("functionCode", audit.getAuditType());
		params.put("distributionChannel", getDistributionChannel(order));
		params.put("orgId", orgId);
		params.put("businessRuleIds", getRuleIds(item));
		return params;
	}
	
	public void changeTaskOperator(ComAudit audit,PermUser user){
		OrdOrder order = findOrderFromOrder(audit);
		OrdOrderItem item = getOrderItemByAudit(order,audit);
		Map<String,Object> params = fillParam(audit, order, item, user.getDepartmentId());
		if(ordAuditAllocationDao.selectCount(params)>0){
			audit.setOperatorName(user.getUserName());
			orderAuditService.updateByPrimaryKey(audit);
		}
	}

	private Map<String, Object> getCsVipMap(OrdOrder order, ComAudit audit) {
		Map<String, Object> csVipMap = new HashMap<String, Object>();
		//排除分销的订单(不包括无线)
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if((Constant.DIST_BRANCH_SELL==order.getDistributorId() && ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue()))
				|| Constant.DIST_FRONT_END==order.getDistributorId()){
			try {
				if(order.getUserNo() == null){
					return csVipMap;
				}
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" getCsVipByCondition start");
				csVipMap = csVipDubboService.allotCsVipByUserId(order.getUserNo());
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" getCsVipByCondition end");
			} catch (Exception e) {
				LOGGER.error("getCsVipByCondition is error,auditId:"+audit.getAuditId());
				csVipMap.put("isError", Boolean.TRUE);
				//如果分单失败的话 就修改下次分单时间  为当前时间的半小时之后
				Map<String, Object> map = new HashMap<String, Object>();
				Calendar nowTime = Calendar.getInstance();
				nowTime.add(Calendar.MINUTE, 30);
				map.put("nextAssignTime", nowTime.getTime());
				map.put("auditId", audit.getAuditId());
				orderAuditService.updateNextAssignTime(map);
			}
		} else if(Constant.DIST_BACK_END==order.getDistributorId()) {
			try {
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" getCsStaff start");
				csVipMap = csVipDubboService.getCsStaff(order.getBackUserId());
				LOGGER.info("auditId:"+audit.getAuditId()+" orderId:"+order.getOrderId()+" getCsStaff end");
			} catch (Exception e) {
				LOGGER.error("getCsStaff is error,auditId:"+audit.getAuditId());
				csVipMap.put("isError", Boolean.TRUE);
				//如果分单失败的话 就修改下次分单时间  为当前时间的半小时之后
				Map<String, Object> map = new HashMap<String, Object>();
				Calendar nowTime = Calendar.getInstance();
				nowTime.add(Calendar.MINUTE, 30);
				map.put("nextAssignTime", nowTime.getTime());
				map.put("auditId", audit.getAuditId());
				orderAuditService.updateNextAssignTime(map);
			}
		}
		return csVipMap;
	}

	private boolean isOutBuVipStatus(){
		//添加sweet配置,实现配置控制出境vip定单是否分配给vip客服
		String isOutBuVipAuditStatus = PropertiesUtil.getValue("outbu_vip_audit_status");
		if("false".equals(isOutBuVipAuditStatus)){
			return false;
		}
		//出境vip分单开关默认打开
		String isOutBuVipStatus = MemcachedUtil.getInstance().get("OUTBU_VIP_AUDIT_STATUS");
		if(isOutBuVipStatus != null && isOutBuVipStatus.equals("false")){
			return false;
		}
		return true;
	}

	private ComAudit doAuditAssign(ComAudit audit,OrdOrder order, OrdOrderItem item,boolean responsibleFlag) {
		
		OrdAuditUserStatus  user = null;
		if (responsibleFlag) {
			
			//下单渠道
			//Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();
			String operateName=null;
			if (Constants.DISTRIBUTOR_2.equals(order.getDistributorId())) {
				
				operateName=order.getBackUserId();
				//是否在线
				if (StringUtils.isNotEmpty(operateName)) {
					user=auditUserStatusDAO.selectByPrimaryKey(operateName);
				}
				//是否有接单权限
				if(user!=null){
					Map<String,Object> params = fillParam(audit, order, item, user.getOrgId());
					if(ordAuditAllocationDao.selectCount(params)<=0){
						user=null;
					}
				}
			}else if (Constants.DISTRIBUTOR_3.equals(order.getDistributorId())) {
				
				//LVCC里面外呼当前下单账户手机号记录的客服 3月内 客服
				OrdPerson ordPerson=order.getContactPerson();
				
				if (ordPerson!=null) {
					String mobile=ordPerson.getMobile();
					operateName=connRecordService.queryConnRecordWithPage(mobile, 1L, 1);
					
					//是否在线
					if (StringUtils.isNotEmpty(operateName)) {
						user=auditUserStatusDAO.selectByPrimaryKey(operateName);
					}
					//是否有接单权限
					if(user!=null){
						Map<String,Object> params = fillParam(audit, order, item, user.getOrgId());
						if(ordAuditAllocationDao.selectCount(params)<=0){
							user=null;
						}
					}
				}
				
			}
			if(user==null&&OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
				//查找同一个
				for(OrdOrderItem tmp:order.getOrderItemList()){
					if(!tmp.getOrderItemId().equals(item.getOrderItemId())&& isSameProduct(tmp,item)){
						OrdResponsible res = responsibleDao.getResponsibleByObject(tmp.getOrderItemId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
						if(res!=null){//这个地方需要考虑是否是按活动分配人来区分，是否需要考虑另一个分配的人是否在线
							Map<String,Object> params = fillParam(audit, order, item, res.getOrgId());
							boolean flag = ordAuditAllocationDao.selectCount(params)>0;
							if(flag){
								user=auditUserStatusDAO.selectByPrimaryKey(res.getOperatorName());
								break;
							}
						}
					}
				}
			}			
		}
		
		if(user==null){
			List<Long> orgList = findOperatorOrgIds(audit, item, order);
			
			if(CollectionUtils.isEmpty(orgList)){
				user = auditUserStatusDAO.getMinTaskCountRandomUserByOrgIds(null, Arrays.asList(344L));
				LOGGER.info("audit id:"+audit.getAuditId()+",type:"+audit.getAuditType()+"  don't found orgids,found in orgid:344,user=="+(user!=null));
			}else{
				if(responsibleFlag){
					user = auditUserStatusDAO.getRandomUserByOrgIds(audit.getObjectType(),orgList);
				}else{
					user = auditUserStatusDAO.getMinTaskCountRandomUserByOrgIds(audit.getAuditType(), orgList);
				}
			}
		}
		
		if(user==null){
			LOGGER.warn("audit 找不到对应的user分单 type:"+audit.getAuditType()+"  id:"+audit.getAuditId()+"  objectId:"+audit.getObjectId()+"    objectType:"+audit.getObjectType());
			return null;
		}
		Long customeOrgId=Constants.ORG_ID_CTI;
		boolean flag=permOrganizationServiceAdapter.hasParentOrgId(user.getOrgId(),customeOrgId);
		return orderAuditService.updateAuditAssign(audit, item, false, flag, user,true, false, false, null);
	}
	
	/**
	 * 新版订单负责人设置
	 * @param audit
	 * @param order
	 * @param item
	 * @param responsibleFlag
	 * @param csLocalUserId 
	 * @param isLocalVip 
	 * @return
	 */
	private ComAudit doAuditAssignNew(ComAudit audit,OrdOrder order, OrdOrderItem item,boolean responsibleFlag, boolean isCsVip, boolean isLocalVip, String csUserId) {
		LOGGER.info("OrderDistributionBusiness doAuditAssignNew audit id:"+audit.getAuditId()+",type:"+audit.getAuditType()+",orderId:" + order.getOrderId() + ",orderItemId:"+item.getOrderItemId()+",responsibleFlag:"+responsibleFlag+",isCsVip:"+isCsVip+",isLocalVip:"+isLocalVip);
		OrdAuditUserStatus  operatorUser = auditUserStatusDAO.selectByPrimaryKey(audit.getOperatorName());
		if(operatorUser == null){
			allocationLogMessageProducer.newAllocationLogMessage(audit, "订单处理人设置-找不到对应的可接单人:"+audit.getOperatorName());
			//如果分单失败的话 就修改下次分单时间  为当前时间的半小时之后
			Map<String,Object> map=new HashMap<String,Object>();
			Calendar nowTime = Calendar.getInstance();
			nowTime.add(Calendar.MINUTE, 30);
			map.put("nextAssignTime",nowTime.getTime());
			map.put("auditId",audit.getAuditId());
			orderAuditService.updateNextAssignTime(map);

			return null;
		}
		
		OrdAuditUserStatus  responseUser = null;//订单负责人
		boolean hasCustomeUser = false;
		if (responsibleFlag) {
			String responseName = null;// 主订单负责人
			OrdResponsible res = responsibleDao.getResponsibleByObject(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			responseName = allocationClientService.autoGetResponseName(audit, order, item , res);
			LOGGER.info("OrderDistributionBusiness doAuditAssignNew responseName:"+responseName+",orderId:" + order.getOrderId());
			if(!StringUtils.isEmpty(responseName)){
				hasCustomeUser = true;
				if(responseName.equals(audit.getOperatorName())){ // 和处理人一样则不需要再次查询
					responseUser = operatorUser;
				}else{
					responseUser = auditUserStatusDAO.selectByPrimaryKey(responseName);
				}
				LOGGER.info("OrderDistributionBusiness doAuditAssignNew responseUser:"+responseUser+",orderId:" + order.getOrderId());
				if(responseUser == null){
					allocationLogMessageProducer.newAllocationLogMessage(audit, "订单负责人设置-找不到对应的可接单人:" + responseName);
					hasCustomeUser = false;
				}
			}
		}
		
		String orderBu = order.getBuCode(); 
		String buType = allocationClientService.calComAuditBelongBu(order, audit);
		boolean isOrderAndItemBuEqual = true;
		if(orderBu!=null && buType!=null)
			isOrderAndItemBuEqual = orderBu.equals(buType);
		
		this.updateCacheCountByName(audit.getOperatorName(),audit);
		return orderAuditService.updateAuditAssign(audit, item, true, hasCustomeUser, responseUser, isOrderAndItemBuEqual, isCsVip,isLocalVip,csUserId);
	}

	/**
	 * 查询能分配活动的组织
	 * @param audit
	 * @param item
	 * @return
	 */
	private List<Long> findOperatorOrgIds(ComAudit audit, OrdOrderItem item, OrdOrder order) {
		return findOperatorOrgIds(audit,item,audit.getAuditType(),order);
	}
	
	/**
	 * 分销渠道
	 * @param order
	 * @return
	 */
	private String getDistributionChannel(OrdOrder order){
		return DistributionUtil.getDistributionChannel(order);
		
		/*// 分销商ID：neither根据DistributionChannel特殊处理  (1997) 
		Long distributionChannel = order.getDistributionChannel();
		if (distributionChannel != null) {
			if (ArrayUtils.contains(Configuration.distribution_neither.split(","), String.valueOf(distributionChannel))) {
				return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.neither.name();
			}
		}
		
		// 渠道Code
		String distributorCode = order.getDistributorCode();
		if (distributorCode != null) {
			if (ArrayUtils.contains(Configuration.distribution_taobao.split(","), distributorCode)) {
				return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.taobao.name();
			} else if (ArrayUtils.contains(Configuration.distribution_other.split(","), distributorCode)) {
				return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.other.name();
			}
		}
		return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.neither.name();*/
		
		/*Long distributionChannel = order.getDistributionChannel();
		if(distributionChannel != null){
			if(distributionChannel.longValue() == 106){
				return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.taobao.name();
			} else if (distributionChannel.longValue() != 106
					&& distributionChannel.longValue() != 10000
					&& distributionChannel.longValue() != 110
					&& distributionChannel.longValue() != 108
					&& distributionChannel.longValue() != 10001
					&& distributionChannel.longValue() != 10002) {
				return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.other.name();
			}
		}
		return OrderEnum.ORDER_DISTRIBUTION_CHANNEL.neither.name();*/
	}	
	
	/**
	 * 查询能分配活动的组织
	 * @param audit
	 * @param item
	 * @return
	 */
	private List<Long> findOperatorOrgIds(ComAudit audit, OrdOrderItem item, String functionCode, OrdOrder order) {
		Map<String,Object> params = fillParam(audit, order, item,null);
		params.put("functionCode", functionCode);
		
		//1.取到分单配置
		List<OrdAuditAllocation> list = ordAuditAllocationDao.selectList(params);
		if(CollectionUtils.isEmpty(list)){
			LOGGER.warn("audit 找不到对应的组织分单 type:"+functionCode+"  id:"+audit.getAuditId()+"      objectType"+audit.getObjectType()+"      objectId:"+audit.getObjectId());
			return null;
		}		
		List<Long> orgList = new ArrayList<Long>();
		for(OrdAuditAllocation aa:list){
			if(!orgList.contains(aa.getOrgId())){
				orgList.add(aa.getOrgId());
			}
		}
		return orgList;
	}
	
	private void initRuleMap(){
		if(businessRuleMap.isEmpty()){
			synchronized (businessRuleMap) {
				//2.取到业务规则
				ResultHandleT<List<BusinessRule>> businessRuleRH = businessRuleService.findBusinessRuleByAllValid();
				if(businessRuleRH.isSuccess()){
					List<BusinessRule> businessRuleList = businessRuleRH.getReturnContent();
					for(BusinessRule rule:businessRuleList){
						businessRuleMap.put(rule.getBusinessRuleId(), rule);
					}
				}
			}
		}
	}
	Map<Long,BusinessRule> businessRuleMap = new HashMap<Long, BusinessRule>();
	
	private OrdOrderItem getOrderItemByAudit(OrdOrder order,ComAudit audit){
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
			return order.getMainOrderItem();
		}else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
			if(order.getOrderItemList()==null){
				order = complexQueryService.queryOrderByOrderId(order.getOrderId());
			}
			for(OrdOrderItem item:order.getOrderItemList()){
				if(item.getOrderItemId().equals(audit.getObjectId())){
					return item;
				}
			}
		}
		return null;
	}
	
	@Override
	public ComAudit makeOrderAuditForDistribution(ComAudit audit) {
		if(OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(audit.getAuditStatus())
				|| OrderEnum.AUDIT_STATUS.UNPROCESSED.name().equals(audit.getAuditStatus())){
			audit.setAuditStatus(null);// 表示不更新状态
		}else{
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		}
		
		OrdAuditUserStatus  user = null;
		OrdOrder order = audit.getOrder();
		if(order == null){
			order = findOrderFromOrder(audit);
		}
		OrdOrderItem item = getOrderItemByAudit(order,audit);
		if(item == null){
			LOGGER.error("audit:"+audit.getAuditId()+" can not find ord_order_item");
			return null;
		}
		// 1、下单人并在线
		String operateName = order.getBackUserId();
		//是否在线
		if (StringUtils.isNotEmpty(operateName)) {
			user = auditUserStatusDAO.selectByPrimaryKey(operateName);
		}
		// 2、是否有接单权限
		if(user!=null){
			Map<String,Object> params = fillParam(audit, order, item, user.getOrgId());
			if(ordAuditAllocationDao.selectCount(params)<=0){
//				LOGGER.info("audit id:"+audit.getAuditId()+",type:"+audit.getAuditType()+"，该用户无接单权限"+user.getOperatorName());
				user = null;
			}
		}
		// 3、随机分配
		if(user == null){
			List<Long> orgList = findOperatorOrgIds(audit, item, order);
			if(CollectionUtils.isEmpty(orgList)){
//				user = auditUserStatusDAO.getMinTaskCountRandomUserByOrgIds(null, Arrays.asList(344L));
//				LOGGER.info("audit id:"+audit.getAuditId()+",type:"+audit.getAuditType()+"  don't found orgids,found in orgid:344,user=="+(user!=null));
				LOGGER.info("audit id:"+audit.getAuditId()+",type:"+audit.getAuditType()+"  don't found orgids.user=="+(user!=null));
			}else{
				user = auditUserStatusDAO.getRandomUserByOrgIds(audit.getObjectType(),orgList);
			}
		}
		if(user == null){
			LOGGER.warn("audit 找不到对应的user分单 type:"+audit.getAuditType()+"  id:"+audit.getAuditId()+"  objectId:"+audit.getObjectId()+"    objectType:"+audit.getObjectType());
			return null;
		}
//		Long customeOrgId = Constants.ORG_ID_CTI;
//		boolean flag = permOrganizationServiceAdapter.hasParentOrgId(user.getOrgId(),customeOrgId);
		this.updateCacheCountByName(user.getOperatorName(), audit);
		return orderAuditService.updateAuditAssign(audit, item, false, true, user, true, false, false, null);
	}

	@Override
	public String getDistributionChannelByAudit(ComAudit audit) {
		OrdOrder order = findOrderFromOrder(audit);
		if(order != null){
			audit.setOrder(order);// 设置好后面会有用
			return getDistributionChannel(order);
		}
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initRuleMap();
	}
	
	private boolean needVipOrder(OrdOrder order) {
		//添加sweet配置,实现配置控制国内vip定单是否分配给vip客服
		String isLocalVipAuditStatus = PropertiesUtil.getValue("local_vip_audit_status");
		if("false".equals(isLocalVipAuditStatus)){
			return false;
		}
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
				&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date myDate = dateFormat.parse("2017-08-29 04:00:00");
			if(order.getCreateTime().before(myDate)){
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId().longValue())){
				return true;
			}
			long productId=0l;
			if(null!=order.getOrdOrderPack()){
				productId=order.getOrdOrderPack().getProductId();
			}else{
				productId=order.getMainOrderItem().getProductId();
			}
			ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(productId);
			ProdProduct product = result.getReturnContent();
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId().longValue())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId().longValue())){
				if(PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(product.getProductType())){
					return true;
				}
			}
		}
		return false;
		
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
	
	/**
	 * @Description:缓存分单数量加1
	 * @param username 分配人
	 * @param audit 工单活动
	 * **/
	private void updateCacheCountByName(String username, ComAudit audit) {
		try {
			if(audit == null || username == null || audit.getObjectType() == null || "SYSTEM".equalsIgnoreCase(audit.getAuditFlag())) {
				return ;
			}
			String key = OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType()) ? "ALLOCATION_USER_ORDER_AUDIT_COUNT" : "ALLOCATION_USER_ORDERITEM_AUDIT_COUNT";
			if(jedisCluster.exists(key)) {
				jedisCluster.getJedisCluster().hincrBy(key, username, 1);
			}else {
				//缓存过期时间 凌晨过期
				long timestamp = DateUtil.getAfterDay(new Date()).getTime();
				jedisCluster.hset(key, username, "1");
				jedisCluster.getJedisCluster().pexpireAt(key, timestamp);
			}
		} catch (Exception e) {
			LOGGER.error("updateCacheCountByName error,auditId="+audit.getAuditId(), e);
		}
	}
}

