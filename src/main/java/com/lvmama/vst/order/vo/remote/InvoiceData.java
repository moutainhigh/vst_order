package com.lvmama.vst.order.vo.remote;

import java.util.List;

public class InvoiceData {
    /**流水号*/
    private String swiftNumber;
    /**发票种类, 发票种类 0-专用发票；1-普通发票*/
    private Integer invType;
    /**红冲标志, 0-正常发票;1-红冲发票*/
    private Integer creditNoteInv;
    /**作废类型, 0正常发票1-作废空白发票;2-作废已开发票*/
    private Integer cancelInvType;
    /**销货单位识别号*/
    private String venderTaxNo;
    /**销货单位名称*/
    private String venderName;
    /**销货单位地址电话*/
    private String venderAddressTel;
    /**销货单位银行帐号*/
    private String venderBankNameNo;
    /**客户税号*/
    private String customerTaxNo;
    /**客户名称*/
    private String customerName;
    /**客户电话地址*/
    private String customerAddressTel;
    /**客户银行及帐号*/
    private String customerBankNameNo;
    /**商品列表*/
    private List<InvoiceDetail> invoiceDetail;
    /**合计金额*/
    private Double sumTotalAmount;
    /**合计税额*/
    private Double sumTaxAmount;
    /**价税合计*/
    private Double total;
    /**备注*/
    private String remark;
    /**收款人*/
    private String receiver;
    /**复核人*/
    private String checker;
    /**开票人*/
    private String issuer;
    /**作废人*/
    private String cancelUser;
    /**所属月份YYYYMM*/
    private String month;
    /**专用发票红票通知单号*/
    private String CNNoticeNo;
    /**作废时对应的原始发票号码或红票对应正数发票代码*/
    private String CNDNCode;
    /**作废时对应的原始发票号码或红票对应正数发票号码*/
    private String CNDNNo;
    
    public String getSwiftNumber() {
        return swiftNumber;
    }
    public void setSwiftNumber(String swiftNumber) {
        this.swiftNumber = swiftNumber;
    }
    public Integer getInvType() {
        return invType;
    }
    public void setInvType(Integer invType) {
        this.invType = invType;
    }
    public Integer getCreditNoteInv() {
        return creditNoteInv;
    }
    public void setCreditNoteInv(Integer creditNoteInv) {
        this.creditNoteInv = creditNoteInv;
    }
    public Integer getCancelInvType() {
        return cancelInvType;
    }
    public void setCancelInvType(Integer cancelInvType) {
        this.cancelInvType = cancelInvType;
    }
    public String getVenderTaxNo() {
        return venderTaxNo;
    }
    public void setVenderTaxNo(String venderTaxNo) {
        this.venderTaxNo = venderTaxNo;
    }
    public String getVenderName() {
        return venderName;
    }
    public void setVenderName(String venderName) {
        this.venderName = venderName;
    }
    public String getVenderAddressTel() {
        return venderAddressTel;
    }
    public void setVenderAddressTel(String venderAddressTel) {
        this.venderAddressTel = venderAddressTel;
    }
    public String getVenderBankNameNo() {
        return venderBankNameNo;
    }
    public void setVenderBankNameNo(String venderBankNameNo) {
        this.venderBankNameNo = venderBankNameNo;
    }
    public String getCustomerTaxNo() {
        return customerTaxNo;
    }
    public void setCustomerTaxNo(String customerTaxNo) {
        this.customerTaxNo = customerTaxNo;
    }
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getCustomerAddressTel() {
        return customerAddressTel;
    }
    public void setCustomerAddressTel(String customerAddressTel) {
        this.customerAddressTel = customerAddressTel;
    }
    public String getCustomerBankNameNo() {
        return customerBankNameNo;
    }
    public void setCustomerBankNameNo(String customerBankNameNo) {
        this.customerBankNameNo = customerBankNameNo;
    }
    public List<InvoiceDetail> getInvoiceDetail() {
        return invoiceDetail;
    }
    public void setInvoiceDetail(List<InvoiceDetail> invoiceDetail) {
        this.invoiceDetail = invoiceDetail;
    }
    public Double getSumTotalAmount() {
        return sumTotalAmount;
    }
    public void setSumTotalAmount(Double sumTotalAmount) {
        this.sumTotalAmount = sumTotalAmount;
    }
    public Double getSumTaxAmount() {
        return sumTaxAmount;
    }
    public void setSumTaxAmount(Double sumTaxAmount) {
        this.sumTaxAmount = sumTaxAmount;
    }
    public Double getTotal() {
        return total;
    }
    public void setTotal(Double total) {
        this.total = total;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public String getChecker() {
        return checker;
    }
    public void setChecker(String checker) {
        this.checker = checker;
    }
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public String getCancelUser() {
        return cancelUser;
    }
    public void setCancelUser(String cancelUser) {
        this.cancelUser = cancelUser;
    }
    public String getMonth() {
        return month;
    }
    public void setMonth(String month) {
        this.month = month;
    }
    public String getCNNoticeNo() {
        return CNNoticeNo;
    }
    public void setCNNoticeNo(String cNNoticeNo) {
        CNNoticeNo = cNNoticeNo;
    }
    public String getCNDNCode() {
        return CNDNCode;
    }
    public void setCNDNCode(String cNDNCode) {
        CNDNCode = cNDNCode;
    }
    public String getCNDNNo() {
        return CNDNNo;
    }
    public void setCNDNNo(String cNDNNo) {
        CNDNNo = cNDNNo;
    }
    @Override
    public String toString() {
        int count = invoiceDetail.size();
        String details = "";
        if (null != invoiceDetail && count > 0) {
            details += "<InvoiceDetail count=\"" + count + "\">";
            for (InvoiceDetail invoiceDetailItem : invoiceDetail) {
                details += invoiceDetailItem.toString();
            }
            details += "</InvoiceDetail>";
        }
        
        
        return "<?xml version=\"1.0\" encoding=\"gbk\"?><InvoiceData><body><input>" 
                + "<SwiftNumber>" + (swiftNumber == null ? "" : swiftNumber)
                + "</SwiftNumber><InvType>" + (invType == null ? "" : invType) 
                + "</InvType><CreditNoteInv>" + (creditNoteInv== null ? "" : creditNoteInv) 
                + "</CreditNoteInv><CancelInvType>" + (cancelInvType == null ? "" : cancelInvType) 
                + "</CancelInvType><VenderTaxNo>" + (venderTaxNo == null ? "" : venderTaxNo) 
                + "</VenderTaxNo><VenderName>" + (venderName == null ? "" : venderName) 
                + "</VenderName><VenderAddressTel>" + (venderAddressTel == null ? "" : venderAddressTel)
                + "</VenderAddressTel><VenderBankNameNo>" + (venderBankNameNo == null ? "" : venderBankNameNo) 
                + "</VenderBankNameNo><CustomerTaxNo>" + (customerTaxNo == null ? "" : customerTaxNo) 
                + "</CustomerTaxNo><CustomerName>" + (customerName == null ? "" : customerName) 
                + "</CustomerName><CustomerAddressTel>" + (customerAddressTel == null ? "" : customerAddressTel) 
                + "</CustomerAddressTel><CustomerBankNameNo>" + (customerBankNameNo == null ? "" : customerBankNameNo) 
                + "</CustomerBankNameNo>"
                + details
                + "<SumTotalAmount>" + (sumTotalAmount == null ? "" : sumTotalAmount) 
                + "</SumTotalAmount><SumTaxAmount>" + (sumTaxAmount == null ? "" : sumTaxAmount) 
                + "</SumTaxAmount><Total>" + (total == null ? "" : total) 
                + "</Total><Remark>" + (remark == null ? "" : remark) 
                + "</Remark><Receiver>" + (receiver == null ? "" : receiver) 
                + "</Receiver><Checker>" + (checker == null ? "" : checker) 
                + "</Checker><Issuer>" + (issuer == null ? "" : issuer) 
                + "</Issuer><CancelUser>" + (cancelUser == null ? "" : cancelUser) 
                + "</CancelUser><Month>" + (month == null ? "" : month)  
                + "</Month><CNNoticeNo>" + (CNNoticeNo == null ? "" : CNNoticeNo) 
                + "</CNNoticeNo><CNDNCode>" + (CNDNCode == null ? "" : CNDNCode) 
                + "</CNDNCode><CNDNNo>" + (CNDNNo == null ? "" : CNDNNo) 
                + "</CNDNNo>" 
                + "</input></body></InvoiceData>";
    }
    
}
