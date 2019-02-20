package com.lvmama.vst.order.web.vo;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.lvmama.comm.search.ast.IndexingUtils;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by zhouyanqun on 2016/6/27.
 *
 * 线路后台下单，产品查询的返回结果
 */
public class OrderLineProductVO implements Serializable {

    private String			productId;
    private String            categoryId;
    private String            subCategoryId;
    private String			productName;
    private String			categoryName;
    private String			managerId;
    private String			bU;
    private String			recommendLevel;
    // 是否有效
    private String			cancelFlag;
    // 是否可售
    private String			saleFlag;
    // 产品类型
    private String			productType;
    // 打包类型
    private String			packageType;
    // 创建时间
    private String			createTime;
    // URL ID
    private String			urlId;

    /*
     * 产品图片相关字段
     */
    // 图片URL
    private String			photoUrl;
    // 图片内容
    private String			photoContent;
    private String videoPhotoUrl;

    /*
     * 出发地行政区相关字段
     */
    // 行政区ID(出发地)
    private String			districtId;
    //计算筛选列表用（不用于索引和搜索）
    private String			districtIds;
    //计算筛选列表用（不用于索引和搜索）
    private String			districtNames;
    // 行政区名称
    private String			districtName;
    // 所属省份行政区ID
    private String			provinceDistId;
    // 所属国家行政区ID
    private String			countryDistId;

    /*
     * 目的地相关字段
     */
    // 目的地ID
    private String			destId;

    // 目的地类型
    private String			destType;
    // 目的地名称
    private String			destName;
    // 景点聚合字段，现不用于搜索
    private String			viewPiont;
    // 目的地英文
    private String			enName;
    // 目的地拼音
    private String			pinyin;
    // 目的地简拼
    private String			shortPinyin;
    // 目的地别名
    private String			destAlias;
    // 主目的地上级（目前没有所属县/区，以此字段代替）
    private String			parentId;

    /*
     * 目的地行政区相关字段
     */
    // 所属城市目的地ID
    private String			cityDestId;
    // 所属城市名称
    private String			cityName;
    // 所属城市拼音
    private String			cityPinyin;
    // 所属省份目的地ID
    private String			provinceDestId;
    // 所属省份名称
    private String			provinceName;
    // 所属省份拼音
    private String			provincePinyin;
    // 所属国家目的地ID
    private String			countryDestId;
    // 所属国家名称
    private String			countryName;
    // 所属国家拼音
    private String			countryPinyin;
    // 所属大洲目的地ID
    private String			continentDestId;
    // 所属大洲名称
    private String			continentName;
    // 所属大洲拼音
    private String			continentPinyin;

    /*
     * 线路专用
     */
    // 多行程标志
    private String			multRouteFlag;
    // 行程天数
    private String			routeNum;
    // 行程晚数
    private String			stayNights;
    // 成团人数
    private Integer		leastClusterPerson;
    // 出发日期
    private String			startDates;
    // 出发月份
    private String			startMonths;
    // 出发年月
    private String			startYearMonth;
    // 出发年月日
    private String			startYearMonthDay;

    // 产品特色
    private String			routeFeature;
    // 住宿类型
    private String			stayTypes;
    // 住宿
    private String			hotel;
    // 住宿是否可换
    private String			changeHotelFlag;
    // 去程交通
    private String			trafficTo;
    // 返程交通
    private String			trafficBack;
    // 交通筛选字段
    private String			traffic;
    // 是否有大交通
    private String			trafficFlag;
    // 交通是否可换
    private String			changeTrafficFlag;
    // 跟团类型ID
    private String			groupTypeId;
    // 跟团类型VALUE
    private String			groupTypeValue;
    // 套餐类型ID
    private String			packageTypeId;
    // 套餐类型VALUE
    private String			packageTypeValue;

    /*
     * 多出发地新增字段
     */
    // 多出发地标志
    private String			multDepartureFlag;
    // 对应多出发地的销售起价
    private String			sellPrices;
    // 对应多出发地的团购起价
    private String			groupPrices;
    // 对应多出发地的秒杀起价
    private String			spikeTypePrices;
    // 对应多出发地的分销起价
    private String			distributorPrices;
    // 对应多出发地的驴途起价
    private String			lvmamaClientPrices;

    /*
     * 主题相关字段（集合）
     */
    // 主题ID
    private String			subjectId;
    // 主题排序
    private String			subjectSeq;
    // 主题名称
    private String			subjectName;
    // 主题拼音
    private String			subjectPinyin;

    /*
     * 标签相关字段（集合）
     */
    // 标签排序
    private String			tagSeq;
    // 标签名称
    private String			tagName;
    // 标签拼音
    private String			tagPinyin;
    // 标签描述
    private String			tagMemo;
    // 标签比例（*1000）
    private Double			tagPercent;

    /*
     * 点评相关字段
     */
    // 点评数
    private String			commentNum;
    // 好评率
    private Double			commentGood;

    /*
     * 促销相关字段
     */
    // 促销标志
    private String			promotionFlag;
    // 促销类型(优惠类型)(最多只显示4个)
    private String			promotionTypes;
    private String            promotionTitle;
    // 买赠标志
    private String			buyPresentFlag;
    //预售标志
    private String			presellFlag;

    /*
     * 返现相关字段
     */
    // 最高返现（PC）
    private Double			maxRebateAmountPc;
    // 最高返现（Mobile）
    private Double			maxRebateAmountMobile;

    /*
     * 起价相关字段
     */
    // 市场起价（单位：元）
    private Float			 marketPrice;
    /*
     * 最终显示的起价，总共考虑三个渠道，即前台(3)、团购(108)、秒杀(110)
     * 如果有三个渠道，则取前台起价sellPrice
     * 如果有两个渠道，则依次取团购、秒杀、前台
     * 如果有一个渠道，则取对应渠道的价格
     * （单位：元）
     */
    private Float			 sellPrice;
    // 团购起价（单位：元）
    private Float			 groupPrice;
    // 秒杀起价（单位：元）
    private Float			 spikeTypePrice;
    // 分销起价（单位：分）
    private Float			 distributorPrice;
    // 驴途起价（单位：分）
    private Float			 lvmamaClientPrice;

    /*
     * 其他字段
     */
    // 分销渠道
    private String			distributorId;
    // 售卖方式
    private String			saleType;
    // 十天销量比(*100000.0)
    private Double			salePer;
    // 佣金
    private Double			commission;
    // 产品总分值
    private Double			productNum;
    // 主题标志
    private Integer			themeFlag;

    /*包含酒店信息*/
    private String hotelId;
    // 百度经度(存放了所有打包酒店的经度，prepare方法中将其第一个有效经度放入hotelBaiduLongitude，今后此字段可不如索引)
    private String hotelBaiduLongitudes;
    // 百度纬度(存放了所有打包酒店的维度，prepare方法中将其第一个有效纬度放入hotelBaiduLatitude，今后此字段可不如索引)
    private String hotelBaiduLatitudes;
    // 百度经度
    private Double hotelBaiduLongitude;
    // 百度纬度
    private Double hotelBaiduLatitude;

    // 酒店名称
    private String hotelName;

    // 酒店星级
    private String hotelStar;

    // 行程概览
    private String routeDetail;

    private String location;
    // 距离
    private Double distance;

    private Integer experienceCount;

    private Double profit;

    //类型
    private String tourTypeId;
    private String managerName;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategoryId() {
        return this.subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getBU() {
        return bU;
    }

    public void setBU(String BU) {
        this.bU = BU;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getRecommendLevel() {
        return recommendLevel;
    }

    public void setRecommendLevel(String recommendLevel) {
        this.recommendLevel = recommendLevel;
    }

    public String getCancelFlag() {
        return cancelFlag;
    }

    public void setCancelFlag(String cancelFlag) {
        this.cancelFlag = cancelFlag;
    }

    public String getSaleFlag() {
        return saleFlag;
    }

    public void setSaleFlag(String saleFlag) {
        this.saleFlag = saleFlag;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoContent() {
        return photoContent;
    }

    public void setPhotoContent(String photoContent) {
        this.photoContent = photoContent;
    }

    public String getVideoPhotoUrl() {
        return this.videoPhotoUrl;
    }

    public void setVideoPhotoUrl(String videoPhotoUrl) {
        this.videoPhotoUrl = videoPhotoUrl;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getDistrictIds() {
        return districtIds;
    }

    public void setDistrictIds(String districtIds) {
        this.districtIds = districtIds;
    }

    public String getDistrictNames() {
        return districtNames;
    }

    public void setDistrictNames(String districtNames) {
        this.districtNames = districtNames;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getProvinceDistId() {
        return provinceDistId;
    }

    public void setProvinceDistId(String provinceDistId) {
        this.provinceDistId = provinceDistId;
    }

    public String getCountryDistId() {
        return countryDistId;
    }

    public void setCountryDistId(String countryDistId) {
        this.countryDistId = countryDistId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getDestType() {
        return destType;
    }

    public void setDestType(String destType) {
        this.destType = destType;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getShortPinyin() {
        return shortPinyin;
    }

    public void setShortPinyin(String shortPinyin) {
        this.shortPinyin = shortPinyin;
    }

    public String getDestAlias() {
        return destAlias;
    }

    public void setDestAlias(String destAlias) {
        this.destAlias = destAlias;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCityDestId() {
        return cityDestId;
    }

    public void setCityDestId(String cityDestId) {
        this.cityDestId = cityDestId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityPinyin() {
        return cityPinyin;
    }

    public void setCityPinyin(String cityPinyin) {
        this.cityPinyin = cityPinyin;
    }

    public String getProvinceDestId() {
        return provinceDestId;
    }

    public void setProvinceDestId(String provinceDestId) {
        this.provinceDestId = provinceDestId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvincePinyin() {
        return provincePinyin;
    }

    public void setProvincePinyin(String provincePinyin) {
        this.provincePinyin = provincePinyin;
    }

    public String getCountryDestId() {
        return countryDestId;
    }

    public void setCountryDestId(String countryDestId) {
        this.countryDestId = countryDestId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryPinyin() {
        return countryPinyin;
    }

    public void setCountryPinyin(String countryPinyin) {
        this.countryPinyin = countryPinyin;
    }

    public String getContinentDestId() {
        return continentDestId;
    }

    public void setContinentDestId(String continentDestId) {
        this.continentDestId = continentDestId;
    }

    public String getContinentName() {
        return continentName;
    }

    public void setContinentName(String continentName) {
        this.continentName = continentName;
    }

    public String getContinentPinyin() {
        return continentPinyin;
    }

    public void setContinentPinyin(String continentPinyin) {
        this.continentPinyin = continentPinyin;
    }

    public String getMultRouteFlag() {
        return multRouteFlag;
    }

    public void setMultRouteFlag(String multRouteFlag) {
        this.multRouteFlag = multRouteFlag;
    }

    public String getRouteNum() {
        return routeNum;
    }

    public void setRouteNum(String routeNum) {
        this.routeNum = routeNum;
    }

    public String getStayNights() {
        return stayNights;
    }

    public void setStayNights(String stayNights) {
        this.stayNights = stayNights;
    }

    public Integer getLeastClusterPerson() {
        return leastClusterPerson;
    }

    public void setLeastClusterPerson(Integer leastClusterPerson) {
        this.leastClusterPerson = leastClusterPerson;
    }

    public String getStartDates() {
        return startDates;
    }

    public void setStartDates(String startDates) {
        this.startDates = startDates;
    }

    public String getStartMonths() {
        return startMonths;
    }

    public void setStartMonths(String startMonths) {
        this.startMonths = startMonths;
    }

    public String getRouteFeature() {
        return routeFeature;
    }

    public void setRouteFeature(String routeFeature) {
        this.routeFeature = routeFeature;
    }

    public String getStayTypes() {
        return stayTypes;
    }

    public void setStayTypes(String stayTypes) {
        this.stayTypes = stayTypes;
    }

    public String getHotel() {
        return hotel;
    }

    public void setHotel(String hotel) {
        this.hotel = hotel;
    }

    public String getChangeHotelFlag() {
        return changeHotelFlag;
    }

    public void setChangeHotelFlag(String changeHotelFlag) {
        this.changeHotelFlag = changeHotelFlag;
    }

    public String getTrafficTo() {
        return trafficTo;
    }

    public void setTrafficTo(String trafficTo) {
        this.trafficTo = trafficTo;
    }

    public String getTrafficBack() {
        return trafficBack;
    }

    public void setTrafficBack(String trafficBack) {
        this.trafficBack = trafficBack;
    }

    public String getTrafficFlag() {
        return trafficFlag;
    }

    public void setTrafficFlag(String trafficFlag) {
        this.trafficFlag = trafficFlag;
    }

    public String getChangeTrafficFlag() {
        return changeTrafficFlag;
    }

    public void setChangeTrafficFlag(String changeTrafficFlag) {
        this.changeTrafficFlag = changeTrafficFlag;
    }

    public String getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(String groupTypeId) {
        this.groupTypeId = groupTypeId;
    }

    public String getGroupTypeValue() {
        return groupTypeValue;
    }

    public void setGroupTypeValue(String groupTypeValue) {
        this.groupTypeValue = groupTypeValue;
    }

    public String getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(String packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public String getPackageTypeValue() {
        return packageTypeValue;
    }

    public void setPackageTypeValue(String packageTypeValue) {
        this.packageTypeValue = packageTypeValue;
    }

    public String getMultDepartureFlag() {
        return multDepartureFlag;
    }

    public void setMultDepartureFlag(String multDepartureFlag) {
        this.multDepartureFlag = multDepartureFlag;
    }

    public String getSellPrices() {
        return sellPrices;
    }

    public void setSellPrices(String sellPrices) {
        this.sellPrices = sellPrices;
    }

    public String getGroupPrices() {
        return groupPrices;
    }

    public void setGroupPrices(String groupPrices) {
        this.groupPrices = groupPrices;
    }

    public String getSpikeTypePrices() {
        return spikeTypePrices;
    }

    public void setSpikeTypePrices(String spikeTypePrices) {
        this.spikeTypePrices = spikeTypePrices;
    }

    public String getDistributorPrices() {
        return distributorPrices;
    }

    public void setDistributorPrices(String distributorPrices) {
        this.distributorPrices = distributorPrices;
    }

    public String getLvmamaClientPrices() {
        return lvmamaClientPrices;
    }

    public void setLvmamaClientPrices(String lvmamaClientPrices) {
        this.lvmamaClientPrices = lvmamaClientPrices;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectSeq() {
        return subjectSeq;
    }

    public void setSubjectSeq(String subjectSeq) {
        this.subjectSeq = subjectSeq;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectPinyin() {
        return subjectPinyin;
    }

    public void setSubjectPinyin(String subjectPinyin) {
        this.subjectPinyin = subjectPinyin;
    }

    public String getTagSeq() {
        return tagSeq;
    }

    public void setTagSeq(String tagSeq) {
        this.tagSeq = tagSeq;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagPinyin() {
        return tagPinyin;
    }

    public void setTagPinyin(String tagPinyin) {
        this.tagPinyin = tagPinyin;
    }

    public String getTagMemo() {
        return tagMemo;
    }

    public void setTagMemo(String tagMemo) {
        this.tagMemo = tagMemo;
    }

    public Double getTagPercent() {
        return tagPercent;
    }

    public void setTagPercent(Double tagPercent) {
        this.tagPercent = tagPercent;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public Double getCommentGood() {
        return commentGood;
    }

    public void setCommentGood(Double commentGood) {
        this.commentGood = commentGood;
    }

    public String getPromotionFlag() {
        return promotionFlag;
    }

    public void setPromotionFlag(String promotionFlag) {
        this.promotionFlag = promotionFlag;
    }

    public String getPromotionTypes() {
        return promotionTypes;
    }

    public void setPromotionTypes(String promotionTypes) {
        this.promotionTypes = promotionTypes;
    }
    public String getPromotionTitle() {
        return this.promotionTitle;
    }

    public void setPromotionTitle(String promotionTitle) {
        this.promotionTitle = promotionTitle;
    }

    public String getBuyPresentFlag() {
        return this.buyPresentFlag;
    }

    public void setBuyPresentFlag(String buyPresentFlag) {
        this.buyPresentFlag = buyPresentFlag;
    }

    public Double getMaxRebateAmountPc() {
        return maxRebateAmountPc;
    }

    public void setMaxRebateAmountPc(Double maxRebateAmountPc) {
        this.maxRebateAmountPc = maxRebateAmountPc;
    }

    public Double getMaxRebateAmountMobile() {
        return maxRebateAmountMobile;
    }

    public void setMaxRebateAmountMobile(Double maxRebateAmountMobile) {
        this.maxRebateAmountMobile = maxRebateAmountMobile;
    }

    public Float getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(Float marketPrice) {
        this.marketPrice = marketPrice;
    }

    public Float getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Float sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Float getGroupPrice() {
        return groupPrice;
    }

    public void setGroupPrice(Float groupPrice) {
        this.groupPrice = groupPrice;
    }

    public Float getSpikeTypePrice() {
        return spikeTypePrice;
    }

    public void setSpikeTypePrice(Float spikeTypePrice) {
        this.spikeTypePrice = spikeTypePrice;
    }

    public Float getDistributorPrice() {
        return distributorPrice;
    }

    public void setDistributorPrice(Float distributorPrice) {
        this.distributorPrice = distributorPrice;
    }

    public Float getLvmamaClientPrice() {
        return lvmamaClientPrice;
    }

    public void setLvmamaClientPrice(Float lvmamaClientPrice) {
        this.lvmamaClientPrice = lvmamaClientPrice;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public Double getSalePer() {
        return salePer;
    }

    public void setSalePer(Double salePer) {
        this.salePer = salePer;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Double getHotelBaiduLongitude() {
        return hotelBaiduLongitude;
    }

    public void setHotelBaiduLongitude(Double hotelBaiduLongitude) {
        this.hotelBaiduLongitude = hotelBaiduLongitude;
    }

    public Double getHotelBaiduLatitude() {
        return hotelBaiduLatitude;
    }

    public void setHotelBaiduLatitude(Double hotelBaiduLatitude) {
        this.hotelBaiduLatitude = hotelBaiduLatitude;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public Double getProductNum() {
        return productNum;
    }

    public void setProductNum(Double productNum) {
        this.productNum = productNum;
    }

    public Integer getThemeFlag() {
        return themeFlag;
    }

    public void setThemeFlag(Integer themeFlag) {
        this.themeFlag = themeFlag;
    }

    public Integer getExperienceCount() {
        return this.experienceCount;
    }

    public void setExperienceCount(Integer experienceCount) {
        this.experienceCount = experienceCount;
    }
    public Double getProfit() {
        return this.profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    /*
	 * XXX 满足特殊需要的一些get方法
	 */
    /**
     * 是否是天天出发，如果三十天内，有超过二十天的出发日期，则算天天出发
     */
    public Boolean getIsEveryDayStart() {
        if (StringUtil.isEmptyString(startDates)) {
            return false;
        }
        String[] startDatesArr = startDates.split(",");
        if (startDatesArr.length < 20) {
            return false;
        }
        Date date1 = DateUtil.getDateByStr(startDatesArr[0].trim(), "yy/MM/dd");
        Date date2 = DateUtil.getDateByStr(startDatesArr[19].trim(), "yy/MM/dd");
        long diffDay = DateUtil.diffDay(date1, date2);
        // 如果当前起到第二十个出发日期，没超过三十天，则算天天出发
        if (diffDay <= 30) {
            return true;
        }
        return false;
    }

    public String getFormattedStartDates() {
        if (StringUtil.isEmptyString(startDates) || getIsEveryDayStart()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        String startDateText = "";
        String[] startDatesArr = startDates.split(",");
        for (int i = 0; i < startDatesArr.length; i++) {
            if (i > 5) {
                break;
            }
            String str = "";
            if (startDatesArr[i].length() > 6) {
                str = startDatesArr[i].trim().substring(3);
            } else {
                str = startDatesArr[i].trim();
            }
            sb.append(str);
            if (i < 5) {
                sb.append(",");
            }
        }
        startDateText = sb.toString();
        if (startDateText.endsWith(",")) {
            startDateText = startDateText.substring(0, startDateText.length() - 1);
        }
        return startDateText;
    }

    public String getHotelLeavlName() {
        StringBuilder hotelLeavlName = new StringBuilder();
        if (StringUtils.equalsIgnoreCase("Y", multRouteFlag) && StringUtils.isNotBlank(stayTypes)) {
            for (String stayType : StringUtils.split(stayTypes, ",")) {
                int hotelLeval = 0;
                try {
                    hotelLeval = Integer.parseInt(stayType);
                    hotelLeavlName.append(this.getLeavlName(hotelLeval));
                } catch (NumberFormatException e) {
                    hotelLeavlName.append("," + stayType);
                }
            }
        } else if (StringUtils.isNotBlank(hotel)) {
            int hotelLeval = 0;
            try {
                hotelLeval = Integer.parseInt(hotel);
                hotelLeavlName.append(this.getLeavlName(hotelLeval));
            } catch (NumberFormatException e) {
                hotelLeavlName.append("," + hotel);
            }
        }
        if (hotelLeavlName.length() > 0) {
            hotelLeavlName.deleteCharAt(0);
        }
        return hotelLeavlName.toString();
    }

    public String getLeavlName(int hotelLeval) {
        String leavlName = ",";
        switch (hotelLeval) {
            case 360:
                leavlName += "其他";
                break;
            case 361:
                leavlName += "客栈";
                break;
            case 362:
                leavlName += "农家院";
                break;
            case 363:
                leavlName += "二星或同等酒店";
                break;
            case 364:
                leavlName += "三星或同等酒店";
                break;
            case 365:
                leavlName += "四星或同等酒店";
                break;
            case 366:
                leavlName += "五星或同等酒店";
                break;
            case 367:
                leavlName += "六星或同等酒店";
                break;
            case 368:
                leavlName += "七星或同等酒店";
                break;
            case 369:
                leavlName += "八星或同等酒店";
                break;
            case 370:
                leavlName += "住在交通工具上";
                break;
            case 371:
                leavlName += "酒店转机/住宿";
                break;
            default:
                leavlName += "";
                break;
        }
        return leavlName;
    }

    /**
     * 产品类型转义
     */
    public String getProductTypeForCnName() {
        if (StringUtils.isNotEmpty(productType)) {
            ProdProduct.PRODUCTTYPE p = ProdProduct.PRODUCTTYPE.valueOf(productType.toUpperCase());
            if (p == ProdProduct.PRODUCTTYPE.FOREIGNLINE) {
                return "出境";
            } else {
                return "国内";
            }
        } else {
            return "";
        }
    }

    /**
     * 产品类型转义
     */
    public int getProductTypeForIndex() {
        return ProdProduct.PRODUCTTYPE.getIndexByCode(productType);
    }

    public String getFormattedMarketPrice() {
        return IndexingUtils.formatMoney(marketPrice);
    }

    public String getFormattedSellPrice() {
        return IndexingUtils.formatMoney(sellPrice).replaceAll(",","");
    }

    public String getTrafficToCnName() {
        if (StringUtil.isNotEmptyString(trafficTo)) {
            return Constant.LINE_TRAFFIC.getCnName(trafficTo);
        }
        return "";
    }

    public String getTrafficBackCnName() {
        if (StringUtil.isNotEmptyString(trafficBack)) {
            return Constant.LINE_TRAFFIC.getCnName(trafficBack);
        }
        return "";
    }

    public String getTrafficToCode() {
        if (StringUtil.isNotEmptyString(trafficTo)) {
            return Constant.LINE_TRAFFIC.getCode(trafficTo);
        }
        return "";
    }

    public String getTrafficBackCode() {
        if (StringUtil.isNotEmptyString(trafficBack)) {
            return Constant.LINE_TRAFFIC.getCode(trafficBack);
        }
        return "";
    }

    public String[] getTagNameArr() {
        if (StringUtil.isNotEmptyString(tagName)) {
            return tagName.split(",");
        }
        return null;
    }

    public String[] getPromotionTypeArr() {
        if (StringUtil.isNotEmptyString(promotionTypes)) {
            return promotionTypes.split(",");
        }
        return null;
    }

    public String getPromotionTypeCnName() {
        String[] promotionTypeCnNameArr = getPromotionTypeArr();
        String promCnName = "";
        if (null != promotionTypeCnNameArr) {
            int i = 0;
            for (String string : promotionTypeCnNameArr) {
                if (i > 4) {
                    break;
                }
                if (!promCnName.contains(Constant.ACTIVITY_TYPE.getCnName(string))) {
                    i++;
                    promCnName = promCnName + Constant.ACTIVITY_TYPE.getCnName(string) + " ";
                }
            }
        }
        return promCnName;
    }

    public String getDistinctCityNames()
    {
        final List<String> destTypeList = new ArrayList<String>();
        Collections.addAll(destTypeList, "COUNTRY","PROVINCE","CITY","SCENIC" );
        TreeMultimap<String, String> treeMap = TreeMultimap.create(new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return destTypeList.indexOf(o1)  - destTypeList.indexOf(o2);
            }
        }, Ordering.natural());

        if (this.destName == null)
        {
            return "";
        }
        else
        {
            String[] destNames = destName.split(",");
            String[] destTypes = destType.split(",");
            if (destTypes.length == destNames.length)
            {
                for (int i = 0; i < destTypes.length; i++)
                {
                    if (destTypeList.contains(destTypes[i]))
                    {
                        treeMap.put(destTypes[i], destNames[i]);
                    }
                }
            }
        }
        String[] arr = treeMap.values().toArray(new String[0]);
        int newLength = arr.length <=5 ? arr.length:5;
        arr = Arrays.copyOf(arr, newLength);
        return StringUtils.join(arr, " + ");
    }

    public void calcSellPrice() {
		/*
		 * 计算最终显示的起价
		 */
        if (distributorId != null) {
            int count = 0;
            Set<String> distributorSet = new HashSet<String>();
            CollectionUtils.addAll(distributorSet, distributorId.split(","));
            if (distributorSet.contains("3")) {
                count++;
            }
            if (distributorSet.contains("108")) {
                count++;
            }
            if (distributorSet.contains("110")) {
                count++;
            }
            switch (count) {
                // 如果只有一个渠道，则取对应渠道的价格
                case 1:
                    if (distributorSet.contains("108")) {
                        sellPrice = groupPrice;
                    } else if (distributorSet.contains("110")) {
                        sellPrice = spikeTypePrice;
                    }
                    break;
                // 如果有两个渠道，依次取团购、秒杀、前台
                case 2:
                    if (null != groupPrice) {
                        sellPrice = groupPrice;
                    } else if (null != spikeTypePrice) {
                        sellPrice = spikeTypePrice;
                    }
                    break;
                // 如果有三个渠道，取前台起价sellPrice
            }
        }
    }

    public String getStartYearMonth() {
        return startYearMonth;
    }

    public void setStartYearMonth(String startYearMonth) {
        this.startYearMonth = startYearMonth;
    }

    public String getTourTypeId() {
        return tourTypeId;
    }

    public void setTourTypeId(String tourTypeId) {
        this.tourTypeId = tourTypeId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getStartYearMonthDay() {
        return startYearMonthDay;
    }

    public void setStartYearMonthDay(String startYearMonthDay) {
        this.startYearMonthDay = startYearMonthDay;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelStar() {
        return hotelStar;
    }

    public void setHotelStar(String hotelStar) {
        this.hotelStar = hotelStar;
    }

    public String getRouteDetail() {
        return routeDetail;
    }

    public void setRouteDetail(String routeDetail) {
        this.routeDetail = routeDetail;
    }

    public String getHotelBaiduLongitudes() {
        return hotelBaiduLongitudes;
    }

    public void setHotelBaiduLongitudes(String hotelBaiduLongitudes) {
        this.hotelBaiduLongitudes = hotelBaiduLongitudes;
    }

    public String getHotelBaiduLatitudes() {
        return hotelBaiduLatitudes;
    }

    public void setHotelBaiduLatitudes(String hotelBaiduLatitudes) {
        this.hotelBaiduLatitudes = hotelBaiduLatitudes;
    }

    @Override
    public int hashCode() {
        final int prime = 32;
        int result = 1;
        result = prime * result
                + ((productId == null) ? 0 : productId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderLineProductVO other = (OrderLineProductVO) obj;
        if (productId == null) {
            if (other.productId != null)
                return false;
        } else if (!productId.equals(other.productId))
            return false;
        return true;
    }


    public String getPresellFlag() {
        return presellFlag;
    }

    public void setPresellFlag(String presellFlag) {
        this.presellFlag = presellFlag;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
