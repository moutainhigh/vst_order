<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>
</head>
<body>

<div class="iframe_header">
 <i class="icon-home ihome"></i>
    <ul class="iframe_nav">
        <li><a href="#">首页</a> &gt;</li>
        <li><a href="#">查询管理</a> &gt;</li>
        <li class="active">长隆数据查询</li>
    </ul>
</div>

<div class="iframe_search">
	<form method="post" action='/vst_order/ord/order/findchimelongordersbyvisittime.do' id="searchForm">
    <table class="s_table">
        <tbody>
            <tr>
                <td class="s_label">游玩时间<span style="color:#FF3300">（必填）</span>（输入格式2015-01-01）：</td>
                <td>&nbsp;如果查询的是某一天，开始结束输入相同时间。</td>
            </tr>
            <tr>
                <td class="s_label">开始时间：<span style="color:#FF3300">*</span>&nbsp;<input name="visitTimeStart" style="width:100px;" value="${visitTimeStart!''}"></input></td>
                <td class="s_label">结束时间：<span style="color:#FF3300">*</span>&nbsp;<input name="visitTimeEnd" style="width:100px;" value="${visitTimeEnd!''}"></input></td>
            </tr>
            <tr>
                <td class="s_label">下单时间（输入格式2015-01-01）：</td>
                <td>&nbsp;如果查询的是某一天，开始结束输入相同时间。开始结束时间必须同时输入或者不输入，不可只填一个。</td>
            </tr>
            <tr>
                <td class="s_label">开始时间：<input name="createTimeStart" style="width:100px;" value="${createTimeStart!''}"></input></td>
                <td class="s_label">结束时间：<input name="createTimeEnd" style="width:100px;" value="${createTimeEnd!''}"></input></td>
            </tr>
            <tr>
                <td class="s_label">长隆商品ID(用英语逗号分隔)：<span style="color:#FF3300">*</span></td>
                <td><input name="ids" style="width:800px;" value="${ids!''}"></input></td>
            </tr>
            <tr>
                <td class="s_label">&nbsp;</td>
                <td>查询数量最多返回1000条，如果实际数量可能超过1000条，请将商品ID分几次输入查询或者缩小查询的时间范围。</td>
            </tr>
            <tr>
                <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a>
                <span style="color:red;font-size:20px;">${errorMsg!''}</span>
                </td>
            </tr>
        </tbody>
    </table>	
	</form>
</div>
	
<!-- 主要内容显示区域\\ -->
<div class="iframe_content">   
    <div class="p_box">
	<table class="p_table table_center">
        <thead>
            <tr>
            <#list resultHead as head> 
        	<th>${head}</th>
        	</#list>
            </tr>
        </thead>
        <tbody>
			<#list resultList as result> 
			<tr>
			<#list resultHead as head>
        		<td>${result[head]}</td>
        	</#list>
			</tr>
			</#list>
        </tbody>
    </table>

</div><!-- div p_box -->
	
</div><!-- //主要内容显示区域 -->
<#include "/base/foot.ftl"/>
</body>
</html>

<script>
var categoryPropListDialog,categoryPropGroupsDialog,branchListDialog;
$(function(){

$("searchForm input[name='ids']").focus();
$("#search_button").bind("click",function(){
	    $("#search_button").innerHTML = "查询中，请稍后...";
		$("#searchForm").submit();
});
	


function confirmAndRefresh(result){
	if (result.code == "success") {
		pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
			$("#searchForm").submit();
		}});
	}else {
		pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
			//$.alert(result.message);
		}});
	}
}
});

</script>

