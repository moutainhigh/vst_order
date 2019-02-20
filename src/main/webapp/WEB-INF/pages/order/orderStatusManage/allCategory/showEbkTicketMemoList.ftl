<#import "/base/spring.ftl" as s/>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>备注内容</th>
		    <th>备注人</th>
		    <th>备注时间</th>
        </tr>
    </thead>
    <tbody>
        <#if ebkTicketMemoList??>
			<#list ebkTicketMemoList as ticketPassMemo>
				<tr>
                    <td>${ticketPassMemo.content!''}</td>
                    <td>
                    	${ticketPassMemo.operatorName!''}
                    </td>
                    <td>
                    	<#if ticketPassMemo.createTime??>
		            		${ticketPassMemo.createTime?string('yyyy-MM-dd HH:mm:ss')}
		            	</#if>
                    </td>
				</tr>
			</#list>
		</#if>

    </tbody>
</table>


