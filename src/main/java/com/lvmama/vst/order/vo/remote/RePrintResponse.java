package com.lvmama.vst.order.vo.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 调用百旺重新打印发票接口响应内容封装类
 * @author WangSizhi
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RePrintResponse {
    /**开票端编号 ,每个开票点对应唯一编号*/
    @XmlElement(name="CLIENTNO")
    private String clientNo;
    /**流水号*/
    @XmlElement(name="SwiftNumber")
    private String swiftNumber;
    /**发票代码 开票、红冲成功返回*/
    @XmlElement(name="InvCode")
    private String invCode;
    /**发票号码 开票、红冲成功返回*/
    @XmlElement(name="InvNo")
    private String invNo;
    /**重新打印是否成功 0 成功 1失败*/
    @XmlElement(name="OperateFlag")
    private int operateFlag;
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
    public int getOperateFlag() {
        return operateFlag;
    }
    public void setOperateFlag(int operateFlag) {
        this.operateFlag = operateFlag;
    }
    public String getReturnMsg() {
        return returnMsg;
    }
    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
    @Override
    public String toString() {
        return "RePrintResponse [clientNo=" + clientNo + ", swiftNumber="
                + swiftNumber + ", invCode=" + invCode + ", invNo=" + invNo
                + ", operateFlag=" + operateFlag + ", returnMsg=" + returnMsg
                + "]";
    }

}
