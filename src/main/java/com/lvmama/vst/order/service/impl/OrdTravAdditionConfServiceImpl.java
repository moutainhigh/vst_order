package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdTravAdditionConf;
import com.lvmama.vst.order.dao.OrdTravAdditionConfDAO;
import com.lvmama.vst.order.service.IOrdTravAdditionConfService;

@Service("ordTravAdditionConfService")
public class OrdTravAdditionConfServiceImpl implements IOrdTravAdditionConfService{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrdTravAdditionConfServiceImpl.class);
	
	@Autowired
	private OrdTravAdditionConfDAO ordTravAdditionConfDAO;

	@Override
	public int saveTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf) {
		return ordTravAdditionConfDAO.insert(ordTravAdditionConf);
	}

	@Override
	public List<OrdTravAdditionConf> queryOrdTravAdditionConfByParam(
            Map<String, Object> param) {
        return ordTravAdditionConfDAO.selectByParam(param);
    }

    @Override
    public int updateTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf) {
        return ordTravAdditionConfDAO.updateByPrimaryKey(ordTravAdditionConf);
    }

	
}
