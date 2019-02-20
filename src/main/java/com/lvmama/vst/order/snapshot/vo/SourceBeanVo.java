package com.lvmama.vst.order.snapshot.vo;

import com.lvmama.vst.back.prod.po.PropValue;

import java.util.List;

/**
 * 源数据VO
 */
public class SourceBeanVo {
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
    public static SourceBeanVo install(List<PropValue> propValueList){
        SourceBeanVo vo =new SourceBeanVo();
        vo.setPropValueList(propValueList);
        return vo;
    }
}
