<#import "/base/spring.ftl" as s/>
<#if msg ?? && msg != ''>
 <script type="text/javascript">
	alert("${msg!''}");
</script>
</#if>
<div style="overflow-x: auto; overflow-y: auto; height: 400px; width:740px;">
<table  class="p_table table_center mt20" align="center" width="720px" height="380px">
    <thead>
        <tr> 
            <th>预售券订单号</th>
            <th>预售券订单状态</th>
            <th>预售券ID</th>
            <th>预售券名称</th>
            <th>预售券码</th>
            <th>抵扣金额</th>
            <th>解绑状态</th>
        </tr>
    </thead>
    <tbody>
        <#list stampCodeList  as stampCodes> 
       		<tr>
                <td>
					${stampCodes.orderId!''}
                </td>
                <td>
					${stampCodes.orderStatus!''}
                </td>
                <td>
					${stampCodes.stampNo!''}
                </td>
                <td>
                    ${stampCodes.stampName!''}
				</td>
				<td>
                    ${stampCodes.serialNumber!''}
				</td>
				<td>
                    ${(stampCodes.price/100)?string('#0.00')}
				</td>
				<td>
                    ${stampCodes.bindStatus!''}
                </td>
            </tr>
        </#list>
     </tbody>
</table>
</div>