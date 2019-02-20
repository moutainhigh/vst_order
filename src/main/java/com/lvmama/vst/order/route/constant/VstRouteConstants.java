package com.lvmama.vst.order.route.constant;

public class VstRouteConstants {
    public static final String FLAG_Y = "Y";
    public static final String FLAG_N = "N";
    public static final String FLAG_S = "S";

    /**
     * 新系统是否开放使用
     */
    public static final String TOTAL_SWITCH = "order.reconstitute.switch";

    /**
     * 新系统是否开放使用
     */
    public static final String TOTAL_SWITCH_NEW = "order2.reconstitute.switch";

    /**
     * 品类的流量模sweet配置项的前缀
     */
    public static final String MODULO_CONFIG_PREFIX = "order.reconstitute.modulo.category_";

    /**
     * 路由品类sweet配置项的键
     */
    public static final String CATEGORIES_CONFIG_KEY = "order.reconstitute.categories";

    /**
     * 无参数路由的配置项的键
     * */
    public static final String EMPTY_PARAM_ROUTE_CONFIG = "order.reconstitute.route_without_param";

    /**
     * Job是否路由到新系统
     * */
    public static final String KEY_JOB_SWITCH = "job.route.switch";

    /**
     * 流量mode值
     */
    public static final int SHUNT_REMAINDER = 0;

    /**
     * 工作流版本
     */
    public static final String WORKFLOW_VERSION="workflow_version";

    /**
     * 新工作流品类sweet配置项的键
     */
    public static final String NEW_WORKFLOW_CATEGORIES_KEY = "order.new.workflow.categories";
}
