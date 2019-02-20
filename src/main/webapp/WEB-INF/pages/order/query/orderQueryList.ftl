<#--页眉-->
<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>
	<title>订单管理-我的工作台</title>
	<#include "/base/head_meta.ftl"/>
</head>
<body>

<#--页面导航-->
<div class="iframe_header">
	<i class="icon-home ihome"></i>
	<ul class="iframe_nav">
		<li><a href="#">首页</a> &gt;</li>
		<li><a href="#">订单管理</a> &gt;</li>
		<li class="active">我的工作台</li>
	</ul>
</div>
<div class="price_tab">
	<ul class="J_tab ui_tab">   
	    <li <#if checkedTab==null || checkedTab=="MYTASK">class="active"</#if>><a name="tabChange" data="MYTASK">我的任务</a></li>
	    <li <#if checkedTab?? && checkedTab=="MYORDER">class="active"</#if>><a name="tabChange" data="MYORDER">我处理过的订单</a></li>
	    <li <#if checkedTab?? && checkedTab=="MYTASKFORPAY">class="active"</#if>><a name="tabChange" data="MYTASKFORPAY">我的已支付未处理后置订单</a></li>
	    <li <#if checkedTab?? && checkedTab=="MYDESTTASK">class="active"</#if>><a name="tabChange" data="MYDESTTASK">新版酒店工作台</a></li>
	</ul>
</div>
<div id="mainContent">
	<#--我的任务 -->
	<#if checkedTab==null || checkedTab=="MYTASK">
		<#include "/order/query/myTaskList.ftl"/>
	</#if>
	<#if checkedTab?? && checkedTab=="MYORDER">
		<br/>
		<#include "/order/query/myOrderList.ftl"/>
	</#if>
	<#if checkedTab?? && checkedTab=="MYTASKFORPAY">
		<br/>
		<#include "/order/query/myTasklist2.ftl"/>
	</#if>
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
<#--js脚本-->
<script type="text/javascript">var currentActivityDetail = '${monitorCnd.activityDetail}';</script>
<script src="/vst_order/js/workbench/workbench.js"></script>
<script>
	vst_pet_util.commListSuggest("#supplierName", "#supplierId",'/vst_order/ord/order/querySupplierList.do');
	var showSelectEmployeeDialog
	function updateOrderUser(){
		showSelectEmployeeDialog = 
				new xDialog("/vst_order/ord/order/showSelectEmployee.do",
				{},
				{title:"选择处理人",width:1000,height:600});
	}
	
	$(function() {
		//tab切换
		$("a[name=tabChange]").click(function(){
			var currentTab = $(this);
			/*
			$("a[name=tabChange]").each(function() {
				if($(this).attr("data") == currentTab.attr("data")) {
					$(this).parent().addClass('active');
				} else {
					$(this).parent().removeClass();
				}
			});
			$.post("/vst_order/ord/order/selectTabInWorkBench.do",{"checkedTab":currentTab.attr("data")},function(returnData){
				if(returnData){
					$("#mainContent").html(returnData);
				}
			});
			*/
			//切换到新版酒店工作台
			if(currentTab.attr("data")!=null && currentTab.attr("data")=="MYDESTTASK"){
				window.location.href = "/vst_order/ord/order/confirm/selectMainCheckedTab.do?mainCheckedTab=" + currentTab.attr("data")+"&operatorName="+$("#operatorName").val();
				return;
			}
			window.location.href = "/vst_order/ord/order/selectTabInWorkBench.do?checkedTab=" + currentTab.attr("data")+"&operatorName="+$("#operatorName").val();
		})
	});
</script>
</body>
</html>