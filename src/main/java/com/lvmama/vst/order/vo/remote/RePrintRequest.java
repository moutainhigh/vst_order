package com.lvmama.vst.order.vo.remote;
/**
 * 调用百旺重新打印发票接口请求入参封装类
 * @author WangSizhi
 *
 */
public class RePrintRequest {
    /**开票端编号 ,每个开票点对应唯一编号*/
    private String clientNo;
    /**税控机标识（IP地址格式）*/
    private String taxMachineIP;
    /**发票数据（XML格式）*/
    private RePrintInvoiceData rePrintInvoiceData;
    
    public String getClientNo() {
        return clientNo;
    }
    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }
    public String getTaxMachineIP() {
        return taxMachineIP;
    }
    public void setTaxMachineIP(String taxMachineIP) {
        this.taxMachineIP = taxMachineIP;
    }
    public RePrintInvoiceData getRePrintInvoiceData() {
        return rePrintInvoiceData;
    }
    public void setRePrintInvoiceData(RePrintInvoiceData rePrintInvoiceData) {
        this.rePrintInvoiceData = rePrintInvoiceData;
    }
    @Override
    public String toString() {
        return "CLIENTNO=" + (clientNo == null ? "" : clientNo) 
                + "&TaxMachineIP=" + (taxMachineIP == null ? "" : taxMachineIP)
                + "&InvoiceData=" + rePrintInvoiceData.toString();
    }
}
