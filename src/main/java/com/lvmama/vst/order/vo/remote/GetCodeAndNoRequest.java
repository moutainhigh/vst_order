package com.lvmama.vst.order.vo.remote;
/**
 * 调用百旺税控盘查询接口请求入参封装类
 * @author WangSizhi
 *
 */
public class GetCodeAndNoRequest {
    /**开票端编号 ,每个开票点对应唯一编号*/
    private String clientNo;
    /**税控机标识（IP地址格式）*/
    private String taxMachineIP;
    /**发票类型代码*/
    private String fplxdm;
    
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
    public String getFplxdm() {
        return fplxdm;
    }
    public void setFplxdm(String fplxdm) {
        this.fplxdm = fplxdm;
    }
    @Override
    public String toString() {
        
        String str = "CLIENTNO=" + (clientNo == null ? "" : clientNo) 
                + "&TaxMachineIP=" + (taxMachineIP == null ? "" : taxMachineIP)
                + "&fplxdm=" + (fplxdm == null ? "" : fplxdm);
        return str;
    }
    
}
