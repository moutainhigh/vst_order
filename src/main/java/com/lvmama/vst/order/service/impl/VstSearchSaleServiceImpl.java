package com.lvmama.vst.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.lvmama.comm.utils.MemcachedUtil;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.order.po.VstSearchSale;
import com.lvmama.vst.back.order.po.VstSearchSaleMuilt;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.VstSearchSaleDao;
import com.lvmama.vst.order.dao.VstSearchSaleMuiltDao;
import com.lvmama.vst.order.service.VstSearchSaleService;

@Service
public class VstSearchSaleServiceImpl implements VstSearchSaleService {

	private static Logger logger = LoggerFactory.getLogger(VstSearchSaleServiceImpl.class);
	@Autowired
	private VstSearchSaleDao vstSearchSaleDao;

	@Autowired
	private VstSearchSaleMuiltDao vstSearchSaleMuiltDao;
	
    @Autowired
    private CategoryClientService categoryClientService;
	
	public static final String memcachedKey = "searchSaleKey";
	
	public static final String memcachedValue = "on";
	
	public static final int seconds = 60*60*2;

	@Override
	public void createVstSearchSale() {

		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(new Date());
		currentDate.add(Calendar.DATE, -10);
		currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE), 0, 0, 0);
		Date beginDate = currentDate.getTime();
		currentDate.setTime(new Date());
		currentDate.add(Calendar.DATE, -1);
		currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE), 23, 59, 59);
		Date endDate = currentDate.getTime();
		String category_route = "category_route";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("categoryCode", category_route);

		// 先清空原有数据
		vstSearchSaleDao.deleteAll();
		// by jeanhuang
		vstSearchSaleMuiltDao.deleteAll();

		int currentPageSize = 1000;
		Page<Long> resultPage = null;
		// 线路存子订单表产品list
		List<VstSearchSaleMuilt> routeItemVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> routeItemVstSale = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> routePackVstSale = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> routePackVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> routeVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		String categoryIdStr = getCategodyIdInStr(category_route);
		if (StringUtil.isNotEmptyString(categoryIdStr)) {
			params.put("categoryIdStr", categoryIdStr);
		}
		// 统计酒店套餐、当地游线路总数（orderItem）
		// long routeCount = vstSearchSaleDao.countProductWeekSale(params);
		long routeCount = vstSearchSaleMuiltDao.countProductWeekSaleByDistribute(params);
		logger.info("routeCount value=" + routeCount);
		for (int i = 1; i <= routeCount / currentPageSize + 1; i++) {
			resultPage = Page.page(routeCount, currentPageSize, i);
			params.put("_start", resultPage.getStartRows());
			params.put("_end", resultPage.getEndRows());
			// routeItemVstSale =
			// vstSearchSaleDao.selectProductWeekSale(params);
			routeItemVstSale = vstSearchSaleMuiltDao.selectProductWeekSaleByDistribute(params);
			if (routeItemVstSale != null) {
				routeItemVstSaleAll.addAll(routeItemVstSale);
			}
		}

		// 统计跟团游、自由行线路总数（orderPack）
		// long packRouteCount =
		// vstSearchSaleDao.countPackProductWeekSale(params);
		long packRouteCount = vstSearchSaleMuiltDao.countPackProductWeekSaleByDistribute(params);
		logger.info("packRouteCount value=" + packRouteCount);
		for (int i = 1; i <= packRouteCount / currentPageSize + 1; i++) {
			resultPage = Page.page(packRouteCount, currentPageSize, i);
			params.put("_start", resultPage.getStartRows());
			params.put("_end", resultPage.getEndRows());
			// routePackVstSale =
			// vstSearchSaleDao.selectPackProductWeekSale(params);
			routePackVstSale = vstSearchSaleMuiltDao.selectPackProductWeekSaleByDistribute(params);
			if (routePackVstSale != null) {
				routePackVstSaleAll.addAll(routePackVstSale);
			}
		}

		// 驴途下单的供应商打包跟团游、自由行不在pack中，而在orderItem中，此处需把orderItem中的订单
		// 和pack中的订单相加，防止出现多条记录
		for (VstSearchSaleMuilt itemSale : routeItemVstSaleAll) {
			for (VstSearchSaleMuilt packSale : routePackVstSaleAll) {
				if (itemSale.getProductId().longValue() == packSale.getProductId().longValue()) {
					itemSale.setWeekSale(packSale.getWeekSale() + itemSale.getWeekSale());
					packSale.setProductId(-1L);
					break;
				}
			}
		}
		routeVstSaleAll.addAll(routePackVstSaleAll);
		routeVstSaleAll.addAll(routeItemVstSaleAll);

		// 门票产品list
		List<VstSearchSaleMuilt> ticketItemVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> ticketItemVstSale = null;
		List<VstSearchSaleMuilt> TicketPackVstSale = null;
		List<VstSearchSaleMuilt> ticketPackVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> ticketVstSaleAll = new ArrayList<VstSearchSaleMuilt>();
		// 门票
		params.clear();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("categoryCode", "category_ticket");
		// 统计子订单表门票总数
		// long ticketCount = vstSearchSaleDao.countProductWeekSale(params);
		long ticketCount = vstSearchSaleMuiltDao.countProductWeekSaleByDistribute(params);
		logger.info("ticketCount value=" + ticketCount);
		for (int i = 1; i <= ticketCount / currentPageSize + 1; i++) {
			resultPage = Page.page(ticketCount, currentPageSize, i);
			params.put("_start", resultPage.getStartRows());
			params.put("_end", resultPage.getEndRows());
			// ticketItemVstSale =
			// vstSearchSaleDao.selectProductWeekSale(params);
			ticketItemVstSale = vstSearchSaleMuiltDao.selectProductWeekSaleByDistribute(params);
			if (ticketItemVstSale != null) {
				ticketItemVstSaleAll.addAll(ticketItemVstSale);
			}
		}

		// long packTicketCount =
		// vstSearchSaleDao.countPackProductWeekSale(params);
		long packTicketCount = vstSearchSaleMuiltDao.countPackProductWeekSaleByDistribute(params);
		logger.info("packTicketCount value=" + packTicketCount);
		for (int i = 1; i <= packTicketCount / currentPageSize + 1; i++) {
			resultPage = Page.page(packTicketCount, currentPageSize, i);
			params.put("_start", resultPage.getStartRows());
			params.put("_end", resultPage.getEndRows());
			// TicketPackVstSale =
			// vstSearchSaleDao.selectPackProductWeekSale(params);
			TicketPackVstSale = vstSearchSaleMuiltDao.selectPackProductWeekSaleByDistribute(params);
			if (TicketPackVstSale != null) {
				ticketPackVstSaleAll.addAll(TicketPackVstSale);
			}
		}

		for (VstSearchSaleMuilt itemSale : ticketItemVstSaleAll) {
			for (VstSearchSaleMuilt packSale : ticketPackVstSaleAll) {
				if (itemSale.getProductId().longValue() == packSale.getProductId().longValue()) {
					itemSale.setWeekSale(packSale.getWeekSale() + itemSale.getWeekSale());
					packSale.setProductId(-1L);
					break;
				}
			}
		}

		ticketVstSaleAll.addAll(ticketItemVstSaleAll);
		ticketVstSaleAll.addAll(ticketPackVstSaleAll);

		// 酒店统计
		List<VstSearchSaleMuilt> hotelSale = new ArrayList<VstSearchSaleMuilt>();
		List<VstSearchSaleMuilt> hotelSaleAll = new ArrayList<VstSearchSaleMuilt>();
		params.clear();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("categoryCode", "category_hotel");
		// 统计子订单表门票总数
		// long hotelCount = vstSearchSaleDao.countHotelWeekSale(params);
		ResultHandleT<BizCategory> resultHotelCategory = categoryClientService.findCategoryByCode("category_hotel");
		if (resultHotelCategory.isSuccess()) {
			BizCategory bizHotelCategory = resultHotelCategory.getReturnContent();
			Long hotelCategoryId = bizHotelCategory.getCategoryId();
			params.put("hotelCategoryId", hotelCategoryId);
		}
		long hotelCount = vstSearchSaleMuiltDao.countHotelWeekSaleByDistribute(params);
		logger.info("hotelCount value=" + hotelCount);
		for (int i = 1; i <= hotelCount / currentPageSize + 1; i++) {
			resultPage = Page.page(hotelCount, currentPageSize, i);
			params.put("_start", resultPage.getStartRows());
			params.put("_end", resultPage.getEndRows());
			// hotelSale = vstSearchSaleDao.selectHotelWeekSale(params);
			hotelSale = vstSearchSaleMuiltDao.selectHotelWeekSaleByDistribute(params);
			if (hotelSale != null) {
				hotelSaleAll.addAll(hotelSale);
				logger.info(" 经过过滤数据之后，酒店的销量统计数据是："+hotelSaleAll.size());
			}
		}

		Long routeMaxSale = 0L;
		Long ticketMaxSale = 0L;
		Long hotelMaxSale = 0L;
		if (routeVstSaleAll != null) {
			// 取线路销量最高产品
			for (VstSearchSaleMuilt sale : routeVstSaleAll) {
				if (sale.getProductId().longValue() != -1 && routeMaxSale < sale.getWeekSale()) {
					routeMaxSale = sale.getWeekSale();
				}
				sale.setFlag(setFlag(sale, routeVstSaleAll));
			}

			// 设置缓存为"on"时不设置销量比值为350
			String value = (String) MemcachedUtil.getInstance().get(memcachedKey);
			if (StringUtil.isEmptyString(value) || !"on".equalsIgnoreCase(value)) {
				if (routeMaxSale > 350l) {
					routeMaxSale = 350l;
				}
			} else if ("on".equalsIgnoreCase(value)) {
				// 重新设置缓存为2小时
				MemcachedUtil.getInstance().set(memcachedKey, seconds, memcachedValue);
			}
			
			if (hotelSaleAll != null) {
				// 取门票销量最高产品 酒店只有单出发地
				for (VstSearchSaleMuilt sale : hotelSaleAll) {
					if (hotelMaxSale < sale.getWeekSale()) {
						hotelMaxSale = sale.getWeekSale();
					}
					sale.setFlag("N");
				}

				// 计算单门票销售比
				for (VstSearchSaleMuilt sale : hotelSaleAll) {
					if (sale.getProductId().longValue() != -1) {
						Double salePer = new BigDecimal(sale.getWeekSale().doubleValue() / hotelMaxSale.doubleValue()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
						sale.setSalePer(salePer);
						try {
							// 判断是否是多出发地，对表数据进行存储
							insertData(sale);
						} catch (Exception e) {
							logger.error("error productId:" + sale.getProductId(), e);
						}
					}
				}
			}

			// 计算单产品销售比
			for (VstSearchSaleMuilt sale : routeVstSaleAll) {
				if (sale.getProductId().longValue() != -1) {
					double weekSale = sale.getWeekSale().doubleValue();

					if (StringUtil.isEmptyString(value) || !"on".equalsIgnoreCase(value)) {
						if (weekSale > 350) {
							weekSale = 350;
						}
					} else if ("on".equalsIgnoreCase(value)) {
						// 重新设置缓存为2小时
						MemcachedUtil.getInstance().set(memcachedKey, seconds, memcachedValue);
					}

					Double salePer = new BigDecimal(weekSale / routeMaxSale.doubleValue()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
					sale.setSalePer(salePer);
					try {
						// 判断是否是多出发地，对表数据进行存储
						insertData(sale);
					} catch (Exception e) {
						logger.error("error productId:" + sale.getProductId(), e);
					}

				}

			}
		}

		if (ticketVstSaleAll != null) {
			// 取门票销量最高产品
			for (VstSearchSaleMuilt sale : ticketVstSaleAll) {
				if (sale.getProductId().longValue() != -1 && ticketMaxSale < sale.getWeekSale()) {
					ticketMaxSale = sale.getWeekSale();
				}
				sale.setFlag(setFlag(sale, ticketVstSaleAll));
			}

			// 计算单门票销售比
			for (VstSearchSaleMuilt sale : ticketVstSaleAll) {
				if (sale.getProductId().longValue() != -1) {
					Double salePer = new BigDecimal(sale.getWeekSale().doubleValue() / ticketMaxSale.doubleValue()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
					sale.setSalePer(salePer);
					try {
						// 判断是否是多出发地，对表数据进行存储
						insertData(sale);
					} catch (Exception e) {
						logger.error("error productId:" + sale.getProductId(), e);
					}
				}
			}
		}


	}

	// 判断是否是多出发地，对表数据进行存储
	private void insertData(VstSearchSaleMuilt sale) throws Exception {
		if ("Y".equalsIgnoreCase(sale.getFlag())) {
			// 多出发地产品
			vstSearchSaleMuiltDao.insertSingleSelective(sale);
		} else {
				VstSearchSale vss = new VstSearchSale();
				vss.setProductId(sale.getProductId());
				vss.setQuantitySale(sale.getQuantitySale());
				vss.setSalePer(sale.getSalePer());
				vss.setWeekSale(sale.getWeekSale());
				vstSearchSaleDao.insertSelective(vss);
		}
	}
	
	private String setFlag(VstSearchSaleMuilt vss,List<VstSearchSaleMuilt> list){
		String flag = "N"; //单地出发
		if(vss.getStartDistrictId()!= null && vss.getStartDistrictId() > 0){
			flag = "Y";
		}
		//如果一个产品有多出发地又有单出发地记录则把它归类为多出发地数据
		for(VstSearchSaleMuilt item : list){
			//排除自己
			if(!(item.getProductId().longValue() == vss.getProductId().longValue()&&vss.getWeekSale().equals(item.getWeekSale())&&
					vss.getStartDistrictId().equals(item.getStartDistrictId()))){
				if(item.getProductId().longValue() == vss.getProductId().longValue()){
					item.setFlag("Y");
					flag = "Y"; //多地出发
				}
			}
		}
		return flag;
	}
	
	
	public String getCategodyIdInStr(String categoryCode){
		List<Long> categodyIdList = getCateGodyIdsByCode(categoryCode);
		String sqlStr = "";
		if(CollectionUtils.isNotEmpty(categodyIdList)){
			sqlStr = StringUtil.getOrIn(categodyIdList, "b.category_id");
		}
		return sqlStr;
	}
	
	
	public List<Long> getCateGodyIdsByCode(String categoryCode){
		List<Long> categodyIdList = new ArrayList<Long>();
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("categoryCode", categoryCode);
			ResultHandleT<List<BizCategory>> reultHandle = categoryClientService.findCategoryListByParentCategoryCode(params);
			List<BizCategory> bizCategoryList = reultHandle.getReturnContent();
			if(CollectionUtils.isNotEmpty(bizCategoryList)){
				for (BizCategory bizCategory : bizCategoryList) {
					categodyIdList.add(bizCategory.getCategoryId());
				}
			}
		}catch(Exception e){
			logger.error("categoryClientService findCategoryListByParentCategoryCode error", e);
		}
		return categodyIdList;
	}
	

}
