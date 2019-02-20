<#--页眉-->
<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
<head>
	<title>订单管理-我的工作台</title> 
</head>
<body>

<#--表单区域-->
<div class="iframe_search">
	<div style="text-align:right;">
		<p>当前状态：<span id="workStatusSpan" class="cc6 f14"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
	<div>
	<form id="searchForm1" action="/vst_order/ord/order/orderQueryListforPayed.do" method="post">
		<input type="hidden" name="checkedTab" value="MYTASKFORPAY"/>
		<input type="hidden" name="isorderPayed" value="Y"/>
		<input type="hidden" name="createOrderTimeInterval" value="48"/>
		<table class="s_table">
			<tbody>
				<tr>
                    <td class="w8 s_label"><span class="notnull">*</span>订单负责人：</td>
                    <td class="w10">
                    	<@s.formInput "monitorCnd.operatorName" 'class="w10" readonly="true"  required="true"'/>
                    </td>
                    <td  class="w9"><a href="javascript:updateOrderUser()">修改订单负责人</a> </td>
                </tr>
			</tbody>
		</table>
		
		<div class="operate mt20" style="text-align:center">
        	<a class="btn btn_cc1" id="search_button1">查询</a>
        </div>
	    
	</form>
</div>
<br/>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							后置订单已付款未锁定的订单
						</tr>
					</thead>
					<tbody>
						<#list resultPage.items as result>
							<#if result.auditSubtype == 'EMERGENCY'>
								<tr style="background-color: red;">
									<td>
										<#if result.objectType == 'ORDER'>
											是
										<#elseif result.objectType == 'ORDER_ITEM'>
											否
										</#if>
									</td>
									<td>
										<#if result.orderMonitorRst.isTestOrder=='Y'>是<#else>否</#if>
									</td>
									<td>
									
										<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.objectId}&objectType=${result.objectType}" target="_blank">
											${result.objectId}
										</a>
										<#if result.orderMonitorRst.guarantee == 'GUARANTEE'>
											<a title="该订单需要担保">#</a>
										</#if>
									</td>
									<td>
									<#if result.orderMonitorRst.stockFlag=='Y'>保留房</#if>
									</td>
									<td>${result.orderMonitorRst.productName} ${result.orderMonitorRst.suppGoodsName}</td>
									<td>${result.orderMonitorRst.supplierName}</td>
									<td>${result.orderMonitorRst.buyCount}</td>
									<td>${result.orderMonitorRst.createTime}</td>
									<td>${result.orderMonitorRst.visitTime}</td>
									<td>${result.orderMonitorRst.contactName}</td>
									<td>${result.orderMonitorRst.contactMobile!''}</td>
									<td>${result.orderMonitorRst.currentStatus}</td>
									<td>${result.auditTypeName}</td>
									<td>${result.auditStatusName}</td>
									<#if monitorCnd.bespokeOrder == null || monitorCnd.bespokeOrder == ''>
										<td>
											<ul>
											     <li style="list-style-type:none;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','5')" style="text-decoration:none;cursor:pointer;">5分钟</a>
											     </li>
											     <li style="list-style-type:none;margin-top: 5px;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','15')" style="text-decoration:none;cursor:pointer;">15分钟</a>
											     </li>
											     <li style="list-style-type:none;margin-top: 5px;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','30')" style="text-decoration:none;cursor:pointer;">30分钟</a>
											     </li>
											</ul>
										</td>
									</#if>
								<tr>
							<#else>		
								<tr>
									<td>
										<#if result.objectType == 'ORDER'>
											是
										<#elseif result.objectType == 'ORDER_ITEM'>
											否
										</#if>
									</td>
									<td>
										<#if result.orderMonitorRst.isTestOrder=='Y'>是<#else>否</#if>
									</td>
									<td>
										<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.objectId}&objectType=${result.objectType}" target="_blank">
											${result.objectId}
										</a>
										<#if result.orderMonitorRst.guarantee == 'GUARANTEE'>
											<a title="该订单需要担保">#</a>
										</#if>
									</td>
									<td>
									<#if result.orderMonitorRst.stockFlag=='Y'>保留房</#if>
									</td>
									<td>${result.orderMonitorRst.productName} ${result.orderMonitorRst.suppGoodsName}</td>
									<td>${result.orderMonitorRst.supplierName}</td>
									<td>${result.orderMonitorRst.buyCount}</td>
									<td>${result.orderMonitorRst.createTime}</td>
									<td>${result.orderMonitorRst.visitTime}</td>
									<td>${result.orderMonitorRst.contactName}</td>
									<td>${result.orderMonitorRst.contactMobile!''}</td>
									<td>${result.orderMonitorRst.currentStatus}</td>
									<td>${result.auditTypeName}</td>
									<td>${result.auditStatusName}</td>
									<#if monitorCnd.bespokeOrder == null || monitorCnd.bespokeOrder == ''>
										<td>
											<ul>
											     <li style="list-style-type:none;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','5')" style="text-decoration:none;cursor:pointer;">5分钟</a>
											     </li>
											     <li style="list-style-type:none;margin-top: 5px;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','15')" style="text-decoration:none;cursor:pointer;">15分钟</a>
											     </li>
											     <li style="list-style-type:none;margin-top: 5px;">
											     	<a href="javascript:void(0)" onclick="javascript:updateRemindTime('${result.auditId}','30')" style="text-decoration:none;cursor:pointer;">30分钟</a>
											     </li>
											</ul>
										</td>
									</#if>
                                    <td>
										<#if result.orderMonitorRst.travellerDelayFlag == 'Y'>
                                            是
										<#else>
                                            否
										</#if>
                                    </td>
                                    <td>
										<#if result.orderMonitorRst.travellerLockFlag == 'Y'>
             <font color="red">是</font>
										<#else>
                                            否
										</#if>
                                    </td>
								</tr>
							</#if>
						</#list>
					</tbody>
				</table>
				<#--分页标签-->
				<@pagination.paging resultPage/>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
	</#if>
</#if>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
<#--js脚本-->
<script type="text/javascript">var currentActivityDetail = '${monitorCnd.activityDetail}';</script>
<script src="/vst_order/js/workbench/workbench.js"></script>
<script>
	$("#search_button1").click(function (){
		$("#searchForm1").submit();
	});
	vst_pet_util.commListSuggest("#supplierName", "#supplierId",'/vst_order/ord/order/querySupplierList.do');
	var showSelectEmployeeDialog
	function updateOrderUser(){
		showSelectEmployeeDialog = 
				new xDialog("/vst_order/ord/order/showSelectEmployee.do",
				{},
				{title:"选择订单负责人",width:1000,height:600});
	}
	
	var distributorIdsStr_back = $("#distributorIdsStr").val();
	var superChannelIdsStr_back = $("#superChannelIdsStr").val();
	if(distributorIdsStr_back != null && distributorIdsStr_back != ""){
		var dArr_back = distributorIdsStr_back.split(",");
		for(var i = 0; i < dArr_back.length; i++){
		   $("#distributorId_" + dArr_back[i]).attr("checked", "checked");
		}
	}
	if(superChannelIdsStr_back != null && superChannelIdsStr_back != ""){
		var sArr_back = superChannelIdsStr_back.split(",");
		for(var i = 0; i < sArr_back.length; i++){
		   $("#channels_" + sArr_back[i]).attr("checked", "checked");
		}
	}
	//预约中的订单过滤条件
	$("#orderFiltercheck").click(function(){
		if($("#orderFiltercheck").is(':checked')){
		   	$("#orderFilter").val("N");
		}else{
			$("#orderFilter").val("Y");
		}
	});
	//加载页面，渠道已选择分销(distributorId=4)
	if(!document.getElementById("distributorId_4").checked){
		//默认展示，未选中就不可用
		$("#superChannel").find("input[type=checkbox]").attr("disabled","disabled");
	}else{
		$("#superChannel").find("input[type=checkbox]").removeAttr("disabled");
	}
	
	$("#distributorId_4").change(function(){
		if(document.getElementById("distributorId_4").checked){
			$("#superChannel").find("input[type=checkbox]").removeAttr("disabled");
		}else{
			$("#superChannel").find("input[type=checkbox]").attr("disabled","disabled");
			$("#superChannel").find("input[type=checkbox]").removeAttr("checked");
		}
	});
	
	//今天游玩过滤条件
	$("#visitToday").change(function(){
		if($("#visitToday").is(':checked')){
		   	var todayDateStr = new Date().format("yyyy-MM-dd");
		   	$("#visitTimeBegin").val(todayDateStr);
		   	$("#visitTimeEnd").val(todayDateStr);
		}else{
			$("#visitTimeBegin").val("");
		   	$("#visitTimeEnd").val("");
		}
	});
	
	//迪士尼订单过滤条件
	$("#disneyOrderCheckbox").change(function(){
		if($("#disneyOrderCheckbox").is(':checked')){
		   	$("#disneyOrder").val("Y");
		}else{
			$("#disneyOrder").val("");
		}
	});
	
	//迪士尼订单页面每8分钟刷新一次
	setInterval(function(){
		if($("#disneyOrderCheckbox").is(':checked')){
			//提交表单
			queryWorkBench();
		}
	},8*60*1000);
	
	//预约中的订单过滤条件
	$("#bespokeOrderCheckbox").change(function(){
		if($("#bespokeOrderCheckbox").is(':checked')){
		   	$("#bespokeOrder").val("Y");
		}else{
			$("#bespokeOrder").val("");
		}
	});
	
	
	
	Date.prototype.format = function(format){ 
		var o = { 
			"M+" : this.getMonth()+1, //month 
			"d+" : this.getDate(), //day 
			"h+" : this.getHours(), //hour 
			"m+" : this.getMinutes(), //minute 
			"s+" : this.getSeconds(), //second 
			"q+" : Math.floor((this.getMonth()+3)/3), //quarter 
			"S" : this.getMilliseconds() //millisecond 
		}
		
		if(/(y+)/.test(format)) { 
			format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
		}
		
		for(var k in o) { 
			if(new RegExp("("+ k +")").test(format)) { 
				format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length)); 
			} 
		} 
		return format; 
	}
</script>
</body>
</html>