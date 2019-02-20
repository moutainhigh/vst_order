<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>

</head>
<body>
<form action="#" method="post" id="ordAddressPersonForm">
	<input type="hidden" name="orderId" id="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="ordPersonId" id="orderItemId" value="${RequestParameters.ordPersonId}">
	<input type="hidden" name="ordAddressId" id="orderRemark" value="${RequestParameters.ordAddressId}">
	
    <table class="p_table form-inline">
        <tbody>
        	<tr>
				<td class="p_label" ><span class="notnull"></span>收件人:</td>
                <td>

						<input  type="text" value="${addressPerson.fullName!''}"  name="fullName" required="true">
	                    	
                </td>
                <td class="p_label"><span class="notnull"></span>联系电话:</td>
                <td>

						<input  type="text" value="${addressPerson.mobile!''}" name="mobile"  required="true">
	                    	
                </td>
               </tr>
                <tr>  
                 <td class="p_label"><span class="notnull"></span>详细地址:</td>
                <td>
						<input  type="text" value="${ordAddress.province}" name="resourceRetentionTime" readOnly="true">
						</br>
						<input  type="text" value="${ordAddress.city}" name="resourceRetentionTime" readOnly="true">
						</br>
						<input  type="text" value="${ordAddress.street}" name="street" required="true">
                </td>
                
                 <td class="p_label"><span class="notnull"></span>邮政编码:</td>
                <td>

						<input  type="text" value="${ordAddress.postalCode!''}" name="postalCode" required="true">
	                    	
                </td>
             </tr>
            
        </tbody>
    </table>
</form>
</body>
</html>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">保存</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">

$("#sendButton").bind("click",function(){
	
	
	//验证
	if(!$("#ordAddressPersonForm").validate().form()){
		return;
	}
	//遮罩层
    var loading = pandora.loading("正在努力保存中...");		
		
		
	
    var url="/vst_order/order/orderManage/updateOrdAddress.do";
 
       			
	$.ajax({
			   url : url,
			   data : $("#ordAddressPersonForm").serialize(),
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

    

//取消按钮事件
$("#closeButton").bind("click", function() {
	editOrdAddressButtonDialog.close();
});
</script>



