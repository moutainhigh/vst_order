package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizBranchProp;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.BranchPropClientService;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import com.lvmama.vst.back.prod.service.IOrdOrderProductService;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderProductQueryService;

@Service
public class OrderProductQueryServiceImpl implements IOrderProductQueryService {

	private Log log = LogFactory.getLog(OrderProductQueryServiceImpl.class);
	
	@Autowired
	private IOrdOrderProductService ordOrderProductServiceRemote;
	
	@Autowired
	private DictClientService dictClientService;

	@Autowired
	private CategoryPropClientService categoryPropClientService;
	
	@Autowired
	private BranchPropClientService branchPropClientService;

	@Override
	public List<OrdOrderGoods> findOrderProductList(
			HashMap<String, Object> params) throws BusinessException {
		List<OrdOrderGoods> ordList = null;
		try 
		{
			ResultHandleT<List<OrdOrderGoods>> resultHandle = ordOrderProductServiceRemote.findOrdOrderProductList(params);
			ordList = resultHandle.getReturnContent();
		} catch (Exception e) 
		{
			log.error("method findOrderProductList error", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordList;
	}
	
	public int countOrderProductList(Map<String,Object> params){
		Integer count = 0;
		ResultHandleT<Integer> result =	ordOrderProductServiceRemote.countOrderProductList(params);
		if(result.isSuccess()){
			count = result.getReturnContent();
		}
		if(count==null){
			return 0;
		}else{
			return count;
		}
	}

	private List<OrdOrderGoods>  findOrderGoodsList(HashMap<String, Object> params){		
		List<OrdOrderGoods> ordGoodsList=null;
		ResultHandleT<List<OrdOrderGoods>> resultOrder = ordOrderProductServiceRemote.findOrdOrderGoodsList(params);
		if(resultOrder.isSuccess()){
			ordGoodsList=resultOrder.getReturnContent();
		}		
		return ordGoodsList;
	}
	
	@Override
	public List<OrdOrderProductVO> findOrderGoodsList(
			HashMap<String, Object> params, List<String> facilitiesForSwimPoor,
			boolean facilitiesForSwimPoorFlag, List<BizBranchProp> bizBedTypes) throws BusinessException {
		List<OrdOrderProductVO> ordOrderProductVOList = new ArrayList<OrdOrderProductVO>();
		OrdOrderProductVO ordOrderProductVO = null;
		try {
			List<OrdOrderGoods> ordList = findOrderGoodsList(params);
			if (ordList != null && ordList.size() > 0) {
				// 过滤游泳池设施的酒店
				if (facilitiesForSwimPoorFlag) {
					Iterator<OrdOrderGoods> iter = ordList.iterator();
					while (iter.hasNext()) {
						boolean flag = false;
						String facilitieTemp = iter.next().getFacilities();
						if (StringUtils.isNotBlank(facilitieTemp)) {
							String[] facilities = facilitieTemp.split(",");
							for (String facilitie : facilitiesForSwimPoor) {
								flag = ArrayUtils.contains(facilities,
										facilitie);
							}
						}
						if (!flag) {
							iter.remove();
						}
					}
				}

				List<OrdOrderGoodsVO> ordOrderGoodsVOList = null;
				Long productId = 0l;
				for (OrdOrderGoods ordOrderGoods : ordList) {
					if (productId.longValue() == ordOrderGoods.getProductId()
							.longValue()) {
						OrdOrderGoodsVO ordOrderGoodsVO = new OrdOrderGoodsVO();
						BeanUtils
								.copyProperties(ordOrderGoods, ordOrderGoodsVO);
						// 取宽带服务
						if (StringUtils.isNotBlank(ordOrderGoods.getInternet())) {
							BizDict bizDict = dictClientService.findDictById(Long.valueOf(ordOrderGoods.getInternet())).getReturnContent();
							ordOrderGoodsVO.setStrInternet(bizDict
									.getDictName());
						}
						//取床型名称
						ordOrderGoodsVO.setStrBedType("");
						if(StringUtils.isNotBlank(ordOrderGoods.getBedType())){
							if(bizBedTypes!=null && bizBedTypes.get(0)!=null && ((BizBranchProp)bizBedTypes.get(0)).getDictList().size()>0){
								for (BizDict bizDict : ((BizBranchProp)bizBedTypes.get(0)).getDictList()) {
									if(bizDict.getDictId().longValue() == Long.valueOf(ordOrderGoods.getBedType())){
										ordOrderGoodsVO.setStrBedType(bizDict.getDictName());
									}
								}
							}
						}
						ordOrderGoodsVOList.add(ordOrderGoodsVO);
					} else {
						productId = ordOrderGoods.getProductId();
						ordOrderProductVO = new OrdOrderProductVO();
						ordOrderGoodsVOList = new ArrayList<OrdOrderGoodsVO>();
						OrdOrderGoodsVO ordOrderGoodsVO = new OrdOrderGoodsVO();
						BeanUtils
								.copyProperties(ordOrderGoods, ordOrderGoodsVO);
						// 取宽带服务
						if (StringUtils.isNotBlank(ordOrderGoods.getInternet())) {
							BizDict bizDict = dictClientService.findDictById(Long.valueOf(ordOrderGoods.getInternet())).getReturnContent();
							ordOrderGoodsVO.setStrInternet(bizDict.getDictName());
						}
						//取床型名称
						ordOrderGoodsVO.setStrBedType("");
						if(StringUtils.isNotBlank(ordOrderGoods.getBedType())){
							if(bizBedTypes!=null && bizBedTypes.get(0)!=null && ((BizBranchProp)bizBedTypes.get(0)).getDictList().size()>0){
								for (BizDict bizDict : ((BizBranchProp)bizBedTypes.get(0)).getDictList()) {
									if(bizDict.getDictId().longValue() == Long.valueOf(ordOrderGoods.getBedType())){
										ordOrderGoodsVO.setStrBedType(bizDict.getDictName());
									}
								}
							}
						}
						
						ordOrderGoodsVOList.add(ordOrderGoodsVO);
						ordOrderProductVO.setProductId(ordOrderGoods
								.getProductId());
						ordOrderProductVO.setProductName(ordOrderGoods
								.getProductName());
						ordOrderProductVO.setStarRate(ordOrderGoods
								.getStarRate());
						ordOrderProductVO
								.setAddress(ordOrderGoods.getAddress());
						ordOrderProductVO.setEstablishmentDate(ordOrderGoods
								.getEstablishmentDate());
						ordOrderProductVO.setRenovationDate(ordOrderGoods
								.getRenovationDate());
						ordOrderProductVO.setDistrictName(ordOrderGoods
								.getDistrictName());
						ordOrderProductVO
								.setOrdOrderGoodsVOList(ordOrderGoodsVOList);
						ordOrderProductVOList.add(ordOrderProductVO);
					}
				}

			}

		} catch (Exception e) {
			log.error("method findOrderGoodsList error", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordOrderProductVOList;
	}

	@Override
	public List<OrdOrderGoods> getBizBranchPropByParams(
			OrdOrderProductQueryVO ordOrderProductQueryVO)
			throws BusinessException {
		List<OrdOrderGoods> ordList = null;
		try {
			ResultHandleT<List<OrdOrderGoods>> resultHanle = ordOrderProductServiceRemote.getBizBranchPropByParams(ordOrderProductQueryVO);
			if(resultHanle.isSuccess()){
				ordList = resultHanle.getReturnContent();
			}
		} catch (Exception e) {
			log.error("method getBizBranchPropByParams error", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordList;
	}
	
	@Override
	public List<OrdOrderGoods> getBizCategoryPropByParams(
			OrdOrderProductQueryVO ordOrderProductQueryVO)
			throws BusinessException {
		List<OrdOrderGoods> ordList = null;
		try {
			ResultHandleT<List<OrdOrderGoods>> resultHanle  = ordOrderProductServiceRemote.getBizCategoryPropByParams(ordOrderProductQueryVO);
			if(resultHanle.isSuccess()){
				ordList = resultHanle.getReturnContent();
			}
		} catch (Exception e) {
			log.error("method getBizCategoryPropByParams error", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordList;
	}
	
	/**
	 * 根据页面条件查询产品
	 */
	@Override
	public List<OrdOrderProductVO> findOrderProductVOList(HashMap<String, Object> params) throws BusinessException {
		List<OrdOrderProductVO> ordOrderProductVOList = null;
		ResultHandleT<List<OrdOrderProductVO>> resultHanle = ordOrderProductServiceRemote.findOrdOrderProductVOList(params);
		if(resultHanle.isSuccess()){
			ordOrderProductVOList = resultHanle.getReturnContent();
		}
		return ordOrderProductVOList;
	}
	
	/**
	 * 根据某一页的产品查询商品
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params) throws BusinessException {
		List<OrdOrderGoodsVO> ordOrderGoodsVOList = null;
		ResultHandleT<List<OrdOrderGoodsVO>> resultHanle = ordOrderProductServiceRemote.findOrdOrderGoodsVOList(params);
		if(resultHanle.isSuccess()){
			ordOrderGoodsVOList = resultHanle.getReturnContent();
		}
		List<Long> districtIds =new ArrayList<Long>();
		for (OrdOrderGoodsVO ordOrderGoodsVO : ordOrderGoodsVOList) {
			if(ordOrderGoodsVO.getDistrictId()!=null){
				districtIds.add(ordOrderGoodsVO.getDistrictId());
			}
		}
		
		if(!districtIds.isEmpty()){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("districtIds", districtIds);
			List<BizDistrict> mainList =null;
			ResultHandleT<List<BizDistrict>> resultBiz = ordOrderProductServiceRemote.findDistrictByDistrictIds(map);
			if(resultBiz.isSuccess()){
				mainList = resultBiz.getReturnContent();
			}
			map.clear();
			for (BizDistrict bizDistrict : mainList) {
				if(!map.containsKey(""+bizDistrict.getDistrictId())){
					map.put(""+bizDistrict.getDistrictId(), bizDistrict);
				}
			}
			for (OrdOrderGoodsVO ordOrderGoodsVO : ordOrderGoodsVOList) {
				if(ordOrderGoodsVO.getDistrictId()!=null){
					ordOrderGoodsVO.setDistrictName(((BizDistrict)map.get(""+ordOrderGoodsVO.getDistrictId())).getDistrictName());
				}
			}
		}
		
		
		return ordOrderGoodsVOList;
	}

}
