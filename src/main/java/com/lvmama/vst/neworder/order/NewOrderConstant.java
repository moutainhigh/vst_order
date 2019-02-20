package com.lvmama.vst.neworder.order;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by dengcheng on 17/3/7.
 */
public class NewOrderConstant {
    public  final static String VST_ORDER_TOKEN="Sb^VyHlgPwpJP89ZWPh$ctAdyxFGS45E";

    public final static String UNIQUE_ORDER_KEY = "UNIQUE_ORDER_KEY";

    //用来标示processor 执行状态
    public final static String WORK_FLOW_FLAG="WORRK_FLOW_FLAG";
    public final static String PERSISTANCE_FLAG="PERSISTANCE_FLAG";
    public final static String ORDER_MSG_FLAG="ORDER_MSG_FLAG";


    public static enum HOTEL_SYSTEM_CODE {

        HOTEL_SYSTEM_ERROR("酒店子系统调用异常");



        private final String cnName;

        HOTEL_SYSTEM_CODE(String name){
            this.cnName = name;
        }

        public String getCnName(String code){
            for(HOTEL_SYSTEM_CODE item: HOTEL_SYSTEM_CODE.values()){
                if(item.getCode().equals(code)){
                    return item.cnName;
                }
            }
            return code;
        }

        public String getCode(){
            return this.name();
        }

        public String getCnName(){
            return this.cnName;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }



    public static com.lvmama.dest.hotel.trade.utils.BusinessException error(String errorCode,String subErrorCode,String message){
        return new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s,%s",errorCode,subErrorCode,message));
    }

    //用来标示processor 执行状态

    public static enum DEST_NEWSYSTEM_CATEGORY {

        HOTEL("1"),
        HOTELCOMB("32");
        private String cnName;
        private int categorId;
        public static Long getCategoryId(String code) {
        for (DEST_NEWSYSTEM_CATEGORY item : DEST_NEWSYSTEM_CATEGORY.values()) {
            if (item.getCode().equals(code)) {
                return Long.getLong(item.getCnName());
            }
        }
        return 0L;
    }

        DEST_NEWSYSTEM_CATEGORY(String name) {
        this.cnName = name;
    }
        public int getCategorId() {
            return categorId;
        }
        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }

        public void setCategorId(int categorId) {
            this.categorId = categorId;
        }
        @Override
        public String toString() {
            return this.name();
        }
    }

    //用来标示processor 执行状态

    public static enum SYSTEM_GOODS_SERVICE_MAPPING {

        GOODS_SERVICE(new Long[]{32L,1L},"HotelSystemGoodsService");
//        BRANCH_SERVICE
//        VST_SERVICE(new Long[]{32L,1L},"HotelSystemGoodsService");


        private String cnName;
        private Long[] categorId;
        private String beanName;

        public static String lookupBeanNameByCategoryId(Long categoryId) {
            for (SYSTEM_GOODS_SERVICE_MAPPING item : SYSTEM_GOODS_SERVICE_MAPPING.values()) {
                for(Long itemCategoryId : item.getCategorId()) {
                    if(itemCategoryId==categoryId){
                        return  item.getBeanName();
                    }
                }
            }
            //默认返回vstgoodsservice
            return "vstSystemGoodsService";
        }

        SYSTEM_GOODS_SERVICE_MAPPING(Long[] categoryId,String beanName) {
            this.categorId = categoryId;
            this.beanName = beanName;
        }


        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }


        @Override
        public String toString() {
            return this.name();
        }
        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public Long[] getCategorId() {
            return categorId;
        }

        public void setCategorId(Long[] categorId) {
            this.categorId = categorId;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

    }

    /**
     * 全局的缓存重单处理
     *
     */
    public static LoadingCache<String,Object> orderCache = CacheBuilder.newBuilder().refreshAfterWrite(30, TimeUnit.SECONDS).maximumSize(100000).build(new CacheLoader<String, Object>() {
        @Override
        public Object load(String key) throws Exception {
            return null;
        }
    });



    public static enum SYSTEM_TIMEPRICE_SERVICE_MAPPING {

        TIMEPRICE_SERVICE(new Long[]{32L,1L},"HotelSystemTimePriceService");
//        BRANCH_SERVICE
//        VST_SERVICE(new Long[]{32L,1L},"HotelSystemGoodsService");


        private String cnName;
        private Long[] categorId;
        private String beanName;

        public static String lookupBeanNameByCategoryId(Long categoryId,boolean hasControled) {
        
            for (SYSTEM_TIMEPRICE_SERVICE_MAPPING item : SYSTEM_TIMEPRICE_SERVICE_MAPPING.values()) {
                for(Long itemCategoryId : item.getCategorId()) {
                    if(itemCategoryId==categoryId){
                    	if(hasControled){
                    		return "hotelCombBuyOutTimePrice";
                    	}else{
                        return  item.getBeanName();
                    	}
                    }
                }
            }
            //默认返回vstgoodsservice
            if(hasControled){
            	return  "vstBuyOutTimePrice";
            }else{
                return "vstSystemTimePriceService";
            }
        }

        SYSTEM_TIMEPRICE_SERVICE_MAPPING(Long[] categoryId,String beanName) {
            this.categorId = categoryId;
            this.beanName = beanName;
        }


        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }


        @Override
        public String toString() {
            return this.name();
        }
        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public Long[] getCategorId() {
            return categorId;
        }

        public void setCategorId(Long[] categorId) {
            this.categorId = categorId;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

    }

    public static enum SYSTEM_BUYOUTTIMEPRICE_SERVICE_MAPPING {
    	BUYOUTTIMEPRICE(new Long[]{32L},"hotelCombBuyOutTimePrice");

        private String cnName;
        private Long[] categorId;
        private String beanName;

        public static String lookupBeanNameByCategoryId(Long categoryId) {
            for (SYSTEM_TIMEPRICE_SERVICE_MAPPING item : SYSTEM_TIMEPRICE_SERVICE_MAPPING.values()) {
                for(Long itemCategoryId : item.getCategorId()) {
                    if(itemCategoryId==categoryId){
                        return  item.getBeanName();
                    }
                }
            }
            //默认返回vstgoodsservice
            return "vstBuyOutTimePrice";
        }

        SYSTEM_BUYOUTTIMEPRICE_SERVICE_MAPPING(Long[] categoryId,String beanName) {
            this.categorId = categoryId;
            this.beanName = beanName;
        }


        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }


        @Override
        public String toString() {
            return this.name();
        }
        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public Long[] getCategorId() {
            return categorId;
        }

        public void setCategorId(Long[] categorId) {
            this.categorId = categorId;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }
    	
    }
    
    public static enum VSTTIKET_TIMEPRICE_SERVICE {
    	VTIKET_TIMEPRIC(new Long[]{3L},"orderTicketNoTimePriceService");

        private String cnName;
        private Long[] categorId;
        private String beanName;

        VSTTIKET_TIMEPRICE_SERVICE(Long[] categoryId,String beanName) {
            this.categorId = categoryId;
            this.beanName = beanName;
        }

        public static String lookupBeanNameByCategoryId(Long categoryId) {
            for (VSTTIKET_TIMEPRICE_SERVICE item : VSTTIKET_TIMEPRICE_SERVICE.values()) {
                for(Long itemCategoryId : item.getCategorId()) {
                    if(itemCategoryId==categoryId){
                        return  item.getBeanName();
                    }
                }
            }
            //默认返回vstgoodsservice
            return "orderTicketAddTimePriceService";
        }
        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }


        @Override
        public String toString() {
            return this.name();
        }
        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public Long[] getCategorId() {
            return categorId;
        }

        public void setCategorId(Long[] categorId) {
            this.categorId = categorId;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }
    	
    }
    public static ThreadLocal<Map<String,Object>> orderThreadLocalCache  = new ThreadLocal<Map<String,Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return Maps.newConcurrentMap();
        }
    };
}
