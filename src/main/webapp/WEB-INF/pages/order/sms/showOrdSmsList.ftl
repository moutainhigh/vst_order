<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<body>
		<div class="p_box">
			<table class="p_table table_center">
			    <thead>
			    	<tr>
				        <th>手机号</th>
				        <th>发送内容</th>
				        <th>发送人</th>
				        <th>发送时间</th>
				        <th>发送状态</th>
			        </tr>
			    </thead>
			    <tbody>
			    	<#if pageParam.items?? && pageParam.items?size &gt; 0>
					    <#list pageParam.items as ordSmsSend>
						    <tr>
						    	<td>${ordSmsSend.mobile!''}</td>
						    	<td>${ordSmsSend.content!''}</td>
						    	<td>${ordSmsSend.operate!''}</td>
						    	<td>${ordSmsSend.sendTime?string("yyyy-MM-dd HH:mm:ss")}</td>
						    	<td>
						    		<#if 'WAIT_SEND'==ordSmsSend.status>等待发送至短信服务平台</#if>
						        	<#if 'SUCCESS'==ordSmsSend.status>已发送至短信服务平台</#if>
						            <#if 'FAIL'==ordSmsSend.status>发送至短信服务平台失败</#if>
						            <#if 'ERROR'==ordSmsSend.status>发送至短信服务平台错误</#if>
						    	</td>
							</tr>
						</#list>
						<tr>
						  	<td colspan ="5">
						  		<div class="paging" >${pageParam.getPagination()}</div>
						  	</td>						
						</tr> 
			    	</#if>
				</tbody>
			</table>
		</div>
	<#include "/base/foot.ftl"/>
	</body>
</html>