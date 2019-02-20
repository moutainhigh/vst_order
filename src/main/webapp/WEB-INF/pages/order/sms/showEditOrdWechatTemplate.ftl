<form method="post" action='/vst_order/ord/ordWechatTemplate/editOrdWechatTemplate.do' id="dataForm">
	<input type="hidden" name="id" value="${ordWechatTemplate.id!''}"/>
	<input type="hidden" name="sendNode" value="${ordWechatTemplate.sendNode!''}"/>
    <input type="hidden" name="messageCode" value="${ordWechatTemplate.messageCode!''}"/>
	<div class="iframe-content">
		<div class="p_box">
			<table class="p_table form-inline">
	        	<tbody>
	            	<tr>
		                <td class="p_label">发送节点：</td>
						<td>
		                     	<select name="sendNode1" disabled="disabled">
		                     		<#list nodeList as node>
		                     			<option value="${node.code!''}" <#if ordWechatTemplate ?? && ordWechatTemplate.sendNode==node.code>selected="selected"</#if>>${node.cnName!''}</option>
			                  		</#list>
					        	</select>
		                </td>
	            	</tr>
	            	<tr>
		                <td class="p_label">模板ID：</td>
						<td>
							<input type="text" name="templateId"  readonly="readonly" value="${ordWechatTemplate.templateId!''}">				
		                </td>
	            	</tr>
	            	<tr>		                
		                <td class="p_label">消息类型：</td>
						<td>
		                     	<select name="messageCode1" disabled="disabled">
		                     		<#list wechatInfoTypeList as wechatInfoType>
		                     			<option value="${wechatInfoType.code!''}" <#if ordWechatTemplate ?? && ordWechatTemplate.messageCode==wechatInfoType.code>selected="selected"</#if>>${wechatInfoType.cnName!''}</option>
			                  		</#list>
					        	</select>
		                </td>
	            	</tr>
	            	<tr>
		                <td class="p_label">消息内容：</td>
						<td>
							<table class="p_table form-inline">
					        	<tbody>
		                     		<#list infoFields as infoField>
						            	<tr>
						            		<#if infoField.type=='input' || infoField.type=='textarea'>
							                	<td class="p_label">${infoField.cnName!''}：</td>
							                <#else>
							                
							                </#if>
											<#if infoField.type=='input'>
												<td>
													<input type="text" name="wechatInfo.infoVars[${infoField_index}][${infoField.code}]" value="${fieldDetailMap[infoField.code]}"/>
												</td>
											<#elseif infoField.type=='textarea'>
												<td>
													<textarea rows="3" cols="50" name="wechatInfo.infoVars[${infoField_index}][${infoField.code}]">${fieldDetailMap[infoField.code]}</textarea>
												</td>
											<#else>
													
											</#if>
						            	</tr>
			                  		</#list>					        	
					            </tbody>
					         </table>							
		                </td>	            	
	            	</tr>	            		                
	        	</tbody>
	    	</table>
		</div>
	</div>
</form>
<p class="tc mt20 operate"><a class="btn btn_cc1" id="new_button">保存</a><p>
<script>
//新增
$("#new_button").bind("click",function(){
	//验证
	if(!$("#dataForm").validate().form()){
		return;
	}
	$("#new_button").hide();
	var msg = '确认保存吗 ？';	
	$.confirm(msg,
		function(){
			$.ajax({
				url : "/vst_order/ord/ordWechatTemplate/editOrdWechatTemplate.do",
				type : "post",
				dataType:"json",
				async: false,
				data : $("#dataForm").serialize(),
				success : function(result) {
					if(result.code=="success"){
						$.alert(result.message,function(){
			   				editDialog.close();
			   				window.location.reload();
			   			});
					}else {
						$.alert(result.message);
						$("#new_button").show(); 
			   		}
			   },
			   error : function(){
				   $("#new_button").show(); 
			   }
			});		
		},
		function(){
			$("#new_button").show(); 
		}
	);
});	
</script>