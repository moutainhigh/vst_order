
<form method="post" action='/vst_order/order/ordAuditConfig/updateOrdAuditConfig.do' id="dataForm">
<div class="iframe-content">   
    <div class="p_box">
    
	 <table class="p_table form-inline">
            <tbody>
					<#list ordAuditConfigInfoList as auditConfigInfo> 
					<#if auditConfigInfo?exists>
					
					<tr>
					<td colSpan ="3">
					<div align="center" >${auditConfigInfo.categoryName}</div>
					</td>
					</tr>
					
						<#list auditConfigInfo.ordFunctionInfoList as ordFunctionInfo> 
						
							 <#if ordFunctionInfo_index%3==0 >
								</tr>
								<tr>
							 </#if>  	 
								<td>
								
								
								<label class="checkbox">
								
									<#if ordFunctionInfo.checked=='false'>
										<input type="checkbox"   name="funcName" value="${ordFunctionInfo.ordFunctionId}"  disabled="true" >
									<#else>
										<input type="checkbox"  checked="true" name="funcName" value="${ordFunctionInfo.ordFunctionId}" disabled="true"  >
									</#if> 
									${ordFunctionInfo.functionName}
								</label>
								<input class="w3" type="text"  name="taskLimit"  value="${ordFunctionInfo.taskLimit}" disabled="true" >
								
								
								</td>
								
								
						</#list>	
							</#if> 				 
					</#list>	
					
					 
					
               
                    
            </tbody>
        </table>
		 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>
<p align="center">
<button  class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelButton">关闭</button>
</p>
<script>
	
	$("#cancelButton").bind("click",function(){
	
		updateOrdAuditCongigDialog.close();
						
	});
	
	
	
</script>