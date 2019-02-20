package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.goods.po.SuppGoodsReschedule;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdItemReschedule;
import com.lvmama.vst.order.dao.OrdItemAdditionStatusDAO;
import com.lvmama.vst.order.dao.OrdItemPersonRelationDao;
import com.lvmama.vst.order.dao.OrdItemRescheduleDao;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdItemRescheduleService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdItemRescheduleServiceImpl implements IOrdItemRescheduleService {

	private static final Log LOG = LogFactory.getLog(OrdItemRescheduleServiceImpl.class);

    @Autowired
    private OrdItemRescheduleDao ordItemRescheduleDao;
    
    @Override
    public int addOrdItemReschedule(OrdItemReschedule ordItemReschedule) {
        return ordItemRescheduleDao.insertSelective(ordItemReschedule);
    }

    @Override
    public OrdItemReschedule findOrdItemRescheduleById(Long ordItemRescheduleId) {
        return ordItemRescheduleDao.selectByPrimaryKey(ordItemRescheduleId);
    }

    @Override
    public int updateOrdItemReschedule(OrdItemReschedule ordItemReschedule) {
        return ordItemRescheduleDao.updateByPrimaryKeySelective(ordItemReschedule);
    }

    @Override
    public OrdItemReschedule findOrdItemRescheduleByOrdItemId(Long ordItemId) {
        return ordItemRescheduleDao.selectByOrderItemId(ordItemId);
    }

    @Override
    public SuppGoodsReschedule toSuppGoodsReschedule(Long ordItemId) {
        SuppGoodsReschedule suppGoodsReschedule = null;
        OrdItemReschedule ordItemReschedule = findOrdItemRescheduleByOrdItemId(ordItemId);
        if(null!=ordItemReschedule && StringUtils.isNotBlank(ordItemReschedule.getRescheduleRules())){
            JSONObject jsonObject = JSONObject.fromObject(ordItemReschedule.getRescheduleRules());
            suppGoodsReschedule= (SuppGoodsReschedule)jsonObject.toBean(jsonObject, SuppGoodsReschedule.class);
        }
        return suppGoodsReschedule;
    }

    @Override
    public int updateExchangeCountByOrdItemId(Long ordItemId) {
        return ordItemRescheduleDao.updateExchangeCountByOrdItemId(ordItemId);
    }
}
