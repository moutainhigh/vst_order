package com.lvmama.vst.order.job;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.lvmama.comm.TaskServiceInterface;
import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.pub.TaskResult;
import com.lvmama.comm.pet.vo.EmailAttachmentData;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.log.util.LogTrackContext;
import com.lvmama.scenic.api.back.biz.po.BizEnum;
import com.lvmama.scenic.api.back.goods.po.ScenicSuppGoods.GOODSSPEC;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdComplexSqlDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.utils.CSVUtils;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import com.lvmama.vst.ticket.utils.DisneyUtils;

public class MailTicketOrderDataProcessJobService implements TaskServiceInterface,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1189533147782685105L;
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger LOG= LoggerFactory
			.getLogger(MailTicketOrderDataProcessJobService.class);
	
	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;
	@Autowired
	private IOrdOrderService iOrdOrderService;
	@Autowired
	private OrdComplexSqlDao ordComplexSqlDao;
	@Autowired
	private IComplexQueryService complexQueryService;
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	@Autowired
	private FSClient vstFSClient;
	
	@SuppressWarnings("unchecked")
	@Override
	public TaskResult execute(Long logId, String parameter) throws Exception {
		LogTrackContext.initTrackNumber();
		LOG.info("MailTicketOrderDataProcessJobService Begin .........parameter:"+parameter);
		TaskResult taskResult = new TaskResult();
		Parameter param = null;
		try{
			param = checkParam(parameter);
		}catch(Exception e){
			LOG.error("MailTicketOrderDataProcessJobService 参数校验不通过："+e.getMessage());
		}
        Map<String, Object> condition = new HashMap<String, Object>();
  		condition.put("paymentStutus", param.getPaymentStutus());
  		condition.put("infoStatus", param.getInfoStatus());
  		condition.put("resourceStatus", param.getResourceStatus());
        condition.put("managerIds", param.getManagerIds());
        
        Calendar calendar=Calendar.getInstance();
		Date date = new Date();//获取当前时间  
		SimpleDateFormat formatter= new SimpleDateFormat ("yyyy-MM-dd"); 
        int beforDaysOfCreateTime = param.getBeforDaysOfCreateTime();
        calendar.setTime(date);    
	    calendar.add(Calendar.DAY_OF_MONTH, -beforDaysOfCreateTime);//当前时间减去天数
        condition.put("startCreateTime", formatter.format(calendar.getTime())+" 00:00:00");
        condition.put("endCreateTime", formatter.format(date)+" 23:59:59");
		
		String startPaymentTime = param.getStartPaymentTime();
        if(StringUtils.isNotEmpty(startPaymentTime) && startPaymentTime.startsWith("-")){
        	calendar.setTime(date);    
   	     	calendar.add(Calendar.DAY_OF_MONTH, -1);//当前时间减去一天    
        	condition.put("startPaymentTime", formatter.format(calendar.getTime())+" "+startPaymentTime.substring(1, startPaymentTime.length()));
        }else{
        	condition.put("startPaymentTime", formatter.format(date)+" "+startPaymentTime);
        }
        condition.put("endPaymentTime", formatter.format(date)+" "+param.getEndPaymentTime());
        condition.put("buCodes", param.getBuCodes());
		List<Result> resultList = this.queryDataProcess(condition);
    	taskResult.setRunStatus(TaskResult.RUN_STATUS.SUCCESS);
    	taskResult.setResult("共" + resultList.size() + "条数据");
        LOG.info("MailTicketOrderDataProcessJobService End .........parameter:"+parameter);
		return taskResult;
	}
	
	/**
	 * 校验参数
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	private Parameter checkParam(String parameter) throws Exception{
		Parameter param = null;
		parameter = parameter.trim();
		if (parameter == null) {
        	throw new Exception("参数不能为空");
        }
        try {
			param = objectMapper.readValue(parameter, Parameter.class);
		} catch (Exception e1) {
			throw new Exception("参数格式错误");
		}
        /*if(param.getStartCreateTime() == null){
        	throw new Exception("startCreateTime订单创建起始时间不能为空");
        }
        if(param.getEndCreateTime() == null){
        	throw new Exception("endCreateTime订单创建结束时间不能为空");
        }*/
        if(param.getStartPaymentTime() == null){
        	throw new Exception("startPaymentTime订单支付起始时间不能为空");
        }
        if(param.getEndPaymentTime() == null){
        	throw new Exception("endPaymentTime订单支付结束时间不能为空");
        }
        return param;
	}
	/**
	 * 邮寄门票订单邮件通知
	 */
	private List<Result> queryDataProcess(Map<String, Object> param) {
			LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess Begin---param:"+JSON.toJSONString(param));
			List<Result> resultList = new ArrayList<Result>();
			try{
				//根据参数，查询订单信息
				List<OrdOrder> orderList= iOrdOrderService.selectMailOrderInfoByParams(param);
				if (orderList == null || orderList.isEmpty()) {
					LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 没有订单信息");
				}
				List<Long> orderIds = new ArrayList<Long>();
				for (OrdOrder order : orderList) {
					orderIds.add(order.getOrderId());
				}
				StringBuilder errorOrderContent = new StringBuilder("");//订单解析报错提示
				if (orderIds!=null && orderIds.size()>0) {
					LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 全部订单id:"+orderIds.toString());
					List<OrdOrderItem> orderItemListAllOrder = null;// 订单子项
					List<OrdPerson> orderPersonListAllOrder = null;// 订单人
					List<OrdAddress> orderAddressListAllOrder = null;// 订单收货地址
					List<OrdOrderPack> orderPackList = null;// 订单打包
					// 初始化订单相关表map
					Map<Long, List<OrdOrderItem>> orderItemMap = new HashMap<Long, List<OrdOrderItem>>();
					Map<Long, List<OrdPerson>> orderPersonMap = new HashMap<Long, List<OrdPerson>>();
					Map<Long, List<OrdAddress>> ordAddressMap = new HashMap<Long, List<OrdAddress>>();
					Map<Long, List<OrdOrderPack>> orderPackMap = new HashMap<Long, List<OrdOrderPack>>();
					
					//子订单 根据订单ID关联查询订单子项集合
					orderItemListAllOrder = ordComplexSqlDao.selectDistinctOrderItemsByorderIds(orderIds);
					if (orderItemListAllOrder != null && !orderItemListAllOrder.isEmpty()) {
						orderItemMap = this.getOrderItemMap(orderItemListAllOrder);// 将list转为map方便处理
					}else{
						LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 全部订单没有子订单数据");
					}
					//联系人、游玩人信息(根据订单ID关联查询订单人集合)--所有订单的联系人、游玩人信息
					orderPersonListAllOrder = ordComplexSqlDao.selectDistinctOrderPersonsByOrderIds(orderIds);
					if (orderPersonListAllOrder != null && !orderPersonListAllOrder.isEmpty()) {
						orderPersonMap = this.getOrderPersonMap(orderPersonListAllOrder);
					}else{
						LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 全部订单没有订单人数据");
					}
					
					// 根据订单ID关联查询订单人对应地址
					orderAddressListAllOrder = ordComplexSqlDao.selectDistinctAddressByorderIds(orderIds);
					if (orderAddressListAllOrder != null && !orderAddressListAllOrder.isEmpty()) {
						ordAddressMap = this.getOrderAddressMap(orderAddressListAllOrder);
					}else{
						LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 全部订单没有订单人地址数据");
					}
					
					// 根据订单ID关联查询订单打包集合
					orderPackList = ordComplexSqlDao.selectDistinctOrderPacksByOrderIds(orderIds);
					if (orderPackList != null && !orderPackList.isEmpty()) {
						orderPackMap = this.getOrderPackMap(orderPackList);
					}else{
						LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 全部订单没有打包数据");
					}

					Result result = null;
					for (OrdOrder order : orderList) {
						LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 循环获取订单信息,当前订单id:"+order.getOrderId());
						try{
							result = new Result();
							result.setPaymentTime(order.getPaymentTime());
							result.setOrderMemo(order.getOrderMemo());
							result.setOrderId(order.getOrderId());

							//1.打包信息--为了获取产品名称--对外卖的产品名字（入口处的产品名字）
							LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+"打包信息");
							if (!orderPackMap.isEmpty() && orderPackMap.containsKey(order.getOrderId())) {
								List<OrdOrderPack> orderPackListOneOrder = new ArrayList<OrdOrderPack>();
								orderPackListOneOrder = orderPackMap.get(order.getOrderId());
								LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",打包信息------1-----orderPackListOneOrder:"+JSON.toJSONString(orderPackListOneOrder));
								order.setOrderPackList(orderPackListOneOrder);
								if (CollectionUtils.isNotEmpty(order.getOrderPackList())
										&& CollectionUtils.isNotEmpty(order.getOrderItemList())) {
									Map<Long, List<OrdOrderItem>> orderItemMapTemp = new HashMap<Long, List<OrdOrderItem>>();
									for (OrdOrderItem orderItem : order.getOrderItemList()) {
										List<OrdOrderItem> itemList = null;

										if (orderItem.getOrderPackId() != null) {
											if (orderItemMapTemp.containsKey(orderItem
													.getOrderPackId())) {
												itemList = orderItemMapTemp.get(orderItem
														.getOrderPackId());
											} else {
												itemList = new ArrayList<OrdOrderItem>();
												orderItemMapTemp.put(
														orderItem.getOrderPackId(), itemList);
											}
											itemList.add(orderItem);
										}
									}
									for (OrdOrderPack orderPack : order.getOrderPackList()) {
										orderPack.setOrderItemList(orderItemMapTemp
												.get(orderPack.getOrderPackId()));
									}
								}
							}

							//2.联系人
							LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+"联系人信息");
							OrdPerson ordPersonContact = null;//联系人
							OrdPerson firstTravellerPerson = null;//第一游客
							List<OrdPerson> ordPersonList = null;//订单联系人、游玩人信息
							if (!orderPersonMap.isEmpty()&& orderPersonMap.containsKey(order.getOrderId())) {
								ordPersonList = orderPersonMap.get(order.getOrderId());
								LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",联系人信息------1-----ordPersonList:"+JSON.toJSONString(ordPersonList));
								order.setOrdPersonList(ordPersonList);

								for (OrdPerson ordPerson : ordPersonList) {
									String personType = ordPerson.getPersonType();
									LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",联系人信息------2-----姓名:"+ordPerson.getFullName()+",personType:"+personType);
									if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {
										ordPersonContact = ordPerson;
									} else if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(personType)) {
										if (firstTravellerPerson==null) {
											firstTravellerPerson=ordPerson;
										}
									}
								}
								//门票没有联系人   默认第一游客为联系人
								if (ordPersonContact==null && firstTravellerPerson!=null) {
									ordPersonContact=firstTravellerPerson;
									LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",联系人信息------3-----没有联系人,默认第一游客为联系人,姓名:"+ordPersonContact.getFullName());
								}
								String mobile = ordPersonContact.getMobile();//手机号
								LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",联系人信息------4-----联系人手机号:"+mobile);
								if(order.getDistributorId()!=null && order.getDistributorId()==3
										&& StringUtils.isEmpty(ordPersonContact.getFullName())
										&& StringUtils.isNotEmpty(mobile)){
									result.setOrdPersonName(mobile.substring(0, 3)+"****"+mobile.substring(7, mobile.length()));

								}else{
									result.setOrdPersonName(ordPersonContact.getFullName());
								}
								result.setOrdPersonMobile(ordPersonContact.getMobile());
							}

							//3.物流
							LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+"物流信息");
							if (!ordAddressMap.isEmpty()) {
								List<OrdPerson> personList = order.getOrdPersonList();
								if (CollectionUtils.isNotEmpty(personList)) {
									for (OrdPerson ordPerson : personList) {
										List<OrdAddress> addressList = new ArrayList<OrdAddress>();
										if (ordAddressMap.containsKey(ordPerson.getOrdPersonId())) {
											addressList = ordAddressMap.get(ordPerson.getOrdPersonId());
											LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",物流信息------1-----addressList:"+JSON.toJSONString(addressList));
											ordPerson.setAddressList(addressList);
										}
									}
								}

								OrdPerson addressPerson = order.getAddressPerson();
								//快递联系人
								if(addressPerson != null){
									LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",物流信息------2-----快递联系人:"+JSON.toJSONString(addressPerson));
									if(StringUtils.isNotEmpty(addressPerson.getFullName())){
										result.setAddressPersonName(addressPerson.getFullName());
									}
									if(StringUtils.isNotEmpty(addressPerson.getMobile())){
										result.setAddressPersonMobile(addressPerson.getMobile());
									}
								}
								OrdAddress ordAddress = order.getOrdAddress();
								String address = "";
								if(ordAddress != null){
									LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",物流信息------3-----快递地址:"+JSON.toJSONString(ordAddress));
									if(StringUtils.isNotEmpty(ordAddress.getProvince())){
										address += ordAddress.getProvince();
									}
									if(StringUtils.isNotEmpty(ordAddress.getCity())){
										address += ordAddress.getCity();
									}
									if(StringUtils.isNotEmpty(ordAddress.getDistrict())){
										address += ordAddress.getDistrict();
									}
									if(StringUtils.isNotEmpty(ordAddress.getStreet())){
										address += ordAddress.getStreet();
									}
									if(StringUtils.isNotEmpty(ordAddress.getPostalCode())){
										address += ordAddress.getPostalCode();
									}
								}
								LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",物流信息------4-----address:"+JSON.toJSONString(ordAddress));
								result.setAddress(address);
							}

							//4.子订单
							LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+"子订单信息");
							List<OrdOrderItem> orderItemList = null;//订单子订单信息
							if(!orderItemMap.isEmpty() && orderItemMap.containsKey(order.getOrderId())){
								orderItemList = orderItemMap.get(order.getOrderId());
								LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",子订单信息------1-----orderItemList:"+JSON.toJSONString(orderItemList));
								order.setOrderItemList(orderItemList);
							}
							result.setProductName(order.getOrderProductName()+"("+order.getProductId()+")");
							LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+",子订单信息------2-----产品名称:"+order.getOrderProductName()+"("+order.getProductId()+")");
							for (OrdOrderItem ordOrderItem : orderItemList) {
								Result resultItem= new Result();
								EnhanceBeanUtils.copyProperties(result, resultItem);
								resultItem.setOrderItemId(ordOrderItem.getOrderItemId());
								resultItem.setOrderItemMemo(ordOrderItem.getOrderMemo());
								String goodsName = buildGoodsName(ordOrderItem);//商品名
								Map<String,Object> contentMap = ordOrderItem.getContentMap();
								String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
								//演出票显示

								if(BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equals(categoryType)){
									ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);

									SuppGoods suppGoods = new SuppGoods();

									if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
										suppGoods = resultHandleSuppGoods.getReturnContent();
									}
									goodsName = ordOrderItem.getSuppGoodsName()+"-"+GOODSSPEC.getSpecName(suppGoods.getGoodsSpec());
								}
								resultItem.setGoodsName(goodsName);
								resultItem.setBuyItemCount(ordOrderItem.getQuantity()+"份");
								resultItem.setVisitTime(buildVisitTime(ordOrderItem));
								resultList.add(resultItem);
							}
						}catch (Exception e){
							LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 解析订单"+order.getOrderId()+"信息报错：",e);
							errorOrderContent.append("\n").append(order.getOrderId());
						}
					}
				}else{
					LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 订单id为空");
				}
				try{
					// 增加附件
					Calendar emailTime=Calendar.getInstance();
					SimpleDateFormat formatter= new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss"); 
					String fileName = "邮寄门票订单数据_" + formatter.format(emailTime.getTime());  
					File file = this.createFile(resultList,fileName);
					EmailAttachment emailAttachment = new EmailAttachment();
					Long fileId = null;
					try {
						fileId = vstFSClient.uploadFile(file, "EMAIL_FILE");
					} catch (Exception e) {
						LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess fail to upload temp file", e);
					}
					if (fileId != null && fileId != 0) {
						emailAttachment.setFileId(fileId);
						emailAttachment.setFileName(fileName+".csv");
					}
					LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess emailAttachment:"+JSON.toJSONString(emailAttachment));
					
					EmailContent emailContent = new EmailContent();
					emailContent.setFromAddress("service@cs.lvmama.com");
					emailContent.setFromName("驴妈妈旅游网");
					emailContent.setSubject("邮寄门票订单数据"+formatter.format(emailTime.getTime()));
					/*emailContent.setToAddress("JLPC-ZS@lvmama.com");*/
					emailContent.setToAddress("jlpc-zc@lvmama.com");//20170807邮寄地址修改
					emailContent.setCcAddress("");
					if(StringUtils.isNotEmpty(errorOrderContent.toString())){//若有解析失败订单，在邮件中记录
						emailContent.setContentText("数据请查收附件！"+"\n以下订单请人工核实相关信息。如有问题请与产品经理联系。"+errorOrderContent.toString());
					}else {
						emailContent.setContentText("数据请查收附件！");
					}
			    	emailContent.setCreateTime(new Date());
			    	vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachment);
				}catch(Exception e){
					LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 发送邮件报错：",e);
				}
			}catch(Exception e){
				LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess 报错：",e);
			}
			LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess End");
			return resultList;
	}
	
	/**
	 * 将数据生成文件
	 * @param resultList
	 * @param fileName
	 * @return
	 */
	private File createFile(List<Result> resultList,String fileName){
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess createFile Begin---resultList:"+resultList+",fileName:"+fileName);
		File file = null;
		try{
			String filePath = System.getProperty("java.io.tmpdir") + "/";//文件路径
			List<LinkedHashMap<String,Object>> exportDataList = new ArrayList<LinkedHashMap<String,Object>>();  
			LinkedHashMap<String,String> titleMap = new LinkedHashMap<String,String>();  //首行字段名
			titleMap.put("1", "支付时间");  
			titleMap.put("2", "订单号");  
			titleMap.put("3", "子订单号");  
			titleMap.put("4", "产品名");  
			titleMap.put("5", "商品名");  
			titleMap.put("6", "游客名称");  
			titleMap.put("7", "出游日期");  
			titleMap.put("8", "预定份数");  
			titleMap.put("9", "联系人手机");  
			titleMap.put("10", "快递地址");  
			titleMap.put("11", "快递收件人");  
			titleMap.put("12", "收件人手机");  
			titleMap.put("13", "主订单备注");  
			titleMap.put("14", "子订单备注");
			
			LinkedHashMap<String,Object> dataMap = null;  
			for (Result result : resultList) {
				dataMap = new LinkedHashMap<String,Object>();  
				if(result.getPaymentTime() != null){
					dataMap.put("1", DateUtil.converDateToString(result.getPaymentTime(), DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss));
				}else{
					dataMap.put("1", "");
				}
				dataMap.put("2", result.getOrderId() != null ? result.getOrderId() : "");
				dataMap.put("3", result.getOrderItemId() != null ? result.getOrderItemId() : "");
				dataMap.put("4", result.getProductName() != null ? result.getProductName() : "");
				dataMap.put("5", result.getGoodsName() != null ? result.getGoodsName() : "");
				dataMap.put("6", result.getOrdPersonName() != null ? result.getOrdPersonName() : "");
				dataMap.put("7", result.getVisitTime() != null ? result.getVisitTime() : "");
				dataMap.put("8", result.getBuyItemCount() != null ? result.getBuyItemCount() : "");
				dataMap.put("9", result.getOrdPersonMobile() != null ? result.getOrdPersonMobile() : "");
				dataMap.put("10", result.getAddress() != null ? result.getAddress() : "");
				dataMap.put("11", result.getAddressPersonName() != null ? result.getAddressPersonName() : "");
				dataMap.put("12", result.getAddressPersonMobile() != null ? result.getAddressPersonMobile() : "");
				dataMap.put("13", result.getOrderMemo() != null ? result.getOrderMemo() : "");
				dataMap.put("14", result.getOrderItemMemo() != null ? result.getOrderItemMemo() : "");
				exportDataList.add(dataMap);  
			}
			file = CSVUtils.createCSVFile(exportDataList, titleMap, filePath, fileName);//生成CSV文件
			LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess createFile 附件路径："+file.getPath()+"，附件名称："+file.getName());
		}catch(Exception e){
			LOG.error("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess createFile 报错：",e);
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess createFile End");
		return file;
	}
	
	/**
	 * 商品名称
	 * @param ordOrderItem
	 * @return
	 */
 	private String buildGoodsName(OrdOrderItem ordOrderItem) {
 		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildGoodsName Begin---子订单id:"+ordOrderItem.getOrderItemId());
		String goodsName = "未知产品名称";
		if (null != ordOrderItem) {

			Map<String,Object> contentMap = ordOrderItem.getContentMap();

			String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());

			// 如果是交通接驳，包含商品这一列显示  产品 + 商品
			if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())){
				goodsName = ordOrderItem.getProductName()+"-"+ordOrderItem.getSuppGoodsName();
			}else{
				goodsName = ordOrderItem.getProductName()+"-"+branchName+"("+ordOrderItem.getSuppGoodsName()+")";
			}
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildGoodsName End---goodsName:"+goodsName);
		return goodsName;
	}
	
	/**
	 * 出游日期
	 * @param orderItem
	 * @return
	 */
	private String buildVisitTime(OrdOrderItem orderItem) {
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildVisitTime Begin---子订单id:"+orderItem.getOrderItemId());
		if(orderItem != null){
			if (orderItem.hasTicketAperiodic()) {
                //取通关时间
                List<OrdTicketPerform> resultList = complexQueryService.selectByOrderItem(orderItem.getOrderItemId());
                if(CollectionUtils.isNotEmpty(resultList)){
                    Date performTime = resultList.get(0).getPerformTime();
                    if(performTime != null){
                    	LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildVisitTime End---visitTime:"+DateUtil.SimpleFormatDateToString(performTime));
                        return DateUtil.SimpleFormatDateToString(performTime);
                    }
                }
                String visitTimeStr = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name());

				//期票不可游玩日期描述
				String unvalidDesc =  (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name());
				if(StringUtil.isNotEmptyString(unvalidDesc)){
                    visitTimeStr +="</br>(不适用日期:"+unvalidDesc+")";
				}
				LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildVisitTime End---visitTime:"+visitTimeStr);
				return visitTimeStr;
			}else{
                StringBuffer visitTime = new StringBuffer();
                visitTime.append(DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd"));

                //马戏票场次信息展示
                String startTime = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.circusActStartTime.name());
                String endTime = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.circusActEndtime.name());
				if (StringUtil.isNotEmptyString(startTime)) {
					String regex = " ";
					String[] split = startTime.split(regex);
					startTime = split[split.length - 1];
					visitTime.append("</br>").append(startTime);
					if (StringUtil.isNotEmptyString(endTime)) {
						split = endTime.split(regex);
						endTime = split[split.length - 1];
						visitTime.append(" - ").append(endTime);
					}
				}
				LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildVisitTime End---visitTime:"+visitTime.toString());
                return visitTime.toString();
            }
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess buildVisitTime End---visitTime:未知日期");
        return "未知日期";
	}
	/**
	 * 将订单子项list集合转化为map集合，方便处理
	 * 
	 * @param orderItemList
	 * @return
	 */
	private Map<Long, List<OrdOrderItem>> getOrderItemMap(final List<OrdOrderItem> orderItemList) {
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderItemMap Begin");
		final Map<Long, List<OrdOrderItem>> map = new HashMap<Long, List<OrdOrderItem>>();
		for (OrdOrderItem item : orderItemList) {
			final List<OrdOrderItem> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdOrderItem>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderItemMap End---map:"+JSON.toJSONString(map));
		return map;
	}
	/**
	 * 将订单游客list转化为map方便处理
	 * @param orderPersonList
	 * @return
	 */
	private Map<Long, List<OrdPerson>> getOrderPersonMap(final List<OrdPerson> orderPersonList) {
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderPersonMap Begin");
		final Map<Long, List<OrdPerson>> map = new HashMap<Long, List<OrdPerson>>();
		for (OrdPerson item : orderPersonList) {
			final List<OrdPerson> list;
			if (map.containsKey(item.getObjectId())) {
				list = map.get(item.getObjectId());
			} else {
				list = new ArrayList<OrdPerson>();
			}
			list.add(item);
			map.put(item.getObjectId(), list);
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderPersonMap End---map:"+JSON.toJSONString(map));
		return map;
	}
	/**
	 * 将订单游客list转化为map方便处理
	 * @param orderAddressList
	 * @return
	 */
	private Map<Long, List<OrdAddress>> getOrderAddressMap(final List<OrdAddress> orderAddressList) {
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderAddressMap Begin");
		final Map<Long, List<OrdAddress>> map = new HashMap<Long, List<OrdAddress>>();
		for (OrdAddress item : orderAddressList) {
			final List<OrdAddress> list;
			if (map.containsKey(item.getOrdPersonId())) {
				list = map.get(item.getOrdPersonId());
			} else {
				list = new ArrayList<OrdAddress>();
			}
			list.add(item);
			map.put(item.getOrdPersonId(), list);
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderAddressMap End---map:"+JSON.toJSONString(map));
		return map;
	}
	
	/**
	 * 将订单打包信息转化为map
	 * 
	 * @param orderPackList
	 * @return
	 */
	private Map<Long, List<OrdOrderPack>> getOrderPackMap(
			final List<OrdOrderPack> orderPackList) {
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderPackMap Begin");
		final Map<Long, List<OrdOrderPack>> map = new HashMap<Long, List<OrdOrderPack>>();
		for (OrdOrderPack item : orderPackList) {
			final List<OrdOrderPack> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdOrderPack>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		LOG.info("邮寄门票订单邮件通知MailTicketOrderDataProcessJobService queryDataProcess getOrderPackMap End---map:"+JSON.toJSONString(map));
		return map;
	}
	
	/**
	 * 查询参数
	 * @author lianghuihui
	 *
	 */
	private static class Parameter implements Serializable {
		private static final long serialVersionUID = 8054602299451908340L;
  		private String paymentStutus;//支付状态：PAYED已支付
  		//审核通过：信息状态(确认通过)+资源状态(资源满足)
  		private String infoStatus;//信息状态：INFOPASS确认通过（已审核）
  		private String resourceStatus;//资源状态：AMPLE资源满足
		private Long[] managerIds;//主订单产品经理id
		private int beforDaysOfCreateTime = 0;//订单创建时间前的天数(eg：7,查询的是近一周的订单信息)
		/*private String startCreateTime;//订单创建时间 开始时间
		private String endCreateTime;//订单创建时间 结束时间*/		
		private String startPaymentTime;//支付时间(支付状态变更时间) 起始时间
		private String endPaymentTime;//支付时间(支付状态变更时间) 结束时间
		private String[] buCodes;//主订单所属BU
		
		public String getPaymentStutus() {
			return paymentStutus;
		}
		public String getInfoStatus() {
			return infoStatus;
		}
		public String getResourceStatus() {
			return resourceStatus;
		}
		public Long[] getManagerIds() {
			return managerIds;
		}
		public int getBeforDaysOfCreateTime() {
			return beforDaysOfCreateTime;
		}
		/*public String getStartCreateTime() {
			return startCreateTime;
		}
		public String getEndCreateTime() {
			return endCreateTime;
		}*/
		public String getStartPaymentTime() {
			return startPaymentTime;
		}
		public String getEndPaymentTime() {
			return endPaymentTime;
		}
		public String[] getBuCodes() {
			return buCodes;
		}
	}

	/**
	 * 查询结果
	 * @author lianghuihui
	 *
	 */
	private static class Result implements Serializable {
		private static final long serialVersionUID = -4781115700950393534L;
		private Date paymentTime;//支付时间
		private Long orderId;//订单号
		private Long orderItemId;//子订单号
		private String productName;//产品名
		private String goodsName;//商品名
		private String ordPersonName;//下单人联系人姓名
		private String visitTime;//出游日期
		private String buyItemCount;//预定份数
		private String ordPersonMobile;//联系人手机
		private String address;//快递地址
		private String addressPersonName;//快递收件人
		private String addressPersonMobile;//收件人手机
		private String orderMemo;//主订单备注
		private String orderItemMemo;//子订单备注
		public Date getPaymentTime() {
			return paymentTime;
		}
		public void setPaymentTime(Date paymentTime) {
			this.paymentTime = paymentTime;
		}
		public Long getOrderId() {
			return orderId;
		}
		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}
		public Long getOrderItemId() {
			return orderItemId;
		}
		public void setOrderItemId(Long orderItemId) {
			this.orderItemId = orderItemId;
		}
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getGoodsName() {
			return goodsName;
		}
		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}
		public String getOrdPersonName() {
			return ordPersonName;
		}
		public void setOrdPersonName(String ordPersonName) {
			this.ordPersonName = ordPersonName;
		}
		public String getVisitTime() {
			return visitTime;
		}
		public void setVisitTime(String visitTime) {
			this.visitTime = visitTime;
		}
		public String getBuyItemCount() {
			return buyItemCount;
		}
		public void setBuyItemCount(String buyItemCount) {
			this.buyItemCount = buyItemCount;
		}
		public String getOrdPersonMobile() {
			return ordPersonMobile;
		}
		public void setOrdPersonMobile(String ordPersonMobile) {
			this.ordPersonMobile = ordPersonMobile;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getAddressPersonName() {
			return addressPersonName;
		}
		public void setAddressPersonName(String addressPersonName) {
			this.addressPersonName = addressPersonName;
		}
		public String getAddressPersonMobile() {
			return addressPersonMobile;
		}
		public void setAddressPersonMobile(String addressPersonMobile) {
			this.addressPersonMobile = addressPersonMobile;
		}
		public String getOrderMemo() {
			return orderMemo;
		}
		public void setOrderMemo(String orderMemo) {
			this.orderMemo = orderMemo;
		}
		public String getOrderItemMemo() {
			return orderItemMemo;
		}
		public void setOrderItemMemo(String orderItemMemo) {
			this.orderItemMemo = orderItemMemo;
		}
	}
}
