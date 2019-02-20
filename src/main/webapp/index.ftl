<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>驴妈妈旅游网后台管理系统</title>
<meta name="keywords" content="" />
<meta name="description" content="" />
<link rel="stylesheet" href="/vst_order/css/ui-common.css">
<link rel="stylesheet" href="/vst_order/css/ui-components.css">
<link rel="stylesheet" href="/vst_order/css/ui-panel.css">
</head>
<body>
<!-- 顶部导航\\ -->
<div class="topbar">
	<a class="logo" href="/panel/"><h1>驴妈妈业务系统<small>业务系统</small></h1></a>
    <p class="top_list">
    	<a href="#">新增任务</a> |
    	<a href="#">我的公告</a> |
        <a href="#">我的任务</a> |
        <a href="#">我的消息</a>
    </p>
	<p>操作员：<span>chenlinjun</span> / <span>陈琳君</span>　[<a class="B" href="">修改密码</a>]　[<a class="B" href="">退出系统</a>]</p>
</div><!-- //顶部导航 -->

<!-- 边栏\\ -->
<div id="panel_aside" class="panel_aside">
	<span id="oper_aside" class="icon-arrow-left"></span>
    <span id="oper_set" class="icon-set"></span>
	<div class="aside_box">
		<ul id="aside_list" class="aside_list ul_oper_list">
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>基础设置</a>
            <ul class="ul_oper_list"> 
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/biz/category/findCategoryList.do"><span class="icon-tag"></span> 品类管理</a></li>
				<li class="oper_item"><a target="iframeMain" href="/vst_order/biz/district/findDistrictList.do"><span class="icon-tag"></span> 行政区域管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/biz/districtSign/findDistrictSignList.do"><span class="icon-tag"></span> 地理位置管理</a></li> 
			</ul>
			</li>
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>产品管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/prod/product/findProductList.do"><span class="icon-tag"></span> 标准产品管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/goods/house/findHouseControlProductList.do"><span class="icon-tag"></span> 房态控制</a></li>
			</ul>
			</li>
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>供应商管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/supp/supplier/findSupplierList.do"><span class="icon-tag"></span> 供应商管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/supp/suppContract/findSupplierContractList.do"><span class="icon-tag"></span> 供应商合同管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/supp/suppContractCheck/findSupplierContractCheckList.do"><span class="icon-tag"></span> 供应商合同审核</a></li>
			</ul>
			</li>
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>分销商管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/dist/distributor/findDistributorList.do"><span class="icon-tag"></span> 分销商管理</a></li>
			</ul>
			</li>
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>订单管理</a>
            <ul class="ul_oper_list">
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/productQuery/showOrderProductQueryList.do"><span class="icon-tag"></span> 新建订单</a></li>
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/showCombOrderProductQueryList.do"><span class="icon-tag"></span> 新建邮轮订单</a></li>
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/productQuery/ticket/showTicketQueryList.do"><span class="icon-tag"></span> 门票后台下单</a></li>
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/insurance/preQuery.do"><span class="icon-tag"></span>后台保险下单</a></li>
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/productQuery/showLineOrderProductQueryList.do"><span class="icon-tag"></span>线路 后台下单</a></li>
            	<li class="oper_item"><a target="iframeMain" href="/vst_order/ord/productQuery/visa/showVisaQueryList.do"><span class="icon-tag"></span>签证后台下单</a></li>
            	
            	
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/intoOrderMonitor.do"><span class="icon-tag"></span> 新订单监控</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/intoOrderMonitorShip.do"><span class="icon-tag"></span> 后台—计调</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/intoOrdCourierListingQuery.do"><span class="icon-tag"></span> 快递寄件清单查询</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordManualDistOrder/showManualDistOrderList.do"><span class="icon-tag"></span> 人工分单</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/intoOrderQuery.do"><span class="icon-tag"></span> 我的工作台</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ord/order/intoWorkStatus.do"><span class="icon-tag"></span> 员工工作状态查询</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordAuditConfig/showOrdAuditConfigList.do"><span class="icon-tag"></span> 员工活动组管理</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordFunction/findOrdFunctionList.do"><span class="icon-tag"></span> 显示权限管理</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordStatusGroup/queyOrdStatusGroupList.do"><span class="icon-tag"></span> 订单状态维护</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/orderAmountChange/showAmountChangeQueryList.do"><span class="icon-tag"></span> 订单价格修改</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/orderSettlementChange/showOrderSettlementList.do"><span class="icon-tag"></span> 订单-结算价修改</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/orderSettlementChange/showOrderSettlementApproveList.do"><span class="icon-tag"></span> 订单-结算价修改-审核</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_order/order/orderSettlementChange/showOrderSettlementHistoryList.do"><span class="icon-tag"></span> 订单-结算价修改-历史</a></li>
	         	<li class="oper_item"><a target="iframeMain" href="/vst_back/visa/approval/showVisaApprovalQueryList.do"><span class="icon-tag"></span> 签证材料审核列表</a></li>
	         
	         	<li class="oper_item">
	         		<a target="iframeMain" href="/vst_order/ebooking/fax/showEbkFaxList.do"><span class="icon-tag"></span> 传真管理</a>
	         	</li>
         		<li class="oper_item">
         			<a target="iframeMain" href="/vst_order/ebooking/faxRecv/findEbookingFaxRecvList.do?readUserStatus=N"><span class="icon-tag"></span> 
         			未读取的传真回传件</a>
         		</li>
         		<li class="oper_item">
         			<a target="iframeMain" href="/vst_order/ebooking/faxRecv/findEbookingFaxRecvList.do?readUserStatus=Y"><span class="icon-tag"></span> 
         			已读取的传真回传件</a>
         		</li>         			         	
			</ul>
			</li>
			<li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>短信管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordSmsSend/findOrdSmsSendList.do"><span class="icon-tag"></span>订单短信查询</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/order/ordSmsTemplate/findOrdSmsTemplateList.do"><span class="icon-tag"></span>模板管理</a></li>
			</ul>
			</li>
            <li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>EBOOKING</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ebooking/userManager/findEbookingSupplierList.do"><span class="icon-tag"></span>用户管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ebooking/announcement/findEbookingAnnouncementList.do"><span class="icon-tag"></span>公告管理</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/ebooking/apply/showEbookingSupplierList.do"><span class="icon-tag"></span>审核管理</a></li>
			</ul>
			</li>
			<li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>促销管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="_blank" href="/vst_order/prom/promotion/showPromotionMaintain.do"><span class="icon-tag"></span>促销设置</a></li>
	            <li class="oper_item"><a target="iframeMain" href="/vst_order/prom/promotion/showPromPromotionQueryList.do"><span class="icon-tag"></span>列表</a></li>
			</ul>	
			<li class="oper_item"><a target="iframeMain" ><span class="icon-tag"></span>邮轮管理</a>
            <ul class="ul_oper_list">
	            <li class="oper_item"><a target="_blank" href="/vst_order/cruise/cruiseProduct/showCruiseProduct.do"><span class="icon-tag"></span>邮轮产品</a></li>
	            <li class="oper_item"><a target="_blank" href="/vst_order/cruise/cruiseProduct/showFindCruiseStockList.do"><span class="icon-tag"></span>库存管理</a></li>
	            
			</ul>			
		</ul><!-- //ul aside_list -->
	</div>
</div><!-- //边栏 -->
<div id="panel_control" class="panel_control"></div>
<!-- 工作区\\ -->
<div id="panel_main" class="panel_main">
	<iframe id="iframeMain" name="iframeMain" src="" frameborder="0" style=" height:100%; background:#fff"></iframe>
	<div class="scoll-mask"></div>
</div><!-- //工作区 -->


<!-- 底部\\ -->
<div class="footer"></div><!-- //底部 -->
<script src="http://pic.lvmama.com/js/new_v/jquery-1.7.min.js"></script>
<script src="/vst_order/js/panel-custom.js"></script>
<script>
	
</script>

</body>
</html>
