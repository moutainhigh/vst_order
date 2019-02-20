<form method="post" action='/vst_order/order/NewOrderConsole/manualDistOrder.do' id="dataForm">
<p align="center"> 

提示：${RequestParameters.resultMessage}
</p>
</form>
<p align="center">

<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="saveMustDistOrderButton">强制分单</button>	
&nbsp;&nbsp;
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelMustDistOrderButton">取消</button>

</p>

<script>

	$("#cancelMustDistOrderButton").bind("click",function(){
	
		mustDistOrderDialog.close();
						
	});
	
	$("#saveMustDistOrderButton").bind("click",function(){
		
		//var data={"groupMember":,"":,"",};
		var param={"manualDistOrder":"true","groupMember":"${RequestParameters.groupMember}","auditIdStatus":"${RequestParameters.auditIdStatus!''}","oneData":"${RequestParameters.oneData!''}"};
		   				
		$.ajax({
		   url : "/vst_order/order/NewOrderConsole/manualDistOrder.do",
		   data : param,
		   dataType:"JSON",
		   type:"POST",
		   success : function(result){
		   		if(result.code=="success"){
		   			$.alert(result.message);
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	
	
</script>