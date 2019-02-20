<#import "/base/spring.ftl" as s/>
<#if msg ?? && msg != ''>
 <script type="text/javascript">
	alert("${msg!''}");
</script>
</#if>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>付费时间</th>
            <th>服务费订单号</th>
            <th>单价\元</th>
            <th>份数</th>
            <th>服务费总金额\元</th>
        </tr>
    </thead>
    <tbody>
        <#list reviseDateFeeRelatedOrderVoList  as reviseDateFeeRelatedOrderVo>
       		<tr>
                <td>${reviseDateFeeRelatedOrderVo.paymentTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                <td>${reviseDateFeeRelatedOrderVo.serviceFeeOrderId!''}</td>
                <td>${reviseDateFeeRelatedOrderVo.price/100}</td>
                <td>${reviseDateFeeRelatedOrderVo.quantity!''}</td>
                <td>${reviseDateFeeRelatedOrderVo.paymentTotalAmount/100}</td>
            </tr>
        </#list>
     </tbody>
</table>
