package com.lvmama.vst.order.vo.remote;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 调用百旺开具发票接口响应内容封装类
 * @author WangSizhi
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CallServiceResponse {
    /**开票端编号 ,每个开票点对应唯一编号*/
    @XmlElement(name="CLIENTNO")
    private String clientNo;
    /**流水号*/
    @XmlElement(name="SwiftNumber")
    private String swiftNumber;
    /**单据号 （通常情况一个单据号对应唯一的发票代码、发票号码，对于合并开票操作SysInvNo参数由多个单据号组成并用逗号分隔）*/
    @XmlElement(name="SysInvNo")
    private String sysInvNo;
    /**发票代码 开票、红冲成功返回*/
    @XmlElement(name="InvCode")
    private String invCode;
    /**发票号码 开票、红冲成功返回*/
    @XmlElement(name="InvNo")
    private String invNo;
    /**开票日期 开票、红冲成功返回*/
    @XmlElement(name="InvDate")
    private Date invDate;
    /**作废日期 作废成功返回*/
    @XmlElement(name="CancelDate")
    private Date cancelDate;
    /**开票、红冲、作废操作是否成功   0 成功 1失败*/
    @XmlElement(name="OperateFlag")
    private int operateFlag;
    /**打印是否成功   0 成功 1失败（作废返回与OperateFlag一致）*/
    @XmlElement(name="PrintFlag")
    private int printFlag;
    /**成功或错误信息  成功或操作返回错误信息、格式验证错误信息*/
    @XmlElement(name="returnmsg")
    private String returnMsg;
    
    public String getClientNo() {
        return clientNo;
    }
    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }
    public String getSwiftNumber() {
        return swiftNumber;
    }
    public void setSwiftNumber(String swiftNumber) {
        this.swiftNumber = swiftNumber;
    }
    public String getSysInvNo() {
        return sysInvNo;
    }
    public void setSysInvNo(String sysInvNo) {
        this.sysInvNo = sysInvNo;
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
    public Date getInvDate() {
        return invDate;
    }
    public void setInvDate(Date invDate) {
        this.invDate = invDate;
    }
    public Date getCancelDate() {
        return cancelDate;
    }
    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }
    public int getOperateFlag() {
        return operateFlag;
    }
    public void setOperateFlag(int operateFlag) {
        this.operateFlag = operateFlag;
    }
    public int getPrintFlag() {
        return printFlag;
    }
    public void setPrintFlag(int printFlag) {
        this.printFlag = printFlag;
    }
    public String getReturnMsg() {
        return returnMsg;
    }
    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
    @Override
    public String toString() {
        return "CallServiceResponse [clientNo=" + clientNo + ", swiftNumber="
                + swiftNumber + ", sysInvNo=" + sysInvNo + ", invCode="
                + invCode + ", invNo=" + invNo + ", invDate=" + invDate
                + ", cancelDate=" + cancelDate + ", operateFlag=" + operateFlag
                + ", printFlag=" + printFlag + ", returnMsg=" + returnMsg + "]";
    }
    
}
