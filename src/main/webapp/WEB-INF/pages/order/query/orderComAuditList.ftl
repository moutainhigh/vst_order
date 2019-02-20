<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<body>
	    <div class="iframe_content">
	    	<#if pageData?? && pageData.items??>
					<#if pageData.items?size gt 0 >
				        <table class="p_table table_center">
				            <thead>
				                <tr>
				                     <th>活动名称</th>
									 <th>子活动名称</th>				                     
				                     <th>处理人</th>
				                     <th>系统自动过</th>	
				                     <th>活动状态</th>				                     
				                     <th>生成时间</th>
				                     <th>处理时间</th>
				                     <th>订单号</th>
				                     <th>订单类型</th>				                     
				                </tr>
				            </thead>
				            <tbody>
					            <#list pageData.items as comAudit> 
					            	<#if comAudit ??>
						                <tr>
											<td>
												${allAuditTypeMap[comAudit.auditType]}
											</td>
											<td>
				                                ${allAuditTypeMap[comAudit.auditSubtype]}
											</td>											
											<td>
												${comAudit.operatorName}
											</td>
											<td>
												
												<#if comAudit.auditFlag == "SYSTEM">
													是
												<#else>
				                                	否
												</#if>
											</td>
											<td>
					                     		<#list auditStatusList as auditStatus>
					                     			<#if comAudit.auditStatus==auditStatus.code>
					                     				${auditStatus.cnName!''}
					                     			</#if>
						                  		</#list>
											</td>																					
											<td>
												${(comAudit.createTime?string("yyyy-MM-dd HH:mm:ss"))!}
											</td>
											<td>
												${(comAudit.updateTime?string("yyyy-MM-dd HH:mm:ss"))!}
											</td>
											<td>
												${comAudit.objectId}
											</td>
											<td>
												<#if comAudit.objectType == "ORDER">
													订单活动
												<#elseif comAudit.objectType == "ORDER_ITEM">
				                                	子订单活动
												</#if>												
											</td>																																		
						                </tr>
					               </#if>
					             </#list>   
				            </tbody>
				        </table>
						<@pagination.paging pageData></@pagination.paging>
				<#else>
					<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关活动！</div>
				</#if>
			</#if> 
		</div>
		<#include "/base/foot.ftl"/>    	
	</body>
</html>