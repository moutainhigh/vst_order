package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrderDisneyInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by luoweiyi on 2016/3/4.
 */
public interface IOrdOrderDisneyInfoService {
    public List<OrdOrderDisneyInfo> findByParams(Map<String,Object> params);

    public Integer insert(OrdOrderDisneyInfo ordOrderDisneyInfo);
}
