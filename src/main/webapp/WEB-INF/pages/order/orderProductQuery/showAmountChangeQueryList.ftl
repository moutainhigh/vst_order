<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>
<#import "/base/spring.ftl" as s/>
</head>
<body>
<div class="iframe_header">
        <ul class="iframe_nav">
            <li><a href="#">首页</a> &gt;</li>
            <li><a href="#">订单管理</a> &gt;</li>
            <li class="active">订单价格修改</li>
        </ul>
</div>
<div class="iframe_content">
    <div class="p_box box_info">
	<form method="post" action='/vst_order/order/orderAmountChange/showAmountChangeQueryList.do' id="searchForm">
        <table class="s_table">
            <tbody>
                <tr>
                
                	<td class="s_label">订单对象：</td>
                    <td class="w18">
                    	<select class="w160" name="objectType"   id="objectType">
                    				<option value="">不限</option>
                    				<option value="ORDER" <#if RequestParameters.objectType == 'ORDER'>selected=selected</#if>>主订单</option>
                    				<option value="ORDER_ITEM" <#if RequestParameters.objectType == 'ORDER_ITEM'>selected=selected</#if> >子订单</option>
        				</select>
                    </td>
                    
                	<td class="s_label">订单号：</td>
                    <td class="w18"><input type="text" name="objectId" value=${objectId}></td>
                	<td class="s_label">审核状态：</td>
                    <td class="w18">
                    	<select class="w160" name="approveStatus">
                    				<option value="">不限</option>
                    				<option value="TOAPPROVE" <#if approveStatus == 'TOAPPROVE'>selected=selected</#if> >待审核</option>
                    				<option value="APPROVE_PASSED" <#if approveStatus == 'APPROVE_PASSED'>selected=selected</#if> >审核通过</option>
                    				<option value="APPROVE_FAILURE" <#if approveStatus == 'APPROVE_FAILURE'>selected=selected</#if> >审核失败</option>
        				</select>
                    </td>
                    <td class="s_label">所属产品经理：</td>
                    <td class="w18">
                              <input type="hidden" id="prodManagerId" name="prodManagerId" value="${prodManagerId}">
                              <input type="text" id="managerName" name="managerName" value="${managerName}" class="search" autocomplete="off">
                    </td>
                </tr>
                <tr>
               		<td class=" operate mt10">&nbsp;</td>
                	<td class=" operate mt10">
                   	<a class="btn btn_cc1" id="search_button">查询</a> 
                   	<a class="btn btn_cc1" id="clear_button">清空</a>
                    </td>
                </tr>
                
            </tbody>
        </table>	
		</form>
	</div>
<!-- 主要内容显示区域\\ -->
    <#if pageParam??>
    <#if pageParam.items?? &&  pageParam.items?size &gt; 0>
    <div class="p_box box_info">
    <table class="p_table table_center">
                <thead>
                    <tr>
                     <th >订单对象</th>
                	<th >订单号</th>
                    <th>申请人</th>
                    <th>审核人</th>
                    
                    <th>调整对象</th>
                    <th>增减类型</th>
                    <th>金额变化</th>
                    <th>订单总价变化</th>
                    <th>类型</th>
                    <th>申请原因</th>
                    <th>状态</th>
                    
                    <th>审核备注</th>
                    <th width="80px">创建时间</th>
                    <th width="80px">审核时间</th>
                    <th width="50px">操作</th>
                    </tr>
                </thead>
                <tbody>
					<#list pageParam.items as amountChange> 
					<tr>
					<td>
						<#if amountChange.objectType == 'ORDER'>
						主订单
						<#else>
						子订单
						</#if>

					</td>
					<td><a href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${amountChange.objectId}&objectType=${amountChange.objectType}" target="_blank">${amountChange.objectId}</a></td>
					<td>${amountChange.operatorName}</td>
					<td>${amountChange.approveOperator}</td>
					
					
					
					<td>
						<#if amountChange.objectType == 'ORDER'>
						订单总价
						<#else>
						商品单价
						</#if>
					</td>
					
					<td>
					<#if amountChange.formulas == 'PLUS'>
					增加
					<#else>
					减少
	                </#if>
					</td>
					<td>
					
					${amountChange.amountChangeDesc}元
					</td>
					
					<td>
						${amountChange.amount/100.0}元
					</td>
					
					<td>${amountChange.amountTypeName}</td>
					<td>${amountChange.reason}</td>
					<td>${amountChange.approveStatusName}</td>
					
					<td>${amountChange.memo}</td>
					<td><#if amountChange.createTime??>${amountChange.createTime?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
					<td><#if amountChange.approveTime??>${amountChange.approveTime?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
					<td class="oper">
                        <#if amountChange.approveStatus=='TOAPPROVE'>
                        	<#if amountChange.distributorId?? && amountChange.distributorId!='4'>
	                        	<@mis.checkPerm permCode="5893">
		                        <a href="javascript:void(0);" class="approve" 
		                        	objectId="${amountChange.objectId}" formulas="${amountChange.formulas}"
		                         objectType="${amountChange.objectType}" amountChangeId="${amountChange.amountChangeId}" >审核</a>
		                         </@mis.checkPerm>
		                    <#elseif amountChange.distributorId?? && amountChange.distributorId=='4' && amountChange.distributorCode='DISTRIBUTOR_B2B'>
		                         <@mis.checkPerm permCode="6116">
		                        <a href="javascript:void(0);" class="approve" 
		                        	objectId="${amountChange.objectId}" formulas="${amountChange.formulas}"
		                         objectType="${amountChange.objectType}" amountChangeId="${amountChange.amountChangeId}" >审核</a>
		                         </@mis.checkPerm>
		                     <#else>
		                      	<@mis.checkPerm permCode="6117">
			                        <a href="javascript:void(0);" class="approve" 
			                        	objectId="${amountChange.objectId}" formulas="${amountChange.formulas}"
			                         objectType="${amountChange.objectType}" amountChangeId="${amountChange.amountChangeId}" >审核</a>
		                         </@mis.checkPerm>
                        	</#if>
                         <#else>${amountChange.approveStatusName}
                         </#if>
                    </td>
					</tr>
					</#list>
                </tbody>
            </table>
				<#if pageParam.items?exists> 
					<div class="paging" > 
					${pageParam.getPagination()}
						</div> 
				</#if>
        
	</div><!-- div p_box -->
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关审批记录</div>
    </#if>
    </#if>
<!-- //主要内容显示区域 -->
</div>
<#include "/order/orderProductQuery/showApproveAmountChange.ftl"/>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
	var approveDialog;
	$("#search_button").click(function(){
		$("#searchForm").submit();
	});
	//清空
	$("#clear_button").bind("click",function(){
		window.location.href = "/vst_order/order/orderAmountChange/showAmountChangeQueryList.do";
	});
	
	$(".approve").click(function(){
	
		$("#amountChangeId").val($(this).attr("amountChangeId"));
		
		
		
		approveDialog = pandora.dialog({
	        width: 500,
	        title: "审核",
	        mask : true,
	        content: $("#approveAmountChange").html()
		});
	});
	
$(function(){
	$("#managerName").jsonSuggest({
			url:"/vst_order/ord/order/queryPermUserList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#prodManagerId").val(item.id);
			}
		});
});
		
</script>


