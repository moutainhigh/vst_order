package com.lvmama.vst.order.snapshot.vo;

import com.lvmama.order.snapshot.comm.po.prod.PropValue;

import java.util.List;

/**
 * 目标数据VO
 */
public class TargetBeanVo {
    private List<PropValue> propValueList;

    public List<PropValue> getPropValueList() {
        return propValueList;
    }

    public void setPropValueList(List<PropValue> propValueList) {
        this.propValueList = propValueList;
    }
    /**
     * 实例化对象
     * @param propValueList
     * @return
     */
    public static TargetBeanVo install(){
        return new TargetBeanVo();
    }
}
