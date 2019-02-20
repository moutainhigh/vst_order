<form action="#"  enctype="multipart/form-data" method="post" target="ajaxUpload"  id="smsDataForm">
	<input type="hidden" name="orderId" id="orderId" value="${RequestParameters.orderId}">
	
    <table class="p_table form-inline">
        <tbody>
            <tr>
				 <td class="p_label"><span class="notnull">*</span>联系电话:</td>
                <td>

						<input  type="text" value="${mobile!''}" name="mobile" id="mobile" required="true" number="true">
	                    	
                </td>
             </tr>
               <tr> 
               		 <td class="p_label"><span class="notnull">*</span>出团通知内容:</td>
	                <td>
	
						<textarea class="w35 textWidth" 
	               errorEle="errorEle30" 
	               style="height:180px" 
	               id="smsContent" 
	               name="smsContent" 
	               placeholder=""  
	               autoValue="true" required="true" maxLength="500"></textarea>
		                    	
	                </td>
		        </tr>

        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendNoticeButton">发送</button>
	
</p>




<script type="text/javascript">
	//定义公共参数
	var basePath = "/vst_order/pet";
</script>
<script src="/vst_order/js/file/ajaxUpload.js"></script>
<script>

	
	
	
	//保存按钮事件
	$("#sendNoticeButton").bind("click",function(){
	
			//验证
			if(!$("#smsDataForm").validate().form()){
				return;
			}
			//获取页面参数
			var orderId = $("#orderId").val();
			var smsContent=$("#smsContent").val();
			var mobile=$("#mobile").val();
			var param={orderId:orderId,smsContent:smsContent,mobile:mobile};
			//遮罩层
    		var loading = pandora.loading("正在努力保存中...");		
	
			//异步调用后台
			$.ajax({
			   url : "/vst_order/order/orderShipManage/smsSendNoticeRegiment.do",
			   data : param,
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
	

</script>