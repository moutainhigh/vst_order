$(function(){
	$("#couponCode").change(function(){
		var code=$("#couponCode").val();
		   if($.trim(code)==""){
			   $("#couponInfoMsg").html("");
			   BACK.BOOK.CHECK.checkStock();
		   }
	});
	$("#couponVerify").click(function(){
        var code=$("#couponCode").val();
        var youhui=$("input[type=radio][name=youhui]:checked").val();
    	var validFlag=true;
        if(youhui!="coupon"){
			 $("#couponInfoMsg").html('<i class="tip-icon tip-icon-error"></i>请选择使用优惠券.');
			 validFlag=false;
    	}
        if($.trim(code)==""){
         $("#couponInfoMsg").html('<i class="tip-icon tip-icon-error"></i>请输入优惠券代码.');
         	validFlag=false;
        }
        if(!validFlag){
        	return;
        }
        $("#couponInfoMsg").html("");
        BACK.BOOK.CHECK.checkStock();
	});
	
	//是否使用优惠券
	$('input[type=radio][name=youhui]').bind('click',function(){
		var v=$(this).val();
		$("#couponChecked").val(v);
		if(v=="false"){
			 
		}
		if(v=="bonus"){
			$("#bonus_number").val("0.00");
			$("#couponCode").val("");
			$("#couponInfoMsg").html("");
		}
		if(v=="coupon"){
			$("#bonus_number").val("");
			$("#bonusInfoMsg").html("");
		}
		BACK.BOOK.CHECK.checkStock();
	});
});