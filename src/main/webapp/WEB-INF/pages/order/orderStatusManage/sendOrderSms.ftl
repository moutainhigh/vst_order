<form action="#" method="post" id="sendSmsDataForm">
	<input type="hidden" name="orderId" id="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderRemark" id="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="operation" id="operation" value="${RequestParameters.operation}">
	<input type="hidden" name="resourceStatus" id="resourceStatus"  value="${RequestParameters.resourceStatus}">
	
    <table class="p_table form-inline">
        <tbody>
        	<tr>
				<td class="p_label"><span class="notnull">*</span>联系人手机:</td>
                <td><input type="text" name="mobile" id="mobile" value="${RequestParameters.mobile!''}" readonly="readonly" >
                </td>
             </tr>
            
              <tr>
                <td class="p_label"><span class="notnull">*</span>短信内容：</td>
                <td id="testTd">
                 <textarea style="width: 330px; height: 58px;" id="smsContent" name="smsContent" onkeyup="checkRemarkLength()" >${content!''}</textarea>
                   <span class="fr" id="zsRemark">0/50字</span>
                </td>
            </tr> 
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">资源审核通过且发送短信</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">

$("#sendButton").bind("click",function(){
	
	var mobile = $("#mobile").val();
	if(mobile==""){
		alert("订单联系人手机号码不能为空!");
		return;
	}
	var smsContent = $("#smsContent").val();
	if(smsContent==""){
		alert("短信内容不能为空!");
		return;
	}
	var formData=$("#sendSmsDataForm").serialize()+"&cancelCode=''&cancleReasonText=''";
	
	var orderId = $("#orderId").val();
	var orderRemark = $("#orderRemark").val();
	var operation=$("#operation").val();
	var resourceStatus = $("#resourceStatus").val();
	
	var param={"orderId":orderId,"resourceStatus":resourceStatus,"orderRemark":orderRemark,"operation":operation,"mobile":mobile,"smsContent":smsContent};
       			
	$.ajax({
			   url : "/vst_order/order/orderStatusManage/updateOrderStatus.do",
			   data : param,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
			   		//var message=result.message;
			   		if(result.code=="success" ){
			   		  alert(result.message);
			   		  
			   		  parent.window.location.reload();
					 
			   		}else {
			   			 alert(result.message);
			   		}
			   }
			});	
	
});
function checkRemarkLength(){
    var messageContent=document.getElementById('content');
    var remarkLength=messageContent.value.length;
    if(remarkLength>50)
    {
    	var messageContentStr= $.trim($("#content").val());
    	$("#content").val( messageContentStr.substring(0,50));
        alert("短信长度小于等于50个字符!");
        $("#zsRemark").attr("style","color:red");
        remarkLength=messageContent.value.length;
    }else{
    	$("#zsRemark").attr("style","");
    
    }
    $("#zsRemark").html(remarkLength+"/50字");
}
    

//取消按钮事件
$("#closeButton").bind("click", function() {
	orderSendSmsDialog.close();
});
</script>



