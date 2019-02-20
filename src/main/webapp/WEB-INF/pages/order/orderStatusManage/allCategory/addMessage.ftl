<#import "/base/spring.ftl" as s/>
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="orderType" value="${RequestParameters.orderType}">
	<input type="hidden" id="isNew" value="${isNew!''}">
    <table class="p_table form-inline">
        <tbody>
        	 <tr>
                <td class="p_label" style="width:15%;"><span class="notnull">*</span>发送对象：</td>
                <td>
                 <select name="messageObject" id="messageObject" >
                 <#list messageObjectMap?keys as testKey>  
		             <option value="${testKey}" categoryId="${orderCategoryMap[testKey]}" <#if testKey == messageObjectValue>selected</#if>>${messageObjectMap[testKey]}</option>
				</#list> 
				</select>
				<#--
				 <@s.formCheckboxes1 "messageObject" messageObjectMap "" ""/>
				 -->
                </td>
            </tr> 
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
                <td class="p_label"><span class="notnull">*</span>通知类型：</td>
                <td id="auditSubTypeTd1">
                	<select name="auditSubType" id="auditSubType1" >
	                	<#list auditSubTypeList1 as auditSubType>
		                	<#if auditSubType.code != 'CANCEL_ORDER'>
		                		<option value="${auditSubType.code!''}">${auditSubType.cnName!''}</option>
		                	</#if>
	                		
	                	</#list>
	            	</select>
                </td>
                <td style="display: none;" id="auditSubTypeTd2">
                	<select name="auditSubType" id="auditSubType2" disabled="disabled">
	                	<#list auditSubTypeList2 as auditSubType>
		                	<#if auditSubType.code == 'CONFIRM_APPROVAL'>
                                <option value="${auditSubType.code!''}">${auditSubType.cnName!''}</option>
		                	<#elseif auditSubType.code != 'CANCEL_ORDER'>
		                		<option value="${auditSubType.code!''}">${auditSubType.cnName!''}</option>
		                	</#if>

	                	</#list>
	            	</select>
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
		
	
	
	//遮罩层
    var loading = pandora.loading("正在努力保存中...");	
	
	$.ajax({
	   url : "/vst_order/order/orderManage/addMessage.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			loading.close();
   			alert(result.message);
   			addMessageDialog.close();
	   		 //parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	addMessageDialog.close();
});

function initAuditSubType() {
    var categoryId = $("#messageObject option:selected").attr("categoryId");

    if (!categoryId) return;

    if ($("#isNew").val() == "true" && (categoryId == 1 || categoryId == 17)) {
        $("#auditSubTypeTd1").hide();
        $("#auditSubType1").attr("disabled", "disabled");

        $("#auditSubTypeTd2").show();
        $("#auditSubType2").removeAttr("disabled");
    } else {
        $("#auditSubTypeTd2").hide();
        $("#auditSubType2").attr("disabled", "disabled");

        $("#auditSubTypeTd1").show();
        $("#auditSubType1").removeAttr("disabled");
    }
}
$("#messageObject").change(function(){

		$("#receiver").val("");
		
		
		var messageObject=$("#messageObject option:selected").val();
		if(messageObject=='')
        {
       		return;
        }
        //console.info(messageObject);	//2024856744-ORDER_ITEM
		var orderId=messageObject.split("-")[0];
		var categoryId = $("#messageObject option:selected").attr("categoryId");
		if ($("#isNew").val() == "true" && (categoryId == 1 || categoryId == 17)) {
			$("#auditSubTypeTd1").hide();
			$("#auditSubType1").attr("disabled", "disabled");

			$("#auditSubTypeTd2").show();
            $("#auditSubType2").removeAttr("disabled");
		} else {
            $("#auditSubTypeTd2").hide();
            $("#auditSubType2").attr("disabled", "disabled");

            $("#auditSubTypeTd1").show();
            $("#auditSubType1").removeAttr("disabled");
		}
        var param="messageObject="+messageObject;
        $.ajax({
			url : "/vst_order/order/orderManage/findPrincipal.do",
		    data : param,
		    type:"POST",
		    dataType:"JSON",
		    success : function(data){
				if(data!=null){
			   		var userName=data.userName;
			   		$("#receiver").val(userName);
			    }
		    }
		   
		});	
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

$(function() {
   initAuditSubType();
});
</script>