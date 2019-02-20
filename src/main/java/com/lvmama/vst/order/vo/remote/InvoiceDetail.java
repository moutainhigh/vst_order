package com.lvmama.vst.order.vo.remote;


public class InvoiceDetail {
    /**商品个数编号*/
    private Integer xh;
    /**发票行性质, 默认传“0”*/
    private Integer fphxz;
    /**商品名称*/
    private String productName;
    /**规格型号*/
    private String productSize;
    /**计量单位*/
    private String productUnit;
    /**数量*/
    private Long productAmount;
    /**单价*/
    private Double unitPrice;
    /**金额*/
    private Double totalAmount;
    /**税率*/
    private Double taxRate;
    /**税额*/
    private Double taxAmount;
    /**含税标志, 0-不含税 1-含税*/
    private Integer taxMark;
    
    public Integer getXh() {
        return xh;
    }
    public void setXh(Integer xh) {
        this.xh = xh;
    }
    public Integer getFphxz() {
        return fphxz;
    }
    public void setFphxz(Integer fphxz) {
        this.fphxz = fphxz;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductSize() {
        return productSize;
    }
    public void setProductSize(String productSize) {
        this.productSize = productSize;
    }
    public String getProductUnit() {
        return productUnit;
    }
    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }
    public Long getProductAmount() {
        return productAmount;
    }
    public void setProductAmount(Long productAmount) {
        this.productAmount = productAmount;
    }
    public Double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public Double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public Double getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
    public Double getTaxAmount() {
        return taxAmount;
    }
    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }
    public Integer getTaxMark() {
        return taxMark;
    }
    public void setTaxMark(Integer taxMark) {
        this.taxMark = taxMark;
    }
    @Override
    public String toString() {
        return "<group xh=\"" + (xh == null ? 0 : xh) + "\"><fphxz>" + (fphxz == null ? "" : fphxz) 
                + "</fphxz><ProductName>" + (productName == null ? "" : productName) 
                + "</ProductName><ProductSize>" + (productSize == null ? "" : productSize) 
                + "</ProductSize><ProductUnit>" + (productUnit == null ? "" : productUnit) 
                + "</ProductUnit><ProductAmount>" + (productAmount == null ? "" : productAmount)
                + "</ProductAmount><UnitPrice>" + (unitPrice  == null ? "" : unitPrice) 
                + "</UnitPrice><TotalAmount>" + (totalAmount == null ? "" : totalAmount) 
                + "</TotalAmount><TaxRate>" + (taxRate == null ? "" : taxRate) 
                + "</TaxRate><TaxAmount>" + (taxAmount == null ? "" : taxAmount) 
                + "</TaxAmount><TaxMark>" + (taxMark == null ? "" : taxMark) 
                + "</TaxMark></group>";
    }
    
}
