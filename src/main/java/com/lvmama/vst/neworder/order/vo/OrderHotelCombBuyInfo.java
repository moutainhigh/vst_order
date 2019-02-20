package com.lvmama.vst.neworder.order.vo;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lvmama.dest.hotel.trade.vo.base.BaseBuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Coupon;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.Item;

/**
 * Created by dengcheng on 17/3/4.
 */
public class OrderHotelCombBuyInfo extends BaseBuyInfo {
    /**
     * 购买的商品列表
     */
    List<GoodsItem> goodsList;
    



    /**
     * 产品列表 实际只有一个 这里用数组方便扩展
     */
    List<ProductItem> productList;
    
    ProdLineRoute prodLineRoute;

	/**
     * 商品游客关系表
     */
    Map<Long,List<Person>> goodsPersonListMap = new HashMap<Long,List<Person>>();

   

    List<Item> itemList;


    public List<Item> getItemList() {
  		return itemList;
  	}

  	public void setItemList(List<Item> itemList) {
  		this.itemList = itemList;
  	}

    public Map<Long, List<Person>> getGoodsPersonListMap() {
        return goodsPersonListMap;
    }

    public void setGoodsPersonListMap(Map<Long, List<Person>> goodsPersonListMap) {
        this.goodsPersonListMap = goodsPersonListMap;
    }

    public List<GoodsItem> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<GoodsItem> goodsList) {
        this.goodsList = goodsList;
    }


    public List<ProductItem> getProductList() {
        return productList;
    }

    public void setProductList(List<ProductItem> productList) {
        this.productList = productList;
    }

	/**
	 * 工作流版本
	 */
	private String workVersion;
	
	public String getWorkVersion() {
		return workVersion;
	}

	public void setWorkVersion(String workVersion) {
		this.workVersion = workVersion;
	}


    public class ProductItem {
        Long productId;
        String productName;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

    }


    public Date getEarliestVisitTime(){
        Date vistiTime = null;
        List<Date> dateList = Lists.newArrayList();
        for(OrderHotelCombBuyInfo.GoodsItem goodsItem :this.getGoodsList()) {
            dateList.add(goodsItem.getCheckInDate());
        }
        //倒排序 取最小的一个时间为出游时间
        Collections.sort(dateList, new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                if (o1.before(o2)) {
                    return 1;
                } else if(o1.after(o2)){
                    return -1;
                }
                return 0;
            }
        });
        vistiTime  = dateList.get(0);
        return vistiTime;
    }

    public class GoodsItem {
        /**
         * 商品id
         */
        Long goodsId;

        public Long getPricePlanId() {
            return pricePlanId;
        }

        public void setPricePlanId(Long pricePlanId) {
            this.pricePlanId = pricePlanId;
        }

        /**
         * 价格计划id 酒店套餐必须
         */
        Long pricePlanId;
        /**
         * 入住时间
         */
        Date checkInDate;
        Date checkOutDate;
        /**
         * checkIn 游客
         */
        String travellerName;
        /**
         * checkIn 游客联系方式
         */
        String travellerMobile;
        /**
         * 购买间数
         */
        Long quantity;

        /**
         * 需要根据当前商品计算
         */
        Long totalAmount;

        /**
         * 提交预定时间
         */
        Long aheadBookTime;

        /**
         * 品类ID
         */
        Long productCategoryId;

        /**
         * 二级品类ID
         */
        Long subCategoryId;

        /**
         * 售卖类型
         */
        String saleType;
        
        /**
         * 默认都可以设置买断价(分销下单不设置买断价)
         */
        boolean withBuyOutPrice = true;

        public boolean isWithBuyOutPrice() {
			return withBuyOutPrice;
		}

		public void setWithBuyOutPrice(boolean withBuyOutPrice) {
			this.withBuyOutPrice = withBuyOutPrice;
		}

        public Date getLastCancelTime() {
            return lastCancelTime;
        }

        public void setLastCancelTime(Date lastCancelTime) {
            this.lastCancelTime = lastCancelTime;
        }

        /**
         * 最晚无损取消时间
         */
        Date lastCancelTime;

        public String getGoodType() {
            return goodType;
        }

        public void setGoodType(String goodType) {
            this.goodType = goodType;
        }

        String goodType;

        public Long getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }


        public Long getProductCategoryId() {
            return productCategoryId;
        }

        public void setProductCategoryId(Long productCategoryId) {
            this.productCategoryId = productCategoryId;
        }


        public Long getGoodsId() {
            return goodsId;
        }


        public Long getSubCategoryId() {
            return subCategoryId;
        }

        public void setSubCategoryId(Long subCategoryId) {
            this.subCategoryId = subCategoryId;
        }

        public void setGoodsId(Long goodsId) {
            this.goodsId = goodsId;
        }

        public Date getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(Date checkInDate) {
            this.checkInDate = checkInDate;
        }

        public Date getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(Date checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public String getTravellerName() {
            return travellerName;
        }

        public void setTravellerName(String travellerName) {
            this.travellerName = travellerName;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public String getTravellerMobile() {
            return travellerMobile;
        }

        public void setTravellerMobile(String travellerMobile) {
            this.travellerMobile = travellerMobile;
        }

        public Long getAheadBookTime() {
            return aheadBookTime;
        }

        public void setAheadBookTime(Long aheadBookTime) {
            this.aheadBookTime = aheadBookTime;
        }

    }
    
    public class ProdLineRoute implements Serializable {
    	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		Long id;
    	Short dayNum;
    	Short nightNum;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Short getDayNum() {
			return dayNum;
		}
		public void setDayNum(Short dayNum) {
			this.dayNum = dayNum;
		}
		public Short getNightNum() {
			return nightNum;
		}
		public void setNightNum(Short nightNum) {
			this.nightNum = nightNum;
		}
		
    }
    
    public ProdLineRoute getProdLineRoute() {
		return prodLineRoute;
	}

	public void setProdLineRoute(ProdLineRoute prodLineRoute) {
		this.prodLineRoute = prodLineRoute;
	}
    /**
     * 酒套餐关联销售商品或者保险
     * 
     */
    public  class Item implements Serializable {
		/**
		 * 序列化ID
		 */
		private static final long serialVersionUID = 1755069417864830632L;
		
		private Long goodsId;
		/**
		 * 打包关系
		 */
		private Long detailId;
		private String visitTime;
		private int quantity;
		
		private Long checkStockQuantity;
		
		//共享数量，用于共享库存的库存校验
		private Long shareTotalStock;
		private Long shareDayLimit;
		
		private String mainItem;
		// 邮轮商品类型
		private String goodType;
		private String buCode;
		//对接机票中的  成人价、儿童价
		private Long adultAmt;
		private Long childAmt;
		private Long productCategoryId;
		/**
		 * 用户自有数量，用户系统之外，不计算价格
		 */
		private int ownerQuantity;

		private int adultQuantity;

		private int childQuantity;
		
		private int gapQuantity;

		//子项游玩人信息放到ItemPersonRelation对象中
		/**
		 * 目前这个属性只支持邮轮使用，
		 * 其他的转到BuyInfo.personRelationMap
		 * @deprecated
		 */
		private List<ItemPersonRelation> itemPersonRelationList;

		/**
		 * 淘宝分销--使用
		 * 淘宝电子票标志  0 非 / 1 是
		 */
		private Integer taobaoETicket;



		//交通去程时间
		private String toDate;
		//交通返程时间
		private String backDate;
		//可换酒店
		private String displayTime;



		
		private String content;//存放关联当地游的产品标识

        private String disneyItemOrderInfo;//存放迪士尼的演出票子订单信息

        private Long totalAmount;//子订单总价

        private Long totalSettlementPrice;//子订单结算总价
        
        private String isDisneyGood;//是否包含迪士尼门票
        
    	private String orderSubType;
    	
    	private String startStampTime;
    	
    	/***
    	 * 产品类型，如果是送退改险，价格可以为0
    	 */
        private String productType;

        public String getOrderSubType() {
			return orderSubType;
		}

		public void setOrderSubType(String orderSubType) {
			this.orderSubType = orderSubType;
		}

		public String getIsDisneyGood() {
			return isDisneyGood;
		}

		public void setIsDisneyGood(String isDisneyGood) {
			this.isDisneyGood = isDisneyGood;
		}

		public Long getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Long getTotalSettlementPrice() {
            return totalSettlementPrice;
        }

        public void setTotalSettlementPrice(Long totalSettlementPrice) {
            this.totalSettlementPrice = totalSettlementPrice;
        }

        public String getDisneyItemOrderInfo() {
            return disneyItemOrderInfo;
        }

        public void setDisneyItemOrderInfo(String disneyItemOrderInfo) {
            this.disneyItemOrderInfo = disneyItemOrderInfo;
        }

        public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		

		public String getDisplayTime() {
			return displayTime;
		}

		public void setDisplayTime(String displayTime) {
			this.displayTime = displayTime;
		}

		public Long getGoodsId() {
			return goodsId;
		}

		public String getBuCode() {
			return buCode;
		}

		public void setBuCode(String buCode) {
			this.buCode = buCode;
		}


		public Long getProductCategoryId() {
			return productCategoryId;
		}

		public void setProductCategoryId(Long productCategoryId) {
			this.productCategoryId = productCategoryId;
		}

		public void setGoodsId(Long goodsId) {
			this.goodsId = goodsId;
		}

		public String getVisitTime() {
			return visitTime;
		}
		public String price;

        public String settlementPrice;

        public String getSettlementPrice() {
            return settlementPrice;
        }

        public void setSettlementPrice(String settlementPrice) {
            this.settlementPrice = settlementPrice;
        }

        public String getPrice() {
			return price;
		}

		public void setPrice(String price) {
			this.price = price;
		}

		
		public void setVisitTime(String visitTime) {
			this.visitTime = visitTime;
		}
		
		public int getQuantity() {
			return quantity;
		}
		
		public Long getCheckStockQuantity() {
			if(checkStockQuantity == null || checkStockQuantity <= 0) {
				return Long.valueOf(quantity);
			}
			return checkStockQuantity;
		}
		public void setCheckStockQuantity(Long checkStockQuantity) {
			this.checkStockQuantity = checkStockQuantity;
		}
		public Long getShareTotalStock() {
			if(shareTotalStock == null) {
				return 0l;
			}
			return shareTotalStock;
		}
		public void setShareTotalStock(Long shareTotalStock) {
			this.shareTotalStock = shareTotalStock;
		}
		public Long getShareDayLimit() {
			if(shareDayLimit == null) {
				return 0l;
			}
			return shareDayLimit;
		}
		public void setShareDayLimit(Long shareDayLimit) {
			this.shareDayLimit = shareDayLimit;
		}
		public int getTotalPersonQuantity(){
			return getAdultQuantity()+getChildQuantity();
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

	

		public String getMainItem() {
			return mainItem;
		}

		public void setMainItem(String mainItem) {
			this.mainItem = mainItem;
		}

		public List<ItemPersonRelation> getItemPersonRelationList() {
			return itemPersonRelationList;
		}

		public void setItemPersonRelationList(
				List<ItemPersonRelation> itemPersonRelationList) {
			this.itemPersonRelationList = itemPersonRelationList;
		}

		public String getGoodType() {
			return goodType;
		}

		public void setGoodType(String goodType) {
			this.goodType = goodType;
		}

		public int getOwnerQuantity() {
			return ownerQuantity;
		}

		public void setOwnerQuantity(int ownerQuantity) {
			this.ownerQuantity = ownerQuantity;
		}

		public int getAdultQuantity() {
			return adultQuantity;
		}

		public void setAdultQuantity(int adultQuantity) {
			this.adultQuantity = adultQuantity;
		}

		public int getChildQuantity() {
			return childQuantity;
		}

		public void setChildQuantity(int childQuantity) {
			this.childQuantity = childQuantity;
		}

		public Long getDetailId() {
			return detailId;
		}

		public void setDetailId(Long detailId) {
			this.detailId = detailId;
		}


		public String getToDate() {
			return toDate;
		}

		public void setToDate(String toDate) {
			this.toDate = toDate;
		}

		public String getBackDate() {
			return backDate;
		}

		public void setBackDate(String backDate) {
			this.backDate = backDate;
		}

		public Long getAdultAmt() {
			return adultAmt;
		}

		public void setAdultAmt(Long adultAmt) {
			this.adultAmt = adultAmt;
		}

		public Long getChildAmt() {
			return childAmt;
		}

		public void setChildAmt(Long childAmt) {
			this.childAmt = childAmt;
		}
		
		public int getGapQuantity() {
			return gapQuantity;
		}
		public void setGapQuantity(int gapQuantity) {
			this.gapQuantity = gapQuantity;
		}
		public Integer getTaobaoETicket() {
			return taobaoETicket;
		}

		public void setTaobaoETicket(Integer taobaoETicket) {
			this.taobaoETicket = taobaoETicket;
		}

		

        
		public String getProductType() {
			return productType;
		}

		public void setProductType(String productType) {
			this.productType = productType;
		}


 
	}
}
