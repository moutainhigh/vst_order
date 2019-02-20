package com.lvmama.vst.order.vo.remote;
/**
 * 调用百旺开具发票接口请求入参封装类
 * @author WangSizhi
 *
 */
public class CallServiceRequest {
    /**开票端编号 ,每个开票点对应唯一编号*/
    private String clientNo;
    /**税控机标识（IP地址格式）*/
    private String taxMachineIP;
    /**单据号 （通常情况一个单据号对应唯一的发票代码、发票号码，对于合并开票操作SysInvNo参数由多个单据号组成并用逗号分隔）*/
    private String sysInvNo;
    /**是否打印清单 1 打印清单 0 不打印清单*/
    private Integer invoiceList;
    /**是否自动拆分 1 拆分  0 不拆分*/
    private Integer invoiceSplit;
    /**是否合并操作 1 合并  0 不合并*/
    private Integer invoiceConsolidate;
    /**发票数据（XML格式）*/
    private InvoiceData invoiceData;
    
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
    public String getSysInvNo() {
        return sysInvNo;
    }
    public void setSysInvNo(String sysInvNo) {
        this.sysInvNo = sysInvNo;
    }
    public Integer getInvoiceList() {
        return invoiceList;
    }
    public void setInvoiceList(Integer invoiceList) {
        this.invoiceList = invoiceList;
    }
    public Integer getInvoiceSplit() {
        return invoiceSplit;
    }
    public void setInvoiceSplit(Integer invoiceSplit) {
        this.invoiceSplit = invoiceSplit;
    }
    public Integer getInvoiceConsolidate() {
        return invoiceConsolidate;
    }
    public void setInvoiceConsolidate(Integer invoiceConsolidate) {
        this.invoiceConsolidate = invoiceConsolidate;
    }
    public InvoiceData getInvoiceData() {
        return invoiceData;
    }
    public void setInvoiceData(InvoiceData invoiceData) {
        this.invoiceData = invoiceData;
    }
    @Override
    public String toString() {
        return "CLIENTNO=" + (clientNo == null ? "" : clientNo)
                + "&TaxMachineIP=" + (taxMachineIP == null ? "" : taxMachineIP) 
                + "&SysInvNo=" + (sysInvNo == null ? "" : sysInvNo) 
                + "&InvoiceList=" + (invoiceList == null ? "" : invoiceList) 
                + "&InvoiceSplit=" + (invoiceSplit == null ? "" : invoiceSplit) 
                + "&InvoiceConsolidate=" + (invoiceConsolidate == null ? "" : invoiceConsolidate) 
                + "&InvoiceData=" + invoiceData.toString(); 
    }
    
}
