
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="operation" value="${RequestParameters.operation}">
	<input type="hidden" name="infoStatus" value="${RequestParameters.infoStatus}">
	
	<input type="hidden" name="faxFlag" value="${orderItem.contentMap['fax_flag']}">
	
    <table class="p_table form-inline">
        <tbody>
           <tr>
				<td class="p_label"><span class="notnull"></span>传真方式：</td>
                <td>
	                <#if orderItem.contentMap['fax_flag']=='Y'>
	                	传真
	                 <#else>
	          			EBK      
	                 </#if>
                        
                </td>
             </tr>
             <#--
             <tr>
                <td class="p_label"><span class="notnull">*</span>供应商号码：</td>
                <td>
                	<input type="text" name="toFax" id="toFax" value="${ebkCertificate.toFax!''}" required>
                </td>
            </tr> 
            -->
              <tr>
             
                <td class="p_label"><span class="notnull"></span>备注：</td>
                <td>
                	 <textarea style="width:285px; height:120px;" id="messageContent" name="messageContent" onkeyup="checkRemarkLength()">${RequestParameters.orderRemark}</textarea>
                    <span class="fr" id="zsRemark">0/50字</span>
                </td>
            </tr> 
           
           
           
           
           
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editButton">信息审核通过且发送传真</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>


<script>
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
	
	
	
	
	$.ajax({
	   url : "/vst_order/order/orderStatusManage/updateOrderStatus.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		  	parent.window.location.reload();
   		}
	   }
	});	
	
	
	
	
	
});
$("#closeButton").bind("click", function() {
 	faxDialog.close();
 	/**
 	if("${RequestParameters.source}"!="noInfoPass"){
 		parent.window.location.reload();
 	}
 	*/
 	
 	
});
</script>