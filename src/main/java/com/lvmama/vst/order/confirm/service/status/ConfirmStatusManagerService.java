package com.lvmama.vst.order.confirm.service.status;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 确认状态管理服务
 */
@Service("confirmStatusManagerService")
public class ConfirmStatusManagerService {

    @Resource(name ="cancelConfirmStatusService")
    private IConfirmStatusService cancelConfirmStatusService;
    @Resource(name ="defaultConfirmStatusService")
    private IConfirmStatusService defaultConfirmStatusService;

    @Resource(name ="inconfirmStatusService")
    private IConfirmStatusService inconfirmStatusService;
    @Resource(name ="inconfirmStatusSupplierService")
    private IConfirmStatusService inconfirmStatusSupplierService;

    @Resource(name ="newOrderStatusService")
    private IConfirmStatusService newOrderStatusService;
    @Resource(name ="rejectStatusService")
    private IConfirmStatusService rejectStatusService;
    @Resource(name ="inquiryConfirmStatusService")
    private IConfirmStatusService inquiryConfirmStatusService;
    @Resource(name ="bookingConfirmStatusService")
    private IConfirmStatusService bookingConfirmStatusService;

    /**
     * 工作台服务
     */
    public static enum CONFIRM_STATUS_SERVICE {
        INCONFIRM_SUPPLIER_SERVICE("已审库服务_供应商"),
        INCONFIRM_SERVICE("已审库服务_客服"),
        CANCEL_CONFIRM_SERVICE("取消确认库服务"),
        /*满房,特殊满房,订单变价*/
        REJECT_SERVICE("拒绝库服务"),
        NEW_ORDER_SERVICE("新单库服务"),
        INQUIRY_CONFIRM_SERVICE("询位确认库服务"),
        BOOKING_CONFIRM_SERVICE("预订通知确认库服务"),

        DEFAULT_SERVICE("确认状态默认服务");

        private String cnName;

        CONFIRM_STATUS_SERVICE(String name) {
            this.cnName = name;
        }
        public String getCnName() {
            return cnName;
        }
    }

    /**
     * 获取确认状态服务
     * @param confirmStatusService
     * @return
     */
    public IConfirmStatusService getService(CONFIRM_STATUS_SERVICE confirmStatusService) {
        if(confirmStatusService ==null) return null;
        if(CONFIRM_STATUS_SERVICE.CANCEL_CONFIRM_SERVICE.equals(confirmStatusService)) return cancelConfirmStatusService;
        if(CONFIRM_STATUS_SERVICE.DEFAULT_SERVICE.equals(confirmStatusService)) return defaultConfirmStatusService;
        if(CONFIRM_STATUS_SERVICE.INCONFIRM_SERVICE.equals(confirmStatusService)) return inconfirmStatusService;
        if(CONFIRM_STATUS_SERVICE.INCONFIRM_SUPPLIER_SERVICE.equals(confirmStatusService)) return inconfirmStatusSupplierService;
        if(CONFIRM_STATUS_SERVICE.NEW_ORDER_SERVICE.equals(confirmStatusService)) return newOrderStatusService;
        if(CONFIRM_STATUS_SERVICE.REJECT_SERVICE.equals(confirmStatusService)) return rejectStatusService;
        if(CONFIRM_STATUS_SERVICE.INQUIRY_CONFIRM_SERVICE.equals(confirmStatusService)) return inquiryConfirmStatusService;
        if(CONFIRM_STATUS_SERVICE.BOOKING_CONFIRM_SERVICE.equals(confirmStatusService)) return bookingConfirmStatusService;

        return null;
    }

}
