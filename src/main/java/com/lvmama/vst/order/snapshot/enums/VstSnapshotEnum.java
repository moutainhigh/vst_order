package com.lvmama.vst.order.snapshot.enums;

/**
 * VST快照枚举
 */
public class VstSnapshotEnum {
    /**
     * 网关类服务枚举
     */
    public static enum GATEWAY_CLASSS_ERVICE{
        VST_PRODUCT_ORDER("vstProducSnapshotGatewayService","主单产品"),
        VST_PRODUCT_ORDER_ITEM("vstProducSnapshotGatewayService","子单产品");

        private String serviceName;
        private String cnName;

        GATEWAY_CLASSS_ERVICE(String serviceName,String cnName){
            this.serviceName = serviceName;
            this.cnName=cnName;
        }

        public String getServiceName() {
            return serviceName;
        }
        public String getCnName() {
            return cnName;
        }
    }

}
