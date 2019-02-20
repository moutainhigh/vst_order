
<form method="post" action='/vst_order/order/ordFuncRelation/addOrdFuncRelationList.do' id="dataForm">
<div class="iframe-content">   
    <div class="p_box">
	 <table class="p_table form-inline">
            <tbody>
                <tr>
					<td class="p_label"><span class="notnull">*</span>功能id：</td>
					<td colspan=2>
						<input type="text" name="ordFunctionId" id="ordFunctionId" value="${RequestParameters.ordFunctionId}" readOnly>
                   	</td>
                </tr>
                  <tr>
					<td class="p_label"><span class="notnull">*</span>功能名字：</td>
					<td colspan=2>
						<input type="text" name="functionName" id="ordFunctionId" value="${RequestParameters.functionName}" readOnly>
                   	</td>
                </tr>
				<tr>
					<td class="p_label"><span class="notnull">*</span>订单对应状态id：</td>
					<td colspan=2>
						<input type="text" onclick="openStatusGroupId()" name="statusGroupId" id="statusGroupId" value="${statusGroupId}" required>
                   	</td>
                </tr>
                <tr>
					<td class="p_label"><span class="notnull">*</span>订单对应状态fields：</td>
					<td colspan=2>
						<input type="text" name="fileds" id="fileds" value="" required>
                   	</td>
                </tr>
                 <tr>
					<td class="p_label"><span class="notnull">*(注意是id值)</span>品类id：</td>
					<td colspan=2>
						<input type="text" name="categoryId" id="categoryId" value="" required>
                   	</td>
                </tr>
                
                    
            </tbody>
        </table>
		 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>

<button class="pbtn pbtn-small" style="float:right;margin-top:20px;" id="saveButton">保存</button>
<button class="pbtn pbtn-small" style="float:right;margin-top:20px;" id="cancelButton">取消</button>
<script>

	$("#cancelButton").bind("click",function(){
	
		addOrdFuncRelationDialog.close();
						
	});
	
	$("#saveButton").bind("click",function(){
		//验证
		if(!$("#dataForm").validate().form()){
			return;
		}
		$.ajax({
		   url : "/vst_order/order/ordFuncRelation/addOrdFuncRelation.do",
		   data : $("#dataForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   			$.alert(result.message);
		   			//updateordFuncRelationDialog.reload();
		   			//addOrdFuncRelationDialog.close();
		   			
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	
	var add_OrdStatusGroupDialog;
	function openStatusGroupId(){
		
		add_OrdStatusGroupDialog = new xDialog("/vst_order/order/ordStatusGroup/findOrdStatusGroupList.do",{},{title:"选择订单状态",iframe:true,width:"600",height:"400"});
		
	}
	
	function onSelectStatusGroupId(params){
		if(params!=null){
			$("#statusGroupId").val(params.statusGroupId);
			$("#fileds").val(params.fileds);
			
		}
		add_OrdStatusGroupDialog.close();
	}
	
</script>