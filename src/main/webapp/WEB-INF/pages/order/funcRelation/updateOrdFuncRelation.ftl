

<form method="post" action='/vst_order/order/ordFuncRelation/addOrdFuncRelationList.do' id="searchForm">
       
<div class="iframe-content">   
    <div class="p_box">
	<table class="p_table table_center">
                <thead>
                    <tr>
                   <th>品类id</th>
                   <th>订单状态id</th>
                   <th>订单状态fields</th>
                    </tr>
                    
                </thead>
                
                <tbody>
                <input type="hidden" name="ordFunctionId" id="ordFunctionId" value="${ordFunctionId}" >
					<#list ordFuncRelationPOList as ordFuncRelationPO> 
					<tr>
					  <input type="hidden" name="ordFuncRelationList[${ordFuncRelationPO_index}].ordFunctionRelationId" value="${ordFuncRelationPO.ordFunctionRelationId}" >
					  <input type="hidden" name="ordFuncRelationList[${ordFuncRelationPO_index}].ordFunctionId" value="${ordFuncRelationPO.ordFunctionId}" >
					  
					  <input type="hidden" name="OrdStatusGroupList[${ordFuncRelationPO_index}].statusGroupId" value="${ordFuncRelationPO.statusGroupId}" >
					  
					<td>
					<input type="text" name="ordFuncRelationList[${ordFuncRelationPO_index}].categoryId"  value="${ordFuncRelationPO.categoryId}" required>
					</td>
					
					<td>
					<input type="text" onclick="openStatusGroupId(this,'${ordFuncRelationPO.statusGroupId}')" id="statusGroupId" name="ordFuncRelationList[${ordFuncRelationPO_index}].statusGroupId"  value="${ordFuncRelationPO.statusGroupId}"  required>
					</td>
					<td>
					<input type="text" name="OrdStatusGroupList[${ordFuncRelationPO_index}].fileds"  value="${ordFuncRelationPO.fileds}" readOnly required>
					</td>
					
					</tr>
					</#list>
                </tbody>
            </table>
			 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>
 <#if ordFuncRelationPOList?size != 0> 
					<button class="pbtn pbtn-small" style="float:right;margin-top:20px;" id="saveButton">保存</button>
</#if>
				

<script>
	$("#saveButton").bind("click",function(){
		
		$.ajax({
		   url : "/vst_order/order/ordFuncRelation/updateOrdFuncRelationList.do",
		   data : $("#searchForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   		 	updateordFuncRelationDialog.reload();
		   			$.alert(result.message);
		   			//updateordFuncRelationDialog.close();
				   
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	var statusGroupObj;
	function openStatusGroupId(obj,statusGroupId){
		
		statusGroupObj=obj;
		OrdStatusGroupDialog = new xDialog("/vst_order/order/ordStatusGroup/findOrdStatusGroupList.do",{},{title:"选择订单状态",iframe:true,width:"600",height:"400"});
		
	}
	
	function onSelectStatusGroupId(params){
		if(params!=null){
			statusGroupObj.value=params.statusGroupId;
			
			var nowInputIndex=$("input[type='text']").index(statusGroupObj);
			//alert($("input[type='text']")[nowInputIndex+1].value); 
			$("input[type='text']")[nowInputIndex+1].value=params.fileds;
			
			//var nextInputObj=$("input[type='text']").slice((nowInputIndex+1),(nowInputIndex+2));
		}
		OrdStatusGroupDialog.close();
	}

	
	
</script>
