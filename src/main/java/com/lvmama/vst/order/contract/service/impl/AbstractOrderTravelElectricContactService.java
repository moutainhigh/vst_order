package com.lvmama.vst.order.contract.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import com.lvmama.commons.logging.LvmamaLog;
import com.lvmama.commons.logging.LvmamaLogFactory;
import com.lvmama.vst.back.dujia.client.comm.prod.service.ProdProductDescriptionClientService;
import com.lvmama.vst.back.dujia.comm.prod.po.ProdProductDescription;
import com.lvmama.vst.comlog.LvmmLogEnum;
import com.lvmama.vst.order.contract.service.OrderTravelElectricContactMailService;
import com.lvmama.vst.order.tnt.contract.service.DesignatedFreetourOrderService;
import com.lvmama.vst.order.tnt.utils.TntOrderUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.service.LineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdContractDetailClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.COMPANY_TYPE_DIC;
import com.lvmama.vst.back.prod.po.ProdProductAssociation;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
//import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.dao.ComFileMapDAO;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderUpdateService;
//import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 *
 * @author Jesley.Sun
 *
 */
public abstract class AbstractOrderTravelElectricContactService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractOrderTravelElectricContactService.class);
	//新加事件日志
	private static final LvmamaLog lvmamaLog = LvmamaLogFactory.getLog(AbstractOrderTravelElectricContactService.class);

	protected static boolean  isDubgPdf = false;//开发的时候设置为true，上线设置为false


	public static final String TRAVEL_ECONTRACT_DIRECTORY = "/WEB-INF/resources/econtractTemplate";

//	public static final String TRAVEL_ECONTRACT_DIRECTORY = "/econtractTemplate";

	private static final String SERVER_TYPE = "COM_AFFIX";

	@Autowired
	protected ComFileMapDAO comFileMapDAO;

	@Autowired
	private LineRouteClientService lineRouteClientService;

	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;

	@Autowired
	private IOrdTravelContractService ordTravelContractService;

	/*@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;*/

	@Resource(name="orderTravelElectricContactService")
	private IOrderElectricContactService orderTravelElectricContactService;


	@Resource(name="travelItineraryContractService")
	private IOrderElectricContactService travelItineraryContractService;

	@Autowired
	protected FSClient fsClient;


	/*@Autowired
	private LvmmLogClientService lvmmLogClientService;*/

	@Autowired
	private IOrderUpdateService ordOrderUpdateService;

	@Autowired
	private IOrdItemContractRelationService ordItemContractRelationService;


	@Autowired
	private CategoryClientService categoryClientService;


	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private ProdProductClientService prodProductClientService;

	@Autowired
	private ProdLineRouteClientService prodLineRouteClientService;

	@Autowired
	private ProdContractDetailClientService prodContractDetailClientService;

	@Autowired
	private ProdProductDescriptionClientService prodProductDescriptionClientService;

	@Resource
	private DesignatedFreetourOrderService designatedFreetourOrderService;

	@Resource
	private OrderTravelElectricContactMailService orderTravelElectricContactMailService;

	protected static HashMap<String, String> filialeNameMap=new HashMap<String, String>();

	protected static HashMap<String, String> jianduTelMap=new HashMap<String, String>();//投诉电话

	protected static HashMap<String, String> permitlMap=new HashMap<String, String>();//许可证编号


	protected static HashMap<String, String> provinceMap=new HashMap<String, String>();//省

	protected static HashMap<String, String> cityMap=new HashMap<String, String>();//市

	protected static HashMap<String, String> lvTSTelephoneMap=new HashMap<String, String>();//投诉电话

	protected static HashMap<String, String> lvAddressMap=new HashMap<String, String>();//地址

	protected static HashMap<String, String> lvpostcodeMap=new HashMap<String, String>();//邮编

	protected static HashMap<String, String> businessScopeMap=new HashMap<String, String>();//经营范围

	protected static HashMap<String, String> businessAddressMap=new HashMap<String, String>();//合同中营业地址

	protected static HashMap<String, String> businessPostCodeMap=new HashMap<String, String>();//营业邮编


	static{

		filialeNameMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "上海驴妈妈兴旅国际旅行社有限公司");
		filialeNameMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "上海驴妈妈兴旅国际旅行社有限公司北京分社");
		filialeNameMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "上海驴妈妈兴旅国际旅行社有限公司成都分社");
		filialeNameMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "上海驴妈妈兴旅国际旅行社有限公司广州分公司");
		//filialeNameMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "上海驴妈妈国际旅行社有限公司三亚分公司");
		//三亚分公司用上海驴妈妈兴旅国际旅行社有限公司的内容
		filialeNameMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "上海驴妈妈兴旅国际旅行社有限公司");

		jianduTelMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "021-64393615、962020");
		jianduTelMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "010-85157015、010-65158249");
		jianduTelMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "028-96527、96927");
		jianduTelMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "12345");
		//三亚分公司用上海驴妈妈兴旅国际旅行社有限公司的内容
		jianduTelMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "021-64393615");

		permitlMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "L-SH-CJ00056");
		permitlMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "L-SH-CJ00056-BJF-CY0057");
		permitlMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "L-SH-CJ00056-A-fs-001");
		permitlMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "L-SH-CJ00056-YXFS001");
		//permitlMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "L-SH-00284-FS-SY001");
		//三亚分公司用上海驴妈妈兴旅国际旅行社有限公司的内容
		permitlMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "L-SH-CJ00056");


		provinceMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "/");
		provinceMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "/");
		provinceMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "广东");
		provinceMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "四川");

		cityMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "上海");
		cityMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "北京");
		cityMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "广州");
		cityMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "成都");

		lvTSTelephoneMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "021-64393615、962020");
		lvTSTelephoneMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "010-85157015、010-65158249");
		lvTSTelephoneMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "12345");
		lvTSTelephoneMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "028-96527、96927");

		lvAddressMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "上海中山南二路 2419 号 B1 楼");
		lvAddressMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "中国北京市朝阳区建国门外大街28号");
		lvAddressMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "广州市东风西路140号东方金融大厦13-15楼");
		lvAddressMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "成都市金牛区二环路北一段4号19楼");

		lvpostcodeMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "201803");
		lvpostcodeMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "100027");
		lvpostcodeMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "/");
		lvpostcodeMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "510170");

		//营业地址
		businessAddressMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "上海市嘉定区景域大道88号驴妈妈科技园");
		businessAddressMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "北京市朝阳区新源里16号琨莎中心A座810室");
		businessAddressMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "广州市越秀区东风中路363号国信大厦2703");
		businessAddressMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "四川省成都市高新区高升桥东路1号长城金融大厦3楼");
		businessAddressMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "上海市嘉定区景域大道88号驴妈妈科技园");
		//营业邮编
		businessPostCodeMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "201803");
		businessPostCodeMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "100027");
		businessPostCodeMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "510045");
		businessPostCodeMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "610041");
		businessPostCodeMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "201803");
		//经营范围
		businessScopeMap.put(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode(), "入境旅游业务、国内旅游业务、出境旅游业务，国际航线或者香港、澳门、台湾地区航线的航空客运销售代理业务，国内航线除香港、澳门、台湾地区航线外的航空客运销售代理业务，销售旅游用品、工艺品，从事旅游领域内的技术咨询、技术开发，会务服务，展览展示服务。");
		businessScopeMap.put(CommEnumSet.FILIALE_NAME.BJ_FILIALE.getCode(), "国内旅游、入境旅游和出境旅游招徕、组织、接待业务。");
		businessScopeMap.put(CommEnumSet.FILIALE_NAME.CD_FILIALE.getCode(), "入境旅游业务、国内旅游业务、出境旅游业务。");
		businessScopeMap.put(CommEnumSet.FILIALE_NAME.GZ_FILIALE.getCode(), "向游客提供旅游、交通、住宿、餐饮等代理服务（不涉及旅行社业务）;会议及展览服务;入境旅游业务;境内旅游业务;出境旅游业务。");
		//businessScopeMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "入境旅游业务、国内旅游业务、出境旅游业务,招徕、组织，接待业务。");
		//三亚分公司用上海驴妈妈兴旅国际旅行社有限公司的内容
		businessScopeMap.put(CommEnumSet.FILIALE_NAME.SY_FILIALE.getCode(), "入境旅游业务、国内旅游业务、出境旅游业务，国际航线或者香港、澳门、台湾地区航线的航空客运销售代理业务，国内航线除香港、澳门、台湾地区航线外的航空客运销售代理业务，销售旅游用品、工艺品，从事旅游领域内的技术咨询、技术开发，会务服务，展览展示服务。");
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

	protected File initDirectory() {
		if (isDubgPdf) {
			 return new File("D:/Ted/workspace/vst_order/src/main/webapp/WEB-INF/resources/econtractTemplate/");
		}
		return ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
	}

	/**
	 * 上传附件
	 *
	 * @param fileName
	 * @param directioryFile
	 * @return
	 */
	protected ResultHandleT<ComFileMap> saveOrUpdateCommonFile(String fileName, File directioryFile) {
		ResultHandleT<ComFileMap> handleT = new ResultHandleT<ComFileMap>();
		ComFileMap comFileMap = comFileMapDAO.getByFileName(fileName);
		if (comFileMap == null) {
			try {
				FileInputStream fis = new FileInputStream(new File(directioryFile, fileName));
				Long fileId = fsClient.uploadFile(fileName, fis, SERVER_TYPE);
				fis.close();
				if (fileId != null && fileId != 0) {
					comFileMap = new ComFileMap();
					comFileMap.setFileName(fileName);
					comFileMap.setFileId(fileId);
					comFileMap.setCreateTime(new Date());

					if (comFileMapDAO.insert(comFileMap) == 1) {
						handleT.setReturnContent(comFileMap);
					} else {
						handleT.setMsg("文件" + directioryFile + "/" + fileName + "生成ComFileMap失败。");
					}
				} else {
					handleT.setMsg("文件" + directioryFile + "/" + fileName + "文件上传失败。");
				}
			} catch  (FileNotFoundException e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				handleT.setMsg("文件" + directioryFile + "/" + fileName + "找不到");
			} catch (IOException e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				handleT.setMsg("流关闭出错。");
			}
		} else {
			handleT.setReturnContent(comFileMap);
		}

		return handleT;
	}


	/**
	 * 获取公司专用章
	 *
	 * @param filialeName
	 * @return
	 */
	protected String getStampImageNameByFilialeName(String filialeName) {
		String StampImageName = "SH_ECONTRACT.png";
		if (CommEnumSet.FILIALE_NAME.SH_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "SH_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.BJ_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "BJ_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.GZ_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "GZ_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.CD_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "SC_ECONTRACT.png";
		}

		return StampImageName;
	}

	protected String getAppendVersion(OrdTravelContract ordTravelContract) {
		String appendVersion="A";
		if (ordTravelContract != null && ordTravelContract.getVersion() != null) {
			String oldVersion = ordTravelContract.getVersion();
			char oldAppendVersion = oldVersion.charAt(oldVersion.length() - 1);
			oldAppendVersion++;
			if (oldAppendVersion > 'A' && oldAppendVersion < 'Z') {
				appendVersion = oldAppendVersion + "";
			}
		}
		return appendVersion;
	}

	protected Map<String,List<OrderMonitorRst>>  findChildOrderList(OrdTravelContract ordTravelContract,OrdOrder order,boolean removeRoteLocal) {

		OrdOrderItem orderContractItem = this.getOrderContractItem(ordTravelContract, order);//获取当前子订单

		boolean relatedMarketingFlag = isExsitLocalRouteItemInOrder(order);//判断订单是否有"关联销售当地游"订单

		boolean orderItemLocalRouteFlag = isLocalRouteOrderItem(orderContractItem);//该子订单是否是关联当地游

		boolean packageTour_outbound=ispackageTourOutbound(order);//判断是否为出境跟团游打包签证

		//子订单列表展示
		Map<String,List<OrderMonitorRst>> resultMap=new HashMap<String,List<OrderMonitorRst>>();
		List<OrdOrderItem> ordItemsList =order.getOrderItemList();
		Long orderItemId = orderContractItem.getOrderItemId();//当前合同的对应的子订单ID
		for (OrdOrderItem ordOrderItem : ordItemsList) {

			if(relatedMarketingFlag == Boolean.TRUE){//判断该订单中是否有关联销售当地游

				//判断该子订单是否是当地游；
				if(orderItemLocalRouteFlag == Boolean.TRUE){//只显示当地游
					if(!orderItemId.equals(ordOrderItem.getOrderItemId())){//跳过其他子订单
						continue;
					}

				}else{//剔除当地游子订单
					boolean flag  = isLocalRouteOrderItem(ordOrderItem);//判断该子订单是否为子订单
					if(flag && !orderItemId.equals(ordOrderItem.getOrderItemId())){
						continue;
					}

				}

			}
			if(packageTour_outbound){
				//判断为出境跟团游打包签证
				//判断是否为跟团游
				if(ordOrderItem.getCategoryId()!=null&&ordOrderItem.getCategoryId()==15&&CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(ordOrderItem.getBuCode())){
					//剔除跟团游的价格
					Long totalAmount = ordOrderItem.getTotalAmount();
					continue;
				}
			}

			String categoryId=ordOrderItem.getCategoryId()+"";
			List<OrderMonitorRst> childOrderResultList =  null;
			if(!resultMap.containsKey(categoryId)){
				childOrderResultList =  new ArrayList<OrderMonitorRst>();
				resultMap.put(categoryId, childOrderResultList);
			}else{
				childOrderResultList=resultMap.get(categoryId);
			}

			Map<String,Object> contentMap = ordOrderItem.getContentMap();
			String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());

			//ResultHandleT<ProdProduct> result=prodProductClientService.findProdProductById(ordOrderItem.getProductId());
			ResultHandleT<ProdProduct> result=prodProductClientService.findProdProductByIdFromCache(ordOrderItem.getProductId());

			ProdProduct prodProduct=result.getReturnContent();
			if (removeRoteLocal&& BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryType)
					&& (ProdProduct.PRODUCTTYPE.INNERLINE.getCode().equals(prodProduct.getProductType())
							|| ProdProduct.PRODUCTTYPE.INNERSHORTLINE.getCode().equals(prodProduct.getProductType())
							|| ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType()))) {
				continue;
			}

			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
//				orderMonitorRst.setPrincipal(principal);//负责人
			orderMonitorRst.setOrderId(ordOrderItem.getOrderItemId());
			orderMonitorRst.setChildOrderType(categoryType);
			orderMonitorRst.setChildOrderTypeName(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCnName(categoryType));
			orderMonitorRst.setProductName(this.buildProductName(ordOrderItem));
			orderMonitorRst.setBuyCount(ordOrderItem.getQuantity().intValue());
			orderMonitorRst.setVisitTime(DateUtil.formatDate(ordOrderItem.getVisitTime(), "yyyy-MM-dd"));
			orderMonitorRst.setPrice(ordOrderItem.getPriceYuan());
			Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
			paramOrdItemPersonRelation.put("orderItemId", ordOrderItem.getOrderItemId());
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);

			orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());

			childOrderResultList.add(orderMonitorRst);

		}

		return resultMap;
	}

	public boolean ispackageTourOutbound(OrdOrder order) {

		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		boolean isoutBound_packageTour=false;
		boolean contantVisaItem=false;
		List<OrdOrderItem> viasOrderItem=new ArrayList<>();
		for(OrdOrderItem item : orderItemList){
			boolean ismainItem = item.getMainItem().equals("true");
			if(ismainItem){
				if(item.getCategoryId()!=null&&item.getCategoryId()==15&& CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(item.getBuCode())){
					//为出境跟团游
					isoutBound_packageTour=true;

				}
			}else if(item.getCategoryId()!=null&&item.getCategoryId()==4){
				contantVisaItem=true;
				viasOrderItem.add(item);
			}
		}

		if(isoutBound_packageTour&&contantVisaItem){
			return true;
		}else{
			return false;
		}
	}

	public boolean isParentageOrder(OrdOrder order) {
		boolean isParentageFlag=false;
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())) {
			for(OrdOrderItem item : orderItemList) {
				if ("true".equals(item.getMainItem()) && item.hasContentValue(
						OrderEnum.ORDER_ROUTE_TYPE.group_mode.name(), CommEnumSet.GROUP_MODE.PARENTAGE_GROUP.getCode())) {
					isParentageFlag = true;
				}

			}
		}
		return isParentageFlag;
	}

	protected boolean isLocalRouteOrderItem(OrdOrderItem orderContractItem) {//该子订单是否是关联当地游
		boolean orderItemLocalRouteFlag = false;
		if(orderContractItem.getContent() != null  && orderContractItem.getContent().length()>0){
			String relatedMarketingFlagStr = (String)orderContractItem.getContentValueByKey("relatedMarketingFlag");
			if(relatedMarketingFlagStr != null && "localRoute".equals(relatedMarketingFlagStr)){
				orderItemLocalRouteFlag = true;
			}
		}
		return orderItemLocalRouteFlag;
	}

	protected boolean isExsitLocalRouteItemInOrder(OrdOrder order) {//判断订单是否有"关联销售当地游"订单
		boolean relatedMarketingFlag = false;
		List<OrdOrderItem> OrdOrderItemList = order.getOrderItemList();

		for(OrdOrderItem ordOrderItem : OrdOrderItemList){
			if(ordOrderItem.getContent() != null  && ordOrderItem.getContent().length()>0){
				String relatedMarketingFlagStr = (String)ordOrderItem.getContentValueByKey("relatedMarketingFlag");
				if(relatedMarketingFlagStr != null && "localRoute".equals(relatedMarketingFlagStr)){
					relatedMarketingFlag = true;
				}
			}
		}
		return relatedMarketingFlag;
	}


	protected String buildProductName(OrdOrderItem ordOrderItem) {
		String productName = "未知产品名称";
		if (null != ordOrderItem) {

			Map<String,Object> contentMap = ordOrderItem.getContentMap();

			String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());

			productName = ordOrderItem.getProductName()+"-"+branchName+"("+ordOrderItem.getSuppGoodsName()+")";
		}
		return productName;
	}

	public boolean isDesignatedFreetourOrder(OrdOrder order) {
		boolean isDesignatedFreetourFlag=false;
		OrdOrderItem mainOrderItem = order.getMainOrderItem();
		if(OrderEnum.DISTRIBUTION_CHANNEL.DISTRIBUTOR_TAOBAO.getCode().equals(order.getDistributorCode())
				&& BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
				&& BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if(mainOrderItem != null && ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(mainOrderItem.getProductType())) {
				isDesignatedFreetourFlag = true;
				LOG.info("isDesignatedFreetourOrder continue,orderId=" + order.getOrderId() + " isDesignatedFreetourFlag is true");
			}
		}
		LOG.info("isDesignatedFreetourOrder end,orderId=" + order.getOrderId() + " isDesignatedFreetourFlag=" + isDesignatedFreetourFlag);
		return isDesignatedFreetourFlag;
	}

	public ResultHandle sendEmailToDesignatedAddress(OrdOrder order,List<OrdTravelContract> ordTravelContractList,String operator){
		/*ResultHandle resultHandle = new ResultHandle();
		try{
			String productName =order.getProductName();

			EmailContent emailContent = new EmailContent();

			emailContent.setContentText("您好，您在驴妈妈旅游网预订的" + productName + "，电子合同已在附件中，请查收。\n\n祝您旅途愉快。");
			emailContent.setFromAddress(Constant.getInstance().getEcontractEmailAddress());
			emailContent.setFromName("驴妈妈旅游网");
			emailContent.setSubject("订单号：" + order.getOrderId() + "的旅游合同");
			emailContent.setToAddress("hetong@lvmama.com");

			List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
			HashMap<String,ComFileMap> comFileHashMap=new HashMap<String,ComFileMap>();
			Map<Long,String> contractMaps =new HashMap<Long, String>();
			for (OrdTravelContract ordTravelContract : ordTravelContractList) {
				contractMaps.put(ordTravelContract.getOrdContractId(), ordTravelContract.getContractName());
				LOG.info("发送合同id"+ordTravelContract.getOrdContractId());
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(ordTravelContract.getFileId());
				emailAttachment.setFileName("合同_" + ordTravelContract.getVersion() + ".pdf");
				emailAttachmentList.add(emailAttachment);

				String attachementUrl = ordTravelContract.getAttachementUrl();

				if (org.apache.commons.lang3.StringUtils.isNotEmpty(attachementUrl)) {
					String[] attachements = attachementUrl.split(",");

					if (attachements != null && attachements.length >=1) {

						for (int i = 0; i < attachements.length; i++) {
							ComFileMap comFileMap = comFileMapDAO.getByFileName(attachements[0]);
							if (comFileMap != null && comFileMap.getFileId() != null) {
								comFileHashMap.put(comFileMap.getFileName(), comFileMap);
							}
						}
					}
				}

				String additionFileId= ordTravelContract.getAdditionFileId();
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(additionFileId)) {
					String[] additionFileIds = additionFileId.split(",");

					if (additionFileIds != null && additionFileIds.length >=1) {

						for (int i = 0; i < additionFileIds.length; i++) {
							emailAttachment = new EmailAttachment();
							emailAttachment.setFileId(NumberUtils.toLong(additionFileIds[i]));
							emailAttachment.setFileName(ordTravelContract.getVersion()+"_行程单.pdf" );
							emailAttachmentList.add(emailAttachment);
						}
					}
				}
			}

			for(String fileName  : comFileHashMap.keySet()) {

				ComFileMap comFileMap=comFileHashMap.get(fileName);
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(comFileMap.getFileId());
				emailAttachment.setFileName("合同_" + comFileMap.getFileName());
				emailAttachmentList.add(emailAttachment);
			}

			Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);

			if (mailId == null || mailId == 0) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
				LOG.info("sendEmailToDesignatedAddress sendEcontractEmailWithFildId:fail,orderId=" + order.getOrderId() + "邮件系统内部发送失败。");
			} else {

				Set<Long> contractIds = contractMaps.keySet();
				for(long id :contractIds){
					insertOrderLog(order.getOrderId(), id, operator, "发送合同至用户邮箱【hetong@lvmama.com】，合同名称:"+contractMaps.get(id), "");
				}
				if(contractIds.size()>0){
					ordTravelContractService.updateSendEmailFlag(contractIds);
					LOG.info("合同发送邮件，更新合同邮件标记"+contractIds.toString());
				}
				LOG.info("sendEmailToDesignatedAddress sendEcontractEmailWithFildId:success,orderId=" + order.getOrderId() + ",mailId=" + mailId);
			}

		}catch(Exception e){
			LOG.error("sendEmailToDesignatedAddress Error occurred while sending email.", e);
		}
		return resultHandle;*/
		return orderTravelElectricContactMailService.sendEmailToDesignatedAddress(order,ordTravelContractList,operator);
	}

	public ResultHandle sendEmail(OrdOrder order,List<OrdTravelContract> ordTravelContractList,String operator){
		/*ResultHandle resultHandle = new ResultHandle();
		try{
			OrdPerson contactPerson = order.getContactPerson();
			if (contactPerson == null || StringUtils.isEmpty(contactPerson.getEmail())) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "联系人邮箱没有填写。");
				return resultHandle;
			}
//		OrdOrderPack ordOrderPack = order.getOrdOrderPack();

//		 String productName =OrderUtils.getorderProductName(order);

			String productName =order.getProductName();


			EmailContent emailContent = new EmailContent();

			emailContent.setContentText("您好，您在驴妈妈旅游网预订的" + productName + "，电子合同已在附件中，请查收。\n\n祝您旅途愉快。");
			emailContent.setFromAddress(Constant.getInstance().getEcontractEmailAddress());
			emailContent.setFromName("驴妈妈旅游网");
			emailContent.setSubject("订单号：" + order.getOrderId() + "的旅游合同");
			emailContent.setToAddress(contactPerson.getEmail());



			List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
			HashMap<String,ComFileMap> comFileHashMap=new HashMap<String,ComFileMap>();
			Map<Long,String> contractMaps =new HashMap<Long, String>();
			for (OrdTravelContract ordTravelContract : ordTravelContractList) {
				contractMaps.put(ordTravelContract.getOrdContractId(), ordTravelContract.getContractName());
				LOG.info("发送合同id"+ordTravelContract.getOrdContractId());
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(ordTravelContract.getFileId());
				emailAttachment.setFileName("合同_" + ordTravelContract.getVersion() + ".pdf");
				emailAttachmentList.add(emailAttachment);


				String attachementUrl = ordTravelContract.getAttachementUrl();


				if (org.apache.commons.lang3.StringUtils.isNotEmpty(attachementUrl)) {
					String[] attachements = attachementUrl.split(",");

					if (attachements != null && attachements.length >=1) {

						for (int i = 0; i < attachements.length; i++) {
							ComFileMap comFileMap = comFileMapDAO.getByFileName(attachements[0]);
							if (comFileMap != null && comFileMap.getFileId() != null) {
								comFileHashMap.put(comFileMap.getFileName(), comFileMap);
							}
						}

					}
				}



				String additionFileId= ordTravelContract.getAdditionFileId();
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(additionFileId)) {
					String[] additionFileIds = additionFileId.split(",");

					if (additionFileIds != null && additionFileIds.length >=1) {

						for (int i = 0; i < additionFileIds.length; i++) {
							emailAttachment = new EmailAttachment();
							emailAttachment.setFileId(NumberUtils.toLong(additionFileIds[i]));
							emailAttachment.setFileName(ordTravelContract.getVersion()+"_行程单.pdf" );
							emailAttachmentList.add(emailAttachment);
						}
					}
				}

			}


			for(String fileName  : comFileHashMap.keySet()) {

				ComFileMap comFileMap=comFileHashMap.get(fileName);
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(comFileMap.getFileId());
				emailAttachment.setFileName("合同_" + comFileMap.getFileName());
				emailAttachmentList.add(emailAttachment);
			}


			Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);

			if (mailId == null || mailId == 0) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
				LOG.info("sendEmail sendEcontractEmailWithFildId:fail,订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
			} else {

				Set<Long> contractIds = contractMaps.keySet();
				for(long id :contractIds){
					insertOrderLog(order.getOrderId(), id, operator, "发送合同至用户邮箱【"+order.getContactPerson().getEmail()+"】，合同名称:"+contractMaps.get(id), "");
				}
				if(contractIds.size()>0){
					ordTravelContractService.updateSendEmailFlag(contractIds);
					LOG.info("合同发送邮件，更新合同邮件标记"+contractIds.toString());
				}
				LOG.info("sendEmail sendEcontractEmailWithFildId:success,订单ID=" + order.getOrderId() + ",mailId=" + mailId);
			}

		}catch(Exception e){
			LOG.error("Error occurred while sending email.", e);
		}
		return resultHandle;*/
		return orderTravelElectricContactMailService.sendEmail(order,ordTravelContractList,operator);
	}


	public ResultHandle sendOrderEcontractEmail(OrdOrder order,String opertator) {
		boolean isDesignatedFreetourFlag = TntOrderUtils.isDesignatedFreetourOrder(order);
		lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.ORDER_ECONTRACT_SEND.name(), order.getOrderId(),LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(),
				"isDesignatedFreetourFlag初始化成功", "isDesignatedFreetourFlag" + isDesignatedFreetourFlag+",operator=" +opertator);
		if(isDesignatedFreetourFlag) {
			return designatedFreetourOrderService.sendOrderEcontractEmail(order, opertator);
		}
		//初始化参数
		Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
		parametersTravelContract.put("orderId",order.getOrderId());
		List<OrdTravelContract> ordTravelContractList=ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
		List<OrdTravelContract> designatedTravelContractList = new ArrayList<OrdTravelContract>();
		List<OrdTravelContract> notDesignatedTravelContractList = new ArrayList<OrdTravelContract>();
		List<OrdTravelContract> travelContractList = new ArrayList<OrdTravelContract>();
		//判断订单支付方式，是否是一律预授权并是后置订单
		if("Y".equals(order.getTravellerDelayFlag()) && "PREAUTH".equals(order.getPaymentType())){
			if(ordTravelContractList!=null){
				for(OrdTravelContract co:ordTravelContractList){
					//订单未锁定
					if("N".equals(order.getTravellerLockFlag())){
						if(co.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())){
							travelContractList.add(co);
							break;
						}
					}else{
						if(!co.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())){
							travelContractList.add(co);
							break;
						}
					}
				}
				/*if("system".equalsIgnoreCase(opertator) && isDesignatedFreetourFlag && CollectionUtils.isNotEmpty(travelContractList)){
					LOG.info("sendOrderEcontractEmail condition access:start,orderId=" + order.getOrderId());
					for(OrdTravelContract tc:travelContractList){
						if(tc.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode())
								|| tc.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode())){
							designatedTravelContractList.add(tc);
						}else{
							notDesignatedTravelContractList.add(tc);
						}
					}
					ResultHandle resultHandle = new ResultHandle();
					if(CollectionUtils.isNotEmpty(designatedTravelContractList)){
						LOG.info("sendEmailToDesignatedAddress:start,orderId=" + order.getOrderId());
						resultHandle = sendEmailToDesignatedAddress(order, designatedTravelContractList,opertator);
					}
					if(CollectionUtils.isNotEmpty(notDesignatedTravelContractList)){
						resultHandle = sendEmail(order, notDesignatedTravelContractList,opertator);
					}
					return resultHandle;
				}else {*/
					LOG.info("sendOrderEcontractEmail condition unvalid:start,orderId=" + order.getOrderId());
					return sendEmail(order, travelContractList, opertator);
				//}
			}
		}else{
			OrdTravelContract preContract = null;
			if(ordTravelContractList!=null){
				for(OrdTravelContract co:ordTravelContractList){
					if(co.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())){
						preContract = co;
						break;
					}
				}
			}
			if(preContract!=null){
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(preContract);
				return sendEmail(order, list,opertator);
			}
			
			/*if("system".equalsIgnoreCase(opertator) && isDesignatedFreetourFlag && CollectionUtils.isNotEmpty(ordTravelContractList)){
				LOG.info("sendOrderEcontractEmail condition access:start==,orderId=" + order.getOrderId());
				for(OrdTravelContract otc:ordTravelContractList){
					if(otc.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode())
							|| otc.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode())){
						designatedTravelContractList.add(otc);
					}else{
						notDesignatedTravelContractList.add(otc);
					}
				}
				ResultHandle resultHandle = new ResultHandle();
				if(CollectionUtils.isNotEmpty(designatedTravelContractList)){
					LOG.info("sendEmailToDesignatedAddress:start==,orderId=" + order.getOrderId());
					resultHandle = sendEmailToDesignatedAddress(order, designatedTravelContractList,opertator);
				}
				if(CollectionUtils.isNotEmpty(notDesignatedTravelContractList)){
					
					resultHandle =  sendEmail(order, notDesignatedTravelContractList,opertator);
				}
				return resultHandle;
			}else {*/
				LOG.info("sendOrderEcontractEmail condition unvalid:start==,orderId=" + order.getOrderId());
				return sendEmail(order, ordTravelContractList, opertator);
			//}
		}

		return null;
	}


	/**
	 * 根据订单与合同号发送合同邮件
	 * @param order
	 * @param contractId
	 * @return
	 */
	public ResultHandle sendContractEmail(OrdOrder order,Long contractId,String operator){
		boolean isDesignatedFreetourFlag = TntOrderUtils.isDesignatedFreetourOrder(order);
		lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.ORDER_ECONTRACT_SEND.name(), order.getOrderId(),LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(),
				"isDesignatedFreetourFlag初始化成功", "isDesignatedFreetourFlag" + isDesignatedFreetourFlag+",operator=" +operator);
		if(isDesignatedFreetourFlag) {
			return designatedFreetourOrderService.sendContractEmail(order, contractId, operator);
		}
		//初始化参数
		OrdTravelContract tract = ordTravelContractService.findOrdTravelContractById(contractId);
        List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
        list.add(tract);
        ResultHandle resultHandle = new ResultHandle();
		/*if(tract != null && "system".equalsIgnoreCase(operator) && isDesignatedFreetourFlag
                && (ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(tract.getContractTemplate())
                || ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(tract.getContractTemplate()))){
            LOG.info("sendContractEmail sendEmailToDesignatedAddress,orderId=" + order.getOrderId());
		    resultHandle = sendEmailToDesignatedAddress(order, list,operator);
        } else {*/
            LOG.info("sendContractEmail sendEmail,orderId=" + order.getOrderId());
            resultHandle = sendEmail(order, list,operator);
        //}
        return resultHandle;
	}


	protected void saveTravelItineraryContract(OrdTravelContract ordTravelContract,String operatorName) {
		/*Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId",orderId );
		params.put("contractTemplate", ELECTRONIC_CONTRACT_TEMPLATE.TRAVEL_ITINERARY.getCode());

		List<OrdTravelContract>  contractList=ordTravelContractService.findOrdTravelContractList(params);
		if (CollectionUtils.isNotEmpty(contractList)) {
			travelItineraryContractService.saveTravelContact(ordTravelContract, operatorName);
		}*/

		ResultHandle re=travelItineraryContractService.saveTravelContact(ordTravelContract, operatorName);
		LOG.info("---------------------合同2==="+re.getMsg());

	}

	protected Map<String,Object> getContractContent(OrdTravelContract ordTravelContract,OrdOrder order) {

		File directioryFile = ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);

		Map<String,Object> map = travelItineraryContractService.captureContract(ordTravelContract,order,directioryFile);

		return map;
	}



	protected HashMap<String, Object> getProductIdAndName(OrdTravelContract ordTravelContract,OrdOrder order) {


//		Long productId=96321L;
		Long productId=0L;
		String productName="";
		OrdOrderItem orderContractItem=new OrdOrderItem();

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ordContractId", ordTravelContract.getOrdContractId());
		List<OrdItemContractRelation> ordItemContractRelationList=ordItemContractRelationService.findOrdItemContractRelationList(params);
		if (order.getCategoryId()==8 || this.isOrderPackTrigger(ordTravelContract)) {//打包资源引起合同 相关合同内容显示这个打包产品
			productId=order.getOrdOrderPack().getProductId();
			productName=order.getOrdOrderPack().getProductName();
		}else{//单个产品引起合同 相关合同内容显示这个产品

//			Long orderItemId=48502L;
			Long orderItemId=ordItemContractRelationList.get(0).getOrderItemId();
			orderContractItem=this.ordOrderUpdateService.getOrderItem(orderItemId);

			productId=orderContractItem.getProductId();
			productName=orderContractItem.getProductName();
		}

		HashMap<String, Object> map=new HashMap<String, Object>();
//		map.put("productId", 99421L);
//		map.put("productName", "测试小天跟团游");

		map.put("productId", productId);
		map.put("productName", productName);
		map.put("orderContractItem", orderContractItem);

		return map;
	}

	protected OrdOrderItem getOrderContractItem(OrdTravelContract ordTravelContract,OrdOrder order) {

		OrdOrderItem orderContractItem=new OrdOrderItem();

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ordContractId", ordTravelContract.getOrdContractId());
		List<OrdItemContractRelation> ordItemContractRelationList = ordItemContractRelationService.findOrdItemContractRelationList(params);
		if(ordItemContractRelationList != null && ordItemContractRelationList.size()>0){
			Long orderItemId = ordItemContractRelationList.get(0).getOrderItemId();
			orderContractItem = this.ordOrderUpdateService.getOrderItem(orderItemId);
		}
		return orderContractItem;
	}

	protected boolean isOrderPackTrigger(OrdTravelContract ordTravelContract) {


		Long[] orderItemIdArray=this.getTriggerOrderItemId(ordTravelContract);
		if (orderItemIdArray!=null ) {
			if (orderItemIdArray.length>1) {
				return true;
			}else if (orderItemIdArray.length==1) {

				OrdOrderItem orderItem=this.ordOrderUpdateService.getOrderItem(orderItemIdArray[0]);
				if (orderItem.getOrderPackId()!=null) {
					return true;
				}else{
					return false;
				}
			}
		}else{

			throw new BusinessException(ordTravelContract.getOrdContractId()+"该id合同未找到关联子订单关系");
		}

		return false;
	}
	/**
	 * 合同  返回对应引起合同资源的子订单id
	 * 是打包引起的还是关联销售引起的 具体看子订单上orderPackId是否为空
	 * @param ordTravelContract
	 * @param order
	 * @return
	 */
	protected Long[] getTriggerOrderItemId(OrdTravelContract ordTravelContract) {

		HashMap<String, Object> mapItemCon = new HashMap<String, Object>();
		mapItemCon.put("ordContractId",
				ordTravelContract.getOrdContractId());
		List<OrdItemContractRelation> itemConRelationList = ordItemContractRelationService
				.findOrdItemContractRelationList(mapItemCon);
		if (CollectionUtils.isNotEmpty(itemConRelationList)) {
			Long[] orderItemIdArray = new Long[itemConRelationList.size()];
			int i = 0;
			for (OrdItemContractRelation ordItemContractRelation : itemConRelationList) {

				orderItemIdArray[i++] = ordItemContractRelation
						.getOrderItemId();

			}

			return orderItemIdArray;
		}else{
//			return new Long[48502];
			throw new BusinessException(ordTravelContract.getOrdContractId()+"该id合同未找到关联子订单关系");
		}

	}
	/**
	 *   只有跟团游供应商打包情况才取，只取得打包上的成人价格 儿童价格，不获取关联商品上的
	 * @param ordTravelContract
	 * @param order
	 * @return
	 */
	protected String[] getPriceAdultAndChild(OrdTravelContract ordTravelContract,OrdOrder order) {
		String priceAdult=null;
		String priceChild=null;
		Long aduitCount=0L;
		Long childCount=0L;
		if(order.getCategoryId()!=8){
			if (this.isOrderPackTrigger(ordTravelContract)) {//打包引起的合同
				OrdOrderPack ordPack = order.getOrdOrderPack();
				if (!ordPack.hasOwn()) {

					Long categoryId = ordPack.getCategoryId();
					ResultHandleT<BizCategory> result = categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode());
					BizCategory bizCategory = result.getReturnContent();
					if (bizCategory.getCategoryId().equals(categoryId)) {

						Long[] orderItemIdArray =this.getTriggerOrderItemId(ordTravelContract);

						String[] priceTypeArray = new String[] {
								// ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
								// ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
								// ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
								ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
								ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode() };

						Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
						paramsMulPriceRate.put("orderItemIdArray",orderItemIdArray);
						paramsMulPriceRate.put("priceTypeArray", priceTypeArray);
						List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);

						if (CollectionUtils.isNotEmpty(ordMulPriceRateList)) {
							Long priceAdultTotal = 0L;
							Long priceChildTotal = 0L;
							for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {

								if (ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode().equals(ordMulPriceRate.getPriceType())) {

									priceAdultTotal += ordMulPriceRate.getPrice();

									aduitCount+=ordMulPriceRate.getQuantity();
								} else if (ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode().equals(ordMulPriceRate.getPriceType())) {

									priceChildTotal += ordMulPriceRate.getPrice();

									childCount+=ordMulPriceRate.getQuantity();
								}

							}

							priceAdult = PriceUtil.trans2YuanStr(priceAdultTotal);
							priceChild = PriceUtil.trans2YuanStr(priceChildTotal);
							/*
							if (aduitCount!=null && aduitCount!=0) {
								priceAdult = PriceUtil
										.trans2YuanStr(priceAdultTotal/aduitCount);
							}else{

								priceAdult = null;
							}

							if (childCount!=null && childCount!=0) {
								priceChild = PriceUtil
										.trans2YuanStr(priceChildTotal/childCount);
							}else{

								priceChild =null;
							}*/


						}

					}
				}


			}

		}
		return new String[]{priceAdult,priceChild,aduitCount+"",childCount+""};
	}



	protected long getTotalPrice(List<OrdOrderItem> ordOrderItemList) {
		long totalPrice = 0;
		if (ordOrderItemList != null) {
			for (OrdOrderItem ordOrderItem : ordOrderItemList) {
				if (ordOrderItem != null) {
					totalPrice = totalPrice + ordOrderItem.getPrice() * ordOrderItem.getQuantity();
				}
			}
		}

		return totalPrice;
	}


	protected List<OrdOrderItem> getInsuranceOrdOrderItem(OrdOrder order) {
		List<OrdOrderItem> ordOrderItemList = null;

		if (order != null) {
			ordOrderItemList = new ArrayList<OrdOrderItem>();
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (orderItem != null) {
					String categoryCode = (String) orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.name().equalsIgnoreCase(categoryCode)) {
						ordOrderItemList.add(orderItem);
					}
				}
			}
		}

		return ordOrderItemList;
	}



	/**
	 * 合同签约状态逻辑
	 * @param ordTravelContract
	 * @param order
	 * @param isCreateOrder
	 */
	protected void setOrdContractStatus(OrdTravelContract ordTravelContract,
			OrdOrder order, boolean isCreateOrder) {
		//有没有预付款协议 的时候合同状态逻辑
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderId", ordTravelContract.getOrderId());
		map.put("contractTemplate", CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name());
		List<OrdTravelContract> contractList=ordTravelContractService.findOrdTravelContractList(map);
		//判断当前订单是否是后置订单
		if("Y".equals(order.getTravellerDelayFlag())){
			if(OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus()) &&
					"Y".equals(order.getTravellerLockFlag()) && "AMPLE".equals(order.getResourceStatus())) {
				ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
			}else if(OrderEnum.ORDER_STATUS.CANCEL.getCode().equalsIgnoreCase(order.getOrderStatus())){
				ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
			}else{
				ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
			}
		}else{
			if (CollectionUtils.isEmpty(contractList)) {//没有预付款协议
				if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
				}else if (OrderEnum.ORDER_STATUS.CANCEL.getCode().equalsIgnoreCase(order.getOrderStatus())) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
				} else {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
				}
			}else{//有预付款协议
				//是否订单创建的时候依据 当前合同对应fileId是否为空，如果为空则为创建订单时候操作，否则为修改合同操作
				if (isCreateOrder) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.name());
				}else if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
				}else if (OrderEnum.ORDER_STATUS.CANCEL.getCode().equalsIgnoreCase(order.getOrderStatus())) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
				} else {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
				}
			}
		}

	}


	/**
	 *
	 * 保存日志
	 *
	 */
	public void insertOrderLog(final Long orderId, Long contractId,String operatorName,String content, String memo){
		/*lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ECONTRACT,
				orderId,
				contractId,
				operatorName,
				content,
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE.name(),
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE.getCnName(),
				memo);*/
		orderTravelElectricContactMailService.insertOrderLog(orderId, contractId, operatorName,content,memo);
	}

	/**
	 *
	 * 保存日志
	 *
	 */
	public void sendEmailLog(final Long orderId, Long contractId,String operatorName,String content, String memo){
		/*lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ECONTRACT,
				orderId,
				contractId,
				operatorName,
				content,
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_EMAIL.name(),
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_EMAIL.getCnName(),
				memo);*/
		orderTravelElectricContactMailService.sendEmailLog(orderId, contractId, operatorName,content,memo);
	}



	public ResultHandleT<String> contractTemplateHtml(String templateName){
		ResultHandleT<String> resultHandle = new ResultHandleT<String>();
		try {
			File directioryFile = initDirectory();
			if (directioryFile == null || !directioryFile.exists()) {
				resultHandle.setMsg("合同模板目录不存在。");
				return resultHandle;
			}
			Configuration configuration = initConfiguration(directioryFile);
			if (configuration == null) {
				resultHandle.setMsg("初始化freemarker失败。");
				return resultHandle;
			}
			Template template = configuration.getTemplate(templateName);
			if (template == null) {
				resultHandle.setMsg("初始化ftl模板失败。");
				return resultHandle;
			}
			    Map<String,Object> rootMap = new HashMap<String, Object>();
				TravelContractVO travelContractVO =  new TravelContractVO();
				OrdOrder order = new OrdOrder();
				travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
				LOG.info("advanceProductAgreementContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
				rootMap.put("travelContractVO", travelContractVO);
				rootMap.put("order", order);
			Map<String,List<OrderMonitorRst>>  chidOrderMap= new HashMap<String, List<OrderMonitorRst>>();
			rootMap.put("chidOrderMap", chidOrderMap);
			StringWriter sw = new StringWriter();
			template.process(rootMap, sw);
			String htmlString = sw.toString();
			if (htmlString == null) {
				resultHandle.setMsg("合同HTML生成失败。");
				return resultHandle;
			}else{
				resultHandle.setReturnContent(htmlString);
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}
		return resultHandle;
	}


	/**
	 * 自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
	 * @param order
	 * @param travelContractVO
	 * @param prodProduct
	 */
	public void fillProdContractDetail(OrdOrder order,
			TravelContractVO travelContractVO, ProdProduct prodProduct) {

		//查询当前产品是否有关联的合同条款
		ProdProductAssociation prodProductAssociation = prodProductClientService.findProdProductAssociationByProductId(prodProduct.getProductId()).getReturnContent();
		if(prodProductAssociation != null && prodProductAssociation.getAssociatedContractProdId() != null){
			//获取被打包关联的合同条款产品id
			Long associatedContractProdId = prodProductAssociation.getAssociatedContractProdId();

			//获取被关联的行程
			List<ProdLineRoute> prodLineRouteList = prodProductClientService.findOnlyLineRouteId(associatedContractProdId);
			if(prodLineRouteList != null && prodLineRouteList.size() > 0){
				travelContractVO.setLineRouteId(prodLineRouteList.get(0).getLineRouteId());
			}
		}

		if(travelContractVO.getLineRouteId()==null){
			travelContractVO.setLineRouteId(order.getLineRouteId());
		}

		Long bizCategoryId = prodProduct.getBizCategoryId();
		List<ProdContractDetail> contractDetailList = null;
		//邮轮组合产品
		if(bizCategoryId.longValue() == 8L) {
			contractDetailList = getProdContractDetailsByProductId(prodProduct.getProductId());
		} else {
			contractDetailList = getProdContractDetails(travelContractVO.getLineRouteId());
		}
		//购物说明
		List<ProdContractDetail> shopingDetailList=new ArrayList<ProdContractDetail>();
		//项目推荐
		List<ProdContractDetail> recommendDetailList=new ArrayList<ProdContractDetail>();

		if (CollectionUtils.isNotEmpty(contractDetailList)) {

			for (ProdContractDetail prodContractDetail : contractDetailList) {
				Date vistStartTime = null;
				Short dayShort = prodContractDetail.getnDays();
				if(dayShort != null && dayShort != 0){
					vistStartTime = DateUtils.addDays(order.getVisitTime(), dayShort.intValue()-1);
				}
				prodContractDetail.setVistStartTime(vistStartTime);

				if ("SHOPING".equals(prodContractDetail.getDetailType())) {
					shopingDetailList.add(prodContractDetail);
				}else if ("RECOMMEND".equals(prodContractDetail.getDetailType())) {
					recommendDetailList.add(prodContractDetail);
				}
			}
		}
		travelContractVO.setShopingDetailList(shopingDetailList);
		travelContractVO.setRecommendDetailList(recommendDetailList);
	}

	/**
	 * 邮轮
	 * @param lineRouteId
	 * @return
	 */
	protected List<ProdContractDetail> getProdContractDetailsByProductId(Long productId) {
		if(productId == null) {
			return null;
		}
		List<ProdContractDetail> prodContractDetailList = null;
		ResultHandleT<List<ProdContractDetail>> resultHandle = null;
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("productId", productId);
			resultHandle = prodContractDetailClientService.findProdContractDetailList(params);
		} catch (Exception e) {
			LOG.error("找不到对应的行程，产品ID：" + productId+ "，异常信息： {}", e);
		}
		if(resultHandle != null && CollectionUtils.isNotEmpty(resultHandle.getReturnContent())) {
			prodContractDetailList = resultHandle.getReturnContent();
		}
		return prodContractDetailList;
	}

	/**
	 * 根据行程ID获取合同条款信息
	 * @param lineRouteId
	 * @return
	 */
	protected List<ProdContractDetail> getProdContractDetails(Long lineRouteId) {
		if(lineRouteId == null) {
			return null;
		}

		ResultHandleT<List<ProdLineRoute>> lineRouteResults = null;
		List<ProdContractDetail> contractDetailList = null;
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("lineRouteId", lineRouteId);
			lineRouteResults = this.prodLineRouteClientService.findProdLineRouteAllList(params, true);
		} catch (Exception e) {
			LOG.error("找不到对应的行程，行程ID：" + lineRouteId+ "，异常信息： {}", e);
		}
		if(lineRouteResults != null && CollectionUtils.isNotEmpty(lineRouteResults.getReturnContent())) {
			ProdLineRoute lineRoute = lineRouteResults.getReturnContent().get(0);
			contractDetailList = lineRoute.getProdContractDetailList();
		}

		return contractDetailList;
	}

	/**
	 * 根据行程ID获取费用包含&费用不包含
	 * @param lineRouteId
	 * @return
	 */
	protected Map<String, Object> getCostIncExc(Long lineRouteId) {
		Map<String, Object> costIncExcMap = new HashMap<String, Object>();
		if(lineRouteId == null) {
			return costIncExcMap;
		}

		ResultHandleT<ProdLineRoute> lineRouteResult = null;
		try {
			lineRouteResult = this.prodLineRouteClientService.findProdLineRouteById(lineRouteId);
		} catch (Exception e) {
			LOG.error("找不到对应的行程，行程ID：" + lineRouteId + "，异常信息： {}", e);
		}

		if(lineRouteResult != null) {
			ProdLineRoute lineRoute = lineRouteResult.getReturnContent();
			//费用包含
			if(StringUtils.isNotBlank(lineRoute.getCostInclude())) {
				costIncExcMap.put(BizEnum.LINE_PROP_CODE.the_fee_includes.getCode(), lineRoute.getCostInclude());
			}
			//费用不包含
			if(StringUtils.isNotBlank(lineRoute.getCostExclude())) {
				costIncExcMap.put(BizEnum.LINE_PROP_CODE.cost_free.getCode(), lineRoute.getCostExclude());
			}
		}

		return costIncExcMap;
	}



	public void setSupplementaryAndMinPersonCount(TravelContractVO travelContractVO, ProdProduct prodProduct) {
		String minPersonCountOfGroup = "";
		Map<String, Object> productPropMap = prodProduct.getPropValue();// curiseProductVO.getProductPropMap();
		if(productPropMap == null) {
			productPropMap = new HashMap<String, Object>();
		}
		//获取费用包含&费用不包含，放进productPropMap
		productPropMap.putAll(getCostIncExc(travelContractVO.getLineRouteId()));

		Map<String, Object> descInfo = new LinkedHashMap<String, Object>();

		if (!productPropMap.isEmpty()) {
			//费用包含
			descInfo.put(BizEnum.LINE_PROP_CODE.the_fee_includes.getCnName(), productPropMap.get(BizEnum.LINE_PROP_CODE.the_fee_includes.getCode()));
			//费用不包含
			descInfo.put(BizEnum.LINE_PROP_CODE.cost_free.getCnName(), productPropMap.get(BizEnum.LINE_PROP_CODE.cost_free.getCode()));

			//行前须知
			descInfo.put(BizEnum.LINE_PROP_CODE.important.getCnName(), productPropMap.get(BizEnum.LINE_PROP_CODE.important.getCode()));

			//出行警示及说明
			descInfo.put(BizEnum.LINE_PROP_CODE.warning.getCnName(), productPropMap.get(BizEnum.LINE_PROP_CODE.warning.getCode()));

			//退改说明
			descInfo.put(BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCnName(), productPropMap.get(BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCode()));

			//最低成团人数
			minPersonCountOfGroup = (String) productPropMap.get("least_cluster_person");

			travelContractVO.setDescInfo(descInfo);
		}

		//最低成团人数
		travelContractVO.setMinPersonCountOfGroup(minPersonCountOfGroup);
	}



	public void setSupplementAndMinPersonCount(
			TravelContractVO travelContractVO, ProdProduct prodProduct,OrdOrder order) {
		String feeIncludeExtra ="";//费用包含补充条款
		//判断当前产品是否有关联的费用包含&不包含
		ProdProductAssociation prodProductAssociation = prodProductClientService.findProdProductAssociationByProductId(prodProduct.getProductId()).getReturnContent();
		if(prodProductAssociation != null && prodProductAssociation.getAssociatedFeeIncludeProdId() != null){
			//获取被打包关联的费用包含不包含产品id
			Long associatedFeeIncludeProdId = prodProductAssociation.getAssociatedFeeIncludeProdId();
			//获取被关联的行程
			List<ProdLineRoute> prodLineRouteList = prodProductClientService.findOnlyLineRouteId(associatedFeeIncludeProdId);
			if(prodLineRouteList != null && prodLineRouteList.size() > 0){
				travelContractVO.setLineRouteId(prodLineRouteList.get(0).getLineRouteId());
			}
			if(StringUtils.isNotEmpty(prodProductAssociation.getFeeIncludeExtra())){
				feeIncludeExtra =  prodProductAssociation.getFeeIncludeExtra();
			}
		}

		String least_cluster_person="";
		Map<String, Object> productPropMap =prodProduct.getPropValue();// curiseProductVO.getProductPropMap();
		if(productPropMap == null) {
			productPropMap = new HashMap<String, Object>();
		}
		//获取费用包含&费用不包含，放进productPropMap
		productPropMap.putAll(getCostIncExc(travelContractVO.getLineRouteId()));

		if (productPropMap != null && !productPropMap.isEmpty()) {
			String code = null;
			String value = null;
			String cnName = null;
			String the_fee_includes = "";
			String cost_free = "";
			String important = "";
			String warning = "";
			String change_and_cancellation_instructions = "";



			for (Entry<String, Object> entry : productPropMap.entrySet()) {
				if (entry != null) {
					code = entry.getKey();
					if (entry.getValue()==null || !entry.getValue().getClass().equals(String.class)) {
						continue;
					}
					value = (String) entry.getValue();
					if ("least_cluster_person".equals(code)) {
						least_cluster_person=value;
					}
					cnName = BizEnum.LINE_PROP_CODE.getCnName(code);
					if (cnName.equals(code)) {
						continue;
					}

					if (value != null && !value.trim().isEmpty()) {
						if ( BizEnum.LINE_PROP_CODE.the_fee_includes.getCode().equals(code)) {//费用包含
							the_fee_includes=BizEnum.LINE_PROP_CODE.the_fee_includes.getCnName()+":<br/>" + value + "<br/>" + feeIncludeExtra;
						}else if ( BizEnum.LINE_PROP_CODE.cost_free.getCode().equals(code)) {//费用不包含
							if(StringUtils.isNotEmpty(travelContractVO.getDescription())){
								cost_free=BizEnum.LINE_PROP_CODE.cost_free.getCnName()+":<br/>"+ value + travelContractVO.getDescription();
							}else{
								cost_free=BizEnum.LINE_PROP_CODE.cost_free.getCnName()+":<br/>"+value;
							}
						}else if ( BizEnum.LINE_PROP_CODE.important.getCode().equals(code)) {//行前须知
							important=BizEnum.LINE_PROP_CODE.important.getCnName()+":<br/>"+value;
						}else if ( BizEnum.LINE_PROP_CODE.warning.getCode().equals(code)) {//出行警示及说明
							warning =BizEnum.LINE_PROP_CODE.warning.getCnName()+":<br/>"+value;
						}else if ( BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCode().equals(code)) {//退改说明
							change_and_cancellation_instructions=BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCnName()+":<br/>"+value;
						}
			       }
				}
			}

			if("".equals(the_fee_includes) && !"".equals(feeIncludeExtra)){
				the_fee_includes=BizEnum.LINE_PROP_CODE.the_fee_includes.getCnName()+":<br/>" + feeIncludeExtra;
			}

			//国内跟团游、当地游合同附件新增出游人预定限制，仅支持pc前台、m站、无线AppV8.0.3(含)之后版本的订单
			String reserve_limit = "";
			boolean needShowReserveLimit = false;
			if((BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(prodProduct.getBizCategoryId())
					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(prodProduct.getBizCategoryId()))
					&& !ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equalsIgnoreCase(prodProduct.getProductType())){

				if(Constant.DIST_FRONT_END == order.getDistributorId()){
					needShowReserveLimit = true;
				}
				if(order.getDistributionChannel() != null && order.getDistributionChannel().longValue() == 10000 && order.getDistributorCode() != null){
					if(StringUtils.isNotBlank(order.getAppVersion()) && (order.getDistributorCode().contains("ANDROID") || order.getDistributorCode().contains("IPHONE"))){
						Long appVersion = Long.parseLong(order.getAppVersion());
						if(appVersion != null && appVersion.longValue() >= 80003){
							needShowReserveLimit = true;
						}
					}
					if(order.getDistributorCode().contains("TOUCH")){
						needShowReserveLimit = true;
					}
				}
			}
			if(needShowReserveLimit){
				ResultHandleT<ProdProductDescription> resultHandleT = prodProductDescriptionClientService.queryReserveLimitDescriptionForContact(prodProduct.getProductId());
				if(resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null){
					if(StringUtils.isNotBlank(resultHandleT.getReturnContent().getContent())){
						reserve_limit = "出游人预订限制说明:<br/>"
								+ resultHandleT.getReturnContent().getContent()
								+ "您已经确认出游人中不包含以上几种情况的出游人。<br/>";
					}
				}
			}

			StringBuffer stringBuilder = new StringBuffer();
			stringBuilder.append(the_fee_includes).append("<br/>").append("<br/>")
					.append(cost_free).append("<br/>").append(important)
					.append("<br/>").append(warning).append("<br/>")
					.append(change_and_cancellation_instructions);
			if(StringUtils.isNotBlank(reserve_limit)){
				stringBuilder.append("<br/>").append(reserve_limit);
			}

			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				for (OrdOrderItem ordOrderItem : insuranceOrderItemList) {
					if (ordOrderItem != null) {
						String totalPrice =  PriceUtil.trans2YuanStr(ordOrderItem.getPrice()); //保险金额
						String insuranceCompanyAndProductName = ordOrderItem.getProductName();//保险名称
						stringBuilder.append("<br/>").append("保险金额：" + totalPrice).append("元/人").append("<br/>").append("保险名称：" + insuranceCompanyAndProductName);
					}
				}
			}

			travelContractVO.setSupplementaryTerms(PdfUtil.convertHtml(stringBuilder.toString()));

		} else {
			travelContractVO.setSupplementaryTerms("");
		}


		//最低成团人数

		travelContractVO.setMinPersonCountOfGroup(least_cluster_person);
	}



	/**
	 * 生成合同的时候调试打开
	 * @param fileBytes
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void newContractDebug(byte[] fileBytes, String fileName)
			throws FileNotFoundException, IOException {
		if (isDubgPdf) {
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(new File("E:/econtract/", fileName));
				fileOutputStream.write(fileBytes);
			} catch (FileNotFoundException e) {
				LOG.error("{}", e);
				throw e;
			} catch (IOException e) {
				LOG.error("{}", e);
				throw e;
			} finally {
				if(fileOutputStream != null) {
					fileOutputStream.flush();
					fileOutputStream.close();
				}
			}

			/*FileWriter fileWriter = new FileWriter(new File(directioryFile, fileName + ".html"));
			fileWriter.write(htmlString);
			fileWriter.close();*/
		}
	}


	/**
	 * 调试时打开自动本地成文件
	 * @param fileBytes
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void updateContractDubg(byte[] fileBytes, String fileName)
			throws FileNotFoundException, IOException {
		// 调试时打开
		if (isDubgPdf) {
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(new File("E:/econtract/", fileName));
				fileOutputStream.write(fileBytes);
			} catch (FileNotFoundException e) {
				LOG.error("{}", e);
				throw e;
			} catch (IOException e) {
				LOG.error("{}", e);
				throw e;
			} finally {
				if(fileOutputStream != null) {
					fileOutputStream.flush();
					fileOutputStream.close();
				}
			}
		}
	}

	/**
	 * 根据产品相关信息生成合同内容
	 * @param templateName
	 * @param productId
	 * @return
	 */
	public ResultHandleT<String> contractTemplateHtml(String templateName,Long productId){
		ResultHandleT<String> resultHandle = new ResultHandleT<String>();
		try {
			File directioryFile = initDirectory();
			if (directioryFile == null || !directioryFile.exists()) {
				resultHandle.setMsg("合同模板目录不存在。");
				return resultHandle;
			}
			Configuration configuration = initConfiguration(directioryFile);
			if (configuration == null) {
				resultHandle.setMsg("初始化freemarker失败。");
				return resultHandle;
			}
			Template template = configuration.getTemplate(templateName);
			if (template == null) {
				resultHandle.setMsg("初始化ftl模板失败。");
				return resultHandle;
			}
			    Map<String,Object> rootMap = new HashMap<String, Object>();
				TravelContractVO travelContractVO =  new TravelContractVO();
				ProdProductParam param = new ProdProductParam();
				param.setProductProp(true);
				param.setProductBranchValue(true);
				param.setProdEcontract(true);
				ResultHandleT<ProdProduct> resProduct=this.prodProductClientService.findLineProductByProductId(productId, param);
				ProdProduct prodProduct=resProduct.getReturnContent();
				//产品是否委托组团
				if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
				{
					travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
					travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
					LOG.info(prodProduct.getProductId()+"product is COMMISSIONED_TOUR ");
				}
				if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())){
					travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
					LOG.info(prodProduct.getProductId()+"product is SELF_TOUR");
				}
				OrdOrder order = new OrdOrder();
				travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
				LOG.info("advanceProductAgreementContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
				rootMap.put("travelContractVO", travelContractVO);
				rootMap.put("order", order);
			Map<String,List<OrderMonitorRst>>  chidOrderMap= new HashMap<String, List<OrderMonitorRst>>();
			rootMap.put("chidOrderMap", chidOrderMap);
			StringWriter sw = new StringWriter();
			template.process(rootMap, sw);
			String htmlString = sw.toString();
			if (htmlString == null) {
				resultHandle.setMsg("合同HTML生成失败。");
				return resultHandle;
			}else{
				resultHandle.setReturnContent(htmlString);
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}
		return resultHandle;
	}


	protected static HashSet<String> GUOLV_contractTemplate = new HashSet<String>();
	protected static String GUOLV_filialeName = "上海驴妈妈国际旅行社有限公司";			// 国旅-旅行社名称
	protected static String GUOLV_permit = "L-SH-CJ00106";							// 国旅-许可证编号	--http://pic.lvmama.com/img/order/stamp/
	protected static String GUOLV_stampImage = "SH_ECONTRACT_GUOLV.png";			// 国旅-子公司专用章
	//protected static String GUOLV_address = "上海市嘉定区景域大道88号驴妈妈科技园";	// 国旅-营业地址		--不变
	//protected static String GUOLV_lvMobile = "1010-6060";							// 国旅-电话			--不变
	//protected static String GUOLV_lvPostcode = "/";								// 国旅-邮编			--不变

	static{
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());			// TeamWithInTerritoryContractServiceImpl
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode());			// TeamOutboundTourismContractServiceImpl
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());	// CommissionedServiceAgreementServiceImpl
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.TRAVEL_ITINERARY.getCode());				// TravelItineraryContractServiceImpl (每个合同都会生成行程单)
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());						// AdvanceProductAgreementContractServiceImpl
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.getCode());				// PreSalesAgreementContractServiceImpl
		GUOLV_contractTemplate.add(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode());// DEST_COMMISSIONED_SERVICE_AGREEMENT
	}

	/**
	 * 根据“公司主体” (目前只针对子公司), 差异化信息处理
	 * @param travelContractVO	2015-6-15 16:57:16
	 */
	public void handleCompanyType(OrdTravelContract ordTravelContract,OrdOrder order, TravelContractVO travelContractVO){
		// 只处理 “国旅” （分公司）
		if (order.getCompanyType() != null
				&& COMPANY_TYPE_DIC.GUOLV.name().equals(order.getCompanyType().trim())
				&& GUOLV_contractTemplate.contains(ordTravelContract.getContractTemplate().toUpperCase())) {

			travelContractVO.setFilialeName(GUOLV_filialeName);	// 国旅-旅行社名称
			travelContractVO.setPermit(GUOLV_permit);			// 国旅-许可证编号
			travelContractVO.setStampImage(GUOLV_stampImage);	// 国旅-子公司专用章
		}
		// 如果当前合同的合同主体不是国旅且所属分公司也不是5大分公司时，默认设置为SH_FILIALE，
		if(StringUtils.isEmpty(travelContractVO.getFilialeName())) {
		    travelContractVO.setFilialeName(filialeNameMap.get(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode()));
		    travelContractVO.setPermit("L-SH-CJ00056");
		    travelContractVO.setStampImage("SH_ECONTRACT.png");
		}
	}


	  	/**
	  	 * 判断生成合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)
	  	 * @param travelContractVO
	  	 * @param order
	  	 * @return
	  	 */
		public ResultHandle checkSaveAgreementData(TravelContractVO travelContractVO, OrdOrder order) {
			LOG.info("=============预警邮件，进入判断生成合同字段方法中(委托服务协议，预付款)============");
			ResultHandle resultHandle = new ResultHandle();
			if(StringUtil.isEmptyString(String.valueOf(order.getOrderId()))){//订单号

				return sendEmailByEmptyField(order, "生成合同时(委托服务协议，预付款)，订单号OrderId："+String.valueOf(order.getOrderId()));
				//1.游玩人后置，且游玩人已锁定，且合同编号为空    2.游玩人非后置，且合同编号为空
			}else if((("Y").equals(order.getTravellerDelayFlag()) && ("Y").equals(order.getTravellerLockFlag()) && StringUtil.isEmptyString(travelContractVO.getContractVersion()))
					|| ((!("Y").equals(order.getTravellerDelayFlag())) && StringUtil.isEmptyString(travelContractVO.getContractVersion()))){//合同编号

				return  sendEmailByEmptyField(order, "生成合同时(委托服务协议，预付款)，合同编号ContractVersion："+travelContractVO.getContractVersion());
			}else if((("Y").equals(order.getTravellerDelayFlag()) && ("Y").equals(order.getTravellerLockFlag()) && StringUtil.isEmptyString(travelContractVO.getFilialeName()))
					|| ((!("Y").equals(order.getTravellerDelayFlag())) && StringUtil.isEmptyString(travelContractVO.getFilialeName()))){//旅行社

				return  sendEmailByEmptyField(order, "生成合同时(委托服务协议，预付款)，旅行社FilialeName："+travelContractVO.getFilialeName());
			}

			return resultHandle;
		}


		/**
	  	 * 判断修改合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)
	  	 * @param travelContractVO
	  	 * @param order
	  	 * @return
	  	 */
		public ResultHandle checkUpdateAgreementData(TravelContractVO travelContractVO, OrdOrder order) {
			LOG.info("=============预警邮件，进入判断生成合同字段方法中(委托服务协议，预付款)============");
			ResultHandle resultHandle = new ResultHandle();
			if(StringUtil.isEmptyString(String.valueOf(order.getOrderId()))){//订单号

				return sendEmailByEmptyField(order, "修改合同时(委托服务协议，预付款)，订单号OrderId："+String.valueOf(order.getOrderId()));
			}else if(StringUtil.isEmptyString(travelContractVO.getContractVersion())){//合同编号

				return  sendEmailByEmptyField(order, "修改合同时(委托服务协议，预付款)，合同编号ContractVersion："+travelContractVO.getContractVersion());
			}else if(StringUtil.isEmptyString(travelContractVO.getFilialeName())){//旅行社

				return  sendEmailByEmptyField(order, "修改合同时(委托服务协议，预付款)，旅行社FilialeName："+travelContractVO.getFilialeName());
			}

			return resultHandle;
		}


	    /**
	     * 判断生成合同中应有字段为空时，发送预警邮件(出境，境内，上海邮轮)
	     * @param travelContractVO
	     * @param order
	     * @return
	     */
		public ResultHandle checkSaveTravelContractData(TravelContractVO travelContractVO, OrdOrder order) {
			LOG.info("=============预警邮件，进入判断生成合同字段方法中(出境，境内，上海邮轮)============");
			ResultHandle resultHandle = new ResultHandle();
			if(StringUtil.isEmptyString(String.valueOf(order.getOrderId()))){//订单号

				return sendEmailByEmptyField(order, "生成合同时(出境，境内，上海邮轮)，订单编号OrderId："+String.valueOf(order.getOrderId()));
				//1.游玩人后置，且游玩人已锁定，且合同编号为空    2.游玩人非后置，且合同编号为空
			}else if((("Y").equals(order.getTravellerDelayFlag()) && ("Y").equals(order.getTravellerLockFlag()) && StringUtil.isEmptyString(travelContractVO.getContractVersion()))
					|| ((!("Y").equals(order.getTravellerDelayFlag())) && StringUtil.isEmptyString(travelContractVO.getContractVersion()))){//合同编号

				return  sendEmailByEmptyField(order, "生成合同时(出境，境内，上海邮轮)，合同编号ContractVersion："+travelContractVO.getContractVersion());
			}else if((("Y").equals(order.getTravellerDelayFlag()) && ("Y").equals(order.getTravellerLockFlag()) && StringUtil.isEmptyString(travelContractVO.getFilialeName()))
					|| ((!("Y").equals(order.getTravellerDelayFlag())) && StringUtil.isEmptyString(travelContractVO.getFilialeName()))){//旅行社

				return  sendEmailByEmptyField(order, "生成合同时(出境，境内，上海邮轮)，旅行社FilialeName："+travelContractVO.getFilialeName());
			}else if((("Y").equals(order.getTravellerDelayFlag()) && ("Y").equals(order.getTravellerLockFlag()) && StringUtil.isEmptyString(travelContractVO.getPermit()))
					|| ((!("Y").equals(order.getTravellerDelayFlag())) && StringUtil.isEmptyString(travelContractVO.getPermit()))){//旅行社业务经营许可证编号

				return  sendEmailByEmptyField(order, "生成合同时(出境，境内，上海邮轮)，经营许可证编号Permit："+travelContractVO.getPermit());
			}

			return resultHandle;
		}



		/**
		 * 判断修改合同中应有字段为空时，发送预警邮件(出境，境内，上海邮轮)
		 * @param travelContractVO
		 * @param order
		 * @return
		 */
		public ResultHandle checkUpdateTravelContractData(TravelContractVO travelContractVO, OrdOrder order) {
			LOG.info("=============预警邮件，进入判断修改合同字段方法中(出境，境内，上海邮轮)============");
			ResultHandle resultHandle = new ResultHandle();
			if (StringUtil.isEmptyString(String.valueOf(order.getOrderId()))) {//订单号

				return sendEmailByEmptyField(order, "修改合同时(出境，境内，上海邮轮)，订单编号OrderId：" + String.valueOf(order.getOrderId()));
			} else if (StringUtil.isEmptyString(travelContractVO.getContractVersion())) {//合同编号

				return sendEmailByEmptyField(order, "修改合同时(出境，境内，上海邮轮)，合同编号ContractVersion：" + travelContractVO.getContractVersion());
			}

			return resultHandle;
		}


		/**
		 * 判断行程单是否有效，无效则发送预警邮件
		 * @param travelContractVO
		 * @param order
		 * @param prodProduct
		 * @return
		 */
		public ResultHandle checkprodLineRouteVOList(TravelContractVO travelContractVO, OrdOrder order,ProdProduct prodProduct) {
			ResultHandle resultHandle = new ResultHandle();
			ResultHandleT<List<LineRoute>> resultHandleT = null;
			if(order.getLineRouteId() != null){
				//邮轮组合产品8L
				if (BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())) {
					LOG.info("============进入到邮轮行程单判断==============");
				   try {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("productId", prodProduct.getProductId());
						resultHandleT = lineRouteClientService.findLineRouteList(params);
						if (resultHandleT == null || CollectionUtils.isEmpty(resultHandleT.getReturnContent())) {
							return sendEmailByEmptyField(order, "邮轮组合产品无行程单resultHandleT");
						}else{
							LineRoute lineRoute = resultHandleT.getReturnContent().get(0);
							if (CollectionUtils.isEmpty(lineRoute.getLineRouteDetails())) {
								return sendEmailByEmptyField(order, "邮轮组合产品无行程单"+lineRoute.getLineRouteDetails().size());
							}
						}
					} catch (Exception e) {
						LOG.error("找不到对应的行程，产品ID：" + order.getProductId() + "，异常信息： {}", e);
						resultHandle.setMsg("找不到对应的行程，产品ID：" + order.getProductId() + "，异常信息： {}"+e);
					}
				} else {
					LOG.info("=================进入到线路行程判断================");
					//if(order.getLineRouteId() != null) {
						ResultHandleT<List<ProdLineRoute>> lineRouteResults = null;
						try {
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("lineRouteId", order.getLineRouteId());
							lineRouteResults = this.prodLineRouteClientService.findProdLineRouteAllList(params, true);
							if(lineRouteResults == null || CollectionUtils.isEmpty(lineRouteResults.getReturnContent())) {
								return sendEmailByEmptyField(order, "线路产品无行程单");
							}else{
								ProdLineRoute lineRoute = lineRouteResults.getReturnContent().get(0);
								if(!"Y".equals(lineRoute.getCancleFlag())){
									return sendEmailByEmptyField(order, "线路产品无效行程单");
								}
							}
						} catch (Exception e) {
							LOG.error("找不到对应的行程，行程ID：" + order.getLineRouteId()+ "，异常信息： {}", e);
							resultHandle.setMsg("找不到对应的行程，产品ID：" + order.getProductId() + "，异常信息： {}"+e);
						}
					//}
				}
			}

			return resultHandle;
		}



	  /**
	   * 合同或者行程单中应有字段丢失，发送预警邮件
	   * @param order
	   * @param emptyField
	   * @return
	   */
	  public ResultHandle sendEmailByEmptyField(OrdOrder order,String emptyField){
		  /*LOG.info("==========进入发送预警邮件=============");
	    ResultHandle resultHandle = new ResultHandle();
	    try{
		      EmailContent emailContent = new EmailContent();
		      emailContent.setSubject("合同中应有字段丢失！");
		      emailContent.setContentText("订单ID=【"+order.getOrderId()+"】,合同中字段【"+emptyField+"】为空！！！");
		      emailContent.setSendTime(new Date());
		      emailContent.setFromAddress("service@cs.lvmama.com");
		      emailContent.setFromName("驴妈妈旅游网");

		      String sendAddressList = "";
		      //国内游事业部
	          if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
	              sendAddressList = Constant.getInstance().getProperty("teamWithInContract");
	   	          emailContent.setToAddress(sendAddressList);
	          //出境游事业部
	          }else if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
	        	  sendAddressList = Constant.getInstance().getProperty("teamOutboundContract");
	     	      emailContent.setToAddress(sendAddressList);
	          }

	          LOG.info("========发送邮件的地址sendAddressList:"+sendAddressList);
	          vstEmailServiceAdapter.sendEmailDirect(emailContent);

		      resultHandle.setMsg("订单ID=" + order.getOrderId() + "预警邮件系统内部发送成功。");
		      LOG.info("==========sendEmailByEmptyField()方法,订单ID=" + order.getOrderId() + "预警邮件系统内部发送成功。");
	    }catch(Exception e){
	      LOG.error("Error occurred while sending email.=="+ExceptionUtils.getFullStackTrace(e), e);
	      resultHandle.setMsg(ExceptionUtils.getFullStackTrace(e)+"合同中应有字段丢失，发送预警邮件失败！");
	    }
	    return resultHandle;*/
		  return orderTravelElectricContactMailService.sendEmailByEmptyField(order, emptyField);
	  }
}
