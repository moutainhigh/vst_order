<form action="#" method="post" id="dataFormVisaDocLastTime">
    <table class="p_table form-inline">
        <tbody>
           
           	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	
              <tr>
                <td class="p_label"><span class="notnull"></span>原材料截止收取时间：</td>
                <td>
                	<input type="hidden" name="oldVisaDocLastTime" value="${RequestParameters.visaDocLastTime}">
                	${RequestParameters.visaDocLastTime!''}
                </td>
              </tr>
              
                <tr>
                <td class="p_label"><span class="notnull"></span> 修改材料截止收取时间：</td>
                <td>
                
                	<input id="d4321" class="Wdate" type="text" value="${RequestParameters.visaDocLastTime!''}"  required
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" errorele="selectDate" name="visaDocLastTime" readonly="readonly">
	                    	
	                    	
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
	if(!$("#dataFormVisaDocLastTime").validate().form()){
		return;
	}
	$.ajax({
	   url : "/vst_order/order/orderManage/updateVisaDocLastTime.do",
	   data : $("#dataFormVisaDocLastTime").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			updateVisaDocLastTimeDialog.close();
	   		 parent.window.location.reload();
   		}else {
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	updateVisaDocLastTimeDialog.close();
});
</script>