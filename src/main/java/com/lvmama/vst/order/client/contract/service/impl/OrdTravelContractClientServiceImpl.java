package com.lvmama.vst.order.client.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.fs.vo.ComFile;
import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.vo.TravelContractDataVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.client.service.o2o.contract.IOrdTravelContractClientService;
import com.lvmama.vst.order.contract.service.IOrderContractSnapshotService;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.impl.AbstractOrderTravelElectricContactService;
import com.lvmama.vst.order.contract.vo.CruiseTourismContractDataVO;
import com.lvmama.vst.order.contract.vo.OutboundTourContractDataVO;
import com.lvmama.vst.order.contract.vo.TeamWithInTerritoryContractDataVO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdTravelContractService;

@Component("OrdTravelContractRemote")
public class OrdTravelContractClientServiceImpl implements IOrdTravelContractClientService {

	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private IOrdItemContractRelationService iOrdItemContractRelationService;
	
	@Resource(name="orderTravelElectricContactService")
	private IOrderElectricContactService orderTravelElectricContactService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	private String TRAVEL_ECONTRACT_DIRECTORY = AbstractOrderTravelElectricContactService.TRAVEL_ECONTRACT_DIRECTORY;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	@Resource(name="teamOutboundTourismContractService")
	private IOrderElectricContactService teamOutboundTourismContractService;
	
	@Autowired
	private IOrderContractSnapshotService orderContractSnapshotService;
	
	@Autowired
	protected FSClient fsClient;
	
	@Resource(name="teamDonggangZhejiangContractService")
	private IOrderElectricContactService teamDonggangZhejiangContractService;
	
	@Resource(name="teamWithInTerritoryContractService")
	private IOrderElectricContactService teamWithInTerritoryContractService;
	
	@Resource(name="advanceProductAgreementContractService")
	private IOrderElectricContactService advanceProductAgreementContractService;
	
	@Resource(name="commissionedServiceAgreementService")
	private IOrderElectricContactService commissionedServiceAgreementService;
	
	@Resource(name="destCommissionedServiceAgreementService")
	private IOrderElectricContactService destCommissionedServiceAgreementService;
	
	@Resource(name="cruiseTourismContractService")
	private IOrderElectricContactService cruiseTourismContractService;
	
	@Resource(name="taiwanTravelContractService")
	private IOrderElectricContactService taiwanTravelContractService;
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public List<OrdTravelContract> findOrdTravelContractList(Map<String, Object> params) {
		return  ordTravelContractService.findOrdTravelContractList(params);
	}

	@Override
	public List<OrdItemContractRelation> findOrdItemContractRelationList(HashMap<String, Object> params) {
		return  iOrdItemContractRelationService.findOrdItemContractRelationList(params);
	}

	@Override
	public ComFileMap getComFileMapByFileName(String fileName) {
		return ordTravelContractService.getComFileMapByFileName(fileName);
	}

	@Override
	public OrdTravelContract findOrdTravelContractById(Long id) {
		return ordTravelContractService.findOrdTravelContractById(id);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdTravelContract ordTravelContract) {
		return ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract);
	}

	@Override
	public int updateContractStatusByOrderId(Map<String, Object> params) {
		return ordTravelContractService.updateContractStatusByOrderId(params);
	}

	@Override
	public void insertOrderLog(Long orderId, Long contractId,String operatorName, String content, String memo) {
		orderTravelElectricContactService.insertOrderLog(orderId, contractId, operatorName, content, memo);
	}

	@Override
	public ResultHandle sendContractEmail(OrdOrder order, Long contractId,String opterator) {
		ResultHandle resultHandle = new ResultHandle();
		resultHandle = orderTravelElectricContactService.sendContractEmail(order, contractId, opterator);
		return resultHandle;
	}
	
	@Resource(name="beijingDayTourContractService")
	private IOrderElectricContactService beijingDayTourContractService;

	@Override
	public TravelContractDataVO showUpdateTravelContract(Long orderId, Long ordContractId) {
		TravelContractDataVO travelContractDataVO = new TravelContractDataVO();
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdTravelContract ordTravelContract=ordTravelContractService.findOrdTravelContractById(ordContractId);
		List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
		list.add(ordTravelContract);
		order.setOrdTravelContractList(list);
		File directioryFile = ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
		if (ordTravelContract != null) {
			 if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=teamOutboundTourismContractService.captureContract(ordTravelContract,order, directioryFile);
				TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");
				/*合同快照数据替代原有数据部分*/
				//根据订单合同ID,从合同快照数据表中获取最新的记录
				Map<String, Object> params  = new HashMap<String, Object>();
				params.put("ordContractId", ordContractId);
				OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
				if(OrdContractSnapshotData != null){
					Long fileId = OrdContractSnapshotData.getJsonFileId();
					ComFile comFile = fsClient.downloadFile(fileId);
					String fileStr = null;
					try {
						fileStr = new String(comFile.getFileData(),"UTF-8");
					} catch (UnsupportedEncodingException e) {
						log.error("解析json文件出错："+e.getMessage());
					}
					OutboundTourContractDataVO outboundTourContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,OutboundTourContractDataVO.class);
					if(outboundTourContractDataVO != null){
						if(StringUtil.isNotEmptyString(outboundTourContractDataVO.getSupplementaryTerms())){
							travelContractVO.setSupplementaryTerms(outboundTourContractDataVO.getSupplementaryTerms());
						}
						if(outboundTourContractDataVO.getRecommendDetailList() != null){
							travelContractVO.setRecommendDetailList(outboundTourContractDataVO.getRecommendDetailList());
						}
						if(outboundTourContractDataVO.getShopingDetailList() != null){
							travelContractVO.setShopingDetailList(outboundTourContractDataVO.getShopingDetailList());
						}
					}
					
				}

				setResult(travelContractDataVO, order, rootMap,travelContractVO);
			} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String, Object> rootMap = teamDonggangZhejiangContractService.captureContract(ordTravelContract, order,directioryFile);
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
		   } else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=teamWithInTerritoryContractService.captureContract(ordTravelContract,order, directioryFile);
				TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");
				/*合同快照数据替代原有数据部分*/
				//根据订单合同ID,从合同快照数据表中获取最新的记录
				Map<String, Object> params  = new HashMap<String, Object>();
				params.put("ordContractId", ordContractId);
				OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
				if(OrdContractSnapshotData != null){
					Long fileId = OrdContractSnapshotData.getJsonFileId();
					ComFile comFile = fsClient.downloadFile(fileId);
					String fileStr = null;
					try {
						fileStr = new String(comFile.getFileData(),"UTF-8");
					} catch (UnsupportedEncodingException e) {
						log.error("解析json文件出错："+e.getMessage());
					}
					TeamWithInTerritoryContractDataVO teamWithInTerritoryContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,TeamWithInTerritoryContractDataVO.class);
					if(teamWithInTerritoryContractDataVO != null){
						if(StringUtil.isNotEmptyString(teamWithInTerritoryContractDataVO.getSupplementaryTerms())){
							travelContractVO.setSupplementaryTerms(teamWithInTerritoryContractDataVO.getSupplementaryTerms());
						}
						if(teamWithInTerritoryContractDataVO.getRecommendDetailList() != null){
							travelContractVO.setRecommendDetailList(teamWithInTerritoryContractDataVO.getRecommendDetailList());
						}
						if(teamWithInTerritoryContractDataVO.getShopingDetailList() != null){
							travelContractVO.setShopingDetailList(teamWithInTerritoryContractDataVO.getShopingDetailList());
						}
					}
					
				}
				
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
			}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=advanceProductAgreementContractService.captureContract(ordTravelContract,order, directioryFile);
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));

			}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=commissionedServiceAgreementService.captureContract(ordTravelContract,order, directioryFile);
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
			}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=destCommissionedServiceAgreementService.captureContract(ordTravelContract,order, directioryFile);
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
			}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String,Object> rootMap=beijingDayTourContractService.captureContract(ordTravelContract,order, directioryFile);
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
			}else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
				Map<String, Object> rootMap = cruiseTourismContractService.captureContract(ordTravelContract, order, directioryFile);
				TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");
				/*合同快照数据替代原有数据部分*/
				//根据订单合同ID,从合同快照数据表中获取最新的记录
				Map<String, Object> params  = new HashMap<String, Object>();
				params.put("ordContractId", ordContractId);
				OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
				if(OrdContractSnapshotData != null){
					Long fileId = OrdContractSnapshotData.getJsonFileId();
					ComFile comFile = fsClient.downloadFile(fileId);
					String fileStr = null;
					try {
						fileStr = new String(comFile.getFileData(),"UTF-8");
					} catch (UnsupportedEncodingException e) {
						log.error("解析json文件出错："+e.getMessage());
					}
					CruiseTourismContractDataVO cruiseTourismContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,CruiseTourismContractDataVO.class);
					if(cruiseTourismContractDataVO != null){
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getProductName())){
							travelContractVO.setProductName(cruiseTourismContractDataVO.getProductName());
						}
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getMinPersonCountOfGroup())){
							travelContractVO.setMinPersonCountOfGroup(cruiseTourismContractDataVO.getMinPersonCountOfGroup());
						}
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getLineShipDesc())){
							travelContractVO.setLineShipDesc(cruiseTourismContractDataVO.getLineShipDesc());
						}
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getDeparturePlace())){
							travelContractVO.setDeparturePlace(cruiseTourismContractDataVO.getDeparturePlace());
						}
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getReturnPlace())){
							travelContractVO.setReturnPlace(cruiseTourismContractDataVO.getReturnPlace());
						}
						if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getSupplementaryTerms())){
							travelContractVO.setSupplementaryTerms(cruiseTourismContractDataVO.getSupplementaryTerms());
						}
						if(cruiseTourismContractDataVO.getRecommendDetailList() != null){
							travelContractVO.setRecommendDetailList(cruiseTourismContractDataVO.getRecommendDetailList());
						}
						if(cruiseTourismContractDataVO.getShopingDetailList() != null){
							travelContractVO.setShopingDetailList(cruiseTourismContractDataVO.getShopingDetailList());
						}
					}
					
				}
				setResult(travelContractDataVO, order, rootMap,(TravelContractVO)rootMap.get("travelContractVO"));
			}

		}
		return travelContractDataVO;
	}

	private void setResult(TravelContractDataVO travelContractDataVO,OrdOrder order, Map<String, Object> rootMap,TravelContractVO travelContractVO) {
		travelContractDataVO.setTravelContractVO(travelContractVO);
		travelContractDataVO.setOrder(order);
		Map<String,List<OrderMonitorRst>> chidOrderMap = (Map<String,List<OrderMonitorRst>>)rootMap.get("chidOrderMap");
		travelContractDataVO.setChidOrderMap(chidOrderMap);
	}

	@Override
	public Object updateTravelContract(TravelContractVO travelContractVO,Long orderId, Long ordContractId,String operatorName) {
		if (log.isDebugEnabled()) {
			log.debug("start method<updateTravelContract>");
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdTravelContract ordTravelContract=ordTravelContractService.findOrdTravelContractById(ordContractId);
		if (order.getOrderStatus().equals(OrderEnum.ORDER_STATUS.CANCEL.name())) {
			//name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
			return new ResultMessage(ResultMessage.ERROR,"订单已经取消不可修改合同");
		}
		ResultHandle resultHandle=new ResultHandle() ;
		if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle = this.teamOutboundTourismContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			/*合同快照部分*/
			//1.获取合同填充的数据和该合同对应的行程单的数据
			OutboundTourContractDataVO outboundTourContractDataVO = new OutboundTourContractDataVO();
			outboundTourContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
			outboundTourContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
			outboundTourContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());
			//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
			uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,outboundTourContractDataVO,resultHandle);
		}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle = this.teamWithInTerritoryContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			/*合同快照部分*/
			//1.获取合同填充的数据和该合同对应的行程单的数据
			TeamWithInTerritoryContractDataVO teamWithInTerritoryContractDataVO = new TeamWithInTerritoryContractDataVO();
			teamWithInTerritoryContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
			teamWithInTerritoryContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
			teamWithInTerritoryContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());
			//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
			uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,teamWithInTerritoryContractDataVO,resultHandle);
		}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle = this.advanceProductAgreementContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
		}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle=commissionedServiceAgreementService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
		}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle=destCommissionedServiceAgreementService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
		}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
			resultHandle=beijingDayTourContractService.saveTravelContact(ordTravelContract, operatorName);
		}else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
			resultHandle = taiwanTravelContractService.saveTravelContact(ordTravelContract, operatorName);
		}else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
			resultHandle = this.teamDonggangZhejiangContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
		}else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
			resultHandle = this.cruiseTourismContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			/*合同快照部分*/
			//1.获取合同填充的数据和该合同对应的行程单的数据
			CruiseTourismContractDataVO cruiseTourismContractDataVO = new CruiseTourismContractDataVO();
			cruiseTourismContractDataVO.setProductName(travelContractVO.getProductName());
			cruiseTourismContractDataVO.setLineShipDesc(travelContractVO.getLineShipDesc());
			cruiseTourismContractDataVO.setMinPersonCountOfGroup(travelContractVO.getMinPersonCountOfGroup());
			cruiseTourismContractDataVO.setDeparturePlace(travelContractVO.getDeparturePlace());
			cruiseTourismContractDataVO.setReturnPlace(travelContractVO.getReturnPlace());
			cruiseTourismContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
			//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
			uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,cruiseTourismContractDataVO,resultHandle);
		}
		if (resultHandle.isSuccess()) {
			resultHandle=teamOutboundTourismContractService.sendOrderEcontractEmail(order,operatorName);
		}
		if (resultHandle.isFail()) {
			log.info("orderId is"+order.getOrderId()+"error info is:"+resultHandle.getMsg());
			String message="更新失败";		
			return new ResultMessage(ResultMessage.ERROR,message);
		}
		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}
	
	private <T> ResultHandle uploadFileAndCreateContractSnapshotDate(Long orderId,OrdTravelContract ordTravelContract, String operatorName,T dataVO,ResultHandle resultHandle) {
		Long jsonfileId = null;
		try {
			String str = com.alibaba.fastjson.JSONObject.toJSONString(dataVO);
			byte[] _fileBytes = str.getBytes("UTF-8");
			ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(_fileBytes);
			String jsonfileName = orderId + ".json";
			jsonfileId = fsClient.uploadFile(jsonfileName, bytesInputStream, SERVER_TYPE);
			if(null == jsonfileId){
				log.error("上传.json格式文件失败！");
				resultHandle.setMsg("上传.json格式文件失败！");
			}
			bytesInputStream.close();
		} catch (IOException e) {
			log.error(e.getMessage());
			resultHandle.setMsg("上传.json格式文件失败！");
		}
		//3.将保存的文件ID插入到ORD_CONTRACT_SNAPSHOT_DATA，合同快照数据表
		OrdContractSnapshotData ordContractSnapshotData = new OrdContractSnapshotData();
		ordContractSnapshotData.setOrdContractId(ordTravelContract.getOrdContractId());
		ordContractSnapshotData.setJsonFileId(jsonfileId);
		ordContractSnapshotData.setCreateTime(new Date());
		int returnValue = orderContractSnapshotService.saveContractSnapshot(ordContractSnapshotData,operatorName);
		return null;
	}

}
