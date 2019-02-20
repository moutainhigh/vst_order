<#import "/base/spring.ftl" as s/>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
		    <th>物流公司</th>
		    <th>运单号</th>
		    <th>发货时间</th>
        </tr>
    </thead>
    <tbody>
			<tr>
                <td>
                	${ylPost.supplierOrderExpressName!''}
                </td>
                <td>${ylPost.supplierOrderExpressNum!''}</td>
                <td>
                </td>
			</tr>
    </tbody>
</table>


