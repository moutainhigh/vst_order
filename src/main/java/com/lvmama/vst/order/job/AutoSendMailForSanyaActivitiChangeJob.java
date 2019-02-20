package com.lvmama.vst.order.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.client.prod.service.ProdDestReClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdDestRe;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

public class AutoSendMailForSanyaActivitiChangeJob implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(AutoSendMailForSanyaActivitiChangeJob.class);
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;
	
	@Autowired
	private ProdDestReClientService  prodDestReRemoteService;
	
	@Autowired
    IOrdOrderService orderService;
	
	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable());
		if(Constant.getInstance().isJobRunnable()){
			try {
				Date endDate = DateUtil.stringToDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH"), "yyyy-MM-dd HH");
				LOG.info("AutoSendMailForSanyaActivitiChangeJob execute ==========> executeTime:"+endDate);
				Date startDate = DateUtils.addHours(endDate, -1);
				Map<String, Object> paramOrder = new HashMap<String, Object>();
				paramOrder.put("startDate", startDate);
				paramOrder.put("endDate", endDate);
				List<OrdOrder> OrdOrders = orderService.getOrderIdsForSendMail(paramOrder);
				if(null!=OrdOrders&&OrdOrders.size()>0){
					List<Long> orderIds=new ArrayList<Long>();
					Map<Long, Long> map=new HashMap<Long, Long>();
					for (OrdOrder ordOrder : OrdOrders) {
						orderIds.add(ordOrder.getOrderId());
						map.put(ordOrder.getOrderId(), ordOrder.getCategoryId());
					}
					//List<Long> orderIdsForSendMail = ordOrderItemDao.getOrderIdsForSendMail(orderIds);
					List<OrdOrderItem>  ordOrderItemList=ordOrderItemDao.getOrderItemsForSendMail(orderIds);
					if(null!=ordOrderItemList&&ordOrderItemList.size()>0){
						 
						List<ProdDestRe>  destList=new ArrayList<ProdDestRe>();
						List<Long> productIdList=new ArrayList<Long>();
						productIdList.add(229142l);
						productIdList.add(269899l);
						productIdList.add(269902l);
						productIdList.add(270020l);
						productIdList.add(1522578l);
						productIdList.add(1614008l);
						productIdList.add(1615962l);
						productIdList.add(1618174l);
						productIdList.add(1618197l);
						productIdList.add(1618466l);
						productIdList.add(1618480l);
						productIdList.add(1618489l);
						productIdList.add(1618509l);
						productIdList.add(1618552l);
						productIdList.add(1618563l);
						productIdList.add(1618586l);
						productIdList.add(1618590l);
						productIdList.add(1618592l);
						productIdList.add(1618594l);

						ResultHandleT<List<ProdDestRe>> ProdDestReHandle=prodDestReRemoteService.selectDestNameByProductIds(productIdList);
						if(ProdDestReHandle!=null&&ProdDestReHandle.getReturnContent()!=null){
							destList=ProdDestReHandle.getReturnContent();
						}
						
						
						StringBuffer stringBuilder = new StringBuffer();
						SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						stringBuilder.append("汇总时间："+sdf.format(startDate)+"~"+sdf.format(endDate)+"\n");
						stringBuilder.append("主订单ID：\n");
						stringBuilder.append("产品品类（跟团/机酒/其它票/景点门票）		主订单ID\n");
						
						List<Long> orderIdList=new ArrayList<Long>();
						for (OrdOrderItem orderItem : ordOrderItemList) {
							
							if(!orderIdList.contains(orderItem.getOrderId())){
								orderIdList.add(orderItem.getOrderId());
								Long categoryId = map.get(orderItem.getOrderId());
								if(categoryId.intValue()==15){
									stringBuilder.append("跟团						"+orderItem.getOrderId()+"\n");
								}else if(categoryId.intValue()==11){
									stringBuilder.append("景点门票						"+orderItem.getOrderId()+"\n");
								}else if(categoryId.intValue()==12){
									stringBuilder.append("其它票						"+orderItem.getOrderId()+"\n");
								}else{
									stringBuilder.append("机酒						"+orderItem.getOrderId()+"\n");
								}
							}
							
							for(ProdDestRe  dest:destList){
								if(dest.getProductId().equals(orderItem.getProductId())){
									stringBuilder.append("城市："+ dest.getDestName()+" \n");
									break;
								}
							}
							
						}
						EmailContent emailContent = new EmailContent();
				        emailContent.setSubject("国内BU含接送机订单汇总");
				        emailContent.setContentText(stringBuilder.toString());
				        emailContent.setSendTime(new Date());
				        emailContent.setFromAddress("service@cs.lvmama.com");
				        emailContent.setFromName("驴妈妈旅游网");
				        emailContent.setToAddress("gnzyjsj@lvmama.com");
				        emailContent.setCcAddress("ddcl@lvmama.com");
				        vstEmailServiceAdapter.sendEmailDirect(emailContent);
				        LOG.info("AutoSendMailForSanyaActivitiChangeJob execute ==========> orderIds:"+stringBuilder.toString()); 
					}
				}
			} catch (Exception e) {
				LOG.error("AutoSendMailForSanyaActivitiChangeJob log error:"+e.getMessage(),e);
			}
		}
	}

}
