package com.lvmama.vst.order.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizFlight;
import com.lvmama.vst.back.biz.po.BizTrain;
import com.lvmama.vst.back.biz.po.BizTrainSeat;
import com.lvmama.vst.back.biz.po.BizTrainStop;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.prod.service.ProdContractDetailClientService;
import com.lvmama.vst.back.client.prod.service.ProdGroupDateClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.BuildRouteTemplateUtil;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.RouteDetailFormat;
import com.lvmama.vst.back.dujia.comm.route.po.ProdRouteFeature;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdGroupDate;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.prod.po.ProdTrafficBus;
import com.lvmama.vst.back.prod.po.ProdTrafficFlight;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.prod.po.ProdTrafficTrain;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.supp.po.SuppContract;
import com.lvmama.vst.back.supp.po.SuppContract.ACC_SUBJECT;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.vo.TrafficForRouteDetailVO;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Controller
public class localRouteProductAction{
	/**
	 * @author liuxiuxiu
	 * @since  2017.07.11
	 * 国内产品下载
	 */
	private static final Log LOG = LogFactory.getLog(localRouteProductAction.class);
	
	private static final String templateName = "routeDown.ftl";
	
	public static final String TRAVEL_ECONTRACT_DIRECTORY = "/WEB-INF/resources/econtractTemplate";
	
	protected static boolean  isDubgPdf = false;//开发的时候设置为true，上线设置为false
	
	private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
    private static final String regEx_html = "<[^>]+>"; 
    private static final String regEx_space = "\\s*|\t|\r|\n";
	
	 @Autowired
	 protected ProdProductClientService prodProductClientRemote;      
	 
	 @Autowired
	 private ProdLineRouteClientService prodLineRouteClientService;
	 
	 @Autowired
	 private ProdGroupDateClientService prodGroupDateClientService;
	 
//	 @Autowired
//	 private LineProductService lineProductServcie;
	 
	 @Autowired
	 protected ProdTrafficClientService prodTrafficClientServiceRemote;
	 
	 @Autowired
	 protected DictClientService dictClientRemote;
	 
	 @Autowired
	 private ProdProductNoticeClientService prodProductNoticeClientRemote;
	 
	 @Autowired
	 private ProdContractDetailClientService prodContractDetailClientRemote;
	 
	 @Autowired
	 private ProdProductClientService prodProductClientService;

	
	@RequestMapping(value = "/ord/local/downloadHtmlOrPdf.do")
	public String downloadHtmlOrPdf(Model model,HttpServletRequest request, HttpServletResponse response)
			throws BusinessException {
		
		Long startDistrictId = null;
		Long productId = null;
		Long routeId = null;
		if(StringUtils.isNotEmpty(request.getParameter("startDistrictId"))){
			startDistrictId = Long.parseLong(request.getParameter("startDistrictId"));
		}
		if(StringUtils.isNotEmpty(request.getParameter("productId"))){
			productId = Long.parseLong(request.getParameter("productId"));
		}
		if(StringUtils.isNotEmpty(request.getParameter("routeId"))){
			routeId = Long.parseLong(request.getParameter("routeId"));
		}
		String isPdf = request.getParameter("isPdf");
		
		try {
			Map<String,Object> rootMap=new HashMap<String, Object>();	
			// 得到线路的产品信息
	        ProdProductParam param = new ProdProductParam();
	        param.setProdEcontract(true);
	        ResultHandleT<ProdProduct> productHandleT = prodProductClientRemote.findLineProductByProductId(productId, param);
	        
	        ProdProduct product = productHandleT.getReturnContent();
	        if(product!=null){
	        	//设置行程
	        	if(routeId != null){
	        		this.findRouteByRouteId(product,routeId);
	        	}
	        	//设置二维码访问路径
	        	product.setUrlQR(excuteQRDataFileByPhoneMP(productId, startDistrictId));
	        	//获取行程出行日期列表
	        	setProdGroupDateList(productId,product.getProdLineRouteList(),startDistrictId,model);
	        	
//	        	lineProductServcie.changeRouteStaticsRouteVo(product);
	        	
	        	
	        	//解决包含项目的问题
	            if(product.getPropValue() != null){
	                Map<String, Object> propValueMap = product.getPropValue();
	                //处理供应商打包的并且包含交通信息的
	                String trafficFlag = (String)propValueMap.get("traffic_flag");
	                if(trafficFlag != null && "Y".equalsIgnoreCase(trafficFlag)){
	                    setTrafficInfo(product,model,rootMap);
	                }
	            }
	            
	        	
				Map<String, String> prodProductPropMap = new HashMap<String, String>();
				if (product.getPropValue() != null) {
					for (String key : product.getPropValue().keySet()) {
						Object v = product.getPropValue().get(key);
						//出行警示及说明 ,退改说明
						if ("warning".equals(key) || "change_and_cancellation_instructions".equals(key)) {
							prodProductPropMap.put(key, v == null ? "" : getTextFromHtml(v.toString()));
						}
					}
				}
				
				
				if (prodProductPropMap != null && prodProductPropMap.size() > 0) { 
					rootMap.put("prodProductPropMap", prodProductPropMap);
				}
	        	
				//国内的自主打包的自行组团的跟团游,设置合同主体为包中的第一个当地游产品的合同主体
	            if("INNERLINE".equals(product.getProductType())||"INNERSHORTLINE".equals(product.getProductType())||"INNERLONGLINE".equals(product.getProductType())||"INNER_BORDER_LINE".equals(product.getProductType())){
	            	if("LVMAMA".equals(product.getPackageType())&&product.getBizCategory().getCategoryId() == 15&&"SELF_TOUR".equals(product.getProdEcontract().getGroupType())){
	            		Long SuppContrantId=prodProductClientRemote.selectFirstSuppContrantIdByProductId(productId);
	                	if(SuppContrantId!=null&&SuppContrantId>0){
	                    	SuppContract suppContract =getSuppContract(SuppContrantId);
	                    	String suppContractCode=suppContract.getAccSubject();
	                    	if(StringUtils.isNotEmpty(suppContractCode)&&(!suppContractCode.equals(ACC_SUBJECT.JOYU.getCode()))&&!suppContractCode.equals(ACC_SUBJECT.JOYU_2015.getCode())){
	                    		String suppContractName=ACC_SUBJECT.getCnName(suppContract.getAccSubject());
	                    		rootMap.put("suppContractName",suppContractName);
	                    	}
	                	}
	            	}
	            }
	            //设置预订须知的公告
	            setNotice(model,product.getProductId(),rootMap);
	        	
	           //得到页面预订须知中的推荐项目和购物说明
	           setRecommendItemAndShopping(product.getProdLineRouteList());
	           
	           //产品特色
	           List<ProdRouteFeature> prodRoutefeatureList =prodProductClientService.findProdRouteFeatureByProdId(productId).getReturnContent();
	           if(prodRoutefeatureList != null && prodRoutefeatureList.size() > 0){
	        	   rootMap.put("prodRoutefeatureList", prodRoutefeatureList);
	           }
	        }
	        
	        rootMap.put("routeId", routeId);
	        rootMap.put("startDistrictId", startDistrictId);
			rootMap.put("routeDetailFormat", new RouteDetailFormat());
			rootMap.put("routeShowType", "PAGE");
			rootMap.put("product", product);
			
			
			File directioryFile = initDirectory();
			if (directioryFile == null || !directioryFile.exists()) {
				model.addAttribute("error", "产品id："+product.getProductId()+"产品详情模板目录不存在：");
    			return "/order/econtractTemplate/localRouteTemplateHtml";
			}
			
			//初始化freemarker
			Configuration configuration = initConfiguration(directioryFile);
			if(configuration == null){
				model.addAttribute("error","产品id："+product.getProductId()+"初始化模板失败");
    			return "/order/econtractTemplate/localRouteTemplateHtml";
			}
			
			//初始化ftl模板
			Template template = configuration.getTemplate(templateName.toString());
			if(template == null){
				model.addAttribute("error","产品id："+product.getProductId()+"初始化ftl模板失败。");
    			return "/order/econtractTemplate/localRouteTemplateHtml";
			}
			
			StringWriter sw = new StringWriter();
			template.process(rootMap, sw);
			String htmlString = sw.toString();
			if(htmlString == null){
				model.addAttribute("error","产品id："+product.getProductId()+"模板HTML生成失败。");
    			return "/order/econtractTemplate/localRouteTemplateHtml";
			}
			
			model.addAttribute("htmlString", htmlString);
			
			if(htmlString != null && "Y".equals(isPdf)){
				ByteArrayOutputStream baos = null;
				long Stime = System.currentTimeMillis();
				baos = PdfUtil.createPdfFile(htmlString); 
				if (baos == null) {
					baos = new ByteArrayOutputStream();
					model.addAttribute("error", "合同PDF生成失败。");
					return "/order/econtractTemplate/localRouteTemplateHtml";
				}
				long Dtime = System.currentTimeMillis() - Stime;
				LOG.info("downloadPdf==productId:"+productId + "createPdfFile==time" +Dtime);
				
		        response.setContentType("application/octet-stream");  
		        response.setHeader("Content-Disposition", "attachment;filename=ordRouteDown.pdf");  
				
				OutputStream out = response.getOutputStream();  
				out.write(baos.toByteArray());
				out.close();
				return null;
			}
			
		} catch (Exception e) {
			LOG.info("downloadPdf==productId"+productId,e);
			LOG.error("productId:"+ productId +ExceptionFormatUtil.getTrace(e));
		}
		return "/order/econtractTemplate/localRouteTemplateHtml";
	}
	
	/**
     * 预订须知中的推荐项目和购物说明
     */
    private void setRecommendItemAndShopping(List<ProdLineRouteVO> prodLineRouteVOList){
        if(CollectionUtils.isEmpty(prodLineRouteVOList))
            return;

        Long productId=0l;
        List<Long> lineRouteIds = new ArrayList<Long>();
        for(ProdLineRouteVO vo : prodLineRouteVOList){
            lineRouteIds.add(vo.getLineRouteId());
            productId=vo.getProductId();
        }

        //获取合同条款（推荐/购物）
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lineRouteIds", lineRouteIds);
        parameters.put("productId", productId);
        parameters.put("_orderby", "N_DAYS");
        parameters.put("_order", "ASC");
        ResultHandleT<List<ProdContractDetail>> contractListHandleT = prodContractDetailClientRemote.findFullContractListForWeb(parameters,null);
        if(null != contractListHandleT && CollectionUtils.isNotEmpty(contractListHandleT.getReturnContent())){
            //Map<行程ID，Map<购物/推荐，合同条款列表>>
            Map<Long,Map<String,List<ProdContractDetail>>> contractListMap = new HashMap<Long,Map<String,List<ProdContractDetail>>>();
            for(ProdContractDetail contract : contractListHandleT.getReturnContent()){
                if(!contractListMap.containsKey(contract.getLineRouteId())){
                    Map<String,List<ProdContractDetail>> contractDetailMap = new HashMap<String,List<ProdContractDetail>>();
                    List<ProdContractDetail> contractDetailList = new ArrayList<ProdContractDetail>();
                    contractDetailList.add(contract);
                    contractDetailMap.put(contract.getDetailType(),contractDetailList);
                    contractListMap.put(contract.getLineRouteId(),contractDetailMap);
                }else if(!contractListMap.get(contract.getLineRouteId()).containsKey(contract.getDetailType())){
                    Map<String,List<ProdContractDetail>> contractDetailMap = contractListMap.get(contract.getLineRouteId());
                    List<ProdContractDetail> contractDetailList = new ArrayList<ProdContractDetail>();
                    contractDetailList.add(contract);
                    contractDetailMap.put(contract.getDetailType(),contractDetailList);
                }else{
                    Map<String,List<ProdContractDetail>> contractDetailMap = contractListMap.get(contract.getLineRouteId());
                    List<ProdContractDetail> contractDetailList = contractDetailMap.get(contract.getDetailType());
                    contractDetailList.add(contract);
                }
            }

            //反向填充合同条款
            for(ProdLineRouteVO vo : prodLineRouteVOList){
            	//有关联的时候使用关联的行程id,否则使用本身的id
				Long lineRouteId = vo.getRefLineRouteId() != null ? vo.getRefLineRouteId() : vo.getLineRouteId();
                Map<String,List<ProdContractDetail>> contractDetailMap = contractListMap.get(lineRouteId);
                if(null != contractDetailMap && contractDetailMap.size() > 0) {
                    vo.setShopingList(contractDetailMap.get(ProdContractDetail.CONTRACT_DETAIL_TYPE.SHOPING.getCode().toString()));
                    vo.setRecommendList(contractDetailMap.get(ProdContractDetail.CONTRACT_DETAIL_TYPE.RECOMMEND.getCode().toString()));
                }
            }
        }

        
    }
	
	/**
     * 设置预订须知的公告
     * @param lineProductVO
     * @param productId
     */
    private void setNotice(Model model,Long productId,Map<String,Object> rootMap){
        Map<String, Object> paramProductNotice = new HashMap<String, Object>();
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date today = sFormat.parse(sFormat.format(new Date()));
            paramProductNotice.put("nowDate",today);
        } catch (ParseException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
        }
        paramProductNotice.put("productId", productId);
        paramProductNotice.put("cancelFlag","Y" );
        paramProductNotice.put("noticeType","PRODUCT_ALL" );
        paramProductNotice.put("_orderby", "CREATE_TIME DESC");

        List<ProdProductNotice> productNoticeList = null;
        ResultHandleT<List<ProdProductNotice>> noticeListHandleT = prodProductNoticeClientRemote.findProductNoticeList(paramProductNotice);
        if(noticeListHandleT != null && noticeListHandleT.getReturnContent() != null){
            productNoticeList = noticeListHandleT.getReturnContent();
            model.addAttribute("productNoticeList", filterProductNoticeListByTime(productNoticeList));
            rootMap.put("productNoticeList", filterProductNoticeListByTime(productNoticeList));
        }
    }
    
    /**
     * @param list
     * @return
     * 根据时间段进行过滤
     */
    private List<ProdProductNotice> filterProductNoticeListByTime(List<ProdProductNotice> list){
        List<ProdProductNotice>  retList = new ArrayList<ProdProductNotice>();
        if(list == null || list.size() <= 0){
            return retList;
        }
        Calendar today = Calendar.getInstance();
        Calendar beginDay = null;
        Calendar endDay = null;
        for(ProdProductNotice tmp : list){
            if(tmp.getStartTime() != null){
                beginDay = Calendar.getInstance();
                beginDay.setTime(tmp.getStartTime());
                beginDay.add(Calendar.DATE, -1);
                if(!today.after(beginDay)){
                    continue;
                }
            }

            if(tmp.getEndTime() != null){
                endDay = Calendar.getInstance();
                endDay.setTime(tmp.getEndTime());
                endDay.add(Calendar.DATE, 1);
                if(!endDay.after(today)){
                    continue;
                }
            }
            retList.add(tmp);
        }
        return retList;
    }
	
	/**根据合同ID获得合同主体
     * @param contractId
     * @return
     */
    private   SuppContract  getSuppContract(Long contractId){
    	SuppContract suppContract = null;
    	String key=MemcachedEnum.SupperContract.getKey()+contractId;
    	try{
    		suppContract=MemcachedUtil.getInstance().get(key);
    		if(suppContract!=null){
    			return suppContract;
    		}
    	}catch(Exception e){
    		LOG.error("get memcatched suppContract error contractId:"+contractId +e.getMessage()+"will be reload suppContract");
    	}
    	suppContract =prodProductClientRemote.findSuppContractById(contractId);
    	if(suppContract!=null){
    		MemcachedUtil.getInstance().set(key, MemcachedEnum.SupperContract.getSec(), suppContract);
    	}
    	return suppContract;
    }
	
	public static String getTextFromHtml(String htmlStr){  
        htmlStr = delHTMLTag(htmlStr);  
        htmlStr = htmlStr.replaceAll(" ", "");  
        return htmlStr;  
    }
	
	/** 
     * @param htmlStr 
     * @return 
     *  删除Html标签 
     */  
    public static String delHTMLTag(String htmlStr) {  
  
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);  
        Matcher m_style = p_style.matcher(htmlStr);  
        htmlStr = m_style.replaceAll(""); // 过滤style标签  
  
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);  
        Matcher m_html = p_html.matcher(htmlStr);  
        htmlStr = m_html.replaceAll(""); // 过滤html标签  
  
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);  
        Matcher m_space = p_space.matcher(htmlStr);  
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签  
        return htmlStr.trim(); // 返回文本字符串  
    }
	
	/**
     * 供应商打包并且含有交通信息的
     */
    private void setTrafficInfo(ProdProduct prodProduct,Model model,Map<String,Object> rootMap){
        List<Map<String,Object>> trafficList = new ArrayList<Map<String, Object>>();
        ProdTrafficVO trafficVO = prodTrafficClientServiceRemote.getProdTrafficVOByProductId(prodProduct.getProductId());
        if(trafficVO == null){
            return;
        }
        ProdTraffic prodTraffic = trafficVO.getProdTraffic();//交通信息表
        List<ProdTrafficGroup> prodTrafficGroupList = trafficVO.getProdTrafficGroupList();
        if(prodTrafficGroupList != null && prodTrafficGroupList.size() > 0){
            int titleIndex = 1;
            for(ProdTrafficGroup prodTrafficGroup : prodTrafficGroupList){
            	int trafficeDetailCount = 0;//有参考信息明细的总数，去程返程最多两个，两个都没有的前台不显示
                Map<String,Object> map = new HashMap<String, Object>();
                //去程的map
                Map<String,List<TrafficForRouteDetailVO>> toMap = new HashMap<String,List<TrafficForRouteDetailVO>>();
                //返程的map
                Map<String,List<TrafficForRouteDetailVO>> backMap = new HashMap<String,List<TrafficForRouteDetailVO>>();
                String toType = prodTraffic.getToType();
                String backType = prodTraffic.getBackType();
                List<TrafficForRouteDetailVO> toTrafficForRouteDetailVOList = new ArrayList<TrafficForRouteDetailVO>();
                List<TrafficForRouteDetailVO> backTrafficForRouteDetailVOList = new ArrayList<TrafficForRouteDetailVO>();
                toMap.put("toList",toTrafficForRouteDetailVOList);
                backMap.put("backList",backTrafficForRouteDetailVOList);
                map.put("toMap",toMap);
                map.put("toType",prodTraffic.getToType());
                map.put("backType",prodTraffic.getBackType());
                map.put("backMap",backMap);

                long toTotalHover = 0;//去程的小时数
                long backTotalHover = 0;//返程的小时数
                //如果是飞机
                if("FLIGHT".equalsIgnoreCase(toType) || "FLIGHT".equalsIgnoreCase(backType)){
                    List<ProdTrafficFlight> prodTrafficFlightList = prodTrafficGroup.getProdTrafficFlightList();
                    if(prodTrafficFlightList != null && prodTrafficFlightList.size() > 0){
                        int toCount = 0;
                        int backCount = 0;

                        for(ProdTrafficFlight prodTrafficFlight : prodTrafficFlightList){
                            BizFlight bizFlight = prodTrafficFlight.getBizFlight();//得到飞机的字典表里的信息也就是航班的信息
                            if(bizFlight != null){
                            	ProdTrafficFlight ptf = prodTrafficClientServiceRemote.selectByPrimaryKey(bizFlight.getFlightId());
                            	trafficeDetailCount ++;
                                TrafficForRouteDetailVO trafficForRouteDetailVO = new TrafficForRouteDetailVO();
                                //得到航空公司
                                Long dictId = bizFlight.getAirline();
                                if(dictId != null){
                                    ResultHandleT<BizDict> bizDictObject= dictClientRemote.findDictById(dictId);
                                    if(bizDictObject.getReturnContent() != null){
                                        String lineCompanyName = bizDictObject.getReturnContent().getDictName();
                                        bizFlight.setAirlineString(lineCompanyName);
                                    }
                                }
                                //是否经停
                                if(bizFlight.getStopCount() != null && bizFlight.getStopCount() > 0){
                                    trafficForRouteDetailVO.setStopFlag("Y");
                                }

                                if("TO".equals(prodTrafficFlight.getTripType())){
                                    trafficForRouteDetailVO.setBelongToCompany(bizFlight.getAirlineString());
                                    trafficForRouteDetailVO.setNumber(bizFlight.getFlightNo());
                                    trafficForRouteDetailVO.setStartTime(bizFlight.getStartTime());
                                    trafficForRouteDetailVO.setEndTime(bizFlight.getArriveTime());
                                    trafficForRouteDetailVO.setStartAirport(bizFlight.getStartAirportString());
                                    trafficForRouteDetailVO.setArriveAirport(bizFlight.getArriveAirportString());
                                    trafficForRouteDetailVO.setFromDistrict(bizFlight.getStartDistrictString());
                                    trafficForRouteDetailVO.setToDistrict(bizFlight.getArriveDistrictString());
                                    trafficForRouteDetailVO.setCabin(ptf.getCabin());

                                    if(toCount > 0 && toTrafficForRouteDetailVOList.size() > 0){
                                        TrafficForRouteDetailVO trafficForRouteDetailVOStop = new TrafficForRouteDetailVO();
                                        TrafficForRouteDetailVO lastTrafficForRouteDetailVO = toTrafficForRouteDetailVOList.get(toTrafficForRouteDetailVOList.size()-1);
                                        trafficForRouteDetailVOStop.setChangeTrains(lastTrafficForRouteDetailVO.getArriveAirport());
                                        trafficForRouteDetailVOStop.setChangeflag("Y");
                                        //是否经停
                                        trafficForRouteDetailVOStop.setStopFlag(lastTrafficForRouteDetailVO.getStopFlag());

                                        toTrafficForRouteDetailVOList.add(trafficForRouteDetailVOStop);
                                    }

                                    //计算路程用的时间
                                    if(bizFlight.getFlightTime() != null){
                                        toTotalHover += bizFlight.getFlightTime();
                                    }
                                    toTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                                    toCount++;
                                }else if("BACK".equals(prodTrafficFlight.getTripType())){
                                    trafficForRouteDetailVO.setBelongToCompany(bizFlight.getAirlineString());
                                    trafficForRouteDetailVO.setNumber(bizFlight.getFlightNo());
                                    trafficForRouteDetailVO.setStartTime(bizFlight.getStartTime());
                                    trafficForRouteDetailVO.setEndTime(bizFlight.getArriveTime());
                                    trafficForRouteDetailVO.setStartAirport(bizFlight.getStartAirportString());
                                    trafficForRouteDetailVO.setArriveAirport(bizFlight.getArriveAirportString());
                                    trafficForRouteDetailVO.setFromDistrict(bizFlight.getStartDistrictString());
                                    trafficForRouteDetailVO.setToDistrict(bizFlight.getArriveDistrictString());
                                    trafficForRouteDetailVO.setCabin(ptf.getCabin());
                                    
                                    if(backCount > 0 && backTrafficForRouteDetailVOList.size() > 0){
                                        TrafficForRouteDetailVO trafficForRouteDetailVOStop = new TrafficForRouteDetailVO();
                                        TrafficForRouteDetailVO lastTrafficForRouteDetailVO = backTrafficForRouteDetailVOList.get(backTrafficForRouteDetailVOList.size()-1);
                                        trafficForRouteDetailVOStop.setChangeTrains(lastTrafficForRouteDetailVO.getArriveAirport());
                                        trafficForRouteDetailVOStop.setChangeflag("Y");
                                        backTrafficForRouteDetailVOList.add(trafficForRouteDetailVOStop);
                                    }

                                    //计算路程用的时间
                                    if(bizFlight.getFlightTime() != null){
                                        backTotalHover += bizFlight.getFlightTime();
                                    }
                                    backTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                                    backCount++;
                                }
                            }
                        }

                    }
                }
                if("TRAIN".equalsIgnoreCase(toType) || "TRAIN".equalsIgnoreCase(backType)){
                    List<ProdTrafficTrain> prodTrafficTrainList = prodTrafficGroup.getProdTrafficTrainList();
                    if(prodTrafficTrainList != null && prodTrafficTrainList.size() > 0){
                        for(ProdTrafficTrain prodTrafficTrain : prodTrafficTrainList){
                            BizTrain bizTrain = prodTrafficTrain.getBizTrain();
                            if(bizTrain != null){
                            	trafficeDetailCount ++;
                                int toCount = 0;
                                int backCount = 0;
                                TrafficForRouteDetailVO trafficForRouteDetailVO = new TrafficForRouteDetailVO();
                                BizTrainStop bizTrainStopStart = bizTrain.getStartTrainStop();
                                BizTrainStop bizTrainStopArrive = bizTrain.getArriveTrainStop();
                                String startTime = null;
                                String arriveTime = null;
                                if(bizTrainStopStart != null){
                                    startTime = bizTrainStopStart.getDepartureTime();
                                }
                                if(bizTrainStopArrive != null){
                                    arriveTime = bizTrainStopArrive.getArrivalTime();
                                }

                                //是否经停
                                if(bizTrain.getStopCount() != null && bizTrain.getStopCount() > 0){
                                    trafficForRouteDetailVO.setStopFlag("Y");
                                }
                                if(startTime != null && !"".equals(startTime)){
                                    if(startTime.length() == 4 && startTime.indexOf(":") < 0){
                                        startTime = startTime.substring(0,2)+":"+startTime.substring(2,4);
                                    }else if(startTime.length() == 3 && startTime.indexOf(":") < 0){
                                        startTime = startTime.substring(0,1)+":"+startTime.substring(1,3);
                                    }
                                    trafficForRouteDetailVO.setStartTime(startTime);
                                }
                                if(arriveTime != null && !"".equals(arriveTime)){
                                    if(arriveTime.length() == 4 && arriveTime.indexOf(":") < 0){
                                        arriveTime = arriveTime.substring(0,2)+":"+arriveTime.substring(2,4);
                                    }else if(arriveTime.length() == 3  && arriveTime.indexOf(":") < 0){
                                        arriveTime = arriveTime.substring(0,1)+":"+arriveTime.substring(1,3);
                                    }
                                    trafficForRouteDetailVO.setEndTime(arriveTime);

                                }
                                if(bizTrain.getTrainSeatList() != null && bizTrain.getTrainSeatList().size() > 0){
                                	for(BizTrainSeat seat:bizTrain.getTrainSeatList()){
                                		if(prodTrafficTrain.getTrainSeatId().longValue() == seat.getTrainSeatId().longValue()){
                                			trafficForRouteDetailVO.setSeatType(seat.getSeatType());
                                		}
                                	}
                                }
                                if(bizTrain.getCostTime() != null){
                                	trafficForRouteDetailVO.setCostTime("约"+bizTrain.getCostTime()+"小时");
                                }
                                
                                trafficForRouteDetailVO.setNumber(bizTrain.getTrainNo());
                                trafficForRouteDetailVO.setFromDistrict(prodTrafficTrain.getStartDistrictString());
                                trafficForRouteDetailVO.setToDistrict(prodTrafficTrain.getEndDistrictString());
                                if("TO".equals(prodTrafficTrain.getTripType())){
                                    if(toCount > 0 && toTrafficForRouteDetailVOList.size() > 0){
                                        TrafficForRouteDetailVO trafficForRouteDetailVOStop = new TrafficForRouteDetailVO();
                                        TrafficForRouteDetailVO lastTrafficForRouteDetailVO = toTrafficForRouteDetailVOList.get(toTrafficForRouteDetailVOList.size()-1);
                                        trafficForRouteDetailVOStop.setChangeTrains(lastTrafficForRouteDetailVO.getArriveAirport());
                                        trafficForRouteDetailVOStop.setChangeflag("Y");
                                        toTrafficForRouteDetailVOList.add(trafficForRouteDetailVOStop);
                                    }
                                    if(bizTrain.getCostTime() != null){
                                        toTotalHover += bizTrain.getCostTime();
                                    }
                                    toTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                                    toCount++;
                                }else if("BACK".equals(prodTrafficTrain.getTripType())){
                                    if(backCount > 0 && backTrafficForRouteDetailVOList.size() > 0){
                                        TrafficForRouteDetailVO trafficForRouteDetailVOStop = new TrafficForRouteDetailVO();
                                        TrafficForRouteDetailVO lastTrafficForRouteDetailVO = backTrafficForRouteDetailVOList.get(backTrafficForRouteDetailVOList.size()-1);
                                        trafficForRouteDetailVOStop.setChangeTrains(lastTrafficForRouteDetailVO.getArriveAirport());
                                        trafficForRouteDetailVOStop.setChangeflag("Y");
                                        backTrafficForRouteDetailVOList.add(trafficForRouteDetailVOStop);
                                    }
                                    if(bizTrain.getCostTime() != null) {
                                        backTotalHover += bizTrain.getCostTime();
                                    }
                                    backTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                                    backCount++;
                                }
                            }
                        }
                    }
                }
                if("BUS".equalsIgnoreCase(toType) || "BUS".equalsIgnoreCase(backType)){
                    List<ProdTrafficBus> prodTrafficBusList = prodTrafficGroup.getProdTrafficBusList();
                    if(prodTrafficBusList != null && prodTrafficBusList.size() > 0){
                    	trafficeDetailCount ++;
                        for(ProdTrafficBus prodTrafficBus : prodTrafficBusList){
                            TrafficForRouteDetailVO trafficForRouteDetailVO = new TrafficForRouteDetailVO();
                            if("TO".equals(prodTrafficBus.getTripType())){
                                trafficForRouteDetailVO.setAddress(prodTrafficBus.getAdress());
                                trafficForRouteDetailVO.setStartTime(prodTrafficBus.getStartTime());
                                toTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                            }else if("BACK".equals(prodTrafficBus.getTripType())){
                                trafficForRouteDetailVO.setAddress(prodTrafficBus.getAdress());
                                trafficForRouteDetailVO.setStartTime(prodTrafficBus.getStartTime());
                                backTrafficForRouteDetailVOList.add(trafficForRouteDetailVO);
                            }
                        }
                    }
                }
                map.put("toTotalHour",generateHourStr(toTotalHover));//去程的小时数
                map.put("backTotalHour",generateHourStr(backTotalHover));//返程的小时数
                if (trafficeDetailCount > 0) {
                	map.put("titleIndex",titleIndex);
                    titleIndex++;
                    trafficList.add(map);
                }
            }
        }
        model.addAttribute("trafficList", trafficList);
        rootMap.put("trafficList", trafficList);
        if(prodTraffic != null && prodTraffic.getReferFlag() != null && prodTraffic.getReferFlag().equalsIgnoreCase("Y")
        		&& trafficList != null && trafficList.size() > 0){
            model.addAttribute("REFER_FLAG","Y");
            rootMap.put("REFER_FLAG","Y");
        }

        List<String> titleList = new ArrayList<String>();
        //交通title的处理
        if(trafficList != null && trafficList.size() > 0){
            for(int i = 0 ; i < trafficList.size(); i++){
                titleList.add(Long.toString(i+1));
            }
        }
        model.addAttribute("titleList",titleList);
        rootMap.put("titleList",titleList);
    }
    
    /**
     * 生成运行时间的字符串
     * @return
     */
    protected String generateHourStr(Long toTotalHour){
        String toTotalHourStr = String.valueOf(toTotalHour / 60L);
        String toTotalTimeMinuteStr = String.valueOf(toTotalHour % 60L);
        StringBuffer sb = new StringBuffer("");
        if(toTotalHourStr != null && !"0".equals(toTotalHourStr)){
            sb.append("约"+toTotalHourStr+"h");
            if(toTotalTimeMinuteStr != null && !"0".equals(toTotalTimeMinuteStr)){
                sb.append(toTotalTimeMinuteStr+"m");
            }
        }else if(toTotalTimeMinuteStr != null && !"0".equals(toTotalTimeMinuteStr)){
            sb.append("约"+toTotalTimeMinuteStr+"m");
        }
        return sb.toString();
    }
	
	 /**
     * 获取行程出行日期列表
     * @param productId
     * @param prodLineRouteVOList
     */
    private void setProdGroupDateList(Long productId,List<ProdLineRouteVO> prodLineRouteVOList,Long startDistrictId,Model model){

        if(null == prodLineRouteVOList)
            return;

        //出行日期列表
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("productId",productId);
        params.put("beginDate",new Date());
        //设置检查参数
        params.put("checkAhead",true);
        params.put("startDistrictId",startDistrictId);
        ResultHandleT<List<ProdGroupDate>> prodGroupDateListHandleT = prodGroupDateClientService.findProdGroupDateListByParam(productId, params);
        if(null != prodGroupDateListHandleT && null != prodGroupDateListHandleT.getReturnContent()){
            //<行程ID，出行时间组>
            Map<Long,List<ProdGroupDate>> dateMap = new HashMap<Long,List<ProdGroupDate>>();
            List<ProdGroupDate> prodGroupDateList = prodGroupDateListHandleT.getReturnContent();
            if(prodGroupDateListHandleT.getReturnContent().size()>0){
//				model.addAttribute("showStartMonth", new SimpleDateFormat("yyyy-MM-dd").format(prodGroupDateList.get(0).getSpecDate()));//供前端自由行日历使用
//				model.addAttribute("showEndMonth", new SimpleDateFormat("yyyy-MM-dd").format(prodGroupDateList.get(prodGroupDateList.size()-1).getSpecDate()));//供前端自由行日历使用
            }
            for(ProdGroupDate prodGroupDate : prodGroupDateList){
                if(dateMap.containsKey(prodGroupDate.getLineRouteId())){
                    List<ProdGroupDate> value = dateMap.get(prodGroupDate.getLineRouteId());
                    value.add(prodGroupDate);
                }else{
                    List<ProdGroupDate> value = new ArrayList<ProdGroupDate>();
                    value.add(prodGroupDate);
                    dateMap.put(prodGroupDate.getLineRouteId(),value);
                }
            }

            //反向填充形成列表中的中的出行日期
            if(null != prodLineRouteVOList){
                for(ProdLineRouteVO vo : prodLineRouteVOList){
                    vo.setProdGroupDateList(dateMap.get(vo.getLineRouteId()));
                }
            }
        }
        
    }
	
	 /**
     * <p>二维码生成</p>
     * @param productId 产品ID
     * @param startDistrictId 多出发地ID
     * @return String 二维码路径
     */
    private String excuteQRDataFileByPhoneMP(Long productId,Long startDistrictId){
        String url = null;
        if(startDistrictId!=null){
        	url = "http://dujia.lvmama.com/group/showLineQr/"+productId+"-D"+startDistrictId;
        }else{
        	url = "http://dujia.lvmama.com/group/showLineQr/"+productId;
        }
        return url;
    }
	
	/**
	 * <p>根据行程ID过滤列表选择当前行程</p>
	 * @User : ZM
	 * @Date : 2016年1月20日下午4:01:09
	 * @param product
	 * @param routeId void
	 */
	private void findRouteByRouteId(ProdProduct product, Long routeId) {
		ResultHandleT<List<ProdLineRoute>> lineRouteResults = null;
		List<ProdLineRouteVO>listTemp = new ArrayList<ProdLineRouteVO>();
		ProdLineRoute lineRouteResult = new ProdLineRoute();
		try {
			lineRouteResults = this.prodLineRouteClientService.findCacheLineRouteListByProductId(product.getProductId());
			if(CollectionUtils.isNotEmpty(lineRouteResults.getReturnContent())){
				for (ProdLineRoute lineRoute : lineRouteResults.getReturnContent()) {
					if(routeId.equals(lineRoute.getLineRouteId())){
						lineRouteResult = lineRoute;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("找不到对应的行程，行程ID：" + routeId+ "，异常信息： {}", e);
		}
		
		ProdLineRoute lineRoute = BuildRouteTemplateUtil.buildTemplate(lineRouteResult);
		ProdLineRouteVO prodLineRouteVO = new ProdLineRouteVO();
        BeanUtils.copyProperties(lineRoute,prodLineRouteVO);
        listTemp.add(prodLineRouteVO);
		product.setProdLineRouteList(listTemp);
	}
	
	
	protected File initDirectory() {
		if (isDubgPdf) {   
			 return new File("D:/Ted/workspace/vst_order/src/main/webapp/WEB-INF/resources/econtractTemplate/");
		}
		return ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
	}
	
	protected Configuration initConfiguration(File directioryFile) throws IOException {
		Configuration configuration = null;
		
		if(directioryFile != null && directioryFile.exists()){
			configuration  =new Configuration();
			configuration.setDefaultEncoding("UTF-8");
			configuration.setOutputEncoding("UTF-8");
			configuration.setNumberFormat("###");
			configuration.setClassicCompatible(true);
			configuration.setDirectoryForTemplateLoading(directioryFile);
		}
		
		return configuration;
	}
	
	
	
}
