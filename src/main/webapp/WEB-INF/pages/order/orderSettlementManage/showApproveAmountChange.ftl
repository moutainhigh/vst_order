<div id="approveAmountChange" style="display:none" >
    <div class="box_content">
    	<form id="approveAmountChangeForm">
    	<input type="hidden" id="recordId" name="recordId">
    	<input type="hidden" id="orderIdLong" name="orderId">
         <table class="e_table form-inline ">
            <tbody>
                <tr>
                    <td class="w6 s_label">审批类型：</td>
                    <td class="w6"><input type="radio" name="status" value="APPROVE_PASSED" checked="checked">通过&nbsp;&nbsp;&nbsp;
                    <input type="radio" name="status" value="APPROVE_FAILURE">不通过
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">审核备注：</td>
                    <td class="w6"><textarea style="width:285px; height:120px;" id="approveRemark" name="approveRemark"></textarea></td>
                </tr>
               
            </tbody>
         </table>
        </form>
        
                
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="submitApproveAmountChangeForm">保存</button>
	&nbsp;&nbsp;
	<#--
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
	-->
</p>
    </div>
</div>
<script>
	var clickAble = true;
	$("#submitApproveAmountChangeForm").live("click",function(){
		if(!clickAble){
			return;
		}
		clickAble = false;
		$.confirm("确认审批?",function(){
			var loading = $.loading("正在提交");
			$.ajax({
				url : '/vst_order/order/orderSettlementChange/approveOrdSettlementChange.do',
			  	data : $("#approveAmountChangeForm").serialize(),
			  	type : 'GET',
			  	dataType : 'JSON',
			  	success : function(result){
			  		clickAble = true;
			  		loading.close();
			  		
			  		if(result.code == 'success'){
			  		
			  			approveDialog.close();
			  			$.alert(result.message,function(){
			  				$("#searchForm").submit();
			  			});
			  		}else {
			  			$.alert(result.message);
			  		}
			  		
			  	},
			  	error :function(msg){
			  		clickAble = true;
			  		loading.close();
			  		$.alert(result.message);
			  		/**
			  		$.alert(result.message,function(){
			  			$("#searchForm").submit();
			  		});
			  		*/
			  	}
		});
	  },function(){
			clickAble = true;
		});
	});
	
	
	
</script>