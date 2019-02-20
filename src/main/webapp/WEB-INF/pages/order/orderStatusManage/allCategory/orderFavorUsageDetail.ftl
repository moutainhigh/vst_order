<#import "/base/spring.ftl" as s/>
<#if msg ?? && msg != ''>
 <script type="text/javascript">
	alert("${msg!''}");
</script>
</#if>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>优惠券ID</th>
            <th>优惠券批次号</th>
            <th>优惠券名称</th>
            <th>优惠券金额</th>
            <th>有效期</th>
            <th>使用范围</th>
            <th>适用平台</th>
        </tr>
    </thead>
    <tbody>
        <#list markCouponUsageList  as markCouponUsage> 
       		<tr>
                <td>${markCouponUsage.couponCodeId!''}</td>
                <td>${markCouponUsage.couponId!''}</td>
                <td>${markCouponUsage.couponName!''}</td>
                <td>${markCouponUsage.amount/100}</td>
                <td>${markCouponUsage.startTime?string('yyyy-MM-dd')}到${markCouponUsage.endTime?string('yyyy-MM-dd')}</td>
				<td>${markCouponUsage.range!''}</td>
				<td>${markCouponUsage.platform!''}</td>
            </tr>
        </#list>

     </tbody>
</table>
