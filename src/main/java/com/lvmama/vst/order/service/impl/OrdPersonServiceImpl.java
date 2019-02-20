package com.lvmama.vst.order.service.impl;

import java.util.*;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.comm.vst.vo.CallBackRequestDto;
import com.lvmama.comm.vst.vo.CallBackResponseDto;
import com.lvmama.comm.vst.vo.VstTravellerCallBackRequest;
import com.lvmama.comm.vst.vo.VstTravellerCallBackResponseDto;
import com.lvmama.comm.vst.vo.VstTravellerInfo;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.pet.adapter.IReceiverUserServiceAdapter;

@Service
public class OrdPersonServiceImpl implements IOrdPersonService {

	private static final Log LOG = LogFactory.getLog(OrdPersonServiceImpl.class);
	private static final String LogViewUtil = null;
	private static final String MSG_PARAMETER_NULL ="参数不匹配，请检查参数";
	private static final int UPDATE_TRAVELLER_COUNT=20;
	private static final String MSG_UPDATE_TRAVELLER_COUNT_UP="游玩人信息修改次数今天已达上限";
	private static final String MSG_SUCCESS="更新ok";
	private static final String MSG_IS_LOCK="游玩人已锁定，无法更改，请联系客服！";
	private static final String SYMBOL_COMMA="、";
	private static final char OLD='旧';
	private static final char NEW='新';
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private OrdAddressDao ordAddressDao;
	
	@Autowired
	private OrdPersonDao ordPersonDao;
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	@Override
	public int addOrdPerson(OrdPerson ordPerson) {
		// TODO Auto-generated method stub
		return ordPersonDao.insert(ordPerson);
	}
	@Override
	public OrdPerson findOrdPersonById(Long id) {
		// TODO Auto-generated method stub
		return ordPersonDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdPerson> findOrdPersonList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordPersonDao.findOrdPersonList(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdPerson ordPerson) {
		// TODO Auto-generated method stub
		return ordPersonDao.updateByPrimaryKeySelective(ordPerson);
	}

	
	public List<OrdPerson> getOrderPersonListWithAddress(Long orderId,String personType) {
		// TODO Auto-generated method stub
		Map<String,Object> params=new HashMap<String,Object>();
		params.put("objectId", orderId);
		params.put("personType", personType);
		return ordPersonDao.findOrdPersonListWithAddress(params);
	}
	
	
	public OrdPerson selectInvoicePersonByInvoiceId(Long invoiceId) {
		Map<String,Object> params=new HashMap<String, Object>();
		params.put("objectType", "INVOICE");
		params.put("objectId", String.valueOf(invoiceId));
		params.put("personType", IReceiverUserServiceAdapter.RECEIVERS_TYPE.ADDRESS.name());
		List<OrdPerson> list=ordPersonDao.findOrdPersonList(params);
		if(CollectionUtils.isNotEmpty(list)){
			return list.get(0);
		}
		return null;
	}
	/**
	 * 需要优化日志??
	 */
	@Override
	public boolean insertInvoicePerson(OrdPerson ordPerson, Long invoiceId,String operatorId) {
		
		if (invoiceId != null){
			ordPerson.setObjectId(invoiceId);
			int count = ordPersonDao.insert(ordPerson);
			if (count > 0) {
				OrdAddress ordaddress = new OrdAddress();
				ordaddress.setOrdPersonId(ordPerson.getOrdPersonId());
				ordaddress.setCity(ordPerson.getAddressList().get(0).getCity());
				ordaddress.setProvince(ordPerson.getAddressList().get(0).getProvince());
				ordaddress.setStreet(ordPerson.getAddressList().get(0).getStreet());
				ordaddress.setPostalCode(ordPerson.getAddressList().get(0).getPostalCode());
				ordAddressDao.insert(ordaddress);
				return true;
			}
		}
		
		return false;
		
		/*if(invoiceId == null){
			int personId=ordPersonDao.insert(ordPerson);
			if(personId!=0){
				insertLog("ORDER_PERSON","ORD_INVOICE", personId,operatorId,Constant.COM_LOG_ORDER_EVENT.insertOrdPerson.name(),
						"添加发票收件信息", LogViewUtil.logNewStr(operatorId));
			}else{
				int count = ordPersonDao.updateByPrimaryKey(ordPerson);
				if(count > 0){
					this.insertLog("ORDER_PERSON","ORD_INVOICE",invoiceId, ordPerson.getOrdPersonId(),operatorId,
							Constant.COM_LOG_ORDER_EVENT.insertOrdPerson.name(),
							"修改发票收件信息", LogViewUtil.logNewStr(operatorId));
				}
			}
		}
		return true;*/
	}
	
	public void insertLog(String objectType,String parentType, Long parentId, Long objectId, String operatorName,
			String logType, String logName, String content){
		ComLog log = new ComLog();
		log.setParentId(parentId);
		log.setParentType(parentType);
		log.setObjectType(objectType);
		log.setObjectId(objectId);
		log.setOperatorName(operatorName);
		log.setLogType(logType);
		log.setLogName(logName);
		
		if (content != null)
			log.setContent(content);
		lvmmLogClientService.sendLog(log);
	}
	@Override
	public VstTravellerCallBackResponseDto updateTravellerPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {
		VstTravellerCallBackResponseDto callback=null;
		if(LOG.isInfoEnabled())
			LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==enter==");
		//参数校验
		callback=parameterValid(travellerRequest);
		if(callback==null){
			//日志防刷
			callback=queryTravellerCountValid(travellerRequest);
			if(callback.isSuccess()){
				//订单查询，根据订单id查询订单，并判断订单是否锁定
				OrdOrder order=ordOrderDao.selectByPrimaryKey(travellerRequest.getOrderId());
				if(order!=null&&"N".equals(order.getTravellerLockFlag())){
					//校验游玩人是否存在order中
					OrdPerson requestPerson = travellerRequest.getOrderPerson();
					//修改前的游玩人
					OrdPerson originalPerson=ordPersonDao.selectByPrimaryKey(requestPerson.getOrdPersonId());
					if(originalPerson!=null&&originalPerson.getObjectId()==travellerRequest.getOrderId()){
						
						if(StringUtil.isNotEmptyString(requestPerson.getIdNo())&&StringUtil.isNotEmptyString(requestPerson.getIdType())){
							//校验身份证证件是否重复
							callback=idNoValidation(travellerRequest, callback, requestPerson);
							if(!callback.isSuccess()){
								return callback;
							}
								
						}
						//修改游玩人次数
						StringBuilder sb = appendTravellerKey(travellerRequest,MemcachedEnum.OrderTravellerPersonLock.getKey());
						boolean isCountUpdated=updateOrdTravellerPersonCountByMemcached(sb.toString(),(callback.getTravellerCount()+1)+"",MemcachedEnum.OrderTravellerPersonLock.getSec());
						if(LOG.isInfoEnabled())
							LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人次数修改成功::"+isCountUpdated);
						if(!isCountUpdated){
							return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
									VSTEnum.ERROR_CODE.MEMBERCACHE_UPDATE_ERROR.getErrorMsg(),
									VSTEnum.ERROR_CODE.MEMBERCACHE_UPDATE_ERROR.getErrorCode(), false);
						}
						//修改游客信息
						int num=ordPersonDao.updateTraveller(requestPerson);
						if(num>0){
							//查询修改人的全部信息
							OrdPerson updatedPerson = ordPersonDao.selectByPrimaryKey(requestPerson.getOrdPersonId());
							//写入日志
							insertComLog(travellerRequest, originalPerson, updatedPerson);
							travellerRequest.setOrderPerson(updatedPerson);
							return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount()+1,MSG_SUCCESS, true);
						}	
					}
					//如果订单查不到人则返回
					if(LOG.isInfoEnabled())
						LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人信息不匹配=personId:"+travellerRequest.getOrderPerson().getOrdPersonId());
					return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
								VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorMsg(),
								VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorCode(), false);
				}
				//如果订单已锁定则返回
				if(LOG.isInfoEnabled())
					LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人锁定=personId:"+travellerRequest.getOrderPerson().getOrdPersonId());
				return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),MSG_IS_LOCK,VSTEnum.ERROR_CODE.MSG_IS_LOCK.getErrorCode(),false);
			}
			if(LOG.isInfoEnabled())
				LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人修改次数已达上限==personId:"+travellerRequest.getOrderPerson().getOrdPersonId());
		}
		if(LOG.isInfoEnabled())
			LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==end==");
		return callback;
	}
	
	
	
	@Override
	public VstTravellerCallBackResponseDto checkDestBuTravDelayPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {
		VstTravellerCallBackResponseDto callback=null;
		if(LOG.isInfoEnabled())
			LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==enter==");
		//参数校验
		callback=parameterValid(travellerRequest);
		if(callback==null){
			callback= new VstTravellerCallBackResponseDto(travellerRequest,0, MSG_SUCCESS, true);
			if(callback.isSuccess()){
				//订单查询，根据订单id查询订单，并判断订单是否锁定
				OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(travellerRequest.getOrderId());
				if("Y".equals(ordAccInsDelayInfo.getTravDelayFlag())&&"UNCOMPLETED".equals(ordAccInsDelayInfo.getTravDelayStatus())){
					//校验游玩人是否存在order中
					OrdPerson requestPerson = travellerRequest.getOrderPerson();
					
					if(StringUtil.isNotEmptyString(requestPerson.getIdNo())&&StringUtil.isNotEmptyString(requestPerson.getIdType())){
						//校验身份证证件是否重复
						callback=idNoValidation(travellerRequest, callback, requestPerson);
						if(!callback.isSuccess()){
							return callback;
						}
							
					}
					//如果订单查不到人则返回
					if(LOG.isInfoEnabled())
						LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人信息不匹配=personId:"+travellerRequest.getOrderPerson().getOrdPersonId());
					return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
								VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorMsg(),
								VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorCode(), false);
				}
			}
		}
		if(LOG.isInfoEnabled())
			LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==end==");
		return callback;
	}
	
	
	private VstTravellerCallBackResponseDto idNoValidation(VstTravellerCallBackRequest travellerRequest, VstTravellerCallBackResponseDto callback,
			OrdPerson requestPerson) {
		Map<String, Object> params=new HashMap<String, Object>();
		params.put("objectId", travellerRequest.getOrderId());
		params.put("personType", VstOrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
		params.put("objectType", VstOrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		List<OrdPerson> personList=ordPersonDao.findOrdPersonList(params);
		if(personList!=null&&personList.size()>0){
			for (OrdPerson ordPerson : personList) {
				if(ordPerson.getOrdPersonId().longValue()!=requestPerson.getOrdPersonId().longValue()&&
						ordPerson.getIdType()!=null&&
						ordPerson.getIdNo()!=null&&
						requestPerson.getIdType().equals(ordPerson.getIdType())&&
						requestPerson.getIdNo().equals(ordPerson.getIdNo())){
					return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
							VSTEnum.ERROR_CODE.INNO_INFO_REPEAT_ERROR.getErrorMsg(),
							VSTEnum.ERROR_CODE.INNO_INFO_REPEAT_ERROR.getErrorCode(), false);
				}
			}
		}
		return callback;
	}
	/**
	 * 游玩人日志记录
	 * @param travellerRequest
	 * @param person
	 * @param newPerson
	 */
	private void insertComLog(VstTravellerCallBackRequest travellerRequest,
			OrdPerson person, OrdPerson newPerson) {
		StringBuilder sb;
		sb=new StringBuilder();
		//拼接旧的游玩人
		sb.append("来源:"+(StringUtil.isEmptyString(travellerRequest.getChannelType())?"无":travellerRequest.getChannelType()));
		sb.append(",修改了游玩人信息");
		insertLogAppendInfo(person, sb, OLD);
		//拼接新的游玩人
		insertLogAppendInfo(newPerson, sb, NEW);
		String operatorName=travellerRequest.getUserCode();
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				travellerRequest.getOrderId(),
				travellerRequest.getOrderId(), 
				operatorName, 
				sb.toString(), 
				ComLog.COM_LOG_LOG_TYPE.CHANGE_TRAVELLER_SUCCESS.name(), 
				ComLog.COM_LOG_LOG_TYPE.CHANGE_TRAVELLER_SUCCESS.getCnName(), "");
	}
	private void insertLogAppendInfo(OrdPerson person, StringBuilder sb,
			char example) {
		sb.append("（"+example+"：");
		sb.append("中文姓名/"+(StringUtil.isEmptyString(person.getFullName())?"无":person.getFullName()));
		sb.append(SYMBOL_COMMA);
		sb.append("英文姓名/"+(StringUtil.isEmptyString(person.getEnglishName())?"无":person.getEnglishName()));
		sb.append(SYMBOL_COMMA);
		sb.append("手机号码/"+(StringUtil.isEmptyString(person.getMobile())?"无":person.getMobile()));
		sb.append(SYMBOL_COMMA);
		sb.append("人群/"+(StringUtil.isEmptyString(person.getPeopleTypeName())?"无":person.getPeopleTypeName()));
		sb.append(SYMBOL_COMMA);
		sb.append("证件类型/"+(StringUtil.isEmptyString(person.getIdTypeName())?"无":person.getIdTypeName()));
		sb.append(SYMBOL_COMMA);
		sb.append("证件号/"+(StringUtil.isEmptyString(person.getIdNo())?"无":person.getIdNo()));
		sb.append(SYMBOL_COMMA);
		sb.append("出生日期/"+(person.getBirthday()==null?"无":person.getBirthday()));
		sb.append(SYMBOL_COMMA);
		sb.append("性别/"+(StringUtil.isEmptyString(person.getGenderName())?"无":person.getGenderName()));
		sb.append("；");
	}

	/**
	 * 前台游客修改次数校验
	 * @param travellerRequest
	 * @return
	 */
	private VstTravellerCallBackResponseDto queryTravellerCountValid(
			VstTravellerCallBackRequest travellerRequest) {
		StringBuilder sb = appendTravellerKey(travellerRequest,MemcachedEnum.OrderTravellerPersonLock.getKey());
		//查询memcached
		int count=queryOrdTravellerPersonCountByMemcached(sb.toString());
		OrdPerson ordPerson = travellerRequest.getOrderPerson();
		if(ordPerson == null){
			return new VstTravellerCallBackResponseDto(travellerRequest,count, MSG_PARAMETER_NULL,VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(),false);
		}
		LOG.info("=updateTravellerPersonInfo==personId:"+ordPerson.getOrdPersonId()+"==orderId:"+travellerRequest.getOrderId()+"=update_traveller_count:"+count);
		if(count>=UPDATE_TRAVELLER_COUNT)
			 return new VstTravellerCallBackResponseDto(travellerRequest,count, MSG_UPDATE_TRAVELLER_COUNT_UP,VSTEnum.ERROR_CODE.MSG_UPDATE_TRAVELLER_COUNT_UP.getErrorCode(),false);
		return new VstTravellerCallBackResponseDto(travellerRequest,count, MSG_SUCCESS, true);
	}
	/**
	 * 游玩人修改次数key
	 * @param travellerRequest
	 * @return
	 */
	private StringBuilder appendTravellerKey(
			VstTravellerCallBackRequest travellerRequest,String key) {
		StringBuilder sb=new StringBuilder();
		sb.append(key);
		sb.append(travellerRequest.getOrderId());
		sb.append("_");
		sb.append(travellerRequest.getOrderPerson().getOrdPersonId());
		sb.append("lock");
		return sb;
	}
	
	/**
	 * 游玩人修改次数key
	 * @param travellerRequest
	 * @return
	 */
	private StringBuilder appendTravellerKeyByOrderId(
			VstTravellerCallBackRequest travellerRequest,String key) {
		StringBuilder sb=new StringBuilder();
		sb.append(key);
		sb.append(travellerRequest.getOrderId());
		sb.append("_");
		sb.append("lock");
		return sb;
	}
	
	/**
	 * 参数校验
	 * @param travellerRequest
	 * @return
	 */
	private VstTravellerCallBackResponseDto parameterValid(VstTravellerCallBackRequest travellerRequest) {

		if(!UtilityTool.isValid(travellerRequest)||
				travellerRequest.getOrderId()<=0l||
				travellerRequest.getOrderPerson() == null||
				travellerRequest.getOrderPerson().getOrdPersonId()<=0||
				StringUtil.isEmptyString(travellerRequest.getUserCode())||
				StringUtil.isEmptyString(travellerRequest.getOrderPerson().getFullName())){
			if(LOG.isInfoEnabled())
				LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==参数校验不合格==");
			return new VstTravellerCallBackResponseDto(travellerRequest, 0, MSG_PARAMETER_NULL,VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(),false);
		}
		return null;
	}
	
	/**
	 * 更新游客
	 * @return
	 */
	private boolean updateOrdTravellerPersonCountByMemcached(String travellerPersonKey,String value,int time){
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY); // 时
		int minutes = calendar.get(Calendar.MINUTE);    // 分
		int seconds = calendar.get(Calendar.SECOND);    // 秒
		time=(time-(hours*60*60+minutes*60+seconds));
		return MemcachedUtil.getInstance().set(travellerPersonKey,time, value);
	}
	
	/**
	 * 根据游客key查询memcached的=中对应的修改次数
	 * @param travellerPersonKey
	 * @return
	 */
	private int queryOrdTravellerPersonCountByMemcached(String travellerPersonKey){
		Object obj=MemcachedUtil.getInstance().get(travellerPersonKey);
		if(obj!=null)
			return Integer.parseInt(obj.toString());
		return 0;
	}
	@Override
	public List<OrdPerson> findOrdPerson(Long orderId, List<Long> personIds) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", orderId);
		List<OrdPerson> ordPersonList = ordPersonDao.findOrdPersonList(params);
		if(ordPersonList != null && ordPersonList.size() > 0){
			for (OrdPerson ordPerson : ordPersonList) {
				for (long ordPersonid : personIds) {
					if(ordPersonid==ordPerson.getOrdPersonId()&&VstOrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(ordPerson.getPersonType())){
						VstTravellerCallBackRequest travellerRequest = new VstTravellerCallBackRequest();
						OrdPerson ordPersonrequest = new OrdPerson();
						ordPersonrequest.setOrdPersonId(ordPerson.getOrdPersonId());
						travellerRequest.setOrderId(orderId);
						travellerRequest.setOrderPerson(ordPersonrequest);
						int count = queryTravellerCountValid(travellerRequest).getTravellerCount();
						if(count > 0){
							ordPerson.setCallBackCount(count);
						}
					}
				}
				
			}
			return ordPersonList;
		}
		return null;
	}
	
	@Override
	public List<OrdPerson> selectLatestContactPerson(Map<String, Object> params) {
		return this.ordPersonDao.selectLatestContactPerson(params);
	}
	@Override
	public VstTravellerCallBackResponseDto updateAndSaveTravellerPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {
		VstTravellerCallBackResponseDto callback=null;
		if(LOG.isInfoEnabled())
			LOG.info("====OrdPersonServiceImpl:updateAndSaveTravellerPersonInfo==enter==");
		//校验参数
		callback=parameterValidList(travellerRequest);
		if(callback!=null){
			return callback;
		}
		//日志防刷
		callback=queryTravellerCountValidByOrderId(travellerRequest);
		if(callback.isSuccess()){
			//订单查询，根据订单id查询订单，并判断订单是否锁定
			OrdOrder order=ordOrderDao.selectByPrimaryKey(travellerRequest.getOrderId());
			if(order!=null&&"N".equals(order.getTravellerLockFlag())){
				//查询游玩人信息
				Map<String, Object> params=new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				params.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				List<OrdPerson> ordpersonlist=ordPersonDao.findOrdPersonList(params);
				if(ordpersonlist == null || ordpersonlist.size() <= 0){
						return new VstTravellerCallBackResponseDto(travellerRequest,0, VSTEnum.ERROR_CODE.MSG_PARAMETER_ILLEGAL.getErrorMsg(),VSTEnum.ERROR_CODE.MSG_PARAMETER_ILLEGAL.getErrorCode(), false);
				}else{
					//游玩人信息不匹配校验
					callback=personValidationForOrderId(travellerRequest, callback, ordpersonlist);
					if(!callback.isSuccess())
						return callback;
					//校验身份证证件是否重复
					callback=idNoValidation(travellerRequest, callback,travellerRequest.getOrdPersonList());
					if(!callback.isSuccess())
						return callback;
					//批量修改游玩人
					insertPersonBatch(travellerRequest, ordpersonlist);
					//修改游玩人次数
					StringBuilder sb = appendTravellerKeyByOrderId(travellerRequest,MemcachedEnum.OrderTravellerOrderLock.getKey());
					boolean isCountUpdated=updateOrdTravellerPersonCountByMemcached(sb.toString(),(callback.getTravellerCount()+1)+"",MemcachedEnum.OrderTravellerOrderLock.getSec());
					if(LOG.isInfoEnabled())
						LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人次数修改成功::"+isCountUpdated);
					if(!isCountUpdated){
						return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
								VSTEnum.ERROR_CODE.MEMBERCACHE_UPDATE_ERROR.getErrorMsg(),
								VSTEnum.ERROR_CODE.MEMBERCACHE_UPDATE_ERROR.getErrorCode(), false);
					}
					return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount()+1,MSG_SUCCESS, true);
				}
			}
		}
		//查询订单游玩人校验
		return callback;
	}
	/**
	 * 批量插入游玩人
	 * @param travellerRequest
	 * @param ordpersonlist
	 */
	private void insertPersonBatch(VstTravellerCallBackRequest travellerRequest, List<OrdPerson> ordpersonlist) {
		for (OrdPerson requestPerson :  travellerRequest.getOrdPersonList()) {
			//修改游客信息
			int num=ordPersonDao.updateTraveller(requestPerson);
			if(num>0){
				//查询修改人的全部信息
				OrdPerson updatedPerson = ordPersonDao.selectByPrimaryKey(requestPerson.getOrdPersonId());
				//修改前的游玩人
				OrdPerson originalPerson=queryOrderPerson(requestPerson.getOrdPersonId(),ordpersonlist);
				//写入日志
				insertComLog(travellerRequest, originalPerson, updatedPerson);
				travellerRequest.setOrderPerson(updatedPerson);
			}
		}
	}
	/**
	 * 游玩人id匹配
	 * @param travellerRequest
	 * @param callback
	 * @param ordpersonlist
	 * @return
	 */
	private VstTravellerCallBackResponseDto personValidationForOrderId(VstTravellerCallBackRequest travellerRequest,
			VstTravellerCallBackResponseDto callback, List<OrdPerson> ordpersonlist) {
		for (OrdPerson requestPerson : travellerRequest.getOrdPersonList()) {
			//修改前的游玩人
			OrdPerson originalPerson=queryOrderPerson(requestPerson.getOrdPersonId(),ordpersonlist);
			if(originalPerson==null){
				//如果订单查不到人则返回
				if(LOG.isInfoEnabled())
					LOG.info("====OrdPersonServiceImpl:updateTravellerPersonInfo==游玩人信息不匹配=personId:"+travellerRequest.getOrderPerson().getOrdPersonId());
				return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
							VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorMsg(),
							VSTEnum.ERROR_CODE.ORDERID_ORTRAVELLER_ID_IS_ERROR.getErrorCode(), false);
			}
		}
		return callback;
	}
	
	private VstTravellerCallBackResponseDto idNoValidation(VstTravellerCallBackRequest travellerRequest, VstTravellerCallBackResponseDto callback,
			List<OrdPerson> personList) {
		if(personList!=null&&personList.size()>0){
			for (OrdPerson ordPerson : personList) {
				for (OrdPerson element  :personList) {
					if(ordPerson.getOrdPersonId().longValue()!=element.getOrdPersonId().longValue()&&
							StringUtil.isNotEmptyString(ordPerson.getIdType())&&
							StringUtil.isNotEmptyString(ordPerson.getIdNo())&&
							ordPerson.getIdType().equals(element.getIdType())&&
							ordPerson.getIdNo().equals(element.getIdNo())){
						return new VstTravellerCallBackResponseDto(travellerRequest,callback.getTravellerCount(),
								VSTEnum.ERROR_CODE.INNO_INFO_REPEAT_ERROR.getErrorMsg(),
								VSTEnum.ERROR_CODE.INNO_INFO_REPEAT_ERROR.getErrorCode(), false);
					}
				}
			}
		}
		return callback;
	}
	/*
	 * 参数校验
	 */
	private VstTravellerCallBackResponseDto parameterValidList(VstTravellerCallBackRequest travellerRequest){
		if(travellerRequest==null||travellerRequest.getOrderId()<=0l||StringUtil.isEmptyString(travellerRequest.getUserCode())){
			return new VstTravellerCallBackResponseDto(travellerRequest, 0, MSG_PARAMETER_NULL,VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(),false);
		}
		if(travellerRequest.getOrdPersonList()==null||travellerRequest.getOrdPersonList().size()<=0){
			return new VstTravellerCallBackResponseDto(travellerRequest, 0, MSG_PARAMETER_NULL,VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(),false);
		}
		for (OrdPerson element : travellerRequest.getOrdPersonList()) {
			if(element.getOrdPersonId()==0||StringUtil.isEmptyString(element.getFullName())){
				return new VstTravellerCallBackResponseDto(travellerRequest, 0, MSG_PARAMETER_NULL,VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(),false);
			}
		}
		return null;
	}
	
	/**
	 * 日志防刷
	 * @param travellerRequest
	 * @return
	 */
	private VstTravellerCallBackResponseDto queryTravellerCountValidByOrderId(VstTravellerCallBackRequest travellerRequest){
		StringBuilder sb = appendTravellerKeyByOrderId(travellerRequest,MemcachedEnum.OrderTravellerOrderLock.getKey());
		//查询memcached
		int count=queryOrdTravellerPersonCountByMemcached(sb.toString());
		LOG.info("=updateAndSaveTravellerPersonInfo===orderId:"+travellerRequest.getOrderId()+"=update_traveller_count:"+count);
		if(count>=UPDATE_TRAVELLER_COUNT)
			 return new VstTravellerCallBackResponseDto(travellerRequest,count, MSG_UPDATE_TRAVELLER_COUNT_UP,VSTEnum.ERROR_CODE.MSG_UPDATE_TRAVELLER_COUNT_UP.getErrorCode(),false);
		return new VstTravellerCallBackResponseDto(travellerRequest,count, MSG_SUCCESS, true);
	}
	/**
	 * 获取orderPerson
	 * @return
	 */
	private OrdPerson queryOrderPerson(long personId,List<OrdPerson> ordpersonlist){
		for (OrdPerson ordPerson : ordpersonlist) {
			if(personId==ordPerson.getOrdPersonId())
				return ordPerson;
		}
		return null;
	}
	@Override
	public int queryListTraverllerCountByOrderId(Long orderId) {
		if(orderId<=0)
			return -1;
		VstTravellerCallBackRequest travellerRequest = new VstTravellerCallBackRequest();
		travellerRequest.setOrderId(orderId);
		VstTravellerCallBackResponseDto callback=queryTravellerCountValidByOrderId(travellerRequest);
		if(callback!=null){
			return callback.getTravellerCount();
		}
		return 0;
	}
	@Override
	public CallBackResponseDto updateOrderTravellerInfo(CallBackRequestDto requestDto) {
		if(requestDto==null){
			return new CallBackResponseDto(false,CallBackResponseDto.ERROR_CODE.CODE_501.getMsg(),CallBackResponseDto.ERROR_CODE.CODE_501.getCode());
		}
		LOG.info("===enter updateOrderTravellerInfo== orderId:"+requestDto.getOrderId());
		//查询游玩人信息
		try{
		Map<String, Object> params=new HashMap<String, Object>();
		params.put("objectId", requestDto.getOrderId());
		params.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		List<OrdPerson> ordpersonlist=ordPersonDao.findOrdPersonList(params);
		if(ordpersonlist == null || ordpersonlist.size() <= 0){
			return new CallBackResponseDto(false,VSTEnum.ERROR_CODE.MSG_PARAMETER_ILLEGAL.getErrorMsg(),VSTEnum.ERROR_CODE.MSG_PARAMETER_ILLEGAL.getErrorCode());
		}
		int count=ordPersonDao.deleteByOrderId(requestDto.getOrderId());
		if(count<0){
			return new CallBackResponseDto(false,CallBackResponseDto.ERROR_CODE.CODE_500.getMsg(),CallBackResponseDto.ERROR_CODE.CODE_500.getCode());
		}
		OrdPerson ordperson=null;
		for (VstTravellerInfo travellerInfo : requestDto.getVstTravellerlist()) {
			ordperson=new OrdPerson();
			ordperson.setObjectId(requestDto.getOrderId());
			ordperson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			ordperson.setFullName(travellerInfo.getFullName());
			ordperson.setFirstName(travellerInfo.getEnglishName());
			ordperson.setIdType(travellerInfo.getIdType());
			ordperson.setIdNo(travellerInfo.getIdNo());
			ordperson.setMobile(travellerInfo.getPhone());
			ordperson.setGender(travellerInfo.getGender());
			ordperson.setPeopleType(travellerInfo.getPeopleType());
			ordperson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
			ordPersonDao.insert(ordperson);
		}
		ordperson=new OrdPerson();
		ordperson.setObjectId(requestDto.getOrderId());
		ordperson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		ordperson.setFullName(requestDto.getContactPerson());
		ordperson.setMobile(requestDto.getContactPhone());
		ordperson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		ordperson.setEmail(requestDto.getContactEmail());
		ordPersonDao.insert(ordperson);
		}catch(Exception e){
			LOG.error("=exception=orderId:"+requestDto.getOrderId()+"==="+e);
			return new CallBackResponseDto(false,CallBackResponseDto.ERROR_CODE.CODE_500.getMsg(),CallBackResponseDto.ERROR_CODE.CODE_500.getCode());
		}
		CallBackResponseDto callback=new CallBackResponseDto();
		callback.setCallBackRequest(requestDto);
		callback.setSuccess(true);
		callback.setMessage("ok");
		return callback;
	}

	@Override
	public List<OrdPerson> getBookPersonInfoByOrderId(Map<String, Object> var1) {
			List<OrdPerson> ls = ordPersonDao.getBookPersonInfoByOrderId(var1);
			return ls;
		}

}
