<form action="#" method="post" id="dataFormPayment" style="height:90px;">
<input type="hidden" name="orderId" value="${orderId}" />
<input type="hidden" name="oughtAmount" value="${oughtAmount}" />
    <table class="e_table form-inline">
        <tbody>
              <tr>
                <td class="e_label">支付方式：</td>
                	<td>
                	<input type="radio" name="payType" value="FULL"    id="onFullPayment"/>全额支付 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                	<input type="radio" name="payType" value="PART"  id="onReservePayment"/>定金支付
                </td>
                <td></td>
              </tr>
                  <tr>
	                <td class="e_label"> 定金金额：</td>
	                <td>
	                	<input  type="text" name="payAmount"  />
	                </td>
	                <td style="color:red; font-size:14px;">
	                	定金金额需大于零，小于订单金额
	                </td>
                  </tr>
        </tbody>
    </table>
</form>
 <div align="center">
		<button class="pbtn pbtn-small btn-ok" id="editButton">保存</button>
		&nbsp;&nbsp;
		<button class="pbtn pbtn-small btn-ok" id="closeButton">取消</button>
</div>
<#include "/base/foot.ftl"/>
<script type="text/javascript">
$(document).ready(function(){
	var orderDownpay='${orderDownpay}';
	  if(orderDownpay != ""){
		  	var paymentType='${orderDownpay.payType}';
			var paymentStatus='${orderDownpay.payStatus}';
			var partAmount='${orderDownpay.payAmount}';
			//全额未支付
			if(paymentType == 'FULL' && paymentStatus == 'UNPAY'){
				$("#onFullPayment").attr("checked","true");
				 $("table tr:eq(1)").hide();
			}
			//全额已支付
			if(paymentType == 'FULL' && paymentStatus == 'PAYED'){
				 $("table tr:eq(0) td:eq(1)").hide();
				 $("table tr:eq(1)").hide();
				 $("#editButton").hide();
				 $("table tr:eq(0) td:eq(2)").html("全额已付款");
			}
			//定金未支付
			if(paymentType == 'PART' && paymentStatus == 'UNPAY'){
				$("#onReservePayment").attr("checked","true");
				$("input[name='payAmount']").val(partAmount/100);
			}
			//定金已支付
			if(paymentType == 'PART' && paymentStatus == 'PAYED'){
				$("table tr:eq(0) td:eq(1)").hide();
				 $("table tr:eq(1)").hide();
				 $("#editButton").hide();
				 $("table tr:eq(0) td:eq(2)").html("定金已付款");
			}
		
		}else{
				$("table tr:eq(1)").hide();
				$("#onFullPayment").attr("checked","true");
		}
		
		$("#onFullPayment").click(function(){
			$("table tr:eq(1)").hide();
		});
		$("#onReservePayment").click(function(){
			 $("table tr:eq(1)").show();
		});
});
	$("#closeButton").bind("click", function() {
	 	paymentTermDialog.close();
	});
$("#editButton").bind("click",function(){
	  if($('#onReservePayment:checked').val()){
		   var checknum=/^(([1-9]\d{0,9})|0)(\.\d{1,2})?$/g;
		   var reserveMoney=$("input[name='payAmount']").val();
		   if(reserveMoney == "" || reserveMoney == null ){
		   		alert("请输入定金支付金额");
		   		return;
		   }
		   if(!checknum.test(reserveMoney)){
		   		alert("只能输入数字或者两位小数的数字");
		   		return;
		   }
		   if(reserveMoney >= $("input[name='oughtAmount']").val()/100 ){
		   		alert("定金金额不能大于等于应收金额");
		   		return;
		   }
		   if(reserveMoney <= 0){
		   		alert("定金金额不能小于等于0");
		   		return;
		   }
	}
   //是否全额付款
  	if($('#onFullPayment:checked').val()){
  		$("input[name='payAmount']").val($("input[name='oughtAmount']").val());
  	}else{
  		$("input[name='payAmount']").val($("input[name='payAmount']").val()*100);
  	}
   
  	//遮罩层
	var loading = pandora.loading("正在努力保存中...");
   
	$.ajax({
	   url : "/vst_order/ord/order/saveOrUpdatePaymentManner.do",
	   data : $("#dataFormPayment").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
	   			alert(result.message);
	   			paymentTermDialog.close();
   			   loading.close();
	   			$(".payment_table").show();
	   			$(".payment_table tr:eq(0) td:eq(0)").html(result.attributes['payType']+'：');
	   			$(".payment_table tr:eq(0) td:eq(1)").html("RMB&nbsp;"+result.attributes['payAmount']/100+'元&nbsp;&nbsp;'+result.attributes['payStatus']);
   		
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
});
</script>