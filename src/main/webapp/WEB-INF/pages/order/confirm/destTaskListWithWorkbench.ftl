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
	    <li <#if mainCheckedTab==null || mainCheckedTab=="MYTASK">class="active"</#if>><a name="tabChange" data="MYTASK">我的任务</a></li>
	    <li <#if mainCheckedTab?? && mainCheckedTab=="MYORDER">class="active"</#if>><a name="tabChange" data="MYORDER">我处理过的订单</a></li>
	    <li <#if mainCheckedTab?? && mainCheckedTab=="MYTASKFORPAY">class="active"</#if>><a name="tabChange" data="MYTASKFORPAY">我的已支付未处理后置订单</a></li>
	    <li <#if mainCheckedTab?? && mainCheckedTab=="MYDESTTASK">class="active"</#if>><a name="tabChange" data="MYDESTTASK">新版酒店工作台</a></li>
	</ul>
</div>
<div id="mainContent">
	<#--我的任务 -->
	<#if mainCheckedTab?? || mainCheckedTab=="MYDESTTASK">
	<br>
		<#include "/order/confirm/destTaskList.ftl"/>
	</#if>
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
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
			window.location.href = "/vst_order/ord/order/confirm/selectMainCheckedTab.do?mainCheckedTab=" + currentTab.attr("data")+"&operatorName="+$("#operatorName").val();
		})
	});
</script>
</body>
</html>