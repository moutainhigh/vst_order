<form action="#" method="post" id="dataFormWaitRetainageTime">
    <table class="p_table form-inline">
        <tbody>
           
           	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	        <input type="hidden" name="stampId" value="${stampId}">
              <tr>
                <td class="p_label"><span class="notnull"></span>原尾款支付等待时间：</td>
                <td>
                	${RequestParameters.waitRetainageTime!''}
                </td>
              </tr>
              
                <tr>
                <td class="p_label"><span class="notnull"></span> 修改尾款支付等待时间：</td>
                <td>
                	<input id="d4321" class="Wdate" type="text" value="${RequestParameters.waitRetainageTime!''}"  required
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="waitRetainageTime" readonly="readonly">
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
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
var contactAddDialog,districtSelectDialog,selectSupplierDialog;

function validateNum(val){//验证整数
var patten = /^-?\d+$/;
return patten.test(val);
 }
 
$("#editButton").bind("click",function(){
	//验证
	if(!$("#dataFormWaitRetainageTime").validate().form()){
		return;
	}
	$.ajax({
	   url : "/vst_order/order/orderManage/updateWaitRetainageTime.do",
	   data : $("#dataFormWaitRetainageTime").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			updateWaitRetainageTimeDialog.close();
	   		 parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	updateWaitRetainageTimeDialog.close();
});
</script>