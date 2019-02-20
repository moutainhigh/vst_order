package com.lvmama.vst.order.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.goods.service.ISuppGoodsBranchPropClientService;
import com.lvmama.vst.order.adaptor.hotel.OrderProductQueryServiceAdaptor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.biz.po.BizBranchProp;
import com.lvmama.vst.back.biz.po.BizCategoryProp;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizDistrict.DISTRICT_TYPE;
import com.lvmama.vst.back.client.biz.service.BranchPropClientService;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderProductQueryService;

/**
 * 后台下单的产品、商品搜索重构
 * 
 * @author wenzhengtao
 * @date 2013-12-31
 * 
 */
@Controller
public class OrderProductGoodsSearchAction extends BaseActionSupport implements Serializable{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -4202451464063888166L;
	
	/**
	 * 日志记录器测试用
	 */
	private static final Log LOG = LogFactory.getLog(OrderProductGoodsSearchAction.class);
	
	/**
	 * 日期格式化串
	 */
	private static final String DATE_FORMAT="yyyy-MM-dd";
	
	/**
	 * 产品和商品查询业务接口
	 */
	@Autowired
	private IOrderProductQueryService orderProductQueryService;

	/**
	 * 产品业务接口
	 */
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	/**
	 * 行政区域业务接口
	 */
	@Autowired
	private DistrictClientService districtClientService;

	/**
	 * 时间价格业务适配器
	 * */
	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;
	
	/**
	 * 产品规格业务接口
	 */
	@Autowired
	private	CategoryPropClientService categoryPropClientService;
	
	/**
	 * 商品规格业务接口
	 */
	@Autowired
	private	BranchPropClientService branchPropClientService;

	/**
	 * 商品规格信息查询接口
	 * */
	@Autowired
	private OrderProductQueryServiceAdaptor orderProductQueryServiceAdaptor;


	@Autowired
	private ISuppGoodsBranchPropClientService suppGoodsBranchPropClientServiceRemote;
	
	/**
	 * ajax查询入住城市
	 * 
	 * @param search
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/queryDistrictList.do")
	public void queryDistrictList(String search,HttpServletResponse response) throws BusinessException{
		//组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("districtName", search);
		//查地级市,直辖市，特别行政区,区县，县级市
		params.put("districtTypes", "'"+DISTRICT_TYPE.CITY.name()+"','"
														  +DISTRICT_TYPE.PROVINCE_DCG.name()+"','"
														  +DISTRICT_TYPE.PROVINCE_SA.name()+"','"
														  +DISTRICT_TYPE.COUNTY.name()+"'");
		params.put("rownum", 20);//只查询20条，防止拼音模糊查询时SQL报警
		params.put("cancelFlag", "Y");//只查有效的记录
		//调用联想查询接口
		List<BizDistrict> districtList = districtClientService.findDistrictList(params).getReturnContent();
		//组装JSON数据
		JSONArray jsonArray = new JSONArray();
		if(null != districtList && !districtList.isEmpty()){
			for(BizDistrict district:districtList){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", district.getDistrictId());
				//获得完整的行政区域
				jsonObject.put("text", district.getDistrictName());
				jsonArray.add(jsonObject);
			}
		}
		//返回JSON数据
		JSONOutput.writeJSON(response, jsonArray);
	}
	
	/**
	 * ajax查询酒店名称
	 * 
	 * @param search
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/queryProductList.do")
	public void queryProductList(Long districtId,String search,HttpServletResponse response) throws BusinessException{
		//组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("districtId", districtId);
		params.put("productName", search);
		//调用联想查询接口
		//List<ProdProduct> productList = prodProductClientService.findProdProductListByCondition(params);
		//只查20条，防止SQL报警
		List<ProdProduct> productList = prodProductClientService.findProdProductListByProductNameAndPinyin(params);
		//组装JSON数据BIZ_DISTRICT
		JSONArray jsonArray = new JSONArray();
		if(null != productList && !productList.isEmpty()){
			for(ProdProduct product:productList){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", product.getProductId());
				jsonObject.put("text", product.getProductName());
				jsonArray.add(jsonObject);
			}
		}
		//返回JSON数据
		JSONOutput.writeJSON(response, jsonArray);
	}
	
	/**
	 * ajax获取商品时间价格表
	 * 
	 * @param visitTime
	 * @param leaveTime
	 * @param suppGoods
	 * @param quantity
	 * @param model
	 * @return
	 */
	@RequestMapping("/ord/order/productAndGoods/getTimePrice.do")
	public String getTimePrice(String visitTime, String leaveTime,
			long suppGoods, int quantity, ModelMap model) {
		Date beginDate = DateUtil.stringToDate(visitTime, DATE_FORMAT);
		Date endDate = DateUtils.addDays(DateUtil.stringToDate(leaveTime, DATE_FORMAT), -1);// 离开日期不能算入住,所以减一天
		ResultHandleT<List<TimePrice>> resultHandleT = distGoodsTimePriceClientServiceAdaptor.findTimePriceList(Constant.DIST_FRONT_END, suppGoods,beginDate, endDate);
		List<TimePrice> timePriceList = resultHandleT.getReturnContent();
		
		//处理数据
		timePriceList = calcSeq(timePriceList,beginDate,endDate);
		
		//获得第一天的房间价格和早餐用于后面比较
		String price = "0";
		String breakfast = "0";
		
		if(CollectionUtils.isNotEmpty(timePriceList)){
			for(TimePrice timePrice:timePriceList){
				if(timePrice.getPrice() != null){
					//获得第一天的价格
					price = String.valueOf(timePrice.getPrice());
					//获得第一天的早餐值
					breakfast = String.valueOf(timePrice.getBreakfast());
					break;
				}
			}
		}
		
		model.addAttribute("timePriceList",timePriceList );
		model.addAttribute("price", price);
		model.addAttribute("breakfast", breakfast);
		model.addAttribute("suppGoods", suppGoods);
		
		return "/order/orderProductQuery/product_goods_time_price";
	}
	
	/**
	 * 取得房间的面积、楼层、无烟房
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/getAreaFloorSmokelessRoom.do")
	@ResponseBody
	public Object getAreaFloorSmokelessRoom(OrdOrderProductQueryVO ordOrderProductQueryVO) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<getAreaFloorSmokelessRoom>");
		}
		ResultMessage resultMessage = null;
		if(ordOrderProductQueryVO.getProductId() != null && ordOrderProductQueryVO.getSuppGoodsId() != null){
			Map<String, Object> attributes = new HashMap<String, Object>();
			//add by changf on 2018/7/15 先查询商品规格,没有则使用产品规格
			boolean useProductBranchFlag=true;
			ResultHandleT<Map<String, Object>> suppGoodsPropResult = suppGoodsBranchPropClientServiceRemote.findSuppGoodsPropByGoodsId(ordOrderProductQueryVO.getSuppGoodsId());
            if(suppGoodsPropResult!=null &&suppGoodsPropResult.isSuccess()){
				Map<String, Object> suppGoodsPropMap = suppGoodsPropResult.getReturnContent();
				if(suppGoodsPropMap!=null){
					useProductBranchFlag=false;
					attributes.put("area", suppGoodsPropMap.get("area"));
					attributes.put("floor", suppGoodsPropMap.get("floor"));
					List<com.lvmama.dest.comm.po.prod.PropValue > destSmoList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) suppGoodsPropMap.get("smokeless_room");
					if(destSmoList!=null && destSmoList.size()>0)
					attributes.put("smokeless_room", destSmoList.get(0).getName());
					attributes.put("addValue", "");
				}
			}
			//end
			if(useProductBranchFlag) {
				//无烟房
				HashMap<String, Object> paramSmokelessRoom = new HashMap<String, Object>();
				paramSmokelessRoom.put("propCode", "smokeless_room");
				List<BizBranchProp> bizSmokelessRoom = branchPropClientService.findBranchPropList(paramSmokelessRoom).getReturnContent();
				BizBranchProp bizBranchProp = null;
				if (bizSmokelessRoom != null && bizSmokelessRoom.size() > 0) {
					bizBranchProp = (BizBranchProp) bizSmokelessRoom.get(0);
				}
				String[] propCodes = {"area", "floor", "smokeless_room"};
				attributes.put("area", "");
				attributes.put("floor", "");
				attributes.put("smokeless_room", "");
				attributes.put("addValue", "");
				ordOrderProductQueryVO.setPropCodes(propCodes);
				List<OrdOrderGoods> bizBranchProps = orderProductQueryServiceAdaptor.getBizBranchPropByParams(ordOrderProductQueryVO);

				if (bizBranchProps != null && bizBranchProps.size() > 0) {
					for (OrdOrderGoods ordOrderGood : bizBranchProps) {
						if ("area".equals(ordOrderGood.getPropCode())) {
							attributes.put("area", ordOrderGood.getProdValue());
						} else if ("floor".equals(ordOrderGood.getPropCode())) {
							attributes.put("floor", ordOrderGood.getProdValue());
							if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
								attributes.put("addValue", ordOrderGood.getAddValue());
							} else {
								attributes.put("addValue", "");
							}
						} else if ("smokeless_room".equals(ordOrderGood.getPropCode())) {
							if (bizBranchProp != null && bizBranchProp.getDictList() != null && bizBranchProp.getDictList().size() > 0) {
								for (BizDict bizDict : bizBranchProp.getDictList()) {
									if (StringUtil.isNotEmptyString(ordOrderGood.getProdValue()) &&
											bizDict.getDictId().longValue() == Long.valueOf(ordOrderGood.getProdValue())) {
										attributes.put("smokeless_room", bizDict.getDictName());
									}
								}
							}
						}
					}
				}
			}
			resultMessage = new ResultMessage(attributes,ResultMessage.SUCCESS,"");
		}
		return resultMessage ;
	}

	
	/**
	 * 取得是否可加房
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/getAddBedFlag.do")
	@ResponseBody
	public Object getAddBedFlag(OrdOrderProductQueryVO ordOrderProductQueryVO) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<getAddBedFlag>");
		}
		ResultMessage resultMessage = null;
		if(ordOrderProductQueryVO.getProductId() != null && ordOrderProductQueryVO.getSuppGoodsId() != null){
			Map<String, Object> attributes = new HashMap<String, Object>();

			//add by changf on 2018/7/15 先查询商品规格,没有则使用产品规格
			boolean useProductBranchFlag=true;
			ResultHandleT<Map<String, Object>> suppGoodsPropResult = suppGoodsBranchPropClientServiceRemote.findSuppGoodsPropByGoodsId(ordOrderProductQueryVO.getSuppGoodsId());
			if(suppGoodsPropResult!=null &&suppGoodsPropResult.isSuccess()){
				Map<String, Object> suppGoodsPropMap = suppGoodsPropResult.getReturnContent();
				if(suppGoodsPropMap!=null){
					useProductBranchFlag=false;
					attributes.put("area", suppGoodsPropMap.get("area"));
					attributes.put("floor", suppGoodsPropMap.get("floor"));
					List<com.lvmama.dest.comm.po.prod.PropValue > bedTypeList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) suppGoodsPropMap.get("bed_type");
					List<com.lvmama.dest.comm.po.prod.PropValue > addBedFlagSmoList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) suppGoodsPropMap.get("add_bed_flag");
					if(bedTypeList!=null && bedTypeList.size()>0){
						attributes.put("bed_type", bedTypeList.get(0).getName());
						attributes.put("bed_size", bedTypeList.get(0).getAddValue());
					}

					if(addBedFlagSmoList!=null && addBedFlagSmoList.size()>0) {
						attributes.put("add_bed_flag", addBedFlagSmoList.get(0).getName());
						attributes.put("extra_bed_price", addBedFlagSmoList.get(0).getAddValue());
					}
				}
			}
			//end
			if(useProductBranchFlag) {
				//床型与床宽
				HashMap<String, Object> paramBedType = new HashMap<String, Object>();
				paramBedType.put("propCode", "bed_type");
				List<BizBranchProp> bizBedType = branchPropClientService.findBranchPropList(paramBedType).getReturnContent();
				BizBranchProp bizBranchPropBedType = null;
				if (bizBedType != null && bizBedType.size() > 0) {
					bizBranchPropBedType = (BizBranchProp) bizBedType.get(0);
				}
				//是否可追加床
				HashMap<String, Object> paramAddBedFlag = new HashMap<String, Object>();
				paramAddBedFlag.put("propCode", "add_bed_flag");
				List<BizBranchProp> bizAddBedFlag = branchPropClientService.findBranchPropList(paramAddBedFlag).getReturnContent();
				BizBranchProp bizBranchPropAddBedFlag = null;
				if (bizAddBedFlag != null && bizAddBedFlag.size() > 0) {
					bizBranchPropAddBedFlag = (BizBranchProp) bizAddBedFlag.get(0);
				}
				String[] propCodes = {"add_bed_flag", "bed_type"};
				attributes.put("add_bed_flag", "");
				attributes.put("bed_type", "");
				attributes.put("extra_bed_price", "");
				attributes.put("bed_size", "");
				ordOrderProductQueryVO.setPropCodes(propCodes);
				List<OrdOrderGoods> bizBranchProps = orderProductQueryServiceAdaptor.getBizBranchPropByParams(ordOrderProductQueryVO);

				if (bizBranchProps != null && bizBranchProps.size() > 0) {
					for (OrdOrderGoods ordOrderGood : bizBranchProps) {
						if ("add_bed_flag".equals(ordOrderGood.getPropCode())) {
							String[] arrAddValue = null;
							HashMap<String, String> addValueHashMap = new HashMap<String, String>();
							if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
								arrAddValue = ordOrderGood.getAddValue().split(ProdProductProp.PROP_ADD_VALUE_SPLIT);
								if (arrAddValue != null && arrAddValue.length > 0) {
									//获得addvalue 的每一个值12313=2222
									for (int i = 0; i < arrAddValue.length; i++) {
										String addValue = arrAddValue[i];
										if (addValue != null && addValue.contains("=")) {
											String[] array = addValue.split("=");
											if (array != null && array.length == 2)
												addValueHashMap.put(array[0], array[1]);
										}
									}
								}
							}
							if (bizBranchPropAddBedFlag != null && bizBranchPropAddBedFlag.getDictList() != null && bizBranchPropAddBedFlag.getDictList().size() > 0) {
								for (BizDict bizDict : bizBranchPropAddBedFlag.getDictList()) {
									if (bizDict.getDictId().longValue() == Long.valueOf(ordOrderGood.getProdValue())) {
										attributes.put("add_bed_flag", bizDict.getDictName());
										if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
											attributes.put("extra_bed_price", addValueHashMap.get(ordOrderGood.getProdValue()));
										}
									}
								}
							}
						} else if ("bed_type".equals(ordOrderGood.getPropCode())) {
							String[] arrAddValue = null;
							HashMap<String, String> addValueHashMap = new HashMap<String, String>();
							if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
								arrAddValue = ordOrderGood.getAddValue().split(ProdProductProp.PROP_ADD_VALUE_SPLIT);
								if (arrAddValue != null && arrAddValue.length > 0) {
									//获得addvalue 的每一个值12313=2222
									for (int i = 0; i < arrAddValue.length; i++) {
										String addValue = arrAddValue[i];
										if (addValue != null && addValue.contains("=")) {
											String[] array = addValue.split("=");
											if (array != null && array.length == 2)
												addValueHashMap.put(array[0], array[1]);
										}
									}
								}
							}
							if (bizBranchPropBedType != null && bizBranchPropBedType.getDictList() != null && bizBranchPropBedType.getDictList().size() > 0) {
								for (BizDict bizDict : bizBranchPropBedType.getDictList()) {
									if (bizDict.getDictId().longValue() == Long.valueOf(ordOrderGood.getProdValue())) {
										attributes.put("bed_type", bizDict.getDictName());
										if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
											attributes.put("bed_size", addValueHashMap.get(ordOrderGood.getProdValue()));
										}
									}
								}
							}
						}
					}
				}
			}
			resultMessage = new ResultMessage(attributes,ResultMessage.SUCCESS,"");
		}
		return resultMessage ;
	}
	
	/**
	 * 取得宽带价格
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/getInternet.do")
	@ResponseBody
	public Object getInternet(OrdOrderProductQueryVO ordOrderProductQueryVO) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<getInternet>");
		}
		ResultMessage resultMessage = null;
		if(ordOrderProductQueryVO.getProductId() != null && ordOrderProductQueryVO.getSuppGoodsId() != null){
			Map<String, Object> attributes = new HashMap<String, Object>();
			//add by changf on 2018/7/15 先查询商品规格,没有则使用产品规格
			boolean useProductBranchFlag=true;
			ResultHandleT<Map<String, Object>> suppGoodsPropResult = suppGoodsBranchPropClientServiceRemote.findSuppGoodsPropByGoodsId(ordOrderProductQueryVO.getSuppGoodsId());
			if(suppGoodsPropResult!=null &&suppGoodsPropResult.isSuccess()){
				Map<String, Object> suppGoodsPropMap = suppGoodsPropResult.getReturnContent();
				if(suppGoodsPropMap!=null){
					useProductBranchFlag=false;
					List<com.lvmama.dest.comm.po.prod.PropValue > internetList= (java.util.List<com.lvmama.dest.comm.po.prod.PropValue>) suppGoodsPropMap.get("internet");
					if(internetList!=null && internetList.size()>0){
						attributes.put("internet", internetList.get(0).getName());
						attributes.put("addValue", internetList.get(0).getAddValue());
					}

				}
			}
			//end
			if(useProductBranchFlag) {
				//宽带价格
				HashMap<String, Object> paramAddBedFlag = new HashMap<String, Object>();
				paramAddBedFlag.put("propCode", "internet");
				List<BizBranchProp> bizAddBedFlag = branchPropClientService.findBranchPropList(paramAddBedFlag).getReturnContent();
				BizBranchProp bizBranchProp = null;
				if (bizAddBedFlag != null && bizAddBedFlag.size() > 0) {
					bizBranchProp = (BizBranchProp) bizAddBedFlag.get(0);
				}
				String[] propCodes = {"internet"};
				attributes.put("internet", "");
				attributes.put("addValue", "");
				ordOrderProductQueryVO.setPropCodes(propCodes);
				List<OrdOrderGoods> bizBranchProps = orderProductQueryServiceAdaptor.getBizBranchPropByParams(ordOrderProductQueryVO);

				if (bizBranchProps != null && bizBranchProps.size() > 0) {
					for (OrdOrderGoods ordOrderGood : bizBranchProps) {
						if ("internet".equals(ordOrderGood.getPropCode())) {
							String[] arrAddValue = null;
							HashMap<String, String> addValueHashMap = new HashMap<String, String>();
							if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
								arrAddValue = ordOrderGood.getAddValue().split(ProdProductProp.PROP_ADD_VALUE_SPLIT);
								if (arrAddValue != null && arrAddValue.length > 0) {
									//获得addvalue 的每一个值12313=2222
									for (int i = 0; i < arrAddValue.length; i++) {
										String addValue = arrAddValue[i];
										if (addValue != null && addValue.contains("=")) {
											String[] array = addValue.split("=");
											if (array != null && array.length == 2)
												addValueHashMap.put(array[0], array[1]);
										}
									}
								}
							}
							if (bizBranchProp != null && bizBranchProp.getDictList() != null && bizBranchProp.getDictList().size() > 0) {
								for (BizDict bizDict : bizBranchProp.getDictList()) {
									if (bizDict.getDictId().longValue() == Long.valueOf(ordOrderGood.getProdValue())) {
										if (StringUtils.isNotEmpty(ordOrderGood.getAddValue())) {
											attributes.put("addValue", addValueHashMap.get(ordOrderGood.getProdValue()));
										}
									}
								}
							}
						}
					}
				}
			}
			resultMessage = new ResultMessage(attributes,ResultMessage.SUCCESS,"");
		}
		return resultMessage ;
	}
	
	/**
	 * 取得早餐价格
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/productQuery/getBreakfast.do")
	@ResponseBody
	public Object getBreakfast(OrdOrderProductQueryVO ordOrderProductQueryVO) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<getBreakfast>");
		}
		ResultMessage resultMessage = null;
		if(ordOrderProductQueryVO.getProductId() != null){
			//早餐价格
			HashMap<String,Object> paramBreakfast = new HashMap<String,Object>();
			paramBreakfast.put("propCode", "breakfast_price");
			List<BizCategoryProp> bizBreakfast = categoryPropClientService.findAllPropsByParams(paramBreakfast).getReturnContent();
			BizCategoryProp bizCategoryPropBreakfast = null;
			if(bizBreakfast!=null && bizBreakfast.size()>0){
				bizCategoryPropBreakfast = (BizCategoryProp)bizBreakfast.get(0);
			}
			String[] propCodes = {"breakfast_price"} ;
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("breakfast_price", "");
			attributes.put("addValue", "");
			ordOrderProductQueryVO.setPropCodes(propCodes);
			List<OrdOrderGoods> bizCategoryProps = orderProductQueryServiceAdaptor.getBizCategoryPropByParams(ordOrderProductQueryVO);

			if(bizCategoryProps!=null && bizCategoryProps.size()>0){
				for (OrdOrderGoods ordOrderGood : bizCategoryProps) {
					if("breakfast_price".equals(ordOrderGood.getPropCode())){
						String[] arrPropValue = null;
						if(StringUtils.isNotEmpty(ordOrderGood.getPropValue())){
							arrPropValue = ordOrderGood.getPropValue().split(",");
						}
						String[] arrAddValue = null;
						HashMap<String,String> addValueHashMap = new HashMap<String,String>();
						if(StringUtils.isNotEmpty(ordOrderGood.getAddValue())){
							arrAddValue = ordOrderGood.getAddValue().split(ProdProductProp.PROP_ADD_VALUE_SPLIT);
							if(arrAddValue!=null&&arrAddValue.length>0){
								//获得addvalue 的每一个值12313=2222
								for(int i=0;i<arrAddValue.length;i++){
									String addValue = arrAddValue[i];
									if(addValue!=null&&addValue.contains("=")){
										String[] array = addValue.split("=");
										if(array!=null&&array.length==2)
										addValueHashMap.put(array[0], array[1]);
									}
								}
							}
						}
						
						String addValue = "";
						if(null != arrPropValue && arrPropValue.length>0){
							for (int i = 0; i < arrPropValue.length; i++) {
								if(bizCategoryPropBreakfast!=null && bizCategoryPropBreakfast.getBizDictList()!=null && bizCategoryPropBreakfast.getBizDictList().size()>0){
									for (BizDict bizDict : bizCategoryPropBreakfast.getBizDictList()) {
										if(bizDict.getDictId().longValue() == Long.valueOf(arrPropValue[i])){
											addValue = addValue + bizDict.getDictName()+"："+addValueHashMap.get(arrPropValue[i]) +"元一份</br>";
										}
									}
								}
							}
						}
						
						attributes.put("addValue", addValue.substring(0, addValue.length()-5));
					}
				}
			}
			resultMessage = new ResultMessage(attributes,ResultMessage.SUCCESS,"");
		}
		return resultMessage ;
	}
	
	/**
	 * 处理时间价格表
	 * 
	 * @param timePriceList
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private List<TimePrice> calcSeq(List<TimePrice> timePriceList,Date beginDate,Date endDate){
		Map<Date,TimePrice> map = new HashMap<Date,TimePrice>();
		if(CollectionUtils.isNotEmpty(timePriceList)){
			for(TimePrice tp:timePriceList){
				map.put(tp.getSpecDate(), tp);
			}
		}
		
		List<TimePrice> result = new ArrayList<TimePrice>();
		for(Date t=beginDate;!t.after(endDate);){
			if(map.containsKey(t)){
				result.add(map.get(t));
			}else{
				TimePrice tp = new TimePrice();
				tp.setSpecDate(t);
				tp.setNullTimePrice(true);
				result.add(tp);
			}
			t=DateUtils.addDays(t, 1);
		}
		return result;
	}
}
