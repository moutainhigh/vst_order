package com.lvmama.vst.order.job;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderUpdateService;

@Service
public class OrderEBKEnterNotInTimeUpdateJob implements Runnable {

	private static final Log LOG = LogFactory
			.getLog(OrderEBKEnterNotInTimeUpdateJob.class);

	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private IOrder2RouteService order2RouteService;

	@Override
	public void run() {
		
		boolean msgAndJobSwitch= false; //msg and job 总开关
		msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
		if(msgAndJobSwitch){
		    return;
		}
		
		if (Constant.getInstance().isJobRunnable()) {
			LOG.info("******设置供应商是否及时入园JOB开始******");
			Map<String, Object> params = new HashMap<String, Object>();
			String today = DateUtil.formatDate(new Date(), "yyyy/MM/dd");
			params.put("visitTime", today);
			List<OrdOrderItem> itemList = iOrdOrderItemService
					.queryTicketOrderItem(params);
			Set<Long> supperlierSet = new HashSet<Long>();
			Map<Long, String> supplierMap = new HashMap<Long, String>();
			for (OrdOrderItem item : itemList) {
				Long supplierId = item.getSupplierId();
				supperlierSet.add(supplierId);
			}
			for (Long supperliId : supperlierSet) {
				String notInTimeFlag = suppSupplierClientService
						.findSuppSupplierById(supperliId).getReturnContent()
						.getNotInTimeFlag();
				supplierMap.put(supperliId, notInTimeFlag);
			}
			
			Set<Long> suppGoodsIdSet = new HashSet<Long>();
			Map<Long, String> suppGoodsIdMap = new HashMap<Long, String>();
			for (OrdOrderItem item : itemList) {
				Long suppGoodsId = item.getSuppGoodsId();
				suppGoodsIdSet.add(suppGoodsId);
			}
			for (Long suppGoodsId : suppGoodsIdSet) {
				SuppGoods suppgoods = suppGoodsClientService.findSuppGoodsById(suppGoodsId).getReturnContent();
				String notInTimeFlag =  suppgoods.getNotInTimeFlag();
				suppGoodsIdMap.put(suppGoodsId, notInTimeFlag);
			}
			
			
			for (OrdOrderItem item : itemList) {
				String notInTimeFlag_supplier = supplierMap.get(item.getSupplierId());
				String notInTimeFlag_suppgoods = suppGoodsIdMap.get(item.getSuppGoodsId());
				if(StringUtils.isBlank(notInTimeFlag_supplier)){
					notInTimeFlag_supplier = "N";
				}
				if(StringUtils.isBlank(notInTimeFlag_suppgoods)){
					notInTimeFlag_suppgoods = "N";
				}
				String notInTimeFlag = "N";
				if("Y".equals(notInTimeFlag_supplier)){
					notInTimeFlag = "Y";
				}else{
					if("Y".equals(notInTimeFlag_suppgoods)){
						notInTimeFlag = "Y";
					}
				}
				params.put("notInTimeFlag", notInTimeFlag);
				params.put("orderItemId", item.getOrderItemId());
				orderUpdateService.updateNotInTimeFlag(params);
			}
			LOG.info("******设置供应商是否及时入园JOB结束******");
		}
	}
}
