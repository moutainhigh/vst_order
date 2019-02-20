package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.supp.po.SuppGoodsBlackList;
import com.lvmama.vst.back.supp.po.SuppGoodsIDCardLimit;
import com.lvmama.vst.back.supp.po.SuppGoodsLimit;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdBlackListDao;
import com.lvmama.vst.order.dao.OrdIDCardAgeLimitDao;
import com.lvmama.vst.order.dao.OrdLimitDao;

@Service("blackListServiceRemote")
public class OrdBlackListServiceImpl implements com.lvmama.vst.back.client.goods.service.SuppGoodsBlackListService {

	private static final Log LOGGER = LogFactory.getLog(OrdBlackListServiceImpl.class);
	@Autowired
	private OrdBlackListDao ordBlackListDao;
	
	@Autowired
	private OrdLimitDao ordLimitDao;
	@Autowired
	private OrdIDCardAgeLimitDao ordIDCardAgeLimitDao;

	@Override
	public SuppGoodsLimit findVisitTimeLimitList(Long suppGoodId)
			throws BusinessException {
		SuppGoodsLimit suppGoodsLimit = (SuppGoodsLimit) MemcachedUtil
				.getInstance().get(
						Constant.MEM_CACH_KEY.VST_SUPP_GOODS_LIMIT_.toString()
								+ suppGoodId);
		if(suppGoodsLimit != null){
			LOGGER.info("从缓存中获取商品限制信息:"+suppGoodId);
		}else{
			suppGoodsLimit = ordLimitDao.selectByGoodKey(suppGoodId);
			//商品限制信息加入缓存，缓存10分钟
			MemcachedUtil.getInstance().set(
					Constant.MEM_CACH_KEY.VST_SUPP_GOODS_LIMIT_.toString()
							+ suppGoodId, 60*10, suppGoodsLimit);
		}
		return suppGoodsLimit;
	}
	
	/**
	 * 给后台管理用
	 */
	@Override
	public SuppGoodsLimit findDBVisitTimeLimitList(Long suppGoodId)
			throws BusinessException {
		return ordLimitDao.selectByGoodKey(suppGoodId);
	}

	@Override
	public List<SuppGoodsBlackList> findPhoneList(Map<String, Object> params)
			throws BusinessException {
		return ordBlackListDao.selectByParams(params);
	}

	@Override
	public List<SuppGoodsBlackList> findIDCARDList(Map<String, Object> params)
			throws BusinessException {
		return ordBlackListDao.selectByParams(params);
	}

	@Override
	public int addBlackList(SuppGoodsBlackList suppBlackList)
			throws BusinessException {
		return ordBlackListDao.insert(suppBlackList);
	}

	@Override
	public int addLimitList(SuppGoodsLimit suppLimitList)
			throws BusinessException {
		return ordLimitDao.insert(suppLimitList);
	}

	@Override
	public int updateBlackList(SuppGoodsBlackList suppBlackList)
			throws BusinessException {
		return ordBlackListDao.updateByPrimaryKeySelective(suppBlackList);
	}

	@Override
	public int updateLimitList(SuppGoodsLimit suppGoodsLimit)
			throws BusinessException {
		return ordLimitDao.updateByPrimaryKeySelective(suppGoodsLimit);
	}

	@Override
	public int deleteBlackListById(Long id) throws BusinessException {
		return ordBlackListDao.deleteByPrimaryKey(id);
	}

	@Override
	public int deleteLimitListById(Long id) throws BusinessException {
		return ordLimitDao.deleteByPrimaryKey(id);
	}

	@Override
	public Long queryCount(Map<String, Object> params) {
		return ordBlackListDao.queryCount(params);
	}

	@Override
	public Long findGoodsIdByBlackId(Long blacklistId) {
		return ordBlackListDao.findGoodsIdByBlackId(blacklistId);
	}

	@Override
	public int addIDCardAgeLimit(SuppGoodsIDCardLimit suppGoodsIDCardLimit) throws BusinessException {
		return ordIDCardAgeLimitDao.insert(suppGoodsIDCardLimit);
	}

	@Override
	public List<SuppGoodsIDCardLimit> findIDCardAgeLimitList(Long suppGoodsId) throws BusinessException {
		return ordIDCardAgeLimitDao.selectByGoodsIdKey(suppGoodsId);
	}

	@Override
	public int deleteIDCardLimitByKey(Long idCardAgelimitId) throws BusinessException {
		return ordIDCardAgeLimitDao.deleteByPrimaryKey(idCardAgelimitId);
	}

	@Override
	public int updateIDCardLimitByKey(SuppGoodsIDCardLimit suppGoodsIDCardLimit) throws BusinessException {
		return ordIDCardAgeLimitDao.updateByPrimaryKey(suppGoodsIDCardLimit);
	}

	@Override
	public SuppGoodsIDCardLimit findSuppGoodsIdcardLimitById(Long idCardAgelimitId) {
		return ordIDCardAgeLimitDao.findSuppGoodsIdcardLimitById(idCardAgelimitId);
	}
}
