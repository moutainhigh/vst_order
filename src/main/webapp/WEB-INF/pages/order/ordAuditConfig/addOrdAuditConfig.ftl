
<form method="post" action='/vst_order/order/ordAuditConfig/addOrdAuditConfig.do' id="dataForm">
<div class="iframe-content">   
    <div class="p_box">
	 <table class="p_table form-inline">
            <tbody>
                
                
                <input type="hidden" name="updateOp" value="${updateOp!''}">
                
               
                <tr>
                <td class="p_label">
                <span class="notnull">*</span>后台用户名：</td>
					<td colspan=2>
					 <#if updateOp=="false">
						<input type="text" name="operatorName" id="operatorName" value="${ordAuditConfig.operatorName}"   required>
					<#else>
					 	<input type="text" name="operatorName" id="operatorName" value="${ordAuditConfig.operatorName}"  readOnly="true" required>
					 	
					 	<input type="hidden" name="ordAuditConfigId" value="${ordAuditConfig.ordAuditConfigId!''}">
					 	
					 </#if>  	
                   	</td>
                   	 </tr>
               
                 <tr>
					<td class="p_label"><span class="notnull">*</span>品类：</td>
					<td colspan=2>
						<select name="categoryId">
	                		<#list bizCategoryList as category>
                    			<#if category.categoryId == ordAuditConfig.categoryId>
                    				<option value="${category.categoryId!''}" selected="selected">${category.categoryName!''}</option>
                    			<#else>
                    			<option value="${category.categoryId!''}">${category.categoryName!''}</option>
                    			</#if>
                    		</#list>
                    	</select>
                    	
                   	</td>
                  </tr>
                  <tr>
                   	<td class="p_label"><span class="notnull">*</span>任务上限：</td>
					<td colspan=2>
						<input type="text" name="taskLimit" id="taskLimit" value="${ordAuditConfig.taskLimit}" required>
                   	</td>
                   </tr>
                   <tr>  	
                   	<td class="p_label"><span class="notnull">*</span>功能名：</td>
					<td colspan=2>
						<input type="text" name="ordFunctionName" id="ordFunctionName" onclick="openFunction()" value="${ordFunctionName}" required>
                   		<input type="hidden" name="ordFunctionId" id="ordFunctionId" value="${ordAuditConfig.ordFunctionId!''}">
                   	</td>
                   </tr>
                    
            </tbody>
        </table>
		 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>
<button class="pbtn pbtn-small" style="float:right;margin-top:20px;" id="saveButton">保存</button>
<script>
	$("#saveButton").bind("click",function(){
		//验证
		/**
		if(!$("#dataForm").validate().form()){
			return;
		}
		*/
		$.ajax({
		   url : "/vst_order/order/ordAuditConfig/addOrdAuditConfig.do",
		   data : $("#dataForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   			//$.alert(result.message);
		   			queryOrdAuditCongigDialog.location.reload();
		   			//alert(parent.location.href);
		   			
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	var add_OrdFunctionDialog;
	function openFunction(){
		
		add_OrdFunctionDialog = new xDialog("/vst_order/order/ordFunction/findSelectOrdFunctionList.do",{},{title:"选择功能",iframe:true,width:"700",height:"400"});
		
	}
	function onOrdFunction(params){
		if(params!=null){
			$("#ordFunctionId").val(params.ordFunctionId);
			$("#ordFunctionName").val(params.ordFunctionName);
			
		}
		add_OrdFunctionDialog.close();
	}
	
</script>