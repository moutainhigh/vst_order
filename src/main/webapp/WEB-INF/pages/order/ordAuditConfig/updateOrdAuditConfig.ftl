
<form method="post" action='/vst_order/order/ordAuditConfig/updateOrdAuditConfig.do' id="dataForm">
<div class="iframe-content">   
    <div class="p_box">
    
	 <table class="p_table form-inline">
            <tbody>
                <input type="hidden" name="updateOp" value="${RequestParameters.updateOp}">
                <input type="hidden" name="operatorNameArray" value="${RequestParameters.userName!''}">
					
					<#if ordAuditConfigList?size=0> 
						<tr>
						<td colSpan ="3">
						<div align="center" style="color:red">
						
						该用户尚未配置活动组的系统权限
	
						</div>
						</td>
						</tr>
							
					</#if>  	
					
					
					<#if ordAuditConfigList?size!=0> 
					<tr>
					<td colSpan ="3">
					<div align="center" >
					
					品类：
           			<select name="categoryIdArray" style="width : auto">
						<#list bizCategoryList as bizCategory>
							<#if bizCategory.categoryId??>
							<option value="${bizCategory.categoryId}">${bizCategory.categoryName}</option>
							</#if>
						</#list>
					</select>

					</div>
					</td>
					</tr>
					</#if>  	
					
					
					<#list ordAuditConfigList as ordAuditConfig> 
					
					 <#if ordAuditConfig_index%3==0 >
						</tr>
						<tr>
					 </#if>  	 
								<td>
								<label class="checkbox">
								<input type="checkbox" id="func_${ordAuditConfig_index}_checkbox" data=${ordAuditConfig_index}   name="ordAuditConfigList[${ordAuditConfig_index}].ordFunctionId" value="${ordAuditConfig.ordFunctionId}" />
								${ordAuditConfig.functionName}
								</label>
								
								<input class="w3" id="func_${ordAuditConfig_index}_text" data=${ordAuditConfig_index}   type="text"  name="ordAuditConfigList[${ordAuditConfig_index}].taskLimit"  value="20" maxLength="5" />
								<span id="func_${ordAuditConfig_index}_Error" class="notnull" style="display:none;">不能为空并且为大于0整数</span>
								
								</td>
												 
					</#list>	
					
					 
					
               
                    
            </tbody>
        </table>
		 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>
<p align="center">
	<#if RequestParameters.updateOp=='true'> 
	  <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="saveButton">保存</button>
	 &nbsp;&nbsp;
	  <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelButton">取消</button>
	<#else>
		<button  class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelButton">关闭</button>
	</#if>
</p>
						
<script>
	var validateResult=false;
	var errorIndex;
	//检查是否为正整数
	function   isUnsignedInteger(a)
	{
		 var r = /^\+?[1-9][0-9]*$/;
	    return r.test(a);
	}
	$("#saveButton").bind("click",function(){
		//验证
		//alert($("input[type='text']").length);
		var textNum=${ordAuditConfigList?size} ;
		var funcErrorIndex;
		$("input[type='text']").each(function(i){
		//alert($(this).attr("data"));
			if( (i+1) <=textNum){
				funcErrorIndex="func_"+$(this).attr("data")+"_Error";
			    document.getElementById(funcErrorIndex).style.display='none';
			}
			 
		}); 
		
		var errorIndexArray=new Array(); 
		var i=0;
		//alert($("input[type='checkbox']:checked").length);
		$("input[type='checkbox']:checked").each(function(j){
		
			var dataArr=$(this).attr("data");
			//alert($(this).attr("data"));
			if ( dataArr === undefined) { 
				return true;
			}
			var funcText="func_"+dataArr+"_text";
			
			var textValue=document.getElementById(funcText).value;
			var funcErrorIndex="func_"+$(this).attr("data")+"_Error";
			
			if(textValue=='' || !isUnsignedInteger(textValue)){
				validateResult=false;
				
				errorIndexArray[i++]=funcErrorIndex;
				//alert(errorIndexArray);
				
			}
		}); 
		$.each(errorIndexArray, function(key, val) {
		    
		   //alert(validateResult+"--"+val);
		   
		    document.getElementById(val).style.display='block';
		    
		});
		 
		 if(errorIndexArray.length>0){
		 	return;
		 }
		
		$.ajax({
		   url : "/vst_order/order/ordAuditConfig/updateOrdAuditConfig.do",
		   data : $("#dataForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   			alert(result.message);
		   			updateOrdAuditCongigDialog.close();
		   			//queryOrdAuditCongigDialog.location.reload();
		   			//alert(parent.location.href);
		   			//window.location.reload("/vst_order/order/ordStatusGroup/queyOrdStatusGroupList.do");
		   			
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	$("#cancelButton").bind("click",function(){
	
		updateOrdAuditCongigDialog.close();
						
	});
	
	
	
	
</script>