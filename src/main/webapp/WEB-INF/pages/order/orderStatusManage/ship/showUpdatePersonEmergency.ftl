<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单</title>
</head>
<body>
<form action="#" method="post" id="dataForm">	
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
    <table class="p_table table_center mt5">
                <thead>
                    <tr>
                        <th >中文姓名</th>
                        <th>联系电话</th>
                    </tr>
                </thead>
                <tbody>
                    
                <#list ordPersonEmergencyList as person> 
                  	<input type="hidden" name="ordPersonList[${person_index}].ordPersonId" value="${person.ordPersonId}">
                    <tr>
                        <td>
                        <input type="text" name="ordPersonList[${person_index}].fullName" style="width:50px;" value="${person.fullName!''}" maxLength=10>
                        </td>
                       <td>
                        <input type="text" name="ordPersonList[${person_index}].mobile"  style="width:100px;" value="${person.mobile!''}" number=true   maxLength=11>
                       </td>
                    </tr>
                </#list>
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
	//遮罩层
    var loading = pandora.loading("正在努力保存中...");		
		
	$.ajax({
	   url : "/vst_order/order/orderShipManage/updateOrdPersonEmergency.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			loading.close();
   			editPersonEmergencyButtonDialog.close();
	   		parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	editPersonEmergencyButtonDialog.close();
});
</script>