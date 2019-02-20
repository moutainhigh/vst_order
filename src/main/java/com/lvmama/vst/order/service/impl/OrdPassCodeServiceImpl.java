/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.service.IOrdPassCodeService;

@Service("ordPassCodeService")
public class OrdPassCodeServiceImpl implements IOrdPassCodeService{

    @Autowired
    private OrdPassCodeDao ordPassCodeDao;

    @Override
    public List<OrdPassCode> findByParams(Map<String, Object> params) {
        return ordPassCodeDao.findByParams(params);
    }
}

