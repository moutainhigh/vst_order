package com.lvmama.vst.order.route.constant;

public class OrderSystemEnum {
    /**
     * 订单路由开关状态
     * */
    public enum ORDER_ROUTE_SWITCH_STATUS {
        ON("打开"),
        OFF("关闭");

        private final String cnName;

        ORDER_ROUTE_SWITCH_STATUS(String cnName) {
            this.cnName = cnName;
        }

        public String getStatus(){
            return this.name();
        }

        public String getCnName(){
            return this.cnName;
        }
    }
}
