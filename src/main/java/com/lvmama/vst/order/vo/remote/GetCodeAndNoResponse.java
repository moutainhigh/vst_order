package com.lvmama.vst.order.vo.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 调用百旺税控盘查询接口响应内容封装类
 * @author WangSizhi
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetCodeAndNoResponse {
/*    *//**主标识*//*
    @XmlAttribute
    private int id;*/
    /**开票端编号 ,每个开票点对应唯一编号*/
    @XmlElement(name="CLIENTNO")
    private String clientNo;
    /**发票类型代码*/
    @XmlElement(name="fplxdm")
    private String fplxdm;
    /**当前发票代码   由发票类型代码查询得到的当前发票代码*/
    @XmlElement(name="dqfpdm")
    private String dqfpdm;
    /**当前发票号码   由发票类型代码查询得到的当前发票号码*/
    @XmlElement(name="dqfphm")
    private String dqfphm;
    /**税控盘查询操作是否成功  0 成功 1 失败*/
    @XmlElement(name="OperateFlag")
    private int operateFlag;
    /**成功或错误信息*/
    @XmlElement(name="returnmsg")
    private String returnMsg;

/*    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }*/
    public String getClientNo() {
        return clientNo;
    }
    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }
    public String getFplxdm() {
        return fplxdm;
    }
    public void setFplxdm(String fplxdm) {
        this.fplxdm = fplxdm;
    }
    public String getDqfpdm() {
        return dqfpdm;
    }
    public void setDqfpdm(String dqfpdm) {
        this.dqfpdm = dqfpdm;
    }
    public String getDqfphm() {
        return dqfphm;
    }
    public void setDqfphm(String dqfphm) {
        this.dqfphm = dqfphm;
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
        return "GetCodeAndNoResponse [clientNo=" + clientNo + ", fplxdm="
                + fplxdm + ", dqfpdm=" + dqfpdm + ", dqfphm=" + dqfphm
                + ", operateFlag=" + operateFlag + ", returnMsg=" + returnMsg
                + "]";
    }

}
