<#import "/base/spring.ftl" as s/>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>操作账号</th>
		    <th>物流公司</th>
		    <th>运单号</th>
		    <th>发货时间</th>
        </tr>
    </thead>
    <tbody>
        <#if ebkTicketPostList??>
			<#list ebkTicketPostList as post>
				<tr>
                    <td>${post.operator!''}</td>
                    <td>
                    	${post.postCompany!''}
                    </td>
                    <td>${post.postNum!''}</td>
                    <td>
                    	<#if post.postTime??>
		            		${post.postTime?string('yyyy-MM-dd HH:mm:ss')}
		            	</#if>
                    </td>
				</tr>
			</#list>
		</#if>

    </tbody>
</table>


