<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单关系监控</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>

<#--页面导航-->
 <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> ></li>
            <li><a href="#">订单管理</a> ></li>
            <li class="active">苏宁订单</li>
        </ul>
        <br>
 </div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" name="searchForm" action="/vst_order/suning/order/queryOrderList.do" method="post">
		<table class="s_table2 form-inline">
            <tbody>
                <tr>
                    <td class="w10 s_label">苏宁订单号：</td>
                    <td>
                    	<input class="w10" id="orderCode" name="orderCode" value="${orderCode!''}" />
                    </td>
                 </tr>
            </tbody>
        </table>


        <p class="tc mt20 operate">
        <a class="btn btn_cc1" id="search_button">查询</a>
        <a class="btn btn_cc1" id="clear_button">清空</a>
        </p>
    </div>
    </form>
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<#--结果显示-->
			<div class="iframe_content">
		        <table class="p_table table_center">
		            <thead>
		                <tr>
		                    <th nowrap="nowrap">苏宁订单号</th>
		                    <th nowrap="nowrap">驴妈妈订单号</th>
		                    <th nowrap="nowrap">产品名称</th>
		                    <th nowrap="nowrap">订单状态</th>
		                    <th nowrap="nowrap">订单创建时间</th>
		                    <th nowrap="nowrap">订单取消时间</th>
		                    <th nowrap="nowrap">订单出游日期</th>
		                    <th nowrap="nowrap">对接状态</th>
		                    <th nowrap="nowrap">有无凭证</th>
		                    <th nowrap="nowrap">退款申请时间</th>
		                    <th nowrap="nowrap">退款结果</th>
		                    <th nowrap="nowrap">备注</th>
						</tr>
					</thead>
					<tbody>
	            	<#list resultPage.items as result>
		                <tr>
		                    <td>${result.orderCode!''}</td>
		                    <td><a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId!''}&amp;callid=" target="_blank">${result.orderId!''}</a></td>
							<td>${result.productName!''}</td>
						    <td>${result.orderStatus!''}</td>
	                    	<td><#if result.orderCreateTime??>${result.orderCreateTime?string('yyyy-MM-dd HH:mm:ss') !''}</#if></td>
							<td><#if result.orderCancelTime??>${result.orderCancelTime?string('yyyy-MM-dd HH:mm:ss') !''}</#if></td>
							<td><#if result.orderVisitTime??>${result.orderVisitTime?string('yyyy-MM-dd') !''}</#if></td>
							<td>${result.invokeInterfaceStatus!''}</td>
							<td>${result.hasPasscode!''}</td>
							<td>${result.attr1!''}</td>
							<td>${result.refundStatus!''}</td>
							<td>${result.remark!''}</td>
		                </tr> 
	                </#list> 
	            </tbody>
	        </table>	
			<@pagination.paging resultPage/>
	        </div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
	</#if>
</#if>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<#--js脚本-->
<script type="text/javascript">
$("#search_button").bind("click",function(){
	$("#searchForm").submit();
});
//清空
$("#clear_button").bind("click",function(){
	window.location.href = "/vst_order/suning/order/findOrderList.do";
});
</script>
