
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderItemId" value="${orderItem.orderItemId}">
	<input type="hidden" name="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="operation" value="${RequestParameters.operation}">
	<input type="hidden" name="infoStatus" value="${RequestParameters.infoStatus}">
    <table class="p_table form-inline">
        <tbody>
           <tr>
				<td class="p_label"><span class="notnull"></span>凭证方式：</td>
                <td>
	               <#if faxFlag=='Y' && mailFlag='Y'>
	                	普通传真/邮件
	                <#elseif faxFlag='Y' && mailFlag!='Y'>
                        普通传真
	                <#elseif faxFlag!='Y' && mailFlag='Y'>
	                	邮件
	                 <#else>
	          			EBK
	                 </#if>
                </td>
             </tr>
             <tr>
               <#if faxFlag=='Y'>
                <td class="p_label"><span class="notnull">*</span>供应商号码：</td>
                <td>
                	<input type="text" name="toFax" id="toFax" value="${suppFaxRule.fax!''}" >
                </td>
                </#if>
            </tr> 
            <tr>
                <td class="p_label"><span class="notnull">*</span>凭证类型：</td>
                <td>
				<#if cancelCertConfirmStatus==''>
					  <#if faxFlag=='Y'>
							<#if order.orderStatus!='CANCEL'>
							<input type="radio" name="certifType" value="CONFIRM" />新订单
							<input type="radio" name="certifType" value="CHANGE" />修改单
							<#else>
							<input type="radio" name="certifType" value="CONFIRM" />新订单
							<input type="radio" name="certifType" value="CHANGE" />修改单
							<input type="radio" name="certifType" value="CANCEL" />取消单
							</#if>
					 <#else>
						<input type="radio" name="certifType" value="CHANGE" checked="checked"/>修改单
					 </#if>
                 <#else>
                     <input type="hidden" name="memo" value="拒绝重发">
          			<input type="radio" name="certifType" value="CANCEL" checked="checked"/>取消重发
                 </#if>
                </td>
            </tr>
           <tr>
               <td class="p_label"><span class="notnull"></span>子订单号：</td>
               <td>
			   ${ordOrderItemIdList}
               </td>
           </tr>
		  <tr>
			<td class="p_label"><span class="notnull"></span>凭证备注：</td>
			<#if faxFlag=='Y'>
			<td>
				 <textarea style="width:285px; height:120px;" id="messageContent" name="messageContent" onkeyup="checkRemarkLength()">${orderItem.contentMap['fax_remark']}</textarea>
				<span class="fr" id="zsRemark">0/500字</span>
			</td>
			<#elseif mailFlag='Y'>
			<td>
				 <textarea style="width:285px; height:120px;" id="messageContent" name="messageContent" onkeyup="checkRemarkLength()">${orderItem.contentMap['mail_remark']}</textarea>
				<span class="fr" id="zsRemark">0/500字</span>
			</td>
			<#else>
				<td>
					<textarea style="width:285px; height:120px;" id="messageContent" name="messageContent" onkeyup="checkRemarkLength()">${orderItem.contentMap['fax_remark']}</textarea>
					<span class="fr" id="zsRemark">0/500字</span>
				</td>
			</#if>
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
function checkRemarkLength(){
    var messageContent=document.getElementById('messageContent');
    var remarkLength=messageContent.value.length;
    if(remarkLength>500)
    {
    	var messageContentStr= $.trim($("#messageContent").val());
    	$("#messageContent").val( messageContentStr.substring(0,500));
        alert("备注长度小于等于500个字符!");
        $("#zsRemark").attr("style","color:red");
        remarkLength=messageContent.value.length;
    }else{
    	$("#zsRemark").attr("style","");
    
    }
    $("#zsRemark").html(remarkLength+"/500字");
}

$("#editButton").bind("click",function(){

    $("#editButton").attr("disabled", true);
    var toFax=$("#toFax").val();
	var certifType=$("#certifType").val();
	if(  toFax=='' ){
		alert("供应商传真号码不能为空");
		return;
	}
	if($('input[name="certifType"]:checked').length<=0)
	{
		alert("凭证类型不能为空");
		return;
	}

	$.ajax({
	   url : "/vst_order/order/orderStatusManage/manualSendOrderFax.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
            $("#editButton").attr("disabled", false);
   			alert(result.message);
   			sendOderFaxDialog.close();
            window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	

});

$("#closeButton").bind("click", function() {
 	sendOderFaxDialog.close();
});

</script>