package com.lvmama.vst.order.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdLineRouteDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.impl.AbstractOrderTravelElectricContactService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.webservice.contract.CancelContractRequest;
import com.lvmama.vst.order.webservice.contract.ContractJSON;
import com.lvmama.vst.order.webservice.contract.ContractTeam;
import com.lvmama.vst.order.webservice.contract.ContractTeamActivity;
import com.lvmama.vst.order.webservice.contract.ContractTeamGuest;
import com.lvmama.vst.order.webservice.contract.ContractTeamRoute;
import com.lvmama.vst.order.webservice.contract.ContractTeamShopping;
import com.lvmama.vst.order.webservice.contract.ContractWebServiceClient;
import com.lvmama.vst.order.webservice.contract.Error;
import com.lvmama.vst.order.webservice.contract.ReturnContent;
import com.lvmama.vst.order.webservice.contract.SubmitContractRequest;

@Service
public class AutoPushContractJob implements Runnable{
	private static final String RETURN_RESULT_SUCCESS = "success";
	
	private static final String RETURN_RESULT_FAILURE = "failure";
	
	private static final String ERROR_CODE_EXCEPTION = "00000";
	
	private static final int LOG_LENGTH = 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(AutoPushContractJob.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Resource(name="teamOutboundTourismContractService")
	private IOrderElectricContactService teamOutboundTourismContractService;
	
	@Autowired
	protected ProdProductClientService prodProductClientService;
	
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			Map<String, Object> params = new HashMap<String, Object>();
			//查找需要推送的合同ID
			List<Map<String, Object>> orderContractIdList = ordTravelContractService.findPushDataByList(params);
			if (orderContractIdList != null) {
				for (Map<String, Object> orderContractIdMap : orderContractIdList) {
//					Long orderId = Long.parseLong(String.valueOf(orderContractIdMap.get("orderId")));
					Long ordContractId = Long.parseLong(String.valueOf(orderContractIdMap.get("ordContractId")));
					try {
						pushData(ordContractId);//进行推送
					} catch (Exception e) {
						logger.error("合同(" + ordContractId + ")同步失败，异常信息：" + e.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * 推送合同数据至金棕榈
	 * @param ordContractId
	 * @throws Exception
	 */
	private void pushData(Long ordContractId) throws Exception {
		//获取合同的信息对象
		OrdTravelContract ordTravelContract = ordTravelContractService
				.findOrdTravelContractById(ordContractId);
		
		String status = ordTravelContract.getStatus();//合同状态
		
		if(ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name().equals(status)) {
			//如果合同是生效状态，则发送取消请求(发送前判断是否有UID)，再发送提交请求
			cancelContract(ordTravelContract);
			submitContract(ordTravelContract);
		} else if(ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name().equals(status)) {
			//如果合同时取消状态，则发送取消请求
			cancelContract(ordTravelContract);
		}
	}
	
	/**
	 * 提交合同数据至金棕榈
	 * @param ordTravelContract
	 * @throws Exception
	 */
	private void submitContract(OrdTravelContract ordTravelContract) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ordContractId", ordTravelContract.getOrdContractId());
		try {
			//下面这段代码用于查询合同数据信息
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordTravelContract.getOrderId());
			List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
			list.add(ordTravelContract);
			order.setOrdTravelContractList(list);
			File directioryFile = ResourceUtil.getResourceFile(AbstractOrderTravelElectricContactService.TRAVEL_ECONTRACT_DIRECTORY);
			
			Map<String,Object> dataMap = teamOutboundTourismContractService.captureContract(ordTravelContract,order, directioryFile);
			
			//线路信息查询
			ProdProductParam param = new ProdProductParam();
			param.setLineRoute(true);
			ResultHandleT<ProdProduct> product=prodProductClientService.findLineProductByProductId(order.getProductId(), param);
			ProdProduct prodProduct = product.getReturnContent();
			
			//组装提交合同的请求对象
			SubmitContractRequest submitRequest = buildSubmitRequest(order, ordTravelContract, prodProduct, dataMap);
			
			//获取const.properties中配置的最大请求次数
			int tryTimes = Integer.parseInt(Constant.getInstance().getContractSyncTimes());
			int alreadyTryTimes = 0;
			ReturnContent returnContent = null;
			
			//当返回失败并且请求次数未达到上限时，循环发送请求
			while (alreadyTryTimes < tryTimes
					&& (returnContent == null || RETURN_RESULT_FAILURE.equals(returnContent.getResult()))) {
				returnContent = doSubmitContract(submitRequest);
				alreadyTryTimes ++;
				if(returnContent != null && RETURN_RESULT_FAILURE.equals(returnContent.getResult())) {
					Thread.sleep(100);
				}
			}
			
			//数据持久化
			
			if(RETURN_RESULT_SUCCESS.equals(returnContent.getResult())) {
				params.put("syncStatus",  OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.SUBMITTED);
				params.put("syncLog","");
				if(returnContent.getContract() != null) {
					params.put("contractUid", returnContent.getContract().getUid());
					params.put("contractNo", returnContent.getContract().getNo());
				}
			} else {
				params.put("syncStatus",  OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.SUBMITTED_FAILED);
				params.put("syncLog", returnContent.getErrorDetails(LOG_LENGTH));
			}
		} catch (Exception e) {
			params.put("syncStatus",  OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.SUBMITTED_FAILED);
			params.put("syncLog", e.getMessage());
			
			logger.error("合同(" + ordTravelContract.getOrdContractId() + ")同步失败，异常信息：", e);
		}
		ordTravelContractService.updatePushDataByContractId(params);
	}
	
	/**
	 * 发送提交合同请求
	 * @param submitRequest
	 * @return
	 */
	private ReturnContent doSubmitContract(SubmitContractRequest submitRequest) {
		ReturnContent returnContent = null;
		try {
			//认证
			ReturnContent authReturn = authentication();
			String token = authReturn.getToken();
			
			//提交合同
			String returnStr = ContractWebServiceClient.submitContract(token, submitRequest);
			returnContent = genReturnContent(returnStr);
		} catch (Exception e) {
			returnContent = new ReturnContent();
			returnContent.setResult(RETURN_RESULT_FAILURE);
			Error error = new Error();
			error.setCode(ERROR_CODE_EXCEPTION);
			error.setInfo(e.getMessage());
		}
		
		return returnContent;
	}
	
	/**
	 * 组装提交合同的请求对象
	 * @param ordTravelContract
	 * @param prodProduct
	 * @param dataMap
	 * @return
	 */
	private SubmitContractRequest buildSubmitRequest(OrdOrder order, OrdTravelContract ordTravelContract, ProdProduct prodProduct, Map<String, Object> dataMap) {
		TravelContractVO travelContractVO = (TravelContractVO)dataMap.get("travelContractVO");
		ProdLineRouteVO route = new ProdLineRouteVO();
		if(CollectionUtils.isNotEmpty(prodProduct.getProdLineRouteList())){
			route=prodProduct.getProdLineRouteList().get(0);
		}
		
        SubmitContractRequest submitRequest = new SubmitContractRequest();
        submitRequest.setNo("TSHCJ00056C" + ordTravelContract.getVersion()); //合同号--服务端生成后返回
		submitRequest.setPrice(travelContractVO.getTraveAmount() == null ? 0
				: Double.parseDouble(travelContractVO.getTraveAmount()));// 是否需要包含保险金额?
		submitRequest.setTravelname(getNotNullStr(travelContractVO.getFirstTravellerPerson().getFullName()));//联系人姓名
        submitRequest.setTravelmobile(StringUtils.isNotBlank(travelContractVO.getFirstTravellerPerson().getMobile()) ? travelContractVO.getFirstTravellerPerson().getMobile() : getNotNullStr(order.getContactPerson().getMobile()));//联系人电话
        submitRequest.setVersion("dlcj2014");//合同名称
        submitRequest.setTransactor("驴妈妈旅游网");//经办人
        submitRequest.setPlatsource("lvmama");//来源
        //电子合同JSON详情对象
        submitRequest.setContractJSON(buildContractJSON(ordTravelContract, travelContractVO));
  
        // 电子合同团队信息
        submitRequest.setContractTeam(buildContractTeam(ordTravelContract, travelContractVO, route));
       
        return submitRequest;
	}
	
	/**
	 * 组装合同相关的一些详细信息
	 * @param travelContractVO
	 * @return
	 */
	private ContractJSON buildContractJSON(OrdTravelContract ordTravelContract, TravelContractVO travelContractVO) {
		ContractJSON contractJSON = new ContractJSON();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("traveler",  getNotNullStr(travelContractVO.getFirstTravellerPerson().getFullName()));//旅游者代表
		jsonObj.put("addr", getNotNullStr(travelContractVO.getPersonAddress()));//旅游者代表住址
		contractJSON.setTraveler(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("teamcode", ordTravelContract.getVersion());//团号
		jsonObj.put("linename", travelContractVO.getProductName());//线路名称
		contractJSON.setLine(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("corp", travelContractVO.getFilialeName());//旅行社
		jsonObj.put("corpCode", travelContractVO.getPermit());//旅行社业务经营许可证编号
		jsonObj.put(
				"scope",
				"入境旅游业务、国内旅游业务、出境旅游业务，国际航线或者香港、澳门、台湾地区航线"
						+ "的航空客运销售代理业务，国内航线除香港、澳门、台湾地区航线外的航空客运销售代理业务，"
						+ "销售旅游用品、工艺品，从事旅游领域内的技术咨询、技术开发，会务服务，展览展示服务。");//经营范围
		contractJSON.setGroupcorp(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("payEachAdult", StringUtils.isBlank(travelContractVO.getPriceAdult()) ? "/" : travelContractVO.getPriceAdult());//成人单人花费
		jsonObj.put("payEachChild", StringUtils.isBlank(travelContractVO.getPriceChild()) ? "/" : travelContractVO.getPriceChild());//儿童单人花费
		jsonObj.put("payTravel", travelContractVO.getTraveAmount());//合计花费
		jsonObj.put("payGuide", StringUtils.isNotBlank(travelContractVO.getGuidePrice()) ? travelContractVO.getGuidePrice() : "/");//导游服务费 
		jsonObj.put("payDeadline", "以实际支付时间为准");//旅游费用交纳期限
		jsonObj.put("payType", "4");//旅游费用交纳方式【1：现金 2：支票 3：信用卡 4：其他】
		jsonObj.put("payOther", "在线支付");//其他交纳方式
		contractJSON.setPay(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("agree", travelContractVO.isHasInsurance() ? "1" : "2");//旅游者【1：委托出境社购买 2：自行购买 3：放弃购买】
		jsonObj.put("product", getNotNullStr(travelContractVO.getInsuranceCompanyAndProductName()));//保险产品名称
		contractJSON.setInsurance(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("personLimit", travelContractVO.getMinPersonCountOfGroup());//最低成团人数
		jsonObj.put("transAgree", "1");//旅行者__旅行社【0：不同意 1：同意】
		jsonObj.put("transAgency", "指定");//旅行社委托__出境社履行合同【出境社名称】
		jsonObj.put("delayAgree", "1");//旅行者__延期出团【0：不同意 1：同意】
		jsonObj.put("changeLineAgree", "1");//旅行者__改签其他线路出团【0：不同意 1：同意】
		jsonObj.put("terminateAgree", "1");//旅行者__解除合同【0：不同意 1：同意】
		jsonObj.put("mergeAgree", "1");//旅行者__采用拼团方式【0：不同意 1：同意】 
		jsonObj.put("mergeAgency", "指定");//拼团拼至__旅行社成团
		jsonObj.put("teminateDealType", "2");//协商或者调解不成的，按第几种方式处理【1：提交仲裁委员会仲裁；2：依法向人民法院起诉】 
		jsonObj.put("committee", "上海市嘉定区");//仲裁委员会
		contractJSON.setGroup(jsonObj.toString());
		
		jsonObj = new JSONObject();
		jsonObj.put("supplementaryClause", getNotNullStr(travelContractVO.getSupplementaryTerms()));//其他约定事项
		jsonObj.put("copys1", "两");//合同一式__份
		jsonObj.put("copys2", "壹");//双方各持__份
		jsonObj.put("agencyComplaintsMobile", getNotNullStr(travelContractVO.getJianduTel()));//出境社监督、投诉电话
		jsonObj.put("lawState", getNotNullStr(travelContractVO.getProvince()));//旅游质监执法机构 省
		jsonObj.put("lawCity", getNotNullStr(travelContractVO.getCity()));//旅游质监执法机构 市
		jsonObj.put("lawComplaintsMobile", getNotNullStr(travelContractVO.getLvTSTelephone()));//旅游质监执法机构 投诉电话
		jsonObj.put("lawEmail", getNotNullStr(travelContractVO.getLvEmail2()));//旅游质监执法机构 电子邮箱
		jsonObj.put("lawAddress", getNotNullStr(travelContractVO.getLvAddress()));//旅游质监执法机构 地址
		jsonObj.put("lawZip", getNotNullStr(travelContractVO.getLvpostcode()));//旅游质监执法机构 邮编
		contractJSON.setOther(jsonObj.toString());
		
		return contractJSON;
	}
	
	/**
	 * 组装ContractTeam对象
	 * @param travelContractVO
	 * @param route
	 * @return
	 */
	private ContractTeam buildContractTeam(OrdTravelContract ordTravelContract, TravelContractVO travelContractVO, ProdLineRouteVO route) {
		ContractTeam contractTeam = new ContractTeam();
	        
		contractTeam.setBgndate(DateUtil.toSimpleDate(travelContractVO.getVistDate()));//出发时间
		contractTeam.setDays(travelContractVO.getRouteDays() == null ? 0 : Integer.parseInt(travelContractVO.getRouteDays()));//天数
		contractTeam.setNights(travelContractVO.getRouteNights() == null ? 0 : Integer.parseInt(travelContractVO.getRouteNights()));//过夜数
//		contractTeam.setNights(1);//过夜数
		contractTeam.setEnddate(DateUtil.toSimpleDate(travelContractVO.getOverDate()));//结束时间
		contractTeam.setLinename(travelContractVO.getProductName());//线路名称
		contractTeam.setLocaltraffic("");//小交通
		contractTeam.setLongtraffic("");//大交通
		contractTeam.setOptype("");//业务类型
		contractTeam.setQty(travelContractVO.getOrdTravellerList() == null ? 0 : travelContractVO.getOrdTravellerList().size());//旅游人数
		contractTeam.setStartcity("");//出发城市
		contractTeam.setTeamcode(ordTravelContract.getVersion());//团号
		contractTeam.setTripmemo("");//行程内容
		        
        contractTeam.setGuests(buildContractTeamGuest(travelContractVO));
        contractTeam.setRoutes(buildContractTeamRoute(travelContractVO, route));
        contractTeam.setActivities(buildContractTeamActivity(travelContractVO));
        contractTeam.setShoppings(buildContractTeamShopping(travelContractVO));
        
        return contractTeam;
	}
	
	/**
	 * 组装旅游者信息
	 * @param travelContractVO
	 * @return
	 */
	private List<ContractTeamGuest> buildContractTeamGuest(TravelContractVO travelContractVO) {
		List<ContractTeamGuest> guests = new ArrayList<ContractTeamGuest>();
        ContractTeamGuest contractTeamGuest = null;
        List<OrdPerson> ordTravellerList = travelContractVO.getOrdTravellerList();
        if(ordTravellerList != null) {
        	for (int i = 0; i < ordTravellerList.size(); i++) {
        		OrdPerson person = ordTravellerList.get(i);
        		contractTeamGuest = new ContractTeamGuest();
        		contractTeamGuest.setBirthday(person.getBirthday()); //生日，没有该值
        		contractTeamGuest.setIdcode(StringUtils.isNotBlank(person.getIdNo()) ? person.getIdNo() : "1");//证件号
        		contractTeamGuest.setFamilyname(""); //拼音姓，没有该值
        		contractTeamGuest.setGivenname("");//拼音名，没有该值
        		contractTeamGuest.setFolk("");//民族，没有该值
        		//contractTeamGuest.setIdenddate();//证照失效日期(护照或通行证用)，没有该值
        		contractTeamGuest.setIdfrom("");//证照签发地(护照或通行证用)，没有该值
        		//		          contractTeamGuest.setIdstartdate();//证照起效日期(护照或通行证用)，没有该值
        		contractTeamGuest.setIdtype(getTransIdType(person.getIdType()));//证件类型，为空时传其它
        		contractTeamGuest.setMobile(getNotNullStr(person.getMobile()));//手机
        		contractTeamGuest.setName(person.getFullName());
        		contractTeamGuest.setNation("");//国籍
        		contractTeamGuest.setNo(i+1);
        		contractTeamGuest.setSex(StringUtils.isBlank(person.getGenderName()) ? "男" : person.getGenderName());
        		guests.add(contractTeamGuest);
        	}
        }
        
        return guests;
	}
	
	/**
	 * 组装行程数据
	 * @param travelContractVO
	 * @param route
	 * @return
	 */
	private List<ContractTeamRoute> buildContractTeamRoute(TravelContractVO travelContractVO, ProdLineRouteVO route) {
		List<ContractTeamRoute> contractTeamRoutes = new ArrayList<ContractTeamRoute>();
        ContractTeamRoute contractTeamRoute = null;
	  
        if (route != null && route.getProdLineRouteDetailList() != null) {
        	for (ProdLineRouteDetail routeDetail : route.getProdLineRouteDetailList()) {
        		contractTeamRoute = new ContractTeamRoute();
        		contractTeamRoute.setArrivecity("");//前往地
        		contractTeamRoute.setArrivenation("");//前往省市
        		contractTeamRoute.setArrivestate("");//前往国家
        		contractTeamRoute.setBoardtime("");//出发时间
        		contractTeamRoute.setCarriername("");//承运人
        		contractTeamRoute.setDay(routeDetail.getnDay());//第几天
        		contractTeamRoute.setDepartcity("");//出发地
				contractTeamRoute.setDinner(getNotNullStr(routeDetail.getBreakfastDesc())
															+ " "
															+ getNotNullStr(routeDetail.getLunchDesc() + " "
															+ getNotNullStr(routeDetail.getDinnerDesc())));//用餐说明
        		contractTeamRoute.setHotel(getNotNullStr(routeDetail.getStayDesc()));//住宿说明
			   	contractTeamRoute.setLineno("");//班次号/航班号
			   	contractTeamRoute.setMemo(getNotNullStr(routeDetail.getContent()));
			   	contractTeamRoute.setOfftime("");//到达时间
			   	contractTeamRoute.setPort("");//过境口岸，出境/赴台用
			   	contractTeamRoute.setStop(1);//当天行程第几站 可以只传1，也就是1天1个站点
			   	contractTeamRoute.setSupplier("");//地接社
			   	contractTeamRoute.setTitle(getNotNullStr(routeDetail.getTitle()));
			   	contractTeamRoute.setTraffic(getNotNullStr(routeDetail.getTrafficOther()));
			   	contractTeamRoute.setTransit("0");//中转标志  0-不过境 1-过境 没有该值
			   	contractTeamRoute.setTrip("");//游览行程
			   	contractTeamRoutes.add(contractTeamRoute);
        	}
        }
        
        return contractTeamRoutes;
	}
	
	/**
	 * 组装自愿参加另行付费旅游项目补充协议数据
	 * @param travelContractVO
	 * @return
	 */
	private List<ContractTeamActivity> buildContractTeamActivity(TravelContractVO travelContractVO) {
		List<ContractTeamActivity> activityList = new ArrayList<ContractTeamActivity>();
        ContractTeamActivity activity = null;
        if(travelContractVO.getRecommendDetailList() != null) {
        	for(ProdContractDetail contractDetail : travelContractVO.getRecommendDetailList()) {
        		activity = new ContractTeamActivity();
        		activityList.add(activity);
        		activity.setDate(contractDetail.getVistStartTime() == null ? "" : DateUtil.formatDate(contractDetail.getVistStartTime(), "yyyy-MM-dd HH:mm:ss"));//具体时间 必填格式(yyyy-MM-dd空格hh:mm:ss)
        		activity.setPlace(getNotNullStr(contractDetail.getAddress()));//地点
        		activity.setItem(getNotNullStr(contractDetail.getDetailName()));//项目
        		activity.setFee(getNotNullStr(contractDetail.getDetailValue()));//费用
        		activity.setStaytime(contractDetail.getStay() == null ? "0" : String.valueOf(contractDetail.getStay()));//最长停留时间(分钟)
        		activity.setMemo(StringUtils.isNotBlank(contractDetail.getOther()) ? contractDetail.getOther() : "无");//其他说明
        		activity.setSignature(getNotNullStr(travelContractVO.getFirstTravellerPerson().getFullName()));//游客签名
        	}
        }
        
        return activityList;
	}
	
	/**
	 * 组装自愿购物活动补充协议数据
	 * @param travelContractVO
	 * @return
	 */
	private List<ContractTeamShopping> buildContractTeamShopping(TravelContractVO travelContractVO) {
		List<ContractTeamShopping> shoppingList = new ArrayList<ContractTeamShopping>();
        ContractTeamShopping shopping = null;
        if(travelContractVO.getShopingDetailList() != null) {
        	for(ProdContractDetail contractDetail : travelContractVO.getShopingDetailList()) {
        		shopping = new ContractTeamShopping();
        		shoppingList.add(shopping);
        		shopping.setDate(contractDetail.getVistStartTime() == null ? "" : DateUtil.formatDate(contractDetail.getVistStartTime(), "yyyy-MM-dd HH:mm:ss"));//具体时间 必填格式(yyyy-MM-dd空格hh:mm:ss)
        		shopping.setPlace(getNotNullStr(contractDetail.getAddress()));//地点
        		shopping.setShoppingplace(getNotNullStr(contractDetail.getDetailName()));//购物场所名称
        		shopping.setGood(getNotNullStr(contractDetail.getDetailValue()));//主要商品信息
        		shopping.setStaytime(contractDetail.getStay() == null ? "0" : String.valueOf(contractDetail.getStay()));//最长停留时间(分钟)
        		shopping.setMemo(StringUtils.isNotBlank(contractDetail.getOther()) ? contractDetail.getOther() : "无");//其他说明
        		shopping.setSignature(getNotNullStr(travelContractVO.getFirstTravellerPerson().getFullName()));//游客签名
        	}
        }
        
        return shoppingList;
	}
	
	/**
	 * 调用金棕榈取消合同接口
	 * @param ordTravelContract 合同信息对象
	 * @throws Exception 
	 */
	private void cancelContract(OrdTravelContract ordTravelContract) throws Exception {
		String contractUid = ordTravelContract.getContractUid();
		//如果合同ID为空，则不调用，流程直接结束
		if(StringUtils.isBlank(contractUid)) {
			return;
		}
		String contractNo = ordTravelContract.getContractNo();
		CancelContractRequest cancelContractRequest = new  CancelContractRequest();
		cancelContractRequest.setId(contractUid);
		cancelContractRequest.setNo(contractNo);
		
		//获取配置文件中配置的最大请求次数
		int tryTimes = Integer.parseInt(Constant.getInstance().getContractSyncTimes());
		int alreadyTryTimes = 0;
		ReturnContent returnContent = null;
		
		//当实际请求次数小于最大请求次数，并且返回结果为失败时，重复调用
		while (alreadyTryTimes < tryTimes
				&& (returnContent == null || RETURN_RESULT_FAILURE.equals(returnContent.getResult()))) {
			returnContent = doCancelContract(cancelContractRequest);
			alreadyTryTimes ++;
			if(returnContent != null && RETURN_RESULT_FAILURE.equals(returnContent.getResult())) {
				Thread.sleep(100);
			}
		}
		
		//将返回的结果进行持久化
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ordContractId", ordTravelContract.getOrdContractId());
		if(RETURN_RESULT_SUCCESS.equals(returnContent.getResult())) {
			params.put("contractUid", "");
			params.put("contractNo", "");
			params.put("syncLog", "");
			params.put("syncStatus",  OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.CANCELED);
		} else {
			params.put("syncStatus",  OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.CANCELED_FAILED);
			params.put("syncLog", returnContent.getErrorDetails(LOG_LENGTH));
		}
		ordTravelContractService.updatePushDataByContractId(params);
	}
	
	/**
	 * 调用金棕榈取消合同接口
	 * @param cancelContractRequest 取消合同请求对象
	 * @return
	 */
	private ReturnContent doCancelContract(CancelContractRequest cancelContractRequest) {
		ReturnContent returnContent = null;
		try {
			//调用认证接口，获得token
			ReturnContent authReturn = authentication();
			String token = authReturn.getToken();
			//调用取消合同接口
			String returnStr = ContractWebServiceClient.cancelContract(token, cancelContractRequest);
			returnContent = genReturnContent(returnStr);
		} catch (Exception e) {
			//捕获异常，并构造取消合同失败的结果对象
			returnContent = new ReturnContent();
			returnContent.setResult(RETURN_RESULT_FAILURE);
			Error error = new Error();
			error.setCode(ERROR_CODE_EXCEPTION);
			error.setInfo(e.getMessage());
		}
		
		return returnContent;
	}

	/**
	 * 使用配置文件中的账号密码调用金棕榈的认证接口
	 * @return 金棕榈认证结果对象
	 * @throws Exception
	 */
	private ReturnContent authentication() throws Exception {
		String code =  Constant.getInstance().getContractSyncUsername();
		String password =  Constant.getInstance().getContractSyncPassword();
		String returnStr = ContractWebServiceClient.authentication(code, password);
		return genReturnContent(returnStr);
	}
	
	/**
	 * 将接口返回的json字符串转换为ReturnContent对象
	 * @param returnStr
	 * @return
	 */
	private ReturnContent genReturnContent(String returnStr) {
		JSONObject jsonObj = JSONObject.fromObject(returnStr);
    	ReturnContent returnContent = (ReturnContent)JSONObject.toBean(jsonObj, ReturnContent.class);
    	return returnContent;
	}
	
	/**
	 * 将null转换为空字符串
	 * @param str
	 * @return
	 */
	private String getNotNullStr(String str) {
		if(str == null) {
			return "";
		}
		
		return str;
	}
	
	/**
	 * 将VST的证件类型转换为金棕榈需要的代码
	 * @param idType
	 * @return
	 */
	private int getTransIdType(String idType) {
		//1-	身份证，2-士官证，3-港澳通行证，4-护照，5-赴台证，6-回乡证，7-台胞证，8-其他
		if(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name().equals(idType)) {
			return 1;
		} else if(OrderEnum.ORDER_PERSON_ID_TYPE.JUNGUAN.name().equals(idType)) {
			return 2;
		} else if(OrderEnum.ORDER_PERSON_ID_TYPE.GANGAO.name().equals(idType)) {
			return 3;
		} else if(OrderEnum.ORDER_PERSON_ID_TYPE.HUZHAO.name().equals(idType)) {
			return 4;
		} else if(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAO.name().equals(idType)) {
			return 7;
		} else if(OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name().equals(idType)) {
			return 6;
		} 
		
		return 8;
	}
}
