package com.lvmama.vst.order.snapshot.async.impl;

import com.lvmama.comm.bee.po.distribution.DistributorInfo;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.order.snapshot.api.gateway.IProductGatewaySnapshotService;
import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.enums.SnapshotApiEnum;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductVo;
import com.lvmama.order.snapshot.comm.util.OrdSnapshotObjectUtils;
import com.lvmama.order.snapshot.comm.util.OrdSnapshotUtils;
import com.lvmama.order.snapshot.comm.vo.param.OrdOrderSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.order.snapshot.util.ResponseBodyUtils;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductPropClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.TntDistributorServiceAdapter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VST产品快照网关服务
 */
@Service("vstProducSnapshotGatewayService")
public class VstProductGatewaySnapshotServiceImpl implements IProductGatewaySnapshotService {
	@Resource
	private PermUserServiceAdapter permUserServiceAdapter;
	@Resource
	private TntDistributorServiceAdapter tntDistributorServiceRemote;
	@Resource
	private ProdProductPropClientService prodProductPropClientService;
	// 注入分销商业务接口(订单来源、下单渠道)
	@Resource
	private DistributorClientService distributorClientService;
	@Resource
	private DistrictClientService districtClientService;
	@Resource
	private ProdProductClientService prodProductClientService;
	@Resource
	private FavorServiceAdapter favorService;
	@Resource
	private SuppSupplierClientService suppSupplierClientService;
	@Resource
	private DictDefClientService dictDefClientService;

	@Override
	public ResponseBody<String> putDisChannelName(OrdOrderSnapshotVo ordOrderSnapshot) {
		if(ordOrderSnapshot.getDistributionChannel() != null) {
			ResultHandleT<DistributorInfo> distributorInfoResult = tntDistributorServiceRemote.getDistributorById(ordOrderSnapshot.getDistributionChannel());
			if (distributorInfoResult.isFail() || distributorInfoResult.getReturnContent() == null) {
				return ResponseBodyUtils.exception(SnapshotApiEnum.BUSSINESS_CODE.OTHER
						,new BusinessException("获取渠道代码" + ordOrderSnapshot.getDistributionChannel() + "对应的渠道信息失败"));
			}
			if(distributorInfoResult.getReturnContent() != null) {
				ordOrderSnapshot.putContent(Snapshot_Detail_Enum.PRODUCT_KEY.disChannelName.name()
						,distributorInfoResult.getReturnContent().getDistributorName());
			}
		}
		return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
	}
	@Override
	public ResponseBody<String> putDistributorName(OrdOrderSnapshotVo ordOrderSnapshot) {
		//下单渠道
		Distributor distributor = distributorClientService.findDistributorById(ordOrderSnapshot.getDistributorId()).getReturnContent();

		ordOrderSnapshot.putContent(Snapshot_Detail_Enum.PRODUCT_KEY.distributorName.name(), distributor.getDistributorName());

		return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
	}
	@Override
	public ResponseBody<String> putProductManager(OrdOrderSnapshotVo ordOrderSnapshot, ProdProductVo prodProductVo) {
		PermUser permUser= null;
		if(prodProductVo.getManagerId() != null){
			permUser = permUserServiceAdapter.getPermUserByUserId(prodProductVo.getManagerId());
		}
		StringBuilder sb = new StringBuilder();
		if (permUser!=null) {
			sb.append(permUser.getRealName());
			sb.append("(");
			sb.append(permUser.getUserName());
			sb.append(")");
		}
		ordOrderSnapshot.putContent(Snapshot_Detail_Enum.PRODUCT_KEY.productManager.name(),sb.toString());

		return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
	}
	@Override
	public ResponseBody<String> putOrderCancelTypeList(OrdOrderSnapshotVo ordOrderSnapshot){
		try {
			Map<String, Object> dictDefPara = new HashMap<String, Object>();
			dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
			dictDefPara.put("cancelFlag", "Y");
			List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
			OrdSnapshotObjectUtils.putContent(ordOrderSnapshot
					, Snapshot_Detail_Enum.PRODUCT_KEY.orderCancelTypeList.name(), dictDefs);

			return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
		}catch (Exception e1) {
			return ResponseBodyUtils.exception(SnapshotApiEnum.BUSSINESS_CODE.OTHER
					,new BusinessException("findDictDefList failure：" +e1.getMessage()));
		}
	}
	@Override
	public ResponseBody<String> putHotelTel(OrdOrderSnapshotVo ordOrderSnapshot, ProdProductVo prodProductVo) {
		/** 给订单添加酒店联系电话 =========start*/
		Map<String,Object> propParams = new HashMap<String, Object>();
		propParams.put("productId", prodProductVo.getProductId());
		propParams.put("propId", 5L);
		ResultHandleT<List<ProdProductProp>> resultHandleT = prodProductPropClientService.findProdProductPropList(propParams);
		List<ProdProductProp> prodProductPropList = resultHandleT.getReturnContent();
		StringBuffer hotelTels = new StringBuffer();
		if(prodProductPropList != null && prodProductPropList.size() > 0){
			for(ProdProductProp prodProductProp : prodProductPropList){
				if(StringUtil.isNotEmptyString(prodProductProp.getPropValue())){
					hotelTels.append(prodProductProp.getPropValue()+"  ");
				}
			}
		}
		if(hotelTels.length() < 1){
			hotelTels.append("无");
		}
		ordOrderSnapshot.putContent(Snapshot_Detail_Enum.PRODUCT_KEY.hotelTel.name(), hotelTels.toString());

		return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
	}
	@Override
	public ResponseBody<String> putTotalOrderAmount(OrdOrderSnapshotVo ordOrderSnapshot, OrderParamVo orderParamVo){
		try {
			//促销减少总金额
			String totalOrderAmount = orderParamVo.getOrderAmountItemByType();
			ordOrderSnapshot.putContent(Snapshot_Detail_Enum.PRODUCT_KEY.totalOrderAmount.name(), totalOrderAmount);

			return ResponseBodyUtils.success(ordOrderSnapshot.getContent());
		} catch (Exception e1) {
			return ResponseBodyUtils.exception(SnapshotApiEnum.BUSSINESS_CODE.OTHER
					,new BusinessException("putOrderAmount failure：" +e1.getMessage()));
		}
	}
	@Override
	public ResponseBody<String> putSuppSupplier(OrderItemParamVo orderItemParamVo){
		ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItemParamVo.getSupplierId());
		if (resultHandleSuppSupplier.isFail()) {
			return ResponseBodyUtils.exception(SnapshotApiEnum.BUSSINESS_CODE.OTHER
					,new BusinessException("putSuppSupplier isFial " + resultHandleSuppSupplier.getMsg()));
		}
		OrdSnapshotObjectUtils.putContent(orderItemParamVo.getContentMap(), Snapshot_Detail_Enum.PRODUCT_KEY.suppSupplier.name()
				, resultHandleSuppSupplier.getReturnContent());
		return ResponseBodyUtils.success(orderItemParamVo.getContent());
	}
	@Override
	public ResponseBody<String> putCity(OrderItemParamVo orderItemParamVo, ProdProductVo prodProductVo) {
		Long bizDistrictId = null;
		if (CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(orderItemParamVo.getBuCode())) {
			Object districtObj = orderItemParamVo.getContentValueByKey(OrderEnum.HOTEL_CONTENT.bizDistrictId.name());
			if (districtObj != null) {
				bizDistrictId = Long.valueOf(districtObj.toString());
			}
		}else{
			bizDistrictId =prodProductVo.getBizDistrictId();
		}
		String city = getCity(bizDistrictId);
		OrdSnapshotUtils.put(orderItemParamVo.getContentMap(), Snapshot_Detail_Enum.PRODUCT_KEY.city.name(), city);

		return ResponseBodyUtils.success(orderItemParamVo.getContent());
	}
	/**
	 * getCity
	 * @param bizDistrictId
	 * @return
	 */
	private String getCity(Long bizDistrictId){
		if (bizDistrictId == null) {
			return "";
		}
		String city = "";
		ResultHandleT<HashMap<String, BizDistrict>> resultHandlt=districtClientService.findCompleteDistrictById(bizDistrictId);
		HashMap<String, BizDistrict> districtMap=resultHandlt.getReturnContent();
		BizDistrict bizDistrict=districtMap.get(BizDistrict.DISTRICT_TYPE.CITY.name());
		if (bizDistrict!=null) {
			city=bizDistrict.getDistrictName();
		}
		return city;
	}
}
