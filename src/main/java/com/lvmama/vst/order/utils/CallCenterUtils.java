package com.lvmama.vst.order.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.lang3.StringUtils;

import com.caucho.hessian.client.HessianProxyFactory;
import com.lvmama.comm.utils.HttpsUtil;
import com.lvmama.lvccweb.po.TRecordInfo;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.order.po.OrderCallId;
import com.lvmama.vst.order.service.IOrderCallIdService;

public class CallCenterUtils {
	
	private static Log log = LogFactory.getLog(CallCenterUtils.class);
	
	public static final String ORDERID_CALLID_SUBSTR1 = "将CallId:";
	public static final String ORDERID_CALLID_SUBSTR2 = "关联到订单号:";
	public static final String ORDERID_CALLID_SUBSTR3 = "[";
	public static final String ORDERID_CALLID_SUBSTR4 = "]";
	
	public static Long PARENT_DELTA = 1L;
	
	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 获取客服语音数据
	 * @return
	 */
	private static TRecordInfoService getRecordInfoService(){
    	HessianProxyFactory factory = new HessianProxyFactory();
    	factory.setOverloadEnabled(true);

    	try {
			String lvccServiceUrl = new String();

			if(StringUtils.isNotBlank(ZooKeeperConfigProperties.getProperties("lvcc.serviceUrl"))){
				lvccServiceUrl = ZooKeeperConfigProperties.getProperties("lvcc.serviceUrl");
			}else {
				try {
					Properties properties = new Properties();
					properties.load(CallCenterUtils.class.getResourceAsStream("/const.properties"));
					lvccServiceUrl = properties.getProperty("lvcc.serviceUrl");
					if (StringUtils.isBlank(lvccServiceUrl)) {
						lvccServiceUrl = "http://10.17.1.20:8080/ivrprocess/remoting";
					}
				} catch (Throwable e) {
					log.warn("Cannot load const file!", e);
				}
			}
			lvccServiceUrl = lvccServiceUrl + "/recordInfoService";
			log.info("lvccServiceUrl:"+lvccServiceUrl);

			return (TRecordInfoService) factory.create(TRecordInfoService.class, lvccServiceUrl);
		} catch (MalformedURLException e) {
			log.error("factory.create error!",e);
		}
		return null;		
	}
	
	public static List<TRecordInfo> getRecordInfoWithCallId(String callId, String createDateofCallId){
		if (StringUtils.isBlank(callId) || StringUtils.isBlank(createDateofCallId) ||
			  //逗号个数必须相等
			  (callId.length() - callId.replace(",", "").length() ) != 
			  ( createDateofCallId.length() - createDateofCallId.replace(",", "").length() )){
			return null;
		}
		else{
			try{
				TRecordInfoService service = CallCenterUtils.getRecordInfoService();
	    		if (null != service){
	    			List<TRecordInfo> list = service.getRecordInfosWithCallid(callId, createDateofCallId);
	    			
	    			if (null == list){
	    				log.error("list null");
	    			}
	    			else{
	    				return list;
	    			}
	    		}
	    		else{
	    			log.error("service null");
	    		}				
			}
			catch(Exception e){
				e.printStackTrace();
			}

			
			return null;
		}
	}
	
	public static Page<ComLog> getOrdLogInfo(Long orderId, Long curPage, Map<String, String> urlParamMap){
		
		StringBuilder sb = new StringBuilder();
		String value;
		for(String key : urlParamMap.keySet()){
			if (StringUtils.isNotBlank(key)){
				value = urlParamMap.get(key);
				if (StringUtils.isNotBlank(value)){
					sb.append(key + "=" + value);
					sb.append("&");
				}
			}
		}
		String params = sb.toString();
		if (params.endsWith("&")){
			params = params.substring(0,params.length()-1);
		}
		String Url = "http://super.lvmama.com/lvmm_log/bizLog/findJson.do?" + params;
		//String Url = "http://super.lvmama.com/lvmm_log/bizLog/findJson.do?parentId=42936128&sysName=VST&objectType=ORD_ORDER_ORDER&curPage=1";
		
		System.out.println("[getOrdLogInfo]url :"+Url);
		
		Page<ComLog> result = new Page<ComLog>();
		try{
			String json = HttpsUtil.requestGet(Url);
			JSONObject obj  = JSONObject.fromObject(json);
			
			if (obj.has("totalRows") && obj.getLong("totalRows") > 0 && obj.has("itemList")){
				JSONArray ary = obj.getJSONArray("itemList");
				List<ComLog> list = new ArrayList<ComLog>();
				for (int i = 0; i<ary.size(); i++){
					ComLog log = new ComLog();
					JSONObject item = ary.getJSONObject(i);
					log.setLogId(item.getLong("logId"));
					log.setCreateTime(new Date(item.getLong("createTime")));
					log.setContent(item.getString("content"));
					log.setLogType(item.getString("logType"));
					log.setLogName(item.getString("logName"));
					log.setMemo(item.getString("memo"));
					log.setOperatorName(item.getString("operatorName"));
					log.setObjectType(item.getString("objectType"));
					log.setObjectId(item.getLong("objectId"));
					log.setParentId(item.getLong("parentId"));
					log.setParentType(item.getString("parentType"));
					log.setContentType(item.getString("contentType"));
					
					list.add(log);
				}
				
				result.setCurrentPage(curPage);
				Long pageSize = obj.getLong("pageSize");
				Long totalRows = obj.getLong("totalRows");
				Long pageNum = 0L;
				if (pageSize > 0){
					pageNum = (long) Math.ceil(totalRows / pageSize);
				}
				result.setTotalResultSize(totalRows);
				result.setTotalPageNum(pageNum);
				result.setItems(list);	
			}
			
		}
		catch(Exception e){
			log.error(e);
			e.printStackTrace();
		}
		
		if (result.getItems() == null){
			result.setCurrentPage(0L);
			result.setTotalResultSize(0L);
			result.setTotalPageNum(0L);
		}
		
		return result;
	}

	private static Map<String, String> splitContent(String content){
		String strLike1 = CallCenterUtils.ORDERID_CALLID_SUBSTR1;
		String strLike2 = CallCenterUtils.ORDERID_CALLID_SUBSTR2;
		String callId = null;
		String orderId = null;
		String createDateOfCallId = null;
		Map<String, String> map = new HashMap<String, String>();
		if(StringUtils.isBlank(content) || content.indexOf(strLike1) == -1 || 
				content.indexOf(strLike2) == -1){
			orderId = "";
			callId = "";
			createDateOfCallId = "";
		}
		else{
			int index1 = content.indexOf(ORDERID_CALLID_SUBSTR3);
			int index2 = content.indexOf(ORDERID_CALLID_SUBSTR4);
			
			if (index1 != -1 && index2 != -1){
				callId = content.substring(index1 + 1, index2);
			}
			
			index1 = content.indexOf(ORDERID_CALLID_SUBSTR3, index2 + 1);
			index2 = content.indexOf(ORDERID_CALLID_SUBSTR4, index2 + 1);
			
			if (index1 != -1 && index2 != -1){
				orderId = content.substring(index1 + 1, index2);
			}
			
			index1 = content.indexOf(ORDERID_CALLID_SUBSTR3, index2 + 1);
			index2 = content.indexOf(ORDERID_CALLID_SUBSTR4, index2 + 1);
			
			if (index1 != -1 && index2 != -1){
				createDateOfCallId = content.substring(index1 + 1, index2);
			}			
		}
		
		map.put("orderId", orderId);
		map.put("callId", callId);		
		map.put("createdateofcallid", createDateOfCallId);		
		return map;
	}
	
	//生成录音的超链接的html
	public static String generateSoundRecHtml(List<TRecordInfo> recInfoList, String callId){
		if (recInfoList == null || StringUtils.isBlank(callId)){
			return "";
		}
		String htmlFormatStr = "<a href=\"http://cc.lvmama.com/qmanager/%s\" target=\"_blank\">播放录音%d</a>";
		StringBuilder sb = new StringBuilder();
		String fileName;
		String url;
		int index = 1;
		for(TRecordInfo item : recInfoList){
			if (!callId.equalsIgnoreCase(item.getCallId())){
				continue;
			}
			fileName = item.getFileName();
			if (StringUtils.isNotBlank(fileName)){
				String[] urlAry = fileName.split("\\*");
				if (urlAry.length != 2){
					continue;
				}
				if ("new".equalsIgnoreCase(urlAry[1])){
					url = "player_v11_html5.jsp?fileUrl="+urlAry[0];
				}
				else{
					url = "player_v12_html5.jsp?fileUrl="+urlAry[0];
				}
				sb.append(String.format(htmlFormatStr, url, index));
				sb.append("</br>\r\n");	
				index++;
			}

		}
		
		
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param orderId
	 * @param page
	 * @param urlParamMap
	 * @return
	 */
	public static Page<ComLog> getRealComLogPageInfo(Long orderId, Integer page, Map<String, String> urlParamMap) {
		Integer curPage = 1;
		if (null != page){
			curPage = page;
		}
		Page<ComLog> comLogPage = null;
		if (null != orderId){
			comLogPage = CallCenterUtils.getOrdLogInfo(orderId, Long.valueOf(curPage), urlParamMap);
			
			DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (comLogPage != null && comLogPage.getItems() != null && comLogPage.getTotalResultSize() > 0){
				List<ComLog> list = comLogPage.getItems();
				String content;
				String strLike1 = CallCenterUtils.ORDERID_CALLID_SUBSTR1;
				String strLike2 = CallCenterUtils.ORDERID_CALLID_SUBSTR2;
				List<ComLog> needToUpdateMemoList = new ArrayList<ComLog>();
				StringBuilder sb = new StringBuilder();
				StringBuilder sbCreate = new StringBuilder();
				for(ComLog item : list){
					content = item.getContent();
					if (content.indexOf(strLike1) != -1 && content.indexOf(strLike2) != -1){
						Map<String, String> map = CallCenterUtils.splitContent(content);
						String tmpOrderId = map.get("orderId");
						String callId = map.get("callId");
						String createDateOfCallId = StringUtils.isBlank(map.get("createdateofcallid"))?f.format(new Date()):map.get("createdateofcallid");
						if (tmpOrderId.equals(orderId.toString())){
							needToUpdateMemoList.add(item);
							if (sb.indexOf(callId+",") == -1){
								sb.append(callId);
								sb.append(",");
								//
								sbCreate.append(createDateOfCallId);
								sbCreate.append(",");
							}
						}
					}
				}
				//
				String callIds;
				if (",".equals(sb.substring(sb.length() - 1))){
					callIds = sb.toString().substring(0, sb.length() - 1);
				}
				else{
					callIds = sb.toString();
				}
				String createDateOfCallIds;
				if (",".equals(sbCreate.substring(sbCreate.length() - 1))){
					createDateOfCallIds = sbCreate.toString().substring(0, sbCreate.length() - 1);
				}
				else{
					createDateOfCallIds = sbCreate.toString();
				}
				List<TRecordInfo> recInfoList = CallCenterUtils.getRecordInfoWithCallId(callIds, createDateOfCallIds);
				//
				for(ComLog item : needToUpdateMemoList){
					content = item.getContent();
					if (content.indexOf(strLike1) != -1 && content.indexOf(strLike2) != -1){
						Map<String, String> map = CallCenterUtils.splitContent(content);
						String tmpOrderId = map.get("orderId");
						String callId = map.get("callId");
						//
						item.setMemo(generateSoundRecHtml(recInfoList,callId));
					}
				}
				
			}
			
		}
		
		return comLogPage;
	}		
	
	public static String getContent(Long orderId, String callId, String createDateOfCallId){
		return CallCenterUtils.ORDERID_CALLID_SUBSTR1+CallCenterUtils.ORDERID_CALLID_SUBSTR3+callId+CallCenterUtils.ORDERID_CALLID_SUBSTR4+
		         CallCenterUtils.ORDERID_CALLID_SUBSTR2+CallCenterUtils.ORDERID_CALLID_SUBSTR3+orderId.toString()+CallCenterUtils.ORDERID_CALLID_SUBSTR4+
		         CallCenterUtils.ORDERID_CALLID_SUBSTR3+createDateOfCallId+CallCenterUtils.ORDERID_CALLID_SUBSTR4;
	}
	
	public static String getContent(Long orderId, String callId){
		return CallCenterUtils.ORDERID_CALLID_SUBSTR1+CallCenterUtils.ORDERID_CALLID_SUBSTR3+callId+CallCenterUtils.ORDERID_CALLID_SUBSTR4+
		         CallCenterUtils.ORDERID_CALLID_SUBSTR2+CallCenterUtils.ORDERID_CALLID_SUBSTR3+orderId.toString()+CallCenterUtils.ORDERID_CALLID_SUBSTR4;
	}	
	
	public static Boolean isExistsCallIdJoin(Page<ComLog> PageInfo, Long orderId, String callId, String createDateOfCallId){
		if (PageInfo == null || PageInfo.getItems() == null || 
				PageInfo.getItems().size() <= 0 || StringUtils.isBlank(callId) || orderId == null){
			return false;
		}
		List<ComLog> items = PageInfo.getItems();
		String content;
		Map<String, String> map;
		String tmpOrderId;
		String tmpcallId;		
		for(ComLog item : items){
			content = item.getContent();
			map = CallCenterUtils.splitContent(content);	
			tmpOrderId = map.get("orderId");
			tmpcallId = map.get("callId");	
			if (tmpOrderId.equalsIgnoreCase(orderId.toString()) && tmpcallId.equalsIgnoreCase(callId)){
				return true;
			}
		}
		
		return false;
	}
	
	public static Page<ComLog> adapterOrderCallToComLog(Long orderId, IOrderCallIdService service){
		Page<ComLog> result = new Page<ComLog>();
		result.setCurrentPage(0L);
		result.setTotalResultSize(0L);
		result.setTotalPageNum(0L);		
		if (service == null){
			return result;
		}
		
		List<OrderCallId> list = service.selectByParams(orderId, null);
		if (list != null && list.size() > 0){
			StringBuilder callidSB = new StringBuilder();
			StringBuilder createDateSB = new StringBuilder();
			result.setItems(new ArrayList<ComLog>(list.size()));
			for(OrderCallId item : list){
				ComLog comLog = new ComLog();
				comLog.setOperatorName(item.getOperUserName());
				comLog.setCreateTime(item.getCreateTime());
				comLog.setContent(CallCenterUtils.getContent(orderId, item.getCallId()));
				comLog.setMemo(item.getCallId()); //先用memo字段临时保存callId
				
				callidSB.append(item.getCallId());
				callidSB.append(",");
				
				createDateSB.append(df.format(item.getCreateTime()));
				createDateSB.append(",");
				
				result.getItems().add(comLog);
			}
			
			//
			String callIds;
			if (",".equals(callidSB.substring(callidSB.length() - 1))){
				callIds = callidSB.toString().substring(0, callidSB.length() - 1);
			}
			else{
				callIds = callidSB.toString();
			}
			String createDateOfCallIds;
			if (",".equals(createDateSB.substring(createDateSB.length() - 1))){
				createDateOfCallIds = createDateSB.toString().substring(0, createDateSB.length() - 1);
			}
			else{
				createDateOfCallIds = createDateSB.toString();
			}
			List<TRecordInfo> recInfoList = CallCenterUtils.getRecordInfoWithCallId(callIds, createDateOfCallIds);
			//
			List<ComLog> refComLogList = result.getItems(); 
			for(ComLog item : refComLogList){
				String callId = item.getMemo();
				item.setMemo(generateSoundRecHtml(recInfoList,callId));
			}
			
			
		}
		
		
		
		
		
		return result;
	}
	
	
	

}
