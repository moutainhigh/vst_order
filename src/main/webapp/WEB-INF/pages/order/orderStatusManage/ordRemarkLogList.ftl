<#import "/base/pagination1.ftl" as pagination>
	   <h2>订单备注明细</h2>
	   <table class="p_table table_center">
			<thead>
				<tr>
			        <th width="10%">操作人</th>
			        <th width="30%">操作时间</th>
			        <th width="70%">备注内容</th>
			    </tr>
			 </thead>
			<tbody>
				<#if pageParam??>
					<#if pageParam.items?? &&  pageParam.items?size &gt; 0>			
						<#list pageParam.items as ordRemarkLog>
							<tr>
								<td>${ordRemarkLog.createdUser}</td>
								<td>${ordRemarkLog.createdTime?string('yyyy-MM-dd HH:mm:ss')}</td>
								<td>${ordRemarkLog.content}</td>
							</tr>
						</#list>
					</#if>
				</#if>
			</tbody>
	  	</table>
		<#if pageParam??>
			<#if pageParam.items?? &&  pageParam.items?size &gt; 0>		  	
				<@pagination.paging pageParam></@pagination.paging>
			</#if>
		</#if>		