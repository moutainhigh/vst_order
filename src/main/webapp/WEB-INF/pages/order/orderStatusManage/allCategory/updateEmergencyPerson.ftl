<form action="#" method="post" id="dataForm">
	<input type="hidden" name="ordPersonId" value="${emergencyPerson.ordPersonId}">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	
     <table class="p_table table_center mt20">
                <thead>
                <tr>
                    <th>中文姓名</th>
                    <th>联系手机</th>
                </tr>
                </thead>
                <tbody>
                    
                     <tr>
                    
                        <td>
                        <input type="text" name="fullName" style="width:100px;" value="${emergencyPerson.fullName!''}" maxLength=10>
                     
                        </td>
                         <td>
                          <input type="text" name="mobile" style="width:150px;" value="${emergencyPerson.mobile!''}" maxLength=11 number=true>
                        
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
   			editEmergencyPersonButtonDialog.close();
	   		 parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	editEmergencyPersonButtonDialog.close();
});
</script>