package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdPassCode;

public interface IOrdPassCodeService {

    public List<OrdPassCode> findByParams(Map<String, Object> params);
}
