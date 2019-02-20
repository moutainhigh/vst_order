<!DOCTYPE html>
<html>
<head>

</head>
<body>
<style>
	td{
		word-break:keep-all;
		white-space:nowrap;
		overflow:hidden;
		text-overflow:ellipsis;
	}
</style> 
<div class="iframe-content">   
<div class="p_box">
<#if orderAttachmentList?size gt 0>
	<input id="orderId" value="${orderAttachmentList[0].orderId}_${contactEmail!''}" style="display:none;"/>
</#if>
<table class="p_table table_center">
    <thead>
        <tr>
              <tr>
              	<#--<th nowrap="nowrap">订单编号</th>-->
				<th nowrap="nowrap">附件编号</th>
				<th nowrap="nowrap">附件类型</th>
				<th nowrap="nowrap">附件名称</th>
				<th nowrap="nowrap">附件备注</th>
				
				
				 <#if  !RequestParameters.sourceType?? && RequestParameters.sourceType!='notice'> 
				 <th nowrap="nowrap">凭证确认类型</th>
				<th nowrap="nowrap">系统说明</th>
				 </#if>	  
				 <th nowrap="nowrap">创建时间</th>
				<th nowrap="nowrap">操作</th>
			</tr>
        </tr>
    </thead>
    <tbody>
    	<#list orderAttachmentList as result>
		    <tr>
			    <#--<td>${result.orderId!''}</td>-->
			    <td>${result.fileId!''}</td>
			    <td>
			    <#if  RequestParameters.sourceType?? && RequestParameters.sourceType=='notice'> 
			    ${result.fileTypeCN!''}
			     <#else>
			    ${result.attachmentTypeCN!''}
			    </#if>	  
				</td>
				<td>
					<div style='width:150px;overflow: hidden;text-overflow:ellipsis;height:18px;' title='${result.attachmentName!''}'  onmouseout='this.style.width="150px"'>
						${result.attachmentName!''}
					</div>
				</td>
				<td>
					<div style='width:90px;overflow: hidden;text-overflow:ellipsis;height:18px;' title='${result.memo!''}'  onmouseout='this.style.width="90px"'>
						${result.memo!''}
					</div>
				</td>
				
				
				  <#if  !RequestParameters.sourceType?? && RequestParameters.sourceType!='notice'> 
				  <td>${result.confirmTypeCN!''}</td>
				    <TD>
				    <div style='width:90px;overflow: hidden;text-overflow:ellipsis;height:18px;' title='${result.logContent!''}'  onmouseout='this.style.width="90px"'>
							${result.logContent!''}
						</div>
					</TD>
				 </#if>	
				 
				 <td>${result.createTime?string('yyyy-MM-dd HH:mm:ss')} </td>
				
				<td>
				<#if result.fileId??> 
					<a href="/vst_back/pet/ajax/file/downLoad.do?fileId=${result.fileId}" title="下载订单附件">下载</a>
				</#if>	  
				<#if contactEmail??>
					<a href="javascript:void(0)" id="sendNotice" title="重发出团通知书">重发</a>
				</#if>
				</td>
			</tr>
	   </#list>
	</tbody>
</table>
</div>
</div>
</body>
<script>
$(function(){
	$("#sendNotice").bind("click",function(){
		var orderIds= $("#orderId").val();
		if(orderIds=='')return;
			//遮罩层
		var loading = pandora.loading("正在努力发送中...");		
		$.ajax({
			url : "/vst_order/order/orderShipManage/sendNoticeRegiment.do",
			type : "post",
			dataType:"JSON",
			data : {"orderIds":orderIds,"oneData":"false"},
			success : function(result) {
				if (result.code == "success") {
					loading.close();
					alert(result.message);
				}else {
					loading.close();
					$.alert(result.message);
				}
			}
		});
	});
});	
</script>
</html>
