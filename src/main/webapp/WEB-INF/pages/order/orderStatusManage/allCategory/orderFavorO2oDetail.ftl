<#import "/base/spring.ftl" as s/>
<#if msg ?? && msg != ''>
 <script type="text/javascript">
	alert("${msg!''}");
</script>
</#if>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>ID</th>
            <th>金额类型</th>
            <th>金额</th>           
        </tr>
    </thead>
    <tbody>
        <#list o2oItemList  as o2oItem> 
       		<tr>
                <td>
					${o2oItem.orderAmountItemId!''}
                </td>
                <td>
                    O2O门店专享
				</td>
				 <td>
					${o2oItem.itemAmount}
                </td>
            </tr>
        </#list>

     </tbody>
</table>
