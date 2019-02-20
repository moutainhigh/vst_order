<form action="#" method="post" id="sendSmsDataForm">
	<input type="hidden" name="orderMemo" id="orderMemo" value="${RequestParameters.orderMemo}">
	<input type="hidden" name="orderItemId" id="orderItemId" value="${RequestParameters.orderItemId}">
    <input type="hidden" name="auditId" id="auditId" value="${RequestParameters.auditId}">
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
<div style="font-size:16px">备注：若“资源保留时间”为空，则该值默认为“资源审核通过”后的2小时</div>

<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">资源审核通过</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">
    /*
                var r=confirm("确定订单"+orderItemId+"审核通过？");
                if (r==true){
                    //遮罩层
                    var loading = pandora.loading("正在努力保存中...");
                    var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo+"&auditId="+auditId;
                    $.ajax({
                        url : "/vst_order/ord/order/confirm/orderPassInquiryAudit.do",
                        data : formData,
                        type:"POST",
                        dataType:"JSON",
                        success : function(result){
                            if(result.code=="success" ){
                                loading.close();
                                alert(result.message);
                                document.location.reload();
                            }else {
                                loading.close();
                                alert(result.message);
                            }
                        },
                        error: function(XMLHttpRequest, textStatus, errorThrown) {
                            loading.close();
                            if(textStatus=='timeout'){
                                alert("程序运行超时");
                                document.location.reload();
                            }else{
                                alert("程序运行出现异常");
                                document.location.reload();
                            }
                        }
                    });
                }
                */

$("#sendButton").bind("click",function(){
    var auditId = $("#auditId").val();
    var orderItemId = $("#orderItemId").val();
    var orderMemo = $("#orderMemo").val();

    var loading = pandora.loading("正在努力保存中...");

    var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo+"&auditId="+auditId;
    $.ajax({
        url : "/vst_order/ord/order/confirm/orderPassInquiryAudit.do",
        data : $("#sendSmsDataForm").serialize(),
        type:"POST",
        dataType:"JSON",
        success : function(result){
            if(result.code=="success" ){
                loading.close();
                alert(result.message);
                document.location.reload();
            }else {
                loading.close();
                alert(result.message);
            }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            loading.close();
            if(textStatus=='timeout'){
                alert("程序运行超时");
                document.location.reload();
            }else{
                alert("程序运行出现异常");
                document.location.reload();
            }
        }
    });
	
});

    

//取消按钮事件
$("#closeButton").bind("click", function() {
	retentionTimeDialog.close();
});
</script>



