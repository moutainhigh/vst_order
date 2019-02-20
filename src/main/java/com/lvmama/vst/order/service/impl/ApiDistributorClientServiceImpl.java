package com.lvmama.vst.order.service.impl;

import com.lvmama.order.vst.api.common.service.IApiDistributorClientService;
import com.lvmama.order.vst.api.common.vo.request.DistributorVo;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 下单渠道桥接服务
 *
 * @author houjian
 * @date 2018/5/9.
 */
@Service("apiDistributorClientService")
public class ApiDistributorClientServiceImpl implements IApiDistributorClientService {
    @Autowired
    private DistributorClientService distributorClientService;

    @Override
    public List<DistributorVo> findDistributorList(Map<String, Object> params) {
        List<DistributorVo> distributorVoList = new ArrayList<>();
        if (params == null)
            return distributorVoList;
        com.lvmama.vst.comm.vo.ResultHandleT<List<com.lvmama.vst.back.dist.po.Distributor>> source = this.distributorClientService.findDistributorList(params);
        if (source == null)
            return distributorVoList;
        if (CollectionUtils.isEmpty(source.getReturnContent()))
            return distributorVoList;
        for (com.lvmama.vst.back.dist.po.Distributor distributor : source.getReturnContent()) {
            DistributorVo distributorVo = new DistributorVo();
            EnhanceBeanUtils.copyProperties(distributor, distributorVo);
            distributorVoList.add(distributorVo);
        }
        return distributorVoList;
    }
}
