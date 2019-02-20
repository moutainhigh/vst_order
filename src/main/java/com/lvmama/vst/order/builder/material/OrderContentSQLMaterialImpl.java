package com.lvmama.vst.order.builder.material;

import org.apache.commons.collections.CollectionUtils;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.COURIER_STATUS;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.SecurityTool;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLMaterial;
import com.lvmama.vst.comm.vo.order.OrderContentParam;
import com.lvmama.vst.order.builder.IComplexQuerySQLConstant;
import com.lvmama.vst.order.builder.IComplexQuerySQLMaterial;

/**
 * 订单内容类SQL原材料
 * 
 * @author wenzhengtao
 * 
 */
public class OrderContentSQLMaterialImpl implements IComplexQuerySQLMaterial, IComplexQuerySQLConstant {

	private static OrderContentSQLMaterialImpl INSTANCE = null;
	
	private OrderContentSQLMaterialImpl(){
		
	}
	
	public static OrderContentSQLMaterialImpl getInstance(){
		if(null == INSTANCE){
			INSTANCE = new OrderContentSQLMaterialImpl();
		}
		return INSTANCE;
	}
	
	@Override
	public ComplexQuerySQLMaterial buildMaterial(final Object object, final ComplexQuerySQLMaterial material) {
		final OrderContentParam orderContentParam = (OrderContentParam) object;
		if (UtilityTool.isValid(orderContentParam.getProductName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			
			material.getTableSet().add(T_ORD_ORDER_PACK);
			// 产品名称
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_PACK.ORDER_ID(+)");
			material.getWhereSet().add("(ORD_ORDER_ITEM.PRODUCT_NAME LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getProductName()) + "%' or ORD_ORDER_PACK.PRODUCT_NAME LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getProductName()) + "%')");
		}
		
		if (UtilityTool.isValid(orderContentParam.getProductId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品编号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.PRODUCT_ID = " + orderContentParam.getProductId());
		}
		
		if (UtilityTool.isValid(orderContentParam.getBelongBU())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品编号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			
			//如果是国内，目的地合成的，传list,否则传string
			String buCode = orderContentParam.getBelongBU();
			String[] arr = buCode.split("\\|");
			if(arr.length > 1) {
				String inCause = "";
				for (int i=0; i< arr.length; i++) {
					if(i != 0) {
						inCause +=",";
					}
					inCause = inCause + "'" + arr[i] + "'";
				}
				material.getWhereSet().add("ORD_ORDER_ITEM.BU_CODE in (" + inCause + ")");
			} else {
				material.getWhereSet().add("ORD_ORDER_ITEM.BU_CODE = '" + orderContentParam.getBelongBU() + "'");
			}
		}
		if (UtilityTool.isValid(orderContentParam.getCourierStatus())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 快递处理状态
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			
			if (COURIER_STATUS.Y.name().equals(orderContentParam.getCourierStatus().trim())) {
				material.getWhereSet().add("ORD_ORDER_ITEM.COURIER_STATUS = '" + orderContentParam.getCourierStatus() + "'");
			} else if (COURIER_STATUS.N.name().equals(orderContentParam.getCourierStatus().trim())) {
				material.getWhereSet().add("( ORD_ORDER_ITEM.COURIER_STATUS = '" + orderContentParam.getCourierStatus() + "' or ORD_ORDER_ITEM.COURIER_STATUS is null )");
			}
			
		}
		
		if (UtilityTool.isValid(orderContentParam.getSuppGoodstId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 产品编号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.SUPP_GOODS_ID = " + orderContentParam.getSuppGoodstId());
		}
		
		
		if (UtilityTool.isValid(orderContentParam.getSupplierId())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 供应商编号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.SUPPLIER_ID = " + orderContentParam.getProductId());
		}
		
		if (UtilityTool.isValid(orderContentParam.getSuppGoodsName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			// 商品名称
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.SUPP_GOODS_NAME LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getSuppGoodsName()) + "%'");
		}
		
		if(UtilityTool.isValid(orderContentParam.getBackUserId())){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//下单人工号
			material.getWhereSet().add("ORD_ORDER.BACK_USER_ID = '"+SecurityTool.preventSqlInjection(orderContentParam.getBackUserId()) + "'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getBookerName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 驴妈妈账号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.BOOKER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.FULL_NAME = '" + SecurityTool.preventSqlInjection(orderContentParam.getBookerName()) + "'");
		}

		if (UtilityTool.isValid(orderContentParam.getBookerMobile())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 驴妈妈账号绑定的手机
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.BOOKER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.MOBILE LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getBookerMobile()) + "%'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getBookEmail())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 驴妈妈账号绑定的手机
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.BOOKER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.EMAIL LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getBookEmail()) + "%'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getTravellerName())&&!UtilityTool.isValid(orderContentParam.getContactName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 出游人姓名
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.FULL_NAME = '" + SecurityTool.preventSqlInjection(orderContentParam.getTravellerName()) + "'");
		}
		
		if (!UtilityTool.isValid(orderContentParam.getTravellerName())&&UtilityTool.isValid(orderContentParam.getContactName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 联系人姓名
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.CONTACT.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.FULL_NAME = '" + SecurityTool.preventSqlInjection(orderContentParam.getContactName()) + "'");
		}
		
		//出游人和联系人都填的情况
		if (UtilityTool.isValid(orderContentParam.getTravellerName())&&UtilityTool.isValid(orderContentParam.getContactName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			//绑定关联关系
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			// 出游人姓名\联系人姓名并集查询
			material.getWhereSet().add(
					" ( "+
					"ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.toString() + "'"+
					" AND "+
					"ORD_PERSON.FULL_NAME = '" + SecurityTool.preventSqlInjection(orderContentParam.getTravellerName()) + "'"+
					" OR "+
					"ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.CONTACT.toString() + "'"+
					" AND "+
					"ORD_PERSON.FULL_NAME = '" + SecurityTool.preventSqlInjection(orderContentParam.getContactName()) + "'"+
					" ) "
					);
		}
		
		if (UtilityTool.isValid(orderContentParam.getContactEmail())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 联系人邮箱
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.CONTACT.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.EMAIL LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getContactEmail()) + "%'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getContactPhone())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 联系人固话
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.CONTACT.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PHONE LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getContactPhone()) + "%'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getContactMobile())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 联系人手机
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" + OrderEnum.ORDER_PERSON_TYPE.CONTACT.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.MOBILE LIKE '" + SecurityTool.preventSqlInjection(orderContentParam.getContactMobile()) + "%'");
		}
		
		if (UtilityTool.isValid(orderContentParam.getMobile()) && UtilityTool.isValid(orderContentParam.getPersoType())  ) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			// 联系人手机
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" +orderContentParam.getPersoType()+ "'");
			material.getWhereSet().add("ORD_PERSON.MOBILE = '" + SecurityTool.preventSqlInjection(orderContentParam.getMobile()) + "'");
		}
		
		/*if ( UtilityTool.isValid(orderContentParam.getPersoType())  ) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" +OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name()+ "'");
		}*/
		
		if ( null != orderContentParam.getOrderIds()&& UtilityTool.isValid(orderContentParam.getPersoType())  ) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单人表
			material.getTableSet().add(T_ORD_PERSON);
			
			// 多个订单ID
			StringBuilder builder = new StringBuilder();
			for (Long orderId : orderContentParam.getOrderIds()) {
				builder.append(orderId);
				builder.append(",");
			}
			if (builder.length()>0) {
				//去掉最后一个逗号
				builder.setLength(builder.length() - 1);
				material.getWhereSet().add("ORD_ORDER.ORDER_ID IN " + K_LEFT_PARENTHESIS + builder.toString() + K_RIGHT_PARENTHESIS);
			}
			
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_PERSON.OBJECT_ID");
			material.getWhereSet().add("ORD_PERSON.OBJECT_TYPE = '" + OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.toString() + "'");
			material.getWhereSet().add("ORD_PERSON.PERSON_TYPE = '" +orderContentParam.getPersoType()+ "'");
		}
		
		
		if (CollectionUtils.isNotEmpty(orderContentParam.getFilialeNames())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 子公司名称
			if (1 == orderContentParam.getFilialeNames().size()) {
				//一个
				material.getWhereSet().add("ORD_ORDER.FILIALE_NAME = '" + orderContentParam.getFilialeNames().get(0)+"' ");
			} else {
				//多个
				StringBuilder builder = new StringBuilder();
				for (String filialeName : orderContentParam.getFilialeNames()) {
					builder.append("'"+filialeName+"'");
					builder.append(",");
				}
				//去掉最后一个逗号
				builder.setLength(builder.length() - 1);
				material.getWhereSet().add("ORD_ORDER.FILIALE_NAME IN " + K_LEFT_PARENTHESIS + builder.toString() + K_RIGHT_PARENTHESIS);
			}
		}
		
		if (UtilityTool.isValid(orderContentParam.getOperatorName())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单审核活动表
			material.getTableSet().add(T_COM_AUDIT);
			// 处理人
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = COM_AUDIT.OBJECT_ID");
			material.getWhereSet().add("COM_AUDIT.OBJECT_TYPE = '" + OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString() + "'");
			if(orderContentParam.getOperatorName().split(",").length>1){
				material.getWhereSet().add("COM_AUDIT.OPERATOR_NAME IN (" + orderContentParam.getOperatorName() + ")");
			}else{
				material.getWhereSet().add("COM_AUDIT.OPERATOR_NAME = '" + orderContentParam.getOperatorName() + "'");
			}
		}
		
		if(UtilityTool.isValid(orderContentParam.getUserId())){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//前台下单人，这里必须为精确查询，否则把别人的订单给查出来了
			material.getWhereSet().add("ORD_ORDER.USER_ID = '"+SecurityTool.preventSqlInjection(orderContentParam.getUserId()) + "'");
		}
		
		if(UtilityTool.isValid(orderContentParam.getUserNo())){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//前台下单人,11位userNo
			material.getWhereSet().add("ORD_ORDER.USER_NO = "+orderContentParam.getUserNo());
		}
		
		if(UtilityTool.isValid(orderContentParam.getPayTarget())){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			//支付方式
			material.getWhereSet().add("ORD_ORDER.PAYMENT_TARGET = '"+SecurityTool.preventSqlInjection(orderContentParam.getPayTarget()) + "'");
		}
		
		if(UtilityTool.isValid(orderContentParam.getStatusType())){
			//订单表
			material.getTableSet().add(T_ORD_ORDER);
			
			material.getTableSet().add(T_ORD_ADDITION_STATUS);
			//出团通知书状态类型
			material.getWhereSet().add("ORD_ORDER.ORDER_ID =ORD_ADDITION_STATUS.ORDER_ID AND ORD_ADDITION_STATUS.STATUS_TYPE='"+SecurityTool.preventSqlInjection(orderContentParam.getStatusType()) + "'");
		}
		
		if(UtilityTool.isValid(orderContentParam.getNeedInvoice())){
			final String[] array = orderContentParam.getNeedInvoice().split(",");			
			material.getTableSet().add(T_ORD_ORDER);
			if(array.length==1){
				material.getWhereSet().add(
						"ORD_ORDER.NEED_INVOICE = '"
								+ SecurityTool.preventSqlInjection(orderContentParam
										.getNeedInvoice()) + "'");
			} else {
				StringBuffer sb = new StringBuffer();

				for (String str : array) {
					sb.append("'");
					sb.append(SecurityTool.preventSqlInjection(str));
					sb.append("',");					
				}
				sb.setLength(sb.length()-1);
				material.getWhereSet().add("ORD_ORDER.NEED_INVOICE in("+sb.toString()+")");
			}
		}
		
		if (UtilityTool.isValid(orderContentParam.getPackageType())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			// 订单子项表
			material.getTableSet().add(T_ORD_ORDER_PACK);
			// 产品编号
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_PACK.ORDER_ID");
			
			String ownPack="";
			if (ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(orderContentParam.getPackageType())) {
				ownPack="true";
			}else{
				ownPack="false";
			}
			material.getWhereSet().add("ORD_ORDER_PACK.OWN_PACK = " + ownPack);
		}
		
		if (UtilityTool.isValid(orderContentParam.getExpressType())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_ORD_COURIER_LISTING);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_COURIER_LISTING.ORDER_ID");
			
			
			material.getWhereSet().add("ORD_COURIER_LISTING.EXPRESS_TYPE in (" + orderContentParam.getExpressType()+")");
		}
		if (UtilityTool.isValid(orderContentParam.getResponsiblePerson())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			material.getTableSet().add(T_ORD_RESPONSIBLE);			
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			material.getWhereSet().add("ORD_ORDER_ITEM.ORDER_ITEM_ID = ORD_RESPONSIBLE.OBJECT_ID");
			material.getWhereSet().add("ORD_RESPONSIBLE.OBJECT_TYPE = '"  + OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name() + "'");
			material.getWhereSet().add("ORD_RESPONSIBLE.OPERATOR_NAME = '"  + orderContentParam.getResponsiblePerson() + "'");
		}	
		// 房型类型
		if (UtilityTool.isValid(orderContentParam.getStockFlag())) {
			// 订单表
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			// 是否保留房
			material.getWhereSet().add(
					"ORD_ORDER_ITEM.STOCK_FLAG = '"
							+ SecurityTool
									.preventSqlInjection(orderContentParam
											.getStockFlag()) + "'");
		}
		//O2O门店系统 产品类型
		if (UtilityTool.isValid(orderContentParam.getProducType())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getWhereSet().add("ORD_ORDER.CATEGORY_ID in ("+SecurityTool.preventSqlInjection(orderContentParam.getProducType()) + ")");
		}
		//O2O门店系统 子 公司，分社，门店
		if (UtilityTool.isValid(orderContentParam.getO2oSubCompanyId())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.SUB_COMPANY_ID ="
							+ Long.valueOf(SecurityTool.preventSqlInjection(orderContentParam.getO2oSubCompanyId())));
		}
		if (UtilityTool.isValid(orderContentParam.getO2oBranchOfficeId())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.BRANCH_OFFICE_ID ="
							+ Long.valueOf(SecurityTool.preventSqlInjection(orderContentParam.getO2oBranchOfficeId())));
		}
		if (UtilityTool.isValid(orderContentParam.getO2oStoreId())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.STORE_ID ="
							+ Long.valueOf(SecurityTool.preventSqlInjection(orderContentParam.getO2oStoreId())));
		}
		//o2o子公司禁售产品的订单排除
		if (UtilityTool.isValid(orderContentParam.getSubCommissionIds())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.SUB_COMMISSION_ID NOT IN ("
							+ SecurityTool.preventSqlInjection(orderContentParam.getSubCommissionIds())+")");
		}
		//o2o分社禁售产品的订单排除
		if (UtilityTool.isValid(orderContentParam.getBranchCommissionIds())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.BRANCH_COMMISSION_ID NOT IN ("
							+ SecurityTool.preventSqlInjection(orderContentParam.getBranchCommissionIds())+")");
		}
		//o2o门店禁售产品的订单排除
		if (UtilityTool.isValid(orderContentParam.getStoreCommissionIds())) {
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_O2O_ORDER);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = O2O_ORDER.ORDER_ID");
			material.getWhereSet().add("O2O_ORDER.STORE_COMMISSION_ID NOT IN ("
							+ SecurityTool.preventSqlInjection(orderContentParam.getStoreCommissionIds())+")");
		}
		
		
		if(UtilityTool.isValid(orderContentParam.getPriceConfirmStatus())){
			material.getTableSet().add(T_ORD_ORDER);
			material.getTableSet().add(T_ORD_ORDER_ITEM);
			material.getWhereSet().add("ORD_ORDER.ORDER_ID = ORD_ORDER_ITEM.ORDER_ID");
			if(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode().equalsIgnoreCase(orderContentParam.getPriceConfirmStatus())) {
				material.getWhereSet().add("(ORD_ORDER_ITEM.PRICE_CONFIRM_STATUS = '" + orderContentParam.getPriceConfirmStatus() + "' OR ORD_ORDER_ITEM.PRICE_CONFIRM_STATUS IS NULL)");
			}else{
				material.getWhereSet().add("ORD_ORDER_ITEM.PRICE_CONFIRM_STATUS = '" + orderContentParam.getPriceConfirmStatus() + "'");
			}

		}

		return material;
	}

}
