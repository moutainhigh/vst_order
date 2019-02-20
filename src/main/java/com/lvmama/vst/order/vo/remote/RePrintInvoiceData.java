package com.lvmama.vst.order.vo.remote;

public class RePrintInvoiceData {
    /**流水号*/
    private String swiftNumber;
    /**发票种类, 发票种类 0-专用发票；1-普通发票*/
    private Integer invType;
    /**发票代码*/
    private String invCode;
    /**发票号码*/
    private String invNo;
    /**打印类型 0-打印发票；1-打印清单*/
    private Integer printType;
    
    public String getSwiftNumber() {
        return swiftNumber;
    }
    public void setSwiftNumber(String swiftNumber) {
        this.swiftNumber = swiftNumber;
    }
    public String getInvCode() {
        return invCode;
    }
    public void setInvCode(String invCode) {
        this.invCode = invCode;
    }
    public String getInvNo() {
        return invNo;
    }
    public void setInvNo(String invNo) {
        this.invNo = invNo;
    }
    public Integer getInvType() {
        return invType;
    }
    public void setInvType(Integer invType) {
        this.invType = invType;
    }
    public Integer getPrintType() {
        return printType;
    }
    public void setPrintType(Integer printType) {
        this.printType = printType;
    }
    @Override
    public String toString() {
        return "<?xml version=\"1.0\" encoding=\"gbk\"?><InvoiceData><body><input>" 
                + "<SwiftNumber>" + (swiftNumber == null ? "" : swiftNumber) 
                + "</SwiftNumber><InvType>" + (invType == null ? "" : invType) 
                + "</InvType><InvCode>" + (invCode == null ? "" : invCode) 
                + "</InvCode><InvNo>" + (invNo == null ? "" : invNo) 
                + "</InvNo><PrintType>" + (printType == null ? "" : printType) 
                + "</PrintType>"
                + "</input></body></InvoiceData>";
    }

}
