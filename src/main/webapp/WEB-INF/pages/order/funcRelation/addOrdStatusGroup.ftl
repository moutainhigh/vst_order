
<form method="post" action='/vst_order/order/ordFuncRelation/addOrdFuncRelationList.do' id="dataForm">
<div class="iframe-content">   
    <div class="p_box">
	 <table class="p_table form-inline">
            <tbody>
                <tr>
                
                <input type="hidden" name="updateOp" value="${updateOp!''}">
                
                <#if updateOp=="true">
                <td class="p_label"><span class="notnull">*</span>订单状态id：</td>
					<td colspan=2>
						<input type="text" name="statusGroupId" id="fileds" value="${statusGroupId}"  readOnly required>
                   	</td>
                </#if>  
                
					<td class="p_label"><span class="notnull">*</span>订单对应状态fields：</td>
					<td colspan=2>
						<input type="text" name="fileds" id="fileds" value="${fileds}" required>
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
		if(!$("#dataForm").validate().form()){
			return;
		}
		$.ajax({
		   url : "/vst_order/order/ordStatusGroup/addOrdStatusGroup.do",
		   data : $("#dataForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   			//$.alert(result.message);
		   			queryOrdStatuGroupDialog.location.reload();
		   			//alert(parent.location.href);
		   			
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	
	
</script>