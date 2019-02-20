<form method="post" action='/vst_order/ord/ordWechatTemplate/addOrdWechatTemplate.do' id="dataForm">
	<div class="iframe-content">
		<div class="p_box">
			<table class="p_table form-inline">
	        	<tbody>
	            	<tr>
		                <td class="p_label">发送节点：</td>
						<td>
		                     	<select onchange="disableMassageType(this)" name="sendNode" required>
		                     		<#list nodeList as node>
		                     			<option <#if messageType?exists &&  messageType==node.code> selected="selected" </#if> value="${node.code!''}">${node.cnName!''}</option>
			                  		</#list>
					        	</select>
		                </td>
	            	</tr>
	            	<tr>
		                <td class="p_label">模板ID：</td>
						<td>
							<input type="text" name="templateId" value="" required>					
		                </td>
	            	</tr>
	            	<tr>		                
		                <td class="p_label">消息类型：</td>
						<td>
		                     	<select name="messageCode" id="massageTypeSelect">
		                     		<#list wechatInfoTypeList as wechatInfoType>

												<if messageType="ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND">
													<option value="${wechatInfoType.code!''}" <#if wechatInfoType.code=='ORDER_STATUS_CHANGE'>selected="selected"</#if>>${wechatInfoType.cnName!''}</option>
												</if>

			                  		</#list>
					        	</select>
		                </td>
	            	</tr>
	            	<tr>
		                <td class="p_label">消息内容：</td>
						<td>

								<div  id="ORDER_STATUS_CHANGE">
									<table>
									<#list infoFields as infoField>
									    <tr>
											<#if infoField.type=='input' || infoField.type=='textarea'>
									            <td class="p_label">${infoField.cnName!''}：</td>
											<#else>

											</#if>
											<#if infoField.type=='input'>
									            <td>
									                <input type="text" name="wechatInfo.infoVars[${infoField_index}][${infoField.code}]"/>
									            </td>
											<#elseif infoField.type=='textarea'>
									            <td>
									                <textarea rows="3" cols="50"
									                          name="wechatInfo.infoVars[${infoField_index}][${infoField.code}]"></textarea>
									            </td>
											<#else>

											</#if>
									    </tr>
									</#list>
									</table>
								</div>





		                </td>	            	
	            	</tr>	            		                
	        	</tbody>
	    	</table>
		</div>
	</div>
	<input type="hidden" name="messageType" value="${messageType!}">
</form>



<p class="tc mt20 operate"><a class="btn btn_cc1" id="new_button">保存</a><p>
<script>
	$(document).ready(function(){

		//$("#HOTEL_TRAVEL_REMIND").hide();
		//alert($("#ORDER_STATUS_CHANGE").html())
	// $("#messageContent").append($("#ORDER_STATUS_CHANGE").html());
	});
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
				url : "/vst_order/ord/ordWechatTemplate/addOrdWechatTemplate.do",
				type : "post",
				dataType:"json",
				async: false,
				data : $("#dataForm").serialize(),
				success : function(result) {
					if(result.code=="success"){
						$.alert(result.message,function(){
			   				addDialog.close();
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


    function disableMassageType(currentObj) {
		addDialog.close();
		addDialog = new xDialog("/vst_order/ord/ordWechatTemplate/showAddOrdWechatTemplate.do?messageType="+$(currentObj).val(), {}, {title:"新增微信消息模板",width:700});
      /*  if ($(currentObj).val() == 'ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND') {
            $("#massageTypeSelect").val("HOTEL_TRAVEL_REMIND");
            $("#massageTypeSelect").attr("disabled", "disabled");
			$("#ORDER_STATUS_CHANGE").hide();
			$("#HOTEL_TRAVEL_REMIND").show();
        } else if ($(currentObj).val() == 'ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND') {
            $("#massageTypeSelect").val("TICKETS_TRAVEL_REMIND");
            $("#massageTypeSelect").attr("disabled", "disabled");
        }else{
			$("#massageTypeSelect").removeAttr("disabled");
			$("#HOTEL_TRAVEL_REMIND").hide();
           $("#ORDER_STATUS_CHANGE").show();
		}*/

    }
</script>