package com.lvmama.vst.order.route.constant;

public class JedisEnum {
    public enum JEDIS_NAPE_ENUM {
        /**订单路由缓存*/
        JEDIS_NAPE_ORDER_ROUTE("jedis_key_order_route_", 7 * 24 * 60 * 60),
        /**子单路由缓存*/
        JEDIS_NAPE_ORDER_ITEM_ROUTE("jedis_key_order_item_route_", 7 * 24 * 60 * 60);

        private String key;
        private int seconds;

        JEDIS_NAPE_ENUM(String key, int seconds) {
            this.key = key;
            this.seconds = seconds;
        }

        public String getKey(){
            return this.key;
        }

        public int getSeconds() {
            return seconds;
        }
    }
}
