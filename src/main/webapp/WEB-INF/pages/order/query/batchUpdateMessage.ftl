<#--页眉-->
<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
<head>
	<title>订单管理-我的工作台-批量处理预定通知</title> 
</head>
<body>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" method="post">
		<table class="s_table">
			<tbody>
				<tr>
                    <td class="w8 s_label"><span class="notnull">*</span>处理人：</td>
                    <td class="w10">
                    	<@s.formInput "monitorCnd.operatorName" 'class="w10" readonly="true"  required="true"'/>
                    </td>
                    <td  class="w9"><a href="javascript:updateOrderUser()">修改处理人</a> </td>
                    <td class="w8 s_label"><span class="notnull">*</span>产品编号：</td>
                    <td class="w15">
                    	<input type="text" id="productId" name="productId" value="${monitorCnd.productId}" required="true"/>
                    </td>
                </tr>
                <tr>
                	<td class="s_label">下单时间：</td>
                    <td>
                    	 <input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
                    	 - 
                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
                    	 	onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
                    	  	errorele="selectDate" name="createTimeEnd">
                    </td>
                    <td class="s_label">游玩/入住时间：</td>
                    <td>
                    	<@s.formInput "monitorCnd.visitTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>-
		                <@s.formInput "monitorCnd.visitTimeEnd" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>
                    </td>
                    <td class="w8 s_label">今天游玩：</td>
                    <td>
                    	<input type="checkbox" id="visitToday" name="visitToday" value="Y" <#if monitorCnd.visitToday && monitorCnd.visitToday=='Y'>checked</#if>/>是
                    </td>
                </tr>
			</tbody>
		</table>
		<input type="hidden" name="activityName" id="activityName" value="" />
		<input type="hidden" name="activityDetail" id="activityDetail" value="" />
		<div class="operate mt20" style="text-align:center">
        	<a class="btn btn_cc1" id="search_batch_button">查询</a>
        	<a class="btn btn_cc1" id="clear_batch_button">清空</a>
        	<a class="btn btn_cc1" id="batchUpdateMessage">完成处理</a>
		</div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	</form>
</div>
<br/>
<div id="resultMessageDiv">
	<#include "/order/query/resultMessage.ftl"/> 
</div>
<#--结果显示-->
<div id="result" class="iframe_content mt20"/>
<#--页脚-->
<#include "/base/foot.ftl"/>
<#--js脚本-->
<script src="/vst_order/js/workbench/workbench.js"></script>
<script>
	vst_pet_util.commListSuggest("#supplierName", "#supplierId",'/vst_order/ord/order/querySupplierList.do');
	var showSelectEmployeeDialog
	function updateOrderUser(){
		showSelectEmployeeDialog = 
				new xDialog("/vst_order/ord/order/showSelectEmployee.do",
				{},
				{title:"选择订单负责人",width:1000,height:600});
	}
	
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