
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderItemId" value="${RequestParameters.orderItemId}">
	<input type="hidden" name="orderRemark" value="${RequestParameters.orderRemark}">
    <table class="p_table form-inline">
        <tbody>
            <tr>
				<td class="p_label"><span class="notnull">*</span>选择接收组：</td>
                <td>
	                <select name="auditType" id="auditType" >
	                	<option value="">请选择</option>
	                	<#list auditTypeList as auditType>
	                		<#if auditType.code == 'FULL_HOUSE_AUDIT' && RequestParameters.categoryId == 1 && RequestParameters.buCode == 'DESTINATION_BU'>
	                			<option value="${auditType.code!''}">${auditType.cnName!''}</option>
	                		<#elseif auditType.code != 'FULL_HOUSE_AUDIT'>
	                			<option value="${auditType.code!''}">${auditType.cnName!''}</option>
	                		</#if>
	                	</#list>
	            	</select>
                	或
                	<span class="notnull"></span>员工编号：
                	<input type="text" name="receiver" id="receiver" >
                </td>
             </tr>
              <tr>
                <td class="p_label"><span class="notnull">*</span>通知内容：</td>
                <td>
                	<textarea  id="messageContent" name="messageContent" style="width: 330px; height: 58px;"  onkeyup="checkMsgContentLength()"></textarea>
                	<span style="width: 330px; text-align:right; display:block;" id="zsRemark">0/500字</span>
                </td>
            </tr> 
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editButton">保存</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>


<script>
var contactAddDialog,districtSelectDialog,selectSupplierDialog;

$("#editButton").bind("click",function(){
	
	var auditType=$("#auditType").val();
	var receiver=$("#receiver").val();
	var messageContent=$("#messageContent").val();
	if( auditType=='' && receiver=='' ){
		alert("选择接收组或者员工编号，请选择一项");
		return;
	}
	if( !(auditType=='' || receiver=='') ){
		alert("选择接收组或者员工编号，只能选择一项");
		return;
	}
	
	if( messageContent==''){
		alert("通知内容不可为空");
		return;
	}
		
	
	
	
	
	$.ajax({
	   url : "/vst_order/order/orderStatusManage/addMessage.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			addMessageDialog.close();
	   		 //parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	addMessageDialog.close();
});

function checkMsgContentLength(){
    var msgContent = document.getElementById('messageContent');
    var contentLength = msgContent.value.length;
    if(contentLength > 500) {
        $("#editButton").attr("disabled",true);
        $("#editButton").hide();
        $("#zsRemark").css("color","red");
    }else{
    	$("#editButton").removeAttr("disabled");
    	$("#editButton").show();
    	$("#zsRemark").css("color","");
    }
    $("#zsRemark").html(contentLength+"/500字");
}
</script>