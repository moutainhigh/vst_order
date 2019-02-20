package com.lvmama.vst.order.service.flight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizDictProp;
import com.lvmama.vst.back.client.biz.service.DictPropClientService;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.flight.info.AirLineInfo;
import com.lvmama.vst.order.service.flight.info.PlaneTypeInfo;

/**
 * 航空信息服务
 * @author libing
 */
@Service
public class AirlineService {
	
	@Autowired 
	private DictPropClientService dictPropClientRemote;

	/**
	 * 查询航空公司信息
	 * @param code	航空公司编码
	 * @return
	 */
	public AirLineInfo findCompanyByCode(String code){
		AirLineInfo retInfo = new AirLineInfo();
		retInfo.setId(70L);
		retInfo.setCode(code);
		ResultHandleT<BizDict> resultDict = dictPropClientRemote.findDictByProp(retInfo.getId(), code);
		if(!resultDict.isSuccess() || resultDict.getReturnContent()==null){
			return null;
		}
		BizDict bizDict = resultDict.getReturnContent(); 
	    retInfo.setName( bizDict.getDictName() );
	    for(BizDictProp prop: bizDict.getDictPropList()){ 
	        System.out.println("propId="+prop.getDictPropId()+"\tpropValue="+prop.getDictPropValue()); 
	        if(prop.getDictPropId()==70L){
	        	retInfo.setCode(prop.getDictPropValue());
	        }
	    } 

		return retInfo;
	}
	
	/**
	 * 查询机型信息
	 * @param code	机型编码
	 * @return
	 */
	public PlaneTypeInfo findPlaneTypeByCode(String code){
		PlaneTypeInfo retInfo = new PlaneTypeInfo();
		retInfo.setId(101L);
		retInfo.setCode(code);
		ResultHandleT<BizDict> resultDict = dictPropClientRemote.findDictByProp(retInfo.getId(), code); 
		if(!resultDict.isSuccess() || resultDict.getReturnContent()==null){
			return null;
		}
	    BizDict bizDict = resultDict.getReturnContent(); 
	    retInfo.setName( bizDict.getDictName() );
	    for(BizDictProp prop: bizDict.getDictPropList()){ 
	        if(prop.getDictPropDefId()==100L){	//制造商
	        	retInfo.setManufacturer(prop.getDictPropValue());
	        }
	        else if(prop.getDictPropDefId()==101L){	//机型代码
	        	retInfo.setCode(prop.getDictPropValue());
	        }
	        else if(prop.getDictPropDefId()==102L){	//机型类型描述
	        	retInfo.setTypeDesp(prop.getDictPropValue());
	        }
	        else if(prop.getDictPropDefId()==103L){	//最少座位数
	        	try{
	        		retInfo.setMinSeats(Long.valueOf( prop.getDictPropValue()));
	        	}catch(Exception ex){}
	        }
	        else if(prop.getDictPropDefId()==104L){	//最大座位数
	        	try{
	        		retInfo.setMaxSeats(Long.valueOf( prop.getDictPropValue()));
	        	}catch(Exception ex){}
	        }
	    } 

		return retInfo;
	}
}
