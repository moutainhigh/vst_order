<#include "/base/head_meta.ftl"/>
<style type="text/css">
	div ul { padding: 20px; }
	div ul li { height: 40px; line-height: 40px; list-style: none; text-align:center; }
	span { width: 30%; }
	.pbtn { padding:5px 0; width: 70px; }
</style>
<form action="#" method="post" id="dataForm">
	<div>
		<ul>
			<li><span>确认重发邮件给</span>
            	<input type="text" id="email" name="email" value="${email }" style="width:280px;height:30px; ">吗？
            </li>
            </ul>
            <input type="hidden" name="orderId" value="${orderId }">
            <input type="hidden" name="orderItemId" value="${orderItemId }">
            <input type="hidden" name="hasSend" id="hasSend">
	</div>
</form>
<p align="center">
	<button class="pbtn" id="editButton">发送</button>
	<button class="pbtn" style="margin-left:70px;" id="closeButton">取消</button>
</p>
<#include "/base/head.ftl"/>
<#include "/base/foot.ftl"/>
<script>
	var sendEmailDialog = parent.sendEmailDialog;
	$("#editButton").bind("click",function(){
        var btnSendEmail = $(this);
		var email = $.trim($('#email').val());
		var orderId= "${orderId !''}";
		var orderItemId= "${orderItemId !''}";
		
		if(email=='' || email==null || email==undefined){
			alert("电子邮件不能为空");
			return;
		}
		$.confirm('你确定重发凭证吗',function(){
			var loading = parent.pandora.loading("正在努力重发邮件，请耐心等待...");
			var param='orderItemId='+orderItemId+"&orderId="+orderId+"&email="+email;
			$.ajax({
            	url : "/vst_order/order/orderManage/resendemail.do?"+param,
                type : 'POST',
                async: false,
                success : function(result){
                    loading.close();
                    $.alert(result.message);         
                    btnSendEmail.attr("disabled",false);
                    setTimeout('prepare()',1000)
                },
                error : function(){
                    $.alert('服务器错误');
                    loading.close();         
                    btnSendEmail.attr("disabled",false);
                }
             });
		});
	});
	function prepare(){
		$("#hasSend").val(Math.random());
        $("#hasSend").change();
	}
	$(function(){
		document.getElementById("hasSend").onchange = function() {
			sendEmailDialog.close();
		}
	})
	$("#closeButton").bind("click", function() {
		sendEmailDialog.close();
	});
</script>