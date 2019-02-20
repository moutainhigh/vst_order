<#import "/base/spring.ftl" as spring/>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderRemark" value="${RequestParameters.orderRemark}">
    <table class="p_table form-inline">
        <tbody>
            <tr>
                <td>
	               
                	<span class="notnull"></span>pay_payment表主键(建议和新订单表订单id对应的记录，如果没有请修改pay_payment中的object_id)：
                	<input type="text" name="paymentId" id="paymentId" >
                </td>
             </tr>
              
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="float:center;margin-top:20px;" id="editButton">支付成功</button>
	&nbsp;&nbsp;
	<!--
	<button class="pbtn pbtn-small btn-ok" style="float:right;margin-top:20px;" id="closeButton">取消</button>
	-->
</p>
</body>
</html>
<script>

$("#editButton").bind("click",function(){
	
	var paymentId=$("#paymentId").val();
	
	
	$.ajax({
	   url : "/vst_order/ord/order/ordPayment.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			//paymentDialog.close();
	   		 //parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
/**
$("#closeButton").bind("click", function() {
 	paymentDialog.close();
});
*/
</script>