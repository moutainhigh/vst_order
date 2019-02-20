/**
 * 
 */
package com.lvmama.vst.order.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.goods.po.SuppGoods.EXPRESSTYPE;
import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.pu.CodeItem;

/**
 * 发票相关操作工具类
 * @author yangbin
 *
 */
public abstract class InvoiceUtil {
	/**
	 * 根据结算主体(上海景域文化传播有限公司)对订单类型分类:门票,酒店,自由行.
	 */
	public static final String COMPANY_1 = Constant.INVOICE_COMPANY.COMPANY_1.name();
	/**
	 * 根据结算主体(上海驴妈妈国际旅行社有限公司)对订单类型分类:国内游.
	 */
	public static final String COMPANY_2 = Constant.INVOICE_COMPANY.COMPANY_2.name();
	/**
	 * 根据结算主体(兴旅)对订单类型分类:出境游.
	 */
	public static final String COMPANY_3 = Constant.INVOICE_COMPANY.COMPANY_3.name();
	
	/**
	 * 默认分隔符:",".
	 */
	private static final String DEFAULT_SEPARATOR = ",";
	

	/**
	 * 检查一个发票是否不修改到开票
	 * @param ordInvoice
	 * @return true不能修改
	 */
	public static boolean checkChangeBillAble(OrdInvoice ordInvoice){
		if (ordInvoice == null){
			return true;
		}
		
		return ArrayUtils.contains(UN_BILLED, ordInvoice.getStatus());
	}
	
	/**
	 * 能否审核
	 * @param ordInvoice
	 * @return true不能审核
	 */
	public static boolean checkChangeApproveAble(OrdInvoice ordInvoice){
		if (ordInvoice == null){
			return true;
		}
		
		return !ordInvoice.getStatus().equals(Constant.INVOICE_STATUS.UNBILL.name());
	}
	
	
	/**
	 * 能否修改发票号
	 * @param ordInvoice
	 * @return true不可以修改
	 */
	public static boolean checkChangeInvoiceNo(OrdInvoice ordInvoice){
		return ordInvoice == null
				|| (ordInvoice.getStatus().equals(
						Constant.INVOICE_STATUS.CANCEL.name()))
				|| (ordInvoice.getStatus().equals(
						Constant.INVOICE_STATUS.COMPLETE.name())
				||(ordInvoice.getStatus().equals(Constant.INVOICE_STATUS.RED.name())));
	}
	
	public static boolean checkChangeExpressNo(OrdInvoice ordInvoice){
		return ordInvoice==null||!ordInvoice.getStatus().equals(Constant.INVOICE_STATUS.BILLED.name());
	}
	private static String[] cancel_status_list={Constant.INVOICE_STATUS.UNBILL.name(),Constant.INVOICE_STATUS.APPROVE.name()};
	/**
	 * 
	 * @param ordInvoice
	 * @return true不能取消
	 */
	public static boolean checkUserCancelAble(OrdInvoice ordInvoice){
		return ordInvoice==null||!ArrayUtils.contains(cancel_status_list,ordInvoice.getStatus());
	}
	
	/**
	 * 不可以变更到开发票的状态的当前状态
	 */
	private static String UN_BILLED[] = {
			Constant.INVOICE_STATUS.CANCEL.name(),
			Constant.INVOICE_STATUS.COMPLETE.name(),
			Constant.INVOICE_STATUS.BILLED.name() };
	
	
	/**
	 * 分公司对应的线路产品全部转到兴旅	
	 */
	private static String company_3_type_route[]={
		Constant.SUB_PRODUCT_TYPE.GROUP.name(),
		Constant.SUB_PRODUCT_TYPE.GROUP_LONG.name(),
		Constant.SUB_PRODUCT_TYPE.SELFHELP_BUS.name(),
		Constant.SUB_PRODUCT_TYPE.FREENESS_LONG.name()
	};

	/**
	 * 按不同公司返回对应的发票产品类型
	 * @param company
	 * @return
	 */
	public static String[] getProductTypeByCompany(String company){
		if(StringUtils.isEmpty(company)){
			return new String[0];
		}
		if(company.equals(COMPANY_1)){
			return company_1_type;
		}else if(company.equals(COMPANY_2)){
			return company_2_type;
		}else if(company.equals(COMPANY_3)){
			return company_3_type;
		}
		return new String[0];
	}
 
	/**
	 * 根据不同结算主体标识返回相应的订单类型集合,订单类型集合以","号分隔拼接成字符串.
	 * @param company 结算主体标识 取值为:COMPANY_1,COMPANY_2,COMPANY_3其中之一.
	 * @return 返回订单类型以","分隔所拼接而成的字符串.
	 */
	public static String getProductTypeStringByCompany(String company) {
		String[] array = getProductTypeByCompany(company);
		StringBuilder sb = new StringBuilder();
		for (String ele : array) {
			sb.append(ele);
			sb.append(DEFAULT_SEPARATOR);
		}
		if (array.length > 0) {
			return sb.substring(0, sb.length() - 1);
		}
		return sb.toString();
	}
	
 
	
//	/**
//	 * 按订单类型返回指定的code_type值
//	 * @param orderType
//	 * @return 不存在返回空值
//	 */
	public static Constant.CODE_TYPE getInvoiceDetail(String orderCoteType){
		String type = orderCoteType.toUpperCase();
		if(ArrayUtils.contains(company_1_type, type)){
			return Constant.CODE_TYPE.COMPANY_1_CONTENT;
			
		}else if(ArrayUtils.contains(company_2_type, type)){
			return Constant.CODE_TYPE.COMPANY_2_CONTENT;
			
		}else if(ArrayUtils.contains(company_3_type, type)){
			return Constant.CODE_TYPE.COMPANY_3_CONTENT;
			
		}else if(ArrayUtils.contains(company_4_type, type)){
			return Constant.CODE_TYPE.COMPANY_4_CONTENT;
			
		}else if(ArrayUtils.contains(company_5_type, type)){
			return Constant.CODE_TYPE.COMPANY_5_CONTENT;
		}else{
			return null;
		}
	}

	
	
	private static String company_1_type[] = {
		Constant.VST_CATEGORY.CATEGORY_HOTEL.name()};

	private static String company_2_type[] = {
		Constant.VST_CATEGORY.CATEGORY_TICKET.name(),
		Constant.VST_CATEGORY.CATEGORY_SINGLE_TICKET.name(),
		Constant.VST_CATEGORY.CATEGORY_COMB_TICKET.name(),
		Constant.VST_CATEGORY.CATEGORY_OTHER_TICKET.name(),
		Constant.VST_CATEGORY.CATEGORY_SHOW_TICKET.name(),};

	private static String company_3_type[] = {
		Constant.VST_CATEGORY.CATEGORY_ROUTE.name(),
		Constant.VST_CATEGORY.CATEGORY_ROUTE_GROUP.name(),
		Constant.VST_CATEGORY.CATEGORY_ROUTE_LOCAL.name(),
		Constant.VST_CATEGORY.CATEGORY_TRANFFIC_BUS.name(),
		Constant.VST_CATEGORY.CATEGORY_TRANFFIC_BUS_OTHER.name(),
		Constant.VST_CATEGORY.CATEGORY_CRUISE.name(),
		Constant.VST_CATEGORY.CATEGORY_COMB_CRUISE.name(),
		Constant.VST_CATEGORY.CATEGORY_CRUISE_ADDITION.name(),
	};
	
	private static String company_4_type[] = {
		Constant.VST_CATEGORY.CATEGORY_ROUTE_FREEDOM.name(),
		Constant.VST_CATEGORY.CATEGORY_ROUTE_HOTELCOMB.name(),
	};
	
	private static String company_5_type[] = {
		Constant.VST_CATEGORY.CATEGORY_VISA.name(),
	};
	
	
//	private static String company_1_type[] = {
//		Constant.PRODUCT_TYPE.TICKET.name(),
//		Constant.PRODUCT_TYPE.HOTEL.name(),
//		Constant.SUB_PRODUCT_TYPE.FREENESS.name() };
//
//	private static String company_2_type[] = {
//			Constant.SUB_PRODUCT_TYPE.GROUP.name(),
//			Constant.SUB_PRODUCT_TYPE.GROUP_LONG.name(),
//			Constant.SUB_PRODUCT_TYPE.SELFHELP_BUS.name(),
//			Constant.PRODUCT_TYPE.OTHER.name(),
//			Constant.SUB_PRODUCT_TYPE.FREENESS_LONG.name() };
//
//	private static String company_3_type[] = {
//			Constant.SUB_PRODUCT_TYPE.FREENESS_FOREIGN.name(),
//			Constant.SUB_PRODUCT_TYPE.GROUP_FOREIGN.name() };
	
	
	
	
	private static String filiale_list[]={
		Constant.FILIALE_NAME.BJ_FILIALE.name(),
		Constant.FILIALE_NAME.CD_FILIALE.name(),
		Constant.FILIALE_NAME.GZ_FILIALE.name(),
		Constant.FILIALE_NAME.HZ_FILIALE.name()
	};
	
	/**
	 * 按订单的类型取发票的主体
	 * @param orderType
	 * @return
	 */
	public static Constant.INVOICE_COMPANY getInvoiceCompany(String orderType){
		if(ArrayUtils.contains(company_1_type, orderType)){
			return Constant.INVOICE_COMPANY.COMPANY_1;
		}else if(ArrayUtils.contains(company_2_type, orderType)){
			return Constant.INVOICE_COMPANY.COMPANY_2;
		}else if(ArrayUtils.contains(company_3_type, orderType)){
			return Constant.INVOICE_COMPANY.COMPANY_3;
		}
		return null;
	}
	
//	public static Constant.INVOICE_COMPANY getInvoiceCompany(OrdOrder order){
//		if(ArrayUtils.contains(filiale_list, order.getFilialeName())&&ArrayUtils.contains(company_3_type_route, order.getOrderType())){
//			return Constant.INVOICE_COMPANY.COMPANY_3;
//		}else{
//			return getInvoiceCompany(order.getOrderType());
//		}
//			
//	}
 
	/**
	 * 取发票内容对应的中文类型
	 * @param str
	 * @return
	 */
	public static String getZhInvoiceContent(String str){
		String array[]=StringUtils.split(str,",");
		StringBuffer sb=new StringBuffer();
		if(!ArrayUtils.isEmpty(array)){
			for(String tmp:array){
				sb.append(Constant.INVOICE_CONTENT.getCnName(tmp));
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}
	
	// 销售渠道
		public static enum INVOICE_CONTENT {
			//门票
			SERVICE_CHARGE("服务费"),
			TRAVEL_CHARGE("旅游费"),
			TICKED_PROXY_CHARGE("门票代理费"),
			
			//酒店			
			REGISTER_PROXY_CHARGE("住宿代理费"),
			
			REGISTER_BOOK_CHARGE("代订住宿费"),
			VISA_SERVICE_CHARGE("服务费-签证"),
			TICKET_SERVICE_CHARGE("门票服务费"),
			TICKET_HOTEL_BOOK_CHARGE("代办费（机票+酒店）"),

			//线路
			GROUP_CHARGE("团费"),//旅游费
			
			INTEGRATION_CHARGE("综合服务费"),
			TICKET_HOTEL_PROXY_CHARGE("代理费（门票+酒店）"),
			TICKET_HOTEL_TRAVEL_CHARGE("旅游费（门票+酒店）"),
			
			//电话wifi
			TRAVEL_SERVICE_CHARGE("旅游服务费");

			private String cnName;

			public static String getCnName(String code) {
				for (EXPRESSTYPE item : EXPRESSTYPE.values()) {
					if (item.getCode().equals(code)) {
						return item.getCnName();
					}
				}
				return code;
			}

			INVOICE_CONTENT(String name) {
				this.cnName = name;
			}

			public String getCode() {
				return this.name();
			}

			public String getCnName() {
				return this.cnName;
			}

			@Override
			public String toString() {
				return this.name();
			}
		}
		public static List<Object> getInvoiceContene() {
	        List<Object> list =  new ArrayList<Object>();
			for (INVOICE_CONTENT content : INVOICE_CONTENT.values()) {
				if (list != null){
					list.add(content);
				}
			}
			return list;
		}
	
//	private static enum TYPE{
//		C1,//团费、旅游费、会务费(单独成团，且包含会务的行程)
//		C2,//代理费（门票+酒店）
//		C3,//门票代理费,旅游费,服务费
//		C4,//住宿代理费
//		C5,//服务费
//		C6//团费/旅游费/服务费
//	}
		
		public static List<Object> getInvoiceContents(BizCategory bizCategory) {
			List<Object> hotelLlist = new ArrayList<Object>();//酒店
			List<Object> ticketLlist = new ArrayList<Object>();//景点门票，门票，组合套餐票，其它票
			List<Object> visaLlist = new ArrayList<Object>();//签证
			List<Object> busLlist = new ArrayList<Object>();//游轮、邮轮组合产品，邮轮附加项，跟团游，当地游，其它巴士，巴士
			List<Object> freeWalkerList = new ArrayList<Object>();//自由行、酒店套餐
			List<Object> wifiList = new ArrayList<Object>();//电话WIFI
//			List<Object> routeAeroList = new ArrayList<Object>();//交通+X
			
			Long categoryId = bizCategory.getCategoryId();
			if(categoryId == 1){
				hotelLlist.add(INVOICE_CONTENT.REGISTER_PROXY_CHARGE.getCnName());
				hotelLlist.add(INVOICE_CONTENT.REGISTER_BOOK_CHARGE.getCnName());
				return hotelLlist;
			}else if(categoryId == 11 || categoryId == 5 || categoryId==13 || categoryId == 12){
				ticketLlist.add(INVOICE_CONTENT.TICKET_SERVICE_CHARGE.getCnName());
				ticketLlist.add(INVOICE_CONTENT.TRAVEL_CHARGE.getCnName());
				return ticketLlist;
			}else if(categoryId == 4){
				visaLlist.add(INVOICE_CONTENT.VISA_SERVICE_CHARGE.getCnName());
				return visaLlist;
			}else if(categoryId == 2 || categoryId == 8 || categoryId == 10 
					|| categoryId == 14 || categoryId == 15 || 
							categoryId == 16 || categoryId == 24 || categoryId == 25 || categoryId == 42){
				busLlist.add(INVOICE_CONTENT.GROUP_CHARGE.getCnName());
				busLlist.add(INVOICE_CONTENT.TRAVEL_CHARGE.getCnName());
				return busLlist;
			}else if(categoryId == 18 || categoryId == 17){
//				freeWalkerList.add(INVOICE_CONTENT.GROUP_CHARGE.getCnName());
				freeWalkerList.add(INVOICE_CONTENT.TRAVEL_CHARGE.getCnName());
				freeWalkerList.add(INVOICE_CONTENT.TICKET_HOTEL_BOOK_CHARGE.getCnName());
				freeWalkerList.add(INVOICE_CONTENT.TICKET_HOTEL_TRAVEL_CHARGE.getCnName());
				return freeWalkerList;
			}else if(categoryId == 28){
				wifiList.add(INVOICE_CONTENT.TRAVEL_SERVICE_CHARGE.getCnName());
				return wifiList;
			}/*else if(categoryId == 29){
				routeAeroList.add(INVOICE_CONTENT.TRAVEL_CHARGE.getCnName());
				return routeAeroList;
			}*/
			return null;
		}
		
		private static enum TYPE{
			C1,//酒店：住宿代理费
			C2,//门票：门票代理费,旅游费,服务费
			C3,//线路：团费，旅游费
			C4,//大交通：团费，旅游费
			C5,//签证：服务费
			C6//游轮，附加项目，组合产品：团费，旅游费
		}
		
	
//	private static OrderInvoiceContent _instance = null;
//
//	public static List<CodeItem> getInvoiceContents(String orderType,
//			boolean blank) {
//		if (_instance == null) {
//			_instance = new OrderInvoiceContent();
//			_instance.init();
//		}
//		return _instance.getList(orderType);
//	}
	
//	public static List<Object> getInvoiceContene() {
//        List<Object> list =  new ArrayList<Object>();
//		for (INVOICE_CONTENT content : INVOICE_CONTENT.values()) {
//			if (list != null){
//				list.add(content);
//			}
//		}
//		return list;
//	}

	static class OrderInvoiceContent {
		// 相同的内容的订单类型
		private Map<String, TYPE> orderTypeMap = new HashMap<String, TYPE>();

		// 类型对应的内容列表
		Map<TYPE, List<CodeItem>> map = new HashMap<TYPE, List<CodeItem>>();

		public List<CodeItem> getList(String orderType) {
			TYPE type = orderTypeMap.get(orderType);
			if (type == null) {
				throw new IllegalArgumentException("订单类型不存在开票内容");
			}
			return map.get(type);
		}

		/**
		 * 初始化订单类型对应的发票内容
		 */
		synchronized void init() {
			
			initInvoiceContent(TYPE.C1, INVOICE_CONTENT.REGISTER_PROXY_CHARGE);
			initInvoiceContent(TYPE.C2,INVOICE_CONTENT.TICKED_PROXY_CHARGE,INVOICE_CONTENT.SERVICE_CHARGE,INVOICE_CONTENT.TRAVEL_CHARGE);
			initInvoiceContent(TYPE.C3, INVOICE_CONTENT.GROUP_CHARGE,INVOICE_CONTENT.TRAVEL_CHARGE);
			initInvoiceContent(TYPE.C4, INVOICE_CONTENT.GROUP_CHARGE,INVOICE_CONTENT.TRAVEL_CHARGE);
			initInvoiceContent(TYPE.C5, INVOICE_CONTENT.SERVICE_CHARGE);
			initInvoiceContent(TYPE.C6, INVOICE_CONTENT.GROUP_CHARGE,INVOICE_CONTENT.TRAVEL_CHARGE);

			//出境跟团游
			orderTypeMap.put(Constant.SUB_PRODUCT_TYPE.GROUP_FOREIGN.name(),TYPE.C1);
			//出境自由行
			orderTypeMap.put(Constant.SUB_PRODUCT_TYPE.FREENESS_FOREIGN.name(),TYPE.C6);		
			
			orderTypeMap.put(Constant.PRODUCT_TYPE.TICKET.name(), TYPE.C2);
			orderTypeMap.put(Constant.PRODUCT_TYPE.HOTEL.name(),TYPE.C1);
            orderTypeMap.put(Constant.PRODUCT_TYPE.ROUTE.name(), TYPE.C3);
			orderTypeMap.put(Constant.PRODUCT_TYPE.TRAFFIC.name(),TYPE.C4);
			orderTypeMap.put(Constant.SUB_PRODUCT_TYPE.VISA.name(), TYPE.C5);

//			orderTypeMap.put(Constant.SUB_PRODUCT_TYPE.FREENESS.name(), TYPE.C2);
			
			
		}

		private void initInvoiceContent(TYPE type,INVOICE_CONTENT...contents){
			List<CodeItem> list=new ArrayList<CodeItem>();
			for(INVOICE_CONTENT c:contents){
				list.add(new CodeItem(c.name(), Constant.INVOICE_CONTENT.getCnName(c.name())));
			}
			map.put(type, list);
		}
	}
	
	public static List<String> getInvoiceLogistics(){
		List<String> invoiceLogisticsList = new ArrayList<String>();
		for(int i=0;i<Constant.INVOICE_LOGISTICS.values().length;i++){
			invoiceLogisticsList.add(Constant.INVOICE_LOGISTICS.values()[i].getCnName());
		}
		return invoiceLogisticsList;
	}
	
	public static List<String> getFilialeNameList(){
		List<String> filialeNameList = new ArrayList<String>();
		for(int i=0;i<Constant.FILIALE_NAME.values().length;i++){
			filialeNameList.add(Constant.FILIALE_NAME.values()[i].getCnName());
		}
		return filialeNameList;
	}
	
	public static List<String> getInvoiceStatusList(){
		List<String> invoiceStatusList = new ArrayList<String>();
		for(int i=0;i<Constant.INVOICE_STATUS.values().length;i++){
			invoiceStatusList.add(Constant.INVOICE_STATUS.values()[i].getCnName());
		}
		return invoiceStatusList;
	}
	
	public static List<String> getDeliveryTypeList(){
		List<String> deliveryTypeList = new ArrayList<String>();
		for(int i=0;i<Constant.DELIVERY_TYPE.values().length;i++){
			deliveryTypeList.add(Constant.DELIVERY_TYPE.values()[i].getCnName());
		}
		return deliveryTypeList;
	}
	
	public static List<String> getInvoiceCompanyList(){
		List<String> invoiceCompanyList = new ArrayList<String>();
		for(int i=0;i<Constant.INVOICE_COMPANY.values().length;i++){
			invoiceCompanyList.add(Constant.INVOICE_COMPANY.values()[i].getCnName());
		}
		return invoiceCompanyList;
	}
}

