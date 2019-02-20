<#--页眉-->
<#import "/base/spring.ftl" as s/>
<form method="POST" action="/vst_order/ord/book/ticket/saveOrderPerson.do" name='orderInfoForm' id="orderInfoForm" onsubmit="return false">
	<input type="hidden" id="orderId" name="orderId" value="${order.orderId}"/>
	<#if travellerLockFlag=='Y'>
	<div style="text-align:center;background-color: #DDDDDD;color:#FF0000">
		出游人信息前台已锁定
	</div>
	</#if>
	<div id="dialogBody" class="dialog-body">
		  <table class="e_table form-inline mt10">
                <tbody>
                	<#include "/order/requiredItem/insurerInfo.ftl"/>
                </tbody>
            </table>
	</div>
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">

<p align="center">
<button class="pbtn pbtn-small btn-ok editButton" style="margin-top:20px;">保存并重新投保</button>
&nbsp;&nbsp;
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
</form>
<script type="text/javascript" src="/vst_order/js/book/order_travellers.js"></script>
<script>
var contactAddDialog,districtSelectDialog,selectSupplierDialog;
var $form =null;
$("button.editButton").bind("click",function(){
	$form=$(this).parents("form");
	//var flag = $(this).attr("flag");
	//$("#updateSendContract").val(flag);
	//	if($("#travellerDelayFlag").val()=='Y' && $("#updateSendContract").val()=='Y'){
			/* if($("#paymentStatus").val()!='PAYED'){
				alert("订单付款后方能锁定出游人并更新发送合同");
				return;
			} */
	//		editPersonButtonDialogContiune = 
	//			new xDialog("/vst_order/order/orderManage/editPersonCountiune.do",{},{title:"系统提示",width:700,dialogAutoStop: true});
	//			return;
	//	}else{
			travellerLockFlag();
	//	}
		
});


$("#closeButton").bind("click", function() {
	editInsurePersonButtonDialog.close();
});

function travellerLockFlag(){
	
	//验证
	if(!$form.validate().form()){
		return false;
	}
	var msg="";
	var checkFlag=true;
	$form.find('input[type=text]').each(function(){
		 var text=$(this).val();
		 var nameType=$(this).attr("name_type");
		 var requiredTpe=$(this).attr("required");
		 if(requiredTpe || text!=""){
			 if(nameType=="email"){
			 	 //var patt1 = new RegExp("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$");
			 	 var patt1 = new RegExp("^([a-zA-Z0-9\.\_\-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$");
			 	 if(!patt1.test(text)){
					//提示
					$(this).next("span").css("display","");
					checkFlag=false;
				}else{
					$(this).next("span").css("display","none");
				}
			 }else if(nameType=="mobile"){
			 	 //var mobileReg = "^0?1[3|4|5|8][0-9]\d{8}$";
			 	 var myreg = /^\d+$/;
				if(!myreg.test(text)){
					//提示
					$(this).next("span").css("display","");
					checkFlag=false;
				}else{
					$(this).next("span").css("display","none");
				}
			 }else if(nameType=="idNo"){
			 	var idType=$(this).prev("select").val();
			 	var num = $(this).prev("select").attr("num");
			 	if(idType!="CUSTOMER_SERVICE_ADVICE"){
				 	if(idType=="ID_CARD"){
				 		 var patt1 = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
					 	 if(!patt1.test(text)){
							//提示
							$(this).next("span").css("display","");
							checkFlag=false;
						}else{
							$(this).next("span").css("display","none");
						}
						// 如果证件类型是身份证，将性别置为空
						var $gender=$("select[name='insurers["+num+"].gender']");
						$gender.attr("value", null);
				 	}else{
				 		var $birthdayInput=$("input[name='insurers["+num+"].birthday']");
				 		if($.trim($birthdayInput.val())=="" && requiredTpe){
				 			$(this).parent().next("span").css("display","");
				 			checkFlag=false;
				 		}else{
				 			$(this).parent().next("span").css("display","none");
				 		}
				 	}
			 	}
			 }
		 }
	});
	
	var isIdCardSame = {};
	//默认是没有相同类型的证件和证件号
	var isIdCardSameB=true;
	$form.find('input[type=text]').each(function(){
		var text=$(this).val();
		var requiredTpe=$(this).attr("required");
		var nameType=$(this).attr("name_type");
		var idType=$(this).prev("select").val();
		if(requiredTpe &&  nameType== "idNo"){
			if(idType!="CUSTOMER_SERVICE_ADVICE"){
				var idType=$(this).prev("select").val();
				var num = $(this).prev("select").attr("num");
				 if(isIdCardSame.num&&isIdCardSame.num==text){
					//如果证件类型一样并且证件号一样 不能提交
					isIdCardSameB=false;
				}else{
					isIdCardSame.num=text;
				}  
			}
		}
	});
	
	if(!checkFlag){
		return;
	}
	
	if(!isIdCardSameB){
		alert("相同证件类型，证件号不能一样");
		return;
	}
	
	//遮罩层
	var loading = pandora.loading("正在努力保存中...");		
	$.ajax({
	   url : "/vst_order/ord/order/update/updateInsurePersonList.do",
	   data : $form.serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			loading.close();
   			editInsurePersonButtonDialog.close();
	   		parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	}); 
}


//当游玩人fullName变动的时候，改当地游关联的人的名称
$('[name_type=fullName][name^=insurers]').live('blur',function(){
	var current = $(this);
	var ordPersonId = current.closest("table").siblings("[name$=ordPersonId]").val();
	$("[name=orderItemTd] [name^=personRelationMap]").each(function(index, e){
		var _ordPersonId = $(e).val();
		if(ordPersonId == _ordPersonId) {
			$(e).siblings("span").text(current.val());
		}
	});
});
</script>