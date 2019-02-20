<form action="#" method="post" id="dataForm">
	<input type="hidden" name="ordPersonId" value="${ordPerson.ordPersonId}">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	
    <table class="p_table form-inline">
        <tbody>
            <tr>
				<td class="p_label"><span class="notnull"></span>订单确认方式：</td>
                <td>
                	<input type="text" name="confimWay" value='短信' readOnly="true">
                </td>
             </tr>
              <tr>
                <td class="p_label"><span class="notnull"></span>当前联系人姓名：</td>
                <td>
                	<input type="text" name="fullName" value="${ordPerson.fullName!''}" required maxlength="50">
                </td>
               </tr>
               <tr>
                <td class="p_label"><span class="notnull"></span>当前联系人手机：</td>
             
               
                <td>
                	<input type="text" name="mobile" value="${ordPerson.mobile!''}" required number=true>
                </td>
            </tr> 
            <tr>
                <td class="p_label"><span class="notnull"></span>当前联系人email：</td>
             
               
                <td>
                	<input type="text" name="email" value="${ordPerson.email!''}" required email=true>
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

$("#editButton").bind("click",function(){
	//验证
	if(!$("#dataForm").validate().form()){
		return;
	}
	$.ajax({
	   url : "/vst_order/order/orderManage/updatePerson.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			updatePersonDialog.close();
	   		 parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	updatePersonDialog.close();
});
</script>