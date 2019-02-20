package com.lvmama.vst.order.vo;

import java.util.Date;
import java.io.Serializable;
import java.text.ParseException;
import com.lvmama.vst.comm.utils.DateUtil;

import org.apache.commons.lang3.ArrayUtils;
/**
 * 后台订单页酒店推荐VO
 * @author Zhangbin
 *
 */
public class OrderRecommendHotel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2294261456024917373L;
	/**
	 * 入住时间
	 */
	private String startDate;
	/**
	 * 离店时间
	 */
	private String endDate;
	/**
	 * 地图类型
	 */
	private String mapType;
	/**
	 * 排序类型
	 */
	private Integer sortType;
	/**
	 * 酒店星级
	 */
	private String starId;
	/**
	 * 经纬度
	 */
	private String baiduGeo;
	/**
	 * 纬度
	 */
	private String latitude;
	/**
	 * 经度
	 */
	private String longitude;
	/**
	 * 行政区ID
	 */
	private Long districtId;
	/**
	 * 商品ID
	 */
	private String suppGoodsId;
	
	public String getStartDate() {
		return startDate;
	}
	public String getStartDateStr() {
		return getDate(this.startDate);
	}
	public String getStartDateWeekStr() {
		try {
			Date date = DateUtil.converDateFromStr4(this.startDate);
			String zhDay = DateUtil.getZHDay(date);
			return zhDay;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public String getEndDateStr() {
		return getDate(this.endDate);
	}
	public String getEndDateWeekStr() {
		try {
			Date date = DateUtil.converDateFromStr4(this.endDate);
			String zhDay = DateUtil.getZHDay(date);
			return zhDay;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getMapType() {
		return mapType;
	}
	public void setMapType(String mapType) {
		this.mapType = mapType;
	}
	public Integer getSortType() {
		return sortType;
	}
	public void setSortType(Integer sortType) {
		this.sortType = sortType;
	}
	public String getStarId() {
		return starId;
	}
	public void setStarId(String starId) {
		this.starId = starId;
	}
	public boolean isInitQueryTabel() {
		return true;
	}
	public String getBaiduGeo() {
		return baiduGeo;
	}
	public void setBaiduGeo(String baiduGeo) {
		this.baiduGeo = baiduGeo;
		String[] baiduGeoArr = baiduGeo.split(",");
		if(ArrayUtils.isNotEmpty(baiduGeoArr) && baiduGeoArr.length == 2){
			this.latitude = baiduGeoArr[1];
			this.longitude = baiduGeoArr[0];
		}else {
			this.latitude = null;
			this.longitude = null;
		}
	}
	public String getLatitude() {
		return latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	private String getDate(String date) {
		if(null != date) {
			return date.replaceAll("-", "");
		}
		return null;
	}
	public Integer getDays(){
		try {
			Date startDate = DateUtil.converDateFromStr4(this.startDate);
			Date endDate = DateUtil.converDateFromStr4(this.endDate);
			return DateUtil.getDaysBetween(startDate, endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Long getDistrictId() {
		return districtId;
	}
	public void setDistrictId(Long districtId) {
		this.districtId = districtId;
	}
	public String getSuppGoodsId() {
		return suppGoodsId;
	}
	public void setSuppGoodsId(String suppGoodsId) {
		this.suppGoodsId = suppGoodsId;
	}
}
