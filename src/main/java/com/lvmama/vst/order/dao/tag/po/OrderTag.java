package com.lvmama.vst.order.dao.tag.po;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2018/6/5.
 * 用于表字段扩容(公共)
 */
public class OrderTag implements Serializable {
    private Long tagId;//主键
    private Long objectId;//业务id(订单id/子订单id/或者其他业务id)
    private String objectType;//业务类型(ORD_ORDER/ORD_ITEM/OTHER)
    private String tagValue;//标志位(用于标志业务值)
    private String tagType;//标志位类型
    private String operatorName;
    private Date createTime;
    private Date udpateTime;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUdpateTime() {
        return udpateTime;
    }

    public void setUdpateTime(Date udpateTime) {
        this.udpateTime = udpateTime;
    }

    @Override
    public String toString() {
        return "OrderTag{" +
                "tagId=" + tagId +
                ", objectId=" + objectId +
                ", objectType='" + objectType + '\'' +
                ", tagValue='" + tagValue + '\'' +
                ", tagType='" + tagType + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", createTime=" + createTime +
                ", udpateTime=" + udpateTime +
                '}';
    }

    public OrderTag(Long objectId, String objectType, String tagValue, String tagType) {
        this.objectId = objectId;
        this.objectType = objectType;
        this.tagValue = tagValue;
        this.tagType = tagType;
    }
    public OrderTag(){}
}
