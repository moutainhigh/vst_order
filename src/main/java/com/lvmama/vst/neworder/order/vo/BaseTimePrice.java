package com.lvmama.vst.neworder.order.vo;

import java.util.Date;

/**
 * Created by dengcheng on 17/3/21.
 */
public class BaseTimePrice {

    /**
     * 价格计划id 酒店系统有这个值
     */
    private Long pricePlanId;
    /**
     * checkInDate;
     */
    private Date specDate;
    /**
     * 售价
     */
    private Long salePrice;
    /**
     * 结算价
     */
    private Long settmentPrice;
    
    private String resrouseStatus ;//资源状态

    private Long  stock;
    
    private String oversellFlag ;
  
    private Long shareTotalStockId;    // 门票保险共享库存

    private Long shareDayLimitId;
    
    public String getOversellFlag() {
		return oversellFlag;
	}

	public void setOversellFlag(String oversellFlag) {
		this.oversellFlag = oversellFlag;
	}

	/** 
     * 是否扣减库存标志位
     */
    private String stockFlag;
    
    public Long getAheadBookTime() {
        return aheadBookTime;
    }

    public void setAheadBookTime(Long aheadBookTime) {
        this.aheadBookTime = aheadBookTime;
    }

    //提前预订时间
    private Long aheadBookTime;

    /**
     * 商品id
     */
    private Long goodsId;


    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }


    public Long getPricePlanId() {
        return pricePlanId;
    }

    public void setPricePlanId(Long pricePlanId) {
        this.pricePlanId = pricePlanId;
    }

    public Date getSpecDate() {
        return specDate;
    }

    public void setSpecDate(Date specDate) {
        this.specDate = specDate;
    }

    public Long getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Long salePrice) {
        this.salePrice = salePrice;
    }

    public Long getSettmentPrice() {
        return settmentPrice;
    }

    public void setSettmentPrice(Long settmentPrice) {
        this.settmentPrice = settmentPrice;
    }

	public String getResrouseStatus() {
		return resrouseStatus;
	}

	public void setResrouseStatus(String resrouseStatus) {
		this.resrouseStatus = resrouseStatus;
	}

	public String getStockFlag() {
		return stockFlag;
	}

	public void setStockFlag(String stockFlag) {
		this.stockFlag = stockFlag;
	}

	public Long getStock() {
		return stock;
	}

	public void setStock(Long stock) {
		this.stock = stock;
	}

	public Long getShareTotalStockId() {
		return shareTotalStockId;
	}

	public void setShareTotalStockId(Long shareTotalStockId) {
		this.shareTotalStockId = shareTotalStockId;
	}

	public Long getShareDayLimitId() {
		return shareDayLimitId;
	}

	public void setShareDayLimitId(Long shareDayLimitId) {
		this.shareDayLimitId = shareDayLimitId;
	}


}
