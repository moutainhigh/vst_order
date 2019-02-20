package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdNoUploadNotice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdNoUploadNoticeDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

public class AutoSendMailForNoUploadNoticeRegimentJob implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(AutoSendMailForNoUploadNoticeRegimentJob.class);
	
	@Autowired
	private OrdOrderDao orderDao;
	
	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;

	@Autowired
	private ProdProductClientService prodProductClientService;
	
    @Autowired
    private PermUserServiceAdapter permUserServiceAdapater;
    
    @Autowired
    private OrdNoUploadNoticeDao ordNoUploadNoticeDao;
	
	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable());
		if(Constant.getInstance().isJobRunnable()){
			try {
				
				Date nowDate = DateUtil.stringToDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
				
				Date startDate=DateUtils.addDays(nowDate, 1);
				Date endDate=DateUtils.addDays(nowDate, 2);
				LOG.info("AutoSendMailForNoUploadNoticeRegimentJob execute ==========> executeTime:"+nowDate+"startDate="+DateUtil.formatSimpleDate(startDate)+"endDate"+DateUtil.formatSimpleDate(endDate));
				Map<String, Object> paramOrder = new HashMap<String, Object>();
				paramOrder.put("startDate", startDate);
				paramOrder.put("endDate", endDate);
				//订单状态为正常，已支付，资源审核通过，信息审核通过、跟团游、当地游、出团通知书状态为未上传出团通知书、出游日期为两天内的
				List<OrdOrder> ordOrdersList = orderDao.getNoUploadNoticeRegiment(paramOrder);
				List<OrdOrder> removeOrdOrders=new ArrayList<OrdOrder>();
				
				//从数据中 通过订单号过滤掉已经发送过邮件的订单
				Map<String, Object> oldOrderNoticeMap = new HashMap<String, Object>();
				oldOrderNoticeMap.put("visitTime", startDate);
				//已经发送了邮件的订单记录
				List<OrdNoUploadNotice>  oldOrderNoticeList=ordNoUploadNoticeDao.selectByParams(oldOrderNoticeMap);
				if(oldOrderNoticeList!=null&&oldOrderNoticeList.size()>0){
					for(OrdNoUploadNotice oldNotice:oldOrderNoticeList){
						for(OrdOrder newOrder:ordOrdersList){
							if(newOrder.getOrderId().equals(oldNotice.getOrderId())){
								 if(!removeOrdOrders.contains(newOrder)){
									 removeOrdOrders.add(newOrder);
								 }
							 }
						}
					}
					ordOrdersList.removeAll(removeOrdOrders); 
				}
				List<OrdOrder> toSendOrdOrderList=new ArrayList<OrdOrder>();//待发邮件的订单总数
				List<Long> managerIdList=new ArrayList<Long>();//产品经理ID
				Map<Long,List<OrdOrder>>  managerOrderMap=new HashMap<Long,List<OrdOrder>>();//产品经理id、订单List
				List<OrdNoUploadNotice>  orderNoticeList=new ArrayList<OrdNoUploadNotice>();
				if(null!=ordOrdersList&&ordOrdersList.size()>0){
					for (OrdOrder ordOrder : ordOrdersList) {
						String jsonConten=ordOrder.getContent();//现在国内BU和目的地BU的线路订单都增加这些字段。{"packageType":"LVMAMA","productId":580927,"productType":"INNERLINE"}
						if(jsonConten==null||jsonConten=="") continue;
						String productType="";
						Long productId=null;
						try{
							JSONObject jsonObject = JSONObject.fromObject(jsonConten);
							 productType=jsonObject.getString("productType");
							 productId = jsonObject.getLong("productId");
						}catch(Exception e){
							LOG.error("AutoSendMailForNoUploadNoticeRegimentJob log  JSONObject  orderId="+ordOrder.getOrderId()+" error:"+e.getMessage(),e);
						}
						if(productType==null||productType=="") continue;
						if(productId==null) continue;
						//过滤产品类型：国内-长线 ，产品ID
						if("INNERLONGLINE".equals(productType)){
							ProdProduct product = prodProductClientService.findProdProductSimpleById(productId).getReturnContent();
							LOG.info("AutoSendMailForNoUploadNoticeRegimentJob  prodProductClientService.findProdProductSimpleById，productId = "+productId);
							//获取对应的产品经理ID 、产品名称
							Long managerId=product.getManagerId();
							String productName=product.getProductName();
							ordOrder.setManagerId(managerId);//产品经理ID
							ordOrder.setProductId(productId);//产品ID
							ordOrder.setProductName(productName);//产品名称
							toSendOrdOrderList.add(ordOrder);
							
							OrdNoUploadNotice ordNoUploadNotice=new OrdNoUploadNotice();
							ordNoUploadNotice.setOrderId(ordOrder.getOrderId());
							ordNoUploadNotice.setProductId(productId);
							ordNoUploadNotice.setVisitTime(ordOrder.getVisitTime());
							ordNoUploadNotice.setSendMailTime(new Date());
							orderNoticeList.add(ordNoUploadNotice);
							
							if(!managerIdList.contains(managerId)){
								managerIdList.add(managerId);
							}
						}
					}
					
					//订单归属于相同产品经理的将通知信息合在一封邮件中进行发送。每封邮件中最多包含5个订单的信息，第6-10单的发送第二封邮件
					for(Long managerId :managerIdList){
						List<OrdOrder> sendMailList=new ArrayList<OrdOrder>();
						for(OrdOrder ord:toSendOrdOrderList){
							if(managerId.equals(ord.getManagerId())){
								sendMailList.add(ord);
							}
						}
						managerOrderMap.put(managerId, sendMailList);
					}
					
					 for (Map.Entry<Long, List<OrdOrder>> entry : managerOrderMap.entrySet()) {
						 Long managerId=entry.getKey();//产品经理ID
						 PermUser prodManager = permUserServiceAdapater.getPermUserByUserId(managerId);
						 String toAddressEmail=prodManager.getEmail();
						 List<OrdOrder> sendMailOrdOrderList=entry.getValue();
						 int size=sendMailOrdOrderList.size();
						 if(size<=5){
							 sendMail(sendMailOrdOrderList,toAddressEmail);
						 }else{
							 int count=size/5;
							 if(size%5!=0){
								 count=count+1;
							 } 
							 for(int i=1;i<=count;i++){
								 if(i==count){
									 List<OrdOrder> lastSendOrderList= sendMailOrdOrderList.subList((i-1)*5, size);
									 sendMail(lastSendOrderList,toAddressEmail);
									 
								 }else{
									List<OrdOrder> singleSendOrderList= sendMailOrdOrderList.subList((i-1)*5, i*5);
									sendMail(singleSendOrderList,toAddressEmail);
								 }
							 }
						 }
					 }
					 
					//删除 当日日期+1 天以前的数据
					 Map<String, Object> delOrderNoticeMap = new HashMap<String, Object>();
					 delOrderNoticeMap.put("visitTime", startDate);
					 ordNoUploadNoticeDao.deleteOrdNoUploadNotices(delOrderNoticeMap);
					 
					 //保存 已经发送的数据
					 for(OrdNoUploadNotice notice :orderNoticeList){
						 ordNoUploadNoticeDao.insertSelective(notice);
					 }
				}
			} catch (Exception e) {
				LOG.error("AutoSendMailForNoUploadNoticeRegimentJob log error:"+e.getMessage(),e);
			}
		}
	}
	
	private void sendMail(List<OrdOrder> sendMailOrdOrderList,String toAddressEmail){
		 StringBuffer stringBuilder = new StringBuffer();
		 String sendOrderId="";
		 for(OrdOrder sendOrder:sendMailOrdOrderList){
			 sendOrderId+=sendOrder.getOrderId()+",";
			 stringBuilder.append("订单号："+sendOrder.getOrderId()+",产品ID："+sendOrder.getProductId()+",产品名称:"+sendOrder.getProductName()+"。\r\n <br>");
		 }
		 stringBuilder.append(" 以上订单中，还未上传出团通知书，请尽快上传，谢谢。");
		 EmailContent emailContent = new EmailContent();
	     emailContent.setSubject("订单号："+sendOrderId+"催出团通知书");
	     emailContent.setContentText(stringBuilder.toString());
	     emailContent.setSendTime(new Date());
	     emailContent.setFromAddress("service@cs.lvmama.com");
	     emailContent.setToAddress(toAddressEmail);
	     vstEmailServiceAdapter.sendEmailDirect(emailContent);
	     LOG.info("AutoSendMailForNoUploadNoticeRegimentJob execute ==========> orderIds:"+sendOrderId); 
	}

}
