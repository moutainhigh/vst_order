<form action="#"  enctype="multipart/form-data" method="post" target="ajaxUpload"  id="orderAttachmentForm">
    <table class="p_table form-inline">
    <tbody>
    	<input type="hidden" name ="orderIds" id="orderIds" value=${RequestParameters.orderIds}>
          <tr> 
            <td><span class="notnull">*</span>请选择文件：</td>  
            <td class="querytd">
               <input type="hidden" id="fileId"/>
			   <input type="hidden" id="fileName"/>
               <input type="file" id="uploadFile" style="width:60px" serverType="ORDER_ATTACHMENT"/>
            </td>
        </tr> 
        <tr>
        	<td>提示信息：</td>
        	<td class="querytd"><span id="attachmentDesc"></span></td>
        </tr>
         <tr> 
            <td>备注信息：</td>  
            <td class="querytd">
               <textarea class="w35 textWidth" 
	               errorEle="errorEle30" 
	               style="height:80px" 
	               id="memo" 
	               name="memo" 
	               placeholder="" 
	               autoValue="true"></textarea>
            </td>
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
<script type="text/javascript">
	//上传按钮事件
	$("#uploadFile").fileUpload({
		onSubmit:function() {
			$("#uploadFile").attr("disabled", true);
			$("#attachmentDesc").html('<font color="red">正在努力的上传中，请稍后......</font>');
		},
		onComplete:function(file,dt){
			var data=eval("("+dt+")");
			if(data.success){
				$("#fileId").val(data.file);
				$("#fileName").val(data.fileName);
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
			var orderIds = $("#orderIds").val();
			var fileId = $("#fileId").val();
			var fileName = $("#fileName").val();
			var memo = $("#memo").val();
			if(fileId == ""){
				alert("您选择的文件未上传成功，文件号不能为空!");
				return;
			}
			
			if(fileName == ""){
				alert("您选择的文件未上传成功，文件名不能为空!");
				return;
			}
			//异步调用后台
			$.ajax({
			   url : "/vst_order/order/orderManage/addOrderNoticeRegiment.do",
			   data : {orderIds:orderIds,fileId:fileId,fileName:fileName,memo:memo},
			   type:"POST",
			   dataType:"JSON",
			   success : function(data){
			   		if(data.result=="success"){
						alert("保存成功！");
						$("#searchForm").submit();
						//monitorShipListDialog.location.reload();
						//uploadNoticeDialog.close();
			   		}else {
			   		  	alert("保存失败！");
			   		}
			   }
			});	
	});
	
	//取消按钮事件
	$("#closeButton").bind("click", function() {uploadNoticeDialog.close();});
</script>
