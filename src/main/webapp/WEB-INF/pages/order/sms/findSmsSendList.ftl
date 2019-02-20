<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<div class="iframe_header">
    <ul class="iframe_nav">
        <li><a href="javascript:void(0);">短信管理</a> &gt;</li>
        <li class="active">订单短信查询</li>
    </ul>
</div>
<div class="iframe_search">
<form method="post" id="searchForm" action='/vst_order/order/ordSmsSend/findOrdSmsSendList.do'>
	<table class="s_table">
    	<tbody>
    		<tr>
            	<td class="s_label">手机号码：</td>
                <td class="w18"><input type="text" name="mobile" id="mobile" value="${mobile!''}"></td>

                <td class="s_label">订单号：</td>
                <td class="w18"><input type="text" name="orderId" id="orderId" value="${orderId!''}"></td>

                <td class="s_label">短信内容：</td>
                <td class="w18"><input type="text" name="content" id="content" value="${content!''}"></td>

				<td class="s_label">发送人：</td>
                <td class="w18"><input type="text" name="operate" id="operate" value="${operate!''}"></td>
			</tr>
			<tr>
			    <td class="s_label">发送状态：</td>
                <td class="w18">
                	<select id="status" name="status">
                		<option value="" <#if ''==status>selected</#if>>全部</option>
                		<option value="WAIT_SEND" <#if 'WAIT_SEND'==status>selected</#if>>等待发送至短信服务平台</option>
                		<option value="SUCCESS" <#if 'SUCCESS'==status>selected</#if>>已发送至短信服务平台</option>
                		<option value="FAIL" <#if 'FAIL'==status>selected</#if>>发送至短信服务平台失败</option>
                		<option value="ERROR" <#if 'ERROR'==status>selected</#if>>发送至短信服务平台错误</option>
			        </select>
			    </td>
		        <td class="s_label">发送时间：</td>
                <td class="w18">
					<input type="text" name="beginDate" value="${beginDate!''}" errorEle="selectDate" class="W8 Wdate" id="d4321" onFocus="WdatePicker({readOnly:true,maxDate:'#F{$dp.$D(\'d4322\',{d:0});}'})" />
		        </td>
		        <td class="w18">
					<input type="text" name="endDate" value="${endDate!''}" errorEle="selectDate" class="W8 Wdate" id="d4322" onFocus="WdatePicker({maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})" />
		        </td>
		    </tr>
            <tr>
                <td class="operate mt10"><a class="btn btn_cc1" id="search_button">查询</a></td>
            </tr>
            <tr>
            	<td class="s_label">已选条件：</td>
            	<td>
            		<#if mobile!=''>手机号码：${mobile}</#if>
            		<#if orderId!=''>订单号码：${orderId}</#if>
            		<#if content!=''>短信内容：${content}</#if>
            		<#if operate!=''>发送人：${operate}</#if>
            		<#if status!=''>发送状态：
            			<#if 'WAIT_SEND'==status>等待发送至短信服务平台</#if>
            			<#if 'SUCCESS'==status>已发送至短信服务平台</#if>
            			<#if 'FAIL'==status>发送至短信服务平台失败</#if>
            			<#if 'ERROR'==status>发送至短信服务平台错误</#if>
            		</#if>
            	</td>
            </tr>
        </tbody>
    </table>
</form>
</div>
<!-- 主要内容显示区域\\ -->
<div class="iframe_search">
<#if pageParam.items?? && pageParam.items?size &gt; 0>
	<div class="p_box">
	<table class="p_table table_center">
    <thead>
    	<tr>
	        <th>订单号</th>
	        <th>手机号</th>
	        <th>发送内容</th>
	        <th>发送人</th>
	        <th>发送时间</th>
	        <th>发送状态</th>
	        <th>操作</th>
        </tr>
    </thead>
    <tbody>
    <#list pageParam.items as ordSmsSend>
    <tr>
    	<td><a id="" href="javascript:void(0);" onclick="javascript:showOrderDetail(${ordSmsSend.orderId})">${ordSmsSend.orderId!''}</a></td>
    	<td>${ordSmsSend.mobile!''}</td>
    	<td>${ordSmsSend.content!''}</td>
    	<td>${ordSmsSend.operate!''}</td>
    	<td>${ordSmsSend.sendTime?string("yyyy-MM-dd HH:mm:ss")}</td>
    	<td>
    		<#if 'WAIT_SEND'==ordSmsSend.status>等待发送至短信服务平台</#if>
        	<#if 'SUCCESS'==ordSmsSend.status>已发送至短信服务平台</#if>
            <#if 'FAIL'==ordSmsSend.status>发送至短信服务平台失败</#if>
            <#if 'ERROR'==ordSmsSend.status>发送至短信服务平台错误</#if>
    	</td>
		<td class="oper">
			<#if ordSmsSend.status == "ERROR" || ordSmsSend.status == 'FAIL' >
				<a href="javascript:void(0);" class="reSend" status="${ordSmsSend.status}" smsId="${ordSmsSend.smsId}">重发</a>
			</#if>	
		</td>
	</tr>
	</#list>
</tbody>
</table>
<#if pageParam.items?exists> 
	<div class="paging" >${pageParam.getPagination()}</div> 
</#if>  
</div><!-- div p_box -->
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关短信记录，重新输入条件查询！</div>
    </#if>
</div><!-- //主要内容显示区域 -->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

$("#orderId").blur(function(){
   var orderId=$("#orderId").val();
   if(orderId.length>0){
   	$("input[name='beginDate']").attr("value","");
   }
});

$("#orderId").blur(function(){
   var orderId=$("#orderId").val();
   if(orderId.length>0){
   	$("input[name='endDate']").attr("value","");
   }
});


$(function(){
//查询
$("#search_button").bind("click",function(){
	if(!$("#searchForm").validate().form()){
		return false;
	}
	$(".iframe-content").empty().append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'> 加载中...</div>");
	$("#searchForm").submit();
});

//重发
$("a.reSend").bind("click",function(){
	if ($(this).attr("status")=='SUCCESS') {
		alert("已发送成功的，不能重发！");
		return;
	}
	var msg = "确认短信重新发送 ？";
	var smsId = $(this).attr("smsId");
	$.confirm(msg, function() {
		$.get("/vst_order/order/ordSmsSend/reSendSms.do?smsId="+smsId, function(result){
			confirmAndRefresh(result);
    	});
    });
});

//确定并刷新
function confirmAndRefresh(result){
	if (result.code == "success") {
		pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
			location.reload();
		}});
	} else {
		pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
			$.alert("发送失败");
		}});
	}
}
});
function showOrderDetail(orderId) {
	//productSelectDialog = new xDialog("/vst_order/order/orderStatusManage/showOrderStatusManage.do?orderId="+orderId,{}, {title:"订单详情",iframe:true,width:"1000",height:"800"});
	window.open("/vst_order/order/ordCommon/showOrderDetails.do?orderId=" + orderId);
}
</script>
