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
				        <th>处理时间</th>
				        <th>退款状态</th>
				        <th>备注</th>
			        </tr>
			    </thead>
			    <tbody>
			    
			         <#if orderRefundBatchDetailList?? && orderRefundBatchDetailList?size&gt;0>
                            <#list orderRefundBatchDetailList as orderRefundBatchDetail > 
                               <tr>
                                <td>${orderRefundBatchDetail.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                                <td>${orderRefundBatchDetail.itemAuditStatusStr!''}</td>
                                 <td>${orderRefundBatchDetail.memo!''}</td>
                                </tr>   
                                </#list>
                          </#if>
						    
				</tbody>
			</table>
		</div>
	<#include "/base/foot.ftl"/>
	</body>
</html>