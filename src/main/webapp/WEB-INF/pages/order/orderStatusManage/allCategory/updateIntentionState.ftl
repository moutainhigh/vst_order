<form method="post" action='/vst_order/order/ordFuncRelation/addOrdFuncRelationList.do' id="updateForm">
       
<div class="iframe-content">   
    <div class="p_box">
    <lable>
       <input type="hidden" id="intenetionId" value="${intentionOrderId}">
       <input type="hidden" name="updateState"  value="index">
	   <input type="hidden" name="IntentionState" value="2"/>   
    </lable>        	 
	</div><!-- div p_box -->
	<div class="p_box" id="reasonDiv" hidden="true">
	<h4>取消原因<span style="color:red;font-size:14px;"></span></h4>
	<lable>
	   <input type="hidden" id="intenetionId" value="${intentionOrderId}">
       <input type="radio" name="cancelReason" value="0"  />资&nbsp;源&nbsp;不&nbsp;确&nbsp;定&nbsp;&nbsp;
	   <input type="radio" name="cancelReason" value="1"  />用&nbsp;户&nbsp;通&nbsp;知&nbsp;取&nbsp;消&nbsp;&nbsp;
	   <input type="radio" name="cancelReason" value="2"  />信&nbsp;息&nbsp;不&nbsp;通&nbsp;过&nbsp;&nbsp;	
	   <input type="radio" name="cancelReason" value="3"  />其&nbsp;它&nbsp;取&nbsp;消&nbsp;&nbsp;
	</lable>
	</div>
</div><!-- //主要内容显示区域 -->
</form>
<button class="pbtn pbtn-small"  id="saveButton">保存</button>				

<script>
	showReason();
	$("#saveButton").bind("click",function(){
		var intentionOrderId = $("#intenetionId").val();
		var intentionState= $('input[name="IntentionState"]').val();
		var cancelReason = $('input[name="cancelReason"]:checked').val();
		var updateState =$('input[name="updateState"]').val();
		//取消时弹框提示
		$.confirm("是否取消意向单?",function(){ 		
		$.ajax({
		   url : "/vst_order/ord/order/updateIntentionState.do",
		   data : {intentionOrderId:intentionOrderId,intentionState:intentionState,cancelReason:cancelReason,updateState:updateState},
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){
		   			var  stateId = 'state_'+intentionOrderId;
		   			$("#"+stateId).html(result.stateView);
		   			$.alert(result.message);
		   			updateIntentionDialog.close();
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});
		});	

						
	});
	
	function showReason(){
	   	var reason = $("#reasonDiv");
	   	reason.show();
	}
</script>
