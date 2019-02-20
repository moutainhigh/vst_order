package com.lvmama.vst.order.web.line.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroupHotel;
import com.lvmama.vst.back.prod.po.ProdPackageGroupLine;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTicket;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTransport;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdPackageGroup.GROUPTYPE;
import com.lvmama.vst.back.prod.po.ProdTrafficFlight;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.supp.vo.SuppGoodsLineTimePriceVo;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.web.line.LineProdPackageGroupContainer;
import com.lvmama.vst.order.web.line.LineUtils;
import com.lvmama.vst.order.web.line.service.LineProdPackageGroupService;

@Service
public class LineProdPackageGroupServiceImpl implements
		LineProdPackageGroupService {

	/**
	 * 商品时间价格表查询服务接口
	 */
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	/**
	 * 大交通组详细信息查询服务接口
	 */
	@Autowired
	private ProdTrafficClientService prodTrafficClientServiceRemote;

	/**
	 * 计算打包产品所有组的时间价格表
	 */
	public LineProdPackageGroupContainer initPackageProductMap(Date specDate,
			Map<String, List<ProdPackageGroup>> packageMap, boolean isSupplier) {

		Assert.notNull(packageMap, "packageMap is null");
		Assert.notNull(specDate, "specDate is null");

		LineProdPackageGroupContainer container = new LineProdPackageGroupContainer();
		List<ProdPackageGroup> allPackageList = new ArrayList<ProdPackageGroup>();
		List<ProdPackageGroup> tempList = null;

		// 得到升级
		tempList = packageMap.get(ProdPackageGroup.GROUPTYPE.UPDATE.name());
		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.UPDATE, isSupplier,container);
			container.setUpdateProdPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		// 得到跟换酒店
		tempList = packageMap.get(ProdPackageGroup.GROUPTYPE.CHANGE.name());
		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.CHANGE, isSupplier,container);
			container.setChangeProdPackageList(tempList);
			allPackageList.addAll(tempList);
		}

		// 得到酒店打包信息
		tempList = packageMap.get(ProdPackageGroup.GROUPTYPE.HOTEL.name());
		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.HOTEL, isSupplier,container);
			container.setHotelProdPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		// 得到交通
		tempList = packageMap.get(ProdPackageGroup.GROUPTYPE.TRANSPORT.name());
		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.TRANSPORT, isSupplier,container);
		
			this.initTrafficBizInfo(tempList);
			container.setTransprotProdPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		// 得到门票打包信息
		tempList = packageMap
				.get(LineProdPackageGroupContainer.LINE_TICKET_single_ticket);

		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE_TICKET, isSupplier,container);
			container.setTicketProdPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		
		// 得到其他门票打包信息
		tempList = packageMap
				.get(LineProdPackageGroupContainer.LINE_TICKET_other_ticket);

		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE_TICKET, isSupplier,container);
			if(CollectionUtils.isNotEmpty(container.getTicketProdPackageList())){
				container.getTicketProdPackageList().addAll(tempList);
			}else{
				container.setTicketProdPackageList(tempList);
			}
			allPackageList.addAll(tempList);
		}
		
		// 得到组合套餐票打包信息
		tempList = packageMap
				.get(LineProdPackageGroupContainer.LINE_TICKET_comb_ticket);
		
		if (tempList != null && tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE_TICKET, isSupplier,container);
			if(CollectionUtils.isNotEmpty(container.getTicketProdPackageList())){
				container.getTicketProdPackageList().addAll(tempList);
			}else{
				container.setTicketProdPackageList(tempList);
			}
			allPackageList.addAll(tempList);
		}
		
		// 得到线路
		tempList = packageMap.get(LineProdPackageGroupContainer.LINE_hotelcomb);
		// 线路酒店套餐
		if (tempList != null&& tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE, isSupplier,container);
			container.setLineHotelCombPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		tempList = packageMap.get(LineProdPackageGroupContainer.LINE_group);
		// 线路跟团游
		if (tempList != null&& tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE, isSupplier,container);
			container.setLineGroupPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		tempList = packageMap.get(LineProdPackageGroupContainer.LINE_freedom);
		// 线路自由行
		if (tempList != null&& tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE, isSupplier,container);
			container.setLineFreedomPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		tempList = packageMap.get(LineProdPackageGroupContainer.LINE_local);
		// 线路当地游
		if (tempList != null&& tempList.size() > 0) {
			this.initProdPackageGroupDateList(tempList, specDate);
			this.initPackageProductBranchList(tempList, specDate,
					GROUPTYPE.LINE, isSupplier,container);
			container.setLineLocalPackageList(tempList);
			allPackageList.addAll(tempList);
		}
		if(CollectionUtils.isNotEmpty(allPackageList)){
			container.setHasPackage(true);
		}
		container.setAllPackageList(allPackageList);
		return container;
	}

	/**
	 * 初始化打包组的时间价格表
	 * 
	 * @param packageProdPackageList
	 * @param specDate
	 * @param type
	 */
	public void initPackageProductBranchList(
			List<ProdPackageGroup> packageProdPackageList, Date specDate,
			GROUPTYPE type, boolean isSupplier,LineProdPackageGroupContainer container) {
		Assert.notNull(packageProdPackageList, "packageProdPackageList is null");
		Assert.notNull(specDate, "specDate is null");
		Assert.notNull(type, "type is null");
		Date specDateTemp = specDate;
		// 信息增加时间价格表
		if (CollectionUtils.isNotEmpty(packageProdPackageList)) {
			for (ProdPackageGroup group : packageProdPackageList) {
				// 如果是门票，需要计算每个组起始日期的时间价格表
				if (group.getGroupType().equals(GROUPTYPE.LINE_TICKET.name())) {
					if (group.getProdPackageGroupTicket() != null
							&& group.getProdPackageGroupTicket().getDateList() != null
							&& group.getProdPackageGroupTicket().getDateList().size() > 0) {
						specDate = DateUtil.toSimpleDate(group.getProdPackageGroupTicket().getDateList().get(0));
					}
				} else if (group.getGroupType().equals(GROUPTYPE.LINE.name())
						|| group.getGroupType().equals(GROUPTYPE.UPDATE.name())
						|| group.getGroupType().equals(GROUPTYPE.CHANGE.name())) {
					if (group.getProdPackageGroupLine() != null
							&& group.getProdPackageGroupLine().getDateList() != null
							&& group.getProdPackageGroupLine().getDateList().size() > 0) {
						specDate = DateUtil.toSimpleDate(group.getProdPackageGroupLine().getDateList().get(0));
					}
				} else if (group.getGroupType().equals(GROUPTYPE.HOTEL.name())) {
					if (group.getProdPackageGroupHotel() != null
							&& group.getProdPackageGroupHotel().getDateList() != null
							&& group.getProdPackageGroupHotel().getDateList().size() > 0) {
						specDate = DateUtil.toSimpleDate(group.getProdPackageGroupHotel().getDateList().get(0));
					}
				}else if (group.getGroupType().equals(GROUPTYPE.TRANSPORT.name())) {
					if(group.getProdPackageGroupTransport() != null){
						if(group.getProdPackageGroupTransport().getToStartDate() != null){
							specDate = DateUtil.toSimpleDate(group.getProdPackageGroupTransport().getToStartDate());
						}else if(group.getProdPackageGroupTransport().getBackStartDate() != null){
							specDate = DateUtil.toSimpleDate(group.getProdPackageGroupTransport().getBackStartDate());
						}
					}
				}
				
				if (CollectionUtils.isNotEmpty(group.getProdPackageDetails())) {
					for (ProdPackageDetail detail : group
							.getProdPackageDetails()) {
						if (detail.getProdProductBranch() != null) {
							Map<String, Long>  map=detail.getProdProductBranch().getSelectPriceMap();
							if(MapUtils.isNotEmpty(map)){
								Map newMap=LineUtils.change(map);
								detail.getProdProductBranch().setSelectPriceMap(newMap);
							}
							
							//设置成人价/儿童价的时间价格Map, 用于页面切换日期后更新成人价/儿童价
							List<SuppGoods> suppGoodsList = detail.getProdProductBranch().getRecommendSuppGoodsList();
							if(group.getGroupType().equals(GROUPTYPE.LINE.name()) && suppGoodsList != null && suppGoodsList.size() > 0) {
								List<SuppGoodsBaseTimePrice> suppGoodsBaseTimePriceList = suppGoodsList.get(0).getSuppGoodsBaseTimePriceList();
								if(suppGoodsBaseTimePriceList != null) {
									for(SuppGoodsBaseTimePrice goodsBaseTimePrice : suppGoodsBaseTimePriceList){
										SuppGoodsLineTimePriceVo lineTimePrice = (SuppGoodsLineTimePriceVo)goodsBaseTimePrice;
										String dateKey = DateUtil.formatSimpleDate(lineTimePrice.getSpecDate());
										
										Long baseAdultPrice = lineTimePrice.getAuditPrice() == null ? 0L : lineTimePrice.getAuditPrice();
										Long baseChildPrice = lineTimePrice.getChildPrice() == null ? 0L : lineTimePrice.getChildPrice();
										
										detail.getProdProductBranch().setAdultPriceMap(dateKey, PriceUtil.trans2YuanStr(baseAdultPrice));
										detail.getProdProductBranch().setChildPriceMap(dateKey, PriceUtil.trans2YuanStr(baseChildPrice));
									}
								}
							}
							
							if (isSupplier) {
								this.initSupplierProdBranchTimePrice(
										detail.getProdProductBranch(),
										specDate, type);
							} else {
								this.initLvmamaProdBranchTimePrice(
										detail.getProdProductBranch(),
										specDate, type, detail);
							}
							//更新时间产品关系
							container.addProductTimeMapItem(detail.getProdProductBranch().getProductId(), specDate);
						}
					}
				}
			}
		}
	}

	/**
	 * 根据日期和线路类型计算商品的时间假期表
	 * 
	 * @param specDate
	 * @param suppGoodsId
	 * @param type
	 * @return
	 */
	public SuppGoodsBaseTimePrice getSuppGoodsBaseTimePrice(Date specDate,
			SuppGoods suppGoods, GROUPTYPE type,boolean hasAperiodic) {
		Assert.notNull(suppGoods, "suppGoods is null");
		Assert.notNull(specDate, "specDate is null");
		Assert.notNull(type, "type is null");

		HashMap<String, Object> goodsMap = new HashMap<String, Object>();
		goodsMap.put("suppGoodsId", suppGoods.getSuppGoodsId());
		goodsMap.put("specDate", specDate);
		goodsMap.put("groupId", suppGoods.getGroupId());
		if (type == GROUPTYPE.LINE_TICKET) {
			// 门票
			if(hasAperiodic){
				//期票
				ResultHandleT<List<SuppGoodsNotimeTimePrice>> handler = suppGoodsTimePriceClientService
						.findSuppGoodsNotimeTimePriceList(goodsMap);
				if (handler != null && handler.isSuccess()
						&& handler.getReturnContent() != null
						&& handler.getReturnContent().size() > 0) {
					return handler.getReturnContent().get(0);
				}
			}else{
				//非期票
				ResultHandleT<List<SuppGoodsAddTimePrice>> handler = suppGoodsTimePriceClientService
						.findSuppGoodsAddTimePriceList(goodsMap);
				if (handler != null && handler.isSuccess()
						&& handler.getReturnContent() != null
						&& handler.getReturnContent().size() > 0) {
					return handler.getReturnContent().get(0);
				}
			}
			
		} else if (type == GROUPTYPE.HOTEL) {
			// 酒店
			ResultHandleT<List<SuppGoodsTimePrice>> handler = suppGoodsTimePriceClientService
					.selectHotelTimePriceCalByParams(goodsMap);
			if (handler != null && handler.isSuccess()
					&& handler.getReturnContent() != null
					&& handler.getReturnContent().size() > 0) {
				return handler.getReturnContent().get(0);
			}
		} else if (type == GROUPTYPE.LINE || type == GROUPTYPE.TRANSPORT || type == GROUPTYPE.CHANGE || type == GROUPTYPE.UPDATE) {
			// 线路和交通
			ResultHandleT<List<SuppGoodsLineTimePriceVo>> handler = suppGoodsTimePriceClientService
					.selectLineTimePriceCalByParams(goodsMap);
			if (handler != null && handler.isSuccess()
					&& handler.getReturnContent() != null
					&& handler.getReturnContent().size() > 0) {
				return handler.getReturnContent().get(0);
			}
		}
		return null;
	}

	/**
	 * 根据行程计算日期列表
	 * 
	 * @param groupList
	 * @param specDate
	 */
	private void initProdPackageGroupDateList(List<ProdPackageGroup> groupList,
			Date specDate) {
		Assert.notNull(groupList, "groupList is null");
		Assert.notNull(specDate, "specDate is null");

		ProdPackageGroupTicket ticketGroup = null;
		ProdPackageGroupLine lineGroup = null;
		ProdPackageGroupTransport transportGroup = null;
		ProdPackageGroupHotel hotelGroup = null;

		for (ProdPackageGroup group : groupList) {
			if (group.getGroupType().equals(GROUPTYPE.LINE_TICKET.name())) {
				ticketGroup = group.getProdPackageGroupTicket();
				if (ticketGroup != null) {
					String startDay = ticketGroup.getStartDay();
					String[] days = startDay.split(",");
					List<String> dateList = new ArrayList<String>();
					if (days != null && days.length > 0) {
						for (String day : days) {
							Date lineSpecDate = DateUtil.DsDay_HourOfDay(
									specDate, (Integer.parseInt(day) - 1) * 24);
							String lineSpecDateStr = DateUtil
									.formatSimpleDate(lineSpecDate);
							dateList.add(lineSpecDateStr);
						}
						ticketGroup.setDateList(dateList);
						if (dateList.size() > 0) {
							group.setStartDay(dateList.get(0));
							group.setEndDay(dateList.get(dateList.size() - 1));
						}
					}
				}
				
			} else if (group.getGroupType().equals(GROUPTYPE.LINE.name())
					|| group.getGroupType().equals(GROUPTYPE.UPDATE.name())) {
				lineGroup = group.getProdPackageGroupLine();
				if (lineGroup != null) {
					String startDay = lineGroup.getStartDay();
					String[] days = startDay.split(",");
					List<String> dateList = new ArrayList<String>();
					if (days != null && days.length > 0) {
						for (String day : days) {
							Date lineSpecDate = DateUtil.DsDay_HourOfDay(
									specDate, (Integer.parseInt(day) - 1) * 24);
							String lineSpecDateStr = DateUtil
									.formatSimpleDate(lineSpecDate);
							dateList.add(lineSpecDateStr);
						}
						lineGroup.setDateList(dateList);
						if (dateList.size() > 0) {
							group.setStartDay(dateList.get(0));
							group.setEndDay(dateList.get(dateList.size() - 1));
						}
					}
				}
			} else if (group.getGroupType().equals(GROUPTYPE.HOTEL.name())
					|| group.getGroupType().equals(GROUPTYPE.CHANGE.name())) {
				lineGroup = group.getProdPackageGroupLine();
				Date lineSpecDate0 = specDate;
				if (lineGroup != null) {
					String startDay = lineGroup.getStartDay();
					String[] days = startDay.split(",");
					List<String> dateList = new ArrayList<String>();
					if (days != null && days.length > 0) {
						for (String day : days) {
							lineSpecDate0 = DateUtil.DsDay_HourOfDay(
									specDate, (Integer.parseInt(day) - 1) * 24);
							String lineSpecDateStr = DateUtil
									.formatSimpleDate(lineSpecDate0);
							dateList.add(lineSpecDateStr);
						}
						lineGroup.setDateList(dateList);
						if (dateList.size() > 0) {
							group.setStartDay(dateList.get(0));
							group.setEndDay(dateList.get(dateList.size() - 1));
						}
					}
				}
				hotelGroup = group.getProdPackageGroupHotel();
				if (hotelGroup != null) {
					String startDay = hotelGroup.getStayDays();
					String[] days = startDay.split(",");
					List<String> dateList = new ArrayList<String>();
					if (days != null && days.length > 0) {
						for (String day : days) {
							Date lineSpecDate = DateUtil.DsDay_HourOfDay(
									lineSpecDate0, (Integer.parseInt(day) - 1) * 24);
							String lineSpecDateStr = DateUtil
									.formatSimpleDate(lineSpecDate);
							dateList.add(lineSpecDateStr);
						}
						hotelGroup.setDateList(dateList);
						if (dateList.size() > 0) {
							group.setStartDay(dateList.get(0));
							group.setEndDay(dateList.get(dateList.size() - 1));
							hotelGroup.setArriveDate(group.getStartDay());
							Date leave = DateUtil.DsDay_HourOfDay(
									DateUtil.toSimpleDate(group.getEndDay()),
									24);
							hotelGroup.setLeaveDate(DateUtil
									.formatSimpleDate(leave));
						}
					}
				}
			} else if (group.getGroupType().equals(GROUPTYPE.TRANSPORT.name())) {
				transportGroup = group.getProdPackageGroupTransport();
				if (transportGroup != null) {
					if (transportGroup.getToStartDays() != null) {
						Date lineSpecDate = DateUtil
								.DsDay_HourOfDay(specDate, (transportGroup
										.getToStartDays().intValue() - 1) * 24);
						transportGroup.setToStartDate(DateUtil
								.formatSimpleDate(lineSpecDate));
					}
					if (transportGroup.getBackStartDays() != null) {
						Date lineSpecDate = DateUtil
								.DsDay_HourOfDay(specDate,
										(transportGroup.getBackStartDays()
												.intValue() - 1) * 24);
						transportGroup.setBackStartDate(DateUtil
								.formatSimpleDate(lineSpecDate));
					}
				}
			}
		}
	}

	/**
	 * 为大交通初始化
	 */
	public void initTrafficBizInfo(
			List<ProdPackageGroup> tracfficPackageGroupList) {
		if (CollectionUtils.isEmpty(tracfficPackageGroupList)) {
			return;
		}
		ProdTrafficVO prodTrafficVO = null;
		
		for (ProdPackageGroup group : tracfficPackageGroupList) {
			if("Y".equals(group.getJiPiaoDuiJieFlag())){
				continue;
			}
			
			List<ProdPackageDetail> details = group.getProdPackageDetails();
			if(CollectionUtils.isEmpty(details)){
				continue;
			}
				
			for (ProdPackageDetail detail : details) {
				if(detail.getProdProduct() == null){
					continue;
				}
				
				prodTrafficVO = prodTrafficClientServiceRemote
						.getProdTrafficVOByProductId(detail
								.getProdProduct().getProductId());
				
				if(prodTrafficVO == null || CollectionUtils.isEmpty(prodTrafficVO.getProdTrafficGroupList())){
					continue;
				}
				
				for(ProdTrafficGroup p:prodTrafficVO.getProdTrafficGroupList()){
					if(CollectionUtils.isEmpty(p.getProdTrafficFlightList())){
						continue;
					}

					for (ProdTrafficFlight pf: p.getProdTrafficFlightList()) {
						Long fligtTime=null;
						if (pf.getBizFlight()!=null) {
							fligtTime=pf.getBizFlight().getFlightTime();
						}
						if(fligtTime!=null){
							Long flyHours=fligtTime/60;
							Long flyM=fligtTime%60;
							pf.getBizFlight().setFlightTimeHour(flyHours.toString());
							pf.getBizFlight().setFlightTimeMinute(flyM.toString());
						}
					}
				}
				
				detail.getProdProduct().setProdTrafficVO(
						prodTrafficVO);
			}
		}
	}

	/**
	 * 计算供应商产品规格的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 * @param packageDetail
	 */
	public void initSupplierProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type) {
		Assert.notNull(prodBranch, "prodBranch is null");
		Assert.notNull(specDate, "specDate is null");
		Assert.notNull(type, "type is null");

		if (prodBranch != null) {
			if (CollectionUtils.isNotEmpty(prodBranch.getSuppGoodsList())) {
				if (prodBranch.getAdultPrice() == null) {
					prodBranch.setAdultPrice(0L);
				}
				if (prodBranch.getChildPrice() == null) {
					prodBranch.setChildPrice(0L);
				}
				if (prodBranch.getGapPrice() == null) {
					prodBranch.setGapPrice(0L);
				}
				for (SuppGoods goods : prodBranch.getSuppGoodsList()) {
					if (goods.getAdultPrice() != null) {
						prodBranch.setAdultPrice(prodBranch.getAdultPrice()
								+ goods.getAdultPrice());
					}
					if (goods.getChildPrice() != null) {
						prodBranch.setChildPrice(prodBranch.getChildPrice()
								+ goods.getChildPrice());
					}
					if (goods.getGapPrice() != null) {
						prodBranch.setGapPrice(prodBranch.getGapPrice()
								+ goods.getGapPrice());
					}
					goods.setSuppGoodsBaseTimePrice(this
							.getSuppGoodsBaseTimePrice(specDate,
									goods, type,goods.hasAperiodic()));
				}
			}
		}
	}

	/**
	 * 计算驴妈妈产品规格的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 * @param packageDetail
	 */
	public void initLvmamaProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type, ProdPackageDetail packageDetail) {
		Assert.notNull(prodBranch, "prodBranch is null");
		Assert.notNull(specDate, "specDate is null");
		Assert.notNull(type, "type is null");

		if (prodBranch != null) {
			if (CollectionUtils.isNotEmpty(prodBranch.getSuppGoodsList())) {
				if (prodBranch.getAdultPrice() == null) {
					prodBranch.setAdultPrice(0L);
				}
				if (prodBranch.getChildPrice() == null) {
					prodBranch.setChildPrice(0L);
				}
				if (prodBranch.getGapPrice() == null) {
					prodBranch.setGapPrice(0L);
				}
				for (SuppGoods goods : prodBranch.getSuppGoodsList()) {
					if (goods.getAdultPrice() != null) {
						prodBranch.setAdultPrice(prodBranch.getAdultPrice()
								+ goods.getAdultPrice());
					}
					if (goods.getChildPrice() != null) {
						prodBranch.setChildPrice(prodBranch.getChildPrice()
								+ goods.getChildPrice());
					}
					if (goods.getGapPrice() != null) {
						prodBranch.setGapPrice(prodBranch.getGapPrice()
								+ goods.getGapPrice());
					}
					goods.setSuppGoodsBaseTimePrice(this
							.getSuppGoodsBaseTimePrice(specDate,
									goods, type,goods.hasAperiodic()));
					
					/*******************需要判断单价格、多价格******************/
					if (type == GROUPTYPE.LINE_TICKET) {
						// 门票
						//分为期票和非期票
						if(goods.hasAperiodic()){
							SuppGoodsNotimeTimePrice noTimePrice=(SuppGoodsNotimeTimePrice) goods.getSuppGoodsBaseTimePrice();
							if(noTimePrice!=null){
								noTimePrice.setPrice(OrderUtils.fillPackageOrderItemPrice(noTimePrice.getSettlementPrice(), noTimePrice.getPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
							}							
						}else{
							SuppGoodsAddTimePrice addTimePrice=(SuppGoodsAddTimePrice) goods.getSuppGoodsBaseTimePrice();
							if(addTimePrice!=null){
								addTimePrice.setPrice(OrderUtils.fillPackageOrderItemPrice(addTimePrice.getSettlementPrice(), addTimePrice.getPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
							}
						}
					} else if (type == GROUPTYPE.HOTEL) {
						// 酒店
						SuppGoodsTimePrice timePrice=(SuppGoodsTimePrice) goods.getSuppGoodsBaseTimePrice();
						if(timePrice!=null){
							timePrice.setPrice(OrderUtils.fillPackageOrderItemPrice(timePrice.getSettlementPrice(), timePrice.getPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
							
						}
					} else if (type == GROUPTYPE.LINE || type == GROUPTYPE.TRANSPORT || type == GROUPTYPE.CHANGE || type == GROUPTYPE.UPDATE) {
						// 线路和交通
						SuppGoodsLineTimePriceVo lineTimePrice=(SuppGoodsLineTimePriceVo) goods.getSuppGoodsBaseTimePrice();
						if(lineTimePrice!=null){							
							lineTimePrice.setAuditPrice(OrderUtils.fillPackageOrderItemPrice(lineTimePrice.getAuditSettlementPrice(), lineTimePrice.getAuditPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
							lineTimePrice.setChildPrice(OrderUtils.fillPackageOrderItemPrice(lineTimePrice.getChildSettlementPrice(), lineTimePrice.getChildPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
							//lineTimePrice.setGapPrice(OrderUtils.fillPackageOrderItemPrice(lineTimePrice.getGrapSettlementPrice(), lineTimePrice.getGapPrice(), packageDetail.getPrice(), packageDetail.getPriceType()));
						}
					}
					
				}
			}
		}
	}

}
