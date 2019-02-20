<form action="#"  enctype="multipart/form-data" method="post" target="ajaxUpload"  id="dataForm">
	<input type="hidden" name="orderId" id="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderRemark" id="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="operation" id="operation" value="${RequestParameters.operation}">
	<input type="hidden" name="attachmentName" id="attachmentName" value="">
	
    <table class="p_table form-inline">
        <tbody>
            <tr>
				<td class="p_label"><span class="notnull">*</span>选择确认方式：</td>
                <td>
	                <select name="confirmType" id="confirmType" >
	                	<#list certificateTypeList as certificateType>
	                		<option value="${certificateType.code!''}">${certificateType.cnName!''}</option>
	                	</#list>
	            	</select>
                	
                </td>
             </tr>
               <tr> 
               		<td class="p_label"><span class="notnull">*</span>请选择文件：</td>
		            <td class="querytd">
		               <input type="hidden" id="fileId"/>
		               <input type="file" id="uploadFile" style="width:60%" serverType="ORDER_ATTACHMENT"/>
		            </td>
		        </tr> 
		         <tr>
		        	<td class="p_label"><span class="notnull"></span>提示信息：</td>
		        	<td class="querytd"><span id="attachmentDesc"></span></td>
		        </tr>

        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="saveButton">保存</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>




<script type="text/javascript">
	//定义公共参数
	var basePath = "/vst_order/pet";
</script>
<script src="/vst_order/js/file/ajaxUpload.js"></script>
<script>

	//上传按钮事件
	$("#uploadFile").fileUpload({
		onSubmit:function() {
			$("#uploadFile").attr("disabled", true);
			$("#attachmentDesc").html('<font color="red">正在使劲的上传中，请稍后......</font>');
		},
		onComplete:function(file,dt){
			var data=eval("("+dt+")");
			if(data.success){
				$("#fileId").val(data.file);
				$("#attachmentName").val(data.fileName);
				$("#attachmentDesc").html('<font color="red">'+"文件名:"+data.fileName+",文件号:"+data.file+",已成功上传至文件服务器!"+'</font>');
			}else{
				$("#attachmentDesc").html('<font color="red">'+"文件名:"+file+","+data.msg+'</font>');
			}
			$("#uploadFile").removeAttr("disabled");
		}
	});
	
	
	
	//保存按钮事件
	$("#saveButton").bind("click",function(){
			//获取页面参数
			var orderId = $("#orderId").val();
			var fileId = $("#fileId").val();
			var attachmentName = $("#attachmentName").val();
			var orderRemark = $("#orderRemark").val();
			var operation=$("#operation").val();
			var confirmType=$("#confirmType").val();
			//alert(confirmType+"--"+"${RequestParameters.isSupplierOrder}");
			if("FAX"==confirmType || "true"=="${RequestParameters.isSupplierOrder}" || "EBK"==confirmType ){
			
			}else{
			
				if(fileId == ""){
					$.alert("您选择的附件未上传成功，文件号不能为空!");
					return;
				}
				
				if(attachmentName == ""){
					$.alert("您选择的附件未上传成功，文件名不能为空!");
					return;
				}
			}
			
				
			
			var param={orderId:orderId,operation:operation,fileId:fileId,confirmType:confirmType,attachmentName:attachmentName,orderRemark:orderRemark};
			
			//异步调用后台
			$.ajax({
			   url : "/vst_order/order/orderStatusManage/updateOrderStatus.do",
			   data : param,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
			   		if( result.code=="success"){
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
 	certificateDialog.close();
});
</script>