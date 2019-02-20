<form action="#" method="post" id="sendSmsDataForm">
	<input type="hidden" name="orderId" id="orderId" value="${RequestParameters.orderId}">
	<input type="hidden" name="orderItemId" id="orderItemId" value="${RequestParameters.orderItemId}">
	<input type="hidden" name="orderRemark" id="orderRemark" value="${RequestParameters.orderRemark}">
	<input type="hidden" name="operation" id="operation" value="${RequestParameters.operation}">
	<input type="hidden" name="resourceStatus" id="resourceStatus"  value="${RequestParameters.resourceStatus}">
	<input type="hidden" name="orderCatType" id="orderCatType"  value="${RequestParameters.orderCatType}">
	
    <table class="p_table form-inline">
        <tbody>
        	<tr>
				<td class="p_label"><span class="notnull"></span>资源保留时间（可为空）:</td>
                <td>

						<input id="d4321" class="Wdate" type="text" value="${resourceRetentionTime!''}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'${kssj!''}',maxDate:'${jssj!''}'})" errorele="selectDate" name="resourceRetentionTime" readonly="readonly">
	                    	
                </td>
             </tr>
            
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">资源审核通过</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">

$("#sendButton").bind("click",function(){
	
	var formData=$("#sendSmsDataForm").serialize()+"&cancelCode=''&cancleReasonText=''";
	
	var orderId = $("#orderId").val();
	var orderItemId = $("#orderItemId").val();
	var orderRemark = $("#orderRemark").val();
	var operation=$("#operation").val();
	var resourceStatus = $("#resourceStatus").val();
	var orderCatType=$("#orderCatType").val();
	
    var url="/vst_order/order/orderShipManage/updateChildOrderStatus.do";
    if(orderCatType=='hotel')
    {
    	url="/vst_order/order/orderStatusManage/updateOrderStatus.do";
    }
       			
	$.ajax({
			   url : url,
			   data : $("#sendSmsDataForm").serialize(),
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
	retentionTimeDialog.close();
});
</script>



