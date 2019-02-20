package com.lvmama.vst.order.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.biz.po.BizBranchProp;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import com.lvmama.vst.comm.web.BusinessException;
/**
 * 后台下单产品商品查询接口
 * 
 * @author wenzhengtao
 *
 */
public interface IOrderProductQueryService {
	
	int countOrderProductList(Map<String,Object> params);

	public List<OrdOrderGoods> findOrderProductList(HashMap<String, Object> params) throws BusinessException;

	public List<OrdOrderProductVO> findOrderGoodsList(
			HashMap<String, Object> params, List<String> facilitiesForSwimPoor, boolean facilitiesForSwimPoorFlag, List<BizBranchProp> bizBedTypes) throws BusinessException;
	
	public List<OrdOrderGoods> getBizBranchPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO) throws BusinessException;
	
	public List<OrdOrderGoods> getBizCategoryPropByParams(
			OrdOrderProductQueryVO ordOrderProductQueryVO)
			throws BusinessException;
	
	/**
	 * 根据页面条件查询产品集合
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List<OrdOrderProductVO> findOrderProductVOList(HashMap<String, Object> params) throws BusinessException;
	
	/**
	 * 根据某一页的产品ID查询商品
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params) throws BusinessException;
}
