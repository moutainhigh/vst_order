<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="certifId" value="${RequestParameters.certifId}">
	
    <table class="p_table form-inline">
        <tbody>
          
             <tr>
                <td class="p_label"><span class="notnull">*</span>传真号码：</td>
                <td>
                	<input type="text" name="toFax" id="toFax"  required="true" number="true" value="${suppFaxRule.fax!''}" >
                </td>
            </tr> 
           
             <tr>
                <td class="p_label"><span class="notnull"></span>凭证备注：</td>
                <td>
                	 <textarea style="width:285px; height:120px;" id="messageContent" name="messageContent" onkeyup="checkRemarkLength()">${orderItem.contentMap['fax_remark']}</textarea>
                    <span class="fr" id="zsRemark">0/50字</span>
                </td>
             </tr> 
           
           
           
           
           
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editButton">发送</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>


<script>
$("#closeButton").bind("click", function() {
 	forwardOrderFaxDialog.close();
});
function checkRemarkLength(){
    var messageContent=document.getElementById('messageContent');
    var remarkLength=messageContent.value.length;
    if(remarkLength>50)
    {
    	var messageContentStr= $.trim($("#messageContent").val());
    	$("#messageContent").val( messageContentStr.substring(0,50));
        alert("备注长度小于等于50个字符!");
        $("#zsRemark").attr("style","color:red");
        remarkLength=messageContent.value.length;
    }else{
    	$("#zsRemark").attr("style","");
    
    }
    $("#zsRemark").html(remarkLength+"/50字");
}
        
        
        
$("#editButton").bind("click",function(){
	
	
	//表单验证
	if(!$("#dataForm").validate().form()){
		return;
	}
			
	var toFax=$("#toFax").val();
	if(  toFax=='' ){
		alert("传真号码不能为空");
		return;
	}
	
	$.ajax({
	   url : "/vst_order/order/orderStatusManage/forwardSendOrderFax.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			forwardOrderFaxDialog.close();
   			
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
	
	
	
	
	
});

</script>