<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>
</head>
<body>

<div class="iframe_content">
<!-- 主要内容显示区域\\ -->
    <#if pageParam??>
    <#if pageParam.items?? &&  pageParam.items?size &gt; 0>
    <div class="p_box box_info">
    <table class="p_table table_center">
                <thead>
                   <tr>
                    <th >订单对象</th>
                	<th >订单号</th>
                    
                    <th>调整对象</th>
                    <th>增减类型</th>
                    <th>金额变化</th>
                    <th>订单总价变化</th>
                    <th>类型</th>
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
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关记录</div>
    </#if>
    </#if>
<!-- //主要内容显示区域 -->
</div>
<#include "/order/orderProductQuery/showApproveAmountChange.ftl"/>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
	
</script>


