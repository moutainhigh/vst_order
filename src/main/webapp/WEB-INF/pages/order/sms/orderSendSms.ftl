<#import "/base/spring.ftl" as s/>
<form action="#" method="post" id="sendSmsForm">
	<@s.formHiddenInput "orderSendSmsVO.orderId" />
    <table class="p_table form-inline">
        <tbody>
        	<tr>
				<td class="p_label">联系人手机:</td>
                <td><@s.formInput "orderSendSmsVO.mobile" 'readonly="readonly"'/></td>
             </tr>
            <tr>
				<td class="p_label"><span class="notnull"></span>选择发送短信:</td>
                <td>
	            	<input type="radio" class="checkbox_top" name="ordinarySMS" id="ordinarySMS" value="ordinarySMS"> 短信模板 &nbsp;&nbsp; <input type="radio" class="checkbox_top" name="ordinarySMS" id="ordinarySMS" value="twoDimensional"> 重发凭证
                </td>
            </tr>
            <tr>
				<td class="p_label"><span class="notnull"></span>选择短信模板:</td>
                <td>
	            	<@s.formSingleSelect "orderSendSmsVO.sendNode" smsTempleMap 'style="width:200px;" onchange="selectSmsContent(this.value);"'/>
                </td>
             </tr>
              <tr>
              	<#--可以再次修改-->
                <td class="p_label"><span class="notnull"></span>短信内容：</td>
                <td id="testTd">
                	<@s.formTextarea "orderSendSmsVO.content" 'style="width: 330px; height: 58px;"'/>
                </td>
            </tr> 
        </tbody>
    </table>
</form>
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">发送</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">
//选择模板事件
function selectSmsContent(sendNode){
	if(sendNode == ""){
		alert("请选择一个短信模板发送节点！");
		return;
	}
	$("#content").val("");
	
	var orderId = $("#orderId").val();
	var smsType = $("#ordinarySMS").attr("checked"); 
	$.ajax({
		   url : "/vst_order/ord/order/selectSmsContent.do",
		   data : {orderId:orderId,sendNode:sendNode},
		   type:"POST",
		   dataType:"JSON",
		   success : function(data){
		   		if(data.msg=="success"){
					$("#content").val(data.result);
		   		}else{
		   		  	alert(data.result);
		   		}
		   }
	});	
}

//发送按钮事件
$("#sendButton").bind("click", function() {
	var orderId = $("#orderId").val();
	var smsType = $("input[name='ordinarySMS']:checked").val();
	if(smsType!="ordinarySMS"&&smsType!="twoDimensional"){
	    alert("请选择一种短信类型发送！");
	    return;
	}
	if(orderId==""&&smsType!="twoDimensional"){
		alert("订单编号不能为空!");
		return;
	}
    if(orderId==""&&smsType=="twoDimensional"){
        alert("请记录订单号并联系开发人员!");
        return;
    }
	var mobile = $("#mobile").val();
	if(mobile==""&&smsType!="twoDimensional"){
		alert("订单联系人手机号码不能为空!");
		return;
	}
	var content = $("#content").val();
	if(content==""&&smsType!="twoDimensional"){
		alert("短信内容不能为空!");
		return;
	}
	$.ajax({
		   url : "/vst_order/ord/order/sendSms.do",
		   data : {orderId:orderId,mobile:mobile,content:content,smsType:smsType},
		   type:"POST",
		   dataType:"JSON",
		   success : function(data){
		   		if(data.msg=="success"){
					orderSendSmsDialog.close();
		   		}else{
		   		  	alert(data.result);
		   		}
		   }
	});	
});

//取消按钮事件
$("#closeButton").bind("click", function() {
	orderSendSmsDialog.close();
});
</script>



